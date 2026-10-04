local Path = require("plenary.path")
local async = require("neotest.async")
local command = require("neotest-kotlin.command")
local filter = require("neotest-kotlin.filter")
local gradle = require("neotest-kotlin.gradle")
local lib = require("neotest.lib")
local logger = require("neotest.logging")
local output = require("neotest-kotlin.output")
local project = require("neotest-kotlin.project")
local test_files = require("neotest-kotlin.test_files")

local M = {}

test_files.setup_autocmds()

---@class neotest.Adapter
---@field name string
M.Adapter = { name = "neotest-kotlin" }

---Find the project root directory given a current directory to work from.
---Should no root be found, the adapter can still be used in a non-project context if a test file matches.
---@async
---@param dir string @Directory to treat as cwd
---@return string | nil @Absolute root dir of test suite
function M.Adapter.root(dir)
  return lib.files.match_root_pattern("gradlew")(dir)
end

---Filter directories when searching for test files
---@async
---@param name string Name of directory
---@param rel_path string Path to directory, relative to root
---@param root string Root directory of project
---@return boolean
function M.Adapter.filter_dir(name, rel_path, root)
  return filter.is_test_directory(name)
end

---Whether the file contains tests, see `test_files.lua`.
---Gradle determines the test files of the whole project once, the result is cached.
---@async
---@param file_path string
---@return boolean
function M.Adapter.is_test_file(file_path)
  if not filter.is_test_file(file_path) then
    return false
  end

  return test_files.is_test_file(file_path, M.Adapter.root)
end

---Discovers the tests of a single file with its own Gradle build.
---@async
---@param file_path string Absolute file path
---@return neotest.Tree | nil
local function discover_file(file_path)
  local cwd = M.Adapter.root(file_path)
  local relative_path = Path:new(file_path):make_relative(cwd)

  local results_path = cwd
    .. "/build/kotlinTestDiscover/"
    .. relative_path
    .. ".json"

  -- Gradle only restores the output from its build cache (e.g. after discovering
  -- another file) when it doesn't exist, it is written again anyway
  os.remove(results_path)

  -- discovery uses the sources and classpath of the Gradle project owning the file
  local cmd, args = command.build_discover(
    project.task(project.find(cwd, file_path), command.DISCOVER_TASK),
    file_path,
    results_path
  )

  -- builds of a project run one at a time, see `neotest-kotlin.gradle`
  local status_code, _, stderr = gradle.run(cwd, cmd, args)

  if status_code ~= 0 then
    error(
      string.format(
        "failed to run '%s %s' in %s to discover Kotlin tests with status code %s: %s",
        cmd,
        table.concat(args, " "),
        cwd,
        tostring(status_code),
        stderr
      )
    )
  end

  if not lib.files.exists(results_path) then
    error(
      string.format(
        "failed to run discover, no output file created '%s'",
        results_path
      )
    )
  end

  ---@type string
  local json_content = lib.files.read(results_path)
  return output.json_to_tree(json_content)
end

---Given a file path, parse all the tests within it
---
---The tests of every test file of the project are discovered by the single
---Gradle run determining the test files (see `test_files.lua`), which
---concurrent calls share. Only files without such a result, e.g. modified
---since, are discovered with a Gradle build of their own.
---@async
---@param file_path string Absolute file path
---@return neotest.Tree | nil
function M.Adapter.discover_positions(file_path)
  local discovered = test_files.discovered(file_path, M.Adapter.root)

  if discovered ~= nil and lib.files.exists(discovered) then
    local ok, json_content = pcall(lib.files.read, discovered)
    if ok then
      return output.json_to_tree(json_content)
    end

    logger.warn(
      "neotest-kotlin: failed to read discovered tests, discovering",
      file_path,
      "on its own:",
      json_content
    )
  end

  return discover_file(file_path)
end

---@class Context
---@field results_path string directory the results of every Gradle project are written to, see `project.file_name`
---@field path string path to the directory/file
---@field projects string[]? Gradle projects whose results apply to their own files only, nil when every result applies to the whole tree

---@class neotest.RunSpec
---@field cwd string?
---@field context Context
---@field command string

---Determines the `kotlinTestExecute` tasks running the classes of the given files.
---
---Each task runs in the Gradle project owning the files, so only the projects
---that are needed compile and run tests. When a file belongs to the root
---project, the task of every project runs instead: the root project may not
---have tests itself (e.g. a project without a build file of its own).
---@param root string
---@param paths string[] files (or directories) of the classes to run
---@return string[] tasks
---@return string[]? projects the projects running tests, nil when every project does
local function execute_tasks(root, paths)
  ---@type string[]
  local projects = {}
  ---@type table<string, boolean>
  local seen = {}

  for _, path in ipairs(paths) do
    local project_path = project.find(root, path)
    if project_path == project.ROOT then
      return { command.EXECUTE_TASK }, nil
    end

    if not seen[project_path] then
      seen[project_path] = true
      table.insert(projects, project_path)
    end
  end

  return vim.tbl_map(function(project_path)
    return project.task(project_path, command.EXECUTE_TASK)
  end, projects),
    projects
end

---@param args neotest.RunArgs
---@return nil | neotest.RunSpec | neotest.RunSpec[]
function M.Adapter.build_spec(args)
  local tree = args.tree
  if not tree then
    return nil
  end

  ---@type string
  local results_path = async.fn.tempname()
  local pos = tree:data()
  local root = M.Adapter.root(pos.path)

  ---@type neotest.RunSpec
  local run_spec = {
    cwd = root,
    context = {
      results_path = results_path,
      path = pos.path,
    },
  }

  if pos.type == "dir" or pos.type == "file" then
    -- every top-level namespace id is `<path>::<FQCN>`
    local classes, class_to_path = output.discovered_classes(tree)
    if #classes == 0 then
      logger.debug("neotest-kotlin: no discovered classes in", pos.path)
      return nil
    end

    local tasks, projects = execute_tasks(
      root,
      vim.tbl_map(function(class)
        return class_to_path[class]
      end, classes)
    )
    run_spec.context.projects = projects

    run_spec.command = command.build_execute(
      tasks,
      command.build_classes(classes),
      nil,
      results_path
    )
  elseif pos.type == "namespace" or pos.type == "test" then
    local class, id = output.split_position_id(pos)
    if class == nil then
      error(string.format("unexpected position id '%s'", pos.id))
    end

    local tasks, projects = execute_tasks(root, { pos.path })
    run_spec.context.projects = projects

    run_spec.command = command.build_execute(tasks, class, id, results_path)
  end

  logger.debug("neotest-kotlin: built command", run_spec.command)

  return run_spec
end

---@class neotest.Error
---@field message string
---@field line? integer

---@class neotest.Result
---@field status neotest.ResultStatus
---@field output? string Path to file containing full output data
---@field short? string Shortened output string
---@field errors? neotest.Error[]

---@async
---@param spec neotest.RunSpec
---@param result neotest.StrategyResult
---@param tree neotest.Tree
---@return table<string, neotest.Result>
function M.Adapter.results(spec, result, tree)
  local results_dir = spec.context.results_path

  if tree == nil or not lib.files.exists(results_dir) then
    return {}
  end

  ---@type table<string, neotest.Result>
  local results = {}

  local projects = spec.context.projects
  if projects ~= nil then
    -- the same class may exist in several projects, the results of a project
    -- only apply to its own files
    local root = spec.cwd or M.Adapter.root(spec.context.path)

    for _, project_path in ipairs(projects) do
      local results_path =
        vim.fs.joinpath(results_dir, project.file_name(project_path))

      if lib.files.exists(results_path) then
        results = vim.tbl_extend(
          "force",
          results,
          output.json_to_results(
            tree,
            lib.files.read(results_path),
            function(path)
              return project.find(root, path) == project_path
            end
          )
        )
      end
    end
  else
    ---@type string[]
    local names = {}
    for name, type in vim.fs.dir(results_dir) do
      if type == "file" and vim.endswith(name, ".json") then
        table.insert(names, name)
      end
    end
    table.sort(names)

    for _, name in ipairs(names) do
      results = vim.tbl_extend(
        "force",
        results,
        output.json_to_results(
          tree,
          lib.files.read(vim.fs.joinpath(results_dir, name))
        )
      )
    end
  end

  return results
end

return M.Adapter

local Path = require("plenary.path")
local async = require("neotest.async")
local command = require("neotest-kotlin.command")
local filter = require("neotest-kotlin.filter")
local lib = require("neotest.lib")
local output = require("neotest-kotlin.output")
local treesitter = require("neotest-kotlin.treesitter")

local M = {}

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

---@async
---@param file_path string
---@return boolean
function M.Adapter.is_test_file(file_path)
  return filter.is_test_file(file_path)
end

---Given a file path, parse all the tests within it
---@async
---@param file_path string Absolute file path
---@return neotest.Tree | nil
function M.Adapter.discover_positions(file_path)
  local cwd = M.Adapter.root(file_path)
  local relative_path = Path:new(file_path):make_relative(cwd)

  local results_path = cwd
    .. "/build/kotlinTestDiscover/"
    .. relative_path
    .. ".json"

  local cmd, args = command.build_discover(relative_path)

  local process, errors = async.process.run({
    cmd = cmd,
    args = args,
    cwd = vim.fs.normalize(cwd),
  })

  if errors ~= nil then
    error(string.format("failed to run Kotlin test discovery: %s", errors))
  end

  local status_code = process.result(false)
  if errors ~= nil or status_code ~= 0 then
    error(
      string.format(
        "failed to run '%s %s' in %s to discover Kotlin tests with status code %d: %s",
        cmd,
        table.concat(args, " "),
        cwd,
        status_code,
        process.stderr.read()
      )
    )
  end

  process.close()

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

---Determines the package of a directory
---@param dir string
---@return string? package
local function dir_determine_package(dir)
  if not lib.files.is_dir(dir) then
    error(string.format("expected '%s' be a directory, but it's not", dir))
  end

  local test_file = nil
  local files = vim.fn.globpath(dir, "**/*.kt", false, true)
  for _, file in ipairs(files) do
    if filter.is_test_file(file) then
      test_file = file
      break
    end
  end

  if test_file == nil then
    return nil
  end

  return treesitter.java_package(test_file)
end

---@class Context
---@field results_path string path to the results file
---@field path string path to the directory/file

---@class neotest.RunSpec
---@field cwd string?
---@field context Context
---@field command string

---@param args neotest.RunArgs
---@return nil | neotest.RunSpec | neotest.RunSpec[]
function M.Adapter.build_spec(args)
  local tree = args.tree
  if not tree then
    return nil
  end

  ---@type string
  local results_path = async.fn.tempname() .. ".json"
  local pos = tree:data()

  ---@type neotest.RunSpec
  local run_spec = {
    cwd = M.Adapter.root(pos.path),
    context = {
      results_path = results_path,
      path = pos.path,
    },
  }

  if pos.type == "dir" then
    local package = dir_determine_package(pos.path) or ""
    run_spec.command = command.build_execute(package, nil, results_path)
  elseif pos.type == "namespace" or pos.type == "test" then
    local segments = vim.split(pos.id, "::")

    run_spec.command = command.build_execute(
      segments[2],
      table.concat(segments, "::", 2),
      results_path
    )
  elseif pos.type == "file" then
    local package = string.format(
      "%s.%s",
      treesitter.java_package(pos.path),
      treesitter.list_all_classes(pos.path)[1]
    )

    run_spec.command = command.build_execute(package, nil, results_path)
  end

  print(run_spec.command)

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
  local result_path = spec.context.results_path
  local path = spec.context.path

  if not lib.files.exists(result_path) then
    return {}
  end

  ---@type string
  local json_content = lib.files.read(result_path)
  return output.json_to_results(path, json_content)
end

return M.Adapter

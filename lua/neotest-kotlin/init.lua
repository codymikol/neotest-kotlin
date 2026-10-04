local Path = require("plenary.path")
local async = require("neotest.async")
local command = require("neotest-kotlin.command")
local filter = require("neotest-kotlin.filter")
local lib = require("neotest.lib")
local logger = require("neotest.logging")
local output = require("neotest-kotlin.output")

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

  if pos.type == "dir" or pos.type == "file" then
    -- every top-level namespace id is `<path>::<FQCN>`
    local classes = output.discovered_classes(tree)
    if #classes == 0 then
      logger.debug("neotest-kotlin: no discovered classes in", pos.path)
      return nil
    end

    run_spec.command =
      command.build_execute(command.build_classes(classes), nil, results_path)
  elseif pos.type == "namespace" or pos.type == "test" then
    local class, id = output.split_position_id(pos)
    if class == nil then
      error(string.format("unexpected position id '%s'", pos.id))
    end

    run_spec.command = command.build_execute(class, id, results_path)
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
  local result_path = spec.context.results_path

  if tree == nil or not lib.files.exists(result_path) then
    return {}
  end

  ---@type string
  local json_content = lib.files.read(result_path)
  return output.json_to_results(tree, json_content)
end

return M.Adapter

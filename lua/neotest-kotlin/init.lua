local async = require("neotest.async")
local command = require("neotest-kotlin.command")
local filter = require("neotest-kotlin.filter")
local lib = require("neotest.lib")
local output_parser = require("neotest-kotlin.output_parser")
local treesitter = require("neotest-kotlin.treesitter")

local M = {}

---@class neotest.Adapter
---@field name string
M.Adapter = { name = "neotest-kotest" }

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
  return treesitter.parse_positions(file_path)
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
    return
  end

  ---@type string
  local results_path = async.fn.tempname() .. ".txt"
  local pos = tree:data()
  local tests = "*"

  ---@type neotest.RunSpec
  local run_spec = {
    cwd = M.Adapter.root(pos.path),
    context = {
      results_path = results_path,
      path = pos.path,
    },
  }

  if pos.type == "dir" then
    local package = dir_determine_package(pos.path) .. ".*"
    run_spec.command = command.build(tests, package, results_path)
  elseif
    pos.type == "file"
    or pos.type == "namespace"
    or pos.type == "test"
  then
    local package = string.format(
      "%s.%s",
      treesitter.java_package(pos.path),
      treesitter.list_all_classes(pos.path)[1]
    )

    run_spec.command = command.build(tests, package, results_path)
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

  ---@type string[]
  local lines = lib.files.read_lines(result_path)
  return output_parser.parse_lines(lines, path)
end

return M.Adapter

local DiscoveryResult = require("neotest-kotlin.output.discovery_result")
local TestResult = require("neotest-kotlin.output.test_result")
local neotest = require("neotest.lib")
local treesitter = require("neotest-kotlin.treesitter")
local types = require("neotest.types")

local M = {}

---Determines all fully qualified classes in the provided file
---@param file string
---@return table<string, string>
local function determine_all_classes_file(file)
  if neotest.files.is_dir(file) then
    error(
      string.format(
        "determine_all_classes_file only operates on files, not directories '%s'",
        file
      )
    )
  end

  ---@type table<string, string>
  local results = {}
  local package = treesitter.java_package(file)
  local classes = treesitter.list_all_classes(file)

  for _, class in ipairs(classes) do
    results[package .. "." .. class] = file
  end

  return results
end

---Determines all fully qualified classes in the provided path
---@param path string
---@return table<string, string>
function M.determine_all_classes(path)
  local results = {}

  if neotest.files.is_dir(path) then
    local files = neotest.files.find(path)

    for _, file in ipairs(files) do
      results =
        vim.tbl_extend("keep", results, determine_all_classes_file(file))
    end
  else
    results = determine_all_classes_file(path)
  end

  return results
end

---Converts JSON of TestNodes to neotest.Results
---@param path string
---@param json_content string
---@return table<string, neotest.Result>
function M.json_to_results(path, json_content)
  ---@type any[]
  local test_results = vim.json.decode(json_content)

  local results = {}
  local class_to_path = M.determine_all_classes(path)

  for _, result_json in ipairs(test_results) do
    local test_result = TestResult.from(result_json)
    local class_path = class_to_path[test_result.className]

    local id, result = test_result:to_result(class_path)
    results[id] = result
  end

  return results
end

---Converts JSON of DiscoveredTests to neotest.Tree
---@param json_content string
---@return types.Tree
function M.json_to_tree(json_content)
  ---@type any[]
  local file_results = vim.json.decode(json_content)
  if #file_results == 0 then
    return {}
  end

  ---@type types.Tree[]
  local results = {}
  for _, json_result in ipairs(file_results) do
    local discovery_result = DiscoveryResult.from(json_result)
    vim.list_extend(results, discovery_result:to_trees())
  end

  return types.Tree.from_list(
    results,
    ---@param types.Tree
    ---@return string
    function(node)
      return node
    end
  )
end

return M

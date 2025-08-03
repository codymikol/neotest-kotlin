local TestNode = require("neotest-kotlin.output.test_node")
local neotest = require("neotest.lib")
local treesitter = require("neotest-kotlin.treesitter")

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
  local test_nodes = vim.json.decode(json_content)

  local results = {}
  local class_to_path = M.determine_all_classes(path)

  for _, node in ipairs(test_nodes) do
    local class_node = TestNode.from(node)
    local class_path = class_to_path[class_node.name]

    results =
      vim.tbl_extend("force", results, class_node:to_results(class_path))
  end

  return results
end

return M

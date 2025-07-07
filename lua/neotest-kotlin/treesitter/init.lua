local neotest = require("neotest.lib")

local class_query = require("neotest-kotlin.treesitter.class-query")
local kotest_query = require("neotest-kotlin.treesitter.kotest-query")
local package_query = require("neotest-kotlin.treesitter.package-query")

local M = {}

---Get all matches for the query perform on the path
---@param path string
---@param query string
---@return string[]
local function get_all_matches_as_string(path, query)
  local language = "kotlin"

  local bufnr = vim.api.nvim_create_buf(false, true)
  local content = vim.fn.readfile(path)
  vim.api.nvim_buf_set_lines(bufnr, 0, -1, false, content)
  vim.api.nvim_set_option_value("filetype", language, { buf = bufnr })

  local parser = vim.treesitter.get_parser(bufnr, language, {})
  if not parser then
    error("Kotlin parser is not available. Please ensure it's installed.")
  end

  local tree = parser:parse()[1]

  ---@type vim.treesitter.Query
  local treesitter_query = vim.treesitter.query.parse(language, query)
  local results = {}

  for _, match, _ in treesitter_query:iter_matches(tree:root(), bufnr, 0, -1) do
    for _, nodes in pairs(match) do
      for _, node in ipairs(nodes) do
        local text = vim.treesitter.get_node_text(node, bufnr)

        if type(text) == "table" then
          table.insert(results, table.concat(text, "\n"))
        else
          table.insert(results, text)
        end
      end
    end
  end

  vim.api.nvim_buf_delete(bufnr, { force = true })

  return results
end

---List all classes in a provided file using treesitter
---@param file string
---@return string[] classes
function M.list_all_classes(file)
  return get_all_matches_as_string(file, class_query)
end

---Get the first java package in a provided file using treesitter
---@param file string
---@return string? package
function M.java_package(file)
  return get_all_matches_as_string(file, package_query)[1]
end

---Uses neotest.treeistter.parse_positions to discover all namespaces/tests in a file.
---@param file string
---@return neotest.Tree?
function M.parse_positions(file)
  return neotest.treesitter.parse_positions(file, kotest_query, {
    nested_namespaces = true,
    nested_tests = false,
    ---@type fun(file_path: string, source: string, captured_nodes: table<string, userdata>, metadata: table<string, vim.treesitter.query.TSMetadata>): neotest.Position|neotest.Position[]|nil Builds one or more positions from the captured nodes from a query match.
    build_position = function(file_path, source, captured_nodes, metadata)
      ---@param captured_nodes table<string, userdata>
      local function get_match_type(captured_nodes)
        if captured_nodes["test.name"] then
          return "test"
        end
        if captured_nodes["namespace.name"] then
          return "namespace"
        end
      end

      local match_type = get_match_type(captured_nodes)
      if match_type then
        local node_name = match_type .. ".name"
        ---@type string
        local name =
          vim.treesitter.get_node_text(captured_nodes[node_name], source)

        if metadata[node_name] ~= nil and metadata[node_name].text ~= nil then
          name = metadata[node_name].text
        end

        local definition = captured_nodes[match_type .. ".definition"]

        return {
          type = match_type,
          path = file_path,
          name = name,
          range = { definition:range() },
        }
      end
    end,
  })
end

return M

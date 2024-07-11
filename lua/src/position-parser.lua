local lib = require("neotest.lib")

local M = {}

M.parse = function(path, query)
  local positions = lib.treesitter.parse_positions(path, query, {
    nested_namespaces = true,
    nested_tests = false,
    fast = false,
  })

  return positions
end

M.get_all_matches_as_string = function(path, query)
  local results = {}

  print(query)

  local file = io.open(path)

  if file == nil then
    error("File not found at path: " .. path)
  end

  local code = file:read("*all")

  local new_buffer_number = vim.api.nvim_create_buf(false, true)
  vim.api.nvim_buf_set_lines(new_buffer_number, 1, -1, false, vim.split(code, "\n"))

  file:close()

  local language = "kotlin"

  local parser = vim.treesitter.get_string_parser(code, language)
  local tree = parser:parse()
  local root = tree[1]:root()

  local query = vim.treesitter.query.parse(language, query)



  for _, match, _ in query:iter_matches(root, new_buffer_number, root:start(), root:end_(), {}) do
    for _, node in pairs(match) do
      local start_row, start_col = node:start()
      local end_row, end_col = node:end_()

      -- string:sub is 1 indexed, but the nodes apis return 0 indexed jawns...
      -- effectively making this a river of brain melting sadness
      local text = code:sub(start_row + 2, end_row - 1):sub(start_col, end_col - 1)

      local row_lines = vim.api.nvim_buf_get_lines(new_buffer_number, start_row + 1, end_row + 2, false)
      print("row lines - start: " .. start_row .. " end: " .. end_row .. vim.inspect(row_lines))


      if #row_lines == 0 then
        print("Error: position parser could not match the passed query.")
        return ""
      end

      if #row_lines > 1 then
        print("Error: position parser currently only supports single line results.")
        return ""
      end

      local found_line = row_lines[1]

      print("found line: " .. found_line)

      local result = found_line:sub(start_col + 1, end_col)

      print("result: " .. result)

      results[#results + 1] = result
    end
  end

  return results
end

-- This will take in a path to a file, run a treesitter query on it, and return the first match as a string.
M.get_first_match_string = function(path, query)
  local results = M.get_all_matches_as_string(path, query)
  if #results > 0 then
    return results[1]
  end
  return nil
end

return M

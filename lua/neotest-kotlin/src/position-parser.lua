local M = {}

---Get all matches for the query perform on the path
---@param path string
---@param query string
---@return string[]
M.get_all_matches_as_string = function(path, query)
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

--- This will take in a path to a file, run a treesitter query on it, and return the first match as a string.
---@param path string path to file
---@param query string treesitter query
---@return string? first_match
M.get_first_match_string = function(path, query)
	local results = M.get_all_matches_as_string(path, query)
	if #results > 0 then
		return results[1]
	end

	return nil
end

return M

local M = {}

local ignored_directories =
	{ "docs", "build", "out", "generated", ".gradle", "main", ".idea", "buildSrc", "kapt", "taret" }

-- This filters out non-test directories that would bog down scnanning.
---@param path string Name of directory
M.is_test_directory = function(path)
	for _, v in ipairs(ignored_directories) do
		if v == path then
			return false
		end
	end
	return true
end

M.is_test_file = function(file_path)
	if file_path == nil then
		return false
	end

	if not vim.endswith(file_path, ".kt") then
		return false
	end

	if string.find(file_path, "src/main") then
		return false
	end

	return true
end

return M

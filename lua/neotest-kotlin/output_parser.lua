local neotest = require("neotest.lib")
local treesitter = require("neotest-kotlin.treesitter")

local M = {}

---Gets the result of a Gradle test output line
---@param line string
---@return string status passed, skipped, failed, none
M.parse_status = function(line)
	local result = "none"

	if vim.endswith(line, "PASSED") then
		result = "passed"
	elseif vim.endswith(line, "SKIPPED") then
		result = "skipped"
	elseif vim.endswith(line, "FAILED") then
		result = "failed"
	end

	return result
end

-- org.example.KotestDescribeSpec > a namespace > should handle failed assertions FAILED
-- '/home/nick/GitHub/neotest-kotlin/lua/tests/example_project/app/src/test/kotlin/org/example/KotestDescribeExample.kt::"a namespace"::"a nested namespace"::"should handle failed assertions"'
---Parses the Neotest id from a Gradle test line output
---@param line string
---@param class_to_path table<string, string> fully qualified class name to path
---@return string? neotest_id
function M.parse_test_id(line, class_to_path)
	local split = vim.split(line, ">", { trimempty = true })
	-- Must have at least "fully qualified test name > test"
	if #split < 2 then
		return nil
	end

	local fully_qualified_class = vim.trim(split[1])
	if class_to_path[fully_qualified_class] == nil then
		return nil
	end

	local names = { unpack(split, 2) }
	local result = class_to_path[fully_qualified_class]

	for i, segment in ipairs(names) do
		segment = vim.trim(segment)
		if (i + 1) == #split then
			segment = segment:match("(.+) [PASSED|FAILED|SKIPPED]")
		end

		-- Deeply nested tests potentially have segments prefixed by the
		-- fully qualified class name.
		--
		-- example: org.example.KotestDescribeExample.this is the test name
		if vim.startswith(segment, fully_qualified_class .. ".") then
			segment = segment:sub(#fully_qualified_class + 2)
		end

		result = result .. '::"' .. segment .. '"'
	end

	return result
end

---Whether the line is a valid gradle test line
---@param line string
---@param class_to_path table<string, string> fully qualified class name to path
---@return boolean
function M.is_valid_gradle_test_line(line, class_to_path)
	if M.parse_status(line) == "none" then
		return false
	end

	for _, class in ipairs(vim.tbl_keys(class_to_path)) do
		if vim.startswith(line, class) then
			return true
		end
	end

	return false
end

---@class TestResult
---@field id string neotest id
---@field status string passed, skipped, failed, none

---@param line string test output line
---@param class_to_path table<string, string> fully qualified class name to path
---@return TestResult?
function M.parse_line(line, class_to_path)
	if not M.is_valid_gradle_test_line(line, class_to_path) then
		return nil
	end

	local id = M.parse_test_id(line, class_to_path)
	if not id then
		return nil
	end

	return {
		id = id,
		status = M.parse_status(line),
	}
end

---Determines all fully qualified classes in the provided file
---@param file string
---@return table<string, string>
local function determine_all_classes_file(file)
	if neotest.files.is_dir(file) then
		error(string.format("determine_all_classes_file only operates on files, not directories '%s'", file))
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
			results = vim.tbl_extend("keep", results, determine_all_classes_file(file))
		end
	else
		results = determine_all_classes_file(path)
	end

	return results
end

---Converts lines of gradle output to test results
---@param lines string[]
---@param path string
---@return table<string, neotest.Result>
M.parse_lines = function(lines, path)
	local results = {}
	local classes = M.determine_all_classes(path)

	for _, line in ipairs(lines) do
		local result = M.parse_line(line, classes)
		if result ~= nil then
			results[result.id] = result
		end
	end

	return results
end

return M

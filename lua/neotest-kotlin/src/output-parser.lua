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
---@param path string
---@param package string
---@return string? neotest_id
M.parse_test_id = function(line, path, package)
	if not M.is_valid_gradle_test_line(line, package) then
		return nil
	end

	local split = vim.split(line, ">", { trimempty = true })
	-- Must have at least "fqn > test"
	if #split < 2 then
		return nil
	end

	local names = { unpack(split, 2) }

	local result = path
	for i, segment in ipairs(names) do
		segment = vim.trim(segment)
		if (i + 1) == #split then
			segment = segment:match("(.+) [PASSED|FAILED|SKIPPED]")
		end

		if vim.startswith(segment, package .. ".") then
			segment = segment:sub(#package + 2)
		end

		result = result .. '::"' .. segment .. '"'
	end

	return result
end

---Whether the line is a valid gradle test line
---@param line string
---@return boolean
function M.is_valid_gradle_test_line(line, package)
	if not vim.startswith(line, package) then
		return false
	end

	return M.parse_status(line) ~= "none"
end

---@class TestResult
---@field id string neotest id
---@field status string passed, skipped, failed, none

---@param line string test output line
---@param path string path to file
---@param package string fully qualified class name
---@return TestResult?
M.parse_line = function(line, path, package)
	if not M.is_valid_gradle_test_line(line, package) then
		return nil
	end

	local id = M.parse_test_id(line, path, package)
	if not id then
		return nil
	end

	return {
		id = id,
		status = M.parse_status(line),
	}
end

---Converts lines of gradle output to test results
---@param lines string[]
---@param path string
---@param package string
---@return table<string, neotest.Result>
M.parse_lines = function(lines, path, package)
	local results = {}

	for _, line in ipairs(lines) do
		local result = M.parse_line(line, path, package)
		if result ~= nil then
			results[result.id] = result
		end
	end

	return results
end

return M

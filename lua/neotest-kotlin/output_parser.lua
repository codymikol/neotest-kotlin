local async = require("neotest.async")
local neotest = require("neotest.lib")
local treesitter = require("neotest-kotlin.treesitter")

local M = {}

---@enum neotest.ResultStatus
local ResultStatus = {
  passed = "passed",
  failed = "failed",
  skipped = "skipped",
  -- None is not part of neotest
  none = "none",
}

---Gets the result of a Gradle test output line
---@param line string
---@return neotest.ResultStatus status passed, skipped, failed, none
function M.parse_status(line)
  ---@type neotest.ResultStatus
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

    result = result .. "::" .. segment
  end

  return result
end

---@param line string
---@param class_to_path table<string, string> fully qualified class name to path
---@return string?
local function find_class_by_line(line, class_to_path)
  return vim.tbl_filter(function(value)
    return vim.startswith(line, value)
  end, vim.tbl_keys(class_to_path))[1]
end

---Whether the line is a valid gradle test line
---@param line string
---@param class_to_path table<string, string> fully qualified class name to path
---@return boolean
function M.is_valid_gradle_test_line(line, class_to_path)
  if M.parse_status(line) == "none" then
    return false
  end

  return find_class_by_line(line, class_to_path) ~= nil
end

---@class TestResult
---@field id string neotest id
---@field status string passed, skipped, failed, none

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

---Parses test output for Kotest
---@param output string[] all lines of output associated with this test failure
---@param class_to_path table<string, string> fully qualified class name to path
---@return string?, neotest.Error[]
local function parse_kotest_assertion_error(output, class_to_path)
  local errors = {}
  local short = nil

  -- Output isn't long enough to have errors
  if #output < 3 then
    return short, errors
  end

  local fully_qualified_class = find_class_by_line(output[1], class_to_path)
  if fully_qualified_class == nil then
    return short, errors
  end

  local file_name = vim.fs.basename(class_to_path[fully_qualified_class])

  -- Match a Kotest soft assertion
  --
  -- Example:
  -- ```text
  -- org.example.KotestFunSpec > namespace > fail FAILED
  -- io.kotest.assertions.MultiAssertionError: The following 3 assertions failed:
  -- 1) expected:<"b"> but was:<"a">
  -- at org.example.KotestFunSpec$1$1$2.invokeSuspend(KotestFunSpec.kt:15)
  -- 2) expected:<"c"> but was:<"b">
  -- at org.example.KotestFunSpec$1$1$2.invokeSuspend(KotestFunSpec.kt:16)
  -- 3) expected:<"d"> but was:<"c">
  -- at org.example.KotestFunSpec$1$1$2.invokeSuspend(KotestFunSpec.kt:17)
  -- ```
  -- Output: {
  --   { message = 'expected:<"b"> but was:<"a">', line = 15 }
  --   { message = 'expected:<"c"> but was:<"b">', line = 16 }
  --   { message = 'expected:<"d"> but was:<"c">', line = 17 }
  -- }
  if output[2]:find("MultiAssertionError") then
    local assertion_count =
      output[2]:match("MultiAssertionError: The following (%d+)")
    local count = tonumber(assertion_count)
    if count == nil then
      return short, errors
    end

    local total_test_assertion_lines = (count * 2) + 2
    if #output < total_test_assertion_lines then
      return short, errors
    end

    for i = 3, total_test_assertion_lines, 2 do
      local message = output[i]:match("%) (.*)")
      local line_number = output[i + 1]:match(file_name .. ":(%d+)")
      table.insert(
        errors,
        { message = vim.trim(message), line = tonumber(line_number) - 1 }
      )
    end
  else
    -- Match a Kotest standard assertion
    --
    -- Example:
    -- ```text
    --org.example.KotestDescribeSpec > a namespace > should handle failed assertions FAILED
    --   io.kotest.assertions.AssertionFailedError: expected:<"b"> but was:<"a">
    --       at app//org.example.KotestDescribeSpec$1$1$4$1.invokeSuspend(KotestDescribeSpec.kt:22)
    --```
    -- Output: { message = "expected:<"b"> but was:<"a">", line = 22 }
    local message = output[2]:match(": (.*)")
    local line_number = output[3]:match(file_name .. ":(%d+)")

    if message == nil or line_number == nil then
      return short, errors
    end

    table.insert(
      errors,
      { message = vim.trim(message), line = tonumber(line_number) - 1 }
    )
  end

  short = table.concat(
    vim.tbl_map(function(value)
      return value.message
    end, errors),
    "\n"
  )

  return short, errors
end

---Converts lines of gradle output to test results
---@param lines string[]
---@param path string
---@return table<string, neotest.Result>
function M.parse_lines(lines, path)
  ---@type neotest.Result[]
  local results = {}
  local classes = M.determine_all_classes(path)

  for i, line in ipairs(lines) do
    if not M.is_valid_gradle_test_line(line, classes) then
      goto continue
    end

    local id = M.parse_test_id(line, classes)
    if not id then
      goto continue
    end

    ---@type string[]
    local output = { line }

    for j = i + 1, #lines do
      if
        vim.trim(lines[j]) == ""
        or M.is_valid_gradle_test_line(lines[j], classes)
      then
        break
      end

      table.insert(output, lines[j])
    end

    local output_path = async.fn.tempname()
    async.fn.writefile(output, output_path)

    local short, errors = parse_kotest_assertion_error(output, classes)
    results[id] = {
      short = short or line,
      status = M.parse_status(line),
      output = output_path,
      errors = errors,
    }

    ::continue::
  end

  return results
end

return M

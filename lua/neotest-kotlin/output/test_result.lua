local async = require("neotest.async")

---@class TestStatus
---@field type string
---@field stackTrace? string only applies to status FAILURE
---@field error? TestNodeStatusError only applies to status FAILURE
---@field reason? string only applies to status IGNORED

---@class TestResult
---@field className string
---@field id string
---@field status TestStatus

local TestResult = {}
TestResult.__index = TestResult

---Creates a new TestResult
---@param id string
---@param className string
---@param status TestStatus
---@return TestResult
function TestResult.new(id, className, status)
  ---@type TestResult
  local self = setmetatable({}, TestResult)
  self.id = id
  self.className = className
  self.status = status

  return self
end

---Creates a TestResult from a table
---@param tbl any
---@return TestResult
function TestResult.from(tbl)
  return setmetatable(tbl, TestResult)
end

---Converts a TestNode.status to a neotest.ResultStatus
---@return neotest.ResultStatus
function TestResult:to_status()
  if self.status.type == "SUCCESS" then
    return "passed"
  elseif self.status.type == "FAILURE" then
    return "failed"
  else
    return "skipped"
  end
end

---Converts a TestResult to a neotest.Result
---@param path string
---@return string, neotest.Result
function TestResult:to_result(path)
  ---@type neotest.Result
  local result = {
    status = self:to_status(),
  }

  if self.status.type == "FAILURE" then
    local error = self.status.error
    assert(error ~= nil, "TestStatus is FAILURE, but has no errors")

    result.short = error.message
    result.errors = {
      { message = error.message, line = error.lineNumber - 1 },
    }

    local output_path = async.fn.tempname()

    async.fn.writefile(
      vim.fn.split(self.status.stackTrace, "\n", false),
      output_path
    )
    result.output = output_path
  end

  return path .. "::" .. self.id, result
end

return TestResult

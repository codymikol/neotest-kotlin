local async = require("neotest.async")

---@class TestStatus
---@field type string
---@field stackTrace? string only applies to status FAILURE
---@field error? TestNodeStatusError only applies to status FAILURE
---@field reason? string only applies to status IGNORED

---@class TestResult
---@field className string
---@field id string empty when the result applies to the entire class
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

---JSON null is decoded as vim.NIL, treat it as nil
---@generic T
---@param value T
---@return T|nil
local function non_null(value)
  if value == vim.NIL then
    return nil
  end

  return value
end

---Whether this TestResult applies to the entire class rather than a single test,
---e.g. an exception in beforeSpec, a class that cannot be instantiated or a test engine error.
---@return boolean
function TestResult:is_class_result()
  local id = non_null(self.id)
  return id == nil or id == ""
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
    local error = non_null(self.status.error)
    local message = error and non_null(error.message)
    local line_number = error and non_null(error.lineNumber)
    local filename = error and non_null(error.filename)

    -- the line is only meaningful in the file of the test, e.g. a failure of a test inherited
    -- from a base class in another file or of an assertion library is shown at the test instead
    if
      line_number ~= nil
      and filename ~= nil
      and vim.fs.basename(filename) ~= vim.fs.basename(path)
    then
      line_number = nil
    end

    result.short = message
    result.errors = {}

    if message ~= nil then
      result.errors = {
        { message = message, line = line_number and line_number - 1 },
      }
    end

    local stack_trace = non_null(self.status.stackTrace)
    if stack_trace ~= nil then
      local output_path = async.fn.tempname()

      async.fn.writefile(vim.fn.split(stack_trace, "\n", false), output_path)
      result.output = output_path
    end
  end

  local id = path .. "::" .. self.className
  if not self:is_class_result() then
    id = id .. "::" .. self.id
  end

  return id, result
end

return TestResult

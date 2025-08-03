local async = require("neotest.async")

---@class TestNodeStatusError
---@field message string
---@field lineNumber integer
---@field filename string

---@class TestNodeStatus
---@field type string
---@field stackTrace? string only applies to status FAILURE
---@field error? TestNodeStatusError only applies to status FAILURE
---@field reason? string only applies to status IGNORED

---@class TestNode
---@field name string
---@field type string
---@field status TestNodeStatus
---@field tests? TestNode[] only applied to type CONTAINER

local TestNode = {}
TestNode.__index = TestNode

---Creates a new Container TestNode
---@param name string
---@param status TestNodeStatus
---@param tests TestNode[]
---@return TestNode
function TestNode.newContainer(name, status, tests)
  ---@type TestNode
  local self = setmetatable({}, TestNode)
  self.name = name
  self.status = status
  self.tests = tests
  self.type = "CONTAINER"

  return self
end

---Creates a new Container TestNode
---@param name string
---@param status TestNodeStatus
---@return TestNode
function TestNode.newTest(name, status)
  ---@type TestNode
  local self = setmetatable({}, TestNode)
  self.name = name
  self.status = status
  self.type = "TEST"

  return self
end

---Creates a TestNode from a table
---@param tbl any
---@return TestNode
function TestNode.from(tbl)
  return setmetatable(tbl, TestNode)
end

---Converts a TestNode.status to a neotest.ResultStatus
---@return neotest.ResultStatus
function TestNode:to_status()
  if self.status.type == "SUCCESS" then
    return "passed"
  elseif self.status.type == "FAILURE" then
    return "failed"
  else
    return "skipped"
  end
end

---Converts a TestNode to a neotest.Result
---@param id string
---@return string, neotest.Result
function TestNode:to_result(id)
  ---@type neotest.Result
  local result = {
    status = self:to_status(),
  }

  if self.status.type == "FAILURE" then
    local error = self.status.error
    assert(error ~= nil, "TestNodeStatus is FAILURE, but has no errors")

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

  return id .. "::" .. self.name, result
end

---Converts a TestNode and all TestNodes contained into neotest.Results
---using Depth First Search.
---@param id string
---@return table<string, neotest.Result>
function TestNode:to_results(id)
  ---@type table<string, neotest.Result>
  local results = {}

  for _, test in ipairs(self.tests or {}) do
    local testNode = TestNode.from(test)

    if testNode.type == "CONTAINER" then
      results = vim.tbl_extend(
        "force",
        results,
        testNode:to_results(id .. "::" .. testNode.name)
      )
    else
      local test_id, result = testNode:to_result(id)
      results[test_id] = result
    end
  end

  return results
end

return TestNode

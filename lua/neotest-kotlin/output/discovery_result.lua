local Discovered = require("neotest-kotlin.output.discovered")
local async = require("neotest.async")

---@class Warning
---@field message string
---@field position Position

---@class DiscoveryResult
---@field tests Discovered[]
---@field warnings Warning[]

local DiscoveryResult = {}
DiscoveryResult.__index = DiscoveryResult

---@param tests Discovered[]
---@param warnings Warning[]
function DiscoveryResult.new(tests, warnings)
  ---@type DiscoveryResult
  local self = setmetatable({}, DiscoveryResult)
  self.tests = tests
  self.warnings = warnings

  return self
end

---@param tbl table
---@return DiscoveryResult
function DiscoveryResult.from(tbl)
  return setmetatable(tbl, DiscoveryResult)
end

---@type number
local neotest_namespace_id = vim.api.nvim_create_namespace("neotest")

---Converts Warning[] to table<bufnr, vim.Diagnostic[]>
---@return table<number, vim.Diagnostic[]>
function DiscoveryResult:to_diagnostics()
  ---@type table<number, vim.Diagnostic[]>
  local results = {}

  for _, warning in ipairs(self.warnings) do
    local bufnr = async.fn.bufnr(warning.position.filename)

    if bufnr ~= nil and bufnr >= 0 then
      ---@type vim.Diagnostic
      local diagnostic = {
        bufnr = bufnr,
        lnum = warning.position.startLine - 1,
        col = warning.position.startColumn - 1,
        end_lnum = warning.position.endLine - 1,
        end_col = warning.position.endColumn - 1,
        message = warning.message,
        namespace = neotest_namespace_id,
        severity = vim.diagnostic.severity.WARN,
        source = "neotest-kotlin",
      }

      local list = results[bufnr] or {}
      table.insert(list, diagnostic)

      results[bufnr] = list
    end
  end

  return results
end

return DiscoveryResult

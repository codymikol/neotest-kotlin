---@class DiscoveryPosition
---@field filename string
---@field start number
---@field end number

---@class DiscoveryResult
---@field id string
---@field name string
---@field position DiscoveryPosition
---@field type "CONTAINER" | "TEST"
---@field tests? DiscoveryResult[] only present in the CONTAINER type

local DiscoveryResult = {}
DiscoveryResult.__index = DiscoveryResult

---@param id string
---@param name string
---@param position DiscoveryPosition
---@param type "CONTAINER" | "TEST"
---@param tests? DiscoveryResult[]
function DiscoveryResult.new(id, name, position, type, tests)
  ---@type DiscoveryResult
  local self = setmetatable({}, DiscoveryResult)
  self.id = id
  self.name = name
  self.position = position
  self.type = type
  self.tests = tests

  return self
end

---@param table tbl
---@return DiscoveryResult
function DiscoveryResult.from(tbl)
  return setmetatable(tbl, DiscoveryResult)
end

---Convert into a neotest.Tree
---@return neotest.Tree[]
function DiscoveryResult:to_trees()
  local results = { self:to_tree() }

  if self.type == "CONTAINER" and self.tests ~= nil then
    for _, test_json in ipairs(self.tests) do
      local test = DiscoveryResult.from(test_json)
      table.insert(results, test:to_trees())
    end
  end

  return results
end

---Convert into a neotest.Tree
---@return neotest.Tree
function DiscoveryResult:to_tree()
  local type = "test"
  if self.type == "CONTAINER" then
    type = "namespace"
  end

  return {
    name = self.name,
    id = self.position.filename .. "::" .. self.id,
    path = self.position.filename,
    range = {
      self.position.start - 1,
      0,
      self.position["end"] - 1,
      0,
    },
    type = type,
  }
end

return DiscoveryResult

---@class Position
---@field filename string
---@field startLine number
---@field endLine number
---@field startColumn number
---@field endColumn number

---@class Discovered
---@field id string
---@field name string
---@field position Position
---@field type "CONTAINER" | "TEST"
---@field tests? Discovered[] only present in the CONTAINER type

local Discovered = {}
Discovered.__index = Discovered

---@param id string
---@param name string
---@param position Position
---@param type "CONTAINER" | "TEST"
---@param tests? Discovered[]
function Discovered.new(id, name, position, type, tests)
  ---@type Discovered
  local self = setmetatable({}, Discovered)
  self.id = id
  self.name = name
  self.position = position
  self.type = type
  self.tests = tests

  return self
end

---@param tbl table
---@return Discovered
function Discovered.from(tbl)
  return setmetatable(tbl, Discovered)
end

---Convert into a neotest.Tree
---@return neotest.Tree[]
function Discovered:to_trees()
  local results = { self:to_tree() }

  if self.type == "CONTAINER" and self.tests ~= nil then
    for _, test_json in ipairs(self.tests) do
      local test = Discovered.from(test_json)
      table.insert(results, test:to_trees())
    end
  end

  return results
end

---Convert into a neotest.Tree
---@return neotest.Tree
function Discovered:to_tree()
  local type = "test"
  if self.type == "CONTAINER" then
    type = "namespace"
  end

  return {
    name = self.name,
    id = self.position.filename .. "::" .. self.id,
    path = self.position.filename,
    range = {
      self.position.startLine - 1,
      self.position.startColumn - 1,
      self.position.endLine - 1,
      self.position.endColumn - 1,
    },
    type = type,
  }
end

return Discovered

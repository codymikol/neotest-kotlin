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
---
---Tests inherited from a superclass or included from a test factory can be declared in
---another file than their class (`position.filename`). A neotest tree only represents
---positions of a single file, signs/diagnostics/"run nearest" use the range of every position in
---the tree with the buffer of the file, so such tests are placed at the range of their top-level
---class in the file of that class instead.
---@param class? Discovered the top-level class this is part of, nil when this is the class itself
---@return neotest.Tree[]
function Discovered:to_trees(class)
  class = class or self
  local results = { self:to_tree(class) }

  if self.type == "CONTAINER" and self.tests ~= nil then
    for _, test_json in ipairs(self.tests) do
      local test = Discovered.from(test_json)
      table.insert(results, test:to_trees(class))
    end
  end

  return results
end

---Convert into a neotest.Tree
---@param class? Discovered the top-level class this is part of, see Discovered:to_trees
---@return neotest.Tree
function Discovered:to_tree(class)
  local type = "test"
  if self.type == "CONTAINER" then
    type = "namespace"
  end

  local position = self.position
  if class ~= nil and position.filename ~= class.position.filename then
    position = class.position
  end

  return {
    name = self.name,
    id = position.filename .. "::" .. self.id,
    path = position.filename,
    range = {
      position.startLine - 1,
      position.startColumn - 1,
      position.endLine - 1,
      position.endColumn - 1,
    },
    type = type,
  }
end

return Discovered

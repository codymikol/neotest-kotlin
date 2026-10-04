local output = require("neotest-kotlin.output")
local types = require("neotest.types")

describe("output", function()
  ---@param type neotest.PositionType
  ---@param path string
  ---@param id string? id relative to the path, nil for files and dirs
  ---@return neotest.Position
  local function position(type, path, id)
    return {
      name = id or vim.fs.basename(path),
      id = id and (path .. "::" .. id) or path,
      path = path,
      type = type,
      range = { 0, 0, 0, 0 },
    }
  end

  ---@param list any[]
  ---@return neotest.Tree
  local function to_tree(list)
    return types.Tree.from_list(list, function(pos)
      return pos.id
    end)
  end

  describe("split_position_id", function()
    it("file", function()
      local class, id = output.split_position_id(position("file", "/a/B.kt"))
      assert.is_nil(class)
      assert.is_nil(id)
    end)

    it("dir", function()
      local class, id = output.split_position_id(position("dir", "/a"))
      assert.is_nil(class)
      assert.is_nil(id)
    end)

    it("namespace", function()
      local class, id = output.split_position_id(
        position("namespace", "/a/B.kt", "org.example.B")
      )
      assert.equals("org.example.B", class)
      assert.equals("org.example.B", id)
    end)

    it("nested test", function()
      local class, id = output.split_position_id(
        position("test", "/a/B.kt", "org.example.B::namespace::pass")
      )
      assert.equals("org.example.B", class)
      assert.equals("org.example.B::namespace::pass", id)
    end)
  end)

  describe("discovered_classes", function()
    it("dir with multiple files and classes", function()
      local tree = to_tree({
        position("dir", "/a"),
        {
          position("file", "/a/A.kt"),
          {
            position("namespace", "/a/A.kt", "org.example.A"),
            { position("test", "/a/A.kt", "org.example.A::pass") },
            { position("test", "/a/A.kt", "org.example.A::fail") },
          },
          { position("namespace", "/a/A.kt", "org.example.A2") },
        },
        {
          position("dir", "/a/b"),
          {
            position("file", "/a/b/B.kt"),
            { position("namespace", "/a/b/B.kt", "org.other.B") },
          },
        },
        -- undiscovered file
        { position("file", "/a/C.kt") },
      })

      local classes, class_to_path = output.discovered_classes(tree)

      assert.are.same(
        { "org.example.A", "org.example.A2", "org.other.B" },
        classes
      )
      assert.are.same({
        ["org.example.A"] = "/a/A.kt",
        ["org.example.A2"] = "/a/A.kt",
        ["org.other.B"] = "/a/b/B.kt",
      }, class_to_path)
    end)

    it("test", function()
      local tree = to_tree({
        position("test", "/a/A.kt", "org.example.A::namespace::pass"),
      })

      local classes, class_to_path = output.discovered_classes(tree)

      assert.are.same({ "org.example.A" }, classes)
      assert.are.same({ ["org.example.A"] = "/a/A.kt" }, class_to_path)
    end)
  end)

  describe("json_to_tree", function()
    it("no tests", function()
      assert.is_nil(output.json_to_tree([[{ "tests": [], "warnings": [] }]]))
    end)

    it("multiple classes", function()
      local json = [[
      {
        "tests": [
          {
            "id": "org.example.First",
            "name": "First",
            "type": "CONTAINER",
            "position": { "filename": "/a/Multiple.kt", "startLine": 3, "endLine": 7, "startColumn": 1, "endColumn": 3 },
            "tests": [
              {
                "id": "org.example.First::pass",
                "name": "pass",
                "type": "TEST",
                "position": { "filename": "/a/Multiple.kt", "startLine": 4, "endLine": 6, "startColumn": 5, "endColumn": 5 }
              }
            ]
          },
          {
            "id": "org.example.Second",
            "name": "Second",
            "type": "CONTAINER",
            "position": { "filename": "/a/Multiple.kt", "startLine": 9, "endLine": 13, "startColumn": 1, "endColumn": 3 },
            "tests": [
              {
                "id": "org.example.Second::pass",
                "name": "pass",
                "type": "TEST",
                "position": { "filename": "/a/Multiple.kt", "startLine": 10, "endLine": 12, "startColumn": 5, "endColumn": 5 }
              }
            ]
          }
        ],
        "warnings": []
      }
      ]]

      local tree = output.json_to_tree(json)
      assert.not_nil(tree)
      assert(tree ~= nil)

      assert.are.same({
        name = "Multiple.kt",
        id = "/a/Multiple.kt",
        path = "/a/Multiple.kt",
        type = "file",
        range = { 0, 0, 12, 0 },
      }, tree:data())

      local children = vim.tbl_map(function(child)
        return child:data().id
      end, tree:children())

      assert.are.same({
        "/a/Multiple.kt::org.example.First",
        "/a/Multiple.kt::org.example.Second",
      }, children)

      local second_pass =
        tree:get_key("/a/Multiple.kt::org.example.Second::pass")
      assert.not_nil(second_pass)
      assert(second_pass ~= nil)
      assert.equals(
        "/a/Multiple.kt::org.example.Second",
        second_pass:parent():data().id
      )

      local classes = output.discovered_classes(tree)
      assert.are.same({ "org.example.First", "org.example.Second" }, classes)
    end)
  end)
end)

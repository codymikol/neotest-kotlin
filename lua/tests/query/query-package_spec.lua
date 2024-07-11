describe("Getting the package of a file", function()
  query = require("src.treesitter.package-query")
  position_parser = require("src.position-parser")
  path = require("plenary.path")

  describe("When the package exists at the top of the file", function()
    it("should return the package name", function()
      local exampleFileLocation = path:new(vim.loop.cwd())
          :joinpath("lua/tests/query/examples/describeSpecExample.kt").filename
      local result = position_parser.get_first_match_string(exampleFileLocation, query)
      assert.equals("com.codymikol.gummibear", result)
    end)
  end)
end)

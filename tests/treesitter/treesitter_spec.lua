local nio = require("nio")
local treesitter = require("neotest-kotlin.treesitter")

describe("treesitter", function()
  local example_project_path = vim.fs.joinpath(
    debug.getinfo(1).source:match("@?(.*/)"),
    "..",
    "example_project",
    "app",
    "src",
    "test",
    "kotlin",
    "org",
    "example"
  )

  local funspec_file = vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

  describe("java_package", function()
    nio.tests.it("valid", function()
      local actual = treesitter.java_package(funspec_file)
      assert.equals("org.example", actual)
    end)
  end)

  describe("list_all_classes", function()
    nio.tests.it("valid", function()
      local actual = treesitter.list_all_classes(funspec_file)
      assert.equals(1, #actual)
      assert.equals("KotestFunSpec", actual[1])
    end)
  end)
end)

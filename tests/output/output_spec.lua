local nio = require("nio")
local output = require("neotest-kotlin.output")

describe("output", function()
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

  describe("determine_all_classes", function()
    nio.tests.it("directory", function()
      local actual = output.determine_all_classes(example_project_path)

      assert.not_nil(actual["org.example.KotestDescribeSpec"])
      assert.is_true(
        vim.startswith(
          actual["org.example.KotestDescribeSpec"],
          example_project_path
        )
      )
    end)

    nio.tests.it("file", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")
      local actual = output.determine_all_classes(test_path)

      assert.not_nil(actual["org.example.KotestDescribeSpec"])
      assert.is_true(
        vim.startswith(actual["org.example.KotestDescribeSpec"], test_path)
      )
    end)
  end)
end)

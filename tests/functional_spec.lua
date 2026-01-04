local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")

describe("discover_positions", function()
  local example_project_path = vim.fs.joinpath(
    debug.getinfo(1).source:match("@?(.*/)"),
    "example_project",
    "app",
    "src",
    "test",
    "kotlin",
    "org",
    "example"
  )

  nio.tests.it("example", function()
    local test_path =
      vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")

    ---@type any[]
    local tree = neotest_kotlin.discover_positions(test_path):to_list()

    assert.are.same({
      id = test_path,
      path = test_path,
      name = "KotestDescribeSpec.kt",
      range = {
        0,
        0,
        33,
        0,
      },
      type = "file",
    }, tree[1])

    assert.are.same({
      id = test_path .. "::" .. "org.example.KotestDescribeSpec",
      path = test_path,
      name = "KotestDescribeSpec",
      range = {
        5,
        0,
        33,
        0,
      },
      type = "namespace",
    }, tree[2][1])

    assert.are.same({
      id = test_path
        .. "::"
        .. "org.example.KotestDescribeSpec"
        .. "::"
        .. "a namespace",
      path = test_path,
      name = "a namespace",
      range = {
        6,
        0,
        32,
        0,
      },
      type = "namespace",
    }, tree[2][2][1])

    assert.are.same({
      id = test_path
        .. "::"
        .. "org.example.KotestDescribeSpec"
        .. "::"
        .. "a namespace"
        .. "::"
        .. "should handle failed assertions",
      path = test_path,
      name = "should handle failed assertions",
      range = {
        7,
        0,
        9,
        0,
      },
      type = "test",
    }, tree[2][2][2][1])

    assert.are.same({
      id = test_path
        .. "::"
        .. "org.example.KotestDescribeSpec"
        .. "::"
        .. "a namespace"
        .. "::"
        .. "should handle passed assertions",
      path = test_path,
      name = "should handle passed assertions",
      range = {
        11,
        0,
        13,
        0,
      },
      type = "test",
    }, tree[2][2][3][1])

    assert.are.same({
      id = test_path
        .. "::"
        .. "org.example.KotestDescribeSpec"
        .. "::"
        .. "a namespace"
        .. "::"
        .. "a nested namespace",
      path = test_path,
      name = "a nested namespace",
      range = {
        19,
        0,
        31,
        0,
      },
      type = "namespace",
    }, tree[2][2][4][1])

    assert.are.same({
      id = test_path
        .. "::"
        .. "org.example.KotestDescribeSpec"
        .. "::"
        .. "a namespace"
        .. "::"
        .. "a nested namespace"
        .. "::"
        .. "should handle failed assertions",
      path = test_path,
      name = "should handle failed assertions",
      range = {
        20,
        0,
        22,
        0,
      },
      type = "test",
    }, tree[2][2][4][2][1])

    assert.are.same({
      id = test_path
        .. "::"
        .. "org.example.KotestDescribeSpec"
        .. "::"
        .. "a namespace"
        .. "::"
        .. "a nested namespace"
        .. "::"
        .. "should handle passed assertions",
      path = test_path,
      name = "should handle passed assertions",
      range = {
        24,
        0,
        26,
        0,
      },
      type = "test",
    }, tree[2][2][4][3][1])
  end)
end)

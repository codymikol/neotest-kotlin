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
  local shouldspec_file =
    vim.fs.joinpath(example_project_path, "KotestShouldSpec.kt")
  local describespec_file =
    vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")
  local stringspec_file =
    vim.fs.joinpath(example_project_path, "KotestStringSpec.kt")
  local expectspec_file =
    vim.fs.joinpath(example_project_path, "KotestExpectSpec.kt")
  local freespec_file =
    vim.fs.joinpath(example_project_path, "KotestFreeSpec.kt")
  local featurespec_file =
    vim.fs.joinpath(example_project_path, "KotestFeatureSpec.kt")
  local annotationspec_file =
    vim.fs.joinpath(example_project_path, "KotestAnnotationSpec.kt")
  local wordspec_file =
    vim.fs.joinpath(example_project_path, "KotestWordSpec.kt")

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

  describe("parse_positions", function()
    nio.tests.it("WordSpec", function()
      local tree = treesitter.parse_positions(wordspec_file):to_list()
      assert.equals("KotestWordSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local When_namespace = tree[2][1]
      assert.is_not_nil(When_namespace)
      assert.equals(wordspec_file .. "::When namespace when", When_namespace.id)
      assert.equals("namespace", When_namespace.type)
      assert.equals("When namespace", When_namespace.name)

      local When_nested_namespace = tree[2][2][1]
      assert.is_not_nil(When_nested_namespace)
      assert.equals(
        wordspec_file .. "::When namespace when::nested When namespace should",
        When_nested_namespace.id
      )
      assert.equals("namespace", When_nested_namespace.type)
      assert.equals("nested When namespace", When_nested_namespace.name)

      local When_nested_namespace_pass_test = tree[2][2][2][1]
      assert.is_not_nil(When_nested_namespace_pass_test)
      assert.equals("test", When_nested_namespace_pass_test.type)
      assert.equals("pass", When_nested_namespace_pass_test.name)

      local When_nested_namespace_fail_test = tree[2][2][3][1]
      assert.is_not_nil(When_nested_namespace_fail_test)
      assert.equals("test", When_nested_namespace_fail_test.type)
      assert.equals("fail", When_nested_namespace_fail_test.name)

      local when_namespace = tree[3][1]
      assert.is_not_nil(when_namespace)
      assert.equals(
        wordspec_file .. "::`when` namespace when",
        when_namespace.id
      )
      assert.equals("namespace", when_namespace.type)
      assert.equals("`when` namespace", when_namespace.name)

      local when_nested_namespace = tree[3][2][1]
      assert.is_not_nil(when_nested_namespace)
      assert.equals(
        wordspec_file
          .. "::`when` namespace when::nested `when` namespace should",
        when_nested_namespace.id
      )
      assert.equals("namespace", when_nested_namespace.type)
      assert.equals("nested `when` namespace", when_nested_namespace.name)

      local when_nested_namespace_pass_test = tree[3][2][2][1]
      assert.is_not_nil(when_nested_namespace_pass_test)
      assert.equals("test", when_nested_namespace_pass_test.type)
      assert.equals("pass", when_nested_namespace_pass_test.name)

      local when_nested_namespace_fail_test = tree[3][2][3][1]
      assert.is_not_nil(when_nested_namespace_fail_test)
      assert.equals("test", when_nested_namespace_fail_test.type)
      assert.equals("fail", when_nested_namespace_fail_test.name)

      local should_namespace = tree[4][1]
      assert.is_not_nil(should_namespace)
      assert.equals("namespace", should_namespace.type)
      assert.equals("namespace", should_namespace.name)

      local should_namespace_pass = tree[4][2][1]
      assert.is_not_nil(should_namespace_pass)
      assert.equals("test", should_namespace_pass.type)
      assert.equals("pass", should_namespace_pass.name)

      local should_namespace_fail = tree[4][3][1]
      assert.is_not_nil(should_namespace_fail)
      assert.equals("test", should_namespace_fail.type)
      assert.equals("fail", should_namespace_fail.name)
    end)

    nio.tests.it("StringSpec", function()
      local tree = treesitter.parse_positions(stringspec_file):to_list()
      assert.equals("KotestStringSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local test = tree[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = tree[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)
    end)

    nio.tests.it("AnnotationSpec", function()
      local tree = treesitter.parse_positions(annotationspec_file):to_list()
      assert.equals("KotestAnnotationSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local test = tree[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = tree[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)
    end)

    nio.tests.it("ExpectSpec", function()
      local tree = treesitter.parse_positions(expectspec_file):to_list()
      assert.equals("KotestExpectSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local content = tree[2]

      local namespace = content[1]
      assert.is_not_nil(namespace)
      assert.equals("namespace", namespace.name)
      assert.equals("namespace", namespace.type)

      local test = content[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = content[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)

      local namespace2 = content[4][1]
      assert.is_not_nil(namespace2)
      assert.equals("namespace", namespace2.type)
      assert.equals("nested namespace", namespace2.name)

      local test3 = content[4][2][1]
      assert.is_not_nil(test3)
      assert.equals("test", test3.type)
      assert.equals("pass", test3.name)

      local test4 = content[4][3][1]
      assert.is_not_nil(test4)
      assert.equals("test", test4.type)
      assert.equals("fail", test4.name)
    end)

    nio.tests.it("FreeSpec", function()
      local tree = treesitter.parse_positions(freespec_file):to_list()
      assert.equals("KotestFreeSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local content = tree[2]

      local namespace = content[1]
      assert.is_not_nil(namespace)
      assert.equals("namespace", namespace.name)
      assert.equals("namespace", namespace.type)

      local test = content[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = content[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)

      local namespace2 = content[4][1]
      assert.is_not_nil(namespace2)
      assert.equals("namespace", namespace2.type)
      assert.equals("nested namespace", namespace2.name)

      local test3 = content[4][2][1]
      assert.is_not_nil(test3)
      assert.equals("test", test3.type)
      assert.equals("pass", test3.name)

      local test4 = content[4][3][1]
      assert.is_not_nil(test4)
      assert.equals("test", test4.type)
      assert.equals("fail", test4.name)
    end)

    nio.tests.it("FeatureSpec", function()
      local tree = treesitter.parse_positions(featurespec_file):to_list()
      assert.equals("KotestFeatureSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local content = tree[2]

      local namespace = content[1]
      assert.is_not_nil(namespace)
      assert.equals("namespace", namespace.name)
      assert.equals("namespace", namespace.type)

      local test = content[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = content[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)

      local namespace2 = content[4][1]
      assert.is_not_nil(namespace2)
      assert.equals("namespace", namespace2.type)
      assert.equals("nested namespace", namespace2.name)

      local test3 = content[4][2][1]
      assert.is_not_nil(test3)
      assert.equals("test", test3.type)
      assert.equals("pass", test3.name)

      local test4 = content[4][3][1]
      assert.is_not_nil(test4)
      assert.equals("test", test4.type)
      assert.equals("fail", test4.name)
    end)

    nio.tests.it("FunSpec", function()
      local tree = treesitter.parse_positions(funspec_file):to_list()
      assert.equals("KotestFunSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local content = tree[2]

      local namespace = content[1]
      assert.is_not_nil(namespace)
      assert.equals("namespace", namespace.name)
      assert.equals("namespace", namespace.type)

      local test = content[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = content[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)

      local namespace2 = content[4][1]
      assert.is_not_nil(namespace2)
      assert.equals("namespace", namespace2.type)
      assert.equals("nested namespace", namespace2.name)

      local test3 = content[4][2][1]
      assert.is_not_nil(test3)
      assert.equals("test", test3.type)
      assert.equals("pass", test3.name)

      local test4 = content[4][3][1]
      assert.is_not_nil(test4)
      assert.equals("test", test4.type)
      assert.equals("fail", test4.name)
    end)

    nio.tests.it("ShouldSpec", function()
      local tree = treesitter.parse_positions(shouldspec_file):to_list()
      assert.equals("KotestShouldSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local content = tree[2]

      local namespace = content[1]
      assert.is_not_nil(namespace)
      assert.equals("namespace", namespace.name)
      assert.equals("namespace", namespace.type)

      local test = content[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("pass", test.name)

      local test2 = content[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("fail", test2.name)

      local namespace2 = content[4][1]
      assert.is_not_nil(namespace2)
      assert.equals("namespace", namespace2.type)
      assert.equals("nested namespace", namespace2.name)

      local test3 = content[4][2][1]
      assert.is_not_nil(test3)
      assert.equals("test", test3.type)
      assert.equals("pass", test3.name)

      local test4 = content[4][3][1]
      assert.is_not_nil(test4)
      assert.equals("test", test4.type)
      assert.equals("fail", test4.name)
    end)

    nio.tests.it("DescribeSpec", function()
      local tree = treesitter.parse_positions(describespec_file):to_list()
      assert.equals("KotestDescribeSpec.kt", tree[1].name)
      assert.equals("file", tree[1].type)

      local content = tree[2]
      assert.is_not_nil(content)
      assert.equals(4, #content)

      local namespace = content[1]
      assert.is_not_nil(namespace)
      assert.equals("namespace", namespace.type)
      assert.equals("a namespace", namespace.name)

      local test = content[2][1]
      assert.is_not_nil(test)
      assert.equals("test", test.type)
      assert.equals("should handle failed assertions", test.name)

      local test2 = content[3][1]
      assert.is_not_nil(test2)
      assert.equals("test", test2.type)
      assert.equals("should handle passed assertions", test2.name)

      local namespace2 = content[4][1]
      assert.is_not_nil(namespace2)
      assert.equals("namespace", namespace2.type)
      assert.equals("a nested namespace", namespace2.name)

      local test3 = content[4][2][1]
      assert.is_not_nil(test3)
      assert.equals("test", test3.type)
      assert.equals("should handle failed assertions", test3.name)

      local test4 = content[4][3][1]
      assert.is_not_nil(test4)
      assert.equals("test", test4.type)
      assert.equals("should handle passed assertions", test4.name)
    end)
  end)
end)

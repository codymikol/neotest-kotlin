local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")
local types = require("neotest.types")

describe("neotest-kotlin", function()
  it("name", function()
    assert.equals("neotest-kotlin", neotest_kotlin.name)
  end)

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

  describe("is_test_file", function()
    it("not .kt file", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "app",
        "build.gradle.kts"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    it("binary file", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "gradlew"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    it("TOML file", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "gradle",
        "libs.versions.toml"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)
    it("/src/main", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "app",
        "src",
        "main",
        "kotlin",
        "org",
        "example",
        "App.kt"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    describe("Kotest", function() end)
    nio.tests.it("FunSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("DescribeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("FreeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFreeSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("ExpectSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestExpectSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("BehaviorSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestBehaviorSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("WordSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestWordSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("AnnotationSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestAnnotationSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("StringSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestStringSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("ShouldSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestShouldSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)
  end)

  describe("build_spec", function()
    nio.tests.it("no tree", function()
      assert.are_nil(neotest_kotlin.build_spec({}))
    end)

    nio.tests.it("dir", function()
      ---@type types.Tree
      local tree = types.Tree.from_list(
        {
          {
            name = "dir",
            id = example_project_path,
            path = example_project_path,
            type = "dir",
            range = {
              0,
              0,
              0,
              0,
            },
          },
        },
        ---@param types.Tree
        ---@return string
        function(node)
          return node
        end
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(example_project_path, spec.context.path)

      assert.equals(
        spec.cwd,
        vim.fs.joinpath(
          debug.getinfo(1).source:match("@?(.*/)"),
          "example_project"
        )
      )

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts kotlinTestExecute %-Pclasses='org%.example' %-PoutputFile='.*%.json'$",
        spec.command
      )
    end)

    nio.tests.it("file", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type types.Tree
      local tree = types.Tree.from_list(
        {
          {
            name = test_path,
            id = test_path,
            path = test_path,
            type = "file",
            range = {
              0,
              0,
              0,
              0,
            },
          },
        },
        ---@param types.Tree
        ---@return string
        function(node)
          return node
        end
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(test_path, spec.context.path)

      assert.equals(
        spec.cwd,
        vim.fs.joinpath(
          debug.getinfo(1).source:match("@?(.*/)"),
          "example_project"
        )
      )

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec' %-PoutputFile='.*%.json'$",
        spec.command
      )
    end)

    nio.tests.it("namespace", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type types.Tree
      local tree = types.Tree.from_list(
        {
          {
            name = "namespace",
            id = test_path
              .. "::"
              .. "org.example.KotestFunSpec"
              .. "::"
              .. "namespace",
            path = test_path,
            type = "namespace",
            range = {
              6,
              4,
              24,
              4,
            },
          },
        },
        ---@param types.Tree
        ---@return string
        function(node)
          return node
        end
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(test_path, spec.context.path)

      assert.equals(
        spec.cwd,
        vim.fs.joinpath(
          debug.getinfo(1).source:match("@?(.*/)"),
          "example_project"
        )
      )

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec' %-PoutputFile='.*%.json' %-Pfilter='org%.example%.KotestFunSpec::namespace'$",
        spec.command
      )
    end)

    nio.tests.it("test", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type types.Tree
      local tree = types.Tree.from_list(
        {
          {
            name = "namespace",
            id = test_path
              .. "::"
              .. "org.example.KotestFunSpec"
              .. "::"
              .. "namespace"
              .. "::"
              .. "pass",
            path = test_path,
            type = "test",
            range = {
              7,
              8,
              9,
              8,
            },
          },
        },
        ---@param types.Tree
        ---@return string
        function(node)
          return node
        end
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(test_path, spec.context.path)

      assert.equals(
        spec.cwd,
        vim.fs.joinpath(
          debug.getinfo(1).source:match("@?(.*/)"),
          "example_project"
        )
      )

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec' %-PoutputFile='.*%.json' %-Pfilter='org%.example%.KotestFunSpec::namespace::pass'$",
        spec.command
      )
    end)
  end)

  describe("discover_positions", function()
    nio.tests.it("Custom Subclass", function()
      local test_path = vim.fs.joinpath(example_project_path, "SubclassSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "SubclassSpec.kt",
        range = {
          0,
          0,
          8,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.SubclassSpec",
        path = test_path,
        name = "SubclassSpec",
        range = {
          4,
          0,
          8,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.SubclassSpec"
          .. "::"
          .. "example",
        path = test_path,
        name = "example",
        range = {
          5,
          4,
          7,
          4,
        },
        type = "test",
      }, tree[2][2][1])
    end)

    nio.tests.it("Duplicate Test Names", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "DuplicateTestNames.kt")

      local bufnr = vim.fn.bufadd(test_path)

      neotest_kotlin.discover_positions(test_path):to_list()

      local count = vim.diagnostic.count(bufnr)
      assert.are.same({ [vim.diagnostic.severity.WARN] = 1 }, count)

      local diagnostics = vim.diagnostic.get(bufnr)
      assert.are.same({
        {
          bufnr = bufnr,
          col = 8,
          end_col = 8,
          end_lnum = 13,
          lnum = 11,
          message = "Multiple tests defined with name 'pass'",
          namespace = 2,
          severity = 2,
          source = "neotest-kotlin",
        },
      }, diagnostics)
    end)

    nio.tests.it("WordSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestWordSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestWordSpec.kt",
        range = {
          0,
          0,
          40,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestWordSpec",
        path = test_path,
        name = "KotestWordSpec",
        range = {
          5,
          0,
          40,
          4,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace",
        path = test_path,
        name = "When namespace",
        range = {
          7,
          8,
          17,
          8,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace"
          .. "::"
          .. "nested When namespace",
        path = test_path,
        name = "nested When namespace",
        range = {
          8,
          12,
          16,
          12,
        },
        type = "namespace",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace"
          .. "::"
          .. "nested When namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          9,
          16,
          11,
          16,
        },
        type = "test",
      }, tree[2][2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace"
          .. "::"
          .. "nested When namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          13,
          16,
          15,
          16,
        },
        type = "test",
      }, tree[2][2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace",
        path = test_path,
        name = "`when` namespace",
        range = {
          19,
          8,
          29,
          8,
        },
        type = "namespace",
      }, tree[2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace"
          .. "::"
          .. "nested `when` namespace",
        path = test_path,
        name = "nested `when` namespace",
        range = {
          20,
          12,
          28,
          12,
        },
        type = "namespace",
      }, tree[2][3][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace"
          .. "::"
          .. "nested `when` namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          21,
          16,
          23,
          16,
        },
        type = "test",
      }, tree[2][3][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace"
          .. "::"
          .. "nested `when` namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          25,
          16,
          27,
          16,
        },
        type = "test",
      }, tree[2][3][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          31,
          8,
          39,
          8,
        },
        type = "namespace",
      }, tree[2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          32,
          12,
          34,
          12,
        },
        type = "test",
      }, tree[2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          36,
          12,
          38,
          12,
        },
        type = "test",
      }, tree[2][4][3][1])
    end)

    nio.tests.it("StringSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestStringSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestStringSpec.kt",
        range = {
          0,
          0,
          12,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestStringSpec",
        path = test_path,
        name = "KotestStringSpec",
        range = {
          4,
          0,
          12,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestStringSpec"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          5,
          4,
          7,
          4,
        },
        type = "test",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestStringSpec"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          9,
          4,
          11,
          4,
        },
        type = "test",
      }, tree[2][3][1])
    end)

    nio.tests.it("AnnotationSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestAnnotationSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestAnnotationSpec.kt",
        range = {
          0,
          0,
          21,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestAnnotationSpec",
        path = test_path,
        name = "KotestAnnotationSpec",
        range = {
          5,
          0,
          21,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestAnnotationSpec"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          4,
          9,
          4,
        },
        type = "test",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestAnnotationSpec"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          12,
          4,
          14,
          4,
        },
        type = "test",
      }, tree[2][3][1])
    end)

    nio.tests.it("ShouldSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestShouldSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestShouldSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestShouldSpec",
        path = test_path,
        name = "KotestShouldSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("ExpectSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestExpectSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestExpectSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestExpectSpec",
        path = test_path,
        name = "KotestExpectSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("FeatureSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFeatureSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestFeatureSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestFeatureSpec",
        path = test_path,
        name = "KotestFeatureSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("FreeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFreeSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestFreeSpec.kt",
        range = {
          0,
          0,
          24,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestFreeSpec",
        path = test_path,
        name = "KotestFreeSpec",
        range = {
          4,
          0,
          24,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          5,
          4,
          23,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          6,
          8,
          8,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          10,
          8,
          12,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          14,
          8,
          22,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          15,
          12,
          17,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          19,
          12,
          21,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("FunSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestFunSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestFunSpec",
        path = test_path,
        name = "KotestFunSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("DescribeSpec", function()
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
          4,
          32,
          4,
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
          8,
          9,
          8,
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
          8,
          13,
          8,
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
          8,
          31,
          8,
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
          12,
          22,
          12,
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
          12,
          26,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)
  end)
end)

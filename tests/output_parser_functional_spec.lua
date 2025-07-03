local async = require("neotest.async")
local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")

describe("output_parser functional", function()
  local example_project_path =
    vim.fs.joinpath(debug.getinfo(1).source:match("@?(.*/)"), "example_project")
  local init_script_path =
    vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1]
  assert.is_not_nil(init_script_path)
  init_script_path = vim.fs.abspath(init_script_path)

  local tests_path = vim.fs.joinpath(
    example_project_path,
    "app",
    "src",
    "test",
    "kotlin",
    "org",
    "example"
  )

  local funspec_file = vim.fs.joinpath(tests_path, "KotestFunSpec.kt")
  local shouldspec_file = vim.fs.joinpath(tests_path, "KotestShouldSpec.kt")
  local describespec_file = vim.fs.joinpath(tests_path, "KotestDescribeSpec.kt")
  local stringspec_file = vim.fs.joinpath(tests_path, "KotestStringSpec.kt")
  local expectspec_file = vim.fs.joinpath(tests_path, "KotestExpectSpec.kt")
  local freespec_file = vim.fs.joinpath(tests_path, "KotestFreeSpec.kt")
  local featurespec_file = vim.fs.joinpath(tests_path, "KotestFeatureSpec.kt")
  local annotationspec_file =
    vim.fs.joinpath(tests_path, "KotestAnnotationSpec.kt")

  nio.tests.it("functional test", function()
    ---@type string
    local results_path = async.fn.tempname() .. ".txt"

    local cmd = string.format(
      "kotest_filter_specs='%s' kotest_filter_tests='%s' %s/gradlew -p %s -I %s test --console=plain | tee -a %s",
      "org.example.*",
      "*",
      example_project_path,
      example_project_path,
      init_script_path,
      results_path
    )

    local success, _, _ = os.execute(cmd)
    assert.is_true(success == 0)

    local spec = {
      context = {
        results_path = results_path,
        path = tests_path,
      },
    }

    local results = neotest_kotlin.results(spec, {}, {})
    local ids = vim.tbl_keys(results)
    assert.equals(31, #ids)

    -- KotestDescribeSpec.kt
    assert.equals(6, #vim.tbl_filter(function(value)
      return vim.startswith(value, describespec_file)
    end, ids))

    assert.equals(
      "passed",
      results[describespec_file .. "::a namespace::should handle passed assertions"].status
    )
    assert.equals(
      "failed",
      results[describespec_file .. "::a namespace::should handle failed assertions"].status
    )
    assert.equals(
      "skipped",
      results[describespec_file .. "::a namespace::should handle skipped assertions"].status
    )
    assert.equals(
      "passed",
      results[describespec_file .. "::a namespace::a nested namespace::should handle passed assertions"].status
    )
    assert.equals(
      "failed",
      results[describespec_file .. "::a namespace::a nested namespace::should handle failed assertions"].status
    )
    assert.equals(
      "skipped",
      results[describespec_file .. "::a namespace::a nested namespace::should handle skipped assertions"].status
    )

    -- KotestFunSpec.kt
    assert.equals(4, #vim.tbl_filter(function(value)
      return vim.startswith(value, funspec_file)
    end, ids))

    assert.equals("passed", results[funspec_file .. "::namespace::pass"].status)
    assert.equals("failed", results[funspec_file .. "::namespace::fail"].status)
    assert.equals(
      "passed",
      results[funspec_file .. "::namespace::nested namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[funspec_file .. "::namespace::nested namespace::fail"].status
    )

    -- KotestFreeSpec.kt
    assert.equals(4, #vim.tbl_filter(function(value)
      return vim.startswith(value, freespec_file)
    end, ids))

    assert.equals(
      "passed",
      results[freespec_file .. "::namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[freespec_file .. "::namespace::fail"].status
    )
    assert.equals(
      "passed",
      results[freespec_file .. "::namespace::nested namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[freespec_file .. "::namespace::nested namespace::fail"].status
    )

    -- KotestFeatureSpec.kt
    assert.equals(4, #vim.tbl_filter(function(value)
      return vim.startswith(value, featurespec_file)
    end, ids))

    assert.equals(
      "passed",
      results[featurespec_file .. "::namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[featurespec_file .. "::namespace::fail"].status
    )
    assert.equals(
      "passed",
      results[featurespec_file .. "::namespace::nested namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[featurespec_file .. "::namespace::nested namespace::fail"].status
    )

    -- KotestExpectSpec.kt
    assert.equals(4, #vim.tbl_filter(function(value)
      return vim.startswith(value, expectspec_file)
    end, ids))

    assert.equals(
      "passed",
      results[expectspec_file .. "::namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[expectspec_file .. "::namespace::fail"].status
    )
    assert.equals(
      "passed",
      results[expectspec_file .. "::namespace::nested namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[expectspec_file .. "::namespace::nested namespace::fail"].status
    )

    -- KotestAnnotationSpec.kt
    assert.equals(3, #vim.tbl_filter(function(value)
      return vim.startswith(value, annotationspec_file)
    end, ids))

    assert.equals("passed", results[annotationspec_file .. "::pass"].status)
    assert.equals("failed", results[annotationspec_file .. "::fail"].status)
    assert.equals("skipped", results[annotationspec_file .. "::ignore"].status)

    -- KotestStringSpec.kt
    assert.equals(2, #vim.tbl_filter(function(value)
      return vim.startswith(value, stringspec_file)
    end, ids))

    assert.equals("passed", results[stringspec_file .. "::pass"].status)
    assert.equals("failed", results[stringspec_file .. "::fail"].status)

    -- KotestShouldSpec.kt
    assert.equals(4, #vim.tbl_filter(function(value)
      return vim.startswith(value, shouldspec_file)
    end, ids))

    assert.equals(
      "passed",
      results[shouldspec_file .. "::namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[shouldspec_file .. "::namespace::fail"].status
    )
    assert.equals(
      "passed",
      results[shouldspec_file .. "::namespace::nested namespace::pass"].status
    )
    assert.equals(
      "failed",
      results[shouldspec_file .. "::namespace::nested namespace::fail"].status
    )
  end)
end)

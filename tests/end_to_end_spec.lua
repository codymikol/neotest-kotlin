local lib = require("neotest.lib")
local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")

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

describe("neotest-kotlin", function()
  nio.tests.it("Basic", function()
    local test_path = vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

    local tree = neotest_kotlin.discover_positions(test_path)
    assert.not_nil(tree)

    local spec = neotest_kotlin.build_spec({ tree = tree })
    assert.not_nil(spec)
    -- remove warnings for spec not being nil
    assert(spec ~= nil)

    assert.not_nil(spec.cwd)
    assert.not_nil(spec.command)

    -- remove single quotes for usage in nio.process.run
    local command = spec.command:gsub("'", "")

    ---@type string[]
    local args = {}
    for arg in command:gmatch("%S+") do
      table.insert(args, arg)
    end

    local run_args = {
      cwd = spec.cwd,
      cmd = args[1],
      args = vim.list_slice(args, 2, #args),
    }

    vim.print("Fork Args:", run_args)

    local process = nio.process.run(run_args)

    assert.not_nil(process)

    ---@type integer
    local status_code = process.result(true)
    assert.equals(0, status_code)

    local results_path = spec.context.results_path
    vim.print("Test Results Path: " .. results_path)
    assert.not_nil(lib.files.exists(results_path))

    local results = neotest_kotlin.results(spec, nil, nil)

    assert.not_nil(
      results[test_path .. "::org.example.KotestFunSpec::namespace::fail"].output
    )

    -- set to nil for full assertion below
    results[test_path .. "::org.example.KotestFunSpec::namespace::fail"].output =
      nil

    assert.not_nil(
      results[test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::fail"].output
    )

    -- set to nil for full assertion below
    results[test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::fail"].output =
      nil

    assert.are.same({
      [test_path .. "::org.example.KotestFunSpec::namespace::fail"] = {
        status = "failed",
        short = "expected:<b> but was:<a>",
        errors = {
          {
            message = "expected:<b> but was:<a>",
            line = 12,
          },
        },
      },
      [test_path .. "::org.example.KotestFunSpec::namespace::pass"] = {
        status = "passed",
      },
      [test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::pass"] = {
        status = "passed",
      },
      [test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::fail"] = {
        status = "failed",
        short = "expected:<b> but was:<a>",
        errors = {
          {
            message = "expected:<b> but was:<a>",
            line = 21,
          },
        },
      },
    }, results)
  end)

  nio.tests.it("Spring", function()
    local test_path = vim.fs.joinpath(example_project_path, "SpringFunSpec.kt")

    local tree = neotest_kotlin.discover_positions(test_path)
    assert.not_nil(tree)

    local spec = neotest_kotlin.build_spec({ tree = tree })
    assert.not_nil(spec)
    -- remove warnings for spec not being nil
    assert(spec ~= nil)

    assert.not_nil(spec.cwd)
    assert.not_nil(spec.command)

    -- remove single quotes for usage in nio.process.run
    local command = spec.command:gsub("'", "")

    ---@type string[]
    local args = {}
    for arg in command:gmatch("%S+") do
      table.insert(args, arg)
    end

    local run_args = {
      cwd = spec.cwd,
      cmd = args[1],
      args = vim.list_slice(args, 2, #args),
    }

    vim.print("Fork Args:", run_args)

    local process = nio.process.run(run_args)

    assert.not_nil(process)

    ---@type integer
    local status_code = process.result(true)
    assert.equals(0, status_code)

    local results_path = spec.context.results_path
    vim.print("Test Results Path: " .. results_path)
    assert.not_nil(lib.files.exists(results_path))

    vim.print(lib.files.read(results_path))

    local results = neotest_kotlin.results(spec, nil, nil)

    assert.are.same({
      [test_path .. "::org.example.SpringFunSpec::GET /actuator/health"] = {
        status = "passed",
      },
    }, results)
  end)
end)

local lib = require("neotest.lib")
local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")
local types = require("neotest.types")

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

---Runs the command of a neotest.RunSpec, asserting it succeeds
---@param spec neotest.RunSpec
local function run(spec)
  -- remove single quotes for usage in nio.process.run
  local command = spec.command:gsub("'", "")

  ---@type string[]
  local args = {}
  for arg in command:gmatch("%S+") do
    table.insert(args, arg)
  end

  local process = nio.process.run({
    cwd = spec.cwd,
    cmd = args[1],
    args = vim.list_slice(args, 2, #args),
  })
  assert.not_nil(process)

  ---@type integer
  local status_code = process.result(true)
  assert.equals(0, status_code)
  assert.is_true(lib.files.exists(spec.context.results_path))
end

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

    local results = neotest_kotlin.results(spec, nil, tree)

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

    local results = neotest_kotlin.results(spec, nil, tree)

    assert.are.same({
      [test_path .. "::org.example.SpringFunSpec::GET /actuator/health"] = {
        status = "passed",
      },
    }, results)
  end)

  nio.tests.it("beforeSpec error", function()
    local test_path =
      vim.fs.joinpath(example_project_path, "KotestBeforeSpecErrorSpec.kt")

    local tree = neotest_kotlin.discover_positions(test_path)
    assert.not_nil(tree)

    local spec = neotest_kotlin.build_spec({ tree = tree })
    assert(spec ~= nil)

    run(spec)

    local results = neotest_kotlin.results(spec, nil, tree)

    local class_id = test_path .. "::org.example.KotestBeforeSpecErrorSpec"
    local test_id = class_id .. "::namespace::pass"

    assert.not_nil(results[class_id].output)
    assert.are.equal(results[class_id].output, results[test_id].output)
    results[class_id].output = nil
    results[test_id].output = nil

    assert.are.same({
      [class_id] = {
        status = "failed",
        short = "java.lang.IllegalStateException: beforeSpec failed",
        errors = {
          {
            message = "java.lang.IllegalStateException: beforeSpec failed",
            line = 7,
          },
        },
      },
      [test_id] = {
        status = "failed",
        short = "java.lang.IllegalStateException: beforeSpec failed",
        errors = {},
      },
    }, results)
  end)

  nio.tests.it("File with multiple classes", function()
    local test_path =
      vim.fs.joinpath(example_project_path, "MultipleClassesSpec.kt")

    local tree = neotest_kotlin.discover_positions(test_path)
    assert.not_nil(tree)

    local spec = neotest_kotlin.build_spec({ tree = tree })
    assert.not_nil(spec)
    assert(spec ~= nil)

    run(spec)

    local results = neotest_kotlin.results(spec, nil, tree)

    assert.are.same({
      [test_path .. "::org.example.MultipleClassesFirstSpec::pass"] = {
        status = "passed",
      },
      [test_path .. "::org.example.MultipleClassesSecondSpec::pass"] = {
        status = "passed",
      },
    }, results)
  end)

  nio.tests.it("Directory with mixed packages", function()
    local mixed_path = vim.fs.joinpath(example_project_path, "mixed")
    local mixed_spec = vim.fs.joinpath(mixed_path, "MixedPackageSpec.kt")
    local other_spec = vim.fs.joinpath(mixed_path, "OtherPackageSpec.kt")

    local mixed_tree = neotest_kotlin.discover_positions(mixed_spec)
    local other_tree = neotest_kotlin.discover_positions(other_spec)
    assert(mixed_tree ~= nil and other_tree ~= nil)

    local tree = types.Tree.from_list({
      {
        id = mixed_path,
        name = "mixed",
        path = mixed_path,
        type = "dir",
      },
      mixed_tree:to_list(),
      other_tree:to_list(),
    }, function(pos)
      return pos.id
    end)

    local spec = neotest_kotlin.build_spec({ tree = tree })
    assert.not_nil(spec)
    assert(spec ~= nil)

    run(spec)

    local results = neotest_kotlin.results(spec, nil, tree)

    assert.are.same({
      [mixed_spec .. "::org.example.mixed.MixedPackageSpec::pass"] = {
        status = "passed",
      },
      [other_spec .. "::org.other.OtherPackageSpec::pass"] = {
        status = "passed",
      },
    }, results)
  end)

  ---Runs the RunSpec built for the tree and returns its results
  ---@param tree neotest.Tree
  ---@return neotest.RunSpec, table<string, neotest.Result>
  local function run_tree(tree)
    local spec = neotest_kotlin.build_spec({ tree = tree })
    assert.not_nil(spec)
    assert(spec ~= nil)

    run(spec)

    local results = neotest_kotlin.results(spec, nil, tree)

    for _, result in pairs(results) do
      -- output is a temporary file, remove for full assertions
      result.output = nil
    end

    return spec, results
  end

  nio.tests.it("Duplicate Test Names", function()
    local test_path =
      vim.fs.joinpath(example_project_path, "DuplicateTestNames.kt")
    local class_id = test_path .. "::org.example.DuplicateTestNames"

    local tree = neotest_kotlin.discover_positions(test_path)
    assert.not_nil(tree)
    assert(tree ~= nil)

    local failure = {
      status = "failed",
      short = "expected:<b> but was:<a>",
      errors = {
        {
          message = "expected:<b> but was:<a>",
          line = 12,
        },
      },
    }

    -- run a single duplicate by its disambiguated id
    local second = nil
    for _, position in tree:iter() do
      if position.id == class_id .. "::pass#2" then
        second = position
      end
    end
    assert.not_nil(second)

    local spec, results =
      run_tree(types.Tree.from_list({ second }, function(node)
        return node.id
      end))
    assert.matches(
      "%-Pfilter='org%.example%.DuplicateTestNames::pass#2'$",
      spec.command
    )

    assert.are.same({
      [class_id .. "::pass#1"] = {
        status = "skipped",
      },
      [class_id .. "::pass#2"] = failure,
    }, results)

    -- run the whole file
    local _, file_results = run_tree(tree)

    assert.are.same({
      [class_id .. "::pass#1"] = {
        status = "passed",
      },
      [class_id .. "::pass#2"] = failure,
    }, file_results)
  end)
end)

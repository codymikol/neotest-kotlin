local TestNode = require("neotest-kotlin.output.test_node")

---Equivalent to assert.are.same, but only matches fields specified in expected.
---@param expected table
---@param actual table
---@param path? string
function assert.equals_specified(expected, actual, path)
  path = path or ""
  for k, v in pairs(expected) do
    local key_path = path .. "." .. tostring(k)
    if type(v) == "table" then
      assert.is_table(actual[k], "Expected table at: " .. key_path)
      assert.equals_specified(v, actual[k], key_path)
    else
      assert.are.equal(v, actual[k], "Mismatch at: " .. key_path)
    end
  end
end

describe("TestNode", function()
  describe("decode", function()
    it("container", function()
      local input = [[
    {
      "name": "namespace",
      "type": "CONTAINER",
      "status": {
        "status": "FAILURE"
      },
      "tests": [
        {
          "name": "passed",
          "type": "TEST",
          "status": {
            "status": "SUCCESS"
          }
        },
        {
          "name": "skipped",
          "type": "TEST",
          "status": {
            "status": "IGNORED",
            "reason": "reason for being ignored"
          }
        },
        {
          "name": "failure",
          "type": "TEST",
          "status": {
            "status": "FAILURE",
            "stackTrace": "example",
            "error": {
              "filename": "/example/path/to/file.kt",
              "lineNumber": 5,
              "message": "example"
            }
          }
        }
      ]
    }
    ]]

      local node = vim.json.decode(input)
      local test_node = TestNode.from(node)

      assert.equals(3, #test_node.tests)
      assert.equals_specified({
        name = "namespace",
        type = "CONTAINER",
        status = { status = "FAILURE" },
      }, test_node)
    end)

    it("passed", function()
      local input = [[
    {
      "name": "passed",
      "type": "TEST",
      "status": {
        "status": "SUCCESS"
      }
    }
    ]]

      local node = vim.json.decode(input)
      local test_node = TestNode.from(node)

      assert.is_nil(test_node.tests)
      assert.equals_specified({
        name = "passed",
        type = "TEST",
        tests = nil,
        status = { status = "SUCCESS" },
      }, test_node)
    end)

    it("skipped", function()
      local input = [[
    {
      "name": "skipped",
      "type": "TEST",
      "status": {
        "status": "IGNORED",
        "reason": "reason for being ignored"
      }
    }
    ]]

      local node = vim.json.decode(input)
      local test_node = TestNode.from(node)

      assert.is_nil(test_node.tests)
      assert.equals_specified({
        name = "skipped",
        type = "TEST",
        tests = nil,
        status = { status = "IGNORED", reason = "reason for being ignored" },
      }, test_node)
    end)

    it("failure", function()
      local input = [[
    {
      "name": "failure",
      "type": "TEST",
      "status": {
        "status": "FAILURE",
        "stackTrace": "example",
        "error": {
          "filename": "/example/path/to/file.kt",
          "lineNumber": 5,
          "message": "example"
        }
      }
    }
    ]]

      local node = vim.json.decode(input)
      local test_node = TestNode.from(node)

      assert.equals_specified({
        name = "failure",
        type = "TEST",
        tests = nil,
        status = {
          status = "FAILURE",
          stackTrace = "example",
          error = {
            filename = "/example/path/to/file.kt",
            lineNumber = 5,
            message = "example",
          },
        },
      }, test_node)
    end)
  end)

  describe("to_status", function()
    it("passed", function()
      local node = TestNode.newTest("name", { status = "SUCCESS" })
      assert.equals("passed", node:to_status())
    end)

    it("failed", function()
      local node = TestNode.newTest("name", { status = "FAILURE" })
      assert.equals("failed", node:to_status())
    end)

    it("skipped", function()
      local node = TestNode.newTest("name", { status = "IGNORED" })
      assert.equals("skipped", node:to_status())
    end)
  end)

  describe("to_results", function()
    local path = "/example/path/to/file.kt"

    it("single top-level test", function()
      local node = TestNode.newContainer(
        "org.example.File",
        { status = "SUCCESS" },
        { TestNode.newTest("passed", { status = "SUCCESS" }) }
      )

      local results = node:to_results(path)
      assert.equals_specified({
        ["/example/path/to/file.kt::passed"] = { status = "passed" },
      }, results)
    end)

    it("multiple top-level test", function()
      local node = TestNode.newContainer(
        "org.example.File",
        { status = "FAILURE" },
        {
          TestNode.newTest("passed", { status = "SUCCESS" }),
          TestNode.newTest("skipped", { status = "IGNORED" }),
          TestNode.newTest("failed", {
            status = "FAILURE",
            stackTrace = "example",
            error = { lineNumber = 5, filename = path, message = "example" },
          }),
        }
      )

      local results = node:to_results(path)
      assert.equals_specified({
        ["/example/path/to/file.kt::passed"] = { status = "passed" },
        ["/example/path/to/file.kt::skipped"] = { status = "skipped" },
        ["/example/path/to/file.kt::failed"] = {
          status = "failed",
          short = "example",
          errors = { { line = 4, message = "example" } },
        },
      }, results)
    end)

    it("multiple nested tests", function()
      local node = TestNode.newContainer(
        "org.example.File",
        { status = "FAILURE" },
        {
          TestNode.newContainer("namespace", { status = "FAILURE" }, {
            TestNode.newTest("passed", { status = "SUCCESS" }),
            TestNode.newTest("skipped", { status = "IGNORED" }),
            TestNode.newTest("failed", {
              status = "FAILURE",
              stackTrace = "example",
              error = { lineNumber = 5, filename = path, message = "example" },
            }),
          }),
        }
      )

      local results = node:to_results(path)
      assert.equals_specified({
        ["/example/path/to/file.kt::namespace::passed"] = { status = "passed" },
        ["/example/path/to/file.kt::namespace::skipped"] = {
          status = "skipped",
        },
        ["/example/path/to/file.kt::namespace::failed"] = {
          status = "failed",
          short = "example",
          errors = { { line = 4, message = "example" } },
        },
      }, results)
    end)

    it("deeply nested test", function()
      local node = TestNode.newContainer(
        "org.example.File",
        { status = "SUCCESS" },
        {
          TestNode.newContainer("namespace", { status = "SUCCESS" }, {
            TestNode.newContainer("nested namespace", { status = "SUCCESS" }, {
              TestNode.newContainer(
                "nested nested namespace",
                { status = "SUCCESS" },
                {
                  TestNode.newTest("passed", { status = "SUCCESS" }),
                }
              ),
            }),
          }),
        }
      )

      local results = node:to_results(path)
      assert.are.same({
        ["/example/path/to/file.kt::namespace::nested namespace::nested nested namespace::passed"] = {
          status = "passed",
        },
      }, results)
    end)

    it("single container", function()
      local node = TestNode.newContainer(
        "org.example.File",
        { status = "SUCCESS" },
        {}
      )

      local results = node:to_results(path)
      assert.are.same({}, results)
    end)

    it("nested containers", function()
      local node = TestNode.newContainer(
        "org.example.File",
        { status = "SUCCESS" },
        {
          TestNode.newContainer("namespace", { status = "SUCCESS" }, {}),
        }
      )

      local results = node:to_results(path)
      assert.are.same({}, results)
    end)
  end)

  describe("to_result", function()
    it("passed", function()
      local node = TestNode.newTest("pass", { status = "SUCCESS" })
      local id, result = node:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::pass", id)
      assert.are.same({ status = "passed" }, result)
    end)

    it("passed - nested", function()
      local node = TestNode.newTest("pass", { status = "SUCCESS" })
      local id, result =
        node:to_result("/example/path/to/file.kt::namespace::nested namespace")

      assert.equals(
        "/example/path/to/file.kt::namespace::nested namespace::pass",
        id
      )
      assert.are.same({ status = "passed" }, result)
    end)

    it("skipped", function()
      local node = TestNode.newTest("skipped", { status = "IGNORED" })
      local id, result = node:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::skipped", id)
      assert.are.same({ status = "skipped" }, result)
    end)

    it("failed", function()
      local node = TestNode.newTest("failed", {
        status = "FAILURE",
        stackTrace = "example\nstacktrace\nhere",
        error = {
          filename = "/example/path/to/file.kt}",
          lineNumber = 5,
          message = "example",
        },
      })

      local id, result = node:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::failed", id)

      assert.not_nil(result.output)
      assert.equals("failed", result.status)
      assert.equals("example", result.short)
      assert.are.same({ { line = 4, message = "example" } }, result.errors)
    end)
  end)
end)

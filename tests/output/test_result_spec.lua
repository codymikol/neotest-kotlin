local TestResult = require("neotest-kotlin.output.test_result")

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

describe("TestResult", function()
  describe("decode", function()
    it("passed", function()
      local input = [[
      {
        "className": "org.example.TestExample",
        "id": "namespace::test",
        "status": {
          "type": "SUCCESS"
        }
      }
    ]]

      local node = vim.json.decode(input)
      local test_result = TestResult.from(node)

      assert.equals_specified({
        className = "org.example.TestExample",
        id = "namespace::test",
        status = { type = "SUCCESS" },
      }, test_result)
    end)

    it("skipped", function()
      local input = [[
      {
        "className": "org.example.TestExample",
        "id": "namespace::test",
        "status": {
          "type": "IGNORED",
          "reason": "ignored"
        }
      }
    ]]

      local node = vim.json.decode(input)
      local test_result = TestResult.from(node)

      assert.equals_specified({
        className = "org.example.TestExample",
        id = "namespace::test",
        status = {
          type = "IGNORED",
          reason = "ignored",
        },
      }, test_result)
    end)

    it("failure", function()
      local input = [[
      {
        "className": "org.example.TestExample",
        "id": "namespace::test",
        "status": {
          "type": "FAILURE",
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
      local test_result = TestResult.from(node)

      assert.equals_specified({
        className = "org.example.TestExample",
        id = "namespace::test",
        status = {
          type = "FAILURE",
          stackTrace = "example",
          error = {
            filename = "/example/path/to/file.kt",
            lineNumber = 5,
            message = "example",
          },
        },
      }, test_result)
    end)
  end)

  describe("to_status", function()
    it("passed", function()
      local result = TestResult.new(
        "namespace::test",
        "org.example.TestExample",
        { type = "SUCCESS" }
      )

      assert.equals("passed", result:to_status())
    end)

    it("failed", function()
      local result = TestResult.new(
        "namespace::test",
        "org.example.TestExample",
        { type = "FAILURE" }
      )

      assert.equals("failed", result:to_status())
    end)

    it("skipped", function()
      local result = TestResult.new(
        "namespace::test",
        "org.example.TestExample",
        { type = "IGNORED" }
      )

      assert.equals("skipped", result:to_status())
    end)
  end)

  describe("to_result", function()
    it("passed", function()
      local test_result = TestResult.new(
        "namespace::test",
        "org.example.TestExample",
        { type = "SUCCESS" }
      )

      local id, result = test_result:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::namespace::test", id)
      assert.are.same({ status = "passed" }, result)
    end)

    it("passed - nested", function()
      local test_result = TestResult.new(
        "namespace::nested::test",
        "org.example.TestExample",
        { type = "SUCCESS" }
      )

      local id, result = test_result:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::namespace::nested::test", id)
      assert.are.same({ status = "passed" }, result)
    end)

    it("skipped", function()
      local test_result =
        TestResult.new("test", "org.example.TestExample", { type = "IGNORED" })
      local id, result = test_result:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::test", id)
      assert.are.same({ status = "skipped" }, result)
    end)

    it("failed", function()
      local test_result = TestResult.new("test", "org.example.TestExample", {
        type = "FAILURE",
        stackTrace = "example\nstacktrace\nhere",
        error = {
          filename = "/example/path/to/file.kt}",
          lineNumber = 5,
          message = "example",
        },
      })

      local id, result = test_result:to_result("/example/path/to/file.kt")

      assert.equals("/example/path/to/file.kt::test", id)

      assert.not_nil(result.output)
      assert.equals("failed", result.status)
      assert.equals("example", result.short)
      assert.are.same({ { line = 4, message = "example" } }, result.errors)
    end)
  end)
end)

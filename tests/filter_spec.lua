local filter = require("neotest-kotlin.filter")

describe("filter", function()
  describe("is_test_file", function()
    it("valid", function()
      assert.is_true(filter.is_test_file("example.kt"))
    end)

    it("invalid - nil", function()
      assert.is_false(filter.is_test_file(nil))
    end)

    it("invalid - kts", function()
      assert.is_false(filter.is_test_file("example.kts"))
    end)

    it("invalid - src/main", function()
      assert.is_false(filter.is_test_file("/src/main/example.kt"))
    end)
  end)

  describe("is_test_directory", function()
    it("target", function()
      assert.is_false(filter.is_test_directory("target"))
    end)

    it("build", function()
      assert.is_false(filter.is_test_directory("build"))
    end)

    it("generated", function()
      assert.is_false(filter.is_test_directory("generated"))
    end)

    it("docs", function()
      assert.is_false(filter.is_test_directory("docs"))
    end)

    it("main", function()
      assert.is_false(filter.is_test_directory("main"))
    end)

    it(".gradle", function()
      assert.is_false(filter.is_test_directory(".gradle"))
    end)

    it("buildSrc", function()
      assert.is_false(filter.is_test_directory("buildSrc"))
    end)

    it("out", function()
      assert.is_false(filter.is_test_directory("out"))
    end)

    it("kapt", function()
      assert.is_false(filter.is_test_directory("kapt"))
    end)

    it("test", function()
      assert.is_true(filter.is_test_directory("test"))
    end)

    it("kotlin", function()
      assert.is_true(filter.is_test_directory("kotlin"))
    end)
  end)
end)

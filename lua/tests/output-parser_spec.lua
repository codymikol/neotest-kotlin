-- Write a test that parses a gradle output line and tests if it is a result line.
--

describe("output-parser", function()
  local output_parser = require("src.output-parser")

  local pkg_example = "com.codymikol.state.neotest.NeotestKotestSpec"

  local passed_example =
  "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle passed assertions PASSED"

  local skipped_example =
  "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle skipped assertions SKIPPED"

  local failed_example =
  "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle failed assertions FAILED"

  describe("get_result_type", function()
    it("should return the correct result for a non-result line", function()
      assert.is_equal(nil, output_parser.get_result_type("This is not a result line", pkg_example))
    end)

    it("should return the correct result for a PASSED result line", function()
      assert.is_equal("passed", output_parser.get_result_type(passed_example, pkg_example))
    end)

    it("should return the correct result for a SKIPPED result line", function()
      assert.is_equal("skipped", output_parser.get_result_type(skipped_example, pkg_example))
    end)

    it("should return the correct result for a FAILED result line", function()
      assert.is_equal("failed", output_parser.get_result_type(failed_example, pkg_example))
    end)
  end)

  describe("get_result_id", function()
    it("should transform a result string into a valid id for PASSED specs", function()
      local result_id = output_parser.make_result_id(
        passed_example,
        "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"
      )
      assert.is_equal(
        '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle passed assertions"',
        result_id
      )
    end)
    it("should transform a result string into a valid id for FAILED specs", function()
      local result_id = output_parser.make_result_id(
        failed_example,
        "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"
      )
      assert.is_equal(
        '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle failed assertions"',
        result_id
      )
    end)
    it("should transform a result string into a valid id for SKIPPED specs", function()
      local result_id = output_parser.make_result_id(
        skipped_example,
        "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"
      )
      assert.is_equal(
        '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle skipped assertions"',
        result_id
      )
    end)
  end)

  -- We now know that YES, more than one line CAN be passed by the streaming function and is.
  --
  -- line{ "2024-07-08T19:48:21.734-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized native services in: /home/cmikol/.gradle/native", "2024-07-08T19:48:21.753-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized jansi services in: /home/cmikol/.gradle/native" }

  describe("line_to_result", function()
    describe("Any message that is NOT a [TestEvent]", function()
      local line =
      "2024-07-08T19:48:21.734-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized native services in: /home/cmikol/.gradle/native"

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"

      local result = output_parser.line_to_result(line, path, pkg_example)

      it("should return an empty table", function()
        assert.are.same({}, result)
      end)
    end)

    describe("Any messages that does NOT end with PASSED, SKIPPED, or FAILED", function()
      local line =
      "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] "

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"

      local result = output_parser.line_to_result(line, path, pkg_example)

      it("should return an empty table", function()
        assert.are.same({}, result)
      end)
    end)

    describe("Any message that is a valid TestLoggingEvent ending with PASSED", function()
      local line =
      "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle passed assertions PASSED"

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"

      local result_entry = output_parser.line_to_result(line, path, pkg_example)

      local expected_id =
      '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle passed assertions"'

      it("should not be an empty table", function()
        assert.is_not.same({}, result)
      end)

      it("should contain an entry with the expected id as a key", function()
        assert.is_not.same(nil, result_entry)
      end)

      it("should contain the correct id", function()
        assert.is_not.same(nil, result_entry.id)
        assert.are.same(result_entry.id, expected_id)
      end)

      it("should contain the correct short description", function()
        assert.is_not.same(nil, result_entry.short)
        assert.are.same(result_entry.short, "should handle passed assertions")
      end)

      it("should contain the correct status", function()
        assert.is_not.same(nil, result_entry.status)
        assert.are.same(result_entry.status, "passed")
      end)
    end)


    describe("Any message that is a valid TestLoggingEvent ending with FAILED", function()
      local line =
      "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle passed assertions FAILED"

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"

      local result_entry = output_parser.line_to_result(line, path, pkg_example)

      local expected_id =
      '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle passed assertions"'

      it("should not be an empty table", function()
        assert.is_not.same({}, result)
      end)

      it("should contain an entry with the expected id as a key", function()
        assert.is_not.same(nil, result_entry)
      end)

      it("should contain the correct id", function()
        assert.is_not.same(nil, result_entry.id)
        assert.are.same(result_entry.id, expected_id)
      end)

      it("should contain the correct short description", function()
        assert.is_not.same(nil, result_entry.short)
        assert.are.same(result_entry.short, "should handle passed assertions")
      end)

      it("should contain the correct status", function()
        assert.is_not.same(nil, result_entry.status)
        assert.are.same(result_entry.status, "failed")
      end)
    end)

    describe("Any message that is a valid TestLoggingEvent ending with SKIPPED", function()
      local line =
      "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle passed assertions SKIPPED"

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"

      local result_entry = output_parser.line_to_result(line, path, pkg_example)

      local expected_id =
      '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle passed assertions"'

      it("should not be an empty table", function()
        assert.is_not.same({}, result)
      end)

      it("should contain an entry with the expected id as a key", function()
        assert.is_not.same(nil, result_entry)
      end)

      it("should contain the correct id", function()
        assert.is_not.same(nil, result_entry.id)
        assert.are.same(result_entry.id, expected_id)
      end)

      it("should contain the correct short description", function()
        assert.is_not.same(nil, result_entry.short)
        assert.are.same(result_entry.short, "should handle passed assertions")
      end)

      it("should contain the correct status", function()
        assert.is_not.same(nil, result_entry.status)
        assert.are.same(result_entry.status, "skipped")
      end)
    end)
  end)

  describe("parse_lines", function()
    describe("when the table contains no valid test lines", function()
      local lines = {
        "2024-07-09T08:46:31.200-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized native services in: /home/cmikol/.gradle/native",
        "2024-07-09T08:46:31.218-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized jansi services in: /home/cmikol/.gradle/native"
      }

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"
      local result = output_parser.lines_to_results(lines, path, pkg_example)

      it("should return an empty table", function()
        assert.are.same({}, result)
      end)
    end)

    describe("when the table contains one valid test line", function()
      local lines = {
        "2024-07-09T08:46:31.200-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized native services in: /home/cmikol/.gradle/native",
        "2024-07-09T08:46:31.218-0400 [INFO] [org.gradle.internal.nativeintegration.services.NativeServices] Initialized jansi services in: /home/cmikol/.gradle/native",
        "2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle passed assertions PASSED"
      }

      local path = "/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt"
      local result = output_parser.lines_to_results(lines, path, pkg_example)
      local expected_id =
      '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"a namespace"::"should handle passed assertions"'

      it("should contain the correct result id", function()
        assert.are.same(expected_id, result[expected_id].id)
      end)
    end)
  end)
end)

describe("output-parser", function()
	local output_parser = require("neotest-kotlin.src.output-parser")

	describe("parse_lines", function()
		it("complete example", function()
			output_parser.parse_lines({
				"> Task :app:cleanTest",
				"> Task :app:checkKotlinGradlePluginConfigurationErrors",
				"> Task :app:compileKotlin UP-TO-DATE",
				"> Task :app:compileJava NO-SOURCE",
				"> Task :app:processResources NO-SOURCE",
				"> Task :app:classes UP-TO-DATE",
				"> Task :app:compileTestKotlin UP-TO-DATE",
				"> Task :app:compileTestJava NO-SOURCE",
				"> Task :app:processTestResources NO-SOURCE",
				"> Task :app:testClasses UP-TO-DATE",
				"> Task :app:test",
				"org.example.KotestDescribeSpec > a namespace > should handle failed assertions FAILED",
				'   io.kotest.assertions.AssertionFailedError: expected:<"b"> but was:<"a">',
				"       at app//org.example.KotestDescribeSpec$1$1$1.invokeSuspend(KotestDescribeExample.kt:9)",
				"       at app//org.example.KotestDescribeSpec$1$1$1.invoke(KotestDescribeExample.kt)",
				"org.example.KotestDescribeSpec > a namespace > should handle passed assertions PASSED",
				"",
				"org.example.KotestDescribeSpec > a namespace > should handle skipped assertions SKIPPED",
				"",
				"org.example.KotestDescribeSpec > a namespace > a nested namespace > org.example.KotestDescribeSpec.should handle failed assertions FAILED",
				'   io.kotest.assertions.AssertionFailedError: expected:<"b"> but was:<"a">',
				"       at app//org.example.KotestDescribeSpec$1$1$4$1.invokeSuspend(KotestDescribeExample.kt:22)",
				"       at app//org.example.KotestDescribeSpec$1$1$4$1.invoke(KotestDescribeExample.kt)",
				"       at app//org.example.KotestDescribeSpec$1$1$4$1.invoke(KotestDescribeExample.kt)",
				"       at app//io.kotest.core.spec.style.scopes.DescribeSpecContainerScope$it$3.invokeSuspend(DescribeSpecContainerScope.kt:112)",
				"org.example.KotestDescribeSpec > a namespace > a nested namespace > org.example.KotestDescribeSpec.should handle passed assertions PASSED",
				"org.example.KotestDescribeSpec > a namespace > a nested namespace > org.example.KotestDescribeSpec.should handle skipped assertions SKIPPED",
				"6 tests completed, 2 failed, 2 skipped",
				"> Task :app:test FAILED",
				"FAILURE: Build failed with an exception.",
				"* What went wrong:",
				"Execution failed for task ':app:test'.",
				"> There were failing tests. See the report at: file:///home/nick/GitHub/neotest-kotlin/lua/tests/example_project/app/build/reports/tests/test/index.html",
				"* Try:",
				"> Run with --scan to get full insights.",
				"BUILD FAILED in 4s",
				"5 actionable tasks: 3 executed, 2 up-to-date",
			}, "/example/path", "org.example.KotestDescribeSpec")
		end)
	end)

	describe("parse_line", function()
		it("invalid test line - gradle task", function()
			local actual =
				output_parser.parse_line("> Task :app:test", "/example/path", "org.example.KotestDescribeSpec")

			assert.is_nil(actual)
		end)

		it("invalid test line - assertion error", function()
			local actual = output_parser.parse_line(
				[[io.kotest.assertions.AssertionFailedError: expected:<"b"> but was:<"a">]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - stacktrace", function()
			local actual = output_parser.parse_line(
				[[at app//io.kotest.engine.test.TestInvocationInterceptor$runBeforeTestAfter$executeWithBeforeAfter$1.invokeSuspend(TestInvocatio]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - failure", function()
			local actual = output_parser.parse_line(
				[[FAILURE: Build failed with an exception.]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - build actions", function()
			local actual = output_parser.parse_line(
				[[5 actionable tasks: 3 executed, 2 up-to-date]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - no test only fqn", function()
			local actual = output_parser.parse_line(
				"org.example.KotestDescribeSpec",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - assertion error", function()
			local actual = output_parser.parse_line(
				[[io.kotest.assertions.AssertionFaiedError: expected:<"b"> but was:<"a">]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("valid - FAILED", function()
			local actual = output_parser.parse_line(
				"org.example.KotestDescribeSpec > should handle failed assertions FAILED",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.equal('/example/path::"should handle failed assertions"', actual.id)
			assert.equal("failed", actual.status)
		end)

		it("valid - PASSED", function()
			local actual = output_parser.parse_line(
				"org.example.KotestDescribeSpec > should handle failed assertions PASSED",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.equal('/example/path::"should handle failed assertions"', actual.id)
			assert.equal("passed", actual.status)
		end)

		it("valid - SKIPPED", function()
			local actual = output_parser.parse_line(
				"org.example.KotestDescribeSpec > should handle failed assertions SKIPPED",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.equal('/example/path::"should handle failed assertions"', actual.id)
			assert.equal("skipped", actual.status)
		end)
	end)

	describe("is_valid_gradle_test_line", function()
		it("valid", function()
			local actual = output_parser.is_valid_gradle_test_line(
				"org.example.KotestDescribeSpec > should handle failed assertions FAILED",
				"org.example.KotestDescribeSpec"
			)

			assert.is_true(actual)
		end)

		it("unknown package prefix", function()
			local actual = output_parser.is_valid_gradle_test_line(
				"org.example.Unknown > should handle failed assertions FAILED",
				"org.example.KotestDescribeSpec"
			)

			assert.is_false(actual)
		end)

		it("no status", function()
			local actual = output_parser.is_valid_gradle_test_line(
				"org.example.KotestDescribeSpec > should handle failed assertions unknown",
				"org.example.KotestDescribeSpec"
			)

			assert.is_false(actual)
		end)
	end)

	describe("parse_status", function()
		it("passed", function()
			local actual = output_parser.parse_status("PASSED")
			assert.equal("passed", actual)
		end)

		it("failed", function()
			local actual = output_parser.parse_status("FAILED")
			assert.equal("failed", actual)
		end)

		it("skipped", function()
			local actual = output_parser.parse_status("SKIPPED")
			assert.equal("skipped", actual)
		end)

		it("none", function()
			local actual = output_parser.parse_status("random input")
			assert.equal("none", actual)
		end)
	end)

	describe("parse_test_id", function()
		it("invalid test line - gradle task", function()
			local actual =
				output_parser.parse_test_id("> Task :app:test", "/example/path", "org.example.KotestDescribeSpec")

			assert.is_nil(actual)
		end)

		it("invalid test line - assertion error", function()
			local actual = output_parser.parse_test_id(
				[[io.kotest.assertions.AssertionFailedError: expected:<"b"> but was:<"a">]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - stacktrace", function()
			local actual = output_parser.parse_test_id(
				[[at app//io.kotest.engine.test.TestInvocationInterceptor$runBeforeTestAfter$executeWithBeforeAfter$1.invokeSuspend(TestInvocatio]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - failure", function()
			local actual = output_parser.parse_test_id(
				[[FAILURE: Build failed with an exception.]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - build actions", function()
			local actual = output_parser.parse_test_id(
				[[5 actionable tasks: 3 executed, 2 up-to-date]],
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("invalid test line - no test only fqn", function()
			local actual = output_parser.parse_test_id(
				"org.example.KotestDescribeSpec",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.is_nil(actual)
		end)

		it("valid top-level test", function()
			local actual = output_parser.parse_test_id(
				"org.example.KotestDescribeSpec > should handle failed assertions FAILED",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.equal('/example/path::"should handle failed assertions"', actual)
		end)

		it("valid single namespace", function()
			local actual = output_parser.parse_test_id(
				"org.example.KotestDescribeSpec > a namespace > should handle failed assertions FAILED",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.equal('/example/path::"a namespace"::"should handle failed assertions"', actual)
		end)

		it("valid multiple nested namespace", function()
			local actual = output_parser.parse_test_id(
				"org.example.KotestDescribeSpec > a namespace > a nested namespace > org.example.KotestDescribeSpec.should handle failed assertions FAILED",
				"/example/path",
				"org.example.KotestDescribeSpec"
			)

			assert.equal(
				'/example/path::"a namespace"::"a nested namespace"::"should handle failed assertions"',
				actual
			)
		end)
	end)
end)

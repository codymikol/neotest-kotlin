local treesitter = require("neotest-kotlin.treesitter")
local nio = require("nio")

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
	local shouldspec_file = vim.fs.joinpath(example_project_path, "KotestShouldSpec.kt")
	local describespec_file = vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")
	local wordspec_file = vim.fs.joinpath(example_project_path, "KotestWordSpec.kt")

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

			local namespace = tree[2][1]
			assert.is_not_nil(namespace)
			assert.equals("namespace", namespace.type)
			assert.equals('"first namespace should"', namespace.name)

			local test = tree[2][2][1]
			assert.is_not_nil(test)
			assert.equals("test", test.type)
			assert.equals('"pass"', test.name)

			local namespace2 = tree[3][1]
			assert.is_not_nil(namespace2)
			assert.equals("namespace", namespace2.type)
			assert.equals('"second namespace when"', namespace2.name)

			local namespace3 = tree[3][2][1]
			assert.is_not_nil(namespace3)
			assert.equals("namespace", namespace3.type)
			assert.equals('"nested namespace should"', namespace3.name)

			local test2 = tree[3][2][2][1]
			assert.is_not_nil(test2)
			assert.equals("test", test2.type)
			assert.equals('"pass"', test2.name)

			local test3 = tree[3][2][3][1]
			assert.is_not_nil(test3)
			assert.equals("test", test3.type)
			assert.equals('"fail"', test3.name)

			local namespace4 = tree[4][1]
			assert.is_not_nil(namespace4)
			assert.equals("namespace", namespace4.type)
			assert.equals('"third namespace when"', namespace4.name)

			local namespace5 = tree[4][2][1]
			assert.is_not_nil(namespace5)
			assert.equals("namespace", namespace5.type)
			assert.equals('"nested namespace should"', namespace5.name)

			local test4 = tree[4][2][2][1]
			assert.is_not_nil(test4)
			assert.equals("test", test4.type)
			assert.equals('"pass"', test4.name)

			local test5 = tree[4][2][3][1]
			assert.is_not_nil(test5)
			assert.equals("test", test5.type)
			assert.equals('"fail"', test5.name)
		end)

		nio.tests.it("FunSpec", function()
			local tree = treesitter.parse_positions(funspec_file):to_list()
			assert.equals("KotestFunSpec.kt", tree[1].name)
			assert.equals("file", tree[1].type)

			local content = tree[2]

			local namespace = content[1]
			assert.is_not_nil(namespace)
			assert.equals('"namespace"', namespace.name)
			assert.equals("namespace", namespace.type)

			local test = content[2][1]
			assert.is_not_nil(test)
			assert.equals("test", test.type)
			assert.equals('"pass"', test.name)

			local test3 = content[4][2][1]
			assert.is_not_nil(test3)
			assert.equals("test", test3.type)
			assert.equals('"pass"', test3.name)

			local test4 = content[4][3][1]
			assert.is_not_nil(test4)
			assert.equals("test", test4.type)
			assert.equals('"fail"', test4.name)
		end)

		nio.tests.it("ShouldSpec", function()
			local tree = treesitter.parse_positions(shouldspec_file):to_list()
			assert.equals("KotestShouldSpec.kt", tree[1].name)
			assert.equals("file", tree[1].type)

			local content = tree[2]

			local namespace = content[1]
			assert.is_not_nil(namespace)
			assert.equals('"namespace"', namespace.name)
			assert.equals("namespace", namespace.type)

			local test = content[2][1]
			assert.is_not_nil(test)
			assert.equals("test", test.type)
			assert.equals('"pass"', test.name)

			local test2 = content[3][1]
			assert.is_not_nil(test2)
			assert.equals("test", test2.type)
			assert.equals('"fail"', test2.name)

			local namespace2 = content[4][1]
			assert.is_not_nil(namespace2)
			assert.equals("namespace", namespace2.type)
			assert.equals('"nested namespace"', namespace2.name)

			local test3 = content[4][2][1]
			assert.is_not_nil(test3)
			assert.equals("test", test3.type)
			assert.equals('"pass"', test3.name)

			local test4 = content[4][3][1]
			assert.is_not_nil(test4)
			assert.equals("test", test4.type)
			assert.equals('"fail"', test4.name)
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
			assert.equals('"a namespace"', namespace.name)

			local test = content[2][1]
			assert.is_not_nil(test)
			assert.equals("test", test.type)
			assert.equals('"should handle failed assertions"', test.name)

			local test2 = content[3][1]
			assert.is_not_nil(test2)
			assert.equals("test", test2.type)
			assert.equals('"should handle passed assertions"', test2.name)

			local namespace2 = content[4][1]
			assert.is_not_nil(namespace2)
			assert.equals("namespace", namespace2.type)
			assert.equals('"a nested namespace"', namespace2.name)

			local test3 = content[4][2][1]
			assert.is_not_nil(test3)
			assert.equals("test", test3.type)
			assert.equals('"should handle failed assertions"', test3.name)

			local test4 = content[4][3][1]
			assert.is_not_nil(test4)
			assert.equals("test", test4.type)
			assert.equals('"should handle passed assertions"', test4.name)
		end)
	end)
end)

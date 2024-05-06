local lib = require("neotest.lib")
local async = require("neotest.async")

local treesitter_query = require("kotest-treesitter-query")

local adapter = { name = "neotest-kotest" }

---Find the project root directory given a current directory to work from.
---Should no root be found, the adapter can still be used in a non-project context if a test file matches.
---@async
---@param dir string @Directory to treat as cwd
---@return string | nil @Absolute root dir of test suite
function adapter.root(dir)
	return lib.files.match_root_pattern("build.gradle.kts")(dir)
end

local ignored_directories =
	{ "docs", "build", "out", "generated", ".gradle", "main", ".idea", "buildSrc", "kapt", "taret" }

---Filter directories when searching for test files
---@async
---@param name string Name of directory
---@param rel_path string Path to directory, relative to root
---@param root string Root directory of project
---@return boolean
function adapter.filter_dir(name, rel_path, root)
	for _, v in ipairs(ignored_directories) do
		if v == name then
			return false
		end
	end
	return true
end

---@async
---@param file_path string
---@return boolean
function adapter.is_test_file(file_path)
	if file_path == nil then
		return false
	end

	if not vim.endswith(file_path, ".kt") then
		return false
	end

	if string.find(file_path, "src/main") then
		return false
	end

	return true
end

local function get_match_type(captured_nodes)
	if captured_nodes["namespace.name"] then
		return "namespace"
	end
	if captured_nodes["test.name"] then
		return "test"
	end
end

function adapter.build_position(file_path, source, captured_nodes)
	local match_type = get_match_type(captured_nodes)
	local definition = captured_nodes[match_type .. ".definition"]

	local build_position = {
		type = match_type,
		path = file_path,
		range = { definition:range() },
	}

	return build_position
end

---Given a file path, parse all the tests within it.
---@async
---@param file_path string Absolute file path
---@return neotest.Tree | nil
function adapter.discover_positions(path)
	local positions = lib.treesitter.parse_positions(path, treesitter_query.value, {
		nested_namespaces = true,
		nested_tests = false,
		-- build_position = 'require("neotest-kotest").build_position',
	})

	return positions
end

function get_package_name(file_path)
	local package_name_query = "(package_header (identifier) @package.name)"

	local file = io.open(file_path)

	if file == nil then
		return "*"
	end

	local code = file:read("*all")

	local new_buffer_number = vim.api.nvim_create_buf(false, true)
	vim.api.nvim_buf_set_lines(new_buffer_number, 0, -1, false, vim.split(code, "\n"))

	file:close()

	local language = "kotlin"

	local parser = vim.treesitter.get_string_parser(code, language)
	local tree = parser:parse()
	local root = tree[1]:root()

	local query = vim.treesitter.query.parse(language, package_name_query)

	for _, match, _ in query:iter_matches(root, new_buffer_number, root:start(), root:end_()) do
		for _, node in pairs(match) do
			local start_row, start_col = node:start()
			local end_row, end_col = node:end_()

			-- string:sub is 1 indexed, but the nodes apis return 0 indexed jawns...
			-- effectively making this a river of brain melting sadness
			local text = code:sub(start_row + 2, end_row - 1):sub(start_col, end_col - 1)

			return text
		end
	end

	-- local package_name = matches[0].captures["package.name"][1]

	-- vim.inspect(package_name)

	return nil
end

---@param args neotest.run.RunArgs
---@return nil | neotest.RunSpec | neotest.RunSpec[]
function adapter.build_spec(args)
	local results_path = async.fn.tempname() .. ".json"

	-- Write something so there is a place to stream to...
	lib.files.write(results_path, "")

	local tree = args.tree

	if not tree then
		return
	end

	local pos = tree:data()

	local root = adapter.root(pos.path)
	local spec = get_package_name(pos.path)
	local test = "*"

	local command_three = "export kotest_filter_tests='"
		.. test
		.. "'; export kotest_filter_specs='"
		.. spec
		.. "'; ./gradlew clean test --info >> "
		.. results_path

	local stream_data, stop_stream = lib.files.stream(results_path)

	return {
		command = command_three,
		cwd = root,
		context = {
			results_path = results_path,
			file = pos.path,
			stop_stream = stop_stream,
		},
		stream = function()
			return function()
				print("streaming...")
				local new_results = stream_data()

				local tests = {}

				tests["foo"] = {
					status = "skipped",
					short = "something goofy",
					output = "comnsole out",
					location = "test",
				}

				return tests
			end
		end,
	}
end

---@async
---@param spec neotest.RunSpec
---@param result neotest.StrategyResult
---@param tree neotest.Tree
---@return table<string, neotest.Result>
function adapter.results(spec, result, tree)
	print("In results")

	print(result)

	spec.context.stop_stream()
	return { "test", {} }
end

return adapter

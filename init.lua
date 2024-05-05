local lib = require("neotest.lib")
local async = require("neotest.async")

local adapter = { name = "neotest-kotest" }

---Find the project root directory given a current directory to work from.
---Should no root be found, the adapter can still be used in a non-project context if a test file matches.
---@async
---@param dir string @Directory to treat as cwd
---@return string | nil @Absolute root dir of test suite
function adapter.root(dir)
	return lib.files.match_root_pattern("build.gradle?(.kts)")(dir)
end

local ignored_directories = { "build", "out", "generated", ".gradle", "main", ".idea", "buildSrc", "kapt", "taret" }

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

	-- todo(mikol): see if this has a kotest import...
	return true
end

---@async
function adapter.discover_positions(path)
	local query = [[

;; --- DESCRIBE SPEC ---

; Matches describe("context") { /** body **/ }

(call_expression 
	(call_expression 
	  (simple_identifier) @func_name (#eq? @func_name "describe")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @namespace.name
          ) 
        )
      )
    )
) @namespace.definition

; Matches it("context") { /** body **/ }

(call_expression 
	(call_expression 
	  (simple_identifier) @func_name (#eq? @func_name "it")
      (call_suffix
        (value_arguments
          (value_argument
            (string_literal) @namespace.name
          ) 
        )
      )
    )
) @namespace.definition

;; -- todo FUN SPEC --
;; -- todo SHOULD SPEC --
;; -- todo STRING SPEC --
;; -- todo BEHAVIOR SPEC --
;; -- todo FREE SPEC --
;; -- todo WORD SPEC --
;; -- todo FEATURE SPEC --
;; -- todo EXPECT SPEC --
;; -- todo ANNOTATION SPEC --
]]

	lib.treesitter.parse_positions(path, query, { nested_namespaces = true })
end

---@param args neotest.RunArgs
---@return nil | neotest.RunSpec | neotest.RunSpec[]
function adapter.build_spec(args)
	local results_path = async.fn.tempname() .. ".json"
	local tree = args.tree

	if not tree then
		return
	end

	local pos = args.tree:data()

	local root = adapter.root(pos.path)

	local package = get_test_package(args)
	local test_name = string.sub()

	local command = "/.gradlew test -Dkotest.filter.specs='"
		.. package
		.. "' -Dkotest.filter.tests='"
		.. test_name
		.. "'"

	local stream_data, stop_stream = lib.files.stream(results_path)

	return {
		command = command,
		cwd = root,
		context = {
			results_path = results_path,
			file = pos.path,
			stop_stream = stop_stream,
		},
		stream = function()
			return function()
				local new_results = stream_data()
				local ok, parsed = pcall(vim.json.decode, new_results, { luanil = { object = true } })

				if not ok or not parsed.testResults then
					return {}
				end

				return {}
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
	spec.context.stop_stream()
	return "something"
end

return adapter

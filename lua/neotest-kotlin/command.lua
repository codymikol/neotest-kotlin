local M = {}

---Constructs the gradle command to execute
---@param tests string the name of the test block
---@param specs string the package name of the file you are interpreting
---@param outfile string where the test output will be written to.
---@return string command the gradle command to execute
M.parse = function(tests, specs, outfile)
	local INIT_SCRIPT_NAME = "test-logging.init.gradle.kts"

	local init_script_path = vim.api.nvim_get_runtime_file(INIT_SCRIPT_NAME, false)[1]
	if init_script_path == nil then
		error(string.format("failed to find '%s' in runtime path", INIT_SCRIPT_NAME))
	end

	return string.format(
		"kotest_filter_specs='%s' kotest_filter_tests='%s' ./gradlew -I %s test --console=plain | tee -a %s",
		specs,
		tests,
		init_script_path,
		outfile
	)
end

return M

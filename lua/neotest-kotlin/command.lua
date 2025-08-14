local M = {}

---Constructs the gradle command to execute
---@param specs string the package name of the file you are interpreting
---@param filter string? the neotest ID to use for filtering
---@param outfile string where the test output will be written to.
---@return string command the gradle command to execute
function M.build(specs, filter, outfile)
  local INIT_SCRIPT_NAME = "test-logging.init.gradle.kts"

  local init_script_path =
    vim.api.nvim_get_runtime_file(INIT_SCRIPT_NAME, false)[1]
  if init_script_path == nil then
    error(
      string.format("failed to find '%s' in runtime path", INIT_SCRIPT_NAME)
    )
  end

  local command = string.format(
    "./gradlew -I %s kotlinTest -Pclasses='%s' -PoutputFile='%s'",
    init_script_path,
    specs,
    outfile
  )

  if filter ~= nil then
    command = command .. " -Pfilter='" .. filter .. "'"
  end

  return command
end

return M

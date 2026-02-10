local M = {}

local INIT_SCRIPT_NAME = "test-logging.init.gradle.kts"

local function determine_init_script_path()
  local init_script_path =
    vim.api.nvim_get_runtime_file(INIT_SCRIPT_NAME, false)[1]
  if init_script_path == nil then
    error(
      string.format("failed to find '%s' in runtime path", INIT_SCRIPT_NAME)
    )
  end

  return vim.fn.fnamemodify(init_script_path, ":p")
end

---Constructs the gradle command to execute tests
---@param specs string the package name of the file you are interpreting
---@param filter string? the neotest ID to use for filtering
---@param outfile string where the test output will be written to.
---@return string command the gradle command to execute
function M.build_execute(specs, filter, outfile)
  local init_script_path = determine_init_script_path()
  local command = string.format(
    "./gradlew -I %s kotlinTestExecute -Pclasses='%s' -PoutputFile='%s'",
    init_script_path,
    specs,
    outfile
  )

  if filter ~= nil then
    command = command .. " -Pfilter='" .. filter .. "'"
  end

  -- KOTEST_PROPERTIES_FILENAME environment variable
  -- https://kotest.io/docs/6.0/intellij/intellij-properties.html#specifying-the-properties-filename
  local kotest_properties_filename = vim.env.KOTEST_PROPERTIES_FILENAME
  if kotest_properties_filename ~= nil then
    command = command
      .. " -Dkotest.properties.filename='"
      .. kotest_properties_filename
      .. "'"
  end

  return command
end

---Constructs the gradle command to discover tests
---@param file string where to discover tests.
---@return string, string[] command the gradle command to execute
function M.build_discover(file)
  assert(
    file ~= nil and not vim.startswith(file, "/"),
    "file must be non-nil and a relative path"
  )

  local args = {
    "-I",
    determine_init_script_path(),
    -- Use gradle configuration cache
    "--configuration-cache",
  }

  table.insert(args, "kotlinTestDiscover_" .. table.concat(
    vim.tbl_map(
      ---@param value string
      function(value)
        return value:match("([^%.]+)")
      end,
      vim.split(file, "/", { plain = true })
    ),
    "_"
  ))

  return "./gradlew", args
end

return M

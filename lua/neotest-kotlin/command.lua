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

---Maximum length of the `-Pclasses` value before falling back to packages.
---Keeps the gradle command line well below OS limits for very large directories.
M.MAX_CLASSES_LENGTH = 4096

---Builds the value for `-Pclasses` from fully qualified class names.
---
---kotlin-test matches each comma separated entry as a prefix of the fully
---qualified class name. When listing every class would exceed
---`M.MAX_CLASSES_LENGTH`, the classes are collapsed into the minimal set of
---their packages (each with a trailing `.` so `org.example.` doesn't match
---`org.examples`). This may run additional classes in those packages that live
---outside of the selected directory; their results are ignored because they
---aren't part of the tree.
---@param classes string[] fully qualified class names
---@return string
function M.build_classes(classes)
  local joined = table.concat(classes, ",")
  if #joined <= M.MAX_CLASSES_LENGTH then
    return joined
  end

  ---@type table<string, boolean>
  local package_set = {}
  for _, class in ipairs(classes) do
    local package = class:match("^(.*)%.[^%.]+$")
    if package == nil then
      -- class in the default package, only an empty prefix can match it
      return ""
    end
    package_set[package .. "."] = true
  end

  ---@type string[]
  local packages = {}
  for package in pairs(package_set) do
    local redundant = false
    for other in pairs(package_set) do
      if other ~= package and vim.startswith(package, other) then
        redundant = true
        break
      end
    end

    if not redundant then
      table.insert(packages, package)
    end
  end

  table.sort(packages)
  return table.concat(packages, ",")
end

---Task executing tests in a Gradle project.
M.EXECUTE_TASK = "kotlinTestExecute"

---Task discovering the tests of a file in a Gradle project.
M.DISCOVER_TASK = "kotlinTestDiscover"

---Constructs the gradle command to execute tests
---@param tasks string[] the tasks to run, e.g. `:app:kotlinTestExecute`
---@param specs string comma separated fully qualified class names or package prefixes
---@param filter string? the neotest ID to use for filtering
---@param output_dir string directory the results of every project are written to, see `project.file_name`
---@return string command the gradle command to execute
function M.build_execute(tasks, specs, filter, output_dir)
  local init_script_path = determine_init_script_path()
  local command = string.format(
    "./gradlew -I %s %s -Pclasses='%s' -PoutputDir='%s'",
    init_script_path,
    table.concat(tasks, " "),
    specs,
    output_dir
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
---
---The file and output are system properties rather than Gradle properties, so
---discovering another file reuses the configuration cache.
---@param task string the discovery task of the project owning the file, e.g. `:app:kotlinTestDiscover`
---@param file string absolute path of the file to discover tests in
---@param output string absolute path of the JSON file to write the discovered tests to
---@return string, string[] command the gradle command to execute
function M.build_discover(task, file, output)
  assert(
    file ~= nil and vim.startswith(file, "/"),
    "file must be non-nil and an absolute path"
  )

  return "./gradlew",
    {
      "-I",
      determine_init_script_path(),
      -- Use gradle configuration cache
      "--configuration-cache",
      task,
      "-DkotlinTestDiscoverFile=" .. file,
      "-DkotlinTestDiscoverOutput=" .. output,
    }
end

---Constructs the gradle command to determine the test files of the project,
---running the task of every Gradle project of the build
---@return string, string[] command the gradle command to execute
function M.build_find_tests()
  return "./gradlew",
    {
      "-I",
      determine_init_script_path(),
      -- Use gradle configuration cache
      "--configuration-cache",
      "kotlinTestFindTests",
    }
end

return M

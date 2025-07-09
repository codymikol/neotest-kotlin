local M = {}

---Finds the nearest parent directory containing a build.gradle or build.gradle.kts file
---@param filepath string the path to the file (e.g. test file)
---@return string|nil module_name the name of the gradle module, or nil if not found

---Runs the Gradle printProjectPaths task and parses the output to map absolute paths to module names
---@param init_script_path string path to the init script
---@return table<string, string> mapping from absolute dir to gradle module (e.g. /abs/path/app -> app)
local function get_gradle_project_paths(init_script_path)
  local handle = io.popen(string.format("./gradlew -I %s printProjectPaths", init_script_path))
  if not handle then return {} end
  local output = handle:read("*a")
  handle:close()
  local map = {}
  for line in output:gmatch("[^\n]+") do
    print("line: " .. line)

    local path, abs = line:match("NEOTEST_GRADLE_PROJECT%s+:(.-)%s+([^	]+)$")
    if not path then
      -- Try with tab separator
      local _, _, p, a = line:find("NEOTEST_GRADLE_PROJECT\t:?(.-)\t(.+)")
      path, abs = p, a
    end
    if path and abs then
      -- Remove leading : from path if present
      path = path:gsub("^:", "")
      map[abs] = path
    end
  end
  return map
end

---Finds the gradle module for a given file path using the mapping from get_gradle_project_paths
---@param filepath string
---@param project_map table<string, string>
---@return string|nil module_name
local function find_gradle_module(filepath, project_map)
  local sep = package.config:sub(1,1)
  local dir = filepath
  while dir and dir ~= "." and dir ~= sep do
    dir = dir:gsub(sep .. "[^" .. sep .. "]+$", "")
    if project_map[dir] then
      return project_map[dir]
    end
    if dir == "." or dir == sep or #dir < 2 then break end
  end
  return nil
end

---@param tests string the name of the test block
---@param specs string the package name of the file you are interpreting
---@param outfile string where the test output will be written to.
---@param filepath string the path to the test file (to determine module)
---@return string command the gradle command to execute
function M.build(tests, specs, outfile, filepath)
  local INIT_SCRIPT_NAME = "test-logging.init.gradle.kts"

  local init_script_path =
    vim.api.nvim_get_runtime_file(INIT_SCRIPT_NAME, false)[1]
  if init_script_path == nil then
    error(
      string.format("failed to find '%s' in runtime path", INIT_SCRIPT_NAME)
    )
  end

  local module = nil
  print("filepath " .. filepath)
  if filepath then
    local project_map = get_gradle_project_paths(init_script_path)
    module = find_gradle_module(filepath, project_map)
  end
  print("module" .. module)
  local gradle_cmd = "./gradlew"
  if module then
    gradle_cmd = string.format("%s :%s:test", gradle_cmd, module)
  else
    gradle_cmd = string.format("%s test", gradle_cmd)
  end
  return string.format(
    "kotest_filter_specs='%s' kotest_filter_tests='%s' %s -I %s --console=plain | tee -a %s",
    specs,
    tests,
    gradle_cmd,
    init_script_path,
    outfile
  )
end

return M

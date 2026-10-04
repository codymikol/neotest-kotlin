---Resolves the Gradle project owning a file or directory of a build.
---
---The owning project is the nearest directory, from the file up to the root
---of the build, containing a `build.gradle.kts` or `build.gradle` file. Its
---Gradle path is derived from its location relative to the root, the default
---layout of `include(...)` in the settings, e.g. `lib/core` is `:lib:core`.
---Files outside of any such directory belong to the root project `:`.
---
---Running Gradle (e.g. a task printing every project) would also cover
---projects with a custom `projectDir`, but would cost a Gradle invocation, so
---the file system is used instead.
local M = {}

local BUILD_FILES = { "build.gradle.kts", "build.gradle" }

---The root project.
M.ROOT = ":"

---Gradle project path per directory.
---@type table<string, string>
local cache = {}

---@param dir string
---@return boolean
local function has_build_file(dir)
  for _, name in ipairs(BUILD_FILES) do
    if vim.uv.fs_stat(vim.fs.joinpath(dir, name)) ~= nil then
      return true
    end
  end

  return false
end

---Determines the Gradle project owning `path`.
---@param root string absolute root directory of the build
---@param path string absolute path of a file or directory in the build
---@return string project_path e.g. `:` or `:lib:core`
function M.find(root, path)
  root = vim.fs.normalize(root)
  path = vim.fs.normalize(path)

  local stat = vim.uv.fs_stat(path)
  local dir = (stat ~= nil and stat.type == "directory") and path
    or vim.fs.dirname(path)

  ---@type string[]
  local visited = {}
  local project_path = M.ROOT

  while vim.startswith(dir, root .. "/") do
    local cached = cache[dir]
    if cached ~= nil then
      project_path = cached
      break
    end

    table.insert(visited, dir)
    if has_build_file(dir) then
      project_path = ":" .. dir:sub(#root + 2):gsub("/", ":")
      break
    end

    dir = vim.fs.dirname(dir)
  end

  for _, visited_dir in ipairs(visited) do
    cache[visited_dir] = project_path
  end

  return project_path
end

---The path of a task of a project, e.g. `:lib:kotlinTestExecute`.
---@param project_path string
---@param task string
---@return string
function M.task(project_path, task)
  if project_path == M.ROOT then
    return M.ROOT .. task
  end

  return project_path .. ":" .. task
end

---Name of the output file of a project in a directory shared by every
---project, see `projectFileName` of the Gradle plugin.
---@param project_path string
---@return string
function M.file_name(project_path)
  return (project_path:gsub(":", "_")) .. ".json"
end

---Clears the cached projects, e.g. after adding a project to the build.
function M.reset()
  cache = {}
end

return M

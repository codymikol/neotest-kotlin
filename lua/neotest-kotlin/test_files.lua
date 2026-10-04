---Determines which Kotlin files contain tests.
---
---The `kotlinTestFindTests` Gradle task determines the test files of a whole
---build in a single run: it runs in every Gradle project, each writing the
---test files of its own project to a file of `M.OUTPUT_DIR`. The result is
---cached per project root, so that `is_test_file`, which neotest calls for
---every file while scanning, starts Gradle at most once per project instead of
---once per file.
---
---The same run discovers the tests of every test file (see `discovered_path`),
---so `discover_positions` reads them from the cached run as well instead of
---starting a Gradle build per file, see `M.discovered`.
---
---The cache of a project is refreshed when
--- - a `.kt` file of the project is written from Neovim (`BufWritePost`)
--- - a file is modified after the cached result was determined (its mtime)
---
---Should the task fail, the path based heuristic of `filter.lua` is used
---instead until the cache is refreshed.
local command = require("neotest-kotlin.command")
local filter = require("neotest-kotlin.filter")
local gradle = require("neotest-kotlin.gradle")
local lib = require("neotest.lib")
local logger = require("neotest.logging")
local nio = require("nio")

local M = {}

---Output directory of the `kotlinTestFindTests` tasks, relative to the project
---root, with one JSON file per Gradle project.
M.OUTPUT_DIR = "build/kotlinTestFindTests"

---@class neotest-kotlin.Timestamp
---@field sec integer
---@field nsec integer

---@class neotest-kotlin.TestFiles
---@field files table<string, string>? discovered tests (JSON file) per absolute path of a test file, nil if the task failed
---@field started_at neotest-kotlin.Timestamp when the task started, files modified later are stale
---@field stale boolean whether the files must be determined again

---@type table<string, neotest-kotlin.TestFiles>
local cache = {}

---In-flight runs of the task per project root, so concurrent calls run it once.
---@type table<string, nio.control.Future>
local loading = {}

---Project root per directory, false when the directory has no project root.
---@type table<string, string | false>
local roots = {}

---@return neotest-kotlin.Timestamp
local function now()
  local sec, usec = vim.uv.gettimeofday()
  return { sec = sec, nsec = usec * 1000 }
end

---@param a neotest-kotlin.Timestamp
---@param b neotest-kotlin.Timestamp
---@return boolean
local function is_after(a, b)
  return a.sec > b.sec or (a.sec == b.sec and a.nsec > b.nsec)
end

---@param path string
---@return string
local function normalize(path)
  if not vim.startswith(path, "/") then
    path = vim.fs.joinpath(assert(vim.uv.cwd()), path)
  end

  return vim.fs.normalize(path)
end

---The JSON file the task writes the tests discovered in a test file to,
---`<directory of the project>/<absolute path of the file>.json`, see
---`discoveredFile` of the Gradle plugin.
---@param project_dir string e.g. `<root>/build/kotlinTestFindTests/_app`
---@param file string absolute path of the test file, as written by the task
---@return string
function M.discovered_path(project_dir, file)
  return vim.fs.joinpath(project_dir, (file:gsub("^/+", ""))) .. ".json"
end

---Runs the `kotlinTestFindTests` task in `root`.
---@async
---@param root string project root
---@return table<string, string>? test_files the discovered tests (JSON file) per absolute path of a test file, nil on failure
---@return string? error
function M.run(root)
  local cmd, args = command.build_find_tests()

  -- builds of a project run one at a time, see `neotest-kotlin.gradle`
  local status_code, _, stderr = gradle.run(root, cmd, args)
  if status_code == nil then
    return nil, string.format("failed to start '%s': %s", cmd, stderr)
  end

  if status_code ~= 0 then
    return nil,
      string.format(
        "'%s %s' in %s failed with status code %d: %s",
        cmd,
        table.concat(args, " "),
        root,
        status_code,
        stderr
      )
  end

  local output_dir = vim.fs.joinpath(root, M.OUTPUT_DIR)
  ---@type string[]
  local output_paths = {}
  if lib.files.exists(output_dir) then
    for name, type in vim.fs.dir(output_dir) do
      -- named after the Gradle project path, e.g. `_app.json` for `:app`
      if
        type == "file"
        and vim.startswith(name, "_")
        and vim.endswith(name, ".json")
      then
        table.insert(output_paths, vim.fs.joinpath(output_dir, name))
      end
    end
  end

  if #output_paths == 0 then
    return nil, string.format("no output files created in '%s'", output_dir)
  end

  ---@type table<string, string>
  local test_files = {}
  for _, output_path in ipairs(output_paths) do
    local ok, decoded = pcall(vim.json.decode, lib.files.read(output_path))
    if
      not ok
      or type(decoded) ~= "table"
      or type(decoded.testFiles) ~= "table"
    then
      return nil, string.format("invalid output file '%s'", output_path)
    end

    -- e.g. `_app` for `_app.json`
    local project_dir = output_path:sub(1, -#".json" - 1)
    for _, file in ipairs(decoded.testFiles) do
      test_files[file] = M.discovered_path(project_dir, file)
    end
  end

  return test_files
end

---Determines the test files of `root` and stores them in the cache.
---@async
---@param root string
local function load(root)
  local started_at = now()
  local test_files, err = M.run(root)

  ---@type table<string, string>?
  local files = nil
  if test_files == nil then
    logger.warn(
      "neotest-kotlin: failed to determine test files, falling back to file paths:",
      err
    )
  else
    files = {}
    local count = 0
    for file, discovered in pairs(test_files) do
      count = count + 1
      files[normalize(file)] = discovered
      -- the paths of neotest may resolve symlinks differently
      local real = vim.uv.fs_realpath(file)
      if real ~= nil then
        files[normalize(real)] = discovered
      end
    end
    logger.debug("neotest-kotlin: found", count, "test files in", root)
  end

  cache[root] = { files = files, started_at = started_at, stale = false }
end

---Whether `file_path` was modified after `entry` was determined.
---@async
---@param entry neotest-kotlin.TestFiles
---@param file_path string
---@return boolean
local function modified_since(entry, file_path)
  local err, stat = nio.uv.fs_stat(file_path)
  if err ~= nil or stat == nil then
    return false
  end

  -- Filesystems stamp mtimes from a coarse clock that can lag `gettimeofday`
  -- by a few milliseconds, so a file written right as the task starts may be
  -- missed until it's written again (BufWritePost) or the cache is refreshed.
  -- mtimes in the future (e.g. clock skew) would determine the files forever
  return is_after(stat.mtime, entry.started_at)
    and not is_after(stat.mtime, now())
end

---Returns the up to date test files of `root`, running the task when needed.
---@async
---@param root string
---@param file_path string? file that must not be modified since determining
---@return table<string, string>? files discovered tests (JSON file) per test file, nil if they couldn't be determined
function M.get(root, file_path)
  local entry = cache[root]
  local needs_load = entry == nil
    or entry.stale
    or (file_path ~= nil and modified_since(entry, file_path))

  if needs_load then
    local future = loading[root]
    if future == nil then
      future = nio.control.future()
      loading[root] = future
      local ok, err = pcall(load, root)
      loading[root] = nil
      if not ok then
        logger.error("neotest-kotlin: failed to determine test files:", err)
        cache[root] = { files = nil, started_at = now(), stale = false }
      end
      future.set()
    else
      future.wait()
    end
  end

  return cache[root] and cache[root].files
end

---@param file_path string normalized absolute path
---@param find_root fun(dir: string): string? finds the project root of a directory
---@return string? root
local function project_root(file_path, find_root)
  local dir = vim.fs.dirname(file_path)
  local root = roots[dir]
  if root == nil then
    root = find_root(dir) or false
    roots[dir] = root
  end

  return root or nil
end

---@param files table<string, string>
---@param file_path string normalized absolute path
---@return string? discovered
local function lookup(files, file_path)
  if files[file_path] ~= nil then
    return files[file_path]
  end

  local real = vim.uv.fs_realpath(file_path)
  return real ~= nil and files[normalize(real)] or nil
end

---Whether `file_path` contains tests.
---@async
---@param file_path string? absolute path
---@param find_root fun(dir: string): string? finds the project root of a directory
---@return boolean
function M.is_test_file(file_path, find_root)
  if not filter.is_test_file(file_path) then
    return false
  end
  ---@cast file_path string

  -- the task can only run asynchronously
  if nio.current_task() == nil then
    return true
  end

  file_path = normalize(file_path)
  local root = project_root(file_path, find_root)

  -- without a gradle project there is no task to run
  if root == nil then
    return true
  end

  local files = M.get(root, file_path)
  if files == nil then
    return true
  end

  return lookup(files, file_path) ~= nil
end

---The JSON file with the tests the task discovered in `file_path`, nil when
---there is none: the file isn't a test file, the task failed or the file was
---modified after the task ran (its tests must be discovered on their own).
---
---Runs the task when the test files of the project aren't determined yet or
---are stale (see `M.invalidate`), concurrent calls share a single run.
---@async
---@param file_path string absolute path
---@param find_root fun(dir: string): string? finds the project root of a directory
---@return string? discovered
function M.discovered(file_path, find_root)
  file_path = normalize(file_path)
  local root = project_root(file_path, find_root)
  if root == nil then
    return nil
  end

  -- unlike `is_test_file`, a modified file doesn't run the task of the whole
  -- build again, only its own discovery
  local files = M.get(root)
  local entry = cache[root]
  if files == nil or entry == nil or modified_since(entry, file_path) then
    return nil
  end

  return lookup(files, file_path)
end

---Marks the test files of every project containing `file_path` as stale,
---they are determined again the next time they are needed.
---@param file_path string
function M.invalidate(file_path)
  local path = normalize(file_path)
  for root, entry in pairs(cache) do
    if vim.startswith(path, normalize(root) .. "/") then
      entry.stale = true
    end
  end
end

---Clears the cache of every project.
function M.reset()
  cache = {}
  loading = {}
  roots = {}
end

---Refresh test files on writes of Kotlin files
---@param group integer? autocmd group
function M.setup_autocmds(group)
  group = group
    or vim.api.nvim_create_augroup(
      "neotest-kotlin.test_files",
      { clear = true }
    )
  vim.api.nvim_create_autocmd("BufWritePost", {
    group = group,
    pattern = "*.kt",
    callback = function(event)
      M.invalidate(vim.fn.fnamemodify(event.match, ":p"))
    end,
  })
end

return M

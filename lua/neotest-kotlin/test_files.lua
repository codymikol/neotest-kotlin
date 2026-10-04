---Determines which Kotlin files contain tests.
---
---The `kotlinTestFindTests` Gradle task determines the test files of a whole
---project in a single run. Its result is cached per project root, so that
---`is_test_file`, which neotest calls for every file while scanning, starts
---Gradle at most once per project instead of once per file.
---
---The cache of a project is refreshed when
--- - a `.kt` file of the project is written from Neovim (`BufWritePost`)
--- - a file is modified after the cached result was determined (its mtime)
---
---Should the task fail, the path based heuristic of `filter.lua` is used
---instead until the cache is refreshed.
local command = require("neotest-kotlin.command")
local filter = require("neotest-kotlin.filter")
local lib = require("neotest.lib")
local logger = require("neotest.logging")
local nio = require("nio")

local M = {}

---Output of the `kotlinTestFindTests` task, relative to the project root.
M.OUTPUT_PATH = "build/kotlinTestFindTests/test-files.json"

---@class neotest-kotlin.Timestamp
---@field sec integer
---@field nsec integer

---@class neotest-kotlin.TestFiles
---@field files table<string, boolean>? absolute paths of test files, nil if the task failed
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

---Runs the `kotlinTestFindTests` task in `root`.
---@async
---@param root string project root
---@return string[]? test_files absolute paths, nil on failure
---@return string? error
function M.run(root)
  local cmd, args = command.build_find_tests()

  local process, err = nio.process.run({
    cmd = cmd,
    args = args,
    cwd = root,
  })
  if process == nil then
    return nil, string.format("failed to start '%s': %s", cmd, err)
  end

  -- consume the output so a full pipe never blocks gradle
  local outputs = nio.gather({
    function()
      return process.stdout.read()
    end,
    function()
      return process.stderr.read()
    end,
  })
  local status_code = process.result(true)

  if status_code ~= 0 then
    return nil,
      string.format(
        "'%s %s' in %s failed with status code %d: %s",
        cmd,
        table.concat(args, " "),
        root,
        status_code,
        outputs[2] or ""
      )
  end

  local output_path = vim.fs.joinpath(root, M.OUTPUT_PATH)
  if not lib.files.exists(output_path) then
    return nil, string.format("no output file created '%s'", output_path)
  end

  local ok, decoded = pcall(vim.json.decode, lib.files.read(output_path))
  if
    not ok
    or type(decoded) ~= "table"
    or type(decoded.testFiles) ~= "table"
  then
    return nil, string.format("invalid output file '%s'", output_path)
  end

  return decoded.testFiles
end

---Determines the test files of `root` and stores them in the cache.
---@async
---@param root string
local function load(root)
  local started_at = now()
  local test_files, err = M.run(root)

  ---@type table<string, boolean>?
  local files = nil
  if test_files == nil then
    logger.warn(
      "neotest-kotlin: failed to determine test files, falling back to file paths:",
      err
    )
  else
    files = {}
    for _, file in ipairs(test_files) do
      files[normalize(file)] = true
      -- the paths of neotest may resolve symlinks differently
      local real = vim.uv.fs_realpath(file)
      if real ~= nil then
        files[normalize(real)] = true
      end
    end
    logger.debug("neotest-kotlin: found", #test_files, "test files in", root)
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
---@return table<string, boolean>? files nil if they couldn't be determined
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
  local dir = vim.fs.dirname(file_path)
  local root = roots[dir]
  if root == nil then
    root = find_root(dir) or false
    roots[dir] = root
  end

  -- without a gradle project there is no task to run
  if not root then
    return true
  end

  local files = M.get(root, file_path)
  if files == nil then
    return true
  end

  if files[file_path] then
    return true
  end

  local real = vim.uv.fs_realpath(file_path)
  return real ~= nil and files[normalize(real)] == true
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

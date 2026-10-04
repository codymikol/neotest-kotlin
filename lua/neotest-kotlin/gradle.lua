---Runs Gradle builds of a project one at a time.
---
---A Gradle daemon runs a single build at a time, so every concurrent build of
---the same project starts another daemon. neotest discovers files
---concurrently (`discovery.concurrent` workers), and every discovery is a
---Gradle build loading the Kotlin Analysis API with the project's classpath,
---so running them concurrently starts many memory hungry daemons. Builds of
---the same project root are therefore queued and run one after another.
local nio = require("nio")

local M = {}

---@type table<string, nio.control.Semaphore>
local semaphores = {}

---Runs a process to completion. Replaceable in tests.
---@async
---@param opts { cmd: string, args: string[], cwd: string }
---@return integer? status_code nil if the process couldn't be started
---@return string stdout
---@return string stderr, or the reason the process couldn't be started
function M.spawn(opts)
  local process, err = nio.process.run({
    cmd = opts.cmd,
    args = opts.args,
    cwd = opts.cwd,
  })
  if process == nil then
    return nil, "", tostring(err)
  end

  -- consume both streams while waiting, a full pipe would block gradle forever
  local outputs = nio.gather({
    function()
      return process.stdout.read()
    end,
    function()
      return process.stderr.read()
    end,
  })
  local status_code = process.result(true)

  return status_code, outputs[1] or "", outputs[2] or ""
end

---Runs `cmd args` in `root`, waiting for any other Gradle build of `root`
---started through this module to finish first.
---@async
---@param root string project root, builds of the same root run one at a time
---@param cmd string
---@param args string[]
---@return integer? status_code nil if the process couldn't be started
---@return string stdout
---@return string stderr, or the reason the process couldn't be started
function M.run(root, cmd, args)
  root = vim.fs.normalize(root)

  local semaphore = semaphores[root]
  if semaphore == nil then
    semaphore = nio.control.semaphore(1)
    semaphores[root] = semaphore
  end

  semaphore.acquire()
  local ok, status_code, stdout, stderr =
    pcall(M.spawn, { cmd = cmd, args = args, cwd = root })
  semaphore.release()

  if not ok then
    error(status_code)
  end

  return status_code, stdout, stderr
end

return M

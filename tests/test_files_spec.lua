local nio = require("nio")
local test_files = require("neotest-kotlin.test_files")

describe("test_files", function()
  local original_run = test_files.run

  ---@type string
  local root
  ---@type string[]
  local roots_searched
  ---@type string[]
  local runs
  ---@type string[]?
  local result

  ---@param dir string
  ---@return string?
  local function find_root(dir)
    table.insert(roots_searched, dir)
    if dir == root or vim.startswith(dir, root .. "/") then
      return root
    end
    return nil
  end

  ---Writes `content` to `path`, creating its parent directories.
  ---Plain Lua and libuv so it works in async tests.
  ---@param path string absolute path
  ---@param content string
  local function write(path, content)
    local dir = ""
    for segment in vim.fs.dirname(path):gmatch("[^/]+") do
      dir = dir .. "/" .. segment
      vim.uv.fs_mkdir(dir, 493)
    end

    local handle = assert(io.open(path, "w"))
    handle:write(content)
    handle:close()
  end

  ---@param path string relative to root
  ---@return string
  local function file(path)
    local absolute = vim.fs.joinpath(root, path)
    write(absolute, "package org.example")
    return absolute
  end

  ---Sets the modification time of `path` to the past, as if it existed before determining test files
  ---@param path string
  local function age(path)
    local past = os.time() - 60
    assert(vim.uv.fs_utime(path, past, past))
  end

  ---Sets the modification time of `path` to now. Filesystems stamp mtimes
  ---from a coarse clock that can lag behind `gettimeofday`, so a file written
  ---right after determining test files could otherwise look older than that.
  ---@param path string
  local function touch(path)
    local sec, usec = vim.uv.gettimeofday()
    local now = sec + usec / 1e6
    assert(vim.uv.fs_utime(path, now, now))
  end

  local spec_file, helper_file

  before_each(function()
    test_files.reset()
    root = vim.fs.normalize(vim.fn.tempname())
    roots_searched = {}
    runs = {}

    spec_file = file("app/src/test/kotlin/org/example/ExampleSpec.kt")
    helper_file = file("app/src/test/kotlin/org/example/Helpers.kt")
    age(spec_file)
    age(helper_file)
    result = { spec_file }

    test_files.run = function(project_root)
      table.insert(runs, project_root)
      -- yield like running gradle does
      nio.sleep(10)
      return result, result == nil and "task failed" or nil
    end
  end)

  after_each(function()
    test_files.run = original_run
    vim.fn.delete(root, "rf")
  end)

  nio.tests.it("answers from the test files determined by gradle", function()
    assert.is_true(test_files.is_test_file(spec_file, find_root))
    assert.is_false(test_files.is_test_file(helper_file, find_root))
    assert.are.same({ root }, runs)
  end)

  nio.tests.it("rejects files without running gradle", function()
    assert.is_false(test_files.is_test_file(nil, find_root))
    assert.is_false(
      test_files.is_test_file(
        vim.fs.joinpath(root, "build.gradle.kts"),
        find_root
      )
    )
    assert.is_false(
      test_files.is_test_file(
        vim.fs.joinpath(root, "app/src/main/kotlin/App.kt"),
        find_root
      )
    )
    assert.are.same({}, runs)
  end)

  nio.tests.it("runs gradle once for concurrent calls", function()
    local results = nio.gather({
      function()
        return test_files.is_test_file(spec_file, find_root)
      end,
      function()
        return test_files.is_test_file(helper_file, find_root)
      end,
      function()
        return test_files.is_test_file(spec_file, find_root)
      end,
    })

    assert.are.same({ true, false, true }, results)
    assert.are.same({ root }, runs)
  end)

  nio.tests.it("caches the project root per directory", function()
    test_files.is_test_file(spec_file, find_root)
    test_files.is_test_file(helper_file, find_root)

    assert.equals(1, #roots_searched)
  end)

  nio.tests.it("runs gradle per project root", function()
    local other_root = root .. "-other"
    local other_file = vim.fs.joinpath(other_root, "src/test/kotlin/Spec.kt")
    write(other_file, "package org.example")
    age(other_file)

    local function find_roots(dir)
      if vim.startswith(dir, other_root) then
        return other_root
      end
      return find_root(dir)
    end

    test_files.is_test_file(spec_file, find_roots)
    test_files.is_test_file(other_file, find_roots)
    test_files.is_test_file(helper_file, find_roots)

    assert.are.same({ root, other_root }, runs)
    vim.uv.fs_unlink(other_file)
  end)

  nio.tests.it("falls back to the file path when gradle fails", function()
    result = nil

    assert.is_true(test_files.is_test_file(spec_file, find_root))
    assert.is_true(test_files.is_test_file(helper_file, find_root))
    -- doesn't retry for every file
    assert.are.same({ root }, runs)
  end)

  nio.tests.it("falls back to the file path when gradle errors", function()
    test_files.run = function(project_root)
      table.insert(runs, project_root)
      error("unexpected")
    end

    assert.is_true(test_files.is_test_file(helper_file, find_root))
    assert.is_true(test_files.is_test_file(spec_file, find_root))
    assert.are.same({ root }, runs)
  end)

  nio.tests.it("falls back to the file path without a project root", function()
    assert.is_true(test_files.is_test_file(helper_file, function()
      return nil
    end))
    assert.are.same({}, runs)
  end)

  it("falls back to the file path outside of an async context", function()
    assert.is_true(test_files.is_test_file(helper_file, find_root))
    assert.are.same({}, runs)
  end)

  nio.tests.it("runs gradle again once invalidated", function()
    assert.is_false(test_files.is_test_file(helper_file, find_root))

    result = { spec_file, helper_file }
    test_files.invalidate(helper_file)

    assert.is_true(test_files.is_test_file(helper_file, find_root))
    assert.is_true(test_files.is_test_file(spec_file, find_root))
    assert.are.same({ root, root }, runs)
  end)

  nio.tests.it("ignores invalidation of other projects", function()
    assert.is_false(test_files.is_test_file(helper_file, find_root))

    test_files.invalidate(root .. "-other/src/test/kotlin/Spec.kt")

    assert.is_false(test_files.is_test_file(helper_file, find_root))
    assert.are.same({ root }, runs)
  end)

  nio.tests.it("runs gradle again for files modified since", function()
    assert.is_false(test_files.is_test_file(helper_file, find_root))

    result = { spec_file, helper_file }
    -- e.g. modified outside of Neovim
    write(helper_file, "package org.example\nclass Spec")
    touch(helper_file)

    assert.is_true(test_files.is_test_file(helper_file, find_root))
    assert.are.same({ root, root }, runs)
  end)

  nio.tests.it("detects new files", function()
    assert.is_false(test_files.is_test_file(helper_file, find_root))

    local new_file = file("app/src/test/kotlin/org/example/NewSpec.kt")
    touch(new_file)
    result = { spec_file, new_file }

    assert.is_true(test_files.is_test_file(new_file, find_root))
    assert.is_false(test_files.is_test_file(helper_file, find_root))
    assert.are.same({ root, root }, runs)
  end)

  nio.tests.it("invalidates on BufWritePost of Kotlin files", function()
    -- before yielding, autocmds can't be created in a fast event
    local group = vim.api.nvim_create_augroup("test_files_spec", {})
    test_files.setup_autocmds(group)

    assert.is_false(test_files.is_test_file(helper_file, find_root))
    result = { spec_file, helper_file }

    nio.api.nvim_exec_autocmds("BufWritePost", {
      group = group,
      pattern = spec_file,
    })
    nio.api.nvim_del_augroup_by_id(group)

    assert.is_true(test_files.is_test_file(helper_file, find_root))
    assert.are.same({ root, root }, runs)
  end)
end)

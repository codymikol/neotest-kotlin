local gradle = require("neotest-kotlin.gradle")
local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")
local test_files = require("neotest-kotlin.test_files")

---Discovery of positions without running Gradle: `gradle.spawn` writes the
---outputs of `kotlinTestFindTests` and `kotlinTestDiscover` instead.
describe("discover_positions", function()
  local original_spawn = gradle.spawn

  ---@type string
  local root
  ---@type string[] absolute paths of the test files
  local files
  ---@type string[] the task of every Gradle build
  local builds
  ---@type integer
  local running, max_running

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

  ---Discovered tests of `path` with a class named `class`.
  ---@param path string
  ---@param class string
  ---@return string
  local function discovered_json(path, class)
    local function position(line)
      return {
        filename = path,
        startLine = line,
        endLine = line + 2,
        startColumn = 1,
        endColumn = 1,
      }
    end

    return vim.json.encode({
      tests = {
        {
          id = "org.example." .. class,
          name = class,
          type = "CONTAINER",
          position = position(1),
          tests = {
            {
              id = "org.example." .. class .. "::pass",
              name = "pass",
              type = "TEST",
              position = position(2),
            },
          },
        },
      },
      warnings = {},
    })
  end

  ---@param args string[]
  ---@param name string
  ---@return string?
  local function system_property(args, name)
    for _, arg in ipairs(args) do
      local value = arg:match("^%-D" .. name .. "=(.*)$")
      if value ~= nil then
        return value
      end
    end
    return nil
  end

  before_each(function()
    test_files.reset()
    root = vim.fs.normalize(vim.fn.tempname())
    write(vim.fs.joinpath(root, "gradlew"), "")

    files = {}
    for _, name in ipairs({ "ASpec.kt", "BSpec.kt", "CSpec.kt", "DSpec.kt" }) do
      local path = vim.fs.joinpath(root, "src/test/kotlin/org/example", name)
      write(path, "package org.example")
      -- as if it existed before determining test files
      local past = os.time() - 60
      assert(vim.uv.fs_utime(path, past, past))
      table.insert(files, path)
    end

    builds = {}
    running, max_running = 0, 0

    ---@param opts { cmd: string, args: string[], cwd: string }
    gradle.spawn = function(opts)
      running = running + 1
      max_running = math.max(max_running, running)
      -- yield like running gradle does
      nio.sleep(10)

      local discover_output =
        system_property(opts.args, "kotlinTestDiscoverOutput")
      if discover_output ~= nil then
        table.insert(builds, "kotlinTestDiscover")
        local file =
          assert(system_property(opts.args, "kotlinTestDiscoverFile"))
        write(discover_output, discovered_json(file, "Single"))
      else
        assert(vim.tbl_contains(opts.args, "kotlinTestFindTests"))
        table.insert(builds, "kotlinTestFindTests")
        local output_dir = vim.fs.joinpath(opts.cwd, test_files.OUTPUT_DIR)
        write(
          vim.fs.joinpath(output_dir, "_.json"),
          vim.json.encode({ testFiles = files })
        )
        for _, file in ipairs(files) do
          write(
            test_files.discovered_path(vim.fs.joinpath(output_dir, "_"), file),
            discovered_json(file, "Batch")
          )
        end
      end

      running = running - 1
      return 0, "", ""
    end
  end)

  after_each(function()
    gradle.spawn = original_spawn
    test_files.reset()
    vim.fn.delete(root, "rf")
  end)

  ---@param tree neotest.Tree?
  ---@return string[] ids of the classes
  local function classes(tree)
    assert(tree ~= nil)
    return vim.tbl_map(function(child)
      return child:data().id
    end, tree:children())
  end

  ---Discovers the positions of `paths` concurrently, like neotest's discovery workers.
  ---@param paths string[]
  ---@return neotest.Tree[]
  local function discover_concurrently(paths)
    return nio.gather(vim.tbl_map(function(path)
      return function()
        return neotest_kotlin.discover_positions(path)
      end
    end, paths))
  end

  nio.tests.it("discovers every file with a single Gradle build", function()
    local trees = discover_concurrently(files)

    assert.are.same(#files, #trees)
    for index, tree in ipairs(trees) do
      assert.are.same({ files[index] .. "::org.example.Batch" }, classes(tree))
    end
    assert.are.same({ "kotlinTestFindTests" }, builds)
    assert.are.same(1, max_running)
  end)

  nio.tests.it("reuses the build of is_test_file", function()
    assert.is_true(neotest_kotlin.is_test_file(files[1]))

    discover_concurrently(files)

    assert.are.same({ "kotlinTestFindTests" }, builds)
  end)

  nio.tests.it("discovers a file modified since on its own", function()
    discover_concurrently(files)

    -- e.g. modified outside of Neovim
    local sec, usec = vim.uv.gettimeofday()
    assert(vim.uv.fs_utime(files[1], sec + usec / 1e6, sec + usec / 1e6))

    local trees = discover_concurrently(files)

    assert.are.same({ files[1] .. "::org.example.Single" }, classes(trees[1]))
    assert.are.same({ files[2] .. "::org.example.Batch" }, classes(trees[2]))
    assert.are.same({ "kotlinTestFindTests", "kotlinTestDiscover" }, builds)
  end)

  nio.tests.it("discovers every file again once invalidated", function()
    discover_concurrently(files)

    -- e.g. written from Neovim (BufWritePost)
    test_files.invalidate(files[1])
    discover_concurrently(files)

    assert.are.same({ "kotlinTestFindTests", "kotlinTestFindTests" }, builds)
  end)

  nio.tests.it("discovers a file without batch result on its own", function()
    local other = vim.fs.joinpath(root, "src/test/kotlin/org/example/Other.kt")
    write(other, "package org.example")
    local past = os.time() - 60
    assert(vim.uv.fs_utime(other, past, past))

    local trees = discover_concurrently({ files[1], other })

    assert.are.same({ files[1] .. "::org.example.Batch" }, classes(trees[1]))
    assert.are.same({ other .. "::org.example.Single" }, classes(trees[2]))
    assert.are.same({ "kotlinTestFindTests", "kotlinTestDiscover" }, builds)
    assert.are.same(1, max_running)
  end)

  nio.tests.it("discovers files on their own when the task fails", function()
    local spawn = gradle.spawn
    gradle.spawn = function(opts)
      if vim.tbl_contains(opts.args, "kotlinTestFindTests") then
        table.insert(builds, "kotlinTestFindTests")
        return 1, "", "failed"
      end
      return spawn(opts)
    end

    local trees = discover_concurrently({ files[1], files[2] })

    assert.are.same({ files[1] .. "::org.example.Single" }, classes(trees[1]))
    assert.are.same({ files[2] .. "::org.example.Single" }, classes(trees[2]))
    assert.are.same(
      { "kotlinTestFindTests", "kotlinTestDiscover", "kotlinTestDiscover" },
      builds
    )
  end)
end)

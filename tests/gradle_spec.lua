local gradle = require("neotest-kotlin.gradle")
local nio = require("nio")

describe("gradle", function()
  local original_spawn = gradle.spawn

  ---@type table<string, integer>
  local running
  ---@type table<string, integer>
  local max_running
  ---@type string[]
  local started

  before_each(function()
    running = {}
    max_running = {}
    started = {}

    ---@param opts { cmd: string, args: string[], cwd: string }
    gradle.spawn = function(opts)
      table.insert(started, opts.args[1])
      running[opts.cwd] = (running[opts.cwd] or 0) + 1
      max_running[opts.cwd] =
        math.max(max_running[opts.cwd] or 0, running[opts.cwd])

      nio.sleep(10)

      running[opts.cwd] = running[opts.cwd] - 1
      if opts.args[1] == "fail" then
        error("spawn failed")
      end
      return 0, "out " .. opts.args[1], ""
    end
  end)

  after_each(function()
    gradle.spawn = original_spawn
  end)

  ---@param root string
  ---@param name string
  ---@return fun(): integer?, string, string
  local function build(root, name)
    return function()
      return gradle.run(root, "./gradlew", { name })
    end
  end

  nio.tests.it("runs builds of the same project one at a time", function()
    local results = nio.gather({
      build("/project", "a"),
      build("/project", "b"),
      build("/project", "c"),
      build("/project", "d"),
    })

    assert.are.same({ ["/project"] = 1 }, max_running)
    assert.are.same(4, #started)
    assert.are.same(0, results[1])
  end)

  nio.tests.it("returns the output of the build", function()
    local status_code, stdout, stderr = gradle.run("/project", "./gradlew", {
      "a",
    })

    assert.are.same(0, status_code)
    assert.are.same("out a", stdout)
    assert.are.same("", stderr)
  end)

  nio.tests.it("runs builds of different projects concurrently", function()
    nio.gather({
      build("/one", "a"),
      build("/two", "b"),
      build("/one", "c"),
      build("/two", "d"),
    })

    assert.are.same({ ["/one"] = 1, ["/two"] = 1 }, max_running)

    -- both projects started before either finished its second build
    assert.are.same({ "a", "b" }, vim.list_slice(started, 1, 2))
  end)

  nio.tests.it("treats equivalent paths as the same project", function()
    nio.gather({
      build("/project", "a"),
      build("/project/", "b"),
    })

    assert.are.same(1, max_running["/project"])
  end)

  nio.tests.it("runs the next build after a failing one", function()
    local results = nio.gather({
      function()
        return pcall(gradle.run, "/project", "./gradlew", { "fail" })
      end,
      build("/project", "b"),
    })

    assert.is_false(results[1])
    assert.are.same({ "fail", "b" }, started)
  end)

  nio.tests.it("drains large output instead of blocking the process", function()
    gradle.spawn = original_spawn

    -- ~2 MB on stdout and stderr, well beyond pipe buffers
    local status_code, stdout, stderr = gradle.run(vim.uv.cwd(), "sh", {
      "-c",
      "head -c 2000000 /dev/zero | tr '\\0' a; head -c 2000000 /dev/zero | tr '\\0' b >&2",
    })

    assert.are.same(0, status_code)
    assert.are.same(2000000, #stdout)
    assert.are.same(2000000, #stderr)
  end)

  nio.tests.it("reports processes that can't be started", function()
    gradle.spawn = original_spawn

    local status_code, _, stderr =
      gradle.run(vim.uv.cwd(), "/does/not/exist", {})

    assert.is_nil(status_code)
    assert.is_true(#stderr > 0)
  end)
end)

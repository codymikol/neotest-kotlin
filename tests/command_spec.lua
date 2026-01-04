local command = require("neotest-kotlin.command")

describe("command", function()
  local init_script_path = vim.fn.fnamemodify(
    vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1],
    ":p"
  )

  describe("build_discover", function()
    it("no file", function()
      local actual_command, actual_args =
        command.build_discover(nil, "/tmp/discover_results_example.json")

      assert.equals("./gradlew", actual_command)
      assert.are.same({
        "-I",
        init_script_path,
        "kotlinTestDiscover",
        "-PoutputFile=/tmp/discover_results_example.json",
        "--parallel",
      }, actual_args)
    end)

    it("file", function()
      local actual_command, actual_args =
        command.build_discover("file", "/tmp/discover_results_example.json")

      assert.equals("./gradlew", actual_command)
      assert.are.same({
        "-I",
        init_script_path,
        "kotlinTestDiscover",
        "-PoutputFile=/tmp/discover_results_example.json",
        "--parallel",
        "-Pfile=file",
      }, actual_args)
    end)
  end)

  describe("build_execute", function()
    it("no filter", function()
      local actual = command.build_execute(
        "An example namespace",
        nil,
        "/tmp/results_example.json"
      )

      assert.equals(
        string.format(
          "./gradlew -I %s kotlinTestExecute -Pclasses='An example namespace' -PoutputFile='/tmp/results_example.json'",
          init_script_path
        ),
        actual
      )
    end)

    it("filter", function()
      local actual = command.build_execute(
        "An example namespace",
        "org.example.TestExample::pass",
        "/tmp/results_example.json"
      )

      assert.equals(
        string.format(
          "./gradlew -I %s kotlinTestExecute -Pclasses='An example namespace' -PoutputFile='/tmp/results_example.json' -Pfilter='org.example.TestExample::pass'",
          init_script_path
        ),
        actual
      )
    end)
  end)
end)

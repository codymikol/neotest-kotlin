local command = require("neotest-kotlin.command")

describe("command", function()
  local init_script_path = vim.fn.fnamemodify(
    vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1],
    ":p"
  )

  describe("build_discover", function()
    it("no file", function()
      assert.error(function()
        command.build_discover(nil)
      end, "file must be non-nil and a relative path")
    end)

    it("file", function()
      local actual_command, actual_args = command.build_discover("file")

      assert.equals("./gradlew", actual_command)
      assert.are.same({
        "-I",
        init_script_path,
        "--configuration-cache",
        "kotlinTestDiscover_file",
      }, actual_args)
    end)

    it("complex path", function()
      local actual_command, actual_args =
        command.build_discover("path/to/a/file.kt")

      assert.equals("./gradlew", actual_command)
      assert.are.same({
        "-I",
        init_script_path,
        "--configuration-cache",
        "kotlinTestDiscover_path_to_a_file",
      }, actual_args)
    end)

    it("absolute path", function()
      assert.error(function()
        command.build_discover("/absolute/path/to/a/file.kt")
      end, "file must be non-nil and a relative path")
    end)
  end)

  describe("build_execute", function()
    it("KOTEST_PROPERTIES_FILENAME environment variable set", function()
      vim.env.KOTEST_PROPERTIES_FILENAME = "example.properties"

      local actual = command.build_execute(
        "An example namespace",
        nil,
        "/tmp/results_example.json"
      )

      assert.equals(
        string.format(
          "./gradlew -I %s kotlinTestExecute -Pclasses='An example namespace' -PoutputFile='/tmp/results_example.json' -Dkotest.properties.filename='example.properties'",
          init_script_path
        ),
        actual
      )

      -- reset to nil to not pollute other tests
      vim.env.KOTEST_PROPERTIES_FILENAME = nil
    end)

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

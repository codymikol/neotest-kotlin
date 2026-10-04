local command = require("neotest-kotlin.command")

describe("command", function()
  local init_script_path = vim.fn.fnamemodify(
    vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1],
    ":p"
  )

  describe("build_discover", function()
    it("no file", function()
      assert.error(function()
        command.build_discover(":kotlinTestDiscover", nil, "/tmp/out.json")
      end, "file must be non-nil and an absolute path")
    end)

    it("relative path", function()
      assert.error(function()
        command.build_discover(
          ":kotlinTestDiscover",
          "path/to/a/file.kt",
          "/tmp/out.json"
        )
      end, "file must be non-nil and an absolute path")
    end)

    it("file", function()
      local actual_command, actual_args = command.build_discover(
        ":lib:kotlinTestDiscover",
        "/project/lib/src/test/kotlin/Spec.kt",
        "/project/build/kotlinTestDiscover/lib/src/test/kotlin/Spec.kt.json"
      )

      assert.equals("./gradlew", actual_command)
      assert.are.same({
        "-I",
        init_script_path,
        "--configuration-cache",
        ":lib:kotlinTestDiscover",
        "-DkotlinTestDiscoverFile=/project/lib/src/test/kotlin/Spec.kt",
        "-DkotlinTestDiscoverOutput=/project/build/kotlinTestDiscover/lib/src/test/kotlin/Spec.kt.json",
      }, actual_args)
    end)
  end)

  describe("build_find_tests", function()
    it("runs the task with the configuration cache", function()
      local actual_command, actual_args = command.build_find_tests()

      assert.equals("./gradlew", actual_command)
      assert.are.same({
        "-I",
        init_script_path,
        "--configuration-cache",
        "kotlinTestFindTests",
      }, actual_args)
    end)
  end)

  describe("build_execute", function()
    it("KOTEST_PROPERTIES_FILENAME environment variable set", function()
      vim.env.KOTEST_PROPERTIES_FILENAME = "example.properties"

      local actual = command.build_execute(
        { ":app:kotlinTestExecute" },
        "An example namespace",
        nil,
        "/tmp/results_example"
      )

      assert.equals(
        string.format(
          "./gradlew -I %s :app:kotlinTestExecute -Pclasses='An example namespace' -PoutputDir='/tmp/results_example' -Dkotest.properties.filename='example.properties'",
          init_script_path
        ),
        actual
      )

      -- reset to nil to not pollute other tests
      vim.env.KOTEST_PROPERTIES_FILENAME = nil
    end)

    it("no filter", function()
      local actual = command.build_execute(
        { ":app:kotlinTestExecute" },
        "An example namespace",
        nil,
        "/tmp/results_example"
      )

      assert.equals(
        string.format(
          "./gradlew -I %s :app:kotlinTestExecute -Pclasses='An example namespace' -PoutputDir='/tmp/results_example'",
          init_script_path
        ),
        actual
      )
    end)

    it("filter", function()
      local actual = command.build_execute(
        { ":app:kotlinTestExecute" },
        "An example namespace",
        "org.example.TestExample::pass",
        "/tmp/results_example"
      )

      assert.equals(
        string.format(
          "./gradlew -I %s :app:kotlinTestExecute -Pclasses='An example namespace' -PoutputDir='/tmp/results_example' -Pfilter='org.example.TestExample::pass'",
          init_script_path
        ),
        actual
      )
    end)
  end)

  it("build_execute with several projects", function()
    local actual = command.build_execute(
      { ":app:kotlinTestExecute", ":lib:kotlinTestExecute" },
      "org.example.A,org.example.B",
      nil,
      "/tmp/results_example"
    )

    assert.equals(
      string.format(
        "./gradlew -I %s :app:kotlinTestExecute :lib:kotlinTestExecute -Pclasses='org.example.A,org.example.B' -PoutputDir='/tmp/results_example'",
        init_script_path
      ),
      actual
    )
  end)

  describe("build_classes", function()
    it("single class", function()
      assert.equals("org.example.A", command.build_classes({ "org.example.A" }))
    end)

    it("multiple classes", function()
      assert.equals(
        "org.example.A,org.other.B",
        command.build_classes({ "org.example.A", "org.other.B" })
      )
    end)

    it("falls back to packages when too long", function()
      local original = command.MAX_CLASSES_LENGTH
      command.MAX_CLASSES_LENGTH = 10

      local actual = command.build_classes({
        "org.example.A",
        "org.example.B",
        "org.example.nested.C",
        "org.other.D",
        "org.examples.E",
      })

      command.MAX_CLASSES_LENGTH = original

      assert.equals("org.example.,org.examples.,org.other.", actual)
    end)

    it("falls back to everything for default package classes", function()
      local original = command.MAX_CLASSES_LENGTH
      command.MAX_CLASSES_LENGTH = 1

      local actual = command.build_classes({ "org.example.A", "B" })

      command.MAX_CLASSES_LENGTH = original

      assert.equals("", actual)
    end)
  end)
end)

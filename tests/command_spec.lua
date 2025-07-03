local command = require("neotest-kotlin.command")

describe("command", function()
  it("valid", function()
    local actual = command.build(
      "An example namespace",
      "com.codymikol.gummibear.pizza.FooClass",
      "/tmp/results_example.txt"
    )

    local init_script_path =
      vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1]

    assert.equals(
      string.format(
        "kotest_filter_specs='com.codymikol.gummibear.pizza.FooClass' kotest_filter_tests='An example namespace' ./gradlew -I %s test --console=plain | tee -a /tmp/results_example.txt",
        init_script_path
      ),
      actual
    )
  end)
end)

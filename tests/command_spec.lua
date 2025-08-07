local command = require("neotest-kotlin.command")

describe("command", function()
  it("valid", function()
    local actual =
      command.build("An example namespace", "/tmp/results_example.json")

    local init_script_path =
      vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1]

    assert.equals(
      string.format(
        "./gradlew -I %s kotlinTest -Pclasses=An example namespace -PoutputFile=/tmp/results_example.json",
        init_script_path
      ),
      actual
    )
  end)
end)

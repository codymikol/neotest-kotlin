local command = require("neotest-kotlin.command")

describe("command", function()
  it("no filter", function()
    local actual =
      command.build("An example namespace", nil, "/tmp/results_example.json")

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

  it("filter", function()
    local actual = command.build(
      "An example namespace",
      "org.example.TestExample::pass",
      "/tmp/results_example.json"
    )

    local init_script_path =
      vim.api.nvim_get_runtime_file("test-logging.init.gradle.kts", false)[1]

    assert.equals(
      string.format(
        "./gradlew -I %s kotlinTest -Pclasses=An example namespace -PoutputFile=/tmp/results_example.json -Pfilter=org.example.TestExample::pass",
        init_script_path
      ),
      actual
    )
  end)
end)

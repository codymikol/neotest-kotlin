describe("command", function()
  local command = require("neotest-kotlin.src.command")

  describe("building a gradle command", function()
    it("should correctly build the gradle command to run a spec for a given file", function()
      local expected =
      "export kotest_filter_tests='An example namespace'; export kotest_filter_specs='com.codymikol.gummibear.pizza.FooClass'; ./gradlew cleanTest test --debug --console=plain | tee -a /tmp/results_example.txt"
      result = command.parse("An example namespace", "com.codymikol.gummibear.pizza.FooClass", "/tmp/results_example.txt")
      assert.equals(expected, result)
    end)
  end)
end)

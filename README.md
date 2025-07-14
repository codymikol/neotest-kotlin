# neotest-kotlin

This is an adapter for the neotest project. This gives you the ability to see and run your tests within neovim.

```lua
-- lazy.nvim setup
{
  "nvim-neotest/neotest",
  dependencies = {
    -- ...
    "codymikol/neotest-kotlin"
  },
  config = function()
    require("neotest").setup({
      adapters = {
        require("neotest-kotlin")
      }
    })
  end
}
```

Feel free to ask questions in our [neotest discord](https://discord.gg/yYtyuQ9am7) or open issues on this repository.
If you'd like to help hack on this, please read the [contributing guide](./.github/CONTRIBUTING.md) 

This is currently in development, here is a roadmap of planned support for this plugin and the current status

### Build Tooling

- [x] Gradle
- [ ] Maven

### Test Frameworks

- [x] Kotest - DescribeSpec
- [x] Kotest - FunSpec
- [x] Kotest - AnnotationSpec
- [ ] Kotest - BehaviorSpec
- [x] Kotest - FreeSpec
- [x] Kotest - StringSpec
- [x] Kotest - WordSpec
- [x] Kotest - ShouldSpec
- [x] Kotest - ExpectSpec
- [x] Kotest - FeatureSpec
- [ ] JUnit
- [ ] kotlin.test

### Features

- [x] Display available test results
- [x] Run tests
- [x] Report result status
- [x] Report failure output

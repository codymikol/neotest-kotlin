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
- [ ] Kotest - WordSpec
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

### Contributing

PRs and issues are always welcome, if you have any questions or need help, feel free to open a new discussion on this project.

# neotest-kotlin

This is an adapter for the neotest project. This gives you the ability to see and run your tests within neovim.

```lua
require("neotest").setup({
  adapters = { 
    require("codymikol/neotest-kotlin"), 
  }
})
```
```

This is currently in development, here is a roadmap of planned support for this plugin and the current status

### Build Tooling

- [x] Gradle
- [ ] Maven

### Test Frameworks

- [x] Kotest - DescribeSpec
- [ ] Kotest - FunSpec
- [ ] Kotest - AnnotationSpec
- [ ] Kotest - BehaviorSpec
- [ ] Kotest - FreeSpec
- [ ] Kotest - StringSpec
- [ ] Kotest - WordSpec
- [ ] Kotest - ShouldSpec
- [ ] Kotest - ExpectSpec
- [ ] Kotest - FeatureSpec
- [ ] JUnit 
- [ ] kotlin.test

### Features

- [x] Display available test results
- [x] Run tests
- [x] Report result status
- [ ] Report failure output

### Contributing

PRs and issues are always welcome, if you have any questions or need help, feel free to open a new discussion on this project.

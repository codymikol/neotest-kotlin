# neotest-kotlin

<img src="./assets/icon-neotest-kotlin.svg" width="100" height="100" />

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
- [x] Kotest - BehaviorSpec
- [x] Kotest - FreeSpec
- [x] Kotest - StringSpec
- [x] Kotest - WordSpec
- [x] Kotest - ShouldSpec
- [x] Kotest - ExpectSpec
- [x] Kotest - FeatureSpec
- [x] JUnit
- [x] kotlin.test

### Features

- [x] Display available test results
- [x] Run tests
- [x] Report result status
- [x] Report failure output

### Compatibility

Tests are run with your project's own test runtime classpath, so the Kotest, JUnit and Kotlin standard
library versions are the ones your project declares. neotest-kotlin only appends a small runner jar
(and `junit-platform-launcher`, aligned with your JUnit Platform version, if your project lacks it).

| Requirement | Supported                                                                                               |
| ----------- | ------------------------------------------------------------------------------------------------------- |
| Kotest      | 6.1.0 or newer (CI: 6.1.0 and 6.2.5). Kotest 5.x and 6.0.x fail with an error asking to upgrade          |
| JUnit       | 5.10 or newer, including JUnit 6 (CI: 5.13.4 and 6.1.3). Kotest 6.1 itself requires JUnit Platform 1.13 |
| Kotlin      | 2.1 or newer (Kotest 6.1 requires 2.2). Discovery parses with Kotlin 2.2.21, newer syntax may not parse |
| Gradle      | 9.0 or newer (CI: 9.0.0 and the latest release)                                                         |
| JDK         | 21 or newer for Gradle itself; tests run on your project's JDK, 11 or newer (17 or newer for JUnit 6)   |

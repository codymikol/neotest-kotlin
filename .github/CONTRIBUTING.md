# Contributing

PRs and issues are always welcome, if you have any questions or need help, feel free to open a new discussion on this project.

## Common Commands

### Publishing kotlin-test locally

This publishes the kotlin-test plugin locally for easier usage.

```sh
make publish-kotlin-test-locally
```

For easier development, using [fswatch](https://github.com/emcrisostomo/fswatch) you can re-run publishing

```sh
make watch-kotlin-test
```

### Running Tests

This will create a `.tests` directory that contains all the plugin dependencies of neotest-kotlin.

```shell
make test
```

### Running formatter

This requires that [StyLua](https://github.com/JohnnyMorganz/StyLua) is installed.

```shell
make format
```

## Modifying/Adding Test Detection

Tests are discovered by [kotlin-test](../kotlin-test) using the Kotlin Analysis API, not by Neovim.
Each supported framework has a discoverer in
[`kotlin-test/core/.../discover`](../kotlin-test/core/src/main/kotlin/io/github/codymikol/kotlintest/discover),
registered in [`TestDiscoverer`](../kotlin-test/core/src/main/kotlin/io/github/codymikol/kotlintest/discover/TestDiscoverer.kt).

To add or change detection for a test format

1. Create a new test in [example_project](../tests/example_project/) for your format
2. Add or update the discoverer under `kotlin-test/core/src/main/kotlin/io/github/codymikol/kotlintest/discover`
3. Add or update its tests under [`kotlin-test/core/src/test`](../kotlin-test/core/src/test/kotlin/io/github/codymikol/kotlintest)
4. Publish it locally with `make publish-kotlin-test-locally` and update the Lua tests in [tests](../tests) if the discovered tree changes

The Lua adapter only consumes the discovered tree: every top-level namespace id is `<file path>::<fully qualified class name>`,
which is used to build the `-Pclasses` argument for running files and directories.

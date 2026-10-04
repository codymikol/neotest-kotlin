local neotest_kotlin = require("neotest-kotlin")
local nio = require("nio")
local types = require("neotest.types")

describe("neotest-kotlin", function()
  it("name", function()
    assert.equals("neotest-kotlin", neotest_kotlin.name)
  end)

  local example_project_path = vim.fs.joinpath(
    debug.getinfo(1).source:match("@?(.*/)"),
    "example_project",
    "app",
    "src",
    "test",
    "kotlin",
    "org",
    "example"
  )

  ---@param type neotest.PositionType
  ---@param path string
  ---@param id string? id relative to the path, nil for files and dirs
  ---@return neotest.Position
  local function position(type, path, id)
    return {
      name = id or vim.fs.basename(path),
      id = id and (path .. "::" .. id) or path,
      path = path,
      type = type,
      range = { 0, 0, 0, 0 },
    }
  end

  ---@param list any[]
  ---@return neotest.Tree
  local function to_tree(list)
    return types.Tree.from_list(list, function(pos)
      return pos.id
    end)
  end

  local example_project_root =
    vim.fs.joinpath(debug.getinfo(1).source:match("@?(.*/)"), "example_project")

  describe("is_test_file", function()
    it("not .kt file", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "app",
        "build.gradle.kts"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    it("binary file", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "gradlew"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    it("TOML file", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "gradle",
        "libs.versions.toml"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)
    it("/src/main", function()
      local test_path = vim.fs.joinpath(
        debug.getinfo(1).source:match("@?(.*/)"),
        "example_project",
        "app",
        "src",
        "main",
        "kotlin",
        "org",
        "example",
        "App.kt"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    describe("Kotest", function() end)
    nio.tests.it("FunSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("DescribeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("FreeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFreeSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("ExpectSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestExpectSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    -- the example project has no BehaviorSpec
    nio.tests.it("FeatureSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFeatureSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("WordSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestWordSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("AnnotationSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestAnnotationSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("StringSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestStringSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("ShouldSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestShouldSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("custom super class", function()
      local test_path = vim.fs.joinpath(example_project_path, "SubclassSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("custom super class from another Gradle project", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "ModuleSubclassSpec.kt")

      assert.is_true(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("abstract base spec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "ParentKotestFunSpec.kt")

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("helper file", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "support", "TestHelpers.kt")

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("test fixture", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "support", "PersonFixture.kt")

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("ProjectConfig", function()
      local test_path = vim.fs.joinpath(
        example_project_root,
        "app",
        "src",
        "test",
        "kotlin",
        "io",
        "kotest",
        "provided",
        "ProjectConfig.kt"
      )

      assert.is_false(neotest_kotlin.is_test_file(test_path))
    end)

    nio.tests.it("runs gradle once for all files", function()
      local test_files = require("neotest-kotlin.test_files")
      test_files.reset()

      local run = test_files.run
      local runs = 0
      test_files.run = function(root)
        runs = runs + 1
        return run(root)
      end

      local ok, err = pcall(function()
        assert.is_true(
          neotest_kotlin.is_test_file(
            vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")
          )
        )
        assert.is_true(
          neotest_kotlin.is_test_file(
            vim.fs.joinpath(
              example_project_path,
              "mixed",
              "MixedPackageSpec.kt"
            )
          )
        )
        assert.is_false(
          neotest_kotlin.is_test_file(
            vim.fs.joinpath(example_project_path, "support", "TestHelpers.kt")
          )
        )
      end)
      test_files.run = run
      assert(ok, err)

      assert.equals(1, runs)
    end)
  end)

  describe("build_spec", function()
    nio.tests.it("no tree", function()
      assert.are_nil(neotest_kotlin.build_spec({}))
    end)

    nio.tests.it("dir", function()
      local fun_spec = vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")
      local string_spec =
        vim.fs.joinpath(example_project_path, "KotestStringSpec.kt")

      local tree = to_tree({
        position("dir", example_project_path),
        {
          position("file", fun_spec),
          {
            position("namespace", fun_spec, "org.example.KotestFunSpec"),
            {
              position(
                "namespace",
                fun_spec,
                "org.example.KotestFunSpec::namespace"
              ),
              {
                position(
                  "test",
                  fun_spec,
                  "org.example.KotestFunSpec::namespace::pass"
                ),
              },
            },
          },
        },
        {
          position("file", string_spec),
          {
            position("namespace", string_spec, "org.example.KotestStringSpec"),
            {
              position(
                "test",
                string_spec,
                "org.example.KotestStringSpec::pass"
              ),
            },
          },
        },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(example_project_path, spec.context.path)

      assert.equals(spec.cwd, example_project_root)

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec,org%.example%.KotestStringSpec' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("dir with mixed packages", function()
      local mixed_path = vim.fs.joinpath(example_project_path, "mixed")
      local mixed_spec = vim.fs.joinpath(mixed_path, "MixedPackageSpec.kt")
      local other_spec = vim.fs.joinpath(mixed_path, "OtherPackageSpec.kt")

      local tree = to_tree({
        position("dir", mixed_path),
        {
          position("file", mixed_spec),
          {
            position(
              "namespace",
              mixed_spec,
              "org.example.mixed.MixedPackageSpec"
            ),
          },
        },
        {
          position("file", other_spec),
          {
            position("namespace", other_spec, "org.other.OtherPackageSpec"),
          },
        },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)
      assert.equals(mixed_path, spec.context.path)
      assert.equals(spec.cwd, example_project_root)

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute %-Pclasses='org%.example%.mixed%.MixedPackageSpec,org%.other%.OtherPackageSpec' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("dir without discovered classes", function()
      local tree = to_tree({ position("dir", example_project_path) })

      assert.are_nil(neotest_kotlin.build_spec({ tree = tree }))
    end)

    nio.tests.it("file", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      local tree = to_tree({
        position("file", test_path),
        {
          position("namespace", test_path, "org.example.KotestFunSpec"),
          {
            position(
              "test",
              test_path,
              "org.example.KotestFunSpec::namespace::pass"
            ),
          },
        },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(test_path, spec.context.path)

      assert.equals(spec.cwd, example_project_root)

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("file with multiple classes", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "MultipleClassesSpec.kt")

      local tree = to_tree({
        position("file", test_path),
        {
          position(
            "namespace",
            test_path,
            "org.example.MultipleClassesFirstSpec"
          ),
          {
            position(
              "test",
              test_path,
              "org.example.MultipleClassesFirstSpec::pass"
            ),
          },
        },
        {
          position(
            "namespace",
            test_path,
            "org.example.MultipleClassesSecondSpec"
          ),
          {
            position(
              "test",
              test_path,
              "org.example.MultipleClassesSecondSpec::pass"
            ),
          },
        },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)
      assert.equals(test_path, spec.context.path)

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute %-Pclasses='org%.example%.MultipleClassesFirstSpec,org%.example%.MultipleClassesSecondSpec' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("file without discovered classes", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")
      local tree = to_tree({ position("file", test_path) })

      assert.are_nil(neotest_kotlin.build_spec({ tree = tree }))
    end)

    nio.tests.it("namespace", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type types.Tree
      local tree = types.Tree.from_list(
        {
          {
            name = "namespace",
            id = test_path
              .. "::"
              .. "org.example.KotestFunSpec"
              .. "::"
              .. "namespace",
            path = test_path,
            type = "namespace",
            range = {
              6,
              4,
              24,
              4,
            },
          },
        },
        ---@param types.Tree
        ---@return string
        function(node)
          return node
        end
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(test_path, spec.context.path)

      assert.equals(
        spec.cwd,
        vim.fs.joinpath(
          debug.getinfo(1).source:match("@?(.*/)"),
          "example_project"
        )
      )

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec' %-PoutputDir='[^']*' %-Pfilter='org%.example%.KotestFunSpec::namespace'$",
        spec.command
      )
    end)

    nio.tests.it("test", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type types.Tree
      local tree = types.Tree.from_list(
        {
          {
            name = "namespace",
            id = test_path
              .. "::"
              .. "org.example.KotestFunSpec"
              .. "::"
              .. "namespace"
              .. "::"
              .. "pass",
            path = test_path,
            type = "test",
            range = {
              7,
              8,
              9,
              8,
            },
          },
        },
        ---@param types.Tree
        ---@return string
        function(node)
          return node
        end
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)

      assert.not_nil(spec.context.results_path)
      assert.are_string(spec.context.results_path)

      assert.not_nil(spec.context.path)
      assert.equals(test_path, spec.context.path)

      assert.equals(
        spec.cwd,
        vim.fs.joinpath(
          debug.getinfo(1).source:match("@?(.*/)"),
          "example_project"
        )
      )

      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute %-Pclasses='org%.example%.KotestFunSpec' %-PoutputDir='[^']*' %-Pfilter='org%.example%.KotestFunSpec::namespace::pass'$",
        spec.command
      )
    end)
  end)

  describe("build_spec with multiple Gradle projects", function()
    local app_spec = vim.fs.joinpath(example_project_path, "SharedNameSpec.kt")
    local lib_spec = vim.fs.joinpath(
      example_project_root,
      "lib",
      "src",
      "test",
      "kotlin",
      "org",
      "example",
      "SharedNameSpec.kt"
    )
    local lib_junit =
      vim.fs.joinpath(vim.fs.dirname(lib_spec), "LibJUnitTest.kt")

    nio.tests.it("file of a subproject", function()
      local tree = to_tree({
        position("file", lib_junit),
        {
          position("namespace", lib_junit, "org.example.LibJUnitTest"),
        },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert(spec ~= nil)

      assert.equals(example_project_root, spec.cwd)
      assert.are.same({ ":lib" }, spec.context.projects)
      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :lib:kotlinTestExecute %-Pclasses='org%.example%.LibJUnitTest' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("test of a subproject", function()
      local tree = to_tree({
        position("test", lib_spec, "org.example.SharedNameSpec::from lib"),
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert(spec ~= nil)

      assert.are.same({ ":lib" }, spec.context.projects)
      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :lib:kotlinTestExecute %-Pclasses='org%.example%.SharedNameSpec' %-PoutputDir='[^']*' %-Pfilter='org%.example%.SharedNameSpec::from lib'$",
        spec.command
      )
    end)

    nio.tests.it("dir spanning several projects", function()
      local tree = to_tree({
        position("dir", example_project_root),
        {
          position("file", app_spec),
          { position("namespace", app_spec, "org.example.SharedNameSpec") },
        },
        {
          position("file", lib_spec),
          { position("namespace", lib_spec, "org.example.SharedNameSpec") },
        },
        {
          position("file", lib_junit),
          { position("namespace", lib_junit, "org.example.LibJUnitTest") },
        },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert(spec ~= nil)

      assert.are.same({ ":app", ":lib" }, spec.context.projects)
      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts :app:kotlinTestExecute :lib:kotlinTestExecute %-Pclasses='org%.example%.SharedNameSpec,org%.example%.LibJUnitTest' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("file of the root project runs every project", function()
      local root_spec =
        vim.fs.joinpath(example_project_root, "src", "test", "RootSpec.kt")
      local tree = to_tree({
        position("file", root_spec),
        { position("namespace", root_spec, "org.example.RootSpec") },
      })

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert(spec ~= nil)

      assert.is_nil(spec.context.projects)
      assert.matches(
        "^%./gradlew %-I /.*/test%-logging%.init%.gradle%.kts kotlinTestExecute %-Pclasses='org%.example%.RootSpec' %-PoutputDir='[^']*'$",
        spec.command
      )
    end)

    nio.tests.it("results of each project apply to its own files", function()
      local results_path = nio.fn.tempname()
      vim.uv.fs_mkdir(results_path, 493)

      ---@param name string
      ---@param test string
      local function write(name, test)
        local file = nio.file.open(vim.fs.joinpath(results_path, name), "w+")
        file.write(string.format(
          [[
          [
            {
              "className": "org.example.SharedNameSpec",
              "id": "%s",
              "duration": 1,
              "status": { "type": "SUCCESS" }
            }
          ]
          ]],
          test
        ))
        file.close()
      end

      write("_app.json", "from app")
      write("_lib.json", "from lib")

      local tree = to_tree({
        position("dir", example_project_root),
        {
          position("file", app_spec),
          { position("namespace", app_spec, "org.example.SharedNameSpec") },
        },
        {
          position("file", lib_spec),
          { position("namespace", lib_spec, "org.example.SharedNameSpec") },
        },
      })

      local spec = {
        cwd = example_project_root,
        context = {
          path = example_project_root,
          results_path = results_path,
          projects = { ":app", ":lib" },
        },
      }

      assert.are.same({
        [app_spec .. "::org.example.SharedNameSpec::from app"] = {
          status = "passed",
        },
        [lib_spec .. "::org.example.SharedNameSpec::from lib"] = {
          status = "passed",
        },
      }, neotest_kotlin.results(spec, nil, tree))
    end)

    nio.tests.it("results without output files", function()
      local tree = to_tree({
        position("file", lib_spec),
        { position("namespace", lib_spec, "org.example.SharedNameSpec") },
      })

      local spec = {
        cwd = example_project_root,
        context = {
          path = lib_spec,
          results_path = nio.fn.tempname(),
          projects = { ":lib" },
        },
      }

      assert.are.same({}, neotest_kotlin.results(spec, nil, tree))
    end)
  end)

  describe("results", function()
    nio.tests.it("single test - passed", function()
      local json = [[
      [
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::pass",
          "duration": 18516838,
          "status": { "type": "SUCCESS" }
        }
      ]
      ]]

      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type string
      local results_path = nio.fn.tempname()
      vim.uv.fs_mkdir(results_path, 493)
      local file =
        nio.file.open(vim.fs.joinpath(results_path, "_app.json"), "w+")
      file.write(json)

      local spec = {
        context = {
          path = test_path,
          results_path = results_path,
        },
      }

      local tree = to_tree({
        position("file", test_path),
        { position("namespace", test_path, "org.example.KotestFunSpec") },
      })

      local actual = neotest_kotlin.results(spec, nil, tree)
      assert.are.same({
        [test_path .. "::org.example.KotestFunSpec::namespace::pass"] = {
          status = "passed",
        },
      }, actual)

      file.close()
    end)

    nio.tests.it("single test - skipped", function()
      local json = [[
      [
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::pass",
          "duration": 0,
          "status": {
            "reason": "org.example.KotestFunSpec/namespace -- pass is excluded by filter(s)",
            "type": "IGNORED"
          }
        }
      ]
      ]]

      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type string
      local results_path = nio.fn.tempname()
      vim.uv.fs_mkdir(results_path, 493)
      local file =
        nio.file.open(vim.fs.joinpath(results_path, "_app.json"), "w+")
      file.write(json)

      local spec = {
        context = {
          path = test_path,
          results_path = results_path,
        },
      }

      local tree = to_tree({
        position("file", test_path),
        { position("namespace", test_path, "org.example.KotestFunSpec") },
      })

      local actual = neotest_kotlin.results(spec, nil, tree)
      assert.are.same({
        [test_path .. "::org.example.KotestFunSpec::namespace::pass"] = {
          status = "skipped",
        },
      }, actual)

      file.close()
    end)

    nio.tests.it("single test - failed", function()
      local json = [[
      [
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::fail",
          "duration": 89925270,
          "status": {
            "stackTrace": "io.kotest.assertions.AssertionFailedError: expected:<\"b\"> but was:<\"a\">\n\tat org.example.KotestFunSpec$1$1$1.invokeSuspend(KotestFunSpec.kt:9)",
            "error": {
              "message": "expected:<\"b\"> but was:<\"a\">",
              "lineNumber": 9,
              "filename": "KotestFunSpec.kt"
            },
            "type": "FAILURE"
          }
        }
      ]
      ]]

      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type string
      local results_path = nio.fn.tempname()
      vim.uv.fs_mkdir(results_path, 493)
      local file =
        nio.file.open(vim.fs.joinpath(results_path, "_app.json"), "w+")
      file.write(json)

      local spec = {
        context = {
          path = test_path,
          results_path = results_path,
        },
      }

      local tree = to_tree({
        position("file", test_path),
        { position("namespace", test_path, "org.example.KotestFunSpec") },
      })

      local actual = neotest_kotlin.results(spec, nil, tree)
      assert.not_nil(
        actual[test_path .. "::org.example.KotestFunSpec::namespace::fail"].output
      )

      -- set to nil for full assertion below
      actual[test_path .. "::org.example.KotestFunSpec::namespace::fail"].output =
        nil

      assert.are.same({
        [test_path .. "::org.example.KotestFunSpec::namespace::fail"] = {
          status = "failed",
          short = 'expected:<"b"> but was:<"a">',
          errors = {
            {
              message = 'expected:<"b"> but was:<"a">',
              line = 8,
            },
          },
        },
      }, actual)

      file.close()
    end)

    nio.tests.it("multiple tests", function()
      local json = [[
      [
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::fail",
          "duration": 101020666,
          "status": {
            "stackTrace": "io.kotest.assertions.AssertionFailedError: expected:<\"b\"> but was:<\"a\">\n\tat org.example.KotestFunSpec$1$1$1.invokeSuspend(KotestFunSpec.kt:9)\n\t",
            "error": {
              "message": "expected:<\"b\"> but was:<\"a\">",
              "lineNumber": 9,
              "filename": "KotestFunSpec.kt"
            },
            "type": "FAILURE"
          }
        },
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::pass",
          "duration": 5608480,
          "status": { "type": "SUCCESS" }
        },
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::nested namespace::pass",
          "duration": 4568264,
          "status": { "type": "SUCCESS" }
        },
        {
          "className": "org.example.KotestFunSpec",
          "id": "namespace::nested namespace::fail",
          "duration": 6377086,
          "status": {
            "stackTrace": "io.kotest.assertions.AssertionFailedError: expected:<\"b\"> but was:<\"a\">\n\tat org.example.KotestFunSpec$1$1$3$2.invokeSuspend(KotestFunSpec.kt:22)\n\t",
            "error": {
              "message": "expected:<\"b\"> but was:<\"a\">",
              "lineNumber": 22,
              "filename": "KotestFunSpec.kt"
            },
            "type": "FAILURE"
          }
        }
      ]
      ]]

      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type string
      local results_path = nio.fn.tempname()
      vim.uv.fs_mkdir(results_path, 493)
      local file =
        nio.file.open(vim.fs.joinpath(results_path, "_app.json"), "w+")
      file.write(json)

      local spec = {
        context = {
          path = test_path,
          results_path = results_path,
        },
      }

      local tree = to_tree({
        position("file", test_path),
        { position("namespace", test_path, "org.example.KotestFunSpec") },
      })

      local actual = neotest_kotlin.results(spec, nil, tree)
      assert.not_nil(
        actual[test_path .. "::org.example.KotestFunSpec::namespace::fail"].output
      )

      -- set to nil for full assertion below
      actual[test_path .. "::org.example.KotestFunSpec::namespace::fail"].output =
        nil

      assert.not_nil(
        actual[test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::fail"].output
      )

      actual[test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::fail"].output =
        nil

      assert.are.same({
        [test_path .. "::org.example.KotestFunSpec::namespace::fail"] = {
          status = "failed",
          short = 'expected:<"b"> but was:<"a">',
          errors = {
            {
              message = 'expected:<"b"> but was:<"a">',
              line = 8,
            },
          },
        },
        [test_path .. "::org.example.KotestFunSpec::namespace::pass"] = {
          status = "passed",
        },
        [test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::pass"] = {
          status = "passed",
        },
        [test_path .. "::org.example.KotestFunSpec::namespace::nested namespace::fail"] = {
          status = "failed",
          short = 'expected:<"b"> but was:<"a">',
          errors = {
            {
              message = 'expected:<"b"> but was:<"a">',
              line = 21,
            },
          },
        },
      }, actual)

      file.close()
    end)
    nio.tests.it("dir with mixed packages", function()
      local json = [[
      [
        {
          "className": "org.example.mixed.MixedPackageSpec",
          "id": "pass",
          "duration": 1,
          "status": { "type": "SUCCESS" }
        },
        {
          "className": "org.other.OtherPackageSpec",
          "id": "pass",
          "duration": 1,
          "status": { "type": "SUCCESS" }
        },
        {
          "className": "org.other.NotInTree",
          "id": "pass",
          "duration": 1,
          "status": { "type": "SUCCESS" }
        }
      ]
      ]]

      local mixed_path = vim.fs.joinpath(example_project_path, "mixed")
      local mixed_spec = vim.fs.joinpath(mixed_path, "MixedPackageSpec.kt")
      local other_spec = vim.fs.joinpath(mixed_path, "OtherPackageSpec.kt")

      ---@type string
      local results_path = nio.fn.tempname()
      vim.uv.fs_mkdir(results_path, 493)
      local file =
        nio.file.open(vim.fs.joinpath(results_path, "_app.json"), "w+")
      file.write(json)

      local spec = {
        context = {
          path = mixed_path,
          results_path = results_path,
        },
      }

      local tree = to_tree({
        position("dir", mixed_path),
        {
          position("file", mixed_spec),
          {
            position(
              "namespace",
              mixed_spec,
              "org.example.mixed.MixedPackageSpec"
            ),
          },
        },
        {
          position("file", other_spec),
          {
            position("namespace", other_spec, "org.other.OtherPackageSpec"),
          },
        },
      })

      local actual = neotest_kotlin.results(spec, nil, tree)
      assert.are.same({
        [mixed_spec .. "::org.example.mixed.MixedPackageSpec::pass"] = {
          status = "passed",
        },
        [other_spec .. "::org.other.OtherPackageSpec::pass"] = {
          status = "passed",
        },
      }, actual)

      file.close()
    end)
  end)

  describe("discover_positions", function()
    nio.tests.it("concurrently, with a single Gradle build", function()
      local gradle = require("neotest-kotlin.gradle")
      local original_spawn = gradle.spawn
      -- not determined by a previous test
      require("neotest-kotlin.test_files").reset()

      local running, max_running, builds = 0, 0, 0
      gradle.spawn = function(opts)
        running = running + 1
        builds = builds + 1
        max_running = math.max(max_running, running)
        local ok, status_code, stdout, stderr = pcall(original_spawn, opts)
        running = running - 1
        if not ok then
          error(status_code)
        end
        return status_code, stdout, stderr
      end

      -- like neotest's discovery workers, which discover files concurrently
      local files = {
        "KotestFunSpec.kt",
        "KotestDescribeSpec.kt",
        "KotestStringSpec.kt",
        "MultipleClassesSpec.kt",
      }
      local ok, trees = pcall(
        nio.gather,
        vim.tbl_map(function(file)
          return function()
            return neotest_kotlin.discover_positions(
              vim.fs.joinpath(example_project_path, file)
            )
          end
        end, files)
      )
      gradle.spawn = original_spawn
      assert(ok, trees)

      assert.are.same(#files, #trees)
      for index, tree in ipairs(trees) do
        assert.are.same(files[index], tree:data().name)
      end
      -- every file is discovered by the build determining the test files
      assert.are.same(1, builds)
      assert.are.same(1, max_running)
    end)

    nio.tests.it("Multiple Classes", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "MultipleClassesSpec.kt")

      local tree = neotest_kotlin.discover_positions(test_path)
      assert.not_nil(tree)
      assert(tree ~= nil)

      assert.equals("file", tree:data().type)
      assert.equals("MultipleClassesSpec.kt", tree:data().name)
      assert.equals(16, tree:data().range[3])

      local children = vim.tbl_map(function(child)
        return child:data().id
      end, tree:children())

      assert.are.same({
        test_path .. "::org.example.MultipleClassesFirstSpec",
        test_path .. "::org.example.MultipleClassesSecondSpec",
      }, children)

      assert.not_nil(
        tree:get_key(
          test_path .. "::org.example.MultipleClassesSecondSpec::pass"
        )
      )

      local spec = neotest_kotlin.build_spec({ tree = tree })
      assert.not_nil(spec)
      assert(spec ~= nil)
      assert.matches(
        "%-Pclasses='org%.example%.MultipleClassesFirstSpec,org%.example%.MultipleClassesSecondSpec'",
        spec.command
      )
    end)

    nio.tests.it("Custom Subclass", function()
      local test_path = vim.fs.joinpath(example_project_path, "SubclassSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "SubclassSpec.kt",
        range = {
          0,
          0,
          8,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.SubclassSpec",
        path = test_path,
        name = "SubclassSpec",
        range = {
          4,
          0,
          8,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.SubclassSpec"
          .. "::"
          .. "example",
        path = test_path,
        name = "example",
        range = {
          5,
          4,
          7,
          4,
        },
        type = "test",
      }, tree[2][2][1])
    end)

    nio.tests.it("Custom Subclass from another Gradle project", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "ModuleSubclassSpec.kt")
      local spec_id = test_path .. "::" .. "org.example.ModuleSubclassSpec"
      local context_id = spec_id .. "::" .. "from another project"

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "ModuleSubclassSpec.kt",
        range = {
          0,
          0,
          11,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = spec_id,
        path = test_path,
        name = "ModuleSubclassSpec",
        range = {
          5,
          0,
          11,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = context_id,
        path = test_path,
        name = "from another project",
        range = {
          6,
          4,
          10,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = context_id .. "::" .. "example",
        path = test_path,
        name = "example",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])
    end)

    nio.tests.it("Duplicate Test Names", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "DuplicateTestNames.kt")

      local bufnr = vim.fn.bufadd(test_path)

      local tree = neotest_kotlin.discover_positions(test_path)
      assert.not_nil(tree)
      assert(tree ~= nil)

      local class_id = test_path .. "::org.example.DuplicateTestNames"

      ---@type table<string, any>
      local positions = {}
      for _, position in tree:iter() do
        positions[position.id] = position
      end

      -- duplicates are disambiguated so each has a unique id
      assert.is_nil(positions[class_id .. "::pass"])

      local first = positions[class_id .. "::pass#1"]
      assert.not_nil(first)
      assert.are.same("pass#1", first.name)
      assert.are.same({ 7, 8, 9, 8 }, first.range)

      local second = positions[class_id .. "::pass#2"]
      assert.not_nil(second)
      assert.are.same("pass#2", second.name)
      assert.are.same({ 11, 8, 13, 8 }, second.range)

      -- vim.diagnostic can't be used in a fast event context
      nio.scheduler()

      local count = vim.diagnostic.count(bufnr)
      assert.are.same({ [vim.diagnostic.severity.WARN] = 1 }, count)

      local diagnostics = vim.diagnostic.get(bufnr)
      assert.are.same({
        {
          bufnr = bufnr,
          col = 8,
          end_col = 8,
          end_lnum = 13,
          lnum = 11,
          message = "Multiple tests defined with name 'pass'",
          namespace = vim.api.nvim_create_namespace("neotest"),
          severity = 2,
          source = "neotest-kotlin",
        },
      }, diagnostics)
    end)

    nio.tests.it("WordSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestWordSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestWordSpec.kt",
        range = {
          0,
          0,
          40,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestWordSpec",
        path = test_path,
        name = "KotestWordSpec",
        range = {
          5,
          0,
          40,
          4,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace",
        path = test_path,
        name = "When namespace",
        range = {
          7,
          8,
          17,
          8,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace"
          .. "::"
          .. "nested When namespace",
        path = test_path,
        name = "nested When namespace",
        range = {
          8,
          12,
          16,
          12,
        },
        type = "namespace",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace"
          .. "::"
          .. "nested When namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          9,
          16,
          11,
          16,
        },
        type = "test",
      }, tree[2][2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "When namespace"
          .. "::"
          .. "nested When namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          13,
          16,
          15,
          16,
        },
        type = "test",
      }, tree[2][2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace",
        path = test_path,
        name = "`when` namespace",
        range = {
          19,
          8,
          29,
          8,
        },
        type = "namespace",
      }, tree[2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace"
          .. "::"
          .. "nested `when` namespace",
        path = test_path,
        name = "nested `when` namespace",
        range = {
          20,
          12,
          28,
          12,
        },
        type = "namespace",
      }, tree[2][3][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace"
          .. "::"
          .. "nested `when` namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          21,
          16,
          23,
          16,
        },
        type = "test",
      }, tree[2][3][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "`when` namespace"
          .. "::"
          .. "nested `when` namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          25,
          16,
          27,
          16,
        },
        type = "test",
      }, tree[2][3][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          31,
          8,
          39,
          8,
        },
        type = "namespace",
      }, tree[2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          32,
          12,
          34,
          12,
        },
        type = "test",
      }, tree[2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestWordSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          36,
          12,
          38,
          12,
        },
        type = "test",
      }, tree[2][4][3][1])
    end)

    nio.tests.it("StringSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestStringSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestStringSpec.kt",
        range = {
          0,
          0,
          12,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestStringSpec",
        path = test_path,
        name = "KotestStringSpec",
        range = {
          4,
          0,
          12,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestStringSpec"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          5,
          4,
          7,
          4,
        },
        type = "test",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestStringSpec"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          9,
          4,
          11,
          4,
        },
        type = "test",
      }, tree[2][3][1])
    end)

    nio.tests.it("AnnotationSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestAnnotationSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestAnnotationSpec.kt",
        range = {
          0,
          0,
          21,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestAnnotationSpec",
        path = test_path,
        name = "KotestAnnotationSpec",
        range = {
          5,
          0,
          21,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestAnnotationSpec"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          4,
          9,
          4,
        },
        type = "test",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestAnnotationSpec"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          12,
          4,
          14,
          4,
        },
        type = "test",
      }, tree[2][3][1])
    end)

    nio.tests.it("ShouldSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestShouldSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestShouldSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestShouldSpec",
        path = test_path,
        name = "KotestShouldSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestShouldSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("ExpectSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestExpectSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestExpectSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestExpectSpec",
        path = test_path,
        name = "KotestExpectSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestExpectSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("FeatureSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFeatureSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestFeatureSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestFeatureSpec",
        path = test_path,
        name = "KotestFeatureSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFeatureSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("FreeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFreeSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestFreeSpec.kt",
        range = {
          0,
          0,
          24,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestFreeSpec",
        path = test_path,
        name = "KotestFreeSpec",
        range = {
          4,
          0,
          24,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          5,
          4,
          23,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          6,
          8,
          8,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          10,
          8,
          12,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          14,
          8,
          22,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          15,
          12,
          17,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFreeSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          19,
          12,
          21,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("FunSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestFunSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestFunSpec.kt",
        range = {
          0,
          0,
          25,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestFunSpec",
        path = test_path,
        name = "KotestFunSpec",
        range = {
          5,
          0,
          25,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace",
        path = test_path,
        name = "namespace",
        range = {
          6,
          4,
          24,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace",
        path = test_path,
        name = "nested namespace",
        range = {
          15,
          8,
          23,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "pass",
        path = test_path,
        name = "pass",
        range = {
          16,
          12,
          18,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestFunSpec"
          .. "::"
          .. "namespace"
          .. "::"
          .. "nested namespace"
          .. "::"
          .. "fail",
        path = test_path,
        name = "fail",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)

    nio.tests.it("DescribeSpec", function()
      local test_path =
        vim.fs.joinpath(example_project_path, "KotestDescribeSpec.kt")

      ---@type any[]
      local tree = neotest_kotlin.discover_positions(test_path):to_list()

      assert.are.same({
        id = test_path,
        path = test_path,
        name = "KotestDescribeSpec.kt",
        range = {
          0,
          0,
          33,
          0,
        },
        type = "file",
      }, tree[1])

      assert.are.same({
        id = test_path .. "::" .. "org.example.KotestDescribeSpec",
        path = test_path,
        name = "KotestDescribeSpec",
        range = {
          5,
          0,
          33,
          0,
        },
        type = "namespace",
      }, tree[2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestDescribeSpec"
          .. "::"
          .. "a namespace",
        path = test_path,
        name = "a namespace",
        range = {
          6,
          4,
          32,
          4,
        },
        type = "namespace",
      }, tree[2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestDescribeSpec"
          .. "::"
          .. "a namespace"
          .. "::"
          .. "should handle failed assertions",
        path = test_path,
        name = "should handle failed assertions",
        range = {
          7,
          8,
          9,
          8,
        },
        type = "test",
      }, tree[2][2][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestDescribeSpec"
          .. "::"
          .. "a namespace"
          .. "::"
          .. "should handle passed assertions",
        path = test_path,
        name = "should handle passed assertions",
        range = {
          11,
          8,
          13,
          8,
        },
        type = "test",
      }, tree[2][2][3][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestDescribeSpec"
          .. "::"
          .. "a namespace"
          .. "::"
          .. "a nested namespace",
        path = test_path,
        name = "a nested namespace",
        range = {
          19,
          8,
          31,
          8,
        },
        type = "namespace",
      }, tree[2][2][4][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestDescribeSpec"
          .. "::"
          .. "a namespace"
          .. "::"
          .. "a nested namespace"
          .. "::"
          .. "should handle failed assertions",
        path = test_path,
        name = "should handle failed assertions",
        range = {
          20,
          12,
          22,
          12,
        },
        type = "test",
      }, tree[2][2][4][2][1])

      assert.are.same({
        id = test_path
          .. "::"
          .. "org.example.KotestDescribeSpec"
          .. "::"
          .. "a namespace"
          .. "::"
          .. "a nested namespace"
          .. "::"
          .. "should handle passed assertions",
        path = test_path,
        name = "should handle passed assertions",
        range = {
          24,
          12,
          26,
          12,
        },
        type = "test",
      }, tree[2][2][4][3][1])
    end)
  end)
end)

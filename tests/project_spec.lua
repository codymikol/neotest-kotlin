local project = require("neotest-kotlin.project")

describe("project", function()
  local example_project_root =
    vim.fs.joinpath(debug.getinfo(1).source:match("@?(.*/)"), "example_project")

  before_each(function()
    project.reset()
  end)

  describe("find", function()
    it("file of a subproject", function()
      assert.equals(
        ":app",
        project.find(
          example_project_root,
          vim.fs.joinpath(
            example_project_root,
            "app/src/test/kotlin/org/example/KotestFunSpec.kt"
          )
        )
      )
      assert.equals(
        ":lib",
        project.find(
          example_project_root,
          vim.fs.joinpath(
            example_project_root,
            "lib/src/test/kotlin/org/example/LibFunSpec.kt"
          )
        )
      )
    end)

    it("directory of a subproject", function()
      assert.equals(
        ":lib",
        project.find(
          example_project_root,
          vim.fs.joinpath(example_project_root, "lib/src/test/kotlin")
        )
      )
      assert.equals(
        ":lib",
        project.find(
          example_project_root,
          vim.fs.joinpath(example_project_root, "lib")
        )
      )
    end)

    it("root directory", function()
      assert.equals(
        ":",
        project.find(example_project_root, example_project_root)
      )
    end)

    it("file outside of subprojects", function()
      assert.equals(
        ":",
        project.find(
          example_project_root,
          vim.fs.joinpath(example_project_root, "src/test/kotlin/RootSpec.kt")
        )
      )
    end)

    it("nested subproject", function()
      local root = vim.fs.normalize(vim.fn.tempname())
      vim.fn.mkdir(vim.fs.joinpath(root, "libs/core/src/test/kotlin"), "p")
      vim.fn.writefile({}, vim.fs.joinpath(root, "libs/core/build.gradle"))

      assert.equals(
        ":libs:core",
        project.find(
          root,
          vim.fs.joinpath(root, "libs/core/src/test/kotlin/Spec.kt")
        )
      )
      -- `libs` has no build file of its own
      assert.equals(
        ":",
        project.find(root, vim.fs.joinpath(root, "libs/Other.kt"))
      )

      vim.fn.delete(root, "rf")
    end)
  end)

  it("task", function()
    assert.equals(":kotlinTestExecute", project.task(":", "kotlinTestExecute"))
    assert.equals(
      ":app:kotlinTestExecute",
      project.task(":app", "kotlinTestExecute")
    )
  end)

  it("file_name", function()
    -- must match `projectFileName` of the Gradle plugin
    assert.equals("_.json", project.file_name(":"))
    assert.equals("_app.json", project.file_name(":app"))
    assert.equals("_app_core.json", project.file_name(":app:core"))
  end)
end)

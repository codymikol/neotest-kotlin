local Discovered = require("neotest-kotlin.output.discovered")
local DiscoveryResult = require("neotest-kotlin.output.discovery_result")
local TestResult = require("neotest-kotlin.output.test_result")
local async = require("neotest.async")
local types = require("neotest.types")

local M = {}

---@param pos neotest.Position
---@return string
local function position_key(pos)
  return pos.id
end

---Splits a discovered position id into its fully qualified class name and the
---id of the position relative to that class.
---
---Discovered ids have the form `<path>::<FQCN>[::<nested>...]` where the
---top-level namespace id is `<path>::<FQCN>`.
---@param pos neotest.Position
---@return string? class fully qualified class name, nil for file/dir positions
---@return string? id the id without the leading `<path>::`
function M.split_position_id(pos)
  if pos.type == "file" or pos.type == "dir" then
    return nil, nil
  end

  local prefix = pos.path .. "::"
  if not vim.startswith(pos.id, prefix) then
    return nil, nil
  end

  local id = pos.id:sub(#prefix + 1)
  return vim.split(id, "::", { plain = true })[1], id
end

---Determines all fully qualified classes discovered in the tree, in tree order,
---along with the file each of them was discovered in.
---@param tree neotest.Tree
---@param include_path (fun(path: string): boolean)? only classes of the files it includes, e.g. of a single Gradle project
---@return string[] classes
---@return table<string, string> class_to_path
function M.discovered_classes(tree, include_path)
  ---@type string[]
  local classes = {}
  ---@type table<string, string>
  local class_to_path = {}
  ---@type table<string, boolean>
  local included_paths = {}

  for _, pos in tree:iter() do
    local class = M.split_position_id(pos)

    if class ~= nil and include_path ~= nil then
      if included_paths[pos.path] == nil then
        included_paths[pos.path] = include_path(pos.path)
      end

      if not included_paths[pos.path] then
        class = nil
      end
    end

    if class ~= nil and class_to_path[class] == nil then
      table.insert(classes, class)
      class_to_path[class] = pos.path
    end
  end

  return classes, class_to_path
end

---Applies class level failures (e.g. an exception in beforeSpec or a test engine error)
---to all tests of that class that have no result of their own, so the error is visible on each
---test that could not run.
---@param results table<string, neotest.Result>
---@param class_ids string[] ids of classes with a class level failure
---@param tree neotest.Tree
local function apply_class_failures(results, class_ids, tree)
  for _, class_id in ipairs(class_ids) do
    local class_result = results[class_id]
    local prefix = class_id .. "::"

    -- only the executed tree, not the entire class, as not all tests may have been requested
    for _, pos in tree:iter() do
      if
        pos.type == "test"
        and results[pos.id] == nil
        and vim.startswith(pos.id, prefix)
      then
        results[pos.id] = {
          status = class_result.status,
          short = class_result.short,
          output = class_result.output,
          -- errors are only shown on the class to avoid duplicate diagnostics
          errors = {},
        }
      end
    end
  end
end

---Converts JSON of TestNodes to neotest.Results
---@param tree neotest.Tree the tree that was executed, also used to apply class level failures to its tests
---@param json_content string
---@param include_path (fun(path: string): boolean)? only results of classes of the files it includes, e.g. of the Gradle project that ran the tests
---@return table<string, neotest.Result>
function M.json_to_results(tree, json_content, include_path)
  ---@type any[]
  local test_results = vim.json.decode(json_content)

  local results = {}
  ---@type string[]
  local failed_class_ids = {}
  local _, class_to_path = M.discovered_classes(tree, include_path)

  for _, result_json in ipairs(test_results) do
    local test_result = TestResult.from(result_json)
    local class_path = class_to_path[test_result.className]

    if class_path ~= nil then
      local id, result = test_result:to_result(class_path)
      results[id] = result

      if test_result:is_class_result() and result.status == "failed" then
        table.insert(failed_class_ids, id)
      end
    end
  end

  apply_class_failures(results, failed_class_ids, tree)

  return results
end

---Converts JSON of DiscoveredTests to neotest.Tree
---
---Each discovered top-level class becomes a direct child of the file node.
---@param json_content string
---@return neotest.Tree? tree nil when no tests were discovered
function M.json_to_tree(json_content)
  ---@type any
  local result_json = vim.json.decode(json_content)
  ---@type DiscoveryResult
  local discovery_result = DiscoveryResult.from(result_json)

  ---@type table<number, vim.Diagnostic[]>
  local bufnr_to_diagnostics = discovery_result:to_diagnostics()

  if next(bufnr_to_diagnostics) ~= nil and vim.in_fast_event() then
    -- vim.diagnostic can't be used in a fast event context (e.g. after nio.process)
    async.scheduler()
  end

  for bufnr, diagnostics in pairs(bufnr_to_diagnostics) do
    local first = diagnostics[1]

    vim.diagnostic.set(first.namespace, bufnr, diagnostics)
  end

  ---@type any[] one nested list per top-level class
  local classes = {}

  for _, test in ipairs(discovery_result.tests) do
    local t = Discovered.from(test)
    table.insert(classes, t:to_trees())
  end

  if #classes == 0 then
    return nil
  end

  local first = classes[1][1]
  local end_line = first.range[3]
  for _, class in ipairs(classes) do
    end_line = math.max(end_line, class[1].range[3])
  end

  local file_tree = {
    name = vim.fs.basename(first.path),
    id = first.path,
    path = first.path,
    type = "file",
    range = {
      0,
      0,
      end_line,
      0,
    },
  }

  return types.Tree.from_list({ file_tree, unpack(classes) }, position_key)
end

return M

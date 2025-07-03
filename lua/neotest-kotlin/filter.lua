local M = {}

local ignored_directories = {
  "docs",
  "build",
  "out",
  "generated",
  ".gradle",
  "main",
  ".idea",
  "buildSrc",
  "kapt",
  "target",
}

---This filters out non-test directories that would bog down scnanning.
---@param path string Name of directory
function M.is_test_directory(path)
  for _, v in ipairs(ignored_directories) do
    if v == path then
      return false
    end
  end

  return true
end

---Whether the file_path provided is a test file.
---@param file_path string?
---@return boolean
function M.is_test_file(file_path)
  if file_path == nil then
    return false
  end

  if not vim.endswith(file_path, ".kt") then
    return false
  end

  if string.find(file_path, "src/main") then
    return false
  end

  return true
end

return M

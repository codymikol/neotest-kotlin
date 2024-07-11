local M = {}

-- the gradle prefix is this long >>> 2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger]
local PREFIX_OFFSET = 54

-- This will get a result type ( NONE, PASSED, SKIPPED, FAILED ) from a gradle output line.
M.get_result_type = function(line, pkg)
  -- This will quickly skip non-TestEventLogger lines most of the time...
  if string.sub(line, PREFIX_OFFSET, PREFIX_OFFSET) ~= "]" then
    return nil
  end

  -- Test to see if the line starting after the prefix is the package name.
  if not string.find(line, pkg, PREFIX_OFFSET + 1) then
    return nil
  end

  if string.find(line, "PASSED", -6) then
    return "passed"
  end

  if string.find(line, "SKIPPED", -7) then
    return "skipped"
  end

  if string.find(line, "FAILED", -6) then
    return "failed"
  end

  return nil
end

-- This will turn a gradle output line into an id that can be looked up by neotest
-- Example Input: '2024-05-19T22:15:04.339-0400 [DEBUG] [TestEventLogger] com.codymikol.state.neotest.NeotestKotestSpec > a namespace > com.codymikol.state.neotest.NeotestKotestSpec.should handle passed assertions PASSED'
-- Example Output: '/home/cody/dev/src/git-down/src/test/kotlin/com/codymikol/state/neotest.kt::"NeotestKotestSpec"::"a namespace"::"should handle passed assertions"'
M.make_result_id = function(line, path)
  --
  -- get the line starting after the PREFIX_OFFSET
  local line_without_prefix = string.sub(line, PREFIX_OFFSET + 2)

  local id = path

  local parts = vim.split(line_without_prefix, " > ", { trimempty = true })

  -- Cleaning the fully qualified class name

  local fullyQualifiedClassName = parts[1]

  local fullyQualifiedClassNameParts = vim.split(fullyQualifiedClassName, "%.")

  local className = fullyQualifiedClassNameParts[#fullyQualifiedClassNameParts]

  parts[1] = className

  print("REMOVING FROM " .. vim.inspect(parts))

  table.remove(parts, 1)

  -- cleaning the it description that is for some reason prepended with the fully qualified class name...

  local itDescription = parts[#parts]

  local cleanedItDescription = string.sub(itDescription, fullyQualifiedClassName:len() + 2)

  parts[#parts] = cleanedItDescription

  for _, value in ipairs(parts) do
    id = id .. "::" .. '"' .. value .. '"'
  end

  -- I'm sure there is a better way to do this, but I'm sleepy...

  if string.find(id, ' PASSED"', -8) then
    return string.sub(id, 1, -9) .. '"'
  end

  if string.find(id, ' SKIPPED"', -9) then
    return string.sub(id, 1, -10) .. '"'
  end

  if string.find(id, ' FAILED"', -8) then
    return string.sub(id, 1, -9) .. '"'
  end

  return result
end

local function escape_magic_chars(str)
  return str:gsub("([%^%$%(%)%%%.%[%]%*%+%-%?])", "%%%1")
end

M.get_result_short = function(line)
  local parts = vim.split(line, ">", { trim = true })
  local short_with_status = parts[#parts]
  local short_with_status_parts = vim.split(short_with_status, " ")
  table.remove(short_with_status_parts, #short_with_status_parts)
  local short = table.concat(short_with_status_parts, " ")
  return short:sub(2)
end

---@params line string
---@params path string
M.line_to_result = function(line, path, package)
  if not string.find(line, "%[TestEventLogger%]") then
    return {}
  end

  if not string.find(line, escape_magic_chars(package)) then
    return {}
  end

  if not string.match(line, "(PASSED)$") and not string.match(line, "(SKIPPED)$") and not string.match(line, "(FAILED)$") then
    return {}
  end

  local id = M.make_result_id(line, path)
  local status = M.get_result_type(line, package)
  local short = M.get_result_short(line)

  return {
    id = id,
    status = status,
    short = short,
  }
end

M.lines_to_results = function(lines, path, package)
  local results = {}

  for _, line in ipairs(lines) do
    local result = M.line_to_result(line, path, package)
    if not result.id then
      -- noop
    else
      results[result.id] = result
    end
  end

  return results
end

return M

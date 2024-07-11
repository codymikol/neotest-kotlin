local M = {}

-- tests is the name of the test block
-- specs is the package name of the file you are interpreting
-- outfile is where the test output will be written to.
M.parse = function(tests, specs, outfile)
  return "export kotest_filter_tests='"
      .. tests
      .. "'; export kotest_filter_specs='"
      .. specs
      .. "'; ./gradlew cleanTest test --debug --console=plain | tee -a "
      .. outfile
end

return M

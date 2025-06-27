local M = {}

--- Initialize before running each test.
function M.init()
	vim.cmd([[set runtimepath=$VIMRUNTIME]]) -- reset, otherwise it contains all of $PATH
	vim.opt.swapfile = false
	vim.opt.packpath = { ".tests/all/site" } -- set packpath to the site directory

	-- add test-logging.init.gradle.kts to runtimepath
	local init_script_path = vim.fs.normalize(
		vim.fs.joinpath(debug.getinfo(1).source:match("@?(.*/)"), "..", "test-logging.init.gradle.kts")
	)

	vim.opt.runtimepath:append(init_script_path)
end

M.init()

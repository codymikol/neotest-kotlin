local M = {}

--- Initialize before running each test.
function M.init()
	vim.cmd([[set runtimepath=$VIMRUNTIME]]) -- reset, otherwise it contains all of $PATH
	vim.opt.swapfile = false
end

M.init()

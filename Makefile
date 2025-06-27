.PHONY: test

test:
	nvim --headless --noplugin -u tests/bootstrap_init.lua -c "PlenaryBustedDirectory tests/ { minimal_init = './tests/minimal_init.lua', timeout = 50000 }"

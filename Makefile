.PHONY: test clean

# timeout set to 3 mins (in milliseconds) to allow for gradle setup time
test:
	nvim --headless --noplugin -u tests/bootstrap_init.lua -c "PlenaryBustedDirectory tests/ { minimal_init = './tests/minimal_init.lua', timeout = 180000 }"

clean:
	rm -rf .tests

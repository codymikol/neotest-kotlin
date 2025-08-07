.PHONY: lua-test kotlin-test test clean format check

SOURCES := $(shell find lua tests -name *.lua)

# timeout set to 3 mins (in milliseconds) to allow for gradle setup time
lua-test:
	nvim --headless --noplugin -u tests/bootstrap_init.lua -c "PlenaryBustedDirectory tests/ { minimal_init = './tests/minimal_init.lua', timeout = 180000 }"

kotlin-test:
	./kotlin-test/gradlew -p kotlin-test test

test: lua-test kotlin-test

clean:
	rm -rf .tests

format:
	stylua --verify $(SOURCES)

check:
	stylua --check $(SOURCES)

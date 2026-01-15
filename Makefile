.PHONY: lua-test kotlin-test test clean format check publish-kotlin-test-locally watch-kotlin-test

SOURCES := $(shell find lua tests -name *.lua)

# timeout set to 10 mins (in milliseconds) to allow for gradle setup time
lua-test:
	nvim --headless --noplugin -u tests/bootstrap_init.lua -c "PlenaryBustedDirectory tests/ { minimal_init = './tests/minimal_init.lua', timeout = 600000 }"

kotlin-test:
	./kotlin-test/gradlew -p kotlin-test test

test: lua-test kotlin-test

clean:
	rm -rf .tests

format:
	stylua --verify $(SOURCES)
	./kotlin-test/gradlew -p kotlin-test detekt --auto-correct

check:
	stylua --check $(SOURCES)

publish-kotlin-test-locally:
	./kotlin-test/gradlew -p kotlin-test publishToMavenLocal

watch-kotlin-test:
	fswatch -o kotlin-test/gradle-plugin kotlin-test/core | xargs -n2 -I{} make publish-kotlin-test-locally

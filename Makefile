.PHONY: lua-test kotlin-test kotlin-functional-test test clean format check publish-kotlin-test-locally watch-kotlin-test

SOURCES := $(shell find lua tests -name *.lua)

# timeout set to 10 mins (in milliseconds) to allow for gradle setup time
lua-test:
	nvim --headless --noplugin -u tests/bootstrap_init.lua -c "PlenaryBustedDirectory tests/ { minimal_init = './tests/minimal_init.lua', timeout = 600000 }"

kotlin-test:
	./kotlin-test/gradlew -p kotlin-test test

# Gradle TestKit tests of the Gradle plugin, GRADLE_VERSION defaults to the version of the kotlin-test wrapper
kotlin-functional-test:
	./kotlin-test/gradlew -p kotlin-test :gradle-plugin:functionalTest $(if $(GRADLE_VERSION),-PfunctionalTestGradleVersion=$(GRADLE_VERSION))

test: lua-test kotlin-test

build-example-project:
	./tests/example_project/gradlew -p tests/example_project build

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

.PHONY: build docker offline check verify

DOCKER ?= docker
OUTPUT_DIR ?= build/distributions
GRADLE_ARGS ?=

# Export the installable ZIP directly; no temporary named container.
build: docker

docker:
	$(DOCKER) build --progress=plain --build-arg "GRADLE_ARGS=$(GRADLE_ARGS)" --target artifact --output "type=local,dest=$(OUTPUT_DIR)" .

offline:
	python3 tools/build-offline.py --test --psi-test

check: offline
	python3 tools/verify-offline.py

verify:
	./gradlew --no-daemon check verifyPlugin buildPlugin

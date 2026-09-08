.PHONY: build docker offline check verify

# Export the installable ZIP directly; no temporary named container.
build: docker

docker:
	docker build --target artifact --output type=local,dest=build/distributions .

offline:
	python3 tools/build-offline.py --test --psi-test

check: offline
	python3 tools/verify-offline.py

verify:
	./gradlew --no-daemon check verifyPlugin buildPlugin

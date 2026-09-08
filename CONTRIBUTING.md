# Contributing

This fork is maintained through AI-assisted development. Read
[AI_DEVELOPMENT.md](AI_DEVELOPMENT.md) and [AGENTS.md](AGENTS.md) before working on it.
Implementation priorities and current limitations are in [the plan](DEVELOPMENT_PLAN.md).

## Checkout and build

```sh
git clone https://github.com/byscrimm/phpstorm-yii2-support.git
cd phpstorm-yii2-support
git remote add upstream https://github.com/vxdy/Yii2-Support-Extended.git
git switch -c codex/your-topic
```

Use Java 25 and the SDK version in `gradle.properties`. With installed PhpStorm:

```sh
python3 tools/build-offline.py --test --psi-test
python3 tools/verify-offline.py
```

Both tools accept `--ide /path/to/PhpStorm`; `PHPSTORM_HOME` can also select the SDK.
Use `--java-home /path/to/jdk-25` for the offline builder if the bundled runtime
lacks `javac`. For a separate PSI run, set `JAVA_HOME` or use the IDE's bundled JDK.

With access to dependency repositories:

```sh
./gradlew --no-daemon check verifyPlugin buildPlugin
```

`make docker` builds with Java 25, runs Gradle checks and exports the ZIP to
`build/distributions`. Plugin Verifier is a separate Gradle/CI step, not part of that
Docker target. Prepared build paths must be described as unverified until executed.

## Test the behavior being changed

Use `tests/` for pure logic and `psi-tests/` for PHP syntax/reference behavior.
`gradle-tests/` exposes both suites to JUnit. Include an example that failed before
the fix and relevant negative cases. Keep incomplete PHP safe while users type.

The lightweight suites do not test the full PHP index or completion UI. For changes
to index-backed completion, navigation or refactoring, also test a minimal Yii2
project in an activated isolated IDE profile. Record IDE build, scenario and result.
Use a disposable database only if a migration test actually requires execution.

For documentation-only changes, check Markdown links and `git diff --check`.
Do not add tests that merely assert documentation text or mirror implementation.

## Pull requests

Keep changes focused. Explain the user-visible problem, resulting behavior, actual
validation and remaining limitations. Use the PR template. Do not remove upstream
notices or reformat unrelated code. AI-generated code requires the same scrutiny as
any other change; never claim a review or test that did not happen.

For bug reports, include the exact IDE build, plugin/Yii/PHP versions, minimal PHP
example, expected result and actual result. Remove credentials and unrelated client
code from examples and logs.

## Releases

1. Choose a new version in `gradle.properties`; update changelog and plan. A release
   candidate remains an RC until its stated release criteria have been met.
2. Run the relevant suites, official Plugin Verifier and isolated-IDE scenarios.
   Record skipped checks and blockers; do not turn a static check into a runtime claim.
3. Build the installable ZIP and verify its descriptor, dependencies, SHA-256 and
   installation. Keep IDE libraries, test classes, profiles and credentials out.
4. Record a dated verification report under `docs/verification/` with artifact hash,
   SDK/build, commands, results and remaining limitations. Update project memory.
5. For an authorized release, publish the ZIP, checksum and report as release assets
   from the reviewed commit. Do not commit generated archives or overwrite an existing
   published version with new bytes. Marketplace publishing is a separate step.

## Repository services

`renovate.json` prepares dependency PRs with automerge disabled and tracks
the repository's default branch. It does not install or authorize Renovate.
The GitHub workflow runs tests/verification and stores artifacts; it does not publish
Marketplace releases. Repository rules, Issues availability and bot access are
GitHub settings, not effects of adding these files.

The text for the GitHub About field is in [.github/about.txt](.github/about.txt).
Git does not automatically sync that field. An authenticated maintainer can apply
it through the repository UI, or through `gh repo edit --description` using that text.

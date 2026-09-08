# Changelog

## Unreleased

- Connect the development checkout to `byscrimm/phpstorm-yii2-support` while preserving upstream history.
- Add English/Russian project descriptions, feature and compatibility documentation, and explicit fork/AI-development attribution.
- Add agent instructions, shared project memory, contributor/release guidance and pull request reporting.
- Correct Renovate fork/default-branch configuration, remove inherited reviewer/automerge settings, and bound CI runtime/concurrency.
- Preserve upstream changelog separately in [docs/UPSTREAM_CHANGELOG.md](docs/UPSTREAM_CHANGELOG.md).

## 1.1.0-rc.1

- ActiveRecord relation completion, segment references and a search contributor for relation getter usages.
- Nested relations, JOIN aliases, callback keys, named arguments, local query variables, inherited relations, `self::class` and `static::class` relation targets.
- Conservative handling of unrelated classes, non-relation getters, SQL strings, ambiguous branches and incomplete PHP.
- Correct last-call handling of `asArray(false)` and safe PHP array-key ownership checks.
- PHP PSI regression suite using IntelliJ CoreApplicationEnvironment; real string-reference rename test.
- Updated Docker/Makefile/CI definitions, versioned ZIP checksums and build reports; deterministic ZIP entry metadata.

This is a release candidate. Local SDK tests and static ABI checks do not establish full IDE runtime compatibility. See the verification report shipped beside the ZIP.

## 1.0.0

- Java 25 / PhpStorm 262 SDK migration, replacement of removed APIs and explicit Swing form construction.
- Alias cycle prevention, component application context, route navigation, SQL placeholder parsing and value-preserving fixes.
- Migration process/output handling and project lifecycle fixes.
- Safer translation completion and offline build tools.

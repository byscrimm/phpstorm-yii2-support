# Changelog

## Unreleased

- Connect the development checkout to `byscrimm/phpstorm-yii2-support` while preserving upstream history.
- Add English/Russian project descriptions, feature and compatibility documentation, and explicit fork/AI-development attribution.
- Add agent instructions, shared project memory, contributor/release guidance and pull request reporting.
- Correct Renovate fork/default-branch configuration, remove inherited reviewer/automerge settings, and bound CI runtime/concurrency.
- Preserve upstream changelog separately in [docs/UPSTREAM_CHANGELOG.md](docs/UPSTREAM_CHANGELOG.md).

## 1.1.0-rc.3

### Added

- Repository governance, descriptive branch names, Conventional Commit PR titles, issue forms and release evidence requirements.
- CI/release reports with ZIP integrity checks, checksums, actual test counts and explicit unmeasured metrics.
- Tag-based draft GitHub Releases with build provenance and a stable-release gate tied to IDE evidence for the exact ZIP.
- Original BSD license, notices and author credits inside the plugin distribution.

### Fixed

- Replace the internal database delegate API with the public data-source accessor, preserving migration refresh behavior.
- Pass the project to the view-call popup constructor to avoid the constructor scheduled for removal.

### Included from earlier local increments

- PhpStorm 2026.2 / Java 25 migration, safer SQL fixes, migration lifecycle, aliases, components, routes and translations.
- ActiveRecord relation completion/navigation/rename and GridView/DetailView model and callback support.
- Docker build, regression suites and searchable settings index. Detailed changes remain in the historical sections below.

### Limitations

- This is a release candidate. Full IDE behavior and performance verification remain pending.
- Marketplace identity migration and third-party asset/license review are required before a separate Marketplace listing.
- No GitHub or Marketplace publication is implied by this changelog entry.

## 1.1.0 (local build, not published)

- Adopt semantic versions without automatic RC suffixes; retain historical prerelease reports.
- Use descriptive feature/fix/build branch names for AI-maintained development.
- Fix Gradle verifier IDE configuration that prevented all Gradle builds.
- Verify Docker check/buildPlugin on Linux arm64, including both regression suites and searchable options.
- Export Docker build checksums, build properties and test reports alongside the installable ZIP.

## 1.1.0-rc.2

- Replace fixed-depth widget PSI heuristics with structural GridView/DetailView contexts.
- Infer GridView rows from ActiveDataProvider queries, local variables and named constructor/widget arguments; support widget/provider subclasses.
- Complete public/PHPDoc attributes, readable getters and nested ActiveRecord relation paths; use cursor-aware attribute:format:label parsing.
- Use filterModel for filterAttribute without confusing it with provider row types.
- Infer only the first value callback parameter, including arrow functions; preserve explicit/PHPDoc types and distinguish array/object query results.
- Defer callback model resolution until indices are available; remove the legacy callback type heuristic.
- Exclude labels, nested options, write-only properties, unrelated widgets, non-data columns and ambiguous queries.
- Add regression coverage using the real PHP PSI, field analysis and PHPDoc parsers from the SDK.

Full IDE completion/type-provider integration and Plugin Verifier remain unverified.

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

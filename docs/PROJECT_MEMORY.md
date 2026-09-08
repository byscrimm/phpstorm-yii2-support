# Project memory

Last updated: 2026-09-08. This is a dated handoff, not an evergreen test result.
Read `AGENTS.md` first; verify current source and configuration before making changes.

## Identity and intent

- Repository: `byscrimm/phpstorm-yii2-support`, default branch `master`.
- Fork of `vxdy/Yii2-Support-Extended`, originally `nvlad/yii2support`.
- Upstream base when connected: `cd9cb4485e457f12a13faf2f0632ca861d8b2685`.
- Maintainer wants a reliable, useful Yii2 plugin for PhpStorm 2026.2.
- All updates to this fork are developed with AI assistance; inherited authorship
  and BSD license remain intact. See `AI_DEVELOPMENT.md`.
- Repository name differs from the current plugin name (`Yii2 Support Extended`),
  ID (`com.yii2supportExtended`) and artifact prefix (`yii2-support-extended`). These
  runtime identifiers have deliberately not been renamed during repository setup.

## Build baseline

`gradle.properties` is the version source: plugin `1.1.0-rc.1`, PhpStorm `2026.2.2`.
SDK used locally: `262.10315.130`; Java 25. Gradle Wrapper configuration: 9.1.0;
IntelliJ Platform Gradle plugin: 2.18.1. Do not assume these are the latest releases.

- `tools/build-offline.py`: compiles with an installed PhpStorm SDK, optionally runs
  both suites, packages only after requested checks pass, writes ZIP/SHA/build info.
- `tools/test-php-psi.py`: lightweight IntelliJ Core environment with real PHP parser.
- `tools/verify-offline.py`: classfile/JVM member reference and descriptor checks.
- `tools/smoke-ide.py`: separate full IDE test profile; requires valid activation.
- `Dockerfile` / `Makefile`: BuildKit artifact export; `make docker` builds/tests.
- `.github/workflows/gradle.yml`: Gradle tests, Plugin Verifier and ZIP artifacts.

`build/` is generated and ignored. A fresh clone does not contain local ZIPs or SDKs.
No full IDE profile or IDE license data belongs in the repository.

## Source map

Production Java is under `src/com/nvlad/yii2support/`; extension registration is in
`resources/META-INF/plugin.xml` and optional descriptors beside it.

| Area | Main entry points / purpose |
| --- | --- |
| `common` | PHP arguments/arrays, local values, aliases, Yii context, routes, SQL scanning |
| `relations` | `YiiModelResolver`, `RelationContext`, completion, segment references, usage search |
| `typeprovider` | Deferred Yii component and ActiveRecord result type resolution |
| `configurations`, `objectfactory` | Component index, config completion and property references |
| `views` | Render/view lookup, references, inspections, settings and view index |
| `url`, `i18n` | Route and translation completion |
| `database`, `properties` | Schema-assisted attributes and SQL parameter inspections/fixes |
| `forms`, `validation`, `widgetsconfig`, `attributeLabels` | Form/model/widget completion |
| `migrations` | Project service, process output, cancellation and tool-window UI |
| `tests`, `psi-tests`, `gradle-tests` | Pure logic tests, PHP PSI regressions, JUnit adapters |
| `integration` | Separate full IDE smoke-test application starter |

## Work already implemented

The first increment migrated removed IDE APIs and build settings to 262/Java 25,
replaced legacy application components and static project-retaining state, improved
aliases, application component scope, routes, translation completion, SQL parameter
fixes and migration process/disposal handling.

The second increment added ActiveRecord relations in `with`, `joinWith`,
`innerJoinWith`, `getRelation`, `via`: dotted paths, JOIN aliases, callback keys,
inherited relation getters, local query variables, named args, `self`/`static`
targets, segment navigation/rename and relation getter usage-search registration.
The final `asArray(false)` now overrides an earlier array-mode call.

## Important implementation findings

- PHP named argument names/colons can be siblings of the expression. Use
  `PhpArguments.get`, not a positional-only parameter lookup.
- A string array key can have a PSI wrapper between it and `ArrayHashElement`.
  Use `PhpArrays.keyEntry`; do not infer ownership from the direct parent alone.
- `LocalPhpValues` is bounded syntax analysis. It rejects ambiguous branch-only
  assignments and cycles; it is not general PHP dataflow analysis.
- `YiiModelResolver` is short-lived and uses injected symbols in tests. Production
  symbols come from `PhpIndex`. Custom Query scopes/typed parameter fallback remain
  incomplete. Do not make this a global cache of PSI or projects.
- Getter names (`getOrders`) differ from relation strings (`orders`); direct soft
  references alone do not provide index-wide getter usage search.
- Relation rename tests use the SDK's real string manipulator and preserve dotted
  suffixes and JOIN aliases. Whole-IDE refactoring still needs integration tests.
- Core test environment needs `TreeAspect` before `PomModelImpl`, parser registry
  defaults from the SDK, and smart-pointer initialization while the app is alive.
- Two existing removal warnings concern a `ListPopupImpl` constructor in
  `views/actions/OpenViewCalls`; do not confuse them with compilation failures.

## Last verified plugin artifact

Historical local report: [1.1.0-rc.1 verification](verification/1.1.0-rc.1.md).

- 148 pure-logic checks passed.
- 52 real PHP PSI checks passed.
- 182 plugin classes checked against 1272 SDK jars; static JVM check passed.
- ZIP/JAR structure, descriptor version and absence of test classes checked.
- ZIP SHA-256: `209c17977f05757517f33d9e13a44f614c37ac574f042f6c460b94150efed8a4`.

These results belong to the recorded local RC artifact. They are not a claim that
every later checkout or a GitHub Actions run passed. Repository documentation and
workflow maintenance do not rebuild that ZIP.

## Outstanding verification and next work

Full IDE startup previously stopped at `No valid license found` in an isolated
profile. Full index, completion UI and end-to-end Find Usages/Rename are unverified.
Gradle/Docker and the official Plugin Verifier have not run locally because their
dependency downloads were not authorized. Do not work around either condition.

Next milestone: GridView/DetailView model inference from `dataProvider`, sharing the
relation model resolver. Then improve controller-to-view flow (named args, `compact`,
local arrays), config merging/DI, routes/i18n and indexing/performance. See the
checkboxes in `DEVELOPMENT_PLAN.md`; these are plans, not advertised completed features.

Repository setup added docs, agent guidance, templates and local Git remotes.
Remote publication, GitHub About changes, repository settings, bot installation and
Marketplace publication must be reported separately when they actually occur.

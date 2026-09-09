# Project memory

Last updated: 2026-09-09. This is a dated handoff, not an evergreen test result.
Read `AGENTS.md` first; verify current source and configuration before making changes.

## Identity and intent

- Repository: `byscrimm/phpstorm-yii2-support`, default branch `master`.
- Fork of `vxdy/Yii2-Support-Extended`, originally `nvlad/yii2support`.
- Upstream base when connected: `cd9cb4485e457f12a13faf2f0632ca861d8b2685`.
- Maintainer wants a reliable, useful Yii2 plugin for PhpStorm 2026.2.
- Start changes only after a direct action request or explicit confirmation; discussion is not authorization.
- Use semantic versions and RCs while release validation is pending. Use meaningful `feature/`,
  `fix/`, `build/` and `docs/` branches, never agent-branded prefixes.
- All updates to this fork are developed with AI assistance; inherited authorship
  and BSD license remain intact. See `AI_DEVELOPMENT.md`.
- Current name: Yii2 Insight; vendor by_scrimm; ID/package io.github.byscrimm.yii2insight.
- User authorized independent identity migration; legacy settings stay untouched.
- See IDENTITY.md. PR #2 has been merged into build/repository-workflow; PR #1 carries the complete 1.0.0 preparation to master.

## Build baseline

`gradle.properties` is the version source: plugin `1.0.0`, PhpStorm `2026.2.2`.
SDK used locally: `262.10315.130`; Java 25. Gradle Wrapper configuration: 9.1.0;
IntelliJ Platform Gradle plugin: 2.18.1. Do not assume these are the latest releases.

- `tools/build-offline.py`: compiles with an installed PhpStorm SDK, optionally runs
  both suites, packages only after requested checks pass, writes ZIP/SHA/build info.
- `tools/test-php-psi.py`: lightweight IntelliJ Core environment with real PHP parser.
- `tools/verify-offline.py`: classfile/JVM member reference and descriptor checks.
- `tools/smoke-ide.py`: separate full IDE test profile; requires valid activation.
- `Dockerfile` / `Makefile`: verified BuildKit artifact export; `make docker` builds/tests.
  `OUTPUT_DIR` changes export location; `GRADLE_ARGS=--info` enables diagnostic logs.
  Shared Gradle download cache is locked across builds. No host JDK/IDE is required.
- Verifier IDE configuration must use `ides.create(IntelliJPlatformType.PhpStorm, version)`,
  not the dependency DSL method `phpstorm`; the latter prevented Gradle configuration.
- `.github/workflows/ci.yml` calls build-and-verify.yml for policy, Docker tests,
  official Plugin Verifier and machine-readable evidence. release.yml creates draft
  GitHub Releases from version tags; stable eligibility requires exact-ZIP IDE evidence.

`build/` is generated and ignored. A fresh clone does not contain local ZIPs or SDKs.
No full IDE profile or IDE license data belongs in the repository.

## Source map

Production Java is under `src/io/github/byscrimm/yii2insight/`; extension registration is in
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

The GridView/DetailView increment (RC.2) replaces fixed-depth PSI heuristics with
`WidgetContext`, adds `WidgetModelResolver` and cursor-aware shorthand parsing.
`WidgetCallbackTypeProvider` records a local deferred signature during indexing and
resolves model classes later. Only the first value parameter is inferred; explicit and
PHPDoc types are preserved. filterModel does not determine row callback types.


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
- Closure values may be PhpExpression wrappers around Function; compare expression/function ranges
  when recognizing a direct callback. Inline @param documentation may sit before the wrapper
  rather than on Function.getDocComment(). Do not rely only on Parameter.getDocTag().
- Field/PHPDoc tests register the SDK DFA assertion/doc-prefix extension points and actual
  property/param tag parsers. These are test harness registrations, not production plugin dependencies.
- Core test environment needs `TreeAspect` before `PomModelImpl`, parser registry
  defaults from the SDK, and smart-pointer initialization while the app is alive.
- The ListPopupImpl constructor scheduled for removal was replaced with its Project
  overload in the repository increment. Keep remaining deprecations in the roadmap.

## Last verified plugin artifact

Current Docker report: [2.0.0-rc.1 verification](verification/2.0.0-rc.1.md).
Previous identity report: [1.1.0-rc.3 verification](verification/1.1.0-rc.3.md).
Previous Docker report: [1.1.0 verification](verification/1.1.0.md).
Previous local report: [1.1.0-rc.2 verification](verification/1.1.0-rc.2.md).
Previous artifact: [1.1.0-rc.1 verification](verification/1.1.0-rc.1.md).

- 148 pure-logic checks passed.
- 119 PHP PSI checks passed (67 added in the GridView/DetailView increment).
- 190 plugin classes checked against 1272 SDK jars; static JVM check passed.
- ZIP/JAR structure, descriptor version and absence of test classes checked.
- Current ZIP hash and Docker test results are recorded in the 2.0.0-rc.1 report.
- Docker exports checksums, build properties and JUnit HTML/XML reports.

The 148/119 suites also passed in Docker on Linux arm64 for 1.1.0.
Static JVM checking used the local macOS SDK. Results belong to the recorded artifacts. They are not a claim that
every later checkout or a GitHub Actions run passed. Repository documentation and
workflow maintenance do not rebuild that ZIP.

## Outstanding verification and next work

Full IDE startup previously stopped at `No valid license found` in an isolated
profile. Full index, completion UI and end-to-end Find Usages/Rename are unverified.
Docker/Gradle check and buildPlugin have now passed with newly authorized dependency
access. buildSearchableOptions also completed in the container. Official Plugin Verifier now passed for 262.10315.130 after replacing the internal
DbDataSource.getDelegate call with getDelegateDataSource. The popup constructor now
receives Project. Deprecated API usages remain; full IDE scenarios are unverified.

Next implementation milestone: SQL aliases/attributes, then asArray result shapes,
controller-to-view flow, config merging/DI, routes/i18n and indexing/performance. GridView provider factories,
post-construction provider/query mutations, widget config variables and custom Query
defaults/scopes remain unsupported. Full IDE callback type/completion integration also remains unverified. See the
checkboxes in `DEVELOPMENT_PLAN.md`; these are plans, not advertised completed features.

Repository setup commit `cb150d9` is on origin/master. The RC.2 increment and Docker
packaging are included in the pushed `build/repository-workflow` branch and draft
PR #1. No candidate changes have been merged into master or released.

## Repository process and workstation boundary

Read GOVERNANCE.md, RELEASING.md, TESTING.md, METRICS.md and GITHUB_SETUP.md in this
directory. The working IDE/settings, license files, Settings Sync, unrelated projects
and live databases must not be used for testing. Use separate profiles and fixtures.

Local branch: build/repository-workflow, based on ca8664e, including earlier local
feature increments. New commits use Conventional Commits and an AI-Assisted trailer.
Original LICENSE is unchanged and now shipped with NOTICE/AUTHORS in both build paths.
Dependabot replaces the unactivated Renovate configuration; no automatic merges.
Release metrics come from actual JUnit/ZIP/build results, not invented coverage.
Release tools were tested for failed suites, stale ZIP evidence, incorrect versions
and altered distribution notices. Runtime JSON is evidence input, not implemented UI automation.

GitHub web session is now authenticated. Observed/applied on 2026-09-08: Issues enabled;
release immutability enabled; squash-only merge with PR title/description; merged branch
cleanup enabled; private vulnerability reporting, dependency graph and Dependabot alerts
enabled. Secret Protection and push protection were already enabled and remain so.
About now identifies features, fork and AI maintenance. Default Actions token is read-only;
Actions cannot create/approve PRs, and full commit SHA pinning is enforced remotely.
master protection requires PRs, resolved conversations, linear history, up-to-date
branches and both GitHub Actions checks: `checks / Repository policy` and
`checks / Build and verify`. No admin bypass, force push or deletion is allowed.
Required independent approvals: zero for this single-maintainer repository.

Draft PR: https://github.com/byscrimm/phpstorm-yii2-support/pull/1
First successful CI: https://github.com/byscrimm/phpstorm-yii2-support/actions/runs/34260061922
It passed Docker, 148 core + 119 PSI checks and Plugin Verifier on Linux x64, but
the report correctly flagged a dirty checkout: inherited gradlew.bat CRLF content
was not normalized in Git despite .gitattributes. Renormalized that file and added
a clean-checkout CI policy. Follow-up run 34260853315 passed with clean source
(PR head 84a5b1c, tested merge affffdf9); exact ZIP hash and artifact link are in
verification/1.1.0-rc.3.md. Later documentation-only commits do not change that evidence.
The active `Immutable version tags` ruleset blocks updates/deletion/force pushes
for v*, has no bypass actors, and permits creating new tags. Topics are yii2,
phpstorm-plugin, intellij-plugin and ai-assisted-development.
No tags/releases/Marketplace publication have been created. Workflows and Dependabot
remain on the PR branch until merge; remote settings are already applied.


## Independent identity increment — 2026-09-08

Yii2 Insight identity migration is implemented on refactor/yii2-insight-identity.
Java source/test packages, settings class/storage, action/index/configurable IDs,
inspection short names/descriptions and view templates are namespaced. Known original
plugins are declared incompatible; automatic settings import is intentionally absent.
Original author comments and LICENSE remain. See IDENTITY.md for transition details.

Final local Docker/Plugin Verifier passed for 262.10315.130 (24 deprecations),
148 core + 119 PSI checks passed; offline JVM check passed for 190 classes/1272 jars.
Five tooling tests and actionlint passed; exact ZIP evidence is in verification/2.0.0-rc.1.md.
GitHub About now says Yii2 Insight by by_scrimm. New branch CI must be checked separately.
No Marketplace registration, published release, full IDE test or automatic old-settings migration.
Priority 0 is identity; SQL-alias and asArray result-shape tasks are recorded after GridView,
with no implementation of those two features in this change.


## Independent version sequence and runtime setup — 2026-09-09

The maintainer chose 1.0.0-rc.1 as the first public Yii2 Insight candidate, then
1.0.0 after validation. The prior 2.0.0-rc.1 was never published; retain its reports
as historical evidence only. GitHub release listing was empty on this date.
Current metadata, installation examples and versioning guidance follow this choice.

Local build with --test --psi-test passed: 148 core and 119 PHP PSI checks.
Static JVM verification passed: 190 classes against 1272 SDK jars.
Repository policy, five tooling tests and workflow validation passed. See
verification/yii2-insight-1.0.0-rc.1.md for the exact new ZIP and verification limits.
Docker/official Plugin Verifier and full IDE scenarios remain pending for this ZIP.

Both draft PRs remain open with passing CI observed before local renumbering:
#2 (identity) targets the branch of #1 (repository workflow), which targets master.
These checks do not cover the local version change. No merge or release performed.
An isolated graphical IDE was launched under build/ide-runtime-2.0.0-rc.1 with
separate config/system/plugins/log paths and awaits user activation. The directory
name is historical; no candidate is installed there yet. UI tooling cannot attach
to its Java app (Invalid app). Do not copy working-profile credentials/settings.
Next: finish Trial activation, use the new 1.0.0-rc.1 ZIP for runtime scenarios,
complete verification and review the dependent PRs before merging.
Existing AGENTS.md and untracked branding changes were preserved.


## Current relation contract and RC.4 — 2026-09-09

The maintainer explicitly authorized this contract: strings in with/joinWith and
other supported relation contexts navigate to getters but cannot initiate Rename.
Find Usages from getters or their PHPDoc properties should find model-matched
relation strings. Rename from declarations pairs getter, local PHPDoc property
and string usages; unrelated columns, nested segments and JOIN aliases must stay.
Missing PHPDoc gets a weak suggestion to add the property, not automatic source
editing or unverified synthetic PHP fields. Changes require direct authorization.

RC.1 user screenshots confirmed region and nested cities completion; user confirmed
navigation. Inplace Rename failed (getter text inserted into string); user undid it.
RC.2 dialog and RC.3 navigation-only policies were superseded by the clarified
contract. Their reports remain historical, not current feature claims.

RC.4 adds a supplemental rename processor, declaration dialog with conflicts and
preview, model-aware getter/PHPDoc usage search, and a missing-PHPDoc inspection.
Native PHP method refactoring remains primary. PHPDoc is paired locally with its
getter; cross-subclass PHPDoc synchronization is not established. No synthetic
property declaration is injected; add PHPDoc when the inspection suggests it.

Checks: offline --test --psi-test passed, 146 core + 143 PHP PSI checks; static JVM
verification passed; repository policy, five tooling tests, workflow validation and
git diff --check passed. New PSI evidence covers rename pairing, model separation,
conflicts, PHPDoc mutation preserving comments/columns, repeat segment mutation,
and missing/dynamic PHPDoc cases. It does not run the full PHP index or Rename UI.
Exact ZIP: verification/yii2-insight-1.0.0-rc.4.md. Docker/official Verifier remain
pending for RC.4. No PR merge or release performed.

Activated isolated IDE lives under build/ide-runtime-2.0.0-rc.1; working IDE must
not be touched. Fixture: build/ide-fixtures/yii2-insight-basic (official Yii 2.0.53
framework source, static analysis only). UI tool cannot attach to the Java app;
user performs UI scenarios and provides screenshots. Next: install RC.4, verify
blocked string Rename and getter/PHPDoc Find Usages, then coordinated Rename with
Preview and Undo in both directions. Continue GridView/settings/restart afterwards.


## Getter PHPDoc usage and read-only suggestion — RC.5, 2026-09-09

User confirmed RC.4 Find Usages finds relation strings, but getter search omitted
the class PHPDoc property. User explicitly authorized fixing that and recommending
@property-read for relations without a public setter. RC.5 emits a scoped PHPDoc
usage reference for the getter; its rename callback is intentionally a no-op because
the paired declaration rename already owns the PHPDoc edit. Existing documentation
is not rewritten. Public inherited/trait setters keep the @property suggestion.

Checks: offline build, 146 core + 151 PHP PSI checks PASS; static JVM verification,
repository policy and git diff --check PASS. Tests include documentation usage
identity, avoiding a duplicate getter-spelled property rename, @property-read
recognition and inherited public setters. Full IDE results for RC.5 are pending.
See verification/yii2-insight-1.0.0-rc.5.md. Next: install RC.5, confirm getter
Find Usages includes PHPDoc and relation strings, then continue Rename/Undo checks.
The isolated IDE's native Restart previously invoked the ordinary PhpStorm launcher;
close the test process and restart with its isolated launch.py instead. RC.4 was
successfully relaunched this way. Never restart the working IDE.


## Class relation PHPDoc quick fixes — RC.6, 2026-09-09

User explicitly requested yellow highlighting on class PHPDoc/name and actions
from the getter and class to create missing relation documentation. RC.6 adds a
smart-pointer LocalQuickFix: one selected relation or all missing local relations.
It recomputes missing entries at invocation, preserves existing text/indentation,
creates a sibling class PHPDoc when absent, and skips ambiguous target/cardinality
or asArray results. Types use fully qualified target names: hasOne adds |null,
hasMany adds []; getter-only properties use @property-read. No automatic edits.

Checks: offline build with 146 core + 165 PHP PSI checks PASS; static JVM check,
repository policy and git diff --check PASS. New tests exercise actual PHPDoc PSI
replacement/creation, preservation, duplicate suppression, single/all selection,
indentation and ambiguous types. IDE highlight/quick-fix action/Undo remain pending.
See verification/yii2-insight-1.0.0-rc.6.md for exact ZIP evidence. No merge/release.

User-confirmed RC.5 scenarios during this conversation: getter Find Usages shows
PHPDoc and strings; coordinated Rename/Preview and Undo scenarios reported OK;
string Rename blocked; GridView attributes/nested attributes and callback model
completion OK; unrelated with() has no relation suggestions. Screenshot confirms
the old missing-PHPDoc warning on getter. These are user-reported/manual results,
not automated full-index tests or complete release evidence. Next: install RC.6,
verify class/getter Alt+Enter actions and one Undo, then continue settings/restart.


## Stable 1.0.0 preparation — 2026-09-09

Maintainer authorized stable release preparation and publication, English/Russian
repository descriptions, upstream/new-feature provenance and Marketplace copy.
Minimum is the tested PhpStorm build 262.10315.130 (2026.2.2), until 262.*;
Java 25 prevents advertising older Java-21 IDE branches with these bytes.
README, FEATURES and Marketplace documents describe capabilities and possible bugs.
No skipped test is recorded as passed. Confirmed RC.6 class/getter PHPDoc actions,
Undo, settings persistence/restart, DetailView and reopen/indexing observations
supplement the earlier relation runtime tests. The separate non-Yii project scenario
was declined; unrelated with() in the Yii fixture is not equivalent evidence.

Final-version local build: 146 logic + 165 PHP PSI checks and static JVM verification
(200 classes, 1272 SDK jars) passed. Tooling tests (5), repository policy and workflows
passed. Docker/official verifier report and final ZIP are recorded in
verification/release-1.0.0.md. Candidate UI results do not certify the new ZIP hash.
Stable publication still requires the exact final ZIP runtime report under RELEASING.md.
User-owned untracked branding files in img/ remain untouched and outside packaging.


Repository handoff: PR #2 merged after CI run 34333539506 passed policy,
Docker suites, official Plugin Verifier and artifact validation. PR #1 is being
updated as the complete 1.0.0 integration. The maintainer authorized all repository
operations without manual involvement. Automated connection to the isolated Java IDE
failed with Invalid app for its observed bundle ID and display name; the working
PhpStorm was not touched. Stable publication remains gated by actual final-ZIP
runtime evidence. Do not invent it or bypass the release workflow.

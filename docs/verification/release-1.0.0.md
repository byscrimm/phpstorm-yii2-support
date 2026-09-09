# Yii2 Insight 1.0.0 — release preparation

Date: 2026-09-09. Publication is authorized by the maintainer; this report records
preparation evidence, not a published release or Marketplace approval.

## Compatibility

Descriptor range: 262.10315.130 through 262.*. Actual target: PhpStorm 2026.2.2,
build 262.10315.130, Java 25. Earlier IDE versions have not been qualified.

## Automated verification

- Offline build: 146 core and 165 actual PHP PSI regression checks passed.
- Static JVM check: 200 plugin classes against 1272 SDK jars passed. This is not
  official Plugin Verifier or a full IDE runtime test.
- Repository policy, five release-tooling tests and workflow validation passed.
- Docker check/buildPlugin/verifyPlugin results and final archive identity are below.

The PHP PSI suite uses a supplied class table rather than a complete PHP index.
JUnit wraps the two regression suites; individual regression checks are not
independent JUnit test methods.

## Manual observations

The maintainer confirmed candidate workflows in the isolated PhpStorm profile:
relation and nested completion, navigation, getter/PHPDoc usages, coordinated
Rename/Preview/Undo, the string-Rename prohibition, widget attribute/callback
completion, missing PHPDoc highlighting and class/getter fixes, settings persistence,
restart and reopening/indexing. These observations belong to the tested candidates;
see the individual RC reports for their archive identities. Bugs may remain.

## Publication boundary

The new final-version archive does not yet have an exact-hash runtime evidence JSON.
The separate non-Yii project scenario was not executed; an unrelated with() call in
the Yii fixture is a narrower negative check. No passed result is recorded for it.
The existing stable release workflow therefore cannot publish this preparation yet.
See [release requirements](../RELEASING.md). No release tag or published asset has
been created by this preparation. Marketplace content is ready for the maintainer's
submission once the release artifact is eligible.

## Final Docker artifact

- Archive: `yii2-insight-1.0.0.zip` (412580 bytes).
- SHA-256: `c91623f24fb4fdc91707042c89c1b58bf006eb8c25fca9ef81690f978b982df3`.
- Docker check/buildPlugin/verifyPlugin: passed (Linux arm64).
- Official Plugin Verifier: Compatible. 24 usages of deprecated API.
- JUnit: two suite methods, zero failures/errors/skips; 146 core + 165 PSI checks.
- Local export: ignored `build/release-1.0.0-verified/`; complete build log under
  `build/release-1.0.0-preparation/docker-final.log`.
- Source was a dirty preparation checkout. CI must supply clean-commit provenance;
  this local archive is not a published or attested release.

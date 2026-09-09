# Verification: Yii2 Insight 1.0.0-rc.4

Date: 2026-09-09. Local uncommitted coordinated relation-symbol implementation.

- `python3 tools/build-offline.py --test --psi-test`: PASS, 146 core + 143 PHP PSI checks.
- `python3 tools/verify-offline.py`: PASS; static JVM/descriptor check, not official Plugin Verifier.
- Repository policy, five tooling tests, workflow validation, `git diff --check`: PASS.
- SDK: PhpStorm 2026.2.2 / 262.10315.130, Java 25.
- ZIP: `yii2-insight-1.0.0-rc.4.zip`.
- SHA-256: `31ea59184329983b4149ebd1087b3bc36bfb2fc9a72a0ce5537ad37ab82b12f0`.

PSI checks cover getter/PHPDoc rename mapping, different-model exclusion, property
usage adapters, conflicts, repeated segment replacement preserving aliases,
PHPDoc mutation preserving comments/columns, and missing/dynamic documentation.
They do not exercise index-wide Find Usages, action dispatch, the Rename dialog,
Preview, actual project-wide refactoring or Undo. Those require isolated IDE testing.
No virtual PHP property PSI is injected. Missing PHPDoc is a weak inspection.
Pairing covers a getter and its local PHPDoc, not all subclass documentation.
Docker and official Plugin Verifier are not rerun for this ZIP. No release or merge.

Runtime checklist (all pending for RC.4):
- String Rename blocked without edits; navigation remains available.
- Getter and PHPDoc Find Usages both show matching with/joinWith segments.
- Rename getter to getArea: PHPDoc $area, strings area.cities r, ordinary property accesses.
- Rename PHPDoc back to region: getter getRegion, matching strings/property accesses.
- Preview, cancel and one Undo preserve/restore every affected file.
- Remove relation PHPDoc: weak suggestion, no automatic edits; unknown getters stay quiet.

# Verification: Yii2 Insight 1.0.0-rc.5

Date: 2026-09-09. Local getter PHPDoc usage and documentation-suggestion fix.

- Offline --test --psi-test: PASS, 146 core + 151 PHP PSI checks.
- Static JVM reference/descriptor verification: PASS.
- Repository policy and git diff --check: PASS.
- SDK: PhpStorm 2026.2.2 / 262.10315.130, Java 25.
- ZIP: `yii2-insight-1.0.0-rc.5.zip`.
- SHA-256: `13269d238f7bb52a806002e27a32d3d9a18f97387285759bd043dca58b15a499`.

New PSI checks cover PHPDoc usage identity, prevention of a duplicate getter-name
write, property-read recognition, and inherited public setter suggestions.
Existing @property declarations are not automatically rewritten. Getter usages
include local PHPDoc only and respect the selected search scope. Full-index/UI
validation, Docker and official Plugin Verifier remain pending for these bytes.
RC.4 Find Usages of relation strings was confirmed by the user; its omission of
PHPDoc in getter results motivated this fix. No rename/Undo success is claimed.

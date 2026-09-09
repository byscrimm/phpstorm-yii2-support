# Verification: Yii2 Insight 1.0.0-rc.6

Date: 2026-09-09. Local class PHPDoc highlighting and relation quick fixes.

- Offline --test --psi-test: PASS, 146 core + 165 PHP PSI checks.
- Static JVM reference/descriptor verification: PASS.
- Repository policy and git diff --check: PASS.
- SDK: PhpStorm 2026.2.2 / 262.10315.130, Java 25.
- ZIP: `yii2-insight-1.0.0-rc.6.zip`.
- SHA-256: `fd4dd829f2ad7a7abce1b21aceafb93886f4871d9b15fde614afcd32336c97f7`.

New tests exercise actual PHP PSI comment insertion/replacement, nullable/collection
relation types, single/all missing local relations, duplicate suppression, preserved
comments/columns/indentation and conservative rejection of ambiguous results.
Quick fixes do not auto-run and do not modify existing property declarations.
Full IDE highlight/action/Undo checks, Docker and official Plugin Verifier remain
pending for this ZIP. Prior user-confirmed RC.5 scenarios do not verify RC.6 bytes.

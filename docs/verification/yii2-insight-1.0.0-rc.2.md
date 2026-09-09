# Verification: Yii2 Insight 1.0.0-rc.2

Date: 2026-09-09. Local uncommitted relation-string Rename fix.

- Offline build with --test --psi-test: PASS, 148 core + 122 PHP PSI checks.
- Static JVM verification: PASS (see local ignored build/version-reset-validation/rename-jvm.log).
- Repository policy and git diff --check: PASS.
- SDK: PhpStorm 2026.2.2 / 262.10315.130, Java 25.
- ZIP: yii2-insight-1.0.0-rc.2.zip.
- SHA-256: 160b585268de603f7f1ee139d91fdeefdb5bc9b4afbcb5a97cdb1a53bf7913d2.

The handler uses the documented [RenameHandler extension](https://plugins.jetbrains.com/docs/intellij/rename-refactoring.html).
Regression checks cover selected-segment targeting, alias exclusion, local PHPDoc
selection and existing segment-preserving mutation. They do not run the dialog,
full PHP index or end-to-end Rename. Repeat those in the activated isolated IDE.
Docker/official Plugin Verifier and runtime scenarios are pending for this ZIP.
RC.1 user screenshots and navigation confirmation are evidence for RC.1 only;
its Rename scenario failed. No stable release readiness is claimed.

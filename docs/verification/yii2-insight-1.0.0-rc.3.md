# Verification: Yii2 Insight 1.0.0-rc.3

Date: 2026-09-09. Local navigation-only relation change, not published.

- Offline build with --test --psi-test: PASS, 146 core + 123 PHP PSI checks.
- Static JVM verification: PASS.
- Repository policy and git diff --check: PASS.
- SDK: PhpStorm 2026.2.2 / 262.10315.130, Java 25.
- ZIP: yii2-insight-1.0.0-rc.3.zip.
- SHA-256: 19a950f583c7a8ff8997cc5085c3d5f0f620f995c62ebcc846c855b9fe8da974.

The maintainer removed relation-string Rename from the intended feature set.
Regression evidence covers navigation targets, ignored rename preserving the
string/getter, and exclusion from getter usage matching. It does not exercise
the IDE Rename action or full index. Repeat Shift+F6 and navigation in the isolated
profile. Docker/official Plugin Verifier and full runtime scenarios remain pending.
The RC.1 Rename failure and abandoned RC.2 workflow are historical evidence only.

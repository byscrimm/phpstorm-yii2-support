# Verification: Yii2 Insight 1.0.0-rc.1

Date: 2026-09-09. Local working tree on refactor/yii2-insight-identity.
This is the independent public version sequence, not the historical upstream build.
No release was published and no GitHub CI result is claimed for this change.

- `python3 tools/build-offline.py --test --psi-test` — PASS: 148 core and 119 PHP PSI checks.
- `python3 tools/verify-offline.py` — PASS: 190 plugin classes against 1272 SDK jars.
- `python3 tools/check_repository.py` — PASS.
- `python3 -m unittest discover -s tools -p 'test_*.py'` — PASS: five tests.
- `bash tools/check-workflows.sh` — PASS.
- SDK: PhpStorm 2026.2.2 / 262.10315.130, Java 25.
- Offline ZIP: `yii2-insight-1.0.0-rc.1.zip`.
- SHA-256: `97279143772477b3ffe6025ebe316b0ea352b2a1f6f635a97ca8d11f9fab78d0`.

Docker and official Plugin Verifier were not rerun for this ZIP. Historical
2.0.0-rc.1 results do not verify these bytes. Full IDE installation, completion,
navigation, rename, inspections, settings persistence, restart, indexing and
non-Yii scenarios remain unverified. The isolated graphical profile awaits activation.
Local logs and artifacts remain under ignored build/.

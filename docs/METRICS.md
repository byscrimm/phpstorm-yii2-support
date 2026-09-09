# Quality metrics

Measure outcomes needed to assess a release. Do not collect runtime user telemetry
or add external analytics to obtain repository metrics.

| Metric | Source / interpretation |
| --- | --- |
| CI pass/failure | GitHub Actions run and commit; canceled is not passed |
| Build duration | Wall time of Docker build command, including downloads when uncached |
| JUnit methods | Actual XML report; currently wrappers around two regression suites |
| Regression checks | Actual PASS counts in suite output; not code coverage |
| Plugin Verifier | Task exit and reports for configured SDK; list warnings/limits |
| ZIP size/hash | Measured bytes and SHA-256 of delivered archive |
| IDE scenarios | Exact artifact hash, IDE build, OS, evidence link and scenario status |
| Coverage | Unmeasured until instrumented and scoped; use null, never invent a percentage |
| Completion/indexing latency and memory | Unmeasured until a repeatable workload and baseline exist |

Do not reward lines of code, AI token consumption or commit count as software quality.
Do not add badges claiming coverage, downloads or compatibility without a real source.
Repository release downloads/issue response times can be reviewed later if they inform
maintenance; they are not required to start distributing the plugin.

Compare performance on the same IDE, fixture, machine and warm/cold index conditions.
Record repeated samples (median/p95 when enough samples exist), baseline plugin state
and test harness overhead. Adopt a regression threshold only after measuring a baseline.

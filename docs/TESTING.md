# Verification and IDE isolation

The maintainer's working PhpStorm and unrelated projects are not test environments.
Never install a candidate in the daily IDE, restart it, clear its caches, modify
VM options, copy license files or change its plugins/settings as a side effect of testing.
Use a separate SDK/profile with separate config, system, plugins and log directories.
Do not import settings or connect Settings Sync. Activation is handled explicitly
if needed. Do not open real projects in the test IDE: use fixtures or a separate copy.
Use disposable databases for migration tests; limit memory and concurrent IDE processes.

## Verification layers

| Layer | Evidence | Does not prove |
| --- | --- | --- |
| Tooling tests | Python unittest output | Plugin behavior |
| Core regression suite | Counts in JUnit stdout | Full PHP index or UI |
| PHP PSI suite | Real parser/PSI with supplied class table | Full index or end-to-end refactoring |
| Static JVM check | tools/verify-offline.py against installed SDK | Official Plugin Verifier or runtime behavior |
| Official Plugin Verifier | Successful verifyPlugin and detailed reports | Correct completion, latency, or absence of all bugs |
| Isolated IDE scenarios | Exact ZIP hash, IDE build, OS, scenario results/logs | Untested IDE builds and project layouts |
| Performance measurements | Defined project, hardware, baseline and repeated samples | Unmeasured platforms or workloads |

## Commands

```sh
python3 tools/check_repository.py
python3 -m unittest discover -s tools -p 'test_*.py'
bash tools/check-workflows.sh
make docker
make docker GRADLE_ARGS=verifyPlugin OUTPUT_DIR=build/verified
python3 tools/release_tools.py --directory build/verified --verifier passed
```

`--verifier passed` is valid only after the official task succeeded. A normal
`make docker` result must use the default `not_run`. CI passes the flag only after
the combined Docker command succeeds. Failed Docker builds preserve logs in CI;
ZIP/report export happens only on success. An old export directory is not evidence
that a failed new build passed. CI uses a clean runner/output directory.

For local offline checks, use the commands in AGENTS.md. Re-run source/descriptor
checks after relevant edits, not just because documentation changed. Never describe
prepared CI or an unexecuted integration harness as a completed run.

## Runtime release scenarios

Use a minimal Yii2 project with ActiveRecord relations, an ActiveDataProvider,
GridView/DetailView, views and configuration. Check both valid and incomplete PHP,
unrelated contexts and ambiguous inputs. Assert expected completion items/types,
navigation destinations and exact rename/fix text preservation. Check settings save,
restart, project reopening/reindexing and a non-Yii project without plugin interference.
Record errors, EDT freezes and indexing issues even if a visible assertion passed.

The current full-index/UI automation is still a roadmap item. The JSON evidence
gate records completed scenarios; it does not implement or simulate those tests.
Starter/Driver automation should be developed in a separate focused change.

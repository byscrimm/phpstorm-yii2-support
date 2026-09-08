"""Regressions for release gates: stale evidence, failed tests and altered distributions."""
import io
import json
from pathlib import Path
import tempfile
import unittest
import zipfile
from release_tools import (RUNTIME_SCENARIOS, changelog_section, inspect_zip,
                           junit_results, runtime_evidence, stable_gate, validate_version)


class ReleaseToolsTest(unittest.TestCase):
    def test_version_and_changelog_boundaries(self):
        for version in ("1.0.0", "1.2.3-rc.1", "2.0.0-beta.2"):
            self.assertEqual(version, validate_version(version))
        for version in ("v1.0.0", "01.0.0", "1.0", "1.0.0-rc.0", "1.0.0\n", "1.0.0;echo bad"):
            with self.assertRaises(ValueError):
                validate_version(version)
        notes = "## 1.0.0-rc.1\n\nCandidate\n\n## 1.0.0\n\nFinal\n"
        self.assertEqual("Final", changelog_section("1.0.0", notes))
        with self.assertRaises(ValueError):
            changelog_section("1.0.1", notes)

    def test_stable_gate_rejects_unverified_or_dirty_builds(self):
        report = {"version": "1.0.0-rc.1", "dirty_worktree": False,
                  "plugin_verifier": "passed", "ide_runtime": {"status": "not_run"}}
        stable_gate(report)
        with self.assertRaises(ValueError):
            stable_gate({**report, "version": "1.0.0"})
        for change in ({"dirty_worktree": True}, {"plugin_verifier": "not_run"}):
            with self.assertRaises(ValueError):
                stable_gate({**report, **change})
        stable_gate({**report, "version": "1.0.0", "ide_runtime": {"status": "passed"}})

    def test_runtime_evidence_cannot_be_reused_for_another_zip(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "runtime.json"
            self.assertEqual("not_run", runtime_evidence(path, "1.0.0", "a" * 64)["status"])
            evidence = {"version": "1.0.0", "artifact_sha256": "a" * 64, "status": "passed",
                        "ide_build": "262.10315.130", "os": "test-os", "architecture": "test-arch",
                        "tested_at": "2026-09-08", "evidence_url": "https://example.com/report",
                        "scenarios": dict.fromkeys(RUNTIME_SCENARIOS, "passed")}
            path.write_text(json.dumps(evidence))
            self.assertEqual("passed", runtime_evidence(path, "1.0.0", "a" * 64)["status"])
            with self.assertRaises(ValueError):
                runtime_evidence(path, "1.0.0", "b" * 64)
            evidence["scenarios"]["rename"] = "not_run"
            path.write_text(json.dumps(evidence))
            with self.assertRaises(ValueError):
                runtime_evidence(path, "1.0.0", "a" * 64)

    def test_junit_requires_executed_successful_suites(self):
        with tempfile.TemporaryDirectory() as temp:
            directory = Path(temp)
            with self.assertRaises(ValueError):
                junit_results(directory)
            template = '<testsuite tests="2" failures="{failures}" errors="0" skipped="{skipped}"><system-out>PASS: 148 core regression checks\nPASS: 119 real PHP PSI regression checks</system-out></testsuite>'
            path = directory / "TEST-example.xml"
            path.write_text(template.format(failures=0, skipped=0))
            result = junit_results(directory)
            self.assertEqual(2, result["test_methods"])
            self.assertEqual(267, sum(result["checks"].values()))
            for failures, skipped in ((1, 0), (0, 1)):
                path.write_text(template.format(failures=failures, skipped=skipped))
                with self.assertRaises(ValueError):
                    junit_results(directory)

    def test_distribution_requires_matching_identity_version_and_notices(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "resources/META-INF").mkdir(parents=True)
            (root / "resources/META-INF/plugin.xml").write_text('<idea-plugin><id>example</id></idea-plugin>')
            for name in ("LICENSE.md", "NOTICE.md", "AUTHORS.md"):
                (root / name).write_text(name)
            archive = root / "plugin.zip"

            def build(version="1.0.0", changed_notice=False, test_class=False):
                data = io.BytesIO()
                with zipfile.ZipFile(data, "w") as jar:
                    jar.writestr("META-INF/plugin.xml", '<idea-plugin><id>example</id><version>' + version + '</version><idea-version since-build="262" until-build="262.*"/></idea-plugin>')
                    for name in ("LICENSE.md", "NOTICE.md", "AUTHORS.md"):
                        jar.writestr("META-INF/" + name, "altered" if changed_notice else name)
                    if test_class:
                        jar.writestr("ExampleRegressionTest.class", b"test")
                with zipfile.ZipFile(archive, "w") as target:
                    target.writestr("example/lib/plugin.jar", data.getvalue())

            build()
            self.assertEqual("example", inspect_zip(archive, "1.0.0", root)["id"])
            for options in ({"version": "1.0.1"}, {"changed_notice": True}, {"test_class": True}):
                build(**options)
                with self.assertRaises(ValueError):
                    inspect_zip(archive, "1.0.0", root)


if __name__ == "__main__":
    unittest.main()

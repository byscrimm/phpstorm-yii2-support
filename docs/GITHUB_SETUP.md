# GitHub settings and activation

These are desired settings, not a claim that they are already enabled. SSH Git
authentication permits push/fetch; GitHub settings and PR/release APIs require a
separate authenticated web session or GitHub CLI authorization.

## Apply after the first successful CI run

1. Push `build/repository-workflow`, create a PR to master titled
   `ci: establish verified release and repository workflow`. Enable Actions on the
   fork if GitHub asks. Inspect the actual check-run names and results.
2. Protect master: require PRs, successful repository-policy and build/verifier
   checks, up-to-date branches and resolved conversations. Apply protection to
   administrators; disallow force pushes and branch deletion. Use linear history.
3. For this single-maintainer project, require zero independent approving reviews.
   CODEOWNERS still identifies the owner. Revisit this when another reviewer joins.
4. Allow squash merging; disable merge-commit and rebase merge options. Use PR title
   as squash title and preserve the useful PR body/trailers. Delete merged branches.
   Keep automatic merging disabled until the stable-branch process has been exercised.
5. Set About from `.github/about.txt`, add meaningful topics (`yii2`, `phpstorm`,
   `intellij-platform`, `java`, `ide-plugin`) and link the README. Enable Issues.
6. Enable private vulnerability reporting, dependency graph/Dependabot alerts and
   secret scanning/push protection where available. Do not claim a feature is enabled
   merely because the account plan supports it. Review new findings before adding gates.
7. Enable **release immutability** in repository Settings → General → Releases.
   Always attach all assets to a draft before publication. Protect release tags
   against modification/deletion with repository rules if available; allow intentional
   creation of new version tags. Do not lock out the release workflow accidentally.
8. Keep default workflow token permissions read-only. Workflows grant narrowly scoped
   writes only to the release-draft job. Do not enable "create and approve PRs" merely
   to simulate independent reviews. Restrict untrusted fork workflow approvals.
9. Dependabot version updates are configured in `.github/dependabot.yml` for Gradle
   and GitHub Actions weekly, with bounded PR counts. No auto-merge is enabled.
   The unused Renovate configuration was removed to avoid competing update bots.

GitHub check names can include reusable-workflow prefixes. Select the names from a
real successful run instead of guessing. `tools/configure_github.py` can prepare or
apply the supported settings via an authenticated `gh`; it discovers actual checks
from an explicit verified commit. It does not apply the remaining UI-only settings.

```sh
# Read-only preview; requires an authenticated gh and a full verified commit SHA.
python3 tools/configure_github.py --verified-commit <full-sha>
# Apply the reviewed preview only when repository administration is authorized.
python3 tools/configure_github.py --verified-commit <full-sha> --apply
```

The script refuses to replace existing branch protection automatically: inspect
and update that policy explicitly, preserving unrelated restrictions. It never
merges PRs, publishes releases, changes visibility or deletes repository content.

## Bootstrap and publication limits

Adding workflows to an unmerged branch does not install them on master. The first
PR can run candidate workflows; branch protection is enabled only after their names
and behavior are known. Do not bypass a failed gate to make the repository look green.
Record actual remote activation separately in PROJECT_MEMORY.md after checking it.

No Marketplace credentials are needed for this setup. Choose the independent
Marketplace identity and perform the license/asset audit in a separate change.

References:

- https://docs.github.com/en/actions/reference/security/secure-use
- https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/establish-provenance-and-integrity/prevent-release-changes
- https://docs.github.com/en/actions/how-tos/secure-your-work/use-artifact-attestations/use-artifact-attestations

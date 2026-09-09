# GitHub settings and activation

The checklist below defines desired settings; the activation record identifies
what has actually been applied. SSH Git
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

### Activation record — 2026-09-08

- Draft [PR #1](https://github.com/byscrimm/phpstorm-yii2-support/pull/1) is open;
  [first CI run](https://github.com/byscrimm/phpstorm-yii2-support/actions/runs/34260061922)
  passed policy, Docker tests/packaging and official Plugin Verifier.
- master protection is active: PR required, zero independent approvals, resolved
  conversations, linear history, up-to-date branch, no admin bypass, force pushes
  or deletion. Both required checks accept results from GitHub Actions only:
  `checks / Repository policy` and `checks / Build and verify`.
- Squash-only merge, PR title/description, automatic branch deletion; auto-merge off.
- About updated from .github/about.txt; Issues and release immutability enabled.
- Private vulnerability reporting, dependency graph and Dependabot alerts enabled;
  Secret Protection and push protection were already on and remain enabled.
- Actions default token is read-only; create/approve PR permission off. Full-SHA
  pinning is enforced in repository settings as well as policy checks.
- Active `Immutable version tags` ruleset targets `v*`: updates, deletions and
  force pushes blocked, no bypass actors, new tag creation allowed.
- Topics: yii2, phpstorm-plugin, intellij-plugin, ai-assisted-development.
- No release/tag workflow or attestation has been executed. Dependabot configuration
  and workflow files are in the PR, awaiting merge; CodeQL is not configured.

The first CI report exposed an inherited gradlew.bat normalization issue (dirty
checkout despite fresh checkout). The follow-up normalizes its Git content and
checks source cleanliness in CI. The [corrected CI run](https://github.com/byscrimm/phpstorm-yii2-support/actions/runs/34260853315)
passed with a clean checkout; exact ZIP evidence is in verification/1.1.0-rc.3.md.
Consult the current PR checks for later documentation-only commits.

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

# Security policy

Security fixes target the latest supported release. This fork has not yet published
an independently verified stable release; historical local builds are not support promises.

Do not post credentials, database contents or exploitable private-project details
in public issues. Use GitHub's **Security → Report a vulnerability** for this
repository (enabled on 2026-09-08). If that control is
unavailable, open an issue requesting a private contact channel, without exploit
details. No private reporting channel or response-time SLA is claimed until configured.

Include the plugin version, IDE build, affected feature, impact and a minimal
sanitized reproduction. The maintainer triages, reproduces in an isolated environment,
prepares a fix and coordinates disclosure. AI may assist with analysis; do not submit
secrets or third-party private code you are not authorized to share.

The plugin must not add telemetry, AI API calls or execute project PHP for static
analysis. Migrations require deliberate user execution; tests use disposable databases.
See docs/TESTING.md for isolation requirements and verification boundaries.

# Security Policy Argonaut

Argonaut is an **unofficial** Android client for the undocumented Argo ScuolaNext
APIs, built on top of the Argos Kotlin library. Because both depend on a
protocol we do not control, treat every release as potentially fragile, and
treat every vulnerability report as confidential.

## Supported versions

Only the latest CalVer tag (`v2026.08.x` at the time of writing) receives
security fixes. Patch releases bump only the `MICRO` component of the version
string.

## Reporting a vulnerability

**Do not open a public issue.** Email the maintainers directly or open a
private advisory via GitHub's "Report a vulnerability" flow on the repository.

Please include.

- The affected version and Android API level.
- A minimal reproduction (what you did, what happened, what you expected).
- Any sanitized logs. **Never include passwords, tokens, or student data.**

You will receive an acknowledgment within 72 hours and a status update within
one week. Fixes land as fast as the `MICRO` release cadence allows.

## What matters here

- **Credentials and session tokens** must stay encrypted at rest (Android
  Keystore-backed AES-GCM) and never appear in logs, crash reports, or widgets.
- **Sanitized diagnostics.** Any log or crash attachment must strip tokens,
  passwords, and personally identifiable information before leaving the device.
- **Network transport** must stay HTTPS end-to-end; certificate pinning is not
  feasible against a third-party API, so TLS verification is delegated to the
  platform.

## Responsible disclosure

If you find a vulnerability, we ask for a reasonable embargo (default 30 days)
before public disclosure so a patched `MICRO` release can ship first.

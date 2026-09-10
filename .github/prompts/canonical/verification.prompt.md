# GeoCeDG canonical verification prompt

Status: canonical operational prompt

Use `tools/agent/verify.ps1`, the typed registry and schemas as executable
authority. Reports summarize preserved evidence; they do not replace execution.
Acceptance and diagnostics are structurally independent.

## Canonical profiles

- `-Profile STATIC`: static safety contracts and separate diagnostics.
- `-Profile INFRA_UNIT`: trusted verification-core tests.
- `-Profile WORKSTATION`: live, read-only installation verification.
- `-Profile OPERATIONAL`: registered pure operational contracts.
- `-Profile DEV`: focused developer selection.
- `-Profile PHASE -Phase <id>`: exact registered phase selection.
- `-Profile INTEGRATION`: integrated pure acceptance coverage.
- `-Profile FINAL`: one complete immutable-candidate campaign.

`COMPOSED` maps to `INTEGRATION`; `FULL` and `FullTests` map to
`FINAL`. An incomplete profile fails safely and never falls back to a mixed
legacy wrapper.

## Result contract

Blocking acceptance classes are `SEMANTIC`, `SAFETY` and
`VERIFICATION_CORE`. Every semantic result declares `semantic_domain` as
`PRODUCT`, `SCIENTIFIC` or `MIXED`. Governance, documentation, historical
consistency, style and performance telemetry are diagnostic classes.
Diagnostics use `DIAGNOSTIC_CLEAR`, `DIAGNOSTIC_FINDING` or
`DIAGNOSTIC_UNAVAILABLE`; they never alter acceptance, coverage or exit code.

Process producers preserve raw execution evidence without assigning a verdict.
Pure leaves and evidence projections assign meaning. Missing or malformed
required evidence makes coverage incomplete or untrusted. The verifier has no
timeout, watchdog, forced termination, duration gate or performance-derived
verdict; duration is informational only.

## Final acceptance and closeout

Freeze the exact candidate SHA before running `FINAL`. Execute each required
producer at most once, preserve all raw evidence, emit one typed report and one
immutable receipt, then make no further commit. Stop for explicit author review.

Closeout is read-only identity inspection. It validates the candidate commit,
tree, receipt schema and bound plan/checker/environment/input identities. It
never launches `FINAL`, edits the worktree or index, creates or amends a
commit, changes refs or tags, pushes, or records author approval. Promotion
requires a separate explicit author instruction naming the exact candidate SHA.

Never install tools silently, suppress a semantic or safety failure, edit
generated evidence, or translate an environment/permission failure into product
code. For every run record the profile, exact command, exit code, report path,
acceptance verdict, coverage verdict, diagnostic count and execution identity.
Run `git diff --check` and report final repository status separately.

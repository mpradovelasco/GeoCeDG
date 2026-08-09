# GeoCeDG canonical governance prompt

Status: canonical operational prompt

## Governing authority

Read `AGENTS.md` first. Then read the current code/build, accepted
specifications and ADRs, approved validation sources, the pinned upstream
record, and task-specific authorities in their declared order. A prompt never
overrides those sources and must not restate geometric truth.

## Execution discipline

1. Classify the task and affected architectural layer.
2. Inspect repository state and the named source-of-truth files from disk.
3. Record assumptions, contradictions, generated artifacts, and stop
   conditions.
4. Produce or identify the required design/specification before code.
5. Change the smallest coherent GeoCeDG-owned file set; preserve upstream
   layout and unrelated work.
6. Add focused tests or deterministic structural checks with the change.
7. Run `tools/agent/verify.ps1` or the narrow executable authority it composes.
8. Report files, layer, semantic and compatibility effects, exact commands,
   exit codes, logs, skipped gates, and remaining risks.

## Source routing

- Geometric meaning and serialization: approved specifications plus shared
  kernel contracts.
- Operational manifests: `geocedg/specs/operations/manifest-contracts.md`.
- Feature availability: `geocedg/features/` after the feature specification is
  approved.
- Regression authority: `geocedg/validation/` and referenced model manifests.
- Generated evidence: `artifacts/`; never source authority.

## Mandatory stop conditions

Stop before editing when work would change the pinned baseline, require an
unapproved geometric/serialization decision, infer a missing license, place
geometric truth outside the kernel, or make a nondeterministic result
authoritative.

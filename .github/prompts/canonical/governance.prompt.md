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
   If the run is intended for final acceptance, first create clean exact
   technical commit `T`, pass mode-neutral
   `tools/agent/phase-closeout.ps1 -Action READINESS` for both routes, and bind
   one canonical clean FULL run to that receipt. Require its authenticated
   envelope to prove the PHASE, COMPOSED and FULL coverage obligations without
   inventing lower-level physical runs. Schema v2 requires complete FULL
   subsumption; if readiness finds an uncovered obligation, it must block until
   an approved schema/orchestrator extension represents and verifies the exact
   minimal complement. Only after
   author review and exact-`T` approval, explicitly select one closeout mode for
   PREPARE.
8. Report files, layer, semantic and compatibility effects, exact commands,
   exit codes, logs, skipped gates, and remaining risks.

## Acceptance and closeout authority

Never infer final-acceptance purpose or closeout mode from a branch, clean tree,
credentials, tag or `latest`. Dirty/precommit verification is development or
diagnostic evidence and remains `closeoutConsumable=false`; no later commit may
inherit its execution identity. `FULL=PASS` and author approval are independent
facts.

`AUTHOR REVIEW READY` means prepared for author review. `AUTHOR CLOSEOUT READY`
requires clean `T`, closeout-consumable required technical evidence, one
explicitly selected post-approval route and a PREPARE-valid binding to the
mode-neutral readiness plan. Readiness itself is not approval and selects no
mode. Only an explicit author decision naming exact `T` may authorize closeout.

`VERIFIED` retains ADR 0023/0024. `AUTHOR_OPERATED` preparation may construct
and stage only the policy-bounded status delta, then must leave commit `C`, tag,
fast-forward and push to the author. The mandatory post-promotion audit is
read-only apart from its result and reports, rather than repairs, any ancestry,
delta, tag, remote, cleanliness, approved-SHA or provenance mismatch. Reference
[`verification-levels.md` section 12](../../../geocedg/specs/operations/verification-levels.md#12-commit-first-acceptance-and-dual-closeout)
instead of reproducing its checks in each phase prompt.

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
authoritative. Stop before final acceptance when `T` is not clean, readiness
cannot validate both declared routes, evidence would depend on a moving ref or
self-reference, or a known lifecycle repair would be deferred until after FULL.
After approval, stop when the explicitly selected route cannot pass PREPARE.

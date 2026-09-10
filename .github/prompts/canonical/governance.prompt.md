# GeoCeDG canonical governance prompt

Status: canonical operational prompt during verification-infrastructure recovery

## Governing authority

Read `AGENTS.md` first, then current code, approved product/scientific
specifications and task-specific author authority. The executable verification
contract is the typed registry, its schemas and `tools/agent/verify.ps1`.
Historical ADRs and reports remain evidence and history; they are not parsed as
mutable execution policy.

`CLAUDE.md` is read-only for automated agents. No task or workflow authorizes
creating or mutating a file whose basename is `claude.md`, case-insensitively.

## Execution discipline

1. Classify the task, affected layer and semantic/safety domains.
2. Inspect repository state and current source-of-truth files from disk.
3. Define the smallest coherent path perimeter and focused checks.
4. Preserve unrelated work, scientific payloads and historical evidence.
5. Run the cheapest applicable independent profile first.
6. Collect every result in that tier; a diagnostic finding never stops
   independent acceptance checks.
7. Freeze the complete candidate before its single final acceptance execution.
8. Report exact commands, identities, results, evidence and coverage gaps.
9. Stop for author review; verification never publishes or records approval.

## Acceptance and diagnostics

Only pure `SEMANTIC`, `SAFETY` and `VERIFICATION_CORE` contracts affect
acceptance. Every `SEMANTIC` contract declares `semantic_domain` as `PRODUCT`,
`SCIENTIFIC` or `MIXED`. Governance, documentation,
historical consistency, style and duration are diagnostics. Diagnostic findings
are visible and traceable but do not change acceptance, coverage or exit code,
and require no waiver.

A process producer is verdict-neutral. Pure evidence projections interpret its
declared outputs. A final leaf may not mix acceptance and diagnostic contracts.
Missing or malformed required evidence makes the affected coverage incomplete
or untrusted. Severity is never inferred from prose, a filename or an aggregate
legacy exit code.

## Candidate and closeout

A final receipt binds the exact candidate commit/tree, execution plan, checker
identities, environment and consumed inputs. It is produced only for complete,
trusted, accepted execution. Diagnostic findings may be recorded without
invalidating it. Duration is informational and excluded from receipt identity.

Closeout is identity inspection only. It does not run verification, modify Git,
create commits or tags, push, or originate author approval. After the author
approves an exact candidate and receipt, promotion moves that exact SHA without
rerunning acceptance.

## Source routing and stop conditions

Geometric meaning and serialization remain in approved specifications and
kernel contracts. Regression authority remains in `geocedg/validation/`.
Generated artifacts remain evidence, never source authority.

Stop rather than guess when licensing is unclear, serialization would change
without migration, geometric truth would move outside the kernel, evidence is
lost, a required semantic domain is untrusted, or the requested path perimeter
is insufficient.

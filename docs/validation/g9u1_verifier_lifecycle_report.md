# G9U1 lifecycle normalization report

- Status: **IN PROGRESS — operational successor and focused proof pending**
- Date: 2026-09-07
- Product status: `FINAL TECHNICAL CANDIDATE — AUTHOR CLOSEOUT READY` remains
  pending completion of this operational correction
- Author-reviewed product checkpoint: `28f7843184cfb202bbfcca1cbcc56a25a7a77bca`
- Product tree: `d08d7beb45d04d6e0f0a478f4c04eb0e97e7e667`
- Historical execution `HEAD`: `e4ef3d48ea95a0c3243e57dfc703b539d455c33e`
- Architecture: [G9U1 verifier lifecycle](../architecture/g9u1_verifier_lifecycle.md)

## 1. Accepted lifecycle defect

The product/UI checkpoint is frozen and manually reviewed. The defect is only
that the G9U1 verifier had no committed-candidate and exact-SHA author-closeout
lifecycle. The successful technical runs were performed with `HEAD=e4ef3d48...`
and the eleven-path precommit worktree later committed as `28f7843...`. They
remain attributed to that original repository/index/status cohort.

No product, Desktop UI, scientific test, resource, profile, tolerance,
reference, Gradle/build input, test selection, JVM/toolchain policy, package,
`.cedg`, macro, identity or persistence change is authorized or included.

## 2. Frozen candidate authority

| Field | Value |
|---|---|
| Entry commit | `e4ef3d48ea95a0c3243e57dfc703b539d455c33e` |
| Implementation commit | `28f7843184cfb202bbfcca1cbcc56a25a7a77bca` |
| Implementation tree | `d08d7beb45d04d6e0f0a478f4c04eb0e97e7e667` |
| Candidate paths | 11, all modified, all mode `100644` |
| Index SHA-256 recorded by historical receipt | `9a93fb584699f85de0d30c0d4835c0cb17090b827d5b392aba360900f71c0dfe` |
| Porcelain status SHA-256 | `e1872ce83ce416f4d0ec71f7de299321b170f616b3b1541034c03db6b59417c3` |
| Raw input files / bytes | 11,299 / 187,439,751 |
| Raw input-inventory SHA-256 | `6841a6adcfe7317d772ad53461216cd73fe629a29735cf72e98beff2d45ec3c4` |
| Raw-tree SHA-256 | `556982f1572e6c98289a7130fb4a746a2ff3da4dedecfed7d3134ce4da0ee557` |
| Input fingerprint | `1091b2786245cd119b01e344eb7d480a4cda89b2411e1fab2c0b10135ec4c9b8` |

The status digest is over the eleven sorted porcelain-v1 ` M <path>` records,
LF-joined with no terminal newline, matching the runtime's exact status text.

All 11,299 historical physical-input records match the current clean
`28f7843...` checkout. Of those, 2,138 physical hashes equal their index blob
bytes directly and 9,161 require the repository's supported clean-filter
equivalence. This is expected on the validated Windows materialization and is
not grounds for a raw-equals-blob assertion.

## 3. Historical execution evidence

The following artifacts retain their native original provenance. Any new
schema-v2 bundle manifest is a retrospective lifecycle index, not evidence that
it existed during or sealed the historical run.

The G9U1 dispatcher requires that exact schema-v2 evidence kind. The generic
schema-v1 evidence-link path remains available only to lifecycle policies that
predate this G9U1 precommit-link contract; it is rejected for G9U1 closeout so
it cannot bypass the strict Git-mode and fully-staged authority checks.

The committed verifier likewise evaluates each historical presentation delta
only through `28f7843...`; lifecycle-only descendants are authenticated by the
separate infrastructure allowlist and never inflate a frozen product inventory.

| Evidence | Result | Frozen SHA-256 |
|---|---:|---|
| Focused A/B canonical summary | deterministic | `a20509ffda779665d6a60cfa041b1fe6568ef70d120e8faf6b79872d84b685bf` |
| PHASE root | PASS | `b3959bc4da983349886c34ad8953af3e80f4dbd6124b3aab0167d72ca01cc4c0` |
| COMPOSED root | 1,515/1,515, exit 0 | `ffc18e155f1e0ba8d74bdb01d64eff04c453eb1249bf6ab0d06b8d8e06c8c856` |
| COMPOSED receipt | sealed | `71233ba374607240230fa97e045a2cf7e3aa92be6872b04749ca2fc6ad0fde00` |
| FULL root | 8,015 passing, 11 inherited disabled, exit 0 | `4c5c4001d0bf56028e2f2ae0025df7fc1bdaeba638412874d4a4773a083fe5d6` |
| FULL receipt | sealed | `838a011c4b8eb627b0f6ebe63d60e7174dd56ee630c878990fa0fc719a20d3b8` |

These runs are not executions on `28f7843...` or on the future lifecycle
successor. The link must prove byte/provenance identity without changing their
recorded repository commit, index, status, timestamps or hashes.

## 4. Exact operational cohort

The repair is bounded to nine paths:

1. `docs/architecture/g9u1_verifier_lifecycle.md`;
2. `docs/validation/g9u1_verifier_lifecycle_report.md`;
3. `geocedg/validation/operations/g9u1-lifecycle-policy.json`;
4. `geocedg/validation/operations/g9u1-lifecycle-repair-policy.json`;
5. `tools/agent/phase-lifecycle.ps1`;
6. `tools/agent/tests/g9u1-lifecycle.Tests.ps1`;
7. `tools/agent/verification-repair-equivalence.ps1`;
8. `tools/agent/verify-g9u1-construction-workspace.ps1`; and
9. `tools/agent/verify-g9u1-lifecycle-repair.ps1`.

The implementation policy permits one operational successor. Any tenth path,
second operational commit or productive/test/build input fails closed.

## 5. ADR 0024 section 11.2 matrix

The fifteen conditions are not optional and are not reduced to a single boolean.
The final operational commit must fill the evidence column and make every row
`PASS` before the exception can be asserted.

| # | Required proof | Current result |
|---:|---|---|
| 1 | zero product Java changes | pending final successor diff |
| 2 | zero Desktop product/UI changes | pending final successor diff |
| 3 | zero scientific/product-test changes | pending final successor diff |
| 4 | zero numerical reference/tolerance changes | pending final successor diff |
| 5 | zero Gradle/build-script changes | pending final successor diff |
| 6 | zero test task/filter/selection changes | pending execution-plan proof |
| 7 | zero required Java/toolchain changes | pending execution-plan proof |
| 8 | zero numerical-command changes | pending execution-plan proof |
| 9 | zero JUnit/result-acceptance semantic changes | pending function-projection proof |
| 10 | zero generated-state lifecycle change affecting execution | pending function-projection proof |
| 11 | executable changes limited to input identity/provenance/closeout validation | pending exact repair-policy proof |
| 12 | successful sealed heavy evidence bound to exact historical cohort | source artifacts identified above; bundle authentication pending |
| 13 | complete focused infrastructure suite passes | pending two deterministic executions |
| 14 | bounded live shared and Desktop integrations pass | pending final successor integrations |
| 15 | deterministic execution-plan fingerprint unchanged | pending final structural comparison |

Until all rows pass:

```text
EVIDENCE_PRESERVING_VERIFIER_REPAIR = NOT YET ESTABLISHED
```

If any row fails or cannot be proved, the value becomes `false` and the normal
PHASE/COMPOSED/FULL requirement returns. No other exception is permitted.

## 6. Future status-only closeout

The lifecycle policy projects exactly fourteen paths: twelve living status and
traceability authorities, the canonical hash sidecar and a new author-closeout
record. It preserves historical Round 1/2/3 statements. The sidecar keeps its
five comments and 156 ordered paths; their LF list digest is
`a05faf95b4527c9e55077085971312945028edc3e70c7ca0de5478caa051f617`.

Real `AUTHOR_CLOSEOUT` is forbidden in this repair task. After the operational
successor is validated, committed and published, a later author instruction must
name that exact SHA. Only then may the status-only descendant, PASS tag and main
promotion be considered. The future pending and committed checks compare each
pre-existing closeout path's Git mode to the reviewed technical commit and
require the decision record to be `100644`; content equality alone is not
sufficient. Pending closeout validation additionally requires the exact delta
to be completely staged so that no worktree-only mode can evade Git authority.

## 7. Validation record

| Check | Result | Evidence |
|---|---|---|
| lifecycle/input-identity focused run A | pending | ignored run directory |
| lifecycle/input-identity focused run B | pending | ignored run directory |
| deterministic focused hash equality | pending | canonical summaries |
| bounded shared integration | pending | ignored run directory |
| bounded Desktop integration | pending | ignored run directory |
| repair-equivalence / execution-plan proof | pending | ignored run directory |
| `git diff --check` | pending final cohort | console/log |
| operational successor commit/tree | pending | exact Git authority |

No PHASE, COMPOSED or FULL rerun is claimed here. The final result must state
which evidence was actually executed and why any heavy campaign was or was not
required.

## 8. Impact and governance

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = NONE
selfApproved = false
authorApprovedImplementation = false
passClaimedImplementation = false
```

Bootstrap remains unchanged because no dependency, PowerShell/JDK/Gradle
requirement, environment variable, package input, workstation role or bootstrap
command changes. The G9U1 product checkpoint is immutable. `main` and
`geocedg-g9u1-pass` remain untouched until a later exact-SHA author closeout.

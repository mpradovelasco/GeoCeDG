# ADR 0023: Phase-verifier lifecycle and author closeout

- Status: **Stage I implementation candidate — technical validation pending**
- Date: 2026-09-03
- Scope: verification lifecycle and evidence provenance only
- Phase status: `G9S1-R1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`
- Product effect: none
- Parent authority: [ADR 0020](0020-verification-levels-and-current-run-evidence.md)
- Architecture: [G9S1-R1 verifier lifecycle](../architecture/g9s1_r1_verifier_lifecycle.md)
- Current record: [Stage I report](../validation/g9s1_r1_verifier_lifecycle_report.md)

## Context

The original G9S1-R1 verifier was deliberately written for an uncommitted
candidate. It required both the exact entry commit
`109f077fc5e2a40bcde45d3271eb928ee66fdfcc` and the exact branch
`codex/g9s1-r1-spline-pair-materialization`, in addition to candidate status and
false approval flags. Committing the byte-exact candidate necessarily changes
`HEAD`, so that verifier must reject the commit even when every committed blob
is identical to the frozen candidate.

That rejection is an entry-domain mismatch, not a scientific or product
failure. Removing the old `HEAD` assertion, accepting an arbitrary descendant,
or relabelling the precommit execution as postcommit evidence would all destroy
evidence provenance. A distinct lifecycle contract is required.

[ADR 0020](0020-verification-levels-and-current-run-evidence.md) already binds
technical evidence to a current source/input cohort and forbids cross-process
receipt reuse. Its own author closeout is a useful bounded precedent, but it did
not define a generic phase-verifier protocol for precommit, committed-candidate
and status-only closeout states. This decision supplies that narrow protocol.
It does not weaken ADR 0020, reactivate an old receipt, or make technical success
equivalent to author approval.

## Decision

### 1. Three explicit modes

A participating phase verifier has three semantically distinct modes. Mode is
selected from explicit lifecycle authority, never inferred from branch naming.
An unknown or internally inconsistent state fails closed.

#### `PRECOMMIT_CANDIDATE`

This mode preserves the original historical G9S1-R1 entry contract:

- `HEAD` is exactly `109f077fc5e2a40bcde45d3271eb928ee66fdfcc`;
- the branch is exactly `codex/g9s1-r1-spline-pair-materialization`;
- the complete candidate inventory and bytes match the declared candidate;
- the phase remains candidate/blocking;
- `selfApproved = false`;
- `authorApprovedPhase = false`;
- `passClaimed = false`.

The original scientific, source-hash, scenario, JUnit, Checkstyle, risk and
historical assertions remain live. This mode exists for exact historical
reproduction and diagnosis; it is not silently broadened to accept a commit.

#### `COMMITTED_CANDIDATE`

This mode authenticates the technical candidate by immutable Git and byte
authority rather than by its current branch:

- the exact candidate commit exists and descends from its authorized base;
- current `HEAD` descends from that exact commit;
- canonical phase provenance names that exact commit and tree;
- every candidate blob equals the frozen raw candidate blob;
- productive source, tests, resources, numerical references and phase semantics
  remain identical to the candidate commit;
- only exhaustively enumerated verification-infrastructure follow-up paths may
  differ, and those paths are separately validated;
- phase status remains implementation candidate;
- all three approval/pass indicators remain false.

Branch location is not durable candidate identity. An unenumerated descendant,
a changed productive blob, a changed test/tolerance/reference, missing ancestry,
or tampered provenance fails closed. This mode continues to run all live
scientific R1 assertions; sealed provenance is not a substitute for inspecting
the current candidate.

#### `AUTHOR_CLOSEOUT`

This mode is implemented and fixture-tested before it is ever activated against
the real phase. Activation requires a later explicit author decision naming one
exact `REVIEWED_TECHNICAL_COMMIT`; neither “latest”, a branch name, nor a future
tag can supply that authority.

For a closeout descendant, the verifier requires:

1. the named reviewed technical commit is an ancestor of `HEAD`;
2. its authenticated technical evidence bundle names that same commit and exact
   source/input cohort;
3. current executable inputs are raw-byte identical to the reviewed cohort;
4. the complete reviewed-to-closeout delta is exhaustively allowlisted as
   approval/status metadata and also satisfies the allowed content contract;
5. no productive source, test, verifier, build input, tolerance or numerical
   reference differs;
6. the explicit author decision and the exact reviewed commit are recorded;
7. `selfApproved = false`, `authorApprovedPhase = true` and
   `passClaimed = true` are mutually consistent;
8. no candidate evidence is presented as an execution on the closeout commit.

This mode verifies consistency of a decision that already exists. It cannot
create or infer author approval.

### 2. Technical cohort and source cohort remain distinct

An execution record belongs to the source/input cohort on which the process ran.
Git ancestry or equal trees do not rewrite that record's `repositoryCommit`,
`HEAD`, index or status. In particular:

```text
precommit execution on base HEAD plus candidate worktree
    !=
postcommit execution on the candidate commit
```

The byte equality between those two states is provenance: it proves what was
committed. It does not claim that the old process executed after the commit.
Fresh PHASE, COMPOSED and FULL results are required for the clean committed
cohort after the verifier infrastructure is repaired.

### 3. Narrow status-only evidence linkage

After a future exact technical commit `T` has its own successful PHASE,
COMPOSED and FULL executions, a later author-approved status-only descendant
`C` may link those technical results without repeating them only when all of
the following are established:

- the author explicitly identifies `T`;
- `T` is an ancestor of `C`;
- the complete raw input inventory and every executable/product/test/verifier/
  reference byte in `C` equal those in `T`;
- `T..C` contains only a fixed, exhaustive closeout allowlist;
- each changed closeout file satisfies a bounded status/approval-content rule,
  not merely a path-name rule;
- the PHASE/COMPOSED/FULL roots, canonical receipts, archived JUnit/Checkstyle
  records and input inventory are hash-linked to `T` and internally coherent;
- the closeout verdict states separately that technical verification executed
  on `T`, closeout consistency was checked on `C`, and executable payloads are
  equal.

Line-ending normalization is not accepted as raw equality for this evidence
link. Unknown, missing, renamed, untracked or extra inputs fail closed. A prior
receipt cannot activate a new same-process capability, and `-SkipBuild` remains
static, incomplete and non-acceptance evidence.

This is a narrowly reusable phase-closeout rule. It does not authorize generic
reuse across implementation commits, verifier changes, different environments,
or altered tests and references.

### 4. Top-level integration

The top-level `PHASE`, `COMPOSED` and `FULL` authorities select the applicable
phase lifecycle explicitly and retain the R1 verifier's live scientific
assertions. `COMMITTED_CANDIDATE` makes the phase callable after its technical
commit; it does not bypass build evidence. Changes to this integration are
verification-infrastructure changes and require a fresh clean FULL execution.

### 5. Current concrete provenance

The exact precommit cohort was frozen before any verifier edit:

- entry `HEAD`: `109f077fc5e2a40bcde45d3271eb928ee66fdfcc`;
- branch: `codex/g9s1-r1-spline-pair-materialization`;
- candidate paths: 76;
- original verifier raw SHA-256:
  `9285001b46961d4ace3ccbdd5ec0a7ee2848c1b67a2446280c96e7a5d299d62a`;
- complete raw input inventory: 11,195 entries, SHA-256
  `1921b8d34c7611f585a49026c8a5a4097933fe1290f321a5bfe3709e80c1d529`;
- frozen record SHA-256:
  `df89be1e312f7e91d1c9d2a50fa51316011d4841603ab7bbb4959b0c8c88afe8`;
- canonical R1 summary SHA-256, identical in both PHASE runs, COMPOSED
  and FULL:
  `424568ebd5a23dcc3b643586310ad4ed3f1f679725ac9cd79f40d45e06e9618b`.

The byte-exact candidate commit is
`f761758bd664504057413539b9729ba444c904c1`, with tree
`e9636950614d73d415e46e77bb32005c40b9d717` and parent
`109f077fc5e2a40bcde45d3271eb928ee66fdfcc`. All 76 committed blob IDs
equal their frozen raw blob IDs. This establishes provenance only.

Running the unmodified old verifier after that commit rejects the new `HEAD`
with `R1 candidate requires unchanged entry HEAD.` This is recorded as
`EXPECTED_VERIFIER_DOMAIN_EXIT`; no scientific tests were reclassified.

The verifier-contract commit and its fresh committed-candidate PHASE, COMPOSED
and FULL results do not yet exist at this documentary checkpoint. No future
PASS is claimed.

## Consequences

- Historical PRECOMMIT evidence remains reproducible under its exact contract.
- A committed candidate gains a branch-independent, fail-closed technical review
  state without permitting arbitrary descendants.
- A future author decision can be checked without falsely claiming a new FULL
  execution, but only under exact reviewed-SHA and raw executable-input identity.
- The lifecycle helper and top-level integration are verification infrastructure;
  `VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED` and fresh FULL evidence
  are mandatory before Stage I can stop.
- `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: the change adds no workstation,
  PowerShell, JDK, Gradle, Conda, download, packaging or toolchain prerequisite;
  it only authenticates lifecycle states already produced by the existing
  verification stack.
- `GUIDE_IMPACT = UPDATED`: the developer guide must explain the three modes and
  the difference between technical execution and author closeout.
- G9U1 design authority and implementation authorization are unchanged. This
  decision performs no G9U1 work.

## Rejected alternatives

| Alternative | Disposition |
|---|---|
| Remove or relax the old `HEAD`/branch assertions | Rejected; it would rewrite the historical PRECOMMIT contract. |
| Treat equal blobs as proof that precommit evidence ran postcommit | Rejected; byte provenance and execution identity are different claims. |
| Accept any descendant of the candidate | Rejected; it could hide productive or test changes. |
| Select committed mode from the feature-branch name | Rejected; branch placement is not durable candidate authority. |
| Allow closeout by path allowlist alone | Rejected; an allowlisted document can contain non-status semantic changes. |
| Let a prior receipt confer current-run authority | Rejected by ADR 0020; evidence linkage is documentary, not capability reuse. |
| Use `-SkipBuild` for closeout acceptance | Rejected; it remains static-only and incomplete. |
| Set approval flags during Stage I fixture testing | Rejected; synthetic fixtures test the contract without changing real phase authority. |

## Current approval boundary

```text
G9S1-R1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
selfApproved = false
authorApprovedPhase = false
passClaimed = false
```

Stage II is not authorized by this decision. It can begin only after a later
author instruction explicitly states `G9S1-R1 = PASS — AUTHOR APPROVED` and
names the exact reviewed technical commit produced by the completed Stage I.

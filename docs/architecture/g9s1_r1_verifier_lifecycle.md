# G9S1-R1 verifier lifecycle architecture

- Status: **Stage I infrastructure candidate — postcommit validation pending**
- Date: 2026-09-03
- Decision: [ADR 0023](../adr/0023-phase-verifier-lifecycle-and-author-closeout.md)
- Parent verification authority: [ADR 0020](../adr/0020-verification-levels-and-current-run-evidence.md)
- Phase: `G9S1-R1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`
- Product/kernel effect: none

## 1. Problem boundary

The R1 scientific candidate and its original verifier were developed in one
uncommitted working tree. The verifier binds the candidate to the entry commit,
branch, complete 76-path inventory, live scientific assertions and false
approval flags. A Git commit changes only the repository state, but that is
enough to leave the verifier's declared domain.

The lifecycle correction must preserve both truths:

1. the original verifier was correct to reject a different `HEAD`; and
2. the same frozen bytes need a safe committed-candidate verification state.

It must not change spline geometry, intersection semantics, tests, numerical
tolerances or R1 approval.

## 2. Cohort model

Four objects are kept separate:

| Object | Meaning | May it prove? |
|---|---|---|
| Source/input cohort | Raw files, Git state, index/status, environment and execution configuration | Exactly which inputs were present |
| Execution evidence | Root result, current-run receipt, archived reports/logs and phase summary | What ran, at which level, against that cohort |
| Technical candidate commit | Immutable commit/tree containing the reviewed candidate bytes | What was frozen into Git |
| Author decision | Explicit human approval naming an exact reviewed technical commit | Governance approval, never technical execution |

Equal candidate blobs across a precommit worktree and a commit prove faithful
freezing. They do not move the precommit run to a later `HEAD`. Likewise, an
author-closeout descendant may prove executable equivalence to its reviewed
technical parent without pretending that PHASE, COMPOSED or FULL executed on
the closeout commit.

## 3. State machine

```text
base HEAD + exact 76-path candidate worktree
    |
    | PRECOMMIT_CANDIDATE: exact legacy entry contract
    v
f761758bd664504057413539b9729ba444c904c1 (T1)
    |
    | old verifier rejects new HEAD: EXPECTED_VERIFIER_DOMAIN_EXIT
    | bounded infrastructure-only lifecycle repair
    v
R1_VERIFIER_CONTRACT_COMMIT (T2, current unpublished technical HEAD)
    |
    | COMMITTED_CANDIDATE + fresh PHASE/COMPOSED/FULL
    v
R1_REVIEWED_TECHNICAL_COMMIT (not yet established)
    |
    | STOP; later author decision must name this exact SHA
    v
future status-only closeout descendant
    |
    | AUTHOR_CLOSEOUT consistency check; no new FULL claim
    v
possible later publication under separately granted authority
```

No transition in this state machine implies approval. Only the explicitly
recorded author decision can activate the future closeout transition.

## 4. Mode A: `PRECOMMIT_CANDIDATE`

The mode preserves the original R1 verifier semantics as an immutable historical
entry profile:

- exact base commit and feature branch;
- exact candidate/blocking status;
- exact 76-path candidate set;
- live source, documentation, evidence, matrix and scenario hashes;
- exact 192-method R1 perimeter and mandatory anchors;
- live Checkstyle and JUnit assertions;
- prior-tag, ancestry, R6, G9U1-checkpoint and retained-risk assertions;
- false self-approval, phase-approval and PASS claims.

The legacy assertions are not generalized in place. A future repository state
can reproduce this mode only by recreating its exact entry cohort.

## 5. Mode B: `COMMITTED_CANDIDATE`

### 5.1 Candidate authentication

The mode takes an exact canonical candidate authority, not a branch name. For
R1 Stage I that authority is:

```text
base   = 109f077fc5e2a40bcde45d3271eb928ee66fdfcc
T1     = f761758bd664504057413539b9729ba444c904c1
tree   = e9636950614d73d415e46e77bb32005c40b9d717
paths  = 76
```

It verifies that T1 exists, has the authorized base ancestry, owns the exact
frozen blobs and is an ancestor of current `HEAD`. The canonical candidate
manifest must agree with constants or another immutable authority; a mutable
manifest cannot bless a different commit by itself.

### 5.2 Descendant boundary

The entire T1-to-current delta is enumerated. Only the bounded verifier-lifecycle
implementation, its fixtures and its operational documentation may follow T1
before the reviewed technical commit. Every productive Java/resource blob,
every scientific test, tolerance/reference and every one of the original 76
candidate blobs remains exact.

An additional allowed path is not automatically an allowed change. Files are
classified, their expected role is checked, and the lifecycle fixtures exercise
tampering and unknown-path failures. Current dirty/index state is also examined;
an uncommitted productive edit cannot hide behind committed ancestry.

### 5.3 Live verification

The committed mode still executes R1's scientific assertions over the current
checkout. Frozen T1 hashes authenticate the candidate; they do not replace:

- live authority/specification checks;
- exact test-method and scenario coverage;
- JUnit result checks;
- Checkstyle;
- canonical summary generation;
- top-level generated-state handling;
- PHASE/COMPOSED/FULL current-run evidence.

The selected branch has no semantic effect. This permits later placement on a
review or main branch without changing candidate identity, provided the exact
ancestry and payload contract continue to hold.

## 6. Mode C: `AUTHOR_CLOSEOUT`

### 6.1 Activation input

The mode requires an explicit parameter or canonical approval authority:

```text
REVIEWED_TECHNICAL_COMMIT = <full exact SHA named by the author>
```

It never resolves `HEAD`, “latest”, current branch or a tag as a substitute.
Stage I uses only isolated fixtures for this mode; the real R1 status remains
unapproved.

### 6.2 Delta proof

For reviewed technical commit T and current closeout C, the checker requires:

- ordinary ancestry T -> C;
- an exhaustive raw Git delta, including modes, additions, deletions and renames;
- a fixed allowlist of closeout/status authorities;
- bounded permitted content changes within each allowlisted file;
- no unknown tracked or nonignored input;
- raw equality of the executable closure, including product, tests, resources,
  build/verifier scripts, tolerances and numerical references;
- exact approval indicators and author-decision record.

Coordinates, canonical-LF equivalence, timestamps, branch names or filenames
alone cannot prove executable equality. Any changed executable byte invalidates
status-only reuse.

### 6.3 Evidence linkage, not replay

The technical evidence bundle for T pins:

- PHASE A/B root results and equal canonical R1 summaries;
- COMPOSED and FULL root results;
- current-run receipts and exact input fingerprints;
- archived JUnit and Checkstyle reports and their hashes;
- raw input inventory and source/index/status identity;
- required test-task outcomes, zero failure/error rules and only the declared
  inherited upstream skips.

The closeout checker validates these saved records as documentary evidence. It
does not install them into the process-local current-run capability and does not
describe them as newly executed. Its verdict has three separate statements:

```text
technical verification executed on T
author closeout checked on C
executable payload C == T
```

Unsafe paths, missing records, a failed root paired with a successful receipt,
tampered XML/logs, wrong level/commit, cached or skipped mandatory tests, or a
relocated bundle that cannot be validated all fail closed.

## 7. Top-level orchestration

`tools/agent/verify.ps1 -Level PHASE -Phase G9S1-R1` must select committed mode
from explicit canonical lifecycle state once T1 exists. It must not inspect the
branch name to select the mode. COMPOSED and FULL use the same mode while
retaining every R1 phase assertion after their canonical build.

The shared current-run receipt contract from ADR 0020 is unchanged. A valid
same-process receipt may avoid duplicate launches, but the R1 focused verifier
still checks its own exact test cases and non-test assertions. `-SkipBuild`
cannot satisfy technical acceptance or author closeout.

## 8. Fixture perimeter

The bounded infrastructure tests must cover:

| Mode | Positive cases | Required negatives |
|---|---|---|
| PRECOMMIT | exact base/branch/candidate/false flags | wrong HEAD; approval flags present |
| COMMITTED | exact T1 ancestry and unchanged candidate payload | wrong SHA; missing ancestry; productive descendant; premature approval; tampered manifest/hash |
| AUTHOR_CLOSEOUT | exact reviewed T, approval-only content delta, executable equality | product/test/verifier/tolerance/reference/unknown-path changes; wrong/nonancestor T; missing approval; candidate evidence alone |

Evidence-bundle fixtures additionally mutate roots, receipts, input inventory,
archived JUnit/Checkstyle data and safe paths. The positive AUTHOR_CLOSEOUT
fixture is synthetic and must not alter real R1 approval files.

## 9. Current checkpoint and pending work

The unmodified precommit verifier completed:

| Run | Result | R1 summary |
|---|---:|---|
| PHASE A | 192/192; exit 0 | `424568ebd5a23dcc3b643586310ad4ed3f1f679725ac9cd79f40d45e06e9618b` |
| PHASE B | 192/192; exit 0 | same |
| COMPOSED | 1,281/1,281; exit 0 | same |
| clean FULL | 7,770 passing, 11 inherited upstream skips, zero failures/errors; exit 0 | same |

All ran with repository commit recorded as the base `109f077f...`, against the
candidate worktree. T1 then committed all 76 frozen raw blobs exactly. The old
verifier's immediate unchanged-entry rejection is the expected boundary proof.

T2 includes the bounded lifecycle repair. Its first postcommit COMPOSED attempt
proved the scientific and lifecycle assertions but exposed one further
operational input: the baseline commit-range whitespace check inherited the
caller's `core.whitespace` policy and interpreted validated CRLF source bytes as
trailing blanks. T2 therefore also binds that check explicitly to Git's normal
blank checks plus `cr-at-eol`, with a fake-first control proving that real
trailing spaces still fail. Fresh committed-candidate PHASE, COMPOSED and FULL
remain pending after that bounded correction. Until all complete, no
`R1_REVIEWED_TECHNICAL_COMMIT` exists and no claim about their outcome is
permitted.

## 10. Operational impact

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = UPDATED
```

The bootstrap conclusion is substantive: no PowerShell minimum, JDK/Gradle
selection, Conda role, dependency acquisition, environment variable, packaging
input or workstation preflight changes. The existing tools execute the same
scientific perimeter; only lifecycle authentication changes. The developer guide
documents how operators select and interpret the three modes.

No G9U1 planning or productive path belongs to this architecture. The protected
G9U1 design checkpoint remains unchanged and G9U1 implementation remains not
authorized.

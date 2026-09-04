# ADR 0024: Verification input identity across runs and Git checkouts

- Status: **PASS — AUTHOR APPROVED** under the author's explicit conditional operational authorization
- Date: 2026-09-04
- Scope: input identity, provenance and cross-checkout closeout validation only
- Product phase effect: `NONE`; scientific contract changed: `false`
- Self approval: `false`
- Governing contract: [verification levels, sections 10–11](../../geocedg/specs/operations/verification-levels.md#10-phase-candidate-and-author-closeout-lifecycle)
- Evidence: [operational report](../validation/verification_input_identity_report.md)

## Context and preserved chronology

The author-approved G9S1-R1 reviewed technical cohort is
`a38d4fcde846fc97c51abc8d958de6998302c436` (`T`). Its separate status-only
closeout is `af459d856f1cdc384805f3035203acce8e6f6104` (`C`). The real
AUTHOR_CLOSEOUT checks passed before and after the closeout commit on the
technical branch, then failed after checkout on main: `verify-baseline.ps1`
had a different physical LF/CRLF representation while its versioned Git content
was unchanged. The failed check remains failed historical evidence.

[ADR 0020](0020-verification-levels-and-current-run-evidence.md) correctly uses
raw physical identity to protect one active execution from concurrent mutation.
[ADR 0023](0023-phase-verifier-lifecycle-and-author-closeout.md) reused that
notion for a later checkout. The latter conflated repository identity with
physical materialization. Its historical raw cross-checkout equality rule is
narrowly superseded here; its author decision, ancestry, complete allowlist,
content, hash linkage and no-self-approval requirements remain.

This new rule is explicitly authorized by the author in this task. It is not
claimed to have existed before this correction, and no historical run is
relabelled under the new rule.

## Decision

### Three authorities

`RUN IDENTITY != REPOSITORY IDENTITY != WORKTREE MATERIALIZATION VALIDITY`.

1. **Repository identity:** an exact commit tree maps every tracked repository
   path to Git mode and blob OID. The complete `T..C` tree delta must equal the
   approved closeout allowlist and content transformation; all other paths,
   modes and blobs remain unchanged. Renames, deletions, additions and mode-only
   changes are real deltas, not hidden by content normalization.
2. **Same-run identity:** preserve complete raw physical inventories, loaded
   module identity, HEAD/index/status, environment and external configuration,
   checks after module execution/before sealing/at consumption/completion, and
   process/runspace-local capability ownership. Raw LF/CRLF mutation during one
   run remains mutation even if Git would consider the result clean.
3. **Materialization validity:** an explicitly named target checkout must have
   the exact expected index tree and Git-clean tracked working files. Audit
   effective attributes/configuration; permit only supported native text/EOL
   and binary contracts, and reject unsupported effective custom filters,
   encoding or other transformations before trusting or executing them.

No repository-wide line-ending rewrite or `*.ps1 eol=lf` workaround is part of
this decision. Git-clean LF and CRLF copies may have distinct archived raw
hashes. That fact is reported, not erased.

### Input partitions and source cohorts

Tracked cross-checkout identity uses Git, consumed untracked inputs retain
exact raw hashes and exhaustive membership, ignored generated state retains
the existing explicit generated-state contract, and external build/JVM inputs
retain their environment/configuration fingerprint contract. An unsupported
input class fails closed.

Archived raw inventories continue proving the physical cohort actually run.
Bundle hashes, roots, receipts, JUnit/Checkstyle records and original input
fingerprints are authenticated without rewriting any of them. Archived receipts
never become live current-process build capabilities.

### Exact closeout targets, independent inspection code

Cross-checkout verification names both `ReviewedTechnicalCommit=T` and
`CloseoutCommit=C`. Policy is frozen in `T`; the decision record and bounded
approved content are read from `C`. `T` must be ancestral to `C` and the entire
delta must satisfy that frozen policy. Newer operational inspection code may
inspect those historical targets in an independent clean target repository.
Its own commit/worktree is separately identified and is not substituted for
either target. “Latest”, branch location or a not-yet-created tag is not authority.

The resulting claim is precisely:

```text
technical execution = T
author decision / closeout target = C
non-closeout tracked executable payload = Git-identical
target materialization = supported and Git-clean
inspection code = separately recorded operational cohort
```

### Published phase regression is a live execution

Normal PHASE/COMPOSED/FULL regression on a later product or operational
descendant is not AUTHOR_CLOSEOUT. G9S1-R1 uses `PUBLISHED_REGRESSION` with
the explicit operational authority naming `T` and `C`. It authenticates the
frozen policy, exact closeout content/record and ancestry, then retains every
live scientific/source/scenario/JUnit/Checkstyle assertion and current-run raw
receipt check. Branch names do not select identity. Historical candidate paths
describe the original R1 scope, not unrelated later changes.

This context does not require a historical build bundle to run new regression,
does not claim historical evidence reuse, and does not claim the newer product
cohort equals `T`. Later authorized product changes must pass the live
regression normally; they cannot consume the historical author decision as a
build receipt. The documentary exact-target closeout proof remains separate.

### Narrow evidence-preserving verifier-repair exception

The author also authorizes the exact fifteen-condition rule in
[verification levels section 11.2](../../geocedg/specs/operations/verification-levels.md#112-narrow-evidence-preserving-verifier-repair).
The exception applies only to changed input-identity/provenance/closeout
functions. Product/UI/Java/scientific tests, tolerances/references, build and
Gradle declarations, task/filter selection, JVM/toolchain requirements,
numerical commands, JUnit acceptance and generated-state execution lifecycle
must remain unchanged.

The execution-plan/impact proof must compare actual execution authorities,
not merely file names: module roots, tasks, filters, style requirements,
numerical/reference commands, toolchain and environment/system-property policy,
and result acceptance. Unclassified executable changes fail the exception.
Successful sealed heavy evidence must remain hash-bound to its original cohort.
New focused identity/lifecycle tests must pass twice deterministically, and
bounded real shared and Desktop canonical integration plus the actual R1
cross-checkout case must pass.

Only when every condition is established may old heavy scientific execution be
linked to the new focused infrastructure evidence without a redundant heavy
campaign. If any condition fails, the normal mandatory levels including FULL
apply. This neither waives coverage nor permits a stale runtime receipt.

## Required negative evidence

Fixtures cover tracked blob/mode/path changes, unauthorized additions, wrong
index, dirty tracked content, consumed-untracked mutation, unsupported effective
filters/encoding, manifest tampering and wrong exact commits. Positive controls
cover LF/CRLF, `core.autocrlf=true/false/input`, explicit text EOL attributes,
binary inputs, modes where supported and actual R1 T/C history. Actual horizontal
trailing whitespace remains rejected under the earlier whitespace policy.

## Consequences and scope boundary

- The R1 phase tag continues to identify `C`, not a later operational successor.
- The original failed checkout check remains preserved and is not retrospectively
  called a passing run.
- No fourth R1-specific infrastructure allowance or rewritten R1 approval commit
  is needed; this is a separately bounded operational descendant.
- This decision creates no product phase or product phase tag.
- G9U1 work is a separately gated subsequent task scope; this operational repair
  itself adds none. The R4 periodic-quarantine risk stays OPEN / TRACKED.
- `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: no PowerShell/JDK/Gradle/Conda,
  wrapper layout, packaging prerequisite, numerical command, download or
  generated-state execution policy changes. Git materialization support is
  characterized using the existing Git prerequisite, not a new workstation
  installation requirement.
- `GUIDE_IMPACT = UPDATED`: developers must distinguish live raw identity from
  durable Git authority and invoke exact targets for cross-checkout closeout.

Every gate of the author's bounded contract is established in the linked
report: two complete focused executions pass 270/270 with identical canonical
summary `cc3b6352518e913eda52c0419c4185f265a673c3fbf4786ff3ec1ee36b424307`,
the exact execution-plan projection is unchanged, both actual LF/CRLF closeout
proofs pass, and real shared/Desktop integrations pass. The authenticated old
PHASE/COMPOSED/FULL executions remain bound to `T` and were not repeated or
relabelled. `EVIDENCE_PRESERVING_VERIFIER_REPAIR=true` records the conjunction
of all fifteen requirements, not a success Boolean supplied by one fixture.
This operational status is authorized by the author's conditional decision;
`selfApproved=false`, `productPhaseEffect=NONE`, and
`scientificContractChanged=false` remain unchanged. This final status/report
adjustment follows the focused executions and changes documentation only.

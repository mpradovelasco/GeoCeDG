# PRE-G9B-R2-E0/E1 closeout record

```text
PRE-G9B-R2-E0/E1 = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
```

This record preserves an explicit author disposition and the exact evidence
identities behind it. It changes no executable, test, build, product,
verification or prompt file, and it does not modify the frozen technical
candidate.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the candidate's own artifacts record only `TECHNICAL_CANDIDATE_STATE = FROZEN`
and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole
authority for current author approval. Every statement in the candidate's
artifacts stands unchanged.

## Approved technical candidate

```text
commit = 60cb1a2aa14cec3b700b492f94ba01ad449ba962
tree   = a3f4778df434e61f4238727c1f1cc49461d28852
parent = 3d13ea72f64c241d11759617dbdff70e373e6a50
base   = a222b82aad821e3e0c0aa93145a57b7050f024c8
```

The approved candidate is the bounded corrective descendant of the frozen
combined candidate `3d13ea72f64c241d11759617dbdff70e373e6a50` (tree
`22bd3794c91b4432771e34ff143879b211fad006`). Both are preserved unchanged. Their
reports are the
[combined candidate report](pre_g9b_r2_e0_e1_candidate_report.md) and the
[corrective candidate report](pre_g9b_r2_e0_e1_corrective_candidate_report.md).

## Accepted content

The author's approval covers the combined design and implementation as finally
reconciled:

- generalized semantic metric-endpoint admissibility;
- capability-based exact-token pair materialization;
- `D-C1`…`D-C5` as finally reconciled, including the `D-C2` clarification;
- versioned structural derivation addresses;
- transformation covariance under a proven common semantic transformation;
- copy/remap of the driver identity and other structural participants, which
  resolves the predecessor's retained limitation
  `PAIR-COPY-GENERATOR-PARAMETERIZATION-CONTRACT`;
- the preserved rule "local admissibility != global completeness";
- fail-closed treatment of unsupported or ambiguous cases.

## Accepted evidence

The author determined the existing `PHASE` and `INTEGRATION` evidence on the
exact approved candidate sufficient; no further verification is run for this
closeout.

| Campaign | Run | Plan hash | Result hash | Verdict |
|---|---|---|---|---|
| `PHASE -Phase G9S1-R1` | `verification-faad72ceb5bc4c22aadf4268b5ddc373` | `4cb0ecf5caa86cc1d26d65a0673c352d83b83ed20de706ee23166e942e0ad430` | `0244c60c0a46b2850edd3b35eeec3d1b25e05dc10383c323e805bb41f277cc25` | `ACCEPTED / COMPLETE`, 13/13 |
| `INTEGRATION` | `verification-0370524a1e244559818a964777485f77` | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` | `e8421a691d9509fe0a03c3e2ac13e05af5d0d24a273af8210a65b1deb574e4c8` | `ACCEPTED / COMPLETE`, 19/19 |

Both runs are bound to the approved candidate commit and tree, with zero
untrusted and zero not-run checks. `PHASE -Phase G9U0-R6` resolved to the same
checks as `PHASE -Phase G9S1-R1`; it was recorded as cohort-equivalent and not
rerun (`OBS-R2-PHASE-SELECTION-COHORT`). `FINAL` was not required by the frozen
`INTEGRATED_PHASE` class and was not run. The `INTEGRATION` diagnostic finding
and the unavailable diagnostic are byte-identical to those of the predecessor's
accepted run. The receipts are kept locally under the untracked
`artifacts/agent/pre-g9b-r2-e0-e1-corrective-phase-g9s1-r1/` and
`artifacts/agent/pre-g9b-r2-e0-e1-corrective-integration/`.

### Historical evidence of the frozen predecessor

These runs bind `3d13ea72f64c241d11759617dbdff70e373e6a50` and keep their
original classification.

| Campaign | Run | Result hash | Verdict |
|---|---|---|---|
| `PHASE -Phase G9S1-R1` | `verification-da004dcac4a0432aaadfc13d1bfa3973` | `fa967cdad72399f4bfdb03d1dd09bb3cdf6f118b1abc2e1a2bd84da04dc3e786` | `ACCEPTED / COMPLETE`, 13/13 |
| `INTEGRATION`, attempt 1 | `verification-d38c2e6a822c4ed5baeeb41b47c0aa5b` | `a046f43ae24b44e15054bb527ff0ff0d5c7212b749febf3c82b4e7f6d845d2ac` | `REJECTED_VERIFICATION_CORE / UNTRUSTED`: `packaging.repository-safety` untrusted, an operator error (a console log held under `artifacts/`) |
| `INTEGRATION`, attempt 2 | `verification-9be6d50510f24bdcbcd438bc793acdc2` | `d61a14830dac217e7530526f50abe16e560f5f6f3284bfd5c4642e53b123a5d9` | `ACCEPTED / COMPLETE`, 19/19 |

## Retained quality debt

Retained explicitly as unresolved, as the author directs.

| Identifier | Statement | Current consequence |
|---|---|---|
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | Pre-existing defect: segment-driven reconstructible loci may evaluate one driver step late and therefore produce incorrect metric values. | The certified model fails closed: its floating verification refuses every affected root (`PAIR#segmentDriverInhomogeneousReadIsRefusedByTheCoherenceGate`). The candidate reports name it `RECONSTRUCTIBLE-SEGMENT-DRIVER-INHOMOGENEOUS-LAG`. |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | Pre-existing defect: some chained constructions containing unnamed inline literal geometry lose the required dependency in the reconstructible evaluator and may evaluate as a wrong fixed locus. | Capture refuses the affected slice (`MODEL#theInlineLiteralDefectShapeIsRefusedRatherThanCertified`). The candidate reports name it `RECONSTRUCTIBLE-INLINE-LITERAL-SLICE-DISCONNECTED`. |
| `TD-UPSTREAM-RECORD-E0-E1-PURPOSE` | Bounded documentation/provenance debt: four existing `docs/upstream/modified-files.yml` entries do not yet describe their E0/E1 purpose completely. | The entries exist and the upstream boundary check passes; only their purpose prose is incomplete. The frozen candidate is not mutated to repair it. |

Both evaluator defects were reproduced on the pre-E0/E1 base
`a222b82aad821e3e0c0aa93145a57b7050f024c8`; neither is an E0/E1 regression. The
certified model must continue to fail closed for the affected shapes until they
are repaired.

The four entries of `TD-UPSTREAM-RECORD-E0-E1-PURPOSE` are:

- `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionSolver2D.java`
- `source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1R1SplinePairMaterializationTest.java`
- `source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0IntersectionTokenTest.java`
- `source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R4IntersectionAdmissibilityContinuationTest.java`

## Ri quality-debt policy

Recorded as the author directs, for every subsequent `PRE-G9B-R` phase:

1. At the entry of each subsequent `PRE-G9B-R` phase, inspect whether its
   authorized footprint materially overlaps one or more retained quality debts.
2. A debt may be proposed or executed as a bounded synergistic quality
   refinement within that phase only when it
   - touches the same subsystem already being modified;
   - can be resolved without widening the phase's scientific purpose;
   - can share the phase's already-required verification campaign;
   - and does not introduce a new architectural decision.

   The refinement is recorded explicitly, never as a silent scope expansion.
3. No separate heavy campaign is created merely to clear such debt.
4. If no efficient overlap occurs, the debt is retained unchanged.
5. Every retained debt must have a terminal disposition or an explicit owner by
   the `PRE-G9B-R7` closeout.

## Other retained items

Carried forward unchanged from earlier closeouts, so that the debt ledger stays
complete for rule 5.

| Item | Class | State |
|---|---|---|
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | retained pre-existing baseline defect | open; unchanged by E0/E1 |
| `OBS-R2-PHASE-SELECTION-COHORT` | verification observation | open; it recurred here, since `PHASE -Phase G9U0-R6` was recorded as cohort-equivalent and not rerun |
| `GUIDE-A1-SECTION-9.3-STALE` | pre-existing documentation drift, owned by `POST-G9U1-A1` | The approved candidate rewrote §9.3 of both guide editions to the A1 behaviour, pinned by `END#userGuideWorkedExampleEndpointsHoldAsDocumented`. No separate author disposition is recorded for this identifier. |

## Promotion

The author authorizes promotion of the exact closeout descendant of the
approved candidate to `main`, by ordinary clean fast-forward only: no rebase,
amend, merge commit, force push, tag or release.

Remote identity before promotion:

```text
local main       = a222b82aad821e3e0c0aa93145a57b7050f024c8
origin/main      = a222b82aad821e3e0c0aa93145a57b7050f024c8
live remote main = a222b82aad821e3e0c0aa93145a57b7050f024c8
tree             = 0768848e89961f0f372f514530f9e9e0b81cc872
```

The identity after promotion is this record's own commit, which the record
cannot name; it is reported with the promotion.

## Authorization state

`PRE-G9B-R3` is authorized by the author to start only after this closeout is
promoted and all `main` refs agree, under its canonical prompt. `R4` and every
later `PRE-G9B-R` phase, `G9B`, `G9C`, `G9U2` and productive `G10` remain
unauthorized. Repair of a retained debt is authorized only as the Ri policy
allows. No tag or release is authorized.

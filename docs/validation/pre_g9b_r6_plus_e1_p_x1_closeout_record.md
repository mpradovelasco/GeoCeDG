# PRE-G9B-R6-plus-E1-P-X1 closeout record

```text
PRE-G9B-R6-plus-E1-P-X1 = PASS — AUTHOR APPROVED
AUTHOR_REVIEW           = PASS
AUTHOR_SMOKE            = NOT REQUIRED — NO PRODUCT BEHAVIOR CHANGE
OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION = RESOLVED IN PRE-G9B-R6-plus-E1-P-X1 — AUTHOR APPROVED
FINAL.DESKTOP           = CLEAN
```

```text
selfApproved              = false
authorApproved            = true    (E1-P-X1 only, candidate T_R6PLUS_E1_P_X1)
passClaimed               = true    (E1-P-X1 only)
implementationAuthorized  = false   (E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B and
                                     every other open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE             = NONE
PACKAGING PRODUCT CHANGE   = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
SERIALIZATION CHANGE       = NONE
DOCUMENT FORMAT CHANGE     = NONE
KERNEL CHANGE              = NONE
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout)
```

This record preserves the explicit author disposition of 2026-10-07 on
`PRE-G9B-R6-plus-E1-P-X1`, given in writing in the session ("Autorizo el
siguiente prompt"), with the exact identities behind it. It changes no product,
test, build, packaging, verifier, registry, inventory, schema, specification,
ADR or upstream file and does not modify any frozen commit. Besides this record
and its machine-readable mirror,
[`pre-g9b-r6-plus-e1-p-x1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-p-x1-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_e1_p_x1_candidate_report.md) and its
evidence mirror record only frozen state and are not amended. The `E1-L`,
`E1-P`, `E1` and `E1-X1` closeout records are not rewritten, and none of those
phases is reopened. This record is the sole authority for the author approval
of `E1-P-X1`.

## Entry verification

```text
live remote main = origin/main = local main
                 = 7d132ec5696535e18419e481b8306b5aecf3fef6   (P_R6PLUS_E1_X1)
                   tree 7509dcb05117eca87011e2bb82ac481592afbcad
branch           = phase/pre-g9b-r6-plus-e1-p-x1-stale-assertion, clean
history          = 7d132ec5 → 85e0aa4a4442e2951013e76c75b3617752778654 (test reconciliation)
                            → 44b90156786bce4ec5fca0466e7b8f81ea5bb358 (frozen candidate)
                   linear; no commit amended, squashed or rewritten
```

## Author disposition

```text
T_R6PLUS_E1_P_X1 = 44b90156786bce4ec5fca0466e7b8f81ea5bb358
                   tree e02daecdd86e8d791e0f00fd6e49d684d2ca0fd6
AUTHOR_REVIEW    = PASS
AUTHOR_SMOKE     = NOT REQUIRED — NO PRODUCT BEHAVIOR CHANGE
AUTHOR_APPROVAL  = PRE-G9B-R6-plus-E1-P-X1 = PASS — AUTHOR APPROVED
selfApproved     = false
```

The author accepts the candidate without a separate product smoke because it
is a verification/test reconciliation only, with no product, packaging,
geometric, serialization, document-format or kernel change.

## Observation disposition

```text
OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION = RESOLVED IN PRE-G9B-R6-plus-E1-P-X1 — AUTHOR APPROVED
```

Historical meaning:

- `E1-L` originally and correctly asserted that `E1-L` itself did not own
  packaging;
- `E1-P` later and legitimately introduced the curated GGT packaging route;
- two `E1-L` assertions of
  `PreG9BR6PlusE1LCuratedLibraryTest.e1l09` therefore became stale (the builder
  "no `ggt-library`" clause and the specification "`.ggb`/`.ggt` models"
  clause);
- `E1-P` product behavior was correct; the stale test expectations, not the
  product, were wrong after `E1-P`.

## Reconciled contract

```text
E1-L: owns curated GGT content and runtime catalog semantics
E1-P: owns the explicit controlled packaging admission route

models/curated/ggt-library/  →  app/ggt-library/
under the normative manifest / hash / rights / profile contract
```

The reconciled test, with its JUnit identity kept, still preserves: no `.ggt`
embedded in ordinary `src/main/resources`; no Gradle ownership of
`models/curated`; no hidden alternate packaging authority (the normative
packaging specification owns the route, every other `.ggt` fails, `.ggb` stays
forbidden, `COMMERCIAL` excludes the library). It no longer rejects the
explicit `E1-P` route.

## Accepted evidence

```text
focal       PreG9BR6PlusE1LCuratedLibraryTest 9/9; PreG9BR6PlusE1LBundledCatalogTest 10/10
adjacent    PHASE PRE-G9B-R6-plus-E1-P
            verification-ebf28a3edc8a45ca9f16cc9e2fc23945, ACCEPTED / COMPLETE, 2/2
global      final.desktop on 85e0aa4a4442e2951013e76c75b3617752778654
            1 874 tests, 0 failures, 1 allowed skip
            (org.geogebra.CasValueExtractor.extractMockValues, allowlisted)
            = clean global Desktop baseline restored by E1-P-X1

PHASE PRE-G9B-R6-plus-E1-P-X1 on T_R6PLUS_E1_P_X1
            verification-1a8e1e045dc44fd2a979389710637e12, ACCEPTED / COMPLETE, 4/4
            plan   9016d9f6022ee7ea6dc0d30537a89849172fb8cdaccf1393eb7970b37870aa8d
            result c2ec8beefadf25b4c6a14a8e23b2fc12a40819f0dbb670d29bd119944507a824
INFRA_UNIT  verification-3e6e9c68025c4d0989ad75936ae9786e, ACCEPTED / COMPLETE, 22/22
STATIC      verification-929e10a453db43129f210dccc3a2f850, ACCEPTED / COMPLETE, 3/3
            (standing governance FINDING and historical-consistency UNAVAILABLE)
git diff --check 7d132ec5..44b90156 = CLEAN
```

**`final.desktop` provenance.** The clean `final.desktop` producer ran on
`85e0aa4a`, which contains the actual test correction. The frozen candidate
`44b90156` then added only verification registration and inventory, canonical
pins derived from clean evidence, candidate evidence and reporting, and roadmap
and mini-track status; no product source or corrected test logic changed after
`85e0aa4a`. The author therefore accepts that run as valid evidence for the
frozen candidate, together with the `PHASE`, `INFRA_UNIT` and `STATIC` runs
bound directly to `44b90156`. `final.desktop` is not rerun for this closeout.

## Inventory reconciliation

```text
final.desktop expected identities  1 855 → 1 874
discovery.desktop identities       1 880 (unchanged)
pre-g9b-r6-plus-e1-p-x1.desktop    19 (new phase selection)
junit_inventory pin                5de49549dd9d82b81ce75ff60b742e6e49c840231dfe627e5d67355edf7e5a55
                                 → eded218f82d1ae5d1df4456ec11c7794d0b522c65c7365452c65069f74c5ad79
registry shape                     42 selections, 50 PHASE selections
```

The closeout neither moves nor invents any pin.

## Historical phase states

```text
PRE-G9B-R6-plus-E1-L  = PASS — AUTHOR APPROVED — PUBLISHED   (unchanged)
PRE-G9B-R6-plus-E1-P  = PASS — AUTHOR APPROVED — PUBLISHED   (unchanged)
PRE-G9B-R6-plus-E1    = PASS — AUTHOR APPROVED — PUBLISHED   (unchanged)
PRE-G9B-R6-plus-E1-X1 = PASS — AUTHOR APPROVED — PUBLISHED   (unchanged)
```

No `FINAL` or `INTEGRATION` was run for this closeout. The closeout commit is a
documentary descendant of `T_R6PLUS_E1_P_X1`, not a replacement candidate; its
`STATIC` run and the publication are reported outside this record, which
cannot name its own commit.

## Next activity

With publication, the verification baseline is clean. The next mini-track phase
is `PRE-G9B-R6-plus-E2`, which may proceed only under a separate explicit author
instruction:

```text
NEXT = PRE-G9B-R6-plus-E2 — NOT AUTHORIZED
```

No implementation authorization for `E2` is implied by this closeout, and `E2`
is not started.

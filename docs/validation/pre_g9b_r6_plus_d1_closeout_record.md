# PRE-G9B-R6-plus-D1 closeout record

```text
PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED
AUTHOR_SMOKE       = PASS
```

```text
selfApproved              = false
authorApproved            = true    (D1 only)
passClaimed               = true    (D1 only)
implementationAuthorized  = false   (A-2, C, E1, E2, E3, F1, F2, G, PRE-G9B-R7, G9B,
                                     the rollback correction, the File/Insert debt)

candidateFrozen           = true
candidateMutated          = false
SERIALIZATION CHANGE      = <geocedgUnits> element per unit-system v1.0 §8
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout)
```

This record preserves the explicit author disposition of 2026-10-03 on
`PRE-G9B-R6-plus-D1` and the exact identities behind it. It changes no product,
test, build, packaging, verifier, registry, schema, specification or ADR file,
and it does not modify either frozen candidate. Besides this record and its
machine-readable mirror,
[`pre-g9b-r6-plus-d1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_d1_candidate_report.md), its evidence mirror
and the canonical prompt record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. They are not amended. This
record is the sole authority for the author approval of `D1`.

## Author disposition

```text
PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED
T_R6PLUS_D1        = 6e1d845902f534b9d45b533ac265089cbc01e689
                     tree 3eb4ca454f22fb596fc1972552db2ab9e359d24b
AUTHOR_SMOKE       = PASS
selfApproved       = false
```

The author instruction of 2026-10-03 approves the exact revised technical
candidate with author smoke `PASS` and authorizes exclusively this documentary
closeout and the publication of the complete `D1` chain. The final smoke
verified, in particular, the defect that had blocked the first approval: copy
from a document with construction unit `mm`, File → New or Open of a document
with construction unit `cm`, paste; coordinates and the target unit preserved;
the non-modal notice visible in the status bar; the full text available in its
tooltip when the window is narrow.

## Approved identities

```text
P_R6PLUS_D0         = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c
                      tree 83b32b6cb579dc28162f990160c3eef0b1928be6
                      published D0 closeout; publication base
T_R6PLUS_D1_PROMPT  = 3fb7542af3ef36db150291ca77e03b0bf4b6f888
                      tree de71421e6adbd9a9b5e6acc8051dd5154f6a5f63
                      preparation package; implementation branch start
A_R6PLUS_D1_PROMPT  = a246d74d1999a0018899c40157e939845babe91e
                      tree eae86c2b2d791d979bb761a69f1497e4c5ec07eb
                      canonical prompt AUTHORIZED (blob 008c397c55d34401dda645969281f2eef76d3937)
T_R6PLUS_D1_R1      = b55aa8173045d6d6bf735e70eb22302979b2b26a
                      tree b07fad69c73f226ed1922465ac9c358ce5979a6a
                      first technical candidate; superseded for acceptance
T_R6PLUS_D1         = 6e1d845902f534b9d45b533ac265089cbc01e689
                      tree 3eb4ca454f22fb596fc1972552db2ab9e359d24b
                      parent b55aa8173045d6d6bf735e70eb22302979b2b26a
                      approved revised technical candidate; not amended

branch              = phase/pre-g9b-r6-plus-d1-unit-system (local until publication)
```

The identity of this closeout commit, `P_R6PLUS_D1`, is its own commit, which
the record cannot name. It is reported with the publication.

## Accepted evidence

The frozen class `GLOBAL_IMPACT` required exactly one `FINAL` on the exact
candidate commit and tree.

| Run | Commit / tree | Result |
|---|---|---|
| `verification-9cf1a0af85fd42abada05105aacf3d89` — `verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r6-plus-d1-remediation-final-6e1d845` | `6e1d8459…` / `3eb4ca45…` | exit 0; `ACCEPTED / COMPLETE`; 41/41 acceptance leaves; plan `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517`; result `9b1f8224481b84351fef903f93d034342604a7b5a7f057fd387c6edfadb0b338`; receipt `bbbdb5b32f66e36f392ce4c87520b52c5357a67dfafdc8f9eec9f810b1b2a903`; standing diagnostics only (`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`) |

`tools/agent/phase-closeout.ps1 -Action INSPECT` on that receipt and candidate:
exit 0, `identity_confirmed = true`, no mismatch, no verification executed, no
repository mutation. `git diff --check` over `3268f9b9…..6e1d8459…`: exit 0.

Historical evidence of the superseded first candidate, not acceptance evidence
for the approved candidate: `verification-c9c0501a068a40738a3a9333b27dcb50`
(`FINAL`, `ACCEPTED / COMPLETE`, 41/41, receipt
`29de0ba1ee85c3652fcce9f96810706935b74949f50a10201ea110100f284035`) on
`b55aa817…` / `b07fad69…`.

## Accepted deviation

```text
ACCEPTED DEVIATION — PHASE-LOCAL APPROVED-ACTION GATE AMENDMENT
```

`tools/agent/workspace-profile-validation.ps1` lists `document.units` among the
approved actions of the live profile. The author accepts it because the same
mechanism already carries explicit `R4`, `A-1` and `B` amendments, `D1`
introduces an author-approved product action, the update does not change the
general semantics of the verifier, it requires no reclassification of
`GLOBAL_IMPACT`, and the final candidate passed `INFRA_UNIT` and `FINAL`. This
acceptance is not an authorization for any other verifier change.

## Smoke-remediation disposition

The initial absence of the paste notice is closed. Accepted root cause:
provenance, the physical comparison, the paste hook and the generated notice
were correct; the status bar's `FlowLayout` placed the notice in a second row
that the one-row status-bar slot cut off entirely in narrow windows, especially
with Spanish text. The single-row layout with the notice elided to the remaining
width and its full text in the tooltip is accepted. The incident requires no
further change.

## Textual precision on "km"

The remediation section of the candidate report quotes the status bar as
`Unidad de presentación: km`. The author's smoke document `Vacio.cedg`
defines a custom unit `usm` with `usmMetersPerUnit="1000.0"`, name `kilometers`
and symbol `km`, and the status bar names `usm` by its user-defined symbol
(`UnitState.symbolOf`, `UsmDefinition.displaySymbol()`). The disposition
confirmed by the author is:

> "km" in the remediation report is the user-defined symbol of the document's
> custom unit (`usm`, 1 usm = 1000 m, name "kilometers"). The product displayed
> it correctly by its symbol. It is not a built-in unit and does not extend the
> v1 unit set, which remains exclusively `mm`, `cm`, `m` and `usm`.

The frozen candidate report is not modified.

## Observations and recorded debt

| ID | Observation or debt | Disposition |
|---|---|---|
| `OBS-D1-ROLLBACK-CONSPROT-GUI` | After a failed native Open, the live GUI XML gains the construction-protocol columns, protocol and navigation-bar elements of the rejected file; the construction itself is restored exactly. The scratch characterization reproduced it on `3268f9b9…`; its A+B+C proposal and tests are evidence for a future independent authorization. | `PRE-EXISTING — CHARACTERIZED — NOT CAUSED BY D1 — NOT D1 BLOCKING — IMPLEMENTATION NOT AUTHORIZED` |
| `DEBT-R6PLUS-FILE-INSERT-SURFACE` | See below. | `PENDING SCHEDULING / RECONCILIATION — IMPLEMENTATION NOT AUTHORIZED`; must be closed before the global closeout of `PRE-G9B-R6-plus` |

The other observations of the candidate report (`OBS-D1-DEFAULTS-SAVE-RELEVANT`,
`OBS-D1-INSTALLED-PREFERENCES-IN-TESTS`, `OBS-D1-INHERITED-HEADLESS-LOGGER`,
`OBS-D1-ANIMATING-DOCUMENT-RETENTION` and the older-reader limitation of
`DQ-D1-10`) are kept as recorded there.

### `DEBT-R6PLUS-FILE-INSERT-SURFACE`

Author requirement, recorded on 2026-10-03; it does not belong to `D1` and is not
assigned to `D1`, `A-2`, `C` or `G`:

1. create a **File → Insert** surface;
2. move **Insert File** and **Apply Template** from the current File/Open surface
   to File → Insert;
3. both operations accept GeoCeDG `.cedg` documents besides `.ggb`, respecting
   the specific semantics of each operation;
4. Insert File stays an import of geometry/construction into the current
   document, not an Open and not a document replacement;
5. Apply Template keeps applying defaults and styles and must neither replace
   the `UnitState` nor import geometry;
6. *OFF file* is not presented in the GeoCeDG UI for now, because it opens an
   explicitly 3D route not wanted in this mini-track;
7. the internal upstream OFF support is not removed as part of this debt unless
   later authorized; the requirement is one of surface and product profile;
8. the debt needs an explicit subphase and verification-class assignment before
   implementation;
9. it must be closed before the global closeout of `PRE-G9B-R6-plus` is declared.

## Status updates in this closeout

- Roadmap 4.51: `PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED` in the last closed
  and last executed phase entries, the next-gate entry, the track paragraph and
  the `D1` row; the debt recorded; the next operational subphase is
  `PRE-G9B-R6-plus-A-2`, `NOT AUTHORIZED`.
- Mini-track plan: the `D1` state in the header, the §3 table and §14; the debt
  recorded; `A-2` marked as the next operational subphase.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_D0        3268f9b99d20c5d9cf62f25d8066ee0a8e47765c
  -> T_R6PLUS_D1_PROMPT 3fb7542af3ef36db150291ca77e03b0bf4b6f888 (preparation)
  -> A_R6PLUS_D1_PROMPT a246d74d1999a0018899c40157e939845babe91e (authorization)
  -> T_R6PLUS_D1_R1     b55aa8173045d6d6bf735e70eb22302979b2b26a (first technical candidate)
  -> T_R6PLUS_D1        6e1d845902f534b9d45b533ac265089cbc01e689 (revised technical candidate)
  -> P_R6PLUS_D1        (this closeout commit)
```

to `main`, by clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_D0` immediately before
the push. Forbidden: merge commits, rebases, squashes, amends of frozen
candidates, force pushes, tags, releases, binary publication and any further
implementation.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-D1         = PASS — AUTHOR APPROVED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-A-2        = NOT AUTHORIZED   (next operational subphase)
PRE-G9B-R6-plus-C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
DEBT-R6PLUS-FILE-INSERT-SURFACE = PENDING SCHEDULING — NOT AUTHORIZED
OBS-D1-ROLLBACK-CONSPROT-GUI correction = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

This authorization ends with the publication of `D1`. `A-2` needs its own
explicit author instruction naming its exact base. No tag or release is
authorized.

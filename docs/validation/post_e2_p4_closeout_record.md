# POST-E2-P4 closeout record

```text
POST-E2-P4 = PASS — AUTHOR APPROVED   (conditional author approval of the
                                      Graphics View follow-up successor, outcome A;
                                      all conditions verified)
```

```text
selfApproved              = false
authorApproved            = true    (conditional, granted in advance by the author for
                                    the bounded follow-up successor of af927bcb;
                                    conditions verified below)
passClaimed               = true    (POST-E2-P4 only)
implementationAuthorized  = none; PRE-G9B-R6-plus-E3 documentary readiness only;
                            E3, F1, F2, F3, F4 and G implementation NOT AUTHORIZED
candidateFrozen           = true
candidateMutated          = false   (neither af927bcb nor the successor)
PRODUCT CHANGE            = YES — explicit "Make offset draggable" action (Algebra View
                            and Graphics View context menus)
GEOMETRIC SEMANTICS       = UNCHANGED
KERNEL / DAG / SERIALIZATION / UNDO CONTRACTS = UNCHANGED
CLASSIC                   = UNCHANGED
```

This record preserves the author instruction of 2026-10-09 ("Ejecuta la siguiente
tarea", "GeoCeDG — POST-E2-P4 — Graphical Context Menu Follow-up, Conditional Author
Approval and Publication"). The author completed the interactive smoke of
`af927bcb`: `AUTHOR_SMOKE = PASS WITH ONE ACCEPTED FOLLOW-UP REQUEST` — all
implemented functionality works; the only request was the action from the Graphics
View context menu of the dimension line. The author accepted the conversion logic
and eligibility rules and granted conditional approval for outcome A (bounded
follow-up implemented) or B (deferred). Outcome **A** applies. Mirror:
[`post-e2-p4-closeout.json`](../../geocedg/validation/post-e2/post-e2-p4-closeout.json).
Preserved evidence: [candidate report](post_e2_p4_candidate_report.md) and
[evidence](../../geocedg/validation/post-e2/post-e2-p4-candidate-evidence.json) of
`af927bcb`, [follow-up report](post_e2_p4_graphics_menu_followup_report.md) and
[evidence](../../geocedg/validation/post-e2/post-e2-p4-followup-evidence.json) of the
successor, [design record](../architecture/post_e2_p4_offset_conversion_design.md).

## Identities

```text
original candidate = af927bcb7d078888020c95781f781af214c82c20
                     tree 73d1b12cde285a323c42a40965f83d8c6cbb5030
                     STATIC verification-59a5ec1295f143b2a5a0dcc6836305b0 3/3
                     INFRA_UNIT verification-13377d2a740e4c749677b99ea68b9f93 22/22
                     PHASE verification-0fddcf97000e4a84a41e4fbda32df810 3/3
                     AUTHOR_SMOKE = PASS WITH ONE ACCEPTED FOLLOW-UP REQUEST
approved successor = 06835fdf477d23ade9694c952c84191b7f2ebe07
                     tree 0732299a9117bceaca1b1f239179944dd9726c00 (parent af927bcb)
                     STATIC verification-d4561ab4be6643dab5a0fc413cb4c539 3/3
                     INFRA_UNIT verification-8a008e1351f74f1ebbe2e467d4e6d243 22/22
                     PHASE verification-18de071eb986411fb0ea1979670585ac 3/3
                     all ACCEPTED / COMPLETE
```

## Conditions of outcome A (all satisfied)

| # | Condition | Evidence |
|---|---|---|
| 1 | Gate A satisfied | cause by source and runtime probe: the Graphics View right click always opens the inherited chooser (`showPopupChooseGeo`), which `GuiManager3D` builds without the product decoration; the chooser receives the dimension output itself, whose parent algorithm is the native dimension. The correction reuses the P4 service through the existing parent-algorithm relation; no selection, hit-testing, chooser, kernel or persistence change |
| 2 | the only product change is the context-menu adaptation | `af927bcb..06835fdf`: production code only `GuiManagerGeoCeDG.showPopupChooseGeo` (override for outputs of one native dimension) plus test, inventory, manifest and documentation |
| 3 | the conversion logic is unchanged | `GeoCeDGDimensionOffsetConversion` byte-identical to `af927bcb` (`git diff af927bcb 06835fdf` empty for it); `PostE2P4OffsetConversionTest` (11) unchanged and passing |
| 4 | focused and phase verification pass | `PostE2P4GraphicsContextMenuTest` 9/9, regressions (P4 conversion 11, E2 dimensions 14, E2 presentation 8, workspace surface 25, product policy 8, menu lifecycle 6, document lifecycle 16); Checkstyle no new violation; upstream boundary 1014 files; official producers (`post-e2-p4.desktop` 59, `final.shared` 6963, `final.desktop` 1965) all `COMPLETED`, exit 0; STATIC, INFRA_UNIT, PHASE above |
| 5 | both views invoke the same conversion semantics | both menus call the same `GeoCeDGDimensionOffsetConversion.menuItem` → `run`; test `caseIJBothRoutesProduceTheSameTransaction`: identical construction XML after conversion, undo and redo, one undo point each, the same number object kept, identical saved documents |
| 6 | no new unresolved regression or architectural issue | none; the *GeoCeDG* workspace submenu stays absent from Graphics View menus as before (unchanged, outside this request) |
| 7 | the author smoke conclusions remain valid | conversion, eligibility, drag, undo/redo, persistence and Algebra View route unchanged and re-tested |
| 8 | clean auditable descendant of `af927bcb` | `af927bcb → 06835fdf` (one commit, parent `af927bcb`); clean tree at the successor and at this closeout |

## Normative promotion

The *POST-E2-P4 amendment* §8.6 of `geocedg/specs/dimensions/native-dimensions.md`
is promoted from `PROPOSED` to `NORMATIVE / AUTHOR APPROVED` with this record; the
other clauses are unchanged.

## Planning notes recorded by the author

- **PRE-G9B-R6-plus-E3** (`IsoABorder` + `ExportArea` integration) is the next
  intended activity; only documentary readiness preparation is authorized. E3
  implementation is NOT AUTHORIZED.
- **PRE-G9B-R6-plus-F4** will be proposed between `F3` and `G`; intended ownership:
  compact style-bar numeric line-thickness entry
  (`OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT`), DXF compatibility recheck with
  AutoCAD 2026, `TD-PDF-1` (physical text size) and `TD-PDF-2` (embedded-font PDF
  behaviour). The graphical P4 context-menu enhancement is **not** deferred (it is
  implemented here). State: `PLANNING INTENT RECORDED — IMPLEMENTATION NOT
  AUTHORIZED`; no implementation contract or verification class is defined.
- Direct working-layer entry (`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`) stays assigned
  to `F2`.
- `G` will later reconcile the remaining historical technical debts into a
  structured register.

## Closeout verification

Documentary closeout (record, mirror, normative promotion, planning, mini-track and
roadmap status); `STATIC` on the closeout commit, reported with the publication.

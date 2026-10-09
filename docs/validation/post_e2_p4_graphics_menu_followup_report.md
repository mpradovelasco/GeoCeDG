# POST-E2-P4 — Graphics View context-menu follow-up: successor report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P4 (successor of the original candidate)
ORIGINAL CANDIDATE        = af927bcb7d078888020c95781f781af214c82c20
                            tree 73d1b12cde285a323c42a40965f83d8c6cbb5030
                            (unchanged; its report and runs stay historical evidence)
AUTHOR_SMOKE (original)   = PASS WITH ONE ACCEPTED FOLLOW-UP REQUEST
FOLLOW-UP GATE            = A — BOUNDED DESKTOP FOLLOW-UP
VERIFICATION_CLASS        = BOUNDED_PHASE (unchanged)
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P4
                            (plus STATIC and INFRA_UNIT) on the exact successor
CONVERSION LOGIC          = UNCHANGED (GeoCeDGDimensionOffsetConversion not modified)
selfApproved              = false
```

Authority: the author instruction of 2026-10-09 "GeoCeDG — POST-E2-P4 — Graphical
Context Menu Follow-up, Conditional Author Approval and Publication". Original
evidence: [candidate report](post_e2_p4_candidate_report.md),
[design record](../architecture/post_e2_p4_offset_conversion_design.md) (§5 added
for this follow-up). Mirror:
[`post-e2-p4-followup-evidence.json`](../../geocedg/validation/post-e2/post-e2-p4-followup-evidence.json).

## 1. Investigation

| # | Question | Finding |
|---|---|---|
| 1 | semantic dimension | the `AlgoAlignedDimension` / `AlgoLinearDimension` algorithm, parent of the five outputs |
| 2 | output hit by the Graphics View | runtime probe on `af927bcb` (real controller, Move mode, right press and release): at the dimension line the top hit is output #1 (`GeoSegment`, parent `AlgoAlignedDimension`); at an extension line output #2; at the value text output #4 |
| 3 | object passed to the menu factory | that output itself, as the selected list of `showPopupChooseGeo(selected, hits, view, p)` |
| 4 | construction parent | the native dimension algorithm (`AlgoNativeDimension.ownerOf`) |
| 5 | single-object menu | the Graphics View never uses `showPopupMenu`: `EuclidianController.processRightReleased` always calls `showPopupMenuChooseGeo`, also for one hit; `GuiManager3D.showPopupChooseGeo` builds `ContextMenuChooseGeoD` and shows it **without** the product decoration. The Algebra View calls `showPopupMenu(geos, tree, p)`, which `GuiManagerD` decorates — hence the action appeared only there |
| 6 | overlapping objects | the same chooser lists the other hits under *Select another*; choosing one re-enters `showPopupChooseGeo` with that object (`ContextMenuGeoElement.geoActionCmd`) |

## 2. Correction

`GuiManagerGeoCeDG.showPopupChooseGeo` (GeoCeDG-owned override): if the menu's
selected objects are outputs of one native dimension (parent-algorithm relation,
the existing `GeoCeDGDimensionOffsetConversion.contextDimension`), it builds the
same inherited `ContextMenuChooseGeoD` and appends a separator and the existing
`menuItem` action; otherwise it calls the inherited chooser unchanged. Selection,
hit testing, the chooser and its rerouting, the kernel, the conversion, eligibility
and persistence are untouched; Classic uses `GuiManager3D` and is unaffected. The
*GeoCeDG* workspace submenu is not added to the chooser (unchanged).

## 3. Tests

`PostE2P4GraphicsContextMenuTest` (9; real controller right clicks, the popup is
captured instead of shown):

| Case | Test | Result |
|---|---|---|
| A aligned | `caseAAnAlignedDimensionLineOffersTheEnabledActionAndConverts` | enabled action appended after all inherited chooser entries; opening the menu changes nothing and stores no undo point; the right click does not drag; executing it labels the same number `offset1`, geometry identical, one undo point |
| B linear | `caseBALinearDimensionLineBehavesTheSame` | `LinearDimension[A, B, xAxis, offset1]`, geometry identical |
| C, D, E | `casesCDEIneligibleOffsetsShowTheDisabledActionWithTheirReason` | already draggable, `2*3`, locked `t`: disabled with the established reasons; nothing converted |
| F | `caseFAnOrdinarySegmentKeepsTheInheritedChooser` | inherited chooser, no product menu |
| G | `caseGEveryDrawnOutputResolvesItsOwnDimensionOnly` | value text and extension lines resolve their own dimension; a second dimension is untouched until its own extension line is used; mixed contexts give no action |
| H | `caseHOverlappingObjectsKeepTheInheritedChooserAndItsRerouting` | chooser opened for an overlapping segment lists the dimension line; choosing it reopens through the normal route with the enabled action and the *Select another* submenu kept; choosing the segment again returns to the inherited chooser |
| I, J | `caseIJBothRoutesProduceTheSameTransaction` | Graphics View and Algebra View routes on two identical documents: same number object kept in each, identical construction XML after conversion, after undo (literal) and after redo, one undo point each, identical saved documents |
| lifecycle | `aMenuKeptAcrossNewOrOpenNeverConvertsAStaleDimension` | a menu built before New or Open warns and changes nothing; a new dimension is never converted by an old menu |
| EN/ES | `theGraphicsViewActionIsLocalized` | Spanish label and tooltip |

Regression run: `PostE2P4OffsetConversionTest` (11),
`PreG9BR6PlusE2NativeDimensionDesktopTest` (14),
`PreG9BR6PlusE2PresentationFollowUpDesktopTest` (8), `G9U1WorkspaceSurfaceTest`
(25), `G9U1ProductPolicyTest` (8), `G9U0R3MenuLifecycleTest` (6),
`GeoCeDGDocumentLifecycleTest` (16) — all pass. Checkstyle: no new violation.
Official producers, inventory, upstream boundary and acceptance runs are recorded
in the evidence mirror and the closeout.

## 4. Limitation kept

The *GeoCeDG* workspace submenu remains absent from Graphics View menus, as before
(not part of this request).

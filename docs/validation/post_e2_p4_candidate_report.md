# POST-E2-P4 — explicit offset conversion of typed dimensions: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P4
VERIFICATION_CLASS        = BOUNDED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P4
                            (on the exact commit and tree; STATIC and INFRA_UNIT as
                            for every candidate)
BASELINE                  = 2b3fafe3ee8d4b29af57e826983ac0201e124ceb
                            tree df24c5f380887a78c1905791f590b6b6bf990ecd
                            (POST-E2-P3 closeout, published main)
READINESS                 = SUFFICIENT — gate A
SELECTED DESIGN           = EXPLICIT OFFSET CONVERSION — OPTION A (author-selected)
OPTION B                  = NOT IMPLEMENTED
PRODUCT CHANGE            = YES — object context menu of native dimensions (GeoCeDG
                            Desktop)
KERNEL / DAG / REDEFINE / SERIALIZATION CONTRACTS = UNCHANGED
CLASSIC                   = UNCHANGED
AUTHOR_SMOKE              = PENDING
selfApproved              = false
authorApproved            = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

This artifact records only facts fixed when it was written; it carries no author
approval. Authority: [P3 closeout record](post_e2_p3_closeout_record.md) (P4
authorization). Readiness gate and design:
[`post_e2_p4_offset_conversion_design.md`](../architecture/post_e2_p4_offset_conversion_design.md).
Mirror: [`post-e2-p4-candidate-evidence.json`](../../geocedg/validation/post-e2/post-e2-p4-candidate-evidence.json).
Normative text: *POST-E2-P4 amendment* §8.6 of
[native-dimensions](../../geocedg/specs/dimensions/native-dimensions.md),
`PROPOSED`.

## 1. Result

**Implementation layer.** GeoCeDG Desktop: new
`GeoCeDGDimensionOffsetConversion`; `GuiManagerGeoCeDG` adds the entry to the
product object context menu; eight EN/ES texts in
`apps/geocedg/application-profile.yml`. No shared kernel, algorithm, command,
reader, writer, drawable, exporter, toolbar or Classic code changed.

**Mechanism.** The literal offset of `AlignedDimension(A,B,1)` is already an
unlabelled, independent number held by the algorithm. The action gives *that
object* the first free label `offsetN`, makes it hidden and auxiliary like a
tool-created offset, and moves it in the construction order to just before the
dimension (existing `setLabel` and `moveInConstructionList`). The result persists
as `offset1 = 1` and `AlignedDimension(A,B,offset1)`. No redefine, no object
replaced or recreated, no dependency edge changed.

**Identity preservation.** Complete: the number, the algorithm, its five outputs
and all dependents keep their object identity; value, geometry and text are
identical. Identity is held through the algorithm and its input object, never
inferred from labels, coordinates, indices or XML position.

**DAG behaviour.** Edges unchanged; the input gains a label and a construction-list
position immediately before its dimension.

**Undo/redo.** One undo point per conversion; undo restores the literal document,
redo the converted one; undo and redo store nothing. A failure after labelling is
reverted exactly in memory and stores no undo point.

**Save/reopen.** `.cedg` save and reopen restore the converted construction; the
reopened construction XML equals the in-memory XML and a second reload is
identical.

**Eligible cases.** `AlignedDimension` and `LinearDimension` (line or vector,
three- or five-length forms) whose offset is an unlabelled, independent, unlocked
number with a plain decimal literal definition (`1`, `-1`, `1.5`, `1E-3`, …), with
the dimension defined and the construction shown at its last step.

**Excluded cases** (entry disabled with its reason; nothing changes):
expressions and constants (`2*3`, `1/3`, `pi`, `sqrt(2)`), computed offsets
(`s+1`, `x(A)+1`), named offsets that are locked or dependent, offsets already
draggable (redundant conversion avoided), undefined dimensions, constructions shown
at an earlier step; dimensions produced by user macros get no entry.

## 2. Tests

`PostE2P4OffsetConversionTest` (11):

| Required case | Test | Result |
|---|---|---|
| eligible aligned, successful conversion, identical value and geometry, dependents, repeated action | `anAlignedLiteralOffsetBecomesANamedFreeNumberInPlace` | same number object labelled `offset1`, free, unlocked, hidden auxiliary; `AlignedDimension[A, B, offset1]`; outputs, value, geometry, text, `m = 2·value` and `Midpoint` identical; ordered just before the dimension; XML element before the command with `a2="offset1"`; the repeated action returns nothing, warns once and changes no byte |
| eligible linear (line, vector), five-argument forms | `linearAndFiveArgumentDimensionsConvertOnlyTheirOffset` | `offset1`…`offset4`; overshoot and gap stay literal; geometry identical; all draggable |
| label collision | `labelsAvoidExistingNames` | with `offset1` (number) and `offset2` (text) present: `offset3`; existing objects untouched |
| dragging after conversion; E2 drag regression | `draggingAfterConversionWritesOnlyTheNewNumber` | the literal is not dragged before; after conversion the drag writes only `offset1` (3), points fixed, one undo point |
| one-step undo/redo | `oneUndoStepRevertsAndOneRedoStepRestoresTheConversion` | one store; undo gives the literal XML byte for byte; redo the converted XML; no further store |
| save/reopen | `saveAndReopenRestoreTheConvertedConstructionDeterministically` | value, geometry, dependents, draggability restored; XML stable |
| unsupported expressions, locked or non-independent offsets, undefined dimension | `expressionsNamedLockedAndUndefinedOffsetsAreNotConverted` | `EXPRESSION`, `NAMED_OFFSET`, `ALREADY_DRAGGABLE`, `UNDEFINED`; disabled entries with the matching reason; construction XML and order unchanged |
| earlier construction step, macro-contained result, semantic context | `aConstructionShownAtAnEarlierStepOrAMacroResultIsNotConverted` | `UNAVAILABLE` at an earlier step; macro output has no entry; a mixed context is not one dimension; line and text resolve to the same algorithm |
| failure rollback | `aFailureAfterLabellingRestoresTheLiteralAndStoresNothing` | injected failure after labelling: literal restored (unlabelled, not in the list, flags restored), no `offset1`, order, step and XML unchanged, no undo point; a later conversion succeeds |
| context menu EN/ES | `theObjectContextMenuOffersTheActionForOneDimensionOnly` | entry for each of the five outputs, none for a point; choosing it converts with one undo point; disabled afterwards; Spanish label |
| GeoGebra Classic compatibility | `classicHasNoActionAndReadsAConvertedDocumentUnchanged` | Classic decorates no context menu; a converted document loads in Classic with `offset1` as the independent offset and identical geometry |

Regression (same selection): `PreG9BR6PlusE2NativeDimensionDesktopTest` (14,
including the E2 offset drag) and `G9U1WorkspaceSurfaceTest` (25, product context
menu); also run focused: `PreG9BR6PlusE2PresentationFollowUpDesktopTest` (8),
`G9U1ProductPolicyTest` (8). Checkstyle: no new violation.

## 3. Verification (suites and acceptance)

Recorded in the evidence mirror: official producers (`post-e2-p4.desktop`,
`final.shared`, `final.desktop`) and the inventory update. STATIC, INFRA_UNIT and
PHASE `POST-E2-P4` run on the frozen commit and are reported outside this artifact.

## 4. Author smoke checklist

1. Type `A=(0,0)`, `B=(4,0)`, `AlignedDimension(A,B,1)`. In Move mode the
   dimension line cannot be dragged.
2. Right-click the dimension line (or the value, or the value in the Algebra view):
   **Make offset draggable**. Nothing moves; the Algebra view (auxiliary objects)
   shows `offset1 = 1`; the dimension definition reads `AlignedDimension(A, B,
   offset1)`.
3. Drag the dimension line: only the offset changes. Undo twice: the drag, then the
   conversion (the definition shows `1` again). Redo.
4. Save, close and reopen: the dimension is still draggable.
5. `LinearDimension(A,B,xAxis,2)` behaves the same (`offset2` when `offset1`
   exists).
6. `AlignedDimension(A,B,2*3)`, a tool-created dimension and a dimension with a
   fixed named offset show the entry disabled with an explanation.

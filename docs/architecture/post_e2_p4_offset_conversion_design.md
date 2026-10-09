# POST-E2-P4 — explicit offset conversion of typed dimensions: readiness gate and design

```text
ARTIFACT_KIND        = READINESS GATE AND DESIGN RECORD (technical candidate)
PHASE                = POST-E2-P4
AUTHORITY            = docs/validation/post_e2_p3_closeout_record.md (P4 authorization,
                       author-selected Option A)
BASELINE             = 2b3fafe3ee8d4b29af57e826983ac0201e124ceb
                       tree df24c5f380887a78c1905791f590b6b6bf990ecd
                       (POST-E2-P3 closeout, published main)
OBSERVATION          = OBS-E2-2 (typed dimensions with a literal offset are not dragged)
READINESS            = SUFFICIENT — gate A
DESIGN               = EXPLICIT OFFSET CONVERSION — OPTION A
OPTION B             = NOT AUTHORIZED, NOT IMPLEMENTED
VERIFICATION_CLASS   = BOUNDED_PHASE (frozen at implementation start)
selfApproved         = false
```

Reused evidence: the E2 contract and its tests
([native-dimensions](../../geocedg/specs/dimensions/native-dimensions.md) §3, §4,
§8.2, §8.4, §9; `PreG9BR6PlusE2NativeDimensionDesktopTest`), and the P4 section of
the [planning proposal](post_e2_planning_proposal.md). Two focused scratch probes
(not committed) answered the questions the E2 evidence did not.

## 1. Readiness gate

| # | Question | Answer (evidence) |
|---|---|---|
| 1 | How is a literal offset represented? | `AlignedDimension(A,B,1)` holds an **unlabelled, independent `GeoNumeric`**, not in the construction list, used by exactly that algorithm, with definition `ExpressionNode(NO_OPERATION, MySpecialDouble "1")`. The same holds for `-1`, `1.5`, `(1)`, `+2`, `1E-3`. Constant expressions (`2*3`, `1/3`, `pi`, `-pi`, `sqrt(2)`) are also unlabelled independent numbers but with a compound or named-constant definition; `x(A)+1` and `s+1` are dependent (as was `1e5`, read as `1·e·5` in a probe document that had an object named `e`). (probe 1) |
| 2 | Does the algorithm receive a durable numeric geo? | Yes. `AlgoNativeDimension` keeps the `GeoNumberValue` it was constructed with as `getOffsetInput()`; the drag gate already reads it (`GeoCeDGEuclidianController.isDraggableOffset`). |
| 3 | How does the DAG represent the dependency? | As for any input: the number lists the dimension algorithm in its algorithm list and is an input of the algorithm. An unlabelled independent input is not a construction element; the command XML writes its value (`a2="1"`). A labelled independent input is a construction element written before the command (`<element type="numeric" label="s">`, `a2="s"`). |
| 4 | Can the existing redefine mechanism replace the literal without replacing unrelated outputs? | Redefine (`changeGeoElement`, `Construction.replace`) rebuilds the construction from XML and therefore replaces objects; it would not keep identities and would enter the spatial redefine transaction contracts. **It is not needed.** Two existing upstream kernel operations reach the target state without redefine: `GeoElement.setLabel` on an unlabelled independent element (what the Algebra view does when an object receives a name; adds it to the construction list) and `Construction.moveInConstructionList` (the construction protocol reordering, guarded by `getMinConstructionIndex`/`getMaxConstructionIndex`). |
| 5 | Are construction identity and dependents preserved? | Yes, completely. The same `GeoNumeric` object becomes the named number; the algorithm, its five outputs and every dependent keep their object identity (probe: `outputs identical true`, dependents `m = 2·value` and `Midpoint(dimension line)` unchanged). No identity is inferred from labels, coordinates, indices or XML position: the conversion holds the algorithm and its input object directly. |
| 6 | One atomic undoable operation? | Yes. Labelling and ordering run synchronously on the EDT; `app.storeUndoInfo()` once afterwards gives one undo point. Undo restores the literal document; redo the named one (probe and tests). A failure between the two steps is reverted in memory by the exact inverse (remove from the construction list and label table, clear the label, restore the visibility flags), so no partial state or orphan number remains and nothing is stored. |
| 7 | Does save/reopen restore the result? | Yes. The XML writes `offset1` as an ordinary numeric element before the command and the command input as `a2="offset1"`; `.cedg` save and reopen give the same construction XML as in memory, and a second reload is identical (probe and tests). |
| 8 | Can it be offered through an existing context menu without changing command syntax? | Yes. `GuiManagerGeoCeDG.decorateProductContextMenu` already decorates the host object context menu in the product only (Classic decorates nothing). The semantic dimension is `AlgoNativeDimension.ownerOf(geo)` — the algorithm parent of the selected output — for every element of the context. No command, signature or argument changes. |

**Gate outcome A.** The conversion uses approved kernel behaviour only (labelling
and construction-order moves), changes no redefine contract, no durable identity
handling and no DAG edge, and adds no kernel code. Bounded implementation is
authorized by the P3 closeout record under this outcome.

## 2. Design

**Ownership.** GeoCeDG Desktop only:
`GeoCeDGDimensionOffsetConversion` (new) and the product context-menu hook in
`GuiManagerGeoCeDG`; eight EN/ES texts in `apps/geocedg/application-profile.yml`.
No shared kernel, algorithm, drawable, exporter, command processor, XML reader or
writer, Classic or toolbar change.

**Eligibility** (`eligibility(owner)`), in order:

| Result | Condition | UI |
|---|---|---|
| `UNAVAILABLE` | not an algorithm of the open construction, not in its construction list, or the construction is not shown at its last step | disabled, reason |
| `ALREADY_DRAGGABLE` | the offset is labelled and already passes the §8.4 gate | disabled, reason |
| `NAMED_OFFSET` | the offset is labelled but dependent or locked | disabled, reason |
| `EXPRESSION` | the offset is not an unlabelled, independent, unlocked `GeoNumeric` used only by this dimension whose value is finite and whose definition is absent or a plain decimal literal (`NO_OPERATION` of a `MyDouble` printed as `[+-]digits[.digits][E±digits]`) | disabled, reason |
| `UNDEFINED` | the dimension value is undefined | disabled, reason |
| `ELIGIBLE` | otherwise | enabled |

Dimensions produced inside a user macro have an `AlgoMacro` parent, are not native
dimension outputs in the document and get no entry.

**Conversion** (`convert(owner)`): re-check eligibility; take the label
`LabelManager.getNextNumberedLabel("offset")` (first free `offsetN`); set the
number auxiliary and hidden in the Graphics view like a tool-created offset
(§8.2); `setLabel`; `moveInConstructionList(number, dimensionIndex)`; verify the
label, the order (number immediately before the dimension), the unchanged input
object and the §8.4 gate; on any failure restore exactly and rethrow. `run(owner)`
wraps it as the user action: unavailable → warning, no change; failure → warning,
no change, no undo point; success → one `storeUndoInfo`.

**Context menu.** `GuiManagerGeoCeDG.showPopupMenu` remembers the context objects
while the host builds its menu; the decoration adds **Make offset draggable** /
**Hacer desplazamiento arrastrable** before the existing *GeoCeDG* submenu when
all context objects are outputs of one native dimension. The multi-object chooser
menu (several objects under the pointer) is the inherited host menu and is not
decorated, as before.

## 3. Non-goals and scope limits

Option B (accepting a drag on a literal by rewriting the argument) and any
implicit mutation of literal arguments are not implemented. Native dimension
mathematics, `Gap`/`Overshoot`, drawing scale, PDF export, spatial kernel
semantics, macro registration, other command signatures and unrelated redefine
operations are unchanged. TD-PDF-1 and TD-PDF-2 are not started.

## 4. Verification class

`BOUNDED_PHASE`, as proposed by the planning record and confirmed by the
implementation: the change is confined to a Desktop action and its menu entry,
uses existing kernel operations without changing redefine, DAG or persistence
semantics, and leaves Classic untouched. Acceptance: PHASE `POST-E2-P4` on the
frozen commit (with STATIC and INFRA_UNIT as for every candidate); the official
`final.shared` and `final.desktop` producers run for the inventory update.

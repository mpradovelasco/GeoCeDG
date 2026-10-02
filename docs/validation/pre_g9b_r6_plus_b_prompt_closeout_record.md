# PRE-G9B-R6-plus-B preparation package closeout record

```text
PRE-G9B-R6-plus-B PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-B                     = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (B documentary preparation package only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = false   (B and every later subphase)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-10-02 on the
documentary preparation package of `PRE-G9B-R6-plus-B`, the author dispositions
on the requested decisions `DQ-B1` to `DQ-B9`, the author precision on the
inherited preview observation, and the exact identities behind them. The
package is not a phase: approving it closes the preparation, not `B`, and
authorizes no implementation. This record changes no product, test, build,
packaging, verifier, registry, schema, specification, ADR or serialization
file, and it does not modify the frozen candidate. Besides this record and its
machine-readable mirror,
[`pre-g9b-r6-plus-b-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-b-prompt-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

## Author disposition

```text
T_R6PLUS_B_PROMPT = 7e00e451167b0d915b061655c478c2faac99e51c
AUTHOR_APPROVAL   = APPROVED, subject to the DQ-B dispositions below
selfApproved      = false
```

The author instruction of 2026-10-02 approves the frozen candidate as the
documentary preparation package of `B` and authorizes exclusively the
documentary closeout, the record of the dispositions below, the necessary
status updates, the applicable documentary verification and the publication of
the linear range. The approved package is:

- the canonical prompt
  [`pre-g9b-r6-plus-b-export-surface-and-export-area.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-b-export-surface-and-export-area.prompt.md),
  which stays `PREPARED — NOT AUTHORIZED` with `implementationAuthorized = false`;
- the versioned [B author-decision record](pre_g9b_r6_plus_b_author_decisions_record.md)
  (`AQ-X1` to `AQ-X7`) and its machine-readable mirror
  [`pre-g9b-r6-plus-b-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-b-author-decisions.json);
- the roadmap 4.41 and mini-track plan updates.

The frozen candidate is not amended. Its prompt carries no author-approval
field. Where the frozen prompt states a default for `DQ-B1` to `DQ-B9`, or a
characterization this record corrects, **this record prevails**. The
instruction that authorizes `B` may integrate these dispositions into the
prompt as its first tracked edit, following the `P0` and `A-1` precedent.

## Author dispositions on `DQ-B1` to `DQ-B9`

| ID | Disposition | Relation to the frozen default |
|---|---|---|
| `DQ-B1` | In GeoCeDG, `ExportImage` with `type=gif` or `type=webm` is rejected explicitly while Animated GIF/WebM stay outside the authorized surface. A silent no-op is not admitted. | accepted |
| `DQ-B2` | The v1 fallback also filters Animated GIF, besides STL, Collada, HTML Collada and Dynamic Worksheet. | accepted |
| `DQ-B3` | See below. | **replaced** |
| `DQ-B4` | The `MANUAL` producer is defined initially through a dialog of bounds in world coordinates. No drag gesture is required in `B`. | accepted |
| `DQ-B5` | See below. | accepted with an architectural precision |
| `DQ-B6` | `Ctrl+Shift+C` runs the graphics copy directly through the common service and the same `ExportArea` authority. It need not open the Picture dialog and is not a second authority. | accepted |
| `DQ-B7` | PSTricks, PGF/TikZ and Asymptote keep their upstream area behavior until `C`, including `selectionRectangle`, as an explicitly documented temporary inconsistency. `B` does not change that semantics. | accepted |
| `DQ-B8` | See below. | accepted and made an explicit stop condition |
| `DQ-B9` | If `--export` cannot resolve a valid export area or produce a valid output, it fails explicitly, returns a non-zero status and leaves no empty file as an apparently valid result. | accepted |

### `DQ-B3` — screen-anchored objects (replaces the frozen default)

A screen-anchored object is **not** turned into a world-anchored object by
computing its world position from the live viewport.

```text
contract : screen-anchored -> export-canvas / presentation anchored
never    : screen position -> live-view world coordinate -> export authority
```

In the offscreen export viewport the object keeps, where technically
sustainable, its presentation position relative to the output canvas. If it
falls outside the canvas it is clipped normally. If the current backend cannot
preserve this semantics correctly without turning presentation into geometry,
or without a material widening of scope, `B` stops and reports the evidence
for an author decision.

### `DQ-B5` — views (architectural precision)

- The initial `B` UI may expose the `MANUAL` definition from Graphics 1 only.
- `ExportArea` and the shared service do not hard-code Graphics 1 as a
  semantic identity. They keep an explicit `sourceViewId` and stay
  architecturally valid for any supported 2D view.
- `EXPORT_POINTS` may be resolved for the corresponding 2D source view.
- The 3D-view export stays entirely outside `B` and does not change.

### `DQ-B8` — PDF exactness (stop condition)

PDF exactness cannot be obtained by silent rounding or truncation. If the PDF
backend used by GeoCeDG cannot represent the required physical size with the
exactness of the `B` contract, the implementation stops, characterizes the
limitation and requests an author decision before introducing any
approximation. A silent conversion, for example of a fractional size in points
to an integer size, is not authorized.

## Author precision on `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`

The observed preview appears when **Save** is run from the Picture export
window. It is shown in the right-hand panel of the dialog that chooses the file
name and path. The author confirms that it does not correspond to a previously
existing file: it is a preview generated during the save flow of the pending
export.

`GeoGebraFileChooser` has routes that generate previews dynamically through
`app.getExportImage(...)`, so `B` does not assume that the side panel reads an
earlier file from disk. `B` reproduces the exact flow and establishes which
mode and route of the file chooser generate that preview for the Picture
export.

```text
save-dialog generated preview effective visibility == final export effective visibility
```

For the `A-1` case: with a layer hidden, the side preview shown when Save is
requested from the Picture export omits its objects, and the corresponding
PNG, PDF, SVG or EMF file applies the same effective visibility under the `B`
contract.

The discrepancy is not resolved by:

- simply hiding the preview panel;
- presenting it as the preview of an earlier file;
- introducing a second, independent render route with different semantics.

The solution reuses, directly or indirectly, the same `ExportArea` authority,
the same hidden-layer state and the same effective rendering semantics that
govern the pending export. If the reproduction shows that the panel uses a
preview route other than the one first inferred, `B` corrects that route with
the minimal change and documents the evidence. It does not widen the general
layer semantics outside the export surface.

## `OBS-A1-BACKGROUND-IMAGE-LAYER`

`B` re-characterizes how this observation affects preview and export. It does
not silently change the general layer semantics of the normal view to resolve
it. If that requires widening the semantics, it documents and defers, or
requests an author decision.

## Corrections and consequences recorded with the dispositions

These do not change the dispositions above; they record where the frozen
prompt reads differently, so that the authorizing instruction can integrate
them.

1. **Preview route (correction of the frozen prompt).** The frozen prompt
   (re-characterization table, row *Picture dialog preview*, and the contract
   detail *Preview*) says that the only image preview found on the Picture
   route shows an image file already on disk. That is not the route of the
   Picture Save. Every `GuiManagerD.showSaveDialog` call, including the PNG,
   PDF, SVG and EMF calls of `GraphicExportDialog`
   (`desktop/export/GraphicExportDialog.java:636`, `:670`, `:706`, `:749`), sets
   `GeoGebraFileChooser.MODE_GEOGEBRA_SAVE`
   (`desktop/gui/GuiManagerD.java:1702-1706`), and in that mode the side panel
   generates `app.getExportImage(THUMBNAIL_PIXELS_X, THUMBNAIL_PIXELS_Y)`
   (`desktop/gui/util/GeoGebraFileChooser.java:474-479`). Read from source,
   that call reaches `EuclidianView.getExportImage` and `exportPaint`, which
   `A-1` filters, so the source alone still does not explain the observation;
   the reproduction the author requires is necessary. The same save-mode
   preview also serves the native document Save; the minimal correction must
   keep that consumer's behavior explained and tested.
2. **`DQ-B3` (replacement).** The frozen default ("world position that
   corresponds to their current screen position") and the `DQ-B3` row of the
   frozen invariants table are superseded. Consequence to characterize: at the
   base, an `Export_1`/`Export_2` export shifts screen-anchored objects with
   the export translation; under the canvas-anchored contract their position
   relative to the output canvas can differ from the base for legacy documents
   with `Export_1`/`Export_2`. `B` reports this in its legacy-compatibility
   evidence, and the contract-sustainability stop above applies.
3. **`DQ-B5` (precision).** The frozen default ("`MANUAL` is bound to
   Graphics 1") is read as a UI limitation only; `sourceViewId` stays explicit
   and the service stays valid for any supported 2D view.
4. **Raster rounding.** `DQ-B8` governs PDF. The frozen prompt's raster rule
   (an integer pixel grid onto which the exact area is mapped, with a
   documented deterministic sub-pixel adjustment) stays a prompt contract and
   is not an author decision; the authorizing instruction may confirm or
   replace it.

## Approved identities

```text
P_R6PLUS_A1       = d32ad608ba8821bc18c9d4dc783c1b700a165ff2
                    tree 9b123be7ed8c7176b5a7e19f74be5946f0164c8b
                    published A-1 closeout; base of the package

T_R6PLUS_B_PROMPT = 7e00e451167b0d915b061655c478c2faac99e51c
                    tree 2f1b535ed593508f3471b8d9944231024652a2b9
                    parent d32ad608ba8821bc18c9d4dc783c1b700a165ff2
                    approved documentary candidate; not amended

branch            = phase/pre-g9b-r6-plus-b-prompt (local; never pushed)
```

The identity of this closeout commit, `P_R6PLUS_B_PROMPT`, is its own commit,
which the record cannot name. It is reported with the publication.

## Accepted evidence

| Commit | Run | Result |
|---|---|---|
| `T_R6PLUS_B_PROMPT` `7e00e451…` | `verification-36d85e09e7c84d49b1813565e40fb322` | `STATIC`; exit 0; `ACCEPTED / COMPLETE`; 3/3 (`prompt.execution-safety`, `repository.boundary-safety`, `static.scientific-inputs.semantic`); plan hash `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`; result hash `0b14f58b8687df3aa528c152074cf49289e1362cb706ab6842b01bd52e4e5637` |

The run reported the two standing diagnostics present on every `STATIC` run,
`diagnostic.governance` (`DIAGNOSTIC_FINDING`) and
`diagnostic.historical-consistency` (`DIAGNOSTIC_UNAVAILABLE`); the other four
were clear. `git diff --check` on the candidate: exit 0. The canonical `B`
prompt validated with `Test-PromptContractDocument` (`task` profile,
`execution_safe = true`). `STATIC` is not `FINAL`; no receipt is expected or
claimed. This closeout commit's own `STATIC` run is reported with the
publication.

## Status updates in this closeout

- Roadmap 4.42: the preparation package is recorded as `PASS — AUTHOR
  APPROVED`, with this record, in the next-gate entry, the track paragraph and
  the `B` row. `B` stays `NOT AUTHORIZED`. The last closed phase is unchanged,
  because the package is not a phase.
- Mini-track plan: the header line for `B`, the §3 `B` state and the §14 `B`
  state.

## Publication

The author authorizes publication of the exact linear range

```text
P_R6PLUS_A1         d32ad608ba8821bc18c9d4dc783c1b700a165ff2
  -> T_R6PLUS_B_PROMPT 7e00e451167b0d915b061655c478c2faac99e51c
  -> P_R6PLUS_B_PROMPT (this closeout commit)
```

to `main`, by clean non-force fast-forward only. Precondition: local `main`,
`origin/main` and the live remote `main` equal `P_R6PLUS_A1` immediately before
the push. Forbidden: merge commits, rebases, squashes, amends, force pushes,
tags, releases, binary publication and any product implementation.

Once published, `P_R6PLUS_B_PROMPT` is the natural implementation base for
`B`. As the prompt states, the instruction that authorizes `B` names the exact
base; nothing here fixes it.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
B preparation package      = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-B          = NOT AUTHORIZED   (next operational subphase;
                             implementationAuthorized = false)
PRE-G9B-R6-plus-D0, D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No tag or release is authorized.

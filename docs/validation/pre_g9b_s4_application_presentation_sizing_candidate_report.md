# PRE-G9B-S4-R1 presentation sizing corrective candidate report

## Candidate identity and authority

- Published entry before S4: `bd39099f668de81e9e65c415b28995de7b1c846b`,
  tree `f41cd5050ff013b1f073e5754fe1ccf3feb96a98`.
- Previous unpublished S4 candidate:
  `8ec5d3eaf17a18f08e9484a37790d54791ef255b`, tree
  `fb955194107594bcb3e632ab909af35d60e6a4e2`.
- Required predecessor: `PRE-G9B-S3 — PASS — AUTHOR APPROVED`.
- Authorization: bounded corrective `PRE-G9B-S4-R1` only.
- Corrected candidate: the immutable commit carrying this report, identified by
  the final S4 PHASE receipt and author handoff; the report avoids a
  self-referential Git identity.
- `selfApproved=false`; second author smoke remains `PENDING`.

## Inherited architecture and corrected ownership

The existing `FontSettings`/`FontManagerD`, `GeoGebraPreferencesD`, Desktop
`updateFonts()` graph, view-specific font hooks and `ImageManagerD` resource
selection remain the architecture. R1 adds no preferences framework, theme
system, construction semantics or document fields.

S4 originally separated menu, toolbar-icon, Algebra, Construction Protocol and
Graphics sizing. R1 retains those five owners and adds one residual General UI
owner. Specific category hooks take precedence over the General UI fallback:

```text
construction Text font
    != general UI font
    != menu font
    != toolbar icon size
    != Algebra font
    != Construction Protocol font
    != Graphics font
```

General UI owns `FontManagerD` typography for Input Bar entry and typed command
text, Input Help, toolbar-right command help, ordinary Desktop controls,
redefine/input editors and Text/general dialogs reached by the existing dialog
font-update lifecycle. Menu, Algebra, Protocol and Graphics immediately reapply
their explicit hooks during a general refresh. Toolbar size is obtained through
a toolbar-specific application hook; application-wide dialog/style-bar icons
continue to use normal GUI-icon scaling.

S2 continues to own ordinary and LaTeX construction Text logical font and the
10 pt choice. S3 continues to own world-anchored construction Text scaling.

## Preferences, defaults and migration

| Preference | Versioned key | Fresh default | Existing-profile fallback |
|---|---|---:|---|
| General UI font | `geocedg.presentation.general-ui-font-size.v1` | 12 pt | effective inherited GUI font |
| Menu font | `geocedg.presentation.menu-font-size.v1` | 14 pt | effective inherited menu font |
| Toolbar icon | `geocedg.presentation.toolbar-icon-size.v1` | 28 px | effective inherited toolbar size |
| Algebra font | `geocedg.presentation.algebra-font-size.v1` | 12 pt | effective inherited Algebra font |
| Construction Protocol font | `geocedg.presentation.construction-protocol-font-size.v1` | 12 pt | effective inherited protocol font |
| Graphics font | `geocedg.presentation.graphics-font-size.v1` | 12 pt | effective inherited view font |

A profile is fresh only when it has neither an S4 key nor legacy saved
user-preferences XML. Fresh values are materialized independently. A valid
category key is preserved as an explicit value. A missing or invalid category
in an existing profile captures and normalizes only that category's inherited
effective value. No key is derived from another, and an explicit value is never
overwritten by a fresh default.

All setters persist before live refresh and do not call `setUnsaved()`. Graphics
and Graphics2 share one Graphics preference. Classic retains inherited defaults
and behavior.

## Corrective findings and dispositions

### Presentation panel and construction label

The six controls now use a compact two-column `GridBagLayout`: descriptors are
left aligned, controls share one horizontal origin, row insets are consistent
and the value column absorbs resize space. Descriptor padding was not used.

The construction-owned host control is localized as `Text font size` / `Tamaño
de fuente de Texto`; its ordinary and LaTeX construction behavior is unchanged.

### Text dialog regression

The missing Apply button was caused by `TextInputDialogD` constructing its base
button row with `showApply=false`; only the embedded Properties lifecycle later
added Apply. The Text dialog now constructs Apply as part of its normal action
row, while the Properties binder reuses it without duplication. Existing S2
Apply/OK/Cancel semantics are unchanged.

The Help height anomaly was caused by the first S4 candidate overriding the
application-wide scaled-icon size with the toolbar preference. R1 confines that
preference to the main-toolbar seam. Apply, OK, Cancel and Help heights are
normalized from their normal Swing preferred content heights, without fixed
widths or arbitrary pixel translations; Help also receives the dialog font.

### General UI live surfaces

The Desktop font manager is updated together with `FontSettings`. Existing
`GuiManagerD.updateFonts()` then refreshes Input Bar/Input Help, tracked dialogs,
Properties and ordinary Desktop controls. `InputPanelD`, which is reused by
redefine and general input dialogs, again consumes the plain General UI font,
not Algebra font. `ToolbarContainer` now explicitly fonts its command-help label
and refreshes it after rebuilds.

### Persistent user-tool alignment

Persistent tools were placed in a `FlowLayout` strip while native toolbar tools
use a `BoxLayout` container with bottom alignment. FlowLayout ignored the copied
native alignment and displaced persistent buttons when extra toolbar height was
available. The persistent strip now mirrors the toolbar axis and native parent
alignment with BoxLayout. Button margins, insets and preferred/minimum/maximum
geometry still come from a live native reference. Monogram/raster presentation
uses the native icon's rendered logical size, including scaled/high-DPI icons.
No tool identity, persistence, grouping or command semantics changed.

## Validation and operational impact

Focused acceptance covers 12 S4/R1 methods, the 76-method retained S2 Desktop
selection and the 20-method retained S2/S3 shared selection. It checks fresh and
migrated values, the aligned grid, all six live owners, Text controls and button
height, toolbar 16/28/32/64 px, scaled native geometry, persistent-tool alignment,
the full independence matrix, construction/GeoText containment and Classic
neutrality. The final exact results and exactly-once S4 PHASE receipt are bound
to the immutable corrected candidate.

- `GUIDE_IMPACT = UPDATED`: user guide and second smoke checklist describe R1.
- `BOOTSTRAP IMPACT = NO_CHANGE_REQUIRED`: no toolchain, runtime, permission or
  workstation prerequisite changed.
- Verification impact: S4 identity inventory grows from 4 to 12 methods and its
  impact paths now cover the corrected Desktop seams; `INFRA_UNIT` is required.
- `INFRA_UNIT`: an initial pre-execution catalog-hash rejection and a subsequent
  stale 4-method inventory assertion were corrected; the final infrastructure
  run is `ACCEPTED / COMPLETE` with zero diagnostics.
- No INTEGRATION/FINAL escalation is justified by this bounded frontend scope.

## Governance disposition

No push or tag is authorized. D1, P1, G9B, G9C, G9U2, further G12 and productive
G10 remain untouched and unauthorized. Technical acceptance does not approve
the result or second author smoke.

```text
PRE-G9B-S4-R1 — CORRECTIVE IMPLEMENTATION CANDIDATE
PRE-G9B-S4 — CORRECTED IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
```

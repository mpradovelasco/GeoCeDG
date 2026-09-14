# PRE-G9B-S4 presentation sizing closeout report

## Candidate identity and authority

- Published entry before S4: `bd39099f668de81e9e65c415b28995de7b1c846b`,
  tree `f41cd5050ff013b1f073e5754fe1ccf3feb96a98`.
- Previous unpublished S4 candidate:
  `8ec5d3eaf17a18f08e9484a37790d54791ef255b`, tree
  `fb955194107594bcb3e632ab909af35d60e6a4e2`.
- Required predecessor: `PRE-G9B-S3 — PASS — AUTHOR APPROVED`.
- Final verified technical candidate:
  `eb6af9799eec70459485b13fe1c9cadd394e8dca`, tree
  `f71c34fff34fcff169a1302eb08e879bb247802d`.
- Status: `PRE-G9B-S4`, R1 and R2 **PASS — AUTHOR APPROVED**.
- Author smoke: **PASS**.
- `selfApproved=false`.

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

R1 matched the logical icon and button geometry but left the persistent panel as
a sibling of the upstream vertically centered toolbar wrapper. Full-toolbar
painted-geometry reproduction measured native-minus-persistent center offsets of
0, 6, 7 and 14 logical pixels at 16, 28, 32 and 64 px respectively. At the
approved 28 px default both buttons were 38 by 38, while the native wrapper was
62 px high and the persistent wrapper 38 px high. The size-dependent offset
disproved a fixed artwork or raster translation cause.

R2 places the persistent lifecycle panel as a direct adjunct in the native
`ProfileToolbar`. It now shares the native toolbar insets, centering, layout and
scaled presentation. Painted-center delta is zero at 16/28/32/64; the 56-device-
pixel, scale-2 path retains 28 logical pixels. No pixel compensation, DPI hack,
tool identity, persistence, profile grouping or command semantic changed.

## Validation and operational impact

Focused acceptance covers 12 S4/R1/R2 methods, the 76-method retained S2 Desktop
selection and the 20-method retained S2/S3 shared selection. It checks fresh and
migrated values, the aligned grid, all six live owners, Text controls and button
height, toolbar 16/28/32/64 px, scaled native geometry, persistent-tool alignment,
the full independence matrix, construction/GeoText containment and Classic
neutrality.

The first R2 PHASE receipt is preserved as rejected historical evidence:
`verification-8e5b5cb792d24e469e84548307862984`, candidate
`2c76ba9b4a0e01a7089831fd40aefdc11a710b7c`, tree
`8d4e7efcb15c906457ccf86c80df6b642e8617d8`,
`REJECTED_VERIFICATION_CORE / UNTRUSTED`. All product checks passed; the sole
cause was `Executed JUnit identities differ from the tracked selection
inventory` after the temporary S4 count became 13 while the inventory still
declared 12. This receipt is not a PASS and is not superseded for that tested
candidate.

The bookkeeping was corrected without changing production code by retaining
the full painted-geometry coverage under the existing tracked alignment-test
identity. The author-authorized replacement PHASE
`verification-6672b2c96f9145f388148105c543239c` tested the exact final candidate
and ran 108 tests: S2 Desktop 76, S4 Desktop 12 and S3 shared 20, with zero
failures, errors or skips. It returned `ACCEPTED / COMPLETE` with zero
diagnostic findings. Execution-plan hash:
`9a876ad59c098d3e75920e1e316ce20ed5474edec0fea5b62ab96f45638a26d1`;
result hash:
`a222fa916fb5481ce8fb46410ecbfcfe1df272610723fcc47ba45f9eaabe7785`.

- `GUIDE_IMPACT = UPDATED`: user guide and second smoke checklist describe R1.
- `BOOTSTRAP IMPACT = NO_CHANGE_REQUIRED`: no toolchain, runtime, permission or
  workstation prerequisite changed.
- Verification impact: the accepted inventory remains 12 methods. R2 changes no
  verification authority or inventory, so no new `INFRA_UNIT` is required.
- `INFRA_UNIT`: an initial pre-execution catalog-hash rejection and a subsequent
  stale 4-method inventory assertion were corrected; the final infrastructure
  run is `ACCEPTED / COMPLETE` with zero diagnostics.
- No INTEGRATION/FINAL escalation is justified by this bounded frontend scope.

## Author evidence and governance disposition

The author reports the final interactive smoke as **PASS**, specifically
confirming the default 28 px toolbar, persistent/native vertical alignment and
no visible regression in the previously accepted S4/R1 presentation behavior.
This is author evidence, not an automated result. The author explicitly approves
R2, R1 and S4 and authorizes documentary closeout and ordinary fast-forward
publication. This is not agent self-approval.

D1, P1, G9B, G9C, G9U2, further G12 and productive G10 remain untouched and
unauthorized. The next gate remains designed but not authorized.

```text
PRE-G9B-S4-R2 — PASS — AUTHOR APPROVED
PRE-G9B-S4-R1 — PASS — AUTHOR APPROVED
PRE-G9B-S4 — PASS — AUTHOR APPROVED
selfApproved=false
NEXT: PRE-G9B-D1 — DESIGNED — RESEARCH/IMPLEMENTATION NOT YET AUTHORIZED
```

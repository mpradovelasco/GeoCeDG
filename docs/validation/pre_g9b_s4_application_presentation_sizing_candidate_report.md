# PRE-G9B-S4 independent application presentation sizing candidate report

## Candidate identity and authority

- Entry commit: `bd39099f668de81e9e65c415b28995de7b1c846b`
- Entry tree: `f41cd5050ff013b1f073e5754fe1ccf3feb96a98`
- Entry source: fetched `origin/main`; local `main` was byte-identical and clean.
- Required predecessor: `PRE-G9B-S3 — PASS — AUTHOR APPROVED`, confirmed in
  the published S3 closeout.
- Authorization: productive `PRE-G9B-S4` only.
- Candidate commit/tree: the immutable commit carrying this report, identified
  by the final PHASE receipt and the author handoff; the report does not embed
  its own Git object and thereby avoids a self-referential commit.
- `selfApproved=false`; author smoke remains `PENDING`.

## Architecture and ownership

S4 changes the Desktop/application frontend only. It adds no construction
geometry, dependency, serialization or document semantics.

Before S4, `FontSettings` separated construction `appFont` from `guiFont`, but
menu/component typography, Algebra, Construction Protocol and Euclidian UI text
consumed broad plain/application fonts, while toolbar rendering and resource
selection were implicitly derived from GUI/font values. S2 already owned the
construction-object font list including 10 pt; S3 already owned GeoText
world-zoom scaling.

After S4, the GeoCeDG application has five explicit user-preference owners:

| Preference | Key | Supported values | Missing/invalid fallback | Live target |
|---|---|---|---|---|
| Menu font | `geocedg.presentation.menu-font-size.v1` | `Util.MENU_FONT_SIZES` | inherited effective GUI/menu size | product and inherited menus |
| Toolbar icon | `geocedg.presentation.toolbar-icon-size.v1` | 16, 20, 24, 28, 32, 40, 48, 56, 64 px | inherited rendered icon size | native/profile toolbar rebuild |
| Algebra font | `geocedg.presentation.algebra-font-size.v1` | `Util.MENU_FONT_SIZES` | inherited Algebra/plain size | tree, renderer, editor and helper presentation |
| Construction Protocol font | `geocedg.presentation.construction-protocol-font-size.v1` | `Util.MENU_FONT_SIZES` | inherited protocol/plain size | table, header, navigation and context menu |
| Graphics font | `geocedg.presentation.graphics-font-size.v1` | `Util.APP_FONT_SIZES` | inherited construction/view UI size | Graphics and Graphics2 UI/label typography |

The five values may share supported-size helpers but are stored and selected
independently. They use the existing installed `GeoGebraPreferencesD` store,
not a second framework and not `.cedg`/`.ggb` XML. The GeoCeDG Advanced options
surface replaces the inherited mixed GUI-font row with one compact localized
panel; Classic retains the original row and default ownership.

## Defaults, migration and live behavior

On first S4 startup, every absent or invalid versioned key is normalized to the
nearest supported *currently effective inherited value* and materialized once.
This preserves an existing installation's observable appearance rather than
reinterpreting one old general-font preference as five linked settings. Fresh
defaults remain the inherited 16 pt fonts and 32 px toolbar rendering.

Selections persist immediately and refresh only their target category. They do
not call `setUnsaved()` and do not alter document state. Graphics and Graphics2
share one value. Graphics ownership includes only typography already governed
by the Euclidian view. `DrawText` deliberately retains the construction
application font as GeoText's logical S3 base, so the Graphics preference does
not mutate or rescale GeoText.

The toolbar value now owns both displayed size and maximum/native resource
selection. Existing 32/64-pixel and scalable resources remain the source;
profile SVG actions rerender from their native scalable source before downscale.
Menu or construction-font changes no longer select toolbar size implicitly.

## Preserved contracts

- S2 construction-object font meaning and 10 pt support are unchanged.
- GeoText logical font properties remain construction-owned.
- S3 world-anchored Text scaling remains
  `logical construction font × view x-scale / standard scale`.
- Screen-fixed Text behavior and construction XML remain unchanged.
- Classic hooks return the exact inherited fonts/view size and no product panel.
- No document format, geometry, toolbar taxonomy, action or protocol semantics
  changed.

## Focused evidence before the immutable PHASE

| Command / authority | Result |
|---|---|
| shared + Desktop compile | exit 0 |
| `PreG9BS4PresentationSizingTest` (4 methods) | exit 0 |
| retained S2 Desktop + shared and retained S3 shared selections | final focused run exit 0; an initial 1/80 high-DPI flyout identity regression was corrected by reusing the popup icon instance |
| Desktop main/test and shared main checkstyle | exit 0; initial three warnings corrected |
| `INFRA_UNIT` | first run rejected only stale inventory-count assertion; corrected second run exit 0, `ACCEPTED`, coverage `COMPLETE`, 0 diagnostics |
| `git diff --check` | exit 0 before candidate formation; repeated at closeout |

The final, exactly-once `PHASE -Phase PRE-G9B-S4` receipt is generated against
the committed clean candidate under `artifacts/verification/PRE-G9B-S4/phase/`
and is reported without mutating that candidate.

## Operational impact

- `GUIDE_IMPACT = UPDATED`: the living user guide documents the five controls,
  ownership boundary and pending-review state.
- `BOOTSTRAP IMPACT = NO_CHANGE_REQUIRED`: S4 adds no JDK, Gradle, Conda,
  environment, native runtime, platform permission or workstation prerequisite.
- Verification infrastructure impact: additive S4 JUnit selection, PHASE plan
  and pure-plan inventory assertions; therefore `INFRA_UNIT` was required.
- Required final level: bounded `PHASE -Phase PRE-G9B-S4`; no COMPOSED/FULL
  escalation is justified by the frontend-only scope.

## Governance disposition

No push or tag is authorized. D1, P1, G9B, G9C, G9U2, further G12 and productive
G10 are untouched and remain unauthorized by this task. Technical acceptance
cannot approve the result or the author smoke.

```text
PRE-G9B-S4 — IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
```

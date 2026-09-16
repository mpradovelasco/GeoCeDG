# PRE-G9B-P0 presentation themes, Preferences consolidation and 1.0.0 baseline

## Candidate identity and authority

- Published entry before P0: `480531cdfb0ca9df4ab278496015efcc84d67236`,
  tree `f1ff998bc027072cee817891c5baba7464fd58ed`.
- Required predecessor: `PRE-G9B-D1 — PASS — AUTHOR APPROVED` (PROFILE NC).
- Branch: `feature/pre-g9b-p0-presentation-themes`.
- Implementation candidate (pre-correction): `f00e4cc45fc95408ff0d18198d95140bf097b7ea`,
  tree `35cc8ada36c2a27d4b6d1513d80a4762e50d69ab`.
- Final verified candidate (author-approved, `1.0` -> `1.0.0` display
  correction applied): `14b4eb135b090a412154a9b2fbfbef85f6705bc5`, tree
  `7eef19e2251361637b64377e72993311fd31301a`.
- Status: **PASS — AUTHOR APPROVED**.
- Author smoke: **PASS** —
  [checklist](pre_g9b_p0_author_smoke_checklist.md).
- `selfApproved=false`; `authorApproved=true`; `passClaimed=true`. This gate does
  not authorize `PRE-G9B-P1`, `G9B`, `G9C`, `G9U2` or any public release.

## Scope

P0 was authorized by the author between D1 and P1 and owns exactly four things:
a closed three-preset application presentation theme, the consolidated
`Layout & Presentation` preferences tab, the relocation of the `PRE-G9B-S4`
presentation sizes into that tab, and fixing the canonical product version at
`1.0.0`. The normative contract is
[application-presentation-theme.md](../../geocedg/specs/ui/application-presentation-theme.md).

The gate-identifier discrepancy is recorded rather than resolved unilaterally:
no other `PRE-G9B-*` series starts at `0`, but `G9U0` preceding `G9U1` is an
existing precedent for a zero-indexed slice, so the author-proposed
`PRE-G9B-P0` was used unchanged.

## Architecture found and used

- The Desktop "Preferences" surface is the Properties view
  (`App.VIEW_PROPERTIES`) hosted by `PropertiesDockPanel`; tab order is the
  `OptionType` enum order and there is **no Apply/OK/Cancel transaction**.
  `OptionsLayoutD.applyModifications()` is an intentional no-op and every
  control applies live. P0 keeps that contract.
- `PRE-G9B-S4` owns six versioned preference keys in the GeoCeDG user-preference
  store, outside every XML. Graphics and Graphics2 deliberately share one
  Graphics preference, so there is no separate Graphics2 key; the sixth family
  is General UI font.
- FlatLaf 3.7 is already the installed look and feel (`AppD.setLAF`), and
  runtime `UIManager` mutation plus a component-tree refresh is already the
  accepted pattern in this fork (`FontManagerD`).
- There is **no reset/restore-defaults control** reachable in the GeoCeDG
  Preferences surface, so the existing contract is preserved: a profile without
  a stored theme resolves to `Original`.

## Theme identity and persistence

`GeoCeDGPresentationTheme` is a closed enum with a stable semantic identity and
a single palette authority; `GeoCeDGThemePreference` persists that identity
under `geocedg.presentation.theme.v1` through the same store seam as the S4
sizes. An absent, empty, unknown or unparsable value resolves to `Original` and
is materialized. The localized display text is never the identity.

| Theme | Identity | frame | surface | panel | canvas | border |
|---|---|---|---|---|---|---|
| Original | `original` | — | — | — | — | — |
| Scientific Paper | `scientific-paper` | `#E5E2DC` | `#ECE9E3` | `#F5F3EE` | `#FFFDF8` | `#C8C4BC` |
| Cool Geometry | `cool-geometry` | `#DEE4E9` | `#E7ECF0` | `#EFF3F6` | `#F8FAFC` | `#BBC4CC` |

## Canvas treatment and why documents stay clean

`<bgColor>` is written unconditionally by `EuclidianView.getXML` from
`getBackgroundCommon()`, which on Desktop reads the Swing panel color, and it is
read back into `EuclidianSettings`. No application-versus-document split existed
anywhere in the tree.

P0 therefore adds a **paint-only** seam: `EuclidianView.clearBackground` asks
`App.getPresentationBackground(documentBackground)`, which returns `null` by
default. `AppGeoCeDG` returns the preset canvas only when a non-`Original` theme
is active **and** the document background equals the application default
background role. `EuclidianSettings`, `getBackgroundCommon()` and `getXML` are
untouched, so a theme can never reach a `.cedg`, a `.ggb` or the preferences
XML, never marks a construction modified, and never overrides a background the
user chose explicitly. No color-similarity heuristic is used; the comparison is
exact equality against the one known default role value.

Known limitation, accepted by the author in advance: a document whose background
is explicitly pure white is indistinguishable from the application default and
is painted with the preset canvas. `Original` restores it exactly.

## Chrome treatment

Presets write known look-and-feel roles (frame/client, menu-toolbar-popup
surface, list/tree/table panel surface, separator/border/grid) and the
GeoCeDG-owned roles `GeoCeDG.presentation.{frame,surface,panel,border}...`. The
few shared Desktop classes that hard-coded a presentation color now read the
GeoCeDG role and keep their inherited literal when it is undefined, so Classic
is unaffected by construction.

The inherited startup path builds menus and toolbars before the product instance
exists, so `AppGeoCeDG` applies the roles in its constructor and reasserts them
in `updateComponentTreeUI()`, reusing the existing Desktop refresh rather than an
ad-hoc repaint. Selection, focus and tool-active colors are unchanged.

## Controls moved out of Advanced

All six S4 controls moved from the Advanced tab to `Layout & Presentation`:
General UI font, Menu font, Toolbar icon, Algebra font, Construction Protocol
font and Graphics font. Keys, fresh defaults (12/14/28/12/12/12), supported
values, migration, live application, ownership precedence and Classic neutrality
are unchanged. The controls exist exactly once; the Advanced tab shows neither
them nor the inherited mixed GUI-font row for a product that owns presentation
sizing.

## Version baseline

`packaging/windows/package.yml` remains the single version authority; exactly one
line changed, `application.version` `0.9.0` -> `1.0.0`. `application.upgrade_uuid`
is unchanged at `b52d8e6d-3996-4bc5-b9ba-4f51f73c6e44`, and
`distribution.status` remains `internal-evaluation` with
`public_redistribution` still blocked. Derived surfaces recompute from the
generated build provenance: the observed window title is `GeoCeDG 1.0.0`. The
author's smoke correction removed the inherited trailing-`.0` truncation from
`GeoCeDGBuildProvenance.getDisplayApplicationVersion()`, so the display form is
now always the exact package-version authority; no second literal was
introduced.

Hand-maintained consumers updated in the same change: `GeoCeDGProfileTest`, the
two normative UI specs, the workspace architecture note, the packaged
construction quick guide, the living licensing prose that named the version, and
the repinned `packaging.source-authority` hashes. Measured D1 evidence was
deliberately **not** rewritten: `component-audit.json` keeps the audited
`0.9.0` package version and the `GeoCeDG-0.9.0-windows-x64-internal.*` artifact
names and hashes, and the historical validation reports are untouched.

`1.0.0` is a version baseline only. It is not a release, not a distribution-state
promotion and not an enablement of any experimental feature.

## Roadmap and P1 reconciliation

The roadmap gains a `PRE-G9B-P0` row and sequence entry, and the stale track
paragraph that still described D1 as open without PASS was corrected. `P1` keeps
the public-surface decision, the public LocusV2/SplineV2 defaults, the
independent extended-DXF default, startup/public-readiness, package/release
readiness within the approved D1 legal scope, and its smoke and promotion
evidence. It no longer owns the `0.9.0` -> `1.0.0` transition. P1 is not started
and not authorized.

## Verification

| Run | Profile | Result |
|---|---|---|
| Focused P0 suite | `:desktop:desktop:test --tests PreG9BP0PresentationThemeTest` | 18 tests, 0 failures, 0 errors, 0 skipped |
| S4/S2/profile regression | `PreG9BS4PresentationSizingTest`, `PreG9BS2ConstructionFontTest`, `GeoCeDGProfileTest` | 21 tests, 0 failures |
| Full Desktop module | inventory selection `final.desktop` | 1458 tests, exit 0 |
| Checkstyle | `:desktop:desktop:checkstyleMain/Test`, `:shared:common:checkstyleMain` | 0 violations |
| `INFRA_UNIT` | `verify.ps1 -Profile INFRA_UNIT` | `ACCEPTED / COMPLETE`, 0 diagnostics |
| `STATIC` | `verify.ps1 -Profile STATIC` | `ACCEPTED / COMPLETE`, 1 pre-existing recovery-protocol governance diagnostic |
| `PHASE PRE-G9B-P0` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-P0` | recorded with the frozen candidate |

The registered selector is `PRE-G9B-P0`, backed by the new
`junit.desktop.pre-g9b-p0.producer`/`.semantic` node pair and the inventory
selection `pre-g9b-p0.desktop` (35 identities: 18 P0, 12 S4, 5 profile). Adding
test methods changed the tracked desktop discovery (1446 -> 1464) and
`final.desktop` (1440 -> 1458); both were regenerated from real producer
evidence rather than edited by hand, and the drift assertions in
`verification-final-coverage.Tests.ps1` were updated accordingly.

- `GUIDE_IMPACT = UPDATED`: the user guide documents the tab, the presets, the
  canvas rule, its limitation and the `1.0.0` baseline; the S4 paragraph's
  navigation path was corrected.
- `BOOTSTRAP IMPACT = NO_CHANGE_REQUIRED`: no toolchain, runtime, permission or
  workstation prerequisite changed.
- Verification impact: the inventory and registry changed, so `INFRA_UNIT` was
  required and was rerun.
- No `INTEGRATION`/`FINAL` escalation is claimed here. `FINAL` carries closeout
  semantics and belongs to an author-approved closeout, not to this candidate.

## Interactive evidence produced by the agent

The real application was launched for each preset and the window captured and
sampled.

| Point | Original | Scientific Paper | Cool Geometry |
|---|---|---|---|
| menu bar | `#F2F2F2` | `#ECE9E3` | `#E7ECF0` |
| toolbar | `#F2F2F2` | `#ECE9E3` | `#E7ECF0` |
| frame/dock header | `#F2F2F2` | `#E5E2DC` | `#DEE4E9` |
| Algebra panel | `#FFFFFF` | `#F5F3EE` | `#EFF3F6` |
| Construction Protocol panel | `#FFFFFF` | `#F5F3EE` | `#EFF3F6` |
| Graphics canvas | `#FFFFFF` | `#FFFDF8` | `#F8FAFC` |
| protocol header (active blue) | `#82CCF7` | `#82CCF7` | `#82CCF7` |
| axis label text | `#2F2F31` | `#2F2F31` | `#2F2F31` |

Every themed value equals the authored palette exactly. The `Original` capture
after the change is identical to the capture taken before it: 0 differing pixels
out of 1 360 900. The window title read `GeoCeDG 1.0.0` in every run, and
`geocedg.presentation.theme.v1` round-tripped through the real profile file
across restarts.

Two items are explicitly **not** covered by agent evidence and are left to the
author smoke: applying a theme live from the dialog without restarting, and the
visual inspection of the consolidated tab. The Properties view registers and
unregisters correctly when invoked from the Options menu, but it did not surface
a visible window in the agent's maximized session; this behavior was not
introduced by P0, no exception was logged, and the tab's composition is asserted
by the focused suite against the real `OptionsLayoutD` and `OptionsAdvancedD`
component trees.

## Author-approved correction: visible version `1.0` -> `1.0.0`

After executing the smoke checklist, the author reported **PASS** with one
required correction: the visible product version must always read `1.0.0`, never
the abbreviated `1.0` form. `GeoCeDGBuildProvenance.getDisplayApplicationVersion()`
no longer truncates a trailing `.0` from the package-version authority, so the
display form is always the exact value of `packaging/windows/package.yml`
`application.version`. No second version authority was introduced,
`application.upgrade_uuid` and `distribution.status` are unchanged, and no
packaging identity was regenerated. The live window title was reconfirmed as
`GeoCeDG 1.0.0`, and the focused suites (`PreG9BP0PresentationThemeTest` 18/18,
`PreG9BS4PresentationSizingTest` 12/12, `GeoCeDGProfileTest` 5/5,
`PreG9BS2ConstructionFontTest` 4/4) were rerun clean. `PHASE PRE-G9B-P0` was
re-verified against the final candidate:
`verification-d919002ce43b413e9172751205af4ac4`, `ACCEPTED / COMPLETE`, 0
diagnostics, plan hash `3e8cd2051b6e6d0c5689337da01b2d264e93b218b79df32c485008b144e85597`,
result hash `cfada470f99f9242fedaeb6f5341422a1b44f543734d3682b6c0dc792242240e`.

## Governance disposition

The author executed the [smoke checklist](pre_g9b_p0_author_smoke_checklist.md)
and declared the result **PASS**. The three available presentation themes remain
`Original` (historical default), `Scientific Paper` and `Cool Geometry`; a theme
is a presentation preference, never geometric authority. The `PRE-G9B-S4`
presentation sizes remain consolidated in `Layout & Presentation`, moved out of
`Advanced`, with a single active preference authority for those six values (no
duplicate owner). The canonical visible version adopted at this gate is `1.0.0`.

```text
PRE-G9B-P0 — PASS — AUTHOR APPROVED
selfApproved=false
authorApproved=true
passClaimed=true
candidate = 14b4eb135b090a412154a9b2fbfbef85f6705bc5
tree = 7eef19e2251361637b64377e72993311fd31301a
AUTHOR SMOKE — PASS
NEXT: PRE-G9B-P1 remains DESIGNED — PROMOTION NOT YET AUTHORIZED; this closeout
does not start or authorize it
```

D1 remains closed for PROFILE NC, P1 is not started or authorized, and G9B, G9C,
G9U2, further G12 and productive G10 remain untouched and unauthorized.

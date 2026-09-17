# POST-P1-DOC-HELP — user-guide audit matrix

- Track: `POST-P1-DOC-HELP` — bilingual user guide and in-app help integration
- Nature: documentation + frontend/help + packaged resources
- Status: `IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`
- `selfApproved=false`, `authorApproved=false`, `passClaimed=false`
- Product effect: none. No kernel, geometry, DAG, serialization, semantic
  identity, metric, intersection, DXF algorithm, packaging policy or version
  change.

This matrix is the pre-writing audit required before any prose was produced. It
records, per guide section, the claim that the guide is allowed to make, the
authority that backs it, the current status of that authority, the wording used
in each edition, the executable evidence, the known limitation, and the
prospective book relevance. It is evidence, not an authority: the durable
sources named in the `source authority` column prevail.

The two audited guides are:

```text
docs/user/geocedg_user_guide_en.md
docs/user/geocedg_user_guide_es.md
```

Section identifiers below are the stable logical IDs carried by both editions as
`<!-- geocedg-guide-section: <id> -->` markers. Visible numbering is not
identity.

## 1. Section-level audit

| guide section | claim / workflow | source authority | current status | EN wording | ES wording | test or code evidence | known limitation | book relevance |
|---|---|---|---|---|---|---|---|---|
| `what-is-geocedg` | GeoCeDG is a CeDG-oriented dynamic-geometry application, not a solid-first CAD modeller; construction and result are distinct | `AGENTS.md` §1, §0 | normative, current | What is GeoCeDG | Qué es GeoCeDG | — (conceptual) | none | II-01, I-04 |
| `what-is-geocedg` | Version shown by the product is `1.0.0`; it is not a public release | roadmap head; `packaging/windows/package.yml` | `PASS — AUTHOR APPROVED` (PRE-G9B-P0/P1) | `GeoCeDG 1.0.0` | `GeoCeDG 1.0.0` | `PreG9BP1PublicSurfaceTest.packageProfileKeepsOneVersionAuthorityAndUpgradeIdentity` | version is not a publication authorization | II-01, IX-02 |
| `getting-started` | Product languages are English and Spanish, fallback English | `apps/geocedg/application-profile.yml` `product_policies.languages` | current | Product language… | Idioma del producto… | live profile parse | Classic keeps the upstream language selection | II-03 |
| `getting-started` | Three closed presentation themes: `Original` (default), `Scientific Paper`, `Cool Geometry` | `geocedg/specs/ui/application-presentation-theme.md` | `NORMATIVE — PASS — AUTHOR APPROVED` | Original / Scientific Paper / Cool Geometry | Original / Papel científico / Geometría fría | `PreG9BP0PresentationThemeTest` | presentation only; never geometry, export or document content | II-03 |
| `getting-started` | Six independent presentation sizes under Preferences → Layout & Presentation | `geocedg/specs/ui/application-presentation-sizing.md` | `NORMATIVE IMPLEMENTED — PASS — AUTHOR APPROVED` | Presentation sizes | Tamaños de presentación | `Presentation.LayoutTab` / `Presentation.Sizes` keys in the live profile | user preference, never serialized into the construction | II-03 |
| `interface-and-workflow` | Menus are File, Edit, View, Construction, Options, Automation, Help; one catalog of 112 actions | `apps/geocedg/application-profile.yml` `menu_sections`, `actions` | current live v2 profile | File / Edit / View / Construction / Options / Automation / Help | Archivo / Editar / Vista / Construcción / Opciones / Automatización / Ayuda | `G9U1WorkspaceSurfaceTest`, `G9U1ActionRegistryTest` | a duplicated action appears once | II-03 |
| `interface-and-workflow` | Continuity is locked OFF in the GeoCeDG product profile | `apps/geocedg/application-profile.yml` `product_policies.continuity` | current | Continuity is OFF | Continuidad desactivada | live profile parse | Classic remains separately configurable | II-03, IV-04 |
| `documents` | `.cedg` is the native document; `.ggb` is compatibility input | `geocedg/specs/ui/native-document-identity.md`; profile `serialization` | `NORMATIVE / AUTHOR APPROVED` | native / compatibility | nativo / compatibilidad | `G9U1NativeLifecycleReviewTest`, `G9S1NativeArchivePersistenceTest` | archives keep the upstream `classic` app code; upstream cannot be promised GeoCeDG-only types | II-03, AP-D |
| `basic-geometry` | Ordinary points, lines, segments, rays, circles, conics, polygons, parameters, text and similarity tools are inherited and unchanged | live profile groups `construction-*` | current | Basic geometry | Geometría básica | profile/action catalog | the guide is not a complete GeoGebra manual | III-01 |
| `locus-v2` | `LocusV2` is a public command with explicit generator, parameter and domain; the drawn stroke is derived | `geocedg/specs/locus/locus-v2-semantics.md`, `locus-v2-public-surface.md`; `command.properties` `LocusV2.Syntax` | `NORMATIVE / AUTHOR APPROVED`; maturity `experimental`, default ON in GeoCeDG since PRE-G9B-P1 | Locus V2 | Locus V2 | `G9U1MetricReviewTest.ordinaryLocusQuickGuideUsesTheSameSemanticMetricAuthority` | not a generic `Path`; GeoCeDG-only | IV-01, IV-02 |
| `locus-v2` | No launch argument is required; `--enableLocusV2=false` is a retained diagnostic override | roadmap PRE-G9B-P1 row; `AppConfigGeoCeDG` | `PASS — AUTHOR APPROVED` | `--enableLocusV2=false` | `--enableLocusV2=false` | `PreG9BP1PublicSurfaceTest.historicalArgumentsRemainAcceptedBidirectionalOverrides` | Classic never acquires V2 creation | IV-05 |
| `spline-v2` | `SplineV2({...},degree)` produces a semantic curve; points use an explicit branch key and canonical parameter | `geocedg/specs/curves/semantic-spline-2d.md`; `AlgoSplineV2.BRANCH_KEY` | `PASS — AUTHOR APPROVED` (G9S1) | Spline V2 | Spline V2 | `G9U1MetricReviewTest`, `G9U1ScriptWorkflowTest` | Classic `Spline` is a different, unchanged object | IV-01, IV-05 |
| `semantic-points-intersections-materialization` | `Intersect(...)` on a semantic curve yields a rich result; only locally admissible exact-token roots can be materialized | `geocedg/specs/locus/locus-v2-intersections.md`; ADR 0017 | `PASS — AUTHOR APPROVED` (G9U0-R4) | rich intersection result | resultado rico de intersección | `G9U1IntersectionSessionTest`, `G9S1NativeArchivePersistenceTest` | global completeness `NOT_ESTABLISHED` does not invalidate a valid local root | IV-04, IV-05 |
| `semantic-points-intersections-materialization` | A point that stops being admissible becomes dormant; only its own selector reactivates it; recompute never creates points | profile `product_policies.materialization`; ADR 0017 | current | dormant / reactivated | inactivo / reactivado | `G9U0R4NativeArchivePersistenceTest` | — | IV-04, AP-C |
| `lengths-and-measurements` | `Length(L)` / `Length(L,P,Q)` return ordinary numbers; `LocusLength(...)` returns rich evidence | `geocedg/specs/locus/locus-v2-metrics.md`; `CmdLength` | `PASS — AUTHOR APPROVED` (G7/G9U0) | Length / LocusLength | Length / LocusLength | `G9U1MetricReviewTest` (`M=4`, `MP=2`) | endpoints need admissible semantic provenance; cartesian coincidence is not semantic position | IV-03 |
| `lengths-and-measurements` | `Length(S,P,Q)` may be undefined when an endpoint was materialized from a SplineV2 × SplineV2 intersection | roadmap `TD-P1-SEMANTIC-LENGTH-INTERSECTION` | `OBSERVED — REQUIRES CHARACTERIZATION/DESIGN` | documented as a current limitation | documentada como limitación actual | roadmap technical-debt entry; P1 author smoke | no workaround by proximity; no date promised | IV-03, AP-G |
| `transformations` | `Translate`, `Rotate`, `Reflect`/`Mirror`, `Dilate` accept a semantic curve and produce a new semantic curve | `geocedg/specs/locus/locus-v2-similarity-transformations.md` | `PASS — AUTHOR APPROVED` (G9U0-R5) | similarity transformations | transformaciones de semejanza | `G9U0R5NativeArchivePersistenceTest` | inversion, shear, affine/projective and 3D are excluded | IV-05, VI-02 |
| `dxf-export` | Extended DXF (G9X1) is default ON in GeoCeDG, independently of Locus V2 | roadmap PRE-G9B-P1 row; `geocedg/features/experimental.yml` | `PASS — AUTHOR APPROVED` | on by default | activada por defecto | `PreG9BP1PublicSurfaceTest.promotedSurfacesAreOnByDefaultAndIndependent` | remains `experimental`, GeoCeDG-only | IX-02 |
| `dxf-export` | Fidelity is per component: `EXACT`, `APPROXIMATE`, `UNSUPPORTED`, `INVALID`; approximation guarantee is `ESTIMATED_ERROR` | `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md`; `GeoCeDGDxfExportController` | `NORMATIVE / AUTHOR APPROVED` | exact / approximate / unsupported / invalid | exacto / aproximado / no soportado / inválido | `G9X1DesktopPreflightContractTest` | not a certified global error bound | IX-02, III-04 |
| `dxf-export` | Exact DXF `SPLINE` is **not implemented**; SplineV2 exports as approximate `LWPOLYLINE` | DXF spec §4, §9; roadmap | current | `NOT IMPLEMENTED` | `NO IMPLEMENTADO` | `PreG9BP1PublicSurfaceTest.userGuideDescribesThePromotedDefaultsAndTheDxfBoundary` (re-pointed to both editions) | approximation derives from semantic geometry, never from render/viewport/zoom/DPI | IX-02 |
| `presentation-and-visualization` | Themes, fonts, toolbar icon size, zoom and navigation are presentation, never semantics | theme/sizing specs; `AGENTS.md` §4 placement rules | current | presentation | presentación | `PreG9BP0PresentationThemeTest` | zoom never changes metrics or identity | II-03 |
| `user-tools-and-automation` | Persistent `.ggt` user tools are installed explicitly into the GeoCeDG profile, not into the document | `geocedg/specs/ui/g9u1-construction-interaction.md`; `UserTools.*` labels | `PASS — AUTHOR APPROVED` (G9U1) | User tools | Herramientas de usuario | `G9U1UserToolLibraryTest` family | a tool may not override a native command; unsupported bodies fail closed | II-03, AP-E |
| `classic-compatibility-and-diagnostics` | Classic opens as a separate diagnostic process with isolated preferences and never acquires V2 creation | `Workspace.LaboratoryWarning`; `GeoCeDGActionRegistry.openDiagnostic` | current | Classic diagnostic session | sesión de diagnóstico Classic | `PreG9BP1PublicSurfaceTest.classicNeverReachesThePromotedCreationPolicy` | diagnostic route, not a normal product feature | II-03 |
| `known-limitations` | Windows is the only validated workstation platform | roadmap; `docs/validation/bootstrap_workstation_report.md` | current | validated platform | plataforma validada | bootstrap evidence | — | IX-02, AP-G |
| `known-limitations` | Locus V2 / Spline V2 keep maturity `experimental` although default ON | roadmap head; `geocedg/features/experimental.yml` | current | experimental | experimental | `PreG9BP1PublicSurfaceTest.onlyTheTwoAuthorizedFeaturesArePromoted` | default ON is not stability | IV-05, AP-C |
| `known-limitations` | Spatial semantics (`cedg.spatial.semantics`) and the legacy laboratory remain gated off | `geocedg/features/experimental.yml` | current | not available | no disponible | `PreG9BP1PublicSurfaceTest.onlyTheTwoAuthorizedFeaturesArePromoted` | G9B/G9C/G9U2/G10 are not present capabilities | V-03, AP-G |
| `command-and-workflow-reference` | Compact task → GUI route → command → result type table | live profile + `command.properties` | current | reference table | tabla de referencia | `PostP1BilingualUserGuideTest` command parity assertions | no abbreviated forms invented | AP-E |

## 2. Stale-wording audit

The following expressions are known to be stale at the audited baseline. The
focused test `PostP1BilingualUserGuideTest` fails if any of them reappears in
either edition or in a live redirect.

| stale expression | why it is stale | governing authority |
|---|---|---|
| `--enableLocusV2=true` | Locus V2 creation is default ON since PRE-G9B-P1; only the disabling direction is documented | roadmap PRE-G9B-P1 row |
| `--enableExtendedDxf=true` | extended DXF is default ON since PRE-G9B-P1 | roadmap PRE-G9B-P1 row |
| `P1 pending`, `P0 pending`, `PRE-G9B-P1: IMPLEMENTATION CANDIDATE` | both phases are `PASS — AUTHOR APPROVED` | roadmap head |
| `PUBLIC REDISTRIBUTION = BLOCKED PENDING LICENSE/ASSET APPROVAL` as the state of PROFILE NC | PROFILE NC is `APPROVED / PACKAGE-READY`; the console summary defect is tracked as `TD-P1-PACKAGING-SUMMARY` | roadmap PRE-G9B-P1 and technical-debt rows |
| exact DXF `SPLINE` implemented | `SPLINE` remains outside the approved baseline | DXF spec §4, §9 |
| `.ggb` as the native GeoCeDG format | `.ggb` is compatibility input only | native-document-identity spec |
| Classic can create Locus V2 / Spline V2 | promotion cannot leak to Classic | `PreG9BP1PublicSurfaceTest.classicNeverReachesThePromotedCreationPolicy` |
| G9B / G9C / G9U2 / G10 capabilities described as present | all remain designed and not authorized | roadmap |

## 3. Historical material used only as input

| document | used for | not used as |
|---|---|---|
| `docs/user/geocedg_user_guide.md` (pre-track content) | capability inventory, DXF boundary wording, limitation list | authority; its phase history, governance status, receipts and developer material are out of user-guide scope |
| `docs/user/geocedg_construction_quick_guide.md` | reproducible worked examples, menu routes, pedagogy | authority; its `--enableLocusV2=true` launch wording and candidate-review framing are stale |

Both were contrasted against code, specifications and tests before any statement
was carried into the new editions.

## 4. Translation glossary

| English | Español |
|---|---|
| construction | construcción |
| branch | rama |
| component | componente |
| semantic point | punto semántico |
| rich result | resultado rico |
| currentness | vigencia (currentness) |
| materialization | materialización |
| exact token | token exacto |
| approximate | aproximado |
| unsupported | no soportado |
| invalid | inválido |
| dormant | inactivo |
| admissible | admisible |
| generator | generador |
| explicit domain | dominio explícito |
| preimage | preimagen |
| orientation | orientación |

Command identifiers, object IDs, file extensions and code literals are never
translated.

## 5. Corrective continuation — audited Markdown subset

The author review of the integrated help found two usability defects: the
Markdown was shown essentially as source, and the window was too wide and could
not be resized. The corrective continuation keeps the tracked Markdown as the
single source and renders it, so the subset the two guides actually use had to
be audited before any renderer was written.

Counts below are the measured occurrences in the two tracked sources at the
candidate. They are the contract boundary of `GeoCeDGGuideRenderer`: constructs
outside this table are deliberately unimplemented.

| Construct | EN | ES | Rendered as | Note |
|---|---|---|---|---|
| ATX heading level 1 | 1 | 1 | `<h1>` | the document title, reused as the window title |
| ATX heading level 2 | 16 | 16 | `<h2>` | the sixteen guide sections |
| ATX heading level 3 | 80 | 80 | `<h3>` | subsections |
| paragraph | 573 lines | 622 lines | `<p>` | consecutive lines join into one logical text |
| fenced code block | 20 | 20 | `<pre>` | fences ` ``` ` and ` ```text `; content escaped |
| unordered list item | 60 | 60 | `<ul><li>` | `- ` only; no nesting in either source |
| ordered list item | 4 | 4 | `<ol><li>` | `1. ` only; no nesting in either source |
| pipe table row | 100 | 100 | `<table>`/`<th>`/`<td>` | delimiter row is structure, never a visible row |
| horizontal rule | 17 | 17 | `<hr>` | `---` on its own line |
| HTML comment | 17 | 17 | *removed* | the stable `geocedg-guide-section` identifiers |
| inline code | 187 | 189 | `<code>` | extracted before emphasis so markers inside code stay literal |
| strong | 153 | 147 | `<b>` | must close on the same logical block |
| emphasis | 14 | 14 | `<i>` | single `*`, applied after strong |
| link | 0 | 0 | `<a>` | none in either source; rendered non-navigable if one appears |
| image | 0 | 0 | — | out of contract |
| block quote | 0 | 0 | — | out of contract |
| setext heading | 0 | 0 | — | out of contract |
| nested list | 0 | 0 | — | out of contract |

Deliberate degradation: unrecognized markup and unbalanced inline markers stay
escaped literal text rather than producing broken layout.

### 5.1 Defect found and fixed during integration

Rendering the real sources exposed one renderer defect. A list item whose
`**strong**` span is wrapped across two source lines left its markers visible,
because list items were converted one source line at a time. The Spanish §3.1
item is such a case:

```text
- **Protocolo de construcción** (**Vista → Protocolo de construcción**) muestra
  la construcción como una secuencia ordenada, que es la lectura directa de su
  estructura de dependencias. **Vista → Mostrar barra de navegación de la
  construcción** activa o desactiva el control por pasos asociado a la vista
  Gráfica; no crea objetos ni genera un paso de deshacer.
```

The fix buffers a list item's lines and converts the joined text once, the same
way paragraphs were already handled. **The guide sources were not reflowed:**
the defect was in the renderer, so the renderer was corrected. A regression case
pins this, and a structural-parity case asserts that both editions render the
same number of `<h1>`, `<h2>`, `<h3>`, `<table>`, `<pre>`, `<li>` and `<hr>`
elements.

### 5.2 Renderer and viewer contract rows

| Guide section | claim / workflow | source authority | current status | EN wording | ES wording | test or code evidence | known limitation | book relevance |
|---|---|---|---|---|---|---|---|---|
| *(all)* | the tracked `.md` stays the single authored source; the application renders it | this track; `GeoCeDGGuideRenderer` | implemented | — | — | `PostP1GuideRenderingTest` | the renderer implements only the audited subset above | AP-H |
| *(all)* | section identifiers are structure and never reach the reader | this track | implemented | — | — | `sectionIdentifierCommentsAreNeverVisible` | — | AP-H |
| `getting-started` | the guide opens in a non-modal, freely resizable reading window | this track; `GeoCeDGGuideWindow` | implemented | Help → GeoCeDG user guide | Ayuda → Guía de usuario de GeoCeDG | `theWindowIsResizableNonModalAndScrollable` | the default size follows the screen, never the content | II-03 |

### 5.3 Operations manual correction

The focal stale-wording review of `docs/developer/geocedg_operations_manual.md`
found exactly one live statement contradicting the P1 authorities: a global
`PUBLIC REDISTRIBUTION STATUS = BLOCKED PENDING LICENSE/ASSET APPROVAL` block
presented as the current distribution status, immediately above a paragraph that
already described the correct per-profile behaviour. It is now a profile-aware
block naming `INTERNAL`, `NC` (`APPROVED / PACKAGE-READY`) and `COMMERCIAL`
(`NOT AUTHORIZED`), and it states that the build console summary still prints the
historical global phrase as the separately tracked `TD-P1-PACKAGING-SUMMARY`
reporting defect. No other live stale wording was found; historical sections
were not rewritten.

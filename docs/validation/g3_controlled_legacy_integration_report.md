# G3 controlled legacy CeDG integration report

- Date: 2026-08-10
- Branch: `feature/g3-legacy-cedg-tools`
- Base: `main` at `11effee8d0bdf861de7c95c88259e348cce37785`
- GeoGebra baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
- Baseline version: 5.4.928.0
- Final status: **PASS**

## Scope and architectural effect

G3 adds a controlled preservation, catalog, inspection, loading, comparison,
and promotion foundation for existing CeDG resources. It changes the
operational, documentation, model-curation, and Desktop-launch orchestration
layers only.

G3 does not add a kernel command, modify geometric semantics, change
serialization, alter `Locus`, implement Locus V2, import the future DSL, change
the G2 application profile, or start G4.

The governing decision is
`docs/adr/0003-controlled-legacy-integration.md`. It extends the G1 version-1
model manifest only with optional fields and keeps `tools/agent/verify.ps1` as
the composed verification authority.

## Initial legacy intake

`docs/legacy/` was inspected completely before design. It initially contained
one executable model and twelve scientific or technical PDFs:

| Initial file | Final role and location |
|---|---|
| `Templatev7.ggb` | Immutable original at `models/legacy/template-v7/original/Templatev7.ggb` |
| `Modelado-parametrico-computacional-v2.pdf` | Primary book under `docs/references/cedg/book/` |
| `CAD_18(2)_2021_272-284.pdf` | Foundation under `docs/references/cedg/foundations/` |
| `2022_CINIE_Dykinson_Asoc vistas y objetos 3D e.pdf` | Historical proof of concept under `docs/references/cedg/spatial-association/` |
| `CeDGLocusIntersect_INGEGRAF2022_vFinal_PrePrin.pdf` | LSIM preprint under `docs/references/cedg/locus-and-intersections/` |
| `YloRMT_abstract_ingegraf.2022-CeDG_en.pdf` | LSIM abstract under `docs/references/cedg/locus-and-intersections/` |
| `symmetry-15-00984-with-cover.pdf` | Intersection/flattening article under `docs/references/cedg/locus-and-intersections/` |
| `symmetry-13-00685.pdf` | Sheet-development article under `docs/references/cedg/developments/` |
| `978-3-031-72829-7_81.pdf` | Tool/oblique-cone chapter under `docs/references/cedg/developments/` |
| `DevelopableRuledSurfaces_Rev.pdf` | Developable-surface manuscript under `docs/references/cedg/developments/` |
| `1-s2.0-S1524070324000419-main.pdf` | Published discrete-elbow article under `docs/references/cedg/discrete-models/` |
| `248c6c6e-767b-4c65-af69-3f4b5a9068d1.pdf` | Discrete-elbow chapter under `docs/references/cedg/discrete-models/` |
| `ElbowDiscreteCeDG.pdf` | Discrete-elbow conference evidence under `docs/references/cedg/discrete-models/` |

`docs/legacy/` now contains only its provenance README. No scientific document
remains mixed with an executable artifact.

The reference catalog records 12 paths, citations, topic relationships,
page counts, SHA-256 values, and factual rights evidence. Three supplied
articles identify CC BY 4.0; publisher/book/proceedings restrictions and
unreviewed manuscripts remain explicit release constraints. No legal
conclusion is made.

## Controlled ingest design

`tools/legacy/ingest.ps1` supports `.ggb`, `.ggt`, `.js`, and `.ggs`. Its
explicit `-Import` mode copies bytes without transformation, refuses to
overwrite a different original, and writes a deterministic derived inventory.
`-Check` recreates that inventory in memory and fails if the committed result
is stale.

The package separates:

1. `original/Templatev7.ggb`: immutable source artifact;
2. `manifest.yml`: provenance, compatibility, rights, maturity, and load
   policy;
3. `curation.yml`: reviewed classification and architectural recommendation;
4. `derived/tool-inventory.yml`: generated structural evidence;
5. future regression metadata, intentionally absent until geometric expected
   results and governing specifications exist.

The XML and ZIP entries are inspection evidence. They do not replace the GGB
as geometric authority. Re-running import twice produced the same inventory
SHA-256. The original hash is:

`f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`

## Templatev7 inventory

The preserved document was produced by GeoGebra 5.2.879.0 Classic Desktop. It
contains 24 embedded macros, a global JavaScript resource with `ggbOnInit` and
`clickL`, two document script blocks, 22 ZIP entries, and a complete custom
toolbar definition.

All 24 tools have a stable GeoCeDG ID, exact legacy index and mode ID, ordered
inputs and outputs with accessible GeoGebra types, baseline command
dependencies, embedded-tool dependencies, script evidence, family, category,
maturity, architectural recommendation, confidence, and observations.
Unknown semantics are not inferred.

The classification totals are:

| Dimension | Counts |
|---|---|
| Maturity | 20 `legacy`; 4 `research` |
| Architectural recommendation | 14 remain external/legacy; 4 future high-level commands; 5 future DSL procedures; 1 future kernel design |
| Families | 5 planar constructions; 5 curve measurements; 4 coordinate transports; 3 locus research; 2 presentation symbols; 2 visibility/presentation; 2 sheet/scale; 1 conditional utility |

The sole `future-kernel-design` recommendation is `postLocus`, because any
future replacement affects locus semantic evaluation. The recommendation is
non-executing and authorizes no G3 kernel work.

### Authoritative legacy toolbar organization

The historical order is preserved exactly as metadata:

| Legacy group | Ordered tools |
|---:|---|
| 13 | `SymmSymbol`, `DuctSymbol` |
| 14 | `PoliLineVisibility`, `ellipseVisibility` |
| 15 | `postLocus`; `listLength`, `listLength12`; `SplineLength`; `Perimeter`, `ellipseLength12` |
| 16 | `EllipseAxis`, `CirclebyD`, `SquarebyDiagonal`, `conj2mainAxesEllipse`, `circArcbyAngle` |
| 17 | `directDimension`, `axisDimension`; `relCoor`, `pointJump`; `translationCoor`, `dummyRotate` |
| 18 | `IFPositiveSelectPoint` |
| 19 | `sheetISOAnLand`, `sheetISOAnVert` |

Subgroup delimiters, raw toolbar text, and tool order also remain in the
derived inventory. This organization is the correct legacy interface
reference, but `future_constraint` is false. It does not define the future
GeoCeDG toolbar architecture.

### Legacy locus evidence

`postLocus`, `listLength`, and `listLength12` are `research` in the
`locus-research` family. `SplineLength` is also research. The publications use
the roles `locusLength` and `locusLength12`; the template commands are actually
named `listLength` and `listLength12`.

These tools filter sampled locus points or sum sampled chords. Their numerical
and dynamic limitations are retained explicitly. They are not Locus V2, native
locus length, exact metric authority, or authorization to modify `Locus`.

## Architectural recommendations

- External/legacy: presentation symbols, sheet/scale construction, visibility
  presentation, convenience planar primitives, and sampled measurements that
  have no approved stable contract.
- Future high-level commands: dimensioning and ellipse procedures that may be
  useful application-level compositions after specification.
- Future DSL procedures: coordinate transport, rotation, conditional selection,
  and similar compositions over existing valid primitives, after the DSL gate.
- Future kernel design: only behavior whose correct replacement requires
  semantic locus evaluation and dependency-graph participation.

No recommendation was implemented in G3.

## CeDG Laboratory

`tools/legacy/open-laboratory.ps1` is an explicit, opt-in loader. It accepts
only a registered, hash-valid, non-default resource with `legacy`, `research`,
`experimental`, or `deprecated` maturity. It prints an explicit
`EXPERIMENTAL / NON-STABLE RESOURCE` warning before launch.

- Default route: `:desktop:desktop:runGeoCeDG`.
- `-Classic`: preserved `:desktop:desktop:run` diagnostic route.
- `-ValidateOnly`: manifest, hash, and deterministic-ingest checks without a
  graphical launch.

Loading `Templatev7.ggb` allows the document-owned toolbar to appear in its
historical context. It does not mutate the G2 profile or enable any legacy
tool by default. The feature manifest records stable ingest infrastructure as
`cedg.legacy.ingest` and the disabled experimental access point as
`cedg.laboratory.legacy`.

## Public model corpus

The book-linked public resource
`https://www.geogebra.org/m/nmsgff5s` is registered as
`cedg.external.geogebra-book-models`. Browser inspection on 2026-08-10 found
the title `Modelos computacionales 3D basados en CeDG`, author Manuel
Prado-Velasco, 11 chapters, and 71 model links.

Every metadata entry relates the public material ID and original URL to a book
chapter/problem label and a nullable future regression manifest. The corpus is
explicitly `build_dependency: false` with policy
`metadata-only-no-bulk-download`.

Four representative future pilot candidates are recorded but not downloaded:

- general cone-cylinder intersection (`ngdveaz8`);
- focal illumination of a sphere (`xcf3g4uu`);
- cylindrical polygonal elbow (`wsp9ktrq`);
- conical polygonal elbow (`tptusqqm`).

No public model was imported in G3. This avoids indiscriminate acquisition and
keeps provenance, rights review, expected geometric evidence, and regression
promotion as explicit later steps.

## Files created or modified

Created durable contracts and documentation:

- ADR 0003 and `geocedg/specs/legacy/controlled-integration.md`;
- four G3 JSON Schemas and optional version-1 model-manifest fields;
- G3 task prompt and this report;
- CeDG reference README, reference catalog, and public model corpus index;
- `docs/legacy/README.md`;
- `models/legacy/template-v7/` package and model catalog entry;
- `tools/legacy/ingest.ps1`, `open-laboratory.ps1`, and
  `tools/agent/verify-legacy.ps1`.

Modified operational integration:

- stable/experimental feature manifests;
- composed and operational verifiers;
- model/manifest documentation, root README, `.gitattributes`, and license
  matrix.

No file below `source/`, no G2 application-profile source, and no controlled
upstream-modification record changed.

## Validation evidence

| Validation | Result |
|---|---|
| Two consecutive `ingest.ps1 -Import` runs | PASS; identical derived-inventory SHA-256 |
| `ingest.ps1 -Check` | PASS |
| PowerShell `Test-Json -SchemaFile` for model, curation, inventory, references, and external corpus | PASS |
| `tools/agent/verify-legacy.ps1` | PASS |
| `tools/agent/verify-operational.ps1` | PASS |
| GeoCeDG Laboratory load of `Templatev7.ggb` | PASS after graceful rerun; AppID `org.geocedg.desktop`, version 5.4.928.0, Java 25.0.4 |
| Classic Laboratory load of `Templatev7.ggb` | PASS; AppID `geogebra.AppId`, version 5.4.928.0, Java 25.0.4 |
| Visual Templatev7 comparison | PASS; both routes displayed the same historical toolbar and document layout |
| Stable GeoCeDG blank launch | PASS; title `GeoCeDG`, G2 six-category toolbar, Algebra/Graphics perspective, and Classic perspective selector remained available |
| Desktop compilation during launches | PASS; Gradle launcher JDK 22, Desktop runtime/toolchain Java 25.0.4 |
| `tools/agent/verify.ps1 -RunBenchmarks` | PASS; exit 0; logs in `%TEMP%\geocedg-g3-verify-final` |
| `git diff --check` | PASS |
| Regenerable-output cleanup | PASS; no residual `build`, `.gradle`, or `.kotlin` directories |

The first graphical attempt occurred while Windows was locked. The application
and legacy resource loaded, but the process was force-closed for diagnostic
capture and Gradle therefore reported exit `-1`. It is not counted as the
passing result. After the session was unlocked, GeoCeDG Laboratory, Classic
Laboratory, and stable GeoCeDG were each captured and closed through their
window close path; all corresponding Gradle tasks completed successfully.

Screenshots are temporary validation evidence and were not committed because
they contain upstream UI assets and workstation-specific screen content.

The first composed-verifier run inside the restricted execution sandbox could
not create a Kotlin daemon marker below `%LOCALAPPDATA%` and was classified as
an environment/permissions failure. The identical command was repeated with
managed permission escalation and completed with exit code 0. The passing run
is the reported verification evidence; no source or build configuration was
changed to mask the environment failure.

## Limitations and pending work

- Tool correctness, dynamic validity domains, degeneracies, and numerical
  expected results remain to be characterized per tool before promotion.
- The legacy GGB contains embedded GeoGebra UI resources; redistribution review
  remains blocked even though the original is preserved for research.
- The public corpus is an inspected metadata snapshot, not an availability
  guarantee or local mirror.
- No pilot regression model was imported because G3 did not need one to prove
  the ingest and Laboratory mechanisms.
- The installed Computer Use package lacked the documentation API required by
  its own workflow and could not enumerate windows. Visual verification used
  a local Win32 capture after the workstation was unlocked.
- Final toolbar reorganization, native commands, DSL procedures, Locus V2,
  spatial semantics, packaging, and G4 remain outside this gate.

## Gate conclusion

G3 is **PASS**. The original resource is preserved immutably, its inventory and
legacy toolbar organization are reproducible, the Laboratory remains explicit
and disabled by default, the G2 stable profile is unchanged, and the composed
authority passes with benchmark execution. G4 was not started.

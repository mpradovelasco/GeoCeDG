# PRE-G9B-R6-plus-E1-L — curated GGT library, owned tool icons and read-only bundled catalog

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-10-07 reviewed the frozen E1
preparation candidate `T_R6PLUS_E1_PREP`, resolved `DQ-E1-1` to `DQ-E1-16`,
split `E1` into `E1-L` and `E1-P`, recorded the rights/provenance decision,
authorized the minimal `AGENTS.md` §3.2 amendment and authorized the
implementation and technical verification of **`E1-L` only**. The instruction
is recorded, versioned, in the
[E1 preparation closeout, author decisions and E1-L authorization record](../../../docs/validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md);
the rights decision in the
[curated GGT library rights record](../../../docs/licensing/curated-ggt-library-rights-record.md)
(version 1). Where this prompt and those records differ, the records prevail.
The characterization evidence is in the
[E1 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_e1_preparation_characterization_report.md),
as corrected by §6 of the authorization record. The combined
[E1 preparation prompt](pre-g9b-r6-plus-e1-ggt-library-and-packaging.prompt.md)
is historical preparation evidence, not an executable contract.

This file is an execution contract, not a second policy document: the
verification classes are defined once in
`geocedg/specs/operations/verification-levels.md` §12.8, the legacy promotion
path once in `geocedg/specs/legacy/controlled-integration.md`, and the durable
library contract once in `geocedg/specs/legacy/curated-ggt-library.md`, which
this phase creates as its first deliverable from the contract frozen here.

```text
PRE-G9B-R6-plus-E1-L =
AUTHORIZED FOR IMPLEMENTATION

identifier               = PRE-G9B-R6-plus-E1-L (author-accepted, DQ-E1-1)
registry phase id        = PRE-G9B-R6-PLUS-E1-L
selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (E1-L implementation and technical verification only)
passClaimed              = false
VERIFICATION_CLASS       = BOUNDED_PHASE (frozen, DQ-E1-2); acceptance one registered PHASE
PHASE_KIND               = GEOCEDG DESKTOP APPLICATION LAYER + CURATED MODEL CONTENT +
                           OWNED ICON ASSETS; NO KERNEL, NO GEOMETRY, NO DOCUMENT FORMAT,
                           NO PACKAGING
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED — PUBLISHED;
                           rights record version 1; the AGENTS.md §3.2 governance commit
SIBLING                  = PRE-G9B-R6-plus-E1-P = PREPARED — NOT AUTHORIZED
NEXT_SUBPHASE            = none implied; E1-P, E2, E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
The phase stops with one exact technically verified candidate pending author
review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement only:

1. the curated, GeoCeDG-owned **derived** GGT library of exactly twelve tools,
   generated deterministically from the immutable
   `models/legacy/template-v7/original/Templatev7.ggb` plus an authored curation
   record, with a generated library manifest and generated `.ggt` files;
2. twelve GeoCeDG-owned SVG icon sources and their deterministic 32 × 32 PNG
   derivatives, which exist only inside the generated `.ggt` files;
3. the read-only bundled catalog in the existing user-tool library, manager and
   `user-tools` menu, fed only from an installation-relative directory;
4. the specification, ADR 0033, the feature-set entry, the asset-manifest
   entries and the binary attribute for curated archives;
5. the focused JUnit tests, the registered `PRE-G9B-R6-PLUS-E1-L` selection
   and the evidence of this class.

Invariants the candidate establishes and tests:

```text
INV-E1L-1  Originals immutable: no byte under models/legacy/** changes;
           Templatev7.ggb SHA-256 stays
           f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113.
INV-E1L-2  Exactly the twelve DQ-E1-6 tools are curated and shipped; no
           directDimension, axisDimension, sheetISOAnLand, sheetISOAnVert or other
           template macro appears in any curated artifact.
INV-E1L-3  Every curated .ggt and library-manifest.json is generated, byte-
           reproducible from Templatev7.ggb, curation.yml and the SVG sources on the
           recorded Desktop test JDK, and never hand-edited.
INV-E1L-4  Each curated definition differs from its source macro only by the
           closed transformation set T-EXTRACT, T-ICON, T-STYLE.
INV-E1L-5  No curated archive contains a template or archive image, a GeoGebra
           stock-named image, JavaScript or a script element; each iconFile names
           the tool's owned PNG entry inside its own archive; no owned PNG is
           byte-equal to any image of Templatev7.ggb, of the 2024 archives or under
           source/.
INV-E1L-6  The bundled catalog is read-only and installation-relative: it never
           writes the user-tool store, never creates a file at startup, never seeds
           user data, and an absent directory yields an empty catalog.
INV-E1L-7  Whole-catalog integrity is fail-closed: any missing, extra or
           mismatching file, manifest field, hash or definition digest makes the
           entire bundled catalog unavailable.
INV-E1L-8  Precedence: a stored user package with a case-insensitively colliding
           command keeps precedence and the bundled entry is unavailable; a
           document macro keeps precedence over both (existing adoption /
           DefinitionMismatch / DocumentConflict policy unchanged).
INV-E1L-9  User pin state for bundled tools lives user-side in a separate sidecar,
           written only by an explicit user pin action; existing version-1/2/3 user
           stores are read and written exactly as before.
INV-E1L-10 No shared-kernel, document-format, action-catalog, build-script or
           packaging change; no curated .ggt in any build output consumed by
           packaging.
```

### Frozen contract

#### Repository structure (`DQ-E1-4`, `DQ-E1-5`)

```text
models/legacy/template-v7/original/Templatev7.ggb   immutable provenance source (unchanged)
models/curated/ggt-library/
  README.md               provenance boundary; "generated, never hand-edited"
  curation.yml            AUTHORED, JSON-compatible YAML: library id and version,
                          provenance source and hashes, recorded toolchain, style
                          policy, rights-record reference, and per tool: command,
                          source command, shipped (true), maturity (experimental),
                          rights state, licensing class, icon source, statement
                          (if any), 2024-archive provenance evidence
  library-manifest.json   GENERATED: per tool the source macro identity and SHA-256,
                          transformation set, styled outputs, resulting .ggt SHA-256,
                          user-library definition digest (v1), maturity, rights state,
                          shipped state, icon identity (SVG canonical-LF SHA-256,
                          archive entry, PNG SHA-256)
  tools/<command>.ggt     GENERATED, binary in .gitattributes
geocedg/resources/icons/ggt-library/<command>.svg   AUTHORED owned icon sources
tools/legacy/curate-ggt-library.ps1                  operator entry point (-VerifyOnly)
```

The curated library is not registered in `models/manifests/catalog.yml`;
`verify-legacy.ps1` is not changed. The 2024 archives are recorded only in
`curation.yml` and the rights record.

#### Curation transformations (closed set)

| ID | Transformation |
|---|---|
| `T-EXTRACT` | the exact `<macro …>…</macro>` text of the selected command from the template's `geogebra_macro.xml`, wrapped in the template's own prolog/root text and closing text |
| `T-ICON` | the macro start tag's `iconFile` value replaced by `<lower-case MD5 of the PNG bytes>/geocedg-ggt-<command>.png` |
| `T-STYLE` | in every macro **output** element whose type is `line`, `segment`, `ray`, `vector`, `conic`, `conicpart`, `polygon` or `polyline`: `objColor` `r`, `g`, `b` set to `0` (alpha kept) and `lineStyle` `thickness` set to `3` (`type` and `typeHidden` kept); every other element unchanged |

Each edit is an exact text replacement that must match once; anything else
fails generation. Command names, tool names, help texts (`DQ-E1-15`), inputs,
outputs, constructions, `copyCaptions`, `showInToolBar` and `eqnStyle` tags are
kept byte-for-byte.

#### Selection and statements (`DQ-E1-6`)

`SquarebyDiagonal`, `CirclebyD`, `circArcbyAngle`, `ellipseLength12`,
`IFPositiveSelectPoint`, `EllipseAxis`, `conj2mainAxesEllipse`, `pointJump`,
`relCoor`, `translationCoor`, `DuctSymbol`, `SymmSymbol`. Statements carried by
`curation.yml`, the manifest, the specification and the bundled-catalog
tooltip (profile texts, English and Spanish): `EllipseAxis` — valid only when
`C` lies on the axis through `O` perpendicular to `OB`; `pointJump`, `relCoor`,
`translationCoor` — no spatial-frame or projection authority.

#### Style (`DQ-E1-9`)

Black, GeoGebra `lineThickness` 3, line types preserved, points, texts and
numerics unchanged, through `T-STYLE` only. No remembered-style hook.

#### Icon pipeline (`DQ-E1-11`)

- One SVG per tool, `viewBox="0 0 32 32"`, LF, drawn from the tool's own
  construction; no text, raster, external reference, script or filter; never
  traced from or derived from a GeoGebra stock icon or a template/archive image.
- Rasterization in the Desktop **test** JVM through the product's existing
  SVG route (`JSVGImageBuilder`/`SVGImage`, backed by the shipped EchoSVG
  dependency) into a 32 × 32 `TYPE_INT_ARGB` image with fixed rendering hints,
  written by `ImageIO` as PNG. User space is fixed: no screen, DPI, look and
  feel or theme input.
- Byte reproduction is claimed on the recorded toolchain:
  `curation.yml` records the Desktop test JVM identity used for generation; the
  golden test reports the current identity on mismatch.
- No standalone PNG is tracked; the PNG exists inside its `.ggt` and is
  identified by hash in the library and asset manifests.

#### Archive form

`java.util.zip` with entries in fixed order (`geogebra_macro.xml`, then the
icon), `STORED` with precomputed CRC and sizes, DOS time fixed through
`ZipEntry.setTimeLocal(1980-01-01T00:00)`, no comments; macro XML UTF-8
without BOM, LF, as produced by `T-EXTRACT`.

#### Generator

`CuratedGgtLibraryGenerator` in the Desktop **test** source set (no build
script change). A golden test regenerates the whole library in memory from the
recorded inputs and byte-compares it with the tracked files; on mismatch it
writes the regenerated set under `source/desktop/desktop/build/geocedg-curated-ggt-library/`
and fails. `tools/legacy/curate-ggt-library.ps1` runs that test and, without
`-VerifyOnly`, copies a complete regenerated set into the tracked paths, then
reruns the test. The definition digest in the manifest is the user library's
own digest (version 1), computed through `GeoCeDGUserToolLibrary`.

#### Runtime: read-only bundled catalog (`DQ-E1-10`)

- **Location.** `<launcher dir>/app/ggt-library/`, derived from the jpackage
  launcher property `jpackage.app-path`; absent property or directory → empty
  catalog, no message, no file created. Tests inject the directory explicitly.
  Layout: `library-manifest.json` and `tools/<command>.ggt`.
- **Integrity.** The directory must hold exactly the manifest and the shipped
  `.ggt` files it lists, each with its SHA-256; every bundled package passes the
  same `inspect` and digest validation as a user install and its definition
  digests equal the manifest. Any failure → the whole catalog is unavailable,
  shown once in the manager and menu.
- **Presentation.** A separate "GeoCeDG library" section after the user
  packages in the existing manager dialog and `user-tools` menu, with the owned
  icon decoded from the bundled archive; Remove and custom icons are disabled
  for bundled entries. No new action id, toolbar group or profile catalog entry;
  only `UserTools.*` profile texts are added.
- **Activation.** The existing explicit activation path (`activate`/`select`,
  `registerWithHost`); document precedence unchanged.
- **Precedence.** User package collision → bundled entry unavailable
  (`UserTools.BundledShadowed`); installing a user package that collides with a
  bundled command is allowed.
- **Pins.** Bundled pins in `<preferences>.bundled-tools-v1.json` beside the
  user store: `{version 1, libraryId, pinned [{command, group, order}]}`,
  written atomically under the existing store lock only on an explicit pin,
  unpin, group or move action that touches a bundled pin; never at startup.
  Entries for commands the current catalog does not offer are kept unchanged.
  The user store file is written only when a user-package pin changes and is
  never created by a bundled action.

#### Serialization

No document-format change. Activating or using a bundled tool writes its
`<macro>` into `geogebra_macro.xml` exactly as a user-library tool does today,
with the owned `iconFile` string and without icon bytes.

#### Feature, specification and assets (`DQ-E1-14`)

- `geocedg/features/experimental.yml`: `cedg.library.curated-ggt`,
  `experimental`, specification `geocedg/specs/legacy/curated-ggt-library.md`,
  `enabled_by_default: true`, depends on `cedg.frontend.profile`. The profile
  `features` list is unchanged (authorization record §6).
- `geocedg/specs/legacy/curated-ggt-library.md` and
  `docs/adr/0033-curated-ggt-library-and-distribution-boundary.md`, approved
  through the `E1-L` author closeout.
- `geocedg/resources/assets-manifest.yml`: a new top-level array
  `ggt_library_assets` with one entry per tool (SVG source canonical-LF SHA-256,
  PNG archive entry and raw SHA-256, derivation, provenance,
  `geocedg-documentation-or-ordinary-art`, trademark role none,
  redistribution per the rights record). Status string, schema version,
  `deliberate_exclusions` and every existing entry unchanged.
- `.gitattributes`: `models/curated/**/*.ggt binary`.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
PUBLISHED_BASE        = 23b971f5a967958f93e745ca8c39fce1162706e6   (P_R6PLUS_C_X1;
                        tree 5f564c8391828dc781dee0b1be93ef223431af58)
PREPARATION           = 0ce52a965bcf8b9de37a1572738c0ad79db52354   (T_R6PLUS_E1_PREP;
                        tree e500e80fddf957078249383ae61e28e6f707d455; never amended)
AUTHORIZATION         = the documentary commit that adds this prompt, child of
                        T_R6PLUS_E1_PREP
BRANCH_START          = the governance commit, child of AUTHORIZATION, whose only
                        change is the AGENTS.md §3.2 amendment
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-e1-l-curated-ggt-library (local)
```

The candidate report records the exact SHAs and trees of `AUTHORIZATION` and
`BRANCH_START`. Entry gate: local `main`, `origin/main` and the live remote
`main` agree with `P_R6PLUS_C_X1`; the worktree is clean; `C-X1` is still
`PASS — AUTHOR APPROVED — PUBLISHED`; `Templatev7.ggb` has the hash of
`INV-E1L-1`. If the live remote `main` changed, stop and reconcile instead of
rebasing.

## Authority and evidence hierarchy

1. `AGENTS.md` (as amended by the governance commit), then current code, build
   and tests at the base.
2. `geocedg/specs/operations/verification-levels.md`, the typed registry, the
   controlled-integration contract.
3. The author records: the authorization record and the rights record.
4. The characterization report and its JSON mirror, as corrected by the
   authorization record (evidence, not authority).
5. Scratch probe outputs and session notes (lowest rank).

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Curated content: `models/curated/ggt-library/**`; the `.gitattributes` rule.
- Owned icons: `geocedg/resources/icons/ggt-library/*.svg`; the new
  `ggt_library_assets` array of `geocedg/resources/assets-manifest.yml`.
- Tooling: `tools/legacy/curate-ggt-library.ps1`.
- Desktop application layer, GeoCeDG-owned classes only:
  `GeoCeDGUserToolLibrary.java`, `GeoCeDGUserTools.java` and new classes under
  `source/desktop/desktop/src/main/java/org/geocedg/desktop/`.
- Profile texts: new `UserTools.*` keys in `apps/geocedg/application-profile.yml`
  (English and Spanish); no action, group, toolbar, feature or other profile
  change.
- Specification, ADR and feature: `geocedg/specs/legacy/curated-ggt-library.md`,
  `docs/adr/0033-curated-ggt-library-and-distribution-boundary.md`, the
  `geocedg/features/experimental.yml` entry.
- Tests: new Desktop tests under `source/desktop/desktop/src/test/java/org/geocedg/desktop/`
  (including the generator), their registration in
  `docs/upstream/modified-files.yml` where the upstream-boundary contract
  requires it, the phase selection, the JUnit inventory through the official
  updater and the registry-shape pins that registration moves.
- The candidate report, its machine-readable evidence, and the roadmap and
  mini-track status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any byte under `models/legacy/**`; `verify-legacy.ps1`;
  `models/manifests/catalog.yml`; ingesting the 2024 archives.
- Curating, shipping or copying `directDimension`, `axisDimension`,
  `sheetISOAnLand`, `sheetISOAnVert` or any other template macro; curated-but-
  unshipped copies; recreating native sheet or dimension functionality.
- Any GeoGebra stock icon, template or archive image, the template's JavaScript,
  defaults, thumbnail or `geogebra.xml` inside the curated library.
- Hand-editing a generated `.ggt` or manifest.
- Packaging: `packaging/**`, `tools/release/**`, `tools/agent/checks/**`,
  `windows-packaging.md`, `NOTICE.md`, `THIRD_PARTY.md`, `component-matrix.md`,
  `LICENSES/**`, the `assets-manifest.yml` exclusions; any route that puts a
  curated `.ggt` into `installDist`, a JAR or an app image (`E1-P`).
- Build scripts (`*.gradle*`, `gradle/**`), shared kernel and shared modules
  (`source/shared/**`), upstream (`org/geogebra/**`) classes, `Macro`,
  `AlgoMacro`, `MacroManager`, `MyXMLioJre`, Classic, document format, undo,
  the version-3 user-store format, the user-library install rules for user
  packages, the action catalog, toolbar groups and the profile `features` list.
- A remembered-style frontend hook; any tool rename or help-text change.
- The About surface and any product-version change
  (`ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`, owner `G`).
- `E1-P`, `E2` (including `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`),
  `E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7`, `G9B`, and every other retained
  debt or observation.
- Any legal conclusion; publication.

## Architectural placement

GeoCeDG Desktop application layer (user-tool library, manager and menu),
curated model content under `models/curated/`, owned resources under
`geocedg/resources/`, operator tooling under `tools/legacy/`. No kernel
placement: the curated tools are ordinary macros over existing primitives; the
library is not a geometric authority (`AGENTS.md` §4).

## Required design/specification

`geocedg/specs/legacy/curated-ggt-library.md` (first deliverable; the frozen
contract above, the invariants and the four-way distinction behavioral
reference / source-provenance / asset ownership / redistribution permission)
and ADR 0033 (curated model category, rejected alternatives, split rationale,
bundled catalog versus first-run seed). Both are approved only through the
`E1-L` author closeout.

## Geometric invariants and degeneracies

No geometric object, algorithm, tolerance or dependency changes. Tests prove,
per curated tool, that inputs and outputs equal the source macro and that the
construction equals it after removing exactly the `T-STYLE` attribute changes;
validity statements (`EllipseAxis`) and authority statements (transport
tools) are documentation, not new semantics.

## Compatibility and serialization

```text
SERIALIZATION CHANGE          = NONE (document format)
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE for documents; NEW application sidecar
                                <preferences>.bundled-tools-v1.json; NEW generated
                                library manifest
```

Existing user stores (versions 1, 2, 3) load and save exactly as before;
older GeoCeDG builds ignore the sidecar. Documents built with the original
tools or the template keep their macros; a bundled entry never replaces a
document macro.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests, all in the phase selection:

| ID | Obligation |
|---|---|
| `T-E1L-ORIGINAL` | `Templatev7.ggb` hash equals `INV-E1L-1`; the generator reads it only |
| `T-E1L-REPRO` | golden test: in-memory regeneration byte-identical to every tracked `.ggt` and to `library-manifest.json`; regeneration twice in one JVM identical |
| `T-E1L-SELECTION` | exactly the twelve commands, each once; no excluded or other template command anywhere in `models/curated/**` |
| `T-E1L-MANIFEST` | manifest ↔ `curation.yml` ↔ files set equality; SHA-256 and definition digests recomputed; source macro hashes equal the rights record |
| `T-E1L-EQUIV` | inputs/outputs equal to the source; construction equal after `T-STYLE` normalization; only `iconFile` differs in the start tag |
| `T-E1L-STYLE` | every styled output black with thickness 3 and its original line type; no other element changed |
| `T-E1L-ASSETS` | `INV-E1L-5` over every archive entry |
| `T-E1L-ICONS` | twelve SVGs in the allowed subset; canonical LF and PNG hashes equal `assets-manifest.yml` and the manifest; PNG 32 × 32 |
| `T-E1L-CATALOG` | injected directory: listing, read-only, absent → empty, whole-catalog fail-closed on extra, missing, hash-mismatching, digest-mismatching and malformed-manifest cases |
| `T-E1L-PRECEDENCE` | user-package collision shadows the bundled entry; user install allowed; document adoption and mismatch unchanged |
| `T-E1L-STORE` | no file created at startup or by listing/activation; version-1/2/3 store fixtures unchanged; sidecar written only on explicit bundled pin; user store not created by a bundled pin |
| `T-E1L-ACTIVATE` | each of the twelve bundled tools activates through the existing path; one used on inputs gives the expected outputs with the curated style |
| `T-E1L-ROUNDTRIP` | save `.cedg` after using a bundled tool, reopen: macro present with the owned `iconFile`, no icon bytes, outputs preserved |
| `T-E1L-BOUNDARY` | no `.ggt` under any `src/main/resources`; no build script references `models/curated`; no action-catalog change |
| `T-SMOKE` | author smoke (*Required artifacts*) |

Harness rules: isolated settings and temporary stores, never the author's
`%APPDATA%`; app scenarios follow the Desktop heap rule (child JVMs if a
complete Desktop run shows retained-heap pressure); `LoggerD` per test; existing
modal-dialog and undo-store harness rules; ASCII escapes in non-test sources.

Adjacent regressions before freezing (development evidence):
`G9U1UserToolLibraryTest`, `G9U1MacroNativeArchivePersistenceTest`,
`GeoCeDGProfileTest`, `G9U1ActionRegistryTest`, `PreG9BP1PublicSurfaceTest`;
Checkstyle main and test (XML reports); `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522`;
`tools/agent/verify-legacy.ps1` (standalone, diagnostic only);
`git diff --check`.

Registration and catalog: register the phase selection `PRE-G9B-R6-PLUS-E1-L`
with `compile.shared.semantic`, `compile.desktop.semantic`,
`workspace-profile.product` and `junit.desktop.pre-g9b-r6-plus-e1-l.semantic`;
discovery dry-run and executed selection evidence (the new selection and
`final.desktop`) through `tools/agent/checks/gradle-test-evidence-producer.ps1`,
then `tools/agent/update-verification-junit-inventory.ps1` in-session with
absolute paths, the canonical pin reproduced before repinning, no hash typed
by hand; a development `INFRA_UNIT` and `STATIC` on the staged tree when
registry-shape pins change.

Acceptance (`BOUNDED_PHASE`), on one clean immutable committed candidate:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-L -PlanOnly
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-L -LogDirectory <log root>
```

One `ACCEPTED / COMPLETE` run on the exact commit and tree; no further commit
afterwards. `INTEGRATION` and `FINAL` are not run; a request for either is a
`VERIFICATION_ESCALATION_REQUEST`. A failure proven to predate the candidate
and lie outside its delta is retained baseline debt. Report exact commands,
exit codes, run ids, plan and result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's instruction of 2026-10-07 authorizes: the documentary
authorization commit; the `AGENTS.md` §3.2 governance commit; local `E1-L`
implementation on the implementation branch from `BRANCH_START`; local commits;
focused tests; the phase registration and its official catalog updates;
development `INFRA_UNIT` and `STATIC` runs; the registered `PHASE` on the
frozen candidate; one frozen technical candidate for author review and author
smoke. It does not authorize self-approval, author smoke by the agent,
`INTEGRATION` or `FINAL`, a class change, `E1-P` or any packaging admission, a
legal conclusion, or any later subphase. A proven inability of
`BOUNDED_PHASE` to cover the change is reported as a reclassification
proposal, never applied silently.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication are forbidden; each needs a separate
explicit author instruction naming the exact candidate SHA.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-E1-L   = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION = ACCEPTED / COMPLETE
AUTHOR_SMOKE           = PENDING
PRE-G9B-R6-plus-E1-P   = PREPARED — NOT AUTHORIZED
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report records `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`;
a later closeout record is the sole authority for approval, for the
specification and ADR 0033, and for publication.

## Required artifacts

- The governance commit; the curated library, icons, manifests, tooling,
  specification, ADR, feature entry; the Desktop delta and tests; the phase
  registration.
- A candidate report under `docs/validation/` with entry-gate evidence, the
  generated manifest summary and hash tables, the recorded toolchain, the
  four-way record per tool, test and verification evidence, impact statements
  and an author-smoke checklist: because no packaged library exists before
  `E1-P`, the smoke uses a test-provided installation-like directory (the
  candidate report gives the exact steps): open the manager and menu, see the
  "GeoCeDG library" section with owned icons, activate `CirclebyD` and
  `SquarebyDiagonal`, check black thickness-3 linework, pin a bundled tool,
  save and reopen, confirm the user store is untouched.
- Machine-readable candidate evidence; roadmap and mini-track status lines;
  `GUIDE_IMPACT`, bootstrap impact and verification-infrastructure impact
  declarations; `git diff --check`; final repository status.

## Stop conditions

Stop and report rather than guess when:

- `E1-L` needs a shared-kernel change, a new document serialization, a new
  `tools/agent/checks/**` script, a build-system semantic change, an
  action-catalog change or a packaging change (`VERIFICATION_ESCALATION_REQUEST`);
- another production or architecture owner is required;
- the generator is environment-dependent beyond the recorded toolchain
  contract, or the twelve-tool selection cannot be reproduced exactly;
- a selected definition would need an edit outside `T-EXTRACT`, `T-ICON`,
  `T-STYLE`, or still embeds a non-authorized third-party asset;
- bundled/user precedence would need a destructive migration, or an existing
  user library becomes incompatible;
- any change under `models/legacy/**` would be needed;
- the frozen class must be widened.

Do not absorb `E1-P` because `E1-L` is complete.

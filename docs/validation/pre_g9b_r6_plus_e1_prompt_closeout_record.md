# PRE-G9B-R6-plus-E1 preparation closeout, author decisions and E1-L authorization record

```text
PRE-G9B-R6-plus-E1 PREPARATION = AUTHOR REVIEWED — DECISIONS RECORDED
PRE-G9B-R6-plus-E1             = SPLIT INTO E1-L AND E1-P (author decision)
PRE-G9B-R6-plus-E1-L           = AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-E1-P           = PREPARED — NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = false   (no E1 technical candidate exists yet)
passClaimed               = false
implementationAuthorized  = true    (E1-L implementation and technical verification only)
                            false   (E1-P, E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B)
publicationAuthorized     = false   (no push, merge, tag, release or binary publication)
```

This record preserves, versioned, the author's instruction of 2026-10-07 given
in the session after review of the frozen E1 preparation candidate. It is the
authority for the `DQ-E1` dispositions; where the frozen preparation prompt or
report differs, this record prevails. It authorizes nothing beyond what is
listed under *Authorization*.

## 1. Identities

```text
T_R6PLUS_E1_PREP = 0ce52a965bcf8b9de37a1572738c0ad79db52354
tree             = e500e80fddf957078249383ae61e28e6f707d455
base             = 23b971f5a967958f93e745ca8c39fce1162706e6 (P_R6PLUS_C_X1, published main)
prep STATIC      = verification-91ade0eb295449a6aa95d69289aad6ba, ACCEPTED / COMPLETE,
                   bound to T_R6PLUS_E1_PREP
```

The preparation candidate is kept unchanged as provenance. The combined
[preparation prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-ggt-library-and-packaging.prompt.md)
stays historical preparation evidence; it is not rewritten into an unsplit
executable contract. The executable contracts are the two split prompts below.

## 2. Author dispositions

| ID | Author decision |
|---|---|
| `DQ-E1-1` | Split accepted. `PRE-G9B-R6-plus-E1-L` (registry `PRE-G9B-R6-PLUS-E1-L`) and `PRE-G9B-R6-plus-E1-P` (registry `PRE-G9B-R6-PLUS-E1-P`) are separate acceptance units. |
| `DQ-E1-2` | `E1-L` frozen `BOUNDED_PHASE`, acceptance one registered `PHASE`. Every reproducibility and runtime check stays JUnit-based. A need for a new `tools/agent/checks/**` verifier, a build-system semantic change, a shared-kernel change, a document-format change or an action-catalog change stops the phase with `VERIFICATION_ESCALATION_REQUEST`; the class is never changed silently. |
| `DQ-E1-3` | `E1-P` prepared, not executable: `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, impact `PHASE_LOCAL`; expected evidence registered `PHASE`, `INFRA_UNIT`, `PACKAGING -CheckToolchain`, `PACKAGING -VerifyPackagingArtifacts`, packaging smoke; no `FINAL` planned while `E1-P` stays within the characterized boundary. `E1-P` implementation is not authorized. |
| `DQ-E1-4` | Structure accepted: immutable provenance source `models/legacy/template-v7/`; curated library `models/curated/ggt-library/`; owned icon sources `geocedg/resources/icons/ggt-library/`; tooling under `tools/`. `tools/ggtfiles/` and any alternative that makes `tools/` authoritative product-content storage are rejected. The minimal `AGENTS.md` §3.2 amendment adding `models/curated/` is explicitly authorized (§4). |
| `DQ-E1-5` | `Templatev7.ggb` stays the immutable provenance source. The sixteen 2024 archives are not ingested; their hashes, writer versions, dates and proven equivalence are provenance evidence only. `verify-legacy.ps1` pins are not changed for this purpose. |
| `DQ-E1-6` | Curate and ship exactly twelve tools: `SquarebyDiagonal`, `CirclebyD`, `circArcbyAngle`, `ellipseLength12`, `IFPositiveSelectPoint`, `EllipseAxis`, `conj2mainAxesEllipse`, `pointJump`, `relCoor`, `translationCoor`, `DuctSymbol`, `SymmSymbol`. `EllipseAxis` keeps its validity-domain statement; `pointJump`, `relCoor` and `translationCoor` state explicitly that they carry no spatial-frame or projection authority. `directDimension` and `axisDimension` are neither curated nor shipped; they stay historical/behavioral references because `E2` owns the native capability. |
| `DQ-E1-7` | `sheetISOAnLand` and `sheetISOAnVert` are neither curated nor shipped, and no curated-but-unshipped copies are created. They stay provenance/behavioral references for `E3` only, because they assume 1 model unit = 1 cm, predate the `D1` unit semantics, use theoretical A-series sizes rather than the exact ISO 216 nominal sizes, and `E3` owns `IsoABorder`. |
| `DQ-E1-8` | Consequently `AQ-G3` and `AQ-G3a` require no `E1` implementation. The historical author requirement (outer dotted rectangle and corner points hidden) remains reference evidence for `E3`, not an `E1` product requirement. |
| `DQ-E1-9` | For the twelve curated tools: color black; GeoGebra `lineThickness` 3; role-specific line types preserved where meaningful; point styles and sizes unchanged; text styles unchanged. No remembered-style frontend hook. The former wording "3 pt" is normatively resolved as GeoGebra `lineThickness = 3`, not typographic points. |
| `DQ-E1-10` | Read-only bundled catalog. Invariants: installation-relative; read-only; no first-run seed; no silent mutation of the user library or store; a user-installed package has precedence over a bundled entry; user pin/favorite state stays user-side; whole-catalog integrity fail-closed. The installed application directory is never a mutable user store. |
| `DQ-E1-11` | GeoCeDG-owned SVG sources under `geocedg/resources/icons/ggt-library/`, authored independently from GeoGebra stock icon pixels, designed from each tool's construction; deterministic 32 × 32 PNG rendering for the `.ggt`; fixed/pinned rendering environment as required; a reproducibility test; exact hashes in the asset and library manifests; no dependence on host DPI, UI theme or machine state. The Java-renderer approach is accepted provided deterministic bytes are demonstrated on the supported recorded JDK. A standalone generated PNG need not become an authoritative source when it is deterministically regenerated. Icons: `geocedg-documentation-or-ordinary-art`, CC-BY-4.0. |
| `DQ-E1-12` | Planned `E1-P` contract (not authorized): approved `.ggt` files only below `app/ggt-library/`; each admitted file a shipped member of `library-manifest.json`, with the exact expected hash and an approved rights/provenance disposition; every other `.ggt` forbidden; every `.ggb` forbidden; no GGT content hidden inside JARs; no broadening to arbitrary JAR-entry inspection; the explicit `app/ggt-library/` payload is the `E1-P` verification target. Profiles: `INTERNAL` includes the library; `NC` includes it, redistributable under the approved NC profile; `COMMERCIAL` excludes it and stays not authorized. `INTERNAL` and `NC` carry the same functional library; no profile-dependent geometry or tool behavior. |
| `DQ-E1-13` | Rights/provenance decision: author and sole rights holder of the twelve selected macro definitions Manuel Prado-Velasco; modification into GeoCeDG curated derivatives, inclusion in GeoCeDG, `PROFILE INTERNAL` inclusion and `PROFILE NC` redistribution authorized; `PROFILE COMMERCIAL` not authorized / pending. No grant over GeoGebra stock icons, embedded third-party UI artwork, GeoGebra software or unrelated `Templatev7` contents. Curated definitions `geocedg-software` (EUPL-1.2); new owned icons `geocedg-documentation-or-ordinary-art` (CC-BY-4.0); no new licensing class. Recorded as [rights record version 1](../licensing/curated-ggt-library-rights-record.md) with its JSON mirror; not legal advice. |
| `DQ-E1-14` | Feature `cedg.library.curated-ggt`, maturity `experimental`, enabled by default `true`; availability by default does not imply stable maturity. `geocedg/specs/legacy/curated-ggt-library.md` and ADR 0033 are `E1-L` deliverables, approved through the `E1-L` author closeout. The feature is not marked `stable`; promotion belongs to `G` or a later explicit author decision. |
| `DQ-E1-15` | The author's macro names and help texts are kept verbatim; no rename in `E1-L`; any future rename needs a new author decision. The bilingual guide stays owned by `G`. |
| `DQ-E1-16` | If `E1-P` later cannot satisfy its rights or packaging gate, `G` does **not** automatically stop depending on `E1-P`. Any re-planning that lets `G` depend on `E1-L` only needs a separate explicit author decision; it is not performed automatically. |

## 3. Cross-phase constraint

`OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` is recorded as an
`E2`-owned cross-phase constraint: legacy `directDimension`/`axisDimension`
versus future native `DirectDimension`/`AxisDimension` (case-insensitive
command lookup). It is not solved in `E1`. No native sheet or dimension
functionality is recreated in the GGT library.

## 4. Governance amendment

Authorized: the minimal `AGENTS.md` §3.2 amendment that adds `models/curated/`
as a recognized curated-model/product-artifact category. It must be minimal,
keep legacy ownership, keep the immutable-original rules, not redefine
`models/legacy`, not authorize arbitrary generated models, and keep the
separation of source and provenance. It is committed as its own linear commit
before any `E1-L` product implementation.

## 5. Additional author requirement — GeoCeDG About and post-mini-track version

```text
ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA
OWNER          = PRE-G9B-R6-plus-G
STATUS         = AUTHOR APPROVED REQUIREMENT
IMPLEMENTATION = NOT AUTHORIZED IN E1
```

- GeoCeDG's About surface must eventually present product-owned information
  instead of relying only on the inherited GeoGebra Classic About content:
  product name, canonical GeoCeDG version, GeoCeDG version date and licensing
  information.
- The licensing presentation distinguishes at least: GeoCeDG-authored software
  (EUPL-1.2); GeoCeDG-authored documentation and ordinary artwork (CC BY 4.0);
  inherited GeoGebra and third-party material (its recorded component-specific
  terms); and the profile-specific distribution condition (`INTERNAL`, `NC` and
  `COMMERCIAL` never conflated).
- The About dialog need not reproduce complete legal texts; it provides a
  product-owned route to the versioned GeoCeDG licensing information and never
  treats the upstream GeoGebra licence URL as the complete licensing authority
  for GeoCeDG. Classic's About behavior does not change.
- Version date ≠ build date: the version date is the date of the
  author-approved promotion of the corresponding product version; a technical
  build date may stay visible separately.
- The canonical visible version stays `1.0.0` through the unfinished
  `PRE-G9B-R6-plus` mini-track. The next visible version is fixed as `1.1.0`,
  promoted only when `PRE-G9B-R6-plus-G = PASS — AUTHOR APPROVED`; `G` then
  also establishes the version date. If an internal technical contract requires
  a normalized three-component semantic version, `G` characterizes it
  explicitly and never silently changes the visible-version decision. Promotion
  to `1.1.0` does not authorize a tag, release, binary publication or
  commercial distribution.
- `E1-L`/`E1-P` may create or update the versioned licensing and rights records
  that the future About surface consumes; `E1` never implements the About UI
  and never promotes the product version. This requirement is a mandatory input
  to `PRE-G9B-R6-plus-G`.

## 6. Corrections recorded at authorization

- **SVG renderer.** The frozen report (§K6) says Desktop draws owned SVGs with
  "the in-tree `JSVGIcon` (no external SVG library)". `JSVGIcon` and
  `JSVGImageBuilder` are in-tree, but they parse and paint through the EchoSVG
  library (`io.sf.carte.echosvg`), an existing product dependency. The accepted
  Java-renderer approach therefore relies on an already shipped dependency; no
  new dependency is introduced. The frozen report is kept unchanged as
  historical evidence.
- **Transformations.** With the sheet tools excluded, `T-HIDE` and `T-NOSCRIPT`
  of the preparation design have no target; the `E1-L` transformation set is
  `T-EXTRACT`, `T-ICON` and `T-STYLE`.
- **Profile features.** The application profile's `features` list is validated
  against the approved candidate `geocedg/specs/ui/application-profile-v2.candidate.yml`
  (`tools/agent/workspace-profile-validation.ps1:81-91`) and has no runtime
  reader. `E1-L` registers the feature in `geocedg/features/experimental.yml`
  only and does not change the profile `features` list; only `UserTools.*`
  texts change in the profile.

## 7. Authorization

Authorized by the instruction, in this order:

1. this record and the [rights record](../licensing/curated-ggt-library-rights-record.md)
   with its mirror;
2. the canonical split prompts:
   [`E1-L`](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-l-curated-ggt-library.prompt.md)
   (`AUTHORIZED FOR IMPLEMENTATION` once this documentary authorization commit
   exists) and
   [`E1-P`](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-p-ggt-library-packaging.prompt.md)
   (`PREPARED — NOT AUTHORIZED`);
3. the `AGENTS.md` §3.2 amendment, alone, in the next linear commit;
4. `E1-L` implementation and technical verification from that governance
   commit: curated-library metadata; deterministic generated definitions for
   exactly the twelve tools; owned icon sources and deterministic derivatives;
   the read-only bundled catalog and the Desktop integration that exposes it;
   the feature, specification and ADR; focused tests; the registered
   `PRE-G9B-R6-PLUS-E1-L` `PHASE` on the exact frozen technical candidate.

Not authorized: `E1-P` implementation or any packaging admission (no curated
`.ggt` reaches a distributable app image), `E2`, `E3`, `F1`, `F2`, `F3`, `G`,
`PRE-G9B-R7`, `G9B`, the E2 name collision, publication, merge, tag, release.
`FINAL` is not run merely because another phase used it.

## 8. Author refinement of `DQ-E1-15` (2026-10-07, after the `E1-L` smoke)

After the functional author smoke of `E1-L` R0 (`AUTHOR_SMOKE_FUNCTIONAL = PASS`
on `1aa05d677910b2d88101f11958d3d15b5f21960b`), the author authorized the focal
UX revision `PRE-G9B-R6-plus-E1-L-R1` (hover-tip wording only) and refined
`DQ-E1-15`:

```text
tool names        = UNCHANGED
macro identity    = UNCHANGED
short hover tips  = GeoCeDG-owned bilingual UX text describing actual tool behavior
extended help     = may include validity-domain and CeDG semantic qualifications
geometry / macro semantics = UNCHANGED
```

UX rule: a library-tool hover tip briefly tells the user what the tool
constructs or computes and, where useful, its principal conceptual input;
short, specific, user-facing, bilingual and truthful; never generic. The
spatial caveat of `pointJump`, `relCoor` and `translationCoor` ("This is a 2D
construction helper and does not establish a spatial projection or reference
frame.") and the `EllipseAxis` validity condition belong to extended help. The
author supplied the twelve English and Spanish tips. R0 stays technically
accepted and is superseded by R1; it is not author-approved or published.

Required stop state after implementation and the registered `PHASE`:

```text
PRE-G9B-R6-plus-E1-L   = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION = ACCEPTED / COMPLETE
AUTHOR_SMOKE           = PENDING
PRE-G9B-R6-plus-E1-P   = PREPARED — NOT AUTHORIZED
selfApproved = false
passClaimed  = false
```

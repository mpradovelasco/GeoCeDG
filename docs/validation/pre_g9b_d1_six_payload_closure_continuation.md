# PRE-G9B-D1 six-payload evidence/grant closure continuation

Status: **CHECKPOINT COMPLETE — AUTHOR APPROVED; D1 PHASE REMAINS OPEN**

Evidence date: 2026-09-15

Entry candidate: `d5b0eddc39921efd468b1cf74d66409296f0b994`

Accepted technical candidate: `bd378db3279051410208c084ae51eac09244ced6`
(tree `fe28dd24d1fd7d98fa89257233d3d9aa41d283aa`)

Distribution marker: **INTERNAL EVALUATION — NOT FOR REDISTRIBUTION**

This continuation is factual provenance/compliance preparation, not legal
advice or permission to release. It preserves the immutable earlier audit and
disposition candidates and records only new evidence authorized by D1-HD-05.
No runtime JAR, DLL, font, renderer, dependency declaration or product
semantics changed.

## Governance dispositions

- D1-HD-01 licensing framework: **AUTHOR APPROVED**, materialized in the living
  licensing authorities.
- D1-HD-02 trademark policy: **AUTHOR APPROVED**, materialized independently
  of any fixed logo pixels.
- D1-HD-03 trademark registration: **DEFERRED / NOT AUTHORIZED**.
- D1-HD-04 GeoGebra commercial contact: **DEFERRED / NOT AUTHORIZED**.
- D1-HD-05 six-payload research: **CHECKPOINT COMPLETE — AUTHOR APPROVED** for
  publication as the current D1 evidence state.

`selfApproved=false`; `checkpointAuthorApproved=true`;
`authorApproved=false` for D1 as a phase; `passClaimed=false`;
`publicRedistributionAllowed=false`.

## Published-checkpoint evidence

The author accepted the complete descendant history from public main
`c2e15b3d09a921442108ecb6b5891b4d6211a3ae` through technical candidate
`bd378db3279051410208c084ae51eac09244ced6`. Earlier states, including audit
checkpoint `3267436dcc7dcf0d7c7bc30b63cb2159f2992f85` and intermediate
disposition checkpoint `ea0c1221d2c298f49538a8b2340b5e170eff5a09`, remain immutable historical
evidence; their open-item counts do not control the living status.

Accepted evidence for `bd378db3279051410208c084ae51eac09244ced6` / tree
`fe28dd24d1fd7d98fa89257233d3d9aa41d283aa`:

| Profile | Run | Result | Result hash |
|---|---|---|---|
| PACKAGING | `verification-368265e284424ff788917717f271c28d` | `ACCEPTED / COMPLETE`, zero diagnostics | `4411ce21921193183ec70b39acd991fd9a43a6081d0c80c7f4b975d1167308e2` |
| STATIC | `verification-ef12293ff46d48cba5d7ea3c280fecd5` | `ACCEPTED / COMPLETE`; one non-blocking recovery-governance finding, documentation clear | `e7b08d57aeb99b06ef3c1d8415f2caa40bee293b0b2ea38542421910275dfc30` |
| INFRA_UNIT | `verification-790c31225685414089a96631df7bf351` | `ACCEPTED / COMPLETE`, zero diagnostics | `d2a303ea6ca168ed9e7745d20b2c066f6082d35136f453ca3f1272a45c5c415e` |

The `.cedg` incident is closed as a verifier defect, not an installer-generation
defect. The MSI already materialized the GeoCeDG-owned extension, MIME, ProgId,
open verb and `GeoCeDG.exe` target. The former checker produced a false negative
because it did not inspect the WiX 5 model correctly. The corrected verifier
decompiles the generated MSI and validates those registry rows deterministically;
the accepted PACKAGING run above observed one `.cedg` association and no `.ggb`
claim.

## Durable D1 document map

- [Factual research/audit](pre_g9b_d1_licensing_assets_deployability_research_audit.md)
- [Immutable machine-readable byte audit](../../geocedg/validation/pre-g9b-d1/component-audit.json)
- [Resolved dependency/font disposition matrix](../../geocedg/validation/pre-g9b-d1/component-disposition.json)
- [Human-decision docket](pre_g9b_d1_human_decision_docket.md)
- [Original remediation plan](pre_g9b_d1_remediation_plan_candidate.md) and
  [payload follow-up plan](pre_g9b_d1_next_payload_remediation_plan_candidate.md)
- [Licensing disposition and bounded-remediation report](pre_g9b_d1_licensing_disposition_and_bounded_remediation_candidate.md)
- [Licensing framework/payload candidate report](pre_g9b_d1_licensing_framework_payload_closure_candidate.md)
- [Living component matrix](../licensing/component-matrix.md),
  [asset manifest](../../geocedg/resources/assets-manifest.yml),
  [source-access manifest](../../geocedg/resources/source-access-manifest.json)
  and [legal-text manifest](../../LICENSES/manifest.json)
- Current packaging authority: [package contract](../../packaging/windows/package.yml),
  [builder](../../tools/release/build-windows-package.ps1) and
  [deterministic product checker](../../tools/agent/checks/packaging-product.ps1)

## Exact closure results

| Payload | Byte/source evidence | Licensing interpretation | Disposition |
|---|---|---|---|
| Giac `70501` | staged/Maven JAR SHA-256 `7dff713215cc645e8a843c11cc56bf9eadea1c15bf0cc59b99eec29ee154d33d`; embedded `javagiac64.dll` SHA-256 `3209b44e153ff26593024249ee3b6687d54b5e371e974076245ebdd2bb43b41c`; official commit `81d221d00bff4fff0f5f6d2298aa83042bf11559`, tree `a102b2083e58667d4d704ba95613431735793a40`, git-svn revision `70501`; PE time and Jenkins/Gradle publication chain align. | Giac source headers grant GPL-3.0-or-later. The pinned native inputs contain GMP 6.3.0 and MPFR 4.2.1; the documented combination selects GMP's GPL-2.0-or-later option and applies LGPL-3.0 section 2 to MPFR. LLVM and MinGW runtime notices are retained. | **RESOLVED — RETAIN WITH GPL/SOURCE ACCESS** for NC and commercial profiles, independently of GeoGebra Materials. Professional review should confirm delivery mechanics. |
| math-cross-platform `3.6.3` | binary SHA-256 `f89cd36004b85e11d0c09118630106f87ddf0b9e3bfaef753b8c464a2fa8e933`; official source JAR SHA-256 `dc2871a35bb31fcada9ca45a671618120db3c6486b5daef145ffe5a50f9c2bf5`; 662/662 normalized Java files match official commit `c4c145ac3d1076507e28efd28bb37363dfa78591`, tree `88551d4729ae9b69da11af0dee2d6eb645a4c3b8`. | 659 files Apache-2.0; `DoubleConsts.java` and `GWTMath.java` compile into the JAR under Oracle GPL-2.0-only with Classpath Exception; compiled/used `Cloner.java` was introduced as GeoGebra-authored source and is treated under the official EUPL-1.2 software-source/library grant. | **RESOLVED — RETAIN WITH COMPOSITE NOTICE**. The `Cloner.java` scope interpretation remains suitable for professional confirmation, not an artifact-identity gap. |
| OpenGeoProver `20120725` | binary SHA-256 `3a75cb7ebf9abc6eff84cb85ae84d1d0fa9300da7d83ec6ae6983ddfd822faee`; official moved-repository commit `8d1bb73c499dbfed325bec42ee150cc77557b1a2`, tree `4ccf75ab1a91e4ef6a0a06da0147fa403673029d`; 237 production sources plus seven JavaCC-generated parser classes account for all 244 top-level binary classes; ten tests are absent. Archived Google Code primary metadata says `license: gpl3`. | The archived project-level GPLv3 grant and source-level “Not for commercial use” banner agree for the intended NC profile but conflict for commercial use. The conflict is not resolved in the permissive direction. | **RESOLVED FOR PROFILE NC — RETAIN WITH GPL/SOURCE ACCESS; HUMAN PERMISSION REQUIRED FOR COMMERCIAL**. The component is reachable through the prover engine and was not excluded. |
| `jlm_cmmib10.ttf` | SHA-256 `5f160003632ae19f17d1fede8b86b67b258904e5d0a6907002dd2ef2d54f5a8a`; official JLaTeXMath lineage `af77a8e80d41ff67dfe2f42f14b41f6860dfeeec`; identical cmap, 133/134 common outlines exact, documented `logicalnot` repair; AMS internal copyright/name evidence. | Derivative of AMSFonts 3.04 under OFL-1.1. `cmmib10` is a Reserved Font Name; the distributed primary name `jlm_cmmib10` is distinct. | **RESOLVED — RETAIN WITH OFL TEXT**. |
| `jlm_cmssi10.ttf` | SHA-256 `1b7514b42f320f1b0821d78c9973d0bb115ff714830d5c88d368c962f6bf3e57`; same official lineage; identical cmap, 134/135 common outlines exact, documented `logicalnot` repair; AMS internal copyright/name evidence. | Derivative of AMSFonts 3.04 under OFL-1.1. `cmssi10` is a Reserved Font Name; the distributed primary name `jlm_cmssi10` is distinct. | **RESOLVED — RETAIN WITH OFL TEXT**. |
| `jlm_cmti10.ttf` | SHA-256 `59a8696017d3624ddd0e05397cf71c9855672b4466620ac550fcb9fe803e80cf`; same official lineage; all 135 upstream outlines match, target adds `logicalnot`, one advance differs by one unit; Basil K. Malyshev internal copyright/name evidence. | Documented derivative of BaKoMa-CM under its permission to copy, modify and distribute for any purpose, with notice/URL preservation. | **RESOLVED — RETAIN WITH BAKOMA NOTICE**. |

No byte-identical font match was substituted for provenance: internal naming,
cmap/outlines, historical blobs and the documented transformation chain were
used. No font metrics or renderer behavior were changed.

## Primary authorities

- Giac exact source/publication chain:
  `https://github.com/GeoGebra/giac/tree/81d221d00bff4fff0f5f6d2298aa83042bf11559`;
  official GMP copying authority `https://gmplib.org/manual/Copying`; official
  MPFR 4.2.1 source/copying authority `https://www.mpfr.org/mpfr-4.2.1/`.
- math-cross-platform exact source:
  `https://github.com/geogebra/apache-math-cross-platform/tree/c4c145ac3d1076507e28efd28bb37363dfa78591`;
  file headers and official GeoGebra legal component distinction.
- OpenGeoProver exact source:
  `https://github.com/ivan-z-petrovic/open-geo-prover/tree/8d1bb73c499dbfed325bec42ee150cc77557b1a2`;
  archived project metadata
  `https://storage.googleapis.com/google-code-archive/v2/code.google.com/open-geo-prover/project.json`.
- JLaTeXMath asset history:
  `https://github.com/opencollab/jlatexmath/tree/af77a8e80d41ff67dfe2f42f14b41f6860dfeeec`;
  AMSFonts 3.04 `https://ctan.org/pkg/amsfonts`; BaKoMa archive
  `https://ctan.org/tex-archive/fonts/cm/ps-type1/bakoma`.

Exact copied texts and their SHA-256 values are controlled by
`LICENSES/manifest.json`. Exact machine-readable source identities,
transformations and file-level classifications are controlled by
`component-disposition.json` and `source-access-manifest.json`.

## Prepared requests — not sent

### OpenGeoProver commercial clarification/permission

Proposed recipient/channel: maintainers/copyright holders named by the official
moved repository, through a channel they publish or an issue used only after
author authorization.

> GeoCeDG requests written clarification for OpenGeoProver commit
> `8d1bb73c499dbfed325bec42ee150cc77557b1a2` and the exact published artifact
> `com.ogprover:OpenGeoProver:20120725` (SHA-256
> `3a75cb7ebf9abc6eff84cb85ae84d1d0fa9300da7d83ec6ae6983ddfd822faee`).
> Archived Google Code metadata records GPLv3, while source banners state “Not
> for commercial use”. Please confirm the right to reproduce and redistribute
> the unmodified software and corresponding source in a publicly downloadable
> non-commercial GeoCeDG Windows binary, with the attribution you require, and
> clarify whether commercial redistribution may be granted separately. If you
> prefer a specific explicit license or notice, please identify it.

### Optional Giac build attestation

Proposed recipient/channel: maintainers of the official `GeoGebra/giac`
repository, only after author authorization.

> Please confirm whether official artifact
> `fr.ujf-grenoble:javagiac:70501:natives-windows-amd64` (JAR SHA-256
> `7dff713215cc645e8a843c11cc56bf9eadea1c15bf0cc59b99eec29ee154d33d`,
> `javagiac64.dll` SHA-256
> `3209b44e153ff26593024249ee3b6687d54b5e371e974076245ebdd2bb43b41c`)
> was produced from commit `81d221d00bff4fff0f5f6d2298aa83042bf11559`
> using the recorded CLANG64 Jenkins/recompile/deploy path and the pinned GMP
> 6.3.0 and MPFR 4.2.1 archives. Please also identify the exact MSYS2 package
> snapshot if retained.

This attestation would strengthen the compiler-runtime record; current exact
artifact/source linkage is already sufficient for the technical NC docket.

## Package/SBOM outcome

The SBOM generator now prefers the resolved disposition license over the
immutable audit's preliminary term, records upstream/source-access properties,
marks Giac's nested native relationship, records upstream font versions when
available, and uses the adopted artwork policy. The package manifest records
zero unresolved PROFILE NC payloads while retaining the public-release block.
The exact prior count of 52 unknown versions had already been reduced to zero
in the preceding D1 candidate; this continuation does not rewrite that
historical result.

## Readiness and residual human/legal boundary

**PROFILE NC = TECHNICALLY/LICENSING-DOCKET READY — FINAL AUTHOR/LEGAL REVIEW
REQUIRED.** The evidence checkpoint is author-approved for repository
publication; the product is not approved for public distribution. Professional review should
confirm the Giac/LGPL/GPL corresponding-source delivery form, the EUPL scope
used for `Cloner.java`, and the OpenGeoProver GPL/non-commercial-banner
interpretation. The author must separately authorize final release.

COMMERCIAL-A is unchanged: a GeoGebra License and Collaboration Agreement is
still required for retained GeoGebra Materials/Language Files; OpenGeoProver
also needs independent clarification/permission. COMMERCIAL-B is unchanged as
a long-term resource-replacement strategy; these six payloads do not require a
new mathematical or renderer replacement project.

Living status after checkpoint publication:

~~~text
PRE-G9B-D1 LICENSING FRAMEWORK / PAYLOAD CLOSURE CHECKPOINT
= COMPLETE — AUTHOR APPROVED

PRE-G9B-D1 PHASE = OPEN — PASS NOT CLAIMED

selfApproved=false
publicRedistributionAllowed=false
~~~

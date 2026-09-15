# PRE-G9B-D1 — licensing framework / payload closure candidate

~~~text
PRE-G9B-D1 LICENSING FRAMEWORK / PAYLOAD CLOSURE
= CANDIDATE — PENDING AUTHOR REVIEW

selfApproved=false
authorApproved=false
passClaimed=false
publicRedistributionAllowed=false
~~~

This report continues without rewriting the immutable audit checkpoint
`3267436dcc7dcf0d7c7bc30b63cb2159f2992f85` (tree
`05fe03cbb5069c7a6c4b0b50b550ddc3d55e1905`) and the licensing/disposition
checkpoint `ea0c1221d2c298f49538a8b2340b5e170eff5a09`. It separates factual
evidence, technical conclusions, author/business policy, licensing
interpretation and matters for professional review. It is not legal advice.

## 1. Authorized outcome and method

The author selected EUPL-1.2 for own code/scripts, CC BY 4.0 for own
documentation and ordinary artwork, PROFILE NC as the short-term product,
COMMERCIAL-A as the preferred commercial route, and COMMERCIAL-B as a
long-term strategic option. This continuation materializes those choices as a
candidate but does not authorize public or commercial distribution.

Work used exact staged bytes, Gradle component metadata, JAR/POM/module
metadata, source JARs, official repositories/tags, embedded notices and current
official GeoGebra/OEPM/WIPO/EUIPO materials. Project-wide terms were not
projected onto opaque binaries or fonts. The package builder now exports the
resolved Gradle graph and joins it to actual staged JARs by SHA-256.

## 2. Four JAR dispositions

| Artifact | Exact evidence | Result | PROFILE NC / commercial | Smallest next action |
|---|---|---|---|---|
| Giac `70501` Windows native | JAR `7dff713215cc645e8a843c11cc56bf9eadea1c15bf0cc59b99eec29ee154d33d`; DLL `3209b44e153ff26593024249ee3b6687d54b5e371e974076245ebdd2bb43b41c`; PE chronology points to `GeoGebra/giac@81d221d00bff4fff0f5f6d2298aa83042bf11559`; GMP/MPFR appear statically incorporated | **UNRESOLVED — PRIMARY EVIDENCE INSUFFICIENT**: no authoritative artifact-to-source/build attestation | blocked / blocked | obtain attestation; otherwise authorize pinned rebuild/upgrade plus CAS regression |
| math-cross-platform `3.6.3` | JAR `f89cd36004b85e11d0c09118630106f87ddf0b9e3bfaef753b8c464a2fa8e933`; exact module-linked source JAR `dc2871a35bb31fcada9ca45a671618120db3c6486b5daef145ffe5a50f9c2bf5`; 659/662 sources Apache-2.0, two Oracle GPL-2.0+Classpath notices, one file without grant | **UNRESOLVED — PRIMARY EVIDENCE INSUFFICIENT** for the complete artifact | blocked / blocked | obtain complete fork legal record; otherwise authorize documented rebuild/replacement plus numerical regression |
| OpenGeoProver `20120725` | JAR `3a75cb7ebf9abc6eff84cb85ae84d1d0fa9300da7d83ec6ae6983ddfd822faee`; source candidate `8d1bb73c499dbfed325bec42ee150cc77557b1a2`, matching 249 source/class count and build chronology; source says non-commercial use but gives no redistribution grant | **HUMAN PERMISSION REQUIRED** | blocked / blocked | obtain explicit grant; exclusion/replacement requires prover/fallback decision and regression |
| `netscape.javascript:jsobject:1` | JAR `e958a7df0ac7f684f3f10d46d34287662176afebc69263fc34094d2089dde53b`; only JSObject/JSException/unused JSUtil; no source use; JDK 25 supplies matching public API in `jdk.jsobject` | **RESOLVED — EXCLUDED** | compatible because absent / compatible because absent | keep zero-dependency/staging assertion |

`jsobject` removal is the only runtime-composition change. Desktop compilation,
`installDist`, the exact runtime exporter and JDK 25 recursive `jdeps` passed;
no missing `netscape`/`jdk.jsobject` reference was reported. No mathematical,
kernel, file-semantic or numerical behavior changed.

## 3. Four font dispositions

| Font | SHA-256 | Exact source result | Disposition |
|---|---|---|---|
| `jlm_cmmib10.ttf` | `5f160003632ae19f17d1fede8b86b67b258904e5d0a6907002dd2ef2d54f5a8a` | not present byte-for-byte in official JLaTeXMath/ReTeX histories or releases; GeoGebra Copybara import is not prior provenance | blocked: exact grant or authorized metrics-tested substitution |
| `jlm_cmssi10.ttf` | `1b7514b42f320f1b0821d78c9973d0bb115ff714830d5c88d368c962f6bf3e57` | same; glyph/TTX differences prevent treating it as metadata-only | blocked: exact grant or authorized metrics-tested substitution |
| `jlm_cmti10.ttf` | `59a8696017d3624ddd0e05397cf71c9855672b4466620ac550fcb9fe803e80cf` | same; glyph/TTX differences prevent treating it as metadata-only | blocked: exact grant or authorized metrics-tested substitution |
| `jlm_special.ttf` | `de5424f7e4b8eac1b8b58b4518a6622cf479d01f98f3b27b3012ba8c1514d97c` | exact official JLaTeXMath blob `1454e6f` at commit `0571d848a54f54386bab4eeaeb3e02f6a88f388e` | **RESOLVED — NOTICE/TEXT REQUIRED**, GPL-2.0-or-later + linking exception/source access |

Final renderer inventory is 46 fonts: 43 resolved with their individual
Knuth/OFL/GPL+exception/component-specific/public-domain evidence and three
blocked. No font was changed.

## 4. SBOM and legal closure

Before this continuation, the SBOM contained 52 JAR components with
`version: unknown`. The candidate stages 51 JARs and records:

- 56 authoritative Gradle artifacts: 45 external modules and 11 project
  dependencies;
- 51 staged JARs: 39 external plus 12 project/main outputs;
- exact group/artifact/version/purl/variant/path/hash for external modules;
- exact source revision for project outputs, not a fabricated version;
- 46 font file components and their package-entry hashes/dispositions;
- versioned GeoCeDG branding assets and trademark roles;
- exact Temurin 25.0.4+7 runtime/source/legal identity;
- WiX 5.0.2 embedded MSI payload only when installers are built; and
- zero unknown JAR versions and zero `jsobject` components.

The builder fails if a staged dependency does not join uniquely by hash, if an
external byte differs from the immutable audit, if legal paths/hashes drift,
or if the font/Gradle/staging closure changes without disposition.

The PROFILE NC legal bundle contains EUPL-1.2, CC BY 4.0, CC BY-NC-SA 4.0,
the current official GeoGebra terms, GPL-2/3, LGPL-2.1/3, MPL-2.0,
Apache-2.0, renderer linking exception, Knuth/OFL/font texts, complete
JOGL/GlueGen/JNA/FlatLaf/EchoSVG/Rhino/component notices, WiX MS-RL,
source-access manifest, immutable audit/current overlay and resolved Gradle
identity. `LICENSES/manifest.json` enumerates and hashes every bundled text.
The jlink runtime separately preserves its complete `runtime/legal/**` tree.

## 5. Proposed GeoCeDG framework

| Class | Candidate policy |
|---|---|
| GeoCeDG software/scripts | EUPL-1.2, excluding files with valid different terms |
| GeoCeDG documentation | CC BY 4.0 with explicit exceptions in `docs/LICENSE.md` |
| ordinary GeoCeDG artwork | CC BY 4.0 by manifest licensing class |
| official name/logo artwork | CC BY 4.0 copyright candidate plus separate trademark/brand-use policy, pending adoption |
| inherited GeoGebra code | EUPL-1.2 with upstream notices and modification register |
| GeoGebra Language Files/UI Materials | applicable CC BY-NC-SA 4.0-or-later and official GeoGebra terms |
| independent third parties | exact component terms/texts/notices/source access |
| complete binary | composite terms; PROFILE NC only while GeoGebra NC Materials remain; never represented solely as EUPL/Open Source/Free Software |

## 6. Brand and registration policy

The candidate policy permits truthful nominative reference, compatibility
statements, official-project links, unmodified official-package redistribution
and educational/scientific screenshots. Permission or rebranding is required
for confusing fork/product names, official-logo use on another product,
modified logos presented as official, endorsement, merchandising and confusing
domains/store identities. EUPL forks remain possible under a distinct name and
replacement branding.

Assets use the durable model `semantic role -> version -> exact hash ->
provenance -> copyright class -> trademark role`. A new logo version updates
the manifest and validation, not the licensing architecture.

The OEPM candidate recommends Spanish word mark `GeoCeDG` first, Nice class 9
for downloadable software; classes 41 and 42 only when actual education/
training or software/SaaS services are offered. A figurative mark should follow
only after artwork stabilizes. EUIPO is a later consideration for real
multi-country EU activity. Clearance search, final wording/owner data and
professional review are required. No filing occurred and `®` is not authorized.

## 7. Distribution-profile conclusions

**PROFILE NC:** technically coherent but **still blocked**. The six exact
payload items must be granted, removed or replaced through authorized work;
then final author/legal approval is required. The current packages remain
internal evaluation only.

**COMMERCIAL-A:** preferred and technically lower-effort. EUPL source rights
and compatible third-party grants remain independent. A GeoGebra License and
Collaboration Agreement is required for commercial retention/use of Language
Files, UI images/styles and other GeoGebra Materials, plus any agreed mark use.
Fees, royalties, territory, duration, exclusivity, sublicensing, support,
updates and reporting remain negotiation points. The prepared dossier/email was
not sent.

**COMMERCIAL-B:** technically separable but a major resources/localization
program. It requires verified removal/replacement of hundreds of Language
Files/UI images/icons/styles/Materials and upstream marks, plus independent
localization, accessibility, visual regression and maintenance. It does not
resolve the six independent blockers and is not authorized for implementation.

## 8. Remaining human/professional decisions

The reduced docket contains only: final author adoption of the licensing
framework; adoption of the brand policy; OEPM filing/owner/service decision;
authorization to contact GeoGebra; and permission/replacement choices for the
six exact blockers if primary grants cannot be obtained. Professional review is
appropriate for public notice/source-offer mechanics, received grants,
trademark clearance/filing and any GeoGebra agreement.

## 9. Technical remediation and verification

Bounded productive change: removal of redundant `jsobject:1` from the Gradle
catalog/Desktop runtime. Infrastructure changes: deterministic resolved
component exporter, hash join, enriched SBOM/legal payload and stronger package
contracts. Documentation/legal records do not alter scientific semantics.

Verification set:

| Check | Expected/recorded outcome | Local evidence path |
|---|---|---|
| Desktop compile + installDist + resolved export | PASS; 56 resolved components; zero jsobject | `source/desktop/desktop/build/reports/geocedg/resolved-runtime-components.json` |
| recursive JDK 25 `jdeps` | exit 0; zero `netscape`, `jdk.jsobject` or missing-dependency diagnostics | `artifacts/pre-g9b-d1/framework-research/` |
| Windows `All` rebuild | PASS; app-image/ZIP/MSI/EXE, 51 staged JARs | `artifacts/packaging/windows/build-manifest.json` |
| focal package product contract | `CONTRACT_SATISFIED`; SBOM/legal/assets/MSI association | `artifacts/pre-g9b-d1/framework-research/packaging-product-all.json` |
| canonical PACKAGING | to be recorded on immutable candidate | `artifacts/pre-g9b-d1/framework-research/verify-packaging-final/verification-result.json` |
| STATIC | to be recorded on immutable candidate | `artifacts/pre-g9b-d1/framework-research/verify-static-final/verification-result.json` |
| INFRA_UNIT | to be recorded on immutable candidate | `artifacts/pre-g9b-d1/framework-research/verify-infra-final/verification-result.json` |
| `git diff --check`, JSON/local-link/legal hash checks | required before candidate commit | command output / final report |

No PHASE, INTEGRATION/COMPOSED or FINAL/FULL scientific profile is warranted:
no kernel, geometry, numerical or file-semantics path changed. Bootstrap impact
is **NO_CHANGE**: JDK 25, Gradle and WiX prerequisites/acquisition are unchanged;
the exporter consumes the existing Gradle wrapper and standard Groovy/JSON/JCA
APIs.

## 10. Next step — not executed

Dispose `D1-HD-01` through `D1-HD-05`. Prefer obtaining exact grants/source
attestations and retaining components. If that fails, authorize a separate
component-specific remediation with the functional/scientific gate required by
its actual impact. Only after rebuilding a zero-blocker closure should the
author consider a D1 deployability review. P1, version 1.0, G9B/G9C/G9U2,
public release, commercial distribution, GeoGebra contact and trademark filing
remain unauthorized.

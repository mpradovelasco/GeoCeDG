# Third-party component register

This register summarizes the component-level terms for the current Windows
payload. Exact byte identities, package paths and evidence are in
`geocedg/validation/pre-g9b-d1/component-audit.json`; the researched overlay is
`component-disposition.json`. Exact texts and their hashes are indexed by
`LICENSES/manifest.json`.

This is factual compliance evidence, not legal advice or release approval.

## Inherited GeoGebra classes

| Class | Terms recorded from the current official source | PROFILE NC | Commercial profile |
|---|---|---|---|
| GeoGebra-authored software source | EUPL-1.2 | compatible with source/notice obligations | independently licensed under EUPL-1.2 |
| Language Files, documentation, UI images and styles | CC BY-NC-SA 4.0-or-later under the official GeoGebra component distinction | retain with attribution/share-alike and product NC conditions | GeoGebra agreement, or remove/replace independently |
| Other GeoGebra Materials retained in the product | official GeoGebra non-commercial terms | permitted only within the compliant NC profile, after all independent blockers close | GeoGebra License and Collaboration Agreement, or remove/replace |
| GeoGebra name and marks | separate trademark/brand rights | nominative attribution only; no endorsement | agreement/clearance if use goes beyond nominative reference |

Primary snapshot: `LICENSES/GeoGebra-License-2025-11.md`, retrieved from the
official GeoGebra legal repository on 2026-09-15.

## Resolved external runtime families

| Component/version | Recorded terms | Required package treatment | Status |
|---|---|---|---|
| ReTeX/JLaTeXMath repository build | GPL-2.0-or-later with recorded linking exception; fonts separately classified | exact component text, GPL text, modification/source access | RESOLVED — NOTICE/TEXT REQUIRED |
| JOGL 2.6.0 | complete upstream composite permissive license set | reproduce version-pinned license/notices | RESOLVED — NOTICE/TEXT REQUIRED |
| GlueGen 2.6.0 | complete upstream composite permissive license set | reproduce version-pinned license/notices | RESOLVED — NOTICE/TEXT REQUIRED |
| JNA 5.18.1 | LGPL-2.1-or-later or Apache-2.0; candidate selects Apache-2.0 | ship exact embedded notice and Apache-2.0 | RESOLVED — RETAIN |
| FlatLaf 3.7, including nested native resources | Apache-2.0 | ship exact license; preserve nested-resource identity in SBOM | RESOLVED — NOTICE/TEXT REQUIRED |
| EchoSVG 2.2.1 family | Apache-2.0 plus NOTICE/W3C/color-profile notices | ship exact license and notices | RESOLVED — NOTICE/TEXT REQUIRED |
| css4j 6.1.1 / xml-dtd 4.3 / tokenproducer 3.3 | component-local LGPL/MPL/notice terms | ship exact component files and applicable generic texts | RESOLVED — NOTICE/TEXT REQUIRED |
| ANTLR 2.7.7 | ANTLR upstream license | ship exact upstream license | RESOLVED — NOTICE/TEXT REQUIRED |
| J2ObjC annotations exact upstream revision | Apache-2.0 | ship exact upstream license | RESOLVED — NOTICE/TEXT REQUIRED |
| JSR305 3.0.2 | exact embedded POM declares Apache-2.0 | ship Apache-2.0; this supersedes the earlier BSD triage label | RESOLVED — RETAIN |
| JCLF linear3 1.0.0 / text 5.0.3 | component-local permissive licenses | ship exact embedded licenses | RESOLVED — NOTICE/TEXT REQUIRED |
| Rhino 1.8.1 / rhino-engine 1.8.1 | exact upstream tag license: MPL-2.0 majority plus listed component exceptions | ship exact Rhino tag license and MPL-2.0 text; source access as recorded | RESOLVED — NOTICE/TEXT REQUIRED |
| remaining hash-matched Maven modules | licenses recorded by exact resolved coordinate in the disposition evidence | retain applicable exact generic/component text | RESOLVED — NOTICE/TEXT REQUIRED |
| Temurin/OpenJDK 25.0.4+7-LTS runtime | GPL-2.0-only with Classpath Exception plus module-specific third-party terms | preserve `runtime/legal/**`; publish pinned source/access identity | RESOLVED — NOTICE/TEXT REQUIRED |
| WiX Toolset 5.0.2 embedded MSI custom-action/UI payload | MS-RL; WiX executable/toolchain itself is build-only | ship MS-RL and exact source-access identity for embedded files | RESOLVED — NOTICE/TEXT REQUIRED |
| Giac `70501` Windows native | GPL-3.0-or-later combination; GMP 6.3.0 GPL-2.0-or-later option, MPFR 4.2.1 LGPL-3.0-or-later converted under LGPL section 2, LLVM exception and MinGW runtime notices | ship GPL/LGPL texts and compiler-runtime notices; provide pinned corresponding-source access | RESOLVED — RETAIN WITH GPL/SOURCE ACCESS |
| math-cross-platform 3.6.3 | Apache-2.0 majority; two Oracle files GPL-2.0-only with Classpath Exception; GeoGebra-authored `Cloner.java` under the official EUPL-1.2 software-library grant | ship Apache-2.0, GPL-2.0, Classpath Exception and EUPL-1.2; preserve source-file notices/source access | RESOLVED — RETAIN WITH COMPOSITE NOTICE |
| OpenGeoProver 20120725 | archived official project metadata records GPLv3; exact binary maps to official source commit; source banners also say “Not for commercial use” | PROFILE NC: ship GPL-3.0 and source access; commercial retention requires clarification/permission | RESOLVED FOR PROFILE NC — COMMERCIAL HUMAN PERMISSION REQUIRED |

The full JAR-by-JAR mapping is intentionally machine-readable rather than
duplicated here. It is joined to actual staged bytes by the package builder.

## Renderer fonts

The Windows runtime distributes 46 exact TTF files. All 46 are connected to
primary license evidence: Knuth terms, SIL OFL 1.1 (including reserved font
names where recorded), JLaTeXMath GPL-2.0-or-later with linking exception,
font-module GPL-2.0 terms, a component-specific free permission, or an upstream
public-domain statement. The package must retain the exact font texts and
corresponding-source records. It must not treat the renderer code license as a
blanket font license.

`jlm_cmmib10.ttf` and `jlm_cmssi10.ttf` are documented JLaTeXMath derivatives
of AMSFonts 3.04 sources under OFL-1.1, with the Reserved Font Names avoided by
their `jlm_` names. `jlm_cmti10.ttf` is a documented JLaTeXMath derivative of
the BaKoMa Computer Modern source, whose exact notice permits copying,
modification and distribution with notice preservation. `jlm_special.ttf` is
resolved by an exact match to official JLaTeXMath history.

## Exact payload closure

Giac `70501`, math-cross-platform `3.6.3`, OpenGeoProver `20120725`, and the
three fonts above form the complete former six-item docket. Their exact hashes,
source correspondence, licensing interpretation and remaining review boundary
are in the machine-readable disposition/source-access records and
`LICENSES/UNRESOLVED.md`. No PROFILE NC technical provenance blocker remains;
this does not authorize public redistribution.

The legacy `netscape.javascript:jsobject:1` JAR is deliberately absent: source
use analysis, Gradle resolution, compilation, install distribution and JDK 25
linkage evidence showed that the platform `jdk.jsobject` module supplies the
API and the staged JAR was redundant.

## Distribution condition

This component record is profile-neutral.The distribution condition of a
particular build is profile-specific and is stated by the distribution notice
packaged with it: INTERNAL_EVALUATION_ONLY.txt for PROFILE INTERNAL, which is
an internal evaluation artifact and must not be redistributed, and
NC_DISTRIBUTION_NOTICE.txt for PROFILE NC, which may be redistributed only
under its approved non-commercial terms. Commercial distribution is not
authorized.

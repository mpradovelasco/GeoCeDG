# GeoCeDG repository map

| Field | Value |
|---|---|
| Scope | Durable source, evidence, operational and generated boundaries |
| Baseline | GeoGebra 5.4.928.0 at `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Current closed capability | G8 `PASS — AUTHOR APPROVED`; Locus V2 metrics/intersections remain experimental/internal |
| Current design phase | G9P-R1, G9P and G9O1 PASS — AUTHOR APPROVED; six normative specifications and ADR 0010–0015 Accepted; G9A1 authorized/not started; no productive spatial G9 implementation started |
| Date | 2026-08-17 |

Use this map to decide where a change belongs. `AGENTS.md`, accepted ADRs and
normative specifications remain authoritative when this overview is
insufficient.

| Path | Ownership / purpose | Durable authority or generated? |
|---|---|---|
| `source/shared/common` | Upstream shared kernel plus localized GeoCeDG Java under `org/geocedg` | Productive source; upstream changes minimized/registered |
| `source/shared/common-jre` | JRE adapters and shared test execution; GeoCeDG kernel/regression tests | Productive/test source |
| `source/shared/*` | Canvas, renderer, editor, keyboard and support modules | Predominantly upstream source |
| `source/desktop/*` | Classic Desktop runtime and GeoCeDG Desktop/profile/laboratory code | Productive source; Desktop frontend only |
| `source/web/*` | Upstream web frontend | Not a validated GeoCeDG platform yet |
| `apps/geocedg` | Product profile manifest and application-level contracts | Durable GeoCeDG source; no geometric truth |
| `geocedg/specs` | Feature/object/export/operation specifications | Status-labelled technical contracts; proposed documents are not normative |
| `geocedg/features` | Stable and experimental maturity/default manifests | Durable feature-state authority |
| `geocedg/resources` | GeoCeDG-owned resource/asset declarations | Durable source/provenance; license boundary |
| `geocedg/validation` | Versioned invariants, tolerance policies and compact evidence | Durable validation authority/evidence; not generated logs |
| `models/legacy` | Immutable original CeDG artifacts plus manifests/provenance | Durable evidence; never silently promoted or packaged |
| `models/regression` | Small deterministic source fixtures and expected semantic results | Versioned regression authority |
| `docs/adr` | Architectural decisions and alternatives | Accepted ADRs supersede earlier roadmap proposals |
| `docs/architecture` | System maps, implementation boundaries and impact analysis | Durable design/implementation documentation |
| `docs/developer` | Internal API, repository and agent operating references | Durable developer documentation |
| `docs/developer/book_repository_workflow.md` | Two-repository boundary and opt-in book-worktree commands | GeoCeDG operational documentation; never editorial authority |
| `docs/user/geocedg_user_guide.md` | Current observable/manual workflow and conceptual entry point | Living user/developer guide |
| `docs/user/geocedg_mathematical_reference.md` | Mathematical definitions and authority links | Living explanatory reference; specifications remain authoritative |
| `docs/validation` | Phase reports and traceability | Historical evidence is immutable; current traceability is maintained separately; neither is normative semantics |
| `docs/references/cedg` | Scientific knowledge corpus | Local reference only; often redistribution-blocked |
| `docs/upstream` | Pinned baseline records and exact archived upstream README | Upstream provenance/evidence |
| `tools/agent` | Focused verifiers plus canonical composed `verify.ps1` | Executable verification authority |
| `tools/agent/evidence-integrity.ps1` | Exact-byte Git blob and frozen G8 evidence helpers | Operational verification source; never rewrites evidence |
| `tools/knowledge` | Deterministic profile-driven source/knowledge bundle generator, independent artifact verifier and disposable fixtures | Operational source; generated bundles are not authority |
| `tools/bootstrap` | Workstation onboarding and focused prerequisite installation | Setup/orchestration, not acceptance authority |
| `tools/book` | Boundary-safe status/alignment, deterministic baseline candidates, G9O1 evidence composition, and explicit delegation for an optional external book worktree | Operational tool; real external-book actions are opt-in and never product authority |
| `tools/locus-v2` | Explicit developer-only Locus V2 laboratory launcher | Operational tool, opt-in; not product UI |
| `tools/legacy` | Reproducible ingest and legacy Laboratory loader | Operational tools with immutable originals |
| `tools/release` | Packaging generation and verification helpers | Reproducible release mechanics, not legal approval |
| `tools/benchmark`, `benchmarks` | Informational harnesses and reproducible suites | Source definitions; raw outputs generated |
| `packaging` | Windows `jpackage` configuration and notices | Durable packaging source; public release remains blocked |
| `.github/prompts`, `ai-shell/prompts` | Canonical tasks/reviews, bounded book operations, and lightweight profiles | Operational instructions, not geometric or editorial truth; prompts never self-authorize phases |
| `.github/workflows` | CI invocation of repository verification | Automation source |
| local `/book` | Optional filesystem link to the independent `mpradovelasco/geocedg_book` worktree | Ignored local convenience; never GeoCeDG authority, content or Git state |
| `artifacts` | Packaging, export, `knowledge` bundles, book technical-baseline candidates, and other generated output boundary | Regenerable/ignored except explicit README/manifests; generated book evidence is not editorial acceptance |
| module `build/`, `.gradle/`, `.kotlin/` | Build caches and generated outputs | Regenerable/ignored; verifiers restore entry state |

## Locus V2 path

```text
geocedg/specs/locus/locus-v2-semantics.md       normative semantics
docs/adr/0006-*                                  architecture decision
source/shared/common/.../org/geocedg/.../locus  productive shared Java
source/shared/common-jre/.../locus               tests and benchmarks
geocedg/validation/locus-v2                      versioned evidence/policies
tools/agent/verify-locus-v2.ps1                  focused gate
tools/locus-v2/open-locus-v2-laboratory.ps1      opt-in visual inspection
docs/architecture/locus_v2_implementation.md     implementation map
docs/developer/locus_v2_api.md                    internal API reference
docs/user/geocedg_user_guide.md                   current operational entry
```

Generated test XML, Gradle reports, raw benchmark logs and app/package binaries
are evidence only and stay outside version control. Scientific PDFs and legacy
`.ggb` originals are durable reference/evidence but are not runtime resources.

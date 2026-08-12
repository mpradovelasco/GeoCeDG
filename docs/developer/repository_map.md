# GeoCeDG repository map

| Field | Value |
|---|---|
| Scope | Durable source, evidence, operational and generated boundaries |
| Baseline | GeoGebra 5.4.928.0 at `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Current closed capability | G6 + G6R; Locus V2 experimental/internal |
| Next phase | G7, pending/not started |
| Date | 2026-08-12 |

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
| `geocedg/specs` | Normative feature/object/export/operation specifications | Technical authority for each accepted capability |
| `geocedg/features` | Stable and experimental maturity/default manifests | Durable feature-state authority |
| `geocedg/resources` | GeoCeDG-owned resource/asset declarations | Durable source/provenance; license boundary |
| `geocedg/validation` | Versioned invariants, tolerance policies and compact evidence | Durable validation authority/evidence; not generated logs |
| `models/legacy` | Immutable original CeDG artifacts plus manifests/provenance | Durable evidence; never silently promoted or packaged |
| `models/regression` | Small deterministic source fixtures and expected semantic results | Versioned regression authority |
| `docs/adr` | Architectural decisions and alternatives | Accepted ADRs supersede earlier roadmap proposals |
| `docs/architecture` | System maps, implementation boundaries and impact analysis | Durable design/implementation documentation |
| `docs/developer` | Internal API and repository operating references | Durable developer documentation |
| `docs/user/geocedg_user_guide.md` | Current observable/manual workflow and conceptual entry point | Living user/developer guide |
| `docs/validation` | Phase reports and traceability | Historical execution evidence, not normative semantics |
| `docs/references/cedg` | Scientific knowledge corpus | Local reference only; often redistribution-blocked |
| `docs/upstream` | Pinned baseline records and exact archived upstream README | Upstream provenance/evidence |
| `tools/agent` | Focused verifiers plus canonical composed `verify.ps1` | Executable verification authority |
| `tools/bootstrap` | Workstation onboarding and focused prerequisite installation | Setup/orchestration, not acceptance authority |
| `tools/locus-v2` | Explicit developer-only Locus V2 laboratory launcher | Operational tool, opt-in; not product UI |
| `tools/legacy` | Reproducible ingest and legacy Laboratory loader | Operational tools with immutable originals |
| `tools/release` | Packaging generation and verification helpers | Reproducible release mechanics, not legal approval |
| `tools/benchmark`, `benchmarks` | Informational harnesses and reproducible suites | Source definitions; raw outputs generated |
| `packaging` | Windows `jpackage` configuration and notices | Durable packaging source; public release remains blocked |
| `.github/prompts`, `ai-shell/prompts` | Canonical tasks/reviews and lightweight profiles | Operational instructions, not geometric truth |
| `.github/workflows` | CI invocation of repository verification | Automation source |
| `artifacts` | Packaging, export and other generated output boundary | Regenerable/ignored except explicit README/manifests |
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

# G6R Locus V2 traceability matrix

| Requirement | Normative spec / ADR | Productive class or API | Test/evidence | User-guide section |
|---|---|---|---|---|
| Provider-owned semantic parameter | Spec §§3–4; ADR 0006 | `LocusDriverDomainProvider2D`, `ExplicitNumericDomainProvider2D`, `StablePathDomainProvider2D` | value/provider/kernel tests; `g6r-hardening-evidence.yml` | “Parameter, branch and domain” |
| Branch identity differs from valid components | Spec §§5–6; ADR 0006 | `LocusBranch2D`, `LocusBranchSnapshot2D` | topology and hardening value tests | “Parameter, branch and domain” |
| Typed lineage | Spec §6 | `LocusLineage2D` | topology fixture; invalid-shape hardening tests | “Topology and lineage” |
| Separate status/quality axes | Spec §§7–8 | `LocusSemanticMetadata2D`, `LocusQuality2D`, `LocusEvaluation2D` | value contracts | “Exactness and numeric guarantee” |
| Semantic revision | Spec §12; ADR 0006 | `AlgoLocusV2`, `GeoLocusV2` | lifecycle/undefined/recovery/nested invalidation tests | “Semantic revision and lifecycle” |
| Nested semantic composition | Spec §11; ADR 0006 | `AlgoNestedLocusV2`, `LocusEvaluationSession2D` | depth 1/2/3/5 functional and hardening benchmarks | “Nested composition” |
| Bounded scoped session | Spec §11; ADR 0006 | `LocusEvaluationSession2D`, `LocusSemanticKey2D`, typed diagnostic | session exception/cycle/eviction/on-off tests | “Sessions and cache” |
| Render/semantic separation | Spec §10; ADR 0006 | `DrawLocusV2`, `LocusRenderCache2D`, `LocusRenderPolicy2D` | uniform/adaptive, zoom, component, seam and unbounded tests | “Render and adaptive tessellation” |
| Legacy/Classic compatibility | Spec §§2, 13; ADR 0006 | distinct `GeoClass.LOCUS_V2`; no `CmdLocus` change | dispatch/source audit; Classic/legacy tests | “Compatibility” |
| No persistence | Spec §13; ADR 0006 | empty `GeoLocusV2.getXML`; copy/set disabled | lifecycle and static verifier | “What is not available” |
| No public `Path` or incidence | Spec §13; ADR 0006 | `GeoLocusV2 extends GeoElement`; false `isGeoLocus*` | kernel and laboratory contract tests | “Can I use Locus V2 now?” |
| No G7 metrics | Spec future boundary; ADR 0006 | no metric classes/dispatch | static verifier and GeoClass audit | “G7/G8/G9 boundaries” |
| No G8 intersections | Spec future boundary; ADR 0006 | no intersection dispatch | static verifier and GeoClass audit | “G7/G8/G9 boundaries” |
| No G5 locus export | Spec §13; ADR 0006 | G5 adapter rejects `GeoLocusV2` | kernel export diagnostic test | “Compatibility and export” |
| No 3D/plane behavior | Spec §§2, 13; ADR 0006 | no 3D/plane dispatch | static verifier and GeoClass audit | “What is not available” |
| Developer-only visual access | G6R task; existing semantic boundary | Desktop `org.geocedg.desktop.locus`, Gradle task and PowerShell script | laboratory contract test and smoke | “Developer laboratory” |
| Measured optimization only | G6 benchmark plan | adaptive render strategy; unchanged session/DAG | hardening benchmark distributions and versioned evidence | “Performance evidence” |

The normative specification remains the semantic authority. This matrix links
that authority to implementation and evidence; it does not create a competing
contract.

# G9P integrated analysis and approved execution plan

| Field | Value |
|---|---|
| Disposition | **G9P-R1 PASS — AUTHOR APPROVED; G9P PASS — AUTHOR APPROVED** |
| Date | 2026-08-16 |
| Repository head inspected | `667e1cfa6f368c693118ed2444159f1aa641d2fe` |
| G8 authority | `geocedg-g8-pass`, peeled `e7810171179825a03b22d8c6eba28c672f468281`, **PASS — AUTHOR APPROVED** |
| Productive G9 state | **NOT STARTED** |
| Normative effect | Six G9P specifications are `NORMATIVE / AUTHOR APPROVED`; ADR 0010–0015 are Accepted |

## 1. Objective and boundary

G9P coordinates five separately gated design workstreams:

- G9P-S: spatial identity, frames, projection bindings, sufficiency,
  reconstruction, persistence, and composed-object boundary;
- G9P-U: public Locus V2 surface and manifest-defined workspaces;
- G9P-X: honest exact/approximate DXF export;
- G9P-D: user, mathematical, developer, agent, and traceability documents; and
- G9P-O: deterministic source/knowledge bundles and evidence operations.

The package contains design, characterization, schemas/configuration, future
prompts, and verification only. It adds no public command, persistence factory,
GUI workspace, DXF approximation, spatial object, or bundle generator.

## 2. Entry evidence

The working branch is `feature/g9-spatial-semantics-design`. At entry, its HEAD
was seven immutable author reference inputs ahead of the G8 peeled snapshot;
the working tree was clean and branch upstream divergence was `0/0`. Local
`main` was the G8 peeled commit. `origin/main` pointed at the G9P reference-input
head rather than local `main`; this is recorded, not silently corrected.

The annotated G8 tag object is
`fed1bfbeea77a48acce285429b397eda77054df1`; it peels to
`e7810171179825a03b22d8c6eba28c672f468281`, which is an ancestor of the G9P
head. G5, G6, G7, and all focused G8 authorities through G8C2 reproduced.

The first composed run in the Windows sandbox failed while Gradle tried to use
the user daemon/profile; this was classified as an environment permission
failure and rerun with managed escalation. The escalated run reached the
baseline gate after all G5–G8 functional gates passed, then exposed three
pre-existing trailing spaces in an immutable author note. G9P preserves those
bytes and records a path-specific Git whitespace exception plus its exact hash;
the final composed gate must pass after that metadata correction.

## 3. Dependency topology and recommended order

G9P-R1 separates three concepts that the initial linear sequence conflated:

1. a **hard semantic or contract dependency** is needed for a phase's result to
   be meaningful or safely integrated;
2. a **recommended execution predecessor** reduces operational conflict but
   does not make the successor's mathematics depend on it; and
3. a **global/release gate** combines independently valid deliverables for an
   author-approved product closeout.

G9A remains the approved three-gate foundation. Its hard dependency graph then
branches into a spatial-kernel track and a public/product track:

```text
                                      +--> G9B --> G9C -----------+
                                      |                            |
G9A1 --> G9A2 --> G9A3 ---------------+                            +--> G9 GLOBAL CLOSEOUT --> G9U2
                                      |                            |
                                      +--> G9U0 -------+           |
                                                       +--> G9U1 --+
G5 + G6-G8 internal semantic authority --> G9X1 ------+

G9O1: recommended first; required by global operational closeout, but no
      semantic edge from G9O1 to G9A1.
```

G9U0 depends on the completed G9A lifecycle substrate because it publishes
persistent user objects. G9X1 does not semantically depend on G9U0: its
read-only adapter can consume already authoritative internal G6-G8 semantic
sources, and it must disclose `persistent` versus `construction-revision`
identity scope. G9U1, as currently scoped, integrates both approved public V2
and DXF actions; those are product-integration prerequisites, never kernel
authorities. G9B depends on G9A3 and not on G9U1.

The recommended low-conflict schedule remains:

```text
G9O1; G9A1; G9A2; G9A3; G9U0; G9X1; G9U1; G9B; G9C;
G9 global closeout; G9U2
```

Semicolons above mean scheduling preference, not semantic arrows. An approved
team may advance the G9B/G9C track independently of U0/X1/U1 after G9A3, while
preserving file ownership and each focused gate.

## 4. Approved phase gates

| Phase | Outcome and owning boundary | Hard semantic/contract dependencies | Recommended execution predecessor | Global/release role and exit gate |
|---|---|---|---|---|
| G9O1 | Deterministic source/knowledge bundles and operational guides; no geometry | approved G9P operations contracts; no G9 semantic phase | first after final G9P approval | global operational closeout requires fresh deterministic evidence; focused + composed PASS and author approval |
| G9A1 | Durable geo/spatial/frame/system/map/relation/binding IDs, registry, XML, copy remap and undo/reopen substrate; no solver | approved spatial spec/ADRs and green frozen G8 authority; **not** G9O1 PASS | after G9O1 to improve reproducibility | identity/system graph round trip, copy, undo, compatible-redefine transaction and collision matrix PASS; author approval |
| G9A2 | Projection systems, intrinsic `pi_i`, diagram maps `delta_i`, roles/bindings/statuses, projection-defined point and one-way 3D adapter | G9A1 PASS and approved point/system contract | immediately after G9A1 | intrinsic/diagram reconstruction, reprojection, hinge/system degeneration and normal-DAG publication PASS; author approval |
| G9A3 | Hostile lifecycle, compatible redefine versus replacement, binding/system mutation, migration, legacy behavior, copy closure and recovery | G9A2 PASS | immediately after G9A2 | complete lifecycle/redefine/migration matrix and deterministic persistence PASS; author approval closes G9A |
| G9U0 | Public experimental Locus V2 with typed one-dimensional generators, rich metric/intersection, guarded standard total Length, semantic point/token, persistence/help/flags | G9A3 PASS plus approved public-surface contracts and green G6-G8 | first public phase after G9A | inspect actual GeoGebra overload conventions and present mapped-scalar syntax for author review; scalar-map, segment/circle/arc/V2 support, nesting/cycles, native Classic preservation and lifecycle regressions PASS; author approval |
| G9X1 | Fidelity DTO, preflight, conditionally mandatory sidecar/paired writes, exact G5 corpus and bounded approved semantic approximations | approved DXF contracts plus green G5 and internal G6-G8 semantics; no hard G9U0 dependency | after G9U0 for the most useful public integration | exact compatibility plus explicit approximation/domain/gap/work/strict-partiality/identity-scope/reader evidence PASS; author approval |
| G9U1 | Manifest/schema v2 and `CeDG Construction` workspace with real approved actions | approved U0 and X1 action contracts/PASS as product-integration inputs; no spatial-kernel authority | after U0 and X1 | manifest-only toolbar authority, point-on-V2/DXF exposure, saved-layout/Classic/accessibility PASS; author approval |
| G9B | Staged canonical schemas for approved primitives under projection-system context | G9A3 PASS and approved primitive proposal; **not** G9U1 | may run after G9A3 or after U1 for lower staffing conflict | type-specific intrinsic/diagram sufficiency, reconstruction, reprojection and degeneration PASS; author approval |
| G9C | Constructive curves, edges, loops, faces, supporting/developable surfaces and bounded composite topology | G9B PASS and approved composed boundary; no GUI dependency | after G9B | incidence/adjacency/orientation/closure/projection-coherence PASS; author approval |
| G9 closeout | Integrate approved kernel, public/product and operational evidence; no new semantics | G9A/B/C, U0/X1/U1 and G9O1 closeout evidence | after both tracks complete | composed verifier, migration, canonical models, docs/bundles, licensing and performance budgets PASS; explicit author approval |
| G9U2 | `CeDG Dihedral Procedures` workspace and explicit procedure tools consuming projection systems/maps/hinges | approved global G9 PASS plus U1 workspace infrastructure; separate prompt | only after global closeout | procedure provenance, Protocol visibility, ambiguity, persistence and system/certificate tests PASS; author approval |

The phase architecture is author approved. G9O1 is authorized and not started;
all other productive phases remain designed and unauthorized, and G9U2 remains
blocked on global G9 PASS. Every execution still requires explicit invocation
of its versioned canonical prompt; a checked-in prompt is not execution.

`G9 global closeout` is an author/verifier review action, not a productive
implementation phase. It is governed by this plan, the author-decision package
and the composed `tools/agent/verify.ps1` authority. It therefore has no separate
implementation prompt and cannot introduce semantics while closing evidence.

## 5. Parallelization boundary

Documentation, independent analytic references, icons/localization inventories,
and export reader-characterization can proceed in parallel only after their
own gate permits it. G9A1–A3 remain serial. After A3, G9B/C may advance
independently of U0/X1/U1 when editable boundaries are enforced. X1 can advance
from the internal G6-G8 source contract, although executing it after U0 remains
the recommended integration schedule. U1 follows U0 and X1 so its promised
professional workspace exposes real approved actions rather than placeholders.

No phase may modify a neighboring semantic authority merely to make its gate
pass. A discovered need for label identity, bidirectional authority cycles,
render-derived geometry, hidden construction export objects, or nonpersistent
public results stops the phase for author review.

## 6. Architectural synthesis

The spatial recommendation is role-gated hybrid authority with exactly one
edit authority per object/revision. A durable projection system separates
intrinsic frame projection `q_i = pi_i(x)` from geometric placement in the
common CeDG diagram `p_i = delta_i(q_i)`. Projection-defined reconstruction is
the first implemented mode; derived projections and 3D views are one-way
outputs. Transitions are explicit graph rewrites, never automatic bidirectional
loops or viewport-derived plane relations.

The normative public Locus design keeps legacy `Locus` untouched and introduces
an experimental typed one-dimensional generator. It covers explicit scalar
maps and semantic points on segment, circle, circular arc or Locus V2 without
making V2 a generic `Path`. Durable preimage identity is separate from
revision/currentness evidence; nested acyclic loci use the normal DAG. Rich
metric/intersection results remain authoritative, while scalar and point
children are guarded adapters. G9U0 must expose total `Length[GeoLocusV2]` only
through rich-result scalar admissibility, and must choose mapped-scalar command
syntax only after inspecting actual GeoGebra overload conventions and presenting
the result for author review. GeoCeDG Classic preserves/recomputes native
supported objects and identities without creation UI; external upstream open is
not guaranteed and may never be hidden through lossy downgrade.

The GUI recommendation makes a versioned profile manifest the single source of
truth for named workspaces, views, docking, input placement, toolbar help,
actions, maturity gates, icons, and localization. Saved document layouts remain
explicit document state; Classic remains a separate diagnostic path.

The DXF design retains exact G5 mappings and uses export-only bounded adaptive
polylines with truthful error evidence. A deterministic sidecar is mandatory
for every fidelity reduction and optional for all-exact output; partial output
is strict-reject by default and any later partial option is explicit, warned,
and sidecar-backed. The documentation/bundle design assigns one owner to each
document role and enumerates bundles from the Git index with provenance,
rights, deterministic ordering, budgets, and restricted-content exclusion.

## 7. Review result

G9P-R1 closed the projection-system, redefine, generator and dependency
topology gaps identified by author review. The author accepted D1–D8, promoted
the six specifications, and accepted ADR 0010–0015. The remaining exact API,
XML, numerical and implementation choices are owned by their future phase
gates rather than unresolved G9P semantics. G9O1 is authorized and not started;
G9A/B/C and G9U0/U1/X1 remain designed but unauthorized, and G9U2 remains
blocked on an approved global G9 gate.

## 8. Post-closeout G9U0-R2 approved design authority (2026-08-21)

This section records the later author-approved planning/design closeout for
`G9U0-R2 — PRE-G9U1 PRODUCT / DOCUMENT REFINEMENT`. It does not rewrite the
author-approved G9P/G9P-R1 decision or its frozen evidence, and it does not
authorize or start R2 implementation.

After G9U0, G9U0-R1 and G9X1 closed `PASS — AUTHOR APPROVED`, the author
requested a formal product-refinement gate before G9U1 for two bounded concerns:

- ordinary Locus V2 visual style/Properties/render continuity; and
- `.cedg` native GeoCeDG document identity with `.ggb` compatibility input.

The approved planning selects **G9U0-R2** as the smallest established naming
convention. `G9U1A` remains the internal schema/compiler slice already named in
the workspace contract and is not reused. R2 is nevertheless an independent
gate with separate specifications, ADR, prompt, verifier, evidence and author
review.

The approved post-closeout product order is:

```text
G9U0-R1 PASS --+
                +--> G9U0-R2 --> G9U1
G9X1 PASS ------+
```

G9U0-R2 implementation requires both closed predecessors as an entry/order gate. G9X1 is not
the semantic authority for styles or document persistence. G9B/G9C retain the
approved G9A3 dependency and remain independent of U1/R2. A future R2 implementation PASS would
only make G9U1 eligible for a separate author authorization; it would not start
or approve U1.

The approved architecture, exact normative supersessions and terminal state
are in `docs/architecture/g9u0_r2_product_refinement_design.md`. The original
G9P plan above remains accepted historical authority while the later closeout
adds this pre-U1 gate:

```text
G9U0-R2 PLANNING / DESIGN = PASS — AUTHOR APPROVED
G9U0-R2 IMPLEMENTATION = AUTHORIZATION REQUIRED — NOT STARTED
implementationAuthorized = false
G9U1 = DESIGNED — NOT AUTHORIZED
```

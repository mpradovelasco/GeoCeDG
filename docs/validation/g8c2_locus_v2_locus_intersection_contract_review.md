# G8C2 Locus V2 × Locus V2 contract review

**Status: PASS — AUTHOR APPROVED**

**Review date:** 2026-08-15

**Reviewed baseline:** G8C1 closeout commit
`3c72e889a436e4bbccde177e1f24423196575f04`, published on `main` and
identified by the peeled `geocedg-g8c1-pass` tag.

## 1. Disposition

The final source-level comparison found no substantive contradiction between
the proposed G8C2 pair architecture and the completed G8C1 implementation.
The author therefore:

- approves the G8C2 two-parameter contract as normative;
- accepts ADR 0009 without changing its architectural decision;
- authorizes a separately invoked G8C2 implementation task; and
- leaves G8C2 implementation not started and G9 not started.

This review adds no productive G8C2 Java and does not execute the canonical
G8C2 prompt.

## 2. Comparison with the actual G8C1 implementation

| G8C1 source fact | G8C2 consequence | Result |
|---|---|---|
| `AlgoLocusIntersectionV2` registers semantic source/target dependencies and atomically publishes `LocusIntersectionResult2D` through `GeoLocusIntersectionResult` | G8C2 reuses publication/lifecycle authority but needs an explicit two-locus DAG algorithm | compatible |
| `LocusIntersectionTarget2D` and `ExtendedTargetIntersectionCapability2D` reduce one locus plus a target to `h(t)` | `F(t)=Q(u)` must not be forced into this adapter seam | separate dual-parameter solver required |
| `LocusIntersectionResult2D` keeps result kind, completeness, roots, overlap, diagnostics, work and current revisions orthogonal | Pair solutions can extend root evidence with two preimages without a second rich-result framework | compatible |
| `AlgoLocusIntersectionPointV2` selects by semantic token and applies local admissibility independent of global completeness | Option B remains valid for locally isolated pair roots | compatible |
| G8C1 continuation rejects coordinate/order repair and records ambiguity | Pair continuation can preserve the same strict identity rules for two moving loci | compatible |
| G8C1 is query-local and its 1/10/100 counters retain no index state | G8C2 starts query-local with bounded pair-space counters | compatible |

No G8C1 code supplies pair rectangles, a two-variable refinement, source-pair
symmetry, or overlap parameter maps. Their absence is the intended G8C2
boundary, not a contradiction or permission to approximate them with samples.

## 3. Final normative decisions

1. **Solver boundary.** Use a dedicated query-local dual-parameter solver and
   explicit two-source DAG algorithm. Reuse the single rich-result/lifecycle
   framework.
2. **Source symmetry.** Durable geometric identity uses a canonical unordered
   source pair. Ordered numerical evidence has a total reversible mapping.
3. **Pair provenance.** Every finite root records both source revisions,
   branch/components, semantic parameters, pair-local evidence and topology.
   Parameters and isolating rectangles are revision-scoped evidence.
4. **Local isolation.** Residual satisfaction or Newton convergence alone is
   insufficient. Point admissibility requires exhaustive local pair-region
   coverage plus justified uniqueness/isolation.
5. **Tangency.** A normalized tangent determinant supports the approved
   transverse/tangent/undetermined hierarchy but never proves multiplicity by
   raw magnitude alone.
6. **Overlap.** Established, suspected-not-established, and unsupported overlap
   remain typed. No overlap is represented by sampled points.
7. **Constructive multiplicity.** Equal coordinates do not merge distinct
   branch/component/parameter-pair solutions.
8. **Completeness and Option B.** `COMPLETE` requires exhaustive coverage of
   every valid component product. An individually verified, locally isolated,
   unambiguous pair root remains consumable under weaker global completeness.
9. **Domains.** Initial support is bounded finite components and approved
   periodic fundamental domains. Viewport or arbitrary-window truncation is
   forbidden.
10. **Identity/lifecycle.** No coordinate repair or universal merge/split
    genealogy. Ambiguity, overlap transitions and topology changes are explicit.
11. **State/work.** Query-local deterministic limits cover branch/component
    pairs, rectangles, evaluations, subdivisions, refinements, Jacobians,
    overlap checks, continuation comparisons and outputs.
12. **G9 gate.** G8C2 is the final required productive G8 subphase. G8 global
    closeout must precede G9.

## 4. Canonical execution handoff

- Prompt:
  [g8c2-locus-v2-locus-intersections.prompt.md](../../.github/prompts/tasks/g8c2-locus-v2-locus-intersections.prompt.md)
- Canonical LF SHA-256:
  `e7f0535332a9c5a2789f98476aef2f9f143e84f9bda0ad3b7d657f070d99e58b`
- Expected branch:
  `feature/g8c2-locus-v2-locus-v2-intersections`

The prompt must be invoked by a separate task. Its successful execution may
only end as an implementation candidate awaiting author review; it may not
self-close G8C2 or G8.

## 5. Scope audit

```text
G8C2 productive source = 0
G8C2 execution = 0
public command/API expansion = 0
generic Path = 0
XML/persistence/migration = 0
legacy GeoLocus/Classic semantic changes = 0
3D/G9 = 0
shared/global intersection state = 0
```

## 6. Final state

```text
G8C1 = PASS — AUTHOR APPROVED
G8C2 CONTRACT REVIEW = PASS — AUTHOR APPROVED
G8C2 CONTRACT = NORMATIVE — AUTHOR APPROVED
ADR 0009 = ACCEPTED
G8C2 = AUTHORIZED — NOT STARTED
G8 = IN PROGRESS
G9 = NOT STARTED
```

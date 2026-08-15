# G8C2 Locus V2 × Locus V2 traceability matrix

**Status: PASS — AUTHOR APPROVED**

| Requirement | Normative source | Productive realization | Validation |
|---|---|---|---|
| Two-source normal DAG | G8C2 spec §4; ADR 0009 | `AlgoLocusLocusIntersectionV2` inputs and atomic rich-Geo publication | lifecycle only-A/only-B/both and atomic-failure tests |
| Source-pair symmetry | G8C2 spec §4.1 | `LocusPairIdentity2D`, canonical query order, reversible pair evidence | `argumentReversalKeepsTokenAndReversesOrderedEvidence` |
| Semantic pair provenance | G8C2 spec §4 | `LocusPairSourceRevisionEvidence2D`, `LocusPairIntersectionEvidence2D` | analytic transverse and stale/currentness tests |
| Independent two-sided verification | G8/ADR 0008; ADR 0009 | `LocusPairIntersectionSolver2D.verifyCandidate()` | residual-failure, evaluator crossing, scale/translation tests |
| Local pair isolation | G8C2 spec §4.2 | `LocalPairIsolationEvidence2D` with method/coverage/uniqueness | isolated analytic root versus evaluator/tangent unisolated tests |
| Option B | normative G8 R1; G8C2 spec | existing point-admissibility predicate plus pair evidence/currentness checks | all three completeness states and point-consumer tests |
| Completeness coverage | G8C2 spec §4.5 | component-product keys; complete-claim downgrade | complete empty, unresolved empty, component coverage tests |
| Tangency/higher contact | G8C2 spec §4.2 | normalized tangent determinant; certified candidate hierarchy | tangent/even root and higher-contact tests |
| Constructive multiplicity | G8C2 spec §4.3 | branch/component/solution lineage tokens; parameter-pair dedup only | same-coordinate distinct preimages and close roots |
| Typed overlap | G8C2 spec §4.4 | additive pair overlap status/relation and mixed result kind | full/partial/reverse/repeated/suspected/unsupported/mixed tests |
| Finite/periodic domain scope | G8C2 spec §4.5 | semantic component products and provider canonicalization | periodic circles, seam lifecycle, unbounded rejection |
| Strict continuation | G8C2 spec §5; ADR 0009 | pair-aware `LocusIntersectionContinuation2D`; appearance epochs | motion, discovery/order, disappearance, merge/split, overlap transition |
| Query-local bounded state | G8C2 spec §6 | `LocusPairIntersectionWorkBudget2D` and pair instrumentation | 1/10/100, 100 consumers, 2×3, 33×33 tests |
| No render/sample authority | G6/G8 specs; ADR 0008/0009 | semantic evaluator only; forbidden counters unchanged | viewport test, zero-counter assertions, static verifier |
| Scientific downstream role | G8C scientific traceability | existing token point consumer over pair rich result | DAG nesting depth 1/2/3 |
| Compatibility boundary | G8C2 prompt/spec | internal Java only; no command/Path/XML/legacy/3D/G9 | diff and focused/composed verifier scope audits |

## Evidence chain

```text
normative G8/G8C2 contract + Accepted ADR 0009
    -> internal pair query/solver/evidence/algorithm
    -> 34 focused tests + independent 80-digit references
    -> focused G8C2 verifier
    -> composed repository verifier
    -> D1–D13 author closeout
    -> G8 global PASS — AUTHOR APPROVED
```

Historical CeDG samples supply scientific scenarios only. Numerical truth is
provided by analytic formulas, the independently reproduced high-precision
manifest, current semantic evaluators, and independently verified residuals.

# G8B-R1 point admissibility versus global completeness

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Date | 2026-08-14 |
| Entry commit | `6529a4ebfafa5dc9dca3cc1b4c3e7a89ebcba375` |
| Branch | `feature/g8b-locus-v2-intersections-kernel` |
| Parent phase | G8B **PASS — AUTHOR APPROVED** |
| Normative contract | [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) |
| Machine evidence | [`g8b-intersection-kernel-evidence.json`](../../geocedg/validation/locus-v2/g8b/g8b-intersection-kernel-evidence.json) |

This focused refinement implements the author-selected Option B. The author
approved G8B-R1 and the containing G8B minimum on 2026-08-14. It adds no target
family and exposes no public surface.

## A. Original issue

The original G8B point consumer admitted a selected token only when the parent
rich result was a current successful `FINITE + COMPLETE` set. That coupled two
different claims: validity of one returned root and proof that every root had
been enumerated. It prevented downstream CeDG construction from consuming an
otherwise established semantic intersection when exhaustive global coverage
was unknown.

## B. Author decision

Global `IntersectionCompleteness` remains mandatory set-level evidence. It says
whether the returned set exhausts the supported semantic domain. Point
admissibility is solution-local. An independently verified, semantically
identified, locally established and unambiguous finite solution can drive its
token-selected point when parent completeness is `COMPLETE`, `INCOMPLETE`, or
`NOT_ESTABLISHED`.

Defining that point does not advertise or imply global completeness. In
particular, `NOT_ESTABLISHED` means exhaustive coverage has not been proved; it
does not invalidate an individually established solution.

## C. Implemented admissibility predicate

`LocusIntersectionResult2D.findPointAdmissibleSolution(rootToken)` returns one
solution if and only if all of the following hold:

1. the token is nonblank and occurs exactly once in the current rich result;
2. the parent computation is `SUCCESS`, geometry kind is `FINITE`, currentness
   is `CURRENT`, and support is not `UNSUPPORTED`;
3. the solver has already published the solution atomically after a fresh
   semantic evaluation, finite-coordinate check, normalized residual check and
   authoritative target-membership check;
4. `LocalIsolationStatus` is `ESTABLISHED`, not merely an evaluator-only
   localization or broad-phase candidate;
5. identity is `NEW_TOPOLOGICAL_SOLUTION` or
   `CONTINUATION_ESTABLISHED`, with an explicit continuation key;
6. source-pair identity, constructive lineage, topology context, established
   branch lineage, semantic revision, and target update stamp agree with the
   parent binding.

The predicate deliberately contains no `completeness == COMPLETE` condition.
It also performs no coordinate search. The derived
`AlgoLocusIntersectionPointV2` uses the verified semantic coordinate from the
returned rich solution and otherwise publishes an undefined point.

## D. Characterized cases

| Parent/solution state | Point behavior |
|---|---|
| `FINITE + COMPLETE`, verified/local/unambiguous token | defined |
| `FINITE + INCOMPLETE`, selected root uncompromised and locally established | defined; parent remains `INCOMPLETE` |
| `FINITE + NOT_ESTABLISHED`, selected root uncompromised and locally established | defined; parent remains `NOT_ESTABLISHED` |
| `EMPTY + COMPLETE` | no token and no point |
| no verified roots with global coverage not established | rich `UNRESOLVED`; no point and no claim of absence |
| evaluator-only/localization-only or failed residual | undefined |
| stale binding, unsupported state, failure, duplicate/missing token | undefined |
| overlap/infinite result | no arbitrary point; the current result taxonomy does not expose a separately projectable finite subresult |
| established even/tangent root with local evidence | may be defined independently of global completeness |
| merge/split with ambiguous correspondence | undefined despite verified coordinates; no universal genealogy |
| newly discovered additional root | existing established tokens and their points remain unchanged unless the new evidence reveals genuine ambiguity |

Two solutions at an identical Cartesian coordinate remain distinct when their
constructive preimages and tokens differ.

## E. Lifecycle and no-retarget contract

Continuation retains one bounded prior successful finite snapshot. Matching is
limited to locally established solutions with an admissible identity state and
an explicit continuation key. A selected point follows its semantic token, not
the solution list position, parameter value, isolating interval, revision, or
coordinate.

The executed lifecycle is:

```text
admissible token -> absent/failure/ambiguity -> undefined
undefined -> same token re-established by approved continuation -> defined
```

Failure publication is atomic: an old coordinate cannot coexist with a current
failure result. Topological merge/split ambiguity creates new or ambiguous
identity events; it does not silently attach the old point to a nearby child.

## F. Performance and bounded state

R1 adds only a bounded scan of the immutable finite-solution list for an exact
token plus constant-time field checks per match. It performs zero semantic or
target evaluations, refinement iterations, candidate isolation, or coordinate
searches. The 100-consumer test confirms that adding consumers leaves the rich
result's semantic-evaluation counter unchanged, retained intersection-index
entries at zero, retained topology epochs at no more than two, and every
forbidden-authority counter at zero.

No whole-locus regeneration, G7 metric-index access, shared owner, global cache,
or unbounded token history was introduced. The existing deterministic G8B work
budgets are unchanged.

## G. Scope and review disposition

R1 changes only the solution-local evidence plumbing, rich-result token lookup,
continuation eligibility, derived point consumer, focused tests, and coupled
contract/evidence documentation. It adds no conic, function, implicit,
locus–locus, public command, generic `Path`, XML/persistence, migration, legacy
`GeoLocus`, Classic, 3D, Python, G9, shared-owner, cache, or unrelated
optimization work.

The focused suite contains 49 G8B tests: 25 kernel, 15 lifecycle, and 9
topology/scientific tests. Exact commands and logs are recorded in the machine
evidence and final task report. The author approved this evidence and the
implemented Option B contract; G8 remains in progress because extended 2D
families are deferred to a separately authorized G8C design task.

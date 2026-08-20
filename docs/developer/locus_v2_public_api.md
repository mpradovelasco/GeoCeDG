# Locus V2 experimental public API

- Status: **G9U0 = PASS — AUTHOR APPROVED**
- Audience: kernel, command, persistence and GeoCeDG frontend developers

The normative semantics are defined by the
[public-surface specification](../../geocedg/specs/locus/locus-v2-public-surface.md)
and the frozen G6–G8 contracts. This document records the author-approved G9U0
experimental surface. Approval does not make the feature stable, default-on or
available outside its explicit GeoCeDG opt-in boundary.

## Author-approved G9U0 command families

| Surface | Result | Contract |
|---|---|---|
| `LocusV2[Q,P]` | `GeoLocusV2` | `P` is constrained by exactly one registered segment, circle, circular-arc or V2-branch provider |
| `LocusV2[Q,s,D]` | `GeoLocusV2` | scalar identity mapping with explicit typed domain descriptor `D` |
| `LocusV2[Q,t,s,D]` | `GeoLocusV2` | mapped scalar; `s` is true coordinate and dependent `t` is never assigned |
| `Point[L,branchKey,u]` | ordinary `GeoPoint` | durable semantic preimage plus current revision binding; V2 does not implement generic `Path` |
| `LocusLength[L]` | rich metric geo | authoritative total query |
| `LocusLength[L,A,B]` | rich metric geo | authoritative between-position query |
| `Length[L]` | `GeoNumeric` | guarded child or reuse of the rich total query |
| `Intersect[L,T]` | rich intersection geo | closed G8 target registry only |
| `Intersect[L1,L2]` | rich intersection geo | finite/periodic G8C2 domain contract |
| `Intersect[R,token]` | ordinary `GeoPoint` | exact token selection without solving or ordinal fallback |

`D` has the strict shape
`{periodic,{a,b,includeA,includeB},...}`. Ordered endpoints encode
orientation. A periodic descriptor contains exactly one non-degenerate
half-open fundamental interval; a finite descriptor contains one or more
ordered disjoint components.

## Provider registry

The initial public registry is closed:

- scalar identity and reconstructible scalar mapping;
- point constrained on a finite segment;
- point constrained on a circle;
- point constrained on an oriented circular arc; and
- point constrained by one explicit supported V2 branch/component position.

Generic paths, legacy `GeoLocus`, arbitrary curves, random/history-dependent
states, viewport windows and fitted samples are rejected with typed status.

## Result authority

Rich metric and intersection geos are nonnumeric diagnostic authorities. Their
reconstructible queries persist; computed revision payloads do not. Scalar and
point children publish only after the rich result admits them.

For intersections, local point admissibility and global completeness are
orthogonal. `INCOMPLETE` or `NOT_ESTABLISHED` coverage does not invalidate a
separately isolated current solution, while an unisolated, stale or ambiguous
token never publishes a point.

Tokens are opaque owner/query/lineage digests with a monotone incarnation. The
rich result separately persists an exact address proof: provider semantic
signature, target contract signature and canonical-parameter bits. That proof
can retain a token when the same preimage moves in Cartesian space, but it is
not token-selection material and cannot authorize proximity/order repair.
Changed address proof, merge/split and overlap burn the old token. Closure copy
rebases only through immediate rich-result, token-text and point `copySource`
records; copy-of-copy never accepts a grandparent token.

## Lifecycle expectations

- recomputation keeps durable identity when the semantic definition is
  equivalent;
- user copy receives fresh owned IDs and rewrites internal references;
- undo/reopen restore serialized operation identity;
- unknown provider versions remain unsupported rather than guessed;
- branch loss or ambiguous continuation undefines bound points and dependents;
- delete/recreate does not recover identity from a label or coordinate; and
- no render cache, sample list or numeric snapshot is serialization authority.

## Feature policy

`cedg.locus.v2` remains experimental and default-off. Algebra input, toolbar,
menu, help and selection actions consult one runtime decision. Native file load
uses a distinct preservation decision so a disabled creation surface does not
strip supported objects. GeoCeDG Classic allows preservation but not creation.
External upstream runtime behavior was not executed during G9U0 and remains
outside the compatibility guarantee.

## Implementation maturity

The signatures above are the author-approved G9U0 experimental selection. The
exact 114-path candidate inventory and 93-case source mapping are frozen;
focused and deterministic execution each passed 93/93 and the complete
composed authority passed. The G9U0 phase decision is `PASS — AUTHOR APPROVED`,
but this document does not authorize G9X1, G9U1, G9B, G9C, G9U2, G10 or any
later implementation.

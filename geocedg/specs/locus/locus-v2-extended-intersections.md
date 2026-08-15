# Locus V2 extended 2D intersection semantics

**Status: G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED**

**Phase: G8C design and G8C1 `PASS — AUTHOR APPROVED`; G8C2 `AUTHORIZED — NOT STARTED`**

**Approval date: 2026-08-15**

This is a non-destructive extension of the normative
[`locus-v2-intersections.md`](locus-v2-intersections.md) contract. Every G8B
rule remains normative for its supported line, segment, ray, and circle
families. The common invariants and the G8C1 one-parameter contract in sections
1–3, 5–7 as applicable to one source locus, and 8.1 below are normative and
author-approved for G8C1. The G8C1 implementation was author-approved on
2026-08-15. The author approved the two-parameter Locus V2 × Locus V2 contract
in section 4, its pair-specific uses of sections 5–7, and section 8.2 on
2026-08-15 after accepting ADR 0009 and reviewing the completed G8C1 kernel.
Those sections are normative for G8C2 implementation. G8C2 is authorized but
not started. Neither phase claims public availability.

## 1. Required semantic chain

Native dynamic intersection with supported ordinary 2D curves and with another
Locus V2 is fundamental CeDG infrastructure:

```text
constructive procedure -> semantic locus/loci -> rich intersection set
    -> semantic token -> internal derived point -> downstream construction
```

The rich immutable result remains authority. A point is a derived consumer and
must preserve parent completeness and per-solution provenance without implying
that all intersections were found.

## 2. Common invariants

- semantic evaluators, valid components, branches, revisions, and explicit
  domains are source authority;
- `IntersectionCompleteness` remains orthogonal to individual verification and
  point admissibility;
- Option B remains required for every extended family;
- identity never derives from coordinate, result order, list index, parameter,
  isolating interval/box, revision, residual, or completeness;
- sign changes alone never establish tangency;
- overlap never becomes an arbitrary finite point sample;
- candidate broad phase never establishes incidence without semantic refinement
  and independent verification;
- publication is current, atomic, deterministic, and query-local;
- render, legacy sampling, viewport, zoom, DPI, pixel tolerances, and G7 metric
  state are forbidden authorities.

## 3. Single-parameter extended targets

For branch `F_j:C_j -> R^2` and a typed target adapter `T`, solve

```text
h_j(t) = level_T(F_j(t)) = 0
```

over every declared valid source component. `level_T` is candidate/refinement
data; `residual_T` is independently defined verification evidence. They may be
related but are not interchangeable.

Every adapter shall declare:

- exact target type and current revision;
- target domain and invalid boundaries;
- authoritative incidence and normalized residual meanings;
- derivative/gradient/tangent capability and guarantee;
- semantic bounds capability;
- analytic/certified/numerical support level;
- overlap and degeneration support;
- deterministic budgets and diagnostics.

### 3.1 Nondegenerate conics

G8C1 support is ellipse, parabola, and hyperbola represented by a
current nondegenerate `GeoConicND`. Circle remains governed by G8B.

At a regular point of canonical quadratic `G`, the verification quantity may
use

```text
rho = G(x,y) / ||grad G(x,y)||.
```

Its guarantee is **first-order normal geometric residual**, not exact distance.
It shall be invariant under `G -> cG`, `c != 0`, within the declared floating
guarantee. Canonical axes and type data remain available for stronger
family-specific analytic checks. Degenerate point/line-pair/double-line/
parallel-line/empty conics are unsupported by this smooth adapter and require
separate subtype semantics.

### 3.2 Functional graphs

G8C1 support is an explicitly bounded, real-valued `GeoFunction`
graph over declared finite valid components. Incidence is

```text
rho_vertical(x,y) = y - f(x).
```

This has model-length units and establishes graph incidence under the target
function semantics, but is not Euclidean distance. Where a valid derivative
exists,

```text
rho_normal_1 = (y-f(x)) / sqrt(1+f'(x)^2)
```

is a first-order normal estimate only. Undefined/nonfinite evaluations, poles,
restricted-domain endpoints, and conditional discontinuities split valid
components and are never bridged. An undefined target value is not `EMPTY`, and
failure to exhaust all valid components is not `COMPLETE`.

Unrestricted functions whose finite search range would be derived from a view,
and unrelated parametric/freehand/data curves, are outside this first subset.

### 3.3 Polynomial implicit curves

G8C1 support is a `GeoImplicitCurve` with finite polynomial
coefficients and a regular gradient at each promoted solution. At a regular
point, use the same typed first-order normal residual `G/||grad G||`, invariant
under nonzero scalar equation multiplication.

General nonpolynomial implicit expressions, singular roots (`||grad G||=0`),
isolated singular points, and factor/component claims without stable semantic
metadata remain unsupported or explicitly unresolved. A singular root may be
promoted only by a future stronger analytic/certified local-isolation contract;
small raw residual is insufficient.

## 4. Locus V2 × Locus V2

**G8C2 status: NORMATIVE — AUTHOR APPROVED / AUTHORIZED — NOT STARTED.** The
author-approved G8C phase split recognizes this as a separate two-parameter
architecture. The final review against G8C1 found no contradiction, and
Accepted ADR 0009 governs the contract below.

For current valid components of branches `j,k`, solve

```text
H_jk(t,u) = F_j(t) - Q_k(u) = (0,0).
```

A finite solution shall record both source identities and revisions, both
branch/component bindings and lineage, `(t,u)`, an isolating parameter region,
evaluated coordinates from both sides, coordinate/residual evidence, normalized
Jacobian/tangent evidence, method/guarantee, completeness provenance, topology
context, continuation status, token, and diagnostics.

The parameter pair and isolating rectangle are revision-scoped numerical
evidence. They are not durable identity.

### 4.1 Source symmetry

The geometric source pair is unordered. The computation may order operands for
determinism, but `Intersect(A,B)` and `Intersect(B,A)` shall expose semantically
equivalent solution identity. Reversal swaps ordered parameter/revision evidence
and tangent orientation; it does not create a new geometric root token.

### 4.2 Local isolation and point admissibility

For regular transverse solutions, local isolation may be `ESTABLISHED` only
when a declared parameter rectangle is exhaustively isolated and a justified
uniqueness result exists, such as certified box uniqueness or regular-Jacobian
evidence coupled to a safeguarded isolating construction. Solver convergence,
small residual, or spatial proximity alone is insufficient.

For

```text
tau = det(F'(t),Q'(u)) / (||F'(t)|| ||Q'(u)||),
```

nonzero `tau` supports transverse classification when both tangents are regular.
`tau=0` is a tangency candidate, not by itself a multiplicity proof. Tangent or
singular solutions need analytic, certified interval, or justified higher-order
evidence; otherwise classification/local isolation is not established.

An individually verified and locally established pair remains point-admissible
under Option B even when global completeness is `INCOMPLETE` or
`NOT_ESTABLISHED`, provided identity and continuation are unambiguous.

### 4.3 Constructive multiplicity

Different branch pairs, component pairs, or semantic parameter pairs are
different constructive solutions even at identical coordinates. Deduplication
occurs only in semantic pair space under explicit lineage and seam rules, never
in Cartesian space alone.

### 4.4 Overlap

The result taxonomy shall distinguish at least established overlap, suspected
but not established overlap, and unsupported overlap. Established overlap needs
semantic component-wide evidence such as an exact/certified parameter map.
Matching samples can only support suspicion. Full, partial, reverse traversal,
repeated traversal, and mixed overlap-plus-isolated solutions remain typed; no
overlap is projected to arbitrary points.

### 4.5 Completeness and domains

`COMPLETE` requires coverage of every declared branch/component pair and
exhaustive isolation/exclusion over each parameter product. Finite candidate
exhaustion alone does not prove coverage.

The initial G8C2 contract supports finite component products and periodic branches
through one canonical fundamental domain with seam equivalence. An arbitrary
window over an unbounded domain never yields `COMPLETE`; viewport truncation is
forbidden. Current G6 intervals do not represent infinite endpoints, so
unbounded Locus V2 × Locus V2 is explicitly unsupported pending a separately
approved provider extension.

## 5. Lifecycle and identity

Durable pair identity conceptually contains the canonical source pair,
constructive solution lineage, applicable branch-pair lineage, and topology/
continuation context. Current parameters, isolating region, revisions, residual,
Jacobian and solver certificate remain evidence. Reparameterization or argument
reversal does not itself change identity when an explicit semantic continuation
map establishes equivalence.

When either source changes, continuation is accepted only if unique under both
current semantic contexts. Root merge/split, simultaneous topology change,
overlap entry/exit, or symmetric correspondence exposes explicit ambiguity or
identity discontinuity. There is no universal genealogy and no nearest-coordinate
repair.

## 6. State, work and failure

Both phase architectures start query-local. Work is bounded by versioned
limits for evaluations, branch/component pairs, subdivisions/boxes, candidates,
refinements, derivative/Jacobian evaluations, overlap checks, continuation
comparisons, and diagnostics. Budget exhaustion produces a coherent current
rich result with truthful completeness; it cannot publish stale or partial
success as current.

Any later shared accelerator must be bounded, revision-scoped, construction-
isolated, deterministic, exception-safe, non-authoritative, semantically equal
to cache-disabled execution, and separately approved.

## 7. Compatibility and maturity

The extension is internal, experimental, disabled by default, and nonpersistent.
It authorizes no public command/API, generic `Path`, XML, migration, legacy
`GeoLocus`, Classic behavior, 3D, G9, Python DSL, or GUI geometric authority.

## 8. Phase gates

### 8.1 G8C1

The author approved the G8C1 subset, one-parameter contract, implementation,
and measured query-local baseline on 2026-08-15. G8C1 is `PASS — AUTHOR
APPROVED`. Its adaptive evaluator capability reports `NOT_ESTABLISHED`
completeness unless an independently authoritative capability supplies an
exhaustive coverage proof. A verified regular root is locally isolated only
with semantic localization plus normalized transverse evidence; local-minimum
tangencies remain unisolated absent stronger uniqueness evidence.

### 8.2 G8C2

G8C2 is `AUTHORIZED — NOT STARTED`. Productive work may begin only through a
separate explicit invocation of the canonical G8C2 prompt from the approved
contract baseline. G8 may close and G9 may begin only after G8C2 implementation
passes and receives author approval, unless the author explicitly changes the
typed minimum scope.

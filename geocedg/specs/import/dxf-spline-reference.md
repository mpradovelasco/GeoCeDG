# DXF SPLINE reference import — `dxf-spline-reference/v1`

| Field | Value |
|---|---|
| Status | **APPROVED — NORMATIVE CONTRACT `dxf-spline-reference/v1`, DOCUMENT REVISION 2, AUTHOR APPROVED (2026-11-24)** with the precisions of the [approval record](../../../docs/validation/pre_g9b_maint_a_n1_author_approval_record.md). The approved text is that of commit `8747c731eccc3644cd1da8b74eb3453a68511813`. `selfApproved=false`. Approval is not implementation: `MAINT-A-DXF0` is not authorized. The initial import limits stay revisable after characterization. Where the sections below call this document, the family specification or ADR 0043 a candidate or proposed, that records the stage at which they were written |
| Contract identifier | `dxf-spline-reference/v1` (document revision 2, 2026-11-23: weight reconstruction of §4.6 per the author's corrective instruction; revision 1 is commit `307bc8fda`) |
| Increment that writes it | `PRE-G9B-MAINT-A-N1` (normative design only) |
| Increment that would implement it | `MAINT-A-DXF0`, **not authorized** |
| Architectural layer | Import adapter outside the geometric kernel (AGENTS §4); pure mathematical library shared with the kernel family |
| Decision | [ADR 0043](../../../docs/adr/0043-explicit-2d-bspline-family-rational-ready.md) (accepted, 2026-11-24) |
| Author directions | D-A-6, D-A-7 (R-a first, R-c later, R-d not authorized), D-A-8 to D-A-10 for the reserved approximation interface, and the D-V4 conditions; [decision sheet](../../../docs/validation/pre_g9b_maint_p0_author_decision_sheet.md) §0 |
| Companion contracts | [B-spline family `bspline-2d`](../curves/bspline-2d.md) (approved, 2026-11-24); [validation matrix](../../../docs/validation/pre_g9b_maint_a_n1_validation_matrix.md) (candidate) |

## 0. Status and boundary

This contract defines how GeoCeDG may read 2D DXF `SPLINE` entities as **imported
reference geometry**: visible, inspectable, with provenance and without implicit CeDG
semantics. It authorizes no code. It does not change the approved DXF export contracts
([geometry export foundation](../export/geometry-export-foundation.md),
[DXF curve fidelity](../export/dxf-curve-fidelity-and-approximation.md)), ADR 0014 or any
SplineV2 contract.

An imported reference is **not** a construction object. It is not a `GeoElement`, it is
not in `Construction`, it has no dependencies, it is not a `GeoLocusV2`, it is never an
input of a construction tool, command or Locus V2 capability, and its render certifies
nothing. The DXF file is never modified.

## 1. The four layers

The import keeps four layers apart (proposal §5.4, D-V4):

| Layer | What it is | Authority |
|---|---|---|
| 1. Authoritative CeDG geometry | DAG objects with definition, identity, dependencies and declared guarantees | the only geometric authority |
| 2. Imported reference definition | immutable data read from an external file, with provenance (this contract) | none over the construction; an external datum |
| 3. Approximating construction | a new CeDG object of the `bspline-2d` family whose relation to a reference is a declared fit (`MAINT-A-DXF1`) | that of its own definition; none over the reference |
| 4. Render | what a view draws of any of the above | none |

No layer silently replaces another. No association between layers is established by
coordinates, proximity, names or handles. The relation between an approximation and its
reference is provenance only.

## 2. Placement

The adapter has three parts, all outside the kernel packages:

1. **Reader**: parses the file, selects candidate entities, validates them and produces
   one typed outcome per entity.
2. **Reference model**: immutable records (the binary64 data as read, the transformed
   data, provenance and diagnostics) owned by the document session (§11), never by
   `Construction`.
3. **Render**: a tessellation in model units computed once per record (§8); a view only
   transforms and draws it.

Evaluation uses the pure mathematical library fixed by the family specification
([`bspline-2d.md`](../curves/bspline-2d.md) §12). That library is not a geometric
authority; using it here gives the reference no CeDG semantics.

## 3. Definitions

For an admitted entity, with degree `p`, control points `P_0..P_n`, knots
`U = (u_0..u_m)`, `m = n + p + 1`, and effective weights `w_0..w_n` reconstructed by
§4.6:

\[
C(u) = \frac{\sum_{i=0}^{n} N_{i,p}(u)\,w_i\,P_i}{\sum_{i=0}^{n} N_{i,p}(u)\,w_i},
\qquad u \in [a,b] = [u_p, u_{n+1}],
\]

with the Cox–de Boor basis, the `0/0 := 0` convention and the knot-ownership rule of the
family specification §3.3 (the same rule; it is not restated with variations here).
Orientation is that of increasing `u`. Four facts about weights are kept apart and
recorded separately (§4.6, §5): the DXF representation flag (bit 4 of code 70), the
provenance of the weights (explicit code-41 values or implicit unit weights because the
file has no code 41), the effective weights, and the mathematical nature of the curve. The
nature is decided by the effective weights alone, by exact binary64 comparison:

| Nature | Effective weights | Curve |
|---|---|---|
| `POLYNOMIAL` | all equal to 1 (implicit or explicit) | the polynomial B-spline of `p`, `U` and the control points |
| `RATIONAL_EQUAL_WEIGHTS` | explicit, all exactly equal to a common value different from 1 | exactly the same polynomial curve (the common weight cancels); the explicit values are kept as read |
| `RATIONAL` | explicit, not all equal | a rational curve |

`POLYNOMIAL` and `RATIONAL_EQUAL_WEIGHTS` are the cases of exact conversion in
`MAINT-A-DXF1` (§14).

## 4. Input scope

### 4.1 File

| Aspect | Admitted | Otherwise |
|---|---|---|
| Encoding | ASCII DXF: a sequence of group-code/value line pairs | binary DXF (sentinel `AutoCAD Binary DXF`) → file rejected, `BINARY_DXF_UNSUPPORTED` |
| Version | any version whose `ENTITIES` section can contain `SPLINE` (R13, `AC1012`, and later); `$ACADVER` is recorded | missing or older version → recorded as `UNKNOWN`; the entity rules still decide |
| Structure | a well-formed `ENTITIES` section | malformed structure (odd line count, nonnumeric group code, unterminated section) → file rejected, `MALFORMED_DXF` |
| Size | at most `FILE_BYTES_MAX` bytes (candidate value 64 MiB) | file rejected before parsing, `FILE_LIMIT_EXCEEDED` |
| Entity count | at most `SPLINE_ENTITIES_MAX` candidate `SPLINE` entities (candidate value 10 000) | the whole import is rejected, `FILE_LIMIT_EXCEEDED`; no prefix is imported, because a file-order prefix would be an arbitrary selection |

String values (layer name, handle) are provenance only. They are decoded as UTF-8 for
`AC1021` and later and as ISO-8859-1 otherwise; undecodable bytes are kept as escaped
bytes. No string value affects geometry or acceptance.

### 4.2 Entity selection

Only top-level `SPLINE` entities of the `ENTITIES` section in model space are candidates.

| Case | Outcome |
|---|---|
| `SPLINE` in model space | candidate, validated by §4.3–§4.5 |
| `SPLINE` in paper space (group 67 = 1) | not imported, `PAPER_SPACE_UNSUPPORTED` |
| entities inside `BLOCKS`, or reached through `INSERT` | not imported, `BLOCK_CONTENT_UNSUPPORTED`; `INSERT` is never expanded |
| every other entity type | ignored; counted by type in the import report |

### 4.3 Group codes of a `SPLINE` entity

| Code | Meaning | Use in `v1` |
|---|---|---|
| 5 | handle | provenance |
| 8 | layer name | provenance |
| 67 | paper-space flag | selection (§4.2) |
| 70 | spline flags: 1 closed, 2 periodic, 4 rational, 8 planar, 16 linear | 2 decides admission; 4 takes part in the weight rule of §4.6 and is recorded; 1 is contrasted with the data; 8 and 16 are informative |
| 71 | degree | definition |
| 72, 73, 74 | number of knots, control points, fit points | consistency |
| 40 | knot value, repeated | definition |
| 41 | weight, repeated; the DXF reference allows it to be omitted when the weight is 1 | effective weights by §4.6 |
| 10, 20, 30 | control point, repeated (WCS) | definition |
| 42, 43, 44 | knot, control-point and fit tolerances | recorded as ignored; never used to merge knots or to decide closure |
| 11, 21, 31 | fit points | recorded as ignored (§4.4) |
| 12/22/32, 13/23/33 | start and end tangents (fit data) | recorded as ignored |
| 210, 220, 230 | plane normal | recorded; never used to transform coordinates |
| 100, 330, 6, 62, 370 and other style or ownership codes | subclass marker, owner and style | ignored; the reference has its own presentation |
| any other code | — | the entity is still decided by the codes above; the unrecognized codes are listed in the record's diagnostics as `UNRECOGNIZED_GROUP_CODES` |

The geometry of a control-point `SPLINE` is fully determined by codes 70, 71, 72, 73, 40,
41 and 10/20/30. No other code can make an entity admissible or change its curve.

### 4.4 Admission predicates

An entity is admitted only when every predicate holds; otherwise it is rejected
atomically with the first failing cause in this order (later causes may be listed as
additional diagnostics):

| # | Predicate | Typed cause on failure |
|---|---|---|
| 1 | every numeric value is a well-formed DXF real (§4.5) and finite | `MALFORMED_REAL`, `NONFINITE_VALUE` |
| 2 | flag 2 (periodic) is clear | `PERIODIC_UNSUPPORTED` |
| 3 | control-point data are present (code 73 > 0 and code-10 points present); a spline with only fit points is not admitted | `FIT_POINTS_ONLY_UNSUPPORTED` |
| 4 | `1 ≤ p ≤ P_MAX_IMPORT` (candidate value 7, aligned with the family) | `DEGREE_OUT_OF_RANGE` |
| 5 | number of control points read `= code 73 = n + 1`, with `p + 1 ≤ n + 1 ≤ N_MAX_IMPORT` (candidate value 1 024) | `CONTROL_POINT_COUNT_MISMATCH`, `TOO_FEW_CONTROL_POINTS`, `SIZE_OUT_OF_RANGE` |
| 6 | number of knots read `= code 72 = n + p + 2` | `KNOT_COUNT_MISMATCH` |
| 7 | knots nondecreasing | `KNOTS_DECREASING` |
| 8 | clamped: `u_0 = … = u_p` and `u_{n+1} = … = u_{n+p+1}` by exact binary64 equality, and `u_p < u_{n+1}` | `NOT_CLAMPED_UNSUPPORTED`, `EMPTY_DOMAIN` |
| 9 | every interior knot value has multiplicity `≤ p` | `INTERIOR_MULTIPLICITY_UNSUPPORTED` |
| 10 | the number of code-41 values is `0` or exactly `n + 1` (§4.6) | `WEIGHT_COUNT_MISMATCH` |
| 11 | when flag 4 is clear, every explicit weight is exactly 1 | `NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG` |
| 12 | every explicit weight `> 0` and `max w / min w ≤ W_RATIO_MAX` (candidate value `10^6`) | `NONPOSITIVE_WEIGHT`, `WEIGHT_RATIO_OUT_OF_RANGE` |
| 13 | every control point has the same `z` by exact binary64 equality (an absent code 30 reads as `0`) | `NONPLANAR_UNSUPPORTED` |

When both control and fit data are present, the control data are used and the presence of
fit data is recorded. Predicate 13 places the curve exactly in the plane `z = c` (partition
of unity and positive weights); `c` is recorded and the reference is the planar curve in
`(x, y)`.

Closure (flag 1) never decides admission. The record is **exactly closed** when `P_0` and
`P_n` are equal coordinate by coordinate under binary64 `==`; otherwise it is open. A flag 1
contradicted by the data, or exact closure without flag 1, is diagnosed
(`CLOSED_FLAG_CONTRADICTED`) and the data decide. Closure is never inferred from
proximity or from the tolerances of codes 42–44.

Each entity is admitted or rejected atomically. A file with admitted and rejected
entities yields the admitted records and the list of rejected entities with their causes.

### 4.5 Decimal reading rule

A real value is the trimmed ASCII token matching
`[+-]?(digits[.digits?]|.digits)([eE][+-]?digits)?`. Tokens with a decimal comma, a
hexadecimal form, `NaN` or `Infinity`, or any other text are `MALFORMED_REAL`. A
well-formed token is converted to the nearest binary64 value, ties to even (the rule of
`Double.parseDouble`). An overflow to an infinity is `NONFINITE_VALUE`. Integers of codes
70–74 must be well-formed decimal integers within range.

The record holds exactly these binary64 values. The difference between the decimal text
of the file and the binary64 data is the declared **reading rounding**; nothing computed
later is claimed about the decimal text itself.

### 4.6 Weight reconstruction

The DXF reference (Autodesk, *AutoCAD DXF Reference*, entity `SPLINE`) defines bit 4 of
code 70 as «rational spline» and code 41 as a weight that is written only when it is not 1;
it states that the weights are present when not all of them are 1. The absence of code 41
is therefore a legitimate representation of unit weights and is not, by itself, an
incoherence. The reader reconstructs the effective weights as follows, with no other rule:

| Flag 4 | Code-41 values | Outcome | Weight provenance | Nature (§3) |
|---|---|---|---|---|
| clear | none | admitted | `IMPLICIT_UNIT` | `POLYNOMIAL` |
| set | none | admitted with implicit unit weights; informative diagnostic `RATIONAL_FLAG_WITH_IMPLICIT_UNIT_WEIGHTS` | `IMPLICIT_UNIT` | `POLYNOMIAL` |
| set | exactly `n + 1`, all valid (predicate 12) | admitted | `EXPLICIT` | `RATIONAL` when not all equal; `RATIONAL_EQUAL_WEIGHTS` when all equal to a value other than 1; `POLYNOMIAL` when all equal to 1 |
| clear | exactly `n + 1`, all exactly 1 | admitted; the explicit representation is recorded; informative diagnostic `EXPLICIT_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG` | `EXPLICIT` | `POLYNOMIAL` |
| clear | exactly `n + 1`, at least one different from 1 | rejected, `NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG` | — | — |
| any | between 1 and `n` values, or more than `n + 1` | rejected, `WEIGHT_COUNT_MISMATCH` | — | — |

Rules:

- With exactly `n + 1` values, the `i`-th code-41 value in order of occurrence within the
  entity is the weight of the `i`-th control point. A list of any other length is never
  completed, truncated or matched to control points: no unknown weight is deduced.
- Implicit unit weights are effective weights only. The record stores them as the explicit
  absence of code 41 (`IMPLICIT_UNIT`), never as code-41 values the file did not contain;
  the canonical encoding of §9 uses the absence marker.
- Finiteness (predicate 1), positivity and the ratio bound (predicate 12) apply to every
  explicit weight; implicit unit weights satisfy them trivially. Size limits are unchanged.
- The flag is recorded as read. It never changes the nature: an entity with flag 4 whose
  effective weights are all 1 is mathematically polynomial and is not given rational
  semantics.

## 5. Reference record

An admitted entity produces one immutable record:

| Field | Content |
|---|---|
| `contract` | `dxf-spline-reference/v1` |
| `nature` | `POLYNOMIAL`, `RATIONAL` or `RATIONAL_EQUAL_WEIGHTS` (§3) |
| `representation` | the code-70 rational flag as read (`SET` or `CLEAR`) and the weight provenance (`EXPLICIT` or `IMPLICIT_UNIT`) (§4.6) |
| `sourceData` | `p`, `U`, control points `(x_i, y_i)`, the explicit weights as read or the absence of code 41, plane `z = c`, exactly as read |
| `effectiveWeights` | the weights reconstructed by §4.6 |
| `transform` | the transformation applied (§6) |
| `data` | the transformed control points (weights and knots are never transformed) |
| `domain` | `[u_p, u_{n+1}]`, orientation of increasing `u` |
| `closure` | `OPEN` or `EXACTLY_CLOSED` |
| `continuity` | parametric continuity per interior knot from its multiplicity (`C^{p-k}`), the same structural rule as the family; no claim of regularity or `G^1` |
| `provenance` | §9 |
| `diagnostics` | ignored data, contradicted flags, unrecognized codes |
| `renderTessellation` | §8; derived, never authority |

A record never changes after admission. Changing the transformation or re-reading the
file produces a new record; it never edits an existing one.

## 6. Units and transformations

- The header `$INSUNITS` code is recorded with its name (`0` = unitless). The document's
  construction unit ([unit system](../units/unit-system.md)) is recorded beside it.
- **No implicit scaling.** By default the transformation is the identity: one DXF drawing
  unit becomes one model unit. When both units are specified and differ, the import report
  shows a non-blocking notice; it never converts on its own, consistent with the unit
  system's rule that numbers are kept and never rescaled implicitly.
- The user may choose an explicit affine transformation
  `x' = a·x + b·y + e`, `y' = c·x + d·y + f`, with finite `a..f` and `a·d − b·c ≠ 0`
  (otherwise `SINGULAR_TRANSFORM`). A proposed unit-conversion scale is such a
  transformation and is applied only by explicit choice.
- The transformation acts on the control points only; knots and weights are unchanged.
  This is exact in exact arithmetic for polynomial and rational curves alike (affine
  invariance with unchanged weights). In binary64 each coordinate is computed as
  `(a·x + b·y) + e` in that order, without fused multiply-add; the result is the declared
  **transformation rounding**.
- The six coefficients are recorded as binary64 values. Orientation and domain are never
  reversed or renormalized silently.

## 7. Evaluation

Points and derivatives of a record are computed by the pure library with the homogeneous
de Boor algorithm in binary64 (family specification §11–§12). They are numerical values,
declared as such, independent of zoom, view, DPI and tessellation. Evaluation serves the
render and the future approximation (§14) only. No CeDG measure (length, intersection,
incidence, tangent, point on curve) is published for a reference.

## 8. Render

- The tessellation is computed once per record, in model units, by deterministic adaptive
  parameter refinement that splits at every distinct knot, with a chordal tolerance
  `τ_render = RENDER_REL_TOL · diag(bbox(control polygon))` (candidate
  `RENDER_REL_TOL = 10^-4`) and a declared work limit. It is independent of zoom; a view
  only applies its transformation. Work-limit exhaustion is diagnosed and the partial
  tessellation is drawn with a visible incomplete-render mark.
- The tessellation error is an estimate, never a certified bound, and never evidence of
  anything about the curve.
- Presentation is the reference presentation (§10), distinct from construction objects.

## 9. Provenance

Per record, with no dependence on the time of import:

| Item | Content |
|---|---|
| file digest | SHA-256 of the file bytes |
| file name | base name only; a full path is session information and is not persisted by any later option without author decision |
| DXF version | `$ACADVER` or `UNKNOWN` |
| entity | handle (code 5), layer (code 8), ordinal of the entity among the `ENTITIES` section entries |
| units | `$INSUNITS` code, document construction unit at import |
| plane | `z = c` |
| transformation | the six coefficients (§6) |
| weights | the code-70 rational flag as read and the weight provenance: explicit code-41 values, or `IMPLICIT_UNIT` when the file contains no code 41 for the entity (§4.6) |
| contract | `dxf-spline-reference/v1` |
| data digests | SHA-256 of the canonical encoding of `sourceData` and of `data` |
| ignored data | fit points, tolerances 42–44, tangents, unrecognized codes, contradicted flags |
| informative diagnostics | `RATIONAL_FLAG_WITH_IMPLICIT_UNIT_WEIGHTS`, `EXPLICIT_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG` (§4.6) |

Canonical encoding of the data for digests: the ASCII tag of the contract, then `p`,
`n + 1`, the knots, the control points and the weights (or an explicit absence marker) as
big-endian binary64 bit patterns in index order. The handle is not unique across files
and is never an identity outside its file; the digest pair (file, entity data) is the
reference's provenance identity.

## 10. Distinction from CeDG constructions

- Distinct presentation (reference style) and an inspector state «Imported reference,
  read-only», with nature, contract, closure, provenance and diagnostics.
- Not selectable as an input of any construction tool and never a snapping target. A click
  of the Point tool on a reference creates no point «on» it: that would be an association
  by coordinates.
- No command accepts a reference as a path, an intersection target, a curve for
  `Length`/`LocusLength`, a transformation source or a list element.
- References are not `GeoElement`s, so they are outside the population of the approved
  semantic exports (DXF, semantic LaTeX); GeoCeDG never rewrites a foreign entity as its
  own. Picture exports of a view: open question Q-DXF-3 (§17).
- Visibility of a reference is a session presentation state; it is not a layer of the
  construction's layer domain and does not mean existence or geometric visibility.

## 11. Persistence `R-a`: session only

Under `v1` the records belong to the document session:

| Event | Behavior |
|---|---|
| Import | adds records to the session store of the open document; it is not a construction step and adds no Undo/Redo entry; `Construction`, its XML and its Undo history stay unchanged |
| Remove | explicit user action on a record; not a construction step |
| Undo / Redo | never adds, removes or changes records |
| Save | references are not written; when records exist, a non-blocking notice says they are session-only |
| New, Open, Close | the session store is cleared |
| Copy / paste | references are never copied into a construction |
| Reopen | requires a new import; nothing in the document refers to a previous import |

`R-a` creates no parallel DAG: a record is a datum without dependencies.

## 12. `R-c`: later priority target

`R-c` keeps a copy of the imported definitions and their provenance in the document,
outside `Construction`, without a parallel DAG. It is not specified by `v1`. Before it is
specified, a characterization must establish at least:

1. how GeoCeDG 1.2.2 and the current build treat an unknown entry of the `.cedg`
   container, and whether re-saving in an older build drops it silently;
2. the entry or element name, its own versioned identifier and its fail-closed rule for a
   newer version;
3. the stored data: the record fields of §5 (never the tessellation), with the stored copy
   as the authority of the reference and the external file as provenance only;
4. save and reopen rules, including a mismatch between the stored copy and a file found
   again;
5. interaction with Undo/Redo, copy/paste between documents and closing without saving;
6. size limits and integrity check of the stored copy;
7. exclusion from semantic exports and from the construction's layer domain.

The read-only characterization made by `MAINT-A-N1` for these points is recorded in the
[candidate report](../../../docs/validation/pre_g9b_maint_a_n1_candidate_report.md) §3.

## 13. `R-d`: not authorized

A reference object inside `Construction` with identity and dependencies (proposal §8.7)
is not specified and is not authorized. Nothing in `v1` presupposes it.

## 14. Interface reserved for the approximation (`MAINT-A-DXF1`)

`MAINT-A-DXF1` consumes a record only through an explicit user action on that record,
never through construction-tool selection. It reads the immutable `data`, `nature`,
`domain`, `closure` and provenance digests. It produces a **new** construction of the
`bspline-2d` family, frozen and independent of the reference (D-A-10), with the
approximation provenance block defined in the family specification §10.5. The record is
not modified and gains no link to the result.

The approximation method, its limits, its typed failures and the computation of its three
error measures are fixed by the separate approximation contract that `MAINT-A-DXF1` must
have approved before code. This contract fixes only the distinctions that contract must
keep (D-A-8, D-A-9):

| Case | Operation | Guarantee |
|---|---|---|
| `POLYNOMIAL` or `RATIONAL_EQUAL_WEIGHTS`, within the family limits | exact conversion: the same `p`, `U` and transformed control points | exact on the data; not an approximation |
| any other admitted record | numerical fit | sample residual, numerical estimate, and, when available, a certified global bound; each with its own grade |

Nothing computed on the approximation (lengths, tangents, intersections, points) is ever
attributed to the reference.

### 14.1 Five notions that stay distinct

Let `C` be the reference curve defined by the record's binary64 `data` (so the reading and
transformation roundings are outside every measure below and are declared separately), and
`A` the approximating polynomial B-spline of degree `q` on the same domain and orientation.

| Notion | Definition | Grade |
|---|---|---|
| Exact conversion | `A` has the same `p`, `U` and control points as `C`; only possible when `C` is polynomial (or has all weights equal) and within the family limits | exact on the data; error 0 by definition, never reported as a fit |
| Numerical fit | computation of `A` from `C`; first direction: linear least squares in the source parameter (`A(u) ≈ C(u)`), interpolated endpoints, deterministic solver (D-A-8) | a construction procedure, not a guarantee |
| Sample residual | `max` and root mean square of `‖C(u_k) − A(u_k)‖` over the fit samples `u_k` | a fact about those samples only; nothing between them |
| Numerical error estimate | maximum of `‖C(u) − A(u)‖` over a distinct, denser verification set refined by local maximization | `ESTIMATED`; not a bound |
| Certified global bound | an upper bound of `sup_{u∈[a,b]} ‖C(u) − A(u)‖` established by the procedure below | rigorous with respect to `data`, under its preconditions |

Because the correspondence is parametric, a bound on `sup_u ‖C(u) − A(u)‖` also bounds the
Hausdorff distance between the two images. It bounds no difference of length, tangent or
curvature, and does not place the intersections of `A` near those of `C`.

**Certified bound procedure (design).** Write `C = 𝒜 / W` with `𝒜 = Σ N_{i,p} w_i P_i`
and `W = Σ N_{i,p} w_i`. Then

\[
C(u) - A(u) = \frac{D(u)}{W(u)}, \qquad D(u) = \mathcal{A}(u) - W(u)\,A(u).
\]

1. Preconditions: all weights finite and `> 0`; `C` and `A` clamped on the same `[a, b]`;
   every input value finite. Otherwise no bound is published (typed reason).
2. Refine both curves to a common knot vector containing every distinct knot of each, by
   knot insertion carried out in **interval arithmetic with outward (directed) rounding**,
   so that each refined coefficient is an interval containing the exact coefficient of the
   exact curve defined by `data`.
3. On each common span, form the Bernstein coefficients of the vector polynomial `D`
   (degree `≤ p + q`) by interval products and sums with outward rounding.
4. By the convex-hull property, `‖D(u)‖ ≤ max_k ‖d_k‖` on the span; bound each `‖d_k‖`
   from above with upward rounding (sum of squared interval magnitudes, then an upward
   square root).
5. By partition of unity and positive weights, `W(u) ≥ min_i w_i` on `[a, b]`; take
   `min_i w_i` rounded downward.
6. The bound is `max_spans max_k ‖d_k‖ / min_i w_i`, the division rounded upward. It may be
   tightened by subdividing spans (de Casteljau in interval arithmetic) under a declared
   work limit; exhausting the limit publishes the last valid bound or, if none, no bound.

The bound is a theorem about the curve defined by `data`; it says nothing about the decimal
text of the file. Its exact arithmetic model, its interval library and its tests belong to
the approximation contract of `MAINT-A-DXF1` (or `MAINT-A-DXF1b`, D-A-9).

## 15. Import report

Each import produces a deterministic report: file provenance, counts of entities by type,
admitted records, rejected entities with cause and the predicate order of §4.4,
diagnostics, units notice and the transformation. The report never claims that an
admitted reference is exact with respect to the decimal text of the file, nor that its
render is exact.

## 16. Validation

The candidate cases are DX-1 to DX-13 of the
[A-N1 validation matrix](../../../docs/validation/pre_g9b_maint_a_n1_validation_matrix.md).
`MAINT-A-DXF0` must execute them; `MAINT-A-N1` does not.

## 17. Stop conditions and open decisions

Stop conditions for any implementation of this contract:

- the reference would enter `Construction`, the DAG or a Locus V2 capability (that is
  `R-d`, not authorized);
- a construction tool, snapping or a command would accept a reference;
- the import would mutate `Construction`, its XML or its Undo history;
- a scale, unit conversion, closure or orientation would be applied without an explicit
  rule of this contract;
- the reader would need a DXF writer change or an approved export-contract change.

Open decisions for the author:

| Id | Question | Candidate proposal |
|---|---|---|
| Q-DXF-1 | Limits `FILE_BYTES_MAX`, `SPLINE_ENTITIES_MAX`, `N_MAX_IMPORT`, `W_RATIO_MAX`, `RENDER_REL_TOL` | 64 MiB, 10 000, 1 024, `10^6`, `10^-4` |
| Q-DXF-2 | Rational flag and code-41 weights | revision 2: policy of §4.6, fixed by the author's corrective instruction of 2026-11-23 after contrast with the DXF reference; only incoherences that are real (non-unit weights without the flag, partial lists) are rejected, consistent with D-A-6. The contract as a whole remains a candidate |
| Q-DXF-3 | Picture exports (PNG, PDF, SVG of a view) when references are visible | reproduce the view as presentation, with no semantic claim, as for any view content; semantic exports exclude references |
| Q-DXF-4 | Location of this contract | new directory `geocedg/specs/import/`, parallel to `export/` |

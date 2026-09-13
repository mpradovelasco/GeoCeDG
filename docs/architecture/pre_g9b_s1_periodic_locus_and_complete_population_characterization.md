# PRE-G9B-S1 periodic Locus and complete-population characterization

Status: **R1 PARTIAL IMPLEMENTATION — LOCUS SEMANTIC BLOCKER OPEN**

Product implementation: **TYPED POPULATION IMPLEMENTED; LOCUS DECOMPOSITION
NOT STARTED**

Entry candidate: `1221e5c5b3bb20f9ad65e7f36ac58f624beef3c0`

Self approval: `false`

This note preserves the bounded investigation after the first S1 candidate and
records the later author disposition. The author approved continuous semantic
components as the Locus export authority and policy B as the complete DXF
population. The population correction is implemented below. The Locus
subproblem remains stopped at its explicit semantic boundary.

## 0. Kernel versus export decision

`LocusBranch2D.getValidDomainComponents()` is normative shared-kernel evidence,
not an export hint. The approved G6 model defines each component as an oriented
parameter interval on which semantic evaluation is currently valid. Metrics,
semantic point operations, intersections, rendering and G9X1 all consume that
same list. Consequently continuous validity decomposition belongs in the
shared Locus kernel; G9X1 must consume it and must not discover a competing
export-only partition.

The current `circle-point/v1` producer contradicts that intended meaning for
`m`: `AlgoDependentPointLocusV2.fixedDomain(...)` publishes the complete nominal
`[-pi, pi)` interval as its sole valid component, while its reconstructible
evaluator returns `DEPENDENCY_UNDEFINED` throughout an internal interval.
Neither `LocusParameterPartition2D` nor the intersection result supplies a
global driver-parameter validity partition. Intersection completeness is
established separately at one construction state; it does not certify where a
durably selected root exists over the whole circle parameter.

No bounded existing API can establish all component boundaries or prove that
no smaller invalid island was missed. Endpoint/midpoint/dyadic sampling can
isolate observed transitions, but cannot turn the intervals between point
evaluations into semantic validity authority. Making `m` exportable therefore
requires a new shared interval/continuation certificate for the selected root
and its dependent construction. The task's stop condition applies before such
productive kernel semantics are invented.

## 1. Periodic `m` finding

The proposed excluded-endpoint failure sequence is **rejected by direct
evidence**. The loaded author fixture establishes all of the following:

- `m` has a `VALID` semantic definition;
- its `circle-point/v1` provider and `generator.main` branch are periodic;
- the one declared/valid component remains exactly `[-pi, pi)`, lower closed
  and upper open;
- the provider canonicalizes `pi` to `-pi` before valid-domain membership and
  evaluation;
- evaluations at `-pi`, `pi`, `0`, and `pi/2` are `VALID`; and
- evaluation at the first dyadic quarter, `-pi/2`, is
  `DEPENDENCY_UNDEFINED`, with the diagnostic `Dependent point is undefined or
  nonfinite`.

Additional probes bind the failure to an internal interval rather than to the
periodic seam:

| Parameter | Status |
|---:|---|
| `-pi` | `VALID` |
| `pi` (canonical `-pi`) | `VALID` |
| `0` | `VALID` |
| `-pi/2 - 1e-6` | `DEPENDENCY_UNDEFINED` |
| `-pi/2` | `DEPENDENCY_UNDEFINED` |
| `-pi/2 + 1e-6` | `DEPENDENCY_UNDEFINED` |
| `pi/2` | `VALID` |
| `-2.5`, `-2`, `-1.8`, `-1.5`, `-1` | `DEPENDENCY_UNDEFINED` |

The exact observed G9X1 count (`evaluations=4`) follows the approved adaptive
order: start, end, midpoint, then the first quarter. The first three are valid;
the quarter at `-pi/2` is not. `G9X1GeometryExportAdapter` correctly recognizes
typed full-period closure, and `LocusDefinition2D` correctly applies the
provider's canonicalization. The failure is therefore not an excluded
half-open endpoint and is not a G9X1 periodic-seam regression.

The construction slice explains the typed failure without making labels an
authority: the circle driver updates a horizontal line; the retained explicit
Locus V2 intersection-root binding supplies point `S`; `U` is the midpoint of
that bound point and the driver. In the probed negative interval that bound
point, and therefore `U`, is undefined. `ReconstructibleLocusEvaluator2D`
truthfully publishes `DEPENDENCY_UNDEFINED` without stale coordinates.

An export-local success cannot be produced under the current authority. It
would have to bridge an internal invalid interval, substitute another
intersection root, consume render samples, or invent an unapproved
continuation. All are forbidden by G9X1 and the requested negative coverage.
The smallest honest next decision is one of:

1. authorize a bounded Locus V2 valid-domain characterization that can prove
   and publish the actual dynamic components and their boundaries; or
2. authorize a new explicit continuation rule for this dependent construction
   if the author intends a different bound root through the missing interval.

Either choice changes kernel semantic evidence rather than repairing the DXF
periodic seam. Until that authority exists, `m` must remain
`INVALID / DISCONTINUITY_UNRESOLVED`, no DXF/manifest pair may be published for
the single-source strict request, and no productive correction is admissible.

## 2. Complete labeled construction inventory

The real extended preflight over the author fixture reports `exact=26`,
`approximate=2`, `unsupported=15`, `invalid=1`, `hidden=14`, and
`writable=false`. The invalid entry is `m`, characterized above. The complete
unsupported population is:

| Sources | Runtime type | Role in this construction | Visible | Auxiliary flag | Current DXF meaning | Current inclusion/blocking assessment |
|---|---|---|---|---|---|---|
| `Iso1`, `Iso2` | `LIST` | ordered point inputs to the two SplineV2 producers | one hidden, one visible | false | no approved list entity | included by the all-construction-elements rule; blocking is contract-consistent but poorly aligned with “geometry” |
| `a`, `b` | `NUMERIC` | SplineV2 degree parameters | hidden | false | no geometric entity | non-geometric construction input; strict blocking follows current population, not DXF utility |
| `c`, `d`, `e`, `i`, `l` | `LOCUS_INTERSECTION_RESULT` | rich typed intersection results used by explicit root selection | hidden | false | semantic analysis/result metadata, not a DXF primitive | traceability-relevant in the DAG but not itself drawable construction geometry |
| `text1`–`text6` | `TEXT` | serialized explicit intersection-root tokens | hidden | true | no approved `TEXT` mapping; these instances are not placed annotations | auxiliary binding inputs, not geometric annotations; visibility alone is not the reason to exclude them |

All entries are identified in the regression by their construction-revision
source ID and typed outcome; labels above are only readable presentation. The
existing Desktop source method passes the entire construction-order set. The
current `COMPLETE_CONSTRUCTION` contract says all labeled 2D construction
objects, G9X1 includes hidden sources, and strict partial output is disabled.
Consequently the 15 unsupported entries continue to block after any independent
resolution of `m`.

At characterization time, construction `Text` still required an author
disposition. The six fixture texts are hidden root-token inputs; a visible,
geometrically located annotation has potential CAD annotation meaning, but
neither G5 nor G9X1 currently approves a DXF `TEXT` mapping. The later R1
decision resolved the current population rule: every `Text` remains outside
the complete geometric DXF population until a separate exact export contract
is approved. Explicit current selection continues to report it as unsupported.

## 3. Policy alternatives and author decision

### A — preserve the current strict population

- **Correctness/compatibility:** exactly preserves G5/G9X1.
- **Population:** deterministic construction order, including hidden and
  non-geometric elements.
- **Traceability:** every requested source receives an outcome.
- **User effect:** ordinary parameter and rich-result auxiliaries prevent a
  “complete” drawing even when every drawable geometry source is exportable.
- **Disposition:** valid current behavior, but selection remains the only
  practical way to obtain an exportable subset.

### B — geometric complete-construction population — author approved

- **Correctness:** define a versioned, semantic/type-based population of
  authoritative 2D geometry and approved typed curve sources.
- **Exclusions:** list containers, numeric parameters and rich analysis results
  can be outside the population because of their types, never merely because
  they are hidden.
- **Text:** absent an approved DXF `TEXT` mapping, `Text` is outside the typed
  complete population; adding annotation export remains separately reviewable.
- **Traceability:** the sidecar/preflight must identify the population-rule
  version and distinguish “outside population” from “requested but omitted”.
- **Compatibility:** refines `COMPLETE_CONSTRUCTION` before strict preflight;
  `CURRENT_SELECTION` retains explicit-source semantics.
- **Extensibility:** gives new semantic geometry types one explicit admission
  point without confusing metadata with failed geometry.

### C — explicit partial-export mode

- **Correctness:** retain the current full population but publish supported
  entries only after explicit user choice.
- **Traceability:** mandatory sidecar and UI warning must enumerate every
  omission and reason, including hidden entries.
- **Compatibility:** changes the approved `partialOutput=false` policy and the
  all-or-nothing paired-publication flow.
- **User effect:** maximizes output availability, but “complete” would no longer
  mean complete without a conspicuous qualification.
- **Disposition:** separately authorizable and potentially complementary to B,
  but not a substitute for a coherent complete-geometry population.

## 4. Implemented population rule

The author selected B. `geocedg-dxf-geometric-2d/v1` now excludes these typed
families before complete-construction preflight:

| Family | Typed reason |
|---|---|
| `LIST` | `LIST_CONTAINER` |
| `NUMERIC` | `NUMERIC_PARAMETER` |
| `LOCUS_INTERSECTION_RESULT` | `RICH_ANALYSIS_RESULT` |
| `TEXT` | `TEXT_WITHOUT_APPROVED_DXF_MAPPING` |

The classifier is deliberately fail-closed for every unclassified family: it
remains in the geometric candidate population, so an unsupported or invalid
eligible source still blocks strict publication. Visibility is not a predicate.
Current selection is not filtered; selecting an auxiliary alone still produces
an explicit unsupported outcome. Population exclusions are diagnostics with
their own code and count and, when a sidecar exists, appear as structured
warnings. They are not omitted components and do not make a complete geometric
request partial.

For the author fixture the rule removes two lists, two numerics, five rich
intersection results and six texts before preflight. The 26 exact and two
SplineV2 approximate components remain eligible. `m` remains an invalid
eligible geometric source until the kernel component contract is established,
so the complete request remains correctly non-writable.

## 5. Remaining gate

Policy B no longer needs the proposed separate complete-population gate. Its
bounded implementation is part of S1-R1. Option C remains outside scope because
it would alter strict no-partial output.

The remaining Locus gate must define reconstructible, revision-aware evidence
that partitions a dependent construction's nominal driver domain into complete
continuous valid components. For the author fixture, that evidence must account
for the durable exact-root token without changing root identity by coordinates
or query order. It must expose `COMPLETE` versus `NOT_ESTABLISHED`, preserve open
boundaries, and prove that no invalid interval is silently bridged. This is a
kernel semantic decision shared by export, metrics and intersections, not a DXF
repair.

```text
LOCUS_CONTINUOUS_COMPONENTS = BLOCKED — NEW SHARED CERTIFICATE REQUIRED
COMPLETE_CONSTRUCTION_POLICY = B — IMPLEMENTED AS TYPED GEOMETRIC POPULATION
PRE-G9B-S1-COMPLETE-POPULATION = NOT REQUIRED / ABSORBED BY S1-R1
```

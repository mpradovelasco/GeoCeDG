# SplineV2 pair intersection materialization — bounded singleton-germ contract

- Status: **NORMATIVE / IMPLEMENTED — PASS — AUTHOR APPROVED**
- `implementationComplete=true`; `manualAuthorSmoke=PASS`;
  `selfApproved=false`; `authorApprovedPhase=true`; `passClaimed=true`.
- Corrections A/B are author approved under ADR 0022; they do not change the
  pair singleton-germ selector or its certification requirements. Both recorded
  blockers are resolved on the reviewed technical cohort. PHASE A/B, COMPOSED
  and FULL `-CleanBuild` evidence remains bound to that exact commit; the later
  author closeout records approval without claiming a new technical execution.
- Phase: G9S1-R1
- Decision: [ADR 0021](../../../docs/adr/0021-spline-pair-singleton-germ-materialization.md)
- Preserved derivation: [D2](../../../docs/architecture/g9s1_r1_d2_pair_sheet_contract.md)
- Parent contracts: [semantic SplineV2](semantic-spline-2d.md),
  [Locus intersections](../locus/locus-v2-intersections.md) and
  [symmetric pairs](../../../docs/adr/0009-locus-v2-locus-intersection-pair-semantics.md).

## 1. Scope and independent evidence dimensions

This contract permits exact-token point materialization for supported
SplineV2 x SplineV2 roots. Similarity images qualify only when both sources
retain authenticated spline provenance and certifiable current polynomial
capability/evaluation. Merely implementing a generic polynomial interface is
not proof of the authorized product family. Generic LocusV2 x LocusV2 remains
under its existing rich-only materialization policy.

Keep distinct: numerical root discovery, local root existence/isolation,
transverse classification, structural identity, class coverage, global
completeness, currentness and public materialization eligibility. A selector
does not upgrade numerical truth. `NOT_ESTABLISHED` global completeness may
coexist with locally admissible roots; global COMPLETE does not admit an
ambiguous or unsupported root. Ordinary rich-result publication checks remain.

## 2. Structural semantic selector

Use scheme `pair-singleton-transverse-germ/v1`. For distinct sources, associate
each durable source ID with its own stable branch lineage, component lineage,
declared orientation, semantic domain kind and parameterization contract
version. Order those descriptors by exact durable source ID. Equal-source
self-pairs are outside this first subset. Do not independently sort IDs and
component names and thereby lose their association.

For canonical sources A/B and orientation signs epsilon_A/epsilon_B, define

```text
H(u,v) = C_A(u) - C_B(v)
germ = sign(det[epsilon_A C_A'(u), -epsilon_B C_B'(v)])
```

The selector includes POSITIVE or NEGATIVE in canonical axes. Caller reversal
transposes the current parameter evidence and reverses the caller determinant;
canonical normalization restores the same structural selector. Result owner,
unordered source-pair identity, constructive query lineage and structural
topology contract remain enclosing ledger authority. Separate rich-result
owners do not share a materialized point merely because their selectors match.

The durable key MUST NOT contain current u/v, Cartesian coordinates, boxes,
span/knot/chart IDs, output indices, projected ranks, cardinalities, current
coefficients/revisions, labels, pixels, proximity or movement history. These
cannot be smuggled into a structural topology identifier. No mutable global
epoch, winding counter or arbitrary atlas cut supplies identity.

## 3. Certified singleton class

For one entire canonical branch/component product and a selected sign, a
positive current certificate establishes all of:

1. Both source identities/revisions and semantic contracts are coherent.
2. A finite root has justified existence and uniqueness in a current semantic
   neighborhood, coherent residual/error bounds, regular transverse sign and
   exact source-associated address ownership.
3. Exactly one root of that sign exists in the full component product.
4. No unresolved region, overlap, singular/multiplicity candidate or semantic
   ambiguity can compete with that class under the certificate assumptions.

Cover the finite component product with a deterministic bounded partition.
Each region is certified zero-free, certified to contain the same unique root
by a sufficient common-root relation, or incapable of containing the selected
sign (for example a strict opposite-sign Jacobian interval throughout it).
Unknown regions block that class. An interval containing zero is not a strict
sign exclusion. Equal discovery counts and sign samples are not coverage.

There are only two transverse signs: at most two eligible roots per component
pair, not a two-root geometric limit. For `+,+,-`, only the singleton negative
class can qualify; its proof must independently exclude competing negative
roots. Two distinct certified positive roots establish positive-class ambiguity
without needing a monodromy proof. More eligible roots may exist across distinct
component pairs; never combine their ranks or components.

## 4. Numerical proof and actual floating semantics

Reuse semantic polynomial broad-phase discovery and floating refinement only
as candidate generation. The certificate requires outward-rounded interval
evaluation and a justified interval-Newton/Krawczyk existence/uniqueness test
or mathematically equivalent proof. Round every arithmetic operation needed
by the enclosure, including coordinate differences, derivative/Jacobian
evaluation and operator composition. An arbitrary epsilon around a floating
answer is not outward rounding. Singular matrices, nonfinite bounds,
underflow/overflow uncertainty and exhausted budgets fail closed.

Native SplineV2 uses the author-authorized structural model defined by
[ADR 0022](../../../docs/adr/0022-structural-spline-continuity.md) and its
[source-backed design](../../../docs/architecture/g9s1_r1_structural_spline_continuity.md).
The bounded direct solve retains the original interpolation/boundary equations;
shared truncated powers establish C^(d-1) at simple interior knots. Periodic
jet closure requires separate exact structural elimination and exact finite
endpoint agreement. Neither condition follows from a tolerance check.

Numerical free coefficients define the represented spline; its interpolation
solve is not silently exact real interpolation. Rounded expanded span arrays
are derived approximate evaluation/discovery data, not the certifier's exact
function. The interval bridge must outward-enclose the actual structural
coefficients, function and derivatives, including their rational denominator.
Retain original-equation residual, finite arithmetic and work checks without
relaxing tolerances. Legacy independent diagnostic spans require their own exact
jet checks and acquire no structural guarantee by type alone. At knots/seams
require canonical ownership and sufficient true regularity for the entire
claimed neighborhood. An approximate C1/C2 match or ideal reference spline
cannot certify a different native source.

Similarity composition must enclose the actual transformed semantic evaluator;
rounded mapped coefficients are not automatically identical to evaluating the
source and transformation. Unsupported or uncertifiable composition remains
rich-only. k=0 collapsed images cannot fabricate isolated pair roots.

Keep numeric guarantee, error/residual and global completeness truthful. Exact
opaque token means exact semantic identity matching, not exact arithmetic or
complete root enumeration. No weaker `DETERMINISTIC_LOCAL` policy is introduced.

## 5. Regular motion, charts and monodromy

Current-state deterministic selection outranks continuity heuristics. Within a
regular certified singleton class, implicit-function local sections coincide
by uniqueness, so the structural selector survives ordinary parameter drift,
projected-rank ties/exchanges, new proof boxes and compatible internal knots.
Neither endpoint certificates nor sampled motion prove an arbitrary unobserved
path, but current resolution needs no such path.

Charts are replaceable proof objects. Sufficient common-root evidence includes
certified existence inside both uniqueness neighborhoods, a verified enclosure
inside the other's uniqueness region, or existence plus uniqueness on a
justified containing region. Mere box overlap or equal germ alone is insufficient.
Canonical half-open periodic ownership publishes one seam preimage; no lift or
history enters this pair key. Missing gluing evidence is numerical uncertainty,
not an invented generic geometry discontinuity.

The accepted repeated-periodic monodromy witness has repeated same-sign sheets
and remains rich-only at every state, regardless of zero/one/inverse/two loops.
R1 does not solve unrestricted global pair labeling or create an arbitrary cut.
No remembered loop irreversibly retires an otherwise recurring structural slot.

## 6. Exact-token lifecycle

| Current condition | Existing materialized point |
|---|---|
| Same selector resolves uniquely with current admissible proof | Same point/ID/token active and defined |
| Missing root/proof, stale revision, unsupported temporary state or budget exhaustion | Retain exact claim; point dormant and undefined |
| Competing certified same-class roots or relevant topology/sheet ambiguity | Retain claim in typed non-current pair state; no retargeting |
| Same selector later has a valid unique current certificate | Reactivate SAME point/ID/token |
| Explicit incompatible durable replacement/disposal | Old binding cannot identify replacement; follow G9A/lifetime rules |

The author accepts recurrence of a structural semantic slot after
`1 -> many/0 -> 1`; it is not proof of a physical trajectory through singularity.
Transient context mismatch remains recoverable when the same durable inputs
return. Only explicit incompatible replacement/disposal is a permanent barrier,
not loss of a certificate, rank, knot, box or remembered path.

Fresh materialization requires a current eligible exact token and explicit user
confirmation. Existing rich-result and ordinary point-parent seams consume it.
Cancel creates nothing. Recompute creates zero new points and does not solve
globally per point. Pair non-current reasons are not R4 PERIODIC_QUARANTINE.

## 7. Persistence, copy and migration

Extend the existing ledger with a strict discriminated pair selector and
two-address proof, not a parallel point/coordinate-token system. A pair-bearing
snapshot uses the next strict format version (v5 after R4 v4); preserve v1-v4
one-source import semantics. Reject unknown discriminators, duplicate/conflicting
entries, down-versioned pair payloads, malformed fields and mismatched sources.
The public envelope stays externally opaque. Old rich-only `locus-pair-root/...`
diagnostic handles acquire no new rank/selector by migration or coordinates.

Reconstruct both source dependencies and owner/constructive context. Persist
durable selector/token/claim provenance; saved active, dormant or quarantine
state and old revisions/enclosures are never sufficient proof on load. The
current snapshot must revalidate the same selector before activation.

Copy/remap uses the existing exact dependency-closure provenance for both
sources. If remapped canonical source order changes, transpose address evidence
and normalize the sign accordingly. Rename does not change identity; compatible
redefine follows G9A and incompatible replacement cannot be matched by label.
Undo/redo restores exact graph/ledger state through normal host mechanisms.
Native `.cedg` tests are required; a diagnostic codec or host XML-only test is
not a substitute for the product archive lifecycle.

## 8. Efficiency and consumer boundaries

One rich query performs bounded discovery/certification; current selectors are
organized once and P existing bindings resolve by direct lookup. The current
certificate/publishing implementation has conservative combinatorial cost
`O((S+B)R + R^2 + C log C + CR + E log E + P)`: S initial span-product boxes,
B visited boxes, R certified roots, C germ classes and E retained ledger entries.
Discovery, model capture and interval/polynomial arithmetic add their own cost;
this is not an end-to-end bit-complexity bound. Ordinary unchanged child lookup
alone remains expected O(P). This corrects the earlier underspecified
bookkeeping estimate to the already characterized
[implementation bound](../../../docs/architecture/g9s1_r1_structural_spline_continuity.md#8-source-derived-work-and-materialized-child-boundary),
without changing implementation or work policy. Record interval boxes/refinements,
budget exhaustion and lookup work; measure rather than assert a speedup.
Keep only current entries and claimed retained bindings, no unbounded root or
trajectory history. R1 adds no Desktop gesture, Point tool or marker semantics.

The protected G9U1 design remains unchanged. Later reconciliation may expose
markers and create-one/multiple/all or explicit frontend auto-materialization
ONLY for current eligible R1 tokens. R6 Point-on-Locus inverse resolution is
separate and cannot bypass pair rich-only states. Existing-point reactivation
is kernel DAG recomputation; creation of new points remains an explicit action.

## 9. Verification and retained limitation

The preserved scientific-stop checkpoint was not technically complete: its
mandatory native-host knot-crossing positive did not meet the exact continuity
hypotheses of the independently rounded pieces. Consult
the [blocker report](../../../docs/validation/g9s1_r1_implementation_blocker_report.md)
and [blocker evidence](../../validation/g9s1-r1/g9s1-r1-implementation-blocker-evidence.json)
for exact jumps and the original failed executions. Those records remain
unchanged. The subsequent author decision resumes R1 with the structural
prerequisite above, not a relaxed certificate or substituted diagnostic positive.
Current state is a technically complete implementation candidate pending author
review. Fresh PHASE A/B, COMPOSED and FULL receipts satisfy the required technical
gates; the implementation matrix records their tested-cohort counts/hash and
locations. No phase PASS is inferred. Require the native positive, the new
structural matrix and every earlier pair/lifecycle regression after correction.

Use the [implementation matrix](../../../docs/validation/g9s1_r1_spline_pair_materialization_validation_matrix.md)
and [canonical prompt](../../../.github/prompts/tasks/g9s1-r1-spline-pair-intersection-materialization.prompt.md).
PHASE A/B, COMPOSED and clean-output FULL are mandatory under ADR 0020 because
R1 registers a new verification phase. Technical gates do not grant phase PASS.
The previous 13-path D2 diagnostic authority remains byte-exact except explicitly
living roadmap/traceability updates, and its historical claims remain scoped.

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN / TRACKED in the
[canonical roadmap](../../../docs/roadmap/geocedg_roadmap.md). Pair lifecycle
evidence neither supplies nor closes that distinct missing R4 native round trip.

# G9S1-R1 — SplineV2 pair materialization implementation validation

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**.
- `implementationComplete=true`; `manualAuthorSmoke=PENDING`. Corrections A/B
  are resolved on the tested cohort and all required technical levels complete
  with exit 0; no historical failed row is waived or relabeled.
- The author authorizes bounded higher-precision construction (A) and certified
  univariate structural implicit-target verification (B). Their earlier DEV
  coverage did not establish completion; the fresh PHASE/COMPOSED/FULL receipts
  below now establish technical completion, not author approval.
- D2 partial design and structural-slot recurrence: author approved.
- `selfApproved=false`; `authorApprovedPhase=false`; `passClaimed=false`.
- Authority: [ADR 0021](../adr/0021-spline-pair-singleton-germ-materialization.md),
  [specification](../../geocedg/specs/curves/spline-v2-pair-materialization.md),
  [canonical prompt](../../.github/prompts/tasks/g9s1-r1-spline-pair-intersection-materialization.prompt.md).
- Historical D2 diagnostic tests/evidence remain unchanged. None of their
  conditional proof fixtures substitutes for the productive cases below.

The preserved predecessor failed the mandatory native knot-crossing positive:
independently rounded pieces lacked the exact C0/C1 gluing needed for the claimed
interval neighborhood. Its historical record is the
[scientific blocker report](g9s1_r1_implementation_blocker_report.md)
and [blocker evidence](../../geocedg/validation/g9s1-r1/g9s1-r1-implementation-blocker-evidence.json).
The author now authorizes [ADR 0022](../adr/0022-structural-spline-continuity.md)
and the [structural correction](../architecture/g9s1_r1_structural_spline_continuity.md).
Retain the old exact diagnostic and require the native positive through the
corrected source. No row is waived and no ideal diagnostic curve substitutes
for native acceptance. Resumed PHASE A/B, COMPOSED and FULL are now complete.

## Completed tested-cohort technical evidence

These receipts apply to the frozen 76-path tested cohort, retained under
`artifacts/g9s1-r1-numerical-corrections/tested-cohort`. This subsequent
status-only documentary update does not claim a new product execution.

- [PHASE A](../../artifacts/g9s1-r1-numerical-corrections/phase-a-02/verification-result.json):
  192/192 passed, exit 0.
- [PHASE B](../../artifacts/g9s1-r1-numerical-corrections/phase-b-02/verification-result.json):
  192/192 passed, exit 0.
- Canonical PHASE A/B SHA-256, identical:
  `59793eef3641d8c93b012998c0a795fb949f5cefdb54d30ebcd337542ab155dd`.
- [COMPOSED](../../artifacts/g9s1-r1-numerical-corrections/composed-02/verification-result.json):
  1281/1281 passed, exit 0.
- [FULL -CleanBuild](../../artifacts/g9s1-r1-numerical-corrections/full-clean/verification-result.json):
  7781 tests, 7770 passed, 11 retained upstream skips, zero failures, exit 0.

All earlier failed cohorts remain historical evidence. Technical gates do not
grant phase PASS; `selfApproved=false`, `authorApprovedPhase=false` and
`passClaimed=false` remain unchanged. Manual author smoke is pending.

## Required scenarios

Every row must map to exact methods in the new scenario inventory. An explicitly
unsupported positive family must be reported as a support limitation and tested
fail-closed, not silently counted as a positive certificate. No result below is
claimed PASS merely because the row or test method exists.

| ID | Required assertion | Evidence layer |
|---|---|---|
| R1-N01 | Actual finite opposite-germ spline pair has certified local existence and uniqueness. | Current interval certificate + host |
| R1-N02 | Every enclosure operation is outward-rounded; strict analytic enclosure controls and real-content perturbations detect wrong bounds. | Arithmetic/certificate |
| R1-N03 | Krawczyk or equivalent inclusion/exclusion predicates distinguish proof, unknown and failure. | Certificate |
| R1-N04 | Near singularity/tangency and unresolved multiplicity never fabricate a transverse certificate. | Certificate/host |
| R1-N05 | Nonfinite/overflow/underflow uncertainty fails closed without stale proof. | Arithmetic/certificate |
| R1-N06 | Work limits are explicit/deterministic; exhausted coverage does not mean geometric ambiguity or permanent retirement. | Budget |
| R1-N07 | Actual floating knot/seam gluing is not certified solely by tolerance or ideal reference coefficients. | Coefficient/semantic contract |
| R1-N08 | Transform certificate encloses actual semantic composition, not an assumed equality of rounded mapped coefficients. | R5/certificate |
| R1-I01 | Opposite normalized germs yield distinct exact structural selectors and tokens when both classes are singleton. | Selector/host |
| R1-I02 | Caller source reversal preserves source-associated descriptors and normalized semantic mapping. | Selector/host |
| R1-I03 | Solver/candidate enumeration reversal preserves semantic selectors and token binding. | Determinism |
| R1-I04 | u-projection rank exchange does not change a valid singleton selector. | Diagnostic + productive control |
| R1-I05 | v-projection rank exchange does not change a valid singleton selector. | Diagnostic + productive control |
| R1-I06 | Parameters, boxes, spans, knots, labels, coefficients and revisions do not enter the durable selector. | Selector serialization |
| R1-I07 | Two same-germ roots remain rich-only; no arbitrary tie breaker. | Class coverage/host |
| R1-I08 | Mixed +,+,- permits only the independently certified singleton class, with exact reason per root. | Class coverage/host |
| R1-I09 | Unknown regions able to contain the selected sign block its uniqueness; strict opposite-sign exclusion remains mathematically valid. | Class coverage |
| R1-I10 | Component-local grouping never interchanges source-associated branch/component lineage. | Selector |
| R1-I11 | Same-source/self-pair and unauthenticated generic polynomial/Locus pairs remain outside the new materialization scope. | Generic negative |
| R1-I12 | NOT_ESTABLISHED/INCOMPLETE global status never alone blocks a proved local class; COMPLETE never admits local ambiguity. | Rich publication |
| R1-M01 | Multiple isolated pair roots in one span pair retain separate semantic evidence without span ordinal identity. | Numeric/selector |
| R1-M02 | Certified chart changes/common-root witness preserve identity; box overlap alone does not. | Certificate transition |
| R1-M03 | Compatible internal knot crossing preserves identity and right-owned canonical address. | Spline semantic boundary |
| R1-M04 | Periodic seam canonicalization creates no duplicate pair preimage and uses no winding history key. | Periodic boundary |
| R1-M05 | Exact repeated-periodic monodromy witness remains rich-only for zero, one, inverse and two circuits. | Retained diagnostic + host negative |
| R1-M06 | Quartic nine-root and original projected-rank counterexamples remain preserved, not recast as certified singleton positives. | Retained diagnostic |
| R1-L01 | Explicit exact-token materialization creates one ordinary GeoPoint; Cancel and Intersect alone create zero points. | Public API/current inspector consumer |
| R1-L02 | Broad regular certified motion keeps each existing point defined with no token swap. | Host lifecycle |
| R1-L03 | Direct/incremental/reverse/legal update sequences reach identical final selector/token/current point state. | Path independence |
| R1-L04 | Temporary absent/invalid/stale/uncertified state makes an existing point dormant without destroying its selector. | Ledger/host |
| R1-L05 | Certified same-class competition causes a typed retained pair non-current state, not R4 periodic quarantine. | Ledger/host |
| R1-L06 | Recurring unique semantic slot reactivates SAME point/ID/token without proof of historical physical trajectory. | Ledger/host |
| R1-L07 | Temporary context mismatch/budget failure or remembered loop does not permanently retire ownership. | Ledger |
| R1-L08 | Actual incompatible source/owner/lineage replacement cannot retarget an old token. | G9A/ledger |
| R1-L09 | Recompute performs no per-child global solve and creates no new GeoElements. | Instrumentation/DAG |
| R1-L10 | Last claim release prunes retained state; no unbounded historical root/trajectory store. | Lifetime/complexity |
| R1-P01 | Native .cedg reopens active pair points with both exact source dependencies and dynamic recomputation. | Desktop archive |
| R1-P02 | Native .cedg reopens dormant/pair-non-current state; later unique current proof reactivates the same ownership. | Desktop archive |
| R1-P03 | Native .cedg after reactivation preserves exact selectors/tokens and current proof revalidation. | Desktop archive |
| R1-P04 | Undo/redo crosses active/dormant/reactivated states without replacement points or stale proof. | Host lifecycle |
| R1-P05 | Copy/remap validates both source closures and normalizes sign when copied canonical ID order reverses. | G9A/ledger |
| R1-P06 | Rename and compatible redefine preserve approved identity; incompatible redefine follows existing new-identity rules. | G9A |
| R1-P07 | Strict pair-bearing format rejects malformed/duplicate/conflicting/down-versioned data; saved ACTIVE is not a certificate. | Serialization negatives |
| R1-P08 | Legacy one-source v1-v4 behavior remains; old diagnostic pair handles are never guessed into new tokens. | Migration negatives |
| R1-B01 | Translation/rotation/reflection/positive and negative dilation retain truthful pair covariance with new transformed source/token identities. | R5 |
| R1-B02 | k=0 collapsed image is nonisolated/degenerate; no fabricated point; valid recovery revalidates retained slots. | R5 lifecycle |
| R1-B03 | Metrics, semantic Point/R6 interaction and Classic Spline remain independent of pair certification. | Historical regressions |
| R1-B04 | Protected G9U1 planning and productive frontend are unchanged; current inspector consumes exact eligible tokens only. | Boundary |
| R1-V01 | Focused A/B execute the exact required cases with identical canonical scientific/source summaries. | PHASE |
| R1-V02 | New phase registration/current-run consumer remains fail-closed; historical live assertions and sealed hashes are preserved. | Operational |
| R1-V03 | COMPOSED and clean-output FULL cover the full required historical perimeter with fresh Test tasks. | Repository authority |
| R1-V04 | Source/evidence hashes, exact inventory, links, Checkstyle, both diff checks and open-risk status are valid. | Static/operational |

## Structural prerequisite cases

These twenty additive cases retain every original row. Exact structural jets,
numerical interpolation accuracy and interval enclosure are separate assertions.
Where the matrix spans several combinations, record the actual fixtures/degree/
size/scale coverage in scenario evidence rather than claiming all combinations.

| ID | Required assertion | Evidence layer |
|---|---|---|
| R1-S01 | Preserve the old exact native C0/C1 discrepancy and pass the corresponding native knot positive through the corrected model. | Historical diagnostic + current host |
| R1-S02 | Ordinary cubic has exact structural jets through degree-1 at every simple interior knot and validates original defining equations. | Structural model |
| R1-S03 | Intermediate degrees preserve the source-specific boundary rows, including selected-span polynomial extension semantics. | Family/equation equivalence |
| R1-S04 | Degree 12 has exact structural continuity and retains degree/work admission without tolerance inflation. | Maximum degree |
| R1-S05 | Minimum supported point count retains the approved family and deterministic validity/failure states. | Minimum size |
| R1-S06 | Larger counts through the supported maximum retain bounded construction and original-equation residual checks. | Size/work |
| R1-S07 | Asymmetric points and positive weights preserve the actual knot/domain construction. | Native input contract |
| R1-S08 | Dyadic internal knots have exact shared jets and canonical ownership without epsilon equality. | Exact knot control |
| R1-S09 | Binary64-rounded non-dyadic input knots remain exactly glued for the represented structural function. | Structural arithmetic |
| R1-S10 | Small coordinate scales preserve structural equality and truthful numerical validity. | Scale |
| R1-S11 | Large coordinate scales preserve outward bounds or fail explicitly on numerical limits. | Scale/overflow |
| R1-S12 | Mixed coordinate scales expose conditioning/backward-error failures without silently relaxing policy. | Conditioning |
| R1-S13 | A transverse root exactly at a native knot is locally certified under the actual structural function. | Certificate/host |
| R1-S14 | Distinct roots immediately adjacent to a knot are not merged by parameter/coordinate proximity. | Isolation/identity negative |
| R1-S15 | A root at knots of BOTH sources has symmetric current proof and one canonical pair preimage. | Pair knot |
| R1-S16 | A root at a knot of ONLY the first source preserves the same selector through regular motion. | Pair knot motion |
| R1-S17 | A root at a knot of ONLY the second source preserves source-reversal equivalence. | Pair symmetry |
| R1-S18 | Multiple roots retain per-sign class coverage; structural continuity alone never admits repeated same-germ sheets. | Multiplicity/coverage |
| R1-S19 | R5 transform compositions enclose the structural source and preserve point/metric/interaction dependencies. | Composition/consumer |
| R1-S20 | Negative dilation preserves current transformed identity/covariance; k=0 remains nonisolated and recovers without new points. | R5 degeneration |

## Independent periodic seam requirements

Periodic closure is not inferred from the interior rows above. It requires its
own exact structural elimination and native consumer evidence.

| ID | Required assertion | Evidence layer |
|---|---|---|
| R1-C01 | Closed native splines satisfy exact jets 0 through degree-1 at 0~1, including binary64-rounded knots. | Structural periodic model |
| R1-C02 | Approximate-but-not-exact closed endpoint coincidence fails explicitly; no averaging or invented periodic C0. | Closure negative |
| R1-C03 | Native periodic pair seam root has one canonical preimage and adequate current uniqueness/coverage proof. | Pair certificate |
| R1-C04 | Periodic regular motion/source reversal preserves an eligible singleton slot without a winding/history key. | Pair lifecycle |
| R1-C05 | Repeated periodic traversal remains rich-only where same-germ sheets compete; exact source smoothness does not solve monodromy. | Monodromy negative |
| R1-C06 | Periodic source reconstruction, R5 composition and R6 seam interaction retain their existing exact identity/address contracts. | Historical/native persistence |

## Historical regressions and R6 consumer guards

These four mandatory rows were added after COMPOSED exposed failures beyond the
earlier focused cohort. Their inclusion alone was not a passing result. C07 and
C08 were the historical scientific blockers and now pass with corrections A/B;
the R6 filter-order correction is independently checked by C09/C10 and did not
substitute for resolving either blocker.

| ID | Required assertion | Evidence layer |
|---|---|---|
| R1-C07 | The published 25-point degree-5 s29 source remains numerically admitted with exact structural jets; no inherited-admission regression is excused as a new negative. | Historical model/corrected regression |
| R1-C08 | The structural straight spline against `(x^2-1)^2+y=0` has no materializable transverse roots; existing points become inadmissible at the two true tangencies. | Historical univariate consumer/corrected regression |
| R1-C09 | Palindromic spline nonminimum stationary guards do not obstruct the two actual regular crossing preimages; represented finite jets, not ideal zero derivatives, govern the test. | Native R6 inverse consumer |
| R1-C10 | A genuinely singular represented minimum remains UNRESOLVED with no selected candidate, point creation or retargeting. | Native R6 singular negative |

## Authorized correction A — bounded construction precision

These rows add the current arithmetic perimeter without deleting or weakening
the original failed methods. Historical degree-12 numerical rejections remain
recorded; a newly recovered input is allowed only under unchanged original
equations/admission, not forced to remain rejected merely because double failed.

| ID | Required assertion | Evidence layer |
|---|---|---|
| R1-A01 | Historical 25-point degree-5 source is again admitted with structural jets and original defining-equation checks. | Historical positive/current construction |
| R1-A02 | Repeated exact input produces identical construction policy, representation and bounded diagnostic work. | Determinism |
| R1-A03 | Ordinary admissible cubic retains the efficient binary64 fast path. | Construction/performance |
| R1-A04 | Representative degrees between 3 and 12 preserve the same family, original boundary rows and existing admission policy; record the actual exercised degrees rather than claim every finite input is admitted. | Degree/family |
| R1-A05 | Difficult degree-12 controls distinguish previous numerical rejection, current valid recovery and genuine inadmission without universal admission claims. | High-degree diagnostic |
| R1-A06 | Asymmetric span lengths and exact binary64 knots are not rounded to a different input problem. | Parameter authority |
| R1-A07 | Small coordinates preserve exact structural relations and original numerical admission. | Scale |
| R1-A08 | Large finite coordinates either validate the same equations or fail with typed bounded numerical evidence. | Scale/overflow |
| R1-A09 | Mixed signs/scales do not introduce geometry-based admission tolerances. | Conditioning |
| R1-A10 | Maximum supported input size stays within explicit construction and precision work bounds. | Size/budget |
| R1-A11 | Periodic construction preserves exact seam jets at every admitted precision. | Periodic structural proof |
| R1-A12 | R5 transformed sources, points and partial metrics consume the corrected actual structural authority. | Transform/consumer |
| R1-A13 | Negative dilation preserves identity/domain and truthful metric/certificate behavior. | Transform orientation |
| R1-A14 | A genuinely singular defining system remains rejected; precision cannot manufacture rank. | Scientific negative |
| R1-A15 | Invalid input or a representation failing the original residual/admission remains rejected; no tolerance relaxation. | Admission negative |
| R1-A16 | Precision/work exhaustion fails closed deterministically with evidence; no unbounded ladder or stale source is published. | Budget negative |

## Authorized correction B — structural univariate implicit certification

These rows concern one-sided spline/implicit traversal, not a new pair selector.
Floating discovery is not a local existence or multiplicity certificate. Methods
must inspect numerical/root evidence before the ledger, not only point counts.

| ID | Required assertion | Evidence layer |
|---|---|---|
| R1-U01 | A genuine simple root has current structural existence/uniqueness and derivative exclusion of zero. | Univariate certificate |
| R1-U02 | Several separated simple roots retain distinct semantic evidence without discovery-order identity. | Univariate/current publication |
| R1-U03 | A near-tangent but certifiable simple root may be admitted only under rigorous current proof. | Near-singular positive |
| R1-U04 | Exact tangent evidence never acquires a transverse certificate merely from floating refinement. | Tangency negative |
| R1-U05 | The historical double-contact example retains two genuine rich contacts, with no ordinary isolated transverse token. | Historical multiplicity |
| R1-U06 | Floating perturbation/splitting of a repeated root cannot produce the four spurious transverse eligible candidates. | Discovery/proof regression |
| R1-U07 | Close roots remain distinct if independently certified; no parameter/proximity merge. | Isolation |
| R1-U08 | Insufficient precision/enclosure remains explicitly unresolved rather than fabricated certified evidence. | Numerical negative |
| R1-U09 | A derivative enclosure containing zero fails the simple/transverse certificate. | Derivative gate |
| R1-U10 | Outward exclusion of zero removes the interval from verified-root publication. | Exclusion |
| R1-U11 | Work/depth exhaustion is deterministic and never upgrades incomplete proof. | Budget |
| R1-U12 | Native knot roots use actual structural continuity and canonical ownership without duplicate representation. | Knot |
| R1-U13 | Periodic seam roots respect structural seam evidence and canonical preimage ownership. | Periodic |
| R1-U14 | Supported R5 composition is enclosed in actual semantic composition order. | Transform |
| R1-U15 | Negative dilation retains truthful root/classification evidence without Cartesian identity or source-token reuse. | Transform negative |

## Execution and evidence rules

The focused verifier owns live required class/method inventories and exact
scenario membership, not annotation counts as proof. Runtime counts and hashes
are recorded only after execution. The final scenario JSON maps each ID to its
test methods or explicit static/manual authority; coverage must not be an
unexplained empty cell. Generated canonical summaries and logs remain ignored.

Required levels: PHASE A/B, COMPOSED and FULL `-CleanBuild`. New verifier/phase
registration is `VERIFICATION_INFRASTRUCTURE_IMPACT=UPDATE_REQUIRED`; no test
selection/cache/fork relaxation is authorized. Existing current-run receipts
may avoid duplicate module launches, never phase scientific/assertion work.
Report unique canonical executions separately from phase consumers.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: the bounded Java implementation uses
the existing supported JDK/Gradle/PowerShell toolchains and introduces no
external numerical library, generated source, installer/runtime dependency or
packaging prerequisite. Bootstrap still delegates to the same default COMPOSED
entrypoint; the new phase registry is validated by FULL rather than a new tool.
Revisit this conclusion if implementation adds a dependency or runtime change.

GUIDE_IMPACT = UPDATED. User/developer contracts must explain partial scope,
exact identity versus numerical proof, structural-slot recurrence and rich-only
cases. Final guide paths/evidence belong in the implementation candidate report.

## Author smoke and retained risk

Manual author smoke remains PENDING: one certified opposite-germ case, one
same-germ/mixed case, and the monodromic rich-only case through the existing R3
inspector, including regular motion and native save/reopen. No new GUI is built.

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` stays OPEN / TRACKED in the
[roadmap](../roadmap/geocedg_roadmap.md), with required disposition before global
G9 closeout. R1 pair-state archive coverage is a different acceptance gate.

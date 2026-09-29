# PRE-G9B-R3-X1 certified expression-point candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R3-X1, certified expression-point construction model extension
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (frozenAtPhaseStart = true)
PLANNED_ACCEPTANCE_LEVEL  = FINAL, one campaign on the exact candidate
IMPLEMENTATION_BASE       = 78134aa8bc6ac30b65ffd916713fd3ca0502e6c6 (tree f23218cc0b7c7189aae69489a1ce7a5ac5ee9fba)
BRANCH                    = feature/pre-g9b-r3-x1-certified-expression-points
CANDIDATE_COMMIT          = the commit that contains this artifact; not nameable inside it, reported separately
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a separate closeout/author-decision record is the sole
authority for current author approval status.

The phase executes the author's explicit `PRE-G9B-R3-X1` instruction of
2026-09-29. The [design record](../architecture/pre_g9b_r3_x1_certified_expression_point_design.md)
gives the grammar, normalization and mechanisms, the
[validation matrix](pre_g9b_r3_x1_validation_matrix.md) maps every obligation to
evidence, and the machine-readable evidence is
[`geocedg/validation/pre-g9b-r3-x1/pre-g9b-r3-x1-evidence.json`](../../geocedg/validation/pre-g9b-r3-x1/pre-g9b-r3-x1-evidence.json).

## 1. Entry and declarations

- **Entry identity.** `HEAD`, local `main`, `origin/main` and live remote `main` all
  resolved to `78134aa8…` (`PRE-G9B-R3-C1-U1 = PASS — AUTHOR APPROVED`), tree
  `f23218cc…`. The worktree and index were clean and `main` had no unpublished commit.
- **Branch.** `feature/pre-g9b-r3-x1-certified-expression-points`, created from
  `78134aa8…`; not pushed.
- **Class.** `GLOBAL_IMPACT` was frozen before any productive edit, with `FINAL` as
  the planned acceptance: shared Locus V2 kernel semantics, certified interval
  capability, pair admissibility and materialization, and evaluator corrections
  that change floating results.
- **Canonical prompt.** None is created, as for `PRE-G9B-R3-C1` and `C1-U1`:
  governance does not require one, `CLAUDE.md` forbids editing `.github/prompts/**`
  as a side effect, and the author instruction is the execution authority.
- **Normative work before code.** The design record, the amendment of the
  certified-construction specification (1.0 → 1.1), the ADR 0028 amendment and the
  one-clause pair-specification amendment were written before productive code, all
  inside the authorized envelope.

## 2. Characterization on the exact base

Scratch probes (session scratchpad, not in the tree) established:

| Case | Base result |
|---|---|
| X1-P1 `E=C+(1,0)` | `AlgoDependentPoint`, isolated definition `PLUS[GeoPoint C, MyVecNode(1,0)]`; no program; pair with the witness spline: 2 roots, 0 admissible, `NOT_ESTABLISHED`, ledger v4, `CAPABILITY_NOT_AVAILABLE … no certified interval curve model` |
| X1-P2 `u=(1,0)`, `E=C+u` | same boundary; definition `PLUS[GeoPoint C, GeoVector u]` |
| X1-CONTROL `Translate(C,u)` | `certified-construction-program/v1 … TRANSLATE(n0)[1,0]`, 2 admissible, ledger v5 |
| R3 form `Midpoint(D,C+(1,0))` | rich-only for the same reason |
| geometric comparison (validation only) | P1, P2 and the control evaluate to the same binary64 values at every probed parameter |
| debt A fixture `LocusV2(Midpoint(Cs,K),Cs)` on `(0,0)–(4,0)`, `K=(0,2)` | evaluates the previous parameter; `LocusLength(ls,Pa,Pb) = 2.25` (expected `1`), `Length(ls,Pa,Pb) = 1.5`, `LocusLength(ls) = 3.5` (expected `2`) |
| debt B fixture (two inline literals) | created undefined: `MALFORMED_RECORD` because the macro XML writes `Point(lower)` twice |

## 3. X1 design

- **Grammar (class v2).** Exactly one `P + V`, `V + P` or `P - V` of an
  `AlgoDependentPoint`, with `P` a driver-dependent `GeoPoint` and `V` a finite,
  driver-independent `GeoVector` or an inline Cartesian `MyVecNode` of two numeric
  literals (`MyDouble`/`MySpecialDouble`). `V + P` is included because real and
  binary64 addition are commutative, and `P - V` because `x - y` is exactly
  `x + (-y)`; both are the same bounded proof problem as `P + V`.
- **Representation.** One new operation, `EXPRESSION_TRANSLATE(ux, uy)`, with
  `u = V` or `-V`.
- **Normalization.** The step is the existing `TRANSLATE` formula applied to the
  unit reading `VEC(P) = (PX, PY, 1)` of `P`, which is exactly what
  `GeoPoint.getVector()`, `GeoVec2D.add/sub` and `AlgoDependentPoint`
  (`setCoords(x, y, 1)`) compute. No new interval formula exists.
- **Versioning.** A program is `certified-construction-program/v2` exactly when it
  has an `EXPRESSION_TRANSLATE` step; every other program keeps
  `certified-construction-program/v1` and its signature.
- **Rejected alternatives.** Admission by class; relabeling the expression as
  `TRANSLATE(P, V)` (not exact when `Pz ≠ 1`, and indistinguishable from v1);
  `MIDPOINT(P,P)` + `TRANSLATE`; a general expression interval evaluator; point +
  point; relabeling every program as v2; hiding evaluator defects in the certifier;
  fixing debt B in the generic `Macro` code.

## 4. Productive changes

| File | Owner | Change |
|---|---|---|
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/CertifiedConstructionProgram2D.java` | GeoCeDG kernel | `EXPRESSION_TRANSLATE`, `VERSION_V2`, `getVersion()`; signature prefix is the derived version |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/CertifiedConstructionCapture2D.java` | GeoCeDG kernel | the bounded §5.2 recognizer |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionIntervalModel2D.java` | GeoCeDG kernel | `EXPRESSION_TRANSLATE` evaluated by the extracted `TRANSLATE` formula on `VEC(P)` |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java` | GeoCeDG kernel | debt A: `updateCoords()` after `pathChanged`; debt B: strict total order of the slice set |

No certifier, resolver, selector, token, ledger, materialization, serialization,
schema, build, packaging, verifier or upstream-owned source changes. The pair
certifier consumes the newly available model unchanged.

## 5. Evidence

### 5.1 Expression points

`C+(1,0)`, `C+u`, `(1,0)+C`, `u+C`, `C-(1,0)`, `C-u`, `C+(0.5,-0.25)` and
`C+Vector((1,0))` capture a v2 program with one `EXPRESSION_TRANSLATE` step of the
expected vector. The control `Translate(C,u)` and the witness `a` keep their v1
signatures byte for byte, as captured on the base. Eleven out-of-grammar shapes,
including the E0/E1 fixture `(x(C)/2,y(C))`, still have no program.

### 5.2 Certified model

Enclosure of value and first derivative holds on 39 parameters per locus for eight
v2 forms, including a meet operand with non-unit homogeneous `z` and a segment
driver. A meet at infinity, a box outside the domain and an interval overflow are
refused (`ArithmeticException`, unresolved). The coherence gate still refuses a
certified `UNIQUE` slot when the floating verification rejects it.

### 5.3 Pairs and materialization

`Intersect(d, C+(1,0))` and `Intersect(d, C+u)` certify both germ classes (`UNIQUE`),
emit exact tokens (ledger v5) and materialize, with completeness still
`NOT_ESTABLISHED`. Caller reversal gives the same selectors and addresses. An
uncertified expression issues no pair token (ledger v4); a tangency is not
upgraded. A materialized point keeps its token, selector and identity under vector
motion, goes dormant when the model is lost and reactivates on the same slot. A
materialized root is a metric endpoint only through the existing pair-intersection
occurrence.

### 5.4 Lifecycle

The R3 witness redefine `E := Midpoint(D,C+(1,0))` keeps R3 compatible-retain
semantics (dependencies `{C}`, definition revision `+1`, topology revision
unchanged); the pair `e = Intersect(d,a)` stays certified under v2, and the point
`X` materialized from it before the redefine keeps its token and selector and
follows the redefined ellipse. Undo restores the v1 state, redo the X1 state, and
a fresh application reopening the saved document shows the same identities,
bindings and position. `g = Intersect(a,c)` and `I`…`L` are unaffected. Redefines
between supported vectors keep the slot; a redefine into `(x(C)/2+1,y(C))` makes the
pair rich-only with its diagnostic and the point dormant, and undo recovers it.
Copy/remap of literal and named-vector expression loci gives fresh identities, a
literal that stays a literal, a copied expression that references the copied
vector, a recaptured v2 program and a current copied point with its own token.

## 6. Evaluator debt disposition

```text
TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE     = RESOLVED_IN_X1
TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY = RESOLVED_IN_X1
PRE-G9B-R3-X2                                = NOT NEEDED
```

- **Segment driver.** Mechanism: `GeoSegment.pathChanged` writes through
  `setCoords2D` and relies on the `updateCoords()` that `AlgoPointOnPath.compute`
  performs; the evaluator omitted it, so the next reset refreshed the
  inhomogeneous coordinates to the previous parameter. Repair: the evaluator
  performs `pathChanged`, then `updateCoords`, then the cascade. Circle and arc
  drivers are unchanged bit for bit (checked against the live construction).
- **Inline literals.** Mechanism: the slice `TreeSet` used the natural order, which
  is not total (geos compare creation ids, algorithms construction indices; an
  inline literal is outside the construction list); `Macro.buildMacroXML` re-adds
  its closure (a G9 addition absent from upstream `9b93256b…`), which re-inserted
  the driver's algorithm and wrote `Point(lower)` twice. Repair: the evaluator's
  set uses construction index, then creation id. `Macro` is unchanged; classic
  `Locus` with the same literals was probed unaffected.
- **Ri conditions.** Both repairs lie in the evaluator whose slice X1 captures, do
  not widen the scientific purpose, make no new architectural decision, share the
  X1 `FINAL`, and correct the evaluator rather than the certifier.
- **Compatibility decision.** Floating values of the affected constructions change
  from wrong to correct; nothing is persisted, so there is no migration.

## 7. Tests

| Class | Tests | Candidate | Extracted base `78134aa8…` |
|---|---|---|---|
| `org.geocedg.common.kernel.locus.intersection.PreG9bR3X1CertifiedExpressionPointModelTest` | 9 | 9 pass | 2 pass (controls: v1 signatures, negative grammar), 7 fail |
| `org.geocedg.common.kernel.locus.PreG9bR3X1CaptureBoundaryTest` | 3 | 3 pass | 2 run, both fail; the version unit test needs the new API and is omitted |
| `org.geocedg.common.locus.PreG9bR3X1ExpressionPointPairMaterializationTest` | 9 | 9 pass | 2 pass (controls: uncertified expression, tangency), 7 fail |
| `org.geocedg.common.locus.PreG9bR3X1RedefineWitnessTest` | 4 | 4 pass | 1 pass (control: unsupported redefine), 3 fail |
| `org.geocedg.common.locus.PreG9bR3X1EvaluatorCorrectnessTest` | 3 | 3 pass | 3 fail |
| `E1MODEL#theInlineLiteralShapeIsReplayedWithItsDriverAndCertified` (converted) | 1 | pass | fails |
| `E1PAIR#segmentDriverInhomogeneousReadIsCurrentAndCertified` (converted) | 1 | pass | fails |

The other 26 methods of the two E0/E1 classes are unchanged and pass on both trees.
The two E0/E1 debt pins were converted in place on their retained fixtures: each
pinned a debt that is now repaired, not the capability boundary, which stays pinned
by `E1MODEL#captureRefusesEveryShapeOutsideClassV1`,
`E1PAIR#slicesOutsideClassV1StayRichOnlyWithDiagnostic` and the X1 negative grammar.
The refusal branch of the coherence gate that the segment pin used to reach is
pinned by `MODEL#theFloatingVerificationStillRefusesACertifiedSlotItRejects`.

## 8. Compatibility

```text
SERIALIZATION_FORMAT_IMPACT = NONE
IDENTITY_SCHEMA_IMPACT      = NONE
MIGRATION_IMPACT            = NONE
CERTIFICATE PERSISTENCE     = NONE (programs are recaptured; no .cedg field)
V1 PROGRAMS                 = version and signature unchanged
HISTORICAL DOCUMENTS        = open with their serialized semantics; a newer build may certify
                              more current constructions at run time, and the two repaired
                              evaluator defects now evaluate correctly on load
```

## 9. Verification

### Local evidence before commit

| Check | Method | Result |
|---|---|---|
| focal tests | the classes of section 7 | 28 new tests and the two E0/E1 classes (28) pass |
| base causality | the same tests on `git archive 78134aa8…` (new-API references mapped) | 55 run, 24 fail, the controls pass (section 7) |
| full shared suite | `gradlew :shared:common-jre:test` | 686 JUnit files, 6 842 tests, 0 failed or errored, 10 skips |
| full Desktop suite | `gradlew :desktop:desktop:test` | 103 JUnit files, 1 533 tests, 0 failed or errored, 1 skip |
| discovery | official producer, `discovery.shared` and `discovery.desktop` with `-TestDryRun` | 5 925 and 1 533 identities |
| Checkstyle | `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings |
| verification-core tests reading the registry or inventory | final-coverage, registry, contract-boundary, cli-compatibility, gradle-producer, supervisor, runtime | all exit 0 |
| upstream boundary | `Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256b…` | passes with 812 registered files |
| whitespace | `git diff --check` | clean |

### Coupled reconciliations

- **JUnit inventory**, regenerated with the official updater from dry-run discovery:
  `discovery.shared` 5 897 → 5 925; `discovery.desktop` 1 533 (identities unchanged).
- **Typed registry.** The `junit_inventory` pin moves from `acca0eef…` to
  `9fa207f0011e079cadf8bc5e4df9a3c4e4a5a378b8c18a8ca955b8d3b804b74e` by canonical-text
  SHA-256; the method first reproduced `acca0eef…` from the unchanged inventory.
- **Upstream modification record.** Six purposes extended and five test files
  registered; 812 files.

### Acceptance

The frozen campaign is one `FINAL` on the committed candidate with a clean worktree:

```powershell
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r3-x1-final
```

`FINAL -PlanOnly` runs first on the same commit. The console log goes outside
`artifacts/`. Both results bind the candidate commit and are reported separately,
because it cannot be named here.

## 10. Documentation

- **User guides (both editions).** The R3 redefine example now states that the
  pair roots stay admissible; §8.7 lists the bounded expression translation
  (`P+v`, `v+P`, `P-v` with a vector object or a two-number literal) and names
  unsupported expressions; the known limitation describing the two evaluator
  defects is replaced by the remaining expression limitation.
- **Developer guide.** One paragraph on class v2 and the evaluator obligations.
- **Normative.** Certified-construction specification 1.1, ADR 0028 amendment,
  pair specification §10 clause.

## 11. Ri quality-debt ledger

| Identifier | State after `PRE-G9B-R3-X1` |
|---|---|
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | **resolved by this candidate** (pending author approval) |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | **resolved by this candidate** under Ri (pending author approval) |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | **resolved by this candidate** under Ri (pending author approval) |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; no overlap with X1; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN`, `OBS-R2-PHASE-SELECTION-COHORT`, `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE`, `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY`, `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | observations retained |

`TD-UPSTREAM-RECORD-E0-E1-PURPOSE` was already resolved in `PRE-G9B-R3` and is not
reopened. New observation, not a debt: `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER`
— `Macro.buildMacroXML` re-adds its closure into the caller's set and is correct
only when that set's order is a strict total order; the evaluator now supplies one,
the other callers (user tools, classic `Locus`) were not observed to fail, and no
generic change is made.

## 12. Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED CAPABILITY EXTENSION (certified-construction-program/v2)
                                     plus two evaluator correctness repairs
SERIALIZATION_FORMAT_IMPACT        = NONE
IDENTITY_SCHEMA_IMPACT             = NONE
MIGRATION_IMPACT                   = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY (JUnit inventory regenerated, registry catalog pin); no verifier code or methodology change
BOOTSTRAP_IMPACT                   = NO_CHANGE_REQUIRED (no workstation prerequisite, toolchain, Gradle, Conda, packaging or environment contract changes)
GUIDE_IMPACT                       = UPDATED (docs/user/geocedg_user_guide_en.md, docs/user/geocedg_user_guide_es.md, docs/developer/geocedg_developer_guide.md)
UPSTREAM_BOUNDARY_IMPACT           = no upstream-owned file modified; six GeoCeDG entries extended, five GeoCeDG test files added; record updated (812)
```

## 13. Suggested author smoke

1. `A=(0.78,2.24)`, `B=(2.56,4.78)`, `c=Circle(A,B)`, `C=Point(c)`, `E=C+(1,0)`,
   `a=LocusV2(E,C)`, a Spline V2 crossing it, `Intersect(spline,a)`: two certified
   roots; **Create all eligible** materializes both.
2. The same with `u=(1,0)` and `E=C+u`; moving `u` moves the curve and the
   materialized points.
3. The control `Translate(C,u)`: unchanged behaviour.
4. An unsupported expression such as `(x(C)/2,y(C))`: roots listed, rich-only, the
   diagnostic says a side lacks the certified model.
5. The R3 witness: materialize a root of `Intersect(d,a)`, then redefine
   `E=Midpoint(D,C+(1,0))` and keep identity; the point stays defined and follows.
6. Undo and redo that redefine; save natively and reopen.
7. Segment driver: `sa=Segment((0,0),(4,0))`, `Cs=Point(sa)`, `K=(0,2)`,
   `ls=LocusV2(Midpoint(Cs,K),Cs)`, `Pa`/`Pb` at `0.25`/`0.75`:
   `LocusLength(ls,Pa,Pb)` is `1`.
8. Inline literals: `Cj` on an arc, `gj=Line(Cj,(0,3))`,
   `gp=PerpendicularLine((0,0),gj)`, `Ej=Intersect(gj,gp)`, `LocusV2(Ej,Cj)`: a
   defined curve, not a single point.

## 14. Authorization state

The author instruction authorizes this phase's design and implementation and one
`FINAL` campaign. It does not authorize `PRE-G9B-R3-X2`, `R4` or any later phase,
arbitrary expression-point certification, a change of serialization or identity,
or release and publication. Push, merge, promotion, tag and release each require a
separate explicit author instruction naming the exact candidate SHA. The phase
stops for author review.

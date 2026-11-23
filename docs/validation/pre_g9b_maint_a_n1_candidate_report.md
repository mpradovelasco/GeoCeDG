# PRE-G9B-MAINT-A-N1 — characterization and candidate report

| Field | Value |
|---|---|
| Status | **NORMATIVE DESIGN CANDIDATE — PENDING AUTHOR REVIEW.** `selfApproved=false`, `authorApproved=false`, `passClaimed=false` |
| Frozen-artifact state | `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. The author's later decision record is the only authority for approval status |
| Authorization | author instruction «GeoCeDG — PRE-G9B-MAINT-A-N1. Autorización de diseño normativo — Familia B-spline preparada para NURBS e interoperabilidad DXF» (2026-11-23); [task prompt](../../.github/prompts/tasks/pre-g9b-maint-a-n1-bspline-nurbs-normative-design.prompt.md) |
| Base | `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, tree `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` |
| Branch and worktree | `phase/pre-g9b-maint-a-n1`, worktree `GeoCeDG-wt-maint-a-n1` (sibling of the main repository) |
| Prompt commit | `8fca7c6165f20eccec7d356e783d0670df241043` (tree `80fba09ed626db668ca080b374230f6dc21f4061`) |
| Verification class | `DOCUMENTATION_STATUS_ONLY`; `STATIC` on the documentary commit; its evidence is reported outside this frozen artifact |
| Candidate contracts | [family `bspline-2d`](../../geocedg/specs/curves/bspline-2d.md); [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md); [import `dxf-spline-reference/v1`](../../geocedg/specs/import/dxf-spline-reference.md); [validation matrix](pre_g9b_maint_a_n1_validation_matrix.md) |

All facts below come from read-only inspection of the base; nothing was built, run or
modified. Paths are relative to `source/shared/common/src/main/java/` unless stated
otherwise; `K/` abbreviates `org/geocedg/common/kernel/`.

## 0. Preconditions checked

- Worktree created from the exact base after `git worktree list --porcelain`; branch and
  path were free; base SHA and tree verified; the new worktree was clean at the base.
- ADR inventory: the highest number in `docs/adr/` is 0042. No local or remote ref contains a
  file `docs/adr/0043-*` or later, and no document references `ADR 0043`. The U2 worktree
  had no commit beyond the base and no uncommitted change at inspection time; no N1
  worktree existed. Number **0043** is used.
- No collision of branch, path or shared file was found.

## 1. Locus V2 consumers: evaluator interfaces versus `PiecewisePolynomialLocus2D`

`PiecewisePolynomialLocus2D` publishes coefficients in descending powers of the **global**
provider-canonical parameter; its javadoc says they may be rounded expansions and support
floating discovery only (`K/locus/PiecewisePolynomialLocus2D.java:8-16`). The interface has
no degree limit and no conditioning check.

| Consumer | Needs | Without it | Certified tier |
|---|---|---|---|
| `PolynomialTargetIntersectionCapability2D` | piecewise-polynomial source; re-expands global coefficients around each span start; at most 64 spans (`:435`), composed degree ≤ 96 (`:46`) | the adaptive evaluator capability answers | only through `SplineImplicitIntervalCertification2D`, gated on `SplineSemanticEvaluator2D` (`:126`) |
| `PiecewisePolynomialPairIntersectionCapability2D` | both sources piecewise-polynomial; ≤ 33 coefficients per coordinate (`:47`); local power and Bernstein conversions built from global coefficients | evaluator pair capability | rich-only without a `CertifiedIntervalCurveModel2D` |
| `CertifiedIntervalCurveModel2D.capture` | concrete `SplineSemanticEvaluator2D` (`K/locus/intersection/CertifiedIntervalCurveModel2D.java:57`) or a reconstructible evaluator | returns no model | gates certified pairs, area disjointness, local continuation evidence |
| `PolicyV2LocusMetricCapability2D` (the metric capability registered in production) | `LocusDifferentialEvaluator2D`; knot breakpoints through `LocusParameterPartition2D` | evaluator-only estimate | class 1 needs an affine or reconstructible source; class 2 needs a certified enclosure; a new family gets class 3 |
| `LocusSimilarityEvaluator2D` | forwards whatever the source exposes; composition depth + 1 | each capability declines | — |
| `LocusPointInteractionResolver2D` | affine, then piecewise-polynomial (global coefficients used directly on the span), then evaluator search | evaluator search | floating |
| `LocusPointContinuation2D` | piecewise-polynomial path, else a chart that needs a certified model | typed refusal or forward continuation | — |
| Render (`DrawLocusV2`, `LocusRenderCache2D`, tessellator) | evaluator, boundary enclosure, existence structure | inherited | — |
| `SemanticCurveExportAdapter2D` | evaluator and certified components | inherited | DXF area exclusion needs certified disjointness |

Conclusions used by the design:

1. Rendering, approximate export, existence and the class-3 metric need only the
   evaluators; a rational evaluator would inherit them without a kernel interface change.
2. One-sided polynomial intersections, pairs and the polynomial point-interaction path need
   the piecewise-polynomial publication; a rational curve cannot implement it honestly.
3. Every certified tier is gated on the final class `SplineSemanticEvaluator2D`; the new
   family inherits none (`MAINT-A4`).
4. The global basis is a real conditioning hazard for the user parameter `u`; consumers
   already build local bases from it. This decides §11.3 of the family specification.
5. No de Boor, Cox–de Boor, NURBS, blossoming or DXF reader exists in production code. The
   only other Bernstein code is inherited implicit-curve plotting code, unrelated to
   Locus V2.

## 2. Command XML persistence

- Writer: `AlgoElement.getXML` is final and calls the private `getCmdXML`
  (`org/geogebra/common/kernel/algos/AlgoElement.java:1456`); `<command>` can carry only
  `name`, `type` and `var`; the only protected hook is `getCmdOutputXML` (`:1589`).
  `AlgoSplineV2` adds nothing; its contract strings are runtime-only.
- Reader (`org/geogebra/common/io/MyXMLHandler.java`): unknown attributes of `<command>` are
  ignored silently; an unknown child of `<command>` only logs `unknown tag in <command>`
  (`:3830-3840`); every attribute of `<input>` is an argument and every attribute of
  `<output>` but one is a label; the command executes when `<output>` is read.
- Unknown command name: `CommandNotFoundError`, caught and collected as a load error; the
  `locusv2` output element becomes a numeric placeholder; the native open route does not
  treat collected errors as fatal.
- Fail-closed precedents: `geocedgSpatial` (identity), `geocedgUnits`,
  `geocedgHiddenLayers`, control layout, `authorityPolicy` with exact `/v1` identifiers. In
  `MyXMLio.doParseXML` their typed exceptions are rethrown with restoration of the previous
  construction (`org/geogebra/common/io/MyXMLio.java:520-542`).
- Undo snapshots, copy/paste and redefinition all reuse the construction XML writer and
  reader; redefinition treats reader errors as a hard failure.

Consequence for the design: a dedicated child element of `<command name="BSpline">` read by
a GeoCeDG codec that throws a typed exception is the only mechanism that is serialized in
every route (save, Undo, clipboard, redefinition) and fails closed with a typed cause.
`MAINT-A2` must verify that the exception thrown while reading that element is not
swallowed by the command-execution error collection, and that rollback applies.

### 2.1 Correction to the P0 proposal

The P0 proposal §6.3 expected that documents with `BSpline` would fail in 1.2.2 as an
«unknown command». The code does not behave that way: an unknown command gives a lenient
partial load. A native document fails to open only because the output's identity record
(family `LOCUS_V2`) disagrees with the placeholder
(`K/spatial/identity/SpatialIdentityRegistry.java:6735-6741`, `MALFORMED_RECORD`), possibly
earlier as `GEO_NOT_SERIALIZABLE`; which fires first is not established and is BS-22. The
family specification §8.4 records the corrected behavior.

## 3. Native `.cedg` container and the `R-c` precedents

- Writing: `AppGeoCeDG.saveNativeDocument` refuses non-`.cedg` targets; the archive holds
  construction images, thumbnail, optional macros, JavaScript, defaults, the optional
  `geocedg_defaults_v2.xml` and `geogebra.xml`; the written bytes are re-read before the file
  is replaced.
- Reading: a disposable preflight validates the archive; the open then captures the current
  state and restores it on a thrown failure.
- **Unknown archive entry**: treated as a possible image; failure is only logged; the entry
  is **not re-saved** (only referenced images are written). An `R-c` copy stored as a new
  archive entry would therefore be lost silently when the document is re-saved by a build
  that does not know it.
- **Document-level block outside `Construction`**: the only precedent is
  `<geocedgHiddenLayers>`, a child of `<geogebra>` in `geogebra.xml`, written only for the
  full document (not Undo, clipboard or preferences), fail-closed on a newer version, and
  dropped on re-save by older readers. Units are deliberately inside `<construction>` so that
  they travel with Undo and redefinition.
- Native Undo rejects any extra archive entry; the Undo snapshot holds `geogebra.xml` only.

These facts feed the `R-c` characterization list of the import contract §12; they do not
decide `R-c`.

## 4. Technical decisions adopted in the candidates

| Decision | Where | Justification |
|---|---|---|
| New family `bspline-2d`, `GeoLocusV2` provider, user knot parameter, right-owned knots | spec §1–§4, ADR 1–3 | D-A-1, D-A-2; shared ownership rule with SplineV2 |
| `v1` limits `1 ≤ p ≤ 7`, `p + 1 ≤ n + 1 ≤ 128`, clamped, interior multiplicity `≤ p`, exact `C^0` closure | spec §4 | D-A-3; closure only by exact equality, as in SplineV2 |
| No silent degree reduction | spec §4.2 | AGENTS §1.8; a default must not change the object's degree |
| Exact closure published as a closed domain with a `C^0` seam owned by the first span | spec §4.3 | avoids duplicate roots at the seam; publishes no smoothness that is not there |
| Structural continuity exact; regularity and `G^1` not claimed | spec §5–§6 | the knot structure decides parametric continuity; control points decide regularity |
| Local per-span polynomial authority, additive optional extension, residual fail-closed rule | spec §11.3, ADR 6 | §1 above: global basis ill-conditioned with `u`; fail-closed alone would decline ordinary curves |
| `<geocedgBSpline>` child element with a fail-closed codec | spec §8, ADR 7 | §2 above: the only route-complete, fail-closed mechanism; identity layer untouched |
| Approximation provenance as a passive child of the family element, applicability by definition digest | spec §10.5, ADR 11 | D-A-10: travels with copy, Undo and persistence; degrades the guarantee, not the object |
| Pure library `org.geocedg.common.math.bspline` with rational evaluation | spec §12, ADR 9 | shared by A2, A-DXF0, A-DXF1; rational evaluation needed to draw imported references, without a rational kernel interface |
| Import admission predicates, decimal rule, no implicit scaling, explicit affine transform | import §4–§6 | D-A-6; unit system rule that numbers are never rescaled implicitly |
| `R-a` lifecycle outside Undo and save | import §11 | D-A-7 |
| Contract location `geocedg/specs/import/` | import header | parallel to `geocedg/specs/export/`; no existing import directory |

## 5. Questions that need author approval

| Id | Question | Candidate proposal |
|---|---|---|
| Q-BS-1 | Conditioning approach | local per-span representation with additive extension and residual fail-closed rule |
| Q-BS-2 | Curves with more than 64 spans | keep `n + 1 ≤ 128`, adaptive route above 64 spans |
| Q-BS-3 | `κ_max` | `2^16` |
| Q-BS-4 | Persistence mechanism | `<geocedgBSpline>` child of `<command>` |
| Q-BS-5 | Library package | `org.geocedg.common.math.bspline` |
| Q-BS-6 | Approximation provenance location | passive child of the family element |
| Q-DXF-1 | Import limits | 64 MiB, 10 000 entities, 1 024 control points, weight ratio `10^6`, render tolerance `10^-4` relative |
| Q-DXF-2 | Flag/weight incoherence | reject both directions |
| Q-DXF-3 | Picture exports with visible references | reproduce the view as presentation; semantic exports exclude references |
| Q-DXF-4 | Contract location | `geocedg/specs/import/` |
| Q-AN1-1 | Approval granularity | the family specification, the ADR and the import contract are approvable separately; the import contract depends on the family specification only through §12 (library) and §10.5 (provenance) |

## 6. Effective dependencies

- **`MAINT-A2`** depends on the author's acceptance of the family specification and of
  ADR 0043 (at least decisions 1–9 and 11). It needs: the pure library (it creates it if
  `MAINT-A-DXF0` has not); the additive local-coefficient extension and shared accessor;
  the additive command-XML writer hook and the fail-closed codec; the product wiring
  (command, canonical header, translations, R5-B fingerprint regeneration); BS-1 to BS-24
  with SplineV2 non-regression. It does not depend on `MAINT-A-DXF0`.
- **`MAINT-A-DXF0`** depends on the author's acceptance of the import contract and of the
  library API of the family specification §12. It does not depend on `MAINT-A2`, the
  kernel family or any Locus V2 change, and needs no change to the document format.
- Coordination: whichever of the two is authorized first creates the library; if both are
  authorized together, `MAINT-A-DXF0` creates it in a first commit that `MAINT-A2` takes as
  base (proposal §11).
- **`MAINT-A-DXF1`** needs A2, A-DXF0 and its own approximation contract (grammar of the
  provenance block, method, limits, the three measures and the certified-bound procedure
  with directed rounding).

## 7. Open compatibility and persistence risks

| Risk | State |
|---|---|
| Released GeoCeDG versions refuse `BSpline` documents without a cause naming the family | accepted behavior to declare in release notes; BS-22 fixes the observed exception |
| The typed exception of the family codec could be swallowed by command-error collection | `MAINT-A2` must prove fail-closed behavior and rollback (BS-21) |
| `R-c` as an archive entry would be dropped silently on re-save by builds that do not know it; as a `<geogebra>` child it is dropped by older readers too | to be decided by the `R-c` characterization; not part of `v1` |
| Generic consumers might assume seam smoothness for a closed domain | `MAINT-A2` characterization item and stop condition |
| More than 64 spans fall back to the adaptive intersection route | declared limitation (Q-BS-2) |
| No certified tier for the family | declared in published guarantees; `MAINT-A4` |
| Global coefficients still published for interface conformance | inventory test in `MAINT-A2`; residual fail-closed rule |
| Stale status headers in SplineV2-related documents (P0 report §A.6) | untouched; separate documentary task |

## 8. Scope respected

No Java, Desktop, resource, test, verification tool, roadmap, approved specification, ADR,
SplineV2 contract, DXF export contract, governance file or other worktree was changed. The
only governance-layer file added is the task prompt, in its own commit, as authorized.

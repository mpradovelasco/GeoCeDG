# PRE-G9B-R6-plus-C preparation package closeout and authorization record

```text
C PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-C     = AUTHORIZED FOR IMPLEMENTATION
```

```text
selfApproved              = false
authorApproved            = true    (C documentary preparation package and its
                                     reconciliation only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = true    (C implementation and technical verification only;
                                     E1, E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B: false)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE    (this record)
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author instruction of 2026-10-04, confirmed
by the author in writing in the session, that approves the documentary
preparation package of `PRE-G9B-R6-plus-C` and its reconciliation, gives the
author dispositions on `DQ-C1` to `DQ-C17`, disposes of
`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`, freezes the verification class and
authorizes the implementation of `C`. The package is not a phase: approving it
closes the preparation, not `C`. This record changes no product, test, build,
packaging, verifier, registry, schema, specification, ADR or serialization
file, and it does not modify either frozen preparation candidate. Its
machine-readable mirror is
[`pre-g9b-r6-plus-c-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-prompt-closeout.json).

## Author disposition: C preparation package

```text
T_R6PLUS_C_PROMPT  = 5ad96304ae9718c296db753b4822130cf3d99b61
                     tree   1596b5153681828c9c3206b98ca9ebd31505e765
                     parent e613502831e3b412780d69424d4a1e433a4ae688  (P_R6PLUS_A2)
                     prompt blob f798eab867608ba4eb824f7a0d8fb925fbbbff2e
D_R6PLUS_C_PROMPT  = 6cb09d55e1ce9b18ef046f445da5494288fd8ecd
                     tree   00f80cc75db6de6542bb295cef813e70c9823b9c
                     parent 5ad96304ae9718c296db753b4822130cf3d99b61
                     prompt blob 514dee31a64b55e648ed5a9c374ceb1dc1b28b1c
                     branch phase/pre-g9b-r6-plus-c-prompt (local; never pushed)
AUTHOR_APPROVAL    = C PREPARATION PACKAGE = PASS — AUTHOR APPROVED,
                     subject to the DQ-C dispositions below
selfApproved       = false
```

The approved package is the canonical prompt
[`pre-g9b-r6-plus-c-2d-export-completion.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md)
as reconciled (blob `514dee31a64b55e648ed5a9c374ceb1dc1b28b1c`), the
[C author-decision record](pre_g9b_r6_plus_c_author_decisions_record.md), the
[C preparation characterization report](pre_g9b_r6_plus_c_preparation_characterization_report.md),
their mirrors, and the roadmap 4.58/4.59 and mini-track plan updates. Accepted
evidence: `STATIC` `verification-8b483c2f07f14b1f9e18524d9d772207` on
`T_R6PLUS_C_PROMPT` and `STATIC` `verification-b15f841a132c4be2bc6586d997363ebf`
on `D_R6PLUS_C_PROMPT`, both exit 0, `ACCEPTED / COMPLETE`, 3/3, plan hash
`25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result hashes
`66f118814ed833e71a393ec207fdea311925a9095bc9c294dac5703b6ebb9887` and
`7c9544b93f16b0f126382d3185904fec4108277b487940c7874f4b33f184e97b`, with the two
standing diagnostics; `Test-PromptContractDocument` (`task` profile): seven
fields in order, `execution_safe = true`.

Neither frozen preparation candidate is amended. Where the reconciled prompt
states a default or a pending question for `DQ-C1` to `DQ-C17`, **this record
prevails**. The instruction requires, as the first tracked edit of the phase,
the amendment of the prompt to `AUTHORIZED FOR IMPLEMENTATION` that records these
dispositions and replaces the pending and default language they supersede.

## Authorization

```text
PRE-G9B-R6-plus-C     = AUTHORIZED FOR IMPLEMENTATION
PUBLISHED_BASE        = e613502831e3b412780d69424d4a1e433a4ae688   (P_R6PLUS_A2;
                        tree 48b00681a727ea4bd8768c3e9b0159bc01573ff0)
BRANCH_START          = 6cb09d55e1ce9b18ef046f445da5494288fd8ecd   (D_R6PLUS_C_PROMPT;
                        tree 00f80cc75db6de6542bb295cef813e70c9823b9c; not published)
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-c-2d-export-completion (local; no rebase)
VERIFICATION_CLASS    = INTEGRATED_PHASE     (frozen)
PLANNED_ACCEPTANCE    = registered PHASE PRE-G9B-R6-PLUS-C + INTEGRATION, both
                        ACCEPTED / COMPLETE on the same exact clean candidate and tree,
                        with fresh log directories; no FINAL unless a stop condition
                        forces reclassification
STOP_STATE            = PRE-G9B-R6-plus-C = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                        selfApproved = false, authorApproved = false, passClaimed = false
```

Entry precondition: the live remote `main` is still the published `A-2`
closeout, unless history has legitimately advanced; if it changed, the phase
stops and reconciles instead of rebasing. If tracked source, test or
specification files change after either acceptance run, that evidence no longer
certifies the tree: the phase reconciles and reruns the required acceptance on
the final candidate. The agent does not perform or claim author smoke.

Not authorized: `E1`, `E2`, `E3`, `F1`, `F2`, `F3` implementation, `G`,
`PRE-G9B-R7`, `G9B`, `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`,
`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`; push, publication,
merge, rebase, squash, amend of either frozen preparation candidate, tag and
release.

## Author dispositions on `DQ-C1` to `DQ-C17`

| ID | Outcome | Disposition |
|---|---|---|
| `DQ-C1` | accepted | No exact Bézier special case in `C`, including degree ≤ 3. `SplineV2` uses the same certified semantic approximation and export contract as `LocusV2`. The rounded polynomial and evaluation data are not promoted to exact coefficient authority. No generated output may claim exactness only because the degree is ≤ 3. |
| `DQ-C2` | accepted | Objects on a persistently hidden GeoCeDG layer stay in DXF with their source layer membership; the corresponding DXF `LAYER` is emitted OFF through the layer-table mechanism. Their geometry is never removed only because the layer is hidden. The `A-2` hidden set participates in the DXF layer-table state, the report and evidence, and the staleness fingerprint. Presentation and export state, never geometric state. An object on a hidden layer that is also individually hidden carries both mechanisms truthfully. |
| `DQ-C3` | accepted | The per-entity `60 = 1` behavior for individually hidden objects is preserved. Object visibility is never reinterpreted as hidden-layer state. Stale profile or specification wording such as "visible geometry" is reconciled to describe the exported population truthfully. Show/Hide object semantics unchanged. |
| `DQ-C4` | accepted | For PSTricks, PGF/TikZ and Asymptote: tolerance explicitly in model coordinates; default `0.001`; an export-request/session input, never document state; unaffected by `constructionUnit`, `presentationUnit` or `drawingScale`; validated positive finite; the approximation machinery and deterministic work limits are those of the approved shared adapter and G9X1 foundation. Each LaTeX dialog may expose the same input. No silent conversion to a physical-paper tolerance. |
| `DQ-C5` | accepted as reconciled | A locally certified, valid, deterministic and unambiguous semantic branch component may be exported when global completeness is `NOT_ESTABLISHED` or the source or result is incomplete; this never implies global completeness. Export-layer classification equivalent to `COMPLETE`, `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, `NO_ADMISSIBLE_OUTPUT` and whole-export `REJECTED` where required. Source, branch and component identity preserved; every component emitted independently; no join across a gap or discontinuity; no interpolation over an invalid interval; no identity from coordinates, order or proximity; evidence of incomplete coverage preserved. `MISSING_DOMAIN`, unresolved discontinuity, degeneracy and work-limit failures affect the component or source and can yield an incomplete export when other certified output remains. `STALE_SOURCE_REVISION`, an unestablishable requested guarantee or tolerance, or no admissible output at all reject the applicable export without pretending success. LaTeX does not mechanically inherit G9X1's strict partial-output rule. DXF keeps the accepted strict G9X1 fidelity contract. |
| `DQ-C6` | accepted | `drawingScale = a:b`, positive integers, normalized by `gcd(a, b)`, default `1:1`; a bounded integer implementation compatible with UI and runtime, with explicit rejection of invalid or overflowing input and no silent clamp; normal-form-equivalent pairs have identical semantics; presets such as `1:1`, `1:2`, `1:5`, `1:10`, `2:1`, `5:1` without restricting free valid entry. A valid pair whose output a writer cannot represent finitely and safely fails that export explicitly, never modifying geometry or scale. |
| `DQ-C7` | accepted as reconciled | Owner: GeoCeDG application/window export-session state, one authoritative value shared by all applicable `C` exporters; default `1:1`; never serialized, never in `Construction`, never undo state. Resets only after a successful effective document transition: `E1` completed File → New at the successful end of `AppGeoCeDG.fileNew` after automatic initialization; `E2` successful native Open through the committed native-document seam; `E3` successful Base64, HTML or non-native-stream replacement through `documentReplacementCommitted`; `E4` successful full-document replacement through the characterized `runDocumentLoad`/`loadXML(String)` lifecycle; `E5` public API `setXML` only when it performs a complete document replacement, through the narrowest GeoCeDG-specific marker in `GgbAPID` (an arbitrary `setXML(…, true)` is never a document transition); `E6` `GgbAPI.reset()`: reloading an existing current file relies on the normal successful load commit without a second semantic reset; with no current file, a successful reset to a blank document resets to `1:1` at the completed reset boundary. No reset merely because `clearConstruction` ran. Never reset for undo, redo, redefine, rollback, preference reload, macro or tool processing, `.ggt`, tool overwrite or replacement, `evalXML`, merge, paste, Insert File, Apply Template or temporary construction clearing. The `ExportArea` and paste-notice reset behavior is not changed by `C`. |
| `DQ-C8` | accepted with precision | Physical construction unit: the GUI engineering sizing authority is `drawingScale = a:b`; physical output size derives solely from `constructionUnit + drawingScale`; DPI stays raster resolution; displayed raster pixel dimensions derive from physical size and DPI and are not a second physical-scale authority; no "fixed physical size" control competing with `drawingScale`; `presentationUnit` does not affect physical size. `UNSPECIFIED_MODEL_UNIT`: no `a:b` claim; no inferred cm; legacy/device-scale controls may stay where existing functionality requires them, labelled explicitly as non-physical/device output; device parameters never presented as model-unit semantics. |
| `DQ-C9` | accepted | The EMF physical-frame defect is corrected in `C`. A FreeHEP modification is authorized only with an explicit GeoCeDG opt-in; default FreeHEP and Classic behavior unchanged; no wider redesign; `rclFrame` from the requested physical output size, never from DPI; DPI never a physical-size authority; conversion to EMF's 0.01 mm unit rounded to the nearest representable value, unavoidable quantization ≤ 0.005 mm per dimension; physical metadata tested independently of device raster and path coordinates. This limited change keeps `C` `INTEGRATED_PHASE`. Stop if the correction requires a global FreeHEP semantic change affecting Classic or default behavior. |
| `DQ-C10` | accepted | `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT = NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`: the Save-dialog preview exists only for an existing target in a sufficiently wide chooser, and the Picture dialog has no preview; this predates `B`/`A-2`. No new preview surface in `C`. The hidden-layer preview-content observation stays distinct. |
| `DQ-C11` | accepted | The controlled sidecar contract and schema evolve explicitly and deterministically to carry unit information; old sidecars are never silently reinterpreted. Built-in physical units: `$INSUNITS` carries the native code and any sidecar reports coherent unit metadata. `usm`: `$INSUNITS = 0`, a visible preflight/custom-unit warning, a mandatory paired sidecar recording at least the token `usm`, the canonical binary64 `metersPerUnit` and versioned unit metadata; coordinates numerically unchanged. `UNSPECIFIED_MODEL_UNIT`: `$INSUNITS = 0`, neutral model `UNITLESS`, no invented factor. |
| `DQ-C12` | accepted | PSTricks, PGF/TikZ and Asymptote output carries deterministic comments exposing the overall export classification, the requested tolerance, the approximation status, per-source/component classification and evidence, and incomplete coverage, addressed by semantic source, branch and component (labels only as presentation metadata). The dialog shows a visible non-modal report with the complete/incomplete/rejected status, approximate components, omitted or failed components with reasons, and tolerance and work-limit information. Comments are never parsed back into geometry. |
| `DQ-C13` | **B1 chosen** | DXF consumes `B`'s authoritative `ExportArea` through semantic participation filtering, never geometric clipping, and never changes entity families to crop. Explicit producers `MANUAL` and `EXPORT_POINTS_*` define the finite participation area; `VISIBLE_VIEWPORT` is no explicit DXF boundary (zoom never geometric authority; G5 zoom invariance kept), stated and tested explicitly. A source, entity or component participates when its exported geometric support intersects the closed explicit area, and is then emitted whole, for complete-construction and current-selection export alike (selection chooses the candidates; the area applies the same rule). Exact analytic predicates per entity: point inside or on the rectangle; segment, polyline and polygon boundary; infinite line and ray; circle, arc and ellipse curves; a boundary that encloses the rectangle without entering it does not participate (no filled-region substitution). Locus V2/Spline V2 components: never excluded from raw samples or render tessellation; `OUTSIDE_EXPORT_AREA` only when the approximation and error evidence proves disjointness through a conservative certified test; otherwise the complete certified component participates; no false-negative omission because the polyline misses the rectangle. `OUTSIDE_EXPORT_AREA` is intentional population filtering, never unsupported or invalid; the area and source are recorded in preflight and report, deterministic export metadata or comment, the sidecar when one exists, and the staleness fingerprint. A sidecar is not made mandatory only because an explicit area filters exact sources. B2 (bounding box) and B3 (wholly inside) are rejected as final semantic authority; A and C are rejected in `C`. The specification states that DXF consumes the area through source/component participation, not rectangular clipping. |
| `DQ-C14` | accepted | The five-significant-digit PDF/SVG path-coordinate formatting is a documented writer limitation; FreeHEP is not modified only to increase it. Path precision, exact page/frame dimensions, `ExportArea` bounds and the physical size contract stay separate; tests prove the limitation does not corrupt page/frame sizing. |
| `DQ-C15` | accepted | Only the live specification and document pins and evidence contracts that `C` supersedes are updated. Frozen historical G9X1 evidence is not rewritten. Where the historical verifier legitimately pins a living specification, it is repinned coherently per D0 §15.3, preserving the historical evidence identity and describing the supersession. The unitless G5/G9X1 corpus stays a required byte and semantic compatibility baseline. |
| `DQ-C16` | accepted with precision | Without an explicit device/render scaling argument, GeoCeDG export routes consume the physical unit contract and the current `drawingScale`. Existing explicit API/CLI device/render parameters stay device/render parameters and never mutate `drawingScale`, `constructionUnit` or physical meaning; for a physical document they may change resolution or sampling but never become a second physical drawing scale. Stop and report before changing the semantics of an API that cannot keep backward compatibility and this distinction together. |
| `DQ-C17` | accepted | ADR 0005 Decision 3 is not rewritten or erased; a dated, versioned amendment section records that the original `$INSUNITS = 0` rule governed the G5 experimental contract and is superseded for GeoCeDG `C`-era export by the accepted `D0`/`C` unit contract. ADR 0014 stays accepted and unchanged unless implementation finds a genuine contradiction. |

## Observation disposition

```text
OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT =
  POST-A2 LIFECYCLE DEFECT / OBSERVATION — NOT C BLOCKING —
  IMPLEMENTATION NOT AUTHORIZED —
  MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT
```

A tool replacement or macro-edit save can run a clearing `setXML` that `A-2`
treats as a committed document transition, recomputing the working layer
although the operation is not a user document replacement. It is not fixed in
`C`. Preferred characterization point: before or during `E1`, which exercises
the user-tool/GGT surface, unless the author schedules a separate focal
remediation earlier. It must not be forgotten at the global `G` closeout.

## Specification, product and verifier atomicity

D0 §15.3 stays mandatory. The technical candidate contains together, as
applicable: product behavior; unit-aware neutral export metadata; DXF
`$INSUNITS`; the `usm` sidecar contract; semantic curve export; `drawingScale`;
physical picture sizing; `ExportArea` participation; visibility semantics; the
normative DXF specification and geometry-export foundation amendments; the
unit-system §15 implementation status; the ADR 0005 amendment; verifier live
pins and evidence contracts; the staleness fingerprint; guides and developer
documentation. No intermediate technical candidate is frozen in which product,
specification and verifier disagree.

## Additional regression obligations

Besides the prompt's obligations, the tests cover: tool and macro replacement
does not reset `drawingScale`; API full-document `setXML` does; API reset `E6`;
equivalent pair normalization; `presentationUnit` invariance; no physical
authority from `printingScale`, zoom or DPI; physical PNG/PDF/SVG/EMF sizes;
EMF `rclFrame` nearest 0.01 mm; Classic/default FreeHEP unchanged; hidden DXF
layer OFF; individually hidden `60 = 1`; DXF explicit-area participation for
every G5 family; participation in current selection; `VISIBLE_VIEWPORT` never
makes DXF zoom-dependent; conservative certified Locus V2 participation near
the boundary; `OUTSIDE_EXPORT_AREA` is not a fidelity error; G5/G9X1 bytes
unchanged without an explicit area or unit change; a locally certified
incomplete Locus V2 is emitted and visibly reported incomplete; stale-revision
and tolerance-guarantee failures reject; semantic gaps stay gaps; no Spline V2
exactness special case; mandatory `usm` sidecar; unspecified-unit non-physical
mode; an explicit API/CLI render scale never mutates `drawingScale` or physical
meaning.

## Author-smoke package (required content)

Concrete fixtures and steps with the candidate, without hand-editing DXF or XML
where an automated fixture or inspection helper can be prepared: a physical
`cm` document at `1:1` and `1:2`; the same model with a different
`presentationUnit` and identical physical size; PNG, PDF, SVG and EMF; DXF
`mm`/`cm`/`m` `$INSUNITS`; DXF `usm` warning and paired sidecar; one hidden
GeoCeDG layer appearing as DXF OFF; one individually hidden object; an explicit
`ExportArea` that excludes and includes known sources; one Locus V2/Spline V2
in PGF, PSTricks and Asymptote; an incomplete but locally certified Locus V2;
the unspecified-unit device mode.

## Authorization state

```text
PRE-G9B-R6-plus-P0, A-1, B, D0, D1, A-2 = PASS — AUTHOR APPROVED — PUBLISHED
C preparation package      = PASS — AUTHOR APPROVED               (not published)
PRE-G9B-R6-plus-C          = AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-F3         = PLANNED — AUTHOR APPROVED — PUBLISHED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-R6-plus-E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.

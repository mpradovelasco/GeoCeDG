# PRE-G9B-R6-plus-F1 author-decision and preparation closeout record

```text
ARTIFACT_KIND             = AUTHOR-DECISION RECORD AND DOCUMENTARY CLOSEOUT
RECORD VERSION            = 1.0
TASK                      = PRE-G9B-R6-plus-F1-PREP-CLOSEOUT
AUTHOR_DECISION_SOURCE    = author instruction of 2026-10-11 ("Author decisions and
                            documentary closeout"; AUTHOR AUTHORIZED — RECONCILIATION
                            OF AUTHOR DECISIONS AND DOCUMENTARY CLOSEOUT)
PUBLISHED BASE            = P_C1 bde2827bb21e4a28e080db6a65b4abc892071cf8
                            tree 1b74102cea5151b8a6c571cb36b96b33af6fb3c8
PREPARATION CANDIDATE     = T_R6PLUS_F1_PREP 732292e6cb9bf3a0f29367e43eb39bdc6b4ad22b
                            tree 0839dd764ffceefc2fb009155b25f6495becaae1
                            (STATIC verification-38ff592383c64c3fbac46cb3c5023dd3,
                            ACCEPTED / COMPLETE); kept as the parent, not amended
CLOSEOUT CANDIDATE        = the commit that contains this record (direct child of
                            732292e6); its identity and STATIC are reported outside
                            this record, which cannot name its own commit
PRE-G9B-R6-plus-F1        = DESIGN — AUTHOR APPROVED; CANONICAL PROMPT PREPARED — NOT AUTHORIZED
IMPLEMENTATION            = NOT AUTHORIZED
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
passClaimed               = false   (no product claim of any kind)
```

This record transcribes the author's decisions of 2026-10-11 on `DQ-F1-1` to
`DQ-F1-9`. It is their authority and prevails over the recommendations of the
preparation candidate wherever they differ. It authorizes **no implementation**
and **no publication**. Machine-readable mirror:
[`pre-g9b-r6-plus-f1-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-author-decisions.json).
The reconciled design is
[`pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md`](../architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`); the execution contract is the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md)
(`PREPARED — NOT AUTHORIZED`). The
[characterization report](pre_g9b_r6_plus_f1_preparation_characterization_report.md),
its JSON mirror and the
[author-decision preparation record](pre_g9b_r6_plus_f1_author_decision_preparation_record.md)
are frozen preparation evidence and are not edited; corrections found during
this reconciliation are listed in §4. The `P0`
[F1 design candidate](../architecture/pre_g9b_r6_plus_f1_orientation_design_candidate.md)
stays unchanged as historical evidence.

## Classification vocabulary

| Tag | Meaning |
|---|---|
| **AUTHOR DECISION** | given by the author on 2026-10-11; authority of this record |
| **NORMATIVE INHERITANCE** | already fixed by an approved specification, ADR or earlier author decision; cited, not re-decided |
| **CHARACTERIZATION EVIDENCE** | read or measured at the base (report `K0`–`K17`) |
| **IMPLEMENTATION DESIGN** | resolution by this reconciliation inside the decided scope; reviewable by the author; not an author decision |
| **OPEN TECHNICAL ITEM** | open; listed with whether it blocks implementation authorization (§3) |

No agent recommendation is recorded here as an author approval.

## 1. Decisions

The author accepts the F1 preparation findings (report `K0`–`K17`).

### `DQ-F1-1` — presentation scope

- Recommendation of the preparation: global.
- **AUTHOR DECISION:** one global application presentation option; no
  per-object attribute.

### `DQ-F1-2` — view coverage

- Recommendation: Graphics View 1 only.
- **AUTHOR DECISION:** F1 initially supports Graphics View 1 only. Graphics
  View 2 is outside the authorized implementation design.

### `DQ-F1-3` — export participation

- Recommendation: screen-only.
- **AUTHOR DECISION:** screen-only. The cue must not appear in print,
  clipboard, image, LaTeX, DXF or any other export.
- CHARACTERIZATION EVIDENCE: no such route calls
  `GeoCeDGEuclidianView.paint` (report `K8`).

### `DQ-F1-4` — visual form

- Recommendation: hollow triangle, `L = 10 px`, `W = 4 px`, object colour.
- **AUTHOR DECISION:** a discreet hollow triangular arrowhead, nominal
  dimensions 10 × 4 device pixels, in the object's colour; geometry and
  placement are presentation-only.
- IMPLEMENTATION DESIGN: length `L = 10` and half-width `W = 4` device pixels
  at style scale 1, as tabled in the preparation (design §6); 1 px outline in
  the object colour and line opacity, interior filled with the view background;
  anchors of design §5. See `OTQ-F1-3`.

### `DQ-F1-5` — supported orientation

- Recommendation: E-B (constructive and inherited, fail-closed).
- **AUTHOR DECISION:** a positive eligibility matrix of constructively oriented
  or demonstrably inherited orientations. The direction authority is
  `GeoLine.getDirection`, validated against kernel path parametrization and the
  applicable construction semantics; `getDirectionInD3` is not the authority. A
  line is not given a user-defined constructive orientation merely because the
  coefficients of its equation admit a directed parametrization. Initially
  excluded equation-defined or convention-dependent routes keep their
  established kernel facts in documentation and show no cue until separately
  admitted. Unknown or unverified orientations are not guessed.
- IMPLEMENTATION DESIGN: the eligibility matrix v1 of design §3.3 (admitted
  `A1`–`A17`, excluded `X1`–`X10`). Option E-D (parametric independent lines)
  is rejected by the decision; option E-C (configuration routes) is outside the
  decision because those routes are neither constructive nor inherited.
  Inherited routes are admitted only when every orienting input is itself
  admitted (recursive, fail-closed). Affine images are limited to the
  dedicated, source-proven methods (§4 erratum `ER-1`). See `OTQ-F1-1` for
  macros.

### `DQ-F1-6` — preference

- Recommendation: `USER_PREFERENCE` `geocedg.view.orientation-cue.v1`,
  default hidden.
- **AUTHOR DECISION:** global preference `geocedg.view.orientation-cue.v1`,
  default OFF; it survives an application restart; it does not belong to
  document serialization; New and Open do not modify it.
- NORMATIVE INHERITANCE: `USER_PREFERENCE` as in `unit-system.md` §17.2 (owner:
  GeoCeDG properties store; no undo).
- IMPLEMENTATION DESIGN: values `shown` / `hidden`; any other value reads as
  OFF and is not written back; written with the portable store (window close,
  Save Settings); never in document, macro or preferences XML; not an undo
  step; one value per process store, a change repaints every GeoCeDG window;
  never read by Classic; a test seam like `GeoCeDGUnitPreferences.useStoreForTesting`.

### `DQ-F1-7` — suppression

- Recommendation: design §4 of the preparation.
- **AUTHOR DECISION:** suppress the cue for undefined, non-finite or
  degenerate objects, effectively hidden objects, insufficient visible extent
  and redundant directional decorations. No arrow is ever displayed for an
  invisible or inadmissible object. Dynamic orientation inversions stay
  faithful to the kernel and are never smoothed or reassigned.
- IMPLEMENTATION DESIGN: the eligibility predicate and degeneration table of
  design §4; "redundant directional decorations" are the three segment
  midpoint arrow decorations (`DECORATION_SEGMENT_ONE_ARROW`,
  `TWO_ARROWS`, `THREE_ARROWS`), as proposed; arrow ending styles do not
  suppress (`OTQ-F1-2`); the sense is re-read from `getDirection` at every
  paint, with no memory, hysteresis or continuity heuristic.

### `DQ-F1-8` — architecture and verification

- Recommendation: S1, `BOUNDED_PHASE`.
- **AUTHOR DECISION:** a presentation-only overlay in GeoCeDG Desktop Graphics
  View 1; no new `GeoElement`, kernel algorithm, persistence attribute,
  construction dependency or shared drawable change. `BOUNDED_PHASE` is accepted
  as the planned implementation class, conditioned on the entry gate confirming
  the approved scope. The registered `PHASE` covers geometric admissibility,
  direction, rendering, lifecycle, visibility, preferences and product-profile
  regressions.
- IMPLEMENTATION DESIGN: seam S1 (design §7); entry-gate conditions and the
  phase coverage in the canonical prompt.

### `DQ-F1-9` — user interface

- Recommendation: U3 (menu action and Layout & Presentation row).
- **AUTHOR DECISION:** one declarative menu action and a synchronized
  preference control in Layout & Presentation; catalog delta 130 → 131. The
  delta alone does not imply `INTEGRATED_PHASE`; the phase proves catalog
  completeness, stable IDs, projections, status synchronization and every
  applicable profile pin. If the UI controls require unexpected inherited-class
  modifications or broaden the product contract, the work stops for author
  review.
- IMPLEMENTATION DESIGN: proposed action id `presentation.orientation-cue`,
  `kind: product-action`, target `geocedg.presentation.orientation-cue`,
  `effect_profile_id: preference-only`, `availability_profile_id:
  product-only`, `command_surface_profile_id: action-only`, maturity
  `experimental`, checkbox state from the preference, in the View menu group
  `view-visibility-style` (precedent `export.area.show`); the Layout &
  Presentation control is a row inside an existing GeoCeDG-owned panel
  (`GeoCeDGThemeOptionsPanel` or `GeoCeDGPresentationOptionsPanel`), so no
  `AppD` or `OptionsLayoutD` hook is needed; both controls read and write the
  one preference owner in `AppGeoCeDG`. Profile pins to update: report `K14`.

## 2. Distinction kept by the design

The cue shows the **kernel orientation** — the sense of `getDirection`, shared
by path parameters, `UnitVector` and oriented angles — not a geometrically
intrinsic sense of the point set. A line as a point set has no sense; the
kernel's sense is meaningful to the user only where the construction gives it
(constructive) or carries it over by a fixed rule (inherited). The excluded
routes keep a real, documented kernel sense (report `K4`, `K6`) that the cue
does not display.

## 3. Open technical items

| Id | Item | Blocks implementation authorization? |
|---|---|---|
| `OTQ-F1-1` | Macro outputs (`A17`): admitted only if the inner output's route can be resolved read-only, by identity or definition position, through existing public API of `AlgoMacro`/`Macro`; otherwise macro outputs stay excluded (`X10`). The entry gate establishes which | no (fail-closed default) |
| `OTQ-F1-2` | Interpretation of "redundant directional decorations" as the three midpoint arrow decorations, not the arrow ending styles; the author may widen it | no (proposal applies unless the author widens it) |
| `OTQ-F1-3` | Interpretation of "10 × 4" as length 10 and half-width 4 (base 8) device pixels, per the preparation design §6; if the author meant a total base of 4 px, only the constant changes | no |
| `OTQ-F1-4` | Host panel of the Layout & Presentation row (`GeoCeDGThemeOptionsPanel` or `GeoCeDGPresentationOptionsPanel`), chosen at the entry gate by which needs no inherited-class change; if neither can host it, stop (`DQ-F1-9`) | no |

## 4. Corrections to the frozen preparation evidence

| Id | Location | Correction |
|---|---|---|
| `ER-1` | report §K5 "Affine transforms"; design (preparation) §3.2 | The probed transforms do not use `GeoLine.matrixTransform`. They use dedicated methods: `translate` leaves `(x, y)` unchanged (`GeoLine.java:750-751`); `dilate` multiplies the coefficients by `r` (`:763-770`); `rotate` rotates `(x, y)` by `φ` about the origin or a point (`:777-800`); `mirror` at a point negates `(x, y)` (`:804-811`); `mirror` at a line applies the reflection to `(x, y)` (`:817-…`). Each maps `getDirection` to its image under the linear part (PROVEN FROM SOURCE; probed R18–R18e). The adjoint argument applies only to `matrixTransform` users (`AlgoShearOrStretch`, `AlgoAttachCopyToView`), which were not probed and stay excluded (`X9`) |
| `ER-2` | preparation design §3.2, `AlgoRotate*` | replaced by the explicit `AlgoRotate` and `AlgoRotatePoint` rows of the eligibility matrix |

## 5. Preparation closeout

| Item | State |
|---|---|
| preparation candidate | `732292e6cb9bf3a0f29367e43eb39bdc6b4ad22b`, tree `0839dd764ffceefc2fb009155b25f6495becaae1`, direct child of `P_C1` `bde2827b`, seven files (report, decision preparation record, reconciled design, JSON mirror, canonical prompt, mini-track plan, roadmap), worktree clean |
| preparation STATIC | `verification-38ff592383c64c3fbac46cb3c5023dd3`, `ACCEPTED / COMPLETE`, bound to that commit and tree; standing diagnostics only (`diagnostic.governance` finding, `diagnostic.historical-consistency` unavailable) |
| `P0` F1 design candidate | unchanged since the base |
| characterization | `COMPLETE`, frozen |
| decisions | `DQ-F1-1` to `DQ-F1-9` recorded (§1) |
| reconciled design | `DESIGN — AUTHOR APPROVED` (the design only) |
| canonical prompt | reconciled; `PREPARED — NOT AUTHORIZED` |
| specification / ADR | none promoted; the specification remains the first deliverable of an authorized F1 |
| planned implementation class | `BOUNDED_PHASE`, author-accepted, conditioned on the entry gate |
| closeout class | `DOCUMENTATION_STATUS_ONLY`, acceptance `STATIC` |
| implementation, publication | `NOT AUTHORIZED` |

## 6. Next decision

After review of this documentary closeout, the author decides whether to
authorize and publish the F1 preparation package (preparation candidate and
this closeout commit), and then, separately, whether to authorize the F1
implementation against an exact published base. `F2`, `F3`, `F4` and `G` are
unchanged and unauthorized.

```text
AUTHOR_DECISION          = RECORDED IN THIS ARTIFACT (DQ-F1-1 to DQ-F1-9, 2026-10-11)
DESIGN                   = DESIGN — AUTHOR APPROVED
IMPLEMENTATION           = NOT AUTHORIZED
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
```

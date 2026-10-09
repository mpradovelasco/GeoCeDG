# PRE-G9B-R6-plus-E3 author-decision record

```text
ARTIFACT_KIND             = AUTHOR-DECISION RECORD (documentary)
TASK                      = PRE-G9B-R6-plus-E3-PREP-CLOSEOUT
AUTHOR_DECISION_SOURCE    = author instruction of 2026-10-10 ("Author Decision
                            Reconciliation, Design Promotion and Canonical Prompt
                            Publication"; AUTHOR AUTHORIZED — DOCUMENTATION AND
                            PUBLICATION ONLY)
PUBLISHED BASE            = P_R6PLUS_POST_E2_P4 13e08ac1fe1c6c931a88985b97df93d8445812b9
                            tree dd2ba17c483a18b8931e1848f38616edfa98b06a
PREPARATION CANDIDATE     = T_R6PLUS_E3_PREP e063a8a27450cded0655afb6abc1e78f89f9ac6f
                            tree 00549e5bf803eb0c7afd35502d18a30396a75f32
                            (STATIC verification-ae183de8b275467a876526563627f35b,
                            ACCEPTED / COMPLETE, 3/3); kept as its ancestor, not amended
RECONCILIATION CANDIDATE  = the commit that contains this record (direct child of
                            e063a8a2); its STATIC and publication are reported with
                            the publication, because a commit cannot contain its own
                            verification identity
PRE-G9B-R6-plus-E3        = DESIGN — AUTHOR APPROVED; CANONICAL PROMPT PREPARED — NOT AUTHORIZED
IMPLEMENTATION            = NOT AUTHORIZED
PUBLICATION (this package)= AUTHORIZED by the same instruction (documentation only)
selfApproved              = false
passClaimed               = false   (no product claim of any kind)
```

This record transcribes the author's decisions of 2026-10-10 on `DQ-E3-1` to
`DQ-E3-16`. It is their authority and supersedes the recommendations of the
preparation candidate wherever they differ. It authorizes **no
implementation**; publication of the design does not authorize its execution.
Machine-readable mirror:
[`pre-g9b-r6-plus-e3-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e3-author-decisions.json).
The reconciled design is
[`pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md`](../architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`); the execution contract is the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md)
(`PREPARED — NOT AUTHORIZED`). The
[characterization report](pre_g9b_r6_plus_e3_preparation_characterization_report.md)
is frozen evidence and is not edited.

## Classification vocabulary

| Tag | Meaning |
|---|---|
| **AUTHOR DECISION** | given by the author on 2026-10-10; authority of this record |
| **NORMATIVE INHERITANCE** | already fixed by an approved specification, ADR or earlier author decision; cited, not re-decided |
| **CHARACTERIZATION EVIDENCE** | measured or read at the base (report `K0`–`K21`) |
| **IMPLEMENTATION DESIGN** | resolution by this reconciliation inside the approved scope; reviewable by the author; not an author decision |
| **UNRESOLVED TECHNICAL DETAIL** | open; listed with whether it blocks implementation authorization (§3) |

No agent recommendation is recorded here as an author approval.

## 1. Decisions

### `DQ-E3-1` — geometric representation

- Previous recommendation: R2, closed `GeoPolyLine` outputs.
- **AUTHOR DECISION:** approve R2. Existing entity types, preferring closed
  polylines and associated existing outputs; no new `GeoElement` type for the
  sheet; dynamic dependencies, identity and traceability, persistence,
  undo/redo, export-adapter compatibility, deterministic reconstruction and
  access to corners through existing operations are preserved. No new durable
  identity mechanism without separate justification and authorization.
- NORMATIVE INHERITANCE: `AGENTS.md` §1, §4, §10; ADR 0034 (β precedent).
- CHARACTERIZATION EVIDENCE: `K8` (drag predicates; polygon region and area;
  `Vertex(<GeoPoly>, n)`; DXF mapping), `K9` (no persistent identity of
  ordinary objects).
- IMPLEMENTATION DESIGN: outputs are associated **only through the kernel's own
  construction relation**: every output's parent is the one `IsoABorder`
  algorithm, and its role is its fixed index in that algorithm's output array
  (`PAPER` = 0, `FRAME` = 1, `LABEL` = 2), serialized as the ordered
  `<output a0 a1 a2>` of `<command name="IsoABorder">`. This is the existing
  multi-output mechanism (`AlgoNativeDimension`, `K8`); no labels, proximity,
  construction indices or new identity infrastructure. Design §3.
- Prompt impact: objective 1, allowed scope, forbidden scope (new type,
  identity infrastructure), stop conditions.
- Verification: `T-TRACEABILITY`, `T-SAVE-REOPEN`, `T-UNDO-REDO`.
- Remaining constraint: none.

### `DQ-E3-2` — sheet geometry and visible frame

- Previous recommendation: (b) trim hidden + frame, uniform 10 mm margin.
- **AUTHOR DECISION:** approve the **inner frame**. Two distinct boundaries:
  (A) the physical paper boundary at nominal ISO 216 size — authoritative for
  the sheet and for its `ExportArea` producer, hidden by default, no visible
  dotted exterior rectangle, hidden corners, viewport-independent, usable as
  an export rectangle while hidden, always part of the construction;
  (B) an optional visible inner drawing frame with margins **left 20 mm,
  right 10 mm, top 10 mm, bottom 10 mm** (ISO 5457-style disposition, **no
  claim of ISO 5457 compliance**), converted exactly like the paper, built as
  a constructive output from `W_f = W_p − m_L − m_R`, `H_f = H_p − m_T − m_B`
  with `W_f > 0`, `H_f > 0`, through the same captured conversion; no title
  block, sheet sets, paper space, viewports or `G15` architecture.
- NORMATIVE INHERITANCE: plan §1.1 (`G15` ownership); `AQ-G3` / `DQ-E1-8`
  (hide requirement as reference evidence).
- CHARACTERIZATION EVIDENCE: `K5` (legacy dotted trim, uniform frame, title
  block); `K20` `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY`.
- IMPLEMENTATION DESIGN: margins are fixed constants of the command semantics
  (not inputs); "left" is the side at `x_P` in both orientations; the frame's
  upper-left corner is `P + (conv(m_L), −conv(m_T))` and its size
  `conv(W_f) × conv(H_f)`; the frame is computed from `W_f`, `H_f`, never from
  differences of paper corners. Design §4, §5.
- Prompt impact: objective, geometric invariants, `T-FRAME`.
- Verification: `T-FRAME`, `T-MARGINS`, `T-PRESENTATION`.
- Remaining constraint: OTQ-E3-2 (Algebra View presentation of an undefined
  frame; non-blocking).
- `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY`: its design
  conflict is **dispositioned** by this decision; nothing is implemented.

### `DQ-E3-3` — ISO A formats and optional frame

- Previous recommendation: A0–A10.
- **AUTHOR DECISION:** A0–A10 supported; inner frame **enabled by default for
  A0–A4, disabled by default for A5–A10**; the user may control the option
  subject to geometric validity; margins are never silently reduced; no zero or
  negative frame; an impossible frame is disabled or rejected with an explicit
  reason; the paper boundary is valid independently of the frame; absence of
  the frame never invalidates the border; label and export never assume a
  frame. ISO 216 nominal integer sizes; the legacy unrounded series is not
  inherited; A3 landscape = 420 × 297 mm.
- NORMATIVE INHERITANCE: none beyond ISO 216 as the cited reference.
- CHARACTERIZATION EVIDENCE: `K4`.
- IMPLEMENTATION DESIGN: frame validity is decided in exact integer
  millimetres before conversion; with the approved margins the only impossible
  case is **A10 portrait** (`W_f = 26 − 30 = −4`); design §6 lists every
  size and orientation.
- Prompt impact: format table, `T-ISO-TABLE`, `T-FRAME`.
- Verification: `T-ISO-TABLE`, `T-FRAME`, `T-INVALID`.
- Remaining constraint: none.

### `DQ-E3-4` — command inputs

- Previous recommendation: index, Boolean, integer pair, literals; optional
  margin argument.
- **AUTHOR DIRECTION:** ordinary explicit inputs; the UI may capture the unit
  factor and the drawing scale at creation; no live read of units or session
  scale; unambiguous format and orientation; positive integer scale pair;
  explicit unit factor; explicit inner-frame state; reproducible without the
  GUI; persisted expressions reconstruct the same geometry; no overloads
  invented without verifying the command infrastructure.
- NORMATIVE INHERITANCE: `unit-system.md` §6.1, §6.3, §12, §18.4.
- CHARACTERIZATION EVIDENCE: `K9`/P4 (literals round-trip bit-exactly);
  `K15` (processors check argument counts explicitly, e.g.
  `CmdAlignedDimension` accepts 3 or 5).
- IMPLEMENTATION DESIGN: **one** signature, no overloads:
  `IsoABorder(<Point>, <ISO A index>, <Landscape>, <Scale numerator>,
  <Scale denominator>, <Model units per millimetre>, <Inner frame>)` — the
  margin argument of the preparation is withdrawn because the margins are
  fixed by `DQ-E3-2`; `<Inner frame>` is a Boolean. Design §2.
- Prompt impact: signature, examples, `T-SIGNATURE`.
- Verification: `T-SIGNATURE`, `T-SAVE-REOPEN`.
- Remaining constraint: none.

### `DQ-E3-5` — numerical expression

- Previous recommendation: `((L·b)/a)·u`, bit-identical tests.
- **AUTHOR DECISION:** accept, subject to validation. Normative relation
  `u = 10^-3 / fb(c)`, `L = P · u · b/a`; preferred binary64 order
  `L = ((P·b)/a)·u`. The characterization supports but does not prove correct
  rounding for every admissible input. The design must specify domains,
  positivity and finiteness, overflow and underflow, exact preservation of the
  integer pair, ratio normalization, reproducible save/reopen and explicit
  tolerances; no silent geometric rounding or size approximation.
- NORMATIVE INHERITANCE: `unit-system.md` §3.2, §4.1, §5.3, §13, §18.4.
- CHARACTERIZATION EVIDENCE: `K6` (S1, S2), `K12`, `K9`/P4.
- IMPLEMENTATION DESIGN: design §7 — domains; `P·b` exact; equivalent pairs
  (`2:100`, `1:50`) give bit-identical results because IEEE 754 division of
  exactly represented integers is correctly rounded; absorbed or non-finite
  coordinates make the outputs undefined; tolerances: bit identity with the
  canonical expression, relative error ≤ `2^-50` against the exact value (exact
  SI factor, or the stored `k` for `usm`; a rounding budget of five operations
  of ≤ `2^-53` each), physical page checks at ≤ 0.001 pt (PDF) and 0.01 mm (EMF).
- Prompt impact: invariants, `T-CONVERSION`.
- Verification: `T-CONVERSION`, `T-INVALID`.
- Remaining constraint: none (the "not proven for every input" caveat is
  carried as a declared bound, not as an open question).

### `DQ-E3-6` — unit change after creation

- Previous recommendation: keep geometry; indicator reports the mismatch.
- **AUTHOR DECISION:** geometry remains unchanged (no rescale, no recompute of
  `u`, no coordinate change, no redefinition, no identity change); the change
  reinterprets the physical meaning; the user is informed when the sheet
  becomes physically inconsistent with its nominal size. Physical-size
  coherence condition `(f_n/f_c) · (s_n/s_c) = 1` within an explicit
  criterion, or an equivalent direct computation from `u`, the constructed
  dimensions and the current unit; no redundant original-unit identifier is
  persisted. **Physical-size coherence** and **scale-label coherence** are two
  independent checks. With an unspecified or invalid unit, physical coherence
  is never claimed. The result is derived presentation state with no kernel
  dependency.
- NORMATIVE INHERITANCE: `unit-system.md` §6.1, §6.2 (status UI is a permitted
  live consumer), §7.2, §13; ADR 0032.
- IMPLEMENTATION DESIGN: design §9 — `f_c = 10^-3/u` from the captured input;
  effective page `E_i = ((P_i·b_c)/a_c)·u · fb(effC) · (a_n/b_n) · 1000` mm,
  computed from the inputs, never from coordinates; physical states
  `COHERENT` (`|E_i − P_i| ≤ 1e-6 mm` for both sides) / `INCOHERENT` /
  `NOT_DETERMINABLE`; scale states `MATCH` (normal-form pairs equal exactly) /
  `DIFFERENT` / `NOT_APPLICABLE` (unspecified unit, `unit-system.md` §12).
- Prompt impact: `T-UNIT-CHANGE`, `T-COHERENCE`.
- Verification: `T-UNIT-CHANGE`, `T-COHERENCE`, `T-USM`.
- Remaining constraint: none; the `1e-6 mm` criterion is IMPLEMENTATION
  DESIGN, reviewable by the author.

### `DQ-E3-7` — unspecified model unit

- **AUTHOR DECISION:** block creation with `UNSPECIFIED_MODEL_UNIT`; offer the
  document-unit configuration; no implicit `mm` or `cm`; a valid `usm` is
  supported.
- NORMATIVE INHERITANCE: `unit-system.md` §3.3, §18.4 (`AQ-U6`).
- CHARACTERIZATION EVIDENCE: `K14` (no unit-gated action exists;
  `unavailableReason`, `openDocumentUnits`).
- IMPLEMENTATION DESIGN: the gate applies to the tool and to
  `ISO_A_SELECTION`; a typed command remains a pure function of its explicit
  inputs (§6.1 forbids it from reading the unit), so its creation is not
  blocked, and its coherence is `NOT_DETERMINABLE` while the unit is
  unspecified. Design §12.
- Verification: `T-NO-UNIT`, `T-USM`.
- Remaining constraint: none.

### `DQ-E3-8` — `ExportArea` participation

- **AUTHOR DECISION:** explicit participation only; creation never replaces the
  active `ExportArea`; an explicit yes/no after creation; activation needs
  consent; one `ExportArea` authority; precedence `explicitly activated
  producer > Export_1/Export_2 > visible viewport` preserved; the area is the
  **physical outer boundary**, never the frame; its hidden state does not
  prevent use; no second mechanism; no serialization of the session state.
- NORMATIVE INHERITANCE: `B` `AQ-X2`, `AQ-X7`; `unit-system.md` §7.3.
- CHARACTERIZATION EVIDENCE: `K10`.
- Design §11; verification `T-PRODUCERS`, `T-EXPORT-CONSISTENCY`.
- Remaining constraint: none.

### `DQ-E3-9` — `ExportArea` linkage lifecycle

- **AUTHOR DECISION:** live, explicit, session-scoped linkage; live while valid,
  discarded when obsolete; never labels or construction positions as
  identity; validity rules for deletion, undo/redo, redefinition,
  reconstruction, document replacement, New/Open and reload; a surviving
  compatible object is distinguished from a replaced one; a stale reference
  never activates another object; afterwards the `ExportArea` fallback rules
  apply; no persistent cross-document association; if the session mechanisms
  cannot meet this without a substantially wider change, document and stop.
- CHARACTERIZATION EVIDENCE: `K9` (rebuild replaces Java objects;
  `isInConstructionList` answers `true` for stale objects), `K10` (no element
  field, no rebuild signal).
- IMPLEMENTATION DESIGN: design §11.3 — the link holds the algorithm, and is
  valid only if an **identity (`==`) scan of the live construction** finds
  that algorithm and its output 0 and the output is defined; the existing
  `ExportAreaSession.resetForDocument` clears it on New and Open. This uses
  existing APIs; **no wider architectural change is needed**, so the stop
  condition is not triggered. A stale link falls back by `B`'s rule with a
  visible notice; an unrelated undo also invalidates the link (consequence of
  `K9`), and the user re-links explicitly.
- Verification: `T-LINK-LIFECYCLE`.
- Remaining constraint: none.

### `DQ-E3-10` — `ISO_A_SELECTION`

- **AUTHOR DIRECTION:** preserve the separate producer contract; `ISO_A_SELECTION`
  (explicit ISO-sized area selection) and `ISO_A_BORDER` (area from an
  `IsoABorder`) are two producers of the same authority, never competing
  authorities and never merged; `AQ-X2` unchanged; candidate §7.5 governs the
  details not changed by these decisions.
- NORMATIVE INHERITANCE: `AQ-X7`.
- IMPLEMENTATION DESIGN: design §11.5 (unchanged from the preparation: corner
  copied, never linked; session `drawingScale` and current unit; computed once
  with the shared helper; no frame; unit gate).
- Remaining constraint: OTQ-E3-3 (coherence presentation for an
  `ISO_A_SELECTION` after a later scale change; non-blocking).

### `DQ-E3-11` — stable sheet label

- **AUTHOR DECISION:** constructive text output; stable, exportable,
  independently hideable; identifies the nominal size and the **captured**
  scale (example `A3 — 1:50`); never the session scale; dynamically
  consistent with its defining inputs; independent of the document language;
  placement accounts for the optional frame; no title-block or physical text
  size work; no claim of DXF text export.
- CHARACTERIZATION EVIDENCE: `K13`, `K8` (text excluded from DXF).
- IMPLEMENTATION DESIGN: design §10 — output 2; text
  `"A" + n + " — " + a' + ":" + b'` with the gcd-normal pair; anchored at
  the paper point `(W_p − m_R, m_B)` from the lower-left corner, which is the
  frame's lower-right corner when the frame exists and stays inside the paper
  when it does not; defined whenever the paper boundary is defined.
- Verification: `T-LABEL`.
- Remaining constraint: none.

### `DQ-E3-12` — scale and physical coherence indicator

- **AUTHOR DECISION:** approve and extend: distinguish (1) captured versus
  current export scale and (2) nominal versus effective physical export
  dimensions; status bar and export dialogs; `DERIVED_TRANSIENT`; never
  modifies geometry or labels, never serialized, never in undo, never
  exported, no live kernel dependency. Explicit action **Use sheet scale**
  sets the session export scale to the sheet's captured scale on explicit
  activation, changes no geometry and does not claim to solve every
  mismatch; afterwards both conditions are re-evaluated and shown. **Activation
  warning:** on activating an `IsoABorder` as producer, evaluate physical
  coherence immediately; if incoherent, warn with the discrepancy in the
  current unit and scale, offer the scale action when applicable, never change
  the scale without consent, never claim that a scale change fixes a
  unit-reinterpretation mismatch; no automatic geometry scaling.
- NORMATIVE INHERITANCE: `unit-system.md` §6.2, §12 (session `drawingScale`);
  `C` `DQ-C7`.
- CHARACTERIZATION EVIDENCE: `K12` (silent page change at another scale).
- IMPLEMENTATION DESIGN: design §9 — the indicator follows the **linked**
  border; "Use sheet scale" is offered when the scale state is `DIFFERENT`;
  when the physical state stays `INCOHERENT` after it, the message names the
  unit reinterpretation as the remaining cause.
- `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH`: **OPEN — correction designed
  (`DQ-E3-12`) — NOT RESOLVED** until an implementation is accepted.
- Verification: `T-COHERENCE`, `T-SCALE-ACTION`, `T-ACTIVATION-WARNING`.

### `DQ-E3-13` — user workflow

- **AUTHOR DECISION:** `Construction → Annotations and media → ISO A Border`;
  no toolbar button; flow: reference point, format and orientation, drawing
  scale, optional inner frame, label configuration, complete dynamic
  construction, one undo point, optional explicit `ExportArea` activation;
  the established behavior for a tool-created point; frame option in the
  dialog, initially enabled for A0–A4 and disabled for A5–A10; no invalid
  frame; no title-block controls; existing interaction patterns and
  localization resources.
- IMPLEMENTATION DESIGN: design §12 (one-shot mode `MODE_ISO_A_BORDER` 144;
  tool-created point hidden, per `AQ-G3`; local scale presets; the frame
  checkbox is disabled with its reason for A10 portrait).
- Verification: `T-TOOL`.

### `DQ-E3-14` — native command and catalog

- **AUTHOR DECISION:** `IsoABorder`, Spanish alias `MarcoISOA`; reconcile the
  actions and catalog against the actual base; verify `127 → 130`; no collision
  with legacy macros, built-in commands or localized aliases; preserve Classic
  behavior unless the approved native-command compatibility contract requires
  otherwise.
- Independently re-verified at the base by this reconciliation: 127 actions
  (`apps/geocedg/application-profile.yml`, runtime pin
  `GeoCeDGProfile.java:340-345`); the ids `presentation.iso-a-border`,
  `export.area.iso-a`, `export.area.use-iso-a-border` are unused;
  `MODE_*` 144 is unused (last 143); `IsoABorder` and `MarcoISOA` occur in no
  `command*.properties` file, and neither equals any of the 24 `Templatev7`
  macro names. Catalog **127 → 130** (one `upstream-mode`, two
  `product-action`); "Use sheet scale" is a dialog/status action, not a
  catalog action.
- NORMATIVE INHERITANCE: ADR 0031 decisions 5, 7, 10, 12.
- Remaining constraint: **OTQ-E3-1** (Classic surface of the new command).

### `DQ-E3-15` — historical macros

- **AUTHOR DECISION:** no migration; `sheetISOAnLand` and `sheetISOAnVert` stay
  behavioral references with their provenance; `Templatev7.ggb` and
  historical baselines unchanged; no silent promotion of macro formulas or
  conventions; coexistence with document macros under the approved
  resolution and precedence rules.
- NORMATIVE INHERITANCE: `DQ-E1-7`, `DQ-E1-8`; ADR 0031 decision 6.
- CHARACTERIZATION EVIDENCE: `K5`, `K16`.
- Verification: `T-LEGACY`.

### `DQ-E3-16` — verification class

- **AUTHOR DECISION:** future implementation class `INTEGRATED_PHASE`, acceptance
  `PHASE + INTEGRATION`, conditional on the reconciled scope; reclassify or
  stop on a new geometric entity type, a new structural XML element,
  persistent `ExportArea` state, unplanned global rendering or geometry
  changes, a material widening of compatibility, or unjustified new
  architectural infrastructure. The future prompt carries the listed test
  families. This documentary task uses `DOCUMENTATION_STATUS_ONLY` (`STATIC`
  and the prompt-contract check); no implementation tests are written.
- NORMATIVE INHERITANCE: `verification-levels.md` §12.8.

## 2. Out-of-scope observations preserved

| Identifier | Disposition |
|---|---|
| `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH` | OPEN — correction designed by `DQ-E3-12` — NOT RESOLVED |
| `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` | OPEN — OUTSIDE E3 — NOT FIXED; `tools/legacy/ingest.ps1`, `models/legacy/template-v7/` and `Templatev7.ggb` unchanged; no inventory regenerated; a separate task was suggested to the author during the preparation |
| `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | OPEN — addressed in design by the identity-only validity test (`DQ-E3-9`) — NOT RESOLVED |
| `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY` | DISPOSITIONED BY `DQ-E3-2` (design conflict closed; nothing implemented) |
| `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` | RECORDED — `E2` unchanged; `E3` fixes its own canonical order (`DQ-E3-5`) |
| `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | OPEN — gate designed by `DQ-E3-7` — NOT RESOLVED |

## 3. Unresolved technical details

| Id | Detail | Blocks implementation authorization? |
|---|---|---|
| `OTQ-E3-1` | Classic surface: `E2`'s commands are not feature-gated, so they are available in the Classic diagnostic profile; no approved contract states this explicitly for GeoCeDG commands. `IsoABorder` either follows that precedent (command available in Classic, no Classic menu, tool, producer or indicator) or is gated out of Classic (a new, `RuntimeFeatureService`-like predicate). | **Yes — the author must choose at implementation authorization.** Recommended default for that decision: follow the `E2` precedent. |
| `OTQ-E3-2` | Algebra View presentation of `FRAME` while it is undefined (frame disabled or impossible): shown as an undefined object, or marked auxiliary by the tool. Presentation only. | No; the implementation reports its choice in the candidate. |
| `OTQ-E3-3` | Coherence presentation for an `ISO_A_SELECTION` rectangle after a later session-scale change; `DQ-E3-12` covers `ISO_A_BORDER` only. | No; without a further author decision, `E3` shows no indicator for `ISO_A_SELECTION`. |
| `OTQ-E3-4` | A frameless sheet (the default for A5–A10) shows only its label; with the paper boundary and a tool-created reference point hidden, it has no visible draggable output and is moved by editing the point or by showing the paper boundary. A consequence of `DQ-E3-2`, `DQ-E3-3` and `DQ-E3-13`, not a new rule. | No; the author may revisit it at implementation authorization. |

## 4. Status after this record

```text
PRE-G9B-R6-plus-E3 DESIGN            = DESIGN — AUTHOR APPROVED (2026-10-10)
PRE-G9B-R6-plus-E3 CANONICAL PROMPT  = PREPARED — NOT AUTHORIZED
PRE-G9B-R6-plus-E3 IMPLEMENTATION    = NOT AUTHORIZED
NEXT GATE                            = author review of the published design package,
                                       then an explicit implementation authorization
                                       naming the exact base and disposing of OTQ-E3-1
F1, F2, F3, F4, G                    = unchanged (NOT AUTHORIZED; F3 PLANNED; F4
                                       PLANNING INTENT RECORDED)
selfApproved                         = false
```

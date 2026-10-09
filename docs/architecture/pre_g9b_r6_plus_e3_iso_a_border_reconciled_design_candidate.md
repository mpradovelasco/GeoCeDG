# PRE-G9B-R6-plus-E3 — native `IsoABorder` and `ExportArea` integration: reconciled design

- Status: **DESIGN — AUTHOR APPROVED** (author decision of 2026-10-10 on
  `DQ-E3-1` to `DQ-E3-16`,
  [E3 author-decision record](../validation/pre_g9b_r6_plus_e3_author_decisions_record.md),
  which is their authority and prevails over this file). The approval concerns
  this reconciled design, **not its implementation**: implementation is
  `NOT AUTHORIZED` and needs a separate explicit author authorization.
- Produced by: `PRE-G9B-R6-plus-E3-PREP` (2026-10-09), reconciled and promoted
  by `PRE-G9B-R6-plus-E3-PREP-CLOSEOUT` (2026-10-10); no implementation
- Bases: `P_R6PLUS_POST_E2_P4` = `13e08ac1fe1c6c931a88985b97df93d8445812b9`
  (tree `dd2ba17c483a18b8931e1848f38616edfa98b06a`); preparation candidate
  `T_R6PLUS_E3_PREP` = `e063a8a27450cded0655afb6abc1e78f89f9ac6f`
  (tree `00549e5bf803eb0c7afd35502d18a30396a75f32`), whose version of this file
  is the proposal the author decided on
- Supersedes, where they differ: the `P0`
  [design candidate](pre_g9b_r6_plus_e3_iso_a_border_design_candidate.md)
  (kept unchanged as `P0` evidence) and the stale facts of the
  [mini-track plan](pre_g9b_r6_plus_minitrack_plan.md) §8.6 listed in §19
- Evidence: [characterization report](../validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md)
  (`K0`–`K21`, frozen) and its JSON mirror
- Execution contract when authorized: [canonical `E3` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md)
  (`PREPARED — NOT AUTHORIZED`)
- Normative inputs it implements, not restates: [unit-system specification](../../geocedg/specs/units/unit-system.md)
  §3.2, §3.3, §5.3, §6, §7.2, §7.3, §12, §13, §17, §18.4;
  [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md);
  [B author decisions](../validation/pre_g9b_r6_plus_b_author_decisions_record.md)
  `AQ-X2` and `AQ-X7`; [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md);
  [ADR 0034](../adr/0034-native-dimension-representation-and-presentation-seams.md)
  (β precedent); `AGENTS.md` §1, §4, §10

Each rule carries its provenance: **[AD]** author decision of 2026-10-10,
**[NI]** normative inheritance, **[CE]** characterization evidence, **[ID]**
implementation design resolved by the reconciliation inside the approved
scope (reviewable, not an author decision), **[OTQ]** unresolved technical
detail (§18). This file is not a specification; the implementing phase turns
it into the durable specification named in the prompt.

## 1. Placement and implementation boundaries

| Responsibility | Owner | Reason |
|---|---|---|
| `IsoABorder` command, algorithm, ISO 216 table, margin constants, canonical conversion, frame validity, degeneracies, label string | **shared Java kernel** (`org.geocedg.common.kernel.sheet`, proposed) [NI `AGENTS.md` §4] | DAG dependency on a point, dynamic evaluation, persistence, every frontend |
| the ISO table, margins and conversion as one pure helper, also used by `ISO_A_SELECTION` and the coherence computation | shared kernel helper [ID] | one numeric source of truth; never duplicated in Desktop |
| tool mode, point pick, dialog, creation-time capture of `u` and `a:b`, unit gate, activation question | GeoCeDG Desktop [NI §6.3] | tool orchestration |
| `ISO_A_BORDER` and `ISO_A_SELECTION` producers, link validity, notices | GeoCeDG Desktop `ExportAreaSession` and the DXF producer mapping [NI `AQ-X2`] | session export state, never geometry |
| scale and physical coherence indicator, "Use sheet scale", activation warning | GeoCeDG Desktop status bar and export dialogs [AD `DQ-E3-12`; NI §6.2] | `DERIVED_TRANSIENT` presentation |
| rendering and export of the outputs | existing drawables and adapters | adapters consume ordinary outputs |

The command never reads the unit state, `drawingScale`, the viewport or the
screen scale [NI §6.1, `AGENTS.md` §10]. Excluded from `E3` [AD]: title
blocks, drawing-sheet sets, paper space, viewports, the `G15` architecture, a
toolbar button, physical text size, a new `GeoElement` type, a new durable
identity mechanism, persistent `ExportArea` state, migration of legacy sheets.

## 2. Command semantics and inputs (`DQ-E3-4`)

One signature, no overloads [ID]:

```text
IsoABorder( <Point>, <ISO A index>, <Landscape>, <Scale numerator>,
            <Scale denominator>, <Model units per millimetre>, <Inner frame> )
```

| # | Input | Type | Valid domain | Meaning |
|---|---|---|---|---|
| 0 | `P` | 2D point | defined, finite | upper-left corner of the physical paper boundary (§4) |
| 1 | `n` | number | integer `0 … 10` exactly (`v == rint(v)`, no rounding) | ISO 216 size `A<n>` [AD `DQ-E3-3`] |
| 2 | `Landscape` | Boolean | `true` / `false` | `true`: width = long side; `false`: width = short side |
| 3, 4 | `a`, `b` | numbers | integers `1 … 10^9` exactly | captured `drawingScale` `a:b` [NI §12; bound = `DrawingScale.MAXIMUM_TERM`, `K11`] |
| 5 | `u` | number | finite, `> 0` | captured model units per millimetre, `10^-3 / fb(effC)` [NI §18.4] |
| 6 | `InnerFrame` | Boolean | `true` / `false` | explicit constructive frame state [AD `DQ-E3-2`, `DQ-E3-4`] |

- Every input is an ordinary construction input; the construction is
  reproducible by typing the command, without the GUI [AD].
- The tool writes literals; literals round-trip bit-exactly through `.cedg`
  save and reopen [CE `K9`, P4]. Typed commands may use any objects of these
  types and then follow them through the DAG.
- The integer pair is preserved exactly as given; only the label uses its
  gcd-normal form; the tool always writes the normal form [ID].

Example (A3 landscape, `mm`, 1:50, frame on):
`IsoABorder(P, 3, true, 1, 50, 1, true)`.

## 3. Outputs, relationships, serialization and identity (`DQ-E3-1`)

| Index | Output | Existing type | Initial presentation | Defined when |
|---|---|---|---|---|
| 0 | `PAPER` — physical paper boundary; authoritative sheet and `ExportArea` rectangle | `GeoPolyLine`, closed by repeating its first vertex | **hidden**; dotted style if the user shows it | all inputs valid (§14) |
| 1 | `FRAME` — inner drawing frame | `GeoPolyLine`, closed by repetition | shown, solid, black, thickness 3 | `PAPER` defined, `InnerFrame = true`, frame valid (§5) |
| 2 | `LABEL` — `A<n> — <a>:<b>` | `GeoText` (`setIsTextCommand`, internal anchor, `setAlwaysFixed`) | shown; independently hideable | `PAPER` defined |

- **Fixed arity** [ID]: the three outputs always exist; disabling the frame
  makes `FRAME` undefined, it does not remove it, so identities survive a
  redefinition of `InnerFrame`.
- **Association** [AD, ID]: every output's parent is the single `IsoABorder`
  algorithm, and its role is its fixed index in that algorithm's output array,
  serialized as the ordered `<output a0 a1 a2>`. This is the existing
  multi-output mechanism (`AlgoNativeDimension`, `K8`); no labels,
  coordinates, proximity, construction indices or new identity infrastructure
  are used.
- **No corner outputs** [AD `DQ-E3-2`]: corners are explicit downstream
  constructions when needed (`Vertex(PAPER, k)`, `K8`); vertices are internal
  unlabelled points owned by the algorithm (β pattern).
- One label names `PAPER`; the others take default labels; labels are
  presentation, not identity.
- **Serialization** [ID]: the ordinary additive form with existing element
  types only:

```xml
<command name="IsoABorder">
	<input a0="P" a1="3" a2="true" a3="1" a4="50" a5="1" a6="true"/>
	<output a0="sheet1" a1="frame1" a2="text1"/>
</command>
```

  Reopen reconstructs the same command and roles from the inputs; no derived
  size is stored; the label string is recomputed. The command stays a
  recognizable constructive operation after save/reopen [AD].
- Creation is one undo point; undo/redo restore the same labels and geometry
  as new Java objects [CE `K9`].

## 4. Physical paper geometry (`DQ-E3-2` A)

- Nominal ISO 216 size [AD]; `(W_p, H_p) = (long, short)` if `Landscape`,
  else `(short, long)`, in millimetres (§6).
- Axis-aligned in **model** coordinates [ID]; `P` is the corner with minimum
  `x` and maximum `y` (model `y` increases upward), as in the legacy macros
  [CE `K5`]; never derived from the viewport.

```text
W = conv(W_p)        H = conv(H_p)                     (§7)
UL = P      UR = (x_P + W, y_P)      LR = (x_P + W, y_P − H)      LL = (x_P, y_P − H)
PAPER vertices: UL, UR, LR, LL, UL                     (clockwise; closed by repetition)
```

- Orientation swaps `W_p` and `H_p` only; nothing is rotated; `P` stays
  upper-left.
- Hidden by default, still part of the construction, still usable as the
  `ExportArea` rectangle [AD `DQ-E3-8`].

## 5. Optional inner frame (`DQ-E3-2` B, `DQ-E3-3`)

Margins [AD], fixed constants of the command semantics, measured on the sheet
as oriented ("left" is the side at `x_P`):

```text
m_L = 20 mm     m_R = 10 mm     m_T = 10 mm     m_B = 10 mm
W_f = W_p − m_L − m_R          H_f = H_p − m_T − m_B          (exact integer mm)
frame valid  ⇔  W_f > 0 and H_f > 0
```

```text
F_UL = (x_P + conv(m_L), y_P − conv(m_T))
F_UR = (F_UL.x + conv(W_f), F_UL.y)
F_LR = (F_UL.x + conv(W_f), F_UL.y − conv(H_f))
F_LL = (F_UL.x,             F_UL.y − conv(H_f))
FRAME vertices: F_UL, F_UR, F_LR, F_LL, F_UL
```

- The frame is built from `W_f`, `H_f` through the same `conv()` and the same
  captured inputs as the paper; there is no independent or approximate
  conversion and no subtraction of converted paper corners [AD].
- The margins follow an ISO 5457-style disposition; **no ISO 5457 compliance is
  claimed** [AD].
- Never silently reduced; an impossible frame is never generated [AD].

## 6. Format table and defaults (`DQ-E3-3`)

ISO 216 nominal sizes (integers) [AD; CE `K4`]; the legacy unrounded series is
not used. Frame dimensions `W_f × H_f` in mm with the approved margins [ID]:

| Size | Paper (short × long) | Landscape frame | Portrait frame | Default `InnerFrame` |
|---|---|---|---|---|
| A0 | 841 × 1189 | 1159 × 821 | 811 × 1169 | on |
| A1 | 594 × 841 | 811 × 574 | 564 × 821 | on |
| A2 | 420 × 594 | 564 × 400 | 390 × 574 | on |
| A3 | 297 × 420 | 390 × 277 | 267 × 400 | on |
| A4 | 210 × 297 | 267 × 190 | 180 × 277 | on |
| A5 | 148 × 210 | 180 × 128 | 118 × 190 | off |
| A6 | 105 × 148 | 118 × 85 | 75 × 128 | off |
| A7 | 74 × 105 | 75 × 54 | 44 × 85 | off |
| A8 | 52 × 74 | 44 × 32 | 22 × 54 | off |
| A9 | 37 × 52 | 22 × 17 | 7 × 32 | off |
| A10 | 26 × 37 | 7 × 6 | **impossible** (`W_f = −4`) | off |

ISO 216 defines paper sizes; framing is a technical-drawing convention applied
on top of it [AD]. A3 landscape is 420 × 297 mm.

## 7. Numerical conversion and invariants (`DQ-E3-5`)

```text
u       = 10^-3 / fb(effC)              captured by the tool         [NI §18.4]
conv(L) = ((L · b) / a) · u             binary64, exactly this order  [AD]
```

- Mathematically `L_model = (L/1000) / (f(c) · a/b) = L · u · b/a` [NI].
- Domain [ID]: `L` ∈ the ISO and margin integers (≤ 1189); `a, b` integers in
  `1 … 10^9`; `u` finite and `> 0`. `L · b ≤ 1.189 · 10^12 < 2^53` is exact;
  `(L·b)/a` is one correctly rounded division; `· u` one more rounding.
- Equivalent pairs give **bit-identical** results (`2:100` and `1:50`): the
  division of exactly represented integers is correctly rounded, so only the
  rational value `b/a` matters [ID]. The label still shows the normal form.
- Overflow and underflow [ID]: if `conv(W_p)`, `conv(H_p)` (or the frame
  values) is not finite or `≤ 0`, or a computed coordinate is not finite, or
  the addition is absorbed (`x_P + W == x_P`, `y_P − H == y_P`, and the same
  for the frame), the affected outputs are undefined (§14); no silent rounding
  or approximation of the paper size [AD].
- Tolerances [ID]: implementation tests compare `conv()` **bit for bit** with
  this expression; against the exact value (exact SI factor, or the stored `k`
  for `usm`) the relative error is ≤ `2^-50` (rounding budget of five
  operations of ≤ `2^-53`); measured: ≤ 1 ulp, 536 of 624 cases correctly
  rounded [CE `K6`]. Physical page checks use ≤ 0.001 pt for PDF and 0.01 mm
  for EMF, as the `C` tests do. The characterization does not prove correct
  rounding for every admissible input, and no such claim is made [AD].
- Reference case: A3 landscape, `mm` (`u = 1`), 1:50 → `21000 × 14850`
  exactly; with `cm` (`u = 0.1`) → `2100 × 1485.0000000000002`, still a
  420 × 297 mm page at 1:50 [CE `K12`].
- Save/reopen reproduces every input and therefore every coordinate
  bit-exactly [CE P4].

## 8. Unit reinterpretation after creation (`DQ-E3-6`)

A later construction-unit change [AD; NI §6.1, §7.2, ADR 0032]: no rescale, no
recompute of `u`, no coordinate change, no redefinition, no identity change.
It reinterprets the physical meaning of the existing model coordinates. The
user is informed through the indicator of §9 when the sheet no longer exports
at its nominal size. No original-unit identifier is persisted: the captured
`u` already determines `f_c = 10^-3 / u` [AD].

## 9. Scale and physical coherence (`DQ-E3-6`, `DQ-E3-12`)

Two **independent** derived checks [AD], computed in presentation/export
orchestration only [NI §6.2], for the **linked** border [ID]:

```text
captured:  n, W_p, H_p, a_c:b_c, u   (inputs)     f_c = 10^-3 / u
current:   effC (unit state), a_n:b_n (session drawingScale)

physical-size coherence   (f_n / f_c) · (s_n / s_c) = 1     s = a/b
  computed directly [ID]: E_i = conv_c(P_i) · fb(effC) · (a_n / b_n) · 1000   mm,  i ∈ {W_p, H_p}
  COHERENT          |E_i − P_i| ≤ 1e-6 mm for both sides
  INCOHERENT        otherwise
  NOT_DETERMINABLE  effC unspecified or invalid, or PAPER undefined         (never "coherent")

scale-label coherence
  MATCH             gcd-normal (a_c:b_c) == gcd-normal (a_n:b_n), exactly
  DIFFERENT         otherwise
  NOT_APPLICABLE    effC unspecified (drawingScale has no engineering meaning, §12)
```

- `E_i` is computed from the captured inputs, never from coordinates, so it is
  independent of `P`. The `1e-6 mm` criterion lies below every output
  quantization (0.001 pt ≈ 3.5 · 10^-4 mm in PDF; 0.01 mm in EMF) and far above
  the binary64 rounding of these magnitudes (≈ 10^-12 mm for 1189 mm) [ID].
- Examples: A3 `mm` 1:50 at session 1:50 → `COHERENT` / `MATCH`; at 1:100 →
  `INCOHERENT` (210 × 148.5 mm) / `DIFFERENT`; after changing the unit to `cm`
  at session 1:50 → `INCOHERENT` (4200 × 2970 mm) / `MATCH`; the same at
  session 1:500 → `COHERENT` / `DIFFERENT` — physical coherence can hold while
  the label scale differs [AD].
- Surfaces [AD]: the status-bar scale segment and the picture, print and LaTeX
  export dialogs show both states and the effective and nominal sizes in the
  current unit and scale.
- **Use sheet scale** [AD]: an explicit user action that sets the session
  `drawingScale` to `a_c:b_c`; offered when the scale state is `DIFFERENT`
  [ID]; it changes no geometry; afterwards both states are recomputed and
  shown; if the physical state stays `INCOHERENT`, the message names the unit
  reinterpretation as the remaining cause and claims no fix.
- **Activation warning** [AD]: when the user activates an `IsoABorder` as the
  export-area producer, physical coherence is evaluated at once; if not
  `COHERENT`, a warning shows the discrepancy and offers "Use sheet scale" when
  applicable; the scale never changes without consent; no geometry is scaled.
- The indicator is `DERIVED_TRANSIENT`: never geometry, never a label change,
  never serialized, never in undo, never exported, no kernel dependency [AD].

## 10. Label semantics (`DQ-E3-11`)

- Output 2; text `"A" + n + " — " + a' + ":" + b'` with `a':b'` the
  gcd-normal captured pair (example `A3 — 1:50`) [AD, ID]; never the session
  scale; independent of the document language, unit state and viewport;
  recomputed in `compute()` from the inputs (no unit listener, `K13`).
- Anchor [ID]: the paper point at `m_R` from the right and `m_B` from the
  bottom, `(x_P + conv(W_p − m_R), y_P − conv(H_p − m_B))`, with the text drawn
  up and to the left of it. With a frame this is the frame's lower-right
  corner; without one it still lies inside the paper for every size. The
  placement never assumes that a frame exists [AD].
- Exported by picture and LaTeX exporters; **absent from DXF**, as every text
  (`K8`) [AD]; no title-block or physical text-size work.

## 11. `ExportArea` producers and session lifecycle (`DQ-E3-8`, `DQ-E3-9`, `DQ-E3-10`)

### 11.1 Participation

Explicit only [AD]: creating a border never replaces the active area; after
creation the tool asks "Use this sheet as the export area?"; a File action
"Use selected ISO A border" links an existing border (any of its outputs
selected, resolved through the parent algorithm). One authority, no second
mechanism, no serialization [AD; NI `AQ-X2`].

### 11.2 Producer model

```text
ExportAreaSession.Producer = EXPORT_POINTS | MANUAL | ISO_A_SELECTION | ISO_A_BORDER
precedence (unchanged)     = explicitly activated producer > Export_1/Export_2 > visible viewport
```

- `ISO_A_BORDER` resolves **live** to the `PAPER` rectangle
  `[x_P, x_P + W] × [y_P − H, y_P]`, never to `FRAME`, whatever its
  visibility [AD]. Bound to Graphics 1, like `MANUAL` [ID, `K10`].
- `ExportArea.Source`, `GeometryExportArea.Producer`, the DXF switch and the
  manifest `resolved_producer` gain both values [ID, `K10`].

### 11.3 Link validity [AD, ID]

The link holds the algorithm. It is **valid** only if an identity (`==`) scan
of the live construction finds that algorithm, its output 0 is the linked
`PAPER` object, and `PAPER` is defined. The stored reference is never trusted
by itself; no label, index, coordinate or comparator-based lookup is used
(`isInConstructionList` is not reliable, `K9`).

| Event | Result |
|---|---|
| the border moves or an input changes value | same objects → link valid; area follows |
| `PAPER` undefined | producer unavailable → fallback, notice |
| deletion of the border | not found → stale |
| undo, redo (any, including unrelated ones) | construction rebuilt, new objects → stale |
| rebuilding redefinition | replaced objects → stale |
| reconstruction, reload, non-native document replacement | replaced objects → stale (also covers `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` routes) |
| New, Open | `ExportAreaSession.resetForDocument` clears it [CE `K10`] |

A stale link never activates another object; `B`'s fallback applies with a
visible notice and the re-link action. No cross-document or persistent
association. This needs no wider architectural change, so the author's stop
condition is not triggered [ID].

### 11.4 Unavailable producer

When unavailable, `resolve` falls back by `B`'s rule and
`isExplicitProducerUnavailable` (today unused, `K10`) feeds the notice; the
area is never silently replaced without it.

### 11.5 `ISO_A_SELECTION` [AD direction; ID unchanged from the preparation]

A separate producer of the same authority, never merged with `ISO_A_BORDER`:
File action `export.area.iso-a`; dialog with the upper-left corner as world
coordinates (prefilled from a single selected point as a copy, never a link),
ISO size and orientation; uses the **current session** `drawingScale` and the
current unit; computed once with the shared helper; stored as values; no
frame; unavailable with an unspecified unit. Coherence presentation after a
later scale change: OTQ-E3-3.

## 12. User interaction (`DQ-E3-7`, `DQ-E3-13`, `DQ-E3-14`)

```text
Construction → Annotations and media → ISO A Border      (menu only; no toolbar button)
```

1. Unit gate [AD]: with `UNSPECIFIED_MODEL_UNIT` the action is unavailable;
   its reason explains why and offers **Document Units…**; nothing is
   inferred; a valid `usm` enables it. The gate also applies to
   `ISO_A_SELECTION`. A typed command stays a pure function of its inputs
   (§6.1); its coherence is then `NOT_DETERMINABLE` [ID].
2. Reference point [AD]: a one-shot mode `MODE_ISO_A_BORDER` (id 144) — click
   an existing point, or an empty position to create a free point. A point
   the tool creates is hidden, because it is a corner of the paper boundary
   (`DQ-E3-2`: hidden corner points; `AQ-G3`), as in the approved workflow; an
   existing point keeps its own presentation. The sheet is moved by dragging
   `FRAME` (or `PAPER` when shown), which translates a free `P` through the
   inherited rule [CE `K8`]; a frameless sheet with hidden `PAPER` is moved by
   editing `P` or by showing `PAPER` (OTQ-E3-4). A single preselected point
   skips the click.
3. Dialog [AD, ID]: ISO size `A0 … A10` (default A3); orientation (default
   landscape); drawing scale as an editable list (1:1, 1:2, 1:5, 1:10, 1:20,
   1:50, 1:100, 1:200, 1:500, 2:1, 5:1, 10:1, and free `a:b`) prefilled with
   the session scale, with `DrawingScale` parsing and inline refusal; **Inner
   frame** checkbox, initially on for A0–A4 and off for A5–A10, re-initialized
   when the size changes, disabled with its reason when the frame is
   impossible (A10 portrait); **Show label** checkbox. The dialog's presets are
   local; `DrawingScale.PRESETS` and the `C` dialogs are unchanged.
4. OK issues one `IsoABorder` command — one undo point [AD]. Cancel removes a
   point the click created.
5. The activation question of §11.1; on yes, the warning of §9 if needed.

Catalog [AD; re-verified at the base]: 127 → **130** actions —
`presentation.iso-a-border` (`upstream-mode`, group
`construction-annotations-media`, no `toolbar_action_ids`),
`export.area.iso-a` and `export.area.use-iso-a-border` (`product-action`, group
`file-export-area`). "Use sheet scale" is a dialog and status action, not a
catalog action. Names: `IsoABorder`, Spanish alias `MarcoISOA`; tool text
"ISO A Border" / "Marco ISO A" through the existing `X.Tool`/`X.Help` keys;
both names are free of built-in commands, aliases and the 24 `Templatev7`
macros [CE `K16`; re-verified]. Classic surface: OTQ-E3-1.

## 13. Persistence classes

| State | Class | Serialized |
|---|---|---|
| command and outputs | `DOCUMENT_SEMANTIC` | additive `<command>`, existing element types (`polyline`, `text`) |
| `a`, `b`, `u`, `InnerFrame`, `n`, `Landscape` | `DOCUMENT_SEMANTIC` | ordinary inputs |
| label string | `DERIVED_TRANSIENT` | recomputed |
| `ISO_A_BORDER` link, `ISO_A_SELECTION` rectangle | `SESSION` | never; reset on New/Open; untouched by undo |
| coherence states, warnings | `DERIVED_TRANSIENT` | never |

## 14. Export and legacy compatibility (`DQ-E3-15`)

- Picture formats, print, clipboard, `--export` and `ExportImage` draw the
  outputs through the shared drawables; LaTeX through `drawPolyLine` and text;
  DXF writes each polyline as an open `LWPOLYLINE` with the closing vertex
  repeated, and no text [CE `K8`; recorded as a durable limitation].
- Documents without the command re-save byte-identically. A build that does
  not know `IsoABorder` drops its outputs and dependents with a load error and
  loses them on re-save [CE `K9`] — a documented forward-compatibility
  limitation, stated in the guides.
- Legacy sheets [AD]: `sheetISOAnLand`, `sheetISOAnVert` stay behavioral
  references with their provenance; `Templatev7.ggb` and historical baselines
  are unchanged; no migration; their formula, margins and title block are not
  promoted; documents using them load and behave as before; coexistence with
  document macros follows the approved resolution rules (ADR 0031 decision 6).

## 15. Degenerations

Failures are deterministic; outputs become undefined and are never removed or
replaced; valid inputs restore them with the same identities.

| Condition | `PAPER` | `FRAME` | `LABEL` |
|---|---|---|---|
| `P` undefined, non-finite or 3D | undefined | undefined | undefined |
| `n` not an integer in `0 … 10` (`3.5`, `−1`, `11`, NaN) | undefined | undefined | undefined |
| `a` or `b` not an integer in `1 … 10^9` | undefined | undefined | undefined |
| `u ≤ 0`, NaN or infinite | undefined | undefined | undefined |
| `conv(W_p)`, `conv(H_p)` non-finite or `≤ 0`; paper coordinates non-finite or absorbed | undefined | undefined | undefined |
| `InnerFrame = false` | defined | undefined | defined |
| frame impossible (`W_f ≤ 0` or `H_f ≤ 0`, A10 portrait) | defined | undefined | defined |
| frame coordinates non-finite or absorbed | defined | undefined | defined |
| all valid | defined | defined iff `InnerFrame` | defined |

The tool never creates an impossible frame; a typed command with
`InnerFrame = true` for A10 portrait yields an undefined `FRAME`.

## 16. Test matrix (for the future implementation)

| Id | Cases | Expected |
|---|---|---|
| `T-ISO-TABLE` | 11 sizes × 2 orientations, `mm`, 1:1 | paper `W`, `H` equal the ISO integers exactly |
| `T-FRAME` | every size × orientation, `InnerFrame` on/off | frame per §5–§6; A10 portrait impossible; no zero or negative frame; paper unaffected |
| `T-MARGINS` | A3, A4 | frame offsets `conv(20)`, `conv(10)`, size `conv(W_f) × conv(H_f)` bit-identical; no conversion other than `conv()` |
| `T-CONVERSION` | `mm`, `cm`, `m`, `usm` (inch; `k = fb(mm)`) × 1:1, 1:2, 1:10, 2:1, 1:50; `2:100` against `1:50` | bit identity with §7; relative error ≤ `2^-50`; equivalent pairs bit-identical |
| `T-REFERENCE-CASE` | A3 landscape, `mm`, 1:50 | `21000 × 14850` exactly; label `A3 — 1:50` |
| `T-REFERENCE-POINT` | `P` = (0,0), (−1234.5, 987.25), (10^6, −10^6) | corners per §4; vertex order; axis-aligned |
| `T-ORIENTATION` | landscape / portrait | width long / short; `P` upper-left; no rotation |
| `T-SIGNATURE` | the seven-argument signature; wrong counts and types | typed argument errors; no overload |
| `T-MOVE-DAG` | move free `P`; drag `PAPER` / `FRAME`; `P` dependent | translation only; same objects; one undo point per drag; dependents of `PAPER` (e.g. `Vertex`) follow |
| `T-UNIT-CHANGE` | `mm → cm`, `→ usm`, `→ unspecified` after creation | coordinates and XML numbers unchanged; label unchanged; coherence states per §9; undo restores |
| `T-SESSION-SCALE` | session 1:50 → 1:100 → 1:1 | geometry unchanged; states per §9; PDF page = paper × session/captured |
| `T-COHERENCE` | the four examples of §9; `usm`; unspecified unit; `PAPER` undefined | the two states exactly; never `COHERENT` when not determinable |
| `T-SCALE-ACTION` | "Use sheet scale" with scale-only and unit-caused mismatches | session scale set only on activation; geometry unchanged; message states the remaining cause |
| `T-ACTIVATION-WARNING` | linking coherent and incoherent sheets | warning shown only when not `COHERENT`; no scale change without consent |
| `T-EXPORT-CONSISTENCY` | linked border, session = captured: PNG, PDF, SVG, EMF/EMF+, print preview, clipboard, `--export`, `ExportImage`, PSTricks, PGF, Asymptote, DXF | page = ISO size (PDF ≤ 0.001 pt, EMF 0.01 mm, PNG pixel rounding); LaTeX bounds = paper; DXF `resolved_producer` = `iso_a_border`; exports never change the document |
| `T-PRODUCERS` | each producer combination with `Export_1`/`Export_2` and the viewport | `AQ-X2` precedence unchanged; the area is `PAPER`, never `FRAME`, also while hidden; `ISO_A_SELECTION` and `ISO_A_BORDER` never active together |
| `T-LINK-LIFECYCLE` | every event of §11.3 | validity per table; a stale link never activates another object; fallback with notice; re-link works |
| `T-ISO-SELECTION` | each unit and session scale | values computed once with the shared helper; page = ISO size at the session scale; unit gate |
| `T-UNDO-REDO` | create; undo; redo | one undo point; same labels and geometry |
| `T-SAVE-REOPEN` | save `.cedg`, reopen | `<command name="IsoABorder">` with literal inputs; element types `polyline`, `text` only; byte-identical round trip; geometry fingerprint equal; `ExportArea` reset; `drawingScale` `1:1` |
| `T-INVALID` | every row of §15 | per table; identities kept; recovery; no exception |
| `T-NO-UNIT` / `T-USM` | unspecified unit; valid `usm` | tool and `ISO_A_SELECTION` blocked with the Units route; `usm` behaves as `mm`/`cm`/`m` |
| `T-PRESENTATION` | visibility, style, layer of each output | no coordinate change; `PAPER` hidden; tool-created `P` hidden; no corner points |
| `T-LABEL` | `2:100` typed; EN/ES; unit change; frame on/off | `A3 — 1:50`; language- and unit-independent; anchor per §10; DXF without text |
| `T-TOOL` | dialog defaults per size; frame checkbox disabled for A10 portrait; cancel | defaults per §12; no invalid frame; cleanup on cancel |
| `T-GATES` | ADR 0031 pins, GGBScript matrix (6 rows per syntax line, 14 probes), R5-B fingerprints, `SelfTest`, profile pins 127 → 130 | pins moved exactly as declared; negative controls still fail closed |
| `T-LEGACY` | `Templatev7.ggb`; g9p documents with `sheetISOAnLand` | hash unchanged; documents load and round-trip unchanged |
| `T-DETERMINISM` | the same border twice in fresh documents | identical XML |

## 17. Verification classification (`DQ-E3-16`)

Future implementation: **`INTEGRATED_PHASE`** [AD] — one registered `PHASE`
(`PRE-G9B-R6-PLUS-E3`) plus `INTEGRATION` on the same commit and tree.
Concrete integration coverage: a shared command through the ADR 0031 gates,
the GGBScript matrix and the R5-B fingerprints; two producers of the single
`ExportArea` consumed by every picture route, print, clipboard, command line,
`ExportImage`, LaTeX and the DXF manifest; the unit and `drawingScale`
cross-cuts; profile pins in nine test sites. Reclassify or stop [AD] on a new
geometric entity type, a new structural XML element, persistent `ExportArea`
state, unplanned global rendering or geometry changes, a material widening of
compatibility, or unjustified new architectural infrastructure. This
documentary closeout itself is `DOCUMENTATION_STATUS_ONLY`.

## 18. Unresolved technical details

| Id | Detail | Blocks implementation authorization |
|---|---|---|
| `OTQ-E3-1` | Classic surface of the command: follow the `E2` precedent (ungated shared command; no Classic menu, tool, producer or indicator) or gate it out of Classic | **yes** — the author chooses at implementation authorization (recommended default: the `E2` precedent) |
| `OTQ-E3-2` | Algebra View presentation of an undefined `FRAME` (shown as undefined, or marked auxiliary by the tool) | no |
| `OTQ-E3-3` | coherence presentation for an `ISO_A_SELECTION` rectangle after a later scale change | no; none in `E3` without a further decision |
| `OTQ-E3-4` | a frameless sheet (default for A5–A10) shows only its label; with `PAPER` and a tool-created `P` hidden there is no visible draggable output, so it is moved by editing `P` or by showing `PAPER` (a consequence of `DQ-E3-2`, `DQ-E3-3` and `DQ-E3-13`, not a new rule) | no; the author may revisit it at implementation authorization |

## 19. Decision traceability and superseded statements

| Id | Disposition (2026-10-10) | Sections |
|---|---|---|
| `DQ-E3-1` | R2 approved | §3 |
| `DQ-E3-2` | inner frame 20/10/10/10 mm; paper hidden | §4, §5 |
| `DQ-E3-3` | A0–A10; frame default on A0–A4, off A5–A10 | §6 |
| `DQ-E3-4` | explicit inputs; one seven-argument signature | §2 |
| `DQ-E3-5` | `((L·b)/a)·u` accepted, bounded | §7 |
| `DQ-E3-6` | geometry unchanged; two coherence checks | §8, §9 |
| `DQ-E3-7` | creation blocked when unspecified | §12 |
| `DQ-E3-8` | explicit participation; paper boundary | §11.1–§11.2 |
| `DQ-E3-9` | live, explicit, session-scoped link | §11.3 |
| `DQ-E3-10` | separate `ISO_A_SELECTION` producer | §11.5 |
| `DQ-E3-11` | constructive label | §10 |
| `DQ-E3-12` | extended indicator, "Use sheet scale", activation warning | §9 |
| `DQ-E3-13` | characterized workflow with frame option | §12 |
| `DQ-E3-14` | `IsoABorder` / `MarcoISOA`; 127 → 130 | §12 |
| `DQ-E3-15` | no migration | §14 |
| `DQ-E3-16` | `INTEGRATED_PHASE` | §17 |

Not selected (kept only as the record of the preparation alternatives): R1
segments, R3 polygon, B dedicated type; trim-only and title-block sheet
contents; a uniform 10 mm margin argument; automatic or absent `ExportArea`
participation; snapshot, freeze, label re-resolution and persisted links;
rescaling on a unit change.

Superseded: plan §8.6 "model length = paper length × scale denominator ÷
conversion(constructionUnit → mm)" (by `unit-system.md` §18.4 and §7 here);
`P0` candidate §3 outputs, §5 profile lines and "+2" pin (by §3, §12); `P0`
candidate §4 linkage (refined by §11.3).

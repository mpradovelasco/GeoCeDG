# PRE-G9B-R6-plus-E3 — native `IsoABorder` and `ExportArea` integration: reconciled design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED.** Every value below
  that no durable specification already fixes is a proposal until the author
  disposes of the matching `DQ-E3` question (§14). Recommendations are not
  author decisions.
- Produced by: `PRE-G9B-R6-plus-E3-PREP` (characterization, design
  reconciliation and preparation, 2026-10-09); no implementation
- Base: `P_R6PLUS_POST_E2_P4` = `13e08ac1fe1c6c931a88985b97df93d8445812b9`
  (tree `dd2ba17c483a18b8931e1848f38616edfa98b06a`)
- Supersedes, where they differ: the `P0`
  [design candidate](pre_g9b_r6_plus_e3_iso_a_border_design_candidate.md)
  (kept unchanged as `P0` evidence) and the stale facts of the
  [mini-track plan](pre_g9b_r6_plus_minitrack_plan.md) §8.6 listed in §15
- Evidence: [characterization report](../validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md)
  (`K0`–`K21`) and its JSON mirror
- Execution contract when authorized: [canonical `E3` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md)
- Normative inputs it implements, not restates: [unit-system specification](../../geocedg/specs/units/unit-system.md)
  §5.3, §6, §7.3, §12, §13, §17, §18.4; [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md);
  [B author decisions](../validation/pre_g9b_r6_plus_b_author_decisions_record.md)
  `AQ-X2` and `AQ-X7`; [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md);
  [ADR 0034](../adr/0034-native-dimension-representation-and-presentation-seams.md)
  (β precedent); `AGENTS.md` §1, §4, §10

This file creates no normative contract. The implementing phase turns it into
the durable specification named in the prompt.

## 1. Placement

| Responsibility | Owner | Reason (`AGENTS.md` §4) |
|---|---|---|
| `IsoABorder` command, algorithm, ISO 216 table, canonical conversion, degeneracies | **shared Java kernel** (`org.geocedg.common.kernel.sheet`, proposed) | DAG dependency on a construction point, dynamic evaluation, persistence, every frontend |
| the ISO table and the conversion as one pure helper also used by `ISO_A_SELECTION` | shared kernel helper | one source of numeric truth; Desktop must not duplicate it |
| tool mode, point pick, dialog, creation-time capture of `u` and `a:b`, unit gate, yes/no linkage question | GeoCeDG Desktop | tool orchestration and §6.3 capture |
| `ISO_A_BORDER` and `ISO_A_SELECTION` producers, link lifecycle, staleness test | GeoCeDG Desktop `ExportAreaSession` (and the DXF producer mapping) | session export state, never geometry (`B`, `AQ-X2`) |
| scale-coherence indicator | GeoCeDG Desktop (status bar, export dialogs) | transient presentation; never exported or persisted |
| rendering and export of the outputs | existing drawables and export adapters | adapters consume ordinary outputs; no geometric authority |

No responsibility is duplicated in Desktop or Python. The command never reads
the unit state, `drawingScale`, the viewport or the screen scale
(`unit-system.md` §6.1, `AGENTS.md` §10).

## 2. Semantic model

### 2.1 Definition

`IsoABorder` is an ordinary dependent construction (an algorithm with existing
output types) that represents, in model coordinates, the trimmed outline of an
ISO 216 A-series sheet drawn at a captured drawing scale for a captured
physical construction unit. It is a **model-space annotation**, not `G15`
paper space (plan §1.1).

### 2.2 Inputs and domain (proposed signature, `DQ-E3-4`)

```text
IsoABorder( <Point P>, <ISO A index n>, <Landscape>, <Scale numerator a>,
            <Scale denominator b>, <Model units per millimetre u>
            [, <Margin in paper millimetres m>] )          [m only if DQ-E3-2 (b)]
```

| Input | Type | Domain (valid) | Meaning |
|---|---|---|---|
| `P` | 2D point (`GeoPoint`) | defined, finite | upper-left corner of the trimmed sheet (§2.3) |
| `n` | number | integer `0 … 10`, exactly (no rounding) | ISO 216 size `A<n>` (`DQ-E3-3`) |
| `Landscape` | Boolean | `true` / `false` | `true`: width = long side; `false`: width = short side |
| `a`, `b` | numbers | integers `1 … 10^9`, exactly | captured `drawingScale` `a:b` (`unit-system.md` §12; bound = `DrawingScale.MAXIMUM_TERM`) |
| `u` | number | finite, `> 0` | captured model units per millimetre of physical length, `10^-3 / fb(effC)` at creation (§18.4) |
| `m` | number | finite, `0 ≤ m < short/2` | frame margin in paper millimetres (only under `DQ-E3-2` (b)) |

The tool writes the inputs as literals (`DQ-E3-4`); typed commands may use
any objects of these types, and the border then follows them through the DAG.

### 2.3 Coordinate convention and reference point

- The border is **axis-aligned in model coordinates**. "Upper-left" is defined
  in the model frame, never from the viewport: `P` is the corner with the
  minimum `x` and the maximum `y` (GeoGebra model `y` increases upward), as in
  the legacy macros (`K5`).
- With `(W_p, H_p) = (long, short)` if `Landscape` else `(short, long)` in
  paper millimetres, and the canonical conversion `conv(L) = ((L · b) / a) · u`
  (§3.2):

```text
W = conv(W_p)      H = conv(H_p)
UL = P             UR = (x_P + W, y_P)
LR = (x_P + W, y_P − H)          LL = (x_P, y_P − H)
trim vertices, in order:  UL, UR, LR, LL, UL      (clockwise; closed by repetition)
```

- Orientation swaps `W_p` and `H_p` only. It never changes the sign or the
  direction of the construction and never rotates anything; `P` stays the
  upper-left corner in both orientations.
- Moving `P` translates every output by the same vector; sizes are not
  recomputed from anything but the inputs.

### 2.4 Outputs (recommended, `DQ-E3-1`, `DQ-E3-2`, `DQ-E3-11`)

| Index | Output | Type (existing) | Initial presentation | Present when |
|---|---|---|---|---|
| 0 | `TRIM` — trimmed sheet edge, the `ExportArea` rectangle | `GeoPolyLine`, closed by repetition | hidden; dotted (type 20) if shown | always |
| 1 | `FRAME` — drawing frame inset by `m` | `GeoPolyLine`, closed by repetition | shown; solid, black, thickness 3 | `DQ-E3-2` (b) |
| 2 | `LABEL` — `A<n> — <a>:<b>` | `GeoText` (`setIsTextCommand`, internal anchor, `setAlwaysFixed`) | shown; hideable | `DQ-E3-11` (a) |

- No corner point is an output. Corners are explicit downstream constructions
  when needed (`Vertex(TRIM, k)`, `K8`), which satisfies the author
  requirement that corner points are hidden in the sheet presentation
  (`AQ-G3`) without creating hidden objects.
- Polyline vertices are internal unlabelled points owned by the algorithm
  (the `E2` β pattern, `K8`).
- One label names the primary output `TRIM`; the others take default labels
  (`E2` precedent). Labels are presentation, not identity: identity is the
  output index of the algorithm (`K9`).

### 2.5 Dependencies, validity and degenerations

The outputs depend on the inputs only. Every failure is deterministic and keeps
the output objects (they become undefined; no output is removed or replaced).

| Condition | `TRIM` | `FRAME` | `LABEL` |
|---|---|---|---|
| `P` undefined, non-finite or 3D | undefined | undefined | undefined |
| `n` not an integer in `0 … 10` (including `3.5`, `−1`, `11`, NaN) | undefined | undefined | undefined |
| `a` or `b` not an integer in `1 … 10^9` | undefined | undefined | undefined |
| `u` ≤ 0, NaN or infinite | undefined | undefined | undefined |
| a computed `W` or `H` not finite (overflow) | undefined | undefined | undefined |
| `m` < 0, non-finite, or `m ≥ short/2` | defined | undefined | undefined |
| `m = 0` | defined | coincides with `TRIM` | defined |
| all valid | defined | defined | defined |

Returning an input to its valid domain restores the outputs with the same
identities. An undefined `TRIM` makes an `ISO_A_BORDER` producer unavailable
(§7.4).

### 2.6 Identity, lifecycle, persistence and reconstruction

- Creation is one undo point. Undo removes the algorithm and its outputs; redo
  restores them with the same labels and geometry, as new Java objects (`K9`).
- Serialization is the ordinary additive form, with existing element types
  only (`polyline`, `text`):

```xml
<command name="IsoABorder">
	<input a0="P" a1="3" a2="true" a3="1" a4="50" a5="1" a6="10"/>
	<output a0="sheet1" a1="frame1" a2="text1"/>
</command>
```

- Reopen reconstructs the same semantic construction from the inputs; no
  derived size is stored. The label string is recomputed, not trusted from the
  file.
- A build that does not know `IsoABorder` drops its outputs and dependents with
  a load error (`K9`): a documented forward-compatibility limitation, as for
  `E2`.
- Old documents (including those using `sheetISOAnLand`/`sheetISOAnVert`) are
  unaffected; there is no migration (`DQ-E3-15`).

## 3. ISO sizes, units and scale

### 3.1 Size set

ISO 216 nominal A series, short × long, millimetres (`K4`): A0 841×1189, A1
594×841, A2 420×594, A3 297×420, A4 210×297, A5 148×210, A6 105×148, A7 74×105,
A8 52×74, A9 37×52, A10 26×37. The legacy unrounded series is not used
(`DQ-E3-3`).

### 3.2 Canonical conversion (`DQ-E3-5`)

```text
conv(L_mm) = ((L_mm · b) / a) · u          binary64, evaluated in exactly this order
u          = 10^-3 / fb(effC)              captured by the tool (unit-system §18.4)
```

- Mathematically equal to `(L/1000) / (f(c) · (a/b))` (`K6`).
- `L_mm · b` is exact for every ISO length and `b ≤ 10^9`; the result has at
  most two roundings after the captured `u`. Over 624 measured cases it is the
  correctly rounded value in 536 and within 1 ulp in all (`K6`, S2).
- Tests compare against this expression bit for bit, and against the exact
  decimal value within 1 ulp (2 ulp for `usm`, whose `k` is itself a stored
  binary64). No exactness beyond binary64 is claimed.
- Reference case: A3, `mm` (`u = 1`), 1:50 → `21000 × 14850` exactly
  (landscape). With `cm` (`u = 0.1`) the same sheet is `2100 ×
  1485.0000000000002`, and its export at 1:50 is still a 420 × 297 mm page
  (`K12`).

### 3.3 Captured versus live

| Quantity | Role in `E3` |
|---|---|
| captured `a:b`, `u` | ordinary construction inputs; the only physical information the border carries |
| session `drawingScale` | default of the tool's scale entry, read once; read live only by export adapters and by the coherence indicator |
| export physical scale | `fb(effC) · 100 · a/b` of the **session** (`C`); never read by the command |
| viewport zoom | never used for any physical size |

The tool never changes the session `drawingScale`; the indicator may offer an
explicit user action to do so (`DQ-E3-12`).

### 3.4 Later construction-unit change (`AQ-E3c`, `DQ-E3-6`)

| Option | Behavior | Compatibility with approved contracts |
|---|---|---|
| (a) preserve model geometry | coordinates unchanged; the border now represents another paper size (`u` no longer equals `10^-3/fb(effC)`); the label still names the captured size and scale; the indicator reports the unit-factor mismatch | the only option compatible with `unit-system.md` §6.1 and §7.2 |
| (b) preserve the paper interpretation | `u` recomputed, border rescaled, dependents move | violates §6.1 (a unit change must not alter numeric geometry); needs a normative amendment |
| (c) recompute model-space dimensions | same as (b) | as (b) |

Recommended: (a), with no automatic re-capture in `E3`. A user who wants the
new physical meaning redefines `u` explicitly.

## 4. Representation alternatives (`DQ-E3-1`)

| Criterion | R1: four `GeoSegment`s (+ corner points) — `P0` | **R2: closed `GeoPolyLine`(s)** — recommended | R3: `GeoPolygon` (+ sides, corners) | B: dedicated `GeoElement` |
|---|---|---|---|---|
| dependency correctness | yes | yes | yes | yes |
| geometric identity | 4–8 objects per rectangle | one object per rectangle | 1 + 4 + 3 objects | one object |
| inherited drag moves `P` | **no** (`K8`) | **yes** (`K8`) | yes (polygon only) | needs new code |
| semantics added | none | a path (length value) | a **region**, fill, area value (`3.1185E8`), point-in-region | new type |
| hit testing | edges | edges | edges if unfilled; interior if filled | new code |
| deterministic persistence | additive `<command>` | additive `<command>`, element `polyline` | additive `<command>` | **new XML element type** |
| undo/redo, dynamic update | ordinary | ordinary | ordinary | new code |
| construction-order behavior | ordinary | ordinary | ordinary | new code |
| old documents | unaffected | unaffected | unaffected | unaffected; older builds lose more |
| picture / LaTeX | yes | yes (`drawPolyLine`) | yes | new adapters in every exporter |
| DXF | 4 `LINE` | one open `LWPOLYLINE`, closing vertex repeated | one closed `LWPOLYLINE` | new mapping |
| upstream modifications | command registration only | command registration only | command registration only | `GeoClass`, XML handler, drawable factory, exporters |
| verification class | `INTEGRATED_PHASE` | `INTEGRATED_PHASE` | `INTEGRATED_PHASE` | `GLOBAL_IMPACT` |

R2 is the least invasive representation that keeps one traceable object per
rectangle, drags as a whole, and adds no region semantics. Its only fidelity
cost is that DXF writes the closing vertex instead of the closed flag; that
is accepted as a durable limitation unless the author prefers R3. No CAD sheet
feature or feature tree is introduced.

## 5. Sheet content (`AQ-E2`, `DQ-E3-2`)

The `P0` recommendation "border only" (`AQ-E2`) and the author requirement "the
outer dotted rectangle and all corner points are hidden in the final sheet
presentation" (`AQ-G3`) cannot both hold if the trimmed edge is the only
output: the only geometry would be hidden (`K5`,
`OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY`).

| Option | Outputs | Preserves `AQ-G3` | New inputs |
|---|---|---|---|
| (a) trim only, visible solid | `TRIM`, `LABEL` | only by reinterpreting it (there is no separate outer rectangle) | none |
| **(b) trim hidden + frame visible** — recommended | `TRIM` (hidden, dotted), `FRAME`, `LABEL` | yes, literally | margin `m` |
| (c) (b) + legacy title-block grid | adds ~10 segments | yes | margin |

(c) is excluded: title blocks belong to `G15` (plan §1.1). For (b) the margin
rule is a sub-decision:

| Margin rule | Note |
|---|---|
| **(b1) one uniform margin `m` in paper millimetres, captured like the scale (default 10 mm)** — recommended | matches the legacy reference (`CeDGMargin`, uniform); no unverified normative claim |
| (b2) ISO 5457 frame margins (commonly 20 mm filing margin on the left, 10 mm elsewhere) | the standard text is not in the repository; the author must confirm it; normative only for A0–A4 |
| (b3) no margin (`m = 0`) | the frame coincides with the trim |

## 6. ISO size set and input forms

- `DQ-E3-3`: A0–A10 (the complete ISO 216 A series; recommended) or A0–A4
  (the usual technical-drawing sheets).
- `DQ-E3-4`: integer index `n` (recommended) rather than a text `"A3"`;
  Boolean `Landscape` (recommended) rather than a number or text; the scale as
  an integer pair `a, b` (recommended, `AQ-E3a`) rather than one ratio; the
  tool writes literals (recommended) rather than hidden auxiliary numbers.
  The command accepts any pair; the label shows its gcd-normal form, and the
  tool always writes the normal form.

## 7. `ExportArea` integration

### 7.1 Participation (`DQ-E3-8`)

| Option | Effect | Compatibility |
|---|---|---|
| (A) automatic active producer on creation | every new border silently becomes the export area | conflicts with `AQ-X2` ("explicitly activated") and with the rule that visible geometry never redefines the export region silently |
| **(B) explicit only** — recommended | the yes/no question after creation (author-fixed scope, plan §2) and a File action "Use selected ISO A border as export area" | consistent with `AQ-X2`; precedence unchanged |
| (C) none | the border is ordinary geometry; the user defines a `MANUAL` area by hand | loses author-fixed scope |

### 7.2 Producer model

```text
ExportAreaSession.Producer = EXPORT_POINTS | MANUAL | ISO_A_SELECTION | ISO_A_BORDER
precedence (unchanged)     = explicitly activated producer > Export_1/Export_2 > visible viewport
```

- One explicit producer at a time, as today (`K10`). `ISO_A_BORDER` resolves
  live to the `TRIM` rectangle `[x_P, x_P + W] × [y_P − H, y_P]`, never to the
  frame. `ISO_A_SELECTION` stores values, like `MANUAL`.
- Both are bound to Graphics 1, like `MANUAL` (`K10`).
- `ExportArea.Source`, `GeometryExportArea.Producer`, the DXF switch and the
  manifest `resolved_producer` gain the two values (`K10`).
- The border is reached from a selected output through its **parent
  algorithm and output index** (`TRIM` = index 0) only; never through labels,
  coordinates, proximity, construction indices or XML position. Visibility,
  style or selection state of any output changes nothing.

### 7.3 Link lifecycle (`AQ-E3b`, `DQ-E3-9`)

| Option | On undo, redo, Open, rebuilding redefinition, deletion |
|---|---|
| **(b1) live link; dropped when stale; fallback by `B`'s rule; visible notice and a re-link action** — recommended (`P0`) | the next export uses `Export_1`/`Export_2` or the viewport; the user is told |
| (b2) snapshot at activation as a value rectangle | never follows later moves of the border |
| (b3) live link; when stale, freeze the last resolved rectangle as `MANUAL` with a notice | the area stays sheet-sized but no longer follows a border that may not exist |
| (b4) re-resolve by label | **rejected**: label-based inference |
| (b5) persist a document flag on the border | **rejected**: `DOCUMENT_PRESENTATION`, contrary to the author's `SESSION` preference |

Implementation obligation for any live option: the session has no rebuild
signal and no element field today (`K10`), and
`Construction.isInConstructionList` answers `true` for stale objects (`K9`).
The staleness test must be an identity (`==`) check of the linked algorithm or
output against the live construction, or a dedicated rebuild notification;
never a comparator-, index-, label- or position-based lookup. If no such test
is possible, the phase stops for an author decision.

### 7.4 Unavailable producer

When the linked `TRIM` is undefined or stale, the producer is unavailable;
`resolve` falls back by `B`'s rule and the existing
`isExplicitProducerUnavailable` (`K10`, today unused) feeds the notice. The
area is never silently replaced by the viewport without that notice.

### 7.5 `ISO_A_SELECTION` (`DQ-E3-10`)

Delivered by `E3` (`AQ-X7`, author-decided). Proposed design: a File action
`export.area.iso-a` with a dialog (upper-left corner as world coordinates —
prefilled from a single selected point as a copy, never a link — ISO size,
orientation), using the **current session** `drawingScale` and the current
unit, computed once with the shared conversion helper; unavailable with an
unspecified unit. The resulting page is the ISO size at the session scale.

## 8. Label and scale coherence (`ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE`)

### 8.1 Persistent construction label (`DQ-E3-11`)

- (a) recommended: output `LABEL` of the algorithm, text
  `"A" + n + " — " + a' + ":" + b'` with `a':b'` the gcd-normal form;
  independent of locale, unit state, session scale and viewport; recomputed in
  `compute()` (no unit listener, `K13`); anchored at the frame's lower-right
  corner, drawn inside the frame; the same in both orientations; hideable;
  exported by picture and LaTeX exporters; absent from DXF (as `E2` text);
  text size is not physical (`TD-PDF-1`, planned `F4`).
- (b) no label output (the user adds an ordinary text).
- (c) a separate command `IsoABorderLabel(<border>)`.

### 8.2 Transient consistency indicator (`DQ-E3-12`)

Compares the **linked** border's captured `a:b` (and its `u` against
`10^-3/fb(effC)`) with the session `drawingScale` (and the current unit). It is
never construction geometry, never exported, never persisted, never a document
dependency, and never changes either scale by itself.

| Surface | Proposal |
|---|---|
| status bar | the display-only scale segment adds a mismatch marker, e.g. `1:100 ≠ sheet 1:50` (recommended) |
| picture, print and LaTeX export dialogs | a non-blocking notice with both scales and an explicit button "Use sheet scale 1:50" that sets the session scale (recommended) |
| view overlay | a non-exported note near the border (alternative) |
| none without a link | with several borders and no link, nothing is compared (recommended) |

## 9. User workflow (`DQ-E3-13`)

```text
Construction → Annotations and media → ISO A sheet border…   (menu only; no toolbar button)
```

1. Gate: with `UNSPECIFIED_MODEL_UNIT` the action is unavailable; the reason
   explains it and offers **Document Units…** (`unit-system.md` §18.4;
   `DQ-E3-7`). Nothing is inferred.
2. A one-shot mode (`MODE_ISO_A_BORDER`, proposed id 144): click an existing
   point, or click an empty position to create a free point (created hidden,
   per `AQ-G3`; the sheet is moved by dragging its frame, `K8`). A single
   preselected point skips the click.
3. Dialog: size `A0 … A10` (default A3); orientation (default landscape);
   drawing scale as an editable list (1:1, 1:2, 1:5, 1:10, 1:20, 1:50, 1:100,
   1:200, 1:500, 2:1, 5:1, 10:1, and free `a:b`), prefilled with the session
   scale; margin in mm (default 10) under `DQ-E3-2` (b); "Show label". Invalid
   entries are refused inline (the `DrawingScaleControl` pattern) and OK stays
   disabled. The E3 preset list is local; the export dialogs' presets are not
   changed.
4. OK issues one `IsoABorder` command (one undo point). Cancel removes a point
   the click created.
5. Yes/no question: "Use this sheet as the export area?" (author-fixed scope).
   Yes activates `ISO_A_BORDER` for this border.
6. Later linkage: File → Export area → "Use selected ISO A border" (any output
   of a border selected).

No physical size depends on mouse distances. No page-layout capability is
added.

## 10. Persistence and compatibility

| State | Class | Serialized |
|---|---|---|
| `IsoABorder` command and outputs | `DOCUMENT_SEMANTIC` | additive `<command>`, existing element types |
| captured `a`, `b`, `u` (and `m`) | `DOCUMENT_SEMANTIC` | ordinary inputs |
| label string | `DERIVED_TRANSIENT` | recomputed (the text element carries only style) |
| `ISO_A_BORDER` link, `ISO_A_SELECTION` rectangle | `SESSION` | never; reset on New/Open; untouched by undo |
| coherence indicator | `DERIVED_TRANSIENT` | never |
| E3 dialog defaults | none in `E3` | — |

## 11. Ownership summary

Kernel: command, algorithm, ISO table, conversion helper, degeneracies,
persistence. Desktop: mode, dialog, capture, unit gate, linkage question, File
actions, `ExportAreaSession` producers and staleness, indicator. Export
adapters: unchanged rendering of polylines and text, the producer value in the
DXF manifest. Python: none.

## 12. Proposed validation matrix

| Id | Cases | Expected |
|---|---|---|
| `T-ISO-TABLE` | 11 sizes × 2 orientations, `mm`, 1:1 | `W`, `H` equal the ISO integers exactly |
| `T-UNITS-SCALES` | `mm`, `cm`, `m`, `usm` (inch, and `k = fb(mm)`) × 1:1, 1:2, 1:10, 2:1, 1:50 | `conv` bit-identical to §3.2; within 1 ulp of exact (2 for `usm`) |
| `T-REFERENCE-CASE` | A3, `mm`, 1:50, landscape | `21000 × 14850` exactly; `LABEL` = `A3 — 1:50` |
| `T-REFERENCE-POINT` | `P` = (0,0), (−1234.5, 987.25), (10^6, −10^6) | corners per §2.3; vertex order UL, UR, LR, LL, UL; axis-aligned |
| `T-ORIENTATION` | landscape / portrait | width = long / short; `P` upper-left in both; no rotation |
| `T-MOVE` | move free `P`; drag `FRAME`; `P` dependent (point on a line) | translation only; same Java objects and labels; drag of `FRAME` moves `P` with one undo point; a dependent `P` follows its parents |
| `T-UNIT-CHANGE` | `mm → cm`, `→ usm`, `→ unspecified` after creation | coordinates and XML numbers unchanged (§6.1); label unchanged; indicator reports the unit-factor mismatch; undo restores |
| `T-SESSION-SCALE` | session 1:50 → 1:100 → 1:1 | geometry unchanged; indicator states; PDF page = border × session/captured |
| `T-EXPORT-CONSISTENCY` | linked border, session = captured; PNG, PDF, SVG, EMF/EMF+, print preview, clipboard, `--export`, `ExportImage`, PSTricks, PGF, Asymptote, DXF | page = ISO size (PDF ≤ 0.001 pt; EMF 0.01 mm; PNG pixel rounding); LaTeX bounds = trim; DXF `resolved_producer` = `iso_a_border`; the document is unchanged by every export |
| `T-EXPORT-OTHER-SCALE` | linked border, session ≠ captured | scaled page; notice shown; no scale changed without the explicit button |
| `T-UNDO-REDO` | create; undo; redo; undo of an unrelated edit while linked | one undo point; same labels and geometry; link handled per `DQ-E3-9` with a notice; session scale unchanged |
| `T-SAVE-REOPEN` | save `.cedg`, reopen | `<command name="IsoABorder">` with literal inputs; element types `polyline`, `text` only; byte-identical round trip; geometry fingerprint equal; `ExportArea` reset; `drawingScale` `1:1` |
| `T-INVALID` | `n` ∈ {−1, 11, 3.5, NaN}; `a`, `b` ∈ {0, −1, 1.5, 10^10}; `u` ∈ {0, −1, NaN, ∞}; `m` ∈ {−1, short/2}; `P` undefined | per §2.5; identities kept; recovery restores; no exception |
| `T-NO-UNIT` | `UNSPECIFIED_MODEL_UNIT` | tool and `ISO_A_SELECTION` unavailable with the reason; no object created; a typed command still evaluates (pure) |
| `T-PRESENTATION` | toggle visibility, style, layer of each output | no coordinate changes; `TRIM` hidden and dotted by default; tool-created `P` hidden; no corner points created |
| `T-LABEL` | `2:100` entered by command; locale EN/ES; unit change | text `A3 — 1:50`; locale- and unit-independent; DXF without text; PDF/SVG with text |
| `T-TRACEABILITY` | relabel outputs; delete the border; select `FRAME` or `LABEL` for linkage | link resolves through the parent algorithm and index 0; relabelling is harmless; deletion → unavailable → fallback with a notice; no label lookup in the producer code |
| `T-ISO-SELECTION` | define with each unit and session scale | values computed once with the shared helper; page = ISO size at the session scale; unavailable when unspecified |
| `T-GATES` | ADR 0031, GGBScript matrix, R5-B fingerprints, `SelfTest`, profile pins | pins moved exactly as declared; six matrix rows per syntax line, 14 probes |
| `T-LEGACY` | `Templatev7.ggb`; the g9p documents with `sheetISOAnLand` | hash unchanged; documents load and round-trip unchanged |
| `T-DETERMINISM` | create the same border twice in fresh documents | identical XML |

## 13. Verification class (`DQ-E3-16`)

`INTEGRATED_PHASE`: one registered `PHASE` (`PRE-G9B-R6-PLUS-E3`) plus
`INTEGRATION` on the same commit and tree. Concrete integration coverage: a
shared command through the ADR 0031 gates, the GGBScript matrix and the R5-B
fingerprints; two new producers of the single `ExportArea` consumed by every
picture route, print, clipboard, command line, `ExportImage`, LaTeX and the DXF
manifest; the unit and `drawingScale` cross-cuts; profile pins in nine test
sites. Escalation to `GLOBAL_IMPACT` (stop and ask): a new `GeoElement` class or
XML element type or attribute, any serialization of `ExportArea` or of the
indicator, or a change of shared parser or dispatch semantics.

## 14. Requested author decisions

None is decided here. Each row names the recommended answer and its reason.

| Id | Question | Options | Recommended | Rationale |
|---|---|---|---|---|
| `DQ-E3-1` | Representation | R1 segments; R2 closed polylines; R3 polygon; B dedicated `GeoElement` | **R2** | one traceable object per rectangle; drags through `P`; no region semantics; existing XML type; `INTEGRATED_PHASE` (§4) |
| `DQ-E3-2` | Sheet content (`AQ-E2` against `AQ-G3`) | (a) trim only; (b) trim hidden + frame; (c) + title block; margin (b1) uniform input, (b2) ISO 5457, (b3) none | **(b) with (b1), default 10 mm** | only option that preserves the hide requirement literally; title blocks stay with `G15` (§5) |
| `DQ-E3-3` | Size set | A0–A10; A0–A4 | **A0–A10** | complete ISO 216 A series; no cost difference (§6) |
| `DQ-E3-4` | Input forms (`AQ-E3a`) | index / text; Boolean / number; pair / ratio; literals / auxiliary numbers | **index, Boolean, integer pair, literals** | matches `DrawingScale` `a:b`; exact integer checks; compact deterministic XML; literals round-trip bit-exactly (`K9`, P4) (§2.2, §6) |
| `DQ-E3-5` | Canonical arithmetic | the six orders of `K6` | **`((L·b)/a)·u`**, bit-identical tests, 1 ulp bound | best measured accuracy; uses only the captured `u` (§3.2) |
| `DQ-E3-6` | Later unit change (`AQ-E3c`) | (a) keep geometry; (b)/(c) rescale | **(a)**, no automatic re-capture, indicator reports the mismatch | the only option compatible with §6.1 (§3.4) |
| `DQ-E3-7` | Unspecified unit | disabled with reason; prompt offering Document Units; explicit `u` entry | **reason-gated, offering Document Units…**; the typed command stays pure | §18.4 and `AQ-U6`; no inference (§9) |
| `DQ-E3-8` | `ExportArea` participation | (A) automatic; (B) explicit; (C) none | **(B)** | `AQ-X2`; no silent redefinition (§7.1) |
| `DQ-E3-9` | Link lifecycle (`AQ-E3b`) | (b1)–(b3); (b4), (b5) rejected | **(b1)** with notice and re-link | `B` fallback rule; no stale authority (§7.3) |
| `DQ-E3-10` | `ISO_A_SELECTION` design | per §7.5; or withdraw it from `E3` | **per §7.5** (session scale, computed once) | `AQ-X7` assigns it to `E3`; no new scale behavior |
| `DQ-E3-11` | Label | (a) output; (b) none; (c) separate command | **(a)**, visible by default | traceable to the border; independent of live state (§8.1) |
| `DQ-E3-12` | Indicator | status bar; export dialogs; overlay; explicit "use sheet scale" button | **status bar + export dialogs + explicit button**, linked border only | visible where the scale matters; never automatic (§8.2) |
| `DQ-E3-13` | GUI | mode + dialog (§9); product action on a selection; defaults | **§9** | `E2` and `presentation.image` precedents; no mouse-distance sizes |
| `DQ-E3-14` | Names and catalog | `IsoABorder` + `MarcoISOA` / `BordeISOA` / `HojaISOA`; action ids; catalog 127 → 130 | **`IsoABorder`, `MarcoISOA`; `presentation.iso-a-border`, `export.area.iso-a`, `export.area.use-iso-a-border`; mode 144** | names free (`K16`); three actions for creation, selection and later linkage |
| `DQ-E3-15` | Legacy relation | keep the legacy tools as unshipped references and offer no migration; or convert legacy sheets | **keep, no migration** | `DQ-E1-7`; `Templatev7.ggb` untouched; macro semantics are not promoted |
| `DQ-E3-16` | Verification class | `INTEGRATED_PHASE`; `GLOBAL_IMPACT` | **`INTEGRATED_PHASE`** with the escalation rule of §13 | additive command, session producers, no new element type |

## 15. Superseded statements

- Plan §8.6 "model length = paper length × scale denominator ÷
  conversion(constructionUnit → mm)": superseded by `unit-system.md` §18.4 and
  §3.2 here.
- `P0` candidate §3 outputs "four segments", §5 profile line numbers and the
  "+2" pin: superseded by §2.4, §4, `K14` and `DQ-E3-14`.
- `P0` candidate §4 linkage: kept as option (b1) with the added staleness
  obligation of §7.3.

# GeoCeDG ISO A sheet border

| Field | Value |
|---|---|
| Status | **PROPOSED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW**; not author approved |
| Version | `1.0` |
| Owner phase | `PRE-G9B-R6-plus-E3` (native `IsoABorder` and `ExportArea` integration) |
| Decision | [ADR 0036](../../../docs/adr/0036-iso-a-border-representation-and-export-area-producers.md) (`PROPOSED`) |
| Governing author decisions | [E3 author-decision record](../../../docs/validation/pre_g9b_r6_plus_e3_author_decisions_record.md) (`DQ-E3-1` to `DQ-E3-16`, with the 2026-10-10 amendment of §5: `OTQ-E3-1` resolved and the numerical-accuracy contract corrected) |
| Approved design transcribed | [E3 reconciled design](../../../docs/architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md) §2–§15 (`DESIGN — AUTHOR APPROVED`) |
| Normative inputs used, not restated | [unit-system specification](../units/unit-system.md) §3.2, §3.3, §5.3, §6, §7.2, §7.3, §12, §13, §17, §18.4; [ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md); [B author decisions](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md) `AQ-X2`, `AQ-X7`; [ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md); [ADR 0034](../../../docs/adr/0034-native-dimension-representation-and-presentation-seams.md) (β precedent) |
| Serialization effect | one additive `<command>` name with the existing output element types `polyline` and `text`; no new element type, attribute or reader change |

## 0. Status and normative language

This specification is the durable contract of the `E3` technical candidate. It
becomes normative only through an author closeout record; until then it binds
the candidate and nothing else. It transcribes the approved design §2–§15
without semantic change; §16 records how the candidate implements it. `MUST`,
`MUST NOT`, `SHOULD` and `MAY` have their usual meaning. Where an author
decision and this text differ, the decision prevails and this text is
defective.

Provenance markers: **[AD]** author decision of 2026-10-10, **[NI]** normative
inheritance, **[CE]** characterization evidence of the E3 preparation
(`K0`–`K21`), **[ID]** implementation design inside the approved scope
(reviewable, not an author decision).

## 1. Scope and placement

1.1 `IsoABorder` constructs the physical boundary of an ISO 216 A-series sheet
at a captured drawing scale and model unit, an optional inner drawing frame and
a constructive label, as an ordinary dependent construction.

1.2 Placement (`AGENTS.md` §4):

| Responsibility | Owner |
|---|---|
| command, algorithm, ISO 216 table, margin constants, canonical conversion, frame validity, degenerations, label string | shared Java kernel, `org.geocedg.common.kernel.sheet` |
| the ISO table, margins, conversion, reliability and coherence as one pure helper (`IsoASheet`), also used by `ISO_A_SELECTION` and the coherence computation | shared kernel helper [ID] |
| tool mode, point pick, dialog, creation-time capture of `u` and `a:b`, unit gate, activation question | GeoCeDG Desktop |
| `ISO_A_BORDER` and `ISO_A_SELECTION` producers, link validity, notices | GeoCeDG Desktop `ExportAreaSession` and the DXF producer mapping |
| scale and physical coherence indicator, "Use sheet scale", activation warning | GeoCeDG Desktop status bar and export-scale control (`DERIVED_TRANSIENT`) |
| rendering and export of the outputs | existing drawables and adapters |

1.3 The command MUST NOT read the unit state, `drawingScale`, the viewport,
zoom, DPI or the screen scale [NI unit-system §6.1, `AGENTS.md` §10].

1.4 Excluded [AD]: title blocks, drawing-sheet sets, paper space, viewports,
the `G15` architecture, a toolbar button, physical text size, a new
`GeoElement` type, a new durable identity mechanism, persistent `ExportArea`
state, migration of legacy sheets.

## 2. Command and inputs (`DQ-E3-4`)

2.1 One signature, no overloads:

```text
IsoABorder( <Point>, <ISO A index>, <Landscape>, <Scale numerator>,
            <Scale denominator>, <Model units per millimetre>, <Inner frame> )
```

| # | Input | Type | Valid domain | Meaning |
|---|---|---|---|---|
| 0 | `P` | 2D point | defined, finite | upper-left corner of the physical paper boundary (§4) |
| 1 | `n` | number | integer `0 … 10` exactly (`v == rint(v)`, no rounding) | ISO 216 size `A<n>` |
| 2 | `Landscape` | Boolean | `true` / `false` | `true`: width = long side; `false`: width = short side |
| 3, 4 | `a`, `b` | numbers | integers `1 … 10^9` exactly | captured `drawingScale` `a:b` (bound = `DrawingScale.MAXIMUM_TERM`) |
| 5 | `u` | number | finite, `> 0` | captured model units per millimetre, `10^-3 / fb(effC)` [NI §18.4] |
| 6 | `InnerFrame` | Boolean | `true` / `false` | explicit constructive frame state |

2.2 Every input is an ordinary construction input; the construction is
reproducible by typing the command, without the GUI [AD]. Typed commands may
use any objects of these types and then follow them through the DAG.

2.3 The tool writes literals; literals round-trip bit-exactly through `.cedg`
save and reopen [CE `K9`, P4]. The integer pair is preserved exactly as given;
only the label uses its gcd-normal form; the tool always writes the normal
form [ID].

2.4 Example (A3 landscape, `mm`, 1:50, frame on):
`IsoABorder(P, 3, true, 1, 50, 1, true)`. Spanish alias: `MarcoISOA`; the
canonical English head is `IsoABorder` (ADR 0031).

2.5 The processor accepts exactly seven arguments: argument 0 a 2D point;
arguments 1, 3, 4 and 5 numbers; arguments 2 and 6 Booleans. Any other count or
type is the ordinary typed argument error.

## 3. Outputs, relationships, serialization and identity (`DQ-E3-1`)

| Index | Output | Existing type | Initial presentation | Defined when |
|---|---|---|---|---|
| 0 | `PAPER` — physical paper boundary; authoritative sheet and `ExportArea` rectangle | `GeoPolyLine`, closed by repeating its first vertex | **hidden**; dotted style if the user shows it | all inputs valid (§15) |
| 1 | `FRAME` — inner drawing frame | `GeoPolyLine`, closed by repetition | shown, solid, black, thickness 3 | `PAPER` defined, `InnerFrame = true`, frame valid (§5) |
| 2 | `LABEL` — `A<n> — <a>:<b>` | `GeoText` (text command, internal anchor, always fixed) | shown; independently hideable | `PAPER` defined |

3.1 **Fixed arity**: the three outputs always exist; disabling the frame makes
`FRAME` undefined, it does not remove it, so identities survive a
redefinition of `InnerFrame`.

3.2 **Association** [AD]: every output's parent is the single `IsoABorder`
algorithm, and its role is its fixed index in that algorithm's output array,
serialized as the ordered `<output a0 a1 a2>`. This is the existing
multi-output mechanism; no labels, coordinates, proximity, construction
indices or new identity infrastructure are used.

3.3 **No corner outputs** [AD `DQ-E3-2`]: corners are explicit downstream
constructions when needed (`Vertex(PAPER, k)`); vertices are internal
unlabelled points owned by the algorithm.

3.4 One label names `PAPER`; the others take default labels; labels are
presentation, not identity.

3.5 **Serialization**: the ordinary additive form with existing element types
only:

```xml
<command name="IsoABorder">
	<input a0="P" a1="3" a2="true" a3="1" a4="50" a5="1" a6="true"/>
	<output a0="sheet1" a1="frame1" a2="text1"/>
</command>
```

Reopen reconstructs the same command and roles from the inputs; no derived
size is stored; the label string is recomputed. The command stays a
recognizable constructive operation after save and reopen.

3.6 Creation is one undo point; undo and redo restore the same labels and
geometry as new Java objects [CE `K9`].

## 4. Physical paper geometry (`DQ-E3-2` A)

4.1 Nominal ISO 216 size; `(W_p, H_p) = (long, short)` if `Landscape`, else
`(short, long)`, in millimetres (§6).

4.2 Axis-aligned in **model** coordinates; `P` is the corner with minimum `x`
and maximum `y` (model `y` increases upward); never derived from the viewport.

```text
W = conv(W_p)        H = conv(H_p)                     (§7)
UL = P      UR = (x_P + W, y_P)      LR = (x_P + W, y_P − H)      LL = (x_P, y_P − H)
PAPER vertices: UL, UR, LR, LL, UL                     (clockwise; closed by repetition)
```

4.3 Orientation swaps `W_p` and `H_p` only; nothing is rotated; `P` stays
upper-left.

4.4 `PAPER` is hidden by default, still part of the construction and still
usable as the `ExportArea` rectangle [AD `DQ-E3-8`].

## 5. Optional inner frame (`DQ-E3-2` B, `DQ-E3-3`)

5.1 Margins, fixed constants of the command semantics, measured on the sheet as
oriented ("left" is the side at `x_P`):

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

5.2 The frame MUST be built from `W_f`, `H_f` through the same `conv()` and the
same captured inputs as the paper; there is no independent or approximate
conversion and no subtraction of converted paper corners [AD].

5.3 The margins follow an ISO 5457-style disposition; **no ISO 5457 compliance
is claimed** [AD].

5.4 Margins are never silently reduced; an impossible frame is never generated
[AD].

## 6. Format table and defaults (`DQ-E3-3`)

ISO 216 nominal sizes (integers); the legacy unrounded series is not used.
Frame dimensions `W_f × H_f` in mm:

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
on top of it. A3 landscape is 420 × 297 mm.

## 7. Numerical conversion and invariants (`DQ-E3-5`, amended 2026-10-10)

```text
u       = 10^-3 / fb(effC)              captured by the tool         [NI §18.4]
conv(L) = ((L · b) / a) · u             binary64, exactly this order  [AD]
```

Mathematically `L_model = (L/1000) / (f(c) · a/b) = L · u · b/a`. The
expression and its evaluation order are fixed; the accuracy contract is the
three obligations below (author decision of 2026-10-10). `ε = 2^-53` is the
binary64 unit roundoff; "normal" means a magnitude in
`[2^-1022, Double.MAX_VALUE]`.

### 7.1 Domain

`L` is a nominal millimetre integer of the construction: an ISO side, a margin,
a frame side `W_f`/`H_f`, or a label offset `W_p − m_R`, `H_p − m_B`; always
`1 ≤ L ≤ 1189`. `a, b` are integers in `1 … 10^9`; `u` is finite and `> 0`
(normal or subnormal).

### 7.2 Obligation A — deterministic evaluation [AD]

For the same binary64 inputs the command evaluates exactly `((L·b)/a)·u` and
produces bit-identical results. The build targets Java 17, whose
floating-point semantics are strict IEEE 754 (JEP 306), so the result is
independent of the supported JVM and platform. Tests compare against this
expression bit for bit; this obligation holds whatever accuracy is achieved.
Save and reopen reproduce every input bit-exactly, hence every coordinate.
Equivalent pairs give bit-identical results (`2:100` and `1:50`): `L·b` and
`a` are exact, and IEEE division is correctly rounded, so only the rational
value `L·b/a` matters.

### 7.3 Obligation B — error relative to the captured factor

Reference: the exact real value `X = L · (b/a) · u` with `u` taken as the
captured binary64 input.

1. `p = L·b` is **exact**: `p ≤ 1189 · 10^9 < 2^53`.
2. `q = fl(p/a)`: `p/a ∈ [10^-9, 1.189 · 10^12]` is always normal, so
   `q = (p/a)(1 + δ₁)`, `|δ₁| ≤ ε`.
3. `r = fl(q·u)`:
   - if `q·u` is normal and finite: `r = q·u(1 + δ₂)`, `|δ₂| ≤ ε`, hence
     `|r/X − 1| ≤ (1+ε)² − 1 = 2ε + ε² < 2^-51.99`;
   - if `0 < q·u < 2^-1022` (subnormal): only the absolute bound
     `|r − q·u| ≤ 2^-1075` holds; **no relative bound is claimed**;
   - if `r = 0` (underflow) or `r` is infinite (overflow): the geometry is not
     representable (§7.6).

The bound `|r/X − 1| < 2^-51.99` holds **exactly when `fl(q·u)` is normal and
finite**; a subnormal `u` alone does not break it.

### 7.4 Obligation C — error relative to the physical unit definition

Reference: `X* = L · (b/a) · 10^-3 / f(c)` with `f(c)` the exact SI value
(`mm`, `cm`, `m`) or, for `usm`, the stored binary64 `k`, which *is* the
definition (unit-system §4.1). The tool captures `u = fl(fl(10^-3) / fb(c))`:

| Unit | Rounding events in `u` | Capture error `|u/u* − 1|` (when `10^-3/fb(c)` is normal) |
|---|---|---|
| `mm` | none: `u = 1` exactly | `0` |
| `m` | `fl(10^-3)` | `≤ ε` |
| `cm` | `fl(10^-3)`, `fb(cm)`, division | `≤ (1+ε)²/(1−ε) − 1 < 3.0001 ε` |
| `usm` (`k` exact by definition) | `fl(10^-3)`, division | `≤ (1+ε)² − 1 < 2.0001 ε` |

Combined with B, when **both** `u` and `fl(q·u)` are normal and finite:
`|r/X* − 1| ≤ (1+ε)⁴/(1−ε) − 1 < 5.001 ε < 2^-50` (worst case `cm`). This is
the only statement of the `2^-50` bound; it holds unconditionally for `mm`,
`cm` and `m`. For `usm`:

| `k` (stored binary64) | `10^-3 / k` | Behavior |
|---|---|---|
| `≈ 5.6 · 10^-312 ≤ k ≤ ≈ 4.5 · 10^304` | normal | bound `< 2^-50` when `fl(q·u)` is normal |
| `k > ≈ 4.5 · 10^304` | subnormal | `u` captured, sheet constructed; **no accuracy guarantee** (reliability `UNGUARANTEED`) |
| `k < ≈ 5.6 · 10^-312` | overflows to `∞` | **capture impossible**: the tool refuses creation with the explicit reason "unit factor not representable"; nothing is clamped |

### 7.5 Coordinates and absorption

Corner coordinates are `fl(x_P + W)`, `fl(y_P − H)` and the frame analogues;
each rounding has absolute error `≤ ε · |result|`. A realized edge, the
difference of its two corner coordinates `c₁`, `c₂`, deviates from the
converted dimension by at most `ε(|c₁| + |c₂|)`. If an addition is fully
absorbed (`x_P + W == x_P`, `y_P − H == y_P`, frame alike), the rectangle
collapses and is not representable (§7.6). A large `|P|` that is not fully
absorbed keeps a defined sheet, but its realized edges carry the coordinate
term above, which the coherence uncertainty includes (§9).

### 7.6 Validity versus reliability

Two separate classifications, never confused:

| Classification | Condition | Effect |
|---|---|---|
| **not representable** (invalid) | an input outside §2; `u` not finite or `≤ 0`; `conv()` result `0` or infinite; a coordinate infinite; an absorbed addition | the affected outputs are **undefined** (§15) — never clamped, rounded or replaced by an approximate sheet |
| **defined, `GUARANTEED`** | representable, `u` normal, every `fl(q·u)` normal | the bounds of §7.3–§7.4 hold |
| **defined, `UNGUARANTEED`** | representable, but `u` or a `fl(q·u)` is subnormal | the sheet exists exactly as computed; no accuracy guarantee is claimed; physical coherence is `NOT_DETERMINABLE` (§9) |

A failure to establish the conventional bound is **not** mathematical
invalidity. The reliability classification is a pure kernel helper over the
inputs; it adds no output, no serialization and no live dependency.

### 7.7 Reference case

A3 landscape, `mm` (`u = 1`), 1:50 → `21000 × 14850` exactly; with `cm`
(`u = 0.1`) → `2100 × 1485.0000000000002`, still a 420 × 297 mm page at 1:50.

## 8. Unit reinterpretation after creation (`DQ-E3-6`)

A later construction-unit change MUST NOT rescale, recompute `u`, change a
coordinate, redefine or change an identity [NI unit-system §6.1, §7.2, ADR
0032]. It reinterprets the physical meaning of the existing model coordinates.
The user is informed through the indicator of §9 when the sheet no longer
exports at its nominal size. No original-unit identifier is persisted: the
captured `u` already determines `f_c = 10^-3 / u`.

## 9. Scale and physical coherence (`DQ-E3-6`, `DQ-E3-12`)

Two **independent** derived checks, computed in presentation and export
orchestration only [NI §6.2], for the **linked** border:

```text
captured:  n, W_p, H_p, a_c:b_c, u   (inputs)     f_c = 10^-3 / u
current:   effC (unit state), a_n:b_n (session drawingScale)

physical-size coherence   (f_n / f_c) · (s_n / s_c) = 1     s = a/b
  computed directly: E_i = conv_c(P_i) · fb(effC) · (a_n / b_n) · 1000   mm,  i ∈ {W_p, H_p}
  uncertainty:      U_i = E_i · 2^-50 + ε(|c₁| + |c₂|)_i · fb(effC) · (a_n / b_n) · 1000   mm
  NOT_DETERMINABLE  effC unspecified or invalid; PAPER undefined; reliability
                    UNGUARANTEED (§7.6); any intermediate of E_i or U_i not
                    finite or subnormal; or U_i > 1e-6 mm on either side
  COHERENT          otherwise, and |E_i − P_i| ≤ 1e-6 mm on both sides
  INCOHERENT        otherwise                                 (never a false "coherent")

scale-label coherence
  MATCH             gcd-normal (a_c:b_c) == gcd-normal (a_n:b_n), exactly
  DIFFERENT         otherwise
  NOT_APPLICABLE    effC unspecified (drawingScale has no engineering meaning)
```

9.1 `E_i` is computed from the captured inputs, never from coordinates; the
coordinate term enters only through `U_i`, so a sheet far from the origin
whose corners can no longer realize the nominal size within the criterion is
`NOT_DETERMINABLE`, not `COHERENT`. The `1e-6 mm` criterion lies below every
output quantization and far above the binary64 rounding of these magnitudes.

9.2 Examples: A3 `mm` 1:50 at session 1:50 → `COHERENT` / `MATCH`; at 1:100 →
`INCOHERENT` (210 × 148.5 mm) / `DIFFERENT`; after changing the unit to `cm` at
session 1:50 → `INCOHERENT` (4200 × 2970 mm) / `MATCH`; the same at session
1:500 → `COHERENT` / `DIFFERENT` — physical coherence can hold while the label
scale differs.

9.3 Surfaces: the status bar and the export-scale control of the picture,
print and LaTeX export dialogs show both states and the effective and nominal
sizes in the current unit and scale.

9.4 **Use sheet scale**: an explicit user action that sets the session
`drawingScale` to `a_c:b_c`; offered when the scale state is `DIFFERENT`; it
changes no geometry; afterwards both states are recomputed and shown; if the
physical state stays `INCOHERENT`, the message names the unit reinterpretation
as the remaining cause and claims no fix.

9.5 **Activation warning**: when the user activates an `IsoABorder` as the
export-area producer, physical coherence is evaluated at once; if not
`COHERENT`, a warning shows the discrepancy and offers "Use sheet scale" when
applicable; the scale never changes without consent; no geometry is scaled.

9.6 The indicator is `DERIVED_TRANSIENT`: never geometry, never a label change,
never serialized, never in undo, never exported, no kernel dependency.

## 10. Label (`DQ-E3-11`)

10.1 Output 2; text `"A" + n + " — " + a' + ":" + b'` with `a':b'` the
gcd-normal captured pair (example `A3 — 1:50`); never the session scale;
independent of the document language, unit state and viewport; recomputed in
`compute()` from the inputs.

10.2 Anchor: the paper point at `m_R` from the right and `m_B` from the
bottom, `(x_P + conv(W_p − m_R), y_P − conv(H_p − m_B))`, with the text drawn
up and to the left of it. With a frame this is the frame's lower-right corner;
without one it still lies inside the paper for every size. The placement
never assumes that a frame exists.

10.3 Exported by picture and LaTeX exporters; **absent from DXF**, as every
text; no title-block or physical text-size work.

## 11. `ExportArea` producers and session lifecycle (`DQ-E3-8`, `DQ-E3-9`, `DQ-E3-10`)

### 11.1 Participation

Explicit only: creating a border never replaces the active area; after
creation the tool asks "Use this sheet as the export area?"; the File action
"Use selected ISO A border" links an existing border (any of its outputs
selected, resolved through the parent algorithm). One authority, no second
mechanism, no serialization [NI `AQ-X2`].

### 11.2 Producer model

```text
ExportAreaSession.Producer = EXPORT_POINTS | MANUAL | ISO_A_SELECTION | ISO_A_BORDER
precedence (unchanged)     = explicitly activated producer > Export_1/Export_2 > visible viewport
```

`ISO_A_BORDER` resolves **live** to the `PAPER` rectangle
`[x_P, x_P + W] × [y_P − H, y_P]`, never to `FRAME`, whatever its visibility;
it is bound to Graphics 1, like `MANUAL`. `ExportArea.Source`,
`GeometryExportArea.Producer`, the DXF switch and the manifest
`resolved_producer` gain both values (`iso_a_selection`, `iso_a_border`).

### 11.3 Link validity

The link holds the algorithm. It is **valid** only if an identity (`==`) scan
of the live construction finds that algorithm, its output 0 is the linked
`PAPER` object, and `PAPER` is defined. The stored reference is never trusted
by itself; no label, index, coordinate or comparator-based lookup is used.

| Event | Result |
|---|---|
| the border moves or an input changes value | same objects → link valid; area follows |
| `PAPER` undefined | producer unavailable → fallback, notice; the link is kept |
| deletion of the border | not found → stale |
| undo, redo (any, including unrelated ones) | construction rebuilt, new objects → stale |
| rebuilding redefinition | replaced objects → stale |
| reconstruction, reload, non-native document replacement | replaced objects → stale |
| New, Open | `ExportAreaSession.resetForDocument` clears it |

A stale link never activates another object; the fallback of `B` applies with
a visible notice and the re-link action. No cross-document or persistent
association.

### 11.4 Unavailable producer

When unavailable, `resolve` falls back by the rule of `B` and
`isExplicitProducerUnavailable` feeds the notice; the area is never silently
replaced without it.

### 11.5 `ISO_A_SELECTION`

A separate producer of the same authority, never merged with `ISO_A_BORDER`:
File action `export.area.iso-a`; dialog with the upper-left corner as world
coordinates (prefilled from a single selected point as a copy, never a link),
ISO size and orientation; uses the **current session** `drawingScale` and the
current unit; computed once with the shared helper; stored as values; no
frame; unavailable with an unspecified unit. No coherence indicator is shown
for an `ISO_A_SELECTION` rectangle (`OTQ-E3-3` disposition).

## 12. User interaction (`DQ-E3-7`, `DQ-E3-13`, `DQ-E3-14`)

```text
Construction → Annotations and media → ISO A Border      (menu only; no toolbar button)
```

12.1 Unit gate: with `UNSPECIFIED_MODEL_UNIT` the action is unavailable; its
reason explains why and offers **Document Units…**; nothing is inferred; a
valid `usm` enables it. The gate also applies to `ISO_A_SELECTION`. A typed
command stays a pure function of its inputs; its coherence is then
`NOT_DETERMINABLE`.

12.2 Reference point: a one-shot mode `MODE_ISO_A_BORDER` (id 144) — click an
existing point, or an empty position to create a free point. A point the tool
creates is hidden, because it is a corner of the paper boundary; an existing
point keeps its own presentation. The sheet is moved by dragging `FRAME` (or
`PAPER` when shown), which translates a free `P` through the inherited rule; a
frameless sheet with hidden `PAPER` is moved by editing `P` or by showing
`PAPER` (`OTQ-E3-4` disposition). A single preselected point skips the click.

12.3 Dialog: ISO size `A0 … A10` (default A3); orientation (default
landscape); drawing scale as an editable list (1:1, 1:2, 1:5, 1:10, 1:20,
1:50, 1:100, 1:200, 1:500, 2:1, 5:1, 10:1, and free `a:b`) prefilled with the
session scale, with `DrawingScale` parsing and inline refusal; **Inner frame**
checkbox, initially on for A0–A4 and off for A5–A10, re-initialized when the
size changes, disabled with its reason when the frame is impossible (A10
portrait); **Show label** checkbox. The dialog's presets are local;
`DrawingScale.PRESETS` and the `C` dialogs are unchanged.

12.4 OK issues one `IsoABorder` command — one undo point. Cancel removes a
point the click created.

12.5 The activation question of §11.1; on yes, the warning of §9.5 if needed.

12.6 Catalog: 127 → **130** actions — `presentation.iso-a-border`
(`upstream-mode`, group `construction-annotations-media`, no toolbar entry),
`export.area.iso-a` and `export.area.use-iso-a-border` (`product-action`,
group `file-export-area`). "Use sheet scale" is a dialog and status action,
not a catalog action. Tool text "ISO A Border" / "Marco ISO A" through the
existing `X.Tool`/`X.Help` keys; both names are free of built-in commands,
aliases and the 24 `Templatev7` macros.

12.7 Classic surface (`OTQ-E3-1`, resolved): `IsoABorder` is a shared-kernel
command available in GeoCeDG and in the Classic profile built from this fork;
no feature gate, no flag, no parallel Classic implementation; its semantics do
not depend on the profile. The menu action, tool, dialog, `ISO_A_SELECTION`,
`ISO_A_BORDER` as a session producer, the coherence indicators, "Use sheet
scale" and the activation workflow and notices are GeoCeDG Desktop only;
Classic gains no GUI.

## 13. Persistence classes

| State | Class | Serialized |
|---|---|---|
| command and outputs | `DOCUMENT_SEMANTIC` | additive `<command>`, existing element types (`polyline`, `text`) |
| `a`, `b`, `u`, `InnerFrame`, `n`, `Landscape` | `DOCUMENT_SEMANTIC` | ordinary inputs |
| label string | `DERIVED_TRANSIENT` | recomputed |
| `ISO_A_BORDER` link, `ISO_A_SELECTION` rectangle | `SESSION` | never; reset on New and Open; untouched by undo |
| coherence states, warnings | `DERIVED_TRANSIENT` | never |

## 14. Export and legacy compatibility (`DQ-E3-15`)

14.1 Picture formats, print, clipboard, `--export` and `ExportImage` draw the
outputs through the shared drawables; LaTeX through polylines and text; DXF
writes each polyline as an open `LWPOLYLINE` with the closing vertex repeated,
and no text (durable limitation).

14.1a Proposed amendment of the `PRE-G9B-R6-plus-E3-R1` corrective candidate
(not author approved): the LaTeX exporters write the label in text mode with the
em dash as `\textemdash{}` and the label's lower-right corner at its anchor
(PGF/TikZ `anchor=south east`, PSTricks `\rput[br]`, Asymptote `NW`), as on
screen. A physical LaTeX export has the export area as its page: PGF/TikZ and
PSTricks use `standalone` without border (PGF/TikZ with the export area as bounding
box, since TikZ clipping does not bound the picture) and the physical unit in TeX points with
eight decimals (the same `fb(c) · 100 · a / b` centimetres per model unit, free of
the fixed-point error of a centimetre value such as `0.01cm`); Asymptote keeps
`size()` and outlines the export area with an invisible zero-width path, so that
the paper boundary, hidden or not, defines the scale and the page. The DXF of a
sheet is the complete AC1015 container of `geometry-export-foundation.md` and
opens in AutoCAD.

14.2 Documents without the command re-save byte-identically. A build that
does not know `IsoABorder` (older GeoCeDG, upstream GeoGebra) drops its
outputs and dependents with a load error and loses them on re-save — a
documented forward-compatibility limitation, stated in the guides.

14.3 Legacy sheets: `sheetISOAnLand`, `sheetISOAnVert` stay behavioral
references with their provenance; `Templatev7.ggb` and historical baselines are
unchanged; no migration; their formula, margins and title block are not
promoted; documents using them load and behave as before; coexistence with
document macros follows ADR 0031 decision 6.

## 15. Degenerations

Failures are deterministic; outputs become undefined and are never removed or
replaced; valid inputs restore them with the same identities.

| Condition | `PAPER` | `FRAME` | `LABEL` |
|---|---|---|---|
| `P` undefined, non-finite or 3D | undefined | undefined | undefined |
| `n` not an integer in `0 … 10` (`3.5`, `−1`, `11`, NaN) | undefined | undefined | undefined |
| `a` or `b` not an integer in `1 … 10^9` | undefined | undefined | undefined |
| `u ≤ 0`, NaN or infinite | undefined | undefined | undefined |
| `conv()` result `0` (underflow) or infinite (overflow) for a paper side | undefined | undefined | undefined |
| defined but `u` or a `fl(q·u)` subnormal (§7.6) | defined, `UNGUARANTEED` | as the other rows, `UNGUARANTEED` | defined |
| `conv(W_p)`, `conv(H_p)` non-finite or `≤ 0`; paper coordinates non-finite or absorbed | undefined | undefined | undefined |
| `InnerFrame = false` | defined | undefined | defined |
| frame impossible (`W_f ≤ 0` or `H_f ≤ 0`, A10 portrait) | defined | undefined | defined |
| frame coordinates non-finite or absorbed | defined | undefined | defined |
| all valid | defined | defined iff `InnerFrame` | defined |

The tool never creates an impossible frame; a typed command with
`InnerFrame = true` for A10 portrait yields an undefined `FRAME`.

## 16. Implementation record of the candidate

This section records how the `E3` candidate realizes §1–§15; it adds no
semantics.

| Item | Realization |
|---|---|
| kernel | `IsoASheet` (constants, ISO table, `conv`, capture, reliability, coherence, gcd label), `AlgoIsoABorder` (outputs, `compute`, initial presentation, `ownerOf`), `CmdIsoABorder` (seven typed arguments), `Commands.IsoABorder` in `TABLE_GEOMETRY` |
| literals written by the tool | `u` as its shortest round-trip decimal (`Double.toString`, for example `1.0`, `0.1`), in plain notation when that form has an exponent, so the literal parses back to the identical binary64; `a`, `b` in gcd-normal form |
| output presentation | `PAPER` hidden, dotted, thickness 2; `FRAME` solid black, thickness 3; `LABEL` black, aligned up and left of its anchor; both polylines carry `visibleInView3D = false`, the state the reader gives every polyline, so the saved XML is byte-stable across reopen |
| unit gate | `GeoCeDGActionRegistry.unavailableReason` disables the tool action and `export.area.iso-a` with the reason; an invocation shows the reason with **Open Document Units…**; an overflowing `usm` factor gives the "not representable" reason |
| tool mode | after creation or cancel the controller renews the mode-start state before returning to Move, so the inherited mode change never removes the finished sheet as an unfinished tool use |
| link validation | identity scan of `getGeoSetConstructionOrder()` at every resolve and on every Graphics 1 paint; a stale link is dropped and reported as lost |
| indicators | status-bar segment `sheet-coherence`; the export-scale control's sheet notice with **Use sheet scale** |
| icon | explicit reuse of the inherited rectangle artwork (`mode_shaperectangle`); no new asset |

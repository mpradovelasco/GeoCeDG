# PRE-G9B-R6-plus-E3 — native `IsoABorder`: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Subphase `E3` needs its own
  explicit author authorization, after `D1` (and `B` for the export linkage).
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  §6 and findings 7, 19

## 1. Author-fixed scope

A native `IsoABorder` under `Construction → Annotations and Media`, with no
toolbar button unless separately authorized. Inputs: upper-left point, ISO A
size, portrait or landscape, drawing scale. The geometry derives from the
nominal ISO physical dimensions, `drawingScale` and the unit semantics. After a
yes/no question it may configure the common `ExportArea`. There is no second
export-area mechanism.

It is a **model-space annotation**, not `G15` paper space (plan §1.1).

## 2. Behavioral reference: the legacy sheet tools (P0 §6)

- Sizes come from an `Iteration` over the **unrounded** theoretical series:
  long side `100·2^(1/4)·2^(−n/2)`, short side `100·2^(−1/4)·2^(−n/2)`, divided
  by `CeDGScale`. At `n = 3` the stored values are 42.0448 × 29.7302, about
  420.45 × 297.30 mm when read as cm, while ISO 216 specifies 420 × 297 mm.
- The margin is uniform. The title block is a scaled grid designed for A3 in
  cm and is not rotated in portrait. There is no guard for a non-positive
  scale. Neither tool creates `Export_1`/`Export_2`.

## 3. Command semantics (kernel)

```text
IsoABorder( <Upper-left point P>, <ISO A index n>, <Landscape (boolean)>,
            <Scale numerator>, <Scale denominator>, <Model units per millimetre> )
```

- `n ∈ {0, …, 10}`. ISO 216 nominal sizes in mm (short × long):

  | n | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
  |---|---|---|---|---|---|---|---|---|---|---|---|
  | mm | 841×1189 | 594×841 | 420×594 | 297×420 | 210×297 | 148×210 | 105×148 | 74×105 | 52×74 | 37×52 | 26×37 |

- Landscape: width = long side; portrait: width = short side.
- **Drawing scale** is dimensionless (`1:n`, `n:1`), entered as a pair of
  positive integers (numerator, denominator), which avoids decimal ratios
  (`AQ-E3a`; alternative: one positive number `s` = paper length ÷ real
  length). For 1:2 the pair is (1, 2).
- **Unit-invariance rule (author-fixed plan §4.1).** The command never reads
  `constructionUnit` or any export `drawingScale` live. Otherwise a later unit
  change would rescale the border and move every dependent point, which
  §4.1 forbids. The physical meaning enters through an explicit numeric
  input: *model units per millimetre* `u`. The **tool** captures it once, at
  creation, from the then-current `constructionUnit` (`u = 1` for mm, `0.1`
  for cm, `0.001` for m), and it is then an ordinary construction input
  (`DOCUMENT_SEMANTIC`), visible and editable.
- Model size = paper size (mm) × denominator ÷ numerator × `u`.
- **After a later `constructionUnit` change** the border keeps its
  coordinates, as §4.1 requires. It then *represents* a different physical
  paper size until the user explicitly edits `u`, which is an ordinary,
  user-initiated redefinition, not an automatic rescale. This consequence
  needs an author decision (`AQ-E3c`). Recommended: accept it, because it is
  the only behavior compatible with §4.1. Rejected: a command that follows the
  unit live, which violates §4.1.
- With `UNSPECIFIED_MODEL_UNIT` the **tool** has no factor to capture. It asks
  for a unit declaration first, or for an explicit `u` (`D0`, `AQ-U6`). It
  never infers `cm`.
- Outputs: four segments forming the trim border (`AQ-E2`, recommended: border
  only; title blocks and margins stay with `G15`) and, optionally, the
  rectangle's four corners as hidden dependent points for downstream
  construction and export linkage.
- Degeneracies: `n` out of range or not an integer; a numerator or denominator
  that is not a positive integer; `u` non-positive or non-finite; or a
  non-finite `P`. Each gives an undefined output.

## 4. Export linkage and the ISO A area selection

`E3` also delivers the two ISO-based producers of `B`'s single `ExportArea`.
They are placed here because they need `D1`'s unit contract and `B` runs
before `D1` (`AQ-X7`).

- `ISO_A_SELECTION`: a File action (`export.area.iso-a`) that computes a world
  rectangle once from a corner point, size, orientation, drawing scale and the
  then-current unit. It is session state, never geometry.
- `ISO_A_BORDER`: after creating a border, the tool asks yes/no. On yes, the
  border becomes the `ISO_A_BORDER` producer of the single `ExportArea`
  (session). There is no
document flag and no `Export_1`/`Export_2` side effect. On construction rebuild
(undo, redo, Open, a rebuilding redefinition) the link is dropped, because the
Java object is replaced (probe P6). The area then falls back by `B`'s rule.
Alternatives for the author (`AQ-E3b`): re-resolve by label (rejected by P0 as
label-based inference); snapshot the rectangle once as a `MANUAL` producer; or
a document-level flag on the border (`DOCUMENT_PRESENTATION`, contrary to the
author's `SESSION` preference).

## 5. Placement in the product

- Action `presentation.iso-a-border` added to `construction-annotations-media`
  (today `["presentation.image"]`, `apps/geocedg/application-profile.yml:3288-3292`),
  with `toolbar_action_ids` left empty. Menu only.
- ADR 0031 gates and GGBScript capability-matrix rows, as for `E2`. Catalog pin
  +2 (`presentation.iso-a-border` and `export.area.iso-a`).
- Relation to the legacy sheet tools: `E1`'s `AQ-G2` decides whether they stop
  shipping once `IsoABorder` is accepted.

## 6. Serialization

Additive `<command name="IsoABorder">` with existing output types. The
scale pair and `u` are ordinary inputs. The linkage and the ISO A area
selection are session state and are not serialized.

## 7. Verification-class recommendation

`INTEGRATED_PHASE` (registered `PHASE` plus `INTEGRATION`): a new shared
command with additive XML, unit integration from `D1`, an `ExportArea` producer
from `B`, the ADR 0031 gates and the matrix. A new `GeoElement` class or XML
element type would make it `GLOBAL_IMPACT`.

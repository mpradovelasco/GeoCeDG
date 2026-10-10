# PRE-G9B-R6-plus-E3-R1 — DXF interoperability and LaTeX export: characterization

```text
TASK                 = PRE-G9B-R6-plus-E3-R1 (bounded corrective follow-up of PRE-G9B-R6-plus-E3)
AUTHORIZATION        = author authorized bounded characterization and corrective implementation (2026-10-10)
PARENT CANDIDATE     = T_R6PLUS_E3 257c854aa65fe82d316968109b9d084deefd2a49, tree b3cedf15b42c3a4dc5925744e83d510ea2dd4850
PUBLISHED BASELINE   = ff56099184544e5988c63e0ea3339e3d0fe01fac, tree 1cdd1e986df785f5f16e7887300d9034e5e4a677
AUTHOR SMOKE         = PASS WITH TWO BLOCKING EXPORT OBSERVATIONS (OBS-E3-DXF-INVALID-OUTPUT, OBS-E3-LATEX-EXPORT)
SCOPE DECISION       = bounded; no IsoABorder geometry, unit, DAG, persistence or ExportArea change
VERIFICATION CLASS   = INTEGRATED_PHASE (PHASE PRE-G9B-R6-PLUS-E3-R1 + INTEGRATION), frozen before the product edits
selfApproved         = false
```

This report records the characterization made on the corrective branch
(`phase/pre-g9b-r6-plus-e3-r1-export-correction`, created from the parent
candidate) before the corrective source edits. It is evidence; the
[candidate report](pre_g9b_r6_plus_e3_r1_candidate_report.md) records the
implementation.

## 1. Entry gate

`main`, `origin/main` and the live remote `main` were
`ff56099184544e5988c63e0ea3339e3d0fe01fac`; the worktree was clean on the parent
candidate `257c854a` (tree `b3cedf15`). The corrective branch descends directly
from it; the parent candidate and its acceptance evidence
(`verification-a243082e9be1444f910aa3d7cab15e47`,
`verification-d0e9c1b892f34061a29e4d41fb6aa0b5`) are untouched.

Tools used: AutoCAD 2026 `accoreconsole.exe` (W.164.0.0) with an inline AutoLISP
script (`OPEN` through `/i`, entity counts, `INSUNITS`, `AUDIT` without fixing);
ezdxf 1.4.4 in an isolated scratch virtual environment (installed with the
author's permission, not a product or repository dependency), loading the
serialized file with the strict `readfile` and auditing it; MiKTeX 26.5
`pdflatex`, `latex`, `dvips`, `ps2pdf`, `asy` 3.06, `pdfinfo`; Ghostscript
10.02.1.

## 2. DXF

### 2.1 Reproduction

The author's files (`isaoA.dxf` `d974a555…ad8b`, the variant without the long
comment `68164b6b…952e`, the ezdxf-normalized variant `52c0700c…ee18`) are not
in the agent workspace. An equivalent sample was reproduced on the parent
candidate: construction unit `mm`, session scale 1:5, `P=(0,0)`,
`A=(1000,-1000)`, `B=(3000,-2000)`, `c=Circle(A,500)`,
`IsoABorder(P,3,true,1,10,1.0,true)` linked as `ISO_A_BORDER`, complete
construction through the default (G5) DXF route. It has the author's structure:
AC1015, `$INSUNITS 4`, 3 `POINT`, 1 `CIRCLE`, 2 `LWPOLYLINE`, the long
`999 GeoCeDG export area … ISO_A_BORDER …` comment.

| Sample (parent writer) | SHA-256 | ezdxf audit | AutoCAD 2026 |
|---|---|---|---|
| E3 sample | `65937a58…285b` | 0 errors, 2 fixes: invalid table head handle in LTYPE and LAYER (as the author found) | refused: "Error in APPID Table … Invalid or incomplete DXF input -- drawing discarded", ErrorStatus 53 |
| simple construction without a sheet | `50701a99…5e6c` | the same 2 fixes | refused, the same error |
| C smoke file `c-layers-and-area.cedg` (G9X1 route) | `33bdfca9…b9f7` (the pinned C digest) | the same 2 fixes | refused, the same error |

### 2.2 Classification

- **Present in the published baseline, exposed by E3.** `DxfExporter` is
  byte-identical in `ff560991` and `257c854a`; E3 changed only the export-area
  producers. Every GeoCeDG DXF since G5 is refused by AutoCAD — recorded during
  `PRE-G9B-R6-plus-C` as `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` — so the
  defect is not introduced by E3; the E3 smoke made it blocking.
- **Not the comment.** Every structural variant below keeps the leading `999`
  comments and the per-entity `999 GeoCeDG source …` comments; AutoCAD reads them.
  The author's variant A (comment removed, still refused) agrees.
- **Not an external-reader limitation.** AutoCAD reports a missing mandatory
  table; ezdxf only reports the two missing table-head handles because it
  silently creates every other missing table, block and object while loading.

### 2.3 Minimal container (differential variants)

The ENTITIES section of the E3 sample was kept verbatim and the container was
rebuilt with one structural feature removed at a time:

| Removed | AutoCAD 2026 |
|---|---|
| nothing (full candidate container) | opens, 6 entities, AUDIT 0 found / 0 fixed |
| `$HANDSEED`; VPORT; ByBlock/ByLayer line types; STYLE; VIEW; UCS; APPID; DIMSTYLE table; BLOCK_RECORD; BLOCKS; OBJECTS; subclass markers; layer `390` plot-style pointer | refused (the last one: "Did not receive PlotStyleName") |
| CLASSES section; DIMSTYLE `Standard` record; `ACAD_GROUP` dictionary; layer `370` lineweight; table/record owners `330` | opens, AUDIT 0 / 0 |

Selected container: the full set minus the tolerated CLASSES section,
DIMSTYLE record, `ACAD_GROUP` and lineweight, keeping the `330` owners as the
ownership relationships of the AC1015 contract. Entities need no owner tag:
AutoCAD and ezdxf assign them to model space. With this container AutoCAD opens
the E3 sample (6 entities, 2 `LWPOLYLINE`, `INSUNITS 4`), the simple construction
(4) and the C smoke file (8), each with AUDIT 0 / 0, and ezdxf reads them with the
strict loader with 0 errors and 0 fixes.

## 3. LaTeX

### 3.1 Reproduction

The same A3 sheet captured at 1:10 was exported with the session scale 1:10 and
1:5, with and without the inner frame, `PAPER` and `P` hidden, axes and grid off,
in the three dialects of the GeoCeDG exporters.

| Finding | Dialect | Cause | Class |
|---|---|---|---|
| `A3 $\emdash$ 1:10`: "Undefined control sequence", no PDF | PGF/TikZ, PSTricks | the host converts U+2014 through `UnicodeTeX`, a JLaTeXMath table whose `\emdash` is not a LaTeX command, and wraps it in math mode | exposed by E3 (first GeoCeDG text with an em dash); host table, used by Classic too |
| `A3 emdash  1:10` (the word) | Asymptote | the same table without the backslash | exposed by E3 |
| label below and right of its anchor (`anchor=north west`, `\rput[tl]`, `SE`) | all three | the host ignores the label's alignment (drawn up and left on screen) | exposed by E3 |
| drawing cropped by the `article` page | PGF/TikZ, PSTricks | the host writes `\documentclass[10pt]{article}`; a physical page larger than its text block is cut | present since C, exposed by E3 |
| page 419.76 × 296.83 mm instead of 420 × 297 (1:10) and 840.17 × 594.12 instead of 840 × 594 (1:5) | PGF/TikZ, PSTricks (with `standalone`) | `x=0.01cm` is evaluated in TeX's 16.16 fixed point: 0.01 = 655/65536 (−5.5e-4), 0.02 = 1311/65536 (+2.1e-4); `\rule{42cm}{29.7cm}` in `standalone` is exact | present since C |
| frame scaled to 84 cm (page 840.4 × 597.0 mm) with a hidden paper | Asymptote | `size(84.cm)` fits the visible content (the inset frame), not the export area | present since C, exposed by E3 |
| page wider by one interword space (3.33 pt) | PGF/TikZ with colours | colour set-up lines in the document body end with a line end, which `standalone` keeps as a space | present since C, exposed by the composition |

The kernel label `A3 — 1:10` is correct; the scale mismatch 1:10 / 1:5 is
intentional and exports at the session scale.

### 3.2 Design checks (hand-applied, compiled)

`standalone` with `border=0pt`, the unit in TeX points (`0.28452756pt`), the label
`\textemdash{}` anchored `south east` / `[br]` / `NW` and, for Asymptote, an
invisible zero-width outline of the export area before the clip:

| Output | Page (bp) | Expected (bp) |
|---|---|---|
| PGF, PSTricks, 1:10 | 1190.56 × 841.90 | 1190.55 × 841.89 |
| PGF, PSTricks, 1:5 | 2381.13 × 1683.80 | 2381.10 × 1683.78 |
| Asymptote without label, 1:10 / 1:5 | 1190.55 × 841.89 / 2381.10 × 1683.78 | the same |

### 3.3 Toolchain limitation

The installed `asy` 3.06 cannot typeset any label on this workstation: a
one-line `label("abc",(0.5,0.5))` fails in `plain_Label.asy` with every TeX engine
(`latex`, `pdflatex`, `xelatex`, `lualatex`), exits with 0 and writes no PDF, and
some label-free runs stay alive after writing their PDF. Asymptote label
compilation is therefore **UNAVAILABLE** here; its drawing and page are compiled
and measured without the label.

## 4. Scope and verification class

The corrections stay at the export boundary: the shared DXF writer's container
(entity records, handles, layers, visibility, units, comments and sidecar
unchanged), the GeoCeDG LaTeX exporters (label output) and the physical LaTeX
composition. No kernel, geometry, unit, DAG, persistence, `ExportArea`, Classic
or rendering change; no new dependency.

The DXF writer is shared by the G5 route, the G9X1 extended route and every
consumer test of B, C, G9X1 and E3; the LaTeX composition applies to every
physical LaTeX export. Both are covered by `final.shared` and `final.desktop`,
which `INTEGRATION` runs, plus a registered `PRE-G9B-R6-PLUS-E3-R1` phase for the
focused evidence. The proposed class `INTEGRATED_PHASE` is therefore sufficient
and was frozen; no global redesign, no compatibility widening and no
`GLOBAL_IMPACT` obligation was found.

## 5. Out of scope, retained

- `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` (proposed separately as "Characterize ES
  command lookup after legacy .ggb load").
- `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` (proposed separately as
  "Fix case-folded types in legacy tool inventory").
- Found during this characterization: `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` —
  the E2 dimension value is written to PSTricks with a 14-decimal rotation that
  `latex` refuses ("Number too big"); reproduced on `257c854a`; not changed.
- The host `UnicodeTeX` mapping of other characters (for example `\endash`) is
  unchanged for every other text.

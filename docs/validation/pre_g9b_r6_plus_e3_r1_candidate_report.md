# PRE-G9B-R6-plus-E3-R1 — DXF interoperability and LaTeX export: corrective candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the candidate commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

TASK                      = PRE-G9B-R6-plus-E3-R1 (bounded corrective follow-up)
PARENT CANDIDATE          = T_R6PLUS_E3 257c854aa65fe82d316968109b9d084deefd2a49,
                            tree b3cedf15b42c3a4dc5925744e83d510ea2dd4850 (retained)
AUTHOR SMOKE (parent)     = PASS WITH TWO BLOCKING EXPORT OBSERVATIONS
                            (OBS-E3-DXF-INVALID-OUTPUT, OBS-E3-LATEX-EXPORT)
VERIFICATION_CLASS        = INTEGRATED_PHASE (frozen before the product edits)
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E3-R1
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
FINAL                     = NOT RUN (not required)
KERNEL GEOMETRY           = UNCHANGED
EXPORTAREA                = AUTHORITY UNCHANGED
DXF ENTITIES              = UNCHANGED (core digests equal to the base bytes)
AUTOCAD 2026              = PENDING AUTHOR RE-SMOKE (agent-side accoreconsole evidence in section 3)
ASYMPTOTE LABEL COMPILE   = UNAVAILABLE on this workstation (section 4.4)
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This artifact records facts fixed when it was written; the candidate commit and
its two acceptance runs are reported outside it. Characterization:
[`pre_g9b_r6_plus_e3_r1_characterization_report.md`](pre_g9b_r6_plus_e3_r1_characterization_report.md).
Machine-readable mirror:
[`pre-g9b-r6-plus-e3-r1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e3-r1-candidate-evidence.json).

## 1. Identity

| Identity | Value |
|---|---|
| published baseline (`main`, remote `main`) | `ff56099184544e5988c63e0ea3339e3d0fe01fac`, tree `1cdd1e986df785f5f16e7887300d9034e5e4a677` |
| E3 documentary authorization checkpoint | `3a577ea44a0bc93492a0d74c6f6c68b561b3ff63`, tree `1cc021a3d9e26850c5e0546e4ee89b036332525a` |
| parent technical candidate `T_R6PLUS_E3` | `257c854aa65fe82d316968109b9d084deefd2a49`, tree `b3cedf15b42c3a4dc5925744e83d510ea2dd4850`; PHASE `verification-a243082e9be1444f910aa3d7cab15e47`, INTEGRATION `verification-d0e9c1b892f34061a29e4d41fb6aa0b5` (historical evidence) |
| corrective branch (local, not pushed) | `phase/pre-g9b-r6-plus-e3-r1-export-correction`, one commit on the parent |

## 2. Author-smoke observations (preserved)

- `OBS-E3-DXF-INVALID-OUTPUT`: AutoCAD 2026 and other applications cannot open
  the ASCII DXF AC1015 of an ISO A sheet. Author differential: removing the long
  `999` comment (`68164b6b…952e`) does not help; the ezdxf-normalized file
  (`52c0700c…ee18`) opens; ezdxf 1.4.4 fixed the invalid table-head handles of
  LTYPE and LAYER in `isaoA.dxf` (`d974a555…ad8b`).
- `OBS-E3-LATEX-EXPORT`: PGF/TikZ contains the undefined `\emdash`; the `article`
  layout cannot hold the physical drawing; with `\textemdash` and
  `\documentclass[10pt,tikz,border=0pt]{standalone}` the drawing compiles
  completely. The export used captured scale 1:10, session scale 1:5, unit `mm`
  (intentionally twice the nominal size).
- The other E3 functionality behaved correctly; those checks are historical
  author evidence and are not repeated here.

## 3. DXF

**Root cause** (characterization §2): the G5 container (`DxfExporter`, unchanged
by E3) writes only HEADER (`$ACADVER`, `$INSUNITS`), LTYPE and LAYER without
handles, owners or subclass markers, and ENTITIES. AutoCAD needs `$HANDSEED`, the
nine symbol tables, the space blocks and the plot-style objects every R2000 layer
points to. Pre-existing since G5 (`OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION`),
exposed by E3; the comment is not the cause.

**Correction** (`DxfExporter`): `$HANDSEED`; VPORT, LTYPE (`ByBlock`, `ByLayer`,
`CONTINUOUS`), LAYER (each with its `390` plot-style pointer), STYLE `Standard`,
VIEW, UCS, APPID `ACAD`, DIMSTYLE, BLOCK_RECORD (`*Model_Space`, `*Paper_Space`),
each table and record with handle, owner and subclass markers; BLOCKS with the two
space blocks; OBJECTS with the root dictionary, `ACAD_PLOTSTYLENAME` and its
placeholder. Structural handles are allocated below `0x100`; the entity handles,
entity records, layers, colours, visibility (`60`), true colours, comments,
`$INSUNITS`, the area comment and the sidecar are unchanged. No runtime
dependency, no post-processing.

**Independent results** (serialized files, not repaired documents):

| Sample | SHA-256 | ezdxf 1.4.4 (strict `readfile` + audit) | AutoCAD 2026 `accoreconsole` |
|---|---|---|---|
| E3 sample, parent writer | `65937a58…285b` | 0 errors, 2 fixes (LTYPE, LAYER heads) | refused, "Error in APPID Table", ErrorStatus 53 |
| E3 sample, corrected | `fa810823…b286` | 0 errors, 0 fixes | opened: 6 entities, 2 `LWPOLYLINE`, `INSUNITS 4`; AUDIT 0 found / 0 fixed |
| simple construction, parent / corrected | `50701a99…5e6c` / `01e791e2…abd` | 2 fixes / 0 fixes | refused / opened, 4 entities, AUDIT 0 / 0 |
| C smoke file (G9X1 route), parent / corrected | `33bdfca9…b9f7` / `f0ed5702…ac59` | 2 fixes / 0 fixes | refused / opened, 8 entities, AUDIT 0 / 0; layer `GEOCEDG_L3` colour −7 (off); 3 invisible entities |

Entity equivalence (DXF-R1-04): for the three samples the ezdxf populations of
the parent and corrected files are identical (types, handles, layers,
invisibility, coordinates, radii, closure). The base-identity test now pins, for
the six G5/G9X1 reference exports and the C smoke file, the complete digest of the
corrected file and a core digest (leading comments and ENTITIES section) that
equals the core of the base bytes, recorded on a worktree of `257c854a` whose
complete digests are the historical base digests.

AutoCAD acceptance by the author remains **pending** (DXF-R1-08); the
`accoreconsole` runs are agent-side evidence only.

## 4. LaTeX

### 4.1 Root causes (characterization §3)

1. The host maps U+2014 through the JLaTeXMath table `UnicodeTeX` to `\emdash`
   (undefined in LaTeX) in math mode, or to the word `emdash` in Asymptote.
2. The host ignores the label's alignment: the text falls below and right of its
   anchor, outside the frame.
3. `article` crops a physical page larger than its text block (since C).
4. A centimetre unit such as `0.01cm` carries TeX's fixed-point error (up to
   6e-4 relative: 419.76 instead of 420 mm).
5. TikZ clipping does not bound the picture, and colour set-up lines in the body
   add an interword space on a `standalone` page.
6. Asymptote `size()` fits the visible content: with a hidden paper the frame,
   not the paper, gets the physical width.

### 4.2 Correction (export boundary only)

- `IsoABorderLatexExport` + the three GeoCeDG exporters: the sheet label (only
  the `IsoABorder` label output) is written in text mode with `\textemdash{}`
  (other characters escaped as for the dimension value) and its lower-right corner
  at the anchor: PGF/TikZ `node[anchor=south east]`, PSTricks
  `\rput[br]{\psframebox[linestyle=none,framesep=0.3333em]{…}}` (the clearance of
  a TikZ node), Asymptote `label(…, NW)`. The kernel label string is unchanged;
  `UnicodeTeX` and every other text are unchanged.
- `PhysicalLatexComposition`, applied by `GeoCeDGLatexSettings` only for a
  physical export (construction unit present): `article` → `standalone` with
  `border=0pt`; set-up lines in the body end with `%`; the unit
  `x=…cm,y=…cm` / `xunit=…cm,yunit=…cm` is written in TeX points with eight
  decimals (`fb(c) · 100 · a / b` cm per unit, the C contract value); PGF/TikZ
  gets `\useasboundingbox` equal to the clip rectangle (not inside a pgfplots
  axis); Asymptote keeps `size()` and outlines the export area with an invisible
  zero-width path before the clip. Device-scale output without a unit is the host
  output (base digests unchanged).

### 4.3 Compiled evidence

`PreG9BR6PlusE3R1ExportInteroperabilityTest` compiles with the installed MiKTeX
26.5 toolchain, measures the PDF page (`pdfinfo`) and rasterizes it (Ghostscript,
36 dpi) to check the four frame edges and the label region:

| Case (A3, captured 1:10) | Expected page | PGF/TikZ (`pdflatex`) | PSTricks (`latex`, `dvips`, `ps2pdf`) | Asymptote |
|---|---|---|---|---|
| session 1:10, frame | 420 × 297 mm (1190.55 × 841.89 bp) | page within 0.05 bp; 4 edges; label up-left of the anchor, nothing below-right | the same | page and 4 edges without the label (4.4) |
| session 1:10, no frame | the same | page; no frame edge; label | the same | page, no edge |
| session 1:5, frame | 840 × 594 mm (2381.10 × 1683.78 bp) | page; 4 edges; label | the same | page and 4 edges without the label |
| session 1:5, no frame | the same | page; no edge; label | the same | page, no edge |

Measured during the characterization of the design: PGF/PSTricks 1190.56 ×
841.90 and 2381.13 × 1683.80 bp; Asymptote 1190.55 × 841.89 and 2381.10 ×
1683.78 bp. Regression (TEX-R1-07): a physical construction without a sheet
(segment, polyline, ordinary text, aligned dimension) keeps the host text
(`anchor=north west`) and the dimension output, and compiles in PGF/TikZ to its
100 × 60 mm export area; PSTricks to the same page without the dimension (4.5).

### 4.4 Asymptote toolchain limitation

The installed `asy` 3.06 cannot typeset any label (even `label("abc")`) with any
TeX engine, exits 0 without a PDF, and sometimes stays alive after writing a PDF.
The labelled Asymptote compilation is **UNAVAILABLE** here and is reported by the
test; the generated label text is checked and the drawing and page are compiled
and measured without the label. The author smoke covers the labelled file.

### 4.5 Observation outside the scope

`OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE`: the E2 dimension value is written to
PSTricks with a 14-decimal rotation (`\rput[b]{21.80140948635181}…`) that `latex`
refuses ("Number too big"); reproduced on `257c854a`; not changed (proposed as a
separate activity).

## 5. Regression analysis and changed paths

| Consumer | Effect |
|---|---|
| G5 route, G9X1 route, B, C, G9X1 and E3 DXF consumers | container added; core (comments and entities) identical; sidecars identical |
| non-physical LaTeX, Classic LaTeX, pictures, EMF, SVG, PDF | byte-identical (base-identity digests) |
| physical LaTeX (C contract) | unit value unchanged, written in points; page = export area |
| kernel, units, persistence, DAG, `ExportArea`, Classic GUI | unchanged |

Changed paths: `DxfExporter`; `GeoCeDGLatexSettings`, the three GeoCeDG
exporters, new `PhysicalLatexComposition` and `IsoABorderLatexExport`; tests
(new `PreG9BR6PlusE3R1DxfContainerTest`, `PreG9BR6PlusE3R1ExportInteroperabilityTest`;
updated pins in `PreG9BR6PlusCBaseIdentityTest`, `PreG9BR6PlusCDxfDesktopTest`,
`PreG9BR6PlusCLatexExportTest`, `PreG9BR6PlusCSharedExportTest`,
`PreG9BR6PlusBExportSurfaceTest` (interim DXF digest),
`PostE2P1DimensionCaptureCharacterizationTest` (PGF unit read in points; measured
geometry unchanged) and `PostE2P1R2BaseFingerprintTest` (the physical PGF key pins
the new digest `cc8ff265…3fa`; the base fixture is unchanged; a scratch probe
proved that this output equals `PhysicalLatexComposition.pgf` applied to the
parent output `26b96019…3e1`, the fixture value, with `x=0.1cm`)); proposed
amendments in `geometry-export-foundation.md` and `iso-a-border.md` §14.1a;
provenance; phase `PRE-G9B-R6-PLUS-E3-R1`, JUnit inventory and catalog pins;
these reports and the evidence; roadmap and mini-track status.

## 6. Verification class

`INTEGRATED_PHASE`, frozen before the product edits (characterization §4): the
corrections are confined to export adapters; `final.shared` and `final.desktop`
in `INTEGRATION` cover every consumer of the shared DXF writer and of the LaTeX
exporters; the registered phase `PRE-G9B-R6-PLUS-E3-R1` carries the focused
evidence. No `GLOBAL_IMPACT` obligation exists; nothing escalated.

## 7. Tests and checks

| Check | Result |
|---|---|
| `:shared:common-jre:test` complete | 6992 tests, 0 failures, 0 errors, 10 skipped (`final.shared` evidence producer); `pre-g9b-r6-plus-e3-r1.shared` 63 tests, 0 failures |
| focused Desktop export classes | `pre-g9b-r6-plus-e3-r1.desktop` 105 tests, 0 failures; `final.desktop` 1985 tests, 0 failures, 1 skipped (evidence producers, uncommitted tree) |
| checkstyle (four source sets) | no finding in an E3-R1 file; one pre-existing warning in `PreG9BR6PlusA1HiddenLayerTest` |
| development `STATIC` / `INFRA_UNIT` | `ACCEPTED` / `COMPLETE` both; `STATIC` diagnostics: the standing governance finding and historical-consistency `UNAVAILABLE` |
| JUnit inventory | regenerated by the in-session updater from discovery and executed evidence: `discovery.shared` 6075, `discovery.desktop` 1991, `final.shared` 6992, `final.desktop` 1985, `pre-g9b-r6-plus-e3-r1.shared` 63, `pre-g9b-r6-plus-e3-r1.desktop` 105; 55 selections, 60 phase selections; catalog pin updated |
| `git diff --check` | clean |

## 8. Remaining limitations

- AutoCAD acceptance is pending the author's re-smoke; the F4 AutoCAD 2026 DXF
  recheck stays a separate activity.
- Asymptote labels cannot be compiled on this workstation.
- DXF still writes no text (approved limitation); the DXF label stays omitted.
- The host `UnicodeTeX` mapping of other characters is unchanged.

## 9. Focused author smoke

**DXF**
1. Open the E3 sample (A3 1:10 sheet with frame, linked as export area) and
   export DXF (default route, then the extended route if enabled).
2. Open the new DXF in AutoCAD 2026: no input error.
3. Two polylines: paper 4200 × 2970 and frame 3900 × 2770 model units (at 1:5 in
   `mm` the same model coordinates; the drawing is model space).
4. Paper invisible, frame visible, both on layer `0`; a hidden GeoCeDG layer
   arrives off.
5. Coordinates equal the GeoCeDG model coordinates; nothing was rescaled.

**LaTeX**
1. Export an A3 sheet with `PAPER` hidden and `FRAME` visible (PGF/TikZ;
   optionally PSTricks and Asymptote) at the captured scale.
2. Compile the generated file without editing it.
3. The label reads `A3 — 1:…` with a proper em dash, inside the frame at its
   lower-right corner.
4. All four frame sides are visible; the page is 420 × 297 mm at 1:10.
5. Change the session scale (for example 1:5) and export again: the page is
   840 × 594 mm, the frame and label are complete, and the geometry is unchanged.

## 10. Final state

```text
PRE-G9B-R6-plus-E3-R1   = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = reported outside this file (PHASE + INTEGRATION on the candidate)
AUTOCAD 2026            = PENDING AUTHOR RE-SMOKE
AUTHOR APPROVAL         = PENDING
PUBLICATION             = NOT AUTHORIZED
selfApproved = false, authorApproved = false, passClaimed = false
```

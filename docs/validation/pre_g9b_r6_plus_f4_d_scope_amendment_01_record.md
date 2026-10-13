# PRE-G9B-R6-plus-F4-D — scope amendment 01: authorization, entry gate and impact analysis

```text
RECORD_KIND               = AUTHOR SCOPE AMENDMENT, ENTRY GATE, IMPACT ANALYSIS, CONTRACT
TASK                      = PRE-G9B-R6-plus-F4-D-SCOPE-AMENDMENT-01 (author instruction,
                            2026-10-14; option A of the stop report)
STARTING POINT            = STOP commit aae7b12ccc7e8c21eced1b10f7aa08562ae2bec2
                            tree f6732b1536af3c831fb9bfb05bd8d0784d6f331a
                            parent 3d609397cf82c428872a0b6859426aba7c0efdeb
                            STATIC verification-ff489c0e65cd484490f8896c37f6173b
                            ACCEPTED / COMPLETE
WIP (inspection, reuse)   = 6c2fc40918be3e43a1df09a454f407be85acd553 (not a candidate)
PUBLISHED MAIN ANCHOR     = 847bb20a593e8092eabf80bd9d7c62ed7c21de08
BRANCH                    = phase/pre-g9b-r6-plus-f4-d-amendment-01 (local, from aae7b12c)

F4-D EXPANDED IMPLEMENTATION = AUTHORIZED (entry gate PASS, §2)
F4-D VERIFICATION_CLASS   = INTEGRATED_PHASE — MAINTAINED (impact analysis §4)
F4-D TECHNICAL ACCEPTANCE = PENDING
F4-D AUTHOR SMOKE         = PENDING
F4-A IMPLEMENTATION       = NOT AUTHORIZED
PHASE G                   = NOT AUTHORIZED
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
```

This record transcribes the author instruction and is the first tracked change of the
amended track. The [implementation stop report](pre_g9b_r6_plus_f4_d_implementation_stop_report.md)
is accepted by the author as correct and stays unchanged as historical evidence, as do the
[authorization record](pre_g9b_r6_plus_f4_d_implementation_authorization_record.md) and the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md),
whose obligations, stop conditions and restrictions keep binding except where this
amendment widens the productive scope. Machine-readable mirror:
[`pre-g9b-r6-plus-f4-d-scope-amendment-01.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-d-scope-amendment-01.json).

## 1. Author decision (essentials)

Option A: extend F4-D, as an amendment and not a new phase, with the minimum additional
correction that preserves the geometric integrity of embedded Type 1 glyph outlines. The
productive change is restricted to (1) `AbstractCharTable.toName(char)` (the previously
authorized delegation) and (2) `CharstringEncoder.move(double, double)`, so that the
required Type 1 moveto is emitted when the displacement is zero. No general FreeHEP
refactoring; nonzero displacements and unrelated output paths keep their behaviour; any
further productive modification needs a new author disposition. Required before freezing:
conformance to the Adobe Type 1 Font Format for initial and subsequent subpaths;
structural charstring tests (zero and nonzero displacement, repeated moveto, closed
contours, multiple subpaths); outlines against the source Java shapes and the raster
scenes; PostScript/EPS reachability; unchanged metrics, extraction, embedding, positioning
and non-affected exports; the approved normalization policy without new exclusions.
External Ghostscript and Poppler rendering and reader checks over the affected glyph
cohort, Spanish accented characters and alternative fonts or sizes; intrinsic outline
correctness distinguished from rasterization; the xref warning stays an independent
observation. `TD-PDF-3` is recorded with its relation to `TD-PDF-2`; neither closes before
verification and author acceptance. Earlier receipts are neither reclassified, overwritten
nor reused.

## 2. Entry gate (2026-10-14)

| # | Check | Result |
|---|---|---|
| 1 | HEAD, tree, parent | `aae7b12c…`, `f6732b15…`, `3d609397…` (exact) |
| 2 | linear ancestry | `847bb20a` → `089fc220` → `3251f085` → `83f07931` → `0c788526` → `3d609397` → `aae7b12c`; no merge commit |
| 3 | worktree | clean (no tracked or untracked change) |
| 4 | `main`, `origin/main`, live remote `main` | `847bb20a593e8092eabf80bd9d7c62ed7c21de08` |
| 5 | product delta `847bb20a..aae7b12c` | none (`source`, `apps`, `tools`, `geocedg/specs` unchanged) |
| 6 | WIP `6c2fc409` | tree `5c2c4f29…`, parent `3d609397`; nine changed files (one product line, test, three fixtures, manifest, registry, inventory, final-coverage pins) |
| 7 | `CharstringEncoder.java` | unchanged since the original import `e6a9568f`; `move()` still has `case LAST_POINT: break;` |
| — | result | **PASS**; branch `phase/pre-g9b-r6-plus-f4-d-amendment-01` created from `aae7b12c` |

## 3. Type 1 conformance (Adobe Type 1 Font Format, version 1.1, February 1993, §6.4)

- `hsbw` (p. 50) sets the left side-bearing point and "also sets the current point to
  (sbx, 0), but does not place the point in the character path"; the specification adds
  "Use rmoveto for the first point in the path". A charstring whose first path command is a
  line therefore has no defined subpath start; this is `TD-PDF-3`.
- `closepath` (p. 51) closes a subpath and, unlike PostScript, "does not reposition the
  current point"; a subsequent `rmoveto` is relative to the point in force before
  `closepath`. The FreeHEP encoder keeps its own rounded current point unchanged across
  `closePath`, which matches; a subpath that starts at that point is the subsequent-subpath
  case of the same defect.
- `hmoveto` (p. 52) is "equivalent to dx 0 rmoveto"; `0 hmoveto` is therefore a valid
  explicit moveto of zero displacement, and it begins the subpath at the current point.
- Consecutive movetos follow PostScript `rmoveto` semantics (the later point replaces the
  earlier one), so an added zero moveto never adds geometry.

The correction: in `move()`, the `LAST_POINT` case (rounded target equal to the current
point) writes `0 hmoveto` instead of nothing. The `BOTH`, `HORIZONTAL` and `VERTICAL`
cases (nonzero displacements), `line()`, `cubic()`, `closePath()`, `startChar()` and
`endchar()` are untouched.

## 4. Source-based impact analysis and class

- **Reachability.** `CharstringEncoder` is instantiated only in
  `FontEmbedderType1.writeGlyph`; `FontEmbedderType1` has one subclass,
  `PDFFontEmbedderType1`, created only in `PDFFontTable.embedAll` when `EMBED_FONTS` is true,
  `EMBED_FONTS_AS` is Type 1 and the font is not a PDF standard font. `embedAll` runs at
  the end of a `PDFGraphics2D` export; production `PDFGraphics2D` instances exist only in
  `PictureExportService.writePDF` and `GraphicExportDialog.exportPDF`, both of which set
  Type 1 embedding when "Export text as shapes" is off. The product fonts are
  `SansSerif.plain` and `Serif.plain` derivatives, which FreeHEP neither substitutes nor
  treats as standard fonts, so every embedded-font PDF with text reaches the encoder.
- **PostScript/EPS.** No PostScript or EPS writer is vendored: there is no
  `org/freehep/graphicsio/ps` package and no `PSGraphics2D`. The shared enum
  `App.ExportType` declares an `EPS` constant, but no source references it, and
  `GraphicExportDialog` mentions EPS only in two comments (lines 108 and 556); the only
  FreeHEP service registration is an image writer. SVG
  (`SVGFontTable`, `SVGGlyph`), EMF and PNG do not use the encoder. The PostScript/EPS
  paths are therefore unreachable by source evidence.
- **Base outputs.** On the base, the embedded route ends in `StackOverflowError` inside
  `writeString`, before `embedAll`; the encoder's output never reached a written file, so
  no output that exists on the base can change.
- **Class.** The regression perimeter — the embedded Type 1 PDF route through both entry
  points — is bounded and is covered by the F4-D selection (obligations 1–9 and the new
  structural class), `INTEGRATION` (`final.desktop` and the rest of the registered
  integration nodes), `INFRA_UNIT` for the registration, `STATIC`, and the external
  rendering evidence. `INTEGRATED_PHASE` is **maintained**; the acceptance requirements
  are unchanged: `PHASE` `PRE-G9B-R6-PLUS-F4-D` + `INTEGRATION` + `INFRA_UNIT` + `STATIC`
  once each on the clean, immutable candidate.

## 5. Plan

- Selective reuse of the WIP: the `AbstractCharTable` line, `PreG9BR6PlusF4DEmbeddedFontPdfTest`
  and its base fixtures (unchanged scene; the o6–o8 base comparisons stay valid), the
  manifest entries and the registry entries. Inventory, catalog pin and final-coverage pins
  are regenerated for the amended tree; no WIP receipt exists or is reused.
- New focused class for the encoder and the outlines: structural charstring cases;
  every glyph of the product fonts (sans and serif, plain, bold, italic, bold-italic)
  against the Java shape FreeHEP encodes (each point equal to the rounded source point);
  every glyph of every embedded program of product PDFs begins each subpath with a moveto;
  widths and encoding unchanged; a glyph-cohort scene for the external evidence.
- Reproduction: the new class run on the WIP product (one-line correction only) must fail
  on the missing movetos.
- External evidence on the committed candidate: Ghostscript and Poppler rendering against
  the text-as-shapes PDFs and between renderers, font, text and structure checks, for the
  focused scene and the cohort scene.
- Normalization: unchanged (PDF date strings, SVG clip identifiers); a new exclusion is a
  stop.

## 6. Files of this record

This record, its mirror, the roadmap (5.42) and the mini-track status. No product, test,
registry, specification or prompt change.

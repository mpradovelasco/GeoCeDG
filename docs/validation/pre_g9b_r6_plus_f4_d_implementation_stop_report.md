# PRE-G9B-R6-plus-F4-D — implementation stop report (Type 1 glyph outlines)

```text
RECORD_KIND               = IMPLEMENTATION STOP REPORT (documentary)
PHASE                     = PRE-G9B-R6-plus-F4-D
AUTHORIZATION             = PRE-G9B-R6-plus-F4-D IMPLEMENTATION AUTHORIZATION (2026-10-13),
                            recorded in 3d609397cf82c428872a0b6859426aba7c0efdeb
APPROVED BASE             = 0c7885260761f442c25b380fcbbae11d1215fb51
                            tree e75fb9c6b55a1677380802766300f514323762d6
STOP CONDITIONS MET       = canonical prompt "After the correction, embedded-font PDFs are
                            … rendered incorrectly" and "Any change beyond the single line
                            appears necessary"
F4-D TECHNICAL CANDIDATE  = NOT ESTABLISHED (no product change on this branch)
WORK IN PROGRESS          = local branch wip/pre-g9b-r6-plus-f4-d-type1-outline-blocked,
                            commit 6c2fc40918be3e43a1df09a454f407be85acd553
                            tree 5c2c4f292ce0144f83e298c8240a48e6d63470cf
                            parent 3d609397 (unverified; not a candidate)
VERIFICATION_CLASS        = INTEGRATED_PHASE (frozen by the authorization; no acceptance run:
                            PHASE, INTEGRATION and INFRA_UNIT not run on any candidate)
NEW FINDING               = TD-PDF-3 (proposed): inherited FreeHEP Type 1 charstrings without
                            an initial moveto — 43 of 230 glyphs of the embedded font
F4-D AUTHOR SMOKE         = NOT APPLICABLE (no candidate)
F4-A IMPLEMENTATION       = NOT AUTHORIZED
PHASE G                   = NOT AUTHORIZED
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
```

The F4-D implementation stopped before freezing a technical candidate. The authorized
one-line correction removes the `StackOverflowError` and produces complete PDFs with an
embedded Type 1 font and the correct text, but the external rendering checks show that
the embedded glyph outlines of many common letters are distorted. The cause is a second,
independent inherited FreeHEP defect whose correction lies outside the authorized single
line. This report records the facts and the options for the author's disposition. The
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md)
and the [authorization record](pre_g9b_r6_plus_f4_d_implementation_authorization_record.md)
are unchanged. Machine-readable mirror:
[`pre-g9b-r6-plus-f4-d-implementation-stop.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-d-implementation-stop.json).

## 1. What was done (development evidence, not acceptance)

All of it is on the WIP branch, none of it on this branch:

- Base reproduction: the new focused class `PreG9BR6PlusF4DEmbeddedFontPdfTest`, run on the
  base product (only the test file added, `AbstractCharTable.java` blob `22cb250d` =
  baseline), failed in the four routes — GeoCeDG physical, GeoCeDG legacy, GeoCeDG picture
  dialog and Classic dialog — with `StackOverflowError` at
  `AbstractCharTable.toName(AbstractCharTable.java:35)`; the delegation test failed the same
  way. The base fingerprint fixtures (obligations 6–8) were produced in that run and
  reproduced byte-identically by an independent second JVM.
- The one line `return toName(Character.valueOf(c)); // GeoCeDG (2026-10-14):
  PRE-G9B-R6-plus-F4-D`. Compiled delta: only the body of `toName(char)` changes (the
  self-call becomes `Character.valueOf(char)` and `toName(Character)`); the other
  disassembly differences are constant-pool renumbering.
- Focused class on the corrected product: 8 of 8 passed (delegation for every `char` of
  PDFLatin, Symbol and Zapfdingbats; obligations 1, 2, 3, 4-text, 6, 7, 8). Phase selection
  `pre-g9b-r6-plus-f4-d.desktop` (the new class and the five obligation-9 classes): 45 of 45
  passed. `final.desktop` producer: 2107 tests, 0 failures, 1 allow-listed disabled upstream
  case, no heap failure. Upstream boundary green (1060 registered files); Checkstyle: no
  finding on the new or changed files.
- Registration: phase `PRE-G9B-R6-PLUS-F4-D`, producer and projection nodes, selection
  inventory through the official updater (`discovery.desktop` 2113, `final.desktop` 2107,
  F4-D selection 45), registry catalog pin and final-coverage pins.

## 2. The finding: distorted embedded glyph outlines

**Observation.** External rendering of the embedded-font PDFs (Ghostscript 10.02.1 and
Poppler 24.04.0 `pdftoppm`, aliasing off) was compared with the text-as-shapes PDF of the
same scene: ink = any non-white pixel; a pixel is matched when the other rendering has ink
within a Chebyshev radius r. The embedded rendering has a thin wedge of extra ink on the
left stems of `D`, `F` and `ñ`: their bottom-left corner is pulled to the left.

| Route (scene of the focused class) | Renderer | unmatched embedded ink at r = 5 px, 1200 dpi (0.106 mm) | at r = 5 px, 2400 dpi (0.053 mm) |
|---|---|---|---|
| GeoCeDG physical, legacy, dialog | Ghostscript | 0 | 236 px (0.05–0.10 % of ink) |
| GeoCeDG physical, legacy, dialog | Poppler | 0 | 237 px (0.05–0.10 %) |
| Classic dialog (larger text) | Ghostscript | 1246 px (1.06 %) | 8072 px (1.72 %) |
| Classic dialog | Poppler | 1414 px (1.22 %) | 8478 px (1.83 %) |

The text-as-shapes ink is fully matched at r = 5 in every comparison except 2 px in the
Classic 2400 dpi Ghostscript comparison, and Ghostscript and Poppler agree with each other
on the embedded PDF within 2 px at 1200 dpi in all four routes:
the distortion is in the PDF, not in a renderer. In the Classic scene the bottom-left corner
of `D` is displaced by about 30 px at 2400 dpi (about 0.3 mm); the deviation scales with the
text size.

**Cause (decoded).** The embedded Type 1 program was decrypted (eexec, hex-encoded; then
the charstrings, `lenIV` 4). In the affected glyphs the first command after `hsbw` is a
line (`vlineto`) with no `rmoveto`/`hmoveto`/`vmoveto`, e.g.
`D: [77, 722] hsbw ; [716] vlineto ; …`, whereas `C` and `o` begin with a moveto. In
`org.freehep.graphicsio.font.CharstringEncoder`, `startChar` sets the encoder's current
point to the side bearing (as `hsbw` does), and `move()` writes nothing when the rounded
first point equals that current point (`case LAST_POINT: break;`). Type 1 `hsbw` sets the
current point but does not place it in the path, so a contour that starts exactly at the
side-bearing point on the baseline begins without a moveto, and the renderers close it
towards a different start — the slanted stem. **43 of the 230 glyphs** of the embedded
program are affected, in both the GeoCeDG and the Classic PDF: `period`, `A B D E F H I K L
M N P R X Z`, `h k l m n r x z`, `Zcaron`, `dotlessi`, `zcaron`, `Agrave Aacute Acircumflex
Atilde Adieresis Aring AE`, `Egrave Eacute Ecircumflex Edieresis`, `Ntilde`, `Thorn`,
`germandbls`, `ntilde`, `.notdef`.

**Pre-existing and latent.** `CharstringEncoder.java` is unchanged since the original
import of the GeoGebra tree (`e6a9568f`), and `FontEmbedderType1.java` changed upstream only
in Javadoc (`6209fea1`, a boxing-only commit for the other FreeHEP files). The defect was
therefore present before `c4fd03ce`, but no embedded-font PDF could be written since that
commit, so it was latent behind `TD-PDF-2`. `FontEmbedderType1.writeGlyph` carries an
inherited `FIXME` about characters "displaced when using the correct sidebearing". The
correction authorized by F4-D does not cause the distortion; it makes it reachable.

**Scratch feasibility (not in the tree).** A page of stem and round glyphs at 40 pt was
written three times with the corrected `AbstractCharTable`: text as shapes, embedded with
the current encoder, and embedded with a scratch encoder whose `move()` also emits a zero
`hmoveto` in the `LAST_POINT` case (two added lines). At 1200 dpi:

| Embedded program | Ghostscript, unmatched embedded ink at r = 10 px (0.21 mm) | Poppler |
|---|---|---|
| current encoder | 103 914 px (5.6 %) | 105 928 px (5.7 %) |
| scratch encoder with the initial moveto | 0 (0 already at r = 2 px, 0.042 mm) | 0 (0 at r = 2 px) |

With the scratch encoder every glyph begins with a moveto (0 of 230 affected), e.g.
`D: [77, 722] hsbw ; [0] hmoveto ; [716] vlineto ; …`. This is feasibility evidence for an
option, not an implemented change.

## 3. Why this is a stop

The canonical prompt makes both of the following binding stop conditions:

- "After the correction, embedded-font PDFs are empty, incomplete, without the font
  program, or render incorrectly." — 43 glyphs, including the most frequent capitals and
  the Spanish `ñ`/`Ñ`, render with a distorted left stem in two independent renderers.
- "Any change beyond the single line appears necessary." — correcting the outlines needs a
  change in `CharstringEncoder`, a second inherited FreeHEP file.

A raster tolerance could have been stated loosely enough to pass the GeoCeDG scene; that
would hide a rendering difference, which approved item 7 forbids, and the Classic scene
exceeds a 0.1 mm tolerance anyway. The in-JVM tests passed because they check structure and
text, not glyph outlines; this is why the external rendering evidence is reviewed before
acceptance.

## 4. Options for the author

| Option | Content | Effect | Recommendation |
|---|---|---|---|
| **A** — extend F4-D with a second minimal inherited correction | `CharstringEncoder.move()` also emits the moveto when the first point equals the current point (scratch: two lines); registered in `modified-files.yml`; a focused test that every glyph charstring of the embedded program begins with a moveto, plus the raster evidence | embedded glyphs match the text-as-shapes outlines within 0.042 mm at 40 pt in Ghostscript and Poppler; perimeter: `CharstringEncoder` is used only by `FontEmbedderType1` ← `PDFFontEmbedderType1` ← `PDFFontTable`, i.e. the embedded Type 1 PDF route, which wrote nothing before F4-D; text-as-shapes PDF, SVG, EMF, PNG and DXF untouched | **recommended**; needs a new author instruction amending the F4-D scope (two inherited files) and confirming the class |
| **B** — accept the one-line correction with the outline defect recorded | `TD-PDF-3` as a known limitation; stop condition 2 waived by the author | embedded-font PDFs become writable but 43 glyphs have visibly distorted stems, more visibly at large sizes | not recommended |
| **C** — withdraw or defer F4-D | no product change; `TD-PDF-2` stays open; text as shapes remains the only working PDF route | status quo | possible if the embedded route is not needed now |

Option D (switching the embedding to Type 3 fonts) is not proposed: it would change the
picture dialog options and `PictureExportService`, which are explicitly forbidden scope, and
would need its own design.

Whatever the choice, the WIP branch keeps the verified one-line correction, its tests,
fixtures and registration for reuse; under option A its registration would be extended and
the evidence regenerated on a new candidate.

## 5. Preserved evidence

Ignored folder `artifacts/pre-g9b-r6-plus-f4-d/` (not tracked):

- `pre-freeze/pdfs/` — the eight PDFs of the focused scene (four routes, embedded and text as
  shapes) from the WIP product; `pre-freeze/external/` — Ghostscript, `pdfinfo`, `pdffonts`,
  `pdftotext` and raster outputs, diff images, `external-evidence.log` (every command, exit
  code and message) and `external-evidence.json`.
- `stop-evidence/` — the feasibility probe PDFs (`probe-text-as-shapes.pdf`,
  `probe-embedded-current-encoder.pdf`, `probe-embedded-scratch-moveto-encoder.pdf`, readable
  in the author's PDF reader), the `D` crops of the Classic scene, the scratch tools
  (`F4DRasterCompare`, `F4DType1Dump`, `F4DCrop`, the evidence script) and the scratch
  encoder variant.

External checks of the focused scene, per route (pre-freeze, WIP product): Ghostscript
structural run (`-dPDFSTOPONERROR`) exit 0 with the pre-existing "incorrect xref size" /
"errors that were repaired or ignored" warning on all eight PDFs, embedded and text as shapes (`OBS-F4P-FREEHEP-XREF-SIZE`, unchanged and out of
scope); `pdfinfo` 1 page, PDF 1.4; `pdffonts` one font `F1`, Type 1, embedded; `pdftotext`
(Poppler) extracts `Cota ñ á é Ü (x)`, `GeoCeDG F4-D 0123`, `Seg` and the dimension value
(`40 mm` physical, `40` legacy and Classic). A second reader in the author's hands was not
used (no candidate). Tool versions: Ghostscript 10.02.1, Poppler 24.04.0 (MiKTeX), Java
22.0.2 for the scratch tools.

## 6. State after this report

`F4-D TECHNICAL CANDIDATE = NOT ESTABLISHED`; `F4-D TECHNICAL ACCEPTANCE` and `AUTHOR SMOKE`
not applicable until a candidate exists; `TD-PDF-2` open; `TD-PDF-3` proposed for
registration by the author; F4-A, G and publication not authorized. The next step is an
author decision among the options of §4.

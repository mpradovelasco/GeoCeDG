# PRE-G9B-R6-plus-F4-D — FreeHEP embedded-font correction (scope amendment 01): candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-F4-D (scope amendment 01; not a new phase)
DEFECTS                   = TD-PDF-2 (AbstractCharTable.toName(char) self-call)
                            TD-PDF-3 (CharstringEncoder: no Type 1 moveto for a zero
                            displacement; latent behind TD-PDF-2)
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE — MAINTAINED by the amendment's impact analysis
                            (executable registry, not the archived verification-levels §12.8)
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F4-D,
                            -Profile INTEGRATION, -Profile INFRA_UNIT, -Profile STATIC
                            (once each on the exact candidate commit and tree; reported outside
                            this file)
FINAL                     = NOT RUN (above the frozen class)
PRODUCT CHANGE            = YES — two inherited FreeHEP files of the Desktop module:
                            AbstractCharTable.toName(char) (1 line) and
                            CharstringEncoder.move(double,double) (2 lines and a 2-line notice)
SHARED / KERNEL CHANGE        = NONE
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE (the embedded-font PDF, which failed before, is written)
GUIDE_IMPACT              = NONE
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED (no workstation prerequisite changes; the
                            external renderers used for evidence stay outside the contract)
TD-PDF-2, TD-PDF-3        = OPEN (closed only after verification and author acceptance)
F4-D TECHNICAL ACCEPTANCE = PENDING
F4-D AUTHOR SMOKE         = PENDING (not performed or attributed by the agent)
F4-A IMPLEMENTATION       = NOT AUTHORIZED
PHASE G                   = NOT AUTHORIZED
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no author-approval
field: a later author decision naming the exact technical commit is the sole authority for
acceptance, for the disposition of `TD-PDF-2` and `TD-PDF-3` and for publication. The
candidate commit cannot name itself, so its identity and the acceptance runs made on it
are reported outside this file. Machine-readable mirror:
[`pre-g9b-r6-plus-f4-d-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-d-candidate-evidence.json).

## 1. Authority and lineage

```text
847bb20a  published main (anchor)
089fc220 → 3251f085 → 83f07931 → 0c788526  F4 preparation, reconciliation, amendment,
                                            author design closeout
3d609397  F4-D implementation authorization (INTEGRATED_PHASE frozen)
aae7b12c  F4-D implementation STOP (distorted Type 1 outlines); accepted by the author as
          correct, kept as historical evidence (STATIC verification-ff489c0e)
39687713  scope amendment 01 record (option A; INTEGRATED_PHASE maintained)
          tree c88143b02760e88ea18d1b4185a0693b55eb31bf
<this>    technical candidate (branch phase/pre-g9b-r6-plus-f4-d-amendment-01)
```

Governing records: the [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md)
(unchanged), the [authorization record](pre_g9b_r6_plus_f4_d_implementation_authorization_record.md),
the [stop report](pre_g9b_r6_plus_f4_d_implementation_stop_report.md) and the
[scope amendment 01 record](pre_g9b_r6_plus_f4_d_scope_amendment_01_record.md). The WIP
commit `6c2fc409` (not a candidate) was reused selectively: the `AbstractCharTable` line,
`PreG9BR6PlusF4DEmbeddedFontPdfTest` and its three base fixtures (byte-identical), the
manifest and registry entries. Inventory, catalog pin and final-coverage pins were
regenerated for this tree; no earlier receipt exists for or is reused by this candidate.

## 2. Product delta

```diff
--- a/source/desktop/desktop/src/main/java/org/freehep/graphics2d/font/AbstractCharTable.java
+++ b/source/desktop/desktop/src/main/java/org/freehep/graphics2d/font/AbstractCharTable.java
@@ -32,7 +32,7 @@ public abstract class AbstractCharTable implements CharTable {
     @Override
     public String toName(char c) {
-        return toName(c);
+        return toName(Character.valueOf(c)); // GeoCeDG (2026-10-14): PRE-G9B-R6-plus-F4-D
     }
--- a/source/desktop/desktop/src/main/java/org/freehep/graphicsio/font/CharstringEncoder.java
+++ b/source/desktop/desktop/src/main/java/org/freehep/graphicsio/font/CharstringEncoder.java
@@ -135,6 +135,10 @@ public class CharstringEncoder extends QuadToCubicPathConstructor {
             writeCommand(4);
             break;
         case LAST_POINT:
+            // GeoCeDG (2026-10-14): PRE-G9B-R6-plus-F4-D a Type 1 subpath begins with
+            // a moveto, also for a zero displacement (0 hmoveto)
+            writeNumber(0);
+            writeCommand(22);
             break;
         }
```

**Justification.** `TD-PDF-2`: since upstream `c4fd03ce` the `char` overload called
itself; `Character.valueOf(c)` restores the delegation to `toName(Character)` that every
encoding table implements. `TD-PDF-3`: `CharstringEncoder.move()` wrote nothing when the
rounded target equalled the encoder's current point (`LAST_POINT`). The Adobe Type 1 Font
Format 1.1 (§6.4) states that `hsbw` "does not place the point in the character path" and
to use a moveto for the first point, and that `closepath` does not reposition the current
point; a subpath starting at that point therefore had no moveto, and 43 of 230 glyphs had
no defined subpath start. `0 hmoveto` ("equivalent to dx 0 rmoveto") begins the subpath
there. The `BOTH`, `HORIZONTAL` and `VERTICAL` cases, `line()`, `cubic()`, `closePath()`,
`startChar()` and `endchar()` are unchanged. Both files are registered in
`docs/upstream/modified-files.yml` as modified third-party FreeHEP source with provenance,
each with a dated GeoCeDG notice at the change.

**Compiled delta.** Each class was compiled from its original blob and from the
candidate (`javac --release 17` against the Desktop and shared build classes) and the
`javap -c -p` disassemblies were compared with constant-pool indices removed:
`AbstractCharTable` (blob `22cb250d` → `52c0ff23`) differs only in the body of
`toName(char)` (the self-call `invokevirtual toName:(C)` becomes `invokestatic
Character.valueOf:(C)` and `invokevirtual toName:(Ljava/lang/Character;)`), the four
methods otherwise identical; `CharstringEncoder` (blob `d665899d` → `e6d6c6f7`) differs
only in `move(double, double)` among its 23 members: the `rmoveto`, `hmoveto` and
`vmoveto` branches are identical and the `LAST_POINT` branch, previously empty, now calls
`writeNumber(0)` and `writeCommand(22)`.

**Perimeter (amendment record §4, re-checked).** `toName(char)` is reached only through
`AbstractCharTable.toEncoding(char)` ← `FontUtilities.showString` ←
`PDFGraphics2D.showCharacterCodes` ← `writeString` (text written as text);
`Lookup.toName(char)` also delegates but has no caller. `CharstringEncoder` is used only
by `FontEmbedderType1` ← `PDFFontEmbedderType1` ← `PDFFontTable.embedAll` (embedding as
Type 1, non-standard font). Production `PDFGraphics2D` instances: `PictureExportService.writePDF`
and `GraphicExportDialog.exportPDF`. No PostScript/EPS writer is vendored (`ExportType.EPS`
unreferenced); SVG, EMF and PNG use neither class. On the base the embedded route ended in
`StackOverflowError` before any font program was written, so no output that exists on the
base can change.

## 3. Reproductions

| State | Result |
|---|---|
| base product (`0c788526`/`3d609397`, F4-D WIP test only) | all four embedded routes and the delegation test end in `StackOverflowError` at `AbstractCharTable.java:35` (recorded in the stop report) |
| one-line state (this branch with only the `AbstractCharTable` line) | `PreG9BR6PlusF4DType1OutlineTest`: 7 of 8 fail — every zero-displacement case lacks its `hmoveto`; `/period` of the product sans font and `/D` of both cohort PDFs show drawing commands without a moveto; the nonzero-displacement test passes |
| candidate | both F4-D classes: 16 of 16 pass |

## 4. Tests

Two focused classes, both in the phase selection, with the embedded-host lifecycle owner,
an in-memory unit-preference store and the Desktop logger. Every assertion fails, none
skips.

**`PreG9BR6PlusF4DEmbeddedFontPdfTest`** (from the WIP, unchanged except five helpers made
package-private for reuse; base fixtures byte-identical). Scene: a labelled segment, two
construction texts at four times the default size (one with `ñ á é Ü` and parentheses),
an aligned dimension; GeoCeDG at 1:1 in mm (physical) and without a unit (legacy), and
Classic.

| Test | Obligation | Assertion |
|---|---|---|
| `characterLookupDelegatesToTheCharacterOverloadForEveryCharacter` | delegation (`TD-PDF-2`) | for PDFLatin, Symbol and Zapfdingbats and every `char` 0–0xFFFF, `toName(char)` = `toName(Character)` and `toEncoding(char)` = the encoding of that name |
| `obligation1EmbeddedFontRoutesFinishWithoutStackOverflow` | (1) | GeoCeDG physical, legacy and picture-dialog routes and the Classic dialog route write a file without `StackOverflowError` |
| `obligation2EmbeddedFontPdfsAreComplete` | (2) | non-empty; header; `%%EOF`; trailer; `startxref`; a page with text and linework in its content stream |
| `obligation3TextFontsAreType1AndEveryNonStandardFontIsEmbedded` | (3) | every text font is `/Type /Font` `/Subtype /Type1`; every non-standard-14 font has a `/FontFile` Type 1 program; at least one embedded per route |
| `obligation4ExtractedTextEqualsTheSourceStrings` | (4), text | text decoded through each font's `/Differences` equals the drawn strings |
| `obligation6TextAsShapesPdfsAreIdenticalToTheBase` | (6) | the four text-as-shapes PDFs equal the base fixture after the date normalization |
| `obligation7ClassicRasterAndEmfAreIdenticalToTheBase` | (7) | Classic PNG pixels and EMF bytes equal the base fixture |
| `obligation8SvgWithAndWithoutEmbeddedFontsIsIdenticalToTheBase` | (8) | GeoCeDG physical and legacy and Classic SVG, with and without embedded fonts, equal the base fixture after the clip normalization |

**`PreG9BR6PlusF4DType1OutlineTest`** (new, scope amendment 01). A Type 1 interpreter in the
test decodes charstrings (number and command encoding §6.2–6.3, eexec and charstring
decryption §7) with the specification's semantics: `hsbw` sets the current point without
starting a subpath, a line or curve outside a subpath begun by a moveto is a violation,
`closepath` keeps the current point.

| Test | Requirement | Assertion |
|---|---|---|
| `zeroDisplacementInitialMovetoBeginsTheSubpath` | zero-displacement moveto | a contour starting at the side-bearing point encodes as `hsbw 77 722`, `hmoveto 0`, …; no violation |
| `nonzeroDisplacementMovetosKeepTheirEncoding` | nonzero displacement unchanged | `rmoveto`, `vmoveto`, `hmoveto`, a two-byte operand and a fractional target encode to the bytes given by the specification |
| `repeatedMovetosEachEmitAMoveto` | repeated moveto | three movetos (one of zero displacement) each emit a moveto |
| `closedContourFollowedBySubpathAtTheCurrentPoint` | closed contours, subsequent subpath | a second subpath starting at the point in force after `closepath` begins with `hmoveto 0` |
| `multipleSubpathsWithCurvesMatchTheSourceShape` | multiple subpaths | a rectangle, an ellipse (cubics) and a quadratic contour decode to the rounded source path |
| `everyGlyphOfTheProductFontsEqualsItsJavaShape` | outlines against the Java shapes | for the product sans and serif fonts in plain, bold, italic and bold-italic (FreeHEP's size-1000 font and render context), every glyph FreeHEP encodes decodes to exactly its Java shape with each point rounded to font units (quadratic segments raised to cubic exactly), `hsbw` equal to the rounded side bearing and advance; no violation; each font has glyphs starting at the side bearing |
| `geocedgCohortPdfStartsEverySubpathWithAMoveto` | product PDF, GeoCeDG | a page of the affected cohort, the accented glyphs and a Spanish sentence in sans and serif, four styles and two sizes: six embedded programs; in every glyph of every program each subpath begins with a moveto; `/Widths` equal the `hsbw` widths; the page text equals the drawn strings |
| `classicCohortPdfStartsEverySubpathWithAMoveto` | product PDF, Classic | the same through the Classic dialog writer |

Obligation (9) is carried by the existing classes kept in the selection:
`PostE2P1R2PhysicalPdfStyleTest`, `PostE2P1R2BaseFingerprintTest`,
`PreG9BR6PlusBPictureFidelityTest`, `PreG9BR6PlusCBaseIdentityTest`,
`PreG9BR6PlusCPhysicalExportTest`. Both classes restore the JVM-wide FreeHEP default
properties after Classic exports. `GEOCEDG_PRE_G9B_R6_PLUS_F4_D_PDF_OUT` copies the
exported PDFs for the external evidence; `GEOCEDG_PRE_G9B_R6_PLUS_F4_D_FINGERPRINT_OUT`
writes the base digests instead of comparing (fixture production).

## 5. Output regression and normalization

Only the exclusions the existing fingerprint tests already apply are used; no new
exclusion was needed or introduced:

| Format | Pattern | Replacement | Field | Origin |
|---|---|---|---|---|
| PDF | `\(D:[0-9+\-Z']*\)` | `(D:)` | `/CreationDate` and `/ModDate` strings | `PostE2P1R2BaseFingerprintTest`, `PreG9BR6PlusCBaseIdentityTest` |
| SVG | `clip[0-9a-f-]+` | `clip` | generated clip-path identifiers | same |

The F4-D fixtures (`o6`, `o7`, `o8-base-fingerprints.json`, 12 digests) were produced on
the base product and are reused unchanged; the candidate reproduces every digest. The
embedded-font PDF has no base counterpart (it failed); it is checked structurally,
semantically, against the Java shapes and by rendering (§4, §6).

## 6. External PDF validation (focused evidence, enforced by no executable profile)

Run before the freeze on PDFs written by the two focused classes from this tree
(`AbstractCharTable` blob `52c0ff23`, `CharstringEncoder` blob `e6d6c6f7`, test blobs
`04f61757` and `3d5b42d6`), with `GEOCEDG_PRE_G9B_R6_PLUS_F4_D_PDF_OUT`; repeated on the
frozen candidate and reported outside this file. Every command, exit code and message is
in `artifacts/pre-g9b-r6-plus-f4-d/amendment-pre-freeze/external/external-evidence.log`
and `external-evidence.json` (ignored folder).

**Tools.** Nothing required was unavailable.

| Tool | Version | Path | Use |
|---|---|---|---|
| Ghostscript `gswin64c` | 10.02.1 | `C:\Program Files\gs\gs10.02.1\bin` | structure (`-dPDFSTOPONERROR -sDEVICE=nullpage`), rasters (`png16m`, 1200 and 2400 dpi, `-dTextAlphaBits=1 -dGraphicsAlphaBits=1`) |
| Poppler `pdfinfo`, `pdffonts`, `pdftoppm`, `pdftotext` | 24.04.0 | MiKTeX `miktex\bin\x64` | structure, font embedding, rasters (`-r 1200/2400 -aa no -aaVector no`), text |
| Xpdf `pdftotext` | 4.06 | Git for Windows `mingw64\bin` | independent text extraction |
| scratch tools `F4DRasterCompare`, `F4DType1Check`, `F4DPdfObjectDiff` | Java 22.0.2 | `artifacts/pre-g9b-r6-plus-f4-d/stop-evidence/` and the session scratchpad | raster comparison, Type 1 structure, object comparison |

**Scenes.** The focused scene of obligations 1–8 through the four routes, and the glyph
cohort of `PreG9BR6PlusF4DType1OutlineTest` (the 43 affected glyphs, the accented and
special Latin glyphs, a Spanish sentence; sans and serif in four styles; two sizes) through
GeoCeDG (physical, font sizes about 1.9 and 3.8 mm) and Classic (24 and 48 px font
size) — each
embedded and as text as shapes. A non-product font (Arial 40 pt, FreeHEP writer directly,
candidate classes) completes the alternative fonts and sizes.

**Intrinsic outline correctness** (independent of any rasterizer):

| Check | Result |
|---|---|
| decoded charstrings against the Java shapes FreeHEP encodes (`everyGlyphOfTheProductFontsEqualsItsJavaShape`, 8 font variants) | every point equals the rounded source point; `hsbw` equals the rounded side bearing and advance |
| `F4DType1Check` on every embedded program (1 per focused scene, 6 per cohort PDF, 1 Arial) | 230 glyphs each; 0 glyphs without an initial moveto; 0 drawing commands outside a subpath (the same tool reports 43 glyphs and 606 defects on the one-line-state PDFs) |
| object comparison one-line state → candidate (four focused embedded PDFs) | 17 of 19 objects identical; only the `FontFile` stream and its length object differ; font dictionary, `/Widths`, `/Encoding`, `/FontDescriptor`, page content (text positions and matrices) and trailer identical; text-as-shapes PDFs fully identical |

**Structure, fonts and text.** Ghostscript exit 0 for every embedded and text-as-shapes PDF,
each with the pre-existing "incorrect xref size" warning (`OBS-F4P-FREEHEP-XREF-SIZE`,
independent, unchanged). `pdfinfo`: one page, PDF 1.4 (GeoCeDG 170.079 × 85.0394 pt and
283.465 × 240.945 pt; Classic 170 × 85 pt and 284 × 241 pt). `pdffonts`: every font
`Type 1`, `emb yes` (one per focused scene, six per cohort PDF). Poppler and Xpdf extract
`Cota ñ á é Ü (x)`, `GeoCeDG F4-D 0123`, `Seg` and the dimension value (`40 mm` physical,
`40` legacy and Classic), and in the cohort PDFs every line, e.g. `ÀÁÂÃÄÅÆ ÈÉÊË Ñ ñ Þ ß Ž
ž ı` and `¿Año? ¡Ñandú! Cigüeña, éxito, Ángulo`.

**Glyph rendering.** Ink = any non-white pixel of an aliased rendering; a pixel is matched
when the other rendering has ink within a Chebyshev radius r. **Stated tolerance** (as in
the stop report): r = 5 px at 1200 dpi (0.106 mm), zero unmatched pixels in both
directions. Values are unmatched pixels "embedded-only / shapes-only":

| Scene | Comparison | r = 0 | r = 1 | r = 2 | r = 5 |
|---|---|---|---|---|---|
| geocedg_physical | gs1200 embedded vs shapes | 2 / 3577 | 0 / 6 | 0 / 0 | 0 / 0 |
| geocedg_physical | pp1200 embedded vs shapes | 317 / 3810 | 0 / 259 | 0 / 0 | 0 / 0 |
| geocedg_physical | gs2400 embedded vs shapes | 10 / 7200 | 0 / 16 | 0 / 0 | 0 / 0 |
| geocedg_physical | pp2400 embedded vs shapes | 1259 / 8059 | 0 / 1201 | 0 / 0 | 0 / 0 |
| geocedg_physical | gs1200 vs pp1200 embedded | 5643 / 1641 | 6 / 44 | 0 / 0 | 0 / 0 |
| geocedg_legacy | gs1200 embedded vs shapes | 2 / 3470 | 0 / 6 | 0 / 0 | 0 / 0 |
| geocedg_legacy | pp1200 embedded vs shapes | 310 / 3716 | 0 / 243 | 0 / 0 | 0 / 0 |
| geocedg_legacy | gs2400 embedded vs shapes | 10 / 7018 | 0 / 5 | 0 / 0 | 0 / 0 |
| geocedg_legacy | pp2400 embedded vs shapes | 1182 / 7811 | 0 / 1141 | 0 / 0 | 0 / 0 |
| geocedg_legacy | gs1200 vs pp1200 embedded | 3693 / 1594 | 6 / 44 | 0 / 0 | 0 / 0 |
| geocedg_dialog | (identical embedded PDF to geocedg_physical after normalization) | same | same | same | same |
| classic_dialog | gs1200 embedded vs shapes | 4 / 7523 | 0 / 5 | 0 / 0 | 0 / 0 |
| classic_dialog | pp1200 embedded vs shapes | 761 / 8286 | 0 / 647 | 0 / 0 | 0 / 0 |
| classic_dialog | gs2400 embedded vs shapes | 490 / 14389 | 0 / 190 | 0 / 2 | 0 / 2 |
| classic_dialog | pp2400 embedded vs shapes | 3105 / 17974 | 0 / 2712 | 0 / 0 | 0 / 0 |
| classic_dialog | gs1200 vs pp1200 embedded | 7500 / 5434 | 30 / 22 | 0 / 0 | 0 / 0 |
| geocedg_cohort | gs1200 embedded vs shapes | 46 / 39753 | 0 / 83 | 0 / 0 | 0 / 0 |
| geocedg_cohort | pp1200 embedded vs shapes | 3253 / 43992 | 0 / 2553 | 0 / 0 | 0 / 0 |
| geocedg_cohort | gs2400 embedded vs shapes | 708 / 79589 | 0 / 161 | 0 / 0 | 0 / 0 |
| geocedg_cohort | pp2400 embedded vs shapes | 10061 / 90304 | 0 / 7845 | 0 / 0 | 0 / 0 |
| geocedg_cohort | gs1200 vs pp1200 embedded | 26746 / 27433 | 43 / 272 | 0 / 0 | 0 / 0 |
| classic_cohort | gs1200 embedded vs shapes | 183 / 53065 | 0 / 231 | 0 / 0 | 0 / 0 |
| classic_cohort | pp1200 embedded vs shapes | 6277 / 59128 | 0 / 5063 | 0 / 1 | 0 / 0 |
| classic_cohort | gs2400 embedded vs shapes | 1239 / 105966 | 0 / 352 | 0 / 0 | 0 / 0 |
| classic_cohort | pp2400 embedded vs shapes | 15601 / 122764 | 0 / 10429 | 0 / 0 | 0 / 0 |
| classic_cohort | gs1200 vs pp1200 embedded | 33399 / 33515 | 449 / 267 | 0 / 0 | 0 / 0 |
| Arial 40 pt | gs1200 embedded vs shapes | 2645 / 33397 | 0 / 1865 | 0 / 0 | 0 / 0 |
| Arial 40 pt | pp1200 embedded vs shapes | 2834 / 36162 | 0 / 962 | 0 / 0 | 0 / 0 |

Result: **the stated tolerance holds in every scene and both renderers** (0 / 0 at r = 5,
1200 dpi), where the stop report measured 1246 and 1414 embedded-only pixels in the Classic
scene. Embedded-only ink is matched within 2 px (0.042 mm at 1200 dpi, 0.021 mm at 2400 dpi)
in every one of the 26 embedded-versus-shapes comparisons. Two shapes-only residues remain
beyond 2 px: 1 px in the Classic cohort with Poppler at 1200 dpi (matched at r = 5) and an
isolated 1 × 2 px mark in the Classic focused scene with Ghostscript at 2400 dpi, with no ink
of either rendering within 5 px; that mark lies at the same coordinates in the stop report's
comparison, i.e. it belongs to the unchanged text-as-shapes output (identical to the base,
obligation 6), not to the embedded outlines.

**Rasterization versus outline.** The intrinsic outlines are exact to the font unit (above).
The remaining r = 0 and r = 1 differences are rasterization: the text-as-shapes route fills
glyph paths taken at the drawn size, the embedded route renders an unhinted 1000-unit Type 1
program, and the two renderers apply different scan-conversion rules (Ghostscript and
Poppler differ from each other on the same embedded PDF by the same order, matched at
r = 2). Before the amendment, the defect showed as embedded-only ink beyond 5 px; after it,
none remains at 2 px.

**Reader compatibility.** Ghostscript, Poppler and Xpdf read every PDF; no reader reported an
error other than the pre-existing xref repair. A reader other than these (the author's usual
one) is part of the author smoke (§10), not of this evidence.

## 7. Registration

| Item | Value |
|---|---|
| phase | `PRE-G9B-R6-PLUS-F4-D`: `compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-f4-d.semantic` |
| nodes | `junit.desktop.pre-g9b-r6-plus-f4-d.producer` (`PROCESS_PRODUCER`, `gradle-test-evidence-producer.ps1 -SelectionId pre-g9b-r6-plus-f4-d.desktop`) and `.semantic` (`EVIDENCE_PROJECTION`, `JUNIT_SELECTION`, `SEMANTIC`/`PRODUCT`); impact paths: the manifest, both changed FreeHEP files, `FontEmbedderType1`, `PDFFontEmbedderType1`, `PDFFontTable`, `FontUtilities`, `PDFGraphics2D`, `PictureExportService`, `GraphicExportDialog`, both F4-D classes and fixtures, the five obligation-9 classes and the test helpers |
| selection | `pre-g9b-r6-plus-f4-d.desktop`: 7 classes, **53** identities |
| inventory | official updater with discovery (`discovery.shared` 6075, `discovery.desktop` 2121) and executed evidence (`pre-g9b-r6-plus-f4-d.desktop` 53, `final.desktop` 2115); `final.shared` 6992 unchanged; 61 selections |
| registry pin | `junit_inventory` catalog pin reproduced from the WIP blob (`ddfed5e5…`) and set to `70c04abb9310423f31914b27eda8b0327761bf203342e22159476da412622694` |
| final-coverage pins | selection count 61, F4-D identity count 53, phase count 66 |
| upstream manifest | `AbstractCharTable.java` and `CharstringEncoder.java` (modified third-party FreeHEP source, provenance, authorized one-line and two-line exceptions), both test classes and the three fixtures (added); `Assert-GeoCeDGUpstreamBoundary` green with 1062 registered files |

## 8. Unchanged semantics

Kernel geometry, DAG and CeDG spatial semantics, Locus V2, units, drawing scale, export
area, text sizes and label offsets (`TD-PDF-1`, S3), physical stroke and marker sizes, page
box, DXF, LaTeX, EMF, SVG and PNG outputs, document identity, serialization and undo are
untouched. The picture dialog, its options and `PictureExportService` are unchanged. The
xref observation `OBS-F4P-FREEHEP-XREF-SIZE` stays an independent observation (§6).

## 9. Risks, debts and observations

| Id | Disposition in this candidate |
|---|---|
| `TD-PDF-2` | corrected technically (one line); **OPEN** until the required verification and the author's acceptance; recorded in the [F4 risk and debt register](pre_g9b_r6_plus_f4_risk_and_debt_register.md) §5 |
| `TD-PDF-3` | recorded in the same register §5 as a confirmed inherited defect owned by F4-D, latent behind `TD-PDF-2`; corrected technically (two lines); **OPEN** under the same condition |
| `F4R-EMBED-FIDELITY` | realized as `TD-PDF-3`; the remaining embedded-versus-shapes raster differences are measured in §6 |
| `F4R-SHAPES-UNCHANGED` | held: text-as-shapes PDFs, Classic PNG and EMF, and SVG equal the base after the existing normalization (obligations 6–8) |
| `OBS-F4P-FREEHEP-XREF-SIZE` | unchanged, independent observation outside this authorization (§6) |
| Classic FreeHEP defaults | the Classic writers still store their options in the JVM-wide FreeHEP defaults (pre-existing, unchanged); the tests restore them |
| FreeHEP provenance | the [FreeHEP provenance record](../licensing/freehep-vectorgraphics-provenance.md) has no section for the `graphics2d/font` and `graphicsio/font` families; its general rule (registration and a dated notice at the change) is followed; adding family sections is an author decision outside this authorization; `docs/licensing/component-matrix.md` still has no FreeHEP entry (pre-existing) |
| `FIXME` in `FontEmbedderType1.writeGlyph` | the inherited comment about characters "displaced when using the correct sidebearing" matches `TD-PDF-3`; the comment is not edited (outside the authorized lines) |
| external tools | Ghostscript, Poppler and Xpdf are outside the workstation contract; their checks are focused evidence enforced by no executable profile; a passing profile is not evidence for them |

No blocking issue is open at the time of writing.

## 10. Author-smoke checklist (not performed or attributed by the agent)

1. GeoCeDG, a document with a construction unit (e.g. mm) containing a labelled segment,
   a dimension and texts with `A B D E F H K L M N P R`, `h k l m n r`, `ñ Ñ á é Ü ¿ ¡`
   (also one text in bold or italic and one in the serif font): Graphics View as Picture →
   PDF with "Export text as shapes" **unchecked** → Save. The file is written (no "Save
   file failed", no 0-byte file).
2. Open it in the author's usual PDF reader (a reader other than Ghostscript): it opens
   without an error or repair prompt; zoom in strongly (400 % or more) on `D`, `F`, `N`,
   `h`, `n` and `ñ`: the left stems are vertical, with no slanted wedge; texts and the
   dimension value are where they are in the drawing.
3. Select and copy the text in the reader: the copied characters equal the drawn
   strings, accents included.
4. Document properties / fonts: embedded Type 1 fonts (one per family and style used).
5. Repeat 1–4 for a document without a construction unit (legacy route).
6. Classic diagnostic application: File → Export → Graphics View as Picture → PDF with
   "Export text as shapes" unchecked → Save; repeat 2–4.
7. Quick regression: the same documents with "Export text as shapes" **checked** (the
   default) in GeoCeDG and Classic, and an SVG with and without the option, look as
   before.
8. Optional: the PDFs evaluated in §6 are kept in the ignored folder
   `artifacts/pre-g9b-r6-plus-f4-d/amendment-pre-freeze/pdfs/`, and the same PDFs written
   by the frozen candidate in `artifacts/pre-g9b-r6-plus-f4-d/candidate/pdfs/` (reported
   outside this file); `geocedg_cohort-embedded.pdf` and `classic_cohort-embedded.pdf`
   contain the whole affected glyph cohort in six font variants.

## 11. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence, not
acceptance. The acceptance runs on the frozen candidate are reported with the candidate,
outside this file.

| Run | Result |
|---|---|
| `PreG9BR6PlusF4DType1OutlineTest` on the one-line state | 7 of 8 failed as expected (§3) |
| both F4-D classes on the candidate | 16 of 16 passed (also with the PDF copy hook) |
| producer `discovery.shared` / `discovery.desktop` (`-TestDryRun`) | completed; 6075 / 2121 identities |
| producer `pre-g9b-r6-plus-f4-d.desktop` | 53 tests, 0 failures, 0 skipped |
| producer `final.desktop` | 2115 tests, 0 failures, 1 skipped (the allow-listed disabled upstream case); 14.7 min; no heap failure |
| official inventory updater | applied; registry catalog pin reproduced and re-pinned |
| `Assert-GeoCeDGUpstreamBoundary` (baseline `9b93256b`) | green, 1062 registered files |
| Checkstyle (`checkstyleMain`, `checkstyleTest`) | no finding on the new or changed files; the only report entry is the pre-existing 101-character line of `PreG9BR6PlusA1HiddenLayerTest.java:188` (untouched) |
| compiled delta (§2) | `toName(char)` and the `LAST_POINT` branch of `move()` only |
| external evidence (§6) | as reported |
| development `INFRA_UNIT` on the staged tree | verification-0a59456c89f34117a0c67a489930895b ACCEPTED / COMPLETE (22 of 22); bound to HEAD `39687713` because the tree was uncommitted |
| development `STATIC` on the staged tree | verification-4b28c6bee5734a378b47668226cc3b3f ACCEPTED / COMPLETE; bound to HEAD `39687713` |
| `git diff --cached --check` | clean |

# PRE-G9B-R6-plus-C preparation characterization report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (C canonical prompt)
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PRE-G9B-R6-plus-C         = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the characterization encoded in the
canonical
[`C` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md)
(sections C1–C18). The prompt holds the resulting contracts and the requested
decisions `DQ-C1` to `DQ-C17`; this report does not restate them and carries no
authority of its own. The inputs fixed by the preparation instruction and the
pending questions are in the
[C author-decision record](pre_g9b_r6_plus_c_author_decisions_record.md). Its
machine-readable mirror is
[`pre-g9b-r6-plus-c-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-preparation-characterization.json).

## 1. Entry gate

```text
P_R6PLUS_A2          = e613502831e3b412780d69424d4a1e433a4ae688
                       tree 48b00681a727ea4bd8768c3e9b0159bc01573ff0
local main           = e613502831e3b412780d69424d4a1e433a4ae688 (tree 48b00681…)
origin/main          = e613502831e3b412780d69424d4a1e433a4ae688 (tree 48b00681…)
live remote main     = e613502831e3b412780d69424d4a1e433a4ae688
                       (git ls-remote origin refs/heads/main)
worktree             = clean (git status --porcelain=v1 --untracked-files=all: empty),
                       before any tracked edit
A-2 state            = PASS — AUTHOR APPROVED — PUBLISHED (AUTHOR_SMOKE = PASS)
preparation branch   = phase/pre-g9b-r6-plus-c-prompt, created from the exact commit
                       above (local; not pushed)
```

No other local branch was created, deleted or moved.

## 2. Method

- Static reading of every seam the prompt cites, at the base, with each
  citation re-checked line by line before it was written; five independent
  read-only sweeps (picture routes, LaTeX exporters, DXF pipeline, session
  lifecycle, semantic curve seams) were consolidated and every contract-bearing
  line was re-read directly.
- One scratch-only probe class, run on the unchanged base (§3). Its source and
  outputs live in the session scratchpad; nothing was written under `source/`.
  The probe directory was added to the Desktop test source set only through a
  Gradle init script that also redirected the JUnit XML report to the scratchpad;
  afterwards `:desktop:desktop:compileTestJava` was run without it (exit 0) and
  the one compile-transaction stash copy of the probe class was deleted. The
  worktree stayed clean apart from the documentary files of this preparation.
- One external reference re-read: the Autodesk DXF header-variable reference
  cited by ADR 0005, for the `$INSUNITS` codes (§4).
- No product, test, build, registry, schema, verifier, specification or ADR
  file changed. No `PHASE`, `INTEGRATION` or `FINAL` ran.

## 3. Probe

`gradlew.bat --init-script <scratch>\cprep-probe.gradle :desktop:desktop:test --tests 'org.geocedg.desktop.CPrepExportProbeTest'`:
exit 0, 4 tests, 0 failures, 0 errors, 0 skipped, on the embedded
`G9U1TestApp` host, view 400 × 300 px at 50 px per unit (`printingScale = 1`).

| Item | SHA-256 |
|---|---|
| probe source `CPrepExportProbeTest.java` | `f08fcb96f1852d60d5ef1f051b3faf297c5e006c12b645e41ba286c144365fcc` |
| init script `cprep-probe.gradle` | `5fab82889e6113093006d6383a12216eed6f8286ef8b0f3dacbb083e55e1c59d` |
| extracted output lines (`CPREP …`) | `cdcd03db15b5aaa24056ad8059662aec583663aa4d34328aa8bf30949c846c1a` |
| JUnit XML | `4a112de6aa2f9b55c8aecee34e2f45a391727a51af8d594e6889c9021069a86f` |

### `P-C-EMF` — EMF frame on the dialog route

Area 4 × 3 units (200 × 150 px), `p = 1`, the dialog formula
`s = p·D/(2.54·sx)` through `GraphicExportDialog.exportEMF`:

| DPI | `rclBounds` | `rclFrame` (0.01 mm) | frame (mm) | labelled size (mm) | ratio | `D/81.28` |
|---|---|---|---|---|---|---|
| 72 | 113 × 85 | 3531 × 2656 | 35.31 × 26.56 | 40 × 30 | 0.8828 | 0.8858 |
| 96 | 151 × 113 | 4718 × 3531 | 47.18 × 35.31 | 40 × 30 | 1.1795 | 1.1811 |
| 150 | 236 × 177 | 7375 × 5531 | 73.75 × 55.31 | 40 × 30 | 1.8438 | 1.8455 |
| 300 | 472 × 354 | 14750 × 11062 | 147.50 × 110.62 | 40 × 30 | 3.6875 | 3.6909 |
| 600 | 945 × 709 | 29531 × 22156 | 295.31 × 221.56 | 40 × 30 | 7.3828 | 7.3819 |

The EMF branch of the dialog hides the DPI combo but still applies its value
(default 300) to the device scale; the frame is the device extent on FreeHEP's
fixed 0.3125 mm reference, so its physical size is `W·p·D/81.28` cm.

### `P-C-EMF` — device-reference route

Device scale chosen so that one device unit is 0.3125 mm
(`s = outputMm·3.2/(W·sx)`), written by `PictureExportService.writeEMF`:

| Unit (m per unit) | Scale | Model W × H | Output (mm) | `rclBounds` | `rclFrame` | frame error (mm) | nearest-0.01 mm error (mm) |
|---|---|---|---|---|---|---|---|
| 0.001 | 1:1 | 4 × 3 | 4.0000 × 3.0000 | 13 × 10 | 406 × 312 | +0.0600 × +0.1200 | 0 × 0 |
| 0.01 | 1:1 | 4 × 3 | 40.0000 × 30.0000 | 128 × 96 | 4000 × 3000 | 0 × 0 | 0 × 0 |
| 0.01 | 1:2 | 4 × 3 | 20.0000 × 15.0000 | 64 × 48 | 2000 × 1500 | 0 × 0 | 0 × 0 |
| 0.01 | 2:1 | 4 × 3 | 80.0000 × 60.0000 | 256 × 192 | 8000 × 6000 | 0 × 0 | 0 × 0 |
| 0.01 | 1:1 | 4.003 × 3.0071 | 40.0300 × 30.0710 | 128 × 96 | 4000 × 3000 | −0.0300 × −0.0710 | 0 × −0.0010 |
| 0.0254 | 1:1 | 4 × 3 | 101.6000 × 76.2000 | 325 × 244 | 10156 × 7625 | −0.0400 × +0.0500 | 0 × 0 |
| 1 | 1:1 | 4 × 3 | 4000 × 3000 | 12800 × 9600 | 400000 × 300000 | 0 × 0 | 0 × 0 |
| 1 | 1:50 | 4 × 3 | 80.0000 × 60.0000 | 256 × 192 | 8000 × 6000 | 0 × 0 | 0 × 0 |
| 0.001 | 1:1 | 4.003 × 3.0071 | 4.0030 × 3.0071 | 13 × 10 | 406 × 312 | +0.0570 × +0.1129 | −0.0030 × +0.0029 |

The device grid (0.3125 mm) plus truncation dominates the error; the format's
own integer 0.01 mm quantization bounds the error by 0.005 mm with nearest
rounding.

### `P-C-PRINTING-SCALE` — physical size follows the zoom

The same 4-unit `MANUAL` area, PDF page width through
`PictureExportService.pdfPointsPerPixel`:

| px per unit | `printingScale` | PDF page width |
|---|---|---|
| 3 | 0.1 | 11.338583 pt = 0.4 cm |
| 20 | 1.0 | 113.385827 pt = 4 cm |
| 37.79527559 | 1.0 | 113.385827 pt = 4 cm |
| 50 | 1.0 | 113.385827 pt = 4 cm |
| 100 | 10.0 | 1133.858268 pt = 40 cm |
| 378 | 10.0 | 1133.858268 pt = 40 cm |
| 500 | 10.0 | 1133.858268 pt = 40 cm |
| 4000 | 100.0 | 11338.582677 pt = 400 cm |

### `P-C-LATEX-SEMANTIC` — silent omission

Scene: four hidden points, a visible segment; then `L = LocusV2(Q, s, D)` with a
two-interval domain and `S = SplineV2({A, B, C, E}, 3)`, both visible, their
inputs hidden. PGF/TikZ (566 characters), PSTricks (800) and Asymptote (1392)
are byte-identical before and after adding `L` and `S`; neither output mentions
a locus or a spline.

### `P-C-SAVE-PREVIEW` — the real Save chooser

`DialogManagerD.initFileChooser`, `MODE_GEOGEBRA_SAVE`, PNG context:

| Case | Preview image |
|---|---|
| selected target that does not exist (`new-export.png`) | none |
| selected target that exists (`old-export.png`) | rendered |
| chooser width 500 px | accessory removed |
| chooser width 800 px again | accessory restored |
| selection reset to `null`, the name field still holding the existing name | rendered (the `B` fallback to the name field) |

## 4. External reference

The Autodesk DXF header-variable reference (AutoCAD 2021, the page cited by
ADR 0005) defines `$INSUNITS` as "0 = Unitless, 1 = Inches, 2 = Feet, …,
4 = Millimeters, 5 = Centimeters, 6 = Meters, …". `$MEASUREMENT` (English or
Metric) and `$LUNITS` (display format) are separate variables outside the D0
contract. The MS-EMF specification page for the header record could not be
fetched (HTTP 404); the `rclFrame` statements of the prompt rest on the FreeHEP
source and the probe, not on that page.

## 5. Static characterization summary

The seam-by-seam results, with citations, are prompt C1–C18. The findings that
shape contracts beyond a direct reading of the governing records:

| # | Finding | Prompt |
|---|---|---|
| 1 | every physical picture and print size is `W·p` cm with `p` the zoom-derived power-of-ten `printingScale`; one GeoCeDG quantity `fb(effC)·100·a/b` reproduces the D0 formulas for PDF, PNG, SVG, print and LaTeX | C1, C4 |
| 2 | the EMF frame scales with a hidden DPI and FreeHEP's fixed 81.28 DPI reference, not only with a 0.01 mm quantization | C5 |
| 3 | no `drawingScale` holder exists; `AppGeoCeDG` is per window and reached by every exporter; the A-2 commit seam plus the `clearConstruction` override cover New, Open and every non-native replacement without touching undo paths | C2, C16 |
| 4 | clearing `setXML` includes same-document reloads (tool-creation save, macro edit window, portable preferences) | C2, `DQ-C7` |
| 5 | the LaTeX dialects never read `ExportArea`, `printingScale` or layers, write the selection rectangle on bound edits, and silently drop every Locus V2 and Spline V2 | C1, C9 |
| 6 | `App.isLayerShown` already gives shared code the `effectiveVisible` rule with a host default of `true` | C9, C18 |
| 7 | the Locus V2 export logic is private to the G9X1 adapter; the approximation builder is DXF-free and reusable | C10 |
| 8 | DXF writes `$INSUNITS = 0` and an always-on layer table; hidden layers are ignored; the fingerprint excludes units and hidden layers | C6, C8 |
| 9 | the two PowerShell DXF verifiers are historical phase verifiers, not registry leaves; the G9X1 one pins the live fidelity spec and a guide literal; its frozen evidence and the G5 corpus pin stay true for unspecified documents | C7, `DQ-C15` |
| 10 | `D1` pins every export as unit-independent on purpose; `B` and `A-1` pin the LaTeX and DXF interim state on purpose | C7 |
| 11 | `SplineV2` degree is 3 to 12; its cubic case is a numerically solved interpolant whose rounded spans are not coefficient authority | C13 |
| 12 | the Save preview renders only for an existing target in a chooser at least 600 px wide; both conditions are upstream code older than `B`; the Picture dialog has no preview | C15 |
| 13 | ADR 0032 states that it does not amend ADR 0005, whose Decision 3 fixes `$INSUNITS = 0` | `DQ-C17` |
| 14 | the GWT module compiles only `org/geogebra/common`, while 37 shared upstream classes already import `org.geocedg`: Web build compatibility is not established by any registered check | C18 |
| 15 | the DXF action's selection-contract reference `geocedg.dxf-exportable-visible-geometry` is referenced but not defined, and the behavior includes hidden objects | C8, `DQ-C3` |

Pre-existing upstream LaTeX defects observed and left outside `C`: the
parametric-plot warning without a newline (PGF and PSTricks), warning flags that
are never reset between generations (PGF), and the empty `getLineTemplate` of
PSTricks and Asymptote, which drops non-LaTeX parametric curves.

## 6. Specific observations

| Observation | Conclusion |
|---|---|
| `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` | wider than quantization: the dialog frame is `D/81.28` times the labelled size (3.69 at the default 300 DPI); device-reference scaling leaves errors up to about 0.32 mm (0.12 mm observed on a 3 mm extent); the format itself requires integer 0.01 mm and allows ≤ 0.005 mm with nearest rounding from the exact size; that route needs a FreeHEP EMF change; the policy is `DQ-C9` |
| `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` | a writer limitation of path coordinates (five significant digits, view-pixel units), distinct from page and frame sizing; `C` need not act on it; `DQ-C14` asks the author to confirm |
| `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` | proposed `NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`: the most likely route is Picture → Save with a new file name (or a narrow chooser), which shows an empty preview by the accepted `B` contract and upstream design; optional enhancement in `DQ-C10` |
| `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | not absorbed: `C` resets only its own `drawingScale` at existing commit points and changes no `ExportArea` or paste-notice reset; the observation stays open and owned by the author; the evidence suggests a later shared session-reset hook |

## 7. Re-characterized verification class

`INTEGRATED_PHASE` is confirmed: the planned `C` changes touch the shared
upstream LaTeX package and dialogs shared with Classic through host-identical
seams, the GeoCeDG export package, Desktop dialogs and the DXF sidecar, and
require concrete integration coverage across three LaTeX dialects, the picture
exporters and DXF. No document serialization, XML parser, undo snapshot,
kernel algorithm, command semantics (except the optional `ExportImage` route of
`DQ-C16`, which follows the `B` GGBScript-matrix precedent) or global verifier
architecture changes. The historical verifier pins of `DQ-C15` are data in a
phase verifier that no registry profile runs. A FreeHEP EMF opt-in (`DQ-C9`)
follows the `B` PDF precedent inside `INTEGRATED_PHASE`; because the `B` record
made any further FreeHEP family change an explicit author decision, the
disposition of `DQ-C9` also confirms or escalates the class.

At the base, `INTEGRATION` resolves to 46 registry nodes with 19 acceptance
leaves (`required_for_profiles` in `geocedg/specs/operations/verification-registry.json`),
including `junit.shared.final.semantic` and `junit.desktop.final.semantic`; the
registry holds 44 phase selections and none for `C`. The registered
`PRE-G9B-R6-PLUS-C` selection and its two JUnit selections are a `C`
implementation deliverable, on the `PRE-G9B-R6-PLUS-B` precedent.

## 8. Obligations and planned tests

The obligation-to-test map is the `T-*` table of the prompt's *Required tests
and commands* (37 identifiers) and its mandatory corpus, with the seventeen
requested decisions `DQ-C1` to `DQ-C17` and their default contracts in
*Decisions requested before authorization*.

## 9. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE (documentary preparation)
SERIALIZATION CHANGE               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT                   — NO CHANGE REQUIRED (no workstation, toolchain,
                                     Gradle, Conda, packaging, download or
                                     environment change)
GUIDE_IMPACT                       = NONE
GUIDE_JUSTIFICATION                = documentary preparation of a phase prompt; no
                                     observable behavior, command, workflow or
                                     file-format contract changes
```

## 10. Changed paths of the preparation

- `.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md` (new)
- `docs/validation/pre_g9b_r6_plus_c_author_decisions_record.md` (new)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-author-decisions.json` (new)
- `docs/validation/pre_g9b_r6_plus_c_preparation_characterization_report.md` (new, this report)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-preparation-characterization.json` (new)
- `docs/roadmap/geocedg_roadmap.md` (status lines only)
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` (status lines only)

The validation of the prompt contract and the `STATIC` run of the frozen
preparation candidate are reported outside this artifact, because it cannot
name its own commit.

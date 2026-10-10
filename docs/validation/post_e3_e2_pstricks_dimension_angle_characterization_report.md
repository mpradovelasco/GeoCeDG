# POST-E3-E2-PSTRICKS-DIMENSION-ANGLE — characterization report

```text
TASK                      = POST-E3-E2-PSTRICKS-DIMENSION-ANGLE
OBSERVATION               = OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE
AUTHORIZATION             = CHARACTERIZATION AND CORRECTIVE DESIGN AUTHORIZED —
                            IMPLEMENTATION NOT AUTHORIZED (author instructions of 2026-10-10)
BASELINE                  = 31286a2355cabebc69c3937442e7f15636da12ba
                            tree d223d7400b2eabacddafba874b49eae581c5c6c4 (published P_R6PLUS_E3)
VERIFICATION_CLASS        = DOCUMENTATION_STATUS_ONLY (frozen at entry, before any tracked edit)
PRODUCT_PHASE_EFFECT      = NONE
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this file
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved = false
```

Machine-readable mirror:
[`post-e3-e2-pstricks-dimension-angle-characterization.json`](../../geocedg/validation/post-e3/post-e3-e2-pstricks-dimension-angle-characterization.json).
Execution contract:
[canonical prompt](../../.github/prompts/tasks/post-e3-e2-pstricks-dimension-angle.prompt.md).
Prepared correction:
[corrective prompt](../../.github/prompts/tasks/post-e3-e2-pstricks-dimension-angle-correction.prompt.md)
(`PREPARED — NOT AUTHORIZED`).

## 1. Result in brief

| Question | Answer |
|---|---|
| Reproducible? | yes. Every non-round dimension value fails PSTricks compilation with `! Number too big.` |
| Generated expression | `\rput[b]{<θ>}(<x>,<y>){\raisebox{0.5ex}{<value>}}`, where `θ` is the shared `GeoGebraExport.format(double)` of the binary64 reading angle. It has up to 17 significant digits (`21.801409486351815`), or exponent notation for tiny angles (`1.1459156850751275E-9`) |
| TeX/PSTricks limit | PSTricks reads the fractional digits of the rotation as a TeX integer, at most 2147483647. Measured with real `latex`: up to **9** fractional digits compile; **10 or more** fail, whatever the value (`0.1234567891` fails too). Exponent notation fails |
| Fix by rounding? | yes. Six fractional digits compile and move the text direction by at most `5·10⁻⁷°` (measured worst case `4.86·10⁻⁷°`, under `10⁻⁶` mm over a 100 mm value) |
| PGF/TikZ, Asymptote | not affected. Every measured value compiles, including exponent notation in PGF/TikZ, and Asymptote gets the lowercase `e` form the exporter already writes |
| Pre-existing? | yes, since `E2`. The rotation text and the compile results are byte-identical at the pre-`E3` baseline `ff560991`; the formatter is upstream code unchanged since the import |
| Wider than `E2` | yes. The host PSTricks rotation of an ellipse (`\rput{18.434948822922014}`) fails the same way. The cause is the shared upstream formatter, and `E2` dimensions are one consumer |
| Correction | justified and minimal: a bounded six-digit serialization in the `E2` PSTricks dimension branch of the export adapter (`BOUNDED_PHASE`), prepared as a canonical prompt, not authorized |

## 2. Entry gate

```text
local main = origin/main = live remote main = 31286a2355cabebc69c3937442e7f15636da12ba
tree d223d7400b2eabacddafba874b49eae581c5c6c4; worktree clean
branch phase/post-e3-e2-pstricks-dimension-angle created from that commit
(independent of the other POST-E3 branches)
reproduction baseline: ff56099184544e5988c63e0ea3339e3d0fe01fac (detached scratch worktree)
```

## 3. Generation path (prompt items 1, 2)

```text
AlgoNativeDimension.getDimensionTextFrame()        model frame of the value
DimensionLatexExport.placement(owner, xu, yu)      φ = toDegrees(atan2(f3·yu, f2·xu))
DrawDimensionText.readableAngle(φ)                 θ = φ − 180·⌈(φ − 90)/180⌉ ∈ (−90°, 90°]
GeoGebraExport.format(θ)                           kernel.format(checkDecimalFraction(θ),
                                                   printFigures(PSTRICKS, 12)) → canonicalNumber2
GeoCeDGGeoGebraToPstricks.drawText (dimension)     \rput[b]{θ}(x,y){\raisebox{0.5ex}{value}}
GeoCeDGGeoGebraToPgf / ToAsymptote                 node[rotate=θ] / rotate(θ)*Label(...)
```

The formatter does not bound the digits for these values. The scratch probe
`PostE3PstAngleProbeTest` recorded the raw angle and the three dialects' text:

| Case | Raw reading angle | Written (all three dialects) |
|---|---|---|
| horizontal | `0.0` | `0.` |
| vertical | `90.0` | `90.` |
| oblique `(10,10)–(60,30)` | `21.80140948635181` | `21.80140948635181` |
| reversed endpoints | `21.801409486351815` | `21.801409486351815` |
| 45° / 135° | `45.0` / `-45.0` | `45.` / `-45.` |
| 30° (computed) | `29.999999999999996` | `30.` |
| near-horizontal (`Δy = 10⁻⁹`) | `1.1459156850751275E-9` | `1.1459156850751275E-9` (Asymptote `…e-9`) |
| near-vertical (`Δx = 10⁻⁹`) | `89.99999999885408` | `89.99999999885408` |
| very short (`Δ ≈ 10⁻⁴`) | `26.56505117707799` | `26.56505117707799` |
| Linear, vector `(1,2)` / `(1,0)` | `63.43494882292201` / `0.0` | same |
| host ellipse (control, no dimension) | — | PSTricks `\rput{18.434948822922014}` |

## 4. Numerical domain under TeX (prompt items 3, 4, 8)

A real `latex` compile of `\rput[b]{θ}(1,1){…}` for each value
(`post-e3-pst-domain.ps1`):

| θ | Fractional digits | Result |
|---|---|---|
| `21`, `21.8` … `21.801409486` | 0–9 | compiles |
| `21.8014094863` … `21.801409486351815` | 10–15 | `! Number too big.` (`<argument> 8014094863`) |
| `0.000000001` | 9 | compiles |
| `0.0000000011`, `0.1234567891`, `0.12345678912` | 10–11 | `! Number too big.` |
| `89.9999999989`, `-21.8014094864`, `359.9999999999` | 10 | `! Number too big.` |
| `1.1459156850751275E-9` | exponent | `! Number too big.` |
| `0.`, `45.`, `-45.` | — | compiles |

The TeX log reads "I can only go up to 2147483647 … so I'm using that number
instead of yours". Without `-halt-on-error`, a run therefore continues with a
clamped value, which can write a wrong angle silently.

Orientation error of rounding to six fractional digits (round half to even):

| Case | Written | `|Δθ|` | Displacement over a 100 mm value |
|---|---|---|---|
| `21.80140948635181` | `21.801409` | `4.86·10⁻⁷°` | `8.5·10⁻⁷` mm |
| `26.56505117707799` / `63.43494882292201` / `18.434948822922014` | `26.565051` / `63.434949` / `18.434949` | `1.77·10⁻⁷°` | `3.1·10⁻⁷` mm |
| `89.99999999885408` | `90` | `1.15·10⁻⁹°` | `2·10⁻⁹` mm |
| `1.1459156850751275E-9` | `0` | `1.15·10⁻⁹°` | `2·10⁻⁹` mm |

The bound is `|Δθ| ≤ 5·10⁻⁷°` for every angle. Six digits leave three digits of
margin to the TeX limit. Rounding from the binary64 value with a fixed rule and
no locale is deterministic.

## 5. Dimension families and regression (prompt items 5, 6)

All compiles use the exporter-generated documents (`post-e3-pst-generated.ps1`):

| Case | PSTricks (`latex`) | PGF/TikZ (`pdflatex`) |
|---|---|---|
| horizontal, vertical, 30°, 45°, 135°, Linear `(1,0)` | compiles | compiles |
| oblique, reversed, near-horizontal, near-vertical, very short, Linear `(1,2)` | `! Number too big.` | compiles |
| host ellipse | `! Number too big.` (`<argument> 4349488229`) | compiles |

Asymptote (label-free numeric check, because the installed `asy` cannot typeset
labels, as recorded in `E3-R1`): `rotate(21.80140948635181)`,
`rotate(1.1459156850751275e-9)` and `rotate(89.99999999885408)` produce a PDF.
Uppercase `E` would be misread by Asymptote, but the exporter writes lowercase
`e`. The rotation text and every compile result are byte-identical on
`ff560991`.

## 6. Specifications (prompt item 7)

The [native-dimensions specification](../../geocedg/specs/dimensions/native-dimensions.md)
1.1 (`NORMATIVE / AUTHOR APPROVED`) §7.3 defines the reading angle and §10.2
requires the value "written rotated by the reading angle". Neither fixes a
serialization precision, so a bounded precision keeps the approved semantics.
§10.2 also keeps every other segment and text on the host output. The host
ellipse defect is therefore outside the `E2` contract and is recorded separately.

## 7. Root cause (confirmed)

The shared upstream `GeoGebraExport.format(double)` writes the binary64 reading
angle with up to 17 significant digits or in exponent notation. PSTricks reads
the fractional digits of `\rput{…}` as a TeX integer, so ten or more digits
overflow 2147483647. The kernel angle is correct. The defect is purely the
serialization precision of one PSTricks argument.

## 8. Recommended minimal correction (not implemented)

| Option | Placement | Assessment |
|---|---|---|
| **C1 — E2 adapter (recommended)** | PSTricks dimension branch of `GeoCeDGGeoGebraToPstricks`: at most six fractional digits, round half to even, plain decimal, `-0` → `0`, locale-independent | fixes the `E2` observation. Only the PSTricks text of dimension values changes; PGF/TikZ, Asymptote and host outputs stay byte-identical. Within the authorized adapter placement |
| C2 — GeoCeDG PSTricks exporter, every rotation | override the rotation formatting for all PSTricks rotations | also fixes host rotations (ellipse) but changes host output bytes, against native-dimensions §10.2; separate decision |
| C3 — shared upstream formatter | `GeoGebraExport.format` | widest change (every dialect and number); upstream modification; not recommended |

Proposed verification class for C1: `BOUNDED_PHASE`. A Desktop export adapter,
one branch and one dialect, with no shared or kernel effect, so a registered
`PHASE` with the focused LaTeX classes suffices. The corrective prompt is
prepared as `PREPARED — NOT AUTHORIZED`.

## 9. New observation (recorded, not addressed)

```text
OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION = OPEN — PRE-EXISTING — UPSTREAM
  host PSTricks rotations (for example rotated ellipses, \rput{18.434948822922014})
  are written by the same shared formatter and fail with "Number too big";
  outside E2 and outside C1; needs its own author decision (C2 or C3)
```

## 10. Changed paths

```text
A  docs/validation/post_e3_e2_pstricks_dimension_angle_characterization_report.md
A  geocedg/validation/post-e3/post-e3-e2-pstricks-dimension-angle-characterization.json
A  .github/prompts/tasks/post-e3-e2-pstricks-dimension-angle-correction.prompt.md
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md (this activity's status line only)
```

No product, test, build, registry, inventory or serialization path changed.
Probes, generated documents and compile logs stay in the session scratchpad;
the JSON mirror records their SHA-256.

## 11. Verification

`STATIC` on the committed candidate and the prompt-contract parser on the
corrective prompt. They are reported outside this file, which cannot name its
own commit.

## 12. Final state

```text
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE = CHARACTERIZATION COMPLETE — PENDING AUTHOR REVIEW
OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE = CHARACTERIZED: pre-existing since E2; upstream
                                         formatter precision vs the PSTricks 9-digit limit;
                                         correction C1 designed (BOUNDED_PHASE), prompt
                                         PREPARED — NOT AUTHORIZED
OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION = NEW — RECORDED — NOT AUTHORIZED
PUBLICATION = NOT AUTHORIZED
selfApproved = false
```

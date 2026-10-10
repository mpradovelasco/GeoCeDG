# POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 — technical candidate report

```text
TASK                      = POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1
OBSERVATION               = OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE
AUTHORIZATION             = AUTHOR AUTHORIZED — BOUNDED IMPLEMENTATION AND TECHNICAL
                            VERIFICATION (2026-10-10; reconciliation record §11)
IMPLEMENTATION BASE       = 00b525fda64968efdf973504a6426e8565931fff
                            tree 23abd1ca06e30e07a9ed00a43522ea116180379d (published POST-E3 integration)
VERIFICATION_CLASS        = BOUNDED_PHASE (frozen at the entry gate, before the first productive edit)
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this file
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved = false
```

Execution contract:
[canonical C1 prompt](../../.github/prompts/tasks/post-e3-e2-pstricks-dimension-angle-correction.prompt.md).
Design and evidence:
[characterization report](post_e3_e2_pstricks_dimension_angle_characterization_report.md) and
[author reconciliation record](post_e3_followups_author_reconciliation_record.md) §4.
Machine-readable mirror:
[`post-e3-e2-pstricks-dimension-angle-c1-candidate-evidence.json`](../../geocedg/validation/post-e3/post-e3-e2-pstricks-dimension-angle-c1-candidate-evidence.json).

## 1. Entry gate and readiness

```text
local main = origin/main = live remote main = 00b525fda64968efdf973504a6426e8565931fff
tree 23abd1ca06e30e07a9ed00a43522ea116180379d; worktree clean
C1 not implemented at the base (GeoCeDGGeoGebraToPstricks wrote \rput[b]{format(angle)})
branch phase/post-e3-e2-pstricks-dimension-angle-c1 created from that commit
```

Readiness, completed before the first productive edit:

- **Code delta.** One argument in the native-dimension branch of
  `GeoCeDGGeoGebraToPstricks.drawText`, plus one package-private helper in
  `DimensionLatexExport`. The source angle stays `DimensionLatexExport.placement`.
- **Scope.** The change stays inside the export adapter. The shared formatter,
  kernel, `AlgoNativeDimension`, `DrawDimensionText.readableAngle`, placement,
  DAG and serialization are not touched.
- **Affected tests.** The `E2` desktop assertion accepts `\rput[b]{30}(` and
  `{30.}(`, so it still passes. No existing pin fixes the PSTricks text of a
  dimension; `PostE2P1R2BaseFingerprintTest` fixes PGF only. No pin changes.
- **Registration.** The smallest registration is a Desktop-only phase selection,
  modelled on `POST-E2-P4`.
- **Class.** `BOUNDED_PHASE` was frozen. The base regression digests were
  recorded on the unmodified product before the edit.

## 2. Implementation

| File | Responsibility |
|---|---|
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/DimensionLatexExport.java` | new `pstricksAngle(double)`. Rounds the exact binary64 reading angle half to even to six fractional digits (`BigDecimal`), so it is at most `5·10⁻⁷°` away, and writes it in plain notation without trailing zeros or negative zero, independent of the locale |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/GeoCeDGGeoGebraToPstricks.java` | the dimension branch writes `\rput[b]{pstricksAngle(θ)}`. A non-finite angle keeps the previous formatting (defined/undefined behaviour unchanged). Host rotations still use the shared formatter |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/PostE3C1PstricksDimensionAngleTest.java` | the focused tests of §3 |

Rounding uses the exact binary64 value, not its shortest decimal string, so the
bound holds also next to rounding transitions. For binary64 an exact tie at the
seventh decimal cannot occur, so half-even and half-up never differ.

## 3. Tests and evidence

| Requirement | Test | Result |
|---|---|---|
| A. Serialization | `everyDimensionFamilyIsWrittenWithBoundedPlainDecimals`, 12 cases covering Aligned and Linear dimensions: horizontal, vertical, oblique, reversed, 30°, 45°, 135°, near-horizontal, near-vertical, very short | written `0`, `90`, `21.801409`, `21.801409`, `30`, `45`, `-45`, `0`, `90`, `26.565051`, `63.434949`, `0`; plain decimals; PGF/TikZ and Asymptote keep the host text |
| A. Formatting rules | `formattingIsBoundedPlainLocaleIndependentAndDeterministic` | `-0.0` and `-1e-9` give `0`; `89.9999996` gives `90`; `-89.9999996` gives `-90`; `89.9999994` gives `89.999999`; de-DE, fr-FR, ar-EG and hi-IN give the same text; repeated calls are identical |
| B. Angular accuracy | the same test (domain sweep of 10 000 angles in `(−90°, 90°)` and 10 000 values within one ulp of a rounding transition) and the family test | maximum on the dimension cases `4.8635·10⁻⁷°` (oblique and reversed); maximum over the sweep `5.0·10⁻⁷°`, never above the bound |
| C. Real compilation | `previouslyRefusedDimensionsCompileWithLatex` | every case: `latex -interaction=nonstopmode -halt-on-error d.tex`, `dvips -q d.dvi -o d.ps` and `ps2pdf d.ps d.pdf` exit 0; DVI non-empty; PDF exists, starts `%PDF-` and is 2977–2987 bytes; `pdfinfo` reports its pages. The six cases refused before with "Number too big" now compile unedited |
| D. Byte identity | `everyOtherOutputIsByteIdenticalToTheBase` | for a mixed scene (two dimensions, segment, polyline, text, circle, rotated ellipse), the PGF/TikZ and Asymptote digests and the PSTricks digest with the dimension rotation normalized all equal the digests recorded on `00b525fd` before the edit |
| D. Host rotation debt | `hostRotationsKeepTheirInheritedFormatting` | the ellipse is still written `\rput{18.434948822922014}`: unchanged and not repaired (`OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` stays open) |
| D. Existing behaviour | `PreG9BR6PlusE2NativeDimensionDesktopTest` (14), `PreG9BR6PlusCLatexExportTest` (10), `PreG9BR6PlusCBaseIdentityTest` (3), `PreG9BR6PlusE3R1ExportInteroperabilityTest` (6), `PreG9BR6PlusE3IsoABorderDesktopTest` (13) | all pass: native-dimension geometry, LaTeX surfaces, base identity, the E3 ISO A export and the E3-R1 LaTeX and DXF behaviour are unchanged |

The PSTricks output of a non-physical export is a host `article` page. Its
compiled PDF has two pages, before and after C1 alike; physical exports use the
`E3-R1` `standalone` page.

Document serialization is unchanged because no reader, writer, kernel or
construction code is touched. Only the text of one PSTricks argument changes.

## 4. Registration, provenance and inventory

- Phase `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`: `compile.shared.semantic`,
  `compile.desktop.semantic`, `junit.desktop.post-e3-c1.semantic`.
- JUnit selection `post-e3-c1.desktop`: the six classes of §3.
- `docs/upstream/modified-files.yml`: purposes appended for the two exporter
  files; the new test registered.
- JUnit inventory regenerated by the in-session updater from discovery and
  executed evidence: `post-e3-c1.desktop` 51 identities; `discovery.desktop` 1991 → 1996 and `final.desktop` 1985 → 1990 (the five new tests); `discovery.shared` 6075 and `final.shared` 6992 unchanged; 56 selections, 61 phase selections; every producer `COMPLETED`, inner exit 0.
- Catalog and coverage pins updated.

## 5. Changed paths

```text
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/export/DimensionLatexExport.java
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/export/GeoCeDGGeoGebraToPstricks.java
A  source/desktop/desktop/src/test/java/org/geocedg/desktop/PostE3C1PstricksDimensionAngleTest.java
M  docs/upstream/modified-files.yml
M  geocedg/specs/operations/verification-registry.json
M  geocedg/specs/operations/verification-junit-inventory.json
M  tools/agent/tests/verification-final-coverage.Tests.ps1
M  .github/prompts/tasks/post-e3-e2-pstricks-dimension-angle-correction.prompt.md (authorization status)
M  docs/validation/post_e3_followups_author_reconciliation_record.md (§11 amendment)
A  docs/validation/post_e3_e2_pstricks_dimension_angle_c1_candidate_report.md
A  geocedg/validation/post-e3/post-e3-e2-pstricks-dimension-angle-c1-candidate-evidence.json
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md (status)
M  docs/roadmap/geocedg_roadmap.md (status)
```

## 6. Verification

The registered `PHASE POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1` runs on the frozen
candidate commit (`BOUNDED_PHASE`). Its result is reported outside this file,
which cannot name its own commit. Development checks before the commit: focused classes 51 tests, 0 failures; checkstyle (Desktop main and test) with no finding in a C1 file (one pre-existing warning in `PreG9BR6PlusA1HiddenLayerTest`); development `INFRA_UNIT` and `STATIC` `ACCEPTED` / `COMPLETE` (`STATIC` with the standing governance finding and historical-consistency `UNAVAILABLE`); `git diff --check` clean.

## 7. Focused author smoke

1. Draw an oblique `AlignedDimension` (for example from `(10,10)` to `(60,30)`) and
   a `LinearDimension` with an oblique direction.
2. Export PSTricks and compile the file without editing it
   (`latex`, `dvips`, `ps2pdf`). No "Number too big"; the values are rotated
   along their dimension lines as before.
3. Open the `.tex`: the rotation reads `\rput[b]{21.801409}`, with at most six
   decimals and no exponent.
4. Export PGF/TikZ and Asymptote of the same drawing. They are unchanged from
   before C1.
5. A rotated ellipse still exports with its long host rotation. That is the
   open host debt, outside C1.

## 8. Final state

```text
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE = CORRECTED IN THE LOCAL CANDIDATE (author review pending)
OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION = OPEN — UNCHANGED — OUTSIDE C1
PUBLICATION = NOT AUTHORIZED
F1 = NOT AUTHORIZED
selfApproved = false
```

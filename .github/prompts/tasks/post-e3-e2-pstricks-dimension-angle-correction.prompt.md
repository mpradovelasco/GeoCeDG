# POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 — bounded PSTricks precision of the dimension value rotation

**CANONICAL CORRECTIVE PROMPT — DESIGN AUTHOR APPROVED — AUTHORIZED FOR
IMPLEMENTATION AND TECHNICAL VERIFICATION ONLY.**

The author's instruction of 2026-10-10 ("POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 —
Bounded implementation") authorizes the implementation of C1 on the published
POST-E3 integration `00b525fda64968efdf973504a6426e8565931fff` (tree
`23abd1ca06e30e07a9ed00a43522ea116180379d`) with `BOUNDED_PHASE`. It is recorded in
the [reconciliation record](../../../docs/validation/post_e3_followups_author_reconciliation_record.md)
§11. The technical scope below is unchanged. Publication and every other PSTricks
correction stay unauthorized.

Prepared by `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE` (characterization and
corrective design only) from the evidence in the
[characterization report](../../../docs/validation/post_e3_e2_pstricks_dimension_angle_characterization_report.md).
The author decision of 2026-10-10 recorded in the
[POST-E3 author reconciliation record](../../../docs/validation/post_e3_followups_author_reconciliation_record.md)
approves the characterization and the corrective design C1 described here. It
approves `BOUNDED_PHASE` as the planned class and makes this prompt the
starting execution contract of the activity `POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`.
The file name is kept for its historical links. The implementation is **not**
authorized by that decision. It becomes executable only through a separate
explicit author instruction that names the exact published POST-E3 integration
commit as implementation base and confirms the readiness and verification
requirements. Where this prompt and the record differ, the record prevails.

```text
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 = AUTHORIZED FOR IMPLEMENTATION

observation              = OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE
selfApproved             = false
authorApproved           = false   (no implementation candidate is author approved)
designAuthorApproved     = true    (C1, 2026-10-10)
implementationAuthorized = true    (bounded C1 implementation and technical verification only)
passClaimed              = false
PHASE_KIND               = EXPORT ADAPTER SERIALIZATION CORRECTION — PSTRICKS DIMENSION VALUE ONLY
CHANGE_ROUTE             = ORDINARY
IMPLEMENTATION_BASE      = 00b525fda64968efdf973504a6426e8565931fff (tree 23abd1ca06e30e07a9ed00a43522ea116180379d)
VERIFICATION_CLASS       = BOUNDED_PHASE (frozen at the entry gate: the change stays in the PSTricks
                           dimension-text adapter and its tests; stop and request author
                           disposition if broader)
RETAINED DEBT            = OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION (outside C1; not repaired)
```

<!-- geocedg-field: objective -->
## Objective

Make the PSTricks export of a native dimension value compile under TeX, without
changing its orientation beyond a stated tolerance:

1. in the dimension branch of `GeoCeDGGeoGebraToPstricks.drawText`, write the
   rotation of `\rput[b]{…}` as a plain decimal with at most six fractional
   digits. Use round half to even of the binary64 reading angle from
   `DimensionLatexExport.placement`, strip trailing zeros, never use exponent
   notation, write `-0` as `0`, and keep the format independent of the locale.
2. Add focused tests: the rotation text for horizontal, vertical, oblique,
   reversed, near-horizontal (`≈1e-9°`), near-vertical and very short
   dimensions, Aligned and Linear; real `latex` compilation when the toolchain
   is available (`UNAVAILABLE` otherwise, never text inspection instead); the
   orientation tolerance `|Δθ| ≤ 5·10⁻⁷°`; and byte identity of every other
   PSTricks, PGF/TikZ and Asymptote output.
3. Provenance, the phase selection of the frozen class and the evidence.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact published POST-E3 integration commit and tree named in the author's
C1 authorization. The characterization ran on the published `P_R6PLUS_E3`
`31286a2355cabebc69c3937442e7f15636da12ba` (tree
`d223d7400b2eabacddafba874b49eae581c5c6c4`), which is not the C1 base. Record
the base at entry and stop on a mismatch, a dirty tree or an unpublished base.

## Authority and evidence hierarchy

`AGENTS.md`; the native-dimensions specification 1.1 §7.3 and §10.2 (the value
is written rotated by the reading angle; no precision is specified) and ADR
0034; the characterization report; then probe evidence.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/GeoCeDGGeoGebraToPstricks.java`:
  the dimension branch only, and a small formatting helper in
  `DimensionLatexExport` if the design needs one.
- Focused Desktop tests; pins of PSTricks outputs of scenes that contain a
  dimension, if any.
- `docs/upstream/modified-files.yml`, the phase registration and JUnit
  inventory of the frozen class, a candidate report and its evidence mirror,
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- The kernel, `AlgoNativeDimension`, `DrawDimensionText.readableAngle`, the
  dimension construction and its `Gap`, `Overshoot` and `Offset` semantics.
- The shared upstream `GeoGebraExport.format` and every host PSTricks rotation
  (for example rotated ellipses, `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION`):
  record only.
- PGF/TikZ and Asymptote outputs, which compile already. The `E3-R1` sheet label
  and physical page composition. The host `UnicodeTeX` mapping. Serialization.
- F1, F2, F3, F4 and G.

## Architectural placement

The defect is the serialization precision of one export argument. The
dimension's PSTricks placement is a GeoCeDG export-adapter responsibility
(native-dimensions §10.2), so the fix belongs in that adapter. The kernel
angle is correct.

## Required design/specification

No specification change: §10.2 does not fix a precision. A note in the
candidate report records the six-digit serialization as an implementation
decision.

## Geometric invariants and degeneracies

The written angle differs from the kernel reading angle by at most
`5·10⁻⁷°`. Near-degenerate dimensions keep their defined/undefined behaviour.
The reading-angle range `(−90°, 90°]` is unchanged, apart from rounding at its
ends.

## Compatibility and serialization

No document or file-format change. Only the PSTricks text of dimension values
changes; every other output stays byte-identical.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- The focused tests above and the classes they touch.
- `tools/agent/verify.ps1 -Profile PHASE -Phase <registered phase>` on the frozen
  candidate (`BOUNDED_PHASE`); `STATIC` for the documents. Report exact
  commands, exit codes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized on 2026-10-10 for the base above: the bounded correction, its tests,
the minimal phase registration, provenance, candidate documentation, local
commits and the registered `PHASE` verification. Not authorized: self-approval,
any other PSTricks correction (including `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION`),
the scripting-language API observation, and F1, F2, F3, F4 and G.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Even when authorized, a local candidate commit only; push, merge, fast-forward
of `main`, tag and release need a separate author instruction.

## Acceptance and closeout

The task stops with one frozen candidate pending author review; its artifacts
record `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`.

## Required artifacts

Candidate report with the before/after rotation text, compilation evidence,
orientation tolerance, byte-identity evidence and verification results;
evidence mirror; provenance.

## Stop conditions

- No authorization, base mismatch or dirty tree.
- The fix would need the shared formatter, the kernel or the drawable.
- Any non-dimension output changes.
- The PSTricks toolchain is unavailable for the compile evidence: report
  `UNAVAILABLE` and stop before claiming the correction verified.

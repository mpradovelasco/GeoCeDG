# POST-E3-E2-PSTRICKS-DIMENSION-ANGLE — PSTricks rotation of E2 dimension values

**CANONICAL EXECUTION PROMPT — PREPARED. CHARACTERIZATION AND CORRECTIVE
DESIGN AUTHORIZED; IMPLEMENTATION NOT AUTHORIZED.**

The author's instruction of 2026-10-10 (`PRE-G9B-R6-plus-E3-AUTHOR-CLOSEOUT`,
with its clarification of the same day) authorizes this activity with the
bounded scope below and orders that it run later as its own task and branch,
preferably after the publication of the `E3`/`E3-R1` closeout. The
authorization is recorded, versioned, in the
[E3 and E3-R1 closeout record](../../../docs/validation/pre_g9b_r6_plus_e3_closeout_record.md)
§8. Where this prompt and that record differ, the record prevails.

```text
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE =
PREPARED — CHARACTERIZATION AND CORRECTIVE DESIGN AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED

observation              = OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = CHARACTERIZATION AND CORRECTIVE DESIGN — NO PRODUCT CHANGE
CHANGE_ROUTE             = ORDINARY
VERIFICATION_CLASS       = DOCUMENTATION_STATUS_ONLY (proposed; freeze at task entry)
ORDER                    = 2 of the three POST-E3 activities
```

The observation as recorded by the `E3-R1` candidate report §4.5: the `E2`
dimension value is written to PSTricks with a 14-decimal rotation (for example
`\rput[b]{21.80140948635181}…`) that `latex` refuses with "Number too big";
reproduced on the original `E3` candidate `257c854a`; not attributed to
`E3-R1` and not changed by it. The emitting code is the dimension branch of
`GeoCeDGGeoGebraToPstricks.drawText` (`\rput[b]{format(place[2])}`), with the
placement from `DimensionLatexExport.placement`.

<!-- geocedg-field: objective -->
## Objective

Characterize the defect and design — without implementing — the minimal
correction. Establish at least:

1. the exact generated rotation-angle expression and its formatting path;
2. its source in the `E2` LaTeX export adapter (`DimensionLatexExport`,
   `GeoCeDGGeoGebraToPstricks`);
3. the numerical domain the PSTricks rotation argument accepts under TeX
   (dimension and integer limits, decimal digits), with compiled evidence;
4. whether ordinary rounding to a bounded number of decimals resolves the TeX
   limitation without a visible orientation error, with a measured bound;
5. horizontal, vertical, oblique and near-degenerate dimensions (angles near
   0°, ±90°, 180°, and lengths near the degeneracy threshold);
6. PGF/TikZ and Asymptote behaviour for the same dimensions, as regression
   evidence;
7. the governing `E2` and POST-`E2` approved specifications
   (`geocedg/specs/dimensions/native-dimensions.md` 1.1 and its amendments);
8. determinism and numerical tolerances of the proposed output.

Deliver a characterization report, the recommended minimal correction, its
verification class and a canonical corrective prompt.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the author when starting the task: the
published `E3`/`E3-R1` closeout on `main` (preferred) or, failing that, the
local closeout commit; record it at entry and stop on a mismatch. Reproduction
baselines: the published pre-`E3` baseline
`ff56099184544e5988c63e0ea3339e3d0fe01fac` (tree
`1cdd1e986df785f5f16e7887300d9034e5e4a677`) and the approved `E3-R1` candidate
`7cd501167c856eef356888ca87ed3a0c97cea55c` (tree
`3c51c9d3635c22939df6cab60259521f885eff92`), probed through `git archive` or a
detached worktree in the session scratchpad.

## Authority and evidence hierarchy

`AGENTS.md`; the current code and build; the native-dimensions specification
1.1 and ADR 0034; the POST-`E2` closeout records; the `E3-R1` candidate report
§4.5; then probe evidence.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Scratch probes and TeX compilations in the session scratchpad (MiKTeX
  `latex`/`dvips`/`ps2pdf`, `pdflatex`, `asy` as installed; report any missing
  tool as `UNAVAILABLE`, never substitute text inspection for compilation).
- New documentation: `docs/validation/post_e3_e2_pstricks_dimension_angle_characterization_report.md`,
  its JSON mirror under `geocedg/validation/post-e3/`, and one corrective prompt
  under `.github/prompts/tasks/` marked `PREPARED — NOT AUTHORIZED`.
- Status lines in the roadmap and in the mini-track plan.

`PRODUCT_PHASE_EFFECT = NONE`.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any product or test change, including the export adapters.
- Fixing the defect by changing the kernel's geometric orientation, the
  dimension construction, `Gap`/`Overshoot`/`Offset` semantics or the drawable.
- The host `UnicodeTeX` mapping, the `E3-R1` sheet label and physical page
  composition, and the Asymptote toolchain limitation of `E3-R1`.
- The other POST-E3 activities; F1, F2, F3, F4 and G.

## Architectural placement

The expected correction, if justified, belongs in the export adapter (the
PSTricks dimension text placement), not in the kernel. The characterization
must confirm or refute this placement with evidence.

## Required design/specification

The native-dimensions specification governs the dimension text orientation;
the correction may change only its LaTeX serialization precision. If an
amendment is needed, propose it; do not apply it.

## Geometric invariants and degeneracies

The on-screen and exported orientation of the dimension text must stay within
a stated, measured tolerance of the kernel value; near-degenerate dimensions
must keep their current defined/undefined behaviour.

## Compatibility and serialization

No document or serialization change. Non-dimension PSTricks output and the
PGF/TikZ and Asymptote outputs must stay byte-identical unless the design
justifies otherwise.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- Compiled probes on both baselines for the dimension families of the
  objective, with exact commands, exit codes and page or raster evidence.
- `tools/agent/verify.ps1 -Profile STATIC` on the committed documentation.
- The corrective prompt validated with `Test-PromptContractDocument`, profile
  `task`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: characterization, corrective design, the recommended verification
class and the canonical corrective prompt. Not authorized: implementation.
Approval of the characterization never implies approval of the correction.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local commit of the documentation is authorized. Push, merge, fast-forward of
`main`, tag and release are not.

## Acceptance and closeout

The task stops with a characterization pending author review;
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` in its artifacts.

## Required artifacts

The characterization report, its JSON mirror, the corrective prompt, the
compiled evidence and the final status block.

## Stop conditions

- Base identity differs from the author's instruction, or the tree is dirty at
  entry.
- The evidence places the cause in the kernel or the drawable.
- No precision bound satisfies both TeX and the orientation tolerance.
- The required TeX toolchain is unavailable for the PSTricks route.

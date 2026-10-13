# PRE-G9B-R6-plus-F4 — final author reconciliation amendment

```text
TASK                 = F4 FINAL AUTHOR RECONCILIATION AMENDMENT (author instruction of
                       2026-10-13; documentation and review only)
BASE                 = 3251f08595fee8cd5819cc4c607e1dd97c6d0ad2 (F4 author-decision
                       reconciliation candidate)
F4 DESIGN            = PENDING FINAL AUTHOR APPROVAL
F4-A IMPLEMENTATION  = NOT AUTHORIZED
F4-D IMPLEMENTATION  = NOT AUTHORIZED
PUBLICATION          = NOT AUTHORIZED
selfApproved         = false
```

## 1. Author dispositions recorded

- **DQ-F4-2 — approved direction:** F4-A covers Graphics 1 and Graphics 2 with
  architecture A1 (GeoCeDG-owned, no inherited source modification); the real-window
  keyboard-focus gate stays a blocking prerequisite.
- **DQ-F4-10 — approved direction:** sequential tracks — (1) F4-D implementation,
  verification, author smoke, closeout and publication; (2) F4-A likewise; (3) a final
  documentary aggregate F4 closeout; each implementation with its own explicit
  authorization and exact base commit.
- **DQ-F4-7 — conditional disposition:** the source argument for `INTEGRATED_PHASE` is
  accepted as technically plausible, to be reconciled with the active recovery contract
  and the executable registry before freezing.

## 2. Verification-authority reconciliation (DQ-F4-7)

**Discrepancy confirmed.** The reconciliation `3251f085` based its class assessment on
`verification-levels.md` §12.8. That section belongs to the "Archived pre-recovery
contract", which "is not parsed as live configuration and must not be used instead of
the current registry and schemas"; the document's status is **ACTIVE RECOVERY CONTRACT**
and its executable authority is `tools/agent/verify.ps1`, `verification-registry.json`
and their schemas.

**Executable evidence (registry on the base).** The registry has no class field; a class
maps to profiles. `INTEGRATION` requires 46 nodes, including `junit.desktop.final` (the
`final.desktop` selection: every Desktop test except six G9U1 methods run by isolated
nodes; GeoCeDG and Classic PDF tests included), `junit.shared.final`, compile, Python,
packaging and repository safety. `FINAL` requires 68 nodes = the 46 of `INTEGRATION`
plus exactly the 22 `infra.*` leaves of `INFRA_UNIT`. `FINAL` therefore adds no product or
export node; its extra nodes cover verification infrastructure, which F4-D touches only
through the ordinary registration of its phase selection and inventory (watched by
`infra.final-coverage`), covered by running `INFRA_UNIT` (F2 and F3 precedent).

**Coverage of the F4-D obligations.** GeoCeDG PDF routes (physical and legacy) and the
Classic dialog route, font embedding (font dictionary and `FontFile`), text extraction,
the text-as-shapes and SVG identity checks and the existing PDF/export test classes are
in-JVM JUnit tests inside the F4-D phase selection and `final.desktop`, hence in `PHASE`
and `INTEGRATION`. The raster comparison and reader compatibility need software outside
the workstation contract; existing external-tool tests report `UNAVAILABLE` and pass when
the tool is missing, so **no profile, `FINAL` included**, enforces them: they are focused
evidence in the candidate report plus the author smoke.

**Result.** `INTEGRATED_PHASE` (registered `PHASE` + `INTEGRATION`, with `INFRA_UNIT` and
`STATIC` for the registration, external-renderer evidence and author smoke) is supported
by the current executable authority for the complete demonstrated perimeter. No
escalation is proposed: `GLOBAL_IMPACT` would add no product coverage.

## 3. Review findings and amendments

| Check | Finding | Amendment |
|---|---|---|
| tracks independent | yes; each prompt states that approval of the other track authorizes nothing | prompts record the approved order (F4-D first; F4-A on the published F4-D closeout) |
| F4-D minimal | yes: one delegation line; everything else forbidden | class text replaced by the executable-authority basis; `INFRA_UNIT` added for registration |
| F4-A without inherited code | yes: A1, inherited files in the forbidden scope | Graphics 2 made definite; `INFRA_UNIT` added for registration |
| byte-identity claims | two unsupported forms found: (a) "every currently successful output … byte-identical by construction" stated as a guarantee; (b) raw byte identity of PDFs and SVGs, which carry volatile date strings and clip identifiers | reworded as source evidence for the inspected paths (no frame on the path catches `Error`) plus a test obligation: identity of the declared outputs after the normalization the existing fingerprint tests apply; no wider claim |
| implied authorization | none found | — |
| `selfApproved` | `false` everywhere | — |

Amended documents: the [author-decision reconciliation](pre_g9b_r6_plus_f4_author_decision_reconciliation.md)
(status, §3, §4, `DQ-F4-2`, `DQ-F4-7`, `DQ-F4-10`, §6), its mirror, the
[two-track design successor](../architecture/pre_g9b_r6_plus_f4_two_track_design_successor_candidate.md),
the [F4-A](../../.github/prompts/tasks/pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md)
and [F4-D](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md)
prompts, the roadmap and the mini-track plan. The F4-PREP package and the superseded
combined prompt are unchanged.

## 4. Readiness

All ten decisions (`DQ-F4-1` to `DQ-F4-10`) are ready for final author approval; no
decision needs further investigation. The F4-D class is ratified at its implementation
authorization. No blocking issue remains for the design approval; the F4-A focus gate is
an implementation-time prerequisite, not a design blocker.

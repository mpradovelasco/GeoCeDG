# PRE-G9B-R6-plus-F4 — author design closeout record

```text
TASK                 = F4 FINAL AUTHOR DESIGN APPROVAL (author instruction of 2026-10-13;
                       documentary design closeout only)
CLASS                = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
APPROVED CANDIDATE   = 83f07931afb3a1c7fbbdba268d7da5fb2d8b3cf9
                       tree d4e6989c585e6839d22eaffc84df783c09a0c065
                       parent 3251f08595fee8cd5819cc4c607e1dd97c6d0ad2
LINEAGE              = 847bb20a (published main) -> 089fc220 (F4-PREP) -> 3251f085
                       (author-decision reconciliation) -> 83f07931 (final author
                       reconciliation amendment) -> this design closeout
F4 AUTHOR DECISIONS  = DQ-F4-1 TO DQ-F4-10 — AUTHOR APPROVED (as amended in 83f07931)
F4 DESIGN            = AUTHOR APPROVED — DOCUMENTARY DESIGN CLOSEOUT
F4-D IMPLEMENTATION  = NOT AUTHORIZED (next track; separate authorization required)
F4-A IMPLEMENTATION  = NOT AUTHORIZED (after F4-D publication; separate authorization)
PUBLICATION          = NOT AUTHORIZED
G                    = NOT AUTHORIZED
PRODUCT CHANGE       = NONE
selfApproved = false; implementationAuthorized = false; publicationAuthorized = false
```

The approval is the author's, given explicitly in the instruction of 2026-10-13; this
record transcribes it and adds no decision. Technical verification of this documentary
candidate (`STATIC`) never implies author approval of anything beyond what the author
stated.

## 1. Author approval (recorded as given)

The author approves the F4 architectural reconciliation and the author decisions
`DQ-F4-1` to `DQ-F4-10`, as amended in the local documentary candidate
`83f07931afb3a1c7fbbdba268d7da5fb2d8b3cf9` (tree
`d4e6989c585e6839d22eaffc84df783c09a0c065`), with these dispositions:

| # | Track / item | Approved disposition | Decisions |
|---|---|---|---|
| 1 | F4-A | Architecture A1, GeoCeDG-owned, covering Graphics 1 and Graphics 2; no inherited modifications; the real-window keyboard-focus feasibility gate remains mandatory and blocking | `DQ-F4-1`, `-2`, `-3`, `-4` |
| 2 | F4-D | Minimal one-line FreeHEP correction affecting GeoCeDG and Classic; `INTEGRATED_PHASE` accepted on the current executable verification registry, not on the archived §12.8 | `DQ-F4-1`, `-5`, `-7`, `-9` |
| 3 | F4-D verification | `PHASE` + `INTEGRATION` + `INFRA_UNIT` + `STATIC`, with the required focused external evidence; PDF glyph-rendering and reader-compatibility checks reported explicitly and reviewed before author acceptance; missing evidence is never silently treated as successful validation | `DQ-F4-7` |
| 4 | F4-B | Closed as resolved for AutoCAD 2026 only | `DQ-F4-6` |
| 5 | F4-C | Deferred as `TD-PDF-1`, pending a new physical text presentation contract | `DQ-F4-8` |
| 6 | Sequencing | F4-D first; F4-A after F4-D publication; then an aggregate documentary F4 closeout; each track keeps independent implementation authorization, verification, author smoke and closeout | `DQ-F4-10` |
| 7 | Output regression | Normalized output fingerprints compared with narrowly defined volatile-field exclusions; no exclusion may mask a geometric, semantic or rendering difference | `DQ-F4-5`, `-7` |

The approval authorizes only this documentary design closeout. It does not authorize
product implementation, publication or phase G.

## 2. Effect on the decision documents

| Document | Change |
|---|---|
| [author-decision reconciliation](pre_g9b_r6_plus_f4_author_decision_reconciliation.md) | status: decisions and design `AUTHOR APPROVED`; content of `DQ-F4-1` to `DQ-F4-10` unchanged |
| [final author reconciliation amendment](pre_g9b_r6_plus_f4_final_author_reconciliation_amendment.md) | unchanged (approved content) |
| [two-track design successor](../architecture/pre_g9b_r6_plus_f4_two_track_design_successor_candidate.md) | status `AUTHOR APPROVED — DESIGN`; implementation not authorized |
| [F4-D prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md) | `DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED`; approved items 3 and 7 written into the compatibility, checks, artifacts and stop conditions |
| [F4-A prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md) | `DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED`; base remains the published F4-D closeout |
| mirror `pre-g9b-r6-plus-f4-author-design-closeout.json` | new evidence mirror of this record |
| mirror `pre-g9b-r6-plus-f4-author-decision-reconciliation.json` | states updated; pointer to this record |
| roadmap 5.39 and mini-track plan | status recorded |

The F4-PREP package and the superseded combined prompt stay unchanged as preparation
evidence. No specification, ADR, product, test or verification-infrastructure file
changes.

## 3. Track boundaries preserved

- **F4-D** and **F4-A** remain independent tracks. Approval of the design authorizes
  neither implementation; approval or acceptance of one track never authorizes the
  other.
- Each track needs its own author instruction naming the exact base commit and tree and
  freezing its class; each has its own verification, author smoke, closeout and
  publication.
- The F4-A real-window keyboard-focus gate stays a blocking implementation-time
  prerequisite (`STOP — F4-A FOCUS FEASIBILITY BLOCKED` otherwise).
- The aggregate F4 closeout is documentary and follows both track closeouts.

## 4. F4-D implementation base and canonical prompt

- **Canonical prompt:**
  [`.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md).
- **Implementation base (approved rule):** the published `main` current at the F4-D
  implementation authorization, named there by exact commit and tree. At this record the
  published `main` is `847bb20a593e8092eabf80bd9d7c62ed7c21de08`; the F4 documentary
  candidates (`089fc220`, `3251f085`, `83f07931` and this closeout) are local and
  unpublished. The canonical prompt the F4-D implementation follows is the version in
  this design closeout, so the F4-D authorization must name either a published `main`
  that contains it or this closeout commit as its exact base; the author chooses at the
  authorization.

## 5. Verification of this candidate

`STATIC` on the documentary approval candidate; links, JSON parsing, encoding, the task
prompt contract of both F4 prompts and `git diff --check` checked locally. The commit,
tree, parent and receipt are reported to the author with the candidate and are not
self-referenced here.

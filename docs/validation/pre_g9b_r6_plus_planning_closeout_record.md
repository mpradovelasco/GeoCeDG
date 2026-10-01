# PRE-G9B-R6-plus planning closeout record

```text
PRE-G9B-R6-plus PLANNING = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true    (the mini-track plan only)
passClaimed               = true    (the mini-track plan only)
implementationAuthorized  = false   (P0 and every subphase)

planningCandidateFrozen   = true
planningCandidateMutated  = false
productPhaseEffect        = NONE
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-10-01 on the
`PRE-G9B-R6-plus` mini-track planning and the exact identities behind it. It
changes no product, test, build, packaging, verifier, registry, schema,
inventory, guide, specification or ADR file, and it does not modify the
planning candidate or its reconciliation. Besides this record and its
machine-readable mirror,
[`pre-g9b-r6-plus-planning-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-planning-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md), and
reconciles one stale sentence in the proposed future `PRE-G9B-R7` prompt, as the
author authorized.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [planning candidate report](pre_g9b_r6_plus_planning_candidate_report.md)
and its mirror
[`pre-g9b-r6-plus-planning.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-planning.json)
record only frozen planning state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. Their documentary
reconciliation in `D_R6PLUS_PLAN` keeps that rule. This record is the sole
authority for the current author approval of the planning.

## Author disposition

```text
PRE-G9B-R6-plus PLANNING = PASS — AUTHOR APPROVED
T_R6PLUS_PLAN            = 147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f
D_R6PLUS_PLAN            = af2aa1133620e49d68ac469ee8d10d273498cd95
AUTHOR_APPROVAL          = APPROVED
selfApproved             = false
```

The author instruction of 2026-10-01 authorized one strictly planning and
documentary reconciliation of the frozen planning candidate and, subject to
that reconciliation completing successfully, recorded the author's approval of
the reconciled mini-track plan. The reconciliation completed with the checks
below. The approval concerns the mini-track plan. It does not authorize `P0`,
any productive subphase or any product implementation, and it is not the
closeout of the `PRE-G9B-R6-plus` track itself.

## Approved identities

```text
P_R6            = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
                  tree f5c8c9f0cc76982170c6282a1242d2a5d07a0897
                  PRE-G9B-R6 = PASS — AUTHOR APPROVED — PUBLISHED

T_R6PLUS_PLAN   = 147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f
                  tree 4122e5626a26d5e039e97d33def6ea9b1993c8b8
                  parent 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
                  planning candidate; not amended

D_R6PLUS_PLAN   = af2aa1133620e49d68ac469ee8d10d273498cd95
                  tree b6bd75cd0f1eea5dbb2910b4a2a962ee8f239620
                  parent 147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f
                  documentary reconciliation; product delta NONE

branch          = feature/pre-g9b-r6-plus-planning (local; never pushed)
```

The identity of this closeout commit, `P_R6PLUS_PLAN`, is its own commit, which
the record cannot name. It is reported with the promotion.

## Documentary reconciliation `D_R6PLUS_PLAN`

`D_R6PLUS_PLAN` changes the plan, the planning report (new §14; corrected §3,
§4.4, §5 and §8), the planning mirror and the `P0` prompt. It records:

1. **Two author-supplied requirements, not missing inputs.**
   - Export: for PNG, PDF, SVG and EMF(+) with an `Export_1`/`Export_2` area,
     export exactly the defined rectangle, without the small outside border
     currently observed, and export the complete rectangle independently of
     the visible viewport, where current export may clip to visible content.
     `P0` reproduces and characterizes both observations against source and
     runtime; `B` implements them.
   - Sheet tools: in their future authorized phase, `sheetISOland` and
     `sheetISOvert` hide the outer dotted rectangle and all corner points.
2. **`AQ-U1 = RESOLVED — AUTHOR CONFIRMED`.** `constructionUnit` is the physical
   interpretation of one model unit; `presentationUnit` is the unit used to
   express dimensional presentation and physical export quantities;
   `drawingScale` is a dimensionless engineering ratio. Physical output size is
   invariant under an equivalent presentation-unit change (10 cm at 1:2 = 5 cm
   = 50 mm on output; 100 mm at 1:2 = 50 mm). `presentationUnit` is never a
   second geometric scale factor.
3. **Author layer targets.** The working layer is not restricted by the current
   0..9 UX and domain. `P0` designs the least invasive correct extension and
   determines its compatibility and verification impact; `A` may escalate
   beyond `INTEGRATED_PHASE`. The working-layer toolbar entry is a GeoCeDG
   mode compatible with the Move group. The status bar is complementary and
   may gain interaction; it does not replace that tool.
4. **GGT design work.** `AQ-G1` (repository structure reconciling the requested
   `tools/ggtfiles/` with the `models/legacy/` provenance conventions) stays
   open for `P0`, never chosen by convenience. Packaging support for the
   approved library is a required design target, and the current `.ggt`
   exclusion is a contract `E1` must reconcile. A provenance and licensing gate
   separates macro behavior, macro provenance, GeoCeDG-owned replacement icons
   and redistributable assets; geometric equivalence with `template-v7` grants
   no redistribution permission, and no agent makes a legal conclusion.
5. **Orientation authority.** `P0` establishes the semantic orientation
   authority for each supported type before `F1`; no accessor is chosen by its
   name.
6. **Observations stay observations.** `P0` may classify them and recommend
   owners; none becomes product scope.

```text
PRODUCT_PHASE_EFFECT            = NONE
TECHNICAL_VERIFICATION_CLAIM    = NONE (documentary reconciliation)
PREVIOUS_EVIDENCE_REINTERPRETED = false
```

## Accepted evidence

Every run used `tools/agent/verify.ps1 -Profile STATIC -LogDirectory <new
external root>` on a clean worktree and bound the exact commit and tree. No
`PHASE`, `INTEGRATION` or `FINAL` run belongs to this planning, as its frozen
`DOCUMENTATION_STATUS_ONLY` class requires.

| Commit | Run | Result |
|---|---|---|
| `T_R6PLUS_PLAN` `147dac8d…` | `verification-9a3a79ecf3314e6fa58b3ad6fdff4dad` | exit 0; `ACCEPTED / COMPLETE`; 3/3 (`prompt.execution-safety`, `repository.boundary-safety`, `static.scientific-inputs.semantic`) |
| `D_R6PLUS_PLAN` `af2aa113…` | `verification-a9163ec657c14298beb729dbad266eec` | exit 0; `ACCEPTED / COMPLETE`; 3/3; plan hash `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result hash `57a1a48acef9e8e5a982a02917da1962dc58ad2ae1361037e0ea940135cbd0d5` |

Both runs reported the two standing diagnostics, `diagnostic.governance`
(`DIAGNOSTIC_FINDING`) and `diagnostic.historical-consistency`
(`DIAGNOSTIC_UNAVAILABLE`), which appear on every `STATIC` run and do not
affect acceptance. The other four diagnostics were clear. Each run keeps the
identity it was bound to; none is reattributed. `STATIC` is not `FINAL`, so no
receipt is expected and none is claimed. This closeout commit's own `STATIC`
run and `git diff --check` are reported with the promotion, because the record
cannot name its own commit.

Prompt contracts, checked with `Test-PromptContractDocument` and the `task`
profile of `geocedg/specs/operations/prompt-contracts.json`:

- the `P0` prompt at `D_R6PLUS_PLAN`, blob
  `2c51ca73e6a1ccbd04d41885f8d4f86b69dd1578`: all seven fields present and
  ordered, one title, `execution_safe = true`;
- the amended `PRE-G9B-R7` prompt below: the same result.

## `PRE-G9B-R7` prompt reconciliation

The author authorized minimally reconciling the stale sentence in the proposed
future `PRE-G9B-R7` prompt
([`pre-g9b-r7-readiness-closeout.prompt.md`](../../.github/prompts/tasks/pre-g9b-r7-readiness-closeout.prompt.md)):

- blob `0e423c5dda507e3c1f3a6e5ddec97c634ad09633` →
  `be941a71454b85abf21a55f8f141b88b44a12281`;
- "its scope is pending author definition" becomes a statement that the author
  defined the scope and approved the mini-track plan on 2026-10-01, planning
  only, with no subphase authorized.

The dependency `PRE-G9B-R7` on `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED` is
unchanged. The prompt stays `PROPOSED FUTURE`, unexecuted and not authorized, and
is otherwise unchanged. `R7` is not redesigned.

## Roadmap and plan status

- The roadmap moves to version 4.34. It records `PRE-G9B-R6-plus` as
  `PLANNING PASS — AUTHOR APPROVED / PRODUCT IMPLEMENTATION NOT AUTHORIZED`
  in its header, its track status and its sequence table, prepends the planning
  closeout to the closure chain, and notes that `P0`'s base will be the
  published planning closeout once the author names it.
- The plan's header and §14 record the author-approved planning design.
- `PRE-G9B-R6-plus-P0` stays `DESIGNED / CANONICAL PROMPT CREATED / NOT
  AUTHORIZED`, and every later subphase stays `NOT AUTHORIZED`.

## Promotion

The author authorizes publication of the exact linear range

```text
P_R6           9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
  -> T_R6PLUS_PLAN 147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f
  -> D_R6PLUS_PLAN af2aa1133620e49d68ac469ee8d10d273498cd95
  -> P_R6PLUS_PLAN (this closeout commit)
```

to `main`, by clean non-force fast-forward only.

- **Precondition:** immediately before the push, local `main`, `origin/main`
  and the live remote `main` must all equal `P_R6`.
- **Forbidden:** merge commits, rebases, squashes, amends, force pushes, tags,
  releases, binary publication and any product implementation.

Remote identity at closeout entry:

```text
local main       = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
origin/main      = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
live remote main = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
tree             = f5c8c9f0cc76982170c6282a1242d2a5d07a0897
```

The identity after promotion is this record's own commit; it is reported with
the promotion, together with the post-promotion audit.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
PRE-G9B-R6                 = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus            = PLANNING PASS — AUTHOR APPROVED
                             PRODUCT IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-R6-plus PLANNING   = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-P0         = DESIGNED / CANONICAL PROMPT CREATED / NOT AUTHORIZED
PRE-G9B-R6-plus-A … G      = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

- **`PRE-G9B-R6-plus-P0`** needs a separate explicit author instruction naming
  the exact published planning closeout as its implementation base.
- **`PRE-G9B-R7`** still requires the `PRE-G9B-R6-plus` track to be `PASS —
  AUTHOR APPROVED`; the planning approval does not satisfy that.
- **`G9B`** does not follow `PRE-G9B-R7` automatically.
- **No tag or release is authorized.**

# PRE-G9B-R6-plus-C-X1 closeout record

```text
PRE-G9B-R6-plus-C-X1 = PASS — AUTHOR APPROVED
AUTHOR_SMOKE         = PASS
OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT = RESOLVED IN PRE-G9B-R6-plus-C-X1 — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true    (C-X1 only, technical candidate T_R6PLUS_C_X1)
passClaimed               = true    (C-X1 only)
implementationAuthorized  = false   (E1, E2, E3, F1, F2, F3 implementation, G,
                                     PRE-G9B-R7, G9B and every open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = YES (Desktop lifecycle/UI only)
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout)
```

This record preserves the explicit author disposition of 2026-10-06 on
`PRE-G9B-R6-plus-C-X1`, given in the session after the author's manual test of
the frozen technical candidate and confirmed in writing ("Autorizo la siguiente
instrucción"), with the exact identities behind it. It changes no product,
test, build, packaging, verifier, registry, schema, specification, ADR,
Classic or upstream file, and it does not modify any frozen commit. Besides
this record and its machine-readable mirror,
[`pre-g9b-r6-plus-c-x1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-x1-closeout.json),
it only updates status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r6_plus_c_x1_candidate_report.md), its evidence
mirror, the [authorization record](pre_g9b_r6_plus_c_x1_prompt_closeout_record.md),
the canonical prompt and the
[preparation characterization report](pre_g9b_r6_plus_c_x1_preparation_characterization_report.md)
record only frozen state. They are not amended. This record is the sole
authority for the author approval of `C-X1`.

## Author disposition

```text
T_R6PLUS_C_X1    = eabe6368f9e012d8186aa668a68a5bcc48cdcc17
                   tree fc8dfb290594d62cfc5240490ad5e1ee38cbc321
AUTHOR_SMOKE     = PASS
AUTHOR_APPROVAL  = PRE-G9B-R6-plus-C-X1 = PASS — AUTHOR APPROVED
selfApproved     = false
```

The author tested the frozen technical candidate manually: the previously
reproducible Classic failure (File → Export → Graphics View as Picture: empty
dialog and application hang) is no longer observed. The author smoke is
empirical product validation of the reported defect; it closes the
uncertainty left by the characterization, which could not reproduce the
severe symptom automatically. Neither the smoke nor the finite automated
evidence is claimed to prove the absence of every possible Swing hang.

## Provenance chain

Verified in the local repository before this closeout; each commit is the
single parent of the next and none is amended or rewritten:

```text
P_R6PLUS_C            39eb05bc16a18ae48167383e90fcf38b12a681b6  tree fc13f5f125a4e8d595dd450bfd47706be193e5cd
  (C closeout, published)
T_R6PLUS_C_X1_PROMPT  50baef73e784f764e95c9faf33d50188986ec3ed  tree 245a8a7a814dc7146a8c1cf32214019da6317999
  (characterization and preparation)
authorization         7a88a491104ecd3c75809d0b84fd1e7dcb3c0968  tree 2034b225d3fe5f790f7459e44bc2cb998f3c6a59
  (preparation approval, prompt amendment)
T_R6PLUS_C_X1         eabe6368f9e012d8186aa668a68a5bcc48cdcc17  tree fc8dfb290594d62cfc5240490ad5e1ee38cbc321
  (technical candidate, approved)
closeout              the commit that contains this record (reported outside it)
```

## Accepted implementation

One production file, `FileMenuD.java`: the Classic Graphics View as Picture
action no longer creates a worker thread for the dialog lifecycle. Its body —
`showGraphicExport()`, the clipboard fallback and the error handling — is
semantically unchanged. Accepted invariant:

```text
INV-X1  Construction and presentation of the Classic Graphics View as Picture
        dialog occur on the Swing EDT.
```

GeoCeDG v2 keeps its pre-existing EDT-correct route. No wider Swing/EDT
redesign is authorized or implied.

## Accepted evidence

```text
PHASE -Phase PRE-G9B-R6-plus-C-X1 on T_R6PLUS_C_X1
  verification-f07334b6440446dea412df7dc82ed95e
  ACCEPTED / COMPLETE, 3/3, exit 0, no diagnostic finding
  execution_plan_hash 6f93ccadff901943de7e7281fe9bab137bafa7eb60067eb80649cd79e8d5403c
  result_hash         6aa9a5f09396b5de3f8bcfb808286f720cc41c1b90fc078e3ae1fae9e35275a0
VERIFICATION_CLASS = BOUNDED_PHASE (frozen); INTEGRATION and FINAL not required, not run
```

Supporting development evidence on the staged candidate tree:
`INFRA_UNIT` `verification-cfaef23e25e44521b9603aab5615499b` (22/22) and
`STATIC` `verification-720f0d6c5cd44c0ab19614d86b3f05d2` (3/3). Focused and
repetition evidence: 55 JVMs, 275 dialog openings, 0 uncaught exceptions,
0 off-EDT dialog operations, 0 concurrency findings, 0 hangs, every dialog
populated, every Cancel successful. The complete Desktop producer
(`final.desktop`) passed after the new application-creating test scenarios
were isolated in child JVMs. The technical acceptance stays bound to
`T_R6PLUS_C_X1`; the closeout commit is documentary and is checked with
`STATIC` and `git diff --check` only.

## Observation dispositions

```text
OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT =
  RESOLVED IN PRE-G9B-R6-plus-C-X1 — AUTHOR APPROVED (2026-10-06)
```

The resolution is prospective and belongs to `C-X1`. The
[C closeout record](pre_g9b_r6_plus_c_closeout_record.md) is unchanged and
remains historically correct: at C, the observation was pre-existing, not
caused by `C`, accepted debt and deferred to post-C.

Preserved, not fixed and not absorbed into this closeout:
`OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES` (Classic startup outside the EDT,
Print Preview threading, the Save/Clipboard buttons of the export dialog, the
other worker-thread actions of `FileMenuD`);
`OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION`;
`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`;
`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`; the Desktop test-heap pressure, the
`exportPDF` UTF-8 debt, the AutoColor JVM-global state and the historical G9X1
authority-scan debt.

## Publication

The author authorizes the ordinary, non-force fast-forward publication of the
complete linear chain `39eb05bc → 50baef73 → 7a88a491 → eabe6368 →` this
closeout commit to `main`, provided that the live remote `main` is still
`39eb05bc`. No tag or release is required. The publication result is reported
outside this record, because the record cannot name its own commit.

## Authorization state

```text
PRE-G9B-R6-plus-C-X1       = PASS — AUTHOR APPROVED (publication authorized)
PRE-G9B-R6-plus-E1         = NOT AUTHORIZED (next operational subphase; no canonical
                             implementation prompt; its preparation is a separate task)
PRE-G9B-R6-plus-E2, E3, F1, F2 = NOT AUTHORIZED
PRE-G9B-R6-plus-F3 implementation = NOT AUTHORIZED
PRE-G9B-R6-plus-G          = NOT AUTHORIZED
PRE-G9B-R7                 = NOT AUTHORIZED
G9B                        = NOT AUTHORIZED
```

`C-X1` is a bounded post-C corrective activity and does not change the
author-approved order `P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 →
F2 → F3 → G`.

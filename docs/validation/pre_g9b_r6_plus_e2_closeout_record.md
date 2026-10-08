# PRE-G9B-R6-plus-E2 closeout record

```text
PRE-G9B-R6-plus-E2 = PASS — AUTHOR APPROVED
AUTHOR_SMOKE       = PASS WITH ACCEPTED NON-BLOCKING OBSERVATIONS
PHASE              = ACCEPTED / COMPLETE
INTEGRATION        = ACCEPTED / COMPLETE
POST-E2            = OBSERVATIONS REGISTERED — IMPLEMENTATION NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (E2 only, revised candidate T_R6PLUS_E2)
passClaimed               = true    (E2 only)
implementationAuthorized  = false   (POST-E2 P1–P4, E3, F1, F2, F3, G, PRE-G9B-R7,
                                     G9B and every other open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
SERIALIZATION CHANGE      = NONE    (closeout; the approved candidate carries
                                     geocedgUnits version 2)
newHeavyIntegration       = NONE    (closeout)
newHeavyFinal             = NONE    (closeout; FINAL not required by INTEGRATED_PHASE)
```

This record preserves the explicit author decision of 2026-10-08 on
`PRE-G9B-R6-plus-E2`, given in writing in the session ("Autorizo el siguiente
prompt", "Author Approval, Publication and POST-E2 Follow-up"), with the exact
identities behind it. It is the sole authority for the author approval of
`E2`. Its machine-readable mirror is
[`pre-g9b-r6-plus-e2-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-closeout.json).

Under the frozen-candidate rule, the
[original candidate report](pre_g9b_r6_plus_e2_candidate_report.md), the
[follow-up report](pre_g9b_r6_plus_e2_smoke_followup_report.md) and their
evidence mirrors record only frozen state and are not amended. Besides this
record and its mirror, the closeout changes only status markers of the approved
normative documents, one user-guide sentence that documents an accepted
observation, and status statements in the living
[roadmap](../roadmap/geocedg_roadmap.md) and
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md).

## Entry verification

```text
live remote main = origin/main = local main
                 = dc63b0e55f12eb96d3aea359db4476cbda6c89dc   (P_R6PLUS_E1_P_X1)
branch           = phase/pre-g9b-r6-plus-e2-smoke-followup, clean
history          = dc63b0e5 → 6ad853c6 → b1ee68c0 → 4a7043f2 (authorization)
                   → 8cfb2011 → 878ff66b (original candidate)
                   → 5a55ba24 (smoke follow-up author decisions)
                   → 3a724661 (revised candidate)
                   linear; no commit amended, squashed or rewritten
```

## Author decision

```text
T_R6PLUS_E2 (revised) = 3a7246614a6ef040d84a1440c4c10b3f93be5776
                        tree 00b5230d19debea5598299906f846f52b4d6e9d7
AUTHOR_SMOKE          = PASS WITH ACCEPTED NON-BLOCKING OBSERVATIONS
                        (focused re-smoke of the revised candidate; every
                        checklist item passed)
AUTHOR_APPROVAL       = PRE-G9B-R6-plus-E2 = PASS — AUTHOR APPROVED
scope                 = the implementation, B1, B2, the versioned geocedgUnits
                        persistence extension and their normative amendments
publication           = AUTHORIZED (ordinary fast-forward of main)
selfApproved          = false
```

## Accepted evidence

```text
PHASE PRE-G9B-R6-PLUS-E2 on 3a724661 / 00b5230d
            verification-ca5dd07b67854be5ad2115671a1bc1c9, ACCEPTED / COMPLETE, 7/7
INTEGRATION on 3a724661 / 00b5230d
            verification-48829d5691c4408f83f6b0dc07f4310f, ACCEPTED / COMPLETE, 19/19
            (standing diagnostics only: diagnostic.governance FINDING,
            diagnostic.historical-consistency UNAVAILABLE)
final.shared 6963 and final.desktop 1898 identities, executed and passing
            (inventory producers on the final code; 512 MB Desktop test heap)
```

Historical evidence kept unchanged: the original candidate `878ff66b`
(tree `e096b857`) with `PHASE` `verification-47c1a96df96440e59db84dd5098f1b66`
and `INTEGRATION` `verification-e1d28044ba814ed8ab216b676e1cbe06`, and the
author smoke of that candidate (`PASS WITH OBSERVATIONS`).

## Normative status after the decision

| Document | Status |
|---|---|
| [native-dimensions specification](../../geocedg/specs/dimensions/native-dimensions.md) `1.1` | `NORMATIVE / AUTHOR APPROVED` |
| [ADR 0034](../adr/0034-native-dimension-representation-and-presentation-seams.md) | `ACCEPTED — AUTHOR APPROVED` |
| [unit-system specification](../../geocedg/specs/units/unit-system.md) amendment `1.1` (§20) | `NORMATIVE / AUTHOR APPROVED`; version `1.0` text unchanged as history |
| [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md) amendment note | author approved; the 2026-10-02 decision unchanged |

Only status markers changed; every clause is the text of the approved
candidate.

## Observation dispositions

```text
OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS = A-INTENTIONAL-PROFILE-BEHAVIOR
                                             (classification accepted; unchanged)
OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET         = DURABLE LIMITATION (unchanged)
OBS-E2-1  extension-line gap and overshoot under mm construction units
          = ACCEPTED NON-BLOCKING — CHARACTERIZATION IN POST-E2 P1
OBS-E2-2  typed native dimensions are not dragged like tool-created ones
          = ACCEPTED NON-BLOCKING — DOCUMENTED; ENHANCEMENT IN POST-E2 P4
OBS-E2-3  unexpected zoom-out when starting algebra input
          = ACCEPTED NON-BLOCKING — POTENTIALLY PRE-EXISTING, NOT ATTRIBUTED TO E2;
            CHARACTERIZATION IN POST-E2 P2
OBS-E2-4  interactive access to embedded document macros
          = OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX, OPEN (preserved);
            POST-E2 P3
```

- **OBS-E2-1.** Reported with `constructionUnit = mm` only. The `E2` contract
  captures `Gap` and `Overshoot` once as model-space values: with a physical
  construction unit, 1 mm and 2 mm on paper at the drawing scale, independent of
  the zoom (native-dimensions §8.3). A first reading of the capture
  (`GeoCeDGDimensionTools.captureMagnitudes`) gives 1 and 2 model units in `mm`
  at `1:1`, 0.1 and 0.2 in `cm` and 0.001 and 0.002 in `m`, which would make the
  marks look large on screen exactly in `mm`; this is a hypothesis for P1, not a
  finding. The semantics are not changed by the closeout.
- **OBS-E2-2.** Dragging moves only an independent, labelled, unlocked offset
  number (native-dimensions §8.4). Typed commands with a named free offset are
  dragged; typed commands with a literal or computed offset are not. The user
  guide §9.5 (EN/ES) now states this. No command signature or `Offset` semantics
  changed.
- **OBS-E2-3.** Observed also in earlier phases; no input-preview or viewport
  code is changed by the closeout.
- **OBS-E2-4.** No toolbar, user-tool library, macro registry or command
  resolution change.

## Closeout verification

The closeout commit is a documentary descendant of the approved candidate, not a
replacement candidate. Because it also adds one sentence to each user guide,
`STATIC`, `INFRA_UNIT` and `PHASE PRE-G9B-R6-PLUS-E2` (whose selection runs the
guide tests) are run on it; those runs and the publication are reported outside
this record, which cannot name its own commit.

## Next activity

```text
NEXT = POST-E2 planning and characterization proposal (P1 gap/overshoot and
       physical units; P2 algebra-input zoom; P3 interactive document-macro
       access; P4 typed-dimension drag) — documentary proposal only;
       implementation NOT AUTHORIZED
E3, F1, F2, F3, G = NOT AUTHORIZED
```

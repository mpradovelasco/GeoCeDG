# POST-E2-P2-R1 closeout record

```text
POST-E2-P2-R1 = PASS — AUTHOR APPROVED   (conditional author approval of the D1
                                         successor; all conditions verified)
```

```text
selfApproved              = false
authorApproved            = true    (conditional, granted in advance by the author for
                                    the exact D1 successor defined in the instruction;
                                    conditions verified below)
passClaimed               = true    (POST-E2-P2-R1 only)
implementationAuthorized  = POST-E2-P3 design and bounded implementation after its
                            readiness gate (INTEGRATED_PHASE); P3 approval and
                            publication NOT AUTHORIZED; P4, TD-PDF-1, TD-PDF-2 NOT
                            AUTHORIZED
candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = YES — KEYBOARD SHORTCUT VALIDATION
GEOMETRIC SEMANTICS       = UNCHANGED
SERIALIZATION             = UNCHANGED
CLASSIC                   = UNCHANGED
```

This record preserves the author instruction of 2026-10-09 ("Ejecuta la siguiente
tarea", "GeoCeDG — POST-E2-P2-R1 / POST-E2-P3 — Conditional Author Approval, D1
Completion, Publication and P3 Execution"). The approval **derives from the
author's conditional D1 authorization**: the author accepted the smoke of the first
R1 candidate, selected D1 for the AltGr question and approved in advance the
precise D1 successor subject to eight conditions. Mirror:
[`post-e2-p2-r1-closeout.json`](../../geocedg/validation/post-e2/post-e2-p2-r1-closeout.json).
The [candidate report](post_e2_p2_r1_candidate_report.md), its evidence, the first
candidate's runs and the P2 characterization are preserved unchanged as evidence.

## Identities

```text
first R1 candidate   = 422dca03696fc6cae9b03a3a45c365daffbc74fe
                       tree 4fe0186e45b6c1ac91d1319d9caba8d2fbf8e043
                       STATIC verification-83ad5726da01452482dbd9a8ceff2c1a 3/3
                       INFRA_UNIT verification-a8986804c19c49159ae35e87b31472e1 22/22
                       PHASE verification-695904a30a644ceb8cd3ee856bd69c6f 3/3
                       AUTHOR_SMOKE = PASS (unexpected zoom resolved; navigation
                       controls satisfactory)
approved D1 successor = 38647c796e6499a10d5435844866d2144fdfbe24
                       tree ebcb853e7d947b792029db1d1fde8518be96e04a
                       STATIC verification-82ab76b6e1af4d9da66cc8a750510a45 3/3
                       INFRA_UNIT verification-873273517d724a3e96d28387520b5923 22/22
                       PHASE verification-475f9c9e124345c6831e19a91bf098a4 3/3
                       all ACCEPTED / COMPLETE
```

## Approved policy

Navigation factor-zoom chords that type or edit text are refused (`TEXT_ENTRY`) and
stored ones load as unassigned without rewriting the store:

- Shift as the only modifier on any key except F1–F24;
- Ctrl+Alt, with or without Shift (no Meta), on any key except F1–F24 (**D1**:
  the Windows AltGr modifier set);
- Alt alone with a numeric-keypad digit (Windows character code);
- chords carrying AltGraph remain not well formed (A7).

Eligible under the unchanged A7 reserved-key and conflict rules: function keys
with Shift, Ctrl+Alt or Ctrl+Alt+Shift (Ctrl+Alt+F11, Shift+F7), Ctrl without Alt,
Alt without Ctrl on non-keypad keys, chords with Meta. A physical-keyboard AltGr
smoke did not occur (keyboard injection unavailable); the guarantee rests on the
`KeyStroke` representation, by code and tests.

## Conditions (all satisfied)

| # | Condition | Evidence |
|---|---|---|
| 1 | change restricted to D1 and directly necessary tests and documentation | `422dca03..38647c79`: `entersText` (one rule), dialog text EN/ES, one new test method and two assertions, spec amendment, guide §12.4, report §7, evidence, inventory and pins |
| 2 | all applicable verification passes | STATIC, INFRA_UNIT, PHASE above; focused classes 7 + 4 + 7 + 6; official producers (discovery, `post-e2-p2.desktop`, `post-e2-p2-r1.desktop` 24, `final.shared` 6963, `final.desktop` 1938) all `COMPLETED`, exit 0; Checkstyle no new violation; upstream boundary 1010 files |
| 3 | previous smoke behavior intact | windowed probe on `38647c79` with a copy of the author's original preferences: 0 unexpected view changes over 79 keystrokes, 7/7 texts exact, stored Shift+A / Shift+Z unassigned, legitimate Shift+F7 zooms from the view and the field, menu Zoom Factor In zooms |
| 4 | no new unresolved functional or architectural issue | none; the AltGr physical smoke is a documented evidence limitation, not an open defect (no Ctrl+Alt character chord can be stored or bound) |
| 5 | no incompatible preference migration | inadmissible stored chords are read as unassigned; the stored text, the factor and valid chords are kept; nothing is rewritten on load (tests and byte-identical settings copy). The author's current file (Shift+F7 in, Ctrl+Shift+F7 out, written by the product at 14:15:14 during the author smoke) stays valid |
| 6 | GeoGebra Classic unchanged | only GeoCeDG-owned files changed; Classic windowed run on the R1 code path: 0 view changes, texts exact |
| 7 | working tree clean | clean at `38647c79` and at this closeout |
| 8 | final candidate an auditable descendant of `422dca03` | `422dca03 → 38647c79` (one commit), parent `422dca03` |

## Normative promotion

The *POST-E2-P2-R1 amendment* of `geocedg/specs/ui/cedg-workspaces.md` (after the A7
navigation paragraph, which stays unchanged) is promoted from `PROPOSED` to
`NORMATIVE / AUTHOR APPROVED` with this record.

## Next activity

```text
POST-E2-P3 = interactive document-macro access; design and bounded implementation
             authorized after a readiness gate (INTEGRATED_PHASE, PHASE + INTEGRATION);
             final approval and publication NOT AUTHORIZED
P4, TD-PDF-1, TD-PDF-2 = NOT AUTHORIZED
```

## Closeout verification

Documentary closeout (record, mirror, normative promotion, planning, mini-track and
roadmap status); `STATIC` runs on the closeout commit, reported with the publication.

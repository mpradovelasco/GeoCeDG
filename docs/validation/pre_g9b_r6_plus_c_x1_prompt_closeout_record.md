# PRE-G9B-R6-plus-C-X1 preparation package closeout and authorization record

```text
C-X1 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-C-X1     = AUTHORIZED FOR IMPLEMENTATION
```

```text
selfApproved              = false
authorApproved            = true    (C-X1 documentary preparation package only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = true    (C-X1 implementation and technical verification only;
                                     E1, E2, E3, F1, F2, F3, G, PRE-G9B-R7, G9B: false)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE    (this record)
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author instruction of 2026-10-06, pasted
into the session and confirmed by the author in writing ("Autorizo el prompt
lanzado."), that approves the documentary preparation package of
`PRE-G9B-R6-plus-C-X1`, accepts the identifier, decides `DQ-X1-1` to
`DQ-X1-6`, freezes the verification class and authorizes the bounded
correction of `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`. The package is not a
phase: approving it closes the preparation, not `C-X1`. `C` stays closed; its
technical acceptance and the historical classification of the observation
(`PRE-EXISTING — NOT CAUSED BY C — ACCEPTED C DEBT — DEFERRED TO POST-C`) are
not reopened, reinterpreted or amended. This record changes no product, test,
build, packaging, verifier, registry, schema, specification, ADR or
serialization file, and it does not modify the frozen preparation candidate.
Its machine-readable mirror is
[`pre-g9b-r6-plus-c-x1-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-x1-prompt-closeout.json).

## Entry verification

```text
P_R6PLUS_C           = 39eb05bc16a18ae48167383e90fcf38b12a681b6
                       (PRE-G9B-R6-plus-C = PASS — AUTHOR APPROVED — PUBLISHED)
T_R6PLUS_C_X1_PROMPT = 50baef73e784f764e95c9faf33d50188986ec3ed
                       tree   245a8a7a814dc7146a8c1cf32214019da6317999
                       parent 39eb05bc16a18ae48167383e90fcf38b12a681b6 (single commit,
                              linear descendant of the published base)
                       prompt blob 3402b02ab73615b8d00880952116a61a70274d25
                              (state PREPARED — NOT AUTHORIZED)
                       branch phase/pre-g9b-r6-plus-c-x1-prompt (local; never pushed)
delta                = documentary only: the prompt, the characterization report,
                       its JSON mirror, one roadmap row and the documental version,
                       mini-track status lines; no product, test, build or registry file
local main = origin/main = live remote main = 39eb05bc16a18ae48167383e90fcf38b12a681b6
worktree             = clean
```

Accepted evidence of the package: `STATIC`
`verification-1d545d3142454ed38e8f898b94acddbc` on `T_R6PLUS_C_X1_PROMPT`,
exit 0, `ACCEPTED / COMPLETE`, 3/3, plan hash
`25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result hash
`ba193f8d6d5969985dbf713c63a668da43874ec8df0d64061cd75fdcc50c4943`, with the two
standing diagnostics; `Test-PromptContractDocument` (`task` profile): seven
fields in order, `execution_safe = true`.

The frozen preparation candidate is not amended. Where the prepared prompt
states a recommendation or a pending question for `DQ-X1-1` to `DQ-X1-6`,
**this record prevails**. As the first tracked edit of the phase, the canonical
prompt is amended to `AUTHORIZED FOR IMPLEMENTATION`, recording these
dispositions and replacing the pending language they supersede.

## Authorization

```text
PRE-G9B-R6-plus-C-X1  = AUTHORIZED FOR IMPLEMENTATION
PUBLISHED_BASE        = 39eb05bc16a18ae48167383e90fcf38b12a681b6   (P_R6PLUS_C;
                        tree fc13f5f125a4e8d595dd450bfd47706be193e5cd)
BRANCH_START          = 50baef73e784f764e95c9faf33d50188986ec3ed   (T_R6PLUS_C_X1_PROMPT;
                        tree 245a8a7a814dc7146a8c1cf32214019da6317999; not published)
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt
                        (local; no rebase)
VERIFICATION_CLASS    = BOUNDED_PHASE        (frozen; not inherited from C)
PLANNED_ACCEPTANCE    = registered PHASE PRE-G9B-R6-PLUS-C-X1, ACCEPTED / COMPLETE on
                        the exact clean candidate commit and tree; no INTEGRATION and
                        no FINAL unless a stop condition forces reclassification
PRODUCT_FILES         = source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/
                        menubar/FileMenuD.java only; no second production file is
                        pre-authorized
STOP_STATE            = PRE-G9B-R6-plus-C-X1 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                        AUTOMATED VERIFICATION = ACCEPTED / COMPLETE
                        AUTHOR_SMOKE = PENDING (required)
                        selfApproved = false, authorApproved = false, passClaimed = false
```

Not authorized: `E1`, `E2`, `E3`, `F1`, `F2`, `F3` implementation, `G`,
`PRE-G9B-R7`, `G9B`; `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION`,
`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`,
`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`; Desktop test-heap pressure, the
`exportPDF` callback UTF-8 debt, the AutoColor JVM-global state and the
historical G9X1 authority-scan debt; any change of the operational order of the
mini-track (`C-X1` is a bounded post-`C` corrective activity and does not
replace `E1`); push to `main`, merge, publication, tag, release, `PASS — AUTHOR
APPROVED`, rebase, squash, and amendment of the frozen preparation candidate or
of any historical `C` commit. A remote feature branch is not pushed.

## Frozen characterization

The author freezes the characterization of the preparation package:

```text
DEFECTIVE ROUTE   FileMenuD.exportGraphicAction -> new Thread -> showGraphicExport()
                  -> GraphicExportDialog construction -> initGUI / pack / setVisible /
                  loadPreferences / UI update, off the EDT while the EDT validates and
                  paints the same hierarchy
C_CAUSALITY       = FALSE   (same ownership on P_R6PLUS_C and on e6135028)
FLATLAF_ROOT_CAUSE = FALSE  (same ownership and exceptions under Metal)
PROTOTYPE         150 openings, 0 uncaught exceptions, 0 off-EDT dialog operations,
                  all dialogs populated, all Cancels successful

PROVEN            the Classic launch violates Swing EDT ownership, and the proposed
                  EDT correction eliminates that race
NOT YET PROVEN    the race is the sole necessary cause of every manually observed
                  empty-dialog/hang event
```

The causal statement must not be strengthened by the phase.

## Author dispositions on `DQ-X1-1` to `DQ-X1-6`

| ID | Outcome | Disposition |
|---|---|---|
| `DQ-X1-1` | accepted | Canonical identifier `PRE-G9B-R6-plus-C-X1`; registry phase id `PRE-G9B-R6-PLUS-C-X1`. |
| `DQ-X1-2` | option A | Minimal caller-site correction in `FileMenuD.java`: remove the dedicated `new Thread`/`start()` of `exportGraphicAction`; execute the existing body directly within the Swing action dispatch; preserve `showGraphicExport()` behavior, the clipboard fallback and the existing error/fallback handling unless a change is strictly required by the removal of the worker thread. No generic marshalling in `showGraphicExport()`; the GeoCeDG v2 route is not modified to normalize both paths. If a supported invocation legitimately enters `exportGraphicAction` off the EDT, stop for author review instead of introducing a wider dispatcher abstraction. Invariant `INV-X1`: construction and presentation of the Classic Graphics View as Picture dialog occur on the Swing EDT. |
| `DQ-X1-3` | accepted, frozen | `BOUNDED_PHASE`; acceptance = registered `PHASE` `PRE-G9B-R6-PLUS-C-X1` on the exact candidate commit and tree. `INTEGRATED_PHASE` is not inherited from `C`; `FINAL` is not run because earlier phases used it. If the verification infrastructure shows that `BOUNDED_PHASE` cannot cover the change, stop and propose reclassification. |
| `DQ-X1-4` | out of scope | Classic startup outside the EDT, Print Preview threading, the Save/Clipboard buttons of the export dialog, the other worker-thread actions of `FileMenuD` and unrelated toolbar/startup layout races are not part of `C-X1` and are not fixed; they are recorded as separate observations. The occasional Classic startup failure is not an authorization to redesign Classic startup. |
| `DQ-X1-5` | accepted with precision | The technical candidate stops at `TECHNICAL CANDIDATE PENDING AUTHOR REVIEW` with `AUTHOR_SMOKE = REQUIRED`. The author smoke is the evidence for the manually observed severe symptom: Classic diagnostic, File → Export → Graphics View as Picture — dialog populated, application responsive, Cancel closes the dialog, no empty-dialog/hang behavior — plus a quick normal GeoCeDG Graphics View as Picture regression. Finite repetition never proves the absence of all hangs. |
| `DQ-X1-6` | accepted | An isolated, windowed, process-level regression test or probe of the real Swing path is included if it can be made deterministic and bounded. The primary assertion is thread ownership and lifecycle correctness, never the mere absence of a random hang. |

## Required automated evidence

At minimum, deterministically: (1) the real Classic File-menu action runs the
picture-export dialog lifecycle on the EDT; (2) dialog construction and
population are complete; (3) no uncaught EDT exception during the tested
lifecycle; (4) Cancel closes the dialog; (5) repeated open/close cycles stay
valid; (6) the File-menu accelerator path that resolves through this action
obeys the same invariant; (7) the existing fallback behavior is preserved;
(8) the normal GeoCeDG v2 Graphics View as Picture remains correct; (9) the
GeoCeDG export-scale and unit UI stays absent from Classic; (10) no
serialization or format change. Repetition of the corrected Classic path must
show zero off-EDT dialog lifecycle operations, zero uncaught EDT exceptions
attributable to the path, successful population and successful Cancel.

## Unchanged semantics

```text
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE
```

Kernel geometry, DAG semantics, CeDG spatial semantics, Locus V2, Spline V2,
`constructionUnit`, `presentationUnit`, `drawingScale`, `ExportArea`, DXF,
LaTeX and EMF semantics, document identity, serialization, undo/redo, the
document modified state and every output format or schema stay unchanged.
Classic keeps host behavior and acquires no GeoCeDG construction-unit control,
`drawingScale` UI, non-physical wording or export-state semantics. GeoCeDG v2
Graphics View as Picture stays unchanged; the GeoCeDG legacy fallback, which
reuses `FileMenuD`, may benefit from the lifecycle correction but gains no new
semantic authority.

## Stop conditions added by the author

Stop before broadening the change if: removing the worker thread does not
eliminate the off-EDT lifecycle; the action is legitimately invoked off the
EDT by a supported route; another production file must change;
`showGraphicExport()` must change; export semantics or serialization would
change; the clipboard fallback cannot be preserved; the corrected isolated path
still produces the same EDT race; a different independent cause is needed to
explain basic dialog corruption; the verification class must be widened; the
current source materially differs from the characterized source. The remaining
inability to reproduce the author's severe hang automatically is not, by
itself, a stop condition.

## Authorization state

```text
PRE-G9B-R6-plus-P0, A-1, B, D0, D1, A-2, C = PASS — AUTHOR APPROVED — PUBLISHED
C-X1 preparation package   = PASS — AUTHOR APPROVED               (not published)
PRE-G9B-R6-plus-C-X1       = AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-F3         = PLANNED — AUTHOR APPROVED — PUBLISHED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-R6-plus-E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.

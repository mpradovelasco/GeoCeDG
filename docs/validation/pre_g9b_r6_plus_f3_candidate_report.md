# PRE-G9B-R6-plus-F3 — File → Insert and restricted native insertion: technical candidate report

```text
TASK                      = PRE-G9B-R6-plus-F3-IMPLEMENTATION-AUTHORIZATION
AUTHORIZATION             = AUTHOR AUTHORIZED — SUBJECT TO ENTRY GATES (2026-10-13);
                            entry gate and verification-contract gate PASS
                            (implementation authorization record, commit bc1c9ac6)
APPROVED BASE             = P_F3_DESIGN_CLOSEOUT 108d9cb6591aef92135a95ec9bf71482fcd78e7f
                            tree 45644a6fabdb5e3b4cb23df063761f57d3fdf133
CHAIN                     = 108d9cb6 → bc1c9ac6 (authorization, class frozen) → the
                            candidate (this report)
VERIFICATION_CLASS        = INTEGRATED_PHASE — FROZEN (PHASE + INTEGRATION; no FINAL)
REGISTERED PHASE          = PRE-G9B-R6-PLUS-F3 (compile.desktop.semantic,
                            workspace-profile.product,
                            junit.desktop.pre-g9b-r6-plus-f3.semantic; selection
                            pre-g9b-r6-plus-f3.desktop)
TECHNICAL CANDIDATE       = the commit that contains this report (frozen); its identity
                            and the acceptance runs are reported outside this file
AUTHOR_SMOKE              = PENDING (required)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PRODUCT CLOSEOUT          = NOT AUTHORIZED; DEBT-R6PLUS-FILE-INSERT-SURFACE stays open (C20-1)
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false; authorApproved = false; passClaimed = false
```

Machine-readable mirror:
[`pre-g9b-r6-plus-f3-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-candidate-evidence.json).
Authority: [F3 author design closeout record](pre_g9b_r6_plus_f3_author_design_closeout_record.md)
(Q1–Q7, `DQ-F3-1`–`DQ-F3-24`, `P1`–`P7`, FR-F3-1..8);
[implementation authorization record](pre_g9b_r6_plus_f3_implementation_authorization_record.md);
contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f3-file-insert.prompt.md)
(not amended); specification:
[`document-insertion.md`](../../geocedg/specs/ui/document-insertion.md) v1.0
(`PROPOSED`).

## 1. Architectural ownership (U0)

GeoCeDG Desktop only. No file under `source/shared/**`, no inherited GeoGebra
Desktop class and no Classic code is changed; no XML, serialization, parser,
preference, unit or kernel change. Classic keeps `UndoManagerD`, the inherited
Open dialog with its four filters and OFF, `AppD.insertFile` and
`AppD.applyTemplate` (never called by GeoCeDG).

| Component | Role |
|---|---|
| `GeoCeDGUndoManager` (new, extends `UndoManagerD`) | observation-only overrides: C1 accounting (`storeUndoInfo`, `storeAndNotifyUnsaved`, `commitUndoBaseline`, `pruneStateList`, `onStoreUndo`); `detach`/`attach` of the original commands and cursor |
| `AppGeoCeDG` | stateless `getUndoManager(Construction)` (G6); hidden helpers (H-1, H-3); action entry points; isolation guards (`saveGeoGebraFile`, `saveCurrentFile`, `dispatchKeyEvent`, picture copy) and the prompt-free Close |
| `GeoCeDGRecoverySnapshot` (new) | verified native recovery snapshot, provenance, ownership, atomic recovery copy |
| `GeoCeDGInsertionAdmission` (new) | `P4` matrix, scripts / 3D / spatial rejection, M1 closure and digests, `DQ-F3-24`, L1 duplicates |
| `GeoCeDGInsertionCopyPaste` (new, extends `CopyPasteD`) | operation-scoped buffer (P2): kernel `insertFrom`, D1 provenance, ordered M1 transfer |
| `GeoCeDGDocumentInsertion` (new) | preflight, helper, admission, dry run, barrier, commit, strict verification, success, K1-S, K1-R-H, serious hand-over, certificates |
| `GeoCeDGInsertionIsolation` (new) | P7 isolation: guards, modal dialog, exits Save verified copy / Reopen / Close |
| `GeoCeDGStyleApplication` (new) | T0 Apply Styles (P5) |
| `GeoCeDGInsertionSourceChooser` (new) | X1 chooser |
| `GuiManagerGeoCeDG` | GeoCeDG Open (A1, OFF-B), OFF refusal for every `doOpenFiles` caller, isolation refusal of Save / Save As / open |
| `GeoCeDGActionRegistry` | two targets; every action unavailable while isolated |
| `GeoCeDGCopyPaste`, `GeoCeDGEuclidianController`, `GeoCeDGAlgebraInputSubmission` | isolation guards (copy/paste, pointer edits, Algebra input) |
| `GeoCeDGDocumentPolicy`, `GeoCeDGProfile`, `GeoCeDGUserToolLibrary` | X1/OFF-B name rules; catalog 133; digest package-accessible |
| `apps/geocedg/application-profile.yml` | `document.insert-file`, `document.apply-styles`, group `file-insert`, File entry, EN/ES texts |
| `tools/agent/workspace-profile-validation.ps1` | `PRE-G9B-R6-plus-F3` amendment (two action ids); static-contract input re-pinned |

## 2. Stage A — transaction infrastructure and recovery safety

**A1 Undo manager.** Installed for every GeoCeDG document (and helper) through
the stateless factory; the host constructor's first undo initialization already
runs it (`f3a01`). Ordinary Undo/Redo unchanged (`f3a02`; the whole Desktop
suite runs on it in INTEGRATION). History is moved: `detach` returns the
original command objects and cursor; `attach` installs them and treats the
action refresh as best effort (`f3a10`, `f3a12`). ER-F3R-3 regression: the
inherited `undoHistoryTo` copy loses labels, `detach` does not (`f3a11`).

**A2 Accounting C1** (invariant `pending == 0 && !degraded ⇒ no current-generation
store can still change the history`), exit by exit:

| Exit | Rule implemented | Test |
|---|---|---|
| success | counted at scheduling; decrement only after the store body's unmarked `pruneStateList` | `f3a03`, `f3a06` |
| synchronous callers (`storeAndNotifyUnsaved`, `commitUndoBaseline`) | thread-local marker, never counted | `f3a03` |
| delayed notification | stays counted until the body completes | `f3a04` |
| stale discard after a baseline | no callback; reset by the baseline; never lands | `f3a05` |
| exception exit (current or stale) | `degraded`, no decrement; only a baseline re-establishes | `f3a07` |
| other `Error` | no callback; stays counted (fail closed) | `f3a08` |
| scheduling (thread-creation) failure | `degraded`, rethrown | `f3a09` |
| uncertain state at the gate | rejection before mutation (degraded immediately; pending after the bounded wait) | `f3i13` |

No C2, C3 or shared change.

**A3 Barrier and attach.** Inside one block synchronized on the manager, on the
EDT: quiescence, native snapshot, detach, committed baseline captured by
reference and preflighted (`GeoCeDGDocumentInsertion.barrierBlock`). Attach only
with `pending == 0`, not degraded and the exact temporary list, else a closing
barrier (`f3r04`: a late store before attach never lands). Cleanup failures are
suppressed into the original failure (`f3r02`, `f3r07`).

**A4 Native recovery snapshot.** Public writer `writeGeoGebraFile(OutputStream,
false)`; entries `geogebra.xml`, `geogebra_macro.xml`, `geogebra_javascript.js`,
`geogebra_defaults2d.xml`, images; preflight and root-identity check; provenance
(`f3s01`); recovery copy reopens with macros, image, hidden layer, units,
library JavaScript, defaults and the original UUID (`f3s02`); tampered or
foreign archives refused (`f3s03`); ownership and release (`f3s04`); capture
failure rejects before mutation (`f3i14`).

**A5 Recovery.** K1-S certified with nothing reset, for saved and modified
documents, macros removed, insertion never reappears (`f3r01`); removal failure
→ guarded reload, certified, reset declared (`f3r02`); identity payload → K1-R-H
from the barrier baseline captured by reference after the paste store landed
(G4), certified, export-link reset declared, insertion never reappears
(`f3r03`). The withdrawn K1-R, `undoHistoryTo`, `restoreCurrentUndoInfo` and the
inherited undo-load fallback are not used.

**A6 Isolation.** Serious failures (two failed reloads `f3r05`; failed
certificate `f3r06`; unsafe attach `f3r07`) isolate the drawing. Routes probed
while isolated, all refused (`f3r05`): live save, Save, Save As, seven profile
actions, paste, Undo/Redo, update scripts, animation, the save-prompt route, New,
open/drop, global keys, Insert File, Apply Styles, picture copy, pointer edits
in Graphics, Algebra input. Exits: Save verified copy refused over the current
file and written to a new file with the original UUID and the pre-insertion
construction (the current file never written); Reopen with Undo re-enabled
first (`f3r05`); Close without prompt (`f3r06`); a history kept aside after an
unsafe attach is released at the exit (`f3r07`).

**EDT.** Every operation runs on the event dispatch thread (off-EDT calls are
refused, `f3i12`); waits are bounded (5 s) and outside the monitor; store
threads never need the EDT (`GuiManagerD.updateActions` has no EDT hand-off).

## 3. Stage B — restricted native insertion R

| Matrix family | Test |
|---|---|
| elementary rows (points, lines, rays, segments, vectors, circle, ellipse, functions and chains, polygon with edges, angle from points, numbers, booleans, dependent text, list, sequence, show condition, layer) | `f3i01`, `f3i02` |
| Locus V2 family (locus, lengths) and Spline V2 | `f3i07` (fresh identities, target ids kept, save/reopen, UUID) |
| Linear and Aligned dimensions | `f3i08` (values unscaled, target units unchanged, D1 notice) |
| IsoABorder | `f3i09` (export link not imported) |
| macro objects | `f3i05`, `f3i06` |
| `.ggb` source (Classic writer) | `f3i15` |
| rejections: missing, `.off`, other extension, garbage, empty, scripts, 3D point, free text, arc, identity + macros, macro conflict, Locus V2 unavailable, history busy, snapshot failure, isolated | `f3i03`, `f3i04`, `f3i06`, `f3i11`, `f3i13`, `f3i14`, `f3r05` |

Every rejection leaves the serializable state (XML, identities, macro digests,
saved state, file, UUID), the history (same command objects, cursor, labels),
the window buffer and its provenance, the recent-file list and the runtime
instances unchanged (`f3i03`). A success is one undo step on the held prefix
(Redo discarded as for any new action), keeps the UUID, keeps target objects
and identities, renames conflicts deterministically (`A_1`, `a_1`), and one Undo
restores the pre-insertion XML (`f3i01`). The selection is cleared and the Move
tool activated by the inherited paste (recorded, not asserted as preserved).

## 4. Stage C — M1, buffer and T0

**M1/MU-1.** Transfer once, digest-equal reuse without duplicate, binding to the
target macro, Undo keeps the tool (`f3i05`); transitive closure transferred in
dependency order (`f3i06`); different definition rejected (`f3i04`, `f3i06`);
identity + macros rejected (`f3i03`). Implementation note: the inherited paste
adds only directly used macros, in an unordered set; the buffer therefore
registers the admitted closure itself, in the source's registration order, and
`verifyMacros` checks every transferred and reused macro by digest after the
commit and in the dry run.

**Buffer and D1.** The window buffer and provenance are untouched and Ctrl+V
after an insertion pastes the user's copy (`f3i10`); the notice comes from the
inserted document (`f3i08`); failures change nothing (`f3i03`).

**T0.** Only the six properties change; exclusions (dimension, IsoABorder, Locus
V2 family outputs) untouched; geometry, layers, captions, fixed, visibility,
hidden layers and units unchanged; one undo step; Undo restores the XML; Redo
reapplies (`f3t01`); selection scope (`f3t02`); invalid or missing source
changes nothing (`f3t03`).

## 5. Surface

File → Insert after the Open group with both actions, no accelerator, catalog
133 (`f3u01`); EN/ES names and messages (`f3u02`); registry dispatch, chooser
Cancel, X1 stem resolution, OFF refusal (`f3u03`); GeoCeDG Open: OFF refused for
the chooser path and every `doOpenFiles` caller, a stem never resolves to `.off`,
document stems resolve (`f3u04`); helpers leave no recent-file entry and no
listener; Classic keeps its GUI manager (`f3u05`). Pinned tests amended: catalog
131 → 133 in nine classes; File flatness admits the one approved `file-insert`
group (`G9U1WorkspaceSurfaceTest`).

## 6. Q1–Q7, `DQ-F3-*` and `P1`–`P7` mapping

| Decision | Implemented by | Evidence |
|---|---|---|
| Q1 / `DQ-F3-21` (K1 corrected) | §2 | Stage A tests |
| Q2 / `DQ-F3-5`, `-6`, `-13`, `-14`, `-18` | admission, dry run, verification, L1, one undo step | §3 |
| Q3 / `DQ-F3-15`, `-22`, `-24` | M1, MU-1, identity + macros rejection | §4 |
| Q4 / `DQ-F3-7`, `-16` (`P2`) | operation-scoped buffer | `f3i08`, `f3i10` |
| Q5 / `DQ-F3-8`, `-9`, `-17`, `-23` (`P5`) | T0 "Apply Styles…" | `f3t01`–`f3t03` |
| Q6 / `DQ-F3-20` (C20-1) | debt stays open | §10 |
| Q7 / `DQ-F3-1`–`-4`, `-10`–`-12`, `-19` | N1, A1, X1, OFF-B, H-1/H-3, F-1, INTEGRATED_PHASE, U0 | §5, §1 |
| `P1` items 1–5 | surface, containers, R, T0 | §3–§5 |
| `P3`, `P4`, `P6`, `P7` | M1; matrix; messages; recovery and isolation | §2–§4 |

## 7. FR-F3-1 to FR-F3-8 (technical outcomes; not an author approval)

| Condition | Outcome | Evidence |
|---|---|---|
| FR-F3-1 accounting | PASS | `f3a03`–`f3a09`, `f3i13` |
| FR-F3-2 history semantics | PASS | `f3a10`, `f3a11`, `f3r01`–`f3r04`, `f3i01` |
| FR-F3-3 native snapshot | PASS | `f3s01`–`f3s04`, `f3i14`, `f3r05` |
| FR-F3-4 isolation | PASS for every in-process route probed (`f3r05`) and the three exits (`f3r05`–`f3r07`); real-window modality, the window close button and an OS file drop are author-smoke items | §2 A6 |
| FR-F3-5 EDT | PASS | `f3i12`; bounded waits; all transactions on the EDT |
| FR-F3-6 document identity | PASS | `f3i01`, `f3i07`, `f3i15`, `f3s02`, `f3r03`, `f3r05` |
| FR-F3-7 semantic admission | PASS | §3 matrix, `verify`, `verifyMacros` |
| FR-F3-8 compatibility | PASS | no shared/inherited/Classic change; upstream boundary; `f3a01`, `f3u05` |

## 8. Observations and limitations

- `OBS-F3-PROTECTED-LABELS`: the inherited construction copy reserves the source
  labels as protected labels of the target (session only; cleared at New, Open
  or reload); K1-S does not reset them (spec §9.1).
- `OBS-F3-TEXT-THICKNESS`: line thickness of texts is not serialized, so a
  reload shows 5 where the live text had 0 (inherited; observed through Redo
  after Apply Styles; spec §9.2).
- Isolation was exercised on embedded hosts; the modal dialog in a real window,
  its close button and an OS file drop are covered by author smoke (spec §9.3).
- Admission is deliberately fail-closed: static texts, arcs, sectors,
  parabolas, hyperbolas, images, buttons, input boxes, CAS cells, independent
  angles and anything unlisted are refused (`DEFER-F3-UNLISTED-TYPES`).
- Implementation detail inside the approved design: the orchestration's own
  undo store is made and identified before the history is re-attached (spec
  §5.7), so a success is certified before the attach.

## 9. Changed paths (from `bc1c9ac6`)

Product: the Desktop classes of §1, `apps/geocedg/application-profile.yml`.
Tests: `PreG9BR6PlusF3UndoAccountingTest` (12), `PreG9BR6PlusF3RecoverySnapshotTest`
(4), `PreG9BR6PlusF3InsertionTest` (15), `PreG9BR6PlusF3RecoveryTest` (7),
`PreG9BR6PlusF3SurfaceAndStylesTest` (8), fixtures `PreG9BR6PlusF3Fixtures`,
`PreG9BR6PlusF3Documents`; pins in nine existing classes. Registration:
`verification-registry.json` (phase, two nodes, two catalog pins),
`verification-junit-inventory.json` (selection `pre-g9b-r6-plus-f3.desktop`,
discovery and `final.desktop` refreshed by the official updater),
`verification-static-contracts.json`, `verification-final-coverage.Tests.ps1`,
`workspace-profile-validation.ps1`, `docs/upstream/modified-files.yml`.
Documentation: the specification, EN/ES guides (§4.2, §4.6, quick reference),
this report and its mirror, roadmap and mini-track status.

## 10. Verification

Development runs (not acceptance evidence): the five F3 classes (46 tests) and
the planned phase selection (458 tests before the guide and flatness
amendments; the amended classes re-run green); Checkstyle without findings in
F3 files; upstream boundary OK (1052 registered files). Official producers for
the inventory (`artifacts/agent/pre-g9b-r6-plus-f3-inventory/`): discovery
`shared` and `desktop` (dry runs), executed `pre-g9b-r6-plus-f3.desktop` (458
tests, 0 failed) and executed `final.desktop` (2085 tests, 0 failed), applied by
the official updater; `junit_inventory` re-pinned. Development `INFRA_UNIT`
verification-8928c7b0bba3488fbe2e307adeaaedfc was `REJECTED_VERIFICATION_CORE` (`infra.final-coverage`: the new
selection's `input_identities` was an object instead of an array; corrected);
the rerun verification-58bd9566233b4153b770144383c40b27 and development `STATIC` verification-3ea051df3c244ee3ad16567a9b1f54fe are
`ACCEPTED / COMPLETE` (standing governance finding and historical-consistency
unavailable only). The frozen PHASE, INTEGRATION and STATIC runs on
this commit are reported outside this artifact. `DEBT-R6PLUS-FILE-INSERT-SURFACE`
stays open until an author product closeout.

## 11. Author smoke checklist (not performed by the agent)

Run in English and Spanish.

1. **File → Insert** shows *Insert File…* and *Apply Styles…* after the Open
   group; **File → Open…** shows only the GeoCeDG documents filter (no Insert,
   Template or OFF types); typing `x.off` in Open shows "OFF files are not
   supported"; Cancel does nothing.
2. Insert a `.cedg` saved in **m** into an **mm** document: the status-bar notice
   appears; numbers are unchanged; one Undo removes the whole insertion; Redo
   brings it back; Ctrl+V afterwards pastes what you copied before.
3. Insert a document whose names collide (A, B, a): inserted objects become
   `A_1`, `B_1`, `a_1`; the existing objects and their dependents are untouched;
   no label dialog.
4. Insert a document using a curated tool into an empty document, then into one
   already holding the same tool: the tool exists once. Insert a document whose
   tool `MyMid` differs from the drawing's `MyMid`: refused, nothing changes.
5. Insert a Locus V2 + Spline V2 document, a document with Linear and Aligned
   dimensions and one with an ISO A sheet: objects correct and editable,
   dimension texts in the drawing's units, the sheet not linked to the export
   area; save, close and reopen: all present.
6. Insert documents with a script, a 3D point, a free text, and Locus V2 plus a
   tool: each refused with a message naming the reason; nothing changed.
7. Insert a missing file, a `.txt` renamed `.cedg`, and an empty document:
   refused; nothing changed.
8. Apply Styles from a template on several layers with a hidden layer, captions,
   fixed objects, a conditional object and a dimension: only colour, line
   style/thickness, point style/size and fill opacity change; the dimension, ISO A
   sheet and Locus V2 keep their styles; one Undo restores them.
9. After any insertion the Open Recent list is unchanged; the selection is cleared
   and the Move tool active.
10. Serious-failure dialog (not reproducible without fault injection): if it
    ever appears, check that the window cannot be edited behind it, that Save
    verified copy refuses the current file and writes a new file, and that
    Reopen and Close never ask to save.
11. Classic (diagnostic launcher): File → Open still offers its four file types,
    including OFF.

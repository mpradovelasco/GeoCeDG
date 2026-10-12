# PRE-G9B-R6-plus-F3 — DQ-F3-21 focused recovery safety report

- Activity: `PRE-G9B-R6-plus-F3-DQ21-FOCUSED-RECOVERY-SAFETY-GATE` (author
  instruction of 2026-10-12; focused characterization, temporary probes,
  documentary candidate)
- Status: **CHARACTERIZATION COMPLETE — PENDING AUTHOR DISPOSITION**
- Base: `T_F3_DECISION_RECONCILIATION`
  `3e24ed3aca422eef4e844a6c1230a3a3fc599c21`, tree
  `65e282431a6ece824c783e8217bed74b85b79e68`, parent
  `8b41af11316f90d7a068ce6af4859213240ce6a6` (STATIC
  `verification-f7ab50a453f74a03bca60b0ad7d2d032` `ACCEPTED / COMPLETE`)
- Companions: [updated DQ-F3-21 assessment](pre_g9b_r6_plus_f3_dq21_recovery_option_assessment.md);
  [risk register update](pre_g9b_r6_plus_f3_risk_register_recovery_gate_update.md);
  [author decision sheet update](pre_g9b_r6_plus_f3_author_decision_sheet_recovery_update.md);
  machine-readable [recovery-state matrix](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-dq21-recovery-state-matrix.json)
- Earlier evidence kept (unchanged): the
  [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md)
  (`DQ21-Q1`–`Q5`) and the correction `ER-F3R-1` in the
  [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_reconciliation.md)
- `selfApproved = false`

No product code, permanent test, specification, ADR or author decision was
changed. Every probe was a scratch class outside the tracked tree; its build
traces were removed (§9).

## 1. Entry gate (2026-10-12)

| Check | Result |
|---|---|
| HEAD, tree, parent | `3e24ed3a…`, `65e28243…`, `8b41af11…` |
| linear chain | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` → `3e24ed3a` |
| `main`, `origin/main`, live remote `main` | `e70718fe` |
| receipt | `verification-f7ab50a4…` `ACCEPTED / COMPLETE`, `COMPLETED`, bound to `3e24ed3a`/`65e28243` |
| worktree | clean |
| product delta since `main` (`source/`, `apps/`, `tools/`, `geocedg/specs/`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/`) | none |
| previous decision-reconciliation artifacts | present and unchanged |
| branch | `phase/pre-g9b-r6-plus-f3-dq21-recovery-safety-gate` (local, from `3e24ed3a`) |
| result | **PASS** |

## 2. Recovery invariant and categories

The pre-insertion state is analysed as `S0 = (C, I, D, U, M, Σ, P)` —
construction and DAG, durable identities, document identity and persistence
state, complete Undo/Redo history (list, cursor, pending stores), macro
registry, session state, presentation and operational state. A recovery is
classified by its verified postconditions only:

| Category | Admissible postconditions |
|---|---|
| **INSERTION REJECTED BEFORE MUTATION** | `C, I, D, U, M, Σ, P` unchanged; live instances the same objects; no undo store scheduled |
| **INSERTION FAILED — RECOVERY CERTIFIED** | full document XML byte-identical (including the root `id`); history list and cursor identical; live instances, Parallel/Perpendicular memory and IsoABorder link kept; macros and saved flag restored; declared inherited effects only: selection cleared, Move tool active |
| **INSERTION FAILED — RECOVERY CERTIFIED WITH DECLARED SESSION RESET** | full document XML byte-identical; history list and cursor identical (Redo included); no reappearance; declared resets only: live instances, Parallel/Perpendicular memory, IsoABorder link, selection, runtime layer of non-drawable numbers and lists (not serialized) |
| **SERIOUS RECOVERY FAILURE — STATE NOT CERTIFIED** | any certificate mismatch, any exception from the recovery, a missing or unidentified paste store; no further automatic mutation; distinct serious message; document marked modified |

A certificate is the conjunction of: full document XML equality, root `id`,
durable identity map, construction fingerprint, macro list, history list and
cursor (file identities), saved flag, current file, file bytes and time. XML
equality alone is never sufficient (it is blind to history and session).

## 3. Source-level facts (inherited code, current base)

| Id | Fact | Source |
|---|---|---|
| `RG-S1` | every Desktop undo store runs `doStoreUndoInfo` on a new thread, adds the state at the cursor and then calls `pruneStateList()`, which deletes every state after the cursor and its file | `desktop/main/undo/UndoManagerD.java:58-101`; `common/main/undo/UndoManager.java:356-388` |
| `RG-S2` | Desktop undo states never compare equal (`FileAppState.equalsTo` returns `false`) and expose no XML (`getXml` returns `null`); every store adds an entry | `desktop/main/undo/FileAppState.java` |
| `RG-S3` | the identity-bearing paste calls `storeAddGeo`, which performs one full asynchronous store; a paste without identity records stores nothing | `desktop/util/CopyPasteD.java:552-558`; `UndoManager.java:502-514` |
| `RG-S4` | `undo`, `redo`, `restoreCurrentUndoInfo`, `getCurrentUndoInfo`, `doStoreUndoInfo` and `commitUndoBaseline` are synchronized on the manager; `pruneStateList` is not; `storeUndoInfo` takes the monitor briefly to read the history generation | `UndoManager.java`, `UndoManagerD.java` |
| `RG-S5` | the store-completion listener has no argument; concurrent store threads acquire the monitor in an order the source does not define; the only cancellation (history generation) is advanced solely by a full baseline replacement | `UndoInfoStoredListener`; `UndoManagerD.java:63-85,125-139` |
| `RG-S6` | `MyXMLio.processXMLString` resets the document UUID (`resetUniqueId`) for every non-macro XML; the handler restores it only when the XML carries `<geogebra id>`; the clipboard wrapper passes no id, so **every successful paste — Insert File and ordinary Ctrl+V — gives the document a new UUID**; the inherited rollback XML and undo states carry the old id | `common/io/MyXMLio.java:203-216`; `MyXMLHandler.java:507-511`; `InternalClipboard.java:627-633` |
| `RG-S7` | `tubeId` is cleared by `resetUniqueId` but has no Desktop reader or writer; it stays `null` in GeoCeDG Desktop | `AppD.java:4312-4315`; `App.java:3871-3877` |
| `RG-S8` | `setUndoActive(true)` re-initializes the whole history (`initUndoInfo`); turning undo off and on to suppress the paste store destroys the history | `App.java:2807-2821` |
| `RG-S9` | `loadUndoInfo` catches a load failure and calls `restoreCurrentUndoInfo`, which moves the cursor back one entry before retrying; a persistent failure walks the cursor backwards until `NoSuchElementException` | `UndoManagerD.java:184-243`; `UndoManager.java:225-233` |
| `RG-S10` | `undoHistoryTo` / `undoHistoryFrom`, `UndoHistory`, the `UndoCommand` copy constructor, `clearUndoInfo`, `UndoCommand.delete` and `FileAppState.getFile` are public inherited API | `UndoManager.java:606-631`; `UndoHistory.java`; `UndoCommand.java:52-102` |

## 4. Probe set-up

Scratch class `F3RecoveryGateProbeTest` (eight methods), run through the
existing F3 Gradle init script in separate JVMs, with isolated preferences and
`ErrorHelper.silent()` evaluation (no modal dialogs). Targets: a saved native
document (Locus V2, Parallel/Perpendicular memory on `ln_{1}`, selection,
layers 3/4, drawing scale, an IsoABorder linked to the export-area session),
then `n` stored user actions and `m` undos. Payloads: the Locus V2 family source
(`DQ21` fixture), a Spline V2 source, an elementary source (`U, V, w, mw`), and
an elementary source with one user macro. Failure injection: a kernel view
proxy that throws on the n-th `add` or `remove` (or on every call), the
manager monitor held by the probe thread (store delay), an open stream on an
undo file (prune delete failure). History certificate: reflection read of
`undoInfoList` and the iterator index (file names) plus a walk through every
reachable position with a construction, identity and UUID fingerprint, then a
later action, Undo and Redo.

## 5. Focus A — document identity

| Scenario | Root UUID | Full XML | Construction / ids | File and save/reopen |
|---|---|---|---|---|
| K1-S without UUID restore (`A-*-K1S-raw`, clean and dirty) | **changed** (the paste) | differs in line 1 (`id`) only | identical | file untouched; the live document carries the new UUID, so a save before any reload would persist it (the probe's later save showed the original UUID only because its intermediate Undo/Redo reloaded an undo state) |
| K1-S with `setUniqueId(captured)` (`A-*-K1S`, plain and macro) | **same** | **byte-identical** | identical; existing macro and its object intact; transferred macro removed | bytes and time unchanged; save + reopen: original UUID, construction and ids equal to a reopen of the original file |
| K1-R (await + undo + prune) | same (undo state carries the old id) | byte-identical | identical | as above |
| inherited rollback inside the paste | same (rollback XML carries the old id) | byte-identical | identical | as above |
| history-isolated reload (`K1-R-H`, §6) | same | byte-identical | identical | as above |
| successful insertion (elementary, Locus V2) and ordinary Ctrl+V | **changed** on every success | — | — | inherited behavior of every paste |

`tubeId` was `null` before and after in every scenario. The captured UUID is
restored through the public `App.setUniqueId`; on Desktop the UUID is read only
by the XML header writer, every later undo state carries the restored value,
and loading another document replaces it as usual. Verdict:
**CLOSED WITH EXPLICIT SCOPE RESTRICTION** for failure paths (restore
mandatory and certified by full-XML equality); the change of document UUID on
a **successful** insertion is inherited, outside `DQ-F3-21`, and recorded as an
open observation for the author (`F3R-DOCUMENT-IDENTITY-SUCCESS`).

## 6. Focus B — pre-existing Undo/Redo history

History shapes: U1 `A→B→C` (current C); U2 `A→B→C`, one Undo (current B, Redo
C); U3 `A→B→C→D`, two Undos (current B, Redo C→D); U4 baseline only.

| Path | Payload | U1 | U2 | U3 | U4 | Reappearance after later action, Undo, Redo |
|---|---|---|---|---|---|---|
| K1-R as proposed (await, undo, prune) | Locus V2, Spline V2 | history identical | **Redo C destroyed** (3 → 2 entries) | **Redo C→D destroyed** (4 → 2) | identical | none |
| K1-S | elementary | identical | identical, Redo intact | identical | identical | none |
| inherited rollback | Locus V2 | identical | identical | identical | identical | none |
| history-isolated reload `K1-R-H` | Locus V2, Spline V2 | identical | **identical, Redo intact** | **identical, Redo C→D intact** | identical | none |
| control (no insertion, two walks) | — | — | identical | — | — | — |

Cause of the K1-R failure: the identity paste's own store (`RG-S1`) prunes the
Redo branch the moment it lands, before any recovery can run; Undo then
returns to B with no Redo. K1-R as proposed therefore **does not satisfy the
safe-rejection contract** whenever Redo exists.

Narrow Desktop-owned correction (`K1-R-H`, demonstrated): before the commit,
`undoHistoryTo` takes the full history aside (files intact) and
`undoHistoryFrom` installs a copy of the prefix up to the cursor, so the paste
store can prune only the prefix; after the failure the operation's own entries
are deleted, the full history is reinstated and `restoreCurrentUndoInfo`
reloads the pre-insertion state — legitimate here because the paste store has
been observed and discarded (§7). On success the live prefix plus the paste
store stays (one undo step) and the held Redo files are deleted, as for any new
action (`U2-success-*`). Detaching the whole history instead is impossible:
the paste reads the current undo state and fails on an empty list
(`NoSuchElementException` at `CopyPasteD.java:462`; first `focusAandD` run).

Fingerprint note: in U4 and in the plain construction comparison, three
non-drawable objects (`a`, `s`, `D`) move from layer 0 to the working layer 4
after any reload; the serialized XML is unchanged and the control shows the
same effect for ordinary Undo/Redo. This is a pre-existing runtime effect of
reloading, not a history difference.

Verdict: **K1-R as proposed — ALTERED — UNACCEPTABLE** for U2/U3;
**K1-R-H — PRESERVED — VERIFIED** for U1–U4 (Locus V2, Spline V2) under the
gates of §7.

## 7. Focus C — asynchronous store synchronization

| Id | Scenario | Result |
|---|---|---|
| A1 | ordinary path, five runs | one store per identity commit (Locus V2 and Spline V2), none for elementary payloads; landed 0.27–0.57 s after scheduling; in 1 of 5 runs before `insertFrom` returned |
| A2 | store blocked (manager monitor held by the committing thread) | bounded await inside the monitor times out → gate yields serious failure without compensation; once released the store lands and prunes Redo |
| A3a | undo issued before the store landed | Undo removed the **user's previous action** (`H2` lost); the late store then appended the insertion; after a later action and Undo the failed insertion **reappeared** (49 objects instead of 14) |
| A3b | immediate `restoreCurrentUndoInfo` before the store landed | deterministic reproduction of `ER-F3R-1`: late store, reappearance (49 instead of 15) |
| A3c | a user store landing between `undo` and `pruneStateList` | prune scope changed by the interleaving (benign in this run); undo and prune must form one monitor-protected step |
| A4 | an unrelated pending store plus the insertion store, twelve runs | observed order always user store first, insertion second; the source does not guarantee it (`RG-S5`); a "first notification" policy would recover before the insertion store landed (A3a hazard) |
| A5 | Redo branch + identity commit | Redo pruned by the paste store before recovery (as in §6) |
| A6 | K1-R, then three actions and six Undo/Redo cycles | maximum object count equals pre + 3; no reappearance |
| I1 | content identification, five runs (prefix live) | the history head after the store equals the live undo XML captured when `insertFrom` returned (5/5); the live document did not change in between |
| Q0, Q1, I2 | pre-commit quiescence gate: head undo file content equals the live undo XML | deterministic (two live serializations equal); true after any landed store; **false while a user store is pending** → the operation can reject before mutation |

Classification:

- **source guarantee:** a recovery step and a store never interleave inside
  one synchronized call; `pruneStateList` alone is not protected;
- **established experimentally:** content identification of the paste store;
  detection of a pending store whose content differs from the head;
- **timing-sensitive:** landing time; landing before or after `insertFrom`
  returns; relative order of concurrent stores;
- **unresolved:** a pending store whose content equals the head (for example a
  redundant store) passes the quiescence gate; if it lands after the recovery
  it prunes the reinstated Redo branch (no geometric or identity effect). A
  GeoCeDG-owned pending-store counter (an `UndoManagerD` subclass installed by
  `AppGeoCeDG.getUndoManager(Construction)`) could close it; not probed.

Verdict: **CLOSED WITH EXPLICIT SCOPE RESTRICTION** for K1-S and K1-R-H with
the gates (quiescence, content-identified bounded await, monitor-atomic swap);
**OPEN — NOT ESTABLISHED** for equal-content pending stores; the "await any
notification" policy of the previous assessment is **unsafe**.

## 8. Focus D — saved and modified state

| Path | D1 clean | D2 modified | File / path | Unintended save |
|---|---|---|---|---|
| K1-S (plain, macro) | true → true | false → false | same path, bytes and time unchanged | none |
| K1-R, inherited rollback, K1-R-H | true → true | false → false | same | none |

The paste marks the document modified in every path (`saved=false` after
commit; the identity store also calls `setUnsaved` on its thread). The
orchestration restores the captured flag with `setSaved` / `setUnsaved` after
the awaited store; the flag was unchanged one second later. A later user Undo
marks the document modified (inherited, ordinary). Save and reopen after
recovery reproduce the original document. Verdict: **CLOSED WITH EXPLICIT
SCOPE RESTRICTION** — correct only after the paste store is awaited; never by
saving.

## 9. Focus E — failure during recovery

| Id | Injection | Observed | Classification |
|---|---|---|---|
| E1 | K1-S removal: kernel view throws on the second `remove` | exception; 2 of 4 inserted objects remain; certificate mismatch | detectable → second line: `restoreCurrentUndoInfo` (no paste store exists, the head is the pre-insertion state) restored byte-identical XML, original UUID, history and Redo, no reappearance; session reset → **CERTIFIED WITH DECLARED SESSION RESET** |
| E2 | K1-R undo: view throws on the first `add` while the undo state loads | inherited code logs and retries; Undo returns normally; XML identical | silent at the call site; only the certificate can confirm |
| E2b | K1-R undo: view throws on every `add` | the inherited retry steps the cursor backwards (`RG-S9`) to 0 and throws `NoSuchElementException`; document 13 objects instead of 15; history cursor invalid | **SERIOUS — STATE NOT CERTIFIED**; history unusable |
| E3 | prune: undo file held open | deletion fails (warning), list and cursor correct, no reappearance; temporary file leaked until exit | certified; leak only |
| E4 | restoration of UUID and saved flag | plain field setters; no failure mode exists to inject | not injectable (reported, not fabricated) |
| E5 | recovery that silently did nothing | every certificate field differs | detectable → serious |
| `DQ21-Q5` (earlier) | inherited rollback restore fails | `IllegalStateException` "Clipboard identity rollback could not restore the construction" | serious |

Containment (design question only, not implemented): after a serious failure
the operation stops mutating, shows the serious message, marks the document
modified and refuses further insertions in that window. Because E2b can leave
the history cursor invalid, a restricted state should also disable Undo and
Redo until the document is saved under a new name or reopened; re-enabling
Undo resets the history (`RG-S8`), which is acceptable only as an explicit,
announced step. Verdict: **F3R-RECOVERY-FAILURE remains OPEN** — detection is
established (exceptions and certificate), containment is designed but not
established.

## 10. Cross-cutting checks

- no partial accepted construction in any certified path; no degraded
  algorithm; no identity reassignment of target objects; rejected objects never
  reappear in any certified path (24 history runs, A6);
- K1-S keeps live instances, Parallel/Perpendicular memory and the IsoABorder
  export link (now **verified**, previously expected); reload paths reset them;
- window copy buffer and provenance unchanged in every path;
- transferred macros removed, the target's existing macro and its object kept
  (`A-*-macro-K1S`).

## 11. Limitations

- Pending stores were delayed by holding the manager monitor; this blocks
  `storeUndoInfo` too, so a store could be delayed only around a whole commit,
  not at an arbitrary point.
- Store order with concurrent threads was observed, not controlled.
- The equal-content pending store case and the pending-store counter were
  analysed, not probed.
- The orchestration itself does not exist; its verification step could not be
  fault-injected (E4, E5 are design properties).
- The Locus V2 fixture's IsoABorder line uses the label `Pi` (π) and does not
  evaluate; IsoABorder was covered by the target's own linked sheet.

## 12. Probe record

| Output | Content | SHA-256 |
|---|---|---|
| `f3-probe.gate-identity.txt` | Focus A, D | `d3b5abf90f2ec7989d76df2ad1db1808892c90477256aeac33f8a801cb851ed2` |
| `f3-probe.gate-history.txt` | Focus B (U1–U4 × five paths, control) | `6d1a4112af771b0c2dc3e8d148d26b2ae445cc993009bf6ac9b1f64aa932cac4` |
| `f3-probe.gate-history-success.txt` | K1-R-H success path | `c26b449a4ba8bea70cc63abbceb869ff065fc50fee910e7ab1eb5097fc2ba974` |
| `f3-probe.gate-async.txt` | Focus C A1–A6 | `54bef7f3ceb0b3e149b28bfb3f6b6ddd4293e652e6d65550f7407462a74ee1cf` |
| `f3-probe.gate-quiescence.txt` | Q0–Q3 | `60dd8524b6af8471aa6e17f30997f52c3bf5c88827d2523d055b9eca76010790` |
| `f3-probe.gate-identification.txt` | I1, I2 | `1f645daadff9b060e72dcdcaa8a26aa05f97d4a602086222a03df4a96b31b31f` |
| `f3-probe.gate-failure.txt` | Focus E | `9f6b8e59d0c7932424f98bb620c3bed0e3ffab1e97eb24802fbbee7cf94c6c1e` |
| probe source | `F3RecoveryGateProbeTest.java` (1267 lines) | `0325c938a430cd1f2de9dadafc8334b1141b930b1751354541a252268fb147e8` |

Runs (each `:desktop:desktop:test --tests …F3RecoveryGateProbeTest.<method>`
with the scratch init script, exit 0): `focusAandD` (twice; the first run used
the whole-history detach and recorded its `NoSuchElementException`, kept only
as a session extract because the second run, with the prefix variant and the
reopen comparison, rewrote the output file), `focusB` (twice; the second after correcting the
fixture label `ln_{1}`), `focusBsuccessPath`, `focusC`, `focusCquiescence`,
`focusCidentification`, `focusE` (twice; the second adding E1 fallback and
E2b). Clean-up: `:desktop:desktop:compileTestJava` without the init script
(exit 0); 18 probe classes, stash copies and the probe JUnit XML removed from
`source/desktop/desktop/build`; `git status` clean.

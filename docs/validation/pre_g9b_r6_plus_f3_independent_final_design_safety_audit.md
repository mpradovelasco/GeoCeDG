# GeoCeDG F3 — Independent Final Design Safety Audit

- Task: `PRE-G9B-R6-plus-F3-INDEPENDENT-FINAL-DESIGN-AUDIT`
- Date: 2026-10-12
- Role: independent reviewer. The previous agent's "READY FOR AUTHOR DISPOSITION" was audited as a hypothesis.
- Location: this report is in the session scratchpad, outside the tracked tree. No repository file was modified.
- `selfApproved = false`

---

## 1. Executive conclusion

The proposed F3 recovery architecture holds up technically. It uses a GeoCeDG-owned `UndoManagerD` subclass installed through the `AppGeoCeDG` factory, moves the original command objects out of the history and back without copying them, uses the existing generation barrier, and recovers with K1-S or K1-R-H. All of it can be built without shared or inherited code changes, and Classic is unaffected. The `ER-F3R-3` diagnosis is correct and independently confirmed: the copy constructor drops `labels` and `stitchToNext`. Moving the original commands is the right fix.

I found **no blocker**. Two parts of the documents, however, specify something that is incorrect as written. Approving the current text would bind the author to those errors:

1. **"Last verified state" is wrongly identified (serious-failure containment).** The design names the barrier-baseline *undo archive* as the state the user can save to a new file. That archive holds only the undo XML. It lacks macros, images, library JavaScript, construction defaults, GUI/perspective XML and the document hidden-layer metadata. It is not a faithful document. The documents also have the barrier baseline deleted in a `finally` block that runs before the isolation dialog would need it.
2. **The pending-store tracker is specified only for the success and discard paths.** In the inherited store, an exception raised while a *stale* store creates its temp file still reaches `onStoreUndo()`. A tracker that counts "completion in `onStoreUndo`" and resets in `commitUndoBaseline` can therefore under-count. The gate could then pass while a real store is still pending.

Both have small, Desktop-only corrections. Neither changes any other F3 decision.

**Verdict: GO WITH CONDITIONS — READY FOR CONDITIONAL AUTHOR DISPOSITION.**

---

## 2. Evidence and source revisions

| Check | Result |
|---|---|
| HEAD / tree / parent | `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3` / `749005c525af0d54724fa9acc1608bde2420253a` / `6e41bf4426b9bdcb137f42e783038b02827ff3bd` — exact |
| `main` / `origin/main` | `e70718fec3612814144cd405d46f6c08864ceadd`; ancestor of HEAD (verified with `git merge-base --is-ancestor`) |
| Worktree | clean at start and at end |
| Delta `e70718fe..HEAD` | 42 files, all under `docs/`, `geocedg/validation/` and the one prepared prompt `.github/prompts/tasks/pre-g9b-r6-plus-f3-file-insert.prompt.md`; **no `source/`, `apps/`, `tools/`, `geocedg/specs/`, `docs/adr/` change** — no F3 production change |
| STATIC receipt | `artifacts/agent/pre-g9b-r6-plus-f3-undo-history-semantic-integrity-static/verification-result.json`: `run_id = verification-d63c246c63ef4e4781b8c4b579993361`, `profile = STATIC`, `run_state = COMPLETED`, `candidate_commit = e479b76d…`, `candidate_tree = 749005c5…`, `acceptance_verdict = ACCEPTED`, `coverage_verdict = COMPLETE` |
| Roadmap | F3 `PLANNED — AUTHOR APPROVED`, implementation not authorized; candidate chain recorded as documentary, pending final author disposition |
| Primary documents read in full | integrity report, final option assessment, risk update, final decision sheet v5.0, recovery design addendum, JSON mirror; also the corrected option assessment and the design successor candidate, because the final procedure keeps their steps 1–3 "unchanged"; relevant sections of the corrective feasibility report and of the prepared canonical prompt |

Source inspected independently (Pass 1) at the candidate tree:

- `source/shared/common/.../main/undo/UndoManager.java`, `UndoCommand.java`, `UndoHistory.java`, `UndoCommandBuilder.java`
- `source/desktop/desktop/.../main/undo/UndoManagerD.java`, `FileAppState.java`
- `source/shared/common/.../kernel/Construction.java` (undo-manager lifecycle)
- `source/shared/common/.../io/MyXMLio.java`, `source/shared/common-jre/.../io/MyXMLioJre.java` (undo archive vs. native archive)
- `source/desktop/desktop/.../main/AppD.java` (factory, native-open transaction, save prompts), `AppGeoCeDG.java`, `GeoGebra.java` (startup thread)
- Save-prompt call sites: `GuiManagerD.java`, `LoadFileListener.java`, `FileDropTargetListener.java`, `GlobalKeyDispatcherD.java`
- `geocedg/specs/operations/verification-levels.md` §12.8

**Probes:** none were run. Each material finding below follows from the control flow or from the serialized content, with no timing involved, so a probe would not change the classification. The repository is unchanged.

---

## 3. Undo-manager subclass assessment (Audit A)

| Question | Finding | Source |
|---|---|---|
| A1 Installable without shared change | **Yes.** `AppD.getUndoManager(Construction)` is a public, non-final factory returning `UndoManagerD`. `AppGeoCeDG` (a `final` class extending `App3D` → `AppD`) can override it with a covariant return type. No other app in the GeoCeDG hierarchy overrides it. | `AppD.java:4383-4385`; `AppGeoCeDG.java:100` |
| A2 Used consistently | **Yes, for every construction of an `AppGeoCeDG`.** `Construction.ensureUndoManagerExists()` calls the app factory once and lazily. The undo manager survives Open and New, because the construction object is kept. Every existing `commitUndoBaseline` caller — native open in `AppD.loadNativeArchive` and `AppGeoCeDG.applyNewDocumentUnitDefaults` — goes through the same instance, so the tracker reset applies to them. | `Construction.java:4291-4300`; `AppD.java:3298-3321`; `AppGeoCeDG.java:229-235` |
| A3 Lazy-initialization hazard | **Implementation gate.** The factory may run during the super-constructor, before `AppGeoCeDG` fields are initialized. The override must be stateless: it may only construct the manager from `cons`. | as above |
| A4 Macro constructions, reloads | No special treatment needed. A macro construction would also receive the subclass, harmlessly (bookkeeping only). Reloads do not replace the manager. | — |
| A5 Desktop behavior change | Bookkeeping-only overrides do not change ordinary Undo. If condition C2 is met by having GeoCeDG own the store path (instead of detecting exits), every GeoCeDG store would run GeoCeDG code. That is still Desktop-only, but it must be declared. | — |
| A6 Public contract preserved | **Yes**, through subclassing. Every member detach/attach needs is accessible (§4). | — |
| A7 Classic unaffected | **Yes.** `AppD`'s factory is unchanged, and Classic does not instantiate `AppGeoCeDG`. | — |

**Classification of the architecture:** technically sound and minimally invasive. It is correctly described as U0 (no inherited source change) and Desktop product layer. It does affect every GeoCeDG document's undo, which the candidate already states.

**Coverage note.** The existing suites that exercise the subclass are those that instantiate `AppGeoCeDG` (for example through `G9U1TestApp.create()`, `G9U1TestApp.java:148-157`). In `desktop/src/test`, 22 files instantiate `AppGeoCeDG`, while 36 files construct `AppD`, `AppDNoGui` or `App3D` directly and will **not** exercise the subclass. The frozen plan should name the `AppGeoCeDG`-based undo, lifecycle and identity suites explicitly.

---

## 4. Undo history metadata and ownership (Audit B)

### B1 History movement — implementable as proposed

- `UndoCommand(UndoCommand)` copies `appState`, `action`, `args`, `slideID`, `undoArgs` and `undoAction`. It does **not** copy `labels` or `stitchToNext` (`UndoCommand.java:31-32, 52-59`). `undoHistoryTo` puts these copies into the saved history (`UndoManager.java:606-614`). `labels` drives `removeActionsWithLabel` (`:667-683`), and `stitchToNext` drives grouped `undo`/`redo` (`:157-185`). `ER-F3R-3` is therefore confirmed independently, at source level.
- **Detach** is possible from a subclass. `undoInfoList` is `protected` (`:60`), the cursor can be read as `getHistorySize() + 1` (`:467-469`), and `clearUndoInfo()` is public and `synchronized` (`:348-351`).
- **Attach** must go through `undoHistoryFrom(Map)` (`:620-631`), because `iterator` is `private` (`:62`) and this is the only route that sets the cursor. It installs the given objects as they are. Two facts need care:
  - `undoHistoryFrom` is **not synchronized**, so the subclass must call it inside `synchronized (this)`.
  - The map key is `app.getConfig().getSubAppCode()`, which is `@CheckForNull` (`AbstractAppConfig.java:61`). A null-tolerant map (`HashMap`) is required; `Map.of` would throw.
- **Command identity, order, cursor, Redo branch, groups and labels** are preserved by construction, because the same objects return to the list. **Checkpoint contents** are preserved because each `FileAppState` stays with its owner.
- **Semantic identities and dependencies** live in the construction, not in the history, and are covered by the certificate (§6).

### B2 Snapshot ownership

- Detach/attach transfers ownership and never shares it. Pruning only deletes what is in the live list, and the held commands are not in it.
- **Defect — barrier-baseline lifetime.** The barrier baseline has two incompatible roles:
  - The final assessment's `ALWAYS` step deletes it in `finally`.
  - Step 7 and report §6 D2 use it as the "last verified state" that isolation offers to save.

  Finding G1 covers this.
- **Recovery-source capture (finding G4).** Report `HS-9` says `getCurrentUndoInfo()` "after a barrier … is the barrier baseline". That is true only until the first post-barrier store lands. For identity payloads the paste's own store lands on top of the baseline. After that, `getCurrentUndoInfo()` returns the *post-insertion* snapshot (`UndoManager.java:215-219`). Reloading it would bring back the rejected insertion — the `ER-F3R-1` class of failure. The baseline `AppState` must therefore be captured **by reference inside the barrier block**.

### B3 Mixed histories

I found no history type that is incompatible with moving the original commands:

- **Action heads.** Undoing the orchestration checkpoint resolves through `getCheckpoint` plus replay of the retained prefix actions (`UndoCommand.java:153-174`; `UndoManager.java:87-106, 328-343`). This works because the order of the prefix is unchanged.
- **Identity payloads.** Locus V2 and Spline V2 always produce snapshots (`storeAddGeo` falls back to `storeUndoInfo` for identity participants, `UndoManager.java:502-507`).
- **Stitched groups.** These are kept as-is. Whether the GeoCeDG UI can produce them (`HS-5`) is not established, and the design correctly treats them as admissible.
- **Pending properties flag.** `commitUndoBaseline` clears `storeUndoInfoNeededForProperties` (`UndoManagerD.java:127`). I checked this: neither the Desktop nor the common source calls `setPropertiesOccurred()` (only the `App` delegator exists), so it has no effect.

### B4 Exceptions

| Point of failure | Assessment |
|---|---|
| Before detach (gate, preflight) | Nothing has changed; rejection before mutation is correct. |
| During detach | Copying references and calling `clearUndoInfo()` cannot fail meaningfully under the monitor. The held list must be built **before** `clearUndoInfo()`. |
| After detach, before barrier commit (`prepareUndoBaseline` throws `IOException`) | `prepareUndoBaseline` runs on the caller thread and throws before any history change (`UndoManagerD.java:110-116`). The design must attach and reject. The `finally` covers this. |
| During restoration (guarded reload) | The design uses `readZipFromMemory` without the inherited recursive fallback (`CF-S4`), with one retry, then the serious path. Sound. |
| During cleanup / `attach` itself | **Gate G7.** `undoHistoryFrom` installs the list and *then* calls `updateUndoActions()` → `app.updateActions()`. An exception there leaves the history correctly restored, but if it propagates out of the `finally` it **masks the original failure**. Attach must mirror `commitUndoBaseline`'s best-effort refresh (`UndoManagerD.java:131-138`): install first, log refresh failures, never throw from `finally`. |

**Verdict on history semantics:** the design preserves the full semantic history, not only its length, subject to gates G4 and G7.

---

## 5. Asynchronous synchronization (Audit C)

### Source facts

- `storeUndoInfo(StringBuilder)` reads the generation under the monitor and starts an anonymous thread (`UndoManagerD.java:57-70`). The XML is captured on the caller thread, before scheduling (`UndoManager.java:284-287`).
- `doStoreUndoInfo` is `synchronized` (and package-private). In order, it:
  1. **creates the `FileAppState` (`:82`)**;
  2. checks the generation, and on a mismatch deletes the file and **returns without calling `onStoreUndo()` (`:83-86`)**;
  3. on success, adds the command, prunes and notifies;
  4. on `Exception | OutOfMemoryError`, logs **and falls through to `onStoreUndo()` (`:95-100`)**;
  5. on any other `Error`, ends the thread without calling `onStoreUndo()`.
- `onStoreUndo()` is also called synchronously by `commitUndoBaseline` (`:133`) and by `storeAndNotifyUnsaved` (`UndoManager.java:457-462`), which serves every action store.
- The generation is advanced **only** by `commitUndoBaseline` (`CF-S1` confirmed).
- `invokeAndWait` appears only at startup (`GeoGebra.java:133`), and GeoCeDG startup runs on the EDT (`GeoGebra.java:66-70, 109-114`). The store thread runs `onStoreUndo()` while holding the manager monitor, but it never waits for the EDT, so there is no EDT↔monitor deadlock cycle at source level (`HS-8` confirmed).

### C1 Pending-store accounting — specified incompletely (finding G2)

The addendum defines the tracker as "count in `storeUndoInfo`, completion in `onStoreUndo` on store threads, reset in `commitUndoBaseline`". The report says store threads are recognised by "exclusion of known caller threads". Tested against every exit path:

| Exit path | `onStoreUndo` called? | Effect on the proposed tracker |
|---|---|---|
| Current-generation success | yes | correct decrement |
| Stale (generation mismatch) | **no** | correct, because the count was already reset |
| Current-generation `IOException` / `OutOfMemoryError` | yes | correct decrement (an undo point is lost, as in the inherited behavior) |
| **Stale + `IOException` / `OutOfMemoryError` in `new FileAppState`** (temp file created *before* the generation check) | **yes** | **spurious decrement after the reset → under-count** |
| Other `Error` | no | count stuck high: fail-closed, but insertion becomes unavailable until the next baseline commit |

**Counterexample (source-level, not timing-only):**

1. Store S1 is scheduled at generation g0.
2. A native Open or New commits a baseline: the generation becomes g1 and the count is reset to 0.
3. The user edits, scheduling S2 at g1: count 1.
4. S1 runs. Its temp-file creation fails transiently, so `onStoreUndo` runs and the count drops to 0.
5. File → Insert: the gate sees no pending store. The barrier advances to g2 and **cancels S2**.

Result: the user's last change gets no undo point of its own. On success, it is merged into the insertion's Undo step. On failure, the restored history head lags the restored construction.

This breaks the proposed normative sentence "An insertion starts only when no undo state is still being recorded". It does **not** resurrect rejected geometry and does not lose Redo. The trigger, a transient temp-file I/O failure, is rare.

**Smallest correction (U0):** make accounting fail-closed over every exit path:

- Recognise the synchronous callers (`storeAndNotifyUnsaved`, `commitUndoBaseline`) structurally, with a re-entrancy marker in the subclass's overrides, instead of by excluding threads.
- On a store-thread `onStoreUndo` whose history head object is **unchanged** (the exception exit), do not decrement and mark the tracker degraded. The gate then rejects.

Alternatively, GeoCeDG can own the asynchronous store body. Everything needed is accessible from a subclass: `maybeStoreUndoCommand` (final protected), `pruneStateList`, `notifyUnsaved` and `onStoreUndo`. With its own per-store token, the decrement can happen in a `finally` only for current tokens. This is a larger behavioral footprint.

**Required test:** a stale-store temp-file failure injected while a current store is pending, plus the `Error` exit.

### C2 Generation barrier

- Stores scheduled **before** the barrier: cancelled whenever they land. This is a source guarantee and is confirmed.
- Stores scheduled **while** the barrier holds the monitor: they block in `storeUndoInfo`'s `synchronized (this)` and receive the new generation. They are therefore operation-generation stores.
- Stores scheduled **after the barrier and during the operation** (the paste's own store, or anything an EDT callback schedules): **not** cancelled by any later step. The design has no closing barrier, and `attach` does not advance the generation.

**Finding G3.** A store of the operation's generation that lands *after* the originals are re-attached would:

- add a checkpoint whose XML was captured at scheduling time — possibly the rejected geometry;
- prune the original Redo branch (`UndoManager.java:356-365`);
- call `notifyUnsaved`, flipping the saved flag.

The design guards against this only by *detection*: "own-store identification", bounded wait, "unexpected store → SERIOUS", checked before attach. I found no concrete GeoCeDG source of a store deferred until after the orchestration returns; a grep of `org.geocedg` store call sites near `invokeLater`/`Timer`/`Thread` found none. The race is therefore not demonstrated in practice, but nothing at source level excludes it after attach.

- In the SERIOUS path, a late paste store would corrupt the attached history *during* isolation. Every isolation exit discards the session history, so this is contained only if isolation never offers to resume Undo.

**Smallest correction:** in the same monitor block as `attach`, require `pending() == 0` and an unchanged temporary list. Alternatively, apply a closing barrier inside that one block: commit a throw-away baseline, then attach, then delete the throw-away. Either way, re-check after the EDT task finishes, as part of certification.

### C3 Atomicity and locks

- The gate → barrier → commit → identify → verify → attach sequence runs on the EDT in one task.
- Gate, detach, commit and preflight belong in one `synchronized` block, together with capturing the recovery source by reference (G4).
- Attach and its pending re-check belong in one `synchronized` block (G3).
- `synchronized` methods are re-entrant: `commitUndoBaseline` called inside the subclass's monitor block is safe.
- **Inherited race (observation N3):** `storeAndNotifyUnsaved` is unsynchronized (`UndoManager.java:457`). It can run on the EDT while a store thread modifies the list. This was not introduced by F3. If the subclass synchronizes it, that becomes a declared change to every GeoCeDG action store.

### C4 Failure handling

With the C1 correction, an inconsistent tracker rejects the insertion. Without it, the tracker can certify quiescence that does not exist.

**Verdict on asynchronous safety:** the barrier and the threading model are sound. The **tracker contract needs condition C2 and the attach-side check C3** before it can be called fail-closed. No race was found that can resurrect rejected geometry or lose Redo on the *certified* paths once G3 and G4 are honoured.

---

## 6. Recovery and containment (Audit D)

### D1 Certificate

The corrected assessment's gate 5, plus the prompt's gate 4, cover:

- full XML (`getFullXML`, which includes construction, units, GUI and hidden layers — `MyXMLio.java:310-327`);
- root UUID;
- the identity registry;
- macros;
- history list, cursor, and per-entry labels and grouping;
- saved flag and file association.

Runtime state is compared separately. Nothing is inferred from labels or coordinates: identities are compared through the registry, and history entries by object identity.

Weaknesses:

- **N4:** macros should be compared by **definition digest**, not by name set.
- Semantic intersection tokens are covered only insofar as they are serialized into the construction XML. This holds for Locus V2 per the earlier evidence and is not re-established here.

### D2 Serious failure

- Bounded: one retry of a guarded reload that has no inherited recursive fallback. The inherited `loadUndoInfo` fallback (`UndoManagerD.java:236-239`) is avoided, which is correct.
- The SERIOUS path re-attaches the originals unchanged. The G3 caveat (late operation-generation store) applies.

### D3 Modal isolation — the contract is sufficient; the mechanism is not yet established

The contract is: only three exits; the unverified state is never edited, saved or exported as if verified; scripts and animation stopped; Undo/Redo suspended. That is an adequate *contract*. Effective containment depends on implementation details the documents do not yet state.

**G5 — inherited save prompts.** Every ordinary lifecycle route asks "save changes?" when the document is unsaved, and the serious path marks it modified:

- `AppD.java:2972, 3001`
- `GuiManagerD.java:1929`
- `LoadFileListener.java:52`
- `FileDropTargetListener.java:125`
- `GlobalKeyDispatcherD.java:306`

If the "reopen" or "close" exits are implemented through these routes, they would offer to **save the uncertified state over the user's file**. Both exits must bypass the prompt.

**Re-enabling Undo before reopen.** `AppD.loadNativeArchive` commits a fresh baseline only when `kernel.isUndoActive()` (`AppD.java:3298-3299`), and `Kernel.setUndoActive(true)` does not reinitialize the history (`CF-S5`). Undo must therefore be re-enabled *before* the reopen. Otherwise the old session history stays live against the reopened document.

**Other mutation routes to test:**

| Route | Status |
|---|---|
| GUI tools, keyboard shortcuts | blocked by modality |
| Undo/Redo | suspended |
| Update scripts | blocked |
| Global JavaScript listeners, Swing timers (animation steps still fire while a modal dialog pumps the EDT) | require explicit stop |
| File drop onto the blocked frame (delivery of drag-and-drop to a modally blocked window is **not established**) | must be tested |
| Clipboard, Insert, Save/Save As, export | reachable only through blocked menus; still assert in tests |
| Programmatic API | Desktop has no external API server by default (`startHttpServer` is a separate launch mode, `GeoGebra.java:87-89`) |

### D4 Last verified state — incorrectly identified (finding G1)

- The design names "the barrier baseline archive, which passes the native preflight". That archive is a `FileAppState` built by `MyXMLioJre.writeZipped` with a **single `geogebra.xml` entry** containing the **undo XML** (`FileAppState.java:41-51`; `MyXMLioJre.java:687-698`; `MyXMLio.getUndoXML`, `MyXMLio.java:142-171`).
- A native save (`writeGeoGebraFile`, `MyXMLioJre.java:331-397`) additionally writes:
  - construction and macro images;
  - **`geogebra_macro.xml`**;
  - `geogebra_javascript.js`;
  - `geogebra_defaults2d.xml` / `geogebra_defaults3d.xml`;
  - the **full** XML with GUI settings.
- The GeoCeDG hidden-layer metadata is "written only into the full document, never into undo" (`MyXMLio.java:321-324`).
- Saving the undo archive as a document would therefore lose user tools (objects built with them could fail to load), images, the global script, defaults, perspective and hidden layers. Passing `DocumentArchivePreflight` shows the container can be loaded, not that the document is faithful.
- In addition, the `ALWAYS` step deletes the barrier baseline in `finally`, before the isolation dialog would need it.

**Smallest correction (U0, already precedented):** at the barrier, also capture a **complete native archive** in memory. `AppD.captureNativeDocumentLoadState` already does this with `getXMLio().writeGeoGebraFile(archive, false)` (`AppD.java:3374-3381`). Preflight it, record its provenance (barrier time, UUID, source file, saved flag) and give the **isolation controller** ownership of it until the user's exit completes. The undo archive stays the *reload* source only.

- "Save to a new file" must never default to or overwrite the current file, and it must not move the live session's file association to the new file while the session remains uncertified.
- Whether the saved copy keeps the document UUID is a judgment call (question 2 in §10). The existing Save As keeps it (`CF-S7`).

### D5 Implementation gates — before functional tests vs. before technical approval

- **Before functional tests can run:**
  - the subclass and factory override (A3);
  - detach/attach with the safe attach (G7);
  - the fail-closed tracker (G2);
  - the barrier with the recovery source captured by reference (G4);
  - the attach-side quiescence check (G3);
  - the complete-archive capture (G1);
  - an isolation dialog whose exits bypass the save prompts (G5).
- **Before technical approval:**
  - EDT runs of A-Q1/A-Q3/A-Q4 and B-F4;
  - H2–H5 and O1–O4 using the factory-installed subclass, not reflection;
  - P1–P4 **plus** the exit-path injections (G2);
  - a late operation-generation store after attach (G3);
  - an `ER-F3R-1` regression with an identity payload, after the paste store lands (G4);
  - isolation exits checked: no prompt, no overwrite, Undo re-enabled before reopen, and the saved verified copy reopened with macros, images and hidden layers intact (G1, G5);
  - the `AppGeoCeDG`-based undo, lifecycle and identity suites as integration coverage.

**Verdict on recovery and containment:** K1-S and K1-R-H are adequately specified. Serious-failure containment is **sufficiently specified as a contract** but **mis-specified in its source of the verified state**. Correction G1 is required before the author approves wording that names "the barrier baseline archive". Effective containment is **NOT ESTABLISHED** until it is implemented, and the candidate says so honestly.

---

## 7. F3 contract consistency (Audit E)

| Check | Result |
|---|---|
| DQ coverage | Q1–Q7 cover `DQ-F3-1`…`-24` exactly once (sheet table verified). |
| Shared-change claims | The claim "every recommendation is U0" holds: the subclass, the attach mechanics and the complete-archive capture all use accessible inherited API. The C2 correction can also be done in U0. |
| Type supported without its closure | None found. Identity payloads with macros are rejected (`DQ-F3-24`), and Locus V2/Spline V2 are admitted only when the target allows Locus V2 and no macros are needed. |
| Success declared atomic with a mis-bound macro | Not found: strict verification includes macro bindings; M1 rejects macros with differing definitions. |
| Certificate despite a changed identity | Not found: UUID and identity registry are both in the certificate. |
| Session reset not communicated | Communicated for K1-R-H ("the user is told so"). |
| **Wording drift (N1)** | The corrected wording's sentence "The selection is always cleared and the Move tool activated" is **missing** from the final normative wording §4. The decision sheet Q1 still lists it as a restriction. The approved text should restate it so K1-S does not imply that the selection is kept. |
| **Contradiction (part of G1)** | The final assessment §2 `ALWAYS: … delete the barrier baseline` conflicts with step 7 / report §6 D2, which use it as the saveable verified state. The same text appears in the prepared prompt's gate 4 (`.github/prompts/tasks/pre-g9b-r6-plus-f3-file-insert.prompt.md:216-217`). |
| **Overstated evidence (N2)** | Report §5 and the JSON say "P1–P4 validated". They cover success and discard only, not the exception exits (G2). The prompt's "bookkeeping-only overrides" stays accurate only if C2 is met by detection rather than by owning the store path. |
| Gate described as verified | No: EDT behaviour, isolation, and the reflection-vs-factory installation are all correctly marked as implementation gates. |
| Risk register | `F3R-UNDO-SNAPSHOT-OWNERSHIP — CLOSED — EVIDENCE SUPPORTED` is premature for the barrier baseline's isolation role (G1). The other deltas are accurate. |
| Settled questions | Not reopened. No contradiction found in R, S, L1, the independent buffer, D1 provenance, T0, UUID preservation or the class. |

**Verdict on consistency:** consistent, except for the G1 contradiction and the N1 wording drift, both of which can be corrected in text.

---

## 8. Verification-class assessment

- `INTEGRATED_PHASE` remains appropriate:
  - no `source/shared/**` or inherited-file change;
  - Classic unaffected;
  - the change is Desktop product layer.
- Under verification-levels §12.8, COMPOSED is added "only when the frozen plan identifies concrete additional integration coverage". The new subclass serves every GeoCeDG document, so that coverage is concrete. The prompt's PHASE + INTEGRATION plan (prompt lines 370-383) is therefore the correct reading.
- The frozen plan should name:
  - the `AppGeoCeDG`-based Desktop undo, document-lifecycle, native open, Save/Save As and identity/persistence suites;
  - Locus V2, Spline V2, F2 session memory, IsoABorder, macro handling and ordinary clipboard;
  - the F3-specific cases.
- No new ADR or reclassification is needed **unless**:
  - the author picks the C3/K2 shared copy-constructor fix (`GLOBAL_IMPACT` review); or
  - an inherited correction is selected under `DQ-F3-19`.
- Owning the store path inside the GeoCeDG subclass (the alternative for C2) stays U0 but widens the Desktop impact. It should be declared in the frozen plan's impact facts, without changing the class.

I am not freezing the class. That is an author act at implementation authorization.

---

## 9. Findings, ranked by severity

### BLOCKERS — 0

### REQUIRED IMPLEMENTATION GATES — 7 (G1 and G2 are also approval conditions)

| Id | Finding | Source | Required correction / evidence | Consequence for decisions |
|---|---|---|---|---|
| **G1** | The "last verified state" is the barrier-baseline undo archive, which holds only the undo XML, with no macros, images, library JavaScript, defaults, GUI or hidden layers. The `ALWAYS` step also deletes it in `finally` while isolation needs it. | `FileAppState.java:41-51`; `MyXMLioJre.java:331-397, 687-698`; `MyXMLio.java:142-171, 321-324`; final assessment §2; report §6 D2; prompt gate 4 | Capture a complete native archive (`writeGeoGebraFile`, as in `AppD.java:3374-3381`) at the barrier; preflight it, record provenance, keep it owned until isolation exits; never overwrite the current file; test by reopening the saved copy with macros, images and hidden layers | **Approval condition on Q1/`DQ-F3-21`** — the wording must not bind the verified state to the undo archive |
| **G2** | The pending-store tracker is not exit-path-complete. A stale store's temp-file exception reaches `onStoreUndo` and under-counts after a reset; other `Error`s leave the count stuck. Store threads are recognised by excluding known caller threads. | `UndoManagerD.java:78-101, 125-139`; `UndoManager.java:457-462`; report §5, §8 | Fail-closed accounting: structural marker for synchronous callers, head-unchanged detection of exception exits (no decrement, degraded → reject), or a GeoCeDG-owned store body with per-store tokens; tests injecting stale-store failure and `Error` | **Approval condition on Q1** — the gate's "no undo state is still being recorded" holds only with this; the author chooses the mechanism (question 1) |
| G3 | Stores of the operation's generation are not cancelled after `attach`; protection is detection-only, before attach. In the SERIOUS path a late paste store can corrupt the attached history during isolation. | `UndoManagerD.java:57-70, 83-86`; final assessment §2; `CF-S2` | `pending()==0` plus an unchanged temporary list checked in the same monitor block as attach, or a closing barrier; re-check during certification; isolation never resumes the session history | Implementation gate under Q1 |
| G4 | The recovery source must be captured by reference inside the barrier. `getCurrentUndoInfo()` returns the post-insertion snapshot once the paste store lands (`HS-9` is imprecise). | `UndoManager.java:215-219` | Capture the `AppState` in the barrier block; `ER-F3R-1` regression with an identity payload after the store lands | Implementation gate under Q1 |
| G5 | Isolation exits must bypass the inherited "save changes?" prompts (the document is marked modified), and Undo must be re-enabled before reopen or no fresh baseline is committed. | `AppD.java:2972, 3001, 3298-3299`; `GuiManagerD.java:1929`; `LoadFileListener.java:52`; `FileDropTargetListener.java:125`; `GlobalKeyDispatcherD.java:306`; `CF-S5` | Tests: close/reopen without prompt or overwrite; Undo re-enabled; file drop, timers and global listeners during isolation | Implementation gate (gate 7) |
| G6 | The factory override must be stateless (it can be called lazily during the super-constructor). Integration coverage must use `AppGeoCeDG`-based suites. | `Construction.java:4291-4300`; `AppD.java:4383-4385`; `G9U1TestApp.java:148-157` | Construction-order test; frozen plan names the suites | Q7 / class plan |
| G7 | `attach` through `undoHistoryFrom` is unsynchronized, uses a nullable map key, and refreshes actions *after* installing the list. An exception thrown from `finally` masks the original failure. | `UndoManager.java:620-631`; `AbstractAppConfig.java:61`; `UndoManagerD.java:131-138` (pattern to mirror) | Synchronize; use a `HashMap`; install first, best-effort refresh; test with an injected `updateActions` failure | Implementation gate 2 |

### NON-BLOCKING OBSERVATIONS — 5

| Id | Observation |
|---|---|
| N1 | The final §4 wording drops "selection always cleared and Move tool activated"; restate it in the approved text. |
| N2 | "P1–P4 validated" is overstated (success and discard only). "Bookkeeping-only overrides" becomes inaccurate if G2 is met by owning the store path. |
| N3 | Inherited unsynchronized `storeAndNotifyUnsaved` (`UndoManager.java:457`) can race store threads. Not introduced by F3; synchronizing it in the subclass would be a declared behavior change. |
| N4 | The certificate should compare macros by definition digest, not by name. |
| N5 | `INTEGRATED_PHASE` holds; the INTEGRATION leg is effectively required and is already planned. A GeoCeDG-owned store path widens the Desktop impact without changing the class. |

### NOT ESTABLISHED — 2

| Id | Item |
|---|---|
| NE1 | EDT behaviour of the gate's bounded wait, the barrier and the guarded reload. Source shows no lock cycle (`HS-8` confirmed), but nothing was run on the EDT; the candidate acknowledges this. Store threads also dispatch `STOREUNDO` events to destination-document listeners while holding the monitor (inherited, `UndoManager.java:546-549`). |
| NE2 | Effective containment against every mutation route: not built. Delivery of drag-and-drop to a modally blocked frame and timer- or listener-driven mutation are open questions. |

---

## 10. Final recommendation

**GO WITH CONDITIONS — READY FOR CONDITIONAL AUTHOR DISPOSITION.**

The design is sound and can be implemented in U0. The conditions below are small, Desktop-only and testable, and none reopens a settled F3 question. If the author approves Q1 / `DQ-F3-21`, the approval should explicitly include:

1. **C1 (G1).** The "last verified state" is a complete native archive, captured and preflighted at the barrier, with recorded provenance. The isolation controller owns it until the user's exit completes. It is never the undo archive, is never deleted while isolation is active, and is never saved over the current file.
2. **C2 (G2).** The pending-store tracker is fail-closed over every exit of the inherited store (success, stale discard, exception before the generation check, `Error`). Synchronous callers are recognised structurally, not by excluding threads. Any accounting anomaly rejects the insertion before mutation.
3. **C3 (G3 + G4).** The recovery source is captured by reference inside the barrier block. Re-attaching the originals happens only with no pending store and an unchanged temporary list, checked in the same monitor block (or behind a closing barrier).
4. **C4 (G5).** The isolation exits never route through an inherited save-changes prompt, and Undo is re-enabled before a reopen so the reopened document gets a fresh baseline.
5. **Wording (N1).** Restore the "selection cleared and Move tool activated" sentence in the approved normative text.

The design documents, the decision sheet and the prepared prompt (gate 4) need the matching textual corrections. That is a separate, author-authorized documentary act; this audit did not modify them.

---

```text
TASK:
PRE-G9B-R6-plus-F3-INDEPENDENT-FINAL-DESIGN-AUDIT

BASE:
e479b76d47c4e62baf9a4cf511ec06268c5b5ab3 / tree 749005c525af0d54724fa9acc1608bde2420253a
(parent 6e41bf44; main e70718fe ancestor; STATIC verification-d63c246c… ACCEPTED / COMPLETE, bound)

AUDIT STATUS:
COMPLETE

UNDO MANAGER ARCHITECTURE:
SOUND — U0, factory-installable, Classic unaffected; stateless-override and
AppGeoCeDG-suite coverage gates (G6)

UNDO HISTORY SEMANTICS:
SOUND — moving the original commands preserves labels, grouping, cursor, Redo
and snapshots; ER-F3R-3 confirmed; gates G4 (recovery-source capture) and G7
(safe attach)

ASYNCHRONOUS SAFETY:
SOUND BARRIER, INCOMPLETE TRACKER CONTRACT — under-count on the stale-store
exception exit (G2, condition); post-attach operation-generation stores need
an attach-side check (G3)

RECOVERY AND CONTAINMENT:
K1-S / K1-R-H ADEQUATE; CONTAINMENT CONTRACT ADEQUATE BUT "LAST VERIFIED
STATE" MIS-SPECIFIED (undo archive is not a faithful document; deleted in
finally) — G1, condition; effective containment NOT ESTABLISHED (NE2)

F3 CONTRACT CONSISTENCY:
CONSISTENT except the G1 contradiction and the N1 wording drift

IMPLEMENTATION CLASS:
INTEGRATED_PHASE (PHASE + INTEGRATION) with AppGeoCeDG-based undo, lifecycle,
open/save, identity, Locus V2, Spline V2, F2, IsoABorder, macro and clipboard
suites named in the frozen plan; no ADR or reclassification unless a shared
or inherited correction is chosen

FINDINGS:
BLOCKERS: 0
REQUIRED IMPLEMENTATION GATES: 7
NON-BLOCKING OBSERVATIONS: 5
NOT ESTABLISHED: 2

FINAL VERDICT:
GO WITH CONDITIONS

AUTHOR DISPOSITION:
NOT PERFORMED

F3 IMPLEMENTATION:
NOT AUTHORIZED

PUBLICATION:
NOT AUTHORIZED

selfApproved = false
```

### Questions that genuinely require author judgment

1. **Tracker mechanism (C2).** Keep "bookkeeping-only" overrides with fail-closed exit detection, which may refuse insertion until the next baseline after a store failure? Or let the GeoCeDG subclass own the asynchronous store body, which gives exact accounting but puts every GeoCeDG undo store through GeoCeDG code?
2. **Saved verified copy (C1).** Should the copy saved from isolation keep the document UUID, so it stays the same lineage as Save As, or receive a new one?
3. **Diagnostic export.** Should the optional, explicitly labelled export of the *uncertified* state be part of the approved contract, or left out?

# PRE-G9B-R6-plus-F3 — independent audit reconciliation report

- Activity: `PRE-G9B-R6-plus-F3-INDEPENDENT-AUDIT-RECONCILIATION` (author
  instruction of 2026-10-12; `DOCUMENTATION_STATUS_ONLY`)
- Status: **RECONCILIATION COMPLETE — PENDING FINAL AUTHOR DISPOSITION**
- Base: `T_F3_UNDO_HISTORY_INTEGRITY` `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3`,
  tree `749005c525af0d54724fa9acc1608bde2420253a`, parent
  `6e41bf4426b9bdcb137f42e783038b02827ff3bd` (STATIC
  `verification-d63c246c63ef4e4781b8c4b579993361` `ACCEPTED / COMPLETE`)
- Independent audit (immutable external review artifact, byte-identical tracked
  copy): [independent final design safety audit](pre_g9b_r6_plus_f3_independent_final_design_safety_audit.md)
- Companions: [recovery design addendum successor](../architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_successor_candidate.md);
  [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_audit_reconciliation.md);
  [Final F3 Author Decision Sheet v6.0](pre_g9b_r6_plus_f3_final_author_decision_sheet_v6.md);
  [reconciled amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals_reconciled.md)
- `selfApproved = false`

The audit is review evidence, not product verification. This report reconciles
it against the source and the recorded evidence; no probe was run and no
product, test, specification or ADR file changed.

## 1. Entry gate (2026-10-12)

| Check | Result |
|---|---|
| HEAD, tree, parent | `e479b76d…`, `749005c5…`, `6e41bf44…` |
| linear chain | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` → `3e24ed3a` → `6c07e01a` → `6e41bf44` → `e479b76d` |
| `main`, `origin/main`, live remote `main` | `e70718fe` (candidates intentionally unpublished) |
| receipt | `verification-d63c246c…` `ACCEPTED / COMPLETE`, `COMPLETED`, bound to `e479b76d`/`749005c5` |
| worktree | clean |
| product delta since `main` (`source/`, `apps/`, `tools/`, `geocedg/specs/`, `docs/adr/`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/`) | none |
| frozen F3 evidence | present and unchanged |
| governance | `verification-levels.md` §12.8 re-read; this activity → STATIC |
| branch | `phase/pre-g9b-r6-plus-f3-independent-audit-reconciliation` (local, from `e479b76d`) |
| result | **PASS** |

## 2. Audit provenance

| Field | Value |
|---|---|
| Task | `PRE-G9B-R6-plus-F3-INDEPENDENT-FINAL-DESIGN-AUDIT` (independent thread), 2026-10-12 |
| Audited candidate | `e479b76d…` / tree `749005c5…` (exact, as recorded in the audit §2) |
| Original location | independent session scratchpad, `geocedg_f3_independent_final_design_safety_audit.md`, 39 483 bytes, 447 lines, modified 2026-10-12T21:37:13+02:00 |
| SHA-256 | `b22629efc7099482f592b98271f707087cccd04e28cada93999221864f5fbd41` (tracked copy byte-identical) |
| Read | complete, all sections |
| Verdict | **GO WITH CONDITIONS** — 0 blockers, 7 required implementation gates (G1, G2 also approval conditions), 5 non-blocking observations, 2 not established; author disposition not performed |

Identifier mapping (the task prompt used its own labels; the audit's govern):

| Task prompt label | Audit identifier |
|---|---|
| Finding A (native recovery snapshot) | G1 |
| Finding B (pending-store accounting) | G2 |
| G1 history reattachment safety | G3 |
| G2 recovery-state capture | G4 |
| G3 save-prompt bypass | G5 |
| G4 factory lifecycle | G6 |
| G5 exception preservation | G7 |

## 3. Source re-verification of the material findings

| Claim | Re-verified | Source |
|---|---|---|
| the undo archive holds one `geogebra.xml` with the undo XML only | yes | `FileAppState.java:41-51`; `MyXMLioJre.java:687-698` |
| document hidden layers are written only into the full document | yes | `MyXMLio.java:321-324` |
| a native archive is written by public `writeGeoGebraFile(OutputStream, boolean)`; `captureNativeDocumentLoadState` is private and uses it | yes | `MyXMLioJre.java:331`; `AppD.java:3374-3381` |
| atomic same-directory publication with `REPLACE_EXISTING` | yes | `AtomicDocumentFileWriter.java:35-56` |
| store exits: temp file created before the generation check; stale → return without `onStoreUndo`; `Exception`/`OutOfMemoryError` → `onStoreUndo`; other `Error` → no `onStoreUndo` | yes | `UndoManagerD.java:78-101` |
| synchronous `onStoreUndo` callers: `commitUndoBaseline`, `storeAndNotifyUnsaved`; synchronous `pruneStateList` callers: `storeAndNotifyUnsaved`, `setPropertiesOccurred` (no caller) | yes | `UndoManagerD.java:133`; `UndoManager.java:416,459-460` |
| save prompts at lifecycle routes | yes | `AppD.java:2972,3001`; `GuiManagerD.java:1929`; `LoadFileListener.java:52`; `FileDropTargetListener.java:125`; `GlobalKeyDispatcherD.java:306` |
| reopen commits a fresh baseline only when undo is active | yes | `AppD.java:3298-3299` |
| `AppGeoCeDG` is `final`, extends `App3D`; factory overridable | yes | `AppGeoCeDG.java:100`; `AppD.java:4383-4385` |

## 4. Findings and dispositions

| Id | Severity | Finding (audit) | Disposition | Where reconciled |
|---|---|---|---|---|
| G1 | gate + approval condition | "last verified state" was the undo archive (incomplete) and was deleted in `finally` | **ACCEPTED** — distinct verified native recovery snapshot, owned until isolation ends (§5) | addendum successor §3; sheet Q1; P7; prompt gates |
| G2 | gate + approval condition | tracker not exit-path complete; under-count after a stale-store exception | **ACCEPTED** — conservative fail-closed accounting C1 (§6), author preference | addendum successor §4; sheet Q1; prompt FR-F3-1 |
| G3 | gate | operation-generation stores after `attach` not cancelled | **ACCEPTED** — attach only with `pending()==0`, not degraded and an unchanged temporary list in the same monitor block, else a closing barrier; re-checked during certification | addendum successor §2 |
| G4 | gate | recovery source must be captured by reference in the barrier block (`HS-9` imprecise) | **ACCEPTED** — `HS-9` qualified; the barrier baseline `AppState` captured by reference; `ER-F3R-1` regression after the paste store lands | addendum successor §2 |
| G5 | gate | isolation exits must bypass inherited save prompts; re-enable Undo before reopen | **ACCEPTED** (§7) | addendum successor §5 |
| G6 | gate | stateless factory override; `AppGeoCeDG`-based suites as coverage | **ACCEPTED** (§9) | addendum successor §1; prompt |
| G7 | gate | attach synchronized, `HashMap` key, install then best-effort refresh; never mask the original failure | **ACCEPTED** — plus suppressed-exception preservation | addendum successor §2 |
| N1 | observation | final wording dropped "selection cleared and Move tool activated" | **ACCEPTED** — restored in the normative text (P7) and in Q1 | P7 |
| N2 | observation | "P1–P4 validated" overstated; "bookkeeping-only" conditional | **ACCEPTED** — corrected: P1–P4 covered success and discard only; C1 keeps bookkeeping-only overrides | risk register; JSON |
| N3 | observation | inherited unsynchronized `storeAndNotifyUnsaved` may race store threads | **REMAINS OBSERVATION** — not introduced by F3; not changed (would be a declared behavior change); the accounting treats it fail-closed (§6) | risk register |
| N4 | observation | certificate should compare macros by definition digest | **ACCEPTED** — certificate field changed to digest set | addendum successor §2 |
| N5 | observation | `INTEGRATED_PHASE` holds; INTEGRATION leg effectively required | **ACCEPTED** (§9) | sheet Q7 |
| NE1 | not established | EDT behavior | **OPEN — IMPLEMENTATION GATE** | prompt FR-F3-5 |
| NE2 | not established | effective containment against every mutation route | **OPEN — IMPLEMENTATION GATE** | prompt FR-F3-4 |

No finding contradicts a settled F3 decision (R, S, L1, buffer, D1, T0, UUID,
class); none requires a shared or kernel change.

## 5. Native recovery snapshot (G1)

Two artifacts with different purposes:

| Artifact | Purpose | Content | Lifetime |
|---|---|---|---|
| undo barrier baseline (`FileAppState`) | reload source for K1-R-H and the K1-S second line | undo XML only | deleted when the operation's history work ends |
| **native recovery snapshot** | source of the verified recovery copy offered by isolation | complete native archive (`writeGeoGebraFile(OutputStream, false)`): full XML with GUI and document hidden layers, `geogebra_macro.xml`, images, library JavaScript, construction defaults; root UUID as live | owned by the operation, then by the isolation controller; released only at a terminal outcome |

Lifecycle:

```text
CAPTURE REQUESTED        inside the barrier block, before any insertion mutation
CAPTURE COMPLETE         byte array in memory (immutable copy)
NATIVE ARCHIVE VALIDATED DocumentArchivePreflight.validate(bytes, product config) true
                         and the archive's <geogebra id> equals the live UUID;
                         a ZIP written without exception is not "verified"
VERIFIED SNAPSHOT HELD   provenance recorded: operation id, capture time, UUID, current
                         file and path, saved flag, generation after the barrier
INSERTION ATTEMPT
SUCCESS                  release (discard bytes)
CERTIFIED RECOVERY       release
UNCERTIFIED FAILURE      transfer ownership to the isolation controller; retain until the
                         user's exit completes; then release
capture/validation fails reject before mutation (S); nothing changed
```

Recovery copy: written only to a user-chosen new path; never the current file;
an existing target is never replaced silently (explicit confirmation for a
different existing file, refusal for the current file); written through
`AtomicDocumentFileWriter` from the held bytes, the published bytes
re-validated by the preflight before the copy is reported as a certified
recovery artifact; the live session's file association is not moved to the
copy. The copy keeps the original UUID (author preference §6.2): it is the
same document lineage, like Save As; two files then share the lineage id, which
is a lineage marker, not a geometric or semantic identity.

## 6. Pending-store accounting — conservative contract C1 (G2)

Ownership: the inherited `UndoManagerD` schedules and executes every
asynchronous store; the GeoCeDG subclass only observes, through overrides that
delegate unchanged.

| Point | Rule |
|---|---|
| registration | `storeUndoInfo(StringBuilder)` override: under the monitor, `pending++` before `super`; if `super` throws (thread creation failure) → `degraded` |
| synchronous callers | `storeAndNotifyUnsaved` and `commitUndoBaseline` overrides set a thread-local re-entrancy marker around `super` (structural, not thread exclusion) |
| success observation | `pruneStateList` override: when called without the marker (only the store body's success path calls it, `UndoManagerD.java:91`), set a thread-local "store reached success" flag on that store thread |
| completion | `onStoreUndo` override, under the monitor: with the marker → synchronous, no accounting; without it and with the success flag → `pending--` (if `pending` was 0 → `degraded`); without the success flag → exception exit (current or stale) → **no decrement, `degraded = true`**; clear thread-locals |
| stale discard | inherited `return` before `onStoreUndo`: no callback, no effect |
| other `Error` | no callback: `pending` stays high (fail-closed) |
| generation / reset | `commitUndoBaseline` override: after `super`, `pending = 0`, `degraded = false`, `epoch++` — every earlier store is cancelled by the generation advance (`CF-S1`) and cannot reach the success path |
| stale callbacks | a stale store can only reach `onStoreUndo` through the exception exit, which never decrements |
| barrier synchronization | gate (`pending()==0 && !degraded`), detach, baseline commit, preflights and snapshot capture in one monitor block; attach with the same check (G3) |
| uncertainty | `degraded` or `pending > 0` after a bounded wait outside the monitor → reject before mutation; message: try again, or reopen the document |
| re-establishment | the next `commitUndoBaseline` (native open, New) re-establishes a certified baseline; F3 never commits a history-discarding baseline to clear an anomaly |

Proof obligation (invariant `pending == 0 ∧ ¬degraded ⇒ no current-generation
store can still mutate the history`): every current-generation store
increments before scheduling; a decrement happens only on the store's own
thread after it has added its command and pruned (the success path); stale
stores cannot reach the success path; every other exit either sets `degraded`
or leaves `pending` high. Assumptions, each tied to source: all asynchronous
stores enter through `storeUndoInfo(StringBuilder)` (`UndoManager.java:278-287`);
the success path calls `pruneStateList` before `onStoreUndo`
(`UndoManagerD.java:89-100`); no other unmarked thread calls `pruneStateList`
(`setPropertiesOccurred` has no caller). False positives (`degraded` after a
stale store's I/O failure, or a stuck count after an `Error`) only deny
insertion. N3 (an unsynchronized action store racing a store thread) cannot
create a false negative because the marker is thread-local.

Alternatives (documented, not selected): **C2** GeoCeDG-owned store body with
per-store tokens (exact, but every GeoCeDG undo store runs GeoCeDG code); **C3**
minimal shared correction (separate authorization, classification review). If
C1 cannot be demonstrated during implementation: STOP (FR-F3-1) and request an
author decision; no automatic switch.

The earlier statement "P1–P4 validated" (history integrity report §5) is
corrected: those probes covered the success and discard paths only (N2).

## 7. Serious-failure isolation (G5, NE2)

Contract: the uncertified drawing is never available for ordinary editing,
saving or export. Exits: (1) save the verified native recovery snapshot to a
new file; (2) reopen the original document from its existing file; (3) close
the document. No export of the uncertified state (author preference §6.3).

| Mutation route | Required guard (implementation) |
|---|---|
| geometric editing, tools, keyboard | modal dialog owned by the main frame |
| Save, Save As, Export, Insert File, Apply Styles, clipboard | GeoCeDG isolation flag consulted by the GeoCeDG action registry / `GuiManagerGeoCeDG` / `AppGeoCeDG.saveGeoGebraFile` (refuses any save of the live state while isolated) |
| update scripts, global JavaScript listeners | `setBlockUpdateScripts(true)`; global-listener dispatch suppressed while isolated (test) |
| animation and timers | animation stopped; timer-driven mutation tested |
| Undo/Redo | `Kernel.setUndoActive(false)` (history kept) |
| file drop onto the frame | `FileDropTargetListener` route guarded or rejected while isolated (delivery to a blocked frame not established — test) |
| API-driven mutation | Desktop has no API server by default; in-process API calls guarded by the isolation flag (test) |

Lifecycle of the exits (no inherited save prompt): the controller performs the
exit through a dedicated GeoCeDG path that does not call `isSaved()` /
`saveCurrentFile()`; reopen first re-enables Undo (`Kernel.setUndoActive(true)`)
so that `loadNativeArchive` commits a fresh baseline, then loads the original
file through the native open path; close disposes of the document without the
save prompt; the isolation flag is cleared only after the exit completes; the
uncertified state is discarded, never written.

## 8. Non-blocking observation N1 — wording

Accepted insertion: one undo step, document UUID kept, selection cleared and
Move tool active (inherited paste). Certified failed insertion (K1-S): drawing
restored exactly; selection cleared and Move tool active. Compensating recovery
with declared session reset (K1-R-H): additionally live objects,
Parallel/Perpendicular memory and IsoABorder link reset. The inherited
behavior of other operations is not changed.

## 9. Architecture and verification

Desktop-owned GeoCeDG only: undo-manager subclass (stateless factory override,
G6), orchestration, isolation controller and guards. No kernel, shared or
inherited source change; Classic unaffected. `INTEGRATED_PHASE` (PHASE +
INTEGRATION) remains the recommendation, not frozen. Existing
`AppGeoCeDG`-based suites to name in the frozen plan:

| Area | Existing suites |
|---|---|
| native open, Save/Save As, UUID, lifecycle | `GeoCeDGDocumentLifecycleTest`, `G9U1NativeLifecycleReviewTest`, `PreG9BR6PlusA2NewDocumentBaselineTest`, `PreG9BR6PlusD1UnitLoadTest`, `G9S1NativeArchivePersistenceTest`, `G9S1R1NativeArchivePersistenceTest`, `G9U0R4NativeArchivePersistenceTest`, `G9U0R5NativeArchivePersistenceTest`, `G9U0R6NativeArchivePersistenceTest` |
| macros | `G9U1MacroNativeArchivePersistenceTest`, `G9U1UserToolLibraryTest`, `PreG9BR6PlusE1LCuratedLibraryTest` |
| hidden layers | `PreG9BR6PlusA1HiddenLayerTest`, `PreG9BR6PlusA2HiddenLayerPersistenceTest`, `PreG9BR6PlusA1LayerWorkspaceTest` |
| Locus V2, identities | `LocusV2DesktopLifecycleRegressionTest`, `PreG9bR2E1SemanticPairNativeArchiveTest`, `PreG9bR2IntersectionEndpointNativeArchiveTest`, `PreG9BR6PlusCBaseIdentityTest`, `PreG9bR3RedefineFrontendTest` |
| Spline V2 | `PreG9BR4SplineV2AuthoringTest` |
| F2 memory | `PreG9BR6PlusF2AuthoringMemoryTest`, `PreG9BR6PlusF2R1DualActivationTest` |
| IsoABorder | `PreG9BR6PlusE3IsoABorderDesktopTest` |
| clipboard and D1 | `PreG9bR3C1DesktopClipboardTest`, `PreG9bR3C1U1DesktopClipboardTest`, `PreG9BR6PlusD1UnitClipboardTest`, `PreG9BR6PlusD1PasteNoticeVisibilityTest` |
| scripts | `PreG9BR6ScriptSurfaceBoundaryTest`, `G9U1ScriptWorkflowTest` |

New focused tests are required (no existing class covers them): asynchronous
accounting exits (G2), attach-side quiescence (G3), recovery-source capture
(G4), labeled and grouped history (`ER-F3R-3`), native recovery snapshot and
recovery copy (G1), isolation routes and exits (G5, NE2), EDT runs (NE1),
factory lifecycle at startup (G6), exception preservation (G7). The design
addendum successor names them.

## 10. Conclusion

All seven gates, five observations and two not-established items are
reconciled; G1 and G2 are incorporated into Q1; no discrepancy requires new
technical evidence before author disposition. Effective containment and EDT
behavior remain implementation gates with explicit stop conditions.

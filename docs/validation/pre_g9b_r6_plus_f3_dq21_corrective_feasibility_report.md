# PRE-G9B-R6-plus-F3 — DQ-F3-21 corrective feasibility report

- Activity: `PRE-G9B-R6-plus-F3-DQ21-RECOVERY-CORRECTIVE-FEASIBILITY-GATE`
  (author instruction of 2026-10-12; focused characterization, temporary
  probes, documentary reconciliation)
- Status: **CHARACTERIZATION COMPLETE — PENDING AUTHOR DISPOSITION**
- Base: `T_F3_RECOVERY_SAFETY` `6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6`, tree
  `8f2e141bcb1311051b14d8efb3d7289b15e95212`, parent
  `3e24ed3aca422eef4e844a6c1230a3a3fc599c21` (STATIC
  `verification-a2b8e613f2fe43d18c8300893c307ba2` `ACCEPTED / COMPLETE`)
- Scope: `F3R-UNDO-EQUAL-CONTENT-PENDING`, `F3R-RECOVERY-FAILURE`,
  `F3R-DOCUMENT-IDENTITY-SUCCESS` and their consequences for `DQ-F3-21` only
- Companions: [corrected option assessment](pre_g9b_r6_plus_f3_dq21_corrected_option_assessment.md);
  [risk update](pre_g9b_r6_plus_f3_risk_register_corrective_update.md);
  [decision sheet update](pre_g9b_r6_plus_f3_author_decision_sheet_corrective_update.md);
  machine-readable [corrective state matrix](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-dq21-corrective-state-matrix.json)
- Earlier evidence (unchanged): [recovery safety report](pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md)
  (`RG-S1`–`RG-S10`, Focus A–E), `ER-F3R-1`, `ER-F3R-2`
- `selfApproved = false`

No product code, permanent test, specification, ADR or author decision was
changed. Probes ran from the scratchpad; build traces were removed (§7).

## 1. Entry gate (2026-10-12)

| Check | Result |
|---|---|
| HEAD, tree, parent | `6c07e01a…`, `8f2e141b…`, `3e24ed3a…` |
| linear chain | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` → `3e24ed3a` → `6c07e01a` |
| `main`, `origin/main`, live remote `main` | `e70718fe` (the local candidates are intentionally unpublished) |
| receipt | `verification-a2b8e613…` `ACCEPTED / COMPLETE`, `COMPLETED`, bound to `6c07e01a`/`8f2e141b` |
| worktree | clean |
| product delta since `main` (`source/`, `apps/`, `tools/`, `geocedg/specs/`, `docs/adr/`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/`) | none |
| previous recovery-safety report, assessment, decision sheet, risk update, matrix | present, unchanged |
| governance | `verification-levels.md` §12.8 re-read (classes); activity class `DOCUMENTATION_STATUS_ONLY` → STATIC |
| branch | `phase/pre-g9b-r6-plus-f3-dq21-recovery-corrective-feasibility` (local, from `6c07e01a`) |
| result | **PASS** |

## 2. Source-level facts (new)

| Id | Fact | Source |
|---|---|---|
| `CF-S1` | the history generation `undoHistoryGeneration` is private, read under the manager monitor when a store is scheduled, compared inside the synchronized `doStoreUndoInfo`, and advanced **only** by the public `commitUndoBaseline`; a store whose generation is older is deleted before it touches the list — no add, no prune, no listener notification | `UndoManagerD.java:42,63-86,125-139` (only occurrences) |
| `CF-S2` | consequently, a monitor-protected block that takes the history aside (`undoHistoryTo`) and commits a fresh baseline cancels **every** store scheduled before it, whatever its content, whenever it lands; stores scheduled afterwards carry the new generation | from `CF-S1` and `UndoManager.java:606-631` |
| `CF-S3` | the existing API gives no pending-store count, queue or operation id: `storeUndoInfo` starts an anonymous thread; the listener has no argument; `FileAppState.getXml` returns `null`. It cannot distinguish "no pending store" from "pending store with equal content" (cases 1, 3 of the task), nor attribute a store to an operation; it can cancel all of them (CF-S2) | `UndoManagerD.java:58-70`; `UndoInfoStoredListener`; `FileAppState` |
| `CF-S4` | `MyXMLioJre.readZipFromMemory` parses an undo archive without touching the undo history and without fallback; the inherited `loadUndoInfo` wraps the same call with a catch that recursively steps the history cursor back (`RG-S9`) | `MyXMLioJre.java:280-295`; `UndoManagerD.java:184-243` |
| `CF-S5` | `Kernel.setUndoActive(false)` disables Undo, Redo and `AppD.storeUndoInfo` without clearing the history; `Kernel.setUndoActive(true)` re-enables it without `initUndoInfo` (only `App.setUndoActive(true)` reinitializes) | `Kernel.java:4290-4299`; `AppD.java:3667-3671`; `App.java:2807-2821` |
| `CF-S6` | `AppGeoCeDG.saveGeoGebraFile` is a GeoCeDG-owned override already used as a save policy point; no GeoCeDG mechanism currently blocks editing or saving | `AppGeoCeDG.java:1752-1758` |
| `CF-S7` | the document UUID is "a random id to identify ggb files eg so that GeoGebraTube can notice it's a version of the same file": written as `<geogebra id>`, read back on load, regenerated for a new app or New document, for files without an id, and as a side effect of every `processXMLString` (the clipboard path); kept by Save and Save As; on Desktop it has no other reader and `tubeId` is never set | `AppD.java:4307-4315,1178`; `App.java:501,2028-2034`; `MyXMLio.java:203-216,279-280`; `MyXMLHandler.java:507-511`; `GFileHandler.java:61,138` |
| `CF-S8` | accepted GeoCeDG lifecycle tests already require the live document UUID to survive a rejected open | `GeoCeDGDocumentLifecycleTest.java:288,322,350,406,428`; `PreG9BR6PlusD1UnitLoadTest.java:213,254` |

## 3. Focus A — equal-content pending stores

### Alternatives

| Id | Alternative | Classification | Reason |
|---|---|---|---|
| A1 | existing sequencing (generation, notifications, ordering) as **observation** | **INFEASIBLE UNDER CURRENT CONTRACT** for identification | no count, queue or id (`CF-S3`); a quiet period is not quiescence |
| A1' | existing generation as **cancellation** | **PROVEN FEASIBLE WITH EXISTING API** | `CF-S1`, `CF-S2`; runtime A-Q1, A-Q3, A-Q4b |
| A2 | Desktop quiescence barrier = content gate + preflight + generation barrier in one monitor block | **PROVEN FEASIBLE WITH EXISTING API** under the stated conditions | content gate rejects differing pending stores before mutation; the barrier cancels all others; afterwards only this operation's stores can land, identified by generation and confirmed by content |
| A3 | serialization of the whole insertion under the manager monitor | **INFEASIBLE UNDER CURRENT CONTRACT** | the paste schedules its store under the same monitor and the operation must observe it; holding the monitor across the commit blocks it (A2 of the previous report); no EDT-blocking wait is acceptable |
| A4 | minimal shared correction (pending-store counter or operation id in `UndoManagerD`) | **not needed** for F3; described in the assessment | A2 gives the guarantee without shared change |

Conditions of A2: the gate, preflight, history take-out and barrier run in one
block synchronized on the undo manager; the operation runs on one thread (the
EDT in the product); the paste's own store is the only store the operation
expects; an unexpected post-barrier store, or one whose content does not equal
the post-commit document, is a serious failure.

### Probes (`f3-probe.corrective-a.txt`)

| Id | Set-up and trigger | Result |
|---|---|---|
| A-Q1 / A-Q2 | history `A→B→C→D`, two Undos (current B, Redo C→D); probe thread holds the manager monitor; `storeUndoInfo()` without change → store thread **BLOCKED in `doStoreUndoInfo`** (genuinely pending, not completed, not deduplicated — Desktop never deduplicates, `RG-S2`); content gate **passes** (blind spot confirmed); barrier applied (generation 3 → 4); on release: **no notification, nothing added, thread gone** | Locus V2 and Spline V2 failures recovered: **CERTIFIED** (full XML, ids, history list and cursor, UUID, macros, saved flag, file); walk of every position identical (Redo C→D intact); later action, Undo, Redo: no reappearance |
| A-Q3 | completion after the recovery boundary, emulated deterministically by invoking the store body with the pending store's XML and generation | previous K1-R-H: store **lands, prunes Redo (4 → 3 entries), flips the saved flag** → NOT CERTIFIED; barrier: **discarded**, history unchanged → CERTIFIED |
| A-Q4a | two pending stores, one with equal content, one after a user change | content gate **rejects before mutation**; after release both user stores land (inherited behavior of the user's own actions); document untouched by F3 |
| A-Q4b | two pending equal-content stores, three runs | both discarded each time (no notification); failure recovered, CERTIFIED, walks identical |
| A-Q-K1S | same pending equal-content store; elementary and macro payloads; K1-S under the barrier (`f3-probe.corrective-a-k1s.txt`) | pending store discarded; the elementary paste schedules no store; **CERTIFIED**, all 14 live instances kept, Parallel/Perpendicular memory and IsoABorder link kept, transferred macro removed, walk identical |
| A-Q5 | after recovery: new construction, full Undo/Redo walk | no missing or phantom actions, no reappearance, identities unchanged (A-Q1, A-Q4b walks) |
| A-Q6 | success with a pending equal-content store (Locus V2, elementary) | final history = previous prefix + one undo step for the insertion; Undo returns to B, Redo to the insertion; the old Redo branch is discarded exactly as by any new action; UUID kept |

Distinctions: **source-level guarantee** — cancellation of every pre-barrier
store (`CF-S1`) and atomicity of gate + barrier (monitor); **runtime-verified**
— A-Q1, A-Q3, A-Q4, A-Q6; **conditional** — single-thread execution of the
operation and no foreign post-barrier store (fail-closed by content check);
**declared effect** — a cancelled pending store is not added to the history;
its content equals the head (otherwise the gate rejects), so no reachable undo
state is lost.

Conclusion: `F3R-UNDO-EQUAL-CONTENT-PENDING` is closable with explicit scope
restriction by a Desktop-owned barrier; K1-R-H in its barrier form preserves
the complete previous history under every scheduling condition admitted by the
gate.

## 4. Focus B — persistent recovery failure

### Approaches

| Id | Approach | Classification |
|---|---|---|
| B1 | preflight of the recovery source before any mutation (`DocumentArchivePreflight.validate` on the held head archive) | **PROVEN FEASIBLE WITH EXISTING API** — guarantees that the archive is a loadable native document, not that the live reload succeeds |
| B2 | protected recovery boundary: guarded reload (`readZipFromMemory`, no history access, no inherited fallback), at most one retry, history reinstated as a pure list operation | **PROVEN FEASIBLE WITH EXISTING API** for history preservation and failure surfacing |
| B3 | serious-failure containment | **CONDITIONALLY FEASIBLE — SPECIFIED GATE REQUIRED**: Undo/Redo suspension exists (`CF-S5`); a save guard and an insertion lock require new GeoCeDG-owned code at existing override points (`CF-S6`); no editing lock exists |
| B4 | shared correction of the inherited recursive fallback | **not needed** for F3 (B2 bypasses it); the inherited behavior remains for ordinary user Undo |

### Probes (`f3-probe.corrective-b.txt`)

| Id | Injection | Result |
|---|---|---|
| B-F1 | recovery source truncated | preflight rejects **before any mutation**; full certificate equal, instances unchanged |
| B-F2 | K1-S removal: view throws at the second `remove` | exception; guarded reload; history reinstated → **CERTIFIED** with declared session reset; walk identical |
| B-F3 | guarded reload: view throws at the first `add` | exception **surfaced** to the orchestration (not swallowed); live history unchanged by the failed attempt; one retry succeeds → **CERTIFIED** |
| B-F4 | guarded reload: view throws on every `add`, history `A→B→C` | two attempts (bounded), both fail; history reinstated **identical**; classified SERIOUS (the document happened to match in content but is not certified because the reload raised); containment: Undo/Redo suspended without losing the history (resume restores it unchanged); **edits and Save to another file remain possible**, the original file is untouched; after the fault is removed a guarded reload certifies the document |
| B-F5 | as B-F4 with Redo C→D | same; Redo intact after reinstatement and after the later certified reload |
| B-F4 reference | inherited `restoreCurrentUndoInfo` with the same fault | `NoSuchElementException`, cursor 2 → 0 (reproduces `RG-S9`) |
| B-F6 | subsequent user operation after a serious failure | the application accepts edits and saves today (B-F4); without the B3 guards an uncertified document can be saved — **containment not established** |

Separated guarantees:

- **Failure detection — established** (runtime: B-F1, B-F3, B-F4; source:
  `CF-S4` has no swallowing fallback).
- **Recovery completion — established within bounds** (B-F2, B-F3: certified
  after removal fault or single reload fault with one retry); a persistent
  fault cannot complete by definition.
- **Recovery failure containment — not established**: history preservation
  and Undo/Redo suspension are proven; a save guard (no overwrite of the
  current file while uncertified; Save As allowed), an insertion lock, and a
  persistent serious message are specified but not implemented; editing cannot
  be blocked with existing mechanisms and must be addressed by the contract
  (author-level: whether editing an uncertified document is permitted with a
  warning).

## 5. Focus C — document UUID after successful insertion

Semantics (`CF-S7`, `CF-S8`): the root UUID identifies the **document lineage**
(the same document across saves and Save As; GeoGebraTube versioning on the
web). It is not an object identity, not a GeoCeDG durable semantic identity,
not session state and, on Desktop, has no external consumer. It is regenerated
for new documents and files without an id; its regeneration on paste is a side
effect of reusing the general XML loader for clipboard fragments, not a
documented paste rule. GeoCeDG already treats it as part of the live document
that a rejected open must keep.

| Case (`f3-probe.corrective-c.txt`) | UUID | Undo / Redo | Save / Save As / reopen |
|---|---|---|---|
| inherited success: elementary, Locus V2, Spline V2, macro | **changed** by the paste | Undo returns the old UUID, Redo the new one | saved and Save-As files carry the **new** UUID; reopen keeps it |
| preserved success (UUID restored before the orchestration's single undo store; for identity payloads the paste's own store is replaced): same four payloads, with prior Redo history | **kept** | Undo and Redo keep the original UUID; construction equal after Redo | saved and Save-As files carry the **original** UUID; reopen keeps it; construction XML equal |
| failed then successful insertion | kept throughout | failure certified; success one undo step | — |
| ordinary Ctrl+V (characterized, unchanged) | changed | — | — |

Reopen comparisons differ only in the runtime layer of three non-drawable
objects (working layer after a reload versus highest used layer after a fresh
open); the construction XML is equal. Metadata other than the id (`format`,
`version`, `app`, `platform`) is unchanged.

Recommendation: **PRESERVE DESTINATION DOCUMENT UUID** for F3 Insert File
(and for the style operation, which imports no document); ordinary Copy/Paste
and Duplicate keep the inherited behavior. Desktop-owned preservation is safe
under the barrier transaction; it requires replacing the paste's own undo
store for identity payloads, which the barrier makes identifiable.

## 6. Architecture

Every guarantee above is obtained with GeoCeDG Desktop orchestration over
public inherited API (`undoHistoryTo/From`, `UndoHistory`, `UndoCommand`,
`prepareUndoBaseline`/`commitUndoBaseline`, `readZipFromMemory`,
`DocumentArchivePreflight`, `Kernel.setUndoActive`, `App.setUniqueId`) and
reading undo archive files. No kernel, shared or inherited Desktop change; no
Classic impact. A shared change would add guarantees only outside F3 (fixing
the recursive fallback for ordinary Undo, an operation-id API, paste UUID
semantics for every paste) and would make the phase global.

## 7. Limitations and probe record

- The barrier and the commit ran on the JUnit thread, the reloads on the EDT;
  the product runs all on the EDT — an implementation test must repeat A-Q1
  and B-F4 on the EDT.
- Late completion after the boundary (A-Q3) was emulated by invoking the store
  body with the original arguments; real late landings were produced only
  before the boundary (A-Q1, A-Q4b), where the source guarantee is identical.
- Foreign stores from other threads after the barrier were not produced; the
  design treats them as a serious failure.
- Containment components (save guard, insertion lock, message) do not exist and
  were not prototyped.

| Output | Content | SHA-256 |
|---|---|---|
| `f3-probe.corrective-a.txt` | Focus A | `7ddf13351f19df9e6295e22a7f2ce344f29fb6dab4c59bced441d3a17ab0c27d` |
| `f3-probe.corrective-b.txt` | Focus B | `6077083474d6962d0b992c588b0397ce1f381bb3746db4a0df9f9493def49a9b` |
| `f3-probe.corrective-c.txt` | Focus C (second run) | `1893985255d8786befe417967aa59f8a5976107d5cfed1c7659726a60755be19` |
| `f3-probe.corrective-a-k1s.txt` | K1-S under the barrier | `80e6a692e1879369c3eb98071e5435faf45d64b5c8cea255cbfb575b369903ca` |
| probe source | `F3CorrectiveGateProbeTest.java` (755 lines, final; reuses the scratch `F3RecoveryGateProbeTest` helpers) | `324a23f215aeb2a426c33a19cc486b2041df74ab29c72921861a833abd99aba9` |

Runs (`:desktop:desktop:test --tests …F3CorrectiveGateProbeTest.<method>` with
the scratch init script, exit 0): `focusA`, `focusB`, `focusC` (twice; the
second fixed the archive entry lookup of the saved-file id and added the
reopen diff), `focusAK1S` (added last; the other methods were unchanged by it).
Clean-up after each campaign: `:desktop:desktop:compileTestJava` without the
init script (exit 0); probe class, stash and JUnit XML files removed from
`source/desktop/desktop/build` (20 each time); `git status` clean before the
documentary changes. Source revision of every
probe: `6c07e01a` (no tracked change).

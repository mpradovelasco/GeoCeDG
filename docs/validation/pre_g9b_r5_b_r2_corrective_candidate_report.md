# PRE-G9B-R5-B-R2 — Input Help after reopen corrective candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R5-B, authorized second corrective descendant PRE-G9B-R5-B-R2
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE (unchanged; no escalation)
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile INTEGRATION

INITIAL T_R5B             = 7bc113afb22bb6b97b2ad5e1e3f00117c8022be2 (technically accepted, superseded)
T_R5B_R1                  = 919adc8e442d0143367063f1fb8dedbb02f8e796
T_R5B_R1 tree             = c2c3364de6a97b7c7a41d974bfd07791a98da863
T_R5B_R1 verification     = INTEGRATION verification-34c0b12566c14f54b45352b354c815c5,
                            ACCEPTED / COMPLETE, 19/19
T_R5B_R1 author smoke     = clean-session language switch confirmed; CORRECTIVE FINDING remains
T_R5B_R1 author approval  = NOT GRANTED
T_R5B_R2                  = the commit that contains this artifact; its parent is
                            T_R5B_R1; not nameable inside it, reported separately

GUIDE_IMPACT              = NONE
SERIALIZATION_IMPACT      = NONE
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval. The
[initial candidate report](pre_g9b_r5_b_candidate_report.md) and the
[R1 corrective report](pre_g9b_r5_b_r1_corrective_candidate_report.md), with their
evidence, stay unchanged as history. Machine-readable evidence of this descendant:
[`pre-g9b-r5-b-r2-corrective-evidence.json`](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-r2-corrective-evidence.json).
The post-commit `INTEGRATION` evidence is reported outside this file.

## 1. Entry identity and authority

| Identity | Value |
|---|---|
| corrective base = parent (not amended) | `T_R5B_R1` `919adc8e442d0143367063f1fb8dedbb02f8e796`, tree `c2c3364de6a97b7c7a41d974bfd07791a98da863`, parent `7bc113af…` |
| `T_R5B` | `7bc113afb22bb6b97b2ad5e1e3f00117c8022be2`, tree `d47aafdfaace0aa7919f66dde9ff24ec3d1dac65` |
| `P_DOTXML` | `504a19f7e655ef50c4bfa8095a07f94c11b27a01`, tree `63e7f0cd79aa9e1cf1259b6466e8ac16dc261704` |
| local `main` = `origin/main` = live remote `main` after `git fetch` | `504a19f7…` |
| local branch (not pushed; not identity) | `phase/pre-g9b-r5-b-canonical-english-command-surface` |
| worktree at entry | clean at `T_R5B_R1` |

On 2026-10-01 the author authorized `PRE-G9B-R5-B-R2` as a second bounded corrective
descendant. The finding: after restarting and reopening `testCircle3.cedg`,
accepting an autocomplete option (for example `Line…`) and pressing Enter leaves
the lower Input Help syntax/detail area un-updated. The author recorded R1's
clean-session language switch as manually confirmed and kept R1's `setLanguage`
fix closed unless contradicted. The class stays `INTEGRATED_PHASE` with
`INTEGRATION` acceptance; no escalation was needed.

## 2. Author fixture

| Artifact | Fact |
|---|---|
| `artifacts/pre-g9b-r5/testCircle3.cedg` | SHA-256 `af9821c4a38dff49d58d047bd2553d9a912be5fb189bad976316b8abcef02c39`, 15 164 bytes, written 2026-10-01 08:54:00; untracked and ignored (`.gitignore` `/artifacts/*`); never modified. Only a scratch copy was opened. Not committed. |
| other session logs | no new `hs_err_pid*.log`; the earlier `artifacts/pre-g9b-r5/hs_err_pid32224.log` is unrelated and still unattributed |

## 3. Reproduction

All runs drive the real windowed product on Temurin 25.0.4:
`org.geocedg.desktop.GeoCeDG.main` with an isolated settings file and the real
frame. A document is opened through `GuiManagerD.loadFile`, the File → Open path.
Input Help is opened with the input bar's own toggle, then the test types `Line`
into the real Algebra Input. It starts the real autocompletion, accepts the first
completion through `validateAutoCompletion` and presses Enter through the field's
key listeners. Before and after each step it records the Input Help selection and
syntax pane, the tree, the field's document listeners and the lookup state.

| Session on unchanged `T_R5B_R1` | After accepting `Line( <Point>, <Point> )` |
|---|---|
| fresh, no document | Input Help selects `Line`; pane shows the `Line` syntax |
| fresh, `c` and `k` typed (same construction) | selects `Line` |
| `testCircle3.cedg` copy reopened | **no selection, empty pane** (also after Enter) |
| clean control reopened (saved by the typed session above) | **no selection, empty pane** |
| empty document (no object) reopened | **no selection, empty pane** |
| no document, `SwingUtilities.updateComponentTreeUI(frame)` | **no selection, empty pane** |
| no document, Product language EN → ES → EN | **no selection, empty pane** |

Every failing session also shows a reordered listener list on the Algebra Input
field's document. The input preview still runs and logs its parse error in every
session.

## 4. Clean control and minimized witness

The clean control was produced in a fresh `T_R5B_R1` session: `c=Circle((0,0),2)` and
`k=Circle((1,1),1)` typed and saved (SHA-256 `56fa434d…`). Reopening it fails exactly
like `testCircle3.cedg`. An empty document saved from a clean session also fails.
The minimum persisted content is therefore **none**: any native document whose
opening reaches `GuiManagerD.updateGUIafterLoadFile` re-triggers the defect. So
does a Swing UI refresh of the window or a product-language change, with no
document at all.

## 5. Archive and content comparison

Both archives hold the same five entries: `geogebra.xml`, `geogebra_defaults2d.xml`,
`geogebra_defaults3d.xml`, `geogebra_javascript.js` (the empty `ggbOnInit` stub) and
`geogebra_thumbnail.png`.

| Class | `testCircle3.cedg` vs clean control |
|---|---|
| construction objects, command identities, expressions | identical: `Circle((0, 0), 2)` → `c`, `Circle((1, 1), 1)` → `k` |
| labels | identical (`c`, `k`); no label equals a command or an alias |
| scripts | none (same empty `ggbOnInit` stub) |
| macros / tools | none |
| undo structures | none persisted |
| views / perspectives / window layout | differ: pane dividers, view sizes and locations, window geometry (author's window) |
| toolbar definition | differs: the author's string lacks tool group `140`; not related to Input Help |
| Input Bar / Input Help settings | identical: `<input show="true" cmd="true" top="algebra"/>` (the product default `InputPosition.algebraView`); no Input Help selection is persisted |
| other document settings | differ: `algebraStyle` 2 vs 0, object layer 5 vs 0, default animation step 0.1 vs 1 |
| language attributes | none in either file |
| GeoCeDG-specific metadata | none in either file |
| command-head text from the earlier defective session | none: commands are stored as internal names |
| R5-B presentation provenance | none: the head record of the re-entry guard is a transient `Localization` field, restored in `finally` and never serialized |

None of the differences is related to Input Help, and both files fail.

## 6. Behaviour by identity

| Identity | fresh | reopened control | reopened `testCircle3` | bare UI refresh |
|---|---|---|---|---|
| `P_DOTXML` | works | **fails** | **fails** | **fails** |
| `T_R5B` | works | **fails** | **fails** | **fails** |
| `T_R5B_R1` | works | **fails** | **fails** | **fails** (and after EN → ES → EN) |

The listener reordering is identical at all three identities.

## 7. Causality classification

```text
CLASSIFICATION = C. PRE-EXISTING DEFECT, NOW CONTRACT-RELEVANT TO R5-B
                 (refined: not in the loader; the loader's UI refresh is one trigger)
BAD STATE PERSISTED = NO (valid content re-triggers a session defect)
```

- **Not A (R5-B) and not B (R1).** It reproduces identically at `P_DOTXML`.
- **Not D (bad content from the pre-R1 session).** A control document from a clean
  session and an empty document both fail.
- **C, refined.** The defect is a pre-existing order dependence in the Algebra Input
  → Input Help bridge (§8). Opening a document is one of its triggers, through the
  Swing UI refresh in `updateGUIafterLoadFile`. It is contract-relevant to R5-B,
  whose accepted Input Help focus by identity (ADR 0031 decision 9; `T-SYNTAX`,
  `T-AUTOCOMPLETE`) then never happens for an accepted completion.
- **Evidence relevant to R1, not contradicting it.** The same defect follows a
  product-language switch in a clean session, both before R1 (bare `setLocale`
  also refreshed the component tree) and after it. R1's `setLanguage` fix is
  correct and retained. R2 removes this remaining trigger with the rest.

## 8. Root cause

Under the GeoCeDG profile the input preview is syntax-only.
`ScheduledPreviewFromInputBar.setInput` parses the text synchronously inside the
Algebra Input's document listener. A parse error goes to
`ErrorHelper.handleException`. That reaches `showCommandError` only when
`handler.getCurrentCommand()` names a command; `AlgebraInputErrorLogger.showCommandError`
then calls `InputBarHelpPanelD.focusCommand`. The logger's `getCurrentCommand()`
returned `inputField.getCommand()`: the word at the field's **caret**, with
square-bracket-only matching.

Accepting a completion replaces the text. The caret's position when the preview
reads it depends on whether Swing's `DefaultCaret` document listener has already
moved it to the end of the inserted text, and `AbstractDocument` notifies
listeners last-added-first.

| State | Notification order on the field's document | Caret seen by the preview | `getCommand()` |
|---|---|---|---|
| after startup | `AlgebraInputD$1` (preview), `CompletionsPopup$1`, `BasicTextUI$UpdateHandler`, `DefaultCaret$Handler` | 0, before the caret update | `Line` → Input Help focused |
| after any UI refresh of the field | `InputMethodRequestsHandler`, `BasicTextUI$UpdateHandler`, `DefaultCaret$Handler`, `AlgebraInputD$1`, `CompletionsPopup$1` | end of text, after the caret update | empty → generic error, no focus |

`SwingUtilities.updateComponentTreeUI` re-installs the field's text UI, which
removes and re-adds the caret and UI listeners after the preview listener. It is
reached from:
- `GuiManagerD.updateGUIafterLoadFile` (File → Open; `AppD.loadFile` alone, used by
  the earlier persistence tests, does not reach it);
- `GuiManagerD.updateFonts` (`setLocale` / `setLanguage`);
- `AppD.updateToolBar`, `updateContentPane` and theme refreshes.

The correct state of the moment is "the command at the edit", and it was read
from a caret whose value during the edit depends on listener order.

State coherence before and after reopening, on `T_R5B_R1`:

| State | Result |
|---|---|
| UI locale, command bundle locale | unchanged |
| reverse table (`Line` → `Line`), sub-command dictionaries, CAS state | unchanged |
| Input Help tree | same panel instance, 21 groups, canonical identity-carrying entries |
| Input Help `selectedCommand` / selected path | none before the completion, as in a fresh session |
| `focusCommand("Line")` called directly | selects `Line` in every session |
| syntax provider output | unchanged |
| completions | the same 28 canonical completions with carried identity |
| R5-B re-entry provenance | not involved, not persisted |
| changed state | **only** the document-listener notification order of the Algebra Input field |

The hypothesised dictionary / Input Help ordering around file load is
**rejected**: no command, dictionary or entry state is stale after a reopen.

## 9. Architectural placement

| Layer | Decision |
|---|---|
| document load lifecycle | unchanged; its UI refresh is legitimate and shared by many other paths |
| Desktop GUI / perspective restoration | unchanged |
| Input Help model lifecycle | unchanged; correct when asked to focus |
| **Algebra Input → Input Help focus bridge** | **owner**: it must determine the current command independently of listener order |
| R5-B canonical entry / provenance lifecycle | unchanged; nothing persisted, nothing to invalidate |
| persisted document content | unchanged; not involved |

## 10. Correction

`source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/inputbar/AlgebraInputD.java`:
- The preview listener passes the `DocumentEvent` offset to its
  `AlgebraInputErrorLogger`.
- Under the GeoCeDG profile, `getCurrentCommand()` applies the same word rule as
  `AutoCompleteTextFieldD.getCommand()` (`InputHelper.updateCurrentWord(true, …, true)`).
  It applies it at that edit offset, into a local buffer, instead of at the live
  caret.

The read never mutates the field's autocompletion state, and the Classic profile
keeps the inherited call. For an accepted completion the edit offset is the
position the preview saw at startup, so a reopened, refreshed or re-languaged
session behaves exactly like a fresh one. There is no fuzzy or nearest-string
matching: identity still comes from `loc.getReverseCommand` of the exact word and
from `focusCommand`'s carried identity. Parser, lookup, reverse table, Input Help,
R5-B entries, provenance, serialization and R1's `setLanguage` lifecycle are
unchanged.

## 11. Serialization impact

`SERIALIZATION_IMPACT = NONE`:
- `AlgebraInputD` is on no serialization path, and no XML writer or reader changes.
- The new regression asserts that focusing Input Help leaves the reopened
  construction XML byte-identical.
- The persistence regressions are unchanged and pass: `T-XML` historical
  document, `DotXmlLocaleIndependentPersistenceTest`, native archive tests.

No document format change or migration is involved.

## 12. Changed paths

| Path | Change |
|---|---|
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/inputbar/AlgebraInputD.java` | the correction (upstream-owned, already registered as modified) |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR5BR2InputHelpAfterReopenTest.java` | new regression (4 tests) |
| `docs/upstream/modified-files.yml` | new test entry; `PRE-G9B-R5-B-R2` purpose on the `AlgebraInputD` entry |
| `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json` | catalog data from the official updater |
| `docs/roadmap/geocedg_roadmap.md` | status only (version 4.29) |
| this report and its evidence mirror | new |

No committed fixture is added: the regression generates its documents in a
temporary directory. The author's file is not committed.

## 13. Regression tests

`PreG9BR5BR2InputHelpAfterReopenTest` (Desktop). It drives the embedded GeoCeDG
application on the event thread, where Swing's caret follows document edits. It
types into the real Algebra Input, accepts the canonical `Line` completion through
`validateAutoCompletion`, and asserts three things. The Input Help selection is the
entry with identity `Commands.Line` and head `Line`. The syntax pane starts with the
canonical syntax of the current UI language and never contains `.Syntax`. And the
first line is `Line( <Point>, <Point> )` in English or `Line( <Punto>, <Punto> )` in
Spanish.

| Test | Scenario |
|---|---|
| `acceptedCompletionFocusesInputHelpInAFreshSession` | control |
| `reopenedDocumentStillFocusesInputHelpOnTheAcceptedCommand` | save → new application → `GuiManagerD.loadFile` → completion; construction XML unchanged; `r=Line((0,0),(1,1))` submitted with Enter creates a `Line`, and Input Help keeps the identity |
| `reopenAcrossLanguagesKeepsIdentityAndUsesTheCurrentSyntaxBody` | saved under EN and under ES, each reopened under EN and ES; the Spanish alias `Recta` finds the identity `Line` |
| `anyInputBarRebuildKeepsTheCompletionFocus` | component-tree UI refresh, and Product language EN → ES → EN |

| Run | Result |
|---|---|
| on unchanged `T_R5B_R1` | 3 failures (`Input Help not focused`), control passes |
| with the correction | 4 passed |

Real windowed product with the correction (Temurin 25.0.4): `testCircle3` reopened
under EN (`Line`) and ES (`Recta`), the clean control, the empty document, the
language round trip and a bare UI refresh all select `Line`. The pane shows
`Line( <Point>, <Point> )…`, or `Line( <Punto>, <Punto> )…` in Spanish, after the
completion and still after Enter.

## 14. Preserved invariants and existing regressions

The correction preserves all of these:
- `E(k)`;
- `USER`/`SCRIPT`/`XML` lookup and reverse-table semantics;
- the parser and serialization semantics;
- R1's host `setLanguage` lifecycle;
- localized aliases and canonical heads;
- explicit autocomplete identity;
- `CANONICAL_HEAD_SHADOWED`;
- the Dot/XML repair.

Unchanged tests that pass with the correction:
- `PreG9BR5BR1RuntimeLanguageSwitchTest` (4);
- `PreG9BR5BCanonicalReentryTest` (5);
- `PreG9BR5BCanonicalHelpTest` (4);
- the other original R5-B classes and `G9U0LocalizationHelpTest.l01`, in the
  complete Desktop suite (§15).

`DotXmlLocaleIndependentSerializationTest` is a shared-module test. The shared
modules are byte-unchanged from `T_R5B_R1`, and `final.shared` runs again in this
descendant's `INTEGRATION`.

## 15. Pre-candidate checks

These are development diagnostics on the staged tree, not acceptance:

| Check | Result |
|---|---|
| complete Desktop suite | 1 638 tests, 0 failures, 0 errors, 1 skipped (1 634 at `T_R5B_R1` plus 4) |
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings (505 and 125 files) |
| `Assert-GeoCeDGUpstreamBoundary` | 858 registered files, no unregistered path |
| `git diff --cached --check` | clean; new files LF; mixed-EOL files keep their bytes, new lines LF |
| `verify.ps1 -PlanOnly` | `INTEGRATION` `4502c4bd…`, `STATIC` `25dfacc8…`, `FINAL` `3d8752b9…`: unchanged |
| `verify.ps1 -Profile INFRA_UNIT` | `ACCEPTED / COMPLETE`, 22/22, 0 diagnostics (`verification-d2317c8c746741159ccdff80ae16a8ed`) |

JUnit inventory, regenerated by the official updater from `discovery.shared` and
`discovery.desktop` dry runs and an executed, passing `final.desktop` run (1 632
tests, 0 failures, 1 allowlisted skip); `final.shared` is unchanged:

| Record | Before | After |
|---|---|---|
| `discovery.desktop` identities | 1 634, `9dbc37a2…` | 1 638, `e6a029474240b4b72579208255da5d3b58633f7f383ba0e82047858396ca220b` |
| `final.desktop` identities | 1 628, `2f52a044…` | 1 632, `5deb5812acd22852f73336708b066edcdf931dccd6cf7a1e15a93c60a3c01dac` |
| module discovery-evidence digests (shared, desktop) | `53300434…`, `b05567b7…` | `0c6b216d…`, `289a948d…` (shared identities unchanged) |
| registry `junit_inventory` pin | `ff4f70b53ccfca49b08dff23daceb2d9c4128d51849eb8ea902a617dbbb4b3fc` | `199d40a63228976ad2e19fe561fa3012742829b3d05d9922bc9cf7608d9d074b` |

The parent pin was reproduced before the update. Every other selection is
unchanged, and no hash was edited by hand.

## 16. Retained observations and debt

| Identifier | Disposition |
|---|---|
| `OBS-R5B-R2-CLASSIC-CARET-READ` | the Classic profile keeps the inherited caret-based read and with it the same pre-existing defect; out of R5-B scope |
| `OBS-R5B-R2-COMPLETIONS-POPUP-LISTENER-ORDER` | the completion popup's own document listener is reordered by the same UI refresh; no symptom observed (completions are offered and accepted after reopen); unchanged |
| `OBS-R5B-R2-EMBEDDED-HOST-ROOT` | the embedded test host does not place the application panel inside its main component; the regression attaches it before the language switch, as the windowed product has it |
| `OBS-R5B-R1-NATIVE-AWT-CRASH-UNATTRIBUTED` | unchanged: not reproduced, not attributed, not claimed fixed |
| `OBS-R5B-R1-SYNTHETIC-INPUT` | the windowed probes dispatch events to Swing components, not operating-system input |
| R1 and R5-B observations, `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | carried forward unchanged |

## 17. Verification plan

`INTEGRATION` runs fresh on the exact frozen descendant; the R1 run is not reused.
`STATIC` is not required: no guide, prompt, ADR or specification changes, and
`INTEGRATION` executes the documentation, style and whitespace diagnostics over the
changed records. FINAL is not required by the frozen class.

## 18. Impact declarations

```text
GUIDE_IMPACT = NONE
Rationale: Input Help syntax for the command being entered is already documented
  behaviour; the correction makes it reliable after reopening a document or
  changing the product language. No policy changes.

SERIALIZATION_IMPACT = NONE

BOOTSTRAP IMPACT — NO CHANGE REQUIRED

VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY
Rationale: the JUnit inventory and its registry pin were regenerated through the
  official updater for the new Desktop tests.

PRODUCT_PHASE_EFFECT = BOUNDED — the GeoCeDG Algebra Input reads the current
  command at the edit; R5-B command policy unchanged
```

## 19. Focused author smoke

Clean session:
1. Start GeoCeDG, type `Line`, accept `Line( <Point>, <Point> )`: the lower Input Help
   area shows the `Line` syntax.
2. **Product language… → Español**, type `Recta`, accept `Line( <Punto>, <Punto> )`:
   the area shows `Line( <Punto>, <Punto> )…`; switch back to English and repeat
   with `Line`.

Problematic-document route:
3. Restart, open `testCircle3.cedg` without switching language, type `Line`, accept
   the completion, complete the arguments (for example `Line((0,0),(1,1))`) and
   press Enter: the area shows the `Line` syntax after accepting and still after
   Enter, and the line is created.

Reopen cycle:
4. Save the session as a new `.cedg`, restart, reopen it, repeat step 3.

Cross-language:
5. Reopen a document saved in English with the product in Spanish (or the
   converse): typing `Recta` / `Line` and accepting shows the canonical `Line` head
   with the current UI-language body.

Semantic spot checks:
6. With the product in Spanish, a Spanish alias such as `Circunferencia((0,0),1)` is
   accepted and shown as `Circle(...)`.
7. After `Circle=5`, redefining an existing circle without editing the command is
   refused with `CANONICAL_HEAD_SHADOWED`.
8. Save and reopen preserve geometry and dependencies.

## 20. State

```text
INITIAL T_R5B          = TECHNICALLY ACCEPTED
                         SUPERSEDED
T_R5B_R1               = TECHNICALLY ACCEPTED
                         SUPERSEDED BY R2 IF R2 SUCCEEDS
                         NOT AUTHOR APPROVED
PRE-G9B-R5-B-R2        = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR SMOKE           = PENDING
AUTHOR APPROVAL        = PENDING
promotion              = NONE
PRE-G9B-R6             = NOT AUTHORIZED
PRE-G9B-R7             = NOT AUTHORIZED
G9B                    = NOT AUTHORIZED
```

Push, branch publication, merge, promotion, tag, release and binary publication
each require a separate explicit author instruction naming the exact candidate SHA.

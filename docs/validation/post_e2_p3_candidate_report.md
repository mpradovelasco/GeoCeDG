# POST-E2-P3 — interactive document-macro access: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P3
VERIFICATION_CLASS        = INTEGRATED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P3
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
BASELINE                  = 9bdcdf834814ce7eeb16ba189283208415fd1e7d
                            tree 06831f900e728900c5fc92b48d4f85e3c10fe759
                            (POST-E2-P2-R1 closeout, published main)
READINESS GATE            = A — E2 evidence reused, sufficient with two focused checks
NEW CHARACTERIZATION PHASE = NOT REQUIRED
PRODUCT CHANGE            = YES — User tools menu (GeoCeDG Desktop presentation)
MACRO SEMANTICS           = UNCHANGED (loading, registration, execution, command
                            resolution, serialization, DAG, kernel)
CLASSIC                   = UNCHANGED
AUTHOR_SMOKE              = PENDING
selfApproved              = false
authorApproved            = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

This artifact records only facts fixed when it was written; it carries no author
approval. Authority: [P2-R1 closeout record](post_e2_p2_r1_closeout_record.md) (P3
authorization). Design and readiness gate:
[`post_e2_p3_document_tools_design.md`](../architecture/post_e2_p3_document_tools_design.md).
Mirror: [`post-e2-p3-candidate-evidence.json`](../../geocedg/validation/post-e2/post-e2-p3-candidate-evidence.json).

## 1. Why no separate characterization

The E2 smoke follow-up part A already characterized the 24 `Templatev7.ggb` macros
(load, registration, command authority, invocation, document toolbar, product
toolbar and User tools policy, management route, persistence); those source paths
are unchanged at the base. Two questions it did not answer were settled by focused
scratch probes (design §1.2): every Templatev7 macro activates its own tool mode
through `app.setMode(MACRO_MODE_ID_OFFSET + getMacroID(macro))` with `ModeMacro`
holding the identical `Macro`, with input types all handled by the existing
`ModeMacro` contract; and the upstream kernel macro lifetime (File → New keeps the
macros and a save writes them; Open replaces them). Both have a unique resolution
within the authorized contracts, so no author decision was needed.

## 2. Result

**Selected design.** User tools ends with a **Document tools** submenu listing the
live macros of the open document; choosing one activates its existing macro tool
mode. Installed and bundled sections are unchanged.

**Modified production files.** `GeoCeDGUserTools.java`,
`GeoCeDGUserToolLibrary.java`, `apps/geocedg/application-profile.yml` (four EN/ES
texts).

**Macro activation mechanism.** The entry keeps the live `Macro`; on choice it is
re-checked (live macro of its command in the current kernel, not presented by the
library) and the mode `MACRO_MODE_ID_OFFSET + kernel.getMacroID(macro)` is set — the
mechanism the library uses for installed tools. No mode number or label is stored
as identity. A stale entry shows "This document tool is no longer available…" and
changes nothing.

**Document/library coexistence.** Unchanged digest rule. A document macro
equivalent to an available installed or bundled entry is offered only by that
entry (the documented "only visible, enabled choice"); without the entry it returns
to Document tools. A same-name installed entry with a different definition stays
unavailable, and the Document tools entry activates the document's own macro. A
macro the library registered in this session is a library tool, not a document
tool.

## 3. Verification (focused)

| Scenario | Test (`PostE2P3DocumentToolsTest`) | Result |
|---|---|---|
| A Templatev7 | `everyTemplateMacroIsListedAndActivatesItsOwnToolMode` | 24/24 listed in kernel order (tool names, e.g. "CirclebyD: Centre & Diameter"); each activates the mode of its own `Macro`; macro count and document XML unchanged |
| A graphical creation | `aChosenTemplateToolBuildsItsResultFromGraphicalSelection` | `SquarebyDiagonal` chosen in the menu; two clicks on existing points build an `AlgoMacro` of that macro with those inputs |
| B, F lifecycle and staleness | `theMenuFollowsTheDocumentAndStaleEntriesFailExplicitly` | empty document: disabled "This document has no tools"; Templatev7: 24; New: same live macros, still valid; Open of a document without macros: 0, an old entry warns and activates nothing; reopen Templatev7: new objects, the old entry never activates its namesake; a macro removed from the kernel fails safely and disappears |
| F duplicate names | `duplicateToolNamesAreDisambiguatedByTheirCommandNames` | "SameTool (MidA)", "SameTool (MidB)"; each activates its own macro |
| F unreadable library | `documentToolsSurviveAnUnreadableInstalledLibrary` | Document tools still listed |
| C coexistence | `equivalentAndSameNameLibraryToolsKeepTheirPrecedenceAndTheDocumentMacro` | equivalent: only the installed entry, same macro object, no copy; removing it restores the document entry; different same-name definition: installed entry `DefinitionMismatch`, document entry activates the document macro; library-registered macro excluded |
| D persistence | `savingAndReopeningKeepsTheDocumentToolsAndTheMenuWritesNothing` | `.cedg` save and reopen in a fresh window: 24 macros, same 24 entries |
| E compatibility | `G9U1UserToolLibraryTest` (38), `G9U0R3MenuLifecycleTest` (6) | installed, bundled and pinned tools and menu lifecycle unchanged |
| E reconciled menu pins | `PreG9BR6PlusE1LBundledCatalogTest.e1lCatalog10…`, `G9U1MacroNativeArchivePersistenceTest.ellipseAxis…` | both pinned the exact User tools item list; the first `final.desktop` producer run failed them on the appended separator and Document tools submenu. They now assert that tail explicitly, the bundled section and its failure entry as before, and that the reopened equivalent `EllipseAxis` is **not** repeated in Document tools (the installed entry stays the only choice) |

Product toolbar: not touched (no profile or toolbar code changed); Classic: no
Classic code changed and `GeoCeDGUserTools` exists only in the product; native
dimensions: no dimension code changed (full suites in §4).

## 4. Verification (suites and acceptance)

Recorded in the evidence mirror: Checkstyle (no new violation), upstream boundary,
official producers (`post-e2-p3.desktop`, `final.shared`, `final.desktop`) and the
inventory update. STATIC, INFRA_UNIT, PHASE `POST-E2-P3` and INTEGRATION run on the
frozen commit and are reported outside this artifact.

## 5. Author smoke checklist

1. Open `models/legacy/template-v7/original/Templatev7.ggb`; **Automation → User
   tools → Document tools** lists 24 tools.
2. Choose *SquarebyDiagonal*; click two points: a square is built. Try a tool with
   a number (*CirclebyD*: a point, then the diameter in the dialog) and one with a
   line (*relCoor*).
3. File → New: the same tools remain (they would be saved with the new document);
   open a document without tools: the submenu says it has none.
4. With an installed tool equivalent to a document tool, only the installed entry
   is shown.
5. Product toolbar, installed, bundled and pinned tools unchanged.

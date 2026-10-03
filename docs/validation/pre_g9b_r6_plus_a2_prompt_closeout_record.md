# PRE-G9B-R6-plus-A-2 preparation package closeout and authorization record

```text
A-2 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-A-2     = AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-F3      = PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (A-2 documentary preparation package and the F3
                                     planning reconciliation only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = true    (A-2 implementation and technical verification only;
                                     F3, C, E1, E2, E3, F1, F2, G, PRE-G9B-R7, G9B: false)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE    (this record)
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author instruction of 2026-10-03 that
approves the `F3` planning reconciliation and the documentary preparation
package of `PRE-G9B-R6-plus-A-2`, gives the author dispositions on the
requested decisions `DQ-A2-1` to `DQ-A2-11`, accepts the prepared architecture,
adds regression obligations and stop conditions, and authorizes the
implementation of `A-2`. The package is not a phase: approving it closes the
preparation, not `A-2`. This record changes no product, test, build,
packaging, verifier, registry, schema, specification, ADR or serialization
file, and it does not modify either frozen candidate. Its machine-readable
mirror is
[`pre-g9b-r6-plus-a2-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-prompt-closeout.json).

## Author disposition: F3 planning

```text
T_R6PLUS_F3_PLAN   = f467eb4563de7a7eb162da1b74954f9f847fa9d3
                     tree ece51c96117d6cbd55bc200d29c5240686516da9
PRE-G9B-R6-plus-F3 = PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED
```

Approved exclusively as planning: the File → Insert scope with Insert File and
Apply Template accepting `.cedg` and `.ggb`; OFF hidden from the GeoCeDG product
UI with the internal upstream support retained; the hard dependency
`D1 → F3`; no dependency with `A-2` or `C`; the operational order
`P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G`; and
the class `INTEGRATED_PHASE` with planned acceptance `PHASE + INTEGRATION`,
frozen only at a future authorization. The finding that Apply Template may
re-layer restyled objects stays an explicit open `F3` characterization
question; `A-2` neither resolves nor implements it. No further `F3` work is
prepared or implemented during `A-2`.

## Author disposition: A-2 preparation package

```text
T_R6PLUS_A2_PROMPT = 0e50626f70107376e691df6dea7bfcfe06de5c0a
                     tree   fefc57cece8090941ad0ac16098e58a679a290ec
                     parent f467eb4563de7a7eb162da1b74954f9f847fa9d3  (T_R6PLUS_F3_PLAN)
                     branch phase/pre-g9b-r6-plus-a2-prompt (local; never pushed)
AUTHOR_APPROVAL    = APPROVED, subject to the DQ-A2 dispositions below
selfApproved       = false
```

The approved package is the canonical prompt
[`pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md)
as prepared (blob `459dbf5dc620a966d8dd593ae899010470789043`), the
[A-2 author-decision record](pre_g9b_r6_plus_a2_author_decisions_record.md),
the [A-2 preparation characterization report](pre_g9b_r6_plus_a2_preparation_characterization_report.md),
their mirrors, and the roadmap 4.53 and mini-track plan updates. Its accepted
evidence is `STATIC` `verification-6d2c2ddb31904684a60ff9dac5dd9ada` on
`T_R6PLUS_A2_PROMPT`: exit 0, `ACCEPTED / COMPLETE`, 3/3, plan hash
`25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result
hash `2d257c74587d76490aa44307935aabdad38a21aab69f6244042825c644e11a9b`, with
the two standing diagnostics; `Test-PromptContractDocument` (`task` profile):
seven fields in order, `execution_safe = true`.

The frozen candidate is not amended. Where its prompt states a default for
`DQ-A2-1` to `DQ-A2-11`, **this record prevails**. The instruction requires, as
the first tracked edit of the phase, the amendment of the prompt to
`AUTHORIZED FOR IMPLEMENTATION` that records these dispositions and corrects
the defaults they supersede.

## Authorization

```text
PRE-G9B-R6-plus-A-2   = AUTHORIZED FOR IMPLEMENTATION
PUBLISHED_BASE        = 2941ddf2f2712340e831d292bdc2687b879850d6   (P_R6PLUS_D1;
                        tree 158184b150a42e6dcae2946c39620d92cbfcd888)
BRANCH_START          = 0e50626f70107376e691df6dea7bfcfe06de5c0a   (T_R6PLUS_A2_PROMPT;
                        tree fefc57cece8090941ad0ac16098e58a679a290ec; not published)
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-a2-layer-domain-persistence (local; no rebase)
VERIFICATION_CLASS    = GLOBAL_IMPACT        (kept)
PLANNED_ACCEPTANCE    = FINAL                 (exactly one, on one exact frozen clean
                                               candidate, after FINAL -PlanOnly)
STOP_STATE            = PRE-G9B-R6-plus-A-2 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                        selfApproved = false, authorApproved = false, passClaimed = false
```

If code or tests change after `FINAL`, the receipt no longer certifies the
modified tree: the phase stops and reports. The agent does not perform or
claim author smoke.

Not authorized: `F3`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7`,
`G9B`; push, publication, merge, rebase, squash, amend of either frozen
candidate, tag and release.

## Author dispositions on `DQ-A2-1` to `DQ-A2-11`

| ID | Outcome | Disposition |
|---|---|---|
| `DQ-A2-1` | accepted with precision | After a successful Open with persistent hidden layers: (1) choose the highest used drawable layer that is not hidden; (2) if no used drawable layer is visible, choose the lowest non-hidden layer in `0..L_MAX`; (3) never modify the persisted hidden-layer set merely to select a working layer. The working layer is session state and is not serialized. For the persisted `A-2` metadata, a hidden set that contains every layer `0..L_MAX` is invalid, because no legal working layer could then be chosen without making it hidden or silently changing the persisted presentation on Open; a recognized `A-2` document with an all-domain hidden set fails closed with an explicit diagnostic. This validation applies to persisted metadata; hidden layers are never inferred from object state. The `A-1` session degeneracy row about every layer being hidden does not authorize writing an invalid persisted state. |
| `DQ-A2-2` | **replaced in part** | A hidden-layer toggle is not undoable, creates no undo point, is document-persistent, and therefore marks the document unsaved when it changes the persisted hidden-layer state. A non-empty hidden-layer set is save-relevant document content: an otherwise geometrically empty document prompts for saving when its persistent hidden-layer state was changed and not saved; an untouched empty document with an empty hidden set stays saved and does not prompt. `persistent != undoable`. The hidden set is not duplicated inside `Construction` to satisfy the saved-state predicate: the narrowest shared/product save-relevance seam with an upstream-identical default is used, and `GeoCeDGLayerWorkspace` stays the single runtime owner. If this requires duplicating the authoritative set or materially redesigning general save semantics, stop and report. |
| `DQ-A2-3` | accepted | The prepared flat, versioned, document-level `<geocedgHiddenLayers .../>` element, a direct child of the full GeoCeDG document XML, outside `<construction>`, at the characterized deterministic position; lazy (absent when empty); canonical ordering; document-wide; never in preferences, macro, `.ggt`, clipboard or undo XML. The prepared grammar applies unless implementation evidence reveals a contradiction requiring author review. |
| `DQ-A2-4` | accepted | A build implementing `A-2` fails closed on recognized malformed or unsupported-newer hidden-layer metadata, including at least a malformed version, an unsupported newer version, a malformed layer list, a duplicate or ambiguous element, an out-of-domain persisted hidden layer, and the all-domain set prohibited by `DQ-A2-1`. Recognized `A-2` metadata is never silently discarded. Older readers that do not recognize the element may ignore or drop it under the accepted forward-compatibility limitation. |
| `DQ-A2-5` | accepted | Classic remains `0..9` and does not acquire the GeoCeDG domain; Classic and Web acquire no GeoCeDG hidden-layer product semantics; for the new element they behave as characterized older, non-owning readers; no hidden-layer UI or runtime authority is introduced there; no frontend broadening. If a shared change would make Classic accept layers above 9 as authoritative, stop. |
| `DQ-A2-6` | accepted with precision | Persistent hidden layers are applied only during an effective full-document, clearing document transition: the characterized New, Open, startup and full-document API routes, only where their effective load purpose denotes replacement of the document. Never from `evalXML`, generic merge, paste, Insert File, Apply Template, macro load, `.ggt` or non-clearing fragments. This is not decided from a method name such as `loadXML` alone, but from the actual load purpose and context established by the live architecture. A failed Open restores the previous hidden-layer state exactly. |
| `DQ-A2-7` | **wording replaced** | Not "non-interactive new objects → layer `L_MAX − 1`". The existing upstream structural rule `min(MAX_LAYERS − 1, app.getMaxLayerUsed())` is preserved with its bound parameterized through the approved product layer-domain authority, conceptually `min(configured L_MAX − 1, app.getMaxLayerUsed())`: GeoCeDG's cap is 98 and Classic's stays 8, as caps, not unconditional default layers. The `A-1` creation hook stays authoritative for interactive GeoCeDG creation and is neither bypassed nor redefined. No object is placed on layer 98 merely because the domain is `0..99`. |
| `DQ-A2-8` | accepted | Object-visibility semantics unchanged. Where `ShowLayer`, `HideLayer` and `setLayerVisible` take a layer number, the GeoCeDG range follows the configured `0..99` domain while Classic stays `0..9`. They are not rerouted to the hidden-layer presentation mechanism: object visibility and persistent GeoCeDG hidden-layer presentation stay distinct concepts. |
| `DQ-A2-9` | accepted only as a presentation compatibility adapter | The stored `GeoElement.layer` in GeoCeDG stays `0..99`. Where the 3D renderer's packed presentation encoding cannot safely represent the full range, `A-2` may clamp or adapt the renderer input to its current Classic-compatible range only. The adaptation never writes back to `GeoElement.layer`, never changes XML, the API-visible layer or the domain, and never establishes a semantic layer value. Layers above the renderer's distinguishable range may not keep distinct 3D draw-order encoding in this phase: a presentation limitation and residual observation, not layer semantics. No persistent hidden-layer behavior in the 3D view. If safe adaptation requires changing geometric or spatial semantics, stop. |
| `DQ-A2-10` | accepted | The established clamp policy is kept against the effective product domain: GeoCeDG `< 0 → 0`, `> 99 → 99`; Classic `< 0 → 0`, `> 9 → 9`; no wrap and no inferred migration. This compatibility behavior is distinct from malformed `geocedgHiddenLayers` metadata, which fails closed (`DQ-A2-4`). |
| `DQ-A2-11` | accepted | Every GeoCeDG layer picker or chooser that represents the product domain derives its range from the configured authority and exposes `0..L_MAX`; no literal `99` duplicated through Desktop; Classic keeps its domain. |

## Architecture accepted for implementation

One product-configurable layer-domain authority with upstream value 9 and the
GeoCeDG override 99; no global replacement of
`EuclidianStyleConstants.MAX_LAYERS = 9` that would silently widen Classic;
`GeoCeDGLayerWorkspace` stays the runtime owner of hidden layers; shared changes
limited to what serialization, validation and product hooks require; hidden
layers stay `DOCUMENT_PRESENTATION`, never geometric truth. Full-document
persistence and exclusion from undo are required invariants. No second
hidden-layer authority.

## Additional regression obligations

Besides the tests of the prompt, the phase proves explicitly that:

1. a hidden-layer toggle changes the saved/dirty state but adds no undo point;
2. a geometrically empty document with a non-empty persistent hidden set is
   save-relevant;
3. undo and redo never change persistent hidden layers;
4. a failed Open restores the hidden layers exactly;
5. Open never silently unhides a persisted hidden layer;
6. all-domain persisted hidden metadata fails closed;
7. Classic stays clamped to `0..9`;
8. GeoCeDG accepts and persists layers 10, 50 and 99;
9. the `L_MAX − 1` rule stays a cap based on `maxLayerUsed`, not a default layer;
10. any renderer-only 3D clamp never mutates the model layer;
11. the `D1` `UnitState` and the `A-2` hidden-layer persistence round-trip
    independently;
12. `B`'s preview/final-export hidden-layer consistency stays correct after
    save and reopen.

## Author smoke checklist (required content)

The candidate report provides a concise checklist covering at least: creation
and selection of layers 0, 10, 50 and 99; chooser bounds; Classic stays
`0..9`; hide layers, save, close and reopen; hidden state survives reopen; the
working layer after Open for the hidden-highest-layer case; the all-used-hidden
case; rejection of malformed and all-domain hidden metadata; a hidden-layer
toggle does not take part in undo/redo; a hidden-layer change makes the
document unsaved; a legacy document opens with an empty hidden set; `B`'s
picture preview and export stay coherent after reopen; `D1` units coexist with
persistent hidden layers.

## Additional stop conditions

Beyond every stop condition of the prompt, the phase stops if: implementing
the saved-state semantics requires duplicating the hidden set in
`Construction`; an all-domain hidden persisted document cannot be rejected
cleanly without materially changing the load architecture; GeoCeDG's `0..99`
domain necessarily widens Classic; renderer compatibility requires modifying
the authoritative model layer; persistence without undo requires a general
undo-architecture redesign; correctness requires changing the `A-1`
hidden-layer predicate or object-visibility semantics; or `F3` or `C` work
becomes necessary.

## Authorization state

```text
PRE-G9B-R6-plus-P0, A-1, B, D0, D1 = PASS — AUTHOR APPROVED — PUBLISHED
A-2 preparation package    = PASS — AUTHOR APPROVED               (not published)
PRE-G9B-R6-plus-A-2        = AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-F3         = PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-R6-plus-C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.

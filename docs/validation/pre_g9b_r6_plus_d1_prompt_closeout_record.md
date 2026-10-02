# PRE-G9B-R6-plus-D1 preparation package closeout and authorization record

```text
D1 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus-D1     = AUTHORIZED FOR IMPLEMENTATION
```

```text
selfApproved              = false
authorApproved            = true    (D1 documentary preparation package only)
passClaimed               = true    (preparation package only)
implementationAuthorized  = true    (D1 implementation and technical verification only;
                                     A-2, C, E1, E2, E3, F1, F2, G, PRE-G9B-R7, G9B: false)

candidateFrozen           = true
candidateMutated          = false
productPhaseEffect        = NONE    (this record)
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author instruction of 2026-10-03 that
approves the documentary preparation package of `PRE-G9B-R6-plus-D1`, gives the
author dispositions on the requested decisions `DQ-D1-1` to `DQ-D1-11`, accepts
the characterization `C1`–`C11` as implementation constraints, and authorizes
the implementation of `D1`. The package is not a phase: approving it closes the
preparation, not `D1`. This record changes no product, test, build, packaging,
verifier, registry, schema, specification, ADR or serialization file, and it
does not modify the frozen candidate. Its machine-readable mirror is
[`pre-g9b-r6-plus-d1-prompt-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-prompt-closeout.json).

## Author disposition

```text
T_R6PLUS_D1_PROMPT = 3fb7542af3ef36db150291ca77e03b0bf4b6f888
                     tree   de71421e6adbd9a9b5e6acc8051dd5154f6a5f63
                     parent 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c  (P_R6PLUS_D0)
                     branch phase/pre-g9b-r6-plus-d1-prompt (local; never pushed)
AUTHOR_APPROVAL    = APPROVED, subject to the DQ-D1 dispositions below
selfApproved       = false
```

The approved package is:

- the canonical prompt
  [`pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md)
  as prepared (blob `96f6e612cce1c43e2043abafe17ead3e897f4ef8`);
- the [D1 author-decision record](pre_g9b_r6_plus_d1_author_decisions_record.md)
  and its mirror;
- the [D1 preparation characterization report](pre_g9b_r6_plus_d1_preparation_characterization_report.md)
  and its mirror;
- the roadmap 4.48 and mini-track plan updates.

The accepted evidence of the package is `STATIC`
`verification-ae3a1972b1bb447f9b62f7f65b80c63f` on `T_R6PLUS_D1_PROMPT`: exit 0,
`ACCEPTED / COMPLETE`, 3/3 (`prompt.execution-safety`,
`repository.boundary-safety`, `static.scientific-inputs.semantic`), plan hash
`25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result hash
`d60ff7d284d67a10c4d030f2f6b192c5b9cd3cc8158be59acf806518da6216ed`, with the two
standing diagnostics (`diagnostic.governance` `DIAGNOSTIC_FINDING`,
`diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`).
`Test-PromptContractDocument` (`task` profile): seven fields in order,
`execution_safe = true`.

The frozen candidate is not amended. Where its prompt states a default for
`DQ-D1-1` to `DQ-D1-11`, **this record prevails**. The same instruction
requires, as the first tracked edit of the phase, the amendment of the prompt
that integrates these dispositions, corrects the boundaries they supersede and
freezes `GLOBAL_IMPACT / FINAL`.

## Authorization

```text
PRE-G9B-R6-plus-D1   = AUTHORIZED FOR IMPLEMENTATION
PUBLISHED_BASE       = 3268f9b99d20c5d9cf62f25d8066ee0a8e47765c   (P_R6PLUS_D0;
                       tree 83b32b6cb579dc28162f990160c3eef0b1928be6)
BRANCH_START         = 3fb7542af3ef36db150291ca77e03b0bf4b6f888   (T_R6PLUS_D1_PROMPT;
                       tree de71421e6adbd9a9b5e6acc8051dd5154f6a5f63; not published)
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-d1-unit-system (local; no rebase)
VERIFICATION_CLASS   = GLOBAL_IMPACT        (frozen)
PLANNED_ACCEPTANCE   = FINAL                 (one and only one, on one exact frozen
                                              clean candidate, after FINAL -PlanOnly)
STOP_STATE           = PRE-G9B-R6-plus-D1 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                       selfApproved = false, authorApproved = false, passClaimed = false
```

If product or test code changes after `FINAL`, that candidate is no longer the
accepted candidate: the phase stops and reports instead of claiming the old
receipt. The agent does not perform or claim author smoke.

Not authorized: `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7`,
`G9B`; push, publication, merge, rebase, squash, amend after freeze, tag,
release and binary publication.

## Author dispositions on `DQ-D1-1` to `DQ-D1-11`

| ID | Outcome | Disposition |
|---|---|---|
| `DQ-D1-1` | accepted | One `document.units` action under `options-product` opens the single Document Units dialog. The two permanent unit segments of the GeoCeDG status bar are presentation-only and invoke that same action when clicked. There is no second editing authority in the status bar. |
| `DQ-D1-2` | accepted with precision | The *New document units* preference panel stores defaults exclusively for future blank documents. Changing those preferences MUST NOT alter the current document's `UnitState`, its XML, its undo stack or its saved/dirty state. The preferences are applied only through the new-document lifecycle characterized in `C6`. |
| `DQ-D1-3` | accepted | A recognized `geocedgUnits` element misplaced outside the document `<construction>` fails closed because its document semantics cannot be established. In a macro context the unit element stays ignored with a text diagnostic, per the `D0` macro/ignored-context contract. Misplaced metadata is never reinterpreted. |
| `DQ-D1-4` | accepted | Explicit localized user-visible diagnostics, at least `en` and `es`, identify: unsupported newer version; malformed element; invalid or missing `usm` factor; duplicate element; misplaced element. The failed File → Open transaction leaves the previous live document and its saved, undo and file state unchanged. |
| `DQ-D1-5` | **replaced** | `Construction.isStarted()` keeps its meaning (geometric or macro construction content exists). A narrow shared save-relevant-content seam, conceptually `Construction.hasSaveRelevantContent()` (or an equivalent name the implementation justifies), carries `saveRelevantContent = constructionStarted OR persistentDocumentMetadataPresent`, where for `D1` `persistentDocumentMetadataPresent` includes `UnitState != EMPTY`. `App.isSaved()` uses it instead of overloading `isStarted()`. Consequences: an otherwise empty document whose unit state is changed becomes unsaved and prompts on New and close; an untouched new document whose defaults were applied as part of its creation stays saved after the baseline is retaken; `isStarted()` keeps its API meaning; Classic documents without unit metadata keep the upstream saved-state behavior. Presentation and session state never become save-relevant content. If the separation is impossible without material widening, the phase stops and reports. |
| `DQ-D1-6` | accepted | Keys `geocedg.units.new-document-construction.v1` (`unspecified`, `mm`, `cm`, `m`) and `geocedg.units.new-document-presentation.v1` (`none`, `mm`, `cm`, `m`); never `usm`. Invalid stored values behave as unset and are not rewritten merely by reading them. |
| `DQ-D1-7` | accepted | Insert File receives transient unit provenance from the inserted document, because it replaces the internal copy buffer. Different physical construction meanings show the same non-blocking notice as an ordinary cross-document paste. Never rescale geometry, convert coordinates or modify the target units. A later Ctrl+V from the same buffer keeps that provenance until the buffer is replaced or cleared. |
| `DQ-D1-8` | accepted | The persisted `usmMetersPerUnit` grammar and the Document Units factor input use the `D0` §8.4 numeric grammar; the decimal separator is `.`. A locale decimal comma is not converted silently, because that would create a second lexical interpretation; it is rejected with a clear localized hint (the equivalent of "Use "." as the decimal separator."). No grouping separators are accepted. The canonical writer stays locale-independent. |
| `DQ-D1-9` | accepted | The mismatch notice is a transient presentation segment of the existing GeoCeDG status bar, with a stable id such as `paste-notice`; no serialization, undo, geometry, sound or modal dialog; it names source and target units enough to explain the mismatch; it clears after about 10 seconds, on the next paste or on the next document transition; it is Swing/EDT-safe; its lifecycle is testable without real 10-second sleeps; it stays distinct from the permanent `layer | construction-unit | presentation-unit` segments. |
| `DQ-D1-10` | accepted | No immediate warning merely because older GeoCeDG or Classic readers may drop unit metadata on re-save. The limitation stays recorded in the `D1` candidate evidence and author-smoke checklist; user-guide treatment stays an obligation of `G` (`D0` closeout). |
| `DQ-D1-11` | accepted with an explicit boundary correction | `geocedgUnits` is shared document semantics: the shared reader, writer, validation and fail-closed rules also apply to the Classic diagnostic application built from this source tree. In a Classic diagnostic session no unit-specific UI and no unit-specific geometry behavior is added; no document with `EMPTY` unit state changes bytes because of `D1`; a valid document carrying `geocedgUnits` is read and preserved per the shared `D0` contract; recognized malformed or future unit metadata follows the same fail-closed semantics. That shared preservation is allowed `D1` behavior. Forbidden are Classic-specific unit UI, geometry semantics, orchestration or feature expansion beyond the shared document-semantic reader/writer behavior required by `D0`. For Web: no new frontend or product behavior claim; shared code stays compatible with its build constraints; no Web-specific unit UI or behavior. If the shared implementation requires a Web-specific semantic fork or materially broadens frontend scope, the phase stops and reports. |

## Binary64 canonical writer

The preparation proves that JDK 17 `Double.toString` cannot satisfy the `D0`
byte contract. `D1` implements the deterministic shared canonical writer
characterized in `C5`, tested against the fixed reference corpus and the
round-trip properties. `Double.toString`, `String.valueOf(double)`, implicit
`double` string concatenation and locale-sensitive formatters are not used for
the persisted `usmMetersPerUnit`. The exact `D0` §8.4 semantics hold on every
supported JDK.

## Characterization accepted as implementation constraints

`C1`–`C11` are accepted as preparation evidence, to be re-established against
the exact execution base before they are relied on. In particular:

- the effective load purpose, not `clearConstruction`, governs unit
  application;
- archives with macros preserve document units;
- File → Open preflight stays the primary live-document protection;
- the `MyXMLio` restore extends to unit rejection for non-native routes;
- defaults apply only after a blank-document transition, and the undo baseline
  is retaken;
- one shared `UnitState` per `Construction`;
- no unit metadata in macro, clipboard or preferences XML;
- clipboard provenance is Desktop/session-only;
- units never become algorithm dependencies;
- `drawingScale` stays entirely outside `D1`.

## Additional stop conditions

Beyond every stop condition of the prompt, the phase stops if implementing the
`DQ-D1-5` replacement requires materially redefining general save semantics
beyond a narrow document-metadata seam, or if satisfying `DQ-D1-11` requires
frontend-specific Classic or Web unit semantics instead of shared document
semantics.

## Author smoke checklist (required content)

The candidate report provides an explicit checklist covering at least:
new-document defaults; the Document Units dialog; standard-unit changes;
define, edit, switch and remove `usm`; status segments and tooltips; undo and
redo; the unsaved prompt on an otherwise empty document after a unit change;
Open of valid unit-bearing documents; rejection of malformed or newer metadata;
paste after New/Open with matching and mismatching units; the Insert File
mismatch notice; legacy document behavior; Classic diagnostic preservation; and
confirmation that the current picture, DXF and LaTeX export behavior has not
changed.

## Authorization state

```text
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-D0         = PASS — AUTHOR APPROVED — PUBLISHED
D1 preparation package     = PASS — AUTHOR APPROVED               (not published)
PRE-G9B-R6-plus-D1         = AUTHORIZED FOR IMPLEMENTATION
PRE-G9B-R6-plus-A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.

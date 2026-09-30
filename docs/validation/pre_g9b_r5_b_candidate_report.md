# PRE-G9B-R5-B — canonical English command surface: candidate report

```text
TECHNICAL_CANDIDATE_STATE = PREPARED FOR ONE CLEAN IMMUTABLE COMMIT
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R5-B
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile INTEGRATION
ADDITIONAL_REQUIRED       = tools/agent/verify.ps1 -Profile STATIC (guide-structure diagnostic)
ACCEPTED_COMPATIBILITY_ADR = e16697d96858639becf2dc72f7c7beaca8fcbd24
ADR_DECISION_CONTENT      = c459de9ab66d61fe849abbf9b9b8097c56605c48
GUIDE_IMPACT              = UPDATED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval. The candidate commit cannot name itself, so
its exact identity and the post-commit `INTEGRATION` evidence are reported
outside this file. The machine-readable mirror is
[`pre-g9b-r5-b-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-candidate-evidence.json).

The report separates five kinds of statement: accepted ADR policy (§3),
source facts observed at `P_DOTXML` (§4), implementation decisions (§5–§12),
regression evidence (§13–§16) and retained observations and pending author
decisions (§18–§19).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R5-B` for implementation and technical
verification on 2026-09-30, on the published `Dot`/XML maintenance closeout.

| Identity | Value |
|---|---|
| implementation base `P_DOTXML` | `504a19f7e655ef50c4bfa8095a07f94c11b27a01` |
| base tree | `63e7f0cd79aa9e1cf1259b6466e8ac16dc261704` |
| linear published history | `e16697d9…` → `4f9bc944…` (`M_DOTXML`) → `504a19f7…` |
| local `main` = `origin/main` = live remote `main` after `git fetch` | `504a19f7e655ef50c4bfa8095a07f94c11b27a01` |
| worktree at entry | clean |
| local branch (not pushed; not identity) | `phase/pre-g9b-r5-b-canonical-english-command-surface` |

Entry states re-read from disk before any edit: ADR 0031
`ACCEPTED — AUTHOR APPROVED` (closeout `e16697d9…`, decision content
`c459de9a…`); `DOT/XML MAINTENANCE = PASS — AUTHOR APPROVED` with its `R5-B`
prerequisite recorded as satisfied on publication; `PRE-G9B-R5-B =
DESIGNED — NOT AUTHORIZED` before this instruction. No identity or authority
differed.

The class was frozen before any productive change. Section 12.8 of
`geocedg/specs/operations/verification-levels.md` maps `INTEGRATED_PHASE` to
PHASE plus COMPOSED (`INTEGRATION`) when the frozen plan names concrete
integration coverage. It does here: the change crosses shared localization,
Desktop presentation, scripts and the existing localization, parser, XML,
redefine and help regressions of both JUnit modules. `INTEGRATION` executes the
complete `final.shared` and `final.desktop` selections, so it completely
executes the phase's focal tests in the same cohort. No PHASE selection is
registered and no escalation was requested; the governance did not contradict
the author's classification. Because both guide editions change and the
`diagnostic.guide-structure` leaf belongs to `STATIC` only, `STATIC` also runs
separately on the exact candidate.

## 2. Canonical prompt amendment

The first action, before any product source change, amended
`.github/prompts/tasks/pre-g9b-r5-b-canonical-english-command-surface.prompt.md`
from the proposed stub (blob `34e1796d5628108498a7c612c155d5c8d72a4c16`) to the
canonical execution prompt for this authorized instance (blob
`43901e3b8fe47f770d791097c5d5ecc6678b36aa`). It resolves `IMPLEMENTATION_BASE`,
`IMPLEMENTATION_BASE_TREE`, `ACCEPTED_COMPATIBILITY_ADR`, `ADR_DECISION_CONTENT`
and `DOT_XML_PREREQUISITE`, records `implementationAuthorized=true`,
`authorApproved=false`, `passClaimed=false`, `selfApproved=false`, and freezes
the class. It corrects the superseded pre-ADR statements: ADR 0031 exists and is
accepted; the `Dot`/XML maintenance is published; `SplineV2` has an ES bundle
entry equal to its English name; the Algebra prefix literal is out of scope; the
localized `DOT` XML head is repaired at the base; `XML`-strategy command strings
accept exact-case enum constants, not localized aliases. It carries the gate,
typed re-entry, autocomplete-identity and dynamic-text obligations, the author
decisions on placeholders, function names and captions, and every `T-*`
obligation. It references ADR 0031 as the policy and is not a second policy
document.

Validation before any source change: the repository prompt-contract parser
(`Test-PromptContractDocument`, `task` profile) found all seven execution-safety
fields, ordered, none missing or duplicated, `execution_safe = true`;
`tools/agent/checks/prompt-execution-safety.ps1` returned `CONTRACT_SATISFIED`,
exit 0; every repository-relative link resolves; the file is LF with no
trailing whitespace.

## 3. Accepted policy (ADR 0031, not re-decided)

Alternative D. For every command identity `k`, `E(k)` is the English
command-bundle value. GeoCeDG displays, suggests, inserts and documents `E(k)`
in every UI language; localized names stay `USER` input aliases where the
current lookup accepts them. `USER`, `SCRIPT` and `XML` lookup, the reverse
table and the parser are unchanged; canonical display is admitted by a
complete-inventory gate and a typed fail-closed re-entry outcome
`CANONICAL_HEAD_SHADOWED`. Syntax placeholders, mathematical function names,
captions and automatic labels stay in the UI language. GGBScript semantics are
canonical English/internal. XML and serialization are unchanged.

## 4. Source characterization at `P_DOTXML`

**Relative to the ADR's characterization base.** Between `P_R5A`
(`4389b30b…`) and `P_DOTXML` the only productive `source/` change is the
`Dot` head in `ExpressionSerializer` (`M_DOTXML`); the other two changed paths
are the maintenance's tests. The ADR's print-site inventory therefore still
holds; the inventory below was redone from source at `P_DOTXML`.

**Command-name authorities.** `Localization.getCommand` is the UI-locale bundle
and was the display head of every localizing template.
`Localization.getEnglishCommand` is the JRE enum mapper (six divergences from
the English bundle, e.g. `Binomial`→`nCr`). The English bundle is
`command_en.properties` over the English base `command.properties`.

**Inventory (measured, mechanized by the gate test).** 564 `Commands`
constants; 513 have an English command-bundle entry; 486 are offered by the
input-bar dictionary (3D included, the ADR's displayed inventory); 37 stored
names have `E(k)` different from the internal identifier.

**Print and insertion sites by role.** Every `getCommand(` call and every
`isPrintLocalizedCommandNames()` branch of `shared/common`, `shared/common-jre`
and `desktop/desktop` was classified:

| Role | Sites | R5-B |
|---|---|---|
| DISPLAY | `AlgoElement.getDefinition` and the Vector wrapper; `AlgoLocusStroke`; `Command.toString`; `ExpressionSerializer` `Dot`, `If`, `DataFunction` display heads; `GeoPieChart` value; `AlgoLocusMetricScalarAdapter` (`Length`); `CommandErrorMessageBuilder`, `ErrorHelper.handleCommandError`, `CommandNotFoundError`, `CAScmdProcessor` heads; `GeoCasCell` eval head; syntax head of `LocalizedCommandSyntax` | head from the command-head authority |
| EDITABLE | `getRedefineString`, `getDefinitionForInputBar`, `getDefinitionForEditor` (through the display sites); autocomplete insertion; Input Help paste | canonical; typed re-entry |
| SCRIPT DISPLAY | `GgbScript.script2LocalizedScript` (editor and CAS-cell input display); `GeoGebraLexer` highlighting set | canonical display; canonical-first save |
| USER INPUT | `Command(kernel, name, true)` → `App.getReverseCommand` → reverse table → `lookupInternal` | unchanged |
| SCRIPT INPUT | `Commands.lookupInternal` under `CommandLookupStrategy.SCRIPT` | unchanged |
| XML/PERSISTENCE | `xmlTemplate` and every non-localizing template; `MyXMLHandler`; script XML; the non-localizing `Dot` head of the maintenance | unchanged |
| INTERNAL REPARSE | `RelativeCopy` (`maxPrecision`, XML strategy); `RemoveSlider`, `EuclidianStyleBarStatic` (localized text, USER); `CASCellProcessor` `Delete` fix-up; `FunctionParser.isCommand`; `CommandSyntaxLookupImpl`; `GeoCeDGUserToolLibrary.nativeCommand` | unchanged; `E(k)` re-enters every regime |
| MATCHERS OF DISPLAY OUTPUT | `DynamicTextProcessor`, `DynamicTextInputPane` (`FormulaText(`, `Name(` prefixes) | same authority as the display |
| CAS/EXPORT | GIAC, MathML, LaTeX function names, PGF/PSTricks | unchanged |
| OTHER (UI prose, ADR decision 1) | `TextDispatcher` label stems, `FunctionInspectorModel`, `CellRangeUtil`, `Relation`, `CASSubDialogD`, `PropertiesPanelD`, `Statistic.getLocalizedName`, `AlgoProveDetails` condition texts, axis labels, `loc.getFunction` names | unchanged, UI language |
| WEB ONLY | `AutocompleteProvider`, `InputBarHelpPanel` (common), `EnglishCommandSyntax` | unchanged |

**Findings of this re-inventory, beyond the ADR text.**

- `englishToInternal` merges `SD`/`stdevp`, `SampleSD`/`stdev`, `MAD`/`mad`,
  `Mean`/`mean` and `Binomial`/`BinomialCoefficient`/`nCr`, and the processor
  factories send each group to one processor. After the CAS fill the reverse
  table maps `SD` to `stdevp` (etc.): a different constant of the same identity.
  The gate compares identities, as the ADR's terminology defines them.
- `Mean` and `mean`, and `MAD` and `mad`, are both offered; the inherited
  dictionary is case-insensitive and keeps the later name. Presentation entries
  follow the same rule, so English lists are unchanged.
- The CAS pass offers `BinomialCoefficient` and `nCr` constants with no English
  entry (§18); both belong to the identity `nCr` already offered by `Binomial`.
- Some bundle syntax lines hard-code an English head (e.g. `Point.SyntaxCAS`);
  under ES they were already English at the base. No bundle line hard-codes a
  Spanish head.
- `CSolve` and `CSolutions` are offered but have only CAS syntax
  (pre-existing, ADR report §4.4).
- A tool (macro) whose name equals a command key was printed through
  `getCommand` by the base (e.g. a tool `Mirror` as `Refleja`). Under the
  GeoCeDG profile tool names are now printed verbatim (ADR decision 1).

## 5. Architecture and API

Shared-kernel localization seams plus Desktop consumers; not the parser, not the
`Command` strategies, not reverse-table construction, not XML parsing, not any
serialization role.

```text
AppConfig.presentsCanonicalEnglishCommandHeads()      product-profile attribute (default false)
AppConfigGeoCeDG                                      true
Localization.isCanonicalEnglishCommandHeads()         JRE: read from the bound application's config
Localization.getCanonicalEnglishCommand(k)            E(k) | null (NO_ENGLISH_PUBLIC_NAME)
Localization.getCommandHead(k)                        canonical: E(k), verbatim when none
                                                      otherwise: getCommand(k) (upstream)
Localization.recordCommandHeads(presentation)         head -> key printed, for typed re-entry
App.getOfferedCommands(cas) / getCasTableCommands()   identities recorded by the existing
                                                      dictionary passes
org.geocedg.common.main.command.*                     CanonicalCommandSurface, CanonicalCommandEntry,
                                                      CanonicalCommandReentry, CanonicalCommandHeadGate,
                                                      CanonicalCommandSurfaceError
```

Four concepts stay separate: identity (a `Commands` constant, up to
`englishToInternal`), `E(k)`, the UI-language alias, and the presentation
context (template, editor, completion, help). A localizing template calls
`getCommandHead`; a non-localizing one keeps printing the internal name, so no
serialization role changes. `StringTemplate.localizeCmds` is not the switch.
Identity flows structurally: printers pass the command key; completion and help
entries carry the constant; the re-entry guard records the key each printed head
came from. No consumer re-derives identity from displayed text with
`getInternalCommand` or `getReverseCommand`. The Classic profile keeps the
upstream behaviour on every modified path.

## 6. Changed paths

**Shared kernel (upstream, modified).** `main/Localization.java`,
`jre/main/LocalizationJre.java`, `main/AppConfig.java`, `main/App.java`,
`main/syntax/LocalizedCommandSyntax.java`, `kernel/algos/AlgoElement.java`,
`kernel/algos/AlgoLocusStroke.java`, `kernel/arithmetic/Command.java`,
`kernel/arithmetic/ExpressionSerializer.java`,
`kernel/statistics/GeoPieChart.java`,
`main/localization/CommandErrorMessageBuilder.java`,
`main/error/ErrorHelper.java`, `kernel/commands/CommandNotFoundError.java`,
`kernel/commands/CAScmdProcessor.java`, `kernel/commands/AlgebraProcessor.java`,
`plugin/script/GgbScript.java`, `kernel/geos/GeoCasCell.java`,
`gui/inputfield/DynamicTextProcessor.java`,
`gui/dialog/options/model/ScriptInputModel.java`; `menu.properties`,
`menu_en.properties`, `menu_es.properties` (two messages each, LF lines).

**Shared kernel (GeoCeDG-owned).** New package
`org.geocedg.common.main.command` (five classes); `AppConfigGeoCeDG`;
`AlgoLocusMetricScalarAdapter`.

**Desktop (upstream, modified).** `gui/inputfield/AutoCompleteTextFieldD.java`,
`gui/inputbar/InputBarHelpPanelD.java`, `gui/inputbar/AlgebraInputD.java`,
`gui/editor/HelpOnKeywordPanel.java`, `gui/editor/GeoGebraLexer.java`,
`gui/DynamicTextInputPane.java`. **Desktop (GeoCeDG-owned).** `AppGeoCeDG`.

**Tests.** New: `PreG9BR5BFingerprints`, `PreG9BR5BCanonicalCommandGateTest` (6),
`PreG9BR5BCanonicalDisplayTest` (10), `PreG9BR5BCanonicalReentryTest` (5),
`PreG9BR5BCanonicalHelpTest` (4), `PreG9BR5BCanonicalScriptTest` (4); resources
`pre-g9b-r5-b/pre-g9b-r5-b-base-fingerprints.json` and
`pre-g9b-r5-b-historical-es.cedg`. Narrowly updated (§16):
`DotXmlLocaleIndependentPersistenceTest`, `PreG9BR4SplineV2AuthoringTest`,
`G9U1MetricReviewTest`.

**Guides.** `docs/user/geocedg_user_guide_en.md` and `_es.md` §16.3;
`docs/developer/geocedg_developer_guide.md` (one paragraph).

**Governance and records.** The canonical R5-B prompt (§2);
`docs/roadmap/geocedg_roadmap.md` (status only); `docs/upstream/modified-files.yml`;
`geocedg/specs/operations/verification-junit-inventory.json` and the registry
pin (§17); this report and its evidence mirror. ADR 0031, `AGENTS.md`,
`CLAUDE.md` and every other prompt are untouched.

## 7. Upstream boundary

The 19 newly modified upstream files were each byte-identical to the upstream
baseline `9b93256b7df401ff056c37b502d82df4d72b1522` at `P_DOTXML` (baseline blob
= base blob; the evidence mirror lists them). Each edit is the minimum at its
site; unchanged branches serve the Classic profile. `docs/upstream/modified-files.yml`
gains 32 entries (19 modified, 13 added) and phase-tagged purposes on 15
existing entries: 856 registered files. `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b…` reports `Controlled upstream boundary: 856
registered file(s).`

## 8. `E(k)`

`LocalizationJre.getCanonicalEnglishCommand(k)` reads `k` from the command bundle
created for `Locale.ENGLISH` through the localization's own bundle factory
(`command_en`, then the base), independent of the UI language and of the JVM
default locale; a key containing `.` (a syntax key) and a missing key give
`null`. `getCommandHead(k)` returns `E(k)` under the GeoCeDG profile. A name
without an English entry (a tool name, an unresolved name) is printed verbatim,
as the English UI prints it; the gate proves that no displayed identity lacks
`E(k)`, so no internal identifier is printed by fallback. Under EN the value is
exactly the base English display for all 564 constants (`T-DISPLAY`, from the
base fixture). The JRE `getEnglishCommand` and its callers are unchanged.

## 9. Collision gate and fail-closed re-entry

**Gate (`CanonicalCommandHeadGate`, decision 5).** For every stored name `k` with
`h = E(k)` (513): `USER` resolves `h` to identity(`k`) in every captured
reverse-table state (`HEAD_NOT_SELF_RESOLVING`), with the same identity in every
state (`HEAD_STATE_DEPENDENT`); `SCRIPT` resolves it (`HEAD_NOT_SCRIPT_RESOLVABLE`);
it is an exact-case enum constant of the identity (`HEAD_NOT_XML_EXACT`); the
canonical-first script save keeps the identity
(`SCRIPT_SAVE_NOT_IDENTITY_PRESERVING`); no parser function of arity 0–8
pre-empts it except `nCr/2` and `nPr/2` (`HEAD_PREEMPTED_BY_PARSER_FUNCTION`); no
other identity shares its lower-case form (`HEAD_NOT_INJECTIVE`). Every one of
the 486 displayed names has `E(k)` (`NO_ENGLISH_PUBLIC_NAME`). Result: zero
findings for EN and ES, fresh and CAS-filled. Negative control: the same gate on a
Classic French application reports `HEAD_NOT_SELF_RESOLVING` for `Intersection`
(French `Intersection` → `Intersect`). The EN/ES emptiness is checked, not
relied on.

**Typed re-entry (`CanonicalCommandReentry`, decision 6).** At the single
string redefinition chokepoint
(`AlgebraProcessor.changeGeoElementNoExceptionHandling(String…)`), under the
GeoCeDG profile and the `USER` regime, the element's editable and display text
is presented again with the head recorder on. Each canonical head the
submission keeps immediately before `(` or `[`, outside strings, must re-enter
as the key it was printed for. Before parsing: an existing label or CAS label
of that name (the parser applies it before any command, `(` form only), a
`USER` resolution to another identity, or a tool on the resolved name (the
dispatcher consults tools first). After parsing and before evaluation: a
function variable of that name. Any of these raises
`CanonicalCommandSurfaceError` with outcome `CANONICAL_HEAD_SHADOWED`, the head,
what shadows it and the expected identity, through the ordinary error path:
nothing is parsed into the construction, evaluated, stored or undone. Heads the
user rewrote follow ordinary `USER` semantics. Implementation decision:
presentation provenance is bound to the element being redefined; text inserted
into a new input carries none.

## 10. Syntax help

`LocalizedCommandSyntax` takes the head from the command-head authority and the
body from the UI-language `.Syntax`, `.Syntax3D` or `.SyntaxCAS` key, English
base as parent; its filters are unchanged. `CanonicalCommandSurface.syntaxLines`
returns an empty list — the typed "no syntax" result — whenever the resolved text
is still a key, so no surface shows `X.Syntax`. `EnglishCommandSyntax` is not
used on Desktop. Consumers: Input Help syntax pane, `focusCommand`,
`HelpOnKeywordPanel`, autocomplete, command errors (`CommandErrorMessageBuilder`
syntax, `ErrorHelper`). Under ES every syntax line of every command equals the
base ES line with the ES head replaced by `E(k)`; bodies are unchanged.

## 11. Autocomplete identity

`CanonicalCommandSurface.completions(app, prefix, cas)` builds one
`CanonicalCommandEntry` per offered constant (identity, `E(k)`, UI-language
alias), from `App.getOfferedCommands`, which the existing dictionary pass
records. It matches the prefix against the head and the alias like the inherited
dictionary (case- and accent-insensitive substring, word starts first).
`AutoCompleteTextFieldD` shows and inserts the canonical syntax lines of each
entry's identity; with a bracket after the word only the head is inserted. F1
help selects the entry whose head or alias is the first name starting with the
word, as the inherited lookup did, and shows help for its carried identity.
Input Help leaves are entries; selection, online help, paste and `focusCommand`
use the carried identity. The script-editor lexer adds the canonical heads. The
Korean and Classic paths are unchanged. Tools are not offered, as at the base.

## 12. Scripts

`SCRIPT` resolution and `GgbScript.run` are unchanged. Display: each command
token is shown by the command-head authority (a stored `OrthogonalLine` shows
`PerpendicularLine`). Save under the GeoCeDG profile (decision 10): a token
`SCRIPT` resolves keeps its identity and is stored as written, except that a
displayed canonical head `E(k)` (any case) is stored as the internal name `k`;
only a token `SCRIPT` cannot resolve is converted through UI-language aliases;
a token that `SCRIPT` resolves to one identity while being an alias of another
is rejected with `SCRIPT_TOKEN_AMBIGUOUS` before the stored script changes.
`ScriptInputModel` keeps an untouched displayed script unchanged. Effects: the
base ES retarget of a stored `Perimeter` to `Circumference` on an untouched save
ends, and an untouched EN or ES script saves byte-identical (the base EN
rewrote a stored `PerpendicularLine` to `OrthogonalLine`). Localized aliases do
not become a script vocabulary. CAS-cell input follows the same conversion.

## 13. XML and persistence

No serialization code path changed: non-localizing templates print internal
names exactly as before, and the maintenance's `Dot` rule is untouched. Evidence
against the base fixture: (1) for fresh EN- and ES-authored constructions of the
42-object witness, the command, input, output and expression lines of the
construction XML equal the `P_DOTXML` lines exactly; (2) the Spanish-UI native
document saved at `P_DOTXML` reopens under EN and ES with construction XML
byte-identical to its `P_DOTXML` reopening, identical values for every witness,
the same stored click script, and re-saves byte-identically.
`TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` is retained unchanged.

## 14. GeoCeDG commands

`LocusV2`, `LocusLength`, `SplineV2` and the `Length` adapter use the host
mechanism only. Under ES: `LocusV2(Q, s, D)`, `LocusLength(L)`, `Length(L)`,
`SplineV2(l1, b)`; syntax `LocusV2( <Punto dependiente>, … )`; aliases
`LugarGeométricoV2` and `LongitudLugarGeométrico` still accepted; `SplineV2`'s ES
entry equals its name and no alias was invented. `AppGeoCeDG` focuses the
SplineV2 Input Help entry by identity. `GeoCeDGUserToolLibrary.nativeCommand`
and the English literal value strings are unchanged.

## 15. `T-*` obligations

| ID | Result | Evidence |
|---|---|---|
| `T-DISPLAY` | PASS | `PreG9BR5BCanonicalDisplayTest` `englishDisplayEditableTextAndSyntaxAreUnchangedFromTheBase`, `spanishDisplayAndEditableTextUseCanonicalEnglishHeads`, `everyKernelPrintSiteUsesTheCanonicalHeadUnderSpanish`; gate test `canonicalEnglishNameIsTheEnglishCommandBundleValue` |
| `T-INPUT` | PASS | gate test `reverseTablesAndLookupRegimesAreIdenticalToTheImplementationBase` (EN/ES × fresh/CAS-filled tables 532/557/983/1029 entries and 1041-token `USER`, `SCRIPT`, exact-`XML` resolutions equal `P_DOTXML`), `everyCanonicalHeadAndEverySpanishAliasIsAcceptedAsItsIdentity` |
| `T-GATE` | PASS | `gatePassesOverTheCompleteInventoryInBothLanguagesAndBothTableStates` (0 findings; 564/513/486); `gateDetectsTheRetainedFrenchRetargetAsNegativeControl` |
| `T-COLLISION` | PASS | `perimetroObservationIsUnchangedAndCanonicalHeadsAreUnaffected` |
| `T-REDEFINE` | PASS | `PreG9BR5BCanonicalReentryTest` `presentedEditableTextSubmittedUnchangedKeepsIdentityInBothLanguages` (24 witnesses × EN/ES), `argumentOnlyEditsKeepTheCommandIdentity` (10) |
| `T-SHADOW` | PASS | `labelShadowingFailsClosedWithoutMutation`, `functionVariableShadowingFailsClosedWithoutMutation`, `macroShadowingFailsClosedWithoutMutation` (typed outcome; XML, parent, undo history unchanged; no undo store) |
| `T-AUTOCOMPLETE` | PASS | `PreG9BR5BCanonicalHelpTest` `autocompleteDisplaysAndInsertsCanonicalHeadsAndFindsSpanishAliases`, `englishAutocompleteKeepsItsCanonicalHeadsAndEnglishBodies` |
| `T-SYNTAX` | PASS | `everyDisplayedCommandHasACanonicalSyntaxAndNeverALiteralKey`, `inputHelpFocusAndKeywordHelpAgreeOnCanonicalSyntax`; display test `spanishSyntaxHasCanonicalHeadsAndUnchangedSpanishBodies` |
| `T-XML` | PASS | `freshConstructionsPersistTheBaseCommandIdentitiesInBothLanguages`, `historicalSpanishDocumentReopensAndSavesIdenticallyToTheBase` |
| `T-SCRIPT` | PASS | `PreG9BR5BCanonicalScriptTest` (4); `SCRIPT` resolution fingerprint of `T-INPUT` |
| `T-GEOCEDG` | PASS | `geoCeDGCommandsAndTheLengthAdapterFollowTheHostPolicy`; GeoCeDG witnesses in every test above |
| `T-ERROR` | PASS | `errorMessagesNameTheCommandByItsCanonicalHeadWithSpanishProse` |
| `T-DYNTEXT` | PASS | `dynamicTextClassificationIsUnchangedUnderSpanish` |
| `T-L01` | PASS unchanged | `G9U0LocalizationHelpTest.l01CommandNamesAndSyntaxAreLocalizedInEnglishAndSpanish` |
| `T-SMOKE` | PENDING (author) | checklist §20 |

The base fixture was produced by a scratch probe on the unchanged source tree
and reproduced on a `git archive` extraction of `P_DOTXML`; every fingerprint
matched between the two runs. The probe itself is not committed.

## 16. Test results before the candidate commit

| Run | Result |
|---|---|
| `:shared:common-jre:test` (complete) | 6 850 tests, 0 failures, 0 errors, 10 skipped |
| `:desktop:desktop:test` (complete) | 1 630 tests, 0 failures, 0 errors, 1 skipped |
| new R5-B classes | 29 tests, all passing |
| adjacent: `DotXmlLocaleIndependentSerializationTest` (8), `DotXmlLocaleIndependentPersistenceTest` (3), `CommandErrorMessageBuilderTest` (2), `G9U1MetricReviewTest` (6), `G9U0LocalizationHelpTest` (4), `PreG9BR4SplineV2AuthoringTest` (21) | all passing |
| Checkstyle `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings |
| guide structure diagnostic | `DIAGNOSTIC_CLEAR`: 17 markers, 1/16/82 headings, 99 aligned entries per edition |
| upstream boundary | 856 registered files, no unexpected or missing path |
| `verify.ps1 -Profile INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE`, 22/22, 0 diagnostics (`verification-acbcdcb8d61647dda56d7544fdf98f61`) |
| `verify.ps1 -Profile STATIC` on the staged tree | `ACCEPTED / COMPLETE`, 3/3; `diagnostic.guide-structure`, `diagnostic.documentation`, `diagnostic.style` and the whitespace diagnostic `DIAGNOSTIC_CLEAR`; only the standing `diagnostic.governance` finding and `diagnostic.historical-consistency` unavailability (`verification-3899366ed9044be481cdf6cd9eab8c28`) |
| `verify.ps1 -PlanOnly` | `INTEGRATION` 46 nodes, `STATIC` 10, `FINAL` 68, all `COMPLETE`; plan hashes unchanged from the base |

Narrow updates of existing tests, each because ADR 0031 intentionally changes
Spanish command presentation, with no persistence assertion weakened:
`DotXmlLocaleIndependentPersistenceTest` now expects `Dot(u, v)` as the ES
display of the Dot built from `ProductoEscalar(u, v)`;
`PreG9BR4SplineV2AuthoringTest` asserts the SplineV2 Input Help leaf by its
carried identity and its shown head; `G9U1MetricReviewTest` expects the `Length`
head from the command-head authority and that it is `Length` in both languages.
`CommandErrorMessageBuilderTest`, which mocks `Localization`, is untouched: the
builder keeps the upstream `getCommand` call outside the canonical profile.

## 17. JUnit inventory and registry pin

The 29 new Desktop tests change `discovery.desktop` and `final.desktop` only; no
registered selection filter matches the new classes and no shared test was
added. The inventory was regenerated with the official updater from real
evidence: `discovery.shared` and `discovery.desktop` dry runs and an executed,
passing `final.desktop` run; `final.shared` is unchanged. The registry
`junit_inventory` pin was reproduced for the base inventory first and then
recomputed with `Get-VerificationCanonicalTextSha256`: `discovery.desktop` 1 601 → 1 630
identities, `final.desktop` 1 595 → 1 624 (executed run: 1 624 tests, 0 failures, 1
allowlisted skip), every other selection unchanged; pin `02c9aa36…` → `cc5ab6c7…`.
The `INTEGRATION`, `STATIC` and `FINAL` plan hashes are unchanged (`4502c4bd…`,
`25dfacc8…`, `3d8752b9…`). The hashes are in the evidence mirror; none was edited
by hand.

## 18. Retained observations and debt

| Identifier | Disposition |
|---|---|
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | retained unchanged; R5-B repairs no historical localized `Dot` XML |
| `HZ-R5B-ALIAS-CAS-STATE` (`Perímetro`) | retained compatibility observation; unchanged and not worsened |
| `OBS-R5B-CAS-TABLE-VERBATIM-HEADS` | the CAS pass offers the constants `BinomialCoefficient` and `nCr`, which have no English command-bundle entry; they are the identity `nCr`, already listed through `Binomial` (`BinomialCoefficient`), so the CAS list adds only a verbatim `nCr`, identical in EN and ES and at the base; outside the ADR's 486-command inventory |
| `OBS-R5B-HARDCODED-SYNTAX-HEADS` | some English base syntax values hard-code their head (e.g. `Point.SyntaxCAS`); already English under ES at the base; none hard-codes a Spanish head |
| `CSolve` / `CSolutions` Algebra syntax | pre-existing literal key at the base; now the typed "no syntax" result; their CAS syntax is shown |
| `OBS-R5B-PROVER-DETAILS-CAPTIONS` | `AlgoProveDetails` condition texts keep UI-language command names, classified by ADR 0031 as captions |
| `OBS-R5B-INPUT-BAR-INSERTED-TEXT` | presentation provenance is bound to the redefined element; text inserted into a new input is ordinary `USER` input (§9) |
| `HZ-R5B-HARNESS-TABLE-FRESHNESS` | the new tests switch language with `setLanguage` and refresh the dictionary |

The ledger of the ADR 0031 and `Dot`/XML closeouts carries forward unchanged.

## 19. Pending author decisions

Approval, amendment or rejection of the exact candidate commit, after EN/ES
author smoke. The implementation decisions of §9 (provenance bound to the
redefined element), §12 (canonical head stored as the internal name on an
edited save) and §18 (CAS-table verbatim heads outside the gate inventory) are
open to the author's review.

## 20. EN/ES author-smoke checklist

Spanish UI (**Options → Language → Español**):

1. Type `c=Circunferencia((0,0),2)`: the Algebra view and the Construction
   Protocol show `Circle((0, 0), 2)`.
2. Type `p=Perpendicular((1,1), Recta((0,0),(1,0)))`: it creates a
   perpendicular line shown as `PerpendicularLine(…)`.
3. Redefine `c` (double-click or right-click → Redefine): the dialog shows
   `Circle((0, 0), 2)`; change `2` to `3` and confirm: still a circle.
4. In the Algebra Input type `Refle`: suggestions start with
   `Reflect( <Objeto>, <Punto> )`; choosing one inserts the English form.
5. Type `LugarGeom`: suggestions show `LocusV2( <Punto dependiente>, … )`.
6. Open Input Help: the tree lists English command names; selecting
   `PerpendicularLine` shows `PerpendicularLine( <Punto>, <Lado …> )` with
   Spanish argument descriptions.
7. Type `k=Circunferencia((0,0),sen(1))`: it is shown as
   `Circle((0, 0), sen(1))`.
8. Build `s=0`, `Q=(s,0)`, `D={false,{-2,2,true,true}}`,
   `L=LugarGeométricoV2(Q,s,D)`, `M=LongitudLugarGeométrico(L)`,
   `n=Longitud(L)` and a `SplineV2` from four points: they are shown as
   `LocusV2(…)`, `LocusLength(L)`, `Length(L)`, `SplineV2(…)`.
9. Save as `.cedg`, close, reopen in Spanish and in English: same objects and
   values.
10. Give a button a GGBScript click script `Perímetro(c)`, close and reopen the
    script editor: it shows `Circumference(c)`; closing it unchanged keeps the
    script unchanged.
11. Optional: create `Circle=5`, then redefine `c` without editing the command:
    the edit is refused with `CANONICAL_HEAD_SHADOWED` and nothing changes.

English UI: repeat 1, 3, 4, 6, 8 and 9 with English inputs; the display,
autocomplete, Input Help and saved documents are as before R5-B.

## 21. Impact declarations

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md;
  docs/user/geocedg_user_guide_es.md;
  docs/developer/geocedg_developer_guide.md
GUIDE_JUSTIFICATION = user-visible command presentation policy (§16.3 of both
  editions, same heading and anchor; packaged copies are byte copies made by
  processResources); developer API pointer

BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, Conda,
  packaging, download or build/verification entry point changed.

VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY
Rationale: the JUnit inventory and its registry pin were regenerated through
  the official updater for the new Desktop tests; no verifier, profile, leaf,
  schema or phase selection changed.

PRODUCT_PHASE_EFFECT = BOUNDED — canonical English command presentation under
  the GeoCeDG profile; lookups, parser, reverse table and serialization unchanged
```

## 22. State

```text
PRE-G9B-R5-B     = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR SMOKE     = PENDING
AUTHOR APPROVAL  = PENDING
promotion        = NONE
PRE-G9B-R6       = NOT AUTHORIZED
PRE-G9B-R7       = NOT AUTHORIZED
G9B              = NOT AUTHORIZED
```

Push, branch publication, merge, promotion, tag, release and binary
publication each require a separate explicit author instruction naming the
exact candidate SHA.

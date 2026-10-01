# PRE-G9B-R6 — GGBScript compatibility gate: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = OPERATIONAL_VERIFICATION_INFRASTRUCTURE
frozenAtPhaseStart        = true
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile INFRA_UNIT
                            tools/agent/verify.ps1 -Profile STATIC
                            tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6
FINAL                     = NOT RUN (not required by the frozen PHASE_LOCAL plan)
GUIDE_IMPACT              = NONE
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
SERIALIZATION IMPACT      = NONE
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval. The candidate commit cannot name itself, so
its identity and the acceptance runs made on it are reported outside this file.
The machine-readable mirror is
[`pre-g9b-r6-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6/pre-g9b-r6-candidate-evidence.json);
the capability matrix itself is
[`ggbscript-capability-matrix.json`](../../geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json)
with its schema
[`ggbscript-capability-matrix.schema.json`](../../geocedg/specs/operations/ggbscript-capability-matrix.schema.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6` for implementation and technical verification
on 2026-10-01, on the published `PRE-G9B-R5-B` closeout.

| Identity | Value |
|---|---|
| implementation base `P_R5B` | `e7401734b4ef76d8f79dd4b0535845304937e4f7` |
| base tree | `3ad888b0895d0cb026be60e3d435c0819c4faed8` |
| local `main` = `origin/main` = live remote `main` after `git fetch` | `e7401734b4ef76d8f79dd4b0535845304937e4f7` |
| worktree at entry | clean except ignored author artifacts |
| local branch (not pushed; not identity) | `phase/pre-g9b-r6-ggbscript-compatibility` |

Entry states re-read from disk before any edit: `PRE-G9B-R5-B = PASS — AUTHOR
APPROVED` ([closeout](pre_g9b_r5_b_closeout_record.md), final technical candidate
`a1d96746a42c5ba7fc83d8909647083f11616009`); `PRE-G9B-R6 = DESIGNED — NOT
AUTHORIZED` immediately before this instruction; `PRE-G9B-R7` and `G9B` not
authorized. No identity differed.

## 2. Canonical prompt amendment

The first edit amended
`.github/prompts/tasks/pre-g9b-r6-ggbscript-compatibility.prompt.md` from the
proposed future stub (blob `e1da5f3b4f0dcc9139b19788fdb1dd4f03138de6`) into the
execution prompt for this instance (blob `01eb1ed8259ebee2fefe4c03f5fa84da98cf3e82`).
It resolves `IMPLEMENTATION_BASE`, `IMPLEMENTATION_BASE_TREE`, `R5_B_STATE`,
`R5_B_CLOSEOUT` and `R5_B_FINAL_TECHNICAL_CANDIDATE`; records
`implementationAuthorized=true`, `authorApproved=false`, `passClaimed=false`,
`selfApproved=false`; removes only the statements that R5-B was unsettled and R6
unauthorized; replaces the legacy `FULL` wording with `FINAL` and the conditional
`PHASE_LOCAL`/`GLOBAL` rule of the live contract; and turns the stub's "recorded
rather than rediscovered" R0 facts into planning input re-characterized at
`P_R5B` (§17). The technical scope and intent are unchanged.

Validation before any other edit: `Test-PromptContractDocument` (`task`
profile) found all seven execution-safety fields, ordered, none missing,
duplicated or unknown, `execution_safe = true`;
`tools/agent/checks/prompt-execution-safety.ps1` returned `CONTRACT_SATISFIED`,
exit 0; every repository-relative link resolves; the file is LF with no carriage
return; `git diff --check` is clean.

## 3. Frozen classification and infrastructure impact

The class and impact were frozen after characterization and before any
productive or verification edit.

`PHASE_LOCAL` applies because R6 registers and executes its own matrix only
through existing mechanisms and changes no global verifier semantics or profile
composition:

- one phase selection `PRE-G9B-R6` and six nodes, all with
  `required_for_profiles = []`;
- two JUnit inventory selections, `pre-g9b-r6.shared` and
  `pre-g9b-r6.desktop`, plus the routine refresh of `discovery.desktop` and
  `final.desktop` that every phase adding Desktop tests performs through the
  official updater;
- one static-contract entry consumed only by a phase-local leaf that runs the
  existing `static-semantic-contract.ps1`;
- one phase-local schema checker, `tools/agent/checks/ggbscript-capability-matrix.ps1`,
  using the existing `Assert-VerificationJsonSchema`;
- the registered-shape literals of two verification-core change detectors,
  updated to the new shape exactly as `PRE-G9B-R4` and `PRE-G9B-R5-A` did:
  `verification-final-coverage.Tests.ps1` (32 inventory selections, the
  `pre-g9b-r6.shared` 11 and `pre-g9b-r6.desktop` 53 identity counts, 42 phase
  selections) and `verification-static-semantic.Tests.ps1` (4 static contracts,
  3 of them `PRODUCT`). Their assertions and the verifier are otherwise
  unchanged; a development `INFRA_UNIT` run on the staged tree had rejected the
  old literals, which is how the need was found before freezing.

No acceptance, receipt, profile, trust-model, registry-schema or
checker-identity semantics change. The evidence is plan identity, resolved with
`-PlanOnly` on the base before any infrastructure edit and again on the
candidate tree:

| Profile | Base | Candidate |
|---|---|---|
| `FINAL` | 68 nodes, `COMPLETE`, `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` | identical |
| `INTEGRATION` | 46 nodes, `COMPLETE`, `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` | identical |
| `INFRA_UNIT` | 22 nodes, `COMPLETE`, `830887b17bb7030cdacda0da6999ad5fde64c86e52e96361a34d5f4cc2ffce1d` | identical |
| `STATIC` | 10 nodes, `COMPLETE`, `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` | identical |
| `PHASE -Phase PRE-G9B-R6` | not registered | 10 nodes, `COMPLETE` |

The "corresponding focused operational evidence" of this class is therefore
`INFRA_UNIT` (the verification-core suites that load the live registry, the
inventory and their schemas) and `STATIC` (repository safety, prompt execution
safety and the static-contract catalog), each on the exact candidate, plus the
registered `PRE-G9B-R6` selection. `FINAL` is not required: section 12.8 of
`verification-levels.md` requires it for this class only when global
verification infrastructure changes, which the identical global plans exclude.
No escalation was requested and none was needed.

## 4. `R5-B` state consumed

`PASS — AUTHOR APPROVED — PUBLISHED`, closeout `e7401734…`. The locale dimension
uses the accepted [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md)
policy unchanged: display, suggestion and insertion use the canonical English
head `E(k)`; localized names stay `USER` aliases exactly where `USER` lookup
accepts them; GGBScript is canonical English/internal; `SCRIPT`, `XML` and
nested-`Execute` lookup are unchanged. The `R5-B-R1` host language lifecycle
(`AppD.setLanguage`) is used for every locale change, and no `R5-B-R2` Input Help
path is touched. No R5-B regression was observed (§16).

## 5. Command inventory

**Derivation (no hand-maintained list).** The completeness gate derives the
inventory from the running GeoCeDG host on every run:

1. every internal constant of the kernel enum `Commands`
   (`Commands.englishToInternal(c) == c`; English alias constants such as
   `Reflect` are aliases of `Mirror`, not identities);
2. its runtime processor, from `CommandDispatcher.commandTableSwitch` of
   `AppGeoCeDG` (an `App3D` host, so 3D processors apply), and that processor's
   class chain up to `CommandProcessor`;
3. a class of that chain is GeoCeDG-owned behaviour when
   `docs/upstream/modified-files.yml` registers its source as `added` or
   `modified` **and** the source references a GeoCeDG-owned package
   (`org.geocedg.`); `added` gives `GEOCEDG_ADDED`, otherwise
   `GEOCEDG_MODIFIED`;
4. a registered processor without such a reference is a structural
   *processor exclusion* that the matrix must list exactly;
5. arity forms are the lines of `<id>.Syntax3D` when present in the English base
   bundle `command.properties`, else `<id>.Syntax`.

**Result at `P_R5B`**, identical to the scratch characterization at the base:

| Command | Role | Runtime processor (registered class) | Syntax key / forms |
|---|---|---|---|
| `LocusV2` | added | `CmdLocusV2` | `LocusV2.Syntax` / 3 |
| `LocusLength` | added | `CmdLocusLength` | `LocusLength.Syntax` / 3 |
| `SplineV2` | added | `CmdSplineV2` | `SplineV2.Syntax` / 4 |
| `Point` | modified | `CmdPoint3D` (`CmdPoint`) | `Point.Syntax` / 5 |
| `Length` | modified | `CmdLength3D` (`CmdLength`) | `Length.Syntax` / 6 |
| `Intersect` | modified | `CmdIntersect3D` (`CmdIntersect`) | `Intersect.Syntax` / 8 |
| `Translate` | modified | `CmdTranslate3D` (`CmdTranslate`) | `Translate.Syntax` / 2 |
| `Rotate` | modified | `CmdRotate3D` (`CmdRotate3D`, `CmdRotate`) | `Rotate.Syntax3D` / 4 |
| `Mirror` | modified | `CmdMirror3D` (`CmdMirror3D`, `CmdMirror`) | `Mirror.Syntax3D` / 4 |
| `Dilate` | modified | `CmdDilate3D` (`CmdDilate`) | `Dilate.Syntax` / 2 |

**Processor exclusions (`NO_GEOCEDG_SEMANTIC_REFERENCE`).**
`CAScmdProcessor` serves 24 CAS-only commands; its only change is the R5-B
command head in the `CASViewOnly` message, already covered by the R5-B
canonical-surface gate. `CmdExecute` is registered by this phase (§16); it is
the `GGBSCRIPT_EXECUTE` entry surface itself, exercised by every
`GGBSCRIPT_EXECUTE` row.

**Form exclusions (`NO_GEOCEDG_BRANCH`, allowed only for modified commands).**
14 forms whose processor arity/type dispatch has no GeoCeDG branch:
`Point` 1, 3, 4; `Length` 1, 3, 4, 5; `Intersect` 1–5; `Translate` 2; and
`Rotate` 4, whose Locus V2 refusal comes from the upstream 3D path. Every other
form is covered.

## 6. Architecture decision

Verification and tooling, plus two minimal shared-runtime lifecycle
corrections. The matrix is observational: it references the kernel command enum,
the runtime dispatcher, the upstream modification record and the command bundle,
and declares no command, processor or geometric fact of its own. Kernel and
shared runtime remain authoritative for script lookup, dispatch, DAG
construction and execution. The executor runs in the Desktop module because the
`ALGEBRA_INPUT` surface is the Desktop submission adapter and native `.cedg`
save/reopen is a Desktop route.

## 7. Matrix schema and row model

Schema `geocedg/specs/operations/ggbscript-capability-matrix.schema.json`
(JSON Schema 2020-12, repository conventions: constant `$schema` path,
`geocedg.local` `$id`, closed objects). Instance
`geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`.

**Row identity** `(command_id, arity_form, entry_surface, ui_locale)`, encoded
as `row_id = <command>#<form index>|<surface>|<locale>`. Each row records the
command identity and role; the arity form as `syntax_key`, `syntax_index` and
the quoted `syntax_line`; the surface and UI locale; the lookup strategy in
force; the fixture; the typed statement, the surface text, the command token and
its kind, and the authoring route; the display name, the stored representation
and the runtime token; the typed `verdict_type`; seven capability verdicts; the
expected error key; the frontend-orchestration expectation; and its JUnit
identity.

**Typed verdicts.** `verdict_type` is `SUPPORTED` or `UNSUPPORTED_TRUTHFUL`.
Capabilities: `name_lookup = RESOLVED`; `processor_dispatch = DISPATCHED |
REFUSED`; `dag_construction = CONSTRUCTED | NOT_APPLICABLE`; `recompute =
RECOMPUTED | NOT_APPLICABLE`; `undo = SINGLE_STEP_RESTORES_PRE_STATEMENT |
NO_UNDO_STEP`; `native_persistence = ROUND_TRIP_IDENTICAL | NOT_APPLICABLE`;
`refusal_atomicity = CONSTRUCTION_XML_IDENTICAL | NOT_APPLICABLE`. No verdict
is empty and no row is skipped. Every `UNSUPPORTED_TRUTHFUL` row carries an
error key and the construction-XML identity assertion.

**Lookup probes** expose the regimes without normalizing them, identity
`(command_id, entry_surface, ui_locale, token_kind, authoring)`, token kinds
`CASE_VARIANT`, `INTERNAL_NAME` (where internal ≠ English), `LOCALIZED_ALIAS`
(Spanish UI) and `FOREIGN_ALIAS` (the Spanish name under the English UI), with
authoring `TYPED`, `RAW` (a script attached without the editor, as XML-loaded or
runtime-built scripts are) or `SCRIPT_EDITOR` (the real editor model).

**What each executed row proves** (`PreG9BR6CapabilityMatrix.execute`):

- the display name equals the host's `getCommandHead`;
- the runtime token resolved under the row's strategy equals the recorded
  token and maps to the row's command identity;
- script rows are authored through `ScriptInputModel`: the stored text equals
  the recorded representation and the editor shows the surface text;
- the statement runs on its real surface: `GeoCeDGAlgebraInputSubmission`
  with the input-bar flags, or a button click through `GeoScriptRunner`;
- `SUPPORTED`: the output's parent algorithm reports the row's command
  identity; it depends on the fixture's free driver; an ordinary redefinition of
  the driver updates it and re-entering the driver's fixture definition restores
  the whole construction XML; native save and reopen in a second host yields an
  identical construction XML, the same identity and a recomputing output; one
  undo restores the pre-statement construction;
- `UNSUPPORTED_TRUTHFUL`: no output, construction XML byte-identical, the
  localized text of the error key in the row's UI language, and no undo step;
- the Desktop intersection session is activated only on rich Algebra-input
  results, auto-materialize is off by default, and scripts never activate it.

No geometric value is asserted anywhere.

## 8. Row counts

162 rows: 27 covered forms × 3 surfaces × 2 locales.

| Command | Covered forms | `SUPPORTED` | `UNSUPPORTED_TRUTHFUL` |
|---|---|---|---|
| `LocusV2` | 1, 2, 3 | 18 | 0 |
| `LocusLength` | 1, 2, 3 | 18 | 0 |
| `SplineV2` | 1, 2, 3, 4 | 24 | 0 |
| `Point` | 2, 5 | 12 | 0 |
| `Length` | 2, 6 | 12 | 0 |
| `Intersect` | 6, 7, 8 | 18 | 0 |
| `Translate` | 1 | 6 | 0 |
| `Rotate` | 1, 2, 3 | 12 | 6 (form 3, 3D axis) |
| `Mirror` | 1, 2, 3, 4 | 12 | 12 (form 3 plane, form 4 circle) |
| `Dilate` | 1, 2 | 12 | 0 |
| **total** | 27 | **144** | **18** |

Per surface and locale: 24 `SUPPORTED` and 3 `UNSUPPORTED_TRUTHFUL` in each of
the six cells. Frontend orchestration: `RICH_RESULT_SESSION_ACTIVATED` 4,
`HOST_PATH_WITHOUT_RICH_RESULT` 44, `NOT_REACHED` 6, `NOT_INHERITED` 108.

138 lookup probes: 64 `SUPPORTED`, 74 `UNSUPPORTED_TRUTHFUL` (English UI 23/37,
Spanish UI 41/37).

## 9. Completeness gate

`PreG9BR6CapabilityMatrixTest.completenessCoversTheDerivedInventory` checks
derived inventory × surfaces × locales × applicable forms against the matrix:
no row or exclusion outside the inventory; every form of every command covered
or excluded, never both; exclusions only for modified commands; at least one
covered form per command; exactly six cells per covered form; quoted syntax
lines and keys equal to the bundle; roles, strategies, display names, verdict
vocabularies, error keys and orchestration values consistent; processor
exclusions equal to the derived set; the exact expected probe set; and every row
and probe naming an existing `@Test` method of the executing class. **Result: no
violation.**

`completenessFailsForAnUncoveredCommandFormOrCell` proves the gate fails for: a
synthetic new GeoCeDG-added command without rows (`MISSING_COVERAGE`); a new
syntax line of `LocusV2` (`FORM_UNACCOUNTED`); a removed row
(`INCOMPLETE_CELLS`); an exclusion of an added command's form; and a row naming
a missing JUnit method.

## 10. Lookup regimes

| Token (UI) | `USER` (Algebra Input) | `SCRIPT` (GGBScript) | `XML` (nested `Execute`) |
|---|---|---|---|
| canonical English `E(k)` (en, es) | accepted | accepted | accepted |
| internal name, e.g. `Mirror` (en, es) | accepted | accepted | accepted |
| lower-case English, e.g. `reflect` (en, es) | accepted | accepted | `UnknownCommand`, atomic |
| Spanish alias, e.g. `Refleja` (es), typed | accepted | — | — |
| Spanish alias, raw script text (es) | — | `UnknownCommand`, atomic | `UnknownCommand`, atomic |
| Spanish alias through the script editor (es) | — | accepted: the editor stores the internal name | `UnknownCommand`, atomic: the `Execute` payload is a string the editor never delocalizes |
| Spanish alias under the English UI | `UnknownCommand`, atomic | `UnknownCommand`, atomic | `UnknownCommand`, atomic |

`SCRIPT` is laxer on case and stricter on language than `USER`; `XML` accepts
exact-case enum names, including the English alias constant `Reflect`, and
nothing else. `SplineV2` has the same Spanish name, so it has no alias probe. The
`USER` reverse lookup, the `SCRIPT` normalization and the `XML` verbatim rule are
unchanged by R6.

## 11. Algebra Input versus GGBScript

- **Orchestration.** Explicit Algebra Input of a rich intersection activates
  the Desktop intersection session and, with the opt-in auto-materialize on,
  materializes points; the opt-in is off by default. A script creating the same
  rich result materializes nothing and never activates the session
  (`PreG9BR6ScriptSurfaceBoundaryTest.scriptsDoNotInheritAlgebraInputOrchestration`).
  Scripts are not made to invoke Desktop orchestration.
- **Evaluation flags.** Algebra Input uses the input-bar flags (sliders,
  degrees, symbolic); scripts run every line with sliders and degrees off and a
  script error handler that refuses undefined-variable creation.
- **Undo** (§13) and **result reporting** (§12) differ by surface, deliberately.

## 12. Nested `Execute`

All 54 `GGBSCRIPT_EXECUTE` rows run inside a click script. String arguments of
the nested command use `UnicodeToLetter(34)` concatenation, because GeoGebra
strings have no `\"` escape; the matrix records this as
`string_argument_encoding`. Inner refusals are reported through the error
presentation and stay atomic, but `Execute` returns its argument list, so the
outer script line is not a failure
(`executeKeepsItsInheritedResultContract`, recorded as
`inner_failure_reported_to_script_result = false`). This inherited contract is
retained, not changed (§21). The `XML` strategy is now restored after any
throwable (§16).

## 13. Undo

- **Algebra Input:** one undo step per successful submission (the adapter's
  single store); a refused submission stores none.
- **Click script / nested `Execute`:** per-line evaluation stores nothing; one
  undo step covers all changing lines of one click run (`GeoScriptRunner`); a
  run that changes nothing stores none.
- **Update script:** never stores undo (inherited `GeoScriptRunner` policy).

Every `SUPPORTED` row proves one undo restores the pre-statement construction;
every `UNSUPPORTED_TRUTHFUL` row proves no undo step is stored
(`undoStorageDiffersBySurface` covers the per-surface policy).

## 14. Persistence

All 144 `SUPPORTED` rows save the native `.cedg` through
`GuiManagerGeoCeDG.saveAsTo`, reopen it in a second host of the same UI
language, and obtain a byte-identical construction XML (including the spatial
identity section), the same command identity, and an output that recomputes
when the reopened driver moves. The script-editor boundary test reopens across
UI languages (§15). No serialization change.

## 15. Script-editor boundary and locale lifecycle

`scriptEditorBoundaryHoldsAcrossUiLanguages`, in both directions ES→EN and
EN→ES: a script typed with canonical English heads and, under Spanish, a
Spanish alias is stored with internal names (`X=Mirror(S,O)`), displayed with
canonical English heads (`X=Reflect(S,O)`), saved as `.cedg`, reopened under the
other UI language with byte-identical stored text, and run: both outputs have
command identity `Mirror`, depend on the spline's points and recompute.

`sameSessionLanguageChangeKeepsScriptIdentityAndUserAliases`: after
`AppD.setLanguage(es)` in the same session the stored script and its canonical
display are unchanged and it runs; the Spanish alias works in Algebra Input; a
raw Spanish token in a script is a truthful `UnknownCommand` refusal.

## 16. `TD-R0-GGBSCRIPT-STRATEGY-RESTORE`

**Characterization at `P_R5B`** (scratch probes on the exact base, then the
committed regressions failing on unfixed code):

1. *Strategy leak.* `GgbScript.run` sets `SCRIPT` and restores the previous
   strategy only after the line loop, outside any `finally`. The per-line
   `catch (Throwable)` keeps loop failures from escaping, but placeholder
   substitution runs before the loop: `%0` is replaced with
   `String.replaceAll`, whose replacement syntax rejects a lone `$`. A
   number-linked input box whose typed text does not evaluate (for example
   `5$`) keeps that text, and pressing Enter runs its click script with it as
   `%0`: `IllegalArgumentException` escapes, the kernel stays in `SCRIPT`, and
   a later Spanish Algebra Input such as `Circunferencia((0,0),1)` fails with
   `Comando desconocido` until something resets the strategy.
2. *Swallowed throwable.* A throwable escaping
   `processAlgebraCommandNoExceptionHandling` (any non-`MyError` `Error`, or a
   failure inside the error presentation) is swallowed and `success` is not
   downgraded, so the script reports success. For update scripts this is
   observable: `GeoScriptRunner` sets `setBlockUpdateScripts(!ok)`, so a failed
   update script unblocked later update scripts.
3. *Nested analogue.* `CmdExecute.perform` sets `XML` and restores the previous
   strategy only after its loop, catching `MyError` and `Exception` but not other
   `Error`s. A natural recursive list `Lr={"Execute(Lr)"}` evaluated by
   `Execute(Lr)` overflows the stack and leaves the kernel in `XML`; an
   escaping `Error` inside a script left the next line under `XML`, so
   `c=circle(...)` failed while the script reported success.

**Correction** (`source/shared/common`, minimal, inside existing semantics):

- `GgbScript.run`: the strategy is restored in `finally` after success or any
  throwable; an escaping per-line throwable sets `success = false`; later lines
  still run; lookup, delocalization and placeholder semantics are unchanged; a
  throwable outside the loop still propagates (no catch-and-ignore).
- `CmdExecute.perform`: the loop runs inside `try … finally` that restores the
  previous strategy; `XML` lookup, error reporting, break-on-error and the
  returned argument list are unchanged.

**Pre-fix failing regression** (`PreG9BR6ScriptStrategyRestoreTest`, run against
the unfixed base sources): 6 tests, 6 failures, each for its intended cause —
`expected USER but was SCRIPT` (input box), `expected false but was true`
(escaping throwable), `expected true but was false` (update-script block),
`expected USER but was XML` (escaping `Error` in `Execute`, and the natural
recursion), and the reported success of a failing line under every previous
strategy. The JUnit report of that run was preserved locally (SHA-256
`895ec30d7ac3cf2807e70421a1a1f519d31046223e84ef2265ef21209476fa73`). After the
correction: 6/6 pass, with the upstream `GgbScriptTest` (7),
`G9U1ScriptWorkflowTest` (8) and `PreG9BR5BCanonicalScriptTest` (4) unchanged.

**Authorization basis.** The `GgbScript` correction is exactly
`TD-R0-GGBSCRIPT-STRATEGY-RESTORE`. The `CmdExecute` correction is the same
defect class on the `GGBSCRIPT_EXECUTE` surface — it leaks lookup state after
failure — and falls under the bounded truthful-failure authorization: minimal,
causally demonstrated by a natural reproduction, inside existing semantics, and
covered by pre-fix failing regressions. It adds no GGBScript functionality.

## 17. Re-characterization of the planning facts

- One dispatcher and one processor factory for all surfaces — **confirmed**:
  every probe resolves to the same runtime processor class under `USER`,
  `SCRIPT` and `XML`.
- The Desktop Algebra input wraps the identical processor entry point, with a
  redefine branch and, on creation only, post-creation orchestration and one undo
  store — **confirmed** (`GeoCeDGAlgebraInputSubmission`).
- Scripts run without sliders or degrees, with a script error handler and no
  per-line undo — **confirmed**; refined: one undo step per changing click run,
  none for update scripts.
- The swallowed-throwable and strategy-restore defect — **confirmed** and
  reproduced; refined: the strategy leak needs a throwable outside the per-line
  catch, reachable through `%0` substitution; the nested `Execute` analogue is
  new evidence.
- "The script editor delocalizes on save and re-localizes on display" —
  **stale after R5-B**: the editor now displays canonical English heads and saves
  canonical-first; aliases typed in the editor still become internal names.
- Existing coverage executes the R0 GeoCeDG commands through scripts and
  mechanizes the feature-off atomic rejection — **confirmed**
  (`G9U1ScriptWorkflowTest`), but it calls `GgbScript.run` directly, not the click
  dispatcher, so it never exercised script undo storage.
- Gaps — locale dimension (only a bare `setLocale` round trip for `SplineV2`
  existed), swallowed-throwable row, undo row and script-side rich-result row —
  **confirmed** and closed by this phase.

## 18. Verification before freeze

| Check | Result |
|---|---|
| `PreG9BR6CapabilityMatrixTest` (Desktop) | 14 tests, 0 failures, 162 rows and 138 probes executed |
| `PreG9BR6ScriptStrategyRestoreTest` | 6/6 after the correction; 6/6 failing before it |
| `PreG9BR6ScriptSurfaceBoundaryTest` | 5/5 |
| selection `pre-g9b-r6.desktop` (official producer) | 53 tests, 0 failures/errors: the three R6 classes and the adjacent `G9U1ScriptWorkflowTest`, `G9U1AlgebraSubmissionTest`, `PreG9BR5BCanonicalScriptTest`, `PreG9BR5BCanonicalCommandGateTest`, `DotXmlLocaleIndependentPersistenceTest` |
| selection `pre-g9b-r6.shared` (official producer) | 11 tests, 0 failures/errors: `GgbScriptTest`, `GeoCeDGCommandsTest`, `CommandsTest.cmdExecute` |
| selection `final.desktop` (official producer, complete Desktop suite) | 1 657 tests, 0 failures/errors, 1 skip |
| selection `final.shared` (official producer, complete shared suite) | 6 850 tests, 0 failures/errors, 10 skips |
| schema conformance (`ggbscript-capability-matrix.ps1`) | `CONTRACT_SATISFIED`; a corrupted row is rejected |
| Checkstyle `:shared:common:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings |
| upstream boundary against `9b93256b…` | every changed `source/` file registered |
| prompt contract | `execution_safe = true`, `CONTRACT_SATISFIED` |
| `-PlanOnly` | global plans identical to the base; `PRE-G9B-R6` `COMPLETE` |

The skips are the pre-existing allowlisted ones (R5-B recorded the same 1 and
10). The full suites ran as pre-freeze regression evidence for the
shared-runtime correction; they are not acceptance campaigns. The acceptance
campaigns on the frozen candidate are reported with the candidate, outside this
file.

**JUnit inventory and pins.** The current pins were reproduced first
(`junit_inventory` `199d40a6…`, `static_contracts` `f32de70b…`). Discovery
evidence came from `--test-dry-run` producer runs of `discovery.shared` and
`discovery.desktop`, executed evidence from passing producer runs of
`pre-g9b-r6.shared`, `pre-g9b-r6.desktop`, `final.desktop` and `final.shared`,
and `tools/agent/update-verification-junit-inventory.ps1` wrote every count and
hash. Changed: Desktop module discovery 1 638 → 1 663 (`discovery.desktop`
`790939ac…`), `final.desktop` 1 632 → 1 657 (`a8f5c8dd…`), and the new
`pre-g9b-r6.shared` (11, `223f39bf…`) and `pre-g9b-r6.desktop` (53,
`196b16d5…`). `discovery.shared`, `final.shared` (6 850) and every other
selection are unchanged. The registry pins were rewritten with
`Get-VerificationCanonicalTextSha256`: `junit_inventory` `a611c831…`,
`static_contracts` `fa2004a1…`. No identity hash was typed by hand; the R6
selection skeletons carried placeholders only until the updater filled them.

## 19. Changed paths

- Governance: `.github/prompts/tasks/pre-g9b-r6-ggbscript-compatibility.prompt.md`.
- Shared runtime: `source/shared/common/src/main/java/org/geogebra/common/plugin/script/GgbScript.java`,
  `source/shared/common/src/main/java/org/geogebra/common/kernel/scripting/CmdExecute.java`.
- Desktop tests (added): `PreG9BR6ScriptHarness`, `PreG9BR6ScriptStrategyRestoreTest`,
  `PreG9BR6CapabilityMatrix`, `PreG9BR6CapabilityMatrixTest`,
  `PreG9BR6ScriptSurfaceBoundaryTest` under
  `source/desktop/desktop/src/test/java/org/geocedg/desktop/`.
- Authority and evidence: `geocedg/specs/operations/ggbscript-capability-matrix.schema.json`,
  `geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`,
  `geocedg/validation/pre-g9b-r6/pre-g9b-r6-candidate-evidence.json`, this report.
- Verification registration: `geocedg/specs/operations/verification-registry.json`,
  `verification-junit-inventory.json`, `verification-static-contracts.json`,
  `tools/agent/checks/ggbscript-capability-matrix.ps1`, and the shape literals of
  `tools/agent/tests/verification-final-coverage.Tests.ps1` and
  `tools/agent/tests/verification-static-semantic.Tests.ps1`.
- Records: `docs/upstream/modified-files.yml`, `docs/roadmap/geocedg_roadmap.md`.

`CLAUDE.md`, `AGENTS.md`, every other prompt, the guides, ADRs and specifications
other than the new schema are untouched.

## 20. Upstream provenance

`CmdExecute.java` was byte-identical to the upstream baseline
`9b93256b7df401ff056c37b502d82df4d72b1522` at `P_R5B` and is newly registered as
`modified`. `GgbScript.java` was already registered (R5-B); its purpose now also
records the R6 correction. Both changes live in the shared runtime because the
defects are shared script and `Execute` semantics consumed by every frontend; no
GeoCeDG-owned seam can restore a strategy that the shared runner leaks. The five
new Desktop test sources are registered as `added`.

## 21. Retained observations and debt

None of these blocks R6 and none is marked resolved by closing it.

| Identifier | Disposition |
|---|---|
| `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` | corrected in this candidate; resolution pending author approval |
| `OBS-R6-PLACEHOLDER-REGEX-REPLACEMENT` | `%0` uses regex replacement, so `$` or `\` in an argument fails or is altered; it now fails truthfully with the strategy restored; changing substitution semantics would be new functionality — retained |
| `OBS-R6-EVENT-DISPATCH-SELF-GEO-ON-ESCAPE` | an exception escaping a listener skips `EventDispatcher`'s `restoreSelfGeo`; upstream, outside R6 — retained |
| `OBS-R6-EXECUTE-INNER-FAILURE-NOT-PROPAGATED` | inherited `Execute` result contract; recorded in the matrix — retained |
| `OBS-R6-EXECUTE-ALIAS-SYNTAX-KEY` | under `XML` the alias constant `Reflect` keeps its verbatim name, so its argument error shows the raw key `Reflect.Syntax`; presentation only — retained |
| `OBS-R6-ROTATE-FORM-4-UPSTREAM-REFUSAL` | `Rotate` form 4 refuses a Locus V2 truthfully through the upstream 3D path; excluded as `NO_GEOCEDG_BRANCH` — retained |

The ledger of the [`PRE-G9B-R5-B` closeout](pre_g9b_r5_b_closeout_record.md)
carries forward unchanged; no retained item touches the script runner, so none
is absorbed. `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` still needs its owner
and disposition by the `PRE-G9B-R7` closeout.

## 22. Impact declarations

```text
PRODUCT_PHASE_EFFECT               = TWO SHARED-RUNTIME LIFECYCLE CORRECTIONS
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, toolchain, Gradle, Conda, packaging,
download or environment contract changes.
GUIDE_IMPACT = NONE
Rationale: the guides state that localized names are accepted in typed input
and that GGBScript uses English/internal names; they describe neither the
corrected defect nor its failure behaviour, and both statements are true after
the correction. The matrix is developer/verification authority.
SERIALIZATION_IMPACT = NONE
```

## 23. Author smoke checklist (not performed on the author's behalf)

1. Spanish UI, new document: `A=(-2,0)`, `B=(0,1)`, `C=(2,0)`,
   `S=SplineV2({A,B,C})`, `O=(0,0)`, a button. In the button's click script type
   `T=Reflect(S,O)` and `M=Length(T)`.
   - The script editor shows the English command names.
   - Click the button: `T` and `M` appear; drag `B`: both update.
   - Undo once: both disappear together; redo.
   - Save as `.cedg`, switch to English (Options → GeoCeDG options → Product
     language…), reopen: the script text is unchanged and the button still works.
2. Spanish UI: `n=1`, `m=0`, an input box for `n` with click script
   `SetValue(m, %0)`. Type `5$` in the box and press Enter (the script fails).
   Then type `Circunferencia((0,0),1)` in Algebra Input: a circle must be
   created. Before the correction this second step failed as unknown command.

## 24. Authorization state

```text
PRE-G9B-R6     = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR SMOKE   = PENDING
AUTHOR APPROVAL = PENDING
promotion      = NONE
PRE-G9B-R7     = NOT AUTHORIZED
G9B            = NOT AUTHORIZED
```

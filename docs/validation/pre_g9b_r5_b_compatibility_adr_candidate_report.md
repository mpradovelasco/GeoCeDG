# PRE-G9B-R5-B — compatibility ADR prerequisite: candidate report

```text
TASK                           = PRE-G9B-R5-B COMPATIBILITY ADR PREREQUISITE
AUTHORIZED_SCOPE               = characterization + compatibility design + ADR candidate
                                 + design evidence + author-review report
CHANGE_ROUTE                   = ORDINARY
VERIFICATION_CLASS             = DOCUMENTATION_STATUS_ONLY   (frozenAtPhaseStart = true)
PRODUCT_PHASE_EFFECT           = NONE
PRODUCT_SEMANTICS_CHANGED      = false
PRODUCT_CODE_CHANGED           = false
R5_B_IMPLEMENTATION_STARTED    = false
R5_B_IMPLEMENTATION_AUTHORIZED = false
ADR                            = docs/adr/0031-canonical-english-command-surface-compatibility.md
ADR STATUS                     = PROPOSED — PRE-G9B-R5-B COMPATIBILITY CANDIDATE
AUTHOR_DECISION                = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved                   = false
```

This report is historical evidence of a design task. It is not an approval and
not an implementation record. The decision it supports is
[ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md). The
machine-readable facts are in
[`pre-g9b-r5-b-command-name-compatibility-evidence.json`](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-command-name-compatibility-evidence.json),
which is design evidence only and never runtime authority.

## 1. Entry identity

| Item | Value |
|---|---|
| authorized base `P_R5A` | `4389b30b5478ac35c78a15255021e3c4f1e10ea3` |
| tree | `1a4506b830a846efd5032fde5e8386ca8872dc89` |
| local `main` = `origin/main` = live remote `main` | `4389b30b5478ac35c78a15255021e3c4f1e10ea3` after `git fetch` |
| worktree at entry | clean |
| design branch | `design/pre-g9b-r5-b-command-surface-compatibility-adr`, created from `P_R5A`, not pushed |
| roadmap at entry | `PRE-G9B-R5-A = PASS — AUTHOR APPROVED`; `PRE-G9B-R5-B = DESIGNED — NOT AUTHORIZED`, `BLOCKED_ON` an accepted compatibility ADR formalizing `AD-R0-4` |
| highest ADR at entry | 0030; 0031 was free |

## 2. Canonical R5-B authority

[`.github/prompts/tasks/pre-g9b-r5-b-canonical-english-command-surface.prompt.md`](../../.github/prompts/tasks/pre-g9b-r5-b-canonical-english-command-surface.prompt.md),
blob `34e1796d5628108498a7c612c155d5c8d72a4c16`, was read in full and not
edited. It remains `PROPOSED FUTURE PROMPT — UNEXECUTED — R5-B IMPLEMENTATION
NOT AUTHORIZED`. Its `IMPLEMENTATION_BASE` and `ACCEPTED_COMPATIBILITY_ADR`
fields stay `UNRESOLVED`. It was used only as the statement of what this ADR
must decide. None of its implementation parts was executed.

Authorities read from disk:

- `AGENTS.md`;
- the canonical governance and verification prompts;
- the roadmap;
- the PRE-G9B-R staged design §4, the `R5-B` and `R6` blocks;
- the R0 characterization report §7.1 (I2) and §10 (`AD-R0-4`);
- `documentation-maintenance.md`;
- `verification-levels.md` §12.8.

## 3. Method

Every fact below was established from current source at `P_R5A`, then checked
mechanically. R0 prose was treated as planning input only. Where it is
imprecise, §10 says so.

1. **Source reading** of the command-name seams.
2. **An exhaustive read-only call-site sweep**, delegated to a sub-agent and
   spot-checked by hand, across `shared/common`, `shared/common-jre`,
   `desktop/desktop` and the GeoCeDG-owned packages.
3. **A static model** that replays the source algorithms over the exact blobs
   listed in the evidence (`sourceIdentity.characterizedBlobs`). The algorithms
   are `App.fillCommandDict`, `putInTranslateCommandTable`,
   `fillCasCommandDict`, `Localization.getReverseCommand`/`getEnglishCommand`,
   `Commands.lookupInternal`/`englishToInternal` and `App.getInternalCommand`.
4. **Two throwaway runtime probes** against the real `AppGeoCeDG`, created with
   `G9U1TestApp.create(true)`. They dump the live reverse table and resolve 1041
   tokens under every regime, in EN and ES, with the CAS object absent and after
   the CAS dictionary fill. The tokens are all enum names, all EN/ES localized
   names and all `getEnglishCommand` values. The probes also run witness
   constructions, redefinitions, script-editor round trips and XML save/reopen.
5. **A diagnostic scan** of the retained upstream command corpus. It does not
   widen GeoCeDG's supported locales.

The probes and scripts lived only in the session scratchpad. They were compiled
into the Desktop test source set through a Gradle `--init-script` and never
written to a tracked path. They are characterization evidence, not acceptance
evidence and not product tests:

```text
gradlew.bat :desktop:desktop:test --tests org.geocedg.desktop.PreG9BR5BCommandNameProbe   --init-script <scratch>/probe-init.gradle   exit 0
gradlew.bat :desktop:desktop:test --tests org.geocedg.desktop.PreG9BR5BSerializationProbe --init-script <scratch>/probe-init.gradle   exit 0
```

The static model agrees with the live table in both fresh states: `en` with 532
keys and `es` with 983 keys, identical key for key. In the CAS-filled states the
static model adds one key in EN and two in ES. They come from a commented-out
`Ggb2giac` signature (`SolveQuartic`). No value differs. Token-resolution
agreement is 1041/1041, and 1040/1041 in ES after the CAS fill, for the same
commented-out entry. The live table is authoritative.

## 4. Current source characterization

### 4.1 Lookup regimes

| Regime | Accepts | Mechanism | Set by |
|---|---|---|---|
| `USER` (Kernel default) | localized aliases, alias constants, internal names; case-insensitive | `Command(kernel, name, true)` → `App.getReverseCommand` → reverse table by lower-case key → `Commands.lookupInternal` | default; `RelativeCopy` restores it |
| `SCRIPT` | any enum constant name, case-insensitive; never a localized alias as such | `Commands.lookupInternal` → `englishToInternal`; unknown kept verbatim | `GgbScript.run` |
| `XML` | exact-case enum constants only | name verbatim; dispatcher `Commands.valueOf` | `MyXMLio`, `CmdExecute`, `GgbAPI.evalCommandGetLabels`, `PolygonFactory`, `RelativeCopy` |

In addition:

- **Script-editor save convention.** `GgbScript.localizedScript2Script` calls
  `App.getInternalCommand`. That tries the `RENAMED` set, then takes the
  **first** enum-order command whose UI-locale localized name matches. It never
  matches English or internal names as such, and it keeps an unmatched token
  verbatim.
- **Macros.** `CommandDispatcher.getProcessor` checks `kernel.getMacro` on the
  resolved name before the command table. This holds for XML-loaded commands
  too.
- **Parser pre-emption.** `FunctionParser.makeFunctionNode` tries parser
  functions before commands, even for `[`. With `(`, an existing GeoElement,
  function-variable or CAS-cell label with the same name turns the call into
  function application or multiplication. Labels equal to command names are
  legal: `LabelManager` checks only reserved parser-function names.
- **English aliases dispatch directly.** 51 of the 52 `TABLE_ENGLISH` constants
  have their own dispatcher case. The exception is `Evaluate`, which is
  CAS-only. `XML`-strategy text therefore accepts English alias constants in
  exact case.

### 4.2 Reverse table

1. `App.fillCommandDict` iterates `Commands` in declaration order. It skips
   constants the filters reject; GeoCeDG hides the dedicated LocusV2 commands
   when creation is disabled.
2. Hidden tables are `TABLE_CAS` and `TABLE_ENGLISH`, plus `TABLE_3D` when 3D
   commands are disabled. `AppGeoCeDG` enables them. A hidden `TABLE_ENGLISH`
   constant inserts only its own lower-case name, and only if absent.
3. For a visible constant:
   - its lower-case internal name is inserted only if absent;
   - its lower-case localized name is inserted with overwrite, first by
     `putInTranslateCommandTable` as `englishToInternal`, then by
     `addCommandEntry` as `comm.name()`.
4. `App.fillCasCommandDict` runs only after the CAS object exists. It
   re-inserts CAS command aliases with overwrite.
5. Consequences:
   - a localized alias always beats an internal or English name with the same
     lower-case key;
   - among colliding localized aliases the last writer wins;
   - `getInternalCommand` uses the opposite rule, first match wins.
6. Freshness. `AppD.setLanguage` → `setLabels` → `updateCommandDictionary`
   rebuilds the table. A bare `setLocale` does not. Right after `setLocale("es")`
   the probe saw the EN table, so `ProductoEscalar` and `Si` were unknown. This
   is a **test-harness caveat**: the product locale path rebuilds, and future
   tests must use it or refresh explicitly.

### 4.3 Name authorities

- `Localization.getCommand` (JRE: `LocalizationJre.getCommand`) is the UI-locale
  bundle value, with the key returned on a miss. It is both the localized alias
  source and the current display head.
- The canonical English public name `E(k)` is the English bundle value. The
  English UI shows it today.
- `Localization.getEnglishCommand` on the JRE is an enum-alias mapper
  (`getMainCommandName`, then the first `TABLE_ENGLISH`-style alias). Non-command
  keys are returned verbatim. The web `LocalizationW.getEnglishCommand` reads the
  English bundle instead; that is the semantics this ADR adopts as `E(k)`.
- `App.getInternalCommand` is the localized-only reverse lookup used by the
  script editor, autocomplete syntax and F1 help.

### 4.4 Syntax provider

`Localization` owns `private final LocalizedCommandSyntax commandSyntax` with a
getter and no setter. `LocalizedCommandSyntax.getLocalizedCommand` serves both
the head, `getLocalizedCommand(internal)`, and the body key,
`getLocalizedCommand(internal + ".Syntax")`. `.Syntax3D` goes through
`loc.getCommand` directly. `EnglishCommandSyntax` overrides that single method
with `getEnglishCommand`. On the JRE that returns `Circle.Syntax` verbatim, so
the "syntax" becomes the literal key. The R0 finding is confirmed from source.
Head resolution and syntax-key resolution must become separate operations.

Two visible commands, `CSolve` and `CSolutions`, have a literal-key `.Syntax` in
both EN and ES today. They have only `.SyntaxCAS` entries. This is pre-existing
and outside R5-B.

### 4.5 Autocomplete and Input Help (Desktop)

- **Dictionary.** `AutoCompleteTextFieldD` uses `app.getCommandDictionary()`,
  which holds localized names plus macro names. Matching is case- and
  accent-insensitive substring, prefix first. English names are not searched on
  Desktop. `englishCommandDict` is built from `getEnglishCommand` and read only
  by web; it contains `IntersectCircle`, not `IntersectConic`.
- **Syntax and F1 help.** Both call `getSyntaxes` / `showCommandHelp`, which
  re-derive the command with `app.getInternalCommand(displayedName)`. Only
  UI-locale localized names match there. English entries under ES would get no
  syntax.
- **Inserted text.** The whole localized syntax line. If a bracket already
  follows the word, only the head is inserted. `AlgebraInputD.insertCommand`
  inserts the localized name plus `[]`.
- **`InputBarHelpPanelD`.** Tree names come from the localized sub-dictionaries.
  Selection runs `getReverseCommand(displayedName)` → `getCommandSyntax`, and
  `focusCommand` compares localized node text. `AppGeoCeDG.setShowInputHelpPanel`
  focuses `getLocalization().getCommand("SplineV2")`.
- **`HelpOnKeywordPanel`**, in the script editor, shows the raw `.Syntax`
  string.

### 4.6 GGBScript

- `GgbScript.getText` displays `script2LocalizedScript`, which calls
  `loc.getCommand` per command token.
- Save goes through `localizedScript2Script` → `getInternalCommand`, keeping
  unmatched tokens.
- `run` switches to `SCRIPT`.
- Stored text is internal, or English when the user typed English. XML-loaded
  scripts are not translated (`createScript(type, text, false)`).
- The same conversions serve CAS-cell input.

Witness in ES: the stored script contains `PerpendicularLine`, `OrthogonalLine`,
`LaTeX`, `SplineV2`, `LocusV2`, `Circumference` and `Perimeter` tokens.

- **Display:** `PerpendicularLine`, `Perpendicular`, `FórmulaTexto`,
  `SplineV2`, `LugarGeométricoV2`, `Perímetro`, `Perímetro`.
- **Untouched save:** the `Perimeter` token becomes `Circumference`.

That is a **pre-existing identity change of the stored token**. It is benign
only because the two processors are equivalent (§5).

### 4.7 XML and serialization

- XML `<command name>` stores internal names under every UI locale. Witnessed:
  `OrthogonalLine`, `Mirror`, `AngularBisector`, `LaTeX`, `PolyLine`,
  `Defined`, `mean`, `SplineV2`, `If`, `Integral`, `Circle`, including objects
  typed with English heads in ES.
- XML expression strings print command heads non-localized and function names
  internal (`sin`).
- **Exception, pre-existing:** `ExpressionSerializer` `case DOT` (the `Dot`
  command becomes `Operation.DOT` via `CmdCAStoOperation`) passes
  `loc.getCommand("Dot")` to `twoVar` for every non-GIAC string type, XML
  included. Under ES the saved XML contains `exp="ProductoEscalar(u, v)"`.
  Reopening that XML under ES or EN leaves `d` as an **independent** number
  with its last value: the dependency on `u` and `v` is lost silently. Under EN
  the expression is `Dot(u, v)` and reloads correctly.
- `FREEHAND` also prints `loc.getFunction("freehand")` (ES `boceto`) without a
  template gate. It was not verified mechanically. It is a function name, not a
  command head, and belongs to the same future serialization audit.

The seams outside the future display policy are:

- `AlgoElement.getXML` / `getCmdXML`;
- `MyXMLHandler` command creation (`translateName=false`);
- `xmlTemplate`, `giacTemplate`, `noLocalDefault`, `prefixed*`, `maxPrecision*`
  and the `GgbAPI` non-localized template;
- the script XML (`getInternalText`);
- the serialization role of the `DOT` site.

### 4.8 Print sites by role

The complete role inventory with sites is in the evidence
(`printSiteCategories`). Summary:

**USER-VISIBLE DISPLAY**

- `AlgoElement.getDefinition` and `appendCheckVector`; `Command.toString`;
- the `ExpressionSerializer` display roles of `DOT`, `If` and `DataFunction`;
- `GeoPieChart`, `AlgoLocusStroke`;
- the GeoCeDG `AlgoLocusMetricScalarAdapter` (`Length` head);
- Construction Protocol, Algebra description and definition modes, tooltips,
  screen-reader text;
- command error messages (`CommandErrorMessageBuilder`, `ErrorHelper`,
  `CommandNotFoundError`, `CAScmdProcessor`);
- Input Help, `HelpOnKeywordPanel`, `AppGeoCeDG` focus, `GeoCeDGDefinitionInspector`;
- the English-literal value strings of `GeoLocusV2`, `GeoLocusMetricResult` and
  `GeoLocusIntersectionResult`, already compliant.

**EDITABLE USER TEXT**

- `getRedefineString` (`editTemplate`) through `DialogManagerD` and
  `RedefineInputHandler`;
- `getDefinitionForInputBar`: `MyCellEditorD`, `ObjectNameModel`, drag and
  drop, `GlobalKeyDispatcher`;
- `getDefinitionForEditor`;
- the autocomplete insertion and `AlgebraInputD.insertCommand`;
- formula-bar and spreadsheet editors; `TextInputDialogD`.

**SCRIPT REPRESENTATION**

- `GgbScript` display and save; CAS-cell input; the `GeoGebraLexer`
  highlighting set, which holds localized names.

**INTERNAL ONLY**

- `FunctionParser.isCommand`, `InputHelper.needsAutocomplete`,
  `Traversing.CommandReplacer`, `CommandSyntaxLookupImpl`,
  `GeoCeDGUserToolLibrary.nativeCommand` (already canonical-first),
  `GuiManager.getHelpURL`.
- `DynamicTextProcessor` and `DynamicTextInputPane` **parse display output** by
  matching `getCommand("LaTeX") + "("` and `getCommand("Name") + "("`.

**Outside the command-head policy (UI prose)**

- `TextDispatcher` auto-label stems such as `distanciaAB`;
- `FunctionInspectorModel` captions; `CellRangeUtil`, `Relation` and
  `AlgoProveDetails` captions;
- tool and menu names;
- parser function names (`sen`, `tg`, `raízn`, `boceto`).

The Algebra "prefix literal" named in the canonical prompt no longer exists.
`PRE-G9B-R4` removed the `SplineV2(` prefill (R4 candidate report). The
remaining SplineV2 help surfaces are the Input Help focus and the profile-owned
tooltip, and the tooltip already uses `SplineV2(…)` in both languages.

## 5. Command-name model and collision inventory

**Inventory.** 564 enum constants, 521 `englishToInternal` identities and 489
processor-equivalence classes. Processor equivalence is `englishToInternal` plus
the shared-processor case groups of the processor factories. 486 commands are
displayable in GeoCeDG, 3D included.

- All 486 have an English bundle entry.
- 30 have no Spanish entry and fall back to the base, which is English.
- 31 have a Spanish value equal to the canonical English name.

**English ≠ internal (37 displayable stored names).** Each maps to its English
public name:

- `AngularBisector`→`AngleBisector`, `OrthogonalLine`→`PerpendicularLine`,
  `LineBisector`→`PerpendicularBisector`;
- `CircleArc`→`CircularArc`, `CircleSector`→`CircularSector`,
  `CircumcircleSector`→`CircumcircularSector`,
  `CircumcircleArc`→`CircumcircularArc`;
- `PolyLine`→`Polyline`, `LaTeX`→`FormulaText`, `Defined`→`IsDefined`,
  `TurningPoint`→`InflectionPoint`, `CurveCartesian`→`Curve`,
  `TaylorSeries`→`TaylorPolynomial`;
- `SecondAxisLength`→`SemiMinorAxisLength`, `SecondAxis`→`MinorAxis`,
  `Diameter`→`ConjugateDiameter`, `FirstAxis`→`MajorAxis`,
  `FirstAxisLength`→`SemiMajorAxisLength`, `Excentricity`→`LinearEccentricity`;
- `Q1`→`Quartile1`, `Q3`→`Quartile3`, `SXY`→`Sxy`, `SXX`→`Sxx`, `SYY`→`Syy`,
  `PMCC`→`CorrelationCoefficient`, `FitLineY`→`FitLine`,
  `Random`→`RandomBetween`, `Binomial`→`BinomialCoefficient`;
- `UnitOrthogonalVector`→`UnitPerpendicularVector`,
  `OrthogonalVector`→`PerpendicularVector`, `Mirror`→`Reflect`,
  `Textfield`→`InputBox`;
- `DelauneyTriangulation`→`DelaunayTriangulation`, `QuadricSide`→`Side`,
  `OrthogonalPlane`→`PerpendicularPlane`, `ConeInfinite`→`InfiniteCone`,
  `CylinderInfinite`→`InfiniteCylinder`.

The Spanish aliases are in the evidence.

**Two JRE English authorities.** The English bundle, `E1`, and the JRE
`getEnglishCommand`, `E2`, differ for `mean` (`mean`/`Mean`), `stdev`
(`stdev`/`SampleSD`), `mad` (`mad`/`MAD`), `stdevp` (`stdevp`/`SD`), `Binomial`
(`BinomialCoefficient`/`nCr`) and `IntersectConic`
(`IntersectConic`/`IntersectCircle`).

**Collision classes, complete for EN and ES:**

| Class | EN | ES |
|---|---|---|
| localized alias = English name of another command | 0 | 0 |
| localized alias = internal or alias-constant name of another command | 0 | 0 |
| localized alias = localized alias of another command | 0 | 1 (`Perímetro`) |
| canonical English head fails to round-trip (`USER` both states, `SCRIPT`, `XML` exact, script-editor save) | 0 | 0 |
| parser function pre-empts a canonical head | 2 (`nCr/2`, `nPr/2`; equivalent) | 2 (same) |
| reverse-table-state-dependent resolution | 0 | 1 (`Perímetro`) |

**`Perímetro`** is the ES alias of both `Circumference` and `Perimeter`.

- `USER` in a fresh ES session, CAS object absent → `Perimeter`.
- After the CAS dictionary fill → `Circumference`.
- Script-editor save → `Circumference`.
- `SCRIPT` and `XML` → not a command.

`CmdCircumference` extends `CmdPerimeter` without an override. The constructed
algorithm depends only on the argument type. A polygon gives the `Perimeter`
algorithm for `Perímetro`, `Perimeter` and `Circumference` alike. The
conic-argument probe rows do not discriminate and are labelled so in the
evidence. The ambiguity is therefore benign and off the canonical path.

**`nCr` / `nPr`.** Their command processors (`CmdCAStoOperation`) produce
exactly the `Operation.NCR` / `NPR` that the parser functions produce. They are
equivalent.

**Upstream corpus (diagnostic only).** Six of 97 retained command bundles
contain a canonical-head retarget. French `Intersection` resolves to
`Intersect`. `bs`, `ms` and `tr` resolve `Mod` to `Mode`. `ms` resolves `Min` to
`Mean`. `da` resolves `Histogram` to `HistogramRight`. `uk` resolves
`ZMeanTest` to `ZMean2Test`. The EN/ES emptiness is a property of today's bundle
content, not of the mechanism.

## 6. Semantic-identity hazards

1. **Name-level clash (R0's hazard).** The mechanism is confirmed: a localized
   alias equal to another command's English name wins `USER` resolution. The
   EN/ES member set is empty at `P_R5A`, and the corpus shows it can become
   non-empty. ADR decision 5 turns this into a mechanized, fail-closed gate.
2. **Construction-dependent shadowing.** A label, function variable or CAS-cell
   label equal to a canonical head, or a macro with the resolved name, makes an
   unchanged English head parse as something else. With English display, the
   labels that shadow ES documents change from Spanish spellings to English ones.
   ADR decision 6 requires the typed `CANONICAL_HEAD_SHADOWED` rejection with no
   mutation.
3. **Script-editor retarget.** This exists today (§4.6). It is removed by English
   display plus canonical-first save (ADR decision 10).
4. **Identity re-derived from display text.** Autocomplete, F1 help and Input
   Help map displayed names back to commands; `getInternalCommand` would fail
   for English entries in ES. ADR decision 9 requires entries to carry identity.
5. **Display-text parsers.** The dynamic-text matchers break unless they use the
   same authority as the display (ADR decision 1).
6. **Serialization.** The `DOT` localized head loses dependencies on reopen. It
   is pre-existing, isolated and not repaired by R5-B (ADR decision 11, author
   decision 3).

## 7. Alternatives and recommendation

The ADR compares four alternatives on compatibility, identity safety, parser
impact, serialization impact, placement, migration and testability:

- **A.** English-first `USER` precedence.
- **B.** A separate generated-text resolver.
- **C.** Validation against `USER` with fail-closed.
- **D.** Unchanged resolver plus role-based presentation, a complete-inventory
  gate and typed re-entry.

It recommends **D**. A and B need forbidden parser, strategy or reverse-table
changes. C alone does not see construction shadowing or the syntax,
autocomplete and script separations. Compatibility cost of D:

- a shadowed ES edit is rejected with a typed error instead of silently
  re-parsed;
- a future bundle change that breaks the gate blocks the candidate until the
  author disposes of it.

Stop-condition review:

- A safe English display and edit round trip exists without a parser rewrite.
- Localized aliases stay compatible with identity safety under design D.
- The collision class was enumerated deterministically and completely.
- `AD-R0-4` is consistent with source.
- **Met, reported rather than resolved:** "current serialization behaviour
  contradicts the expected unchanged-XML policy". The `DOT` expression head is
  UI-localized in XML, and historical ES files already lose those dependencies
  on reopen (§4.7). The ADR does not change the XML policy, does not guess a
  repair, and does not let R5-B change those bytes. It records the finding for
  author disposition.
- **Met, reported rather than resolved:** "an implementation decision would
  require author choice between materially different compatibility semantics".
  This covers the `DOT` disposition, the syntax placeholder language (§4.4),
  function-name localization and caption reuse. The ADR recommends one option
  for each and leaves the choice to the author, as the task permits.

Neither finding blocks the ADR text itself. Both block R5-B implementation
authorization until the author disposes of them.

## 8. Persistence and script conclusions

- Native `.cedg`/`.ggb` XML command semantics stay the internal persisted
  authority.
- No migration and no format version change are needed or authorized.
- Historical ES documents reopen as at `P_R5A`, including the pre-existing
  `DOT` loss, which R5-B must not change silently in either direction.
- GGBScript stays canonical English/internal. `SCRIPT` runtime is unchanged. UI
  language never changes script meaning.
- The editor displays English and saves canonical-first.
- R6 consumes the policy and implements none of R5-B. R6 remains unauthorized.

## 9. Future architecture

The layers are shared-kernel localization seams and Desktop consumers. The work
does not touch:

- the parser;
- the `Command` constructor strategies;
- reverse-table construction;
- XML parsing;
- the serialization roles of any template;
- geometry.

The policy is selected by the GeoCeDG product profile and stays orthogonal to
`localizeCmds`. Other configurations keep upstream behaviour. The minimum
upstream seams are listed in ADR decision 14. None was written. Against the
upstream baseline, every named seam is unmodified at `P_R5A` except:

- `App.java`: presentation hooks;
- `Commands.java`: the three GeoCeDG constants;
- `CommandDispatcher.java`: the LocusV2 filter;
- `InputBarHelpPanelD`: the R4 `focusCommand` line;
- `AlgebraInputD`: GeoCeDG input-lifecycle changes.

None of those changes touches command naming.

## 10. Corrections to R0 planning statements

The R0 statements are unchanged historical evidence. These corrections apply to
the current source only.

| R0 statement | Current source |
|---|---|
| Spanish names exist for `LocusV2` and `LocusLength` "and not for the third" | `command_es.properties` has `SplineV2=SplineV2` since G9S1 (`de33f3a80`); the ES entry exists and equals the English name |
| "the correct canonical resolver is the English name" | correct in intent. It must be the English bundle, not the JRE `getEnglishCommand`, which diverges for six names |
| text-valued command strings "already require internal names" | they require exact-case enum constants: internal names or English alias constants |
| "Construction XML always stores internal names" | true for command elements and command heads; false for the `DOT` expression head |
| the SplineV2 action prefills `SplineV2(` in the Algebra input | removed by `PRE-G9B-R4` |
| the localized/English clash can silently redefine on edit | the mechanism is confirmed; the EN/ES member set is empty at `P_R5A`; the upstream corpus shows the class is real |

## 11. Changed paths

| Path | Class |
|---|---|
| `docs/adr/0031-canonical-english-command-surface-compatibility.md` | new ADR candidate (`PROPOSED`) |
| `docs/validation/pre_g9b_r5_b_compatibility_adr_candidate_report.md` | new validation report |
| `geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-command-name-compatibility-evidence.json` | new design evidence (not runtime authority) |
| `docs/roadmap/geocedg_roadmap.md` | status only: the ADR candidate exists pending author review |

Nothing changed under `source/`, `tools/`, `.github/prompts/`, `ai-shell/`,
`apps/`, `packaging/` or `geocedg/specs/`. There are no verification-registry,
test, build or serialization changes. `CLAUDE.md`, `AGENTS.md` and the canonical
R5-B prompt are untouched.

## 12. Verification

The frozen class `DOCUMENTATION_STATUS_ONLY` requires static validation only,
under `verification-levels.md` §12.8. This task runs no PHASE, INTEGRATION or
FINAL, emits no receipt and makes no product acceptance claim. The checks are
`git diff --check` and JSON parse validation of the evidence. Then `STATIC` runs
once on the frozen clean candidate:

```text
tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/<fresh>
```

The candidate cannot contain its own run identity. The candidate commit and the
`STATIC` run id, exit code, verdicts and diagnostics are therefore reported with
the task result, outside this file. The standing diagnostics (`diagnostic.governance` finding,
`diagnostic.historical-consistency` unavailable) are pre-existing and
non-acceptance. There is still no repository-relative Markdown link checker, so
the links in the changed documents were resolved by hand.

## 13. Impact declarations

```text
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = design/ADR/status task; no observable or developer-facing
  contract changed. The future R5-B implementation will require GUIDE_IMPACT =
  UPDATED (user guides §16.3 in both editions).
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
  packaging, download or environment contract changed.
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
PRODUCT_PHASE_EFFECT = NONE
```

## 14. Author decisions required

These are the same five as ADR 0031:

1. Acceptance of the ADR on an exact commit.
2. Syntax placeholder language: UI locale (recommended) or English.
3. `DOT` localized XML: a separate serialization-compatibility task
   (recommended), or an explicit exception inside R5-B.
4. ES parser function names: keep localized (recommended) or apply a separate
   policy.
5. Captions and label stems reusing command translations: keep in the UI
   language (recommended) or switch to English.

The canonical R5-B prompt amendments that acceptance would need are listed at
the end of the ADR. They are not applied.

## 15. State

```text
R5-B COMPATIBILITY ADR      = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
ADR STATUS                  = PROPOSED
PRE-G9B-R5-B IMPLEMENTATION = NOT AUTHORIZED
PRE-G9B-R6                  = NOT AUTHORIZED
PRE-G9B-R7                  = NOT AUTHORIZED
G9B                         = NOT AUTHORIZED
promotion                   = NONE
```

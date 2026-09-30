# ADR 0031: Canonical English command surface and localized-alias compatibility

- Status: **ACCEPTED — AUTHOR APPROVED** (decision of 2026-09-30 on the exact
  amended candidate `c459de9ab66d61fe849abbf9b9b8097c56605c48`, tree
  `3c8d0943f2be7b990e0e4cc97cd114203dd47a49`; recorded in the
  [compatibility ADR closeout record](../validation/pre_g9b_r5_b_compatibility_adr_closeout_record.md))
- Date: 2026-09-30
- Amended: 2026-09-30 by the author-decision amendment `R1`, a corrective
  descendant of the initial candidate `3b65af5303aac316a8b2747739aeb43e2921bac4`.
  The measured source facts are unchanged; the author's policy decisions are
  incorporated.
- Phase: `PRE-G9B-R5-B` compatibility ADR prerequisite (design only)
- Formalizes: author decision `AD-R0-4` of 2026-09-17 (policy direction only)
- Future implementation authority it would govern once accepted:
  [`pre-g9b-r5-b-canonical-english-command-surface.prompt.md`](../../.github/prompts/tasks/pre-g9b-r5-b-canonical-english-command-surface.prompt.md)
  (blob `34e1796d5628108498a7c612c155d5c8d72a4c16`, unchanged by this task)
- Characterization: [candidate report](../validation/pre_g9b_r5_b_compatibility_adr_candidate_report.md)
  and [design evidence](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-command-name-compatibility-evidence.json)
- Source base: `4389b30b5478ac35c78a15255021e3c4f1e10ea3`, tree
  `1a4506b830a846efd5032fde5e8386ca8872dc89`

```text
AUTHOR_DECISION                = APPROVED (exact amended candidate c459de9a…)
selfApproved                   = false
implementationAuthorized       = false
R5_B_IMPLEMENTATION_AUTHORIZED = false
PRODUCT_PHASE_EFFECT           = NONE
```

This ADR carries no approval of its own. Acceptance is recorded by the separate
author-decision record named in the status line. The decision text below is
unchanged from the approved candidate `c459de9a…`. Its candidate-state wording,
such as "the only remaining decision is the author's approval", describes that
candidate as it was frozen.

Acceptance does not authorize productive `R5-B` implementation. Decision 17
still applies: the separate `Dot`/XML serialization maintenance must be
published first, and a new explicit author instruction must name the exact
implementation base.

## Author decisions incorporated (amendment R1)

These are **author policy decisions**. They are not measured source facts. The
measured facts of the initial candidate stand unchanged. The section column says
where each decision takes effect.

| Topic | Author decision | Section |
|---|---|---|
| alternative | **D adopted**: unchanged lookup, role-based English presentation, complete-inventory gate, typed fail-closed re-entry | Decisions 4–6; Alternatives |
| internal command identity | unchanged: no internal identifier or enum constant is renamed for presentation | Terminology; Decision 2 |
| canonical public command name | canonical English public name; authority = the English command bundle, not the internal identifier and not the JRE `getEnglishCommand` mapper | Decision 2 |
| localized command names | compatibility input only, wherever current `USER` rules accept them; no alias removed; reverse table unchanged; alias acceptance not extended | Decisions 4, 7 |
| scope | commands only, including `LocusV2`, `LocusLength` and `SplineV2`, through the host policy | Decisions 1, 12 |
| syntax placeholders | current UI language | Decision 8 |
| mathematical and function names (`sen`, `tg`, …) | current UI-locale behaviour retained; outside the command policy | Decision 1 |
| captions and automatically generated labels | current UI language when they are presentation text | Decision 1 |
| scripts | GGBScript canonical English/internal; editor displays English; valid script tokens are preserved on save | Decision 10 |
| XML / serialization | `R5-B` changes neither; no migration; no format version change | Decision 11 |
| `Dot` XML defect | real, pre-existing, upstream-inherited; fixed by a **separate bounded maintenance task, published before productive `R5-B`** | Decisions 11, 17 |
| collision guard | adopted: a catalog change that would retarget canonical English text must fail validation | Decision 5 |
| Spanish `Perímetro` | retained compatibility observation; not an `R5-B` blocker | Decision 7 |

## Context

`AD-R0-4` decided that GeoCeDG displays, suggests, inserts and documents command
names canonically in English. Localized names remain accepted compatibility
aliases in ordinary input where current compatibility permits. XML and
serialization semantics stay unchanged, and GGBScript stays canonical
English/internal. The canonical `R5-B` prompt forbids changing the lookup
strategy, the reverse command table, XML parsing or the serialization template.
It forbids removing localized names and reusing the `localizeCmds` template
flag. It requires this ADR to settle the clash hazard before implementation.

The characterization at the source base established the following facts
mechanically. The report gives the details.

1. **Three lookup regimes, one editor convention.** `USER` accepts localized
   aliases, English alias constants and internal names, case-insensitively,
   through the reverse table and then `Commands.lookupInternal`. `SCRIPT`
   accepts any enum constant name case-insensitively and never a localized
   alias as such. `XML` accepts exact-case enum constant names only. This covers
   file loading, nested `Execute` and the API `evalCommandGetLabels` path. The
   script editor's save step,
   `App.getInternalCommand`, is a fourth convention. It matches only UI-locale
   localized names, and the first enum-order match wins. In the reverse table
   the last writer wins.
2. **Reverse-table precedence.** A localized entry always overwrites an
   internal or English entry with the same lower-case key. An internal entry is
   inserted only if absent. CAS command aliases are inserted only after the CAS
   object exists, so resolution of CAS aliases depends on session state.
3. **Two JRE "English" authorities disagree.** The English command bundle is
   what the English UI shows today, and it is what the web client's
   `getEnglishCommand` returns. It differs from the JRE `getEnglishCommand` enum
   mapper for six stored names: `Binomial`, `IntersectConic`, `mean`, `stdev`,
   `mad` and `stdevp`. The JRE mapper also returns non-command keys verbatim,
   which is why `EnglishCommandSyntax` degrades to the literal `X.Syntax` key on
   Desktop.
4. **Internal names are not English names.** 37 displayable stored names have an
   English public name that differs from the internal identifier, for example
   `OrthogonalLine`→`PerpendicularLine`, `Mirror`→`Reflect`, `LaTeX`→`FormulaText`,
   `PolyLine`→`Polyline` and `Defined`→`IsDefined`.
5. **Clash inventory for EN and ES is complete and small.** No Spanish alias
   equals the English, internal or alias-constant name of a different command.
   Every canonical English head re-resolves to its own processor identity under
   `USER` in both locales and in both reverse-table states. It does the same
   under `SCRIPT`, under exact-case `XML` and through the ES script-editor save.
   The only non-command parses are `nCr` and `nPr`, which are parser functions
   producing exactly the operation their command processors produce. One Spanish
   alias names two commands: `Perímetro` for `Circumference` and `Perimeter`.
   It resolves to either one depending on CAS state, but the two processors
   behave identically: `CmdCircumference` extends `CmdPerimeter` without an
   override.
6. **The clash class is real outside EN/ES.** The retained upstream corpus,
   scanned as diagnostic evidence only, contains canonical-head retargets in six
   of its 97 command bundles, for example French `Intersection`→`Intersect`. The
   EN/ES set is
   empty today by content, not by construction.
7. **Serialization is internal except one pre-existing defect.** XML
   `<command name>` elements and XML expression command heads are internal under
   every UI locale. The `DOT` expression head (the `Dot` command becomes
   `Operation.DOT`) is written UI-localized, e.g. ES `ProductoEscalar(u, v)`.
   Reopening such a file in either language silently drops the dependency.
8. **Heads are printed at far more sites than the Algebra view.** The sites are
   inventoried by role in the report: definitions, editable text, error
   messages, Input Help, autocomplete, script display, CAS input and GeoCeDG
   adapters. Some internal consumers parse display text, and autocomplete
   re-derives command identity from the displayed name.

## Terminology

These terms are normative for this ADR and for any implementation it governs.

- **Command identity.** The `Commands` constant the dispatcher reaches, taken up
  to processor equivalence. Constants that `Commands.englishToInternal` maps
  together, or that the command-processor factories send to one processor, are
  one identity. At the base there are 564 constants, 521 `englishToInternal`
  identities and 489 processor-equivalence classes. Identity is never inferred
  from display text, labels, UI language or display history.
- **Internal command identifier.** The enum constant name stored in XML
  `<command name>` and printed by non-localizing templates. It is persistence
  and dispatch vocabulary, not a public name.
- **Canonical English public name**, `E(k)`. The value of the English command
  bundle for definition name `k`, resolved through the `Locale.ENGLISH` chain
  `command_en.properties` then `command.properties`. It is exactly what the
  English UI shows today. All 486 displayable GeoCeDG commands have one.
- **Localized alias.** The UI-locale command bundle value that the reverse table
  accepts in `USER` input. It is compatibility vocabulary only.
- **Compatibility alias constant.** An enum constant such as `PerpendicularLine`,
  `Reflect` or `mean` that is not the internal identifier of its identity but is
  accepted by `USER`, `SCRIPT` and `XML`.
- **Syntax-bundle key.** `<internal>.Syntax`, `.Syntax3D` or `.SyntaxCAS`. It is
  a resource key, never a command name.
- **Re-entry path.** Any route by which product-presented text is parsed again:
  redefine or inline edit (`USER`), autocomplete insertion (`USER`), script
  editor save plus execution (`SCRIPT`), and text-valued command strings
  (`XML` for `Execute`).

## Decision

### 1. Policy by semantic role

Every command head is classified by the role of the output it appears in, never
by a file allowlist.

| Role | Rule under the GeoCeDG product profile |
|---|---|
| USER-VISIBLE DISPLAY | head = `E(identity)` in every UI locale |
| EDITABLE USER TEXT | head = `E(identity)`; must satisfy decisions 5 and 6 |
| SCRIPT REPRESENTATION (editor display) | head = `E(identity)`; storage unchanged; save per decision 10 |
| SERIALIZATION | unchanged: internal identifiers, including the `DOT` defect (decision 11) |
| INTERNAL ONLY | unchanged, except that a consumer parsing display output must use the same authority that produced it |
| outside the command-head policy | current UI language, by author decision: auto-label stems (`distanciaAB`), captions that reuse command translations, tool and menu names, descriptive syntax placeholders (Decision 8), and parser function names (`sen`, `tg`, `raízn`, `boceto`) |

A command head is a command name in command-call position (`Name(`, `Name[`), a
command entry in a completion list, the Input Help tree, a syntax line or an
error message naming a command. Macro (user-tool) command names are
user-authored identifiers. They are printed verbatim and never pass through a
command-name authority.

The author's rule separates two kinds of text:

```text
command identity / public command head  → canonical English
ordinary UI caption, label or prose     → current UI language
```

- **Mathematical and function names.** The author decided to retain the current
  UI-locale behaviour. In a Spanish UI, `Circle((0,0), sen(1))` is correct
  output: `Circle` is the canonical English command head and `sen` the current
  localized function name. `R5-B` does not Anglicize the expression language
  and does not change parser-function localization. The only exception would be
  later source evidence of a direct command-identity requirement, which would
  need its own decision.
- **Captions and labels.** Where current code takes a caption or label stem from
  the command bundle, the future implementation may have to give that text its
  own resource path. It must do so without changing the text the user sees in
  the UI language, and without making it a command-head authority.

### 2. Canonical English authority

`E(k)` defined above is the only authority for display, suggestion, insertion
and documentation. By author decision (amendment `R1`):

```text
CANONICAL ENGLISH PUBLIC COMMAND NAME AUTHORITY = English command bundle
```

It is neither the internal identifier nor a Desktop convenience mapping. No
semantic role takes a different authority; no exception is defined. The six
measured divergences between the two JRE English sources (Context, fact 3) are
the evidence for naming one explicit authority. Internal command identities are
unchanged, and no enum constant is renamed for presentation.
`OrthogonalLine`→`PerpendicularLine` and `Mirror`→`Reflect` stay the standing
examples that the internal name is not, in general, the public name.

- **Prohibited:** assuming internal identifier = canonical English name. Using
  the internal identifier as a display fallback is also prohibited. A
  displayable identity without an English entry fails the gate (decision 5); it
  is never printed as its internal name.
- **Prohibited as display authority:** the JRE `Localization.getEnglishCommand`
  enum mapper. It would change English-UI output for six names. It would turn
  `Binomial` into `nCr`, which is a parser function, and `IntersectConic` into
  the retired alias `IntersectCircle`. It cannot resolve syntax keys. Its
  current callers keep their current semantics; `R5-B` must not silently change
  it.
- Consequence: English-UI output is unchanged by `R5-B`, and the heads shown in
  ES are the heads shown in EN.

### 3. Separate authorities

The future implementation keeps these concepts separate even where one resource
backs two of them.

| Authority | Semantics | Backing |
|---|---|---|
| canonical display name | `E(identity)` | English command bundle |
| canonical inserted head | `E(identity)`, used by insertion sites as its own role | same bundle; separate role so insertion cannot regress to localized |
| ordinary `USER` lookup | current reverse table then `lookupInternal` | unchanged |
| compatibility aliases | localized aliases plus alias constants accepted by `USER`; searchable in autocomplete | unchanged reverse table; UI-locale bundle |
| syntax-bundle key lookup | UI-locale bundle value of `<internal>.Syntax*`, English base as parent | bundle getter, **never** a name resolver |
| `SCRIPT` lookup | `Commands.lookupInternal` | unchanged |
| `XML` / internal lookup | exact-case enum constant | unchanged |

The presentation policy is carried by a product-profile attribute, the GeoCeDG
application configuration, that is orthogonal to `StringTemplate.localizeCmds`.
That flag keeps governing brackets, extensive bracketing and scientific
notation. Templates with `localizeCmds=false` keep printing internal
identifiers. Configurations other than GeoCeDG keep the upstream localized
behaviour. The Classic application is not mutated globally, as `AGENTS.md` §6
requires.

### 4. Resolution stays unchanged

`USER`, `SCRIPT` and `XML` resolution, the reverse table and its precedence, and
every localized bundle entry stay unchanged. English names and alias constants
are already accepted in every locale, so the acceptance half of `AD-R0-4` needs
no new mechanism. The collision safety of the display half comes from decisions
5 and 6, not from changing precedence.

By author decision (Alternative D), the future implementation must not:

- reorder `USER` precedence globally so that English wins;
- rewrite the generic parser;
- replace localized input with an English-only parser.

The governing invariant is:

```text
display canonical English command head
        ↓
edit / redefine
        ↓
same internal command semantic identity
```

Where that cannot be guaranteed, the outcome is **fail closed**. The product
never silently resolves to another command.

### 5. Canonical-head admissibility gate (fail closed at verification)

The display policy is admissible only while a mechanized gate holds over the
**complete** command inventory. The gate runs for every supported UI locale (EN,
ES) and for both reverse-table states: CAS object absent, and CAS dictionary
filled. It covers every stored name `k`, meaning every `Commands` constant that
can be a definition name; the evidence checks all 564. With `h = E(k)`:

| Property | Typed failure |
|---|---|
| `E(k)` exists | `NO_ENGLISH_PUBLIC_NAME` |
| `USER` resolves `h` to `k` in each locale and state | `HEAD_NOT_SELF_RESOLVING` |
| the `USER` result is the same in both states | `HEAD_STATE_DEPENDENT` |
| `SCRIPT` resolves `h` to `k` | `HEAD_NOT_SCRIPT_RESOLVABLE` |
| `h` is an exact-case enum constant of `k` (valid in `XML`/`Execute`) | `HEAD_NOT_XML_EXACT` |
| the script-editor save of `h` (decision 10) keeps identity `k` | `SCRIPT_SAVE_NOT_IDENTITY_PRESERVING` |
| no parser function pre-empts `h`, unless listed as equivalent | `HEAD_PREEMPTED_BY_PARSER_FUNCTION` |
| no other identity has the same lower-case `h` | `HEAD_NOT_INJECTIVE` |

The only recorded equivalences are `nCr/2`→`Operation.NCR` and
`nPr/2`→`Operation.NPR`. Both are hidden alias constants. In display they
already appear as hard-coded operation heads, and re-entry parses them as the
very operation their command processors produce.
Any failure rejects the candidate. There is no runtime fallback to a localized
or internal head, and no nearest-match, label, history or UI-language repair.
The gate result at the source base is zero failures. The gate must also prove
it detects: a negative control runs it against a retained-corpus bundle with a
known member, for example French `Intersection`. That run is a test fixture
only and does not widen GeoCeDG's supported locales.

The author adopted this guard. The measured EN/ES emptiness of the retarget
class is a current property of the bundles and is **not** relied on. Any
translation or catalog change that would make canonical English text resolve to
another command identity must fail validation. It must never change semantics
silently.

### 6. Typed fail-closed re-entry for construction-dependent shadowing

The gate cannot see the current construction. Pre-existing mechanisms can make an
unchanged canonical head parse as something else:

- a GeoElement, function variable or CAS-cell label equal to the head, which
  turns `h(…)` into function application or multiplication;
- a macro whose command name equals the resolved name, which wins dispatch.

English display changes which labels shadow in ES documents. The policy must
therefore prevent a silent retarget there:

- Editable text is still presented with `E(k)`. There is no fallback spelling.
- When a submission from an editable-text surface would construct, for a
  command head the product presented, a result whose identity differs from the presented
  identity, including "no command at all", the submission is rejected. The
  outcome is typed `CANONICAL_HEAD_SHADOWED` and names the head, the shadowing
  object and the expected identity. It causes **no mutation**: construction, XML
  and undo state are unchanged.
- Text the user rewrote into different heads follows ordinary `USER` semantics.
  Presentation provenance ends where the user's own text begins.

The mechanism is left to the implementation design, within the forbidden-scope
limits: no parser, lookup-strategy or reverse-table change.

### 7. Localized-alias compatibility by context

| Context | Regime | Localized aliases | Canonical English |
|---|---|---|---|
| fresh Algebra input, pasted expressions | `USER` | accepted, current precedence | accepted |
| redefine and inline edit | `USER` | accepted when typed by the user | presented; round trip per decisions 5–6 |
| autocomplete | display, search, insertion | searchable | displayed and inserted |
| Input Help | display | not shown as tree names | shown; syntax per decision 8 |
| GGBScript runtime | `SCRIPT` | not accepted (unchanged) | accepted |
| script editor | display and save | converted on save only when `SCRIPT` cannot resolve the token | displayed; untouched text saves unchanged |
| XML files, nested `Execute`, other `XML`-strategy text | `XML` | not accepted (unchanged) | accepted: every `E(k)` is an exact enum constant |
| help examples, guides | documentation | may be listed as aliases | the documented form |
| macros | verbatim | not applicable | not applicable |

Example, Spanish UI:

- the product displays `PerpendicularLine(A, f)`;
- the user may still type the Spanish alias; the measured ES bundle value is
  `Perpendicular` (`OrthogonalLine=Perpendicular`);
- both resolve to the internal identity `OrthogonalLine`.

The author's illustration used the conceptual spelling `RectaPerpendicular`,
which is not in the ES bundle. This ADR adds no alias; acceptance stays exactly
what the current source supports.

`Perímetro` is recorded as a **retained compatibility observation and not an
`R5-B` blocker**, by author decision. It resolves to `Perimeter` or
`Circumference` depending on reverse-table and CAS state, and the characterized
processors are behaviourally equivalent. `R5-B` does not redesign CAS
initialization or reverse-table precedence for it, and must not worsen it
(`T-COLLISION`).

### 8. Syntax help

A syntax line is the canonical head `E(k)` plus the syntax body. The body is the
UI-locale value of the syntax-bundle key (`.Syntax`, `.Syntax3D`, `.SyntaxCAS`)
with the English base bundle as parent fallback. Existing syntax filters still
apply.

- Head resolution and key resolution are separate operations. A missing key
  yields a typed "no syntax" result, never the literal key.
- `EnglishCommandSyntax` is rejected as-is on the JRE, because one overridden
  resolver serves both the head and the key.
- `Localization` owns one final `LocalizedCommandSyntax` instance. Routing
  Desktop syntax through the canonical head therefore requires a
  shared-kernel seam: an injectable head strategy or a separate canonical
  syntax provider. The implementation design chooses.
- **Author decision: `SYNTAX PLACEHOLDER LANGUAGE = CURRENT UI LANGUAGE`.** A
  Spanish UI shows, for example, `Circle( <Punto>, <Radio> )`. Two authorities
  stay distinct:
  - the **command head authority**, `E(k)`, which is canonical English;
  - the **localized descriptive syntax text**: placeholders, argument names and
    explanatory prose in the UI language.

  Placeholders are presentation text and never command identity. `R5-B` does not
  force syntax prose or parameter descriptions into English.
- `HelpOnKeywordPanel`, which shows the raw `.Syntax` text, gets the canonical
  head as well.

### 9. Autocomplete

- **Display name:** `E(k)`.
- **Search tokens:** `E(k)` plus the UI-locale localized aliases of `k`.
- **Accepted input:** unchanged `USER`.
- **Inserted text:** a syntax line headed by `E(k)`.
- **Identity:** every completion entry carries the command identity. No site
  may re-derive identity from display text through `getInternalCommand` or
  `getReverseCommand`. Today's `getInternalCommand` would lose the syntax of
  every English entry in ES.
- **Macros:** listed by their own names.
- **Script-editor lexer:** its highlighting set includes the canonical names.

### 10. GGBScript boundary

- Stored scripts stay internal/English. `SCRIPT` runtime resolution is
  unchanged. UI language never changes script meaning.
- The editor displays `E(k)` for command tokens.
- Saving is canonical-first:
  - a token that `SCRIPT` already resolves is preserved as written, without
    consulting localized aliases. It may be rewritten only where the rewrite is
    proven semantics-preserving, i.e. to a spelling of the same processor
    identity;
  - only a token that `SCRIPT` cannot resolve is converted through localized
    aliases, as today;
  - a token that is both `SCRIPT`-resolvable to `B` and a localized alias of
    `A≠B` is rejected with `SCRIPT_TOKEN_AMBIGUOUS`. It is unreachable on EN/ES
    per the gate, but defined.
- Effect: the current ES rewrite of a stored `Perimeter` token to
  `Circumference` on an untouched save ends.
- CAS-cell input conversion follows the same rule.
- `PRE-G9B-R6` owns the machine-readable GGBScript compatibility gate and
  `TD-R0-GGBSCRIPT-STRATEGY-RESTORE`. `R5-B` supplies R6's locale and name-policy
  dimension, namely this decision, decision 7 and the gate of decision 5, and
  implements nothing of R6.

### 11. Serialization and persistence boundary

Presentation names and serialized construction identity are separate.

```text
public display / edit naming policy  ≠  persistent command identity
```

- `R5-B` **does not change XML or serialization semantics** (author decision).
- Native `.cedg`/`.ggb` XML command semantics remain the internal persisted
  authority.
- `R5-B` authorizes no migration and no document-format version change.
- Historical ES-UI documents reopen as they do at the future `R5-B`
  implementation base.

**`Dot` defect.**

- *Measured source fact.* The `DOT` expression head is written UI-localized. A
  Spanish-UI `Dot(u,v)` is stored as `ProductoEscalar(u, v)`, and reopening the
  file in either language drops the dependency: the object becomes an
  independent number.
- *Author disposition.* Real, pre-existing and upstream-inherited. It is **not**
  absorbed into `R5-B` and is not repaired by this ADR. It will be fixed by a
  **separate bounded maintenance task**, which must be implemented, technically
  verified, author-approved and **published before productive `R5-B`
  implementation** (Decision 17). That task owns the writer fix and any
  reader-side compatibility for historical files, under its own authorization.
- *Design consequence.* `R5-B` changes only the display role of that serializer
  site and does not touch its serialization role. The persistence invariant is
  not weakened to tolerate the defect: `R5-B` inherits the repaired
  serialization of its implementation base and must leave it byte-identical.

### 12. GeoCeDG commands

`LocusV2`, `LocusLength` and `SplineV2` follow the host policy. There is no
parallel naming system.

- Canonical names: `LocusV2`, `LocusLength`, `SplineV2`.
- ES aliases `LugarGeométricoV2` and `LongitudLugarGeométrico` stay accepted in
  ordinary input.
- The ES bundle entry for `SplineV2` exists and equals the English name.
  `R5-B` invents no alias for it.
- GeoCeDG-owned print sites obey decision 1:
  - the `Length` head of `AlgoLocusMetricScalarAdapter`;
  - `AppGeoCeDG.setShowInputHelpPanel`, which must select by the same authority
    as the tree;
  - `GeoCeDGDefinitionInspector`.
- `GeoCeDGUserToolLibrary.nativeCommand` is already canonical-first and stays
  unchanged.
- The English literal value strings of `GeoLocusV2`, `GeoLocusMetricResult` and
  `GeoLocusIntersectionResult` already comply.

### 13. Existing localization regression

`G9U0LocalizationHelpTest.l01` asserts the Spanish bundle values of `LocusV2`
and `LocusLength` and the Spanish syntax placeholders. Under this ADR both stay
true, because the alias bundle is unchanged and the author decided that
placeholders stay in the UI language. The test is not amended. Its role becomes
alias-preservation evidence, and its identity stays stable.

### 14. Architectural placement

**Shared-kernel localization seams plus Desktop consumers.** Not the parser,
not the `Command` constructor strategies, not reverse-table construction, not
XML parsing, not the serialization roles of any template, and not geometry
semantics. The minimum upstream changes the implementation may require are
listed below; this ADR writes none of them. Each needs a
`docs/upstream/modified-files.yml` entry.

1. A policy-selectable command-head presentation authority owned by the
   localization layer. The upstream default stays localized; the GeoCeDG profile
   selects canonical English.
2. English command-bundle access on the JRE for `E(k)`.
3. Separation of head resolution from syntax-key resolution in
   `LocalizedCommandSyntax`, or a separate provider.
4. Display-role routing at:
   - `AlgoElement.getDefinition` and `appendCheckVector`;
   - `Command.toString`;
   - the `ExpressionSerializer` display roles of `DOT`, `If` and `DataFunction`;
   - `GeoPieChart`;
   - `AlgoLocusStroke`;
   - the command heads of `CommandErrorMessageBuilder`, `ErrorHelper`,
     `CommandNotFoundError` and `CAScmdProcessor`.
5. `GgbScript` display and canonical-first save, and the same rule for CAS
   input.
6. Desktop consumers:
   - `AutoCompleteTextFieldD`;
   - `InputBarHelpPanelD`;
   - `AlgebraInputD.insertCommand`;
   - `HelpOnKeywordPanel`;
   - `GeoGebraLexer`;
   - `DynamicTextInputPane` and `DynamicTextProcessor`, whose matchers must use
     the display authority;
   - `AppGeoCeDG`.

### 15. Future implementation contract

The semantics are normative. The names below are illustrative, and the Java API
is left to the implementation design.

- `canonicalEnglishName(identity)` → `E(k)` or `NO_ENGLISH_PUBLIC_NAME`. Never
  the internal identifier by assumption.
- `commandHeadFor(identity, role)` → `E(k)` for display, editable and
  script-display roles under the GeoCeDG profile; the internal identifier for
  serialization; the upstream localized head for other profiles.
- `canonicalSyntax(identity, dimension, uiLocale)` → head from
  `canonicalEnglishName`, body from UI-locale syntax keys, or "no syntax".
- `lookupOrdinaryUserCommand(token, uiLocale)`, `lookupScriptCommand(token)`,
  `lookupXmlInternalCommand(token)` → today's semantics, unchanged.
- `completionEntries(prefix, uiLocale)` → entries of `{identity, E(k), matched
  alias}`, searched by `E(k)` and aliases.
- `delocalizeScriptToken(token, uiLocale)` → decision 10, returning
  `SCRIPT_TOKEN_AMBIGUOUS` when needed.
- `validateCanonicalHeads(inventory, locales, states)` → the decision 5 gate
  with typed failures.
- `validateEditableHeadReentry(presentedIdentities, parsedResult,
  construction)` → OK or `CANONICAL_HEAD_SHADOWED`, with no mutation.

### 16. Future acceptance obligations

These are design output only. `R5-B` must satisfy them. None is executed by this
task.

| ID | Obligation |
|---|---|
| `T-DISPLAY` | every DISPLAY, EDITABLE and SCRIPT-display site prints `E(k)` under ES; under EN the output is unchanged from the source base; no ES localized head survives in these roles |
| `T-INPUT` | the EN and ES reverse tables, in both states, are identical to the source base; every ES alias resolves as before; every `E(k)` is accepted in EN and ES; internal names keep exactly their current acceptance |
| `T-GATE` | the decision 5 gate passes over the complete inventory, EN/ES × both states; the French negative control is detected |
| `T-COLLISION` | `Perímetro` resolution and algorithms are unchanged; objects created through it display truthfully; no canonical head is affected |
| `T-REDEFINE` | presented editable text submitted unchanged, or with an argument-only edit, keeps identity and XML command name for representative members of every English≠internal class and for the GeoCeDG commands |
| `T-SHADOW` | label, function-variable and macro shadowing each yield `CANONICAL_HEAD_SHADOWED` with construction, XML and undo byte-identical |
| `T-AUTOCOMPLETE` | English display and insertion; ES alias search finds the identity; entries carry identity; syntax shown for every English entry under ES |
| `T-SYNTAX` | canonical head plus correctly resolved UI-locale body for every displayable identity, including 3D and CAS variants; never a literal key; Input Help, `focusCommand` and `HelpOnKeywordPanel` agree |
| `T-XML` | committed historical ES-authored documents, including `LocusV2`, `LocusLength`, `SplineV2` and English≠internal commands, reopen under EN and ES with the same command identity and values; for the same construction, saved XML bytes are unchanged relative to the implementation base, whose `Dot` serialization has already been repaired by the separate maintenance (Decision 17) |
| `T-SCRIPT` | `SCRIPT` resolution is unchanged; scripts with English, internal and ES tokens behave as at the base; in the ES editor an untouched displayed script saves byte-identical to the stored text |
| `T-GEOCEDG` | all of the above for `LocusV2`, `LocusLength`, `SplineV2` and the `Length` adapter head |
| `T-ERROR` | error messages use the English head and English syntax head with localized prose |
| `T-DYNTEXT` | dynamic-text classification keeps working under ES |
| `T-L01` | `G9U0LocalizationHelpTest.l01` passes unchanged (decision 13) |
| `T-SMOKE` | explicit Desktop author smoke in EN and in ES |

### 17. Prerequisites of productive `R5-B` implementation

Productive `R5-B` implementation stays **not authorized**. It will require
**both**:

1. `ACCEPTED_COMPATIBILITY_ADR` = the exact author-accepted and published
   identity of this ADR;
2. the separate `Dot`/XML serialization maintenance (Decision 11), implemented,
   technically verified, author-approved and published.

The future implementation base is therefore **not necessarily** `P_R5A`
(`4389b30b5478ac35c78a15255021e3c4f1e10ea3`). It is the exact published commit
that follows the `Dot` maintenance, fixed by a new explicit author instruction.
This ADR authorizes no implementation, from its design branch or from anywhere
else.

## Alternatives considered

| | A. English-first `USER` precedence | B. Separate generated-text resolver | C. Validate against `USER` and fail closed | **D. Unchanged resolver + role presentation + gate + typed re-entry (adopted by author decision)** |
|---|---|---|---|---|
| Compatibility | changes the meaning of localized input wherever a localized alias equals an English name; nil on EN/ES today but real in six corpus locales; alters redefine of historical text | ordinary input unchanged, but generated text re-enters under a different regime from the text the user edits | ordinary input unchanged | ordinary input, aliases, XML and scripts unchanged; EN output unchanged |
| Identity safety | by precedence only; blind to CAS state, labels and macros | only while provenance survives; free-text edits mix provenance | safe for name resolution; blind to construction shadowing | name level by gate; construction level by typed re-entry |
| Parser / strategy | changes the reverse table (forbidden) | needs a new strategy or text provenance through the parser (forbidden) | none | none |
| Serialization | none | none | none | none; `DOT` defect repaired by separate maintenance published before `R5-B` |
| Placement | shared kernel lookup | parser + kernel | shared kernel + Desktop | shared-kernel localization seams + Desktop consumers |
| Migration | none, but silent reinterpretation of localized text | none | none | none |
| Testability | global side effects hard to bound | provenance hard to test | good | complete-inventory gate plus focal typed-outcome tests |

**A** is rejected: forbidden scope, and a silent reinterpretation of localized
input in other languages. **B** is rejected: it would need a new lookup regime
or parser-carried provenance, and it would treat identical text differently
depending on who typed it. **C** satisfies `AD-R0-4` for name resolution, but
alone it misses construction-dependent shadowing and the separation needs of
syntax, autocomplete and scripts. **D** keeps C's validation as a
verification-time gate over the complete inventory and adds the narrow runtime
guard. The author adopted **D** in amendment `R1`. Its compatibility cost: shadowed edits in ES documents are rejected
instead of silently retargeted, and a future bundle change that breaks the gate
blocks the candidate until the author disposes of it.

Rejected naive approaches:

- **Turning command localization off globally** (`forceEnglishCommands` or
  `localizeCmds=false`) removes Spanish aliases from input and changes
  brackets and scientific notation.
- **Using internal identifiers as English names** would show `OrthogonalLine`
  and 36 more internal names.
- **Using the JRE `getEnglishCommand`** changes English output for six names and
  keeps the literal-key syntax defect.
- **Changing parser or lookup precedence without compatibility analysis** is
  alternative A.
- **Localizing XML**, or **localizing stored GGBScript**, lets UI language
  change persisted meaning.
- **Fixing only Algebra Input and autocomplete** produces mixed-language output
  at the other print sites.
- **Using the current UI language as identity**, or **accepting an ambiguous
  round trip because the text looked right**, contradicts the invariant.
- **Deriving identity from display text in autocomplete** fails for English
  entries under ES.
- **Switching every `loc.getCommand` site blindly** would silently change the
  XML bytes of the `DOT` site.
- **Resolving `Perímetro` by editing the reverse table** is forbidden scope, and
  unnecessary because the ambiguity is benign.

## Consequences

- One canonical English authority exists for display and insertion. It equals
  current English output, so the EN UI does not move.
- Localized input keeps working exactly as today. The reverse table,
  persistence and GGBScript semantics are untouched.
- Collision safety becomes a mechanized, complete-inventory property rather
  than an assumption about bundle content. A future locale or bundle change
  cannot make the hazard live silently.
- Some ES edits that today silently re-parse as a shadowing label will instead
  fail with a typed error.
- The `DOT` persistence defect is documented, isolated and assigned to a
  separate maintenance task, which blocks productive `R5-B` until it is
  published.
- Spanish displays mix English command heads with Spanish function names and
  placeholders, by author decision, e.g. `Circle((0,0), sen(1))`.
- `R5-B` remains a shared-kernel localization change with Desktop consumers
  under `INTEGRATED_PHASE`, and a user-guide update (`GUIDE_IMPACT`,
  guides §16.3) will be required.

## Author decisions

The initial candidate left five choices open. Amendment `R1` resolves all of
them:

| Initial open choice | Author decision (amendment R1) |
|---|---|
| alternative | D |
| syntax placeholder language | current UI language |
| `DOT` localized-XML defect | separate bounded maintenance, published before productive `R5-B`; not inside `R5-B` |
| parser function names (`sen`, `tg`) | retain current UI-locale behaviour |
| captions and label stems | current UI language |

The only remaining decision is the author's approval, amendment or rejection of
this exact amended commit.

## Candidate consequences for the canonical `R5-B` prompt (not applied)

The prompt is unchanged here. After this ADR is accepted, a governance-layer
amendment task would need to:

- name this ADR's exact accepted identity in `ACCEPTED_COMPATIBILITY_ADR`;
- set `IMPLEMENTATION_BASE` to the exact published commit after the `Dot`/XML
  maintenance, not `P_R5A`;
- state that the `Dot`/XML defect has already been repaired externally by that
  maintenance and remains outside `R5-B`;
- define the "English-name resolver" as the English command bundle `E(k)`, not
  the JRE `getEnglishCommand` and not the internal identifier;
- drop the "policy-derived Algebra prefix literal", which `PRE-G9B-R4` removed;
- correct the stale R0 statements:
  - finding 3: `SplineV2` has an ES bundle entry equal to its English name;
  - "Construction XML always stores internal names": false for `DOT` before the
    maintenance;
  - the clash hazard: the mechanism is real, but the EN/ES member set is empty
    today;
- restate "text-valued command strings require internal names" as: `Execute`
  and other `XML`-strategy strings accept exact-case enum constants, meaning
  internal names or English alias constants. Every canonical English head is
  one; localized aliases are not accepted;
- record the author's placeholder, function-name and caption decisions;
- add the gate, shadowing, autocomplete-identity and dynamic-text obligations to
  the focal tests.

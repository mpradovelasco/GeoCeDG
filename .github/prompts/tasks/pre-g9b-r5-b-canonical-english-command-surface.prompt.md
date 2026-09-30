# PRE-G9B-R5-B — canonical English command surface

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-09-30 names `PRE-G9B-R5-B`, its exact
implementation base and its accepted compatibility ADR. That instruction also
authorizes this governance-layer amendment as part of `R5-B`. The amendment
replaces the earlier proposed stub (blob
`34e1796d5628108498a7c612c155d5c8d72a4c16`), whose planning text predated
ADR 0031 and the published `Dot`/XML maintenance. This file is an execution
contract, not a second policy document: the policy is ADR 0031.

```text
implementationAuthorized = true
authorApproved           = false
passClaimed              = false
selfApproved             = false
```

`authorApproved = false` means that the technical candidate this phase produces
has not been author-approved. Technical verification never creates author
approval.

<!-- geocedg-field: objective -->
## Objective

Implement ADR 0031 (Alternative D). GeoCeDG **displays, suggests, inserts and
documents** command heads in canonical English, independently of the UI
language. Localized command names stay accepted compatibility aliases in
ordinary `USER` input exactly where the current lookup accepts them. `USER`,
`SCRIPT` and `XML` resolution, the reverse command table, the parser and XML
serialization stay unchanged. GGBScript semantics stay canonical
English/internal.

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = INTEGRATED_PHASE
frozenAtPhaseStart  = true
ACCEPTANCE          = INTEGRATION
```

The class is frozen at phase start. It is not downgraded because individual
edits are small, and it is not escalated to `FINAL` because the change crosses
the shared kernel and Desktop. Section 12.8 of
`geocedg/specs/operations/verification-levels.md` maps `INTEGRATED_PHASE` to
PHASE plus COMPOSED (`INTEGRATION`) when the frozen plan identifies concrete
additional integration coverage. This plan identifies it: the change crosses
shared localization seams, Desktop presentation, script display and the
existing localization, parser, XML, redefine and help regressions of both JUnit
modules.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
504a19f7e655ef50c4bfa8095a07f94c11b27a01          (P_DOTXML)

IMPLEMENTATION_BASE_TREE =
63e7f0cd79aa9e1cf1259b6466e8ac16dc261704

ACCEPTED_COMPATIBILITY_ADR =
e16697d96858639becf2dc72f7c7beaca8fcbd24          (published ADR 0031 closeout)

ADR_DECISION_CONTENT =
c459de9ab66d61fe849abbf9b9b8097c56605c48          (approved amended ADR candidate,
                                                    tree 3c8d0943f2be7b990e0e4cc97cd114203dd47a49)

DOT_XML_PREREQUISITE =
SATISFIED / PUBLISHED
```

`P_DOTXML` carries the linear published history
`e16697d9… → 4f9bc944… (M_DOTXML, author approved) → 504a19f7…` (the `Dot`/XML
maintenance closeout). A moving branch is not a base, and the task is not rebased
onto any later commit without a new author instruction.

## Authority and evidence hierarchy

1. [ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md),
   `ACCEPTED — AUTHOR APPROVED`: the governing policy. It takes precedence over
   planning prose, including the earlier text of this prompt.
2. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry,
   `tools/agent/verify.ps1`).
3. Current source and tests at `P_DOTXML`: the shared localization layer, the
   command lookup strategies, the reverse command table, the command-syntax
   provider, every command-head print site, the Desktop input bar, autocomplete,
   Input Help and script editor, and the command bundles.
4. The [ADR 0031 closeout](../../../docs/validation/pre_g9b_r5_b_compatibility_adr_closeout_record.md)
   and the [`Dot`/XML maintenance closeout](../../../docs/validation/maintenance_dot_xml_locale_independent_serialization_closeout_record.md).
5. Characterization evidence only: the
   [ADR candidate report](../../../docs/validation/pre_g9b_r5_b_compatibility_adr_candidate_report.md)
   and its design evidence, measured at `P_R5A`. It is re-inventoried at
   `P_DOTXML` before coding and is never runtime authority.
6. Historical planning input only: the
   [PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
   §4 `PRE-G9B-R5-B` block and the
   [R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
   §7.1.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Exactly the productive scope of ADR 0031 decisions 1–15:

- **One canonical English authority.** `E(k)` is the English command-bundle
  value for definition name `k` (`Locale.ENGLISH` chain: `command_en` then the
  base bundle). It is not the internal identifier, not the JRE
  `getEnglishCommand` enum mapper, not the UI-locale bundle, not a label and not
  text recovered by reverse lookup. Semantic command identity, `E(k)`,
  localized compatibility aliases and presentation context stay four separate
  concepts.
- **A policy-selectable command-head presentation authority** owned by the
  shared localization layer. The upstream default stays localized; the GeoCeDG
  product profile selects canonical English. It is orthogonal to
  `StringTemplate.localizeCmds`, which keeps governing brackets, bracketing and
  scientific notation.
- **Syntax.** Separate head resolution (`E(k)`) from syntax-bundle key
  resolution (UI-locale `.Syntax`, `.Syntax3D`, `.SyntaxCAS` with the English
  base as parent). A missing key is a typed "no syntax" result, never the
  literal key. Applies to Input Help, `focusCommand`, `HelpOnKeywordPanel`,
  autocomplete syntax and the 3D/CAS variants.
- **Display-role routing** at every command-head print site of the roles
  USER-VISIBLE DISPLAY, EDITABLE USER TEXT and SCRIPT REPRESENTATION, classified
  site by site at `P_DOTXML` before coding (ADR decisions 1 and 14). Internal,
  persistence, reparse and CAS/export consumers keep their vocabulary; a
  consumer that parses display output uses the authority that produced it.
- **Autocomplete** that displays and inserts `E(k)`, finds an entry through its
  UI-locale aliases, and carries the command identity in each entry, never
  re-derived from display text.
- **GGBScript editor** display with `E(k)` and canonical-first save
  (decision 10), with the same rule for CAS-cell input.
- **Typed fail-closed re-entry** `CANONICAL_HEAD_SHADOWED` for presented
  editable text (decision 6), with no construction, XML or undo mutation.
- **The complete-inventory admissibility gate** of decision 5, EN/ES × both
  reverse-table states, with the retained-corpus negative control.
- **GeoCeDG commands** `LocusV2`, `LocusLength`, `SplineV2` and the `Length`
  adapter head, through the host policy only.
- **Guides.** `GUIDE_IMPACT = UPDATED`: the official English and Spanish user
  guides, §16.3 or its structurally corresponding section, with EN/ES
  structural parity and the `R5-A` stable-anchor contract; packaged copies
  follow the documentation-maintenance contract.
- Upstream provenance in `docs/upstream/modified-files.yml`; JUnit inventory and
  registry pin only if genuinely required and only through the official
  updater; the candidate report and its machine-readable evidence mirror.

### Corrections to superseded planning statements

The earlier text of this prompt recorded `R0` findings that ADR 0031 and the
published maintenance supersede. For execution:

1. ADR 0031 exists and is accepted; the `AD-R0-4` policy is formalized there.
2. The separate `Dot`/XML maintenance is completed and published. At `P_DOTXML`
   the `Dot` head is localized only by templates that localize command names;
   `R5-B` neither repeats nor redesigns that repair.
3. `SplineV2` **has** an ES command-bundle entry, equal to its English public
   name. No alias is invented for it.
4. The "policy-derived Algebra prefix literal" is **removed** from scope:
   `PRE-G9B-R4` removed the `SplineV2(` prefill.
5. The correct canonical resolver is the English command bundle `E(k)`. The JRE
   `getEnglishCommand` differs from it for six stored names and keeps its current
   callers and semantics.
6. `XML`-strategy command strings (`Execute`, file loading, `evalCommandGetLabels`)
   accept exact-case enum constants: internal names and English alias
   constants. Localized aliases are not an XML vocabulary. Every `E(k)` is such
   a constant (gate property `HEAD_NOT_XML_EXACT`).
7. The name-level clash mechanism is real; its EN/ES member set is empty at the
   characterized base. That emptiness is not relied on: the gate enforces it.

### Author decisions carried by ADR 0031

```text
syntax placeholders and argument descriptions = current UI language
mathematical / function names (sen, tg, …)    = current UI-locale behaviour
captions and automatic presentation labels    = current UI language
GGBScript                                     = canonical English/internal semantics
XML / serialization                           = unchanged by R5-B
Spanish Perímetro                             = retained observation; must not worsen
```

Under an ES UI, `Circle((0,0), sen(1))` is the correct output.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not change command lookup precedence, the lookup strategies, the reverse
command table or its construction; remove, rename or add localized aliases;
change the parser; change XML/file parsing, serialization semantics or any
template's serialization role; change internal command identifiers; infer
identity from display text; reuse `StringTemplate.localizeCmds` as the naming
switch; undo or redesign the published `Dot`/XML maintenance; introduce a second
command registry or a second parser/lookup regime for generated text; migrate
historical XML; or touch `R6`, `R7`, `G9B`, `G9C`, `G9U2`, productive `G10` or
further `G12`. No tag or release.

## Architectural placement

Shared-kernel localization seams (the `E(k)` authority, the common command-head
presentation, syntax resolution required consistently by every consumer, and
the identity-preserving re-entry semantics) plus Desktop presentation consumers
(autocomplete, Input Help, help-on-keyword, the script-editor lexer and the
GeoCeDG profile). Not the parser, not the `Command` strategies, not
reverse-table construction, not XML parsing, not serialization, not geometry.
Presentation-only orchestration stays in Desktop.

## Required design/specification

ADR 0031, accepted and published as `ACCEPTED_COMPATIBILITY_ADR`. Before coding,
inventory and classify every affected print and insertion site at `P_DOTXML` by
semantic role (`DISPLAY`, `EDITABLE`, `SCRIPT DISPLAY`, `USER INPUT`,
`SCRIPT INPUT`, `XML/PERSISTENCE`, `INTERNAL REPARSE`, `CAS/EXPORT`, `OTHER`) and
design the API and identity flow. No second policy document is created.

## Geometric invariants and degeneracies

Not applicable to geometry. The governing invariant is semantic identity:

```text
canonical English presentation → edit / redefine → same internal command semantic identity
```

No display transformation may silently retarget a command. Identity is never
inferred from rendered or localized text, labels, coordinates, proximity,
construction position or an enum/display-name coincidence. Ambiguity fails
closed with a typed outcome and no mutation.

## Compatibility and serialization

For the same construction, XML after `R5-B` is byte-identical to XML at
`P_DOTXML`. Historical ES-authored documents reopen under EN and ES with the same
command identities and values, including English≠internal commands, `LocusV2`,
`LocusLength`, `SplineV2` and `Dot` under the repaired base behaviour.
`TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` is retained unchanged: `R5-B` does not
repair historical localized `Dot` XML. `SCRIPT` resolution is unchanged; stored
scripts keep their valid tokens; an untouched displayed script saves without
retargeting. No migration and no document-format version change.

`G9U0LocalizationHelpTest.l01` passes unchanged: it becomes alias-preservation
evidence (ADR decision 13). An old test asserting Spanish command presentation
that ADR 0031 intentionally changes is updated narrowly, with the reason
documented; unrelated assertions are not weakened.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every ADR 0031 §16 obligation:

| ID | Obligation |
|---|---|
| `T-DISPLAY` | every DISPLAY, EDITABLE and SCRIPT-display site prints `E(k)` under ES; EN output unchanged from the base; no ES localized head survives in these roles |
| `T-INPUT` | EN and ES reverse tables, in every characterized state, behaviourally identical to the base; every ES alias accepted as before; every `E(k)` accepted in EN and ES; internal names keep their acceptance |
| `T-GATE` | the decision 5 gate passes over the complete inventory, EN/ES × both states; the retained-corpus negative control (French `Intersection`) is detected |
| `T-COLLISION` | `Perímetro` resolution and algorithms unchanged; canonical heads unaffected |
| `T-REDEFINE` | presented editable text submitted unchanged, or with an argument-only edit, keeps identity and XML command name for every English≠internal class and the GeoCeDG commands |
| `T-SHADOW` | label, function-variable and macro shadowing each yield `CANONICAL_HEAD_SHADOWED` with construction, XML and undo unchanged |
| `T-AUTOCOMPLETE` | English display and insertion; ES alias search; entries carry identity; correct syntax under ES |
| `T-SYNTAX` | canonical head plus resolved UI-locale body for every displayable identity, 3D and CAS included; never a literal key; Input Help, `focusCommand` and `HelpOnKeywordPanel` agree |
| `T-XML` | historical ES documents reopen under EN and ES with the same identities and values; saved XML byte-identical to `P_DOTXML` |
| `T-SCRIPT` | `SCRIPT` resolution unchanged; English/internal/ES tokens behave as at the base; untouched ES editor save preserves the stored text |
| `T-GEOCEDG` | all of the above for `LocusV2`, `LocusLength`, `SplineV2` and the `Length` adapter |
| `T-ERROR` | English command head and English syntax head with localized prose |
| `T-DYNTEXT` | dynamic-text classification and behaviour correct under ES |
| `T-L01` | `G9U0LocalizationHelpTest.l01` passes unchanged |
| `T-SMOKE` | an explicit EN/ES author-smoke checklist; the agent does not perform author smoke |

Then adjacent localization, parser, XML, script, redefine and help regressions;
Checkstyle; upstream-boundary validation; JUnit inventory and registry pin only
if genuinely required, through the official updater with executed selection
evidence; and `git diff --check`.

Acceptance, on one frozen clean immutable candidate:

```text
tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/<fresh>
```

`INTEGRATION` executes the complete shared and Desktop JUnit selections, and
therefore completely executes and authenticates this phase's focal
obligations in the same cohort (`verification-levels.md` §12.8). `STATIC` runs
separately only where a documentation or guide contract needs evidence
`INTEGRATION` does not cover. A narrow focal PASS never substitutes for
`INTEGRATION`. `FINAL` is not run unless current authoritative governance
requires it because the implemented scope changed classification; that
contradiction stops the phase before a heavier campaign.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: this amendment, local implementation, local commits, technical
verification and one frozen technical candidate `T_R5B` for author review and
EN/ES author smoke. Not authorized: author approval, `R6` — whose locale
dimension consumes this phase's outcome — `R7`, `G9B` and every later gate.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only. Push of the working branch, promotion to `main`, merge,
rebase, squash, amend after candidate freeze, tag, release, binary publication
and public distribution are forbidden; each needs a separate explicit author
instruction naming the exact candidate SHA.

## Acceptance and closeout

The phase stops with one technically verified candidate pending author review.
**Author smoke is required in both EN and ES UI** and is performed by the
author. Author approval is an explicit decision naming the exact candidate
commit; the candidate report records `AUTHOR_DECISION =
NOT_RECORDED_IN_THIS_ARTIFACT`.

## Required artifacts

Source and focal tests; EN/ES guide updates; upstream provenance; any
legitimately changed verification metadata; a candidate report under
`docs/validation/` distinguishing accepted policy, observed source facts,
implementation decisions, regression evidence, retained observations and
pending author decisions; its machine-readable evidence mirror; the exact
`INTEGRATION` command, exit code, run and log identities; bootstrap- and
infrastructure-impact outcomes; `GUIDE_IMPACT = UPDATED`; and the EN/ES
author-smoke checklist.

## Stop conditions

Stop and report rather than improvise when: `main` differs from `P_DOTXML` at
entry; ADR 0031 authority is inconsistent; the `Dot`/XML prerequisite is not
published; canonical English presentation would need a parser or
lookup-precedence change; collision safety would need a reverse-table change;
XML or serialization would change; a generated English head could silently
retarget under edit or redefine and the typed fail-closed outcome cannot prevent
it; autocomplete cannot carry identity without a broader redesign; a
mixed-language command head remains at an unclassified presentation site;
current governance requires a different verification class; or the work would
belong to `R6` or later.

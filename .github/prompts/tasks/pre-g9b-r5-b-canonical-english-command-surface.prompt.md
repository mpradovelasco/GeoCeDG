# PRE-G9B-R5-B — canonical English command surface

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

**Additionally requires the compatibility ADR described by `PRE-G9B-R0` to be
written and accepted before implementation.** That ADR does not yet exist.

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R5-B` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
BLOCKED_ON               = accepted compatibility ADR (AD-R0-4 policy)
```

<!-- geocedg-field: objective -->
## Objective

Present command names canonically in English independently of UI language, while
keeping localized names as accepted input aliases.

`AD-R0-4` is the author-decided policy this phase formalizes and implements:

- GeoCeDG **displays, suggests, inserts and documents** command names canonically
  in English;
- localized command names remain **accepted compatibility aliases** in ordinary
  user input where current compatibility permits;
- **XML and serialization semantics remain unchanged**;
- **GGBScript remains canonical English/internal**.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze
`VERIFICATION_CLASS = INTEGRATED_PHASE` before implementation begins, with
`frozenAtPhaseStart=true`.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
ACCEPTED_COMPATIBILITY_ADR = UNRESOLVED — MUST BE NAMED EXACTLY AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace these fields with the exact commit and tree the author explicitly
authorizes, and with the exact accepted compatibility ADR identity. A moving
branch name is not sufficient. **Execution is forbidden while either field
remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

The accepted compatibility ADR is the governing authority for the policy and
takes precedence over planning prose. Then `AGENTS.md`; current shared-kernel
localization source, including the command lookup strategy, the reverse command
table, the localization object's command-syntax instance and every command-head
print site; the Desktop input-bar and autocomplete source; the command
localization bundles; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§4 (`PRE-G9B-R5-B` block) and the
[R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
§7.1 as planning input only.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- A canonical display-name mechanism routed through the **English-name resolver**
  and applied at **every** command-head print site.
- An English syntax path that actually resolves syntax keys on the JRE/desktop
  client.
- English heads in the desktop completion list.
- The script-editor display policy.
- The policy-derived Algebra prefix literal.
- Guide and matrix updates.

### Four R0 findings that shape this slice

1. English and internal command names are **already accepted** in the Algebra
   input in every locale, because the reverse lookup falls back to the internal
   table. The alias half of the policy needs no new mechanism.
2. The correct canonical resolver is the **English** name, not the internal name.
   Several commands have internal names differing from their English names, so
   switching localization off naively would display internal identifiers. This is
   the single largest naive-application failure.
3. The three GeoCeDG-added commands are `LocusV2`, `LocusLength` and `SplineV2`.
   Spanish names exist for the first two and not for the third — part of the
   inconsistency this slice removes.
4. **This is not a frontend-only change.** Command heads are printed at roughly a
   dozen sites beyond the two obvious ones, including a GeoCeDG-owned kernel
   adapter and several function-head sites in the expression serializer, so a
   hook at only two sites would produce mixed-language output. And the desktop
   syntax-help path is **not selectable**: the localization object holds a single
   final command-syntax instance with a getter and no setter, so routing desktop
   syntax help through an English provider requires a **shared-kernel change**.
   The existing English syntax class is **not** a drop-in: it overrides the
   resolver used for both the head and the syntax-bundle key, and on the JRE
   client that resolver returns the bundle key verbatim, so the syntax degrades
   to the literal key and is rejected as command-not-found.

Plan this as a shared-kernel localization change with a desktop consumer, not as
a presentation tweak.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not change the command lookup strategy. Do not change XML/file parsing or the
serialization template. Do not change the reverse command table or remove any
localized command name. Do not reuse the existing localize-commands template flag
as the naming switch — it also governs scientific-notation display. **This must
not become a parser rewrite.** Do not touch `R6`, `R7`, `G9B`, `G9C`, `G9U2`,
productive `G10` or further `G12`. Do not publish, tag, release or push a
publication ref.

## Architectural placement

Shared kernel localization seams plus Desktop presentation. **Not** the parser
and **not** serialization. Construction XML continues to store internal names and
to load under the XML strategy.

## Required design/specification

An ADR covering the display, insertion and documentation policy; the
alias-acceptance policy; and the localized-name clash hazard. **Implementation
may not begin before that ADR is accepted.**

The clash hazard must be covered explicitly: a localized name equal to another
command's English name resolves to the localized meaning, because the localized
entry is inserted after the internal one. With English display plus user-strategy
parsing this can silently redefine an object on edit — a semantic-identity
hazard, not a cosmetic one.

## Geometric invariants and degeneracies

**Not applicable to geometry itself**, but one semantic-identity invariant
governs this phase: no naming or display change may cause a different object to
be resolved, redefined or constructed than the user intended. Command resolution
must remain deterministic and must never silently retarget an existing object.

## Compatibility and serialization

Construction XML is unchanged. Historical Spanish-UI documents must open and
recompute identically. Scripts remain canonical English/internal; scripts stored
in documents were delocalized at authoring time and load untranslated. The risk
lies in editing round-trips and in text-valued command strings, which already
require internal names.

An existing localization regression asserting the Spanish display names is a
deliberate barrier; the ADR must decide explicitly whether to amend it.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: English display under an ES UI **across every print site**;
localized alias still accepted; round trip of a historical ES-authored document;
definition presentation; autocomplete insertion; syntax-help resolution on the
desktop path; script editor round trip; and the documented clash class.

Acceptance: `INTEGRATION`, because the change crosses shared localization,
presentation and existing localization regressions. Then `git diff --check` and
one clean immutable candidate commit.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.

Execution requires **both** an explicit author instruction naming
`PRE-G9B-R5-B` with an exact implementation base **and** an accepted
compatibility ADR carrying the `AD-R0-4` policy. `AD-R0-4` decides the policy
direction; it does not by itself authorize implementation.

`R6` — whose locale dimension depends on this phase's outcome — `R7` and every
later gate remain unauthorized. No tag, release, publication or commercial action
is implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

The phase stops with a candidate pending author review. **Author smoke is
required in both EN and ES UI.** Author approval is an explicit decision naming
the exact accepted commit.

## Required artifacts

The accepted compatibility ADR; source and focal tests; guide and matrix updates;
a candidate report under `docs/validation/`; the exact required-level commands
with exit codes and log paths; bootstrap- and infrastructure-impact outcomes; and
`GUIDE_IMPACT` — a user-visible command presentation change normally requires a
user-guide update.

## Stop conditions

Stop and report rather than guess when the clash class cannot be made safe
without changing parsing — that is a separate compatibility decision and must
stop the phase; when mixed-language output cannot be eliminated without touching
a forbidden seam; when the compatibility ADR is absent or ambiguous; or while
`IMPLEMENTATION_BASE` or `ACCEPTED_COMPATIBILITY_ADR` remains `UNRESOLVED`.

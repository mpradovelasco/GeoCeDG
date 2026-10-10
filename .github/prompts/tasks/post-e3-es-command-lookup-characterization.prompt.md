# POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION — Spanish command lookup after a legacy `.ggb` load

**CANONICAL EXECUTION PROMPT — PREPARED. CHARACTERIZATION AND DESIGN
AUTHORIZED; CORRECTION NOT AUTHORIZED.**

The author's instruction of 2026-10-10 (`PRE-G9B-R6-plus-E3-AUTHOR-CLOSEOUT`,
with its clarification of the same day) authorizes this activity with the
bounded scope below and orders that it run later as its own task and branch,
preferably after the publication of the `E3`/`E3-R1` closeout. The
authorization is recorded, versioned, in the
[E3 and E3-R1 closeout record](../../../docs/validation/pre_g9b_r6_plus_e3_closeout_record.md)
§8. Where this prompt and that record differ, the record prevails. This file is
an execution contract, not a second policy document.

```text
POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION =
PREPARED — CHARACTERIZATION AND DESIGN AUTHORIZED — CORRECTION NOT AUTHORIZED

observation              = OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP
selfApproved             = false
authorApproved           = false
implementationAuthorized = false   (no product, test-surface or localization change)
passClaimed              = false
PHASE_KIND               = CHARACTERIZATION AND CORRECTIVE DESIGN — NO PRODUCT CHANGE
CHANGE_ROUTE             = ORDINARY
VERIFICATION_CLASS       = DOCUMENTATION_STATUS_ONLY (proposed; freeze at task entry)
ORDER                    = 1 of the three POST-E3 activities
```

The observation as recorded by the `E3` candidate report §12: in the Desktop
test harness, after `loadXML` of `Templatev7.ggb` and `setLanguage("es")`, no
Spanish command name resolves (`Punto`, `CotaAlineada` and `MarcoISOA` alike;
`getReverseCommand` returns `null`); in a fresh application they resolve. It
was not characterized further and is not attributed to any phase.

<!-- geocedg-field: objective -->
## Objective

Establish, with reproducible evidence, what the observation is and where it
comes from, and propose — without implementing — the correction if one is
needed. Characterize at least:

1. a fresh application versus a loaded document;
2. the normal File > Open route versus programmatic `loadXML`;
3. a language change before and after loading;
4. `App.getReverseCommand` (`source/shared/common/src/main/java/org/geogebra/common/main/App.java`)
   versus `Localization.getReverseCommand` (`.../main/Localization.java`);
5. command-dictionary initialization and invalidation;
6. canonical English and localized command lookup;
7. existing `E2` commands (`AlignedDimension`, `LinearDimension`) and `E3`
   `IsoABorder`;
8. GeoCeDG versus Classic;
9. real GUI behaviour versus test-harness behaviour.

Reproduce on the published pre-`E3` baseline and on the approved `E3-R1`
candidate, and classify the defect as pre-existing, introduced by a specific
phase, confined to the test harness, and/or visible to normal users. Freeze
`VERIFICATION_CLASS` at entry; `DOCUMENTATION_STATUS_ONLY` is proposed because
the deliverable is documentation, a JSON mirror and possibly an uncatalogued
prompt.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the author when starting the task: the
published `E3`/`E3-R1` closeout on `main` (preferred) or, failing that, the
local closeout commit. This prompt is part of that commit and cannot name it;
record the exact identity at entry and stop if it differs from the author's
instruction. Reproduction baselines:

```text
published pre-E3 baseline  ff56099184544e5988c63e0ea3339e3d0fe01fac  tree 1cdd1e986df785f5f16e7887300d9034e5e4a677
approved E3-R1 candidate   7cd501167c856eef356888ca87ed3a0c97cea55c  tree 3c51c9d3635c22939df6cab60259521f885eff92
```

Probe older trees with `git archive` or a detached worktree in the session
scratchpad; never check them out over the working tree.

## Authority and evidence hierarchy

`AGENTS.md`; the current code and build; ADR 0031 and the R5-B canonical
English command surface; `geocedg/specs/operations/verification-levels.md`;
the `E3` candidate report §12 and the closeout record. Previous agent output is
evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Scratch probes (JUnit through a Gradle init script, windowed Desktop probes)
  kept in the session scratchpad, never in tracked source paths.
- New documentation: `docs/validation/post_e3_es_command_lookup_characterization_report.md`
  and its JSON mirror under `geocedg/validation/post-e3/`.
- If a correction is justified: one proposed correction prompt under
  `.github/prompts/tasks/`, marked `PREPARED — NOT AUTHORIZED`.
- Status lines in the roadmap and in the mini-track plan.

`PRODUCT_PHASE_EFFECT = NONE`.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change to command lookup, command-dictionary initialization, reverse
  tables, `Localization`, language switching or ADR 0031 compatibility
  behaviour.
- Any product, test, build, registry, inventory, verifier or serialization
  change; `Templatev7.ggb`, `models/legacy/` originals and
  `tools/legacy/ingest.ps1`.
- The other POST-E3 activities, and any unrelated defect found during the
  characterization (record it; do not fix it).
- F1, F2, F3, F4 and G.

## Architectural placement

Not a placement decision yet. The characterization identifies the owning layer
(upstream localization, GeoCeDG command surface, document load lifecycle or
test harness) for a later, separately authorized correction.

## Required design/specification

ADR 0031 and the R5-B command-surface contract govern; this activity proposes
no amendment unless the evidence requires one, and then only as a proposal.

## Geometric invariants and degeneracies

Not applicable: command-name resolution is non-geometric.

## Compatibility and serialization

No document, macro or file-format change. A proposed correction must keep
ADR 0031 compatibility and legacy document behaviour.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- The probes above on both baselines, with exact commands, exit codes and
  result-file hashes.
- `tools/agent/verify.ps1 -Profile STATIC` on the committed documentation
  (commit first; STATIC binds `HEAD`).
- A proposed prompt, if any, validated with the prompt-contract parser
  (`Test-PromptContractDocument`, profile `task`), because STATIC does not cover
  uncatalogued prompts.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: characterization, root-cause analysis, corrective design and a
proposed correction prompt. Not authorized: any correction, even a one-line
one. Approval of the characterization never implies approval of the proposed
correction.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local commit of the documentation is authorized. Push, merge, fast-forward of
`main`, tag and release are not.

## Acceptance and closeout

The task stops with a characterization pending author review. The report and
its mirror record `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later
author record is the sole authority for its approval.

## Required artifacts

The characterization report (matrix of the nine dimensions on both baselines,
classification, root cause or the strongest supported hypothesis, owning
layer), its JSON mirror, the proposed correction prompt if required, the exact
commands and verification evidence, and the final status block.

## Stop conditions

- Base identity differs from the author's instruction, or the tree is dirty at
  entry.
- The only way to establish causality is a product change.
- The evidence shows a user-visible data-loss or document-corruption risk:
  report it to the author before continuing.
- Any request to change localization or command lookup within this task.

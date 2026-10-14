# PRE-G9B-R6-plus-G — help, product metadata, integrated verification and R6-plus closeout

**CANONICAL PROMPT CANDIDATE — REVISION 2 (RECONCILED) — PREPARED — PENDING AUTHOR REVIEW —
NOT AUTHORIZED.**

| Revision | Origin | Base |
|---|---|---|
| 1 | G readiness operation of the author instruction "GeoCeDG — F4-A PUBLICATION + F4 AGGREGATE CLOSEOUT + G READINESS" (2026-10-14) | `abe12893`; kept as history |
| 2 | author instruction "GeoCeDG — PRE-G9B-R6-plus-G AUTHOR DECISION RECONCILIATION" (2026-10-14) | published `main` `abe12893` |

Inputs:
- the [readiness package](../../../docs/architecture/pre_g9b_r6_plus_g_readiness_package.md);
- the [author decision reconciliation](../../../docs/architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md),
  which holds decisions `DQ-G-1` to `DQ-G-10` and the readiness assessment;
- the [product metadata contract candidate](../../../docs/architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md);
- the [debt and observation disposition proposal](../../../docs/validation/pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md).

Nothing here authorizes implementation, verification campaigns, publication, release or
`PRE-G9B-R7`. A choice marked **[DQ-G-n]** is a proposal pending the author.
`DQ-G-10` is resolved: the publication is completed and the exact base is established.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. Close the `PRE-G9B-R6-plus` mini-track as planned
([mini-track plan](../../../docs/architecture/pre_g9b_r6_plus_minitrack_plan.md) §3.2,
§12) with:

1. bilingual guide and in-product help for every accepted R6-plus capability;
2. the GeoCeDG-owned About surface required by `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`;
3. the integrated verification of the accepted subphases on G's own frozen candidate;
4. the structured debt and observation register with author dispositions;
5. the global R6-plus closeout record, pending author approval.

Proposed `VERIFICATION_CLASS = INTEGRATED_PHASE` **[DQ-G-1]**, frozen only by the G
implementation authorization. Proposed `CLOSEOUT_MODE = AUTHOR_OPERATED`; the author
selects it explicitly.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the G implementation authorization. Recommended: the
published `main` `abe12893b74f574c248117fbe873633a449e23e0`, tree
`ba4298c5658bf3f897455db5a49a76fea7105e72`. It contains:
- the F4 aggregate closeout `19f8f206`;
- the readiness package.

If the author approves and publishes the documentary reconciliation commit(s), the
authorization may name that published successor instead. A local documentary commit is
never the base by default.

Entry gate:
- exact identity;
- a clean worktree;
- local, tracking and live remote `main` agree;
- every hard predecessor of plan §3.1 is `PASS — AUTHOR APPROVED` and is an ancestor of
  the base.

The prerequisite matrix of the reconciliation (§1.3) is re-checked on the base. F4 is an
operational predecessor, not a hard dependency. Any missing predecessor or contradiction
stops the phase.

## Authority and evidence hierarchy

`AGENTS.md`; current code and build; the approved specifications, in particular the
[Windows packaging contract](../../../geocedg/specs/packaging/windows-packaging.md), the
[application profile](../../../geocedg/specs/ui/application-profile.md) and the
[documentation-maintenance contract](../../../geocedg/specs/operations/documentation-maintenance.md).
Then:
- the licensing authorities: `LICENSE`, `NOTICE.md`, `THIRD_PARTY.md`, `docs/LICENSE.md`,
  the [component matrix](../../../docs/licensing/component-matrix.md), and the NC and
  COMMERCIAL profile documents;
- the [package profile](../../../packaging/windows/package.yml);
- the executable verification authority: `tools/agent/verify.ps1`,
  `verification-registry.json` and its schemas. The archived §12.8 of
  `verification-levels.md` is history only;
- the mini-track plan and the roadmap, then the subphase closeout records;
- the author decisions on the reconciliation;
- the readiness package and the reconciliation as prepared evidence.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

1. **Help** **[DQ-G-7]**:
   - EN/ES user-guide coverage of every accepted R6-plus capability, following the
     reconciliation's coverage matrix (§6) and the guide-structure rules of the
     documentation contract §10;
   - corrections of existing in-product entry points (profile texts, tooltips, Input Help,
     the guide-window route) where the matrix shows a gap;
   - no new help framework or window type.
2. **About** **[DQ-G-2]**, **[DQ-G-3]**: implement the product metadata contract as
   approved (candidate PM-1 to PM-12), through:
   - the existing `geocedg.help.about` action;
   - the live application-profile texts;
   - `package.yml` `application.version_date`;
   - the generated provenance resource;
   - the builder-written distribution record.
3. **Version** **[DQ-G-4]**: the visible version stays `1.0.0` in every commit except
   the single version-transition commit. That commit changes `application.version` to
   `1.1.0` and `application.version_date` to the author-designated date `D`, together with
   the guides and the version tests. It is the last productive commit before the freeze.
   No other version, tag or release.
4. **Characterization** **[DQ-G-5]**, only if the authorization grants it: the bounded
   scenarios of reconciliation §5 for
   `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`, with
   `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` alongside. They run as scratch probes or
   focused tests. No correction.
5. **Integrated verification**: the `PRE-G9B-R6-PLUS-G` phase selection, the JUnit
   inventory and the registry pins through the official mechanisms, then the frozen
   acceptance plan.
6. **Debt register** **[DQ-G-6]**: the structured R6-plus register, recording the
   dispositions the author decides on the C proposal.
7. **Status reconciliation** **[DQ-G-8]**, **[DQ-G-9]**: the documentary corrections
   approved from reconciliation §7, each traced to its superseding record.
   - Historical closeout records are never rewritten.
   - No specification, ADR or scientific-contract content changes beyond approved
     header, status or count lines.
8. **Closeout**:
   - the global closeout record;
   - roadmap and mini-track status;
   - the candidate report and mirror;
   - the `docs/upstream/modified-files.yml` entries;
   - the normative transcription of the approved metadata contract (candidate §9).

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- New product features.
- Geometry, kernel, DAG, units or serialization semantics.
- Any inherited source change not separately authorized.
- Classic's About, menu, title and behaviour: `GeoGebraMenuBar`, `HelpMenuD`,
  `MacApplicationListener` stay unmodified.
- Closing, waiving or implementing a debt without its author disposition.
- Correcting an open observation beyond characterization, unless the author authorizes
  it **[DQ-G-5]**.
- Changing the visible version outside the version-transition commit.
- Encoding approval state in product bytes.
- Deriving a distribution condition from a file name, an installation path, an
  environment heuristic, a UI label or an artifact name.
- Showing `COMMERCIAL` as an authorized condition.
- Changing global verification infrastructure without an escalation:
  - `verify.ps1`, the shared verification modules, the registry, inventory or receipt
    schemas, an output adapter, the receipt or closeout flow;
  - an `infra.*` check;
  - the semantics of an existing leaf beyond a phase-local contract addition.
- Public release, binary publication, a tag, redistribution, `PROFILE COMMERCIAL`.
- `PRE-G9B-R7` and `G9B`.

## Architectural placement

GeoCeDG Desktop presentation, build provenance, packaging and documentation.

- The About content comes from the authorities fixed by the metadata contract:
  - `package.yml` for identity, version and version date;
  - the generated provenance for the build identity;
  - the builder-written distribution record, verified against the JAR, for the condition;
  - the licensing documents, summarized and never copied.
- `desktop.jar` stays profile-neutral.

## Required design/specification

The product metadata contract as approved by the author **[DQ-G-2]**, **[DQ-G-3]**,
**[DQ-G-4]**. Without that approval, stop after characterization.

Its normative transcription (candidate §9) is part of G and requires no further design.

## Geometric invariants and degeneracies

Not applicable: no geometric change.

## Compatibility and serialization

No document serialization change.

The version transition reaches:
- the window title and the guide window;
- the build provenance (schema `3`, internal to `desktop.jar`);
- the package profile (`packaging/windows/package.yml`, a pinned static-contract input,
  re-pinned through the official mechanism);
- the packaged metadata (`--app-version`, artifact names, `build-manifest.json`, the
  distribution record);
- the user guides.

The DXF fidelity sidecar keeps its schema. The installers keep `upgrade_uuid`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

**Focused tests.** The new selection `junit.desktop.pre-g9b-r6-plus-g` asserts:
- the single authority for name, version and version date;
- the provenance schema `3`;
- build-identity rendering;
- the four distribution states, from fixture records: `COMMERCIAL` and `not-authorized`
  records give `UNVERIFIED`; binding mismatches give `UNVERIFIED`;
- About content and order in EN and ES;
- licensing consistency against the authority files;
- Classic About unchanged;
- guide-coverage assertions at structure level for the newly documented capabilities.

**Checks:** Checkstyle, the upstream boundary, `git diff --check`, JSON validation of
mirrors, and `Test-PromptContractDocument` for every changed prompt.

**Official acceptance** on the clean committed frozen candidate `T`, with no inherited
evidence **[DQ-G-1]**:

| # | Check | Status |
|---|---|---|
| 1 | `STATIC` | mandatory |
| 2 | `PHASE -Phase PRE-G9B-R6-PLUS-G` | mandatory; selection below |
| 3 | `INTEGRATION` | mandatory |
| 4 | `INFRA_UNIT` | mandatory |
| 5–7 | `PACKAGING -CheckToolchain`; `PACKAGING -VerifyPackagingArtifacts` on an INTERNAL set and on an NC set, each built from `T` | mandatory when the version, the version date or the distribution record reaches the package, which is the expected case |
| 8 | the `COMMERCIAL` refusal recipe (output root unchanged) | mandatory with 5–7 |

Proposed selection `PRE-G9B-R6-PLUS-G`:
- `compile.desktop.semantic`;
- `workspace-profile.product`;
- `junit.desktop.pre-g9b-r6-plus-g.semantic`;
- plus `packaging.product` and `infra.contract-boundary` when the builder or the
  packaging check changes.

Further rules:
- **Inventory.** Refresh the inventory with executed `final.shared` and `final.desktop`
  selection evidence before freezing.
- **`FINAL`.** Only through a `VERIFICATION_ESCALATION_REQUEST`, in two cases:
  - a global verification-infrastructure change;
  - the author elects one receipt-bound milestone campaign. `FINAL` then replaces 3+4,
    with identical leaf coverage.
- **Failures.** Apply the task-template causality rule to any failure: retained baseline
  debt is not phase scope. Never repeat a heavy campaign on an unchanged candidate without
  author authorization.

**Author smoke:**
- help entry points for the R6-plus capabilities;
- About in EN and ES on a development run, an INTERNAL app-image and an NC app-image or
  ZIP built from `T`;
- Classic About.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must:
- name the exact base commit and tree;
- freeze the class and its acceptance plan;
- decide `DQ-G-1` to `DQ-G-4` and `DQ-G-7`;
- state whether the `DQ-G-5` characterization is authorized.

`D` must be designated before the candidate is frozen. `DQ-G-5`, `DQ-G-6`, `DQ-G-8` and
`DQ-G-9` must be decided before the global closeout.

Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

No push, merge, tag, release or binary. Publication happens only through a separate
author instruction naming the exact accepted commit. A public release or redistribution
is a further, separate decision. Neither `1.1.0` nor its date authorizes one.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review.
- The candidate report records invariant facts only
  (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`).
- `1.1.0` and `D` become authoritative only by the author's approval of the exact `T`.
- The documentary closeout descends from `T` and changes no product, build, packaging,
  test or verification path.
- The global `PRE-G9B-R6-plus` closeout is recorded only by the author's approval of G.

## Required artifacts

- the help coverage matrix as implemented;
- the normative product metadata documents and their evidence;
- the structured debt register with the author's dispositions;
- the `DQ-G-5` characterization report, if authorized;
- the candidate report and mirror;
- the PHASE, INTEGRATION, INFRA_UNIT, STATIC and PACKAGING evidence, with exact commands,
  exit codes and log paths;
- the author-smoke checklist;
- roadmap and mini-track status;
- the `GUIDE_IMPACT`, bootstrap-impact and infrastructure-impact records.

## Stop conditions

- A hard predecessor is missing from the base, or the base, worktree or remote diverges.
- `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` or
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` has no author disposition when the global
  closeout is reached. The latter's requirement is set by the C prompt closeout record.
- A kernel, serialization or inherited change appears necessary.
- A global verification-infrastructure change is found without an authorized escalation.
- A licensing fact for the About surface cannot be sourced from an existing authority.
- Freeze or post-freeze conditions on the version:
  - the version date `D` is not designated when the candidate is to be frozen;
  - a version other than `1.1.0` is requested;
  - any product, build, packaging, test or verification path changes after `T`.
- A distribution condition would be derived from anything other than the verified record.
- A `COMMERCIAL` record, artifact or authorized label appears.
- Coverage is incomplete or untrusted.
- An author decision is missing.

# PRE-G9B-R6-plus-G — help, product metadata, integrated verification and R6-plus closeout

**G CANONICAL PROMPT — DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED.**

The author approved the G design on 2026-10-14
([author design closeout record](../../../docs/validation/pre_g9b_r6_plus_g_author_design_closeout_record.md)).
Execution requires a separate G implementation authorization naming an approved and
published base.

| Revision | Origin | Base |
|---|---|---|
| 1 | G readiness operation of the author instruction "GeoCeDG — F4-A PUBLICATION + F4 AGGREGATE CLOSEOUT + G READINESS" (2026-10-14) | `abe12893`; kept as history |
| 2 | author instruction "GeoCeDG — PRE-G9B-R6-plus-G AUTHOR DECISION RECONCILIATION" (2026-10-14) | `ccb4ef34`; kept as history |
| 3 | author instruction "GeoCeDG — PRE-G9B-R6-plus-G FINAL AUTHOR DESIGN RECONCILIATION" (2026-10-14): author design directions incorporated | `caf740a1` |
| 3 (design approved) | author instruction "GeoCeDG — PRE-G9B-R6-plus-G FINAL AUTHOR DESIGN APPROVAL AND DOCUMENTARY CLOSEOUT" (2026-10-14): design approved, prompt status updated, approved metadata contract referenced | the design closeout commit |

Inputs:
- the [author design closeout record](../../../docs/validation/pre_g9b_r6_plus_g_author_design_closeout_record.md):
  - the approved decisions `DQ-G-1` to `DQ-G-10`;
  - the debt dispositions recorded by the author;
  - the conditions for the implementation authorization;
- the [product metadata contract](../../../docs/architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md),
  **AUTHOR APPROVED — DESIGN** (PM-1 to PM-12);
- the [author design decision record](../../../docs/validation/pre_g9b_r6_plus_g_author_design_decision_record.md):
  the design directions;
- the [author decision reconciliation](../../../docs/architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md),
  revision 2: characterization plan, help matrix and documentation plan;
- the [debt and observation disposition proposal](../../../docs/validation/pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md);
- the [readiness package](../../../docs/architecture/pre_g9b_r6_plus_g_readiness_package.md).

The G design decisions are approved and are not repeated as open choices here. Nothing in
this file authorizes implementation, verification campaigns, publication, release or
`PRE-G9B-R7`.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. Close the `PRE-G9B-R6-plus` mini-track as planned
([mini-track plan](../../../docs/architecture/pre_g9b_r6_plus_minitrack_plan.md) §3.2,
§12) with:

1. bilingual guide and in-product help for every accepted R6-plus capability;
2. the GeoCeDG-owned About surface required by `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`,
   with GeoCeDG `1.1.0` and the author-designated version date `2026-10-15`;
3. the bounded characterization of the two document-replacement observations;
4. the integrated verification of the accepted subphases on G's own frozen candidate;
5. the structured debt and observation register with the author's dispositions;
6. the global R6-plus closeout record, pending author approval.

`VERIFICATION_CLASS = INTEGRATED_PHASE` (approved for `DQ-G-1`). It is frozen explicitly
by the G implementation authorization. `CLOSEOUT_MODE = AUTHOR_OPERATED` (approved).

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the G implementation authorization. The base must be
explicitly named, approved and **published**. The proposed base is the G author design
closeout commit, once the author approves and publishes it. Until then the only
published base is `main` `abe12893b74f574c248117fbe873633a449e23e0` (tree
`ba4298c5658bf3f897455db5a49a76fea7105e72`; `DQ-G-10 = PUBLICATION COMPLETED — EXACT BASE
ESTABLISHED`). That base lacks the approved design documents.

A local documentary commit is never the base.

Entry gate:
- exact identity and a clean worktree;
- local, tracking and live remote `main` agree;
- every hard predecessor of plan §3.1 is `PASS — AUTHOR APPROVED` and is an ancestor of
  the base.

The prerequisite matrix of the reconciliation (§1.3) is re-checked on the base. F4 is an
operational predecessor, not a hard dependency. A missing predecessor or a contradiction
stops the phase.

## Authority and evidence hierarchy

1. `AGENTS.md`; current code and build.
2. The approved specifications, in particular:
   - the [Windows packaging contract](../../../geocedg/specs/packaging/windows-packaging.md);
   - the [application profile](../../../geocedg/specs/ui/application-profile.md);
   - the [documentation-maintenance contract](../../../geocedg/specs/operations/documentation-maintenance.md).
3. The licensing authorities: `LICENSE`, `NOTICE.md`, `THIRD_PARTY.md`, `docs/LICENSE.md`,
   the [component matrix](../../../docs/licensing/component-matrix.md), and the NC and
   COMMERCIAL profile documents.
4. The [package profile](../../../packaging/windows/package.yml).
5. The executable verification authority: `tools/agent/verify.ps1`,
   `verification-registry.json` and its schemas. The archived §12.8 of
   `verification-levels.md` is history only.
6. The mini-track plan and the roadmap; the subphase closeout records.
7. The author design closeout record; the approved product metadata contract (design
   authority until its normative transcription into the specifications of item 2); the
   author design decision record.
8. The reconciliation, the debt proposal and the readiness package,
   as prepared evidence.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

1. **Help** (approved `DQ-G-7`).
   - The minimal EN/ES help package of reconciliation §6.2: PDF export with text as
     shapes or with embedded Type 1 fonts; physical PDF stroke widths; the curated GeoCeDG
     tool library and its limitations; the numeric thickness editor; the accepted
     user-visible limitations; product metadata and About.
   - The optional editorial items the authorization selects.
   - Paragraphs only, respecting the guide-outline pin (`PreG9BR5AGuideOutlineTest`) and the
     bilingual structure contract (documentation contract §10).
   - Small editorial terminology and cross-reference corrections that change no guide
     structure, normative meaning or product behaviour.
   - No new help framework or UI window. No modification of Classic-shared help
     implementations or properties.
2. **About and product metadata** (approved `DQ-G-2`, `DQ-G-3`, PM-1 to PM-12).
   Implement the **AUTHOR APPROVED — DESIGN** metadata contract:
   - `package.yml` stays the single authority for name and version, with the new required
     `application.version_date`;
   - build identity from repository provenance; no wall-clock timestamp;
   - the installed record `app/geocedg-distribution.json`, derived from the selected
     package profile, with the four states INTERNAL, NC, DEVELOPMENT and UNVERIFIED;
   - `package_profile_sha256` **runtime-checked**: the documented deterministic
     canonicalization (UTF-8, `CRLF`/`CR` → `LF`, SHA-256), and a real comparison with the
     digest embedded in the build provenance;
   - consistency checks only: no cryptographic authenticity, signed metadata, online
     validation, DRM or licensing authority;
   - a profile-neutral `desktop.jar`;
   - the About inside the existing `geocedg.help.about` dialog: a composite-product
     statement distinct from component permissions, and never "licensed solely under
     EUPL-1.2".
3. **Version** (approved `DQ-G-4`, M1).
   - Every commit before the version-transition commit shows `1.0.0` with no version-date
     field.
   - One atomic version-transition commit, the last productive commit, sets:
     - `application.version` `1.1.0` and `application.version_date` `2026-10-15`;
     - the package-profile `schema_version` `2`, the Gradle guard and the provenance
       schema `3`;
     - the About date line;
     - the guide version strings and the version tests.
   - No version date for `1.0.0`. No other version, tag or release.
4. **Characterization** (approved `DQ-G-5`).
   - Execute the 27 scenarios of reconciliation §5.3, SC-01 to SC-27, as scratch probes
     or untracked focused tests. They cover
     `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and
     `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`.
   - Keep document replacement distinct from insertion, merging, tool replacement and
     other session transitions.
   - Report the evidence for the author's disposition. No productive correction.
5. **Integrated verification.**
   - Register the `PRE-G9B-R6-PLUS-G` phase selection, the JUnit inventory and the
     registry pins through the official mechanisms.
   - Refresh executed `final.shared` and `final.desktop` selection evidence before
     freezing.
   - Then run the frozen acceptance plan.
6. **Debt register** (approved `DQ-G-6` framework and planning owners).
   - The structured R6-plus register on the 134-entry framework, with the author's
     planning owners:
     - `TD-PDF-1` → G15, after a physical-presentation contract-design activity;
     - direct working-layer entry → G11;
     - manual `ExportArea` interaction → G15;
     - physical zoom aid → G12;
     - `DEFER-F3-*` → a future author-named insertion and styles activity;
     - full-suite instability → G monitoring and `PRE-G9B-R7` follow-up.
   - Record the author dispositions of the design closeout §4.1 (the E2 naming collision,
     the E3 `ExportArea` identity validity and unit gate, the five F4 risks) with their
     evidence.
   - Keep `OBS-R6P-SAVE-WARNING` and the independent observations open.
   - Every other pending item keeps an explicit author-disposition requirement.
7. **Documentation** (`DQ-G-8`, `DQ-G-9`; reconciliation §7.3).
   - Apply the class-1 corrections, each traced to its superseding record. They include
     the published status of F4 and the earlier subphases.
   - Clarify the track-level "NO SUBPHASE AUTHORIZED" as absence of blanket
     authorization.
   - Record the remaining class-2 items with their narrowest truthful status until the
     author disposes of them.
   - Classes 3 and 4 are retained unless separately authorized: verifier-pinned headers,
     the ADR 0035 body wording, the undecided C/E3 normative amendments.
8. **Closeout.**
   - The global closeout record; roadmap and mini-track status.
   - The candidate report and mirror; `docs/upstream/modified-files.yml` entries.
   - The normative transcription of PM-1 to PM-12 into the existing approved
     `windows-packaging.md` and `application-profile.md`, and the package-profile schema
     (contract §9). The contract stays the historical design authority; no duplicate
     normative specification is created.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- New product features.
- Geometry, kernel, DAG, units or serialization semantics.
- Any inherited source change not separately authorized.
- Classic's About, menu, title and behaviour: `GeoGebraMenuBar`, `HelpMenuD` and
  `MacApplicationListener` stay unmodified.
- Any correction of either document-replacement observation, or of any other open
  observation, without a separate author authorization.
- Closing, waiving or re-categorizing a debt without its author disposition; promoting
  P or O to R or L.
- Version changes:
  - a visible-version change outside the version-transition commit;
  - a version or date other than `1.1.0` / `2026-10-15`;
  - a version date for `1.0.0`;
  - productive changes after the candidate freeze;
  - approval state encoded in product bytes.
- Distribution condition:
  - deriving it from a file name, an installation path, an environment heuristic, a UI
    label or an artifact name;
  - showing `COMMERCIAL` as authorized;
  - signed metadata, online validation or DRM.
- Documentation:
  - class 3 or class 4 changes under documentation maintenance (normative amendment
    promotion, ADR bodies, verifier-pinned files, historical byte pins, old verification
    scripts);
  - editing closeout records or accepted evidence.
- Global verification infrastructure, without an escalation:
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
  - the generated provenance for build identity;
  - the builder-written distribution record, consistency-checked against the JAR, for the
    distribution condition;
  - the licensing documents, summarized and never copied.
- The characterization lives in scratch probes, never in product code.

## Required design/specification

The product metadata contract, **AUTHOR APPROVED — DESIGN** on 2026-10-14 (PM-1 to PM-12,
revision 2 with the design-closeout clarifications). It is the design authority for G.
Transcribing it into the existing approved Windows packaging and application-profile
specifications (contract §9) is part of G and requires no further design. Any deviation
from the approved design stops the phase for an author decision.

## Geometric invariants and degeneracies

Not applicable: no geometric change.

## Compatibility and serialization

No document serialization change.

The version transition reaches:
- the window title and the guide window;
- the build provenance (schema `3`, internal to `desktop.jar`);
- the package profile (`packaging/windows/package.yml` and its schema `2`, pinned
  static-contract inputs re-pinned through the official mechanism);
- the packaged metadata (`--app-version`, artifact names, `build-manifest.json`, the
  distribution record);
- the user guides.

The DXF fidelity sidecar keeps its schema. The installers keep `upgrade_uuid`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

**Focused tests** (`junit.desktop.pre-g9b-r6-plus-g`), as listed in contract PM-11:
- the single authority, with `1.1.0` / `2026-10-15`;
- the schemas: package profile `2`, provenance `3`;
- build-identity rendering;
- the four distribution states from fixture records, including digest and binding
  mismatches, and `COMMERCIAL` and `not-authorized` records giving `UNVERIFIED`;
- About content and order in EN and ES; licensing consistency;
- Classic About unchanged;
- guide-coverage assertions at structure level.

**Checks:** Checkstyle, the upstream boundary, `git diff --check`, JSON validation of
mirrors, and `Test-PromptContractDocument` for every changed prompt.

**Official acceptance** on the clean committed frozen candidate `T`, with no inherited
evidence. Only the current executable verifier, registry and schemas are acceptance
authorities.

| # | Check | Status |
|---|---|---|
| 1 | `STATIC` | mandatory |
| 2 | `PHASE -Phase PRE-G9B-R6-PLUS-G` | mandatory; selection below |
| 3 | `INTEGRATION` | mandatory |
| 4 | `INFRA_UNIT` | mandatory |
| 5 | `PACKAGING -CheckToolchain` | mandatory |
| 6 | `PACKAGING -VerifyPackagingArtifacts` on an INTERNAL set built from `T` | mandatory |
| 7 | `PACKAGING -VerifyPackagingArtifacts` on an NC set built from `T` | mandatory |
| 8 | `COMMERCIAL` refusal evidence: `-DistributionProfile COMMERCIAL` refuses and the output root is unchanged | mandatory |

Selection `PRE-G9B-R6-PLUS-G`:
- `compile.desktop.semantic`;
- `workspace-profile.product`;
- `junit.desktop.pre-g9b-r6-plus-g.semantic`;
- `packaging.product` and `infra.contract-boundary`, because the builder and the packaging
  check change.

In the live registry INTEGRATION ∪ INFRA_UNIT equals the leaf set of `FINAL`. That
equivalent leaf coverage is **not** a single receipt-bound `FINAL` campaign.

`FINAL` only in two cases:
- through a `VERIFICATION_ESCALATION_REQUEST`, when a global verification-infrastructure
  change becomes necessary;
- by a separate author decision requesting a unified milestone receipt.

Apply the task-template causality rule to any failure: retained baseline debt is not phase
scope. Never repeat a heavy campaign on an unchanged candidate without author
authorization.

**Author smoke:**
- the help entry points;
- About in EN and ES on a development run, an INTERNAL app-image and an NC app-image or
  ZIP built from `T`;
- Classic About.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The G design is approved; implementation is **not** authorized. A separate G
implementation authorization must:
- authorize this prompt for implementation;
- name the exact base commit and tree, approved and published;
- freeze `INTEGRATED_PHASE` explicitly with the acceptance plan above, under
  `CLOSEOUT_MODE = AUTHOR_OPERATED`;
- state whether the `DQ-G-5` characterization runs in the same task;
- select the optional editorial help items, if any.

Before the global closeout the author must decide:
- the dispositions of both document-replacement observations;
- the remaining per-item debt dispositions (design closeout §4.3);
- the remaining class-2 dispositions.

Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

No push, merge, tag, release or binary. Publication happens only through a separate
author instruction naming the exact accepted commit. A public release or redistribution
is a further, separate decision; neither `1.1.0` nor `2026-10-15` authorizes one.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review.
- The candidate report records invariant facts only
  (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`).
- `1.1.0` and `2026-10-15` become authoritative only by the author's approval of the exact
  `T`. The closeout records the adopted version date separately from the actual approval
  date.
- The documentary closeout descends from `T` and changes no product, build, packaging,
  test or verification path.
- The global `PRE-G9B-R6-plus` closeout is recorded only by the author's approval of G.

## Required artifacts

- the help coverage matrix as implemented;
- the normative product metadata documents and their evidence;
- the characterization report for SC-01 to SC-27;
- the structured debt register with the author's dispositions;
- the candidate report and mirror;
- the PHASE, INTEGRATION, INFRA_UNIT, STATIC and PACKAGING evidence, with exact commands,
  exit codes and log paths, and the COMMERCIAL refusal evidence;
- the author-smoke checklist;
- roadmap and mini-track status;
- the `GUIDE_IMPACT`, bootstrap-impact and infrastructure-impact records.

## Stop conditions

- A hard predecessor is missing from the base, or the base, worktree or remote diverges.
- The base is not an explicitly named, author-approved and published commit.
- The implementation would deviate from the approved metadata design, for example:
  - a second version authority;
  - an unchecked digest presented as verified;
  - a profile-specific `desktop.jar`;
  - a duplicate normative specification.
- A characterized contract violation would require a code change: stop that corrective
  branch and request authorization.
- `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` or
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` has no author disposition when the global
  closeout is reached. The latter's requirement is set by the C prompt closeout record.
- A kernel, serialization or inherited change appears necessary.
- A global verification-infrastructure change is found without an authorized escalation.
- A licensing fact for the About surface cannot be sourced from an existing authority.
- Version:
  - a version or date other than `1.1.0` / `2026-10-15` would be needed;
  - a version date for `1.0.0` would be introduced;
  - any product, build, packaging, test or verification path changes after `T`.
- A distribution condition would be derived from anything other than the verified record.
- A `COMMERCIAL` record, artifact or authorized label appears.
- A class 3 or 4 documentary change would be needed.
- Coverage is incomplete or untrusted.
- An author decision is missing.

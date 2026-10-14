# PRE-G9B-R6-plus-G — help, product metadata, integrated verification and R6-plus closeout

**CANONICAL PROMPT CANDIDATE — PREPARED — PENDING AUTHOR REVIEW — NOT AUTHORIZED.**
Prepared by the G readiness operation of the author instruction "GeoCeDG — F4-A
PUBLICATION + F4 AGGREGATE CLOSEOUT + G READINESS" (2026-10-14). The readiness package is
[`pre_g9b_r6_plus_g_readiness_package.md`](../../../docs/architecture/pre_g9b_r6_plus_g_readiness_package.md),
which holds the prerequisite matrix, the open-debt matrix and decisions `DQ-G-1` to
`DQ-G-10`. Nothing here authorizes implementation, global verification, publication,
release or `PRE-G9B-R7`. Choices marked **[DQ-G-n]** are proposals pending the author.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. Close the `PRE-G9B-R6-plus` mini-track as planned
([mini-track plan](../../../docs/architecture/pre_g9b_r6_plus_minitrack_plan.md) §3.2, §12):

1. bilingual guide and in-product help for every accepted R6-plus capability;
2. the GeoCeDG-owned About surface required by `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`;
3. the integrated verification of the accepted subphases on G's own candidate;
4. the structured debt and observation register with author dispositions;
5. the global R6-plus closeout record, pending author approval.

Proposed `VERIFICATION_CLASS = INTEGRATED_PHASE` **[DQ-G-1]**, frozen only by the G
implementation authorization. Proposed acceptance on the clean committed candidate:
- `PHASE PRE-G9B-R6-PLUS-G` (a selection registered by G);
- `INTEGRATION`;
- `INFRA_UNIT` and `STATIC`, for the registration and for any re-pinned static-contract
  input;
- `PACKAGING` for the INTERNAL and NC artifact sets, when the version, the version date or
  the distribution marker reaches the package profile or the build.

`FINAL` only if a global verification-infrastructure change is found **[DQ-G-1]**.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the G implementation authorization. The recommendation
is the published successor that contains, in order:
- the F4 aggregate closeout `19f8f206b5ed7df8d203b3cebddd59eb1183d589`;
- the G readiness package.

Both are documentary and unpublished at the time of writing **[DQ-G-10]**.

Entry gate:
- exact identity;
- a clean worktree;
- local, tracking and live remote `main`;
- every hard predecessor of the plan (§3.1) at `PASS — AUTHOR APPROVED` and contained in
  the base;
- the readiness package's prerequisite matrix re-checked on the base.

A missing predecessor or a contradiction stops the phase.

## Authority and evidence hierarchy

`AGENTS.md`; current code and build; the
[documentation-maintenance contract](../../../geocedg/specs/operations/documentation-maintenance.md);
the licensing authorities (`LICENSE`, `NOTICE.md`, `THIRD_PARTY.md`, `docs/LICENSE.md`,
[component matrix](../../../docs/licensing/component-matrix.md), the NC and COMMERCIAL
profile documents); the [package profile](../../../packaging/windows/package.yml); the
mini-track plan and the roadmap; the subphase closeout records; the readiness package.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

1. **Help.**
   - EN/ES user-guide coverage of every accepted R6-plus capability, with a coverage
     matrix; the readiness package lists the gaps found so far;
   - in-product help entries (profile texts, Input Help, the guide window) only where a
     capability lacks them **[DQ-G-7]**;
   - the guide-structure rules of the documentation contract.
2. **About.** A GeoCeDG-owned About surface (`GeoCeDGActionRegistry` About action, profile
   texts, a GeoCeDG product-metadata source **[DQ-G-2]**, **[DQ-G-3]**) showing:
   - the product name;
   - the canonical version;
   - a canonical version date distinct from the build date;
   - EUPL-1.2 for GeoCeDG software;
   - CC BY 4.0 for documentation and ordinary artwork;
   - inherited components under their own terms;
   - the INTERNAL, NC or COMMERCIAL distribution condition of the running build.
3. **Version.** The visible version stays `1.0.0` until the G completion contract applies
   `1.1.0` and its version date **[DQ-G-4]**. No other version, tag or release.
4. **Integrated verification.** Phase selection, inventory and pins through the official
   mechanisms, then the frozen acceptance campaign.
5. **Debt register.** The structured R6-plus debt and observation register, with the
   dispositions the author decides. That includes the mandatory disposition of
   `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` **[DQ-G-5]** and owners for the
   deferred items **[DQ-G-6]**.
6. **Status reconciliation.** Documentary reconciliation of stale status labels found by
   the readiness package **[DQ-G-8]** and of the registered documentary observations
   **[DQ-G-9]**.
7. **Closeout.** The global closeout record, roadmap and mini-track status, the candidate
   report and mirror, and `docs/upstream/modified-files.yml` entries.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- New product features.
- Geometry, kernel, DAG, units or serialization semantics.
- Any inherited source change not separately authorized.
- Classic's About and Classic behaviour.
- Closing, waiving or implementing a debt without its author disposition; corrections of
  open observations, beyond characterization, unless the author authorizes them
  **[DQ-G-5]**.
- Changing the visible version before the G completion contract.
- Public release, binary publication, a tag, redistribution, `PROFILE COMMERCIAL`.
- `PRE-G9B-R7` and `G9B`.

## Architectural placement

GeoCeDG Desktop presentation and documentation. The About content comes from a GeoCeDG
product-metadata authority, never from a second copy of licensing truth. Licensing facts
are referenced from the licensing documents and the package profile.

## Required design/specification

An About/product-metadata contract approved by the author before implementation
**[DQ-G-2]**, **[DQ-G-3]**. Without it, stop after the characterization.

## Geometric invariants and degeneracies

Not applicable: no geometric change.

## Compatibility and serialization

No document serialization change. A version change reaches:
- the window title;
- the build provenance (`application.version`);
- the package profile (`packaging/windows/package.yml`, a pinned static-contract input);
- the packaged metadata.

Each is reconciled through the official pins and the `PACKAGING` evidence.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- **Focused tests:**
  - About content and order in EN and ES;
  - version and version-date source, with the date distinct from the build date;
  - distribution condition per profile (INTERNAL, NC, COMMERCIAL marker never shown as
    authorized);
  - Classic About unchanged;
  - guide structure and coverage.
- **Checks:** Checkstyle, the upstream boundary, the guide-structure diagnostic.
- **Official acceptance** on the frozen candidate as frozen by the authorization
  **[DQ-G-1]**. The executable authority is the active recovery contract of
  `verification-levels.md` with `verify.ps1` and `verification-registry.json`; its
  archived §12.8 is design history only.
- **Packaging:** `PACKAGING` for the INTERNAL and NC sets when packaging inputs change.
- **Author smoke:**
  - About in EN and ES in an INTERNAL and an NC build;
  - Classic About;
  - the help entry points for the R6-plus capabilities.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must:
- name the exact base commit and tree;
- freeze the class and its acceptance profiles;
- decide `DQ-G-1` to `DQ-G-10`.

Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

No push, merge, tag, release or binary. Publication only through a separate author
closeout naming the exact accepted commit. A public release or redistribution is a
further, separate decision.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review. The candidate report
records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`). The
global `PRE-G9B-R6-plus` closeout is recorded only by the author's approval of G.

## Required artifacts

- the help coverage matrix;
- the About contract and evidence;
- the structured debt register;
- the candidate report and mirror;
- the PHASE, INTEGRATION, INFRA_UNIT, STATIC (and PACKAGING) receipts;
- the author-smoke checklist;
- roadmap and mini-track status.

## Stop conditions

- A hard predecessor is missing from the base, or the prerequisite matrix fails.
- `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` has no author disposition when
  the global closeout is reached.
- An inherited, kernel or serialization change appears necessary.
- A global verification-infrastructure change is found under a non-`FINAL` class.
- A licensing fact for the About surface cannot be sourced from an existing authority.
- The base identity, worktree or remote diverges; coverage is incomplete or untrusted; or
  an author decision is missing.

# PRE-G9B-R6-plus-G — author decision reconciliation

| Field | Value |
|---|---|
| Status | **G AUTHOR DECISION RECONCILIATION — REVISION 2 — AUTHOR DESIGN DIRECTIONS RECONCILED — PENDING FINAL AUTHOR REVIEW** (documentary). The directions the author gave on 2026-10-14 are recorded in the [author design decision record](../validation/pre_g9b_r6_plus_g_author_design_decision_record.md). Every other recommendation below remains a proposal. |
| Revisions | 1: `ccb4ef34dde4736030cc1a600cd7b84624f9ab56` (tree `04882e1d042282442cc6977efa2ea07398b0340a`). 2: this revision, under "GeoCeDG — PRE-G9B-R6-plus-G FINAL AUTHOR DESIGN RECONCILIATION" (2026-10-14). |
| Author instruction | "GeoCeDG — PRE-G9B-R6-plus-G AUTHOR DECISION RECONCILIATION" (2026-10-14): architectural, contractual and verification-design reconciliation of the prepared G phase; documentation and review only |
| Base | published `main` `abe12893b74f574c248117fbe873633a449e23e0`, tree `ba4298c5658bf3f897455db5a49a76fea7105e72` (§1) |
| Package | this record (A, D summary, E); [product metadata contract candidate](pre_g9b_r6_plus_g_product_metadata_contract_candidate.md) (B); [debt and observation disposition proposal](../validation/pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md) (C); [canonical G prompt, reconciled candidate](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md) (D); [JSON mirror](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-author-decision-reconciliation.json) |
| Supersedes | nothing. The [readiness package](pre_g9b_r6_plus_g_readiness_package.md) stays as prepared at `abe12893`. This record reconciles its proposals and records where they change. |
| Boundaries | G implementation, the G technical candidate, G verification campaigns, G approval, the global `PRE-G9B-R6-plus` closeout, `PRE-G9B-R7`, `G9B`, version change, public release, push, merge and tag: **NOT AUTHORIZED**; `selfApproved = false` |

## 0. Revision 2 — author design directions

On 2026-10-14 the author resolved the remaining architectural questions of this design
and authorized its documentary reconciliation. **G implementation is not authorized.**
The directions, transcribed in the
[author design decision record](../validation/pre_g9b_r6_plus_g_author_design_decision_record.md),
are:

- **Version.** Target `1.1.0`; canonical version date `2026-10-15`, author-designated.
  `1.0.0` keeps no version date, and the `2026-09-16` proposal is withdrawn.
- **Metadata.** `DQ-G-2` alternative A, `DQ-G-3` PM-5, and PM-6 to PM-12 are accepted, with
  the clarifications reconciled in contract revision 2:
  - the semantics of the version date;
  - the atomic schema migration;
  - the role of `package_profile_sha256`;
  - consistency is not authenticity.
- **Lifecycle.** `DQ-G-4` M1 is adopted.
- **Verification.** `DQ-G-1` is adopted as proposed.
- **Characterization.** The `DQ-G-5` characterization (27 scenarios) is in the planned G
  scope. No correction is authorized.
- **Debt register.** `DQ-G-6`: the framework and the planning owners are adopted, but no
  per-item disposition.
- **Help.** `DQ-G-7`: the minimal help package.
- **Documentation.** `DQ-G-8` and `DQ-G-9`: design preparation of minimal corrections in
  four classes (§7.3).
- **Publication.** `DQ-G-10` is recorded.

Where the "Disposition requested" field of a revision-1 decision below conflicts with a
direction, the direction prevails. The summary table shows the current state of each
decision. The decisions that remain open are listed in §9 and in the design decision
record §2.

## Executive assessment

The G scope is internally consistent and its preconditions are known. Every hard
predecessor is author-approved and contained in the published base (§1.3).

Three proposals of the readiness package change after source characterization:

- **`DQ-G-2` — version date.** Extend the existing package-profile authority
  (`application.version_date`) instead of adding a product-metadata resource. The
  approved packaging contract already makes `application.version` the *single*
  product-version authority (`windows-packaging.md:165-167`). A second resource would
  duplicate it or move it.
- **`DQ-G-3` — distribution condition.** `desktop.jar` is profile-neutral, and one JAR
  serves both INTERNAL and NC. The condition therefore cannot live in the build
  provenance. It needs an install-level distribution record, written by the builder and
  verified at runtime against the JAR's own provenance. Development builds get a
  distinct, conservative `DEVELOPMENT` condition instead of impersonating `INTERNAL`.
- **`DQ-G-4` — version lifecycle.** Any approve-the-exact-SHA model must contain `1.1.0`
  in the candidate before approval. The requirement "visible `1.0.0` until approval" is
  therefore reconcilable only as "every approved or published state shows `1.0.0` until
  G is approved". A post-approval version commit does not avoid the tension; it doubles
  the campaign. The recommended mechanism approves what was verified, with an
  author-designated version date fixed before the freeze.

`DQ-G-1` is confirmed with a precise acceptance plan: `INTEGRATED_PHASE` = PHASE +
INTEGRATION + INFRA_UNIT + STATIC + PACKAGING (INTERNAL and NC). In the live registry,
INTEGRATION ∪ INFRA_UNIT is exactly the leaf set of `FINAL`. `FINAL` would therefore add
a single-campaign receipt, not coverage. It is required only for a global
verification-infrastructure change, or by author choice.

`DQ-G-10` is a completed historical decision:
`DQ-G-10 = PUBLICATION COMPLETED — EXACT BASE ESTABLISHED` (§1.2).

No debt or observation blocks G entry. Three items gate the global closeout:

- `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (`DQ-G-5`);
- `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`. The author-approved C prompt closeout records
  "MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT"; the readiness package
  had classified it as pending only;
- the mandatory About requirement.

Status-label drift (R-1, R-2, R-7 to R-10) is documentary and is reconciled in §7. Five
new findings (R-11 to R-15) are added there. R-11 follows from the publication of
`abe12893`.

## 1. Repository identity and status

### 1.1 Git identities (verified 2026-10-14)

| Identity | Value |
|---|---|
| local `main` | `abe12893b74f574c248117fbe873633a449e23e0` |
| live remote `main` (`git ls-remote origin refs/heads/main`) | `abe12893b74f574c248117fbe873633a449e23e0` |
| `origin/main` (tracking) | `abe12893b74f574c248117fbe873633a449e23e0` |
| tree | `ba4298c5658bf3f897455db5a49a76fea7105e72` |
| worktree | clean before this task |
| history | linear: `07424dee` (F4-A closeout) → `19f8f206` (F4 aggregate closeout) → `abe12893` (G readiness) |
| paths changed since the last productive commit `6baa66a5` | documentation and validation mirrors only (10 paths: prompt, readiness package, plan, roadmap, three F4 records, three JSON mirrors) |
| documentary branch of this task | `docs/pre-g9b-r6-plus-g-author-decision-reconciliation`, created at `abe12893`; local only |

No divergence was found.

### 1.2 `DQ-G-10` — publication and implementation base (resolved historical decision)

The author resolved the publication before this task. Verification:
- `19f8f206` (F4 aggregate closeout) and `abe12893` (G readiness package) are on the live
  remote `main`;
- the remote tip is `abe12893`.

```text
DQ-G-10 = PUBLICATION COMPLETED — EXACT BASE ESTABLISHED
          published main abe12893b74f574c248117fbe873633a449e23e0
          tree ba4298c5658bf3f897455db5a49a76fea7105e72
```

Evidence:
- the author's statement in the instruction of this task;
- `git ls-remote` (§1.1);
- the local, git-ignored publication audit
  `artifacts/agent/g-readiness-publication-audit/publication-audit.json`, SHA-256
  `4d4cc197deb13a3b86a4a74248fa98b333f611dff89ed7180ba7076afe67e2fd`. It records
  operation "GeoCeDG — F4 AGGREGATE + G READINESS PUBLICATION" (2026-10-14), result
  `PASS`, the non-force push `07424dee6..abe12893b` with the administrator bypass of the
  `main` rules reported, and no tag. The file is evidence, not authority.

No tracked record of this publication existed before this task. These records
described `19f8f206` as unpublished (finding R-11, §7):
- the F4 aggregate closeout (`F4 AGGREGATE PUBLICATION = PENDING SEPARATE AUTHORIZATION`).
  It is a closeout record and is not rewritten.
- the readiness package and its mirror, which keep their prepared text;
- the roadmap row "Última fase cerrada" and the plan §14 F4 and G blocks. This task
  completes these additively.

This record registers the publication fact. The roadmap and mini-track entries added by
this task cite it.

Recommended future G implementation base: **`abe12893`** (tree `ba4298c5`).
- The local documentary commit(s) of this task do **not** become the base automatically.
- If the author approves and publishes them, the base becomes that published descendant.
  The G implementation authorization must name it by exact commit and tree.

### 1.3 Current status and predecessor matrix (rechecked on `abe12893`)

```text
F4 AGGREGATE                       = COMPLETE — AUTHOR APPROVED — PUBLISHED
                                     (19f8f206, contained in published main abe12893)
G READINESS                        = PREPARED — PUBLISHED — PENDING AUTHOR REVIEW
G CANONICAL PROMPT                 = CANDIDATE — NOT AUTHORIZED
G IMPLEMENTATION                   = NOT AUTHORIZED
PRE-G9B-R6-plus GLOBAL CLOSEOUT    = NOT AUTHORIZED
PRE-G9B-R7                         = NOT AUTHORIZED (DESIGNED; blocked until the global closeout)
G9B                                = NOT AUTHORIZED
```

Every approved implementation below is an ancestor of `abe12893`
(`git merge-base --is-ancestor`, 2026-10-14):

| Predecessor | Dependency class (plan §3.1) | Approved implementation (full SHA) |
|---|---|---|
| `A-1` | hard | `2a71133ad622e18e9963b04a466a92665a784722` |
| `A-2` | hard | `06a2ea6b4523a71f9f84fefca39e7d3b2b717e64` |
| `B` | hard | `848206c413eb4f78694c9149e153f6e867eb9525` |
| `C` (+ `C-X1`) | hard | `6248bf0fc4208540de09d26a7755d4163055ac76`; `eabe6368f9e012d8186aa668a68a5bcc48cdcc17` |
| `D1` (+ `D0`) | hard | `6e1d845902f534b9d45b533ac265089cbc01e689`; `8383a153dc2228fc08b4e00a0c92ef8025608916` |
| `E1` (`E1-L`, `E1-P`, `E1-X1`, `E1-P-X1`) | hard | `5f34d181b4f03412acfbdbeedb3fd7931de02ed9`; `dcda4075843766e8c384c176007c5edb776357c2`; `b12ab32c3a4dcf42941617e95d06a92bfb3b2955`; `44b90156786bce4ec5fca0466e7b8f81ea5bb358` |
| `E2` | hard | `3a7246614a6ef040d84a1440c4c10b3f93be5776` |
| `E3` (+ `E3-R1`; `POST-E3-INTEGRATION`) | hard | `7cd501167c856eef356888ca87ed3a0c97cea55c`; `00b525fda64968efdf973504a6426e8565931fff` |
| `F1` | hard | `1cea3c89e34d32ab6321a434e7bb873bc1a33f83` |
| `F2` (+ `F2-R1`) | hard | `488f1e991fee641b9ec521059edc318df7271bdd` |
| `F3` (+ `F3-R1`, `F3-R2`) | hard; recommended predecessor | `2d7b2cae23d5229f889877f22924b3669f98b755` |
| `F4` (aggregate) | **operational predecessor, not hard** | `F4-A` `6baa66a584ceef1973c854fb63b00aa7070708b4`; `F4-D` `6214d15a0bc986cf1d16f178c67fd76bf6e54bc2`; closeout `19f8f206b5ed7df8d203b3cebddd59eb1183d589` |

The three dependency classes stay separate (documentation-maintenance contract §3):

- **Hard** (plan §3.1, unchanged): `A-1, A-2, B, C, D1, E1, E2, E3, F1, F2, F3 ──► G`.
- **Recommended execution predecessor:** `F3`. F4 was inserted operationally between F3
  and G by the author planning decision of 2026-10-09. This record does not turn it into
  a hard geometric or contractual dependency.
- **Global closeout gate:** `G = PASS — AUTHOR APPROVED` is the `PRE-G9B-R6-plus` closeout
  that unblocks `PRE-G9B-R7`. Publication, tag, release and `PROFILE COMMERCIAL` stay
  separate decisions.

## 2. A — Author decision sheet

Each decision uses the same fields:
- question;
- existing contract and evidence;
- alternatives;
- recommendation;
- architectural consequences;
- verification consequences;
- remaining uncertainty;
- the disposition requested.

`DQ-G-10` is a **resolved historical decision**. `DQ-G-1` to `DQ-G-9` were new decisions at
revision 1. Their current state after the author directions of 2026-10-14 is:

### Summary

| Id | Choice | State (revision 2) | Still open |
|---|---|---|---|
| `DQ-G-1` | `INTEGRATED_PHASE`: PHASE `PRE-G9B-R6-PLUS-G` + INTEGRATION + INFRA_UNIT + STATIC + PACKAGING (INTERNAL, NC) + COMMERCIAL refusal evidence on the frozen candidate; `FINAL` only by escalation or by a separate author decision for a unified milestone receipt | **direction accepted** | freezing it, with `CLOSEOUT_MODE`, in the G implementation authorization |
| `DQ-G-2` | Alternative A: `application.version_date` in `package.yml`, the single authority | **direction accepted** | approval of the final contract revision |
| `DQ-G-3` | `app/geocedg-distribution.json`; four runtime states; conservative `DEVELOPMENT`/`UNVERIFIED`; `COMMERCIAL` unauthorized; digest runtime-checked (or audit-only); consistency is not authenticity | **direction accepted** | approval of the final contract revision (digest role) |
| `DQ-G-4` | M1; `1.1.0` / `2026-10-15`; no date for `1.0.0` | **direction accepted** | — |
| `DQ-G-5` | **A**: the 27 scenarios are in the planned G scope; no correction | **direction accepted** | disposition of both observations on the evidence, before the global closeout |
| `DQ-G-6` | the 134-entry framework; planning owners for `TD-PDF-1`, working-layer entry, manual `ExportArea`, zoom aid, `DEFER-F3-*` and instability | **framework and owners accepted** | per-item dispositions (B, P, the `PROPOSED` re-categorizations, the O owners) |
| `DQ-G-7` | the minimal EN/ES help package; no new framework or window | **direction accepted** | selection of the optional editorial items |
| `DQ-G-8` | the four-class documentation plan (§7.3) | **design preparation authorized** | the class 2, 3 and 4 decisions |
| `DQ-G-9` | per-observation correction, retention or escalation (§7.2, §7.3) | **design preparation authorized**; ADR 0035 retained as a discrepancy | the class 3 and 4 items |
| `DQ-G-10` | `PUBLICATION COMPLETED — EXACT BASE ESTABLISHED` (`abe12893`) | **resolved** | — |

### `DQ-G-1` — verification class and acceptance profiles

- **Question.** Which frozen class and which profiles accept G?
- **Existing contract.**
  - The executable authority is `tools/agent/verify.ps1` and
    `geocedg/specs/operations/verification-registry.json`. `verification-levels.md` is an
    **ACTIVE RECOVERY CONTRACT** whose live part is "Current contract" (`:8-63`).
  - Its §12.8 class table is archived design history (`:65-69`). Classes are therefore
    planning labels that map onto registry profiles, not registry fields.
  - Plan §12 (`:1179`) proposes `INTEGRATED_PHASE` with `INTEGRATION` on G's own
    candidate, inheriting no evidence, and `FINAL` "if a subphase changed global
    verification infrastructure".
- **Source evidence.**
  - Registry profiles (`verification-registry.json:9-19`): `STATIC`, `INFRA_UNIT`,
    `WORKSTATION`, `PACKAGING`, `OPERATIONAL` (incomplete), `DEV` (incomplete), `PHASE`,
    `INTEGRATION`, `FINAL`.
  - The packaging nodes (`packaging.product`, `packaging.toolchain-safety`,
    `packaging.repository-safety`) are required for `PACKAGING`, `INTEGRATION` and `FINAL`
    (`:185-255`). Without `-VerifyPackagingArtifacts`, however, `packaging.product`
    inspects only the profile, builder and legal bundle (`packaging-product.ps1:20-23,529`).
  - Packaged artifacts are verified one built profile at a time. Precedent: the E1-P
    closeout accepted `PACKAGING -CheckToolchain` plus `PACKAGING
    -VerifyPackagingArtifacts` on separately built INTERNAL and NC sets
    (`pre_g9b_r6_plus_e1_p_closeout_record.md:55-58`).
  - `FINAL` = INTEGRATION + the `infra.*` INFRA_UNIT leaves (registry `required_for_profiles`).
- **Scope categories and their coverage.**

| Category | G content | Covered by |
|---|---|---|
| 1. Product and UI acceptance | About content, distribution states, help entry points, Classic About | new phase selection `junit.desktop.pre-g9b-r6-plus-g` (PHASE); the whole Desktop suite including the P0/P1/G9U1 version and About tests (`junit.desktop.final`, INTEGRATION); author smoke |
| 2. Documentation consistency | EN/ES guide sections, coverage | `diagnostic.guide-structure` (STATIC, **diagnostic**: never acceptance). Acceptance-relevant guide assertions belong in G's JUnit selection, for example existing section identifiers in both editions and version strings, as `PreG9BP1PublicSurfaceTest` already does. |
| 3. Registry and inventory registration | new `PRE-G9B-R6-PLUS-G` phase selection, JUnit inventory, registry catalog pins | INFRA_UNIT (`infra.registry`, final-coverage checks); STATIC |
| 4. Version and build provenance | `package.yml` `version`/`version_date`, schema `3` provenance resource, title, About | PHASE (focused), INTEGRATION (all version pins), STATIC (re-pinned `package.yml` and schema) |
| 5. Packaging compatibility | `--app-version 1.1.0`, distribution record, build manifest, upgrade identity | PACKAGING `-CheckToolchain`, `-VerifyPackagingArtifacts` for an INTERNAL set and an NC set built from the exact candidate; agent `COMMERCIAL` refusal recipe |
| 6. Global verification infrastructure | none planned | — (escalation trigger) |

- **Frozen acceptance plan (proposal).** All runs on the clean, committed, frozen candidate
  `T`, with no inherited evidence (not even `D1`'s or `A-2`'s `FINAL`):

| # | Check | Status |
|---|---|---|
| 1 | `verify.ps1 -Profile STATIC` | **mandatory** |
| 2 | `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-G` | **mandatory**; proposed selection below |
| 3 | `verify.ps1 -Profile INTEGRATION` | **mandatory** (plan §12) |
| 4 | `verify.ps1 -Profile INFRA_UNIT` | **mandatory**: the phase-selection registration and inventory and registry pins change; it also covers any packaging-check extension through `infra.contract-boundary` |
| 5 | `verify.ps1 -Profile PACKAGING -CheckToolchain` | **mandatory** under the recommended `DQ-G-2`/`DQ-G-3`/`DQ-G-4` (the version, date and distribution record reach the package); conditional only if the author rejects all three |
| 6 | `verify.ps1 -Profile PACKAGING -VerifyPackagingArtifacts` on the INTERNAL set built from `T` | same as 5 |
| 7 | `verify.ps1 -Profile PACKAGING -VerifyPackagingArtifacts` on the NC set built from `T` | same as 5 |
| 8 | `-DistributionProfile COMMERCIAL` refuses and leaves the output root unchanged (agent recipe, not a profile) | **mandatory** with 5–7 |
| 9 | `git diff --check`; `Test-PromptContractDocument` for any changed prompt; JSON validation of mirrors | **mandatory** |
| 10 | author smoke (help entry points; About EN/ES on development, INTERNAL and NC builds; Classic About) | **mandatory**, after the technical acceptance |

- **Proposed `PRE-G9B-R6-PLUS-G` selection.**

| Check id | Status | Reason |
|---|---|---|
| `compile.desktop.semantic` | mandatory | — |
| `workspace-profile.product` | mandatory | the About and help texts live in the live application profile |
| `junit.desktop.pre-g9b-r6-plus-g.semantic` | mandatory | G's focused tests |
| `packaging.product` | conditional | builder or packaging-check change (expected under `DQ-G-3`; E1-P shape) |
| `infra.contract-boundary` | conditional | same condition |
| `compile.shared.semantic` | excluded | shared code is out of G scope; its appearance is a stop condition |

- **When `FINAL` is required.** Only through an explicit `VERIFICATION_ESCALATION_REQUEST`,
  in either of two cases:
  - G changes **global** verification infrastructure: `verify.ps1`, the shared
    verification modules, the registry, inventory or receipt schemas, an output adapter,
    the receipt or closeout flow, an `infra.*` check, or the semantics of an existing leaf
    beyond a phase-local contract addition;
  - the author chooses to treat the `1.1.0` promotion as a milestone that needs one
    receipt-bound campaign. In that case `FINAL` replaces items 3 and 4, with the same
    leaf coverage, one campaign and a receipt.

  A phase-local addition inside `packaging.product`, of the E1-P kind, is not global.
  Recorded intermittent observations (R-6) are no reason to escalate. The task-template
  causality rule applies: a failure proven to predate the candidate and to lie outside
  its delta is retained baseline debt. A heavy campaign is not repeated on an unchanged
  candidate without author authorization.
- **Recommendation.** Freeze `INTEGRATED_PHASE` with items 1–10. `CLOSEOUT_MODE` is
  proposed `AUTHOR_OPERATED`, the mode of the R6-plus subphase closeouts. Choose `FINAL`
  in place of 3+4 only if a single receipt-bound campaign is wanted for the version
  milestone.
- **Architectural consequences.** None on product layers.
- **Verification consequences.**
  - One new phase selection and its JUnit inventory, refreshed through the official
    updater with executed `final.desktop` and `final.shared` selection evidence. Missing
    that evidence makes INTEGRATION `EVIDENCE_UNTRUSTED`.
  - Re-pinned `package.yml` and schema.
  - INTERNAL and NC package builds.
- **Remaining uncertainty.** External tools (Ghostscript, Poppler, LaTeX/Asymptote,
  AutoCAD) are outside every profile. The R-6 intermittent observations may disturb
  `junit.desktop.final`.
- **Disposition requested.** Freeze the class and items 1–10, including the conditional
  rule for 5–7 and the selection, in the G implementation authorization. Alternatively,
  name `FINAL` explicitly.

### `DQ-G-2` — canonical version and version-date source

- **Question.** Where do the canonical version and its date live?
- **Existing contract.**
  - `package.yml` `application.version` is "the single product-version authority"
    (`windows-packaging.md:165-167`; P0 single-authority test).
  - No version date exists anywhere.
- **Alternatives.** A, B and C, compared in the
  [contract candidate](pre_g9b_r6_plus_g_product_metadata_contract_candidate.md) §3.
- **Recommendation.** **A** (`application.version_date` beside `application.version`;
  PM-1 to PM-4). B would duplicate the approved single authority or relocate it.
- **Architectural consequences.**
  - Package-profile schema: one required field.
  - Gradle: a second guarded extraction; provenance resource schema `2` → `3`.
  - `GeoCeDGBuildProvenance`: one getter.
  - No document-format change.
- **Verification consequences.** STATIC re-pins; focused tests; PACKAGING.
- **Remaining uncertainty.** None material.
- **Disposition requested.** Approve A, or B with an explicit amendment of the
  single-authority clause.

### `DQ-G-3` — distribution condition of the running build

- **Question.** From what authority does About state INTERNAL, NC or COMMERCIAL?
- **Existing contract and evidence.**
  - The builder selects a profile explicitly, defaulting to INTERNAL, and refuses
    COMMERCIAL (`build-windows-package.ps1:361-408,377-382`).
  - The JAR is profile-neutral. The only profile record, `build-manifest.json`, is
    outside the app-image (`:1051-1165`).
  - The runtime has no access to the condition.
- **Alternatives.**
  1. Install-level record written by the builder (recommended).
  2. Per-profile JAR built with a Gradle property.
  3. jpackage `-D` property.
  4. Inference from the notice file present.

  The last is prohibited. Alternatives 2 and 3 are compared in PM-5.
- **Recommendation.**
  - PM-5: `app/geocedg-distribution.json`, bound to the JAR's version, date and revision
    and to the notice hash.
  - Four runtime states: `INTERNAL`, `NC`, `DEVELOPMENT`, `UNVERIFIED`.
  - `DEVELOPMENT` is the conservative condition of unpackaged builds.
  - `COMMERCIAL` and any `not-authorized` profile yield `UNVERIFIED`.
- **Architectural consequences.**
  - The builder writes one more staged file.
  - The provenance resource embeds the profile table, so Java holds no marker literals.
  - Runtime reading follows the curated-library precedent (`jpackage.app-path` locates;
    content decides).
- **Verification consequences.**
  - A phase-local `packaging.product` contract addition (E1-P precedent).
  - PACKAGING artifacts for INTERNAL and NC.
  - Fixture-record tests for every state.
- **Remaining uncertainty.** The author may prefer `DEVELOPMENT` to read "INTERNAL
  (development)". Both are non-redistributable.
- **Disposition requested.** Approve PM-5 and the state wording.

### `DQ-G-4` — `1.0.0` → `1.1.0` lifecycle

- **Question.** When do `1.1.0` and its date become authoritative and observable, without
  post-acceptance mutation and without presenting an unapproved version as released?
- **Existing contract.** The E1 preparation closeout §5:
  - visible `1.0.0` through the unfinished mini-track;
  - `1.1.0` "promoted only when `PRE-G9B-R6-plus-G = PASS — AUTHOR APPROVED`";
  - version date = "the date of the author-approved promotion"; build date separate;
  - no tag or release implied.
- **Alternatives.**
  - **M1:** approve what was verified.
  - **M2:** a post-approval version commit.
  - **M3:** approval flag plus activation.

  See contract candidate §6.2.
- **Recommendation.** M1 with lifecycle stages L0–L7:
  - `1.1.0` and `D` are proposed in the version-transition commit, which is part of `T`;
  - observable only in unpublished candidate builds;
  - authoritative at the author's approval of the exact `T`;
  - published by a separate exact-SHA publication;
  - never a release.

  `D` is designated by the author before the freeze, and the approval adopts it. A
  different `D` or version requires a fully re-verified metadata-only successor.
- **Architectural consequences.** No post-approval product mutation. The documentary
  closeout must change no product, build, packaging, test or verification path.
- **Verification consequences.** The single frozen campaign of `DQ-G-1` covers the
  version transition.
- **Remaining uncertainty.** The reading of "visible version" in §6.1 of the candidate
  needs explicit author confirmation.
- **Disposition requested.**
  - **(a)** Approve M1 and the reading.
  - **(b)** Confirm or reject `2026-09-16` as the date of `1.0.0`. Evidence: the P0
    closeout commit `1c97e780`.
  - **(c)** Designate `D` now or before the freeze.

### `DQ-G-5` — `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`

- **Question.** How is the observation disposed of before the global closeout?
- **Existing contract.**
  - The A-2 closeout `:195` records it: `OPEN — NOT A-2 BLOCKING — IMPLEMENTATION NOT
    AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT`.
  - The C closeout `:192` and the C-X1 closeout `:135` keep it unchanged.
  - The approved inputs:
    - `AQ-X2`: `ExportArea` is `SESSION`, "resets on New and Open";
    - `DQ-D1-9`: the paste notice clears "on the next document transition";
    - `DQ-A2-6`: the document-transition vocabulary, including the API file and URL routes;
    - A-2 revision 2: the `documentReplacementCommitted()` seam; Base64 is transport, not
      a format;
    - `DQ-C7`: `drawingScale` resets "independently of transport";
    - `iso-a-border.md:417-425`: "non-native document replacement → stale" link versus
      "New, Open → `resetForDocument`".
- **Source evidence** (§5):
  - `ExportArea` and the paste notice reset only in `AppGeoCeDG.clearConstruction`,
    `loadExistingFile` and `loadXML(String)` (`AppGeoCeDG.java:1280,1283,1303,1304,1316,1317`).
  - The approved hook `documentReplacementCommitted()` (`App.java:4368`; override
    `AppGeoCeDG.java:470-477`) commits the layer workspace and resets `drawingScale` only.
  - Proven by reading source: five full-document replacement routes keep the previous
    `ExportArea`, producer, overlay and notice:
    - GeoCeDG File → Open of `.html`/`.htm`, a file type of the approved Open dialog
      (`DQ-F3-2`);
    - API `setBase64`;
    - API `openFile` of `.cedg`/`.ggb` and URL drops;
    - API `openFile` of non-native files;
    - API `setXML`.
  - **Correction to the earlier records.** A non-native *file* opened through File → Open
    or Open Recent does reset, because it passes through `loadExistingFile`. The A-2 and C
    records describe this too broadly.
  - Nothing has been run at runtime.
- **What is proven, suspected and untested.**
  - **Proven by approved tests:** the working-layer, hidden-set, unit, `drawingScale`,
    memory-slot and orientation-cue lifecycle on these routes.
  - **Proven by source only:** the five non-resetting routes.
  - **Suspected:** the user-visible consequence that the next Picture, LaTeX or DXF export
    of the new document uses the previous document's manual or ISO rectangle.
  - **Untested:** `ExportArea` and notice on every non-resetting route; the link-lost
    notice after a replacement; the `.html` route through the real chooser.
- **Alternatives (task options).**
  - **A** characterize first, then the author disposes;
  - **B** accept as a characterized limitation;
  - **C** defer with an owner;
  - **D** a separately authorized correction.
- **Recommendation: A.** The approved texts disagree:
  - the literal `AQ-X2` "Open" covers the `.html` Open route and, under `DQ-A2-6`, API
    `openFile` of native documents;
  - the E3 specification presumes that a non-native replacement leaves a stale link
    rather than resetting it.

  No approved text fixes the expected `ExportArea` or notice behaviour for `setBase64`,
  non-native `openFile` or `setXML`. B would accept unmeasured consequences. C would leave
  a source-evident deviation on a published GeoCeDG Open route. D needs the author to
  settle the scope of "Open" first. The bounded plan of §5 is headless, run from a scratch
  location, and changes no product code.
- **Information for a later D.**
  - Any correction is a single session reset of `ExportArea` and the paste notice at the
    existing successful-transition events E2, E3 and E5: `nativeDocumentLoadCommitted`,
    `documentReplacementCommitted` and the API branch of `setXML`, as the C preparation
    already suggested.
  - It must not go into `commitLoadedDocument` or the generic clearing `setXML`, which
    also serve tool replacement.
  - This is not a proposal to implement.
- **Architectural consequences.** None for A.
- **Verification consequences.** The scratch probe evidence is part of G's
  characterization report, not of acceptance.
- **Remaining uncertainty.** The runtime behaviour of the exports. How the author reached
  Open URL in the A-2 smoke: the v1-fallback route pre-clears, which hides the
  observation.
- **Disposition requested.**
  - Authorize the characterization inside G (scenarios SC-01 to SC-27).
  - Decide the disposition after it, before the global closeout. A split outcome is
    possible: D for routes in the approved "Open" vocabulary, B for v1-fallback-only or
    embedded-only routes.
  - Characterize `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` together with it (shared
    `setXML` → `commitLoadedDocument` seam), but dispose of it separately.

### `DQ-G-6` — structured debt and risk register

- **Question.** What disposition, owner and blocking effect does each open item of the
  R6-plus track get?
- **Existing contract.** Readiness §5 holds the proposed matrix. Several approved
  closeouts delegate the owner choice explicitly to "the G structured debt register":
  F3 closeout `:250-252` for the seven `DEFER-F3-*`, and the retained `OBS-F3*` rows.
  `PRE-G9B-R7` later stops on any open debt with no owner.
- **Source evidence.** Proposal C re-derives 134 rows from the latest approved records
  and the approved code: 3 B, 18 P, 10 D, 27 O, 31 L, 45 R.
- **Alternatives.**
  1. Decide every row in G (recommended for B and P; owners for D and O).
  2. Defer owner assignment for O rows to `PRE-G9B-R7`. This is allowed, but R7 then
     starts with unowned items.
- **Recommendation.** Option 1, with the changes proposal C makes to readiness §5:
  - `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` moves P → B, because the approved C prompt
    closeout requires a disposition before the global closeout.
  - The two E3 unit-gate and `ExportArea` observations, and five F4 reconciliation risks,
    move to R. The approved code and tests hold them; the author confirms.
  - `OBS-E2-1`, `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER`, `OBS-A1-RECOMPUTE-OUTPUTS` and
    `OBS-D1-DEFAULTS-SAVE-RELEVANT` move to L.
  - Desktop test-heap pressure moves to R. The E2 test-only host-retention correction
    applies, and the residual risk is monitored in G's INTEGRATION.
  - Two E2 library collisions move O → P.
  - Eight items the readiness package missed are added. Among them: undisposed C and E3
    specification amendments still labelled "technical candidate" inside approved
    specifications (P-18); the E3 retained limitations; the F1 approval exclusions; and a
    pre-existing off-EDT export race relevant to the INTEGRATION risk R-6.
- **Specific requests from the task.**

| Item | Proposal |
|---|---|
| seven `DEFER-F3-*` | **D**, owner a named future document-insertion and styles contract phase. `DEFER-F3-3D` comes after the G9 spatial closeout. |
| `TD-PDF-1` | **D**, owner G15 ("PDF vectorial con tamaño exacto"), preceded by a named contract-design task |
| `OBS-F4P-FREEHEP-XREF-SIZE` | **O**, owner G15 |
| `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | **P**, owner G11, unless the author wants a bounded slice before G9B |
| `ENH-B-MANUAL-EXPORT-AREA-DRAG` | **P**, owner G15 (export area and physical publication), with G12 as an alternative |
| E3 observations | **R**, pending author confirmation |
| Asymptote-with-label limitation | **L**, tracking owner G15; G may retry if its environment can typeset labels |
| R-6 instability observations | **O**, watched in G's INTEGRATION; persistent owner `PRE-G9B-R7` |
| F4-A-L1 to L3 | **L**. L3 is a revalidation trigger for any runtime or look-and-feel change, including G's packaging. |

- **Architectural consequences.** None.
- **Verification consequences.**
  - Recurrence of the R-6 observations during INTEGRATION is handled by the
    task-template causality rule.
  - The `OBS-R6P-SAVE-WARNING` finding stands: the specification requires a save warning
    whose method `GeoCeDGExternalCompatibilityWarning.confirmSave` has no production
    caller. G's help must not claim the warning exists until the author disposes of it.
- **Remaining uncertainty.** Two rows were not re-verified to the line: `P-14` and `P-15`.
  G checks them at its base.
- **Disposition requested.**
  - Decide the category and owner of every B, P and D row.
  - Assign owners to the O rows, or accept that R7 inherits them.
  - Confirm the R rows marked "closeout silent".

### `DQ-G-7` — help and documentation depth

- **Question.** How deep must G's guide and in-product help work go?
- **Existing contract.**
  - Plan §3.2 G: "bilingual guide and in-product help for every accepted capability".
  - The documentation-maintenance contract: claim vocabulary §3; `GUIDE_IMPACT` §5;
    guide structure §10.
  - The E1 preparation closeout `DQ-E1-15` left the library's extended help to G.
- **Source evidence** (§6):
  - Every R6-plus command has EN/ES Input Help syntax (`command.properties:509-518`,
    `command_es.properties:497-506`).
  - Every R6-plus tool mode has EN/ES tool help (`menu.properties:1203-1212`,
    `menu_es.properties:1119-1128`).
  - Every non-mode action has a bilingual tooltip, because the profile loader rejects a
    missing text (`GeoCeDGProfile.java:319-322`).
  - The profile `long_help` texts and the library caveats (`UserTools.BundledNote.*`)
    have no display route. The guide is their only user-visible form.
  - The EN and ES editions are structurally identical: 17 markers and 1/16/87 headings.
- **Alternatives.**
  1. Guide prose plus corrections of existing entry points (recommended).
  2. A new contextual-help surface that displays `long_help`.
  3. A tooltip on the inherited picture dialog.

  Options 2 and 3 add UI surface or touch Classic-shared upstream keys, without
  necessity.
- **Recommendation.** Option 1, with the minimal help package of §6.2. Five required
  items, all paragraphs, no new `###` headings: `PreG9BR5AGuideOutlineTest` pins the
  outline. Optional editorial items are listed separately.
- **Architectural consequences.** Documentation and profile text only.
- **Verification consequences.**
  - The guides are packaged byte copies, covered by `PostP1BilingualUserGuideTest` and
    `PreG9BR5AGuideOutlineTest`.
  - Guide coverage assertions belong in G's selection; the structure diagnostic is not
    acceptance.
  - `GUIDE_IMPACT = UPDATED` (both editions).
- **Remaining uncertainty.** Whether the author wants F4-A-L1 and TD-PDF-1 named in the
  user guide. Recommended: yes, as neutral known-limitation bullets.
- **Disposition requested.** Approve option 1 and the required items; select the optional
  ones.

### `DQ-G-8` — stale status reconciliation

- **Question.** How are the stale or contradictory status labels reconciled? They are
  readiness findings R-1, R-2 and R-7 to R-10, plus the new findings R-11 to R-15 of this
  record.
- **Existing contract.** Documentation-maintenance contract:
  - §4: living documents are corrected at `HEAD`, and historical evidence is never
    rewritten;
  - §7: the smallest coherent set; historical wording kept only with an explicit
    commit/tag qualifier.
- **Source evidence.** §7.1: every label, the record that supersedes it, and the git
  ancestry of the published commits.
- **Alternatives.**
  1. Correct inside G as documentation (readiness proposal).
  2. A separately authorized documentary commit before G, so that G starts from a clean
     register.
  3. Leave the labels and rely on the G structured register only.
- **Recommendation.**
  - **Option 1 by default**: the corrections are part of G's closeout documentation.
  - **Option 2** if the author wants the implementation base to carry a consistent plan
    table, for example for the G entry check.
  - Never option 3 for the plan §3 State column, which the plan declares "the only current
    status" (`:312` at `abe12893`).
  - Every correction is a dated, additive status update in a living document. Closeout
    records, candidate reports, JSON mirrors and the readiness package stay byte-identical.
- **Architectural consequences.** None.
- **Verification consequences.** STATIC only.
- **Remaining uncertainty.** Three labels need author confirmation, not just a citation,
  because no approved closeout disposes of them explicitly:
  - R-9a: `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`;
  - R-9c: `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL`;
  - R-9d: `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN`.
- **Disposition requested.**
  - Approve the correction list of §7.1 and option 1 or 2.
  - Confirm or reject "resolved" for R-9a, R-9c and R-9d.
  - Decide on R-13 to R-15, which lie outside the readiness findings.

### `DQ-G-9` — documentary observations

- **Question.** Do the seven documentary observations still apply? Can each be corrected
  as documentation maintenance?
- **Source evidence.** §7.2. All seven still apply at `abe12893`, except the count part of
  `OBS-R6P-ACTION-COUNT-DOC`, which is already fixed. Two targets are byte-pinned by
  historical phase verifiers:
  - `verify-g9u0-r2-product-refinement.ps1:40-45,451-462` pins the native-document-identity
    specification and ADR 0016;
  - `verify-g9x1-extended-dxf.ps1:60-64` pins the DXF fidelity specification, and that pin
    **already mismatches at `HEAD`** since E3 `257c854a`. This is pre-existing. The script
    is not a registry leaf.
- **Alternatives, per item.**
  1. Correct the status, header or purpose line.
  2. Retain it as a registered documentary inconsistency.
  3. Request a separate authorization, when the correction would touch an ADR body, a
     normative promotion, or a verifier pin.
- **Recommendation.** Per item, §7.2. Summary:
  - correct as maintenance: `OBS-F4P-S3-SPEC-HEADER` (with author confirmation),
    `OBS-R6P-DXF-DEFAULT-DOC`, `OBS-R6P-DXF-SPEC-HEADER` line `:7` only,
    `OBS-R6P-ACTION-COUNT-DOC` (residual labels) and
    `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE`;
  - for `OBS-R6P-DOC-IDENTITY-HEADER`, the ADR 0029 status needs author confirmation; the
    specification and ADR 0016 header lines are blocked by the G9U0-R2 pin, so either
    retain them or authorize a pin reconciliation;
  - retain `OBS-F4P-ADR0035-CONSEQUENCE-LINE` as a registered inconsistency, unless the
    author separately authorizes an additive cross-reference in the ADR body;
  - the DXF specification's v1.1 / E3 amendment status (`:5`, `:181-187`) is a normative
    decision, outside documentation maintenance.
- **Architectural consequences.** None. No geometric specification, ADR decision or
  scientific contract changes.
- **Verification consequences.**
  - STATIC.
  - `docs/upstream/modified-files.yml` is an impact path of Desktop JUnit selections.
    Editing it widens G's impacted selection but breaks no contract.
- **Remaining uncertainty.** None beyond the author confirmations listed.
- **Disposition requested.** Approve, retain or escalate each item of §7.2.

### `DQ-G-10` — publication and implementation base

- **Kind.** Resolved historical decision. The author published `19f8f206` and
  `abe12893` before this task.
- **Record.** `DQ-G-10 = PUBLICATION COMPLETED — EXACT BASE ESTABLISHED` (§1.2).
- **Consequence.** The recommended implementation base is `abe12893`, unless the author
  approves and publishes a documentary successor and names it.
- **Disposition requested.** None beyond naming the exact base in the G implementation
  authorization.

## 3. B — Product metadata contract (summary)

The [contract candidate](pre_g9b_r6_plus_g_product_metadata_contract_candidate.md) holds
the full design: PM-1 to PM-12 and the lifecycle in §6. In short:

| Fact | Authority | Shown |
|---|---|---|
| identity | `package.yml` `application.name` | About, title |
| version | `package.yml` `application.version` (sole authority) | About, title, guide window, jpackage, guides |
| version date | `package.yml` `application.version_date` (author-designated promotion date) | About |
| build identity | the generated provenance (commit, state, source) | About, as "Build: …"; never a date |
| distribution condition | install-level record written by the builder, verified against the JAR | About (`INTERNAL`, `NC`, `DEVELOPMENT`, `UNVERIFIED`) |
| licensing | `LICENSE`, `NOTICE.md`, `THIRD_PARTY.md`, `docs/LICENSE.md`, component matrix | About summary plus route; never a single-licence label |

Classic About is unchanged.

## 4. C — Debt and observation disposition (summary)

The full traceable matrix is the
[debt and observation disposition proposal](../validation/pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md).
For every item it gives:
- the exact identifier and its source authority;
- the recorded status, quoted;
- the supporting evidence;
- the proposed category and owner;
- three blocking columns (G entry / G completion / global closeout);
- the required author decision.

It also records twelve contradictions between records and how the authority order
resolves them.

| Category | Rows | Blocks G entry | Needs a decision before the global closeout |
|---|---|---|---|
| **B** blocking | 3: `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`, `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`, `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` | no | **yes**: disposition or implementation |
| **P** pending author disposition | 18 (Classic off-EDT sites, the two enhancements, `OBS-R6P-SAVE-WARNING`, four `OBS-A1-*`, seven documentary items, two E2 library collisions, undisposed C/E3 spec amendments) | no | owner or disposition; the documentary ones through `DQ-G-9` |
| **D** deferred with owner | 10 (seven `DEFER-F3-*`, `TD-PDF-1`, POST-E2-P1 Option B, physical zoom aid) | no | an owner (delegated to the G register by the approved closeouts) |
| **O** open, non-blocking | 27 | no | an owner by R7 at the latest; the F3-delegated ones in G |
| **L** accepted limitation | 31 | no | none (author confirmation for four re-categorized rows) |
| **R** resolved by approved implementation | 45 | no | label reconciliation and author confirmation for rows where the closeout is silent |

**Not done by this proposal:** it closes, waives or re-owns nothing. An item is never
treated as closed merely because its suspected cause resembles code that was later
modified. Every R row cites the approved implementation and the test or closeout that
proves it.

## 5. Non-native document replacement: characterization plan

The source characterization was done read-only at `abe12893`; nothing was executed.

Path abbreviations:
- `AGD`: `org/geocedg/desktop/AppGeoCeDG.java`;
- `APPD`: `org/geogebra/desktop/main/AppD.java`;
- `APP`: `org/geogebra/common/main/App.java`;
- `GMD`: `org/geogebra/desktop/gui/GuiManagerD.java`;
- `EAS`: `org/geocedg/desktop/export/ExportAreaSession.java`.

### 5.1 The distinctions the task requires

| Notion | Meaning at `abe12893` | Commit hook |
|---|---|---|
| native full-document opening | `.cedg`/`.ggb` through `loadNativeArchive` (`APPD:3284-3344`): File → Open, Open Recent, file drop, API `openFile`, URL drop | `nativeDocumentLoadCommitted()` (`AGD:455-462`): layer commit and `drawingScale` reset (E2) |
| non-native full-document replacement | a stream that is not a native archive, a Base64 archive or an HTML page replaces the whole document (`App.loadXML(ZipFile)`, `GFileHandler.loadXML`) | `documentReplacementCommitted()` (`APP:4352,4368`; `GFileHandler.java:111`; override `AGD:470-477`): layer commit and `drawingScale` reset (E3) |
| Base64 transport | `setBase64` (`GgbAPIJre.java:100-111`), `.html` pages (`APPD:1304-1376`), `base64://` startup. **The same document format** (A-2 closeout) | as non-native replacement |
| file insertion / merging | F3 Insert File through a hidden helper and paste (`GeoCeDGDocumentInsertion`); Apply Styles; v1 merges and `evalXML` | none on the target. The target keeps working layer, `ExportArea`, link and `drawingScale`. Insert File shows a **new** paste notice. |
| session vs document state | session: working layer, `ExportArea` with producer, overlay and ISO link, paste notice, clipboard provenance, `drawingScale`, reference memory, tool activation. Document: hidden layers (since A-2), units, objects. Preference: orientation cue. Derived: status segments, line-thickness field. | — |

### 5.2 Route × state outcome (source-derived)

**Bold** marks a difference from New/Open.

| Route | working layer | `ExportArea` (manual, producer, overlay) | ISO link | paste notice | `drawingScale` |
|---|---|---|---|---|---|
| New; native Open; non-native **file** Open via `loadExistingFile`; `loadXML(String)`; blank `reset()` | reset or `DQ-A2-1` | reset | cleared | cleared | 1:1 |
| File → Open `.html`/`.htm` (`GMD:2190-2195` → `loadBase64File`) | `DQ-A2-1` | **kept** | **stale → link lost** | **kept** | 1:1 |
| API `setBase64` | `DQ-A2-1` | **kept** | **stale** | **kept** | 1:1 |
| API `openFile` `.cedg`/`.ggb`; URL drop (`APPD:3249-3282`) | `DQ-A2-1` | **kept** | **stale** | **kept** | 1:1 |
| API `openFile` non-native | `DQ-A2-1` | **kept** | **stale** | **kept** | 1:1 |
| API `setXML` (`AGD:512-528`) | `DQ-A2-1` | **kept** | **stale** | **kept** | 1:1 |
| v1 Open URL (pre-clear) | `DQ-A2-1` | reset by the pre-clear, unless its save prompt is cancelled | — | — | 1:1 |
| tool replacement (clearing `setXML` without API marker) | **recomputed** (`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`) | kept (same document) | stale (rebuild) | kept | kept |
| embedded `closeWithoutSavePrompt` (`AGD:1580-1590`) | **kept** | **kept** | **stale** | **kept** | **kept** |
| Insert File; Apply Styles | kept | kept | kept | Insert File: new notice | kept |

### 5.3 Characterization scenarios (proposed; execution only under G authorization)

- **Vehicle.** A scratch JUnit class run through a Gradle init script on the existing
  Desktop test classpath, following `PreG9BR6PlusA2HiddenLayerPersistenceTest`'s
  live-host pattern. It is never committed.
- **Real routes.** The `.html` route goes through `GuiManagerGeoCeDG.openResolved`. URL
  drops go through `FileDropTargetListener` in the windowed harness.
- **Observables.**
  - `ExportAreaSession`: producer, manual area, overlay, `isLinkLost`, resolved source;
  - status-bar notice and sheet segments;
  - `drawingScale`, working layer and hidden set;
  - reference memory, tool activation, orientation cue;
  - construction identity, saved flag, undo history.

Expected values:
- **R** reset;
- **P** preserved;
- **C** recomputed;
- **N** no approved contract, so the author decides. The bracketed value is the
  analogue-consistent expectation, given for information.

| Id | Route | State | Expected | Contract |
|---|---|---|---|---|
| SC-01 | File → Open `.html` | `ExportArea` | R | `AQ-X2` + `DQ-F3-2` A1 (`.html` is an Open file type, `document-insertion.md:34-38`) |
| SC-02 | API `openFile` native; URL drop | `ExportArea` | R | `AQ-X2` + `DQ-A2-6` ("native-document Open transaction … including the API file and URL routes") |
| SC-03 | API `setBase64` | `ExportArea` | N [R] | Base64 is transport (A-2 closeout); `DQ-C7` "independently of transport" |
| SC-04 | API `openFile` non-native; `loadXML(File)` | `ExportArea` | N [R] | as SC-03 |
| SC-05 | API `setXML` | `ExportArea` | N [R] | as SC-03; C event E5 |
| SC-06 | controls: `loadFile` native and non-native | `ExportArea`, notice | R | `AQ-X2`; `DQ-D1-9` (confirms the correction in §5.1) |
| SC-07 | tool replacement | `ExportArea` | N [P] | C: "not a document transition" |
| SC-08 | Insert File (success, K1-S); Apply Styles | `ExportArea`, link | link P (proven); manual N [P] | `document-insertion.md:177` |
| SC-09 | non-resetting routes with an active ISO link | link, `linkLost`, sheet segment | stale with notice; R if the author picks R in SC-01..05 | `iso-a-border.md:424` vs `:425` |
| SC-10 | tool replacement with an ISO link | link | stale | `iso-a-border.md:424` |
| SC-11 | non-resetting routes with a visible notice | notice | R | `DQ-D1-9` "next document transition", with `DQ-A2-6` and C E2/E3/E5 |
| SC-12 | tool replacement with a visible notice | notice | N [P] | C: not a transition |
| SC-13 | Apply Styles with a visible notice | notice | N [P] | `document-insertion.md:242-246` |
| SC-14 | `.html` via the chooser; URL drop | `drawingScale` | R | C E2/E3 |
| SC-15 | Insert File; Apply Styles | `drawingScale` | P | C non-reset routes |
| SC-16 | v1 Open URL, save prompt cancelled, then Base64 | `ExportArea`, notice, scale | scale R; others N | C (pre-clear is not a reset point) |
| SC-17 | tool replacement, working layer set below the top | working layer | N (currently recomputed 2 → 6) | `AQ-L2`, `DQ-A2-1`, A-2 route table; C labels disagree |
| SC-18 | tool replacement from a macro-edit window | working layer of the original window | N | as SC-17 |
| SC-19 | Insert File; Apply Styles | working layer | P | `AQ-L8`; `DQ-A2-6` |
| SC-20 | `.html` via the chooser; URL drop | working layer, hidden set | C (`DQ-A2-1`) | A-2 prompt |
| SC-21 | non-resetting routes | memory slots | R (EMPTY) | `authoring-memory.md:124` |
| SC-22 | `setBase64`, `setXML` with Parallel memory active | activation | P | `authoring-memory.md:274-276` |
| SC-23 | native Open, API `openFile` with memory active | activation | N (inherited Move switch: departure or clearing load?) | `authoring-memory.md:270-276` |
| SC-24 | every route | orientation cue | P | `orientation-cue.md:179-181` |
| SC-25 | API routes on and off the EDT | status segments | C | `unit-system.md:788`; D1 EDT rule |
| SC-26 | embedded `closeWithoutSavePrompt` | session states | N (embedded host only) | `document-insertion.md:222-223` (silent) |
| SC-27 | startup `base64://`, `.html`; New Window | all | fresh | `AGD:414-433` |

Each scenario records the before and after values of its observables. For any route the
author later decides must reset, the scenario becomes the regression fingerprint. Without
a separate authorization, nothing here changes product code or adds tracked tests.

## 6. Help coverage matrix

Characterized read-only at `abe12893`.

Abbreviations:
- **EN**, **ES**: the user-guide editions `docs/user/geocedg_user_guide_{en,es}.md`;
- **PRF**: `apps/geocedg/application-profile.yml`;
- **MENU**, **CMD**: the shared `menu*.properties` and `command*.properties`.

Classes follow the documentation contract §3:
- **OBS**: observable;
- **INT**: productive internal;
- **FUT**: not implemented or not approved, never to be documented as available.

### 6.1 Matrix

| Capability | Class | Guide (EN / ES) | In-product entry points | G action |
|---|---|---|---|---|
| A-1 working layer, Move-group mode, status-bar `Layer: n` | OBS | complete (EN §12.5 :1758-1782; ES :1842-1868) | tool help MENU:1203-1204; status tooltip; chooser | none |
| A-1 numeric Algebra View order | OBS | partial: "Sort by Layer" named, numeric order not stated | inherited view | optional clause |
| A-1/A-2 effective hiding; domain 0..99; hidden layers saved | OBS | complete (EN :1760-1795; ES :1848-1884) | eye; "(hidden)" suffix; load errors | none |
| working-layer direct numeric entry (`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`) | FUT | correctly absent | — | none |
| B export surface and `ExportArea` | OBS | complete (EN §4.5 :416-458; ES :430-478) | profile actions and texts | none |
| manual export-area drag (`ENH-B-MANUAL-EXPORT-AREA-DRAG`) | FUT | correctly absent (EN :432 says a selection rectangle is never an area) | — | none |
| C drawing scale and physical size; LaTeX semantic curves; DXF units, sidecar and hidden layers | OBS | complete (EN :460-501, :583-587, §11.7–§11.10) | `ExportScale.*`, `LatexExport.*`, `ExportDxf.*` | optional: name Spline V2 beside Locus V2 for vector pictures |
| C-X1 Classic picture dialog on the EDT | INT | — | — | none |
| D1 units, unit status, new-document defaults, paste notice | OBS | complete (EN §4.6 :514-587; ES :536-618) | Document units dialog; status segments; paste-notice tooltip | none |
| E1 curated GGT library (12 tools, GeoCeDG library section, precedence, integrity) | OBS (installed product only) | **near absent** (one mention, EN :1855) | hover tips PRF:5267-5314; caveats PRF:5251-5266 **never displayed** | **required:** one paragraph (§13) plus an installed-only qualifier in §2.1 (EN :85-88 currently implies a source checkout has every capability) |
| E2 `AlignedDimension`, `LinearDimension`, presentation preferences, draggable offset | OBS | complete (EN §9.5 :1340-1424) | tool help MENU:1205-1210; Input Help CMD:513-516 | none |
| E3 `IsoABorder`, export-area link, coherence checks | OBS | complete (EN §4.7 :589-676) | MENU:1211-1212; CMD:517-518 | optional: qualify the LaTeX-label sentence (EN :663-664) for Asymptote (verification limitation) |
| F1 orientation cue | OBS | complete (EN §5.2 :713-722); missing from the §2.4 preference list | action and preference text | optional |
| F2/F2-R1 remembered references, Angle with Given Size | OBS | complete (EN :724-780) | status indicator; upstream tool help (Classic-shared) | none |
| F3 Insert File, Apply Styles; OFF files leave the UI | OBS | complete (EN §4.2 :337-386) | actions and texts | optional, ES only: "hoja" vs "lámina" ISO A |
| F3 Apply Template | FUT (not approved under T0) | correctly absent | no action | none |
| F4-A numeric thickness field | OBS | **partial**: only under dimensions (EN :1384-1392); §12.3 silent | tooltip `Properties.LineThickness.Field` | **required:** one §12.3 sentence; F4-A-L1 as a known limitation |
| F4-D embedded Type 1 fonts and "Export Text as Shapes" | OBS | **absent** | inherited checkbox `ExportTextAsShapes` (MENU:737; ES "Exportar textos como figuras"). It defaults on (`GraphicExportDialog.java:109`) and is re-selected whenever PDF is chosen (`:320-321`). When cleared, the PDF embeds Type 1 fonts (`PictureExportService.java:181-182`). | **required:** 2–3 sentences in §4.5. No tooltip: the dialog and key are Classic-shared. |
| F4-B DXF opened in AutoCAD 2026 | OBS (interoperability outcome) | no reader statement | — | recommended: one sentence, "checked with AutoCAD 2026 only" |
| POST-E2-P1 physical PDF style sizes (`0.4·t` pt) | OBS | **absent** | — | **required:** one sentence in the §4.5 physical-size paragraph |
| TD-PDF-1 physical PDF text size | deferred debt, observable | absent | — | **required:** a §15 known-limitation bullet |
| POST-E2 P2–P4 | OBS | complete | — | none |

### 6.2 Minimal G help package (recommended for `DQ-G-7`)

Required, EN and ES in lockstep, written as paragraphs (no new `###` headings):

1. §4.5 — "Export Text as Shapes" and embedded Type 1 fonts (F4-D).
2. §4.5 — physical PDF style sizes (POST-E2-P1).
3. §13 — the GeoCeDG library paragraph, with the author's caveats; §2.1 installed-only
   qualifier.
4. §12.3 — the numeric thickness field.
5. §15 — known limitations:
   - TD-PDF-1;
   - F4-A-L1;
   - DXF checked with AutoCAD 2026 only;
   - `DQ-G-5`, if the author accepts it as a limitation.
6. §1.3 — the new About content; the version strings change only in the
   version-transition commit (EN :4,65,83,2131; ES :4,67,86,2234; index :9).

Optional, editorial:
- the A-1 numeric-order clause;
- F1 in §2.4;
- Spline V2 in the §4.5 picture sentence;
- the Asymptote label qualifier;
- §16.1 and §16.4 reference rows;
- the setext defect: a `---` directly after a paragraph at EN :1804-1805 and ES
  :1892-1893;
- the ES "hoja/lámina" alignment;
- in the profile texts: `Ctrl+Shift+U/C/D` in `Workspace.Shortcuts`; "construction
  guide" → "user guide" in the guide-window tooltip.

Not recommended:
- editing Classic-shared upstream properties;
- a tooltip on the inherited picture dialog;
- a display route for `long_help` or `BundledNote`;
- documenting F4-A-L2 or F4-A-L3 for users. They stay in the debt register.

## 7. Status and documentary reconciliation

Line numbers refer to the committed `abe12893` blobs. The plan lines shift in the
documentary candidates as follows:

| After `abe12893` line | Revision 1 (`ccb4ef34`) | Revision 2 |
|---|---|---|
| `:100` | +1 | +2 |
| `:1663` | +5 | +6 |
| `:1757` | +10 | +16 |

"Plan" is
`docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`; "roadmap" is
`docs/roadmap/geocedg_roadmap.md`; records are under `docs/validation/`.

Every correction is proposed, not applied, unless marked **applied here**. Every
correction:
- is additive or replaces a status cell in a living document;
- is dated "reconciled by G, <date>";
- leaves the superseded text recoverable from history.

### 7.1 Stale status labels (`DQ-G-8`)

| Id | Current text (at `abe12893`) | Superseding source | Correct status | Minimal correction | Preserve |
|---|---|---|---|---|---|
| R-1a E1-P / E1 | plan §3 `:303` E1 row `PASS — AUTHOR APPROVED`; E1-P `PASS — AUTHOR APPROVED`; diagnostic-launcher observation "open"; plan §14 `:1271`, `:1281` | E1-P closeout §7 `:133-140` (non-force fast-forward to the closeout `fdc1ade6` authorized); E1-X1 authorization `:29-30` (live remote `main` = `fdc1ade6`); plan §14 `:2057-2059` (launcher resolved in E1-X1). The roadmap `:3393`, `:3395` is already correct. | E1, E1-P `PASS — AUTHOR APPROVED — PUBLISHED`; launcher observation resolved in E1-X1 `b12ab32c` | append `— PUBLISHED` (with `fdc1ade6`) in `:303`, `:1271`, `:1281`; qualify "open" as "at the E1 closeout; resolved in E1-X1" | E1-P closeout record and mirror; plan bullet `:25` |
| R-1b E2 | plan §3 `:304` `NOT AUTHORIZED` (unchanged since `147dac8d`) | E2 closeout `:4`, `:60-69` (publication authorized `:68`); plan §14 `:1308-1316`; roadmap `:3398` | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-08, `3a724661`) | replace the State cell, keeping "earlier `NOT AUTHORIZED`" | E2 records; §14 "earlier" chain |
| R-1c F1 | plan §3 `:306` `NOT AUTHORIZED` | F1 closeout `:4`, `:70`, `:139-155`; roadmap `:3400` | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-11, `1cea3c89`; excludes X1–X10 and Graphics View 2) | replace the State cell | F1 records |
| R-1d E3 | plan §3 `:305` and §14 `:1345-1350` "publication NOT AUTHORIZED"; POST-E3 sub-items `:1384`, `:1406-1407`; roadmap `:3399` without `PUBLISHED` | `post_e3_followups_author_reconciliation_record.md:42-45` (`P_POST_E3` `31286a23`, published E3/E3-R1 closeout) and §11 `:261-264`; plan §14 `:1416-1418` (`POST-E3-INTEGRATION` `00b525fd` published) | E3, E3-R1 `PASS — AUTHOR APPROVED — PUBLISHED`, first published by `31286a23`, before `00b525fd`. The readiness package's "via `00b525fd`" is imprecise; it is not corrected, because the package is a frozen preparation. | append `— PUBLISHED`, qualify "publication NOT AUTHORIZED" as "at the closeout"; same pattern for roadmap `:3399` and the POST-E3 sub-items | E3 closeout record (`:8`, `:83`, `:223-233`) and mirror; plan bullets `:50-51` |
| R-1e F4 | plan §3 `:309` F4 row has no publication fact; **the roadmap phase table has no F4 row** (`:3402` F3 is followed by `:3403` G) | F4 aggregate closeout `:4-10`; publication facts §1.2 | `COMPLETE — AUTHOR APPROVED — PUBLISHED` | append the publication to `:309` (**applied here**, together with the §14 F4 block, as the `DQ-G-10` registration); insert one roadmap row for `PRE-G9B-R6-plus-F4` carrying the three dependency classes (operational predecessor of G, not hard) | F4 records and mirrors |
| R-2 | plan §14 `:2100-2104` "F3 Apply Template re-layering … DISPOSITION PENDING DQ-F3-9" | F3 author design closeout `:87` (Q5, T0 approved), `:142` (`DQ-F3-9` "layers never touched"), `:169`; F3/F3-R1/F3-R2 closeout `:12-13`, `:185-186`, `:196-197` (inherited `applyTemplate` not used) | `MOOT — CLOSED UNDER T0` | additive line after `:2104` | `:2100-2104`; F3-PREP characterization |
| R-7 | plan §14 `:1779-1780` `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` "(preferred owner F2)" | F2 author decisions `:216-222` (`DQ-F2-9`: separate pending enhancement, not implemented in F2); F2-R1 decisions `:186`; F2/F2-R1 closeout mirror `:11` (not approved) | `ENHANCEMENT — IMPLEMENTATION NOT AUTHORIZED — NOT IMPLEMENTED IN F2 — NO OWNER` (owner per `DQ-G-6`) | qualify "(preferred owner F2 at the A-2 closeout; …; owner per DQ-G-6)" | A-2 closeout `:197`; POST-E2-P4 closeout `:89-90` |
| R-8 | plan §14 `:1898-1901` "(DQ-F4-6 pending)"; `:2004-2011` "(DQ-F4-8 pending)" | F4 author design closeout `:13`, `:40-41`; F4 aggregate closeout `:57-65`, `:67-71`, `:104-105` | `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` `CLOSED — RESOLVED FOR AUTOCAD 2026` (others `NOT RECHECKED — COVERAGE GAP`); `TD-PDF-1` `OPEN — DEFERRED` | two additive lines | F4-PREP lines |
| R-9a | plan §14 `:1919-1922` legacy dimension-macro name collision "PENDING AUTHOR REVIEW" | E2 author decisions `:38`, `:88-89` (`DQ-E2-7` naming resolved); E2 closeout approved the implementation but **does not name** the observation (`:102-137`) | author review completed with E2; "resolved" **needs author confirmation** | additive line "author review completed with E2; resolved pending author confirmation (DQ-G-8)" | `:1902-1922`; E2 candidate report |
| R-9b | plan §14 `:1812-1813` legacy-inventory case fix "publication pending" | POST-E3 reconciliation `:16`, §11 `:261-264`; `718c4081` ⊂ `00b525fd` ⊂ `main` | `RESOLVED — PUBLISHED` (2026-10-10) | additive line | candidate `28e9aebe` and report |
| R-9c | `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL`: only in the E3 author decisions `:381` "OPEN … NOT RESOLVED"; absent from plan §14 and the E3 closeout | approved code unchanged since `7cd50116`: identity-only link validated on every resolve and every Graphics 1 paint (`ExportAreaSession.java:32-38,234-272`; `GeoCeDGEuclidianView.java:80-82`); test `PreG9BR6PlusE3IsoABorderDesktopTest` scenario `link()` | **evidence supports resolution; author confirmation required**. No rebuild signal was added; by `DQ-E3-9` the design replaces the signal with identity validation. | register entry in the G structured register | E3 author decisions `:381`, mirror `:58` |
| R-9d | `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN`: E3 author decisions `:384` "OPEN … NOT RESOLVED" | `GeoCeDGActionRegistry.java:224-235,241-249`; `GeoCeDGIsoABorderTool.java:47-97`; test scenario `gate()` | **evidence supports resolution; author confirmation required** | register entry | E3 author decisions `:384`, mirror `:61` |
| R-10 | ~60 identifiers recorded only in candidate, preparation or closeout reports, never in the plan register | per-identifier sources in the [C proposal](../validation/pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md) | — | **do not back-fill the plan**; the G structured register holds one row per identifier | all source reports |
| **R-11** (new) | roadmap `:10` "publicación del cierre agregado pendiente de autorización separada"; plan §14 `:1659-1663` "aggregate publication pending separate authorization"; plan §14 `:1757` G readiness "publication not authorized" | §1.2 (`DQ-G-10`) | `19f8f206` and `abe12893` published | **applied here**: roadmap `:10` (historical clause kept, completion appended); plan §14 F4 block and G block (additive lines); plan §3 F4 row | F4 aggregate closeout `:12`, `:130-132` and mirror `:8`; readiness package and JSON (prepared at `19f8f206`); G prompt revision 1 at `abe12893` |
| **R-12** (new) | roadmap `:14` "Siguiente puerta": unchanged since the C closeout `39eb05bc` (2026-10-06); says no later subphase (E1–G) is authorized | phase table `:3392-3402` | all hard predecessors closed and published; next gate = the author's G implementation authorization on an exact base | qualify the old clause "en el cierre de `C` (2026-10-06)" and append the current next gate | earlier clauses of `:14` |
| **R-13** (new, optional) | roadmap `:9` "Estado actual": unchanged since R3-X1 `f96373ac` (2026-09-29); names no R4–R6-plus state | phase table `:3380-3403` | — | append one dated summary sentence pointing to the phase table; the author decides whether this is G scope | — |
| **R-14** (new; may be intentional) | plan header `:3` "NO SUBPHASE AUTHORIZED"; `:5`, §14 `:1239-1240`, roadmap `:3384` "PRODUCT IMPLEMENTATION NOT AUTHORIZED" (track level) | the per-subphase authorizations | "no blanket authorization" may be the intended meaning | **author decision**; for example "EACH SUBPHASE SEPARATELY AUTHORIZED (see §3 State column)" | — |
| **R-15** (new) | documentation-maintenance contract §9 `:147-149` ("G9A1 … not started") and §10 `:153-156` ("becomes normative only through the author's decision") | roadmap `:9` (G9A1–A3 approved); R5-A `PASS — AUTHOR APPROVED` on `4804f505` | status wording only, but in a normative operations specification | **author confirmation** before any edit; not inside the `DQ-G-9` list | — |

### 7.2 Documentary observations (`DQ-G-9`)

| Observation | Still applies at `abe12893`? | Exact minimal correction | Kind | Proposal |
|---|---|---|---|---|
| `OBS-F4P-S3-SPEC-HEADER` | yes: `geocedg/specs/ui/construction-text-view-scaling.md:4` `IMPLEMENTATION AUTHORIZED — CANDIDATE PENDING REVIEW`, while S3 is `PASS — AUTHOR APPROVED` (S3 closeout report `:1-10`; roadmap `:3129`) | `:4` → `NORMATIVE / AUTHOR APPROVED` (`PRE-G9B-S3` on `543c7729`, with a link to the closeout report) | status line of a specification; no contract text changes | correct in G after a one-line author confirmation; no pin found |
| `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | yes: `docs/adr/0035-physical-pdf-style-sizes.md:56-57` still says markers are `1.6 · pointSize` pt; the ADR's own Amendment `:64-84` and the specification `:60-78` supersede it | additive "superseded for point markers by the Amendment below" | **ADR body, not maintenance** | **retain** as a registered inconsistency (the Amendment prevails inside the same accepted ADR), unless the author separately authorizes the annotation |
| `OBS-R6P-DXF-DEFAULT-DOC` | yes: `docs/architecture/g9_extended_dxf_architecture.md:3-4` "default-off"; default on since PRE-G9B-P1 (`AppConfigGeoCeDG.java:37`, `e1672b22`) | rewrite `:3-4` keeping "default-off" as a historical qualifier ("default-off at the G9X1 closeout; enabled by default since PRE-G9B-P1") — `verify-g9x1-extended-dxf.ps1:834-839` asserts the token | architecture status line | correct in G |
| `OBS-R6P-DXF-SPEC-HEADER` | yes: `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md:7` "implementation not authorized" | `:7` → "implemented by G9X1 (`PASS — AUTHOR APPROVED`); enabled by default since PRE-G9B-P1" | metadata line. Also found: `:5` and `:181-187` (C v1.1 amendment and E3 producer paragraph) were never promoted, which is a **normative decision** outside maintenance. | correct `:7` in G; the `:5`/`:181-187` promotion is a separate author decision. The G9X1 phase verifier's spec pin already mismatches at `HEAD` (pre-existing since E3). |
| `OBS-R6P-DOC-IDENTITY-HEADER` | yes: `geocedg/specs/ui/native-document-identity.md:6-7,10` and `docs/adr/0016…:6-7` "implementation not authorized / not started"; `docs/adr/0029…:3-4` "Proposed" although PRE-G9B-R3 approved it (`pre_g9b_r3_closeout_record.md:3-5,62-84`) | header lines → "implementation `PASS — AUTHOR APPROVED` (G9U0-R2)"; ADR 0029 → "Accepted — AUTHOR APPROVED (PRE-G9B-R3, as amended by R3-R1; decision text unchanged)" | header lines; ADR 0029 status needs author confirmation | ADR 0029: correct after author confirmation. Specification and ADR 0016: **blocked** by the byte pin in `verify-g9u0-r2-product-refinement.ps1:40-45,451-462`, so retain or separately authorize a pin reconciliation |
| `OBS-R6P-ACTION-COUNT-DOC` | count **fixed** (`geocedg/specs/ui/cedg-workspaces.md:478-480`: 133 actions, equal to the test pins; commits `2a71133a`, `16547115`); residual stale labels at `:5-6`, `:478`, `:517-518`, `:525-526`, `:532-533` ("technical candidate pending author review" / `PROPOSED` for E3, F1, F3 and G9U1, all approved) | replace those status qualifiers | status qualifiers in a specification | correct the residuals in G; the count observation is closed by those commits |
| `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | yes: `docs/upstream/modified-files.yml:2601,2607,2613,2685,2691,2697` omit the G9S1 `SplineV2` lines (`de33f3a8`) | append one G9S1 sentence to each purpose; keep the `authority` field | purpose metadata in the upstream-modification register | correct in G; the author picks the G9S1 authority path cited |

None of these corrections changes a geometric specification, an ADR decision or a
scientific contract.

### 7.3 Revised documentation plan: four classes (revision 2)

The author authorized design preparation for minimal current-state corrections that
approved records support (2026-10-14). The items of §7.1 and §7.2 fall into four classes.
Where a revision-1 proposal in §7.1 or §7.2 differs, this classification prevails. For
example, the DXF specification `:7` header moves to class 4, because its file is pinned
by a historical verifier.

The perimeter is the same for every class:
- historical closeout records, accepted evidence, JSON mirrors and the readiness package
  are never edited;
- historical byte pins, old verification scripts, ADR decision bodies and normative
  scientific content are never modified under a documentation-maintenance pretext;
- an observation is never closed merely because it resembles a later implementation.

Corrections that are planned but not applied are applied by G's documentary closeout, each
traced to its superseding record. The publication registration (R-11) is the one class-1
correction already **applied** in revision 1, as the `DQ-G-10` record.

**Class 1 — confirmed stale status labels** (an approved record supports the
correction; within the documentation-maintenance perimeter):

| Item | Correction planned for G |
|---|---|
| R-1a E1-P / E1, R-1b E2, R-1c F1, R-1d E3 and POST-E3 sub-items | `— PUBLISHED` and "at the closeout" qualifiers in the plan §3 State column, the §14 register and roadmap `:3399` |
| R-1e F4 | plan row already completed (applied); one roadmap phase-table row for `PRE-G9B-R6-plus-F4`, carrying the three dependency classes |
| R-2 | "MOOT — CLOSED UNDER T0" line in plan §14 |
| R-7 | owner qualifier: "not implemented in F2 (DQ-F2-9); planning owner G11 (author, 2026-10-14)" |
| R-8 | `DQ-F4-6` and `DQ-F4-8` decided lines in plan §14 |
| R-9b | "PUBLISHED (2026-10-10, POST-E3-INTEGRATION 00b525fd)" line |
| R-11 | applied (roadmap `:10`; plan F4 and G blocks) |
| R-12 | roadmap "Siguiente puerta": "at the C closeout" qualifier, plus the current next gate |
| `OBS-R6P-DXF-DEFAULT-DOC` | `docs/architecture/g9_extended_dxf_architecture.md:3-4`, keeping the token "default-off" as history (the historical G9X1 script asserts the token; no byte pin) |
| `OBS-R6P-ACTION-COUNT-DOC` residuals | status qualifiers in `geocedg/specs/ui/cedg-workspaces.md:5-6,478,517-518,525-526,532-533`; the count is already correct |
| `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | one G9S1 sentence per `docs/upstream/modified-files.yml` purpose |
| `OBS-F4P-S3-SPEC-HEADER` | status line `geocedg/specs/ui/construction-text-view-scaling.md:4` → `NORMATIVE / AUTHOR APPROVED`. S3 is approved (S3 closeout report; roadmap `:3129`). Author confirmation is recommended because the line promotes a specification status. |
| ADR 0029 status line | `docs/adr/0029…:3-4` → "Accepted — AUTHOR APPROVED (PRE-G9B-R3, as amended by R3-R1; decision text unchanged)". This is the status line only, approved by `pre_g9b_r3_closeout_record.md:62-84`; author confirmation is recommended. |

**Class 2 — approved implementations whose old observations lack an explicit closeout
disposition.** The approved code evidence is preserved. The narrowest truthful status is
proposed below, and none of these items is closed by it.

| Item | Narrowest truthful status proposed |
|---|---|
| `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` | `NAMING RESOLVED BY DQ-E2-7; COEXISTENCE VALIDATED IN THE APPROVED E2 IMPLEMENTATION 3a724661 (Commands.java:155,158; PreG9BR6PlusE2NativeDimensionDesktopTest) — NO CLOSEOUT DISPOSITION RECORDED — AUTHOR DISPOSITION REQUIRED`. The future-collision failure mode stays with the user-library row (P-16). |
| `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | `ADDRESSED BY THE APPROVED E3 IMPLEMENTATION 7cd50116 (GeoCeDGActionRegistry unit gate; GeoCeDGIsoABorderTool.checkUnit; test unitGateBlocksCreationAndOffersDocumentUnits) — NO CLOSEOUT DISPOSITION RECORDED — AUTHOR DISPOSITION REQUIRED` |
| `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | `MITIGATED BY DESIGN IN THE APPROVED E3 IMPLEMENTATION 7cd50116 (identity-only link validated on every resolve and Graphics 1 paint; no rebuild signal added, DQ-E3-9; test explicitLinkLifecycleByIdentity) — NO CLOSEOUT DISPOSITION RECORDED — AUTHOR DISPOSITION REQUIRED` |
| `F4R-G2-VIEW-CLASS` | proposed author-reviewed disposition `CLOSED — NOT REALIZED`. Evidence: `GuiManagerGeoCeDG.java:113-121`; `PreG9BR6PlusF4AStyleBarThicknessTest` `:225,:234`; no exact-class dependency found in source. |
| `F4R-A1-REBUILD` | proposed `CLOSED — HELD BY TEST` (`theFieldIsAttachedAgainAfterLanguageAndFontChanges`, approved F4-A `6baa66a5`) |
| `F4R-INHERITED-FREEHEP` | proposed `CLOSED — PERIMETER VERIFIED AND REGISTERED` (F4-D perimeter re-check; `modified-files.yml` entry; `TD-PDF-2` and `TD-PDF-3` closed by the F4-D closeout) |
| `F4R-CLASSIC-PERIMETER` | proposed `CLOSED — HELD` (Classic assertions in the F4-A and F4-D test classes; `F4R-CLASSIC-LEAK` `CLOSED — HELD` by the F4-A closeout) |
| `F4R-READER-COMPAT` | proposed `CLOSED FOR THE CHECKED READERS (Ghostscript, Poppler, Xpdf)`; the xref repair remains `OBS-F4P-FREEHEP-XREF-SIZE` (open, owner G15 proposed) |
| `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | `CLOSED IN THE APPROVED A-2 CANDIDATE REPORT ONLY (persistent reopen) — NO CLOSEOUT DISPOSITION RECORDED — AUTHOR DISPOSITION REQUIRED` |
| Desktop test-heap pressure = `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION`; the minor E2 items marked "closeout silent" in C | `ADDRESSED BY THE APPROVED E2 IMPLEMENTATION 3a724661 — NO CLOSEOUT DISPOSITION RECORDED — AUTHOR DISPOSITION REQUIRED` (heap: monitored in G's INTEGRATION) |

**Class 3 — normative amendments requiring separate author approval.** These stay
explicitly pending, and G does not promote them under documentation maintenance:
- the `PRE-G9B-R6-plus-C` and `PRE-G9B-R6-plus-E3` amendments still marked "technical
  candidate pending author review" inside approved specifications:
  - `geocedg/specs/export/geometry-export-foundation.md:7,92`;
  - `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md:5,183`;
  - `geocedg/specs/units/unit-system.md:20,678,837`;

  (debt proposal P-18; E3 closeout `:168` says the pending C amendment "is not disposed
  of");
- R-15: the stale sentences of the documentation-maintenance contract §9 (`:147-149`) and
  §10 (`:153-156`). This is wording only, but it sits in a normative operations
  specification.

**Class 4 — ADR-body changes or verifier-pinned files requiring separate authorization.**
These are retained by default:
- `OBS-F4P-ADR0035-CONSEQUENCE-LINE`: **retained as a registered discrepancy** (author
  direction). The Amendment prevails inside the same accepted ADR.
- `OBS-R6P-DOC-IDENTITY-HEADER` for `geocedg/specs/ui/native-document-identity.md:6-7,10`
  and `docs/adr/0016…:6-7`. Both are byte-pinned by
  `tools/agent/verify-g9u0-r2-product-refinement.ps1:40-45,451-462`.
- `OBS-R6P-DXF-SPEC-HEADER` (`dxf-curve-fidelity-and-approximation.md:7`). The file is
  pinned by `tools/agent/verify-g9x1-extended-dxf.ps1:60-64`, and that pin already
  mismatches at `HEAD` since E3 (pre-existing).

**Author decisions on intent, not classed above:** R-13 (optional "Estado actual"
summary) and R-14 (track-level "NO SUBPHASE AUTHORIZED" wording).

## 8. D — Canonical prompt reconciliation

The prompt is updated **in place** as revision 2 of the candidate. One canonical path
avoids a duplicate prompt authority; revision 1 stays at `abe12893`. Its status stays
`PREPARED — PENDING AUTHOR REVIEW — NOT AUTHORIZED`. The changes:

1. **Implementation base.** It cites the published base `abe12893` (`DQ-G-10` resolved)
   and no longer says the F4 closeout and the package are unpublished.
2. **Acceptance plan.** It carries the `DQ-G-1` plan of §2: the check table, the
   conditional rule, the selection and the escalation triggers.
3. **About scope.** It references the contract candidate (PM-1 to PM-12) instead of
   restating it. The prompt carries no second copy of the contract. It names the
   version-transition commit and the date-designation rule.
4. **Characterization step.** It adds the `DQ-G-5` characterization as an authorized step
   *only if* the authorization grants it. No correction is included.
5. **Debt, help and labels.** It references the C proposal, the help matrix and the label
   reconciliation as inputs, with every disposition still pending.
6. **Stop conditions.** Added:
   - an undesignated `D` at freeze;
   - any product, build, packaging, test or verification path changed after `T`;
   - a `COMMERCIAL` record, artifact or authorized label;
   - a distribution state inferred from file names or paths;
   - a global verification-infrastructure change without escalation.

`Test-PromptContractDocument` (profile `task`) was run on revision 2.

**Revision 3 (this documentary revision).** The prompt becomes internally coherent with
the author directions and is ready for final author review. It is **not** labelled
authorized for implementation. Changes from revision 2:
1. **Fixed values.** `1.1.0` / `2026-10-15` and the author-designated date semantics. The
   version-transition commit is defined as the single atomic commit of contract PM-3,
   with no version date for `1.0.0`.
2. **Metadata.** The accepted directions for PM-1 to PM-12, including the
   `package_profile_sha256` role and the consistency-is-not-authenticity clause. Normative
   transcription happens only after the author approves the final contract revision.
3. **Verification.** `DQ-G-1` is accepted as the class to freeze, with COMMERCIAL refusal
   evidence. Equivalent leaf coverage is kept distinct from a single receipt-bound
   `FINAL`.
4. **Characterization.** The 27 `DQ-G-5` scenarios are planned G scope, covering both
   observations. A demonstrated contract violation that needs code stops that branch.
5. **Debt register.** The 134-entry framework and the author's planning owners; no silent
   P/O → R/L promotion.
6. **Help.** The minimal help package with its required coverage.
7. **Documentation.** The four-class documentation plan of §7.3.
8. **Stop conditions.** Updated: a version or date other than `1.1.0` / `2026-10-15`; a
   version date for `1.0.0`; a class 3 or 4 change made as maintenance.

`Test-PromptContractDocument` result on revision 3: §10.

## 9. E — Implementation readiness assessment

| Question | Assessment |
|---|---|
| Is the proposed scope internally consistent? | **Yes**, with the three refinements of the executive assessment. No scope item needs a kernel, serialization or inherited change. The About, packaging-record and version changes are GeoCeDG-owned Desktop, build and packaging work. Help is documentation and profile text. |
| Are all mandatory preconditions known? | **Yes**: the hard predecessors (§1.3), the About requirement, the base, the class proposal, and the global-closeout dispositions (`DQ-G-5`, `DQ-G-6`). |
| Decisions required **before implementation** (revision 2) | The design directions of `DQ-G-1` to `DQ-G-7` are accepted. Still required: (1) approval of the final metadata contract revision, including the digest role and the package-profile schema `2`; (2) the G implementation authorization of prompt revision 3. That authorization names the exact base (`abe12893`, or an approved and published documentary successor), freezes `INTEGRATED_PHASE` and selects `CLOSEOUT_MODE`. (3) The selection of the optional help items. |
| Decisions required **before the candidate freeze** | none. `1.1.0` / `2026-10-15` is designated. |
| Decisions required **before the global closeout** | the dispositions of `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` on the characterization evidence; the per-item `DQ-G-6` dispositions; the class 2 dispositions (§7.3); the class 1 corrections applied. Class 3 and 4 items may stay explicitly pending or retained. |
| Issues that may be handled **during authorized G characterization** | the `DQ-G-5` scenarios (§5), with `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` alongside; confirmation of the `OBS-R6PLUS-E3-*` closure evidence and of `OBS-R6P-SAVE-WARNING` at the base; recurrence of R-6 during INTEGRATION |
| Exact future implementation base | `abe12893b74f574c248117fbe873633a449e23e0`, tree `ba4298c5658bf3f897455db5a49a76fea7105e72`, unless superseded by a named, approved and published successor |
| Proposed verification plan | `DQ-G-1` items 1–10 |
| Remaining stop conditions | listed in the reconciled prompt. Main ones: a missing hard predecessor or base divergence; a kernel, serialization or inherited change needed; a global verification-infrastructure change without escalation; a licensing fact not sourceable; a version or date other than `1.1.0` / `2026-10-15`; a product, build or packaging change after `T`; a characterized contract violation that needs code; a `COMMERCIAL` artifact or label; incomplete or untrusted coverage; the `DQ-G-5` or tool-replacement disposition missing at the global closeout |

## 10. Documentary candidate and validation

The documentary candidate holds only these files:

| Path | Change |
|---|---|
| `docs/architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md` | new (this record) |
| `docs/architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md` | new (B) |
| `docs/validation/pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md` | new (C) |
| `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-author-decision-reconciliation.json` | new (mirror) |
| `.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md` | revision 2 of the candidate (D) |
| `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` | status entries: header bullet, G row, §14 G block; `DQ-G-10` registration in the F4 row and the §14 F4 block |
| `docs/roadmap/geocedg_roadmap.md` | version 5.52; "Última fase cerrada" publication completion; "Última fase ejecutada"; G row |

Revision 2 (a documentary commit on the same lineage, child of `ccb4ef34`):

| Path | Change |
|---|---|
| `docs/validation/pre_g9b_r6_plus_g_author_design_decision_record.md` | new: the author design decision record |
| `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-author-design-decisions.json` | new: its mirror |
| this record | revision 2 (§0, the summary table, §7.3, §8, §9, §10, §11) |
| the contract candidate | revision 2 (PM-3, PM-5, PM-6, PM-11, §5 to §7, §9) |
| the debt proposal | revision 2 section (framework and planning owners; no silent promotion) |
| the canonical G prompt | revision 3 |
| `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-author-decision-reconciliation.json` | revision 2 fields |
| roadmap, plan | status entries for this revision (roadmap 5.53) |

The checks are the same as for revision 1, run again on revision 2. The official STATIC
result of the exact revision-2 commit is reported outside the candidate.

Untouched:
- product source, tests, kernel, serialization, build logic and packaging configuration;
- executable verification infrastructure;
- approved specifications and ADRs;
- closeout records and accepted evidence.

The readiness package and its mirror stay as prepared. Line endings are preserved byte
for byte: the plan is CRLF in the working tree, and the roadmap keeps its mixed
terminators. New files are LF.

Checks run on the working tree before the commit:
- `git diff --check`: clean, with the new files added as intent-to-add;
- every repository-relative Markdown link in the new and changed G documents resolves;
- both new JSON documents parse;
- `Test-PromptContractDocument`, profile `task`, on the revision-2 prompt: no missing,
  duplicate, unknown or out-of-order fields; one title; `execution_safe = true`.

The official `verify.ps1 -Profile STATIC` runs on the clean committed candidate. A
candidate cannot contain its own commit identity, so its identity, result and log are
reported to the author outside the candidate.
`GUIDE_IMPACT = NONE` (`GUIDE_JUSTIFICATION`: status and design-preparation documents
only; no observable or developer-facing contract changed). Bootstrap impact: no change
required (no workstation prerequisite, toolchain or environment contract changed).

## 11. Authorization state

```text
G AUTHOR DECISION RECONCILIATION   = REVISION 2 — PREPARED
G VERSION TARGET                   = 1.1.0
G VERSION_DATE                     = 2026-10-15 — AUTHOR DESIGNATED
G METADATA DESIGN                  = RECONCILED — PENDING FINAL AUTHOR REVIEW
G METADATA CONTRACT                = CANDIDATE REVISION 2 — PREPARED FOR AUTHOR-APPROVED
                                     NORMATIVE TRANSCRIPTION — NOT NORMATIVE
G DEBT DISPOSITION PLAN            = FRAMEWORK AND PLANNING OWNERS ADOPTED — PER-ITEM
                                     DISPOSITIONS PENDING
G CANONICAL PROMPT                 = REVISION 3 CANDIDATE — NOT AUTHORIZED
G IMPLEMENTATION                   = NOT AUTHORIZED
G TECHNICAL CANDIDATE              = NOT CREATED
PRE-G9B-R6-plus GLOBAL CLOSEOUT    = NOT AUTHORIZED
PRE-G9B-R7                         = NOT AUTHORIZED
G9B                                = NOT AUTHORIZED
PUBLIC RELEASE                     = NOT AUTHORIZED
VISIBLE VERSION                    = 1.0.0 (unchanged)
DQ-G-10                            = PUBLICATION COMPLETED — EXACT BASE ESTABLISHED (abe12893)
selfApproved                       = false
```

A successful STATIC run on this documentary candidate is technical evidence about the
documents only. It implies no author approval of any decision above.

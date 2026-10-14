# PRE-G9B-R6-plus-G — author design decision record

```text
PRE-G9B-R6-plus-G AUTHOR DESIGN DIRECTIONS = RECORDED (author instruction of 2026-10-14)
G VERSION TARGET                = 1.1.0
G VERSION_DATE                  = 2026-10-15 — AUTHOR DESIGNATED
G METADATA DESIGN               = RECONCILED — PENDING FINAL AUTHOR REVIEW
G CANONICAL PROMPT              = REVISION 3 CANDIDATE — NOT AUTHORIZED
G IMPLEMENTATION                = NOT AUTHORIZED
G TECHNICAL CANDIDATE           = NOT CREATED
G GLOBAL CLOSEOUT               = NOT AUTHORIZED
PRE-G9B-R7                      = NOT AUTHORIZED
G9B                             = NOT AUTHORIZED
PUBLIC RELEASE                  = NOT AUTHORIZED
DQ-G-10                         = PUBLICATION COMPLETED — EXACT BASE ESTABLISHED
selfApproved                    = false
```

| Field | Value |
|---|---|
| Author instruction | "GeoCeDG — PRE-G9B-R6-plus-G FINAL AUTHOR DESIGN RECONCILIATION" (2026-10-14). It resolves the remaining architectural questions of the prepared G design and authorizes its documentary reconciliation. **It does not authorize G product implementation.** |
| Entry identity | published base `abe12893b74f574c248117fbe873633a449e23e0` (tree `ba4298c5658bf3f897455db5a49a76fea7105e72`, equal to the live remote `main`); previous local documentary candidate `ccb4ef34dde4736030cc1a600cd7b84624f9ab56` (tree `04882e1d042282442cc6977efa2ea07398b0340a`, parent `abe12893`). Both were verified before this revision, with a clean worktree. The reconciliation branch continues from them. |
| Inputs | the [author decision reconciliation](../architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md); the [product metadata contract candidate](../architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md); the [debt and observation disposition proposal](pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md); the [canonical G prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md) |
| Mirror | [`pre-g9b-r6-plus-g-author-design-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-author-design-decisions.json) |

This record transcribes the directions the author gave. It does not extend them, and it
records no technical approval. A **direction** fixes the design that G must implement
once it is authorized. A **remaining decision** is still open.

## 1. Accepted directions

| Topic | Author direction (2026-10-14) | Where reconciled |
|---|---|---|
| Version target and date | `1.1.0`; canonical version date `2026-10-15`, author-designated. The date is not automatically the date of technical approval, publication, build or public release. | contract PM-3, §6 |
| PM-1, PM-2 | `packaging/windows/package.yml` stays the single authority for product name and canonical version. No second independently editable version source. | contract PM-1, PM-2 |
| PM-3 semantics | the version date is a calendar date explicitly designated by the author for a product version, adopted when the author approves that exact verified version. It is distinct from the actual approval date and from the build timestamp, commit date, publication date and release date. | contract PM-3 |
| PM-3, `1.0.0` | No date is inferred for `1.0.0`, and none is approved retroactively. The published `1.0.0` stays historically valid without a version-date field. The `2026-09-16` proposal is withdrawn. | contract PM-3, §6.3 L0–L1 |
| PM-3 migration | the required field is introduced by the G version-transition implementation, with a coherent schema migration. The final verified candidate requires and validates the field. | contract PM-3 (one atomic version-transition commit; package-profile `schema_version` `2`) |
| PM-4 | build identity comes from repository provenance. No wall-clock build timestamp, and no confusion with the version date. | contract PM-4 |
| PM-5 | the installed record `app/geocedg-distribution.json`, derived from the selected authoritative package profile, with four runtime states (INTERNAL, NC, DEVELOPMENT, UNVERIFIED). Development and unverifiable installations get a conservative, non-redistributable state. COMMERCIAL stays unauthorized. The role of `package_profile_sha256` is clarified, and consistency checks are not cryptographic authenticity or a licensing authority. `desktop.jar` stays profile-neutral. No signed metadata, online validation, DRM or new security infrastructure. | contract PM-5 (digest runtime-checked against the build-embedded canonical digest; audit-only as the alternative) |
| PM-6 to PM-12 | accepted: licensing summaries and authority links; GeoCeDG-owned About; window and guide titles; Classic preservation; determinism; verification obligations; document-format compatibility. The composite-product terms stay distinct from component permissions. The About never claims the complete binary is licensed solely under EUPL-1.2. | contract PM-6 to PM-12 |
| `DQ-G-4` | M1, "approve exactly what was verified". The final G technical candidate carries `1.1.0` / `2026-10-15` before author approval, as an unpublished, unapproved candidate. Version and date become authoritative by the author's approval of the exact technical commit. No productive bytes are modified after acceptance. A changed version or date requires a new technical candidate and its own verification. Publication and release are independent. | contract §6.3 (L0–L7) |
| `DQ-G-1` | `INTEGRATED_PHASE` with PHASE `PRE-G9B-R6-PLUS-G`, INTEGRATION, INFRA_UNIT, STATIC, PACKAGING for INTERNAL and NC, and evidence of the COMMERCIAL refusal. Only the current executable verifier, registry and schemas are acceptance authorities. Equivalent leaf coverage stays distinct from a single receipt-bound FINAL campaign. FINAL only by explicit escalation (a global verification-infrastructure change) or by a separate author decision requesting a unified milestone receipt. | prompt revision 3 |
| `DQ-G-5` | the 27 bounded characterization scenarios are part of the planned G scope. They cover `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`. Document replacement is distinguished from insertion, merging, tool replacement and other session transitions. No productive correction. Neither observation is closed before its evidence and an explicit author disposition. A demonstrated contract violation that needs code stops that branch and requests authorization. | reconciliation §5; prompt revision 3 |
| `DQ-G-6` | the 134-entry inventory and its framework are adopted, with no blanket approval of per-item dispositions. Previously approved closures and limitations are preserved. Planning owners: `TD-PDF-1` → G15, after a physical-presentation contract-design activity; direct working-layer entry → G11; manual `ExportArea` interaction → G15 (preferred); physical zoom aid → G12; deferred F3 extensions → a future author-named insertion and styles activity; full-suite instability → G monitoring and `PRE-G9B-R7` follow-up. These are planning ownership decisions, not implementation authorizations or scope changes. Other pending items keep an explicit author-disposition requirement; P and O are never silently promoted to R or L. Both document-replacement observations keep their mandatory pre-global-closeout disposition. | debt proposal "Revision 2" |
| `DQ-G-7` | the minimal EN/ES help package, with no new help framework or UI window. Coverage: PDF text as shapes or embedded fonts; physical PDF stroke widths; the curated tool library and its limitations; the numeric thickness editor; accepted user-visible limitations; product metadata and About. The guide-outline and bilingual-structure contracts are respected. | reconciliation §6.2; prompt revision 3 |
| `DQ-G-8`, `DQ-G-9` | design preparation is authorized for minimal current-state corrections supported by approved records. Historical closeout records and accepted evidence are preserved. The documentation plan separates four classes: confirmed stale labels; approved implementations whose old observations lack a closeout disposition; normative amendments needing separate approval; ADR-body or verifier-pinned changes needing separate authorization. `OBS-F4P-ADR0035-CONSEQUENCE-LINE` is retained as a registered discrepancy. No historical byte pins, old verification scripts, ADR decision bodies or normative scientific content are modified under a documentation-maintenance pretext. | reconciliation §7.3 |
| `DQ-G-10` | `PUBLICATION COMPLETED — EXACT BASE ESTABLISHED`. The published base remains `abe12893`. A newer documentary commit becomes the implementation base only after explicit author approval and controlled publication. | reconciliation §1.2 |

## 2. Remaining decisions requiring author approval

| # | Decision | Needed before |
|---|---|---|
| 1 | Approve the **final revision of the product metadata contract** for normative transcription. This includes the digest role (runtime-checked, as recommended, or audit-only) and the package-profile `schema_version` `2` migration. | G implementation authorization |
| 2 | Authorize the **canonical G prompt revision 3** for implementation, naming the exact base: `abe12893`, or an approved and published documentary successor. Freeze `INTEGRATED_PHASE` with the acceptance plan of the prompt, and select `CLOSEOUT_MODE` (proposed `AUTHOR_OPERATED`). | G implementation |
| 3 | Approve and, separately, publish the documentary reconciliation commits, if they are to become the implementation base | G implementation authorization (optional) |
| 4 | Select the optional editorial help items (reconciliation §6.2) | G implementation |
| 5 | Dispose of `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` on the characterization evidence | global closeout |
| 6 | Decide each still-pending row of the debt proposal: B and P dispositions; owners of the O rows not covered by §1; the `PROPOSED` re-categorizations | global closeout (owners by `PRE-G9B-R7` at the latest) |
| 7 | **Class 2** (reconciliation §7.3): the dispositions of approved implementations whose observations lack an explicit closeout disposition. These are the E2 naming collision; the E3 unit gate and `ExportArea` link; the five F4 reconciliation risks; and the other "closeout silent" rows. | global closeout |
| 8 | **Class 3**: the normative amendments whose promotion was never decided: the C/E3 amendments in `geometry-export-foundation.md`, `dxf-curve-fidelity-and-approximation.md` and `unit-system.md`; the wording of the documentation-maintenance contract §9/§10. Each needs separate author approval. | whenever the author chooses; they stay explicitly pending |
| 9 | **Class 4**: authorize, or keep retained, the ADR 0035 consequence-line annotation, and the header edits of the files pinned by historical phase verifiers. Those are the native-document-identity specification, ADR 0016 and the DXF fidelity specification. | whenever the author chooses; retained by default |
| 10 | The plan's track-level label (R-14) and the optional "Estado actual" summary (R-13) | G documentary closeout |

## 3. Boundaries

- No publication, push, merge, tag, release or redistribution.
- No G implementation; no G technical candidate.
- No G acceptance campaign: only the STATIC check of this documentary candidate.
- No product, build, packaging or executable-verifier change.
- No normative promotion: the metadata contract stays a candidate until item 1.

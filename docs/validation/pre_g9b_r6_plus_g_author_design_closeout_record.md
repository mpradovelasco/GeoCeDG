# PRE-G9B-R6-plus-G — author design closeout record

```text
G DESIGN                        = AUTHOR APPROVED
G METADATA CONTRACT (PM-1..12)  = AUTHOR APPROVED — DESIGN (normative transcription in G)
G CANONICAL PROMPT              = DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED
G VERSION TARGET                = 1.1.0
G VERSION_DATE                  = 2026-10-15 — AUTHOR DESIGNATED
VERIFICATION_CLASS (proposed)   = INTEGRATED_PHASE — to be frozen by the implementation authorization
CLOSEOUT_MODE                   = AUTHOR_OPERATED
G IMPLEMENTATION                = NOT AUTHORIZED
G TECHNICAL CANDIDATE           = NOT CREATED
G GLOBAL CLOSEOUT               = NOT AUTHORIZED
PRE-G9B-R7                      = NOT AUTHORIZED
G9B                             = NOT AUTHORIZED
PUBLIC RELEASE                  = NOT AUTHORIZED
PUBLICATION OF THIS CLOSEOUT    = NOT AUTHORIZED (separate author decision)
selfApproved                    = false
```

## 1. Author decision and scope

| Field | Value |
|---|---|
| Instruction | "GeoCeDG — PRE-G9B-R6-plus-G FINAL AUTHOR DESIGN APPROVAL AND DOCUMENTARY CLOSEOUT" (2026-10-14) |
| Decision | The author approves the final G design directions represented by the documentary candidate below, subject to its identity, content and verification checks (§2). The approval covers G **design** and this documentary closeout only. |
| Not authorized | G implementation, product changes, publication, release, any subsequent phase |
| Mirror | [`pre-g9b-r6-plus-g-author-design-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-author-design-closeout.json) |
| Class of this closeout | `DOCUMENTATION_STATUS_ONLY`; acceptance `STATIC` on the exact closeout commit, reported outside it |

## 2. Approved candidate: identity and authentication

| Item | Value | Check (2026-10-14) |
|---|---|---|
| repository | `mpradovelasco/GeoCeDG` | — |
| published base | `abe12893b74f574c248117fbe873633a449e23e0`, tree `ba4298c5658bf3f897455db5a49a76fea7105e72` | equal to the live remote `main` (`git ls-remote`) |
| prepared reconciliation | `ccb4ef34dde4736030cc1a600cd7b84624f9ab56`, tree `04882e1d042282442cc6977efa2ea07398b0340a`, parent `abe12893` | linear |
| **final documentary candidate** | **`caf740a1c0c5633abf535dc2dd8279fdabc6611d`**, tree **`93699b2323747aa3ca270f3fbb4accd8172de353`**, parent `ccb4ef34` | exact; worktree clean |
| STATIC receipt of the candidate | `verification-af736ce5110941d0a90ca8a5b268bac7` | re-authenticated with `Read-VerificationJson` and `Get-VerificationResultHash`. `run_state` `COMPLETED`; candidate `caf740a1`/`93699b23`; `ACCEPTED`/`COMPLETE`; 3/3 acceptance; plan `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`; recorded and recomputed `result_hash` both `0755825a0a7d7783813a0dda6ee168e5111e95370f0d8ffffac89f378153bc8d`; file SHA-256 `f39186de67616652fbc56b2210b3e8fd14cc9d8987be7ac7fa9194a3bd9e6936`; standing diagnostics only (governance `FINDING`, historical-consistency `UNAVAILABLE`) |
| material content | the candidate holds what its report claimed: decision record; contract revision 2; debt proposal revision 2; prompt revision 3; mirrors; status entries; documentation paths only since `abe12893` | confirmed |

The author asked for **three documentary clarifications** before recording this closeout.
They are applied in the closeout commit, and they do not extend the implementation scope:

1. **PM-5 record.** Two illustrative records, one per packageable profile. `profile_id`
   is the actual selected profile. `redistributable` is the declared boolean: `false` for
   INTERNAL, `true` for NC. No `"INTERNAL | NC"` value.
2. **§6.3 table.** The duplicated header of the lifecycle table is removed. Stages L0–L7
   and `2026-10-15` are unchanged.
3. **§9 normative ownership.** PM-1 to PM-12 are transcribed into the existing approved
   Windows packaging and application-profile specifications during the authorized G
   implementation. The PM design record stays the historical design authority, not a
   competing normative specification.

## 3. Decisions DQ-G-1 to DQ-G-10

| Id | Author decision (2026-10-14) | Remaining disposition |
|---|---|---|
| `DQ-G-1` | **Approved:** `INTEGRATED_PHASE`, to be frozen explicitly by the implementation authorization. Acceptance plan: PHASE `PRE-G9B-R6-PLUS-G`; INTEGRATION; INFRA_UNIT; STATIC; PACKAGING INTERNAL and NC built from the exact frozen candidate; COMMERCIAL refusal evidence; focused compatibility and regression tests; the author smoke after technical verification. `CLOSEOUT_MODE = AUTHOR_OPERATED`. FINAL is not automatically required: a global verification-infrastructure impact needs an explicit escalation, and the author may separately elect a unified FINAL milestone campaign. | freezing in the implementation authorization |
| `DQ-G-2` | **Approved:** `packaging/windows/package.yml` stays the single product-version authority; `application.version_date` is added; package-profile schema version 2 arrives with the version transition; deterministic extraction and validation; no second independent version authority | — |
| `DQ-G-3` | **Approved:** the GeoCeDG-owned installed distribution record and the four runtime states INTERNAL, NC, DEVELOPMENT, UNVERIFIED. The main JAR stays profile-neutral. The **runtime-checked `package_profile_sha256`** is selected: documented deterministic canonicalization (UTF-8, `CRLF`/`CR` → `LF`, SHA-256), with a real comparison against the digest embedded in the build provenance. Consistency verification is distinct from cryptographic authenticity. COMMERCIAL stays unauthorized. Classic and the component-specific licensing authorities are preserved. | — |
| `DQ-G-4` | **Approved:** M1, `VERSION = 1.1.0`, `VERSION_DATE = 2026-10-15`, author-designated and not inferred from approval, build, commit, publication or release time. No retrospective date for `1.0.0`. Preliminary G commits keep `1.0.0`. The version-transition commit introduces the new required metadata together with compatible readers, generators, tests and packaging contracts. The frozen candidate contains `1.1.0 / 2026-10-15` and stays unapproved until the author accepts its exact SHA. No productive mutation after technical acceptance. | adoption of the date by the author's approval of the exact technical candidate |
| `DQ-G-5` | **Approved:** the planned characterization of the 27 bounded scenarios (SC-01 to SC-27) covering `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`. It may be part of the later authorized G implementation task. No corrective product change is approved. | both observations keep their mandatory author disposition before the global R6-plus closeout |
| `DQ-G-6` | **Approved:** the structured debt-register framework (134 entries) and the planning owners already specified (`TD-PDF-1` → G15 after a contract-design activity; direct working-layer entry → G11; manual `ExportArea` interaction → G15; physical zoom aid → G12; `DEFER-F3-*` → a future author-named insertion and styles activity; full-suite instability → G monitoring and `PRE-G9B-R7` follow-up). **Not** an approval of every proposed per-item disposition. | the remaining per-item dispositions (§4.3) |
| `DQ-G-7` | **Approved:** the minimal complete bilingual EN/ES help plan, including the documented PDF behaviours, the curated tool library, the compact numeric thickness field and the relevant known limitations. Small editorial terminology and cross-reference corrections are permitted if they change no guide structure, normative meaning or product behaviour. No new help framework; no modification of Classic-shared help implementations. | — |
| `DQ-G-8` | **Approved:** G reconciles the confirmed stale status labels (class 1), including the published status of F4 and the earlier subphases. The track-level wording "NO SUBPHASE AUTHORIZED" means **absence of blanket authorization**, not denial of the individually approved and completed subphases (R-14). Historical closeout records are not rewritten. | class-1 corrections are applied by G; R-13 remains optional |
| `DQ-G-9` | **Approved:** verifier-pinned headers, the ADR 0035 body wording and the undecided C/E3 normative amendments are **retained** unless separately authorized | classes 3 and 4 stay pending or retained |
| `DQ-G-10` | **Resolved earlier:** `PUBLICATION COMPLETED — EXACT BASE ESTABLISHED` (`abe12893`) | — |

## 4. Debt and observation dispositions recorded by this decision

### 4.1 Recorded as resolved (author disposition, 2026-10-14)

Each resolution is limited to the tested and approved scope it cites.

| Item | Author disposition | Exact supporting evidence |
|---|---|---|
| `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` (naming and coexistence) | **RESOLVED — AUTHOR DISPOSITION** | naming by `DQ-E2-7` (E2 author decisions `:38`, `:88-89`). Coexistence in the approved E2 implementation `3a724661`: `Commands.java:155,158` registers `AlignedDimension` and `LinearDimension`, with no `DirectDimension` or `AxisDimension`; `PreG9BR6PlusE2NativeDimensionDesktopTest` (`:163`, `:1212-1216`). **Preserved as independent and open:** `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION` (debt row P-16) and the whole-library failure mode on a future native collision. |
| `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | **RESOLVED BY THE IDENTITY-VALIDITY SOLUTION — AUTHOR DISPOSITION.** **No rebuild signal was added.** By design (`DQ-E3-9`), the `ISO_A_BORDER` link is validated by object identity on every resolve and every Graphics 1 paint, and a stale link is dropped and reported. | approved E3/E3-R1 `7cd50116`: `ExportAreaSession.java:32-38,227-272`; `GeoCeDGEuclidianView.java:80-82`; test `PreG9BR6PlusE3IsoABorderDesktopTest.explicitLinkLifecycleByIdentity` (`:117-120`; scenario `link()`); normative `iso-a-border.md` §11.3 |
| `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | **RESOLVED — AUTHOR DISPOSITION** | `GeoCeDGActionRegistry.java:224-235,241-249`; `GeoCeDGIsoABorderTool.java:47-97`; test `unitGateBlocksCreationAndOffersDocumentUnits` (`:102-105`; scenario `gate()`); normative `iso-a-border.md` §12.1 |
| `F4R-G2-VIEW-CLASS` | **CLOSED — NOT REALIZED**, within the approved F4-A scope | `GuiManagerGeoCeDG.java:113-121`; `PreG9BR6PlusF4AStyleBarThicknessTest` `:225,:234`; no exact-class dependency in source |
| `F4R-A1-REBUILD` | **CLOSED — HELD BY TEST** | `GeoCeDGEuclidianStyleBar.java:62-63`; `theFieldIsAttachedAgainAfterLanguageAndFontChanges`; approved F4-A `6baa66a5` |
| `F4R-INHERITED-FREEHEP` | **CLOSED — PERIMETER VERIFIED AND REGISTERED** | the F4-D perimeter re-check; the `docs/upstream/modified-files.yml` entries; `TD-PDF-2` and `TD-PDF-3` closed by the F4-D closeout (`6214d15a`) |
| `F4R-CLASSIC-PERIMETER` | **CLOSED — HELD**, within the tested scope | the Classic assertions of the F4-A and F4-D test classes; `F4R-CLASSIC-LEAK` `CLOSED — HELD` (F4-A closeout) |
| `F4R-READER-COMPAT` | **CLOSED FOR THE CHECKED READERS** (Ghostscript, Poppler, Xpdf) | F4-D candidate report reader evidence; F4-D `AUTHOR_SMOKE = PASS`. The xref repair stays the independent open `OBS-F4P-FREEHEP-XREF-SIZE` |

### 4.2 Kept open or preserved

- `OBS-R6P-SAVE-WARNING` stays **open**, pending its own contractual or implementation
  disposition. The specification requires a save warning, but
  `GeoCeDGExternalCompatibilityWarning.confirmSave` has no production caller.
- `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` stay open. Their mandatory disposition comes
  before the global closeout.
- All accepted limitations, the previously approved closures, and the independent open
  observations are preserved as recorded.

### 4.3 Still pending author disposition

- The other "closeout silent" rows:
  - `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`;
  - Desktop test-heap pressure / `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION`;
  - the minor E2 items.
- The `PROPOSED` re-categorizations of the debt proposal.
- The B and P dispositions.
- The owners of the O rows not covered by the planning owners. They are needed by
  `PRE-G9B-R7` at the latest.

## 5. Documentary authority after this closeout

| Document | State |
|---|---|
| [Product metadata contract](../architecture/pre_g9b_r6_plus_g_product_metadata_contract_candidate.md) | **AUTHOR APPROVED — DESIGN** (revision 2 plus the three clarifications) |
| [Canonical G prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md) | **G CANONICAL PROMPT — DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED** |
| [Author design decision record](pre_g9b_r6_plus_g_author_design_decision_record.md) | historical record of the directions; this closeout supersedes its "remaining decisions" where decided here |
| [Reconciliation](../architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md), [debt proposal](pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md) | prepared evidence; current decisions are those of this record |

## 6. Conditions for the separate G implementation authorization

1. **Base.** The closeout commit of this record, or another named base, approved by the
   author and **published** under control. The next implementation session starts only
   from an explicitly named, approved and published base.
2. **Authorization text.** An explicit G implementation authorization that:
   - names the exact base commit and tree;
   - freezes `INTEGRATED_PHASE` with the §3 acceptance plan and
     `CLOSEOUT_MODE = AUTHOR_OPERATED`;
   - authorizes the canonical prompt for implementation;
   - states whether the `DQ-G-5` characterization runs in the same task;
   - selects the optional editorial help items, if any.
3. **Escalation.** A global verification-infrastructure change needs an explicit
   escalation to FINAL. Alternatively, the author elects a unified FINAL milestone
   campaign.
4. **Before the global closeout:**
   - the dispositions of both document-replacement observations;
   - the per-item debt dispositions of §4.3;
   - the class-1 label corrections applied by G.

No product, build, packaging, test or executable-verifier file changes in this
documentary closeout. This record originates no publication and no implementation.

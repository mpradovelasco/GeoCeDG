# PRE-G9B-R6-plus-G — author closeout record and global PRE-G9B-R6-plus closeout

```text
PRE-G9B-R6-plus-G                    = PASS — AUTHOR APPROVED
PRE-G9B-R6-plus (mini-track)         = PASS — AUTHOR APPROVED
G-R1 TECHNICAL CANDIDATE             = PASS — AUTHOR APPROVED
                                       da1a09528a6f97254734400b12256e8fb20a6216
                                       tree 6e02685bf2877536ee02f98866352e46ef1ec49e
G AUTHOR SMOKE                       = PASS
VERSION                              = 1.1.0
VERSION_DATE                         = 2026-10-15 — AUTHOR DESIGNATED AND ADOPTED
AUTHOR APPROVAL DATE                 = 2026-10-15 (date of the author instruction; recorded
                                       separately from the version date)
G ORIGINAL CANDIDATE T               = VERIFIED — SUPERSEDED (historical; dd3937cf)
D-1                                  = ACCEPTED RECOVERY — AUTHOR DISPOSITION
G-R1 OPEN-ROUTE CORRECTION           = AUTHOR APPROVED (bounded, §3.2)
OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET
                                     = OPEN ROUTES CORRECTED — UNCONTRACTED RESIDUE OPEN —
                                       RETAINED WITH OWNER — NON-BLOCKING
OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT
                                     = DEFERRED — RETAINED WITH OWNER G11
STRUCTURED DEBT REGISTER             = 134 entries preserved; 25 gate rows disposed;
                                       0 awaiting a global-closeout decision
G11 / G12 / G15                      = PENDING — ROADMAP RECONCILIATION PREPARED
ENH-B-MANUAL-EXPORT-AREA-DRAG        = OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15
PRE-G9B-R7                           = DESIGNED — NOT AUTHORIZED (entry statement §9)
G9B / G9C / G9U2 / G10               = NOT AUTHORIZED
PUBLICATION                          = NOT AUTHORIZED (separate exact-SHA authorization, §10)
PUBLIC RELEASE / REDISTRIBUTION      = NOT AUTHORIZED
selfApproved                         = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-R6-plus-G Final Author
Approval, Global R6-plus Closeout and G11/G12/G15 Roadmap Reconciliation" (2026-10-15). It
records the author's approval of the exact G-R1 technical candidate, the author's debt and
observation dispositions, the closeout of `PRE-G9B-R6-plus-G` and of the
`PRE-G9B-R6-plus` mini-track, and the preparation of the PRE-G9B-R7 entry. It is a
documentary closeout descending from T': it changes no product, build, packaging, test or
executable-verification input, and the G acceptance campaign is not rerun. Machine-readable
mirror: [`pre-g9b-r6-plus-g-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-closeout.json).

## 1. Author decision

| Field | Value |
|---|---|
| Instruction | "GeoCeDG — PRE-G9B-R6-plus-G Final Author Approval, Global R6-plus Closeout and G11/G12/G15 Roadmap Reconciliation" (2026-10-15) |
| Author smoke | `AUTHOR SMOKE = PASS` on T' (author statement) |
| Decision | approval of the exact T', subject to the identity and acceptance authentication of §2; the approval covers the exact technical candidate and its tested scope |
| Not authorized | modification of productive bytes after T'; new product implementation; G11, G12 or G15 implementation; PRE-G9B-R7; G9B; push, merge, rebase, tag, publication, binary publication, public release or redistribution |
| Class of this closeout | documentary; acceptance `STATIC` on the exact closeout commit, reported outside it |

## 2. Approved technical candidate: identity and authentication (2026-10-15)

| Item | Value | Check |
|---|---|---|
| repository | `mpradovelasco/GeoCeDG` | — |
| published implementation base | `ceb5a7e9ef8bafcfabb10aeba5162451d88f79c3`, tree `fa748fd1b87ae16c9f0c248f8f77b94069cc2ea3` | equal to the live remote `main` (`git ls-remote`) |
| **T'** | **`da1a09528a6f97254734400b12256e8fb20a6216`**, tree **`6e02685bf2877536ee02f98866352e46ef1ec49e`**, parent `e7076658294f2ea9343d6a95ce3286f82966843e`, branch `phase/pre-g9b-r6-plus-g-r1` | exact; worktree clean; no commit after T' before this closeout |
| original candidate T | `dd3937cfba7092877f48e7f927dd6213959292cf`, tree `b45311d2b78d9c3625e379835b8d76031df9530a`, branch `phase/pre-g9b-r6-plus-g-implementation` | unchanged; not an ancestor of T'; historical, superseded |
| ancestry | linear first-parent chain base → `6e09eee9` → `ebd6accb` → `5962569e` → `f52cf5b3` → `9854a75f` → `051a5173` → `2696d855` → `e7076658` → T' | verified from local objects |
| version ordering | the eight commits before T' keep `1.0.0` without a version date; T' alone carries `1.1.0` / `2026-10-15`, package-profile schema `2`, provenance schema `3` | the version transition is the last productive commit |
| campaign evidence | 68 files, git-ignored `artifacts/agent/g-da1a0952-campaign-evidence/` | all match `SHA256SUMS.evidence.txt` |

The seven acceptance results of T' (each re-authenticated with `Read-VerificationJson` and
`Get-VerificationResultHash`: recorded and recomputed result hash equal, run journal
`COMPLETED`, candidate and tree equal to T'):

| Check | `run_id` | `result_hash` | Result |
|---|---|---|---|
| `STATIC` | `verification-a7c532697854438890e73d63007be7d8` | `81ec15b4d08919d0507e04585d18d56c4123d59a3115c66ee22e8c3a4b77340f` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |
| `INFRA_UNIT` | `verification-a55ffff5a5c9428aae7583eb96e72350` | `90eea154730d79e747e48c692ae678c881321289395256e57b39ff0710c0a2fb` | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-R6-PLUS-G` | `verification-2be44eeeaaad492aa9a45dc8da95e156` | `33fcb7c13ffe94b2a6c3050d84d84cfb00e94d8976bc4a4c9568f882ab7b215e` | `ACCEPTED / COMPLETE`, 5/5; 127 tests, 0 failures |
| `INTEGRATION` | `verification-0249a836d09e486ba7af7675ca6fabec` | `ea8ada3425c1f6f198b9865266673cd2cd593b4c7502851da1ce72e0c0f7d312` | `ACCEPTED / COMPLETE`, 19/19; Desktop 2153 (1 skipped), shared 6992 (10 skipped), 0 failures |
| `PACKAGING -CheckToolchain` | `verification-975073e4e9454e73928b2202a39df2af` | `6a1b3e03d54bd7276bf4acb3f6dfa52515275028096e4713499134f1bb158e15` | `ACCEPTED / COMPLETE`, 3/3 |
| `PACKAGING -VerifyPackagingArtifacts` (INTERNAL set of T') | `verification-93241d615be4453a826bb6f2cf466936` | `e9eb1d5313d48cd63bf7994d75e4a189b991a5daec81ce9bb964a8762d4b6ab0` | `ACCEPTED / COMPLETE`; `packaging.product` 45/45 |
| `PACKAGING -VerifyPackagingArtifacts` (NC set of T') | `verification-8dc060acbe654be6ac5b1d79205f1691` | `95436b567389ed42a8d770c92568392d7622266d5f4e7f929844ca17115c20b3` | `ACCEPTED / COMPLETE`; `packaging.product` 45/45 |

Also on T': `COMMERCIAL` refused (exit 1, "not authorized"), output root unchanged (441
files); the INTERNAL and NC records bind to T' (`1.1.0` / `2026-10-15`, digest
`4fd3dd71…`); `desktop.jar` is identical in both sets; the runtime probe of the shipped
`desktop.jar` gave `INTERNAL`, `NC`, `DEVELOPMENT` and `UNVERIFIED` (missing and tampered
record). `FINAL` was not run: no global verification-infrastructure change was found and
none was authorized. Results and packages: [candidate report](pre_g9b_r6_plus_g_candidate_report.md)
and the G-R1 handoff evidence.

## 3. Approved content

### 3.1 G implementation

- **Product metadata and About (B-3):** PM-1 to PM-12 as transcribed into the
  [Windows packaging contract](../../geocedg/specs/packaging/windows-packaging.md) and the
  [application profile](../../geocedg/specs/ui/application-profile.md); the GeoCeDG About with
  the version date, build identity, runtime distribution condition (`INTERNAL`, `NC`,
  `DEVELOPMENT`, `UNVERIFIED`) and the composite-work licensing summary. `COMMERCIAL` stays
  unauthorized; no distribution right, commercial permission or public release is implied.
- **Version:** `1.1.0`, version date `2026-10-15`, author designated and adopted by this
  approval; the date is not the approval, build, commit or publication date.
- **Help:** the minimal bilingual EN/ES help package with the selected editorial items; no
  new heading, window or help framework; Classic-shared help unchanged.
- **Characterization:** SC-01 to SC-27 ([report](pre_g9b_r6_plus_g_document_replacement_characterization_report.md)),
  the historical fingerprint of the uncorrected product.
- **Structured debt register:** the 134-entry register with the dispositions of §4.
- **D-1:** the packaging recovery of T's campaign, accepted (G-R1 authorization §3).

### 3.2 G-R1 corrective disposition (bounded approval)

The author approves the bounded Open-route correction of T' ([report](pre_g9b_r6_plus_g_r1_correction_report.md)):

- the approved session state (the `ExportArea` session with its MANUAL rectangle, explicit
  producer, overlay and ISO A link) resets once, only after a successful qualifying Open
  (File > Open of `.html`/`.htm`, API `openFile` of native documents, URL drops and the
  existing `loadFile`/`loadXML(String)` routes);
- the transient paste notice clears on successful full-document E2/E3/E5 transitions,
  including the characterized API routes — **explicitly accepted by the author**;
- this does not extend the `ExportArea` reset to the API transports whose `ExportArea`
  lifecycle is undecided (`setBase64`, non-native `openFile`/`loadXML(File)`, API `setXML`);
  that behaviour is a characterized, non-blocking observation with an accountable owner
  (§4), and it is not presented as compliant with a contract that does not exist.

Preserved: no reset on unsuccessful loads; no Open semantics for Insert File or Apply
Styles; no tool-replacement correction; no document-serialization or kernel change.

### 3.3 Class-1 documentation reconciliation

Applied by G (`f52cf5b3`): the mini-track plan and roadmap status labels,
`cedg-workspaces.md` (133 actions), `construction-text-view-scaling.md` and ADR 0029 status
lines, `g9_extended_dxf_architecture.md` (default-off as the G9X1 historical qualifier) and
the six SplineV2 manifest purposes; the S3 and ADR 0029 status lines were refined and
confirmed by the author in G-R1 (`e7076658`). Classes 3 and 4 stay retained (P-10, P-12,
P-13 pinned headers, P-18). Historical closeout records are not rewritten.

### 3.4 No unauthorized product change

From the base to T' the product delta is GeoCeDG-owned Desktop source
(`org/geocedg/desktop/**`), the Desktop build script, the packaging profile, builder and
check, and profile texts; no inherited, shared, kernel, serialization or geometry source
changed (upstream boundary OK, 1069 registered files). This closeout changes documentation
and mirrors only.

## 4. Debt and observation dispositions (author, 2026-10-15)

Applied individually in the [structured register](pre_g9b_r6_plus_g_structured_debt_register.md)
(§2 rows and §4) and its mirror; the 134 entries are preserved.

| Group | Rows | Recorded disposition |
|---|---|---|
| A — supported by approved implementation | P-11, P-14, P-15, R-5, R-19, R-33 | `RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION`, each limited to the characterized issue; P-11, P-14 and P-15 are documentary corrections, not normative amendments |
| B — accepted limitations and retained observations | L-17, L-26, L-27, P-1, P-7, P-17 | `ACCEPTED LIMITATION` (owner: author) |
| | P-16 | `OPEN — RETAINED WITH OWNER` (author; checkpoint PRE-G9B-R7) |
| | P-10 | `DOCUMENTARY DISCREPANCY — RETAINED` (author) |
| | B-1 residue (SC-03 to SC-05, SC-16, SC-23, SC-26) | `OPEN — RETAINED WITH OWNER — NON-BLOCKING` (author; a future bounded document-session lifecycle-contract activity; checkpoint PRE-G9B-R7) |
| C — explicit deferrals | B-2, P-5, P-6, P-8 | `DEFERRED — RETAINED WITH OWNER G11` (within its layer/view and lifecycle-contract scope) |
| | P-12; the pinned remainder of P-13 | `DEFERRED — RETAINED WITH OWNER PRE-G9B-R7` (documentary reconciliation; historical pins and scripts unchanged) |
| D — pending normative corrections | P-4 | `OPEN — SPECIFICATION / IMPLEMENTATION DISCREPANCY` (author; a separately authorized bounded resolution activity is required; the requirement is not waived) |
| | P-18 | `NORMATIVE DISPOSITION DEFERRED — AUTHOR OWNER` (promotion, withdrawal or supersession per clause) |
| E — mandatory About requirement | B-3 | `RESOLVED — AUTHOR APPROVED` on T' |
| already disposed by G-R1 | P-9; the ADR 0029 part of P-13 | author confirmation (2026-10-15) |

**Both document-replacement observations have explicit dispositions:**
`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` — Open routes corrected and
approved, uncontracted residue retained with an accountable owner;
`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` — `DEFERRED — RETAINED WITH OWNER G11`, not
corrected, with the recorded contract question "does a tool replacement keep the session
working layer?" ([correction report](pre_g9b_r6_plus_g_r1_correction_report.md) §6).

**Owners.** Every retained `OPEN` or `DEFERRED` row names an accountable owner; the O rows,
D-9 and L-7 whose owner was undecided now name the author, with `PRE-G9B-R7` as the next
reconciliation checkpoint (no implementation authorized). Monitoring: O-1, O-2 and O-27 did
not recur in the INTEGRATION runs of T and T'; their causal attribution stays open.

**Count.** Gate rows awaiting an author decision: **0** (25 disposed). Closeout blockers: none.

## 5. Remaining limitations, open risks and future owners

| Owner | Items |
|---|---|
| G11 | B-2 (tool-replacement working layer), P-2 (`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`), P-5, P-6, P-8 |
| G12 | D-10 (physical zoom aid) |
| G15 | P-3 (`ENH-B-MANUAL-EXPORT-AREA-DRAG`), D-8 (`TD-PDF-1`, after a physical-presentation contract-design activity) |
| PRE-G9B-R7 | P-12 and the pinned headers of P-13; follow-up of O-1, O-2 (planning owner) |
| future insertion and styles activity (author-named) | D-1 to D-7 (`DEFER-F3-*`) |
| author (accountable; checkpoint PRE-G9B-R7) | B-1 residue, P-4, P-10, P-16, P-18, D-9, L-7 (tracking), O-3 to O-27 |

Accepted limitations stay as recorded (L-1 to L-31 and the Group B rows). Open technical
risks with standing evidence: the uncontracted `ExportArea` lifecycle of the API transports;
the save-warning discrepancy; the user-library collision policy; the PDF debts
(`TD-PDF-1`, `OBS-F4P-FREEHEP-XREF-SIZE`); the intermittent full-suite observations.

## 6. Global closeout gate evaluation

Conditions of the [G design closeout](pre_g9b_r6_plus_g_author_design_closeout_record.md)
§6.4 and the plan:

| Condition | Result |
|---|---|
| dispositions of both document-replacement observations | met (§4) |
| per-item dispositions of the design closeout §4.3 (closeout-silent rows, proposed re-categorizations, B and P rows) | met (§4; 0 gate rows pending) |
| class-1 label corrections applied by G | met (§3.3) |
| G technical candidate approved with the author smoke | met (§1, §2) |
| every subphase of the mini-track closed | met (matrix below) |

Predecessor matrix (each commit verified as an ancestor of T'):

| Subphase | Approved identity | Status |
|---|---|---|
| P0 | candidate `6b7fd5de`, closeout `562e2bb1` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| A-1 | `2a71133a` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| A-2 | `06a2ea6b` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| B | `848206c4` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| C; C-X1 | `6248bf0f`; `eabe6368` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| D0; D1 | `8383a153`; `6e1d8459` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| E1 (E1-L, E1-P, E1-X1, E1-P-X1) | `5f34d181`, `dcda4075`, `b12ab32c`, `44b90156` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| E2 | `3a724661` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| E3 (E3-R1; POST-E3-INTEGRATION) | `7cd50116`; `00b525fd` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| F1 | `1cea3c89` | `PASS — AUTHOR APPROVED — PUBLISHED` (excludes routes X1–X10 and Graphics View 2) |
| F2 (with F2-R1) | `488f1e99` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| F3 (with F3-R1, F3-R2) | `2d7b2cae` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| F4 (F4-A, F4-D; aggregate) | `6baa66a5`, `6214d15a`; `19f8f206` | `COMPLETE — AUTHOR APPROVED` (F4-B closed for AutoCAD 2026; F4-C deferred as `TD-PDF-1`) |
| G (design; implementation G-R1) | design closeout `ceb5a7e9`; T' `da1a0952` | `PASS — AUTHOR APPROVED` (this record) |

Result: **`PRE-G9B-R6-plus-G = PASS — AUTHOR APPROVED`** and
**`PRE-G9B-R6-plus = PASS — AUTHOR APPROVED`**. This closes the mini-track only; it does not
start or close `PRE-G9B-R7`, and nothing is `PUBLISHED` until a separately authorized
publication.

## 7. G implementation ancestry

| Commit | Role |
|---|---|
| `6e09eee99a9365ef1d770aa6e48ebab2e5938994` | implementation authorization and entry gate |
| `ebd6accb1a417ca98339d7d2f9342c6140d630ae` | characterization SC-01 to SC-27 |
| `5962569e308652686df2097dceb0ad0575ba39d8` | About, distribution condition, bilingual help (1.0.0) |
| `f52cf5b31fc36b3dac007c7204b2df53ea80fcc6` | structured debt register and class-1 reconciliation |
| (`dd3937cfba7092877f48e7f927dd6213959292cf`) | original transition and candidate T, superseded, not an ancestor |
| `9854a75f86f57976618d6f4a40956b30cb465b81` | G-R1 authorization, entry gate, D-1 |
| `051a517387e0bda212140216d8a2c2d9c0276cf9` | G-R1 Open-route correction and regression tests |
| `2696d855e5b6ac6a50a336cb4fcd9fb3c08b5e7b` | G-R1 correction and tool-replacement report |
| `e7076658294f2ea9343d6a95ce3286f82966843e` | decision sheet; S3 and ADR 0029 status |
| `da1a09528a6f97254734400b12256e8fb20a6216` | T': rebuilt version transition (last productive commit) |
| this closeout lineage | documentary closeout and roadmap reconciliation |

## 8. Roadmap reconciliation (G11, G12, G15)

The evidence-based reconciliation of the future G11, G12 and G15 scopes is the
[roadmap reconciliation report](../architecture/pre_g9b_r6_plus_g11_g12_g15_roadmap_reconciliation.md).
It classifies every original roadmap item, proposes wording for `PRE-G9B-R7` review and marks
none of the three gates as passed. `ENH-B-MANUAL-EXPORT-AREA-DRAG` stays
`OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15` (an interactive rectangle gesture updating
the existing `ExportArea(MANUAL)` beside the numeric editor; no second export-area authority).

## 9. PRE-G9B-R7 entry-gate statement

Authority: the [final stabilization design](../architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md),
section `PRE-G9B-R7`, and the author decision of 2026-10-01 that `PRE-G9B-R7` requires
`PRE-G9B-R6-plus = PASS — AUTHOR APPROVED`.

| Entry item | State at this closeout |
|---|---|
| G closure | `PASS — AUTHOR APPROVED` recorded here; the closeout commit must be **published** to `main` by a separate, exact-SHA, non-force author authorization before R7 starts from it |
| mini-track | `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED` (recorded here, unpublished) |
| accumulated technical evidence | the G-R1 campaign of T' (§2) and the approved closeouts of every subphase (§6); no R7 verification has been run |
| debt ownership | R6-plus register: 0 gate rows pending; every retained OPEN or DEFERRED row has an owner. PRE-G9B-R track ledger (latest in the [PRE-G9B-R6 closeout record](pre_g9b_r6_closeout_record.md)): to be reconciled by R7 itself, including `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`, which still needs an owner by the R7 closeout |
| roadmap reconciliation | the G11/G12/G15 report (§8), for author review inside R7; no substantive roadmap restructuring yet |
| verification class | to be frozen by the R7 authorization from the actual integrated scope: `INTEGRATION` at minimum, `FINAL` if any authorized phase changed global verification infrastructure (none known in G) |
| author decisions and smoke | the R7 authorization (exact published base, frozen class); the revised G11/G12/G15 scope; the bounded resolutions of P-4 and P-18 and the lifecycle contract for the B-1 residue if wanted; the R7 author smoke (required) |
| stop conditions | an open debt item without an owner; an unresolved author decision from the design §6 |

`PRE-G9B-R7` stays `DESIGNED — NOT AUTHORIZED`; this record does not start it or run its
verification. `G9B`, `G9C`, `G9U2`, productive `G10` and G11, G12 or G15 implementation are
not authorized.

## 10. Publication target and boundary

The publication target is the final documentary closeout commit of this lineage (descending
from T' and from the published base `ceb5a7e9`), to be promoted to `main` only by a separate
author authorization naming its exact SHA, by a non-force fast-forward. Its identity and its
`STATIC` result are reported outside this record. No push, merge, rebase, tag, binary
publication, public release or redistribution is authorized by this record.

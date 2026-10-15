# PRE-G9B-R6-plus-G-R1 — consolidated author decision sheet (global-closeout items)

```text
SHEET_KIND                   = CONSOLIDATED AUTHOR DECISION PREPARATION (G-R1, operation 5)
SOURCE REGISTER              = 134 entries, 25 rows gating the global PRE-G9B-R6-plus closeout
DISPOSED BY G-R1             = P-9 (author confirmation, G-R1 authorization §1 op. 6);
                               the ADR 0029 part of P-13; the Open-route part of B-1
                               (authorized correction); D-1 (not a register row)
STILL AWAITING AN AUTHOR DECISION = 24 rows (§1), in five groups (§2)
AUTHOR DECISION              = NOT RECORDED HERE — the author decides in one later decision
selfApproved                 = false
```

This sheet prepares, in one place, the author decisions still required before the global
`PRE-G9B-R6-plus` closeout. It is a proposal: it closes, accepts, defers or re-owns nothing.
Sources: the [structured register](pre_g9b_r6_plus_g_structured_debt_register.md) (134
entries, preserved), the [adopted disposition proposal](pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md)
(evidence per row), the [G author design closeout](pre_g9b_r6_plus_g_author_design_closeout_record.md)
§4 and §6.4, the [G-R1 authorization](pre_g9b_r6_plus_g_r1_authorization_record.md) and the
[G-R1 correction report](pre_g9b_r6_plus_g_r1_correction_report.md). Machine-readable mirror:
[`pre-g9b-r6-plus-g-r1-decision-sheet.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-r1-decision-sheet.json).

Reconciliation of the count: the register marks 25 rows `Global gate = Yes` (B-1 to B-3,
P-1, P-4 to P-18, L-17, L-26, L-27, R-5, R-19, R-33). G-R1 records the author's
confirmation of the S3 status line, so P-9 needs no further decision; P-13 keeps its
pinned-header part; B-1 keeps its uncontracted residue; B-3 is the approval of G itself.
24 rows remain. None of them is a product dependency of the G technical acceptance; each
blocks only the global closeout until decided.

Status vocabulary: **already approved** (an earlier author approval covers it); **authorized
by G-R1**; **pending author decision**; **open non-blocking** (no current contract shown to
fail); **blocking global closeout** (a required disposition or implementation is missing).

## 1. Items

| # | Item | Recorded → current verified status | Approved evidence | Product correction needed | Explicit author disposition needed | Covered by an earlier approval | Recommended disposition (owner) | Effect: G acceptance / global closeout |
|---|---|---|---|---|---|---|---|---|
| B-1 | `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | OPEN, violations SC-01/02/11 → **Open routes corrected in G-R1**; SC-03 to SC-05, SC-16, SC-23, SC-26 unchanged, no contract | characterization F-1 to F-6; G-R1 report §1, §4 (28 rows) | none further | yes, for the residue | Open-route part: authorized by G-R1 | accept the residue as characterized behaviour: the API transports and the cancelled v1 Open URL keep the export area (the ISO link stays identity-protected, F-4); embedded close (embedded hosts only) and the Move switch on a native Open as observed. Alternative: a separately authorized extension of the reset. | correction verified with T'; blocking global closeout until the residue is decided |
| B-2 | `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` | OPEN → unchanged by G-R1 (probe, tests) | characterization F-5; G-R1 report §6 | none required by a contract | yes | no | record "tool replacement is not a document transition" (export area, notice, scale kept; ISO link revalidated by identity); defer the working-layer recomputation to **G11** (owner of layer organization and of P-2) | none / blocking until decided |
| B-3 | `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` | implemented in T → implemented in T' (PM-1 to PM-12, `1.1.0` / `2026-10-15`); T superseded for final review | G candidate report; T' acceptance | no | yes (approval of the exact T' after smoke) | the PM contract is design-approved | approve T' after the author smoke | it is the G approval; blocking global closeout until then |
| P-1 | `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES` | PENDING → unchanged (pre-existing Classic and adjacent sites; 1 of 100 Classic launches failed in the C-X1 preparation) | C-X1 records, `DQ-X1-4` | optional | yes | recorded by C-X1 | accept as an inherited limitation of the Classic diagnostic surface, with a revalidation trigger if Classic becomes a supported user surface; alternative: a named bounded corrective (C-X1 precedent) | none / blocking until decided |
| P-4 | `OBS-R6P-SAVE-WARNING` | OPEN (kept open, design closeout §4.2) → unchanged: `confirmSave` has no production caller; `locus-v2-public-surface.md:609-612` requires the warning; the G help claims none | P0 and planning reports | yes: specification or code | yes | kept open by the author | separately authorized correction: preferably a normative amendment aligning the warning with the approved `.cedg`-only native save policy; alternatively wire the existing warning into Save | none / blocking until decided |
| P-5 | `OBS-A1-3D-VIEW` | PENDING → unchanged (hidden layers not applied in 3D) | A-1 closeout; A-2 candidate | not in R6-plus | yes | no | defer to **G11** (per-view visibility) | none / blocking until an owner is recorded |
| P-6 | `OBS-A1-NON-HIT-SELECTION` | PENDING → unchanged (Select All reaches hidden layers) | A-1 closeout; A-2 evidence | not in R6-plus | yes | no | defer to **G11** (locking and filters) | as P-5 |
| P-7 | `OBS-A1-GRAPHICS2-GEOCEDG-MODES` | PENDING → unchanged (Graphics View 2 has no GeoCeDG mode parity) | A-1 closeout; F2 exclusion | not in R6-plus | yes | L-29 already accepts the F1 Graphics View 2 exclusion | accept as a limitation consistent with L-29 until the author names a frontend owner | as P-5 |
| P-8 | `OBS-A1-BACKGROUND-IMAGE-LAYER` | PENDING → unchanged | A-1 closeout; B candidate | not in R6-plus | yes | never accepted by B | defer to **G11** (layer semantics) | as P-5 |
| P-9 | `OBS-F4P-S3-SPEC-HEADER` | CORRECTED BY G → **confirmed by the author in G-R1**; the line cites `PASS — AUTHOR APPROVED` on `543c77295fdc…` and the S3 closeout report | S3 closeout report; roadmap S3 row | no | no (given) | **authorized by G-R1** | close | none / gate satisfied |
| P-10 | `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | RETAINED (class 4) → unchanged; G-R1 excludes ADR 0035 body changes | F4 aggregate closeout; the ADR's Amendment | no | yes (retention) | the accepted Amendment prevails | retain as a registered discrepancy; an annotation only by separate authorization | none / blocking until retention is recorded |
| P-11 | `OBS-R6P-DXF-DEFAULT-DOC` | CORRECTED BY G (class 1) → verified at `f52cf5b3` | `f52cf5b3`; `AppConfigGeoCeDGTest` | no | yes (confirm closure) | class-1 corrections authorized for G (area F) | confirm closure | none / blocking until confirmed |
| P-12 | `OBS-R6P-DXF-SPEC-HEADER` | RETAINED (class 4, pinned by the historical G9X1 verifier) → unchanged | planning and P0 reports | no product; a pin reconciliation | yes | no | retain; defer the pin reconciliation to **PRE-G9B-R7** (integrated verification) | none / blocking until decided |
| P-13 | `OBS-R6P-DOC-IDENTITY-HEADER` | PARTLY CORRECTED → **ADR 0029 part confirmed by the author in G-R1** (cites the R3 closeout, `89fab9d1…`); `native-document-identity.md` and ADR 0016 headers retained (byte-pinned by the G9U0-R2 verifier; excluded by G-R1) | R3 closeout record; planning and P0 reports | no product; a pin reconciliation | yes, for the pinned headers | ADR 0029 part: **authorized by G-R1** | retain the pinned headers; defer their reconciliation to **PRE-G9B-R7** | none / blocking until decided |
| P-14 | `OBS-R6P-ACTION-COUNT-DOC` | CORRECTED BY G (class 1) → 133 actions; qualifiers reconciled | `cedg-workspaces.md`; `f52cf5b3` | no | yes (confirm closure) | class-1 corrections (area F) | confirm closure | as P-11 |
| P-15 | `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | CORRECTED BY G (class 1) → six purposes completed | `modified-files.yml` at `f52cf5b3` | no | yes (confirm closure) | class-1 corrections (area F) | confirm closure | as P-11 |
| P-16 | `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION` | OPEN, pre-existing → unchanged | E2 preparation; plan | not in R6-plus | yes | `DQ-E2-7` decided naming only | accept as a pre-existing limitation with a revalidation trigger at the next user-library policy change; alternative: a future author-named user-library policy activity | none / blocking until decided |
| P-17 | `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE` | RECORDED (record only) → unchanged | E2 preparation | no | yes | record only | accept as a limitation (document macros take precedence over native commands) | none / blocking until decided |
| P-18 | Undisposed C and E3 specification amendments | RETAINED (class 3) → unchanged; G-R1 excludes their promotion | E3 closeout; the amendment clauses | a documentary normative act | yes, per amendment | C, E3, F1 and F3 are approved; the clauses were never promoted | separately authorized documentary promotion of the clauses of approved phases, each named by the author; retained until then | none / blocking until decided |
| L-17 | `OBS-D1-DEFAULTS-SAVE-RELEVANT` | RECORDED (O) — PROPOSED L → unchanged | D1 closeout | no | yes (re-categorization) | consequence of `DQ-D1-5` | confirm L | none / blocking until confirmed |
| L-26 | `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` | RECORDED (R) — PROPOSED L → unchanged | E3 author decisions (`DQ-E3-5`) | no | yes (re-categorization) | `DQ-E3-5` | confirm L | as L-17 |
| L-27 | `OBS-A1-RECOMPUTE-OUTPUTS` | RECORDED (R, owner none required) — PROPOSED L → unchanged | A-1 closeout | no | yes (re-categorization) | A-1 closeout | confirm L | as L-17 |
| R-5 | `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION` | ADDRESSED BY E2 — DISPOSITION REQUIRED → `final.desktop` on T: 2144 tests, 0 failures, no out-of-memory; T' reported in the handoff | E2 candidate; G INTEGRATION | no | yes (confirm) | E2 approved | confirm closure (resolved by the approved E2 implementation; new host-creating tests use the lifecycle owner, as the G-R1 class does) | as P-11 |
| R-19 | `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | CLOSED IN THE A-2 CANDIDATE ONLY → unchanged | A-2 candidate evidence | no | yes (confirm) | A-2 approved | confirm closure | as P-11 |
| R-33 | E2 minor items (drag interception, LaTeX text package, LaTeX dimension fidelity) | APPROVED WITH E2 — CLOSEOUT SILENT → unchanged | E2 candidate | no | yes (confirm) | E2 approved | confirm closure | as P-11 |

## 2. Groups and proposed consolidated decisions

| Group | Rows | Proposed author decision (one sentence each) |
|---|---|---|
| 1. Closable on existing authoritative approval | P-11, P-14, P-15, R-5, R-19, R-33 (P-9 and the ADR 0029 part of P-13 already disposed by G-R1) | "I confirm the closure of P-11, P-14, P-15, R-5, R-19 and R-33 on their recorded evidence and earlier approvals." |
| 2. Acceptance as limitations or retentions | L-17, L-26, L-27 (re-categorization to L); P-1, P-7, P-16, P-17 (limitations); P-10 (retained discrepancy); B-1 residue (SC-03 to SC-05, SC-16, SC-23, SC-26) | "I accept L-17, L-26 and L-27 as limitations, P-1, P-7, P-16 and P-17 as limitations with the stated revalidation triggers, P-10 as a retained discrepancy, and the uncontracted B-1 routes as characterized behaviour." |
| 3. Deferral with named owners | B-2 (G11, plus the not-a-transition record); P-5, P-6, P-8 (G11); P-12 and the pinned headers of P-13 (PRE-G9B-R7) | "I record tool replacement as not a document transition and defer its working-layer recomputation and P-5, P-6 and P-8 to G11, and the pinned-header reconciliation of P-12 and P-13 to PRE-G9B-R7; no implementation is authorized now." |
| 4. Separately authorized corrections | P-4 (specification amendment preferred, or wiring); P-18 (promotion of the approved amendments) | "I will authorize separately the P-4 correction (specification amendment or wiring) and the P-18 promotion, each with its own scope." |
| 5. Open pending additional evidence | B-3 (author smoke and approval of T') | "I approve, or not, the exact T' after the smoke." |

Monitoring rows outside the gate set (O-1, O-2, O-27) are reported with the T' acceptance in
the handoff; D-1 is disposed by the G-R1 authorization and is not a register row.

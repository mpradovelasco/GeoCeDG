# PRE-G9B-R7 — consolidated PRE-G9B-R debt reconciliation

```text
VIEW_KIND             = PHASE-SPECIFIC RECONCILIATION VIEW (PRE-G9B-R7, deliverable C)
GLOBAL LEDGER         = NONE CREATED (AD-R0-7 still binds; every row points to its
                        authoritative record, which keeps its authority)
STATUS CHANGES        = NONE — no OPEN item is promoted to RESOLVED or ACCEPTED LIMITATION
OWNERSHIP DESIGNATIONS = applied under the R7 authorization ownership rule to every
                        previously unowned item:
                        OWNER = AUTHOR — ACCOUNTABLE
                        NEXT_CHECKPOINT = PRE-G9B-R7 AUTHOR REVIEW
                        (retention and accountability only; no resolution, no
                        implementation authorization)
ITEMS WITHOUT A LIVE OWNER = 38 before R7 (§2: 1, §3: 30, §4: 7) -> 0 after R7;
                        R6-plus register rows without an owner: 0 (unchanged)
PROPOSED DISPOSITIONS = §7 (candidate proposals, kept as history)
AUTHOR DECISIONS      = ADOPTED 2026-10-15 (§8; PRE-G9B-R7 closeout record), which
                        prevail over §1-§7 where they differ
R7 TECHNICAL BLOCKERS = none
selfApproved          = false
```

This view reconciles the complete debt lineage of the `PRE-G9B-R` track at the base
`b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513`: the
[R0 disposition matrix](pre_g9b_r0_technical_debt_disposition_matrix.md), the ledgers
carried forward by every later closeout up to the
[R6 closeout](pre_g9b_r6_closeout_record.md) (which carries the R5-B ledger "and
through it the earlier ones"), the author's `Ri` quality-debt policy in the
[R2-E0/E1 closeout](pre_g9b_r2_e0_e1_closeout_record.md) (rule 5: every retained debt
needs a terminal disposition or an explicit owner by the `PRE-G9B-R7` closeout), and
the 134-entry [R6-plus structured register](pre_g9b_r6_plus_g_structured_debt_register.md)
with the [G closeout](pre_g9b_r6_plus_g_closeout_record.md) §4–§5. Identifiers and
provenance are preserved. The "current approved status" column transcribes the
authoritative record; the "R7" columns are this phase's designation or proposal.
Machine-readable mirror:
[`pre-g9b-r7-debt-reconciliation.json`](../../geocedg/validation/pre-g9b-r7/pre-g9b-r7-debt-reconciliation.json).

Abbreviations: `V/` `docs/validation/`; `RM` the roadmap; `AA` = `AUTHOR — ACCOUNTABLE,
NEXT_CHECKPOINT = PRE-G9B-R7 AUTHOR REVIEW` (designated by R7); `AL` = accepted
limitation; `ADR` = author decision required.

## 1. Rules applied

1. **No status promotion.** An item recorded `OPEN`, `RETAINED` or `DEFERRED` keeps that
   status. Where the evidence supports a terminal disposition, R7 *proposes* it and the
   author decides (§7).
2. **Owner first.** An existing explicitly approved owner is kept. Only items with no
   recorded owner receive `AA`. G11, G12 and G15 debts stay with those gates and are not
   moved into R7 scope.
3. **Historical records are not edited.** Contradictions between records (§6) are
   recorded here and resolved only by author decision or in living documents.
4. **Blocking effect.** "None" means the item does not block the R7 technical candidate
   or the R7 closeout once its owner is recorded; the closeout needs only the author
   decisions the author chooses to take.

## 2. The R0 items and the item explicitly delegated to R7

| ID | Source authority | Current approved status | Current owner | R7 proposal | R7 blocking | Evidence | ADR |
|---|---|---|---|---|---|---|---|
| `TD-P1-SEMANTIC-LENGTH-INTERSECTION` | [R2 closeout](pre_g9b_r2_closeout_record.md) | CLOSED by `PRE-G9B-R2` | R2 (done) | terminal; no action | none | R2 closeout; RM `PRE-G9B-R2` row | no |
| `TD-P1-PACKAGING-SUMMARY` | [R1 closeout](pre_g9b_r1_closeout_record.md); R1 evidence JSON | CLOSED by `PRE-G9B-R1` (R1 is `AUTHOR APPROVED FOR PROMOTION — RETAINED PRE-EXISTING BASELINE DEFECT`) | R1 (done) | terminal; no action | none | `geocedg/validation/pre-g9b-r1/pre-g9b-r1-verification-evidence.json` | no |
| `TD-VERIFY-RECEIPT-RECOVERY` | same | CLOSED by `PRE-G9B-R1`; every later accepted `FINAL` emitted a receipt | R1 (done) | terminal; no action | none | e.g. A-2 `FINAL` receipt `b492d484…` | no |
| `TD-G9X1-HISTORICAL-PIN` | same | CLOSED by `PRE-G9B-R1` (historical, successor and supersession recorded) | R1 (done) | terminal; the later standalone-scan failure is the separate register row O-24 (§6, item 5) | none | R1 closeout; `tools/agent/verify-g9x1-extended-dxf.ps1` constants | no |
| `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` | [R1-R1 closeout](pre_g9b_r1_r1_closeout_record.md) | `RESOLVED` by explicit author decision | R1-R1 (done) | terminal; living-document drift recorded as `OBS-R7-G9R4-LIVING-DOC-DRIFT` (§4) | none | R1-R1 closeout §decision | no |
| `TD-R0-G9U1-Q02-BINDING` | R1-R1 closeout | CLOSED by `PRE-G9B-R1-R1` | R1-R1 (done) | terminal | none | R1-R1 closeout | no |
| `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` | [R4 closeout](pre_g9b_r4_closeout_record.md) | `RESOLVED` | R4 (done) | terminal | none | R4 closeout; `pre-g9b-r4-closeout.json` | no |
| `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY` | [R3 closeout](pre_g9b_r3_closeout_record.md) | resolved only implicitly: "`D3-a` and `D3-b` resolved" (D3-b is this item); the identifier is never named closed and RM still shows it `NEW` with owner R3 | R3 (done) | confirm `RESOLVED BY PRE-G9B-R3 (D3-b)` by identifier | none | R0 JSON maps D3-b to this ID; R3 closeout | **yes** (confirmation) |
| `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` | [R6 closeout](pre_g9b_r6_closeout_record.md) | `RESOLVED — AUTHOR APPROVED` | R6 (done) | terminal | none | R6 closeout | no |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | [R3 candidate report](pre_g9b_r3_candidate_report.md) §4; R3 closeout (class `deferred refinement`, `resolvedHere=false`) | "retained; owner and disposition due by the `PRE-G9B-R7` closeout" (R3 through R6; G closeout §9) | **none recorded** | **owner `AA` designated**; proposal: `DEFERRED REFINEMENT — RETAINED WITH OWNER (author)`, with option A `ACCEPTED LIMITATION` (inherited GeoGebra placement preserved, order never identity or provenance) or option B retention for a future, separately authorized construction-order activity | none | it is a requested refinement, not a defect: a redefine that makes A depend on a later B moves A after its newest predecessor instead of moving only B; the host's two placement strategies, the post-reorder failure snapshot and P3-R1 retained-target placement made it exceed one bounded refinement; no later phase touched the redefine reorder (POST-E2-P4 uses the host move primitive for a different feature) | **yes** (A or B) |

## 3. R-track ledger items retained without an owner (R1–R6)

All rows below were carried forward as "retained", "carried forward unchanged" or
"observation, retained" with no recorded owner, last in the R6 closeout ledger. R7
designates `AA` for each. None blocks R7.

| ID | Source authority | Current approved status | R7 proposal | Evidence | ADR |
|---|---|---|---|---|---|
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | [Dot/XML maintenance closeout](maintenance_dot_xml_locale_independent_serialization_closeout_record.md) | retained; "no migration is authorized" | `AL` — documents saved before the correction keep their localized `Dot` head; any migration needs its own explicit design (`AGENTS.md` §11.6, §12.6) | maintenance candidate report; new saves corrected (`HZ-R5B-DOT-LOCALIZED-XML` resolved) | yes |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | [R1-R1 candidate report](pre_g9b_r1_r1_periodic_quarantine_native_roundtrip_candidate_report.md) | `RETAINED PRE-EXISTING BASELINE DEFECT`; carried "for later governance/documentation reconciliation" | retain with `AA`; the standalone historical G9U1 verifier is not a registry leaf; a reconciliation of its script pin against the immutable Round-3 evidence needs a separately authorized historical-verifier task | `verify-g9u1-construction-workspace.ps1` constants vs the frozen evidence JSON | yes |
| `OBS-R2-PHASE-SELECTION-COHORT` | [R2 closeout](pre_g9b_r2_closeout_record.md) | carried forward unchanged | `AL` (informational; two phase selections resolve to the same cohort; no acceptance effect) | R2 closeout | yes |
| `OBS-R6-PLACEHOLDER-REGEX-REPLACEMENT` | R6 closeout | `RETAINED` | retain with `AA` as a candidate for a future GGBScript hardening activity (fails truthfully since R6) | R6 candidate report; capability matrix | yes |
| `OBS-R6-EVENT-DISPATCH-SELF-GEO-ON-ESCAPE` | R6 closeout | `RETAINED` (upstream) | `AL` (inherited upstream) | R6 candidate report | yes |
| `OBS-R6-EXECUTE-INNER-FAILURE-NOT-PROPAGATED` | R6 closeout | `RETAINED` (inherited `Execute` contract, in the matrix) | `AL` | capability matrix rows | yes |
| `OBS-R6-EXECUTE-ALIAS-SYNTAX-KEY` | R6 closeout | `RETAINED` (presentation only) | `AL` | R6 candidate report | yes |
| `OBS-R6-ROTATE-FORM-4-UPSTREAM-REFUSAL` | R6 closeout | `RETAINED` (truthful upstream refusal) | `AL` | capability matrix (`NO_GEOCEDG_BRANCH`) | yes |
| `HZ-R5B-ALIAS-CAS-STATE` | [R5-B closeout](pre_g9b_r5_b_closeout_record.md) | retained compatibility observation; not worsened | `AL` | R5-B closeout | yes |
| `HZ-R5B-HARNESS-TABLE-FRESHNESS` | R5-B closeout | retained, unchanged | `AL` (test-harness caveat) | R5-B closeout | yes |
| `OBS-R5B-CAS-TABLE-VERBATIM-HEADS`, `OBS-R5B-HARDCODED-SYNTAX-HEADS`, `OBS-R5B-PROVER-DETAILS-CAPTIONS`, `OBS-R5B-INPUT-BAR-INSERTED-TEXT` | R5-B closeout | retained, unchanged | retain with `AA` (residual non-canonical heads and captions; a future command-surface polish, not authorized) | R5-B candidate report | yes |
| `OBS-R5B-R1-SYNTHETIC-INPUT`, `OBS-R5B-R1-CAS-GROUP-READINESS`, `OBS-R5B-R2-EMBEDDED-HOST-ROOT` | R5-B closeout | retained, unchanged | `AL` (test-harness and readiness notes) | R5-B-R1/R2 reports | yes |
| `OBS-R5B-R2-COMPLETIONS-POPUP-LISTENER-ORDER` | R5-B closeout | retained observation; no observed product failure | `AL` | R5-B-R2 report | yes |
| `OBS-R5B-R1-LANGUAGE-CHANGE-RETURNS-TO-MOVE`, `OBS-R5B-R2-CLASSIC-CARET-READ`, `OBS-R5B-R1-NATIVE-AWT-CRASH-UNATTRIBUTED` | R5-B closeout (distinct statuses: accepted inherited / outside R5-B scope / unattributed, not reproduced) | dispositioned by R5-B; the R6 ledger lumps them as "OBS-R5B-* retained" (§6, item 11) | keep the R5-B statuses; the unattributed crash keeps `AA` for monitoring | R5-B closeout | no (first two); yes (crash monitoring) |
| `CSolve` / `CSolutions` Algebra syntax | R5-B closeout | pre-existing, outside R5-B; retained | `AL` (inherited) | ADR 0031 closeout | yes |
| `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL` | [R5-A closeout](pre_g9b_r5_a_closeout_record.md) | retained observation; needs an author decision on global verification infrastructure | retain with `AA`; R7 covers the leaf by the focused `PHASE -Phase PRE-G9B-R5-A`; adding it to `FINAL` would be a separately authorized verification-infrastructure change | registry: `infra.guide-structure-diagnostic` is required only by `PRE-G9B-R5-A` | yes |
| `OBS-R5A-LATENT-NESTED-LIST-CONTINUATION` | R5-A closeout | retained / non-live | `AL` | R5-A candidate report | yes |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE`, `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | [R3-C1 closeout](pre_g9b_r3_c1_closeout_record.md) | observation, retained (upstream restore paths) | `AL` (inherited upstream) | R3-C1 candidate report | yes |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | [R3-C1-U1 closeout](pre_g9b_r3_c1_u1_closeout_record.md) | retained | `AL` (inherited generic XML paste) | R3-C1-U1 closeout | yes |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | [R3-X1 closeout](pre_g9b_r3_x1_closeout_record.md) | retained | `AL` | R3-X1 candidate report | yes |
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION`, `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY`, `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY` | [R4 closeout](pre_g9b_r4_closeout_record.md) | retained | `AL` (documented authoring behaviour of the approved `SplineV2` contract) | R4 candidate report | yes |
| `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION` | R4 closeout | retained ("may be considered in a later frontend/workspace maintenance pass") | retain with `AA` for a future frontend maintenance activity (not authorized) | R4 corrective report | yes |

## 4. Fixed, conflicting, never-ledgered and newly found items

| ID | Source authority | Current approved status | Current owner | R7 proposal | R7 blocking | Evidence | ADR |
|---|---|---|---|---|---|---|---|
| `BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION` | [R1 closeout](pre_g9b_r1_closeout_record.md) | `OPEN — NOT REPAIRED IN PRE-G9B-R1`; RM still "retained … for resolution before freezing the next phase" | none → `AA` | `RESOLVED BY eab5a7937 — PENDING AUTHOR CONFIRMATION` | none | commit `eab5a7937` pins both guide sources to `text eol=lf` (`.gitattributes`), blob content unchanged; it is an ancestor of the R1-R1 base `d0c4bef1`; every later `FINAL` was accepted with `PostP1BilingualUserGuideTest` in `final.desktop`; R7's own `FINAL` re-executes it | **yes** |
| `GUIDE-A1-SECTION-9.3-STALE` | [R2 closeout](pre_g9b_r2_closeout_record.md) | "carried forward unchanged" (owner the closed `POST-G9U1-A1`) | closed phase → `AA` | `RESOLVED BY PRE-G9B-R2-E0/E1 — PENDING AUTHOR CONFIRMATION` | none | the guide text was rewritten in R2-E0/E1 ("no separate author disposition is recorded"); the guides now state the partial length as defined (§9.3 of both editions) | **yes** |
| `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | R6 ledger ("retained") vs register R-17 ("RESOLVED", A-1 candidate report: closed for new objects) | conflicting | A-1 (new objects) | `RESOLVED FOR NEW OBJECTS BY PRE-G9B-R6-plus-A-1`; residue for documents saved before A-1 proposed as `AL` | none | A-1 candidate report; register R-17 | **yes** |
| `CLIPBOARD-COPY-POINT-DRIVEN-LOCUS-SLICE` | [R2-E0/E1 candidate report](pre_g9b_r2_e0_e1_candidate_report.md) (pre-existing, fail-closed) | never entered a closeout ledger | none → `AA` | `AL` (fail-closed limitation) | none | R2-E0/E1 design and evidence JSON | yes |
| `A3-PAIR-SOURCE-COMPATIBLE-STRUCTURAL-REDEFINE` | same | never ledgered | none → `AA` | `AL` (fail-closed limitation) | none | same | yes |
| `LOCUS-PARTICIPANT-AXIS-NOT-SERIALIZABLE` | same | never ledgered | none → `AA` | `AL` (fail-closed limitation) | none | same | yes |
| `OBS-R7-UNREGISTERED-HISTORICAL-SUITES` (new identifier for an unnamed residue) | [generated-state maintenance closeout](maintenance_final_generated_state_historical_retirement_closeout_record.md) ("other unregistered historical suites (among them `verification-runtime.Tests.ps1`)", out of scope) | out of scope, no identifier | none → `AA` | retain with `AA`; a retirement or registration decision is a separately authorized verification-maintenance task | none | registry has no leaf for these suites | yes |
| `OBS-R7-G9R4-LIVING-DOC-DRIFT` (new) | R1-R1 closeout (`RESOLVED`) | living documents still describe the risk as open: `geocedg/specs/README.md`, `geocedg/specs/ui/cedg-workspaces.md`, `docs/developer/locus_v2_api.md`, `docs/developer/geocedg_developer_guide.md`; `tools/agent/verify-g9s1-r1-spline-pair-materialization.ps1` names it as `$OpenRisk` | none → `AA` | a bounded documentary correction of the four living documents (the historical verifier stays unchanged) at the R7 closeout or later, only by author decision; R7 does not edit specifications outside its authorized perimeter | none | R1-R1 closeout vs the cited lines | yes |
| `OBS-R7-ROADMAP-PANORAMA-TRUNCATED-LINKS` (new) | roadmap §15 panorama rows G11, G12, G15, written by the G closeout commit `b9bdb1e1` | three table rows ended in an unterminated link `([informe](` | — | **corrected by R7** as a factual reference correction (roadmap living document) | none | baseline link check: 3 unterminated links, 797 resolved | no |

## 5. R6-plus register rows reconciled at the R7 checkpoint

The register stays the authority for its 134 entries (B 3, P 18, D 10, O 27, L 31, R 45;
0 gate rows pending). R7 neither reopens the published `PRE-G9B-R6-plus` approval nor
changes a row. It reconciles the rows owned by R7 and gives a proposal for every row
whose next checkpoint is R7.

### 5.1 Rows owned by `PRE-G9B-R7`

| Row | Item | Recorded disposition | R7 action | R7 proposal | ADR |
|---|---|---|---|---|---|
| P-12 | `OBS-R6P-DXF-SPEC-HEADER` | `DEFERRED — RETAINED WITH OWNER PRE-G9B-R7` (documentary reconciliation without altering historical pins or scripts) | reconciled by cross-reference: the header line "Phase \| G9X1 design; implementation not authorized" of `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` is a historical qualifier; the current state is G9X1 `PASS — AUTHOR APPROVED`, promoted to default GeoCeDG behaviour by `PRE-G9B-P1`, with the C amendment retained under P-18. The file stays byte-unchanged because `verify-g9x1-extended-dxf.ps1` pins its live SHA-256 | terminal `RETAINED AS HISTORICAL PINNED HEADER — CURRENT STATUS RECORDED BY CROSS-REFERENCE (R7)`; owner after R7: author. Alternative: a separately authorized task that amends the header and re-pins the historical verifier together | **yes** |
| P-13 (pinned remainder) | `OBS-R6P-DOC-IDENTITY-HEADER` | `DEFERRED — RETAINED WITH OWNER PRE-G9B-R7` (ADR 0029 part confirmed by G-R1) | reconciled by cross-reference: "implementation not authorized / not started" in `geocedg/specs/ui/native-document-identity.md` and `docs/adr/0016-native-geocedg-document-identity.md` are historical qualifiers of the G9U0-R2 planning closeout; the implementation was approved later (G9U0-R2 implementation `PASS — AUTHOR APPROVED`). Both files stay byte-unchanged because `verify-g9u0-r2-product-refinement.ps1` byte-pins them | as P-12 | **yes** |
| O-1 | `OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS` | OPEN — causal attribution not established; G monitoring; R7 follow-up | R7's `FINAL` is a further monitoring run; its result is reported with the R7 acceptance evidence | retain with owner author (`AA`); characterization only if it recurs | yes |
| O-2 | `OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` | as O-1 | as O-1 | as O-1 | yes |

### 5.2 Rows owned by the author with `PRE-G9B-R7` as next checkpoint

| Row | Item | Recorded status | R7 proposal (future owner) |
|---|---|---|---|
| B-1 residue | `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (SC-03 to SC-05, SC-16, SC-23, SC-26) | OPEN — RETAINED WITH OWNER — NON-BLOCKING | keep author; a future bounded document-session lifecycle-contract activity when the author authorizes it |
| P-4 | `OBS-R6P-SAVE-WARNING` | OPEN — SPECIFICATION / IMPLEMENTATION DISCREPANCY | keep author; the requirement is not waived; a separately authorized bounded resolution (amend `locus-v2-public-surface.md` §609-612 or wire `confirmSave`) |
| P-7 | `OBS-A1-GRAPHICS2-GEOCEDG-MODES` | ACCEPTED LIMITATION (author) | keep; name a frontend owner only if Graphics View 2 parity is wanted |
| P-10 | `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | DOCUMENTARY DISCREPANCY — RETAINED | keep author; an annotation only by separate authorization |
| P-16 | `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION` | OPEN — RETAINED WITH OWNER | keep author; a future user-library policy decision |
| P-18 | undisposed C and E3 amendments in approved specifications | NORMATIVE DISPOSITION DEFERRED — AUTHOR OWNER | keep author; per clause promotion, withdrawal or supersession (the same pattern affects the A7 wording in `cedg-workspaces.md`, see the [roadmap review](../architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md)) |
| D-9 | POST-E2-P1 Option B (configurable physical defaults) | preferred future enhancement — not authorized | owner proposal: a future native-dimensions preferences enhancement (author-named) |
| L-7 | `OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED` (tracking) | ACCEPTED LIMITATION; tracking owner pending | tracking owner G15 (as proposed by the register) |
| O-3 | `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` | OPEN — NON-BLOCKING | keep author (inherited) |
| O-4 | `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` | OPEN — NON-BLOCKING | G15 |
| O-5 | `OBS-D1-ROLLBACK-CONSPROT-GUI` | OPEN — NON-BLOCKING | keep author |
| O-6 | `OBS-F3-PROTECTED-LABELS` | OPEN — NON-BLOCKING | the future insertion and styles activity (with D-1 to D-7) |
| O-7 | `OBS-F3-TEXT-THICKNESS` | OPEN — NON-BLOCKING | a future serialization owner (author-named) |
| O-8 | `OBS-F3R2-PARTIAL-RESTYLE` | OPEN — NON-BLOCKING | the future insertion and styles activity |
| O-9 | `OBS-F3R2-INHERITED-STALE-SELECTION` | OPEN — NON-BLOCKING | a characterization activity (author-named) |
| O-10 | `OBS-F4P-FREEHEP-XREF-SIZE` | OPEN — INDEPENDENT | G15 (with `TD-PDF-1`) |
| O-11 | AutoCAD releases other than 2026 | NOT RECHECKED — COVERAGE GAP | keep author (optional validation) |
| O-12 | `OBS-R6PLUS-E2-ALIGNDRAWTEXT-HASCHANGED` | OPEN — NON-BLOCKING | keep author |
| O-13 | `OBS-R6P0-ROTATE-TEXT-POINT` | OPEN — NON-BLOCKING | keep author |
| O-14 | `OBS-R6P0-CONSTRUCTION-XML-SWALLOW` | OPEN — NON-BLOCKING | keep author (proposed R7 by the register; R7 has no implementation scope) |
| O-15 | `OBS-R6P0-STALE-REFERENCE-FLAG` | OPEN — NON-BLOCKING | keep author |
| O-16 | `OBS-E1P-SMOKE-READER-LOG` | OPEN — NON-BLOCKING | keep author |
| O-17 | `OBS-E1P-USERSTORE-LOCK` | OPEN — NON-BLOCKING | keep author |
| O-18 | `OBS-E1P-SOURCE-REVISION` | OPEN | keep author |
| O-19 | `OBS-D1-INSTALLED-PREFERENCES-IN-TESTS` | OPEN — NON-BLOCKING | keep author (test-harness hygiene; a future verification-maintenance task) |
| O-20 | `OBS-D1-INHERITED-HEADLESS-LOGGER` | OPEN — NON-BLOCKING | as O-19 |
| O-21 | `OBS-D1-ANIMATING-DOCUMENT-RETENTION` | OPEN — NON-BLOCKING | as O-19 |
| O-22 | `exportPDF` callback UTF-8 read | OPEN — NON-BLOCKING | G15 |
| O-23 | `AutoColor` JVM-global state | OPEN — NON-BLOCKING | as O-19 (affects output digests in tests) |
| O-24 | historical G9X1 authority scan | OPEN — NON-BLOCKING | keep author; tied to P-12 and to the historical-verifier task proposed there |
| O-25 | host `UnicodeTeX` mapping of other characters | OPEN — NON-BLOCKING | G15 |
| O-26 | standing verifier diagnostics | OPEN — standing | keep author; they reappear on R7's `STATIC` and `FINAL` and stay verdict-neutral |
| O-27 | off-EDT construction race (`exportImageRows` NPE) | OPEN — monitored | as O-1 (R7 `FINAL` monitoring) |

Rows owned by G11 (B-2, P-2, P-5, P-6, P-8), G12 (D-10), G15 (P-3, D-8) and the future
insertion and styles activity (D-1 to D-7) keep their approved owners; the
[roadmap review](../architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md) folds the gate
owners into the proposed gate wording. Their implementation stays unauthorized.

## 6. Documentary discrepancies between records

Recorded, not repaired in historical records:

1. `BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION`: fixed by `eab5a7937`; the R1 closeout and
   the roadmap `PRE-G9B-R1` row still state it as retained (§4).
2. `GUIDE-A1-SECTION-9.3-STALE`: corrected in R2-E0/E1, carried as open drift from R3 to R6 (§4).
3. `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST`: retained (R6 ledger) vs resolved
   for new objects (register R-17) (§4).
4. `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`: never closed by identifier; the roadmap R0 debt
   table still shows `NEW` (§2).
5. `TD-G9X1-HISTORICAL-PIN` was closed with "the standalone G9X1 verifier passes"; since
   `PRE-G9B-R6-plus-B` the historical G9X1 authority scan fails at its base on a scanned
   GeoCeDG file (register O-24). This is a later, separate observation; it does not reopen
   the R1 closure.
6. `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`: resolved, but four living documents and one
   historical verifier still name it open (`OBS-R7-G9R4-LIVING-DOC-DRIFT`, §4).
7. The staged design requires every earlier phase "closed as `PASS — AUTHOR APPROVED`";
   `PRE-G9B-R1` is `AUTHOR APPROVED FOR PROMOTION — RETAINED PRE-EXISTING BASELINE DEFECT`
   with `passClaimed = false`. The R7 authorization resolves this with "closure, or
   explicit author disposition" and keeps R1's rejected `FINAL` and its author exception
   as distinct facts ([phase-entry record](pre_g9b_r7_phase_entry_record.md) §3).
8. The R7 stub's nine-item list omitted every item opened after R0; the execution prompt
   now covers them by rule.
9. Register vs G closeout: §5 of the G closeout puts P-4, P-10 and P-18 at the R7
   checkpoint, while their register owner cells name only the author; P-7 names R7 in the
   register but is absent from §5; register §4 says B-3 was approved "on T''" while row B-3
   and the G closeout say T'. All are reconciled here by owner (§5.2); the register is not
   edited.
10. Publication state: the G closeout record, the mini-track plan and the roadmap said
    "unpublished"; the closeout is `main` `b9bdb1e1` since 2026-10-15. Registered in the
    living roadmap and plan by R7; the closeout record keeps its pre-publication text.
11. The R6 ledger lumps "`OBS-R5B-*` retained", erasing R5-B's distinct statuses; §3 keeps
    the R5-B statuses.
12. Register row D-8 names `TD-PDF-1` as `OBS-POST-E2-P1-PDF-TEXT-SIZE`; its origin record
    names it `OBS-POST-E2-P1-R1-PDF-TEXT-SIZE` ([POST-E2-P1-R1 closeout](post_e2_p1_r1_closeout_record.md)).
    `TD-PDF-1` is the identifier both use; the register is not edited.
13. Approved behaviour still carries "pending" markers inside approved specifications
    (P-18): SP `units/unit-system.md`, `export/dxf-curve-fidelity-and-approximation.md`,
    `export/geometry-export-foundation.md` and the A7 wording of `ui/cedg-workspaces.md`;
    retained under P-18 (§5.2).

## 7. Author decisions requested for the debt record

| Decision | Items | Options | R7 recommendation |
|---|---|---|---|
| DEC-R7-D1 | `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | A `ACCEPTED LIMITATION`; B `DEFERRED REFINEMENT — RETAINED WITH OWNER` (author) for a future authorized construction-order activity | B, unless the author no longer wants the refinement |
| DEC-R7-D2 | confirmations by identifier: `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`, `BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION`, `GUIDE-A1-SECTION-9.3-STALE`, `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` (new objects) | confirm `RESOLVED` as proposed, or keep open | confirm, citing the evidence of §2 and §4 |
| DEC-R7-D3 | the `AL` proposals of §3 and §4 | accept as `ACCEPTED LIMITATION`, or keep `RETAINED WITH OWNER (author)` | accept the inherited, upstream, harness and fail-closed items; keep the others retained |
| DEC-R7-D4 | P-12 and P-13 (pinned headers) | terminal cross-reference retention, or a separately authorized historical-verifier re-pin task | terminal cross-reference retention |
| DEC-R7-D5 | future owners of §5.2 | adopt the proposed owners, or name others | adopt; gate owners authorize nothing |
| DEC-R7-D6 | `OBS-R7-G9R4-LIVING-DOC-DRIFT`, `OBS-R7-UNREGISTERED-HISTORICAL-SUITES` | authorize a bounded documentary correction (G9-R4 wording) and a verification-maintenance decision, or retain | authorize the documentary correction with the R7 closeout; retain the suites item |

Until the author decides, every item above remains in its recorded status with the
owner shown, which satisfies the R7 ownership requirement.

## 8. Author decisions (2026-10-15)

Adopted by the author instruction "GeoCeDG — PRE-G9B-R7 FINAL AUTHOR APPROVAL, DECISION
ADOPTION AND DOCUMENTARY CLOSEOUT", which accepts the final decision review including its
modifications ([closeout record](pre_g9b_r7_closeout_record.md) §3). These dispositions
prevail over the proposals of §1–§7, which stay as the candidate's history. Historical
closeout records and verdicts are not edited.

### 8.1 DEC-R7-D1 — construction-protocol reorder

`TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER = OPEN — CORRECTION DEFERRED — AUTHOR DECIDED`;
owner `AUTHOR — ACCOUNTABLE`. A future, separately authorized correction is required; it must
characterize and preserve constructive dependencies, semantic identity, DAG invariants,
persistence, undo/redo and compatibility, and is not a visual reordering. The historical R3
classification `deferred refinement` stays in the R3 records. Neither option A nor B of §7 is
adopted; no phase number is assigned; no correction is authorized within R7.

### 8.2 DEC-R7-D2 — resolutions confirmed by evidence

| Item | Author disposition | Evidence |
|---|---|---|
| `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY` | `RESOLVED BY PRE-G9B-R3 (D3-b)` | R3 closeout ("`D3-a` and `D3-b` resolved"); R0 mirror maps D3-b to this identifier |
| `BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION` | `RESOLVED BY eab5a7937 — CONFIRMED BY ACCEPTED R7 FINAL` | `.gitattributes` `text eol=lf` for both guides; `PostP1BilingualUserGuideTest` 16/16 in `verification-58826f64…`; the R1 FINAL verdict and author exception are unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | `RESOLVED BY APPROVED PRE-G9B-R2-E0/E1` | both guide editions rewritten; `PreG9bR2E0SemanticEndpointAdmissibilityTest.userGuideWorkedExampleEndpointsHoldAsDocumented` passing in the R7 FINAL |
| `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | `RESOLVED FOR NEW OBJECTS BY PRE-G9B-R6-plus-A-1`; the layer behaviour of documents created before A-1 is an `ACCEPTED LIMITATION` | A-1 candidate report; register R-17 |

### 8.3 DEC-R7-D3 — individual classification

`ACCEPTED LIMITATION` (inherited, upstream, test-harness, documented-contract or fail-closed),
each limited to its recorded characterization:

| Item | Basis |
|---|---|
| `OBS-R2-PHASE-SELECTION-COHORT` | informational verification observation |
| `OBS-R6-EVENT-DISPATCH-SELF-GEO-ON-ESCAPE` | inherited upstream |
| `OBS-R6-EXECUTE-INNER-FAILURE-NOT-PROPAGATED` | inherited `Execute` contract, recorded in the matrix |
| `OBS-R6-EXECUTE-ALIAS-SYNTAX-KEY` | presentation only |
| `OBS-R6-ROTATE-FORM-4-UPSTREAM-REFUSAL` | truthful upstream refusal |
| `HZ-R5B-ALIAS-CAS-STATE` | retained compatibility observation, not worsened |
| `HZ-R5B-HARNESS-TABLE-FRESHNESS` | test-harness caveat |
| `OBS-R5B-R1-SYNTHETIC-INPUT` | test-harness note |
| `OBS-R5B-R1-CAS-GROUP-READINESS` | test-harness note |
| `OBS-R5B-R2-EMBEDDED-HOST-ROOT` | test-harness note |
| `OBS-R5B-R2-COMPLETIONS-POPUP-LISTENER-ORDER` | no observed product failure |
| `CSolve` / `CSolutions` Algebra syntax | inherited, outside R5-B |
| `OBS-R5A-LATENT-NESTED-LIST-CONTINUATION` | non-live renderer behaviour |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | inherited upstream restore path |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | inherited upstream restore path |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | inherited generic XML paste |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | documented constraint |
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION` | documented `SplineV2` contract |
| `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` | documented `SplineV2` contract |
| `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY` | documented `SplineV2` tool contract |
| `CLIPBOARD-COPY-POINT-DRIVEN-LOCUS-SLICE` | fail-closed capability boundary |
| `A3-PAIR-SOURCE-COMPATIBLE-STRUCTURAL-REDEFINE` | fail-closed capability boundary |
| `LOCUS-PARTICIPANT-AXIS-NOT-SERIALIZABLE` | fail-closed capability boundary |

`OPEN — RETAINED WITH OWNER` (author):

| Item | Note |
|---|---|
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | `OPEN — RETAINED WITH OWNER — FUTURE MIGRATION DECISION` (author change); technical debt that may need a future correction |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | historical-verifier pin; a reconciliation needs its own authorization |
| `OBS-R6-PLACEHOLDER-REGEX-REPLACEMENT` | candidate for a future GGBScript hardening |
| `OBS-R5B-CAS-TABLE-VERBATIM-HEADS` | future command-surface polish |
| `OBS-R5B-HARDCODED-SYNTAX-HEADS` | future command-surface polish |
| `OBS-R5B-PROVER-DETAILS-CAPTIONS` | future command-surface polish |
| `OBS-R5B-INPUT-BAR-INSERTED-TEXT` | future command-surface polish |
| `OBS-R5B-R1-NATIVE-AWT-CRASH-UNATTRIBUTED` | monitoring |
| `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL` | covered by the focused R7 PHASE run; a FINAL change needs its own authorization |
| `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION` | future frontend maintenance |

The R5-B dispositions of `OBS-R5B-R1-LANGUAGE-CHANGE-RETURNS-TO-MOVE` and
`OBS-R5B-R2-CLASSIC-CARET-READ` stand as recorded by R5-B. No wildcard classification is
applied beyond the entries listed.

### 8.4 DEC-R7-D4 — historical pinned headers

P-12 and the pinned remainder of P-13: `RETAINED AS HISTORICAL PINNED HEADER — CURRENT STATUS
RECORDED BY CROSS-REFERENCE` (§5.1); owner author. The files, their byte pins, the ADR and the
verifier scripts are unchanged; a re-pin is a separately authorized activity.

### 8.5 DEC-R7-D5 — planning owners

G15: O-4, O-10, O-22, O-25 and the L-7 tracking. Future insertion-and-styles activity: O-6,
O-8. Future characterization activity: O-9. Future serialization owner: O-7. Future
verification-maintenance activity: O-19, O-20, O-21, O-23. Author: every other item of §5.2
(B-1 residue, P-4, P-7, P-10, P-16, P-18, D-9, O-1 to O-3, O-5, O-11 to O-18, O-24, O-26,
O-27). The author stays accountable until each unnamed activity is formally established.
Planning dispositions only; no implementation is authorized. P-4 remains a specification /
implementation discrepancy that is not waived; P-18 remains a pending normative decision.

### 8.6 DEC-R7-D6 — G9-R4 drift and historical suites

`OBS-R7-G9R4-LIVING-DOC-DRIFT`: a separate bounded documentary task, which may be planned with
P-18 and DEC-R7-G9 while keeping their distinct authorizations; no normative specification is
edited by the R7 closeout. `OBS-R7-UNREGISTERED-HISTORICAL-SUITES = OPEN — RETAINED WITH
OWNER` (author); any verification-maintenance activity needs its own authorization.

### 8.7 State at closeout

Every item of this view has a terminal disposition or an explicit retained state with an
accountable owner; 0 items lack an owner. Future corrections stay `OPEN`.

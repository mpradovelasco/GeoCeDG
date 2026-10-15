# PRE-G9B-R6-plus-G — structured debt and observation register

```text
REGISTER_KIND              = STRUCTURED R6-plus DEBT AND OBSERVATION REGISTER (G, area E)
FRAMEWORK                  = 134 entries, categories B/P/D/O/L/R and blocking columns,
                             adopted by the author (DQ-G-6, 2026-10-14)
DISPOSITION AUTHORITY      = G author design closeout record §4 (2026-10-14)
CATEGORY COUNTS            = B 3, P 18, D 10, O 27, L 31, R 45
ROWS GATING THE GLOBAL CLOSEOUT = 25 (author decision required before the global
                             PRE-G9B-R6-plus closeout)
G-R1 UPDATE (2026-10-15)    = rows B-1, B-2, B-3, P-9 and P-13 updated; 24 rows still await an
                             author decision (consolidated G-R1 decision sheet)
G CLOSEOUT (2026-10-15)     = author dispositions of the 25 gate rows applied; 0 rows await a
                             global-closeout decision; every retained OPEN or DEFERRED row
                             has an accountable owner (G closeout record)
OBS-R6P-SAVE-WARNING       = OPEN (kept open by the design closeout §4.2)
PER-ITEM DISPOSITIONS      = only those recorded by the author; every other pending item
                             keeps an explicit author-disposition requirement
selfApproved               = false
```

This register implements area E of the
[G implementation authorization](pre_g9b_r6_plus_g_implementation_authorization_record.md).
Its rows are the 134 entries of the
[debt and observation disposition proposal](pre_g9b_r6_plus_g_debt_and_observation_disposition_proposal.md)
(the adopted framework); the evidence of each row stays in that proposal and is not
copied here. The dispositions recorded by the author in the
[G author design closeout record](pre_g9b_r6_plus_g_author_design_closeout_record.md)
§4.1 are applied, each limited to its tested and approved scope. Machine-readable
mirror: [`pre-g9b-r6-plus-g-debt-register.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-debt-register.json).

## 1. Rules applied

1. **No silent promotion.** A row whose proposed category differs from its recorded
   status keeps its recorded status, marked `PROPOSED — AUTHOR CONFIRMATION REQUIRED`,
   until the author decides it (L-17, L-26, L-27, P-16, P-17, R-5). The category column
   keeps the framework position so that the 134-entry structure stays stable.
2. **Author dispositions applied.** `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`
   (R-4), `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` (R-1),
   `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` (R-2) and the five F4 reconciliation risks
   (R-6 to R-10), as recorded in the design closeout §4.1.
3. **Kept open.** `OBS-R6P-SAVE-WARNING` (P-4), the user-library collision (P-16),
   `OBS-F4P-FREEHEP-XREF-SIZE` (O-10) and both document-replacement observations (B-1,
   B-2). Previously approved closures and accepted limitations stay as recorded.
4. **Planning owners.** The author's planning owners are recorded: `TD-PDF-1` → G15
   after a physical-presentation contract-design activity (D-8); direct working-layer
   entry → G11 (P-2); manual `ExportArea` interaction → G15 (P-3); physical zoom aid →
   G12 (D-10); `DEFER-F3-*` → a future author-named insertion and styles activity (D-1 to
   D-7); full-suite instability → G monitoring and `PRE-G9B-R7` follow-up (O-1, O-2).
   **They authorize no implementation** and change no phase's approved scope.
5. **Proposed owners stay proposals.** Where the proposal suggested an owner the author
   has not decided, the owner cell says so; `PRE-G9B-R7` stops on an open item without an
   owner, so every non-R row needs an owner by R7 at the latest.
6. **Documentary items.** Class-1 corrections applied by G are marked `CORRECTED BY G`;
   classes 3 and 4 are `RETAINED` (`DQ-G-9`).
7. **Global gate** marks the rows whose author decision the design closeout §6.4 requires
   before the global `PRE-G9B-R6-plus` closeout: B-1 and B-2; B-3 through the approval of
   G; the P dispositions; the class-2 rows R-5, R-19 and R-33; and the proposed
   re-categorizations.

## 2. Register

| # | Item | Cat | Register status | Owner | Global gate | Author decision remaining |
|---|---|---|---|---|---|---|
| B-1 | `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | B | OPEN ROUTES CORRECTED BY G-R1 — AUTHOR APPROVED (SC-01, SC-02; the paste notice clears on successful E2/E3/E5 transitions, SC-11, accepted by the author); UNCONTRACTED RESIDUE — OPEN — RETAINED WITH OWNER — NON-BLOCKING: the export area of setBase64, non-native openFile/loadXML(File) and API setXML (SC-03 to SC-05), the cancelled v1 Open URL pre-clear (SC-16), the Move switch on a native Open (SC-23) and the embedded close (SC-26) have no approved contract and are not claimed compliant | author (accountable; future bounded document-session lifecycle-contract activity, not authorized; next checkpoint PRE-G9B-R7) | Yes — satisfied (G closeout) | a lifecycle contract for the uncontracted routes, by a future separate authorization |
| B-2 | `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` | B | DEFERRED — RETAINED WITH OWNER G11 (author decision, G closeout): working-layer recomputation and session-state ownership on tool replacement (SC-17, SC-18); export area, paste notice and drawing scale kept (SC-07, SC-12); ISO A link revalidated by identity (SC-10); unchanged by G-R1; NOT CORRECTED | G11 | Yes — satisfied (G closeout) | contract question for G11: whether a tool replacement keeps the session working layer (no G11 implementation authorized) |
| B-3 | `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` | B | RESOLVED — AUTHOR APPROVED on T' da1a09528a6f97254734400b12256e8fb20a6216 (PM-1 to PM-12; G-R1 acceptance ACCEPTED / COMPLETE; AUTHOR SMOKE PASS); no distribution right, commercial permission or public release implied | PRE-G9B-R6-plus-G | Yes — satisfied (G closeout) | none |
| P-1 | `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES` | P | ACCEPTED LIMITATION — AUTHOR DISPOSITION (G closeout): inherited Classic and adjacent off-EDT sites as characterized by C-X1 (1 of 100 Classic launches); Classic is a diagnostic surface; no product correction | author (accountable; revalidation if Classic becomes a supported user surface) | Yes — satisfied (G closeout) | none (revalidation trigger) |
| P-2 | `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | P | ENHANCEMENT — NOT IMPLEMENTED — IMPLEMENTATION NOT AUTHORIZED | G11 (planning owner, author decision of 2026-10-14) | No | implementation scope is the owner phase's own decision |
| P-3 | `ENH-B-MANUAL-EXPORT-AREA-DRAG` | P | OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15 (confirmed by the G closeout, 2026-10-15): an interactive rectangle-selection gesture updating the existing ExportArea(MANUAL) beside the numeric coordinate editor; no second export-area authority; IMPLEMENTATION NOT AUTHORIZED | G15 (planning owner, author decision of 2026-10-14) | No | implementation scope is the owner phase's own decision |
| P-4 | `OBS-R6P-SAVE-WARNING` | P | OPEN — SPECIFICATION / IMPLEMENTATION DISCREPANCY (author disposition, G closeout): locus-v2-public-surface.md:609-612 requires a save warning; GeoCeDGExternalCompatibilityWarning.confirmSave has no production caller; the requirement is not waived | author (a separately authorized bounded resolution activity is required) | Yes — satisfied (G closeout) | authorize the bounded resolution (specification amendment or wiring) |
| P-5 | `OBS-A1-3D-VIEW` | P | DEFERRED — RETAINED WITH OWNER G11 (per-view visibility; author decision, G closeout); no implementation authorized | G11 | Yes — satisfied (G closeout) | none until G11 |
| P-6 | `OBS-A1-NON-HIT-SELECTION` | P | DEFERRED — RETAINED WITH OWNER G11 (locking and filters; author decision, G closeout); no implementation authorized | G11 | Yes — satisfied (G closeout) | none until G11 |
| P-7 | `OBS-A1-GRAPHICS2-GEOCEDG-MODES` | P | ACCEPTED LIMITATION — AUTHOR DISPOSITION (G closeout): Graphics View 2 has no GeoCeDG mode parity, consistent with the accepted F1 exclusion L-29 | author (accountable; a frontend owner is named only if parity is wanted; next checkpoint PRE-G9B-R7) | Yes — satisfied (G closeout) | none (name a frontend owner if parity is wanted) |
| P-8 | `OBS-A1-BACKGROUND-IMAGE-LAYER` | P | DEFERRED — RETAINED WITH OWNER G11 (layer semantics of background images; author decision, G closeout); no implementation authorized | G11 | Yes — satisfied (G closeout) | none until G11 |
| P-9 | `OBS-F4P-S3-SPEC-HEADER` | P | RESOLVED — AUTHOR CONFIRMATION (G-R1, 2026-10-15): the status line NORMATIVE / AUTHOR APPROVED cites PASS — AUTHOR APPROVED on 543c77295fdc56ade60656a8d52829c7d6254cc7 and the S3 closeout report | PRE-G9B-R6-plus-G (documentary) | Yes — satisfied (G closeout) | none |
| P-10 | `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | P | DOCUMENTARY DISCREPANCY — RETAINED (author disposition, G closeout): the Amendment inside the accepted ADR 0035 prevails; the consequence line is not amended | author | Yes — satisfied (G closeout) | an annotation only by separate authorization |
| P-11 | `OBS-R6P-DXF-DEFAULT-DOC` | P | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (G closeout): status wording corrected (default-off kept as the G9X1 historical qualifier); AppConfigGeoCeDGTest:62 asserts the default true; documentary correction, no normative amendment | PRE-G9B-R6-plus-G (documentary) | Yes — satisfied (G closeout) | none |
| P-12 | `OBS-R6P-DXF-SPEC-HEADER` | P | DEFERRED — RETAINED WITH OWNER PRE-G9B-R7 (documentary reconciliation without altering historical pins or scripts; author decision, G closeout): the header is pinned by the historical G9X1 verifier | PRE-G9B-R7 | Yes — satisfied (G closeout) | none until PRE-G9B-R7 |
| P-13 | `OBS-R6P-DOC-IDENTITY-HEADER` | P | PARTLY RESOLVED: the ADR 0029 status line CONFIRMED BY THE AUTHOR (G-R1, 2026-10-15); the native-document-identity and ADR 0016 headers DEFERRED — RETAINED WITH OWNER PRE-G9B-R7 (byte-pinned by the G9U0-R2 verifier; pins and scripts unchanged) | PRE-G9B-R7 (pinned headers) | Yes — satisfied (G closeout) | none until PRE-G9B-R7 |
| P-14 | `OBS-R6P-ACTION-COUNT-DOC` | P | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (G closeout): the live catalog of 133 actions is asserted by G9U1ActionRegistryTest:65 and G9U1ProfileCompilerTest:44; residual qualifiers reconciled; documentary correction, no normative amendment | PRE-G9B-R6-plus-G (documentary) | Yes — satisfied (G closeout) | none |
| P-15 | `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | P | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (G closeout): the G9S1 SplineV2 sentence completes the six manifest purposes; documentary manifest correction | PRE-G9B-R6-plus-G (documentary) | Yes — satisfied (G closeout) | none |
| P-16 | `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION` | P | OPEN — RETAINED WITH OWNER (author disposition, G closeout): pre-existing; a language switch can make a valid stored user library unreadable on a future native-command collision; the user-library policy question is unresolved; no product correction | author (accountable; next checkpoint PRE-G9B-R7) | Yes — satisfied (G closeout) | a future user-library policy decision |
| P-17 | `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE` | P | ACCEPTED LIMITATION — AUTHOR DISPOSITION (G closeout): the Perimeter macro of Templatev7.ggb shadows the native command (document macros take precedence); pre-existing | author | Yes — satisfied (G closeout) | none |
| P-18 | Undisposed C and E3 specification amendments inside approved specifications | P | NORMATIVE DISPOSITION DEFERRED — AUTHOR OWNER (G closeout): the C and E3 amendment clauses are retained and not promoted | author | Yes — satisfied (G closeout) | per clause: promotion, withdrawal or supersession |
| D-1 | `DEFER-F3-TEMPLATE-DEFAULTS` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-2 | `DEFER-F3-M2` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-3 | `DEFER-F3-SCRIPTS` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-4 | `DEFER-F3-3D` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-5 | `DEFER-F3-UNLISTED-TYPES` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-6 | `DEFER-F3-IDENTITY-MACROS` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-7 | `DEFER-F3-FORENSIC-EXPORT` | D | OPEN — NOT AUTHORIZED (deferred) | future author-named insertion and styles activity (planning owner) | No | none until that activity |
| D-8 | `TD-PDF-1` (`OBS-POST-E2-P1-R1-PDF-TEXT-SIZE`) | D | OPEN — DEFERRED (contract design required); documented as a known limitation in the G guides | G15, preceded by a physical-presentation contract-design activity (planning owner) | No | none until that activity |
| D-9 | POST-E2-P1 Option B (configurable physical defaults) | D | RETAINED AS A PREFERRED FUTURE ENHANCEMENT — NOT AUTHORIZED | author (accountable; next checkpoint PRE-G9B-R7); proposed: a future native-dimensions preferences enhancement | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| D-10 | POST-E2-P1 physical zoom aid | D | FUTURE DESIGN POSSIBILITY — NOT AUTHORIZED | G12 (planning owner) | No | none until G12 |
| O-1 | `OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS` | O | OPEN — CAUSAL ATTRIBUTION NOT ESTABLISHED — monitored on the G candidate (result in the handoff); not recurred in the INTEGRATION runs of T and T' (2026-10-15) | G monitoring in its INTEGRATION run; PRE-G9B-R7 follow-up (planning owner) | No | characterization authorization if it recurs |
| O-2 | `OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` | O | OPEN — CAUSAL ATTRIBUTION NOT ESTABLISHED — monitored on the G candidate (result in the handoff); not recurred in the INTEGRATION runs of T and T' (2026-10-15) | G monitoring in its INTEGRATION run; PRE-G9B-R7 follow-up (planning owner) | No | characterization authorization if it recurs |
| O-3 | `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: author (inherited) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-4 | `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: G15 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-5 | `OBS-D1-ROLLBACK-CONSPROT-GUI` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: author | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-6 | `OBS-F3-PROTECTED-LABELS` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: G register retention | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-7 | `OBS-F3-TEXT-THICKNESS` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: future serialization owner | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-8 | `OBS-F3R2-PARTIAL-RESTYLE` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: future F3 hardening | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-9 | `OBS-F3R2-INHERITED-STALE-SELECTION` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: characterization activity | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-10 | `OBS-F4P-FREEHEP-XREF-SIZE` | O | OPEN — INDEPENDENT; preserved by the G design closeout §4.2 | author (accountable; next checkpoint PRE-G9B-R7); proposed: G15 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-11 | AutoCAD releases other than 2026 | O | NOT RECHECKED — COVERAGE GAP; the G guides state "AutoCAD 2026 only" | author (accountable; next checkpoint PRE-G9B-R7); author (optional) | No | optional author-controlled validation |
| O-12 | `OBS-R6PLUS-E2-ALIGNDRAWTEXT-HASCHANGED` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-13 | `OBS-R6P0-ROTATE-TEXT-POINT` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-14 | `OBS-R6P0-CONSTRUCTION-XML-SWALLOW` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-15 | `OBS-R6P0-STALE-REFERENCE-FLAG` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-16 | `OBS-E1P-SMOKE-READER-LOG` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-17 | `OBS-E1P-USERSTORE-LOCK` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-18 | `OBS-E1P-SOURCE-REVISION` | O | OPEN — build-manifest source_revision still has no dirty-tree flag; the G About shows the build state from the provenance | author (accountable; next checkpoint PRE-G9B-R7) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-19 | `OBS-D1-INSTALLED-PREFERENCES-IN-TESTS` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-20 | `OBS-D1-INHERITED-HEADLESS-LOGGER` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-21 | `OBS-D1-ANIMATING-DOCUMENT-RETENTION` | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-22 | `exportPDF` callback UTF-8 read (carried) | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: G15 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-23 | `AutoColor` JVM-global state (carried) | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-24 | Historical G9X1 authority scan (carried) | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-25 | Host `UnicodeTeX` mapping of other characters | O | OPEN — NON-BLOCKING | author (accountable; next checkpoint PRE-G9B-R7); proposed: G15 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-26 | Standing verifier diagnostics (governance `FINDING`, historical-consistency `UNAVAILABLE`) | O | OPEN — standing; expected again in the G STATIC and INTEGRATION runs; present again in the G-R1 STATIC and INTEGRATION runs | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| O-27 | Off-EDT construction race (`PreG9BR6CapabilityMatrixTest.exportImageRows` NPE) | O | OPEN — candidate report only; monitored on the G candidate (result in the handoff); not recurred in the INTEGRATION runs of T and T' (2026-10-15) | author (accountable; next checkpoint PRE-G9B-R7); proposed: PRE-G9B-R7 after G monitoring | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| L-1 | F4-A-L1 | L | ACCEPTED LIMITATION (as recorded); documented in the G guides §15 | — | No | none |
| L-2 | F4-A-L2 | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-3 | F4-A-L3 | L | ACCEPTED LIMITATION (revalidation trigger); G changes no runtime or look-and-feel | — | No | none |
| L-4 | `F4R-LINETYPE-COUPLING` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-5 | `F4R-DEFAULTS-UNDO` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-6 | F4-D retained notes (FreeHEP provenance families, inherited `FIXME`, JVM-wide Classic FreeHEP defaults, external tools) | L | RETAINED NOTES (as recorded); the G About states that third-party components keep their own terms; the component matrix still has no FreeHEP entry (pre-existing) | — | No | none |
| L-7 | `OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED` | L | ACCEPTED LIMITATION (as recorded) | author (accountable; next checkpoint PRE-G9B-R7); tracking owner pending (proposed G15) | No | future-owner decision at the PRE-G9B-R7 checkpoint (no implementation authorized) |
| L-8 | A-2 3D draw-order limitation | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-9 | `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-10 | `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY` (DXF side) | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-11 | `OBS-R6PLUS-E2-READING-FLIP-AT-VERTICAL` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-12 | `OBS-R6PLUS-E2-VALUE-LONGER-THAN-DIMENSION-LINE` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-13 | `OBS-R6PLUS-E2-OLDER-READER-OUTPUTS-SURVIVE` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-14 | `OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-15 | `OBS-E2-1` / `P1-DEFAULT-UX-LIMITATION` | L | ACCEPTED LIMITATION (POST-E2-P1 closeout); listed twice by the readiness package | — | No | none |
| L-16 | `DQ-D1-10` older-reader limitation | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-17 | `OBS-D1-DEFAULTS-SAVE-RELEVANT` | L | ACCEPTED LIMITATION — AUTHOR DISPOSITION (G closeout; recategorized from O): a new document whose defaults set a physical unit is save-relevant (DQ-D1-5) | author | Yes — satisfied (G closeout) | none |
| L-18 | `OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-19 | `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-20 | `C-SMOKE-1` `EXTERNAL_READER_LIMITATION` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-21 | F2/F2-R1 platform and interaction limitations | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-22 | `OBS-F3R2-SILENT-NO-OP` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-23 | F3 real-window isolation (spec §9.3) | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-24 | `F3R-RECOVERY-FAILURE` | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-25 | `F3R-*` closed with explicit scope restriction (`F3R-ATOMICITY`, `-UNDO-EQUAL-CONTENT-PENDING`, `-UNDO-HISTORY-COPY-SEMANTICS`, `-UNDO-SNAPSHOT-OWNERSHIP`, `-DOCUMENT-IDENTITY`, `-SAVED-STATE`, `-REDO-PRESERVATION`, `-MACRO-M1` (MU-1), `-LABEL-L1`, `-TEMPLATE-T0`, `-INHERITED-SESSION-EFFECTS`, `-INHERITED-STORE-RACE`) | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-26 | `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` | L | ACCEPTED LIMITATION — AUTHOR DISPOSITION (G closeout; recategorized from R): the documented binary64 divergence of the E3 canonical unit-factor order (DQ-E3-5) | author | Yes — satisfied (G closeout) | none |
| L-27 | `OBS-A1-RECOMPUTE-OUTPUTS` | L | ACCEPTED LIMITATION — AUTHOR DISPOSITION (G closeout; recategorized from R): the A-1 output recomputation, no correction recorded | author | Yes — satisfied (G closeout) | none |
| L-28 | E3 known limitations (no DXF sheet label; builds without `IsoABorder` lose sheets on re-save; no title block, sheet set, paper space or viewport; `OTQ-E3-2` to `OTQ-E3-4`) | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-29 | F1 approval exclusions: routes `X1`–`X10` and Graphics View 2 | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-30 | Native dimensions: "dimension inside a user macro" (bare value) and "`Corner` of the value text" | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| L-31 | POST-E2-P3 macro lifetime; POST-E2-P2-R1 AltGr evidence limitation | L | ACCEPTED LIMITATION (as recorded) | — | No | none |
| R-1 | `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | R | RESOLVED BY THE IDENTITY-VALIDITY SOLUTION — AUTHOR DISPOSITION (G design closeout §4.1); no rebuild signal was added | — | No | none |
| R-2 | `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | R | RESOLVED — AUTHOR DISPOSITION (G design closeout §4.1) | — | No | none |
| R-3 | `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` | R | RESOLVED — PUBLISHED (plan label reconciled by G, class 1) | — | No | none |
| R-4 | `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` | R | RESOLVED — AUTHOR DISPOSITION (G design closeout §4.1): naming and coexistence; the future-collision mode stays with P-16 | — | No | none |
| R-5 | Desktop test-heap pressure = `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION` | R | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (G closeout): addressed by the approved E2 implementation; final.desktop ran 2144 (T) and 2153 (T') tests without failures or out-of-memory; new host-creating tests use the lifecycle owner | — | Yes — satisfied (G closeout) | none |
| R-6 | `F4R-G2-VIEW-CLASS` | R | CLOSED — NOT REALIZED (G design closeout §4.1) | — | No | none |
| R-7 | `F4R-A1-REBUILD` | R | CLOSED — HELD BY TEST (G design closeout §4.1) | — | No | none |
| R-8 | `F4R-INHERITED-FREEHEP` | R | CLOSED — PERIMETER VERIFIED AND REGISTERED (G design closeout §4.1) | — | No | none |
| R-9 | `F4R-CLASSIC-PERIMETER` | R | CLOSED — HELD, within the tested scope (G design closeout §4.1) | — | No | none |
| R-10 | `F4R-READER-COMPAT` | R | CLOSED FOR THE CHECKED READERS (G design closeout §4.1); the xref repair stays O-10 | — | No | none |
| R-11 | `OBS-R6P0-PLAN-STATUS-STALE` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-12 | `OBS-R6P-CTRL-SHIFT-D` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-13 | `OBS-R6P0-HIDDEN-EXPORT-ROUTES` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-14 | `OBS-R6P0-CLI-EXPORT-ZERO-SIZE` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-15 | `OBS-R6P0-LOCUSV2-SCREEN-TESSELLATION` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-16 | `OBS-R6P-DXF-VISIBLE-CONTRACT` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-17 | `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-18 | `OBS-A1-URL-OPEN` | R | RESOLVED FOR THE LAYER PART (A-2); the session part is carried by B-1 | — | No | none |
| R-19 | `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | R | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (G closeout): closed under persistent reopen in the approved A-2 candidate | — | Yes — satisfied (G closeout) | none |
| R-20 | `OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-21 | `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-22 | `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-23 | `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-24 | `DEBT-R6PLUS-FILE-INSERT-SURFACE` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-25 | `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` with X1-F1, X1-F2 and X1-F3 | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-26 | `OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-27 | `OBS-E2-2` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-28 | `OBS-E2-3` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-29 | `OBS-E2-4` = `OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-30 | `OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-31 | `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-32 | `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-33 | `OBS-R6PLUS-E2-DEPENDENT-DRAG-MOVES-MEASURED-POINTS`; `OBS-R6PLUS-E2-LATEX-TEXT-PACKAGE`; LaTeX side of `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY` | R | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (G closeout): approved with E2 (Desktop drag interception; plain-text LaTeX; LaTeX dimension outputs); the DXF side stays L-10 | — | Yes — satisfied (G closeout) | none |
| R-34 | `OBS-E3-DXF-INVALID-OUTPUT`, `OBS-E3-LATEX-EXPORT` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-35 | `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE`, `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-36 | `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-37 | `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-38 | `FR-2` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-39 | `OBS-F3R2-SHARED-UUID`; `OBS-F3R1-TEST-LIFECYCLE-MASKING`; `OBS-F3R1-LAUNCHER-ROUTE`; `F3R2-T0-CONTRACT-DISCREPANCY`; F3-R1 and F3-R2 findings | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-40 | `F3R-*` product-verified (`-UNDO-PENDING-ACCOUNTING`, `-UNDO-ATTACH-RACE`, `-RECOVERY-SOURCE-CAPTURE`, `-NATIVE-RECOVERY-SNAPSHOT`, `-EDT`, `-FACTORY-LIFECYCLE`, `-EXCEPTION-PRESERVATION`, `-SPECIALIZED-*`, `-CLIPBOARD-D1`) | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-41 | F3 Apply Template re-layering | R | MOOT — CLOSED UNDER T0 (plan label reconciled by G, class 1) | — | No | none |
| R-42 | `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` (AutoCAD 2026) | R | CLOSED — RESOLVED FOR AUTOCAD 2026 (DQ-F4-6; plan label reconciled by G, class 1); other releases are O-11 | — | No | none |
| R-43 | `TD-PDF-2`, `TD-PDF-3` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-44 | `F4R-POPUP-FOCUS`, `F4R-CLASSIC-LEAK` | R | RESOLVED (as recorded by its approved record) | — | No | none |
| R-45 | `F4R-EMBED-FIDELITY`, `F4R-SHAPES-UNCHANGED`, `F4R-FAILED-EXPORT-REPORTING` | R | RESOLVED (as recorded by its approved record) | — | No | none |

## 3. Decisions still required from the author

Before the global `PRE-G9B-R6-plus` closeout (the rows with global gate `Yes`):

1. B-1 and B-2: dispositions on the evidence of the
   [characterization report](pre_g9b_r6_plus_g_document_replacement_characterization_report.md).
2. B-3: approval of the exact G technical candidate (and its author smoke).
3. P-1, P-5 to P-8, P-16, P-17: disposition and owner; P-4 (`OBS-R6P-SAVE-WARNING`):
   contractual or implementation disposition.
4. P-9, P-11, P-13 (ADR 0029 line), P-14, P-15: confirmation of the class-1 corrections
   and of the closure; P-10, P-12, P-13 (pinned headers), P-18: retention or a separate
   authorization.
5. R-5, R-19, R-33: the class-2 dispositions of the design closeout §4.3.
6. L-17, L-26, L-27: the proposed re-categorizations.

By `PRE-G9B-R7` at the latest: the owners of the O rows not covered by the planning
owners, D-9 and the tracking owner of L-7.

G-R1 (2026-10-15): the [consolidated author decision sheet](pre_g9b_r6_plus_g_r1_author_decision_sheet.md)
prepares the 24 remaining decisions in five groups. The author's G-R1 authorization
confirms P-9 and the ADR 0029 part of P-13; the Open-route part of B-1 is corrected by the
authorized G-R1 correction ([report](pre_g9b_r6_plus_g_r1_correction_report.md)).

This register closes, waives or re-owns nothing beyond the author's recorded decisions.

## 4. Author dispositions of the G closeout (2026-10-15)

The author's instruction "GeoCeDG — PRE-G9B-R6-plus-G Final Author Approval, Global R6-plus
Closeout and G11/G12/G15 Roadmap Reconciliation" disposes of every gate row individually
(rows above; [G closeout record](pre_g9b_r6_plus_g_closeout_record.md) §4):

| Group | Rows | Disposition |
|---|---|---|
| A — supported by approved implementation | P-11, P-14, P-15, R-5, R-19, R-33 | RESOLVED BY APPROVED EVIDENCE — AUTHOR DISPOSITION (documentary corrections are not normative amendments) |
| B — accepted limitations and retained observations | L-17, L-26, L-27, P-1, P-7, P-17 (ACCEPTED LIMITATION); P-16 (OPEN — RETAINED WITH OWNER); P-10 (DOCUMENTARY DISCREPANCY — RETAINED); B-1 residue (OPEN — RETAINED WITH OWNER — NON-BLOCKING) | retention within the characterized limits, owner recorded per row |
| C — explicit deferrals | B-2, P-5, P-6, P-8 (G11); P-12 and the pinned remainder of P-13 (PRE-G9B-R7) | DEFERRED — RETAINED WITH OWNER; no implementation authorized |
| D — pending normative corrections | P-4 (OPEN — SPECIFICATION / IMPLEMENTATION DISCREPANCY); P-18 (NORMATIVE DISPOSITION DEFERRED — AUTHOR OWNER) | retained; separately authorized resolution |
| E — mandatory About requirement | B-3 | RESOLVED — AUTHOR APPROVED on T'' |
| already disposed by G-R1 | P-9; the ADR 0029 part of P-13 | author confirmation |

Gate rows still awaiting an author decision: **0**. Every retained OPEN or DEFERRED row now
names an accountable owner; rows whose owner was undecided name the author, with
PRE-G9B-R7 as the next reconciliation checkpoint (not an implementation authorization).

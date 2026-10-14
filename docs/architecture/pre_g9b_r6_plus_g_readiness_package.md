# PRE-G9B-R6-plus-G — readiness and authorization package

| Field | Value |
|---|---|
| Status | **G READINESS — PREPARED — PENDING AUTHOR REVIEW** (documentary; no G approval) |
| Prepared by | Operation 3 of the author instruction "GeoCeDG — F4-A PUBLICATION + F4 AGGREGATE CLOSEOUT + G READINESS" (2026-10-14) |
| Prepared on | the F4 aggregate closeout `19f8f206b5ed7df8d203b3cebddd59eb1183d589` (tree `4d803c6a9f3f8db9cfeea2420d161835e08d7871`; STATIC `verification-152be358dba24759ab1af829e1ec2d84`), descending from the published `main` `07424dee6e5293c6259e84b97ca0ea3e6e5fc72f` |
| Canonical prompt | [candidate](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md) `PREPARED — PENDING AUTHOR REVIEW — NOT AUTHORIZED`; no G prompt existed before |
| Mirror | [`pre-g9b-r6-plus-g-readiness.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-readiness.json) |
| Boundaries | G implementation, global verification execution, G approval, the global `PRE-G9B-R6-plus` closeout, `PRE-G9B-R7`, `G9B`, release, binary publication and redistribution: **NOT AUTHORIZED**; `selfApproved = false` |

G is the planned help, integrated-verification and global-closeout phase of the
`PRE-G9B-R6-plus` mini-track ([plan](pre_g9b_r6_plus_minitrack_plan.md) §3.2, §12;
[roadmap](../roadmap/geocedg_roadmap.md), row `PRE-G9B-R6-plus-G`). The authorities used
are:
- the roadmap and the mini-track plan;
- the subphase closeout records;
- the documentation-maintenance contract;
- the licensing documents and the package profile;
- the executable verification registry.

The completion of F4 does not authorize G.

## 1. Prerequisite matrix

**Hard dependencies** (plan §3.1, unchanged):
`A-1, A-2, B, C, D1, E1, E2, E3, F1, F2, F3 ──► G`.

**Recommended predecessor:** `F3`.

**F4:** an author-added operational predecessor (planning decision of 2026-10-09: "to be
proposed between F3 and G"). It is **not** a hard semantic dependency, and this package
does not introduce one.

| Predecessor | Kind | State (latest record) | Approved implementation | In published `main` `07424dee` | Remaining limitations / notes |
|---|---|---|---|---|---|
| `A-1` | hard | `PASS — AUTHOR APPROVED — PUBLISHED` | `2a71133a` | yes | — |
| `A-2` | hard | `PASS — AUTHOR APPROVED — PUBLISHED` (`FINAL`) | `06a2ea6b` | yes | 3D draw-order presentation limitation; opened `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (§5) |
| `B` | hard | `PASS — AUTHOR APPROVED — PUBLISHED` | `848206c4` | yes | — |
| `C` (with `C-X1`) | hard | `PASS — AUTHOR APPROVED — PUBLISHED` | `6248bf0f`; `C-X1` `eabe6368` | yes | accepted pre-existing debt (Classic picture dialog) resolved by `C-X1` |
| `D1` (with `D0`) | hard | `PASS — AUTHOR APPROVED — PUBLISHED` (`FINAL`) | `6e1d8459`; `D0` `8383a153` | yes | — |
| `E1-L` | hard (via `E1`) | `PASS — AUTHOR APPROVED — PUBLISHED` | `5f34d181` | yes | curated library `experimental` |
| `E1-P` | hard (via `E1`) | `PASS — AUTHOR APPROVED` | `dcda4075` | **yes** (published with its closeout, E1-P record §7) | headline label lacks `PUBLISHED` (§2, finding R-1) |
| `E1` aggregate (with `E1-X1`, `E1-P-X1`) | hard | `PASS — AUTHOR APPROVED` | `E1-X1` `b12ab32c`, `E1-P-X1` `44b90156` | yes | — |
| `E2` (with POST-E2 P1–P4) | hard | `PASS — AUTHOR APPROVED — PUBLISHED` | `3a724661` | yes | `OBS-E2-1`..`4` dispositioned by POST-E2 |
| `E3` (with `E3-R1`) | hard | `PASS — AUTHOR APPROVED` | `7cd50116` | **yes** (via `POST-E3-INTEGRATION` `00b525fd`, published) | headline label lacks `PUBLISHED` (R-1); Asymptote-with-label verification limitation |
| `F1` | hard | `PASS — AUTHOR APPROVED — PUBLISHED` | `1cea3c89` | yes | approval excludes routes X1–X10 and Graphics View 2 |
| `F2` (with `F2-R1`) | hard | `PASS — AUTHOR APPROVED — PUBLISHED` | `488f1e99` | yes | — |
| `F3` (with `F3-R1`, `F3-R2`) | hard; recommended predecessor | `PASS — AUTHOR APPROVED — PUBLISHED` | `2d7b2cae` | yes | seven `DEFER-F3-*` debts and four F3 observations (§5) |
| `F4` (aggregate) | operational predecessor, not hard | `COMPLETE — AUTHOR APPROVED` (documentary, `19f8f206`, unpublished) | F4-A `6baa66a5`, F4-D `6214d15a` | implementations yes; aggregate closeout **no** | F4-A-L1..L3; `TD-PDF-1` deferred; `OBS-F4P-FREEHEP-XREF-SIZE` open |

Every approved implementation listed was checked to be an ancestor of the published
`main` on 2026-10-14.

### Readiness findings

| Id | Finding | Proposed handling |
|---|---|---|
| R-1 | Status-label drift. The E1-P and E3 headlines lack `PUBLISHED` although their commits are published. The plan's subphase table still shows `E2` and `F1` as `NOT AUTHORIZED` and `E3` without publication; F4 had no row until the aggregate closeout added one. | documentary reconciliation in G (`DQ-G-8`) |
| R-2 | The plan register still has "F3 Apply Template re-layering" at `DISPOSITION PENDING DQ-F3-9`. In fact the F3 design closeout decided `DQ-F3-9` as "layers never touched", and the F3 closeout approved T0 Apply Styles without the inherited Apply Template path. | record the item as moot under T0 (`DQ-G-8`) |
| R-3 | The user guide has no section on the PDF option "Export text as shapes" or on embedded fonts (F4-D made the embedded route work) | help gap for G (§3) |
| R-4 | No canonical version date exists. The visible version derives from `packaging/windows/package.yml`, a pinned static-contract input. | About design (`DQ-G-2`) |
| R-5 | The build provenance carries no distribution profile, so the About cannot state INTERNAL / NC / COMMERCIAL today | About design (`DQ-G-3`) |
| R-6 | Full-suite instability is recorded: `OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS` and `OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` | risk for G's `INTEGRATION` campaign (§6) |
| R-7 | The plan register still names F2 as the preferred owner of `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`. F2 kept it as a separate pending enhancement (`DQ-F2-9`), and the F2/F2-R1 closeout mirror lists it as not approved. It has no owner. | owner (`DQ-G-6`); label (`DQ-G-8`) |
| R-8 | The plan register still has `DQ-F4-6` pending for `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` and `DQ-F4-8` pending for `TD-PDF-1`. The F4 aggregate closeout records both as decided. | `DQ-G-8` |
| R-9 | Plan register labels left behind by approved closeouts: `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` still says "PENDING AUTHOR REVIEW" after E2's approval, and `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` still says "publication pending" although published. Separately, the E3 author-decision record leaves `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` and `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` `OPEN — NOT RESOLVED`. The approved E3 candidate report describes the unit gate and the identity-only link that address them, but the E3 closeout says nothing about either. | confirm against the approved E3 and E3-R1 code, then reconcile (`DQ-G-8`) |
| R-10 | Many R6-plus observations appear only in candidate or closeout reports, never in the plan register: the P0 `OBS-R6P*` items, `OBS-A1-*`, `OBS-B-*`, `OBS-D1-*`, minor E2 items, `OBS-E1P-*`, `OBS-F3R*`, and the F4R risks recorded only at reconciliation level. `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` is closed only in the A-2 candidate report. | the G structured register is the reconciliation target (§5) |

## 2. Proposed implementation base

The exact commit and tree named by the G implementation authorization. Recommended: the
published successor containing the F4 aggregate closeout `19f8f206` and this readiness
package. That requires two separate exact-SHA publication authorizations, or one naming
both (`DQ-G-10`). The base must contain every hard predecessor; all do already in
`07424dee`.

## 3. G scope and outstanding decisions

**Scope** (plan §3.2):
1. bilingual guide and in-product help for every accepted capability;
2. the About product metadata (§4);
3. `INTEGRATION` on G's own candidate, with no inherited evidence;
4. debt and observation reconciliation into a structured register (§5);
5. the global closeout record, pending author approval.

**Help gaps found so far** (preliminary keyword scan of the EN guide; G must confirm with
a coverage matrix): no mention of "Export text as shapes" or embedded PDF fonts (F4-D).

The other capabilities have guide entries:
- working layer and hidden layers;
- export area;
- construction unit and drawing scale;
- LaTeX/PGF export;
- the curated GGT library;
- native dimensions;
- `IsoABorder`;
- orientation cue;
- reference memory;
- Insert File and Apply Styles;
- the style-bar thickness field.

Their completeness is not yet established.

**Author decisions to prepare:**

| Id | Question | Options and recommendation |
|---|---|---|
| `DQ-G-1` | Verification class and acceptance profiles | **Recommended:** `INTEGRATED_PHASE`, with PHASE (G selection) + `INTEGRATION` + `INFRA_UNIT` + `STATIC`, plus `PACKAGING` (INTERNAL and NC) when packaging inputs change. `FINAL` (`GLOBAL_IMPACT`) only if a global verification-infrastructure change is found. |
| `DQ-G-2` | Source of the canonical version and version date | (a) a `version_date` field in `packaging/windows/package.yml`: a package-profile schema and static-contract change with packaging evidence; (b) a GeoCeDG product-metadata resource holding version and date, with the package version kept consistent by a check. **Recommended (b)**, if the author accepts a second declared field under one checked authority; otherwise (a). |
| `DQ-G-3` | Source of the distribution condition shown in About | the packaging writes the profile id into the build provenance; development builds show INTERNAL. COMMERCIAL is never shown as authorized. |
| `DQ-G-4` | When `1.1.0` and its date apply | **Recommended:** the G candidate carries `1.1.0` and the proposed date as the values the author approves with G. A post-approval version commit would change the approved product. |
| `DQ-G-5` | Disposition of `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (mandatory before the global closeout) | (i) characterize and correct inside G (product scope expansion); (ii) characterize and accept as a documented limitation; (iii) defer with an owner (e.g. `PRE-G9B-R7`). **Recommended:** a focused characterization at G entry, then (ii) or (iii) by the author. |
| `DQ-G-6` | Owners and dispositions for the items in §5 still pending or without an owner: `DEFER-F3-*`, F3 and F4 observations, `TD-PDF-1`, `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`, `ENH-B-MANUAL-EXPORT-AREA-DRAG`, the POST-E2-P1 Option B, items awaiting author disposition, and the Asymptote tracking | the G structured register proposes an owner and a disposition per item; the author decides |
| `DQ-G-7` | Depth of in-product help | guide sections plus existing tooltips and Input Help (recommended); new help surfaces only if needed |
| `DQ-G-8` | Documentary status reconciliation (R-1, R-2, R-7 to R-10) | inside G as documentation, every change traced to the closeout record that decided it (recommended) |
| `DQ-G-9` | Documentary observations owned by G or registered for it: `OBS-F4P-S3-SPEC-HEADER`, `OBS-F4P-ADR0035-CONSEQUENCE-LINE`, `OBS-R6P-DXF-DEFAULT-DOC`, `OBS-R6P-DXF-SPEC-HEADER`, `OBS-R6P-DOC-IDENTITY-HEADER`, `OBS-R6P-ACTION-COUNT-DOC`, `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | first check whether each still applies at the base; then correct it as documentation in G, or retain it |
| `DQ-G-10` | Publication of the F4 aggregate closeout and of this package before the G authorization | two exact-SHA publications, or one naming both |

## 4. Mandatory About / product-metadata requirements (`ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`)

These are author-approved requirements (2026-10-07; E1-L and E1-P closeouts; roadmap row G).

| # | Requirement | Current state (`07424dee`) | Obligation |
|---|---|---|---|
| M-1 | GeoCeDG-owned About surface | `geocedg.help.about` shows title, a description stating "author-review implementation candidate", GeoGebra upstream baseline, author, and a pointer to `LICENSE`, `NOTICE.md` and `THIRD_PARTY.md` | rebuild the content to M-2..M-7; update the description wording |
| M-2 | product name | `GeoCeDG` (`GeoCeDGProductInfo`) | keep |
| M-3 | canonical version | `1.0.0`, from `package.yml` through the build provenance | keep `1.0.0` until the G completion contract (`DQ-G-4`), then `1.1.0` |
| M-4 | canonical version date, distinct from the build date | **absent** | add under `DQ-G-2`; tests prove it differs from any build timestamp |
| M-5 | software license EUPL-1.2 where applicable | only via the pointer to `LICENSE` | state EUPL-1.2 for GeoCeDG-authored software (`LICENSES/EUPL-1.2.txt`) |
| M-6 | documentation and ordinary artwork CC BY 4.0 where applicable | not stated | state CC BY 4.0 (`docs/LICENSE.md`, `LICENSES/CC-BY-4.0.txt`, component matrix); brand policy for official artwork |
| M-7 | inherited components with independent terms | pointer only | state that GeoGebra and other inherited components keep their own terms (`NOTICE.md`, `THIRD_PARTY.md`, component matrix) |
| M-8 | INTERNAL / NC / COMMERCIAL distribution condition | not shown; profiles exist in `package.yml` (INTERNAL internal-evaluation, NC approved non-commercial, COMMERCIAL not authorized) | show the running build's condition (`DQ-G-3`); COMMERCIAL never shown as authorized |
| M-9 | Classic About unchanged | Classic uses the inherited About | regression test and author smoke |
| M-10 | visible version `1.0.0` preserved during preparation | unchanged by this package | no change before G |

Preparing G authorizes no public release, commercial redistribution, tag or binary
publication.

## 5. Open-debt and risk disposition matrix

Categories: **R** resolved by an author-approved implementation; **L** accepted
limitation; **D** deferred with owner (to be assigned); **O** open, non-blocking;
**B** blocking G entry or the global closeout; **P** pending author decision.

Each item's state is the latest one recorded in the
[plan](pre_g9b_r6_plus_minitrack_plan.md) §14 register or in the named closeout record.
**The category is only this package's proposal**; the G structured register and the
author decide it. No debt or observation blocks **G entry**. The decisions G needs
before implementation are `DQ-G-1` to `DQ-G-4` and `DQ-G-10` (§3).

### 5.1 Blocking the global closeout

| Item | Latest recorded state | Category | Proposed handling |
|---|---|---|---|
| `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` | `OPEN — NOT A-2 BLOCKING — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT` (A-2 closeout; plan §14); the C and C-X1 closeouts did not absorb it | **B** (global closeout) + **P** | `DQ-G-5`: a focused characterization at G entry, then correction, accepted limitation or deferral with owner. G stops at the closeout if it has no disposition. |
| `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` | `AUTHOR APPROVED REQUIREMENT (2026-10-07) — OWNER PRE-G9B-R6-plus-G — MANDATORY G INPUT` | **B** (mandatory G scope) | implemented by G under §4 and `DQ-G-2` to `DQ-G-4` |

### 5.2 Pending author decision

| Item | Latest recorded state | Category | Proposed handling |
|---|---|---|---|
| `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` | `A-2 BEHAVIOR — NOT C SCOPE — NOT FIXED — AUTHOR DISPOSITION` (C reconciliation; C and C-X1 closeouts) | **P** | disposition in the register; G may characterize it together with `DQ-G-5`, since both concern document replacement under A-2 |
| `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES` | `PRE-EXISTING — NOT FIXED — AUTHOR DISPOSITION` (C-X1, `DQ-X1-4`) | **P** | disposition and owner (`DQ-G-6`) |
| `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | `ENHANCEMENT — IMPLEMENTATION NOT AUTHORIZED`; not implemented in F2 (`DQ-F2-9`); no owner (R-7) | **P** | owner or deferral (`DQ-G-6`) |
| `ENH-B-MANUAL-EXPORT-AREA-DRAG` | "Not implemented in B" (B closeout); no later record | **P** | owner or deferral (`DQ-G-6`) |
| `OBS-R6P-SAVE-WARNING` | owner "author disposition" (P0 candidate report); no later record | **P** | check at the base, then decide |
| `OBS-A1-3D-VIEW`, `OBS-A1-NON-HIT-SELECTION`, `OBS-A1-GRAPHICS2-GEOCEDG-MODES`, `OBS-A1-BACKGROUND-IMAGE-LAYER` | owner author or "later frontend phase" (A-1 closeout; A-2 and B candidate reports); never dispositioned | **P** | disposition in the register |
| Documentary items owned by G: `OBS-F4P-S3-SPEC-HEADER`, `OBS-F4P-ADR0035-CONSEQUENCE-LINE`, `OBS-R6P-DXF-DEFAULT-DOC`, `OBS-R6P-DXF-SPEC-HEADER`, `OBS-R6P-DOC-IDENTITY-HEADER`, `OBS-R6P-ACTION-COUNT-DOC`, `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | registered with owner G (P0 candidate report, E2 preparation, F4-PREP) | **P** | `DQ-G-9` |
| F4 risks recorded only at reconciliation level: `F4R-G2-VIEW-CLASS`, `F4R-A1-REBUILD`, `F4R-INHERITED-FREEHEP`, `F4R-CLASSIC-PERIMETER`, `F4R-READER-COMPAT` | never dispositioned in a closeout | **P** | confirm closure or retention in the register |

### 5.3 Deferred; owner to be assigned

| Item | Latest recorded state | Category | Proposed handling |
|---|---|---|---|
| `DEFER-F3-TEMPLATE-DEFAULTS`, `DEFER-F3-M2`, `DEFER-F3-SCRIPTS`, `DEFER-F3-3D`, `DEFER-F3-UNLISTED-TYPES`, `DEFER-F3-IDENTITY-MACROS`, `DEFER-F3-FORENSIC-EXPORT` | `OPEN — NOT AUTHORIZED (owner assigned by the G structured debt register)` (F3 closeout) | **D** | owners (`DQ-G-6`) |
| `TD-PDF-1` (`OBS-POST-E2-P1-R1-PDF-TEXT-SIZE`) | `OPEN — DEFERRED` (`DQ-F4-8`; contract design required) | **D** | future contract phase as owner (`DQ-G-6`) |
| POST-E2-P1 Option B (configurable physical defaults) and the physical zoom aid | "retained as a preferred future enhancement; not authorized" (POST-E2-P1 closeout) | **D** | owner (`DQ-G-6`) |

### 5.4 Open, non-blocking

| Item | Latest recorded state | Category | Proposed handling |
|---|---|---|---|
| `OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS`, `OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` | `OPEN — NON-BLOCKING — CAUSAL ATTRIBUTION NOT ESTABLISHED`; correction not authorized | **O** | `INTEGRATION` risk R-6; characterize if they recur |
| `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` | `OPEN — PRE-EXISTING UPSTREAM BEHAVIOR — PRODUCT CORRECTION NOT AUTHORIZED` | **O** | register |
| `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` | `OPEN — PRE-EXISTING — OUTSIDE C1 — IMPLEMENTATION NOT AUTHORIZED` | **O** | register |
| `OBS-D1-ROLLBACK-CONSPROT-GUI` | `PRE-EXISTING — CHARACTERIZED — IMPLEMENTATION NOT AUTHORIZED` (D1 closeout) | **O** | register |
| `OBS-F3-PROTECTED-LABELS`, `OBS-F3-TEXT-THICKNESS`, `OBS-F3R2-PARTIAL-RESTYLE` | `OPEN — RETAINED — NON-BLOCKING` (F3 closeout) | **O** | register |
| `OBS-F3R2-INHERITED-STALE-SELECTION` | `OPEN — RETAINED — NOT CHARACTERIZED BEYOND T0` (inherited) | **O** | register; characterization candidate |
| `OBS-F4P-FREEHEP-XREF-SIZE` | `OPEN — INDEPENDENT` (F4 aggregate) | **O** | register |
| AutoCAD releases other than 2026 | `NOT RECHECKED — COVERAGE GAP` (F4-B) | **O** | optional author-controlled validation |
| `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION`, `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE`, `OBS-R6PLUS-E2-ALIGNDRAWTEXT-HASCHANGED` | pre-existing; unchanged; outside E2 (E2 candidate report) | **O** | register |
| `OBS-R6P0-ROTATE-TEXT-POINT`, `OBS-R6P0-CONSTRUCTION-XML-SWALLOW`, `OBS-R6P0-STALE-REFERENCE-FLAG` | upstream; no later disposition (P0; F2 preparation) | **O** | register |
| `OBS-E1P-SMOKE-READER-LOG`, `OBS-E1P-USERSTORE-LOCK`, `OBS-E1P-SOURCE-REVISION` | "no action proposed" (E1-P candidate report) | **O** | register |
| `OBS-D1-DEFAULTS-SAVE-RELEVANT`, `OBS-D1-INSTALLED-PREFERENCES-IN-TESTS`, `OBS-D1-INHERITED-HEADLESS-LOGGER`, `OBS-D1-ANIMATING-DOCUMENT-RETENTION` | "kept as recorded" (D1 closeout) | **O** | register |
| Carried test and tool debts (Desktop test-heap pressure, `exportPDF` callback UTF-8 read, `AutoColor` JVM-global state, historical G9X1 authority scan) | "unchanged; not absorbed by C" (C and C-X1 closeouts) | **O** | register with identifiers; check the heap item against the E2 test-only host-retention correction |

### 5.5 Accepted limitations

| Item | Source | Category |
|---|---|---|
| F4-A-L1, F4-A-L2, F4-A-L3 (L3 requires renewed verification after a runtime or look-and-feel change) | F4-A closeout | **L** |
| `F4R-LINETYPE-COUPLING`, `F4R-DEFAULTS-UNDO` (inherited); F4-D retained notes | F4 aggregate; F4-D closeout | **L** |
| `OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED`: "independent tracking required; no owner authorized" | E3/E3-R1 closeout | **L**; owner for the tracking (`DQ-G-6`) |
| A-2 3D draw-order presentation | A-2 closeout | **L** |
| DXF dimension subset (`OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET`, the DXF side of `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY`); `OBS-R6PLUS-E2-READING-FLIP-AT-VERTICAL`; `OBS-R6PLUS-E2-VALUE-LONGER-THAN-DIMENSION-LINE`; `OBS-R6PLUS-E2-OLDER-READER-OUTPUTS-SURVIVE`; `OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS` (intentional profile behaviour) | E2 closeout and candidate report | **L** |
| `P1-DEFAULT-UX-LIMITATION` (`OBS-E2-1`) | POST-E2-P1 closeout | **L** |
| D1 older-reader limitation (`DQ-D1-10`); `OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE`; `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` (`DQ-C14`); C-SMOKE-1 `EXTERNAL_READER_LIMITATION` | A-2 prompt closeout; B closeout; C candidate report and closeout | **L** |
| F2/F2-R1 platform and interaction limitations; `OBS-F3R2-SILENT-NO-OP` (approved behaviour; enhancement candidate); F3 real-window isolation; `F3R-RECOVERY-FAILURE`; `F3R-*` closed with explicit scope restriction, including `F3R-MACRO-M1` (MU-1) | F2/F2-R1 and F3 closeouts; F3 risk registers | **L** |

### 5.6 Resolved

These are resolved by author-approved work. Label reconciliation, where noted, is
documentary only.

- **P0 and A-1:**
  - `OBS-R6P0-PLAN-STATUS-STALE`;
  - `OBS-R6P-CTRL-SHIFT-D`, `OBS-R6P0-HIDDEN-EXPORT-ROUTES`, `OBS-R6P0-CLI-EXPORT-ZERO-SIZE` and `OBS-R6P0-LOCUSV2-SCREEN-TESSELLATION` (B);
  - `OBS-R6P-DXF-VISIBLE-CONTRACT` (C, `DQ-C3`);
  - `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` (A-1, for new objects);
  - `OBS-A1-RECOMPUTE-OUTPUTS` and `OBS-A1-URL-OPEN`;
  - `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` (closed in the A-2 candidate report only; R-10).
- **A-2, B, C and D1:**
  - `OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT`;
  - `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` (not a defect, `DQ-C10`);
  - `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` (`DQ-C9`);
  - `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` (C-X1);
  - `DEBT-R6PLUS-FILE-INSERT-SURFACE` (F3, C20-1).
- **E1:**
  - `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` with X1-F1 to X1-F3 (E1-X1);
  - `OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION` (E1-P-X1).
- **E2 and POST-E2:**
  - `OBS-E2-1` to `OBS-E2-4`, including `OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX`;
  - `OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM`;
  - `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` (F4-A, with F4-A-L1);
  - `OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE` (C1);
  - `OBS-R6PLUS-E2-DEPENDENT-DRAG-MOVES-MEASURED-POINTS`, `OBS-R6PLUS-E2-LATEX-TEXT-PACKAGE`, `OBS-R6PLUS-E2-DESKTOP-TEST-HOST-RETENTION`, and the LaTeX side of `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY`;
  - `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`: naming by `DQ-E2-7` and coexistence validated; label to reconcile (R-9).
- **E3 and POST-E3:**
  - `OBS-E3-DXF-INVALID-OUTPUT` and `OBS-E3-LATEX-EXPORT` (E3-R1);
  - `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE` and `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH` (E3);
  - `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY` (`DQ-E3-2`);
  - `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` (dispositioned, D0);
  - `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` (recorded);
  - `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` (correction approved; label to reconcile, R-9);
  - `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` and `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN`: probably resolved by the approved E3 unit gate and identity-only link, to be confirmed (R-9).
- **F2, F3 and F4:**
  - `FR-2` (O1 replacement);
  - `OBS-F3R2-SHARED-UUID`, `OBS-F3R1-TEST-LIFECYCLE-MASKING`, `OBS-F3R1-LAUNCHER-ROUTE`, `F3R2-T0-CONTRACT-DISCREPANCY`;
  - the `F3R-*` risks closed as product-verified;
  - F3 Apply Template re-layering (moot under T0, R-2);
  - `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` (AutoCAD 2026 only);
  - `TD-PDF-2`, `TD-PDF-3`;
  - `F4R-POPUP-FOCUS`, `F4R-CLASSIC-LEAK`, `F4R-EMBED-FIDELITY`, `F4R-SHAPES-UNCHANGED`, `F4R-FAILED-EXPORT-REPORTING`.

This package closes, waives and implements nothing. The inventory was collected on
2026-10-14 from the committed plan register and the subphase records. The G structured
register re-derives it on the G base.

## 6. Proposed verification class and rationale

`INTEGRATED_PHASE`: the plan §12 proposal, still subject to being frozen by the author
(`DQ-G-1`).

**Rationale:**
- G integrates accepted subphases, adds documentation and a GeoCeDG-owned About surface,
  and changes no kernel, geometry or document serialization.
- The plan requires `INTEGRATION` on G's own candidate, inheriting no evidence, not even
  D1's `FINAL`. In the executable registry, `FINAL` = `INTEGRATION` ∪ `INFRA_UNIT`, so
  `INTEGRATION` + `INFRA_UNIT` + `STATIC` cover the global executable surface.
- A version or profile change reaches pinned static-contract inputs (`package.yml`) and the
  packaged product, which adds `PACKAGING` evidence as for E1-P.
- No R6-plus record consulted reports a global verification-infrastructure change. E1-P
  was `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, `PHASE_LOCAL`. G entry must confirm this.
  Otherwise the plan's escalation rule applies (`FINAL`).

**Risks:**
- R-6: two recorded intermittent full-suite effects could disturb `INTEGRATION`. They are
  not attributed and need characterization if they recur.
- External tools are outside the workstation contract (Ghostscript, Poppler, AutoCAD,
  LaTeX/Asymptote).

## 7. Canonical G execution prompt

[Candidate](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md),
status `PREPARED — PENDING AUTHOR REVIEW — NOT AUTHORIZED`.
- It is execution-safe under the task prompt contract (`Test-PromptContractDocument`:
  no missing, duplicate, unknown or out-of-order field).
- It covers the base, the About requirements, help integration, integrated validation,
  the global-closeout prerequisites, the debt dispositions, the author smoke, the
  publication and release boundaries, and the stop conditions.

## 8. Author authorization checklist

Before a G implementation can start, the author would need to:

1. review this package and the prompt candidate;
2. decide `DQ-G-1` (class and acceptance profiles, frozen);
3. decide `DQ-G-2` and `DQ-G-3` (version-date and distribution-condition sources) and
   approve the About contract they imply;
4. decide `DQ-G-4` (when `1.1.0` and its date apply);
5. decide `DQ-G-5` for `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`
   (characterization first, then limitation, deferral or correction);
6. decide `DQ-G-6` to `DQ-G-9` (owners, help depth, status reconciliation, documentary
   observations);
7. authorize the publication of the F4 aggregate closeout and of this package, or name
   another exact base (`DQ-G-10`);
8. issue the G implementation authorization naming the exact base commit and tree.

The global closeout, `PRE-G9B-R7`, `G9B`, release, tags and redistribution each stay
separate decisions.

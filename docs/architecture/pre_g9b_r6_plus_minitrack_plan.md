# PRE-G9B-R6-plus — final refinement mini-track plan

- Status: **AUTHOR APPROVED PLANNING DESIGN — NO SUBPHASE AUTHORIZED**
- Recorded: 2026-10-01
- Track state: `PRE-G9B-R6-plus` = `PLANNING PASS — AUTHOR APPROVED / PRODUCT IMPLEMENTATION NOT AUTHORIZED`
- Author disposition: `PRE-G9B-R6-plus PLANNING = PASS — AUTHOR APPROVED` (2026-10-01) on `T_R6PLUS_PLAN` `147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f` as reconciled by `D_R6PLUS_PLAN` `af2aa1133620e49d68ac469ee8d10d273498cd95` ([closeout record](../validation/pre_g9b_r6_plus_planning_closeout_record.md))
- Planning base: `P_R6` = `9b8bc5b10a7ef61da0095853ea4c572ca06bfa72`, tree `f5c8c9f0cc76982170c6282a1242d2a5d07a0897`
- Change route: `ORDINARY`; frozen class of the planning task: `DOCUMENTATION_STATUS_ONLY`
- Claim vocabulary: **accepted planning direction**. No normative geometric,
  unit, layer or export contract is created here; the approval does not
  promote any specification or ADR
- Evidence record: [planning candidate report](../validation/pre_g9b_r6_plus_planning_candidate_report.md) and its machine-readable mirror `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-planning.json`
- First executable task: [`PRE-G9B-R6-plus-P0`](../../.github/prompts/tasks/pre-g9b-r6-plus-p0-integrated-characterization-and-design.prompt.md), `PASS — AUTHOR APPROVED` (2026-10-02) on `T_R6PLUS_P0` `6b7fd5de343b6556b384e71a9345907e7944d1e5` ([closeout record](../validation/pre_g9b_r6_plus_p0_closeout_record.md)); its design candidates stay candidates and its open author decisions stay open
- Author decisions for `A` (2026-10-02): [A author-decision record](../validation/pre_g9b_r6_plus_a_author_decisions_record.md). `A` is split into `A-1` (`INTEGRATED_PHASE`) and `A-2` (`GLOBAL_IMPACT`); the next subphase is `A-1`, whose [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md) is prepared on `562e2bb1e77b249b9c97c2e6e06e8b123d6de900`, tree `9f578093f2f3cba481f5630fe827fd3ef08cff61` and is `NOT AUTHORIZED`; the preparation package `T_R6PLUS_A1_PROMPT` `3c5aba19b2efd4b65d6f7b7deba6d3b19611630b` is `PASS — AUTHOR APPROVED` (2026-10-02, [closeout record](../validation/pre_g9b_r6_plus_a1_prompt_closeout_record.md)); implementation was authorized on 2026-10-02 on `069c7a03da92f2c30f015815a0aa278e7356b356` and `A-1` is `PASS — AUTHOR APPROVED` (2026-10-02, author smoke PASS, `T_R6PLUS_A1` `2a71133ad622e18e9963b04a466a92665a784722`; [closeout record](../validation/pre_g9b_r6_plus_a1_closeout_record.md)); the next operational subphase is `B`, `NOT AUTHORIZED`
- Author decisions for `B` (2026-10-02): [B author-decision record](../validation/pre_g9b_r6_plus_b_author_decisions_record.md) (`AQ-X1` to `AQ-X7`). The [canonical `B` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-b-export-surface-and-export-area.prompt.md) is prepared on `d32ad608ba8821bc18c9d4dc783c1b700a165ff2`, tree `9b123be7ed8c7176b5a7e19f74be5946f0164c8b`: `PREPARED — NOT AUTHORIZED`, proposed class `INTEGRATED_PHASE`; the preparation package `T_R6PLUS_B_PROMPT` `7e00e451167b0d915b061655c478c2faac99e51c` is `PASS — AUTHOR APPROVED` (2026-10-02, with the author dispositions on `DQ-B1` to `DQ-B9`, which prevail over the prompt's defaults; [closeout record](../validation/pre_g9b_r6_plus_b_prompt_closeout_record.md)); implementation was authorized on 2026-10-02 on `f6194f09358de9c5f8ac5051c31b3a9dc8a9491b`, the amended prompt is the execution contract, and `B` is `PASS — AUTHOR APPROVED` (2026-10-02, author smoke PASS, `T_R6PLUS_B` `848206c413eb4f78694c9149e153f6e867eb9525`; [candidate report](../validation/pre_g9b_r6_plus_b_candidate_report.md), [closeout record](../validation/pre_g9b_r6_plus_b_closeout_record.md)); the next operational subphase is `D0`, `NOT AUTHORIZED`
- Author decisions for `D0` (2026-10-02): [D0 author-decision record](../validation/pre_g9b_r6_plus_d0_author_decisions_record.md) (`AQ-U2` to `AQ-U6`, undo semantics, the unit-independence invariant, the persistence target for `D1` and the export-foundation amendment). The [canonical `D0` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-d0-normative-unit-system-design.prompt.md) is prepared on `0ef616bb7bb8efd3e4f19762f38936ff4742d1c3`, tree `be7ef7acbc3503002f3dac2bc33f047795f7a644`: `PREPARED — NOT AUTHORIZED`, class `DOCUMENTATION_STATUS_ONLY` (author-frozen; future acceptance `STATIC`); the preparation package `T_R6PLUS_D0_PROMPT` `8814468101e3caef45d9f6cbad4bf563523c3478` is `PASS — AUTHOR APPROVED` (2026-10-02, with the author dispositions on `DQ-D0-1` to `DQ-D0-13`, which prevail over the prompt's defaults; [closeout and authorization record](../validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md)); execution was authorized on 2026-10-02 on that unpublished package, the amended prompt is the execution contract, and `D0` is `PASS — AUTHOR APPROVED` (2026-10-02, `T_R6PLUS_D0` `8383a153dc2228fc08b4e00a0c92ef8025608916`; [candidate report](../validation/pre_g9b_r6_plus_d0_candidate_report.md); [closeout record](../validation/pre_g9b_r6_plus_d0_closeout_record.md); [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md) `ACCEPTED — AUTHOR APPROVED`; [unit-system specification](../../geocedg/specs/units/unit-system.md) v1.0 `NORMATIVE / AUTHOR APPROVED`); the next operational subphase is `D1`, `NOT AUTHORIZED`
- Author decisions for `D1` (2026-10-02): [D1 author-decision record](../validation/pre_g9b_r6_plus_d1_author_decisions_record.md) (frozen class, product boundary, architecture rule, stop conditions). The [canonical `D1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md) is prepared on `3268f9b99d20c5d9cf62f25d8066ee0a8e47765c`, tree `83b32b6cb579dc28162f990160c3eef0b1928be6`; preparation package `PASS — AUTHOR APPROVED` and implementation authorized on 2026-10-03 on `3fb7542a` ([closeout and authorization record](../validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md), `DQ-D1-1` to `DQ-D1-11`); class `GLOBAL_IMPACT` (author-frozen; acceptance by one `FINAL`), with its [characterization report](../validation/pre_g9b_r6_plus_d1_preparation_characterization_report.md) and its [candidate report](../validation/pre_g9b_r6_plus_d1_candidate_report.md): `PASS — AUTHOR APPROVED` (2026-10-03; `T_R6PLUS_D1` `6e1d8459`, revision 2 after the author smoke of `b55aa817`; `AUTHOR_SMOKE = PASS`; `FINAL` `verification-9cf1a0af…` `ACCEPTED / COMPLETE`; [closeout record](../validation/pre_g9b_r6_plus_d1_closeout_record.md))
- Author decisions for the `A-2` preparation (2026-10-03): [A-2 author-decision record](../validation/pre_g9b_r6_plus_a2_author_decisions_record.md) (kept class, normative scope, mandatory characterization, forbidden scope, stop conditions). The [canonical `A-2` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md) is prepared on the local `F3` planning commit `f467eb4563de7a7eb162da1b74954f9f847fa9d3`, tree `ece51c96117d6cbd55bc200d29c5240686516da9` (no product delta from `P_R6PLUS_D1`), with its [characterization report](../validation/pre_g9b_r6_plus_a2_preparation_characterization_report.md). On 2026-10-03 the author approved the preparation package `T_R6PLUS_A2_PROMPT` `0e50626f70107376e691df6dea7bfcfe06de5c0a` (`PASS — AUTHOR APPROVED`), disposed of `DQ-A2-1` to `DQ-A2-11` (with `AQ-L7(3)` as `DQ-A2-1`) and authorized the implementation, class `GLOBAL_IMPACT` frozen, acceptance by one `FINAL` ([preparation closeout and authorization record](../validation/pre_g9b_r6_plus_a2_prompt_closeout_record.md)); `A-2` is `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-04, `AUTHOR_SMOKE = PASS`, `T_R6PLUS_A2` `06a2ea6b4523a71f9f84fefca39e7d3b2b717e64`, tree `a05d84fd0e4ddcff27e15b2e09757ae28d36debb`, `FINAL` `verification-814f89fabe834b78ad29f08d78729081` `ACCEPTED / COMPLETE`; [closeout record](../validation/pre_g9b_r6_plus_a2_closeout_record.md); [candidate report](../validation/pre_g9b_r6_plus_a2_candidate_report.md)); the `F3` planning `f467eb45` and the preparation package `0e50626f` are published with it; the next operational subphase is `C`, `NOT AUTHORIZED`: revision 2 (2026-10-04, author-authorized focal correction) commits the hidden-layer set on Base64 and non-native file or URL document replacements; revision 3 (2026-10-04, author-authorized focal correction of the pre-existing `OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT`, reproduced on `P_R6PLUS_D1` and not caused by `A-2`) makes a completed File → New the saved baseline; `c1c08e90` and its `FINAL` stay historical evidence; the first candidate `e3cbdc60` and its `FINAL` stay historical evidence; `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (export-area and paste-notice session state on those routes) is recorded, not fixed, and needs its own disposition before the global closeout
- Preparation of `C` (2026-10-04): [C author-decision record](../validation/pre_g9b_r6_plus_c_author_decisions_record.md) (frozen semantic inputs from `B`, `D0`/`D1` and `A-2`, planned class, mandatory characterization `C1`–`C18`, forbidden scope, stop conditions, and the pending questions `DQ-C1` to `DQ-C17`, which are not author decisions). The [canonical `C` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md) is prepared on the published `P_R6PLUS_A2` `e613502831e3b412780d69424d4a1e433a4ae688`, tree `48b00681a727ea4bd8768c3e9b0159bc01573ff0`, with its [characterization report](../validation/pre_g9b_r6_plus_c_preparation_characterization_report.md): `PREPARED — NOT AUTHORIZED`, class `INTEGRATED_PHASE` re-characterized and confirmed (registered `PHASE` + `INTEGRATION`); implementation not authorized. Documentary reconciliation of 2026-10-04 in a linear descendant of `5ad96304ae9718c296db753b4822130cf3d99b61` (kept intact): author directions on `DQ-C5` (local admissibility versus global completeness in LaTeX) and `DQ-C7` (`drawingScale` resets only on successful effective document transitions), `DQ-C13` re-characterized after the author rejected "DXF ignores `ExportArea`" (still pending), `DQ-C9` constraints amended (still pending); `INTEGRATED_PHASE` still holds. Authorization of 2026-10-04 ([C preparation closeout and authorization record](../validation/pre_g9b_r6_plus_c_prompt_closeout_record.md), recorded in `75a2cf06`): preparation package `PASS — AUTHOR APPROVED`, `DQ-C1`–`DQ-C17` decided (`DQ-C13` choice `B1`), implementation authorized from `6cb09d55`; the [C candidate report](../validation/pre_g9b_r6_plus_c_candidate_report.md) records the frozen technical candidate. Author closeout of 2026-10-06 ([C closeout record](../validation/pre_g9b_r6_plus_c_closeout_record.md)): revision 1 `T_R6PLUS_C` `6248bf0f` `PASS — AUTHOR APPROVED — PUBLISHED`, `AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT` (`OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` reproduced by the author, pre-existing, not caused by `C`, accepted debt deferred to a separate post-`C` activity, not authorized); no later subphase is authorized
- Mini-track debt `DEBT-R6PLUS-FILE-INSERT-SURFACE` (recorded 2026-10-03, [D1 closeout record](../validation/pre_g9b_r6_plus_d1_closeout_record.md)): a File → Insert surface holding Insert File and Apply Template, both accepting `.cedg` and `.ggb` with their own semantics (Insert File imports into the current document; Apply Template applies defaults and styles and never replaces the `UnitState` or imports geometry); OFF files are not presented in the GeoCeDG UI, internal upstream OFF support stays. Assigned by the author's planning decision of 2026-10-03 to the new subphase `F3` — File / Insert surface and document insertion compatibility ([F3 author-decision record](../validation/pre_g9b_r6_plus_f3_author_decisions_record.md)): `PLANNED — NOT AUTHORIZED`, planned class `INTEGRATED_PHASE` (registered `PHASE` + `INTEGRATION`), hard dependency `D1 → F3`; it must be closed before the global closeout of `PRE-G9B-R6-plus`, and its implementation prompt is not prepared
- Post-`C` preparation (2026-10-06): the author authorized a bounded characterization, design and implementation preparation for `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`, outside the subphase sequence and without authorizing the fix. The [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt.prompt.md) is prepared on `P_R6PLUS_C` `39eb05bc16a18ae48167383e90fcf38b12a681b6`, tree `fc13f5f125a4e8d595dd450bfd47706be193e5cd`, with its [characterization report](../validation/pre_g9b_r6_plus_c_x1_preparation_characterization_report.md). Authorization of 2026-10-06 ([C-X1 preparation closeout and authorization record](../validation/pre_g9b_r6_plus_c_x1_prompt_closeout_record.md), recorded in `7a88a491`): preparation package `50baef73` `PASS — AUTHOR APPROVED`, identifier `PRE-G9B-R6-plus-C-X1` accepted, `DQ-X1-1`–`DQ-X1-6` decided, `BOUNDED_PHASE` frozen (one registered `PHASE`), implementation authorized; the [C-X1 candidate report](../validation/pre_g9b_r6_plus_c_x1_candidate_report.md) records the technical candidate `eabe6368`. Author closeout of 2026-10-06 ([C-X1 closeout record](../validation/pre_g9b_r6_plus_c_x1_closeout_record.md)): `T_R6PLUS_C_X1` `eabe6368` `PASS — AUTHOR APPROVED — PUBLISHED`, `AUTHOR_SMOKE = PASS`, `PHASE` `verification-f07334b6` `ACCEPTED / COMPLETE`; `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` resolved in `C-X1`. `C-X1` does not replace `E1` and does not change the operational order.
- Preparation of `E1` (2026-10-06): after the `C-X1` closeout the author authorized characterization, design reconciliation, author-decision preparation and the canonical `E1` prompt, without authorizing implementation and without promoting the P0 recommendations. The [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-ggt-library-and-packaging.prompt.md) is prepared on the published `P_R6PLUS_C_X1` `23b971f5a967958f93e745ca8c39fce1162706e6`, tree `5f564c8391828dc781dee0b1be93ef223431af58`, with its [characterization report](../validation/pre_g9b_r6_plus_e1_preparation_characterization_report.md): `PREPARED — NOT AUTHORIZED`; proposed split `E1-L` (`BOUNDED_PHASE`) / `E1-P` (`OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, `PHASE_LOCAL`), not author-approved; decisions `DQ-E1-1` to `DQ-E1-16` pending (covering `AQ-G1`–`AQ-G7`); implementation not authorized.
- Author decisions and `E1-L` authorization (2026-10-07): on the frozen preparation candidate `T_R6PLUS_E1_PREP` `0ce52a965bcf8b9de37a1572738c0ad79db52354`, tree `e500e80fddf957078249383ae61e28e6f707d455`, the author resolved `DQ-E1-1` to `DQ-E1-16` ([E1 preparation closeout, author decisions and E1-L authorization record](../validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md)): `E1` split into `E1-L` (`BOUNDED_PHASE`, one registered `PHASE`; [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-l-curated-ggt-library.prompt.md) `AUTHORIZED FOR IMPLEMENTATION`) and `E1-P` (`OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, `PHASE_LOCAL`; [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-p-ggt-library-packaging.prompt.md) `PREPARED — NOT AUTHORIZED`); twelve tools curated and shipped; dimension and sheet tools excluded (references for `E2`/`E3`; `AQ-G3`/`AQ-G3a` need no `E1` implementation); black at GeoGebra `lineThickness` 3; read-only installation-relative bundled catalog; [rights record version 1](../licensing/curated-ggt-library-rights-record.md) (author sole rights holder; `INTERNAL` and `NC` authorized, `COMMERCIAL` not authorized / pending); `AGENTS.md` §3.2 amendment for `models/curated/` authorized as its own commit; `G` keeps depending on `E1-P` unless the author re-plans it. Author-approved requirement `ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA` (owner `G`): product-owned About metadata and licensing route; visible version stays `1.0.0` during the mini-track and becomes `1.1.0` only at `G = PASS — AUTHOR APPROVED`, with its version date.
- `E1-L` closeout (2026-10-07): `PASS — AUTHOR APPROVED — PUBLISHED` on `T_R6PLUS_E1_L` `5f34d181b4f03412acfbdbeedb3fd7931de02ed9` (revision 1 after the functional smoke of R0 `1aa05d67`; [E1-L closeout record](../validation/pre_g9b_r6_plus_e1_l_closeout_record.md)); ADR 0033 `ACCEPTED — AUTHOR APPROVED`, curated-library specification `NORMATIVE / AUTHOR APPROVED`; `E1-P` stays `PREPARED — NOT AUTHORIZED`; no later subphase is authorized.
- `E1-P` and aggregate `E1` closeout (2026-10-07): `E1-P` implementation authorized on `P_R6PLUS_E1_L` `47b39e5f` (`6bce8cf5`); technical candidate `T_R6PLUS_E1_P` `dcda4075843766e8c384c176007c5edb776357c2` ([candidate report](../validation/pre_g9b_r6_plus_e1_p_candidate_report.md)) `PASS — AUTHOR APPROVED`, `AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT` ([E1-P and E1 closeout record](../validation/pre_g9b_r6_plus_e1_p_closeout_record.md)); `INTERNAL` and `NC` package the same curated library at `app/ggt-library/`, `COMMERCIAL` excludes it; `PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED`; `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` registered as open pre-existing debt, only its focal characterization authorized next; `E2` not authorized.
- `E1-X1` focal characterization (2026-10-07): at the author's instruction, on the published `P_R6PLUS_E1` `fdc1ade6e7ab707bbc919d337bc48d1abce90a1b`, tree `cc613c0337d3d75fdf14e55d20db5cb896af2b52`, `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` is characterized for both users of the shared route ([characterization report](../validation/pre_g9b_r6_plus_e1_x1_preparation_characterization_report.md)): the jpackage runtime has no `java(w).exe` by construction (`--strip-native-commands`), so `openDiagnostic` fails in every packaged form; the same seam also leaves the child's pipes undrained (`X1-F2`) and drops the declared JVM options in development (`X1-F3`). The [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-x1-packaged-classic-diagnostic-launcher.prompt.md) is `PREPARED — NOT AUTHORIZED`: recommended jpackage additional launcher found as a sibling of `jpackage.app-path`, proposed class `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` (`PHASE_LOCAL`), decisions `DQ-E1X1-1` to `DQ-E1X1-9`; implementation not authorized; `E1` not reopened.
- `E1-X1-R1` micro-characterization (2026-10-07): at the author's instruction, on the local characterization candidate `T_R6PLUS_E1_X1_CHAR` `b6172af4f319d297aa5780c9f516b310254d3e90`, a scratch prototype of an early Classic dispatch in `GeoCeDG.main` (Candidate C) was compared with the jpackage secondary launcher (Candidate A) ([launcher comparison report](../validation/pre_g9b_r6_plus_e1_x1_r1_launcher_comparison_report.md)): C met the decision rule `C1`–`C11` in development and in `INTERNAL`/`NC` app-images and ZIPs built by the unmodified builder, with the `E1-P` launcher inventory, installers and shortcuts and a 42/42 packaging contract; preferred implementation Candidate C (A kept as the documented alternative), proposed class `BOUNDED_PHASE`, decisions `DQ-E1X1-1` to `DQ-E1X1-8`; the canonical prompt is reconciled to C and stays `PREPARED — NOT AUTHORIZED`; implementation not authorized.
- `E1-X1` implementation (2026-10-07): the author accepted the R1 recommendation and authorized the implementation and technical verification with Candidate C ([authorization record](../validation/pre_g9b_r6_plus_e1_x1_authorization_record.md), `51bb677e`), on the implementation branch from `b3c78e6d`, `BOUNDED_PHASE` frozen with one registered `PHASE` `PRE-G9B-R6-PLUS-E1-X1`. The technical candidate ([candidate report](../validation/pre_g9b_r6_plus_e1_x1_candidate_report.md)) is pending author review and author smoke; packaging product change none.
- `E1-X1` closeout (2026-10-07): `PASS — AUTHOR APPROVED — PUBLISHED` on `T_R6PLUS_E1_X1` `b12ab32c3a4dcf42941617e95d06a92bfb3b2955` (`AUTHOR_SMOKE = PASS`, `DQ-E1X1-4` accepted, [E1-X1 closeout record](../validation/pre_g9b_r6_plus_e1_x1_closeout_record.md)); `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` resolved with `X1-F2` and `X1-F3`; `OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION` stays open and must be resolved before `E2`; the only next authorized activity is `PRE-G9B-R6-plus-E1-P-X1`, whose implementation is not yet authorized; `E2` not started.
- `E1-P-X1` reconciliation (2026-10-07): at the author's authorization, a test-only verification reconciliation on `P_R6PLUS_E1_X1` `7d132ec5` replaces the superseded packaging-negation clauses of `PreG9BR6PlusE1LCuratedLibraryTest.e1l09` with the controlled route owned by the packaging specification (test identity kept), restores a clean `final.desktop` and refreshes the official inventory; `BOUNDED_PHASE` with one registered `PHASE` `PRE-G9B-R6-PLUS-E1-P-X1`; `PRODUCT CHANGE = NONE`; the technical / verification candidate ([candidate report](../validation/pre_g9b_r6_plus_e1_p_x1_candidate_report.md)) is pending author review; `E2` not started.
- `E1-P-X1` closeout (2026-10-07): `PASS — AUTHOR APPROVED — PUBLISHED` on `T_R6PLUS_E1_P_X1` `44b90156786bce4ec5fca0466e7b8f81ea5bb358` (`AUTHOR_REVIEW = PASS`, `AUTHOR_SMOKE = NOT REQUIRED — NO PRODUCT BEHAVIOR CHANGE`, [E1-P-X1 closeout record](../validation/pre_g9b_r6_plus_e1_p_x1_closeout_record.md)); `OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION` resolved; `final.desktop` clean; `E1-L`, `E1-P`, `E1` and `E1-X1` unchanged; the verification baseline is clean and the next phase, `PRE-G9B-R6-plus-E2`, may proceed only under a separate explicit author instruction; `E2` not started.
- Preparation of `E2` (2026-10-07): at the author's instruction, characterization, design reconciliation and preparation only, on the published `P_R6PLUS_E1_P_X1` `dc63b0e55f12eb96d3aea359db4476cbda6c89dc`, tree `8b2b5027a2119f996de8ca5af91b03ce32731a8c` ([characterization report](../validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md); [reconciled design candidate](pre_g9b_r6_plus_e2_native_dimensions_reconciled_design_candidate.md), which supersedes the stale facts of §8.5 — 125 actions, not 115; `construction-metrics` at `application-profile.yml:3475-3480`; no `F1` dependency). The [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e2-native-dimensions.prompt.md) is `PREPARED — NOT AUTHORIZED`: representation β, presentation seam, side rule, direction authority, text orientation, legacy macro collision, verification class (`INTEGRATED_PHASE` proposed, not frozen), export fidelity, names and placement, and tool capture are pending author decisions `DQ-E2-1` to `DQ-E2-11`; `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` re-characterized (macros resolve before native commands, case-insensitively, in documents; a stored legacy package disables the whole user library); implementation not authorized.
- Author decisions for `E2` (2026-10-07): on the documentary candidate `T_R6PLUS_E2_PREP` `6ad853c618da8f07a279b266c83002c145c6292c` (STATIC `verification-b355b147258849beb498a541293a4fd9` `ACCEPTED / COMPLETE`), the author froze `DQ-E2-1` to `DQ-E2-11` ([E2 author-decision record](../validation/pre_g9b_r6_plus_e2_author_decisions_record.md)): β approved for design; commands `AlignedDimension` / `LinearDimension` with ES aliases `CotaAlineada` / `CotaLineal` (the legacy `DirectDimension` / `AxisDimension` / `directDimension` / `axisDimension` are not registered, which resolves the naming side of `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`); mandatory offset; side pinned by explicit inputs (B1); model-unit value with unit-sensitive presentation text; `INTEGRATED_PHASE` approved for design; DXF segment subset as a durable limitation. The author rejected the horizontal-text limitation and required a normative-style aligned value (`DQ-E2-6`); the authorized focal characterization ([addendum](../validation/pre_g9b_r6_plus_e2_dq6_aligned_text_addendum.md)) found it **feasible with β** through one bounded drawable seam (no new `GeoElement`, no XML), pending author review. The reconciled design candidate and the canonical prompt are updated; `E2` = `PREPARED — AUTHOR DECISIONS FROZEN — IMPLEMENTATION NOT AUTHORIZED`.
- Implementation of `E2` (2026-10-08): authorized by the author ([E2 authorization record](../validation/pre_g9b_r6_plus_e2_authorization_record.md)); technical candidate pending author review ([candidate report](../validation/pre_g9b_r6_plus_e2_candidate_report.md); [specification](../../geocedg/specs/dimensions/native-dimensions.md); [ADR 0034](../adr/0034-native-dimension-representation-and-presentation-seams.md) `PROPOSED`).
- Author smoke follow-up of `E2` (2026-10-08): `AUTHOR_SMOKE = PASS WITH OBSERVATIONS` on the original candidate `878ff66b` (unchanged); legacy document macros classified `A-INTENTIONAL-PROFILE-BEHAVIOR`; dimension line thickness and the unit suffix policy (unit-system amendment `1.1` §20, `PROPOSED`) delivered in a revised technical candidate pending author review ([author decisions](../validation/pre_g9b_r6_plus_e2_smoke_followup_author_decisions_record.md); [follow-up report](../validation/pre_g9b_r6_plus_e2_smoke_followup_report.md)).
- `E2` closeout (2026-10-08): `PASS — AUTHOR APPROVED — PUBLISHED` on the revised `T_R6PLUS_E2` `3a7246614a6ef040d84a1440c4c10b3f93be5776` (`AUTHOR_SMOKE = PASS WITH ACCEPTED NON-BLOCKING OBSERVATIONS`, [E2 closeout record](../validation/pre_g9b_r6_plus_e2_closeout_record.md)); `OBS-E2-1` to `OBS-E2-4` registered for a POST-E2 planning and characterization proposal; POST-E2 implementation not authorized.
- POST-E2 planning (2026-10-08): `PLANNING PASS — AUTHOR APPROVED` ([planning proposal](post_e2_planning_proposal.md); [author decision record](../validation/post_e2_planning_author_decision_record.md)); `POST-E2-P1` characterization authorized (`BOUNDED_PHASE`); P1 implementation and P2–P4 execution not authorized.
- `POST-E2-P1` (2026-10-08): `PASS — AUTHOR APPROVED` as a characterization ([P1 closeout record](../validation/post_e2_p1_closeout_record.md)); `OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM` registered, characterization owner `POST-E2-P1-R1` (authorized; correction not authorized).
- `POST-E2-P1-R1` (2026-10-08): `PASS — AUTHOR APPROVED` as a characterization ([P1-R1 closeout record](../validation/post_e2_p1_r1_closeout_record.md)).
- `POST-E2-P1-R2` (2026-10-09): `PASS — AUTHOR APPROVED — PUBLISHED` on `6dde194eba240ed7bcc249df323a3ca6173b8a0e` ([R2 closeout record](../validation/post_e2_p1_r2_closeout_record.md)); physical PDF style sizes, marker sizes, Properties thickness field and status-bar drawing scale; `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` registered (not authorized); `POST-E2-P2` characterization authorized (`BOUNDED_PHASE`), implementation not authorized.
- `POST-E2-P2` (2026-10-09): characterization `PASS — AUTHOR APPROVED — PUBLISHED` on `216964dc4ce8512e7b2be9587c8f93ca1d5a4355` ([P2 closeout record](../validation/post_e2_p2_closeout_record.md)); global menu-accelerator conflict from a text-producing user-assigned shortcut; correction `POST-E2-P2-R1` (Option A, `BOUNDED_PHASE`) authorized, approval and publication pending.
- `POST-E2-P2-R1` (2026-10-09): `PASS — AUTHOR APPROVED — PUBLISHED` by conditional author approval of the D1 successor `38647c796e6499a10d5435844866d2144fdfbe24` ([P2-R1 closeout record](../validation/post_e2_p2_r1_closeout_record.md)); `POST-E2-P3` design and bounded implementation authorized after a readiness gate (`INTEGRATED_PHASE`).
- `POST-E2-P3` (2026-10-09): `PASS — AUTHOR APPROVED — PUBLISHED` on `1e8714a968b7def9697d67bf056fc77f48da0fc8` ([P3 closeout record](../validation/post_e2_p3_closeout_record.md)); User tools → Document tools; `POST-E2-P4` readiness/design gate authorized with the author-selected Option A (explicit offset conversion), bounded implementation only on gate outcome A.
- `POST-E2-P4` (2026-10-09): `PASS — AUTHOR APPROVED — PUBLISHED` by conditional author approval of the Graphics View follow-up successor `06835fdf477d23ade9694c952c84191b7f2ebe07` of `af927bcb` ([P4 closeout record](../validation/post_e2_p4_closeout_record.md)); explicit Make offset draggable conversion (Algebra View and Graphics View menus); `PRE-G9B-R6-plus-E3` documentary readiness only; `PRE-G9B-R6-plus-F4` planning intent recorded (between `F3` and `G`), not authorized.
- Preparation of `E3` (`PRE-G9B-R6-plus-E3-PREP`, 2026-10-09): at the author's instruction, characterization, design reconciliation, author-decision preparation and the canonical prompt only, on the published `P_R6PLUS_POST_E2_P4` `13e08ac1fe1c6c931a88985b97df93d8445812b9`, tree `dd2ba17c483a18b8931e1848f38616edfa98b06a` ([characterization report](../validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md); [reconciled design candidate](pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md), which supersedes the §8.6 conversion wording by `unit-system.md` §18.4 and the stale `P0` profile line numbers and catalog delta — 127 actions; `construction-annotations-media` at `application-profile.yml:3527-3532`). The [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md) is `PREPARED — NOT AUTHORIZED`: representation, sheet content against the legacy hide requirement, size set, input forms, canonical binary64 expression, `AQ-E3c`, the unspecified-unit gate, `ExportArea` participation and link lifecycle (`AQ-E3b`), `ISO_A_SELECTION`, the label and the coherence indicator, workflow, names and catalog, legacy relation and verification class (`INTEGRATED_PHASE` proposed, not frozen) are pending author decisions `DQ-E3-1` to `DQ-E3-16`; preparation class `DOCUMENTATION_STATUS_ONLY`; implementation and publication not authorized.
- Author decisions and `E3` design closeout (`PRE-G9B-R6-plus-E3-PREP-CLOSEOUT`, 2026-10-10): on the preparation candidate `T_R6PLUS_E3_PREP` `e063a8a27450cded0655afb6abc1e78f89f9ac6f`, tree `00549e5bf803eb0c7afd35502d18a30396a75f32` (STATIC `verification-ae183de8b275467a876526563627f35b` `ACCEPTED / COMPLETE`), the author decided `DQ-E3-1` to `DQ-E3-16` ([E3 author-decision record](../validation/pre_g9b_r6_plus_e3_author_decisions_record.md)): R2 closed polylines; hidden ISO 216 paper boundary as the authoritative sheet and `ExportArea` rectangle; optional inner frame with 20/10/10/10 mm margins (ISO 5457-style, no compliance claim), default on for A0–A4 and off for A5–A10; one seven-argument command with an explicit frame input; `((L·b)/a)·u`; geometry unchanged by a later unit change, with physical-size and scale-label coherence as independent derived checks, "Use sheet scale" and an activation warning; creation blocked with an unspecified unit; explicit, live, session-scoped `ISO_A_BORDER` link validated by identity; separate `ISO_A_SELECTION`; `IsoABorder` / `MarcoISOA`, catalog 127 → 130; no legacy migration; `INTEGRATED_PHASE`. The [reconciled design](pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md) is `DESIGN — AUTHOR APPROVED — PUBLISHED`; the [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md) is `PREPARED — NOT AUTHORIZED — PUBLISHED`; `E3` implementation `NOT AUTHORIZED`; `OTQ-E3-1` (Classic surface) must be disposed of at implementation authorization; next authorized activity: author review of the published `E3` design package. The planned sequence stays `E3 → F1 → F2 → F3 → F4 → G`, each subphase with its own authorization; the states of `F1`, `F2`, `F3`, `F4` and `G` are unchanged.
- `E3` implementation authorization (`PRE-G9B-R6-plus-E3-IMPLEMENTATION`, 2026-10-10): after reviewing the published design package (`P_R6PLUS_E3_DESIGN` `ff56099184544e5988c63e0ea3339e3d0fe01fac`, tree `1cdd1e986df785f5f16e7887300d9034e5e4a677`), the author resolved `OTQ-E3-1` (shared command available in GeoCeDG and Classic; GUI and export-area orchestration GeoCeDG-only), corrected the numerical-accuracy contract (deterministic evaluation, conditional error bounds, validity versus reliability, `NOT_DETERMINABLE` coherence) and authorized the implementation, class `INTEGRATED_PHASE` frozen ([author-decision record](../validation/pre_g9b_r6_plus_e3_author_decisions_record.md) §5); the [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md) is `AUTHORIZED FOR IMPLEMENTATION`; publication of the implementation `NOT AUTHORIZED`.
- `E3` technical candidate (`PRE-G9B-R6-plus-E3-IMPLEMENTATION` Stage B, 2026-10-10): native `IsoABorder` (R2: two closed `GeoPolyLine` and one `GeoText`, seven arguments, `MarcoISOA`), the menu-only tool with its unit gate, the `ISO_A_SELECTION` and `ISO_A_BORDER` export-area producers with the identity-only link, the physical-size and scale-label coherence indicators and "Use sheet scale", catalog 127 → 130, the shared command available in GeoCeDG and Classic; [candidate report](../validation/pre_g9b_r6_plus_e3_candidate_report.md), [specification](../../geocedg/specs/sheets/iso-a-border.md) and [ADR 0036](../adr/0036-iso-a-border-representation-and-export-area-producers.md) `PROPOSED`; `TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`; `PHASE` and `INTEGRATION` on the candidate commit reported outside the report; author smoke pending; publication `NOT AUTHORIZED`; `selfApproved = false`.
- `E3-R1` corrective candidate (`PRE-G9B-R6-plus-E3-R1`, 2026-10-10): after the author smoke of `257c854a` (`PASS WITH TWO BLOCKING EXPORT OBSERVATIONS`), the bounded correction completes the AC1015 DXF container (entities unchanged; AutoCAD 2026 opens it in the agent-side check, author re-smoke pending) and fixes the LaTeX export of the sheet (label with `\textemdash{}` inside the frame; physical page equal to the export area in PGF/TikZ, PSTricks and Asymptote); [candidate report](../validation/pre_g9b_r6_plus_e3_r1_candidate_report.md); `INTEGRATED_PHASE`; publication `NOT AUTHORIZED`; `selfApproved = false`.
- `E3` and `E3-R1` author closeout (`PRE-G9B-R6-plus-E3-AUTHOR-CLOSEOUT`, 2026-10-10): `PRE-G9B-R6-plus-E3-R1` and `PRE-G9B-R6-plus-E3` = `PASS — AUTHOR APPROVED` on the corrective successor `T_R6PLUS_E3_R1` `7cd501167c856eef356888ca87ed3a0c97cea55c`, tree `3c51c9d3635c22939df6cab60259521f885eff92` (the original candidate `257c854a` stays historical evidence); `AUTHOR_SMOKE = PASS WITH ACCEPTED NON-BLOCKING VERIFICATION LIMITATION` (Asymptote with label `UNAVAILABLE — NOT CLAIMED PASS`); ISO A specification `1.0` with §14.1a `NORMATIVE / AUTHOR APPROVED`, ADR 0036 `ACCEPTED — AUTHOR APPROVED`, the `E3-R1` G5 container amendment `AUTHOR APPROVED — IMPLEMENTED AND VERIFIED` (G5 stays `Experimental`); three POST-E3 activities authorized with bounded scopes and prepared, not executed; [closeout record](../validation/pre_g9b_r6_plus_e3_closeout_record.md); publication `NOT AUTHORIZED`; `selfApproved = false`.
- POST-E3 author decisions and local integration (`POST-E3-AUTHOR-RECONCILIATION-AND-INTEGRATION-PREP`, 2026-10-10): on the published `P_POST_E3` `31286a2355cabebc69c3937442e7f15636da12ba`, the author approved the Spanish command-lookup characterization (D0 for the legacy-load attribution; new `OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP` open), the PSTricks dimension-angle characterization and the C1 design (implementation a separate task; planned `BOUNDED_PHASE`), and the legacy inventory correction (`28e9aebe`). `OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION` is retained outside C1. The three candidates are integrated linearly on `phase/post-e3-followups-author-integration` (`ed3f7a5e`, `61dbb0ad`, `718c4081` and a reconciliation commit); integration candidate pending author review; publication `NOT AUTHORIZED`; [reconciliation record](../validation/post_e3_followups_author_reconciliation_record.md); `selfApproved = false`.
- Preparation of `F1` (`PRE-G9B-R6-plus-F1-PREP`, 2026-10-11): at the author's instruction, characterization, design reconciliation, author-decision preparation and the canonical prompt only, on the published `P_C1` `bde2827bb21e4a28e080db6a65b4abc892071cf8`, tree `1b74102cea5151b8a6c571cb36b96b33af6fb3c8` ([characterization report](../validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md); [reconciled design candidate](pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md), which re-establishes §9.1 at the base — `getDirection` is the accessor authority, path parameters and `UnitVector` agree, `getDirectionInD3` is opposite without an end point; equation lines and tangents at a point of a conic have representation-dependent senses; the `P0` overlay citation is stale and no export route reaches the overlay; [author-decision preparation record](../validation/pre_g9b_r6_plus_f1_author_decision_preparation_record.md)). The [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md) is `PREPARED — NOT AUTHORIZED`: scope, views, export, visual design, eligibility, preference lifecycle, suppression, seam and class, and UI surface are pending author decisions `DQ-F1-1` to `DQ-F1-9`; proposed `BOUNDED_PHASE`, not frozen; preparation class `DOCUMENTATION_STATUS_ONLY`; the `P0` F1 candidate stays unchanged as evidence; implementation and publication not authorized.
- Self approval: **false**

This note records the mini-track the author defined on 2026-10-01 as the final
refinement before `PRE-G9B-R7`. It turns that scope into a decomposition,
dependencies, owners and verification proposals. It authorizes no
implementation. Each subphase needs a separate explicit author authorization
naming its exact base.

Its findings are **pre-characterization**. They were gathered at `P_R6` to make
the decomposition technically credible, and they are cited with `path:line`
evidence. They are not normative. `P0` must re-establish every finding it uses
at its own base and correct this note where the evidence differs.

**Documentary reconciliation.** The planning candidate `T_R6PLUS_PLAN`
(`147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f`) was reconciled by the author
instruction of 2026-10-01 in its direct descendant `D_R6PLUS_PLAN`, with no
product delta. It records two requirements the author had already supplied
(the `Export_1`/`Export_2` clipping and padding behavior, and the sheet-tool
modifications), the author confirmation of `AQ-U1`, and the author targets for
layer numbers and the Move-group working-layer mode. It also sharpens three
design obligations for `P0`: the GGT repository structure and packaging, the
GGT provenance and licensing gate, and the orientation authority for `F1`. The
pre-characterization findings are unchanged.

## 1. Governing authority and placement

The authority order is `AGENTS.md` §2, refined for this track by the author
instruction: current code and serialization; `AGENTS.md`; accepted
specifications and ADRs, with the canonical models and approved baselines under
`models/` and `geocedg/validation/`; the live verification and documentation-maintenance
contracts; the roadmap and published closeouts; the pinned upstream source;
local author input; then agent reports and summaries. The author's scope
requirements in §2 and §4 are **author-fixed scope constraints** recorded from
the 2026-10-01 instruction: they bind every subphase as scope, and become
technical contracts only through the accepted ADRs and specifications that
later subphases produce. Technical statements in any
report, including `ReportExport.md` and this note, are checked against source.

`PRE-G9B-R6-plus` is CeDG refinement, not a CAD feature-tree redesign. It is
not a vehicle for complete `G9B` spatial semantics, new spatial-object
reconstruction, full 3D semantics, the DSL, productive `G10`, `G9C`, `G9U2`,
Web deployment, commercial or public binary release, STL or Collada support, or
exact DXF `SPLINE`.

### 1.1 Relation to the owning future gates

Three capabilities overlap gates the roadmap assigns to later phases. The track
delivers **bounded slices ahead of those gates without moving their
ownership**, on the precedent of `POST-G9U1-A7`, which delivered a bounded
`G12` slice while the rest of `G12` stayed unauthorized.

| Owning gate | Roadmap scope | Bounded slice here | What stays with the gate |
|---|---|---|---|
| `G11` layers and view states | hierarchy, roles, locking, filters, per-view visibility, savable states, DXF/PDF integration | explicit working layer, layer numbers beyond 0..9 (author target; extension designed by `P0`), flat hide/show of numeric layers, status display | hierarchy, roles, locking, filters, per-view states, named layer states |
| `G12` navigation and scales | separation of model, view, drawing and print scale | `constructionUnit`, `presentationUnit` and engineering export scale | named views, extended scale profiles, the rest of `G12` |
| `G15` sheets, PDF and formats | `DrawingSheet` presentation space, frames, title blocks, exact-size PDF, viewports | one export-area authority, ISO A border as a model-space annotation, physical scale on existing exporters | paper space, viewports, title blocks as a document service, sheet sets |

`IsoABorder` is a **model-space annotation**: a construction object drawn at
drawing scale, like the legacy sheet tools. It is not the `G15`
`DrawingSheet` paper space, and it must not be extended into one inside this
track.

## 2. Author-defined scope

The author fixed the following scope on 2026-10-01. It binds every subphase.

**Layers.** An explicit working layer that the user can change at any time;
new objects use it; a working-layer tool placed with the current Move tools
unless characterization gives a compelling reason otherwise; the active layer
shown in the status bar; the Algebra View `Sort by Layer` retained; layers
hidden and shown from the Algebra View without overwriting individual object
visibility, so that

```text
effectiveVisible(object) = objectVisible(object) AND NOT layerHidden(object.layer)
```

```text
AUTHOR TARGET = working layer not restricted by the current 0–9 UX/domain
working-layer toolbar entry = GeoCeDG mode compatible with the Move group
```

The author requires that the user can choose an arbitrary layer number. The
current upstream domain is 0..9 and several consumers rely on it (§5.1), so
`P0` designs the least invasive correct extension and determines its real
compatibility and verification impact. The widening is not implemented by the
planning and is not presumed harmless. The working-layer tool stays in the
current Move group, which accepts modes only (§5.5), so it is a GeoCeDG mode;
`P0` confirms the exact implementation design.

**Status bar.** The layer information strip becomes an extensible GeoCeDG status
bar showing at least `Working layer | Construction unit | Presentation unit`,
for example `Layer: 4 | Construction: mm | Dimensions: cm`. It is presentation
and frontend state, never geometric authority. It is complementary status
information and may gain interaction if `P0` recommends it; it does not replace
the Move-group tool.

**File / Export and `ExportArea`.** Expose or reorganize: Graphics View as
Picture (PNG, PDF, SVG, EMF/EMF+), DXF, PSTricks, PGF/TikZ and Asymptote. Do not
expose STL, Collada, HTML Collada or Dynamic Worksheet as Web Page in this
generation. Correct explicit-area padding and clipping where source confirms the
author's observations. Provide File actions to define and display the export
area. One coherent `ExportArea` authority, with candidate producers
`Export_1`/`Export_2`, a manual rectangle, an ISO A area selection and the
future `IsoABorder`:

```text
explicit ExportArea != viewport
zoom / camera / screen clipping != export authority
```

An explicit area larger than or outside the visible viewport is still exported
completely. The visible Graphics View is the fallback only when no explicit
area exists. The author's current preference is session lifetime.

The author has reported, and requires correcting, two behaviors of PNG, PDF,
SVG and EMF(+) export when the area is defined by `Export_1` and `Export_2`:

- the exported area appears to include a small border outside the exact
  rectangle; the required result is **exactly** the rectangle the two points
  define;
- when part of that rectangle lies outside the visible viewport, the export
  may be clipped to the visible content; the required result is the
  **complete** explicitly defined rectangle, independently of what the
  viewport currently shows.

These are author-supplied requirements. `P0` reproduces and characterizes them
against source and runtime (§6.3); `B` implements them.

**Semantic export.** Export support for `LocusV2` and `SplineV2` in the
applicable LaTeX and vector exporters, without making render tessellation
geometric authority:

```text
export adapter consumes semantic geometry
kernel does not become an export formatter
```

**Units.** The fixed semantics of §4.

**GGT library.** A curated library using the local author tools as references,
with provenance, eventual installer integration and GeoCeDG-owned,
reproducibly maintained icons for every retained tool. The author intends to
ship the approved library, so packaging support is a **required design target**.
Style through line type, color, thickness and remembered last style if
sustainable; otherwise the author fallback of black at 3 pt. The author requires
two modifications of the `sheetISOland`/`sheetISOvert` references, made only in
their future authorized phase: the outer dotted rectangle is hidden, and all
corner points are hidden.

**Native dimensions.** `DirectDimension` and `AxisDimension`, with the supplied
tools as behavioral references: Distance-group and Construction-menu
placement, tool help and tips, presentation-unit conversion, a traceable
placement parameter, a movable dimension line, no pathological far-away
placement, a centred figure, deterministic above/left placement and parallel
text where feasible. No manual unit multiplier hidden in the geometry.

**`IsoABorder`.** Native, under `Construction → Annotations and Media`, with no
toolbar button unless separately authorized. Inputs: upper-left point, ISO A
size, portrait or landscape, drawing scale. Its geometry respects ISO physical
size, drawing scale and the unit contract. It may configure the common
`ExportArea` after a yes/no question. There is no second hidden export-area
mechanism.

**Orientation display.** An optional, discreet cue (preferably a hollow arrow)
for the actual direction the kernel uses for a line, segment or ray. It derives
from kernel semantics, never from the viewport, and changes neither geometry nor
command behavior.

**Authoring refinements.** `Angle with Given Size` creates its angle hidden by
default. The parallel and perpendicular tools each remember an independent
reference: point first reuses the remembered reference; reference first and
then a point uses the new reference and remembers it; first use with nothing
remembered asks for a reference. This is mode and session orchestration and
does not change command semantics.

**Help and closeout.** A final integrated documentation and help phase covering
every accepted capability before `PRE-G9B-R7`.

## 3. Decomposition and dependencies

The author's proposed decomposition is **preserved**. Characterization supports
every boundary and found no reason to merge or split a subphase. It refines
the dependencies: two links are added, one is downgraded and three are scoped
to the frontend part of a subphase, each justified in §3.1.

On 2026-10-02 the author split `A` into `A-1` and `A-2` (`AQ-L1b`, as `P0`
recommended; [A author-decision record](../validation/pre_g9b_r6_plus_a_author_decisions_record.md)). On
2026-10-03 the author added `F3`, which takes the debt
`DEBT-R6PLUS-FILE-INSERT-SURFACE` recorded at the `D1` closeout
([F3 author-decision record](../validation/pre_g9b_r6_plus_f3_author_decisions_record.md)).
No other boundary changed.

| ID | Scope | Owning layers (§10) | Proposed class (§12) | State |
|---|---|---|---|---|
| `P0` | integrated characterization and normative design candidates | documentation | `DOCUMENTATION_STATUS_ONLY` | `PASS — AUTHOR APPROVED — PUBLISHED` |
| `A-1` | layer workspace with session-only layer state: working layer, Move-group mode, status bar, numeric Algebra View order, effective hiding in the normal views; no domain widening, no new serialization, no exporter change | application session, view, Desktop | `INTEGRATED_PHASE` (author-accepted) | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-02; `T_R6PLUS_A1` `2a71133a`) |
| `A-2` | layer-domain widening to `0..99` and hidden-layer persistence | shared kernel/document serialization, Desktop | `GLOBAL_IMPACT` (author-accepted; frozen at authorization) | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-04; `T_R6PLUS_A2` `06a2ea6b`, revision 3; `AUTHOR_SMOKE = PASS`; `FINAL` `verification-814f89fa…` `ACCEPTED / COMPLETE`; [closeout record](../validation/pre_g9b_r6_plus_a2_closeout_record.md); preparation package `0e50626f` `PASS — AUTHOR APPROVED` with the `DQ-A2-1`–`DQ-A2-11` dispositions; implementation authorized 2026-10-03; revision 2 authorized 2026-10-04 for the Base64 and non-native document-replacement commit; revision 3 authorized 2026-10-04 for the saved baseline of a completed File → New (pre-existing `OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT`, not caused by `A-2`, now `CLOSED`); `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` open; `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` recorded for `F2`; [candidate report](../validation/pre_g9b_r6_plus_a2_candidate_report.md)) |
| `B` | export surface and `ExportArea` authority | profile, Desktop, shared view export path | `INTEGRATED_PHASE` (author-proposed; frozen at authorization) | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-02; `T_R6PLUS_B` `848206c4`; authorized 2026-10-02 on `f6194f09`; canonical prompt prepared on `d32ad608` and `AUTHORIZED`; author decisions `AQ-X1`–`AQ-X7` recorded 2026-10-02; preparation package `PASS — AUTHOR APPROVED` 2026-10-02, `T_R6PLUS_B_PROMPT` `7e00e451`, with the `DQ-B` dispositions) |
| `D0` | unit-system normative design | documentation (ADR + specification) | `DOCUMENTATION_STATUS_ONLY` (author-frozen 2026-10-02) | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-02; `T_R6PLUS_D0` `8383a153`; ADR 0032 and unit-system v1.0 author-approved; authorized 2026-10-02 on `8814468`; canonical prompt prepared on `0ef616bb` and `AUTHORIZED`; author decisions `AQ-U2`–`AQ-U6` recorded 2026-10-02; preparation package `PASS — AUTHOR APPROVED` 2026-10-02, `T_R6PLUS_D0_PROMPT` `8814468`, with the `DQ-D0` dispositions) |
| `D1` | unit-system implementation and status integration | shared kernel/document serialization, Desktop | `GLOBAL_IMPACT` (author-frozen 2026-10-02) | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-03; `T_R6PLUS_D1` `6e1d8459`; `AUTHOR_SMOKE = PASS`; revision 2 after the author smoke of `b55aa817`; authorized 2026-10-03 on `3fb7542a`; canonical prompt prepared on `3268f9b9` and `AUTHORIZED`; preparation decisions recorded 2026-10-02; preparation package `PASS — AUTHOR APPROVED`) |
| `C` | 2D export completion and semantic curve exporters | export adapters, shared export package | `INTEGRATED_PHASE` (re-characterized and confirmed by the preparation; frozen at authorization) | `PASS — AUTHOR APPROVED — PUBLISHED` (2026-10-06; `T_R6PLUS_C` `6248bf0f`, revision 1; `AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT`; `PHASE` `verification-a198a69b…` and `INTEGRATION` `verification-0bcf9709…` `ACCEPTED / COMPLETE`; `b981f8ea` and its runs historical evidence; hidden-circle DXF finding expected by `DQ-C3`; Classic picture-dialog empty dialog and hang reproduced by the author, pre-existing, not caused by `C`, accepted debt deferred to post-`C`; [closeout record](../validation/pre_g9b_r6_plus_c_closeout_record.md); [candidate report](../validation/pre_g9b_r6_plus_c_candidate_report.md)) |
| `E1` | GGT library, assets and packaging | legacy store, resources, packaging, Desktop library | `BOUNDED_PHASE` (+ packaging evidence); split by the author into `E1-L` (`BOUNDED_PHASE`) and `E1-P` (`OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, `PHASE_LOCAL`) | `PASS — AUTHOR APPROVED` (aggregate, 2026-10-07; `E1-L` `T_R6PLUS_E1_L` `5f34d181` `PASS — AUTHOR APPROVED — PUBLISHED`, [closeout record](../validation/pre_g9b_r6_plus_e1_l_closeout_record.md); `E1-P` `T_R6PLUS_E1_P` `dcda4075` `PASS — AUTHOR APPROVED`, `AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT`, [closeout record](../validation/pre_g9b_r6_plus_e1_p_closeout_record.md); `OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER` open, pre-existing) |
| `E2` | native dimension tools | shared kernel commands, Desktop modes, profile | `INTEGRATED_PHASE` | `NOT AUTHORIZED` |
| `E3` | `IsoABorder` and `ExportArea` integration | shared kernel command, Desktop, profile | `INTEGRATED_PHASE` | `PASS — AUTHOR APPROVED` (2026-10-10; `T_R6PLUS_E3_R1` `7cd50116`, corrective successor of `257c854a`; `AUTHOR_SMOKE = PASS WITH ACCEPTED NON-BLOCKING VERIFICATION LIMITATION`; `PHASE` `verification-c4853de4…` and `INTEGRATION` `verification-cf79b0af…` `ACCEPTED / COMPLETE`; publication `NOT AUTHORIZED`; [closeout record](../validation/pre_g9b_r6_plus_e3_closeout_record.md)) |
| `F1` | direction visualization | view/drawables, Desktop option | `BOUNDED_PHASE` | `NOT AUTHORIZED` |
| `F2` | authoring-memory refinements | Desktop mode orchestration | `BOUNDED_PHASE` | `NOT AUTHORIZED` |
| `F3` | File / Insert surface and document insertion compatibility (`DEBT-R6PLUS-FILE-INSERT-SURFACE`) | profile, Desktop file surface and chooser routes, minimal upstream-identical seams | `INTEGRATED_PHASE` (author planning assignment 2026-10-03; frozen at authorization) | `PLANNED — AUTHOR APPROVED — PUBLISHED — IMPLEMENTATION NOT AUTHORIZED` (planning approved 2026-10-03, published with the `A-2` chain on 2026-10-04; implementation prompt not prepared) |
| `G` | help, integrated verification and R6-plus closeout | documentation, verification | `INTEGRATED_PHASE` (`INTEGRATION`) | `NOT AUTHORIZED` |

The `State` column is the only current status. Every proposed class is a
proposal for the author to freeze at that subphase's start; none is a
derivation. The `A-1` and `A-2` classes were accepted by the author on
2026-10-02; each is frozen when its subphase is authorized.

### 3.1 Dependency classes

Per the documentation-maintenance contract, the three classes are kept
separate.

**Hard semantic or contract dependencies**

```text
P0 ─────────────► every later subphase      (exact contracts and decisions)
P0 ──► D0 ──► D1                             D1 needs the author-accepted unit ADR and specification
D1 ──► C                                     physical scale and the DXF unit header need the unit contract
B  ──► C                                     C's exporters consume the single ExportArea authority
A-1 ──► A-2                                  A-2 widens the domain and persists the hidden-layer state A-1 introduces
A-1 ──► C                                    [added] C's LaTeX and DXF exporters consume A-1's effective-visibility rule
D1 ──► E2                                    dimension values need the presentation-unit conversion
D1 ──► E3                                    ISO physical size, drawing scale and unit contract
D1 ──► F3                                    [added 2026-10-03] Insert File keeps the D1 unit-provenance contract
P0 ──► F1, F2
A-1, A-2, B, C, D1, E1, E2, E3, F1, F2, F3 ──► G
G = PASS — AUTHOR APPROVED ──► unblocks PRE-G9B-R7

Scoped to the frontend part of a subphase only:
A-1 ──► D1 (status-bar segment)              [added] D1's unit segments extend A-1's status bar
E1 ──► E2 (Desktop tool icons)               E2's tool icons use E1's owned-icon pipeline
B  ──► E3 (ExportArea linkage)               the yes/no linkage is a producer of B's ExportArea
```

The documentation-maintenance contract (§3) forbids making a frontend client a
prerequisite of the semantics it consumes. The three scoped links therefore bind
only the frontend part of the dependent subphase: `D1`'s unit semantics and
serialization do not depend on `A`, `E2`'s kernel commands do not depend on
`E1`, and the `IsoABorder` command does not depend on `B`. If `A` or `E1` is
not yet accepted, the dependent subphase may still deliver its kernel part, and
its frontend part waits.

Relative to the author's graph: `A → C` and the scoped `A → D1` are added,
because the effective-visibility rule (§5.3) and the status bar (§5.4) are
contracts that later subphases consume. `E1 → E3` is **downgraded to a
recommended predecessor**: the border needs no toolbar icon, and its only
coupling to `E1` is that the sheet GGT disposition should be settled before a
native border overlaps it. `D1 + E1 → E2` is kept with `E1` scoped to the tool
icons. Of `B + D1 + E1 → E3`, `D1` stays hard, `B` is scoped to the export
linkage and `E1` becomes recommended.

**Recommended execution predecessors**

```text
P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G
```

This is the author's order, as accepted on 2026-10-02 with the split of `A`
and extended on 2026-10-03 with `F3` before `G`.
`A-2` follows `D1`, so the two subphases that add shared serialization and
need `FINAL` run adjacently. Since the split, `A` in the prose of this
subsection means `A-1`, the subphase that introduces the effective-visibility
rule and the status bar. The sequence is the operational order currently
chosen by the author, not a dependency: `D0` is documentation, technically
independent, and may be scheduled in parallel with `A-1` or `B`. `F1` and `F2` are independent and may be scheduled anywhere after `P0`.
`F3` has `D1` as its only hard predecessor; the author created no dependency
between `F3` and `A-2` or `C` in either direction.
None of these orderings is a kernel dependency.

**Global and release gates**

- `PRE-G9B-R7` requires `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED`, which is the
  closeout of `G`.
- A rights review of the GGT definitions and of any icon precedes any
  distribution (`AGENTS.md` §7, §14 license and asset inventory).
- Publication, tag, release, binary publication and `PROFILE COMMERCIAL` stay
  separate author decisions.
- `G11`, `G12` and `G15` keep their ownership (§1.1). Nothing here is a hard
  dependency of `G9B`.

### 3.2 Subphase briefs

Each brief states the minimum contract a future prompt must carry. `P0` turns
each into an exact design candidate.

- **`A` — layer workspace and status bar** (historical brief, **superseded by
  `A-1`/`A-2`** below; not an executable phase). Working layer chosen through a
  GeoCeDG mode in the Move group, not restricted to the current 0..9 domain,
  through the extension `P0` designs, and applied to every creation route
  including macro outputs. Per-layer hide/show in the Algebra View, with
  numeric group order, that never writes an object's own visibility. The
  `effectiveVisible` rule at the view. An extensible, complementary status bar
  showing the working layer.
  Split by the author on 2026-10-02 ([A author-decision record](../validation/pre_g9b_r6_plus_a_author_decisions_record.md)):
  - **`A-1`** — working layer (`SESSION`), one-shot Move-group mode, status
    bar, numeric Algebra View order and effective hiding in the normal views
    through painting and hit testing, with `SESSION` hidden-layer state; no
    domain widening, no new serialization and no exporter change. Exports
    that reuse the view painting inherit the hiding; SVG (`B`), LaTeX and DXF
    (`C`) do not until those subphases close.
  - **`A-2`** — domain widening to `0..99` and hidden-layer persistence, with
    their serialization and compatibility impact; it starts only after the
    author resolves the interaction between Open and persistent hidden layers
    (`AQ-L7`).
- **`B` — export surface and `ExportArea`.** Expose PNG, PDF, SVG, EMF/EMF+,
  DXF, PSTricks, PGF/TikZ and Asymptote; never STL, Collada, HTML Collada or
  the worksheet upload, including through shortcuts if they fire. One
  `ExportArea` authority with explicit producers and the viewport fallback.
  File actions to define and show it. For an `Export_1`/`Export_2` area, the
  exact rectangle with no outside border, and the complete rectangle even
  where it lies outside the viewport (§2), in every picture format including
  SVG, without losing points, graphs, axes, grid or labels. Update the
  profile's `document.sheet-export` deferral.
  Fixed by the author on 2026-10-02 ([B author-decision record](../validation/pre_g9b_r6_plus_b_author_decisions_record.md)):
  an offscreen export viewport; `SESSION` `ExportArea` with the producers
  `EXPORT_POINTS` and `MANUAL` (`ISO_A_*` in `E3`); the selection rectangle is
  never an authority; Animated GIF out of this generation; the LaTeX and DXF
  integration with `ExportArea` stays in `C`.
- **`D0` — unit-system design.** The unit ADR and normative specification
  carrying §4, its consequences (§4.5), the serialization and forward-
  compatibility rule, and the amendment of the accepted export foundation's
  `UNITLESS` statement.
- **`D1` — unit implementation.** Persistence, presentation conversion,
  status-bar segments for both units, the user default for new documents, and
  proof that legacy documents round-trip byte-identically.
- **`C` — 2D export completion.** Engineering scale (`1:n`, `n:1`) replacing
  `Scale in cm` in the picture and LaTeX exporters; the DXF unit header; and
  semantic `LocusV2`/`SplineV2` output in PSTricks, PGF/TikZ and Asymptote from
  the semantic evaluator with explicit tolerance, on the DXF adapter model.
- **`E1` — GGT library and assets.** The repository structure `P0` designs for
  `AQ-G1`, the shipped selection (`AQ-G2`), the author-required sheet
  modifications, owned icons with a deterministic generator, the style decision
  (§8.3), packaging support for the approved library through an explicit
  contract reconciliation, and the provenance and licensing gate (§8.2).
- **`E2` — native dimensions.** §8.5.
- **`E3` — `IsoABorder`.** §8.6.
- **`F1` — direction cue.** §9.1, with the `AQ-F1` option scope.
- **`F2` — authoring memory.** §9.2 and §9.3.
- **`F3` — File / Insert surface.** Added by the author on 2026-10-03
  ([F3 author-decision record](../validation/pre_g9b_r6_plus_f3_author_decisions_record.md),
  which also records the characterization at `P_R6PLUS_D1`). A GeoCeDG
  File → Insert surface holding Insert File and Apply Template, both accepting
  `.cedg` and `.ggb`. Insert File keeps its semantics: it imports into the
  current document, is not Open, keeps the `D1` provenance and notice policy
  and never scales coordinates by units. Apply Template keeps its semantics:
  labeling, defaults and styles only, no geometry, no change of the target
  `UnitState`. OFF files leave the GeoCeDG UI; the upstream OFF support stays.
  No other format and no 3D import. Desktop surface and policy, with minimal
  upstream-identical seams; no geometry semantics and no document-format
  change.
- **`G` — help and closeout.** Bilingual guide and in-product help for every
  accepted capability, the integrated `INTEGRATION` run, debt and observation
  reconciliation, and the closeout pending author approval.

## 4. Unit semantics fixed by the author

These decisions are design constraints. `D0` writes the normative ADR and
specification; it does not reopen them.

### 4.1 Construction unit

`constructionUnit` states the physical meaning of one numerical model unit.
Changing it is a deliberate reinterpretation of the real dimensions the
construction represents. It rescales nothing: with `A = (0, 0)` and
`B = (10, 0)`, `AB` represents 10 cm under `cm` and 10 mm under `mm`, and the
numerical geometry is identical.

```text
changing constructionUnit MUST NOT change:
  coordinates; authoritative numerical geometry; DAG topology; dependencies;
  semantic identities; LocusV2 branch/component identity; SplineV2 semantics;
  parameter domains; construction order; geometric object identity

change constructionUnit != geometric scaling
                        != coordinate transformation
                        != global redefine
```

### 4.2 Legacy documents

```text
legacy document with no unit metadata -> constructionUnit = UNSPECIFIED_MODEL_UNIT
```

`P0` may recommend another rule only with exact source and compatibility
evidence that this one is unsafe. Centimetres are never inferred merely because
an exporter uses them internally.

### 4.3 Presentation unit

A separate `presentationUnit` controls dimensional presentation. Where both
units are physical:

```text
displayValue = modelValue × conversion(constructionUnit -> presentationUnit)
```

Changing `presentationUnit` must not alter geometry or the physical
interpretation of the model, and must update dependent dimensional
presentation. Changing `constructionUnit` also updates dimensional presentation,
because the physical interpretation changed. Physical dimensions are never
invented for an arbitrary `GeoNumeric`.

### 4.4 Physical export

```text
physical model-to-output unit = presentationUnit
```

This is the semantic mapping between model dimensions and physical output. It
does not replace native PDF, SVG, EMF or LaTeX device and file units. The
engineering scale is written conventionally (`1:1`, `1:2`, `1:5`, `2:1`, …).
The current `Scale in cm` wording is not the GeoCeDG contract.

**Author confirmation (`AQ-U1 = RESOLVED — AUTHOR CONFIRMED`, 2026-10-01).**
The normative interpretation is:

```text
constructionUnit = physical interpretation of one model unit
presentationUnit = unit used to express dimensional presentation
                   and physical export quantities
drawingScale     = dimensionless engineering ratio
```

Changing only `presentationUnit`, with the equivalent dimensional conversion,
does not change the physical printed or exported size:

```text
10 cm  at 1:2 = 5 cm on output = 50 mm on output
100 mm at 1:2 = 50 mm on output

physical output size is invariant under an equivalent presentation-unit change
```

`presentationUnit` is the physical unit used to express and convert dimensional
and export quantities, which is how physical export uses the presentation-unit
context. It is **not** a second geometric scale factor and must never be
reinterpreted as one.

> **Supersession note (`PRE-G9B-R6-plus-D0`, by author instruction of
> 2026-10-02).** The line `physical model-to-output unit = presentationUnit`
> above, and the reading of `presentationUnit` as a unit that "converts"
> export quantities, are superseded. Physical output derives from
> `constructionUnit + drawingScale`; `presentationUnit` only controls how
> dimensional quantities are expressed, and physical output size stays
> invariant under an equivalent presentation-unit change (`AQ-U1`). The
> governing rule is the [GeoCeDG unit system](../../geocedg/specs/units/unit-system.md)
> §13 (`NORMATIVE / AUTHOR APPROVED`, 2026-10-02). The author-fixed text above is kept
> unedited as the record of the 2026-10-01 decision.

### 4.5 Consequences `D0` must make explicit

These follow from §4.1–§4.4. Item 1 is author-confirmed; the others are stated
so that `D0` addresses them, not as normative decisions.

1. **Invariance of printed size** (`AQ-U1`, author-confirmed in §4.4). With a
   physical `constructionUnit` `c`, a presentation unit `p` and scale `1:n`, a
   model length `L` prints as `L·k(c→p)/n` in `p`, which is `L·k(c→mm)/n`
   millimetres whatever `p` is. `D0` carries this into the normative
   specification.
2. **Unspecified construction unit.** No physical interpretation exists.
   Dimensions show the bare model value with no unit. `D0` must decide what
   `presentationUnit` means then (forced to model unit, or recorded but inert),
   and whether engineering-scale export is unavailable, requires a declared
   unit first, or falls back to an explicitly labelled legacy mapping. It must
   never infer `cm` silently.
3. **Unspecified presentation unit with a physical construction unit.**
   `D0` decides whether the default is `presentationUnit = constructionUnit`.
4. **Dimensional scope.** Only objects that are lengths by construction receive
   dimensional presentation: at minimum the native dimensions of `E2`. Whether
   `Distance`, `Length`, `LocusLength` and similar results also present units
   is a `D0` decision; areas (unit²) and angles (own `angleUnit`) are outside
   the length contract unless `D0` extends it explicitly.
5. **Cross-document copy.** Copy and paste between documents with different
   construction units moves numbers, not lengths. `D0` decides whether to warn,
   to refuse, or to accept silently. Converting on paste is geometric scaling
   and needs its own explicit decision.
6. **Exporter consequences.** The accepted
   [geometry export foundation](../../geocedg/specs/export/geometry-export-foundation.md)
   fixes the source unit as `UNITLESS` and DXF `$INSUNITS = 0`, and says that
   physical units need "an explicit document/application contract" that "must
   not reinterpret screen scale as model scale". `D0` is that contract, so it
   must state the amendment of that specification: for example `$INSUNITS`
   from `constructionUnit` with unchanged model coordinates.
7. **Unit set.** `D0` fixes the admitted units (at least `mm`, `cm`, `m` and
   `UNSPECIFIED_MODEL_UNIT`), their exact conversion factors and their
   serialized tokens.

## 5. Layers and status bar: pre-characterization

Paths are relative to `source/shared/common/src/main/java/org/geogebra/common/`
(`common/`), `source/desktop/desktop/src/main/java/org/geogebra/desktop/`
(`desktop/`) and `source/desktop/desktop/src/main/java/org/geocedg/desktop/`
(`geocedg-desktop/`) unless stated.

### 5.1 The numeric domain is 0..9 and clamped

- `EuclidianStyleConstants.MAX_LAYERS = 9` (`common/plugin/EuclidianStyleConstants.java:186`).
  `GeoElement.setLayer` clamps any value above 9 to 9 and any negative value
  to 0 (`common/kernel/geos/GeoElement.java:1001-1018`).
- There are **no per-layer drawable arrays** in this baseline. The view draws
  one list sorted by `DefaultGeoPriorityComparator`, which compares
  `a.getLayer() - b.getLayer()` first (`common/kernel/geos/DefaultGeoPriorityComparator.java:24-27`).
  The common assumption of a `MAX_LAYERS + 1` array does not hold here.
- Bounded-range assumptions do exist elsewhere:
  - hit testing keeps only the top layer, starting from `maxlayer = 0`
    (`common/euclidian/HitDetector.java:102-116`);
  - `SelectionManager.getSelectedLayer` uses `-1` and `-2` as sentinels;
  - the Properties layer combo uses the combo index as the layer and offers
    0..9 only;
  - the Algebra View orders layer groups **lexicographically**
    (`desktop/gui/view/algebra/AlgebraViewD.java:529`), so `10` would sort
    before `2`;
  - `ShowLayer`/`HideLayer` and the API's `setLayerVisible` ignore values
    outside 0..9;
  - the 3D renderer packs the layer into the alpha channel and shifts depth by
    it;
  - XML reads clamp silently, so an out-of-range value is lost on the next save
    (`common/io/ConsElementXMLHandler.java:610-618`).
- DXF maps layer `n ≠ 0` to `GEOCEDG_L<n>` (accepted
  [geometry export foundation](../../geocedg/specs/export/geometry-export-foundation.md)).

**Consequence.** Arbitrary layer numbers are not a local change. They need the
upstream clamp removed, and then negative layers become unclickable, collide
with the selection sentinels and break the combo; layers above 9 break the
combo, the scripting commands and the Algebra View order; very large values can
overflow the comparator; and the persisted value is no longer readable by any
build that clamps. Roadmap §14.1 also requires "compatibility with the
inherited numeric layer".

The author has nevertheless fixed the target (§2):

```text
AUTHOR TARGET = working layer not restricted by the current 0–9 UX/domain
```

So the obligation moves to `P0`: design the **least invasive correct
extension** that meets the target, and determine its real compatibility and
verification impact, covering every consumer above, persistence and older
builds. The widening is not implemented by the planning and is not
pre-classified as harmless. If `P0` concludes that broad shared or
serialization assumptions must change, it classifies `A` accordingly,
including escalation beyond the currently recommended `INTEGRATED_PHASE`
(§12). Hierarchical, named and role-based layers stay with `G11`.

### 5.2 New-object layer: upstream already has an implicit rule

- No active or working layer exists upstream (negative grep for
  current/active/working layer).
- A new object takes `min(MAX_LAYERS − 1, app.getMaxLayerUsed())`, layer 9
  being reserved "so that it's always over new objects"
  (`common/kernel/ConstructionDefaults.java:907-924`). `updateMaxLayerUsed`
  only raises the value (`common/main/App.java:1282-1290`); it is reset by
  `App.resetMaxLayerUsed` when the construction is cleared, for example on
  File → New (`App.java:1411-1414`, `desktop/main/AppD.java:2888`).
- Macro outputs are forced onto `app.getMaxLayerUsed()`, without the cap at 8
  (`common/kernel/algos/AlgoMacro.java:291`, `:306`).
- `App.getMaxLayerUsed()` is public and non-final. Its Desktop callers are the
  two paths above; the Web Edit menu is a third, Web-only caller. INFERRED:
  overriding it in `AppGeoCeDG` would route new objects and macro outputs to
  the working layer with **no upstream edit**. Limits: the defaults path still
  caps the result at 8 while the macro path does not, and the override also
  applies to objects rebuilt during load and undo before their `<layer>` tag is
  read. `P0` must probe all three.

### 5.3 Visibility decision path

In order (`common/euclidian/EuclidianView.java:2083-2240`,
`common/kernel/geos/GeoElement.java:1424-1445`):

1. `drawableNeeded`: visible in this view (`isVisibleInThisView` →
   `GeoElement.isVisibleInView(viewId)`), labelled, and `isEuclidianVisible()`;
2. `isEuclidianVisible()`: forced visibility, `showInEuclidianView()`,
   restricted visibility, then the raw `euclidianVisible` flag or the
   condition to show object;
3. painting (`DrawableList.drawAll`) and hit testing (`HitDetector`) use the
   drawable's cached visibility.

The Algebra View marble shows `isEuclidianVisible()` and toggles the raw flag.
XML serializes the raw flag. The upstream `ShowLayer`/`HideLayer` commands
**write** `setEuclidianVisible` on every object of the layer
(`common/kernel/scripting/CmdShowHideLayer.java:58-75`), which is exactly the
destructive behavior the author excludes. They must not be reused for the
hidden-layer feature.

The least invasive placement of
`effectiveVisible = objectVisible AND NOT layerHidden` is at the **view**: a
`GeoCeDGEuclidianView` override of `isVisibleInThisView` that also requires the
layer not to be hidden. A hidden-layer object then has no drawable, so it is
neither painted nor hittable, and its own flag and `<show object>` stay
untouched. Folding the rule into the final `GeoElement.isEuclidianVisible` is
the rejected alternative: it is an upstream kernel edit, it changes what every
script, command and exporter reads as visibility, it makes the marble report
"hidden" while a click flips an unseen raw flag, and it would make DXF mark the
objects hidden. Caveats for `P0`: only Graphics 1 is a GeoCeDG view class
today, the same predicate has a few other consumers (slope fields, ODE
integrals, input boxes, absolute positioning), and the Show/Hide tool would not
reveal hidden-layer objects.

### 5.4 Algebra View and status bar

- `Sort by Layer` is upstream `AlgebraViewD` (`:188-200`, `:513-537`); GeoCeDG
  reaches it through `GeoCeDGHostMenuFactory` (`:38-39`, `:60-79`). Group nodes
  have open/closed icons only and no show/hide control. INFERRED: a
  `GeoCeDGAlgebraView` renderer override (`newMyRenderer`) plus click
  interception can add a per-layer eye that drives the hidden-layer set without
  writing any object flag. The group order must become numeric.
- No status bar exists. `AppD.buildApplicationPanel` leaves the `SOUTH` slot
  of `applicationPanel` unused (`desktop/main/AppD.java:2270-2369`), so a
  GeoCeDG override can host the status bar without touching the input bar.

### 5.5 Where the working-layer tool can live

Native toolbar groups accept **modes only**:
`GeoCeDGToolbarContainer` throws "Non-mode action in native toolbar group"
(`geocedg-desktop/GeoCeDGToolbarContainer.java:203-208`), and a profile flyout
cannot precede the first native group (`:173-178`). The first group,
`edit-selection`, is native. The author keeps the working-layer tool in that
group, so the design target is:

```text
working-layer toolbar entry = GeoCeDG mode compatible with the Move group
```

For example, a click mode that adopts the layer of the clicked object and
offers a numeric chooser on an empty click. `P0` confirms the exact
implementation design. The status bar is complementary and may gain
interaction if `P0` recommends it, but it does not replace the Move-group tool.
The profile's action catalog grows, which changes the 115-action catalog pinned in
`geocedg-desktop/GeoCeDGProfile.java:340-343` and its tests. (The workspace
specification still names 110 stable action IDs at
`geocedg/specs/ui/cedg-workspaces.md:457-458`, a figure from before
`PRE-G9B-R4`.)

## 6. Export surface and export area: pre-characterization

### 6.1 What GeoCeDG exposes today

GeoCeDG never builds the upstream `FileMenuD`. Its menus are projected from the
profile's action catalog (`geocedg-desktop/GeoCeDGMenuBar.java:48-162`), which
has no export kind; the registry admits only two export-like targets, the DXF
dialog and Print Preview (`geocedg-desktop/GeoCeDGActionRegistry.java:50-71`).
The File menu shows New, Open, Open recent, the Classic diagnostic, Save, Save
as, Print preview, **Export 2D geometry as DXF (experimental)**, and Close. The
profile records PDF/SVG sheet production as `deferred` pending "independent
document/export design and author authorization"
(`apps/geocedg/application-profile.yml:3046-3051`).

Upstream routes nevertheless stay reachable outside the menu (PROVEN in source;
whether they fire in GeoCeDG needs a `P0` probe):

- `Ctrl+Shift+U` opens Graphics View as Picture and `Ctrl+Shift+C` copies to
  the clipboard;
- `Ctrl+Shift+W` opens the worksheet dialog, which **uploads to
  GeoGebra.org** rather than writing local HTML
  (`common/main/GlobalKeyDispatcher.java:678-681`,
  `desktop/gui/GuiManagerD.java:2971-2977`). Subphase `B` must close this route
  if it fires, since Dynamic Worksheet is excluded;
- `Ctrl+Shift+D`, GeoCeDG's DXF accelerator, is also the upstream chord that
  toggles "selection allowed" on all objects and fixes or unfixes sliders
  (`GlobalKeyDispatcher.java:898-952`). If both fire, it is a latent defect;
- the command-line `--export` options, the scripting API and the v1-profile
  fallback, which restores the full upstream menu including Collada because
  `AppGeoCeDG` extends `App3D`.

### 6.2 Area rules differ per family

| Family | Area rule (PROVEN) |
|---|---|
| PNG, PDF, EMF, Print, Clipboard, Animated GIF | `selectionRectangle` > `Export_1`/`Export_2` (+2 px) > full view (`EuclidianView.java:5206-5238`, `:5795-5824`) |
| SVG | same clip, but drawables are **not** re-updated against the export frame |
| PSTricks, PGF/TikZ, Asymptote | selection rectangle at frame open, else the visible view; bounds editable; `Export_1`/`Export_2` **ignored** (`common/export/pstricks/GeoGebraExport.java:240-258`) |
| DXF | whole construction or current selection; no view, no viewport, no `Export_1`/`Export_2` |

`selectionRectangle` is transient, per view and never serialized. It is set by
a right-drag, Select mode or Alt-drag, by the LaTeX frames when a bound is
edited, and by GeoCeDG's zoom-window tool; it survives until the next press or
mode change. A leftover rectangle can therefore crop a later picture export
silently (`P0` probe). `Export_1`/`Export_2` are found by **label**
(`kernel.lookupLabel`) and accepted only if both are 2D points.

### 6.3 Clipping and padding: the observations have source causes

`exportPaint` re-updates all drawables against the export frame "in case they
are off screen but within Export_1, Export_2 rectangle"
(`EuclidianView.java:5861-5882`). Lines, segments, rays, conics, polygons,
legacy-locus frames and `LocusV2` respect the frame. These do **not**:

- points are culled against the physical view size
  (`common/euclidian/draw/DrawPoint.java:180-187`);
- function graphs are sampled only over the view's x-range;
- axes, grid and some labels are drawn only inside the view;
- legacy `Locus` is sampled over the visible window plus half its width;
- SVG is not re-updated at all, so even lines stay clipped near the view edge.

The only padding is the `+2` px added when `Export_1`/`Export_2` is the area
(`EuclidianView.java:5216`, `:5234`). PDF page sizes are truncated to integers.
INFERRED until probed: an explicit area larger than the viewport loses points,
graphs, axes, grid, some labels and far legacy loci, and loses more in SVG.
That matches the principle `explicit ExportArea != viewport` failing today.

Both author-reported behaviors (§2) have a plausible source cause: the `+2` px
is a border outside the exact `Export_1`/`Export_2` rectangle, and the
view-bounded drawables explain clipping to visible content. These are
pre-characterization hypotheses. `P0` must reproduce and characterize both
observations against source and runtime, per format, on the exported files.

### 6.4 Units and scale per family

- Picture export: `printingScale` is cm per model unit, recomputed from zoom on
  every coordinate-system change and never serialized
  (`EuclidianView.java:311`, `:1930-1934`). The dialog's "Scale in cm" row is
  "*a* units = *b* cm" (`desktop/export/PrintScalePanel.java:140-145`).
- LaTeX: `xunit`/`yunit` in cm per model unit, default 1, independent of
  `printingScale`.
- DXF: `UNITLESS`, `$INSUNITS = 0`.
- INFERRED: at the standard zoom of 50 px/unit, `printingScale = 1`, the only
  upstream "1 unit = 1 cm" convention, and it is zoom-derived. It is not a
  model unit and must not be inferred as one.

### 6.5 Semantic curves

`GeoLocusV2 extends GeoElement` and deliberately implements neither `Path` nor
`GeoLocus`; `SplineV2` produces a `GeoLocusV2`. The renderer `DrawLocusV2`
draws an adaptive screen-tolerance tessellation (0.75 px) from the semantic
evaluator, so every view exporter renders it. The LaTeX dispatcher matches no
branch and logs "Export: unsupported GeoElement" at debug level, **silently
skipping** it (`GeoGebraExport.java:453-460`). DXF already exports it from
semantic evaluation in model coordinates, as an approximate `LWPOLYLINE` per
branch component with explicit tolerance and a sidecar manifest. That DXF path
is the model for subphase `C`.

## 7. Units and persistence: pre-characterization

- No physical length unit carries meaning anywhere. `Distance`, `Length` and
  `Area` are plain model numbers formatted without a suffix
  (`common/kernel/algos/AlgoDistancePoints.java:96-97`,
  `common/kernel/geos/GeoNumeric.java:662-710`).
- Every `cm` is an output scale (print, picture, LaTeX, SVG, STL, Collada) or a
  free-text axis label (`<axis unitLabel>`, options include `mm`, `cm`, `m`,
  `°`, `π`, `$`). Neither is read by geometry. Axis labels remain an independent
  per-view label.
- GeoCeDG already has unit-shaped notions to reconcile in `D0`: the export
  model's `Unit.UNITLESS`; `MetricUnit2D.CONSTRUCTION_LENGTH_UNIT` for Locus
  metrics; intersection residual units `"model-coordinate"`; and an opaque,
  equality-checked `units` token on version-2 spatial frame records.
- **Persistence owner for `constructionUnit` (candidate).** The `<construction>`
  element is reset per load, is in the undo snapshot and is **not** in the
  preferences XML; GeoCeDG already writes `<geocedgSpatial>` there
  (`common/kernel/Construction.java:1517-1550`). A flat, self-closing,
  versioned GeoCeDG element inside `<construction>` fits. `<kernel>` is
  rejected: it is not reset per load and it leaks into the preferences XML. A
  bare `<construction>` attribute is unversioned and is also emitted for macro
  constructions.
- **Unknown content.** Upstream ignores unknown attributes and logs and skips
  unknown elements (`common/io/MyXMLHandler.java:3093`). So an older GeoCeDG
  build would load a newer document, drop the unit and re-save without it.
  `D0` must decide whether that is acceptable or whether the element needs the
  fail-closed treatment `<geocedgSpatial>` already has.
- **Precedent for `presentationUnit`.** `angleUnit` keeps values in radians and
  converts only on output, with a template-gated suffix
  (`common/kernel/Kernel.java:2345-2425`). It is **not** pure presentation: it
  also changes input parsing (`sin(15)` becomes `sin(15°)`) and the rounding of
  angles. A presentation unit must copy the output-only part and must not
  change parsing or rounding.
- **Legacy default.** When the element is absent: `UNSPECIFIED_MODEL_UNIT`, no
  suffix, unchanged output. Migration stays lazy: nothing is written until the
  user sets a unit, so legacy documents round-trip byte-identically, following
  ADR 0029 and `durable-dependency-projection.md`. A user default never applies
  to a loaded document.
- **User preferences.** GeoCeDG keeps product preferences in a portable
  properties file with `geocedg.<area>.<name>.v1` keys, outside the document
  and the preferences XML (`geocedg-desktop/GeoCeDGPresentationPreferences.java`,
  `GeoCeDGThemePreference.java`). A default presentation unit for **new**
  documents would follow that precedent.
- `.cedg` is the same ZIP/XML archive as `.ggb`; a new layout, app code or
  format version needs a separate decision
  (`geocedg/specs/ui/native-document-identity.md`).

## 8. GGT library and native tools: pre-characterization

### 8.1 The supplied tools are already in the repository

All 16 author `.ggt` macros are construction-equivalent to macros embedded in
the G3 legacy package `cedg.legacy.template-v7`. They differ only by
serialization tags a later writer added and by three missing icon references
(planning report §4.1). So `E1` curates tools whose provenance, inventory and
first classification already exist: the
[G9U1 user-tool review](../validation/g9u1_user_tools_review.md) classes are
the starting dispositions below. The template manifest records redistribution
review as **blocked**. Twelve of the supplied icons carry GeoGebra stock
toolbar names.

**Provenance and licensing gate for `E1`.** Geometric equivalence with the
`template-v7` macros does not imply redistribution permission, and neither the
planning nor any later agent makes a legal conclusion. `P0` and `E1` must keep
four things distinct:

```text
macro behavior / reference      what a tool constructs; usable as a design reference
macro source / provenance       who authored the definition and under which terms
GeoCeDG-owned replacement icons new owned assets, never the bundled stock icons
redistributable packaged asset  only after an explicit rights record and author decision
```

### 8.2 Governance constraints on the library

- **Location (`AQ-G1`, open for `P0`).** The author requests a project library
  at `tools/ggtfiles/`. Current governance and provenance conventions point
  elsewhere: `AGENTS.md` §3.2 places `.ggt` resources under `models/` and
  tooling under `tools/`; `AGENTS.md` §5 requires existing `.ggt` tools to be
  imported first under `models/legacy/` with a manifest; and the accepted
  [controlled-integration contract](../../geocedg/specs/legacy/controlled-integration.md)
  fixes `original/` (immutable), `manifest.yml`, `curation.yml` and `derived/`,
  through `tools/legacy/ingest.ps1`. This is a real governance conflict, and no
  location is chosen here. `P0` must design a provenance-safe and
  packaging-safe structure. It may recommend a canonical reference or source
  under `models/legacy/`, a curated or generated distributable library
  elsewhere, a manifest-driven packaging route, an explicit governance
  amendment, or a combination, but must not choose by convenience.
- **Promotion.** Making a tool available by default is a promotion. It needs a
  rights review, characterization, a placement decision, deterministic tests
  and a feature-manifest change. Presence in a toolbar is never a promotion
  decision.
- **Packaging.** The accepted
  [Windows packaging contract](../../geocedg/specs/packaging/windows-packaging.md)
  requires the pipeline to **fail** if the package contains `.ggb`/`.ggt`
  models. The author intends to ship the approved GGT library, so packaging
  support is a required design target. The current exclusion is a contract
  that `E1` must reconcile explicitly (`AQ-G5`), not evidence that GGT
  distribution is unwanted. No packaging change is authorized by the planning.
- **User-tool library.** The existing library installs only from a user-chosen
  file, stores itself next to the preferences file
  (`<preferences>.user-tools-v1.json`), ignores a package's own
  `iconFileName`, and takes a separately chosen PNG pin icon
  (`geocedg-desktop/GeoCeDGUserTools.java`,
  `geocedg-desktop/GeoCeDGUserToolLibrary.java`). It rejects scripts, which
  matters because both sheet tools carry an empty `ggbscript onUpdate`
  (`P0` probe). A shipped collection is new behavior: either a read-only bundled
  catalog in the manager or a first-run seed of the library.
- **Icons.** No tool-icon generator exists. GeoCeDG tool icons are hand-authored
  SVGs registered with a canonical LF SHA-256 and a versioning policy in
  `assets-manifest.yml`. The only deterministic derivation script is
  `tools/resources/generate-geocedg-branding.ps1`, for branding. `E1` therefore
  designs owned SVG sources, a deterministic rasterizer for the library's PNG
  size, and manifest entries, on the branding-script precedent.

### 8.3 Style: fixed in the macro, not remembered

`AlgoMacro` copies each output's style from the macro definition and disables
visual defaults (`common/kernel/algos/AlgoMacro.java:288-315`). The style bar
cannot target macro outputs: macro modes have no entry in the default style map
and the just-created list is cleared. Line thickness cannot be driven by a macro
input. INFERRED: a remembered style is feasible only as a GeoCeDG frontend hook
on `MODE_MACRO` that finds the new `AlgoMacro` and applies a stored template,
persisted per command in the library store. `P0` decides whether that is
sustainable. Otherwise the author fallback applies, black at 3 pt, which means
restyling the **derived** curated definitions, never the originals. Today the
dimension tools use thickness 2 and the sheet trim edges use a dotted thickness
2.

### 8.4 Proposed disposition (subject to `P0`, `E1` and `AQ-G2`)

| Tool | G9U1 class | Proposed disposition |
|---|---|---|
| `directDimension`, `axisDimension` | 3 | behavioral references for native `E2`; retired from the shipped library once `E2` is accepted |
| `sheetISOAnLand`, `sheetISOAnVert` | 5 | behavioral references for native `E3`; in their future authorized phase the author-required modifications apply: hide the outer dotted rectangle and hide all corner points |
| `SquarebyDiagonal`, `CirclebyD`, `circArcbyAngle`, `ellipseLength12`, `IFPositiveSelectPoint` | 2 | library candidates: explicit user-owned conveniences over native primitives |
| `EllipseAxis` | 3 | library candidate with a documented validity domain: it assumes `C` lies on the axis perpendicular to `OB` |
| `conj2mainAxesEllipse` | 3 | library candidate (Rytz construction) |
| `pointJump`, `relCoor`, `translationCoor` | 4/6 | library candidates with an explicit statement that they carry no frame or projection authority |
| `DuctSymbol`, `SymmSymbol` | 5 | library presentation symbols |

### 8.5 Native dimensions (`E2`)

- **Kernel commands.** `DirectDimension` and `AxisDimension` need the dependency
  graph, dynamic evaluation, persistence and every frontend, so they are shared
  kernel commands. Each must pass the `PRE-G9B-R5-B` canonical-head and
  complete-inventory gates
  ([ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md)
  Decision 5), with an English canonical name and Spanish aliases. Each must also
  extend the `PRE-G9B-R6` GGBScript capability matrix, whose completeness gate
  fails closed for a new command.
- **Placement parameter.** An explicit, signed offset that is a construction
  input (`DOCUMENT_SEMANTIC`), draggable, and independent of `|AB|`. A command
  must not read the screen scale (`AGENTS.md` §10). The **tool** may choose the
  initial offset value from the view at creation time; after that the value is
  an ordinary construction parameter. This removes the legacy cause of
  far-away placement (planning report §4.3).
- **Value.** `Distance × conversion(constructionUnit → presentationUnit)`,
  formatted with the presentation unit's suffix. With an unspecified unit, the
  bare model value. No hidden multiplier.
- **Figure.** Text centred on the dimension line; a deterministic default side
  (above for a near-horizontal measure, left for a near-vertical one) derived
  from geometry, never from input order alone; text parallel to the line where
  feasible (`P0` checks rotated-text support).
- **Placement in the UI.** The `construction-metrics` presentation group
  (Metrics and validation, which holds `measure.distance-length`) and the
  Construction menu, with tool names, help and tips in English and Spanish
  through the existing `X.Tool`/`X.Help` keys. The profile's catalog size is
  pinned at 115 actions in
  `GeoCeDGProfile` (`:340-343`) and in tests; every new action updates that pin.
- **Representation.** Either a composite object with its own drawable and
  export adapters, or a composition of existing outputs (segments, arrows,
  text). The choice changes the export work and possibly the verification class
  (`AQ-E1`).

### 8.6 `IsoABorder` (`E3`)

- **Kernel command.** It depends on a construction point and is dynamic, so it
  is a shared kernel command. Inputs: upper-left point, ISO A size, orientation,
  drawing scale.
- **Sizes.** ISO 216 nominal millimetre sizes (A3 = 420 × 297 mm), not the
  legacy formula. Model length = paper length × scale denominator ÷
  `conversion(constructionUnit → mm)`. With an unspecified construction unit
  the border is undefined, or blocked behind a unit declaration (a `D0`
  decision).
- **Placement in the UI.** Menu only, under `Construction → Annotations and
  media` (`construction-annotations-media`, today holding only
  `presentation.image`). No toolbar button unless separately authorized.
- **Export linkage.** After the yes/no question, the border becomes a producer
  of the single `ExportArea` authority of `B`. There is no hidden second
  mechanism. Title blocks and margins stay with `G15` unless the author decides
  otherwise.

## 9. Orientation and authoring memory: pre-characterization

### 9.1 Kernel orientation (`F1`)

| Construction | Kernel direction (PROVEN) |
|---|---|
| `Line(A, B)`, `Segment(A, B)`, `Ray(A, B)` | `B − A` (`GeoVec3D.lineThroughPoints`, comment at `common/kernel/geos/GeoVec3D.java:273`: "we want AB to be the direction vector") |
| parallel line through `P` (`AlgoLinePointLine`) | same as the reference |
| perpendicular line through `P` (`AlgoOrthoLinePointLine`) | reference direction rotated +90° |
| `Line(P, v)` | `v` |

`GeoLine.getDirection` is `(y, −x)` (`common/kernel/geos/GeoLine.java:444-457`),
and path parameters run along it: a line over the whole real line, a segment
from start to end on `[0, 1]`, a ray from its start. Orientation is already
observable through `UnitVector`/`Direction`, `Angle(g, h)`, the parametric form
and point-on-path parameters.

**Pitfall.** `GeoLine.getDirectionInD3()` returns `(−y, x)`, the **opposite**
direction, when the line has no end point (`GeoLine.java:1402-1409`), which is
the case for parallel, perpendicular and point–vector lines. At least one
accessor therefore exposes a direction opposite to the construction
convention. `F1` must not choose an accessor merely because its name begins
with `getDirection`. Before `F1` is authorized, `P0` must establish the
**semantic orientation authority** for each supported object type (line,
segment, ray) and construction route, and name the accessor or derivation that
implements it. Pre-characterization only indicates that `getDirection` and the
path parameters agree with the construction convention.

Only `GeoSegment` has arrow decorations today; `DrawLine` and `DrawRay` have
none. `GeoCeDGEuclidianView.paint` already draws a transient non-semantic
overlay, but that override is not part of the export paint path, so an overlay
cue would not reach exports (`AQ-F1`).

### 9.2 Parallel and perpendicular memory (`F2`)

- Both tools accept a point and a line, vector or function in either order and
  fire as soon as both are selected. The selection getters clear the lists as
  they read them, so nothing is remembered.
- `EuclidianController.parallel()` is `protected final`; `orthogonal(Hits,
  boolean)` is overridable. INFERRED hook: a GeoCeDG session helper, on the
  `GeoCeDGSimilarityTools` precedent, driven from the existing
  `GeoCeDGEuclidianController.switchModeForProcessMode` override. It pre-seeds
  the line selection with the remembered reference and reads the used reference
  back from the new algorithm's input.
- **Gesture conflict.** With a line already selected, a click on another line
  creates a point on that line (upstream issue #2610 handling). "Reference
  first, then point" with a remembered reference therefore needs the helper to
  replace the seed on a line click rather than let the host create a point.
- **Lifecycle.** Undo and redo rebuild the construction, so remembered
  references go stale. `P0` must define clearing or re-validation on undo,
  redo, deletion, redefinition, New and Open, without label-based inference.

### 9.3 Angle with Given Size (`F2`)

The tool reaches `AlgoDispatcher.angle`, which rotates the point and builds
`AlgoAnglePoints`; there is no dedicated algorithm. The angle is visible
because `AlgoAngle.newGeoAngle` calls `setDrawableNoSlider()`. INFERRED hook: a
GeoCeDG controller companion whose `createAngle` hides the angle after creation
on the **tool path only**. The `Angle(A, B, α)` command and scripts stay
unchanged, as the author requires.

## 10. Architecture-owner matrix

The rule is `AGENTS.md` §4. "Kernel" means the shared Java kernel and its
shared document serialization. "View" means the shared `euclidian` view and
drawable code, which is frontend presentation even though it is shared.

| Capability | Owner | Kernel? | Reason |
|---|---|---|---|
| working-layer state | Desktop application session | no | authoring state; layer is not geometric truth |
| new-object layer assignment | application (`AppGeoCeDG.getMaxLayerUsed` override, candidate) | no | one creation seam reached by every route, with no kernel edit |
| hidden-layer set and `effectiveVisible` | application/view (`GeoCeDGEuclidianView` predicate) | no | presentation; must not change object visibility |
| Algebra View layer toggle | Desktop | no | GUI orchestration |
| status bar | Desktop | no | presentation and frontend state |
| export menu surface | profile and action registry | no | menu/profile presentation |
| `ExportArea` authority | application/session service | no | export state; never geometry |
| export render-area correction | view (export paint path, drawables) | no | rendering, consumed by every view exporter |
| `constructionUnit` | shared document model (`<construction>` serialization) | **yes** | document-semantic metadata that must serialize and reach every consumer; no geometric computation reads it |
| `presentationUnit` conversion | shared kernel output formatting | **yes** | dimension values are kernel objects updated in the graph; the conversion must be reachable from evaluation, like the output part of `angleUnit` |
| engineering export scale | export dialogs and adapters | no | export formatting |
| `LocusV2`/`SplineV2` LaTeX export | export adapter over the semantic evaluator | no | adapter consumes semantic geometry; the kernel is not an export formatter |
| DXF unit header | export adapter | no | export formatting under the amended export contract |
| GGT library | legacy store, owned resources, Desktop library, packaging | no | packaging, resources and tool library |
| tool icons | owned resources and a deterministic generator | no | icons and resources |
| `DirectDimension`, `AxisDimension` | shared kernel commands, Desktop modes, profile | **yes** | dependency graph, dynamic evaluation, persistence, several frontends |
| `IsoABorder` | shared kernel command, Desktop menu action, profile | **yes** | depends on a construction point; dynamic and persistent |
| `IsoABorder` → `ExportArea` linkage | application/session | no | orchestration of export state |
| orientation cue | view decoration reading kernel direction | no | presentation derived from kernel semantics, read-only |
| parallel/perpendicular memory | Desktop controller session helper | no | mode and session orchestration |
| hidden angle for Angle with Given Size | Desktop controller companion | no | tool-path authoring default; the command is unchanged |
| File → Insert surface, Insert File and Apply Template routes (`F3`) | profile, action registry and Desktop chooser routes, with minimal upstream-identical seams | no | menu, chooser and orchestration of existing document-consuming operations; no geometry semantics and no document-format change |
| help and guide | documentation and localization | no | help |

No row duplicates semantics in Desktop or Python. Four rows are kernel rows:
`constructionUnit`, the `presentationUnit` conversion and the two command
families. Shared serialization changes in `constructionUnit`, in the
persisted `presentationUnit` (§11), in the new command elements, and in
`hiddenLayers` if it is persisted (§11, `AQ-L3`). That last change is
presentation state in a document-level element (`A-2`), not kernel semantics,
but it still touches the shared XML handler.

## 11. Persistence matrix (candidates)

Only the first two rows are fixed by the author. Every other row is a candidate
for `P0` to confirm or replace, with evidence.

| State | Candidate class | Rejected alternatives | Consequence |
|---|---|---|---|
| `constructionUnit` | `DOCUMENT_SEMANTIC` (**author-fixed**) | — | new versioned GeoCeDG element in `<construction>`; `D1` is `GLOBAL_IMPACT` |
| legacy missing `constructionUnit` | `UNSPECIFIED_MODEL_UNIT` (**author-fixed**) | inferring `cm` | lazy: nothing written until the user sets a unit; legacy bytes round-trip |
| `presentationUnit` | `DOCUMENT_PRESENTATION`, with a `USER_PREFERENCE` default for **new** documents only | `SESSION` (dimension text would change on reopen); `USER_PREFERENCE` only (the same document would read differently per user) | serialized beside `constructionUnit`; the default never applies to a loaded document |
| `workingLayer` | `SESSION` | `DOCUMENT_PRESENTATION` (the CAD current-layer precedent) | no serialization; `P0` defines the value after New and Open (`AQ-L2`) |
| `hiddenLayers` | `DOCUMENT_PRESENTATION`, per view | `SESSION` (lost on reopen); document-wide | GeoCeDG child of `<euclidianView>`, excluded from preferences XML, captured by undo; needs one new upstream XML-handler case (`AQ-L3`). Superseded by the `A` author decisions and `A-2` (`DQ-A2-3`): document-wide, a document-level element after `</construction>`, never in undo or preferences XML |
| `ExportArea` | `SESSION` (author preference) | `DOCUMENT_PRESENTATION` | no serialization; `Export_1`/`Export_2` and `IsoABorder` remain document objects that act as producers |
| orientation-display option | `USER_PREFERENCE` (view-level toggle) | per-object `DOCUMENT_PRESENTATION` style; `SESSION` | portable-preferences key; no document change (`AQ-F1`) |
| parallel `lastReference` | `SESSION` | any persisted form | cleared or re-validated on undo, redo, delete, New, Open |
| perpendicular `lastReference` | `SESSION`, independent of the parallel one | — | as above |
| GGT remembered style | `USER_PREFERENCE`, per tool command, in the library store | `DOCUMENT_PRESENTATION` | only if `P0` finds it sustainable; otherwise none (fixed black 3 pt) |
| dimension placement parameter | `DOCUMENT_SEMANTIC` | `SESSION`, screen offset | a construction input of the dimension object |
| `IsoABorder` export linkage | `SESSION` (follows `ExportArea`) | `DOCUMENT_PRESENTATION` | the border itself is `DOCUMENT_SEMANTIC` |
| `effectiveVisible`, status-bar text, export frame, render tessellation | `DERIVED_TRANSIENT` | — | never serialized, never authority |

## 12. Recommended verification class per subphase

These are proposals for the author to freeze at each subphase start under
`verification-levels.md` §12.8. Each is chosen from the concrete impact, not
from the importance of the track.

| Subphase | Proposed class | Planned acceptance | Impact facts that select it | Would escalate if |
|---|---|---|---|---|
| `P0` | `DOCUMENTATION_STATUS_ONLY` | `STATIC` | documents only | any product, verifier or prompt-contract path |
| `A-1` | `INTEGRATED_PHASE` (author-accepted 2026-10-02) | registered `PHASE` + `INTEGRATION` | working layer on every interactive creation route, shared view painting and hit testing in Graphics 1 and 2, Algebra View, profile catalog and application layout | domain widening, any serialization, or an exporter change → stop; that scope is `A-2`, `B` or `C` |
| `A-2` | `GLOBAL_IMPACT` (author-accepted 2026-10-02) | `FINAL` | widens the shared `<layer>` domain read on every load and adds hidden-layer persistence to document serialization | — |
| `A` — **superseded by `A-1`/`A-2`**; historical record only, not an executable phase | `INTEGRATED_PHASE`, **provisional** | registered `PHASE` + `INTEGRATION` | the working layer applies to **every** creation route (tools, Algebra input, scripts, macros, paste, load and undo rebuild), which is concrete integration coverage; view predicate; profile catalog | `P0` re-classifies `A` from its layer-extension design: widening shared layer or serialization assumptions for the author's arbitrary-layer target, moving the predicate into `GeoElement`, or persisting hidden layers through the shared XML handler (unless `AQ-L3` chooses `SESSION`) → `GLOBAL_IMPACT` |
| `B` | `INTEGRATED_PHASE` (author-proposed 2026-10-02; frozen at authorization) | registered `PHASE` + `INTEGRATION` on the same commit and tree | profile surface; shared export-area seam and export paint path consumed by picture, preview, clipboard, print, command line, `ExportImage` and API; export viewport; shortcut routes; v1 fallback; GGBScript matrix rows | export area becomes serialized, or an unplanned global widening of the renderer → stop and reclassify |
| `D0` | `DOCUMENTATION_STATUS_ONLY` | `STATIC` | ADR and specification candidates | — |
| `D1` | `GLOBAL_IMPACT` | `FINAL` | new document-semantic serialization in shared `<construction>` XML, read on every load, written on every save and undo; legacy byte-identity must be proven across all documents | — |
| `C` | `INTEGRATED_PHASE` | registered `PHASE` + `INTEGRATION` | shared LaTeX export package; DXF header under an amended accepted spec; no document serialization. Concrete integration coverage: three LaTeX exporters, the picture exporters and DXF must agree on one export area, one unit contract and one effective-visibility rule | the export area or scale becomes document-serialized |
| `E1` | `BOUNDED_PHASE` | registered `PHASE` + packaging and static evidence | legacy store, owned resources, library, packaging contract; `AGENTS.md` §14 requires the packaging smoke when packaging changes | — |
| `E2` | `INTEGRATED_PHASE` | registered `PHASE` + `INTEGRATION` | new shared commands with additive XML; ADR 0031 gates; GGBScript matrix; profile pin | a new `GeoElement` class or XML element type → `GLOBAL_IMPACT` |
| `E3` | `INTEGRATED_PHASE` | registered `PHASE` + `INTEGRATION` | new shared command; `ExportArea` producer; units | as `E2` |
| `F1` | `BOUNDED_PHASE` | registered `PHASE` | presentation-only decoration and option | the cue is exported or persisted per object |
| `F2` | `BOUNDED_PHASE` | registered `PHASE` | Desktop mode orchestration only | a command or algorithm changes |
| `F3` | `INTEGRATED_PHASE` (author planning assignment 2026-10-03; frozen at authorization) | registered `PHASE` + `INTEGRATION` | GeoCeDG File surface and profile pins; chooser routes in the shared upstream `GuiManagerD`; two document-consuming operations (Insert File through the hidden helper and the copy buffer, Apply Template through the shared `TemplateHelper`); the `D1` Insert File provenance; no serialization and no kernel geometry | shared serialization or parser change, a Classic semantic change of `ConstructionDefaults` or `TemplateHelper`, or a global verification-infrastructure change → stop for author review |
| `G` | `INTEGRATED_PHASE` | `INTEGRATION` on `G`'s own candidate | integrates the accepted subphases; no evidence is inherited from any earlier subphase, including `D1`'s `FINAL` | a subphase changed global verification infrastructure → `FINAL` |

Registering a phase selection is ordinary phase-local work and does not make a
subphase an operational-infrastructure phase. It does update the registry-shape
pins in the infrastructure tests, which each subphase must reconcile through
the official mechanism.

## 13. Author decisions requested

Rows marked **AUTHOR TARGET FIXED**, **AUTHOR-SUPPLIED REQUIREMENT** or
**RESOLVED** were settled by the author on 2026-10-01 and recorded in the
documentary reconciliation `D_R6PLUS_PLAN`. Rows marked **OPEN** or
**DESIGN TARGET** are design work for `P0`. The remaining rows are still open
questions; none is decided by this note. Each row names the earliest subphase
it blocks.

On 2026-10-02 the author decided `AQ-L1a`, `AQ-L1b`, `AQ-L1c`, `AQ-L2`,
`AQ-L3`, `AQ-L4`, `AQ-L5`, `AQ-L7` and `AQ-L8`. The
[A author-decision record](../validation/pre_g9b_r6_plus_a_author_decisions_record.md) is their authority
and supersedes the planning recommendations in the corresponding rows below.

On 2026-10-02 the author also decided `AQ-X1` to `AQ-X7`. The
[B author-decision record](../validation/pre_g9b_r6_plus_b_author_decisions_record.md)
is their authority and supersedes the planning recommendations in the `AQ-X`
rows below.

On 2026-10-02 the author also decided `AQ-U2` to `AQ-U6`, the undo semantics
of the unit state and the unit-independence invariant. The
[D0 author-decision record](../validation/pre_g9b_r6_plus_d0_author_decisions_record.md)
is their authority and supersedes the planning recommendations in the `AQ-U`
rows below. It does not change §4.

| ID | Question | Blocks | Planning recommendation |
|---|---|---|---|
| `AQ-L1` | **AUTHOR TARGET FIXED**: the working layer is not restricted by the current 0..9 UX/domain (§2, §5.1). | `P0` | `P0` designs the least invasive correct extension and classifies `A` from its real impact |
| `AQ-L2` | Is the working layer `SESSION` or `DOCUMENT_PRESENTATION`, and what is it after New and Open? | `A` | `SESSION` |
| `AQ-L3` | Are hidden layers per view or document-wide, persisted or session-only, and are hidden-layer objects excluded from LaTeX and DXF export (DXF could carry them on an "off" layer)? | `A`, `C` | per view, persisted; excluded from LaTeX; DXF policy decided in `C` |
| `AQ-L4` | **AUTHOR TARGET FIXED**: the working-layer toolbar entry is a GeoCeDG mode compatible with the Move group; the status bar is complementary (§2, §5.5). | `P0` | `P0` confirms the exact implementation design |
| `AQ-L5` | Do the upstream `ShowLayer`/`HideLayer` commands stay unchanged? | `A` | unchanged; rerouting them would make them GeoCeDG-modified commands that must enter the GGBScript matrix |
| `AQ-X1` | **AUTHOR-SUPPLIED REQUIREMENT**: exact `Export_1`/`Export_2` rectangle with no outside border, and the complete rectangle even outside the viewport, for PNG, PDF, SVG and EMF(+) (§2). | `P0` | `P0` reproduces and characterizes against source and runtime; `B` implements |
| `AQ-X2` | Precedence of `ExportArea` producers, and does a leftover selection rectangle still count? | `B` | explicit area > `Export_1`/`Export_2` > viewport; a leftover rectangle never counts |
| `AQ-X3` | Close the hidden upstream export routes, including the `Ctrl+Shift+W` upload, if `P0` proves they fire? | `B` | yes |
| `AQ-X4` | Are Graphics View to Clipboard, Animated GIF and Print Preview in the export surface and under `ExportArea`? | `B` | keep Print Preview; decide the other two explicitly |
| `AQ-U1` | **RESOLVED — AUTHOR CONFIRMED**: printed and exported physical size is invariant under an equivalent presentation-unit change; `presentationUnit` is never a second scale factor (§4.4). | `D0` | carried into the `D0` specification |
| `AQ-U2` | Which results present units: native dimensions only, or also `Distance`, `Length`, `LocusLength`? | `D0` | native dimensions only, first |
| `AQ-U3` | Admitted unit set and behavior of older builds that ignore the new element. | `D0` | `mm`, `cm`, `m`; a decision on fail-closed versus lenient |
| `AQ-G1` | **OPEN — DESIGN WORK FOR `P0`**: repository structure reconciling the requested `tools/ggtfiles/` library with the `models/legacy/` provenance conventions (§8.2). | `E1` | `P0` designs a provenance-safe and packaging-safe structure, not chosen by convenience; the author decides |
| `AQ-G2` | Which of the 16 tools ship, and are the dimension and sheet tools retired once native replacements exist? | `E1` | §8.4 |
| `AQ-G3` | **AUTHOR-SUPPLIED REQUIREMENT**: in their future authorized phase, the sheet references hide the outer dotted rectangle and all corner points (§2). | `E1` | characterized by `P0`; implemented only when authorized |
| `AQ-G4` | Provenance and licensing record for redistributing the macro definitions, with GeoCeDG-owned replacement icons (§8.1 gate). | `E1` | an explicit rights record and author decision; no legal conclusion by an agent |
| `AQ-G5` | **DESIGN TARGET**: how packaging supports the approved GGT library, reconciling the current `.ggt` exclusion (§8.2). | `E1` | `P0` designs the route; the contract amendment needs its own author approval |
| `AQ-E1` | Native dimension representation: composite object or composition of existing outputs? | `E2` | `P0` recommends from export and verification cost; the author decides |
| `AQ-E2` | Does `IsoABorder` draw border only, without title block or margin? | `E3` | border only; title blocks stay with `G15` |
| `AQ-F1` | Is the orientation cue a global or per-object option, and is it exported? | `F1` | global, screen-only |
| `AQ-R7` | The future `PRE-G9B-R7` prompt still says the `R6-plus` scope is "pending author definition". | planning closeout | minimal wording reconciliation, which the author authorized for the planning closeout |

## 14. Authorization state

```text
PRE-G9B-R6                 = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus            = PLANNING PASS — AUTHOR APPROVED
                             PRODUCT IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-R6-plus PLANNING   = PASS — AUTHOR APPROVED (2026-10-01)
PRE-G9B-R6-plus-P0         = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-02; T_R6PLUS_P0 6b7fd5de)
PRE-G9B-R6-plus-A-1        = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-02; T_R6PLUS_A1 2a71133a; AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B          = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-02; T_R6PLUS_B 848206c4; AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-D0         = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-02; T_R6PLUS_D0 8383a153;
                             ADR 0032 and unit-system v1.0 author-approved)
PRE-G9B-R6-plus-D1         = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-03; T_R6PLUS_D1 6e1d8459; AUTHOR_SMOKE = PASS;
                             revision 2 after the author smoke of b55aa817)
PRE-G9B-R6-plus-A-2        = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-04; T_R6PLUS_A2 06a2ea6b, revision 3;
                             AUTHOR_SMOKE = PASS; FINAL verification-814f89fa;
                             revisions e3cbdc60 and c1c08e90 historical evidence)

PRE-G9B-R6-plus-C          = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-06; T_R6PLUS_C 6248bf0f, revision 1;
                             AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT;
                             PHASE verification-a198a69b and INTEGRATION
                             verification-0bcf9709; b981f8ea and its runs
                             historical evidence)

PRE-G9B-R6-plus-C-X1       = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-06; T_R6PLUS_C_X1 eabe6368; AUTHOR_SMOKE = PASS;
                             PHASE verification-f07334b6; BOUNDED_PHASE; post-C
                             corrective activity, operational order unchanged)

PRE-G9B-R6-plus-E1         = PASS — AUTHOR APPROVED (aggregate, 2026-10-07: E1-L and
                             E1-P closed; split into E1-L and E1-P by the author on
                             T_R6PLUS_E1_PREP 0ce52a96; DQ-E1-1 to DQ-E1-16 decided;
                             not reopened by OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER)
PRE-G9B-R6-plus-E1-L       = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-07; T_R6PLUS_E1_L 5f34d181, revision 1;
                             AUTHOR_SMOKE = PASS (R0 functional, R1 hover tips);
                             PHASE verification-1c7d88a3; BOUNDED_PHASE; R0 1aa05d67
                             historical evidence; ADR 0033 and the curated-library
                             specification author-approved)
PRE-G9B-R6-plus-E1-P       = PASS — AUTHOR APPROVED
                             (2026-10-07; T_R6PLUS_E1_P dcda4075; AUTHOR_SMOKE = PASS
                             WITH ACCEPTED PRE-EXISTING DEBT; PHASE verification-81ffec9c,
                             INFRA_UNIT verification-ace3de87, PACKAGING verification-e3c98ad1,
                             -a40cda69 (INTERNAL), -ce43e964 (NC);
                             OPERATIONAL_VERIFICATION_INFRASTRUCTURE, PHASE_LOCAL;
                             INTERNAL and NC include the library, COMMERCIAL excluded)
PRE-G9B-R6-plus-E1-X1      = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-07; T_R6PLUS_E1_X1 b12ab32c; AUTHOR_SMOKE = PASS;
                             PHASE verification-1286494a; BOUNDED_PHASE; DQ-E1X1-4
                             accepted; packaging product change none)
                             earlier: TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                             (implementation authorized 2026-10-07, 51bb677e; CANDIDATE C —
                             PRIMARY LAUNCHER EARLY CLASSIC DISPATCH; BOUNDED_PHASE, one
                             registered PHASE, no INTEGRATION, no FINAL; AUTHOR_SMOKE =
                             PENDING; packaging product change none)
                             earlier: CHARACTERIZED / IMPLEMENTATION PREPARED — PROMPT
                             PREPARED — NOT AUTHORIZED (2026-10-07; post-E1 focal
                             activity for OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER;
                             recommended jpackage additional launcher; proposed
                             OPERATIONAL_VERIFICATION_INFRASTRUCTURE, PHASE_LOCAL;
                             DQ-E1X1-1 to DQ-E1X1-9 pending; E1 not reopened)
PRE-G9B-R6-plus-E1-X1-R1   = CHARACTERIZATION COMPLETE (2026-10-07; A-versus-C
                             micro-characterization; preferred implementation
                             CANDIDATE C — PRIMARY LAUNCHER EARLY CLASSIC DISPATCH;
                             prompt reconciled, PREPARED — NOT AUTHORIZED; proposed
                             BOUNDED_PHASE; DQ-E1X1-1 to DQ-E1X1-8 pending)
PRE-G9B-R6-plus-E2         = PASS — AUTHOR APPROVED — PUBLISHED (2026-10-08 author
                             decision on the revised T_R6PLUS_E2 3a724661, tree
                             00b5230d; AUTHOR_SMOKE = PASS WITH ACCEPTED NON-BLOCKING
                             OBSERVATIONS; PHASE verification-ca5dd07b 7/7 and
                             INTEGRATION verification-48829d56 19/19 ACCEPTED /
                             COMPLETE; native-dimensions 1.1 and unit-system
                             amendment 1.1 NORMATIVE / AUTHOR APPROVED, ADR 0034
                             ACCEPTED; OBS-E2-1 to OBS-E2-4 registered for POST-E2,
                             implementation NOT AUTHORIZED; selfApproved = false)
                             earlier: REVISED TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                             (2026-10-08 author smoke follow-up: AUTHOR_SMOKE = PASS
                             WITH OBSERVATIONS on 878ff66b, kept as historical
                             evidence; decisions 5a55ba24; B1 dimension segment
                             thickness 2 with a creation preference; B2 dimension
                             unit suffix policy, geocedgUnits version 2, hidden for
                             new documents; PHASE + INTEGRATION on the revised
                             commit; focused author re-smoke of B1/B2 pending;
                             selfApproved = false)
                             earlier: TECHNICAL CANDIDATE PENDING AUTHOR REVIEW (2026-10-08;
                             implementation authorized 4a7043f2 on B_R6PLUS_E2_IMPL
                             b1ee68c0; β with existing types; AlignedDimension /
                             LinearDimension; DQ-E2-6 aligned value implemented;
                             INTEGRATED_PHASE: PHASE + INTEGRATION on the candidate
                             commit; author smoke pending; selfApproved = false)
                             earlier: PREPARED — AUTHOR DECISIONS FROZEN — IMPLEMENTATION
                             NOT AUTHORIZED (2026-10-07; DQ-E2-1 to DQ-E2-11 frozen on
                             T_R6PLUS_E2_PREP 6ad853c6; AlignedDimension / LinearDimension;
                             β and INTEGRATED_PHASE approved for design; DQ-E2-6 aligned
                             value FEASIBLE WITH β, bounded drawable seam, pending author
                             review)
                             earlier: PREPARED — NOT AUTHORIZED
                             (2026-10-07; characterization COMPLETE on P_R6PLUS_E1_P_X1
                             dc63b0e5; canonical prompt prepared; DQ-E2-1 to DQ-E2-11
                             pending; proposed INTEGRATED_PHASE not frozen;
                             implementation NOT AUTHORIZED)
                             earlier: NOT AUTHORIZED (NEXT after E1-P-X1, verification
                             baseline clean; requires a separate explicit author instruction)
PRE-G9B-R6-plus-E3         = PASS — AUTHOR APPROVED (with PRE-G9B-R6-plus-E3-R1)
                             (2026-10-10; approved candidate T_R6PLUS_E3_R1 7cd50116,
                             corrective successor of 257c854a; AUTHOR_SMOKE = PASS WITH
                             ACCEPTED NON-BLOCKING VERIFICATION LIMITATION; Asymptote with
                             label UNAVAILABLE — NOT CLAIMED PASS; publication NOT
                             AUTHORIZED; selfApproved = false)
                             earlier: TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                             with corrective candidate PRE-G9B-R6-plus-E3-R1
                             (2026-10-10; DXF container and LaTeX export; INTEGRATED_PHASE;
                             focused author smoke PENDING; publication NOT AUTHORIZED)
                             earlier: TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                             (2026-10-10; Stage B on Stage A 3a577ea4; INTEGRATED_PHASE:
                             PHASE + INTEGRATION on the candidate commit, reported
                             outside the candidate report; AUTHOR_SMOKE PENDING;
                             publication NOT AUTHORIZED; selfApproved = false)
                             earlier: DESIGN — AUTHOR APPROVED — RECONCILED
                             IMPLEMENTATION AUTHORIZED (2026-10-10, on ff560991;
                             INTEGRATED_PHASE frozen; OTQ-E3-1 resolved; numerical
                             contract corrected; publication of the implementation
                             NOT AUTHORIZED; selfApproved = false)
                             earlier: DESIGN — AUTHOR APPROVED — PUBLISHED
                             CANONICAL PROMPT PREPARED — NOT AUTHORIZED — PUBLISHED
                             IMPLEMENTATION NOT AUTHORIZED
                             (2026-10-10; PRE-G9B-R6-plus-E3-PREP-CLOSEOUT; DQ-E3-1 to
                             DQ-E3-16 decided by the author; INTEGRATED_PHASE approved
                             for the future implementation; OTQ-E3-1 to be disposed of
                             at implementation authorization; next: author review of
                             the published design package; selfApproved = false)
                             earlier: PREPARED — NOT AUTHORIZED
                             (2026-10-09; PRE-G9B-R6-plus-E3-PREP; characterization
                             COMPLETE on P_R6PLUS_POST_E2_P4 13e08ac1; canonical prompt
                             prepared; DQ-E3-1 to DQ-E3-16 pending; proposed
                             INTEGRATED_PHASE not frozen; preparation class
                             DOCUMENTATION_STATUS_ONLY; implementation and
                             publication NOT AUTHORIZED; selfApproved = false)
                             earlier: NOT AUTHORIZED (documentary readiness only,
                             POST-E2-P4 closeout)
POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION = PASS — AUTHOR APPROVED (2026-10-10, POST-E3
                             author reconciliation; D0 for the legacy-load attribution;
                             integrated as ed3f7a5e; publication NOT AUTHORIZED)
                             earlier: CHARACTERIZATION COMPLETE — PENDING AUTHOR
                             REVIEW (2026-10-10; on P_R6PLUS_E3 31286a23; pre-existing
                             upstream lazy reverse-table refill, not legacy-load specific;
                             user-visible only via the scripting language API; correction
                             NOT REQUIRED, D0 recommended, NOT AUTHORIZED;
                             docs/validation/post_e3_es_command_lookup_characterization_report.md)
                             earlier: PREPARED — CHARACTERIZATION AND DESIGN AUTHORIZED —
                             CORRECTION NOT AUTHORIZED (2026-10-10, E3/E3-R1 closeout; order 1)
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE = PASS — AUTHOR APPROVED; C1 DESIGN AUTHOR APPROVED
                             (2026-10-10; integrated as 61dbb0ad; C1 implementation NOT
                             AUTHORIZED here: separate task POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1
                             on the published integration; planned BOUNDED_PHASE)
                             earlier: CHARACTERIZATION COMPLETE — PENDING AUTHOR REVIEW
                             (2026-10-10; on P_R6PLUS_E3 31286a23; pre-existing since E2:
                             unbounded upstream formatter vs the PSTricks 9-digit limit;
                             correction C1 designed, BOUNDED_PHASE, prompt PREPARED — NOT
                             AUTHORIZED; new OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION
                             recorded; docs/validation/post_e3_e2_pstricks_dimension_angle_characterization_report.md)
                             earlier: PREPARED — CHARACTERIZATION AND CORRECTIVE DESIGN
                             AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED (2026-10-10; order 2)
POST-E3-LEGACY-INVENTORY-CASE-FIX = PASS — AUTHOR APPROVED (2026-10-10, original candidate
                             28e9aebe; integrated as 718c4081, equivalent; publication NOT
                             AUTHORIZED)
                             earlier: TECHNICAL CANDIDATE PENDING AUTHOR REVIEW (2026-10-10;
                             on P_R6PLUS_E3 31286a23; readiness gate passed; class
                             OPERATIONAL_VERIFICATION_INFRASTRUCTURE; ordinal label map in
                             tools/legacy/ingest.ps1; 24 of 169 template-v7 I/O types
                             corrected, originals unchanged; publication NOT AUTHORIZED;
                             docs/validation/post_e3_legacy_inventory_case_fix_candidate_report.md)
                             earlier: PREPARED — BOUNDED IMPLEMENTATION AUTHORIZED — SUBJECT
                             TO CHARACTERIZATION AND READINESS GATE (2026-10-10; order 3)
POST-E3-INTEGRATION        = AUTHOR APPROVED — PUBLISHED (2026-10-10; main = 00b525fd,
                             tree 23abd1ca; the three POST-E3 outcomes PASS — AUTHOR
                             APPROVED — PUBLISHED)
                             earlier: LOCAL INTEGRATION CANDIDATE — PENDING AUTHOR REVIEW (2026-10-10;
                             phase/post-e3-followups-author-integration on P_POST_E3 31286a23;
                             publication NOT AUTHORIZED; next after publication:
                             POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1; F1 NOT AUTHORIZED)
POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1 = PASS — AUTHOR APPROVED — PUBLISHED (2026-10-11;
                             AUTHOR_SMOKE = PASS; candidate 22a215d8 on 00b525fd; PHASE
                             verification-6fc36073 3/3; docs/validation/post_e3_e2_pstricks_dimension_angle_c1_closeout_record.md;
                             next: PRE-G9B-R6-plus-F1 readiness review, F1 NOT AUTHORIZED)
                             earlier: TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
                             (2026-10-10; implementation authorized on 00b525fd; BOUNDED_PHASE;
                             PSTricks dimension rotation with at most six decimals,
                             |dtheta| <= 5e-7 deg; host rotations unchanged; publication NOT
                             AUTHORIZED; docs/validation/post_e3_e2_pstricks_dimension_angle_c1_candidate_report.md)
PRE-G9B-R6-plus-F1         = PREPARED — NOT AUTHORIZED
                             (2026-10-11; PRE-G9B-R6-plus-F1-PREP; characterization
                             COMPLETE on P_C1 bde2827b; canonical prompt prepared;
                             DQ-F1-1 to DQ-F1-9 pending; proposed BOUNDED_PHASE not
                             frozen; preparation class DOCUMENTATION_STATUS_ONLY;
                             implementation and publication NOT AUTHORIZED;
                             selfApproved = false)
                             earlier: NOT AUTHORIZED (readiness review named at the
                             C1 closeout)
PRE-G9B-R6-plus-F2         = NOT AUTHORIZED

PRE-G9B-R6-plus-F3         = PLANNED — AUTHOR APPROVED — PUBLISHED
                             IMPLEMENTATION NOT AUTHORIZED
                             (2026-10-03; takes DEBT-R6PLUS-FILE-INSERT-SURFACE)
PRE-G9B-R6-plus-F4         = PLANNING INTENT RECORDED — IMPLEMENTATION NOT AUTHORIZED
                             (author planning decision recorded at the POST-E2-P4
                             closeout, 2026-10-09: to be proposed between F3 and G;
                             intended ownership: OBS-POST-E2-R2-COMPACT-STYLEBAR-
                             THICKNESS-INPUT, DXF compatibility recheck with AutoCAD
                             2026, TD-PDF-1, TD-PDF-2; no implementation contract or
                             verification class defined; direct working-layer entry
                             stays with F2; G later reconciles the remaining
                             historical debts into a structured register)
DEBT-R6PLUS-FILE-INSERT-SURFACE = ASSIGNED TO PRE-G9B-R6-plus-F3 — NOT AUTHORIZED

PRE-G9B-R6-plus-G          = NOT AUTHORIZED

PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus GLOBAL CLOSEOUT

G9B                        = NOT AUTHORIZED

OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET = OPEN — NOT A-2 BLOCKING —
                             IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE
                             DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT
OBS-R6PLUS-NEW-DOCUMENT-PREFERENCE-NUMERIC-SAVE-PROMPT = PRE-EXISTING — REPRODUCED
                             ON P_R6PLUS_D1 2941ddf2 — NOT CAUSED BY A-2 —
                             AUTHOR-AUTHORIZED FOCAL CORRECTION DURING A-2 REVIEW —
                             CLOSED
OBS-R6PLUS-EXPORT-PREVIEW-ABSENT = NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED (author
                             disposition DQ-C10, 2026-10-04; the guides state
                             the Save-preview conditions)
OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT = RECORDED BY THE C RECONCILIATION —
                             A tool replacement reloads the document with a
                             clearing setXML that A-2 commits as a document
                             transition (working layer recomputed) — A-2
                             BEHAVIOR — NOT C SCOPE — NOT FIXED — AUTHOR DISPOSITION
ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY = ENHANCEMENT — NOT A-2 BLOCKING —
                             IMPLEMENTATION NOT AUTHORIZED (preferred owner F2)
ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE = ENHANCEMENT — OWNER PRE-G9B-R6-plus-E3 —
                             IMPLEMENTATION NOT AUTHORIZED (recorded by the C
                             smoke remediation, 2026-10-06): a stable, exportable
                             and hideable construction label such as "A3 — 1:50";
                             a transient, non-exported coordination indicator
                             comparing the scale captured by IsoABorder with the
                             current session drawingScale; no geometric
                             dependency on the live drawingScale
                             CHARACTERIZED BY THE E3 PREPARATION (2026-10-09):
                             at the base a scale mismatch changes the exported
                             page silently; label and indicator designs are
                             DQ-E3-11 and DQ-E3-12, pending — NOT IMPLEMENTED
                             DESIGN AUTHOR APPROVED (2026-10-10, DQ-E3-11 and
                             DQ-E3-12: constructive label; physical-size and
                             scale-label coherence, "Use sheet scale", activation
                             warning) — IMPLEMENTATION NOT AUTHORIZED
                             IMPLEMENTED BY E3 — APPROVED WITH E3 (2026-10-10, E3/E3-R1 closeout)
OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH = OPEN — CORRECTION DESIGNED (DQ-E3-12, 2026-10-10)
                             — NOT RESOLVED (at the base a model-space sheet exported
                             at another session scale changes page size silently;
                             resolved only by an accepted E3 implementation)
                             RESOLVED — E3 PASS — AUTHOR APPROVED (2026-10-10)
OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES = OPEN — OUTSIDE E3 — NOT FIXED
                             (recorded by the E3 preparation, 2026-10-09:
                             tools/legacy/ingest.ps1 keys a case-insensitive map by
                             label, so models/legacy/template-v7/derived/tool-inventory.yml
                             records e.g. D as conic; neither file is changed by E3;
                             needs its own characterization and authorization)
                             BOUNDED CORRECTION AUTHORIZED (2026-10-10, E3/E3-R1 closeout) as
                             POST-E3-LEGACY-INVENTORY-CASE-FIX — CORRECTED IN THE LOCAL
                             TECHNICAL CANDIDATE (2026-10-10) — AUTHOR REVIEW PENDING
                             CORRECTION PASS — AUTHOR APPROVED (2026-10-10, 28e9aebe;
                             integrated as 718c4081; publication pending)
OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP = OPEN — OUTSIDE E3 — RETAINED (E3 candidate report
                             §12: after loadXML of Templatev7.ggb and setLanguage("es") no
                             Spanish command name resolves in the Desktop test harness)
                             CHARACTERIZATION AUTHORIZED (2026-10-10) as
                             POST-E3-ES-COMMAND-LOOKUP-CHARACTERIZATION — CORRECTION NOT
                             AUTHORIZED — NOT STARTED
                             DISPOSITIONED — NOT LEGACY-LOAD SPECIFIC (author decision D0,
                             2026-10-10); the narrower cause is
                             OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP
OBS-POST-E3-SCRIPT-LANGUAGE-COMMAND-LOOKUP = OPEN — PRE-EXISTING UPSTREAM BEHAVIOR —
                             USER-VISIBLE THROUGH SCRIPTING API — PRODUCT CORRECTION NOT
                             AUTHORIZED (registered 2026-10-10: a language switch through
                             the scripting API can leave localized command lookup stale;
                             English canonical names stay usable; independent of legacy
                             documents; reproduced in the real GUI)
OBS-R6PLUS-E2-PSTRICKS-DIMENSION-ANGLE = OPEN — PRE-EXISTING ON 257c854a — NOT ATTRIBUTED
                             TO E3-R1 (E3-R1 report §4.5: 14-decimal \rput rotation of an E2
                             dimension value; latex "Number too big")
                             CHARACTERIZATION AUTHORIZED (2026-10-10) as
                             POST-E3-E2-PSTRICKS-DIMENSION-ANGLE — IMPLEMENTATION NOT
                             AUTHORIZED — NOT STARTED
                             CHARACTERIZATION PASS — AUTHOR APPROVED; C1 DESIGN AUTHOR
                             APPROVED (2026-10-10); C1 implementation a separate task —
                             NOT AUTHORIZED YET
                             RESOLVED BY C1 — PASS — AUTHOR APPROVED — PUBLISHED (2026-10-11,
                             22a215d8; six-decimal PSTricks dimension rotation)
OBS-R6PLUS-PSTRICKS-HOST-ROTATION-PRECISION = OPEN — PRE-EXISTING — OUTSIDE C1 —
                             IMPLEMENTATION NOT AUTHORIZED (registered 2026-10-10: inherited
                             PSTricks rotations of other objects, e.g. a rotated ellipse, fail
                             the same way; not assigned to F1)
OBS-R6PLUS-E3-R1-ASYMPTOTE-LABEL-COMPILATION-UNVERIFIED = ACCEPTED NON-BLOCKING
                             VERIFICATION LIMITATION (2026-10-10, E3/E3-R1 closeout):
                             Asymptote with label UNAVAILABLE in the agent environment —
                             NOT CLAIMED PASS; independent tracking required; no owner
                             authorized
OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT = REPRODUCED BY AUTHOR — PRE-EXISTING —
                             NOT CAUSED BY C — ACCEPTED DEBT — NOT C-BLOCKING —
                             DEFERRED TO POST-C (author disposition at the C
                             closeout, 2026-10-06; upstream FileMenuD builds and
                             shows GraphicExportDialog on a background thread;
                             EDT layout NullPointerExceptions on the base
                             e6135028 and on C alike; the author reproduced the
                             empty dialog and hang); NOT FIXED — no Classic or
                             upstream code changed; acceptance of C does not
                             resolve it; a separate post-C activity with its own
                             characterization and authorization — NOT AUTHORIZED
                             RESOLVED IN PRE-G9B-R6-plus-C-X1 — AUTHOR APPROVED
                             (2026-10-06; T_R6PLUS_C_X1 eabe6368, AUTHOR_SMOKE = PASS;
                             the C-time disposition above stays historically
                             unchanged; the resolution belongs to C-X1)
OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES = RECORDED BY C-X1 (DQ-X1-4) — PRE-EXISTING —
                             NOT FIXED — AUTHOR DISPOSITION (Classic startup through
                             GeoGebra.doMain without the EDT, observed once as a JToolBar
                             layout failure at startup; GeoGebraMenuBar.showPrintPreview,
                             also used by GeoCeDG v2; the Save and Clipboard buttons of
                             GraphicExportDialog, Classic and GeoCeDG; the worker-thread
                             FileMenuD actions newWindowAction, drawingPadToClipboardAction,
                             exportWorksheet and exportGeoGebraTubeAction)
OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION = PRE-EXISTING SINCE G5 (AutoCAD 2024
                             refuses the minimal AC1015 DXF container: "Error in
                             APPID Table"; the entity encoding, including
                             60 = 1, is honoured inside an accepted container) —
                             NOT C SCOPE — NOT FIXED — AUTHOR DISPOSITION
                             RESOLVED BY E3-R1 FOR AUTOCAD 2026 — AUTHOR CONFIRMED
                             (2026-10-10, E3/E3-R1 closeout); other AutoCAD versions not
                             rechecked; F4 planning intent unchanged
OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION = RECORDED BY THE E1
                             PREPARATION (2026-10-06) — E2-OWNED CROSS-PHASE
                             CONSTRAINT (author decision 2026-10-07) — NOT SOLVED
                             IN E1 (Commands.lookupInternal is case-insensitive:
                             native DirectDimension / AxisDimension would make the
                             user library reject the legacy macros directDimension /
                             axisDimension)
                             RE-CHARACTERIZED BY THE E2 PREPARATION (2026-10-07):
                             document macros resolve before native commands,
                             case-insensitively, in USER, SCRIPT and XML; a stored
                             legacy package makes the whole user library unreadable;
                             disposition pending DQ-E2-7 — NOT FIXED
                             NAMING SIDE RESOLVED BY DQ-E2-7 (author decision 2026-10-07:
                             native heads AlignedDimension / LinearDimension; legacy
                             spellings never registered); coexistence to be validated
                             in E2; the whole-library failure mode on a future native
                             collision stays pre-existing
                             COEXISTENCE VALIDATED IN THE E2 TECHNICAL CANDIDATE
                             (2026-10-08: Templatev7.ggb and a user store with the
                             legacy macros keep working next to the new commands, no
                             byte change) — PENDING AUTHOR REVIEW
OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS = A-INTENTIONAL-PROFILE-BEHAVIOR
                             (author decision 2026-10-08: the Templatev7.ggb macros
                             are loaded, registered, executable and preserved, the
                             same on dc63b0e5 and 878ff66b; the profile-compiled
                             toolbar does not rebuild document tools; not an E2
                             regression; user guide clarified) — CLASSIFICATION ACCEPTED BY THE AUTHOR
OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX = OPEN (2026-10-08: embedded macros
                             cannot be chosen interactively as tools; an interactive
                             document-tool entry needs a separate author
                             authorization); accepted as OBS-E2-4 at the E2 closeout
                             (2026-10-08) — POST-E2 P3
                             ADDRESSED BY CANDIDATE — POST-E2-P3 technical candidate
                             (2026-10-09, pending author review): User tools →
                             Document tools activates the document macros' existing
                             tool modes
                             RESOLVED — POST-E2-P3 PASS — AUTHOR APPROVED —
                             PUBLISHED (2026-10-09, author smoke PASS)
OBS-E2-1                   = ACCEPTED NON-BLOCKING (E2 closeout 2026-10-08):
                             extension-line gap and overshoot look excessive with
                             constructionUnit = mm only; E2 captures Gap/Overshoot
                             once in model space (semantics unchanged) — POST-E2 P1
OBS-E2-2                   = ACCEPTED NON-BLOCKING — DOCUMENTED (E2 closeout
                             2026-10-08): typed dimensions with a literal or
                             computed offset are not dragged; command signatures
                             and Offset semantics unchanged — POST-E2 P4
                             POST-E2-P4 readiness/design gate authorized (2026-10-09,
                             author-selected Option A: explicit "Make offset
                             draggable" conversion; Option B not authorized)
                             ADDRESSED BY CANDIDATE — POST-E2-P4 technical candidate
                             (2026-10-09, pending author review): the literal offset
                             is labelled offsetN in place and becomes draggable
                             RESOLVED — POST-E2-P4 PASS — AUTHOR APPROVED —
                             PUBLISHED (2026-10-09, conditional approval of the
                             successor 06835fdf; Algebra View and Graphics View menus)
OBS-E2-3                   = ACCEPTED NON-BLOCKING — POTENTIALLY PRE-EXISTING (E2
                             closeout 2026-10-08): large unexpected zoom-out when
                             starting algebra input, also seen in earlier phases,
                             not attributed to E2 — POST-E2 P2
                             CHARACTERIZED — POST-E2-P2 technical characterization
                             candidate (2026-10-09, pending author review): a
                             user-assigned printable factor-zoom chord (author file:
                             Shift+A out, Shift+Z in) fires as a window menu
                             accelerator from the focused input field; GeoCeDG-
                             specific since POST-G9U1-A7, configuration-dependent;
                             correction NOT AUTHORIZED
                             CHARACTERIZATION PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-09, P2 closeout record); correction owner
                             POST-E2-P2-R1, Option A (authorized; approval pending)
                             CORRECTED — POST-E2-P2-R1 PASS — AUTHOR APPROVED —
                             PUBLISHED (2026-10-09, conditional approval of the D1
                             successor 38647c79): text-producing factor chords
                             refused, stored ones unassigned
OBS-E2-1 disposition       = POST-E2-P1 PASS — AUTHOR APPROVED (2026-10-08):
                             P1-INTENDED-PHYSICAL-CAPTURE + P1-DEFAULT-UX-LIMITATION;
                             guide clarified (Option A); Option B retained, not
                             authorized
OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM = REGISTERED (2026-10-08):
                             PDF stroke width appears to follow the view zoom at
                             export while geometry stays correct; root cause and
                             population not established — characterization owner
                             POST-E2-P1-R1 (correction NOT AUTHORIZED)
                             CHARACTERIZED — POST-E2-P1-R1 PASS — AUTHOR APPROVED
                             (2026-10-08): real variation, upstream-inherited,
                             physical-output contract gap; correction owner
                             POST-E2-P1-R2 (authorized; approval pending)
                             CORRECTED — POST-E2-P1-R2 PASS — AUTHOR APPROVED —
                             PUBLISHED (2026-10-09): 0.4 t pt strokes, s pt markers
OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT = ENHANCEMENT — ACCEPTED
                             NON-BLOCKING — IMPLEMENTATION NOT AUTHORIZED (2026-10-09):
                             numeric thickness entry also in the compact style-bar
                             selector; keep the slider, synchronized field, same
                             property and range, undo/redo and serialization kept, no
                             independent mechanism; intended owner
                             PRE-G9B-R6-plus-F4 (planning intent, 2026-10-09)
OBS-POST-E2-P1-R1-PDF-TEXT-SIZE = TD-PDF-1 — OPEN — CHARACTERIZATION REQUIRED —
                             NOT AUTHORIZED (planned after P2–P4; intended owner
                             PRE-G9B-R6-plus-F4, planning intent 2026-10-09)
OBS-POST-E2-P1-R1-PDF-EMBEDDED-FONTS-FAILS = TD-PDF-2 — OPEN — UNCONFIRMED IN
                             PRODUCT — HEADLESS TEST ENVIRONMENT OBSERVATION — NOT
                             AUTHORIZED (planned after P2–P4; intended owner
                             PRE-G9B-R6-plus-F4, planning intent 2026-10-09)
OBS-R6PLUS-PACKAGED-CLASSIC-DIAGNOSTIC-LAUNCHER = OPEN — PRE-EXISTING —
                             CHARACTERIZATION AUTHORIZED NEXT (registered at the E1-P
                             closeout, 2026-10-07: reproduced by the author in a
                             mid-September GeoCeDG build; not caused by E1-P or E1-L;
                             not E1-P blocking; not resolved. Packaged "Open Classic
                             diagnostic session" fails at child launch, CreateProcess
                             error=2, attempted <packaged java.home>\bin\java;
                             GeoCeDGActionRegistry.openDiagnostic derives the child
                             launcher from java.home/bin/javaw.exe, fallback
                             java.home/bin/java. Only a focal characterization after the
                             E1 publication is authorized; no correction)
                             CHARACTERIZED — UNRESOLVED (2026-10-07; PRE-G9B-R6-plus-E1-X1:
                             jpackage runtime without java(w).exe by construction;
                             both routes fail in app-image, ZIP and NC-metadata image;
                             same-seam pre-existing findings X1-F2 undrained child
                             pipes and X1-F3 dropped JVM options) — IMPLEMENTATION NOT
                             AUTHORIZED — PENDING AUTHOR REVIEW
                             CHARACTERIZED — IMPLEMENTATION PREPARED (2026-10-07;
                             PRE-G9B-R6-plus-E1-X1-R1: Candidate C preferred, no
                             packaging change) — IMPLEMENTATION NOT AUTHORIZED
                             IMPLEMENTATION AUTHORIZED 2026-10-07 — TECHNICALLY RESOLVED
                             IN CANDIDATE / PENDING AUTHOR APPROVAL — AUTHOR_SMOKE =
                             PENDING; the observation stays open until the author
                             disposition after smoke
                             RESOLVED IN PRE-G9B-R6-plus-E1-X1 — AUTHOR APPROVED
                             (2026-10-07; not caused by E1; X1-F2 and X1-F3 RESOLVED IN
                             E1-X1; E1-X1 closeout record)
OBS-R6PLUS-E1L-STALE-NO-PACKAGING-ASSERTION = RECORDED BY E1-X1 — PRE-EXISTING SINCE
                             E1-P dcda4075 — NOT FIXED — AUTHOR DISPOSITION
                             (PreG9BR6PlusE1LCuratedLibraryTest.e1l09 still asserts that
                             build-windows-package.ps1 does not contain ggt-library, which
                             E1-P added; it fails in final.desktop on the published base,
                             outside every PHASE selection; the official inventory updater
                             therefore cannot refresh final.desktop)
                             OPEN — PRE-EXISTING AT X1 BASE — INTRODUCED BY THE LEGITIMATE
                             E1-P PACKAGING EVOLUTION — NOT CAUSED BY E1-X1 — NOT BLOCKING
                             E1-X1 — MUST BE RESOLVED BEFORE E2 (author disposition at the
                             E1-X1 closeout, 2026-10-07; final.desktop KNOWN RED AT X1
                             BASE; next authorized activity PRE-G9B-R6-plus-E1-P-X1,
                             implementation not yet authorized)
                             TECHNICALLY RESOLVED / PENDING AUTHOR APPROVAL (2026-10-07;
                             PRE-G9B-R6-plus-E1-P-X1 test-only candidate, PRODUCT CHANGE =
                             NONE, final.desktop CLEAN; candidate report)
                             RESOLVED IN PRE-G9B-R6-plus-E1-P-X1 — AUTHOR APPROVED
                             (2026-10-07; E1-P-X1 closeout record)
PRE-G9B-R6-plus-E1-P-X1    = PASS — AUTHOR APPROVED — PUBLISHED
                             (2026-10-07; T_R6PLUS_E1_P_X1 44b90156; AUTHOR_REVIEW = PASS;
                             AUTHOR_SMOKE NOT REQUIRED; PHASE verification-1a8e1e04;
                             BOUNDED_PHASE; PRODUCT CHANGE = NONE; final.desktop CLEAN)
                             earlier: NEXT AUTHORIZED ACTIVITY — IMPLEMENTATION NOT AUTHORIZED
                             (reconcile the stale E1-L packaging-negation assertion and
                             restore final.desktop to a clean baseline)
                             IMPLEMENTATION AUTHORIZED 2026-10-07 (test-only) — TECHNICAL
                             / VERIFICATION CANDIDATE PENDING AUTHOR REVIEW
ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA = AUTHOR APPROVED REQUIREMENT (2026-10-07) —
                             OWNER PRE-G9B-R6-plus-G — MANDATORY G INPUT —
                             IMPLEMENTATION NOT AUTHORIZED IN E1 (product-owned
                             About: name, canonical version, version date (not the
                             build date), licensing route distinguishing EUPL-1.2
                             software, CC BY 4.0 documentation and ordinary art,
                             inherited component terms and the INTERNAL / NC /
                             COMMERCIAL distribution condition; Classic About
                             unchanged; visible version 1.0.0 until G = PASS —
                             AUTHOR APPROVED, then 1.1.0 with its version date; no
                             tag, release or publication implied)
A-2 3D draw-order limitation = KNOWN PRESENTATION LIMITATION — NOT AUTHORITATIVE
                             LAYER SEMANTICS — NOT A-2 BLOCKING
F3 Apply Template re-layering = OPEN F3 CHARACTERIZATION QUESTION —
                             F3 IMPLEMENTATION NOT AUTHORIZED
G11 / G12 / G15 ownership  = UNCHANGED
selfApproved               = false
```

The author approval concerns the mini-track plan only, as recorded in the
[planning closeout record](../validation/pre_g9b_r6_plus_planning_closeout_record.md).
It does not authorize `P0`, `D0` or any other subphase, each of which needs its
own explicit author instruction naming an exact base, and it is not the
`PRE-G9B-R6-plus` track closeout that `PRE-G9B-R7` depends on. There is no
automatic transition from `PRE-G9B-R7` to `G9B`.

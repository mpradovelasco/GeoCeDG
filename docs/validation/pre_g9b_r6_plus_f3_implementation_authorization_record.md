# PRE-G9B-R6-plus-F3 implementation authorization record

```text
RECORD_KIND              = AUTHOR AUTHORIZATION, ENTRY GATE AND FROZEN VERIFICATION CONTRACT
TASK                     = PRE-G9B-R6-plus-F3-IMPLEMENTATION-AUTHORIZATION
AUTHORIZATION_DATE       = 2026-10-13
APPROVED_BASE            = P_F3_DESIGN_CLOSEOUT 108d9cb6591aef92135a95ec9bf71482fcd78e7f
                           tree 45644a6fabdb5e3b4cb23df063761f57d3fdf133
                           parent 9d130b43fbeb731a25d5dcf6413ed0dd0439f61e
                           STATIC verification-029706cfc5124d31a3d6c0d130afd919
                           ACCEPTED / COMPLETE
LAST_PUBLISHED_MAIN      = e70718fec3612814144cd405d46f6c08864ceadd
BRANCH                   = phase/pre-g9b-r6-plus-f3-implementation (local, from 108d9cb6)

F3_DESIGN                = AUTHOR APPROVED
F3_IMPLEMENTATION        = AUTHOR AUTHORIZED — SUBJECT TO ENTRY GATES (entry gate PASS, §2)
F3_VERIFICATION_CLASS    = INTEGRATED_PHASE — FROZEN (PHASE + INTEGRATION, §3)
F3_TECHNICAL_CANDIDATE   = NOT YET ESTABLISHED
F3_AUTHOR_SMOKE          = PENDING
F3_PRODUCT_CLOSEOUT      = NOT AUTHORIZED
F3_PUBLICATION           = NOT AUTHORIZED
selfApproved             = false
passClaimed              = false
```

This record transcribes the author instruction
`PRE-G9B-R6-plus-F3-IMPLEMENTATION-AUTHORIZATION` of 2026-10-13 and records the
entry gate and the verification-contract gate it requires before product code.
It is the first tracked change of the implementation. It changes no decision of
the [F3 author design closeout record](pre_g9b_r6_plus_f3_author_design_closeout_record.md),
which stays the authority for the approved design; the canonical prompt
[`pre-g9b-r6-plus-f3-file-insert.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-f3-file-insert.prompt.md)
is not amended (the instruction does not authorize an amendment); its execution
contract applies as authorized here. Machine-readable mirror:
[`pre-g9b-r6-plus-f3-implementation-authorization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-implementation-authorization.json).

## 1. Author authorization (essentials)

The author authorizes implementation of `PRE-G9B-R6-plus-F3` on the exact base
above, covering "the minimum GeoCeDG Desktop production changes, associated
focused tests, integration work, verification-registry entries, documentation
and validation evidence necessary to implement the already approved F3
contract". It does not authorize additional functionality, changes to approved
design decisions, shared or kernel modifications, inherited GeoGebra changes,
general spatial import or unrelated roadmap work; nor push, merge, tag, release
or binary publication; nor self-approval, author smoke by the agent, product
closeout, `GLOBAL_IMPACT`, K2, C2, C3 or an alternative implementation class.
FR-F3-1 to FR-F3-8 are binding stop conditions with the STOP protocol of the
instruction (§5). `FINAL` is not required.

## 2. Entry gate (2026-10-13)

| # | Check | Result |
|---|---|---|
| 1 | HEAD, tree, parent | `108d9cb6…`, `45644a6f…`, `9d130b43…` |
| 2 | linear ancestry from published `main` | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` → `3e24ed3a` → `6c07e01a` → `6e41bf44` → `e479b76d` → `9d130b43` → `108d9cb6` |
| 3 | worktree | clean |
| 4 | `main`, `origin/main`, live remote `main` | `e70718fe` (base intentionally local and ahead) |
| 5 | STATIC receipt | `verification-029706cf…` `ACCEPTED / COMPLETE`, `COMPLETED`, bound to `108d9cb6`/`45644a6f` (standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE` only) |
| 6 | product delta in the documentary chain | none (`git diff e70718fe..108d9cb6 -- source apps tools geocedg/specs` empty) |
| 7 | frozen candidates and evidence | unchanged (documentary commits only; no rewrite) |
| 8 | design closeout record and JSON mirror | present |
| 9 | Q1–Q7; `DQ-F3-1`–`DQ-F3-24` | each disposed exactly once (closeout record §4–§5) |
| 10 | `P1`–`P7` | approved in their exact scope (closeout record §6) |
| 11 | canonical prompt | present and consistent with this authorization (design approved; execution contract, gates 1–9, FR-F3-1..8) |
| 12 | FR-F3-1 to FR-F3-8 | present and binding |
| 13 | verification, governance, formatting, provenance | `AGENTS.md`, canonical governance and verification prompts, `verification-levels.md` §12.8 read from disk |
| — | result | **PASS** |

## 3. Verification-contract gate and frozen class

**Inspected:** `verification-registry.json` (schema version, `phase_selections`,
`nodes`, profile coverage), `verification-junit-inventory.json`, the F2-R1 and
E3 phase precedents (phase entry; `PROCESS_PRODUCER` running
`tools/agent/checks/gradle-test-evidence-producer.ps1 -SelectionId
<id>.desktop`; `EVIDENCE_PROJECTION` `JUNIT_SELECTION`, `SEMANTIC` /
`PRODUCT`), `tools/agent/tests/verification-final-coverage.Tests.ps1`
(selection, per-selection identity and phase-selection count pins),
`tools/agent/workspace-profile-validation.ps1` (approved action amendments) and
its static-contract pin, and the official inventory updater.

```text
VERIFICATION_CLASS        = INTEGRATED_PHASE — FROZEN (author-selected; verification-levels §12.8)
ACCEPTANCE                = tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F3
                              -LogDirectory artifacts\agent\<fresh>
                            tools\agent\verify.ps1 -Profile INTEGRATION
                              -LogDirectory artifacts\agent\<fresh>
                            both once, on the same frozen clean candidate commit and tree
PHASE_ID                  = PRE-G9B-R6-PLUS-F3 (registry convention of PRE-G9B-R6-PLUS-F2-R1)
REQUIRED_CHECK_IDS        = compile.desktop.semantic
                            workspace-profile.product
                            junit.desktop.pre-g9b-r6-plus-f3.semantic
                              (producer junit.desktop.pre-g9b-r6-plus-f3.producer,
                               selection pre-g9b-r6-plus-f3.desktop)
INTEGRATION_COVERAGE      = the registered INTEGRATION profile (COMPOSED alias), which
                            includes junit.desktop.final, junit.shared.final,
                            compile, packaging and checkstyle nodes; it is required
                            because the frozen plan names concrete integration
                            coverage (restricted design §4; canonical prompt
                            "Integration obligations")
ALSO RUN                  = development STATIC and INFRA_UNIT (registry-shape pins);
                            git diff --check
NOT RUN                   = FINAL (a run above the frozen class fails with
                            VERIFICATION_ESCALATION_REQUEST)
```

**Existing suites retained in the phase selection** (verified to exist at the
base; integration coverage named by the audit reconciliation report §9 and the
surface/profile pins): `GeoCeDGDocumentLifecycleTest`,
`G9U1NativeLifecycleReviewTest`, `PreG9BR6PlusA2NewDocumentBaselineTest`,
`PreG9BR6PlusD1UnitLoadTest`, `G9S1NativeArchivePersistenceTest`,
`G9S1R1NativeArchivePersistenceTest`, `G9U0R4NativeArchivePersistenceTest`,
`G9U0R5NativeArchivePersistenceTest`, `G9U0R6NativeArchivePersistenceTest`,
`G9U1MacroNativeArchivePersistenceTest`, `G9U1UserToolLibraryTest`,
`PreG9BR6PlusE1LCuratedLibraryTest`, `PreG9BR6PlusA1HiddenLayerTest`,
`PreG9BR6PlusA2HiddenLayerPersistenceTest`, `PreG9BR6PlusA1LayerWorkspaceTest`,
`locus.LocusV2DesktopLifecycleRegressionTest`,
`PreG9bR2E1SemanticPairNativeArchiveTest`,
`PreG9bR2IntersectionEndpointNativeArchiveTest`,
`PreG9BR6PlusCBaseIdentityTest`, `PreG9bR3RedefineFrontendTest`,
`PreG9BR4SplineV2AuthoringTest`, `PreG9BR6PlusF2AuthoringMemoryTest`,
`PreG9BR6PlusF2R1DualActivationTest`, `PreG9BR6PlusE3IsoABorderDesktopTest`,
`PreG9bR3C1DesktopClipboardTest`, `PreG9bR3C1U1DesktopClipboardTest`,
`PreG9BR6PlusD1UnitClipboardTest`, `PreG9BR6PlusD1PasteNoticeVisibilityTest`,
`PreG9BR6ScriptSurfaceBoundaryTest`, `G9U1ScriptWorkflowTest`,
`G9U1ActionRegistryTest`, `G9U1WorkspaceSurfaceTest`, `GeoCeDGProfileTest`,
`PreG9BR5AGuideOutlineTest`, `PostP1BilingualUserGuideTest` (all in package
`org.geocedg.desktop`).

**Focused tests the implementation must create** (none exists; names fixed when
created, never claimed before): Stage A — accounting C1 exit by exit, stale and
concurrent stores, generation barrier, attach quiescence and closing barrier,
history labels and grouping, Redo preservation, snapshot ownership, native
recovery snapshot completeness, UUID, recovery fault injection (K1-S, K1-R-H,
serious), EDT, factory lifecycle, isolation routes and exits; Stage B —
admission and rejection classes, dry run, strict verification, L1, the `P4`
matrix rows (including Locus V2, Spline V2, Linear and Aligned dimensions,
IsoABorder), one undo step, UUID, save/reopen; Stage C — M1/MU-1, the
independent buffer and `D1` provenance, T0; surface — menu, localization, pins,
Open route and OFF, Classic, fallback, helper, guides.

**Registration:** the selection `pre-g9b-r6-plus-f3.desktop` and the discovery
records through `tools/agent/update-verification-junit-inventory.ps1` from
official producer evidence (`-TestDryRun` discovery; executed selection
evidence for `pre-g9b-r6-plus-f3.desktop`, `final.desktop` and `final.shared`);
the registry phase entry, producer and projection nodes and the
`junit_inventory` catalog pin reproduced with `Get-VerificationCanonicalTextSha256`;
the coverage-test count pins; the workspace-profile validator amendment for
`document.insert-file` and `document.apply-styles` with its static-contract
re-pin. The resolved PHASE plan is validated before the frozen run.

**Criteria for the final executable selection:** every existing suite above;
every focused test created for the obligations above; no planned test counted
before it exists and passes; the coverage obligations of this section are not
weakened or reclassified. If the selection cannot be registered, validated or
made complete without violating governance: `STOP — VERIFICATION CONTRACT NOT
ESTABLISHED`.

## 4. Scope boundary and stop conditions

The class stays valid only while the implementation is GeoCeDG Desktop-owned
(U0): `source/shared/**`, inherited GeoGebra Desktop classes and Classic are
not changed. Any demonstrated need for a shared, kernel or inherited change, a
broader impact, K2, C2 or C3 stops the work for the author (FR-F3-8, FR-F3-1).
FR-F3-1 to FR-F3-8 are recorded verbatim in the canonical prompt and the
closeout record §7.

## 5. Status

`DEBT-R6PLUS-FILE-INSERT-SURFACE` stays open (C20-1). No technical candidate,
verification result or author smoke exists at the time of this record.
`selfApproved = false`.

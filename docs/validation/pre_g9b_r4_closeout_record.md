# PRE-G9B-R4 closeout record

```text
PRE-G9B-R4 = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-09-30 on the
`PRE-G9B-R4` corrective candidate and the exact evidence identities behind it.
It changes no product, test, build, verifier, inventory, registry, schema,
localization, specification or acceptance-semantics file. It does not modify
the approved technical candidate. Besides this record and its machine-readable
mirror, it only updates the roadmap status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [initial candidate report](pre_g9b_r4_candidate_report.md), the
[corrective candidate report](pre_g9b_r4_corrective_candidate_report.md) and
their evidence mirrors record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. This record is the sole
authority for current author approval. Every other statement in those artifacts
stands unchanged.

## Author disposition

```text
PRE-G9B-R4                 = PASS — AUTHOR APPROVED

FINAL TECHNICAL CANDIDATE  = 13d1192b573656ab032df5d54f4372c3f32a2849   (T_R4C)
FINAL TREE                 = 3bda0491c9990c418d232a6ac963597ac2b1ad26
AUTHOR SMOKE               = PASS
AUTHOR APPROVAL            = APPROVED
selfApproved               = false
```

The approval applies to exact `T_R4C` and extends to no other descendant except
this status-only closeout. It does not retroactively make the initial candidate
the approved technical candidate:

```text
INITIAL T_R4        = 558c15c9537fdde8c1915fd34e810d7ade969f93
INITIAL T_R4 STATE  = TECHNICALLY ACCEPTED
                      SUPERSEDED BY AUTHOR-SMOKE CORRECTIVE DESCENDANT
                      NOT THE FINAL AUTHOR-APPROVED CANDIDATE
```

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base `P_GSTATE` | `62b1281b5006272d4f9b1a0ae9d413e1f7b94320` | `8ce2f6719f1094e300beeaafbadab4cd5135a8d5` | `d8a63d49…` |
| initial candidate `T_R4` (superseded) | `558c15c9537fdde8c1915fd34e810d7ade969f93` | `43f6946113beb8436d73321071958686cbc39323` | `P_GSTATE` |
| final candidate `T_R4C` (approved) | `13d1192b573656ab032df5d54f4372c3f32a2849` | `3bda0491c9990c418d232a6ac963597ac2b1ad26` | `T_R4` |

Branch: `feature/pre-g9b-r4-spline-v2-list-authoring-ux`. The history is linear and
single-parent, with no merge. The canonical prompt
`.github/prompts/tasks/pre-g9b-r4-spline-v2-list-authoring-ux.prompt.md` (blob
`51abda02734bb7884c8b811b26ba10a14a56119f`) is unchanged.

## Accepted evidence

The class was frozen at phase start and never escalated:

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = BOUNDED_PHASE
frozenAtPhaseStart  = true
ACCEPTANCE LEVEL    = PHASE PRE-G9B-R4
```

Under `geocedg/specs/operations/verification-levels.md` §12.8, `BOUNDED_PHASE`
requires PHASE only. Both runs used the same plan (hash
`f6ae0452b1a6dcb3cba29668aca1669ce5de784a262f102b2b873747d90adc44`, 9 nodes, 5
required acceptance checks) and are preserved as executed:

| Candidate | Run | Result hash | Result file SHA-256 | Verdict | Author disposition |
|---|---|---|---|---|---|
| `T_R4` `558c15c9…` | `verification-133e6b111d9d48a29ced957f81069e4c` | `6136bc520f2c03bb8369c460f1db197ffc92a0e50dbd4e86183ef247f822a933` | `71a753572443d0af42abcde32cf5f9ce747dcd2686153ef5883b3d9f1a0bc559` | `ACCEPTED / COMPLETE`, 5/5 | smoke `CORRECTIVE FINDINGS`; approval not granted to this candidate |
| `T_R4C` `13d1192b…` | `verification-ffc495c4fcbf467082bbaf75d8e54a06` | `6027887e5a9c458682fa9b3aa61aad5e8edac15ac05564bc58a4e3c0c0930ebd` | `d9d2fbefc3d432d41c941470dad715ffdee5488cbece9106a72279dbe7e20d23` | `ACCEPTED / COMPLETE`, 5/5 | smoke `PASS`; approved |

The corrective run has 5 acceptance checks satisfied, 0 violated, 0 untrusted, 0
not run and 0 diagnostics. It covers the Desktop selection `pre-g9b-r4.desktop`
(158/158 passed) and the shared selection `pre-g9b-r4.shared` (5/5 passed). Each
run is bound to its own candidate commit and tree; no evidence crosses between
them.

PHASE emits no FINAL acceptance receipt. So, as in the status-only closeouts of
accepted bounded phases (`PRE-G9B-R1-R1`, `PRE-G9B-R2`), no receipt is claimed,
fabricated or fed to the FINAL-receipt identity-only closeout adapter. The run
evidence stays locally in the ignored evidence area (`artifacts/agent/pre-g9b-r4-phase-558c15c9/`
and `artifacts/agent/pre-g9b-r4c-phase-13d1192b/`). No PHASE, FULL or `GOV-R` run is
made for this closeout; only its static checks run.

## Author smoke

```text
AUTHOR_SMOKE = PASS
```

The accepted author-smoke behaviour:

- the dedicated **List tools** group is second from the left, immediately right
  of Move;
- **Create List from Selection** is present there and no longer belongs to
  Polygons;
- all three Spline V2 variants visibly reflect their active toolbar/flyout state;
- activating a Spline V2 tool does not open Input Help automatically;
- explicit Input Help and contextual-help requests select and display `SplineV2`
  correctly;
- default-degree, explicit-degree and Closed Spline V2 authoring behave
  correctly;
- cancel and undo/redo behave correctly;
- ordered list creation behaves correctly.

The pre-existing containing-list hit selection below is a retained observation,
not an R4 smoke failure.

## Accepted capability

`PRE-G9B-R4` closes its planned scope:

- Spline V2 authoring tools and the ordered list authoring tool;
- mode semantics and toolbar/flyout integration;
- the explicit-degree gesture and the explicit closed-form gesture (`AD-R0-8`);
- contextual help and Input Help integration;
- localization and the tooltip correction (`TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`,
  resolved);
- the bilingual user-guide update.

The final UX decisions, as recorded in
[the specification](../../geocedg/specs/ui/spline-v2-and-ordered-list-authoring.md):

```text
Input Help visibility is user-controlled.
Mode activation alone never opens Input Help.
Toolbar selected state follows host mode authority.
List authoring owns a dedicated toolbar presentation group.
```

No spline mathematics moved into the frontend. No `CmdSplineV2` semantic change,
and no serialization or durable-identity contract change, was made.

## Observation disposition

None of the five observations below blocks `PRE-G9B-R4`. Each is carried forward
unchanged; none is repaired here.

| Identifier | Disposition |
|---|---|
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION` | retained observation, not an R4 defect: the current durable-participation machinery may promote and name SplineV2 argument objects; R4 does not alter that kernel/identity behaviour |
| `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` | retained observation, pre-existing kernel behaviour: the frontend never infers closure from proximity or coordinates, and only the explicit Closed Spline V2 action synthesizes the repeated endpoint; no kernel semantic change is authorized |
| `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY` | retained usability observation; no action required |
| `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | pre-existing, retained outside R4: identical on the published base, on the initial candidate and on the list-tool path. Ordinary objects and lists sit on a higher host layer, the Locus V2-family curve on layer 0, and host hit filtering keeps only top-layer hits |
| `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION` | pre-existing, retained outside R4: a mode without a toolbar button leaves the previous group shown; may be considered in a later frontend/workspace maintenance pass |

**Fixture provenance.** The finding-D diagnosis used the author's local fixture
`artifacts/preg-g9b-r4/TestNewTools.cedg`, SHA-256
`fbff3f70c7a9cf5f11baa3d5e39f763b9274b1278c1f2efa1aa15a345f3a2dad`, an ignored,
untracked file. It was read only from a temporary copy; it is neither tracked
nor modified by this closeout.

## Retained debt and observations from earlier phases

The latest ledger (the
[generated-state maintenance closeout](maintenance_final_generated_state_historical_retirement_closeout_record.md))
carries forward unchanged. R4 absorbs none of it:

| Identifier | Owner / state |
|---|---|
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | observation, retained |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observation, retained |

## Promotion

The author authorizes promotion of the linear history
`62b1281b… → 558c15c9… → 13d1192b… → this closeout commit` to `main`, by
ordinary clean non-force fast-forward only. There is no merge, rebase, squash,
amend, force push, feature-branch push, tag, release or binary publication.
Promotion proceeds only if local `main`, `origin/main` and the live remote
`main` still equal the base below immediately before it.

Remote identity at closeout entry:

```text
local main       = 62b1281b5006272d4f9b1a0ae9d413e1f7b94320
origin/main      = 62b1281b5006272d4f9b1a0ae9d413e1f7b94320
live remote main = 62b1281b5006272d4f9b1a0ae9d413e1f7b94320
tree             = 8ce2f6719f1094e300beeaafbadab4cd5135a8d5
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
PRE-G9B-R5-A = DESIGNED — NOT AUTHORIZED   (índice, esquema y navegación de la guía bilingüe)
PRE-G9B-R5-B = DESIGNED — NOT AUTHORIZED   (superficie canónica de comandos en inglés;
                                            separate phase, requires its own compatibility
                                            ADR / AD-R0-4 execution authority)
PRE-G9B-R6   = NOT AUTHORIZED
PRE-G9B-R7   = NOT AUTHORIZED
G9B          = NOT AUTHORIZED
```

`R5-A` is the next planned gate and requires a separate explicit author
authorization from the exact published closeout commit. `R5-A` and `R5-B` are
not collapsed. `G9C`, `G9U2`, productive `G10` and further `G12` remain
unauthorized. No tag or release is authorized.

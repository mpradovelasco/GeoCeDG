# PRE-G9B-R6 closeout record

```text
PRE-G9B-R6 = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyIntegration       = NONE
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-10-01 on the
`PRE-G9B-R6` GGBScript compatibility gate and the exact evidence identities
behind it. It changes no product, test, build, verifier, inventory, registry,
schema, matrix, localization, guide, specification, ADR or acceptance-semantics
file, and it does not modify the approved technical candidate. Besides this
record and its machine-readable mirror,
[`pre-g9b-r6-closeout.json`](../../geocedg/validation/pre-g9b-r6/pre-g9b-r6-closeout.json),
it only updates the roadmap status statements and adds the author-authorized
`PRE-G9B-R6-plus` dependency to the proposed future `PRE-G9B-R7` prompt.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [candidate report](pre_g9b_r6_candidate_report.md) and its evidence mirror
[`pre-g9b-r6-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6/pre-g9b-r6-candidate-evidence.json)
record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`. Their documentary
reconciliation in `D_R6_DOC` (below) keeps that rule. This record is the sole
authority for current author approval. Every other statement in those artifacts
stands unchanged.

## Author disposition

```text
PRE-G9B-R6                    = PASS — AUTHOR APPROVED

APPROVED TECHNICAL CANDIDATE  = 4333d032f2e3b309ef9a22913686609c951b6f52   (T_R6)
TREE                          = a781559e72ce4b56f6d6b3b871689fcf4e893c78
PARENT                        = e7401734b4ef76d8f79dd4b0535845304937e4f7   (P_R5B)
AUTHOR SMOKE                  = PASS
AUTHOR APPROVAL               = APPROVED
selfApproved                  = false
```

The approval applies to exact `T_R6`. The documentary descendant is not a new
technical candidate and is not relabelled as one:

```text
D_R6_DOC = DOCUMENTARY RECONCILIATION OF T_R6 EVIDENCE
TECHNICAL SOURCE COHORT = T_R6
PRODUCT DELTA FROM T_R6 = NONE
```

The approval extends to no other descendant except `D_R6_DOC` and this
status-only closeout.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published implementation base `P_R5B` | `e7401734b4ef76d8f79dd4b0535845304937e4f7` | `3ad888b0895d0cb026be60e3d435c0819c4faed8` | `a1d96746…` |
| technical candidate `T_R6` (approved) | `4333d032f2e3b309ef9a22913686609c951b6f52` | `a781559e72ce4b56f6d6b3b871689fcf4e893c78` | `P_R5B` |
| documentary reconciliation `D_R6_DOC` | `dd486b594a86b4853575e61e872d65325d05647e` | `099c8cbdba19a3153b554407804086eb2c872d6e` | `T_R6` |
| closeout `P_R6` | this record's own commit | reported with the promotion | `D_R6_DOC` |

- **Branch:** `phase/pre-g9b-r6-ggbscript-compatibility`, local and never pushed.
- **History:** linear and single-parent, with no merge.
- **Canonical prompt:**
  `.github/prompts/tasks/pre-g9b-r6-ggbscript-compatibility.prompt.md`, amended
  by `T_R6` as authorized (blob `e1da5f3b4f0dcc9139b19788fdb1dd4f03138de6` →
  `01eb1ed8259ebee2fefe4c03f5fa84da98cf3e82`), unchanged by `D_R6_DOC` and by
  this closeout.

## Documentary reconciliation `D_R6_DOC`

`D_R6_DOC` is the direct child of `T_R6`. Its scope:

| Path | Change |
|---|---|
| `docs/validation/pre_g9b_r6_candidate_report.md` | §16 item 1 states the real `textSubmitted()` route; §23 step 2 is the corrected smoke; a reconciliation note; §24 marked as the state when `T_R6` was frozen |
| `geocedg/validation/pre-g9b-r6/pre-g9b-r6-candidate-evidence.json` | `correction.reproductions.strategyLeak` states the real route |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6ScriptStrategyRestoreTest.java` | one comment line (64); same line count |

- **No change to:**
  - product, test behaviour, the matrix, its schema, serialization, command or
    GGBScript semantics;
  - verification semantics;
  - the JUnit inventory or any registry pin.
- **Comment-only test edit, bytecode identity.** The SHA-256 of the compiled
  classes is identical before and after:
  - `PreG9BR6ScriptStrategyRestoreTest.class`:
    `3f633b28bfeb71ef37b72f3b07fab3dacb41f912b6c5dc433131d35a4db134bc`;
  - `PreG9BR6ScriptStrategyRestoreTest$PresentationFailure.class`:
    `35dd54c22bffeabde6ef03f59606329fd0179572dd076603b1ce40c318cbe1c1`.
- **Unchanged from `T_R6`:** `geocedg/specs`, `source/shared`, `tools` and
  `ggbscript-capability-matrix.json`.
- **Checks:** `STATIC` `verification-4c6e125d0dd54337990e5c1a1315a39a` on
  `D_R6_DOC` (tree `099c8cbd…`): `ACCEPTED / COMPLETE`, 3/3, plan `25dfacc8…`
  (identical to `T_R6`). Result hash
  `f35cafe58aecec4c3af6108dbd004f79674135bb44a2c7d6eec6086929e1cae0`, result file
  SHA-256 `a7762efaf41eb4e57d5ce636fedf7e68a93dc755719f927acca1601cb70d3358`.
  The static contract `pre-g9b-r6.ggbscript-capability-matrix` passes, and
  `git diff --check` is clean.
- **`authorSmoke` field.** The evidence mirror's `"authorSmoke": "PENDING"`
  stays as the frozen state. This record carries the result.

## Accepted evidence

The class was frozen at phase start and never escalated:

```text
CHANGE_ROUTE                        = ORDINARY
VERIFICATION_CLASS                  = OPERATIONAL_VERIFICATION_INFRASTRUCTURE
frozenAtPhaseStart                  = true
VERIFICATION_INFRASTRUCTURE_IMPACT  = PHASE_LOCAL
ACCEPTANCE                          = INFRA_UNIT + STATIC + PHASE -Phase PRE-G9B-R6
FINAL                               = NOT REQUIRED
```

`FINAL` is not required. Under `verification-levels.md` §12.8 this class needs
`FINAL` only when global verification infrastructure changes. The `FINAL`,
`INTEGRATION`, `INFRA_UNIT` and `STATIC` plans are identical to the base.

The technical acceptance evidence for the approved candidate is its own fresh
campaigns on `4333d032…` / `a781559e…`:

| Profile | Run | Plan | Verdict | Result hash | Result file SHA-256 |
|---|---|---|---|---|---|
| `INFRA_UNIT` | `verification-2968b49a32834743a361e5511b544d1a` | `830887b1…` | `ACCEPTED / COMPLETE`, 22/22 | `6e6068541bc7cf49643c9acd8bfc8e8779e3a6f794be152b149b34b41f7066ae` | `13c58834667883a17aa42caccee82d5a618666800ae17b557259bab1ff297973` |
| `STATIC` | `verification-09701faa95f3447db42ab47c657b1295` | `25dfacc8…` | `ACCEPTED / COMPLETE`, 3/3 | `be1e7e4004ba6dc2b8429f42112322baaa83ae3139bf91c19fdad13b06e6d3bc` | `3b24dac97285702b3d42c1364c127e86e719f056623ae018d2363f93cf53402f` |
| `PHASE -Phase PRE-G9B-R6` | `verification-0115a60739804474b1c1bba79b132f0c` | `2583976cdd2a47df080a3f8f6834a33c6fd07d0ff4e29dacfa34b0f9143e21ae` | `ACCEPTED / COMPLETE`, 6/6 (4 producers) | `c88174938e0d87c63eea2cce85bdc51c2e78facac6d3cd309fba291f35fb2824` | `ccbc055416767f554971c24d55f387183b5350ac5965e1ed6af876ce7526080c` |

None of these runs had a violated, untrusted or not-run check. The `STATIC`
runs carry the standing `diagnostic.governance` FINDING and
`diagnostic.historical-consistency` UNAVAILABLE.

The [candidate report](pre_g9b_r6_candidate_report.md) §18 records the
pre-freeze regression evidence:
- `pre-g9b-r6.desktop`: 53 tests, 0 failures or errors;
- `pre-g9b-r6.shared`: 11 tests, 0 failures or errors;
- the complete Desktop suite, `final.desktop`: 1 657 tests, 0 failures or
  errors, 1 allowlisted skip;
- the complete shared suite, `final.shared`: 6 850 tests, 0 failures or errors,
  10 allowlisted skips;
- Checkstyle: 0 findings;
- the upstream boundary: every changed `source/` file registered;
- the R6 strategy-restore regression: 6/6 failing before the correction and
  6/6 passing after it.

- **Local evidence:** the run evidence stays locally in the ignored evidence area:
  - `artifacts/agent/pre-g9b-r6-infra-unit-4333d032/`;
  - `artifacts/agent/pre-g9b-r6-static-4333d032/`;
  - `artifacts/agent/pre-g9b-r6-phase-4333d032/`;
  - `artifacts/agent/pre-g9b-r6-doc-static-dd486b59/`.
- **Development runs:** the `pre-g9b-r6-dev-*` runs on the base are development
  evidence, not acceptance, and are not reattributed.
- **No FINAL receipt:** none is claimed, fabricated or fed to the FINAL-receipt
  identity-only closeout adapter. The candidate's identity is recorded directly.
- **No cross-candidate reuse:** the `STATIC` run on `D_R6_DOC` is the
  descendant's own static check; it is not acceptance evidence for `T_R6`, and
  `T_R6`'s runs are not reattributed to `D_R6_DOC`.

## Author smoke

```text
AUTHOR_SMOKE = PASS
```

The author executed both steps of the corrected smoke checklist (candidate
report §23) on the R6 line, with result PASS:

1. Spanish UI, `SplineV2` reflection and length through a button's click
   script, with these checks:
   - English command names in the script editor;
   - creation and update on drag;
   - one undo step and redo;
   - a `.cedg` round trip across a product-language change.
2. Spanish UI, a default symbolic input box for `n` with the click script
   `SetValue(m, %0)`:
   - symbolic Enter updates `n` and leaves `m` unchanged;
   - `RunClickScript(<box>)` runs the script with `%0` = the box text;
   - after `5$`, `RunClickScript(<box>)` fails in the `%0` substitution;
   - a following `Circunferencia((0,0),1)` creates a circle.

**Input Box investigation disposition.** The author-authorized bounded
investigation of the earlier symbolic-box smoke discrepancy concluded:

```text
PRIMARY   = A — SMOKE DESIGN ERROR
SECONDARY = B — TEST / REAL-UI PATH MISMATCH
NOT       = C (product defect), D (verification defect)
```

The Input Box event routes were measured identical at `P_R5B` and `T_R6`:
- The default symbolic Enter runs `SymbolicEditor.onEnter → applyChanges →
  updateLinkedGeo`. It fires only `UPDATE` and never the On Click script.
- `EuclidianController.runScriptsIfNeeded` skips Input Boxes on a mouse click.
- The On Click script runs only through `GeoInputBox.textSubmitted()`. That is
  reached by `RunClickScript(box)`, by the focus loss after an edit of a
  classic non-symbolic box, or directly.

The original smoke step therefore could not exercise the failing path. The
regression test calls `updateLinkedGeo` and `textSubmitted()` directly. Its
comment and the report wording described the route as an Enter, and
`D_R6_DOC` corrects that wording. The corrected route reproduces the leak on
`P_R5B`, where `SCRIPT` stays set and Spanish `Circunferencia` fails, and
`T_R6` restores `USER`. The terminal messages seen during the smoke are not
causal:
- "file has 3D objects";
- "cbAlgebraView not implemented";
- ScreenReader debug output.

## Accepted capability

`PRE-G9B-R6` closes its planned scope through `T_R6`:

- **Capability matrix.** It covers every command GeoCeDG added (`LocusV2`,
  `LocusLength`, `SplineV2`) or modified (`Point`, `Length`, `Intersect`,
  `Translate`, `Rotate`, `Mirror`, `Dilate`).
  - **Rows:** one per `command_id × arity_form × entry_surface × ui_locale`,
    over `ALGEBRA_INPUT`, `GGBSCRIPT` and `GGBSCRIPT_EXECUTE` in `en`/`es`. That
    gives 162 rows: 144 `SUPPORTED` and 18 `UNSUPPORTED_TRUTHFUL`, plus 138
    lookup probes.
  - **Files:** schema
    `geocedg/specs/operations/ggbscript-capability-matrix.schema.json` and
    instance `geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`.
  - **Completeness gate:** it fails closed for a new command, a new form, a
    missing cell, an added-command exclusion or a missing JUnit method.
- **Lookup regimes.**
  - `USER`: canonical English, internal names and case variants, plus Spanish
    aliases under the Spanish UI only.
  - `SCRIPT`: English and internal names, case-insensitive.
  - `XML` (`Execute`): exact-case enum names only.
  - All three are unchanged.
- **Undo and persistence.**
  - Undo: one step per successful Algebra submission and one per changing
    click run, none per line and none for update scripts.
  - Persistence: every supported row round-trips native `.cedg` with
    byte-identical construction XML.
- **Strategy restoration (`TD-R0-GGBSCRIPT-STRATEGY-RESTORE`).**
  - `GgbScript.run` restores the previous command lookup strategy in `finally`,
    after success or any throwable.
  - A throwable escaping a line is a failed line, never a successful script.
  - `CmdExecute` restores its previous strategy in `finally`. This is the
    bounded truthful-failure correction on the `GGBSCRIPT_EXECUTE` surface.
- **Unchanged:**
  - script delocalization, placeholder substitution, line continuation;
  - the `Execute` lookup, error reporting, break-on-error and returned argument
    list;
  - command semantics and serialization.
- **Impact:** `GUIDE_IMPACT = NONE`, `BOOTSTRAP IMPACT = NO CHANGE REQUIRED`,
  `SERIALIZATION_IMPACT = NONE`.

## Observation and debt disposition

None of the items below blocks `PRE-G9B-R6`. No other debt is marked resolved
merely because the phase closes.

| Identifier | Disposition |
|---|---|
| `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` | **RESOLVED — AUTHOR APPROVED** by the approved candidate `T_R6` |
| `OBS-R6-PLACEHOLDER-REGEX-REPLACEMENT` | **RETAINED**; `%0` uses regex replacement, so `$` or `\` in an argument fails or is altered; it now fails truthfully with the strategy restored |
| `OBS-R6-EVENT-DISPATCH-SELF-GEO-ON-ESCAPE` | **RETAINED**; upstream, outside `R6` |
| `OBS-R6-EXECUTE-INNER-FAILURE-NOT-PROPAGATED` | **RETAINED**; inherited `Execute` result contract, recorded in the matrix |
| `OBS-R6-EXECUTE-ALIAS-SYNTAX-KEY` | **RETAINED**; presentation only |
| `OBS-R6-ROTATE-FORM-4-UPSTREAM-REFUSAL` | **RETAINED**; truthful upstream refusal, `NO_GEOCEDG_BRANCH` |
| `OBS-R6-SYMBOLIC-INPUTBOX-CLICK-SCRIPT-NOT-ON-ENTER` | **INHERITED BEHAVIOR / NOT AN R6 DEFECT**; the default symbolic Input Box's Enter, focus loss and click never run its On Click script |

The ledger of the [`PRE-G9B-R5-B` closeout](pre_g9b_r5_b_closeout_record.md)
carries forward unchanged, and through it the earlier ones:

| Identifier | Owner / state |
|---|---|
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | retained |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN`, `OBS-R2-PHASE-SELECTION-COHORT`, `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| the `OBS-R5B-*`, `HZ-R5B-ALIAS-CAS-STATE`, `HZ-R5B-HARNESS-TABLE-FRESHNESS` and `CSolve`/`CSolutions` items | retained, unchanged |
| `OBS-R5A-*`, `OBS-C1-*`, `OBS-U1-*`, `OBS-X1-*`, `OBS-R4-*`, `OBS-R4C-*` | retained |

## Closeout classification

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = DOCUMENTATION_STATUS_ONLY
ACCEPTANCE          = STATIC
```

`geocedg/specs/operations/verification-levels.md` §12.8 requires static
validation only for `DOCUMENTATION_STATUS_ONLY`. This commit changes:
- this record and its mirror;
- the roadmap status statements;
- the author-authorized dependency line of the proposed future `PRE-G9B-R7`
  prompt.

The amended prompt passes `Test-PromptContractDocument` with the `task`
profile: all seven fields are present and ordered, and `execution_safe = true`.
`prompt-execution-safety.ps1` returns `CONTRACT_SATISFIED`. This commit's own
`STATIC` run and `git diff --check` are reported with the promotion, because the
record cannot name its own commit.

## `PRE-G9B-R6-plus` and `PRE-G9B-R7`

```text
PRE-G9B-R6-plus = AUTHOR-DECLARED FUTURE PHASE
                  SCOPE PENDING AUTHOR DEFINITION
                  NOT AUTHORIZED
```

The author declared `PRE-G9B-R6-plus` on 2026-10-01 as the final refinement of
this GeoCeDG version. Nothing about it is defined here: no objective beyond that
statement, and no features, kernel or frontend changes, debt, verification
class, tests, branch, base, ADR or specification, smoke or prompt. It is not
`DESIGNED`, `AUTHORIZED` or `IMPLEMENTATION READY`, and no canonical prompt
exists for it.

The roadmap sequence becomes `PRE-G9B-R6` → `PRE-G9B-R6-plus` → `PRE-G9B-R7`.
The proposed future prompt
[`pre-g9b-r7-readiness-closeout.prompt.md`](../../.github/prompts/tasks/pre-g9b-r7-readiness-closeout.prompt.md)
was amended minimally:
- blob `7fbe83997605f25b13188df6167fd31fd107b255` →
  `0e423c5dda507e3c1f3a6e5ddec97c634ad09633`;
- it adds the dependency `PRE-G9B-R7` depends on
  `PRE-G9B-R6-plus` = `PASS — AUTHOR APPROVED`, in its header, its status
  block, its authorization boundary and its stop conditions.

It stays `PROPOSED FUTURE`, unexecuted and not authorized, and is otherwise
unchanged.

## Promotion

The author authorizes promotion of the linear history
`e7401734… → 4333d032… → dd486b59… → this closeout commit` to `main`, by ordinary
clean non-force fast-forward only.
- **Excluded:**
  - merges, rebases, squashes, amends, force pushes and history rewrites;
  - working-branch or unrelated-branch pushes;
  - tags and GitHub releases;
  - binary or commercial publication.
- **Precondition:** promotion proceeds only if local `main`, `origin/main` and
  the live remote `main` still equal the base below immediately before it.

Remote identity at closeout entry:

```text
local main       = e7401734b4ef76d8f79dd4b0535845304937e4f7
origin/main      = e7401734b4ef76d8f79dd4b0535845304937e4f7
live remote main = e7401734b4ef76d8f79dd4b0535845304937e4f7
tree             = 3ad888b0895d0cb026be60e3d435c0819c4faed8
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained locally.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
PRE-G9B-R6                      = PASS — AUTHOR APPROVED
GGBScript capability matrix     = CLOSED
TD-R0-GGBSCRIPT-STRATEGY-RESTORE = RESOLVED — AUTHOR APPROVED
D_R6_DOC                        = DOCUMENTARY RECONCILIATION OF T_R6 EVIDENCE
PRE-G9B-R6-plus                 = AUTHOR-DECLARED FUTURE PHASE / SCOPE PENDING AUTHOR DEFINITION / NOT AUTHORIZED
PRE-G9B-R7                      = DESIGNED — NOT AUTHORIZED
                                  BLOCKED UNTIL PRE-G9B-R6-plus IS CLOSED
G9B                             = NOT AUTHORIZED
```

- **`PRE-G9B-R6-plus`** needs a separate author definition of its scope and a
  separate explicit authorization naming an exact base.
- **`PRE-G9B-R7`** additionally requires `PRE-G9B-R6-plus` =
  `PASS — AUTHOR APPROVED`.
- **`G9B`** does not follow `PRE-G9B-R7` automatically.
- **Still unauthorized:** `G9C`, `G9U2`, productive `G10` and further `G12`.
- **No tag or release is authorized.**

# PRE-G9B-R7 — frozen technical/documentary candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN AT COMMIT (this report is part of the candidate)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
CANDIDATE IDENTITY        = the commit that adds this report; a file cannot name its own
                            commit, so the commit, tree and every acceptance result are
                            reported with the author handoff and the untracked evidence
                            manifest (§5), never edited into this file
PARENT                    = b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513 (tree 3aef929b)
VERIFICATION_CLASS        = GLOBAL_IMPACT; PRIMARY_ACCEPTANCE = FINAL
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

Under the frozen-candidate rule of `.github/prompts/tasks/task-template.prompt.md`, this
report records only facts fixed when the candidate was frozen. The later author decision and
closeout record are the sole authority for author approval.

## 1. Delta from the base

One commit on `phase/pre-g9b-r7-readiness-closeout`, parent `b9bdb1e1`. Documentation,
roadmap, prompt and validation mirrors only; the file list is the
[phase-entry record](pre_g9b_r7_phase_entry_record.md) §5. Unchanged: `source/**`,
`tools/**`, `gradle/**`, build scripts, `packaging/**`, `LICENSE`, `LICENSES/**`,
`NOTICE.md`, `THIRD_PARTY.md`, `geocedg/specs/operations/**` (registry, schemas, static
contracts, JUnit inventory, prompt catalog), `docs/user/**`, every historical closeout
record and accepted evidence file. The product is therefore byte-for-byte the product of the
approved G-R1 technical candidate T' `da1a0952` (whose descendants up to the base changed
only documentation and mirrors).

## 2. Frozen execution plans (resolved with `-PlanOnly` on the base)

| Profile | Plan hash | Nodes | Acceptance leaves |
|---|---|---|---|
| `STATIC` | `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` | 10 | 3 |
| `FINAL` | `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` | 68 | 41 (the 19 `INTEGRATION` leaves and the 22 `INFRA_UNIT` leaves) |
| `PHASE -Phase PRE-G9B-R5-A` | `31a5d90f3f251a4abcc1f74fd03c637d9860970ef0494edca7030708da3f0b04` | 7 | includes `infra.guide-structure-diagnostic` |
| `PHASE -Phase PRE-G9B-R6` | `2583976cdd2a47df080a3f8f6834a33c6fd07d0ff4e29dacfa34b0f9143e21ae` (identical to the accepted R6 plan) | 10 | includes `ggbscript-matrix.product`, `static.ggbscript-matrix-inputs.semantic` |
| reference only: `INTEGRATION` | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` | 46 | 19 (covered by `FINAL`; not run separately) |
| reference only: `INFRA_UNIT` | `830887b17bb7030cdacda0da6999ad5fde64c86e52e96361a34d5f4cc2ffce1d` | 22 | 22 (covered by `FINAL`; not run separately) |

## 3. Frozen acceptance campaign

Executed once each, on the exact frozen candidate, in this order:

```text
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-r7-static-<commit8>
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R5-A -LogDirectory artifacts\agent\pre-g9b-r7-phase-r5a-<commit8>
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6 -LogDirectory artifacts\agent\pre-g9b-r7-phase-r6-<commit8>
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r7-final-<commit8>
```

Console output goes to the session scratchpad; the log directories are fresh paths inside
the repository, as `packaging.repository-safety` requires. If `FINAL` is accepted, its
receipt is authenticated with `Read-VerificationJson` and `Get-VerificationResultHash` and
inspected with `tools/agent/phase-closeout.ps1 -Action INSPECT`. No run is repeated on the
unchanged candidate to erase a diagnostic or an environmental incident.

Pre-freeze documentary checks (on the staged candidate tree): repository-relative Markdown
links of every changed file; JSON validity of the three mirrors; `git diff --cached --check`;
the byte-level line-ending preservation of the mixed-EOL roadmap; `Test-PromptContractDocument`
(profile `task`) for the amended prompt; the phase-lineage verification of the entry record.

## 4. Coverage reasoning

- `FINAL` is the frozen class's acceptance and covers every live `INTEGRATION` and
  `INFRA_UNIT` leaf; no standalone `INTEGRATION` or `INFRA_UNIT` duplicates it.
- `STATIC` is run separately for the documentary and repository-contract changes.
- The two `PHASE` runs are focused verification for the three registered acceptance leaves
  that no `FINAL` node contains (§2); their small JUnit selections duplicate `FINAL` coverage
  and are accepted as the cost of executing those leaves through their registered phases.
- No other accumulated phase selection carries a non-JUnit leaf outside `FINAL`; every phase
  JUnit selection is a subset of `final.shared` or `final.desktop`.

## 5. Evidence handling

Run evidence stays in the ignored `artifacts/agent/pre-g9b-r7-*` directories, with an
untracked evidence manifest listing each run's command, exit code, run identifier, candidate
commit and tree, plan hash, result hash, verdicts, diagnostic findings and receipt identity,
plus SHA-256 sums of the evidence files. A rejected verdict is reported as executed and never
relabelled; historical rejected results (`PRE-G9B-R1`, `PRE-G9B-R3-X1`) stay unchanged.

## 6. Impact declarations

```text
PRODUCT_PHASE_EFFECT = NONE
SERIALIZATION_IMPACT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
GUIDE_IMPACT = NONE (developer documentation updated for distribution-status and version references)
AUTHOR_SMOKE = REQUIRED — NOT EXECUTED (author)
```

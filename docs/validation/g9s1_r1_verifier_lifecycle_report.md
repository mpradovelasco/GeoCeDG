# G9S1-R1 verifier lifecycle Stage I report

- Status: **IN PROGRESS — T1 frozen; verifier-contract T2 and fresh postcommit gates pending**
- Date: 2026-09-03
- Phase status: `IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`
- Decision: [ADR 0023](../adr/0023-phase-verifier-lifecycle-and-author-closeout.md)
- Architecture: [verifier lifecycle](../architecture/g9s1_r1_verifier_lifecycle.md)
- Evidence root: `artifacts/g9s1-r1-verifier-lifecycle/` (ignored operational evidence)

## 1. Accepted conflict characterization

The original
`tools/agent/verify-g9s1-r1-spline-pair-materialization.ps1` is a PRECOMMIT
candidate verifier. It requires the exact base `HEAD`, exact feature branch,
candidate status, and false approval/PASS indicators. The first candidate commit
therefore leaves its intended domain by construction.

The conflict is verification-lifecycle-specific. No R1 geometry, pair selector,
interval certificate, token, persistence or public command defect was found.
The correction must retain the old verifier behavior while adding separately
authenticated committed-candidate and future author-closeout states.

## 2. Entry and frozen candidate

| Field | Frozen value |
|---|---|
| Entry `HEAD` | `109f077fc5e2a40bcde45d3271eb928ee66fdfcc` |
| Branch | `codex/g9s1-r1-spline-pair-materialization` |
| Index | empty |
| Candidate paths | 76 |
| Full raw input entries | 11,195 |
| Full raw input inventory SHA-256 | `1921b8d34c7611f585a49026c8a5a4097933fe1290f321a5bfe3709e80c1d529` |
| Original R1 verifier raw SHA-256 | `9285001b46961d4ace3ccbdd5ec0a7ee2848c1b67a2446280c96e7a5d299d62a` |
| R1 evidence raw SHA-256 | `b5a0d1617feb1038f5f1d18367c41b6b491235fe119286e08272913c0d172347` |
| R1 scenarios raw SHA-256 | `bbbb24f2900db65e28b7b04b459259798aa811fd03e9af5be9b26eaeff682f53` |
| Freeze record SHA-256 | `df89be1e312f7e91d1c9d2a50fa51316011d4841603ab7bbb4959b0c8c88afe8` |
| Freeze input-list SHA-256 | `1921b8d34c7611f585a49026c8a5a4097933fe1290f321a5bfe3709e80c1d529` |

The freeze record identifies itself as
`EXACT_PRECOMMIT_COHORT_PROVENANCE_NOT_AUTHOR_APPROVAL`. It records:

```text
selfApproved = false
authorApprovedPhase = false
passClaimed = false
```

The raw candidate bytes and input inventory were captured after the final
precommit execution and before T1. No tracked executable/source input was changed
between that capture and the commit.

## 3. Exact precommit execution evidence

Every run used the original verifier bytes and recorded repository commit
`109f077fc5e2a40bcde45d3271eb928ee66fdfcc`.

| Run | UTC interval | Result | Root-result SHA-256 |
|---|---|---|---|
| PHASE A | 16:10:38–16:11:20 | 192/192, exit 0 | `57423783148cfe6f0402f9cf913cec9c05b5c9cda6b3fd43c405d4a7ad6e5a53` |
| PHASE B | 16:11:56–16:12:28 | 192/192, exit 0 | `8444a5e5263c354ba06f2114f9ae37afe89a080ce6cb40abb1c34f42fef7e4aa` |
| COMPOSED | 16:12:55–16:30:19 | 1,281/1,281, exit 0 | `62de39ee7b106b1f4063a5bdbbf18de60d1566181845a018e9c7074efd95b641` |
| clean FULL | 16:31:00–16:54:40 | 7,770 passing; 11 inherited upstream skips; zero failures/errors; exit 0 | `6aa8be9c9b320db246685de1b059e787ebd232d530bd3e192cebb860b957b51f` |

The canonical R1 summary is identical across all four executions:

```text
424568ebd5a23dcc3b643586310ad4ed3f1f679725ac9cd79f40d45e06e9618b
```

The COMPOSED and FULL current-run receipts share input fingerprint:

```text
5eb5307e93c9c82eedfd8f7a25d2361495ef0ea622478290c9c1162f80a030b8
```

Additional receipt pins:

| Evidence | COMPOSED | FULL |
|---|---|---|
| Build-evidence SHA-256 | `298334cca8817167aace9b84ed9e8f6c3310d1d25084299061c09e2b86c23cc7` | `defb6b8649482efbc9195f7c71b9ef55b86cd0f3e9bccabce76a7f9c33173627` |
| Receipt input-inventory SHA-256 | `40259fe70013d7ca678f2a871aaafd08e9f703312f08e5c90f88b67b12ae8c45` | same |
| JUnit tests | 1,281 | 7,781 total, including 11 inherited upstream skips |
| JUnit failures/errors | 0/0 | 0/0 |

These are precommit technical results. They are not postcommit executions and
do not confer author approval.

## 4. Technical candidate commit T1

The exact validated candidate was committed without a preparatory source edit:

| Field | Value |
|---|---|
| `R1_CANDIDATE_COMMIT` | `f761758bd664504057413539b9729ba444c904c1` |
| Tree | `e9636950614d73d415e46e77bb32005c40b9d717` |
| Parent | `109f077fc5e2a40bcde45d3271eb928ee66fdfcc` |
| Changed paths | 76 |
| Commit purpose | `Implement G9S1-R1 certified spline pair materialization` |

All 76 T1 blob IDs equal the corresponding `rawGitBlob` values in the freeze;
the mismatch count is zero. This proves that T1 contains the frozen candidate
bytes exactly. It does not relabel any precommit result as having run on T1.

## 5. Expected old-verifier domain exit

Immediately after T1, the unmodified old verifier stopped at its entry assertion:

```text
R1 candidate requires unchanged entry HEAD.
```

This is recorded as `EXPECTED_VERIFIER_DOMAIN_EXIT`. The invocation log SHA-256
is:

```text
323c2084c069c43907ce2c1b0efd9e98a3a70d80db9ab6cfc84544aeac61ef25
```

The failure occurred before a scientific classification could change. It is not
a product regression, and no old evidence has been altered to hide it.

## 6. Bounded verifier-contract correction

The Stage I infrastructure delta is limited to exactly twelve paths:

- preserving the original `PRECOMMIT_CANDIDATE` entry semantics;
- authenticating T1 and a tightly bounded infrastructure descendant in
  `COMMITTED_CANDIDATE` without branch-name authority;
- implementing but not activating `AUTHOR_CLOSEOUT` for a future exact reviewed
  SHA;
- fixture tests for positive and negative lifecycle/provenance cases;
- the minimum top-level phase integration;
- the generic narrow status-only evidence-linkage rule in ADR 0023 and its
  operational documentation.

Those paths are the lifecycle helper and its fake-first test, the R1, baseline
and verification-infrastructure entry points, the verification-runtime
fake-first test, the exact lifecycle policy, ADR 0023, this report, the lifecycle
architecture, the verification-level contract and the developer-guide impact
note. There is no `source/` path in this delta.

The correction must retain all live R1 scientific assertions. It may not change
productive Java, scientific tests, tolerances, numerical references, candidate
status or approval flags.

The second technical commit is the current unpublished technical HEAD. The
first committed-candidate COMPOSED attempt passed its canonical 1,281 tests and
all R1 assertions, then failed the baseline commit-range whitespace gate because
validated committed CRLF bytes were interpreted as trailing blanks by the
caller's implicit Git whitespace policy. This is retained as a failed
postcommit attempt, not relabelled as scientific failure or PASS.

The bounded correction makes the baseline verifier pass Git's explicit normal
blank rules plus `cr-at-eol`. Its fake-first control requires an LF-to-CRLF-only
commit range to pass and a real trailing-space mutation to fail. The unpushed T2
must be amended so the required two-commit architecture remains exact, then all
focused/PHASE/COMPOSED/FULL evidence must restart against the new T2.

## 7. Future status-only closeout contract

A future author-closeout check will require the author to name one exact
`REVIEWED_TECHNICAL_COMMIT`. It will prove ancestry, hash-linked technical
evidence, an exhaustive bounded approval/status delta and raw equality of all
executable inputs. Any product, test, verifier, tolerance/reference or unknown
change invalidates the shortcut.

If the proof passes, reporting remains explicit:

```text
technical PHASE/COMPOSED/FULL executed on T
author closeout consistency checked on C
executable payload C == T
```

It must not say that the old tests executed on C. Stage I tests this mode only
with synthetic isolated fixtures; actual R1 approval remains false.

## 8. Focused infrastructure evidence and pending Stage I gates

The final precommit infrastructure run completed without Gradle or product
execution:

| Fixture | Result |
|---|---:|
| verification runtime | 114/114 expected after the bounded CRLF fixture |
| generated-state safety | 18 cases / 143 assertions |
| phase lifecycle | 34/34 |

The phase-lifecycle result is normalized independently of its random temporary
fixture path as
`a3b1074cc07ccf912021d77220a485b6fcdd528b64c4ed4ef46e9320a915a99a`.
It verifies both positive lifecycle forms and fail-closed mutations of
ancestry, productive/test/verifier/tolerance paths, approval records, evidence
manifests, receipts, archives and raw input identity. The documentary link is
also proved non-consumable as a current-run receipt.

The following remain mandatory and **not yet claimed**:

1. `R1_VERIFIER_CONTRACT_COMMIT` containing only the bounded infrastructure
   delta;
2. a fresh shell/process against its clean committed source cohort;
3. postcommit `G9S1-R1` PHASE;
4. postcommit COMPOSED;
5. postcommit clean FULL;
6. exact designation of `R1_REVIEWED_TECHNICAL_COMMIT`;
7. final source/inventory/diff and false-approval checks.

The old precommit FULL cannot satisfy the postcommit FULL gate. A future result
must not be predicted or backfilled into this report.

## 9. Impact review

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = UPDATED
```

Bootstrap remains unchanged because this work introduces no new executable,
PowerShell/JDK/Gradle/Conda requirement, download, package, environment variable,
workstation role or scientific reference. Existing wrappers and toolchain
selection remain authoritative. The developer guide is updated because operators
must distinguish PRECOMMIT, COMMITTED and AUTHOR_CLOSEOUT modes and must not
misstate evidence reuse as a rerun.

## 10. Governance boundary

Current real authority remains:

```text
G9S1-R1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
selfApproved = false
authorApprovedPhase = false
passClaimed = false
```

There has been no tag, main promotion or G9U1 work in this Stage I checkpoint.
The protected G9U1 design remains unchanged and G9U1 implementation remains
NOT AUTHORIZED. Stage II remains forbidden until a later author instruction both
approves G9S1-R1 and names the exact reviewed technical commit established after
fresh postcommit validation.

## 11. Bounded index-reconstruction hotfix after the stopped Stage II attempt

The subsequent author decision approved technical commit
`0d621a91696e3de530f4410d22932c4fd6759f3e`. Its attempted `AUTHOR_CLOSEOUT`
exited 1 with `Receipt index does not represent the exact technical commit.`
The failure is retained as historical evidence, not relabelled as success.
PowerShell applied `-f` to the final concatenated term instead of the whole
format string, leaving literal `{0} {1}` in each reconstructed index record.
The actual 11,201-entry receipt index hash was
`cd92f76f15479852d9b6512311dbd71fa3b0225a61901636d447a050ad41afc5`;
the defective reconstruction produced
`b75409800f6ff40d9bb6e58bbdddd2df61284765a5cfbdf6c451128e5f4550b1`.

The authorized implementation correction only groups the complete format
string before `-f`. It changes no hash algorithm, receipt meaning, ordering,
normalization, index check, or scientific assertion. The fixture now obtains
receipt index evidence independently from Git's `ls-files --stage`, rather
than generating the oracle with the helper being tested. Controls include
exact mode/blob records, a UTF-8 path, the historical Git inventory without
duplicating it, and malformed/reordered/mode/blob/literal-placeholder tampering.
On the unchanged defective helper the expanded fixture records 32/38 passes
(exit 1), including failures of both correct synthetic closeout forms.

The author separately authorized changing only
`maximumInfrastructureCommits: 1 -> 2` so this one additional technical hotfix
can follow the existing infrastructure commit. The original one-commit-bound
negative remains, and a new two-commit fixture accepts the bounded second
commit but rejects a third. Ancestry, path, blob, status, approval and evidence
assertions are unchanged. No allowlist is enlarged.

The seven pending approval/status files were preserved byte-exactly, with a
manifest and tracked diff, under ignored
`artifacts/g9s1-r1-closeout-hotfix/preserved-closeout/`, then restored to the
reviewed technical HEAD. The author decision is preserved; its prepared
closeout delta is not part of this hotfix and must not be reapplied without a
later exact-SHA authorization. The prior FULL remains bound to `0d621a9...`.
New focused checks and clean FULL are required on the new technical commit.
The unchanged documentary-link contract also requires separate PHASE A/B and
COMPOSED roots; a FULL root cannot be renamed to impersonate those executions.
New run results belong in ignored execution evidence, not in historical records.

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = NONE
```

The existing PowerShell/JDK/Gradle selection, workstation prerequisites,
bootstrap entrypoints and developer lifecycle commands are unchanged. This is
a format-expression fix plus its independently generated infrastructure
fixtures and the explicitly bounded commit policy; no user or scientific API
changes. G9U1 is not implemented. The R4 periodic-quarantine round-trip risk
remains open and tracked. No publication, phase tag or main promotion is
authorized by this hotfix task; stop after new technical validation for author
review of the exact new commit.

## 12. Bounded logical-EOL hotfix after the second stopped Stage II attempt

The author approved technical commit
`22f9ef4198e34ca79f542eb82a4f72b1f8e51e56`. Its real `AUTHOR_CLOSEOUT`
attempt exited 1 in the inherited R4 `git diff --check` call (line 2167 of
that commit). It rejected roadmap line 10's final CR from the approved CRLF
delimiter as trailing whitespace. There was no trailing ASCII space or tab.
The failed execution remains FAILED historical evidence under ignored
`artifacts/g9s1-r1-closeout-hotfix/stage-ii-approved/`; it is not reclassified.
The roadmap's pending approved raw SHA-256 remains
`04c5eaed53b437fa7fcc9dbb51a8ef1f392b7ec6999422dcff5564776fb8907f`.

### Audited perimeter and exact correction

The actual documentary-closeout call chain is R1 -> R6 -> G9S1 -> R5 -> R4.
The audit includes its lifecycle, evidence-integrity, verification-runtime and
generated-state helpers, import edges, newline splitting and whitespace tests.
R4 refers to R3 source but does not invoke R3 on this path. The top-level
PHASE/COMPOSED/FULL orchestrator is not invoked by `AUTHOR_CLOSEOUT`.

| Executed verifier at reviewed commit | Unstaged/staged check lines | Correction |
|---|---|---|
| `verify-g9u0-r4-intersection-admissibility-continuation.ps1` | 2167 / 2170 | pin logical-EOL Git policy |
| `verify-g9u0-r5-locus-v2-similarity-transformations.ps1` | 941 / 944 | same |
| `verify-g9s1-semantic-spline-2d-capability.ps1` | 956 / 959 | same |
| `verify-g9u0-r6-semantic-locus-point-interaction-support.ps1` | 1088 / 1091 | same |
| `verify-g9s1-r1-spline-pair-materialization.ps1` | 593 / 595 | same |

All ten calls now explicitly select Git's normal
`blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol` policy, following the
already approved baseline-verifier precedent and section 10 of
[verification levels](../../geocedg/specs/operations/verification-levels.md).
Only the final CR delimiter is permitted; actual spaces/tabs before LF or CRLF
remain errors. This is not an ignore-whitespace option, a broad `TrimEnd`, a
Git/global configuration edit, or roadmap normalization.

There is no `\s+$` trailing-whitespace predicate in the audited chain.
Existing `\r?\n` splits are logical-line parsing; the lifecycle helper's
`TrimEnd("\r", "\n")` handles Git command-output delimiters, not source
whitespace validation. Path-separator trims and Git index/tree parsers have
different purposes and remain unchanged. The four helpers contain no affected
whitespace check. Unrelated standalone verifiers are outside this bounded
AUTHOR_CLOSEOUT correction; no general verifier refactor is performed.

### Regression and byte-authority separation

The expanded lifecycle suite executes the exact whitespace-command ASTs from
each of the five verifiers in disposable Git fixtures, never the real closeout.
The matrix covers LF, CRLF, the byte-exact approved roadmap projection, spaces
and tabs before each delimiter, and an extra terminal CR. Each runs through
both unstaged and staged checks with strict and permissive inherited settings.
The check must not modify file bytes; raw LF and CRLF hashes must remain
different. Existing raw-cohort, index, receipt and scientific-source assertions
remain live. No authority hash function, scientific tolerance or source input
is normalized by this hotfix.

Before the ten-call correction, the expanded suite recorded 39/44 successes
and five failures: every inherited verifier rejected valid CRLF. These are
expected regression-red results, not a new product failure. Green focused
results and fresh technical executions are required and will be recorded as
new ignored execution evidence after the hotfix commit; none is predicted here.

### Cohort isolation, provenance and impact

The original worktree retains all seven pending approval files byte-exactly.
An isolated local worktree at `artifacts/r1-eol`, branch
`codex/g9s1-r1-closeout-whitespace-hotfix`, starts from `22f9ef4...`; it contains
the technical candidate state, not those pending approval outputs. Its input
bytes are materialized against the reviewed raw inventory before correction.
No pending file is removed, reapplied, normalized or committed by this task.

The author's authorization for one additional bounded technical hotfix is
represented by `maximumInfrastructureCommits: 2 -> 3` and exactly four added
infrastructure paths: the inherited R4/R5/G9S1/R6 verifiers above. R1's verifier,
the lifecycle fixture, policy and this report were already listed. Existing
one- and two-commit rejection fixtures remain; the new three-commit fixture
rejects a fourth. Candidate blobs, ancestor checks and the entire seven-file
closeout path/content allowlist are unchanged. This does not authorize any
arbitrary future follow-up or productive delta.

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = NONE
Author decision: G9S1-R1 PASS — AUTHOR APPROVED (preserved)
PUBLISHED CLOSEOUT = PENDING
```

No PowerShell/JDK/Gradle prerequisite, bootstrap entrypoint, environment policy,
scientific input or operating command changes. The existing lifecycle guide
already separates technical evidence from closeout. ADR 0020 requires a fresh
clean FULL on the new infrastructure cohort; the existing documentary linkage
also requires independent PHASE A/B and COMPOSED roots. Prior FULL evidence is
historical, not evidence of this hotfix. Stop after the new technical cohort is
validated: no real AUTHOR_CLOSEOUT, approval commit, push, tag or promotion.
G9U1 remains unimplemented and unauthorized; the independent
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` risk remains OPEN / TRACKED.

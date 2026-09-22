# PRE-G9B-R1 closeout record

```text
PRE-G9B-R1 — AUTHOR APPROVED FOR PROMOTION — RETAINED PRE-EXISTING BASELINE DEFECT
```

```text
selfApproved                   = false
authorApproved                 = true
passClaimed                    = false

finalAcceptanceClaimed         = false
finalCoverageComplete          = true
candidateRegressionEstablished = false
authorBaselineException        = true
```

This record preserves an explicit author disposition and the exact evidence
identities behind it. It changes no executable, test, build, product,
verification or prompt file, and it does not modify the frozen technical
candidate.

It supersedes the `state` and `authorApproved` fields of the candidate report
and evidence file carried inside the frozen candidate, which necessarily
recorded `CANDIDATE — PENDING AUTHOR REVIEW` and `authorApproved = false`
because they were written before the author's review. Every other statement in
those two artifacts stands.

## Frozen technical candidate

```text
commit = 36c8d754a05b8394428ffb9505ec4e541fdf93eb
tree   = 7cab60ab1c76578fba6ec2ecbac193905a19eae3
```

The candidate is frozen exactly as reviewed and is not modified by this
closeout.

## Retained FINAL evidence

The completed FINAL evidence is retained exactly as executed:

```text
run              = verification-8763e250d1be413f83e54429b5923ca3
plan_hash        = defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0
result_hash      = c7e507897530acc9a83cb41535ca161db62bf43483d1a6181ec015dad7d352ca
coverage         = COMPLETE
required         = 40
completed        = 40
untrusted        = 0
not_run          = 0
acceptance       = REJECTED_SEMANTIC
receipt          = none
```

The recorded acceptance verdict is `REJECTED_SEMANTIC`. It is **not** rewritten,
suppressed or reinterpreted as `ACCEPTED`. No canonical acceptance receipt was
produced and none is fabricated: the receipt emitter is gated on an accepted
report, and its silence here is correct behaviour rather than an omission.

Supporting evidence from the same phase, all recorded in the frozen candidate:
the R1-focused receipt tests pass (14 cases, 69 assertions, 0 failures); `STATIC`
is `ACCEPTED` / `COMPLETE`; `INFRA_UNIT` is `ACCEPTED` / `COMPLETE`; and the
standalone G9X1 verifier passes with its DXF authority suite at 61/61.

## Retained pre-existing baseline defect

```text
id       = BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION
class    = RETAINED PRE-EXISTING BASELINE DEFECT
scope    = deterministic LF materialization of the bilingual guide resources
state    = OPEN — NOT REPAIRED IN PRE-G9B-R1
```

The sole semantic violation in the FINAL campaign was
`PostP1BilingualUserGuideTest.everySelectedResourceResolvesToTheDocumentOfThatLanguage`.

Causality was established before any disposition was sought. Every input
responsible for that failure is byte-identical between the authorized PRE-G9B-R1
base `78983cab36ca0cafbb63b3b1bec7392bce5c0424` and the candidate:

- both user-guide sources under `docs/user/`;
- the Desktop Gradle resource-copy rule;
- the failing test;
- the relevant action-registry input;
- `.gitattributes`.

The defect therefore predates PRE-G9B-R1 and was merely exposed by the first
FINAL campaign to execute that test after POST-P1-DOC-HELP. The bilingual
edition and the assertion both landed on 2026-09-17, and the last preceding
`FINAL` / `ACCEPTED` / `COMPLETE` campaign started 2026-09-16T21:08:30Z — the day
before. **It is not an R1 regression.**

Its preferred future disposition is to establish canonical LF materialization at
repository authority level rather than to weaken the test. Implementation of that
disposition is outside this closeout, and nothing here authorizes it.

The retained defect must be corrected before a future gate whose acceptance
genuinely depends on that resource invariant, preferably as a bounded maintenance
item absorbed before the next already-required heavy verification campaign.

## Author disposition

The author accepts the PRE-G9B-R1 implementation candidate for promotion because:

- all R1-focused receipt tests pass;
- `STATIC` is `ACCEPTED` / `COMPLETE`;
- `INFRA_UNIT` is `ACCEPTED` / `COMPLETE`;
- the standalone G9X1 verifier passes;
- the FINAL execution reached `COMPLETE` coverage;
- its sole rejection is causally established as a pre-existing baseline defect
  outside the R1 delta;
- no known R1-introduced regression remains.

This is an explicit author exception to the normal green-FINAL closeout
requirement. It does not change the recorded FINAL verdict and does not
fabricate a canonical acceptance receipt. Technical acceptance was not
established, and none is claimed.

## Operational rule recorded for future work

A global verification failure discovered during a phase must not automatically
become phase scope. After a complete heavy campaign on an immutable candidate:

- failures causally attributable to the candidate remain phase blockers;
- failures proven to predate and lie outside the candidate delta are retained
  baseline debt;
- retained baseline debt does not trigger repeated heavy campaigns during the
  same phase unless it invalidates confidence in the verifier itself or prevents
  establishing causality.

Heavy verification should be executed on frozen meaningful cohorts, not
repeatedly after incidental unrelated repairs.

This rule is recorded here as an author disposition. Promoting it into durable
operational authority is a separate author-authorized task; this closeout changes
no verification authority.

## Authorization state

No successor `PRE-G9B-R` phase is authorized by this disposition. `R1-R1` through
`R7`, `R2-E0`, `R2-E1`, `G9B`, `G9C`, `G9U2`, productive `G10` and further `G12`
remain unauthorized. No further FINAL, FULL, INTEGRATION, PHASE, INFRA_UNIT,
STATIC, JUnit or other verification campaign is to be executed for PRE-G9B-R1.

Promotion is authorized by the author for the exact frozen candidate; the exact
closeout candidate is reported for author review before any push or promotion.

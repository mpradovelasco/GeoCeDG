# PRE-G9B-R2 closeout record

```text
PRE-G9B-R2 = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
```

This record preserves an explicit author disposition and the exact evidence
identities behind it. It changes no executable, test, build, product,
verification or prompt file, and it does not modify the frozen technical
candidate.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the candidate's own artifacts record only `TECHNICAL_CANDIDATE_STATE = FROZEN`
and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole
authority for current author approval. Every statement in the candidate's
artifacts stands unchanged.

## Frozen technical candidate

```text
commit = f451f603cb4c9e7f887cbfef3ad6cf90800f98a5
tree   = c82d0e4a869b3c094b2cf802216573257e0db240
base   = 51a6b096335938bb2580eb3b8e1101db0f2f1575
```

## Accepted evidence

The author determined the three prescribed campaigns sufficient; no further
verification is run for this closeout.

| Campaign | Run | Plan hash | Result hash | Verdict |
|---|---|---|---|---|
| `PHASE -Phase G9S1-R1` | `verification-9b8743ca78714821ac4eb9810cc76139` | `4cb0ecf5caa86cc1d26d65a0673c352d83b83ed20de706ee23166e942e0ad430` | `3c688f23ce117d123f5630668db8d2809353bceb7d1e9d070ffbe2980b92aa57` | `ACCEPTED / COMPLETE`, 13/13 |
| `PHASE -Phase G9U0-R6` | `verification-eed461f5907c4bedb055b6ce6887ec5b` | `db8be1b098fcbda0ad294de2158e3a298c913f43212849cd4fc8cf11f8aa48c2` | `89d55c2b7b74accde44823a55dfda710c0b5a1d0f04a6198278dbe261d06cee2` | `ACCEPTED / COMPLETE`, 13/13 |
| `INTEGRATION` | `verification-df533754a9c94fe6b51e0f52e7ef0f1c` | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` | `71a7853a2416277eec97c45466f08617f50c359437b19a47749b00646ae88b26` | `ACCEPTED / COMPLETE`, 19/19 |

Every run is bound to the frozen candidate commit and tree, with zero untrusted
and zero not-run checks.

## Debt disposition

```text
TD-P1-SEMANTIC-LENGTH-INTERSECTION = CLOSED
```

## Retained separately

These are recorded here and are not resolved by this closeout.

| Item | Class | Statement |
|---|---|---|
| `GUIDE-A1-SECTION-9.3-STALE` | pre-existing documentation drift, owned by POST-G9U1-A1 | Both guide editions' §9.3 state that `Length(S,A,C)` is undefined for the constructor points of the §7.5 example; that has been false since A1, as `PostG9U1A1SplineConstructorProvenanceTest#uniqueOccurrencesFeedRichAndScalarMetricsAndAgreeWithAddresses` shows |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | retained pre-existing baseline defect | The standalone G9U1 verifier fails on a branding evidence pin whose origin predates PRE-G9B-R1-R1; it is outside the frozen `PHASE` acceptance |
| `OBS-R2-PHASE-SELECTION-COHORT` | verification observation | `PHASE -Phase G9S1-R1` and `PHASE -Phase G9U0-R6` currently resolve to the same thirteen required checks, and `INTEGRATION` is a superset, so the prescribed R2 triple largely re-ran one cohort |

## Authorization state

`PRE-G9B-R2-E0`, `PRE-G9B-R2-E1`, `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive
`G10` and further `G12` remain unauthorized; this closeout neither authorizes nor
executes any of them. No tag or release is authorized. Promotion is authorized by
the author for the exact frozen candidate, by clean fast-forward of `main`.

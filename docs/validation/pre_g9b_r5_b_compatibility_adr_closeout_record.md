# PRE-G9B-R5-B compatibility ADR closeout record

```text
ADR 0031 = ACCEPTED — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
candidateMutated          = false
technicalCandidateFrozen  = true
implementationAuthorized  = false
newHeavyFinal             = NONE
```

This record preserves the explicit author decision of 2026-09-30 on the
`PRE-G9B-R5-B` compatibility ADR, and the exact evidence identities behind it.
It changes no product, test, build, verifier, inventory, registry, schema,
diagnostic, guide, prompt, packaged-resource or specification file. It does not
modify the approved candidate. Besides this record and its machine-readable
mirror, it changes only:

- the status header of
  [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md),
  whose decision text is unchanged;
- the roadmap status statements.

Under the frozen-candidate rule, the
[candidate report](pre_g9b_r5_b_compatibility_adr_candidate_report.md) and its
[evidence mirror](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-command-name-compatibility-evidence.json)
record the frozen design state, including
`AUTHOR_DECISION = PENDING APPROVAL OF THE EXACT AMENDED CANDIDATE`. They stay
unchanged as historical design evidence. This record is the sole authority for
the current author approval.

## Author decision

```text
ADR                        = 0031-canonical-english-command-surface-compatibility
APPROVED CANDIDATE         = c459de9ab66d61fe849abbf9b9b8097c56605c48   (D_R5B_ADR_R1)
APPROVED TREE              = 3c8d0943f2be7b990e0e4cc97cd114203dd47a49
AUTHOR DECISION            = APPROVED
selfApproved               = false
```

The approval covers the compatibility ADR and the policy and design decisions
in that exact commit. It extends to no other descendant except this status-only
closeout. It does **not** authorize productive `PRE-G9B-R5-B` implementation.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base `P_R5A` | `4389b30b5478ac35c78a15255021e3c4f1e10ea3` | `1a4506b830a846efd5032fde5e8386ca8872dc89` | `4804f505…` |
| initial candidate `D_R5B_ADR` | `3b65af5303aac316a8b2747739aeb43e2921bac4` | `c6a81b27ba0c805dfbab969cf52727b27e53c564` | `P_R5A` |
| amended candidate `D_R5B_ADR_R1` (approved) | `c459de9ab66d61fe849abbf9b9b8097c56605c48` | `3c8d0943f2be7b990e0e4cc97cd114203dd47a49` | `D_R5B_ADR` |

The branch is `design/pre-g9b-r5-b-command-surface-compatibility-adr`. History
is linear and single-parent, with no merge. The canonical `R5-B` prompt
`.github/prompts/tasks/pre-g9b-r5-b-canonical-english-command-surface.prompt.md`
(blob `34e1796d5628108498a7c612c155d5c8d72a4c16`) is unchanged. Its
`ACCEPTED_COMPATIBILITY_ADR` and `IMPLEMENTATION_BASE` fields stay unresolved
until a separate governance-layer amendment.

## Accepted evidence

The class was frozen at task start and never escalated:

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = DOCUMENTATION_STATUS_ONLY
frozenAtPhaseStart  = true
ACCEPTANCE          = STATIC
```

Under `geocedg/specs/operations/verification-levels.md` §12.8,
`DOCUMENTATION_STATUS_ONLY` requires static validation only. Each run is bound
to its exact candidate and preserved as executed:

| Candidate | Run | Plan hash | Result hash | Result file SHA-256 | Verdict |
|---|---|---|---|---|---|
| `D_R5B_ADR` `3b65af53…` | `verification-781b5534a0d24c448297b7ea02336637` | `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` | `4969b6bed35d080f713d63848d5d44eff6a3027158677858f8b975d69c6c38cc` | `e3f7bb856aa4eaf09aaa3d4e2f9f0c54c48dd49115be659433a2cfcbe4da2374` | `ACCEPTED / COMPLETE`, 3/3 |
| `D_R5B_ADR_R1` `c459de9a…` (approved) | `verification-45a9a4d0fc014c4192ed9dddbc562c30` | `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` | `bfa56ef13ac121c200dd59a2225e6c9ad0a5506cd4ba8553621f25877d49e107` | `dae3f5e49a6bd7dab9502a88674304ea15ecd8a65cc322886fcb7eb16e9925c3` | `ACCEPTED / COMPLETE`, 3/3 |

Both runs satisfied `prompt.execution-safety`, `repository.boundary-safety` and
`static.scientific-inputs.semantic`. The only non-clear diagnostics are the
standing `diagnostic.governance` finding and the
`diagnostic.historical-consistency` unavailability that every `STATIC` run
carries.

`STATIC` emits no FINAL acceptance receipt. None is claimed, fabricated or fed
to the identity-only closeout adapter. The run evidence stays in the ignored
local evidence area, under `artifacts/agent/pre-g9b-r5-b-adr-static-3b65af53/`
and `artifacts/agent/pre-g9b-r5-b-adr-r1-static-c459de9a/`. The scratch
characterization probes are recorded in the candidate report §3 and were never
acceptance evidence. No PHASE, INTEGRATION, FINAL or `GOV-R` run is made for
this closeout; only its static checks run.

## Accepted policy

The command-name model:

```text
internal command identity     = unchanged internal semantic authority
canonical public command name = canonical English public name
                                (authority: the English command bundle,
                                 not the internal enum identifier)
localized command names       = compatibility aliases only, where currently supported
```

The accepted decisions:

```text
Alternative D                 = accepted
syntax placeholders           = UI language
mathematical/function names   = current UI-locale behaviour
captions / automatic labels   = UI language
GGBScript                     = canonical English/internal semantics
XML / serialization           = unchanged by R5-B
collision guard               = complete-inventory validation; fail closed
Spanish Perímetro             = retained compatibility observation, not a blocker
```

Alternative D keeps ordinary `USER`, `SCRIPT` and `XML` lookup and the reverse
table unchanged. It presents command heads in canonical English by semantic
role, and it admits that presentation only through a complete-inventory
round-trip gate plus a typed fail-closed re-entry outcome. The governing
invariant:

```text
canonical English display → edit / redefine → same internal command semantic identity
```

Ambiguity fails closed. Silent retargeting is forbidden.

## `Dot`/XML disposition

```text
DOT_XML_DEFECT = REAL / PRE-EXISTING / UPSTREAM-INHERITED
DISPOSITION    = SEPARATE BOUNDED MAINTENANCE
ORDER          = MUST BE FIXED AND PUBLISHED BEFORE PRODUCTIVE R5-B
REPAIRED HERE  = false
```

Measured fact, unchanged: a Spanish-UI `Dot(u,v)` is serialized as
`ProductoEscalar(u, v)`, and reopening the file in either UI language loses the
dependency.

The defect is not repaired here and is not absorbed into `R5-B`. ADR 0031 is
not weakened to tolerate it: `R5-B` must not change serialization, and it
inherits the repaired serialization of its future implementation base.

## Impact dispositions

```text
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = ADR acceptance and status only; no observable or
  developer-facing contract changed. Productive R5-B will require
  GUIDE_IMPACT = UPDATED (user guides §16.3).

BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, Conda,
  packaging, download or build/verification entry point changed.

VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
```

## Observation and debt disposition

None of the items below blocks ADR acceptance. Each is carried forward
unchanged, and none is repaired here.

| Identifier | Disposition |
|---|---|
| `HZ-R5B-DOT-LOCALIZED-XML` | separate bounded maintenance; blocks productive `R5-B` until published |
| `HZ-R5B-ALIAS-CAS-STATE` (`Perímetro`) | retained compatibility observation; `R5-B` must not worsen it |
| `HZ-R5B-LABEL-SHADOWING`, `HZ-R5B-MACRO-PRECEDENCE` | handled by the ADR's typed re-entry obligation in future `R5-B` |
| `HZ-R5B-IDENTITY-FROM-DISPLAY-TEXT`, `HZ-R5B-DISPLAY-TEXT-MATCHERS`, `HZ-R5B-SCRIPT-EDITOR-RETARGET` | future `R5-B` obligations |
| `HZ-R5B-HARNESS-TABLE-FRESHNESS` | test-harness caveat for future tests |
| `CSolve` / `CSolutions` literal-key syntax | pre-existing; outside `R5-B` |

The ledger of the [`PRE-G9B-R5-A` closeout](pre_g9b_r5_a_closeout_record.md)
carries forward unchanged.

## Promotion

The author authorizes promotion of the linear history
`4389b30b… → 3b65af53… → c459de9a… → this closeout commit` to `main`, by
ordinary clean non-force fast-forward only. There is no merge, rebase, squash,
amend, force push, design-branch push, tag or release. Promotion proceeds only
if local `main`, `origin/main` and the live remote `main` still equal the base
below immediately before it.

Remote identity at closeout entry:

```text
local main       = 4389b30b5478ac35c78a15255021e3c4f1e10ea3
origin/main      = 4389b30b5478ac35c78a15255021e3c4f1e10ea3
live remote main = 4389b30b5478ac35c78a15255021e3c4f1e10ea3
tree             = 1a4506b830a846efd5032fde5e8386ca8872dc89
```

The identity after promotion is this record's own commit, which the record
cannot name; it is reported with the promotion.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
ADR 0031                     = ACCEPTED — AUTHOR APPROVED
PRE-G9B-R5-B                 = DESIGNED — NOT AUTHORIZED
PRE-G9B-R5-B BLOCKED_ON      = published Dot/XML serialization maintenance
PRE-G9B-R6                   = NOT AUTHORIZED
PRE-G9B-R7                   = NOT AUTHORIZED
G9B                          = NOT AUTHORIZED
```

The next permissible task is the separately authorized bounded `Dot`/XML
serialization maintenance. It has not started.

Productive `R5-B` also needs a new explicit author instruction naming its exact
implementation base, which will be the published commit after that maintenance.
That instruction must come with the governance-layer amendment of the canonical
`R5-B` prompt, listed in ADR 0031 and the candidate report §16.5. `G9C`, `G9U2`,
productive `G10` and further `G12` remain unauthorized. No tag or release is
authorized.

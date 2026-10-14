# PRE-G9B-R6-plus-G implementation authorization record

```text
RECORD_KIND                       = AUTHOR AUTHORIZATION AND ENTRY GATE
TASK                              = PRE-G9B-R6-plus-G IMPLEMENTATION AUTHORIZATION
AUTHORIZATION_DATE                = 2026-10-14
APPROVED_BASE                     = ceb5a7e9ef8bafcfabb10aeba5162451d88f79c3
                                    tree fa748fd1b87ae16c9f0c248f8f77b94069cc2ea3
                                    (published G author design closeout)
BRANCH                            = phase/pre-g9b-r6-plus-g-implementation (local, from ceb5a7e9)

PRE-G9B-R6-plus-G DESIGN          = AUTHOR APPROVED — PUBLISHED
G IMPLEMENTATION                  = AUTHORIZED — EXACT BASE ceb5a7e9
VERIFICATION_CLASS                = INTEGRATED_PHASE — FROZEN BY THE AUTHOR
CLOSEOUT_MODE                     = AUTHOR_OPERATED
VERSION TARGET                    = 1.1.0
VERSION_DATE                      = 2026-10-15
DOCUMENT-REPLACEMENT CHARACTERIZATION   = AUTHORIZED (SC-01 to SC-27)
DOCUMENT-REPLACEMENT PRODUCT CORRECTION = NOT AUTHORIZED
G TECHNICAL APPROVAL              = PENDING AUTHOR REVIEW
PRE-G9B-R6-plus GLOBAL CLOSEOUT   = NOT AUTHORIZED
PRE-G9B-R7                        = NOT AUTHORIZED
G9B                               = NOT AUTHORIZED
PUBLIC RELEASE                    = NOT AUTHORIZED
selfApproved                      = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-R6-plus-G
IMPLEMENTATION AUTHORIZATION" and records the entry gate it requires. It is the first
tracked change of the implementation. It changes no decision of the
[G author design closeout record](pre_g9b_r6_plus_g_author_design_closeout_record.md),
which stays the authority for the approved design. The canonical prompt
[`pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md)
is executed as published at the base. As the instruction allows, its documentary header
is not rewritten to record this authorization. Machine-readable mirror:
[`pre-g9b-r6-plus-g-implementation-authorization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-implementation-authorization.json).

## 1. Author authorization (essentials)

The author authorizes the implementation and technical verification of
`PRE-G9B-R6-plus-G` under the approved canonical prompt and the author-approved design
contract. The authorization is limited to the approved G scope:

| Area | Authorized content |
|---|---|
| A. Product metadata and About | the **AUTHOR APPROVED — DESIGN** metadata contract PM-1 to PM-12 and its normative transcription into the existing Windows packaging and application-profile specifications; no competing metadata specification |
| B. Version transition | mechanism M1; `VERSION = 1.1.0`, `VERSION_DATE = 2026-10-15`; one version-transition commit, the last productive commit before the freeze, introducing together `application.version_date`, package-profile schema `2`, the Gradle guard, provenance schema `3`, the About version-date line, the guide and version-test updates and the compatible packaging changes. Earlier commits keep `1.0.0` without a version-date field. |
| C. Help | the minimal EN/ES help package of the canonical prompt; the selected editorial items: numeric sorting by layer in the Algebra View; the orientation cue in the preferences summary; the Spanish ISO A terminology; the shortcut and guide-window tooltip descriptions. Existing sections and GeoCeDG-owned profile text only. |
| D. Characterization | SC-01 to SC-27 for `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`, through scratch probes or untracked focused tests. **No productive correction.** |
| E. Debt register | the structured G register on the approved 134-entry framework, with the design-closeout dispositions; `OBS-R6P-SAVE-WARNING` stays open |
| F. Documentation | class-1 status corrections; class 2 qualified; classes 3 and 4 retained |

Frozen execution governance:

| Item | Value |
|---|---|
| `VERIFICATION_CLASS` | `INTEGRATED_PHASE` |
| `CLOSEOUT_MODE` | `AUTHOR_OPERATED` |
| Acceptance on the exact frozen candidate | `STATIC`; `PHASE -Phase PRE-G9B-R6-PLUS-G`; `INTEGRATION`; `INFRA_UNIT`; `PACKAGING -CheckToolchain`; `PACKAGING -VerifyPackagingArtifacts` on an INTERNAL set and on an NC set built from the candidate; `COMMERCIAL` refusal evidence with an unchanged output root |
| Also | focused tests, compatibility checks, the verification-inventory refresh |
| `FINAL` | not authorized automatically: a global verification-infrastructure change needs an explicit escalation request; a unified milestone `FINAL` needs a separate author decision |

Not authorized: kernel, geometry, DAG or spatial-serialization changes; inherited source
changes without separate authorization; any document-replacement correction; other
deferred enhancements; new help frameworks; global verification-infrastructure changes
without escalation; `PRE-G9B-R7`, `G9B`, `COMMERCIAL`; public release, binary publication,
redistribution; push, merge, tag or publication; G approval and the global closeout.

## 2. Entry gate (2026-10-14)

| # | Check | Result |
|---|---|---|
| 1 | live remote `main` (`git ls-remote origin refs/heads/main`) | `ceb5a7e9ef8bafcfabb10aeba5162451d88f79c3` |
| 2 | `origin/main`, local `main`, `HEAD` | `ceb5a7e9…`; tree `fa748fd1b87ae16c9f0c248f8f77b94069cc2ea3` |
| 3 | worktree | clean |
| 4 | published design closeout present | `ceb5a7e9` adds the [design closeout record](pre_g9b_r6_plus_g_author_design_closeout_record.md) and its mirror and carries contract revision 2 with the three clarifications and prompt revision 3 (design approved) |
| 5 | hard predecessors are ancestors of the base (`git merge-base --is-ancestor`) | `A-1` `2a71133a`, `A-2` `06a2ea6b`, `B` `848206c4`, `C` `6248bf0f`, `C-X1` `eabe6368`, `D0` `8383a153`, `D1` `6e1d8459`, `E1-L` `5f34d181`, `E1-P` `dcda4075`, `E1-X1` `b12ab32c`, `E1-P-X1` `44b90156`, `E2` `3a724661`, `E3-R1` `7cd50116`, `POST-E3-INTEGRATION` `00b525fd`, `F1` `1cea3c89`, `F2` `488f1e99`, `F3` `2d7b2cae`: all yes |
| 6 | operational predecessor F4 | `F4-A` `6baa66a5`, `F4-D` `6214d15a`, aggregate closeout `19f8f206`: all ancestors |
| 7 | unexpected divergence or unapproved change | none |

Entry gate: **PASS**. No earlier unpublished branch or candidate is substituted for the
base.

## 3. Execution plan

The authorized steps organize one G implementation; they are not roadmap phases.

1. Baseline and dependency verification (§2).
2. Document-replacement characterization (scratch probe, report only).
3. Metadata, About and distribution implementation at `1.0.0`.
4. Bilingual help and documentary reconciliation.
5. Focused tests, inventory, registry and packaging preparation.
6. The atomic version-transition commit, which is also the frozen technical candidate.
7. Acceptance of the exact frozen candidate.
8. Evidence consolidation and the author-review package.

If the characterization demonstrates a contract violation that needs code, the corrective
branch stops for a separate author decision; independent authorized G work continues.

Technical acceptance never implies author approval. This record originates no
publication, release or approval.

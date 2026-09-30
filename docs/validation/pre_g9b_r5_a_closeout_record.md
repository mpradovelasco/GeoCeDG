# PRE-G9B-R5-A closeout record

```text
PRE-G9B-R5-A = PASS — AUTHOR APPROVED
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
`PRE-G9B-R5-A` technical candidate and the exact evidence identities behind it.
It changes no product, test, build, verifier, inventory, registry, schema,
diagnostic, guide, packaged-resource or specification file. It does not modify
the approved technical candidate. Besides this record and its machine-readable
mirror, it only updates the roadmap status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [candidate report](pre_g9b_r5_a_candidate_report.md) and its
[evidence mirror](../../geocedg/validation/pre-g9b-r5-a/pre-g9b-r5-a-candidate-evidence.json)
record only frozen technical state and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`.
This record is the sole authority for current author approval. Every other
statement in those artifacts stands unchanged.

## Author disposition

```text
PRE-G9B-R5-A               = PASS — AUTHOR APPROVED

FINAL TECHNICAL CANDIDATE  = 4804f5057082f6eb233263bf8635e2754875f3cc   (T_R5A)
FINAL TREE                 = 52b24a3d3d433d44f8cd186f925e2b443b12b277
AUTHOR SMOKE               = PASS
AUTHOR APPROVAL            = APPROVED
selfApproved               = false
```

The approval applies to exact `T_R5A` and extends to no other descendant except
this status-only closeout. It authorizes no further implementation.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base `P_R4` | `cfed55fad3f8b3fb5b55954f72fe43fe9ec2ab4e` | `f34d350aa28e5324f184a4d1fb65ac55e0334c4b` | `13d1192b…` |
| technical candidate `T_R5A` (approved) | `4804f5057082f6eb233263bf8635e2754875f3cc` | `52b24a3d3d433d44f8cd186f925e2b443b12b277` | `P_R4` |

Branch: `feature/pre-g9b-r5-a-guide-navigation`. The history is linear and
single-parent, with no merge. The canonical prompt
`.github/prompts/tasks/pre-g9b-r5-a-guide-navigation.prompt.md` (blob
`fd68945b6b95ddd5b9ce3201c29d1fbb06668854`) is unchanged.

## Accepted evidence

The class was frozen at phase start and never escalated:

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = BOUNDED_PHASE
frozenAtPhaseStart  = true
ACCEPTANCE          = PHASE PRE-G9B-R5-A + STATIC
```

Under `geocedg/specs/operations/verification-levels.md` §12.8 `BOUNDED_PHASE`
requires PHASE; the canonical prompt adds `STATIC` for the structural
diagnostic. Both runs are bound to exact `T_R5A` and preserved as executed:

| Profile | Run | Plan hash | Result hash | Result file SHA-256 | Verdict |
|---|---|---|---|---|---|
| `PHASE -Phase PRE-G9B-R5-A` | `verification-a9ffbed6632a47538c601149f4bd0483` | `31a5d90f3f251a4abcc1f74fd03c637d9860970ef0494edca7030708da3f0b04` | `93bc88e0ad9340b2c43e073451aec90138a8ab84b272aaefcd89d658d91266cb` | `48ef79d619a1afaa6e1965b1c0b3d0f00c4f48dc3ef271e861a2d77128a8fb61` | `ACCEPTED / COMPLETE`, 4/4, 0 diagnostics |
| `STATIC` | `verification-407c9036a9db4e0282866c13038d63c3` | `25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52` | `d4242b3e54891189586d1e0eb6692d9d44a242d9dfc32dbe078f946d0f4dd7a0` | `4b08763f4c929fca63f3e7a5d9656fb3aedfd31e0bc5c9023eeb23d1ee2c7427` | `ACCEPTED / COMPLETE`, 3/3 |

PHASE satisfied `compile.shared.semantic`, `compile.desktop.semantic`,
`junit.desktop.pre-g9b-r5-a.semantic` (79/79) and
`infra.guide-structure-diagnostic`. In STATIC, `diagnostic.guide-structure` is
`DIAGNOSTIC_CLEAR`: 17 markers per edition, 99 aligned entries, 0 EN/ES
structural mismatches. The only other STATIC notes are the standing governance
finding and the historical-consistency unavailability that every STATIC run
carries.

PHASE and STATIC emit no FINAL acceptance receipt, so, as in the status-only
closeouts of accepted bounded phases (`PRE-G9B-R1-R1`, `PRE-G9B-R2`,
`PRE-G9B-R4`), no receipt is claimed, fabricated or fed to the FINAL-receipt
identity-only closeout adapter. The run evidence stays locally in the ignored
evidence area (`artifacts/agent/pre-g9b-r5-a-phase-4804f505/` and
`artifacts/agent/pre-g9b-r5-a-static-4804f505/`). No PHASE, FINAL or `GOV-R`
run is made for this closeout; only its static checks run.

## Author smoke

```text
AUTHOR_SMOKE = PASS
```

The accepted author-smoke behaviour:

- the English guide navigation tree renders correctly;
- the Spanish guide navigation tree renders correctly;
- the EN and ES navigation structures correspond;
- tree selection scrolls to the expected section;
- manual scrolling updates the tree selection coherently;
- boundary and end-of-document behaviour is usable;
- no visible synthetic anchors or table of contents appear;
- existing guide rendering remains correct;
- existing help and Input Help behaviour is unaffected.

## Accepted capability

`PRE-G9B-R5-A` closes its planned scope:

- derived stable anchors;
- a derived structural outline;
- a left navigation tree;
- tree → document navigation;
- document → tree scroll synchronization;
- the bilingual structural diagnostic;
- the documentation-maintenance anchor rule.

The preserved architecture:

```text
Markdown EN/ES          = sole authored sources
outline                 = derived presentation data
anchor identity         ≠ translated prose
packaged Markdown       = byte-identical to authored Markdown
```

No hyperlink-listener architecture, no manual table of contents or index
authority and no `R5-B` command-surface work was introduced.

## Documentation-contract disposition

Section 10 of
[`documentation-maintenance.md`](../../geocedg/specs/operations/documentation-maintenance.md),
carried by exact `T_R5A` as candidate normative text, is accepted by this author
decision. Its own condition, normativity through the author's decision on the
exact candidate, is thereby met; its text is not edited or broadened here. The
accepted rule establishes:

```text
navigation identity derives from stable section markers
and numeric subsection structure
translated heading text is display only
manual navigation indexes are forbidden
EN/ES structural alignment is machine-checked
```

## Impact dispositions

```text
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = R5-A changes navigation presentation around the existing
authoritative bilingual guides; their authored contents remain byte-identical
and are explicitly frozen by the phase contract.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, Conda, packaging,
download or build/verification entry point changed.

VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED
Scope: registry, JUnit inventory and static-diagnostic registration only; no
verifier semantics changed; the INTEGRATION, FINAL and INFRA_UNIT plans are
byte-identical to the base.
```

## Observation and debt disposition

None of the items below blocks `PRE-G9B-R5-A`. Each is carried forward
unchanged; none is repaired here.

| Identifier | Disposition |
|---|---|
| `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL` | `RETAINED OBSERVATION`: the structural diagnostic belongs to `STATIC` and its verification-core fixtures to the `PRE-G9B-R5-A` PHASE; they are not added to FINAL in this closeout |
| `OBS-R5A-LATENT-NESTED-LIST-CONTINUATION` | `RETAINED / NON-LIVE`: the current guides contain no nested lists, and R5-A does not redesign nested-list parsing |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged; guide-content correction was explicitly outside R5-A and is not absorbed |

The rest of the latest ledger (the
[`PRE-G9B-R4` closeout](pre_g9b_r4_closeout_record.md)) carries forward
unchanged:

| Identifier | Owner / state |
|---|---|
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | observation, retained |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observation, retained |
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION` | retained observation |
| `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` | retained observation |
| `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY` | retained observation |
| `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | retained outside R4 |
| `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION` | retained outside R4 |

## Promotion

The author authorizes promotion of the linear history
`cfed55fa… → 4804f505… → this closeout commit` to `main`, by ordinary clean
non-force fast-forward only. There is no merge, rebase, squash, amend, force
push, feature-branch push, tag, release or binary publication. Promotion
proceeds only if local `main`, `origin/main` and the live remote `main` still
equal the base below immediately before it.

Remote identity at closeout entry:

```text
local main       = cfed55fad3f8b3fb5b55954f72fe43fe9ec2ab4e
origin/main      = cfed55fad3f8b3fb5b55954f72fe43fe9ec2ab4e
live remote main = cfed55fad3f8b3fb5b55954f72fe43fe9ec2ab4e
tree             = f34d350aa28e5324f184a4d1fb65ac55e0334c4b
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
PRE-G9B-R5-B = DESIGNED — NOT AUTHORIZED   (superficie canónica de comandos en inglés)
BLOCKED_ON   = accepted compatibility ADR implementing/formalizing AD-R0-4
PRE-G9B-R6   = NOT AUTHORIZED
PRE-G9B-R7   = NOT AUTHORIZED
G9B          = NOT AUTHORIZED
```

`AD-R0-4` is an author-decided policy direction, not an accepted ADR; the
compatibility ADR that formalizes it does not yet exist. The next permissible
task is the separately authorized design, review and author acceptance of that
ADR; no `R5-B` source change may start before it is accepted and the phase is
authorized from an exact published base. `G9C`, `G9U2`, productive `G10` and
further `G12` remain unauthorized. No tag or release is authorized.

# Maintenance — locale-independent `Dot` XML serialization: closeout record

```text
DOT/XML MAINTENANCE = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-09-30 on the `Dot`/XML
serialization maintenance candidate and the exact evidence identities behind it. It
changes no product, test, build, verifier, inventory, registry pin, schema,
toolchain, serialization or acceptance-semantics file, and it does not modify the
approved technical candidate. Besides this record and its machine-readable mirror,
it only updates the roadmap status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [candidate report](maintenance_dot_xml_locale_independent_serialization_candidate_report.md)
and its evidence mirror record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole authority
for current author approval. Every statement in those artifacts stands unchanged,
including their pending-author wording.

## Author disposition

```text
DOT/XML MAINTENANCE = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = 4f9bc94440c772388381bd6970ed689a7502564b   (M_DOTXML)
APPROVED_TREE                = 71bf61504719c034a940a8e81fd90a8454d17ce4
AUTHOR_SMOKE                 = PASS
FINAL                        = verification-7e9764bceb534e569510f324d0896ffb,
                               ACCEPTED / COMPLETE, 41/41
FINAL_RECEIPT                = d1de410ffd53e4eb3bac86b201d2f68bf024cd3f8effe5ba16679ca3d0ccef63
AUTHOR_APPROVAL              = APPROVED
selfApproved                 = false
```

The exact candidate is accepted without mutation. The approval applies to that
commit and extends to no other descendant except this status-only closeout.

**Author smoke.** The author reviewed the recommended EN/ES lifecycle smoke and
reported `AUTHOR_SMOKE = PASS`. This record contains no individual smoke
observation beyond that statement. The optional `.ggb` compatibility route is not
recorded as a separate author observation; its evidence is the automated
`DotXmlLocaleIndependentPersistenceTest.spanishCompatibilityDocumentReopensAsTheSameDependency`
inside the accepted `FINAL`.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base `P_R5B_ADR` | `e16697d96858639becf2dc72f7c7beaca8fcbd24` | `3269ce7e29c14e72ec2c52011a350387f276e36a` | `c459de9a…` |
| technical candidate `M_DOTXML` | `4f9bc94440c772388381bd6970ed689a7502564b` | `71bf61504719c034a940a8e81fd90a8454d17ce4` | `P_R5B_ADR` |

Branch: `maintenance/dot-xml-locale-independent-serialization`. The candidate's
artifacts are the
[candidate report](maintenance_dot_xml_locale_independent_serialization_candidate_report.md)
and
[`maintenance-dot-xml-candidate-evidence.json`](../../geocedg/validation/maintenance-dot-xml/maintenance-dot-xml-candidate-evidence.json).

## Accepted content

- **Placement.** The fix is accepted in the shared Java kernel serialization path
  (`ExpressionSerializer`, `case DOT`, non-GIAC branch). No Desktop, presentation
  or reader workaround is part of it.
- **Gating.** The author accepts
  `tpl.isPrintLocalizedCommandNames() ? loc.getCommand("Dot") : "Dot"` for every
  template that declares command names non-localized, not only `xmlTemplate`:
  localizing templates keep the localized `Dot` presentation where it applies;
  non-localizing templates print the stable internal `Dot` head. This restores the
  existing `StringTemplate` contract for `DOT`. It must not be narrowed to an
  XML-specific special case.
- **Presentation.** Spanish user-facing presentation stays localized wherever the
  template requests localization (`ProductoEscalar(u, v)` in the default, edit and
  LaTeX templates). MathML and GIAC are unchanged.
- **Persistence.** New saves are locale-independent for `Dot`: EN and ES write the
  same `<expression label="d" exp="Dot(u, v)"/>` and
  `<expression label="e" exp="(2 * Dot(u, v))"/>`; English output is byte-identical
  to the base.
- **Dependency identity.** After native `.cedg` save/reopen, `.ggb` compatibility
  input, XML reload and undo, the object keeps its `AlgoDependentNumber` parent,
  top operation `DOT` and inputs `{u, v}`, and recomputes when an input changes.
- **Historical files.** Documents already saved with a localized `Dot` head are not
  migrated. No locale-aware XML migration, translation guessing, textual
  replacement heuristic or generalized localized XML parser is authorized.
  `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` is retained historical compatibility debt.
- **Upstream provenance.** Unchanged from the candidate: `ExpressionSerializer.java`
  was upstream-unmodified (baseline `9b93256b7df401ff056c37b502d82df4d72b1522`,
  blob `1fd72720d688b10999df9041a9bdbe49ee6a7e6f`) and is now a registered minimal
  upstream modification; the two new test classes are registered as added
  (824 registered files).

## Accepted evidence

| Field | Value |
|---|---|
| verification class | `GLOBAL_IMPACT`, frozen at track start; one normal `FINAL` |
| `FINAL` run | `verification-7e9764bceb534e569510f324d0896ffb`, `ACCEPTED / COMPLETE`, 41/41 acceptance contracts satisfied, 17/17 producers completed with exit 0 |
| result | hash `b530e266e2d7333432e59e6b068373679c39f06ea8b15fa7c40ecb79ecf4ad0e`, file SHA-256 `4fa9c8414208f8108bd97ee749b01d1a552dfde5d479b4fad535ac65c204e470` |
| plan | `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` |
| receipt | `d1de410ffd53e4eb3bac86b201d2f68bf024cd3f8effe5ba16679ca3d0ccef63`, `GEOCEDG_ACCEPTANCE_RECEIPT`, bound to exact `M_DOTXML` commit and tree; file SHA-256 `a08c0447a4fc380b99e66347e7313022e98217dbc00565ebc24995c3be27f450` |
| receipt identities | checker `e316a6d306e18d2346fbc38949fe77500ff1ce120b721934cfdf13fba21e071b`, input `45686308052f515dc2a06b59d59f8bc209e522997a80c686741fa5dec8ad8cc8`, environment `de567676a3168eb19a13b0b8764c15e6fdca0d97baec6925aac5b72f6c7dc4c0` |

JUnit evidence inside `FINAL`: `final.shared` 6 850 tests and `final.desktop` 1 595
tests, 0 failures or errors; the 10 and 1 skips equal their allowlisted
not-applicable identities; all 11 new regression tests executed and passed.
Diagnostics, separate from acceptance: eight `DIAGNOSTIC_CLEAR`;
`diagnostic.governance` `DIAGNOSTIC_FINDING` and
`diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`, the standing
pre-existing diagnostics.

**Closeout inspection.** `tools/agent/phase-closeout.ps1 -Action INSPECT
-CandidateCommit 4f9bc944… -ReceiptPath <receipt> -ApprovedCommit 4f9bc944…`
returned exit 0, `operation = IDENTITY_ONLY_CLOSEOUT`, `identity_confirmed = true`,
`mismatches = []`, `verification_executed = false`, `repository_mutated = false`.
The run evidence and receipt are kept locally in the ignored evidence area
(`artifacts/agent/maintenance-dotxml-final-4f9bc944/`).

## Closeout verification

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = DOCUMENTATION_STATUS_ONLY
ACCEPTANCE          = STATIC
```

Under `geocedg/specs/operations/verification-levels.md` §12.8,
`DOCUMENTATION_STATUS_ONLY` requires static validation only. `STATIC` emits no FINAL
acceptance receipt; none is claimed for this closeout, and `FINAL` is not rerun for
the unchanged accepted candidate. The `STATIC` run on this closeout commit is
reported with the promotion, because this record cannot name its own commit.

```text
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = author decision and status only; no observable or
  developer-facing contract changed by this closeout.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, Conda,
  packaging, download or build/verification entry point changed.

VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
```

## Debt disposition

| Identifier | State |
|---|---|
| `HZ-R5B-DOT-LOCALIZED-XML` | **RESOLVED — AUTHOR APPROVED** for new saves by the approved candidate; the `R5-B` prerequisite is satisfied once this closeout is published |
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | **RETAINED** historical compatibility debt: documents already saved with a localized `Dot` head are not recovered; no migration is authorized |

The remaining ledger of the
[`R5-B` compatibility ADR closeout](pre_g9b_r5_b_compatibility_adr_closeout_record.md)
and, through it, of the [`PRE-G9B-R5-A` closeout](pre_g9b_r5_a_closeout_record.md)
carries forward unchanged.

## Promotion

The author authorizes promotion of the linear history
`e16697d9… → 4f9bc944… → this closeout commit` to `main`, by ordinary clean
non-force fast-forward only: no merge, rebase, squash, amend, force push, history
rewrite, tag, GitHub release, binary or commercial publication, or unrelated branch
publication. Promotion proceeds only if local `main`, `origin/main` and the live
remote `main` still equal the base below immediately before it.

Remote identity at closeout entry:

```text
local main       = e16697d96858639becf2dc72f7c7beaca8fcbd24
origin/main      = e16697d96858639becf2dc72f7c7beaca8fcbd24
live remote main = e16697d96858639becf2dc72f7c7beaca8fcbd24
tree             = 3269ce7e29c14e72ec2c52011a350387f276e36a
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
DOT/XML MAINTENANCE          = PASS — AUTHOR APPROVED
DOT/XML R5-B PREREQUISITE    = SATISFIED on publication of this closeout
PRE-G9B-R5-B                 = DESIGNED — NOT AUTHORIZED
PRE-G9B-R6                   = NOT AUTHORIZED
PRE-G9B-R7                   = NOT AUTHORIZED
G9B                          = NOT AUTHORIZED
```

Once published, this closeout commit is the only valid candidate base for a future
productive `PRE-G9B-R5-B` authorization. That authorization requires a new explicit
author instruction naming it as `IMPLEMENTATION_BASE`, together with the
governance-layer amendment of the canonical `R5-B` prompt; neither is part of this
closeout. `G9C`, `G9U2`, productive `G10` and further `G12` remain unauthorized. No
tag or release is authorized.

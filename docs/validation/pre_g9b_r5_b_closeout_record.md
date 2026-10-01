# PRE-G9B-R5-B closeout record

```text
PRE-G9B-R5-B = PASS — AUTHOR APPROVED
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
`PRE-G9B-R5-B` canonical English command surface and the exact evidence
identities behind it. It changes no product, test, build, verifier, inventory,
registry, schema, localization, guide, specification, ADR or acceptance-semantics
file, and it does not modify the approved technical candidate. Besides this
record and its machine-readable mirror,
[`pre-g9b-r5-b-closeout.json`](../../geocedg/validation/pre-g9b-r5-b/pre-g9b-r5-b-closeout.json),
it only updates the roadmap status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
three reports and their evidence mirrors record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`:
- the [initial candidate report](pre_g9b_r5_b_candidate_report.md);
- the [R1 corrective report](pre_g9b_r5_b_r1_corrective_candidate_report.md);
- the [R2 corrective report](pre_g9b_r5_b_r2_corrective_candidate_report.md).

This record is the sole authority for current author approval. Every other
statement in those artifacts stands unchanged.

## Author disposition

```text
PRE-G9B-R5-B               = PASS — AUTHOR APPROVED

FINAL TECHNICAL CANDIDATE  = a1d96746a42c5ba7fc83d8909647083f11616009   (T_R5B_R2)
FINAL TREE                 = 61af3ce965cf279924ab44486d93ca87886229e6
PARENT                     = 919adc8e442d0143367063f1fb8dedbb02f8e796   (T_R5B_R1)
AUTHOR SMOKE               = PASS
AUTHOR APPROVAL            = APPROVED
selfApproved               = false
```

The approval applies to exact `T_R5B_R2` and extends to no other descendant except
this status-only closeout. It does not retroactively make the initial candidate
or the first corrective descendant the approved candidate:

```text
INITIAL T_R5B  = 7bc113afb22bb6b97b2ad5e1e3f00117c8022be2
                 TECHNICALLY ACCEPTED
                 SUPERSEDED
                 NOT AUTHOR APPROVED

T_R5B_R1       = 919adc8e442d0143367063f1fb8dedbb02f8e796
                 TECHNICALLY ACCEPTED
                 SUPERSEDED
                 NOT AUTHOR APPROVED

T_R5B_R2       = a1d96746a42c5ba7fc83d8909647083f11616009
                 FINAL TECHNICAL CANDIDATE
                 PASS — AUTHOR APPROVED
```

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published implementation base `P_DOTXML` | `504a19f7e655ef50c4bfa8095a07f94c11b27a01` | `63e7f0cd79aa9e1cf1259b6466e8ac16dc261704` | `4f9bc944…` |
| initial `T_R5B` (superseded) | `7bc113afb22bb6b97b2ad5e1e3f00117c8022be2` | `d47aafdfaace0aa7919f66dde9ff24ec3d1dac65` | `P_DOTXML` |
| corrective `T_R5B_R1` (superseded) | `919adc8e442d0143367063f1fb8dedbb02f8e796` | `c2c3364de6a97b7c7a41d974bfd07791a98da863` | `T_R5B` |
| final corrective `T_R5B_R2` (approved) | `a1d96746a42c5ba7fc83d8909647083f11616009` | `61af3ce965cf279924ab44486d93ca87886229e6` | `T_R5B_R1` |

- **Branch:** `phase/pre-g9b-r5-b-canonical-english-command-surface`, local and never
  pushed.
- **History:** linear and single-parent, with no merge.
- **Unchanged across the chain:**
  - the canonical prompt
    `.github/prompts/tasks/pre-g9b-r5-b-canonical-english-command-surface.prompt.md`
    (blob `43901e3b8fe47f770d791097c5d5ecc6678b36aa`);
  - ADR 0031 (blob `af88ba3865fa4ca94696da7293d26ef8be734ca0`).

## Normative authority

[ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md) is
`ACCEPTED — AUTHOR APPROVED`. Its decision content is `c459de9ab66d61fe849abbf9b9b8097c56605c48`
(tree `3c8d0943f2be7b990e0e4cc97cd114203dd47a49`), and its published closeout is
`e16697d96858639becf2dc72f7c7beaca8fcbd24`
([record](pre_g9b_r5_b_compatibility_adr_closeout_record.md)). The accepted
implementation is interpreted under its Alternative D. The ADR is not reopened.
The published [`Dot`/XML maintenance](maintenance_dot_xml_locale_independent_serialization_closeout_record.md)
remains authoritative for the `Dot` head.

## Accepted evidence

The class was frozen at phase start and never escalated:

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = INTEGRATED_PHASE
frozenAtPhaseStart  = true
ACCEPTANCE LEVEL    = INTEGRATION
```

The technical acceptance evidence for the approved candidate is its own fresh
`INTEGRATION` campaign:

```text
run               = verification-c23ef3bd3b8146f6a7ac64517e469dd1
candidate / tree  = a1d96746a42c5ba7fc83d8909647083f11616009 / 61af3ce965cf279924ab44486d93ca87886229e6
plan              = 4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea
verdict           = ACCEPTED / COMPLETE, 19/19 acceptance checks satisfied
                    (0 violated, 0 untrusted, 0 not run, 17 producers)
result hash       = 87c6cb60d7a881367fd547113e8ff8bef995fa040847fdb885e86dd9766a7699
result file       = SHA-256 d4bbcac2e58f7cee350eb613e60c051a7a4414b918da486466d24c784ceca3eb
final.shared      = 6 850 tests, 0 failures/errors, 10 allowlisted skips
final.desktop     = 1 632 tests, 0 failures/errors, 1 allowlisted skip
g9u1-isolated     = 5/5
diagnostics       = 8 clear; the standing diagnostic.governance FINDING and
                    diagnostic.historical-consistency UNAVAILABLE
```

Additional evidence for the final candidate, recorded before freeze in the
[R2 corrective report](pre_g9b_r5_b_r2_corrective_candidate_report.md):
- the complete Desktop suite: 1 638 tests, 0 failures/errors, 1 skip;
- Checkstyle: 0 findings;
- the upstream boundary: 858 registered files, nothing unregistered;
- `INFRA_UNIT` `verification-d2317c8c746741159ccdff80ae16a8ed`: 22/22;
- `git diff --check`: clean.

Earlier campaigns are preserved as executed, each bound to its own candidate.
None is reattributed:

| Candidate | Run | Result hash | Result file SHA-256 | Verdict | Disposition |
|---|---|---|---|---|---|
| `T_R5B` `7bc113af…` | `INTEGRATION` `verification-cf57f99b1a6f4ebe8398837769747b2f` | `3852d157ffba0c7a0a7ef528adba9e338500a6870bb88b3799056c949ee6fe45` | `bc3636b78ab664e10c713f87fb906912eaff7b6c9c03eda4146d1f1c3c2e13ff` | `ACCEPTED / COMPLETE`, 19/19 | technically accepted, superseded |
| `T_R5B` `7bc113af…` | `STATIC` `verification-dc45cec7497c4993b76e84b0bb3f62db` | `4a22d4ef670f35d7e7e4c36ceccb3c534615a9a83f60c531be293e0b78aa987a` | `9748bd6b3a11dab5d074f9b6626d15cb759b779e2c15f9ad678e72b299e457a8` | `ACCEPTED / COMPLETE`, 3/3 | guide-structure evidence of the initial candidate |
| `T_R5B_R1` `919adc8e…` | `INTEGRATION` `verification-34c0b12566c14f54b45352b354c815c5` | `d3c4a2d9f4d959888e31289591576a40458d74bffba9a765c14b7f2866a8c013` | `0cc1b7ba6eac7eb7ee9f0d7fe907a92050eafde51fcc2011963c8372ce910d9f` | `ACCEPTED / COMPLETE`, 19/19 | technically accepted, superseded |
| `T_R5B_R2` `a1d96746…` | `INTEGRATION` `verification-c23ef3bd3b8146f6a7ac64517e469dd1` | `87c6cb60d7a881367fd547113e8ff8bef995fa040847fdb885e86dd9766a7699` | `d4bbcac2e58f7cee350eb613e60c051a7a4414b918da486466d24c784ceca3eb` | `ACCEPTED / COMPLETE`, 19/19 | accepted technical evidence for the approved candidate |

- **Local evidence:** the run evidence stays locally in the ignored evidence area
  (`artifacts/agent/pre-g9b-r5-b-integration-7bc113af/`,
  `artifacts/agent/pre-g9b-r5-b-static-7bc113af/`,
  `artifacts/agent/pre-g9b-r5-b-r1-integration-919adc8e/`,
  `artifacts/agent/pre-g9b-r5-b-r2-integration-a1d96746/`).
- **No FINAL receipt:** `INTEGRATION` is the accepted campaign for this class and
  emits no FINAL acceptance receipt. As in the status-only closeouts of accepted
  non-FINAL phases (`PRE-G9B-R4`, `PRE-G9B-R5-A`), no receipt is claimed,
  fabricated or fed to the FINAL-receipt identity-only closeout adapter. The
  candidate's identity is recorded directly in this record.
- **No re-run:** no `INTEGRATION`, `FULL` or `GOV-R` run is made for this
  closeout; only its static checks run.

## Author smoke

```text
AUTHOR_SMOKE = PASS
```

The author completed the focused final smoke on `T_R5B_R2` and reports that the
system behaves correctly. Earlier author statements, already recorded in the
corrective authorizations, stand:
- the manual `T-SHADOW` smoke passed on the initial candidate's line;
- the runtime product-language switch in a clean session behaved correctly
  after R1.

No individual manual observation beyond these statements is recorded. The
automated evidence above is the authority for the detailed `T-*` obligations.

## Accepted capability

`PRE-G9B-R5-B` closes its planned scope through `T_R5B_R2`:

- **Canonical command surface.** For every displayable command semantic identity
  `k`, `E(k)` is the canonical English public command name from the English
  command bundle. Internal command identity is unchanged. GeoCeDG displays,
  suggests, inserts and documents command heads in English independently of the
  UI language. Localized names stay compatibility aliases in ordinary `USER`
  input exactly where the existing lookup already accepts them.
- **Semantic separation.** Semantic command identity, the canonical English
  public name, localized compatibility aliases, the persistence vocabulary and
  the presentation context stay distinct. Identity is never inferred from
  displayed text.
- **Syntax.** The canonical English head with the current UI-language syntax body
  and placeholders.
- **Autocomplete.** It shows canonical heads and inserts canonical syntax. It
  accepts localized aliases as search tokens where already supported, and it
  carries semantic identity instead of recovering it from rendered text.
- **Editable re-entry.** Canonical text presented by GeoCeDG keeps its identity on
  redefine. Construction-dependent shadowing fails closed with
  `CANONICAL_HEAD_SHADOWED`, without construction, XML or undo mutation. Fresh
  user input keeps ordinary `USER` semantics.
- **Scripts.** `SCRIPT` runtime lookup is unchanged and GGBScript stays canonical
  English/internal. The editor follows the canonical head policy without
  silently retargeting stored script semantics.
- **XML / persistence.** No XML vocabulary or serialization semantics change. The
  published `Dot`/XML maintenance stays authoritative, and no historical
  migration is introduced.

**R1 correction (accepted).** The product-language chooser enters the host Desktop
language lifecycle, `AppD.setLanguage(locale)`, instead of only changing locale
resources with `setLocale(locale)`. A runtime language change therefore
coherently refreshes labels, Input Help, command dictionaries, aliases, syntax and
the other affected Desktop UI consumers. The host's return to Move on a language
change is accepted as inherited behaviour, not an `R5-B` defect.

**R2 correction (accepted).** The accepted diagnosis is a listener-order chain:
1. a Swing UI refresh changes the document listener order;
2. the caret can then move before the preview listener runs;
3. the caret-based `getCurrentCommand()` reads the wrong edit position;
4. the command error loses its command identity;
5. Input Help focus is not updated.

The fix: under the GeoCeDG profile, preview and error command extraction uses the
edit offset captured by the preview event, with the same exact word resolution.
No fuzzy or approximate matching and no display-derived identity is introduced.
The Classic profile keeps the upstream behaviour.

**Persistence disposition of the R2 finding:**

```text
testCircle3.cedg      = DIAGNOSTIC WITNESS, NOT CORRUPTED PERSISTENCE
R2 defect             = RE-TRIGGERED LOAD/UI-REFRESH LIFECYCLE DEFECT
SERIALIZATION IMPACT  = NONE
```

`testCircle3.cedg` (SHA-256
`af9821c4a38dff49d58d047bd2553d9a912be5fb189bad976316b8abcef02c39`, an ignored,
untracked author file read only from a scratch copy) holds no durable `R5-B` Input
Help or provenance state. Before R2, opening any saved document or otherwise
re-installing the Swing UI reproduced the same pre-existing listener-order
defect. No format migration or new persistent metadata is part of the accepted
correction.

## Observation and debt disposition

None of the items below blocks `PRE-G9B-R5-B`. No debt is marked resolved
merely because the phase closes.

| Identifier | Disposition |
|---|---|
| `TD-DOT-XML-HISTORICAL-LOCALIZED-HEAD` | **RETAINED** |
| native `awt.dll` crash (`OBS-R5B-R1-NATIVE-AWT-CRASH-UNATTRIBUTED`) | **UNATTRIBUTED / NOT REPRODUCED** |
| Classic caret-based Input Help read (`OBS-R5B-R2-CLASSIC-CARET-READ`) | **PRE-EXISTING / OUTSIDE R5-B SCOPE** |
| completion-popup listener reorder (`OBS-R5B-R2-COMPLETIONS-POPUP-LISTENER-ORDER`) | **RETAINED OBSERVATION / NO OBSERVED PRODUCT FAILURE** |
| `HZ-R5B-ALIAS-CAS-STATE` (`Perímetro`) | **RETAINED COMPATIBILITY OBSERVATION**; not worsened |
| `OBS-R5B-R1-LANGUAGE-CHANGE-RETURNS-TO-MOVE` | accepted inherited host behaviour; not an `R5-B` defect |
| `OBS-R5B-R1-SYNTHETIC-INPUT`, `OBS-R5B-R1-CAS-GROUP-READINESS`, `OBS-R5B-R2-EMBEDDED-HOST-ROOT` | retained, unchanged |
| `OBS-R5B-CAS-TABLE-VERBATIM-HEADS`, `OBS-R5B-HARDCODED-SYNTAX-HEADS`, `OBS-R5B-PROVER-DETAILS-CAPTIONS`, `OBS-R5B-INPUT-BAR-INSERTED-TEXT`, `HZ-R5B-HARNESS-TABLE-FRESHNESS` | retained, unchanged |
| `CSolve` / `CSolutions` Algebra syntax | pre-existing, outside `R5-B`; retained (now the typed "no syntax" result, CAS syntax shown) |
| `HZ-R5B-DOT-LOCALIZED-XML` | already **RESOLVED — AUTHOR APPROVED** by the published `Dot`/XML maintenance; unchanged here |
| `HZ-R5B-LABEL-SHADOWING`, `HZ-R5B-MACRO-PRECEDENCE` | the ADR's typed re-entry obligation, discharged by the approved candidate (`T-SHADOW`: label, function-variable and macro shadowing fail closed) |
| `HZ-R5B-IDENTITY-FROM-DISPLAY-TEXT` | the ADR obligation, discharged by the approved candidate (identity-carrying entries; `T-AUTOCOMPLETE`, `T-REDEFINE`) |
| `HZ-R5B-DISPLAY-TEXT-MATCHERS` | the ADR obligation, discharged by the approved candidate (`T-DYNTEXT`) |
| `HZ-R5B-SCRIPT-EDITOR-RETARGET` | the ADR obligation, discharged by the approved candidate (`T-SCRIPT`: untouched scripts save byte-identically) |

The ledger of the [`PRE-G9B-R5-A` closeout](pre_g9b_r5_a_closeout_record.md) carries
forward unchanged, and through it the earlier ones:

| Identifier | Owner / state |
|---|---|
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL`, `OBS-R5A-LATENT-NESTED-LIST-CONTINUATION` | retained |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE`, `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY`, `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS`, `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observations, retained |
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION`, `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY`, `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY`, `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST`, `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION` | retained |

## Closeout classification

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = DOCUMENTATION_STATUS_ONLY
ACCEPTANCE          = STATIC
```

Under `geocedg/specs/operations/verification-levels.md` §12.8,
`DOCUMENTATION_STATUS_ONLY` requires static validation only. This commit's own
`STATIC` run and `git diff --check` are reported with the promotion, because the
record cannot name its own commit.

## Promotion

The author authorizes promotion of the linear history
`504a19f7… → 7bc113af… → 919adc8e… → a1d96746… → this closeout commit` to `main`,
by ordinary clean non-force fast-forward only. Merges, rebases, squashes, amends,
force pushes, history rewrites, working-branch or unrelated-branch pushes, tags,
GitHub releases, and binary or commercial publication are all excluded. Promotion
proceeds only if local `main`, `origin/main` and the live remote `main` still
equal the base below immediately before it.

Remote identity at closeout entry:

```text
local main       = 504a19f7e655ef50c4bfa8095a07f94c11b27a01
origin/main      = 504a19f7e655ef50c4bfa8095a07f94c11b27a01
live remote main = 504a19f7e655ef50c4bfa8095a07f94c11b27a01
tree             = 63e7f0cd79aa9e1cf1259b6466e8ac16dc261704
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. It becomes the only valid implementation
base for a future `PRE-G9B-R6` authorization. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion.

```text
PRE-G9B-R5-B                       = PASS — AUTHOR APPROVED
canonical English command surface  = CLOSED
ADR 0031 implementation            = CLOSED
R5-B runtime language correction R1 = INCORPORATED
R5-B Input Help UI-refresh correction R2 = INCORPORATED
PRE-G9B-R6                         = DESIGNED — NOT AUTHORIZED
PRE-G9B-R7                         = DESIGNED — NOT AUTHORIZED
G9B                                = NOT AUTHORIZED
```

`PRE-G9B-R6` may become the next planned phase. It requires a separate explicit
author authorization naming the exact published closeout commit as its base, and
its prompt is not changed here. `G9C`, `G9U2`, productive `G10` and further `G12`
remain unauthorized. No tag or release is authorized.

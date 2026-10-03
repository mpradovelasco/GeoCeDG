# PRE-G9B-R6-plus-A-2 preparation characterization report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (A-2 canonical prompt)
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PRE-G9B-R6-plus-A-2       = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the characterization encoded in the
canonical
[`A-2` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md)
(sections C1–C12). The prompt holds the resulting contracts and the requested
decisions `DQ-A2-1` to `DQ-A2-11`; this report does not restate them and
carries no authority of its own. The decisions of the preparation instruction
are in the [A-2 author-decision record](pre_g9b_r6_plus_a2_author_decisions_record.md);
the layer decisions are in the
[A author-decision record](pre_g9b_r6_plus_a_author_decisions_record.md). Its
machine-readable mirror is
[`pre-g9b-r6-plus-a2-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-preparation-characterization.json).

## 1. Entry gate

```text
P_R6PLUS_D1          = 2941ddf2f2712340e831d292bdc2687b879850d6
                       tree 158184b150a42e6dcae2946c39620d92cbfcd888
local main           = 2941ddf2f2712340e831d292bdc2687b879850d6 (tree 158184b1…)
origin/main          = 2941ddf2f2712340e831d292bdc2687b879850d6 (tree 158184b1…)
live remote main     = 2941ddf2f2712340e831d292bdc2687b879850d6 (git ls-remote origin refs/heads/main)
worktree             = clean (git status --porcelain=v1: empty), before any tracked edit
D1 state             = PASS — AUTHOR APPROVED — PUBLISHED (AUTHOR_SMOKE = PASS)
```

## 2. Preparation base: the F3 planning reconciliation

The same instruction first required the documentary reconciliation of
`DEBT-R6PLUS-FILE-INSERT-SURFACE`, committed locally, and named that commit as
the exact preparation base of `A-2`:

```text
T_R6PLUS_F3_PLAN = f467eb4563de7a7eb162da1b74954f9f847fa9d3
                   tree   ece51c96117d6cbd55bc200d29c5240686516da9
                   parent 2941ddf2f2712340e831d292bdc2687b879850d6 (P_R6PLUS_D1)
                   branch phase/pre-g9b-r6-plus-f3-planning (local)
```

It changes only documentation: the
[F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md) and
its mirror, the mini-track plan and the roadmap (4.52). `STATIC`
`verification-f2735a0c88ef4a1895f69f1bca4b34fb` on it: exit 0,
`ACCEPTED / COMPLETE`, 3/3 (`prompt.execution-safety`,
`repository.boundary-safety`, `static.scientific-inputs.semantic`), plan hash
`25dfacc8b0c53df285a3ac19bbc438c5868d0c94dad7d1021727287ee892cc52`, result hash
`2a3dc6840c328bdb2b6c765a37fd3d4b7e47c260a97e1aa9531fb48145cbf712`, with the two
standing diagnostics (`diagnostic.governance` `DIAGNOSTIC_FINDING`,
`diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`). Product source is
identical to `P_R6PLUS_D1`, so every citation of the prompt holds at both.

## 3. Merged-branch cleanup

The instruction asked for the inspection, and deletion with `git branch -d`
only, of two local branches before the preparation branch was created. `HEAD`
was on `phase/pre-g9b-r6-plus-f3-planning`. Worktrees: the repository root (on
that branch) and `.claude/worktrees/blissful-bhabha-c66a90` (on
`claude/blissful-bhabha-c66a90`, not touched). No file under `tools/` references
either branch (`git grep`, no match); the tracked mentions are historical
evidence fields of the `D1` records, which pin commit identities, not refs.

| Branch | Tip | Ancestor of `main` | `git log main..<branch>` | Worktree | Retention reason | Result |
|---|---|---|---|---|---|---|
| `phase/pre-g9b-r6-plus-d1-unit-system` | `2941ddf2f` (`P_R6PLUS_D1`) | yes (exit 0) | empty | none | none | `Deleted branch … (was 2941ddf2f)` |
| `phase/pre-g9b-r6-plus-d1-prompt` | `3fb7542af` (`T_R6PLUS_D1_PROMPT`) | yes (exit 0) | empty | none | none | `Deleted branch … (was 3fb7542af)` |

No remote branch was touched (`origin` has no `phase/*` branch). Remaining
local branches: `claude/blissful-bhabha-c66a90`,
`feature/g9u1-construction-workspace-planning`,
`feature/g9u1-construction-workspace-planning-after-r6`, `main`,
`maintenance/retire-orphaned-historical-verification-suites`,
`phase/pre-g9b-r6-plus-f3-planning` and the preparation branch
`phase/pre-g9b-r6-plus-a2-prompt`, created from the exact commit
`f467eb4563de7a7eb162da1b74954f9f847fa9d3`.

## 4. Method

- Static reading of every seam the prompt cites, at the base, with each
  citation re-checked line by line before it was written.
- Scratch-only probes. Their sources and outputs live in the session
  scratchpad; nothing was written under `source/`. The probe directory was
  added to the `common-jre` test source set only through a Gradle init script;
  afterwards `:shared:common-jre:compileTestJava` was run without it, and the
  probe JUnit XML files and the two compile-transaction stash copies were
  deleted. The worktree stayed clean apart from the documentary files of this
  preparation.
- No product, test, build, registry, schema, verifier, specification or ADR
  file changed. No `PHASE`, `INTEGRATION` or `FINAL` ran.

## 5. Probes

Both probe classes ran with `gradlew.bat --init-script <scratch> :shared:common-jre:test --tests 'org.geocedg.common.a2probe.*'`
on the unchanged base (each 1 test, 0 failures, 0 errors). The first run of
`A2PrepLayerXmlProbeTest` had a defect in the probe's own text substitution
(no layer value was inserted, so its XML-read section read only zeros); the
probe was corrected and re-run, and only the corrected run is reported.

| Probe class | Source SHA-256 | Output SHA-256 | JUnit XML SHA-256 |
|---|---|---|---|
| `A2PrepLayerXmlProbeTest` (headless Classic configuration) | `7aedd68ace9217adc77046d69e31b09659492fafd2db7a0135a5588c3813d0fd` | `0eaf3a19477b8850267e331e2049ec0293ae428c710ce3dff6ce2e15bab1285c` | `5767156e492f6660839a19143c36da6d99aa54274d0d9567e295a15e9a6021c1` |
| `A2PrepGeoCeDGConfigLayerProbeTest` (headless `AppConfigGeoCeDG`) | `f40695bc2a3c3c3559f7ab3728190f65be8eb9f233b825257b79865b75340fee` | `084b9a2df673c3d06a04d6f80ea337080ac8c6bb24fa7f90acb6c14dffbe792b` | `fd332e0a6bddb1ee28f4d12a7054a7db490e93fa53f9eb01e39cb4a041d0c2e6` |

### `P-LAYER-SETTER`, `P-LAYER-COMMAND`, `P-LAYER-API` (Classic configuration)

| Input | `setLayer` | `SetLayer(A, n)` | API `setLayer` |
|---|---|---|---|
| −5 | 0 | — | — |
| −1 | 0 | 0 | 0 |
| 0 | 0 | — | — |
| 5 | 5 | 5 | 5 |
| 9 | 9 | — | — |
| 10 | 9 | — | — |
| 50 | 9 | 9 | 9 |
| 99 | 9 | 9 | 9 |
| 100 | 9 | — | — |
| 1000 | 9 | — | — |

### `P-LAYER-XML-READ` (the base as an older reader)

Ten points written with `<layer val>` −5, −1, 0, 5, 9, 10, 50, 99, 100, 1000 and
loaded by a clearing parse: read as 0, 0, 0, 5, 9, 9, 9, 9, 9, 9; highest used
layer 9; the re-saved XML writes 0, 0, 0, 5, 9, 9, 9, 9, 9, 9. Every value
outside `0..9` is lost on re-save.

### `P-UNKNOWN-ELEMENT`

`<geocedgLayers version="1" hidden="2 7"/>` inserted as the first child of
`<geogebra>`, after `</construction>`, inside `<euclidianView>`, inside
`<construction>` and inside `<gui>`: every variant loads with all 10 objects
and logs `unknown tag in <geogebra>` for the two top-level placements and
`unknown tag in <euclidianView>`, `<construction>` or `<gui>` for the others;
the element is absent from the re-saved XML.
`evalXML` of the element raises nothing, logs `unknown tag in <construction>`,
and adds nothing.

### `P-XML-PARTS`

| XML | Top-level children of `<geogebra>` |
|---|---|
| `MyXMLio.getUndoXML` | `euclidianView`, `kernel`, `tableview`, `construction` |
| `MyXMLio.getFullXML` and `App.getXML()` | `gui`, `euclidianView`, `algebraView`, `kernel`, `tableview`, `scripting`, `construction` |
| `MyXMLio.getPreferencesXML` | `gui`, `euclidianView`, `algebraView`, `kernel`, `tableview`, `scripting` |

### `P-GEOCEDG-CONFIG`

With `AppConfigGeoCeDG`: `setLayer` of −1, 9, 10, 50, 99, 100 gives 0, 9, 9, 9,
9, 9; with an object on layer 9 the highest used layer is 9 and a new object
takes 8; `HideLayer(42)` leaves an object on layer 3 visible (silent no-op);
`HideLayer(3)` hides it by writing its own visibility.

## 6. Static characterization summary

The seam-by-seam results, with citations, are prompt C1–C12. The findings that
shape contracts beyond a direct reading of the author decisions:

| # | Finding | Prompt |
|---|---|---|
| 1 | one constant and eight enforcement sites; every app has its configuration before its kernel, and a configuration default method already selects GeoCeDG behavior in shared code | C1, C2 |
| 2 | the Insert File helper and the Open preflight already use the GeoCeDG configuration; Classic uses `AppConfigDefault` | C2 |
| 3 | the base, Classic and GeoCeDG alike, clamps every route and loses values above 9 on re-save | C3 |
| 4 | a holder in `Construction` would be cleared by every undo restore | C4 |
| 5 | the undo XML holds no top-level document element; `<geocedgUnits>` is undoable by design and is no precedent for a non-undoable state | C5 |
| 6 | undo and Open reach the reader with the same load purpose, so commit must be app-level | C6 |
| 7 | `A-1` clears the hidden set after Open, which would wipe every persisted set | C7 |
| 8 | after Open, the `A-1` working-layer rule can pick a hidden layer | C8, `DQ-A2-1` |
| 9 | `B` corrected the Save preview for session state, but no record closes `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | C9 |
| 10 | `App.openEditMacro` copies the part of the document before `<construction>`, so the element goes after `</construction>` | C5, `DQ-A2-3` |
| 11 | an `A-1` toggle never marks the document modified, and `D1` keeps presentation out of save-relevant content | `DQ-A2-2` |
| 12 | the 3D render coding grows with the layer; the Properties combo uses its index as the layer | `DQ-A2-9`, `DQ-A2-11` |
| 13 | the living workspace specification, the developer guide and the user guides state session-only hidden layers and `0..9` | *Contradictions* |
| 14 | the F3 record finds that Apply Template appears to re-layer restyled objects through the default-layer rule that `DQ-A2-7` touches; the author created no dependency, so `A-2` neither absorbs nor depends on it | C1, F3 record finding 7 |

## 7. Obligations and planned tests

The obligation-to-test map is the `T-*` table of the prompt's *Required tests
and commands* (22 identifiers), with the eleven requested decisions `DQ-A2-1`
to `DQ-A2-11` and their default contracts in *Decisions requested before
authorization*.

## 8. Proposed acceptance

`VERIFICATION_CLASS = GLOBAL_IMPACT` (author-accepted, kept), one `FINAL` on one
exact frozen clean candidate after focal, adjacent, catalog and boundary
evidence. At the preparation base the `FINAL` profile resolves to 68 registry
nodes (`required_for_profiles` contains `FINAL` in
`geocedg/specs/operations/verification-registry.json`: 28 acceptance leaves, 19
evidence projections, 17 process producers, 4 diagnostic leaves; 41 nodes of a
blocking acceptance class), including `junit.shared.final` and
`junit.desktop.final`; no phase selection is needed and none is proposed.

## 9. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE (documentary preparation)
SERIALIZATION CHANGE               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT                   — NO CHANGE REQUIRED (no workstation, toolchain,
                                     Gradle, Conda, packaging, download or
                                     environment change)
GUIDE_IMPACT                       = NONE
GUIDE_JUSTIFICATION                = documentary preparation of a phase prompt; no
                                     observable behavior, command, workflow or
                                     file-format contract changes
```

## 10. Changed paths of the preparation

- `.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md` (new)
- `docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md` (new)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-author-decisions.json` (new)
- `docs/validation/pre_g9b_r6_plus_a2_preparation_characterization_report.md` (new, this report)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-preparation-characterization.json` (new)
- `docs/roadmap/geocedg_roadmap.md` (status lines only)
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` (status lines only)

The validation of the prompt contract and the `STATIC` run of the frozen
preparation candidate are reported outside this artifact, because it cannot
name its own commit.

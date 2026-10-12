# PRE-G9B-R6-plus-F3 — File / Insert surface and document insertion compatibility

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F3-PREP` at the author's
instruction of 2026-10-12 ("File / Insert Surface and Document Insertion
Compatibility"), given after `PRE-G9B-R6-plus-F2` and its corrective `F2-R1`
were closed and published. That instruction authorized characterization,
design reconciliation, author-decision preparation and this prompt only. It
stated that **F3 implementation is not authorized** and that no preparation
finding becomes an approved product requirement without author authorization.
The author planning decisions `F3-D1` to `F3-D6` of 2026-10-03
([F3 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f3_author_decisions_record.md))
govern the assignment and the author-fixed scope. The evidence is the
[F3 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f3_preparation_characterization_report.md)
(`K0`–`K15`) and its JSON mirror; the proposed design, the alternatives and the
test matrix are in the
[reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md);
the questions, alternatives and recommendations are in the
[author-decision preparation record](../../../docs/validation/pre_g9b_r6_plus_f3_author_decision_preparation_record.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F3` and the exact prepared candidate or base, and that
disposes of `DQ-F3-1` to `DQ-F3-12`. That instruction may authorize, as the
first tracked edit of the phase, an amendment of this prompt to the authorized
state, following the `F1` and `F2` precedents. This file is an execution
contract, not a second policy document: the unit contract is stated once in
`geocedg/specs/units/unit-system.md` (§8.8, §11, §12, §17.2); the verification
classes once in `geocedg/specs/operations/verification-levels.md` §12.8; the
route and lifecycle evidence once in the characterization report; the proposed
behavior once in the reconciled design, whose section numbers are cited below.
Where this prompt names a design value that no durable specification holds yet,
the value is a proposal until the author disposes of the matching `DQ-F3-*`,
and the first deliverable of `F3` turns it into the durable specification named
under *Required design/specification*.

```text
PRE-G9B-R6-plus-F3 =
PLANNED — AUTHOR APPROVED (2026-10-03, F3-D1 to F3-D6)
PREPARED — NOT AUTHORIZED

PREPARATION ACTIVITY     = PRE-G9B-R6-plus-F3-PREP (DOCUMENTATION_STATUS_ONLY)
CHARACTERIZATION         = COMPLETE (2026-10-12, on P_F2_F2R1_CLOSEOUT e70718fe)
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP PRODUCT SURFACE AND ORCHESTRATION OF EXISTING
                           DOCUMENT-CONSUMING OPERATIONS (File → Insert menu,
                           GeoCeDG chooser policy, orchestration of the unchanged
                           AppD.insertFile and AppD.applyTemplate); no kernel,
                           command, algorithm, serialization, parser or
                           unit-semantics change
DEBT                     = DEBT-R6PLUS-FILE-INSERT-SURFACE (closes with the author
                           approval of F3)
DEPENDS_ON               = PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED — PUBLISHED (hard, F3-D5)
                           PRE-G9B-R6-plus-F2 / F2-R1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (operational predecessor, F3-D6)
GLOBAL GATE              = closeout before the global PRE-G9B-R6-plus closeout (in G)
PLANNED CLASS            = INTEGRATED_PHASE (F3-D4) — NOT FROZEN; frozen at authorization
PRECONDITION             = author dispositions of DQ-F3-1 to DQ-F3-12
NEXT_SUBPHASE            = none implied; F4, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-fixed `F3` scope (`F3-D2`; design §1), subject to the
`DQ-F3` dispositions:

1. **File → Insert / Archivo → Insertar** (design §3; `DQ-F3-1`) holding
   **Insert File / Insertar archivo** and **Apply Template / Aplicar
   plantilla** as independent profile actions; both removed from the GeoCeDG
   Open dialog (design §4; `DQ-F3-2`).
2. **`.cedg` and `.ggb` only** for both operations, with the selection,
   default-extension and refusal rules of `DQ-F3-3` (design §5.1).
3. **OFF not offered or reachable in the GeoCeDG Open UI** per `DQ-F3-4`
   (design §4); the internal upstream OFF support unchanged.
4. **Insert File** keeps its import semantics through the unchanged
   `AppD.insertFile`, keeps the `D1` provenance and notice, never converts
   coordinates, and, per `DQ-F3-5`–`DQ-F3-7`, is one undo step on success and
   no mutation on Cancel or failure (design §5).
5. **Apply Template** keeps its template semantics through the unchanged
   `AppD.applyTemplate`, imports no geometry, never touches the target
   `UnitState`, and per `DQ-F3-8`–`DQ-F3-9` keeps object layers and is one undo
   step (design §6).
6. The helper effects of `DQ-F3-10`, the compatibility of `DQ-F3-11`, the
   tests of design §11 and the durable specification of *Required
   design/specification*.

### Author-fixed requirements and open questions

| Id | Subject | Recommendation (pending) |
|---|---|---|
| `DQ-F3-1` | submenu and action identities | group `file-insert`; `document.insert-file`, `document.apply-template`; `geocedg.document.*` targets |
| `DQ-F3-2` | chooser architecture | GeoCeDG-owned Open override and insertion chooser; no inherited change |
| `DQ-F3-3` | formats and fallback | one `.cedg`+`.ggb` filter; stem → `.cedg`, `.ggb`; refuse others; single selection |
| `DQ-F3-4` | OFF | no filter; refuse explicit and fallback `.off` with a message |
| `DQ-F3-5` | labels and macros | inherited Rename/Overwrite/Cancel; fail closed on conflicting macro definitions |
| `DQ-F3-6` | undo, cancel, failure | one undo point per success; nothing on Cancel or failure |
| `DQ-F3-7` | `D1` | mechanism unchanged; end-to-end tests |
| `DQ-F3-8` | template invariants and state | inherited style semantics documented; one undo point; unsaved |
| `DQ-F3-9` | template and layers | preserve each object's layer |
| `DQ-F3-10` | helper and clipboard | no recent-list publication; registrations released; `CF-F3-7` recorded outside F3 |
| `DQ-F3-11` | Classic and fallback | Classic unchanged; fallback keeps the inherited dialog |
| `DQ-F3-12` | class | `INTEGRATED_PHASE`: registered `PHASE` + `INTEGRATION` |

<!-- geocedg-field: implementation_base -->
## Implementation base

Prepared on, and to be authorized on an exact descendant named by the author:

```text
P_F2_F2R1_CLOSEOUT    = e70718fec3612814144cd405d46f6c08864ceadd
                        (published F2/F2-R1 closeout,
                        docs/validation/pre_g9b_r6_plus_f2_f2r1_closeout_record.md)
tree                  = 26817bfc6a72b378e4babc1cd08efd0d734473c9
preparation candidate = the local commit of phase/pre-g9b-r6-plus-f3-prep
                        that contains this file (documentation only)
```

Before any tracked change the agent reads `AGENTS.md` and every applicable
nested instruction; verifies that local `main`, `origin/main` and the live
remote `main` resolve to the authorized base (or that the authorized
preparation candidate is its only delta); requires a clean worktree; confirms
`D1` and `F2`/`F2-R1` published and `F3` not implemented; creates a local phase
branch from the authorized base; and re-establishes the cited seams of report
`K3`–`K9`. Any difference stops the work with a report.

**Implementation entry gate** (before product code, recorded in the candidate
report): `OTI-F3-1` (pre-validation plus empty-source refusal covers every
failure path of report `K5` C8), `OTI-F3-2` (macro-collision guard before any
target mutation without an inherited change, if `DQ-F3-5` M-B is chosen),
`OTI-F3-3` (release of the helper's registrations without touching `AppD`),
and the freezing of `INTEGRATED_PHASE` with the registered nodes. A failed gate
item stops the work for author disposition; it is never worked around by an
unauthorized inherited change.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` and `verification-levels.md`; the approved `A-1`/`A-2` layer
contracts and the `D1` decisions; the author decisions `F3-D1` to `F3-D6` and
the dispositions of `DQ-F3-1` to `DQ-F3-12`; this prompt; then the reconciled
design candidate, the characterization report and its JSON mirror; then the
2026-10-03 characterization of the F3 decision record and older evidence. Probe
outputs, reports and screenshots are evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Paths are the expected ones (design §10); the implementation re-establishes
each at its base. All under the `DQ-F3` dispositions.

**Product profile.** `apps/geocedg/application-profile.yml`: the
`file-insert` presentation group, its File menu entry, the two actions, their
cluster placement and EN/ES `localized_text` (design §3), and the localized
refusal and status texts the orchestration needs.

**Desktop (`source/desktop/desktop`), GeoCeDG-owned code only.**

- `GeoCeDGActionRegistry`: the two targets (allow-list and `execute`).
- `GeoCeDGProfile`: the catalog pin and its message.
- A new orchestration class (proposed `GeoCeDGDocumentInsertion`) for both
  operations: chooser, name resolution, pre-validation, macro guard if chosen,
  undo point, layer preservation, helper release (design §5, §6, §8).
- `GeoCeDGDocumentPolicy`: insertion extensions and Open-name resolution
  without `.off`.
- `GuiManagerGeoCeDG`: overrides of `openFile(File)`,
  `doOpenFiles(File[], boolean)` and `showRenamedObjectsDialog`, gated off in
  the v1 fallback (design §4, §5.4, §9).
- `GeoCeDGCopyPaste`: the `insertFrom` override and its outcome (design §5.4).
- `AppGeoCeDG`: the helper flag and reference and the helper `setCurrentFile`
  override (design §8).
- Desktop tests of design §11 and the amendment of the pinned tests of report
  `K3` (catalog size, File flatness).

**Documentation and verification.**

- The durable specification of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- `tools/agent/workspace-profile-validation.ps1`: the phase-local
  `PRE-G9B-R6-plus-F3` amendment (two action ids) and its message; the
  re-pins in `geocedg/specs/operations/verification-static-contracts.json` and
  the `static_contracts` pin of `verification-registry.json` through
  `Get-VerificationCanonicalTextSha256`.
- The phase registration `PRE-G9B-R6-PLUS-F3` (selection, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, through the official mechanism.
- The EN and ES user guides (File table, File → Insert, the `D1` notice
  sentence, the OFF note, Apply Template semantics) and their outline pins, if
  touched.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**` (kernel, `ConstructionDefaults`,
  `TemplateHelper`, `CopyPaste`, `InternalClipboard`, XML handlers, parsers,
  unit system).
- Any change of an inherited Desktop class (`GuiManagerD`, `AppD`,
  `CopyPasteD`, `FileMenuD`, `FileDropTargetListener`, `GeoGebraFileChooser`,
  `DocumentArchivePreflight`), unless the author dispositions explicitly
  authorize a named inherited seam (`DQ-F3-2` A2, `DQ-F3-10` P2 or an
  `OTI-F3-2` seam); an unauthorized need is an authorization blocker.
- A new import or copy/paste algorithm, a GeoCeDG copy of `AppD.insertFile` or
  `AppD.applyTemplate`, a migration framework, other formats, 3D import.
- Unit conversion, normalization or scaling of coordinates; applying the
  template's units; a second provenance mechanism; unit logic in the GUI.
- Document-format, XML, serialization, parser or preference-format changes;
  persistent identity changes; label-based identity inference by product code.
- Removing or rewriting OFF support (`AppD.loadOffFile`,
  `GuiManagerD.loadOffFile`, `OFFHandler`).
- Changes to Classic behavior or to global recent-file behavior beyond the
  helper rule of `DQ-F3-10`.
- New geometric commands, modes or command IDs.
- `F4`, `G`, `PRE-G9B-R7`, `G9B`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than an authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop product and application layer only (`AGENTS.md` §4): a GUI surface,
a product profile and the orchestration of existing document-consuming
operations. Geometry, construction dependencies, object identity, macro
semantics, document loading and parsing, unit authority and construction
defaults stay in the shared kernel and document services, unchanged (report
`K12`). Export adapters, packaging and Python: unchanged.

## Required design/specification

First deliverable of the phase, before product code:
`geocedg/specs/ui/document-insertion.md` (proposed path) — the durable
contract of the File → Insert surface, the Open-route extension policy and the
OFF refusal, the Insert File and Apply Template orchestration (selection,
pre-validation, outcomes, undo and saved state, layer preservation, helper
rules), the `D1` and `A-1`/`A-2` interactions and the documented inherited
semantics (label dialog, macro import, template resets), from the reconciled
design and the author dispositions, status `PROPOSED` until the author approves
it. No ADR is expected; one is required if a disposition introduces an
inherited seam.

## Geometric invariants and degeneracies

- Design §2 invariants 1–10.
- Inserted objects keep the algorithm classes, inputs and values
  `CopyPasteD.insertFrom` produces today; the only permitted refusal of a
  semantic substitution is the `DQ-F3-5` macro guard.
- Coordinates and the target `UnitState` are byte-identical before and after
  either operation; the drawing scale is unchanged.
- Apply Template creates and removes no object and changes no dependency.

## Compatibility and serialization

- Documents written after `F3` have the same format; no new element or
  attribute.
- Classic and the v1 fallback keep the inherited Open dialog, its filters and
  OFF.
- Ordinary GeoCeDG Open of `.cedg`, `.ggb`, `.ggt`, `.html`, `.htm` is
  unchanged in behavior.
- Rollback: reverting the phase commits restores the base; no migration.

## Integration obligations

The candidate must show, on one commit, that the File surface and its pins
cross the profile compiler, the menu bar and the registry; that Insert File
crosses the helper load, the copy buffer and the `D1` provenance and notice;
that Apply Template crosses the `A-1` working layer and the `A-2` domain and
hidden set; and that ordinary Open and the document-lifecycle suites are
unchanged. These are the concrete integration facts that select `INTEGRATION`
under `INTEGRATED_PHASE`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The obligations are design §11: `T-F3-MENU`, `T-F3-L10N`, `T-F3-CLASSIC`,
`T-F3-FALLBACK`, `T-F3-OPEN-UNCHANGED`, `T-F3-INSERT-CEDG`,
`T-F3-INSERT-GGB`, `T-F3-TEMPLATE-CEDG`, `T-F3-TEMPLATE-GGB`, `T-F3-LABELS`,
`T-F3-MACROS`, `T-F3-REPEAT`, `T-F3-DEPENDENCIES`, `T-F3-UNDO`,
`T-F3-CANCEL`, `T-F3-FAILURES`, `T-F3-ARCHIVES`, `T-F3-D1-PROVENANCE`,
`T-F3-NO-UNIT-CONVERSION`, `T-F3-TEMPLATE-UNITS`,
`T-F3-TEMPLATE-NO-GEOMETRY`, `T-F3-TEMPLATE-LAYERS`, `T-F3-TEMPLATE-STATE`,
`T-F3-HELPER`, `T-F3-CLIPBOARD`, `T-F3-OFF`, `T-F3-PINS`, `T-F3-GUIDES`, and
the author smoke `T-SMOKE` (not performed by the agent). Chooser tests drive the
real chooser on the EDT; undo assertions use the synchronous undo baseline and
store listeners; Desktop hosts use `G9U1TestApp.Lifecycle`, the per-test
Desktop logger and isolated preferences; dialogs are answered through their own
components or a thread-local `JOptionPane` mock.

Commands (PowerShell; log directories fresh, under `artifacts\agent\`; console
output in the session scratchpad):

```powershell
.\gradlew.bat :desktop:desktop:test --tests <focused F3 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F3 -LogDirectory artifacts\agent\<fresh>
.\tools\agent\verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-F3` covers at least
`compile.desktop.semantic`, `workspace-profile.product` and
`junit.desktop.pre-g9b-r6-plus-f3.semantic`. `PHASE` and `INTEGRATION` run once
each on the frozen clean commit and tree. No `FINAL` under `INTEGRATED_PHASE`;
a run above the frozen class fails with `VERIFICATION_ESCALATION_REQUEST` and
waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`F3` on an exact base with the `DQ-F3` dispositions; it then authorizes local
implementation on a local branch, local commits, focused tests, the phase
registration, the validator amendment and their official catalog updates,
development `INFRA_UNIT` and `STATIC` runs, the frozen acceptance runs, and one
frozen technical candidate for author review and author smoke. It never
authorizes self-approval, author smoke by the agent, a run above the frozen
class, a change of the class, or an inherited-class change not named by a
disposition; a proven inability of the frozen class or of the product-owned
seams to cover the change is reported as a reclassification or seam proposal,
never applied silently.

`F4`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does every open
observation not named here (the pre-existing anomalies `CF-F3-*` keep the
dispositions the author gives them). Author approval is never created by
technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication are forbidden; each needs a separate
explicit author instruction naming the exact candidate SHA. Acceptance evidence
never grants publication authority.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-F3      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class: PHASE + INTEGRATION)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval and for closing
`DEBT-R6PLUS-FILE-INSERT-SURFACE`. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification; the profile, Desktop, validator, registry and guide
  deltas with their tests.
- A candidate report with the entry gate (including `OTI-F3-1` to `OTI-F3-3`),
  report `K3`–`K10` re-established with every correction, the `DQ-F3`
  dispositions applied, the `CF-F3-*` dispositions, test evidence, the exact
  commands, exit codes, report paths and verdicts, and the author-smoke
  checklist of design §12 in EN and ES.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- a target needs a change in `source/shared/**`, in serialization, parsing or
  unit semantics, or in `TemplateHelper` or `ConstructionDefaults`;
- an inherited Desktop class must change and no disposition names that seam;
- the `D1` invariants (no coordinate scaling, target `UnitState` unchanged,
  provenance through the existing buffer, the approved notice policy) cannot be
  kept by the orchestration;
- a failure path of report `K5` C8 can still mutate the target or re-paste the
  previous buffer;
- the macro guard of `DQ-F3-5` M-B (if chosen) cannot run before any target
  mutation;
- Apply Template layer preservation would need the `A-1` seam changed;
- the helper cannot be kept off the recent-file list or released without an
  inherited change (`DQ-F3-10`);
- the frozen class cannot cover a required change.

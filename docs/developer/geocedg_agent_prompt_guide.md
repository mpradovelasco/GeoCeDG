# GeoCeDG agent prompt usage guide

- Status: current operational guide
- Productive knowledge-bundle tooling: **PASS — AUTHOR APPROVED**

## Authority

Start with `AGENTS.md`, then current source/tests/serialization, accepted specs
and ADRs, canonical models/evidence, pinned upstream authority, scientific
references, generated reports and finally conversation history. A prompt scopes
work; it does not supersede geometric truth.

Canonical prompts live in `.github/prompts/`. Short launch profiles under
`ai-shell/prompts/` reference those authorities and must not duplicate them.

Discover prompts from disk rather than memory:

```powershell
Get-ChildItem .github\prompts -Recurse -Filter *.prompt.md
Select-String -Path .github\prompts\**\*.prompt.md -Pattern "# Objective"
```

Choose the prompt whose entry gate and architectural layer match the task; a
similarly named roadmap or generated report is not a substitute.

## New task startup

1. Read `AGENTS.md` and any nested instruction file.
2. Read the named task prompt completely.
3. Inspect `git status`, branch/HEAD, upstream baseline and cited sources.
4. Classify the task: planning, implementation, review, debugging, validation or
   documentation.
5. Identify source authority, generated evidence, smallest edit set and focused
   verifier.
6. If the task can close a phase, freeze its impact-based
   `VERIFICATION_CLASS` and planned acceptance level, then identify a
   declarative policy that supports
   both `VERIFIED` and `AUTHOR_OPERATED`. READINESS validates both without
   selecting either; select exactly one mode only in the post-review,
   post-approval PREPARE chain, never from branch, credentials or repository
   state.
7. Never escalate above the frozen plan by prudence. Emit
   `VERIFICATION_ESCALATION_REQUEST` for a concrete uncovered obligation and
   wait for author authorization. In a single-integrator context, explicitly
   default to `BOUNDED_PHASE` and propose `AUTHOR_OPERATED` without inferring
   approval.
8. Stop when an entry gate, source, approval, closeout route or semantic policy
   is missing.

Concise launch examples:

```text
Execute .github/prompts/tasks/g8c2-locus-v2-locus-intersections.prompt.md
Review the current change with .github/prompts/reviews/change-review.prompt.md
Run the repository verification contract from .github/prompts/canonical/verification.prompt.md
```

Do not claim that a checked-in prompt or nonexistent tool has run. Authorization
and execution are separate states.

## Canonical LF hashes

Prompt/evidence hashes use strict UTF-8 content with optional BOM removed and
CRLF/CR normalized to LF. Hash bytes, not PowerShell line-pipeline output.
Historical manifests and targets are read from their fixed tag blobs; living
files are assessed separately.

## Branches, phases and review

- Confirm the exact entry commit/tag and require ancestry before work.
- Characterization phases are read-only with respect to productive source.
- Implementation phases require explicit authorization and an approved design.
- Development and diagnostic review candidates may remain uncommitted. Final
  acceptance may not: create a clean technical commit `T`, run
  mode-neutral `CLOSEOUT_READINESS` for both routes, compare the real level
  plans, and bind one canonical FULL campaign with authenticated
  PHASE/COMPOSED/FULL claims to that receipt.
- Author approval is a separate state from tests passing.
- `AUTHOR REVIEW READY` means only prepared for author review. `AUTHOR CLOSEOUT
  READY` additionally requires clean exact `T`, consumable acceptance evidence,
  one explicitly selected post-approval route and a PREPARE-valid binding to
  the mode-neutral readiness plan. Readiness never creates approval or chooses
  a mode.
- After explicit approval of exact `T`, `VERIFIED` retains the verified
  status-only commit/tag/fast-forward path. `AUTHOR_OPERATED` preparation stages
  only the projected closeout delta and stops; the author personally commits,
  tags, fast-forwards and pushes, followed by automatic audit.
- Never force-push or rewrite shared history. Never let tooling silently perform
  a Git operation reserved to the selected author-operated mode.
- Create the next phase branch only from its approved entry gate.

For every future phase, read three separate fields rather than interpreting a
roadmap arrow as universal authority:

1. **hard semantic/contract dependencies** — required for a meaningful safe
   result;
2. **recommended execution predecessors** — scheduling/reproducibility advice;
3. **global/release gates** — evidence combined only at product closeout.

G9P-R1 specifically makes G9O1 operational-first but not a semantic dependency
of G9A1, removes G9U1 as a prerequisite of G9B, and treats U0-before-X1 as the
recommended integration order rather than an X1 semantic requirement. Prompts
must preserve those distinctions.

The frozen G8 evidence anchor is annotated tag object
`fed1bfbeea77a48acce285429b397eda77054df1`, peeled commit
`e7810171179825a03b22d8c6eba28c672f468281`. Current `HEAD` must descend from
the peeled commit when reproducing that evidence.

## Environment and evidence

Classify failures before editing: toolchain/import origin, Windows sandbox,
stale test/API mismatch, missing source, unavailable runtime, production
regression or scientific/modeling issue. Use managed escalation for a genuine
sandbox restriction; do not patch production code around it.

Run repository wrappers under `tools/agent/`, retain command, exit code and log
path, and distinguish static/fake-first/skipped/real-runtime evidence. Generated
logs/build outputs remain evidence, not source.

For final acceptance, first run
`tools/agent/phase-closeout.ps1 -Action READINESS` with exact `T`, policy,
and receipt path, without `-CloseoutMode`; then pass the resulting receipt to
one `verify.ps1 -Level FULL -CleanBuild` invocation through
`-CloseoutReadinessPath`. Its same-run envelope must truthfully distinguish
physically nested PHASE, plan-subsumed COMPOSED and physical-root FULL claims.
Run a lower standalone level only for a concrete uncovered obligation reported
by readiness. Without the receipt the result is explicitly
`DEVELOPMENT_DIAGNOSTIC`, even if clean. A dirty successful run remains
`closeoutConsumable=false`; never reattribute it after commit.

After review/smoke and exact-SHA approval, select one mode explicitly and use
`phase-closeout.ps1 -Action PREPARE -CloseoutMode <mode>`. In
`AUTHOR_OPERATED`, stop for the author's commit/tag/fast-forward/push and run
`-Action AUDIT` after publication. An audit failure is evidence of a failed
promotion, not permission to move refs, reinterpret receipts or rerun fewer
technical gates. Use the exact interface and evidence classification in
[`verification-levels.md` section 12](../../geocedg/specs/operations/verification-levels.md#12-commit-first-acceptance-and-dual-closeout).

## Stop conditions

Stop rather than guess for unclear licensing, missing cited sources,
non-reproducible baseline, unapproved serialization changes, nondeterminism,
unresolved geometric ambiguity, or a proposal that moves truth into GUI,
exporter, script or generated output.

## Avoid duplicated contracts

Prompts reference specs/ADRs and list scope, gates and artifacts. They do not
copy mathematical definitions. Guides explain how to operate prompts. Reports
record what happened. When a living guide changes, do not rewrite historical
evidence manifests.

Closable phase prompts declare policy data consumed by the generic
`tools/agent/closeout-workflow.ps1` helper; they do not clone readiness,
preparation or audit logic into one verifier per phase. Preserve the G9U1
precommit receipts, later lifecycle normalization and published closeout as
separate historical cohorts. ADR 0024 section 11.2 remains exceptional and
cannot waive FULL for a deliberate verification-methodology change.

When maintaining the G9 prompts, reference the approved projection-system,
redefine and one-dimensional-generator contracts instead of copying their
equations. Recompute the canonical-LF hash only for a prompt whose bytes
changed, update current evidence, and leave every future prompt explicitly
unexecuted. The canonical G9O1 prompt has been executed and closed as **PASS —
AUTHOR APPROVED**. G9A1 is authorized but remains unexecuted; the other eight
future prompts remain unauthorized and unexecuted. Frozen G9P prompt hashes and
integrity data are read from `geocedg-g9p-pass`, not rewritten.

## Knowledge bundles

The normative bundle contract provides bounded reading sets through declared
profiles while preserving repository file boundaries and provenance. From a
clean tree, run:

```powershell
.\tools\knowledge\build-knowledge-bundle.ps1 -Profile operational
.\tools\knowledge\verify-knowledge-bundle.ps1 `
  -BundleDirectory artifacts\knowledge\operational-<bundle-id>
```

Use the exact directory printed by the generator. Default generation rejects a
dirty tree. `-AllowDirty` is diagnostic-only: it records staged, unstaged and
untracked hashes, labels the manifest `NON_RELEASE_EVIDENCE`, and does not make
the artifact releasable. Never commit anything below `artifacts/knowledge/`.

The `source` and `knowledge` profiles cover broad source/governance context;
thematic profiles cover Locus V2, governance, frontend/DXF, spatial G9 and
operations. Restricted binaries, generated outputs, caches, secrets and
unregistered upstream modifications fail closed. Verify the implementation and
fixtures with:

```powershell
.\tools\agent\verify-knowledge-bundles.ps1
```

Bundles are generated evidence, not authority. Follow every `FILE:` path back
to the repository before editing. G9O1 is **PASS — AUTHOR APPROVED**. G9A1 is
authorized but not started, and no productive spatial G9 implementation has
started.

```text
PRODUCT_PHASE_EFFECT = NONE
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: commit-first evidence classification and closeout Git checks add no
PowerShell, Git, JDK, Gradle, Conda, packaging or workstation prerequisite.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md;
  docs/developer/geocedg_agent_prompt_guide.md
```

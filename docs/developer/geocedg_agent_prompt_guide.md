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
6. If the task can reach final acceptance, identify the required pure profile
   coverage, freeze the exact candidate commit and resolve `FINAL -PlanOnly`
   before requesting one real FINAL authorization.
7. Keep acceptance, author approval and publication as separate decisions.
   A receipt records accepted evidence for one immutable Git identity; it does
   not grant approval or permission to publish.
8. Stop when an entry identity, source, required coverage, receipt, explicit
   approval or publication boundary is missing.

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
  acceptance may not: create a clean immutable candidate, resolve a complete
  FINAL plan and bind one authorized FINAL campaign and receipt to its exact
  commit/tree, plan, checker, environment, inputs and evidence.
- Author approval is a separate state from tests passing.
- A candidate pending author review has no publication authority. After exact
  SHA approval, closeout only validates receipt and Git identity; promotion is
  a separate explicitly authorized operation.
- Never force-push or rewrite shared history. Never let tooling silently perform
  a Git operation during closeout identity inspection.
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

For final acceptance, freeze the clean candidate and run one authorized
`tools/agent/verify.ps1 -Profile FINAL -LogDirectory <new-external-root>`.
Require accepted semantics and safety, complete trusted coverage, a valid report
and the canonical immutable receipt. Never reinterpret diagnostic findings as
acceptance failures and never reattribute evidence to a different commit.

After review and exact-SHA approval, use
`phase-closeout.ps1 -Action INSPECT -CandidateCommit <sha> -ReceiptPath
<receipt> -ApprovedCommit <sha>`. The adapter validates identity only. It never
runs verification, repeats FINAL, modifies Git or publishes. Recheck the live
remote separately before any explicitly authorized non-force fast-forward.

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

Current prompts name pure profiles, exact Git identities, evidence and
authorization boundaries; they do not clone registry or receipt logic.
Preserve G9U1 precommit receipts, later lifecycle normalization and published
closeout as separate historical cohorts. Historical lifecycle mechanisms remain
evidence, not executable authority for a new candidate.

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

# GeoCeDG agent prompt usage guide

- Status: current operational guide
- Productive knowledge-bundle tooling: absent; G9O1 is authorized and not started

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
6. Stop when an entry gate, source, approval or semantic policy is missing.

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
- Review candidates may remain uncommitted; do not create a commit merely to
  make evidence convenient.
- Author approval is a separate state from tests passing.
- After approval, closeout may commit/tag and fast-forward the intended branch;
  never force-push or rewrite shared history.
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

When maintaining the G9 prompts, reference the approved projection-system,
redefine and one-dimensional-generator contracts instead of copying their
equations. Recompute the canonical-LF hash only for a prompt whose bytes
changed, update the catalog and current integrity manifest, and leave every
future prompt explicitly unexecuted. G9O1 is the sole authorized future prompt
after G9P closeout; the other nine remain unauthorized, and none has run.

## Knowledge bundles

The normative bundle contract can provide bounded reading sets after G9O1 is
executed. G9O1 is authorized but not started; no generator or
`verify-knowledge-bundles.ps1` exists yet. Until that canonical prompt is
explicitly invoked and implemented, discover files directly from the repository
and follow the normal authority hierarchy.

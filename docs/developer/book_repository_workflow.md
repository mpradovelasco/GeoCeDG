# External GeoCeDG book repository workflow

## Authority boundary

GeoCeDG and its future book use independent Git repositories. The boundary is
architectural, not merely a directory convention.

| Repository | Owns | Does not own |
|---|---|---|
| GeoCeDG | Product/kernel source, specifications, ADRs, product roadmap and phase state, feature manifests, validation evidence, canonical models, deterministic bundles, and the book operational bridge | BOOK roadmap, editorial acceptance, structure, manuscript, bibliography, publication assets, book build state, or BOOK-P lifecycle decisions |
| `mpradovelasco/geocedg_book` | BOOK roadmap, editorial contract and structure, historical/current accepted technical baselines, source mapping, future manuscript, bibliography, publication assets, book build and release state | GeoCeDG product semantics, canonical product source, product phase state, or product validation authority |

GeoCeDG owns an operation only when correctness requires knowledge of product
Git state or product technical authority. The book accepts or rejects the
result editorially. No GeoCeDG command may silently update an editorial file,
and no prompt in GeoCeDG authorizes a BOOK phase or manuscript work.

`book` at the GeoCeDG root is only a local filesystem link to an external
clone. The root-scoped `/book` ignore rule prevents accidental discovery by
GeoCeDG Git. It is not a submodule, gitlink, vendored manuscript, or recorded
machine path. A normal GeoCeDG clone has no link and remains fully buildable
and verifiable without the book.

## Establish the optional link

Clone the book outside the GeoCeDG worktree and create a local link. Choose the
external location locally; never record its absolute path in either repository.

```powershell
$BookClonePath = Read-Host "External directory for geocedg_book"
git clone https://github.com/mpradovelasco/geocedg_book.git $BookClonePath
New-Item -ItemType SymbolicLink -Path .\book -Target $BookClonePath
```

A directory junction is also accepted on Windows. The bridge resolves its
physical target and requires the exact external Git root. It rejects a normal
nested directory, shared Git authority, tracked `book` entry, submodule or
superproject state, missing ignore rule, repository subdirectory, or origin
other than `mpradovelasco/geocedg_book`. The worktree roots and both Git/common
metadata directories must also remain physically non-nested across repository
authorities. Git-reported common/worktree metadata paths containing a
filesystem-link alias are rejected before authority comparison.

Git ownership protection remains in force. Do not add a global `safe.directory`
exception from this workflow. A sandbox ownership failure is an environment
classification and should be handled by the execution environment, not by a
repository change.

## Published technical authority

The bridge resolves published product state offline from the local
`refs/remotes/origin/main` and the closest reachable eligible technical
reference. It never fetches. Working `HEAD`, local `main`, `origin/main`,
ahead/behind, and dirty state are reported separately.

A technical reference has an explicit type and a pinned identity (type, tag
name, tag object, commit and tree):

| Type | Tag | Eligible when |
|---|---|---|
| `PHASE_CHECKPOINT` | annotated `geocedg-*-pass` | the tag is annotated and its commit is reachable from `origin/main` |
| `PUBLIC_RELEASE` | annotated `vMAJOR.MINOR.PATCH` | as above, and `docs/releases/<tag>.release.json`, read from the Git object of `origin/main`, agrees with the tag object, commit, tree and version, with the package profile at that commit, and records a contrasted publication |

The three notions are distinct. A phase checkpoint records a phase approval and
implies no binary distribution. A public release records that the author
authorized a distribution; its record lists the verification that was accepted
on the release commit, what was not executed or is not established there, and
its limits, and it carries no single pass flag. Neither is an editorial
acceptance: the book accepts or rejects a reference in its own ledger.

A release record is a verifiable descriptor, not an autonomous statement of
technical success. The offline bridge checks that it is coherent with Git
objects; whether the release is publicly available was contrasted when the
record was written (`publication_contrast`) and is not rechecked by the bridge.

Lightweight tags are never a reference. An annotated version tag without a
record, or whose record lacks contrasted publication provenance, is reported as
ineligible. A record that contradicts Git identity, a tag message that declares
another commit or tree, or a package profile that declares another version
stops the operation: errors of identity or provenance fail closed.

Selection is deterministic: the eligible reference with the fewest commits to
`origin/main`; a public release before a phase checkpoint on the same commit;
then the tag name. Two eligible references on one commit are one technical
state and the candidate lists the other as coincident. `-ReferenceTag <tag>`
selects one eligible reference explicitly. If a nearer release has a lower
version than a farther one, the default selection is refused and an explicit
tag is required. `origin/main` after the last reference is reported as
`MAIN AHEAD OF PUBLISHED REFERENCE` with the changed authority categories; it
never replaces the commit of the reference.

The deterministic technical snapshot is derived from Git objects at the
commit of the reference; it does not depend on the reference type. Fingerprint
schema version 2 records conservative domain fingerprints for:

- normative phase state;
- feature maturity and public surface;
- kernel semantics and command/API source;
- persistence and compatibility;
- application profile, GUI, and workflow;
- technical specifications, architecture, and ADRs;
- validation/model evidence;
- G9O1 bundle/source provenance; and
- packaging/export workflow.

Each domain is based on reproducible Git blob/tree identifiers. The overall
fingerprint changes when any covered domain changes and alignment reports the
changed domains. Product commits confined to the book bridge remain outside
that product-authority fingerprint, so a newer commit alone does not imply
technical/editorial drift.

## Operations

### Status and alignment

The default operation validates both repositories and reports their Git state,
published product authority, book authority disposition, and current technical
alignment. It does not mutate either repository.

```powershell
.\tools\book\book-worktree.ps1
.\tools\book\book-worktree.ps1 -Action Status
.\tools\book\book-worktree.ps1 -Action Alignment
```

The book owns `editorial/source-mapping/TECHNICAL_BASELINES.json`. GeoCeDG reads
that ledger from the locally available published
`refs/remotes/origin/main` Git object, never from an untracked, dirty,
candidate-branch, or local-only `main` file, and never writes it. The operation
does not fetch; preflight must establish that the remote-tracking ref is current.
Alignment validates the accepted commit, annotated tag, fingerprint schema,
recomputed fingerprint, and book authority before comparing current published
state. It uses the current accepted editorial baseline, not a historical BOOK
phase pin. It reads ledger schema 1, whose records are phase checkpoints, and
schema 2, whose current record declares its `reference_type`; an accepted
public release must also pin its tag object and tree and keep a valid release
record in published GeoCeDG authority. Any other schema, type or identity is a
`TECHNICAL CONTRADICTION`.

Finite classifications are:

- `ALIGNED`: the accepted fingerprint matches published authority, including
  the case where newer commits do not change the bounded snapshot;
- `EDITORIAL BASELINE STALE`: the accepted commit is an ancestor and the
  bounded authority snapshot materially changed;
- `TECHNICAL CONTRADICTION`: identifiers, fingerprints, or histories conflict;
- `REFERENCE MISSING`: the ledger/current accepted reference or Git object is
  absent, including `NOT_YET_REFRESHED`;
- `UNPUBLISHED PRODUCT STATE`: an editorial reference points beyond the
  published reference.

Dirty/ahead checkout state is reported separately as the product-source
relationship; it does not contaminate the deterministic candidate.

Drift is a review signal. Chapter relevance remains an editorial decision made
under the book roadmap; no command automatically changes a chapter or baseline.

### Technical-baseline candidate

This GeoCeDG-only action can run without a book clone. It writes deterministic,
review-only JSON below ignored `artifacts/book/`. It never accepts a baseline
or changes the book.

```powershell
.\tools\book\book-worktree.ps1 -Action BaselineCandidate
.\tools\book\book-worktree.ps1 -Action BaselineCandidate `
  -OutputPath artifacts/book/p1-candidate.json
.\tools\book\book-worktree.ps1 -Action BaselineCandidate `
  -ReferenceTag v1.2.1 -OutputPath artifacts/book/v1.2.1-candidate.json
```

Candidate schema 2 records the typed reference (`published_reference` with its
type, tag object and tree), the release block of a public release, coincident
and reachable references, ineligible tags and the delta of `origin/main`.

The candidate uses the published commit timestamp, not wall-clock generation
time. Repeated runs over the same published repository/ref state are
byte-identical even from a dirty or differently named checkout branch. Physical
paths are not recorded. Output is restricted to untracked
`artifacts/book/**`; tracked targets, traversal, and filesystem-link components
are rejected.

### Book verifier delegation

GeoCeDG invokes, but does not reimplement, the book-owned verifier. Delegated
entry points must be tracked regular files with no filesystem-link components.

```powershell
.\tools\book\book-worktree.ps1 -Action Verify
.\tools\book\book-worktree.ps1 -Action Build
```

`Verify` delegates to book `tools/verify.ps1`. `Build` is retained as an
explicit book-owned action; invoking it does not authorize manuscript content
or publication. Optional `-BookArguments` are passed as argument-array values.
The quarantined generated-chapter branch is never accepted as book authority.

### Evidence export

Book-oriented technical evidence composes the existing author-approved G9O1
generator. No book-specific knowledge system or binary model copy is created.

```powershell
.\tools\book\book-worktree.ps1 -Action Evidence
.\tools\book\book-worktree.ps1 -Action Evidence `
  -EvidenceProfiles knowledge,source `
  -EvidenceOutputDirectory artifacts/knowledge/book
```

The default profile is `knowledge`. `source` remains an explicit composable
option; any requested identifier must already exist in
`geocedg/specs/operations/knowledge-bundle-profiles.json`. G9O1 clean-tree,
ownership, rights, budget, path, and determinism rules remain authoritative.
The bridge propagates a profile's authoritative encoding/ownership failure; it
does not patch or bypass G9O1. Release-quality evidence requires a clean
checkout exactly at the commit of the published reference. `-AllowDirtyEvidence`
is an explicit `NON_RELEASE_DIRTY_OR_UNPUBLISHED_STATE` mode. Evidence output is
restricted to `artifacts/knowledge/book/**`. Each export writes a deterministic
bridge-owned `book-evidence-export.v2.json` sidecar beside the profile
directories. The sidecar records the release/non-release disposition, source
commit, the type, tag and commit of the reference, dirty state, and SHA-256 bindings to each G9O1 manifest
and archive; it does not replace or modify the G9O1 bundle authority. Canonical
`.ggb`/`.ggt` models remain referenced by repository path and blob/manifests
rather than copied into text bundles.

## Rolling baseline and chapter provenance boundary

The book repository defines and accepts:

1. immutable historical phase baselines;
2. the current editorial technical baseline;
3. semantic chapter-entry refresh decisions; and
4. chapter closeout provenance.

GeoCeDG supplies published candidates, alignment classifications, and evidence
only. It does not decide whether a change is relevant to a chapter, alter the
BOOK roadmap, or bind a chapter to floating `HEAD`. See the authoritative book
roadmap and baseline policy in the external repository.

## Prompts and phase authorization

`.github/prompts/canonical/book/operations.prompt.md` governs only bounded book
operations. It must inspect the external BOOK roadmap instead of copying it.
Running that prompt or any command in `tools/book` does not authorize
BOOK-P0-post closeout, BOOK-P1, manuscript execution, or a product phase.

## Independent verification

The real external book is deliberately absent from `tools/agent/verify.ps1`,
normal bootstrap, and CI. Missing or dirty book state cannot change product
acceptance. `tools/agent/verify-book-operations.ps1` uses disposable fixture
repositories only; actual `Status`, `Alignment`, and `Verify` calls remain
explicit and opt-in.

Run Git commands separately from the intended repository root. Never use a
recursive `git add` from GeoCeDG to operate on book content. Neither bridge nor
verifier installs, stages, commits, merges, tags, fetches, pulls, or pushes.

The block below is the state at the BOOK-P0-post closeout, kept as the
historical record of when this workflow was written. It is not the current
state of the BOOK phases: that state is read in the roadmap of the book
repository and is never copied here.

```text
BOOK-P0-post = PASS — AUTHOR APPROVED
BOOK-P1 = NOT AUTHORIZED
BOOK MANUSCRIPT EXECUTION = PARKED
```

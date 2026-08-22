# GeoCeDG canonical book-operations prompt

Status: canonical operational prompt

## Objective

Perform bounded product-to-book operations through the validated independent
book worktree while preserving GeoCeDG technical authority and book editorial
authority.

## Authority hierarchy

Read `AGENTS.md`, current GeoCeDG Git/product authority, the normative product
roadmap and feature manifests, G9O1 bundle contracts, and
`docs/developer/book_repository_workflow.md`. For editorial state, inspect the
external book repository's `AGENTS.md`, canonical BOOK roadmap, editorial
contract, and technical-baseline ledger. Never copy those editorial authorities
into GeoCeDG.

## Allowed scope

- validate the external repository boundary and expected origin;
- report product/book Git state without mutation;
- derive a deterministic published technical-baseline candidate;
- audit the current accepted editorial baseline against published product
  authority;
- generate existing G9O1 evidence profiles;
- invoke the book-owned verifier through the validated worktree;
- prepare a BOOK phase preflight report without starting that phase.

## Forbidden scope

No BOOK roadmap decision, editorial acceptance, manuscript prose, chapter
drafting or rewriting, bibliography policy, publication state, phase closeout,
product phase execution, kernel semantics, automatic cross-repository write,
commit, merge, tag, fetch, pull, push, or hidden synchronization.

## Invariants

- `geocedg_book` remains an external, non-nested, non-submodule Git authority.
- Historical BOOK baselines are immutable and distinct from the current
  editorial baseline and current published GeoCeDG state.
- Alignment is semantic; SHA inequality alone is not a stale-baseline result.
- Generated candidates and bundles are evidence, not source or approval.
- Canonical model binaries remain referenced by provenance rather than copied
  into text bundles.
- A prompt or successful command never self-authorizes a BOOK-P phase.

## Commands and evidence

Use `tools/book/book-worktree.ps1` for status, alignment, baseline candidate,
G9O1 evidence composition, and explicit book verification. Use
`tools/agent/verify-book-operations.ps1` for fixture-only GeoCeDG verification.
Record commands, exit codes, logs/artifacts, classifications, and skipped
checks. Run `git diff --check` in each changed repository.

## Stop conditions

Stop for a dirty unrelated worktree, missing editorial authority, unexpected
origin, nested/shared/submodule state, unreproducible published reference,
technical contradiction, attempted editorial mutation, generated-branch
authority, or any request that requires new phase/author approval.

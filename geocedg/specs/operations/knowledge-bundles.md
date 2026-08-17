# Deterministic source and knowledge bundle contract

- Status: **NORMATIVE / AUTHOR APPROVED**
- Phase: G9O1
- Accepted decision: `docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md`
- Schema: `geocedg/specs/operations/knowledge-bundle.schema.json`
- Profiles: `geocedg/specs/operations/knowledge-bundle-profiles.json`
- Implementation status: **PASS — AUTHOR APPROVED**

## 1. Boundary

A bundle is a generated, read-only knowledge artifact. It does not become
source authority, alter the working tree, enter the geometric dependency graph,
or replace repository paths. G9O1 implements the generator at
`tools/knowledge/build-knowledge-bundle.ps1`, its independent artifact verifier
at `tools/knowledge/verify-knowledge-bundle.ps1`, and the focused repository
gate at `tools/agent/verify-knowledge-bundles.ps1`.

## 2. Ownership classification

Every candidate path receives exactly one class:

1. `THIRD_PARTY_OR_RESTRICTED` when license/provenance or an explicit asset rule
   prevents default inclusion;
2. `GENERATED` for build, package, report, cache, log or other regenerable
   output;
3. `GEOCEDG_NATIVE` for project-authored paths/namespaces confirmed by history;
4. `UPSTREAM_MODIFIED` when the modified-file inventory or a comparison with the
   pinned GeoGebra baseline proves a project change;
5. `UPSTREAM_UNCHANGED_REFERENCE` for an unchanged upstream file included only
   by an explicit profile rule.

Classification uses path/namespace, `docs/upstream/modified-files.yml`, Git
comparison with `docs/upstream/BASELINE_COMMIT.txt`, file history and explicit
exclusions. Folder name alone is insufficient. Default source bundles include
only `GEOCEDG_NATIVE` and `UPSTREAM_MODIFIED`.

An inventory/Git disagreement is a hard error requiring human review; the
generator must not guess ownership.

## 3. Profiles

- **source**: native and upstream-modified source, focused tests, feature/profile
  data, verification wrappers, complete modified files, baseline identity,
  concise change summaries and optional unified diffs;
- **knowledge**: `AGENTS.md`, roadmap, specs, ADRs, architecture, API/user/
  developer/prompt guides, canonical prompts, current validation summaries,
  scientific catalog metadata, source maps and a reading order;
- **thematic**: governance, frontend/DXF, Locus V2, normative unimplemented spatial G9 and the
  operational layer.

Profiles compose ordered file selections. They do not concatenate the entire
repository into one uncontrolled document.

## 4. Manifest

Each bundle has one schema-versioned manifest containing repository/branch/
commit/dirty state, pinned upstream baseline, generator version, normalized
configuration and a deterministic bundle ID. Every entry records source path,
ownership, language, encoding, line range, raw and canonical SHA-256, baseline blob,
change type, related spec/ADR/phase/tests, license/provenance and ordering.

For `UPSTREAM_MODIFIED`, the complete current file is mandatory. Baseline blob
identity and change summary are mandatory; a unified diff is optional and must
be derived from the recorded baseline/current pair.

## 5. Determinism

- UTF-8 text, canonical LF, deterministic path and entry ordering;
- stable archive metadata and no variable timestamps in deterministic content;
- same commit plus normalized configuration produces identical manifest,
  archive and hashes;
- clean tree required by default;
- dirty mode is explicit, records the diff/hash and marks the bundle
  non-release evidence;
- bundle ID is a hash of schema version, commit and canonical configuration;
- current `HEAD` must equal the manifest commit for a fresh bundle;
- size, file and token budgets fail closed.

Text hashing consumes bytes, decodes strict UTF-8, removes only a UTF-8 BOM and
normalizes CRLF or CR to LF. PowerShell line pipelines must not be used to
capture Git blobs because they can alter line boundaries.

## 6. Chunking

Chunk in this order: complete file, top-level declaration, section, then line
range. Never split a class or Markdown section when another budget-respecting
boundary exists. Every unavoidable continuation records a stable continuation
ID, sequence/total, original path, complete source hash and exact line range.
Chunks never replace the complete-file requirement for upstream-modified source.
The configured `maximum_chunk_tokens` bounds canonical source payload using the
declared byte-based estimate; provenance headers are additional metadata. A
single source line that cannot fit fails closed rather than being split.

## 7. Default exclusions

Exclude `.git`, build outputs, `.gradle`, `.kotlin`, installers, caches, logs,
temporary files, secrets, local settings, absolute user paths, unrelated
third-party source, restricted assets and large PDFs. Scientific PDFs are
represented by catalog path, provenance and hash unless a profile explicitly
authorizes copying them.

## 8. Verification design

The artifact verifier independently recomputes ownership, path order, hashes,
bundle ID, archive metadata, budgets, exclusions and freshness. It must reject
missing sources, stale commits, ambiguous ownership, absolute paths, restricted
assets, hash mismatch and invalid continuation topology.

## 9. Stop conditions

Stop for missing baseline objects, unresolved license/provenance, dirty state
without explicit authorization, ownership disagreement, non-UTF-8 text without
an explicit binary policy, nondeterministic output or a budget that would split
semantic boundaries unsafely.

This normative contract defines G9O1. The implementation is **PASS — AUTHOR
APPROVED**. Generated bundle instances remain ignored evidence under
`artifacts/knowledge/` and are never committed as source authority.

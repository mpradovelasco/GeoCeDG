# G9O1 source/knowledge bundles and guides closeout report

- Date: 2026-08-17
- Status: **PASS — AUTHOR APPROVED**
- Architectural layer: operational tooling and living documentation
- Product/geometric effect: none
- Canonical prompt LF SHA-256:
  `b0b04ee4095423fd76f4ecd18a9ea567c091d23197b9bdfd92c736c25b1b9ed6`

## Entry authority

Implementation began only after the following gates passed on a clean
`feature/g9o1-source-knowledge-bundles-guides` tree:

- `HEAD`, local `main` and `origin/main` were
  `94f92f49a44560e44bae9e75ba52595067471368`;
- annotated `geocedg-g9p-pass` tag object
  `6ce37f03df6f742aa448323d2150dd1655c986a5` peeled to that commit and the
  published feature branch resolved to it;
- the canonical prompt hash matched the value above;
- all six G9 specifications were normative, ADR 0010–0015 were Accepted, G9O1
  alone was authorized, and no later prompt was authorized;
- `tools/agent/verify-operational.ps1` exited 0;
- the focused G9P design gate exited 0 with logs below
  `artifacts/g9o1/entry/g9p-design`;
- the composed entry gate ran without `-SkipBuild` under managed escalation and
  exited 0 with logs below `artifacts/g9o1/entry/composed-escalated`. The first
  sandboxed attempt was classified as Windows Gradle/Kotlin cache permission
  failure, not a project-code regression.

## Author closeout decisions

The author approved the complete G9O1 implementation on 2026-08-17:

| Decision | Disposition | Preserved contract |
| --- | --- | --- |
| D1 — G9O1 implementation | **APPROVE** | `G9O1 = PASS — AUTHOR APPROVED` |
| D2 — generator architecture | **APPROVE** | Git index/tree enumeration, ownership precedence, semantic chunking, UTF-8/LF canonicalization, raw/canonical hashes, stable ZIPs, ordering, inventory/freshness validation, budgets, clean/dirty modes, safe paths/remotes and rights-aware exclusions |
| D3 — verification independence | **APPROVE** | the artifact verifier remains logically separate and does not import or reuse the generator implementation |
| D4 — generated artifact policy | **APPROVE** | generated bundles remain untracked; `tracked generated bundles = 0` |
| D5 — historical evidence | **APPROVE** | frozen G7/G8/G9P evidence remains distinct from living G9 documentation |
| D6 — documentation | **APPROVE** | `GUIDE_IMPACT = UPDATED` |

This approval authorizes closeout publication, fast-forward-only promotion, the
annotated `geocedg-g9o1-pass` tag and preparation of the clean
`feature/g9a1-spatial-identity-persistence-foundation` branch. It authorizes
G9A1 but does not execute it.

## Implemented boundary

The approved implementation adds a checked-in PowerShell generator and
independent verifier.
Candidate enumeration comes from Git index/tree objects, never an unrestricted
filesystem crawl. Ownership is applied in the approved order: restricted,
generated, GeoCeDG-native, upstream-modified, then explicitly selected unchanged
upstream reference. Inventory/Git disagreement, unsupported Git modes and
ambiguous ownership stop generation.

Text is captured as bytes, decoded as strict UTF-8, stripped only of an optional
BOM and normalized to LF. The manifest records raw and canonical hashes,
complete canonical files, provenance-rich chunks, baseline/current blob
identity and optional baseline-derived diffs. Bundle identity uses schema
version, commit and normalized configuration. ZIP entries use fixed ordering,
1980 timestamp, Unix 0644 attributes and no compression.

Clean state is the default. Explicit `-AllowDirty` mode records staged,
unstaged and untracked evidence and marks the bundle `NON_RELEASE_EVIDENCE`.
Output is confined to ignored `artifacts/knowledge`; no generated bundle is
tracked.

## Verification design and fixtures

`tools/knowledge/verify-knowledge-bundle.ps1` does not import the generator. It
independently recomputes current state, profile membership, ownership, raw and
canonical bytes, configuration/bundle identity, complete files, chunks,
continuations, diffs, dirty evidence, totals, budgets, archive order/metadata,
archive bytes and freshness.

The disposable real-Git fixture suite covers:

- byte-equal clean and explicit-dirty reruns;
- profile membership and all admissible ownership classes;
- BOM/CRLF raw-versus-canonical hashing;
- complete upstream-modified file, baseline blob and derived diff;
- configured, restricted and generated exclusions;
- clean-default rejection and dirty untracked hashing;
- whole-catalog profile validation plus profile/output traversal rejection;
- control-character/dot-segment path and symbolic-link rejection;
- local or credential-bearing remote provenance rejection;
- staged and unstaged inventory disagreement rejection;
- file, byte, token and unsplittable-line chunk budget rejection;
- independent validation and stale-HEAD rejection.

## Historical evidence and guide impact

The G9P verifier now checks its design closeout from annotated
`geocedg-g9p-pass` blobs and requires current `HEAD` to descend from that
commit. The immutable `geocedg/validation/g9p/g9p-evidence.sha256` file was not
rewritten. Living G9O1 documents are validated separately. The closeout adds
`geocedg/validation/g9o1/g9o1-evidence.sha256`; after publication, the focused
gate validates those 29 canonical-LF hashes from the annotated G9O1 tag blobs.

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = README.md; docs/architecture/geocedg_documentation_architecture.md; docs/architecture/knowledge_bundle_architecture.md; docs/developer/geocedg_agent_prompt_guide.md; docs/developer/geocedg_developer_guide.md; docs/developer/repository_map.md; docs/roadmap/geocedg_roadmap.md; docs/user/geocedg_user_guide.md; docs/validation/g9_documentation_bundle_traceability.md; geocedg/specs/README.md
```

The guides explain profile choice, clean/default behavior, explicit dirty
diagnostics, independent verification, ignored output and the fact that bundles
are evidence rather than authority.

## Scope audit

The approved change count is zero for every productive boundary below:

- Java geometric kernel;
- spatial semantics implementation and `SpatialObject3D` or equivalent;
- productive `ProjectionSystem` implementation;
- Locus V2 public command surface;
- GUI and workspaces;
- DXF implementation;
- new spatial XML/persistence;
- G9A1, G9A2 and G9A3 implementation;
- G9B and G9C implementation; and
- G9U0, G9X1, G9U1 and G9U2 implementation.

G9O1 remains an operational/tooling and living-documentation phase only.

## Validation record

Promotion requires exit 0 from every authority below after the closeout record
is frozen. Generated logs are ignored evidence; exact command outcomes and
published Git objects are reported in the author handoff without modifying the
candidate after verification.

| Authority | Command | Log root |
| --- | --- | --- |
| Focused G9O1 | `.\tools\agent\verify-knowledge-bundles.ps1 -LogDirectory artifacts\g9o1\closeout\focused` | `artifacts/g9o1/closeout/focused` |
| Frozen G9P/G9P-R1 | `.\tools\agent\verify-g9p-design.ps1 -LogDirectory artifacts\g9o1\closeout\g9p-design` | `artifacts/g9o1/closeout/g9p-design` |
| Operational | `.\tools\agent\verify-operational.ps1` | console; also composed logs |
| Full composed, build enabled | `.\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory artifacts\g9o1\closeout\composed` | `artifacts/g9o1/closeout/composed` |
| Git whitespace | `git diff --check` and `git diff --cached --check` | console |

## Compatibility and next gate

No Java/kernel, spatial, Locus V2 public-surface, GUI, workspace, DXF,
serialization or product feature file changed. Source boundaries and geometric
semantics are preserved verbatim; text normalization is confined to generated
bundle copies. Restricted binary bytes and historical evidence remain
unchanged.

The implementation is validated on the repository's Windows/PowerShell 7
authority. G9A1 is authorized but remains unexecuted; G9A2 and every later
canonical prompt remain unauthorized. No productive spatial G9 implementation
is claimed.

```text
G9O1 = PASS — AUTHOR APPROVED
G9A1 = AUTHORIZED — NOT STARTED
G9 PRODUCTIVE SPATIAL IMPLEMENTATION = NOT STARTED
```

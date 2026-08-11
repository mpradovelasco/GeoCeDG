# G6B canonical prompt hardening report

- Review date: 2026-08-11
- Branch: `feature/g6b-locus-v2-kernel`
- Affected layer: documentation and executable task contract only
- Product implementation: not started
- Review status: pending validation

## 1. Preconditions and scope

The review started from commit `b6601425acdcd073e6f5d49eda82113cd03cd32f`,
which was also the local `main` tip. `main` was an ancestor of the working
branch and the worktree was clean. The repository recorded:

- `G6A = PASS — AUTHOR APPROVED`;
- ADR 0006 as `Accepted`; and
- `geocedg/specs/locus/locus-v2-semantics.md` as the normative G6 semantic
  contract.

This review does not execute G6B. It changes no productive source,
serialization contract, feature manifest, architecture decision or semantic
specification. Its sole implementation target is the existing canonical prompt
`.github/prompts/tasks/g6b-locus-v2-kernel.prompt.md`.

## 2. Decision-to-prompt audit before editing

| Accepted decision | Previous G6B prompt treatment | Gap or ambiguity | Required hardening |
|---|---|---|---|
| Parallel `GeoLocusV2`; legacy `GeoLocus` and public `Locus[...]` unchanged | Present at high level | Did not name `myPointList` as a forbidden authority or require tests around all legacy entry points | Prohibit reinterpretation/consumption of `myPointList`; enumerate command, metric, incidence and ODE exclusions and their tests |
| Distinct append-only V2 `GeoClass` preserving existing ordinals | Present | Switch audit, ordinal snapshot and negative predicate/dispatch tests were implicit | Require pre/post ordinal evidence, every affected switch audit, and tests proving no legacy dispatch |
| Provider-owned semantic parameter | Generic provider statement | Approved provider IDs and restricted path set were absent | Name `explicit-numeric-domain/v1` and `stable-path-domain/v1`; restrict the first path fixtures and prohibit implicit `PathParameter`, sample index and viewport domains |
| Branch identity differs from valid-domain components | Present but compressed | Required fields, key prohibitions and domain-component transition distinction were not independently testable | Enumerate `branchKey`, declared domain, components, orientation, provenance and lineage; require the approved topology fixture |
| Semantic/status/quality axes remain separate | Referred to as “state layers” and “four quality axes” | Easy to collapse into one enum because the individual axes were not named | Enumerate all nine independent axes and prohibit a catch-all status/quality enum |
| Pointwise and characterized canonical-continuation determinism only | Present generically | No explicit unsupported policy or history/cache-state prohibition | Require pointwise support, only characterized continuation, typed `UNSUPPORTED_NONDETERMINISM`, and query-order/cache independence |
| Nested composition is a first-class PASS gate | Present | `myPointList`, `PathMoverLocus` and upstream sampled paths were not named; functional formula was missing | State semantic call chain, exact forbidden inputs and `q * d` functional budget |
| Recursive evaluators plus scoped shared evaluation session | Present | Coherent revision set, exact-once eligibility, reference execution and session lifetime were incomplete | Require bounded/discardable session, full key, coherent revisions, exact-once eligible keys, cache-disabled reference and active-key cycle guard |
| Dependency-slice work must not occur per point query | Present as a sentence | No mandatory counters or evidence schema | Require build, synchronization/reset, dependency-update, per-level call, duplicate, hit/miss and revision counters |
| Two author models are immutable complementary legacy evidence | Present | Hash/manifest revalidation and precise role boundary were not gates | Require hash verification, no mutation, two-level control/pathological roles and internal three-level V2 fixture only |
| Future G7 services remain revision-scoped and compositional | Only `Perimeter` exclusion was explicit | Forward compatibility rule was not preserved | Record the G7 constraint while prohibiting metric index, length, perimeter and productive metric cache in G6B |
| Local monotonic semantic revision | Mentioned only as cache-key material | Publication/increment rules and `Construction.getStep()` prohibition were absent | Define snapshot publication, point/render non-increment, DAG invalidation and negative `getStep()` rule |
| Separate V2 drawable and render cache | Present at high level | Unbounded clipping, per-view key and explicit two-zoom evidence were underspecified | Require evaluator-only rendering, per-view/revision/policy cache and different tessellation with identical semantics |
| Approved uncertified validation envelope | Formula present | Required scale documentation and separation from endpoint/render/G7/G8 policy needed stronger gates | Preserve exact formula and explicitly prohibit all screen/origin inputs and cross-use by other tolerance families |
| Deliberately narrow G6B demonstrator | Scope was broad (“approved drivers”, “Level A”) | Could invite support for additional path types or scientific models | Enumerate the minimum providers, analytic/topology cases, nested fixture and regressions; prohibit convenience expansion |
| No public surface, persistence, G7/G8/G9, DXF, 3D, concurrency or new dependency | Mostly present | No exact mandatory blocked disposition and a few forbidden surfaces were distributed across sections | Consolidate stop conditions and mandate `G6B = BLOCKED PENDING AUTHOR REVIEW` |
| `LEGACY`/`V2`/`DUAL` are diagnostic; manifest is not a flag | Present | Internal creation and dual comparison semantics needed a sharper contract | Define Classic/public command as legacy, V2 as internal/test, and DUAL as explicit sampled-vs-semantic evidence |
| User guide must become primary operational/scientific evidence | Only “user guide” listed | No required content or future-monograph depth | Add mandatory use/status, scientific basis, real architecture, evidence, limitations and negative capability sections |
| Focused verifier remains subordinate to composed authority | Present | Required unit families and negative compatibility tests were compressed | Enumerate value/provider/determinism/topology/nested/session/cycle/revision/render/dispatch tests and verifier integration |
| Every productive upstream edit requires minimum-impact justification and registration | Only generic modification record | Timing and per-file discipline were missing | Require justification before each edit and immediate `docs/upstream/modified-files.yml` update; prohibit unrelated refactors/formatting |
| G4 packaging architecture remains closed | Only forbade packaging of legacy models | Product class inclusion check was absent | Permit only existing packaging smoke/inclusion checks; prohibit packaging policy changes |
| Final execution report must be independently reviewable | “Exhaustive G6B report” only | Required evidence and final dispositions were not enumerated | Define report contents, PASS/BLOCKED language, changed upstream files, validation logs, limitations and non-started future phases |

The audit found no accepted G6A decision that required changing ADR 0006 or the
normative semantic contract. The required correction belongs entirely in the
canonical execution prompt.

## 3. Source and evidence cross-check

The pinned source still exhibits the coupling described by G6A:
`GeoLocusND` implements `Path` and owns `myPointList`; `PathMoverLocus`
traverses that list; `AlgoLocusND` and `AlgoLocusSliderND` build and update
dependency slices with a 500 ms per-step guard; and `EuclidianDraw`, metrics,
commands, XML and 3D paths dispatch legacy locus behavior. `GeoClass.LOCUS`
therefore cannot be reused for V2 without violating the accepted boundary.

The preserved model hashes remain:

- `InterCilConoObliqueTwoLevels.ggb`:
  `587328a8e5b6474aee3169bb6af2fe2a711e98e000a423a96bba6e38274fb2b6`;
- `InterCilConoOblique.ggb`:
  `b1cb614f1a4c414144fbff29349ddebda92d1026acb4c535990a2895c589fa27`.

Their public redistribution remains blocked. They are evidence, not V2
persistence fixtures or package inputs.

## 4. Validation record

| Check | Result | Evidence |
|---|---|---|
| Canonical prompt contract | PASS | All headings required by the G1 operational prompt contract are present |
| Decision coverage | PASS | Focused literal audit found every mandatory G6A decision and hardening gate |
| Internal references | PASS | All referenced current authorities, scripts, records and model paths exist; the future G6B report is intentionally prospective |
| Normative coherence | PASS | No contradiction with the normative semantic contract or Accepted ADR 0006 was found |
| `tools/agent/verify-operational.ps1` | PASS | Exit code 0; operational structure, prompt contracts, schemas, CI delegation, text hygiene and upstream boundary passed |
| `tools/agent/verify-locus-v2.ps1 -SkipBuild` | PASS on canonical LF checkout | Exit code 0 in a temporary detached LF-normalized worktree at `b6601425acd`; no product build or source edit was performed |
| Productive source boundary | PASS | `git diff --name-only -- ':(glob)source/**/src/main/**'` returned no paths |
| G6B implementation absence | PASS | No productive V2 class was added; the focused verifier retained its G6A no-product-code gate |

The first focused-verifier invocation in the normal Windows checkout exposed a
pre-existing portability defect: `core.autocrlf=true` materializes the three
versioned Java characterization tests with CRLF, while the verifier compares a
raw worktree SHA-256 with the recorded LF canonical hash. LF normalization of
each test reproduced its recorded hash exactly, and binary evidence hashes
already matched. The verifier then passed unchanged in a temporary
LF-normalized detached worktree, which was removed afterwards. This hardening
task does not alter the operational script because its authorized scope is
documentation only. A later operational maintenance task should make evidence
hash verification line-ending invariant or explicitly hash canonical Git blob
content.

Final whitespace/status checks and the documentation-only commit are recorded
in the task closeout message.

## 5. Final disposition

`G6B PROMPT HARDENING = PASS`.

ADR 0006 and the normative semantic contract remain unchanged. G6B remains
`NOT STARTED`.

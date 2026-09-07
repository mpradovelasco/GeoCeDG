# G9U1 verifier lifecycle and exact-SHA author closeout

- Status: operational lifecycle implementation candidate; not G9U1 PASS
- Product checkpoint: `28f7843184cfb202bbfcca1cbcc56a25a7a77bca`
- Product tree: `d08d7beb45d04d6e0f0a478f4c04eb0e97e7e667`
- Historical precommit entry: `e4ef3d48ea95a0c3243e57dfc703b539d455c33e`
- Authority: ADR 0023, ADR 0024 and verification-levels sections 10 and 11.2
- Semantic effect: none

## 1. Problem and boundary

The G9U1 product checkpoint was made from an exact candidate worktree after the
successful technical executions. Those executions truthfully recorded
`HEAD=e4ef3d48...`, an empty index and eleven modified tracked paths. Committing
the exact bytes produced `28f7843...`; that Git operation did not make the old
execution a clean-commit execution and must never be reported as such.

The historical G9U1 verifier only described the precommit candidate. An exact-SHA
author decision therefore had no authenticated `COMMITTED_CANDIDATE ->
AUTHOR_CLOSEOUT` route. This correction adds that route by reusing the generic
phase lifecycle and repository-input-identity authorities. It changes no
workspace action, Desktop resource, product test, build input, tolerance,
reference, persistence format, macro contract or geometry.

## 2. Frozen implementation authority

`geocedg/validation/operations/g9u1-lifecycle-policy.json` freezes:

- the one-parent `e4ef3d48... -> 28f7843...` implementation edge;
- the exact `28f7843...` tree;
- the eleven modified paths and raw Git-blob SHA-256 values;
- one and only one operational successor touching the nine declared lifecycle
  paths; and
- a future fourteen-path status-only closeout projection.

Branch names and `latest` are never durable authority. The implementation commit,
future reviewed technical commit and future closeout commit are explicit SHA
arguments.

## 3. Lifecycle states

### Historical precommit candidate

The original precommit assertions remain reproducible at `e4ef3d48...` with the
eleven-path candidate worktree and false approval/PASS flags. This state is
historical technical evidence. It is not a committed-candidate or author-closeout
claim.

### Committed candidate

`COMMITTED_CANDIDATE` authenticates `28f7843...`, its tree and all eleven blob
hashes, then permits only the single linear operational successor described by
the policy. It does not use a feature-branch name. Every existing G9U1 product,
scientific, manifest and historical-source assertion remains live. The state
must remain:

```text
selfApproved = false
authorApprovedImplementation = false
passClaimedImplementation = false
```

### Author closeout

`AUTHOR_CLOSEOUT` can run only after a later author decision names the exact
operational successor as `REVIEWED_TECHNICAL_COMMIT`. It proves ancestry,
implementation policy, exact closeout path/content projection, Git identity of
all non-closeout tracked content, supported clean materialization, external and
untracked input authority, and a valid closeout record. It must reject any
product, test, verifier, build, tolerance, reference or unknown delta.

The result states only:

```text
technical execution = historical precommit cohort
reviewed product checkpoint = 28f7843...
reviewed technical lifecycle successor = exact later author-named SHA
author closeout consistency = exact status-only descendant
```

It does not say that PHASE, COMPOSED or FULL executed on either later commit.

### Published regression

After closeout publication, ordinary `verify.ps1` invocations must keep running
the live G9U1 assertions without treating the status-only commit as a second
infrastructure successor. `PUBLISHED_REGRESSION` therefore authenticates the
fixed annotated tag `geocedg-g9u1-pass`, its exact message and peeled closeout
commit, then reads the exact reviewed technical SHA from the closeout record and
replays the same policy/tree proof. The tag is required to be annotated; no
branch name, `HEAD` convention or latest-commit search is accepted. This mode
authenticates historical approval but supplies no reusable build receipt: live
scientific/product checks still execute at the level requested by the caller.

## 4. Evidence-preserving precommit link

The new precommit-link manifest is a retrospective, authenticated lifecycle
index over already sealed artifacts. It is not a producer-created artifact of
the historical run. Artifact-native timestamps, hashes, repository `HEAD`, index
and status remain unchanged.

The schema-v2 link requires these semantic roles exactly once:

- `FOCUSED_A_SUMMARY` and `FOCUSED_B_SUMMARY`;
- one `PHASE_ROOT` and one `PHASE_SUMMARY` (G9U1 did not produce separate
  top-level PHASE-A/PHASE-B roots);
- `COMPOSED_ROOT` and its canonical `COMPOSED_RECEIPT`/build evidence;
- `FULL_ROOT` and its canonical `FULL_RECEIPT`/build evidence;
- every input-inventory, JUnit, Checkstyle and audit artifact transitively
  hash-bound by those receipts, under the generic `ARTIFACT` role; and
- the eighteen fixed singleton repair roles, including the paired infrastructure
  results, structural proof, bounded integrations and diff/static checks.

For G9U1, this schema-v2 authority is mandatory at the public lifecycle
dispatcher. A legacy schema-v1 technical-evidence manifest remains supported
for older phase policies, but cannot enter G9U1 `AUTHOR_CLOSEOUT` and bypass
the exact staging, Git-mode, role-closure or precommit-provenance checks.

Historical Round 3 presentation inventories also stop at the immutable product
checkpoint in committed and closeout modes. Operational lifecycle successors
must not be mistaken for part of either reviewed presentation delta; the
precommit mode retains its original live worktree calculation.

Every entry has `role`, repository-relative `path`, original `recordedPath` and
raw `sha256`. The manifest also records:

- schema and evidence-kind identifiers;
- historical repository commit, index SHA-256 and porcelain-status SHA-256;
- raw input count, byte count, inventory SHA-256, raw-tree SHA-256 and input
  fingerprint;
- implementation commit/tree and the eleven exact candidate paths;
- focused deterministic summary hash;
- PHASE/COMPOSED/FULL result counts and root hashes; and
- explicit false approval/self-approval/PASS indicators.

Validation requires all role/path/hash pins, original receipt-native sealing,
the exact historical dirty status, matching COMPOSED/FULL input authority, and
byte identity between the historical raw input inventory and the content
committed in `28f7843...`. It accepts supported clean-filter equivalence for
Git materialization; it never asserts physical raw bytes equal Git blob bytes.

The bounded R1 cross-checkout regression applies the same separation to its
verification-code cohort: durable authority is the exact technical commit plus
each source path, Git mode and blob OID. Raw SHA-256 values in the emitted R1
result prove same-run physical stability only; they are not compared across a
later LF/CRLF checkout.

## 5. Hash-manifest closeout rule

The candidate sidecar contains five leading comments and 156 ordered authority
records. The lifecycle policy pins `28f7843...` as the immutable source of that
header and path-list shape, with:

```text
preserveLeadingComments = 5
authorityPathCount = 156
authorityPathListSha256 =
a05faf95b4527c9e55077085971312945028edc3e70c7ca0de5478caa051f617
```

The digest is over the 156 paths joined by LF with one terminal LF. During a
future closeout, hashes are recomputed from the exact reviewed technical commit
plus the policy-defined expected status bytes. The historical comments and path
order are preserved. No generated manifest can add a path implicitly.

## 6. Exact closeout surface

The fourteen paths are twelve living G9U1 status/traceability authorities, the
156-record hash sidecar and
`geocedg/validation/g9u1/g9u1-author-closeout.json`. Exact literal replacements
update only live status blocks. Historical Round 1/2/3 and failed-run narratives
remain unchanged. The closeout record must name the exact reviewed technical
commit and an authenticated evidence-link manifest. Every pre-existing closeout
path must retain its reviewed Git mode, and the decision record must be a normal
`100644` blob. Matching bytes never authorize an executable-bit or other mode
change, in either pending-index or committed closeout state. A pending closeout
must therefore be completely staged, with no unstaged overlay or untracked
decision record, before its Git modes can be authenticated.

## 7. Evidence-preserving verifier-repair gate

ADR 0024 section 11.2 is conjunctive. The repair verifier must establish all
fifteen conditions, including exact product/test/build/execution-plan equality,
the bounded identity-only function projection, focused tests twice, real shared
and Desktop integrations, and unchanged acceptance semantics. A single false or
unproved condition sets:

```text
EVIDENCE_PRESERVING_VERIFIER_REPAIR = false
```

and restores normal PHASE/COMPOSED/FULL requirements. A true result links old
heavy scientific evidence plus new focused lifecycle evidence without claiming
that the old tests ran again.

## 8. Operational impact

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT = NONE
```

No PowerShell/JDK/Gradle prerequisite, dependency, workstation bootstrap,
environment policy, packaging input or user-facing operating command changes.
The generic ADR 0023/0024 guidance already defines the operator model; this file
binds G9U1 to it. G9U1 remains a technical candidate until a later exact-SHA
author decision.

# G9U0-R5 similarity transformations closeout

## Status

```text
G9U0-R5 = PASS — AUTHOR APPROVED
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true

manualAuthorSmoke = PASS WITH G9A FREE-INPUT LIMITATION CHARACTERIZED
freeInputCompatibleRedefine = DEFERRED TO G9U1 DESIGN / NOT AN R5 BLOCKER

G9U1 = DESIGNED — NOT AUTHORIZED
```

This report records the author's R5 PASS decision. The author-selected
finite-zero-scale policy is Option A: a valid source-domain-preserving
`COLLAPSED_IMAGE`, rich length zero and no fabricated isolated intersection
root. The canonical execution prompt remains immutable.

Final closeout audit also covered the finite-extreme arithmetic boundary of
Option A. A collapsed dilation returns its finite center before evaluating
`x - center`, so finite extreme source/center coordinates cannot turn the exact
collapsed image into `NaN` through the intermediate expression
`0 * infinity`. Exact zero-angle rotation and unit dilation likewise return the
source point before centering arithmetic. The existing edge-case and identity-map
authorities contain these regressions; no non-identity transformation semantics
changed.

## Operational authority

The focused authority is
[`verify-g9u0-r5-locus-v2-similarity-transformations.ps1`](../../tools/agent/verify-g9u0-r5-locus-v2-similarity-transformations.ps1).
It executes after the sealed R4 authority and before future G9U1. Its durable
inputs are the 106 active Option-A rows in the
[validation matrix](g9u0_r5_locus_v2_similarity_transformations_validation_matrix.md),
the [scenario inventory](../../geocedg/validation/g9u0-r5/g9u0-r5-locus-v2-similarity-transformations-scenarios.json)
and the paired [machine evidence](../../geocedg/validation/g9u0-r5/g9u0-r5-locus-v2-similarity-transformations-evidence.json).
The four rejected Option-B rows remain inactive planning history.

The focused inventory declares 46 JUnit methods: 26 semantic/covariance tests,
8 public command-routing tests, 8 semantic degeneration/domain controls and 4
Desktop native-archive/preservation tests plus one byte-exact author-fixture
dynamic-dilation regression. Focused A and B each execute that exact set and
must produce byte-identical canonical summaries. Automated evidence and the
author's approval remain distinct: `selfApproved = false`.

Tracked clean source evidence is read from the current Git blob; modified or
new candidate source is read from its candidate bytes. Both use UTF-8,
BOM-independent, canonical-LF SHA-256. Physical LF/CRLF checkout representation
is not authority, while a content mutation must change the evidence. Binary
fixtures, if added later, require byte-exact hashing.
The summary includes the active scenario set, exact JUnit results, candidate
inventory, canonical source/authority hashes, seven public command forms and
open risk identifiers.

## Automated candidate validation

The initial focused A/B executions each passed 45/45 with Checkstyle clean and
byte-identical canonical summaries. The initial full composed authority also
exited 0 with `All GeoCeDG verification gates passed.` After all durable
evidence edits, replacement focused A and B again each passed 45/45 with
Checkstyle clean. Their canonical summaries match byte-for-byte at SHA-256
`f953750bb3db95076223ccde6f271f8da1c4ede639fe20924c76e3eadf779213`.
The summary hash is deliberately kept in this report rather than in an
authority JSON whose own hash participates in the summary. The final replacement
composed authority exited 0 with `All GeoCeDG verification gates passed.`

The closeout replacement authority adds the passing dynamic-dilation regression
for a final 46/46 focused inventory and deterministic rerun. Focused A and B
match byte-for-byte at canonical summary SHA-256
`083eece5f931d3cccf3cca844c14000e291e7a346f71cd5dd04e996240b3be23`.
The recursively hashed durable JSON authority deliberately does not contain its
own summary hash. The complete closeout composed authority exited 0 with
`All GeoCeDG verification gates passed.` before publication.

The definitive prospective G9U1 authority is
`.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r5.prompt.md`,
canonical-LF SHA-256
`0b96f571932144f9a99f7681938edf756c8999cb847b61095f82430068e96389`.
It preserves the historical G9P and post-R3 prompts, remains unexecuted and
unauthorized, and requires sealed R5 PASS plus a separate author authorization.
It consumes all seven ordinary R5 forms, fresh transformed-query tokens and
the truthful `COLLAPSED_IMAGE` boundary without adding G9U1 product code.

## Historical and retained authority

R4 product authority remains the immutable annotated tag
`geocedg-g9u0-r4-pass`, tag object
`0f9b303057b00d23722ad1f9d3594b4609d668a7`, peeling to product commit
`63c291464111a5bcdbca488d6639662e46c389c4`. The post-closeout operational
descendant `ab465bfcbd08f168c730ba639ec5f99a4b08b9df` is the R5 entry commit.
Historical verification is consumed through the sealed tagged-descendant
pattern; R5 does not substitute its current tree for R4 evidence.

The retained risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains
`OPEN / TRACKED / NONBLOCKING`. R5 neither resolves nor closes it and does not
turn it into an implicit transformation dependency.

## Author-smoke dynamic-dilation investigation

The author reported an input-error message while entering the free-input
expression `k=0.25`. The exact ignored author archive is 25,704 bytes with SHA-256
`13cde59d54a463413140007e793a50e8cb933cab21d4be286c9d76f6b2f713fe`.
The byte-identical durable regression copy is
`source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r5/fourSolutionsDynamicDilate.cedg`.

The first bounded characterization distinguishes three host routes:

- the real slider mutation seam (`GeoNumeric.setValue` followed by normal
  repaint/cascade) preserves the same transformed parent, durable ID and
  semantic object through positive, negative and repeated zero crossings;
- explicit Algebra-view editing uses `changeGeoElement`, captures its target
  before parsing and also succeeds without an error;
- a free input-bar expression such as `k=0.25` is a same-label replacement
  request without explicit target authority. G9A deliberately rejects it with
  `REDEFINE_CONTEXT_MISSING` before R5 recomputation, leaving the slider,
  transformed object and identity unchanged.

The focused regression proves the first two positive paths, repeated
nonzero/zero recovery, native save/reopen recovery and the atomic G9A negative
boundary. No persistent construction corruption was reproduced. Making the
third path retain identity from the label would change the accepted cross-cutting
G9A redefine contract, so it is not a bounded R5 correction. The author accepts
this characterization, explicitly forbids broadening G9A in R5 and records the
free-input usability issue as a future G9U1 design requirement. It is not an R5
semantic failure or an R5 closeout blocker.

## Manual author smoke — PASS with characterized G9A limitation

1. Launch GeoCeDG with the existing Locus V2 opt-in.
2. Exercise translation, origin/center rotation, point/line reflection and
   origin/center dilation through their ordinary commands.
3. Vary every transformation input and verify dependent Point, rich length and
   rich intersection recomputation.
4. Reach the same transformed-query geometry through direct, incremental and
   partial-reverse updates and compare exact transformed token bindings.
5. Compose at least three transformations.
6. Set `k=0`: retain the semantic domain/branches, obtain typed rich zero
   length and fabricate no isolated materializable intersection root.
7. Restore nonzero `k`, edit presentation style and verify semantic invariance.
8. Save/reopen `.cedg`; verify transformed identities, dependencies, styles,
   metrics, points and fresh transformed-query token authority.

The author records this smoke as PASS with the free-input compatible-redefine
limitation characterized and deferred. Candidate markers, workspace UX, G9U1,
G9B, G9C, G9U2 and productive G10 are outside R5.

# G9U1 native lifecycle author-review correction

Status: IMPLEMENTATION CANDIDATE — PENDING AUTHOR RE-REVIEW. No author PASS is
inferred from diagnostic execution.

## Preserved evidence and earliest failed contract

The exact author `TestBasic1.cedg` is retained unchanged (31,885 bytes; SHA-256
`0791895e1133d4a44ff26c88760cfc951db787c42056a8b5758c79a9b5687be0`). The archived
DEV03 reproduction reaches `SpatialIdentityRegistry.LoadSession.commit` and
fails the existing exact construction-DAG dependency check. This is distinct
from the truthful undefined partial metric: the free interpolation inputs A/C
do not have semantic-point endpoint provenance.

The participating list `l1` was registered before A/C acquired durable IDs. Its
stored dependency list remained empty when a later public operation explicitly
registered A/C. On reopen, the unchanged strict loader sees A/C as participating
direct `AlgoDependentList` inputs and rejects the stale empty record. The
interaction-owned point E has exact encoded direction/input bits; it is not the
earliest failed predicate.

## Producer correction (design before implementation)

The correction belongs to the shared registry's existing ordinary publication
transaction, not the XML loader, metric algorithm or frontend identity model.
When new concrete geo identities are published, inspect only existing
construction-defined records whose already-existing direct algorithm inputs
include those exact newly participating handles. First require that each old
record agrees exactly with the old registry/algorithm DAG. Then derive its
prospective dependency IDs using the existing provider contract and new
prospective attachments. Preserve its durable ID, provider, family, schema,
roles, revisions and copy provenance: no geometric definition, input edge or
semantic identity has changed; an existing input has become explicitly durable.

Install these derived records and the newly participating inputs in the same
existing prospective-graph/runtime switch. A failure leaves both old records and
attachments exact. No public generic record-replacement API or new serialized
field is introduced. Copy/import/load continue using exact supplied records;
they never repair a malformed archive. Redefine staging must not publish live
changes, and an affected existing record inside a sealed/claimed redefine scope
must fail before publication rather than bypass the G9A captured-context lease.
The existing RETAIN/FRESH compatibility and atomic rollback rules remain intact.

The dependency refresh scans current construction records and their direct
inputs during an explicit participation publication; it adds no geometric solve,
render dependency, nearest-point search or separate history. Discovery of
affected records is O(V + E) over current participating construction records and
direct edges. Existing exact dependency validation uses list-based duplicate
checks and sorting; including that work, affected records of degrees d incur
O(sum(d^2 + d log d)). This is not a claim that the entire transaction is linear;
publication already prepares the complete prospective registry graph.

## Native save boundary

The Desktop correction separately validates the existing temporary native archive
with `DocumentArchivePreflight` (a disposable kernel using the existing loader)
before atomic target replacement. It adds no recovery, schema migration or
loader exception. A failed preflight retains the prior target and unsaved state;
successful Save must produce an archive the same application can reopen. This
adds one fresh disposable reconstruction/solve only to explicit native Save, not
to ordinary recomputation. The overhead and failure preservation require tests.
The exact historical malformed author archive remains rejected and is never
silently sanitized.

## Validation perimeter

Required focused evidence: late participation after a registered dependent list;
sequential promotion of two inputs; unchanged IDs/revisions/roles; exact native
record reload after normal publication; failed publication rollback; malformed
historical record rejection; G9A staging/lease preservation; explicit native-save
failure preserving target/unsaved state; valid native save/reopen. Parent task
serializes all DEV/acceptance execution. No result is claimed before its run.

VERIFICATION_INFRASTRUCTURE_IMPACT: UPDATE_REQUIRED for the bounded successor
verification inventory; the original protected candidate's frontend-only
inventory remains historical. BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no runtime,
JDK, Gradle, external service or workstation prerequisite changes. GUIDE_IMPACT:
UPDATE_REQUIRED to distinguish semantic endpoints from free interpolation inputs
and explain truthful native save/open diagnostics.

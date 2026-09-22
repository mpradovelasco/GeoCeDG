# PRE-G9B-R1-R1 native periodic-quarantine round-trip candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R1-R1
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = BOUNDED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase G9U1
PRODUCT_PHASE_EFFECT      = NONE
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It deliberately
carries no author-approval field: the separate closeout/author-decision record is
the sole authority for current author approval status, and this file must never
require mutation because the author later approves the candidate it belongs to.

Implementation base: commit `d0c4bef1e9bd4677c90659598d60443a298caf2d`, tree
`341f31915ff675f7235cda0afcbf0d9c7b2e811f`, worktree clean at entry.
Machine-readable evidence:
[`geocedg/validation/pre-g9b-r1-r1/pre-g9b-r1-r1-quarantine-evidence.json`](../../geocedg/validation/pre-g9b-r1-r1/pre-g9b-r1-r1-quarantine-evidence.json).

## What the phase closes

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` carried three declared native
experiments that had no executable evidence, and `TD-R0-G9U1-Q02-BINDING`
recorded that one of them was bound to a test that did not perform its declared
procedure. All three now run through a real `.cedg` save and reopen.

`AD-R0-3` is honoured literally: every assertion goes through a genuine native
archive lifecycle, and **no kernel API was widened**. There is no XML
fabrication and no ledger-only export/import substitute.

### `U1-Q02` — binding reconciled

The bound method now performs the declared procedure before any release: it
saves the already-reopened quarantined document and reopens it a **second**
time while the periodic offset evidence is still insufficient, asserting an
unchanged serialized ledger, four claimed periodic-quarantine entries, no marker
solution, no eligible token, an empty `materializeAll`, unchanged point count,
unchanged persistent identities and unchanged selected root tokens. Only then
does the existing unique-zero release run.

### `U1-Q04` — proved-nonzero retirement

A fork of the byte-identical quarantined seed is driven to a phase that
establishes a nonzero cyclic offset. The observed contract:

| Clause | Evidence |
|---|---|
| retires **only** its affected group | the surviving consumers stay defined; retirement is partial, not wholesale |
| fails closed | returning the parameter to the exact phase that releases the whole group in `U1-Q03` does **not** revive a retired consumer |
| cannot retarget another root | every consumer keeps its exact selected root token across the move, the archive round trip and the return |
| no interactive creation | the `GeoPoint` count is unchanged at every step |
| identity preserved | every persistent `GeoId` is unchanged |
| durable | the retired outcome survives save/reopen, and a second save/reopen at the original phase |

One deliberate precision: the serialized ledger is **not** asserted byte-equal
across the retirement round trip. Reopening legitimately re-derives fresh
unclaimed `ACTIVE` allocations for the current roots the retired consumers no
longer own — the claimed entries keep their incarnations while the fresh ones
receive new ones, so no retired token is reused. The claimed
periodic-quarantine count is asserted instead, and token identity carries the
no-retargeting guarantee.

### `U1-Q05` — feature-off and Classic preservation boundary

The same seed is reopened headlessly on the feature-off GeoCeDG route and on the
separate Classic diagnostic route, then saved and reopened again on each. On both
routes the serialized token ledger is byte-equal to the seed, the serialized XML
is byte-equal across the re-save, `mayCreateLocusV2` is false, the app code stays
`CLASSIC`, every consumer stays dormant with its exact token and unchanged
persistent `GeoId`, and geo and identity record counts are unchanged.

## Coupled reconciliations

Two changes outside the test file were required consequences, not scope
widening.

**JUnit inventory.** Two new desktop methods necessarily change the pinned
test-identity sets, which the acceptance projection enforces. Regenerated with
the official [`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from `--test-dry-run` discovery evidence plus one completed passing `final.desktop`
run (`inner_exit_code 0`):

| Selection | Before | After |
|---|---|---|
| `discovery.desktop` | 1 517 | 1 519 |
| `final.desktop` | 1 511 | 1 513 |
| module `desktop` discovered | 1 517 | 1 519 |

The shared module is unchanged.

**Typed registry.** The registry pins the JUnit inventory catalog by
canonical-LF SHA-256, so regenerating the inventory requires repinning
`junit_inventory` from `190f2d46…` to `553cc04e…`. `PHASE G9U1` refused to run
until it was reconciled, reporting `Verification catalog identity mismatch:
junit_inventory`. The hash method was validated first by reproducing the
previous pin exactly from the base blob; no registry semantics changed.

**Standalone G9U1 verifier.** The script hard-coded a `RETAINED_RISK` exception
for `U1-Q04` and `U1-Q05`. Once those scenarios carry executable evidence that
exception contradicts the reconciled manifest, so exactly one line was removed
and both scenarios now fall under the ordinary executable-support rule. Every
other exception is unchanged, and no quarantine semantics were touched.

## Retained pre-existing baseline defect

```text
id    = BASELINE-G9U1-BRANDING-EVIDENCE-PIN
class = RETAINED PRE-EXISTING BASELINE DEFECT
state = OPEN — NOT REPAIRED IN PRE-G9B-R1-R1
```

The standalone `verify-g9u1-construction-workspace.ps1` **already fails at the
authorized base**, before any change in this phase, with a clean worktree:

```text
Branding evidence pin differs:
  source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/helixSnapshot.png
```

The script constants `Round3BrandingResourcePins` were moved forward to the
current author branding assets, while the immutable G9U1 Round-3 evidence JSON
still records the superseded values; the script asserts the two agree.

| Asset | Historical evidence | Script constant and live file |
|---|---|---|
| `helixSnapshot.png` | 251 689 / `abcf272553` | 1 184 851 / `19ff4bf40b` |
| `geocedg-startup-542x720.png` | 155 029 / `04d5e79b99` | 636 733 / `8add7345db` |

Origin is author commit `ed7558ea1`, which replaced the branding sources without
reconciling the historical evidence record. Repair would mean either mutating
immutable historical G9U1 author evidence or reverting verification constants —
both separate author decisions outside this phase.

This failure is **not part of the frozen acceptance**: the registry's `G9U1`
phase selection requires only ordinary compile, junit, python and static nodes,
and the standalone script is not among them. Under the operational rule promoted
in the preceding consolidation, a complete heavy-verification failure proven to
predate the candidate and lie causally outside its delta is retained baseline
debt, not phase scope.

A consequence worth stating plainly: the `--tests` dispatch the prompt names sits
inside that standalone script, after the failing contract assertion. The two new
methods are therefore executed by the frozen `PHASE G9U1` acceptance, through
`junit.desktop.final.semantic`, rather than by the standalone verifier.

## Risk record

```text
G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
```

**This phase does not move it to resolved.** The three declared native-lifecycle
experiments now have executable evidence through real `.cedg` cycles, with no XML
fabrication, no ledger-only substitute and no kernel API widening. The author may
consider the evidence obligation met; only the author may resolve the record.
Residual for the author: the corresponding GUI smoke, and any scenario variants
not exercised by the named methods.

## Impact

```text
PRODUCT_PHASE_EFFECT               = NONE
KERNEL / QUARANTINE / RESOLVER     = NONE
LEDGER FORMAT / SERIALIZATION      = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = NONE
```

The quarantine mechanism is observed, never changed. `GUIDE_IMPACT` is `NONE`
because the phase adds tests and reconciles operational manifests only; no
user-visible behaviour and no public tool changed.

## Authorization state

`PRE-G9B-R2`, `R2-E0`, `R2-E1`, `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10`
and further `G12` remain unauthorized. Global `G9` closeout is not authorized. No
successor phase is authorized. Push, branch publication, merge, promotion, tag,
release and binary publication are outside this phase and each requires a
separate explicit author instruction naming the exact candidate SHA.

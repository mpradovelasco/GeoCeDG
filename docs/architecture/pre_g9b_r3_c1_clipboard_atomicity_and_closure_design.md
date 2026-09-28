# PRE-G9B-R3-C1 clipboard atomicity and predecessor closure

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PHASE                     = PRE-G9B-R3-C1
KIND                      = CORRECTNESS REPAIR OF APPROVED CONTRACTS; no new semantics
```

This record documents the repair of the two `PRE-G9B-R3-C1` debts,
`TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` and
`TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE`. It introduces no semantic
choice. It restores behaviour that the approved contracts already require:

- the [G9A1 design](g9a1_spatial_identity_persistence_design.md): the
  `CLIPBOARD_IMPORT` purpose "require[s] a complete declared closure and remap[s]
  it once", and copy "expands the selected semantic component before ordinary
  construction-DAG predecessors";
- [ADR 0011](../adr/0011-g9-spatial-persistence-and-phase-gates.md) class F: fresh
  IDs and copy lineage for the complete copied semantic closure, with every copied
  reference rewritten through one table;
- the copy row of the
  [G9 spatial projection semantics](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md)
  lifecycle table;
- the runtime rollback protocol of `SpatialSemanticRuntime`, as already used by
  `MyXMLio.restoreRejectedSpatialParse` and `Construction.restoreSpatialRedefineSnapshot`.

Characterization, tests and evidence are in the
[candidate report](../validation/pre_g9b_r3_c1_candidate_report.md) and the
[validation matrix](../validation/pre_g9b_r3_c1_validation_matrix.md).

## 1. C1-A: rollback protocol

### Causal chain on the base

1. `InternalClipboard.pasteGeoGebraXMLInternal` (shared) and
   `CopyPasteD.pasteFromXML` (Desktop) save the script policy, set
   `blockUpdateScripts = true` and call `evalClipboardXMLAtomically`.
2. The identity-bearing import fails, for example with `MALFORMED_RECORD`
   ("Construction identity base contract disagrees with the attached geo") for an
   incomplete payload.
3. `MyXMLio.doParseXML` restores its own entry snapshot through the approved
   protocol (`beginRollbackRestore`, `ROLLBACK_RESTORE`, parse,
   `finishRollbackRestore`) and rethrows the failure. The construction, identities,
   records, DAG, spatial section, rich-result ledgers and undo history are exact
   again.
4. The outer catch of `evalClipboardXMLAtomically` sets `ROLLBACK_RESTORE`
   without `beginRollbackRestore`. `kernel.clearConstruction` calls
   `SpatialSemanticRuntime.clearForRollbackRestore`, which throws "Spatial rollback
   restore is not active".
5. That throw happens in `doParseXML` before its `try`. Its `finally` never runs,
   so kernel host state leaks: `loadingMode = true`, `notifyViewsActive = false`,
   command lookup `XML`.
6. The thrown `IllegalStateException` ("Clipboard identity rollback could not
   restore the construction") carries only the secondary failure; the original
   import failure is lost.
7. Neither paste entry point restores the script policy on an exception, so
   `blockUpdateScripts = true` leaks. It is saved as `<scripting blocked="true">`
   and survives reopening.

### Repair

- `evalClipboardXMLAtomically` restores its snapshot through a private
  `restoreClipboardSnapshot` with the approved protocol:
  - `beginRollbackRestore`, then `ROLLBACK_RESTORE`, then the parse;
  - in `finally`, clear the purpose and call `finishRollbackRestore(restored)`.

  No runtime guard is weakened or bypassed.
- If the restore itself fails, the `IllegalStateException` keeps the restore
  failure as its cause and the import failure as a suppressed exception. This is
  the pattern of `Construction.runAtomicConstructionMutation`. A successful restore
  returns `false` and logs the import failure as text.
- Both paste entry points restore the exact previous script policy on every
  non-success exit of the import: rejection, failure, or a throwable from the
  boundary. Nothing forces `false`: a policy that was `true` stays `true`. On
  success the policy returns at the unchanged upstream point.

The outer restore still runs after an inner restore, because its snapshot is
taken before any caller mutation. The Desktop Insert File overwrite passes that
snapshot and mutation (`spatialInsertRollbackXml`, `spatialInsertMutation`)
through the same method. So the repair covers Insert File mechanically, and it
needs no separate transaction model.

## 2. C1-B: predecessor closure

### Causal chain on the base

For `f = Line(C, xAxis)`, `D = Intersect(f, yAxis)`, `a = LocusV2(D, C)`, copying
only `a` runs, in both `InternalClipboard.copyToXMLInternal` and
`CopyPasteD.copyToXML`:

| Step | Owner | Result |
|---|---|---|
| semantic closure, no predecessors | GeoCeDG | `a, c, C, D` |
| `CopyPaste.addPredecessorGeos` | upstream | adds the predecessor geos `A, f` and the parent algorithms of the listed geos only |
| `addAlgosDependentFromInside` | upstream | adds an algorithm only when every input is listed; `xAxis` never is, so `Line(C, xAxis)` is not added |
| semantic closure, with predecessors | GeoCeDG | runs `addSpatialPredecessors` only for geos added in this pass; the first pass already added them all |

`f` is therefore renamed (`CLIPBOARDmagicSTRINGf`) but never serialized, because
the fragment is written from the construction steps, and a dependent geo is
written through its parent algorithm. Two outcomes follow on paste:

- when a participating record depends on the omission, identity validation fails
  and the C1-A chain follows;
- when only a helper without identity depends on it (for example
  `M = Midpoint(D, P)` in the closure of a participating circle), the command fails,
  the host's XML handler collects the error, and the following `<element>` creates
  `M` as **free** geometry. The paste reports success. This is the silent
  degradation of the characterization.

### Repair

- `InternalClipboard.addSpatialClosureParentAlgorithms` completes an
  identity-bearing payload with the parent algorithm of every copied labeled geo
  whose parent is missing. It adds that algorithm's missing outputs as hidden
  side-effect outputs, as upstream does for the algorithms it adds.
  - Construction constants are never copied and never acquire identity; commands
    reference them by name, which resolves in every construction.
  - `AlgoTableToChart` keeps its upstream exclusion.
  - Both copy pipelines call it only when the payload is identity-bearing, that is
    when the closure has identity records.
- `evalClipboardXMLAtomically` rejects an identity-bearing import whose parse
  reported host rebuild errors, with `INCOMPLETE_CLOSURE`, before any label
  handling or undo. The snapshot restore of C1-A then returns the exact prior state.
  - This is the fail-closed rule for any closure that still cannot be represented,
    for example a payload produced by an older build.
  - It follows the upstream `Construction` redefine precedent, which also rejects
    and restores when `getXMLio().hasErrors()` is set.

## 3. Rejected alternatives

| Alternative | Reason |
|---|---|
| treat constants as copied in upstream `addAlgosDependentFromInside` | changes generic GeoGebra copy for every document, including documents without identity |
| run `addSpatialPredecessors` for every closure geo inside `addSpatialIdentityClosure` | `Macro.buildMacroXML` uses the same method; it would pull the ancestry of macro inputs into macro templates |
| give axes durable identity so that they join the closure | forbidden: constants must not acquire CeDG identity to satisfy closure |
| set `blockUpdateScripts = false` after a failure | overwrites a policy that was `true` before the paste |
| skip the outer restore after an inner restore | the outer snapshot also covers the caller mutation of Insert File |
| reuse `Construction.runAtomicConstructionMutation` | it takes its own undo-XML snapshot and cannot use the caller's pre-mutation snapshot |
| check completeness in `MyXMLHandler` | upstream parser shared by every load purpose; the clipboard seam already owns the import plan |
| weaken `SpatialSemanticRuntime` rollback checks | forbidden; the caller now enters and leaves the protocol |

## 4. Placement and compatibility

- **Layer:** shared Java clipboard/import seam (`InternalClipboard`) plus the
  Desktop adapter's own script-policy exit (`CopyPasteD`). Desktop duplicates no
  transaction semantics and no geometry moves into a frontend.
- **Upstream-owned paths:** `InternalClipboard.java` and `CopyPasteD.java`, both
  already modified for G9A1 and recorded in `docs/upstream/modified-files.yml`.
- **Serialization, identity format, `schemaVersion` 1/2, migration and lineage
  semantics:** unchanged. Payloads gain only the host commands their renamed
  labels already referenced. Historical documents open unchanged.
- **Documents without identity:** unchanged. The completion step and the rejection
  guard apply only to identity-bearing payloads.

## 5. Not changed by C1

- In a document without durable identity, upstream copy still omits a
  predecessor command with a constant input, and the paste degrades that geo to
  free geometry. Changing it would change generic GeoGebra copy semantics. It is
  registered as `TD-CLIPBOARD-UPSTREAM-CONSTANT-INPUT-DEGRADATION` for author
  disposition.
- `CommandNotLoadedError` (web lazy command loading) is an `Error`, not a
  `MyError`, and does not enter the outer restore. For an identity-bearing parse,
  the approved inner `MyXMLio` restore still applies and rethrows it. Web paste
  carries no caller mutation, and Desktop loads every command, so no C1 invariant
  is violated. This is recorded as an observation only.
- `MyXMLio.doParseXML` calls `clearConstruction` before its `try`. The C1 protocol
  repair removes the characterized trigger. The upstream structure itself is left
  unchanged.

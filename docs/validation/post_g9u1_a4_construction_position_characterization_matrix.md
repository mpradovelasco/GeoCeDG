# POST-G9U1-A4 construction-position characterization matrix

- Status: **RESEARCH / DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Base commit: `c0a018ab01c0b44a7ed1fb33010bf2481e30ac2a`
- Product phase effect: **NONE**
- Product code changed: **false**
- Guide impact: **NO**
- Focused test:
  `org.geocedg.common.spatial.PostG9U1A4ConstructionPositionCharacterizationTest`

This matrix records current host behavior only. It is not PHASE acceptance and
does not authorize or implement POST-G9U1-A3.

| Obligation / question | Evidence | Result |
|---|---|---|
| Separate construction order from update order | `Construction.ceList` and `Construction.algoList` are distinct collections | OBSERVED |
| Numeric index derives from order | `updateConstructionIndex()` renumbers `ceList` after add/move/remove | OBSERVED |
| Legal movement respects DAG | `moveInConstructionList()` enforces min/max construction bounds | OBSERVED |
| XML preserves construction order | construction XML iterates `ceList`; focused save/reopen restores all four checked indices | PASS |
| Durable identity survives reopen | focused reopen resolves the same `PersistentGeoId` | PASS |
| Java identity is not durable | focused reopen reconstructs every checked Java object | PASS |
| Rollback restores exact construction snapshot | rejected redefine produces byte-equal `getCurrentUndoXML(false)` and exact checked indices | PASS |
| Rollback restores dependencies | dependent algorithm input resolves to the reconstructed target | PASS |
| Rollback restores durable identity | reconstructed target resolves the original `PersistentGeoId` | PASS |
| Rollback may replace Java instances | every checked pre-failure object differs by Java reference after reconstruction | PASS |
| Protocol/navigation is not rollback authority | prior `Construction.step` is not restored by the operation-local undo XML; reconstruction ends at the last step | PASS |
| Compatible retain can move procedurally | admitted new-instance `RETAIN` moves target from before to after a surviving independent neighbor | PASS |
| Compatible retain remains topologically valid | every new input precedes the resulting target | PASS |
| P1 exact numeric slot | Rejected: mutable index is renumbered by unrelated list mutation | CHARACTERIZED |
| P2 topology only | Insufficient: it permits crossing an unaffected independent neighbor | CHARACTERIZED |
| P3 procedural anchors | Selected design candidate: stable relative interval, subordinate to DAG bounds | DESIGN CANDIDATE |
| P4 current transaction sufficient | Rejected by focused compatible-redefine evidence | CHARACTERIZED |
| Undo/redo Protocol-step restoration in every GUI configuration | Not required for semantic contract and not exhaustively exercised | UNKNOWN / NOT ESTABLISHED |
| Stable `algoList` identity/order across every rebuild family | Not serialized as an independent authority and not exhaustively exercised | UNKNOWN / NOT ESTABLISHED |

## Focused scenarios

| Test method | Existing host operation | Characterized outcome |
|---|---|---|
| `compatibleNewInstanceMayMoveAfterAnIndependentNeighbor` | Current G9A3 target-based compatible redefine | Durable identity retained; Java instance replaced; dependencies valid; relative independent-neighbor order changes |
| `failedRedefineRestoresExactOrderByXmlButNotJavaOrNavigationStep` | Current provider rejection plus operation-local rollback | Exact construction XML/order, durable ID and DAG restored; Java references and earlier navigation step not preserved |
| `saveReopenReconstructsExactOrderAndDurableIdentityWithNewInstances` | Full document XML reopen | Exact serialized order, durable ID and DAG reconstructed with new Java instances |

## Non-authorities explicitly excluded

No test or conclusion uses label equality, coordinates, proximity, render state,
Construction Protocol row, construction index, XML position or Java reference
as durable semantic identity. The labels in the focused fixture are lookup
handles only; the assertions of continuity use `PersistentGeoId` and explicit
DAG relations.

No product defect was fixed or suppressed. The observed relative movement is
the reason A3 needs a bounded procedural-position mechanism if the P3 design
candidate is approved.

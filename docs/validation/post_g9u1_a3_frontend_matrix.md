# POST-G9U1-A3-FRONTEND Classic 5 validation matrix

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Base: `5feb94089991fbebf7395d5ad21c8fd5c5df745c`
- Required profile: `PHASE -Phase POST-G9U1-A3-FRONTEND`
- Client: Classic 5 Desktop only
- Web changed: **false**
- Kernel semantics changed: **false**
- Interactive smoke: **NOT EXECUTED — ENVIRONMENT LIMITATION** (the current
  agent surface cannot drive a native Classic 5 modal reliably)
- Self approval: **false**

| Obligation | Focused evidence | Expected result |
|---|---|---|
| Compatible retain | `advancedRetainAndForcedRelocationNeedNoLegacyConfirmation` | Executes only advanced retain; durable source ID remains |
| P3-R1 relocation | same | Later predecessor forces the approved kernel relocation without legacy choice |
| Explicit legacy boundary | `legacyCancelPresentsCompleteImpactAndLeavesConstructionUnchanged` | No mutation before confirmation; cancel restores the exact construction |
| Complete typed impact | same | Every kernel impact entry and all typed durable fields occur in the localized presentation model |
| Fresh identity and Undo | `explicitLegacyAcceptanceCreatesFreshIdentityAsOneUndoOperation` | Explicit acceptance selects legacy replacement, creates a fresh source ID and one Undo restores the old ID/state |
| Stale choice | `staleConfirmationNeverExecutesAndNextSubmissionUsesNewAssessment` | Stale selection is discarded; a later submission obtains a distinct assessment and asks again |
| Invalid/non-offerable statuses | `invalidAndNonOfferableStatusesNeverExposeLegacyExecution` | No legacy mode for cycle, host-incompatible, unsupported, ambiguous or stale states |
| Advanced status mapping | `frontendMapsBothAdvancedStatusesOnlyToAdvancedMode` | Both advanced statuses map only to `ADVANCED_RETAIN` |
| Ordinary GeoGebra redefine | `ordinaryClassicGeoGebraRedefineRemainsUnchanged` | Nonparticipating numeric follows unchanged host behavior and opens no semantic dialog |
| Existing G9U1 affordances | `G9U1DefinitionAffordanceTest`, `G9U1AlgebraSubmissionTest` | V2 source definitions use A3 preflight; rich result objects remain read-only |
| Kernel seam regression | selected A3-R1 assessment tests | Explicit mode, staleness, complete impact and atomic lifecycle remain kernel-owned |

The dialog does not navigate, rebind or retain a `GeoElement` handle. It displays
durable typed entries directly and derives localized prose only from closed
status/reason/recovery codes. Interactive smoke is recorded separately and is
not a substitute for the bounded PHASE result.

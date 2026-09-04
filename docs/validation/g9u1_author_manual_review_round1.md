# G9U1 — author manual review, round 1

- State: **COMPLETED WITH FINDINGS — NOT PASS**.
- Reviewed candidate: `b492194082f1adc9f981d85d92a58ef57490196f`.
- Immutable annotated checkpoint: `geocedg-g9u1-author-review-checkpoint-1`;
  object `755f22bd2b101d4ca2ad6bea98429bc2ba941af9`, peel as above.
- Successor: `codex/g9u1-author-review-stabilization-1`.
- Product main remains `f8a21a087234b18fc13741a0ac2baf80608e9022`.
- `authorApprovedImplementation=false`, `passClaimedImplementation=false`,
  `selfApproved=false`; a new author re-smoke is required.

## Author observations (not inferred automated results)

The functional candidate is very mature; the author does not reject its
architecture. Frontend organization is not acceptable as final UX. The broad
button-like horizontal menu strip should become normal application menus above
a compact, high-frequency toolbar. Application menus are the discoverability
superset; the toolbar is a curated subset. Templatev7 is workflow evidence,
not a layout or macro bundle to copy.

Some Locus V2 icons are good and should be preserved. Spline V2, semantic-curve
operations and appropriate rich-result/materialization actions need a coherent
owned icon family, with accessible text and DPI support.

In `artifacts/smoke-test-g9u1/TestBasic1.cedg`, the author entered
`Length(b,A,C)` and observed `?`; hover/definition showed approximately
`Length(o)`. Metric validity and opaque hidden-parent presentation are separate
findings. The rich parent must remain the real authority.

During the session, some objects disappeared after an unspecified operation.
Ctrl-Z recovered them and work continued. On exit the author answered YES to
Save; subsequently reopening the same file reported `Opening file failed`.
The exact preceding gesture and an earlier good archive were not supplied.
This is a high-severity persistence finding until reproduced and characterized.

Ordinary objects should retain legal editing. Semantic definition inspection
may correctly be read-only, but Algebra, Properties and context menus need
coherent, nonredundant affordances and explanations. No unrestricted semantic
redefine or new identity predicate is authorized.

The author additionally requires a GeoCeDG-profile persistent user-tool route,
using the host macro/.ggt engine, explicit installation and removal, startup/new
document availability and optional toolbar placement. Document-local macros must
not auto-install; Templatev7 tools require curation and provenance, and do not
become native kernel commands. A short practical guide and a coherent re-smoke
checklist are required.

## Preserved author archive

- Original: `artifacts/smoke-test-g9u1/TestBasic1.cedg` (untouched).
- Durable test copy:
  `source/desktop/desktop/src/test/resources/org/geocedg/desktop/g9u1-review/TestBasic1.cedg`.
- Length: **31,885 bytes**.
- Raw SHA-256: `0791895e1133d4a44ff26c88760cfc951db787c42056a8b5758c79a9b5687be0`.
- Initial read-only inspection: all five ZIP entries decompress; `geogebra.xml`
  is well-formed XML (51,638 bytes). This does not prove semantic open success.
- `b=SplineV2(l1,a)`, `l1={A,B,C,D}`, `a=3`.
- `A=(2.36,3.04)` and `C=(9.44,3.24)` are serialized free input points;
  they are not serialized semantic-position children of `b`.
- `o=LocusLength(b,A,C)`; `q=Length(o)` is serialized `NaN`.
- The archive contains an interaction-owned point `E`, transformed curves,
  rich intersection results and materialized exact-token points.

## Investigation and validation boundary

Archive readability, endpoint admissibility, public definition presentation and
the producing save/undo lifecycle must be tested independently. No archive
repair, tolerance change, numerical admission expansion or identity/schema
reinterpretation may be used to hide the failure. Subsequent diagnostics and
fixes are recorded as new evidence, never as a rewritten author PASS.

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains **OPEN / TRACKED**.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: this review uses the existing Windows,
JDK, Gradle and PowerShell verification entry points and introduces no new
workstation prerequisite. GUIDE_IMPACT — UPDATE_REQUIRED: public workflow,
menu placement, read-only/editable boundaries and user-tool installation need
the requested practical guide. Final verification level will be selected from
the actual stabilized executable/verification delta under ADR 0020/0024;
the old candidate's FULL is not evidence of newly changed inputs.

## Reduced reproduction and disposition

DEV03 reproduced the strict native-load failure before correction:
`MALFORMED_RECORD: Construction identity dependencies disagree with the
prospective algorithm DAG`, subject `geo:b8933eecf9f7df32d9192b9b2312e198` (`l1`).
The list acquired identity before A/C did; later input participation left its
stored dependency signature stale. The bounded producer refresh and atomic-save
preflight are specified in [native lifecycle review](g9u1_native_lifecycle_review.md).
No new schema, migration, ID/redefine predicate or permissive loader is added.
The exact historical archive remains rejected. The unspecified disappearance
gesture was not reconstructed; the reduced participation sequence explains the
reopen predicate and is tested across undo/redo and two native reopens.

The separate reduced metric payload is correctly defined as a rich result with
`INVALID_QUERY / ABSENT / INCOMPLETE / TARGET_NOT_REACHABLE`, current source
revision, no finite value/guarantee and a diagnostic requiring exact semantic
addresses. Free A/C lack those addresses; `Length(b,A,C)` remains `?` rather than
inventing a preimage. Its ordinary definition now expresses `Length(b,A,C)`;
the actual one-rich-parent DAG and XML `Length(o)` stay unchanged. Valid explicit
semantic endpoints yield 2 in the straight-spline control and update dynamically.

## Development evidence (not acceptance)

- `artifacts/g9u1-review-round1/dev-archive-01`: failed in the sandbox Gradle
  environment; no product correction inferred from that environment failure.
- `dev-archive-02`: failed compilation during an unfinished edit; concurrent
  candidate mutation also invalidated that development attempt. It is not PASS.
- `dev-archive-03/dev/b51cf3cb71574db2840cee7ce0a318e0/dev-gradle.log`:
  one expected diagnostic failure established the malformed native record above.
- `dev-participation-01`: exit 0, seven shared transaction/producer regressions.
- `dev-review-01/dev/ac7bee9915184992bd8dcaa2078ea8e8/dev-gradle.log`:
  exit 0, 76 Desktop tests, zero failures/skips; includes five metric and two
  native lifecycle tests, 21 user-tool executions and menu/profile/affordance
  checks. Later method-shape and bounded startup/guide assertions require the
  final rerun; these DEV results are not substituted for that source cohort.

Agent GUI observation used the canonical `runGeoCeDG` task with
`--enableLocusV2=true` and a separate `artifacts/g9u1-review-round1/` settings file.
Six normal application menus and the compact flyout toolbar were observed in
an empty document; Construction and Automation menus were opened, and the
application exited successfully. An inherited automatic Classic-perspective
popup was discovered and assigned a product-only startup correction before
the final freeze. No author preference/document was modified by this session.
This limited agent observation is **not** author re-smoke PASS or exhaustive GUI
acceptance. [The checklist](g9u1_author_resmoke_checklist.md) remains blank.

A second fresh-settings launch after the startup correction compiled and exited
successfully (Gradle exit 0). Its capture was occluded by another application;
no clean visual confirmation of the corrected popup is claimed. The dedicated
startup callback regression and the pending author visual check remain distinct.

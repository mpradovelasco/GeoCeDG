# PRE-G9B-MAINT-A-N2 — candidate validation matrix (revision R3)

| Field | Value |
|---|---|
| Status | **CANDIDATE — READY FOR AUTHOR ADOPTION (revision R4).** `selfApproved=false`. A plan of tests, not evidence: no case is executed by `PRE-G9B-MAINT-A-N2` or its refinements R1, R2, R3 and R4 (R3 ran scratch budget probes as evidence, not as these cases) |
| Revision | R4 (2026-11-26): new RC-25 (plane normal groups and presence; case (f) for the import record under author decision D-R4-1) and RC-26 (`values` counter); RX-15 case (e) through the real controller. R3 (2026-11-25): C-4, RC-23 and RC-24 at the measured budget; RX-11, RX-12, RX-15 and RX-19 with the sidecar situations and the references-only rule; AP-11 wording. R2 (2026-11-25): RX-4, RX-16 and the new RX-19 follow the author decisions RX-D18 and RX-D19. R1 (2026-11-25): resource-policy cases RC-23 and RC-24; atomic routes and Classic opaque preservation (RC-14, RC-18, RC-20); no derivation history (RC-9, AP-8, AP-10); export state model and conformance with AutoCAD Core Console and ezdxf (RX-5, RX-8, RX-16 to RX-18). The A-N2 text is commit `874286e95` |
| Contracts covered | [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md) (RC); [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md) (RX); [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md) (AP, NUM); layer separation (SEP) |
| Executing increments | `MAINT-A-DXF0-RC-S0` (C-1 to C-3 and the current-behavior part of C-5), `MAINT-A-DXF0-RC` (RC, and C-4 and the budget part of C-5 as its closeout gates), `MAINT-A-DXF0-EXP` (RX except RX-7 and RX-18, with blocks as fixtures read through DXF1-M; SEP-6, SEP-9), `MAINT-A-DXF1-S0` (NUM), `MAINT-A-DXF1` (AP, NUM, RX-7, RX-18, SEP-7, SEP-8); `MAINT-A-DXF0-RC` also runs SEP-5, `MAINT-A-DXF1b` (AP-12, AP-13); none is authorized |
| Identifiers | AP-1 to AP-6 keep the intent of the reserved rows of the [A-N1 matrix](pre_g9b_maint_a_n1_validation_matrix.md) §3, corrected (no ordering between measures, approval-record observation AP-2); SEP-5 onward continue SEP-1 to SEP-4 of that matrix |

## 0. Rules for every case

- The A-N1 matrix §0 rules apply: references independent of the implementation (exact
  rational arithmetic on the binary64 inputs, closed forms, a separately written evaluator);
  «exact» means equality of binary64 data (bit patterns where a case says bit-identical,
  `==` where it says equal) or of a structural fact, never of a floating evaluation; two runs
  give identical results and diagnostics; a typed rejection publishes nothing.
- Binary64 comparisons use `Double.doubleToRawLongBits`; each case states whether the sign of
  zero is compared.
- Adversarial values (set **ADV**; for the export cases RX-3 and RX-4 only the members whose
  tokens keep at most two exponent digits apply, the others belong to RX-19): `0.1 + 0.2`, `1/3`, `2/3`, `100/3`, `nextUp(1)`,
  `nextDown(1)`, `123456789.12345678`, `nextUp(1e15)`, `1e16 + 2`, `2^53`, `2^63`,
  `Double.MIN_NORMAL`, `nextDown(MIN_NORMAL)`, `4.9E-324`, `Double.MAX_VALUE`, `π`, `e`,
  `-0.0`, a knot `k` with `nextUp(k)` in the same vector, values that need 17 significant
  digits.
- Export states are built **by identifiers** (provenance blocks and store), never by placing
  geometry.
- Document round trips compare canonical XML, the archive entry inventory and entry hashes,
  and, for the store entry, its bytes.
- Resource cases record JVM version, heap and workstation with their timings.

## 1. Characterization (before code in RC-S0; C-4 and C-5 at the budget as closeout gates of RC)

| Id | Increment | Case | Expected evidence |
|---|---|---|---|
| C-1 | RC-S0 | every route of store §7.1 with a failure injected after parsing | the live document and store unchanged or fully restored; the install never happens in `commitLoadedDocument`; non-transactional routes identified for the native transaction |
| C-2 | RC-S0 | isolated GeoCeDG 1.2.2 with a document carrying the entry | opens; ignores the entry; registers the empty image; drops the entry on re-save |
| C-3 | RC-S0 | GeoCeDG Classic with the entry, every save route (Save, Save As, `.ggb`, base64, HTML) | opaque preservation byte for byte, or the case for the save barrier on every route |
| C-4 | RC, closeout gate (on its implementation; not in RC-S0) | full Open → Save → Reopen cycle with the previous document held, at 100, 1 000 and 10 000 typical references and at the budget: a representative corpus, near-maximal references and the worst admissible case, 10 000 references at `maxValues` with the whole aggregate string allowance used (100 % of `maxEntryBytes`, not measured in R3); reader-safety cases: honest over-budget store, under-declared store, inflation bomb, a single attribute of maximal length within the entry budget | time, retained and peak heap, minimum-heap sweep on 1 GiB, compared with the R3 measurements (R3 report §3); the budget is confirmed or revised on this evidence; no linear extrapolation; the absence of an `OutOfMemoryError` alone is never a margin |
| C-5 | RC-S0 (current behavior) and RC, closeout gate (at the budget) | expert inspection, expert worker, angle-mode check of Insert File, recovery snapshot: in RC-S0 with a document carrying the entry, on the unchanged product; in RC with stores at the budget, on the implementation | RC-S0: the current refusal or acceptance recorded per route; RC: the entry recognized and validated under the policy; no 4 MiB refusal |

## 2. Reference store `cedg-reference-store/v1` (`MAINT-A-DXF0-RC`)

| Id | Case | Property | Contract |
|---|---|---|---|
| RC-1 | Save/reopen of the six weight representations of DX-13, rational quarter and half circle, a cubic with interior knots, a non-identity transformation, ADV values in coordinates and knots | every stored field bit-identical (sign of zero included); same `id`, order, visibility, name, preference, diagnostics; recomputed nature, weights, closure; tessellation recomputed identical | §5, §9 |
| RC-2 | Save → reopen → save | the entry bytes identical; no timestamp | §5 |
| RC-3 | Empty store | no entry; inventory and entry bytes equal to the base writer | §5.4 |
| RC-4 | Corpus of existing `.cedg` and `.ggb` documents | canonical XML and entries unchanged after reopen and save; no unknown `geocedg_*` entry in the corpus | §11, RC-D8 |
| RC-5 | `contract="cedg-reference-store/v2"`; an unknown `geocedg_*` entry; two store entries | typed cause; previous document restored (construction, store, current file, saved state, title) | §10 |
| RC-6 | Malformation matrix: missing, unknown or duplicated attribute or element; text between elements; DOCTYPE and external entity; non-canonical integer; non-finite bit pattern; list length different from `17k − 1`; `w` without `EXPLICIT`; `count` or `values` mismatch | `MALFORMED_STORE`; previous document kept | §5, §10 |
| RC-7 | Integrity: one bit of `data`; one digest; a duplicated `id`; invalid UTF-8; a record failing an import predicate | `TRANSFORM_MISMATCH`, `DIGEST_MISMATCH`, `DUPLICATE_ID`, `MALFORMED_STORE`, `INADMISSIBLE_RECORD` | §9 |
| RC-8 | Save with an invalid store injected | rejected by the preflight; previous file byte-identical; document unchanged | §10 |
| RC-9 | Import, hide, show, rename, preference, remove; a conversion and a failed approximation (when A-DXF1 exists) | no Undo step; Undo/Redo never touch the store; Undo snapshots keep one entry; A-DXF1 success or failure leaves the store bytes identical | §7 |
| RC-10 | Copy/paste between documents with references on both sides | no reference transported; both stores unchanged | §7 |
| RC-11 | Insert File from a document with references, and with a `v2` store | receiver store unchanged; insertion rejected for `v2` | §7, RC-D6 |
| RC-12 | New, Close, Close without saving, drop of a file, Reset without file | the save prompt appears when only the store changed; the file is unchanged without saving | §8 |
| RC-13 | Failed open of another document while the store is full | live store unchanged | §7.1 |
| RC-14 | GeoCeDG Classic: open a document with a store, edit unrelated content, Save, Save As `.cedg` and `.ggb`, base64 and HTML export, reopen in GeoCeDG | the entry bytes are preserved on every route; or, under the fallback, every save route is refused with a message; never a silent loss | §7.2 |
| RC-15 | Independence | construction XML, DAG, spatial identity and semantic exports identical with and without a store | §0 |
| RC-16 | Identity | the same file imported twice gives different `id`s; ids stable after Save As; equal data digests only give `SAME_DATA_AS_REFERENCE` | §6 |
| RC-17 | Mutation against the budget | an import that would exceed the budget is rejected as a whole; a rename or preference change past it is refused; nothing changes and the store stays savable | §5.3 |
| RC-18 | Replacement routes: `setBase64`, `.html` with embedded base64, `loadURL` of archive bytes, `base64://`, API clearing `setXML`, non-API clearing `setXML` (tool creation, preference reload), API `getGGBfile`/`getBase64` | the store round-trips; installation only with a successful construction; on failure construction and store restored together; non-API reloads leave the store alone | §7.1 |
| RC-19 | Loss of the external file | after save, move or delete the DXF; reopen restores every reference and its details | §0 |
| RC-20 | Clipboard-image insertion recovery; recovery snapshot | the live store never changes; deterministic serialization of the snapshot | §7 |
| RC-21 | Strings with non-ASCII, control characters and backslashes in layer, handle, base name and name; a string of exactly 1 024 bytes after escaping and one of 1 025 | stored and read back identical up to 1 024 bytes; saving never fails for an admitted record; an import or rename over 1 024 bytes refused with `STRING_TOO_LONG`; a stored string over 1 024 bytes rejects the document with `MALFORMED_STORE` while it is read; never truncated | §5.2, §5.3, §10 |
| RC-22 | Removal with live derived constructions | confirmation names the count; constructions remain; their provenance becomes orphan (export case 8) | §7 |
| RC-23 | Budget: a store at the initial budget (10 000 references, 1 500 000 values, entry bytes `17 × maxValues + 1 KiB × maxReferences`) accepted; a store above it rejected with `STORE_BUDGET_EXCEEDED` naming the required values, file unaltered, previous document kept; the same store accepted after an explicit administrative increase; preflights do not tessellate | configurable, explicit, never truncating; full-cycle minimum heap at the budget within the margin measured by C-4 | §5.3 |
| RC-24 | Reader safety: an entry that inflates beyond the byte budget (ZIP bomb), a single attribute longer than its bound, a declared `count`/`values` larger than the budget, a declared size smaller than the content, a hostile list length | rejected by the streaming reader at the root counts or at the byte counter (`STORE_BUDGET_EXCEEDED`), at the attribute bound or when the content exceeds its declared counts (`MALFORMED_STORE`); heap bounded by those bounds (R3 measured 0.3 MiB for an honest over-budget rejection in streaming mode); no partial store | §5.3, §9 |
| RC-25 | Plane normal (R4): DXF entities with (a) none of the groups 210/220/230; (b) the three groups, including the explicit triple `0 0 1` and `0 0 0`; (c) each partial subset (`210`, `220`, `230`, `210 220`, `210 230`, `220 230`), with `-0.0` and a non-default value; (d) the effective normal requested for presentation in each case; (e) a group repeated within one entity; (f) (D-R4-1) the import record itself for (a) to (c), including a present `0.0` and a present `-0.0`, compared with the base importer; then save, reopen and save again | (a) `normalGroups="none"`, no `normal`; (b) three values exactly as read; (c) only the groups present and their values, never `0.0` or `1.0` for an absent group, so that `230` alone and `0 0 v` stay distinct; (d) the effective normal is derived (absent 210/220 = 0, absent 230 = 1, all absent `(0, 0, 1)`) and never written into the store or attributed to the source; (e) the last occurrence, as the reader keeps; `NORMAL_RECORDED` present iff a group is present; byte-identical second save; malformed variants (unknown or repeated group, wrong order, value count different from the groups, `normal` with `none`, `NORMAL_RECORDED` without groups) rejected with `MALFORMED_STORE` ; (f) per component, presence and the original binary64 bit pattern, a present `0.0` or `-0.0` never taken as absent and an absent group never given a value; admission, causes, diagnostics (`NORMAL_RECORDED` included), data, digests and tessellation identical to the base importer (DX-1 to DX-13) | §5.2 item 11, F22, A-IMP-12, D-R4-1 |
| RC-26 | `values` counter (R4): stores with polynomial and explicit-weight references, every normal case of RC-25, attributes and elements permuted within what §5.1 admits, and declared `values` equal to, one below and one above `Σ v_r` | the writer declares exactly `Σ (K_r + (4 + e_r) · c_r + 7 + g_r)`; the reader accepts the exact count whatever the order, stops as soon as the running count exceeds the declaration, rejects a lower or higher final count and any list of another length with `MALFORMED_STORE`; a declared `values` above `maxValues` is `STORE_BUDGET_EXCEEDED` before allocation; implicit unit weights, absent normal groups and `<data>` knots never count; the example of store §5.1 counts 29 | §5.2 items 1 and 12, §5.3 |

## 3. Reference DXF export `cedg-reference-export/v1` (`MAINT-A-DXF0-EXP`)

| Id | Case | Property | Contract |
|---|---|---|---|
| RX-1 | Option off, or no references | DXF and sidecar byte-identical; the existing export suites unchanged (`GeometryExportFoundationTest`, `G9X1PreflightFidelityTest`, `G9X1LocusV2ExportTest`, `G9X1DxfManifestTest`, `G9X1PairedOutputTest`, `PreG9BS1AuthorDxfReproductionTest`) | §0 |
| RX-2 | Golden `SPLINE` pairs: `POLYNOMIAL` (implicit and explicit unit weights), `RATIONAL_EQUAL_WEIGHTS`, `RATIONAL` (quarter, half and full circle, ratio `10^6`), degrees 1 to 7, interior knots of multiplicity up to `p`, exact closure | expected group codes and order; flags 8, 4, 1 as specified; no 2, no fit points | §4 |
| RX-3 | Re-import of each RX-2 output with the GeoCeDG reader | new `sourceData` `==` exported `data` (bits except the sign of zero); nature equal; explicit weights iff written | §5, §12 |
| RX-4 | ADV values in knots, coordinates and weights | every written token parses back to the same binary64; `-0.0` reported as `0.0`; the write self-check rejects an injected mismatch (`WRITE_ROUND_TRIP_FAILED`, `NOT_WRITABLE`); values with three-digit exponents are covered by RX-19 | §5.2 |
| RX-5 | The resolved states of §6.4 and the eight cases of §6.5 built by identifiers | default inclusion, construction export, duplication warning and diagnostics per row; constructions never filtered | §6.4, §6.5 |
| RX-6 | A construction geometrically identical to a reference without block; a far construction with a valid block; labels renamed; list reordered | identical-without-block → `NO_CEDG_REPRESENTATION`; far-with-block → represented; renames and order have no effect | §6.1 |
| RX-7 | Transitions of §6.6: delete, Undo/Redo of the creation, modify and return exactly, redefine to another family, copy in the same document, paste into another document, remove the reference, reopen, Save As, reimport | states recomputed as listed; no stale state | §6.6 |
| RX-8 | Related construction modified | the reference is included by default with the notice; the modified construction is exported; the user can exclude the reference | §6.4, D-R1-6 |
| RX-9 | Explicit selection | user include of a represented reference writes both with `DUPLICATE_REPRESENTATION`; user exclude of an unrepresented one writes none; persistent preference respected | §7 |
| RX-10 | Exporting does not mutate | construction XML, Undo history, store bytes and saved state identical before and after | §0 |
| RX-11 | Sidecar situations with the option on: (A) a reference written; (B) the export completes with references excluded by the user, by preference, as represented, outside the area, or by the user after `EXPONENT_NOT_PORTABLE` (never automatically), with and without any reference written; (C) rejection by an included `NOT_WRITABLE` reference | A and B: DXF and sidecar paired, schema version 4, `external_references` lists every written reference with fidelity and every excluded one with its cause, DXF SHA-256 link, rollback; the encoding validator counts both populations; C: no DXF and no sidecar, cause in the result and report; with the option off the sidecar keeps its current version and bytes | §10 |
| RX-12 | Strictness | an included reference that cannot be written rejects the operation: no DXF, no sidecar, nothing partial reported as success, the cause in the result; after the user excludes it the export completes (situation B) | §7.3, §10 |
| RX-13 | Handles, layer, comment | construction handles unchanged; references after them; `$HANDSEED`; `GEOCEDG_REF` in the layer table only when used; `999` ASCII with the id; DXF pure ASCII with non-ASCII source strings | §4 |
| RX-14 | Hidden references; explicit export area; selection export | hidden → `60 = 1` and reported; area by the control-polygon hull with `PARTICIPATION_CONSERVATIVE`; selection without the option has no reference | §7.2 |
| RX-15 | References-only export: empty neutral model with (a) at least one writable reference included; (b) every reference excluded or outside the area; (c) the option off; (d) only one included reference, `NOT_WRITABLE` (`EXPONENT_NOT_PORTABLE`); (e) (R4, discriminating) an empty `GeoElement` population (empty selection, and a document without exportable `GeoElement`s) through the real Desktop controller: in the extended flow with the option on and one writable reference, then with the option off; in the legacy G5 flow | (a) writable: DXF with only `SPLINE` entities and sidecar; (b) and (c) no file written, as the approved rule of the foundation (A-GEF-1); (d) rejected (situation C): no DXF and no sidecar, the cause in the operation result; (e) extended flow, option on: the controller does not stop at the empty-population guard, reaches the preflight and writes a DXF with only the reference and its sidecar; extended flow, option off, and legacy G5 flow (which offers no option): the current message «The selected export population is empty.» at the same point, no file, bytes and behavior of both exports unchanged; references never enter the neutral model | §2, §9, §9.1, A-GEF-1 |
| RX-16 | AutoCAD Core Console on every RX-2 file plus a sweep of random values (version recorded) | opens without repair, AUDIT clean; **per value** within 4 ulp of the written binary64 (initial test criterion for AutoCAD 2026, `ACADVER` 25.1, only); **structure** preserved (degree, counts, equal knots stay equal and distinct stay distinct, order, domain, closure equality and inequality, weight positivity, equality and unit pattern, nature); **geometry** from `vlax-curve-getPointAtParam` within the test tolerance at declared parameters, multiple knots and ends; a fixture failing structure or geometry is not conformant whatever its ulp distances; maxima per field kind, geometric maximum, normalizations (closure inferred, `70 = 15`, 42/43 ignored) and test limits recorded | §12.1 to §12.3 |
| RX-17 | ezdxf (version pinned, 1.4.2 in the R1 pilot) on every RX-2 file | definition read from entity attributes exactly equal to the written decimals; auditor clean; `ezdxf.math.BSpline` built from the raw lists evaluated against the exact-rational reference (unit circle for rational arcs, multiple knots, orientation, closure); normalizations of `construction_tool()` (knot rounding by code 42) recorded and never used as definition reading | §12.1, §12.2 |
| RX-18 | A failed approximation, then save and reopen | the reference state and default inclusion are unchanged (no failure state) | §6.5 case 6 |
| RX-19 | Non-portable exponents (token rule of §5.2): in a control-point `x` and in a control-point `y`: `4.9E-324`, `1.0E-100`, `-1.0E-150`, `Math.nextDown(1.0E-99)` (`9.999999999999998E-100`), `1.0E100`, `-1.0E200`, `Double.MAX_VALUE`; in a knot vector: an interior knot `1.0E-150` with clamped ends `0` and `1`, and a vector scaled to `1.0E200`; in weights: positive all-equal vectors (all `1.0E-150`, all `1.0E200`) admissible by import predicate 12; boundary values written normally: `1.0E-99`, `Math.nextDown(1.0E100)` (`9.999999999999998E99`) and their negatives in coordinates | each reference with a non-portable value is `NOT_WRITABLE`/`EXPONENT_NOT_PORTABLE` with field kind, index and value; the operation blocks; no polyline, no rewritten token; while included, the operation is rejected without DXF or sidecar (situation C); after user exclusion the export completes and the sidecar records `USER_EXCLUDED` with `EXPONENT_NOT_PORTABLE` (situation B); boundary values are written normally; ezdxf and the GeoCeDG reader would read the excluded tokens correctly (documented, not used) | §5.2, §7.3, §10 |

## 4. Conversion and approximation `dxf-spline-approximation/v1` (`MAINT-A-DXF1`, `MAINT-A-DXF1b`)

| Id | Case | Property | Contract |
|---|---|---|---|
| AP-1 | Polynomial references `p = 1..7` with multiple interior knots; `RATIONAL_EQUAL_WEIGHTS` with `w ≡ 2.5`; rational flag with implicit weights; ADV values | `EXACT_CONVERSION`; data equal to `data` under `==`, bit-identical except the sign of zero; `-0.0` replaced explicitly before creation and reported; no measure | §4 |
| AP-1b | Polynomial references with 129 and 1 024 control points, simple and multiple knots, `q` from 1 to 7 | never converted; «Approximate…» not offered when the initial knot vector exceeds `N_A` for every admissible `q`; otherwise `INITIAL_KNOTS_EXCEED_POINT_LIMIT` for the `q` that exceed it; the reference stays exportable | §4.1, §5.4 |
| AP-2 | Rational quarter circle (`w_1` the binary64 of `√2/2`), `q = 3` | success with `residualMax ≤ ε` and held-out `errorEstimate ≤ ε`; each measure with its grade; **no ordering asserted**; the estimate compared with a high-precision reference maximum as test evidence only | §6 |
| AP-2b | Semicircle with a double interior knot | multiplicity `q` at that knot (`C^0`); same properties; convergence near the corner measured (§5.9 item 4) | §5.4, §5.9 |
| AP-2c | Weight ratio `10^6`; the same curves translated to coordinates of `10^6` and `10^9` | success or a typed failure with the best indicator; the dialog proposal respects `ε_floor` and says so; the floor rule measured | §5.8, §8 |
| AP-2d | Near-degenerate: consecutive coincident control points, all coincident, a span of 1 ulp | a valid result, `SAMPLE_DESIGN_DEGENERATE` or `SPAN_NOT_SPLITTABLE`; never an exception | §5.5, §5.7 |
| AP-3 | `ε < ε_floor`; `N_A = 8` unreachable; `R_MAX`; stagnation; work limit; held-out failure (`VERIFICATION_NOT_PASSED`); cancellation | typed cause; no object; no Undo entry; best indicator shown and kept in the session report; reference intact and exportable | §5.8, §9 |
| AP-4 | Move a control point; move it back exactly; redefine knots; make the curve undefined | `NOT_APPLICABLE_DEFINITION_CHANGED` → `APPLICABLE` → `NOT_APPLICABLE_*`; the object stays valid | §11 |
| AP-5 | Exactly closed rational circle | exactly closed `C^0` approximation | §5.2, §5.9 |
| AP-6 | Save, reopen, Undo/Redo, copy (both clipboards), Insert File | block bytes identical; definition digest identical; copy with a new durable identity and the same block | §13.1 |
| AP-7 | Two runs, event thread and worker | identical data and block, byte for byte | §15 |
| AP-8 | Failure injected at creation; a failed fit | atomicity: no object, no Undo entry; record and store bytes unchanged; the document not marked modified; the cause in the result and the session report; nothing after save and reopen | §9, §13.2 |
| AP-9 | `version="1"` block with an unknown attribute or an illegible value; `version="2"` | `NOT_INTERPRETABLE` and the document loads; `version="2"` fails closed | §10.3 |
| AP-10 | Success, Undo of the creation, Redo | the store never changes; the resolver computes the representation from `Construction` alone | §13.2 |
| AP-11 | Conditioning and rank: uniform, graded and far-offset knot distributions; a design injected to violate Schoenberg–Whitney; spans of a few ulps | `κ̂` reported, and its variation with the knot offset measured (independent in exact arithmetic); the injected design gives `RANK_DEFICIENT`; tiny spans give `SAMPLE_DESIGN_DEGENERATE` or `SPAN_NOT_SPLITTABLE`; no normal equations (code inspection); no regularization, column dropping, degree reduction or reparametrization | §5.6, §5.9 |
| AP-12 | `MAINT-A-DXF1b`: certified bound on AP-2, AP-2b, AP-5 | `B ≥` the high-precision reference maximum; `B·B ≥ max_s B_s²` checked exactly; floating errors `≤ B + δ_eval` | §7 |
| AP-13 | `MAINT-A-DXF1b`: work budget exhausted | last valid bound or `BOUND_NOT_COMPUTED_WORK_LIMIT` | §7.2 |
| AP-14 | Three disjoint measure sets | no refinement-grid or verification point equals a fit sample, and no verification point equals a refinement-grid point; skipped points counted; the verification set evaluated once, after refinement stops (instrumented count) | §5.7, §6.2 |

## 5. Numeric persistence (`MAINT-A-DXF1-S0`, then `MAINT-A-DXF1`)

| Id | Case | Property |
|---|---|---|
| NUM-1 | Scalar writers (`Kernel.formatRaw` for XML, `XMLStringBuilder.attr(double)`) on ADV | every value round-trips bit for bit; `-0.0` pinned as the known loss |
| NUM-2 | Exact conversion of ADV data | the created inputs equal the record data (zero sign normalized); the digest equals the digest of the normalized data |
| NUM-3 | Save → reopen → save → reopen | bit-identical inputs; byte-identical XML of the inputs and of the command |
| NUM-4 | Undo/Redo of the creation and of an unrelated later step | bit-identical inputs |
| NUM-5 | Copy/paste on the shared and Desktop clipboards; Insert File | bit-identical inputs and digest; block copied |
| NUM-6 | Compatible redefinition by the ordinary and the replacement routes | digest and block kept on both routes (today the replacement route loses the block); a 1-ulp change makes the block not applicable |
| NUM-7 | Knot pairs `k`, `nextUp(k)` and equal knots through NUM-3 to NUM-6 | multiplicities preserved |
| NUM-8 | `-0.0` in data | replaced before creation; digest stable through NUM-3 to NUM-6 |
| NUM-9 | Inventory of creation code | no exact-decimal number, no list without definition in the created objects; a pinned test shows the 15-digit loss of that route |
| NUM-10 | After every operation of NUM-3 to NUM-6 | recorded `definitionDigest` equals the live digest |

## 6. Separation of layers (continuation of A-N1 SEP-1 to SEP-4)

| Id | Case | Property |
|---|---|---|
| SEP-5 | The external file is lost after saving | the persisted references are recovered complete (RC-19) |
| SEP-6 | Export of a rational reference with the option on | a rational `SPLINE` with its weights; no polynomial approximation, no polyline |
| SEP-7 | A failed approximation | the reference stays in the store, visible as before, exportable; nothing about the failure is persisted |
| SEP-8 | After a conversion: move, hide, rename or remove the reference; reimport the file | the construction is unchanged and has no dependency on the reference; nothing computed on the construction is attributed to the reference |
| SEP-9 | Export state | determined only by a current explicit association (RX-5, RX-6) |
| SEP-4 (amended) | Semantic DXF export with the option off | references absent; approximations and B-splines by the approved approximate route until `MAINT-A3` |

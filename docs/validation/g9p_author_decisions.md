# G9P author decision package

**Disposition: G9P-R1 and G9P PASS — AUTHOR APPROVED. The six G9P
specifications are normative, ADR 0010–0015 are Accepted, and only G9O1 is
authorized for later execution. No productive G9 implementation has started.**

## Final G9P closeout decisions

| ID | Author disposition | Binding closeout effect |
|---|---|---|
| D1 | **APPROVE** | `G9P-R1 = PASS — AUTHOR APPROVED` |
| D2 | **APPROVE** | `G9P = PASS — AUTHOR APPROVED`; integrated architecture, roadmap, validation strategy, and phase decomposition accepted |
| D3 | **APPROVE** | all six G9P specifications are `NORMATIVE / AUTHOR APPROVED` |
| D4 | **APPROVE** | ADR 0010–0015 are `Accepted` |
| D5 | **APPROVE WITH API DEFERRAL** | typed one-dimensional scalar-generator semantics are normative; G9U0, after inspecting actual GeoGebra overload conventions, owns the exact user-facing mapped-scalar syntax and must present it for author review |
| D6 | **APPROVE** | the rich metric result remains authority; G9U0 must expose total `Length[GeoLocusV2]` only through the scalar-admissibility guard; legacy `Length[GeoLocus]` is unchanged |
| D7 | **APPROVE** | the GeoCeDG Classic diagnostic path preserves native V2/rich/spatial types, IDs, tokens and bindings with the same kernel semantics; unsupported external-upstream open is documented and never repaired by silent lossy downgrade |
| D8 | **APPROVE** | approved approximation is explicit; sidecar is mandatory for every fidelity reduction, optional for all-exact output; strict reject/stop is the partiality default; unbounded non-native curves require an explicit semantic domain |

Every row records the minimum requested decision evidence, alternatives,
recommendation, rejection impact, and gate.

## Spatial semantics

| ID / decision | Evidence | Alternatives | Recommendation | Impact if rejected | Required gate |
|---|---|---|---|---|---|
| S1 phase subdivision | Identity/XML is independent of point reconstruction; projection-system persistence precedes evaluation; lifecycle hardening is broader than the pilot | one G9A; A1/A2; A1/A2/A3 | G9A1 identity/persistence for geo/object/frame/system/map/relation/binding records, G9A2 system evaluation + point/core, G9A3 hostile lifecycle/redefine/migration | larger review surface or public consumers racing unfinished lifecycle | approve roadmap and separate prompts |
| S2 authority/edit direction | CeDG references are projection-defined; two-way updates create cycles | projection-defined; spatial-defined; implicit hybrid | role-gated hybrid, one authority per object/revision, projection-defined first | a single universal direction limits later use; implicit hybrid risks loops | accept ADR 0010 before G9A2 |
| S3 durable IDs | `ceID`, labels, order, coordinates and XML position do not survive required lifecycle | reuse `ceID`; label map; new typed IDs | construction-scoped persistent Geo/spatial/frame/projection-system/diagram-map/frame-relation/binding IDs and registry | copy/reopen/redefine cannot be made reliable | G9A1 identity tests |
| S4 lifecycle and redefine | Host redefine has in-place, soft, instance-replacement and full-XML-rebuild routes; Java reference or label continuity is neither necessary nor sufficient | every redefine is new; every same-label redefine preserves; explicit semantic transaction | recompute keeps ID; an explicit target-based compatible redefine may transfer ID only after provider/type/schema/role/cardinality checks and increments definition revision; true replacement/incompatible redefine gets a fresh ID or fails; delete+recreate and copy get fresh IDs; undo/reopen restore serialized IDs | blanket replacement loses Construction Protocol intent; blanket transfer misbinds semantics | G9A1 transaction seam and G9A3 hostile-route matrix |
| S5 defining vs derived | references mix manual construction and presentation views | all defining; all derived; roles | typed `defining`, `derived`, `auxiliary`, `analysis`, `presentation` roles | loss of reconstruction evidence or editable CeDG workflow | G9A2 role/DAG tests |
| S6 revision model | G6–G8 separate durable identity from revision evidence | mutate one ID snapshot; identity + one revision; orthogonal revisions | durable ID plus semantic, topology, and construction revision evidence | stale certificates or needless identity churn | G9A2/A3 stale-state tests |
| S7 persistence split | current spatial and V2 geos lack the required XML/copy behavior | in-memory first; persistence first; split substrate/core | persistent identity substrate in A1 before semantic point pilot | nonreopenable stable-looking objects or oversized first phase | accept ADR 0011; A1 gate |
| S8 legacy migration | old `.ggb` has no trustworthy associations | infer by labels/geometry; reject; leave unassociated | load as unassociated; user/explicit migration only | false spatial identity or reduced convenience | G9A3 legacy corpus |
| S9 projection system and primitive sufficiency | view count alone fails; intrinsic frame coordinates differ from their geometric placement in a common dihedral diagram | treat screen placement as geometry; independent frames only; typed system/maps | durable `ProjectionSystem` context with `q_i=pi_i(x)`, `p_i=delta_i(q_i)`, explicit hinges/change-of-plane relations, intrinsic type-specific reconstruction and intrinsic+diagram reprojection; coherent diagram gauge changes do not alter sufficiency | GUI would infer plane relations or diagram placement could manufacture spatial information | G9A1/A2 system gates, then approved primitive spec before G9B |
| S10 status taxonomy | validity, sufficiency, consistency, lifecycle and guarantee can vary independently | one enum; exceptions; separate axes | closed axes: support `SUPPORTED/UNSUPPORTED`; definition `DEFINED/UNDEFINED/DEGENERATE`; certificate `NOT_EVALUATED/VALID/UNDERDETERMINED/AMBIGUOUS/INCONSISTENT_PROJECTIONS/DEGENERATE/UNDEFINED`; currentness `CURRENT/INVALIDATED/FAILED_CURRENT_REVISION`; fidelity `EXACT/NUMERICAL/DISCRETE`; guarantee `NOT_APPLICABLE/CERTIFIED_BOUND/ESTIMATED_ERROR/UNRESOLVED`; correspondence `ESTABLISHED/AMBIGUOUS/BROKEN/NOT_REQUIRED`; no stale spatial value after failure | state explosion hidden in one ambiguous status | G9A2 and G9B state coverage |
| S11 composed boundary | CeDG needs constructive topology but not opaque CAD history | no composites; full CAD B-Rep; bounded projective boundary | bounded constructive structures for spatial point collections, curves/arcs, edges, oriented loops, faces, supporting/ruled/developable surfaces, polyhedral objects, incidence, adjacency, orientation, boundary ownership and connected components; opaque feature trees, generic solid booleans and a general CAD B-Rep kernel remain outside G9 | insufficient procedures or CAD-like authority creep | G9C proposal + canonical models |

## GUI and public surface

| ID / decision | Evidence | Alternatives | Recommendation | Impact if rejected | Required gate |
|---|---|---|---|---|---|
| U1 workspace names | prompt and reference workflows distinguish construction from procedures | mode; perspective; workspace names | `CeDG Construction` and `CeDG Dihedral Procedures`; internal IDs separate | terminology collision with GeoGebra tool modes | approve workspace spec |
| U2 Construction groups | reference files have 19 mixed Classic/custom groups; profile has only 6 | copy reference toolbar; minimal 6; CeDG task groups | 11 task-oriented groups specified for normative manifest v2 | clutter/model-specific macros or missing G6–G8 access | G9U1 usability/mapping review |
| U3 public creation | only legacy `Locus[Q,P]` is public; V2 factory is injected/internal; a dependent numeric does not identify its unique true driver | redirect legacy; two-argument type enumeration; typed generator/explicit-driver overload | explicit experimental V2 command over a typed one-dimensional semantic generator; retain `LocusV2[Q,P]` only when `P` already carries an unambiguous admitted provider; G9U0 must inspect actual overload conventions and present its final mapped-scalar syntax for author review; legacy untouched | compatibility risk, ambiguous dependency capture or extra command vocabulary | normative public spec/ADR; G9U0 API review and tests |
| U4 semantic generator | current internal providers prove finite/periodic domains and V2 nesting but only a segment has a live path pilot | slider/segment enumeration; generic `Path`; typed generator | versioned `G:D->S` with exactly one explicit driving coordinate, domain/orientation/components/revisions and registered dependency slice; initial scalar identity/map family plus semantic point on segment, circle, circular arc or explicit Locus V2 branch/component; nested acyclic V2 is mandatory | U0 cannot represent reference workflows, dependent scalars or durable preimages | U0 provider, continuation, cycle and persistence suite |
| U5 metric command | legacy `Length[GeoLocus]` returns sample count; rich G7 status remains authoritative | only rich command; immediate standalone scalar; rich plus guarded adapter | rich metric query/result remains authority; G9U0 must expose standard total `Length[GeoLocusV2]` only as its guarded child/reuser when `isScalarAdmissible()` succeeds; otherwise scalar failure is explicit/undefined; legacy behavior unchanged | extra vocabulary or hidden incomplete/error status | U0 command/compatibility/admissibility tests |
| U6 Intersect integration | general tool exists; G8 rich results/token point are internal | parallel Locus tool; general overload | extend general `Intersect` for V2 and preserve all existing branches | duplicated UX or higher controller-change risk | U0 selection/dispatch tests |
| U7 result selection | index/proximity is unstable; exact token consumer exists | list index; nearest root; token | choose among established roots graphically, persist exact semantic token | downstream point retargets silently | U0 continuation/lifecycle suite |
| U8 persistence requirement | V2/rich geos currently lack XML and safe copy/set | public transient objects; persistence before public | no public creation until save/reopen/copy/undo works | misleading data loss | G9A3 then U0 round trip |
| U9 Classic policy | separate GeoCeDG Classic diagnostic process/path exists; external upstream may not know new persisted types | reject; fully expose; preserve/read-only diagnostic; lossy downgrade | native parse/preserve/recompute/save/reopen with IDs/tokens/bindings and no creation UI; unsupported external-open boundary is explicit and never converted silently | interoperability loss or accidental semantic downgrade | G9A3/U0 Classic and external-open corpus |
| U10 workspace authority | current schema cannot express reference layout; hard-coded seams exist | hard-code perspective; saved GGB only; manifest v2 | versioned application-profile manifest compiles all workspace state/actions | duplicate authorities and drift | accept ADR 0012; G9U1 static/runtime gate |
| U11 post-G9 workspace | procedures require spatial IDs/bindings/certificates | pilot after A2; after G9A; after global G9 | design now, implement only after global G9 PASS | delayed UX or procedures built on incomplete types | explicit global G9 author approval |

## DXF

| ID / decision | Evidence | Alternatives | Recommendation | Impact if rejected | Required gate |
|---|---|---|---|---|---|
| X1 fidelity taxonomy | current approximate marker is unreported by writer/UI | exact flag only; file-level; component outcomes | per-component `EXACT/APPROXIMATE/UNSUPPORTED/INVALID` plus reasons/guarantee | silent loss or more DTO complexity | accept spec/ADR before X1A |
| X2 exact set | G5 writer already has native mappings | expand conics now; retain G5 | retain POINT/LINE/RAY/XLINE/CIRCLE/ARC/ELLIPSE/LWPOLYLINE | missed early capability or unproved exactness | G5 executable corpus |
| X3 approximation baseline | render data forbidden; no spline conformance | adaptive polyline; fitted SPLINE; temporary geos | deterministic semantic-domain dyadic `LWPOLYLINE` | larger files or dishonest/side-effecting output | X1B analytic suite |
| X4 SPLINE | no independent exactness/reader evidence | use now; never; later evidence gate | defer exact rational/fitted SPLINE to X1C | delayed compactness or premature false exactness | math + external reader conformance |
| X5 error contract | samples cannot prove global Hausdorff error | claim tolerance; estimated/certified axes | distinguish requested tolerance, estimate/bound, guarantee and work | weaker UX or false certification | X1B scale/reference tests |
| X6 sidecar/paired writes | DXF cannot carry full provenance; current direct write can truncate | no sidecar; always; conditional mandatory | deterministic JSON sidecar and paired temp promotion are mandatory for approximate/omitted/partial/unsupported-not-exported/work-limited or otherwise reduced output; all-exact sidecar may be optional | extra artifact or untraceable fidelity reduction | X1A filesystem/hash tests |
| X7 unbounded domains | only native ray/line encode unbounded truthfully | viewport clip; fixed default; explicit semantic domain | native RAY/XLINE only; otherwise explicit closed parameter subdomain | more user input or viewport-dependent geometry | X1B domain tests |
| X8 approximation/partial defaults | approximation and omission are different decisions | approved typed approximation as normal explicit/reported behavior; strict only; explicit partial option | approved typed approximation may be normal but must be visibly identified; partial is off by default, strict reject/stop applies, and any future partial option needs explicit intent, warning and mandatory sidecar | more friction or silent omissions | X1A UI/preflight/failure tests |
| X9 identity/dependency scope | G5 source ID is revision/label scoped; internal G6-G8 semantic sources already exist; G9A/U0 may later supply durable public IDs | require U0/A1 for all X1; disclose scope | sidecar declares `persistent` vs `construction-revision`; X1 has no hard U0 dependency, while U0-before-X1 remains the recommended integration order | less cross-save traceability or an unnecessary reverse phase gate | X1 identity-scope fixtures and global integration gate |

## Documentation

| ID / decision | Evidence | Alternatives | Recommendation | Impact if rejected | Required gate |
|---|---|---|---|---|---|
| D1 guide structure | user guide mixes workflows, internals, mathematics and phase history | one monolith; split by audience | concise user guide plus math, developer and agent guides linked to specs | more navigation or continued drift/duplication | approve maintenance spec; link gate |
| D2 mathematical split | equations are useful but specs must remain normative | keep in user guide; duplicate spec; explanatory reference | nonnormative math reference with stable spec links | less approachable docs or competing truth | documentation traceability gate |
| D3 impact gate | living docs contradict G8 closeout | manual best effort; checklist; machine matrix | every capability declares guide impact and traceability row | implementation/doc drift | future focused verifier |
| D4 ownership | current roles overlap | shared ownership; role owners | user=observable behavior, math=explanation, developer=implementation, agent=workflow, roadmap=status | handoff overhead or repeated contradictions | G9O1 author approval |

## Bundles and operations

| ID / decision | Evidence | Alternatives | Recommendation | Impact if rejected | Required gate |
|---|---|---|---|---|---|
| O1 profiles | one universal dump exceeds relevance/rights budgets | one bundle; arbitrary filters; thematic profiles | governance, frontend/DXF, Locus, spatial G9, operations, current-index profiles | more artifacts or uncontrolled context | approve schema/config |
| O2 ownership classes | path prefix misses native Java inside upstream tree and rewritten root docs | path only; Git change only; precedence rules | explicit precedence incl. `GEOCEDG_NATIVE` and `UPSTREAM_MODIFIED` | classification work or wrong authority | G9O1 ownership fixtures |
| O3 manifest | reproducibility needs source and canonical hashes | archive hash only; file list; versioned manifest | source hash, canonical hash, baseline blob, role/authority/as-of, reading order | larger metadata or unverifiable bundles | schema validation |
| O4 format/determinism | normal ZIP timestamps and filesystem order vary | directory; ordinary ZIP; canonical archive | fixed ordering/separators/metadata and content-derived bundle ID | custom generator work or nondeterministic evidence | byte-equal rerun gate |
| O5 dirty tree | local changes can silently alter context | allow; reject; explicit dirty mode | reject by default; explicit mode records staged/unstaged/untracked hashes/warning | less convenience or stale provenance | dirty-state fixtures |
| O6 exclusions/rights | catalog contains restricted/unreviewed assets | include all; path blacklist; Git-index + rights policy | exclude generated/ignored/untracked/restricted/third-party by default; binary metadata only | incomplete research bundle or redistribution risk | license/catalog gate |
| O7 implementation order | guides and G9 phases benefit from reproducible context, but bundle failure does not make spatial mathematics undefined | after G9; semantic prerequisite; operational-first | execute G9O1 first as the recommended operational predecessor; do not make its PASS a hard semantic dependency of G9A1; require its evidence at global operational closeout | delays features, postpones reproducibility or incorrectly couples tooling to geometry | explicit G9O1 invocation and separate closeout gate |

## Roadmap

| ID / decision | Evidence | Alternatives | Recommendation | Impact if rejected | Required gate |
|---|---|---|---|---|---|
| R1 dependency topology | public V2 needs completed identity lifecycle; primitive schemas do not consume GUI; X1 can consume internal semantic snapshots | one linear chain; unconstrained parallelism; typed dependency graph | hard graph: A1→A2→A3→B→C and A3→U0; U0+X1→U1 as product integration; X1 depends on G5/G6-G8 contracts, not U0; both tracks join global closeout; G9U2 follows it | reverse GUI dependency in kernel or unsafe public persistence | author approves roadmap topology |
| R2 recommended order | operational reproducibility improves every later task and shared files still favor low-conflict scheduling | start A1; parallelize immediately; keep former serial order as semantic graph | recommend O1; A1; A2; A3; U0; X1; U1; B; C; closeout; U2, explicitly as scheduling rather than semantic arrows | later bundles/guides, merge conflict or mistaken dependency | execute only explicitly invoked canonical prompts |
| R3 global/release gates | G9 product closeout combines kernel, professional public surface and reproducible evidence | kernel-only closeout; UI-first gate; integrated closeout | require approved A/B/C, U0/X1/U1 and O1 operational evidence at global closeout; no new semantics in closeout | partial product release or operational evidence coupled to kernel definitions | composed verifier and explicit author approval |
| R4 G9U2 gate | procedures consume projection systems, G9B/C objects/certificates and U1 workspace infrastructure | after A2; after G9A; after global G9 | global G9 PASS and explicit author approval | delayed procedures or premature opaque tooling | G9 closeout tag + U2 prompt |

## Closeout disposition

The author accepted this package with D1–D8 above. Acceptance does not execute
a productive phase. G9O1 is authorized and not started; every other future
prompt remains unexecuted and unauthorized, with G9U2 blocked on global
`G9 PASS — AUTHOR APPROVED`.

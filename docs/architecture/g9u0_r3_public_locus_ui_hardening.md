# G9U0-R3 public Locus V2 UI exposure hardening

- Phase: **G9U0-R3**
- State: **PASS — AUTHOR APPROVED**
- Layer: Desktop/application presentation
- Kernel, solver, identity and serialization changes: **none**

```text
G9U0-R3 = PASS — AUTHOR APPROVED
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorSmoke = PASS
manualAuthorReSmoke = PASS
```

## Authority continuity

Current entry `88801ba540cceeaeb1c2366be3c3a8d705f1b09d` retains the annotated
G9U0-R2 PASS target `9694dd4c3c274f627839d0eb5d2827a7910bf0ca` as an ancestor. The only
intervening commit is `Consolidate BOOK-P0-post operations`. Its paths are the
book bridge, guides and operational tooling; the repository workflow explicitly
excludes book-bridge-only commits from technical product authority. R3 therefore
continues the approved product line and neither rewrites nor competes with R2.
The historical G9U0 public-surface spec is hash-frozen; the additive
`geocedg/specs/locus/locus-v2-public-ui-exposure.md` owns only this R3 frontend
refinement and the prospective marker-boundary clarification.

The historical R2 verifier previously admitted only the protected checkpoint or
its single direct closeout child. R1 and G9X1 already use the repository's
tagged-descendant mode after promotion. R3 applies that existing operational
pattern to R2: the annotated PASS tag object and peel, exact two-commit shape and
ancestry are mandatory, while the 51/26 R2 inventory remains derived from the
sealed PASS commit. Current-source R2 tests and static contracts still run; no
R2 hash, scenario, evidence or product criterion is weakened.

## Characterized menu failure

`GeoCeDGMenuBar.initMenubar()` created a plain `JMenu` and populated all six
product entries. The inherited `GeoGebraMenuBar.updateFonts()` deliberately
calls `removeAll()` on every top-level menu. Upstream `BaseMenu` subclasses
rebuild lazily from `menuSelected`; the plain GeoCeDG menu has no such listener.
It consequently remained visible and enabled with `itemCount == 0`. This one
lifecycle fault hid DXF and every Locus V2 entry, including the action that
already routes to
`GeoCeDGEuclidianController.inspectRichResultSelection()` and
`GeoCeDGLocusV2Dialogs.inspectRichResult()`.

R3 retains one `populateProductMenu()` authority. Initial/full rebuild calls it
once; the Desktop font/localization lifecycle first performs its inherited work
and then calls the same method, reapplies the normal recursive font and component
orientation. Feature decisions and action bodies are unchanged. The normal
`--enableLocusV2=true` flag remains the sole public Locus V2 opt-in; DXF retains
its independent policy.

## Exact-token auxiliary presentation

Explicit point materialization creates a `GeoText` holding the opaque exact
token because `AlgoLocusIntersectionPointV2` requires it as a normal
reconstructible input. The helper was auxiliary but inherited ordinary
Euclidian visibility, exposing a long opaque string. R3 marks that same helper
non-Euclidian at its creation seam. It is not removed, replaced, assigned a
semantic layer or changed in content. Save/reopen, copy/remap, recompute and
undo/redo continue through the existing DAG and XML contracts.

The inspector previously also used the complete opaque token in
`TokenChoice.toString()`. Swing therefore treated token length as combo-box and
dialog layout authority. R3 now builds one localized compact presentation label
per admissible solution (transient ordinal plus contact class) while retaining
the full token in the choice object and passing it unchanged to materialization.
The ordinal is a snapshot-only UI discriminator and is never persisted or used
as identity. Full-token diagnostics remain read-only and wrapping; the bounded
text viewport, not token length, constrains dialog layout.

## Preserved boundary

The rich intersection/metric result remains non-drawable semantic authority.
The menu is a presentation client. R3 introduces no persistent point from
`Intersect(L,T)`, no candidate-marker overlay, no new feature flag, no `Path`
conformance and no kernel, solver, identity, token-ledger, XML, render or
workspace behavior. Transient active-result candidate markers remain a future
G9U1 presentation concern.

## Verification topology

The R3 verifier owns M01–M06, I01–I11, T01–T03 and N01–N02 plus existing
profile/runtime/localization/tool-surface tests. It writes a deterministic
canonical summary containing scenario/test results and hashes only the four
productive/test R3 source paths. The composed verifier invokes this gate after
R2 and before any future G9U1 gate. Historical G9U0/R1/R2, G9X1, G5, G9A and
legacy Locus authorities remain independent and unchanged.

The repository-state contract fixture reads the roadmap from the same committed
revision as the actual state resolver. It therefore tests branch/detached
invariance without letting an uncommitted phase-closeout roadmap masquerade as
the committed HEAD authority; post-promotion it naturally resolves the closed
R3 roadmap.

## Post-R3 G9U1 planning authority

The original G9P G9U1 prompt remains immutable historical evidence. After the
first complete R3 composed execution passed, the planning-only successor
`.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r3.prompt.md`
became the prospective execution authority. It cannot run unless R3 first
closes `PASS — AUTHOR APPROVED` and the author separately authorizes G9U1.

The successor keeps the action manifest as the only toolbar/menu authority and
the eleven-group professional workspace. It places active-result candidate
markers in a transient frontend overlay over already admissible exact tokens;
markers have no GeoElement/XML/DAG/Protocol/undo identity and never create
persistent points automatically. It also plans a reviewable frontend-only
GeoCeDG visual system with two independent logical roles:
`geocedg.brand.topbar` for top-bar/product chrome and
`geocedg.brand.startup` for startup/application identity and only suitably
validated platform derivatives. The future author sources are
`helixTopBar.png` and `helixSnapshot.png`; neither is integrated by R3. Each
future role requires its own canonical provenance/hash authority and
deterministic derivatives without hand-copied packaging duplicates. No logo,
physical durable asset path or permanent palette is invented in R3. GeoCeDG
Classic remains a separate, visibly diagnostic path.

## Author closeout

Automation first passed for the initial R3 candidate. The first real author
smoke confirmed the menu, inspector, exact-token materialization, hidden helper
and native reopen behavior, but exposed the excessive-width defect. The bounded
chooser correction then passed replacement focused, deterministic and composed
verification. The author subsequently re-smoked the corrected real Desktop flow
and accepted every R3 criterion, including bounded long-token layout and exact
unchanged token authority. This closes R3 without implementing candidate
markers, G9U1 or the proposed future R4 intersection investigation.

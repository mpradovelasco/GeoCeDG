# PRE-G9B-R7 — author-review sheet

```text
SHEET_KIND        = AUTHOR REVIEW PACKAGE (PRE-G9B-R7, deliverable H)
CANDIDATE         = the commit that adds this sheet (identity, tree and acceptance results are
                    reported with the handoff; a file cannot name its own commit)
AUTHOR SMOKE      = REQUIRED — NOT EXECUTED (only the author's statement can record PASS)
AUTHOR APPROVAL   = NOT RECORDED (separate author decision naming the exact commit)
PUBLICATION       = NOT AUTHORIZED
selfApproved      = false
```

## 1. Exact approval conditions

The author can close `PRE-G9B-R7` only by a separate instruction that:

1. names the exact R7 candidate commit and tree reported in the handoff;
2. finds its `STATIC`, `PHASE -Phase PRE-G9B-R5-A`, `PHASE -Phase PRE-G9B-R6` and `FINAL`
   results acceptable as executed and authenticated (an accepted `FINAL` carries a receipt
   inspected with `phase-closeout.ps1 -Action INSPECT`; a non-accepted `FINAL` needs a causal
   characterization and an explicit author disposition, never a relabelling);
3. states the author smoke result of §3;
4. takes, or explicitly defers with an owner, the decisions of §2;
5. authorizes the documentary closeout and, separately, any publication by an exact-SHA,
   non-force fast-forward.

Approving R7 authorizes no later phase.

## 2. Decisions the author must take or defer

| ID | Decision | Where | R7 recommendation |
|---|---|---|---|
| DEC-R7-D1 | `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`: accepted limitation, or deferred refinement retained with the author | [debt reconciliation](pre_g9b_r7_debt_reconciliation.md) §2, §7 | retain as deferred refinement |
| DEC-R7-D2 | confirm by identifier: `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`, `BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION`, `GUIDE-A1-SECTION-9.3-STALE`, `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` (new objects) | §2, §4 | confirm |
| DEC-R7-D3 | the accepted-limitation proposals for inherited, upstream, harness and fail-closed items | §3, §4 | accept as proposed |
| DEC-R7-D4 | P-12 and the pinned P-13 headers: terminal cross-reference retention or a historical-verifier re-pin task | §5.1 | terminal cross-reference retention |
| DEC-R7-D5 | future owners of the author-accountable R6-plus rows at the R7 checkpoint | §5.2 | adopt the proposed owners |
| DEC-R7-D6 | G9-R4 living-document correction; unregistered historical suites | §4 | authorize the documentary correction; retain the suites item |
| DEC-R7-G1 … G9 | G11/G12/G15 revised scope, scale and persistence questions, owners | [roadmap review](../architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md) §6 | as listed there |
| DEC-R7-N1 | the next roadmap step (§4) | below | author's choice |

Until decided, every item keeps its recorded status and the owner shown in the debt
reconciliation; no item lacks an owner.

## 3. Author smoke checklist

**Copy under test.** Either an installed `INTERNAL` or `NC` package of GeoCeDG 1.1.0 (for
example the T' packages of the G-R1 campaign: the R7 candidate changes no product source,
resource, build or packaging input, so their behaviour is that of the candidate), or a run
of the candidate from source with `.\gradlew.bat :desktop:desktop:runGeoCeDG` (About then
shows *development build*, and the bundled tool library is absent). State which copy was
used. Use a copy of the settings, not your live settings, if earlier sessions changed
preferences.

**Required external tools:** none beyond a PDF viewer and an SVG-capable browser for S6.
**Optional interoperability:** AutoCAD 2026 (`accoreconsole` or GUI) for S7; a LaTeX
installation for PSTricks/PGF output; Asymptote (label compilation stays unverified, L-7).

Record each step as PASS, FAIL or NOT RUN.

| # | Workflow | Steps | Expected |
|---|---|---|---|
| S1 | About, version, guide | Start GeoCeDG. Open **Help → About GeoCeDG**. Open **Help → GeoCeDG user guide**; switch **Options → GeoCeDG options → Product language…** to Spanish and open the guide again | title `GeoCeDG 1.1.0`; About shows `1.1.0`, version date `2026-10-15`, the build, the distribution condition (*INTERNAL EVALUATION — NOT FOR REDISTRIBUTION*, *NON-COMMERCIAL DISTRIBUTION — PROFILE NC* or *development build*) and the composite-work licensing summary; the guide opens in English, then in Spanish |
| S2 | Open, save, reopen | **File → Open** an existing `.cedg` document of yours; change one object; **File → Save**; close; reopen it | the document reopens with the change; no spurious save prompt on an unmodified reopen |
| S3 | Locus V2, Spline V2 and derived constructions | With points `A`–`D`, use **Construction → Semantic curves → Spline V2** (select A, B, C, D, then A again to finish) to create `s`. Enter `P=Point(s,"spline-v2/main",0.25)` and `Q=Point(s,"spline-v2/main",0.75)`; `t=Translate(s, Vector((1,1)))`; a Locus V2 with **Construction → Semantic curves → Locus V2** (or `LocusV2(<Dependent Point>, <Constrained Point>)`) for a dependent point of your choice. Drag `B`; Undo and Redo | one Spline V2 and one undo step; `P` and `Q` stay on the curve at their parameters; the translated image and the locus update dynamically; Undo/Redo restore exactly |
| S4 | Intersection and metric public surfaces | `l=Line((-5,0),(5,1))`; `r=Intersect(s, l)`; materialize one solution from the rich result (inspector or `Intersect(r, <Solution Token>)`); `Length(s)`; `LocusLength(s)`; `Length(s, P, Q)`; then `Length(s, P, (0,0))` | the rich result lists the solutions; the materialized point is an ordinary dependent point; total and partial lengths are defined; an endpoint without semantic provenance gives an undefined (`INVALID_QUERY`) result, never a nearest match |
| S5 | Numeric layers and document transitions | Choose working layer 12 with **Working Layer** (Move group) or the status bar; create an object; in the Algebra View use **Sort by Layer** and hide layer 12; save; **File → New**; reopen the saved document | the object is on layer 12; hiding marks the document modified (not an undo step); New shows every layer; the reopened document hides layer 12 again |
| S6 | Units, drawing scale, ExportArea and picture export | **Options → GeoCeDG options → Document units…**: construction unit `mm`; set a drawing scale (for example 1:50) from the status bar; **File → Export area → Define export area…** (four world bounds); **Show export area**; export **Graphics View as Picture…** as PDF, SVG and PNG; open them; then **File → Open** another document | status bar shows units and scale; the PDF page and SVG `viewBox` match the area exactly with no margin; hidden layers are omitted; after Open the export area is cleared (G-R1) |
| S7 | DXF export (documented limitations) | **Export 2D geometry as DXF (experimental)…**; read the preflight; export with a sidecar | preflight lists exact and approximate entities; `$INSUNITS` follows the unit; Spline V2 is written as approximate `LWPOLYLINE` (exact `SPLINE` not implemented); hidden layers are written OFF. Optional: open in AutoCAD 2026 (other releases unverified, O-11) |
| S8 | Native dimensions and ISO A borders | `AlignedDimension(A,B,1)` and `LinearDimension(A,B,xAxis,1)`; `IsoABorder((0,0), 3, true, 1, 50, 1, true)` with construction unit `mm`; **File → Export area** from the ISO A sheet; export PDF | dimension values in model units, text in the presentation unit; the sheet measures 21000 × 14850 model units; the exported PDF page is 420 × 297 mm; the border is a model-space construction (no title block, no paper space) |
| S9 | GGBScript (supported command-language contract) | Spanish UI: a button whose On Click script (English command names) is `u=Translate(s, Vector((0,1)))` then `L=Length(u)`; click it; Undo; Redo; save; switch the product language to English; reopen and click again | the script editor shows English command names; the script creates the image and its length through the real script path; one undo step per changing click; the `.cedg` round trip keeps the script and it runs after the language change; localized names stay ordinary-input aliases, not script canon |

Not required: manual reproduction of automated tests, Classic diagnostic sessions,
packaging builds (no packaging input changed), commercial or public-release checks.

## 4. Possible next roadmap choices (none authorized)

1. R7 documentary closeout and publication of the closeout commit.
2. A bounded documentary or contract activity: P-4 save warning, P-18 per-clause disposition,
   the G9-R4 living-document correction, the B-1 document-session lifecycle contract.
3. Adoption of the revised G11/G12/G15 scope, followed by the design of one gate under its
   own authorization.
4. Preparation of `G9B` — still `DESIGNED — NOT AUTHORIZED`; it does not follow R7
   automatically and needs its own explicit instruction and exact base.
5. Another separately authorized priority, for example a public-release decision for `NC`
   (with the release gate and a fresh license audit) or verification maintenance.

```text
PRE-G9B-R6-plus = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R7      = EXECUTION AUTHORIZED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
VERIFICATION_CLASS = GLOBAL_IMPACT; PRIMARY_ACCEPTANCE = FINAL
G11 / G12 / G15 = PENDING — ROADMAP SCOPE REVIEW
ENH-B-MANUAL-EXPORT-AREA-DRAG = OPEN — OWNER G15
G9B / G9C / G9U2 / PRODUCTIVE G10 = NOT AUTHORIZED
PUBLICATION / PUBLIC RELEASE = NOT AUTHORIZED
selfApproved = false
```

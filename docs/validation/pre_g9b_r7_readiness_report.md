# PRE-G9B-R7 — integrated PRE-G9B readiness report

```text
REPORT_KIND           = INTEGRATED READINESS ASSESSMENT (PRE-G9B-R7, deliverable E)
BASE                  = b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513 (tree 3aef929b)
PRODUCT STATE         = identical to the approved G-R1 technical candidate T' da1a0952
                        (no source, test, build or packaging-input change since T')
TECHNICAL READINESS   = assessed below; the R7 FINAL result on the exact candidate is
                        reported with the acceptance evidence, outside this file
AUTHORIZATION         = this report authorizes nothing; technical readiness is not
                        authorization to implement any phase
G9B                   = NOT AUTHORIZED; it does not follow R7 automatically
selfApproved          = false
```

The assessment rests on the approved closeouts of the track (verified in the
[phase-entry record](pre_g9b_r7_phase_entry_record.md) §3), the current source and tests,
the [debt reconciliation](pre_g9b_r7_debt_reconciliation.md), the
[G11/G12/G15 roadmap review](../architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md) and
the R7 verification campaign defined in the
[candidate report](pre_g9b_r7_candidate_report.md). Claims use the vocabulary of
`geocedg/specs/operations/documentation-maintenance.md` §3.

## A. Geometric and semantic readiness

**Assessment: supported by approved contracts and by the accumulated tests; no new
semantics.**

| Contract | Approved authority | Executable evidence re-run by R7 |
|---|---|---|
| Locus V2 semantic entity, metrics and intersections (G6–G8) | `geocedg/specs/locus/locus-v2-semantics.md`, ADR 0006–0008; G8 `PASS — AUTHOR APPROVED` | `final.shared` JUnit; Python references `python.g7a`, `g8a`, `g8c`, `g8c1`, `g8c2` |
| public Locus V2, `SplineV2`, semantic points, exact-token materialization (G9U0 R1–R6, G9S1, G9S1-R1, R2-E0/E1) | normative specifications and ADR 0018, 0019, 0021; `R2-E0/E1` approved | `final.shared`, `final.desktop` |
| metric endpoint provenance (`R2`) and redefine correctness (`R3`, ADR 0029) | R2 and R3 closeouts | `final.shared`, `final.desktop` |
| durable identity, persistence, lifecycle and migration (G9A1–G9A3), native `.cedg` identity (ADR 0016) | G9A closeouts; native-document-identity specification | `final.shared`, `final.desktop`; `python.g9a2-reference`, `python.g9a2-model` |
| dynamic dependency (DAG), clipboard predecessor closure and atomic rollback (R3-C1, R3-C1-U1), certified expression points (R3-X1) | R3-C1, R3-C1-U1, R3-X1 closeouts | `final.shared`, `final.desktop` |
| document units, hidden-layer persistence, `IsoABorder`, native dimensions (D0/D1, A-2, E3, E2) | `unit-system.md` (ADR 0032), `cedg-workspaces.md`, `iso-a-border.md` (ADR 0036), dimension specification (ADR 0034) | `final.shared`, `final.desktop` |
| canonical scientific inputs | static contracts | `static.scientific-inputs.semantic` |

Retained semantic limitations are explicit and fail closed: generic `LocusV2 × LocusV2` pairs
outside the certified families stay rich-only by contract (`D1`, not a defect); the three
R2-E0/E1 fail-closed limitations; spatial semantics stay experimental, default-off and
limited to the G9A2 point pilot; `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` is resolved.
R7 introduces no algorithm, identity semantic or serialization change.

## B. Desktop and authoring readiness

**Assessment: complete contracts for the approved authoring surface, with recorded
limitations.**

Complete approved contracts: the GeoCeDG profile and CeDG Construction workspace (G9U1,
133 actions); canonical English command surface with localized aliases (R5-B, ADR 0031);
`SplineV2` and ordered-list tools (R4); bilingual guide with derived navigation (R5-A) and
the minimal EN/ES help package (G); GGBScript compatibility matrix on the real script
surfaces (R6: 162 rows); numeric layers `0..99`, working layer and hidden-layer persistence
(A-1, A-2); export surface and single `ExportArea` authority (B); 2D export completion with
physical scale and semantic-curve exporters (C, C-X1); document units and status bar
(D0, D1); curated GGT library and its packaging (E1); native dimensions (E2) and the
POST-E2 refinements (physical PDF strokes, safe navigation chords, user-tool and offset
fixes); `IsoABorder` and its export integration (E3, E3-R1); orientation cue (F1);
authoring memory (F2, F2-R1); File → Insert (F3); compact thickness entry and embedded PDF
fonts (F4-A, F4-D); About, version `1.1.0` / `2026-10-15`, runtime distribution condition
and the G-R1 Open-route correction (G).

Retained limitations and open items (all owned; see the debt reconciliation): physical PDF
text size `TD-PDF-1` and `OBS-F4P-FREEHEP-XREF-SIZE` (G15); the uncontracted `ExportArea`
lifecycle of API transports (B-1 residue); the tool-replacement working layer (B-2, G11);
`OBS-R6P-SAVE-WARNING` (P-4, specification/implementation discrepancy, not waived); the
user-library collision policy (P-16); Graphics View 2 without GeoCeDG modes (P-7); F1
exclusions; AutoCAD releases other than 2026 unverified (O-11); Asymptote label compilation
unverified (L-7).

## C. Verification and reproducibility

**Assessment: the executable authority is coherent; R7's own `FINAL` is the deciding
evidence.**

- Registry at the base: 185 nodes; `FINAL` plan `3d8752b9…`, 68 nodes, coverage `COMPLETE`;
  `INTEGRATION` 46 nodes; 22 `INFRA_UNIT` leaves; plans unchanged by R7.
- Receipt flow (R1): a `FINAL` receipt is emitted only for a completed, accepted, complete
  run; closeout inspection is identity-only (`tools/agent/phase-closeout.ps1 -Action
  INSPECT`).
- JUnit inventory: the executed `final.shared` / `final.desktop` identities were refreshed by
  G-R1 and accepted by its `INTEGRATION` (Desktop 2153 tests, 1 skipped; shared 6992, 10
  skipped); R7 changes no test.
- Leaves not in `FINAL` and covered by R7's focused runs: `infra.guide-structure-diagnostic`
  (`PHASE -Phase PRE-G9B-R5-A`; `OBS-R5A-GUIDE-STRUCTURE-CHECKS-OUTSIDE-FINAL`),
  `ggbscript-matrix.product` and `static.ggbscript-matrix-inputs.semantic`
  (`PHASE -Phase PRE-G9B-R6`; the matrix was last executed in E3, before F1–G).
- Not enforced by any profile: external tools (Ghostscript, LaTeX, Asymptote, AutoCAD);
  they remain focused evidence and author smoke.
- Standing diagnostics (`diagnostic.governance` FINDING, `diagnostic.historical-consistency`
  UNAVAILABLE; register O-26) are verdict-neutral.
- Historical verifiers outside the registry (G9X1, G9U0-R2, G9U1 standalone scripts) are not
  live authority; their known failures stay recorded (O-24, `BASELINE-G9U1-BRANDING-EVIDENCE-PIN`).
- Intermittent full-suite observations O-1, O-2 and O-27 did not recur in the T and T'
  `INTEGRATION` runs; R7's `FINAL` is a further monitoring run.

## D. Technical debt

**Assessment: every retained obligation has an accountable owner; author decisions remain.**

Before R7, 38 R-track items had no live owner, including
`TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`. R7 designated `AUTHOR — ACCOUNTABLE`,
`NEXT_CHECKPOINT = PRE-G9B-R7 AUTHOR REVIEW` for each, under the author's ownership rule; no
status was changed. The nine R0 items are closed (one needs confirmation by identifier).
The R6-plus register keeps 134 entries and 0 unowned rows. Six debt decisions
(`DEC-R7-D1` to `DEC-R7-D6`) are proposed; none blocks the R7 technical candidate.

## E. Roadmap readiness

**Assessment: the roadmap states the exact status of each phase after R7's factual
corrections; the G11/G12/G15 revision awaits the author.**

Realistic next options, none authorized here:

1. **R7 closeout** — author review of the exact candidate, author smoke, the debt decisions
   and the G11/G12/G15 decisions; a documentary closeout and a separate exact-SHA
   publication authorization.
2. **A bounded documentary or contract activity** — for example the P-4 save-warning
   resolution, the P-18 per-clause disposition, the G9-R4 living-document correction, or a
   document-session lifecycle contract for the B-1 residue.
3. **An author-approved adjustment of G11/G12/G15 planning** — adopting the proposed wording,
   then the design of one gate (for example G15 `DrawingSheet` with the `TD-PDF-1` contract,
   or G11 hierarchy) under its own authorization.
4. **Preparation for G9B** — `G9B` stays `DESIGNED — NOT AUTHORIZED`; its hard dependency is
   `G9A3` and the primitive contract, nothing in `PRE-G9B-R` is a hard dependency of it, and
   a G9B authorization needs its own explicit author instruction and base.
5. **Another separately authorized priority** — for example a public-release decision for
   `NC` (license audit and release gate) or verification maintenance (unregistered historical
   suites; guide-structure leaf in `FINAL`).

Technical readiness for any of these is not authorization to implement it.

## Remaining technical risks

- Intermittent full-suite observations (O-1, O-2, O-27) with no causal attribution.
- The JVM-global `AutoColor` state affecting output digests in tests (O-23).
- External-tool coverage is manual (LaTeX, Asymptote, AutoCAD, PDF viewers).
- Session-only `ExportArea`, ISO link and `drawingScale` limit reproducible publication until
  G15 decides their persistence.
- Approved behaviour still marked "pending" in some approved specifications (P-18).

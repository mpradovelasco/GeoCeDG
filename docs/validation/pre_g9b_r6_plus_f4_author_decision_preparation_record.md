# PRE-G9B-R6-plus-F4 — author-decision preparation

```text
STATUS               = AUTHOR DECISIONS PREPARED — PENDING (no decision is taken here)
PREPARED BY          = PRE-G9B-R6-plus-F4-PREP (2026-10-13) on 847bb20a
EVIDENCE             = pre_g9b_r6_plus_f4_preparation_characterization_report.md
DESIGN               = pre_g9b_r6_plus_f4_reconciled_design_candidate.md
                       (PROPOSED — PENDING AUTHOR REVIEW)
IMPLEMENTATION       = NOT AUTHORIZED
selfApproved         = false
```

Each decision lists the recommendation, alternatives and proposed wording. A
recommendation is not a decision.

## Summary sheet

| Item | Existing authority | Verified state | Work remains | Recommendation | Footprint | Proposed class |
|---|---|---|---|---|---|---|
| F4-A compact style-bar thickness | POST-E2-P1-R2 closeout (accepted enhancement) | no numeric entry in the compact selector; reusable product-gated field exists | yes | INCLUDE — IMPLEMENTATION CANDIDATE | Desktop; one profile-gated inherited host hook | within F4 `INTEGRATED_PHASE` |
| F4-B DXF / AutoCAD | E3 closeout (author confirmed AutoCAD 2026) | AutoCAD 2026 opens every G5 family, both routes, AUDIT clean on `847bb20a`; other versions not rechecked | no product work | CLOSE — ALREADY RESOLVED; coverage gap recorded; optional VALIDATION ONLY | none | — |
| F4-C `TD-PDF-1` | P1-R1 closeout; physical-pdf-style-sizes §4 | no defect against an approved contract; texts follow S3 (model units), labels follow view pixels | contract design, not implementation | DEFER — NOT IN F4 | later: export layer (labels) and presentation hook (texts) | later |
| F4-D `TD-PDF-2` | P1-R1 closeout | PRODUCT DEFECT CONFIRMED, inherited (`c4fd03ce`), user-reachable, GeoCeDG and Classic | yes, one line | INCLUDE — IMPLEMENTATION CANDIDATE | inherited vendored FreeHEP, registered | within F4 `INTEGRATED_PHASE` |

## DQ-F4-1 — F4 scope

- **Recommended:** F4 implements F4-A and F4-D; F4-B is closed by documentary
  disposition; F4-C is deferred outside F4.
- **Alternatives:** F4-A only; F4-D only; include F4-C option C-1 (labels) as well.
- **Proposed wording:** "F4 implements the compact style-bar thickness entry and the
  correction of the embedded-font PDF route; the DXF recheck is closed as resolved for
  AutoCAD 2026; TD-PDF-1 is deferred to a separate text-size contract decision."

## DQ-F4-2 — host of the style-bar field

- **Recommended:** A-1, one profile-gated call in the inherited `EuclidianStyleBarD`
  (registered upstream modification), reusing `GeoCeDGLineThicknessField`.
- **Alternatives:** A-2, a GeoCeDG style-bar subclass installed by the Graphics 1 and
  Graphics 2 factories (no inherited diff); A-3, defer.
- **Proposed wording:** "A-1 is approved; Classic is unchanged."

## DQ-F4-3 — keyboard-focus fallback (`FG-F4A-1`)

- **Recommended:** if the real window shows that the non-focusable popup blocks typing,
  a profile-gated focusability of that one popup while the GeoCeDG field is present is
  admissible; any other change stops for a new decision.
- **Alternatives:** stop at the gate without fallback; host the field outside the popup
  (new UI, not recommended).
- **Proposed wording:** "Fallback F-1 is admissible under the stated limits."

## DQ-F4-4 — behaviour of the style-bar field

- **Recommended:** identical to the approved Properties field (whole numbers within the
  slider range, Enter or focus loss, refusal restores, one undo step per accepted
  entry); the inherited couplings are kept and documented: the selected line type is
  applied together with the thickness, and in a creation mode the construction defaults
  are edited outside the undo history.
- **Alternatives:** thickness-only application (would change inherited
  `applyLineStyle` semantics; not recommended); decimals (no property support).
- **Proposed wording:** "The field behaves as the Properties field; inherited couplings
  are accepted as documented."

## DQ-F4-5 — correction of the embedded-font route

- **Recommended:** D-1, `AbstractCharTable.toName(char)` delegates to
  `toName(Character.valueOf(c))`: a one-line inherited correction that restores the
  upstream semantics broken by `c4fd03ce`, also correcting Classic.
- **Alternatives:** D-2, GeoCeDG-gated removal of the embedded-font choice for PDF;
  D-3, defer.
- **Proposed wording:** "D-1 is authorized as a registered inherited modification;
  every currently successful export must stay byte-identical."

## DQ-F4-6 — DXF / AutoCAD disposition

- **Recommended:** close the F4 DXF recheck as `RESOLVED — AUTHOR-VERIFIED` for AutoCAD
  2026, supported by the agent re-check on `847bb20a`; record AutoCAD 2024 and other
  releases as `NOT RECHECKED — COVERAGE GAP`; an external validation with other versions
  is optional and author-controlled.
- **Alternatives:** keep the item open until another version is checked.
- **Proposed wording:** "The DXF recheck is closed for AutoCAD 2026; other versions are
  an accepted coverage gap."

## DQ-F4-7 — structure and verification class

- **Recommended:** one F4 implementation (F4-A + F4-D), class `INTEGRATED_PHASE`
  (registered phase `PRE-G9B-R6-PLUS-F4` + `INTEGRATION`), with author smoke for keyboard
  entry in the popup and for an embedded-font PDF opened in a PDF reader.
- **Alternatives:** two independent `BOUNDED_PHASE` tracks (F4-1 style bar, F4-2
  embedded fonts); `BOUNDED_PHASE` for the combined F4.
- **Proposed wording:** "F4 is one `INTEGRATED_PHASE` implementation, frozen at its
  authorization."

## DQ-F4-8 — `TD-PDF-1` disposition

- **Recommended:** reclassify `TD-PDF-1` as `CHARACTERIZED — CONTRACT DESIGN REQUIRED —
  NOT AUTHORIZED` (no defect against an approved contract) and decide a physical
  text-size contract (options C-0 to C-3 of the design candidate §5) outside F4.
- **Alternatives:** C-0, close as accepted behaviour with a guide explanation; include
  C-1 (physical labels) in F4.
- **Proposed wording:** "TD-PDF-1 stays open as a contract-design debt outside F4."

## DQ-F4-9 — records

- **Recommended:** reclassify `TD-PDF-2` as `CONFIRMED — INHERITED PRODUCT DEFECT —
  OWNED BY F4`; register `OBS-F4P-FREEHEP-XREF-SIZE`, `OBS-F4P-S3-SPEC-HEADER` and
  `OBS-F4P-ADR0035-CONSEQUENCE-LINE` for the G structured register without editing the
  approved documents now.
- **Alternatives:** correct the two stale headers in a separate documentary task.
- **Proposed wording:** "The records are reclassified and registered as proposed."

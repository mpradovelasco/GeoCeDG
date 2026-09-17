# PRE-G9B-R4 — SplineV2 and list authoring UX

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R4` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
```

<!-- geocedg-field: objective -->
## Objective

Give `SplineV2` real mode semantics and correct contextual help, and add an
ordered "Create List from Selection" tool producing an ordinary `GeoList`.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze `VERIFICATION_CLASS = BOUNDED_PHASE`
before implementation begins, with `frozenAtPhaseStart=true`; a new `PHASE`
selection is registered for `PRE-G9B-R4`.

`AD-R0-8` is author-decided: the Desktop may construct the existing canonical
closed form **only through an explicit closed-form user action**, and may
synthesize the repeated first/last point member the existing kernel
representation requires. **Closedness must never be inferred from coordinate
proximity, visual coincidence or an ambiguous repeated click.**

`AD-R0-5` is author-decided: the invalid `SplineV2` tooltip
(`TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`) is corrected **as part of this phase**,
not as an independent microphase.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace this field with the exact commit and tree the author explicitly
authorizes for this phase. A moving branch name is not sufficient. **Execution
is forbidden while this field remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

`AGENTS.md`; current Desktop source for the Euclidian controller, mode
constants, the GeoCeDG action registry and profile; the GeoCeDG application
profile manifest; the localization bundles; `geocedg/specs/curves/semantic-spline-2d.md`
for the closed-form contract; `geocedg/specs/ui/cedg-workspaces.md` and
`geocedg/specs/ui/g9u1-construction-interaction.md`; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§2.2 and the
[R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
§4 as planning input only.

Two R0 findings prevent wasted work: the tool **icon already exists** as an owned
SVG and is already bound to the action key, and the action is **already
rendered** in the construction-semantic-curves profile flyout. What is missing is
mode *semantics*, not an asset and not toolbar rendering.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- A `SplineV2` mode constant and mode text returning a `SplineV2.Tool` key, plus
  `SplineV2.Tool` / `SplineV2.Help` entries in the default, `_en` and `_es`
  bundles — this alone repairs the contextual-help action and the toolbar help
  strip.
- The profile action converted from command-kind to `upstream-mode` with an
  audited numeric id, so the registry takes the set-mode branch and the flyout
  mode selector recognizes it.
- A controller collection branch modelled on the existing polyline collection,
  accumulating into the ordered selected-point list and requiring at least three
  points, with the existing polyline preview seam reused as a provisional control
  polygon.
- The input-help panel selection seam, so that invoking `SplineV2` opens the
  panel **with `SplineV2` selected** and renders the existing localized syntax
  from the default, `_en` and `_es` command bundles. R0 characterized this as D2
  defect 2: the action currently opens the generic panel with nothing selected,
  which is **not** a missing-key degradation — the syntax keys already exist.
- The ordered "Create List from Selection" tool.
- Correction of `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`.
- Optionally, moving `SplineV2` to the geometry command table.

### Intended `SplineV2` tool flow

```text
activate -> select ordered points -> finish -> degree defaults to 3
         -> optional explicit degree change -> one normal construction commit
```

Degree defaults to `3` with no prompt; changing the degree is a **second,
explicit gesture**, never inferred. One kernel creation call, returning through
the normal end-of-mode path so the existing single undo-store callback produces
one undo step.

### "Create List from Selection" contract

| Aspect | Contract |
|---|---|
| Ordering | exactly the click order from the ordered selection list; never construction order, label order, hit order or z-order |
| Duplicates | rejected by default, because the host selection path toggles; a repeated member needs its own explicit gesture and must never arise implicitly |
| Mixed types | allowed; the output is an ordinary `GeoList`. Consumers validate and report their own localized errors |
| Cancel | Escape or a mode switch clears the pending selection and creates nothing; no partial list is committed |
| Undo/redo | one undo point per committed list, from the existing end-of-mode callback; a cancelled collection stores nothing |
| Downstream dependencies | normal dependent `GeoList` in the ordinary DAG |
| Member deletion / redefinition | inherited host behaviour for a dependent list; nothing special-cased for splines |

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No spline mathematics, degree admissibility, span or knot computation,
closedness detection or continuity logic in the frontend. **No new
CeDG-specific list semantic type** — the output is the ordinary host `GeoList`.
The `SplineV2` tool must **not** be required to create a visible auxiliary list;
its default commit uses the bare-point form, whose wrapped list is unlabeled and
suppressed. No change to `CmdSplineV2` argument semantics. No synthesizing a
repeated closing member outside the explicit closed-form user action `AD-R0-8`
permits. Do not touch `R5-A`, `R5-B`, `R6`, `R7`, Locus V2 tools, `G9B`, `G9C`,
`G9U2`, productive `G10` or further `G12`. Do not publish, tag, release or push a
publication ref.

## Architectural placement

Desktop/frontend and localization resources. **No kernel change for the open
forms** — the kernel command surface is already complete: point list; list plus
degree; list, degree and weight function; and bare point arguments wrapped into a
suppressed list, with degree defaulting to 3.

The frontend collects an ordered selection and nothing more. It never becomes
geometric authority.

## Required design/specification

A UI specification under `geocedg/specs/ui/` for the two tools, consistent with
the existing workspace and interaction specs.

## Geometric invariants and degeneracies

Spline mathematics stays in the kernel. Note the R0 precision that shapes help
text: explicit-degree validation — the degree range and the degree ≤ point-count
ceiling — runs **only** on the forms that supply a degree; default-degree paths
bypass it, so an over-large point set is rejected later by the polynomial model's
own guard and surfaces as the localized invalid-definition message. The forms
therefore differ in where and how they fail, and the help text must not claim
otherwise.

Closedness is real kernel state declared solely by a repeated first/last list
member; the public creation entry carries no closed parameter. `AD-R0-8` permits
the Desktop to synthesize that member **only** under an explicit closed-form user
action.

## Compatibility and serialization

No change to `CmdSplineV2` argument semantics, to the document format, or to
persisted identity. Existing constructions and their reopen behaviour are
unaffected.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: ordered collection and commit; cancel; undo/redo; the three-point
minimum; degree default and explicit change; the over-large point set failing
through the model guard with the localized message; mode text and help-key
resolution; the input-help panel opening with `SplineV2` selected and rendering
the existing localized syntax; list ordering, duplicates, mixed types, cancel,
undo, downstream dependency and member deletion/redefinition; the corrected
tooltip matching an actually accepted form; and the explicit closed-form action,
including that closedness is never inferred.

Acceptance: the new `PHASE` selection registered for `PRE-G9B-R4`. Then
`git diff --check` and one clean immutable candidate commit.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R4` and its
exact implementation base.

`R5-A`, `R5-B`, `R6`, `R7` and every later gate remain unauthorized. No tag,
release, publication or commercial action is implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

Acceptance criteria carried from the R0 staged design as planning input, not as
accepted specification:

- activating the tool, selecting ordered points and finishing produces **one**
  construction commit and **one** undo step with degree 3;
- an explicit degree change is a **separate gesture**;
- contextual help describes `SplineV2`, not the previously active tool;
- **the input-help panel opens with `SplineV2` selected and shows the existing
  syntax**;
- the list tool produces an ordinary `GeoList` in click order.

The phase stops with a candidate pending author review. **Author smoke is
required**: build a spline from ordered points with the tool and with an explicit
degree, and build a list from selection and feed it to `SplineV2`. Author
approval is an explicit decision naming the exact accepted commit.

## Required artifacts

The UI specification; source, resources and focal tests; a candidate report under
`docs/validation/`; machine-readable evidence under `geocedg/validation/` where
applicable; the exact required-level command with exit code and log path;
bootstrap- and infrastructure-impact outcomes; and `GUIDE_IMPACT` — a new public
tool normally requires a user-guide update.

## Stop conditions

Stop and report rather than guess for any requirement that would place spline
mathematics in the frontend; if the closed-form gesture cannot be made
unambiguous without inferring closure from proximity or a repeated click; if the
list tool would require a new semantic type; or while `IMPLEMENTATION_BASE`
remains `UNRESOLVED`.

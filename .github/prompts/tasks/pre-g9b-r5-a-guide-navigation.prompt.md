# PRE-G9B-R5-A — user-guide index, outline and navigation

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R5-A` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
```

<!-- geocedg-field: objective -->
## Objective

Give the bilingual ES/EN user guide a derived table of contents, working
anchors, a left navigation tree and practical scroll synchronization.

One structured heading and navigation model **derived from the Markdown
sources**. Two independently maintained manual indexes are explicitly rejected.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze `VERIFICATION_CLASS = BOUNDED_PHASE`
before implementation begins, with `frozenAtPhaseStart=true`; a new `PHASE`
selection plus `STATIC` for the structural diagnostic.

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

`AGENTS.md`; the current Desktop guide renderer and guide window; the packaged
guide resources and their build copy task; the authored sources
`docs/user/geocedg_user_guide_en.md` and `docs/user/geocedg_user_guide_es.md`;
`geocedg/specs/operations/documentation-maintenance.md` and
[the documentation architecture](../../../docs/architecture/geocedg_documentation_architecture.md);
the POST-P1-DOC-HELP audit matrix and candidate report; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§4 (`PRE-G9B-R5-A` block) as planning input only.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Anchor emission before each heading in the renderer.
- The renderer returning a structural outline alongside the HTML.
- A split-pane guide window with a navigation tree whose selection scrolls to a
  reference.
- Scroll-position synchronization between the document and the tree.
- A `STATIC`-tier structural diagnostic asserting EN/ES alignment.

### The exact anchor rule

The two editions are already structurally aligned — identical heading-level
sequences, identical decimal numbering prefixes and identical ordered stable
section identifiers — and neither contains a single Markdown link or nested list.
Anchors are therefore **computed, never authored**, and never derived from
translated prose.

There are 17 section markers but only 16 level-2 headings, because the **first
marker sits on the level-1 title**. The invariant that actually holds in both
editions is:

> every marker is immediately followed by an h1 or h2, and every h1/h2 is
> immediately preceded by a marker.

The level-1 heading therefore takes the first marker's identifier; inventing a
synthetic root identifier would orphan an authored, parity-tested id. Level-3
headings take the enclosing marker id plus the decimal numbering token, which
diffs clean across editions.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No two independently maintained manual indexes. No separate JSON or YAML outline
resource. **No slugifying translated heading text** as identity. No transformed
packaged artifact — the Markdown stays the single authored source and the
packaged copies stay byte-identical. **No hyperlink listener**, which an existing
test forbids; the naive design of clickable in-body TOC links would break that
contract and would be its own authorized change. Do not touch `R5-B`, `R6`, `R7`,
guide content, the packaged resource contract, `G9B`, `G9C`, `G9U2`, productive
`G10` or further `G12`. Do not publish, tag, release or push a publication ref.

## Architectural placement

Desktop help presentation, documentation convention and an operational structural
audit. Help and navigation are **never geometric authority** and must not be
consulted by intersection, identity, DAG or serialization code.

## Required design/specification

Extend the documentation maintenance contract with the anchor derivation rule.

## Geometric invariants and degeneracies

**Not applicable.** This phase changes presentation and documentation structure
only. Geometry, kernel semantics, the dependency graph and serialization remain
unchanged.

## Compatibility and serialization

The authored Markdown remains the single source. The packaged copies must stay
byte-identical to the authored sources, and the existing packaged-copy
byte-identity test must continue to pass. Anchor emission is additive and must
not change existing tag counts, so existing renderer parity tests still hold.

One latent renderer detail to respect: an indented list line following an open
list item is currently absorbed as a wrapped continuation of that item, with its
marker left visible. Both guides contain zero nested lists, so this is latent
rather than live — but it is another reason an in-document TOC written as an
indented list is out of contract.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: anchor vector equality and uniqueness across editions; outline
construction; existing renderer parity tests unchanged; the new structural
diagnostic.

The structural diagnostic must assert, by pure structural text extraction with no
prose interpretation: that the EN and ES ordered vectors of
`(heading level, numbering prefix, enclosing section id)` are equal element for
element; that every marker is immediately followed by an h1 or h2 and every h1/h2
immediately preceded by a marker; that every h3 carries a numeric prefix; and
that derived anchor ids are unique within each edition.

Acceptance: the new `PHASE` selection plus `STATIC`. Then `git diff --check` and
one clean immutable candidate commit.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R5-A` and
its exact implementation base.

`R5-B`, `R6`, `R7` and every later gate remain unauthorized; in particular this
phase does **not** authorize the canonical English command surface. No tag,
release, publication or commercial action is implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

The phase stops with a candidate pending author review. **Author smoke is
required**: open the guide in both languages and navigate. Author approval is an
explicit decision naming the exact accepted commit.

## Required artifacts

The documentation-maintenance amendment carrying the anchor rule; source and
focal tests; the structural diagnostic and its registration; a candidate report
under `docs/validation/`; the exact required-level commands with exit codes and
log paths; bootstrap- and infrastructure-impact outcomes; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess for a design that requires nested lists or
in-body links, which are outside the renderer contract; for any need to change
the authored guide structure to make navigation work; if the packaged-copy
byte-identity contract would have to be relaxed; or while `IMPLEMENTATION_BASE`
remains `UNRESOLVED`.

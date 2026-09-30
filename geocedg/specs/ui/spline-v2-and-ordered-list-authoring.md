# Spline V2 and ordered-list authoring interaction

- Phase: `PRE-G9B-R4`
- Status: **CANDIDATE — PENDING AUTHOR REVIEW** (`selfApproved=false`);
  corrective candidate after the author smoke of the initial candidate
- Layer: Desktop/frontend interaction, application-profile action configuration
  and localization/help
- Geometric authority:
  [Semantic Spline V2](../curves/semantic-spline-2d.md) and the existing
  `SplineV2` command; this document defines no spline mathematics
- Consistent with: [CeDG workspace contract](cedg-workspaces.md) and
  [G9U1 Construction interaction](g9u1-construction-interaction.md)
- Kernel, command, serialization and durable-identity effect: **none**

## 1. Scope

This contract defines two Desktop authoring interactions of the GeoCeDG product:

1. graphical creation of a `SplineV2` from an explicit ordered selection of
   existing points, in an open default-degree form, an open explicit-degree form
   and an explicit closed form;
2. an ordered **Create List from Selection** tool that produces an ordinary host
   `GeoList` in click order.

It also fixes the `SplineV2` contextual help, the Input Help selection and the
invalid Algebra Input tooltip (`TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`).

It does not define or change interpolation, degree admissibility, default-degree
behaviour, weight functions, spans, knots, periodic representation, validity,
degenerations, DAG dependencies, persistence or identity. Those remain owned by
the kernel and the `SplineV2` command exactly as they are.

## 2. Frontend/kernel boundary

```text
ordered user selection (Desktop)
        -> one ordinary SplineV2 / List construction command (kernel)
        -> kernel validity, representation, DAG, identity, persistence
```

The Desktop collects explicit user intent: an ordered sequence of existing
objects, the choice of tool, and — only in the explicit-degree tool — the degree
the user types. It then issues **one** ordinary construction through the
existing command dispatcher, passing the selected objects by reference. It never
builds a command from labels, never compares coordinates, never evaluates or
validates a degree, never decides closedness and never computes or displays
spline geometry of its own.

## 3. Actions and modes

Each tool is one profile action of kind `upstream-mode` bound to one audited
numeric mode. The ids are the next free ids after the GeoCeDG block `133–136`;
none is otherwise used in `EuclidianConstants`.

| Action id | Mode constant | Id | Mode text key | Form issued |
|---|---|---|---|---|
| `semantic.spline-v2.create` | `MODE_SPLINE_V2` | 137 | `SplineV2.Tool` | open, default degree |
| `semantic.spline-v2.create-degree` | `MODE_SPLINE_V2_DEGREE` | 138 | `SplineV2.Degree.Tool` | open, explicit degree |
| `semantic.spline-v2.create-closed` | `MODE_SPLINE_V2_CLOSED` | 139 | `SplineV2.Closed.Tool` | closed, default degree |
| `construction.list-from-selection` | `MODE_ORDERED_LIST` | 140 | `OrderedList.Tool` | ordinary dependent list |

`semantic.spline-v2.create` keeps its stable id, owned icon and flyout position;
only its kind changes from `command` to `upstream-mode`. No second `SplineV2`
action authority, icon or toolbar entry is introduced. The two `SplineV2`
variants are presented in the same semantic-curves flyout and reuse the owned
`SplineV2` artwork, as the two `LocusLength` modes already reuse the Locus V2
artwork; their accessible names distinguish them. The list tool reuses the
inherited host list artwork already shipped for the host List tool, as the
ZoomWindow action reuses the host zoom artwork.

The `SplineV2` tools keep the `cedg.locus.v2` feature requirement and the
`locus-v2-product` availability of the original action. The list tool has no
feature requirement and is GeoCeDG-only (`product-only`).

### 3.1 Toolbar presentation

List authoring owns a dedicated toolbar presentation group. The presentation
group `construction-lists` (name key `Group.Construction.Lists`, "List tools" /
"Herramientas de listas") contains only `construction.list-from-selection`. It
is the second toolbar group, immediately right of the Move/selection group, and
is rendered as an ordinary native mode group. The same group is the tool's one
Construction menu entry. The tool is no longer part of the Polygons group; its
action id, mode, icon and catalog cluster membership are unchanged, and no
second action, icon or toolbar entry exists.

### 3.2 Toolbar selected state

Toolbar selected state follows host mode authority. The selected presentation of
every mode action — the native mode groups and the mode entries of the mixed
semantic-curves flyout alike — is set from the host mode when the mode changes.
There is no parallel selection state:

- choosing any of the three `SplineV2` tools, from the toolbar, the flyout or a
  menu, shows the semantic-curves flyout selected with that tool as its active
  entry;
- switching between the variants updates the active entry and keeps the flyout
  selected;
- a refresh of action names, availability or toggle states (for example when
  the menu bar or the Algebra Input visibility is updated) never clears it,
  because a mode action has no toggle state of its own;
- leaving the mode clears the flyout selection normally, as the newly selected
  mode's group becomes selected. A mode without a toolbar button keeps the
  inherited profile-toolbar behaviour of every tool: the previous group stays
  shown (observation `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION`).

Toggle state (`SELECTED_KEY`) remains the authority only for the flyout's
non-mode toggle actions.

## 4. `SplineV2` tool lifecycle

### 4.1 Collection

1. Activation sets the mode and clears any previous selection.
2. Each click on an existing point appends it to the ordered selected-point list.
   The order is exactly the click order.
3. Clicking a point that is already collected deselects it (the host toggle);
   the same point can therefore never be collected twice by an ordinary click.
4. Clicks on empty canvas or on non-point objects collect nothing and create
   nothing. Points are created beforehand with the ordinary Point tools.
5. While collecting, the host polyline preview shows the provisional control
   polygon through the collected points and the pointer.

### 4.2 Finish

The finish gesture is the host Polyline gesture: **select the first collected
point again**, once at least three points have been collected. Before three
points the re-click is an ordinary deselection. Selecting any other collected
point again is an ordinary deselection, never a finish and never a closure.

The finish gesture carries no closedness meaning in any tool: closedness comes
only from the tool the user chose (section 5).

### 4.3 Commit

On finish the tool issues exactly one ordinary `SplineV2` construction:

| Tool | Command form issued | Degree |
|---|---|---|
| Spline V2 | `SplineV2(P1, …, Pn)` (bare points) | the command default, `3` |
| Spline V2 with degree | `SplineV2({P1, …, Pn}, d)` | the value the user entered |
| Closed Spline V2 | `SplineV2(P1, …, Pn, P1)` (bare points) | the command default, `3` |

- The default path shows **no prompt**.
- In the explicit-degree tool the finish opens the host number-input dialog,
  prefilled with `3`. Entering a number and confirming is the second, explicit
  degree gesture. The Desktop passes the entered number unchanged; admissibility
  is decided by the command. Cancelling the dialog creates nothing.
- The construction returns through the normal end-of-mode path, which stores
  exactly **one undo point** for the committed construction.
- The committed spline is labelled like any typed `SplineV2`. The command
  promotes its direct arguments to named construction objects — the point list
  and the degree — exactly as it does for typed input. The tool creates **no
  list of its own** before or after the command and no helper point.

### 4.4 Cancel

Escape, a tool change or a workspace switch clears the pending collection and
the preview. Nothing is created and no undo point is stored. A cancelled degree
dialog discards the collection.

### 4.5 Preview authority

The control-polygon preview is a presentation drawable of the view only. It is
not a `GeoElement`, not an input of the committed spline, not evaluated and never
used for metrics, incidence, closedness or identity. The spline appears only
after commit, drawn from the kernel result.

## 5. Explicit closed form (`AD-R0-8`)

The closed form is authored **only** by choosing the **Closed Spline V2** tool.
This is the host's established pattern for an explicit construction variant —
one tool per variant, as with Polyline and Polygon — so the choice is explicit
and unambiguous before the first click.

On finish, the closed tool synthesizes the repeated closing member required by
the existing kernel representation by appending the **same point object** as the
first collected point, as the permitted `AD-R0-8` synthesis. The open tools never
append it.

Closedness is never inferred by the Desktop from coordinate proximity, visual
coincidence, screen position, a repeated click or nearest-point logic. The
finish re-click is identical in the open and closed tools.

**Kernel boundary, unchanged.** The kernel decides periodicity from the supplied
list by its existing normative rule (equal first and last input points). If a
user explicitly collects two *distinct* points whose values are exactly equal as
the first and last members of an open spline, the kernel rule applies to that
input as it does for typed input. The Desktop neither inspects nor alters this;
it is recorded as an observation, not changed.

## 6. Degenerations and errors

The Desktop adds no validation. Results follow the command contract:

| Situation | Result |
|---|---|
| fewer than three collected points | no finish is possible; nothing is created |
| explicit degree outside `3…point count` or beyond the work policy | the command rejects the arguments with its localized error; nothing is created |
| default-degree form with more points than the model's bounded policy | the command creates the spline, which the model guard leaves **undefined** |
| non-numeric or cancelled degree entry | the host dialog keeps or discards the entry; nothing is created |
| feature `cedg.locus.v2` unavailable | the tool reports the existing feature-disabled message and creates nothing |

Help text must not claim that the forms fail alike. The explicit-degree forms
check degree range and point count before creation. The default-degree forms do
not; an oversized point set yields an undefined spline.

## 7. Create List from Selection

| Aspect | Contract |
|---|---|
| Input | any selectable objects; mixed types allowed |
| Ordering | exactly the click order of the ordered selection; never construction, label, hit or z-order |
| Duplicates | rejected by the host toggle: clicking a collected object again deselects it; a repeated member is never created implicitly and has no graphical gesture (Algebra Input remains available) |
| Finish | select the first collected object again once at least two are collected |
| Output | one ordinary dependent host `GeoList` from the existing dependent-list algorithm, labelled normally; no CeDG-specific list type |
| Cancel | Escape, tool change or workspace switch clears the pending selection; nothing is created |
| Undo/redo | one undo point per committed list; none for a cancelled collection |
| Downstream | ordinary DAG dependency; a list of points can be passed to `SplineV2` through Algebra Input |
| Member deletion/redefinition | inherited host behaviour of a dependent list; nothing is special-cased |

The tool is general-purpose and is not used internally by the `SplineV2` tools.

## 8. Help, Input Help and localization

- Tool name and help resolve through the mode text: `SplineV2.Tool`,
  `SplineV2.Help`, `SplineV2.Degree.Tool`, `SplineV2.Degree.Help`,
  `SplineV2.Closed.Tool`, `SplineV2.Closed.Help`, `OrderedList.Tool`,
  `OrderedList.Help`, plus the degree prompt `SplineV2.DegreePrompt`, in the
  default, `_en` and `_es` menu bundles. Contextual help therefore describes the
  active `SplineV2` tool, not the previously active one.
- Profile action names are `Spline V2`, `Spline V2 with degree`,
  `Closed Spline V2` and `Create List from Selection` (and their Spanish
  equivalents), through presentation keys where the host mode text differs.
- Input Help visibility is user-controlled. Mode activation alone never opens
  it. Choosing a `SplineV2` tool neither opens nor closes Input Help and does not
  change Algebra Input visibility.
- An explicit help request while a `SplineV2` tool is active — the Input Help or
  command-list help action, contextual help, or the Input Help button of Algebra
  Input — shows the Input Help panel **with the `SplineV2` command selected** and
  scrolled into view, which renders the existing localized command syntax, and
  applies the corrected Algebra Input tooltip. The help actions also show Algebra
  Input, as they always have. The selection is made in one place, where the
  application shows the panel, so every request route behaves alike. No second
  syntax authority is created and nothing is typed into or focused in the input
  field. Outside a `SplineV2` tool, Input Help keeps its inherited behaviour.
- The Algebra Input tooltip lists only accepted forms: bare points or a point
  list for degree `3`, a point list with an explicit degree, and the repeated
  first point for the closed form.
- Command names and syntax remain those of the existing command bundles; the
  canonical command-name surface (`R5-B`) is not touched.

## 9. Persistence and compatibility

No change to `CmdSplineV2` argument semantics, to the native or compatibility
document format, to durable identities or to Classic `Spline`. A spline built by
a tool is the same construction as the equivalent typed command and reopens
unchanged. Existing documents and their reopen behaviour are unaffected. The
Classic diagnostic session does not expose the new actions.

## 10. Validation

Focal Desktop tests exercise the real controller, registry, help panel and
bundles: ordered collection, the three-point minimum, the finish gesture,
default degree `3`, the explicit degree gesture, one commit and one undo point,
cancel, undo/redo, preview authority, parity with the typed command, the closed
form's synthesized member, the open forms' absence of it, the oversized
default-degree outcome, mode text and help keys in `en` and `es`, unchanged Input
Help visibility on tool activation, the Input Help selection on an explicit help
request in `en` and `es`, the tooltip examples, the toolbar selected state of the
three `SplineV2` tools through variant switching, a menu-bar refresh and leaving
the mode, the dedicated List tools toolbar group, and every list-tool row of
section 7. Acceptance is the registered `PHASE` selection for `PRE-G9B-R4`.

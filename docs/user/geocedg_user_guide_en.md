<!-- geocedg-guide-section: about-this-guide -->
# GeoCeDG user guide

GeoCeDG 1.0.0 · English edition

This is the official user guide for GeoCeDG. It describes what the application
does today, how to work with it, and where its current boundaries are. A
Spanish edition with the same structure and the same examples is available as
`geocedg_user_guide_es.md`; the application opens the edition that matches the
active product language.

The guide is written for a technical or scientific user. It is not a developer
manual: it does not describe the build, the verification system or the internal
Java architecture. Where an internal notion is required to understand observable
behaviour, it is explained conceptually.

Everything stated here is backed by the current product, its approved
specifications and its tests. Capabilities that are designed but not available
are named as such, and nothing that is merely planned is presented as present.

---

<!-- geocedg-guide-section: what-is-geocedg -->
## 1. What is GeoCeDG

GeoCeDG is a dynamic-geometry application for **Computer-extended Descriptive
Geometry (CeDG)**. It is built on the shared GeoGebra geometric kernel and
extends it where descriptive-geometry work requires semantics that ordinary
dynamic geometry does not provide.

### 1.1 The central idea: the construction is the object

In CeDG, a geometric result is never separable from the explicit sequence of
constructions that produces it. This has practical consequences you will meet
throughout the application:

- **Constructive traceability.** Every derived object remains linked to its
  defining inputs. Change an input and the result recomputes through the
  dependency graph.
- **Explicit parameterization.** Parameters, their domains and their validity
  ranges are stated, not inferred from what happens to be drawn.
- **Procedure and result are different things.** The curve you see on screen is
  a *rendering* of a semantic object. It is not the object, and it is never the
  authority for a length, an incidence or an intersection.
- **Explicit approximation.** When a value is approximate, the application says
  so, and reports the method and the evidence behind it. An approximation is
  never presented as an exact result.
- **Deliberate degeneracy handling.** Tangency, coincidence, discontinuity,
  branch loss and near-singular configurations produce an explicit status, not
  stale geometry left over from a previous state.

### 1.2 How this differs from CAD

GeoCeDG is not a solid-first modeller. It has no feature tree, no history-based
solid modelling workflow and no generic CAD abstraction layered over the
geometry. The construction itself — its objects, its dependencies, its explicit
parameters — is the model.

The practical difference is that you build by *stating relations*, and the
application preserves those relations. You do not sculpt a result and then hope
to recover the intent behind it.

### 1.3 Version and status

The application identifies itself as **GeoCeDG 1.0.0** in the window title and
under **Help → About GeoCeDG**. That version number identifies the product
state described by this guide. It is not, by itself, a public release: packaging
and distribution are governed separately.

Two capabilities described here — semantic curves (Locus V2 and Spline V2) and
extended DXF export — are enabled by default in GeoCeDG but keep the maturity
`experimental`. They work as normal product behaviour and require no launch
argument; "experimental" means their public contract may still be refined.

---

<!-- geocedg-guide-section: getting-started -->
## 2. Getting started

### 2.1 Starting GeoCeDG

Start GeoCeDG from its installed shortcut. The window title reads
`GeoCeDG 1.0.0`, or `GeoCeDG 1.0.0 — <file name>` once a document is open.

If you are working from a prepared source checkout instead of an installed
copy, the product profile is launched through the Gradle task
`:desktop:desktop:runGeoCeDG`. No additional argument is required to obtain the
capabilities described in this guide.

### 2.2 The main window

GeoCeDG opens in the **CeDG Construction** workspace, which is the product
workspace. Its default layout provides:

- the **Graphics** view, with visible axes and no grid;
- the **Algebra** view;
- the **Algebra Input** bar, with input help.

The **Spreadsheet**, **CAS**, **Construction Protocol** and **Properties**
views are available and initially closed. A saved document or a saved
preference may supply a different layout; **View → Restore Construction
layout** puts the product layout back without touching the geometry.

### 2.3 Product language

GeoCeDG offers two product languages: **English** and **Spanish**. English is
the fallback when no other choice applies.

Choose the language under **Options → GeoCeDG options → Product language…**.
The choice affects menus, dialogs and in-application help, including which
edition of this guide **Help → GeoCeDG user guide** opens.

### 2.4 Preferences

**Options → Preferences…** opens the global configuration. Opening it does not
select an object.

The **Layout & Presentation** tab holds the GeoCeDG presentation settings: the
presentation theme and the presentation sizes described below.

### 2.5 Presentation themes

A presentation theme selects the colours of known interface surfaces. There are
exactly three, chosen under **Preferences → Layout & Presentation → Theme**:

| Theme | Character |
|---|---|
| **Original** | The historical GeoCeDG appearance. This is the default and the fallback. |
| **Scientific Paper** | A warm, low-contrast palette suited to printed-paper work and screenshots for publication. |
| **Cool Geometry** | A cool, neutral palette with a slightly brighter canvas. |

A theme changes only presentation surfaces — frame, menu and toolbar surface,
panels, the Graphics canvas background and separators. It never changes object
colours, styles, visibility, axis or grid meaning, export output, or anything
stored in a document. The choice is a user preference: it survives restart and
is not affected by opening a document.

### 2.6 Presentation sizes

**Preferences → Layout & Presentation → Presentation sizes** offers six
independent controls:

| Control | What it governs | Default |
|---|---|---|
| General UI font size | Input bar, command help, ordinary dialogs | 12 pt |
| Menu font size | The menu bar and its items | 14 pt |
| Toolbar icon size | Toolbar buttons, including pinned user tools | 28 px |
| Algebra font size | The Algebra view and its editor | 12 pt |
| Construction Protocol font size | The protocol table and navigation | 12 pt |
| Graphics font size | Axes, coordinates and object labels in Graphics views | 12 pt |

These are independent: none is derived from another. The font of construction
**Text** objects is a separate setting (**Text font size**) and is not part of
this group, because a Text object belongs to the document.

All six are user preferences. They are never written into a construction and
never appear in a saved file.

---

<!-- geocedg-guide-section: interface-and-workflow -->
## 3. Interface and construction workflow

### 3.1 Views

- **Graphics** is where you construct and where the current tool acts.
- **Algebra** lists the objects with their value, description or definition,
  depending on the display mode chosen under **Options → Algebra display**.
- **Construction Protocol** (**View → Construction protocol**) shows the
  construction as an ordered sequence, which is the direct reading of its
  dependency structure. **View → Show construction navigation bar** toggles the
  step control attached to the Graphics view; it creates no objects and no undo
  step.
- **Algebra Input** accepts commands and definitions.

### 3.2 Menus

The menu bar contains **File**, **Edit**, **View**, **Construction**,
**Options**, **Automation** and **Help**. They are compact projections of a
single action catalog, so an action referenced from several groups still
appears once.

**Construction** is the geometric menu and is organised as:

| Group | Contents |
|---|---|
| Points | Point, Point on object, Midpoint |
| Lines and vectors | Line, Segment, Ray, Vector, Segment with given length, Vector from point |
| Polygons | Polygon, Polyline, Regular polygon, Rigid polygon, Vector polygon |
| Derived constructions | Parallel line, Perpendicular line, Perpendicular bisector, Angle bisector |
| Parameters and drivers | Slider, Angle with given size, Check box, Button, Input box, Animate selected object, Text |
| Relations and intersections | Intersect, Tangents, Relation, Polar/diameter line, Inspect rich result, and the three materialization actions |
| Circles and conics | Circle (two points, three points, centre+radius), Arc, Conic through five points, Ellipse, Parabola, Hyperbola, Compass, Semicircle, Circumcircular arc, Sector, Circumcircular sector |
| Semantic curves | Locus V2, Point on semantic curve, Spline V2, Inspect semantic curve definition… |
| Metrics and validation | Angle, Distance or length, Locus V2 total length, Locus V2 partial length, Area, Slope |
| Similarity transformations | Reflect about point, Reflect about line, Translate by vector, Rotate by angle, Dilate from point |
| Annotations and media | Image |
| Manual projection procedures | CeDG dihedral procedures (not authorized) |

The last entry is visible but unavailable: the dihedral-procedure workspace is
not an authorized capability and reports that when selected.

### 3.3 Toolbar and flyouts

The toolbar projects the frequently used actions of that same catalog, grouped
as Move; Point and Intersection; Lines and vectors; Polygons; Derived
constructions; Circles and conics; Semantic curves; Angles and lengths;
Transformations; Parameters and drivers; Navigation.

**Semantic curves** and **Navigation** are compact mixed flyouts: they show the
last action you chose as the main button. That memory is presentation state
only; it changes no semantics.

An unavailable action explains why it is unavailable and creates nothing.

### 3.4 Selecting, creating and editing

Select with the **Move** tool. Create by choosing a tool and clicking the
required inputs, or by typing a definition in Algebra Input.

Algebra Input shows a preview while you type. **Enter** commits one transaction;
**Escape** cancels; losing focus creates nothing. Nothing is created until you
press Enter.

To edit an ordinary numeric object you may double-click it, press **F2**, edit
its Algebra row directly, or type a compatible assignment such as `k=0.25` in
Algebra Input. All four routes keep the same object; the edit is explicit and is
one undo step.

**Undo** is `Ctrl+Z` and **Redo** is `Ctrl+Y`, also available under **Edit →
History**.

### 3.5 Inspecting a definition

**Inspect definition…** (context menu, or **View → Construction inspection**)
shows how an object is defined, read-only. For semantic curves and their derived
objects, **Inspect semantic curve definition…** additionally shows the semantic
structure: branch, component, semantic address, address status (current and
admissible, or retained/dormant), source, provider, parameter and, for periodic
sources, the periodic lift and seam side.

Inspection never changes an object, never changes identity, and never changes
the global Algebra display mode.

Some objects show a **read-only definition**. That is not a defect: it means
that arbitrary direct redefinition of that object is not an approved operation.
Edit its defining inputs instead. Properties explains this when it applies.

---

<!-- geocedg-guide-section: documents -->
## 4. Documents

```text
.cedg = native GeoCeDG document
.ggb  = compatibility input
```

### 4.1 Native and compatibility formats

`.cedg` is the native GeoCeDG document extension. Save your work as `.cedg`.

`.ggb` is accepted as compatibility input. GeoCeDG opens it without altering it
destructively. Opening a `.ggb` does not convert it, and saving GeoCeDG work as
`.ggb` is not a promise that an external upstream application will preserve
GeoCeDG-only object types.

The extension is a product and input/output identity. It does not change
geometric meaning, object maturity, durable identity or command behaviour, and
renaming a file does not transform its contents. Internally both archives
currently keep the inherited `classic` application code and archive layout;
GeoCeDG does not introduce a new archive format.

### 4.2 Document commands

| Action | Where |
|---|---|
| New file | File → Open group |
| Open… | File → Open group |
| Open recent… | File → Open group |
| Save | File → Save group |
| Save as… | File → Save group |
| Print preview… | File → Print |
| Close | File → Close |

**Save** writes to the current native path when there is one; otherwise it
behaves as **Save as…**. There is no silent downgrade from `.cedg` to `.ggb`
and no destructive conversion in either direction.

If a save fails its pre-validation, the previous file on disk is preserved and
your changes remain unsaved. Nothing is written half-way.

### 4.3 What a document preserves

A saved `.cedg` preserves the construction and its dependency structure,
including semantic curves, their explicit domains and parameters, semantic
points with their branch and canonical parameter, materialized intersection
points with their durable selectors, and the visual layout of the document.

It does not preserve application preferences. Language, theme, presentation
sizes and the installed user-tool library belong to the GeoCeDG profile, not to
the document.

### 4.4 Relation to Classic

The GeoCeDG Classic diagnostic session is a separate process with its own
isolated preferences. It reads ordinary documents, but it does not acquire
GeoCeDG semantic-curve creation. See section 14.

---

<!-- geocedg-guide-section: basic-geometry -->
## 5. Basic geometry

GeoCeDG inherits the ordinary planar construction tools of the shared kernel.
This section covers what you need in order to work with the CeDG capabilities;
it is not a complete manual of every inherited tool.

### 5.1 Points

Create a free point with the **Point** tool or by typing a coordinate pair:

```text
A=(0,0)
```

**Point on object** constrains a point to an existing object. **Midpoint**
creates the midpoint of two points or of a segment.

A point placed on a *semantic* curve is a different, richer construction; it is
described in section 8.

### 5.2 Lines, segments, rays and vectors

```text
g=Line(A,B)
s=Segment(A,B)
r=Ray(A,B)
v=Vector(A,B)
```

The corresponding tools are under **Construction → Lines and vectors**.
**Segment with given length** and **Vector from point** take a numeric input.

### 5.3 Circles and conics

```text
c=Circle((0,0),1)
k=Circle(A,B,C)
e=Ellipse(A,B,C)
```

**Construction → Circles and conics** additionally offers arcs, sectors,
semicircles, the compass, the conic through five points, the parabola and the
hyperbola.

### 5.4 Polygons

**Construction → Polygons** provides the ordinary polygon, polyline, regular
polygon, rigid polygon and vector polygon tools. They behave as in the shared
kernel.

### 5.5 Parameters and drivers

A parameter is an explicit, editable value that other constructions depend on.
Create one by typing an assignment or with the **Slider** tool:

```text
k=1
```

Sliders, check boxes, buttons, input boxes and the angle with a given size are
under **Construction → Parameters and drivers**. A parameter is the normal way
to drive a locus, a transformation or any relation you want to study
dynamically.

### 5.6 Text

The **Text** tool creates a construction Text object. Text belongs to the
document; its logical font size is a document property (**Text font size**), and
in GeoCeDG a Text anchored to real coordinates scales visually with the zoom of
each Graphics view while its content, anchor and logical size stay unchanged.
A screen-positioned Text keeps the inherited fixed size.

### 5.7 Inherited transformations

**Construction → Similarity transformations** provides reflection about a point
or a line, translation by a vector, rotation by an angle and dilation from a
point. Applied to ordinary objects they behave as in the shared kernel; applied
to semantic curves they have the additional semantics described in section 10.

---

<!-- geocedg-guide-section: locus-v2 -->
## 6. Locus V2

### 6.1 What a Locus V2 is

A **Locus V2** is a semantic curve: the image of a dependent construction
evaluated over an explicit, oriented one-dimensional domain.

Informally, you have a *generator* — a driver parameter and the construction
that depends on it — and you declare the interval over which the driver is
allowed to move. The locus is the set of positions that the dependent point
takes over that interval.

### 6.2 Why it is not a rendered polyline

The classical sampled locus is a list of computed points joined by straight
segments. Its identity *is* that sample. A Locus V2 is different: the sample
drawn on screen is a derived representation, produced for display, and it is
never the authority for anything.

In particular:

- lengths are computed from the semantic definition, not from the drawn chords;
- intersections are solved in the original parameters, not against screen
  segments;
- results do not change with zoom, viewport or screen resolution;
- a point "on" the curve carries a semantic parameter, not a vertex index.

This separation is the reason Locus V2 exists. It is also why the classical
`Locus` command remains available and unchanged: the two are different objects
with different contracts.

### 6.3 Structure: generator, domain, orientation, branch, component

- **Generator (source).** The driver and the dependent construction that
  produces the curve. Its identity comes from the explicit semantic
  coordinate/domain, the provider kind and version and its durable inputs —
  never from a slider's visible range, a label, a drawn vertex or the current
  value.
- **Parameter.** The canonical path parameter is the driver/domain parameter.
  It is not the index of a sampled point.
- **Explicit domain.** You state the interval and whether each end is included.
  The domain does not come from the visual limits of a slider or from what is
  drawn.
- **Orientation.** The domain is oriented, which is what makes a partial length
  and a direction meaningful.
- **Branch.** A semantic address within the curve. The default branch produced
  by a generator is `generator.main`.
- **Component.** A maximal connected piece of the curve within a given
  evaluation. Where the construction becomes undefined, the curve is split into
  components; the gap is never bridged.

### 6.4 Currentness

A semantic result is computed for a specific state of the construction. When an
input changes, the result is recomputed and the previous evidence stops being
current. This is why an address can be reported as **current and admissible**
or as **retained/dormant — not current** in the definition inspector.

Currentness matters most when you materialize intersection points: see
section 8.

### 6.5 Creating a Locus V2

Through the interface, **Construction → Semantic curves → Locus V2** offers the
generator and domain selection.

Through Algebra Input, the command forms are:

```text
LocusV2( <Dependent Point>, <Constrained Point> )
LocusV2( <Dependent Point>, <Scalar State>, <Domain Descriptor> )
LocusV2( <Dependent Point>, <Scalar State>, <True Driver>, <Domain Descriptor> )
```

A domain descriptor is a list `{periodic, {start, end, startIncluded, endIncluded}}`.

### 6.6 Worked example

Enter each line once, pressing Enter after each:

```text
u=0
G=(u,0)
dom={false,{0,4,true,true}}
L=LocusV2(G,u,dom)
U=Point(L,"generator.main",1)
V=Point(L,"generator.main",3)
LL=Length(L)
LP=Length(L,U,V)
```

Expected results: **`LL` = 4** and **`LP` = 2**.

The locus here is the segment of the *x*-axis from `(0,0)` to `(4,0)`, because
you declared the driver domain `[0,4]` explicitly. Note that the domain came
from `dom`, not from any slider range and not from what is drawn.

### 6.7 Availability

Locus V2 creation is normal GeoCeDG behaviour and needs no launch argument. The
historical argument `--enableLocusV2=false` is retained as a diagnostic and
compatibility override that starts GeoCeDG *without* the V2 creation surface;
it changes exposure only, never durable identity, serialization or geometric
meaning.

The Classic diagnostic session never acquires V2 creation, whatever arguments
are used.

Locus V2 keeps the maturity `experimental` and is GeoCeDG-only. It supports the
documented generators, explicit domains and targets. It is **not** a generic
`Path`: operations that expect an arbitrary path object are not implied.

---

<!-- geocedg-guide-section: spline-v2 -->
## 7. Spline V2

### 7.1 What a Spline V2 is

A **Spline V2** is a semantic curve built from an ordered list of constructor
points and a degree. It is a first-class semantic object of the same family as
Locus V2: it has an explicit parameter, an oriented domain, branches and
components, and the drawn stroke is a derived representation.

It is a different object from the inherited Classic `Spline`, which is
unchanged and remains available.

### 7.2 Creating a Spline V2

**Construction → Semantic curves → Spline V2**, or Algebra Input:

```text
SplineV2( <List of Points> )
SplineV2( <List of Points>, <Degree> )
SplineV2( <List of Points>, <Degree>, <Weight Function> )
SplineV2( <Point>, <Point>, <Point>, ... )
```

The default branch key of a Spline V2 is `spline-v2/main`.

### 7.3 Constructor points are not semantic points

The points you pass to `SplineV2` are **constructor points**. They define the
curve. They are ordinary free or dependent points, and they do **not** by
themselves carry a semantic position on the resulting curve — even when their
coordinates lie exactly on it.

This is a deliberate contract, and it is the most common source of surprise.
A metric or an incidence query needs a point with admissible semantic
provenance on that curve; cartesian coincidence is not semantic position. See
section 9.

### 7.4 Points on a Spline V2

There are two routes:

- **Interactive.** Choose the ordinary **Point** tool and click on the curve
  *stroke*. Clicking inside a closed curve does not select it. If the semantic
  preimage is unique, one point is created. If several preimages exist, you
  choose explicitly or cancel; nothing is chosen by proximity. Such a point can
  be dragged with Move and keeps its identity while the continuation is unique.
- **Explicit parameter.** Use the address form, which is exact and scriptable:

```text
P=Point(S,"spline-v2/main",0.25)
```

A point created with an explicit parameter is controlled by that parameter, not
by a drag that would silently replace it. The parameter may be a named number
so that you can edit it.

### 7.5 Worked example

In a new construction:

```text
h=0
A=(-2,h)
B=(-2/3,h)
C=(2/3,h)
D=(2,h)
S=SplineV2({A,B,C,D},3)
P=Point(S,"spline-v2/main",0.25)
Q=Point(S,"spline-v2/main",0.75)
M=Length(S)
MP=Length(S,P,Q)
```

Expected results: **`M` = 4** and **`MP` = 2**.

Editing `h` translates the spline without changing either length. That is the
dynamic dependency working as intended: the metric follows the semantic
definition, not the drawing.

### 7.6 Persistence

A Spline V2, its semantic points and their parameters are saved in the native
`.cedg` document and are reconstructed on reopening, with their durable
identities preserved.

### 7.7 Availability

Like Locus V2, Spline V2 creation is normal GeoCeDG behaviour with no launch
argument, keeps the maturity `experimental`, and is not available in the
Classic diagnostic session.

---

<!-- geocedg-guide-section: semantic-points-intersections-materialization -->
## 8. Semantic points, intersections and materialization

### 8.1 Semantic points

A **semantic point** on a semantic curve carries an explicit address: the
source curve, a branch, a component and a canonical parameter, together with
evidence of its currentness. That address — not its coordinates — is its
identity.

Consequently, none of the following defines a point's position on a curve:

- its coordinates;
- its proximity to the drawn stroke;
- the graphical drawing order;
- a visual or list index.

Two points with identical coordinates may have completely different semantic
provenance, and one of them may be usable for a given query while the other is
not.

### 8.2 Rich intersection results

`Intersect(...)` applied to a semantic curve does not return a list of points.
It returns a **rich intersection result**: an object that carries every root
that the solver found, together with its evidence — the branch and component on
each side, the semantic parameters, a residual, an admissibility verdict and a
durable **exact token** identifying that solution.

```text
c=Circle((0,0),1)
R=Intersect(S,c)
```

The relevant command forms are:

```text
Intersect( <Locus V2>, <Supported Object> )
Intersect( <Locus V2>, <Locus V2> )
Intersect( <Rich Intersection Result>, <Solution Token> )
```

A rich result is a normal dependent object: it recomputes when its inputs
change.

### 8.3 Local admissibility versus global completeness

Two independent statements are made about an intersection:

- **Local admissibility** of a single root: this specific solution is certified
  and has a resolved, unique identity, so a point may be created from it.
- **Global completeness** of the result: the solver established that it found
  *all* solutions in the requested region.

These are separate. A result whose global completeness is `NOT_ESTABLISHED`
does not thereby invalidate a locally admissible root. Conversely, a root that
is tangent, ambiguous, stale or insufficiently certified does not become
admissible because it is visible on screen.

The inspector labels these states: **Certified; eligible**, **Rich result
only**, **Already materialized**, and a stale-result warning when the underlying
result has changed since you looked at it.

### 8.4 The inspector

Select the rich result in Algebra and choose **Inspect rich result**
(**Construction → Relations and intersections**, or the context menu). The
inspector lists the solutions with their evidence and lets you:

- **Create one** — materialize the highlighted solution;
- **Create one and close**;
- **Create selected** — materialize an explicitly selected group (`Ctrl` or
  `Shift` to select several);
- **Create all eligible** — materialize every currently admissible root;
- **Close**.

The inspector stays available so you can continue after creating a point.
Solutions already materialized are identified as such.

**Show solution markers** displays transient markers for the candidate
solutions. Markers are presentation only: they are not objects, they are not
saved, and they confer no admissibility.

### 8.5 The exact token

Each admissible solution has an opaque **exact token**. It is the durable
identity of that solution across recomputation, save and reopen. It is not a
coordinate, not an index and not a label.

You do not need to handle tokens manually when you use the inspector — that is
what the inspector is for. The scripted form `Intersect(R,"<token>")` exists for
reproducible or automated work; do not transcribe tokens by hand.

### 8.6 Materialization

Materializing a solution creates an ordinary `GeoPoint` whose parent is that
intersection and whose identity is bound to the exact token. From then on it is
a normal member of the dependency graph.

Three rules govern the lifecycle:

- **Recomputation never creates points.** Changing an input recomputes the rich
  result; it does not silently add points to your construction.
- **Losing admissibility makes an existing point dormant**, not deleted. It
  becomes undefined while its solution is not currently admissible, and the
  definition inspector reports its address as *retained/dormant — not current*.
- **Only its own selector reactivates it.** When the same solution becomes
  admissible again under its exact selector, that same point is reactivated. No
  new point is invented, and no point is reassigned by proximity.

**Auto-create eligible points for new explicit queries** (**Options → GeoCeDG
options**) is an explicit, visible opt-in that applies only to a newly submitted
query. The query and the points it creates form one compound undo step. It does
not run when you merely select, reopen or recompute a result.

### 8.7 Spline V2 × Spline V2

A pair of semantic splines consumes the same rich-result machinery. Several
distinct roots may each be individually admissible. A root whose multiplicity or
identity is unresolved — a tangency, for instance — stays rich-only.

This section describes the workflow and the concepts you need. The full
mathematical treatment of intersection identity is not in scope for a user
guide.

---

<!-- geocedg-guide-section: lengths-and-measurements -->
## 9. Lengths and measurements

### 9.1 Two surfaces

```text
Length(...)       ordinary number
LocusLength(...)  rich result
```

`Length` returns an ordinary numeric object that you can use anywhere a number
is expected. `LocusLength` returns a **rich metric result** that additionally
carries the computation status, the covered domain, the error estimate and the
diagnostics. It is not an ordinary numeric row and should not be expected to
behave as one.

A scalar produced by `Length` on a semantic curve is an adapter over that same
rich evidence: its visible definition reads `Length(S,P,Q)`, while the rich
auxiliary remains its real parent and is visible under advanced inspection.

### 9.2 Total and partial length

```text
Length( <Locus V2> )
Length( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )

LocusLength( <Locus V2> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point>,
             <Direction>, <Boundary Policy>, <Same-position Policy> )
```

The total form measures the whole curve over its declared domain. The partial
form measures the oriented arc between two semantic positions.

Both work on a Locus V2 and on a Spline V2, since a Spline V2 is a semantic
curve of the same family.

### 9.3 Endpoints need semantic provenance

The partial form requires the two endpoints to be **semantic points admissible
on that curve**. This is the practical face of the rule stated in section 8.1.

In the section 7.5 example, `A` and `C` are constructor points: ordinary points
that define the spline. `P` and `Q` are semantic points created with an explicit
address. Therefore:

```text
Length(S,P,Q)   defined
Length(S,A,C)   undefined
```

even though `A` and `C` lie exactly on the curve. There is no proximity
selection and no implicit choice of preimage — which matters as soon as a curve
self-intersects and a position has more than one preimage.

Similarly, a semantic point belonging to a *different* curve is not a valid
endpoint for this one.

### 9.4 Current limitation: endpoints from a spline-pair intersection

`Length(S,P,Q)` may be **undefined** when `S` is a Spline V2 and one endpoint is
a point that was materialized from a **Spline V2 × Spline V2** intersection.

What this is:

- it is **not** a numerical failure of the spline or of the intersection;
- the intersection point is a valid, certified point, and the intersection
  evidence does carry, per side, the locus identity, the semantic revision, the
  branch, the component, the semantic parameter and the token/currentness
  evidence;
- the metric resolution currently accepts semantic points whose source is `S`
  and unique constructor-point occurrences, and does not yet consume the
  intersection provenance that such a point carries.

What to do:

- use a point with an explicit address on `S` (the `Point(S, branch, parameter)`
  form) as the metric endpoint;
- do **not** substitute a nearby point of any kind: proximity is not semantic
  position, and doing so would produce an answer with no provenance.

No implementation date is promised for this. It is recorded as an open item
requiring characterization and a semantic contract before any change.

---

<!-- geocedg-guide-section: transformations -->
## 10. Transformations

### 10.1 Supported forms

The ordinary two-dimensional similarity transformations accept a semantic curve
as their first argument:

```text
Translate( L, v )
Rotate( L, angle )
Rotate( L, angle, center )
Reflect( L, point )      / Mirror( L, point )
Reflect( L, line )       / Mirror( L, line )
Dilate( L, factor )
Dilate( L, factor, center )
```

The same tools are available under **Construction → Similarity
transformations**.

Circle or conic inversion, shear, non-uniform stretch, arbitrary affine or
projective maps and 3D transformations are **not** supported on semantic curves.

### 10.2 The result is a new semantic entity

Transforming a semantic curve produces **another semantic curve**, with its own
durable identity and its own place in the dependency graph. It is not a
transformed picture of the original and not a copy of its render samples.

The transformation acts on the geometry, not on the semantic parameter: for a
source `L` and a transformation `T`, the result evaluates as `T(L(u))` at the
same parameter `u`. The source identity is never reused, not even for an
identity map.

### 10.3 Dynamic behaviour

The result depends on the source curve and on every ordinary transformation
input. Edit the vector, the angle, the centre or the factor and the transformed
curve follows.

```text
O=(0,0)
k=1
T=Dilate(S,k,O)
```

The length of `T` scales by `abs(k)`. Translation, rotation and reflection
preserve lengths.

At `k=0` the image collapses, but it retains its valid domain: it is a collapsed
image, not an unparameterized point, and a fresh click does not invent a
preimage on it. Restoring `k` recovers the existing semantic points through the
kernel.

Intersections of transformed curves derive their own selectors and tokens; they
are not inherited from the source intersection.

### 10.4 Reflection about an axis

The built-in coordinate axes have no ordinary persistent object identity, so
they cannot be selected as the mirror of a semantic reflection. Construct the
line explicitly — for example `a: y = 0` — and select that line.

---

<!-- geocedg-guide-section: dxf-export -->
## 11. DXF export

### 11.1 What DXF export is, and is not

**File → Import and export → Export 2D geometry as DXF (experimental)…**
writes an ASCII DXF (AC1015) drawing from already-resolved 2D model geometry.

It is an interoperability adapter. It is not a screenshot, it does not clip to
the window, it does not change the construction, and it is never geometric
authority. Nothing in the exported file feeds back into the model.

Extended DXF export (the G9X1 capability) is enabled by default in GeoCeDG,
independently of semantic curves: the two defaults are separate decisions and
neither implies the other. The historical `--enableExtendedDxf=false` argument
is retained as a disabling override.

### 11.2 Exact native mappings

For these families the native DXF entity represents the resolved
model-coordinate object over its declared domain, without intentional
discretization:

| Source | DXF entity |
|---|---|
| point | `POINT` |
| segment | `LINE` |
| ray | `RAY` |
| line | `XLINE` |
| circle | `CIRCLE` |
| circular arc | `ARC` |
| ellipse / elliptic arc | `ELLIPSE` |
| polygon / polyline | `LWPOLYLINE` |

"Exact" here means that no deliberate approximation was introduced. It is not a
claim of symbolic arithmetic.

### 11.3 Approximate export-only geometry

Curves that have no approved exact DXF entity are exported as bounded
`LWPOLYLINE` geometry produced by deterministic dyadic refinement in **model
coordinates**:

| Source | Representation |
|---|---|
| parabola / hyperbola | bounded `LWPOLYLINE` over an explicit finite interval |
| bounded function / parametric curve | bounded `LWPOLYLINE`, explicit finite domain required |
| Locus V2 | one `LWPOLYLINE` per branch and valid component |
| Spline V2 | bounded `LWPOLYLINE` |

The approximation is derived from the **semantic geometry**. It is not derived
from the render cache, the viewport, the zoom level or the screen resolution.
The same construction and the same request produce the same bytes.

Invalid-domain gaps are never bridged, coincident constructive components stay
distinct, and endpoint proximity alone never establishes periodic closure.

### 11.4 Spline V2 and the `SPLINE` entity

```text
DXF SPLINE exact entity      = NOT IMPLEMENTED
SplineV2 DXF representation  = APPROXIMATE LWPOLYLINE under current G9X1
```

There is no exact `SPLINE` entity in the approved export baseline, and none is
produced. A Spline V2 is exported as approximate polyline geometry with its
evidence recorded. Any future exact rational `SPLINE` would require a separate
authorization with mathematical exactness and independent reader-conformance
evidence.

### 11.5 Fidelity outcomes

Fidelity is assigned per source component, not once per file:

| Outcome | Meaning |
|---|---|
| `EXACT` | native entity, no intentional discretization |
| `APPROXIMATE` | intentional discretization with method, tolerance, guarantee and work evidence |
| `UNSUPPORTED` | valid source, no approved mapping for its family or domain |
| `INVALID` | undefined, non-finite, stale, ill-domained, over budget, or unable to meet the requested evidence |

Reason codes accompany these, including `MISSING_DOMAIN`, `NON_FINITE`,
`DISCONTINUITY_UNRESOLVED`, `TOLERANCE_NOT_ESTABLISHED`, `WORK_LIMIT`,
`STALE_SOURCE_REVISION` and `UNSUPPORTED_FAMILY`. Omission is never silent.

### 11.6 Guarantee: `ESTIMATED_ERROR`

Approximation evidence carries a guarantee level. The level used by the current
export is:

```text
ESTIMATED_ERROR
```

This is deterministic sample and derivative evidence, not a proof. It is
**not a certified global error bound**, and this guide does not describe it as
one. A `CERTIFIED_ERROR_BOUND` would require an interval, curvature or enclosure
proof contract that the current baseline does not provide.

### 11.7 Preflight and the export dialog

Before writing anything, the export runs a **preflight** and shows you the
request and its consequences. The dialog currently presents its labels in
English regardless of the product language.

| Field | Meaning |
|---|---|
| Objects | `Complete 2D geometric construction` or `Current selection` |
| Model-coordinate tolerance | requested tolerance, default `0.001` |
| Approximation | whether approved typed curve approximation is allowed |
| Closed domains | explicit finite domains, as `start:end` or `source@branch:start:end`, separated by `;` |
| Allowed evidence | `ESTIMATED_ERROR` |
| Maximum evaluations / dyadic depth / vertices per component / total vertices | deterministic work limits |
| Coordinates / units | `Cartesian 2D world / UNITLESS` |
| Partial output | `Disabled (strict complete request)` |
| Sidecar | request a manifest even for an all-exact export |

The preflight report then lists the exact, approximate, unsupported, invalid and
omitted component counts before you choose a destination.

For a complete-construction request the input is the typed geometric population.
Lists, numeric parameters, rich intersection results and Text have no approved
DXF mapping and are reported as outside that population before preflight. That
is not partial output. An explicit current selection is never filtered by this
rule.

### 11.8 Strict default

Partial component output is **disabled** by default: a request that cannot be
satisfied completely is rejected rather than silently truncated. Hidden objects
are included and visibly reported.

### 11.9 Sidecar manifest

A deterministic UTF-8 `<drawing>.dxf.manifest.json` is written whenever the
export contains approximate geometry, omitted or partial geometry, an
unsupported requested component, or a work-limit termination. A wholly exact
export may omit it, and you may request it explicitly.

The manifest records the schema and build provenance, the DXF SHA-256, the
complete request policy and work limits, and per component: the source
identifier and its scope, the branch key, the component, the semantic interval,
the DXF handle and entity type, the fidelity, the approximation method, the
requested tolerance, the achieved estimate, the guarantee, the evaluation count,
the vertex count and the depth, plus every structured warning and omission.

DXF and manifest are written to same-directory temporary files, validated, and
then promoted together under a defined rollback policy.

### 11.10 Viewport independence

Coordinates are unitless Cartesian model coordinates. Neither the export nor its
fidelity depends on zoom, pan, window size, DPI or the current view. The same
construction revision and the same request always produce the same output.

### 11.11 Current export boundary

There is no DXF import, no viewport export, no physical-unit contract, no text
export, no export of the legacy sampled `Locus`, no implicit-curve contouring,
no exact `SPLINE` and no 3D export. Line weight, line style, fill, opacity,
point size and labels are not transported; layer, RGB colour and current
visibility are.

---

<!-- geocedg-guide-section: presentation-and-visualization -->
## 12. Presentation and visualization

Everything in this section is presentation. None of it changes geometry,
identity, metrics, document content or export output.

### 12.1 Themes and canvas

The three presentation themes are described in section 2.5. A theme sets the
frame, menu/toolbar surface, panel and separator colours and the Graphics canvas
background. A background you choose explicitly for a view keeps its meaning.

### 12.2 Fonts and icon size

The six independent presentation sizes are described in section 2.6: general UI
font, menu font, toolbar icon size, Algebra font, Construction Protocol font and
Graphics font. The construction **Text font size** is separate and belongs to
the document.

### 12.3 Object styling

Colour, line style, point style, visibility and labelling of individual objects
are ordinary object properties, set through **Properties** or **View →
Visibility and style**. **Copy visual style** transfers them between objects.

### 12.4 Zoom and navigation

**View → Navigation** provides pan, the inherited zoom in and out, the standard
view, show all objects, and the GeoCeDG actions described below. The same
actions are on the toolbar in the Navigation flyout.

**Zoom to rectangle** waits for an ordinary press-drag-release in the canvas and
zooms to the rectangle you draw. It preserves the current X/Y scale relation and
`Escape` cancels the gesture.

**View → Configure navigation zoom…** sets one zoom factor (default 10) and
independent, initially unassigned shortcuts for **Zoom Factor In** and **Zoom
Factor Out**. These multiply or divide both view scales around the current
cursor, or around the true view centre when there is no current cursor. While
you edit a shortcut, the dialog immediately reports *Available*, *Unassigned
(valid)*, *Invalid*, or the action it collides with; **Apply** stays disabled
until the whole draft is valid, and **Cancel** keeps the previous configuration.

From the keyboard, click an empty area of the Graphics view to give it focus and
use `Ctrl`+`+` and `Ctrl`+`-`. Do not type those into Algebra Input.

No zoom or navigation operation changes metrics, coordinates, identity or
provenance.

---

<!-- geocedg-guide-section: user-tools-and-automation -->
## 13. User tools and automation

### 13.1 Persistent user tools

**Automation → User tools → Manage user tools…** manages a library of `.ggt`
tool packages that belong to the **GeoCeDG profile**, not to the open document.

To install one:

1. Open the manager.
2. Choose **Install .ggt…**, select the package, and review the name and any
   rejection.
3. Choose the tool from the menu to activate it. Activating it registers the
   definition in the document; then select its inputs to produce the results.
4. Optionally pin it to the toolbar, order it, assign it a toolbar group and
   give it a PNG icon. Without an icon the toolbar shows a compact initial and
   keeps the full name in the tooltip and accessibility information.

The installation survives closing the document, opening another and restarting.
Removing the installation does not delete results that were already built.

### 13.2 Tool, document and library

These are three different scopes, and keeping them distinct avoids most
confusion:

- the **library** is an application preference of the GeoCeDG profile;
- a **document tool** (an embedded macro) belongs to the `.cedg` and rebuilds
  its results from the document;
- **invoking** an installed tool copies the needed definition into the document,
  so a construction remains portable after you remove the library entry.

When a reopened document contains an embedded macro equivalent to an installed
one, GeoCeDG compares the complete set of definitions and their digests. The
installed entry remains the only visible, enabled choice, while the embedded
macro continues to belong to the document and to rebuild its results. It is
never deleted or replaced. If an installed tool offers the same command name
with a *different* definition, its installed entry is disabled in that document
only. No association is ever decided by name, load order or toolbar position.

With no package installed, **Document tools (local only)…** still lets you
manage the portable definition.

### 13.3 Limits

A user tool may not override a native command such as `Point`, `Length`,
`LocusV2`, `SplineV2` or `Intersect`. Tools that need unsupported scripts,
semantic objects or non-planar operations are rejected with an explanation;
they do not bypass product policy. Oversized packages and invalid or partial
archives fail closed.

With several windows open, the manager re-checks the library before changing it
and asks you to retry if another window is updating it.

### 13.4 Scripting

**Automation → Object properties and scripting…** opens the Scripting tab of the
existing Properties dialog. GeoGebraScript works through commands, not through
synthetic mouse gestures.

---

<!-- geocedg-guide-section: classic-compatibility-and-diagnostics -->
## 14. Classic compatibility and diagnostic tools

### 14.1 What the Classic diagnostic session is for

**File → Open Classic diagnostic session** launches an inherited Classic
application in a **separate process with isolated preferences**. It exists so
that you can compare behaviour against the inherited baseline, open an inherited
document with inherited tools, and use inherited facilities that the GeoCeDG
product profile deliberately does not expose — most usefully **Create tool** for
authoring a `.ggt` from scratch.

### 14.2 What it preserves and what it does not

The Classic session keeps its own tools, toolbar and upstream configuration,
including the upstream language selection. Continuity remains separately
configurable there, whereas the GeoCeDG product profile locks it **off** so that
semantic selections are deterministic rather than a search for the position
nearest the previous one.

Classic **cannot create** Locus V2 or Spline V2 objects. The default promotion
of the semantic surface applies to the GeoCeDG product profile only and cannot
leak into Classic, whatever launch arguments are used.

### 14.3 Diagnostic legacy resources

**Automation → Legacy laboratory (separate session)** opens a file you choose
explicitly in a separate diagnostic Classic process. That dialog does **not**
verify the file against the canonical legacy catalog and does not promote it as
scientific authority. For registered, hash-verified canonical resources use the
repository script `tools/legacy/open-laboratory.ps1`.

No legacy toolbar is installed in the Construction workspace.

These are diagnostic routes for a technical user, not normal product features.
Legacy macros may carry undocumented validity ranges, degeneracies, dynamic
limitations and sampled numerical approximations.

---

<!-- geocedg-guide-section: known-limitations -->
## 15. Known limitations

These are the limitations that affect what you can do in the application today.

**Semantic curves and metrics**

- Locus V2 and Spline V2 keep the maturity `experimental` although they are
  enabled by default. "Default on" is exposure, not stability: their public
  contract may still be refined.
- Locus V2 is not a generic `Path`. It supports the documented generators,
  explicit domains and targets.
- Semantic curves are GeoCeDG-only. They are saved in the native document, and
  an external upstream application is outside the compatibility guarantee.
- `Length(S,P,Q)` may be undefined when an endpoint is a point materialized from
  a Spline V2 × Spline V2 intersection. See section 9.4.
- A tangent, ambiguous, stale or insufficiently certified intersection root
  stays rich-only and cannot be materialized.

**Export**

- Exact DXF `SPLINE` is not implemented; Spline V2 exports as approximate
  `LWPOLYLINE`.
- Approximation evidence is `ESTIMATED_ERROR`, not a certified global error
  bound.
- There is no DXF import, viewport export, physical-unit contract, text export,
  legacy `Locus` export, implicit-curve contouring or 3D export.

**Not available**

- Spatial semantics — three-dimensional object identity, projection frames and
  canonical projection certificates — is not available in the product. The
  **CeDG dihedral procedures** workspace is visible but not authorized.
- No CeDG DSL, workbench, study service or optimization layer exists.

**Platform and product state**

- Windows is the only validated workstation platform.
- Branding is textual and provisional: there is no final GeoCeDG logo, icon set
  or installer artwork, and the runtime still uses inherited strings and
  interface assets.
- `.cedg` and compatible `.ggb` archives keep the inherited `classic`
  application code internally; there is no new archive format.
- The DXF export dialog is presented in English regardless of the product
  language.

---

<!-- geocedg-guide-section: command-and-workflow-reference -->
## 16. Command and workflow reference

### 16.1 Reference table

| Task | GUI route | Command | Result type | Important notes |
|---|---|---|---|---|
| Create a semantic locus | Construction → Semantic curves → Locus V2 | `LocusV2(G,u,dom)` | semantic curve | the domain is explicit; it does not come from a slider range |
| Create a semantic spline | Construction → Semantic curves → Spline V2 | `SplineV2({A,B,C,D},3)` | semantic curve | constructor points are not semantic points |
| Point on a semantic curve, interactively | Point tool, click the curve stroke | — | semantic point | unique preimage, or choose explicitly; never by proximity |
| Point on a semantic curve, by parameter | Construction → Semantic curves → Point on semantic curve | `Point(S,"spline-v2/main",0.25)` | semantic point | branch key and canonical parameter; the parameter may be a named number |
| Point on a Locus V2, by parameter | same | `Point(L,"generator.main",1)` | semantic point | default generator branch key |
| Total length | Construction → Metrics and validation | `Length(S)` | number | scalar adapter over the rich metric result |
| Partial length | Construction → Metrics and validation | `Length(S,P,Q)` | number | both endpoints need admissible semantic provenance |
| Rich metric evidence | Construction → Metrics and validation | `LocusLength(S)` / `LocusLength(S,P,Q)` | rich result | status, coverage, error estimate, diagnostics |
| Intersect semantic curve and object | Construction → Relations and intersections → Intersect | `Intersect(S,c)` | rich intersection result | not a list of points |
| Intersect two semantic curves | same | `Intersect(S,T)` | rich intersection result | several roots may each be admissible |
| Materialize solutions | Inspect rich result → Create one / Create selected / Create all eligible | `Intersect(R,"<token>")` | point | recompute never creates points; do not transcribe tokens |
| Translate | Construction → Similarity transformations | `Translate(S,v)` | new semantic curve | lengths preserved |
| Rotate | same | `Rotate(S,angle,center)` | new semantic curve | lengths preserved |
| Reflect | same | `Reflect(S,line)` / `Mirror(S,line)` | new semantic curve | construct the mirror line explicitly; axes are not selectable |
| Dilate | same | `Dilate(S,k,O)` | new semantic curve | length scales by `abs(k)`; `k=0` collapses but keeps its domain |
| Export DXF | File → Import and export → Export 2D geometry as DXF (experimental)… | — | DXF file (+ manifest) | preflight first; strict complete request by default |
| Open a document | File → Open… | — | — | `.cedg` native, `.ggb` compatibility input |
| Save a document | File → Save / Save as… | — | — | save native work as `.cedg` |
| Inspect a definition | context menu → Inspect definition… | — | — | read-only; does not change Algebra display mode |
| Inspect semantic structure | Construction → Semantic curves → Inspect semantic curve definition… | — | — | branch, component, address, status, parameter |

### 16.2 Command syntax summary

```text
LocusV2( <Dependent Point>, <Constrained Point> )
LocusV2( <Dependent Point>, <Scalar State>, <Domain Descriptor> )
LocusV2( <Dependent Point>, <Scalar State>, <True Driver>, <Domain Descriptor> )

SplineV2( <List of Points> )
SplineV2( <List of Points>, <Degree> )
SplineV2( <List of Points>, <Degree>, <Weight Function> )
SplineV2( <Point>, <Point>, <Point>, ... )

Point( <Locus V2>, <Branch Key>, <Canonical Parameter> )

Length( <Locus V2> )
Length( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )

LocusLength( <Locus V2> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point> )
LocusLength( <Locus V2>, <Start Semantic Point>, <End Semantic Point>,
             <Direction>, <Boundary Policy>, <Same-position Policy> )

Intersect( <Locus V2>, <Supported Object> )
Intersect( <Locus V2>, <Locus V2> )
Intersect( <Rich Intersection Result>, <Solution Token> )
```

A Spline V2 is a semantic curve of the Locus V2 family, so the forms written
above for `<Locus V2>` accept it.

### 16.3 Command names and language

Command identifiers are not translated in this guide, and the English forms
shown above work in both product languages. With Spanish selected, the input
help also offers localized names — `LugarGeométricoV2` for `LocusV2`,
`LongitudLugarGeométrico` for `LocusLength`, `Longitud` for `Length`,
`Interseca` for `Intersect`, `Punto` for `Point`, `Refleja` for
`Reflect`/`Mirror` — while `SplineV2` keeps its
name. Branch keys such as `"generator.main"` and `"spline-v2/main"`, exact
tokens, file extensions and code literals are identifiers and are never
translated.

### 16.4 Keyboard

| Key | Action |
|---|---|
| `Enter` | commit the Algebra Input transaction |
| `Escape` | cancel input, exit a tool, cancel a rectangle gesture |
| `Ctrl`+`Z` / `Ctrl`+`Y` | undo / redo |
| `Ctrl`+`+` / `Ctrl`+`-` | zoom in / out, with the Graphics view focused |
| `F2` | edit the selected object |
| `F1` | tool help |
| `Tab` | move through controls |

---

*GeoCeDG 1.0.0 — Manuel Prado-Velasco, Universidad de Sevilla. Required upstream
credits and licenses are retained in `LICENSE`, `NOTICE.md` and
`THIRD_PARTY.md`.*

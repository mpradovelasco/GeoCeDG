# GeoCeDG G9P Reference Workflows

## 1. General Construction Workflow

### Purpose of the model

The model represents a hemispherical tank, defined by its orthographic projections, to which three cylindrical legs are to be attached. The axes of the legs pass through the centre of the sphere and through the points \(O_1\), \(O_2\), and \(O_3\), which are themselves defined by their projections.

As described in the CeDG book, my usual convention is to identify projections through labels, while allowing GeoGebra to control the internal names of the objects. The object `text1` contains the problem statement because the model was originally created as an examination exercise. The initial data given in the exercise consisted only of the sphere and the horizontal projections of the three points \(O_i\).

The main parameters are:

- \(R_p\): radius of the cylindrical legs;
- \(R_e\): radius of the sphere;
- \(R_c\): radius of the circle, in the horizontal projection, on which the points where the leg axes intersect the spherical surface are located.

### Typical operations

- Definition of parameters and control through sliders.
- Construction of parallel and perpendicular lines.
- Intersections between different curves.
- Construction of circles.
- Construction of ellipses from principal axes and from conjugate axes.
- Construction of tangents to conics and circles.
- Intersection of tangents with conics and circles.
- Construction of segments.
- Visual emphasis using colour, line type, and line thickness.

### Usual order of work

1. Obtain the projections \(o'_1\), \(o'_2\), and \(o'_3\). This completes the definition of the points where the axes of the legs intersect the sphere.
2. Perform a dihedral rotation of the axis of the leg passing through \(O_1\), about an axis perpendicular to the horizontal projection plane and passing through the centre of the sphere.
3. In the rotated position, the leg-sphere intersection circle lies in a plane perpendicular to the vertical projection plane. It can therefore be drawn directly in the vertical projection. Its horizontal projection is then constructed from its principal axes.
4. Return the leg axis to its original position by taking the ellipse in the horizontal projection back to the original position of \(o_1\). Obtain the corresponding vertical projection from its conjugate axes, using first the command/tool that derives the principal axes and then the ellipse-from-principal-axes command/tool.
5. Obtain the horizontal-projection ellipses of the other two legs by rotating the first one by \(120^\circ\). In the vertical projection, obtain the ellipse centred at \(o'_2\) by reflection, and obtain the ellipse centred at \(o'_3\) by taking advantage of the fact that it is related by a dihedral rotation using the same rotation axis as in step 2.
6. Complete the contour generatrices of the legs with segments, extending them to the horizontal support plane.
7. Obtain the intersection ellipse between the horizontal support plane and the leg whose axis passes through \(O_2\). The intersection of the leg axis with the horizontal plane gives the centre of the ellipse. The intersections of the relevant contour generatrices with the horizontal plane define its principal axes (`e_4`).
8. Complete the support geometry of the other two legs by applying \(120^\circ\) rotations to the ellipse obtained for the leg whose axis passes through \(O_2\).

### Views and panels normally used

**Algebra View.** I use it to monitor the objects as they are created and to help with hiding, locating, and redefining them.

**Construction Protocol.** I use this extensively. To preserve the original construction order—and therefore the intended reproducibility of the model—I do not simply modify earlier constructions from the final state. Instead, I move back through the Construction Protocol to the step where an object was created and modify it there. This allows GeoGebra to preserve the intended construction order.

**Properties window.** I usually keep it floating. One of its most frequent uses is to define labels, although I also use it to control other object properties.

**Graphics View 2 / Canvas 2.** It is not used in this particular model, but I use it in other models. One reason is to create functions that describe some aspect of system behaviour. This is clearly illustrated, for example, by the articulated-door model presented in the CeDG book.

**Main 2D Graphics View.** This is the primary construction workspace.

**`ICeDGFunctions` list.** I normally keep this list hidden because GeoGebra does not behave exactly the same way when it is visible. The list contains helper functions that I encapsulated in scripts and, in some cases, JavaScript to overcome limitations of GeoGebra, including operations for releasing/free-copying objects and points. This list should not exist in the final GeoCeDG workflow. If any of these capabilities still need to be available as tools, they should be exposed through the proper GeoCeDG user-facing/public surface.

**Input Bar.** It is important to keep the input bar readily accessible, normally at the bottom of the application.

**Command help.** In the application settings I normally enable contextual help for the currently active command. GeoGebra displays it in the upper area, to the right of the toolbar.

### Toolbar groups that are especially useful

In manual CeDG work, most of the standard tools are used frequently. The following are examples of tools that I normally do **not** need directly in the toolbar:

- Complex Number.
- Attach / Detach Point.
- Polyline.
- Segment with Given Length.
- Polar or Diameter Line.
- Rigid Polygon.
- Vector Polygon.
- Semicircle.
- Circumcircular Arc.
- Circumcircular Sector.
- Slope.
- Reflect Object in Circle.
- Reflect Object in Point.
- Image.
- Pen.
- Freehand Shape.
- Function Inspector.
- Show / Hide Object.
- Show / Hide Label.
- Copy Visual Style.
- Delete.

From the `SymmSymbol` group onward, the tools are custom tools created through macros and are all of interest as workflow evidence. In standard GeoGebra such tools are not permanent product capabilities; this is precisely one of the aspects that differs in GeoCeDG following the G2-G3 design and implementation.

### Tools that should remain directly accessible

Access through well-organized toolbar groups is appropriate. There is no requirement for every useful tool to have a permanently visible individual button.

### Current workarounds or limitations

Some current GeoGebra versions may display custom-tool icons at an excessively large size. This appears to be a GUI bug. Changing **Options → Font Size** forces the toolbar to reset and corrects the icon size.

---

## 2. Locus and Development Workflows

Three reference models are described:

1. `geocedg-reference-locus-cylindrical-graft-development.ggb`  
   A self-compensating expansion-joint/graft connection to an oblique cylindrical duct. This is a simplified configuration; in normal engineering use the expansion joint would typically be applied to an elbow.

2. `geocedg-reference-locus-truncated-cone-cylinder-connections.ggb`  
   A truncated-conical transition duct connecting two horizontal cylinders of different diameters.

3. `geocedg-reference-locus-focal-sphere-illumination.ggb`  
   A rotating sphere carrying a light sensor and illuminated by a conical light beam.

### Purpose of the models

1. Compute the connection/intersection between the expansion joint (graft) and the oblique cylinder. The spatial intersection is defined through its projections, represented by locus curves according to the CeDG methodology. The flat development of the graft is also obtained.
2. Compute the two connection/intersection curves between the truncated-conical duct and the two cylinders. Their projections are defined by locus curves.
3. Compute the region of the sphere illuminated by the conical light beam and then determine the fraction of each complete rotation of the sphere during which the sensor remains illuminated.

In all three models I try to preserve the intended sequence in the Construction Protocol, which makes their analysis easier. The descriptive-geometry procedures are similar in character to those used in the **General Construction Workflow** model.

### Locus-based operations and intersections

The following is a concise description of how the three models are solved. Their procedures combine 2D intersections with the construction of locus curves.

A major limitation of legacy GeoGebra Locus was that the resulting locus curve could not be intersected natively with adjacent geometric objects. Whenever such an intersection was required, I had to use alternative and cumbersome constructions to compute the corresponding points. This limitation should no longer govern the GeoCeDG workflow because native Locus V2 intersection capability has now been developed.

#### Model 1 — Cylindrical graft / expansion-joint development

The solution starts by obtaining the points \(P_1\) and \(P_2\) through their projections, using the frontal plane passing through the unlabeled point represented by object `L_1`. This plane generates intersection generatrices on both cylinders. From these, I retain the generatrices corresponding to the graft entrance.

The points \(P_1\) and \(P_2\) generate the loci that define the projections of the spatial intersection curve, following the CeDG surface-intersection method.

The flat pattern of the graft is obtained from the transformed positions of \(P_1\) and \(P_2\). These transformed points are constructed by transporting the corresponding generatrices to the flat domain, preserving lengths and taking the generatrix through \(A\) as the reference generatrix. The flat transform of the intersection curve on the developed graft is itself represented by a locus.

With legacy GeoGebra Locus, none of these locus curves could be intersected directly with the generatrices to which they geometrically belong. This forced indirect constructions. Native GeoCeDG Locus V2 intersection support is expected to remove this limitation.

#### Model 2 — Truncated-conical transition between two cylinders

The solution begins by folding down a right section of the connecting cone. This section is defined at the point \(o'-o\) on the cone axis. I intentionally left this section position free because any suitable right section can be used, and its position can be chosen to make the graphical construction convenient on the canvas.

The folded-down circle allows a free point \(p\) to be defined. This point corresponds to the folded-down position of \(p'-p\). The cone generatrix passing through this point intersects a fixed profile plane at \(p'_r-p_r\). Transporting this point to the profile view provides the profile projection of the cone generatrix.

To obtain the point on this cone generatrix that also belongs to one of the cylinder intersections—for example, the smaller-radius cylinder—we use a plane from the pencil of planes that passes through \(v'-v\) and through the line parallel to the cylinder generatrices through \(v'-v\). This is a pencil whose planes cut both the cone and cylinder along straight generatrices.

The selected plane intersects the profile plane along a line whose profile projection passes through \(v''\) and \(p''_r\). This determines the cutting generatrices of the cylinder, shown in yellow in the model. These generatrices intersect the cone generatrix at \(c'_1\) for the smaller-radius cylinder and at \(c'_2\) for the larger-radius cylinder.

The corresponding horizontal projections are obtained by projecting vertically to the horizontal projection of the cone generatrix.

The intersection projections are then generated as loci. For example, `Locus(L_2, B_2)` is used for the vertical projection of the intersection with the smaller-radius cylinder, with analogous locus constructions for the remaining projections.

#### Model 3 — Focal illumination of a rotating sphere

A vertical change of projection plane is first used to place the axis of the cone that is tangent to the sphere from \(V\) in a frontal position. This allows the light separatrix circle to be determined. In this auxiliary vertical projection the separatrix circle is projecting and therefore appears as the segment \(1'_2-2'_2\).

A second change of projection plane is then used to place the axis of the actual light cone issuing from \(V\) perpendicular to the new horizontal projection plane. The ground-line reference for this change is `CP_1`.

In this new projection system—formed by the original vertical projection and the new horizontal projection—the intersection between the sphere and the actual light cone is solved. The cone aperture is parameterized by the half-angle `halfCono`.

Horizontal auxiliary planes are used. Each auxiliary plane cuts both the sphere and the cone in circles. The cutting plane is controlled by the free point represented by object `K`.

The intersections of these circles give the points \(p_1,q_1\) in the new horizontal projection and \(p',q'\) in the vertical projection. These moving points generate the projections of the sphere-cone intersection curve through locus constructions, for example `Locus(p_1, K)`.

The resulting sphere-cone intersection is a bite-type intersection. The portion lying inside the region of the sphere that can be illuminated—bounded by the light separatrix—defines the actual light footprint.

The sensor is represented by \(I'-I\) on the sphere. When the sphere rotates horizontally in the original projection system, the model determines the fraction of a full revolution during which the sensor is illuminated. Object `text7` presents this fraction as

\[
\frac{\gamma}{360^\circ}.
\]

### Length measurements

Because of the limitations of legacy Locus and the associated workarounds, these reference models do not contain length measurements along their locus curves.

Native Locus V2 should make additional meaningful measurements possible. For example, the perimeter length of an intersection curve can be studied through its developed representation; in Model 1, this corresponds to measuring the relevant Locus V2 curve in the calculated flat development of the graft. More generally, the G9P analysis should distinguish direct semantic Locus V2 length from length inferred through a valid isometric development.

No model of the exact development of a general oblique cone is included here. With legacy GeoGebra Locus this problem could not be solved exactly; only discrete approximations were practical, as discussed in the CeDG book and later work on Locus-based flattening.

### Development / flattening operations

The principal flattening workflow is illustrated by Model 1 and described above.

The important point for G9P is that a developed curve is not merely a drawing artifact. It is a dependent geometric result produced by an explicit descriptive-geometry transformation with length-preservation constraints and construction provenance.

### Downstream constructions using locus-defined curves

The models above illustrate why locus-defined curves must behave as genuine geometric entities.

A locus may represent:

- a projection of a spatial intersection curve;
- a transformed curve in a flat development;
- an intermediate result required by later constructions.

Downstream CeDG construction should therefore be able to use Locus V2 through native incidence/intersection and metric operations without reverting to sampled legacy workarounds.

### Legacy tools expected to be replaced by native Locus V2 capability

The legacy helper tools/functions:

- `postLocus`;
- `listLength`;
- `listLength12` (or the corresponding `_2` helper used in legacy material);

should no longer be needed for the native GeoCeDG workflow once the corresponding Locus V2 capabilities are exposed through the proper user-facing surface.

Legacy GeoGebra Locus may remain temporarily for compatibility with old `.ggb` models, but new GeoCeDG constructions should not need to rely on it when a supported Locus V2 operation exists.

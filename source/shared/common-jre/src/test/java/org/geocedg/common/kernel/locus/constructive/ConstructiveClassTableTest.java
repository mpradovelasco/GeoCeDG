/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.locus.constructive.ConstructiveClassTable.BoundaryCategory;
import org.geocedg.common.kernel.locus.constructive.ConstructiveClassTable.Classification;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;

/**
 * The class table {@code constructive-class/v1} ({@code locus-v2-constructive-authority.md}
 * section 3.2): membership by algorithm and never by value, the structural positions of the
 * inputs, the declared boundary of categories D2 to D5, and no change to any inherited output
 * (validation matrix rows V-AUT-NUM-1, V-AUT-NUM-5 and V-AUT-NUM-6 at the level of the
 * classification).
 */
final class ConstructiveClassTableTest extends BaseUnitTest {

	@Test
	void everyAlgorithmOfTheClassHasItsOperationAndStructuralInputs() {
		final GeoElement a = add("A=(1,2)");
		final GeoElement b = add("B=(4,6)");
		final GeoElement number = add("a=2");
		final GeoElement angle = add("w=30°");
		final GeoElement line = add("f=Line(A,B)");
		final GeoElement segment = add("s=Segment(A,B)");
		final GeoElement perpendicular = add("u=OrthogonalLine(B,f)");
		final GeoElement circle = add("c=Circle(A,B)");
		final GeoElement meet = add("M=Intersect(f,u)");

		assertOperation(ConstructiveOperation.FREE_POINT, a);
		assertOperation(ConstructiveOperation.FREE_NUMBER, number);
		assertOperation(ConstructiveOperation.FREE_NUMBER, angle);
		assertOperation(ConstructiveOperation.AXIS, getConstruction().getXAxis());
		assertOperation(ConstructiveOperation.JOIN, line, a, b);
		assertOperation(ConstructiveOperation.SEGMENT, segment, a, b);
		assertOperation(ConstructiveOperation.RAY, add("t=Ray(A,B)"), a, b);
		assertOperation(ConstructiveOperation.PARALLEL, add("q=Line(A,xAxis)"), a,
				getConstruction().getXAxis());
		assertOperation(ConstructiveOperation.PERPENDICULAR, perpendicular, b, line);
		AlgoIntersectLines lines = (AlgoIntersectLines) meet.getParentAlgorithm();
		assertOperation(ConstructiveOperation.MEET, meet, lines.getg(), lines.geth());
		assertEquals(Set.of(line, perpendicular),
				Set.copyOf(ConstructiveClassTable.classify(meet).getInputs()));
		assertOperation(ConstructiveOperation.MIDPOINT, add("N=Midpoint(A,B)"), a, b);
		assertOperation(ConstructiveOperation.CIRCLE_THROUGH, circle, a, b);
		assertOperation(ConstructiveOperation.CIRCLE_RADIUS, add("d=Circle(A,a)"), a, number);
		assertOperation(ConstructiveOperation.CIRCLE_RADIUS, add("e=Circle(A,s)"), a, segment);
		assertOperation(ConstructiveOperation.PATH_POINT, add("P=Point(f)"), line);
		assertOperation(ConstructiveOperation.PATH_POINT, add("Q=Point(c)"), circle);
		assertOperation(ConstructiveOperation.PATH_POINT, add("R=Point(s)"), segment);
	}

	@Test
	void lineCircleOutputsCarryTheInheritedSelectionState() {
		add("A=(0,0)");
		add("B=(3,0)");
		GeoElement line = add("f=Line((-5,1),(5,1))");
		GeoElement circle = add("c=Circle(A,B)");
		GeoElementND[] outputs = getElements("Intersect(c,f)");
		GeoElement single = add("S=Intersect(c,f,2)");

		for (int index = 0; index < 2; index++) {
			Classification output = ConstructiveClassTable.classify(outputs[index]
					.toGeoElement());
			assertEquals(ConstructiveOperation.LINE_CIRCLE_OUTPUT, output.getOperation());
			assertEquals(List.of(line, circle), output.getInputs(),
					"structural positions: the line, then the circle");
			assertEquals(index, output.getOutputIndex());
			InheritedLineCircleSelection selection = output.getSelection();
			assertNotNull(selection);
			AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) outputs[index]
					.toGeoElement().getParentAlgorithm();
			assertEquals(algorithm.isSelectionFirstIntersectionPending(),
					selection.isCompaction());
			assertTrue(selection.isLabelled(0) && selection.isLabelled(1));
		}
		Classification indexed = ConstructiveClassTable.classify(single);
		assertEquals(ConstructiveOperation.LINE_CIRCLE_OUTPUT, indexed.getOperation());
		assertEquals(1, indexed.getOutputIndex(), "Intersect(c, f, 2) selects output 1");
		assertEquals(List.of(line, circle), indexed.getInputs());
	}

	@Test
	void aCircleIsInTheClassByItsAlgorithmNeverByItsValue() {
		add("A=(0,0)");
		add("B=(2,0)");
		GeoElement constructed = add("c=Circle(A,B)");
		GeoElement equation = add("k: x^2+y^2=4");
		add("f=Line(A,(1,1))");

		assertEquals(ConstructiveOperation.CIRCLE_THROUGH,
				ConstructiveClassTable.classify(constructed).getOperation());
		Classification free = ConstructiveClassTable.classify(equation);
		assertEquals(ConstructiveOperation.OUTSIDE, free.getOperation(),
				"the same circle by value, defined by an equation, is outside the class");
		assertEquals(BoundaryCategory.FREE_OBJECT, free.getCategory());
		Classification onEquation = ConstructiveClassTable.classify(add("P=Point(k)"));
		assertEquals(ConstructiveOperation.BOUNDARY_POINT, onEquation.getOperation());
		assertEquals("point on a path outside the class", onEquation.getReason());
		assertEquals(ConstructiveOperation.PATH_POINT,
				ConstructiveClassTable.classify(add("Q=Point(c)")).getOperation());
	}

	@Test
	void theDeclaredBoundaryKeepsItsInheritedCategory() {
		add("A=(1,2)");
		add("B=(4,6)");
		add("a=2");
		add("w=30°");
		add("c=Circle(A,B)");
		add("f=Line(A,B)");
		add("list1={A,B}");

		assertBoundary(ConstructiveOperation.OUTSIDE, BoundaryCategory.GEOMETRIC_TEST,
				add("equal=AreEqual(A,B)"));
		assertBoundary(ConstructiveOperation.OUTSIDE, BoundaryCategory.GEOMETRIC_TEST,
				add("smaller=a<3"));
		assertBoundary(ConstructiveOperation.BOUNDARY_POINT,
				BoundaryCategory.CONDITIONAL_DEFINITION, add("T=If(a>1,A,B)"));
		assertBoundary(ConstructiveOperation.BOUNDARY_POINT,
				BoundaryCategory.CONDITIONED_SELECTION, add("E=Element(list1,1)"));
		assertBoundary(ConstructiveOperation.BOUNDARY_POINT,
				BoundaryCategory.CONDITIONED_SELECTION, add("X=Intersect(c,f,A)"));
		assertBoundary(ConstructiveOperation.BOUNDARY_POINT,
				BoundaryCategory.NUMERICAL_ALGORITHM, add("X0=Root(x^2-2,1,2)"));
		assertBoundary(ConstructiveOperation.BOUNDARY_POINT, BoundaryCategory.OTHER_ALGORITHM,
				add("Z=A+(1,1)"));
		assertBoundary(ConstructiveOperation.BOUNDARY_NUMBER, BoundaryCategory.OTHER_ALGORITHM,
				add("b=2a"));
		assertBoundary(ConstructiveOperation.BOUNDARY_POINT, BoundaryCategory.OTHER_ALGORITHM,
				add("Y=Point(f,0.3)"));
		Classification rotation = ConstructiveClassTable.classify(add("Ar=Rotate(A,w,B)"));
		assertEquals(ConstructiveOperation.ROTATE_POINT, rotation.getOperation());
		assertEquals(List.of(lookup("A"), lookup("w"), lookup("B")), rotation.getInputs(),
				"a rotation keeps its inputs for the enclosure of its ideal value");
		assertEquals(ConstructiveOperation.Membership.BOUNDARY,
				rotation.getOperation().getMembership());
	}

	@Test
	void continuousModeIntersectionsAreConditionedSelections() {
		add("A=(0,0)");
		add("c=Circle(A,(3,0))");
		add("f=Line((-5,1),(5,1))");
		GeoElement output = add("S=Intersect(c,f,1)");
		boolean continuous = getKernel().isContinuous();
		try {
			getKernel().setContinuous(true);
			assertBoundary(ConstructiveOperation.BOUNDARY_POINT,
					BoundaryCategory.CONDITIONED_SELECTION, output);
		} finally {
			getKernel().setContinuous(continuous);
		}
	}

	@Test
	void classifyingChangesNoInheritedOutput() {
		add("A=(1,2)");
		add("B=(4,6)");
		add("a=2");
		add("c=Circle(A,B)");
		add("f=Line(A,(0,7))");
		getElements("Intersect(c,f)");
		add("T=If(a>1,A,B)");
		add("equal=AreEqual(A,B)");
		add("P=Point(f)");
		String before = getApp().getXML();

		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			ConstructiveClassTable.classify(geo);
		}

		assertEquals(before, getApp().getXML(), "V-AUT-NUM-6: nothing inherited changes");
	}

	private static void assertOperation(ConstructiveOperation expected, GeoElement geo,
			GeoElement... inputs) {
		Classification classification = ConstructiveClassTable.classify(geo);
		assertEquals(expected, classification.getOperation(), geo.getLabelSimple());
		assertEquals(List.of(inputs), classification.getInputs(), geo.getLabelSimple());
		assertSame(null, classification.getCategory());
		assertFalse(classification.hasOutlyingIntersections());
	}

	private static void assertBoundary(ConstructiveOperation expected,
			BoundaryCategory category, GeoElement geo) {
		Classification classification = ConstructiveClassTable.classify(geo);
		assertEquals(expected, classification.getOperation(), geo.getLabelSimple());
		assertEquals(category, classification.getCategory(), geo.getLabelSimple());
		assertTrue(classification.getInputs().isEmpty(), "a boundary object is a leaf");
	}
}

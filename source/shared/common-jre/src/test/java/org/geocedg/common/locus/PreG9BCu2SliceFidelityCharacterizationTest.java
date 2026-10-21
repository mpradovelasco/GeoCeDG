/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.Operation;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T1, D-CU2-20-R focal characterization on the base: how the
 * reconstruction slice of a dependent-point Locus V2 reproduces the inputs it reads, measured
 * by exact bits and by comparing slice evaluations with the live construction.
 *
 * <p>The characterization reads the isolated slice reflectively, and the selection state of
 * the inherited intersection algorithm likewise, until the read-only accessors of step T3
 * (D-CU2-17) exist. The test that pins the angle-literal gap is inverted by the fidelity
 * validation of step T3.</p>
 */
class PreG9BCu2SliceFidelityCharacterizationTest extends BaseUnitTest {
	private static final double[] DRIVER_PARAMETERS = {-3.0, -2.2, -1.1, -0.3, 0.4, 1.3, 2.1,
			2.9};

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/** V-REV-EXACT-1 and V-REV-EXACT-3, synthetic: one ulp of a free number. */
	@Test
	void oneUlpOfAFreeNumberPublishesARevisionWithAnExactSliceCopy() throws Exception {
		add("r=3.6569437271164964");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		GeoNumeric radius = assertInstanceOf(GeoNumeric.class, lookup("r"));
		double base = radius.getValue();
		// A value set by program replaces the typed literal; the round trip below keeps
		// that representation (see representationOnlyChangesPublishARevisionAtTheBase).
		setValue(radius, base);
		String baseFingerprint = fingerprint(locus);
		long revision = locus.getSemanticRevision();
		long builds = locus.getInstrumentation().getDependencySliceBuilds();
		setValue(radius, Math.nextUp(base));
		assertEquals(revision + 1, locus.getSemanticRevision());
		assertEquals(builds + 1, locus.getInstrumentation().getDependencySliceBuilds());
		assertNotEquals(baseFingerprint, fingerprint(locus));
		assertEquals(exact(radius), exact(copy(locus, "r")));
		assertSliceEvaluatesTheLiveConstruction(locus, "Q");
		setValue(radius, base);
		assertEquals(revision + 2, locus.getSemanticRevision());
		assertEquals(baseFingerprint, fingerprint(locus));
		assertEquals(exact(radius), exact(copy(locus, "r")));
	}

	/**
	 * Base over-sensitivity of the v1 fingerprint (V-REV-EXACT-2): it hashes the serialized
	 * representation, so setting a typed number to its own value by program removes its
	 * literal expression, changes the fingerprint and publishes a revision with the same
	 * semantic state. Step T3 inverts this test.
	 */
	@Test
	void representationOnlyChangesPublishARevisionAtTheBase() throws Exception {
		add("r=3.6569437271164964");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		GeoNumeric radius = assertInstanceOf(GeoNumeric.class, lookup("r"));
		String typed = fingerprint(locus);
		long revision = locus.getSemanticRevision();
		setValue(radius, radius.getValue());
		assertNotEquals(typed, fingerprint(locus));
		assertEquals(revision + 1, locus.getSemanticRevision());
	}

	/** V-REV-EXACT-6, synthetic: moving only the driver keeps the revision and the slice. */
	@Test
	void driverOnlyMovesKeepTheRevisionAndTheSlice() throws Exception {
		add("c=Circle((0,0),2)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		String fingerprint = fingerprint(locus);
		long revision = locus.getSemanticRevision();
		long builds = locus.getInstrumentation().getDependencySliceBuilds();
		for (double parameter : DRIVER_PARAMETERS) {
			move(parameter);
		}
		assertEquals(revision, locus.getSemanticRevision());
		assertEquals(builds, locus.getInstrumentation().getDependencySliceBuilds());
		assertEquals(fingerprint, fingerprint(locus));
	}

	/** Inline literal arguments become unlabelled inputs of the slice, reproduced exactly. */
	@Test
	void inlineLiteralArgumentsAreReproducedExactly() throws Exception {
		add("c=Circle((0,0),3.6569437271164964)");
		add("P=Point(c)");
		add("Q=Rotate(P,23.456789012345678deg,(0.1234567890123456,0.30000000000000004))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		List<String> compared = compareUnlabelledInputs(locus);
		assertEquals(4, compared.size(), compared.toString());
		assertSliceEvaluatesTheLiveConstruction(locus, "Q");
	}

	/** A number literal without original text is serialized with Double.toString. */
	@Test
	void programmaticNumberLiteralsAreReproducedExactly() throws Exception {
		add("c=Circle((0,0),2)");
		GeoPoint driver = add("P=Point(c)");
		ExpressionNode offset = new ExpressionNode(getKernel(), driver, Operation.PLUS,
				new MyVecNode(getKernel(), new MyDouble(getKernel(), 0.1 + 0.2),
						new MyDouble(getKernel(), 1.0 / 3)));
		AlgoDependentPoint shifted = new AlgoDependentPoint(getConstruction(), offset, false);
		shifted.getPoint().setLabel("Q");
		XMLStringBuilder xml = new XMLStringBuilder(new StringBuilder());
		shifted.getXML(xml, false);
		assertTrue(xml.toString().contains("(0.30000000000000004, 0.3333333333333333)"),
				xml.toString());
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		assertSliceEvaluatesTheLiveConstruction(locus, "Q");
	}

	/**
	 * Base gap of D-CU2-20-R: an angle literal without original text is serialized in
	 * degrees ({@code Kernel.formatAngle}), so the slice copy reads another binary64 value
	 * and evaluates another configuration without any failure. Step T3 inverts this test.
	 */
	@Test
	void programmaticAngleLiteralsAreNotReproducedExactlyAtTheBase() throws Exception {
		add("c=Circle((0,0),3.6569437271164964)");
		add("P=Point(c)");
		MyDouble literal = new MyDouble(getKernel(), 0.4097);
		literal.setAngle();
		AlgoDependentNumber angle = new AlgoDependentNumber(getConstruction(),
				new ExpressionNode(getKernel(), literal), true);
		angle.getNumber().toGeoElement().setLabel("ang");
		XMLStringBuilder xml = new XMLStringBuilder(new StringBuilder());
		angle.getXML(xml, false);
		assertTrue(xml.toString().contains("23.47408086650983*°"), xml.toString());
		add("Q=Rotate(P,ang,(1,1))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		GeoElement live = lookup("ang");
		GeoElement copy = copy(locus, "ang");
		assertEquals("num:0x1.a3886594af4f1p-2", exact(live));
		assertEquals("num:0x1.a3886594af4f2p-2", exact(copy));
		assertTrue(countEvaluationsDifferingFromTheLiveConstruction(locus, "Q") > 0);
	}

	/** The labels of the outputs of a slice algorithm enter the v1 fingerprint. */
	@Test
	void outputLabelsArePartOfTheSerializedSlice() throws Exception {
		add("c=Circle((0,0),3)");
		add("P=Point(c)");
		add("h=Line(P,(5,1))");
		add("S=Intersect(c,h)");
		add("M=Midpoint(S_1,(0,0))");
		GeoLocusV2 locus = add("l=LocusV2(M,P)");
		String before = fingerprint(locus);
		lookup("S_2").rename("T");
		assertNotEquals(before, fingerprint(locus));
	}

	/**
	 * F-CU2-8: the selection state of a line-conic intersection is not serialized; the copy
	 * of the slice initializes it again by closeness, and reproduces the live state here.
	 */
	@Test
	void lineConicSelectionStateIsReinitializedAndReproducedByTheCopy() throws Exception {
		add("c=Circle((0,0),3)");
		add("P=Point(c)");
		add("h=Line(P,(5,1))");
		add("S=Intersect(c,h)");
		add("M=Midpoint(S_1,(0,0))");
		GeoLocusV2 locus = add("l=LocusV2(M,P)");
		AlgoIntersectLineConic live = assertInstanceOf(AlgoIntersectLineConic.class,
				lookup("S_1").getParentAlgorithm());
		XMLStringBuilder xml = new XMLStringBuilder(new StringBuilder());
		live.getXML(xml, false);
		assertTrue(!xml.toString().contains("permutation"), xml.toString());
		AlgoIntersectLineConic copy = assertInstanceOf(AlgoIntersectLineConic.class,
				copy(locus, lookup("S_1").getLabelSimple()).getParentAlgorithm());
		assertArrayEquals((int[]) field(live, "permutation"),
				(int[]) field(copy, "permutation"));
		assertSliceEvaluatesTheLiveConstruction(locus, "M");
	}

	private void assertSliceEvaluatesTheLiveConstruction(GeoLocusV2 locus, String point)
			throws Exception {
		List<String> differing = evaluationsDifferingFromTheLiveConstruction(locus, point);
		assertTrue(differing.isEmpty(), differing.toString());
	}

	private int countEvaluationsDifferingFromTheLiveConstruction(GeoLocusV2 locus,
			String pointLabel) throws Exception {
		return evaluationsDifferingFromTheLiveConstruction(locus, pointLabel).size();
	}

	private List<String> evaluationsDifferingFromTheLiveConstruction(GeoLocusV2 locus,
			String pointLabel) throws Exception {
		GeoPoint point = assertInstanceOf(GeoPoint.class, lookup(pointLabel));
		List<String> differing = new ArrayList<>();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (double parameter : DRIVER_PARAMETERS) {
				LocusDefinition2D definition = locus.getSemanticDefinition();
				// The evaluator reads the canonical parameter of the periodic domain,
				// which may differ from the semantic one in its last bits.
				move(definition.getProvider().canonicalize(parameter));
				LocusEvaluation2D evaluation = definition.evaluate(
						definition.getBranches().get(0).getBranchKey(), parameter, session);
				if (!evaluation.isValid()
						|| Double.doubleToLongBits(evaluation.getPoint().getX())
								!= Double.doubleToLongBits(point.getInhomX())
						|| Double.doubleToLongBits(evaluation.getPoint().getY())
								!= Double.doubleToLongBits(point.getInhomY())) {
					differing.add("t=" + parameter + " live=(" + point.getInhomX() + ","
							+ point.getInhomY() + ") slice=" + (evaluation.isValid()
									? "(" + evaluation.getPoint().getX() + ","
											+ evaluation.getPoint().getY() + ")"
									: evaluation.getStatus()));
				}
			}
		}
		return differing;
	}

	private List<String> compareUnlabelledInputs(GeoLocusV2 locus) throws Exception {
		List<String> compared = new ArrayList<>();
		AlgoDependentPointLocusV2 parent = parent(locus);
		for (GeoElement live : parent.getReconstructionSliceGeos()) {
			AlgoElement algorithm = live.getParentAlgorithm();
			if (algorithm == null || !live.isLabelSet()) {
				continue;
			}
			AlgoElement copyAlgorithm = copy(locus, live.getLabelSimple()).getParentAlgorithm();
			for (int index = 0; index < algorithm.getInput().length; index++) {
				GeoElement input = algorithm.getInput()[index];
				if (input.isIndependent() && !input.isLabelSet()) {
					assertEquals(exact(input), exact(copyAlgorithm.getInput()[index]),
							live.getLabelSimple() + " input " + index);
					compared.add(live.getLabelSimple() + "#" + index);
				}
			}
		}
		return compared;
	}

	private void move(double parameter) {
		GeoPoint driver = (GeoPoint) lookup("P");
		driver.getPathParameter().setT(parameter);
		((Path) lookup("c")).pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private static void setValue(GeoNumeric number, double value) {
		number.setValue(value);
		number.updateRepaint();
	}

	private String fingerprint(GeoLocusV2 locus) {
		GeoElement driver = lookup("P");
		return ReconstructionSliceFingerprint2D.compute(
				parent(locus).getReconstructionSliceGeos(), List.of(driver, driver));
	}

	private static AlgoDependentPointLocusV2 parent(GeoLocusV2 locus) {
		return assertInstanceOf(AlgoDependentPointLocusV2.class, locus.getParentAlgorithm());
	}

	private static GeoElement copy(GeoLocusV2 locus, String label) throws Exception {
		ReconstructibleLocusEvaluator2D evaluator = (ReconstructibleLocusEvaluator2D) field(
				parent(locus), "evaluator");
		Construction isolated = (Construction) field(evaluator, "isolatedConstruction");
		for (GeoElement geo : isolated.getGeoSetConstructionOrder()) {
			if (label.equals(geo.getLabelSimple())) {
				return geo;
			}
		}
		throw new AssertionError("No slice copy of " + label);
	}

	private static String exact(GeoElement geo) {
		assertNotNull(geo);
		if (geo instanceof GeoNumeric) {
			return "num:" + Double.toHexString(((GeoNumeric) geo).getValue());
		}
		GeoPoint point = assertInstanceOf(GeoPoint.class, geo);
		return "pt:" + Double.toHexString(point.getX()) + ","
				+ Double.toHexString(point.getY()) + "," + Double.toHexString(point.getZ());
	}

	private static Object field(Object target, String name) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException absent) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}

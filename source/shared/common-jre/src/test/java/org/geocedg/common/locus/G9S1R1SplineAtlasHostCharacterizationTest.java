/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spline.SplinePolynomialModel2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * Design-only characterization of a public double-traversal spline family.
 *
 * <p>The public cubic uses floating solved coefficients. Comparison with an
 * exactly defined C2 cubic reference is diagnostic, not an exact gluing or root
 * certificate. This class does not add a pair-materialization policy, selector,
 * atlas or continuation algorithm.</p>
 */
final class G9S1R1SplineAtlasHostCharacterizationTest
		extends G9U0PublicSurfaceTestBase {

	@Test
	void doubleTraversalCapturesPublicPeriodicSpansAndFloatingGluing() {
		Fixture fixture = createFixture();
		SplinePolynomialModel2D model = model(fixture.source);
		LocusDefinition2D definition = fixture.source.getSemanticDefinition();
		assertEquals(3, model.getDegree());
		assertEquals(8, model.getSpanCount());
		assertTrue(model.isClosed());
		assertTrue(definition.getProvider().isPeriodic());
		assertFalse(definition.getProvider().getDeclaredDomain().isUpperClosed());
		assertEquals(Orientation.INCREASING, definition.getProvider().getOrientation());
		assertEquals(1, definition.getBranches().size());
		assertEquals(AlgoSplineV2.BRANCH_KEY, definition.getBranches().get(0).getBranchKey());
		assertEquals(1, definition.getBranches().get(0).getValidDomainComponents().size());
		assertEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				definition.getBranches().get(0).getQuality().getNumericGuarantee());
		for (int knot = 0; knot <= 8; knot++) {
			assertEquals(Double.doubleToRawLongBits(knot / 8.0),
					Double.doubleToRawLongBits(model.getKnots()[knot]));
			assertEquals(Math.min(knot, 7), model.findOwningSpan(knot / 8.0));
			assertReferencePoint(model, knot / 8.0);
		}
		assertEquals(0.0, definition.getProvider().canonicalize(1.0));

		double coefficientError = 0;
		int differingCoefficientBits = 0;
		for (int span = 0; span < 8; span++) {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				double[] actual = model.getCoefficients(span, coordinate);
				double[] reference = referenceCoefficients(span, coordinate);
				for (int power = 0; power < actual.length; power++) {
					coefficientError = Math.max(coefficientError,
							Math.abs(actual[power] - reference[power]));
					if (Double.doubleToRawLongBits(actual[power])
							!= Double.doubleToRawLongBits(reference[power])) {
						differingCoefficientBits++;
					}
				}
			}
			assertReferencePoint(model, (span + 0.5) / 8.0);
		}
		assertTrue(Double.isFinite(coefficientError));
		System.out.println("G9S1_R1_ATLAS_HOST coefficient_max_abs="
				+ Double.toHexString(coefficientError) + " differing_coefficient_bits="
				+ differingCoefficientBits + " arithmetic=FLOATING_POINT_UNCERTIFIED");
		for (int derivative = 0; derivative <= 2; derivative++) {
			double defect = gluingDefect(model, derivative);
			assertTrue(Double.isFinite(defect));
			System.out.println("G9S1_R1_ATLAS_HOST gluing_derivative=" + derivative
					+ " max_abs=" + Double.toHexString(defect)
					+ " exact_certificate=NOT_ESTABLISHED");
		}
		characterizeStraightTarget(model(fixture.target));
	}

	@Test
	void directIncrementalAndReversePathsRecoverIdenticalCurrentCoefficientBits() {
		Fixture fixture = createFixture();
		final PersistentGeoId sourceId = fixture.source.getPersistentLocusId();
		final PersistentGeoId targetId = fixture.target.getPersistentLocusId();
		final String sourceBits = modelBits(fixture.source);
		final String initialTargetBits = modelBits(fixture.target);
		setDirection(fixture, 1, 0.25);
		String direct = modelBits(fixture.target);
		setDirection(fixture, 1, 0);
		for (int step = 1; step <= 8; step++) {
			setDirection(fixture, 1, step / 32.0);
		}
		assertEquals(direct, modelBits(fixture.target));
		setDirection(fixture, 1, 0.5);
		setDirection(fixture, 0.75, 0.125);
		setDirection(fixture, 1, 0.25);
		assertEquals(direct, modelBits(fixture.target));
		assertEquals(sourceBits, modelBits(fixture.source));
		assertEquals(sourceId, fixture.source.getPersistentLocusId());
		assertEquals(targetId, fixture.target.getPersistentLocusId());
		assertSame(fixture.source, requireLookup("S"));
		assertSame(fixture.target, requireLookup("T"));
		setDirection(fixture, 1, 0);
		assertEquals(initialTargetBits, modelBits(fixture.target));
	}

	@Test
	void closedControlLoopReturnsExactInputsAndDefinitionsWithoutPointTracking() {
		Fixture fixture = createFixture();
		PersistentGeoId sourceId = fixture.source.getPersistentLocusId();
		PersistentGeoId targetId = fixture.target.getPersistentLocusId();
		String sourceBits = modelBits(fixture.source);
		String targetBits = modelBits(fixture.target);
		double[][] loop = {{1, 1}, {0, 1}, {-1, 1}, {-1, 0},
				{-1, -1}, {0, -1}, {1, -1}, {1, 0}};
		for (double[] direction : loop) {
			setDirection(fixture, direction[0], direction[1]);
			assertTrue(fixture.target.isDefined());
			assertEquals(sourceId, fixture.source.getPersistentLocusId());
			assertEquals(targetId, fixture.target.getPersistentLocusId());
		}
		assertEquals(Double.doubleToRawLongBits(1.0),
				Double.doubleToRawLongBits(fixture.x.getDouble()));
		assertEquals(Double.doubleToRawLongBits(0.0),
				Double.doubleToRawLongBits(fixture.y.getDouble()));
		assertEquals(sourceBits, modelBits(fixture.source));
		assertEquals(targetBits, modelBits(fixture.target));
		assertSame(fixture.source, requireLookup("S"));
		assertSame(fixture.target, requireLookup("T"));
		System.out.println("G9S1_R1_ATLAS_HOST control_loop=EXACT_FINAL_DEFINITIONS"
				+ " materialized_pair_points=0 monodromy_certificate=NOT_CLAIMED");
	}

	@Test
	void hostXmlRoundTripRetainsSourceIdsAndCurrentPolynomialDefinitions() {
		Fixture fixture = createFixture();
		setDirection(fixture, 1, 0.25);
		PersistentGeoId sourceId = fixture.source.getPersistentLocusId();
		PersistentGeoId targetId = fixture.target.getPersistentLocusId();
		String sourceBits = modelBits(fixture.source);
		String targetBits = modelBits(fixture.target);
		String xml = getApp().getXML();
		assertTrue(xml.contains("name=\"SplineV2\""));
		getApp().setXML(xml, true);
		Fixture reopened = lookupFixture();
		assertEquals(sourceId, reopened.source.getPersistentLocusId());
		assertEquals(targetId, reopened.target.getPersistentLocusId());
		assertEquals(sourceBits, modelBits(reopened.source));
		assertEquals(targetBits, modelBits(reopened.target));
		assertTrue(reopened.source.getSemanticDefinition().getProvider().isPeriodic());
		setDirection(reopened, 0.75, 0.5);
		setDirection(reopened, 1, 0.25);
		assertEquals(targetBits, modelBits(reopened.target));
		System.out.println("G9S1_R1_ATLAS_HOST persistence=HOST_XML_ROUNDTRIP"
				+ " native_cedg_zip=NOT_TESTED identical_definition_bits=true");
	}

	@Test
	void currentPublicSplinePairRemainsRichOnlyWithoutAssumingRootCount() {
		Fixture fixture = createFixture();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertNotNull(rich);
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		assertNotNull(result);
		assertTrue(result.getFiniteSolutions().stream().noneMatch(root ->
				rich.isPointAdmissible(root.getIdentity().getRootToken())));
		assertNotNull(fixture.source.getPersistentLocusId());
		assertNotNull(fixture.target.getPersistentLocusId());
		System.out.println("G9S1_R1_ATLAS_HOST public_pair_status="
				+ result.getComputationStatus() + " geometry=" + result.getGeometryKind()
				+ " finite_roots=" + result.getFiniteSolutions().size()
				+ " completeness=" + result.getCompletenessEvidence().getCompleteness()
				+ " materializable=0 expected_root_count=NOT_ASSERTED");
	}

	private Fixture createFixture() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		add("w(x,y)=1");
		add("S=SplineV2({A,B,C,D,A,B,C,D,A},3,w)");
		add("rx=1");
		add("ry=0");
		add("E=(rx/2,ry/2)");
		add("F=(5*rx/6,5*ry/6)");
		add("G=(7*rx/6,7*ry/6)");
		add("H=(3*rx/2,3*ry/2)");
		// Public SplineV2 rejects degree 1. Use collinear data for the public cubic.
		add("T=SplineV2({E,F,G,H},3,w)");
		Fixture fixture = lookupFixture();
		assertTrue(fixture.source.isDefined());
		assertTrue(fixture.target.isDefined());
		return fixture;
	}

	private Fixture lookupFixture() {
		return new Fixture(assertInstanceOf(GeoLocusV2.class, requireLookup("S")),
				assertInstanceOf(GeoLocusV2.class, requireLookup("T")),
				assertInstanceOf(GeoNumeric.class, requireLookup("rx")),
				assertInstanceOf(GeoNumeric.class, requireLookup("ry")));
	}

	private static void setDirection(Fixture fixture, double x, double y) {
		fixture.x.setValue(x);
		fixture.y.setValue(y);
		fixture.x.updateCascade();
		fixture.y.updateCascade();
	}

	private static SplinePolynomialModel2D model(GeoLocusV2 source) {
		return assertInstanceOf(AlgoSplineV2.class, source.getParentAlgorithm())
				.getPolynomialModel();
	}

	private static String modelBits(GeoLocusV2 source) {
		SplinePolynomialModel2D model = model(source);
		StringBuilder result = new StringBuilder().append(model.getDegree())
				.append('|').append(model.isClosed());
		for (double knot : model.getKnots()) {
			result.append('|').append(Double.doubleToRawLongBits(knot));
		}
		for (int span = 0; span < model.getSpanCount(); span++) {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				for (double coefficient : model.getCoefficients(span, coordinate)) {
					result.append('|').append(Double.doubleToRawLongBits(coefficient));
				}
			}
		}
		return result.toString();
	}

	private static void characterizeStraightTarget(SplinePolynomialModel2D model) {
		assertEquals(3, model.getDegree());
		assertFalse(model.isClosed());
		double highOrder = 0;
		for (int span = 0; span < model.getSpanCount(); span++) {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				double[] coefficients = model.getCoefficients(span, coordinate);
				highOrder = Math.max(highOrder,
						Math.max(Math.abs(coefficients[0]), Math.abs(coefficients[1])));
			}
		}
		for (double parameter : new double[] {0, 0.25, 0.5, 0.75, 1}) {
			assertEquals(0.5 + parameter, model.evaluate(parameter)[0], 1E-9);
			assertEquals(0, model.evaluate(parameter)[1], 1E-9);
		}
		System.out.println("G9S1_R1_ATLAS_HOST linear_image_public_degree=3"
				+ " high_order_coefficient_max_abs=" + Double.toHexString(highOrder));
	}

	private static void assertReferencePoint(SplinePolynomialModel2D model, double u) {
		int span = Math.min((int) (u * 8), 7);
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			// This is only numerical agreement with the reference, not exact gluing.
			assertEquals(derivative(referenceCoefficients(span, coordinate), u, 0),
					model.evaluate(u)[coordinate], 1E-8);
		}
	}

	private static double gluingDefect(SplinePolynomialModel2D model, int order) {
		double maximum = 0;
		for (int right = 0; right < model.getSpanCount(); right++) {
			int left = (right + model.getSpanCount() - 1) % model.getSpanCount();
			double rightParameter = model.getKnots()[right];
			double leftParameter = right == 0 ? 1 : rightParameter;
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				maximum = Math.max(maximum, Math.abs(
						derivative(model.getCoefficients(left, coordinate), leftParameter, order)
						- derivative(model.getCoefficients(right, coordinate),
								rightParameter, order)));
			}
		}
		return maximum;
	}

	private static double derivative(double[] coefficients, double parameter, int order) {
		double value = 0;
		for (int index = 0; index < coefficients.length - order; index++) {
			double coefficient = coefficients[index];
			int degree = coefficients.length - index - 1;
			for (int factor = 0; factor < order; factor++) {
				coefficient *= degree - factor;
			}
			value = value * parameter + coefficient;
		}
		return value;
	}

	private static double[] referenceCoefficients(int span, int coordinate) {
		// q=8u-span, x=1-3q^2/2+q^3/2, y=3q/2-q^3/2, rotated by span*pi/2.
		int quadrant = span % 4;
		boolean useX = (coordinate == 0) == (quadrant % 2 == 0);
		double sign = coordinate == 0 ? (quadrant == 1 || quadrant == 2 ? -1 : 1)
				: (quadrant >= 2 ? -1 : 1);
		double cubic = useX ? 0.5 : -0.5;
		double quadratic = useX ? -1.5 : 0;
		double linear = useX ? 0 : 1.5;
		double constant = useX ? 1 : 0;
		double shift = -span;
		return new double[] {sign * cubic * 512,
				sign * (3 * cubic * 64 * shift + quadratic * 64),
				sign * (3 * cubic * 8 * shift * shift + 2 * quadratic * 8 * shift
						+ linear * 8),
				sign * (cubic * shift * shift * shift + quadratic * shift * shift
						+ linear * shift + constant)};
	}

	private static final class Fixture {
		private final GeoLocusV2 source;
		private final GeoLocusV2 target;
		private final GeoNumeric x;
		private final GeoNumeric y;

		private Fixture(GeoLocusV2 source, GeoLocusV2 target, GeoNumeric x, GeoNumeric y) {
			this.source = source;
			this.target = target;
			this.x = x;
			this.y = y;
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Productive G8C1 DAG, token, failure and point-consumer tests. */
class G8C1ExtendedTargetLifecycleTest extends BaseUnitTest {

	@Test
	void optionBPointConsumesVerifiedRootWithoutGlobalCompleteness() {
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"option-b", 0, 3, 0);
		AlgoLocusIntersectionV2 intersection = algorithm(fixture,
				add("x^2/4+y^2=1"), "option-b");
		LocusIntersectionResult2D result = result(intersection);
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		String token = onlyToken(result);
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		assertTrue(point.getPoint().isDefined());
		assertEquals(2, point.getPoint().getInhomX(), 2E-11);
		assertEquals(Completeness.NOT_ESTABLISHED,
				point.getRichInput().getIntersectionResult()
						.getCompletenessEvidence().getCompleteness());
	}

	@Test
	void sourceMotionPreservesTokenAndUpdatesThreeDownstreamDagLevels() {
		var fixture = G8C1IntersectionTestSupport.curve(getConstruction(),
				"source-motion", 0, 3,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, source));
		AlgoLocusIntersectionV2 intersection = algorithm(fixture,
				add("x^2/4+y^2=1"), "source-motion");
		String token = onlyToken(result(intersection));
		AlgoLocusIntersectionPointV2 selected = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		OffsetPointAlgo depthOne = new OffsetPointAlgo(getConstruction(),
				selected.getPoint());
		OffsetPointAlgo depthTwo = new OffsetPointAlgo(getConstruction(),
				depthOne.getPoint());
		OffsetPointAlgo depthThree = new OffsetPointAlgo(getConstruction(),
				depthTwo.getPoint());
		fixture.source().setValue(0.6);
		fixture.source().updateCascade();
		assertEquals(token, onlyToken(result(intersection)));
		double x = 2 * Math.sqrt(1 - 0.36);
		assertEquals(x, selected.getPoint().getInhomX(), 2E-11);
		assertEquals(x + 3, depthThree.getPoint().getInhomX(), 2E-11);
		assertEquals(6.6, depthThree.getPoint().getInhomY(), 2E-11);
	}

	@Test
	void targetMotionPreservesSemanticTokenWithoutCoordinateMatching() {
		GeoNumeric shift = add("a=0");
		GeoConic target = add("(x-a)^2/4+y^2=1");
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"target-motion", 0, 4, 0);
		AlgoLocusIntersectionV2 intersection = algorithm(fixture, target,
				"target-motion");
		String token = onlyToken(result(intersection));
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		shift.setValue(0.5);
		shift.updateCascade();
		assertEquals(token, onlyToken(result(intersection)));
		assertEquals(2.5, point.getPoint().getInhomX(), 2E-11);
		assertTrue(result(intersection).getWork().getContinuationComparisons() > 0);
	}

	@Test
	void disappearanceMakesPointUndefinedAndRecoveryUsesEstablishedKey() {
		GeoNumeric shift = add("a=0");
		GeoConic target = add("(x-a)^2/4+y^2=1");
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"recovery", 0, 3, 0);
		AlgoLocusIntersectionV2 intersection = algorithm(fixture, target,
				"recovery");
		String token = onlyToken(result(intersection));
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		shift.setValue(6);
		shift.updateCascade();
		assertEquals(GeometryKind.UNRESOLVED,
				result(intersection).getGeometryKind());
		assertFalse(point.getPoint().isDefined());
		shift.setValue(0);
		shift.updateCascade();
		assertEquals(token, onlyToken(result(intersection)));
		assertTrue(point.getPoint().isDefined());
		assertEquals(2, point.getPoint().getInhomX(), 2E-11);
	}

	@Test
	void discoveringNewRootAndChangingOrderNeverRetargetsSelectedToken() {
		GeoNumeric mode = new GeoNumeric(getConstruction(), 0);
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"new-root", -3, 3, 0);
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("new-root/v1", context -> {
					var positive = G8BIntersectionFixtures.Root.simple(
							fixture.branchKey(), fixture.componentKey(), 2,
							"positive-root");
					if (mode.getDouble() == 0) {
						return G8BIntersectionFixtures.roots(context,
								List.of(positive), Completeness.NOT_ESTABLISHED);
					}
					var negative = G8BIntersectionFixtures.Root.simple(
							fixture.branchKey(), fixture.componentKey(), -2,
							"negative-root");
					return G8BIntersectionFixtures.roots(context,
							List.of(negative, positive),
							Completeness.NOT_ESTABLISHED);
				});
		AlgoLocusIntersectionV2 intersection = G8C1IntersectionTestSupport
				.algorithm(getConstruction(), fixture,
						add("x^2/4+y^2=1"), "new-root", capability,
						new GeoElement[] {mode});
		String token = onlyToken(result(intersection));
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		mode.setValue(1);
		mode.updateCascade();
		assertEquals(2, result(intersection).getFiniteSolutions().size());
		assertEquals(token, tokenForKey(result(intersection), "positive-root"));
		assertEquals(2, point.getPoint().getInhomX(), 0);
	}

	@Test
	void identicalCoordinatesWithDistinctPreimagesRemainDistinctSolutions() {
		var fixture = G8C1IntersectionTestSupport.curve(getConstruction(),
				"constructive-preimages", -2, 2,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter * parameter, 0));
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), fixture, add("x^2+y^2/4=1"),
				"constructive-preimages");
		assertEquals(2, result.getFiniteSolutions().size());
		assertEquals(result.getFiniteSolutions().get(0).getEvaluatedPoint().getX(),
				result.getFiniteSolutions().get(1).getEvaluatedPoint().getX(),
				2E-11);
		assertNotEquals(result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken(), result.getFiniteSolutions().get(1).getIdentity()
				.getRootToken());
	}

	@Test
	void twoToOneToTwoTangencyKeepsAmbiguityExplicit() {
		var fixture = G8C1IntersectionTestSupport.curve(getConstruction(),
				"merge-split", -3, 3,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, source));
		fixture.source().setValue(0.5);
		fixture.source().updateCascade();
		AlgoLocusIntersectionV2 intersection = algorithm(fixture,
				add("x^2/4+y^2=1"), "merge-split");
		assertEquals(2, result(intersection).getFiniteSolutions().size());
		assertTrue(result(intersection).getFiniteSolutions().stream()
				.noneMatch(solution -> solution.getIdentity()
						.getExplicitContinuationKey().isPresent()));
		fixture.source().setValue(1);
		fixture.source().updateCascade();
		assertEquals(1, result(intersection).getFiniteSolutions().size());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				result(intersection).getFiniteSolutions().get(0)
						.getClassification().getContactClass());
		fixture.source().setValue(0.5);
		fixture.source().updateCascade();
		assertEquals(2, result(intersection).getFiniteSolutions().size());
		assertTrue(result(intersection).getFiniteSolutions().stream()
				.noneMatch(solution -> solution.getIdentity()
						.getExplicitContinuationKey().isPresent()));
	}

	@Test
	void currentFailureNeverLeaksOldPointAndRecoveryIsAtomic() {
		GeoNumeric state = new GeoNumeric(getConstruction(), 0);
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"atomic-failure", 0, 3, 0);
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("atomic-failure/v1", context -> {
					if (state.getDouble() == 1) {
						throw new IllegalStateException("injected private failure");
					}
					return G8BIntersectionFixtures.roots(context,
							List.of(G8BIntersectionFixtures.Root.simple(
									fixture.branchKey(), fixture.componentKey(), 2,
									"stable-root")),
							Completeness.NOT_ESTABLISHED);
				});
		AlgoLocusIntersectionV2 intersection = G8C1IntersectionTestSupport
				.algorithm(getConstruction(), fixture,
						add("x^2/4+y^2=1"), "atomic-failure", capability,
						new GeoElement[] {state});
		String token = onlyToken(result(intersection));
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		state.setValue(1);
		state.updateCascade();
		assertEquals(ComputationStatus.NUMERICAL_FAILURE,
				result(intersection).getComputationStatus());
		assertFalse(point.getPoint().isDefined());
		state.setValue(0);
		state.updateCascade();
		assertEquals(token, onlyToken(result(intersection)));
		assertTrue(point.getPoint().isDefined());
	}

	@Test
	void boundedFunctionDependencyUpdatesThroughNormalDag() {
		GeoNumeric offset = add("b=1");
		GeoFunction function = add("f(x)=x+b");
		assertTrue(function.setInterval(-2, 0));
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"function-dag", -2, 0, 0);
		AlgoLocusIntersectionV2 intersection = algorithm(fixture, function,
				"function-dag");
		String token = onlyToken(result(intersection));
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), intersection.getResult(), token);
		offset.setValue(0.5);
		offset.updateCascade();
		assertEquals(token, onlyToken(result(intersection)));
		assertEquals(-0.5, point.getPoint().getInhomX(), 2E-11);
	}

	@Test
	void viewportChangesDoNotAffectExtendedSemanticResult() {
		var fixture = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"viewport", 0, 3, 0);
		AlgoLocusIntersectionV2 intersection = algorithm(fixture,
				add("x^2/4+y^2=1"), "viewport");
		String token = onlyToken(result(intersection));
		double parameter = onlyParameter(result(intersection));
		getApp().getEuclidianView1().setRealWorldCoordSystem(-100, 100, -50, 50);
		intersection.compute();
		assertEquals(token, onlyToken(result(intersection)));
		assertEquals(parameter, onlyParameter(result(intersection)), 0);
		assertTrue(result(intersection).getWork().hasZeroForbiddenAuthorityReads());
	}

	private AlgoLocusIntersectionV2 algorithm(
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			String identity) {
		return G8C1IntersectionTestSupport.algorithm(getConstruction(), fixture,
				target, identity);
	}

	private static LocusIntersectionResult2D result(
			AlgoLocusIntersectionV2 algorithm) {
		return algorithm.getResult().getIntersectionResult();
	}

	private static String onlyToken(LocusIntersectionResult2D result) {
		assertEquals(1, result.getFiniteSolutions().size());
		return result.getFiniteSolutions().get(0).getIdentity().getRootToken();
	}

	private static double onlyParameter(LocusIntersectionResult2D result) {
		assertEquals(1, result.getFiniteSolutions().size());
		return result.getFiniteSolutions().get(0).getRevisionEvidence()
				.getSemanticParameter();
	}

	private static String tokenForKey(LocusIntersectionResult2D result,
			String key) {
		return result.getFiniteSolutions().stream()
				.filter(solution -> solution.getIdentity()
						.getExplicitContinuationKey().filter(key::equals).isPresent())
				.map(solution -> solution.getIdentity().getRootToken()).findFirst()
				.orElseThrow();
	}

	private static final class OffsetPointAlgo extends AlgoElement {
		private final GeoPoint inputPoint;
		private final GeoPoint point;

		OffsetPointAlgo(Construction construction, GeoPoint inputPoint) {
			super(construction, false);
			this.inputPoint = inputPoint;
			point = new GeoPoint(construction);
			setInputOutput();
			setDependencies();
			compute();
		}

		@Override
		protected void setInputOutput() {
			input = new GeoElement[] {inputPoint};
			setOnlyOutput(point);
		}

		@Override
		public void compute() {
			if (!inputPoint.isDefined()) {
				point.setUndefined();
				return;
			}
			point.setCoords(inputPoint.getInhomX() + 1,
					inputPoint.getInhomY() + 2, 1);
		}

		GeoPoint getPoint() {
			return point;
		}

		@Override
		public Algos getClassName() {
			return Algos.Expression;
		}
	}
}

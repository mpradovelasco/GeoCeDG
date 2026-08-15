/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusLocusIntersectionV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapRelationKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.locus.G8C2IntersectionTestSupport.PairRoot;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Normal-DAG, strict-token, ambiguity and atomicity tests for G8C2. */
class G8C2LocusPairLifecycleTest extends BaseUnitTest {

	@Test
	void onlyFirstSourceMotionPreservesTokenAndUpdatesPoint() {
		MovingCrossing crossing = movingCrossing("first-motion");
		String token = onlyToken(result(crossing.algorithm));
		AlgoLocusIntersectionPointV2 point = selected(crossing.algorithm, token);
		crossing.first.source().setValue(0.25);
		crossing.first.source().updateCascade();
		assertEquals(token, onlyToken(result(crossing.algorithm)));
		assertEquals(0, point.getPoint().getInhomX(), 2E-12);
		assertEquals(0.25, point.getPoint().getInhomY(), 2E-12);
		assertEquals(IdentityStatus.CONTINUATION_ESTABLISHED,
				result(crossing.algorithm).getFiniteSolutions().get(0)
						.getIdentity().getIdentityStatus());
	}

	@Test
	void onlySecondAndThenBothSourcesUpdateCoherently() {
		MovingCrossing crossing = movingCrossing("both-motion");
		String token = onlyToken(result(crossing.algorithm));
		AlgoLocusIntersectionPointV2 point = selected(crossing.algorithm, token);
		crossing.second.source().setValue(0.4);
		crossing.second.source().updateCascade();
		assertEquals(token, onlyToken(result(crossing.algorithm)));
		assertEquals(0.4, point.getPoint().getInhomX(), 2E-12);
		crossing.first.source().setValue(-0.3);
		crossing.second.source().setValue(-0.2);
		crossing.first.source().updateCascade();
		crossing.second.source().updateCascade();
		assertEquals(token, onlyToken(result(crossing.algorithm)));
		assertEquals(-0.2, point.getPoint().getInhomX(), 2E-12);
		assertEquals(-0.3, point.getPoint().getInhomY(), 2E-12);
	}

	@Test
	void discoveredRootAndOrderingChangeDoNotRetargetExistingToken() {
		GeoNumeric mode = new GeoNumeric(getConstruction(), 0);
		var folded = G8C2IntersectionTestSupport.curve(getConstruction(),
				"new-root-A", -2, 2, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter - 1));
		var axis = G8C2IntersectionTestSupport.line(getConstruction(),
				"new-root-B", -2, 2, 0, true);
		var capability = G8C2IntersectionTestSupport.capability("new-root/v1",
				context -> {
					PairRoot positive = G8C2IntersectionTestSupport.simple(1, 1,
							"positive");
					if (mode.getDouble() == 0) {
						return G8C2IntersectionTestSupport.roots(context,
								List.of(positive), Completeness.NOT_ESTABLISHED);
					}
					PairRoot negative = G8C2IntersectionTestSupport.simple(-1,
							-1, "negative");
					return G8C2IntersectionTestSupport.roots(context,
							List.of(negative, positive),
							Completeness.NOT_ESTABLISHED);
				});
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), folded, axis, "new-root", capability,
						new GeoElement[] {mode});
		String token = onlyToken(result(algorithm));
		AlgoLocusIntersectionPointV2 point = selected(algorithm, token);
		mode.setValue(1);
		mode.updateCascade();
		assertEquals(2, result(algorithm).getFiniteSolutions().size());
		assertEquals(token, tokenForKey(result(algorithm), "positive"));
		assertEquals(1, point.getPoint().getInhomX(), 2E-12);
	}

	@Test
	void disappearanceMakesPointUndefinedAndSemanticRecoveryRestoresIt() {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"recovery-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"recovery-B", -1, 1, 0, false);
		var capability = G8C2IntersectionTestSupport.capability("recovery/v1",
				context -> Math.abs(first.source().getDouble()) > 1
						? G8C2IntersectionTestSupport.completeEmpty(context)
						: G8C2IntersectionTestSupport.roots(context,
								List.of(G8C2IntersectionTestSupport.simple(0,
										first.source().getDouble(), "stable")),
								Completeness.COMPLETE));
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), first, second, "recovery", capability,
						new GeoElement[0]);
		String token = onlyToken(result(algorithm));
		AlgoLocusIntersectionPointV2 point = selected(algorithm, token);
		first.source().setValue(2);
		first.source().updateCascade();
		assertEquals(GeometryKind.EMPTY, result(algorithm).getGeometryKind());
		assertFalse(point.getPoint().isDefined());
		first.source().setValue(0);
		first.source().updateCascade();
		assertEquals(token, onlyToken(result(algorithm)));
		assertTrue(point.getPoint().isDefined());
	}

	@Test
	void overlapTransitionDoesNotInventContinuationAcrossInfiniteSet() {
		GeoNumeric mode = new GeoNumeric(getConstruction(), 1);
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"overlap-transition-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.curve(getConstruction(),
				"overlap-transition-B", -1, 1, false,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter,
								mode.getDouble() * parameter));
		var capability = G8C2IntersectionTestSupport.capability(
				"overlap-transition/v1", context -> mode.getDouble() == 0
						? G8C2IntersectionTestSupport.overlap(context,
								OverlapStatus.OVERLAP_ESTABLISHED,
								OverlapRelationKind.FULL_COMPONENT,
								GeometryKind.OVERLAP)
						: G8C2IntersectionTestSupport.roots(context,
								List.of(G8C2IntersectionTestSupport.simple(0, 0,
										"cross")), Completeness.COMPLETE));
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), first, second, "overlap-transition",
						capability, new GeoElement[] {mode});
		String oldToken = onlyToken(result(algorithm));
		AlgoLocusIntersectionPointV2 point = selected(algorithm, oldToken);
		mode.setValue(0);
		mode.updateCascade();
		assertFalse(point.getPoint().isDefined());
		mode.setValue(1);
		mode.updateCascade();
		String newToken = onlyToken(result(algorithm));
		assertNotEquals(oldToken, newToken);
		assertFalse(point.getPoint().isDefined());
	}

	@Test
	void twoToOneToTwoMergeSplitLeavesAmbiguityExplicit() {
		var parabola = G8C2IntersectionTestSupport.curve(getConstruction(),
				"merge-A", -2, 2, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						parameter * parameter + source));
		var axis = G8C2IntersectionTestSupport.line(getConstruction(),
				"merge-B", -2, 2, 0, true);
		parabola.source().setValue(-1);
		parabola.source().updateCascade();
		var capability = G8C2IntersectionTestSupport.capability("merge/v1",
				context -> {
					double state = parabola.source().getDouble();
					if (state == 0) {
						PairRoot tangent = new PairRoot(0, 0, "merge", "merge",
								ContactClass.TANGENT_ESTABLISHED, 2, false,
								LineageEventKind.MERGE_CANDIDATE,
								List.of("left", "right"));
						return G8C2IntersectionTestSupport.roots(context,
								List.of(tangent), Completeness.COMPLETE);
					}
					double root = Math.sqrt(-state);
					PairRoot left = new PairRoot(-root, -root, "left", "left",
							ContactClass.TRANSVERSE_ESTABLISHED, 1, true,
							LineageEventKind.SPLIT_CANDIDATE, List.of("merge"));
					PairRoot right = new PairRoot(root, root, "right", "right",
							ContactClass.TRANSVERSE_ESTABLISHED, 1, true,
							LineageEventKind.SPLIT_CANDIDATE, List.of("merge"));
					return G8C2IntersectionTestSupport.roots(context,
							List.of(left, right), Completeness.COMPLETE);
				});
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), parabola, axis, "merge", capability,
						new GeoElement[0]);
		String leftToken = tokenForKey(result(algorithm), "left");
		AlgoLocusIntersectionPointV2 point = selected(algorithm, leftToken);
		parabola.source().setValue(0);
		parabola.source().updateCascade();
		assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
				result(algorithm).getFiniteSolutions().get(0).getIdentity()
						.getIdentityStatus());
		assertFalse(point.getPoint().isDefined());
		parabola.source().setValue(-1);
		parabola.source().updateCascade();
		assertTrue(result(algorithm).getFiniteSolutions().stream().allMatch(
				solution -> solution.getIdentity().getIdentityStatus()
						== IdentityStatus.AMBIGUOUS_CONTINUATION));
		assertFalse(point.getPoint().isDefined());
	}

	@Test
	void atomicCapabilityFailureNeverLeaksOldPointAndRecovers() {
		GeoNumeric failure = new GeoNumeric(getConstruction(), 0);
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				"failure-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"failure-B", -1, 1, 0, false);
		var capability = G8C2IntersectionTestSupport.capability("failure/v1",
				context -> {
					if (failure.getDouble() == 1) {
						throw new IllegalStateException("injected pair failure");
					}
					return G8C2IntersectionTestSupport.roots(context,
							List.of(G8C2IntersectionTestSupport.simple(0, 0,
									"stable")), Completeness.COMPLETE);
				});
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), first, second, "failure", capability,
						new GeoElement[] {failure});
		String token = onlyToken(result(algorithm));
		AlgoLocusIntersectionPointV2 point = selected(algorithm, token);
		failure.setValue(1);
		failure.updateCascade();
		assertEquals(ComputationStatus.NUMERICAL_FAILURE,
				result(algorithm).getComputationStatus());
		assertFalse(point.getPoint().isDefined());
		failure.setValue(0);
		failure.updateCascade();
		assertEquals(token, onlyToken(result(algorithm)));
		assertTrue(point.getPoint().isDefined());
	}

	@Test
	void tokenSelectedPointDrivesThreeNormalDagLevels() {
		MovingCrossing crossing = movingCrossing("nested");
		String token = onlyToken(result(crossing.algorithm));
		AlgoLocusIntersectionPointV2 selected = selected(crossing.algorithm,
				token);
		OffsetPointAlgo depthOne = new OffsetPointAlgo(getConstruction(),
				selected.getPoint());
		OffsetPointAlgo depthTwo = new OffsetPointAlgo(getConstruction(),
				depthOne.getPoint());
		OffsetPointAlgo depthThree = new OffsetPointAlgo(getConstruction(),
				depthTwo.getPoint());
		crossing.first.source().setValue(0.5);
		crossing.second.source().setValue(0.25);
		crossing.first.source().updateCascade();
		crossing.second.source().updateCascade();
		assertEquals(3.25, depthThree.getPoint().getInhomX(), 2E-12);
		assertEquals(6.5, depthThree.getPoint().getInhomY(), 2E-12);
	}

	@Test
	void periodicSeamRepresentationContinuesBySemanticKey() {
		GeoNumeric seam = new GeoNumeric(getConstruction(), 0);
		var first = G8C2IntersectionTestSupport.curve(getConstruction(),
				"seam-A", -Math.PI, Math.PI, true,
				(source, branch, parameter) ->
						new LocusPoint2D(Math.cos(parameter), Math.sin(parameter)));
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				"seam-B", -1, 1, -1, false);
		var capability = G8C2IntersectionTestSupport.capability("seam/v1",
				context -> G8C2IntersectionTestSupport.roots(context,
						List.of(G8C2IntersectionTestSupport.simple(
								seam.getDouble() == 0 ? -Math.PI : Math.PI, 0,
								"seam-root")), Completeness.COMPLETE));
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), first, second, "seam", capability,
						new GeoElement[] {seam});
		String token = onlyToken(result(algorithm));
		seam.setValue(1);
		seam.updateCascade();
		assertEquals(token, onlyToken(result(algorithm)));
		assertEquals(-Math.PI, result(algorithm).getFiniteSolutions().get(0)
				.getPairEvidence().orElseThrow().getFirst()
				.getSemanticParameter(), 0);
	}

	@Test
	void viewportStateNeverAffectsPairIdentityOrGeometry() {
		MovingCrossing crossing = movingCrossing("viewport");
		String token = onlyToken(result(crossing.algorithm));
		LocusPoint2D point = result(crossing.algorithm).getFiniteSolutions().get(0)
				.getEvaluatedPoint();
		getApp().getEuclidianView1().setRealWorldCoordSystem(-100, 100, -50, 50);
		crossing.algorithm.compute();
		assertEquals(token, onlyToken(result(crossing.algorithm)));
		assertEquals(point,
				result(crossing.algorithm).getFiniteSolutions().get(0)
						.getEvaluatedPoint());
		assertTrue(result(crossing.algorithm).getWork()
				.hasZeroForbiddenAuthorityReads());
	}

	private MovingCrossing movingCrossing(String identity) {
		var first = G8C2IntersectionTestSupport.line(getConstruction(),
				identity + "-A", -1, 1, 0, true);
		var second = G8C2IntersectionTestSupport.line(getConstruction(),
				identity + "-B", -1, 1, 0, false);
		var capability = G8C2IntersectionTestSupport.capability(identity + "/v1",
				context -> G8C2IntersectionTestSupport.roots(context,
						List.of(G8C2IntersectionTestSupport.simple(
								second.source().getDouble(),
								first.source().getDouble(), "moving-cross")),
						Completeness.NOT_ESTABLISHED));
		AlgoLocusLocusIntersectionV2 algorithm = G8C2IntersectionTestSupport
				.algorithm(getConstruction(), first, second, identity, capability,
						new GeoElement[0]);
		return new MovingCrossing(first, second, algorithm);
	}

	private AlgoLocusIntersectionPointV2 selected(
			AlgoLocusLocusIntersectionV2 algorithm, String token) {
		return new AlgoLocusIntersectionPointV2(getConstruction(),
				algorithm.getResult(), token);
	}

	private static LocusIntersectionResult2D result(
			AlgoLocusLocusIntersectionV2 algorithm) {
		return algorithm.getResult().getIntersectionResult();
	}

	private static String onlyToken(LocusIntersectionResult2D result) {
		assertEquals(1, result.getFiniteSolutions().size());
		return result.getFiniteSolutions().get(0).getIdentity().getRootToken();
	}

	private static String tokenForKey(LocusIntersectionResult2D result,
			String key) {
		return result.getFiniteSolutions().stream()
				.filter(solution -> solution.getIdentity()
						.getExplicitContinuationKey().filter(key::equals).isPresent())
				.map(solution -> solution.getIdentity().getRootToken()).findFirst()
				.orElseThrow();
	}

	private record MovingCrossing(G8BIntersectionFixtures.Fixture first,
			G8BIntersectionFixtures.Fixture second,
			AlgoLocusLocusIntersectionV2 algorithm) {
	}

	private static final class OffsetPointAlgo extends AlgoElement {
		private final GeoPoint inputPoint;
		private final GeoPoint point;

		OffsetPointAlgo(Construction construction, GeoPoint inputPoint) {
			super(construction, false);
			this.inputPoint = inputPoint;
			this.point = new GeoPoint(construction);
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

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Policy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D.OriginWrap;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D.Piece;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T6: the structural certificate of a between-position length and the
 * certification of a locus that depends on it ({@code locus-v2-metrics.md} section 26;
 * oriented length design sections 4 and 5), on synthetic constructions. Matrix rows V-CMP-1,
 * V-UND-1, V-E2-4, V-E2-6 and V-E2-8 (structure parts) and gate GC-6; the author model is
 * step T7. The length of the source circle is never read: its jump is bounded by a chord and
 * by an enclosure of the speed.
 */
class PreG9BCu2MetricStructureTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-CMP-1, V-E2-4 and V-E2-1 (synthetic parts), GC-6: the length from a fixed origin on a
	 * closed source is defined everywhere with one origin wrap at the exact address of the
	 * origin, and the dependent locus is one full-length open arc that starts and ends there:
	 * the jump is a boundary, never an interior point. No metric value is read.
	 */
	@Test
	void anOriginWrapCutsTheDependentLocusIntoOneOpenArc() {
		closedSource("0", "");
		LocusMetricFunctionStructure2D structure = structure("w");
		assertTrue(structure.isCertified(), structure.toString());
		assertEquals(1, structure.getPieces().size());
		Piece piece = structure.getPieces().get(0);
		assertEquals(LocusMetricFunctionStructure2D.State.DEFINED, piece.getState());
		assertEquals(new LocusInterval2D(-Math.PI, Math.PI, true, false), piece.getInterval());
		OriginWrap wrap = singleWrap(structure);
		assertEquals(0, wrap.getDriverParameter());
		assertEquals(TraversalDirection.FORWARD, wrap.getDirection());
		assertEquals(SamePositionPolicy.ZERO_LENGTH, wrap.getSamePosition());
		assertTrue(wrap.isJumpPositive());
		// The circle of radius 2 has length 4 pi: a chord bounds it below and the speed
		// enclosures above, without the value.
		assertTrue(wrap.getJumpLowerBound() > 0 && wrap.getJumpLowerBound() <= 4 * Math.PI);
		assertTrue(wrap.getJumpUpperBound() >= 4 * Math.PI);
		assertTrue(wrap.getJumpValueReference().startsWith("LocusLength("));
		LocusMetricResult2D jump = metric("w").resolveJumpValue();
		assertNotNull(jump);
		assertEquals(4 * Math.PI, jump.getMetricValue().getFiniteValue().getAsDouble(), 1E-6);
		assertNotNull(jump.getGuaranteeClass());

		long reads = work("e", LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION);
		long nested = locus("e").getInstrumentation().getNestedCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION);
		Result result = certificate("e");
		assertEquals(reads, work("e", LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION),
				"GC-6");
		assertEquals(nested, locus("e").getInstrumentation().getNestedCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION), "GC-6 nested");
		LocusExistenceStructure2D existence = result.getStructure();
		assertEquals(Completeness.COMPLETE, existence.getCompleteness(), existence.toString());
		assertFalse(existence.isClosedPeriodic());
		ContinuousComponent arc = singleComponent(existence);
		assertOpenArc(arc, 0, true, "start", "end");
		assertEquals(Maximality.ESTABLISHED, arc.getMaximality());
		assertEquals(0, arc.getArcCoordinate(0), "the origin starts the arc");
		assertEquals(1, existence.getCoverage().size());
		assertEquals(CoverageState.CERTIFIED_EXISTS,
				existence.getCoverage().get(0).getState());
		// The slice copy derives the same structure from its own source certificate.
		LocusMetricFunctionStructure2D copy = copyStructure("e");
		assertEquals(pieces(structure), pieces(copy));
		OriginWrap copyWrap = singleWrap(copy);
		assertEquals(wrap.getDriverParameter(), copyWrap.getDriverParameter());
		assertEquals(wrap.getJumpLowerBound(), copyWrap.getJumpLowerBound());
		assertEquals(wrap.getJumpUpperBound(), copyWrap.getJumpUpperBound());
	}

	/**
	 * V-E2-8 and V-E2-6 (backward, structure parts): the ends of the cut share the address of
	 * the origin and are distinct; the direction and the same-position policy decide which
	 * one includes it: the start with {@code zero-length} forward or {@code full-cycle}
	 * backward, the end otherwise. The scalar length has only the default policy, so the
	 * dependent program is certified with the structure of the explicit-policy length.
	 */
	@Test
	void theIncludedEndFollowsDirectionAndSamePosition() {
		String[][] cases = {{"\"reverse\",\"strict\",\"zero-length\"", "false"},
			{"\"forward\",\"strict\",\"full-cycle\"", "false"},
			{"\"reverse\",\"strict\",\"full-cycle\"", "true"},
			{"\"forward\",\"wrap-to-start\",\"zero-length\"", "true"}};
		LocusInterval2D domain = new LocusInterval2D(-Math.PI, Math.PI, true, false);
		for (String[] policy : cases) {
			closedSource("0", "," + policy[0]);
			boolean startIncluded = Boolean.parseBoolean(policy[1]);
			LocusMetricFunctionStructure2D structure = structure("w");
			OriginWrap wrap = singleWrap(structure);
			assertEquals(startIncluded, wrap.isStartIncluded(), policy[0]);
			ConstructionBoxModel2D model = model("e");
			LocusExistenceStructure2D existence = HybridExistenceCertifier2D.certify(model,
					Map.of(model.metricInputs().get(0), structure), domain, true, BRANCH, null,
					Policy.DEFAULT, null).getStructure();
			assertEquals(Completeness.COMPLETE, existence.getCompleteness(), policy[0]);
			ContinuousComponent arc = singleComponent(existence);
			assertOpenArc(arc, 0, startIncluded, "start", "end");
			assertEquals(startIncluded ? 0 : 2 * Math.PI, arc.getArcCoordinate(0), 1E-15,
					policy[0]);
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * V-E2-6 (origin at the seam): the cut is the seam itself; the dependent locus is the
	 * full-length open arc that starts at {@code -pi}, which is not periodic.
	 */
	@Test
	void anOriginAtTheSeamCutsAtTheSeam() {
		closedSource("-pi", "");
		OriginWrap wrap = singleWrap(structure("w"));
		assertEquals(-Math.PI, wrap.getDriverParameter());
		LocusExistenceStructure2D existence = certificate("e").getStructure();
		assertEquals(Completeness.COMPLETE, existence.getCompleteness(), existence.toString());
		assertFalse(existence.isClosedPeriodic());
		ContinuousComponent arc = singleComponent(existence);
		assertOpenArc(arc, -Math.PI, true, "start", "end");
		assertEquals(0, arc.getArcCoordinate(-Math.PI));
		assertEquals(1, existence.getCoverage().size());
		assertEquals(new LocusInterval2D(-Math.PI, Math.PI, true, false),
				existence.getCoverage().get(0).getInterval());
	}

	/**
	 * V-E2-6 (origin that depends on the driver): no structural event; the length has no
	 * structure and the dependent locus is not certified, never certified nonexistent.
	 */
	@Test
	void anOriginThatFollowsTheDriverHasNoStructure() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("K=N+(1,0)");
		add("o=LocusV2(K,N)");
		add("A=Point(o,\"" + BRANCH + "\",x(N)/2)");
		add("w=LocusLength(o,A,K)");
		add("d=Length(o,A,K)");
		add("R=N-d*(1,0)");
		add("e=LocusV2(R,N)");
		LocusMetricFunctionStructure2D structure = structure("w");
		assertFalse(structure.isCertified());
		assertEquals("origin-not-a-fixed-semantic-position", structure.getEvidence());
		Result result = certificate("e");
		assertEquals(Completeness.NOT_ESTABLISHED, result.getStructure().getCompleteness());
		assertNoNonexistence(result.getStructure());
	}

	/**
	 * V-E2-6 (non-periodic source): on an interval source there is no wrap. A strict
	 * {@code zero-length} route runs forward from the origin to the end of the source; before
	 * the origin it is unreachable, which is certified nonexistence of the dependent locus
	 * through its dependency. The dependent locus is one interval that starts exactly at the
	 * origin, closed there, with a structural end; the cover is complete.
	 */
	@Test
	void anIntervalSourceDefinesTheRouteFromTheOriginToItsEnd() {
		add("s=Segment((0,0),(4,0))");
		add("M=Point(s)");
		moveDriver((GeoPoint) lookup("M"), 0.5);
		add("K=M+(0,1)");
		add("o=LocusV2(K,M)");
		add("A=Point(o,\"" + BRANCH + "\",1/4)");
		add("w=LocusLength(o,A,K)");
		add("d=Length(o,A,K)");
		add("R=M+d*(1,1)");
		add("e=LocusV2(R,M)");
		LocusMetricFunctionStructure2D structure = structure("w");
		assertTrue(structure.isCertified(), structure.toString());
		assertTrue(structure.getEvents().isEmpty());
		assertEquals(2, structure.getPieces().size(), structure.toString());
		assertEquals(LocusMetricFunctionStructure2D.State.UNDEFINED,
				structure.getPieces().get(0).getState());
		assertEquals(new LocusInterval2D(0.25, 1, true, true),
				structure.getPieces().get(1).getInterval());
		assertEquals(LocusMetricFunctionStructure2D.State.DEFINED,
				structure.getPieces().get(1).getState());

		LocusExistenceStructure2D existence = certificate("e").getStructure();
		assertEquals(Completeness.COMPLETE, existence.getCompleteness(), existence.toString());
		ContinuousComponent interval = singleComponent(existence);
		assertFalse(interval.isArc());
		assertEquals(new LocusInterval2D(0.25, 1, true, true), interval.getInterval());
		LocusBoundary2D start = interval.getLowerBoundary();
		assertEquals(LocusBoundary2D.Kind.STRUCTURAL_EVENT, start.getKind());
		assertEquals("ROUTE_DOMAIN", start.getSource().getPredicate());
		assertTrue(start.getSource().getOrigin().endsWith("-METRIC_INPUT"));
		assertTrue(start.isIncluded());
		assertEquals(LocusBoundary2D.Kind.DOMAIN_END, interval.getUpperBoundary().getKind());
		assertEquals(Maximality.ESTABLISHED, interval.getMaximality());
		Coverage before = coverageAt(existence, 0.2);
		assertEquals(new LocusInterval2D(0, 0.25, true, false), before.getInterval());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, before.getState());
		assertEquals(LocusCoverageCause2D.DEPENDENCY_CERTIFIED_NONEXISTENT, before.getCause());
	}

	/**
	 * V-E2-6 (non-periodic arc and locally bounded source, structure part): on the bite arc of
	 * the synthetic critical configuration, which crosses the seam, the strict forward route
	 * from the origin runs across the seam to the far end of the arc; the enclosure of the
	 * far root carries the certified boundary of the source, and the near part of the arc,
	 * the near root and the gap are unreachable. The live length of this source still reads
	 * the published compatibility structure (B07, step T11), so no dependent locus is built.
	 */
	@Test
	void anOpenSourceArcDefinesTheRouteAcrossTheSeamToItsFarEnd() {
		bite();
		LocusExistenceStructure2D source = certificate("lq").getStructure();
		assertEquals(Completeness.COMPLETE, source.getCompleteness());
		ContinuousComponent sourceArc = singleComponent(source);
		assertTrue(sourceArc.crossesSeam());
		LocusMetricFunctionStructure2D structure = structure("w");
		assertTrue(structure.isCertified(), structure.toString());
		assertTrue(structure.getEvents().isEmpty());
		List<String> states = new ArrayList<>();
		for (Piece piece : structure.getPieces()) {
			states.add(piece.getState().name());
		}
		assertEquals(List.of("DEFINED", "BOUNDARY", "UNDEFINED", "UNDEFINED", "UNDEFINED",
				"DEFINED"), states, structure.toString());
		assertEquals(new LocusInterval2D(-Math.PI, sourceArc.getEnd(), true, true),
				structure.getPieces().get(0).getInterval());
		assertEquals(new LocusInterval2D(2, Math.PI, true, false),
				structure.getPieces().get(5).getInterval());
		Piece boundary = structure.getPieces().get(1);
		assertSame(sourceArc.getUpperBoundary(), boundary.getBoundary());
		assertTrue(boundary.isDefinedBelow());
	}

	/**
	 * V-E2-6 (source only locally certified): the unlabelled second output of the exact
	 * critical configuration is certified only around its band. There is no wrap; the route
	 * from the origin is defined up to the edge of the band. Past that edge the source may
	 * continue, so the band and everything the route would reach through it, the part of the
	 * arc behind the origin included, stay unresolved: nothing is certified unreachable.
	 */
	@Test
	void aLocallyCertifiedSourceKeepsItsBandUnresolved() {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40)");
		add("N=Point(g1)");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		getKernel().updateConstruction(false);
		add("A=Point(lr2,\"" + BRANCH + "\",2)");
		add("w=LocusLength(lr2,A,R2)");
		LocusExistenceStructure2D source = certificate("lr2").getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, source.getCompleteness());
		ContinuousComponent sourceArc = singleComponent(source);
		LocusMetricFunctionStructure2D structure = structure("w");
		assertTrue(structure.isCertified(), structure.toString());
		assertTrue(structure.getEvents().isEmpty());
		List<Piece> pieces = structure.getPieces();
		assertEquals(new LocusInterval2D(-Math.PI, sourceArc.getEnd(), true, true),
				pieces.get(0).getInterval());
		assertEquals(LocusMetricFunctionStructure2D.State.DEFINED, pieces.get(0).getState());
		Piece last = pieces.get(pieces.size() - 1);
		assertEquals(new LocusInterval2D(2, Math.PI, true, false), last.getInterval());
		assertEquals(LocusMetricFunctionStructure2D.State.DEFINED, last.getState());
		double critical = Math.atan2(4, 3);
		assertEquals(LocusMetricFunctionStructure2D.State.UNRESOLVED,
				structure.pieceContaining(critical).getState());
		assertEquals(LocusMetricFunctionStructure2D.State.UNRESOLVED,
				structure.pieceContaining(1.5).getState());
		for (Piece piece : pieces.subList(1, pieces.size() - 1)) {
			assertEquals(LocusMetricFunctionStructure2D.State.UNRESOLVED, piece.getState(),
					structure.toString());
		}
	}

	/**
	 * V-E2-6 (degenerate source) and V-UND-1: a jump that no chord certifies leaves a band at
	 * the origin, whose two ends keep their inclusion; a structure that fails computationally
	 * leaves the whole domain unresolved with its typed cause, never nonexistent. A source of
	 * length zero is not rectifiable by a positive bound and has no structure.
	 */
	@Test
	void anUnestablishedJumpOrStructureLeavesTheDependentUnresolved() {
		closedSource("0", "");
		ConstructionBoxModel2D model = model("e");
		int node = model.metricInputs().get(0);
		LocusInterval2D domain = new LocusInterval2D(-Math.PI, Math.PI, true, false);
		LocusMetricFunctionStructure2D unbounded = LocusMetricFunctionStructure2D.certified(
				List.of(new Piece(domain, LocusMetricFunctionStructure2D.State.DEFINED, null,
						false)), List.of(new OriginWrap(0, TraversalDirection.FORWARD,
								SamePositionPolicy.ZERO_LENGTH, Double.NaN, 100, "test")), "test");
		LocusExistenceStructure2D band = HybridExistenceCertifier2D.certify(model,
				Map.of(node, unbounded), domain, true, BRANCH, null, Policy.DEFAULT, null)
				.getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, band.getCompleteness());
		ContinuousComponent arc = singleComponent(band);
		assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE, arc.getLowerBoundary().getKind());
		assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE, arc.getUpperBoundary().getKind());
		assertTrue(arc.getLowerBoundary().isIncluded());
		assertFalse(arc.getUpperBoundary().isIncluded());
		assertEquals(Maximality.LOCAL_CERTIFIED_SUBINTERVAL, arc.getMaximality());

		LocusExistenceStructure2D failed = HybridExistenceCertifier2D.certify(model,
				Map.of(node, LocusMetricFunctionStructure2D.notEstablished(
						LocusCoverageCause2D.COMPUTATIONAL_DEPENDENCY_FAILURE, "test")), domain,
				true, BRANCH, null, Policy.DEFAULT, null).getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, failed.getCompleteness());
		assertTrue(failed.getContinuousValidComponents().isEmpty());
		for (Coverage coverage : failed.getCoverage()) {
			assertEquals(CoverageState.UNRESOLVED, coverage.getState());
			assertEquals(LocusCoverageCause2D.COMPUTATIONAL_DEPENDENCY_FAILURE,
					coverage.getCause());
		}
		getKernel().clearConstruction(true);

		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("K=Dilate(N,0,(1,1))");
		add("o=LocusV2(K,N)");
		add("A=Point(o,\"" + BRANCH + "\",0)");
		add("w=LocusLength(o,A,K)");
		LocusMetricFunctionStructure2D zero = structure("w");
		assertFalse(zero.isCertified());
		assertTrue(zero.getEvidence().startsWith("rectifiability-not-established"),
				zero.getEvidence());
	}

	/**
	 * V-E2-6 (explicit route between fixed positions) and section 26.3: a length between two
	 * fixed positions does not depend on the driver and has no structure; its value is an
	 * estimate, never a rigorous constant, so a locus that reads it has no program.
	 */
	@Test
	void aLengthBetweenFixedPositionsIsNeverACertificateInput() {
		closedSource("0", "");
		add("B=Point(o,\"" + BRANCH + "\",1)");
		add("f=LocusLength(o,A,B)");
		LocusMetricFunctionStructure2D structure = structure("f");
		assertFalse(structure.isCertified());
		assertEquals("target-not-the-generator-occurrence", structure.getEvidence());
		add("df=Length(o,A,B)");
		add("Rf=N-df*(1,0)");
		add("ef=LocusV2(Rf,N)");
		ReconstructibleLocusEvaluator2D evaluator = evaluator("ef");
		assertFalse(evaluator.captureCertifiedProgramV3().isPresent());
		assertEquals("metric-value-constant", evaluator.getProgramV3Refusal());
		Result result = certificate("ef");
		assertEquals(Completeness.NOT_ESTABLISHED, result.getStructure().getCompleteness());
		assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
				result.getStructure().getCoverage().get(0).getCause());
	}

	/**
	 * A metric input is never invariant under a reflection of the driver parameter: a length
	 * from a fixed origin to a folding source point grows on both sides of the fold, so the
	 * dependent locus has no certified stationary point although its other steps are
	 * symmetric.
	 */
	@Test
	void aMetricInputIsNeverSymmetric() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("lx=LocusV2(X,N)");
		assertEquals(2, certificate("lx").getStationaryPoints().size());
		add("A=Point(lx,\"" + BRANCH + "\",0)");
		add("d=Length(lx,A,X)");
		add("R=X+d*(1,0)");
		add("e=LocusV2(R,N)");
		Result result = certificate("e");
		assertEquals(Completeness.COMPLETE, result.getStructure().getCompleteness(),
				result.getStructure().toString());
		assertTrue(result.getStationaryPoints().isEmpty(),
				result.getStationaryPoints().toString());
	}

	/**
	 * The structure is computed once per revision of the source: repeated queries and
	 * driver moves return the same certificate; a new origin address derives a new one.
	 */
	@Test
	void theStructureIsComputedOncePerRevision() {
		closedSource("0", "");
		LocusMetricFunctionStructure2D first = structure("w");
		assertSame(first, structure("w"));
		moveDriver((GeoPoint) lookup("N"), 1);
		assertSame(first, structure("w"));
		lookup("v").setUndefined();
		add("SetValue(v,1/2)");
		LocusMetricFunctionStructure2D moved = structure("w");
		assertNotEquals(first.getSignature(), moved.getSignature());
		assertEquals(0.5, singleWrap(moved).getDriverParameter());
	}

	/**
	 * Closed source circle {@code o} of radius 2 centred at (1, 0), traced by {@code K =
	 * N + (1, 0)}; origin {@code A} at the given parameter; length {@code w} from {@code A} to
	 * the generator {@code K} with the given policy; dependent locus {@code e} of {@code R =
	 * N - d (1, 0)} with the scalar default-policy length {@code d}, like {@code e_2}.
	 */
	private void closedSource(String origin, String policy) {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("K=N+(1,0)");
		add("o=LocusV2(K,N)");
		add("v=" + origin);
		add("A=Point(o,\"" + BRANCH + "\",v)");
		add("w=LocusLength(o,A,K" + policy + ")");
		add("d=Length(o,A,K)");
		add("R=N-d*(1,0)");
		add("e=LocusV2(R,N)");
	}

	/**
	 * The bite of the synthetic critical configuration ({@code 40 + 2^-10}): {@code lq} is one
	 * certified arc that crosses the seam; the origin is at parameter 2.
	 */
	private void bite() {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40+2^(-10))");
		add("N=Point(g1)");
		moveDriver((GeoPoint) lookup("N"), 2.5);
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "Q2"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
		add("A=Point(lq,\"" + BRANCH + "\",2)");
		add("w=LocusLength(lq,A,Q)");
	}

	private static void assertOpenArc(ContinuousComponent arc, double origin,
			boolean startIncluded, String startSide, String endSide) {
		assertTrue(arc.isArc());
		assertTrue(arc.isFullLength());
		assertFalse(arc.isClosedPeriodic());
		assertEquals(origin, arc.getStart());
		assertEquals(origin, arc.getEnd());
		LocusBoundary2D start = arc.getLowerBoundary();
		LocusBoundary2D end = arc.getUpperBoundary();
		for (LocusBoundary2D boundary : List.of(start, end)) {
			assertEquals(LocusBoundary2D.Kind.STRUCTURAL_EVENT, boundary.getKind());
			assertEquals("ORIGIN_WRAP", boundary.getSource().getPredicate());
			assertTrue(boundary.getSource().getOrigin().endsWith("-METRIC_INPUT"));
			assertEquals(LocusEvidenceNature2D.EXACT, boundary.getNature());
		}
		assertEquals(startSide, start.getSource().getSide());
		assertEquals(endSide, end.getSource().getSide());
		assertNotEquals(start.getSource(), end.getSource());
		assertEquals(startIncluded, start.isIncluded());
		assertEquals(!startIncluded, end.isIncluded());
	}

	private static void assertNoNonexistence(LocusExistenceStructure2D structure) {
		for (Coverage coverage : structure.getCoverage()) {
			assertNotEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, coverage.getState(),
					coverage.toString());
		}
	}

	private static ContinuousComponent singleComponent(LocusExistenceStructure2D structure) {
		assertEquals(1, structure.getContinuousValidComponents().size(), structure.toString());
		return structure.getContinuousValidComponents().get(0);
	}

	private static OriginWrap singleWrap(LocusMetricFunctionStructure2D structure) {
		assertTrue(structure.isCertified(), structure.toString());
		assertEquals(1, structure.getEvents().size(), structure.toString());
		return structure.getEvents().get(0);
	}

	private static List<String> pieces(LocusMetricFunctionStructure2D structure) {
		List<String> result = new ArrayList<>();
		for (Piece piece : structure.getPieces()) {
			result.add(piece.getSignature());
		}
		return result;
	}

	private static Coverage coverageAt(LocusExistenceStructure2D structure, double parameter) {
		for (Coverage coverage : structure.getCoverage()) {
			if (coverage.getInterval().contains(parameter, 0)) {
				return coverage;
			}
		}
		throw new AssertionError("No coverage at " + parameter + " in " + structure);
	}

	private static void moveDriver(GeoPoint driver, double parameter) {
		driver.getPathParameter().setT(parameter);
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private AlgoLocusBetweenMetricV2 metric(String label) {
		return (AlgoLocusBetweenMetricV2) assertInstanceOf(GeoLocusMetricResult.class,
				lookup(label), label).getParentAlgorithm();
	}

	private LocusMetricFunctionStructure2D structure(String label) {
		return metric(label).getFunctionStructure();
	}

	private ReconstructibleLocusEvaluator2D evaluator(String label) {
		return (ReconstructibleLocusEvaluator2D) locus(label).getSemanticDefinition()
				.getEvaluatorCapability();
	}

	private LocusMetricFunctionStructure2D copyStructure(String label) {
		Map<Integer, LocusMetricFunctionStructure2D> structures =
				evaluator(label).getMetricStructuresV3();
		assertEquals(1, structures.size(), structures.toString());
		return structures.values().iterator().next();
	}

	private ConstructionBoxModel2D model(String label) {
		return new ConstructionBoxModel2D(evaluator(label).captureCertifiedProgramV3()
				.orElseThrow());
	}

	private Result certificate(String label) {
		Result result = ((AlgoDependentPointLocusV2) locus(label).getParentAlgorithm())
				.getHybridExistenceCertificate();
		assertNotNull(result, label);
		assertNull(result.getDiagnostic(), result.getDiagnostic());
		return result;
	}

	private long work(String label, LocusCertificationWork2D kind) {
		return locus(label).getInstrumentation().getCertificationWork(kind);
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}
}

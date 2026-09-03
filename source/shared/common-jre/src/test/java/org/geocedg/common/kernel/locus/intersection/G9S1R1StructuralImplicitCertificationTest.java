/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.SplineImplicitIntervalCertification2D.Status;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;
import org.junit.jupiter.api.Test;

/** Native structural one-sided certification, distinct from pair-sheet identity. */
final class G9S1R1StructuralImplicitCertificationTest extends BaseUnitTest {

	private static final LocusInterval2D DOMAIN = new LocusInterval2D(0, 1, true, true);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void simpleImplicitRootHasStructuralExistenceAndUniqueMaterialization() {
		line();
		GeoLocusIntersectionResult result = query("S", "x+0.25+y");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertSimple(result, 1);
		GeoPoint point = materialize(result, roots(result).get(0));
		assertTrue(point.isDefined());
		assertEquals(-0.25, point.getInhomX(), 1E-10);
	}

	@Test
	void separatedSimpleRootsPreserveBothCurrentTokens() {
		line();
		GeoLocusIntersectionResult result = query("S", "x^2-1+y");
		assertEquals(2, roots(result).size(), diagnostic(result));
		assertSimple(result, 2);
		assertEquals(2, roots(result).stream().map(root -> root.getIdentity()
				.getRootToken()).distinct().count());
	}

	@Test
	void nonzeroTinyDerivativeUsesIntervalSignInsteadOfTangencyEpsilon() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, new double[][] {{0}, {0x1p-50}});
		var proof = certificate.verify(DOMAIN, 0.5, 0.5, 0.5);
		assertEquals(Status.SIMPLE, proof.status, certificate.workSummary());
		assertFalse(proof.derivative.containsZero());
		assertTrue(proof.derivative.upper < 1E-12);
		GeoLocusIntersectionResult result = query("S", "x/1125899906842624+y");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertSimple(result, 1);
	}

	@Test
	void exactTangencyRemainsOneEstimatedRichContactWithoutToken() {
		line();
		GeoLocusIntersectionResult result = query("S", "x^2+y");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertRichContactsOnly(result);
	}

	@Test
	void singularImplicitContactRemainsRichWithoutInventedMultiplicity() {
		// Exact historical four-point input: its approximate discovery seed has
		// a nonzero evaluated gradient. Exact singular target evaluations remain
		// outside the existing regular-target residual/membership contract.
		add("S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)");
		GeoLocusIntersectionResult result = query("S", "x^3+y^3");
		assertEquals(1, roots(result).size(), diagnostic(result));
		LocusIntersectionSolution2D root = roots(result).get(0);
		assertEquals(ContactClass.CONTACT_UNDETERMINED,
				root.getClassification().getContactClass());
		assertEquals(MultiplicityStatus.NOT_ESTABLISHED,
				root.getClassification().getMultiplicityStatus());
		assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
				root.getRevisionEvidence().getLocalIsolationStatus());
		assertFalse(result.isPointAdmissible(root.getIdentity().getRootToken()));
	}

	@Test
	void stationaryDiscoveryProvenanceDoesNotCertifyMultiplicity() {
		var recorder = new PolynomialRootIsolation2D.WorkRecorder() {
			@Override
			public void recordIsolationSubdivision(int depth) {
			}

			@Override
			public void recordRefinementStarted() {
			}

			@Override
			public void recordRefinementIteration(long iteration) {
			}
		};
		var stationary = PolynomialRootIsolation2D.isolate(new double[] {0, 0, 1},
				-1, 1, 1E-12, 80, recorder);
		assertEquals(1, stationary.getCells().size());
		assertTrue(stationary.getCells().get(0).isStationaryProposal());
		var simple = PolynomialRootIsolation2D.isolate(new double[] {-0.25, 1},
				-1, 1, 1E-12, 80, recorder);
		assertEquals(1, simple.getCells().size());
		assertFalse(simple.getCells().get(0).isStationaryProposal());
	}

	@Test
	void exactBoundaryContactIsSeparateFromStationaryDiscoveryAndMultiplicity() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, new double[][] {{0, 1}, {0}, {1}});
		assertTrue(certificate.hasExactBoundaryZero(0.5));
		assertFalse(certificate.hasExactBoundaryZero(Math.nextUp(0.5)));
		var proof = certificate.verify(DOMAIN, 0.5, 0.5, 0.5);
		assertEquals(Status.UNRESOLVED, proof.status);
		assertTrue(proof.derivative.containsZero());
		var nonzero = certificate(source, new double[][] {{0x1p-80, 1}, {0}, {1}});
		assertFalse(nonzero.hasExactBoundaryZero(0.5));
		GeoLocusV2 translated = add("U=Translate(S,Vector((0,0),(0,1)))");
		assertFalse(certificate(translated, new double[][] {{-1, 1}, {0}, {1}})
				.hasExactBoundaryZero(0.5));
	}

	@Test
	void threeSimpleRootsRemainDistinctWithoutCoordinateZeroSnapping() {
		line();
		GeoLocusIntersectionResult result = query("S", "(x+1)*x*(x-1)+y");
		assertEquals(3, roots(result).size(), diagnostic(result));
		assertSimple(result, 3);
		assertEquals(3, roots(result).stream().map(root -> root.getIdentity()
				.getRootToken()).distinct().count());
		for (LocusIntersectionSolution2D root : roots(result)) {
			double x = root.getEvaluatedPoint().getX();
			assertTrue(Math.abs((x + 1) * x * (x - 1)) < 1E-10);
		}
	}

	@Test
	void squaredTargetRetainsTwoRichContactsAndNoFalseTransverseRoot() {
		line();
		GeoLocusIntersectionResult result = query("S", "(x^2-1)^2+y");
		assertEquals(2, roots(result).size(), diagnostic(result));
		assertRichContactsOnly(result);
	}

	@Test
	void floatingArtificialSplitCannotAcquireSimpleRootCertificate() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, squareTarget());
		for (double parameter : new double[] {0.25 - 0x1p-28, 0.25 + 0x1p-28,
				0.75 - 0x1p-28, 0.75 + 0x1p-28}) {
			var proof = certificate.verify(DOMAIN, parameter, parameter, parameter);
			assertTrue(proof.status != Status.SIMPLE, certificate.workSummary());
		}
		System.out.println(certificate.workSummary());
	}

	@Test
	void veryCloseDistinctSimpleRootsAreNotMergedByParameterTolerance() {
		line();
		GeoLocusIntersectionResult result = query("S", "x^2-1/1048576+y");
		assertEquals(2, roots(result).size(), diagnostic(result));
		assertSimple(result, 2);
		assertTrue(roots(result).get(0).getEvaluatedPoint().getX()
				* roots(result).get(1).getEvaluatedPoint().getX() < 0);
	}

	@Test
	void certifiedRootsBelowLegacyDeduplicationToleranceRemainDistinct() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, new double[][] {{-0x1p-78, 1}, {0}, {1}});
		// Exact analytic seeds test certification/publication, not completeness
		// of the floating discovery expansion, which may lose this tiny constant.
		double first = 0.5 - 0x1p-41;
		double second = 0.5 + 0x1p-41;
		var left = certificate.verify(DOMAIN, first, first, first);
		var right = certificate.verify(DOMAIN, second, second, second);
		assertEquals(Status.SIMPLE, left.status, certificate.workSummary());
		assertEquals(Status.SIMPLE, right.status, certificate.workSummary());
		assertFalse(left.sameCertifiedRoot(right));
		assertTrue(second - first < LocusIntersectionPolicy2D.DEFAULT_DEDUPLICATION_TOLERANCE);
		var policy = LocusIntersectionPolicy2D.initial("certificate-test", "normalized-u");
		GeoImplicitCurve target = add("I=ImplicitCurve(x^2-2^(-78)+y)");
		var captured = LocusIntersectionTargets2D.capture(target, "test-target", 0);
		var query = new LocusIntersectionQuery2D("test-pair", "test-lineage", "test-source",
				source.getSemanticRevision(), "test-target", 0, "test-context", policy);
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			var context = new IntersectionCapabilityContext2D(query, source.getSemanticDefinition(),
					captured, session,
					new LocusIntersectionInstrumentation2D(policy.getWorkBudget()));
			var a = candidate(left);
			var b = candidate(right);
			assertEquals(2, LocusIntersectionSolver2D.deduplicate(context, List.of(a, b)).size());
			assertEquals(2, LocusIntersectionSolver2D.deduplicate(context, List.of(b, a)).size());
		}
	}

	@Test
	void derivativeIntervalContainingZeroCannotProveTransversality() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, new double[][] {{0}, {0}, {1}});
		assertTrue(certificate.derivative(new SplineOutwardInterval2D(0.49, 0.51))
				.containsZero());
		assertEquals(Status.UNRESOLVED,
				certificate.verify(DOMAIN, 0.5, 0.49, 0.51).status);
	}

	@Test
	void structuralValueIntervalExcludesAFalseCandidate() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, new double[][] {{0}, {1}});
		assertFalse(certificate.value(new SplineOutwardInterval2D(0.8, 0.9))
				.containsZero());
		assertEquals(Status.EXCLUDED, certificate.verify(DOMAIN, 0.85, 0.84, 0.86).status);
		assertTrue(certificate.workSummary().contains("certifiedExclusions=1"));
	}

	@Test
	void boundedWorkExhaustionPublishesNoSimpleCertificate() {
		GeoLocusV2 source = line();
		var budget = new LocusIntersectionWorkBudget2D(100, 100, 100, 100,
				1, 1, 1, 100, 100, 100, 100, 0, 2);
		var certificate = SplineImplicitIntervalCertification2D.capture(
				source.getSemanticDefinition(), AlgoSplineV2.BRANCH_KEY,
				new double[][] {{-0x1p-20}, {0}, {1}}, budget);
		assertNotNull(certificate);
		assertEquals(Status.UNRESOLVED,
				certificate.verify(DOMAIN, 0.5, 0.5, 0.5).status);
		assertTrue(certificate.workSummary().contains("workLimitHits=1"),
				certificate.workSummary());
	}

	@Test
	void exactStructuralKnotHasOneCertifiedRootNotTwoSpanCandidates() {
		line();
		GeoLocusIntersectionResult result = query("S", "x+y");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertSimple(result, 1);
		assertEquals(0.5, roots(result).get(0).getRevisionEvidence().getSemanticParameter(), 0);
	}

	@Test
	void openSplineIncludedEndpointHasNoArtificialPeriodicChartBarrier() {
		GeoLocusV2 source = add("S=SplineV2({(0,0),(1,0),(2,0)},3)");
		var certificate = certificate(source, new double[][] {{0, 1}, {1}});
		var proof = certificate.verify(DOMAIN, 0, 0, 0);
		assertEquals(Status.SIMPLE, proof.status, certificate.workSummary());
		GeoLocusIntersectionResult result = query("S", "x+y");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertSimple(result, 1);
		assertEquals(0, roots(result).get(0).getRevisionEvidence().getSemanticParameter(), 0);
	}

	@Test
	void periodicSeamHasOneCanonicalRootAndNoEndpointDuplicate() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		add("S=SplineV2({A,B,C,D,A},3)");
		GeoLocusIntersectionResult result = query("S", "y*(1+x^2)");
		assertEquals(2, roots(result).size(), diagnostic(result));
		assertSimple(result, 2);
		assertEquals(1, roots(result).stream().filter(root ->
				root.getRevisionEvidence().getSemanticParameter() == 0).count());
		assertTrue(roots(result).stream().allMatch(root ->
				root.getRevisionEvidence().getSemanticParameter() < 1));
	}

	@Test
	void liftedPeriodicProofUsesOutwardCanonicalEnclosureAndCertifiedDeduplication() {
		GeoLocusV2 source = add("S=SplineV2({(1,0),(0,1),(-1,0),(0,-1),(1,0)},3)");
		var certificate = certificate(source, new double[][] {{0, -1}, {1}});
		var canonical = certificate.verify(DOMAIN, 0.125, 0.125, 0.125);
		var lifted = certificate.verify(DOMAIN, 1.125, 1.125, 1.125);
		assertEquals(Status.SIMPLE, canonical.status, certificate.workSummary());
		assertEquals(Status.SIMPLE, lifted.status, certificate.workSummary());
		assertTrue(canonical.sameCertifiedRoot(lifted));
		assertTrue(lifted.canonicalRoot.lower <= lifted.parameter);
		assertTrue(lifted.parameter <= lifted.canonicalRoot.upper);
		assertTrue(lifted.canonicalRoot.width() > 0);
		assertTrue(lifted.root.lower > 1);
		assertTrue(lifted.canonicalRoot.upper < 1);
	}

	@Test
	void translatedSplineCertifiesOriginalCompositionNotExpandedSurrogate() {
		line();
		add("U=Translate(S,Vector((0,0),(1,2)))");
		GeoLocusIntersectionResult result = query("U", "x-1+y-2");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertSimple(result, 1);
		assertEquals(1, roots(result).get(0).getEvaluatedPoint().getX(), 1E-10);
		assertEquals(2, roots(result).get(0).getEvaluatedPoint().getY(), 1E-10);
	}

	@Test
	void negativeDilationRetainsCertifiedOneSidedRoot() {
		line();
		add("U=Dilate(S,-2,(0,0))");
		GeoLocusIntersectionResult result = query("U", "x-1+y");
		assertEquals(1, roots(result).size(), diagnostic(result));
		assertSimple(result, 1);
		assertEquals(1, materialize(result, roots(result).get(0)).getInhomX(), 1E-10);
	}

	@Test
	void arithmeticUncertaintyCannotTurnSquaredContactIntoSimpleRoot() {
		GeoLocusV2 source = line();
		var certificate = certificate(source, squareTarget());
		var proof = certificate.verify(DOMAIN, Math.nextUp(0.25), 0.25, Math.nextUp(0.25));
		assertEquals(Status.UNRESOLVED, proof.status, certificate.workSummary());
		assertTrue(proof.compatibleContact);
	}

	@Test
	void genericScalarLocusDoesNotAcquireTheStructuralSplineCapability() {
		add("s=0");
		add("P=(s,0)");
		add("D={false,{-2,2,true,true}}");
		GeoLocusV2 source = add("L=LocusV2(P,s,D)");
		assertNull(SplineImplicitIntervalCertification2D.capture(source.getSemanticDefinition(),
				source.getSemanticDefinition().getBranches().get(0).getBranchKey(),
				new double[][] {{0}, {1}}, LocusIntersectionWorkBudget2D.initial()));
	}

	private GeoLocusV2 line() {
		getKernel().setContinuous(false);
		add("A=(-2,0)");
		add("B=(0,0)");
		add("C=(2,0)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private static SplineImplicitIntervalCertification2D certificate(GeoLocusV2 source,
			double[][] target) {
		var result = SplineImplicitIntervalCertification2D.capture(source.getSemanticDefinition(),
				AlgoSplineV2.BRANCH_KEY, target, LocusIntersectionWorkBudget2D.initial());
		assertNotNull(result);
		return result;
	}

	private GeoLocusIntersectionResult query(String source, String expression) {
		add("I=ImplicitCurve(" + expression + ")");
		return add("R=Intersect(" + source + ",I)");
	}

	private static List<LocusIntersectionSolution2D> roots(GeoLocusIntersectionResult result) {
		return result.getIntersectionResult().getFiniteSolutions();
	}

	private static String diagnostic(GeoLocusIntersectionResult result) {
		return result.getIntersectionResult().getDiagnostics().toString();
	}

	private static void assertSimple(GeoLocusIntersectionResult result, int count) {
		assertEquals(count, roots(result).stream().filter(root ->
				result.isPointAdmissible(root.getIdentity().getRootToken())).count(),
				diagnostic(result));
		for (LocusIntersectionSolution2D root : roots(result)) {
			assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
					root.getClassification().getContactClass());
			assertEquals(LocalIsolationStatus.ESTABLISHED,
					root.getRevisionEvidence().getLocalIsolationStatus());
		}
	}

	private static void assertRichContactsOnly(GeoLocusIntersectionResult result) {
		for (LocusIntersectionSolution2D root : roots(result)) {
			assertEquals(ContactClass.TANGENT_ESTABLISHED,
					root.getClassification().getContactClass());
			assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
					root.getRevisionEvidence().getLocalIsolationStatus());
			assertFalse(result.isPointAdmissible(root.getIdentity().getRootToken()));
		}
	}

	private GeoPoint materialize(GeoLocusIntersectionResult result,
			LocusIntersectionSolution2D root) {
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(), "X", result,
				new GeoText(getConstruction(), root.getIdentity().getRootToken()));
	}

	private static double[][] squareTarget() {
		return new double[][] {{1, 1}, {0}, {-2}, {0}, {1}};
	}

	private static IntersectionCandidate2D candidate(
			SplineImplicitIntervalCertification2D.Proof proof) {
		return new IntersectionCandidate2D(AlgoSplineV2.BRANCH_KEY,
				IntersectionCapabilityContext2D.componentKey(AlgoSplineV2.BRANCH_KEY, 0),
				proof.parameter, OptionalDouble.empty(),
				new IntersectionParameterInterval2D(proof.root.lower, proof.root.upper),
				LocalIsolationStatus.ESTABLISHED, Optional.empty(),
				ContactClass.TRANSVERSE_ESTABLISHED, MultiplicityStatus.NOT_ESTABLISHED,
				OptionalInt.empty(), SolverMethod.CERTIFIED_INTERVAL,
				NumericGuarantee.ESTIMATED_ERROR, LineageEventKind.APPEARED, List.of(), List.of())
				.withStructuralCertificate(proof);
	}
}

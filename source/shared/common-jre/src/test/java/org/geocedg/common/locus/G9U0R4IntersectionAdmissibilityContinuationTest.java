/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionCapabilityContext2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCompletenessEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootIdentity2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootLineage2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootRevisionEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.IntersectionTokenLineage2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTargets2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.locus.intersection.LocusSemanticIntersectionToken2D;
import org.geocedg.common.kernel.locus.intersection.PublicIntersectionRootIdentityResolver2D;
import org.geocedg.common.kernel.locus.intersection.TargetResidual2D;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;
import org.junit.jupiter.api.Test;

/** G9U0-R4 author-fixture initial-admissibility regression. */
final class G9U0R4IntersectionAdmissibilityContinuationTest
		extends G9U0PublicSurfaceTestBase {
	private static final String FIXTURE = "/org/geocedg/common/locus/g9u0-r2/"
			+ "locusFromMidpoint.cedg";
	private static final String FIXTURE_SHA256 =
			"47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c";
	private static final String FOUR_SOLUTION_FIXTURE =
			"/org/geocedg/common/locus/g9u0-r4/fourSolutions.cedg";
	private static final String FOUR_SOLUTION_FIXTURE_SHA256 =
			"51dcf7a002cb3984bb4cf5843d50e100f4bc8ef91217d4502fa7987c5b1ec21c";

	@Test
	void authorFourSolutionFixtureUsesFourIntrinsicSemanticSelectors()
			throws Exception {
		loadFourSolutionFixture();
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) requireLookup("b");
		assertNotNull(rich);
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(SupportLevel.VERIFIED_UNCERTIFIED,
				result.getSupportLevel());
		assertEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				result.getNumericGuarantee());
		assertEquals(0, result.getWork().getUnresolvedCandidates());
		assertTrue(result.getUnresolvedCandidateComponentKeys().isEmpty());
		assertEquals(4, result.getFiniteSolutions().size(),
				describeFourSolutionEvidence(rich));
		assertEquals(1, result.getFiniteSolutions().stream()
				.map(solution -> solution.getRevisionEvidence()
						.getResolvedValidComponentKey())
				.distinct().count(), describeFourSolutionEvidence(rich));

		Map<String, Long> rootsByGerm = result.getFiniteSolutions().stream()
				.collect(Collectors.groupingBy(solution -> solution
						.getRevisionEvidence().getCurrentRootGerm().orElseThrow(),
						LinkedHashMap::new, Collectors.counting()));
		assertEquals(2, rootsByGerm.size(), describeFourSolutionEvidence(rich));
		assertTrue(rootsByGerm.values().stream().allMatch(count -> count == 2),
				describeFourSolutionEvidence(rich));
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			assertEquals(LocalIsolationStatus.ESTABLISHED,
					solution.getRevisionEvidence().getLocalIsolationStatus());
			assertEquals(NumericGuarantee.ESTIMATED_ERROR,
					solution.getRevisionEvidence().getNumericGuarantee());
			assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
					solution.getClassification().getContactClass());
			assertTrue(solution.getIdentity().getIdentityStatus()
						== IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
					|| solution.getIdentity().getIdentityStatus()
						== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED);
			assertTrue(solution.getIdentity().getExplicitContinuationKey()
					.isPresent());
			assertTrue(solution.getIdentity().getExplicitContinuationKey()
					.orElseThrow().contains(
							"g9u0-r4/deterministic-current-root/v2/"));
			assertEquals(solution.getIdentity().getIdentityStatus()
						== IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
							? LineageEventKind.APPEARED
							: LineageEventKind.UNCHANGED,
					solution.getLineage().getEventKind());
			if (solution.getIdentity().getIdentityStatus()
					== IdentityStatus.NEW_TOPOLOGICAL_SOLUTION) {
				assertFalse(solution.getLineage().isContinuationEstablished());
			}
			assertTrue(rich.isPointAdmissible(
					solution.getIdentity().getRootToken()));
		}
		assertEquals(4, tokens(rich).size(), describeFourSolutionEvidence(rich));
		GeoLocusV2 locus = (GeoLocusV2) requireLookup("a");
		for (int index = 0; index < result.getFiniteSolutions().size(); index++) {
			LocusIntersectionSolution2D solution =
					result.getFiniteSolutions().get(index);
			assertTrue(materialize("fourPoint" + index, rich, solution).isDefined());
			assertAuthorFixtureIncidence(locus, solution);
		}
	}

	@Test
	void authorFourSolutionSelectorsIgnoreEverySolverPermutation()
			throws Exception {
		loadFourSolutionFixture();
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) requireLookup("b");
		AlgoLocusIntersectionV2 algorithm =
				(AlgoLocusIntersectionV2) rich.getParentAlgorithm();
		LocusIntersectionResult2D candidate = rich.getIntersectionResult();
		Map<String, String> expected = rootTokenMap(resolveDetached(algorithm,
				candidate, "r4-order-owner"));
		assertEquals(4, expected.size());
		List<List<LocusIntersectionSolution2D>> permutations =
				permutations(candidate.getFiniteSolutions());
		assertEquals(24, permutations.size());
		for (List<LocusIntersectionSolution2D> permutation : permutations) {
			LocusIntersectionResult2D permuted = withCompleteness(candidate,
					candidate.getCompletenessEvidence().getCompleteness(),
					permutation);
			assertEquals(expected, rootTokenMap(resolveDetached(algorithm,
					permuted, "r4-order-owner")));
		}
	}

	@Test
	void authorFourSolutionBindingsArePathIndependentAndMoveRegularly()
			throws Exception {
		loadFourSolutionFixture();
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) requireLookup("b");
		List<LocusIntersectionSolution2D> roots = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(4, roots.size());
		for (int index = 0; index < roots.size(); index++) {
			assertTrue(materialize("fourPathPoint" + index, rich,
					roots.get(index)).isDefined());
		}
		final String byteIdenticalSeed = getApp().getXML();
		PathSnapshot expected = runFourSolutionPath(byteIdenticalSeed,
				MotionPath.DIRECT);
		for (MotionPath path : MotionPath.values()) {
			assertEquals(expected, runFourSolutionPath(byteIdenticalSeed, path),
					path.name());
		}
		assertEquals(4, expected.bindings().size());
		assertTrue(expected.bindings().values().stream().allMatch(binding ->
				binding.defined() && binding.pointAdmissible()
						&& binding.identityStatus()
								== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED));
	}

	@Test
	void authorFourSolutionPointsRemainDefinedThroughoutRegularMotion()
			throws Exception {
		loadFourSolutionFixture();
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) requireLookup("b");
		Map<String, String> pointTokens = new LinkedHashMap<>();
		List<LocusIntersectionSolution2D> roots = rich.getIntersectionResult()
				.getFiniteSolutions();
		for (int index = 0; index < roots.size(); index++) {
			String label = "fourMotionPoint" + index;
			pointTokens.put(label, roots.get(index).getIdentity().getRootToken());
			assertTrue(materialize(label, rich, roots.get(index)).isDefined());
		}
		Set<String> exactTokens = Set.copyOf(pointTokens.values());
		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		double initialX = radiusPoint.getInhomX();
		double y = radiusPoint.getInhomY();
		for (int step = 1; step <= 64; step++) {
			radiusPoint.setCoords(initialX + 0.02 * step / 64, y, 1);
			radiusPoint.updateCascade();
			assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size(),
					"step " + step + ": " + describeFourSolutionEvidence(rich));
			assertEquals(exactTokens, tokens(rich), "step " + step);
			assertTokenPoints(pointTokens, rich);
		}
	}

	@Test
	void authorMidpointCircleRootsAreInitiallyPointAdmissible() throws Exception {
		loadAuthorFixture();
		GeoLocusV2 locus = (GeoLocusV2) requireLookup("a");
		GeoConic circle = (GeoConic) requireLookup("c");
		assertNotNull(locus);
		assertTrue(circle.isCircle());

		GeoLocusIntersectionResult rich = add("R4=Intersect(a,c)");
		assertNotNull(rich);
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		assertNotNull(result);
		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(2, result.getFiniteSolutions().size());
		assertEquals(0, result.getWork().getContinuationComparisons());
		assertEquals(1, result.getWork().getRetainedTopologyEpochs());

		List<LocusIntersectionSolution2D> solutions = result.getFiniteSolutions();
		for (LocusIntersectionSolution2D solution : solutions) {
			assertAuthorFixtureIncidence(locus, solution);
			assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
					solution.getClassification().getContactClass());
			assertEquals(LocalIsolationStatus.ESTABLISHED,
					solution.getRevisionEvidence().getLocalIsolationStatus());
			assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
					solution.getIdentity().getIdentityStatus());
			assertEquals(LineageEventKind.APPEARED,
					solution.getLineage().getEventKind());
			assertTrue(solution.getDiagnostics().stream().noneMatch(diagnostic ->
					diagnostic.getCode()
							== DiagnosticCode.CONTINUATION_ESTABLISHED));
			assertTrue(solution.getIdentity().getExplicitContinuationKey().isPresent());
			assertTrue(rich.isPointAdmissible(
					solution.getIdentity().getRootToken()));
		}
		assertNotEquals(solutions.get(0).getIdentity().getRootToken(),
				solutions.get(1).getIdentity().getRootToken());

		GeoPoint first = materialize("P4a", rich, solutions.get(0));
		GeoPoint second = materialize("P4b", rich, solutions.get(1));
		assertTrue(first.isDefined());
		assertTrue(second.isDefined());
	}

	@Test
	void publishedR3SingletonPositiveControlRemainsAdmissible() {
		add("s=0");
		add("Q=(s,0)");
		add("D={false,{-2,2,true,true}}");
		add("L=LocusV2(Q,s,D)");
		add("u:x=0");
		GeoLocusIntersectionResult rich = add("S=Intersect(L,u)");
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		assertEquals(1, result.getFiniteSolutions().size());
		LocusIntersectionSolution2D solution = result.getFiniteSolutions().get(0);
		assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
				solution.getIdentity().getIdentityStatus());
		assertTrue(solution.getIdentity().getExplicitContinuationKey().isPresent());
		assertTrue(rich.isPointAdmissible(
				solution.getIdentity().getRootToken()));
		assertTrue(materialize("X", rich, solution).isDefined());
	}

	@Test
	void preR4V1XmlTokenPointMigratesWithoutTokenChangeAndTracksMotion()
			throws Exception {
		add("legacyTargetX=0");
		add("legacyParameter=0");
		add("LegacyGenerator=(legacyParameter,0)");
		add("legacyDomain={false,{-2,2,true,true}}");
		add("legacyLocus=LocusV2(LegacyGenerator,legacyParameter,legacyDomain)");
		add("legacyLine:x=legacyTargetX");
		GeoLocusIntersectionResult rich = add(
				"legacyRich=Intersect(legacyLocus,legacyLine)");
		assertEquals(1, rich.getIntersectionResult().getFiniteSolutions().size());
		LocusIntersectionSolution2D solution = rich.getIntersectionResult()
				.getFiniteSolutions().get(0);
		assertTrue(materialize("legacyPoint", rich, solution).isDefined());
		LegacyV1Seed legacy = legacyV1Seed(rich, solution);

		String currentXml = getApp().getXML();
		assertTrue(currentXml.contains(legacy.currentState()));
		assertTrue(currentXml.contains(legacy.currentToken()));
		String legacyXml = currentXml
				.replace(legacy.currentState(), legacy.legacyState())
				.replace(legacy.currentToken(), legacy.legacyToken());
		assertNotEquals(currentXml, legacyXml);
		assertTrue(legacyXml.contains(legacy.legacyState()));
		assertTrue(legacyXml.contains(legacy.legacyToken()));

		getApp().setXML(legacyXml, true);
		GeoLocusIntersectionResult migrated =
				(GeoLocusIntersectionResult) requireLookup("legacyRich");
		GeoPoint point = (GeoPoint) requireLookup("legacyPoint");
		assertMigratedLegacyPoint(migrated, point, legacy.legacyToken());
		assertTrue(migrated.getTokenLedgerState().startsWith("3|"));
		assertNotEquals(legacy.legacyState(), migrated.getTokenLedgerState());

		GeoNumeric targetX = (GeoNumeric) requireLookup("legacyTargetX");
		targetX.setValue(0.5);
		targetX.updateCascade();
		assertMigratedLegacyPoint(migrated, point, legacy.legacyToken());
		assertEquals(0.5, point.getInhomX(), 1E-9);
		assertEquals(0, point.getInhomY(), 1E-9);
	}

	@Test
	void authorTokenPointsSurviveSaveReopenAndSameStateRecompute()
			throws Exception {
		loadAuthorFixture();
		GeoLocusIntersectionResult rich = add("R4=Intersect(a,c)");
		List<LocusIntersectionSolution2D> solutions = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, solutions.size());
		Map<String, String> pointTokens = new LinkedHashMap<>();
		for (int index = 0; index < solutions.size(); index++) {
			String label = "P4" + index;
			pointTokens.put(label,
					solutions.get(index).getIdentity().getRootToken());
			assertTrue(materialize(label, rich, solutions.get(index)).isDefined());
		}
		String ledger = rich.getTokenLedgerState();

		reload();
		GeoLocusIntersectionResult reopened =
				(GeoLocusIntersectionResult) requireLookup("R4");
		assertEquals(ledger, reopened.getTokenLedgerState());
		assertEquals(Set.copyOf(pointTokens.values()), tokens(reopened));
		assertTokenPoints(pointTokens, reopened);

		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		movePointX(radiusPoint, radiusPoint.getInhomX() + 0.5, 64);
		assertEquals(Set.copyOf(pointTokens.values()), tokens(reopened));
		assertTokenPoints(pointTokens, reopened);

		requireLookup("C").updateCascade();
		assertEquals(Set.copyOf(pointTokens.values()), tokens(reopened));
		assertTokenPoints(pointTokens, reopened);
	}

	@Test
	void authorMidpointDirectRegularMotionRetainsExactTokenPoints()
			throws Exception {
		loadAuthorFixture();
		GeoLocusIntersectionResult rich = add("R4=Intersect(a,c)");
		List<LocusIntersectionSolution2D> solutions = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, solutions.size());
		Map<String, String> pointTokens = new LinkedHashMap<>();
		for (int index = 0; index < solutions.size(); index++) {
			String label = "directPoint" + index;
			pointTokens.put(label,
					solutions.get(index).getIdentity().getRootToken());
			assertTrue(materialize(label, rich, solutions.get(index)).isDefined());
		}

		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		radiusPoint.setCoords(radiusPoint.getInhomX() + 0.05,
				radiusPoint.getInhomY(), 1);
		radiusPoint.updateCascade();

		assertEquals(Set.copyOf(pointTokens.values()), tokens(rich),
				describeRoots(rich));
		assertTokenPoints(pointTokens, rich);
	}

	@Test
	void authorMidpointBindingIsPathIndependentAcrossRegularHistories()
			throws Exception {
		loadAuthorFixture();
		GeoLocusIntersectionResult rich = add("R4=Intersect(a,c)");
		List<LocusIntersectionSolution2D> solutions = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, solutions.size());
		for (int index = 0; index < solutions.size(); index++) {
			assertTrue(materialize("pathPoint" + index, rich,
					solutions.get(index)).isDefined());
		}
		final String byteIdenticalSeed = getApp().getXML();

		PathSnapshot direct = runMidpointPath(byteIdenticalSeed,
				MotionPath.DIRECT);
		PathSnapshot incremental = runMidpointPath(byteIdenticalSeed,
				MotionPath.INCREMENTAL);
		PathSnapshot forwardReverse = runMidpointPath(byteIdenticalSeed,
				MotionPath.FORWARD_REVERSE);
		PathSnapshot reopened = runMidpointPath(byteIdenticalSeed,
				MotionPath.REOPENED);
		PathSnapshot oddIncrements = runMidpointPath(byteIdenticalSeed,
				MotionPath.ODD_INCREMENTS);

		assertEquals(direct, incremental);
		assertEquals(direct, forwardReverse);
		assertEquals(direct, reopened);
		assertEquals(direct, oddIncrements);
		assertEquals(2, direct.bindings().size());
		assertTrue(direct.bindings().values().stream().allMatch(binding ->
				binding.defined() && binding.pointAdmissible()
						&& binding.identityStatus()
								== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED));
	}

	@Test
	void authorMidpointBroadRegularMotionIsContinuousAndDoesNotSwap()
			throws Exception {
		loadAuthorFixture();
		GeoLocusIntersectionResult rich = add("R4=Intersect(a,c)");
		List<LocusIntersectionSolution2D> initial = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, initial.size());
		Map<String, String> pointLabels = new LinkedHashMap<>();
		Map<String, String> germs = new LinkedHashMap<>();
		Map<String, String> branchLineages = new LinkedHashMap<>();
		Map<String, String> components = new LinkedHashMap<>();
		Map<String, String> selectorKeys = new LinkedHashMap<>();
		Map<String, LocusPoint2D> priorPoints = new LinkedHashMap<>();
		for (int index = 0; index < initial.size(); index++) {
			LocusIntersectionSolution2D solution = initial.get(index);
			String token = solution.getIdentity().getRootToken();
			String label = "motionPoint" + index;
			pointLabels.put(token, label);
			germs.put(token, solution.getRevisionEvidence().getCurrentRootGerm()
					.orElseThrow());
			branchLineages.put(token,
					solution.getIdentity().getEstablishedBranchLineage());
			components.put(token, solution.getRevisionEvidence()
					.getResolvedValidComponentKey());
			selectorKeys.put(token, solution.getIdentity()
					.getExplicitContinuationKey().orElseThrow());
			priorPoints.put(token, solution.getEvaluatedPoint());
			assertTrue(materialize(label, rich, solution).isDefined());
		}

		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		double initialX = radiusPoint.getInhomX();
		double initialY = radiusPoint.getInhomY();
		for (int step = 1; step <= 96; step++) {
			double nextX = step == 96 ? initialX + 0.5
					: initialX + 0.5 * step / 96;
			setPoint(radiusPoint, nextX, initialY);
			assertEquals(pointLabels.keySet(), tokens(rich),
					"step=" + step + "; " + describeRoots(rich));
			for (Map.Entry<String, String> binding : pointLabels.entrySet()) {
				String token = binding.getKey();
				LocusIntersectionSolution2D solution = rich
						.findExactPointAdmissibleSolution(token).orElseThrow();
				GeoPoint point = (GeoPoint) requireLookup(binding.getValue());
				assertTrue(point.isDefined(), binding.getValue());
				assertEquals(token, ((AlgoLocusIntersectionPointV2)
						point.getParentAlgorithm()).getSelectedRootToken());
				assertEquals(germs.get(token), solution.getRevisionEvidence()
						.getCurrentRootGerm().orElseThrow());
				assertEquals(branchLineages.get(token), solution.getIdentity()
						.getEstablishedBranchLineage());
				assertEquals(components.get(token), solution.getRevisionEvidence()
						.getResolvedValidComponentKey());
				assertEquals(selectorKeys.get(token), solution.getIdentity()
						.getExplicitContinuationKey().orElseThrow());
				assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
						solution.getIdentity().getIdentityStatus());
				LocusPoint2D prior = priorPoints.put(token,
						solution.getEvaluatedPoint());
				double stepDistance = Math.hypot(
						solution.getEvaluatedPoint().getX() - prior.getX(),
						solution.getEvaluatedPoint().getY() - prior.getY());
				assertTrue(stepDistance < 0.25,
						binding.getValue() + " discontinuity at step " + step
								+ ": " + stepDistance);
			}
		}
	}

	@Test
	void materializedAuthorTokenPointUsesNormalUndoRedoLifecycle()
			throws Exception {
		activateUndo();
		loadAuthorFixture();
		GeoLocusIntersectionResult rich = add("R4=Intersect(a,c)");
		getApp().storeUndoInfo();
		LocusIntersectionSolution2D selected = rich.getIntersectionResult()
				.getFiniteSolutions().get(0);
		String token = selected.getIdentity().getRootToken();
		assertTrue(materialize("UndoPoint", rich, selected).isDefined());
		getApp().storeUndoInfo();

		getKernel().undo();
		assertNull(lookup("UndoPoint"));
		getKernel().redo();
		GeoPoint restored = (GeoPoint) requireLookup("UndoPoint");
		GeoLocusIntersectionResult restoredRich =
				(GeoLocusIntersectionResult) requireLookup("R4");
		assertTrue(restored.isDefined());
		assertEquals(token, ((AlgoLocusIntersectionPointV2)
				restored.getParentAlgorithm()).getSelectedRootToken());
		assertTrue(restoredRich.findExactPointAdmissibleSolution(token).isPresent());
	}

	@Test
	void localAdmissibilityRemainsIndependentOfGlobalCompleteness() {
		add("m=0");
		add("QM=(m,0)");
		add("DM={false,{-2,-0.5,true,true},{0.5,2,true,true}}");
		add("LM=LocusV2(QM,m,DM)");
		add("matrixLine:x=-1.5");
		GeoLocusIntersectionResult locallyAdmissible =
				add("RM=Intersect(LM,matrixLine)");
		LocusIntersectionResult2D notEstablished =
				locallyAdmissible.getIntersectionResult();
		String token = notEstablished.getFiniteSolutions().get(0)
				.getIdentity().getRootToken();
		assertEquals(Completeness.NOT_ESTABLISHED,
				notEstablished.getCompletenessEvidence().getCompleteness());
		assertTrue(notEstablished.findPointAdmissibleSolution(token).isPresent());

		AlgoLocusIntersectionV2 algorithm =
				(AlgoLocusIntersectionV2) locallyAdmissible.getParentAlgorithm();
		LocusIntersectionSolution2D established =
				notEstablished.getFiniteSolutions().get(0);
		String establishedComponent = established.getRevisionEvidence()
				.getResolvedValidComponentKey();
		String unrelatedComponent = IntersectionCapabilityContext2D.componentKey(
				established.getRevisionEvidence().getBranchSnapshotKey(), 1);
		LocusIntersectionResult2D mixedCandidate = withUnresolvedCandidate(
				notEstablished, List.of(unrelatedComponent));
		LocusIntersectionTarget2D captured = LocusIntersectionTargets2D.capture(
				algorithm.getTarget(), mixedCandidate.getSourceBinding()
						.getTargetIdentity(), mixedCandidate.getSourceBinding()
						.getTargetUpdateStamp());
		LocusIntersectionPolicy2D mixedPolicy = LocusIntersectionPolicy2D.initial(
				algorithm.getSource().getSemanticDefinition().getProvider()
						.getProviderId(),
				algorithm.getSource().getSemanticDefinition().getProvider()
						.getParameterDescriptor());
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = ledger.begin(
				"r4-mixed-result-owner",
				mixedCandidate.getSourceBinding().getSourcePairIdentity(),
				mixedCandidate.getSourceBinding()
						.getConstructiveIntersectionLineage(),
				mixedCandidate.getSourceBinding().getTopologyContext());
		LocusIntersectionResult2D mixedResolved =
				new PublicIntersectionRootIdentityResolver2D().resolve(null,
						mixedCandidate,
						algorithm.getSource().getSemanticDefinition(), captured,
						mixedPolicy, evaluation);
		ledger.abort(evaluation);
		assertEquals(1, mixedResolved.getWork().getUnresolvedCandidates());
		assertEquals(1, mixedResolved.getFiniteSolutions().size());
		assertTrue(mixedResolved.findPointAdmissibleSolution(
				mixedResolved.getFiniteSolutions().get(0).getIdentity()
						.getRootToken()).isPresent());

		LocusIntersectionResult2D collidingCandidate = withUnresolvedCandidate(
				notEstablished, List.of(establishedComponent));
		LocusIntersectionTokenLedger2D collidingLedger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation collidingEvaluation =
				collidingLedger.begin("r4-colliding-result-owner",
						collidingCandidate.getSourceBinding()
								.getSourcePairIdentity(),
						collidingCandidate.getSourceBinding()
								.getConstructiveIntersectionLineage(),
						collidingCandidate.getSourceBinding().getTopologyContext());
		LocusIntersectionResult2D collidingResolved =
				new PublicIntersectionRootIdentityResolver2D().resolve(null,
						collidingCandidate,
						algorithm.getSource().getSemanticDefinition(), captured,
						mixedPolicy, collidingEvaluation);
		collidingLedger.abort(collidingEvaluation);
		assertFalse(collidingResolved.findPointAdmissibleSolution(
				collidingResolved.getFiniteSolutions().get(0).getIdentity()
						.getRootToken()).isPresent());

		LocusIntersectionResult2D incomplete = withCompleteness(notEstablished,
				Completeness.INCOMPLETE, notEstablished.getFiniteSolutions());
		assertTrue(incomplete.findPointAdmissibleSolution(token).isPresent());

		add("t=0");
		add("QT=(t,t^2)");
		add("DT={false,{-2,2,true,true}}");
		add("LT=LocusV2(QT,t,DT)");
		add("tangentAxis:y=0");
		GeoLocusIntersectionResult tangent =
				add("RT=Intersect(LT,tangentAxis)");
		LocusIntersectionResult2D tangentResult = tangent.getIntersectionResult();
		assertEquals(1, tangentResult.getFiniteSolutions().size());
		LocusIntersectionSolution2D ambiguous = ambiguous(
				tangentResult.getFiniteSolutions().get(0));
		LocusIntersectionResult2D globallyCompleteAmbiguous = withCompleteness(
				tangentResult, Completeness.COMPLETE, List.of(ambiguous));
		assertFalse(globallyCompleteAmbiguous.findPointAdmissibleSolution(
				ambiguous.getIdentity().getRootToken()).isPresent());

		add("v=0");
		add("QV=(v,abs(v))");
		add("DV={false,{-1,1,true,true}}");
		add("LV=LocusV2(QV,v,DV)");
		add("unisolatedAxis:y=0");
		GeoLocusIntersectionResult unisolated =
				add("RV=Intersect(LV,unisolatedAxis)");
		LocusIntersectionResult2D unisolatedResult =
				unisolated.getIntersectionResult();
		assertFalse(unisolatedResult.getFiniteSolutions().isEmpty());
		assertTrue(unisolatedResult.getFiniteSolutions().stream().allMatch(
				solution -> solution.getRevisionEvidence()
						.getLocalIsolationStatus()
						== LocalIsolationStatus.NOT_ESTABLISHED));
		assertTrue(unisolatedResult.getFiniteSolutions().stream().allMatch(
				solution -> !unisolatedResult.findPointAdmissibleSolution(
						solution.getIdentity().getRootToken()).isPresent()));
	}

	@Test
	void disappearanceNewAppearanceAndTangencyRemainFailClosed() {
		add("h=1");
		GeoLocusIntersectionResult rich = intersect(createParabola(), "y=h");
		LocusIntersectionResult2D initial = rich.getIntersectionResult();
		assertEquals(2, initial.getFiniteSolutions().size());
		final Set<String> initialTokens = tokens(rich);
		List<GeoPoint> oldPoints = new ArrayList<>();
		for (int index = 0; index < initial.getFiniteSolutions().size(); index++) {
			oldPoints.add(materialize("old" + index, rich,
					initial.getFiniteSolutions().get(index)));
		}

		GeoNumeric height = (GeoNumeric) requireLookup("h");
		height.setValue(-1);
		height.updateCascade();
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(oldPoints.stream().noneMatch(GeoPoint::isDefined));

		height.setValue(1);
		height.updateCascade();
		LocusIntersectionResult2D reappeared = rich.getIntersectionResult();
		assertEquals(2, reappeared.getFiniteSolutions().size());
		Set<String> reappearedTokens = tokens(rich);
		assertTrue(initialTokens.stream().noneMatch(reappearedTokens::contains));
		assertTrue(reappeared.getFiniteSolutions().stream().allMatch(solution ->
				solution.getIdentity().getIdentityStatus()
						== IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
						&& rich.isPointAdmissible(
								solution.getIdentity().getRootToken())));
		assertTrue(oldPoints.stream().noneMatch(GeoPoint::isDefined));

		height.setValue(0);
		height.updateCascade();
		LocusIntersectionResult2D tangent = rich.getIntersectionResult();
		assertEquals(1, tangent.getFiniteSolutions().size());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				tangent.getFiniteSolutions().get(0).getClassification()
						.getContactClass());
		assertTrue(tangent.getFiniteSolutions().stream().allMatch(solution ->
				!rich.isPointAdmissible(
						solution.getIdentity().getRootToken())));
		assertTrue(oldPoints.stream().noneMatch(GeoPoint::isDefined));

		height.setValue(1E-6);
		height.updateCascade();
		LocusIntersectionResult2D split = rich.getIntersectionResult();
		assertEquals(2, split.getFiniteSolutions().size());
		assertTrue(split.getFiniteSolutions().stream().allMatch(solution ->
				solution.getIdentity().getIdentityStatus()
						== IdentityStatus.AMBIGUOUS_CONTINUATION
						&& solution.getLineage().getEventKind()
								== LineageEventKind.SPLIT_CANDIDATE
						&& LocusSemanticIntersectionToken2D.isRevisionLocalHandle(
								solution.getIdentity().getRootToken())
						&& !rich.isPointAdmissible(
								solution.getIdentity().getRootToken())));
	}

	@Test
	void periodicSeamIsCanonicalAndDoesNotDuplicateTheBoundaryRoot() {
		GeoLocusIntersectionResult rich = intersect(createPeriodicCircle(),
				"y=0");
		List<LocusIntersectionSolution2D> solutions = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, solutions.size());
		List<Double> parameters = solutions.stream()
				.map(solution -> solution.getRevisionEvidence()
						.getSemanticParameter())
				.sorted().toList();
		assertEquals(0, parameters.get(0), 2E-10);
		assertEquals(Math.PI, parameters.get(1), 2E-10);
		assertTrue(parameters.stream().allMatch(parameter -> parameter >= 0
				&& parameter < 2 * Math.PI));
		assertEquals(2, solutions.stream()
				.map(solution -> Double.doubleToLongBits(solution
						.getRevisionEvidence().getSemanticParameter()))
				.collect(Collectors.toSet()).size());
		assertEquals(2, tokens(rich).size());
		assertTrue(solutions.stream().allMatch(solution -> rich.isPointAdmissible(
				solution.getIdentity().getRootToken())));
	}

	@Test
	void orientedRootCrossesPeriodicSeamWithoutDuplicateOrTokenSwap() {
		createPeriodicCircle();
		add("seamHeight=0.1");
		add("seamAxis:y=seamHeight");
		GeoLocusIntersectionResult rich = add(
				"seamResult=Intersect(L,seamAxis)");
		List<LocusIntersectionSolution2D> initial = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, initial.size());
		LocusIntersectionSolution2D nearLowerSeam = initial.stream()
				.filter(solution -> solution.getRevisionEvidence()
						.getSemanticParameter() < Math.PI / 2)
				.findFirst().orElseThrow();
		String seamToken = nearLowerSeam.getIdentity().getRootToken();
		GeoPoint seamPoint = materialize("seamPoint", rich, nearLowerSeam);
		Set<String> initialTokens = tokens(rich);

		GeoNumeric height = (GeoNumeric) requireLookup("seamHeight");
		moveNumericPreservingTokens(height, -0.1, 32, rich, initialTokens);
		assertEquals(2, rich.getIntersectionResult().getFiniteSolutions().size());
		assertEquals(2, tokens(rich).size());
		assertTrue(seamPoint.isDefined());
		LocusIntersectionSolution2D continued = rich
				.findExactPointAdmissibleSolution(seamToken).orElseThrow();
		assertTrue(continued.getRevisionEvidence().getSemanticParameter()
				> 3 * Math.PI / 2);
		assertEquals(IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED,
				continued.getIdentity().getIdentityStatus());
	}

	@Test
	void stableTransverseMotionUsesDeterministicCurrentSelectors() {
		add("stableHeight=1");
		GeoLocusIntersectionResult rich = intersect(createParabola(),
				"y=stableHeight");
		List<LocusIntersectionSolution2D> initial = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, initial.size());
		Map<String, String> pointTokens = new LinkedHashMap<>();
		for (int index = 0; index < initial.size(); index++) {
			String label = "stablePoint" + index;
			pointTokens.put(label, initial.get(index).getIdentity().getRootToken());
			assertTrue(materialize(label, rich, initial.get(index)).isDefined());
		}

		GeoNumeric height = (GeoNumeric) requireLookup("stableHeight");
		height.setValue(1 + 3.0 / 128);
		height.updateCascade();
		assertEquals(Set.copyOf(pointTokens.values()), tokens(rich),
				describeRoots(rich));
		moveNumericPreservingTokens(height, 4, 127, rich,
				Set.copyOf(pointTokens.values()));
		assertEquals(Set.copyOf(pointTokens.values()), tokens(rich));
		assertTokenPoints(pointTokens, rich);
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> solution.getIdentity().getIdentityStatus()
						== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
						&& solution.getLineage().getEventKind()
								== LineageEventKind.UNCHANGED));

		add("jumpDriver=-1");
		add("jump=If(jumpDriver<0,-1,1)");
		add("jumpAxis:x=jump");
		GeoLocusIntersectionResult discontinuous = add(
				"jumpResult=Intersect(L,jumpAxis)");
		String beforeJump = discontinuous.getIntersectionResult()
				.getFiniteSolutions().get(0).getIdentity().getRootToken();
		GeoPoint oldJumpPoint = materialize("oldJumpPoint", discontinuous,
				discontinuous.getIntersectionResult().getFiniteSolutions().get(0));
		GeoNumeric jumpDriver = (GeoNumeric) requireLookup("jumpDriver");
		jumpDriver.setValue(1);
		jumpDriver.updateCascade();
		String afterJump = discontinuous.getIntersectionResult()
				.getFiniteSolutions().get(0).getIdentity().getRootToken();
		assertEquals(beforeJump, afterJump);
		assertTrue(oldJumpPoint.isDefined());
		assertEquals(discontinuous.getIntersectionResult().getFiniteSolutions().get(0)
				.getEvaluatedPoint().getX(), oldJumpPoint.getInhomX(), 1E-9);
	}

	@Test
	void spatialLeftRightOrderCanReverseWithoutTokenSwap() {
		createPeriodicCircle();
		GeoNumeric angle = add("orderingAngle=4*pi/9");
		add("orderingLine:-sin(orderingAngle)*x+cos(orderingAngle)*y=0");
		GeoLocusIntersectionResult rich = add(
				"orderingResult=Intersect(L,orderingLine)");
		List<LocusIntersectionSolution2D> initial = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, initial.size());
		LocusIntersectionSolution2D lowerParameterRoot = initial.stream()
				.filter(solution -> solution.getRevisionEvidence()
						.getSemanticParameter() < Math.PI)
				.findFirst().orElseThrow();
		LocusIntersectionSolution2D upperParameterRoot = initial.stream()
				.filter(solution -> solution.getRevisionEvidence()
						.getSemanticParameter() > Math.PI)
				.findFirst().orElseThrow();
		String lowerToken = lowerParameterRoot.getIdentity().getRootToken();
		String upperToken = upperParameterRoot.getIdentity().getRootToken();
		GeoPoint lowerPoint = materialize("orderingLower", rich,
				lowerParameterRoot);
		GeoPoint upperPoint = materialize("orderingUpper", rich,
				upperParameterRoot);
		assertTrue(lowerPoint.getInhomX() > upperPoint.getInhomX());

		Set<String> exactTokens = Set.of(lowerToken, upperToken);
		moveNumericPreservingTokens(angle, Math.PI / 2, 32, rich,
				exactTokens);
		assertEquals(0, lowerPoint.getInhomX(), 1E-9);
		assertEquals(0, upperPoint.getInhomX(), 1E-9);
		assertTrue(lowerPoint.getInhomY() > upperPoint.getInhomY());
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> solution.getIdentity().getIdentityStatus()
						== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
						&& solution.getClassification().getContactClass()
								== ContactClass.TRANSVERSE_ESTABLISHED));

		moveNumericPreservingTokens(angle, 5 * Math.PI / 9, 32, rich,
				exactTokens);

		assertEquals(exactTokens, tokens(rich));
		assertTrue(lowerPoint.isDefined());
		assertTrue(upperPoint.isDefined());
		assertTrue(lowerPoint.getInhomX() < upperPoint.getInhomX());
		assertEquals(lowerToken,
				((AlgoLocusIntersectionPointV2) lowerPoint.getParentAlgorithm())
						.getSelectedRootToken());
		assertEquals(upperToken,
				((AlgoLocusIntersectionPointV2) upperPoint.getParentAlgorithm())
						.getSelectedRootToken());
	}

	@Test
	void uniqueDeterministicSelectorSurvivesUnobservedDirectUpdate() {
		add("jumpParameter=0");
		add("JumpPoint=(jumpParameter,0)");
		add("jumpDomain={false,{-2,2,true,true}}");
		add("jumpLocus=LocusV2(JumpPoint,jumpParameter,jumpDomain)");
		add("jumpOffset=-1");
		add("jumpAxis:x=jumpOffset");
		GeoLocusIntersectionResult rich = add(
				"jumpResult=Intersect(jumpLocus,jumpAxis)");
		assertEquals(1, rich.getIntersectionResult().getFiniteSolutions().size());
		String before = rich.getIntersectionResult().getFiniteSolutions().get(0)
				.getIdentity().getRootToken();
		GeoPoint priorPoint = materialize("jumpPrior", rich,
				rich.getIntersectionResult().getFiniteSolutions().get(0));

		GeoNumeric offset = (GeoNumeric) requireLookup("jumpOffset");
		offset.setValue(1);
		offset.updateCascade();
		assertEquals(1, rich.getIntersectionResult().getFiniteSolutions().size());
		String after = rich.getIntersectionResult().getFiniteSolutions().get(0)
				.getIdentity().getRootToken();
		assertEquals(before, after);
		assertTrue(priorPoint.isDefined());
		assertTrue(rich.isPointAdmissible(after));
	}

	@Test
	void repeatedDeterministicSelectorsUseIntrinsicSemanticRank() {
		add("multi=0");
		add("M=(multi,(multi^2-1)*(multi^2-4))");
		add("multiDomain={false,{-3,3,true,true}}");
		add("multiLocus=LocusV2(M,multi,multiDomain)");
		add("multiHeight=0");
		add("multiAxis:y=multiHeight");
		GeoLocusIntersectionResult rich = add(
				"multiResult=Intersect(multiLocus,multiAxis)");
		List<LocusIntersectionSolution2D> initial = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(4, initial.size());
		assertEquals(4, tokens(rich).size());
		assertTrue(initial.stream().allMatch(solution -> rich.isPointAdmissible(
				solution.getIdentity().getRootToken())));
		assertEquals(Set.of("negative", "positive"), initial.stream()
				.map(solution -> solution.getRevisionEvidence().getCurrentRootGerm()
						.orElseThrow())
				.map(germ -> germ.endsWith("positive")
						? "positive" : "negative")
				.collect(Collectors.toSet()));

		GeoNumeric height = (GeoNumeric) requireLookup("multiHeight");
		height.setValue(0.1);
		height.updateCascade();
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		assertEquals(4, tokens(rich).size());
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> rich.isPointAdmissible(
						solution.getIdentity().getRootToken())));
	}

	@Test
	void rankedCollisionGroupAppearanceAndDisappearanceInvalidatesWithoutShift() {
		add("cardinalityParameter=0");
		add("CardinalityGenerator=(cardinalityParameter,"
				+ "(cardinalityParameter^2-1)*(cardinalityParameter^2-4))");
		add("cardinalityDomain={false,{-3,3,true,true}}");
		add("cardinalityLocus=LocusV2(CardinalityGenerator,"
				+ "cardinalityParameter,cardinalityDomain)");
		GeoNumeric height = add("cardinalityHeight=0");
		add("cardinalityAxis:y=cardinalityHeight");
		GeoLocusIntersectionResult rich = add(
				"cardinalityResult=Intersect(cardinalityLocus,cardinalityAxis)");
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		Set<String> fourTokens = tokens(rich);
		List<GeoPoint> oldPoints = new ArrayList<>();
		for (int index = 0; index < 4; index++) {
			oldPoints.add(materialize("cardinalityPoint" + index, rich,
					rich.getIntersectionResult().getFiniteSolutions().get(index)));
		}

		height.setValue(5);
		height.updateCascade();
		assertEquals(2, rich.getIntersectionResult().getFiniteSolutions().size());
		Set<String> twoTokens = tokens(rich);
		assertTrue(java.util.Collections.disjoint(fourTokens, twoTokens));
		assertTrue(oldPoints.stream().noneMatch(GeoPoint::isDefined));

		height.setValue(0);
		height.updateCascade();
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		Set<String> restoredTokens = tokens(rich);
		assertTrue(java.util.Collections.disjoint(fourTokens, restoredTokens));
		assertTrue(java.util.Collections.disjoint(twoTokens, restoredTokens));
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> rich.isPointAdmissible(
						solution.getIdentity().getRootToken())));
		assertTrue(oldPoints.stream().noneMatch(GeoPoint::isDefined));
	}

	@Test
	void reversingProviderOrientationNeverTransfersRankedTokensWithoutMap() {
		final GeoBoolean reverse = add("reverseDomain=false");
		add("orientationParameter=0");
		add("OrientationGenerator=(orientationParameter,"
				+ "(orientationParameter^2-1)*(orientationParameter^2-4))");
		add("orientationDomain=If(reverseDomain,"
				+ "{false,{3,-3,true,true}},{false,{-3,3,true,true}})");
		add("orientationLocus=LocusV2(OrientationGenerator,"
				+ "orientationParameter,orientationDomain)");
		add("orientationAxis:y=0");
		GeoLocusIntersectionResult rich = add(
				"orientationResult=Intersect(orientationLocus,orientationAxis)");
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		Set<String> increasingTokens = tokens(rich);
		List<GeoPoint> increasingPoints = new ArrayList<>();
		for (int index = 0; index < 4; index++) {
			increasingPoints.add(materialize("orientationPoint" + index, rich,
					rich.getIntersectionResult().getFiniteSolutions().get(index)));
		}

		reverse.setValue(true);
		reverse.updateCascade();
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		Set<String> decreasingTokens = tokens(rich);
		assertTrue(java.util.Collections.disjoint(increasingTokens,
				decreasingTokens));
		assertTrue(increasingPoints.stream().noneMatch(GeoPoint::isDefined));
		requireLookup("orientationLocus").updateCascade();
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		decreasingTokens = tokens(rich);
		assertTrue(java.util.Collections.disjoint(increasingTokens,
				decreasingTokens));
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> rich.isPointAdmissible(
						solution.getIdentity().getRootToken())));
	}

	@Test
	void rankedPeriodicSeamInvalidatesInsteadOfRotatingOpaqueTokens() {
		final GeoNumeric phase = add("rankedPhase=0");
		add("rankedPeriodicParameter=0");
		add("RankedPeriodicGenerator=(cos(rankedPeriodicParameter+rankedPhase),"
				+ "sin(2*(rankedPeriodicParameter+rankedPhase))"
				+ "+0.2*sin(rankedPeriodicParameter+rankedPhase)"
				+ "+0.1*cos(rankedPeriodicParameter+rankedPhase))");
		add("rankedPeriodicDomain={true,{0,2*pi,true,false}}");
		add("rankedPeriodicLocus=LocusV2(RankedPeriodicGenerator,"
				+ "rankedPeriodicParameter,rankedPeriodicDomain)");
		add("rankedPeriodicAxis:y=0");
		GeoLocusIntersectionResult rich = add(
				"rankedPeriodicResult=Intersect(rankedPeriodicLocus,"
						+ "rankedPeriodicAxis)");
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		List<Double> initialParameters = rich.getIntersectionResult()
				.getFiniteSolutions().stream().map(solution -> solution
						.getRevisionEvidence().getSemanticParameter()).sorted().toList();
		Set<String> initialTokens = tokens(rich);
		Map<String, GeoPoint> initialPoints = new LinkedHashMap<>();
		for (int index = 0; index < 4; index++) {
			LocusIntersectionSolution2D solution = rich.getIntersectionResult()
					.getFiniteSolutions().get(index);
			initialPoints.put(solution.getIdentity().getRootToken(),
					materialize("rankedPeriodicPoint" + index, rich, solution));
		}
		final String byteIdenticalSeed = getApp().getXML();

		boolean observedTypedDiscontinuity = false;
		for (int step = 1; step <= 128; step++) {
			phase.setValue(-0.1 * step / 128);
			phase.updateCascade();
			observedTypedDiscontinuity |= rich.getIntersectionResult()
					.getFiniteSolutions().stream().anyMatch(solution ->
							solution.getIdentity().getIdentityStatus()
									== IdentityStatus.IDENTITY_DISCONTINUITY)
					|| rich.getIntersectionResult().getDiagnostics().stream()
							.anyMatch(diagnostic -> diagnostic.getCode()
									== DiagnosticCode.IDENTITY_DISCONTINUITY);
		}
		assertTrue(observedTypedDiscontinuity,
				"initial=" + initialParameters + "; final="
						+ rich.getIntersectionResult().getFiniteSolutions().stream()
								.map(solution -> solution.getRevisionEvidence()
										.getSemanticParameter()).sorted().toList()
						+ "; diagnostics="
						+ rich.getIntersectionResult().getDiagnostics());
		assertTrue(initialPoints.values().stream()
				.anyMatch(point -> !point.isDefined()));
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		Set<String> afterSeam = tokens(rich);
		assertNotEquals(initialTokens, afterSeam);
		for (Map.Entry<String, GeoPoint> entry : initialPoints.entrySet()) {
			if (afterSeam.contains(entry.getKey())) {
				LocusIntersectionSolution2D current = rich
						.findExactPointAdmissibleSolution(entry.getKey())
						.orElseThrow();
				assertTrue(entry.getValue().isDefined());
				assertEquals(current.getEvaluatedPoint().getX(),
						entry.getValue().getInhomX(), 1E-9);
				assertEquals(current.getEvaluatedPoint().getY(),
						entry.getValue().getInhomY(), 1E-9);
			} else {
				assertFalse(entry.getValue().isDefined());
			}
		}
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> rich.isPointAdmissible(
						solution.getIdentity().getRootToken())));

		for (int step = 1; step <= 256; step++) {
			phase.setValue(-0.1 + (-2 * Math.PI + 0.1) * step / 256);
			phase.updateCascade();
		}
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		assertTrue(java.util.Collections.disjoint(initialTokens, tokens(rich)));
		assertTrue(initialPoints.values().stream().noneMatch(GeoPoint::isDefined));
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> rich.isPointAdmissible(
						solution.getIdentity().getRootToken())));

		getApp().setXML(byteIdenticalSeed, true);
		final GeoNumeric directPhase = (GeoNumeric) requireLookup("rankedPhase");
		final GeoLocusIntersectionResult directRich =
				(GeoLocusIntersectionResult) requireLookup("rankedPeriodicResult");
		assertEquals(initialTokens, tokens(directRich));
		Map<String, GeoPoint> directInitialPoints = new LinkedHashMap<>();
		for (int index = 0; index < 4; index++) {
			GeoPoint point = (GeoPoint) requireLookup(
					"rankedPeriodicPoint" + index);
			AlgoLocusIntersectionPointV2 algorithm =
					(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
			directInitialPoints.put(algorithm.getSelectedRootToken(), point);
		}
		assertEquals(initialTokens, directInitialPoints.keySet());

		directPhase.setValue(-0.1);
		directPhase.updateCascade();
		assertEquals(4,
				directRich.getIntersectionResult().getFiniteSolutions().size());
		assertTrue(directRich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> solution.getIdentity().getIdentityStatus()
						== IdentityStatus.IDENTITY_DISCONTINUITY));
		Set<String> directTransitionParents = directRich.getIntersectionResult()
				.getFiniteSolutions().stream()
				.flatMap(solution -> solution.getLineage()
						.getCandidateParentTokens().stream())
				.collect(Collectors.toSet());
		assertEquals(4, directTransitionParents.size());
		assertEquals(initialTokens, directTransitionParents,
				"A direct seam crossing must fail closed for every ranked group "
						+ "whose previous/current relation was not established");
		assertTrue(directInitialPoints.values().stream()
				.noneMatch(GeoPoint::isDefined));
		assertTrue(java.util.Collections.disjoint(initialTokens,
				tokens(directRich)));
		assertTrue(directRich.getIntersectionResult().getFiniteSolutions().stream()
				.noneMatch(solution -> directRich.isPointAdmissible(
						solution.getIdentity().getRootToken())));

		directPhase.updateCascade();
		assertEquals(4,
				directRich.getIntersectionResult().getFiniteSolutions().size());
		assertTrue(java.util.Collections.disjoint(initialTokens,
				tokens(directRich)));
		assertTrue(directRich.getIntersectionResult().getFiniteSolutions().stream()
				.allMatch(solution -> directRich.isPointAdmissible(
						solution.getIdentity().getRootToken())));
	}

	@Test
	void mergeCandidateParentKeysRemainExplicitAndNonAdmissible() {
		add("mergeHeight=1");
		GeoLocusIntersectionResult rich = intersect(createParabola(),
				"y=mergeHeight");
		LocusIntersectionResult2D before = rich.getIntersectionResult();
		assertEquals(2, before.getFiniteSolutions().size());
		List<LocusIntersectionSolution2D> canonicalParents = before
				.getFiniteSolutions().stream()
				.sorted(Comparator.comparing(solution ->
						solution.getIdentity().getRootToken()))
				.toList();
		List<String> parentTokens = canonicalParents.stream()
				.map(solution -> solution.getIdentity().getRootToken()).toList();
		List<String> parentKeys = canonicalParents.stream()
				.map(solution -> solution.getIdentity()
						.getExplicitContinuationKey().orElseThrow()).toList();

		GeoNumeric height = (GeoNumeric) requireLookup("mergeHeight");
		height.setValue(0);
		height.updateCascade();
		LocusIntersectionResult2D tangent = rich.getIntersectionResult();
		assertEquals(1, tangent.getFiniteSolutions().size());
		LocusIntersectionSolution2D actual = tangent.getFiniteSolutions().get(0);
		assertEquals(LineageEventKind.MERGE_CANDIDATE,
				actual.getLineage().getEventKind());
		assertEquals(parentTokens,
				actual.getLineage().getCandidateParentTokens());
		assertEquals(parentKeys,
				actual.getLineage().getCandidateParentContinuationKeys());

		ArrayList<LocusIntersectionSolution2D> reversedParents =
				new ArrayList<>(before.getFiniteSolutions());
		Collections.reverse(reversedParents);
		LocusIntersectionResult2D reversedBefore = withCompleteness(before,
				before.getCompletenessEvidence().getCompleteness(), reversedParents);
		AlgoLocusIntersectionV2 algorithm =
				(AlgoLocusIntersectionV2) rich.getParentAlgorithm();
		LocusIntersectionSolution2D canonicalOrderMerge = resolveDetached(before,
				algorithm, tangent, "r4-merge-parent-order-canonical")
				.getFiniteSolutions().get(0);
		LocusIntersectionSolution2D reversedOrderMerge = resolveDetached(
				reversedBefore, algorithm, tangent,
				"r4-merge-parent-order-reversed")
				.getFiniteSolutions().get(0);
		assertEquals(LineageEventKind.MERGE_CANDIDATE,
				canonicalOrderMerge.getLineage().getEventKind());
		assertEquals(LineageEventKind.MERGE_CANDIDATE,
				reversedOrderMerge.getLineage().getEventKind());
		assertEquals(canonicalOrderMerge.getLineage().getCandidateParentTokens(),
				reversedOrderMerge.getLineage().getCandidateParentTokens());
		assertEquals(canonicalOrderMerge.getLineage()
				.getCandidateParentContinuationKeys(), reversedOrderMerge.getLineage()
						.getCandidateParentContinuationKeys());
		IntersectionRootIdentity2D old = actual.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				old.getRootToken(), old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				Optional.empty(), IdentityStatus.AMBIGUOUS_CONTINUATION);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				LineageEventKind.MERGE_CANDIDATE, parentTokens,
				List.of(old.getRootToken()), parentKeys, false);
		LocusIntersectionSolution2D mergeCandidate =
				new LocusIntersectionSolution2D(identity,
						actual.getRevisionEvidence(), actual.getEvaluatedPoint(),
						actual.getClassification(), lineage,
						actual.getDiagnostics(), actual.getPairEvidence());
		LocusIntersectionResult2D explicitMerge = withCompleteness(tangent,
				tangent.getCompletenessEvidence().getCompleteness(),
				List.of(mergeCandidate));

		assertEquals(parentTokens,
				mergeCandidate.getLineage().getCandidateParentTokens());
		assertEquals(parentKeys, mergeCandidate.getLineage()
				.getCandidateParentContinuationKeys());
		assertFalse(explicitMerge.findPointAdmissibleSolution(
				mergeCandidate.getIdentity().getRootToken()).isPresent());
	}

	@Test
	void stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence() {
		final GeoLocusV2 locus = createLine();
		add("r4Line:x=0");
		add("r4Segment=Segment((0,-1),(0,1))");
		add("r4Ray=Ray((0,-1),(0,1))");
		add("r4Circle=Circle((0,0),1)");
		add("r4Ellipse:x^2/4+(y-0.5)^2=1");
		GeoFunction function = add("r4Function(x)=x^2-1");
		assertTrue(function.setInterval(-2, 2));
		add("r4Implicit=ImplicitCurve(x^3+y^3-1)");
		add("w=0");
		add("W=(w,w)");
		add("DW={false,{-2,2,true,true}}");
		add("r4Locus=LocusV2(W,w,DW)");
		Map<String, TargetFamily> targets = new LinkedHashMap<>();
		targets.put("r4Line", TargetFamily.LINE);
		targets.put("r4Segment", TargetFamily.SEGMENT);
		targets.put("r4Ray", TargetFamily.RAY);
		targets.put("r4Circle", TargetFamily.CIRCLE);
		targets.put("r4Ellipse", TargetFamily.ELLIPSE);
		targets.put("r4Function", TargetFamily.BOUNDED_FUNCTION_GRAPH);
		targets.put("r4Implicit", TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT);
		targets.put("r4Locus", TargetFamily.LOCUS_V2);
		for (Map.Entry<String, TargetFamily> target : targets.entrySet()) {
			GeoLocusIntersectionResult rich = add("R_" + target.getKey()
					+ "=Intersect(" + locus.getLabelSimple() + ","
					+ target.getKey() + ")");
			LocusIntersectionResult2D result = rich.getIntersectionResult();
			assertEquals(ComputationStatus.SUCCESS,
					result.getComputationStatus(), target.getKey());
			assertEquals(target.getValue(),
					result.getSourceBinding().getTargetFamily(), target.getKey());
			assertFalse(result.getFiniteSolutions().isEmpty(), target.getKey());
			for (LocusIntersectionSolution2D solution
					: result.getFiniteSolutions()) {
				if (target.getValue() == TargetFamily.LOCUS_V2) {
					assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
							solution.getPairEvidence().orElseThrow()
									.getLocalIsolation().getStatus(), target.getKey());
					assertFalse(rich.isPointAdmissible(
							solution.getIdentity().getRootToken()), target.getKey());
					continue;
				} else {
					assertEquals(LocalIsolationStatus.ESTABLISHED,
							solution.getRevisionEvidence()
									.getLocalIsolationStatus(), target.getKey());
				}
				assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
						solution.getClassification().getContactClass(),
						target.getKey());
				assertTrue(rich.isPointAdmissible(
						solution.getIdentity().getRootToken()), target.getKey());
			}
		}

		GeoLocusIntersectionResult lineRich =
				(GeoLocusIntersectionResult) requireLookup("R_r4Line");
		Map<String, LocusPoint2D> lineSnapshot =
				admissibleTokenSnapshot(lineRich);
		String lineToken = lineSnapshot.keySet().iterator().next();
		GeoPoint linePoint = materialize("R4LineRepresentationPoint", lineRich,
				lineRich.findExactPointAdmissibleSolution(lineToken).orElseThrow());
		GeoLine line = (GeoLine) requireLookup("r4Line");
		line.setCoords(-line.getX(), -line.getY(), -line.getZ());
		line.updateRepaint();
		assertEquivalentTargetRepresentation(lineRich, lineSnapshot, linePoint,
				lineToken);

		GeoLocusIntersectionResult conicRich =
				(GeoLocusIntersectionResult) requireLookup("R_r4Ellipse");
		Map<String, LocusPoint2D> conicSnapshot =
				admissibleTokenSnapshot(conicRich);
		String conicToken = conicSnapshot.keySet().iterator().next();
		GeoPoint conicPoint = materialize("R4ConicRepresentationPoint", conicRich,
				conicRich.findExactPointAdmissibleSolution(conicToken).orElseThrow());
		GeoConic conic = (GeoConic) requireLookup("r4Ellipse");
		double[] matrix = conic.getFlatMatrix().clone();
		conic.setCoeffs(-matrix[0], -2 * matrix[3], -matrix[1],
				-2 * matrix[4], -2 * matrix[5], -matrix[2]);
		conic.updateRepaint();
		assertEquivalentTargetRepresentation(conicRich, conicSnapshot, conicPoint,
				conicToken);

		GeoLocusIntersectionResult implicitRich =
				(GeoLocusIntersectionResult) requireLookup("R_r4Implicit");
		Map<String, LocusPoint2D> implicitSnapshot =
				admissibleTokenSnapshot(implicitRich);
		String implicitToken = implicitSnapshot.keySet().iterator().next();
		GeoPoint implicitPoint = materialize("R4ImplicitRepresentationPoint",
				implicitRich, implicitRich.findExactPointAdmissibleSolution(
						implicitToken).orElseThrow());
		GeoImplicitCurve implicit =
				(GeoImplicitCurve) requireLookup("r4Implicit");
		double[][] coefficients = implicit.getCoeff();
		double[][] negativeCoefficients = new double[coefficients.length][];
		for (int xDegree = 0; xDegree < coefficients.length; xDegree++) {
			negativeCoefficients[xDegree] = coefficients[xDegree].clone();
			for (int yDegree = 0;
					yDegree < negativeCoefficients[xDegree].length; yDegree++) {
				negativeCoefficients[xDegree][yDegree] =
						-negativeCoefficients[xDegree][yDegree];
			}
		}
		implicit.setCoeff(negativeCoefficients);
		implicit.updateRepaint();
		assertEquivalentTargetRepresentation(implicitRich, implicitSnapshot,
				implicitPoint, implicitToken);
	}

	private static Map<String, LocusPoint2D> admissibleTokenSnapshot(
			GeoLocusIntersectionResult rich) {
		Map<String, LocusPoint2D> snapshot = new LinkedHashMap<>();
		for (LocusIntersectionSolution2D solution
				: rich.getIntersectionResult().getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			if (rich.isPointAdmissible(token)) {
				snapshot.put(token, solution.getEvaluatedPoint());
			}
		}
		assertFalse(snapshot.isEmpty());
		return snapshot;
	}

	private static LocusIntersectionResult2D resolveDetached(
			AlgoLocusIntersectionV2 algorithm,
			LocusIntersectionResult2D candidate, String owner) {
		return resolveDetached(null, algorithm, candidate, owner);
	}

	private static LocusIntersectionResult2D resolveDetached(
			LocusIntersectionResult2D previous,
			AlgoLocusIntersectionV2 algorithm,
			LocusIntersectionResult2D candidate, String owner) {
		LocusIntersectionTarget2D captured = LocusIntersectionTargets2D.capture(
				algorithm.getTarget(),
				candidate.getSourceBinding().getTargetIdentity(),
				candidate.getSourceBinding().getTargetUpdateStamp());
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				algorithm.getSource().getSemanticDefinition().getProvider()
						.getProviderId(),
				algorithm.getSource().getSemanticDefinition().getProvider()
						.getParameterDescriptor());
		LocusIntersectionTokenLedger2D ledger =
				new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = ledger.begin(owner,
				candidate.getSourceBinding().getSourcePairIdentity(),
				candidate.getSourceBinding().getConstructiveIntersectionLineage(),
				candidate.getSourceBinding().getTopologyContext());
		LocusIntersectionResult2D resolved =
				new PublicIntersectionRootIdentityResolver2D().resolve(previous,
						candidate, algorithm.getSource().getSemanticDefinition(),
						captured, policy, evaluation);
		ledger.abort(evaluation);
		return resolved;
	}

	private static Map<String, String> rootTokenMap(
			LocusIntersectionResult2D result) {
		Map<String, String> mapping = new LinkedHashMap<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			IntersectionRootRevisionEvidence2D evidence =
					solution.getRevisionEvidence();
			String semantic = evidence.getBranchSnapshotKey() + "|"
					+ evidence.getResolvedValidComponentKey() + "|"
					+ evidence.getCurrentRootGerm().orElseThrow() + "|"
					+ Long.toHexString(Double.doubleToLongBits(
							evidence.getSemanticParameter()));
			assertNull(mapping.put(semantic,
					solution.getIdentity().getRootToken()));
		}
		return Map.copyOf(mapping);
	}

	private static List<List<LocusIntersectionSolution2D>> permutations(
			List<LocusIntersectionSolution2D> input) {
		ArrayList<List<LocusIntersectionSolution2D>> output = new ArrayList<>();
		ArrayList<LocusIntersectionSolution2D> mutable = new ArrayList<>(input);
		permutations(mutable, 0, output);
		return List.copyOf(output);
	}

	private static void permutations(
			ArrayList<LocusIntersectionSolution2D> mutable, int start,
			List<List<LocusIntersectionSolution2D>> output) {
		if (start == mutable.size()) {
			output.add(List.copyOf(mutable));
			return;
		}
		for (int index = start; index < mutable.size(); index++) {
			Collections.swap(mutable, start, index);
			permutations(mutable, start + 1, output);
			Collections.swap(mutable, start, index);
		}
	}

	private static void assertEquivalentTargetRepresentation(
			GeoLocusIntersectionResult rich,
			Map<String, LocusPoint2D> expectedByToken, GeoPoint materialized,
			String selectedToken) {
		for (Map.Entry<String, LocusPoint2D> expected
				: expectedByToken.entrySet()) {
			LocusIntersectionSolution2D current = rich
					.findExactPointAdmissibleSolution(expected.getKey())
					.orElseThrow();
			assertEquals(expected.getValue().getX(),
					current.getEvaluatedPoint().getX(), 1E-9);
			assertEquals(expected.getValue().getY(),
					current.getEvaluatedPoint().getY(), 1E-9);
		}
		assertTrue(materialized.isDefined());
		AlgoLocusIntersectionPointV2 pointAlgorithm =
				(AlgoLocusIntersectionPointV2) materialized.getParentAlgorithm();
		assertEquals(selectedToken, pointAlgorithm.getSelectedRootToken());
		assertEquals(selectedToken, pointAlgorithm.getEffectiveRootToken());
	}

	private GeoPoint materialize(String label, GeoLocusIntersectionResult rich,
			LocusIntersectionSolution2D solution) {
		GeoPoint point = add(label + "=Intersect(" + rich.getLabelSimple()
				+ ",\"" + solution.getIdentity().getRootToken() + "\")");
		assertNotNull(point);
		assertEquals(solution.getEvaluatedPoint().getX(), point.getInhomX(), 1e-9);
		assertEquals(solution.getEvaluatedPoint().getY(), point.getInhomY(), 1e-9);
		return point;
	}

	private static LegacyV1Seed legacyV1Seed(
			GeoLocusIntersectionResult rich,
			LocusIntersectionSolution2D solution) {
		String currentState = rich.getTokenLedgerState();
		String currentToken = solution.getIdentity().getRootToken();
		LocusSemanticIntersectionToken2D.DecodedToken decoded =
				LocusSemanticIntersectionToken2D.decode(currentToken).orElseThrow();
		String branch = decoded.getEstablishedBranchLineage();
		String legacyContinuation =
				"g9u0/g8c1-explicit-unique-local-root/v1/"
						+ branch.length() + ":" + branch;
		String legacySolution = branch + "/solution/" + legacyContinuation;
		IntersectionTokenLineage2D legacyLineage =
				new IntersectionTokenLineage2D(legacySolution, branch,
						Optional.of(legacyContinuation));
		final String legacyToken = LocusSemanticIntersectionToken2D.create(
				decoded.getResultOwnerIdentity(),
				solution.getIdentity().getSourcePairIdentity(),
				solution.getIdentity().getConstructiveIntersectionLineage(),
				solution.getIdentity().getTopologyContext(), legacyLineage,
				decoded.getIncarnation());

		String[] state = currentState.split("\\|", -1);
		assertEquals(4, state.length);
		assertEquals("3", state[0]);
		assertEquals("-", state[3]);
		String[] snapshot = state[2].split("~", -1);
		assertEquals(6, snapshot.length);
		assertEquals("1", snapshot[4]);
		assertEquals(hex(decoded.getResultOwnerIdentity()), snapshot[0]);
		assertEquals(hex(solution.getIdentity().getSourcePairIdentity()),
				snapshot[1]);
		assertEquals(hex(solution.getIdentity()
				.getConstructiveIntersectionLineage()), snapshot[2]);
		assertEquals(hex(solution.getIdentity().getTopologyContext()),
				snapshot[3]);

		String[] entry = snapshot[5].split(",", -1);
		assertEquals(10, entry.length);
		assertEquals(Long.toString(decoded.getIncarnation()), entry[1]);
		assertEquals(hex(branch), entry[3]);
		String legacyEntry = String.join(",", entry[0], entry[1],
				hex(legacySolution), entry[3], hex(legacyContinuation), entry[5],
				entry[6], entry[7]);
		String legacySnapshot = String.join("~", snapshot[0], snapshot[1],
				snapshot[2], snapshot[3], snapshot[4], legacyEntry);
		String legacyState = "1|" + state[1] + "|" + legacySnapshot + "|-";
		return new LegacyV1Seed(currentState, currentToken, legacyState,
				legacyToken);
	}

	private static void assertMigratedLegacyPoint(
			GeoLocusIntersectionResult rich, GeoPoint point, String token) {
		assertTrue(point.isDefined());
		assertTrue(rich.isPointAdmissible(token));
		AlgoLocusIntersectionPointV2 algorithm =
				(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
		assertEquals(token, algorithm.getSelectedRootToken());
		assertEquals(token, algorithm.getEffectiveRootToken());
		assertEquals(token, ((GeoText) algorithm.getInput(1)).getTextString());
		LocusIntersectionSolution2D current = rich
				.findExactPointAdmissibleSolution(token).orElseThrow();
		assertEquals(token, current.getIdentity().getRootToken());
		assertEquals(current.getEvaluatedPoint().getX(), point.getInhomX(), 1E-9);
		assertEquals(current.getEvaluatedPoint().getY(), point.getInhomY(), 1E-9);
	}

	private void assertAuthorFixtureIncidence(GeoLocusV2 locus,
			LocusIntersectionSolution2D solution) {
		double parameter = solution.getRevisionEvidence().getSemanticParameter();
		LocusEvaluation2D evaluation;
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			evaluation = locus.getSemanticDefinition().evaluate(
					solution.getRevisionEvidence().getBranchSnapshotKey(), parameter,
					session);
		}
		assertTrue(evaluation.isValid());
		assertEquals(solution.getEvaluatedPoint().getX(),
				evaluation.getPoint().getX(), 1E-12);
		assertEquals(solution.getEvaluatedPoint().getY(),
				evaluation.getPoint().getY(), 1E-12);

		GeoPoint center = (GeoPoint) requireLookup("A");
		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		LocusPoint2D point = solution.getEvaluatedPoint();
		double radius = Math.hypot(radiusPoint.getInhomX() - center.getInhomX(),
				radiusPoint.getInhomY() - center.getInhomY());
		double distance = Math.hypot(point.getX() - center.getInhomX(),
				point.getY() - center.getInhomY());
		assertEquals(radius, distance, 2E-9);

		TargetResidual2D residual = solution.getRevisionEvidence()
				.getResidualEvidence();
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				locus.getSemanticDefinition().getProvider().getProviderId(),
				locus.getSemanticDefinition().getProvider().getParameterDescriptor(),
				residual.getContract());
		assertTrue(Math.abs(residual.getNormalizedResidual()) <= policy
				.getResidualTolerance().threshold(residual.getCharacteristicScale()));
	}

	private void assertTokenPoints(Map<String, String> pointTokens,
			GeoLocusIntersectionResult rich) {
		for (Map.Entry<String, String> expected : pointTokens.entrySet()) {
			GeoPoint point = (GeoPoint) requireLookup(expected.getKey());
			assertTrue(point.isDefined(), expected.getKey());
			AlgoLocusIntersectionPointV2 algorithm =
					(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
			assertEquals(expected.getValue(), algorithm.getSelectedRootToken(),
					expected.getKey());
			LocusIntersectionSolution2D solution = rich
					.findExactPointAdmissibleSolution(expected.getValue())
					.orElseThrow();
			assertEquals(solution.getEvaluatedPoint().getX(), point.getInhomX(),
					1E-9, expected.getKey());
			assertEquals(solution.getEvaluatedPoint().getY(), point.getInhomY(),
					1E-9, expected.getKey());
		}
	}

	private static Set<String> tokens(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.collect(Collectors.toSet());
	}

	private static String describeRoots(GeoLocusIntersectionResult rich) {
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		return "comparisons=" + result.getWork().getContinuationComparisons()
				+ ", unresolved=" + result.getWork().getUnresolvedCandidates()
				+ ", overlaps=" + result.getOverlapEvidence().size()
				+ ", source=" + result.getSourceBinding().getSourcePairIdentity()
				+ ", lineage=" + result.getSourceBinding()
						.getConstructiveIntersectionLineage()
				+ ", topology=" + result.getSourceBinding().getTopologyContext()
				+ "; " + result.getFiniteSolutions().stream()
				.map(solution -> "u=" + solution.getRevisionEvidence()
						.getSemanticParameter() + ", germ=" + solution
						.getRevisionEvidence().getCurrentRootGerm()
						+ ", identity=" + solution.getIdentity()
								.getIdentityStatus())
				.collect(Collectors.joining(" | "));
	}

	private static String describeFourSolutionEvidence(
			GeoLocusIntersectionResult rich) {
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		String roots = result.getFiniteSolutions().stream()
				.map(solution -> {
					IntersectionRootRevisionEvidence2D revision =
							solution.getRevisionEvidence();
					return "parameter=" + revision.getSemanticParameter()
							+ ", component="
							+ revision.getResolvedValidComponentKey()
							+ ", branch=" + revision.getBranchSnapshotKey()
							+ ", germ=" + revision.getCurrentRootGerm()
							+ ", isolation="
							+ revision.getLocalIsolationStatus()
							+ ", rootGuarantee="
							+ revision.getNumericGuarantee()
							+ ", method=" + revision.getSolverMethod()
							+ ", interval=" + revision.getIsolatingInterval()
							+ ", residual=" + revision.getResidualEvidence()
									.getNormalizedResidual()
							+ ", contact=" + solution.getClassification()
									.getContactClass()
							+ ", multiplicity=" + solution.getClassification()
									.getMultiplicityStatus()
							+ ", domain=" + solution.getClassification()
									.getDomainLocation()
							+ ", regularity=" + solution.getClassification()
									.getSourceRegularity()
							+ ", identity=" + solution.getIdentity()
									.getIdentityStatus()
							+ ", explicitKey=" + solution.getIdentity()
									.getExplicitContinuationKey()
							+ ", lineage=" + solution.getLineage().getEventKind()
							+ ", continuation=" + solution.getLineage()
									.isContinuationEstablished()
							+ ", admissible=" + rich.isPointAdmissible(
									solution.getIdentity().getRootToken())
							+ ", diagnostics=" + solution.getDiagnostics();
				})
				.collect(Collectors.joining(" || "));
		return "status=" + result.getComputationStatus()
				+ ", geometry=" + result.getGeometryKind()
				+ ", currentness=" + result.getCurrentness()
				+ ", completeness=" + result.getCompletenessEvidence()
						.getCompleteness()
				+ ", guarantee=" + result.getNumericGuarantee()
				+ ", unresolved=" + result.getWork().getUnresolvedCandidates()
				+ ", unresolvedComponents="
				+ result.getUnresolvedCandidateComponentKeys()
				+ ", resultDiagnostics=" + result.getDiagnostics()
				+ " :: " + roots;
	}

	private static void moveNumericPreservingTokens(GeoNumeric numeric,
			double target, int steps, GeoLocusIntersectionResult rich,
			Set<String> expectedTokens) {
		double initial = numeric.getDouble();
		for (int step = 1; step <= steps; step++) {
			numeric.setValue(initial + (target - initial) * step / steps);
			numeric.updateCascade();
			assertEquals(expectedTokens, tokens(rich),
					"step=" + step + "; " + describeRoots(rich));
		}
	}

	private static void movePointX(GeoPoint point, double targetX,
			int steps) {
		double initialX = point.getInhomX();
		double y = point.getInhomY();
		for (int step = 1; step <= steps; step++) {
			double nextX = step == steps ? targetX
					: initialX + (targetX - initialX) * step / steps;
			point.setCoords(nextX, y, 1);
			point.updateCascade();
		}
	}

	private static LocusIntersectionResult2D withCompleteness(
			LocusIntersectionResult2D source, Completeness completeness,
			List<LocusIntersectionSolution2D> solutions) {
		CompletenessMethod method = completeness == Completeness.COMPLETE
				? CompletenessMethod.ANALYTIC_ROOT_ENUMERATION
				: completeness == Completeness.INCOMPLETE
						? CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE
						: CompletenessMethod.NOT_ESTABLISHED;
		IntersectionCompletenessEvidence2D evidence =
				new IntersectionCompletenessEvidence2D(completeness, method,
						solutions.size(), source.getCompletenessEvidence()
								.getCoveredComponentKeys(),
						source.getCompletenessEvidence().getDiagnostics());
		return new LocusIntersectionResult2D(source.getSourceBinding(),
				source.getComputationStatus(), evidence, source.getGeometryKind(),
				source.getCurrentness(), source.getSupportLevel(),
				source.getNumericGuarantee(), solutions,
				source.getOverlapEvidence(),
				source.getUnresolvedCandidateComponentKeys(), source.getWork(),
				source.getDiagnostics());
	}

	private static LocusIntersectionResult2D withUnresolvedCandidate(
			LocusIntersectionResult2D source,
			List<String> unresolvedComponentKeys) {
		LocusIntersectionInstrumentationSnapshot2D work = source.getWork();
		LocusIntersectionInstrumentationSnapshot2D mixedWork =
				new LocusIntersectionInstrumentationSnapshot2D(
						work.getSemanticEvaluations(),
						work.getDerivativeEvaluations(),
						work.getTargetEvaluations(),
						work.getTargetDerivativeEvaluations(),
						work.getTargetDomainEvaluations(),
						work.getInvalidTargetEvaluations(),
						work.getCandidateIntervals(),
						work.getIsolationSubdivisions(),
						work.getMaximumIsolationDepth(),
						work.getRefinementCalls(),
						work.getRefinementIterations(),
						work.getResidualVerifications(),
						work.getMembershipChecks(),
						work.getDeduplicationComparisons(),
						work.getContinuationComparisons(),
						work.getVerifiedSolutions(),
						work.getRejectedCandidates(), 1,
						work.getPublishedSnapshots(),
						work.getFailedPrivateComputations(),
						work.getRetainedIndexEntries(),
						work.getRetainedTopologyEpochs(),
						work.getWholeLocusRegenerations(),
						work.getRenderCacheReads(), work.getRenderVertexReads(),
						work.getLegacySampleReads(), work.getViewportReads(),
						work.getPixelToleranceReads(), work.getMetricIndexReads());
		return new LocusIntersectionResult2D(source.getSourceBinding(),
				source.getComputationStatus(), source.getCompletenessEvidence(),
				source.getGeometryKind(), source.getCurrentness(),
				source.getSupportLevel(), source.getNumericGuarantee(),
				source.getFiniteSolutions(), source.getOverlapEvidence(),
				unresolvedComponentKeys, mixedWork, source.getDiagnostics());
	}

	private static LocusIntersectionSolution2D ambiguous(
			LocusIntersectionSolution2D source) {
		IntersectionRootIdentity2D old = source.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				old.getRootToken(), old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(),
				old.getEstablishedBranchLineage(), old.getTopologyContext(),
				Optional.empty(), IdentityStatus.AMBIGUOUS_CONTINUATION);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				LineageEventKind.AMBIGUOUS_EVENT, List.of(),
				List.of(old.getRootToken()), List.of(), false);
		return new LocusIntersectionSolution2D(identity,
				source.getRevisionEvidence(), source.getEvaluatedPoint(),
				source.getClassification(), lineage, source.getDiagnostics(),
				source.getPairEvidence());
	}

	private PathSnapshot runMidpointPath(String seed, MotionPath path)
			throws Exception {
		getApp().setXML(seed, true);
		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		double initialX = radiusPoint.getInhomX();
		double initialY = radiusPoint.getInhomY();
		double finalX = initialX + 0.05;
		applyMotionPath(radiusPoint, initialX, initialY, finalX, path);
		return capturePathSnapshot("R4", "pathPoint", 2, "B");
	}

	private PathSnapshot runFourSolutionPath(String seed, MotionPath path)
			throws Exception {
		getApp().setXML(seed, true);
		GeoPoint radiusPoint = (GeoPoint) requireLookup("B");
		double initialX = radiusPoint.getInhomX();
		double initialY = radiusPoint.getInhomY();
		double finalX = initialX + 0.02;
		applyMotionPath(radiusPoint, initialX, initialY, finalX, path);
		return capturePathSnapshot("b", "fourPathPoint", 4, "B");
	}

	private void applyMotionPath(GeoPoint radiusPoint, double initialX,
			double initialY, double finalX, MotionPath path) {
		switch (path) {
		case DIRECT:
			setPoint(radiusPoint, finalX, initialY);
			break;
		case INCREMENTAL:
			movePointX(radiusPoint, finalX, 64);
			break;
		case FORWARD_REVERSE:
			movePointX(radiusPoint, finalX, 32);
			movePointX(radiusPoint, initialX + 0.02, 24);
			movePointX(radiusPoint, finalX, 32);
			break;
		case REOPENED:
			movePointX(radiusPoint,
					initialX + 0.4 * (finalX - initialX), 16);
			String intermediate = getApp().getXML();
			getApp().setXML(intermediate, true);
			radiusPoint = (GeoPoint) requireLookup("B");
			setPoint(radiusPoint, finalX, initialY);
			break;
		case ODD_INCREMENTS:
			movePointX(radiusPoint, finalX, 17);
			break;
		default:
			throw new IllegalStateException("Unhandled motion path " + path);
		}
	}

	private PathSnapshot capturePathSnapshot(String richLabel, String pointPrefix,
			int pointCount, String movedPointLabel) {
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) requireLookup(richLabel);
		GeoPoint radiusPoint = (GeoPoint) requireLookup(movedPointLabel);
		Map<String, PathBinding> bindings = new LinkedHashMap<>();
		for (int index = 0; index < pointCount; index++) {
			GeoPoint point = (GeoPoint) requireLookup(pointPrefix + index);
			AlgoLocusIntersectionPointV2 algorithm =
					(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
			String token = algorithm.getSelectedRootToken();
			LocusIntersectionSolution2D solution = rich
					.findExactPointAdmissibleSolution(token).orElseThrow();
			GeoText tokenInput = (GeoText) algorithm.getInput(1);
			PathBinding prior = bindings.put(token, new PathBinding(
					point.getLabelSimple(), point.isDefined(),
					rich.isPointAdmissible(token), token,
					algorithm.getEffectiveRootToken(), tokenInput.getTextString(),
					solution.getIdentity().getSourcePairIdentity(),
					solution.getIdentity().getConstructiveIntersectionLineage(),
					solution.getIdentity().getEstablishedBranchLineage(),
					solution.getRevisionEvidence().getBranchSnapshotKey(),
					solution.getRevisionEvidence().getResolvedValidComponentKey(),
					solution.getIdentity().getTopologyContext(),
					solution.getRevisionEvidence().getCurrentRootGerm()
							.orElseThrow(),
					solution.getIdentity().getExplicitContinuationKey()
							.orElseThrow(),
					solution.getIdentity().getIdentityStatus(),
					Double.doubleToLongBits(solution.getRevisionEvidence()
							.getSemanticParameter()),
					Double.doubleToLongBits(solution.getEvaluatedPoint().getX()),
					Double.doubleToLongBits(solution.getEvaluatedPoint().getY()),
					Double.doubleToLongBits(point.getInhomX()),
					Double.doubleToLongBits(point.getInhomY())));
			assertNull(prior, "Duplicate durable token binding");
		}
		return new PathSnapshot(rich.getTokenLedgerState(),
				Double.doubleToLongBits(radiusPoint.getInhomX()),
				Double.doubleToLongBits(radiusPoint.getInhomY()),
				Map.copyOf(bindings));
	}

	private static void setPoint(GeoPoint point, double x, double y) {
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private enum MotionPath {
		DIRECT,
		INCREMENTAL,
		FORWARD_REVERSE,
		REOPENED,
		ODD_INCREMENTS
	}

	private record PathSnapshot(String ledger, long radiusXBits,
			long radiusYBits,
			Map<String, PathBinding> bindings) {
	}

	private record LegacyV1Seed(String currentState, String currentToken,
			String legacyState, String legacyToken) {
	}

	private record PathBinding(String pointLabel, boolean defined,
			boolean pointAdmissible, String selectedToken, String effectiveToken,
			String tokenInput, String sourcePair, String constructiveLineage,
			String establishedBranchLineage, String branchSnapshot,
			String validComponent, String topologyContext, String currentRootGerm,
			String explicitContinuationKey, IdentityStatus identityStatus,
			long parameterBits, long evaluatedXBits, long evaluatedYBits,
			long pointXBits, long pointYBits) {
	}

	private void loadAuthorFixture() throws Exception {
		byte[] archive = readResource(FIXTURE);
		assertEquals(13_301, archive.length);
		assertEquals(FIXTURE_SHA256, sha256(archive));
		getApp().setXML(readZipEntry(archive, "geogebra.xml"), true);
	}

	private void loadFourSolutionFixture() throws Exception {
		byte[] archive = readResource(FOUR_SOLUTION_FIXTURE);
		assertEquals(14_601, archive.length);
		assertEquals(FOUR_SOLUTION_FIXTURE_SHA256, sha256(archive));
		getApp().setXML(readZipEntry(archive, "geogebra.xml"), true);
	}

	private static byte[] readResource(String name) throws IOException {
		try (InputStream input = G9U0R4IntersectionAdmissibilityContinuationTest
				.class.getResourceAsStream(name)) {
			assertNotNull(input, "Missing author fixture " + name);
			return input.readAllBytes();
		}
	}

	private static String readZipEntry(byte[] archive, String name)
			throws IOException {
		try (ZipInputStream zip = new ZipInputStream(
				new java.io.ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (name.equals(entry.getName())) {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					zip.transferTo(output);
					return output.toString(StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing " + name + " in author fixture");
	}

	private static String sha256(byte[] value)
			throws NoSuchAlgorithmException {
		byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
		StringBuilder result = new StringBuilder(digest.length * 2);
		for (byte current : digest) {
			result.append(String.format("%02x", current & 0xff));
		}
		return result.toString();
	}

	private static String hex(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte currentByte : bytes) {
			int current = currentByte & 0xff;
			result.append(Character.forDigit(current >>> 4, 16));
			result.append(Character.forDigit(current & 0x0f, 16));
		}
		return result.toString();
	}
}

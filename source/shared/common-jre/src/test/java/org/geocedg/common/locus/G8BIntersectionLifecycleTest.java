/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidate2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionParameterInterval2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.GeoClass;
import org.junit.jupiter.api.Test;

/** Productive G8B normal-DAG, continuation and consumer lifecycle tests. */
class G8BIntersectionLifecycleTest extends BaseUnitTest {

	@Test
	void richGeoIsNonnumericNormalDagAuthority() {
		DynamicFixture fixture = dynamicFixture("rich-dag");
		GeoLocusIntersectionResult rich = fixture.algorithm().getResult();
		assertTrue(rich.isDefined());
		assertEquals(fixture.algorithm(), rich.getParentAlgorithm());
		assertEquals(fixture.semantic().locus(), fixture.algorithm().getInput(0));
		assertEquals(fixture.target(), fixture.algorithm().getInput(1));
		assertEquals(fixture.height(), fixture.algorithm().getInput(2));
		assertEquals(ValueType.VOID, rich.getValueType());
		assertEquals(GeoClass.LOCUS_INTERSECTION_RESULT,
				rich.getGeoClassType());
		assertFalse(GeoNumeric.class.isAssignableFrom(rich.getClass()));
		assertTrue(getConstruction().getAlgoList().contains(fixture.algorithm()));
	}

	@Test
	void tokenSelectedPointDrivesDownstreamConstructionAndUpdatesNormally() {
		DynamicFixture fixture = dynamicFixture("downstream");
		String token = onlyToken(fixture.algorithm());
		AlgoLocusIntersectionPointV2 selected =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						fixture.algorithm().getResult(), token);
		DownstreamPointAlgo downstream = new DownstreamPointAlgo(getConstruction(),
				selected.getPoint());
		assertEquals(0, selected.getPoint().getInhomX(), 0);
		assertEquals(1, downstream.getPoint().getInhomX(), 0);
		assertEquals(2, downstream.getPoint().getInhomY(), 0);

		long revision = fixture.semantic().locus().getSemanticRevision();
		fixture.semantic().source().setValue(0.5);
		fixture.semantic().source().updateCascade();
		assertEquals(revision + 1,
				fixture.semantic().locus().getSemanticRevision());
		assertEquals(token, onlyToken(fixture.algorithm()));
		assertEquals(-0.5, selected.getPoint().getInhomX(), 1E-15);
		assertEquals(0, selected.getPoint().getInhomY(), 1E-15);
		assertEquals(0.5, downstream.getPoint().getInhomX(), 1E-15);

		fixture.height().setValue(0.25);
		fixture.height().updateCascade();
		assertEquals(token, onlyToken(fixture.algorithm()));
		assertEquals(-0.25, selected.getPoint().getInhomX(), 1E-15);
		assertEquals(0.25, selected.getPoint().getInhomY(), 1E-15);
		assertEquals(2.25, downstream.getPoint().getInhomY(), 1E-15);
	}

	@Test
	void pointDoesNotRetargetWhenOnlyCoordinateMatches() {
		G8BIntersectionFixtures.Fixture semantic = lineLocus("no-retarget");
		GeoLine target = add("y=0");
		GeoNumeric mode = new GeoNumeric(getConstruction(), 0);
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("identity-switch/v1", context -> {
					String key = mode.getDouble() == 0 ? "root-a" : "root-b";
					return G8BIntersectionFixtures.completeRoots(context,
							List.of(G8BIntersectionFixtures.Root.simple(
									semantic.branchKey(), semantic.componentKey(), 0,
									key)));
				});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"no-retarget", capability, mode);
		String firstToken = onlyToken(algorithm);
		AlgoLocusIntersectionPointV2 selected =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), firstToken);
		assertTrue(selected.getPoint().isDefined());
		mode.setValue(1);
		mode.updateCascade();
		assertNotEquals(firstToken, onlyToken(algorithm));
		assertFalse(selected.getPoint().isDefined());
		assertEquals(0, algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().get(0).getEvaluatedPoint().getX(), 0);
	}

	@Test
	void selectedTokenIsUndefinedDuringAbsenceOrFailureAndRecoversByProof() {
		G8BIntersectionFixtures.Fixture semantic = lineLocus("recovery");
		GeoLine target = add("y=0");
		GeoNumeric state = new GeoNumeric(getConstruction(), 0);
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("recovery/v1", context -> {
					if (state.getDouble() == 1) {
						return G8BIntersectionFixtures.completeEmpty(context);
					}
					if (state.getDouble() == 3) {
						throw new IllegalStateException("injected private failure");
					}
					return G8BIntersectionFixtures.completeRoots(context,
							List.of(G8BIntersectionFixtures.Root.simple(
									semantic.branchKey(), semantic.componentKey(), 0,
									"stable-root")));
				});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"recovery", capability, state);
		String token = onlyToken(algorithm);
		AlgoLocusIntersectionPointV2 selected =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), token);

		state.setValue(1);
		state.updateCascade();
		assertEquals(GeometryKind.EMPTY,
				algorithm.getResult().getIntersectionResult().getGeometryKind());
		assertFalse(selected.getPoint().isDefined());
		state.setValue(2);
		state.updateCascade();
		assertEquals(token, onlyToken(algorithm));
		assertTrue(selected.getPoint().isDefined());

		state.setValue(3);
		state.updateCascade();
		LocusIntersectionResult2D failure =
				algorithm.getResult().getIntersectionResult();
		assertEquals(ComputationStatus.NUMERICAL_FAILURE,
				failure.getComputationStatus());
		assertEquals(GeometryKind.UNRESOLVED, failure.getGeometryKind());
		assertEquals(0, failure.getFiniteSolutions().size());
		assertFalse(selected.getPoint().isDefined());
		state.setValue(4);
		state.updateCascade();
		assertEquals(token, onlyToken(algorithm));
		assertTrue(selected.getPoint().isDefined());
	}

	@Test
	void incompleteFiniteVerifiedRootRemainsPointAdmissible() {
		G8BIntersectionFixtures.Fixture semantic = lineLocus("incomplete-point");
		GeoLine target = add("y=0");
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("incomplete-point/v1", context ->
						new IntersectionCandidateSet2D(Completeness.INCOMPLETE,
								CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE,
								GeometryKind.FINITE, SupportLevel.CERTIFIED,
								NumericGuarantee.CERTIFIED_ERROR_BOUND,
								context.getAllComponentKeys(),
								List.of(candidate(semantic, 0, "known-root",
										LineageEventKind.APPEARED, List.of())),
								List.of(), List.of()));
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"incomplete-point", capability);
		LocusIntersectionResult2D result =
				algorithm.getResult().getIntersectionResult();
		assertEquals(Completeness.INCOMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		String token = result.getFiniteSolutions().get(0).getIdentity()
				.getRootToken();
		AlgoLocusIntersectionPointV2 selected =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), token);
		assertTrue(selected.getPoint().isDefined());
		assertTrue(algorithm.getResult().isPointAdmissible(token));
		assertEquals(Completeness.INCOMPLETE, selected.getRichInput()
				.getIntersectionResult().getCompletenessEvidence().getCompleteness());
	}

	@Test
	void notEstablishedFiniteVerifiedRootKeepsCompletenessProvenance() {
		G8BIntersectionFixtures.Fixture semantic = G8BIntersectionFixtures.single(
				getConstruction(), "not-established-point", -2, 2, true, true,
				false, (source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter));
		GeoLine target = add("y=0");
		LocusIntersectionCapability2D capability = G8BIntersectionFixtures
				.capability("not-established-point/v1", context ->
						G8BIntersectionFixtures.roots(context,
								List.of(G8BIntersectionFixtures.Root.tangent(
										semantic.branchKey(), semantic.componentKey(), 0,
										"verified-root", 2)),
								Completeness.NOT_ESTABLISHED));
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"not-established-point", capability);
		String token = onlyToken(algorithm);
		AlgoLocusIntersectionPointV2 selected =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), token);

		assertTrue(selected.getPoint().isDefined());
		assertEquals(Completeness.NOT_ESTABLISHED, selected.getRichInput()
				.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		assertEquals(LocalIsolationStatus.ESTABLISHED,
				algorithm.getResult().getIntersectionResult().getFiniteSolutions()
						.get(0).getRevisionEvidence().getLocalIsolationStatus());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				algorithm.getResult().getIntersectionResult().getFiniteSolutions()
						.get(0).getClassification().getContactClass());
	}

	@Test
	void localizationOnlyAndFailedResidualCannotDefinePoint() {
		G8BIntersectionFixtures.Fixture semantic = lineLocus("local-evidence");
		LocusIntersectionCapability2D localizationOnly =
				G8BIntersectionFixtures.capability("localization-only/v1", context ->
						new IntersectionCandidateSet2D(
								Completeness.NOT_ESTABLISHED,
								CompletenessMethod.NOT_ESTABLISHED,
								GeometryKind.FINITE,
								SupportLevel.VERIFIED_UNCERTIFIED,
								NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
								context.getAllComponentKeys(),
								List.of(candidate(semantic, 0, "localized-root",
										LineageEventKind.APPEARED, List.of(),
										LocalIsolationStatus.NOT_ESTABLISHED)),
								List.of(), List.of()));
		AlgoLocusIntersectionV2 localized = algorithm(semantic, add("y=0"),
				"localization-only", localizationOnly);
		String localizedToken = onlyToken(localized);
		AlgoLocusIntersectionPointV2 localizedPoint =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						localized.getResult(), localizedToken);
		assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
				localized.getResult().getIntersectionResult().getFiniteSolutions()
						.get(0).getRevisionEvidence().getLocalIsolationStatus());
		assertFalse(localizedPoint.getPoint().isDefined());

		LocusIntersectionCapability2D failedResidual =
				G8BIntersectionFixtures.capability("failed-residual/v1", context ->
						G8BIntersectionFixtures.roots(context,
								List.of(G8BIntersectionFixtures.Root.simple(
										semantic.branchKey(), semantic.componentKey(), 0,
										"false-candidate")),
								Completeness.NOT_ESTABLISHED));
		AlgoLocusIntersectionV2 rejected = algorithm(semantic, add("y=1"),
				"failed-residual", failedResidual);
		assertEquals(GeometryKind.UNRESOLVED,
				rejected.getResult().getIntersectionResult().getGeometryKind());
		assertTrue(rejected.getResult().getIntersectionResult()
				.getFiniteSolutions().isEmpty());
		AlgoLocusIntersectionPointV2 rejectedPoint =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						rejected.getResult(), "false-candidate-token");
		assertFalse(rejectedPoint.getPoint().isDefined());
	}

	@Test
	void notEstablishedWithoutVerifiedRootIsUnresolvedAndHasNoPoint() {
		G8BIntersectionFixtures.Fixture semantic = lineLocus("unknown-empty");
		LocusIntersectionCapability2D capability = G8BIntersectionFixtures
				.capability("unknown-empty/v1", context ->
						new IntersectionCandidateSet2D(
								Completeness.NOT_ESTABLISHED,
								CompletenessMethod.NOT_ESTABLISHED,
								GeometryKind.UNRESOLVED,
								SupportLevel.VERIFIED_UNCERTIFIED,
								NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
								List.of(), List.of(), List.of(), List.of()));
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, add("y=0"),
				"unknown-empty", capability);
		assertEquals(GeometryKind.UNRESOLVED,
				algorithm.getResult().getIntersectionResult().getGeometryKind());
		assertEquals(Completeness.NOT_ESTABLISHED, algorithm.getResult()
				.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		AlgoLocusIntersectionPointV2 point = new AlgoLocusIntersectionPointV2(
				getConstruction(), algorithm.getResult(), "absent-token");
		assertFalse(point.getPoint().isDefined());
	}

	@Test
	void newlyDiscoveredRootAndOrderingCannotRetargetExistingTokens() {
		G8BIntersectionFixtures.Fixture semantic = G8BIntersectionFixtures.single(
				getConstruction(), "new-root", -2, 2, true, true, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter,
						(parameter + 1) * parameter * (parameter - 1)));
		GeoNumeric phase = new GeoNumeric(getConstruction(), 0);
		LocusIntersectionCapability2D capability = G8BIntersectionFixtures
				.capability("new-root/v1", context -> {
					List<G8BIntersectionFixtures.Root> roots = phase.getDouble() == 0
							? List.of(
									G8BIntersectionFixtures.Root.simple(
											semantic.branchKey(), semantic.componentKey(),
											-1, "root-a"),
									G8BIntersectionFixtures.Root.simple(
											semantic.branchKey(), semantic.componentKey(),
											1, "root-b"))
							: List.of(
									G8BIntersectionFixtures.Root.simple(
											semantic.branchKey(), semantic.componentKey(),
											0, "root-c"),
									G8BIntersectionFixtures.Root.simple(
											semantic.branchKey(), semantic.componentKey(),
											1, "root-b"),
									G8BIntersectionFixtures.Root.simple(
											semantic.branchKey(), semantic.componentKey(),
											-1, "root-a"));
					return G8BIntersectionFixtures.roots(context, roots,
							Completeness.NOT_ESTABLISHED);
				});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, add("y=0"),
				"new-root", capability, phase);
		String tokenA = tokenForKey(algorithm, "root-a");
		String tokenB = tokenForKey(algorithm, "root-b");
		AlgoLocusIntersectionPointV2 pointA = new AlgoLocusIntersectionPointV2(
				getConstruction(), algorithm.getResult(), tokenA);
		final AlgoLocusIntersectionPointV2 pointB = new AlgoLocusIntersectionPointV2(
				getConstruction(), algorithm.getResult(), tokenB);

		phase.setValue(1);
		phase.updateCascade();
		assertEquals(3, algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().size());
		assertEquals(tokenA, tokenForKey(algorithm, "root-a"));
		assertEquals(tokenB, tokenForKey(algorithm, "root-b"));
		assertNotEquals(tokenA, tokenForKey(algorithm, "root-c"));
		assertEquals(-1, pointA.getPoint().getInhomX(), 0);
		assertEquals(1, pointB.getPoint().getInhomX(), 0);
		assertEquals(Completeness.NOT_ESTABLISHED, pointA.getRichInput()
				.getIntersectionResult().getCompletenessEvidence().getCompleteness());
	}

	@Test
	void equalCoordinatesFromDistinctPreimagesKeepDistinctAdmissibleTokens() {
		G8BIntersectionFixtures.Fixture semantic = G8BIntersectionFixtures.single(
				getConstruction(), "equal-preimages", -2, 2, true, true, false,
				(source, branch, parameter) -> new LocusPoint2D(
						parameter * parameter - 1,
						parameter * parameter - 1));
		LocusIntersectionCapability2D capability = G8BIntersectionFixtures
				.capability("equal-preimages/v1", context ->
						G8BIntersectionFixtures.roots(context, List.of(
								G8BIntersectionFixtures.Root.simple(
										semantic.branchKey(), semantic.componentKey(), -1,
										"negative-preimage"),
								G8BIntersectionFixtures.Root.simple(
										semantic.branchKey(), semantic.componentKey(), 1,
										"positive-preimage")),
								Completeness.NOT_ESTABLISHED));
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, add("y=0"),
				"equal-preimages", capability);
		String negative = tokenForKey(algorithm, "negative-preimage");
		String positive = tokenForKey(algorithm, "positive-preimage");
		assertNotEquals(negative, positive);
		AlgoLocusIntersectionPointV2 first = new AlgoLocusIntersectionPointV2(
				getConstruction(), algorithm.getResult(), negative);
		AlgoLocusIntersectionPointV2 second = new AlgoLocusIntersectionPointV2(
				getConstruction(), algorithm.getResult(), positive);
		assertTrue(first.getPoint().isDefined());
		assertTrue(second.getPoint().isDefined());
		assertEquals(first.getPoint().getInhomX(), second.getPoint().getInhomX(), 0);
		assertEquals(first.getPoint().getInhomY(), second.getPoint().getInhomY(), 0);
	}

	@Test
	void nonCurrentResultCannotAdmitAnOtherwiseVerifiedToken() {
		DynamicFixture fixture = dynamicFixture("non-current");
		LocusIntersectionResult2D current =
				fixture.algorithm().getResult().getIntersectionResult();
		String token = onlyToken(fixture.algorithm());
		LocusIntersectionResult2D stale = new LocusIntersectionResult2D(
				current.getSourceBinding(), current.getComputationStatus(),
				current.getCompletenessEvidence(), current.getGeometryKind(),
				Currentness.NON_CURRENT, current.getSupportLevel(),
				current.getNumericGuarantee(), current.getFiniteSolutions(),
				current.getOverlapEvidence(), current.getWork(),
				current.getDiagnostics());
		assertFalse(stale.findPointAdmissibleSolution(token).isPresent());
	}

	@Test
	void monotoneScalingAndOrientationReversalPreserveDurableToken() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), "reparameterized",
						-2, 2, true, true, false,
						(source, branch, parameter) ->
								new LocusPoint2D(parameter / source, 0));
		semantic.source().setValue(1);
		semantic.source().updateCascade();
		GeoLine target = add("x=0.25");
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("reparameterization/v1", context ->
						G8BIntersectionFixtures.completeRoots(context,
								List.of(G8BIntersectionFixtures.Root.simple(
										semantic.branchKey(), semantic.componentKey(),
										0.25 * semantic.source().getDouble(),
										"physical-quarter"))));
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"reparameterization", capability);
		String token = onlyToken(algorithm);
		assertEquals(0.25, parameter(algorithm), 0);

		semantic.source().setValue(2);
		semantic.source().updateCascade();
		assertEquals(token, onlyToken(algorithm));
		assertEquals(0.5, parameter(algorithm), 0);
		assertEquals(0.25, point(algorithm).getX(), 1E-15);
		semantic.source().setValue(-1);
		semantic.source().updateCascade();
		assertEquals(token, onlyToken(algorithm));
		assertEquals(-0.25, parameter(algorithm), 0);
		assertEquals(0.25, point(algorithm).getX(), 1E-15);
		assertEquals(IdentityStatus.CONTINUATION_ESTABLISHED,
				algorithm.getResult().getIntersectionResult().getFiniteSolutions()
						.get(0).getIdentity().getIdentityStatus());
	}

	@Test
	void periodicSeamRepresentationChangesEvidenceNotIdentity() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), "periodic-identity",
						0, 2 * Math.PI, true, true, true,
						(source, branch, parameter) -> new LocusPoint2D(
								Math.cos(parameter), Math.sin(parameter)));
		GeoNumeric representation = new GeoNumeric(getConstruction(), 0);
		GeoLine target = add("x=1");
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("periodic-representation/v1",
						context -> {
							double parameter = representation.getDouble() == 0
									? 0 : 2 * Math.PI;
							double radius = context.getQuery().getPolicy()
									.getRootParameterTolerance().getValue();
							IntersectionCandidate2D candidate =
									new IntersectionCandidate2D(semantic.branchKey(),
											semantic.componentKey(), parameter,
											parameter == 0 ? OptionalDouble.empty()
													: OptionalDouble.of(parameter),
											new IntersectionParameterInterval2D(
													parameter - radius, parameter + radius),
											LocalIsolationStatus.ESTABLISHED,
											Optional.of("periodic-seam-root"),
											ContactClass.TANGENT_ESTABLISHED,
											MultiplicityStatus.ESTABLISHED,
											OptionalInt.of(2),
											SolverMethod.ANALYTIC_ROOT_ENUMERATION,
											NumericGuarantee.CERTIFIED_ERROR_BOUND,
											LineageEventKind.APPEARED, List.of(),
											List.of());
							return new IntersectionCandidateSet2D(
									Completeness.NOT_ESTABLISHED,
									CompletenessMethod.NOT_ESTABLISHED,
									GeometryKind.FINITE, SupportLevel.EXACT_CAPABILITY,
									NumericGuarantee.CERTIFIED_ERROR_BOUND,
									context.getAllComponentKeys(), List.of(candidate),
									List.of(), List.of());
						});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"periodic-identity", capability, representation);
		String token = onlyToken(algorithm);
		AlgoLocusIntersectionPointV2 selected =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), token);
		assertTrue(selected.getPoint().isDefined());
		assertEquals(Completeness.NOT_ESTABLISHED, algorithm.getResult()
				.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		assertEquals(0, parameter(algorithm), 0);
		assertFalse(algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().get(0).getRevisionEvidence()
				.getLiftedPeriodicParameter().isPresent());

		representation.setValue(1);
		representation.updateCascade();
		assertEquals(token, onlyToken(algorithm));
		assertTrue(selected.getPoint().isDefined());
		assertEquals(0, parameter(algorithm), 0);
		assertEquals(2 * Math.PI, algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().get(0).getRevisionEvidence()
				.getLiftedPeriodicParameter().orElseThrow(), 0);
	}

	@Test
	void mergeSplitPublishesCandidateGenealogyWithoutUniversalInheritance() {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), "merge-split",
						-2, 2, true, true, false,
						(source, branch, parameter) ->
								new LocusPoint2D(parameter, parameter * parameter));
		GeoNumeric height = add("g8bMergeHeight=1");
		GeoNumeric phase = new GeoNumeric(getConstruction(), 0);
		GeoLine target = add("y=g8bMergeHeight");
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("merge-split/v1", context -> {
					double h = height.getDouble();
					if (h == 0) {
						G8BIntersectionFixtures.Root tangent =
								new G8BIntersectionFixtures.Root(
										semantic.branchKey(), semantic.componentKey(), 0,
										"tangent", 2,
										ContactClass.TANGENT_ESTABLISHED,
										LineageEventKind.MERGE_CANDIDATE,
										List.of("left", "right"));
						return G8BIntersectionFixtures.roots(context,
								List.of(tangent), Completeness.NOT_ESTABLISHED);
					}
					double root = Math.sqrt(h);
					LineageEventKind event = phase.getDouble() == 0
							? LineageEventKind.APPEARED
							: LineageEventKind.SPLIT_CANDIDATE;
					List<String> parents = phase.getDouble() == 0 ? List.of()
							: List.of("tangent");
					return G8BIntersectionFixtures.roots(context, List.of(
							new G8BIntersectionFixtures.Root(semantic.branchKey(),
									semantic.componentKey(), -root, "left", 1,
									ContactClass.TRANSVERSE_ESTABLISHED, event, parents),
							new G8BIntersectionFixtures.Root(semantic.branchKey(),
									semantic.componentKey(), root, "right", 1,
									ContactClass.TRANSVERSE_ESTABLISHED, event, parents)),
							Completeness.NOT_ESTABLISHED);
				});
		AlgoLocusIntersectionV2 algorithm = algorithm(semantic, target,
				"merge-split", capability, height, phase);
		List<String> firstTokens = tokens(algorithm);
		assertEquals(2, firstTokens.size());
		AlgoLocusIntersectionPointV2 selectedLeft =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), firstTokens.get(0));
		assertTrue(selectedLeft.getPoint().isDefined());

		phase.setValue(1);
		height.setValue(0);
		height.updateCascade();
		LocusIntersectionResult2D merged =
				algorithm.getResult().getIntersectionResult();
		String tangentToken = onlyToken(algorithm);
		assertFalse(firstTokens.contains(tangentToken));
		assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
				merged.getFiniteSolutions().get(0).getIdentity()
						.getIdentityStatus());
		assertEquals(firstTokens, merged.getFiniteSolutions().get(0)
				.getLineage().getCandidateParentTokens());
		assertFalse(selectedLeft.getPoint().isDefined());
		AlgoLocusIntersectionPointV2 selectedTangent =
				new AlgoLocusIntersectionPointV2(getConstruction(),
						algorithm.getResult(), tangentToken);
		assertFalse(selectedTangent.getPoint().isDefined());

		phase.setValue(2);
		height.setValue(1);
		height.updateCascade();
		LocusIntersectionResult2D split =
				algorithm.getResult().getIntersectionResult();
		assertEquals(2, split.getFiniteSolutions().size());
		for (var solution : split.getFiniteSolutions()) {
			assertNotEquals(tangentToken, solution.getIdentity().getRootToken());
			assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
					solution.getIdentity().getIdentityStatus());
			assertEquals(List.of(tangentToken),
					solution.getLineage().getCandidateParentTokens());
		}
		assertFalse(selectedLeft.getPoint().isDefined());
		assertFalse(selectedTangent.getPoint().isDefined());

		phase.setValue(3);
		height.setValue(0);
		height.updateCascade();
		assertEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
				algorithm.getResult().getIntersectionResult().getFiniteSolutions()
						.get(0).getIdentity().getIdentityStatus());

		phase.setValue(0);
		height.setValue(1);
		height.updateCascade();
		assertFalse(selectedLeft.getPoint().isDefined());
		assertFalse(selectedTangent.getPoint().isDefined());
		assertTrue(algorithm.getResult().getIntersectionResult()
				.getFiniteSolutions().stream().allMatch(solution ->
						solution.getIdentity().getIdentityStatus()
								== IdentityStatus.NEW_TOPOLOGICAL_SOLUTION));
	}

	@Test
	void copySetRemovalXmlAndManyConsumersStayBoundedAndInternal() {
		DynamicFixture fixture = dynamicFixture("lifecycle-boundaries");
		GeoLocusIntersectionResult rich = fixture.algorithm().getResult();
		GeoLocusIntersectionResult copy = (GeoLocusIntersectionResult)
				rich.copyInternal(getConstruction());
		assertFalse(copy.isDefined());
		copy.set(rich);
		assertFalse(copy.isDefined());
		rich.setLabel("g8bInternalIntersectionResult");
		assertEquals("", rich.getXML());
		assertFalse(getApp().getXML().contains("g8bInternalIntersectionResult"));

		String token = onlyToken(fixture.algorithm());
		long evaluations = rich.getIntersectionResult().getWork()
				.getSemanticEvaluations();
		List<AlgoLocusIntersectionPointV2> consumers = new ArrayList<>();
		for (int index = 0; index < 100; index++) {
			consumers.add(new AlgoLocusIntersectionPointV2(getConstruction(), rich,
					token));
		}
		assertTrue(consumers.stream().allMatch(
				consumer -> consumer.getPoint().isDefined()));
		assertEquals(evaluations,
				rich.getIntersectionResult().getWork().getSemanticEvaluations());
		assertEquals(0, rich.getIntersectionResult().getWork()
				.getRetainedIndexEntries());
		assertTrue(rich.getIntersectionResult().getWork()
				.getRetainedTopologyEpochs() <= 2);
		assertTrue(rich.getIntersectionResult().getWork()
				.hasZeroForbiddenAuthorityReads());
		for (AlgoLocusIntersectionPointV2 consumer : consumers) {
			consumer.remove();
		}
		fixture.algorithm().remove();
		assertFalse(getConstruction().getAlgoList().contains(fixture.algorithm()));
		assertFalse(rich.isDefined());
	}

	private DynamicFixture dynamicFixture(String identity) {
		G8BIntersectionFixtures.Fixture semantic =
				G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
						true, true, false,
						(source, branch, parameter) ->
								new LocusPoint2D(parameter, source + parameter));
		GeoNumeric height = add("g8bHeight=0");
		GeoLine target = add("y=g8bHeight");
		LocusIntersectionCapability2D capability =
				G8BIntersectionFixtures.capability("dynamic-root/v1", context -> {
					double root = height.getDouble()
							- semantic.source().getDouble();
					if (root < -2 || root > 2) {
						return G8BIntersectionFixtures.completeEmpty(context);
					}
					return G8BIntersectionFixtures.roots(context,
							List.of(G8BIntersectionFixtures.Root.simple(
									semantic.branchKey(), semantic.componentKey(), root,
									"dynamic-root")),
							Completeness.NOT_ESTABLISHED);
				});
		return new DynamicFixture(semantic, height, target,
				algorithm(semantic, target, identity, capability, height));
	}

	private G8BIntersectionFixtures.Fixture lineLocus(String identity) {
		return G8BIntersectionFixtures.single(getConstruction(), identity, -2, 2,
				true, true, false,
				(source, branch, parameter) -> new LocusPoint2D(parameter, parameter));
	}

	private AlgoLocusIntersectionV2 algorithm(
			G8BIntersectionFixtures.Fixture semantic, GeoElement target,
			String identity, LocusIntersectionCapability2D capability,
			GeoElement... dependencies) {
		return new AlgoLocusIntersectionV2(getConstruction(), semantic.locus(),
				target, identity + "/pair", identity + "/lineage",
				identity + "/target", identity + "/topology-v1", capability,
				dependencies);
	}

	private static IntersectionCandidate2D candidate(
			G8BIntersectionFixtures.Fixture semantic, double parameter, String key,
			LineageEventKind event, List<String> parents) {
		return candidate(semantic, parameter, key, event, parents,
				LocalIsolationStatus.ESTABLISHED);
	}

	private static IntersectionCandidate2D candidate(
			G8BIntersectionFixtures.Fixture semantic, double parameter, String key,
			LineageEventKind event, List<String> parents,
			LocalIsolationStatus localIsolationStatus) {
		return new IntersectionCandidate2D(semantic.branchKey(),
				semantic.componentKey(), parameter, OptionalDouble.empty(),
				new IntersectionParameterInterval2D(parameter - 1E-12,
						parameter + 1E-12), localIsolationStatus,
				Optional.of(key),
				ContactClass.TRANSVERSE_ESTABLISHED,
				MultiplicityStatus.ESTABLISHED, OptionalInt.of(1),
				SolverMethod.CERTIFIED_INTERVAL,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, event, parents,
				Collections.emptyList());
	}

	private static String onlyToken(AlgoLocusIntersectionV2 algorithm) {
		return algorithm.getResult().getIntersectionResult().getFiniteSolutions()
				.get(0).getIdentity().getRootToken();
	}

	private static List<String> tokens(AlgoLocusIntersectionV2 algorithm) {
		return algorithm.getResult().getIntersectionResult().getFiniteSolutions()
				.stream().map(solution -> solution.getIdentity().getRootToken())
				.toList();
	}

	private static String tokenForKey(AlgoLocusIntersectionV2 algorithm,
			String continuationKey) {
		return algorithm.getResult().getIntersectionResult().getFiniteSolutions()
				.stream().filter(solution -> solution.getIdentity()
						.getExplicitContinuationKey().filter(continuationKey::equals)
						.isPresent())
				.map(solution -> solution.getIdentity().getRootToken()).findFirst()
				.orElseThrow();
	}

	private static double parameter(AlgoLocusIntersectionV2 algorithm) {
		return algorithm.getResult().getIntersectionResult().getFiniteSolutions()
				.get(0).getRevisionEvidence().getSemanticParameter();
	}

	private static LocusPoint2D point(AlgoLocusIntersectionV2 algorithm) {
		return algorithm.getResult().getIntersectionResult().getFiniteSolutions()
				.get(0).getEvaluatedPoint();
	}

	private record DynamicFixture(G8BIntersectionFixtures.Fixture semantic,
			GeoNumeric height, GeoLine target,
			AlgoLocusIntersectionV2 algorithm) {
	}

	private static final class DownstreamPointAlgo extends AlgoElement {
		private final GeoPoint inputPoint;
		private final GeoPoint point;

		DownstreamPointAlgo(Construction construction, GeoPoint inputPoint) {
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

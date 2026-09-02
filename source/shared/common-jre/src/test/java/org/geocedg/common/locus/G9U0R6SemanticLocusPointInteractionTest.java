/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedAffineLocus2D;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticAddressState2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionCandidate2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D.SearchCoverage;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/** Focused kernel acceptance for G9U0-R6 semantic Locus V2 point interaction. */
final class G9U0R6SemanticLocusPointInteractionTest
		extends G9U0PublicSurfaceTestBase {

	private static final String SPLINE_BRANCH = "spline-v2/main";
	private final LocusPointInteractionResolver2D resolver =
			new LocusPointInteractionResolver2D();

	@Test
	void r601SplinePolynomialInverseIsUniqueAndCarriesExactAddressEvidence() {
		GeoLocusV2 spline = createLineSpline("S");
		LocusPointInteractionResult2D result = resolve(spline, 0.25, 0.1, 0.2);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus());
		assertEquals(SearchCoverage.ALL_EXPLICIT_POLYNOMIAL_SPANS,
				result.getSearchCoverage());
		LocusPointInteractionCandidate2D candidate = result.getUniqueCandidate();
		assertNotNull(candidate);
		assertEquals(SPLINE_BRANCH, candidate.getAddress().getBranchKey());
		assertEquals(spline.getPersistentLocusId(),
				candidate.getAddress().getSourceLocusId());
		assertEquals(spline.getSemanticRevision(), candidate.getSourceRevision());
		assertEquals(0.1, candidate.getWorldDistance(), 1E-9);
		assertTrue(candidate.getMethod().startsWith("piecewise-polynomial"));
		assertNoPresentationReads(result);
	}

	@Test
	void r602SplineKnotHasOneCanonicalOwnerIndependentOfSpanEnumeration() {
		GeoLocusV2 spline = createFivePointLineSpline("S");
		LocusPoint2D knot = evaluate(spline, 0.5);
		LocusPointInteractionResult2D first = resolve(spline, knot.getX(),
				knot.getY(), 1E-6);
		LocusPointInteractionResult2D second = resolve(spline, knot.getX(),
				knot.getY(), 1E-6);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				first.getStatus());
		assertEquals(0.5, first.getUniqueCandidate().getAddress()
				.getCanonicalParameter(), 1E-10);
		assertEquals(first.getUniqueCandidate().getAddress(),
				second.getUniqueCandidate().getAddress());
		assertEquals(first.getInstrumentation().getPolynomialSpans(),
				second.getInstrumentation().getPolynomialSpans());
	}

	@Test
	void r603SelfIntersectionReturnsAllExactPreimagesAndFailsClosed() {
		GeoLocusV2 spline = createSelfIntersectingSpline("S");
		LocusPoint2D crossing = evaluate(spline, 0.25);
		LocusPointInteractionResult2D result = resolve(spline, crossing.getX(),
				crossing.getY(), 1E-6);

		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				result.getStatus(), result.getDiagnostic());
		assertTrue(result.getCandidates().size() >= 2);
		assertTrue(result.getCandidates().stream().anyMatch(candidate -> Math.abs(
				candidate.getAddress().getCanonicalParameter() - 0.25) < 1E-8));
		assertTrue(result.getCandidates().stream().anyMatch(candidate -> Math.abs(
				candidate.getAddress().getCanonicalParameter() - 0.75) < 1E-8));
		assertNull(result.getUniqueCandidate());
	}

	@Test
	void r604PolynomialOutsideWorldThresholdHasNoAdmissiblePreimage() {
		GeoLocusV2 spline = createLineSpline("S");
		LocusPointInteractionResult2D result = resolve(spline, 0, 5, 0.01);

		assertEquals(LocusPointInteractionStatus2D.NO_ADMISSIBLE_PREIMAGE,
				result.getStatus());
		assertTrue(result.getCandidates().isEmpty());
		assertTrue(result.getDiagnostic().contains("outside the world threshold"));
	}

	@Test
	void r605EvaluatorFallbackIsBoundedAndBudgetExhaustionIsTyped() {
		GeoLocusV2 locus = createLine();
		LocusPointInteractionResult2D regular = resolve(locus, 0.4, 0.05, 0.1);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				regular.getStatus());
		assertEquals(SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS,
				regular.getSearchCoverage());
		assertEquals(0, regular.getInstrumentation().getSubdivisions());
		assertEquals(0, regular.getInstrumentation().getRefinementIterations());
		assertTrue(regular.getUniqueCandidate().getMethod()
				.startsWith("certified-affine-projection"));

		LocusPointInteractionPolicy2D tightPolicy =
				new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
						new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
		LocusPointInteractionResult2D tight = resolver.resolve(
				new LocusPointInteractionQuery2D(locus, 0.4, 0,
						tightPolicy));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				tight.getStatus(), tight.getDiagnostic());
		assertEquals(SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS,
				tight.getSearchCoverage());
		assertEquals(regular.getUniqueCandidate().getAddress(),
				tight.getUniqueCandidate().getAddress());
		assertEquals(1, tight.getInstrumentation().getSemanticEvaluations());
		assertEquals(0, tight.getInstrumentation().getSubdivisions());
		assertNoPresentationReads(tight);
	}

	@Test
	void r606ExplicitSemanticPointIsNotInteractionOwnedAndCannotBeMoved() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint explicit = add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.25)");

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> LocusV2PublicOperations.moveInteractiveSemanticPoint(explicit,
						0, 0, policy(0.2)));
		assertTrue(exception.getMessage().contains("interaction-owned"));
		assertEquals(0.25, semanticParent(explicit).getSemanticAddress()
				.getCanonicalParameter(), 0);
	}

	@Test
	void r607InteractiveCreationAndMovePreservePointIdentityAndExactOwnership() {
		GeoLocusV2 spline = createLineSpline("S");
		LocusPointInteractionCandidate2D initial = resolve(spline, -1, 0.05, 0.1)
				.getUniqueCandidate();
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline, initial);
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId pointId = registry.getPersistentGeoId(point);
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		final LocusSemanticAddress2D oldAddress = parent.getSemanticAddress();
		assertTrue(parent.getBranchInput().isAuxiliaryObject());
		assertFalse(parent.getBranchInput().isEuclidianVisible());
		assertTrue(parent.getParameterInput().toGeoElement().isAuxiliaryObject());
		assertFalse(parent.getParameterInput().toGeoElement().isEuclidianVisible());

		LocusPointInteractionResult2D moved =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point, 1, -0.05,
						policy(0.1));

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				moved.getStatus());
		assertSame(point, requireLookup("P"));
		assertEquals(pointId, registry.getPersistentGeoId(point));
		assertNotEquals(oldAddress.getCanonicalParameter(),
				parent.getSemanticAddress().getCanonicalParameter());
		assertEquals(moved.getUniqueCandidate().getAddress(),
				parent.getSemanticAddress());
		assertEquals(moved.getUniqueCandidate().getEvaluatedPoint().getX(),
				point.getInhomX(), 1E-10);
		assertEquals(moved.getUniqueCandidate().getEvaluatedPoint().getY(),
				point.getInhomY(), 1E-10);
	}

	@Test
	void r608AmbiguousMoveLeavesInteractionOwnedDagInputsUntouched() {
		GeoLocusV2 spline = createSelfIntersectingSpline("S");
		LocusPoint2D crossing = evaluate(spline, 0.25);
		LocusPointInteractionResult2D ambiguous = resolve(spline,
				crossing.getX(), crossing.getY(), 1E-6);
		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				ambiguous.getStatus());
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline, ambiguous.getCandidates().get(0));
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D before = parent.getSemanticAddress();

		LocusPointInteractionResult2D result =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
						crossing.getX(), crossing.getY(), policy(1E-6));

		assertTrue(result.getStatus()
				== LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES
				|| result.getStatus()
						== LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getDiagnostic());
		assertEquals(before, parent.getSemanticAddress());
		assertTrue(point.isDefined());
	}

	@Test
	void r609PeriodicSeamUsesCanonicalParameterAndNearestIntrinsicLift() {
		GeoLocusV2 circle = createPeriodicCircle();
		LocusPointInteractionResult2D initial = resolve(circle, 1, 0, 1E-5);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				initial.getStatus(), initial.getDiagnostic());
		assertEquals(SearchCoverage.BOUNDED_EVALUATOR_SEARCH,
				initial.getSearchCoverage());
		LocusPointInteractionCandidate2D initialCandidate =
				onlyDiscoveredCandidate(initial);
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", circle, initialCandidate);
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D before = parent.getSemanticAddress();

		double angle = -0.05;
		LocusPointInteractionResult2D moved =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
						Math.cos(angle), Math.sin(angle), policy(1E-4));
		LocusPointInteractionCandidate2D proposed = onlyDiscoveredCandidate(moved);
		LocusSemanticAddress2D address = proposed.getAddress();

		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				moved.getStatus(), moved.getDiagnostic());
		assertEquals(SearchCoverage.BOUNDED_EVALUATOR_SEARCH,
				moved.getSearchCoverage());
		assertNull(moved.getUniqueCandidate());
		assertEquals(-1, address.getPeriodicLift());
		assertEquals(2 * Math.PI + angle, address.getCanonicalParameter(), 2E-7);
		assertEquals(SeamSide.INTERIOR, address.getSeamSide());
		assertEquals(before, parent.getSemanticAddress());
		assertEquals(1, point.getInhomX(), 1E-10);
		assertEquals(0, point.getInhomY(), 1E-10);
		assertTrue(point.isDefined());
		assertDiscoveredCandidatesForwardVerified(circle, moved, Math.cos(angle),
				Math.sin(angle), 1E-4);
	}

	@Test
	void r610SimilarityImagesResolveSemanticallyAndCollapsedImageRecovers() {
		final GeoLocusV2 source = createLineSpline("S");
		GeoLocusV2 translated = add("T=Translate(S,(3,4))");
		LocusPointInteractionResult2D translatedResult =
				resolve(translated, 3, 4.1, 0.2);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				translatedResult.getStatus());
		assertEquals(translated.getPersistentLocusId(), translatedResult
				.getUniqueCandidate().getAddress().getSourceLocusId());

		GeoNumeric factor = add("k=0");
		GeoLocusV2 dilated = add("D=Dilate(S,k,(0,0))");
		LocusPointInteractionResult2D collapsed = resolve(dilated, 0, 0, 0.1);
		assertEquals(LocusPointInteractionStatus2D.DEGENERATE_SOURCE_IMAGE,
				collapsed.getStatus());
		factor.setValue(2);
		factor.updateCascade();
		LocusPointInteractionResult2D recovered = resolve(dilated, 0, 0.1, 0.2);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				recovered.getStatus());
		assertEquals(dilated.getPersistentLocusId(), recovered.getUniqueCandidate()
				.getAddress().getSourceLocusId());
		assertNotEquals(source.getPersistentLocusId(),
				dilated.getPersistentLocusId());
	}

	@Test
	void r611InvalidDynamicSourceFailsClosedAndValidRecoveryIsDeterministic() {
		createLineSpline("S");
		GeoNumeric factor = add("k=2");
		GeoLocusV2 dilated = add("D=Dilate(S,k)");
		LocusPointInteractionResult2D before = resolve(dilated, 1, 0, 0.1);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				before.getStatus());

		factor.setUndefined();
		factor.updateCascade();
		assertEquals(LocusPointInteractionStatus2D.INVALID_SOURCE,
				resolve(dilated, 1, 0, 0.1).getStatus());
		factor.setValue(2);
		factor.updateCascade();
		LocusPointInteractionResult2D after = resolve(dilated, 1, 0, 0.1);
		assertEquals(before.getUniqueCandidate().getAddress(),
				after.getUniqueCandidate().getAddress());
	}

	@Test
	void r612RepeatedQueriesArePathIndependentAndReadNoPresentationAuthority() {
		GeoLocusV2 spline = createLineSpline("S");
		LocusPointInteractionResult2D first = resolve(spline, 0.75, 0.1, 0.2);
		LocusPointInteractionResult2D second = resolve(spline, 0.75, 0.1, 0.2);

		assertEquals(first.getStatus(), second.getStatus());
		assertEquals(first.getUniqueCandidate().getAddress(),
				second.getUniqueCandidate().getAddress());
		assertEquals(first.getUniqueCandidate().getWorldDistance(),
				second.getUniqueCandidate().getWorldDistance(), 0);
		assertEquals(first.getInstrumentation().getSemanticEvaluations(),
				second.getInstrumentation().getSemanticEvaluations());
		assertEquals(first.getInstrumentation().getRefinementIterations(),
				second.getInstrumentation().getRefinementIterations());
		assertNoPresentationReads(first);
		assertNoPresentationReads(second);
	}

	@Test
	void r613InteractivePointSaveReopenRestoresOwnedStateAndMovement() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -0.5, 0.05, 0.1).getUniqueCandidate());
		PersistentGeoId pointId = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		String xml = getApp().getXML();

		getApp().setXML(xml, true);
		GeoPoint reopened = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(reopened));
		LocusPointInteractionResult2D moved =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(reopened, 0.5,
						-0.05, policy(0.1));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				moved.getStatus());
		assertTrue(reopened.isDefined());
	}

	@Test
	void r614InteractivePointUndoRedoRetainsDurableIdentityAndOwnership() {
		GeoLocusV2 spline = createLineSpline("S");
		activateUndo();
		getApp().storeUndoInfo();
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, 0, 0.05, 0.1).getUniqueCandidate());
		PersistentGeoId pointId = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		getApp().storeUndoInfo();

		getKernel().undo();
		assertNull(lookup("P"));
		getKernel().redo();
		GeoPoint restored = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(restored));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				LocusV2PublicOperations.moveInteractiveSemanticPoint(restored, 0.5,
						0, policy(0.1)).getStatus());
	}

	@Test
	void r615InteractiveMoveUndoRedoRestoresTheExactSemanticAddress() {
		GeoLocusV2 spline = createLineSpline("S");
		activateUndo();
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -1, 0, 0.1).getUniqueCandidate());
		getApp().storeUndoInfo();
		LocusSemanticAddress2D initial = semanticParent(point).getSemanticAddress();
		LocusV2PublicOperations.moveInteractiveSemanticPoint(point, 1, 0,
				policy(0.1));
		LocusSemanticAddress2D moved = semanticParent(point).getSemanticAddress();
		assertNotEquals(initial, moved);
		getApp().storeUndoInfo();

		getKernel().undo();
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertEquals(initial, semanticParent(point).getSemanticAddress());
		getKernel().redo();
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertEquals(moved, semanticParent(point).getSemanticAddress());
	}

	@Test
	void r616CopyRemapsTheOwnedSourceAndRemainsInteractivelyMovable() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -0.5, 0, 0.1).getUniqueCandidate());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId sourcePointId = registry.getPersistentGeoId(point);
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(point), text -> text);
		paste(clipboard);

		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> sourcePointId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copy = assertInstanceOf(GeoPoint.class,
				registry.getGeo(copiedRecord.getId()));
		AlgoSemanticLocusPoint2D copiedParent = semanticParent(copy);
		assertNotEquals(spline.getPersistentLocusId(),
				copiedParent.getSource().getPersistentLocusId());
		assertEquals(copiedParent.getSource().getPersistentLocusId(),
				copiedParent.getSemanticAddress().getSourceLocusId());
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				LocusV2PublicOperations.moveInteractiveSemanticPoint(copy, 0.5, 0,
						policy(0.1)).getStatus());
	}

	@Test
	void r617DisconnectedComponentsRejectGapSelectionAndPreventPointJump() {
		GeoLocusV2 locus = createDisconnectedLine();
		LocusPointInteractionResult2D gap = resolve(locus, 0, 0, 2);
		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				gap.getStatus());
		assertEquals(2, gap.getCandidates().size());
		assertNotEquals(gap.getCandidates().get(0).getAddress()
				.getComponentLineageKey(), gap.getCandidates().get(1).getAddress()
				.getComponentLineageKey());

		LocusPointInteractionResult2D initial = resolve(locus, -1.5, 0, 0.1);
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", locus, initial.getUniqueCandidate());
		LocusSemanticAddress2D before = semanticParent(point).getSemanticAddress();
		LocusPointInteractionResult2D acrossGap =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point, 1.5, 0,
						policy(0.1));

		assertNotEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				acrossGap.getStatus());
		assertEquals(before, semanticParent(point).getSemanticAddress());
		assertEquals(-1.5, point.getInhomX(), 1E-7);
		assertEquals(1, acrossGap.getInstrumentation().getComponentsInspected());
	}

	@Test
	void r618TransformedGeneralLocusUsesEvaluatorFallbackWhenPolynomialIsUnsupported() {
		GeoLocusV2 source = createParabola();
		GeoLocusV2 translated = add("T=Translate(L,(3,4))");
		PiecewisePolynomialLocus2D advertisedWrapper = assertInstanceOf(
				PiecewisePolynomialLocus2D.class,
				translated.getSemanticDefinition().getEvaluatorCapability());
		assertFalse(advertisedWrapper.supportsPiecewisePolynomial(
				translated.getSemanticDefinition()));
		LocusPointInteractionResult2D result = resolve(translated, 3.5, 4.3,
				0.1);

		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus());
		assertEquals(SearchCoverage.BOUNDED_EVALUATOR_SEARCH,
				result.getSearchCoverage());
		assertNull(result.getUniqueCandidate());
		assertDiscoveredCandidatesForwardVerified(translated, result, 3.5, 4.3,
				0.1);
		assertNotEquals(source.getPersistentLocusId(),
				translated.getPersistentLocusId());
	}

	@Test
	void r619RotatedReflectedAndNegativeDilatedSplinesResolveCovariantly() {
		GeoLocusV2 source = createLineSpline("S");
		GeoLocusV2 rotated = add("R=Rotate(S,pi/2,(1,1))");
		GeoLocusV2 reflected = add("F=Reflect(S,y=x)");
		GeoLocusV2 dilated = add("D=Dilate(S,-2,(1,0))");

		assertUniquePoint(rotated, 2.05, 0, 0.1, 2, 0);
		assertUniquePoint(reflected, 0.05, 0.5, 0.1, 0, 0.5);
		assertUniquePoint(dilated, 2, 0.05, 0.1, 2, 0);
		assertNotEquals(source.getPersistentLocusId(),
				rotated.getPersistentLocusId());
		assertNotEquals(source.getPersistentLocusId(),
				reflected.getPersistentLocusId());
		assertNotEquals(source.getPersistentLocusId(),
				dilated.getPersistentLocusId());
	}

	@Test
	void r620ExistingPointSurvivesTwoZeroMinusTwoCollapseCycleWithSameAddress() {
		createLineSpline("S");
		GeoNumeric factor = add("k=2");
		GeoLocusV2 dilated = add("D=Dilate(S,k,(0,0))");
		LocusPointInteractionResult2D initial = resolve(dilated, 1, 0, 0.1);
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", dilated, initial.getUniqueCandidate());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId pointId = registry.getPersistentGeoId(point);
		LocusSemanticAddress2D address = semanticParent(point).getSemanticAddress();

		factor.setValue(0);
		factor.updateCascade();
		assertTrue(point.isDefined());
		assertEquals(0, point.getInhomX(), 0);
		assertEquals(address, semanticParent(point).getSemanticAddress());
		assertEquals(pointId, registry.getPersistentGeoId(point));
		assertEquals(LocusPointInteractionStatus2D.DEGENERATE_SOURCE_IMAGE,
				resolve(dilated, 0, 0, 0.1).getStatus());

		factor.setValue(-2);
		factor.updateCascade();
		assertTrue(point.isDefined());
		assertEquals(-1, point.getInhomX(), 1E-8);
		assertEquals(address, semanticParent(point).getSemanticAddress());
		assertEquals(pointId, registry.getPersistentGeoId(point));
	}

	@Test
	void r621RenameChangesNoDurableInteractionIdentityOrAddress() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -0.5, 0, 0.1).getUniqueCandidate());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId sourceId = spline.getPersistentLocusId();
		PersistentGeoId pointId = registry.getPersistentGeoId(point);
		LocusSemanticAddress2D address = semanticParent(point).getSemanticAddress();

		spline.setLabel("RenamedSource");
		point.setLabel("RenamedPoint");
		assertEquals(sourceId, spline.getPersistentLocusId());
		assertEquals(pointId, registry.getPersistentGeoId(point));
		assertEquals(address, semanticParent(point).getSemanticAddress());
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point, 0.5, 0,
						policy(0.1)).getStatus());
	}

	@Test
	void r622StaleCandidateIsRejectedBeforeAnyConstructionPublication() {
		GeoLocusV2 spline = createLineSpline("S");
		LocusPointInteractionCandidate2D stale = resolve(spline, 0, 0, 0.1)
				.getUniqueCandidate();
		long oldRevision = spline.getSemanticRevision();
		GeoPoint defining = assertInstanceOf(GeoPoint.class, requireLookup("SA"));
		defining.setCoords(-3, 0, 1);
		defining.updateCascade();
		assertNotEquals(oldRevision, spline.getSemanticRevision());
		int constructionSize = getConstruction().getGeoSetConstructionOrder().size();
		int identityRecords = getConstruction().getSpatialIdentityRegistry()
				.getRecords().size();

		assertThrows(IllegalArgumentException.class,
				() -> LocusV2PublicOperations.createInteractiveSemanticPoint(
						getConstruction(), "P", spline, stale));
		assertNull(lookup("P"));
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(identityRecords, getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());
	}

	@Test
	void r623InstrumentationSeparatesGlobalAndLocalScopesWithinEveryDragBudget() {
		GeoLocusV2 locus = createDisconnectedLine();
		LocusPointInteractionResult2D global = resolve(locus, -1.5, 0, 0.1);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				global.getStatus());
		assertEquals(1, global.getInstrumentation().getGlobalSearches());
		assertEquals(0, global.getInstrumentation().getLocalSearches());
		assertEquals(0, global.getInstrumentation().getGlobalFallbacks());
		assertEquals(1, global.getInstrumentation().getBranchesInspected());
		assertEquals(2, global.getInstrumentation().getComponentsInspected());
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", locus, global.getUniqueCandidate());
		int constructionSize = getConstruction().getGeoSetConstructionOrder().size();
		int identityRecords = getConstruction().getSpatialIdentityRegistry()
				.getRecords().size();

		for (int step = 0; step < 24; step++) {
			double target = -1.9 + step * 0.035;
			LocusPointInteractionResult2D local =
					LocusV2PublicOperations.moveInteractiveSemanticPoint(point, target,
							0.02, policy(0.1));
			assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
					local.getStatus());
			assertEquals(0, local.getInstrumentation().getGlobalSearches());
			assertEquals(1, local.getInstrumentation().getLocalSearches());
			assertEquals(0, local.getInstrumentation().getGlobalFallbacks());
			assertEquals(1, local.getInstrumentation().getBranchesInspected());
			assertEquals(1, local.getInstrumentation().getComponentsInspected());
			assertTrue(local.getInstrumentation().getSemanticEvaluations()
					<= policy(0.1).getWorkBudget().getMaximumSemanticEvaluations());
			assertTrue(local.getInstrumentation().getSubdivisions()
					<= policy(0.1).getWorkBudget().getMaximumSubdivisions());
			assertNoPresentationReads(local);
		}
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(identityRecords, getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());
	}

	@Test
	void r624VersionedAddressStateRoundTripPreservesPeriodicSemanticEvidence() {
		GeoLocusV2 circle = createPeriodicCircle();
		LocusPointInteractionResult2D seedResult = resolve(circle, 1, 0, 1E-6);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				seedResult.getStatus(), seedResult.getDiagnostic());
		LocusSemanticAddress2D seed = onlyDiscoveredCandidate(seedResult)
				.getAddress();
		LocusSemanticAddress2D liftedSeam = new LocusSemanticAddress2D(
				seed.getSourceLocusId(), seed.getProviderVersion(),
				seed.getBranchKey(), seed.getComponentLineageKey(),
				seed.getCanonicalParameter(), -1, SeamSide.UPPER_APPROACH);
		LocusPointInteractionResult2D liftedResult = resolver.resolve(
				new LocusPointInteractionQuery2D(circle, 1, 0, policy(1E-6),
						liftedSeam));
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				liftedResult.getStatus(), liftedResult.getDiagnostic());
		assertEquals(SearchCoverage.BOUNDED_EVALUATOR_SEARCH,
				liftedResult.getSearchCoverage());
		assertEquals(liftedSeam,
				onlyDiscoveredCandidate(liftedResult).getAddress());
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", circle,
				onlyDiscoveredCandidate(liftedResult));
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D before = parent.getSemanticAddress();
		String encoded = parent.getBranchInput().getTextString();

		assertTrue(encoded.startsWith("geocedg-locus-address/v1|"));
		assertEquals(LocusSemanticAddressState2D.encode(before), encoded);
		assertEquals(seed.getComponentLineageKey(),
				before.getComponentLineageKey());
		assertEquals(seed.getProviderVersion(), before.getProviderVersion());
		assertEquals(SeamSide.UPPER_APPROACH, before.getSeamSide());
		assertEquals(-1, before.getPeriodicLift());

		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		GeoPoint reopened = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		AlgoSemanticLocusPoint2D reopenedParent = semanticParent(reopened);
		LocusSemanticAddress2D after = reopenedParent.getSemanticAddress();

		assertEquals(before, after);
		assertEquals(encoded, reopenedParent.getBranchInput().getTextString());
		assertEquals(before.getComponentLineageKey(),
				after.getComponentLineageKey());
		assertEquals(before.getProviderVersion(), after.getProviderVersion());
		assertEquals(before.getSeamSide(), after.getSeamSide());
		assertEquals(before.getPeriodicLift(), after.getPeriodicLift());
		assertTrue(reopened.isDefined());
	}

	@Test
	void r625MalformedAddressStateCannotMoveOrFallBackToGlobalResolution() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -0.5, 0, 0.1).getUniqueCandidate());
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D original = parent.getSemanticAddress();
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId pointId = registry.getPersistentGeoId(point);
		GeoNumeric parameter = assertInstanceOf(GeoNumeric.class,
				parent.getParameterInput().toGeoElement());
		String malformed = "geocedg-locus-address/v1|malformed";
		parent.getBranchInput().setTextString(malformed);
		parent.getBranchInput().updateCascade();

		assertFalse(point.isDefined());
		assertEquals(original, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());
		double unchangedParameter = parameter.getDouble();
		try {
			LocusPointInteractionResult2D result =
					LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
							0.5, 0, policy(0.1));
			assertNotEquals(
					LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
					result.getStatus());
		} catch (IllegalArgumentException | IllegalStateException expected) {
			// A rejected edit may be reported by exception or typed result.
		}

		assertEquals(malformed, parent.getBranchInput().getTextString());
		assertEquals(unchangedParameter, parameter.getDouble(), 0);
		assertEquals(pointId, registry.getPersistentGeoId(point));
		assertEquals(original, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());
		assertFalse(point.isDefined());
	}

	@Test
	void r626IncompatibleVersionedAddressCannotMoveOrReattachGlobally() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -0.5, 0, 0.1).getUniqueCandidate());
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D original = parent.getSemanticAddress();
		LocusSemanticAddress2D incompatible = new LocusSemanticAddress2D(
				original.getSourceLocusId(),
				original.getProviderVersion() + "/incompatible",
				original.getBranchKey(), original.getComponentLineageKey(),
				original.getCanonicalParameter(), original.getPeriodicLift(),
				original.getSeamSide());
		String encodedIncompatible = LocusSemanticAddressState2D.encode(incompatible);
		GeoNumeric parameter = assertInstanceOf(GeoNumeric.class,
				parent.getParameterInput().toGeoElement());
		parent.getBranchInput().setTextString(encodedIncompatible);
		parent.getBranchInput().updateCascade();

		assertFalse(point.isDefined());
		assertEquals(original, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());
		double unchangedParameter = parameter.getDouble();
		try {
			LocusPointInteractionResult2D result =
					LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
							0.5, 0, policy(0.1));
			assertNotEquals(
					LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
					result.getStatus());
		} catch (IllegalArgumentException | IllegalStateException expected) {
			// A rejected edit may be reported by exception or typed result.
		}

		assertEquals(encodedIncompatible,
				parent.getBranchInput().getTextString());
		assertEquals(unchangedParameter, parameter.getDouble(), 0);
		assertEquals(original, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());
		assertFalse(point.isDefined());
	}

	@Test
	void r627DeepSimilarityCompositionCountsNestedMissesAndStopsAtBudget() {
		final GeoLocusV2 source = createLine();
		add("T1=Translate(L,(1,2))");
		add("T2=Rotate(T1,pi/2,(0,0))");
		add("T3=Reflect(T2,y=x)");
		add("T4=Dilate(T3,-2,(0,0))");
		add("T5=Translate(T4,(3,-1))");
		GeoLocusV2 deep = add("T6=Rotate(T5,-pi/2,(0,0))");
		LocusPoint2D target = evaluateFirstBranch(deep, 0.5);
		LocusPointInteractionResult2D regular = resolve(deep, target.getX(),
				target.getY(), 1E-6);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				regular.getStatus(), regular.getDiagnostic());
		assertEquals(SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS,
				regular.getSearchCoverage());
		assertEquals(regular.getInstrumentation().getCacheMisses(),
				regular.getInstrumentation().getSemanticEvaluations());
		assertTrue(regular.getInstrumentation().getSemanticEvaluations()
				> regular.getInstrumentation().getSubdivisions());
		assertTrue(regular.getInstrumentation().getSemanticEvaluations()
				<= policy(1E-6).getWorkBudget().getMaximumSemanticEvaluations());
		assertEquals(0, regular.getInstrumentation().getSubdivisions());
		assertTrue(regular.getUniqueCandidate().getMethod()
				.startsWith("certified-affine-projection"));
		assertNotEquals(source.getPersistentLocusId(),
				deep.getPersistentLocusId());
		assertNoPresentationReads(regular);

		int maximumMisses = 4;
		LocusPointInteractionPolicy2D constrained =
				new LocusPointInteractionPolicy2D(1E-6, 1E-10, 1E-12,
						new LocusPointInteractionWorkBudget2D(maximumMisses,
								2, 4, 4));
		LocusPointInteractionResult2D exhausted = resolver.resolve(
				new LocusPointInteractionQuery2D(deep, target.getX(),
						target.getY(), constrained));

		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				exhausted.getStatus());
		assertEquals(exhausted.getInstrumentation().getCacheMisses(),
				exhausted.getInstrumentation().getSemanticEvaluations());
		assertTrue(exhausted.getInstrumentation().getSemanticEvaluations() > 0);
		assertTrue(exhausted.getInstrumentation().getSemanticEvaluations()
				<= maximumMisses);
		assertNoPresentationReads(exhausted);

		GeoNumeric centerX = add("cxOverflow=1E308");
		add("AO=(cxOverflow,0)");
		GeoLocusV2 overflow = add("O=Rotate(L,pi,AO)");
		assertTrue(overflow.isDefined());
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			assertEquals(EvaluationStatus.NON_FINITE,
					overflow.evaluate(BRANCH, 0.5, session).getStatus());
		}
		CertifiedAffineLocus2D unavailable = assertInstanceOf(
				CertifiedAffineLocus2D.class,
				overflow.getSemanticDefinition().getEvaluatorCapability());
		assertFalse(unavailable.supportsCertifiedAffine(
				overflow.getSemanticDefinition()));

		centerX.setValue(1);
		centerX.updateCascade();
		assertTrue(overflow.isDefined());
		CertifiedAffineLocus2D recovered = assertInstanceOf(
				CertifiedAffineLocus2D.class,
				overflow.getSemanticDefinition().getEvaluatorCapability());
		assertTrue(recovered.supportsCertifiedAffine(
				overflow.getSemanticDefinition()));
		LocusPointInteractionResult2D recertified =
				resolve(overflow, 1.5, 0, 0.1);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				recertified.getStatus(), recertified.getDiagnostic());
		assertEquals(SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS,
				recertified.getSearchCoverage());
	}

	@Test
	void r628ExcessivePolynomialDegreeFailsTypedBeforeUnboundedIsolationWork() {
		GeoLocusV2 source = createLine();
		LocusDefinition2D previous = source.getSemanticDefinition();
		source.publishSemanticDefinition(new LocusDefinition2D(
				source.getLocusIdentity(), previous.getSemanticRevision() + 1,
				DefinitionStatus.VALID, previous.getProvider(),
				previous.getBranches(), new ExcessiveDegreePolynomialEvaluator(),
				Determinism.POINTWISE_DETERMINISTIC,
				"r6-excessive-polynomial-degree/v1", source.getInstrumentation()));

		LocusPointInteractionResult2D result = resolve(source, 0, 0, 0.1);

		assertTrue(result.getStatus()
				== LocusPointInteractionStatus2D.UNSUPPORTED_CAPABILITY
				|| result.getStatus()
						== LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getDiagnostic());
		assertTrue(result.getDiagnostic().contains("degree")
				|| result.getDiagnostic().contains("Degree"),
				result.getDiagnostic());
		assertEquals(0, result.getInstrumentation().getSemanticEvaluations());
		assertEquals(0, result.getInstrumentation().getRefinementIterations());
		assertEquals(0, result.getInstrumentation().getRetainedCandidates());
		assertNoPresentationReads(result);
	}

	@Test
	void r629MultipleBranchesAreCanonicalUnderDefinitionOrderPerturbation() {
		GeoLocusV2 locus = createLine();
		LocusDefinition2D publicDefinition = locus.getSemanticDefinition();
		LocusBranch2D publicBranch = publicDefinition.getBranches().get(0);
		LocusBranch2D branchZ = new LocusBranch2D("r6/branch-z",
				publicBranch.getDeclaredDriverDomain(),
				publicBranch.getValidDomainComponents(), publicBranch.getOrientation(),
				"r6/branch-z/semantic-v1", publicBranch.getLineage(),
				publicBranch.getProperties(), publicBranch.getQuality());
		LocusBranch2D branchA = new LocusBranch2D("r6/branch-a",
				publicBranch.getDeclaredDriverDomain(),
				publicBranch.getValidDomainComponents(), publicBranch.getOrientation(),
				"r6/branch-a/semantic-v1", publicBranch.getLineage(),
				publicBranch.getProperties(), publicBranch.getQuality());
		locus.publishSemanticDefinition(new LocusDefinition2D(
				publicDefinition.getLocusIdentity(),
				publicDefinition.getSemanticRevision() + 1, DefinitionStatus.VALID,
				publicDefinition.getProvider(), List.of(branchZ, branchA),
				publicDefinition.getEvaluatorCapability(),
				Determinism.POINTWISE_DETERMINISTIC,
				"r6-multiple-branches/v1", locus.getInstrumentation()));
		LocusPointInteractionResult2D first = resolve(locus, 0.25, 0, 1E-6);

		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				first.getStatus(), first.getDiagnostic());
		assertEquals(List.of("r6/branch-a", "r6/branch-z"), branchKeys(first));
		List<LocusSemanticAddress2D> firstAddresses = addresses(first);
		LocusDefinition2D previous = locus.getSemanticDefinition();
		locus.publishSemanticDefinition(new LocusDefinition2D(
				locus.getLocusIdentity(), previous.getSemanticRevision() + 1,
				DefinitionStatus.VALID, previous.getProvider(),
				List.of(branchA, branchZ),
				previous.getEvaluatorCapability(),
				Determinism.POINTWISE_DETERMINISTIC,
				"r6-multiple-branches-reordered/v1", locus.getInstrumentation()));

		LocusPointInteractionResult2D reordered = resolve(locus, 0.25, 0, 1E-6);
		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				reordered.getStatus(), reordered.getDiagnostic());
		assertEquals(List.of("r6/branch-a", "r6/branch-z"),
				branchKeys(reordered));
		assertEquals(firstAddresses, addresses(reordered));
		assertNoPresentationReads(reordered);
	}

	@Test
	void r630CuspWithZeroSemanticSpeedCannotClaimUniqueAddress() {
		add("r6CuspT=0");
		add("r6CuspQ=Point({r6CuspT^2,r6CuspT^3})");
		add("r6CuspD={false,{-1,1,true,true}}");
		GeoLocusV2 cusp = add("r6Cusp=LocusV2(r6CuspQ,r6CuspT,r6CuspD)");

		LocusPointInteractionResult2D result = resolve(cusp, 0, 0, 1E-6);

		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus(), result.getDiagnostic());
		assertNull(result.getUniqueCandidate());
		assertTrue(result.getDiagnostic().toLowerCase().contains("singular"),
				result.getDiagnostic());
		assertNoPresentationReads(result);
	}

	@Test
	void r631HighCurvatureEvaluatorRefinesAndForwardVerifiesSemantically() {
		add("r6CurveT=0");
		add("r6CurveQ=Point({r6CurveT,20*r6CurveT^2})");
		add("r6CurveD={false,{-1,1,true,true}}");
		GeoLocusV2 curved = add(
				"r6Curve=LocusV2(r6CurveQ,r6CurveT,r6CurveD)");
		double parameter = 0.613;
		LocusPointInteractionResult2D result = resolve(curved, parameter,
				20 * parameter * parameter, 1E-5);

		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus(), result.getDiagnostic());
		assertEquals(SearchCoverage.BOUNDED_EVALUATOR_SEARCH,
				result.getSearchCoverage());
		assertNull(result.getUniqueCandidate());
		assertDiscoveredCandidatesForwardVerified(curved, result, parameter,
				20 * parameter * parameter, 1E-5);
		assertTrue(result.getInstrumentation().getSemanticEvaluations()
				<= policy(1E-5).getWorkBudget().getMaximumSemanticEvaluations());
		assertNoPresentationReads(result);
	}

	@Test
	void r632NonperiodicSplineEndpointsHaveOneCanonicalOwnerEach() {
		GeoLocusV2 spline = createLineSpline("S");
		LocusPointInteractionResult2D lower = resolve(spline, -2, 0, 1E-7);
		LocusPointInteractionResult2D upper = resolve(spline, 2, 0, 1E-7);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				lower.getStatus(), lower.getDiagnostic());
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				upper.getStatus(), upper.getDiagnostic());
		assertEquals(0, lower.getUniqueCandidate().getAddress()
				.getCanonicalParameter(), 0);
		assertEquals(1, upper.getUniqueCandidate().getAddress()
				.getCanonicalParameter(), 0);
		assertEquals(1, lower.getCandidates().size());
		assertEquals(1, upper.getCandidates().size());
	}

	@Test
	void r633PolynomialSearchRejectsSpansOutsideTheValidSemanticComponent() {
		GeoLocusV2 source = createLine();
		ExplicitNumericDomainProvider2D provider = G8BIntersectionFixtures.provider(
				"r6-selective-spans", -3, 3, true, true, false,
				Orientation.INCREASING);
		LocusInterval2D component = new LocusInterval2D(-1, 1, true, true);
		LocusBranch2D branch = G8BIntersectionFixtures.branch("r6/selective-main",
				provider, List.of(component), EnumSet.of(BranchProperty.FINITE));
		LocusDefinition2D previous = source.getSemanticDefinition();
		source.publishSemanticDefinition(new LocusDefinition2D(
				source.getLocusIdentity(), previous.getSemanticRevision() + 1,
				DefinitionStatus.VALID, provider, List.of(branch),
				new SelectiveSpanPolynomialEvaluator(),
				Determinism.POINTWISE_DETERMINISTIC,
				"r6-selective-spans/v1", source.getInstrumentation()));

		LocusPointInteractionResult2D result = resolve(source, 0.25, 0, 1E-7);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus(), result.getDiagnostic());
		assertEquals(SearchCoverage.ALL_EXPLICIT_POLYNOMIAL_SPANS,
				result.getSearchCoverage());
		assertEquals(1, result.getInstrumentation().getPolynomialSpans());
		assertEquals("r6/selective-main",
				result.getUniqueCandidate().getAddress().getBranchKey());
		assertEquals(0.25, result.getUniqueCandidate().getAddress()
				.getCanonicalParameter(), 1E-9);
	}

	@Test
	void r634ComponentLossRoundTripRecoversTheSameInteractivePoint() {
		GeoLocusV2 locus = createDisconnectedLine();
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", locus,
				resolve(locus, -1.5, 0, 0.1).getUniqueCandidate());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId pointId = registry.getPersistentGeoId(point);
		final LocusSemanticAddress2D address = semanticParent(point).getSemanticAddress();
		String encodedAddress = semanticParent(point).getBranchInput()
				.getTextString();

		editGeoElement(requireLookup("D"), "D={false,{1,2,true,true}}");
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertFalse(point.isDefined());
		assertEquals(encodedAddress,
				semanticParent(point).getBranchInput().getTextString());
		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertFalse(point.isDefined());
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));

		editGeoElement(requireLookup("D"),
				"D={false,{-2,-1,true,true},{1,2,true,true}}");
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertTrue(point.isDefined());
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
		assertEquals(address, semanticParent(point).getSemanticAddress());
		assertEquals(-1.5, point.getInhomX(), 1E-8);
	}

	@Test
	void r635ClosedSplineSeamDeduplicatesEquivalentPolynomialEndpoints() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D,A},3)");
		LocusPoint2D seam = evaluate(spline, 0);

		LocusPointInteractionResult2D result = resolve(spline, seam.getX(),
				seam.getY(), 1E-6);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus(), result.getDiagnostic());
		assertEquals(1, result.getCandidates().size());
		assertEquals(0, result.getUniqueCandidate().getAddress()
				.getCanonicalParameter(), 1E-10);
		assertEquals(0, result.getUniqueCandidate().getAddress().getPeriodicLift());
	}

	@Test
	void r649ClosedSplineInteractivePointCrossesPeriodicSeamBidirectionally() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D,A},3)");
		LocusPoint2D upperSide = evaluate(spline, 0.98);
		LocusPointInteractionResult2D initial = resolve(spline, upperSide.getX(),
				upperSide.getY(), 1E-6);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				initial.getStatus(), initial.getDiagnostic());
		assertEquals(1, initial.getCandidates().size());
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline, initial.getUniqueCandidate());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId pointId = registry.getPersistentGeoId(point);
		PersistentGeoId sourceId = spline.getPersistentLocusId();
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D initialAddress = parent.getSemanticAddress();
		String branch = initialAddress.getBranchKey();
		String component = initialAddress.getComponentLineageKey();
		int constructionSize = getConstruction().getGeoSetConstructionOrder().size();

		LocusSemanticAddress2D firstForward = moveToParameter(spline, point, 0.02,
				0.2).getUniqueCandidate().getAddress();
		assertPeriodicInteractionState(point, spline, pointId, sourceId, branch,
				component, 0.02, 1, constructionSize);
		assertEquals(firstForward, parent.getSemanticAddress());

		moveToParameter(spline, point, 0.08, 0.3);
		assertPeriodicInteractionState(point, spline, pointId, sourceId, branch,
				component, 0.08, 1, constructionSize);

		moveToParameter(spline, point, 0.98, 0.4);
		assertPeriodicInteractionState(point, spline, pointId, sourceId, branch,
				component, 0.98, 0, constructionSize);
		moveToParameter(spline, point, 0.92, 0.3);
		assertPeriodicInteractionState(point, spline, pointId, sourceId, branch,
				component, 0.92, 0, constructionSize);

		LocusSemanticAddress2D directFinal = moveToParameter(spline, point, 0.08,
				0.5).getUniqueCandidate().getAddress();
		assertPeriodicInteractionState(point, spline, pointId, sourceId, branch,
				component, 0.08, 1, constructionSize);
		moveToParameter(spline, point, 0.92, 0.5);
		moveToParameter(spline, point, 0.97, 0.3);
		moveToParameter(spline, point, 0.01, 0.2);
		LocusSemanticAddress2D incrementalFinal = moveToParameter(spline, point,
				0.08, 0.3).getUniqueCandidate().getAddress();

		assertEquals(directFinal, incrementalFinal);
		assertPeriodicInteractionState(point, spline, pointId, sourceId, branch,
				component, 0.08, 1, constructionSize);
	}

	@Test
	void r636NonfiniteInteractionInputsFailBeforeConstructionMutation() {
		GeoLocusV2 locus = createLine();
		int constructionSize = getConstruction().getGeoSetConstructionOrder().size();

		assertThrows(IllegalArgumentException.class,
				() -> new LocusPointInteractionQuery2D(locus, Double.NaN, 0,
						policy(0.1)));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusPointInteractionQuery2D(locus, 0,
						Double.POSITIVE_INFINITY, policy(0.1)));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusPointInteractionPolicy2D(Double.NaN, 1E-10,
						1E-12, LocusPointInteractionWorkBudget2D.initial()));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusPointInteractionPolicy2D(0.1, 1E-10, 0,
						LocusPointInteractionWorkBudget2D.initial()));
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void r637InteractivePointFeedsPartialMetricThroughTheNormalDag() {
		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", spline,
				resolve(spline, -1, 0, 1E-7).getUniqueCandidate());
		add("Q=Point(S,\"" + SPLINE_BRANCH + "\",0.75)");
		GeoNumeric partial = add("MP=Length(S,P,Q)");
		double before = partial.getDouble();

		LocusPointInteractionResult2D moved =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point, 0, 0,
						policy(1E-7));

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				moved.getStatus(), moved.getDiagnostic());
		assertTrue(partial.isDefined());
		assertNotEquals(before, partial.getDouble());
		assertEquals(1, partial.getDouble(), 1E-8);
		assertSame(point, requireLookup("P"));
	}

	@Test
	void r638RepeatedComponentLossPreservesLastAcceptedAddressUntilRecovery() {
		GeoLocusV2 locus = createDisconnectedLine();
		GeoPoint point = semanticPoint(locus, -1.5);
		final LocusSemanticAddress2D accepted = semanticParent(point).getSemanticAddress();

		editGeoElement(requireLookup("D"), "D={false,{1,2,true,true}}");
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		parent.compute();
		parent.compute();
		assertFalse(point.isDefined());
		assertEquals(accepted, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());

		editGeoElement(requireLookup("D"),
				"D={false,{-1.75,-1.25,true,true},{1,2,true,true}}");
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		parent = semanticParent(point);
		parent.compute();
		assertFalse(point.isDefined());
		assertEquals(accepted, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());

		editGeoElement(requireLookup("D"),
				"D={false,{-2,-1,true,true},{1,2,true,true}}");
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertTrue(point.isDefined());
		assertEquals(accepted, semanticParent(point).getSemanticAddress());
		assertEquals(-1.5, point.getInhomX(), 1E-8);
	}

	@Test
	void r639ExtremePeriodicLiftFailsClosedWithoutStaleCoordinates() {
		GeoLocusV2 circle = createPeriodicCircle();
		LocusPointInteractionResult2D seed = resolve(circle, 1, 0, 1E-6);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				seed.getStatus(), seed.getDiagnostic());
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", circle,
				onlyDiscoveredCandidate(seed));
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		final LocusSemanticAddress2D accepted = parent.getSemanticAddress();
		GeoNumeric parameter = assertInstanceOf(GeoNumeric.class,
				parent.getParameterInput().toGeoElement());
		final double originalRawParameter = parameter.getDouble();

		parameter.setValue(Double.MAX_VALUE);
		parameter.updateCascade();
		assertFalse(point.isDefined());
		assertEquals(accepted, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());
		assertNull(parent.getMetricPositionBinding());
		assertTrue(Double.isNaN(point.getInhomX()));
		assertTrue(Double.isNaN(point.getInhomY()));

		parameter.setValue(Double.NaN);
		parameter.updateCascade();
		assertFalse(point.isDefined());
		assertEquals(accepted, parent.getSemanticAddress());
		assertNull(parent.getCurrentSemanticAddress());
		assertTrue(Double.isNaN(point.getInhomX()));

		parameter.setValue(originalRawParameter);
		parameter.updateCascade();
		assertTrue(point.isDefined());
		assertEquals(accepted, parent.getSemanticAddress());
	}

	@Test
	void r640AddressStateCodecRejectsAliasesAndMalformedUtf8() {
		GeoLocusV2 circle = createPeriodicCircle();
		LocusPointInteractionResult2D seed = resolve(circle, 1, 0, 1E-6);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				seed.getStatus(), seed.getDiagnostic());
		LocusSemanticAddress2D address = onlyDiscoveredCandidate(seed).getAddress();
		String prefix = "geocedg-locus-address/v1|";
		String encoded = LocusSemanticAddressState2D.encode(address);
		String[] fields = encoded.substring(prefix.length()).split("\\|", -1);

		assertTrue(LocusSemanticAddressState2D.decode(encoded).matches(address));
		assertEquals(encoded, LocusSemanticAddressState2D.encode(address));

		String[] uppercaseText = fields.clone();
		uppercaseText[0] = uppercaseText[0].toUpperCase(Locale.ROOT);
		assertNotEquals(fields[0], uppercaseText[0]);
		assertThrows(IllegalArgumentException.class, () ->
				LocusSemanticAddressState2D.decode(prefix
						+ String.join("|", uppercaseText)));

		String[] leadingZeroBits = fields.clone();
		leadingZeroBits[3] = "0" + leadingZeroBits[3];
		assertThrows(IllegalArgumentException.class, () ->
				LocusSemanticAddressState2D.decode(prefix
						+ String.join("|", leadingZeroBits)));

		String[] signedLift = fields.clone();
		signedLift[4] = "+" + signedLift[4];
		assertThrows(IllegalArgumentException.class, () ->
				LocusSemanticAddressState2D.decode(prefix
						+ String.join("|", signedLift)));

		String[] malformedUtf8 = fields.clone();
		malformedUtf8[0] = "ff";
		assertThrows(IllegalArgumentException.class, () ->
				LocusSemanticAddressState2D.decode(prefix
						+ String.join("|", malformedUtf8)));
	}

	@Test
	void r641RemovalCleansOnlyExclusiveDedicatedAuxiliaries() {
		GeoLocusV2 locus = createLine();
		GeoPoint interaction =
				LocusV2PublicOperations.createInteractiveSemanticPoint(
						getConstruction(), "P", locus,
						resolve(locus, -0.5, 0, 0.1).getUniqueCandidate());
		AlgoSemanticLocusPoint2D parent = semanticParent(interaction);
		GeoText dedicatedBranch = parent.getBranchInput();
		GeoElement dedicatedParameter = parent.getParameterInput().toGeoElement();
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();

		assertTrue(getConstruction().isInConstructionList(dedicatedBranch));
		assertTrue(getConstruction().isInConstructionList(dedicatedParameter));
		interaction.remove();
		assertFalse(getConstruction().isInConstructionList(dedicatedBranch));
		assertFalse(getConstruction().isInConstructionList(dedicatedParameter));
		assertNull(registry.getPersistentGeoId(dedicatedBranch));
		assertNull(registry.getPersistentGeoId(dedicatedParameter));

		GeoText sharedBranch = add("b=\"" + BRANCH + "\"");
		GeoNumeric sharedParameter = add("u=-0.5");
		GeoPoint explicit = add("E=Point(L,b,u)");
		GeoNumeric dependent = add("K=u+1");
		explicit.remove();

		assertSame(sharedBranch, requireLookup("b"));
		assertSame(sharedParameter, requireLookup("u"));
		assertSame(dependent, requireLookup("K"));
		assertTrue(dependent.isDefined());
		assertEquals(0.5, dependent.getDouble(), 0);
	}

	@Test
	void r642ActualSpanPermutationPreservesKnotAndCrossingCandidates() {
		GeoLocusV2 knotSpline = createFivePointLineSpline("K");
		LocusPoint2D knot = evaluate(knotSpline, 0.5);
		LocusPointInteractionResult2D knotBaseline = resolve(knotSpline,
				knot.getX(), knot.getY(), 1E-6);
		reversePolynomialSpanInventory(knotSpline);
		LocusPointInteractionResult2D knotReversed = resolve(knotSpline,
				knot.getX(), knot.getY(), 1E-6);

		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				knotBaseline.getStatus(), knotBaseline.getDiagnostic());
		assertEquivalentCandidateResults(knotBaseline, knotReversed);
		assertEquals(0.5, knotReversed.getUniqueCandidate().getAddress()
				.getCanonicalParameter(), 1E-10);

		GeoLocusV2 crossingSpline = createSelfIntersectingSpline("X");
		LocusPoint2D crossing = evaluate(crossingSpline, 0.25);
		LocusPointInteractionResult2D crossingBaseline = resolve(crossingSpline,
				crossing.getX(), crossing.getY(), 1E-6);
		reversePolynomialSpanInventory(crossingSpline);
		LocusPointInteractionResult2D crossingReversed = resolve(crossingSpline,
				crossing.getX(), crossing.getY(), 1E-6);

		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				crossingBaseline.getStatus(), crossingBaseline.getDiagnostic());
		assertTrue(crossingBaseline.getCandidates().size() >= 2);
		assertEquivalentCandidateResults(crossingBaseline, crossingReversed);
		assertNoPresentationReads(knotReversed);
		assertNoPresentationReads(crossingReversed);
	}

	@Test
	void r643EverySplineAndSimilarityCandidateIsForwardVerified() {
		GeoLocusV2 spline = createSelfIntersectingSpline("S");
		LocusPoint2D crossing = evaluate(spline, 0.25);
		double radius = 1E-6;
		LocusPointInteractionResult2D splineResult = resolve(spline,
				crossing.getX(), crossing.getY(), radius);

		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				splineResult.getStatus(), splineResult.getDiagnostic());
		assertAllCandidatesForwardVerified(spline, splineResult, crossing.getX(),
				crossing.getY(), radius);

		GeoLocusV2 translated = add("T=Translate(S,(3,4))");
		LocusPoint2D translatedCrossing = evaluate(translated, 0.25);
		LocusPointInteractionResult2D translatedResult = resolve(translated,
				translatedCrossing.getX(), translatedCrossing.getY(), radius);

		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				translatedResult.getStatus(), translatedResult.getDiagnostic());
		assertAllCandidatesForwardVerified(translated, translatedResult,
				translatedCrossing.getX(), translatedCrossing.getY(), radius);
		assertNoPresentationReads(splineResult);
		assertNoPresentationReads(translatedResult);
	}

	@Test
	void r644EncodedOrdinaryPointInputsKeepTheirPresentationOwnership() {
		GeoLocusV2 locus = createLine();
		LocusSemanticAddress2D address = resolve(locus, -0.5, 0, 0.1)
				.getUniqueCandidate().getAddress();
		GeoText branch = new GeoText(getConstruction());
		branch.setTextString(LocusSemanticAddressState2D.encode(address));
		branch.setAuxiliaryObject(false);
		branch.setEuclidianVisible(true);
		branch.setRestrictedEuclidianVisibility(false);
		GeoNumeric parameter = new GeoNumeric(getConstruction(), -0.5);
		parameter.setAuxiliaryObject(false);
		parameter.setEuclidianVisible(true);
		parameter.setRestrictedEuclidianVisibility(false);

		GeoPoint point = LocusV2PublicOperations.createSemanticPoint(
				getConstruction(), "E", locus, branch, parameter);
		point.updateCascade();
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId pointId = registry.getPersistentGeoId(point);
		final PersistentGeoId branchId = registry.getPersistentGeoId(branch);
		final PersistentGeoId parameterId = registry.getPersistentGeoId(parameter);

		assertTrue(point.isDefined());
		assertEquals(address, parent.getSemanticAddress());
		assertFalse(branch.isAuxiliaryObject());
		assertTrue(branch.isEuclidianVisible());
		assertFalse(parameter.isAuxiliaryObject());
		assertTrue(parameter.isEuclidianVisible());
		assertNotNull(pointId);
		assertNotNull(branchId);
		assertNotNull(parameterId);
		GeoIdentityRecord pointRecord = registry.getGeoRecord(
				pointId);
		assertEquals("VALUE", pointRecord.getStableOutputRole());

		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		GeoPoint reopened = assertInstanceOf(GeoPoint.class, requireLookup("E"));
		AlgoSemanticLocusPoint2D reopenedParent = semanticParent(reopened);
		GeoText reopenedBranch = reopenedParent.getBranchInput();
		GeoNumeric reopenedParameter = assertInstanceOf(GeoNumeric.class,
				reopenedParent.getParameterInput().toGeoElement());

		assertTrue(reopened.isDefined());
		assertEquals(address, reopenedParent.getSemanticAddress());
		assertEquals(LocusSemanticAddressState2D.encode(address),
				reopenedBranch.getTextString());
		assertEquals(-0.5, reopenedParameter.getDouble(), 0);
		assertFalse(reopenedBranch.isAuxiliaryObject());
		assertTrue(reopenedBranch.isEuclidianVisible());
		assertFalse(reopenedParameter.isAuxiliaryObject());
		assertTrue(reopenedParameter.isEuclidianVisible());
		assertEquals(pointId, registry.getPersistentGeoId(reopened));
		assertEquals(branchId, registry.getPersistentGeoId(reopenedBranch));
		assertEquals(parameterId, registry.getPersistentGeoId(reopenedParameter));
		assertEquals("VALUE", registry.getGeoRecord(pointId)
				.getStableOutputRole());
	}

	@Test
	void r645PersistedComponentLineageWinsAtSharedSemanticEndpoint() {
		GeoLocusV2 locus = createLine();
		LocusDefinition2D previous = locus.getSemanticDefinition();
		LocusBranch2D previousBranch = previous.getBranches().get(0);
		LocusInterval2D left = new LocusInterval2D(-2, 0, true, true);
		LocusInterval2D right = new LocusInterval2D(0, 2, true, true);
		LocusBranch2D sharedEndpointBranch = new LocusBranch2D(BRANCH,
				previousBranch.getDeclaredDriverDomain(), List.of(left, right),
				previousBranch.getOrientation(), previousBranch.getProvenance(),
				previousBranch.getLineage(), previousBranch.getProperties(),
				previousBranch.getQuality());
		locus.publishSemanticDefinition(new LocusDefinition2D(
				locus.getLocusIdentity(), previous.getSemanticRevision() + 1,
				DefinitionStatus.VALID, previous.getProvider(),
				List.of(sharedEndpointBranch), previous.getEvaluatorCapability(),
				previous.getDeterminism(), "r6-shared-endpoint/v1",
				locus.getInstrumentation()));
		LocusPointInteractionResult2D ambiguous = resolve(locus, 0, 0, 1E-8);

		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				ambiguous.getStatus(), ambiguous.getDiagnostic());
		assertEquals(SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS,
				ambiguous.getSearchCoverage());
		assertEquals(2, ambiguous.getCandidates().size());
		assertNotEquals(ambiguous.getCandidates().get(0).getAddress()
				.getComponentLineageKey(), ambiguous.getCandidates().get(1)
						.getAddress().getComponentLineageKey());
		final List<LocusSemanticAddress2D> canonicalAmbiguousAddresses =
				addresses(ambiguous);
		LocusSemanticAddress2D rightAddress = new LocusSemanticAddress2D(
				locus.getPersistentLocusId(), previous.getProvider().getProviderId(),
				BRANCH, LocusComponentLineage2D.create(BRANCH, right), 0, 0,
				SeamSide.NOT_PERIODIC);
		GeoText branch = new GeoText(getConstruction());
		branch.setTextString(LocusSemanticAddressState2D.encode(rightAddress));
		GeoNumeric parameter = new GeoNumeric(getConstruction(), 0);

		GeoPoint point = LocusV2PublicOperations.createSemanticPoint(
				getConstruction(), "E", locus, branch, parameter);
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		GeoText unqualifiedBranch = new GeoText(getConstruction());
		unqualifiedBranch.setTextString(BRANCH);
		GeoNumeric unqualifiedParameter = new GeoNumeric(getConstruction(), 0);
		GeoPoint unqualified = LocusV2PublicOperations.createSemanticPoint(
				getConstruction(), "U", locus, unqualifiedBranch,
				unqualifiedParameter);

		assertTrue(point.isDefined());
		assertEquals(rightAddress, parent.getSemanticAddress());
		assertFalse(unqualified.isDefined());
		assertNull(semanticParent(unqualified).getSemanticAddress());

		LocusDefinition2D ordered = locus.getSemanticDefinition();
		LocusBranch2D reversedBranch = new LocusBranch2D(BRANCH,
				previousBranch.getDeclaredDriverDomain(), List.of(right, left),
				previousBranch.getOrientation(), previousBranch.getProvenance(),
				previousBranch.getLineage(), previousBranch.getProperties(),
				previousBranch.getQuality());
		locus.publishSemanticDefinition(new LocusDefinition2D(
				locus.getLocusIdentity(), ordered.getSemanticRevision() + 1,
				DefinitionStatus.VALID, ordered.getProvider(),
				List.of(reversedBranch), ordered.getEvaluatorCapability(),
				ordered.getDeterminism(), "r6-shared-endpoint-reordered/v1",
				locus.getInstrumentation()));
		parent.compute();
		semanticParent(unqualified).compute();
		LocusPointInteractionResult2D reordered = resolve(locus, 0, 0, 1E-8);

		assertTrue(point.isDefined());
		assertEquals(rightAddress, parent.getSemanticAddress());
		assertFalse(unqualified.isDefined());
		assertNull(semanticParent(unqualified).getSemanticAddress());
		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				reordered.getStatus(), reordered.getDiagnostic());
		assertEquals(canonicalAmbiguousAddresses, addresses(reordered));
		assertNoPresentationReads(reordered);
	}

	@Test
	void r646NarrowGenericMinimumCannotUpgradeBoundedCoverage() {
		double parameter = 0.123456789;
		GeoLocusV2 narrow = createScalarLocus("N", "r6NarrowT", "NQ",
				"(0.000001*r6NarrowT,1-exp(-100000000*(r6NarrowT-"
						+ parameter + ")^2))", "{false,{-1,1,true,true}}");
		LocusPoint2D exact = evaluateFirstBranch(narrow, parameter);
		double targetX = 0.000001 * parameter;
		LocusPointInteractionResult2D result = resolve(narrow, targetX, 0,
				1E-4);

		assertEquals(targetX, exact.getX(), 1E-14);
		assertEquals(0, exact.getY(), 1E-14);
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus(), result.getDiagnostic());
		assertNotEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus());
		assertNotEquals(LocusPointInteractionStatus2D.NO_ADMISSIBLE_PREIMAGE,
				result.getStatus());
		assertEquals(SearchCoverage.BOUNDED_EVALUATOR_SEARCH,
				result.getSearchCoverage());
		assertNull(result.getUniqueCandidate());
		assertDiscoveredCandidatesForwardVerified(narrow, result, targetX, 0,
				1E-4);
		assertNoPresentationReads(result);
	}

	@Test
	void r647FailedMoveRollsBackTheWholeConstructionAndCanRecover() {
		GeoLocusV2 locus = createLine();
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", locus,
				resolve(locus, -0.5, 0, 0.1).getUniqueCandidate());
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		GeoText branch = parent.getBranchInput();
		GeoNumeric parameter = assertInstanceOf(GeoNumeric.class,
				parent.getParameterInput().toGeoElement());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId locusId = locus.getPersistentLocusId();
		final PersistentGeoId pointId = registry.getPersistentGeoId(point);
		final PersistentGeoId branchId = registry.getPersistentGeoId(branch);
		final PersistentGeoId parameterId = registry.getPersistentGeoId(parameter);
		final LocusSemanticAddress2D address = parent.getSemanticAddress();
		final String encodedAddress = branch.getTextString();
		final double rawParameter = parameter.getDouble();
		int constructionSize = getConstruction().getGeoSetConstructionOrder().size();
		final int identityRecords = registry.getRecords().size();
		String xml = getApp().getXML();
		AtomicBoolean callbackAttempted = new AtomicBoolean();
		View hostileView = mock(View.class);
		doAnswer(invocation -> {
			if (callbackAttempted.compareAndSet(false, true)) {
				add("R6AtomicLeak=42");
				throw new IllegalStateException("r6 injected move publication failure");
			}
			return null;
		}).when(hostileView).update(any(GeoElement.class));

		getKernel().attach(hostileView);
		try {
			RuntimeException failure = assertThrows(RuntimeException.class,
					() -> LocusV2PublicOperations.moveInteractiveSemanticPoint(
							point, 1, 0, policy(0.1)));
			assertTrue(failure.getMessage().contains("injected move publication"),
					failure::getMessage);
		} finally {
			getKernel().detach(hostileView);
		}

		assertTrue(callbackAttempted.get());
		assertNull(lookup("R6AtomicLeak"));
		assertEquals(xml, getApp().getXML());
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(identityRecords, registry.getRecords().size());
		GeoLocusV2 restoredLocus = assertInstanceOf(GeoLocusV2.class,
				requireLookup("L"));
		GeoPoint restoredPoint = assertInstanceOf(GeoPoint.class,
				requireLookup("P"));
		AlgoSemanticLocusPoint2D restoredParent = semanticParent(restoredPoint);
		GeoText restoredBranch = restoredParent.getBranchInput();
		GeoNumeric restoredParameter = assertInstanceOf(GeoNumeric.class,
				restoredParent.getParameterInput().toGeoElement());

		assertEquals(locusId, restoredLocus.getPersistentLocusId());
		assertEquals(pointId, registry.getPersistentGeoId(restoredPoint));
		assertEquals(branchId, registry.getPersistentGeoId(restoredBranch));
		assertEquals(parameterId, registry.getPersistentGeoId(restoredParameter));
		assertEquals(address, restoredParent.getSemanticAddress());
		assertEquals(encodedAddress, restoredBranch.getTextString());
		assertEquals(rawParameter, restoredParameter.getDouble(), 0);
		assertEquals(-0.5, restoredPoint.getInhomX(), 1E-10);
		assertEquals(0, restoredPoint.getInhomY(), 1E-10);

		LocusPointInteractionResult2D recovered =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(restoredPoint,
						1, 0, policy(0.1));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				recovered.getStatus(), recovered.getDiagnostic());
		assertEquals(SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS,
				recovered.getSearchCoverage());
		assertNotEquals(address, restoredParent.getSemanticAddress());
		assertEquals(recovered.getUniqueCandidate().getAddress(),
				restoredParent.getSemanticAddress());
		assertEquals(1, restoredPoint.getInhomX(), 1E-10);
		assertEquals(0, restoredPoint.getInhomY(), 1E-10);
	}

	@Test
	void r648ExcessivePolynomialCompositionDepthStopsPointAndPairTraversal() {
		GeoLocusV2 source = createLineSpline("S");
		LocusDefinition2D previous = source.getSemanticDefinition();
		LocusEvaluator2D evaluator = previous.getEvaluatorCapability();
		PiecewisePolynomialLocus2D polynomial = assertInstanceOf(
				PiecewisePolynomialLocus2D.class, evaluator);
		ExcessiveDepthPolynomialEvaluator excessive =
				new ExcessiveDepthPolynomialEvaluator(evaluator, polynomial);
		source.publishSemanticDefinition(new LocusDefinition2D(
				previous.getLocusIdentity(), previous.getSemanticRevision() + 1,
				previous.getDefinitionStatus(), previous.getProvider(),
				previous.getBranches(), excessive, previous.getDeterminism(),
				excessive.getPolynomialCapabilitySignature(),
				previous.getInstrumentation()));

		LocusPointInteractionResult2D pointResult = resolve(source, 0, 0, 0.1);

		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				pointResult.getStatus(), pointResult.getDiagnostic());
		assertEquals(SearchCoverage.NOT_APPLICABLE,
				pointResult.getSearchCoverage());
		assertTrue(pointResult.getDiagnostic().toLowerCase(Locale.ROOT)
				.contains("composition depth"), pointResult.getDiagnostic());
		assertEquals(0, pointResult.getInstrumentation().getSemanticEvaluations());
		assertEquals(0, pointResult.getInstrumentation().getPolynomialSpans());
		assertFalse(excessive.wasPolynomialInventoryRead());

		add("TA=(-2,-2)");
		add("TB=(-2/3,-2/3)");
		add("TC=(2/3,2/3)");
		add("TD=(2,2)");
		GeoLocusV2 other = add("T=SplineV2({TA,TB,TC,TD},3)");
		assertSame(excessive,
				source.getSemanticDefinition().getEvaluatorCapability());
		GeoLocusIntersectionResult pair = add("R=Intersect(S,T)");

		assertNotNull(other);
		assertNotNull(pair);
		assertNotNull(pair.getIntersectionResult());
		assertSame(excessive,
				source.getSemanticDefinition().getEvaluatorCapability());
		assertFalse(excessive.wasPolynomialInventoryRead());
	}

	private LocusPointInteractionResult2D resolve(GeoLocusV2 source,
			double targetX, double targetY, double radius) {
		return resolver.resolve(new LocusPointInteractionQuery2D(source, targetX,
				targetY, policy(radius)));
	}

	private LocusPointInteractionResult2D moveToParameter(GeoLocusV2 source,
			GeoPoint point, double parameter, double radius) {
		LocusPoint2D target = evaluate(source, parameter);
		LocusPointInteractionResult2D result =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
						target.getX(), target.getY(), policy(radius));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus(), result.getDiagnostic());
		assertEquals(SearchCoverage.ALL_EXPLICIT_POLYNOMIAL_SPANS,
				result.getSearchCoverage());
		assertEquals(1, result.getCandidates().size());
		assertNoPresentationReads(result);
		return result;
	}

	private void assertPeriodicInteractionState(GeoPoint point,
			GeoLocusV2 source, PersistentGeoId pointId, PersistentGeoId sourceId,
			String branch, String component, double canonicalParameter,
			long periodicLift, int constructionSize) {
		assertSame(point, requireLookup("P"));
		assertSame(source, semanticParent(point).getSource());
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
		assertEquals(sourceId, source.getPersistentLocusId());
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());
		LocusSemanticAddress2D address = semanticParent(point).getSemanticAddress();
		assertEquals(sourceId, address.getSourceLocusId());
		assertEquals(branch, address.getBranchKey());
		assertEquals(component, address.getComponentLineageKey());
		assertEquals(canonicalParameter, address.getCanonicalParameter(), 1E-9);
		assertEquals(periodicLift, address.getPeriodicLift());
		assertEquals(SeamSide.INTERIOR, address.getSeamSide());
		assertTrue(point.isDefined());
	}

	private static LocusPointInteractionPolicy2D policy(double radius) {
		return LocusPointInteractionPolicy2D.initial(radius);
	}

	private static AlgoSemanticLocusPoint2D semanticParent(GeoPoint point) {
		return assertInstanceOf(AlgoSemanticLocusPoint2D.class,
				point.getParentAlgorithm());
	}

	private static void assertNoPresentationReads(
			LocusPointInteractionResult2D result) {
		assertEquals(0, result.getInstrumentation().getRenderReads());
		assertEquals(0, result.getInstrumentation().getViewportReads());
		assertEquals(0, result.getInstrumentation().getPixelReads());
	}

	private static List<String> branchKeys(
			LocusPointInteractionResult2D result) {
		return result.getCandidates().stream().map(candidate -> candidate
				.getAddress().getBranchKey()).toList();
	}

	private static List<LocusSemanticAddress2D> addresses(
			LocusPointInteractionResult2D result) {
		return result.getCandidates().stream()
				.map(LocusPointInteractionCandidate2D::getAddress).toList();
	}

	private static LocusPointInteractionCandidate2D onlyDiscoveredCandidate(
			LocusPointInteractionResult2D result) {
		assertEquals(1, result.getCandidates().size(), result.getDiagnostic());
		return result.getCandidates().get(0);
	}

	private static void reversePolynomialSpanInventory(GeoLocusV2 source) {
		LocusDefinition2D previous = source.getSemanticDefinition();
		LocusEvaluator2D evaluator = previous.getEvaluatorCapability();
		PiecewisePolynomialLocus2D polynomial = assertInstanceOf(
				PiecewisePolynomialLocus2D.class, evaluator);
		String branchKey = previous.getBranches().get(0).getBranchKey();
		assertTrue(polynomial.getPolynomialSpanCount(branchKey) > 1);
		ReversedSpanPolynomialEvaluator reversed =
				new ReversedSpanPolynomialEvaluator(evaluator, polynomial);
		source.publishSemanticDefinition(new LocusDefinition2D(
				previous.getLocusIdentity(), previous.getSemanticRevision() + 1,
				previous.getDefinitionStatus(), previous.getProvider(),
				previous.getBranches(), reversed, previous.getDeterminism(),
				reversed.getPolynomialCapabilitySignature(),
				previous.getInstrumentation()));
	}

	private static void assertEquivalentCandidateResults(
			LocusPointInteractionResult2D expected,
			LocusPointInteractionResult2D actual) {
		assertEquals(expected.getStatus(), actual.getStatus(),
				actual.getDiagnostic());
		assertEquals(expected.getSearchCoverage(), actual.getSearchCoverage());
		assertEquals(addresses(expected), addresses(actual));
		assertEquals(expected.getCandidates().size(), actual.getCandidates().size());
		for (int index = 0; index < expected.getCandidates().size(); index++) {
			LocusPointInteractionCandidate2D first = expected.getCandidates()
					.get(index);
			LocusPointInteractionCandidate2D second = actual.getCandidates()
					.get(index);
			assertEquals(first.getEvaluatedPoint(), second.getEvaluatedPoint());
			assertEquals(first.getWorldDistance(), second.getWorldDistance(), 0);
			assertEquals(first.getIntervalLower(), second.getIntervalLower(), 0);
			assertEquals(first.getIntervalUpper(), second.getIntervalUpper(), 0);
			assertEquals(first.getRegularity(), second.getRegularity());
			assertEquals(first.getNumericGuarantee(), second.getNumericGuarantee());
			assertEquals(first.getMethod(), second.getMethod());
			assertEquals(first.getLocalEvidence().getStatus(),
					second.getLocalEvidence().getStatus());
			assertEquals(first.getLocalEvidence().getMethod(),
					second.getLocalEvidence().getMethod());
			assertEquals(first.getLocalEvidence().getNumericGuarantee(),
					second.getLocalEvidence().getNumericGuarantee());
			assertEquals(first.getLocalEvidence().getDiagnostic(),
					second.getLocalEvidence().getDiagnostic());
		}
		assertEquals(expected.getInstrumentation().getPolynomialSpans(),
				actual.getInstrumentation().getPolynomialSpans());
		assertEquals(expected.getInstrumentation().getRetainedCandidates(),
				actual.getInstrumentation().getRetainedCandidates());
	}

	private static void assertAllCandidatesForwardVerified(GeoLocusV2 source,
			LocusPointInteractionResult2D result, double targetX, double targetY,
			double radius) {
		assertFalse(result.getCandidates().isEmpty());
		assertDiscoveredCandidatesForwardVerified(source, result, targetX, targetY,
				radius);
	}

	private static void assertDiscoveredCandidatesForwardVerified(
			GeoLocusV2 source, LocusPointInteractionResult2D result,
			double targetX, double targetY, double radius) {
		LocusDefinition2D definition = source.getSemanticDefinition();
		for (LocusPointInteractionCandidate2D candidate : result.getCandidates()) {
			LocusSemanticAddress2D address = candidate.getAddress();
			assertEquals(source.getPersistentLocusId(), address.getSourceLocusId());
			assertEquals(definition.getSemanticRevision(),
					candidate.getSourceRevision());
			LocusBranch2D branch = definition.getBranch(address.getBranchKey());
			assertNotNull(branch);
			assertTrue(branch.getValidDomainComponents().stream().anyMatch(component ->
					address.getComponentLineageKey().equals(
							LocusComponentLineage2D.create(
									branch.getBranchKey(), component))
							&& component.contains(address.getCanonicalParameter(),
									definition.getProvider().getDomainEpsilon())));
			try (LocusEvaluationSession2D session =
					LocusEvaluationSession2D.reference()) {
				LocusEvaluation2D forward = definition.evaluate(
						address.getBranchKey(), address.getCanonicalParameter(), session);
				assertTrue(forward.isValid(), forward.getDiagnostic());
				assertNotNull(forward.getPoint());
				assertEquals(forward.getPoint().getX(),
						candidate.getEvaluatedPoint().getX(), 0);
				assertEquals(forward.getPoint().getY(),
						candidate.getEvaluatedPoint().getY(), 0);
				assertEquals(forward.getRegularity(), candidate.getRegularity());
				assertEquals(forward.getQuality().getNumericGuarantee(),
						candidate.getNumericGuarantee());
				double distance = Math.hypot(forward.getPoint().getX() - targetX,
						forward.getPoint().getY() - targetY);
				double tolerance = 32 * Math.ulp(Math.max(1, distance));
				assertEquals(distance, candidate.getWorldDistance(), tolerance);
				assertTrue(distance <= radius);
			}
			assertTrue(candidate.getLocalEvidence().isEstablished());
			assertTrue(candidate.getDiagnostic()
					.startsWith("Forward-verified semantic minimum"));
		}
	}

	private void assertUniquePoint(GeoLocusV2 source, double targetX,
			double targetY, double radius, double expectedX, double expectedY) {
		LocusPointInteractionResult2D result = resolve(source, targetX, targetY,
				radius);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus());
		assertEquals(expectedX, result.getUniqueCandidate().getEvaluatedPoint()
				.getX(), 1E-8);
		assertEquals(expectedY, result.getUniqueCandidate().getEvaluatedPoint()
				.getY(), 1E-8);
	}

	private static LocusPoint2D evaluate(GeoLocusV2 source, double parameter) {
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			LocusEvaluation2D evaluation = source.evaluate(SPLINE_BRANCH, parameter,
					session);
			assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
			return evaluation.getPoint();
		}
	}

	private static LocusPoint2D evaluateFirstBranch(GeoLocusV2 source,
			double parameter) {
		String branchKey = source.getSemanticDefinition().getBranches().get(0)
				.getBranchKey();
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			LocusEvaluation2D evaluation = source.evaluate(branchKey, parameter,
					session);
			assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
			return evaluation.getPoint();
		}
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}

	private GeoLocusV2 createLineSpline(String label) {
		add(label + "A=(-2,0)");
		add(label + "B=(-2/3,0)");
		add(label + "C=(2/3,0)");
		add(label + "D=(2,0)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D},3)");
	}

	private GeoLocusV2 createFivePointLineSpline(String label) {
		add(label + "A=(-2,0)");
		add(label + "B=(-1,0)");
		add(label + "C=(0,0)");
		add(label + "D=(1,0)");
		add(label + "E=(2,0)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D," + label + "E},3)");
	}

	private GeoLocusV2 createSelfIntersectingSpline(String label) {
		add(label + "A=(-1,0)");
		add(label + "B=(0,1)");
		add(label + "C=(1,0)");
		add(label + "D=(0,1)");
		add(label + "E=(-1,0)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D," + label + "E},3)");
	}

	private static final class ReversedSpanPolynomialEvaluator
			implements LocusEvaluator2D, PiecewisePolynomialLocus2D {
		private final LocusEvaluator2D evaluator;
		private final PiecewisePolynomialLocus2D polynomial;

		private ReversedSpanPolynomialEvaluator(LocusEvaluator2D evaluator,
				PiecewisePolynomialLocus2D polynomial) {
			this.evaluator = evaluator;
			this.polynomial = polynomial;
		}

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition,
				LocusBranch2D branch, double canonicalParameter,
				LocusEvaluationSession2D session) {
			return evaluator.evaluate(definition, branch, canonicalParameter, session);
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey, double lower,
				double upper) {
			return polynomial.getInteriorBreakpoints(branchKey, lower, upper);
		}

		@Override
		public boolean supportsPiecewisePolynomial(LocusDefinition2D definition) {
			return polynomial.supportsPiecewisePolynomial(definition);
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			return polynomial.getPolynomialSpanCount(branchKey);
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			return polynomial.getPolynomialSpanLower(branchKey,
					reversedIndex(branchKey, spanIndex));
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			return polynomial.getPolynomialSpanUpper(branchKey,
					reversedIndex(branchKey, spanIndex));
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey, int spanIndex,
				int coordinate) {
			return polynomial.getPolynomialCoefficients(branchKey,
					reversedIndex(branchKey, spanIndex), coordinate);
		}

		@Override
		public double[][] getPolynomialCoordinateCoefficients(String branchKey,
				int spanIndex) {
			return polynomial.getPolynomialCoordinateCoefficients(branchKey,
					reversedIndex(branchKey, spanIndex));
		}

		@Override
		public int getPolynomialCompositionDepth() {
			return polynomial.getPolynomialCompositionDepth();
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey,
				double providerCanonicalParameter) {
			return polynomial.evaluatePolynomialDerivative(branchKey,
					providerCanonicalParameter);
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "r6-test-reversed-span-inventory/v1|delegate="
					+ polynomial.getPolynomialCapabilitySignature();
		}

		private int reversedIndex(String branchKey, int spanIndex) {
			int count = polynomial.getPolynomialSpanCount(branchKey);
			if (spanIndex < 0 || spanIndex >= count) {
				throw new IllegalArgumentException("Unknown reversed test span");
			}
			return count - 1 - spanIndex;
		}
	}

	private static final class ExcessiveDepthPolynomialEvaluator
			implements LocusEvaluator2D, PiecewisePolynomialLocus2D {
		private final LocusEvaluator2D evaluator;
		private final PiecewisePolynomialLocus2D polynomial;
		private boolean polynomialInventoryRead;

		private ExcessiveDepthPolynomialEvaluator(LocusEvaluator2D evaluator,
				PiecewisePolynomialLocus2D polynomial) {
			this.evaluator = evaluator;
			this.polynomial = polynomial;
		}

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition,
				LocusBranch2D branch, double canonicalParameter,
				LocusEvaluationSession2D session) {
			return evaluator.evaluate(definition, branch, canonicalParameter, session);
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey, double lower,
				double upper) {
			return polynomial.getInteriorBreakpoints(branchKey, lower, upper);
		}

		@Override
		public boolean supportsPiecewisePolynomial(LocusDefinition2D definition) {
			return polynomial.supportsPiecewisePolynomial(definition);
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			polynomialInventoryRead = true;
			return polynomial.getPolynomialSpanCount(branchKey);
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			polynomialInventoryRead = true;
			return polynomial.getPolynomialSpanLower(branchKey, spanIndex);
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			polynomialInventoryRead = true;
			return polynomial.getPolynomialSpanUpper(branchKey, spanIndex);
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey, int spanIndex,
				int coordinate) {
			polynomialInventoryRead = true;
			return polynomial.getPolynomialCoefficients(branchKey, spanIndex,
					coordinate);
		}

		@Override
		public double[][] getPolynomialCoordinateCoefficients(String branchKey,
				int spanIndex) {
			polynomialInventoryRead = true;
			return polynomial.getPolynomialCoordinateCoefficients(branchKey,
					spanIndex);
		}

		@Override
		public int getPolynomialCompositionDepth() {
			return PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH + 1;
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey,
				double providerCanonicalParameter) {
			polynomialInventoryRead = true;
			return polynomial.evaluatePolynomialDerivative(branchKey,
					providerCanonicalParameter);
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "r6-excessive-polynomial-composition-depth/v1|delegate="
					+ polynomial.getPolynomialCapabilitySignature();
		}

		private boolean wasPolynomialInventoryRead() {
			return polynomialInventoryRead;
		}
	}

	private static final class ExcessiveDegreePolynomialEvaluator
			implements LocusEvaluator2D, PiecewisePolynomialLocus2D {
		private static final int COEFFICIENT_COUNT = 2049;

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition,
				LocusBranch2D branch, double canonicalParameter,
				LocusEvaluationSession2D session) {
			double highDegreeTerm = 1E-320;
			for (int power = 1; power < COEFFICIENT_COUNT; power++) {
				highDegreeTerm *= canonicalParameter;
			}
			return LocusEvaluation2D.valid(
					new LocusPoint2D(canonicalParameter + highDegreeTerm, 0),
					Regularity.REGULAR, branch.getQuality());
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey,
				double lower, double upper) {
			return List.of();
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			return 1;
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			return -2;
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			return 2;
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey,
				int spanIndex, int coordinate) {
			double[] coefficients = new double[COEFFICIENT_COUNT];
			if (coordinate == 0) {
				coefficients[0] = 1E-320;
				coefficients[COEFFICIENT_COUNT - 2] = 1;
			}
			return coefficients;
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey,
				double providerCanonicalParameter) {
			double highDegreeDerivative = COEFFICIENT_COUNT - 1;
			highDegreeDerivative *= 1E-320;
			for (int power = 1; power < COEFFICIENT_COUNT - 1; power++) {
				highDegreeDerivative *= providerCanonicalParameter;
			}
			return new LocusPoint2D(1 + highDegreeDerivative, 0);
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "r6-excessive-polynomial-degree/v1";
		}
	}

	private static final class SelectiveSpanPolynomialEvaluator
			implements LocusEvaluator2D, PiecewisePolynomialLocus2D {
		private static final double[] LOWER = {-3, -1, 1};
		private static final double[] UPPER = {-1, 1, 3};

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition,
				LocusBranch2D branch, double canonicalParameter,
				LocusEvaluationSession2D session) {
			return LocusEvaluation2D.valid(new LocusPoint2D(canonicalParameter, 0),
					Regularity.REGULAR, branch.getQuality());
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey,
				double lower, double upper) {
			return List.of();
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			return LOWER.length;
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			return LOWER[spanIndex];
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			return UPPER[spanIndex];
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey,
				int spanIndex, int coordinate) {
			return coordinate == 0 ? new double[] {1, 0} : new double[] {0};
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey,
				double providerCanonicalParameter) {
			return new LocusPoint2D(1, 0);
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "r6-selective-spans/v1";
		}
	}
}

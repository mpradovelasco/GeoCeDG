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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusParameterAddressState2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchState2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticAddressState2D;
import org.geocedg.common.kernel.locus.LocusSemanticPointCause2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.RelationIntegrity;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * Increment I1 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 6, 8.1 and 8.2): the parameter address and its
 * codec, point validity separated from the admissibility of a continuation, and the
 * obligations of section 6.6. Criteria 1a (its point part) and 4a.
 */
final class PostR1ParameterAddressTest extends G9U0PublicSurfaceTestBase {

	private GeoLocusV2 outsideClass(String ordinate) {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P)," + ordinate + ")");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		assertNotNull(locus);
		assertTrue(locus.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getContinuousValidComponents().isEmpty());
		return locus;
	}

	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point);
	}

	private PersistentGeoId id(GeoPoint point) {
		return getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
	}

	private static LocusPointInteractionPolicy2D policy(double radius) {
		return LocusPointInteractionPolicy2D.initial(radius);
	}

	private GeoPoint clicked(GeoLocusV2 locus, String label, double x, double y) {
		try (LocusPointCreation2D.Selection selection = LocusPointCreation2D.query(locus, x,
				y, policy(0.05))) {
			assertEquals(LocusPointCreation2D.Route.CONDITIONED_PARAMETER_ADDRESS,
					selection.getRoute(), String.valueOf(selection.getFailure()));
			assertEquals(1, selection.getChoices().size());
			return selection.commit(label, selection.getChoices().get(0));
		}
	}

	@Test
	void aPointByBranchAndParameterIsDefinedByItsOwnEvaluation() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		GeoPoint point = add("A=Point(L,\"" + BRANCH + "\",0.25)");
		assertTrue(point.isDefined());
		assertEquals(0.25, point.getInhomX(), 1e-12);
		assertEquals(Math.exp(0.25), point.getInhomY(), 1e-12);
		AlgoSemanticLocusPoint2D algorithm = parent(point);
		assertEquals(LocusSemanticPointCause2D.RESOLVED, algorithm.getResolutionCause());
		LocusSemanticAddress2D address = algorithm.getCurrentSemanticAddress();
		assertTrue(address.isParameterAddress());
		assertNull(address.getComponentLineageKey());
		assertEquals(0.25, address.getCanonicalParameter(), 0);
		assertEquals(BRANCH, address.getBranchKey());
		assertEquals(locus.getPersistentLocusId(), address.getSourceLocusId());
		// Point validity needs no interval evidence: none was computed for it.
		assertNull(locus.getPublishedConditionedEvidence(BRANCH));
		assertNull(algorithm.getContainingConditionedCell());
		// Nothing is certified by defining the point.
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
		assertTrue(algorithm.getMetricPositionBinding().isValid());
	}

	@Test
	void aPointByParameterAloneFollowsItsNumericInputAndKeepsItsIdentity() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		add("a=0.25");
		GeoPoint point = add("A=Point(L,a)");
		assertTrue(point.isDefined());
		AlgoSemanticLocusPoint2D algorithm = parent(point);
		LocusParameterAddressState2D.Decoded state = LocusParameterAddressState2D.decode(
				algorithm.getBranchInput().getTextString());
		assertNotNull(state);
		assertTrue(state.isInputSelection());
		assertEquals(BRANCH, state.getBranchKey());
		assertTrue(ConstructionGeoRedefineProvider.hasDedicatedPrincipalBranchState(point));
		assertFalse(algorithm.getBranchInput().isEuclidianVisible());
		PersistentGeoId identity = id(point);
		LocusSemanticAddress2D first = algorithm.getCurrentSemanticAddress();
		assertTrue(first.isParameterAddress());

		GeoNumeric input = (GeoNumeric) lookup("a");
		input.setValue(0.75);
		input.updateCascade();
		assertTrue(point.isDefined());
		assertEquals(Math.exp(0.75), point.getInhomY(), 1e-12);
		LocusSemanticAddress2D second = algorithm.getCurrentSemanticAddress();
		assertNotEquals(first, second);
		assertEquals(0.75, second.getCanonicalParameter(), 0);
		assertEquals(identity, id(point));
		assertEquals(locus.getPersistentLocusId(), second.getSourceLocusId());
	}

	@Test
	void theCodecIsItsOwnKindAndNoTextIsReadAsAnother() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		PersistentGeoId source = locus.getPersistentLocusId();
		String provider = locus.getSemanticDefinition().getProvider().getProviderId();
		LocusSemanticAddress2D address = LocusSemanticAddress2D.byParameter(source, provider,
				BRANCH, 0.375, 0, SeamSide.NOT_PERIODIC);
		String text = LocusParameterAddressState2D.encode(address);
		assertTrue(text.startsWith("geocedg-locus-parameter-address/v1|"));
		LocusParameterAddressState2D.Decoded decoded = LocusParameterAddressState2D
				.decode(text);
		assertFalse(decoded.isInputSelection());
		assertTrue(decoded.matches(address));
		assertEquals(address, decoded.toParameterAddress(source));
		assertEquals(text, LocusParameterAddressState2D.encode(
				decoded.toParameterAddress(source)));
		// Neither earlier codec reads it, and it reads neither earlier codec.
		assertNull(LocusSemanticAddressState2D.decode(text));
		assertNull(LocusPrincipalBranchState2D.decode(text));
		LocusSemanticAddress2D selected = new LocusSemanticAddress2D(source, provider, BRANCH,
				"some/component", 0.375, 0, SeamSide.NOT_PERIODIC);
		String earlier = LocusSemanticAddressState2D.encode(selected);
		assertNull(LocusParameterAddressState2D.decode(earlier));
		assertNull(LocusParameterAddressState2D.decode(
				LocusPrincipalBranchState2D.encode(selected)));
		assertNull(LocusParameterAddressState2D.decode(BRANCH));
		// An address is never written in the codec of the other kind.
		assertThrows(IllegalArgumentException.class,
				() -> LocusSemanticAddressState2D.encode(address));
		assertThrows(IllegalArgumentException.class,
				() -> LocusParameterAddressState2D.encode(selected));
		assertNotEquals(address, selected);
		// Malformed texts fail closed.
		for (String malformed : List.of(text + "|", text.substring(0, text.length() - 1),
				text.replace("|0|", "|00|"),
				"geocedg-locus-parameter-address/v1|zz|zz|input",
				"geocedg-locus-parameter-address/v1|" + text.split("\\|")[1])) {
			assertThrows(IllegalArgumentException.class,
					() -> LocusParameterAddressState2D.decode(malformed), malformed);
		}
		String input = LocusParameterAddressState2D.encodeInputSelection(BRANCH, provider);
		assertTrue(LocusParameterAddressState2D.decode(input).isInputSelection());
		assertThrows(IllegalStateException.class,
				() -> LocusParameterAddressState2D.decode(input).toParameterAddress(source));
	}

	@Test
	void anUndefinedPointHasATypedCauseAndRecoversAtTheSameAddress() {
		GeoLocusV2 locus = outsideClass("sqrt(x(P)-0.3)+exp(x(P))");
		add("a=0.6");
		GeoPoint point = add("A=Point(L,a)");
		assertTrue(point.isDefined());
		PersistentGeoId identity = id(point);
		AlgoSemanticLocusPoint2D algorithm = parent(point);
		GeoNumeric input = (GeoNumeric) lookup("a");

		input.setValue(0.1);
		input.updateCascade();
		assertFalse(point.isDefined());
		assertEquals(LocusSemanticPointCause2D.EVALUATION_FAILED,
				algorithm.getResolutionCause());
		assertFalse(algorithm.getMetricPositionBinding().isValid());

		input.setValue(7);
		input.updateCascade();
		assertFalse(point.isDefined());
		assertNotEquals(LocusSemanticPointCause2D.RESOLVED, algorithm.getResolutionCause());

		input.setValue(0.6);
		input.updateCascade();
		assertTrue(point.isDefined());
		assertEquals(0.6, point.getInhomX(), 1e-12);
		assertEquals(identity, id(point));
		assertEquals(0.6, algorithm.getCurrentSemanticAddress().getCanonicalParameter(), 0);
		assertNotNull(locus.getSemanticDefinition());

		GeoPoint outside = add("B=Point(L,\"" + BRANCH + "\",0.1)");
		assertFalse(outside.isDefined());
		assertEquals(LocusSemanticPointCause2D.EVALUATION_FAILED,
				parent(outside).getResolutionCause());
	}

	@Test
	void aCertifiedComponentKeepsItsSelectorAndItsTexts() {
		GeoLocusV2 locus = createLine();
		add("u=0.25");
		GeoPoint principal = add("PO=Point(L,u)");
		GeoPoint explicit = semanticPoint(locus, 0.5);
		for (GeoPoint point : List.of(principal, explicit)) {
			assertTrue(point.isDefined());
			LocusSemanticAddress2D address = parent(point).getCurrentSemanticAddress();
			assertFalse(address.isParameterAddress());
			assertNotNull(address.getComponentLineageKey());
			assertEquals(LocusSemanticAddressState2D.encode(address),
					AlgoSemanticLocusPoint2D.encodeAddressState(address));
		}
		assertNotNull(LocusPrincipalBranchState2D.decode(
				parent(principal).getBranchInput().getTextString()));
		assertNull(LocusParameterAddressState2D.decode(
				parent(principal).getBranchInput().getTextString()));
		// Outside its certified domain a certified locus has no parameter address.
		GeoPoint beyond = add("Z=Point(L,\"" + BRANCH + "\",5)");
		assertFalse(beyond.isDefined());
		assertNull(locus.getConditionedEvidence(BRANCH));
	}

	@Test
	void aClickCreatesAPointThatFollowsItsRegionAndStopsAtABand() {
		GeoLocusV2 locus = outsideClass("sqrt(x(P)-0.3)+exp(x(P))");
		double y06 = Math.sqrt(0.3) + Math.exp(0.6);
		GeoPoint point = clicked(locus, "A", 0.6, y06);
		assertTrue(point.isDefined());
		AlgoSemanticLocusPoint2D algorithm = parent(point);
		LocusSemanticAddress2D created = algorithm.getCurrentSemanticAddress();
		assertTrue(created.isParameterAddress());
		assertEquals(0.6, created.getCanonicalParameter(), 1e-6);
		assertTrue(ConstructionGeoRedefineProvider.hasDedicatedInteractionPointState(point));
		assertEquals(LocusParameterAddressState2D.encode(created),
				algorithm.getBranchInput().getTextString());
		PersistentGeoId identity = id(point);

		LocusPointContinuation2D.Context gesture =
				LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		double y08 = Math.sqrt(0.5) + Math.exp(0.8);
		LocusPointContinuation2D.Result moved = LocusV2PublicOperations
				.moveGeneralInteractiveSemanticPoint(point, 0.8, y08, policy(0.05), gesture);
		assertEquals(LocusPointContinuation2D.Status.CONTINUATION_ESTABLISHED,
				moved.getStatus());
		assertTrue(moved.isConditioned());
		assertEquals(LocusPointContinuation2D.CONDITIONED_METHOD, moved.getMethod());
		assertFalse(moved.getConditionedEvidenceUse().getCells().isEmpty());
		LocusSemanticAddress2D followed = algorithm.getCurrentSemanticAddress();
		assertTrue(followed.isParameterAddress());
		assertEquals(0.8, followed.getCanonicalParameter(), 1e-4);
		assertEquals(identity, id(point));

		// To an explicit parameter of the same region.
		LocusPointContinuation2D.Result toParameter = LocusV2PublicOperations
				.followInteractiveSemanticPointToParameter(point, 0.5, policy(0.05), gesture);
		assertEquals(LocusPointContinuation2D.Status.CONTINUATION_ESTABLISHED,
				toParameter.getStatus());
		assertEquals(0.5, algorithm.getCurrentSemanticAddress().getCanonicalParameter(), 0);

		// Into the absence: the target is refused with the kind of its cell and the point
		// stays defined at its address.
		LocusSemanticAddress2D kept = algorithm.getCurrentSemanticAddress();
		LocusPointContinuation2D.Result refused = LocusV2PublicOperations
				.followInteractiveSemanticPointToParameter(point, 0.1, policy(0.05), gesture);
		assertEquals(LocusPointContinuation2D.Status.CONTINUATION_REACHED_BAND,
				refused.getStatus());
		assertEquals(LocusConditionedCellKind2D.ABSENT_BAND, refused.getBandKind());
		assertTrue(refused.isRecoverableRefusal());
		assertTrue(point.isDefined());
		assertEquals(kept, algorithm.getCurrentSemanticAddress());

		// By the pointer, toward the edge: never beyond the region, never a neighbour.
		double before = algorithm.getCurrentSemanticAddress().getCanonicalParameter();
		for (int step = 0; step < 12; step++) {
			double target = 0.5 - 0.03 * (step + 1);
			LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, target,
					Math.sqrt(Math.max(0, target - 0.3)) + Math.exp(target), policy(0.05),
					gesture);
			double now = algorithm.getCurrentSemanticAddress().getCanonicalParameter();
			assertTrue(point.isDefined());
			assertTrue(now > 0.3 && now <= before + 1e-12, "parameter " + now);
			before = now;
		}
		assertEquals(identity, id(point));
	}

	@Test
	void aValidPointInsideABandIsDefinedAndHasNoContinuation() {
		// A transition band and a discontinuity band, each with a valid parameter inside.
		// The jump at 0.75 is across the curve; a jump along the tangent would leave no trace
		// in chord deviation or turning, which is the declared limit of the traversal.
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P)+floor(x(P)+0.25),sqrt(x(P)-0.3)+exp(x(P)))");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(BRANCH);
		LocusConditionedCell2D transition = null;
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			if (cell.getKind() == LocusConditionedCellKind2D.TRANSITION_BAND) {
				transition = cell;
			}
		}
		assertNotNull(transition);
		assertNotNull(evidence.bandAt(0.75));
		assertEquals(LocusConditionedCellKind2D.DISCONTINUITY_BAND,
				evidence.bandAt(0.75).getKind());
		String provider = locus.getSemanticDefinition().getProvider().getProviderId();
		double[] inside = {transition.getUpper(), 0.75};
		LocusConditionedCellKind2D[] kinds = {LocusConditionedCellKind2D.TRANSITION_BAND,
				LocusConditionedCellKind2D.DISCONTINUITY_BAND};
		for (int index = 0; index < inside.length; index++) {
			LocusSemanticAddress2D address = LocusSemanticAddress2D.byParameter(
					locus.getPersistentLocusId(), provider, BRANCH, inside[index], 0,
					SeamSide.NOT_PERIODIC);
			GeoPoint point = LocusV2PublicOperations.createConditionedInteractivePoint(
					getConstruction(), "B" + index, locus, address);
			AlgoSemanticLocusPoint2D algorithm = parent(point);
			assertTrue(point.isDefined(), "inside " + kinds[index]);
			assertEquals(address, algorithm.getCurrentSemanticAddress());
			assertEquals(kinds[index], algorithm.getContainingConditionedCell().getKind());

			LocusPointContinuation2D.Context gesture =
					LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
			LocusPointContinuation2D.Result follow = LocusV2PublicOperations
					.followInteractiveSemanticPointToParameter(point, 0.5, policy(0.05),
							gesture);
			assertEquals(LocusPointContinuation2D.Status.CONTINUATION_START_IN_BAND,
					follow.getStatus());
			assertEquals(kinds[index], follow.getBandKind());
			LocusPointContinuation2D.Result drag = LocusV2PublicOperations
					.moveGeneralInteractiveSemanticPoint(point, 0.5,
							Math.sqrt(0.2) + Math.exp(0.5), policy(0.05), gesture);
			assertEquals(LocusPointContinuation2D.Status.CONTINUATION_START_IN_BAND,
					drag.getStatus());
			// Refused, and the point keeps its address, its identity and its definition.
			assertTrue(point.isDefined());
			assertEquals(address, algorithm.getCurrentSemanticAddress());
		}

		// Two spans of different regions: a target in another region is refused as such.
		LocusSemanticAddress2D start = LocusSemanticAddress2D.byParameter(
				locus.getPersistentLocusId(), provider, BRANCH, 0.5, 0, SeamSide.NOT_PERIODIC);
		GeoPoint mover = LocusV2PublicOperations.createConditionedInteractivePoint(
				getConstruction(), "M", locus, start);
		LocusPointContinuation2D.Context gesture =
				LocusV2PublicOperations.beginInteractiveSemanticPointDrag(mover);
		assertEquals(LocusPointContinuation2D.Status.TARGET_IN_ANOTHER_REGION,
				LocusV2PublicOperations.followInteractiveSemanticPointToParameter(mover, 0.9,
						policy(0.05), gesture).getStatus());
		assertEquals(start, parent(mover).getCurrentSemanticAddress());
		// An explicit redefinition is not a continuation and is always possible.
		add("r=0.9");
		GeoPoint redefined = add("R=Point(L,r)");
		assertTrue(redefined.isDefined());
		assertEquals(0.9, parent(redefined).getCurrentSemanticAddress()
				.getCanonicalParameter(), 0);
	}

	@Test
	void anAddressIsUnchangedByThePublicationsOfTheEvidence() {
		GeoLocusV2 locus = outsideClass("x(P)+0*sqrt((x(P)-0.3)^2-1E-8)+0*exp(x(P))");
		GeoPoint point = add("A=Point(L,\"" + BRANCH + "\",0.6)");
		AlgoSemanticLocusPoint2D algorithm = parent(point);
		LocusSemanticAddress2D address = algorithm.getCurrentSemanticAddress();
		PersistentGeoId identity = id(point);
		// The evidence is computed, then refined by a confirmed contradiction elsewhere.
		LocusConditionedEvidence2D before = locus.getConditionedEvidence(BRANCH);
		assertTrue(before.inOneRegion(0.1, 0.9));
		assertEquals(ReportOutcome.CONFIRMED,
				locus.reportConditionedContradiction(BRANCH, 0.3));
		assertFalse(locus.getConditionedEvidence(BRANCH).inOneRegion(0.1, 0.9));
		assertTrue(point.isDefined());
		assertEquals(address, algorithm.getCurrentSemanticAddress());
		assertEquals(identity, id(point));
		assertEquals(LocusConditionedCellKind2D.SPAN,
				algorithm.getContainingConditionedCell().getKind());

		// A point inside the hole is undefined and its evaluation contradicts the span that
		// the first content published there: the producer is told and confirms it itself.
		GeoPoint hole = add("H=Point(L,\"" + BRANCH + "\",0.30001)");
		assertFalse(hole.isDefined());
		assertEquals(LocusSemanticPointCause2D.EVALUATION_FAILED,
				parent(hole).getResolutionCause());
		assertTrue(locus.getConditionedEvidence(BRANCH).getContradictions().contains(0.3));
	}

	@Test
	void anInvalidPointInsideASpanIsReportedToTheProducer() {
		GeoLocusV2 locus = outsideClass("x(P)+0*sqrt((x(P)-0.3)^2-1E-8)+0*exp(x(P))");
		LocusConditionedEvidence2D before = locus.getConditionedEvidence(BRANCH);
		assertTrue(before.inOneRegion(0.1, 0.9));
		GeoPoint dependent = add("D=Point(L,\"" + BRANCH + "\",0.6)");
		assertTrue(dependent.isDefined());
		GeoPoint hole = add("H=Point(L,\"" + BRANCH + "\",0.30001)");
		assertFalse(hole.isDefined());
		LocusConditionedEvidence2D after = locus.getConditionedEvidence(BRANCH);
		assertEquals(2, locus.getConditionedPublicationCounter(BRANCH));
		assertEquals(List.of(0.30001), after.getContradictions());
		assertFalse(after.inOneRegion(0.1, 0.9));
		// The publication is an update of the locus: its dependents were recomputed and a
		// point whose own evaluation is valid stays defined at its address.
		assertTrue(dependent.isDefined());
		assertEquals(0.6, parent(dependent).getCurrentSemanticAddress()
				.getCanonicalParameter(), 0);
		assertEquals(before.getRevision(), locus.getSemanticRevision());
	}

	@Test
	void underTheConstructiveAuthorityThePointExistsWithItsIntegrityNotEstablished() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		GeoPoint clicked = clicked(locus, "C", 0.5, Math.exp(0.5));
		AuthorityPolicyAdoption.adopt(getConstruction());
		locus = (GeoLocusV2) lookup("L");
		GeoPoint point = add("A=Point(L,\"" + BRANCH + "\",0.25)");
		// Criterion 1a, point part: the point is published; conditioned evidence never
		// grants RELAXED, and a consumer that requires an integrity keeps failing closed.
		assertTrue(point.isDefined());
		assertTrue(parent(point).getCurrentSemanticAddress().isParameterAddress());
		assertEquals(RelationIntegrity.NOT_ESTABLISHED,
				parent(point).getParameterIncidenceIntegrity());
		clicked = (GeoPoint) lookup("C");
		assertTrue(clicked.isDefined());
		LocusPointContinuation2D.Context gesture =
				LocusV2PublicOperations.beginInteractiveSemanticPointDrag(clicked);
		assertEquals(LocusPointContinuation2D.Status.AUTHORITY_NOT_ESTABLISHED,
				LocusV2PublicOperations.followInteractiveSemanticPointToParameter(clicked,
						0.7, policy(0.05), gesture).getStatus());
		// A certified address states no integrity through this accessor.
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED, locus
				.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getCompleteness());
	}

	@Test
	void aRegionOfAWholePeriodIsFollowedAcrossItsSeam() {
		add("c=Circle((0,0),1)");
		add("E=Point(c)");
		add("Q=(x(E)*exp(0*y(E)),y(E))");
		GeoLocusV2 locus = add("L=LocusV2(Q,E)");
		assertNotNull(locus);
		assertTrue(locus.getSemanticDefinition().getProvider().isPeriodic());
		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(BRANCH);
		assertNotNull(evidence);
		assertEquals(1, evidence.getRegions().size());
		assertTrue(evidence.seamNodesAgree());
		GeoPoint point = clicked(locus, "A", Math.cos(3.0), Math.sin(3.0));
		AlgoSemanticLocusPoint2D algorithm = parent(point);
		assertEquals(3.0, algorithm.getCurrentSemanticAddress().getCanonicalParameter(),
				1e-6);
		PersistentGeoId identity = id(point);
		LocusPointContinuation2D.Context gesture =
				LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		// Across the seam at -pi = pi, by the pointer, in small steps.
		for (int step = 1; step <= 8; step++) {
			double angle = 3.0 + 0.05 * step;
			assertEquals(LocusPointContinuation2D.Status.CONTINUATION_ESTABLISHED,
					LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
							Math.cos(angle), Math.sin(angle), policy(0.05), gesture)
							.getStatus(), "angle " + angle);
		}
		LocusSemanticAddress2D after = algorithm.getCurrentSemanticAddress();
		assertTrue(after.isParameterAddress());
		assertTrue(after.getCanonicalParameter() < -2.8, "wrapped to "
				+ after.getCanonicalParameter());
		assertEquals(1, after.getPeriodicLift());
		assertEquals(Math.cos(3.4), point.getInhomX(), 1e-3);
		assertEquals(identity, id(point));
	}

	@Test
	void saveAndReopenReadTheAddressesBackAndRecomputeTheEvidence() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		add("a=0.25");
		add("A=Point(L,a)");
		add("B=Point(L,\"" + BRANCH + "\",0.5)");
		GeoPoint clicked = clicked(locus, "C", 0.75, Math.exp(0.75));
		locus.getConditionedEvidence(BRANCH);
		List<LocusSemanticAddress2D> addresses = new ArrayList<>();
		List<String> texts = new ArrayList<>();
		List<PersistentGeoId> identities = new ArrayList<>();
		for (String label : List.of("A", "B", "C")) {
			GeoPoint point = (GeoPoint) lookup(label);
			addresses.add(parent(point).getCurrentSemanticAddress());
			texts.add(parent(point).getBranchInput().getTextString());
			identities.add(id(point));
		}
		assertNotNull(clicked);
		String document = getApp().getXML();
		// Evidence is never stored: no cell, node, region or content identity.
		assertFalse(document.contains("conditioned-evidence"));
		assertFalse(document.contains("conditioned-traversal"));
		assertTrue(document.contains(LocusParameterAddressState2D.KIND));

		AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		reopened.setXML(document, true);
		SpatialIdentityRegistry registry = reopened.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		GeoLocusV2 loaded = (GeoLocusV2) reopened.getKernel().lookupLabel("L");
		assertNull(loaded.getPublishedConditionedEvidence(BRANCH));
		int index = 0;
		for (String label : List.of("A", "B", "C")) {
			GeoPoint point = (GeoPoint) reopened.getKernel().lookupLabel(label);
			assertTrue(point.isDefined(), label);
			LocusSemanticAddress2D address = parent(point).getCurrentSemanticAddress();
			assertTrue(address.isParameterAddress(), label);
			assertEquals(addresses.get(index).getCanonicalParameter(),
					address.getCanonicalParameter(), 0, label);
			assertEquals(addresses.get(index).getBranchKey(), address.getBranchKey());
			assertEquals(texts.get(index), parent(point).getBranchInput().getTextString());
			assertEquals(identities.get(index), registry.getPersistentGeoId(point), label);
			index++;
		}
		assertNotNull(loaded.getConditionedEvidence(BRANCH));
		assertEquals(document, reopened.getXML());
	}

	@Test
	void undoAndRedoRestoreTheAddressAndTheDefinednessOfEachStep() {
		GeoLocusV2 locus = outsideClass("sqrt(x(P)-0.3)+exp(x(P))");
		getApp().setUndoActive(true);
		GeoPoint point = clicked(locus, "A", 0.6, Math.sqrt(0.3) + Math.exp(0.6));
		getApp().storeUndoInfo();
		LocusSemanticAddress2D initial = parent(point).getCurrentSemanticAddress();
		PersistentGeoId identity = id(point);
		LocusPointContinuation2D.Context gesture =
				LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		assertEquals(LocusPointContinuation2D.Status.CONTINUATION_ESTABLISHED,
				LocusV2PublicOperations.followInteractiveSemanticPointToParameter(point, 0.9,
						policy(0.05), gesture).getStatus());
		LocusSemanticAddress2D moved = parent(point).getCurrentSemanticAddress();
		assertNotEquals(initial, moved);
		getApp().storeUndoInfo();

		getKernel().undo();
		point = assertInstanceOf(GeoPoint.class, lookup("A"));
		assertTrue(point.isDefined());
		assertEquals(initial.getCanonicalParameter(), parent(point)
				.getCurrentSemanticAddress().getCanonicalParameter(), 0);
		assertTrue(parent(point).getCurrentSemanticAddress().isParameterAddress());
		assertEquals(identity, id(point));
		getKernel().redo();
		point = assertInstanceOf(GeoPoint.class, lookup("A"));
		assertEquals(moved.getCanonicalParameter(), parent(point)
				.getCurrentSemanticAddress().getCanonicalParameter(), 0);
		assertEquals(identity, id(point));
	}

	@Test
	void aCopyHasANewIdentityAndItsOwnSource() {
		GeoLocusV2 locus = outsideClass("exp(x(P))");
		GeoPoint point = clicked(locus, "A", 0.5, Math.exp(0.5));
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		PersistentGeoId original = registry.getPersistentGeoId(point);
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(point), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		GeoIdentityRecord copied = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> original.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copy = assertInstanceOf(GeoPoint.class, registry.getGeo(copied.getId()));
		assertTrue(copy.isDefined());
		assertNotEquals(original, registry.getPersistentGeoId(copy));
		AlgoSemanticLocusPoint2D copiedParent = parent(copy);
		LocusSemanticAddress2D address = copiedParent.getCurrentSemanticAddress();
		assertTrue(address.isParameterAddress());
		assertNotEquals(locus.getPersistentLocusId(),
				copiedParent.getSource().getPersistentLocusId());
		assertEquals(copiedParent.getSource().getPersistentLocusId(),
				address.getSourceLocusId());
		assertEquals(parent(point).getCurrentSemanticAddress().getCanonicalParameter(),
				address.getCanonicalParameter(), 0);
	}
}

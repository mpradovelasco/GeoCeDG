/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T2: existence structure v2 ({@code locus-v2-existence-components.md}
 * section 15) on structures built in the test: arcs on the circle with one-sided ends
 * (V-ARC-2), exploratory evidence as a sibling without authority (V-LVL-2), point components
 * (V-POINT-1), typed causes, nature of evidence, the single closure query, and the
 * compatibility certificate expressed in the new model without any change of its legacy
 * views.
 */
class PreG9BCu2ExistenceStructureV2Test {
	private static final String BRANCH = "generator.main";
	private static final LocusInterval2D CIRCLE = new LocusInterval2D(-Math.PI, Math.PI,
			true, false);

	/** V-ARC-2: an arc that crosses the seam contains the seam and one coordinate each. */
	@Test
	void anArcCrossingTheSeamIsOneComponentWithUniqueCoordinates() {
		double start = 2.4;
		double end = -2.0;
		ContinuousComponent arc = ContinuousComponent.arc(CIRCLE, start, end, false,
				transition("start", true), transition("end", true),
				Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "arc/0");
		LocusExistenceStructure2D structure = periodic(List.of(
				exists(new LocusInterval2D(-Math.PI, end, true, true)),
				absent(new LocusInterval2D(end, start, false, false)),
				exists(new LocusInterval2D(start, Math.PI, true, false))),
				List.of(arc), Collections.emptyList(), Completeness.COMPLETE);

		assertTrue(arc.isArc());
		assertTrue(arc.crossesSeam());
		assertFalse(arc.isClosedPeriodic());
		assertEquals(1, structure.getContinuousValidComponents().size());
		assertTrue(arc.contains(-Math.PI));
		assertEquals(Math.PI - start, arc.getArcCoordinate(-Math.PI), 1e-15);
		assertEquals(0, arc.getArcCoordinate(start));
		assertEquals(arc.getLength(), arc.getArcCoordinate(end));
		assertEquals(end - start + 2 * Math.PI, arc.getLength());
		assertTrue(Double.isNaN(arc.getArcCoordinate(0)));
		assertTrue(Double.isNaN(arc.getArcCoordinate(Math.PI)));
		assertSame(arc, structure.getComponentContaining(3.0));
		assertSame(arc, structure.getComponentContaining(-3.0));
		assertNull(structure.getComponentContaining(0));
		assertEquals(new LocusInterval2D(start, end + 2 * Math.PI, true, true),
				arc.getInterval());
		assertFalse(structure.isClosedPeriodic());
	}

	/**
	 * A full-length open arc has two distinct ends at the same canonical parameter: only the
	 * included end is the image of that parameter, and the curve is not closed.
	 */
	@Test
	void aFullLengthOpenArcKeepsItsTwoEndsDistinct() {
		double seam = -1.0;
		LocusBoundary2D startEnd = new LocusBoundary2D(
				LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("metric-w_1", "origin-wrap", "after"), true,
				LocusEvidenceNature2D.EXACT);
		LocusBoundary2D finalEnd = new LocusBoundary2D(
				LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("metric-w_1", "origin-wrap", "before"), false,
				LocusEvidenceNature2D.EXACT);
		ContinuousComponent arc = ContinuousComponent.arc(CIRCLE, seam, seam, true,
				startEnd, finalEnd, Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
				"e2/0");
		LocusExistenceStructure2D structure = periodic(List.of(exists(CIRCLE)),
				List.of(arc), Collections.emptyList(), Completeness.COMPLETE);

		assertTrue(arc.isFullLength());
		assertFalse(arc.isClosedPeriodic());
		assertFalse(structure.isClosedPeriodic());
		assertEquals(0, arc.getArcCoordinate(seam));
		assertEquals(2 * Math.PI, arc.getLength());
		assertNotEquals(arc.getLowerBoundary().getSource(), arc.getUpperBoundary().getSource());
		assertTrue(arc.contains(-Math.PI));
		assertThrows(IllegalArgumentException.class,
				() -> ContinuousComponent.arc(CIRCLE, seam, seam, true, startEnd,
						new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
								finalEnd.getSource(), true, LocusEvidenceNature2D.EXACT),
						Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "e2/1"));
	}

	/** The certificate's closed-periodic flag is the single closure authority. */
	@Test
	void onlyACertifiedClosedPeriodicArcClosesTheBranch() {
		LocusBoundary2D closure = new LocusBoundary2D(LocusBoundary2D.Kind.PERIODIC_CLOSURE,
				new LocusBoundarySource2D("certificate", "periodic-closure", "seam"), true,
				LocusEvidenceNature2D.EXACT);
		ContinuousComponent closed = ContinuousComponent.arc(CIRCLE, 0.5, 0.5, true,
				closure, closure, Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
				"closed/0");
		LocusExistenceStructure2D structure = periodic(List.of(exists(CIRCLE)),
				List.of(closed), Collections.emptyList(), Completeness.COMPLETE);
		assertTrue(structure.isClosedPeriodic());
		assertEquals(0, closed.getArcCoordinate(0.5));
		assertEquals(2 * Math.PI - 0.5 - Math.PI, closed.getArcCoordinate(-Math.PI), 1e-15);
		assertThrows(IllegalArgumentException.class,
				() -> ContinuousComponent.arc(CIRCLE, 0.5, 2.0, false, closure, closure,
						Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "partial"));
		assertThrows(IllegalArgumentException.class,
				() -> new LocusExistenceStructure2D(BRANCH, CIRCLE, DomainTopology.BOUNDED,
						0, List.of(exists(CIRCLE)), List.of(closed),
						Collections.emptyList(), Completeness.COMPLETE, "fixture/v1"));
	}

	/**
	 * Step T11 (D-CU2-7, option A): the explicit certificate of a static or analytic producer
	 * cites the contract that proves it in every fact; it closes a periodic domain only when
	 * that contract proves the closure, and otherwise is the open arc of full length. It
	 * replaces the retired provider-compatibility certificate.
	 */
	@Test
	void theProviderCertificateCitesItsContract() {
		String contract = "fixture-producer/v1";
		LocusExistenceStructure2D closed = LocusExistenceStructure2D.providerProved(BRANCH,
				CIRCLE, List.of(CIRCLE), true, true, contract);
		ContinuousComponent component = closed.getContinuousValidComponents().get(0);
		assertEquals(CIRCLE, component.getInterval());
		assertEquals("provider-component/0", component.getEvidenceKey());
		assertEquals(Maximality.ESTABLISHED, component.getMaximality());
		assertEquals(LocusExistenceStructure2D.PROVIDER_PROVED_CONTRACT,
				closed.getCertifierContract());
		assertEquals(Completeness.COMPLETE, closed.getCompleteness());
		assertEquals(LocusEvidenceNature2D.EXACT, component.getNature());
		assertTrue(closed.isClosedPeriodic());
		assertEquals("provider-contract",
				component.getLowerBoundary().getSource().getOrigin());
		assertEquals(contract, closed.getCoverage().get(0).getEvidence());
		assertEquals(LocusCoverageCause2D.PROVIDER_CONTRACT,
				closed.getCoverage().get(0).getCause());
		assertEquals(LocusEvidenceNature2D.EXACT, closed.getCoverage().get(0).getNature());

		LocusExistenceStructure2D open = LocusExistenceStructure2D.providerProved(BRANCH,
				CIRCLE, List.of(CIRCLE), true, false, contract);
		ContinuousComponent arc = open.getContinuousValidComponents().get(0);
		assertFalse(open.isClosedPeriodic(), "a periodic provider alone never closes");
		assertTrue(arc.isArc());
		assertEquals(LocusBoundary2D.Kind.DOMAIN_END, arc.getLowerBoundary().getKind());
		assertFalse(arc.getUpperBoundary().isBinaryEndIncluded());

		LocusInterval2D left = new LocusInterval2D(-2, -1, true, true);
		LocusInterval2D right = new LocusInterval2D(1, 2, true, false);
		LocusInterval2D bounded = new LocusInterval2D(-3, 3, true, true);
		LocusExistenceStructure2D boundedCertificate = LocusExistenceStructure2D
				.providerProved(BRANCH, bounded, List.of(right, left), false, false,
						contract);
		assertEquals(List.of(left, right), boundedCertificate
				.getContinuousValidComponents().stream()
				.map(ContinuousComponent::getInterval).toList());
		assertEquals(DomainTopology.BOUNDED, boundedCertificate.getTopology());
		assertFalse(boundedCertificate.isClosedPeriodic());
		assertEquals(List.of(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				CoverageState.CERTIFIED_EXISTS, CoverageState.CERTIFIED_DOES_NOT_EXIST,
				CoverageState.CERTIFIED_EXISTS, CoverageState.CERTIFIED_DOES_NOT_EXIST),
				boundedCertificate.getCoverage().stream().map(Coverage::getState)
						.toList());
		assertTrue(boundedCertificate.getCoverage().stream()
				.allMatch(item -> contract.equals(item.getEvidence())));

		LocusInterval2D upperSeam = new LocusInterval2D(0, 1, false, true);
		LocusExistenceStructure2D upperSeamCertificate = LocusExistenceStructure2D
				.providerProved(BRANCH, upperSeam, List.of(upperSeam), true, true,
						contract);
		assertTrue(upperSeamCertificate.isClosedPeriodic());
		assertEquals(upperSeam, upperSeamCertificate.getContinuousValidComponents()
				.get(0).getInterval());
		assertFalse(LocusExistenceStructure2D.providerProved(BRANCH, CIRCLE,
				List.of(new LocusInterval2D(-Math.PI, 0, true, false),
						new LocusInterval2D(0.5, 1, true, true)), true, true, contract)
				.isClosedPeriodic());
	}

	/**
	 * Step T11: restricted to declared components that share an excluded end, as the scalar
	 * domain [-2, 0) and (0, 2], the certificate keeps one component in each and certifies
	 * the excluded point [0, 0] as nonexistence by the declaration, ordered before (0, 2].
	 */
	@Test
	void declaredComponentsSharingAnExcludedEndAreRestrictedInOrder() {
		LocusInterval2D domain = new LocusInterval2D(-2, 2, true, true);
		LocusInterval2D left = new LocusInterval2D(-2, 0, true, false);
		LocusInterval2D right = new LocusInterval2D(0, 2, false, true);
		LocusExistenceStructure2D restricted = LocusExistenceStructure2D.providerProved(
				BRANCH, domain, List.of(domain), false, false, "fixture-producer/v1")
				.restrictedTo(List.of(right, left), "fixture/declared-components");
		assertEquals(Completeness.COMPLETE, restricted.getCompleteness());
		assertEquals(List.of(left, right), restricted.getContinuousValidComponents().stream()
				.map(ContinuousComponent::getInterval).toList());
		assertEquals(List.of(left, new LocusInterval2D(0, 0, true, true), right),
				restricted.getCoverage().stream().map(Coverage::getInterval).toList());
		Coverage gap = restricted.getCoverage().get(1);
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, gap.getState());
		assertEquals(LocusCoverageCause2D.PROVIDER_COMPLEMENT, gap.getCause());
		assertEquals("fixture/declared-components", gap.getEvidence());
	}

	/**
	 * Author resolution before step T12 (point component of {@code restrictedTo}): certified
	 * existence that a declared isolated parameter keeps is a point component, with
	 * isolated-point coverage at that parameter only, the maximality and nature of its
	 * component and a lineage key of its own; never a zero-length interval and never dropped.
	 */
	@Test
	void aDeclaredIsolatedParameterOfCertifiedExistenceIsAPointComponent() {
		LocusInterval2D domain = new LocusInterval2D(-2, 2, true, true);
		LocusInterval2D left = new LocusInterval2D(-2, -1, true, true);
		LocusInterval2D zero = new LocusInterval2D(0, 0, true, true);
		LocusInterval2D right = new LocusInterval2D(1, 2, true, true);
		LocusExistenceStructure2D restricted = LocusExistenceStructure2D.providerProved(
				BRANCH, domain, List.of(domain), false, false, "fixture-producer/v1")
				.restrictedTo(List.of(left, zero, right), "fixture/declared-components");
		assertEquals(Completeness.COMPLETE, restricted.getCompleteness());
		assertEquals(List.of(left, right), restricted.getContinuousValidComponents().stream()
				.map(ContinuousComponent::getInterval).toList());
		assertEquals(1, restricted.getPointComponents().size());
		LocusPointComponent2D point = restricted.getPointComponents().get(0);
		assertEquals(0, point.getParameterLower(), 0);
		assertEquals(0, point.getParameterUpper(), 0);
		assertEquals(Maximality.ESTABLISHED, point.getMaximality());
		assertTrue(point.getNature().certifies());
		Coverage at = restricted.getCoverageAt(0);
		assertEquals(zero, at.getInterval());
		assertEquals(CoverageState.CERTIFIED_ISOLATED_POINTS, at.getState());
		assertEquals(LocusCoverageCause2D.PROVIDER_ISOLATED_POINT, at.getCause());
		for (double neighbour : new double[] {-0.5, Math.nextDown(0.0), Double.MIN_VALUE,
				0.5}) {
			assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
					restricted.getCoverageAt(neighbour).getState(), "at " + neighbour);
		}
		assertNull(restricted.getComponentContaining(0));
		assertEquals(BRANCH + "/point/" + point.getSource().getSignature(),
				LocusComponentLineage2D.of(restricted, point));
	}

	/**
	 * Adversarial cases of the same correction: a declared isolated parameter where
	 * existence is certified false or unresolved yields no point component and keeps the
	 * fact of the source; two declared isolated parameters yield two points with distinct
	 * lineage keys; a declared component that only touches a certified component at its
	 * closed end keeps that end as a point; and the point's disappearance is a typed lineage
	 * transition.
	 */
	@Test
	void declaredIsolatedParametersNeverInventExistence() {
		LocusInterval2D domain = new LocusInterval2D(-2, 2, true, true);
		LocusInterval2D zero = new LocusInterval2D(0, 0, true, true);
		LocusExistenceStructure2D absent = LocusExistenceStructure2D.providerProved(BRANCH,
				domain, List.of(new LocusInterval2D(-2, -1, true, true)), false, false,
				"fixture-producer/v1").restrictedTo(List.of(zero), "fixture/declared");
		assertTrue(absent.getPointComponents().isEmpty());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				absent.getCoverageAt(0).getState());

		LocusExistenceStructure2D unresolved = LocusExistenceStructure2D.notEstablished(BRANCH,
				domain, DomainTopology.BOUNDED, LocusCoverageCause2D.BUDGET_EXHAUSTED,
				"fixture/unresolved").restrictedTo(List.of(zero), "fixture/declared");
		assertTrue(unresolved.getPointComponents().isEmpty());
		assertEquals(CoverageState.UNRESOLVED, unresolved.getCoverageAt(0).getState());
		assertEquals(Completeness.NOT_ESTABLISHED, unresolved.getCompleteness());

		LocusExistenceStructure2D whole = LocusExistenceStructure2D.providerProved(BRANCH,
				domain, List.of(domain), false, false, "fixture-producer/v1");
		LocusExistenceStructure2D pair = whole.restrictedTo(List.of(
				new LocusInterval2D(-1, -1, true, true), new LocusInterval2D(1, 1, true, true)),
				"fixture/declared");
		assertEquals(2, pair.getPointComponents().size());
		List<String> keys = LocusComponentLineage2D.keys(pair);
		assertEquals(2, keys.size());
		assertNotEquals(keys.get(0), keys.get(1));

		LocusExistenceStructure2D touching = LocusExistenceStructure2D.providerProved(BRANCH,
				domain, List.of(new LocusInterval2D(-2, 0, true, true)), false, false,
				"fixture-producer/v1").restrictedTo(
						List.of(new LocusInterval2D(0, 1, true, true)), "fixture/declared");
		assertTrue(touching.getContinuousValidComponents().isEmpty());
		assertEquals(1, touching.getPointComponents().size());
		assertEquals(CoverageState.CERTIFIED_ISOLATED_POINTS,
				touching.getCoverageAt(0).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				touching.getCoverageAt(0.5).getState());
		assertEquals(Completeness.COMPLETE, touching.getCompleteness());

		LocusExistenceStructure2D single = whole.restrictedTo(List.of(zero),
				"fixture/declared");
		List<LocusLineage2D> lineage = LocusComponentLineage2D.transitions(single, absent,
				true);
		assertEquals(1, lineage.size());
		assertEquals(LineageTransition.DISAPPEARED, lineage.get(0).getTransition());
		assertEquals(LocusComponentLineage2D.keys(single), lineage.get(0).getParentKeys());
	}

	/** V-POINT-1: a point component is an entity of its own, never an interval. */
	@Test
	void anIsolatedParameterIsAPointComponentAndNeverAZeroLengthInterval() {
		double root = Math.atan2(-4, -3);
		LocusPointComponent2D point = new LocusPointComponent2D(Math.nextDown(root),
				Math.nextUp(root), new LocusBoundarySource2D("line-circle", "discriminant",
						"even-root-1"), Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
				"external-tangency/0", new double[] {14.999999, 15.000001, 19.999999,
						20.000001});
		LocusExistenceStructure2D structure = periodic(List.of(
				absent(new LocusInterval2D(-Math.PI, Math.nextDown(root), true, false)),
				isolated(new LocusInterval2D(Math.nextDown(root), Math.nextUp(root), true,
						true)),
				absent(new LocusInterval2D(Math.nextUp(root), Math.PI, false, false))),
				Collections.emptyList(), List.of(point), Completeness.COMPLETE);

		assertEquals(List.of(point), structure.getPointComponents());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertNull(structure.getComponentContaining(root));
		assertEquals(0, point.getLength());
		assertTrue(point.getParameterLower() < root && root < point.getParameterUpper());
		assertEquals(LocusPointComponent2D.PositionState.CERTIFIED_ENCLOSURE,
				point.getPositionState());
		assertFalse(structure.isClosedPeriodic());
		assertTrue(structure.getSemanticSignature().contains("|point="));
		LocusBranch2D branch = new LocusBranch2D(BRANCH, CIRCLE, structure,
				Orientation.INCREASING, "fixture/v1", LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE, BranchProperty.PERIODIC),
				LocusQuality2D.analyticDoubleSemantic());
		assertTrue(branch.getValidDomainComponents().isEmpty());
		assertTrue(branch.getCertifiedContinuousValidComponents().isEmpty());
		assertThrows(IllegalArgumentException.class,
				() -> new LocusPointComponent2D(root, root, point.getSource(),
						Maximality.ESTABLISHED, LocusEvidenceNature2D.EXPLORATORY, "x",
						null));
		assertThrows(IllegalArgumentException.class, () -> periodic(List.of(
				absent(CIRCLE)), Collections.emptyList(), List.of(point),
				Completeness.COMPLETE));
	}

	/** A root at the seam is enclosed one ulp below the canonical start. */
	@Test
	void aPointComponentAtTheSeamIsEnclosedBelowTheCanonicalStart() {
		LocusPointComponent2D seam = new LocusPointComponent2D(Math.nextDown(-Math.PI),
				-Math.PI, new LocusBoundarySource2D("line-circle", "discriminant",
						"even-root-seam"), Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
				"seam/0", null);
		LocusExistenceStructure2D structure = periodic(List.of(
				isolated(new LocusInterval2D(-Math.PI, -Math.PI, true, true)),
				absent(new LocusInterval2D(-Math.PI, Math.PI, false, false))),
				Collections.emptyList(), List.of(seam), Completeness.COMPLETE);
		assertEquals(LocusPointComponent2D.PositionState.UNAVAILABLE, seam.getPositionState());
		assertEquals(Completeness.COMPLETE, structure.getCompleteness());
		assertThrows(IllegalStateException.class, seam::getPositionEnclosure);
	}

	/**
	 * V-POINT-3 (structure part, decision D-CU2-T13-R1): a point component and a component of
	 * dimension one change dimension into each other only through the operation and predicate
	 * of their sources, when exactly one component that changes on each side names them and
	 * the selection rule is unchanged; otherwise the change fails closed as disappearance and
	 * appearance. Neither the relation nor the keys read the binary64 ends.
	 */
	@Test
	void aChangeOfDimensionRelatesOnlyThroughUniqueStructuralSources() {
		double root = Math.atan2(-4, -3);
		LocusExistenceStructure2D tangency = points(point(root, "root-1"));
		LocusExistenceStructure2D bite = arcs("discriminant", root - 0.01, root + 0.01);
		String pointKey = LocusComponentLineage2D.keys(tangency).get(0);
		String arcKey = LocusComponentLineage2D.keys(bite).get(0);
		assertFalse(pointKey.contains("0x"), pointKey);
		assertFalse(arcKey.contains("0x"), arcKey);
		assertEquals(List.of(new LocusLineage2D(LineageTransition.DIMENSION_CHANGED,
				List.of(pointKey), List.of(arcKey))),
				LocusComponentLineage2D.transitions(tangency, bite, true));
		assertEquals(List.of(new LocusLineage2D(LineageTransition.DIMENSION_CHANGED,
				List.of(arcKey), List.of(pointKey))),
				LocusComponentLineage2D.transitions(bite, tangency, true));
		LocusExistenceStructure2D moved = arcs("discriminant", 1.0, 2.0);
		assertEquals(List.of(arcKey), LocusComponentLineage2D.keys(moved));
		assertEquals(LocusComponentLineage2D.transitions(tangency, bite, true),
				LocusComponentLineage2D.transitions(tangency, moved, true));

		// A change of the selection rule relates nothing.
		assertEquals(unrelated(List.of(pointKey), List.of(arcKey)),
				LocusComponentLineage2D.transitions(tangency, bite, false));
		// Another predicate of the same operation is no structural relation.
		LocusExistenceStructure2D radius = arcs("radius", root - 0.01, root + 0.01);
		assertEquals(unrelated(List.of(pointKey), LocusComponentLineage2D.keys(radius)),
				LocusComponentLineage2D.transitions(tangency, radius, true));
		// Two points of that operation and predicate: no unique relation.
		LocusExistenceStructure2D pair = points(point(root, "root-1"), point(0.5, "root-3"));
		assertEquals(unrelated(LocusComponentLineage2D.keys(pair), List.of(arcKey)),
				LocusComponentLineage2D.transitions(pair, bite, true));
		// Two components that name them: no unique relation, and no structural key either.
		LocusExistenceStructure2D twice = arcs("discriminant", root - 0.01, root + 0.01, 1.0,
				2.0);
		List<String> twiceKeys = LocusComponentLineage2D.keys(twice);
		assertTrue(twiceKeys.stream().allMatch(key -> key.contains("0x")), twiceKeys.toString());
		assertEquals(unrelated(List.of(pointKey), twiceKeys),
				LocusComponentLineage2D.transitions(tangency, twice, true));
		assertEquals(unrelated(twiceKeys, List.of(pointKey)),
				LocusComponentLineage2D.transitions(twice, tangency, true));
		assertThrows(IllegalArgumentException.class, () -> new LocusLineage2D(
				LineageTransition.DIMENSION_CHANGED, List.of(pointKey), List.of()));
		assertThrows(IllegalArgumentException.class, () -> new LocusLineage2D(
				LineageTransition.DIMENSION_CHANGED, List.of(pointKey, arcKey),
				List.of("other")));
	}

	/** V-LVL-2: exploratory evidence is a sibling without authority over the snapshot. */
	@Test
	void exploratoryEvidenceNeverCertifiesAndIsOutsideTheSnapshot() {
		LocusExistenceStructure2D unresolved = LocusExistenceStructure2D.notEstablished(
				BRANCH, CIRCLE, DomainTopology.PERIODIC,
				LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS, "conic-conic-outside-class");
		LocusExploratoryEvidence2D first = new LocusExploratoryEvidence2D(BRANCH, 3,
				"uniform-samples-and-bisection", "exploration/v1", 256,
				List.of(new LocusInterval2D(-1, 1, true, true)),
				List.of(new LocusInterval2D(1, 1.01, true, true)), Collections.emptyList());
		LocusExploratoryEvidence2D second = new LocusExploratoryEvidence2D(BRANCH, 3,
				"uniform-samples-and-bisection", "exploration/v2", 512,
				List.of(new LocusInterval2D(-1.2, 1, true, true)), Collections.emptyList(),
				Collections.emptyList());

		assertFalse(first.getNature().certifies());
		assertNotEquals(first.getPolicyVersion(), second.getPolicyVersion());
		assertEquals(Completeness.NOT_ESTABLISHED, unresolved.getCompleteness());
		assertTrue(unresolved.getContinuousValidComponents().isEmpty());
		assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
				unresolved.getCoverage().get(0).getCause());
		assertEquals(unresolved.getSemanticSignature(), LocusExistenceStructure2D
				.notEstablished(BRANCH, CIRCLE, DomainTopology.PERIODIC,
						LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
						"conic-conic-outside-class").getSemanticSignature());
	}

	/** Typed causes and the nature of evidence are validated, never inferred. */
	@Test
	void typedCausesAndNaturesAreValidated() {
		assertTrue(LocusEvidenceNature2D.EXACT.certifies());
		assertTrue(LocusEvidenceNature2D.RIGOROUS_INTERVAL.certifies());
		assertFalse(LocusEvidenceNature2D.NUMERICAL_UNDER_HYPOTHESES.certifies());
		assertFalse(LocusEvidenceNature2D.EXPLORATORY.certifies());
		assertFalse(LocusEvidenceNature2D.NONE.certifies());
		assertThrows(IllegalArgumentException.class,
				() -> new Coverage(CIRCLE, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.COMPUTATIONAL_DEPENDENCY_FAILURE,
						LocusEvidenceNature2D.NONE, "metric-failure-is-not-nonexistence"));
		assertThrows(IllegalArgumentException.class,
				() -> periodic(List.of(new Coverage(CIRCLE, CoverageState.UNRESOLVED,
						LocusCoverageCause2D.BUDGET_EXHAUSTED, LocusEvidenceNature2D.NONE,
						"budget")), Collections.emptyList(), Collections.emptyList(),
						Completeness.COMPLETE));
		LocusExistenceStructure2D empty = periodic(List.of(absent(CIRCLE)),
				Collections.emptyList(), Collections.emptyList(), Completeness.COMPLETE);
		assertEquals(Completeness.COMPLETE, empty.getCompleteness());
		assertTrue(empty.getContinuousValidComponents().isEmpty());
		assertTrue(empty.getPointComponents().isEmpty());
	}

	private static LocusExistenceStructure2D periodic(List<Coverage> coverage,
			List<ContinuousComponent> components, List<LocusPointComponent2D> points,
			Completeness completeness) {
		return new LocusExistenceStructure2D(BRANCH, CIRCLE, DomainTopology.PERIODIC, 0,
				coverage, components, points, completeness, "fixture-hybrid/v1");
	}

	private static Coverage exists(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_EXISTS,
				LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE, LocusEvidenceNature2D.EXACT,
				"fixture-exists");
	}

	private static Coverage absent(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_DOES_NOT_EXIST,
				LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE, LocusEvidenceNature2D.EXACT,
				"fixture-absent");
	}

	private static Coverage isolated(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_ISOLATED_POINTS,
				LocusCoverageCause2D.ISOLATED_EVEN_ROOT, LocusEvidenceNature2D.EXACT,
				"fixture-isolated");
	}

	/** @return the disappearance of every parent and the appearance of every child */
	private static List<LocusLineage2D> unrelated(List<String> parents, List<String> children) {
		return List.of(new LocusLineage2D(LineageTransition.DISAPPEARED, parents, List.of()),
				new LocusLineage2D(LineageTransition.APPEARED, List.of(), children));
	}

	/** An isolated root of the discriminant of one operation at the given address. */
	private static LocusPointComponent2D point(double root, String address) {
		return new LocusPointComponent2D(Math.nextDown(root), Math.nextUp(root),
				new LocusBoundarySource2D("intersection-J", "discriminant", address),
				Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "point/" + address, null);
	}

	/** A complete structure of point components in increasing order. */
	private static LocusExistenceStructure2D points(LocusPointComponent2D... points) {
		List<Coverage> coverage = new ArrayList<>();
		double start = -Math.PI;
		boolean closed = true;
		for (LocusPointComponent2D point : points) {
			coverage.add(absent(new LocusInterval2D(start, point.getParameterLower(), closed,
					false)));
			coverage.add(isolated(new LocusInterval2D(point.getParameterLower(),
					point.getParameterUpper(), true, true)));
			start = point.getParameterUpper();
			closed = false;
		}
		coverage.add(absent(new LocusInterval2D(start, Math.PI, closed, false)));
		return periodic(coverage, Collections.emptyList(), List.of(points),
				Completeness.COMPLETE);
	}

	/**
	 * A complete structure of components bounded by certified transitions of one predicate of
	 * one operation, given by increasing pairs of ends.
	 */
	private static LocusExistenceStructure2D arcs(String predicate, double... ends) {
		List<Coverage> coverage = new ArrayList<>();
		List<ContinuousComponent> components = new ArrayList<>();
		double start = -Math.PI;
		boolean closed = true;
		for (int index = 0; index < ends.length; index += 2) {
			coverage.add(absent(new LocusInterval2D(start, ends[index], closed, false)));
			coverage.add(exists(new LocusInterval2D(ends[index], ends[index + 1], true, true)));
			components.add(ContinuousComponent.arc(CIRCLE, ends[index], ends[index + 1], false,
					end(predicate, "lower"), end(predicate, "upper"), Maximality.ESTABLISHED,
					LocusEvidenceNature2D.EXACT, "arc/" + index));
			start = ends[index + 1];
			closed = false;
		}
		coverage.add(absent(new LocusInterval2D(start, Math.PI, closed, false)));
		return periodic(coverage, components, Collections.emptyList(), Completeness.COMPLETE);
	}

	private static LocusBoundary2D end(String predicate, String side) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.CERTIFIED_TRANSITION,
				new LocusBoundarySource2D("intersection-J", predicate, side), true,
				LocusEvidenceNature2D.EXACT);
	}

	private static LocusBoundary2D transition(String side, boolean included) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.CERTIFIED_TRANSITION,
				new LocusBoundarySource2D("intersection-J", "discriminant", side), included,
				LocusEvidenceNature2D.EXACT);
	}
}

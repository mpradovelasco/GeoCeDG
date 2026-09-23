/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResult2D.Status;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R2 side-selection rules applied to real kernel-produced pair evidence.
 *
 * <p>No evidence is fabricated: every root comes from a published Locus V2 pair
 * result. The rules under test are metrics sections 23.2 and 23.3.</p>
 */
class PreG9bR2IntersectionEndpointSideSelectionTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void aSelfPairRootWithBothSidesOnTheAddressedSourceIsAmbiguous() {
		selfCrossingSpline();
		GeoLocusIntersectionResult self = add("RK=Intersect(K,K)");
		// No S x S root is point-admissible today, so no self-pair endpoint can be
		// materialized; that is R2-E0/R2-E1 territory. The ambiguity rule is the
		// fail-closed authority, exercised here with real per-side evidence.
		assertTrue(self.getIntersectionResult() == null
				|| self.getIntersectionResult().getFiniteSolutions().stream().noneMatch(
						root -> self.isPointAdmissible(root.getIdentity().getRootToken())));

		GeoLocusV2 spline = straightSpline();
		verticalSpline("T", -0.5);
		verticalSpline("U", 0.5);
		GeoLocusIntersectionResult left = add("R=Intersect(S,T)");
		GeoLocusIntersectionResult right = add("V=Intersect(S,U)");
		LocusIntersectionSolution2D leftRoot = admissibleRoot(left);
		LocusIntersectionSolution2D rightRoot = admissibleRoot(right);
		LocusPairIntersectionEvidence2D real = leftRoot.getPairEvidence().orElseThrow();
		// Two genuine S-side evidences at distinct parameters model one transverse
		// S x S root: both sides carry S's identity.
		LocusPairIntersectionEvidence2D selfPair = new LocusPairIntersectionEvidence2D(
				sideOn(leftRoot, spline), sideOn(rightRoot, spline),
				real.getLocalIsolation(), real.getResidual(), OptionalDouble.empty(),
				real.getSolverMethod(), real.getNumericGuarantee());
		for (LocusPairIntersectionEvidence2D view : List.of(selfPair,
				selfPair.reversed())) {
			IntersectionEndpointProvenanceResult2D result = resolve(view, spline, left,
					leftRoot);
			assertEquals(Status.MULTIPLE, result.getStatus(), result.getDiagnostic());
			assertEquals(2, result.getMatches().size());
			assertNotEquals(result.getMatches().get(0).getOccurrenceKey(),
					result.getMatches().get(1).getOccurrenceKey());
			assertNotEquals(
					Double.doubleToLongBits(result.getMatches().get(0).getAddress()
							.getCanonicalParameter()),
					Double.doubleToLongBits(result.getMatches().get(1).getAddress()
							.getCanonicalParameter()));
		}
	}

	@Test
	void exactlyOneMatchingSideIsUniqueAndAnUnrelatedSourceIsNoAddress() {
		GeoLocusV2 first = straightSpline();
		GeoLocusV2 second = verticalSpline("T", 0.5);
		GeoLocusV2 unrelated = verticalSpline("U", -0.5);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		LocusPairIntersectionEvidence2D evidence = root.getPairEvidence().orElseThrow();
		for (LocusPairIntersectionEvidence2D view : List.of(evidence,
				evidence.reversed())) {
			IntersectionEndpointProvenanceResult2D onFirst = resolve(view, first, rich,
					root);
			IntersectionEndpointProvenanceResult2D onSecond = resolve(view, second,
					rich, root);
			assertEquals(Status.UNIQUE, onFirst.getStatus(), onFirst.getDiagnostic());
			assertEquals(Status.UNIQUE, onSecond.getStatus(), onSecond.getDiagnostic());
			assertEquals(0.75, onFirst.getUniqueMatch().getAddress()
					.getCanonicalParameter(), 1E-9);
			assertEquals(Status.NO_ADDRESS,
					resolve(view, unrelated, rich, root).getStatus());
		}
	}

	@Test
	void aStaleSideIsFinalWhileTheUnchangedSideStaysCurrent() {
		GeoLocusV2 first = straightSpline();
		GeoLocusV2 second = verticalSpline("T", 0.5);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		LocusPairIntersectionEvidence2D captured = root.getPairEvidence().orElseThrow();
		long firstBefore = first.getSemanticDefinition().getSemanticRevision();
		long secondBefore = second.getSemanticDefinition().getSemanticRevision();
		GeoPoint control = (GeoPoint) getKernel().lookupLabel("A");
		control.setCoords(-1, 0.1, 1);
		control.updateCascade();
		assertNotEquals(firstBefore, first.getSemanticDefinition().getSemanticRevision());
		assertEquals(secondBefore, second.getSemanticDefinition().getSemanticRevision());

		IntersectionEndpointProvenanceResult2D stale = resolve(captured, first, rich,
				root);
		assertEquals(Status.UNRESOLVED_INVALID, stale.getStatus());
		assertTrue(stale.getDiagnostic().contains("stale"), stale.getDiagnostic());
		assertEquals(Status.UNIQUE, resolve(captured, second, rich, root).getStatus());
	}

	@Test
	void aDuplicatedTokenIsNeverPointAdmissibleSoNoEndpointCanCarryIt() {
		straightSpline();
		verticalSpline("T", 0.5);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		String token = root.getIdentity().getRootToken();
		LocusIntersectionResult2D current = rich.getIntersectionResult();
		assertTrue(current.findPointAdmissibleSolution(token).isPresent());
		// A hypothetical result publishing the same real solution twice, so its token
		// is no longer unique. Only the solution list and its matching root count
		// differ from the current result; this pins a structural invariant and is not
		// acceptance evidence.
		IntersectionCompletenessEvidence2D completeness = current.getCompletenessEvidence();
		LocusIntersectionResult2D duplicated = new LocusIntersectionResult2D(
				current.getSourceBinding(), current.getComputationStatus(),
				new IntersectionCompletenessEvidence2D(completeness.getCompleteness(),
						completeness.getMethod(), 2, completeness.getCoveredComponentKeys(),
						completeness.getDiagnostics()),
				current.getGeometryKind(),
				current.getCurrentness(), current.getSupportLevel(),
				current.getNumericGuarantee(), List.of(root, root),
				current.getOverlapEvidence(), current.getWork(),
				current.getDiagnostics());
		assertTrue(duplicated.findPointAdmissibleSolution(token).isEmpty());
	}

	private IntersectionEndpointProvenanceResult2D resolve(
			LocusPairIntersectionEvidence2D evidence, GeoLocusV2 source,
			GeoLocusIntersectionResult rich, LocusIntersectionSolution2D root) {
		// Free points carry no durable identity; the key's point field only needs one.
		PersistentGeoId endpoint = id(rich);
		return IntersectionEndpointProvenanceResolver2D.resolveSides(evidence,
				source.getSemanticDefinition(),
				source.getSemanticDefinition().getSemanticRevision(),
				source.getPersistentLocusId(), id(rich),
				root.getIdentity().getRootToken(), endpoint);
	}

	private GeoLocusV2 selfCrossingSpline() {
		getKernel().setContinuous(false);
		add("KA=(-1,1)");
		add("KB=(1,-1)");
		add("KC=(1,1)");
		add("KD=(-1,-1)");
		return add("K=SplineV2({KA,KB,KC,KD},3)");
	}

	private GeoLocusV2 straightSpline() {
		getKernel().setContinuous(false);
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private GeoLocusV2 verticalSpline(String label, double x) {
		add(label + "E=(" + x + ",-2)");
		add(label + "F=(" + x + ",-2/3)");
		add(label + "G=(" + x + ",2/3)");
		add(label + "H=(" + x + ",2)");
		return add(label + "=SplineV2({" + label + "E," + label + "F," + label + "G,"
				+ label + "H},3)");
	}

	private static LocusPairSourceRevisionEvidence2D sideOn(
			LocusIntersectionSolution2D root, GeoLocusV2 source) {
		LocusPairIntersectionEvidence2D evidence = root.getPairEvidence().orElseThrow();
		String identity = source.getSemanticDefinition().getLocusIdentity();
		return identity.equals(evidence.getFirst().getLocusIdentity())
				? evidence.getFirst() : evidence.getSecond();
	}

	private static List<LocusIntersectionSolution2D> pairRoots(
			GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.filter(root -> root.getPairEvidence().isPresent()).toList();
	}

	private static LocusIntersectionSolution2D admissibleRoot(
			GeoLocusIntersectionResult rich) {
		List<LocusIntersectionSolution2D> admissible = pairRoots(rich).stream()
				.filter(root -> rich.isPointAdmissible(root.getIdentity().getRootToken()))
				.toList();
		assertEquals(1, admissible.size(), "expected one admissible pair root");
		return admissible.get(0);
	}

	private PersistentGeoId id(GeoElement geo) {
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id, geo.getLabelSimple());
		return id;
	}
}

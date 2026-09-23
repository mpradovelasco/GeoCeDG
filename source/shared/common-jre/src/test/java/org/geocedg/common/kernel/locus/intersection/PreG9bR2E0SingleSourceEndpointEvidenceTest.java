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
 * PRE-G9B-R2-E0 single-source currentness, component and uniqueness rules applied
 * to real kernel-produced root evidence (metrics section 24.4).
 *
 * <p>No evidence is fabricated: every root comes from a published single-source
 * Locus V2 intersection result, and a recombination changes exactly one field.</p>
 */
class PreG9bR2E0SingleSourceEndpointEvidenceTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void aCurrentRootIsUniqueOnItsOwnComponent() {
		GeoLocusV2 spline = straightSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,vl)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		IntersectionEndpointProvenanceResult2D result =
				resolve(root.getRevisionEvidence(), spline, rich, root);
		assertEquals(Status.UNIQUE, result.getStatus(), result.getDiagnostic());
		assertEquals(0.25, result.getUniqueMatch().getAddress().getCanonicalParameter(),
				1E-9);
		// The versioned key of metrics section 24.3.
		assertTrue(result.getUniqueMatch().getOccurrenceKey().startsWith(
				"metric-endpoint/single-source-intersection-occurrence/v1|addressed-source="));
	}

	@Test
	void aStaleRootIsFinalAndNeverFallsThrough() {
		GeoLocusV2 spline = straightSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,vl)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		IntersectionRootRevisionEvidence2D captured = root.getRevisionEvidence();
		long before = spline.getSemanticDefinition().getSemanticRevision();
		GeoPoint control = (GeoPoint) getKernel().lookupLabel("A");
		control.setCoords(-1, 0.1, 1);
		control.updateCascade();
		assertNotEquals(before, spline.getSemanticDefinition().getSemanticRevision());

		IntersectionEndpointProvenanceResult2D stale = resolve(captured, spline, rich,
				root);
		assertEquals(Status.UNRESOLVED_INVALID, stale.getStatus());
		assertTrue(stale.getDiagnostic().contains("stale"), stale.getDiagnostic());
		// The recomputed current root of the same slot resolves again.
		LocusIntersectionSolution2D current = admissibleRoot(rich);
		assertEquals(root.getIdentity().getRootToken(), current.getIdentity().getRootToken());
		assertEquals(Status.UNIQUE,
				resolve(current.getRevisionEvidence(), spline, rich, current).getStatus());
	}

	@Test
	void aDisagreeingComponentOrBranchIsUnresolved() {
		GeoLocusV2 spline = straightSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,vl)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		IntersectionRootRevisionEvidence2D real = root.getRevisionEvidence();
		String branch = real.getBranchSnapshotKey();
		IntersectionEndpointProvenanceResult2D otherComponent = resolve(
				recombined(real, branch, IntersectionCapabilityContext2D.componentKey(branch,
						1)), spline, rich, root);
		assertEquals(Status.UNRESOLVED_INVALID, otherComponent.getStatus());
		assertTrue(otherComponent.getDiagnostic().contains("component"),
				otherComponent.getDiagnostic());
		IntersectionEndpointProvenanceResult2D otherBranch = resolve(
				recombined(real, branch + "/other", real.getResolvedValidComponentKey()),
				spline, rich, root);
		assertEquals(Status.UNRESOLVED_INVALID, otherBranch.getStatus());
	}

	@Test
	void missingDurableIdentitiesAreUnresolved() {
		GeoLocusV2 spline = straightSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,vl)");
		LocusIntersectionSolution2D root = admissibleRoot(rich);
		IntersectionRootRevisionEvidence2D evidence = root.getRevisionEvidence();
		String token = root.getIdentity().getRootToken();
		PersistentGeoId richId = id(rich);
		PersistentGeoId sourceId = spline.getPersistentLocusId();
		long revision = spline.getSemanticDefinition().getSemanticRevision();
		for (IntersectionEndpointProvenanceResult2D result : List.of(
				IntersectionEndpointProvenanceResolver2D.resolveSingle(evidence,
						spline.getSemanticDefinition(), revision, sourceId, richId, token,
						null),
				IntersectionEndpointProvenanceResolver2D.resolveSingle(evidence,
						spline.getSemanticDefinition(), revision, sourceId, null, token,
						richId),
				IntersectionEndpointProvenanceResolver2D.resolveSingle(evidence,
						spline.getSemanticDefinition(), revision, null, richId, token,
						richId))) {
			assertEquals(Status.UNRESOLVED_INVALID, result.getStatus());
		}
	}

	@Test
	void aDuplicatedSingleSourceTokenIsNeverPointAdmissible() {
		straightSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,vl)");
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
			IntersectionRootRevisionEvidence2D evidence, GeoLocusV2 source,
			GeoLocusIntersectionResult rich, LocusIntersectionSolution2D root) {
		// Free points carry no durable identity; the key's point field only needs one.
		return IntersectionEndpointProvenanceResolver2D.resolveSingle(evidence,
				source.getSemanticDefinition(),
				source.getSemanticDefinition().getSemanticRevision(),
				source.getPersistentLocusId(), id(rich),
				root.getIdentity().getRootToken(), id(rich));
	}

	private static IntersectionRootRevisionEvidence2D recombined(
			IntersectionRootRevisionEvidence2D real, String branch, String component) {
		return new IntersectionRootRevisionEvidence2D(real.getLocusSemanticRevision(),
				real.getTargetUpdateStamp(), branch, component, real.getSemanticParameter(),
				real.getLiftedPeriodicParameter(), real.getIsolatingInterval(),
				real.getLocalIsolationStatus(), real.getResidualEvidence(),
				real.getSolverMethod(), real.getNumericGuarantee(),
				real.getCurrentRootGerm());
	}

	private GeoLocusV2 straightSpline() {
		getKernel().setContinuous(false);
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		add("vl:x=-0.5");
		return add("S=SplineV2({A,B,C},3)");
	}

	private static LocusIntersectionSolution2D admissibleRoot(
			GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		List<LocusIntersectionSolution2D> admissible = rich.getIntersectionResult()
				.getFiniteSolutions().stream()
				.filter(root -> root.getPairEvidence().isEmpty()
						&& rich.isPointAdmissible(root.getIdentity().getRootToken()))
				.toList();
		assertEquals(1, admissible.size(), "expected one admissible single-source root");
		return admissible.get(0);
	}

	private PersistentGeoId id(GeoElement geo) {
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id, geo.getLabelSimple());
		return id;
	}
}

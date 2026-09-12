/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactEntry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactReport;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineTransaction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** A3-R1 preflight, explicit fallback and complete impact evidence. */
class PostG9U1A3R1RedefineAssessmentTest extends G9U0PublicSurfaceTestBase {
	@Test
	void preflightDistinguishesAdvancedRelocationLegacyAndInvalidDag() {
		baseSplineInputs();
		GeoLocusV2 source = add("S=SplineV2(pointsA,degree)");
		GeoLocusV2 compatible = add("Compatible=SplineV2(pointsA,degree)");
		SpatialRedefineAssessment noMove = assess(source, compatible);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				noMove.getStatus());

		add("D=(-2,1)");
		add("E=(0,2)");
		add("F=(2,1)");
		add("pointsB={D,E,F}");
		GeoLocusV2 later = add("Later=SplineV2(pointsB,degree)");
		SpatialRedefineAssessment relocated = assess(source, later);
		assertEquals(SpatialRedefineAssessmentStatus
				.ADVANCED_RETAIN_AVAILABLE_WITH_RELOCATION,
				relocated.getStatus());

		add("w(x,y)=1+x^2+y^2");
		GeoLocusV2 weighted = add("Weighted=SplineV2(pointsA,degree,w)");
		SpatialRedefineAssessment legacy = assess(source, weighted);
		assertEquals(SpatialRedefineAssessmentStatus.LEGACY_REPLACEMENT_AVAILABLE,
				legacy.getStatus());
		assertEquals(SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE,
				legacy.getImpactReport().getCompleteness());

		add("v=(1,0)");
		GeoLocusV2 cyclic = add("Cyclic=Translate(S,v)");
		SpatialRedefineAssessment invalid = assess(source, cyclic);
		assertEquals(SpatialRedefineAssessmentStatus.INVALID_DAG,
				invalid.getStatus());
		assertEquals(null, invalid.getImpactReport());
	}

	@Test
	void retainFailureNeedsExplicitLegacySelectionAndStalePlanRejects() {
		baseSplineInputs();
		add("w(x,y)=1+x^2+y^2");
		GeoLocusV2 source = add("S=SplineV2(pointsA,degree)");
		GeoLocusV2 weighted = add("Weighted=SplineV2(pointsA,degree,w)");
		PersistentGeoId sourceId = id(source);
		SpatialRedefineAssessment legacy = assess(source, weighted);

		assertThrows(SpatialIdentityException.class,
				() -> registry().prepareRedefine(legacy,
						SpatialRedefineExecutionMode.ADVANCED_RETAIN));
		assertEquals(sourceId, id(source));
		SpatialRedefineTransaction explicit = registry().prepareRedefine(legacy,
				SpatialRedefineExecutionMode.LEGACY_REPLACEMENT);
		assertEquals(SpatialRedefineDecision.FRESH, explicit.getDecision());
		explicit.rollback();
		assertEquals(sourceId, id(source));

		SpatialRedefineAssessment current = assess(source, weighted);
		assertTrue(current.isCurrent());
		add("Mutation=9");
		assertFalse(current.isCurrent());
		assertThrows(SpatialIdentityException.class,
				() -> registry().prepareRedefine(current,
						SpatialRedefineExecutionMode.LEGACY_REPLACEMENT));

		final SpatialRedefineAssessment beforePreparedMutation =
				assess(source, weighted);
		final SpatialRedefineTransaction prepared = registry().prepareRedefine(
				beforePreparedMutation,
				SpatialRedefineExecutionMode.LEGACY_REPLACEMENT);
		add("MutationAfterPrepare=10");
		assertThrows(SpatialIdentityException.class,
				() -> registry().authorizeRedefineHostMutation(prepared));
		assertThrows(SpatialIdentityException.class,
				() -> prepared.commit(source));
		prepared.rollback();
	}

	@Test
	void legacyImpactIsCompleteTypedAndNeverUsesCoincidentUnrelatedGeometry() {
		baseSplineInputs();
		add("w(x,y)=1+x^2+y^2");
		GeoLocusV2 source = add("S=SplineV2(pointsA,degree)");
		GeoPoint first = add("P=Point(S,\"" + BRANCH + "\",-1)");
		GeoPoint second = add("R=Point(S,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult metric = add("M=LocusLength(S,P,R)");
		final GeoLocusV2 coincidentButUnrelated = add(
				"Other=SplineV2(pointsA,degree)");
		ProjectionBindingRecord brokenBinding = new ProjectionBindingRecord(
				registry().allocateProjectionBindingId(), 1,
				registry().allocateSpatialObjectId(),
				registry().allocateProjectionSystemId(),
				registry().allocateProjectionDiagramMapId(),
				registry().allocateProjectionFrameId(),
				ProjectionBindingRole.DEFINING, "POINT", "POINT",
				"cedg.test.a3-r1.impact", 1, List.of(id(source)), 0);
		registry().registerRecords(List.of(brokenBinding));
		GeoLocusV2 weighted = add("Weighted=SplineV2(pointsA,degree,w)");

		SpatialRedefineAssessment assessment = assess(source, weighted);
		SpatialRedefineImpactReport report = assessment.getImpactReport();
		assertNotNull(report);
		assertEquals(SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE,
				report.getCompleteness());
		Set<Object> affected = report.getEntries().stream()
				.map(SpatialRedefineImpactEntry::getParticipantId)
				.collect(Collectors.toSet());
		assertTrue(affected.contains(id(source)));
		assertTrue(affected.contains(id(first)));
		assertTrue(affected.contains(id(second)));
		assertTrue(affected.contains(id(metric)));
		assertTrue(affected.contains(brokenBinding.getId()));
		assertFalse(affected.contains(id(coincidentButUnrelated)));

		SpatialRedefineImpactEntry sourceImpact = report.getEntries().stream()
				.filter(entry -> entry.getParticipantId().equals(id(source)))
				.findFirst().orElseThrow();
		assertEquals(SpatialRedefineImpactEntry.PredictedStatus.RETIRED,
				sourceImpact.getPredictedStatus());
		assertEquals(SpatialRedefineImpactEntry.RecoveryClass
				.REQUIRES_EXPLICIT_REDEFINE, sourceImpact.getRecoveryClass());
		assertSame(source, registry().resolveImpactParticipant(sourceImpact));
		assertTrue(report.getEntries().stream()
				.filter(entry -> !entry.getParticipantId().equals(id(source)))
				.filter(entry -> !entry.getParticipantId().equals(
						brokenBinding.getId()))
				.allMatch(entry -> entry.getRecoveryClass()
						== SpatialRedefineImpactEntry.RecoveryClass
								.REQUIRES_EXPLICIT_REDEFINE));
		SpatialRedefineImpactEntry bindingImpact = report.getEntries().stream()
				.filter(entry -> entry.getParticipantId().equals(
						brokenBinding.getId()))
				.findFirst().orElseThrow();
		assertEquals(SpatialRedefineImpactEntry.PredictedStatus.BECOMES_UNDEFINED,
				bindingImpact.getPredictedStatus());
		assertEquals(SpatialRedefineImpactEntry.RecoveryClass
				.REQUIRES_EXPLICIT_REBIND, bindingImpact.getRecoveryClass());
		assertTrue(report.getEntries().stream().noneMatch(entry -> entry
				.getPredictedStatus()
				== SpatialRedefineImpactEntry.PredictedStatus.AUTO_REVALIDATES));
	}

	private void baseSplineInputs() {
		add("A=(-2,0)");
		add("B=(0,1)");
		add("C=(2,0)");
		add("pointsA={A,B,C}");
		add("degree=3");
	}

	private SpatialRedefineAssessment assess(GeoLocusV2 source,
			GeoLocusV2 candidate) {
		SpatialRedefineContext context = registry().captureRedefineContext(source);
		return registry().assessRedefine(context, candidate, List.of(candidate));
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private PersistentGeoId id(GeoElement geo) {
		PersistentGeoId id = registry().getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
	}
}

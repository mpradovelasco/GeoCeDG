/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Focused read-only Locus V2 export contract tests for G9X1. */
class G9X1LocusV2ExportTest extends BaseUnitTest {

	private final GeometryExportService service = new GeometryExportService();

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void l01OneEntityIsEmittedPerSemanticComponent() { // X1-L01
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,-1,true,true},{1,2,true,true}}");
		GeometryExportPreflight preflight = preflight(locus, request());

		assertTrue(preflight.isWritable());
		assertEquals(2, preflight.getApproximateCount());
		assertEquals(2, preflight.getModel().getEntities().size());
		assertEquals(2, preflight.getModel().getOutcomes().size());
		assertEquals(Arrays.asList("component-0", "component-1"), preflight
				.getModel().getOutcomes().stream().map(outcome -> outcome
						.getComponentAddress().getComponentKey())
				.collect(Collectors.toList()));
	}

	@Test
	void l02InvalidDomainGapIsNeverBridged() { // X1-L02
		GeoLocusV2 locus = createLocus("(s,0)",
				"{false,{-2,-1,true,true},{1,2,true,true}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		PolylineGeometry left = polyline(preflight, 0);
		PolylineGeometry right = polyline(preflight, 1);

		assertEquals(-1, last(left).getX(), 0);
		assertEquals(1, first(right).getX(), 0);
		assertTrue(last(left).getX() < first(right).getX());
		assertEquals(-2, preflight.getModel().getOutcomes().get(0)
				.getComponentAddress().getParameterStart(), 0);
		assertEquals(-1, preflight.getModel().getOutcomes().get(0)
				.getComponentAddress().getParameterEnd(), 0);
		assertEquals(1, preflight.getModel().getOutcomes().get(1)
				.getComponentAddress().getParameterStart(), 0);
		assertEquals(2, preflight.getModel().getOutcomes().get(1)
				.getComponentAddress().getParameterEnd(), 0);
	}

	@Test
	void l03SemanticComponentOrientationIsPreserved() { // X1-L03
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{2,-2,true,true}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		PolylineGeometry polyline = polyline(preflight, 0);
		ComponentAddress address = onlyOutcome(preflight).getComponentAddress();

		assertEquals(2, address.getParameterStart(), 0);
		assertEquals(-2, address.getParameterEnd(), 0);
		assertEquals(2, first(polyline).getX(), 0);
		assertEquals(-2, last(polyline).getX(), 0);
		for (int index = 1; index < polyline.getVertices().size(); index++) {
			assertTrue(polyline.getVertices().get(index - 1).getX()
					> polyline.getVertices().get(index).getX());
		}
	}

	@Test
	void l04CoincidentConstructiveComponentsRemainDistinct() { // X1-L04
		GeoLocusV2 locus = createLocus("(s^2,0)",
				"{false,{-2,-1,true,true},{1,2,true,true}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		PolylineGeometry first = polyline(preflight, 0);
		PolylineGeometry second = polyline(preflight, 1);
		SourceExportOutcome firstOutcome = preflight.getModel().getOutcomes().get(0);
		SourceExportOutcome secondOutcome = preflight.getModel().getOutcomes().get(1);

		assertEquals(4, first(first).getX(), 0);
		assertEquals(1, last(first).getX(), 0);
		assertEquals(1, first(second).getX(), 0);
		assertEquals(4, last(second).getX(), 0);
		assertEquals(firstOutcome.getSourceId(), secondOutcome.getSourceId());
		assertNotEquals(firstOutcome.getNeutralEntityId(),
				secondOutcome.getNeutralEntityId());
		assertNotEquals(firstOutcome.getComponentAddress().getComponentKey(),
				secondOutcome.getComponentAddress().getComponentKey());
	}

	@Test
	void l05TypedFullPeriodEvidenceClosesThePolyline() { // X1-L05
		GeoLocusV2 locus = createLocus("(cos(s),sin(s))",
				"{true,{0,2*pi,true,false}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		PolylineGeometry polyline = polyline(preflight, 0);
		ComponentAddress address = onlyOutcome(preflight).getComponentAddress();

		assertTrue(polyline.isClosed());
		assertTrue(address.isStartIncluded());
		assertFalse(address.isEndIncluded());
		assertFalse(samePoint(first(polyline), last(polyline)));
		assertEquals(polyline.getVertices().size(),
				onlyOutcome(preflight).getApproximationEvidence().getSegments());

		String sourceId = G9X1GeometryExportAdapter.requestSourceId(locus, 0);
		String branchKey = locus.getSemanticDefinition().getBranches().get(0)
				.getBranchKey();
		GeometryExportRequest explicitPeriod = GeometryExportRequest.builder(0.01)
				.addSourceSemanticDomain(sourceId, new SemanticDomain(branchKey,
						"full-period", 0, 2 * Math.PI, true, true))
				.build();
		GeometryExportPreflight explicit = preflight(locus, explicitPeriod);
		assertTrue(explicit.isWritable());
		assertTrue(polyline(explicit, 0).isClosed());
	}

	@Test
	void l06EndpointProximityDoesNotInferClosure() { // X1-L06
		GeoLocusV2 locus = createLocus("(cos(s),sin(s))",
				"{false,{0,2*pi,true,true}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		PolylineGeometry polyline = polyline(preflight, 0);

		assertFalse(polyline.isClosed());
		assertTrue(distance(first(polyline), last(polyline)) < 1E-12);
		assertEquals(0, onlyOutcome(preflight).getComponentAddress()
				.getParameterStart(), 0);
		assertEquals(2 * Math.PI, onlyOutcome(preflight).getComponentAddress()
				.getParameterEnd(), 1E-14);
	}

	@Test
	void l07OpenComponentWithoutClosedExportDomainIsRejected() { // X1-L07
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,2,false,false}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		SourceExportOutcome outcome = onlyOutcome(preflight);

		assertFalse(preflight.isWritable());
		assertTrue(preflight.isSidecarRequired());
		assertEquals(1, preflight.getInvalidCount());
		assertEquals(Fidelity.INVALID, outcome.getFidelity());
		assertEquals(Reason.MISSING_DOMAIN, outcome.getReason());
		assertTrue(preflight.getModel().getEntities().isEmpty());
	}

	@Test
	void l08ExplicitClosedSourceSubdomainIsAccepted() { // X1-L08
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,2,false,false}}");
		String sourceId = G9X1GeometryExportAdapter.requestSourceId(locus, 0);
		String branchKey = locus.getSemanticDefinition().getBranches().get(0)
				.getBranchKey();
		GeometryExportRequest wrongBranch = GeometryExportRequest.builder(0.01)
				.addSourceSemanticDomain(sourceId, new SemanticDomain(
						"other-branch", "bounded-subdomain", -1, 1, true, true))
				.build();
		GeometryExportPreflight rejected = preflight(locus, wrongBranch);
		assertFalse(rejected.isWritable());
		assertEquals(Reason.INVALID_DOMAIN,
				onlyOutcome(rejected).getReason());
		GeometryExportRequest mistypedSource = GeometryExportRequest.builder(0.01)
				.addSourceSemanticDomain(sourceId + "-typo", new SemanticDomain(
						branchKey, "bounded-subdomain", -1, 1, true, true))
				.build();
		GeometryExportPreflight mistyped = preflight(locus, mistypedSource);
		assertFalse(mistyped.isWritable());
		assertTrue(mistyped.getModel().getOutcomes().stream()
				.anyMatch(item -> item.getReason() == Reason.INVALID_DOMAIN));

		GeometryExportRequest exportRequest = GeometryExportRequest.builder(0.01)
				.addSourceSemanticDomain(sourceId, new SemanticDomain(
						branchKey, "bounded-subdomain", -1, 1, true, true))
				.build();
		GeometryExportPreflight preflight = preflight(locus, exportRequest);
		SourceExportOutcome outcome = onlyOutcome(preflight);

		assertTrue(preflight.isWritable());
		assertEquals(Fidelity.APPROXIMATE, outcome.getFidelity());
		assertEquals("component-0/subdomain-bounded-subdomain",
				outcome.getComponentAddress().getComponentKey());
		assertEquals(-1, outcome.getComponentAddress().getParameterStart(), 0);
		assertEquals(1, outcome.getComponentAddress().getParameterEnd(), 0);
		assertEquals(-1, first(polyline(preflight, 0)).getX(), 0);
		assertEquals(1, last(polyline(preflight, 0)).getX(), 0);
	}

	@Test
	void l09LocusRevisionChangeInvalidatesPreflight() { // X1-L09
		GeoLocusV2 locus = createLocus("(s,0)",
				"{false,{-2,2,true,true}}");
		GeometryExportPreflight preflight = preflight(locus, request());
		long revision = locus.getSemanticRevision();
		GeoNumeric driver = (GeoNumeric) lookup("s");
		assertTrue(preflight.isSourceRevisionCurrent());

		driver.setValue(0.75);
		driver.updateCascade();

		assertTrue(locus.getSemanticRevision() > revision);
		assertFalse(preflight.isSourceRevisionCurrent());
		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> service.encode(preflight));
		assertTrue(failure.getMessage().contains("STALE_SOURCE_REVISION"));
	}

	@Test
	void l10ExportNeverReadsRenderOrViewportAuthority() { // X1-L10
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,2,true,true}}");
		locus.getInstrumentation().reset();
		getApp().getActiveEuclidianView().setCoordSystem(341, 217, 43, 29);
		String first = service.encode(preflight(locus, request())).getDxfText();
		getApp().getActiveEuclidianView().setCoordSystem(611, 389, 157, 83);
		String second = service.encode(preflight(locus, request())).getDxfText();

		assertEquals(first, second);
		locus.getInstrumentation().reset();
		String exportOnly = service.encode(preflight(locus, request())).getDxfText();
		assertEquals(first, exportOnly);
		assertTrue(locus.getInstrumentation().getEvaluatorCalls() > 0);
		assertEquals(0, locus.getInstrumentation().getRenderEvaluations());
		assertEquals(0, locus.getInstrumentation().getWholeLocusRegenerations());
	}

	@Test
	void l11ExportLeavesGeometryDagXmlAndUndoUnchanged() { // X1-L11
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,2,true,true}}");
		activateUndo();
		getApp().storeUndoInfo();
		LocusDefinition2D definition = locus.getSemanticDefinition();
		long revision = locus.getSemanticRevision();
		String xml = getApp().getXML();
		int steps = getConstruction().steps();
		int geos = getConstruction().getGeoSetConstructionOrder().size();
		int identities = getConstruction().getSpatialIdentityRegistry()
				.getRecords().size();
		final int undo = getConstruction().getUndoManager().getHistorySize();
		final List<GeoElement> inputs = Arrays.asList(
				locus.getParentAlgorithm().getInput().clone());

		GeometryExportPreflight preflight = preflight(locus, request());
		assertNotNull(service.encode(preflight));

		assertSame(definition, locus.getSemanticDefinition());
		assertEquals(revision, locus.getSemanticRevision());
		assertEquals(xml, getApp().getXML());
		assertEquals(steps, getConstruction().steps());
		assertEquals(geos, getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(identities, getConstruction().getSpatialIdentityRegistry()
				.getRecords().size());
		assertEquals(undo,
				getConstruction().getUndoManager().getHistorySize());
		assertEquals(inputs,
				Arrays.asList(locus.getParentAlgorithm().getInput()));
	}

	@Test
	void l12ExportIsDeterministicAcrossRecomputeAndReopen() { // X1-L12
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,2,true,true}}");
		String identity = locus.getLocusIdentity();
		GeometryExportPreflight initial = preflight(locus, request());
		String initialDxf = service.encode(initial).getDxfText();
		String initialSignature = exportSignature(initial);

		locus.getParentAlgorithm().update();
		GeometryExportPreflight recomputed = preflight(locus, request());
		assertEquals(initialDxf, service.encode(recomputed).getDxfText());
		assertEquals(initialSignature, exportSignature(recomputed));

		reload();
		GeoLocusV2 reopened = (GeoLocusV2) lookup("L");
		assertNotNull(reopened);
		assertEquals(identity, reopened.getLocusIdentity());
		GeometryExportPreflight reopenedPreflight = preflight(reopened, request());
		assertEquals(initialDxf,
				service.encode(reopenedPreflight).getDxfText());
		assertEquals(initialSignature, exportSignature(reopenedPreflight));
	}

	private GeoLocusV2 createLocus(String pointExpression,
			String domainExpression) {
		add("s=0");
		add("Q=" + pointExpression);
		add("D=" + domainExpression);
		GeoLocusV2 locus = add("L=LocusV2(Q,s,D)");
		assertNotNull(locus);
		return locus;
	}

	private GeometryExportPreflight preflight(GeoElement source,
			GeometryExportRequest exportRequest) {
		return service.preflight(Collections.singletonList(source),
				SelectionMode.CURRENT_SELECTION, exportRequest);
	}

	private static GeometryExportRequest request() {
		return GeometryExportRequest.builder(0.01).build();
	}

	private static SourceExportOutcome onlyOutcome(
			GeometryExportPreflight preflight) {
		assertEquals(1, preflight.getModel().getOutcomes().size());
		return preflight.getModel().getOutcomes().get(0);
	}

	private static PolylineGeometry polyline(GeometryExportPreflight preflight,
			int index) {
		Entity entity = preflight.getModel().getEntities().get(index);
		return (PolylineGeometry) entity.getGeometry();
	}

	private static Point2D first(PolylineGeometry polyline) {
		return polyline.getVertices().get(0);
	}

	private static Point2D last(PolylineGeometry polyline) {
		return polyline.getVertices().get(polyline.getVertices().size() - 1);
	}

	private static double distance(Point2D first, Point2D second) {
		return Math.hypot(first.getX() - second.getX(),
				first.getY() - second.getY());
	}

	private static boolean samePoint(Point2D first, Point2D second) {
		return Double.doubleToLongBits(first.getX())
				== Double.doubleToLongBits(second.getX())
				&& Double.doubleToLongBits(first.getY())
						== Double.doubleToLongBits(second.getY());
	}

	private static String exportSignature(GeometryExportPreflight preflight) {
		StringBuilder signature = new StringBuilder();
		for (SourceExportOutcome outcome : preflight.getModel().getOutcomes()) {
			ComponentAddress address = outcome.getComponentAddress();
			signature.append(outcome.getSourceId()).append('|')
					.append(outcome.getFidelity()).append('|')
					.append(address.getBranchKey()).append('|')
					.append(address.getComponentKey()).append('|')
					.append(address.getParameterStart()).append('|')
					.append(address.getParameterEnd()).append('|')
					.append(outcome.getApproximationEvidence().getEvaluations())
					.append('|')
					.append(outcome.getApproximationEvidence().getVertices())
					.append(';');
		}
		for (Entity entity : preflight.getModel().getEntities()) {
			for (Point2D vertex : ((PolylineGeometry) entity.getGeometry())
					.getVertices()) {
				signature.append(Long.toHexString(
						Double.doubleToLongBits(vertex.getX()))).append(':')
						.append(Long.toHexString(
								Double.doubleToLongBits(vertex.getY())))
						.append(',');
			}
		}
		return signature.toString();
	}
}

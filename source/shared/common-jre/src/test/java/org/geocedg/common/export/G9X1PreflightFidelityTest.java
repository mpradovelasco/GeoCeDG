/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Focused preflight and component-fidelity contract tests for G9X1. */
class G9X1PreflightFidelityTest extends BaseUnitTest {

	private final GeometryExportService service = new GeometryExportService();

	@Test
	void p01ExactComponentsAreMachineClassified() { // X1-P01
		GeoElement point = add("A=(1,2)");
		GeoElement segment = add("s=Segment((0,0),(3,4))");
		GeometryExportPreflight preflight = preflight(
				Arrays.asList(point, segment), request());

		assertEquals(2, preflight.getExactCount());
		assertEquals(0, preflight.getApproximateCount());
		assertTrue(preflight.getModel().getOutcomes().stream()
				.allMatch(outcome -> outcome.getFidelity() == Fidelity.EXACT));
		assertTrue(preflight.getModel().getOutcomes().stream()
				.allMatch(SourceExportOutcome::isEmitted));
		String exactSourceId = G9X1GeometryExportAdapter.requestSourceId(point, 0);
		GeometryExportRequest unusedOverride = GeometryExportRequest.builder(0.01)
				.addSourceSemanticDomain(exactSourceId,
						new SemanticDomain("unused", 0, 1, true, true))
				.sourceSemanticDomains("mistyped-empty-source",
						Collections.emptyList())
				.build();
		GeometryExportPreflight rejected = preflight(
				Collections.singletonList(point), unusedOverride);
		assertFalse(rejected.isWritable());
		assertTrue(rejected.getModel().getOutcomes().stream()
				.anyMatch(item -> item.getReason() == Reason.INVALID_DOMAIN));
		assertEquals(2, rejected.getInvalidCount());
	}

	@Test
	void p02ApproximateComponentsCarryEstablishedEvidence() { // X1-P02
		GeoElement function = add("f(x)=x^2");
		GeometryExportPreflight preflight = preflight(
				Collections.singletonList(function), boundedRequest(-1, 1));
		SourceExportOutcome outcome = onlyOutcome(preflight);

		assertEquals(0, preflight.getExactCount());
		assertEquals(1, preflight.getApproximateCount());
		assertEquals(Fidelity.APPROXIMATE, outcome.getFidelity());
		assertEquals(Reason.NONE, outcome.getReason());
		assertNotNull(outcome.getApproximationEvidence());
		assertTrue(outcome.getApproximationEvidence().hasAchievedError());

		GeoElement limited = add("g=Function(x^2,0,1)");
		assertEquals(Fidelity.APPROXIMATE, onlyOutcome(preflight(
				Collections.singletonList(limited), boundedRequest(0.2, 0.8)))
				.getFidelity());
		assertEquals(Fidelity.APPROXIMATE, onlyOutcome(preflight(
				Collections.singletonList(limited), boundedRequest(0.8, 0.2)))
				.getFidelity());
		assertEquals(Reason.INVALID_DOMAIN, onlyOutcome(preflight(
				Collections.singletonList(limited), boundedRequest(-0.1, 0.8)))
				.getReason());
	}

	@Test
	void p03UnsupportedComponentsExposeTypedReason() { // X1-P03
		GeoElement text = add("T=\"not geometry\"");
		GeometryExportPreflight preflight = preflight(
				Collections.singletonList(text), request());
		SourceExportOutcome outcome = onlyOutcome(preflight);

		assertEquals(1, preflight.getUnsupportedCount());
		assertEquals(Fidelity.UNSUPPORTED, outcome.getFidelity());
		assertEquals(Reason.UNSUPPORTED_FAMILY, outcome.getReason());
		assertFalse(outcome.isEmitted());
		assertNotNull(outcome.getMessage());
	}

	@Test
	void p04InvalidComponentsExposeTypedReason() { // X1-P04
		GeoElement undefined = undefinedPoint("U");
		GeometryExportPreflight preflight = preflight(
				Collections.singletonList(undefined), request());
		SourceExportOutcome outcome = onlyOutcome(preflight);

		assertEquals(1, preflight.getInvalidCount());
		assertEquals(Fidelity.INVALID, outcome.getFidelity());
		assertEquals(Reason.UNDEFINED_SOURCE, outcome.getReason());
		assertFalse(outcome.isEmitted());
		assertNotNull(outcome.getMessage());
	}

	@Test
	void p05SourceAndComponentTotalsReconcile() { // X1-P05
		GeoElement exact = add("A=(1,2)");
		GeoElement approximate = add("f(x)=x^2");
		GeoElement unsupported = add("T=\"not geometry\"");
		GeoElement invalid = undefinedPoint("U");
		List<GeoElement> sources = Arrays.asList(exact, approximate, unsupported,
				invalid);
		GeometryExportPreflight preflight = preflight(sources,
				boundedRequest(-1, 1));

		int classified = preflight.getExactCount()
				+ preflight.getApproximateCount()
				+ preflight.getUnsupportedCount() + preflight.getInvalidCount();
		Set<String> classifiedSources = new HashSet<>();
		for (SourceExportOutcome outcome : preflight.getModel().getOutcomes()) {
			classifiedSources.add(outcome.getSourceId());
		}
		assertEquals(4, classified);
		assertEquals(sources.size(), classifiedSources.size());
		assertEquals(2, preflight.getModel().getEntities().size());
		assertEquals(2, preflight.getOmittedCount());
	}

	@Test
	void p06HiddenSourcesRemainIncludedAndReported() { // X1-P06
		GeoElement exact = add("A=(1,2)");
		GeoElement approximate = add("f(x)=x^2");
		exact.setEuclidianVisible(false);
		approximate.setEuclidianVisible(false);
		GeometryExportPreflight preflight = preflight(
				Arrays.asList(exact, approximate), boundedRequest(-1, 1));

		assertEquals(2, preflight.getHiddenCount());
		assertEquals(2, preflight.getModel().getEntities().size());
		assertTrue(preflight.getModel().getOutcomes().stream()
				.noneMatch(SourceExportOutcome::isVisible));
		assertTrue(preflight.getModel().getEntities().stream()
				.noneMatch(entity -> entity.getStyle().isVisible()));
	}

	@Test
	void p07StrictPolicyRejectsPartialOutput() { // X1-P07
		GeoElement exact = add("A=(1,2)");
		GeoElement unsupported = add("T=\"not geometry\"");
		GeometryExportRequest strict = request();
		GeometryExportPreflight preflight = preflight(
				Arrays.asList(exact, unsupported), strict);

		assertFalse(strict.isPartialOutputAllowed());
		assertFalse(preflight.isWritable());
		assertThrows(IllegalStateException.class,
				() -> service.encode(preflight));
		GeometryExportRequest partial = GeometryExportRequest.builder(0.01)
				.allowPartialOutput(true).build();
		assertThrows(IllegalArgumentException.class,
				() -> preflight(Collections.singletonList(exact), partial));
	}

	@Test
	void p08EveryFidelityReductionRequiresSidecar() { // X1-P08
		GeoElement exact = add("A=(1,2)");
		GeoElement approximate = add("f(x)=x^2");
		GeoElement unsupported = add("T=\"not geometry\"");
		GeoElement invalid = undefinedPoint("U");

		assertFalse(preflight(Collections.singletonList(exact), request())
				.isSidecarRequired());
		assertTrue(preflight(Collections.singletonList(approximate),
				boundedRequest(-1, 1)).isSidecarRequired());
		assertTrue(preflight(Collections.singletonList(unsupported), request())
				.isSidecarRequired());
		assertTrue(preflight(Collections.singletonList(invalid), request())
				.isSidecarRequired());
		GeometryExportRequest requested = GeometryExportRequest.builder(0.01)
				.requestSidecar(true).build();
		assertTrue(preflight(Collections.singletonList(exact), requested)
				.isSidecarRequired());
	}

	@Test
	void p09WritabilityIsDecidedBeforeEncoding() { // X1-P09
		GeoElement exact = add("A=(1,2)");
		GeoElement unsupported = add("T=\"not geometry\"");
		GeometryExportPreflight writable = preflight(
				Collections.singletonList(exact), request());
		GeometryExportPreflight blocked = preflight(
				Collections.singletonList(unsupported), request());

		assertTrue(writable.isWritable());
		assertNotNull(service.encode(writable));
		assertFalse(blocked.isWritable());
		assertEquals(1, blocked.getOmittedCount());
		assertThrows(IllegalStateException.class,
				() -> service.encode(blocked));
		assertThrows(IllegalArgumentException.class,
				() -> GeometryExportRequest.builder(0.01)
						.addDefaultSemanticDomain(new SemanticDomain(
								"duplicate", 0, 1, true, true))
						.addDefaultSemanticDomain(new SemanticDomain(
								"duplicate", 1, 2, true, true)));
	}

	@Test
	void p10EncodingRejectsStaleSourceRevision() { // X1-P10
		GeoNumeric driver = add("a=1");
		GeoElement point = add("A=(a,0)");
		GeometryExportPreflight preflight = preflight(
				Collections.singletonList(point), request());
		assertTrue(preflight.isSourceRevisionCurrent());

		driver.setValue(2);
		driver.updateCascade();

		assertFalse(preflight.isSourceRevisionCurrent());
		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> service.encode(preflight));
		assertTrue(failure.getMessage().contains("STALE_SOURCE_REVISION"));
	}

	private GeometryExportPreflight preflight(List<GeoElement> sources,
			GeometryExportRequest exportRequest) {
		return service.preflight(sources, SelectionMode.CURRENT_SELECTION,
				exportRequest);
	}

	private static GeometryExportRequest request() {
		return GeometryExportRequest.builder(0.01).build();
	}

	private static GeometryExportRequest boundedRequest(double start,
			double end) {
		return GeometryExportRequest.builder(0.01)
				.addDefaultSemanticDomain(new SemanticDomain("bounded-domain",
						start, end, true, true)).build();
	}

	private GeoElement undefinedPoint(String label) {
		GeoElement point = add(label + "=(0,0)");
		point.setUndefined();
		assertFalse(point.isDefined());
		return point;
	}

	private static SourceExportOutcome onlyOutcome(
			GeometryExportPreflight preflight) {
		assertEquals(1, preflight.getModel().getOutcomes().size());
		return preflight.getModel().getOutcomes().get(0);
	}
}

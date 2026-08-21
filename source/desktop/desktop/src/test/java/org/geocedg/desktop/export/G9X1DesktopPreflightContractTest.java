/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.export.G9X1GeometryExportAdapter;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.desktop.export.DxfExportPreflightPresentation.Destination;
import org.geocedg.desktop.export.DxfExportPreflightPresentation.DestinationPort;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

class G9X1DesktopPreflightContractTest extends BaseUnitTest {

	private final GeometryExportService service = new GeometryExportService();

	// X1-S01
	@Test
	void showsPreflightBeforeChoosingDestination() {
		DxfExportPreflightPresentation presentation = presentation(
				add("A=(1,2)"), strictRequest(false));
		List<String> events = new ArrayList<>();
		Path path = Path.of("build", "g9x1-s01.dxf");

		Destination destination = presentation.requestDestination(
				acceptingPort(events, path));

		assertEquals(List.of("preflight", "destination"), events);
		assertEquals(path, destination.getDxfPath());
		assertFalse(destination.isReplaceExisting());
		assertFalse(DxfExportPreflightPresentation.isExtendedDxfEnabled(
				new AppConfigGeoCeDG()));
		assertFalse(DxfExportPreflightPresentation.isExtendedDxfEnabled(
				new AppConfigGeoCeDG(true)));
		assertTrue(DxfExportPreflightPresentation.isExtendedDxfEnabled(
				new AppConfigGeoCeDG(false, true)));
	}

	// X1-S02
	@Test
	void identifiesApproximateOutputAndItsEvidence() {
		GeoElement function = add("f(x)=x^2");
		DxfExportPreflightPresentation presentation = presentation(function,
				approximateRequest(false));
		List<String> events = new ArrayList<>();

		final Destination destination = presentation.requestDestination(
				acceptingPort(events, Path.of("build", "g9x1-s02.dxf")));

		assertEquals(List.of("preflight", "approximate-confirmation",
				"destination"), events);
		assertTrue(presentation.requiresApproximateConfirmation());
		assertTrue(presentation.getSummaryText().contains("approximate=1"));
		assertTrue(presentation.getApproximationEvidenceText()
				.contains("ORIENTED_DYADIC_REFINEMENT"));
		assertTrue(presentation.getApproximationEvidenceText()
				.contains("ESTIMATED_ERROR"));
		assertTrue(presentation.getApproximationEvidenceText()
				.contains("requested=0.05"));
		assertTrue(presentation.getCompletionEvidenceText()
				.contains("Requested tolerance: 0.05"));
		assertTrue(presentation.getCompletionEvidenceText()
				.contains("Allowed guarantees: [ESTIMATED_ERROR]"));
		assertTrue(presentation.getCompletionEvidenceText()
				.contains("achieved="));
		assertNotNull(destination);

		DxfExportPreflightPresentation stalePresentation = presentation(function,
				approximateRequest(false));
		List<String> staleEvents = new ArrayList<>();
		Destination staleDestination = stalePresentation.requestDestination(
				new DestinationPort() {
					@Override
					public boolean presentPreflight(
							DxfExportPreflightPresentation ignored) {
						staleEvents.add("preflight");
						return true;
					}

					@Override
					public boolean confirmApproximateExport(
							DxfExportPreflightPresentation ignored) {
						staleEvents.add("approximate-confirmation");
						add("staleMarker=1");
						return true;
					}

					@Override
					public void reportStaleSource(
							DxfExportPreflightPresentation ignored) {
						staleEvents.add("stale");
					}

					@Override
					public Destination chooseDestination(
							DxfExportPreflightPresentation ignored) {
						staleEvents.add("destination");
						return new Destination(Path.of("must-not-be-chosen.dxf"),
								false);
					}
				});
		assertNull(staleDestination);
		assertEquals(List.of("preflight", "approximate-confirmation", "stale"),
				staleEvents);
		assertTrue(stalePresentation.getWarningsText()
				.contains("STALE_SOURCE_REVISION"));
	}

	// X1-S03
	@Test
	void keepsStrictNoPartialBehaviorAsUiDefault() {
		DxfExportPreflightPresentation presentation = presentation(
				add("n=1"), strictRequest(false));
		List<String> events = new ArrayList<>();

		Destination destination = presentation.requestDestination(
				acceptingPort(events, Path.of("build", "must-not-be-chosen.dxf")));

		assertNull(destination);
		assertEquals(Collections.singletonList("preflight"), events);
		assertTrue(presentation.isStrictNoPartial());
		assertFalse(presentation.isWritable());
		assertTrue(presentation.getSummaryText()
				.contains("disabled (strict complete-request policy)"));

		GeometryExportRequest limited = GeometryExportRequest.builder(1E-12)
				.addDefaultSemanticDomain(new SemanticDomain("limited", 0, 1,
						true, true))
				.maximumDepth(0)
				.build();
		DxfExportPreflightPresentation failedApproximation = presentation(
				add("limited(x)=x^2"), limited);
		assertFalse(failedApproximation.isWritable());
		assertTrue(failedApproximation.getApproximationEvidenceText()
				.contains("fidelity=INVALID"));
		assertTrue(failedApproximation.getApproximationEvidenceText()
				.contains("reason=WORK_LIMIT"));
		assertTrue(failedApproximation.getApproximationEvidenceText()
				.contains("evaluations="));
		assertTrue(failedApproximation.getWarningsText()
				.contains("[WORK_LIMIT]"));
		assertFalse(failedApproximation.getWarningsText()
				.contains("[DEGENERATE]"));
	}

	// X1-S04
	@Test
	void reportsSidecarUnitlessCoordinatesAndWarnings() throws Exception {
		GeoElement function = add("g(x)=sin(x)");
		function.setEuclidianVisible(false);
		DxfExportPreflightPresentation presentation = presentation(function,
				approximateRequest(false));

		assertTrue(presentation.isSidecarRequired());
		assertTrue(presentation.getSummaryText()
				.contains("source unit=UNITLESS; target unit=UNITLESS"));
		assertTrue(presentation.getSummaryText()
				.contains("Sidecar: mandatory/requested"));
		assertTrue(presentation.getSummaryText().contains("hidden=1"));
		assertTrue(presentation.getWarningsText()
				.contains("APPROXIMATE"));
		assertTrue(presentation.getWarningsText()
				.contains("HIDDEN_SOURCE_INCLUDED"));

		GeometryExportRequest parsed = parseDesktopRequest(
				"-2:-1;g@function:-1:1", Collections.singletonList(function));
		assertEquals(1, parsed.getDefaultSemanticDomains().size());
		String sourceId = G9X1GeometryExportAdapter.requestSourceId(function, 0);
		assertEquals(1, parsed.getSourceSemanticDomains().get(sourceId).size());
		assertEquals("function", parsed.getSourceSemanticDomains().get(sourceId)
				.get(0).getBranchKey());
		DxfExportPreflightPresentation directed = presentation(function, parsed);
		assertTrue(directed.getSummaryText().contains("[branch=function]"));
	}

	private DxfExportPreflightPresentation presentation(GeoElement source,
			GeometryExportRequest request) {
		GeometryExportPreflight preflight = service.preflight(
				Collections.singletonList(source), SelectionMode.CURRENT_SELECTION,
				request);
		return DxfExportPreflightPresentation.from(preflight);
	}

	private static GeometryExportRequest strictRequest(
			boolean sidecarRequested) {
		return GeometryExportRequest.builder(0.05)
				.allowApproximation(true)
				.allowPartialOutput(false)
				.requestSidecar(sidecarRequested)
				.build();
	}

	private static GeometryExportRequest approximateRequest(
			boolean sidecarRequested) {
		return GeometryExportRequest.builder(0.05)
				.addDefaultSemanticDomain(new SemanticDomain("bounded", -1, 1,
						true, true))
				.allowApproximation(true)
				.allowPartialOutput(false)
				.requestSidecar(sidecarRequested)
				.build();
	}

	private static GeometryExportRequest parseDesktopRequest(String domains,
			List<GeoElement> sources) throws Exception {
		Class<?> controller = Class.forName(
				"org.geocedg.desktop.GeoCeDGDxfExportController");
		Method request = controller.getDeclaredMethod("request", String.class,
				boolean.class, String.class, String.class, String.class,
				String.class, String.class, boolean.class, List.class);
		request.setAccessible(true);
		return (GeometryExportRequest) request.invoke(null, "0.05", true, domains,
				Long.toString(GeometryExportRequest.DEFAULT_MAXIMUM_EVALUATIONS),
				Integer.toString(GeometryExportRequest.DEFAULT_MAXIMUM_DEPTH),
				Integer.toString(
						GeometryExportRequest.DEFAULT_MAXIMUM_VERTICES_PER_COMPONENT),
				Integer.toString(
						GeometryExportRequest.DEFAULT_MAXIMUM_TOTAL_VERTICES),
				false, sources);
	}

	private static DestinationPort acceptingPort(List<String> events,
			Path path) {
		return new DestinationPort() {
			@Override
			public boolean presentPreflight(
					DxfExportPreflightPresentation presentation) {
				events.add("preflight");
				return true;
			}

			@Override
			public boolean confirmApproximateExport(
					DxfExportPreflightPresentation presentation) {
				events.add("approximate-confirmation");
				return true;
			}

			@Override
			public void reportStaleSource(
					DxfExportPreflightPresentation presentation) {
				events.add("stale");
			}

			@Override
			public Destination chooseDestination(
					DxfExportPreflightPresentation presentation) {
				events.add("destination");
				return new Destination(path, false);
			}
		};
	}
}

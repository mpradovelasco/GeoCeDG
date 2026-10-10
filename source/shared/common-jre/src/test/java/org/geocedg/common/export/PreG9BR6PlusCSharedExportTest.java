/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.export.GeometryExportArea.Producer;
import org.geocedg.common.export.GeometryExportModel.ArcGeometry;
import org.geocedg.common.export.GeometryExportModel.AreaExclusion;
import org.geocedg.common.export.GeometryExportModel.CircleGeometry;
import org.geocedg.common.export.GeometryExportModel.EllipseGeometry;
import org.geocedg.common.export.GeometryExportModel.GeometryType;
import org.geocedg.common.export.GeometryExportModel.LinearGeometry;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PointGeometry;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportModel.Unit;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.Component;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.SourceResult;
import org.geocedg.common.export.SemanticExportClassification.ExportClass;
import org.geocedg.common.export.SemanticExportClassification.SourceClass;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.IdentityScope;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.export.SourceExportOutcome.SemanticCoverage;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.layers.HiddenLayerSet;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.intersection.CertifiedComponentAreaDisjointness2D;
import org.geocedg.common.kernel.locus.intersection.CertifiedComponentAreaDisjointness2D.Status;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-C shared export contracts: DXF units and the usm sidecar rule
 * (DQ-C11), hidden layers written OFF and 60 = 1 (DQ-C2, DQ-C3), B1 export-area
 * participation with the conservative certified test (DQ-C13), the staleness
 * inputs, the extracted semantic adapter (C10), the LaTeX result model (DQ-C5)
 * and the physical scale (C4).
 */
class PreG9BR6PlusCSharedExportTest extends BaseUnitTest {

	private final GeometryExportService service = new GeometryExportService();

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	// ------------------------------------------------------------ T-DXF-UNITS

	@Test
	void dxfDeclaresTheEffectiveConstructionUnitAndKeepsCoordinates() {
		List<GeoElement> sources = exactScene();
		String unitless = exactDxf(sources, GeometryExportContext.UNSPECIFIED);
		assertEquals("0", header(unitless, "$INSUNITS"));
		UnitState[] states = {UnitState.of(UnitToken.MM, null, null),
				UnitState.of(UnitToken.CM, UnitToken.MM, null),
				UnitState.of(UnitToken.M, UnitToken.CM, null)};
		String[] codes = {"4", "5", "6"};
		Unit[] units = {Unit.MM, Unit.CM, Unit.M};
		for (int index = 0; index < states.length; index++) {
			GeometryExportContext context = GeometryExportContext.of(states[index],
					HiddenLayerSet.EMPTY, null);
			GeometryExportModel model = service.createModel(sources,
					SelectionMode.COMPLETE_CONSTRUCTION, context);
			assertEquals(units[index], model.getSourceUnit());
			assertEquals(units[index], model.getTargetUnit());
			String dxf = service.exportDxf(model);
			assertEquals(codes[index], header(dxf, "$INSUNITS"), states[index].toString());
			assertEquals(unitless, dxf.replace("$INSUNITS\r\n70\r\n" + codes[index],
					"$INSUNITS\r\n70\r\n0"), "coordinates and entities unchanged");
		}
		assertEquals(4, Unit.MM.getInsunitsCode());
		assertEquals(5, Unit.CM.getInsunitsCode());
		assertEquals(6, Unit.M.getInsunitsCode());
		assertEquals(0, Unit.USM.getInsunitsCode());
		assertEquals(0, Unit.UNITLESS.getInsunitsCode());
		assertNull(Unit.UNITLESS.getToken());
	}

	@Test
	void unspecifiedDocumentsKeepUnitlessAndAPresentationChangeNothing() {
		List<GeoElement> sources = exactScene();
		getConstruction().getUnitSystem().replace(UnitState.of(UnitToken.MM, null, null));
		String mm = service.exportDxf(service.createModel(sources,
				SelectionMode.COMPLETE_CONSTRUCTION));
		getConstruction().getUnitSystem().replace(
				UnitState.of(UnitToken.MM, UnitToken.M, null));
		assertEquals(mm, service.exportDxf(service.createModel(sources,
				SelectionMode.COMPLETE_CONSTRUCTION)), "presentationUnit never matters");
		getConstruction().getUnitSystem().replace(UnitState.of(null, null,
				UsmDefinition.of(0.3048, null, "ft")));
		GeometryExportModel unspecified = service.createModel(sources,
				SelectionMode.COMPLETE_CONSTRUCTION);
		assertEquals(Unit.UNITLESS, unspecified.getTargetUnit(),
				"a usm definition without a construction unit stays unspecified");
		assertEquals("0", header(service.exportDxf(unspecified), "$INSUNITS"));
		assertNull(unspecified.getContext().getCanonicalMetresPerUnit());
	}

	// -------------------------------------------------------------- T-DXF-USM

	@Test
	void usmWritesUnitlessDxfAWarningCommentAndRequiresTheSidecar() {
		List<GeoElement> sources = exactScene();
		UnitState state = UnitState.of(UnitToken.USM, UnitToken.CM,
				UsmDefinition.of(0.0254, "inch", "in"));
		getConstruction().getUnitSystem().replace(state);
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, exactRequest());
		assertTrue(preflight.isWritable());
		assertEquals(0, preflight.getApproximateCount());
		assertTrue(preflight.isCustomUnit());
		assertTrue(preflight.isSidecarRequired(), "usm makes the sidecar mandatory");
		assertEquals("0.0254", preflight.getModel().getContext()
				.getCanonicalMetresPerUnit());
		assertNotNull(preflight.getModel().getContext().getUsmDefinition());
		String dxf = service.encode(preflight).getDxfText();
		assertEquals("0", header(dxf, "$INSUNITS"));
		assertTrue(dxf.contains("999\r\nGeoCeDG construction unit usm (custom unit, "
				+ "0.0254 m per unit); $INSUNITS 0;"), dxf);
	}

	// ------------------------------------------------------- T-DXF-VISIBILITY

	@Test
	void hiddenLayersAreOffDxfLayersAndHiddenObjectsKeepGroup60() {
		GeoElement onHidden = add("a=Segment((0,0),(1,0))");
		onHidden.setLayer(3);
		GeoElement hiddenObject = add("b=Segment((0,1),(1,1))");
		hiddenObject.setEuclidianVisible(false);
		GeoElement both = add("c=Segment((0,2),(1,2))");
		both.setLayer(3);
		both.setEuclidianVisible(false);
		GeoElement shown = add("d=Segment((0,3),(1,3))");
		shown.setLayer(4);
		List<GeoElement> sources = Arrays.asList(onHidden, hiddenObject, both, shown);
		GeometryExportContext context = GeometryExportContext.of(UnitState.EMPTY,
				HiddenLayerSet.of(3), null);
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.CURRENT_SELECTION, exactRequest(), () -> context);
		assertEquals(4, preflight.getModel().getEntities().size(),
				"every object stays in the export");
		String dxf = service.encode(preflight).getDxfText();
		// PRE-G9B-R6-plus-E3-R1: each layer record now starts with its handle, owner
		// and subclass markers; name, flags and color are unchanged
		String layer = "100\r\nAcDbLayerTableRecord\r\n2\r\n";
		assertTrue(dxf.contains(layer + "GEOCEDG_L3\r\n70\r\n0\r\n62\r\n-7\r\n"));
		assertTrue(dxf.contains(layer + "GEOCEDG_L4\r\n70\r\n0\r\n62\r\n7\r\n"));
		assertTrue(dxf.contains(layer + "0\r\n70\r\n0\r\n62\r\n7\r\n"));
		List<List<String[]>> entities = entities(dxf);
		assertEquals(4, entities.size());
		assertEquals("GEOCEDG_L3", value(entities.get(0), 8));
		assertNull(value(entities.get(0), 60), "a hidden layer is not object visibility");
		assertEquals("1", value(entities.get(1), 60));
		assertEquals("GEOCEDG_L3", value(entities.get(2), 8));
		assertEquals("1", value(entities.get(2), 60), "both mechanisms");
		assertNull(value(entities.get(3), 60));
		assertTrue(onHidden.isEuclidianVisible(), "object visibility is never changed");
		assertEquals(Arrays.asList(3), context.getHiddenLayers().toList());
	}

	// ------------------------------------------------- T-DXF-AREA predicates

	@Test
	void exactPredicatesDecideParticipationOfEveryFamily() {
		GeometryExportArea r = GeometryExportArea.explicit(Producer.MANUAL, 0, 10, 0,
				10, 1);
		assertFalse(meets(new PointGeometry(p(12, 5)), r));
		assertTrue(meets(new PointGeometry(p(5, 5)), r));
		assertTrue(meets(new PointGeometry(p(10, 5)), r), "closed boundary");
		assertTrue(meets(segment(p(-1, 0), p(2, 1)), r));
		assertTrue(meets(segment(p(-5, 10), p(15, 10)), r), "touching an edge");
		assertTrue(meets(segment(p(-1, -1), p(0, 0)), r), "touching a corner");
		assertFalse(meets(segment(p(-1, -1), p(-0.5, 20)), r));
		assertFalse(meets(segment(p(11, -5), p(20, 5)), r),
				"bounding boxes overlap, the line separates");
		assertTrue(meets(new LinearGeometry(GeometryType.RAY, p(5, 5), p(1, 0)), r));
		assertFalse(meets(new LinearGeometry(GeometryType.RAY, p(5, 15), p(0, 1)), r));
		assertTrue(meets(new LinearGeometry(GeometryType.RAY, p(5, 15), p(0, -1)), r));
		double s = Math.sqrt(0.5);
		GeometryExportArea behind = GeometryExportArea.explicit(Producer.MANUAL, -3,
				-1, -3, 5, 1);
		assertFalse(meets(new LinearGeometry(GeometryType.RAY, p(0, 0), p(s, s)),
				behind), "the line meets the area only behind the ray origin");
		assertTrue(meets(new LinearGeometry(GeometryType.INFINITE_LINE, p(0, 5),
				p(1, 0)), r));
		assertFalse(meets(new LinearGeometry(GeometryType.INFINITE_LINE, p(0, 15),
				p(1, 0)), r));
		assertTrue(meets(new CircleGeometry(p(10, 10), 3), r));
		assertFalse(meets(new CircleGeometry(p(5, 5), 20), r),
				"a circle enclosing the area is a boundary that does not meet it");
		assertTrue(meets(new CircleGeometry(p(5, 5), 2), r), "inside");
		assertTrue(meets(new CircleGeometry(p(-3, 5), 3), r), "tangent");
		assertFalse(meets(new CircleGeometry(p(-3, 5), 2.5), r));
		assertTrue(meets(new ArcGeometry(p(10, 10), 3, 180, 270), r));
		assertFalse(meets(new ArcGeometry(p(10, 10), 3, 0, 90), r),
				"the arc lies in the outside quadrant");
		assertTrue(meets(new EllipseGeometry(p(0, 0), p(4, 0), 0.5, 0, 2 * Math.PI),
				r));
		assertFalse(meets(new EllipseGeometry(p(5, 5), p(40, 0), 0.75, 0,
				2 * Math.PI), r), "an enclosing ellipse does not meet the area");
		assertFalse(meets(new EllipseGeometry(p(0, 0), p(4, 0), 0.5, Math.PI,
				1.5 * Math.PI), r), "an elliptic arc in the third quadrant");
		assertTrue(meets(new PolylineGeometry(Arrays.asList(p(-2, -2), p(2, -2),
				p(2, 2), p(-2, 2)), true), r));
		assertFalse(meets(new PolylineGeometry(Arrays.asList(p(-20, -20), p(20, -20),
				p(20, 20), p(-20, 20)), true), r), "a polygon around the area");
		assertTrue(meets(new PolylineGeometry(Arrays.asList(p(-5, 20), p(-5, 5),
				p(5, 5)), false), r), "an open polyline crossing by an edge");
		assertFalse(meets(new PolylineGeometry(Arrays.asList(p(-5, 20), p(-5, 5),
				p(5, 30)), false), r), "no edge meets the area");
		assertThrows(IllegalArgumentException.class, () -> ExportAreaParticipation2D
				.meets(new PointGeometry(p(0, 0)),
						GeometryExportArea.visibleViewportFallback()));
	}

	// ---------------------------------------------- T-DXF-AREA preflight

	@Test
	void explicitAreaSelectsWholeSourcesAndReportsTheRestOutside() {
		List<GeoElement> sources = exactScene();
		GeometryExportArea area = GeometryExportArea.explicit(
				Producer.EXPORT_POINTS_AUTOMATIC, 2.5, 6, -1, 1.5, 1);
		GeometryExportContext context = GeometryExportContext.of(UnitState.EMPTY,
				HiddenLayerSet.EMPTY, area);
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, exactRequest(), () -> context);
		List<String> emitted = new ArrayList<>();
		for (GeometryExportModel.Entity entity : preflight.getModel().getEntities()) {
			emitted.add(entity.getLabel());
		}
		assertEquals(Arrays.asList("B", "s", "g"), emitted,
				"whole sources meeting the area, in construction order");
		List<String> outside = new ArrayList<>();
		for (AreaExclusion exclusion : preflight.getModel().getAreaExclusions()) {
			outside.add(exclusion.getLabel());
			assertEquals(IdentityScope.CONSTRUCTION_REVISION,
					exclusion.getIdentityScope());
		}
		assertEquals(Arrays.asList("A", "c", "poly"), outside);
		assertEquals(3, preflight.getOutsideExportAreaCount());
		assertTrue(preflight.isWritable(), "outside the area is not a fidelity error");
		assertEquals(0, preflight.getUnsupportedCount() + preflight.getInvalidCount());
		assertFalse(preflight.isSidecarRequired(),
				"area filtering alone never requires the sidecar");
		String dxf = service.encode(preflight).getDxfText();
		assertTrue(dxf.contains("999\r\nGeoCeDG export area "
				+ "geocedg-export-area-participation-b1/v1: EXPORT_POINTS_AUTOMATIC "
				+ "[2.5, 6.0] x [-1.0, 1.5] of view 1; whole sources and components "
				+ "meeting the closed area; 3 outside the export area, reported, not "
				+ "emitted\r\n"), dxf);
		LinearGeometry s = (LinearGeometry) preflight.getModel().getEntities().get(1)
				.getGeometry();
		assertEquals(0.0, s.getStart().getX(), 0, "the segment is written whole");
		assertEquals(3.0, s.getVector().getX(), 0);

		GeometryExportPreflight selection = service.preflight(
				Arrays.asList(sources.get(0), sources.get(2)),
				SelectionMode.CURRENT_SELECTION, exactRequest(), () -> context);
		assertEquals(1, selection.getModel().getEntities().size());
		assertEquals("s", selection.getModel().getEntities().get(0).getLabel());
		assertEquals(1, selection.getOutsideExportAreaCount(),
				"current selection: selection ∩ area, the rest reported");
	}

	@Test
	void theVisibleViewportFallbackIsNoBoundaryAndKeepsTheBaseBytes() {
		List<GeoElement> sources = exactScene();
		String none = exactDxf(sources, GeometryExportContext.UNSPECIFIED);
		GeometryExportContext fallback = GeometryExportContext.of(UnitState.EMPTY,
				HiddenLayerSet.EMPTY, GeometryExportArea.visibleViewportFallback());
		assertFalse(fallback.hasAreaBoundary());
		assertEquals(none, exactDxf(sources, fallback),
				"VISIBLE_VIEWPORT never filters and never comments");
		assertEquals(GeometryExportArea.visibleViewportFallback(),
				GeometryExportArea.visibleViewportFallback(),
				"the fallback record has no bounds, so the zoom never enters it");
		assertTrue(Double.isNaN(fallback.getArea().getXmin()));
		assertThrows(IllegalArgumentException.class, () -> GeometryExportArea
				.explicit(Producer.VISIBLE_VIEWPORT, 0, 1, 0, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> GeometryExportArea
				.explicit(Producer.MANUAL, 0, 0, 0, 1, 1));
	}

	// ------------------------------- certified Locus V2 / Spline V2 participation

	@Test
	void semanticComponentsAreExcludedOnlyByACertifiedDisjointnessProof() {
		GeoLocusV2 spline = add("S=SplineV2({(0,0),(1,1),(2,0),(3,1)},3)");
		GeoLocusV2 expression = createLocus("(s,s^2)", "{false,{-1,1,true,true}}");
		LocusDefinition2D definition = spline.getSemanticDefinition();
		LocusBranch2D branch = definition.getBranches().get(0);
		LocusInterval2D component = branch.getExistenceStructure()
				.getContinuousValidComponents().get(0).getInterval();
		assertEquals(Status.PROVEN_DISJOINT, CertifiedComponentAreaDisjointness2D
				.prove(definition, branch.getBranchKey(), component, 10, 13, 10, 13,
						CertifiedComponentAreaDisjointness2D.DEFAULT_MAXIMUM_BOXES));
		assertEquals(Status.NOT_PROVEN, CertifiedComponentAreaDisjointness2D
				.prove(definition, branch.getBranchKey(), component, 1, 2, -1, 2,
						CertifiedComponentAreaDisjointness2D.DEFAULT_MAXIMUM_BOXES),
				"a component crossing the area is never proven disjoint");
		assertEquals(Status.NOT_PROVEN, CertifiedComponentAreaDisjointness2D
				.prove(definition, branch.getBranchKey(), component, 3, 4, 1, 2, 1),
				"a proof that does not close within the budget keeps the component");
		LocusDefinition2D expressionDefinition = expression.getSemanticDefinition();
		LocusBranch2D expressionBranch = expressionDefinition.getBranches().get(0);
		Status uncertified = CertifiedComponentAreaDisjointness2D.prove(
				expressionDefinition, expressionBranch.getBranchKey(),
				expressionBranch.getExistenceStructure().getContinuousValidComponents()
						.get(0).getInterval(), 10, 13, 10, 13, 4096);
		assertTrue(uncertified == Status.NO_CERTIFIED_MODEL
				|| uncertified == Status.PROVEN_DISJOINT, uncertified.toString());

		GeometryExportArea far = GeometryExportArea.explicit(Producer.MANUAL, 10, 13,
				10, 13, 1);
		GeometryExportContext context = GeometryExportContext.of(UnitState.EMPTY,
				HiddenLayerSet.EMPTY, far);
		GeometryExportPreflight preflight = service.preflight(
				Collections.singletonList(spline), SelectionMode.CURRENT_SELECTION,
				approximateRequest(), () -> context);
		assertEquals(0, preflight.getModel().getEntities().size());
		assertEquals(1, preflight.getOutsideExportAreaCount());
		assertEquals(0, preflight.getModel().getOutcomes().size());
		AreaExclusion exclusion = preflight.getModel().getAreaExclusions().get(0);
		assertEquals(branch.getBranchKey(),
				exclusion.getComponentAddress().getBranchKey(),
				"the component address is kept");

		GeometryExportArea near = GeometryExportArea.explicit(Producer.MANUAL, 2.9, 4,
				0.9, 2, 1);
		GeometryExportPreflight crossing = service.preflight(
				Collections.singletonList(spline), SelectionMode.CURRENT_SELECTION,
				approximateRequest(), () -> GeometryExportContext.of(UnitState.EMPTY,
						HiddenLayerSet.EMPTY, near));
		assertEquals(1, crossing.getModel().getEntities().size(),
				"a component near or on the boundary participates whole");
		assertEquals(0, crossing.getOutsideExportAreaCount());
		PolylineGeometry whole = (PolylineGeometry) crossing.getModel().getEntities()
				.get(0).getGeometry();
		assertEquals(0, whole.getVertices().get(0).getX(), 1E-9,
				"the approximation is never shortened by the area");
	}

	// --------------------------------------------------------- T-DXF-STALENESS

	@Test
	void unitHiddenLayerAndAreaChangesMakeAPendingPreflightStale() {
		List<GeoElement> sources = exactScene();
		AtomicReference<GeometryExportContext> current = new AtomicReference<>(
				GeometryExportContext.UNSPECIFIED);
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, exactRequest(), current::get);
		assertTrue(preflight.isSourceRevisionCurrent());
		current.set(GeometryExportContext.of(UnitState.of(UnitToken.MM, null, null),
				HiddenLayerSet.EMPTY, null));
		assertFalse(preflight.isSourceRevisionCurrent(), "unit change");
		current.set(GeometryExportContext.UNSPECIFIED);
		assertTrue(preflight.isSourceRevisionCurrent());
		current.set(GeometryExportContext.of(UnitState.EMPTY, HiddenLayerSet.of(2),
				null));
		assertFalse(preflight.isSourceRevisionCurrent(), "hidden-layer change");
		current.set(GeometryExportContext.of(UnitState.EMPTY, HiddenLayerSet.EMPTY,
				GeometryExportArea.explicit(Producer.MANUAL, 0, 1, 0, 1, 1)));
		assertFalse(preflight.isSourceRevisionCurrent(), "area change");
		assertThrows(IllegalStateException.class, () -> service.encode(preflight));

		GeometryExportPreflight document = service.preflight(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, exactRequest());
		assertTrue(document.isSourceRevisionCurrent());
		getConstruction().getUnitSystem().replace(UnitState.of(UnitToken.CM, null,
				null));
		assertFalse(document.isSourceRevisionCurrent(),
				"the default context reads the document unit state again");
	}

	// ------------------------------------------------------ T-SEMANTIC-ADAPTER

	@Test
	void theSemanticAdapterKeepsComponentsGapsOrientationAndRevision() {
		GeoNumeric k = add("k=1");
		GeoLocusV2 locus = createLocus("(s,s^2)",
				"{false,{-2,-k,true,true},{k,2,true,true}}");
		SemanticCurveExportAdapter2D adapter = new SemanticCurveExportAdapter2D();
		GeometryExportRequest request = approximateRequest();
		SourceResult result = adapter.adaptLocus(locus, 0, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "test:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
		assertEquals(2, result.getComponents().size(), "one result per component");
		for (Component component : result.getComponents()) {
			assertTrue(component.isEmitted());
			assertEquals(Fidelity.APPROXIMATE, component.getOutcome().getFidelity());
			assertFalse(((PolylineGeometry) component.getEntity().getGeometry())
					.isClosed(), "gaps are never bridged");
		}
		assertEquals(-1, last(result.getComponents().get(0)).getX(), 0);
		assertEquals(1, first(result.getComponents().get(1)).getX(), 0);
		assertTrue(result.isCurrent());
		SourceResult again = adapter.adaptLocus(locus, 0, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "test:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
		for (int index = 0; index < 2; index++) {
			assertEquals(polyline(result.getComponents().get(index)).getVertices().size(),
					polyline(again.getComponents().get(index)).getVertices().size(),
					"deterministic");
		}
		k.setValue(1.5);
		k.updateCascade();
		assertFalse(result.isCurrent(), "a changed source makes the result stale");

		add("t=0");
		add("P=(cos(t),sin(t))");
		add("E={true,{0,2*pi,true,false}}");
		GeoLocusV2 circle = add("C=LocusV2(P,t,E)");
		SourceResult closed = adapter.adaptLocus(circle, 1, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "test:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
		assertTrue(polyline(closed.getComponents().get(0)).isClosed(),
				"closure only from a full-period certificate");
		assertEquals(SemanticCoverage.COMPLETE,
				closed.getComponents().get(0).getOutcome().getSemanticCoverage());
	}

	@Test
	void theSemanticAdapterNeverReadsTheRenderPath() throws Exception {
		Path source = Paths.get(System.getProperty("user.dir")).getParent()
				.resolve("common/src/main/java/org/geocedg/common/export/"
						+ "SemanticCurveExportAdapter2D.java");
		String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
		assertFalse(text.contains("evaluateForRender"));
		assertFalse(text.contains("LocusRenderCache"));
		assertFalse(text.contains("EuclidianView"));
		assertTrue(text.contains("definition.evaluate(branchKey, parameter"));
	}

	// ----------------------------------------------------- T-EXPORT-RESULT-MODEL

	@Test
	void theResultModelSeparatesLocalAdmissibilityFromGlobalCompleteness() {
		SourceExportOutcome complete = emitted(SemanticCoverage.COMPLETE);
		SourceExportOutcome local = emitted(
				SemanticCoverage.LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED);
		SourceExportOutcome workLimit = failed(Reason.WORK_LIMIT);
		SourceExportOutcome missing = failed(Reason.MISSING_DOMAIN);
		assertEquals(SourceClass.COMPLETE, SemanticExportClassification
				.classifySource(Arrays.asList(complete, complete)));
		assertEquals(SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				SemanticExportClassification.classifySource(Arrays.asList(local)));
		assertEquals(SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				SemanticExportClassification.classifySource(Arrays.asList(complete,
						workLimit)));
		assertEquals(SourceClass.NO_ADMISSIBLE_OUTPUT, SemanticExportClassification
				.classifySource(Arrays.asList(missing)));
		assertEquals(SourceClass.NO_ADMISSIBLE_OUTPUT, SemanticExportClassification
				.classifySource(Collections.<SourceExportOutcome>emptyList()));
		assertTrue(SemanticExportClassification.rejectsExport(
				failed(Reason.TOLERANCE_NOT_ESTABLISHED)));
		assertTrue(SemanticExportClassification.rejectsExport(
				failed(Reason.STALE_SOURCE_REVISION)));
		assertFalse(SemanticExportClassification.rejectsExport(workLimit));
		assertEquals(ExportClass.COMPLETE, SemanticExportClassification.classifyExport(
				Arrays.asList(SourceClass.COMPLETE), false, false));
		assertEquals(ExportClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				SemanticExportClassification.classifyExport(Arrays.asList(
						SourceClass.COMPLETE,
						SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS), false, false));
		assertEquals(ExportClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				SemanticExportClassification.classifyExport(Arrays.asList(
						SourceClass.NO_ADMISSIBLE_OUTPUT), true, false),
				"other admissible content keeps the export");
		assertEquals(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
				SemanticExportClassification.classifyExport(Arrays.asList(
						SourceClass.NO_ADMISSIBLE_OUTPUT), false, false));
		assertEquals(ExportClass.REJECTED_NO_ADMISSIBLE_OUTPUT,
				SemanticExportClassification.classifyExport(Arrays.asList(
						SourceClass.COMPLETE), true, true));
		assertEquals(ExportClass.COMPLETE, SemanticExportClassification.classifyExport(
				Collections.<SourceClass>emptyList(), false, false));
	}

	@Test
	void dxfKeepsTheStrictWritabilityRule() {
		GeoLocusV2 locus = createLocus("(s,s^2)", "{false,{-2,-1,true,true},"
				+ "{1,2,true,true}}");
		GeometryExportRequest budget = GeometryExportRequest.builder(1E-9)
				.maximumEvaluations(8).build();
		GeometryExportPreflight preflight = service.preflight(
				Collections.singletonList(locus), SelectionMode.CURRENT_SELECTION,
				budget);
		assertTrue(preflight.getInvalidCount() > 0);
		assertFalse(preflight.isWritable(),
				"any INVALID component still blocks a DXF (ADR 0014 Decision 6)");
	}

	// ------------------------------------------------------------ physical scale

	@Test
	void thePhysicalScaleFollowsUnitAndDrawingScaleOnly() {
		assertEquals(0.1, PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.MM, UnitToken.M, null), 1, 1), 0);
		assertEquals(0.05, PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.MM, null, null), 1, 2), 0);
		assertEquals(1.0, PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.CM, null, null), 1, 1), 0);
		assertEquals(0.5, PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.CM, UnitToken.MM, null), 1, 2), 0);
		assertEquals(200.0, PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.M, null, null), 2, 1), 0);
		assertEquals(0.0254 * 100 * 1 / 1, PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, null, "in")),
				1, 1), 0);
		assertTrue(Double.isNaN(PhysicalExportScale.centimetresPerUnit(UnitState.EMPTY,
				1, 1)));
		assertEquals(PhysicalExportScale.centimetresPerUnit(
				UnitState.of(UnitToken.CM, null, null), 1, 2),
				PhysicalExportScale.centimetresPerUnit(
						UnitState.of(UnitToken.CM, UnitToken.M, null), 1, 2),
				"presentationUnit is never a scale factor");
		assertThrows(IllegalArgumentException.class, () -> PhysicalExportScale
				.centimetresPerUnit(UnitState.EMPTY, 0, 1));
		assertEquals(3, PhysicalExportScale.deviceExtent(2.5, "w"));
		assertThrows(PhysicalExportLimitException.class,
				() -> PhysicalExportScale.deviceExtent(0.99, "w"));
		assertThrows(PhysicalExportLimitException.class,
				() -> PhysicalExportScale.deviceExtent(3E9, "w"));
		assertThrows(PhysicalExportLimitException.class,
				() -> PhysicalExportScale.deviceExtent(Double.NaN, "w"));
		assertEquals(4000, PhysicalExportScale.emfFrameHundredthsOfMillimetre(4, "w"));
		assertEquals(4001, PhysicalExportScale.emfFrameHundredthsOfMillimetre(4.0006,
				"w"), "nearest 0.01 mm, at most 0.005 mm away");
		assertThrows(PhysicalExportLimitException.class, () -> PhysicalExportScale
				.emfFrameHundredthsOfMillimetre(3E6, "w"), "beyond 2^31 - 1");
		assertThrows(PhysicalExportLimitException.class, () -> PhysicalExportScale
				.emfFrameHundredthsOfMillimetre(0.000001, "w"));
		assertThrows(PhysicalExportLimitException.class, () -> PhysicalExportScale
				.requirePdfPageExtent(1E10, "w"));
		assertEquals(12.5, PhysicalExportScale.requirePdfPageExtent(12.5, "w"), 0);
	}

	// ------------------------------------------------------------------ helpers

	private List<GeoElement> exactScene() {
		List<GeoElement> scene = new ArrayList<>();
		scene.add(add("A=(1,1)"));
		scene.add(add("B=(3,1)"));
		scene.add(add("s=Segment((0,0),(3,1))"));
		scene.add(add("c=Circle((-5,-5),1)"));
		scene.add(add("poly=Polygon((-3,4),(-2,4),(-2,5))"));
		scene.add(add("g=Line((0,0),(1,0))"));
		return scene;
	}

	private String exactDxf(List<GeoElement> sources, GeometryExportContext context) {
		return service.exportDxf(service.createModel(sources,
				SelectionMode.COMPLETE_CONSTRUCTION, context));
	}

	private GeoLocusV2 createLocus(String pointExpression, String domainExpression) {
		add("s=0");
		add("Q=" + pointExpression);
		add("D=" + domainExpression);
		GeoLocusV2 locus = add("L=LocusV2(Q,s,D)");
		assertNotNull(locus);
		return locus;
	}

	private static GeometryExportRequest exactRequest() {
		return GeometryExportRequest.builder(0.001).build();
	}

	private static GeometryExportRequest approximateRequest() {
		return GeometryExportRequest.builder(0.01)
				.allowedGuarantees(EnumSet.of(ApproximationEvidence.Guarantee
						.ESTIMATED_ERROR)).build();
	}

	private static boolean meets(GeometryExportModel.Geometry geometry,
			GeometryExportArea area) {
		return ExportAreaParticipation2D.meets(geometry, area);
	}

	private static LinearGeometry segment(Point2D start, Point2D end) {
		return new LinearGeometry(GeometryType.SEGMENT, start, end);
	}

	private static Point2D p(double x, double y) {
		return new Point2D(x, y);
	}

	private static PolylineGeometry polyline(Component component) {
		return (PolylineGeometry) component.getEntity().getGeometry();
	}

	private static Point2D first(Component component) {
		return polyline(component).getVertices().get(0);
	}

	private static Point2D last(Component component) {
		List<Point2D> vertices = polyline(component).getVertices();
		return vertices.get(vertices.size() - 1);
	}

	private static SourceExportOutcome emitted(SemanticCoverage coverage) {
		return new SourceExportOutcome("source", "LOCUS_V2", "L", 1, true,
				IdentityScope.CONSTRUCTION_REVISION, new ComponentAddress("b", "c"),
				Fidelity.APPROXIMATE, Reason.NONE, "id-" + coverage,
				new ApproximationEvidence(
						ApproximationEvidence.Method.ORIENTED_DYADIC_REFINEMENT, 0.01,
						0.001, ApproximationEvidence.Guarantee.ESTIMATED_ERROR, 10, 2, 3,
						4, 2), null, coverage);
	}

	private static SourceExportOutcome failed(Reason reason) {
		return new SourceExportOutcome("source", "LOCUS_V2", "L", 1, true,
				IdentityScope.CONSTRUCTION_REVISION, new ComponentAddress("b", "f"),
				Fidelity.INVALID, reason, null, null, "failed");
	}

	private static String header(String dxf, String variable) {
		String[] lines = dxf.split("\r\n");
		for (int index = 0; index + 3 < lines.length; index += 2) {
			if ("9".equals(lines[index]) && variable.equals(lines[index + 1])) {
				return lines[index + 3];
			}
		}
		throw new AssertionError("missing " + variable);
	}

	private static List<List<String[]>> entities(String dxf) {
		String[] lines = dxf.split("\r\n");
		List<List<String[]>> result = new ArrayList<>();
		boolean inside = false;
		List<String[]> current = null;
		for (int index = 0; index + 1 < lines.length; index += 2) {
			String code = lines[index];
			String value = lines[index + 1];
			if ("2".equals(code) && "ENTITIES".equals(value)) {
				inside = true;
				continue;
			}
			if (!inside) {
				continue;
			}
			if ("0".equals(code)) {
				if ("ENDSEC".equals(value)) {
					break;
				}
				current = new ArrayList<>();
				result.add(current);
			}
			if (current != null) {
				current.add(new String[] {code, value});
			}
		}
		return result;
	}

	private static String value(List<String[]> entity, int code) {
		for (String[] pair : entity) {
			if (Integer.toString(code).equals(pair[0])) {
				return pair[1];
			}
		}
		return null;
	}
}

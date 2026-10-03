/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.units.UnitDocumentOperations;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-D1 T-INVARIANCE (unit-system v1.0, section 6.1): no unit operation,
 * nor its undo and redo, changes coordinates, numeric geometry, the DAG, the order,
 * identities, Locus V2 or Spline V2 semantics or parameter domains; a pure unit
 * operation updates no geo at all.
 */
class PreG9BR6PlusD1UnitInvarianceTest extends BaseUnitTest {
	private static final UsmDefinition INCH = UsmDefinition.of(0.0254, "inch", "in");

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private void buildCorpus() {
		getKernel().setContinuous(false);
		for (String command : new String[] {"A=(0,0)", "c=Circle(A,2)", "C=Point(c)",
				"f=Line(C,xAxis)", "D=Intersect(f,yAxis)", "L=LocusV2(D,C)",
				"P=(4,1)", "Q=(6,3)", "R=(5,5)", "poly=Polygon(P,Q,R)", "d=Distance(P,Q)",
				"t=Slider(0,1,0.1)", "S=SplineV2({(0,0),(1,2),(3,1),(4,4)},3)",
				"e=Ellipse(A,(3,0),(0,1))", "len=LocusLength(L)"}) {
			add(command);
		}
	}

	/** @return ordered fingerprint of elements, values, order, identities and DAG */
	private Map<String, String> fingerprint() {
		Map<String, String> fingerprint = new LinkedHashMap<>();
		StringBuilder elements = new StringBuilder();
		getConstruction().getConstructionElementsXML(new XMLStringBuilder(elements), false);
		fingerprint.put("elementsXml", elements.toString());
		List<String> order = new ArrayList<>();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			order.add(geo.getLabelSimple());
			fingerprint.put("value:" + geo.getLabelSimple(),
					geo.toValueString(StringTemplate.maxPrecision));
			fingerprint.put("definition:" + geo.getLabelSimple(),
					geo.getDefinition(StringTemplate.xmlTemplate));
			PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(geo);
			fingerprint.put("id:" + geo.getLabelSimple(), String.valueOf(id));
			fingerprint.put("parents:" + geo.getLabelSimple(),
					geo.getParentAlgorithm() == null ? "free"
							: geo.getParentAlgorithm().getDefinition(StringTemplate.xmlTemplate));
		}
		fingerprint.put("order", String.join(",", order));
		return fingerprint;
	}

	private List<UnitState> operations() {
		UnitState mm = UnitState.of(UnitToken.MM, null, null);
		return List.of(mm, mm.withConstructionUnit(UnitToken.M),
				UnitState.of(UnitToken.M, UnitToken.CM, null),
				UnitState.of(UnitToken.M, UnitToken.CM, INCH),
				UnitState.of(UnitToken.USM, UnitToken.CM, INCH),
				UnitState.of(UnitToken.USM, UnitToken.CM, UsmDefinition.of(0.3048, "foot", "ft")),
				UnitState.of(UnitToken.USM, UnitToken.CM, UsmDefinition.of(0.3048, "pie", "p")),
				UnitState.of(UnitToken.CM, UnitToken.USM, UsmDefinition.of(0.3048, "pie", "p")),
				UnitState.of(null, null, UsmDefinition.of(0.3048, "pie", "p")),
				UnitState.EMPTY);
	}

	@Test
	void unitOperationsNeverTouchGeometryAndUpdateNoGeo() {
		buildCorpus();
		Map<String, String> before = fingerprint();
		Map<String, GeoElement> instances = new LinkedHashMap<>();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			instances.put(geo.getLabelSimple(), geo);
		}
		CountingView view = new CountingView();
		getKernel().attach(view);
		try {
			for (UnitState state : operations()) {
				UnitDocumentOperations.commit(getApp(), state);
				assertEquals(state, getConstruction().getUnitSystem().getState());
				assertEquals(before, fingerprint(), "after " + state);
			}
		} finally {
			getKernel().detach(view);
		}
		assertEquals(0, view.updates, "a unit operation updates no geo");
		for (Map.Entry<String, GeoElement> entry : instances.entrySet()) {
			assertSame(entry.getValue(), lookup(entry.getKey()), "same instance");
		}
	}

	@Test
	void undoAndRedoOfUnitOperationsKeepEveryGeometricIdentity() {
		buildCorpus();
		activateUndo();
		getApp().storeUndoInfo();
		Map<String, String> before = fingerprint();
		List<UnitState> operations = operations();
		for (UnitState state : operations) {
			assertTrue(UnitDocumentOperations.commit(getApp(), state) || state.isEmpty());
		}
		for (int i = operations.size() - 1; i >= 0; i--) {
			getKernel().undo();
			assertSameGeometry(before, fingerprint(), "after undo " + i);
		}
		assertTrue(getConstruction().getUnitSystem().getState().isEmpty());
		for (int i = 0; i < operations.size(); i++) {
			getKernel().redo();
			assertSameGeometry(before, fingerprint(), "after redo " + i);
		}
	}

	private static void assertSameGeometry(Map<String, String> expected,
			Map<String, String> actual, String context) {
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			String other = actual.get(entry.getKey());
			// A reload from an undo snapshot re-creates Locus V2 runtimes and advances
			// their runtime revision counter; it is a reload artifact, not a unit effect,
			// and the element XML (compared exactly) carries no revision.
			if (!reloadNeutral(entry.getValue()).equals(reloadNeutral(other))) {
				throw new AssertionError(context + ": " + entry.getKey() + " differs: "
						+ firstDifference(entry.getValue(), String.valueOf(other)));
			}
		}
		assertEquals(expected.keySet(), actual.keySet(), context);
	}

	private static String reloadNeutral(String value) {
		return value == null ? null : value.replaceAll("revision=\\d+", "revision=*");
	}

	private static String firstDifference(String a, String b) {
		int i = 0;
		while (i < a.length() && i < b.length() && a.charAt(i) == b.charAt(i)) {
			i++;
		}
		int from = Math.max(0, i - 80);
		return "[" + a.substring(from, Math.min(a.length(), i + 80)) + "] vs ["
				+ b.substring(from, Math.min(b.length(), i + 80)) + "]";
	}

	@Test
	void classicConfigurationHasTheSameSharedSemantics() {
		AppCommon classic = AppCommonFactory.create(new AppConfigDefault());
		classic.getKernel().getConstruction().getUnitSystem()
				.replace(UnitState.of(UnitToken.MM, null, null));
		assertTrue(classic.getXML().contains("<geocedgUnits version=\"1\" construction=\"mm\"/>"));
		assertTrue(App.class.isInstance(classic));
	}

	/** Counts geo updates reaching a view. */
	private static final class CountingView implements View {
		int updates;

		@Override
		public void add(GeoElement geo) {
			// not counted
		}

		@Override
		public void remove(GeoElement geo) {
			// not counted
		}

		@Override
		public void rename(GeoElement geo) {
			// not counted
		}

		@Override
		public void update(GeoElement geo) {
			updates++;
		}

		@Override
		public void updateVisualStyle(GeoElement geo, GProperty prop) {
			updates++;
		}

		@Override
		public void updateHighlight(GeoElementND geo) {
			// presentation only
		}

		@Override
		public void updateAuxiliaryObject(GeoElement geo) {
			updates++;
		}

		@Override
		public void repaintView() {
			// presentation only
		}

		@Override
		public boolean suggestRepaint() {
			return false;
		}

		@Override
		public void reset() {
			// not counted
		}

		@Override
		public void clearView() {
			// not counted
		}

		@Override
		public void setMode(int mode, ModeSetter m) {
			// not counted
		}

		@Override
		public int getViewID() {
			return 999_001;
		}

		@Override
		public boolean hasFocus() {
			return false;
		}

		@Override
		public void startBatchUpdate() {
			// not counted
		}

		@Override
		public void endBatchUpdate() {
			// not counted
		}

		@Override
		public void updatePreviewFromInputBar(GeoElement[] geos) {
			// not counted
		}
	}
}

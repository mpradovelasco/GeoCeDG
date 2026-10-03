/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.layers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.layers.LayerDomain;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.geogebra3D.euclidian3D.draw.Drawable3D;
import org.geogebra.common.gui.dialog.options.model.LayerModel;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.AppCommon3D;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.properties.impl.objects.LayerProperty;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-A-2 T-DOMAIN-CONFIG, T-DOMAIN-ROUTES, T-DOMAIN-CONSUMERS (shared),
 * T-CLASSIC (shared) and T-3D-CODING: the GeoCeDG layer domain 0..99 read from the
 * product configuration, and the inherited 0..9 domain of every other configuration.
 */
class PreG9BR6PlusA2LayerDomainTest {
	private static final int[] PROBE = {-5, -1, 0, 5, 9, 10, 50, 99, 100, 1000};

	private static AppCommon geocedg() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private static AppCommon classic() {
		return AppCommonFactory.create();
	}

	private static GeoElement add(AppCommon app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommandNoExceptions(command, false);
		assertNotNull(result, command);
		return result.length == 0 ? null : result[0].toGeoElement();
	}

	private static void run(AppCommon app, String command) {
		app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptions(command, false);
	}

	/** Loads points P0..P9 written with the probe layers through a clearing parse. */
	private static List<Integer> readBack(AppCommon app) throws Exception {
		for (int i = 0; i < PROBE.length; i++) {
			add(app, "P" + i + "=(" + i + ",0)");
		}
		String xml = app.getXML();
		for (int i = 0; i < PROBE.length; i++) {
			xml = xml.replaceFirst("(?s)(<element type=\"point\" label=\"P" + i
					+ "\">.*?)<layer val=\"\\d+\"/>", "$1<layer val=\"" + PROBE[i] + "\"/>");
		}
		app.getXMLio().processXMLString(xml, true, false);
		List<Integer> layers = new ArrayList<>();
		for (int i = 0; i < PROBE.length; i++) {
			layers.add(app.getKernel().lookupLabel("P" + i).getLayer());
		}
		return layers;
	}

	private static List<Integer> written(String xml) {
		List<Integer> layers = new ArrayList<>();
		Matcher m = Pattern.compile("(?s)<element type=\"point\" label=\"P\\d\">.*?"
				+ "<layer val=\"(-?\\d+)\"/>").matcher(xml);
		while (m.find()) {
			layers.add(Integer.parseInt(m.group(1)));
		}
		return layers;
	}

	// ------------------------------------------------------------ T-DOMAIN-CONFIG

	@Test
	void theDomainIsTheConfiguredProductBound() {
		assertEquals(EuclidianStyleConstants.MAX_LAYERS, new AppConfigDefault().getMaxLayer());
		assertEquals(9, EuclidianStyleConstants.MAX_LAYERS, "the inherited constant is kept");
		assertEquals(99, new AppConfigGeoCeDG().getMaxLayer());
		assertEquals(AppConfigGeoCeDG.L_MAX, new AppConfigGeoCeDG(false).getMaxLayer());
		assertFalse(new AppConfigDefault().persistsDocumentHiddenLayers());
		assertTrue(new AppConfigGeoCeDG().persistsDocumentHiddenLayers());
		assertEquals(9, LayerDomain.maxLayer(null), "a detached element keeps the inherited");
		assertEquals(9, LayerDomain.maxLayer(classic()));
		assertEquals(99, LayerDomain.maxLayer(geocedg()));
		assertEquals(0, LayerDomain.clamp(-3, 99));
		assertEquals(42, LayerDomain.clamp(42, 99));
		assertEquals(99, LayerDomain.clamp(250, 99));
		assertEquals(9, LayerDomain.clamp(42, 9));
	}

	// ------------------------------------------------------------ T-DOMAIN-ROUTES

	@Test
	void geocedgKeepsLayersTenFiftyAndNinetyNineOnEveryRoute() throws Exception {
		AppCommon app = geocedg();
		GeoElement a = add(app, "A=(1,1)");
		int[] expected = {0, 0, 0, 5, 9, 10, 50, 99, 99, 99};
		for (int i = 0; i < PROBE.length; i++) {
			a.setLayer(PROBE[i]);
			assertEquals(expected[i], a.getLayer(), "setLayer(" + PROBE[i] + ")");
		}
		for (int layer : new int[] {10, 50, 99}) {
			a.setLayer(0);
			run(app, "SetLayer(A," + layer + ")");
			assertEquals(layer, a.getLayer(), "SetLayer");
			a.setLayer(0);
			app.getGgbApi().setLayer("A", layer);
			assertEquals(layer, a.getLayer(), "API");
			assertEquals(layer, app.getGgbApi().getLayer("A"));
		}
		a.setLayer(0);
		run(app, "SetLayer(A,150)");
		assertEquals(99, a.getLayer(), "DQ-A2-10: clamped to the product maximum");

		assertEquals(List.of(0, 0, 0, 5, 9, 10, 50, 99, 99, 99), readBack(geocedg()),
				"the XML reader keeps 10, 50 and 99 and clamps outside 0..99");
		AppCommon reread = geocedg();
		readBack(reread);
		assertEquals(99, reread.getMaxLayerUsed());
		assertEquals(List.of(0, 0, 0, 5, 9, 10, 50, 99, 99, 99), written(reread.getXML()),
				"and writes them back unchanged");
	}

	// ------------------------------------------------------------------ T-CLASSIC

	@Test
	void classicStaysClampedToZeroToNineOnEveryRoute() throws Exception {
		AppCommon app = classic();
		GeoElement a = add(app, "A=(1,1)");
		int[] expected = {0, 0, 0, 5, 9, 9, 9, 9, 9, 9};
		for (int i = 0; i < PROBE.length; i++) {
			a.setLayer(PROBE[i]);
			assertEquals(expected[i], a.getLayer(), "setLayer(" + PROBE[i] + ")");
		}
		a.setLayer(0);
		run(app, "SetLayer(A,50)");
		assertEquals(9, a.getLayer());
		a.setLayer(0);
		app.getGgbApi().setLayer("A", 50);
		assertEquals(9, a.getLayer());
		assertEquals(List.of(0, 0, 0, 5, 9, 9, 9, 9, 9, 9), readBack(classic()));
		AppCommon reread = classic();
		readBack(reread);
		assertEquals(9, reread.getMaxLayerUsed());
		assertEquals(List.of(0, 0, 0, 5, 9, 9, 9, 9, 9, 9), written(reread.getXML()));
	}

	@Test
	void layerScriptingOutsideTheDomainIsANoOpAndInsideItWritesObjectVisibility() {
		AppCommon geocedg = geocedg();
		GeoElement a = add(geocedg, "A=(1,1)");
		a.setLayer(42);
		run(geocedg, "HideLayer(42)");
		assertFalse(a.isEuclidianVisible(), "DQ-A2-8: object visibility, in the GeoCeDG range");
		run(geocedg, "ShowLayer(42)");
		assertTrue(a.isEuclidianVisible());
		geocedg.getGgbApi().setLayerVisible(42, false);
		assertFalse(a.isEuclidianVisible());
		geocedg.getGgbApi().setLayerVisible(42, true);
		run(geocedg, "HideLayer(100)");
		geocedg.getGgbApi().setLayerVisible(100, false);
		assertTrue(a.isEuclidianVisible(), "outside 0..99 nothing happens");

		AppCommon classic = classic();
		GeoElement b = add(classic, "B=(1,1)");
		b.setLayer(9);
		run(classic, "HideLayer(42)");
		classic.getGgbApi().setLayerVisible(42, false);
		assertTrue(b.isEuclidianVisible(), "Classic keeps 0..9");
		run(classic, "HideLayer(9)");
		assertFalse(b.isEuclidianVisible());
	}

	// --------------------------------------------------------- T-DOMAIN-CONSUMERS

	@Test
	void theDefaultLayerRuleIsACapOnTheHighestUsedLayerNeverADefault() {
		AppCommon app = geocedg();
		GeoElement a = add(app, "A=(1,1)");
		assertEquals(0, a.getLayer(), "a new GeoCeDG document is not on layer 98");
		assertEquals(0, add(app, "B=(2,2)").getLayer());
		a.setLayer(50);
		assertEquals(50, app.getMaxLayerUsed());
		assertEquals(50, add(app, "C=(3,3)").getLayer(), "min(98, 50)");
		a.setLayer(99);
		assertEquals(98, add(app, "D=(4,4)").getLayer(),
				"min(98, 99): the top layer stays reserved");

		AppCommon host = classic();
		GeoElement h = add(host, "A=(1,1)");
		assertEquals(0, h.getLayer());
		h.setLayer(9);
		assertEquals(8, add(host, "B=(2,2)").getLayer(), "Classic keeps min(8, maxLayerUsed)");
	}

	@Test
	void listAndPolyhedronPropagationKeepTheProductDomain() {
		AppCommon app = geocedg();
		add(app, "n=2");
		GeoList list = (GeoList) add(app, "L=Sequence((k,k),k,1,n)");
		list.setLayer(60);
		assertEquals(60, list.getLayer());
		run(app, "SetValue(n,4)");
		assertEquals(4, list.size());
		assertEquals(60, list.get(3).getLayer(), "an added element takes the list layer");

		AppCommon3D space = AppCommonFactory.create3D(new AppConfigGeoCeDG(true));
		GeoElement cube = add(space, "c=Cube((0,0,0),(1,0,0))");
		cube.setLayer(70);
		assertEquals(70, cube.getLayer());
		assertFalse(faceAndEdgeLayers(space).isEmpty());
		assertTrue(faceAndEdgeLayers(space).stream().allMatch(layer -> layer == 70),
				"faces and edges follow the polyhedron");

		AppCommon3D host = AppCommonFactory.create3D();
		GeoElement hostCube = add(host, "c=Cube((0,0,0),(1,0,0))");
		hostCube.setLayer(70);
		assertEquals(9, hostCube.getLayer());
		assertTrue(faceAndEdgeLayers(host).stream().allMatch(layer -> layer == 9),
				"Classic keeps 0..9");
	}

	private static List<Integer> faceAndEdgeLayers(AppCommon app) {
		List<Integer> layers = new ArrayList<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.isGeoPolygon() || geo.isGeoSegment()) {
				layers.add(geo.getLayer());
			}
		}
		return layers;
	}

	@Test
	void layerPickersExposeTheConfiguredDomain() throws Exception {
		AppCommon app = geocedg();
		GeoElement a = add(app, "A=(1,1)");
		LayerModel model = new LayerModel(app);
		List<String> choices = model.getChoices(app.getLocalization());
		assertEquals(100, choices.size());
		assertEquals(" 99", choices.get(99), "the index is the layer");
		LayerProperty property = new LayerProperty(app.getLocalization(), a);
		assertEquals(100, property.getValues().size());
		assertEquals(99, property.getValues().get(99));
		model.setGeos(new Object[] {a});
		for (int layer : new int[] {10, 50, 99}) {
			assertTrue(model.applyChanges(layer));
			assertEquals(layer, a.getLayer(), "the Properties route keeps " + layer);
			assertEquals(layer, model.getValueAt(0));
		}

		AppCommon host = classic();
		GeoElement b = add(host, "B=(1,1)");
		assertEquals(10, new LayerModel(host).getChoices(host.getLocalization()).size());
		assertEquals(10, new LayerProperty(host.getLocalization(), b).getValues().size());
	}

	// --------------------------------------------------------------- T-3D-CODING

	@Test
	void theRendererAdapterClampsOnlyItsInputNeverTheModelLayer() {
		assertEquals(0, Drawable3D.renderCodingLayer(0));
		assertEquals(9, Drawable3D.renderCodingLayer(9));
		assertEquals(9, Drawable3D.renderCodingLayer(10));
		assertEquals(9, Drawable3D.renderCodingLayer(99));
		AppCommon app = geocedg();
		GeoElement a = add(app, "A=(1,1)");
		a.setLayer(50);
		int coded = Drawable3D.renderCodingLayer(a.getLayer());
		assertEquals(9, coded);
		assertEquals(50, a.getLayer(), "the model layer is unchanged");
		assertEquals(50, app.getGgbApi().getLayer("A"));
		assertTrue(app.getXML().contains("<layer val=\"50\"/>"));
	}
}

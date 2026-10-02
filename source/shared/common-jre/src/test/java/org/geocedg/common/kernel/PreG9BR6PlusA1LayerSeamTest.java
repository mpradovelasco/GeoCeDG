/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.DrawableList;
import org.geogebra.common.factories.AwtFactoryCommon;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.test.LocalizationCommonUTF;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-A-1 shared seams: the host defaults reproduce the upstream
 * layer rule and presentation, and every creation route asks the application
 * seam with the owning construction.
 */
class PreG9BR6PlusA1LayerSeamTest {

	/** A product-like host whose seam answers a fixed layer and records its calls. */
	private static final class LayerApp extends AppCommon {
		// No field initializers: the host constructor already creates objects.
		private Integer requested;
		private List<Construction> asked;

		LayerApp() {
			super(new LocalizationCommonUTF(2), new AwtFactoryCommon(),
					new AppConfigGeoCeDG(true));
		}

		@Override
		public int getLayerForNewObject(Construction construction, int upstreamLayer) {
			if (asked == null) {
				asked = new ArrayList<>();
			}
			asked.add(construction);
			return requested == null ? upstreamLayer : requested;
		}
	}

	private static GeoElement add(AppCommon app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		return result[0].toGeoElement();
	}

	// ------------------------------------------------------------------ T-CLASSIC

	@Test
	void hostSeamsReturnTheUpstreamValueAndShowEveryLayer() {
		AppCommon app = AppCommonFactory.create();
		Construction construction = app.getKernel().getConstruction();
		for (int layer = 0; layer <= 9; layer++) {
			assertEquals(layer, app.getLayerForNewObject(construction, layer));
			assertTrue(app.isLayerShown(layer));
		}
	}

	@Test
	void hostCreationKeepsTheUpstreamTopLayerRule() {
		AppCommon app = AppCommonFactory.create();
		GeoElement a = add(app, "A=(1,1)");
		a.setLayer(6);
		int expected = Math.min(8, app.getMaxLayerUsed());
		assertEquals(expected, add(app, "B=(2,2)").getLayer());
		a.setLayer(9);
		assertEquals(8, add(app, "C=(3,3)").getLayer(), "layer 9 stays reserved");
	}

	@Test
	void hostDrawAllDrawsWhatThePredicateFormDrawsWithAnAdmittingPredicate() {
		AppCommon app = AppCommonFactory.create();
		GeoElement a = add(app, "A=(1,1)");
		Drawable drawable = mock(Drawable.class);
		when(drawable.getGeoElement()).thenReturn(a);
		DrawableList list = new DrawableList(app.getGeoPriorityComparator());
		list.add(drawable);
		GGraphics2D graphics = mock(GGraphics2D.class);
		list.drawAll(graphics);
		verify(drawable, times(1)).draw(graphics);
		list.drawAll(graphics, d -> true);
		verify(drawable, times(2)).draw(graphics);
		list.drawAll(graphics, d -> false);
		verify(drawable, times(2)).draw(graphics);
	}

	// --------------------------------------------------------- seams are consulted

	@Test
	void constructionDefaultsAskTheSeamWithTheOwningConstruction() {
		LayerApp app = new LayerApp();
		app.requested = 4;
		app.asked = new ArrayList<>();
		GeoElement point = add(app, "A=(1,1)");
		assertEquals(4, point.getLayer());
		assertEquals(4, add(app, "c=Circle(A,1)").getLayer());
		assertTrue(app.asked.contains(app.getKernel().getConstruction()));
	}

	@Test
	void macroOutputsAskTheSeam() {
		LayerApp app = new LayerApp();
		GeoElement a = add(app, "A=(1,1)");
		GeoElement b = add(app, "B=(2,2)");
		GeoElement f = add(app, "f=Line(A,B)");
		ToolCreationDialogModel builder = new ToolCreationDialogModel(app, () -> {
			// no dialog to update
		});
		builder.addToInput(a);
		builder.addToInput(b);
		builder.addToOutput(f);
		builder.createTool();
		builder.finish(app, "SeamLine", "SeamLine", "two points", false, null);
		app.requested = 7;
		app.asked = new ArrayList<>();
		assertEquals(7, add(app, "g=SeamLine((1,3),(2,3))").getLayer());
		Construction document = app.getKernel().getConstruction();
		assertTrue(app.asked.stream().anyMatch(c -> c != document),
				"the macro construction is passed as itself, never as the document");
	}

	@Test
	void locusV2FamilyOutputsAskTheSeam() {
		LayerApp app = new LayerApp();
		app.requested = 5;
		add(app, "s=0");
		add(app, "Q=(s,0)");
		add(app, "D={false,{-2,2,true,true}}");
		GeoElement locus = add(app, "L=LocusV2(Q,s,D)");
		assertInstanceOf(GeoLocusV2.class, locus);
		assertEquals(5, locus.getLayer());
		add(app, "A=(0,0)");
		add(app, "B=(1,1)");
		add(app, "C=(2,0)");
		add(app, "E=(3,1)");
		GeoElement spline = add(app, "S=SplineV2({A,B,C,E},3)");
		assertEquals(5, spline.getLayer());
	}

	@Test
	void anUpstreamAnswerLeavesLocusV2WhereTheHostPutsIt() {
		LayerApp app = new LayerApp();
		add(app, "s=0");
		add(app, "Q=(s,0)");
		add(app, "D={false,{-2,2,true,true}}");
		GeoElement locus = add(app, "L=LocusV2(Q,s,D)");
		assertEquals(0, locus.getLayer(), "the host value of a V2 output is unchanged");
		LayerApp other = new LayerApp();
		assertNotSame(app.getKernel().getConstruction(), other.getKernel().getConstruction());
		assertSame(app, locus.getKernel().getApplication());
	}
}

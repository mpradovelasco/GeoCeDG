/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.euclidian.draw.DrawInputBox;
import org.geogebra.common.euclidian.draw.DrawSlider;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Checks live Graphics 1 presentation against the native drawable and hit paths. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class GeoCeDGControlLayoutDesktopTest {
	@Test
	void panAndAnisotropicZoomMoveOnlyTheWorldOrigin() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			GeoNumeric slider = (GeoNumeric) absolute(app, "s=Slider(0,10,1)");
			GeoElement field = absolute(app, "f=InputBox(s)");
			GeoElement button = absolute(app, "b=Button()");
			GeoBoolean checkbox = (GeoBoolean) absolute(app, "q=Checkbox()");
			checkbox.setCheckboxFixed(false);
			Panel world = layout.addPanel(Anchor.WORLD, 2, 1, 100, 80, true);
			Panel fixed = layout.addPanel(Anchor.FIXED, 40, 60, 100, 80, true);
			layout.addSlot(world.id(), slider, 4, 15);
			layout.addSlot(world.id(), field, 5, 32);
			layout.addSlot(fixed.id(), button, 12, 31);
			layout.addSlot(fixed.id(), checkbox, 30, 58);

			view.setCoordSystem(200, 210, 50, 40);
			var sliderDrawable = (DrawSlider) view.getDrawableFor(slider);
			assertNotNull(sliderDrawable);
			assertPosition(304, 185, sliderDrawable.getSliderLocation());
			assertPosition(305, 202, view.getControlLayoutPosition(field));
			var buttonPosition = view.getControlLayoutPosition(button);
			var checkboxPosition = view.getControlLayoutPosition(checkbox);
			view.setCoordSystem(280, 250, 22, 60);
			assertPosition(328, 205, sliderDrawable.getSliderLocation());
			assertPosition(329, 222, view.getControlLayoutPosition(field));
			assertPosition(buttonPosition.x, buttonPosition.y, view.getControlLayoutPosition(button));
			assertPosition(checkboxPosition.x, checkboxPosition.y,
					view.getControlLayoutPosition(checkbox));
			assertEquals(87, slider.getAbsoluteScreenLocX());
			assertEquals(114, slider.getAbsoluteScreenLocY());
			app.setExporting(ExportType.PNG, 1);
			try {
				assertNull(view.getControlLayoutPosition(slider));
				assertPosition(87, 114, sliderDrawable.getSliderLocation());
				assertNull(view.getControlLayoutPosition(field));
			} finally {
				app.setExporting(ExportType.NONE, 1);
			}
		});
	}

	@Test
	void panelLayerAndStepFilterHitsWithoutChangingTheControl() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			GeoBoolean q = (GeoBoolean) absolute(app, "q=Checkbox()");
			q.setCheckboxFixed(false);
			q.setLayer(3);
			must(app, "n=7");
			GeoElement field = absolute(app, "f=InputBox(n)");
			Panel panel = layout.addPanel(Anchor.FIXED, 40, 60, 200, 90, true);
			layout.addSlot(panel.id(), q, 20, 30);
			layout.addSlot(panel.id(), field, 120, 45);
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			assertTrue(view.isControlLayoutVisible(q));
			GPoint2D at = view.getControlLayoutPosition(q);
			var hit = new GPoint((int) at.x + 10, (int) at.y + 10);
			view.setHits(hit, PointerEventType.MOUSE);
			assertTrue(view.getHits().contains(q));
			click(view, hit);
			assertFalse(q.getBoolean());
			click(view, hit);
			assertTrue(q.getBoolean());
			app.getLayerWorkspace().setLayerHidden(3, true);
			view.setHits(hit, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(q));
			app.getLayerWorkspace().setLayerHidden(3, false);

			layout.updatePanel(new Panel(panel.id(), panel.anchor(), panel.x(), panel.y(),
					panel.width(), panel.height(), false));
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			view.setHits(hit, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(q));
			layout.updatePanel(panel);
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			double fieldX = drawnFieldX(view, field);
			int step = q.getConstruction().getStep();
			q.getConstruction().setStep(-1);
			assertFalse(view.isControlLayoutVisible(q));
			q.getConstruction().setStep(step);
			assertTrue(view.isControlLayoutVisible(q));
			assertTrue(view.isControlLayoutVisible(field));
			assertEquals(fieldX, drawnFieldX(view, field), 1);
			assertTrue(q.getBoolean());
			assertEquals(3, q.getLayer());
			assertEquals(87, q.getAbsoluteScreenLocX());
		});
	}

	@Test
	void panelRefreshVisitsOnlyItsSlotsAtTheDocumentLimit() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var kernel = app.getKernel();
			var cons = kernel.getConstruction();
			var layout = layout(app);
			kernel.setNotifyViewsActive(false);
			try {
				for (int i = 0; i < 1000; i++) {
					GeoPoint unrelated = new GeoPoint(cons, i, i + 1, 1);
					unrelated.setEuclidianVisible(false);
					unrelated.setAuxiliaryObject(true);
					unrelated.setLoadedLabel("P_{" + i + "}");
				}
				Panel first = layout.addPanel(Anchor.FIXED, 20, 20, 200, 200, true);
				Panel second = layout.addPanel(Anchor.FIXED, 240, 20, 200, 200, true);
				for (int i = 0; i < 256; i++) {
					GeoBoolean q = new GeoBoolean(cons, true);
					q.setCheckboxFixed(false);
					q.setEuclidianVisible(true);
					q.setLoadedLabel("q_{" + i + "}");
					layout.addSlot(i < 12 ? first.id() : second.id(), q, 0, 30 + i * 20);
					view.add(q);
				}
				assertEquals(1256, cons.getGeoSetConstructionOrder().size());
				assertEquals(12, view.controlLayoutWorkspace().refreshPanel(first.id()));
				assertEquals(244, view.controlLayoutWorkspace().refreshPanel(second.id()));
			} finally {
				cons.clearConstruction();
				kernel.setNotifyViewsActive(true);
			}
		});
	}

	private static GeoCeDGEuclidianView view(AppGeoCeDG app) {
		var view = (GeoCeDGEuclidianView) app.getEuclidianView1();
		view.getJPanel().setSize(800, 600);
		return view;
	}

	private static ControlLayout layout(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getControlLayout();
	}

	private static GeoElement absolute(AppGeoCeDG app, String command) {
		GeoElement geo = must(app, command);
		((AbsoluteScreenLocateable) geo).setAbsoluteScreenLoc(87, 114);
		geo.setEuclidianVisible(true);
		geo.updateRepaint();
		return geo;
	}

	private static void assertPosition(double x, double y, GPoint2D actual) {
		assertNotNull(actual);
		assertEquals(x, actual.x, 0.01);
		assertEquals(y, actual.y, 0.01);
	}

	private static void click(GeoCeDGEuclidianView view, GPoint point) {
		var controller = (GeoCeDGEuclidianController) view.getEuclidianController();
		controller.mousePressed(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_PRESSED,
				0, MouseEvent.BUTTON1_DOWN_MASK, point.x, point.y, 1, false, MouseEvent.BUTTON1));
		controller.mouseReleased(new MouseEvent(view.getJPanel(), MouseEvent.MOUSE_RELEASED,
				1, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
	}

	private static double drawnFieldX(GeoCeDGEuclidianView view, GeoElement field) {
		var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		try {
			view.drawObjects(new GGraphics2DD(graphics));
			var drawable = (DrawInputBox) view.getDrawableFor(field);
			assertNotNull(drawable);
			return drawable.getInputFieldBounds().getX();
		} finally {
			graphics.dispose();
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipInputStream;

import javax.swing.JMenuItem;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.euclidian.draw.DrawInputBox;
import org.geogebra.common.euclidian.draw.DrawSlider;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
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
	void overlappingPanelsCompositeBackToFront() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			Panel rear = layout.addPanel(Anchor.FIXED, 50, 50, 180, 155, true,
					"Rear", 0x5599dd, 65);
			GeoBoolean check = checkbox(app);
			layout.addSlot(rear.id(), check, 30, 70);
			view.controlLayoutWorkspace().refreshPanel(rear.id());
			GPoint sample = controlHit(view, check);
			Color original = sample(view, sample);
			view.setHits(sample, PointerEventType.MOUSE);
			assertTrue(view.getHits().contains(check));

			Panel middle = layout.addPanel(Anchor.FIXED, 65, 72, 150, 110, true,
					"Middle", 0xffaa22, ControlLayout.MIN_OPACITY);
			assertComposite(original, middle, sample(view, sample));
			Color twice = sample(view, sample);
			Panel front = layout.addPanel(Anchor.FIXED, 78, 87, 145, 115, true,
					"Front", 0x129943, ControlLayout.MAX_OPACITY);
			assertComposite(twice, front, sample(view, sample));
			assertSame(front, view.controlLayoutWorkspace().panelAt(sample.x, sample.y));
			view.setHits(sample, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(check));
			click(view, sample);
			assertTrue(check.getBoolean());
			layout.removePanel(front.id());
			layout.removePanel(middle.id());
			click(view, sample);
			assertFalse(check.getBoolean());
		});
	}

	@Test
	void detachMovesTheControlBehindItsOldPanel() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			Panel panel = layout.addPanel(Anchor.FIXED, 55, 50, 200, 160, true,
					"Panel", 0xcc5500, 70);
			GeoBoolean check = checkbox(app);
			layout.addSlot(panel.id(), check, 32, 64);
			view.controlLayoutWorkspace().refreshPanel(panel.id());
			GPoint point = controlHit(view, check);
			Color grouped = sample(view, point);
			layout.removeSlot(check);
			check.updateRepaint();
			assertNull(view.getControlLayoutPosition(check));
			assertComposite(grouped, panel, sample(view, point));
			view.setHits(point, PointerEventType.MOUSE);
			assertFalse(view.getHits().contains(check));
			click(view, point);
			assertTrue(check.getBoolean());
			layout.removePanel(panel.id());
			view.setHits(point, PointerEventType.MOUSE);
			assertTrue(view.getHits().contains(check));
			click(view, point);
			assertFalse(check.getBoolean());
		});
	}

	@Test
	void contextResolvesTheFrontAccessiblePanelWithoutChangingTheLayout() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			var layout = layout(app);
			var workspace = view.controlLayoutWorkspace();
			Panel rear = layout.addPanel(Anchor.FIXED, 40, 40, 150, 120, true);
			Panel front = layout.addPanel(Anchor.FIXED, 100, 70, 150, 120, true);
			var before = layout.panels();
			assertSame(front, workspace.panelAt(130, 110));
			assertSame(rear, workspace.panelAt(60, 85));
			assertNull(workspace.panelAt(310, 200));
			var menu = workspace.panelContextMenu(front);
			assertEquals(1, menu.getComponentCount());
			assertTrue(menu.getComponent(0) instanceof JMenuItem);
			assertEquals(before, layout.panels());
		});
	}

	@Test
	void longUnicodeHeaderFitsWithoutSplittingCharacters() throws Exception {
		onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGEuclidianView view = view(app);
			String name = "Δ𐐷e\u0301".repeat(12);
			Panel panel = layout(app).addPanel(Anchor.FIXED, 50, 50, 85, 90, true,
					name, 0x51aa29, 65);
			var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
			var graphics = image.createGraphics();
			try {
				var surface = new GGraphics2DD(graphics);
				var workspace = view.controlLayoutWorkspace();
				String fitted = workspace.fittedName(surface, panel.name(), 60);
				assertTrue(fitted.endsWith("…"));
				assertTrue(fitted.length() < name.length());
				assertTrue(fitted.codePoints().noneMatch(cp -> cp >= 0xd800 && cp <= 0xdfff));
				workspace.paint(surface);
				var insets = view.getSafeAreaInsets();
				assertEquals(0x365b7b,
						image.getRGB(insets.getLeft() + 52, insets.getTop() + 52) & 0xffffff);
			} finally {
				graphics.dispose();
			}
		});
	}

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
			assertFieldEnclosureAfterUndoAndThumbnail();
			return null;
		});
	}

	private static void assertFieldEnclosureAfterUndoAndThumbnail() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGEuclidianView view = view(app);
		must(app, "n=7");
		GeoInputBox field = (GeoInputBox) absolute(app, "f=InputBox(n)");
		field.setLabelVisible(true);
		var layout = layout(app);
		Panel panel = layout.addPanel(Anchor.FIXED, 330, 145, 60, 80, true);
		var slot = layout.addSlot(panel.id(), field, 18, 43);
		view.controlLayoutWorkspace().refreshPanel(panel.id());
		drawnFieldX(view, field);
		var drawable = (DrawInputBox) view.getDrawableFor(field);
		var widget = drawable.getInputFieldBounds();
		int right = (int) Math.ceil(Math.max(widget.getMaxX(), drawable.getBounds().getMaxX()));
		var fill = new GPoint(right + 3, (int) (widget.getY() + widget.getHeight() / 2));
		GPoint2D fieldOrigin = view.getControlLayoutPosition(field);
		assertTrue(fill.x > fieldOrigin.x - slot.dx() + panel.width());
		Color expectedFill = sample(view, fill);
		assertComposite(Color.WHITE, panel, expectedFill);
		baseline(app);
		layout.updatePanel(panel.withStyle("Field", 0x265a90, 70));
		store(app);
		undo(app).undo();

		GeoInputBox restored = (GeoInputBox) app.getKernel().lookupLabel("f");
		assertNotNull(restored);
		assertNotSame(field, restored);
		Panel restoredPanel = layout.panel(panel.id());
		assertEquals(panel, restoredPanel);
		Color firstPaint = sample(view, fill);
		assertSame(restoredPanel, view.controlLayoutWorkspace().panelAt(fill.x, fill.y));

		var archive = new ByteArrayOutputStream();
		app.getXMLio().writeGeoGebraFile(archive, true);
		try (var entries = new ZipInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
			boolean thumbnail = false;
			for (var entry = entries.getNextEntry(); entry != null; entry = entries.getNextEntry()) {
				thumbnail |= MyXMLio.XML_FILE_THUMBNAIL.equals(entry.getName());
			}
			assertTrue(thumbnail, "the native writer rendered a thumbnail");
		}
		assertFalse(app.isExporting());
		Color paintAfterThumbnail = sample(view, fill);
		assertAll(() -> assertEquals(expectedFill, firstPaint, "first paint after Undo"),
				() -> assertEquals(expectedFill, paintAfterThumbnail, "first paint after thumbnail"));
		assertEquals(87, restored.getAbsoluteScreenLocX());
		assertTrue(undo(app).redoPossible());
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

	private static GeoBoolean checkbox(AppGeoCeDG app) {
		GeoBoolean check = (GeoBoolean) absolute(app, "q=Checkbox()");
		check.setCheckboxFixed(false);
		return check;
	}

	private static GPoint controlHit(GeoCeDGEuclidianView view, GeoElement control) {
		GPoint2D at = view.getControlLayoutPosition(control);
		return new GPoint((int) at.x + 10, (int) at.y + 10);
	}

	private static Color sample(GeoCeDGEuclidianView view, GPoint point) {
		var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			view.drawObjects(new GGraphics2DD(graphics));
			return new Color(image.getRGB(point.x, point.y));
		} finally {
			graphics.dispose();
		}
	}

	private static void assertComposite(Color behind, Panel front, Color actual) {
		var tint = new Color(front.rgb());
		double alpha = Math.round(front.opacity() * 255 / 100.0) / 255.0;
		assertEquals(behind.getRed() * (1 - alpha) + tint.getRed() * alpha, actual.getRed(), 1);
		assertEquals(behind.getGreen() * (1 - alpha) + tint.getGreen() * alpha,
				actual.getGreen(), 1);
		assertEquals(behind.getBlue() * (1 - alpha) + tint.getBlue() * alpha, actual.getBlue(), 1);
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

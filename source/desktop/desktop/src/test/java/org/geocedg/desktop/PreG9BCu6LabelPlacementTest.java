/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.DrawableND;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.MoveMode;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** M03 reaches the normal label-drag path and the existing XML/history codec. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BCu6LabelPlacementTest {
	@Test
	void extendedOffsetsRemainHitTestableAndSurviveXmlHistory() throws Exception {
		AppGeoCeDG app = onEdt(() -> {
			AppGeoCeDG host = G9U1TestApp.create();
			GeoElement segment = segment(host);
			baseline(host);
			dragLabel(host.getEuclidianView1(), segment, 190, -65);
			assertEquals(190, segment.labelOffsetX);
			assertEquals(-65, segment.labelOffsetY);
			return host;
		});
		awaitQuiescent(app);
		onEdt(() -> {
			String xml = app.getXML();
			app.getKernel().undo();
			assertEquals(0, G9U1TestApp.lookup(app, "s_1").labelOffsetX);
			app.getKernel().redo();
			assertEquals(190, G9U1TestApp.lookup(app, "s_1").labelOffsetX);
			app.setXML(xml, true);
			GeoElement segment = G9U1TestApp.lookup(app, "s_1");
			assertEquals(190, segment.labelOffsetX);
			assertEquals(-65, segment.labelOffsetY);

			EuclidianViewD view = app.getEuclidianView1();
			view.setCoordSystem(720, 300, 50, 50);
			paint(view);
			assertTrue(view.getDrawableFor(segment).getLabelX() > 850,
					"label stays at the visible edge rather than jumping back to the anchor");
			assertEquals(190, segment.labelOffsetX, "rendering does not overwrite the offset");
			view.setCoordSystem(380, 300, 50, 50);
			paint(view);
			assertTrue(view.getDrawableFor(segment).getLabelX() > 540);

			segment.labelOffsetY = Integer.MAX_VALUE;
			paint(view);
			assertTrue(view.getDrawableFor(segment).getLabelY() < view.getHeight() - 1,
					"indexed descenders stay above the lower clip");
			segment.setCaption("$\\frac{x^2}{y}$");
			segment.setLabelMode(GeoElement.LABEL_CAPTION);
			segment.updateRepaint();
			paint(view);
			double edge = view.getDrawableFor(segment).getLabelY();
			assertTrue(edge < view.getHeight() - 1, "formula height fits above the lower clip");
			assertEquals(Integer.MAX_VALUE, segment.labelOffsetY);
			dragLabel(view, segment, 0, -35);
			assertTrue(view.getDrawableFor(segment).getLabelY() < edge - 25,
					"another drag starts from the displayed formula position");
			return null;
		});
	}

	@Test
	void classicRetainsRadialCap() throws Exception {
		onEdt(() -> {
			G9U1TestApp.create(); // Initialize the same Desktop factories for the Classic host.
			App3D classic = G9U1TestApp.withoutWindowDispatcher(new App3D(
					new CommandLineArguments(new String[] {"--silent"}), new JPanel()));
			GeoElement segment = segment(classic);
			dragLabel(classic.getEuclidianView1(), segment, 190, -65);
			assertEquals(80, Math.hypot(segment.labelOffsetX, segment.labelOffsetY), 1);
			return null;
		});
	}

	private static GeoElement segment(AppD app) {
		var view = app.getEuclidianView1();
		view.getJPanel().setSize(new Dimension(900, 600));
		view.updateSize();
		view.setCoordSystem(380, 300, 50, 50);
		app.setMode(EuclidianConstants.MODE_MOVE);
		GeoElement segment = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("s_1=Segment((-2,0),(2,0))", false)[0].toGeoElement();
		segment.setLabelVisible(true);
		segment.updateRepaint();
		return segment;
	}

	private static void dragLabel(EuclidianViewD view, GeoElement geo, int dx, int dy) {
		paint(view);
		DrawableND drawable = view.getDrawableFor(geo);
		int x = (int) drawable.getLabelX() + 2;
		int y = (int) drawable.getLabelY() - 4;
		assertSame(geo, view.getLabelHit(new GPoint(x, y), PointerEventType.MOUSE));
		var controller = view.getEuclidianController();
		controller.setDraggingDelay(0);
		controller.wrapMousePressed(event(view, MouseEvent.MOUSE_PRESSED, x, y));
		assertEquals(MoveMode.LABEL, controller.getMoveMode());
		controller.wrapMouseDragged(
				event(view, MouseEvent.MOUSE_DRAGGED, x + dx / 2, y + dy / 2), true);
		controller.wrapMouseDragged(event(view, MouseEvent.MOUSE_DRAGGED, x + dx, y + dy), true);
		controller.wrapMouseReleased(event(view, MouseEvent.MOUSE_RELEASED, x + dx, y + dy));
		paint(view);
		assertSame(geo, view.getLabelHit(new GPoint((int) drawable.getLabelX() + 2,
				(int) drawable.getLabelY() - 4), PointerEventType.MOUSE));
	}

	private static AbstractEvent event(EuclidianViewD view, int id, int x, int y) {
		return MouseEventD.wrapEvent(new MouseEvent(view.getJPanel(), id, 1,
				id == MouseEvent.MOUSE_RELEASED ? 0 : InputEvent.BUTTON1_DOWN_MASK,
				x, y, 1, false, id == MouseEvent.MOUSE_DRAGGED
						? MouseEvent.NOBUTTON : MouseEvent.BUTTON1));
	}

	private static void paint(EuclidianViewD view) {
		view.updateAllDrawables(true);
		BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			view.paint(new GGraphics2DD(graphics));
		} finally {
			graphics.dispose();
		}
	}
}

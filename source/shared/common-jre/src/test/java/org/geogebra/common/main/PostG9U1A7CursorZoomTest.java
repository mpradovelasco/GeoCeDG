/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.editor.share.util.KeyCodes;
import org.geogebra.test.BaseAppTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Classic view-input regression for A7; no construction semantics participate. */
class PostG9U1A7CursorZoomTest extends BaseAppTestSetup {

	private GlobalKeyDispatcherHeadless dispatcher;

	@BeforeEach
	void setUp() {
		setupClassicApp();
		dispatcher = new GlobalKeyDispatcherHeadless(getApp());
	}

	@Test
	void classicKeyboardFallbackUsesTrueNonSquareViewCenter() throws Exception {
		EuclidianView view = getApp().getActiveEuclidianView();
		view.getEuclidianController().wrapMouseExited(null);
		int centerX = view.getWidth() / 2;
		int centerY = view.getHeight() / 2;
		double worldX = view.toRealWorldCoordX(centerX);
		double worldY = view.toRealWorldCoordY(centerY);
		double scale = view.getXscale();
		dispatcher.handleCtrlKeys(KeyCodes.PLUS, false, false, true);
		awaitScaleChange(view, scale);
		assertEquals(centerX, view.toScreenCoordXd(worldX), 1E-7);
		assertEquals(centerY, view.toScreenCoordYd(worldY), 1E-7);
	}

	@Test
	void currentCursorRemainsAnchorForZoomInAndOut() throws Exception {
		EuclidianView view = getApp().getActiveEuclidianView();
		EuclidianController controller = view.getEuclidianController();
		int x = Math.max(1, view.getWidth() / 3);
		int y = Math.max(1, view.getHeight() / 4);
		controller.wrapMouseWheelMoved(x, y, 0, false, false);
		double worldX = view.toRealWorldCoordX(x);
		double worldY = view.toRealWorldCoordY(y);
		double scale = view.getXscale();
		controller.wrapMouseWheelMoved(x, y, -1, false, false);
		awaitScaleChange(view, scale);
		assertEquals(x, view.toScreenCoordXd(worldX), 1E-7);
		assertEquals(y, view.toScreenCoordYd(worldY), 1E-7);
		scale = view.getXscale();
		dispatcher.handleCtrlKeys(KeyCodes.PLUS, false, false, true);
		awaitScaleChange(view, scale);
		assertEquals(x, view.toScreenCoordXd(worldX), 1E-7);
		assertEquals(y, view.toScreenCoordYd(worldY), 1E-7);
		for (int i = 0; i < 8; i++) {
			scale = view.getXscale();
			dispatcher.handleCtrlKeys(i % 2 == 0 ? KeyCodes.MINUS : KeyCodes.PLUS,
					false, false, true);
			awaitScaleChange(view, scale);
			assertEquals(x, view.toScreenCoordXd(worldX), 1E-7);
			assertEquals(y, view.toScreenCoordYd(worldY), 1E-7);
		}
		assertTrue(Double.isFinite(view.getXscale()));
		view.setCoordSystem(view.getXZero(), view.getYZero(), 1E14, 1E14);
		assertTrue(Double.isFinite(view.toRealWorldCoordX(x)));
		view.setCoordSystem(view.getXZero(), view.getYZero(), 1E-14, 1E-14);
		assertTrue(Double.isFinite(view.toScreenCoordXd(worldX)));
	}

	private static void awaitScaleChange(EuclidianView view, double previous)
			throws InterruptedException {
		long deadline = System.nanoTime() + 2_000_000_000L;
		while (view.getXscale() == previous && System.nanoTime() < deadline) {
			Thread.sleep(5);
		}
		assertTrue(view.getXscale() != previous);
	}
}

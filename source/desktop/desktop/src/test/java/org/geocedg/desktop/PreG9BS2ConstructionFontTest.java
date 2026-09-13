/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.Util;
import org.geogebra.desktop.gui.dialog.TextInputDialogD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BS2ConstructionFontTest {

	@Test
	void constructionFontMenuAcceptsTenWithoutChangingGuiOrToolbarIcons() {
		AppGeoCeDG app = G9U1TestApp.create();
		int guiFontSize = app.getGUIFontSize();
		int menuFontSize = app.getPlainFont().getSize();
		int toolbarIconSize = app.getScaledIconSize();
		int maximumIconSize = app.getImageManager().getMaxIconSize();

		JMenu fontMenu = GeoCeDGHostMenuFactory.fontSize(app);
		JRadioButtonMenuItem tenPoints = item(fontMenu, "font-size.10");
		assertNotNull(tenPoints);
		tenPoints.doClick();

		assertEquals(10, app.getFontSize());
		assertEquals(guiFontSize, app.getGUIFontSize());
		assertEquals(menuFontSize, app.getPlainFont().getSize());
		assertEquals(toolbarIconSize, app.getScaledIconSize());
		assertEquals(maximumIconSize, app.getImageManager().getMaxIconSize());
	}

	@Test
	void constructionFontSizesRemainSeparateFromMenuFontChoices() {
		assertEquals(10, Util.appFontSizes(0));
		assertEquals(12, Util.menuFontSizes(0));
		assertEquals(10, Util.getValidFontSize(10));
		assertTrue(Util.appFontSizesLength() > Util.menuFontSizesLength());
	}

	@Test
	void normalConstructionFontValueStillAppliesAndPersistsInDocumentXml()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		JMenu fontMenu = GeoCeDGHostMenuFactory.fontSize(app);
		item(fontMenu, "font-size.16").doClick();
		assertEquals(16, app.getFontSize());
		item(fontMenu, "font-size.10").doClick();
		String xml = app.getXML();
		assertTrue(xml.contains("<font size=\"10\"/>"));

		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.setXML(xml, true);
		assertEquals(10, reopened.getFontSize());
	}

	@Test
	void textRedefineCreatesOneUndoStepAndSupportsRedo() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoText original = (GeoText) G9U1TestApp.eval(app, "t=\"A\"");
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		AtomicReference<GeoText> result = new AtomicReference<>();
		ErrorAccumulator errors = new ErrorAccumulator();
		TextInputDialogD.redefineText(app, original, "\"B\"", false, errors,
				result::set);
		assertEquals("", errors.getErrors());
		assertSame(original, result.get());
		assertEquals("B", ((GeoText) app.getKernel().lookupLabel("t")).getTextString());
		await(undo::undoPossible);
		assertEquals(1, undo.getHistorySize());
		app.getKernel().undo();
		await(() -> app.getKernel().lookupLabel("t") instanceof GeoText
				&& "A".equals(((GeoText) app.getKernel().lookupLabel("t")).getTextString()));
		assertTrue(undo.redoPossible());
		app.getKernel().redo();
		await(() -> app.getKernel().lookupLabel("t") instanceof GeoText
				&& "B".equals(((GeoText) app.getKernel().lookupLabel("t")).getTextString()));
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean());
	}

	private static JRadioButtonMenuItem item(JMenu menu, String id) {
		for (int index = 0; index < menu.getItemCount(); index++) {
			if (menu.getItem(index) instanceof JRadioButtonMenuItem) {
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) menu.getItem(index);
				if (id.equals(item.getClientProperty(
						GeoCeDGHostMenuFactory.HOST_CONTROL_ID))) {
					return item;
				}
			}
		}
		return null;
	}
}

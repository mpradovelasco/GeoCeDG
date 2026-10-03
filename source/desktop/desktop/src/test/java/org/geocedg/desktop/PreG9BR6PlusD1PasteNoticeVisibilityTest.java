/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-D1 remediation of the author-smoke finding: after a paste whose
 * source and target documents have different physical meanings, the paste notice must
 * be visible in the live status bar of the real application panel, not only marked
 * visible. The bar sits in the one-row SOUTH slot; when its segments are wider than
 * the window the notice must stay on that row (elided) instead of wrapping below it.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusD1PasteNoticeVisibilityTest {
	private static final UnitState MM = UnitState.of(UnitToken.MM, null, null);
	private static final UnitState CM = UnitState.of(UnitToken.CM, null, null);

	@TempDir
	Path temporaryDirectory;

	private final List<Runnable> expiries = new ArrayList<>();

	private AppGeoCeDG app(UnitState state, Locale locale) {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(locale);
		app.getKernel().setContinuous(false);
		app.getDocumentUnits().replace(state);
		app.getStatusBar().setNoticeTimer((delay, expiry) -> {
			expiries.add(expiry);
			return () -> { };
		});
		return app;
	}

	private static void ctrl(AppGeoCeDG app, int keyCode) {
		app.getGlobalKeyDispatcher().dispatchKeyEvent(new KeyEvent(
				app.getEuclidianView1().getJPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, keyCode,
				KeyEvent.CHAR_UNDEFINED));
	}

	private static void copyAll(AppGeoCeDG app) {
		app.getSelectionManager().setSelectedGeos(new ArrayList<>(
				app.getKernel().getConstruction().getGeoSetConstructionOrder()));
		ctrl(app, KeyEvent.VK_C);
	}

	private static void layoutTree(Container container) {
		container.doLayout();
		for (Component child : container.getComponents()) {
			if (child instanceof Container) {
				layoutTree((Container) child);
			}
		}
	}

	/** Lays out the real application panel, with the status bar in its SOUTH slot. */
	private static GeoCeDGStatusBar layOut(AppGeoCeDG app, JPanel panel, int width)
			throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			panel.setSize(width, 600);
			layoutTree(panel);
		});
		return app.getStatusBar();
	}

	private static JLabel notice(GeoCeDGStatusBar bar) {
		return bar.getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT);
	}

	private static void assertVisibleInTheBar(GeoCeDGStatusBar bar, String text) {
		JLabel notice = notice(bar);
		Rectangle bounds = notice.getBounds();
		String context = bounds + " in " + bar.getSize();
		assertTrue(notice.isVisible(), context);
		assertEquals(text, notice.getText());
		assertEquals(text, notice.getToolTipText(), "the full text stays in the tooltip");
		assertTrue(bounds.width > 0 && bounds.height > 0, context);
		assertTrue(bounds.y >= 0 && bounds.y + bounds.height <= bar.getHeight(),
				"inside the one-row bar: " + context);
		assertTrue(bounds.x + bounds.width <= bar.getWidth(), context);
		assertEquals(bar.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT).getY(), bounds.y,
				"on the row of the permanent segments: " + context);
	}

	private static Map<String, String> permanent(GeoCeDGStatusBar bar) {
		Map<String, String> state = new LinkedHashMap<>();
		for (String id : new String[] {GeoCeDGStatusBar.LAYER_SEGMENT,
				GeoCeDGStatusBar.CONSTRUCTION_UNIT_SEGMENT,
				GeoCeDGStatusBar.PRESENTATION_UNIT_SEGMENT}) {
			JLabel label = bar.getSegment(id);
			state.put(id, label.getText() + " " + label.isVisible() + " " + label.getBounds()
					+ " preferredWidth=" + (label.getWidth() == label.getPreferredSize().width));
		}
		return state;
	}

	@Test
	void aMismatchedPasteShowsTheNoticeOnTheVisibleRowOfANarrowWindow() throws Exception {
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			AppGeoCeDG app = app(MM, locale);
			G9U1TestApp.eval(app, "A=(1,2)");
			copyAll(app);
			app.setSaved();
			app.fileNew();
			app.getDocumentUnits().replace(CM);
			JPanel panel = app.buildApplicationPanel();
			GeoCeDGStatusBar bar = layOut(app, panel, 800);
			final Map<String, String> before = permanent(bar);
			final String xml = app.getXML();
			ctrl(app, KeyEvent.VK_V);
			int needed = bar.getPreferredSize().width;
			// the window is narrower than the bar's content: the old FlowLayout moved the
			// notice to a second row outside the one-row SOUTH slot
			layOut(app, panel, needed - 200);
			assertVisibleInTheBar(bar, app.layerText("Units.PasteNotice", "mm", "cm"));
			assertTrue(notice(bar).getWidth() < notice(bar).getPreferredSize().width,
					"the notice is elided, not moved");
			layOut(app, panel, 800);
			assertEquals(before, permanent(bar), "permanent segments are untouched");
			assertEquals(CM, app.getDocumentUnits().getState());
			assertEquals("(1, 2)", G9U1TestApp.lookup(app, "A")
					.toValueString(StringTemplate.defaultTemplate));
			assertFalse(app.getXML().contains(notice(bar).getText()));
			assertTrue(app.getXML().length() > xml.length(), "the geometry was pasted");
			layOut(app, panel, needed + 100);
			assertEquals(notice(bar).getPreferredSize().width, notice(bar).getWidth(),
					"with room, the notice is shown in full");
		}
	}

	@Test
	void openBetweenCopyAndPasteShowsTheSameVisibleNotice() throws Exception {
		AppGeoCeDG target = app(CM, Locale.forLanguageTag("es"));
		G9U1TestApp.eval(target, "Z=(0,0)");
		Path cm = temporaryDirectory.resolve("cm.cedg");
		assertTrue(((GuiManagerGeoCeDG) target.getGuiManager()).saveAsTo(cm.toFile()));
		AppGeoCeDG app = app(MM, Locale.forLanguageTag("es"));
		G9U1TestApp.eval(app, "A=(1,2)");
		copyAll(app);
		app.setSaved();
		assertTrue(app.loadFile(cm.toFile(), false));
		assertEquals(CM, app.getDocumentUnits().getState());
		JPanel panel = app.buildApplicationPanel();
		ctrl(app, KeyEvent.VK_V);
		GeoCeDGStatusBar bar = layOut(app, panel, 800);
		assertVisibleInTheBar(bar, app.layerText("Units.PasteNotice", "mm", "cm"));
	}

	@Test
	void theSamePhysicalMeaningOrAnUnspecifiedSideShowsNoNotice() throws Exception {
		Object[][] cases = {
			{MM, UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.001, "milli", "mmm"))},
			{UnitState.EMPTY, CM},
			{MM, UnitState.EMPTY}
		};
		for (Object[] pair : cases) {
			AppGeoCeDG app = app((UnitState) pair[0], Locale.ENGLISH);
			G9U1TestApp.eval(app, "A=(1,2)");
			copyAll(app);
			app.setSaved();
			app.fileNew();
			app.getDocumentUnits().replace((UnitState) pair[1]);
			JPanel panel = app.buildApplicationPanel();
			ctrl(app, KeyEvent.VK_V);
			GeoCeDGStatusBar bar = layOut(app, panel, 800);
			assertFalse(notice(bar).isVisible(), String.valueOf(pair[1]));
			assertEquals("", notice(bar).getText());
			assertEquals(1, app.getKernel().getConstruction().getGeoSetConstructionOrder()
					.size(), "the paste itself succeeded");
		}
	}

	@Test
	void theNoticeClearsOnTheNextPasteATransitionAndExpiry() throws Exception {
		AppGeoCeDG app = app(MM, Locale.ENGLISH);
		G9U1TestApp.eval(app, "A=(1,2)");
		copyAll(app);
		app.setSaved();
		app.fileNew();
		app.getDocumentUnits().replace(CM);
		JPanel panel = app.buildApplicationPanel();
		String text = app.layerText("Units.PasteNotice", "mm", "cm");

		ctrl(app, KeyEvent.VK_V);
		assertVisibleInTheBar(layOut(app, panel, 800), text);
		G9U1TestApp.eval(app, "Q=(5,5)");
		app.getSelectionManager().setSelectedGeos(new ArrayList<>(
				List.of(G9U1TestApp.lookup(app, "Q"))));
		ctrl(app, KeyEvent.VK_C);
		ctrl(app, KeyEvent.VK_V);
		GeoCeDGStatusBar bar = layOut(app, panel, 800);
		assertFalse(notice(bar).isVisible(), "the next paste (cm into cm) clears it");

		copyAllFrom(app, MM);
		ctrl(app, KeyEvent.VK_V);
		assertVisibleInTheBar(layOut(app, panel, 800), text);
		app.setSaved();
		app.fileNew();
		assertFalse(notice(layOut(app, panel, 800)).isVisible(), "a document transition");

		app.getDocumentUnits().replace(CM);
		ctrl(app, KeyEvent.VK_V);
		assertVisibleInTheBar(layOut(app, panel, 800), text);
		Map<String, String> before = permanent(bar);
		expiries.get(expiries.size() - 1).run();
		assertFalse(notice(layOut(app, panel, 800)).isVisible(), "expiry, no real sleep");
		assertEquals(before, permanent(bar), "permanent segments survive the removal");
	}

	/** Replaces the buffer with an mm document's point, as a copy in another document. */
	private void copyAllFrom(AppGeoCeDG app, UnitState sourceUnits) {
		app.setSaved();
		app.fileNew();
		app.getDocumentUnits().replace(sourceUnits);
		G9U1TestApp.eval(app, "A=(1,2)");
		copyAll(app);
		app.setSaved();
		app.fileNew();
		app.getDocumentUnits().replace(CM);
	}

	@Test
	void insertFileShowsTheSameVisibleNotice() throws Exception {
		AppGeoCeDG source = app(UnitState.of(UnitToken.M, null, null), Locale.ENGLISH);
		G9U1TestApp.eval(source, "Q=(5,5)");
		AppGeoCeDG target = app(MM, Locale.ENGLISH);
		G9U1TestApp.eval(target, "R=(1,1)");
		JPanel panel = target.buildApplicationPanel();
		((GeoCeDGCopyPaste) target.getCopyPaste()).insertFrom(source, target,
				new LinkedHashSet<>(), false);
		GeoCeDGStatusBar bar = layOut(target, panel, 800);
		assertVisibleInTheBar(bar, target.layerText("Units.PasteNotice", "m", "mm"));
		GeoElement inserted = G9U1TestApp.lookup(target, "Q");
		assertEquals("(5, 5)", inserted.toValueString(StringTemplate.defaultTemplate));
		assertEquals(MM, target.getDocumentUnits().getState());
	}
}

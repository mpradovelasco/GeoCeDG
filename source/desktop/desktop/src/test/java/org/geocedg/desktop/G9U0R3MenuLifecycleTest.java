/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** G9U0-R3 public product-menu lifecycle scenarios R3-M01--R3-M06. */
class G9U0R3MenuLifecycleTest {

	private static final List<String> LOCUS_ACTION_KEYS = List.of(
			"semantic.locus-v2.create", "semantic.locus-v2.point-explicit",
			"measure.locus-v2-total-length", "measure.locus-v2-partial-length",
			"result.inspect-rich");

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void m01ProductMenuIsPopulatedAfterInitMenubar() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		assertApprovedEnabledItems(menuBar);
		menuBar.initMenubar();
		assertEquals(5, menuBar.getMenuCount());
		assertApprovedEnabledItems(menuBar);
	}

	@Test
	void m02ProductMenuRemainsPopulatedAfterUpdateFonts() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		menuBar.updateFonts();

		assertApprovedEnabledItems(menuBar);
	}

	@Test
	void m03ProductMenuRemainsPopulatedAfterRepeatedUpdateFonts() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		menuBar.updateFonts();
		menuBar.updateFonts();
		menuBar.updateFonts();

		assertApprovedEnabledItems(menuBar);
	}

	@Test
	void m04LocalizationRefreshRebuildsLocalizedItems() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		app.setLocale(new Locale("es"));

		assertApprovedEnabledItems(menuBar);
		assertEquals(GeoCeDGProfile.getText("geocedg.action.ResultInspect.name", "es"),
				item(menuBar, "result.inspect-rich").getText());
	}

	@Test
	void m05SingleLocusFlagExposesEveryApprovedProductAction() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		assertTrue(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()));
		assertApprovedEnabledItems(menuBar);
	}

	@Test
	void m06FeatureOffKeepsLocusActionsUnavailable() {
		AppGeoCeDG app = appWithLocusV2(false);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()));
		for (String id : LOCUS_ACTION_KEYS) {
			assertFalse(item(menuBar, id).isEnabled(), id);
		}
		assertTrue(item(menuBar, "export.dxf-2d").isEnabled());
	}

	private static AppGeoCeDG appWithLocusV2(boolean enabled) {
		return new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=" + enabled}), new JPanel());
	}

	private static GeoCeDGMenuBar initializeMenuBar(AppGeoCeDG app) {
		app.getGuiManager().initMenubar();
		return (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
	}

	private static JMenuItem item(GeoCeDGMenuBar menuBar, String id) {
		for (int index = 0; index < menuBar.getMenuCount(); index++) {
			JMenuItem item = G9U1WorkspaceSurfaceTest.findItem(menuBar.getMenu(index), id);
			if (item != null) {
				return item;
			}
		}
		throw new AssertionError("Missing product action " + id);
	}

	private static void assertApprovedEnabledItems(GeoCeDGMenuBar menuBar) {
		for (String id : LOCUS_ACTION_KEYS) {
			assertTrue(item(menuBar, id).isVisible(), id);
			assertTrue(item(menuBar, id).isEnabled(), id);
		}
		assertTrue(item(menuBar, "export.dxf-2d").isEnabled());
		assertEquals(GeoCeDGMenuBar.DXF_ACTION_ACCELERATOR,
				item(menuBar, "export.dxf-2d").getAccelerator());
	}
}

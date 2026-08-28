/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JMenu;
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
			"LocusV2.Tool", "LocusV2.Point.Tool",
			"LocusLength.Total.Tool", "LocusLength.Partial.Tool",
			"LocusV2.Results.Inspect");

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

		assertApprovedEnabledItems(app, productMenu(menuBar));
		menuBar.initMenubar();
		assertEquals(1, productMenuCount(menuBar));
		assertApprovedEnabledItems(app, productMenu(menuBar));
	}

	@Test
	void m02ProductMenuRemainsPopulatedAfterUpdateFonts() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		menuBar.updateFonts();

		assertApprovedEnabledItems(app, productMenu(menuBar));
	}

	@Test
	void m03ProductMenuRemainsPopulatedAfterRepeatedUpdateFonts() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		menuBar.updateFonts();
		menuBar.updateFonts();
		menuBar.updateFonts();

		assertApprovedEnabledItems(app, productMenu(menuBar));
	}

	@Test
	void m04LocalizationRefreshRebuildsLocalizedItems() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		app.setLocale(new Locale("es"));

		JMenu productMenu = productMenu(menuBar);
		assertApprovedEnabledItems(app, productMenu);
		assertTrue(itemTexts(productMenu).contains(
				"Inspeccionar resultado rico de Locus V2"));
	}

	@Test
	void m05SingleLocusFlagExposesEveryApprovedProductAction() {
		AppGeoCeDG app = appWithLocusV2(true);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		assertTrue(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()));
		assertApprovedEnabledItems(app, productMenu(menuBar));
	}

	@Test
	void m06FeatureOffKeepsLocusActionsUnavailable() {
		AppGeoCeDG app = appWithLocusV2(false);
		GeoCeDGMenuBar menuBar = initializeMenuBar(app);

		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()));
		assertEquals(List.of(GeoCeDGMenuBar.DXF_ACTION_TEXT),
				itemTexts(productMenu(menuBar)));
	}

	private static AppGeoCeDG appWithLocusV2(boolean enabled) {
		return new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=" + enabled}), new JPanel());
	}

	private static GeoCeDGMenuBar initializeMenuBar(AppGeoCeDG app) {
		app.getGuiManager().initMenubar();
		return (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
	}

	private static JMenu productMenu(GeoCeDGMenuBar menuBar) {
		for (int index = 0; index < menuBar.getMenuCount(); index++) {
			JMenu candidate = menuBar.getMenu(index);
			if (candidate != null && "GeoCeDG".equals(candidate.getText())) {
				return candidate;
			}
		}
		throw new AssertionError("GeoCeDG product menu is missing");
	}

	private static int productMenuCount(GeoCeDGMenuBar menuBar) {
		int count = 0;
		for (int index = 0; index < menuBar.getMenuCount(); index++) {
			JMenu candidate = menuBar.getMenu(index);
			if (candidate != null && "GeoCeDG".equals(candidate.getText())) {
				count++;
			}
		}
		return count;
	}

	private static void assertApprovedEnabledItems(AppGeoCeDG app,
			JMenu productMenu) {
		List<String> expected = new ArrayList<>();
		expected.add(GeoCeDGMenuBar.DXF_ACTION_TEXT);
		for (String key : LOCUS_ACTION_KEYS) {
			expected.add(app.getLocalization().getMenu(key));
		}
		assertEquals(expected, itemTexts(productMenu));
		for (Component component : productMenu.getMenuComponents()) {
			if (component instanceof JMenuItem) {
				assertTrue(component.isVisible());
				assertTrue(component.isEnabled());
			}
		}
	}

	private static List<String> itemTexts(JMenu menu) {
		List<String> texts = new ArrayList<>();
		for (Component component : menu.getMenuComponents()) {
			if (component instanceof JMenuItem) {
				texts.add(((JMenuItem) component).getText());
			}
		}
		return texts;
	}
}

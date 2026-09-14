/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;

import org.geocedg.desktop.GeoCeDGPresentationPreferences.Category;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BS4PresentationSizingTest {

	@Test
	void missingAndInvalidValuesCaptureInheritedAppearanceAndReloadIndependently() {
		MemoryStore store = new MemoryStore();
		store.values.put(Category.ALGEBRA_FONT.preferenceKey(), "invalid");
		GeoCeDGPresentationPreferences.InheritedValues inherited =
				new GeoCeDGPresentationPreferences.InheritedValues(18, 48, 20, 14, 10);
		GeoCeDGPresentationPreferences preferences =
				new GeoCeDGPresentationPreferences(store, inherited);

		assertArrayEquals(new int[] {18, 48, 20, 14, 10}, values(preferences));
		assertEquals(Category.values().length, store.values.size());

		preferences.set(Category.MENU_FONT, 24);
		preferences.set(Category.TOOLBAR_ICON, 56);
		GeoCeDGPresentationPreferences reloaded =
				new GeoCeDGPresentationPreferences(store, inherited);
		assertArrayEquals(new int[] {24, 56, 20, 14, 10}, values(reloaded));
	}

	@Test
	void optionsApplyFiveIndependentLiveSizesWithoutDirtyingConstruction() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		try {
			GuiManagerD manager = (GuiManagerD) app.getGuiManager();
			final AlgebraViewD algebra = manager.getAlgebraView();
			ConstructionProtocolViewD protocol =
					(ConstructionProtocolViewD) manager.getConstructionProtocolView();
			final JTable protocolTable =
					(JTable) protocol.getCpPanel().getViewport().getView();
			app.getEuclidianView2(1);
			GeoCeDGPresentationOptionsPanel panel =
					new GeoCeDGPresentationOptionsPanel(app);
			Map<Category, Integer> expected = new EnumMap<>(original);
			app.setSaved();

			for (Category category : Category.values()) {
				int selected = differentSupportedSize(category, expected.get(category));
				panel.selectSize(category, selected);
				expected.put(category, selected);
				assertEquals(expected, snapshot(app));
				assertEquals(selected, panel.selectedSize(category));
				assertTrue(app.isSaved(), category.name());
			}

			JMenu menu = GeoCeDGHostMenuFactory.fontSize(app);
			assertEquals(expected.get(Category.MENU_FONT).intValue(),
					menu.getFont().getSize());
			assertEquals(expected.get(Category.TOOLBAR_ICON).intValue(),
					app.getScaledIconSize());
			assertEquals(expected.get(Category.TOOLBAR_ICON).intValue(),
					app.getImageManager().getMaxIconSize());
			JToggleButton nativeTool = GeoCeDGToolbarContainer.createNativeToolReference(app);
			assertEquals(app.getScaledIconSize(), nativeTool.getIcon().getIconWidth());
			assertEquals(expected.get(Category.ALGEBRA_FONT).intValue(),
					algebra.getFont().getSize());
			assertEquals(expected.get(Category.CONSTRUCTION_PROTOCOL_FONT).intValue(),
					protocolTable.getFont().getSize());
			assertEquals(expected.get(Category.GRAPHICS_FONT).intValue(),
					app.getEuclidianView1().getFontSize());
			assertEquals(expected.get(Category.GRAPHICS_FONT).intValue(),
					app.getEuclidianView2(1).getFontSize());
		} finally {
			restore(app, original);
		}
	}

	@Test
	void constructionFontAndGeoTextRemainOutsidePresentationPreferences() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		try {
			GeoText text = (GeoText) G9U1TestApp.eval(app, "t=\"S4\"");
			double logicalSize = text.getFontSize(app.getFontSize());
			int changedMenu = differentSupportedSize(Category.MENU_FONT,
					original.get(Category.MENU_FONT));
			int changedGraphics = differentSupportedSize(Category.GRAPHICS_FONT,
					original.get(Category.GRAPHICS_FONT));

			app.setPresentationSize(Category.MENU_FONT, changedMenu);
			app.setPresentationSize(Category.GRAPHICS_FONT, changedGraphics);
			assertEquals(logicalSize, text.getFontSize(app.getFontSize()));
			assertEquals(original.get(Category.TOOLBAR_ICON).intValue(),
					app.getScaledIconSize());

			Map<Category, Integer> presentationBeforeConstructionChange = snapshot(app);
			app.setFontSize(10, true);
			assertEquals(10, app.getFontSize());
			assertEquals(presentationBeforeConstructionChange, snapshot(app));
			assertEquals(original.get(Category.TOOLBAR_ICON).intValue(),
					app.getScaledIconSize());
		} finally {
			restore(app, original);
		}
	}

	@Test
	void classicDefaultsKeepExistingPresentationOwnership() {
		G9U1TestApp.create();
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(
				new CommandLineArguments(new String[] {"--silent"}), new JPanel(), true));

		assertEquals(classic.getPlainFont(), classic.getMenuFont());
		assertEquals(classic.getPlainFont(), classic.getAlgebraFont());
		assertEquals(classic.getPlainFont(), classic.getConstructionProtocolFont());
		assertEquals(classic.getFontSize(), classic.getEuclidianViewFontSize());
		assertNull(classic.newProductPresentationOptionsPanel());
	}

	private static int[] values(GeoCeDGPresentationPreferences preferences) {
		return new int[] {
				preferences.get(Category.MENU_FONT),
				preferences.get(Category.TOOLBAR_ICON),
				preferences.get(Category.ALGEBRA_FONT),
				preferences.get(Category.CONSTRUCTION_PROTOCOL_FONT),
				preferences.get(Category.GRAPHICS_FONT)
		};
	}

	private static Map<Category, Integer> snapshot(AppGeoCeDG app) {
		Map<Category, Integer> snapshot = new EnumMap<>(Category.class);
		for (Category category : Category.values()) {
			snapshot.put(category, app.getPresentationSize(category));
		}
		return snapshot;
	}

	private static void restore(AppGeoCeDG app, Map<Category, Integer> values) {
		values.forEach(app::setPresentationSize);
	}

	private static int differentSupportedSize(Category category, int current) {
		for (int size : GeoCeDGPresentationPreferences.supportedSizes(category)) {
			if (size != current) {
				return size;
			}
		}
		throw new AssertionError("No alternative size for " + category);
	}

	private static final class MemoryStore
			implements GeoCeDGPresentationPreferences.Store {
		private final Map<String, String> values = new HashMap<>();

		@Override
		public String load(String key) {
			return values.get(key);
		}

		@Override
		public void save(String key, String value) {
			values.put(key, value);
		}
	}
}

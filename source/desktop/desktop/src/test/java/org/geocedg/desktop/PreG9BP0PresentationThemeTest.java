/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.geocedg.desktop.GeoCeDGPresentationPreferences.Category;
import org.geocedg.desktop.GeoCeDGPresentationPreferences.ValueSource;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.main.App;
import org.geogebra.common.main.OptionType;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;
import org.geogebra.desktop.gui.dialog.options.OptionsAdvancedD;
import org.geogebra.desktop.gui.dialog.options.OptionsLayoutD;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-P0 focused contracts: the closed presentation-theme preset, the Preferences
 * consolidation into the Layout &amp; Presentation tab, and the single `1.0.0` version
 * authority. A theme is presentation only: it never mutates the document, the construction
 * or the serialized background.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BP0PresentationThemeTest {

	// ----------------------------------------------------------------- identity/default

	@Test
	void freshAndHistoricalProfilesDefaultToOriginal() {
		MemoryStore store = new MemoryStore();
		GeoCeDGThemePreference preference = new GeoCeDGThemePreference(store);

		assertSame(GeoCeDGPresentationTheme.ORIGINAL, preference.get());
		assertEquals(ValueSource.FRESH_DEFAULT, preference.source());
		assertEquals("original", store.values.get(GeoCeDGThemePreference.PREFERENCE_KEY));
		assertNull(GeoCeDGPresentationTheme.ORIGINAL.palette());
	}

	@Test
	void unknownOrCorruptStoredIdentityFallsBackToOriginal() {
		for (String stored : new String[] {"", "Scientific Paper", "dark", "1"}) {
			MemoryStore store = new MemoryStore();
			store.values.put(GeoCeDGThemePreference.PREFERENCE_KEY, stored);

			GeoCeDGThemePreference preference = new GeoCeDGThemePreference(store);

			assertSame(GeoCeDGPresentationTheme.ORIGINAL, preference.get(), stored);
			assertEquals("original",
					store.values.get(GeoCeDGThemePreference.PREFERENCE_KEY), stored);
		}
	}

	@Test
	void themeIdentityIsSemanticStableAndClosedToThreePresets() {
		assertEquals(3, GeoCeDGPresentationTheme.values().length);
		assertEquals("original", GeoCeDGPresentationTheme.ORIGINAL.id());
		assertEquals("scientific-paper", GeoCeDGPresentationTheme.SCIENTIFIC_PAPER.id());
		assertEquals("cool-geometry", GeoCeDGPresentationTheme.COOL_GEOMETRY.id());
		for (GeoCeDGPresentationTheme theme : GeoCeDGPresentationTheme.values()) {
			assertSame(theme, GeoCeDGPresentationTheme.fromId(theme.id()));
			// Identity is never derived from the localized text shown to the user.
			assertFalse(theme.id().equals(
					GeoCeDGProfile.getText(theme.textKey(), "en")), theme.id());
			assertFalse(theme.id().equals(
					GeoCeDGProfile.getText(theme.textKey(), "es")), theme.id());
		}
		assertNull(GeoCeDGPresentationTheme.fromId("cool geometry"));
	}

	@Test
	void eachThemeSelectionPersistsAndReloadsItsIdentity() {
		MemoryStore store = new MemoryStore();
		for (GeoCeDGPresentationTheme theme : GeoCeDGPresentationTheme.values()) {
			new GeoCeDGThemePreference(store).set(theme);

			GeoCeDGThemePreference reloaded = new GeoCeDGThemePreference(store);
			assertSame(theme, reloaded.get());
			assertEquals(ValueSource.EXPLICIT_PERSISTED, reloaded.source());
			assertEquals(theme.id(),
					store.values.get(GeoCeDGThemePreference.PREFERENCE_KEY));
		}
		assertThrows(IllegalArgumentException.class,
				() -> new GeoCeDGThemePreference(store).set(null));
	}

	@Test
	void paletteAuthorityMatchesTheApprovedPresetValues() {
		GeoCeDGPresentationTheme.Palette paper =
				GeoCeDGPresentationTheme.SCIENTIFIC_PAPER.palette();
		assertEquals(0xE5E2DC, paper.frame());
		assertEquals(0xECE9E3, paper.surface());
		assertEquals(0xF5F3EE, paper.panel());
		assertEquals(0xFFFDF8, paper.canvas());
		assertEquals(0xC8C4BC, paper.border());

		GeoCeDGPresentationTheme.Palette cool =
				GeoCeDGPresentationTheme.COOL_GEOMETRY.palette();
		assertEquals(0xDEE4E9, cool.frame());
		assertEquals(0xE7ECF0, cool.surface());
		assertEquals(0xEFF3F6, cool.panel());
		assertEquals(0xF8FAFC, cool.canvas());
		assertEquals(0xBBC4CC, cool.border());
	}

	// ------------------------------------------------------------------ chrome surfaces

	@Test
	void installingAThemeSetsKnownPresentationRolesAndOriginalRestoresThem() {
		Object frameBaseline = UIManager.get("Panel.background");
		try {
			for (GeoCeDGPresentationTheme theme : new GeoCeDGPresentationTheme[] {
					GeoCeDGPresentationTheme.SCIENTIFIC_PAPER,
					GeoCeDGPresentationTheme.COOL_GEOMETRY}) {
				GeoCeDGThemeInstaller.install(theme);
				GeoCeDGPresentationTheme.Palette palette = theme.palette();

				assertSame(theme, GeoCeDGThemeInstaller.installedTheme());
				// frame/client area, menu and toolbar surface, panels, borders
				assertEquals(palette.frameColor(), UIManager.getColor("Panel.background"));
				assertEquals(palette.surfaceColor(),
						UIManager.getColor("MenuBar.background"));
				assertEquals(palette.surfaceColor(),
						UIManager.getColor("ToolBar.background"));
				assertEquals(palette.surfaceColor(),
						UIManager.getColor("PopupMenu.background"));
				// Algebra and Construction Protocol content surfaces
				assertEquals(palette.panelColor(), UIManager.getColor("Tree.background"));
				assertEquals(palette.panelColor(), UIManager.getColor("Table.background"));
				assertEquals(palette.borderColor(),
						UIManager.getColor("Separator.foreground"));
				// GeoCeDG-owned roles consumed by shared classes that hard-coded a color
				assertEquals(palette.panelColor(),
						UIManager.getColor(GeoCeDGThemeInstaller.PANEL_ROLE));
				assertEquals(palette.surfaceColor(),
						UIManager.getColor(GeoCeDGThemeInstaller.SURFACE_ROLE));
			}

			GeoCeDGThemeInstaller.install(GeoCeDGPresentationTheme.ORIGINAL);
			assertEquals(frameBaseline, UIManager.get("Panel.background"));
			for (String role : new String[] {GeoCeDGThemeInstaller.FRAME_ROLE,
					GeoCeDGThemeInstaller.SURFACE_ROLE, GeoCeDGThemeInstaller.PANEL_ROLE,
					GeoCeDGThemeInstaller.BORDER_ROLE}) {
				assertNull(UIManager.getColor(role), role);
			}
		} finally {
			GeoCeDGThemeInstaller.install(GeoCeDGPresentationTheme.ORIGINAL);
		}
	}

	@Test
	void sharedPanelsConsumeTheProductRoleAndKeepTheirLiteralWithoutIt() {
		try {
			GeoCeDGThemeInstaller.install(GeoCeDGPresentationTheme.ORIGINAL);
			assertEquals(Color.white,
					ConstructionProtocolViewD.panelBackground());

			GeoCeDGThemeInstaller.install(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER);
			assertEquals(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER.palette().panelColor(),
					ConstructionProtocolViewD.panelBackground());
		} finally {
			GeoCeDGThemeInstaller.install(GeoCeDGPresentationTheme.ORIGINAL);
		}
	}

	// --------------------------------------------------------------------- canvas policy

	@Test
	void canvasPresetAppliesToTheApplicationDefaultBackgroundOnly() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGPresentationTheme initial = app.getPresentationTheme();
		try {
			app.setPresentationTheme(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER);

			GColor canvas = GeoCeDGPresentationTheme.SCIENTIFIC_PAPER.palette()
					.canvasColor();
			assertEquals(canvas, app.getPresentationBackground(GColor.WHITE));
			// An explicitly chosen document background keeps its own meaning.
			assertNull(app.getPresentationBackground(GColor.newColor(240, 12, 12)));
			assertNull(app.getPresentationBackground(GColor.BLACK));

			app.setPresentationTheme(GeoCeDGPresentationTheme.ORIGINAL);
			assertNull(app.getPresentationBackground(GColor.WHITE));
		} finally {
			app.setPresentationTheme(initial);
		}
	}

	@Test
	void canvasPresetNeverMutatesGraphicsOrGraphics2DocumentState() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGPresentationTheme initial = app.getPresentationTheme();
		try {
			app.getGuiManager().setShowView(true, App.VIEW_EUCLIDIAN2);
			EuclidianSettings first = app.getSettings().getEuclidian(1);
			EuclidianSettings second = app.getSettings().getEuclidian(2);
			GColor firstBefore = first.getBackground();
			GColor secondBefore = second.getBackground();

			app.setPresentationTheme(GeoCeDGPresentationTheme.COOL_GEOMETRY);

			assertEquals(firstBefore, first.getBackground());
			assertEquals(secondBefore, second.getBackground());
			assertEquals(GColor.WHITE, first.getBackground());
			// The Swing panel keeps the document color, so getXML stays unchanged.
			assertEquals(GColor.WHITE, app.getEuclidianView1().getBackgroundCommon());
			// One presentation canvas serves Graphics and Graphics2.
			assertEquals(GeoCeDGPresentationTheme.COOL_GEOMETRY.palette().canvasColor(),
					app.getPresentationBackground(GColor.WHITE));
		} finally {
			app.setPresentationTheme(initial);
		}
	}

	@Test
	void themeAddsNoSerializationAndLeavesObjectsAxesAndGridUntouched() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGPresentationTheme initial = app.getPresentationTheme();
		try {
			G9U1TestApp.eval(app, "P=(1,2)");
			String before = app.getXML();
			GColor objectColorBefore = app.getKernel().lookupLabel("P")
					.getObjectColor();
			EuclidianSettings settings = app.getSettings().getEuclidian(1);
			final GColor axesBefore = settings.getAxesColor();
			final GColor gridBefore = settings.getGridColor();

			app.setPresentationTheme(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER);
			String after = app.getXML();

			assertEquals(before, after);
			assertTrue(after.contains("<bgColor r=\"255\" g=\"255\" b=\"255\""), after);
			for (GeoCeDGPresentationTheme theme : GeoCeDGPresentationTheme.values()) {
				assertFalse(after.contains(theme.id()), theme.id());
			}
			assertFalse(after.contains("presentation.theme"));
			assertEquals(objectColorBefore,
					app.getKernel().lookupLabel("P").getObjectColor());
			assertEquals(axesBefore, settings.getAxesColor());
			assertEquals(gridBefore, settings.getGridColor());
			// Selecting a theme is not a construction change.
			assertTrue(app.isSaved());
		} finally {
			app.setPresentationTheme(initial);
		}
	}

	@Test
	void loadingADocumentDoesNotChangeTheThemePreference() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGPresentationTheme initial = app.getPresentationTheme();
		try {
			app.setPresentationTheme(GeoCeDGPresentationTheme.COOL_GEOMETRY);
			G9U1TestApp.eval(app, "Q=(3,4)");
			String document = app.getXML();

			app.setXML(document, true);

			assertSame(GeoCeDGPresentationTheme.COOL_GEOMETRY, app.getPresentationTheme());
			assertEquals(GColor.WHITE, app.getSettings().getEuclidian(1).getBackground());
		} finally {
			app.setPresentationTheme(initial);
		}
	}

	// ------------------------------------------------------------ Preferences placement

	@Test
	void presentationSizesAndThemeLiveInLayoutAndLeaveAdvanced() {
		AppGeoCeDG app = G9U1TestApp.create();

		assertTrue(app.productOwnsPresentationSizing());
		OptionsLayoutD layout = new OptionsLayoutD(withDockBar(app));
		OptionsAdvancedD advanced = new OptionsAdvancedD(app);

		List<Component> layoutTree = flatten(layout.getWrappedPanel());
		List<Component> advancedTree = flatten(advanced.getWrappedPanel());

		assertEquals(1, count(layoutTree, GeoCeDGPresentationOptionsPanel.class));
		assertEquals(1, count(layoutTree, GeoCeDGThemeOptionsPanel.class));
		assertEquals(0, count(advancedTree, GeoCeDGPresentationOptionsPanel.class));
		assertEquals(0, count(advancedTree, GeoCeDGThemeOptionsPanel.class));
	}

	@Test
	void movedPresentationSizesKeepKeysDefaultsAndLiveBehaviour() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> before = new HashMap<>();
		for (Category category : Category.values()) {
			before.put(category, app.getPresentationSize(category));
		}
		try {
			OptionsLayoutD layout = new OptionsLayoutD(withDockBar(app));
			GeoCeDGPresentationOptionsPanel sizes = single(
					flatten(layout.getWrappedPanel()),
					GeoCeDGPresentationOptionsPanel.class);

			// Unchanged S4 keys, defaults and supported values.
			assertEquals("geocedg.presentation.menu-font-size.v1",
					Category.MENU_FONT.preferenceKey());
			assertEquals(14, GeoCeDGPresentationPreferences.freshDefault(
					Category.MENU_FONT));
			assertEquals(28, GeoCeDGPresentationPreferences.freshDefault(
					Category.TOOLBAR_ICON));

			int target = before.get(Category.MENU_FONT) == 16 ? 18 : 16;
			sizes.selectSize(Category.MENU_FONT, target);

			assertEquals(target, app.getPresentationSize(Category.MENU_FONT));
			assertEquals(target, sizes.selectedSize(Category.MENU_FONT));
			assertEquals(target, app.getMenuFont().getSize());
			// Graphics and Graphics2 still share one Graphics preference.
			assertEquals(app.getPresentationSize(Category.GRAPHICS_FONT),
					app.getEuclidianViewFontSize());
		} finally {
			before.forEach(app::setPresentationSize);
		}
	}

	@Test
	void themeSelectorAppliesThroughThePanelAndIsLocalized() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGPresentationTheme initial = app.getPresentationTheme();
		try {
			GeoCeDGThemeOptionsPanel panel = single(
					flatten(new OptionsLayoutD(withDockBar(app)).getWrappedPanel()),
					GeoCeDGThemeOptionsPanel.class);

			panel.selectTheme(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER);
			assertSame(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER,
					app.getPresentationTheme());
			assertSame(GeoCeDGPresentationTheme.SCIENTIFIC_PAPER, panel.selectedTheme());

			panel.selectTheme(GeoCeDGPresentationTheme.ORIGINAL);
			assertSame(GeoCeDGPresentationTheme.ORIGINAL, app.getPresentationTheme());

			assertEquals("Theme", GeoCeDGProfile.getText("Presentation.ThemeGroup", "en"));
			assertEquals("Tema", GeoCeDGProfile.getText("Presentation.ThemeGroup", "es"));
			assertEquals("Scientific Paper", GeoCeDGProfile.getText(
					GeoCeDGPresentationTheme.SCIENTIFIC_PAPER.textKey(), "en"));
			assertEquals("Geometría fría", GeoCeDGProfile.getText(
					GeoCeDGPresentationTheme.COOL_GEOMETRY.textKey(), "es"));
		} finally {
			app.setPresentationTheme(initial);
		}
	}

	@Test
	void layoutTabIsLocalizedAsLayoutAndPresentation() {
		AppGeoCeDG app = G9U1TestApp.create();

		assertEquals("Layout & Presentation",
				GeoCeDGProfile.getText("Presentation.LayoutTab", "en"));
		assertEquals("Disposición y presentación",
				GeoCeDGProfile.getText("Presentation.LayoutTab", "es"));
		assertEquals(GeoCeDGProfile.getText("Presentation.LayoutTab",
						app.getLocale().getLanguage()),
				app.getProductOptionTypeTitle(OptionType.LAYOUT));
		for (OptionType type : OptionType.values()) {
			if (type != OptionType.LAYOUT) {
				assertNull(app.getProductOptionTypeTitle(type), type.name());
			}
		}
	}

	// ---------------------------------------------------------------- Classic containment

	@Test
	void classicInheritsNoGeoCeDGPresetOrConsolidation() {
		G9U1TestApp.create();
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(
				new CommandLineArguments(new String[] {"--silent"}), new JPanel(), true));

		assertFalse(classic.productOwnsPresentationSizing());
		assertNull(classic.newProductPresentationOptionsPanel());
		assertNull(classic.newProductPresentationThemePanel());
		assertNull(classic.getProductOptionTypeTitle(OptionType.LAYOUT));
		assertNull(classic.getPresentationBackground(GColor.WHITE));

		List<Component> layoutTree = flatten(
				new OptionsLayoutD(withDockBar(classic)).getWrappedPanel());
		assertEquals(0, count(layoutTree, GeoCeDGPresentationOptionsPanel.class));
		assertEquals(0, count(layoutTree, GeoCeDGThemeOptionsPanel.class));
	}

	// ------------------------------------------------------------------ version baseline

	@Test
	void singleVersionAuthorityDeclaresOneZeroZeroWithoutPromotingThePackage()
			throws IOException {
		String profile = Files.readString(repositoryRoot()
				.resolve("packaging/windows/package.yml"), StandardCharsets.UTF_8);

		assertEquals(1, occurrences(profile, "\"version\""), profile);
		assertTrue(profile.contains("\"version\": \"1.0.0\""), profile);
		assertFalse(profile.contains("0.9.0"), profile);
		// The installer upgrade identity is preserved across the version baseline.
		assertTrue(profile.contains(
				"\"upgrade_uuid\": \"b52d8e6d-3996-4bc5-b9ba-4f51f73c6e44\""), profile);
		// The version baseline does not promote distribution state.
		assertTrue(profile.contains("\"status\": \"internal-evaluation\""), profile);
		assertTrue(profile.contains(
				"\"public_redistribution\": \"blocked-pending-license-and-asset-approval\""),
				profile);
	}

	@Test
	void derivedVersionSurfacesConsumeTheSingleAuthority() {
		assertEquals("1.0.0", GeoCeDGProductInfo.semanticVersion());
		assertEquals("1.0", GeoCeDGProductInfo.displayVersion());
		assertEquals("GeoCeDG 1.0", GeoCeDGProductInfo.applicationTitle());
		assertEquals("GeoCeDG 1.0 — Revision1.cedg",
				GeoCeDGProductInfo.windowTitle("Revision1.cedg"));
	}

	// ------------------------------------------------------------------------- helpers

	private static <T extends AppD> T withDockBar(T app) {
		// The Layout tab inspects the real dock bar; build the ordinary application panel.
		app.buildApplicationPanel();
		return app;
	}

	private static List<Component> flatten(Container root) {
		List<Component> components = new ArrayList<>();
		collect(root, components);
		return components;
	}

	private static void collect(Container container, List<Component> components) {
		for (Component component : container.getComponents()) {
			components.add(component);
			if (component instanceof Container child) {
				collect(child, components);
			}
		}
	}

	private static int count(List<Component> components, Class<?> type) {
		int found = 0;
		for (Component component : components) {
			if (type.isInstance(component)) {
				found++;
			}
		}
		return found;
	}

	private static <T> T single(List<Component> components, Class<T> type) {
		T found = null;
		for (Component component : components) {
			if (type.isInstance(component)) {
				assertNull(found, type.getName());
				found = type.cast(component);
			}
		}
		assertNotNull(found, type.getName());
		return found;
	}

	private static int occurrences(String text, String token) {
		int found = 0;
		int index = text.indexOf(token);
		while (index >= 0) {
			found++;
			index = text.indexOf(token, index + token.length());
		}
		return found;
	}

	private static Path repositoryRoot() {
		Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null && !Files.isRegularFile(current.resolve("AGENTS.md"))) {
			current = current.getParent();
		}
		if (current == null) {
			throw new IllegalStateException("Repository root is unavailable");
		}
		return current;
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

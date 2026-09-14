/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.geocedg.desktop.GeoCeDGPresentationPreferences.Category;
import org.geocedg.desktop.GeoCeDGPresentationPreferences.ValueSource;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.dialog.InputDialogD;
import org.geogebra.desktop.gui.dialog.TextInputDialogD;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.ScaledIcon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BS4PresentationSizingTest {

	@Test
	void freshProfileUsesApprovedIndependentDefaults() {
		MemoryStore store = new MemoryStore();
		GeoCeDGPresentationPreferences preferences = new GeoCeDGPresentationPreferences(
				store, inherited(18, 18, 48, 20, 14, 10));

		assertArrayEquals(new int[] {12, 14, 28, 12, 12, 12}, values(preferences));
		assertEquals(Category.values().length, store.values.size());
		for (Category category : Category.values()) {
			assertEquals(ValueSource.FRESH_DEFAULT, preferences.source(category));
		}
		assertTrue(contains(GeoCeDGPresentationPreferences.supportedSizes(
				Category.TOOLBAR_ICON), 28));
	}

	@Test
	void existingProfileMigratesOnlyMissingValuesAndPreservesExplicitValues() {
		MemoryStore store = new MemoryStore();
		store.legacyUserPreferences = true;
		store.values.put(Category.MENU_FONT.preferenceKey(), "24");
		store.values.put(Category.ALGEBRA_FONT.preferenceKey(), "invalid");
		GeoCeDGPresentationPreferences.InheritedValues inherited =
				inherited(18, 18, 48, 20, 14, 10);
		GeoCeDGPresentationPreferences preferences =
				new GeoCeDGPresentationPreferences(store, inherited);

		assertArrayEquals(new int[] {18, 24, 48, 20, 14, 10}, values(preferences));
		assertEquals(ValueSource.EXPLICIT_PERSISTED,
				preferences.source(Category.MENU_FONT));
		assertEquals(ValueSource.MIGRATED_EFFECTIVE,
				preferences.source(Category.GENERAL_UI_FONT));
		assertEquals(ValueSource.MIGRATED_EFFECTIVE,
				preferences.source(Category.ALGEBRA_FONT));

		preferences.set(Category.GENERAL_UI_FONT, 20);
		preferences.set(Category.TOOLBAR_ICON, 56);
		GeoCeDGPresentationPreferences reloaded =
				new GeoCeDGPresentationPreferences(store, inherited);
		assertArrayEquals(new int[] {20, 24, 56, 20, 14, 10}, values(reloaded));
		for (Category category : Category.values()) {
			assertEquals(ValueSource.EXPLICIT_PERSISTED, reloaded.source(category));
		}
	}

	@Test
	void oneExistingS4KeySelectsConservativeMigrationNotFreshDefaults() {
		MemoryStore store = new MemoryStore();
		store.values.put(Category.GRAPHICS_FONT.preferenceKey(), "24");
		GeoCeDGPresentationPreferences preferences = new GeoCeDGPresentationPreferences(
				store, inherited(18, 18, 48, 20, 14, 10));

		assertArrayEquals(new int[] {18, 18, 48, 20, 14, 24}, values(preferences));
		assertEquals(ValueSource.EXPLICIT_PERSISTED,
				preferences.source(Category.GRAPHICS_FONT));
		assertEquals(ValueSource.MIGRATED_EFFECTIVE,
				preferences.source(Category.TOOLBAR_ICON));
	}

	@Test
	void presentationPanelUsesOneAlignedDescriptorAndValueGrid() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGPresentationOptionsPanel panel = new GeoCeDGPresentationOptionsPanel(app);
		GridBagLayout layout = assertInstanceOf(GridBagLayout.class, panel.getLayout());
		assertEquals(Category.values().length * 2, panel.getComponentCount());

		for (int index = 0; index < Category.values().length; index++) {
			Component label = panel.getComponent(index * 2);
			Component control = panel.getComponent(index * 2 + 1);
			assertInstanceOf(JLabel.class, label);
			assertInstanceOf(JComboBox.class, control);
			GridBagConstraints labelConstraints = layout.getConstraints(label);
			GridBagConstraints controlConstraints = layout.getConstraints(control);
			assertEquals(0, labelConstraints.gridx);
			assertEquals(1, controlConstraints.gridx);
			assertEquals(labelConstraints.gridy, controlConstraints.gridy);
			assertEquals(GridBagConstraints.LINE_START, labelConstraints.anchor);
			assertEquals(GridBagConstraints.LINE_START, controlConstraints.anchor);
			assertEquals(labelConstraints.insets.top, controlConstraints.insets.top);
			assertEquals(labelConstraints.insets.bottom, controlConstraints.insets.bottom);
		}
	}

	@Test
	void optionsApplySixIndependentLiveSizesWithoutDirtyingConstruction() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		try {
			GeoCeDGPresentationOptionsPanel panel = new GeoCeDGPresentationOptionsPanel(app);
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

			GuiManagerD manager = (GuiManagerD) app.getGuiManager();
			final AlgebraViewD algebra = manager.getAlgebraView();
			ConstructionProtocolViewD protocol =
					(ConstructionProtocolViewD) manager.getConstructionProtocolView();
			final JTable protocolTable =
					(JTable) protocol.getCpPanel().getViewport().getView();
			app.getEuclidianView2(1);
			JMenu constructionTextMenu = GeoCeDGHostMenuFactory.fontSize(app);
			assertEquals("Text font size", constructionTextMenu.getText());
			assertEquals(expected.get(Category.MENU_FONT).intValue(),
					constructionTextMenu.getFont().getSize());
			assertEquals(expected.get(Category.GENERAL_UI_FONT).intValue(),
					app.getPlainFont().getSize());
			assertEquals(expected.get(Category.TOOLBAR_ICON).intValue(),
					app.getToolbarIconSize());
			assertEquals(expected.get(Category.TOOLBAR_ICON).intValue(),
					app.getImageManager().getMaxIconSize());
			JToggleButton nativeTool = GeoCeDGToolbarContainer.createNativeToolReference(app);
			assertEquals(app.getToolbarIconSize(), nativeTool.getIcon().getIconWidth());
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
	void generalUiOwnsInputBarToolbarHelpAndOrdinaryDialogsLive() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		InputDialogD redefine = null;
		try {
			app.setPresentationSize(Category.MENU_FONT, 24);
			app.setPresentationSize(Category.ALGEBRA_FONT, 20);
			app.setPresentationSize(Category.CONSTRUCTION_PROTOCOL_FONT, 18);
			app.setPresentationSize(Category.GRAPHICS_FONT, 16);
			app.setPresentationSize(Category.TOOLBAR_ICON, 64);
			app.setPresentationSize(Category.GENERAL_UI_FONT, 12);

			GuiManagerD manager = (GuiManagerD) app.getGuiManager();
			AlgebraInputD input = (AlgebraInputD) manager.getAlgebraInput();
			assertEquals(12, input.getTextField().getFont().getSize());
			input.getTextField().setText("Circle((0,0),2)");
			assertEquals(12, input.getTextField().getFont().getSize());

			ToolbarContainer toolbar = manager.getToolbarPanel();
			toolbar.getToolbarHelpPanel();
			toolbar.updateFonts();
			JLabel commandHelp = descendants(toolbar.getToolbarHelpPanel(), JLabel.class)
					.get(0);
			assertEquals(12, commandHelp.getFont().getSize());
			app.setPresentationSize(Category.GENERAL_UI_FONT, 14);
			assertEquals(14, input.getTextField().getFont().getSize());
			assertEquals(14, commandHelp.getFont().getSize());

			redefine = new InputDialogD(app, "Definition", "Redefine", "x+1", true,
					(inputText, handler, callback) -> callback.callback(true));
			redefine.updateFonts();
			for (JTextComponent editor : descendants(
					redefine.getWrappedDialog().getContentPane(), JTextComponent.class)) {
				assertEquals(14, editor.getFont().getSize());
			}
			for (JButton button : descendants(
					redefine.getWrappedDialog().getContentPane(), JButton.class)) {
				assertEquals(14, button.getFont().getSize());
			}

			assertEquals(24, app.getMenuFont().getSize());
			assertEquals(20, app.getAlgebraFont().getSize());
			assertEquals(18, app.getConstructionProtocolFont().getSize());
			assertEquals(16, app.getEuclidianViewFontSize());
			assertEquals(14, app.getPlainFont().getSize());
			assertEquals(64, app.getToolbarIconSize());
			assertNotEquals(app.getToolbarIconSize(), app.getScaledIconSize());
		} finally {
			if (redefine != null) {
				disposeTestDialog(redefine);
			}
			restore(app, original);
		}
	}

	@Test
	void textDialogRetainsApplyOkCancelHelpAndContentDerivedEqualHeights() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		TextInputDialogD dialog = null;
		try {
			app.setPresentationSize(Category.GENERAL_UI_FONT, 12);
			app.setPresentationSize(Category.TOOLBAR_ICON, 64);
			dialog = new TextInputDialogD(app, app.getLocalization().getMenu("Text"),
					null, null, true, 30, 5, false);
			dialog.updateFonts();

			Map<String, JButton> buttons = new HashMap<>();
			for (JButton button : descendants(dialog.getButtonPanel(), JButton.class)) {
				buttons.put(button.getText(), button);
			}
			List<String> expected = List.of(app.getLocalization().getMenu("Apply"),
					app.getLocalization().getMenu("OK"),
					app.getLocalization().getMenu("Cancel"),
					app.getLocalization().getMenu("Help"));
			assertTrue(buttons.keySet().containsAll(expected));
			assertEquals(1, expected.stream().map(buttons::get)
					.map(JButton::getPreferredSize).map(Dimension::getHeight).distinct().count());
			assertEquals(12, buttons.get(app.getLocalization().getMenu("Help"))
					.getFont().getSize());
			assertNotEquals(app.getToolbarIconSize(),
					buttons.get(app.getLocalization().getMenu("Help")).getIcon().getIconWidth());
		} finally {
			if (dialog != null) {
				disposeTestDialog(dialog);
			}
			restore(app, original);
		}
	}

	@Test
	void persistentToolContainerHonorsNativeBottomAlignmentAtSupportedSizes() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		try {
			for (int size : new int[] {16, 28, 32, 64}) {
				app.setPresentationSize(Category.TOOLBAR_ICON, size);
				JToggleButton nativeButton =
						GeoCeDGToolbarContainer.createNativeToolReference(app);
				JToggleButton persistentButton = new JToggleButton(squareIcon(
						nativeButton.getIcon().getIconWidth()));
				GeoCeDGToolbarContainer.applyNativeToolPresentation(
						persistentButton, nativeButton);
				JPanel persistent = GeoCeDGUserTools.createPinnedContainer(
						nativeButton, true);
				persistent.add(persistentButton);
				JPanel nativeContainer = (JPanel) nativeButton.getParent();
				JPanel row = new JPanel();
				row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
				row.add(nativeContainer);
				row.add(persistent);
				Dimension preferred = row.getPreferredSize();
				row.setSize(preferred.width, preferred.height + 12);
				row.doLayout();
				nativeContainer.doLayout();
				persistent.doLayout();
				Point nativeOrigin = SwingUtilities.convertPoint(nativeContainer,
						nativeButton.getLocation(), row);
				Point persistentOrigin = SwingUtilities.convertPoint(persistent,
						persistentButton.getLocation(), row);
				assertEquals(nativeOrigin.y, persistentOrigin.y, "toolbar size " + size);
				assertEquals(nativeButton.getPreferredSize(),
						persistentButton.getPreferredSize(), "toolbar size " + size);
				assertEquals(nativeButton.getAlignmentY(),
						persistentButton.getAlignmentY(), "toolbar size " + size);
			}
		} finally {
			restore(app, original);
		}
	}

	@Test
	void scaledToolbarIconRetainsLogicalNativeGeometry() {
		ScaledIcon scaled = new ScaledIcon(new BufferedImage(56, 56,
				BufferedImage.TYPE_INT_ARGB), 2);
		assertEquals(28, scaled.getIconWidth());
		assertEquals(28, scaled.getIconHeight());
		JToggleButton nativeButton = new JToggleButton(scaled);
		JToggleButton persistentButton = new JToggleButton(squareIcon(28));
		GeoCeDGToolbarContainer.applyNativeToolPresentation(persistentButton, nativeButton);
		assertEquals(nativeButton.getPreferredSize(), persistentButton.getPreferredSize());
		assertEquals(nativeButton.getIcon().getIconWidth(),
				persistentButton.getIcon().getIconWidth());
	}

	@Test
	void constructionFontAndGeoTextRemainOutsideAllPresentationPreferences() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		try {
			GeoText text = (GeoText) G9U1TestApp.eval(app, "t=\"S4-R1\"");
			double logicalSize = text.getFontSize(app.getFontSize());
			int originalConstruction = app.getFontSize();

			for (Category category : Category.values()) {
				app.setPresentationSize(category,
						differentSupportedSize(category, app.getPresentationSize(category)));
				assertEquals(logicalSize, text.getFontSize(app.getFontSize()), category.name());
				assertEquals(originalConstruction, app.getFontSize(), category.name());
			}

			Map<Category, Integer> presentationBeforeConstructionChange = snapshot(app);
			app.setFontSize(10, true);
			assertEquals(10, app.getFontSize());
			assertEquals(presentationBeforeConstructionChange, snapshot(app));
		} finally {
			restore(app, original);
		}
	}

	@Test
	void specificOwnersWinOverGeneralUiAndToolbarRemainsFontIndependent() {
		AppGeoCeDG app = G9U1TestApp.create();
		Map<Category, Integer> original = snapshot(app);
		try {
			app.setPresentationSize(Category.MENU_FONT, 24);
			app.setPresentationSize(Category.ALGEBRA_FONT, 20);
			app.setPresentationSize(Category.CONSTRUCTION_PROTOCOL_FONT, 18);
			app.setPresentationSize(Category.GRAPHICS_FONT, 16);
			app.setPresentationSize(Category.TOOLBAR_ICON, 64);
			Map<Category, Integer> specific = snapshot(app);
			app.setPresentationSize(Category.GENERAL_UI_FONT, 12);
			assertEquals(24, app.getMenuFont().getSize());
			assertEquals(20, app.getAlgebraFont().getSize());
			assertEquals(18, app.getConstructionProtocolFont().getSize());
			assertEquals(16, app.getEuclidianViewFontSize());
			assertEquals(64, app.getToolbarIconSize());
			for (Category category : Category.values()) {
				if (category != Category.GENERAL_UI_FONT) {
					assertEquals(specific.get(category), app.getPresentationSize(category));
				}
			}

			app.setPresentationSize(Category.TOOLBAR_ICON, 28);
			assertEquals(12, app.getPresentationSize(Category.GENERAL_UI_FONT));
			app.setPresentationSize(Category.MENU_FONT, 14);
			assertEquals(28, app.getToolbarIconSize());
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
		assertEquals(classic.getScaledIconSize(), classic.getToolbarIconSize());
		assertNull(classic.newProductPresentationOptionsPanel());
	}

	private static GeoCeDGPresentationPreferences.InheritedValues inherited(
			int general, int menu, int toolbar, int algebra, int protocol, int graphics) {
		return new GeoCeDGPresentationPreferences.InheritedValues(general, menu,
				toolbar, algebra, protocol, graphics);
	}

	private static int[] values(GeoCeDGPresentationPreferences preferences) {
		return new int[] {
				preferences.get(Category.GENERAL_UI_FONT),
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

	private static boolean contains(int[] values, int expected) {
		for (int value : values) {
			if (value == expected) {
				return true;
			}
		}
		return false;
	}

	private static ImageIcon squareIcon(int size) {
		return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
	}

	private static <T extends Component> List<T> descendants(Container root,
			Class<T> type) {
		List<T> matches = new ArrayList<>();
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) {
				matches.add(type.cast(child));
			}
			if (child instanceof Container nested) {
				matches.addAll(descendants(nested, type));
			}
		}
		return matches;
	}

	private static void disposeTestDialog(InputDialogD dialog) {
		Window owner = dialog.getWrappedDialog().getOwner();
		dialog.getWrappedDialog().dispose();
		if (owner != null) {
			owner.dispose();
		}
	}

	private static final class MemoryStore
			implements GeoCeDGPresentationPreferences.Store {
		private final Map<String, String> values = new HashMap<>();
		private boolean legacyUserPreferences;

		@Override
		public String load(String key) {
			return values.get(key);
		}

		@Override
		public void save(String key, String value) {
			values.put(key, value);
		}

		@Override
		public boolean hasLegacyUserPreferences() {
			return legacyUserPreferences;
		}
	}
}

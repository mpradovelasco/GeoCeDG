/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.settings.AlgebraStyle;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.menubar.LanguageActionListener;
import org.geogebra.desktop.gui.menubar.OptionsMenuD;
import org.geogebra.desktop.gui.view.algebra.AlgebraControllerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraHelperBar;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;

/** Locale/help/definition/presentation policy stays separate from geometry. */
class G9U1ProductPolicyTest {

	@Test
	void documentFilterUsesProductBrandWithoutChangingClassic() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLocale(locale);
			assertEquals("GeoCeDG " + app.getLocalization().getMenu("Files"),
					((GuiManagerGeoCeDG) app.getGuiManager()).getDocumentOpenDescription());
		}
		AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
				new javax.swing.JPanel(), true);
		Method description = GuiManagerD.class.getDeclaredMethod("getDocumentOpenDescription");
		description.setAccessible(true);
		assertEquals("GeoGebra" + classic.getLocalization().getMenu("Files"),
				description.invoke(classic.getGuiManager()));
	}

	@Test
	void productLanguageIsEnglishOrSpanishWithEnglishFallback() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.forLanguageTag("es-MX"));
		assertEquals("es", app.getLocale().getLanguage());
		app.setLocale(Locale.FRENCH);
		assertEquals("en", app.getLocale().getLanguage());
		app.setLocale(Locale.ENGLISH);
		assertEquals("en", app.getLocale().getLanguage());
	}

	@Test
	void productLanguageChooserHasExactlyTwoExclusiveChoices() {
		AppGeoCeDG app = G9U1TestApp.create();
		JMenu languages = new JMenu();
		OptionsMenuD.addLanguageMenuItems(app, languages, new LanguageActionListener(app));
		assertEquals(2, languages.getMenuComponentCount());
		for (Component component : languages.getMenuComponents()) {
			assertTrue(component instanceof JRadioButtonMenuItem);
		}
		((JRadioButtonMenuItem) languages.getMenuComponent(1)).doClick();
		assertEquals("es", app.getLocale().getLanguage());
		assertFalse(((JRadioButtonMenuItem) languages.getMenuComponent(0)).isSelected());
	}

	@Test
	void readonlyDefinitionDoesNotChangeGlobalDescriptionOrConstruction() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement spline = G9U1TestApp.eval(app, "S=SplineV2({(0,0),(1,2),(2,0)},3)");
		AlgebraStyle style = app.getSettings().getAlgebra().getStyle();
		String xml = app.getXML();
		String definition = GeoCeDGDefinitionInspector.definition(spline);
		assertTrue(definition.contains("SplineV2"), definition);
		assertEquals(style, app.getSettings().getAlgebra().getStyle());
		assertEquals(xml, app.getXML());
		assertFalse(spline.isAlgebraViewEditable());
	}

	@Test
	void descriptionMenuReflectsOnlyCurrentStyleNotTreeSort() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		AlgebraViewD view = new AlgebraViewD(new AlgebraControllerD(app.getKernel()));
		AlgebraHelperBar bar = new AlgebraHelperBar(view, app);
		Field field = AlgebraHelperBar.class.getDeclaredField("descriptionMenu");
		field.setAccessible(true);
		for (AlgebraStyle style : AlgebraStyle.getAvailableValues(app)) {
			app.getSettings().getAlgebra().setStyle(style);
			bar.updateLabels();
			JPopupMenu menu = (JPopupMenu) field.get(bar);
			JRadioButtonMenuItem[] items = Arrays.stream(menu.getComponents())
					.filter(JRadioButtonMenuItem.class::isInstance)
					.map(JRadioButtonMenuItem.class::cast).toArray(JRadioButtonMenuItem[]::new);
			assertEquals(AlgebraStyle.getAvailableValues(app).size(), items.length);
			assertEquals(1, Arrays.stream(items).filter(JRadioButtonMenuItem::isSelected).count());
			assertEquals(app.getLocalization().getMenu(style.getTranslationKey()),
					Arrays.stream(items).filter(JRadioButtonMenuItem::isSelected)
							.findFirst().orElseThrow().getText());
		}
	}

	@Test
	void enabledSemanticCommandHelpMatchesThreePointMinimumInBothLanguages() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLocale(locale);
			String syntax = app.getKernel().getAlgebraProcessor().getSyntax(
					app.getLocalization().getCommandSyntax(), "SplineV2", app.getSettings());
			assertNotNull(syntax);
			String tail = syntax.substring(syntax.lastIndexOf('\n') + 1);
			assertEquals(3, tail.chars().filter(character -> character == '<').count());
		}
	}

	@Test
	void featureDisabledHelpCannotAdvertiseBlockedSemanticCommands() {
		G9U1TestApp.create();
		AppGeoCeDG disabled = new AppGeoCeDG(new CommandLineArguments(
				new String[] {"--silent"}), new javax.swing.JPanel());
		assertNull(disabled.getKernel().getAlgebraProcessor().getSyntax(
				disabled.getLocalization().getCommandSyntax(), "SplineV2", disabled.getSettings()));
		assertNotNull(disabled.getKernel().getAlgebraProcessor().getSyntax(
				disabled.getLocalization().getCommandSyntax(), "Line", disabled.getSettings()));
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.util.List;
import java.util.Locale;

import javax.swing.JTextField;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.settings.AlgebraStyle;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BCu6AlgebraPresentationTest {

	@Test
	void searchFindsVisibleNamesAndTracksLargeMutableTree() throws Exception {
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			GeoCeDGAlgebraView view = algebra(app);
			GeoElement first = G9U1TestApp.eval(app, "A_{12}=(1,2)");
			GeoElement second = G9U1TestApp.eval(app, "A_{120}=(3,4)");
			GeoElement accent = G9U1TestApp.eval(app, "café=7");
			for (int i = 0; i < 1500; i++) {
				new GeoNumeric(app.getKernel().getConstruction(), i)
						.setLabel("bulk_{" + i + "}");
			}
			assertEquals(1500, GeoCeDGAlgebraSearchBar.find(view.getModel().getRoot(),
					"bulk_").size());
			assertSame(accent, AlgebraTree.getGeoElementForPath(GeoCeDGAlgebraSearchBar
					.find(view.getModel().getRoot(), "cafe\u0301").get(0)));

			var search = (GeoCeDGAlgebraSearchBar) view.getHelperBar();
			JTextField field = searchField(search.getComponents());
			field.setText("a₁₂");
			String xml = app.getXML();
			field.postActionEvent();
			assertSame(first, AlgebraTree.getGeoElementForPath(view.getSelectionPath()));
			assertEquals(List.of(first), app.getSelectionManager().getSelectedGeos());
			field.postActionEvent();
			assertSame(second, AlgebraTree.getGeoElementForPath(view.getSelectionPath()));
			assertEquals(xml, app.getXML(), "search leaves the saved construction unchanged");
			first.remove();
			field.postActionEvent();
			assertSame(second, AlgebraTree.getGeoElementForPath(view.getSelectionPath()));
			return null;
		});
	}

	@Test
	void tooltipComplementsEffectiveDisplayAndBoundsUntrustedText() throws Exception {
		PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			app.setLocale(Locale.ENGLISH);
			GeoCeDGAlgebraView view = algebra(app);
			GeoElement input = G9U1TestApp.eval(app, "a=2");
			GeoElement value = G9U1TestApp.eval(app, "b=3*a");
			GeoElement undefined = G9U1TestApp.eval(app, "u=a/(a-a)");
			GeoElement text = G9U1TestApp.eval(app, "T=Text(\"<script>" + "x".repeat(300)
					+ "\"+a)");
			app.getSettings().getAlgebra().setStyle(AlgebraStyle.VALUE);
			assertTrue(view.complementaryToolTip(value).contains("Definition"));
			app.getSettings().getAlgebra().setStyle(AlgebraStyle.DEFINITION);
			assertTrue(view.complementaryToolTip(input).contains("Definition unavailable"));
			assertTrue(view.complementaryToolTip(value).contains("<b>Value</b><br>6"));
			assertFalse(undefined.isDefined());
			assertTrue(view.complementaryToolTip(undefined).contains("Undefined"));
			String popup = view.complementaryToolTip(text);
			assertTrue(popup.contains("&lt;script&gt;"));
			assertTrue(popup.contains("…"));
			assertFalse(popup.contains("<script>"));
			app.getSettings().getAlgebra().setStyle(AlgebraStyle.DESCRIPTION);
			app.setLocale(Locale.forLanguageTag("es"));
			assertTrue(view.complementaryToolTip(undefined).contains("Indefinido"));
			return null;
		});
	}

	private static GeoCeDGAlgebraView algebra(AppGeoCeDG app) {
		var view = (GeoCeDGAlgebraView) ((GuiManagerD) app.getGuiManager()).getAlgebraView();
		view.attachView();
		return view;
	}

	private static JTextField searchField(Component[] components) {
		for (Component component : components) {
			if (component instanceof JTextField field
					&& "geocedg.algebra.search".equals(field.getName())) {
				return field;
			}
		}
		throw new AssertionError("The Algebra helper bar has no search field");
	}
}

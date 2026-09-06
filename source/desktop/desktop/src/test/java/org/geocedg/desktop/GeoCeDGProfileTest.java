/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.gui.toolbar.ToolBar;
import org.geogebra.common.io.layout.DockPanelData;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GeoCeDGProfileTest {

	@BeforeAll
	static void initializeAwtFactory() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
	}

	@Test
	void compilesManifestToolbarIntoExistingGrammar() {
		String toolbar = GeoCeDGProfile.getToolbarDefinition();
		assertThat(GeoCeDGProfile.getProfileId(), equalTo("geocedg-desktop"));
		assertThat(GeoCeDGProfile.getActions(), hasSize(110));
		assertThat(ToolBar.parseToolbarString(toolbar), hasSize(9));
		assertThat(Arrays.asList(toolbar.split("[ |]+")),
				not(org.hamcrest.Matchers.hasItem("47")));
	}

	@Test
	void createsConservativeInitialPerspective() {
		Perspective perspective = GeoCeDGProfile.createInitialPerspective();
		Map<Integer, DockPanelData> panels = Arrays.stream(
				perspective.getDockPanelData()).collect(Collectors.toMap(
						DockPanelData::getViewId, panel -> panel));
		assertThat(panels.get(App.VIEW_EUCLIDIAN).isVisible(), is(true));
		assertThat(panels.get(App.VIEW_ALGEBRA).isVisible(), is(true));
		assertThat(panels.get(App.VIEW_EUCLIDIAN3D).isVisible(), is(false));
		assertThat(perspective.getId(), equalTo("cedg-construction"));
		assertThat(panels.get(App.VIEW_CONSTRUCTION_PROTOCOL).isVisible(), is(true));
		assertThat(perspective.isUserDefined(), is(false));
		assertThat(perspective.getShowGrid(), is(false));
		assertThat(perspective.getShowAxes(), is(true));
		assertThat(perspective.isUnitAxesRatio(), is(true));
	}

	@Test
	void usesTextualIdentityAndNoUpstreamSplash() {
		assertThat(GeoCeDGProductInfo.semanticVersion(), equalTo("0.9.0"));
		assertThat(GeoCeDGProductInfo.displayVersion(), equalTo("0.9"));
		assertThat(GeoCeDGFrame.APPLICATION_TITLE, equalTo("GeoCeDG 0.9"));
		assertThat(GeoCeDGProductInfo.windowTitle("Revision1.cedg"),
				equalTo("GeoCeDG 0.9 — Revision1.cedg"));
		assertThat(GeoCeDGFrame.APPLICATION_USER_MODEL_ID,
				equalTo("org.geocedg.desktop"));
		String splash = GeoCeDG.getSplashResource().toExternalForm();
		assertThat(splash, containsString(
				"/org/geocedg/desktop/branding/v1/derived/"
						+ "geocedg-startup-361x480.png"));
		assertThat(splash, not(containsString("/org/geogebra/desktop/splash.png")));
	}

	@Test
	void isolatesDefaultPreferencesPath() {
		assertThat(GeoCeDG.getDefaultPreferencesFile().toString(),
				containsString("GeoCeDG"));
	}

	@Test
	void exposesExperimentalDxfOnlyThroughGeoCeDGMenu() {
		assertThat(GeoCeDGMenuBar.DXF_ACTION_TEXT,
				equalTo("Export 2D geometry as DXF (experimental)..."));
		assertThat(GeoCeDGMenuBar.PRODUCT_MENU_MNEMONIC, equalTo((int) 'G'));
		assertThat(GeoCeDGMenuBar.DXF_ACTION_ACCELERATOR.getKeyCode(),
				equalTo(KeyEvent.VK_D));
		int requiredModifiers = InputEvent.CTRL_DOWN_MASK
				| InputEvent.SHIFT_DOWN_MASK;
		assertThat(GeoCeDGMenuBar.DXF_ACTION_ACCELERATOR.getModifiers()
				& requiredModifiers, equalTo(requiredModifiers));
		assertNativeLocusV2SaveBoundary();
	}

	private static void assertNativeLocusV2SaveBoundary() {
		AppCommon app = AppCommonFactory.create();
		GeoNumeric legacy = new GeoNumeric(app.getKernel().getConstruction(), 1);
		legacy.setLabel("legacyNumber");
		assertThat(GeoCeDGExternalCompatibilityWarning.containsNativeLocusV2(
				app.getKernel().getConstruction()), is(false));

		GeoLocusV2 locus = new GeoLocusV2(
				app.getKernel().getConstruction(), "test-locus-identity");
		locus.setLabel("nativeLocusV2");
		assertThat(GeoCeDGExternalCompatibilityWarning.containsNativeLocusV2(
				app.getKernel().getConstruction()), is(true));
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.settings.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.selector.CommandFilter;
import org.junit.jupiter.api.Test;

class AppConfigGeoCeDGTest {

	private final AppConfigGeoCeDG config = new AppConfigGeoCeDG();

	@Test
	void hasIndependentProductIdentityAndPreferences() {
		assertThat(AppConfigGeoCeDG.PROFILE_ID, equalTo("geocedg-desktop"));
		assertThat(config.getAppName(), equalTo("GeoCeDG"));
		assertThat(config.getPreferencesKey(), equalTo("geocedg"));
	}

	@Test
	void preservesClassicSerializationAppCode() {
		assertThat(config.getAppCode(), equalTo(GeoGebraConstants.CLASSIC_APPCODE));
	}

	@Test
	void gatesOnlyDedicatedLocusV2CommandsByDefault() {
		CommandFilter defaultFilter = config.createCommandFilter();
		assertThat(defaultFilter.isCommandAllowed(Commands.Point), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.Locus), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.Length), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.Intersect), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.LocusV2),
				equalTo(false));
		assertThat(defaultFilter.isCommandAllowed(Commands.LocusLength),
				equalTo(false));

		CommandFilter enabledFilter = new AppConfigGeoCeDG(true)
				.createCommandFilter();
		assertThat(enabledFilter.isCommandAllowed(Commands.LocusV2),
				equalTo(true));
		assertThat(enabledFilter.isCommandAllowed(Commands.LocusLength),
				equalTo(true));
	}
}

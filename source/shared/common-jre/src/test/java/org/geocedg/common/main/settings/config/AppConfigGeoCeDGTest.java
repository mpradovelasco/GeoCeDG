/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.settings.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.geogebra.common.GeoGebraConstants;
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
	void startsWithoutCommandRestrictions() {
		assertThat(config.createCommandFilter(), nullValue());
	}
}

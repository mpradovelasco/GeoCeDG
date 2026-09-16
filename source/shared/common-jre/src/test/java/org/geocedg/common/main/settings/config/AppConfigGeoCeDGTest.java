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
	void promotesDedicatedLocusV2CommandsByDefault() {
		CommandFilter defaultFilter = config.createCommandFilter();
		assertThat(defaultFilter.isCommandAllowed(Commands.Point), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.Locus), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.Length), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.Intersect), equalTo(true));
		// PRE-G9B-P1 promoted the approved public surface to a product default.
		assertThat(defaultFilter.isCommandAllowed(Commands.LocusV2), equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.LocusLength),
				equalTo(true));
		assertThat(defaultFilter.isCommandAllowed(Commands.SplineV2), equalTo(true));
	}

	@Test
	void retainsExplicitDiagnosticOverrideThatDisablesCreation() {
		CommandFilter disabledFilter = new AppConfigGeoCeDG(false)
				.createCommandFilter();
		assertThat(disabledFilter.isCommandAllowed(Commands.LocusV2), equalTo(false));
		assertThat(disabledFilter.isCommandAllowed(Commands.LocusLength),
				equalTo(false));
		assertThat(disabledFilter.isCommandAllowed(Commands.SplineV2), equalTo(false));
		// Ordinary upstream commands are never gated by the product default.
		assertThat(disabledFilter.isCommandAllowed(Commands.Point), equalTo(true));
		assertThat(disabledFilter.isCommandAllowed(Commands.Locus), equalTo(true));
	}

	@Test
	void promotesLocusV2AndExtendedDxfIndependently() {
		assertThat(AppConfigGeoCeDG.DEFAULT_LOCUS_V2_CREATION_ENABLED, equalTo(true));
		assertThat(AppConfigGeoCeDG.DEFAULT_EXTENDED_DXF_ENABLED, equalTo(true));
		assertThat(config.getRuntimeFeatureService().isLocusV2CreationEnabled(),
				equalTo(true));
		assertThat(config.getRuntimeFeatureService().isExtendedDxfEnabled(),
				equalTo(true));
		// Neither default implies the other: all four combinations stay expressible.
		assertThat(new AppConfigGeoCeDG(false, true).getRuntimeFeatureService()
				.isLocusV2CreationEnabled(), equalTo(false));
		assertThat(new AppConfigGeoCeDG(false, true).getRuntimeFeatureService()
				.isExtendedDxfEnabled(), equalTo(true));
		assertThat(new AppConfigGeoCeDG(true, false).getRuntimeFeatureService()
				.isLocusV2CreationEnabled(), equalTo(true));
		assertThat(new AppConfigGeoCeDG(true, false).getRuntimeFeatureService()
				.isExtendedDxfEnabled(), equalTo(false));
	}
}

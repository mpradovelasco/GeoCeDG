/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.settings.config;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.kernel.commands.selector.CommandFilter;
import org.geogebra.common.main.settings.config.AppConfigDefault;

/**
 * Application configuration for the GeoCeDG Desktop profile.
 */
public final class AppConfigGeoCeDG extends AppConfigDefault {

	/** Stable product profile ID; it is not a persisted .ggb app code. */
	public static final String PROFILE_ID = "geocedg-desktop";
	/** User-visible provisional product name. */
	public static final String APPLICATION_NAME = "GeoCeDG";
	/** Preferences key used by the profile contract. */
	public static final String PREFERENCES_KEY = "geocedg";
	private final RuntimeFeatureService runtimeFeatureService;

	/** Creates the default-off GeoCeDG profile. */
	public AppConfigGeoCeDG() {
		this(false);
	}

	/**
	 * @param locusV2CreationEnabled explicit experimental-surface opt-in
	 */
	public AppConfigGeoCeDG(boolean locusV2CreationEnabled) {
		runtimeFeatureService = new RuntimeFeatureService(
				locusV2CreationEnabled);
	}

	/**
	 * @return application-owned runtime feature authority
	 */
	public RuntimeFeatureService getRuntimeFeatureService() {
		return runtimeFeatureService;
	}

	@Override
	public String getAppTitle() {
		return APPLICATION_NAME;
	}

	@Override
	public String getAppName() {
		return APPLICATION_NAME;
	}

	@Override
	public String getAppNameShort() {
		return APPLICATION_NAME;
	}

	@Override
	public String getAppNameWithoutCalc() {
		return APPLICATION_NAME;
	}

	@Override
	public String getPreferencesKey() {
		return PREFERENCES_KEY;
	}

	@Override
	public String getAppCode() {
		return GeoGebraConstants.CLASSIC_APPCODE;
	}

	@Override
	public CommandFilter createCommandFilter() {
		return runtimeFeatureService::isCommandVisible;
	}
}

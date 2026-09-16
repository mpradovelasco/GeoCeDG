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

	/**
	 * PRE-G9B-P1 product default: the author-approved Locus V2/SplineV2 public
	 * construction surface is normal GeoCeDG behavior. Promotion changes default
	 * exposure only; kernel semantics, identity and persistence are unchanged, and
	 * Classic never reaches this profile.
	 */
	public static final boolean DEFAULT_LOCUS_V2_CREATION_ENABLED = true;
	/**
	 * PRE-G9B-P1 product default: the author-approved G9X1 extended DXF export is
	 * normal GeoCeDG behavior. This decision is deliberately independent of
	 * {@link #DEFAULT_LOCUS_V2_CREATION_ENABLED}; neither implies the other.
	 */
	public static final boolean DEFAULT_EXTENDED_DXF_ENABLED = true;

	private final RuntimeFeatureService runtimeFeatureService;

	/** Creates the GeoCeDG profile with both promoted product defaults. */
	public AppConfigGeoCeDG() {
		this(DEFAULT_LOCUS_V2_CREATION_ENABLED, DEFAULT_EXTENDED_DXF_ENABLED);
	}

	/**
	 * @param locusV2CreationEnabled explicit Locus V2 creation policy; the extended
	 *        DXF policy keeps its independent product default
	 */
	public AppConfigGeoCeDG(boolean locusV2CreationEnabled) {
		this(locusV2CreationEnabled, DEFAULT_EXTENDED_DXF_ENABLED);
	}

	/**
	 * @param locusV2CreationEnabled explicit Locus V2 opt-in
	 * @param extendedDxfEnabled explicit G9X1 DXF opt-in
	 */
	public AppConfigGeoCeDG(boolean locusV2CreationEnabled,
			boolean extendedDxfEnabled) {
		runtimeFeatureService = new RuntimeFeatureService(
				locusV2CreationEnabled, extendedDxfEnabled);
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
	public boolean scalesConstructionTextWithEuclidianView() {
		return true;
	}

	@Override
	public CommandFilter createCommandFilter() {
		return runtimeFeatureService::isCommandVisible;
	}
}

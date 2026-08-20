/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.main.feature;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.MyError;

/**
 * Runtime authority for experimental GeoCeDG surface creation.
 *
 * <p>Creation and preservation are deliberately separate. The feature flag
 * controls new commands and tools, while file loading may reconstruct already
 * persisted native objects without making the feature interactively available.
 */
public final class RuntimeFeatureService {

	/** Durable feature-manifest identifier. */
	public static final String LOCUS_V2_FEATURE_ID = "cedg.locus.v2";
	/** Desktop opt-in argument, used as {@code --enableLocusV2=true}. */
	public static final String LOCUS_V2_ARGUMENT = "enableLocusV2";

	private final boolean locusV2CreationEnabled;
	private BooleanSupplier preservationContext = () -> false;

	/**
	 * @param locusV2CreationEnabled whether the experimental public surface is enabled
	 */
	public RuntimeFeatureService(boolean locusV2CreationEnabled) {
		this.locusV2CreationEnabled = locusV2CreationEnabled;
	}

	/**
	 * Binds the application-owned file-loading state after its kernel exists.
	 *
	 * @param preservationContext true only while native construction data is loading
	 */
	public void bindPreservationContext(BooleanSupplier preservationContext) {
		this.preservationContext = Objects.requireNonNull(preservationContext);
	}

	/**
	 * @return whether interactive Locus V2 creation is enabled for this profile
	 */
	public boolean isLocusV2CreationEnabled() {
		return locusV2CreationEnabled;
	}

	/**
	 * Dynamic command-discovery filter. Loading may resolve persisted experimental
	 * commands, but the processors still recheck access because they are cached.
	 *
	 * @param command command being discovered or resolved
	 * @return whether the command may currently resolve
	 */
	public boolean isCommandVisible(Commands command) {
		return !isDedicatedLocusV2Command(command)
				|| locusV2CreationEnabled || preservationContext.getAsBoolean();
	}

	/**
	 * @param construction authoritative construction context
	 * @return whether a new V2/rich/semantic-position algorithm may be created
	 */
	public static boolean mayCreateLocusV2(Construction construction) {
		if (construction == null || construction.getApplication() == null
				|| !(construction.getApplication().getConfig()
						instanceof AppConfigGeoCeDG)) {
			return false;
		}
		AppConfigGeoCeDG config = (AppConfigGeoCeDG) construction
				.getApplication().getConfig();
		return config.getRuntimeFeatureService().isLocusV2CreationEnabled();
	}

	/**
	 * @param construction authoritative construction context
	 * @return whether processors may create or reconstruct V2 objects now
	 */
	public static boolean mayUseLocusV2(Construction construction) {
		return construction != null
				&& (mayCreateLocusV2(construction) || construction.isFileLoading());
	}

	/**
	 * Enforces the processor boundary independently of command-table caching.
	 *
	 * @param construction authoritative construction context
	 * @throws MyError localized feature-unavailable failure
	 */
	public static void requireLocusV2Access(Construction construction)
			throws MyError {
		if (!mayUseLocusV2(construction)) {
			Localization localization = construction.getApplication()
					.getLocalization();
			throw new MyError(localization,
					localization.getMenu("LocusV2.FeatureDisabled"));
		}
	}

	/**
	 * @param command command identifier
	 * @return whether it belongs exclusively to the experimental V2 surface
	 */
	public static boolean isDedicatedLocusV2Command(Commands command) {
		return command == Commands.LocusV2 || command == Commands.LocusLength;
	}
}

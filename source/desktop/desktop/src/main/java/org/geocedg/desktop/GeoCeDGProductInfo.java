/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.desktop.export.GeoCeDGBuildProvenance;

/** Product identity derived from the packaged build provenance. */
final class GeoCeDGProductInfo {

	/** Product name; equal to the package-profile schema constant (PM-1). */
	static final String PRODUCT_NAME = "GeoCeDG";
	private static final GeoCeDGBuildProvenance BUILD =
			GeoCeDGBuildProvenance.load();

	private GeoCeDGProductInfo() {
		// Static product authority.
	}

	/** @return the embedded build provenance of the running Desktop classes */
	static GeoCeDGBuildProvenance provenance() {
		return BUILD;
	}

	static String semanticVersion() {
		return BUILD.getApplicationVersion();
	}

	/**
	 * PRE-G9B-R6-plus-G (PM-3): the canonical version date designated for the product
	 * version, never a build or commit time.
	 *
	 * @return {@code YYYY-MM-DD}, or null when the package profile declares none
	 */
	static String versionDate() {
		return BUILD.getApplicationVersionDate();
	}

	static String displayVersion() {
		return BUILD.getDisplayApplicationVersion();
	}

	static String applicationTitle() {
		return PRODUCT_NAME + " " + displayVersion();
	}

	static String windowTitle(String fileName) {
		return fileName == null || fileName.isBlank()
				? applicationTitle() : applicationTitle() + " \u2014 " + fileName;
	}
}

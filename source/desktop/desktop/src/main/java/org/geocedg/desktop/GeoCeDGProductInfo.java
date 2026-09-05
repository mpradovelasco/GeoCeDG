/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.desktop.export.GeoCeDGBuildProvenance;

/** Product identity derived from the packaged build provenance. */
final class GeoCeDGProductInfo {

	private static final String PRODUCT_NAME = "GeoCeDG";
	private static final GeoCeDGBuildProvenance BUILD =
			GeoCeDGBuildProvenance.load();

	private GeoCeDGProductInfo() {
		// Static product authority.
	}

	static String semanticVersion() {
		return BUILD.getApplicationVersion();
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

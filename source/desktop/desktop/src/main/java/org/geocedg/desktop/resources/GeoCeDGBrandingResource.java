/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.resources;

import java.net.URL;

import org.geogebra.desktop.util.ImageResourceD;

/** Versioned GeoCeDG-owned Desktop branding resources. */
public enum GeoCeDGBrandingResource implements ImageResourceD {

	APPLICATION_ICON("geocedg.brand.topbar",
			"derived/geocedg-application-icon-64.png"),
	STARTUP_SPLASH("geocedg.brand.startup",
			"derived/geocedg-startup-361x480.png");

	private static final String RESOURCE_ROOT =
			"/org/geocedg/desktop/branding/v1/";

	private final String logicalId;
	private final String resourceName;

	GeoCeDGBrandingResource(String logicalId, String resourceName) {
		this.logicalId = logicalId;
		this.resourceName = resourceName;
	}

	/**
	 * @return stable product-profile role represented by this resource
	 */
	public String getLogicalId() {
		return logicalId;
	}

	@Override
	public String getFilename() {
		return RESOURCE_ROOT + resourceName;
	}

	/**
	 * Resolve the declared tracked resource without an inherited-brand fallback.
	 *
	 * @return resource URL
	 * @throws IllegalStateException if the product resource is absent
	 */
	public URL getRequiredUrl() {
		URL resource = GeoCeDGBrandingResource.class.getResource(getFilename());
		if (resource == null) {
			throw new IllegalStateException(
					"Required GeoCeDG branding resource is missing: " + getFilename());
		}
		return resource;
	}
}

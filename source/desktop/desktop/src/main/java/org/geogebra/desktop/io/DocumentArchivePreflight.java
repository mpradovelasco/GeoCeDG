/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * Modified for GeoCeDG: disposable native-document validation.
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.desktop.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.main.LocalizationD;

/** Validates an archive in a disposable kernel before the live app is touched. */
public final class DocumentArchivePreflight {

	private DocumentArchivePreflight() {
	}

	/**
	 * @param archive immutable archive bytes
	 * @param config fresh product configuration
	 * @return whether the existing reader accepted the whole archive
	 * @throws IOException for invalid archive input
	 * @throws XMLParseException for invalid XML
	 */
	public static boolean validate(byte[] archive, AppConfig config)
			throws IOException, XMLParseException {
		Log previousLogger = Log.getLogger();
		AppDNoGui scratch;
		try {
			scratch = new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
		if (config instanceof AppConfigGeoCeDG) {
			((AppConfigGeoCeDG) config).getRuntimeFeatureService()
					.bindPreservationContext(() -> scratch.getKernel()
							.getConstruction().isFileLoading());
		}
		return GFileHandler.loadXML(scratch,
				new ByteArrayInputStream(archive), false);
	}
}

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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geocedg.common.document.controls.ControlLayoutException;
import org.geocedg.common.document.controls.ControlLayoutXml;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.io.MyXMLio;
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
		return validate(archive, config, true);
	}

	/** Validates the native document and enforces the caller's archive route. */
	public static boolean validate(byte[] archive, AppConfig config, boolean allowControlLayout)
			throws IOException, XMLParseException {
		boolean hasLayout = hasControlLayout(archive);
		if (hasLayout && !allowControlLayout) {
			throw new ControlLayoutException(ControlLayoutException.Code.CONTEXT,
					"Control layout requires a native .cedg document");
		}
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
		try (var input = new ByteArrayInputStream(archive)) {
			return hasLayout ? GFileHandler.loadPreflightedNativeXML(scratch, input)
					: GFileHandler.loadXML(scratch, input, false);
		}
	}

	/** @return whether the actual main XML carries a layout or slot marker */
	public static boolean hasControlLayout(byte[] archive) throws IOException, XMLParseException {
		return hasControlLayout(new ByteArrayInputStream(archive));
	}

	/** Scans the source archive and closes the supplied input stream. */
	public static boolean hasControlLayout(InputStream input) throws IOException, XMLParseException {
		boolean marked = false;
		int constructionEntries = 0;
		try (ZipInputStream zip = new ZipInputStream(input)) {
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				String name = entry.getName();
				if (MyXMLio.XML_FILE.equals(name)) {
					constructionEntries++;
					marked |= ControlLayoutXml.inspect(new String(zip.readAllBytes(),
							StandardCharsets.UTF_8));
				} else if ((MyXMLio.XML_FILE_MACRO.equals(name)
						|| MyXMLio.XML_FILE_DEFAULTS_2D.equals(name)
						|| MyXMLio.XML_FILE_DEFAULTS_3D.equals(name))
						&& ControlLayoutXml.inspect(new String(zip.readAllBytes(),
								StandardCharsets.UTF_8))) {
					throw new ControlLayoutException(ControlLayoutException.Code.CONTEXT,
							"Control layout belongs only to the main document entry");
				}
			}
		}
		if (marked && constructionEntries != 1) {
			throw new ControlLayoutException(ControlLayoutException.Code.DUPLICATE,
					"Multiple main XML entries in a control layout document");
		}
		return marked;
	}
}

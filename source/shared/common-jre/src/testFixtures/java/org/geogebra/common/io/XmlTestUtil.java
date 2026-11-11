/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 * 
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.util.debug.Log;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlTestUtil {

	private static final String GEOGEBRA_XSD_URL = "https://www.geogebra.org/apps/xsd/ggb.xsd";
	private static final int SCHEMA_DOWNLOAD_ATTEMPTS = 3;
	private static final int SCHEMA_TIMEOUT_MILLIS = 10000;
	private static Validator VALIDATOR;
	private static IllegalStateException validatorLoadFailure;

	/**
	 * Validate app's state against XML schema.
	 *
	 * @param application
	 *            app
	 */
	public static void checkCurrentXML(AppCommon application) {
		checkXML(application.getXML());
	}

	/**
	 * Validate XML against GGB schema.
	 * @param xml XML content
	 */
	public static void checkXML(String xml) {
		try {
			Validator validator = getValidator();
			Source xmlFile = new StreamSource(new StringReader(xml));
			validator.validate(xmlFile);
		} catch (SAXParseException se) {
			int l = se.getLineNumber();
			String[] rows = xml.split("\\n");
			for (int i = l - 2; i < l + 3 && i > 0 && i < rows.length; i++) {
				Log.debug(rows[i]);
			}
			throw new AssertionError(se.getLocalizedMessage(), se);
		} catch (Exception e) {
			Log.debug(e);
			throw new AssertionError(e.getLocalizedMessage(), e);
		}
	}

	private static Validator getValidator() {
		if (VALIDATOR != null) {
			return VALIDATOR;
		}
		if (validatorLoadFailure != null) {
			throw validatorLoadFailure;
		}
		try {
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			byte[] xsd = downloadSchema(new URL(GEOGEBRA_XSD_URL));
			Schema schema = schemaFactory.newSchema(
					new StreamSource(new ByteArrayInputStream(xsd), GEOGEBRA_XSD_URL));
			VALIDATOR = schema.newValidator();
		} catch (IOException | SAXException | IllegalArgumentException e) {
			validatorLoadFailure = new IllegalStateException(
					"Unable to load XML validation schema from " + GEOGEBRA_XSD_URL, e);
			throw validatorLoadFailure;
		}
		return VALIDATOR;
	}

	private static byte[] downloadSchema(URL url) throws IOException {
		for (int attempt = 1; ; attempt++) {
			try {
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(SCHEMA_TIMEOUT_MILLIS);
				connection.setReadTimeout(SCHEMA_TIMEOUT_MILLIS);
				try (InputStream stream = connection.getInputStream()) {
					return stream.readAllBytes();
				}
			} catch (IOException e) {
				if (attempt == SCHEMA_DOWNLOAD_ATTEMPTS) {
					throw e;
				}
			}
		}
	}
}

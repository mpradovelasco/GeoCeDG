/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.jre.io;

import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.geocedg.common.kernel.geos.LegacyStyleDefaults;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.ConstructionDefaults;

/** XML boundary for legacy personal defaults. It never opens a construction reader. */
public final class LegacyStyleDefaultsReader {
	private static final int MAX_CHARS = 256 * 1024;
	private static final Set<String> HEADER = Set.of("format", "version", "app",
			"subApp", "platform", "id", "noNamespaceSchemaLocation");

	private LegacyStyleDefaultsReader() {
	}

	/**
	 * @param xml legacy object preferences, or null when absent
	 * @param prototypes supported default types
	 * @return the complete validated appearance snapshot
	 * @throws XMLParseException if the envelope or any style field is invalid
	 */
	public static LegacyStyleDefaults read(String xml, ConstructionDefaults prototypes)
			throws XMLParseException {
		if (xml == null) {
			return LegacyStyleDefaults.empty();
		}
		if (xml.length() > MAX_CHARS) {
			throw new XMLParseException("Personal construction defaults exceed size limit");
		}
		XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		try {
			XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
			try {
				return read(reader, prototypes);
			} finally {
				reader.close();
			}
		} catch (XMLStreamException | IllegalArgumentException failure) {
			throw new XMLParseException("Invalid personal construction defaults", failure);
		}
	}

	private static LegacyStyleDefaults read(XMLStreamReader reader, ConstructionDefaults prototypes)
			throws XMLStreamException {
		LegacyStyleDefaults.Decoder styles = null;
		boolean defaultsSeen = false;
		int depth = 0;
		Set<String> fields = new HashSet<>();
		while (reader.hasNext()) {
			switch (reader.next()) {
			case XMLStreamConstants.START_ELEMENT:
				String name = reader.getLocalName();
				require(noNamespace(reader.getNamespaceURI()), "namespaced element");
				Map<String, String> attrs = attributes(reader, depth);
				switch (depth++) {
				case 0:
					require("geogebra".equals(name) && HEADER.containsAll(attrs.keySet()),
							"expected geogebra header");
					double format = Double.parseDouble(attrs.getOrDefault("format", "5.0"));
					require(Double.isFinite(format) && format > 0, "file format");
					styles = new LegacyStyleDefaults.Decoder(prototypes, format);
					break;
				case 1:
					require("defaults".equals(name) && attrs.isEmpty() && !defaultsSeen,
							"expected one defaults section");
					defaultsSeen = true;
					break;
				case 2:
					require("element".equals(name) && attrs.keySet().equals(
							Set.of("default", "type", "label")), "expected a typed prototype");
					styles.startPrototype(Integer.parseInt(attrs.get("default")), attrs.get("type"));
					fields.clear();
					break;
				case 3:
					require(fields.add(name), "duplicate prototype field");
					styles.leaf(name, attrs);
					break;
				default:
					throw new IllegalArgumentException("nested prototype field");
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (--depth == 2) {
					styles.endPrototype();
				}
				break;
			case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA:
				require(reader.getText().trim().isEmpty(), "unexpected text");
				break;
			case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE,
					XMLStreamConstants.PROCESSING_INSTRUCTION:
				throw new IllegalArgumentException("unsupported XML directive");
			default:
				break;
			}
		}
		require(defaultsSeen && depth == 0, "incomplete defaults");
		return styles.finish();
	}

	private static Map<String, String> attributes(XMLStreamReader reader, int depth) {
		Map<String, String> attributes = new LinkedHashMap<>();
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String name = reader.getAttributeLocalName(i);
			String namespace = reader.getAttributeNamespace(i);
			require(noNamespace(namespace) || depth == 0 && "noNamespaceSchemaLocation".equals(name)
					&& "http://www.w3.org/2001/XMLSchema-instance".equals(namespace),
					"namespaced attribute");
			require(attributes.put(name, reader.getAttributeValue(i)) == null, "duplicate attribute");
		}
		return attributes;
	}

	private static boolean noNamespace(String uri) {
		return uri == null || uri.isEmpty();
	}

	private static void require(boolean valid, String context) {
		if (!valid) {
			throw new IllegalArgumentException(context);
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.layers.HiddenLayerMetadataException.Code;
import org.geogebra.common.io.XMLStringBuilder;

/**
 * PRE-G9B-R6-plus-A-2 (DQ-A2-3, DQ-A2-4): writer and strict reader of the flat,
 * versioned, self-closing document-level element
 *
 * <pre>
 * hidden-element = "&lt;geocedgHiddenLayers" SP 'version="1"'
 *                  SP 'layers="' layer-list '"' "/&gt;"
 * layer-list     = layer *( SP layer )       ; strictly ascending, no duplicates
 * layer          = "0" / ( %x31-39 *DIGIT )  ; no sign, no leading zero, &lt;= max
 * </pre>
 *
 * The writer emits nothing for an empty set. The reader validates a recognized
 * element completely or throws {@link HiddenLayerMetadataException}.
 */
public final class HiddenLayersXml {
	/** Element name. */
	public static final String ELEMENT = "geocedgHiddenLayers";
	/** Highest supported version. */
	public static final int VERSION = 1;

	static final String VERSION_ATTRIBUTE = "version";
	static final String LAYERS_ATTRIBUTE = "layers";

	private static final int MAX_LAYER_DIGITS = 9;

	private HiddenLayersXml() {
	}

	/**
	 * Appends the element on its own line as a direct child of {@code <geogebra>}, or
	 * nothing for an empty set.
	 *
	 * @param sb full document XML builder
	 * @param hiddenLayers set to write
	 */
	public static void write(XMLStringBuilder sb, HiddenLayerSet hiddenLayers) {
		if (hiddenLayers == null || hiddenLayers.isEmpty()) {
			return;
		}
		StringBuilder list = new StringBuilder();
		for (int layer : hiddenLayers.toList()) {
			if (list.length() > 0) {
				list.append(' ');
			}
			list.append(layer);
		}
		sb.startTag(ELEMENT, 0);
		sb.attr(VERSION_ATTRIBUTE, VERSION);
		sb.attr(LAYERS_ATTRIBUTE, list.toString());
		sb.endTag();
	}

	/**
	 * Reads one recognized element; a newer version is rejected before any other
	 * attribute is interpreted.
	 *
	 * @param attributes element attributes
	 * @param maxLayer highest admissible layer of the reading product
	 * @return the validated non-empty set
	 * @throws HiddenLayerMetadataException for every fail-closed case of DQ-A2-4
	 */
	public static HiddenLayerSet read(Map<String, String> attributes, int maxLayer) {
		int version = readVersion(attributes.get(VERSION_ATTRIBUTE));
		if (version > VERSION) {
			throw new HiddenLayerMetadataException(Code.UNSUPPORTED_VERSION,
					"version " + attributes.get(VERSION_ATTRIBUTE) + " is newer than " + VERSION);
		}
		for (String name : attributes.keySet()) {
			if (!VERSION_ATTRIBUTE.equals(name) && !LAYERS_ATTRIBUTE.equals(name)) {
				throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT,
						"unknown attribute " + name);
			}
		}
		String text = attributes.get(LAYERS_ATTRIBUTE);
		if (text == null || text.isEmpty()) {
			throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT, "missing layers");
		}
		List<Integer> layers = new ArrayList<>();
		int previous = -1;
		for (String token : text.split(" ", -1)) {
			if (!isCanonicalLayer(token)) {
				throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT,
						"malformed layer list " + text);
			}
			if (token.length() > MAX_LAYER_DIGITS || Integer.parseInt(token) > maxLayer) {
				throw new HiddenLayerMetadataException(Code.OUT_OF_DOMAIN_LAYER,
						"layer " + token + " outside 0.." + maxLayer);
			}
			int layer = Integer.parseInt(token);
			if (layer <= previous) {
				throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT,
						"layers not strictly ascending: " + text);
			}
			layers.add(layer);
			previous = layer;
		}
		HiddenLayerSet set = HiddenLayerSet.of(layers);
		if (set.coversDomain(maxLayer)) {
			throw new HiddenLayerMetadataException(Code.ALL_LAYERS_HIDDEN,
					"every layer 0.." + maxLayer + " is hidden");
		}
		return set;
	}

	private static boolean isCanonicalLayer(String token) {
		if (token.isEmpty()) {
			return false;
		}
		if ("0".equals(token)) {
			return true;
		}
		if (token.charAt(0) < '1' || token.charAt(0) > '9') {
			return false;
		}
		for (int i = 1; i < token.length(); i++) {
			char c = token.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	private static int readVersion(String text) {
		if (text == null || text.isEmpty()) {
			throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT, "missing version");
		}
		long value = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c < '0' || c > '9') {
				throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT,
						"non-integer version " + text);
			}
			value = Math.min(Integer.MAX_VALUE, value * 10 + (c - '0'));
		}
		if (value < 1) {
			throw new HiddenLayerMetadataException(Code.MALFORMED_ELEMENT, "version " + text);
		}
		return (int) value;
	}
}

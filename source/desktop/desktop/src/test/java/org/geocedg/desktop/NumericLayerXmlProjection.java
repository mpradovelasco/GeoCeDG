/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.GeoClass;

/**
 * Verifies the additive numeric-layer amendment before a historic byte comparison.
 * The counterfactual is the same live construction writer with the additive
 * numeric-layer decision disabled. The pre-existing layer branch still runs.
 */
final class NumericLayerXmlProjection {
	private static final Pattern NUMERIC_OWNER = Pattern.compile(
			"(?ms)^<element type=\"numeric\"( label=\"[^\"\\r\\n]*\")[^\\r\\n]*>\\n"
					+ "(.*?)^</element>\\n");
	private static final Pattern FIELD = Pattern.compile(
			"\\t<([A-Za-z][A-Za-z0-9]*)\\b[^\\r\\n]*/>\\n");

	// GeoNumeric's value/slider fields, then XMLBuilder's visual prefix up to <layer>.
	private static final Set<String> BEFORE_LAYER = Set.of("value", "symbolic", "slider",
			"pointSize", "startPoint", "lineStyle", "slopeTriangleSize", "show", "showTrimmed",
			"condition", "objColor", "tableview", "bgColor");

	private NumericLayerXmlProjection() {
	}

	static String constructionXml(App app) {
		return constructionXml(app, new XMLStringBuilder());
	}

	static String constructionFromFullXml(String full) {
		int start = full.indexOf("\n<construction ");
		int end = full.indexOf("</construction>\n", start + 1);
		require(start >= 0 && end > start
				&& full.indexOf("\n<construction ", start + 1) < 0, "unique main construction");
		return full.substring(start + 1, end + "</construction>\n".length());
	}

	static String archiveXml(InputStream input) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(input)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if ("geogebra.xml".equals(entry.getName())) {
					return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing geogebra.xml");
	}

	static Projection verify(App app, String actualConstruction) {
		String legacy = constructionXml(app, new XMLStringBuilder() {
			@Override
			public boolean writesNumericLayers() {
				return false;
			}
		});
		Map<String, GeoElement> byXmlLabel = new HashMap<>();
		var construction = app.getKernel().getConstruction();
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			if (geo.getGeoClassType() == GeoClass.NUMERIC) {
				require(geo.getConstruction() == construction, "live numeric owner");
				String xmlLabel = new XMLStringBuilder().attr("label", geo.getLabelSimple()).toString();
				require(byXmlLabel.put(xmlLabel, geo) == null, "unique live numeric owner");
			}
		}
		Map<String, Integer> layers = new HashMap<>();
		Map<String, Integer> additions = new HashMap<>();
		StringBuilder expected = new StringBuilder();
		int cursor = 0;
		var owners = NUMERIC_OWNER.matcher(legacy);
		while (owners.find()) {
			GeoElement geo = byXmlLabel.get(owners.group(1));
			require(geo != null, "numeric element owner" + owners.group(1));
			int layer = geo.getLayer();
			require(layers.put(geo.getLabelSimple(), layer) == null,
					"unique numeric element" + owners.group(1));
			String body = owners.group(2);
			int slot = layerSlot(body);
			String line = "\t<layer val=\"" + layer + "\"/>\n";
			boolean alreadyPresent = body.startsWith(line, slot);
			require(body.indexOf("\t<layer") == (alreadyPresent ? slot : -1)
					&& body.indexOf("\t<layer", slot + line.length()) < 0,
					"canonical pre-existing layer" + owners.group(1));
			int position = owners.start(2) + slot;
			expected.append(legacy, cursor, position);
			if (!alreadyPresent) {
				expected.append(line);
				additions.put(geo.getLabelSimple(), layer);
			}
			cursor = position;
		}
		expected.append(legacy, cursor, legacy.length());
		String amended = expected.toString();
		if (!amended.equals(actualConstruction)) {
			throw new IllegalStateException("XML differs beyond canonical numeric layer insertions"
					+ " at character " + commonPrefixLength(amended, actualConstruction));
		}
		return new Projection(legacy, amended, Map.copyOf(layers), Map.copyOf(additions));
	}

	private static int layerSlot(String body) {
		int slot = 0;
		var fields = FIELD.matcher(body);
		while (fields.find() && fields.start() == slot && BEFORE_LAYER.contains(fields.group(1))) {
			slot = fields.end();
		}
		return slot;
	}

	private static int commonPrefixLength(String first, String second) {
		int length = Math.min(first.length(), second.length());
		int i = 0;
		while (i < length && first.charAt(i) == second.charAt(i)) {
			i++;
		}
		return i;
	}

	private static String constructionXml(App app, XMLStringBuilder builder) {
		app.getKernel().getConstruction().getConstructionXML(builder, false);
		return builder.toString();
	}

	private static void require(boolean condition, String diagnostic) {
		if (!condition) {
			throw new IllegalStateException(diagnostic);
		}
	}

	record Projection(String legacyConstruction, String nativeConstruction, Map<String, Integer> layers,
			Map<String, Integer> additions) {
		String projectFullXml(String full) {
			require(constructionFromFullXml(full).equals(nativeConstruction), "exact native construction");
			int start = full.indexOf(nativeConstruction);
			require(start >= 0 && full.indexOf(nativeConstruction, start + 1) < 0,
					"unique native construction bytes");
			return full.substring(0, start) + legacyConstruction
					+ full.substring(start + nativeConstruction.length());
		}

		void verifyReopenedLayers(App reopened) {
			var construction = reopened.getKernel().getConstruction();
			for (var entry : layers.entrySet()) {
				GeoElement geo = reopened.getKernel().lookupLabel(entry.getKey());
				require(geo != null && geo.getConstruction() == construction
						&& geo.getGeoClassType() == GeoClass.NUMERIC
						&& geo.getLayer() == entry.getValue(), "reopened numeric layer " + entry.getKey());
			}
		}
	}
}

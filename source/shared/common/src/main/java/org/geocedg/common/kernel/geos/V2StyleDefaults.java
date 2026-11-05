/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.DocHandler;
import org.geogebra.common.io.QDParser;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.EuclidianStyleConstants;

/** The bounded, geometry-free defaults payload shared by preferences and native files. */
public final class V2StyleDefaults implements DocHandler {
	/** Name of the optional native archive entry. */
	public static final String ZIP_ENTRY = "geocedg_defaults_v2.xml";
	/** Maximum uncompressed payload size. */
	public static final int MAX_BYTES = 16 * 1024;

	private static final String[] TAGS = {
		"geogebra", "defaults", "element", "objColor", "lineStyle", "show", "labelMode"
	};
	private final Map<Integer, Style> styles = new LinkedHashMap<>();
	private int field;
	private int type;
	private GColor color;
	private int thickness;
	private int lineType;
	private boolean labelVisible;

	private V2StyleDefaults() {
	}

	/** Appearance that can be transferred to a curve without changing its graph or identity. */
	public record Style(GColor color, int thickness, int lineType,
			boolean labelVisible, int labelMode) {
		/** @param geo prototype supplying the appearance
		 * @return the detached style snapshot
		 */
		public static Style of(GeoElement geo) {
			return new Style(geo.getObjectColor(), geo.getLineThickness(), geo.getLineType(),
					geo.getLabelVisible(), geo.getLabelMode());
		}

		/** @param geo curve or prototype receiving the appearance */
		public void apply(GeoElement geo) {
			geo.setObjColor(color);
			geo.setLineThickness(thickness);
			geo.setLineType(lineType);
			geo.setLabelVisible(labelVisible);
			if (geo.isDefaultGeo()) {
				// Loading a prototype must not change the application's labeling menu.
				geo.labelMode = labelMode;
			} else {
				geo.setLabelMode(labelMode);
			}
		}
	}

	/**
	 * @param styles nonempty styles in default-type order
	 * @return the versioned XML, or null if all styles use the inherited fallback
	 */
	public static String write(Map<Integer, Style> styles) {
		if (styles.isEmpty()) {
			return null;
		}
		XMLStringBuilder xml = new XMLStringBuilder();
		xml.startOpeningTag("geogebra", 0).endTag();
		xml.startOpeningTag("defaults", 0).attr("version", 1).endTag();
		styles.forEach((type, style) -> {
			xml.startOpeningTag("element", 1).attr("default", type).endTag();
			xml.startTag("objColor", 2).attr("r", style.color().getRed())
					.attr("g", style.color().getGreen()).attr("b", style.color().getBlue())
					.attr("alpha", style.color().getAlpha()).endTag();
			xml.startTag("lineStyle", 2).attr("thickness", style.thickness())
					.attr("type", style.lineType()).endTag();
			xml.startTag("show", 2).attr("label", style.labelVisible()).endTag();
			xml.startTag("labelMode", 2).attr("val", style.labelMode()).endTag();
			xml.closeTag("element");
		});
		xml.closeTag("defaults");
		xml.closeTag("geogebra");
		return xml.toString();
	}

	/**
	 * @param xml the standalone defaults payload
	 * @return validated entries, with no side effect on a construction
	 * @throws XMLParseException malformed, unknown or oversized payload
	 */
	public static Map<Integer, Style> read(String xml) throws XMLParseException {
		if (xml == null) {
			return Map.of();
		}
		if (xml.length() > MAX_BYTES) {
			throw invalid();
		}
		V2StyleDefaults handler = new V2StyleDefaults();
		try {
			new QDParser().parse(handler, new StringReader(xml));
			String canonical = write(handler.styles);
			// QDParser historically accepts preamble and trailing content. The V2
			// block is canonical apart from whitespace between its elements.
			if (canonical == null || !compact(canonical).equals(compact(xml))) {
				throw invalid();
			}
		} catch (IOException | IllegalArgumentException failure) {
			throw new XMLParseException("Invalid V2 construction defaults", failure);
		}
		return handler.styles;
	}

	/**
	 * Check the scope of retained ordinary preferences before applying them to a live document.
	 * @param xml legacy preferences produced by the ordinary defaults writer
	 * @param defaults known prototype IDs
	 * @throws XMLParseException if the payload can escape the defaults section
	 */
	public static void validateLegacyDefaults(String xml, ConstructionDefaults defaults)
			throws XMLParseException {
		try {
			new QDParser().parse(new LegacyScope(defaults), new StringReader(xml));
		} catch (IOException | IllegalArgumentException failure) {
			throw new XMLParseException("Invalid personal construction defaults", failure);
		}
	}

	private static final class LegacyScope implements DocHandler {
		private final ConstructionDefaults defaults;
		private final List<String> open = new ArrayList<>();

		LegacyScope(ConstructionDefaults defaults) {
			this.defaults = defaults;
		}

		@Override
		public void startElement(String tag, Map<String, String> attrs) throws XMLParseException {
			switch (open.size()) {
			case 0:
				require("geogebra".equals(tag));
				break;
			case 1:
				require("defaults".equals(tag));
				break;
			case 2:
				int id = Integer.parseInt(attrs.get("default"));
				GeoElement prototype = defaults.getDefaultGeo(id);
				require("element".equals(tag) && prototype != null && !prototype.isGeoElement3D()
						&& id != ConstructionDefaults.DEFAULT_LOCUS_V2
						&& id != ConstructionDefaults.DEFAULT_SPLINE_V2
						&& prototype.getXMLTypeString().equals(attrs.get("type"))
						&& attrs.get("label") != null && !attrs.containsKey("geocedgId"));
				break;
			case 3:
				require(!"element".equals(tag) && !"command".equals(tag)
						&& !"script".equals(tag) && !"condition".equals(tag)
						&& !attrs.containsKey("dynamicr"));
				break;
			default:
				throw invalid();
			}
			open.add(tag);
		}

		private static void require(boolean valid) throws XMLParseException {
			if (!valid) {
				throw new XMLParseException("Invalid personal construction defaults");
			}
		}

		@Override
		public void endElement(String tag) throws XMLParseException {
			int last = open.size() - 1;
			require(last >= 0 && tag.equals(open.get(last)));
			open.remove(last);
		}

		@Override
		public void startDocument() {
		}

		@Override
		public void endDocument() throws XMLParseException {
			require(open.isEmpty());
		}

		@Override
		public void text(String value) throws XMLParseException {
			require(value.trim().isEmpty());
		}
	}

	private static String compact(String xml) {
		return xml.trim().replaceAll(">\s+<", "><");
	}

	private static XMLParseException invalid() {
		return new XMLParseException("Invalid V2 construction defaults");
	}

	@Override
	public void startElement(String tag, Map<String, String> attributes) throws XMLParseException {
		if (field >= TAGS.length || !TAGS[field].equals(tag)) {
			throw invalid();
		}
		switch (field++) {
		case 1:
			if (!"1".equals(attributes.get("version"))) {
				throw invalid();
			}
			break;
		case 2:
			type = number(attributes, "default", ConstructionDefaults.DEFAULT_LOCUS_V2,
					ConstructionDefaults.DEFAULT_SPLINE_V2);
			if (styles.containsKey(type)) {
				throw invalid();
			}
			break;
		case 3:
			color = GColor.newColor(number(attributes, "r", 0, 255),
					number(attributes, "g", 0, 255), number(attributes, "b", 0, 255),
					number(attributes, "alpha", 0, 255));
			break;
		case 4:
			thickness = number(attributes, "thickness", 0, Integer.MAX_VALUE);
			lineType = number(attributes, "type", -1, 30);
			if (lineType != EuclidianStyleConstants.LINE_TYPE_POINTWISE
					&& lineType != EuclidianStyleConstants.LINE_TYPE_FULL
					&& lineType != EuclidianStyleConstants.LINE_TYPE_DASHED_SHORT
					&& lineType != EuclidianStyleConstants.LINE_TYPE_DASHED_LONG
					&& lineType != EuclidianStyleConstants.LINE_TYPE_DOTTED
					&& lineType != EuclidianStyleConstants.LINE_TYPE_DASHED_DOTTED) {
				throw invalid();
			}
			break;
		case 5:
			String visible = attributes.get("label");
			if (!"true".equals(visible) && !"false".equals(visible)) {
				throw invalid();
			}
			labelVisible = Boolean.parseBoolean(visible);
			break;
		case 6:
			int mode = number(attributes, "val", GeoElementND.LABEL_NAME,
					GeoElementND.LABEL_CAPTION_VALUE);
			styles.put(type, new Style(color, thickness, lineType, labelVisible, mode));
			break;
		default:
			break;
		}
	}

	private static int number(Map<String, String> attributes, String key, int min, int max)
			throws XMLParseException {
		int value = Integer.parseInt(attributes.get(key));
		if (value < min || value > max) {
			throw invalid();
		}
		return value;
	}

	@Override
	public void endElement(String tag) throws XMLParseException {
		if ("element".equals(tag)) {
			if (field != TAGS.length) {
				throw invalid();
			}
			field = 2;
		}
	}

	@Override
	public void startDocument() {
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void text(String value) throws XMLParseException {
		if (!value.trim().isEmpty()) {
			throw invalid();
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.LinearEquationRepresentable;
import org.geogebra.common.kernel.QuadraticEquationRepresentable;
import org.geogebra.common.kernel.arithmetic.VectorValue;
import org.geogebra.common.kernel.geos.AngleProperties;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.geos.HasSegmentStyle;
import org.geogebra.common.kernel.geos.LimitedPath;
import org.geogebra.common.kernel.geos.PointProperties;
import org.geogebra.common.kernel.geos.SegmentStyle;
import org.geogebra.common.kernel.geos.TextProperties;
import org.geogebra.common.kernel.geos.VectorHeadStyle;
import org.geogebra.common.kernel.geos.properties.FillType;
import org.geogebra.common.main.App;

/** Detached, typed values for ordinary appearance prototypes. */
public final class LegacyStyleDefaults {
	private final Map<Integer, List<Value<?>>> prototypes;

	private LegacyStyleDefaults(Map<Integer, List<Value<?>>> prototypes) {
		this.prototypes = Collections.unmodifiableMap(new LinkedHashMap<>(prototypes));
	}

	/** @return an empty style snapshot */
	public static LegacyStyleDefaults empty() {
		return new LegacyStyleDefaults(Map.of());
	}

	/**
	 * @param defaults receiving prototypes of the same configuration
	 * @return whether the snapshot supplied a Classic Locus style
	 */
	public boolean applyTo(ConstructionDefaults defaults) {
		prototypes.forEach((id, values) -> {
			GeoElement prototype = defaults.getDefaultGeo(id);
			values.forEach(value -> value.applyTo(prototype));
		});
		return prototypes.containsKey(ConstructionDefaults.DEFAULT_LOCUS);
	}

	private record Value<T>(T data, BiConsumer<GeoElement, T> write) {
		void applyTo(GeoElement prototype) {
			write.accept(prototype, data);
		}
	}

	/** Decodes an entire legacy snapshot before any setter can run. */
	public static final class Decoder {
		private final Map<Integer, List<Value<?>>> prototypes = new LinkedHashMap<>();
		private final ConstructionDefaults defaults;
		private final double format;
		private List<Value<?>> values;
		private GeoElement prototype;
		private int type;

		/**
		 * @param defaults lookup of the supported 2D prototypes and their types
		 * @param format original GeoGebra file format version
		 */
		public Decoder(ConstructionDefaults defaults, double format) {
			this.defaults = defaults;
			this.format = format;
		}

		/**
		 * @param id default type
		 * @param xmlType serialized type of that prototype
		 */
		public void startPrototype(int id, String xmlType) {
			prototype = defaults.getDefaultGeo(id);
			require(prototype != null && !prototype.isGeoElement3D()
					&& id != ConstructionDefaults.DEFAULT_LOCUS_V2
					&& id != ConstructionDefaults.DEFAULT_SPLINE_V2
					&& prototype.getXMLTypeString().equalsIgnoreCase(xmlType)
					&& !prototypes.containsKey(id), "invalid or duplicate prototype");
			type = id;
			values = new ArrayList<>();
		}

		/** Finish the current prototype. */
		public void endPrototype() {
			prototypes.put(type, List.copyOf(values));
		}

		/** @return the completed, geometry-free style snapshot */
		public LegacyStyleDefaults finish() {
			return new LegacyStyleDefaults(prototypes);
		}

		/**
		 * @param name one allowed prototype leaf
		 * @param attributes its attributes, consumed during validation
		 */
		public void leaf(String name, Map<String, String> attributes) {
			switch (name) {
			case "objColor":
				objectColor(attributes);
				break;
			case "bgColor":
				int opacity = integer(attributes, "alpha", 255);
				require(opacity >= 0 && opacity <= 255, "background opacity");
				add(color(attributes).deriveWithAlpha(opacity), GeoElement::setBackgroundColor);
				break;
			case "lineStyle":
				lineStyle(attributes);
				break;
			case "show":
				show(attributes);
				break;
			case "animation":
				animation(attributes);
				break;
			case "slider":
				slider(attributes);
				break;
			case "font":
				font(attributes);
				break;
			case "eqnStyle":
				equationStyle(attributes);
				break;
			case "coordStyle":
				coordinatesStyle(attributes);
				break;
			case "startStyle", "endStyle":
				segmentStyle(name, attributes);
				break;
			case "pointSize":
				pointSize(attributes);
				break;
			case "pointStyle":
				require(prototype instanceof PointProperties, name);
				int style = integer(attributes, "val", null);
				add(style == -1 ? 0 : style, (geo, value) -> ((PointProperties) geo).setPointStyle(value));
				break;
			case "headStyle":
				require(prototype instanceof GeoVector, name);
				add(enumValue(VectorHeadStyle.values(), attributes, "val"),
						(geo, value) -> ((GeoVector) geo).setHeadStyle(value));
				break;
			case "angleStyle":
				require(prototype instanceof AngleProperties, name);
				add(enumValue(GeoAngle.AngleStyle.values(), attributes, "val").ordinal(),
						(geo, value) -> ((AngleProperties) geo).setAngleStyle(value));
				break;
			case "arcSize":
				require(prototype instanceof AngleProperties, name);
				add(integer(attributes, "val", null), (geo, value) -> ((AngleProperties) geo).setArcSize(value));
				break;
			case "labelMode":
				add(integer(attributes, "val", null), (geo, value) -> {
					if (geo instanceof GeoNumeric numeric) {
						numeric.setLabelModeFromXML(value);
					} else {
						geo.setLabelMode(value);
					}
				});
				break;
			case "algebra":
				add(flag(attributes, "labelVisible", null), GeoElement::setAlgebraLabelVisible);
				break;
			case "labelOffset":
				add(integer(attributes, "x", null), (geo, value) -> geo.labelOffsetX = value);
				add(integer(attributes, "y", null), (geo, value) -> geo.labelOffsetY = value);
				break;
			case "layer":
				add(integer(attributes, "val", null), GeoElement::setPrototypeLayer);
				break;
			case "tooltipMode":
				add(integer(attributes, "val", null), GeoElement::setTooltipMode);
				break;
			case "decoration":
				add(integer(attributes, "type", null), GeoElement::setDecorationType);
				break;
			case "caption":
				add(required(attributes, "val"), GeoElement::setCaption);
				break;
			case "autocolor":
				add(flag(attributes, "val", null), GeoElement::setAutoColor);
				break;
			case "fixed":
				add(flag(attributes, "val", null), GeoElement::setFixed);
				break;
			case "selectionAllowed":
				add(flag(attributes, "val", null), GeoElement::setSelectionAllowed);
				break;
			case "auxiliary":
				add(flag(attributes, "val", null), GeoElement::setAuxiliaryObject);
				break;
			case "isShape":
				flag(attributes, "val", null);
				break;
			case "showTrimmed":
				add(flag(attributes, "val", null), GeoElement::setShowTrimmedIntersectionLines);
				break;
			case "checkbox":
				require(prototype instanceof GeoBoolean, name);
				add(flag(attributes, "fixed", null), (geo, value) -> ((GeoBoolean) geo).setCheckboxFixed(value));
				break;
			case "inBackground":
				require(prototype instanceof GeoImage, name);
				add(flag(attributes, "val", null), (geo, value) -> ((GeoImage) geo).setInBackground(value));
				break;
			case "outlyingIntersections":
				require(prototype instanceof LimitedPath, name);
				add(flag(attributes, "val", null),
						(geo, value) -> ((LimitedPath) geo).setAllowOutlyingIntersections(value));
				break;
			case "keepTypeOnTransform":
				require(prototype instanceof LimitedPath, name);
				add(flag(attributes, "val", null),
						(geo, value) -> ((LimitedPath) geo).setKeepTypeOnGeometricTransform(value));
				break;
			case "simplifyCoefficients":
				require(prototype instanceof GeoFunction, name);
				add(flag(attributes, "val", null),
						(geo, value) -> ((GeoFunction) geo).setSimplifyCoefficients(value));
				break;
			case "coords":
				require(prototype instanceof VectorValue || prototype instanceof GeoLine, name);
				placeholder(attributes, "x", "y", "z");
				break;
			case "eigenvectors":
				require(prototype.isGeoConic(), name);
				placeholder(attributes, "x0", "y0", "z0", "x1", "y1", "z1");
				break;
			case "matrix":
				require(prototype.isGeoConic(), name);
				placeholder(attributes, "A0", "A1", "A2", "A3", "A4", "A5");
				break;
			case "value":
				if (prototype instanceof GeoBoolean) {
					flag(attributes, "val", null);
				} else {
					require(prototype instanceof GeoNumeric, name);
					placeholder(attributes, "val");
					require(!flag(attributes, "random", false), "random default");
				}
				break;
			case "file":
				// The old writer includes an empty image resource slot.
				require(prototype instanceof GeoImage && required(attributes, "name").isEmpty(), name);
				break;
			default:
				throw new IllegalArgumentException("unsupported style leaf " + name);
			}
			require(attributes.isEmpty(), "unsupported attribute in " + name);
		}

		private <T> void add(T value, BiConsumer<GeoElement, T> setter) {
			if (value != null) {
				values.add(new Value<>(value, setter));
			}
		}

		private void objectColor(Map<String, String> attributes) {
			add(color(attributes), GeoElement::setObjColor);
			Double alpha = decimal(attributes, "alpha", false);
			if (alpha != null && (!prototype.isGeoList() || format > 3.19)) {
				require(alpha >= -1 && alpha <= 1, "fill opacity");
				add(alpha, GeoElement::setAlphaValue);
			}
			Integer angle = optionalInteger(attributes, "hatchAngle");
			Integer distance = optionalInteger(attributes, "hatchDistance");
			require((angle == null) == (distance == null), "hatch parameters");
			if (angle != null) {
				add(angle, GeoElement::setHatchingAngle);
				add(distance, GeoElement::setHatchingDistance);
				add(FillType.HATCH, GeoElement::setFillType);
			}
			if (attributes.containsKey("fillType")) {
				add(enumValue(FillType.values(), attributes, "fillType"), GeoElement::setFillType);
			}
			add(attributes.remove("fillSymbol"), GeoElement::setFillSymbol);
			String image = attributes.remove("image");
			if (image != null) {
				add(image, GeoElement::setFillImage);
				add(FillType.IMAGE, GeoElement::setFillType);
			}
			add(flag(attributes, "inverseFill", null, false), GeoElement::setInverseFill);
		}

		private void lineStyle(Map<String, String> attributes) {
			int thickness = integer(attributes, "thickness", null);
			require(thickness >= 0, "line thickness");
			add(thickness, GeoElement::setLineThickness);
			add(integer(attributes, "type", null), GeoElement::setLineType);
			add(optionalInteger(attributes, "typeHidden"), GeoElement::setLineTypeHidden);
			add(optionalInteger(attributes, "opacity"), GeoElement::setLineOpacity);
			if (attributes.containsKey("drawArrow")) {
				require(prototype instanceof GeoLocus, "drawArrow");
				add(flag(attributes, "drawArrow", null), (geo, value) -> ((GeoLocus) geo).drawAsArrows(value));
			}
		}

		private void show(Map<String, String> attributes) {
			add(flag(attributes, "object", null), GeoElement::setPrototypeVisibility);
			add(flag(attributes, "label", null), GeoElement::setLabelVisible);
			int views = integer(attributes, "ev", 0);
			require(views >= 0 && views < 64 && (views & 48) != 48, "view mask");
			add(views, LegacyStyleDefaults::applyViews);
		}

		private void animation(Map<String, String> attributes) {
			require(!flag(attributes, "playing", false), "active animation");
			Double step = decimal(attributes, "step", prototype instanceof GeoAngle);
			add(step, GeoElement::setAnimationStep);
			if (prototype instanceof GeoNumeric) {
				add(step == null, (geo, value) -> ((GeoNumeric) geo).setAutoStep(value));
			}
			// Bypass construction defaults for the detached scalar slot.
			add(decimal(attributes, "speed", false), (geo, value) -> geo.setAnimationSpeedObject(
					new GeoNumeric(geo.getConstruction(), value, false)));
			Integer type = optionalInteger(attributes, "type");
			require(type == null || type >= 0 && type <= GeoElement.ANIMATION_INCREASING_ONCE,
					"animation type");
			add(type, GeoElement::setAnimationType);
		}

		private void slider(Map<String, String> attributes) {
			require(prototype instanceof GeoNumeric, "slider type");
			boolean angle = prototype instanceof GeoAngle;
			Double min = decimal(attributes, "min", angle);
			Double max = decimal(attributes, "max", angle);
			require(min == null && max == null || min != null && max != null && min < max,
					"slider interval");
			add(min, (geo, value) -> ((GeoNumeric) geo).setPrototypeSliderBounds(value, max));
			add(flag(attributes, "absoluteScreenLocation", false),
					(geo, value) -> ((GeoNumeric) geo).setAbsoluteScreenLocActive(value));
			Double x = decimal(attributes, "x", false);
			Double y = decimal(attributes, "y", false);
			// Old writers emitted prototype placement as well as width.
			require((x == null) == (y == null), "slider position");
			Double width = decimal(attributes, "width", false);
			require(width != null && width > 0, "slider width");
			add(width, (geo, value) -> ((GeoNumeric) geo).setSliderWidth(value, true));
			add(flag(attributes, "fixed", false), (geo, value) -> ((GeoNumeric) geo).setSliderFixed(value));
			add(flag(attributes, "horizontal", false),
					(geo, value) -> ((GeoNumeric) geo).setSliderHorizontal(value));
			add(flag(attributes, "showAlgebra", false),
					(geo, value) -> ((GeoNumeric) geo).setAVSliderOrCheckboxVisible(value));
		}

		private void pointSize(Map<String, String> attributes) {
			if (prototype instanceof GeoNumeric) {
				Double size = decimal(attributes, "val", false);
				require(size != null && size > 0, "slider knob size");
				add(size, (geo, value) -> ((GeoNumeric) geo).setSliderBlobSize(value));
			} else {
				require(prototype instanceof PointProperties, "point size");
				add(integer(attributes, "val", null),
						(geo, value) -> ((PointProperties) geo).setPointSize(value));
			}
		}

		private void font(Map<String, String> attributes) {
			require(prototype instanceof TextProperties, "font type");
			Integer offset = optionalInteger(attributes, "size");
			Double multiplier = decimal(attributes, "sizeM", false);
			require(multiplier != null ? multiplier > 0 : offset != null, "font size");
			if (multiplier == null) {
				add(offset, (geo, value) -> {
					double size = geo.getKernel().getApplication().getFontSize();
					((TextProperties) geo).setFontSizeMultiplier(Math.max(size + value, 4) / size);
				});
			} else {
				add(multiplier, (geo, value) -> ((TextProperties) geo).setFontSizeMultiplier(value));
			}
			add(flag(attributes, "serif", null, false),
					(geo, value) -> ((TextProperties) geo).setSerifFont(value));
			add(optionalInteger(attributes, "style"),
					(geo, value) -> ((TextProperties) geo).setFontStyle(value));
		}

		private void equationStyle(Map<String, String> attributes) {
			String form = required(attributes, "style");
			String parameter = attributes.remove("parameter");
			require(parameter == null || parameter.matches("[\\p{L}_][\\p{L}\\p{N}_]*"),
					"equation parameter name");
			if (prototype instanceof LinearEquationRepresentable) {
				require(List.of("implicit", "explicit", "parametric", "general", "user").contains(form),
						"linear equation style");
				add(form, (geo, value) -> ((LinearEquationRepresentable) geo)
						.setEquationFormFromXML(value, parameter));
			} else {
				require(prototype instanceof QuadraticEquationRepresentable
						&& List.of("implicit", "explicit", "specific", "parametric",
								"user", "vertex", "conic").contains(form), "quadratic equation style");
				add(form, (geo, value) -> ((QuadraticEquationRepresentable) geo)
						.setEquationFormFromXML(value, parameter));
			}
		}

		private void coordinatesStyle(Map<String, String> attributes) {
			require(prototype instanceof VectorValue, "coordinate style type");
			int mode = switch (required(attributes, "style")) {
			case "cartesian" -> Kernel.COORD_CARTESIAN;
			case "polar" -> Kernel.COORD_POLAR;
			case "complex" -> Kernel.COORD_COMPLEX;
			default -> throw new IllegalArgumentException("coordinate style");
			};
			add(mode, (geo, value) -> ((VectorValue) geo).setMode(value));
		}

		private void segmentStyle(String name, Map<String, String> attributes) {
			SegmentStyle style = SegmentStyle.fromString(required(attributes, "val"));
			require(prototype instanceof HasSegmentStyle && style != null, name);
			add(style, (geo, value) -> {
				HasSegmentStyle segment = (HasSegmentStyle) geo;
				if ("startStyle".equals(name)) {
					segment.setStartStyle(value);
				} else {
					segment.setEndStyle(value);
				}
			});
		}
	}

	private static void applyViews(GeoElement geo, int mask) {
		setView(geo, App.VIEW_EUCLIDIAN, (mask & 1) == 0);
		setView(geo, App.VIEW_EUCLIDIAN2, (mask & 2) != 0);
		if ((mask & 4) != 0) {
			geo.addViews3D();
		}
		if ((mask & 8) != 0) {
			geo.removeViews3D();
		}
		if ((mask & 48) != 0) {
			geo.setVisibleInViewForPlane((mask & 16) != 0);
		}
	}

	private static void setView(GeoElement geo, int view, boolean visible) {
		if (visible) {
			geo.addView(view);
		} else {
			geo.removeView(view);
		}
	}

	private static GColor color(Map<String, String> attributes) {
		return GColor.newColor(channel(attributes, "r"), channel(attributes, "g"), channel(attributes, "b"));
	}

	private static int channel(Map<String, String> attributes, String key) {
		int value = integer(attributes, key, null);
		require(value >= 0 && value <= 255, "color channel");
		return value;
	}

	private static <T> T enumValue(T[] values, Map<String, String> attributes, String key) {
		int value = integer(attributes, key, null);
		require(value >= 0 && value < values.length, key);
		return values[value];
	}

	private static Integer optionalInteger(Map<String, String> attributes, String key) {
		String raw = attributes.remove(key);
		return raw == null ? null : Integer.valueOf(raw);
	}

	private static int integer(Map<String, String> attributes, String key, Integer fallback) {
		Integer value = optionalInteger(attributes, key);
		require(value != null || fallback != null, key);
		return value == null ? fallback : value;
	}

	private static Boolean flag(Map<String, String> attributes, String key, Boolean fallback,
			boolean mandatory) {
		String value = attributes.remove(key);
		require(value != null || !mandatory, key);
		if (value == null) {
			return fallback;
		}
		require("true".equals(value) || "false".equals(value), key);
		return Boolean.valueOf(value);
	}

	private static boolean flag(Map<String, String> attributes, String key, Boolean fallback) {
		return flag(attributes, key, fallback, fallback == null);
	}

	private static Double decimal(Map<String, String> attributes, String key, boolean degreesAllowed) {
		String raw = attributes.remove(key);
		return raw == null ? null : literal(raw, degreesAllowed);
	}

	private static double literal(String raw, boolean degreesAllowed) {
		String text = raw.trim();
		boolean degrees = degreesAllowed && text.endsWith("°");
		if (degrees) {
			text = text.substring(0, text.length() - (text.endsWith("*°") ? 2 : 1)).trim();
		}
		require(text.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?"),
				"expected a numeric literal");
		double value = Double.parseDouble(text);
		require(Double.isFinite(value), "expected a finite number");
		return degrees ? value * Kernel.PI_180 : value;
	}

	private static void placeholder(Map<String, String> attributes, String... keys) {
		for (String key : keys) {
			String raw = required(attributes, key);
			if (!"NaN".equals(raw) && !"?".equals(raw)) {
				literal(raw, false);
			}
		}
	}

	private static String required(Map<String, String> attributes, String key) {
		String value = attributes.remove(key);
		require(value != null, key);
		return value;
	}

	private static void require(boolean valid, String field) {
		if (!valid) {
			throw new IllegalArgumentException(field);
		}
	}
}

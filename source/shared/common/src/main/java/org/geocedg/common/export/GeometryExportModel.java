/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable, read-only representation of resolved 2D geometry for exporters.
 */
public final class GeometryExportModel {

	/** Input population selected before geometry adaptation. */
	public enum SelectionMode {
		/** All labeled 2D construction objects. */
		COMPLETE_CONSTRUCTION,
		/** The explicit current application selection. */
		CURRENT_SELECTION
	}

	/** Unit carried by source and target model. */
	public enum Unit {
		/** No approved physical unit is present. */
		UNITLESS
	}

	/** Fidelity of a neutral entity relative to its source geometry. */
	public enum Exactness {
		/** Analytic parameters are retained. */
		EXACT,
		/** A tolerance-controlled approximation is retained. */
		APPROXIMATE
	}

	/** Neutral geometry families supported by the G5 model. */
	public enum GeometryType {
		POINT,
		SEGMENT,
		RAY,
		INFINITE_LINE,
		CIRCLE,
		ARC,
		ELLIPSE,
		POLYLINE
	}

	/** Diagnostic category for a source object omitted from the model. */
	public enum DiagnosticCode {
		UNSUPPORTED,
		UNDEFINED,
		NON_FINITE,
		NOT_2D,
		DEGENERATE,
		DUPLICATE_POLYGON_SIDE
	}

	/** Immutable 2D coordinate or vector. */
	public static final class Point2D {
		private final double x;
		private final double y;

		/**
		 * @param x x coordinate
		 * @param y y coordinate
		 */
		public Point2D(double x, double y) {
			assertFinite(x, "x");
			assertFinite(y, "y");
			this.x = normalizeZero(x);
			this.y = normalizeZero(y);
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		/** @return Euclidean norm */
		public double norm() {
			return Math.sqrt(x * x + y * y);
		}
	}

	/** Presentation metadata that is safe to transport in G5. */
	public static final class Style {
		private final int red;
		private final int green;
		private final int blue;
		private final boolean visible;

		/**
		 * @param red red channel
		 * @param green green channel
		 * @param blue blue channel
		 * @param visible current object visibility
		 */
		public Style(int red, int green, int blue, boolean visible) {
			assertColor(red);
			assertColor(green);
			assertColor(blue);
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.visible = visible;
		}

		public int getRed() {
			return red;
		}

		public int getGreen() {
			return green;
		}

		public int getBlue() {
			return blue;
		}

		public boolean isVisible() {
			return visible;
		}

		/** @return DXF 24-bit true-color integer */
		public int toTrueColor() {
			return red << 16 | green << 8 | blue;
		}
	}

	/** Marker for typed neutral geometry values. */
	public interface Geometry {
		/** @return geometry family */
		GeometryType getType();
	}

	/** Point geometry. */
	public static final class PointGeometry implements Geometry {
		private final Point2D point;

		public PointGeometry(Point2D point) {
			this.point = require(point, "point");
		}

		@Override
		public GeometryType getType() {
			return GeometryType.POINT;
		}

		public Point2D getPoint() {
			return point;
		}
	}

	/** Bounded or unbounded linear geometry. */
	public static final class LinearGeometry implements Geometry {
		private final GeometryType type;
		private final Point2D start;
		private final Point2D vector;

		/**
		 * Segment stores its end point in {@code vector}; ray and line store a
		 * unit direction.
		 *
		 * @param type segment, ray, or infinite line
		 * @param start start/base point
		 * @param vector end point or unit direction
		 */
		public LinearGeometry(GeometryType type, Point2D start, Point2D vector) {
			if (type != GeometryType.SEGMENT && type != GeometryType.RAY
					&& type != GeometryType.INFINITE_LINE) {
				throw new IllegalArgumentException("Invalid linear type: " + type);
			}
			this.type = type;
			this.start = require(start, "start");
			this.vector = require(vector, "vector");
			if (type != GeometryType.SEGMENT
					&& Math.abs(vector.norm() - 1) > 1E-12) {
				throw new IllegalArgumentException("Infinite direction must be unit length");
			}
		}

		@Override
		public GeometryType getType() {
			return type;
		}

		public Point2D getStart() {
			return start;
		}

		public Point2D getVector() {
			return vector;
		}
	}

	/** Circle geometry. */
	public static final class CircleGeometry implements Geometry {
		private final Point2D center;
		private final double radius;

		/**
		 * Creates an exact circle.
		 * @param center circle center in model coordinates
		 * @param radius positive model-coordinate radius
		 */
		public CircleGeometry(Point2D center, double radius) {
			this.center = require(center, "center");
			assertPositive(radius, "radius");
			this.radius = radius;
		}

		@Override
		public GeometryType getType() {
			return GeometryType.CIRCLE;
		}

		public Point2D getCenter() {
			return center;
		}

		public double getRadius() {
			return radius;
		}
	}

	/** Counterclockwise circular arc geometry in global Cartesian axes. */
	public static final class ArcGeometry implements Geometry {
		private final Point2D center;
		private final double radius;
		private final double startAngleDegrees;
		private final double endAngleDegrees;

		/**
		 * Creates a counterclockwise circular arc.
		 * @param center arc center in model coordinates
		 * @param radius positive model-coordinate radius
		 * @param startAngleDegrees start angle in degrees
		 * @param endAngleDegrees end angle in degrees
		 */
		public ArcGeometry(Point2D center, double radius,
				double startAngleDegrees, double endAngleDegrees) {
			this.center = require(center, "center");
			assertPositive(radius, "radius");
			assertFinite(startAngleDegrees, "start angle");
			assertFinite(endAngleDegrees, "end angle");
			this.radius = radius;
			this.startAngleDegrees = normalizeDegrees(startAngleDegrees);
			this.endAngleDegrees = normalizeDegrees(endAngleDegrees);
		}

		@Override
		public GeometryType getType() {
			return GeometryType.ARC;
		}

		public Point2D getCenter() {
			return center;
		}

		public double getRadius() {
			return radius;
		}

		public double getStartAngleDegrees() {
			return startAngleDegrees;
		}

		public double getEndAngleDegrees() {
			return endAngleDegrees;
		}
	}

	/** Full or partial ellipse in DXF-compatible parameterization. */
	public static final class EllipseGeometry implements Geometry {
		private final Point2D center;
		private final Point2D majorAxis;
		private final double minorMajorRatio;
		private final double startParameter;
		private final double endParameter;

		/**
		 * Creates a full or partial ellipse.
		 * @param center ellipse center in model coordinates
		 * @param majorAxis major-axis vector from the center
		 * @param minorMajorRatio minor-axis to major-axis ratio
		 * @param startParameter start parameter in radians
		 * @param endParameter end parameter in radians
		 */
		public EllipseGeometry(Point2D center, Point2D majorAxis,
				double minorMajorRatio, double startParameter, double endParameter) {
			this.center = require(center, "center");
			this.majorAxis = require(majorAxis, "major axis");
			assertPositive(majorAxis.norm(), "major axis length");
			assertPositive(minorMajorRatio, "minor/major ratio");
			if (minorMajorRatio > 1) {
				throw new IllegalArgumentException("Ellipse ratio must not exceed one");
			}
			assertFinite(startParameter, "start parameter");
			assertFinite(endParameter, "end parameter");
			this.minorMajorRatio = minorMajorRatio;
			this.startParameter = startParameter;
			this.endParameter = endParameter;
		}

		@Override
		public GeometryType getType() {
			return GeometryType.ELLIPSE;
		}

		public Point2D getCenter() {
			return center;
		}

		public Point2D getMajorAxis() {
			return majorAxis;
		}

		public double getMinorMajorRatio() {
			return minorMajorRatio;
		}

		public double getStartParameter() {
			return startParameter;
		}

		public double getEndParameter() {
			return endParameter;
		}
	}

	/** Ordered open or closed polyline geometry. */
	public static final class PolylineGeometry implements Geometry {
		private final List<Point2D> vertices;
		private final boolean closed;

		/**
		 * Creates an ordered polyline.
		 * @param vertices model-coordinate vertices
		 * @param closed whether the final vertex connects to the first
		 */
		public PolylineGeometry(List<Point2D> vertices, boolean closed) {
			if (vertices == null || vertices.size() < 2) {
				throw new IllegalArgumentException("Polyline requires at least two vertices");
			}
			this.vertices = immutableCopy(vertices);
			this.closed = closed;
		}

		@Override
		public GeometryType getType() {
			return GeometryType.POLYLINE;
		}

		public List<Point2D> getVertices() {
			return vertices;
		}

		public boolean isClosed() {
			return closed;
		}
	}

	/** One exportable source entity and its neutral geometry. */
	public static final class Entity {
		private final String sourceId;
		private final String sourceType;
		private final String label;
		private final String layer;
		private final Style style;
		private final Exactness exactness;
		private final Double approximationTolerance;
		private final Geometry geometry;

		/**
		 * Creates a neutral export entity with source and style metadata.
		 * @param sourceId deterministic identifier for this construction revision
		 * @param sourceType source GeoGebra type
		 * @param label optional source label
		 * @param layer normalized export layer
		 * @param style transportable presentation metadata
		 * @param exactness exact or approximate representation status
		 * @param approximationTolerance positive tolerance for approximate entities
		 * @param geometry neutral geometry value
		 */
		public Entity(String sourceId, String sourceType, String label,
				String layer, Style style, Exactness exactness,
				Double approximationTolerance, Geometry geometry) {
			this.sourceId = requireText(sourceId, "source id");
			this.sourceType = requireText(sourceType, "source type");
			this.label = label;
			this.layer = requireText(layer, "layer");
			this.style = require(style, "style");
			this.exactness = require(exactness, "exactness");
			this.geometry = require(geometry, "geometry");
			if (exactness == Exactness.APPROXIMATE) {
				if (approximationTolerance == null) {
					throw new IllegalArgumentException("Approximation requires a tolerance");
				}
				assertPositive(approximationTolerance, "approximation tolerance");
			} else if (approximationTolerance != null) {
				throw new IllegalArgumentException("Exact entity cannot have a tolerance");
			}
			this.approximationTolerance = approximationTolerance;
		}

		public String getSourceId() {
			return sourceId;
		}

		public String getSourceType() {
			return sourceType;
		}

		public String getLabel() {
			return label;
		}

		public String getLayer() {
			return layer;
		}

		public Style getStyle() {
			return style;
		}

		public Exactness getExactness() {
			return exactness;
		}

		public Double getApproximationTolerance() {
			return approximationTolerance;
		}

		public Geometry getGeometry() {
			return geometry;
		}
	}

	/** Explicit record of a source object not emitted as geometry. */
	public static final class Diagnostic {
		private final String sourceId;
		private final String sourceType;
		private final DiagnosticCode code;
		private final String message;

		/**
		 * Creates an explicit skipped-object or invalid-source diagnostic.
		 * @param sourceId deterministic source identifier
		 * @param sourceType source GeoGebra type
		 * @param code machine-readable diagnostic code
		 * @param message actionable diagnostic text
		 */
		public Diagnostic(String sourceId, String sourceType,
				DiagnosticCode code, String message) {
			this.sourceId = requireText(sourceId, "source id");
			this.sourceType = requireText(sourceType, "source type");
			this.code = require(code, "code");
			this.message = requireText(message, "message");
		}

		public String getSourceId() {
			return sourceId;
		}

		public String getSourceType() {
			return sourceType;
		}

		public DiagnosticCode getCode() {
			return code;
		}

		public String getMessage() {
			return message;
		}
	}

	private final SelectionMode selectionMode;
	private final String coordinateSystem;
	private final Unit sourceUnit;
	private final Unit targetUnit;
	private final List<Entity> entities;
	private final List<Diagnostic> diagnostics;

	/**
	 * @param selectionMode source population
	 * @param entities neutral entities
	 * @param diagnostics explicit omissions
	 */
	public GeometryExportModel(SelectionMode selectionMode, List<Entity> entities,
			List<Diagnostic> diagnostics) {
		this.selectionMode = require(selectionMode, "selection mode");
		this.coordinateSystem = "GEOGEBRA_CARTESIAN_2D_WORLD";
		this.sourceUnit = Unit.UNITLESS;
		this.targetUnit = Unit.UNITLESS;
		this.entities = immutableCopy(entities);
		this.diagnostics = immutableCopy(diagnostics);
	}

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public String getCoordinateSystem() {
		return coordinateSystem;
	}

	public Unit getSourceUnit() {
		return sourceUnit;
	}

	public Unit getTargetUnit() {
		return targetUnit;
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public List<Diagnostic> getDiagnostics() {
		return diagnostics;
	}

	private static double normalizeDegrees(double angle) {
		double normalized = angle % 360;
		return normalized < 0 ? normalized + 360 : normalized;
	}

	private static double normalizeZero(double value) {
		return value == 0 ? 0 : value;
	}

	private static void assertFinite(double value, String name) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			throw new IllegalArgumentException(name + " must be finite");
		}
	}

	private static void assertPositive(double value, String name) {
		assertFinite(value, name);
		if (value <= 0) {
			throw new IllegalArgumentException(name + " must be positive");
		}
	}

	private static void assertColor(int value) {
		if (value < 0 || value > 255) {
			throw new IllegalArgumentException("Color channel outside 0..255");
		}
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static <T> T require(T value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static <T> List<T> immutableCopy(List<T> values) {
		if (values == null) {
			throw new IllegalArgumentException("List is required");
		}
		return Collections.unmodifiableList(new ArrayList<>(values));
	}
}

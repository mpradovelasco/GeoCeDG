/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.geocedg.common.export.GeometryExportModel.ArcGeometry;
import org.geocedg.common.export.GeometryExportModel.CircleGeometry;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.EllipseGeometry;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Exactness;
import org.geocedg.common.export.GeometryExportModel.Geometry;
import org.geocedg.common.export.GeometryExportModel.GeometryType;
import org.geocedg.common.export.GeometryExportModel.LinearGeometry;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PointGeometry;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportModel.Style;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.kernel.matrix.Coords;

/**
 * Sole G5 boundary that reads GeoGebra geometry and creates neutral entities.
 */
public final class GeoElementGeometryExportAdapter {

	private static final double DIRECTION_EPSILON = 1E-14;

	/**
	 * @param geos ordered source population
	 * @param selectionMode source population provenance
	 * @return immutable neutral model
	 */
	public GeometryExportModel adapt(Collection<GeoElement> geos,
			SelectionMode selectionMode) {
		if (geos == null) {
			throw new IllegalArgumentException("Source geometry collection is required");
		}
		List<GeoElement> ordered = new ArrayList<>(geos);
		Set<GeoElement> polygonSides = polygonSides(ordered);
		List<Entity> entities = new ArrayList<>();
		List<Diagnostic> diagnostics = new ArrayList<>();
		for (int ordinal = 0; ordinal < ordered.size(); ordinal++) {
			GeoElement geo = ordered.get(ordinal);
			if (geo == null) {
				continue;
			}
			String sourceId = sourceId(geo, ordinal);
			String sourceType = geo.getGeoClassType().name();
			if (polygonSides.contains(geo)) {
				diagnostics.add(new Diagnostic(sourceId, sourceType,
						DiagnosticCode.DUPLICATE_POLYGON_SIDE,
						"Generated polygon side omitted because its polygon boundary is present."));
				continue;
			}
			if (!geo.isDefined()) {
				diagnostics.add(diagnostic(geo, sourceId, DiagnosticCode.UNDEFINED,
						"Undefined source object cannot be exported."));
				continue;
			}
			if (geo.isGeoElement3D()) {
				diagnostics.add(diagnostic(geo, sourceId, DiagnosticCode.NOT_2D,
						"G5 exports only resolved 2D geometry."));
				continue;
			}
			try {
				Geometry geometry = adaptGeometry(geo);
				if (geometry == null) {
					diagnostics.add(diagnostic(geo, sourceId,
							DiagnosticCode.UNSUPPORTED,
							unsupportedMessage(geo)));
					continue;
				}
				entities.add(new Entity(sourceId, sourceType, geo.getLabelSimple(),
						layerName(geo.getLayer()), style(geo), Exactness.EXACT,
						null, geometry));
			} catch (IllegalArgumentException exception) {
				diagnostics.add(diagnostic(geo, sourceId, DiagnosticCode.DEGENERATE,
						exception.getMessage()));
			}
		}
		return new GeometryExportModel(selectionMode, entities, diagnostics);
	}

	private Geometry adaptGeometry(GeoElement geo) {
		if (geo instanceof GeoPoint) {
			GeoPoint point = (GeoPoint) geo;
			if (!point.isFinite()) {
				throw new IllegalArgumentException("Infinite point cannot be exported.");
			}
			return new PointGeometry(point(point));
		}
		if (geo instanceof GeoSegment) {
			GeoSegment segment = (GeoSegment) geo;
			return new LinearGeometry(GeometryType.SEGMENT,
					point(segment.getStartPoint()), point(segment.getEndPoint()));
		}
		if (geo instanceof GeoRay) {
			GeoRay ray = (GeoRay) geo;
			Point2D start = point(ray.getStartPoint());
			Point2D next = point(ray.getPointInD(2, 1));
			return new LinearGeometry(GeometryType.RAY, start,
					unitDirection(start, next));
		}
		if (geo instanceof org.geogebra.common.kernel.geos.GeoLine) {
			org.geogebra.common.kernel.geos.GeoLine line =
					(org.geogebra.common.kernel.geos.GeoLine) geo;
			Point2D base = point(line.getPointInD(2, 0));
			Point2D next = point(line.getPointInD(2, 1));
			return new LinearGeometry(GeometryType.INFINITE_LINE, base,
					unitDirection(base, next));
		}
		if (geo instanceof GeoPolygon) {
			GeoPolygon polygon = (GeoPolygon) geo;
			return new PolylineGeometry(points(polygon.getPoints()), true);
		}
		if (geo instanceof GeoPolyLine) {
			GeoPolyLine polyline = (GeoPolyLine) geo;
			return new PolylineGeometry(points(polyline.getPoints()), false);
		}
		if (geo instanceof GeoConicPart) {
			return adaptConicPart((GeoConicPart) geo);
		}
		if (geo instanceof GeoConic) {
			return adaptConic((GeoConic) geo);
		}
		return null;
	}

	private Geometry adaptConic(GeoConic conic) {
		if (conic.getType() == GeoConicNDConstants.CONIC_CIRCLE) {
			return new CircleGeometry(point(conic.getMidpoint2D()),
					conic.getCircleRadius());
		}
		if (conic.getType() == GeoConicNDConstants.CONIC_ELLIPSE) {
			return ellipse(conic, 0, Math.PI * 2);
		}
		return null;
	}

	private Geometry adaptConicPart(GeoConicPart conic) {
		if (conic.getConicPartType() != GeoConicNDConstants.CONIC_PART_ARC) {
			return null;
		}
		if (conic.getType() == GeoConicNDConstants.CONIC_CIRCLE) {
			return circularArc(conic);
		}
		if (conic.getType() == GeoConicNDConstants.CONIC_ELLIPSE) {
			double start = conic.getParameterStart();
			double end = conic.getParameterEnd();
			if (basisDeterminant(conic) < 0) {
				double transformedStart = -end;
				end = -start;
				start = transformedStart;
			}
			return ellipse(conic, normalizeRadians(start), normalizeRadians(end));
		}
		return null;
	}

	private Geometry circularArc(GeoConicPart conic) {
		double start = actualAngle(conic, conic.getParameterStart());
		double end = actualAngle(conic, conic.getParameterEnd());
		if (basisDeterminant(conic) < 0) {
			double swap = start;
			start = end;
			end = swap;
		}
		return new ArcGeometry(point(conic.getMidpoint2D()),
				conic.getCircleRadius(), Math.toDegrees(start), Math.toDegrees(end));
	}

	private EllipseGeometry ellipse(GeoConic conic, double start, double end) {
		Coords axis = conic.getEigenvec(0);
		double majorRadius = conic.getHalfAxis(0);
		Point2D majorVector = new Point2D(axis.getX() * majorRadius,
				axis.getY() * majorRadius);
		return new EllipseGeometry(point(conic.getMidpoint2D()), majorVector,
				conic.getHalfAxis(1) / majorRadius, start, end);
	}

	private static double actualAngle(GeoConic conic, double parameter) {
		Coords first = conic.getEigenvec(0);
		Coords second = conic.getEigenvec(1);
		double x = first.getX() * Math.cos(parameter)
				+ second.getX() * Math.sin(parameter);
		double y = first.getY() * Math.cos(parameter)
				+ second.getY() * Math.sin(parameter);
		return Math.atan2(y, x);
	}

	private static double basisDeterminant(GeoConic conic) {
		Coords first = conic.getEigenvec(0);
		Coords second = conic.getEigenvec(1);
		return first.getX() * second.getY() - first.getY() * second.getX();
	}

	private static List<Point2D> points(GeoPointND[] source) {
		if (source == null) {
			throw new IllegalArgumentException("Vertex list is unavailable.");
		}
		List<Point2D> result = new ArrayList<>();
		for (GeoPointND sourcePoint : source) {
			result.add(point(sourcePoint));
		}
		return result;
	}

	private static Point2D point(GeoPointND source) {
		if (source == null || !source.isFinite()) {
			throw new IllegalArgumentException("Required point is non-finite.");
		}
		return new Point2D(source.getInhomX(), source.getInhomY());
	}

	private static Point2D point(Coords source) {
		if (source == null) {
			throw new IllegalArgumentException("Required coordinates are unavailable.");
		}
		return new Point2D(source.getX(), source.getY());
	}

	private static Point2D unitDirection(Point2D start, Point2D next) {
		double x = next.getX() - start.getX();
		double y = next.getY() - start.getY();
		double norm = Math.sqrt(x * x + y * y);
		if (!isFinite(norm) || norm <= DIRECTION_EPSILON) {
			throw new IllegalArgumentException("Linear direction is degenerate.");
		}
		return new Point2D(x / norm, y / norm);
	}

	private static Set<GeoElement> polygonSides(List<GeoElement> geos) {
		Set<GeoElement> input = Collections.newSetFromMap(new IdentityHashMap<>());
		input.addAll(geos);
		Set<GeoElement> result = Collections.newSetFromMap(new IdentityHashMap<>());
		for (GeoElement geo : geos) {
			if (geo instanceof GeoPolygon) {
				GeoSegmentND[] segments = ((GeoPolygon) geo).getSegments();
				if (segments != null) {
					for (GeoSegmentND segment : segments) {
						GeoElement side = segment.toGeoElement();
						if (input.contains(side)) {
							result.add(side);
						}
					}
				}
			}
		}
		return result;
	}

	private static Style style(GeoElement geo) {
		GColor color = geo.getObjectColor();
		return new Style(color.getRed(), color.getGreen(), color.getBlue(),
				geo.isEuclidianVisible());
	}

	private static String layerName(int layer) {
		return layer == 0 ? "0" : "GEOCEDG_L" + layer;
	}

	private static String sourceId(GeoElement geo, int ordinal) {
		String label = geo.getLabelSimple();
		String suffix = label == null || label.isEmpty() ? "item-" + ordinal
				: label.replaceAll("[^A-Za-z0-9_.-]", "_");
		return "geo-" + geo.getConstructionIndex() + "-" + suffix;
	}

	private static Diagnostic diagnostic(GeoElement geo, String sourceId,
			DiagnosticCode code, String message) {
		return new Diagnostic(sourceId, geo.getGeoClassType().name(), code, message);
	}

	private static String unsupportedMessage(GeoElement geo) {
		if (geo.isGeoLocus()) {
			return "Legacy Locus is unsupported: G5 does not export sampled display data.";
		}
		return "No exact G5 mapping exists for " + geo.getTypeString() + ".";
	}

	private static double normalizeRadians(double value) {
		double normalized = value % (Math.PI * 2);
		return normalized < 0 ? normalized + Math.PI * 2 : normalized;
	}

	private static boolean isFinite(double value) {
		return !Double.isNaN(value) && !Double.isInfinite(value);
	}
}

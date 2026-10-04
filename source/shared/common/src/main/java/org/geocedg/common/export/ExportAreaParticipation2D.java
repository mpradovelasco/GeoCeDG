/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.math.BigDecimal;
import java.util.List;

import org.geocedg.common.export.GeometryExportModel.ArcGeometry;
import org.geocedg.common.export.GeometryExportModel.CircleGeometry;
import org.geocedg.common.export.GeometryExportModel.EllipseGeometry;
import org.geocedg.common.export.GeometryExportModel.Geometry;
import org.geocedg.common.export.GeometryExportModel.LinearGeometry;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PointGeometry;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;

/**
 * PRE-G9B-R6-plus-C (author decision {@code DQ-C13}, choice {@code B1}): whether
 * the exported support of one exact neutral entity meets a closed axis-aligned
 * export area. Curves are boundaries, never filled regions: a circle or polygon
 * that encloses the area without meeting it does not participate.
 *
 * <p>Points, segments, rays, lines, polylines, polygons and circles are decided
 * exactly: their binary64 inputs are converted to {@link BigDecimal} and every
 * comparison is an exact sign test (separating-axis tests for linear supports,
 * squared distances for circles). Circular and elliptic arcs use closed-form
 * edge intersections in binary64 with a deterministic tie rule that resolves
 * near-touching cases toward participation, so that nothing on the boundary is
 * dropped. No predicate emits or changes geometry.
 */
final class ExportAreaParticipation2D {

	/** Relative width of the participation tie band of the arc predicates. */
	private static final double TIE_RELATIVE = 1E-12;
	private static final double FULL_TURN = Math.PI * 2;

	private ExportAreaParticipation2D() {
		// predicates only
	}

	/**
	 * @param geometry exact neutral geometry
	 * @param area explicit closed export area
	 * @return whether the geometry meets the closed area
	 */
	static boolean meets(Geometry geometry, GeometryExportArea area) {
		if (geometry == null || area == null || !area.isBoundary()) {
			throw new IllegalArgumentException(
					"Geometry and an explicit export area are required");
		}
		Box box = new Box(area);
		switch (geometry.getType()) {
		case POINT:
			return box.contains(((PointGeometry) geometry).getPoint());
		case SEGMENT:
			LinearGeometry segment = (LinearGeometry) geometry;
			return segmentMeets(segment.getStart(), segment.getVector(), box);
		case RAY:
			LinearGeometry ray = (LinearGeometry) geometry;
			return rayMeets(ray.getStart(), ray.getVector(), box);
		case INFINITE_LINE:
			LinearGeometry line = (LinearGeometry) geometry;
			return !box.strictlyOneSide(line.getStart(), line.getVector());
		case POLYLINE:
			return polylineMeets((PolylineGeometry) geometry, box);
		case CIRCLE:
			CircleGeometry circle = (CircleGeometry) geometry;
			return circleMeets(circle.getCenter(), circle.getRadius(), box);
		case ARC:
			return arcMeets((ArcGeometry) geometry, box);
		case ELLIPSE:
			return ellipseMeets((EllipseGeometry) geometry, box);
		default:
			// An unknown family is never excluded.
			return true;
		}
	}

	private static boolean segmentMeets(Point2D start, Point2D end, Box box) {
		if (Math.max(start.getX(), end.getX()) < box.xmin
				|| Math.min(start.getX(), end.getX()) > box.xmax
				|| Math.max(start.getY(), end.getY()) < box.ymin
				|| Math.min(start.getY(), end.getY()) > box.ymax) {
			return false;
		}
		BigDecimal dx = exact(end.getX()).subtract(exact(start.getX()));
		BigDecimal dy = exact(end.getY()).subtract(exact(start.getY()));
		if (dx.signum() == 0 && dy.signum() == 0) {
			return box.contains(start);
		}
		return !box.strictlyOneSide(start, dx, dy);
	}

	private static boolean rayMeets(Point2D start, Point2D direction, Box box) {
		if (separatedAlong(start.getX(), direction.getX(), box.xmin, box.xmax)
				|| separatedAlong(start.getY(), direction.getY(), box.ymin,
						box.ymax)) {
			return false;
		}
		return !box.strictlyOneSide(start, direction);
	}

	private static boolean separatedAlong(double origin, double direction,
			double min, double max) {
		if (direction > 0) {
			return origin > max;
		}
		if (direction < 0) {
			return origin < min;
		}
		return origin < min || origin > max;
	}

	private static boolean polylineMeets(PolylineGeometry polyline, Box box) {
		List<Point2D> vertices = polyline.getVertices();
		for (int index = 0; index + 1 < vertices.size(); index++) {
			if (segmentMeets(vertices.get(index), vertices.get(index + 1), box)) {
				return true;
			}
		}
		return polyline.isClosed() && segmentMeets(vertices.get(vertices.size() - 1),
				vertices.get(0), box);
	}

	private static boolean circleMeets(Point2D center, double radius, Box box) {
		BigDecimal cx = exact(center.getX());
		BigDecimal cy = exact(center.getY());
		BigDecimal nearestX = distanceToRange(cx, box.exactXmin, box.exactXmax);
		BigDecimal nearestY = distanceToRange(cy, box.exactYmin, box.exactYmax);
		BigDecimal r = exact(radius);
		BigDecimal radiusSquared = r.multiply(r);
		BigDecimal nearestSquared = nearestX.multiply(nearestX)
				.add(nearestY.multiply(nearestY));
		if (nearestSquared.compareTo(radiusSquared) > 0) {
			return false;
		}
		BigDecimal farX = cx.subtract(box.exactXmin).abs()
				.max(cx.subtract(box.exactXmax).abs());
		BigDecimal farY = cy.subtract(box.exactYmin).abs()
				.max(cy.subtract(box.exactYmax).abs());
		BigDecimal farSquared = farX.multiply(farX).add(farY.multiply(farY));
		return radiusSquared.compareTo(farSquared) <= 0;
	}

	private static BigDecimal distanceToRange(BigDecimal value, BigDecimal min,
			BigDecimal max) {
		if (value.compareTo(min) < 0) {
			return min.subtract(value);
		}
		if (value.compareTo(max) > 0) {
			return value.subtract(max);
		}
		return BigDecimal.ZERO;
	}

	private static boolean arcMeets(ArcGeometry arc, Box box) {
		if (!circleMeets(arc.getCenter(), arc.getRadius(), box)) {
			return false;
		}
		double cx = arc.getCenter().getX();
		double cy = arc.getCenter().getY();
		double radius = arc.getRadius();
		double start = Math.toRadians(arc.getStartAngleDegrees());
		double end = Math.toRadians(arc.getEndAngleDegrees());
		double tie = tieBand(box, cx, cy, radius, radius);
		if (box.containsWithin(cx + radius * Math.cos(start),
				cy + radius * Math.sin(start), tie)
				|| box.containsWithin(cx + radius * Math.cos(end),
						cy + radius * Math.sin(end), tie)) {
			return true;
		}
		return curveCrossesEdges(cx, cy, radius, 0, 0, radius, start, end,
				box, tie);
	}

	private static boolean ellipseMeets(EllipseGeometry ellipse, Box box) {
		double cx = ellipse.getCenter().getX();
		double cy = ellipse.getCenter().getY();
		double mx = ellipse.getMajorAxis().getX();
		double my = ellipse.getMajorAxis().getY();
		double ratio = ellipse.getMinorMajorRatio();
		double nx = -my * ratio;
		double ny = mx * ratio;
		double start = ellipse.getStartParameter();
		double end = ellipse.getEndParameter();
		double reach = Math.abs(mx) + Math.abs(nx) + Math.abs(my) + Math.abs(ny);
		double tie = tieBand(box, cx, cy, reach, reach);
		if (box.containsWithin(cx + mx * Math.cos(start) + nx * Math.sin(start),
				cy + my * Math.cos(start) + ny * Math.sin(start), tie)
				|| box.containsWithin(cx + mx * Math.cos(end) + nx * Math.sin(end),
						cy + my * Math.cos(end) + ny * Math.sin(end), tie)) {
			return true;
		}
		return curveCrossesEdges(cx, cy, mx, nx, my, ny, start, end, box, tie);
	}

	/**
	 * Point(t) = (cx + a cos t + b sin t, cy + c cos t + d sin t); a circle uses
	 * a = d = r and b = c = 0. Every edge line of the box is intersected in closed
	 * form, and an intersection counts when it lies on the closed edge and inside
	 * the counterclockwise parameter range from start to end.
	 */
	private static boolean curveCrossesEdges(double cx, double cy, double a,
			double b, double c, double d, double start, double end, Box box,
			double tie) {
		return crossesLine(a, b, box.xmin - cx, cy, c, d, box.ymin, box.ymax,
				start, end, tie)
				|| crossesLine(a, b, box.xmax - cx, cy, c, d, box.ymin, box.ymax,
						start, end, tie)
				|| crossesLine(c, d, box.ymin - cy, cx, a, b, box.xmin, box.xmax,
						start, end, tie)
				|| crossesLine(c, d, box.ymax - cy, cx, a, b, box.xmin, box.xmax,
						start, end, tie);
	}

	/**
	 * Solves {@code p cos t + q sin t = target} and checks the other coordinate
	 * {@code origin + r cos t + s sin t} against the closed edge range.
	 */
	private static boolean crossesLine(double p, double q, double target,
			double origin, double r, double s, double min, double max,
			double start, double end, double tie) {
		double amplitude = Math.hypot(p, q);
		if (!(amplitude > 0)) {
			return false;
		}
		double ratio = target / amplitude;
		if (ratio > 1 + TIE_RELATIVE || ratio < -1 - TIE_RELATIVE) {
			return false;
		}
		double phase = Math.atan2(q, p);
		double offset = Math.acos(Math.max(-1, Math.min(1, ratio)));
		double[] candidates = {phase + offset, phase - offset};
		for (double candidate : candidates) {
			double other = origin + r * Math.cos(candidate) + s * Math.sin(candidate);
			if (other >= min - tie && other <= max + tie
					&& withinCounterclockwise(candidate, start, end, tie)) {
				return true;
			}
		}
		return false;
	}

	private static boolean withinCounterclockwise(double angle, double start,
			double end, double tie) {
		double span = normalize(end - start);
		if (span == 0) {
			// equal normalized ends: the full closed curve
			span = FULL_TURN;
		}
		double offset = normalize(angle - start);
		return offset <= span + TIE_RELATIVE * FULL_TURN
				|| offset >= FULL_TURN - TIE_RELATIVE * FULL_TURN;
	}

	private static double normalize(double angle) {
		double normalized = angle % FULL_TURN;
		return normalized < 0 ? normalized + FULL_TURN : normalized;
	}

	private static double tieBand(Box box, double cx, double cy, double reachX,
			double reachY) {
		double scale = Math.max(Math.max(Math.abs(box.xmin), Math.abs(box.xmax)),
				Math.max(Math.abs(box.ymin), Math.abs(box.ymax)));
		scale = Math.max(scale, Math.max(Math.abs(cx) + Math.abs(reachX),
				Math.abs(cy) + Math.abs(reachY)));
		return TIE_RELATIVE * Math.max(1, scale);
	}

	private static BigDecimal exact(double value) {
		return new BigDecimal(value);
	}

	private static final class Box {
		private final double xmin;
		private final double xmax;
		private final double ymin;
		private final double ymax;
		private final BigDecimal exactXmin;
		private final BigDecimal exactXmax;
		private final BigDecimal exactYmin;
		private final BigDecimal exactYmax;

		private Box(GeometryExportArea area) {
			xmin = area.getXmin();
			xmax = area.getXmax();
			ymin = area.getYmin();
			ymax = area.getYmax();
			exactXmin = exact(xmin);
			exactXmax = exact(xmax);
			exactYmin = exact(ymin);
			exactYmax = exact(ymax);
		}

		private boolean contains(Point2D point) {
			return point.getX() >= xmin && point.getX() <= xmax
					&& point.getY() >= ymin && point.getY() <= ymax;
		}

		private boolean containsWithin(double x, double y, double tie) {
			return x >= xmin - tie && x <= xmax + tie && y >= ymin - tie
					&& y <= ymax + tie;
		}

		/**
		 * @return whether every corner lies strictly on the same side of the line
		 *         through the origin with the direction
		 */
		private boolean strictlyOneSide(Point2D origin, Point2D direction) {
			return strictlyOneSide(origin, exact(direction.getX()),
					exact(direction.getY()));
		}

		private boolean strictlyOneSide(Point2D origin, BigDecimal dx,
				BigDecimal dy) {
			BigDecimal ox = exact(origin.getX());
			BigDecimal oy = exact(origin.getY());
			int first = side(ox, oy, dx, dy, exactXmin, exactYmin);
			if (first == 0) {
				return false;
			}
			return side(ox, oy, dx, dy, exactXmax, exactYmin) == first
					&& side(ox, oy, dx, dy, exactXmax, exactYmax) == first
					&& side(ox, oy, dx, dy, exactXmin, exactYmax) == first;
		}

		private static int side(BigDecimal ox, BigDecimal oy, BigDecimal dx,
				BigDecimal dy, BigDecimal x, BigDecimal y) {
			return dx.multiply(y.subtract(oy)).subtract(dy.multiply(x.subtract(ox)))
					.signum();
		}
	}
}

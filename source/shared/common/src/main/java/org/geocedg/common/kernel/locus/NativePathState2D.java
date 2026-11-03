/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;

/**
 * Immutable native defining geometry and parameter-domain evidence for CU7 R3.
 * It is neither a geometric solver nor an identity. Callers independently bind
 * the source/document, or a private rollback correspondence, before using it.
 */
public final class NativePathState2D {

	private final Class<?> type;
	private final boolean defined;
	private final boolean closed;
	private final long[] values;

	private NativePathState2D(GeoElement source, List<Double> state) {
		type = source.getClass();
		defined = source.isDefined();
		closed = ((Path) source).isClosedPath();
		values = new long[state.size()];
		for (int index = 0; index < state.size(); index++) {
			values[index] = Double.doubleToLongBits(state.get(index));
		}
	}

	/**
	 * Captures the supported native 2D table, without mutation or projection.
	 * @param source actual native source
	 * @return defining state, or {@code null} for a source outside this table
	 */
	public static NativePathState2D capture(GeoElement source) {
		if (source == null || !(source instanceof Path)) {
			return null;
		}
		Class<?> type = source.getClass();
		List<Double> state = new ArrayList<>();
		if (type == GeoLine.class || type == GeoAxis.class
				|| type == GeoRay.class || type == GeoSegment.class) {
			GeoLine line = (GeoLine) source;
			state.add(line.x); state.add(line.y); state.add(line.z);
			point(state, line.getStartPoint());
			point(state, line.getEndPoint());
			if (type == GeoSegment.class) {
				state.add(((GeoSegment) source).getLength());
			}
		} else if (type == GeoConic.class || type == GeoConicPart.class) {
			GeoConic conic = (GeoConic) source;
			state.add((double) conic.getType());
			for (double coefficient : conic.getMatrix()) {
				state.add(coefficient);
			}
			state.add(conic.getHalfAxes()[0]); state.add(conic.getHalfAxes()[1]);
			state.add(conic.getEigenvec(0).getX()); state.add(conic.getEigenvec(0).getY());
			state.add(conic.getEigenvec(1).getX()); state.add(conic.getEigenvec(1).getY());
			state.add(conic.getTranslationVector().getX());
			state.add(conic.getTranslationVector().getY()); state.add(conic.p);
			if (type == GeoConicPart.class) {
				GeoConicPart part = (GeoConicPart) source;
				state.add((double) part.getConicPartType());
				state.add(part.getParameterStart()); state.add(part.getParameterEnd());
				state.add(part.getParameterExtent());
				state.add(part.positiveOrientation() ? 1.0 : 0.0);
			}
		} else {
			return null;
		}
		Path path = (Path) source;
		state.add(path.getMinParameter()); state.add(path.getMaxParameter());
		return new NativePathState2D(source, state);
	}

	private static void point(List<Double> state, GeoPoint point) {
		state.add(point == null ? 0.0 : 1.0);
		if (point != null) {
			state.add(point.isDefined() ? 1.0 : 0.0);
			state.add(point.x); state.add(point.y); state.add(point.z);
		}
	}

	/** @return whether the current defining geometry/domain has exactly these bits */
	public boolean matches(GeoElement source) {
		NativePathState2D current = capture(source);
		return current != null && type == current.type && defined == current.defined
				&& closed == current.closed && Arrays.equals(values, current.values);
	}

	/** @return whether the captured native source is defined */
	public boolean isDefined() {
		return defined;
	}
}

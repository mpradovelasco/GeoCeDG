/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import org.geocedg.common.math.bspline.BSplineCanonicalEncoding;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineLimits;
import org.geocedg.common.math.bspline.BSplineValidation;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * Immutable captured definition of one {@code BSpline} revision ({@code bspline-2d.md}
 * sections 3, 4 and 7): the binary64 data validated by the shared library, the knot rule,
 * or exactly one typed cause. The capture reads the current values of the DAG inputs and
 * keeps no reference to them.
 */
public final class BSplineDefinition2D {
	/** Contract identifier of the family. */
	public static final String CONTRACT = "bspline-2d/v1";
	/** Limits of {@code v1}: degree {@code 1..7}, at most 128 control points. */
	public static final BSplineLimits LIMITS = BSplineLimits.of(7, 128);
	/** Degree of the form without a degree argument. */
	public static final int DEFAULT_DEGREE = 3;

	private final String knotRule;
	private final BSplineData data;
	private final BSplineInvalidity invalidity;

	private BSplineDefinition2D(String knotRule, BSplineData data,
			BSplineInvalidity invalidity) {
		this.knotRule = knotRule;
		this.data = data;
		this.invalidity = invalidity;
	}

	/**
	 * Captures the current inputs. Causes are decided in the order of section 7: undefined
	 * inputs first, then the library's validation of the data.
	 *
	 * @param points list of control points
	 * @param degree degree input
	 * @param knots explicit knot list, or null for the rule {@code clamped-uniform/v1}
	 * @return valid definition or typed invalidity
	 */
	public static BSplineDefinition2D capture(GeoList points, GeoNumberValue degree,
			GeoList knots) {
		String rule = knots == null ? BSplineContractXml.KNOTS_CLAMPED_UNIFORM_V1
				: BSplineContractXml.KNOTS_EXPLICIT;
		if (!isDefined(points) || degree == null || !degree.toGeoElement().isDefined()
				|| knots != null && !isDefined(knots)) {
			return invalid(rule);
		}
		int count = points.size();
		double[] xs = new double[count];
		double[] ys = new double[count];
		for (int index = 0; index < count; index++) {
			GeoElement element = points.get(index);
			if (!(element instanceof GeoPoint) || element.isGeoElement3D()
					|| !element.isDefined()) {
				return invalid(rule);
			}
			GeoPoint point = (GeoPoint) element;
			xs[index] = point.getInhomX();
			ys[index] = point.getInhomY();
		}
		double[] knotValues = null;
		if (knots != null) {
			knotValues = new double[knots.size()];
			for (int index = 0; index < knots.size(); index++) {
				GeoElement element = knots.get(index);
				if (!(element instanceof GeoNumberValue) || !element.isDefined()) {
					return invalid(rule);
				}
				knotValues[index] = ((GeoNumberValue) element).getDouble();
			}
		}
		double degreeValue = degree.getDouble();
		int p = Double.isFinite(degreeValue) && degreeValue == Math.rint(degreeValue)
				&& Math.abs(degreeValue) <= 1024 ? (int) degreeValue : -1;
		if (knots == null) {
			knotValues = clampedUniform(p, count);
		}
		BSplineValidation validation = BSplineData.validate(p, knotValues, xs, ys, null,
				LIMITS);
		if (!validation.isValid()) {
			return new BSplineDefinition2D(rule, null,
					BSplineInvalidity.of(validation.cause()));
		}
		return new BSplineDefinition2D(rule, validation.data(), null);
	}

	/**
	 * Rule {@code clamped-uniform/v1} (section 4.2): {@code p + 1} zeros, the integers
	 * {@code 1..n-p}, then {@code p + 1} copies of {@code n - p + 1}. Without an admissible
	 * degree or size the rule generates no knots, so the earlier cause of section 7 is the
	 * one reported.
	 *
	 * @param degree degree, or -1 when not admissible
	 * @param controlPointCount {@code n + 1}
	 * @return generated knots
	 */
	public static double[] clampedUniform(int degree, int controlPointCount) {
		if (degree < 1 || degree > LIMITS.maxDegree() || controlPointCount < degree + 1
				|| controlPointCount > LIMITS.maxControlPoints()) {
			return new double[0];
		}
		int n = controlPointCount - 1;
		double[] knots = new double[n + degree + 2];
		for (int index = 0; index < knots.length; index++) {
			if (index <= degree) {
				knots[index] = 0;
			} else if (index <= n) {
				knots[index] = index - degree;
			} else {
				knots[index] = n - degree + 1;
			}
		}
		return knots;
	}

	private static boolean isDefined(GeoList list) {
		return list != null && list.isDefined();
	}

	private static BSplineDefinition2D invalid(String rule) {
		return new BSplineDefinition2D(rule, null, BSplineInvalidity.INPUT_UNDEFINED);
	}

	/** @return whether the revision is valid */
	public boolean isValid() {
		return invalidity == null;
	}

	/** @return the single primary cause, or null when valid */
	public BSplineInvalidity getInvalidity() {
		return invalidity;
	}

	/** @return validated library data, or null when invalid */
	public BSplineData getData() {
		return data;
	}

	/** @return persisted knot rule of this form */
	public String getKnotRule() {
		return knotRule;
	}

	/**
	 * @return SHA-256 definition digest of section 12.3 (derived, never persisted, never
	 *         identity), or null when invalid; reserved for MAINT-A-DXF1 applicability
	 */
	public String getDefinitionDigestHex() {
		return data == null ? null : BSplineCanonicalEncoding.digestHex(CONTRACT, data);
	}
}

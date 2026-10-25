/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationBudget;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * {@code Point(L, D)} and {@code Intersect(L, r, D)} (PRE-G9B-CU3 Stage A under the constructive
 * authority, ADR 0040): the point of the Locus V2 {@code L} at the driver position that
 * corresponds to the point {@code D}, and, in the second form, that point when its incidence with
 * the target {@code r} is established or admitted. The four facts are resolved by
 * {@link StructuralPointResolver}; a failed fact leaves the point undefined with its typed cause.
 * The point serializes as the ordinary command with its arguments: no parameter, certificate,
 * integrity or coordinate is persisted, and every revision recomputes its evidence; a drag of the
 * driver of {@code L} changes no input and repeats nothing.
 */
public final class AlgoLocusDriverCorrespondencePoint2D extends AlgoElement {
	private final GeoLocusV2 source;
	private final GeoPoint driverPoint;
	private final GeoElement target;
	private final GeoPoint point;
	private StructuralPointResolution resolution;

	/**
	 * @param construction construction
	 * @param source the locus {@code L}
	 * @param target the target {@code r}, or {@code null} for {@code Point(L, D)}
	 * @param driverPoint the point {@code D}
	 */
	public AlgoLocusDriverCorrespondencePoint2D(Construction construction, GeoLocusV2 source,
			GeoElement target, GeoPoint driverPoint) {
		super(construction);
		this.source = Objects.requireNonNull(source);
		this.target = target;
		this.driverPoint = Objects.requireNonNull(driverPoint);
		this.point = new GeoPoint(construction);
		setInputOutput();
		compute();
	}

	@Override
	protected void setInputOutput() {
		input = target == null ? new GeoElement[] {source, driverPoint}
				: new GeoElement[] {source, target, driverPoint};
		setOnlyOutput(point);
		setDependencies();
	}

	@Override
	public void compute() {
		resolution = StructuralPointResolver.resolve(source, driverPoint, target,
				ExactEvaluationBudget.V1, resolution);
		if (resolution.isDefined()) {
			point.setCoords(resolution.getX(), resolution.getY(), 1);
		} else {
			point.setUndefined();
		}
	}

	@Override
	public Commands getClassName() {
		return target == null ? Commands.Point : Commands.Intersect;
	}

	/** @return the output point */
	public GeoPoint getPoint() {
		return point;
	}

	/** @return the locus {@code L} */
	public GeoLocusV2 getSource() {
		return source;
	}

	/** @return the target {@code r}, or {@code null} */
	public GeoElement getTarget() {
		return target;
	}

	/** @return the point {@code D} */
	public GeoPoint getDriverPoint() {
		return driverPoint;
	}

	/**
	 * @return the resolution of the current revision: position and declared error, integrity
	 *         records and transfer, or the typed cause of an undefined point
	 */
	public StructuralPointResolution getResolution() {
		return resolution;
	}
}

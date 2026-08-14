/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;

/** Internal ordinary-point consumer selected by one semantic root token. */
public final class AlgoLocusIntersectionPointV2 extends AlgoElement {
	private final GeoLocusIntersectionResult richInput;
	private final String selectedRootToken;
	private final GeoPoint point;

	/** Creates a derived consumer; this algorithm never solves or retargets. */
	public AlgoLocusIntersectionPointV2(Construction construction,
			GeoLocusIntersectionResult richInput, String selectedRootToken) {
		super(construction, false);
		this.richInput = java.util.Objects.requireNonNull(richInput);
		if (selectedRootToken == null || selectedRootToken.trim().isEmpty()) {
			throw new IllegalArgumentException("Selected root token is required");
		}
		this.selectedRootToken = selectedRootToken;
		this.point = new GeoPoint(construction);
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[] {richInput};
		setOnlyOutput(point);
	}

	@Override
	public void compute() {
		if (!richInput.isDefined()) {
			point.setUndefined();
			return;
		}
		java.util.Optional<LocusIntersectionSolution2D> selected =
				richInput.getIntersectionResult()
						.findPointAdmissibleSolution(selectedRootToken);
		if (!selected.isPresent()) {
			point.setUndefined();
			return;
		}
		LocusPoint2D value = selected.get().getEvaluatedPoint();
		point.setCoords(value.getX(), value.getY(), 1);
	}

	public GeoLocusIntersectionResult getRichInput() {
		return richInput;
	}

	public String getSelectedRootToken() {
		return selectedRootToken;
	}

	public GeoPoint getPoint() {
		return point;
	}

	@Override
	public Algos getClassName() {
		return Algos.Expression;
	}
}

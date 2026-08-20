/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Optional;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;

/** Internal ordinary-point consumer selected by one semantic root token. */
public final class AlgoLocusIntersectionPointV2 extends AlgoElement {
	private final GeoLocusIntersectionResult richInput;
	private final String selectedRootToken;
	private final GeoText tokenInput;
	private final GetCommand commandName;
	private final GeoPoint point;
	private String effectiveRootToken;

	/** Creates a derived consumer; this algorithm never solves or retargets. */
	public AlgoLocusIntersectionPointV2(Construction construction,
			GeoLocusIntersectionResult richInput, String selectedRootToken) {
		this(construction, richInput, selectedRootToken, null, null, false,
				Algos.Expression);
	}

	/** Creates a reconstructible public exact-token point consumer. */
	public AlgoLocusIntersectionPointV2(Construction construction, String label,
			GeoLocusIntersectionResult richInput, GeoText selectedRootToken) {
		this(construction, richInput, null,
				java.util.Objects.requireNonNull(selectedRootToken), label, true,
				Commands.Intersect);
	}

	private AlgoLocusIntersectionPointV2(Construction construction,
			GeoLocusIntersectionResult richInput, String selectedRootToken,
			GeoText tokenInput, String label, boolean addToConstructionList,
			GetCommand commandName) {
		super(construction, addToConstructionList);
		this.richInput = java.util.Objects.requireNonNull(richInput);
		if (tokenInput == null && (selectedRootToken == null
				|| selectedRootToken.trim().isEmpty())) {
			throw new IllegalArgumentException("Selected root token is required");
		}
		this.selectedRootToken = selectedRootToken;
		this.tokenInput = tokenInput;
		this.commandName = commandName;
		this.point = new GeoPoint(construction);
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
		if (label != null) {
			point.setLabel(label);
		}
	}

	@Override
	protected void setInputOutput() {
		input = tokenInput == null ? new GeoElement[] {richInput}
				: new GeoElement[] {richInput, tokenInput};
		setOnlyOutput(point);
	}

	@Override
	public void compute() {
		if (!richInput.isDefined()) {
			point.setUndefined();
			return;
		}
		String requestedToken = tokenInput == null ? selectedRootToken
				: tokenInput.getTextString();
		Optional<LocusIntersectionSolution2D> selected = richInput
				.findExactPointAdmissibleSolution(requestedToken);
		if (!selected.isPresent() && tokenInput != null) {
			selected = richInput
					.rebaseCopiedPointAdmissibleSolution(requestedToken,
							tokenInput, point);
		}
		if (!selected.isPresent()) {
			effectiveRootToken = null;
			point.setUndefined();
			return;
		}
		effectiveRootToken = selected.get().getIdentity().getRootToken();
		if (tokenInput != null
				&& !effectiveRootToken.equals(tokenInput.getTextString())) {
			// Closure copy rebases the copied token input exactly once so reopen
			// never depends on a stale source-owner token.
			tokenInput.setTextString(effectiveRootToken);
		}
		LocusPoint2D value = selected.get().getEvaluatedPoint();
		point.setCoords(value.getX(), value.getY(), 1);
	}

	public GeoLocusIntersectionResult getRichInput() {
		return richInput;
	}

	public String getSelectedRootToken() {
		return tokenInput == null ? selectedRootToken : tokenInput.getTextString();
	}

	/** @return current token after any authorized copy-source rebase */
	public String getEffectiveRootToken() {
		return effectiveRootToken;
	}

	public GeoPoint getPoint() {
		return point;
	}

	@Override
	public GetCommand getClassName() {
		return commandName;
	}
}

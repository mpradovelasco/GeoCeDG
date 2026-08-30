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
	private String registeredRootToken;

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
		String requestedToken = tokenInput == null ? selectedRootToken
				: tokenInput.getTextString();
		Optional<LocusIntersectionSolution2D> selected = richInput.isDefined()
				? richInput.findExactPointAdmissibleSolution(requestedToken)
				: Optional.empty();
		if (tokenInput != null) {
			Optional<String> retained;
			if (selected.isPresent()) {
				// A current exact token is sufficient to establish the first durable
				// point claim. Copy provenance is needed only when the copied result
				// does not own the token presented by its copied GeoText yet.
				retained = Optional.of(selected.get().getIdentity().getRootToken());
			} else {
				retained = richInput.resolveRetainedMaterializedToken(
						requestedToken, tokenInput, point);
			}
			synchronizeMaterializedClaim(retained.orElse(null));
			effectiveRootToken = retained.orElse(null);
			if (retained.isPresent()
					&& !retained.get().equals(tokenInput.getTextString())) {
				// Closure copy rebases its exact dependency even while the selected
				// semantic root is temporarily dormant.
				tokenInput.setTextString(retained.get());
			}
			requestedToken = retained.orElse(requestedToken);
			if (!selected.isPresent() && retained.isPresent()
					&& richInput.isDefined()) {
				selected = richInput.findExactPointAdmissibleSolution(
						retained.get());
			}
		}
		if (!richInput.isDefined()) {
			point.setUndefined();
			return;
		}
		if (!selected.isPresent() && tokenInput == null) {
			selected = richInput.findExactPointAdmissibleSolution(requestedToken);
		}
		if (!selected.isPresent()) {
			if (tokenInput == null) {
				effectiveRootToken = null;
			}
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

	private void synchronizeMaterializedClaim(String retainedToken) {
		if (tokenInput == null
				|| java.util.Objects.equals(registeredRootToken, retainedToken)) {
			return;
		}
		String previous = registeredRootToken;
		registeredRootToken = retainedToken != null
				&& richInput.retainMaterializedPointToken(retainedToken)
						? retainedToken : null;
		if (previous != null) {
			richInput.releaseMaterializedPointToken(previous);
		}
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
	public void remove() {
		String claimed = registeredRootToken;
		registeredRootToken = null;
		if (claimed != null) {
			richInput.releaseMaterializedPointToken(claimed);
		}
		super.remove();
	}

	@Override
	public GetCommand getClassName() {
		return commandName;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.main.AppD;

/** Transient selection of exact kernel tokens in one reusable rich-result session. */
final class GeoCeDGIntersectionSession {
	private final AppD app;
	private GeoLocusIntersectionResult active;
	private PersistentGeoId activeId;
	private final Set<String> selected = new LinkedHashSet<>();
	private boolean markersVisible;
	private boolean autoMaterialize;

	GeoCeDGIntersectionSession(AppD app) {
		this.app = app;
	}

	void activate(GeoLocusIntersectionResult rich) {
		if (active != rich) {
			active = rich;
			activeId = rich == null ? null : app.getKernel().getConstruction()
					.getSpatialIdentityRegistry().getPersistentGeoId(rich);
			selected.clear();
		}
	}

	GeoLocusIntersectionResult getActive() {
		if (activeId != null) {
			var current = app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getGeo(activeId);
			if (current instanceof GeoLocusIntersectionResult) {
				// Undo/reopen may rebuild the host instance; only its exact durable
				// owner identity can restore this presentation session.
				active = (GeoLocusIntersectionResult) current;
			} else {
				active = null;
				activeId = null;
				selected.clear();
			}
		}
		return active;
	}

	List<LocusIntersectionSolution2D> solutions() {
		GeoLocusIntersectionResult rich = getActive();
		return rich == null || !rich.isDefined() ? Collections.emptyList()
				: rich.getIntersectionResult().getFiniteSolutions();
	}

	List<LocusIntersectionSolution2D> markerSolutions() {
		GeoLocusIntersectionResult rich = getActive();
		if (rich == null || !rich.isDefined()
				|| rich.getIntersectionResult().getCurrentness() != Currentness.CURRENT) {
			return Collections.emptyList();
		}
		List<LocusIntersectionSolution2D> markers = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich.getIntersectionResult()
				.getFiniteSolutions()) {
			if (rich.isPointAdmissible(solution.getIdentity().getRootToken())) {
				markers.add(solution);
			}
		}
		return markers;
	}

	Set<String> materializedTokens() {
		Set<String> tokens = new LinkedHashSet<>();
		GeoLocusIntersectionResult rich = getActive();
		if (rich != null) {
			for (AlgoElement algorithm : rich.getAlgorithmList()) {
				if (algorithm instanceof AlgoLocusIntersectionPointV2) {
					tokens.add(((AlgoLocusIntersectionPointV2) algorithm)
							.getEffectiveRootToken());
				}
			}
		}
		return tokens;
	}

	List<String> eligibleTokens() {
		List<String> tokens = new ArrayList<>();
		Set<String> existing = materializedTokens();
		for (LocusIntersectionSolution2D solution : solutions()) {
			String token = solution.getIdentity().getRootToken();
			if (active.isPointAdmissible(token) && !existing.contains(token)) {
				tokens.add(token);
			}
		}
		return tokens;
	}

	void select(Collection<String> tokens) {
		selected.clear();
		selected.addAll(tokens);
	}

	Set<String> selectedTokens() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(selected));
	}

	boolean hasEligibleSelection(boolean single) {
		List<String> eligible = eligibleTokens();
		return !selected.isEmpty() && (!single || selected.size() == 1)
				&& eligible.containsAll(selected);
	}

	List<GeoPoint> materializeSelected(boolean single, boolean storeUndo) {
		List<String> tokens = new ArrayList<>(selected);
		if (single && tokens.size() != 1) {
			return Collections.emptyList();
		}
		return materialize(tokens, storeUndo);
	}

	List<GeoPoint> materializeAll(boolean storeUndo) {
		return materialize(eligibleTokens(), storeUndo);
	}

	List<GeoPoint> materialize(Collection<String> requested, boolean storeUndo) {
		GeoLocusIntersectionResult rich = getActive();
		Set<String> tokens = new LinkedHashSet<>(requested);
		Set<String> existing = materializedTokens();
		if (rich == null) {
			return Collections.emptyList();
		}
		for (String token : tokens) {
			if (!rich.isPointAdmissible(token)) {
				throw new IllegalArgumentException("Stale or inadmissible exact token");
			}
		}
		tokens.removeAll(existing);
		List<GeoPoint> points = new ArrayList<>();
		if (tokens.isEmpty()) {
			return points;
		}
		Construction construction = app.getKernel().getConstruction();
		construction.runAtomicConstructionMutation(() -> {
			for (String token : tokens) {
				GeoText input = new GeoText(construction, token);
				input.setAuxiliaryObject(true);
				input.setEuclidianVisible(false);
				GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
						construction, null, rich, input);
				point.setLabel(null);
				points.add(point);
			}
		});
		if (storeUndo) {
			app.storeUndoInfo();
		}
		app.getKernel().notifyRepaint();
		return points;
	}

	boolean isMarkersVisible() {
		return markersVisible;
	}

	void setMarkersVisible(boolean visible) {
		markersVisible = visible;
	}

	boolean isAutoMaterialize() {
		return autoMaterialize;
	}

	void setAutoMaterialize(boolean enabled) {
		autoMaterialize = enabled;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Opaque, construction-confined participation staged while a redefine candidate
 * is parsed. Reservations are visible to the provider through this read-only
 * graph view, but no record, attachment or persistence label is published before
 * the provider decision is sealed.
 */
public final class SpatialRedefineCandidateParticipation
		implements SpatialIdentityGraph, AutoCloseable {
	enum State {
		ACTIVE,
		SEALED,
		CLAIMED,
		COMPLETED,
		ABANDONED
	}

	private final SpatialIdentityRegistry registry;
	private final SpatialRedefineContext context;
	private final IdentityHashMap<GeoElement, GeoIdentityRecord> recordsByGeo =
			new IdentityHashMap<>();
	private final Map<PersistentGeoId, GeoElement> geosById = new LinkedHashMap<>();
	private final IdentityHashMap<GeoElement, PromotionState> promotions =
			new IdentityHashMap<>();
	private final Set<ConstructionElement> entryElements =
			Collections.newSetFromMap(
					new IdentityHashMap<ConstructionElement, Boolean>());
	private final Set<GeoElement> entryGeos = Collections.newSetFromMap(
			new IdentityHashMap<GeoElement, Boolean>());
	private State state = State.ACTIVE;
	private boolean labelsActivated;

	SpatialRedefineCandidateParticipation(SpatialIdentityRegistry registry,
			SpatialRedefineContext context) {
		this.registry = Objects.requireNonNull(registry);
		this.context = Objects.requireNonNull(context);
		for (int index = 0; index < context.getOldTarget().getConstruction().steps();
				index++) {
			ConstructionElement element = context.getOldTarget().getConstruction()
					.getConstructionElement(index);
			if (element != null) {
				entryElements.add(element);
			}
		}
		entryGeos.addAll(context.getOldTarget().getConstruction()
				.getGeoSetConstructionOrder());
	}

	/**
	 * Seals the exact staged set after candidate parsing. The returned object is
	 * carried explicitly into provider preparation; it is not a live global mode.
	 *
	 * @return this sealed participation
	 */
	public SpatialRedefineCandidateParticipation seal() {
		registry.sealRedefineCandidateParticipation(this);
		return this;
	}

	/** Abandons an unclaimed preparation and releases every unused reservation. */
	public void abandon() {
		registry.abandonRedefineCandidateParticipation(this);
	}

	@Override
	public void close() {
		if (state == State.ACTIVE || state == State.SEALED) {
			abandon();
		}
	}

	@Override
	public List<SpatialIdentityRecord> getRecords() {
		ArrayList<SpatialIdentityRecord> result =
				new ArrayList<>(registry.getRecords());
		result.addAll(recordsByGeo.values());
		Collections.sort(result, new Comparator<SpatialIdentityRecord>() {
			@Override
			public int compare(SpatialIdentityRecord first,
					SpatialIdentityRecord second) {
				return first.getId().compareTo(second.getId());
			}
		});
		return Collections.unmodifiableList(result);
	}

	@Override
	public SpatialIdentityRecord getRecord(SpatialIdentityId id) {
		GeoElement staged = id instanceof PersistentGeoId
				? geosById.get(id) : null;
		return staged == null ? registry.getRecord(id) : recordsByGeo.get(staged);
	}

	@Override
	public SpatialRecordResolution getResolution(SpatialIdentityId id) {
		return id instanceof PersistentGeoId && geosById.containsKey(id)
				? null : registry.getResolution(id);
	}

	@Override
	public GeoElement getGeo(PersistentGeoId id) {
		GeoElement staged = geosById.get(id);
		return staged == null ? registry.getGeo(id) : staged;
	}

	@Override
	public PersistentGeoId getPersistentGeoId(GeoElement geo) {
		GeoIdentityRecord staged = recordsByGeo.get(geo);
		return staged == null ? registry.getPersistentGeoId(geo) : staged.getId();
	}

	SpatialRedefineContext getContext() {
		return context;
	}

	State getState() {
		return state;
	}

	void setState(State next) {
		state = next;
	}

	boolean areLabelsActivated() {
		return labelsActivated;
	}

	void markLabelsActivated() {
		labelsActivated = true;
	}

	IdentityHashMap<GeoElement, GeoIdentityRecord> copyRecordsByGeo() {
		return new IdentityHashMap<>(recordsByGeo);
	}

	GeoIdentityRecord getStagedRecord(GeoElement geo) {
		return recordsByGeo.get(geo);
	}

	Map<PersistentGeoId, GeoElement> copyGeosById() {
		return new LinkedHashMap<>(geosById);
	}

	boolean wasPresentAtEntry(GeoElement geo) {
		return entryGeos.contains(geo);
	}

	void put(GeoElement geo, GeoIdentityRecord record, boolean auxiliary) {
		recordsByGeo.put(geo, record);
		geosById.put(record.getId(), geo);
		ConstructionElement element = geo.isIndependent() ? geo
				: geo.getParentAlgorithm();
		promotions.put(geo, new PromotionState(geo,
				entryElements.contains(element), auxiliary, geo.isLabelSet()));
	}

	void rollbackPromotions() {
		rollbackPromotionStates(new ArrayList<>(promotions.values()));
		labelsActivated = false;
	}

	void rollbackUninstalledRetainedPromotions(Set<GeoElement> installed) {
		Objects.requireNonNull(installed);
		if (state != State.CLAIMED) {
			throw new IllegalStateException(
					"Only a claimed redefine can discard uninstalled candidates");
		}
		ArrayList<PromotionState> uninstalled = new ArrayList<>();
		for (Map.Entry<GeoElement, PromotionState> promotion
				: promotions.entrySet()) {
			if (!installed.contains(promotion.getKey())) {
				uninstalled.add(promotion.getValue());
			}
		}
		rollbackPromotionStates(uninstalled);
	}

	private void rollbackPromotionStates(ArrayList<PromotionState> ordered) {
		Collections.sort(ordered, new Comparator<PromotionState>() {
			@Override
			public int compare(PromotionState first, PromotionState second) {
				int firstIndex = first.constructionElement == null ? -1
						: first.constructionElement.getConstructionIndex();
				int secondIndex = second.constructionElement == null ? -1
						: second.constructionElement.getConstructionIndex();
				return Integer.compare(secondIndex, firstIndex);
			}
		});
		Set<ConstructionElement> removed = Collections.newSetFromMap(
				new IdentityHashMap<ConstructionElement, Boolean>());
		for (int index = 0; index < ordered.size(); index++) {
			ordered.get(index).rollback(labelsActivated, removed);
		}
	}

	private static final class PromotionState {
		private final GeoElement geo;
		private final ConstructionElement constructionElement;
		private final boolean alreadyInConstruction;
		private final boolean auxiliary;
		private final boolean labelWasSet;

		private PromotionState(GeoElement geo, boolean alreadyInConstruction,
				boolean auxiliary, boolean labelWasSet) {
			this.geo = geo;
			constructionElement = geo.isIndependent() ? geo
					: geo.getParentAlgorithm();
			this.alreadyInConstruction = alreadyInConstruction;
			this.auxiliary = auxiliary;
			this.labelWasSet = labelWasSet;
		}

		private void rollback(boolean labelWasActivated,
				Set<ConstructionElement> removed) {
			if (labelWasActivated && !labelWasSet) {
				String currentLabel = geo.getLabelSimple();
				if (currentLabel != null
						&& geo.getConstruction().lookupLabel(currentLabel) == geo) {
					geo.getConstruction().removeLabel(geo);
				}
				geo.setLabelSimple(null);
				geo.setLabelSet(false);
				geo.setLabelWanted(false);
				geo.setAuxiliaryObject(auxiliary);
			}
			if (!alreadyInConstruction && constructionElement != null
					&& removed.add(constructionElement)
					&& geo.getConstruction().isInConstructionList(geo)) {
				geo.getConstruction().removeFromConstructionList(constructionElement);
			}
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geogebra.common.kernel.geos.GeoElement;

/** Immutable, complete identity/attachment/resolution graph prepared off-line. */
public final class SpatialLifecycleProspectiveGraph implements SpatialIdentityGraph {
	private final SpatialLifecycleOperationKind operationKind;
	private final String provenanceToken;
	private final Map<SpatialIdentityId, SpatialIdentityRecord> records;
	private final IdentityHashMap<GeoElement, PersistentGeoId> idsByGeo;
	private final Map<PersistentGeoId, GeoElement> geosById;
	private final Map<SpatialIdentityId, SpatialRecordResolution> resolutions;
	private final Set<SpatialIdentityId> changedRecordIds;
	private final Set<SpatialIdentityId> changedResolutionIds;
	private final Set<SpatialIdentityId> affectedIds;
	private final Set<SpatialIdentityId> declaredExternalReferenceIds;

	SpatialLifecycleProspectiveGraph(SpatialLifecycleOperationKind operationKind,
			String provenanceToken,
			Map<SpatialIdentityId, SpatialIdentityRecord> records,
			IdentityHashMap<GeoElement, PersistentGeoId> idsByGeo,
			Map<PersistentGeoId, GeoElement> geosById,
			Map<SpatialIdentityId, SpatialRecordResolution> resolutions,
			Collection<SpatialIdentityId> changedRecordIds,
			Collection<SpatialIdentityId> changedResolutionIds,
			Collection<SpatialIdentityId> declaredExternalReferenceIds) {
		this.operationKind = operationKind;
		this.provenanceToken = provenanceToken;
		this.records = Collections.unmodifiableMap(new LinkedHashMap<>(records));
		this.idsByGeo = new IdentityHashMap<>(idsByGeo);
		this.geosById = Collections.unmodifiableMap(new LinkedHashMap<>(geosById));
		this.resolutions = Collections.unmodifiableMap(new LinkedHashMap<>(resolutions));
		this.changedRecordIds = immutableIds(changedRecordIds);
		this.changedResolutionIds = immutableIds(changedResolutionIds);
		LinkedHashSet<SpatialIdentityId> affected = new LinkedHashSet<>(changedRecordIds);
		affected.addAll(changedResolutionIds);
		this.affectedIds = Collections.unmodifiableSet(affected);
		this.declaredExternalReferenceIds = immutableIds(declaredExternalReferenceIds);
	}

	public SpatialLifecycleOperationKind getOperationKind() {
		return operationKind;
	}

	public String getProvenanceToken() {
		return provenanceToken;
	}

	@Override
	public List<SpatialIdentityRecord> getRecords() {
		ArrayList<SpatialIdentityRecord> ordered = new ArrayList<>(records.values());
		Collections.sort(ordered, new Comparator<SpatialIdentityRecord>() {
			@Override
			public int compare(SpatialIdentityRecord first,
					SpatialIdentityRecord second) {
				return first.getId().compareTo(second.getId());
			}
		});
		return Collections.unmodifiableList(ordered);
	}

	@Override
	public SpatialIdentityRecord getRecord(SpatialIdentityId id) {
		return records.get(id);
	}

	@Override
	public SpatialRecordResolution getResolution(SpatialIdentityId id) {
		return resolutions.get(id);
	}

	@Override
	public GeoElement getGeo(PersistentGeoId id) {
		return geosById.get(id);
	}

	@Override
	public PersistentGeoId getPersistentGeoId(GeoElement geo) {
		return idsByGeo.get(geo);
	}

	/** @return immutable exact-identity attachment snapshot */
	public Map<GeoElement, PersistentGeoId> getAttachments() {
		return Collections.unmodifiableMap(new IdentityHashMap<>(idsByGeo));
	}

	public Set<SpatialIdentityId> getChangedRecordIds() {
		return changedRecordIds;
	}

	public Set<SpatialIdentityId> getChangedResolutionIds() {
		return changedResolutionIds;
	}

	public Set<SpatialIdentityId> getAffectedIds() {
		return affectedIds;
	}

	public Set<SpatialIdentityId> getDeclaredExternalReferenceIds() {
		return declaredExternalReferenceIds;
	}

	Map<SpatialIdentityId, SpatialIdentityRecord> copyRecordMap() {
		return new LinkedHashMap<>(records);
	}

	IdentityHashMap<GeoElement, PersistentGeoId> copyIdsByGeo() {
		return new IdentityHashMap<>(idsByGeo);
	}

	Map<PersistentGeoId, GeoElement> copyGeosById() {
		return new LinkedHashMap<>(geosById);
	}

	Map<SpatialIdentityId, SpatialRecordResolution> copyResolutionMap() {
		return new LinkedHashMap<>(resolutions);
	}

	private static Set<SpatialIdentityId> immutableIds(
			Collection<SpatialIdentityId> source) {
		ArrayList<SpatialIdentityId> ordered = new ArrayList<>(source);
		Collections.sort(ordered);
		return Collections.unmodifiableSet(new LinkedHashSet<>(ordered));
	}
}

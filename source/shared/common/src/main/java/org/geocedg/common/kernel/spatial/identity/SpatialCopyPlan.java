/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.kernel.geos.GeoElement;

/** Explicit source/destination and dependency policy for one spatial copy. */
public final class SpatialCopyPlan {
	private final SpatialCopyPolicy policy;
	private final SpatialIdentityRegistry sourceRegistry;
	private final SpatialIdentityRegistry destinationRegistry;
	private final IdentityHashMap<GeoElement, GeoElement> geoCopies;
	private final Set<SpatialIdentityId> ownedRecordIds;
	private final Set<SpatialIdentityId> externalContextRootIds;
	private final String provenanceToken;

	private SpatialCopyPlan(SpatialCopyPolicy policy,
			SpatialIdentityRegistry sourceRegistry,
			SpatialIdentityRegistry destinationRegistry,
			Map<GeoElement, GeoElement> geoCopies,
			Collection<? extends SpatialIdentityId> ownedRecordIds,
			Collection<? extends SpatialIdentityId> externalContextRootIds,
			String provenanceToken) {
		this.policy = Objects.requireNonNull(policy);
		this.sourceRegistry = Objects.requireNonNull(sourceRegistry);
		this.destinationRegistry = Objects.requireNonNull(destinationRegistry);
		this.geoCopies = identityCopy(geoCopies);
		this.ownedRecordIds = immutableIds(ownedRecordIds);
		this.externalContextRootIds = immutableIds(externalContextRootIds);
		this.provenanceToken = SpatialRecordSupport.requireText(provenanceToken,
				"provenanceToken");
		if (this.geoCopies.isEmpty()) {
			throw new IllegalArgumentException("A spatial copy requires explicit geo copies");
		}
		for (Map.Entry<GeoElement, GeoElement> entry : this.geoCopies.entrySet()) {
			if (entry.getKey() == entry.getValue()) {
				throw new IllegalArgumentException(
						"A spatial copy cannot attach a source geo as its own copy");
			}
		}
	}

	/**
	 * Default whole-connected-closure copy, including cross-document import.
	 *
	 * @return validated complete-closure copy plan
	 */
	public static SpatialCopyPlan completeClosure(
			SpatialIdentityRegistry sourceRegistry,
			SpatialIdentityRegistry destinationRegistry,
			Map<GeoElement, GeoElement> geoCopies, String provenanceToken) {
		return new SpatialCopyPlan(SpatialCopyPolicy.COMPLETE_CLOSURE,
				sourceRegistry, destinationRegistry, geoCopies,
				Collections.<SpatialIdentityId>emptySet(),
				Collections.<SpatialIdentityId>emptySet(), provenanceToken);
	}

	/**
	 * Same-construction owned-subgraph copy with an exact external allow-list.
	 *
	 * @return validated same-construction copy plan
	 */
	public static SpatialCopyPlan declaredSameConstructionExternal(
			SpatialIdentityRegistry registry,
			Collection<? extends SpatialIdentityId> ownedRecordIds,
			Map<GeoElement, GeoElement> geoCopies,
			Collection<? extends ProjectionSystemId> externalSystemRoots,
			Collection<? extends ProjectionDiagramMapId> externalMapRoots,
			String provenanceToken) {
		LinkedHashSet<SpatialIdentityId> roots = new LinkedHashSet<>();
		roots.addAll(Objects.requireNonNull(externalSystemRoots));
		roots.addAll(Objects.requireNonNull(externalMapRoots));
		return new SpatialCopyPlan(
				SpatialCopyPolicy.DECLARED_SAME_CONSTRUCTION_EXTERNAL,
				registry, registry, geoCopies, ownedRecordIds, roots,
				provenanceToken);
	}

	public SpatialCopyPolicy getPolicy() {
		return policy;
	}

	public SpatialIdentityRegistry getSourceRegistry() {
		return sourceRegistry;
	}

	public SpatialIdentityRegistry getDestinationRegistry() {
		return destinationRegistry;
	}

	public Map<GeoElement, GeoElement> getGeoCopies() {
		return Collections.unmodifiableMap(new IdentityHashMap<>(geoCopies));
	}

	public Set<SpatialIdentityId> getOwnedRecordIds() {
		return ownedRecordIds;
	}

	public Set<SpatialIdentityId> getExternalContextRootIds() {
		return externalContextRootIds;
	}

	public String getProvenanceToken() {
		return provenanceToken;
	}

	IdentityHashMap<GeoElement, GeoElement> copyGeoMap() {
		return new IdentityHashMap<>(geoCopies);
	}

	private static IdentityHashMap<GeoElement, GeoElement> identityCopy(
			Map<GeoElement, GeoElement> source) {
		IdentityHashMap<GeoElement, GeoElement> copy = new IdentityHashMap<>();
		for (Map.Entry<GeoElement, GeoElement> entry
				: Objects.requireNonNull(source).entrySet()) {
			copy.put(Objects.requireNonNull(entry.getKey()),
					Objects.requireNonNull(entry.getValue()));
		}
		return copy;
	}

	private static Set<SpatialIdentityId> immutableIds(
			Collection<? extends SpatialIdentityId> source) {
		LinkedHashSet<SpatialIdentityId> copy = new LinkedHashSet<>();
		for (SpatialIdentityId id : Objects.requireNonNull(source)) {
			copy.add(Objects.requireNonNull(id));
		}
		return Collections.unmodifiableSet(copy);
	}
}

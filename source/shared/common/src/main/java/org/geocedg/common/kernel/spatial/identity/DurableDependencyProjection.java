/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Versioned durable-dependency projection of one construction-defined
 * participant (PRE-G9B-R3, ADR 0029). The persisted {@code schemaVersion} of a
 * construction identity record names the rule that governs its dependencies:
 * version 1 keeps the historical direct projection and version 2 the transitive
 * durable frontier. Publication, redefine assessment, validation and persistence
 * all use this single implementation.
 *
 * <p>The projection reads only the construction DAG and the durable identity of
 * each visited geo. Coordinates, proximity, labels, construction order, XML
 * position and render state are never evidence.</p>
 */
public enum DurableDependencyProjection {
	/** Durable identities among the direct parent inputs; helpers are not traversed. */
	DIRECT(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT),
	/** Through every identity-free input to its nearest durable ancestors. */
	TRANSITIVE_DURABLE_FRONTIER(
			ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER);

	private final int schemaVersion;

	DurableDependencyProjection(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	/** @return construction identity schema version governed by this rule */
	public int getSchemaVersion() {
		return schemaVersion;
	}

	/** @return whether this build understands the construction schema version */
	public static boolean isSupportedSchemaVersion(int schemaVersion) {
		for (DurableDependencyProjection projection : values()) {
			if (projection.schemaVersion == schemaVersion) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the rule that governs a record of the given schema version
	 * @throws IllegalArgumentException for a version this build does not know; a
	 *         newer record is never interpreted under an older rule
	 */
	public static DurableDependencyProjection forSchemaVersion(int schemaVersion) {
		for (DurableDependencyProjection projection : values()) {
			if (projection.schemaVersion == schemaVersion) {
				return projection;
			}
		}
		throw new IllegalArgumentException(
				"Unsupported construction identity schema version " + schemaVersion);
	}

	/**
	 * Projects the durable dependencies of one participant.
	 *
	 * @param geo participant whose parent algorithm is projected
	 * @param identity durable identity of a geo, or {@code null} when it has none
	 * @return sorted distinct durable dependency identities
	 */
	public List<PersistentGeoId> project(GeoElement geo,
			Function<GeoElement, PersistentGeoId> identity) {
		LinkedHashSet<PersistentGeoId> ids = new LinkedHashSet<>();
		walk(geo, identity, ids, null);
		ArrayList<PersistentGeoId> sorted = new ArrayList<>(ids);
		Collections.sort(sorted);
		return Collections.unmodifiableList(sorted);
	}

	/**
	 * @param geo participant whose parent algorithm is projected
	 * @param identity durable identity of a geo, or {@code null} when it has none
	 * @return identity-free geos crossed by the projection, empty for
	 *         {@link #DIRECT}
	 */
	public List<GeoElement> traversedHelpers(GeoElement geo,
			Function<GeoElement, PersistentGeoId> identity) {
		ArrayList<GeoElement> helpers = new ArrayList<>();
		walk(geo, identity, new LinkedHashSet<>(), helpers);
		return Collections.unmodifiableList(helpers);
	}

	private void walk(GeoElement geo, Function<GeoElement, PersistentGeoId> identity,
			LinkedHashSet<PersistentGeoId> ids, List<GeoElement> helpers) {
		Objects.requireNonNull(geo);
		Objects.requireNonNull(identity);
		IdentityHashMap<GeoElement, Boolean> visited = new IdentityHashMap<>();
		visited.put(geo, Boolean.TRUE);
		ArrayDeque<GeoElement> pending = new ArrayDeque<>();
		pushInputs(geo, pending);
		while (!pending.isEmpty()) {
			GeoElement current = pending.pop();
			if (visited.put(current, Boolean.TRUE) != null) {
				continue;
			}
			PersistentGeoId id = identity.apply(current);
			if (id != null) {
				ids.add(id);
			} else if (this == TRANSITIVE_DURABLE_FRONTIER) {
				if (helpers != null) {
					helpers.add(current);
				}
				pushInputs(current, pending);
			}
		}
	}

	private static void pushInputs(GeoElement geo, ArrayDeque<GeoElement> pending) {
		List<GeoElement> inputs = ConstructionGeoRedefineProvider.durableDependencyGeos(geo);
		for (int index = inputs.size() - 1; index >= 0; index--) {
			GeoElement input = inputs.get(index);
			if (input != null) {
				pending.push(input);
			}
		}
	}
}

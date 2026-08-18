/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/** Immutable stable-role map for a complete old or candidate output group. */
public final class SpatialRedefineOutputGroup<T extends SpatialRedefineOutput> {
	private final Map<String, T> outputsByRole;

	private SpatialRedefineOutputGroup(Collection<? extends T> outputs) {
		Objects.requireNonNull(outputs);
		if (outputs.isEmpty()) {
			throw new IllegalArgumentException("A redefine output group cannot be empty");
		}
		TreeMap<String, T> byRole = new TreeMap<>();
		for (T output : outputs) {
			T present = Objects.requireNonNull(output);
			String role = SpatialRecordSupport.requireText(
					present.getStableOutputRole(), "stableOutputRole");
			if (byRole.put(role, present) != null) {
				throw new IllegalArgumentException(
						"Duplicate stable output role: " + role);
			}
		}
		outputsByRole = Collections.unmodifiableMap(byRole);
	}

	/**
	 * Creates a role-sorted group and rejects duplicate or empty roles.
	 *
	 * @return immutable output group
	 */
	public static <T extends SpatialRedefineOutput>
			SpatialRedefineOutputGroup<T> of(Collection<? extends T> outputs) {
		return new SpatialRedefineOutputGroup<>(outputs);
	}

	/**
	 * Creates the single-role compatibility form.
	 *
	 * @return immutable single-output group
	 */
	public static <T extends SpatialRedefineOutput>
			SpatialRedefineOutputGroup<T> singleton(T output) {
		return new SpatialRedefineOutputGroup<>(
				Collections.singletonList(Objects.requireNonNull(output)));
	}

	/** @return output for the supplied stable role, or {@code null} when absent */
	public T get(String stableOutputRole) {
		return outputsByRole.get(stableOutputRole);
	}

	/** @return whether the supplied stable role is present */
	public boolean containsRole(String stableOutputRole) {
		return outputsByRole.containsKey(stableOutputRole);
	}

	/**
	 * Resolves a provider-owned role from the exact enumerated candidate handle.
	 * Reference equality is enumeration evidence only; it never supplies identity
	 * or continuity authority.
	 *
	 * @return unique stable role, or {@code null} when the geo is absent
	 */
	public String getRoleForGeo(org.geogebra.common.kernel.geos.GeoElement geo) {
		String result = null;
		for (Map.Entry<String, T> entry : outputsByRole.entrySet()) {
			if (entry.getValue().getGeo() == geo) {
				if (result != null) {
					return null;
				}
				result = entry.getKey();
			}
		}
		return result;
	}

	/** @return number of stable-role outputs */
	public int size() {
		return outputsByRole.size();
	}

	/** @return roles in deterministic lexical order */
	public Set<String> getRoles() {
		return outputsByRole.keySet();
	}

	/** @return outputs in deterministic stable-role order */
	public List<T> getOutputs() {
		return Collections.unmodifiableList(
				new ArrayList<>(outputsByRole.values()));
	}

	/** @return immutable stable-role map */
	public Map<String, T> asMap() {
		return outputsByRole;
	}

	/**
	 * @return whether every signature declares the supplied complete cardinality
	 */
	public boolean declaresCardinality(int cardinality) {
		for (T output : outputsByRole.values()) {
			if (output.getSignature().getOutputCardinality() != cardinality) {
				return false;
			}
		}
		return true;
	}
}

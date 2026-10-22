/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;

/**
 * Resolution of a retained component selector against a certified structure
 * ({@code locus-v2-existence-components.md} section 15.6; certification design section 7,
 * decision E-3 revised). A selector resolves only through a demonstrated structural
 * correspondence: the same lineage key, equality of the decomposition (the cited component
 * was the whole declared domain and the structure is one closed periodic component or, on a
 * bounded domain, one component equal to the domain), or a recorded split or merge from the
 * cited component. A change of dimension resolves no selector, since a point component is
 * never selected by a parameter (decision D-CU2-T13-R1). Unique containment of the parameter
 * then validates the correspondence; it never creates one. Resolution never rewrites the
 * retained selector.
 */
public final class LocusComponentSelectorResolver2D {
	/** Outcome of a resolution. */
	public enum Status {
		/** A component of the structure has the retained lineage key. */
		RESOLVED_BY_KEY,
		/** The retained key cites the whole domain and the structure is that closed curve. */
		RESOLVED_BY_DECOMPOSITION_EQUALITY,
		/** A recorded lineage transition leads from the retained key to the component. */
		RESOLVED_BY_LINEAGE_TRANSITION,
		/** No demonstrated correspondence: the selector stays unresolved. */
		NO_CORRESPONDENCE,
		/** Several corresponding components contain the parameter. */
		AMBIGUOUS,
		/** No corresponding component contains the parameter. */
		NOT_CONTAINED;

		/** @return whether the selector resolves to one component */
		public boolean isResolved() {
			return this == RESOLVED_BY_KEY || this == RESOLVED_BY_DECOMPOSITION_EQUALITY
					|| this == RESOLVED_BY_LINEAGE_TRANSITION;
		}
	}

	/** Resolved component with its lineage key, or the typed cause of a failure. */
	public static final class Resolution {
		private final Status status;
		private final ContinuousComponent component;
		private final String lineageKey;

		private Resolution(Status status, ContinuousComponent component, String lineageKey) {
			this.status = status;
			this.component = component;
			this.lineageKey = lineageKey;
		}

		/** @return outcome of the resolution */
		public Status getStatus() {
			return status;
		}

		/** @return resolved component, or {@code null} */
		public ContinuousComponent getComponent() {
			return component;
		}

		/** @return current lineage key of the resolved component, or {@code null} */
		public String getLineageKey() {
			return lineageKey;
		}
	}

	private LocusComponentSelectorResolver2D() {
		// Utility class.
	}

	/**
	 * @param retainedKey lineage key the selector retains
	 * @param structure current certified structure of the branch
	 * @param transitions lineage transitions recorded into this structure
	 * @param parameter canonical parameter of the selected position
	 * @return resolution with its typed outcome
	 */
	public static Resolution resolve(String retainedKey, LocusExistenceStructure2D structure,
			List<LocusLineage2D> transitions, double parameter) {
		return resolve(retainedKey, structure, transitions, parameter, 0);
	}

	/**
	 * Resolves with the provider tolerance of the domain in the validating containment
	 * ({@link ContinuousComponent#contains(double, double)}), for consumers that read it.
	 *
	 * @param retainedKey lineage key the selector retains
	 * @param structure current certified structure of the branch
	 * @param transitions lineage transitions recorded into this structure
	 * @param parameter canonical parameter of the selected position
	 * @param domainEpsilon provider-owned domain tolerance
	 * @return resolution with its typed outcome
	 */
	public static Resolution resolve(String retainedKey, LocusExistenceStructure2D structure,
			List<LocusLineage2D> transitions, double parameter, double domainEpsilon) {
		Objects.requireNonNull(retainedKey);
		List<ContinuousComponent> same = new ArrayList<>();
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			if (retainedKey.equals(LocusComponentLineage2D.of(structure, component))) {
				same.add(component);
			}
		}
		if (!same.isEmpty()) {
			return validate(Status.RESOLVED_BY_KEY, same, structure, parameter,
					domainEpsilon);
		}
		if (isWholeDomain(structure) && retainedKey.equals(LocusComponentLineage2D
				.create(structure.getBranchKey(), structure.getCanonicalDomain()))) {
			return validate(Status.RESOLVED_BY_DECOMPOSITION_EQUALITY,
					structure.getContinuousValidComponents(), structure, parameter,
					domainEpsilon);
		}
		for (LocusLineage2D transition : transitions) {
			boolean related = transition.getTransition() == LineageTransition.SPLIT
					|| transition.getTransition() == LineageTransition.MERGED;
			if (!related || !transition.getParentKeys().contains(retainedKey)) {
				continue;
			}
			List<ContinuousComponent> children = new ArrayList<>();
			for (ContinuousComponent component : structure.getContinuousValidComponents()) {
				if (transition.getChildKeys().contains(
						LocusComponentLineage2D.of(structure, component))) {
					children.add(component);
				}
			}
			if (!children.isEmpty()) {
				return validate(Status.RESOLVED_BY_LINEAGE_TRANSITION, children, structure,
						parameter, domainEpsilon);
			}
		}
		return new Resolution(Status.NO_CORRESPONDENCE, null, null);
	}

	/**
	 * Equality of the decomposition (decision E-3) for a consumer that holds no parameter to
	 * validate it: the retained key cites the whole declared domain and the structure is
	 * that whole domain, whose one component contains every parameter.
	 *
	 * @param retainedKey lineage key the selector retains
	 * @param structure current certified structure of the branch
	 * @return whether the key corresponds to the one whole-domain component
	 */
	public static boolean citesWholeDomain(String retainedKey,
			LocusExistenceStructure2D structure) {
		return isWholeDomain(structure) && retainedKey.equals(LocusComponentLineage2D
				.create(structure.getBranchKey(), structure.getCanonicalDomain()));
	}

	/** Equality of the decomposition with a selector that cites the whole declared domain. */
	private static boolean isWholeDomain(LocusExistenceStructure2D structure) {
		return LocusComponentLineage2D.isWholeDomain(structure);
	}

	/** Unique containment of the parameter validates a demonstrated correspondence. */
	private static Resolution validate(Status status, List<ContinuousComponent> candidates,
			LocusExistenceStructure2D structure, double parameter, double domainEpsilon) {
		ContinuousComponent found = null;
		for (ContinuousComponent candidate : candidates) {
			if (candidate.contains(parameter, domainEpsilon)) {
				if (found != null) {
					return new Resolution(Status.AMBIGUOUS, null, null);
				}
				found = candidate;
			}
		}
		if (found == null) {
			return new Resolution(Status.NOT_CONTAINED, null, null);
		}
		return new Resolution(status, found, LocusComponentLineage2D.of(structure, found));
	}
}

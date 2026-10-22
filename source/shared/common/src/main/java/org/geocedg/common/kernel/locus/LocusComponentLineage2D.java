/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;

/** Canonical durable lineage key for one semantic branch component. */
public final class LocusComponentLineage2D {
	/** Key suffix of the closed periodic component of a branch, which has no boundary. */
	private static final String CLOSURE = "/component/periodic-closure";

	private LocusComponentLineage2D() {
		// Utility class.
	}

	/**
	 * Builds a revision-independent key from approved semantic component data.
	 *
	 * @return canonical component-lineage key
	 */
	public static String create(String branchKey, LocusInterval2D component) {
		String branch = Objects.requireNonNull(branchKey);
		if (branch.trim().isEmpty() || !branch.equals(branch.trim())) {
			throw new IllegalArgumentException("Branch key must be canonical");
		}
		LocusInterval2D interval = Objects.requireNonNull(component);
		return branch + "/component/"
				+ Double.toHexString(interval.getLower()) + "/"
				+ Double.toHexString(interval.getUpper()) + "/"
				+ (interval.isLowerClosed() ? "closed" : "open") + "/"
				+ (interval.isUpperClosed() ? "closed" : "open");
	}

	/**
	 * Structural lineage key of a certified component ({@code
	 * locus-v2-existence-components.md} section 15.6): the branch key and the ordered sources
	 * of its two ends, independent of the binary64 position of its boundaries, so that moving
	 * a boundary without changing its source keeps the key. The closed periodic component of
	 * a branch has no boundary and one key. A component with an untyped end, recorded before
	 * PRE-G9B-CU2, with the edge of an unresolved band, which is no structural source, or with
	 * an end or closure that no proof certifies, such as those of the provider-compatibility
	 * certificate, keeps the key of {@link #create(String, LocusInterval2D)}; so does a
	 * component whose two sources another component of the same structure shares, since
	 * nothing structural tells them apart.
	 *
	 * @param structure certified structure of the branch
	 * @param component one of its continuous components
	 * @return lineage key of the component in this structure
	 */
	public static String of(LocusExistenceStructure2D structure,
			ContinuousComponent component) {
		String structural = structural(structure.getBranchKey(), component);
		if (structural != null) {
			for (ContinuousComponent other : structure.getContinuousValidComponents()) {
				if (other != component
						&& structural.equals(structural(structure.getBranchKey(), other))) {
					structural = null;
					break;
				}
			}
		}
		return structural != null ? structural
				: create(structure.getBranchKey(), component.getInterval());
	}

	/**
	 * Key under which the intersection roots of a component are identified (decision E-3,
	 * applied to persisted root tokens; PRE-G9B-CU2 step T11). When the decomposition is the
	 * whole declared domain, the key of that domain, which the root tokens persisted before
	 * PRE-G9B-CU2 cite, corresponds to the component by equality of the decomposition, and it
	 * stays the identity key; any other component is identified by its lineage key.
	 *
	 * @param structure certified structure of the branch
	 * @param component one of its continuous components
	 * @return identity key of the roots on the component
	 */
	public static String identityKey(LocusExistenceStructure2D structure,
			ContinuousComponent component) {
		return isWholeDomain(structure)
				? create(structure.getBranchKey(), structure.getCanonicalDomain())
				: of(structure, component);
	}

	/**
	 * Equality of the decomposition with the whole declared domain (decision E-3): the
	 * structure is one closed periodic component, or, on a bounded domain, one interval
	 * component equal to the domain; an open arc of full length is not the closed curve that
	 * a key of the whole domain cites.
	 *
	 * @param structure certified structure of the branch
	 * @return whether its decomposition is the whole declared domain
	 */
	public static boolean isWholeDomain(LocusExistenceStructure2D structure) {
		if (structure.isClosedPeriodic()) {
			return true;
		}
		List<ContinuousComponent> components = structure.getContinuousValidComponents();
		return structure.getTopology() == LocusExistenceStructure2D.DomainTopology.BOUNDED
				&& components.size() == 1 && structure.getPointComponents().isEmpty()
				&& !components.get(0).isArc()
				&& components.get(0).getInterval().equals(structure.getCanonicalDomain());
	}

	/**
	 * Lineage key of a point component: the branch and the source of its root. A point
	 * whose source another point of the same structure shares, such as one of several
	 * declared isolated parameters, keeps the key of its enclosure instead, as a component
	 * does in {@link #of(LocusExistenceStructure2D, ContinuousComponent)}, since nothing
	 * structural tells them apart.
	 *
	 * @return lineage key of the point component
	 */
	public static String of(LocusExistenceStructure2D structure,
			LocusPointComponent2D point) {
		String signature = point.getSource().getSignature();
		for (LocusPointComponent2D other : structure.getPointComponents()) {
			if (other != point && signature.equals(other.getSource().getSignature())) {
				return structure.getBranchKey() + "/point/"
						+ Double.toHexString(point.getParameterLower()) + "/"
						+ Double.toHexString(point.getParameterUpper());
			}
		}
		return structure.getBranchKey() + "/point/" + signature;
	}

	/** @return lineage keys of every component of the structure, in canonical order */
	public static List<String> keys(LocusExistenceStructure2D structure) {
		List<String> keys = new ArrayList<>();
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			keys.add(of(structure, component));
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			keys.add(of(structure, point));
		}
		return keys;
	}

	/** @return structural key, or {@code null} when an end has no structural source */
	private static String structural(String branchKey, ContinuousComponent component) {
		if (component.isClosedPeriodic()) {
			return component.getLowerBoundary().getNature().certifies()
					? branchKey + CLOSURE : null;
		}
		LocusBoundary2D lower = component.getLowerBoundary();
		LocusBoundary2D upper = component.getUpperBoundary();
		if (!isStructural(lower) || !isStructural(upper)) {
			return null;
		}
		return branchKey + "/component/" + lower.getSource().getSignature() + ";"
				+ upper.getSource().getSignature();
	}

	private static boolean isStructural(LocusBoundary2D boundary) {
		switch (boundary.getKind()) {
		case DOMAIN_END:
		case CERTIFIED_TRANSITION:
		case STRUCTURAL_EVENT:
			return boundary.getNature().certifies();
		default:
			return false;
		}
	}

	/**
	 * Typed lineage transitions between two certified structures of one branch, derived from
	 * their lineage keys alone ({@code locus-v2-existence-components.md} section 15.6;
	 * certification design section 7). Components whose keys persist are unchanged. When the
	 * previous structure was the closed periodic component, every new component lies in it:
	 * a split, possibly into one arc. When the new structure is closed periodic, every
	 * previous component lies in it: a merge. Any other change has no demonstrated relation
	 * and is recorded as disappearance and appearance. A change of the selection rule never
	 * relates components. No label, index, coordinate or proximity is read.
	 *
	 * @param previous structure of the previous certificate, or {@code null} before the first
	 * @param current structure of the current certificate
	 * @param sameSelection whether the selection rule of the construction is unchanged
	 * @return transitions; one unchanged record when no key changes
	 */
	public static List<LocusLineage2D> transitions(LocusExistenceStructure2D previous,
			LocusExistenceStructure2D current, boolean sameSelection) {
		Set<String> before = previous == null ? Set.of()
				: new LinkedHashSet<>(keys(previous));
		Set<String> after = new LinkedHashSet<>(keys(current));
		List<String> parents = new ArrayList<>();
		for (String key : before) {
			if (!after.contains(key)) {
				parents.add(key);
			}
		}
		List<String> children = new ArrayList<>();
		for (String key : after) {
			if (!before.contains(key)) {
				children.add(key);
			}
		}
		List<LocusLineage2D> result = new ArrayList<>();
		if (parents.isEmpty() && children.isEmpty()) {
			result.add(LocusLineage2D.unchanged());
		} else if (parents.isEmpty()) {
			result.add(new LocusLineage2D(LineageTransition.APPEARED, List.of(), children));
		} else if (children.isEmpty()) {
			result.add(new LocusLineage2D(LineageTransition.DISAPPEARED, parents, List.of()));
		} else if (sameSelection && previous.isClosedPeriodic()) {
			result.add(new LocusLineage2D(LineageTransition.SPLIT, parents, children));
		} else if (sameSelection && current.isClosedPeriodic()) {
			result.add(new LocusLineage2D(LineageTransition.MERGED, parents, children));
		} else {
			result.add(new LocusLineage2D(LineageTransition.DISAPPEARED, parents, List.of()));
			result.add(new LocusLineage2D(LineageTransition.APPEARED, List.of(), children));
		}
		return result;
	}
}

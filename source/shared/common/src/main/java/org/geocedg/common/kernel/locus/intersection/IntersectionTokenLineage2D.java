/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Explicit semantic material used to mint one intersection token.
 *
 * <p>No coordinate, parameter value, candidate order or sample index is
 * admitted here.</p>
 */
public final class IntersectionTokenLineage2D {
	private final String solutionLineageKey;
	private final String establishedBranchLineage;
	private final Optional<String> continuationKey;

	/** Creates immutable token material from established semantic lineage. */
	public IntersectionTokenLineage2D(String solutionLineageKey,
			String establishedBranchLineage,
			Optional<String> continuationKey) {
		this.solutionLineageKey = requireText(solutionLineageKey,
				"Solution lineage key");
		this.establishedBranchLineage = requireText(establishedBranchLineage,
				"Established branch lineage");
		this.continuationKey = Objects.requireNonNull(continuationKey);
		if (continuationKey.isPresent()
				&& continuationKey.get().trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Continuation key cannot be blank");
		}
	}

	/**
	 * Creates public token material for one exact semantic component. Revision
	 * component ordinals, candidates, coordinates and parameters are absent.
	 *
	 * @return stable single-component solution lineage
	 */
	public static IntersectionTokenLineage2D forSingleComponent(
			String branchKey, LocusInterval2D interval,
			Optional<String> continuationKey) {
		String component = stableComponentLineage(branchKey, interval);
		return new IntersectionTokenLineage2D(
				solutionLineage(component, continuationKey), component,
				continuationKey);
	}

	/**
	 * Creates source-order-independent public token material for one exact pair of
	 * semantic components. Frozen G8 revision keys remain outside this material.
	 *
	 * @return stable canonical component-pair solution lineage
	 */
	public static IntersectionTokenLineage2D forCanonicalComponentPair(
			String firstBranchKey, LocusInterval2D firstInterval,
			String secondBranchKey, LocusInterval2D secondInterval,
			Optional<String> continuationKey) {
		String pair = stableCanonicalComponentPairLineage(firstBranchKey,
				firstInterval, secondBranchKey, secondInterval);
		return new IntersectionTokenLineage2D(
				solutionLineage(pair, continuationKey), pair, continuationKey);
	}

	/** @return stable branch-plus-domain component lineage */
	public static String stableComponentLineage(String branchKey,
			LocusInterval2D interval) {
		return LocusComponentLineage2D.create(branchKey, interval);
	}

	/** @return canonical unordered pair of stable component lineages */
	public static String stableCanonicalComponentPairLineage(
			String firstBranchKey, LocusInterval2D firstInterval,
			String secondBranchKey, LocusInterval2D secondInterval) {
		String first = stableComponentLineage(firstBranchKey, firstInterval);
		String second = stableComponentLineage(secondBranchKey, secondInterval);
		return LocusPairIdentity2D.componentPair(firstBranchKey, first,
				secondBranchKey, second);
	}

	public String getSolutionLineageKey() {
		return solutionLineageKey;
	}

	public String getEstablishedBranchLineage() {
		return establishedBranchLineage;
	}

	public Optional<String> getContinuationKey() {
		return continuationKey;
	}

	private static String solutionLineage(String componentLineage,
			Optional<String> continuationKey) {
		Objects.requireNonNull(continuationKey);
		return componentLineage + "/solution/"
				+ continuationKey.orElse("unestablished-incarnation");
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}

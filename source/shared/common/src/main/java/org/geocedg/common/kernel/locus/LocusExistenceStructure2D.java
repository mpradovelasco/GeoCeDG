/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Immutable, revision-bound existence and continuous-valid-domain evidence for
 * one semantic locus branch.
 *
 * <p>Canonical order is traversal order only. Neither interval position nor
 * component ordinal is identity.</p>
 */
public final class LocusExistenceStructure2D {
	/** Whether the finite component inventory is globally established. */
	public enum Completeness {
		COMPLETE,
		NOT_ESTABLISHED
	}

	/** Interval-wide existence knowledge. */
	public enum CoverageState {
		CERTIFIED_EXISTS,
		CERTIFIED_DOES_NOT_EXIST,
		UNRESOLVED
	}

	/** Whether a certified component is known maximal. */
	public enum Maximality {
		ESTABLISHED,
		LOCAL_CERTIFIED_SUBINTERVAL
	}

	/** One interval-wide existence assertion and its reconstructible evidence. */
	public static final class Coverage {
		private final LocusInterval2D interval;
		private final CoverageState state;
		private final String evidence;

		/** Creates one immutable interval-wide coverage assertion. */
		public Coverage(LocusInterval2D interval, CoverageState state,
				String evidence) {
			this.interval = Objects.requireNonNull(interval);
			this.state = Objects.requireNonNull(state);
			this.evidence = requireText(evidence, "Coverage evidence");
		}

		/** @return certified parameter interval */
		public LocusInterval2D getInterval() {
			return interval;
		}

		/** @return interval-wide existence state */
		public CoverageState getState() {
			return state;
		}

		/** @return deterministic evidence description */
		public String getEvidence() {
			return evidence;
		}

		private String signature() {
			return interval + ":" + state + ":" + evidence;
		}
	}

	/** One revision-local continuous-valid component certificate. */
	public static final class ContinuousComponent {
		private final LocusInterval2D interval;
		private final String evidenceKey;
		private final String boundaryEvidence;
		private final Maximality maximality;
		private final String numericGuarantee;

		/** Creates one revision-local continuous-valid component certificate. */
		public ContinuousComponent(LocusInterval2D interval, String evidenceKey,
				String boundaryEvidence, Maximality maximality,
				String numericGuarantee) {
			this.interval = Objects.requireNonNull(interval);
			this.evidenceKey = requireText(evidenceKey, "Component evidence key");
			this.boundaryEvidence = requireText(boundaryEvidence,
					"Boundary evidence");
			this.maximality = Objects.requireNonNull(maximality);
			this.numericGuarantee = requireText(numericGuarantee,
					"Numeric guarantee");
		}

		/** @return certified oriented interval */
		public LocusInterval2D getInterval() {
			return interval;
		}

		/**
		 * @return revision-local evidence key; never a durable identity
		 */
		public String getEvidenceKey() {
			return evidenceKey;
		}

		/** @return boundary-certification evidence */
		public String getBoundaryEvidence() {
			return boundaryEvidence;
		}

		/** @return whether maximality is globally established */
		public Maximality getMaximality() {
			return maximality;
		}

		/** @return interval-proof numeric guarantee */
		public String getNumericGuarantee() {
			return numericGuarantee;
		}

		private String signature() {
			return interval + ":" + evidenceKey + ":" + boundaryEvidence + ":"
					+ maximality + ":" + numericGuarantee;
		}
	}

	private final String branchKey;
	private final LocusInterval2D canonicalDomain;
	private final long semanticRevision;
	private final List<Coverage> coverage;
	private final List<ContinuousComponent> components;
	private final Completeness completeness;
	private final String certifierContract;

	/** Creates validated immutable semantic evidence. */
	public LocusExistenceStructure2D(String branchKey,
			LocusInterval2D canonicalDomain, long semanticRevision,
			List<Coverage> coverage, List<ContinuousComponent> components,
			Completeness completeness, String certifierContract) {
		this.branchKey = requireText(branchKey, "Branch key");
		this.canonicalDomain = Objects.requireNonNull(canonicalDomain);
		if (semanticRevision < 0) {
			throw new IllegalArgumentException("Semantic revision cannot be negative");
		}
		this.semanticRevision = semanticRevision;
		this.coverage = immutableCoverage(coverage, canonicalDomain);
		this.components = immutableComponents(components, canonicalDomain);
		this.completeness = Objects.requireNonNull(completeness);
		this.certifierContract = requireText(certifierContract,
				"Certifier contract");
		validateConsistency();
	}

	/**
	 * @return complete compatibility evidence for the proven components
	 */
	public static LocusExistenceStructure2D completeCompatibility(String branchKey,
			LocusInterval2D canonicalDomain, List<LocusInterval2D> intervals) {
		ArrayList<LocusInterval2D> ordered = new ArrayList<>(intervals);
		ordered.sort(Comparator.comparingDouble(LocusInterval2D::getLower));
		ArrayList<Coverage> coverage = completeCoverage(canonicalDomain, ordered);
		ArrayList<ContinuousComponent> components = new ArrayList<>();
		int index = 0;
		for (LocusInterval2D interval : ordered) {
			String key = "provider-component/" + index++;
			components.add(new ContinuousComponent(interval, key,
					"provider-endpoints/v1", Maximality.ESTABLISHED,
					"provider-contract"));
		}
		return new LocusExistenceStructure2D(branchKey, canonicalDomain, 0,
				coverage, components, Completeness.COMPLETE,
				"locus-existence/provider-compatibility/v1");
	}

	/**
	 * @return unresolved evidence without a manufactured empty-domain assertion
	 */
	public static LocusExistenceStructure2D notEstablished(String branchKey,
			LocusInterval2D canonicalDomain, String evidence) {
		return new LocusExistenceStructure2D(branchKey, canonicalDomain, 0,
				List.of(new Coverage(canonicalDomain, CoverageState.UNRESOLVED,
						evidence)), Collections.emptyList(),
				Completeness.NOT_ESTABLISHED,
				"locus-existence/shared-certifier/v1");
	}

	/** @return stable semantic branch key */
	public String getBranchKey() {
		return branchKey;
	}

	/** @return canonical provider driver domain */
	public LocusInterval2D getCanonicalDomain() {
		return canonicalDomain;
	}

	/** @return source semantic revision to which this evidence is bound */
	public long getSemanticRevision() {
		return semanticRevision;
	}

	/** @return canonical ordered interval coverage assertions */
	public List<Coverage> getCoverage() {
		return coverage;
	}

	/** @return canonical ordered locally certified components */
	public List<ContinuousComponent> getContinuousValidComponents() {
		return components;
	}

	/** @return global component-inventory completeness */
	public Completeness getCompleteness() {
		return completeness;
	}

	/** @return reconstructible certifier contract identifier */
	public String getCertifierContract() {
		return certifierContract;
	}

	/**
	 * @return the same evidence bound to the published semantic revision
	 */
	public LocusExistenceStructure2D bindToRevision(long revision) {
		if (revision < 1) {
			throw new IllegalArgumentException("Published revision must be positive");
		}
		return semanticRevision == revision ? this
				: new LocusExistenceStructure2D(branchKey, canonicalDomain, revision,
						coverage, components, completeness, certifierContract);
	}

	/**
	 * @return content signature that deliberately excludes the publication revision
	 */
	public String getSemanticSignature() {
		StringBuilder signature = new StringBuilder(certifierContract)
				.append('|').append(branchKey).append('|').append(canonicalDomain)
				.append('|').append(completeness);
		for (Coverage item : coverage) {
			signature.append("|coverage=").append(item.signature());
		}
		for (ContinuousComponent component : components) {
			signature.append("|component=").append(component.signature());
		}
		return signature.toString();
	}

	private void validateConsistency() {
		for (ContinuousComponent component : components) {
			boolean covered = false;
			for (Coverage item : coverage) {
				if (item.state == CoverageState.CERTIFIED_EXISTS
						&& contains(item.interval, component.interval)) {
					covered = true;
					break;
				}
			}
			if (!covered) {
				throw new IllegalArgumentException(
						"Continuous component lacks existence coverage");
			}
		}
		if (completeness == Completeness.COMPLETE) {
			if (coverage.isEmpty() || !coversCanonicalDomain(coverage,
					canonicalDomain)) {
				throw new IllegalArgumentException(
						"Complete existence evidence must cover the canonical domain");
			}
			for (ContinuousComponent component : components) {
				if (component.maximality != Maximality.ESTABLISHED) {
					throw new IllegalArgumentException(
							"Complete components must have established maximality");
				}
			}
		}
	}

	private static boolean coversCanonicalDomain(List<Coverage> items,
			LocusInterval2D domain) {
		if (items.get(0).interval.getLower() != domain.getLower()
				|| items.get(items.size() - 1).interval.getUpper()
						!= domain.getUpper()) {
			return false;
		}
		for (int index = 1; index < items.size(); index++) {
			Coverage previous = items.get(index - 1);
			Coverage current = items.get(index);
			if (previous.interval.getUpper() != current.interval.getLower()
					|| !previous.interval.isUpperClosed()
							&& !current.interval.isLowerClosed()) {
				return false;
			}
		}
		return true;
	}

	private static boolean contains(LocusInterval2D outer,
			LocusInterval2D inner) {
		return outer.getLower() <= inner.getLower()
				&& outer.getUpper() >= inner.getUpper()
				&& (outer.getLower() < inner.getLower()
						|| outer.isLowerClosed() || !inner.isLowerClosed())
				&& (outer.getUpper() > inner.getUpper()
						|| outer.isUpperClosed() || !inner.isUpperClosed());
	}

	private static ArrayList<Coverage> completeCoverage(
			LocusInterval2D domain, List<LocusInterval2D> components) {
		ArrayList<Coverage> result = new ArrayList<>();
		double cursor = domain.getLower();
		boolean cursorOwned = domain.isLowerClosed();
		for (LocusInterval2D component : components) {
			addGap(result, cursor, component.getLower(), cursorOwned,
					!component.isLowerClosed());
			addExistence(result, component);
			cursor = component.getUpper();
			cursorOwned = !component.isUpperClosed();
		}
		addGap(result, cursor, domain.getUpper(), cursorOwned,
				domain.isUpperClosed());
		return result;
	}

	private static void addExistence(List<Coverage> result,
			LocusInterval2D component) {
		if (!result.isEmpty()) {
			Coverage previous = result.get(result.size() - 1);
			LocusInterval2D previousInterval = previous.interval;
			if (previous.state == CoverageState.CERTIFIED_EXISTS
					&& previousInterval.getUpper() == component.getLower()
					&& (previousInterval.isUpperClosed()
							|| component.isLowerClosed())) {
				result.set(result.size() - 1, new Coverage(new LocusInterval2D(
						previousInterval.getLower(), component.getUpper(),
						previousInterval.isLowerClosed(), component.isUpperClosed()),
						CoverageState.CERTIFIED_EXISTS,
						"provider-interval-contract/v1"));
				return;
			}
		}
		result.add(new Coverage(component, CoverageState.CERTIFIED_EXISTS,
				"provider-interval-contract/v1"));
	}

	private static void addGap(List<Coverage> result, double lower, double upper,
			boolean lowerClosed, boolean upperClosed) {
		if (lower < upper || lower == upper && lowerClosed && upperClosed) {
			result.add(new Coverage(new LocusInterval2D(lower, upper, lowerClosed,
					upperClosed), CoverageState.CERTIFIED_DOES_NOT_EXIST,
					"provider-complement-contract/v1"));
		}
	}

	private static List<Coverage> immutableCoverage(List<Coverage> input,
			LocusInterval2D domain) {
		ArrayList<Coverage> copy = new ArrayList<>(Objects.requireNonNull(input));
		copy.sort(Comparator.comparingDouble(item -> item.interval.getLower()));
		validateIntervals(copy.stream().map(item -> item.interval).toList(), domain,
				false);
		return Collections.unmodifiableList(copy);
	}

	private static List<ContinuousComponent> immutableComponents(
			List<ContinuousComponent> input, LocusInterval2D domain) {
		ArrayList<ContinuousComponent> copy = new ArrayList<>(
				Objects.requireNonNull(input));
		copy.sort(Comparator.comparingDouble(item -> item.interval.getLower()));
		validateIntervals(copy.stream().map(item -> item.interval).toList(), domain,
				true);
		return Collections.unmodifiableList(copy);
	}

	private static void validateIntervals(List<LocusInterval2D> intervals,
			LocusInterval2D domain, boolean allowSharedClosedEndpoint) {
		LocusInterval2D previous = null;
		for (LocusInterval2D interval : intervals) {
			Objects.requireNonNull(interval);
			if (!contains(domain, interval)) {
				throw new IllegalArgumentException(
						"Existence evidence lies outside the canonical domain");
			}
			if (previous != null && (interval.getLower() < previous.getUpper()
					|| !allowSharedClosedEndpoint
							&& interval.getLower() == previous.getUpper()
							&& interval.isLowerClosed()
							&& previous.isUpperClosed())) {
				throw new IllegalArgumentException(
						"Existence evidence intervals must be disjoint");
			}
			previous = interval;
		}
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()
				|| !value.equals(value.trim())) {
			throw new IllegalArgumentException(name + " must be canonical");
		}
		return value;
	}
}

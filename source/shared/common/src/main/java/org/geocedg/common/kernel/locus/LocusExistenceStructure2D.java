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
 *
 * <p>PRE-G9B-CU2 ({@code locus-v2-existence-components.md} section 15): on a periodic
 * domain a component may be an arc with two distinct one-sided ends; point components are
 * a kind of their own; every coverage assertion carries a typed cause and every fact the
 * nature of its proof; the closed-periodic flag of the certificate is the single closure
 * authority. The legacy interval views are adapters: an arc that crosses the seam is
 * returned as one lifted interval, and point components never appear in them.</p>
 */
public final class LocusExistenceStructure2D {
	/**
	 * Contract of the explicit certificate of a static or analytic producer ({@code
	 * locus-v2-existence-components.md} section 15.7).
	 */
	public static final String PROVIDER_PROVED_CONTRACT = "locus-existence/provider-proved/v1";
	/** Origin token of the ends and closures that a producer's contract declares. */
	private static final String PROVIDER_SOURCE = "provider-contract";

	/** Whether the finite component inventory is globally established. */
	public enum Completeness {
		COMPLETE,
		NOT_ESTABLISHED
	}

	/** Interval-wide existence knowledge. */
	public enum CoverageState {
		CERTIFIED_EXISTS,
		CERTIFIED_DOES_NOT_EXIST,
		UNRESOLVED,
		/**
		 * Existence holds exactly at the point components inside the interval and
		 * nowhere else in it (PRE-G9B-CU2, section 15.4).
		 */
		CERTIFIED_ISOLATED_POINTS,
		/**
		 * The interval is the certified enclosure of exactly one root that bounds a
		 * component; on each side of the root existence follows the adjacent coverage, and
		 * the root itself follows the inclusion of the boundary (PRE-G9B-CU2, sections 15.2
		 * and 7: the enclosure is never rounded into the invalid region).
		 */
		CERTIFIED_BOUNDARY
	}

	/** Whether a certified component is known maximal. */
	public enum Maximality {
		ESTABLISHED,
		LOCAL_CERTIFIED_SUBINTERVAL
	}

	/** Topology of the canonical driver domain (PRE-G9B-CU2, section 15.3). */
	public enum DomainTopology {
		/** A bounded interval: components are intervals. */
		BOUNDED,
		/**
		 * A periodic, half-open domain: components may be arcs that cross the seam, whose
		 * representative is the included end of the domain.
		 */
		PERIODIC
	}

	/** One interval-wide existence assertion and its reconstructible evidence. */
	public static final class Coverage {
		private final LocusInterval2D interval;
		private final CoverageState state;
		private final String evidence;
		private final LocusCoverageCause2D cause;
		private final LocusEvidenceNature2D nature;

		/** Creates one immutable interval-wide coverage assertion without a typed cause. */
		public Coverage(LocusInterval2D interval, CoverageState state,
				String evidence) {
			this(interval, state, LocusCoverageCause2D.UNTYPED,
					LocusEvidenceNature2D.NONE, evidence);
		}

		/**
		 * Creates one immutable interval-wide coverage assertion with its typed cause and
		 * the nature of its proof.
		 */
		public Coverage(LocusInterval2D interval, CoverageState state,
				LocusCoverageCause2D cause, LocusEvidenceNature2D nature, String evidence) {
			this.interval = Objects.requireNonNull(interval);
			this.state = Objects.requireNonNull(state);
			this.cause = Objects.requireNonNull(cause);
			this.nature = Objects.requireNonNull(nature);
			this.evidence = requireText(evidence, "Coverage evidence");
			if (!cause.admits(state)) {
				throw new IllegalArgumentException("Coverage cause " + cause
						+ " cannot justify " + state);
			}
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

		/** @return typed cause of the assertion */
		public LocusCoverageCause2D getCause() {
			return cause;
		}

		/** @return nature of the proof of the assertion */
		public LocusEvidenceNature2D getNature() {
			return nature;
		}

		private String signature() {
			return interval + ":" + state + ":" + cause + ":" + nature + ":" + evidence;
		}
	}

	/**
	 * One revision-local certified component of positive length: an interval, or an arc
	 * of a periodic domain given by its canonical start and end parameters.
	 */
	public static final class ContinuousComponent {
		private final LocusInterval2D interval;
		private final String evidenceKey;
		private final String boundaryEvidence;
		private final Maximality maximality;
		private final String numericGuarantee;
		private final LocusInterval2D periodicDomain;
		private final double start;
		private final double end;
		private final boolean fullLength;
		private final LocusBoundary2D lowerBoundary;
		private final LocusBoundary2D upperBoundary;
		private final LocusEvidenceNature2D nature;

		/**
		 * Creates one revision-local continuous-valid component certificate recorded
		 * without typed boundaries.
		 */
		public ContinuousComponent(LocusInterval2D interval, String evidenceKey,
				String boundaryEvidence, Maximality maximality,
				String numericGuarantee) {
			this(interval, evidenceKey, boundaryEvidence, maximality, numericGuarantee,
					null, interval.getLower(), interval.getUpper(), false,
					LocusBoundary2D.untyped(interval.isLowerClosed()),
					LocusBoundary2D.untyped(interval.isUpperClosed()),
					LocusEvidenceNature2D.NONE);
		}

		private ContinuousComponent(LocusInterval2D interval, String evidenceKey,
				String boundaryEvidence, Maximality maximality, String numericGuarantee,
				LocusInterval2D periodicDomain, double start, double end,
				boolean fullLength, LocusBoundary2D lowerBoundary,
				LocusBoundary2D upperBoundary, LocusEvidenceNature2D nature) {
			this.interval = Objects.requireNonNull(interval);
			this.evidenceKey = requireText(evidenceKey, "Component evidence key");
			this.boundaryEvidence = requireText(boundaryEvidence,
					"Boundary evidence");
			this.maximality = Objects.requireNonNull(maximality);
			this.numericGuarantee = requireText(numericGuarantee,
					"Numeric guarantee");
			this.periodicDomain = periodicDomain;
			this.start = start;
			this.end = end;
			this.fullLength = fullLength;
			this.lowerBoundary = Objects.requireNonNull(lowerBoundary);
			this.upperBoundary = Objects.requireNonNull(upperBoundary);
			this.nature = Objects.requireNonNull(nature);
		}

		/**
		 * Creates a typed interval component.
		 *
		 * @param interval certified interval of positive length
		 * @param lower lower end, whose inclusion is that of the interval; an enclosed root
		 *        has the closed upper bound of its enclosure as the interval end
		 * @param upper upper end, whose inclusion is that of the interval; an enclosed root
		 *        has the closed lower bound of its enclosure as the interval end
		 * @param maximality whether the component is known maximal
		 * @param nature nature of the proof of existence and continuity
		 * @param evidenceKey revision-local evidence key; never a durable identity
		 * @return interval component
		 */
		public static ContinuousComponent interval(LocusInterval2D interval,
				LocusBoundary2D lower, LocusBoundary2D upper, Maximality maximality,
				LocusEvidenceNature2D nature, String evidenceKey) {
			if (!(interval.getLower() < interval.getUpper())) {
				throw new IllegalArgumentException(
						"A component interval has positive length");
			}
			if (lower.isBinaryEndIncluded() != interval.isLowerClosed()
					|| upper.isBinaryEndIncluded() != interval.isUpperClosed()) {
				throw new IllegalArgumentException(
						"End inclusion must follow the boundaries");
			}
			if (lower.getKind() == LocusBoundary2D.Kind.PERIODIC_CLOSURE
					|| upper.getKind() == LocusBoundary2D.Kind.PERIODIC_CLOSURE) {
				throw new IllegalArgumentException("Only a full-length arc closes");
			}
			return new ContinuousComponent(interval, evidenceKey,
					lower.getSignature() + ";" + upper.getSignature(), maximality,
					nature.name(), null, interval.getLower(), interval.getUpper(), false,
					lower, upper, nature);
		}

		/**
		 * Creates a typed arc of a periodic, half-open canonical domain.
		 *
		 * @param periodicDomain periodic canonical domain
		 * @param start canonical start parameter, at arc coordinate {@code s = 0}
		 * @param end canonical end parameter, at arc coordinate {@code s = length}: equal
		 *        to the start exactly for a full-length arc, and below it for an arc that
		 *        crosses the seam
		 * @param fullLength whether the arc has the length of the period
		 * @param startBoundary start end of the arc
		 * @param endBoundary end end of the arc
		 * @param maximality whether the component is known maximal
		 * @param nature nature of the proof of existence and continuity
		 * @param evidenceKey revision-local evidence key; never a durable identity
		 * @return arc component
		 */
		public static ContinuousComponent arc(LocusInterval2D periodicDomain,
				double start, double end, boolean fullLength,
				LocusBoundary2D startBoundary, LocusBoundary2D endBoundary,
				Maximality maximality, LocusEvidenceNature2D nature, String evidenceKey) {
			if (periodicDomain.isLowerClosed() == periodicDomain.isUpperClosed()
					|| !(periodicDomain.getLower() < periodicDomain.getUpper())) {
				throw new IllegalArgumentException(
						"A periodic canonical domain is half-open and nonempty");
			}
			if (!periodicDomain.contains(start, 0) || !periodicDomain.contains(end, 0)) {
				throw new IllegalArgumentException(
						"Arc ends must be canonical parameters of the periodic domain");
			}
			if (fullLength != (start == end)) {
				throw new IllegalArgumentException(
						"Only a full-length arc starts and ends at the same parameter");
			}
			boolean closingStart = startBoundary.getKind()
					== LocusBoundary2D.Kind.PERIODIC_CLOSURE;
			boolean closingEnd = endBoundary.getKind()
					== LocusBoundary2D.Kind.PERIODIC_CLOSURE;
			if (closingStart != closingEnd || closingStart && !fullLength) {
				throw new IllegalArgumentException(
						"A periodic closure needs a full-length arc with both closing ends");
			}
			if (fullLength && !closingStart && startBoundary.isBinaryEndIncluded()
					&& endBoundary.isBinaryEndIncluded()) {
				throw new IllegalArgumentException(
						"The two ends of an open full-length arc are never identified");
			}
			double period = periodicDomain.getUpper() - periodicDomain.getLower();
			LocusInterval2D lifted;
			if (closingStart && start == seam(periodicDomain)) {
				lifted = periodicDomain;
			} else if (fullLength) {
				lifted = new LocusInterval2D(start, start + period,
						closingStart || startBoundary.isBinaryEndIncluded(),
						!closingEnd && endBoundary.isBinaryEndIncluded());
			} else {
				lifted = new LocusInterval2D(start, end > start ? end : end + period,
						startBoundary.isBinaryEndIncluded(),
						endBoundary.isBinaryEndIncluded());
			}
			return new ContinuousComponent(lifted, evidenceKey,
					startBoundary.getSignature() + ";" + endBoundary.getSignature(),
					maximality, nature.name(), periodicDomain, start, end, fullLength,
					startBoundary, endBoundary, nature);
		}

		/**
		 * Types the ends of an interval component recorded by a certifier that predates
		 * PRE-G9B-CU2, keeping its recorded evidence texts.
		 *
		 * @param lower lower end, whose inclusion is that of the interval
		 * @param upper upper end, whose inclusion is that of the interval
		 * @param proofNature nature of the proof of existence and continuity
		 * @return the same component with typed ends
		 */
		public ContinuousComponent withTypedBoundaries(LocusBoundary2D lower,
				LocusBoundary2D upper, LocusEvidenceNature2D proofNature) {
			if (periodicDomain != null
					|| lower.getKind() == LocusBoundary2D.Kind.PERIODIC_CLOSURE
					|| upper.getKind() == LocusBoundary2D.Kind.PERIODIC_CLOSURE) {
				throw new IllegalArgumentException("Only interval ends are typed here");
			}
			if (lower.isBinaryEndIncluded() != interval.isLowerClosed()
					|| upper.isBinaryEndIncluded() != interval.isUpperClosed()) {
				throw new IllegalArgumentException(
						"End inclusion must follow the boundaries");
			}
			return new ContinuousComponent(interval, evidenceKey, boundaryEvidence,
					maximality, numericGuarantee, null, start, end, false, lower, upper,
					proofNature);
		}

		/**
		 * @return certified interval; for an arc, the lifted interval from its start, whose
		 *         upper end exceeds the canonical domain when the arc crosses the seam
		 */
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

		/** @return whether the component is an arc of a periodic domain */
		public boolean isArc() {
			return periodicDomain != null;
		}

		/** @return canonical start parameter: the lower end of an interval */
		public double getStart() {
			return start;
		}

		/** @return canonical end parameter: the upper end of an interval */
		public double getEnd() {
			return end;
		}

		/** @return whether the arc has the length of the period */
		public boolean isFullLength() {
			return fullLength;
		}

		/** @return whether the arc crosses the seam of its periodic domain */
		public boolean crossesSeam() {
			return periodicDomain != null && !fullLength && end < start;
		}

		/**
		 * @return whether this is a certified closed periodic component: a full-length arc
		 *         with certified continuity at its closing point
		 */
		public boolean isClosedPeriodic() {
			return fullLength
					&& lowerBoundary.getKind() == LocusBoundary2D.Kind.PERIODIC_CLOSURE;
		}

		/** @return lower end of an interval, or start end of an arc ({@code s = 0}) */
		public LocusBoundary2D getLowerBoundary() {
			return lowerBoundary;
		}

		/** @return upper end of an interval, or end end of an arc ({@code s = length}) */
		public LocusBoundary2D getUpperBoundary() {
			return upperBoundary;
		}

		/** @return nature of the proof of existence and continuity */
		public LocusEvidenceNature2D getNature() {
			return nature;
		}

		/** @return arc length, or interval length for an interval component */
		public double getLength() {
			if (periodicDomain == null) {
				return interval.getUpper() - interval.getLower();
			}
			double period = periodicDomain.getUpper() - periodicDomain.getLower();
			return fullLength ? period : end > start ? end - start : end - start + period;
		}

		/**
		 * Tests membership of one canonical parameter, without any tolerance: on an arc a
		 * parameter belongs when an included arc coordinate exists.
		 *
		 * @return whether the parameter belongs to the component
		 */
		public boolean contains(double canonicalParameter) {
			return !Double.isNaN(getArcCoordinate(canonicalParameter));
		}

		/**
		 * Membership with the provider tolerance of the domain, for consumers that read it
		 * (PRE-G9B-CU2, consumption design section 4): an interval, a partial arc whose lifted
		 * interval lies in its domain and a closed periodic arc from the seam test that
		 * interval with the tolerance, as the legacy views did; any other arc, which crosses
		 * the seam or is an open arc of full length, tests its arc coordinate exactly
		 * ({@link #contains(double)}).
		 *
		 * @param domainEpsilon provider-owned domain tolerance
		 * @return whether the parameter belongs to the component
		 */
		public boolean contains(double canonicalParameter, double domainEpsilon) {
			return !Double.isNaN(getArcCoordinate(canonicalParameter, domainEpsilon));
		}

		/**
		 * Arc coordinate under the membership of {@link #contains(double, double)}: the
		 * offset from the start of a component tested as an interval, which follows the
		 * canonical order, and the exact arc coordinate of any other arc.
		 *
		 * @param domainEpsilon provider-owned domain tolerance
		 * @return arc coordinate, or {@code NaN} when the parameter does not belong
		 */
		public double getArcCoordinate(double canonicalParameter, double domainEpsilon) {
			if (followsCanonicalOrder()) {
				return interval.contains(canonicalParameter, domainEpsilon)
						? canonicalParameter - start : Double.NaN;
			}
			return getArcCoordinate(canonicalParameter);
		}

		/**
		 * @return the canonical intervals that the component covers, in canonical order: its
		 *         own interval when {@link #contains(double, double)} tests it as an
		 *         interval, otherwise its pieces split at the seam, for consumers that search
		 *         canonical intervals; they belong to this one component
		 */
		public List<LocusInterval2D> getCanonicalPieces() {
			return followsCanonicalOrder() ? Collections.singletonList(interval)
					: canonicalPieces();
		}

		/**
		 * @return whether the lifted interval lies in the canonical domain and follows its
		 *         order, so that canonical parameters are its own coordinates: an interval,
		 *         a partial arc that does not cross the seam, or a closed periodic arc from
		 *         the seam; otherwise consumers use the arc coordinate
		 */
		public boolean followsCanonicalOrder() {
			return periodicDomain == null
					|| !fullLength && interval.getUpper() <= periodicDomain.getUpper()
					|| isClosedPeriodic() && start == seam(periodicDomain);
		}

		/**
		 * @return the unique included arc coordinate {@code s} of the canonical parameter,
		 *         measured from the start; {@code NaN} when it does not belong. On a
		 *         full-length open arc the shared parameter of the two ends maps to the
		 *         included end only; the excluded end is reached only as a one-sided limit.
		 */
		public double getArcCoordinate(double canonicalParameter) {
			double t = canonicalParameter;
			if (periodicDomain == null) {
				return interval.contains(t, 0) ? t - start : Double.NaN;
			}
			if (!periodicDomain.contains(t, 0)) {
				return Double.NaN;
			}
			double period = periodicDomain.getUpper() - periodicDomain.getLower();
			if (fullLength) {
				if (t != start) {
					return t > start ? t - start : t - start + period;
				}
				if (isClosedPeriodic() || lowerBoundary.isBinaryEndIncluded()) {
					return 0;
				}
				return upperBoundary.isBinaryEndIncluded() ? period : Double.NaN;
			}
			if (t == start) {
				return lowerBoundary.isBinaryEndIncluded() ? 0 : Double.NaN;
			}
			if (t == end) {
				return upperBoundary.isBinaryEndIncluded() ? getLength() : Double.NaN;
			}
			if (end > start) {
				return t > start && t < end ? t - start : Double.NaN;
			}
			if (t > start) {
				return t - start;
			}
			return t < end ? t - start + period : Double.NaN;
		}

		/** @return the canonical intervals covered by the component, split at the seam */
		private List<LocusInterval2D> canonicalPieces() {
			if (periodicDomain == null || !fullLength && end > start) {
				return Collections.singletonList(periodicDomain == null ? interval
						: new LocusInterval2D(start, end, lowerBoundary.isBinaryEndIncluded(),
								upperBoundary.isBinaryEndIncluded()));
			}
			double lower = periodicDomain.getLower();
			double upper = periodicDomain.getUpper();
			boolean lowerClosed = periodicDomain.isLowerClosed();
			boolean upperClosed = periodicDomain.isUpperClosed();
			List<LocusInterval2D> pieces = new ArrayList<>();
			if (fullLength) {
				boolean closing = isClosedPeriodic();
				boolean shared = closing || lowerBoundary.isBinaryEndIncluded()
						|| upperBoundary.isBinaryEndIncluded();
				if (start == seam(periodicDomain)) {
					pieces.add(new LocusInterval2D(lower, upper,
							lowerClosed && shared, upperClosed && shared));
					return pieces;
				}
				if (start > lower) {
					pieces.add(new LocusInterval2D(lower, start, lowerClosed,
							!closing && upperBoundary.isBinaryEndIncluded()));
				}
				if (start < upper) {
					pieces.add(new LocusInterval2D(start, upper,
							closing || lowerBoundary.isBinaryEndIncluded(), upperClosed));
				}
				return pieces;
			}
			if (end > lower || upperBoundary.isBinaryEndIncluded() && lowerClosed) {
				pieces.add(new LocusInterval2D(lower, end, lowerClosed,
						upperBoundary.isBinaryEndIncluded()));
			}
			if (start < upper || lowerBoundary.isBinaryEndIncluded() && upperClosed) {
				pieces.add(new LocusInterval2D(start, upper, lowerBoundary.isBinaryEndIncluded(),
						upperClosed));
			}
			return pieces;
		}

		private String signature() {
			StringBuilder signature = new StringBuilder(interval.toString()).append(':')
					.append(evidenceKey).append(':').append(boundaryEvidence).append(':')
					.append(maximality).append(':').append(numericGuarantee);
			if (periodicDomain != null) {
				signature.append(":arc[").append(Double.toHexString(start)).append(',')
						.append(Double.toHexString(end)).append(',')
						.append(fullLength ? "full" : "partial").append(']');
			}
			return signature.append(':').append(lowerBoundary.getSignature()).append(':')
					.append(upperBoundary.getSignature()).append(':').append(nature)
					.toString();
		}
	}

	private final String branchKey;
	private final LocusInterval2D canonicalDomain;
	private final DomainTopology topology;
	private final long semanticRevision;
	private final List<Coverage> coverage;
	private final List<ContinuousComponent> components;
	private final List<LocusPointComponent2D> pointComponents;
	private final List<LocusSingularParameter2D> singularParameters;
	private final List<LocusCertifiedFold2D> certifiedFolds;
	private final Completeness completeness;
	private final String certifierContract;

	/** Creates validated immutable semantic evidence on a bounded domain. */
	public LocusExistenceStructure2D(String branchKey,
			LocusInterval2D canonicalDomain, long semanticRevision,
			List<Coverage> coverage, List<ContinuousComponent> components,
			Completeness completeness, String certifierContract) {
		this(branchKey, canonicalDomain, DomainTopology.BOUNDED, semanticRevision,
				coverage, components, Collections.emptyList(), completeness,
				certifierContract);
	}

	/**
	 * Creates validated immutable semantic evidence with the domain topology and the point
	 * components (PRE-G9B-CU2).
	 */
	public LocusExistenceStructure2D(String branchKey,
			LocusInterval2D canonicalDomain, DomainTopology topology,
			long semanticRevision, List<Coverage> coverage,
			List<ContinuousComponent> components,
			List<LocusPointComponent2D> pointComponents, Completeness completeness,
			String certifierContract) {
		this(branchKey, canonicalDomain, topology, semanticRevision, coverage, components,
				pointComponents, Collections.emptyList(), completeness, certifierContract);
	}

	/**
	 * Creates validated immutable semantic evidence with the certified singular parameters
	 * interior to its components (PRE-G9B-CU2, section 15.2).
	 */
	public LocusExistenceStructure2D(String branchKey,
			LocusInterval2D canonicalDomain, DomainTopology topology,
			long semanticRevision, List<Coverage> coverage,
			List<ContinuousComponent> components,
			List<LocusPointComponent2D> pointComponents,
			List<LocusSingularParameter2D> singularParameters, Completeness completeness,
			String certifierContract) {
		this(branchKey, canonicalDomain, topology, semanticRevision, coverage, components,
				pointComponents, singularParameters, Collections.emptyList(), completeness,
				certifierContract);
	}

	/**
	 * Creates validated immutable semantic evidence with the certified folds of the traced
	 * map interior to its components (PRE-G9B-CU2, certification design section 5), which a
	 * drawing reads for its only turn exemption besides singular parameters.
	 */
	public LocusExistenceStructure2D(String branchKey,
			LocusInterval2D canonicalDomain, DomainTopology topology,
			long semanticRevision, List<Coverage> coverage,
			List<ContinuousComponent> components,
			List<LocusPointComponent2D> pointComponents,
			List<LocusSingularParameter2D> singularParameters,
			List<LocusCertifiedFold2D> certifiedFolds, Completeness completeness,
			String certifierContract) {
		this.branchKey = requireText(branchKey, "Branch key");
		this.canonicalDomain = Objects.requireNonNull(canonicalDomain);
		this.topology = Objects.requireNonNull(topology);
		if (topology == DomainTopology.PERIODIC
				&& canonicalDomain.isLowerClosed() == canonicalDomain.isUpperClosed()) {
			throw new IllegalArgumentException("A periodic canonical domain is half-open");
		}
		if (semanticRevision < 0) {
			throw new IllegalArgumentException("Semantic revision cannot be negative");
		}
		this.semanticRevision = semanticRevision;
		this.coverage = immutableCoverage(coverage, canonicalDomain);
		this.components = immutableComponents(components, canonicalDomain, topology);
		this.pointComponents = immutablePoints(pointComponents);
		ArrayList<LocusSingularParameter2D> singular = new ArrayList<>(
				Objects.requireNonNull(singularParameters));
		singular.forEach(Objects::requireNonNull);
		singular.sort(Comparator.comparingDouble(
				LocusSingularParameter2D::getParameterLower));
		this.singularParameters = Collections.unmodifiableList(singular);
		ArrayList<LocusCertifiedFold2D> folds = new ArrayList<>(
				Objects.requireNonNull(certifiedFolds));
		folds.forEach(Objects::requireNonNull);
		folds.sort(Comparator.comparingDouble(LocusCertifiedFold2D::getParameterLower));
		this.certifiedFolds = Collections.unmodifiableList(folds);
		this.completeness = Objects.requireNonNull(completeness);
		this.certifierContract = requireText(certifierContract,
				"Certifier contract");
		validateConsistency();
	}

	/**
	 * @param folds certified folds of the traced map, interior to the components
	 * @return the same evidence with the certified folds
	 */
	public LocusExistenceStructure2D withCertifiedFolds(List<LocusCertifiedFold2D> folds) {
		return new LocusExistenceStructure2D(branchKey, canonicalDomain, topology,
				semanticRevision, coverage, components, pointComponents, singularParameters,
				folds, completeness, certifierContract);
	}

	/**
	 * Explicit certificate of a static or analytic producer whose own contract proves
	 * existence and continuity on the components it declares ({@code
	 * locus-v2-existence-components.md} section 15.7; decision D-CU2-7, option A; ADR 0027).
	 * Every fact cites that contract and is an exact structural proof: the components exist,
	 * their complement in the canonical domain does not, and their ends are the ends the
	 * contract declares. On a periodic domain a single component equal to the domain is the
	 * closed periodic arc only when the contract proves continuity across the seam;
	 * otherwise it is the open arc of full length from the seam. This replaces the
	 * provider-compatibility certificate, retired by step T11 of PRE-G9B-CU2.
	 *
	 * @param branchKey branch key
	 * @param canonicalDomain canonical driver domain of the provider
	 * @param intervals disjoint components that the contract proves
	 * @param periodic whether the canonical domain is periodic
	 * @param closureProved whether the contract proves continuity across the seam of a
	 *        periodic domain whose single component is the whole domain
	 * @param providerContract versioned contract of the producer that proves the facts
	 * @return complete explicit certificate of the producer
	 */
	public static LocusExistenceStructure2D providerProved(String branchKey,
			LocusInterval2D canonicalDomain, List<LocusInterval2D> intervals,
			boolean periodic, boolean closureProved, String providerContract) {
		requireText(providerContract, "Provider contract");
		boolean periodicDomain = topologyOf(canonicalDomain, periodic)
				== DomainTopology.PERIODIC;
		ArrayList<LocusInterval2D> ordered = new ArrayList<>(intervals);
		ordered.sort(Comparator.comparingDouble(LocusInterval2D::getLower));
		ArrayList<Coverage> coverage = completeCoverage(canonicalDomain, ordered,
				providerContract);
		ArrayList<ContinuousComponent> components = new ArrayList<>();
		ArrayList<LocusPointComponent2D> points = new ArrayList<>();
		boolean wholePeriodic = periodicDomain && ordered.size() == 1
				&& ordered.get(0).equals(canonicalDomain);
		int index = 0;
		for (LocusInterval2D interval : ordered) {
			String key = "provider-component/" + index++;
			if (interval.getLower() == interval.getUpper()) {
				// An isolated declared parameter is a point component, never a zero-length
				// interval (section 15.4); its position is not part of the declaration.
				points.add(new LocusPointComponent2D(interval.getLower(), interval.getUpper(),
						new LocusBoundarySource2D(PROVIDER_SOURCE, "declared-isolated-parameter",
								"point"), Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
						key, null));
			} else if (wholePeriodic) {
				double seam = seam(canonicalDomain);
				LocusBoundary2D start = closureProved ? new LocusBoundary2D(
						LocusBoundary2D.Kind.PERIODIC_CLOSURE, new LocusBoundarySource2D(
								PROVIDER_SOURCE, "periodic-closure", "seam"),
						true, LocusEvidenceNature2D.EXACT)
						: providerEnd("lower", canonicalDomain.isLowerClosed());
				LocusBoundary2D end = closureProved ? start
						: providerEnd("upper", canonicalDomain.isUpperClosed());
				components.add(ContinuousComponent.arc(canonicalDomain, seam, seam, true,
						start, end, Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT,
						key));
			} else {
				components.add(ContinuousComponent.interval(interval,
						providerEnd("lower", interval.isLowerClosed()),
						providerEnd("upper", interval.isUpperClosed()),
						Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, key));
			}
		}
		return new LocusExistenceStructure2D(branchKey, canonicalDomain,
				periodicDomain ? DomainTopology.PERIODIC : DomainTopology.BOUNDED, 0,
				coverage, components, points, Completeness.COMPLETE,
				PROVIDER_PROVED_CONTRACT);
	}

	/**
	 * The same evidence restricted to the components that a generator declares for its
	 * driver domain (PRE-G9B-CU2 step T11; ADR 0027): outside them the locus has no driver
	 * parameter, so its nonexistence is exact by the declaration; inside them every fact is
	 * kept, and an interval component that a declared gap cuts ends at the declared ends. An
	 * arc is never cut: when a declared gap would cut one, nothing inside the declared
	 * components is certified any more (conservative). Certified existence that the
	 * declaration reduces to one parameter, such as a declared isolated parameter inside a
	 * certified component, is a point component with isolated-point coverage (section 15.4;
	 * author resolution before step T12): never a zero-length interval and never dropped.
	 *
	 * @param declared disjoint declared components of the driver domain
	 * @param declarationEvidence evidence of the declaration
	 * @return the restricted evidence
	 */
	public LocusExistenceStructure2D restrictedTo(List<LocusInterval2D> declared,
			String declarationEvidence) {
		ArrayList<LocusInterval2D> ordered = new ArrayList<>(declared);
		ordered.sort(Comparator.comparingDouble(LocusInterval2D::getLower));
		ArrayList<ContinuousComponent> kept = new ArrayList<>();
		ArrayList<LocusPointComponent2D> isolated = new ArrayList<>();
		boolean cutArc = false;
		for (ContinuousComponent component : components) {
			if (component.isArc()) {
				if (insideOne(component.canonicalPieces(), ordered)) {
					kept.add(component);
				} else {
					cutArc = true;
				}
				continue;
			}
			int piece = 0;
			for (LocusInterval2D interval : ordered) {
				LocusInterval2D part = intersection(component.interval, interval);
				if (part == null) {
					continue;
				}
				if (!(part.getLower() < part.getUpper())) {
					if (component.nature.certifies()) {
						isolated.add(new LocusPointComponent2D(part.getLower(),
								part.getUpper(), new LocusBoundarySource2D(PROVIDER_SOURCE,
										"declared-isolated-parameter", "point"),
								component.maximality, component.nature,
								component.evidenceKey + "/declared-point-" + piece++, null));
					}
					continue;
				}
				if (part.equals(component.interval)) {
					kept.add(component);
					continue;
				}
				boolean sameLower = part.getLower() == component.interval.getLower()
						&& part.isLowerClosed() == component.interval.isLowerClosed();
				boolean sameUpper = part.getUpper() == component.interval.getUpper()
						&& part.isUpperClosed() == component.interval.isUpperClosed();
				kept.add(ContinuousComponent.interval(part,
						sameLower ? component.lowerBoundary
								: declaredEnd("lower", part.isLowerClosed()),
						sameUpper ? component.upperBoundary
								: declaredEnd("upper", part.isUpperClosed()),
						component.maximality, component.nature,
						component.evidenceKey + "/declared-piece-" + piece++));
			}
		}
		ArrayList<Coverage> restricted = new ArrayList<>();
		for (Coverage item : coverage) {
			for (LocusInterval2D interval : ordered) {
				LocusInterval2D part = intersection(item.interval, interval);
				if (part == null) {
					continue;
				}
				if (cutArc && item.state != CoverageState.CERTIFIED_DOES_NOT_EXIST) {
					restricted.add(new Coverage(part, CoverageState.UNRESOLVED,
							LocusCoverageCause2D.NOT_CERTIFIED, LocusEvidenceNature2D.NONE,
							"declared-components-cut-an-arc"));
				} else if (item.state == CoverageState.CERTIFIED_EXISTS
						&& isolatedAt(isolated, part)) {
					restricted.add(new Coverage(part, CoverageState.CERTIFIED_ISOLATED_POINTS,
							LocusCoverageCause2D.PROVIDER_ISOLATED_POINT, item.nature,
							declarationEvidence + "|" + item.evidence));
				} else {
					restricted.add(new Coverage(part, item.state, item.cause, item.nature,
							item.evidence));
				}
			}
		}
		double cursor = canonicalDomain.getLower();
		boolean cursorOwned = canonicalDomain.isLowerClosed();
		for (LocusInterval2D interval : ordered) {
			addGap(restricted, cursor, interval.getLower(), cursorOwned,
					!interval.isLowerClosed(), declarationEvidence);
			cursor = interval.getUpper();
			cursorOwned = !interval.isUpperClosed();
		}
		addGap(restricted, cursor, canonicalDomain.getUpper(), cursorOwned,
				canonicalDomain.isUpperClosed(), declarationEvidence);
		if (cutArc) {
			return new LocusExistenceStructure2D(branchKey, canonicalDomain, topology,
					semanticRevision, restricted, Collections.emptyList(),
					Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
					Completeness.NOT_ESTABLISHED, certifierContract);
		}
		ArrayList<LocusPointComponent2D> points = new ArrayList<>();
		for (LocusPointComponent2D point : pointComponents) {
			if (insideOne(List.of(new LocusInterval2D(point.getParameterLower(),
					point.getParameterUpper(), true, true)), ordered)) {
				points.add(point);
			}
		}
		points.addAll(isolated);
		points.sort(Comparator.comparingDouble(LocusPointComponent2D::getParameterLower));
		ArrayList<LocusSingularParameter2D> singular = new ArrayList<>();
		for (LocusSingularParameter2D parameter : singularParameters) {
			if (insideOne(List.of(new LocusInterval2D(parameter.getParameterLower(),
					parameter.getParameterUpper(), true, true)), ordered)) {
				singular.add(parameter);
			}
		}
		ArrayList<LocusCertifiedFold2D> folds = new ArrayList<>();
		for (LocusCertifiedFold2D fold : certifiedFolds) {
			if (insideOne(List.of(new LocusInterval2D(fold.getParameterLower(),
					fold.getParameterUpper(), true, true)), ordered)) {
				folds.add(fold);
			}
		}
		// Without any declared component the whole domain is certified nonexistence.
		return new LocusExistenceStructure2D(branchKey, canonicalDomain, topology,
				semanticRevision, restricted, kept, points, singular, folds,
				ordered.isEmpty() ? Completeness.COMPLETE : completeness, certifierContract);
	}

	/**
	 * Existence of a dependent on a complete locus support whose traced point follows the
	 * generator point of the support through structurally total and continuous steps only:
	 * the minimal certified composition ({@code locus-v2-certified-construction-model.md}
	 * section 10.10, author decision V-CON-6). The dependent exists exactly where the
	 * support does, so it takes the support's decomposition: its domain, topology, coverage
	 * and completeness, and its components with their shapes, ends, maximality and nature,
	 * never more. The support's nonexistence is a certified nonexistence of a dependency
	 * (section 15.5). Certified singular parameters and folds, facts of the support's own
	 * map, are not composed.
	 *
	 * @param branchKey branch key of the dependent
	 * @param support existence structure of a complete support branch without point
	 *        components
	 * @param certifierContract contract under which the dependent publishes the structure
	 * @param evidence evidence of the composition
	 * @return the composed structure
	 */
	public static LocusExistenceStructure2D composedThroughTotalSteps(String branchKey,
			LocusExistenceStructure2D support, String certifierContract, String evidence) {
		if (support.completeness != Completeness.COMPLETE
				|| !support.pointComponents.isEmpty()) {
			throw new IllegalArgumentException(
					"Only a complete support without point components is composed");
		}
		ArrayList<Coverage> coverage = new ArrayList<>();
		for (Coverage item : support.coverage) {
			coverage.add(item.state == CoverageState.CERTIFIED_DOES_NOT_EXIST
					? new Coverage(item.interval, item.state,
							LocusCoverageCause2D.DEPENDENCY_CERTIFIED_NONEXISTENT, item.nature,
							evidence + "|" + item.evidence)
					: new Coverage(item.interval, item.state, item.cause, item.nature,
							evidence + "|" + item.evidence));
		}
		ArrayList<ContinuousComponent> components = new ArrayList<>();
		for (ContinuousComponent component : support.components) {
			components.add(new ContinuousComponent(component.interval,
					evidence + "|" + component.evidenceKey, component.boundaryEvidence,
					component.maximality, component.numericGuarantee,
					component.periodicDomain, component.start, component.end,
					component.fullLength, component.lowerBoundary, component.upperBoundary,
					component.nature));
		}
		return new LocusExistenceStructure2D(branchKey, support.canonicalDomain,
				support.topology, 0, coverage, components, Collections.emptyList(),
				support.completeness, certifierContract);
	}

	/** @return whether a zero-length part is the parameter of an isolated declared point */
	private static boolean isolatedAt(List<LocusPointComponent2D> isolated,
			LocusInterval2D part) {
		if (part.getLower() != part.getUpper()) {
			return false;
		}
		for (LocusPointComponent2D point : isolated) {
			if (point.getParameterLower() == part.getLower()) {
				return true;
			}
		}
		return false;
	}

	/** @return a component end that the declared driver domain imposes */
	private static LocusBoundary2D declaredEnd(String side, boolean included) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
				new LocusBoundarySource2D(PROVIDER_SOURCE, "declared-component-end", side),
				included, LocusEvidenceNature2D.EXACT);
	}

	/** @return whether every piece lies inside one of the declared intervals */
	private static boolean insideOne(List<LocusInterval2D> pieces,
			List<LocusInterval2D> declared) {
		for (LocusInterval2D piece : pieces) {
			boolean inside = false;
			for (LocusInterval2D interval : declared) {
				inside |= contains(interval, piece);
			}
			if (!inside) {
				return false;
			}
		}
		return true;
	}

	/** @return the intersection of two intervals, or {@code null} when it is empty */
	private static LocusInterval2D intersection(LocusInterval2D first,
			LocusInterval2D second) {
		double lower = Math.max(first.getLower(), second.getLower());
		boolean lowerClosed = (first.getLower() != lower || first.isLowerClosed())
				&& (second.getLower() != lower || second.isLowerClosed());
		double upper = Math.min(first.getUpper(), second.getUpper());
		boolean upperClosed = (first.getUpper() != upper || first.isUpperClosed())
				&& (second.getUpper() != upper || second.isUpperClosed());
		if (lower > upper || lower == upper && !(lowerClosed && upperClosed)) {
			return null;
		}
		return new LocusInterval2D(lower, upper, lowerClosed, upperClosed);
	}

	/**
	 * @return unresolved evidence without a manufactured empty-domain assertion
	 */
	public static LocusExistenceStructure2D notEstablished(String branchKey,
			LocusInterval2D canonicalDomain, String evidence) {
		return notEstablished(branchKey, canonicalDomain, DomainTopology.BOUNDED,
				LocusCoverageCause2D.NOT_CERTIFIED, evidence);
	}

	/**
	 * @param topology topology of the canonical domain
	 * @param cause typed cause of the unresolved coverage
	 * @return unresolved evidence without a manufactured empty-domain assertion
	 */
	public static LocusExistenceStructure2D notEstablished(String branchKey,
			LocusInterval2D canonicalDomain, DomainTopology topology,
			LocusCoverageCause2D cause, String evidence) {
		return new LocusExistenceStructure2D(branchKey, canonicalDomain, topology, 0,
				List.of(new Coverage(canonicalDomain, CoverageState.UNRESOLVED, cause,
						LocusEvidenceNature2D.NONE, evidence)), Collections.emptyList(),
				Collections.emptyList(), Completeness.NOT_ESTABLISHED,
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

	/** @return topology of the canonical driver domain */
	public DomainTopology getTopology() {
		return topology;
	}

	/** @return source semantic revision to which this evidence is bound */
	public long getSemanticRevision() {
		return semanticRevision;
	}

	/** @return canonical ordered interval coverage assertions */
	public List<Coverage> getCoverage() {
		return coverage;
	}

	/** @return canonical ordered locally certified components of positive length */
	public List<ContinuousComponent> getContinuousValidComponents() {
		return components;
	}

	/** @return point components in canonical order (PRE-G9B-CU2, section 15.4) */
	public List<LocusPointComponent2D> getPointComponents() {
		return pointComponents;
	}

	/**
	 * @return certified singular parameters interior to the components, in canonical
	 *         order (PRE-G9B-CU2, section 15.2)
	 */
	public List<LocusSingularParameter2D> getSingularParameters() {
		return singularParameters;
	}

	/**
	 * @return certified non-degenerate folds of the traced map interior to the components,
	 *         in canonical order (PRE-G9B-CU2, certification design section 5)
	 */
	public List<LocusCertifiedFold2D> getCertifiedFolds() {
		return certifiedFolds;
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
	 * Single closure authority of every consumer (PRE-G9B-CU2, section 15.3): a branch is
	 * closed only when its certificate has exactly one component, a certified closed
	 * periodic arc, and no point component. The provider's periodicity never closes it.
	 *
	 * @return whether the branch is a certified closed periodic curve
	 */
	public boolean isClosedPeriodic() {
		return topology == DomainTopology.PERIODIC && components.size() == 1
				&& pointComponents.isEmpty() && components.get(0).isClosedPeriodic();
	}

	/**
	 * @return the component of positive length that contains the canonical parameter, or
	 *         {@code null}; a point component is never selected by a parameter
	 */
	public ContinuousComponent getComponentContaining(double canonicalParameter) {
		for (ContinuousComponent component : components) {
			if (component.contains(canonicalParameter)) {
				return component;
			}
		}
		return null;
	}

	/**
	 * @param upperEnd whether the end is the upper end of an interval or the end of an arc;
	 *        otherwise the lower end or the start
	 * @return the component of positive length with that end at the canonical parameter, or
	 *         {@code null}; ends are compared by their binary64 parameters, without tolerance
	 */
	public ContinuousComponent getComponentWithEnd(double canonicalParameter,
			boolean upperEnd) {
		for (ContinuousComponent component : components) {
			if ((upperEnd ? component.getEnd() : component.getStart()) == canonicalParameter) {
				return component;
			}
		}
		return null;
	}

	/**
	 * @return the point component whose parameter enclosure contains the canonical
	 *         parameter, or {@code null}; a consumer that addresses components by a binary64
	 *         parameter reports it as a typed cause and never selects it (section 15.8)
	 */
	public LocusPointComponent2D getPointComponentEnclosing(double canonicalParameter) {
		for (LocusPointComponent2D point : pointComponents) {
			if (point.getParameterLower() <= canonicalParameter
					&& canonicalParameter <= point.getParameterUpper()) {
				return point;
			}
		}
		return null;
	}

	/**
	 * @return the coverage assertion whose interval contains the canonical parameter, or
	 *         {@code null}; membership is exact
	 */
	public Coverage getCoverageAt(double canonicalParameter) {
		for (Coverage item : coverage) {
			if (item.interval.contains(canonicalParameter, 0)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * @return the same evidence bound to the published semantic revision
	 */
	public LocusExistenceStructure2D bindToRevision(long revision) {
		if (revision < 1) {
			throw new IllegalArgumentException("Published revision must be positive");
		}
		return semanticRevision == revision ? this
				: new LocusExistenceStructure2D(branchKey, canonicalDomain, topology,
						revision, coverage, components, pointComponents, singularParameters,
						certifiedFolds, completeness, certifierContract);
	}

	/**
	 * @return content signature that deliberately excludes the publication revision
	 */
	public String getSemanticSignature() {
		StringBuilder signature = new StringBuilder(certifierContract)
				.append('|').append(branchKey).append('|').append(canonicalDomain)
				.append('|').append(topology).append('|').append(completeness);
		for (Coverage item : coverage) {
			signature.append("|coverage=").append(item.signature());
		}
		for (ContinuousComponent component : components) {
			signature.append("|component=").append(component.signature());
		}
		for (LocusPointComponent2D point : pointComponents) {
			signature.append("|point=").append(point.getSignature());
		}
		for (LocusSingularParameter2D singular : singularParameters) {
			signature.append("|singular=").append(singular.getSignature());
		}
		for (LocusCertifiedFold2D fold : certifiedFolds) {
			signature.append("|fold=").append(fold.getSignature());
		}
		return signature.toString();
	}

	/**
	 * @return {@code PERIODIC} for a periodic, half-open canonical domain; {@code BOUNDED}
	 *         otherwise
	 */
	public static DomainTopology topologyOf(LocusInterval2D canonicalDomain,
			boolean periodic) {
		return periodic && canonicalDomain.isLowerClosed() != canonicalDomain.isUpperClosed()
				? DomainTopology.PERIODIC : DomainTopology.BOUNDED;
	}

	/** @return representative of the seam of a half-open periodic domain */
	private static double seam(LocusInterval2D periodicDomain) {
		return periodicDomain.isLowerClosed() ? periodicDomain.getLower()
				: periodicDomain.getUpper();
	}

	private void validateConsistency() {
		for (ContinuousComponent component : components) {
			for (LocusInterval2D piece : component.canonicalPieces()) {
				if (!coveredBy(piece, CoverageState.CERTIFIED_EXISTS)) {
					throw new IllegalArgumentException(
							"Continuous component lacks existence coverage");
				}
			}
		}
		for (LocusPointComponent2D point : pointComponents) {
			validatePoint(point);
		}
		for (LocusSingularParameter2D singular : singularParameters) {
			LocusInterval2D enclosure = clippedEnclosure(singular.getParameterLower(),
					singular.getParameterUpper(), "Singular parameter");
			if (!coveredBy(enclosure, CoverageState.CERTIFIED_EXISTS)) {
				throw new IllegalArgumentException(
						"Singular parameter lacks existence coverage");
			}
		}
		for (LocusCertifiedFold2D fold : certifiedFolds) {
			LocusInterval2D enclosure = clippedEnclosure(fold.getParameterLower(),
					fold.getParameterUpper(), "Fold");
			if (!coveredBy(enclosure, CoverageState.CERTIFIED_EXISTS)) {
				throw new IllegalArgumentException("Fold lacks existence coverage");
			}
		}
		if (completeness == Completeness.COMPLETE) {
			if (coverage.isEmpty() || !coversCanonicalDomain(coverage,
					canonicalDomain)) {
				throw new IllegalArgumentException(
						"Complete existence evidence must cover the canonical domain");
			}
			for (Coverage item : coverage) {
				if (item.state == CoverageState.UNRESOLVED) {
					throw new IllegalArgumentException(
							"Complete existence evidence has no unresolved coverage");
				}
			}
			for (ContinuousComponent component : components) {
				if (component.maximality != Maximality.ESTABLISHED) {
					throw new IllegalArgumentException(
							"Complete components must have established maximality");
				}
			}
			for (LocusPointComponent2D point : pointComponents) {
				if (point.getMaximality() != Maximality.ESTABLISHED) {
					throw new IllegalArgumentException(
							"Complete point components must have established maximality");
				}
			}
		}
	}

	/**
	 * A point enclosure lies in the canonical domain; on a periodic domain the enclosure of
	 * a root at the seam may exceed the included end that represents the seam by one ulp,
	 * because the exact seam is in general not a binary64 value.
	 */
	private void validatePoint(LocusPointComponent2D point) {
		LocusInterval2D enclosure = clippedEnclosure(point.getParameterLower(),
				point.getParameterUpper(), "Point component");
		if (!coveredBy(enclosure, CoverageState.CERTIFIED_ISOLATED_POINTS)) {
			throw new IllegalArgumentException(
					"Point component lacks isolated-point coverage");
		}
	}

	/**
	 * @return the closed enclosure of an isolated parameter, clipped to the canonical
	 *         domain, which on a periodic domain it may exceed by one ulp at the seam
	 */
	private LocusInterval2D clippedEnclosure(double parameterLower,
			double parameterUpper, String name) {
		boolean periodic = topology == DomainTopology.PERIODIC;
		double domainLower = canonicalDomain.getLower();
		double domainUpper = canonicalDomain.getUpper();
		double floor = periodic && canonicalDomain.isLowerClosed()
				? Math.nextDown(domainLower) : domainLower;
		double ceiling = periodic && canonicalDomain.isUpperClosed()
				? Math.nextUp(domainUpper) : domainUpper;
		double lower = Math.max(parameterLower, domainLower);
		double upper = Math.min(parameterUpper, domainUpper);
		if (parameterLower < floor || parameterUpper > ceiling
				|| upper < lower || !canonicalDomain.contains(lower, 0)
				|| !canonicalDomain.contains(upper, 0)) {
			throw new IllegalArgumentException(
					name + " lies outside the canonical domain");
		}
		return new LocusInterval2D(lower, upper, true, true);
	}

	private boolean coveredBy(LocusInterval2D piece, CoverageState state) {
		for (Coverage item : coverage) {
			if (item.state == state && contains(item.interval, piece)) {
				return true;
			}
		}
		return false;
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

	/**
	 * @return an end that the contract of the producer declares; the source is a token, the
	 *         contract itself is cited by the coverage evidence
	 */
	private static LocusBoundary2D providerEnd(String side, boolean included) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
				new LocusBoundarySource2D(PROVIDER_SOURCE, "declared-component-end", side),
				included, LocusEvidenceNature2D.EXACT);
	}

	private static ArrayList<Coverage> completeCoverage(LocusInterval2D domain,
			List<LocusInterval2D> components, String providerContract) {
		ArrayList<Coverage> result = new ArrayList<>();
		double cursor = domain.getLower();
		boolean cursorOwned = domain.isLowerClosed();
		for (LocusInterval2D component : components) {
			addGap(result, cursor, component.getLower(), cursorOwned,
					!component.isLowerClosed(), providerContract);
			addExistence(result, component, providerContract);
			cursor = component.getUpper();
			cursorOwned = !component.isUpperClosed();
		}
		addGap(result, cursor, domain.getUpper(), cursorOwned,
				domain.isUpperClosed(), providerContract);
		return result;
	}

	private static void addExistence(List<Coverage> result,
			LocusInterval2D component, String providerContract) {
		if (component.getLower() == component.getUpper()) {
			result.add(new Coverage(component, CoverageState.CERTIFIED_ISOLATED_POINTS,
					LocusCoverageCause2D.PROVIDER_ISOLATED_POINT, LocusEvidenceNature2D.EXACT,
					providerContract));
			return;
		}
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
						LocusCoverageCause2D.PROVIDER_CONTRACT, LocusEvidenceNature2D.EXACT,
						providerContract));
				return;
			}
		}
		result.add(new Coverage(component, CoverageState.CERTIFIED_EXISTS,
				LocusCoverageCause2D.PROVIDER_CONTRACT, LocusEvidenceNature2D.EXACT,
				providerContract));
	}

	private static void addGap(List<Coverage> result, double lower, double upper,
			boolean lowerClosed, boolean upperClosed, String providerContract) {
		if (lower < upper || lower == upper && lowerClosed && upperClosed) {
			result.add(new Coverage(new LocusInterval2D(lower, upper, lowerClosed,
					upperClosed), CoverageState.CERTIFIED_DOES_NOT_EXIST,
					LocusCoverageCause2D.PROVIDER_COMPLEMENT, LocusEvidenceNature2D.EXACT,
					providerContract));
		}
	}

	private static List<Coverage> immutableCoverage(List<Coverage> input,
			LocusInterval2D domain) {
		ArrayList<Coverage> copy = new ArrayList<>(Objects.requireNonNull(input));
		// At one lower end a closed end precedes an open one: the point [a, a] of a declared
		// gap comes before (a, b] (PRE-G9B-CU2 step T11, restrictedTo).
		copy.sort(Comparator.comparingDouble((Coverage item) -> item.interval.getLower())
				.thenComparing(item -> !item.interval.isLowerClosed()));
		validateIntervals(copy.stream().map(item -> item.interval).toList(), domain,
				false);
		return Collections.unmodifiableList(copy);
	}

	private static List<ContinuousComponent> immutableComponents(
			List<ContinuousComponent> input, LocusInterval2D domain,
			DomainTopology topology) {
		ArrayList<ContinuousComponent> copy = new ArrayList<>(
				Objects.requireNonNull(input));
		copy.sort(Comparator.comparingDouble(item -> item.start));
		ArrayList<LocusInterval2D> pieces = new ArrayList<>();
		for (ContinuousComponent component : copy) {
			if (component.periodicDomain != null && (topology != DomainTopology.PERIODIC
					|| !component.periodicDomain.equals(domain))) {
				throw new IllegalArgumentException(
						"An arc belongs to the periodic canonical domain of its structure");
			}
			pieces.addAll(component.canonicalPieces());
		}
		pieces.sort(Comparator.comparingDouble(LocusInterval2D::getLower));
		validateIntervals(pieces, domain, true);
		return Collections.unmodifiableList(copy);
	}

	private static List<LocusPointComponent2D> immutablePoints(
			List<LocusPointComponent2D> input) {
		ArrayList<LocusPointComponent2D> copy = new ArrayList<>(
				Objects.requireNonNull(input));
		for (LocusPointComponent2D point : copy) {
			Objects.requireNonNull(point);
		}
		copy.sort(Comparator.comparingDouble(LocusPointComponent2D::getParameterLower));
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

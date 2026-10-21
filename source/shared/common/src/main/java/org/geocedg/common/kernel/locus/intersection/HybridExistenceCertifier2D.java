/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;
import org.geocedg.common.kernel.locus.exact.CircleDriverRootStructure2D;
import org.geocedg.common.kernel.locus.exact.ExactBudget1D;
import org.geocedg.common.kernel.locus.exact.ExactBudgetExhaustedException;
import org.geocedg.common.kernel.locus.exact.ExactPolynomial1D;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.locus.exact.RealRootStructure1D;
import org.geocedg.common.kernel.locus.exact.SegmentDriverRootStructure2D;
import org.geocedg.common.kernel.locus.exact.TrigonometricPolynomial2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D.PredicateEnclosure;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D.Target;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Kind;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Unresolved;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D.OriginWrap;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D.Piece;

/**
 * Hybrid existence certifier of a dependent-point locus (contract {@value #CONTRACT};
 * {@code locus-v2-existence-components.md} section 15; {@code
 * locus-v2-certified-construction-model.md} section 10; certification design sections 3
 * and 4). It certifies existence, nonexistence and boundaries of the class-v3 program of
 * one revision over its whole driver domain, and fails closed wherever it cannot prove.
 *
 * <p>Authorities, in the order of {@code locus-v2-existence-components.md} section 7:
 * structural totality decides a program without any box; the exact algebraic certificate
 * decides the roots of a first-level predicate, their multiplicities and the signs between
 * them; outward interval boxes decide the other predicates, a simple root by a sign change
 * with a derivative enclosure that excludes zero, and an extremum by a stationary point
 * isolated with a second-derivative enclosure. A multiple root that no exact certificate
 * decides, an inherited tolerance that restricts definedness, an unlabelled output at
 * coincident roots, a value that is never evidence and an exhausted budget leave a typed
 * {@code UNRESOLVED} band; the edges of a band are never component boundaries that claim
 * maximality.</p>
 *
 * <p>A circle driver satisfies the configuration premise of section 10.8 through its
 * class-v3 program: boxes are taken on the circle, a seam cell covers the binary64 gap of
 * the canonical domain at the seam, and a component may cross the seam. A component of
 * positive length ends at the closed inner bound of the enclosure of its boundary root.
 * Exploratory evidence of the unresolved bands is a sibling of the structure and never
 * certifies anything.</p>
 *
 * <p>A structural metric input contributes the structural certificate of its length
 * ({@code locus-v2-metrics.md} section 26), never its value: where the route is defined, it
 * and the total steps it reaches are defined without a value; where it is certified
 * undefined, the traced point does not exist; elsewhere the cell is an unresolved band.
 * The partition is cut exactly at the ends of the pieces of the structure. An origin wrap
 * is a zero-width event cell: the components on both sides end there with distinct
 * structural ends, which certifies the discontinuity only when the enclosures of the
 * traced point at the two one-sided limits of the length, {@code 0} and the rigorous
 * bounds of the total length, are disjoint; otherwise the event is a band.</p>
 */
public final class HybridExistenceCertifier2D {
	/** Contract of the certificates of this certifier. */
	public static final String CONTRACT = "locus-existence/hybrid/v1";
	/** Method of the exploratory evidence of unresolved bands. */
	public static final String EXPLORATION_METHOD = "uniform-validity-samples-bisection/v1";

	/**
	 * Contract of a proof of nonexistence that uses no selection that the slice copy does not
	 * reproduce (normative precision D-CU2-T11-R1).
	 */
	public static final String SELECTION_INDEPENDENT_CONTRACT =
			"selection-independent-nonexistence/v1";
	private static final String EVIDENCE = "hybrid/v1:";
	private static final int NARROWING_LIMIT = 256;

	private HybridExistenceCertifier2D() {
		// Utility class.
	}

	/**
	 * Versioned constants and budgets of the certifier ({@code
	 * locus-v2-certified-construction-model.md} section 10.9; decision D-CU2-5).
	 */
	public static final class Policy {
		/** Version of the default policy. */
		public static final String VERSION = "hybrid-existence-policy/v1";
		/**
		 * Default policy: brackets of width {@code 2^-40} periods, 4 096 boxes per branch and
		 * revision, minimum depth 6, interior band refinement to depth 8, the default exact
		 * budget (4 096 coefficient bits, 512 bisections per root), 32 exploratory samples
		 * per band with 24 bisections per transition, and fold isolation boxes from
		 * {@code 2^-8} down to {@code 2^-30} periods.
		 */
		public static final Policy DEFAULT = new Policy(VERSION, 40, 4_096, 6, 8,
				ExactBudget1D.DEFAULT, 32, 24, 8, 30);

		private final String version;
		private final int deltaExponent;
		private final int boxBudget;
		private final int minimumDepth;
		private final int bandDepth;
		private final ExactBudget1D exactBudget;
		private final int explorationSamples;
		private final int explorationBisections;
		private final int foldWidestExponent;
		private final int foldNarrowestExponent;

		private Policy(String version, int deltaExponent, int boxBudget, int minimumDepth,
				int bandDepth, ExactBudget1D exactBudget, int explorationSamples,
				int explorationBisections, int foldWidestExponent,
				int foldNarrowestExponent) {
			if (deltaExponent < 1 || boxBudget < 1 || minimumDepth < 0
					|| bandDepth < minimumDepth || explorationSamples < 2
					|| explorationBisections < 0 || foldWidestExponent < 1
					|| foldNarrowestExponent < foldWidestExponent) {
				throw new IllegalArgumentException("Invalid hybrid certification policy");
			}
			this.version = version;
			this.deltaExponent = deltaExponent;
			this.boxBudget = boxBudget;
			this.minimumDepth = minimumDepth;
			this.bandDepth = bandDepth;
			this.exactBudget = Objects.requireNonNull(exactBudget);
			this.explorationSamples = explorationSamples;
			this.explorationBisections = explorationBisections;
			this.foldWidestExponent = foldWidestExponent;
			this.foldNarrowestExponent = foldNarrowestExponent;
		}

		/**
		 * @param budget existence boxes per branch and revision
		 * @return a test policy with another box budget and its own version
		 */
		public Policy withBoxBudget(int budget) {
			return new Policy(version + "+boxes=" + budget, deltaExponent, budget,
					minimumDepth, bandDepth, exactBudget, explorationSamples,
					explorationBisections, foldWidestExponent, foldNarrowestExponent);
		}

		/**
		 * @param budget exact budget
		 * @return a test policy with another exact budget and its own version
		 */
		public Policy withExactBudget(ExactBudget1D budget) {
			return new Policy(version + "+exact=" + budget.maximumCoefficientBits() + "/"
					+ budget.maximumBisectionsPerRoot(), deltaExponent, boxBudget,
					minimumDepth, bandDepth, budget, explorationSamples,
					explorationBisections, foldWidestExponent, foldNarrowestExponent);
		}

		/**
		 * @param samples exploratory samples per band
		 * @param bisections bisections per explored transition
		 * @return a policy that differs only in its exploration, with its own version
		 */
		public Policy withExploration(int samples, int bisections) {
			return new Policy(version + "+exploration=" + samples + "/" + bisections,
					deltaExponent, boxBudget, minimumDepth, bandDepth, exactBudget, samples,
					bisections, foldWidestExponent, foldNarrowestExponent);
		}

		/** @return exponent {@code k} of the bracket width {@code 2^-k} periods */
		public int getDeltaExponent() {
			return deltaExponent;
		}

		/** @return existence boxes per branch and revision */
		public int getBoxBudget() {
			return boxBudget;
		}

		/** @return depth of the initial dyadic partition */
		public int getMinimumDepth() {
			return minimumDepth;
		}

		/** @return depth below which the interior of a band is not refined */
		public int getBandDepth() {
			return bandDepth;
		}

		/** @return budget of the exact algebraic certificate */
		public ExactBudget1D getExactBudget() {
			return exactBudget;
		}

		/** @return canonical text of the policy */
		public String getSignature() {
			return version + "|delta=2^-" + deltaExponent + "|boxes=" + boxBudget
					+ "|depth=" + minimumDepth + "|band-depth=" + bandDepth + "|exact-bits="
					+ exactBudget.maximumCoefficientBits() + "|exact-bisections="
					+ exactBudget.maximumBisectionsPerRoot() + "|exploration="
					+ explorationSamples + "/" + explorationBisections + "|fold-boxes=2^-"
					+ foldWidestExponent + "..2^-" + foldNarrowestExponent;
		}
	}

	/**
	 * A certified stationary point of the traced map ({@code
	 * locus-v2-certified-construction-model.md} section 10.7; certification design section
	 * 5). Its existence comes from an exact structural symmetry {@code sigma} with
	 * {@code F o sigma = F} at a fixed point {@code t0} of {@code sigma} interior to a
	 * certified component. A fold adds isolation and non-degeneracy: on an isolation box
	 * around {@code t0} one component of {@code F''} excludes zero, so {@code t0} is the only
	 * stationary point there and the one-sided tangents are the opposite directions of
	 * {@code F''(t0)}. A degenerate stationary point has no such box: its existence is
	 * proven, never a non-degenerate fold.
	 */
	public static final class CertifiedStationaryPoint {
		/** Kind of the stationary point. */
		public enum StationaryKind {
			/** A non-degenerate fold: the curve turns back along the same arc. */
			FOLD,
			/** A stationary point proven by the symmetry whose non-degeneracy is not. */
			DEGENERATE
		}

		private final StationaryKind kind;
		private final String mirror;
		private final double parameterLower;
		private final double parameterUpper;
		private final double isolationLower;
		private final double isolationUpper;
		private final int axis;
		private final double[] secondDerivative;

		CertifiedStationaryPoint(StationaryKind kind, String mirror, double parameterLower,
				double parameterUpper, double isolationLower, double isolationUpper, int axis,
				double[] secondDerivative) {
			this.kind = kind;
			this.mirror = mirror;
			this.parameterLower = parameterLower;
			this.parameterUpper = parameterUpper;
			this.isolationLower = isolationLower;
			this.isolationUpper = isolationUpper;
			this.axis = axis;
			this.secondDerivative = secondDerivative;
		}

		/** @return kind of the stationary point */
		public StationaryKind getKind() {
			return kind;
		}

		/** @return verified mirror of the symmetry, as named by its operation table */
		public String getSymmetry() {
			return ConstructionSymmetry2D.VERSION + ":" + mirror;
		}

		/** @return lower bound of the canonical enclosure of {@code t0} */
		public double getParameterLower() {
			return parameterLower;
		}

		/** @return upper bound of the canonical enclosure of {@code t0} */
		public double getParameterUpper() {
			return parameterUpper;
		}

		/**
		 * @return lower end of the isolation box, the band where a turn of the curve is
		 *         certified; {@code NaN} for a degenerate point
		 */
		public double getIsolationLower() {
			return isolationLower;
		}

		/** @return upper end of the isolation box; {@code NaN} for a degenerate point */
		public double getIsolationUpper() {
			return isolationUpper;
		}

		/** @return 0 or 1 for the component of {@code F''} that excludes zero; -1 if none */
		public int getAxis() {
			return axis;
		}

		/**
		 * @return {@code {x''Lower, x''Upper, y''Lower, y''Upper}} on the isolation box, the
		 *         directions of the one-sided tangents; {@code null} for a degenerate point
		 */
		public double[] getSecondDerivative() {
			return secondDerivative == null ? null : secondDerivative.clone();
		}

		/**
		 * @return {@link LocusEvidenceNature2D#EXACT} for the existence of a degenerate
		 *         point; {@link LocusEvidenceNature2D#RIGOROUS_INTERVAL} for a fold, whose
		 *         isolation rests on interval enclosures
		 */
		public LocusEvidenceNature2D getNature() {
			return kind == StationaryKind.FOLD ? LocusEvidenceNature2D.RIGOROUS_INTERVAL
					: LocusEvidenceNature2D.EXACT;
		}

		/** @return canonical text of the stationary point */
		public String getSignature() {
			StringBuilder signature = new StringBuilder(kind.name()).append('[')
					.append(Double.toHexString(parameterLower)).append(',')
					.append(Double.toHexString(parameterUpper)).append("]:").append(getSymmetry());
			if (kind == StationaryKind.FOLD) {
				signature.append(":isolation[").append(Double.toHexString(isolationLower))
						.append(',').append(Double.toHexString(isolationUpper)).append("]:axis=")
						.append(axis);
				for (double bound : secondDerivative) {
					signature.append(',').append(Double.toHexString(bound));
				}
			}
			return signature.toString();
		}

		@Override
		public String toString() {
			return getSignature();
		}
	}

	/** Pointwise validity of the evaluator, the only input of exploratory evidence. */
	public interface Sampler {
		/**
		 * @param canonicalParameter canonical driver parameter
		 * @return whether the evaluator returns a valid point there
		 */
		boolean isValid(double canonicalParameter);
	}

	/**
	 * One certified root of a validity predicate: its kind, the certified enclosure of its
	 * parameter, its structural source and the nature of its proof.
	 */
	public static final class CertifiedRoot {
		/** Kind of the root. */
		public enum RootKind {
			/** Sign change of a strict-inequality predicate: a component boundary. */
			TRANSITION,
			/** Even root with a nonnegative predicate on both sides: interior. */
			SINGULAR_INTERIOR,
			/** Even root with a nonpositive predicate on both sides: a point component. */
			ISOLATED_POINT,
			/**
			 * Root of an equality predicate: existence fails at it, and the inherited
			 * tolerance around it leaves an unresolved band.
			 */
			OPEN_EVENT
		}

		private final RootKind kind;
		private final double lower;
		private final double upper;
		private final LocusBoundarySource2D source;
		private final boolean included;
		private final int multiplicity;
		private final LocusEvidenceNature2D nature;
		private final boolean insideBand;

		CertifiedRoot(RootKind kind, double lower, double upper, LocusBoundarySource2D source,
				boolean included, int multiplicity, LocusEvidenceNature2D nature,
				boolean insideBand) {
			this.kind = kind;
			this.lower = lower;
			this.upper = upper;
			this.source = source;
			this.included = included;
			this.multiplicity = multiplicity;
			this.nature = nature;
			this.insideBand = insideBand;
		}

		/** @return kind of the root */
		public RootKind getKind() {
			return kind;
		}

		/** @return lower bound of the enclosure of the root parameter */
		public double getLower() {
			return lower;
		}

		/** @return upper bound of the enclosure of the root parameter */
		public double getUpper() {
			return upper;
		}

		/** @return structural source of the root */
		public LocusBoundarySource2D getSource() {
			return source;
		}

		/** @return whether the exact root belongs to the existence set */
		public boolean isIncluded() {
			return included;
		}

		/** @return multiplicity of the root; 1 for a simple root certified by intervals */
		public int getMultiplicity() {
			return multiplicity;
		}

		/** @return nature of the proof of the root */
		public LocusEvidenceNature2D getNature() {
			return nature;
		}

		/** @return whether the root lies inside an unresolved band */
		public boolean isInsideBand() {
			return insideBand;
		}

		/** @return canonical text of the root */
		public String getSignature() {
			return kind + "[" + Double.toHexString(lower) + "," + Double.toHexString(upper)
					+ "]:" + source.getSignature() + ":" + (included ? "closed" : "open")
					+ ":multiplicity=" + multiplicity + ":" + nature
					+ (insideBand ? ":band" : "");
		}

		@Override
		public String toString() {
			return getSignature();
		}
	}

	/**
	 * Evidence of one exact algebraic certificate ({@code
	 * locus-v2-certified-construction-model.md} section 10.6): method version, coefficient
	 * fingerprint, degree, maximal coefficient bits, width bound, bisections and result.
	 */
	public static final class ExactEvidence {
		private final String target;
		private final String status;
		private final String coefficientFingerprint;
		private final int degree;
		private final int maximumCoefficientBits;
		private final String widthBound;
		private final int bisections;
		private final int maximumRootBisections;
		private final String roots;

		ExactEvidence(String target, String status, String coefficientFingerprint,
				int degree, int maximumCoefficientBits, String widthBound, int bisections,
				int maximumRootBisections, String roots) {
			this.target = target;
			this.status = status;
			this.coefficientFingerprint = coefficientFingerprint;
			this.degree = degree;
			this.maximumCoefficientBits = maximumCoefficientBits;
			this.widthBound = widthBound;
			this.bisections = bisections;
			this.maximumRootBisections = maximumRootBisections;
			this.roots = roots;
		}

		/** @return address of the classified predicate */
		public String getTarget() {
			return target;
		}

		/** @return {@code CLASSIFIED}, {@code IDENTICALLY_ZERO} or {@code BUDGET_EXHAUSTED} */
		public String getStatus() {
			return status;
		}

		/** @return SHA-256 of the canonical text of the exact predicate */
		public String getCoefficientFingerprint() {
			return coefficientFingerprint;
		}

		/** @return degree of the classified polynomial */
		public int getDegree() {
			return degree;
		}

		/** @return largest coefficient bit length built by the classification */
		public int getMaximumCoefficientBits() {
			return maximumCoefficientBits;
		}

		/** @return number of exact bisections */
		public int getBisections() {
			return bisections;
		}

		/** @return largest number of bisections on the path of one root */
		public int getMaximumRootBisections() {
			return maximumRootBisections;
		}

		/** @return canonical text of the evidence */
		public String getSignature() {
			return RealRootStructure1D.METHOD_VERSION + "|" + target + "|" + status
					+ "|coefficients=" + coefficientFingerprint + "|degree=" + degree
					+ "|bits=" + maximumCoefficientBits + "|width=" + widthBound
					+ "|bisections=" + bisections + "/" + maximumRootBisections + "|roots="
					+ roots;
		}

		@Override
		public String toString() {
			return getSignature();
		}
	}

	/** Certificate of one branch and revision, with its evidence and work. */
	public static final class Result {
		private final LocusExistenceStructure2D structure;
		private final LocusExploratoryEvidence2D exploration;
		private final List<CertifiedRoot> roots;
		private final List<ExactEvidence> exactEvidence;
		private final List<CertifiedStationaryPoint> stationaryPoints;
		private final long boxes;
		private final int transitions;
		private final int bands;
		private final String policySignature;
		private final String diagnostic;

		Result(LocusExistenceStructure2D structure, LocusExploratoryEvidence2D exploration,
				List<CertifiedRoot> roots, List<ExactEvidence> exactEvidence,
				List<CertifiedStationaryPoint> stationaryPoints, long boxes, int transitions,
				int bands, String policySignature, String diagnostic) {
			this.structure = structure;
			this.exploration = exploration;
			this.roots = Collections.unmodifiableList(new ArrayList<>(roots));
			this.exactEvidence = Collections.unmodifiableList(new ArrayList<>(exactEvidence));
			this.stationaryPoints = Collections.unmodifiableList(
					new ArrayList<>(stationaryPoints));
			this.boxes = boxes;
			this.transitions = transitions;
			this.bands = bands;
			this.policySignature = policySignature;
			this.diagnostic = diagnostic;
		}

		/** @return existence structure, bound to no revision until published */
		public LocusExistenceStructure2D getStructure() {
			return structure;
		}

		/** @return exploratory evidence of the unresolved bands, or {@code null} */
		public LocusExploratoryEvidence2D getExploration() {
			return exploration;
		}

		/** @return every certified root, in parameter order */
		public List<CertifiedRoot> getRoots() {
			return roots;
		}

		/** @return evidence of every exact certificate computed */
		public List<ExactEvidence> getExactEvidence() {
			return exactEvidence;
		}

		/** @return certified stationary points of the traced map, in parameter order */
		public List<CertifiedStationaryPoint> getStationaryPoints() {
			return stationaryPoints;
		}

		/** @return existence boxes evaluated, point boxes included */
		public long getExistenceBoxes() {
			return boxes;
		}

		/** @return certified transitions published as component boundaries */
		public int getCertifiedTransitions() {
			return transitions;
		}

		/** @return unresolved bands published in the coverage */
		public int getUnresolvedBands() {
			return bands;
		}

		/** @return signature of the policy that produced the certificate */
		public String getPolicySignature() {
			return policySignature;
		}

		/** @return diagnostic of a structural failure of the certifier, or {@code null} */
		public String getDiagnostic() {
			return diagnostic;
		}

		/**
		 * The same certificate restricted to the components that the generator declares for
		 * its driver domain ({@link LocusExistenceStructure2D#restrictedTo}); the exploratory
		 * evidence keeps only what lies inside them, and the computation evidence is kept.
		 *
		 * @param declared disjoint declared components of the driver domain
		 * @param declarationEvidence evidence of the declaration
		 * @return the restricted certificate
		 */
		public Result restrictedTo(List<LocusInterval2D> declared,
				String declarationEvidence) {
			return new Result(structure.restrictedTo(declared, declarationEvidence),
					exploration == null ? null : exploration.restrictedTo(declared), roots,
					exactEvidence, stationaryPoints, boxes, transitions, bands,
					policySignature, diagnostic);
		}
	}

	/**
	 * Certifies the program of one revision.
	 *
	 * @param model box model of the class-v3 program of the revision
	 * @param domain canonical driver domain of the branch
	 * @param periodic whether the domain is periodic
	 * @param branchKey branch key of the structure
	 * @param sampler pointwise validity for exploratory evidence, or {@code null}
	 * @param policy versioned constants and budgets
	 * @param instrumentation counters of the locus, or {@code null}
	 * @return certificate with its evidence
	 */
	public static Result certify(ConstructionBoxModel2D model, LocusInterval2D domain,
			boolean periodic, String branchKey, Sampler sampler, Policy policy,
			LocusInstrumentation2D instrumentation) {
		return certify(model, Map.of(), domain, periodic, branchKey, sampler, policy,
				instrumentation);
	}

	/**
	 * Certifies the program of one revision whose metric inputs have structural
	 * certificates.
	 *
	 * @param model box model of the class-v3 program of the revision
	 * @param metrics structural certificate of each metric input node that has one; the
	 *        other metric inputs stay undecided
	 * @param domain canonical driver domain of the branch
	 * @param periodic whether the domain is periodic
	 * @param branchKey branch key of the structure
	 * @param sampler pointwise validity for exploratory evidence, or {@code null}
	 * @param policy versioned constants and budgets
	 * @param instrumentation counters of the locus, or {@code null}
	 * @return certificate with its evidence
	 */
	public static Result certify(ConstructionBoxModel2D model,
			Map<Integer, LocusMetricFunctionStructure2D> metrics, LocusInterval2D domain,
			boolean periodic, String branchKey, Sampler sampler, Policy policy,
			LocusInstrumentation2D instrumentation) {
		return certify(model, metrics, List.of(), domain, periodic, branchKey, sampler,
				policy, instrumentation);
	}

	/**
	 * Certifies the program of one revision whose steps have durable operation identities,
	 * which name the structural sources of its boundaries ({@code
	 * locus-v2-existence-components.md} section 15.6).
	 *
	 * @param operations durable identity token of each step in program order; a step without
	 *        one keeps its revision-local program address
	 * @return certificate with its evidence
	 */
	public static Result certify(ConstructionBoxModel2D model,
			Map<Integer, LocusMetricFunctionStructure2D> metrics, List<String> operations,
			LocusInterval2D domain, boolean periodic, String branchKey, Sampler sampler,
			Policy policy, LocusInstrumentation2D instrumentation) {
		return new Run(Objects.requireNonNull(model), Objects.requireNonNull(metrics),
				Objects.requireNonNull(operations), Objects.requireNonNull(domain), periodic,
				branchKey, sampler, Objects.requireNonNull(policy), instrumentation).certify();
	}

	/**
	 * Certificate of a revision without a class-v3 program: nothing is certified, and the
	 * whole domain is one unresolved band with exploratory evidence.
	 *
	 * @param cause typed cause of the unresolved coverage
	 * @param reason evidence of the refusal
	 * @return certificate with its evidence
	 */
	public static Result notEstablished(LocusInterval2D domain, boolean periodic,
			String branchKey, LocusCoverageCause2D cause, String reason, Sampler sampler,
			Policy policy, LocusInstrumentation2D instrumentation) {
		DomainTopology topology = LocusExistenceStructure2D.topologyOf(domain, periodic);
		LocusExistenceStructure2D structure = LocusExistenceStructure2D.notEstablished(
				branchKey, domain, topology, cause, EVIDENCE + reason);
		structure = new LocusExistenceStructure2D(branchKey, domain, topology, 0,
				structure.getCoverage(), List.of(), List.of(), Completeness.NOT_ESTABLISHED,
				CONTRACT);
		record(instrumentation, LocusCertificationWork2D.UNRESOLVED_BAND);
		LocusExploratoryEvidence2D exploration = explore(branchKey,
				Collections.singletonList(domain), sampler, policy, instrumentation);
		return new Result(structure, exploration, List.of(), List.of(), List.of(), 0, 0, 1,
				policy.getSignature(), null);
	}

	/**
	 * Certificate of a particular case of the contract computed by its own certifier
	 * (decision D-CU2-9, option A: the selected SplineV2 root of {@code
	 * SelectedRootContinuousDomainCertifier2D}): its structure is published as it is, with
	 * the exploratory evidence of its unresolved bands.
	 *
	 * @param structure structure of the particular certifier
	 * @param sampler pointwise validity for exploratory evidence, or {@code null}
	 * @param policy versioned constants and budgets of the exploration
	 * @param instrumentation counters of the locus, or {@code null}
	 * @return certificate with its evidence
	 */
	public static Result particular(LocusExistenceStructure2D structure, Sampler sampler,
			Policy policy, LocusInstrumentation2D instrumentation) {
		List<LocusInterval2D> bands = new ArrayList<>();
		for (Coverage item : structure.getCoverage()) {
			if (item.getState() == CoverageState.UNRESOLVED) {
				bands.add(item.getInterval());
				record(instrumentation, LocusCertificationWork2D.UNRESOLVED_BAND);
			}
		}
		LocusExploratoryEvidence2D exploration = explore(structure.getBranchKey(), bands,
				sampler, policy, instrumentation);
		return new Result(structure, exploration, List.of(), List.of(), List.of(), 0, 0,
				bands.size(), policy.getSignature(), null);
	}

	/**
	 * Proves that a locus has no point on its whole canonical domain without using the
	 * selection of the intersections that the slice copy does not reproduce
	 * ({@value #SELECTION_INDEPENDENT_CONTRACT}; normative precision D-CU2-T11-R1 of
	 * D-CU2-17 and D-CU2-20-R). Every box of a dyadic cover of the domain, from the minimum
	 * depth of the policy down to its resolution and within its box budget, must be uniformly
	 * invalid by a predicate of a step that does not depend on a selected output of those
	 * intersections, or by the discriminant of one of them: without raw roots no permutation
	 * and no compaction creates an output, and every step of the program precedes the traced
	 * point, so the traced point does not exist. A valid box, an undecided box, a box decided
	 * by any other predicate or an exhausted budget proves nothing, and no partial result is
	 * returned.
	 *
	 * @param model box model of the program captured from the copy as it is
	 * @param unreproduced nodes of the line-circle steps whose selection is not reproduced
	 * @param operations durable identity token of each step, which the evidence names
	 * @param domain canonical driver domain of the branch
	 * @param periodic whether the domain is periodic
	 * @param branchKey branch key of the structure
	 * @param policy versioned constants and budgets
	 * @param instrumentation counters of the locus, or {@code null}
	 * @return the complete certificate with no component, or empty without a proof
	 */
	public static Optional<Result> selectionIndependentNonexistence(
			ConstructionBoxModel2D model, Set<Integer> unreproduced, List<String> operations,
			LocusInterval2D domain, boolean periodic, String branchKey, Policy policy,
			LocusInstrumentation2D instrumentation) {
		List<CertifiedConstructionProgram2D.Step> steps = model.getProgram().getSteps();
		// An indexed output of an unreproduced intersection is its selected root: only its
		// discriminant, which concerns no root, is independent of the selection. A step that
		// reads such an output, or reads a step that does, transitively, depends on it.
		boolean[] selectedOutput = new boolean[steps.size() + 1];
		boolean[] selectionDependent = new boolean[steps.size() + 1];
		for (int node = 1; node <= steps.size(); node++) {
			CertifiedConstructionProgram2D.Step step = steps.get(node - 1);
			for (CertifiedConstructionProgram2D.Operand operand : step.getOperands()) {
				int source = operand.getKind() == CertifiedConstructionProgram2D.OperandKind.NODE
						? operand.getNode() : 0;
				if (source <= 0) {
					continue;
				}
				if (unreproduced.contains(source)) {
					if (step.getOperation()
							== CertifiedConstructionProgram2D.Operation.INTERSECTION_OUTPUT) {
						selectedOutput[node] = true;
					} else {
						selectionDependent[node] = true;
					}
				}
				if (selectedOutput[source] || selectionDependent[source]) {
					selectionDependent[node] = true;
				}
			}
		}
		double width = domain.getUpper() - domain.getLower();
		double resolution = width * Math.scalb(1.0, -policy.deltaExponent);
		int initial = 1 << policy.minimumDepth;
		List<double[]> pending = new ArrayList<>();
		for (int index = initial - 1; index >= 0; index--) {
			pending.add(new double[] {domain.getLower() + width * index / initial,
					index + 1 == initial ? domain.getUpper()
							: domain.getLower() + width * (index + 1) / initial});
		}
		Set<String> deciding = new TreeSet<>();
		long boxes = 0;
		while (!pending.isEmpty()) {
			double[] box = pending.remove(pending.size() - 1);
			if (++boxes > policy.boxBudget) {
				return Optional.empty();
			}
			record(instrumentation, LocusCertificationWork2D.EXISTENCE_BOX);
			ConstructionBoxOutcome2D outcome = model.evaluate(box[0], box[1]);
			if (outcome.getKind() == Kind.VALID) {
				return Optional.empty();
			}
			int step = outcome.getStep();
			if (outcome.getKind() == Kind.UNIFORMLY_INVALID && step >= 1
					&& step <= steps.size() && !selectionDependent[step]
					&& (!unreproduced.contains(step) && !selectedOutput[step]
							|| outcome.getPredicate()
									== ConstructionValidityPredicate2D.DISCRIMINANT)) {
				deciding.add(outcome.getInvalidReason() + "@" + operation(operations, step));
				continue;
			}
			double middle = box[0] / 2 + box[1] / 2;
			if (!(box[1] - box[0] > resolution) || !(middle > box[0] && middle < box[1])) {
				return Optional.empty();
			}
			pending.add(new double[] {middle, box[1]});
			pending.add(new double[] {box[0], middle});
		}
		StringBuilder evidence = new StringBuilder(EVIDENCE)
				.append(SELECTION_INDEPENDENT_CONTRACT).append(":deciding=")
				.append(String.join(",", deciding)).append(";unreproduced=");
		String separator = "";
		for (int node : unreproduced) {
			evidence.append(separator).append(operation(operations, node));
			separator = ",";
		}
		DomainTopology topology = LocusExistenceStructure2D.topologyOf(domain, periodic);
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(branchKey, domain,
				topology, 0, List.of(new Coverage(domain, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.SELECTION_INDEPENDENT_NONEXISTENCE,
						LocusEvidenceNature2D.RIGOROUS_INTERVAL, evidence.toString())),
				List.of(), List.of(), Completeness.COMPLETE, CONTRACT);
		return Optional.of(new Result(structure, null, List.of(), List.of(), List.of(), boxes,
				0, 0, policy.getSignature(), null));
	}

	private static String operation(List<String> operations, int node) {
		return node >= 1 && node <= operations.size() ? operations.get(node - 1)
				: "class-v3-step-" + node;
	}

	/**
	 * Encloses the image of a certified root of a validity predicate with the limit form of the
	 * certificate (certification design section 3.4; consumption design section 5.2): the
	 * program is evaluated on the certified enclosure of the root assuming its predicate, so
	 * that a discriminant enters through its clamped square root and the double root of a
	 * tangency, {@code lambda = -b'/a}, never takes the square root of a negative rounding. Only
	 * a predicate whose root belongs to its component has a limit form; a vanishing divisor,
	 * direction or radius has none, because the limit may not exist.
	 *
	 * @param model box model of the class-v3 program of the revision
	 * @param operations durable identity token of each step, as the certificate named the
	 *        sources of its roots
	 * @param source structural source of the root
	 * @param lower lower bound of the certified enclosure of the root parameter
	 * @param upper upper bound of the certified enclosure of the root parameter
	 * @param periodic whether the domain is periodic
	 * @return {@code {xLower, xUpper, yLower, yUpper}}, an outward enclosure of the image of
	 *         every parameter of the enclosure where the predicate holds, or {@code null} when
	 *         the limit form does not enclose it
	 */
	public static double[] encloseRootImage(ConstructionBoxModel2D model,
			List<String> operations, LocusBoundarySource2D source, double lower, double upper,
			boolean periodic) {
		Target target = target(model, operations, source);
		if (target == null || target.predicate.isEquality()
				|| !target.predicate.isClosedAtRoot() || !(lower <= upper)) {
			return null;
		}
		ConstructionBoxOutcome2D outcome = model.evaluateAssuming(
				new SplineOutwardInterval2D(lower, upper), target, periodic);
		if (outcome.kind != Kind.VALID || outcome.value == null) {
			return null;
		}
		return new double[] {outcome.value[0].lower, outcome.value[0].upper,
			outcome.value[1].lower, outcome.value[1].upper};
	}

	/**
	 * @return the predicate that a structural source names ({@link #predicateToken}), or
	 *         {@code null} when the source names no root of the program
	 */
	private static Target target(ConstructionBoxModel2D model, List<String> operations,
			LocusBoundarySource2D source) {
		int node = operations.indexOf(source.getOrigin()) + 1;
		String token = source.getPredicate();
		if (node < 1 || node > model.getProgram().getSteps().size()
				|| token.endsWith("-coincidence-band")) {
			return null;
		}
		int member = token.indexOf("-member-");
		int index = ConstructionBoxModel2D.STEP_PREDICATE;
		String name = token;
		if (member > 0) {
			try {
				index = Integer.parseInt(token.substring(member + "-member-".length()));
			} catch (NumberFormatException exception) {
				return null;
			}
			name = token.substring(0, member);
		}
		try {
			return new Target(node, ConstructionValidityPredicate2D.valueOf(name), index);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private static void record(LocusInstrumentation2D instrumentation,
			LocusCertificationWork2D work) {
		if (instrumentation != null) {
			instrumentation.recordCertificationWork(work);
		}
	}

	/** Uniform validity samples of each band, with bisection of sampled transitions. */
	private static LocusExploratoryEvidence2D explore(String branchKey,
			List<LocusInterval2D> bands, Sampler sampler, Policy policy,
			LocusInstrumentation2D instrumentation) {
		if (sampler == null || bands.isEmpty()) {
			return null;
		}
		List<LocusInterval2D> runs = new ArrayList<>();
		List<LocusInterval2D> transitions = new ArrayList<>();
		long work = 0;
		for (LocusInterval2D band : bands) {
			record(instrumentation, LocusCertificationWork2D.EXPLORATORY_TRAVERSAL);
			int count = policy.explorationSamples;
			double[] samples = new double[count];
			boolean[] valid = new boolean[count];
			for (int index = 0; index < count; index++) {
				double fraction = (index + 0.5) / count;
				samples[index] = band.getLower()
						+ (band.getUpper() - band.getLower()) * fraction;
				valid[index] = sampler.isValid(samples[index]);
				work++;
			}
			for (int index = 1; index < count; index++) {
				double left = samples[index - 1];
				double right = samples[index];
				if (!(left < right)) {
					continue;
				}
				if (valid[index - 1] && valid[index]) {
					runs.add(new LocusInterval2D(left, right, true, true));
				} else if (valid[index - 1] != valid[index]) {
					boolean leftValid = valid[index - 1];
					for (int step = 0; step < policy.explorationBisections; step++) {
						double middle = left + (right - left) / 2;
						if (!(left < middle && middle < right)) {
							break;
						}
						work++;
						if (sampler.isValid(middle) == leftValid) {
							left = middle;
						} else {
							right = middle;
						}
					}
					transitions.add(new LocusInterval2D(left, right, true, true));
				}
			}
		}
		return new LocusExploratoryEvidence2D(branchKey, 0, EXPLORATION_METHOD,
				policy.getSignature(), work, runs, transitions, List.of());
	}

	/** State of one cell of the partition. */
	private enum State {
		/** Not evaluated yet. */
		PENDING,
		/** Every validity predicate certified true. */
		VALID,
		/** A validity predicate certified false. */
		INVALID,
		/** Undecided, still to resolve. */
		UNRESOLVED,
		/** Undecided, final: a cell of an unresolved band. */
		BAND,
		/** The enclosure of one certified transition root. */
		BOUNDARY,
		/** The enclosure of one isolated root at which existence holds. */
		ISOLATED,
		/**
		 * A certified structural event of a metric input: a zero-width cell at its exact
		 * parameter, or the seam cell for an event at the start of the circle.
		 */
		EVENT
	}

	/** A certified root of one predicate, placed in the cell coordinates. */
	private static final class Root {
		private final Target target;
		/**
		 * Root among the roots of its predicate: {@code root-k} for the {@code k}-th root of
		 * an exact certificate in canonical order, {@code root} for a simple root certified
		 * by intervals. A revision-local address; structural lineage is step T8.
		 */
		private final String address;
		private final CertifiedRoot.RootKind kind;
		private final int multiplicity;
		private final LocusEvidenceNature2D nature;
		private final double lower;
		private final double upper;
		private final double canonicalLower;
		private final double canonicalUpper;

		Root(Target target, String address, CertifiedRoot.RootKind kind, int multiplicity,
				LocusEvidenceNature2D nature, double lower, double upper,
				double canonicalLower, double canonicalUpper) {
			this.target = target;
			this.address = address;
			this.kind = kind;
			this.multiplicity = multiplicity;
			this.nature = nature;
			this.lower = lower;
			this.upper = upper;
			this.canonicalLower = canonicalLower;
			this.canonicalUpper = canonicalUpper;
		}
	}

	/** One part of the exact sign structure of a predicate: a gap with a sign, or a root. */
	private static final class Segment {
		private final double lower;
		private final double upper;
		private final int sign;
		private final Root root;

		Segment(double lower, double upper, int sign, Root root) {
			this.lower = lower;
			this.upper = upper;
			this.sign = sign;
			this.root = root;
		}
	}

	/** Exact sign structure of one first-level predicate over the cell coordinates. */
	private static final class Exact {
		private final boolean identicallyZero;
		private final List<Segment> segments;

		Exact(boolean identicallyZero, List<Segment> segments) {
			this.identicallyZero = identicallyZero;
			this.segments = segments;
		}
	}

	/** One cell of the partition of the driver domain. */
	private static final class Cell {
		private double lower;
		private double upper;
		private State state = State.PENDING;
		private ConstructionBoxOutcome2D outcome;
		private final List<Target> assumed;
		private final Set<Target> exactApplied;
		private Root root;
		private LocusCoverageCause2D cause;
		private LocusEvidenceNature2D nature = LocusEvidenceNature2D.NONE;
		private String evidence = "";
		private double[] position;
		/** Metric inputs that their structures define on the cell. */
		private Set<Integer> metricDefined = Set.of();
		/** Whether the structure of a metric input excludes the route on the cell. */
		private boolean metricUndefined;
		/** Cause of a metric input that its structure does not decide, or {@code null}. */
		private LocusCoverageCause2D metricCause;
		private String metricEvidence;
		/** Whether the cell is a structural event of a metric input. */
		private boolean event;
		private double eventParameter;
		/** End of the component below an event, or {@code null}. */
		private LocusBoundary2D eventBelow;
		/** End of the component above an event, or {@code null}. */
		private LocusBoundary2D eventAbove;

		Cell(double lower, double upper, List<Target> assumed, Set<Target> exactApplied) {
			this.lower = lower;
			this.upper = upper;
			this.assumed = new ArrayList<>(assumed);
			this.exactApplied = new HashSet<>(exactApplied);
		}

		Cell child(double from, double to) {
			Cell child = new Cell(from, to, assumed, exactApplied);
			child.copyMetric(this);
			return child;
		}

		/** Sub-cells lie inside the same pieces of every metric structure. */
		void copyMetric(Cell other) {
			metricDefined = other.metricDefined;
			metricUndefined = other.metricUndefined;
			metricCause = other.metricCause;
			metricEvidence = other.metricEvidence;
		}

		boolean sameMetric(Cell other) {
			return metricDefined.equals(other.metricDefined)
					&& metricUndefined == other.metricUndefined
					&& metricCause == other.metricCause;
		}

		boolean isFinal() {
			return state != State.PENDING && state != State.UNRESOLVED;
		}
	}

	/** One certification of one revision. */
	private static final class Run {
		private final ConstructionBoxModel2D model;
		private final CertifiedConstructionProgram2D program;
		private final Map<Integer, LocusMetricFunctionStructure2D> metrics;
		private final List<String> operations;
		private final List<Integer> metricNodes;
		private final Set<Double> eventParameters = new TreeSet<>();
		private final LocusInterval2D domain;
		private final boolean circle;
		private final String branchKey;
		private final Sampler sampler;
		private final Policy policy;
		private final LocusInstrumentation2D instrumentation;
		private final double period;
		private final double delta;
		private final double bandWidth;
		private final double seamUpper;
		private final List<Cell> cells = new ArrayList<>();
		private final Map<Target, Exact> exact = new LinkedHashMap<>();
		private final List<ExactEvidence> exactEvidence = new ArrayList<>();
		private int transitions;
		private long boxes;
		private boolean exhausted;
		private String diagnostic;

		Run(ConstructionBoxModel2D model, Map<Integer, LocusMetricFunctionStructure2D> metrics,
				List<String> operations, LocusInterval2D domain, boolean periodic,
				String branchKey, Sampler sampler, Policy policy,
				LocusInstrumentation2D instrumentation) {
			this.model = model;
			this.program = model.getProgram();
			this.metrics = metrics;
			this.operations = operations;
			this.metricNodes = model.metricInputs();
			this.domain = domain;
			this.branchKey = branchKey;
			this.sampler = sampler;
			this.policy = policy;
			this.instrumentation = instrumentation;
			// The configuration premise (section 10.8) holds for every class-v3 program of a
			// circle driver on the half-open canonical circle domain.
			circle = periodic && program.getDriver() == Driver.CIRCLE
					&& domain.getLower() == -Math.PI && domain.getUpper() == Math.PI
					&& domain.isLowerClosed() && !domain.isUpperClosed();
			period = domain.getUpper() - domain.getLower();
			delta = Math.scalb(period, -policy.deltaExponent);
			bandWidth = Math.scalb(period, -policy.bandDepth);
			seamUpper = Math.nextUp(Math.PI);
		}

		Result certify() {
			if (model.isStructurallyTotal()) {
				return total();
			}
			int count = 1 << policy.minimumDepth;
			TreeSet<Double> cuts = new TreeSet<>();
			for (int index = 0; index <= count; index++) {
				cuts.add(index == count ? domain.getUpper()
						: domain.getLower() + period * index / count);
			}
			// The metric structures cut the partition exactly at the ends of their pieces.
			Map<Double, Cell> events = metricEvents(cuts);
			Double previous = null;
			for (double cut : cuts) {
				if (previous != null) {
					Cell event = events.get(previous);
					if (event != null && previous > domain.getLower()) {
						cells.add(event);
					}
					cells.add(new Cell(previous, cut, List.of(), Set.of()));
				}
				previous = cut;
			}
			if (circle) {
				// The seam cell covers the canonical circle from Math.PI, below pi, to
				// -Math.PI + 2 pi, above pi: no canonical parameter lies strictly inside. An
				// event at the start of the circle is that cell.
				Cell event = events.get(domain.getLower());
				cells.add(event != null ? event : new Cell(Math.PI, seamUpper, List.of(),
						Set.of()));
			}
			for (Cell cell : cells) {
				if (!cell.event) {
					assignMetric(cell);
				}
			}
			boolean progress = true;
			while (progress && !exhausted) {
				progress = false;
				for (int index = 0; index < cells.size() && !exhausted; index++) {
					Cell cell = cells.get(index);
					if (cell.state == State.PENDING) {
						if (cell.root != null) {
							decideBracket(cell);
						} else {
							evaluate(cell);
						}
						progress = true;
					} else if (cell.state == State.UNRESOLVED) {
						progress |= resolve(index);
					}
				}
			}
			for (Cell cell : cells) {
				if (!cell.isFinal()) {
					band(cell, exhausted ? LocusCoverageCause2D.BUDGET_EXHAUSTED
							: causeOf(cell.outcome), exhausted ? "budget-exhausted"
									: describe(cell.outcome));
				}
			}
			return assemble();
		}

		/** Structural totality: defined and continuous everywhere, without any box. */
		private Result total() {
			LocusBoundarySource2D totality = new LocusBoundarySource2D("structural-totality",
					ConstructionBoxModel2D.TOTALITY_VERSION.replace('/', '-'), "whole-domain");
			List<Coverage> coverage = List.of(new Coverage(domain,
					CoverageState.CERTIFIED_EXISTS, LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE,
					LocusEvidenceNature2D.EXACT, EVIDENCE + "structural-totality"));
			ContinuousComponent component;
			DomainTopology topology;
			if (circle) {
				topology = DomainTopology.PERIODIC;
				LocusBoundary2D closure = new LocusBoundary2D(
						LocusBoundary2D.Kind.PERIODIC_CLOSURE, totality, true,
						LocusEvidenceNature2D.EXACT);
				component = ContinuousComponent.arc(domain, domain.getLower(),
						domain.getLower(), true, closure, closure, Maximality.ESTABLISHED,
						LocusEvidenceNature2D.EXACT, "hybrid-component/0");
			} else {
				topology = DomainTopology.BOUNDED;
				component = ContinuousComponent.interval(domain,
						domainEnd(true), domainEnd(false), Maximality.ESTABLISHED,
						LocusEvidenceNature2D.EXACT, "hybrid-component/0");
			}
			LocusExistenceStructure2D structure = new LocusExistenceStructure2D(branchKey,
					domain, topology, 0, coverage, List.of(component), List.of(), List.of(),
					Completeness.COMPLETE, CONTRACT);
			List<CertifiedStationaryPoint> stationary = stationaryPoints(structure);
			structure = structure.withCertifiedFolds(folds(stationary));
			return new Result(structure, null, List.of(), List.of(), stationary, boxes, 0, 0,
					policy.getSignature(), null);
		}

		private LocusBoundary2D domainEnd(boolean lower) {
			return new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
					lower ? LocusBoundarySource2D.declaredLower()
							: LocusBoundarySource2D.declaredUpper(),
					lower ? domain.isLowerClosed() : domain.isUpperClosed(),
					LocusEvidenceNature2D.EXACT);
		}

		/**
		 * Adds the interior ends of the pieces of every certified metric structure to the
		 * cuts and builds the event cells: one per origin wrap, and one where a defined piece
		 * meets an undefined one at an exact binary64 parameter.
		 *
		 * @return event cells by their canonical parameter
		 */
		private Map<Double, Cell> metricEvents(TreeSet<Double> cuts) {
			Map<Double, Cell> events = new TreeMap<>();
			for (int node : metricNodes) {
				LocusMetricFunctionStructure2D structure = metrics.get(node);
				if (structure == null || !structure.isCertified()) {
					continue;
				}
				List<Piece> pieces = structure.getPieces();
				for (Piece piece : pieces) {
					addCut(cuts, piece.getInterval().getLower());
					addCut(cuts, piece.getInterval().getUpper());
				}
				String token = metricToken(node);
				for (OriginWrap wrap : structure.getEvents()) {
					double at = normalized(wrap.getDriverParameter());
					addCut(cuts, at);
					putEvent(events, at, wrapEvent(node, token, wrap, at));
				}
				for (int index = 0; index < pieces.size(); index++) {
					boolean seam = index + 1 == pieces.size();
					if (seam && !circle) {
						break;
					}
					Piece below = pieces.get(index);
					Piece above = pieces.get(seam ? 0 : index + 1);
					double at = seam ? domain.getLower() : below.getInterval().getUpper();
					boolean adjacent = seam
							? below.getInterval().getUpper() == domain.getUpper()
									&& above.getInterval().getLower() == domain.getLower()
							: above.getInterval().getLower() == at;
					boolean belowDefined = below.getState() == LocusMetricFunctionStructure2D
							.State.DEFINED;
					boolean aboveDefined = above.getState() == LocusMetricFunctionStructure2D
							.State.DEFINED;
					boolean belowUndefined = below.getState() == LocusMetricFunctionStructure2D
							.State.UNDEFINED;
					boolean aboveUndefined = above.getState() == LocusMetricFunctionStructure2D
							.State.UNDEFINED;
					if (!adjacent || !(belowDefined && aboveUndefined
							|| belowUndefined && aboveDefined)) {
						continue;
					}
					Cell event = eventCell(at);
					event.state = State.EVENT;
					event.nature = LocusEvidenceNature2D.EXACT;
					event.evidence = "route-domain:" + token;
					// At the seam the defined side below ends at its last canonical parameter.
					event.eventBelow = belowDefined ? structuralEnd(token, "ROUTE_DOMAIN", "upper",
							seam || below.getInterval().isUpperClosed()) : null;
					event.eventAbove = aboveDefined ? structuralEnd(token, "ROUTE_DOMAIN", "lower",
							above.getInterval().isLowerClosed()) : null;
					putEvent(events, at, event);
				}
			}
			eventParameters.addAll(events.keySet());
			return events;
		}

		private void addCut(TreeSet<Double> cuts, double cut) {
			double value = normalized(cut);
			if (domain.getLower() < value && value < domain.getUpper()) {
				cuts.add(value);
			}
		}

		private static double normalized(double value) {
			return value == 0 ? 0 : value;
		}

		/** Two events at one parameter certify nothing there: a band, which fails closed. */
		private void putEvent(Map<Double, Cell> events, double at, Cell event) {
			if (events.containsKey(at)) {
				diagnostic = "Coincident structural events at " + Double.toHexString(at);
				Cell band = eventCell(at);
				band.event = false;
				band(band, LocusCoverageCause2D.NOT_CERTIFIED, "coincident-structural-events");
				events.put(at, band);
				return;
			}
			events.put(at, event);
		}

		/**
		 * The origin wrap of a metric input: two distinct structural ends when the jump of
		 * the traced point is certified, band edges otherwise.
		 */
		private Cell wrapEvent(int node, String token, OriginWrap wrap, double at) {
			Cell cell = eventCell(at);
			boolean start = wrap.isStartIncluded();
			if (wrap.isJumpPositive() && outputJumps(node, wrap, at)) {
				cell.state = State.EVENT;
				cell.nature = LocusEvidenceNature2D.EXACT;
				cell.evidence = "origin-wrap:" + token;
				cell.eventAbove = wrap.end(token, true);
				cell.eventBelow = wrap.end(token, false);
			} else {
				band(cell, LocusCoverageCause2D.NOT_CERTIFIED,
						"origin-wrap-jump-not-established:" + token);
				cell.eventAbove = new LocusBoundary2D(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
						new LocusBoundarySource2D(token, "ORIGIN_WRAP", "start"), start,
						LocusEvidenceNature2D.NONE);
				cell.eventBelow = new LocusBoundary2D(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
						new LocusBoundarySource2D(token, "ORIGIN_WRAP", "end"), !start,
						LocusEvidenceNature2D.NONE);
			}
			return cell;
		}

		/**
		 * The traced point jumps at the event when its enclosures at the two one-sided
		 * limits of the length, {@code 0} and the rigorous bounds of the total length of the
		 * source, are disjoint: the total steps are continuous in the length, so the two
		 * one-sided limits of the traced point differ. No metric value is read.
		 */
		private boolean outputJumps(int node, OriginWrap wrap, double at) {
			if (metricNodes.size() != 1 || !spend() || !spend()) {
				return false;
			}
			SplineOutwardInterval2D box = SplineOutwardInterval2D.point(at);
			SplineOutwardInterval2D[] restart = model.encloseWithMetricValues(box, circle,
					Map.of(node, new double[] {0, 0}));
			SplineOutwardInterval2D[] full = model.encloseWithMetricValues(box, circle,
					Map.of(node, new double[] {wrap.getJumpLowerBound(),
						wrap.getJumpUpperBound()}));
			return restart != null && full != null
					&& (restart[0].disjoint(full[0]) || restart[1].disjoint(full[1]));
		}

		private static LocusBoundary2D structuralEnd(String token, String kind, String side,
				boolean included) {
			return new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
					new LocusBoundarySource2D(token, kind, side), included,
					LocusEvidenceNature2D.EXACT);
		}

		/** @return a zero-width cell at an interior parameter, or the seam cell at -pi */
		private Cell eventCell(double at) {
			Cell cell = circle && at == domain.getLower()
					? new Cell(Math.PI, seamUpper, List.of(), Set.of())
					: new Cell(at, at, List.of(), Set.of());
			cell.event = true;
			cell.eventParameter = at;
			return cell;
		}

		private String metricToken(int node) {
			return operation(node);
		}

		/**
		 * @return durable identity token of the step, or its revision-local program address
		 *         when the caller supplied none
		 */
		private String operation(int node) {
			if (node - 1 < operations.size()) {
				return operations.get(node - 1);
			}
			return "class-v3-step-" + node + "-"
					+ program.getSteps().get(node - 1).getOperation().name();
		}

		/** Records the state of every metric input on a cell, from its structure. */
		private void assignMetric(Cell cell) {
			Set<Integer> defined = new TreeSet<>();
			for (int node : metricNodes) {
				LocusMetricFunctionStructure2D structure = metrics.get(node);
				if (structure == null) {
					unknown(cell, LocusCoverageCause2D.NOT_CERTIFIED,
							"metric-input-without-structure");
					continue;
				}
				if (!structure.isCertified()) {
					unknown(cell, structure.getCause(), "metric-structure-not-established");
					continue;
				}
				LocusMetricFunctionStructure2D.State state = stateOn(structure, cell);
				if (state == LocusMetricFunctionStructure2D.State.DEFINED) {
					defined.add(node);
				} else if (state == LocusMetricFunctionStructure2D.State.UNDEFINED) {
					cell.metricUndefined = true;
				} else if (state == LocusMetricFunctionStructure2D.State.UNRESOLVED) {
					unknown(cell, LocusCoverageCause2D.UPSTREAM_NOT_ESTABLISHED,
							"metric-source-unresolved");
				} else if (state == LocusMetricFunctionStructure2D.State.BOUNDARY) {
					// Finiteness of the route at the exact root is not certified.
					unknown(cell, LocusCoverageCause2D.NOT_CERTIFIED,
							"metric-route-at-source-boundary");
				} else {
					unknown(cell, LocusCoverageCause2D.NOT_CERTIFIED,
							"metric-structure-not-uniform");
				}
			}
			cell.metricDefined = Collections.unmodifiableSet(defined);
		}

		private static void unknown(Cell cell, LocusCoverageCause2D cause, String evidence) {
			if (cell.metricCause == null) {
				cell.metricCause = cause;
				cell.metricEvidence = evidence;
			}
		}

		/**
		 * @return the state of the piece that holds the whole cell, its ends at events
		 *         aside; on the seam cell the common state of the two canonical sides of the
		 *         seam; {@code null} when no piece holds it
		 */
		private LocusMetricFunctionStructure2D.State stateOn(
				LocusMetricFunctionStructure2D structure, Cell cell) {
			if (circle && cell.lower == Math.PI && cell.upper == seamUpper) {
				Piece last = structure.pieceContaining(Math.nextDown(domain.getUpper()));
				Piece first = structure.pieceContaining(domain.getLower());
				return last == null || first == null || last.getState() != first.getState()
						? null : first.getState();
			}
			Piece piece = structure.pieceContaining(cell.lower + (cell.upper - cell.lower) / 2);
			return piece != null && covers(piece, cell.lower) && covers(piece, cell.upper)
					? piece.getState() : null;
		}

		private boolean covers(Piece piece, double parameter) {
			LocusInterval2D interval = piece.getInterval();
			return interval.contains(parameter, 0) || eventParameters.contains(parameter)
					|| parameter == domain.getUpper() && interval.getUpper() == parameter;
		}

		private boolean spend() {
			if (boxes >= policy.boxBudget) {
				exhausted = true;
				return false;
			}
			boxes++;
			record(instrumentation, LocusCertificationWork2D.EXISTENCE_BOX);
			return true;
		}

		private ConstructionBoxOutcome2D run(double lower, double upper,
				List<Target> assumed, Set<Integer> definedMetrics) {
			if (!spend()) {
				return null;
			}
			SplineOutwardInterval2D box = new SplineOutwardInterval2D(lower, upper);
			if (!assumed.isEmpty()) {
				return model.evaluateAssuming(box, assumed, circle, definedMetrics);
			}
			return model.evaluateCertifying(box, circle, definedMetrics);
		}

		private PredicateEnclosure enclose(double lower, double upper, Target target) {
			if (!spend()) {
				return null;
			}
			return model.enclose(new SplineOutwardInterval2D(lower, upper), target, circle);
		}

		/** @return sign of the predicate at one parameter: 1, -1, 0 when undecided */
		private int signAt(double parameter, Target target) {
			PredicateEnclosure enclosure = enclose(parameter, parameter, target);
			if (enclosure == null) {
				return 0;
			}
			return enclosure.value.lower > 0 ? 1 : enclosure.value.upper < 0 ? -1 : 0;
		}

		/** @return sign of the first derivative of the predicate at one parameter */
		private int derivativeSignAt(double parameter, Target target) {
			PredicateEnclosure enclosure = enclose(parameter, parameter, target);
			if (enclosure == null || enclosure.first == null) {
				return 0;
			}
			return enclosure.first.lower > 0 ? 1 : enclosure.first.upper < 0 ? -1 : 0;
		}

		/** Evaluates a cell; a predicate certified elsewhere is assumed where needed. */
		private void evaluate(Cell cell) {
			if (cell.metricUndefined) {
				// The traced point reads the metric input, whose route is certified undefined.
				cell.state = State.INVALID;
				cell.cause = LocusCoverageCause2D.DEPENDENCY_CERTIFIED_NONEXISTENT;
				cell.nature = LocusEvidenceNature2D.RIGOROUS_INTERVAL;
				cell.evidence = "metric-route-certified-undefined";
				return;
			}
			ConstructionBoxOutcome2D outcome = run(cell.lower, cell.upper, List.of(),
					cell.metricDefined);
			if (outcome == null) {
				return;
			}
			if (outcome.kind != Kind.VALID && cell.assumed.contains(outcome.getTarget())) {
				if (outcome.kind == Kind.UNIFORMLY_INVALID) {
					band(cell, LocusCoverageCause2D.NOT_CERTIFIED,
							"inconsistent-assumption:" + outcome);
					diagnostic = "Interval invalidity contradicts a certified predicate: "
							+ outcome;
					return;
				}
				outcome = run(cell.lower, cell.upper, cell.assumed, cell.metricDefined);
				if (outcome == null) {
					return;
				}
			}
			apply(cell, outcome, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
		}

		private void apply(Cell cell, ConstructionBoxOutcome2D outcome,
				LocusEvidenceNature2D nature) {
			cell.outcome = outcome;
			switch (outcome.kind) {
			case VALID:
				cell.state = State.VALID;
				cell.nature = nature;
				cell.evidence = "predicates-certified-true";
				break;
			case UNIFORMLY_INVALID:
				invalid(cell, nature, outcome.getInvalidReason());
				break;
			default:
				cell.state = State.UNRESOLVED;
				break;
			}
		}

		private void invalid(Cell cell, LocusEvidenceNature2D nature, String reason) {
			cell.state = State.INVALID;
			cell.cause = LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE;
			cell.nature = nature;
			cell.evidence = reason;
		}

		private boolean band(Cell cell, LocusCoverageCause2D cause, String detail) {
			cell.state = State.BAND;
			cell.cause = cause;
			cell.nature = LocusEvidenceNature2D.NONE;
			cell.evidence = detail;
			return true;
		}

		/** Decides the enclosure of one certified root. */
		private void decideBracket(Cell cell) {
			Root root = cell.root;
			List<Target> assumed = new ArrayList<>(cell.assumed);
			assumed.add(root.target);
			ConstructionBoxOutcome2D outcome = run(cell.lower, cell.upper, assumed,
					cell.metricDefined);
			if (outcome == null) {
				return;
			}
			cell.outcome = outcome;
			if (outcome.kind == Kind.UNIFORMLY_INVALID) {
				invalid(cell, LocusEvidenceNature2D.RIGOROUS_INTERVAL,
						outcome.getInvalidReason());
				return;
			}
			if (outcome.kind != Kind.VALID) {
				// An enclosure is never subdivided: an undecided root is a band.
				band(cell, causeOf(outcome), "root-enclosure:" + describe(outcome));
				return;
			}
			// Existence inside the enclosure rests on the box evaluation; the root records
			// keep the nature of the root itself.
			cell.nature = LocusEvidenceNature2D.RIGOROUS_INTERVAL;
			switch (root.kind) {
			case TRANSITION:
				cell.state = State.BOUNDARY;
				cell.evidence = "root-enclosure:"
						+ source(root.target, root.address).getSignature();
				break;
			case SINGULAR_INTERIOR:
				cell.state = State.VALID;
				cell.evidence = "predicates-certified-true";
				break;
			default:
				cell.state = State.ISOLATED;
				cell.evidence = "isolated-root:"
						+ source(root.target, root.address).getSignature();
				cell.position = outcome.value == null ? null : new double[] {
					outcome.value[0].lower, outcome.value[0].upper, outcome.value[1].lower,
					outcome.value[1].upper};
				break;
			}
		}

		/** @return whether the cell changed */
		private boolean resolve(int index) {
			Cell cell = cells.get(index);
			ConstructionBoxOutcome2D outcome = cell.outcome;
			Target target = outcome.getTarget();
			switch (outcome.unresolved) {
			case PREDICATE:
				if (target != null && !cell.exactApplied.contains(target)) {
					Exact info = exactInfo(target);
					if (info != null && applyExact(index, target, info)) {
						return true;
					}
				}
				if (target != null && model.isTransitionTarget(target)
						&& !cell.assumed.contains(target) && intervalRoot(index, target)) {
					return true;
				}
				// The zeros of a predicate are isolated events: refine without waiting.
				return refine(index, causeOf(outcome), true);
			case TOLERANCE:
			case UNLABELLED_COINCIDENT_OUTPUT:
			case ENCLOSURE:
				return refine(index, causeOf(outcome), false);
			default:
				// A structural metric value or a box outside the domain: refining never helps.
				// A metric input that its structure does not decide keeps the typed cause of
				// the structure.
				boolean metric = outcome.unresolved == Unresolved.METRIC_VALUE
						&& cell.metricCause != null;
				return band(cell, metric ? cell.metricCause : causeOf(outcome),
						describe(outcome) + (metric ? ";" + cell.metricEvidence : ""));
			}
		}

		/**
		 * Splits a refinable cell, or makes a band of a cell that cannot be refined.
		 *
		 * @param always whether the cell is refined even inside a run of undecided cells;
		 *        otherwise only a cell next to a certified one, or wider than the band
		 *        resolution, is refined, so that the edges of a band are located without
		 *        subdividing its interior
		 */
		private boolean refine(int index, LocusCoverageCause2D cause, boolean always) {
			Cell cell = cells.get(index);
			double width = cell.upper - cell.lower;
			double middle = cell.lower + width / 2;
			boolean divisible = width > delta && cell.lower < middle && middle < cell.upper;
			boolean frontier = always || isFrontier(index);
			if (divisible && (frontier || width > bandWidth)) {
				replace(index, 1, List.of(cell.child(cell.lower, middle),
						cell.child(middle, cell.upper)));
				return true;
			}
			if (!divisible) {
				Exact info = cell.outcome == null || cell.outcome.getTarget() == null ? null
						: exact.get(cell.outcome.getTarget());
				return band(cell, info == null && exactFailed(cell.outcome)
						? LocusCoverageCause2D.BUDGET_EXHAUSTED : cause,
						describe(cell.outcome));
			}
			// An interior cell of a band waits for its neighbours.
			return false;
		}

		private boolean exactFailed(ConstructionBoxOutcome2D outcome) {
			if (outcome == null || outcome.getTarget() == null) {
				return false;
			}
			String key = describe(outcome.getTarget());
			for (ExactEvidence evidence : exactEvidence) {
				if (evidence.target.equals(key) && "BUDGET_EXHAUSTED".equals(evidence.status)) {
					return true;
				}
			}
			return false;
		}

		private boolean isFrontier(int index) {
			return isDecided(neighbour(index, -1)) || isDecided(neighbour(index, 1));
		}

		private Cell neighbour(int index, int offset) {
			int other = index + offset;
			if (other < 0 || other >= cells.size()) {
				if (!circle) {
					return null;
				}
				other = (other + cells.size()) % cells.size();
			}
			return cells.get(other);
		}

		/** A certified neighbour; a pending one is not, or a band would split everywhere. */
		private static boolean isDecided(Cell cell) {
			return cell != null && (cell.state == State.VALID || cell.state == State.INVALID
					|| cell.state == State.BOUNDARY || cell.state == State.ISOLATED
					|| cell.state == State.EVENT);
		}

		private void replace(int index, int count, List<Cell> pieces) {
			for (int removed = 0; removed < count; removed++) {
				cells.remove(index);
			}
			cells.addAll(index, pieces);
		}

		/**
		 * Certifies a simple root by a sign change with a derivative enclosure that excludes
		 * zero, or decides the predicate through an isolated stationary point.
		 *
		 * @return whether the cell changed
		 */
		private boolean intervalRoot(int index, Target target) {
			Cell cell = cells.get(index);
			PredicateEnclosure enclosure = enclose(cell.lower, cell.upper, target);
			if (enclosure == null || enclosure.first == null) {
				return false;
			}
			int slope = enclosure.first.lower > 0 ? 1 : enclosure.first.upper < 0 ? -1 : 0;
			if (slope == 0 && enclosure.second != null && !enclosure.second.containsZero()) {
				return stationary(index, target, enclosure.second.lower > 0 ? 1 : -1);
			}
			return slope != 0 && monotone(index, target);
		}

		/** The predicate is strictly monotone on the cell. */
		private boolean monotone(int index, Target target) {
			Cell cell = cells.get(index);
			int left = signAt(cell.lower, target);
			int right = signAt(cell.upper, target);
			if (left == 0 || right == 0) {
				return false;
			}
			if (left == right) {
				signed(cell, target, left, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
				return true;
			}
			double[] bracket = narrow(cell.lower, cell.upper, left, target);
			if (bracket == null) {
				return false;
			}
			Root root = new Root(target, "root", CertifiedRoot.RootKind.TRANSITION, 1,
					LocusEvidenceNature2D.RIGOROUS_INTERVAL, bracket[0], bracket[1],
					bracket[0], bracket[1]);
			List<Cell> pieces = new ArrayList<>();
			if (cell.lower < bracket[0]) {
				Cell side = cell.child(cell.lower, bracket[0]);
				signed(side, target, left, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
				pieces.add(side);
			}
			Cell enclosure = cell.child(bracket[0], bracket[1]);
			enclosure.root = root;
			pieces.add(enclosure);
			if (bracket[1] < cell.upper) {
				Cell side = cell.child(bracket[1], cell.upper);
				signed(side, target, right, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
				pieces.add(side);
			}
			replace(index, 1, pieces);
			return true;
		}

		/** A predicate of known constant sign on a cell: invalid, or assumed to hold. */
		private void signed(Cell cell, Target target, int sign,
				LocusEvidenceNature2D nature) {
			if (sign < 0) {
				invalid(cell, nature, target.predicate.getInvalidReason());
			} else {
				if (!cell.assumed.contains(target)) {
					cell.assumed.add(target);
				}
				cell.state = State.PENDING;
				cell.outcome = null;
			}
		}

		/** @return a bracket of width at most delta with the given end signs, or null */
		private double[] narrow(double lower, double upper, int leftSign, Target target) {
			double left = lower;
			double right = upper;
			for (int iteration = 0; iteration < NARROWING_LIMIT && right - left > delta;
					iteration++) {
				double middle = left + (right - left) / 2;
				if (!(left < middle && middle < right)) {
					break;
				}
				int sign = signAt(middle, target);
				if (sign == 0) {
					// Too close to the root for the enclosure: try the quarter points.
					double quarter = (right - left) / 4;
					int lowerSign = signAt(middle - quarter, target);
					int upperSign = signAt(middle + quarter, target);
					if (lowerSign == -leftSign && lowerSign != 0) {
						right = middle - quarter;
					} else if (upperSign == leftSign && upperSign != 0) {
						left = middle + quarter;
					} else if (lowerSign == leftSign && upperSign == -leftSign) {
						left = middle - quarter;
						right = middle + quarter;
					} else {
						break;
					}
				} else if (sign == leftSign) {
					left = middle;
				} else {
					right = middle;
				}
			}
			return right - left <= delta ? new double[] {left, right} : null;
		}

		/**
		 * The second derivative keeps its sign on the cell: the first derivative has at
		 * most one zero, a certified extremum whose value decides the predicate.
		 */
		private boolean stationary(int index, Target target, int curvature) {
			Cell cell = cells.get(index);
			int left = derivativeSignAt(cell.lower, target);
			int right = derivativeSignAt(cell.upper, target);
			if (left == 0 || right == 0) {
				return false;
			}
			if (left == right) {
				return monotone(index, target);
			}
			double low = cell.lower;
			double high = cell.upper;
			for (int iteration = 0; iteration < NARROWING_LIMIT && high - low > delta;
					iteration++) {
				double middle = low + (high - low) / 2;
				if (!(low < middle && middle < high)) {
					break;
				}
				int sign = derivativeSignAt(middle, target);
				if (sign == 0) {
					break;
				}
				if (sign == left) {
					low = middle;
				} else {
					high = middle;
				}
			}
			PredicateEnclosure extremum = enclose(low, high, target);
			if (extremum == null) {
				return false;
			}
			int value = extremum.value.lower > 0 ? 1 : extremum.value.upper < 0 ? -1 : 0;
			if (value == 0) {
				// A root of even multiplicity, or two roots too close to separate.
				return false;
			}
			if (value == curvature) {
				// A minimum above zero or a maximum below zero: one sign on the whole cell.
				signed(cell, target, value, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
				return true;
			}
			// Two simple roots, one on each monotone side of the extremum.
			Cell lowerSide = cell.child(cell.lower, low);
			Cell middleCell = cell.child(low, high);
			Cell upperSide = cell.child(high, cell.upper);
			signed(middleCell, target, value, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
			List<Cell> pieces = new ArrayList<>();
			if (cell.lower < low) {
				pieces.add(lowerSide);
			}
			pieces.add(middleCell);
			if (high < cell.upper) {
				pieces.add(upperSide);
			}
			replace(index, 1, pieces);
			return true;
		}

		/** @return exact sign structure of a first-level predicate, or {@code null} */
		private Exact exactInfo(Target target) {
			if (exact.containsKey(target)) {
				return exact.get(target);
			}
			Exact info = null;
			if (circle) {
				Optional<TrigonometricPolynomial2D> form =
						FirstLevelPredicate2D.build(program, target);
				if (form.isPresent()) {
					info = classifyCircle(target, form.get());
				}
			} else if (program.getDriver() == Driver.SEGMENT && domain.getLower() == 0
					&& domain.getUpper() == 1) {
				Optional<ExactPolynomial1D> form =
						FirstLevelPredicate2D.buildSegment(program, target);
				if (form.isPresent()) {
					info = classifySegment(target, form.get());
				}
			}
			exact.put(target, info);
			return info;
		}

		private Exact classifyCircle(Target target, TrigonometricPolynomial2D form) {
			ExactRational2D width = ExactRational2D.ofBinary64(
					Math.scalb(period, -(policy.deltaExponent + 2)));
			CircleDriverRootStructure2D structure;
			try {
				structure = CircleDriverRootStructure2D.classify(form, width,
						policy.exactBudget);
			} catch (ExactBudgetExhaustedException exception) {
				exactEvidence.add(new ExactEvidence(describe(target), "BUDGET_EXHAUSTED",
						fingerprint(form.toString()), 2 * form.totalDegree(), 0,
						width.toString(), 0, 0, exception.getMessage()));
				return null;
			}
			exactEvidence.add(new ExactEvidence(describe(target),
					structure.status().name(), fingerprint(form.toString()),
					structure.tangentHalfAnglePolynomial().degree(),
					structure.maximumCoefficientBitLength(), width.toString(),
					structure.bisections(), structure.maximumRootBisections(),
					structure.roots().toString()));
			if (structure.isIdenticallyZero()) {
				return new Exact(true, List.of(new Segment(domain.getLower(), seamUpper, 0,
						null)));
			}
			List<CircleDriverRootStructure2D.Root> finite = new ArrayList<>();
			CircleDriverRootStructure2D.Root seam = null;
			for (CircleDriverRootStructure2D.Root root : structure.roots()) {
				if (root.isAtInfinity()) {
					seam = root;
				} else if (root.tLower() < domain.getLower()
						|| root.tUpper() > Math.PI) {
					// The enclosure crosses the canonical representative of the seam.
					diagnostic = "Exact root enclosure crosses the seam: " + root;
					return null;
				} else {
					finite.add(root);
				}
			}
			List<Integer> gaps = structure.gapSigns();
			int first = seam == null ? 0 : 1;
			List<Segment> segments = new ArrayList<>();
			double cursor = domain.getLower();
			for (int index = 0; index < finite.size(); index++) {
				CircleDriverRootStructure2D.Root root = finite.get(index);
				segments.add(new Segment(cursor, root.tLower(), gaps.get(first + index),
						null));
				segments.add(new Segment(root.tLower(), root.tUpper(), 0,
						exactRoot(target, structure.roots().indexOf(root), root.kind(),
								root.multiplicity(), root.tLower(), root.tUpper(),
								root.tLower(), root.tUpper())));
				cursor = root.tUpper();
			}
			int last = gaps.get(first + finite.size());
			if (seam == null) {
				segments.add(new Segment(cursor, seamUpper, last, null));
			} else {
				segments.add(new Segment(cursor, Math.PI, last, null));
				segments.add(new Segment(Math.PI, seamUpper, 0, exactRoot(target,
						structure.roots().indexOf(seam), seam.kind(), seam.multiplicity(),
						Math.PI, seamUpper, seam.canonicalTLower(), seam.canonicalTUpper())));
			}
			return new Exact(false, segments);
		}

		private Exact classifySegment(Target target, ExactPolynomial1D form) {
			ExactRational2D width = ExactRational2D.ofBinary64(
					Math.scalb(period, -(policy.deltaExponent + 1)));
			SegmentDriverRootStructure2D structure;
			try {
				structure = SegmentDriverRootStructure2D.classify(form, width,
						policy.exactBudget);
			} catch (ExactBudgetExhaustedException exception) {
				exactEvidence.add(new ExactEvidence(describe(target), "BUDGET_EXHAUSTED",
						fingerprint(form.toString()), form.degree(), 0, width.toString(), 0,
						0, exception.getMessage()));
				return null;
			}
			exactEvidence.add(new ExactEvidence(describe(target), structure.status().name(),
					fingerprint(form.toString()), form.degree(),
					structure.maximumCoefficientBitLength(), width.toString(),
					structure.bisections(), structure.maximumRootBisections(),
					structure.roots().toString()));
			if (structure.isIdenticallyZero()) {
				return new Exact(true, List.of(new Segment(0, 1, 0, null)));
			}
			List<Integer> gaps = structure.gapSigns();
			List<Segment> segments = new ArrayList<>();
			double cursor = 0;
			int gap = 0;
			for (SegmentDriverRootStructure2D.Root root : structure.roots()) {
				if (root.isDomainEnd() && root.innerSign() < 0) {
					// Existence reduced to a domain end is left to the boxes.
					diagnostic = "Exact root isolates a domain end: " + root;
					return null;
				}
				if (root.end() == SegmentDriverRootStructure2D.DomainEnd.LOWER) {
					// The empty gap before a root at 0; the next gap includes 0, where the
					// predicate vanishes.
					gap++;
					continue;
				}
				if (root.end() == SegmentDriverRootStructure2D.DomainEnd.UPPER) {
					break;
				}
				// An exact interior root is a binary64 value: one ulp each way encloses it.
				double lower = root.isExact() ? Math.nextDown(root.tLower()) : root.tLower();
				double upper = root.isExact() ? Math.nextUp(root.tUpper()) : root.tUpper();
				if (!(cursor < lower) || !(upper < 1)) {
					diagnostic = "Exact root enclosure reaches a domain end: " + root;
					return null;
				}
				segments.add(new Segment(cursor, lower, gaps.get(gap), null));
				segments.add(new Segment(lower, upper, 0, exactRoot(target,
						structure.roots().indexOf(root), root.kind(), root.multiplicity(),
						lower, upper, lower, upper)));
				cursor = upper;
				gap++;
			}
			segments.add(new Segment(cursor, 1, gaps.get(gap), null));
			return new Exact(false, segments);
		}

		private Root exactRoot(Target target, int ordinal, RealRootStructure1D.RootKind kind,
				int multiplicity, double lower, double upper, double canonicalLower,
				double canonicalUpper) {
			CertifiedRoot.RootKind rootKind;
			if (target.predicate.isEquality()) {
				rootKind = CertifiedRoot.RootKind.OPEN_EVENT;
			} else if (kind == RealRootStructure1D.RootKind.TRANSITION) {
				rootKind = CertifiedRoot.RootKind.TRANSITION;
			} else if (kind == RealRootStructure1D.RootKind.SINGULAR_INTERIOR) {
				rootKind = CertifiedRoot.RootKind.SINGULAR_INTERIOR;
			} else {
				rootKind = CertifiedRoot.RootKind.ISOLATED_POINT;
			}
			return new Root(target, "root-" + ordinal, rootKind, multiplicity,
					LocusEvidenceNature2D.EXACT, lower, upper, canonicalLower, canonicalUpper);
		}

		/**
		 * Applies the exact sign structure of a predicate to an undecided cell. A
		 * strict-inequality predicate is certified false on its negative gaps and assumed on
		 * its positive gaps, and each root enclosure becomes a cell of its own; an equality
		 * predicate is never assumed, because an inherited tolerance restricts definedness
		 * around its roots: only an identically vanishing one decides the cell.
		 *
		 * @return whether the cell changed
		 */
		private boolean applyExact(int index, Target target, Exact info) {
			Cell cell = cells.get(index);
			cell.exactApplied.add(target);
			if (target.predicate.isEquality()) {
				if (info.identicallyZero) {
					invalid(cell, LocusEvidenceNature2D.EXACT,
							target.predicate.getInvalidReason());
					return true;
				}
				return false;
			}
			if (info.identicallyZero) {
				// A discriminant that vanishes identically: the roots coincide everywhere.
				signed(cell, target, 1, LocusEvidenceNature2D.EXACT);
				return true;
			}
			double lower = cell.lower;
			double upper = cell.upper;
			for (Segment segment : info.segments) {
				if (segment.root != null && segment.upper > cell.lower
						&& segment.lower < cell.upper) {
					lower = Math.min(lower, segment.lower);
					upper = Math.max(upper, segment.upper);
				}
			}
			// The cells that a root enclosure overlaps are rebuilt; the parts of the
			// neighbours outside the rebuilt range keep their decisions.
			int first = index;
			int last = index;
			while (first > 0 && cells.get(first - 1).upper > lower) {
				first--;
			}
			while (last + 1 < cells.size() && cells.get(last + 1).lower < upper) {
				last++;
			}
			List<Cell> originals = new ArrayList<>(cells.subList(first, last + 1));
			for (Cell original : originals) {
				if (original != cell && original.root != null) {
					return conflict(cell, "Overlapping root enclosures");
				}
				if (original.event || !original.sameMetric(cell)) {
					// A root enclosure across a cut of a metric structure: never merged.
					return conflict(cell, "A root enclosure overlaps a metric structure cut");
				}
			}
			lower = Math.max(lower, originals.get(0).lower);
			upper = Math.min(upper, originals.get(originals.size() - 1).upper);
			List<Cell> pieces = new ArrayList<>();
			Cell head = originals.get(0);
			if (head.lower < lower) {
				pieces.add(trimmed(head, head.lower, lower));
			}
			for (Segment segment : info.segments) {
				double from = Math.max(segment.lower, lower);
				double to = Math.min(segment.upper, upper);
				if (!(from < to)) {
					continue;
				}
				Cell piece = new Cell(from, to, common(originals, from, to),
						Set.of(target));
				piece.copyMetric(cell);
				if (segment.root != null) {
					piece.root = segment.root;
				} else {
					signed(piece, target, segment.sign, LocusEvidenceNature2D.EXACT);
				}
				pieces.add(piece);
			}
			Cell tail = originals.get(originals.size() - 1);
			if (upper < tail.upper) {
				pieces.add(trimmed(tail, upper, tail.upper));
			}
			replace(first, originals.size(), pieces);
			return true;
		}

		/** @return the part of a cell that keeps its decision */
		private static Cell trimmed(Cell cell, double from, double to) {
			Cell part = cell.child(from, to);
			part.state = cell.state;
			part.outcome = cell.outcome;
			part.root = cell.root;
			part.cause = cell.cause;
			part.nature = cell.nature;
			part.evidence = cell.evidence;
			part.position = cell.position;
			return part;
		}

		/** @return the assumptions certified on every original cell the range overlaps */
		private static List<Target> common(List<Cell> originals, double from, double to) {
			List<Target> result = null;
			for (Cell original : originals) {
				if (original.upper <= from || original.lower >= to) {
					continue;
				}
				if (result == null) {
					result = new ArrayList<>(original.assumed);
				} else {
					result.retainAll(original.assumed);
				}
			}
			return result == null ? new ArrayList<>() : result;
		}

		private boolean conflict(Cell cell, String detail) {
			diagnostic = detail;
			return band(cell, LocusCoverageCause2D.DEGENERATE_CONFIGURATION,
					"coincident-roots:" + describe(cell.outcome));
		}

		private LocusCoverageCause2D causeOf(ConstructionBoxOutcome2D outcome) {
			if (outcome == null || outcome.unresolved == null) {
				return LocusCoverageCause2D.NOT_CERTIFIED;
			}
			switch (outcome.unresolved) {
			case PREDICATE:
				return LocusCoverageCause2D.DEGENERATE_CONFIGURATION;
			case TOLERANCE:
				return LocusCoverageCause2D.INHERITED_TOLERANCE_BEHAVIOUR;
			case UNLABELLED_COINCIDENT_OUTPUT:
				return LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT;
			default:
				return LocusCoverageCause2D.NOT_CERTIFIED;
			}
		}

		/** Builds the structure from the final cells. */
		private Result assemble() {
			List<CertifiedRoot> roots = new ArrayList<>();
			List<LocusSingularParameter2D> singular = new ArrayList<>();
			List<LocusPointComponent2D> points = new ArrayList<>();
			for (int index = 0; index < cells.size(); index++) {
				Cell cell = cells.get(index);
				if (cell.root == null || cell.state == State.INVALID) {
					// A root where another predicate fails bounds nothing.
					continue;
				}
				Root root = cell.root;
				boolean inBand = cell.state == State.BAND;
				LocusBoundarySource2D source = source(root.target, root.address);
				roots.add(new CertifiedRoot(root.kind, root.canonicalLower,
						root.canonicalUpper, source, !root.target.predicate.isEquality(),
						root.multiplicity, root.nature, inBand));
				if (cell.state == State.VALID
						&& root.kind == CertifiedRoot.RootKind.SINGULAR_INTERIOR) {
					singular.add(new LocusSingularParameter2D(root.canonicalLower,
							root.canonicalUpper, source, root.multiplicity, root.nature));
				} else if (cell.state == State.ISOLATED) {
					boolean bounded = isInvalid(neighbour(index, -1))
							&& isInvalid(neighbour(index, 1));
					points.add(new LocusPointComponent2D(root.canonicalLower,
							root.canonicalUpper, source, bounded ? Maximality.ESTABLISHED
									: Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
							LocusEvidenceNature2D.RIGOROUS_INTERVAL, "hybrid-point/"
									+ points.size(), cell.position));
				}
			}
			roots.addAll(bandRoots());
			roots.sort((a, b) -> Double.compare(a.lower, b.lower));
			List<ContinuousComponent> components = new ArrayList<>();
			boolean maximal;
			try {
				maximal = components(components);
			} catch (IllegalStateException | IllegalArgumentException exception) {
				return failed(exception.getMessage(), roots);
			}
			List<Coverage> coverage;
			try {
				coverage = coverage();
			} catch (IllegalArgumentException exception) {
				return failed(exception.getMessage(), roots);
			}
			List<LocusInterval2D> bands = new ArrayList<>();
			for (Coverage item : coverage) {
				if (item.getState() == CoverageState.UNRESOLVED) {
					bands.add(item.getInterval());
					record(instrumentation, LocusCertificationWork2D.UNRESOLVED_BAND);
				}
			}
			// The seam cell holds no canonical parameter: an unresolved seam leaves the
			// structure not established although no band appears in the coverage.
			boolean complete = maximal;
			for (Cell cell : cells) {
				complete &= cell.state != State.BAND;
			}
			for (LocusPointComponent2D point : points) {
				complete &= point.getMaximality() == Maximality.ESTABLISHED;
			}
			LocusExistenceStructure2D structure;
			try {
				structure = new LocusExistenceStructure2D(branchKey, domain,
						circle ? DomainTopology.PERIODIC : DomainTopology.BOUNDED, 0, coverage,
						components, points, singular,
						complete ? Completeness.COMPLETE : Completeness.NOT_ESTABLISHED,
						CONTRACT);
			} catch (IllegalArgumentException exception) {
				return failed(exception.getMessage(), roots);
			}
			List<CertifiedStationaryPoint> stationary = stationaryPoints(structure);
			try {
				structure = structure.withCertifiedFolds(folds(stationary));
			} catch (IllegalArgumentException exception) {
				return failed(exception.getMessage(), roots);
			}
			LocusExploratoryEvidence2D exploration = explore(branchKey, bands, sampler,
					policy, instrumentation);
			return new Result(structure, exploration, roots, exactEvidence, stationary, boxes,
					transitions, bands.size(), policy.getSignature(), diagnostic);
		}

		/** Fails closed: a structural failure of the assembly certifies nothing. */
		private Result failed(String detail, List<CertifiedRoot> roots) {
			diagnostic = detail;
			LocusExistenceStructure2D structure = new LocusExistenceStructure2D(branchKey,
					domain, LocusExistenceStructure2D.topologyOf(domain, circle), 0,
					List.of(new Coverage(domain, CoverageState.UNRESOLVED,
							LocusCoverageCause2D.NOT_CERTIFIED, LocusEvidenceNature2D.NONE,
							EVIDENCE + "assembly-failed")), List.of(), List.of(),
					Completeness.NOT_ESTABLISHED, CONTRACT);
			record(instrumentation, LocusCertificationWork2D.UNRESOLVED_BAND);
			return new Result(structure, explore(branchKey, List.of(domain), sampler,
					policy, instrumentation), roots, exactEvidence, List.of(), boxes, 0, 1,
					policy.getSignature(), detail);
		}

		/**
		 * Certifies the stationary points at the fixed points of every verified symmetry of a
		 * circle program (certification design section 5): existence by the symmetry at a
		 * fixed point interior to a certified component; a fold when, on an isolation box from
		 * the widest of the policy down to the narrowest, one component of {@code F''}
		 * excludes zero.
		 */
		private List<CertifiedStationaryPoint> stationaryPoints(
				LocusExistenceStructure2D structure) {
			List<CertifiedStationaryPoint> points = new ArrayList<>();
			if (!circle) {
				return points;
			}
			double halfPi = Math.PI / 2;
			for (ConstructionSymmetry2D.Mirror mirror
					: ConstructionSymmetry2D.invariantMirrors(program)) {
				double[][] fixed = mirror == ConstructionSymmetry2D.Mirror.HORIZONTAL
						? new double[][] {{0, 0, 0, 0}, {Math.PI, seamUpper, -seamUpper,
							-Math.PI}}
						: new double[][] {{halfPi, Math.nextUp(halfPi), halfPi,
							Math.nextUp(halfPi)}, {-Math.nextUp(halfPi), -halfPi,
								-Math.nextUp(halfPi), -halfPi}};
				for (double[] parameter : fixed) {
					CertifiedStationaryPoint point = stationaryPoint(structure, mirror,
							parameter);
					if (point != null) {
						points.add(point);
					}
				}
			}
			points.sort((a, b) -> Double.compare(a.parameterLower, b.parameterLower));
			return points;
		}

		/**
		 * @return the non-degenerate folds, which the structure publishes as the only bands
		 *         where a drawing may waive its turning criterion (consumption design section
		 *         5.1); a degenerate stationary point is never one
		 */
		private static List<LocusCertifiedFold2D> folds(
				List<CertifiedStationaryPoint> stationary) {
			List<LocusCertifiedFold2D> folds = new ArrayList<>();
			for (CertifiedStationaryPoint point : stationary) {
				if (point.getKind() == CertifiedStationaryPoint.StationaryKind.FOLD) {
					folds.add(new LocusCertifiedFold2D(point.getParameterLower(),
							point.getParameterUpper(), point.getIsolationLower(),
							point.getIsolationUpper(), point.getSymmetry(), point.getNature()));
				}
			}
			return folds;
		}

		/**
		 * @param parameter enclosure {@code {lower, upper}} of the fixed point {@code t0}
		 *        in the cell coordinates, followed by its canonical enclosure
		 */
		private CertifiedStationaryPoint stationaryPoint(LocusExistenceStructure2D structure,
				ConstructionSymmetry2D.Mirror mirror, double[] parameter) {
			boolean interior = false;
			for (int exponent = policy.foldWidestExponent;
					exponent <= policy.foldNarrowestExponent; exponent++) {
				double width = Math.scalb(period, -exponent);
				double lower = Math.nextDown(parameter[0] - width);
				double upper = Math.nextUp(parameter[1] + width);
				if (!insideOneComponent(structure, lower, upper)) {
					continue;
				}
				interior = true;
				if (!spend()) {
					break;
				}
				SplineOutwardInterval2D[][] jets = model.encloseTraced(
						new SplineOutwardInterval2D(lower, upper), true);
				if (jets == null) {
					continue;
				}
				for (int axis = 0; axis < 2; axis++) {
					if (!jets[2][axis].containsZero()) {
						return new CertifiedStationaryPoint(
								CertifiedStationaryPoint.StationaryKind.FOLD, mirror.name(),
								parameter[2], parameter[3], lower, upper, axis, new double[] {
									jets[2][0].lower, jets[2][0].upper, jets[2][1].lower,
									jets[2][1].upper});
					}
				}
			}
			return interior ? new CertifiedStationaryPoint(
					CertifiedStationaryPoint.StationaryKind.DEGENERATE, mirror.name(),
					parameter[2], parameter[3], Double.NaN, Double.NaN, -1, null) : null;
		}

		/**
		 * @return whether the canonical image of the box lies in certified existence inside one
		 *         component: the fixed point is then interior to it and the traced map is
		 *         differentiable there (proof 4)
		 */
		private boolean insideOneComponent(LocusExistenceStructure2D structure, double lower,
				double upper) {
			List<double[]> pieces = new ArrayList<>();
			if (lower < domain.getLower()) {
				// Shifted by the binary64 period, below the real one: a larger image.
				pieces.add(new double[] {lower + period, domain.getUpper()});
				pieces.add(new double[] {domain.getLower(), upper});
			} else if (upper >= domain.getUpper()) {
				pieces.add(new double[] {lower, domain.getUpper()});
				pieces.add(new double[] {domain.getLower(), upper - period});
			} else {
				pieces.add(new double[] {lower, upper});
			}
			ContinuousComponent component = null;
			for (double[] piece : pieces) {
				double last = piece[1] >= domain.getUpper() ? Math.nextDown(domain.getUpper())
						: piece[1];
				for (double end : new double[] {piece[0], last}) {
					ContinuousComponent containing = structure.getComponentContaining(end);
					if (containing == null || component != null && containing != component) {
						return false;
					}
					component = containing;
				}
				for (Coverage coverage : structure.getCoverage()) {
					LocusInterval2D interval = coverage.getInterval();
					boolean overlaps = interval.getLower() <= last
							&& interval.getUpper() >= piece[0];
					if (overlaps && coverage.getState() != CoverageState.CERTIFIED_EXISTS) {
						return false;
					}
				}
			}
			return true;
		}

		/** Roots of equality predicates that exact certificates locate inside bands. */
		private List<CertifiedRoot> bandRoots() {
			List<CertifiedRoot> result = new ArrayList<>();
			for (Map.Entry<Target, Exact> entry : exact.entrySet()) {
				Target target = entry.getKey();
				if (entry.getValue() == null || !target.predicate.isEquality()) {
					continue;
				}
				for (Segment segment : entry.getValue().segments) {
					if (segment.root != null && insideBand(segment.lower, segment.upper)) {
						result.add(new CertifiedRoot(CertifiedRoot.RootKind.OPEN_EVENT,
								segment.root.canonicalLower, segment.root.canonicalUpper,
								source(target, segment.root.address), false,
								segment.root.multiplicity,
								LocusEvidenceNature2D.EXACT, true));
					}
				}
			}
			return result;
		}

		private boolean insideBand(double lower, double upper) {
			for (Cell cell : cells) {
				if (cell.state == State.BAND && cell.lower <= lower && upper <= cell.upper) {
					return true;
				}
			}
			for (int index = 0; index + 1 < cells.size(); index++) {
				Cell left = cells.get(index);
				Cell right = cells.get(index + 1);
				if (left.state == State.BAND && right.state == State.BAND
						&& left.lower <= lower && upper <= right.upper) {
					return true;
				}
			}
			return false;
		}

		private static boolean isInvalid(Cell cell) {
			return cell != null && cell.state == State.INVALID;
		}

		/**
		 * Collects the components: maximal runs of valid cells, on the circle for a circle
		 * driver.
		 *
		 * @return whether every component is maximal
		 */
		private boolean components(List<ContinuousComponent> components) {
			int size = cells.size();
			boolean allValid = true;
			for (Cell cell : cells) {
				allValid &= cell.state == State.VALID;
			}
			if (allValid) {
				LocusEvidenceNature2D nature = runNature(0, size - 1);
				if (circle) {
					LocusBoundary2D closure = new LocusBoundary2D(
							LocusBoundary2D.Kind.PERIODIC_CLOSURE, new LocusBoundarySource2D(
									"hybrid-certifier", "periodic-closure", "seam"), true,
							nature);
					components.add(ContinuousComponent.arc(domain, domain.getLower(),
							domain.getLower(), true, closure, closure, Maximality.ESTABLISHED,
							nature, "hybrid-component/0"));
				} else {
					components.add(ContinuousComponent.interval(domain, domainEnd(true),
							domainEnd(false), Maximality.ESTABLISHED, nature,
							"hybrid-component/0"));
				}
				return true;
			}
			// On the circle, start right after a cell that is not valid, so that no run is cut.
			int start = 0;
			if (circle) {
				while (cells.get(start).state == State.VALID) {
					start++;
				}
				start = (start + 1) % size;
			}
			boolean maximal = true;
			int index = start;
			for (int visited = 0; visited < size;) {
				if (cells.get(index).state != State.VALID) {
					index = (index + 1) % size;
					visited++;
					continue;
				}
				int first = index;
				int last = index;
				visited++;
				while (visited < size && (circle || last + 1 < size)
						&& cells.get((last + 1) % size).state == State.VALID) {
					last = (last + 1) % size;
					visited++;
				}
				maximal &= component(first, last, components);
				index = (last + 1) % size;
			}
			return maximal;
		}

		/** @return whether the component of the run of valid cells is maximal */
		private boolean component(int first, int last, List<ContinuousComponent> components) {
			int size = cells.size();
			int outside = (first - 1 + size) % size;
			if (circle && (last + 1) % size == outside && cells.get(outside).event) {
				// Every cell but one event: a full-length open arc that starts and ends at the
				// event, with its two distinct ends (section 15.3).
				Cell event = cells.get(outside);
				LocusBoundary2D lower = boundary(event, true);
				LocusBoundary2D upper = boundary(event, false);
				boolean maximal = lower.getKind() != LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE
						&& upper.getKind() != LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE;
				components.add(ContinuousComponent.arc(domain, event.eventParameter,
						event.eventParameter, true, lower, upper, maximal ? Maximality.ESTABLISHED
								: Maximality.LOCAL_CERTIFIED_SUBINTERVAL, runNature(first, last),
						"hybrid-component/" + components.size()));
				return maximal;
			}
			Cell firstCell = cells.get(first);
			Cell lastCell = cells.get(last);
			boolean seamFirst = circle && first == size - 1;
			boolean seamLast = circle && last == size - 1;
			double start = seamFirst ? domain.getLower() : firstCell.lower;
			double end;
			if (seamLast) {
				end = domain.getLower();
			} else if (circle && lastCell.upper >= domain.getUpper()) {
				end = Math.nextDown(domain.getUpper());
			} else {
				end = lastCell.upper;
			}
			LocusBoundary2D lower = boundary(circle || first > 0 ? neighbour(first, -1) : null,
					true);
			LocusBoundary2D upper = boundary(circle || last < size - 1 ? neighbour(last, 1)
					: null, false);
			boolean maximal = lower.getKind() != LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE
					&& upper.getKind() != LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE;
			LocusEvidenceNature2D nature = runNature(first, last);
			String key = "hybrid-component/" + components.size();
			Maximality maximality = maximal ? Maximality.ESTABLISHED
					: Maximality.LOCAL_CERTIFIED_SUBINTERVAL;
			if (circle) {
				if (start == end) {
					throw new IllegalStateException(
							"A component of positive length has no canonical extent");
				}
				components.add(ContinuousComponent.arc(domain, start, end, false, lower,
						upper, maximality, nature, key));
			} else {
				components.add(ContinuousComponent.interval(new LocusInterval2D(start, end,
						lower.isBinaryEndIncluded(), upper.isBinaryEndIncluded()), lower, upper,
						maximality, nature, key));
			}
			return maximal;
		}

		private LocusEvidenceNature2D runNature(int first, int last) {
			int size = cells.size();
			for (int index = first;; index = (index + 1) % size) {
				if (cells.get(index).nature == LocusEvidenceNature2D.RIGOROUS_INTERVAL) {
					return LocusEvidenceNature2D.RIGOROUS_INTERVAL;
				}
				if (index == last) {
					return LocusEvidenceNature2D.EXACT;
				}
			}
		}

		/** @return the end of a component next to a cell that is not valid */
		private LocusBoundary2D boundary(Cell next, boolean lower) {
			if (next == null) {
				return domainEnd(lower);
			}
			if (next.event) {
				LocusBoundary2D end = lower ? next.eventAbove : next.eventBelow;
				if (end == null) {
					throw new IllegalStateException(
							"A valid cell borders a structural event without its end");
				}
				return end;
			}
			String side = lower ? "lower" : "upper";
			switch (next.state) {
			case BOUNDARY:
				Root root = next.root;
				// A seam root is enclosed at -pi below a component that starts at the seam.
				boolean canonical = lower && circle && next == cells.get(cells.size() - 1);
				record(instrumentation, LocusCertificationWork2D.CERTIFIED_TRANSITION);
				transitions++;
				return LocusBoundary2D.enclosed(LocusBoundary2D.Kind.CERTIFIED_TRANSITION,
						source(root.target, side), root.target.predicate.isClosedAtRoot(),
						root.nature, canonical ? root.canonicalLower : root.lower,
						canonical ? root.canonicalUpper : root.upper);
			case BAND:
				return new LocusBoundary2D(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
						new LocusBoundarySource2D("hybrid-certifier", "unresolved-band",
								next.cause.name()), true, LocusEvidenceNature2D.NONE);
			default:
				throw new IllegalStateException(
						"A valid cell borders certified nonexistence without a root: "
								+ next.state);
			}
		}

		/** @return canonical coverage of the final cells */
		private List<Coverage> coverage() {
			List<Coverage> result = new ArrayList<>();
			int size = circle ? cells.size() - 1 : cells.size();
			Cell seam = circle ? cells.get(size) : null;
			for (int index = 0; index < size; index++) {
				Cell cell = cells.get(index);
				if (cell.event) {
					// An event holds no parameter of its own: the side whose end includes its
					// parameter owns it.
					continue;
				}
				double lower = cell.lower;
				double upper = Math.min(cell.upper, domain.getUpper());
				boolean lowerClosed;
				if (index == 0 && circle && seam.event) {
					lowerClosed = ownerAbove(seam);
					if (!lowerClosed) {
						add(result, cells.get(size - 1), lower, lower, true, true);
					}
				} else if (index == 0) {
					lowerClosed = circle ? priority(cell) >= priority(seam)
							: domain.isLowerClosed();
					if (circle && !lowerClosed) {
						add(result, seam, lower, lower, true, true);
					}
				} else {
					Cell previous = cells.get(index - 1);
					lowerClosed = previous.event ? ownerAbove(previous)
							: priority(cell) > priority(previous);
				}
				boolean upperClosed;
				if (index == size - 1) {
					upperClosed = domain.isUpperClosed();
				} else {
					Cell next = cells.get(index + 1);
					upperClosed = next.event ? !ownerAbove(next)
							: priority(cell) >= priority(next);
				}
				if (lower < upper || lower == upper && lowerClosed && upperClosed) {
					add(result, cell, lower, upper, lowerClosed, upperClosed);
				}
			}
			return result;
		}

		/**
		 * @return whether the parameter of an event belongs to the side above it: by the
		 *         inclusion of the end above, or, without one, unless the end below includes it
		 */
		private static boolean ownerAbove(Cell event) {
			return event.eventAbove != null ? event.eventAbove.isIncluded()
					: !(event.eventBelow != null && event.eventBelow.isIncluded());
		}

		private void add(List<Coverage> result, Cell cell, double lower, double upper,
				boolean lowerClosed, boolean upperClosed) {
			CoverageState state = coverageState(cell);
			LocusCoverageCause2D cause = coverageCause(cell);
			LocusEvidenceNature2D nature = cell.state == State.BAND
					? LocusEvidenceNature2D.NONE : cell.nature;
			String evidence = EVIDENCE + (state == CoverageState.CERTIFIED_EXISTS
					? "predicates-certified-true" : cell.evidence);
			if (!result.isEmpty()) {
				Coverage previous = result.get(result.size() - 1);
				LocusInterval2D interval = previous.getInterval();
				// One band per cause: the roots inside it are listed with the certificate.
				boolean mergeable = previous.getState() == state
						&& previous.getCause() == cause
						&& (state == CoverageState.CERTIFIED_EXISTS
								|| state == CoverageState.UNRESOLVED
								|| previous.getEvidence().equals(evidence)
										&& previous.getNature() == nature)
						&& interval.getUpper() == lower
						&& (interval.isUpperClosed() || lowerClosed);
				if (mergeable) {
					LocusEvidenceNature2D merged = previous.getNature() == nature ? nature
							: LocusEvidenceNature2D.RIGOROUS_INTERVAL;
					result.set(result.size() - 1, new Coverage(new LocusInterval2D(
							interval.getLower(), upper, interval.isLowerClosed(),
							upperClosed), state, cause, merged, previous.getEvidence()));
					return;
				}
			}
			result.add(new Coverage(new LocusInterval2D(lower, upper, lowerClosed,
					upperClosed), state, cause, nature, evidence));
		}

		private static CoverageState coverageState(Cell cell) {
			switch (cell.state) {
			case VALID:
				return CoverageState.CERTIFIED_EXISTS;
			case INVALID:
				return CoverageState.CERTIFIED_DOES_NOT_EXIST;
			case BOUNDARY:
				return CoverageState.CERTIFIED_BOUNDARY;
			case ISOLATED:
				return CoverageState.CERTIFIED_ISOLATED_POINTS;
			default:
				return CoverageState.UNRESOLVED;
			}
		}

		private static LocusCoverageCause2D coverageCause(Cell cell) {
			switch (cell.state) {
			case VALID:
				return LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE;
			case INVALID:
				// A predicate certified false, or a metric route certified undefined.
				return cell.cause == null ? LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE
						: cell.cause;
			case BOUNDARY:
				return LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE;
			case ISOLATED:
				return LocusCoverageCause2D.ISOLATED_EVEN_ROOT;
			default:
				return cell.cause == null ? LocusCoverageCause2D.NOT_CERTIFIED : cell.cause;
			}
		}

		/** Ownership of a shared end: the stronger certified state owns it. */
		private static int priority(Cell cell) {
			switch (cell.state) {
			case ISOLATED:
				return 4;
			case VALID:
				return 3;
			case INVALID:
				return 2;
			case BOUNDARY:
				return 1;
			default:
				return 0;
			}
		}

		private LocusBoundarySource2D source(Target target, String side) {
			return new LocusBoundarySource2D(operation(target.step), predicateToken(target),
					side);
		}
	}

	private static String predicateToken(Target target) {
		if (target.index == ConstructionBoxModel2D.STEP_PREDICATE) {
			return target.predicate.name();
		}
		if (target.index == ConstructionBoxModel2D.COINCIDENCE_BAND) {
			return target.predicate.name() + "-coincidence-band";
		}
		return target.predicate.name() + "-member-" + target.index;
	}

	private static String describe(Target target) {
		return "step" + target.step + ":" + predicateToken(target);
	}

	private static String describe(ConstructionBoxOutcome2D outcome) {
		return outcome == null ? "not-evaluated" : outcome.toString().replace('|', ';');
	}

	private static String fingerprint(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				hex.append(Character.forDigit((value >> 4) & 0xF, 16))
						.append(Character.forDigit(value & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}

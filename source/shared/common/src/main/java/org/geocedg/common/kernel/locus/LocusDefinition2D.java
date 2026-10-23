/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;

/** Immutable semantic definition published for one locus revision. */
public final class LocusDefinition2D {
	/** Diagnostic cause of a point in certified nonexistence (PRE-G9B-CU2, P5). */
	public static final String CERTIFIED_NONEXISTENCE = "CERTIFIED_NONEXISTENCE";
	/** Diagnostic of an evaluator point that the certificate contradicts. */
	public static final String EVALUATOR_DISAGREEMENT = "EVALUATOR_DISAGREEMENT";
	/** Diagnostic cause of a failed evaluation where existence is certified. */
	public static final String COMPUTATIONAL_FAILURE = "COMPUTATIONAL_FAILURE";
	private final String locusIdentity;
	private final long semanticRevision;
	private final DefinitionStatus definitionStatus;
	private final LocusDriverDomainProvider2D provider;
	private final List<LocusBranch2D> branches;
	private final Map<String, LocusBranch2D> branchesByKey;
	private final LocusEvaluator2D evaluator;
	private final Determinism determinism;
	private final String evaluatorSignature;
	private final LocusInstrumentation2D instrumentation;

	/** Creates a complete immutable semantic snapshot. */
	public LocusDefinition2D(String locusIdentity, long semanticRevision,
			DefinitionStatus definitionStatus, LocusDriverDomainProvider2D provider,
			List<LocusBranch2D> branches, LocusEvaluator2D evaluator,
			Determinism determinism, String evaluatorSignature,
			LocusInstrumentation2D instrumentation) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| semanticRevision < 1 || evaluatorSignature == null
				|| evaluatorSignature.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Stable identity, revision and evaluator signature required");
		}
		this.locusIdentity = locusIdentity;
		this.semanticRevision = semanticRevision;
		this.definitionStatus = Objects.requireNonNull(definitionStatus);
		this.provider = Objects.requireNonNull(provider);
		this.branches = immutableBranches(branches, semanticRevision);
		this.branchesByKey = indexBranches(this.branches);
		this.evaluator = Objects.requireNonNull(evaluator);
		this.determinism = Objects.requireNonNull(determinism);
		this.evaluatorSignature = evaluatorSignature;
		this.instrumentation = Objects.requireNonNull(instrumentation);
		if (definitionStatus == DefinitionStatus.VALID && this.branches.isEmpty()) {
			throw new IllegalArgumentException("A valid definition needs a branch");
		}
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public DefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	public LocusDriverDomainProvider2D getProvider() {
		return provider;
	}

	public List<LocusBranch2D> getBranches() {
		return branches;
	}

	/** @return branch for the exact stable key, or {@code null} */
	public LocusBranch2D getBranch(String branchKey) {
		return branchesByKey.get(branchKey);
	}

	public Determinism getDeterminism() {
		return determinism;
	}

	public LocusInstrumentation2D getInstrumentation() {
		return instrumentation;
	}

	/**
	 * Exposes optional semantic evaluator capabilities to kernel consumers.
	 * Consumers must still use {@link #evaluate(String, double,
	 * LocusEvaluationSession2D)} for ordinary point evaluation.
	 *
	 * @return immutable evaluator captured by this semantic revision
	 */
	public LocusEvaluator2D getEvaluatorCapability() {
		return evaluator;
	}

	/**
	 * Evaluates one semantic address through a scoped session.
	 *
	 * @return typed semantic evaluation
	 */
	public LocusEvaluation2D evaluate(String branchKey, double semanticParameter,
			LocusEvaluationSession2D session) {
		Objects.requireNonNull(session);
		LocusBranch2D branch = branchesByKey.get(branchKey);
		LocusEvaluation2D refusal = refusal(branchKey, branch);
		if (refusal != null) {
			return refusal;
		}
		return evaluateCanonical(branch, provider.canonicalize(semanticParameter), session);
	}

	/**
	 * Evaluates a provider-canonical parameter as given, for an evaluator of this package that
	 * composes this definition with its own provider, such as a similarity image: the
	 * canonicalization is not applied again, so that an included end of a component of the
	 * image is this definition evaluated at its own parameter ({@code
	 * locus-v2-existence-components.md} section 15.3; PRE-G9B-CU2, finding H-CU2A-6).
	 *
	 * @return typed semantic evaluation
	 */
	LocusEvaluation2D evaluateCanonicalParameter(String branchKey, double canonicalParameter,
			LocusEvaluationSession2D session) {
		Objects.requireNonNull(session);
		LocusBranch2D branch = branchesByKey.get(branchKey);
		LocusEvaluation2D refusal = refusal(branchKey, branch);
		if (refusal != null) {
			return refusal;
		}
		return evaluateCanonical(branch, canonicalParameter, session);
	}

	/**
	 * Evaluates a canonical parameter as given: the coverage and the membership of the
	 * existence structure decide it, and the canonicalization of the provider is not applied
	 * again.
	 */
	private LocusEvaluation2D evaluateCanonical(LocusBranch2D branch, double canonical,
			LocusEvaluationSession2D session) {
		LocusQuality2D quality = branch.getQuality();
		if (!provider.contains(canonical)) {
			return LocusEvaluation2D.invalid(EvaluationStatus.OUT_OF_DOMAIN, quality,
					"Parameter is outside the canonical branch domain");
		}
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		LocusExistenceStructure2D.Coverage coverage = structure.getCoverageAt(canonical);
		boolean certifiedNonexistence = coverage != null
				&& coverage.getState()
						== LocusExistenceStructure2D.CoverageState.CERTIFIED_DOES_NOT_EXIST
				&& coverage.getNature().certifies();
		if (!certifiedNonexistence && structure.getCompleteness()
				== LocusExistenceStructure2D.Completeness.COMPLETE
				&& !branch.containsValidParameter(canonical, provider)) {
			// A binary64 parameter never selects a point component, and says so with its
			// typed cause (section 15.8; PRE-G9B-CU2, finding H-CU2A-11).
			return LocusEvaluation2D.invalid(EvaluationStatus.OUT_OF_DOMAIN, quality,
					structure.getPointComponentEnclosing(canonical) != null
							? LocusSemanticPointCause2D.POINT_COMPONENT + ": the parameter lies"
									+ " in the enclosure of a point component, which a binary64"
									+ " parameter never selects"
							: "Parameter lies outside the complete existence domain");
		}
		return reconcile(branch, canonical, certifiedNonexistence ? coverage : null,
				session.evaluate(this, branch, canonical));
	}

	/**
	 * Evaluates one end of a certified component of a branch
	 * ({@code locus-v2-existence-components.md} section 15.3): the image of its canonical
	 * parameter when the binary64 end belongs to the component, otherwise its one-sided limit
	 * through the evaluator's explicit query. Canonicalization never stands in for an
	 * excluded end, which is the image of no canonical parameter.
	 *
	 * @param canonicalParameter canonical parameter of the end: the start or end of an arc,
	 *        or the lower or upper end of an interval
	 * @param upperEnd whether the end is the upper end of an interval or the end of an arc;
	 *        otherwise the lower end or the start
	 * @return typed evaluation of the end; {@code OUT_OF_DOMAIN} when no certified
	 *         component of the branch has that end
	 */
	public LocusEvaluation2D evaluateComponentEnd(String branchKey,
			double canonicalParameter, boolean upperEnd, LocusEvaluationSession2D session) {
		Objects.requireNonNull(session);
		LocusBranch2D branch = branchesByKey.get(branchKey);
		LocusEvaluation2D refusal = refusal(branchKey, branch);
		if (refusal != null) {
			return refusal;
		}
		LocusExistenceStructure2D.ContinuousComponent component = branch
				.getExistenceStructure().getComponentWithEnd(canonicalParameter, upperEnd);
		if (component == null) {
			return LocusEvaluation2D.invalid(EvaluationStatus.OUT_OF_DOMAIN,
					branch.getQuality(), "No certified component has that end");
		}
		LocusBoundary2D end = upperEnd ? component.getUpperBoundary()
				: component.getLowerBoundary();
		if (component.isClosedPeriodic()) {
			return evaluate(branchKey, canonicalParameter, session);
		}
		if (end.isBinaryEndIncluded()) {
			// The binary64 end belongs to the component (section 15.3). The periodic
			// canonicalization of the provider, lower + (t - lower), can move it by a few ulps
			// off an arc whose membership is exact, so the end is evaluated as given.
			return evaluateCanonical(branch, canonicalParameter, session);
		}
		instrumentation.recordEvaluatorCall(locusIdentity);
		try {
			LocusEvaluation2D result = evaluator.evaluateOneSidedLimit(this, branch,
					component, upperEnd, session);
			return result == null ? LocusEvaluation2D.invalid(
					EvaluationStatus.EVALUATION_FAILED, branch.getQuality(),
					LocusEvaluator2D.ONE_SIDED_LIMIT_NOT_AVAILABLE
							+ ": evaluator returned null") : result;
		} catch (RuntimeException exception) {
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), LocusEvaluator2D.ONE_SIDED_LIMIT_NOT_AVAILABLE + ": "
							+ exception.getClass().getSimpleName() + ": "
							+ exception.getMessage());
		}
	}

	/**
	 * Evaluates a parameter of the lifted range of one certified component, as consumers
	 * that traverse a component do (PRE-G9B-CU2): an end of the range that is the excluded
	 * end of a structural event is its one-sided limit ({@link #evaluateComponentEnd}),
	 * never the image of the canonical parameter that it shares with the other end; an
	 * included end of a component that is not closed periodic is its canonical end
	 * parameter, which the lifted end of an arc across the seam represents only up to
	 * rounding; every other parameter is evaluated as {@link #evaluate} does.
	 *
	 * @param componentExtent lifted interval of a component of the branch
	 * @return typed semantic evaluation
	 */
	public LocusEvaluation2D evaluateInComponent(String branchKey,
			LocusInterval2D componentExtent, double parameter,
			LocusEvaluationSession2D session) {
		LocusBranch2D branch = branchesByKey.get(branchKey);
		boolean lowerEnd = parameter == componentExtent.getLower();
		if (branch != null && (lowerEnd || parameter == componentExtent.getUpper())) {
			for (LocusExistenceStructure2D.ContinuousComponent component
					: branch.getExistenceStructure().getContinuousValidComponents()) {
				if (!component.getInterval().equals(componentExtent)) {
					continue;
				}
				LocusBoundary2D end = lowerEnd ? component.getLowerBoundary()
						: component.getUpperBoundary();
				if (!component.isClosedPeriodic() && (end.isBinaryEndIncluded()
						|| end.getKind() == LocusBoundary2D.Kind.STRUCTURAL_EVENT)) {
					return evaluateComponentEnd(branchKey,
							lowerEnd ? component.getStart() : component.getEnd(), !lowerEnd,
							session);
				}
				break;
			}
		}
		return evaluate(branchKey, parameter, session);
	}

	/**
	 * Encloses the image of a certified end of a component with the limit form of the
	 * certificate (PRE-G9B-CU2, certification design section 3.4), for a representation whose
	 * evaluation of the end fails. Only an enclosed certified transition whose exact root
	 * belongs to the component qualifies: an excluded end has no limit form, because its limit
	 * may not exist.
	 *
	 * @param end end of a certified component of the branch
	 * @return enclosed image, or an unavailable one with its typed reason
	 */
	public LocusBoundaryImage2D encloseBoundaryImage(String branchKey, LocusBoundary2D end) {
		if (end.getKind() != LocusBoundary2D.Kind.CERTIFIED_TRANSITION || !end.hasEnclosure()
				|| !end.isIncluded() || !end.getNature().certifies()) {
			return LocusBoundaryImage2D.notAvailable(LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE
					+ ": the end is not an included certified transition");
		}
		return image(branchKey, end.getEnclosureLower(), end.getEnclosureUpper(),
				end.getSource());
	}

	/**
	 * Encloses the image of a certified singular parameter with the limit form of the
	 * certificate, the exact vertex of a representation (consumption design section 5.2).
	 *
	 * @param singular certified singular parameter of the branch
	 * @return enclosed image, or an unavailable one with its typed reason
	 */
	public LocusBoundaryImage2D encloseSingularImage(String branchKey,
			LocusSingularParameter2D singular) {
		if (!singular.getNature().certifies()) {
			return LocusBoundaryImage2D.notAvailable(LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE
					+ ": the singular parameter is not certified");
		}
		return image(branchKey, singular.getParameterLower(), singular.getParameterUpper(),
				singular.getSource());
	}

	private LocusBoundaryImage2D image(String branchKey, double parameterLower,
			double parameterUpper, LocusBoundarySource2D source) {
		LocusBranch2D branch = branchesByKey.get(branchKey);
		LocusEvaluation2D refusal = refusal(branchKey, branch);
		if (refusal != null) {
			return LocusBoundaryImage2D.notAvailable(LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE
					+ ": " + refusal.getDiagnostic());
		}
		try {
			LocusBoundaryImage2D image = evaluator.encloseBoundaryImage(this, branch,
					parameterLower, parameterUpper, source);
			return image == null ? LocusBoundaryImage2D.notAvailable(
					LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE + ": evaluator returned null")
					: image;
		} catch (RuntimeException exception) {
			return LocusBoundaryImage2D.notAvailable(LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE
					+ ": " + exception.getClass().getSimpleName() + ": "
					+ exception.getMessage());
		}
	}

	/** @return refusal of every evaluation of the definition or branch, or {@code null} */
	private LocusEvaluation2D refusal(String branchKey, LocusBranch2D branch) {
		LocusQuality2D quality = branch == null ? LocusQuality2D.analyticDoubleSemantic()
				: branch.getQuality();
		if (definitionStatus != DefinitionStatus.VALID) {
			EvaluationStatus status = definitionStatus == DefinitionStatus.DRIVER_INVALID
					? EvaluationStatus.DEPENDENCY_UNDEFINED
					: definitionStatus == DefinitionStatus.UNSUPPORTED
							? EvaluationStatus.UNSUPPORTED_NONDETERMINISM
							: EvaluationStatus.OUT_OF_DOMAIN;
			return LocusEvaluation2D.invalid(status, quality,
					"Definition status: " + definitionStatus);
		}
		if (determinism == Determinism.UNSUPPORTED_NONDETERMINISM) {
			return LocusEvaluation2D.invalid(
					EvaluationStatus.UNSUPPORTED_NONDETERMINISM, quality,
					"Evaluator has no approved deterministic rule");
		}
		if (branch == null) {
			return LocusEvaluation2D.invalid(EvaluationStatus.OUT_OF_DOMAIN, quality,
					"Unknown branch key: " + branchKey);
		}
		return null;
	}

	/**
	 * Separates existence from evaluation ({@code locus-v2-existence-components.md}
	 * section 15.5): a point that the evaluator returns where the certificate establishes
	 * nonexistence is not published as valid, and a failure where the certificate
	 * establishes existence is computational. Facts without a certifying proof change
	 * nothing.
	 *
	 * @param nonexistence certified nonexistence that covers the parameter, or {@code null}
	 */
	private static LocusEvaluation2D reconcile(LocusBranch2D branch, double canonical,
			LocusExistenceStructure2D.Coverage nonexistence, LocusEvaluation2D result) {
		if (nonexistence != null) {
			return LocusEvaluation2D.invalid(EvaluationStatus.DEPENDENCY_UNDEFINED,
					result.getQuality(), CERTIFIED_NONEXISTENCE + ": "
							+ nonexistence.getCause() + (result.isValid()
									? "; " + EVALUATOR_DISAGREEMENT + ": the evaluator returned"
											+ " a point where the certificate establishes"
											+ " nonexistence"
									: "; evaluator: " + result.getStatus()));
		}
		if (result.isValid() || result.getStatus() == EvaluationStatus.EVALUATION_FAILED
				|| result.getStatus() == EvaluationStatus.UNSUPPORTED_NONDETERMINISM) {
			return result;
		}
		LocusExistenceStructure2D.ContinuousComponent component = branch
				.getExistenceStructure().getComponentContaining(canonical);
		if (component == null || !component.getNature().certifies()) {
			return result;
		}
		return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
				result.getQuality(), COMPUTATIONAL_FAILURE + ": existence is certified at the"
						+ " parameter; evaluator " + result.getStatus() + ": "
						+ result.getDiagnostic());
	}

	LocusEvaluation2D compute(LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session) {
		instrumentation.recordEvaluatorCall(locusIdentity);
		try {
			LocusEvaluation2D result = evaluator.evaluate(this, branch,
					canonicalParameter, session);
			return result == null ? LocusEvaluation2D.invalid(
					EvaluationStatus.EVALUATION_FAILED, branch.getQuality(),
					"Evaluator returned null") : result;
		} catch (RuntimeException exception) {
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), exception.getClass().getSimpleName() + ": "
							+ exception.getMessage());
		}
	}

	/**
	 * Compares semantic content while ignoring revision and mutable counters.
	 *
	 * @return whether both snapshots express identical semantic content
	 */
	public boolean hasSameSemanticContent(LocusDefinition2D other) {
		if (other == null || definitionStatus != other.definitionStatus
				|| determinism != other.determinism
				|| !provider.getSemanticSignature()
						.equals(other.provider.getSemanticSignature())
				|| !evaluatorSignature.equals(other.evaluatorSignature)
				|| branches.size() != other.branches.size()) {
			return false;
		}
		for (int index = 0; index < branches.size(); index++) {
			if (!branches.get(index).equals(other.branches.get(index))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Copies this content under a newly published revision.
	 *
	 * @return immutable snapshot with the supplied revision
	 */
	public LocusDefinition2D withRevision(long revision) {
		return new LocusDefinition2D(locusIdentity, revision, definitionStatus,
				provider, branches, evaluator, determinism, evaluatorSignature,
				instrumentation);
	}

	/**
	 * Rebinds immutable semantic content to a lifecycle-remapped durable identity.
	 * This changes no geometry, domain, revision or evaluator evidence.
	 *
	 * @return identity-refreshed snapshot
	 */
	public LocusDefinition2D withLocusIdentity(String identity) {
		if (locusIdentity.equals(identity)) {
			return this;
		}
		return new LocusDefinition2D(identity, semanticRevision, definitionStatus,
				provider, branches, evaluator, determinism, evaluatorSignature,
				instrumentation);
	}

	private static List<LocusBranch2D> immutableBranches(List<LocusBranch2D> input,
			long semanticRevision) {
		Objects.requireNonNull(input);
		ArrayList<LocusBranch2D> copy = new ArrayList<>();
		for (LocusBranch2D branch : input) {
			copy.add(Objects.requireNonNull(branch)
					.bindExistenceToRevision(semanticRevision));
		}
		return Collections.unmodifiableList(copy);
	}

	private static Map<String, LocusBranch2D> indexBranches(
			List<LocusBranch2D> input) {
		Map<String, LocusBranch2D> index = new LinkedHashMap<>();
		for (LocusBranch2D branch : input) {
			if (index.put(branch.getBranchKey(), branch) != null) {
				throw new IllegalArgumentException("Duplicate branch key");
			}
		}
		return Collections.unmodifiableMap(index);
	}
}

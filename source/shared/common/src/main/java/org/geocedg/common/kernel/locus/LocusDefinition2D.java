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
		this.branches = immutableBranches(branches);
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
	 * Evaluates one semantic address through a scoped session.
	 *
	 * @return typed semantic evaluation
	 */
	public LocusEvaluation2D evaluate(String branchKey, double semanticParameter,
			LocusEvaluationSession2D session) {
		Objects.requireNonNull(session);
		LocusBranch2D branch = branchesByKey.get(branchKey);
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
		double canonical = provider.canonicalize(semanticParameter);
		if (!branch.containsValidParameter(canonical, provider)) {
			return LocusEvaluation2D.invalid(EvaluationStatus.OUT_OF_DOMAIN, quality,
					"Parameter is outside the valid branch domain");
		}
		return session.evaluate(this, branch, canonical);
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

	private static List<LocusBranch2D> immutableBranches(List<LocusBranch2D> input) {
		Objects.requireNonNull(input);
		ArrayList<LocusBranch2D> copy = new ArrayList<>();
		for (LocusBranch2D branch : input) {
			copy.add(Objects.requireNonNull(branch));
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

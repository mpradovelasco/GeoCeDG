/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable typed inverse semantic-address result. */
public final class LocusPointInteractionResult2D {
	/** Diagnostic cause only; never a new admission permission. */
	public enum Reason {
		CERTIFIED_ABSENCE, UNIQUE, MULTIPLE, INVALID_SOURCE, DEGENERATE_SOURCE,
		UNSUPPORTED_CAPABILITY, GLOBAL_UNIQUENESS_NOT_ESTABLISHED,
		LOCAL_EVIDENCE_INSUFFICIENT, BUDGET_EXHAUSTED
	}
	/** Strength of whole-domain search evidence. */
	public enum SearchCoverage {
		ALL_CERTIFIED_AFFINE_COMPONENTS,
		ALL_EXPLICIT_POLYNOMIAL_SPANS,
		PARTIAL_EXPLICIT_POLYNOMIAL_SPANS,
		BOUNDED_EVALUATOR_SEARCH,
		NOT_APPLICABLE;

		/** @return whether every requested semantic component was searched */
		public boolean establishesCompleteRequestedScope() {
			return this == ALL_CERTIFIED_AFFINE_COMPONENTS
					|| this == ALL_EXPLICIT_POLYNOMIAL_SPANS;
		}
	}

	private final LocusPointInteractionStatus2D status;
	private final Reason reason;
	private final List<LocusPointInteractionCandidate2D> candidates;
	private final SearchCoverage searchCoverage;
	private final String diagnostic;
	private final LocusPointInteractionInstrumentationSnapshot2D instrumentation;

	/** Creates one complete query result. */
	public LocusPointInteractionResult2D(LocusPointInteractionStatus2D status,
			List<LocusPointInteractionCandidate2D> candidates,
			SearchCoverage searchCoverage, String diagnostic,
			LocusPointInteractionInstrumentationSnapshot2D instrumentation) {
		this(status, candidates, searchCoverage, diagnostic, instrumentation, defaultReason(status));
	}

	LocusPointInteractionResult2D(LocusPointInteractionStatus2D status,
			List<LocusPointInteractionCandidate2D> candidates,
			SearchCoverage searchCoverage, String diagnostic,
			LocusPointInteractionInstrumentationSnapshot2D instrumentation, Reason reason) {
		this.status = Objects.requireNonNull(status);
		this.reason = Objects.requireNonNull(reason);
		this.candidates = Collections.unmodifiableList(
				new ArrayList<>(Objects.requireNonNull(candidates)));
		this.searchCoverage = Objects.requireNonNull(searchCoverage);
		this.diagnostic = diagnostic == null ? "" : diagnostic;
		this.instrumentation = Objects.requireNonNull(instrumentation);
		if (status == LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
				&& this.candidates.size() != 1) {
			throw new IllegalArgumentException("Unique result needs one candidate");
		}
		if ((status == LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
				|| status
						== LocusPointInteractionStatus2D.NO_ADMISSIBLE_PREIMAGE)
				&& !searchCoverage.establishesCompleteRequestedScope()) {
			throw new IllegalArgumentException(
					"Definitive inverse result needs complete semantic coverage");
		}
	}

	private static Reason defaultReason(LocusPointInteractionStatus2D status) {
		return switch (status) {
		case NO_ADMISSIBLE_PREIMAGE -> Reason.CERTIFIED_ABSENCE;
		case UNIQUE_ADMISSIBLE_PREIMAGE -> Reason.UNIQUE;
		case MULTIPLE_SEMANTIC_PREIMAGES -> Reason.MULTIPLE;
		case INVALID_SOURCE -> Reason.INVALID_SOURCE;
		case DEGENERATE_SOURCE_IMAGE -> Reason.DEGENERATE_SOURCE;
		case UNSUPPORTED_CAPABILITY -> Reason.UNSUPPORTED_CAPABILITY;
		case UNRESOLVED_NUMERICAL_SEARCH -> Reason.LOCAL_EVIDENCE_INSUFFICIENT;
		};
	}

	public Reason getReason() {
		return reason;
	}

	public LocusPointInteractionStatus2D getStatus() {
		return status;
	}

	public List<LocusPointInteractionCandidate2D> getCandidates() {
		return candidates;
	}

	/** @return the unique candidate, otherwise {@code null} */
	public LocusPointInteractionCandidate2D getUniqueCandidate() {
		return status == LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
				? candidates.get(0) : null;
	}

	public SearchCoverage getSearchCoverage() {
		return searchCoverage;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	public LocusPointInteractionInstrumentationSnapshot2D getInstrumentation() {
		return instrumentation;
	}
}

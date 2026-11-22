/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.HashMap;
import java.util.Map;

/**
 * Memo of the nested semantic evaluations that the services of one reconstruction slice
 * request (decision DN-3 of the author, 2026-11-22; {@code locus-v2-semantics.md},
 * corrections after block 3B). It belongs to one evaluator instance, that is, to one
 * isolated copy built for one revision of one producer: it is never shared between
 * producers, between evaluators or between revisions, it holds immutable evaluation
 * results only and no geometric object, and it is unreachable when its evaluator is.
 *
 * <p>A key is the semantic key of the nested request: identity of the nested locus, its
 * semantic revision, its branch and the canonical parameter. Inside one evaluator
 * instance nothing else enters the result: the only input of the copy that changes
 * between requests is its driver, a nested locus whose definition follows that driver
 * publishes another revision, and a nested locus that only moves its own driver keeps its
 * revision and its function. A result that was computed while the evaluation context
 * reported a re-entry, an incoherent revision or an exhausted limit is never stored.</p>
 *
 * <p>The memo saves time and nothing else: a request that it answers is charged to the
 * work budget of its session as a request that is computed, so no classification depends
 * on what the memo holds.</p>
 */
public final class LocusNestedEvaluationMemo2D {

	/** How the memo behaves; see {@link #mode()}. */
	public enum Mode {
		/** Results are reused. */
		ON,
		/** No memo exists: every request is computed. The regression oracle. */
		OFF,
		/** Every request is computed and compared with the stored result. */
		VERIFY
	}

	/** Entries that one evaluator instance may hold; beyond it nothing more is stored. */
	static final int MAXIMUM_ENTRIES = 1 << 16;

	private static volatile Mode override;

	private final Map<LocusSemanticKey2D, LocusEvaluation2D> entries = new HashMap<>();
	private final boolean verifying;
	private long hits;
	private long verified;

	private LocusNestedEvaluationMemo2D(boolean verifying) {
		this.verifying = verifying;
	}

	/**
	 * The mode of the process: the test override when one is set, otherwise the system
	 * property {@code geocedg.nestedEvaluationMemo} or the environment variable {@code
	 * GEOCEDG_NESTED_EVALUATION_MEMO}, with the values {@code off} and {@code verify};
	 * anything else is {@link Mode#ON}.
	 *
	 * @return current mode
	 */
	public static Mode mode() {
		Mode forced = override;
		if (forced != null) {
			return forced;
		}
		String value = System.getProperty("geocedg.nestedEvaluationMemo");
		if (value == null) {
			value = System.getenv("GEOCEDG_NESTED_EVALUATION_MEMO");
		}
		if ("off".equalsIgnoreCase(value)) {
			return Mode.OFF;
		}
		return "verify".equalsIgnoreCase(value) ? Mode.VERIFY : Mode.ON;
	}

	/**
	 * Sets or clears the mode of the evaluators created from now on. It exists for the
	 * regression oracle and the verification of tests; the product reads the property.
	 *
	 * @param mode mode to force, or {@code null} to read the property again
	 */
	public static void overrideMode(Mode mode) {
		override = mode;
	}

	/** @return the memo of a new evaluator instance, or {@code null} when the mode is off */
	static LocusNestedEvaluationMemo2D create() {
		Mode current = mode();
		return current == Mode.OFF ? null
				: new LocusNestedEvaluationMemo2D(current == Mode.VERIFY);
	}

	/** @return the stored result of the request, or {@code null} */
	LocusEvaluation2D get(LocusSemanticKey2D key) {
		LocusEvaluation2D found = entries.get(key);
		if (found != null) {
			hits++;
		}
		return found;
	}

	/** Stores the result of a request that was computed with no context event. */
	void put(LocusSemanticKey2D key, LocusEvaluation2D result) {
		if (entries.size() < MAXIMUM_ENTRIES) {
			entries.put(key, result);
		}
	}

	/** @return whether a stored result is only compared, never returned */
	boolean isVerifying() {
		return verifying;
	}

	/** Compares a recomputed result with the stored one, in the verification mode. */
	void verify(LocusSemanticKey2D key, LocusEvaluation2D stored, LocusEvaluation2D computed) {
		verified++;
		if (!stored.equals(computed)) {
			throw new IllegalStateException("Nested evaluation memo mismatch at " + key
					+ ": stored " + stored + ", computed " + computed);
		}
	}

	/** @return requests that the memo answered, or compared in the verification mode */
	public long getHits() {
		return hits;
	}

	/** @return requests that were recomputed and compared in the verification mode */
	public long getVerified() {
		return verified;
	}

	/** @return results that the memo holds */
	public int size() {
		return entries.size();
	}
}

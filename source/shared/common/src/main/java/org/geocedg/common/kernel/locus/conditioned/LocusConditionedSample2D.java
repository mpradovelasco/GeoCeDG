/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One evaluation of the traced point with the testimony of its selecting dependencies
 * (candidate contract {@code post-r1-conditioned-use.md}, sections 3.2 and 5). A failure is
 * never an absence: the two outcomes are distinct and only an absence with an eligible
 * geometric cause can become conditioned absence.
 */
public final class LocusConditionedSample2D {
	/** Outcome of one evaluation, as the traversal reads it. */
	public enum Outcome {
		/** The traced point is defined. */
		VALID,
		/** The traced point is undefined with an eligible geometric cause. */
		ABSENT,
		/**
		 * The evaluation failed: a computational cause, a work limit, an authority refusal,
		 * an unreproduced selection or an unfaithful slice.
		 */
		FAILED
	}

	private final Outcome outcome;
	private final double x;
	private final double y;
	private final String cause;
	private final List<LocusSelectionTestimony2D> testimony;

	private LocusConditionedSample2D(Outcome outcome, double x, double y, String cause,
			List<LocusSelectionTestimony2D> testimony) {
		this.outcome = outcome;
		this.x = x;
		this.y = y;
		this.cause = cause;
		this.testimony = testimony;
	}

	/**
	 * @param x abscissa of the traced point
	 * @param y ordinate of the traced point
	 * @param testimony testimony of every selecting dependency that the slice read
	 * @return valid evaluation
	 */
	public static LocusConditionedSample2D valid(double x, double y,
			List<LocusSelectionTestimony2D> testimony) {
		if (!Double.isFinite(x) || !Double.isFinite(y)) {
			throw new IllegalArgumentException("A valid evaluation has finite coordinates");
		}
		ArrayList<LocusSelectionTestimony2D> copy = new ArrayList<>(
				Objects.requireNonNull(testimony));
		for (LocusSelectionTestimony2D each : copy) {
			Objects.requireNonNull(each);
		}
		return new LocusConditionedSample2D(Outcome.VALID, x, y, "",
				Collections.unmodifiableList(copy));
	}

	/**
	 * @param cause eligible geometric cause, from the closed list of the policy
	 * @return evaluation that states a geometric absence
	 */
	public static LocusConditionedSample2D absent(String cause) {
		return new LocusConditionedSample2D(Outcome.ABSENT, Double.NaN, Double.NaN,
				requireText(cause), Collections.emptyList());
	}

	/**
	 * @param cause computational cause, work limit, refusal or unreproduced selection
	 * @return failed evaluation, which is never evidence of absence
	 */
	public static LocusConditionedSample2D failed(String cause) {
		return new LocusConditionedSample2D(Outcome.FAILED, Double.NaN, Double.NaN,
				requireText(cause), Collections.emptyList());
	}

	/** @return outcome of the evaluation */
	public Outcome getOutcome() {
		return outcome;
	}

	/** @return whether the traced point is defined */
	public boolean isValid() {
		return outcome == Outcome.VALID;
	}

	/** @return abscissa of the traced point; {@link Double#NaN} unless valid */
	public double getX() {
		return x;
	}

	/** @return ordinate of the traced point; {@link Double#NaN} unless valid */
	public double getY() {
		return y;
	}

	/** @return cause of an absence or of a failure; empty when valid */
	public String getCause() {
		return cause;
	}

	/** @return testimony of every selecting dependency; empty unless valid */
	public List<LocusSelectionTestimony2D> getTestimony() {
		return testimony;
	}

	/** @return whether every selecting dependency has an established selection */
	public boolean hasEstablishedSelections() {
		for (LocusSelectionTestimony2D each : testimony) {
			if (!each.isEstablished()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param other another valid evaluation
	 * @return whether both evaluations read the same dependencies with the same selections
	 */
	public boolean hasSameSelections(LocusConditionedSample2D other) {
		if (testimony.size() != other.testimony.size()) {
			return false;
		}
		for (int i = 0; i < testimony.size(); i++) {
			LocusSelectionTestimony2D mine = testimony.get(i);
			LocusSelectionTestimony2D theirs = other.testimony.get(i);
			if (!mine.getOperationIdentity().equals(theirs.getOperationIdentity())
					|| !mine.getSelectionKey().equals(theirs.getSelectionKey())) {
				return false;
			}
		}
		return true;
	}

	private static String requireText(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("A cause is required");
		}
		return value;
	}
}

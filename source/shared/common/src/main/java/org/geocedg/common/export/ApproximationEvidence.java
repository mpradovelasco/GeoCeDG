/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

/** Immutable evidence for one export-only curve approximation. */
public final class ApproximationEvidence {

	/** Approved deterministic approximation method. */
	public enum Method {
		/** Oriented parameter-domain bisection with deterministic work limits. */
		ORIENTED_DYADIC_REFINEMENT
	}

	/** Strength of the reported approximation error evidence. */
	public enum Guarantee {
		/** A global error bound is justified by an approved proof contract. */
		CERTIFIED_ERROR_BOUND,
		/** Deterministic samples or derivatives establish an estimate only. */
		ESTIMATED_ERROR,
		/** Floating-point evidence is diagnostic and not certified. */
		FLOATING_POINT_UNCERTIFIED
	}

	private final Method method;
	private final double requestedTolerance;
	private final Double achievedError;
	private final Guarantee guarantee;
	private final long evaluations;
	private final long subdivisions;
	private final long segments;
	private final long vertices;
	private final int maximumDepth;

	/**
	 * Creates a complete deterministic approximation work record.
	 *
	 * @param method approved approximation method
	 * @param requestedTolerance requested positive model-coordinate tolerance
	 * @param achievedError achieved bound or estimate; null when not established
	 * @param guarantee strength of achieved-error evidence
	 * @param evaluations semantic evaluator calls
	 * @param subdivisions interval subdivisions
	 * @param segments emitted or candidate polyline segments
	 * @param vertices emitted or candidate vertices
	 * @param maximumDepth deepest deterministic subdivision level
	 */
	public ApproximationEvidence(Method method, double requestedTolerance,
			Double achievedError, Guarantee guarantee, long evaluations,
			long subdivisions, long segments, long vertices, int maximumDepth) {
		this.method = require(method, "method");
		assertPositive(requestedTolerance, "requested tolerance");
		if (achievedError != null) {
			assertNonNegative(achievedError, "achieved error");
			if (achievedError > requestedTolerance) {
				throw new IllegalArgumentException(
						"achieved error exceeds requested tolerance");
			}
		}
		this.requestedTolerance = requestedTolerance;
		this.achievedError = achievedError;
		this.guarantee = require(guarantee, "guarantee");
		assertNonNegative(evaluations, "evaluations");
		assertNonNegative(subdivisions, "subdivisions");
		assertNonNegative(segments, "segments");
		assertNonNegative(vertices, "vertices");
		assertNonNegative(maximumDepth, "maximum depth");
		this.evaluations = evaluations;
		this.subdivisions = subdivisions;
		this.segments = segments;
		this.vertices = vertices;
		this.maximumDepth = maximumDepth;
	}

	public Method getMethod() {
		return method;
	}

	public double getRequestedTolerance() {
		return requestedTolerance;
	}

	/** @return whether an achieved error estimate is present */
	public boolean hasAchievedError() {
		return achievedError != null;
	}

	public Double getAchievedError() {
		return achievedError;
	}

	public Guarantee getGuarantee() {
		return guarantee;
	}

	public long getEvaluations() {
		return evaluations;
	}

	public long getSubdivisions() {
		return subdivisions;
	}

	public long getSegments() {
		return segments;
	}

	public long getVertices() {
		return vertices;
	}

	public int getMaximumDepth() {
		return maximumDepth;
	}

	private static void assertPositive(double value, String name) {
		if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
			throw new IllegalArgumentException(name + " must be positive and finite");
		}
	}

	private static void assertNonNegative(double value, String name) {
		if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
			throw new IllegalArgumentException(
					name + " must be non-negative and finite");
		}
	}

	private static void assertNonNegative(long value, String name) {
		if (value < 0) {
			throw new IllegalArgumentException(name + " must be non-negative");
		}
	}

	private static <T> T require(T value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}

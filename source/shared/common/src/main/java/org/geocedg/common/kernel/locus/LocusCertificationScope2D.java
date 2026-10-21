/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Open scope of one existence-certificate computation (PRE-G9B-CU2, gates
 * GC-1 to GC-3 and GC-6).
 *
 * <p>A producer computes the existence certificate of a candidate revision
 * inside {@link #certify}; metric services report every value they compute or
 * hand out to the innermost open scope, so a certificate that reads a metric
 * value is observable through public counters. Like the evaluation session
 * frames, scopes are confined to the kernel thread.</p>
 */
public final class LocusCertificationScope2D {
	private static final Deque<LocusInstrumentation2D> OPEN = new ArrayDeque<>();
	/** Owner of the exploratory evaluations, which are never reads of a certificate. */
	private static final LocusInstrumentation2D EXPLORATION = new LocusInstrumentation2D();

	private LocusCertificationScope2D() {
		// Utility class.
	}

	/**
	 * Records one certificate computation for {@code owner} and runs it inside
	 * a scope owned by that instrumentation.
	 *
	 * @param owner instrumentation of the locus whose certificate is computed
	 * @param computation certificate computation
	 * @return computation result
	 */
	public static <T> T certify(LocusInstrumentation2D owner,
			Supplier<T> computation) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(computation);
		owner.recordCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION);
		OPEN.push(owner);
		try {
			return computation.get();
		} finally {
			OPEN.pop();
		}
	}

	/**
	 * Records one metric value computed or handed out while a certificate is
	 * computed; outside a scope it records nothing.
	 */
	public static void recordMetricRead() {
		LocusInstrumentation2D owner = OPEN.peek();
		if (owner != null && owner != EXPLORATION) {
			owner.recordCertificationWork(
					LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION);
		}
	}

	/**
	 * Runs evaluations that explore the unresolved bands of a certificate: exploratory
	 * evidence never certifies ({@code locus-v2-existence-components.md} section 15.1), so
	 * the metric values it computes are not reads of the certificate (gate GC-6).
	 *
	 * @param exploration exploratory evaluations
	 * @return exploration result
	 */
	public static <T> T explore(Supplier<T> exploration) {
		Objects.requireNonNull(exploration);
		OPEN.push(EXPLORATION);
		try {
			return exploration.get();
		} finally {
			OPEN.pop();
		}
	}

	/** @return whether a certificate computation is open on this thread */
	public static boolean isOpen() {
		return !OPEN.isEmpty() && OPEN.peek() != EXPLORATION;
	}
}

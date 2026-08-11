/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Explicit DUAL diagnostic. Legacy points remain sampled evidence and never
 * become a provider, branch, domain, cache or semantic authority.
 */
public final class LocusDualRunDiagnostic2D {
	/** One explicitly parameter-associated legacy evidence point. */
	public static final class SampleEvidence {
		private final String branchKey;
		private final double semanticParameter;
		private final LocusPoint2D legacySample;

		/** Creates explicitly addressed legacy sampled evidence. */
		public SampleEvidence(String branchKey, double semanticParameter,
				LocusPoint2D legacySample) {
			this.branchKey = branchKey;
			this.semanticParameter = semanticParameter;
			this.legacySample = legacySample;
		}
	}

	/** One comparison result; its tolerance is validation-only. */
	public static final class Comparison {
		private final SampleEvidence evidence;
		private final LocusEvaluation2D semanticEvaluation;
		private final double distance;
		private final boolean withinEnvelope;

		private Comparison(SampleEvidence evidence,
				LocusEvaluation2D semanticEvaluation, double distance,
				boolean withinEnvelope) {
			this.evidence = evidence;
			this.semanticEvaluation = semanticEvaluation;
			this.distance = distance;
			this.withinEnvelope = withinEnvelope;
		}

		public SampleEvidence getEvidence() {
			return evidence;
		}

		public LocusEvaluation2D getSemanticEvaluation() {
			return semanticEvaluation;
		}

		public double getDistance() {
			return distance;
		}

		public boolean isWithinEnvelope() {
			return withinEnvelope;
		}
	}

	private LocusDualRunDiagnostic2D() {
		// Static diagnostic.
	}

	/**
	 * Compares explicitly associated evidence without reading any render cache.
	 *
	 * @return immutable diagnostic comparisons
	 */
	public static List<Comparison> compare(GeoSemanticLocusView locus,
			List<SampleEvidence> evidence, double characteristicScale) {
		double envelope = LocusValidationTolerance2D
				.evaluationEnvelope(characteristicScale);
		LocusEvaluationSession2D session = LocusEvaluationSession2D
				.memoizing(Math.max(1, evidence.size()));
		List<Comparison> comparisons = new ArrayList<>();
		for (SampleEvidence sample : evidence) {
			LocusEvaluation2D semantic = locus.evaluate(sample.branchKey,
					sample.semanticParameter, session);
			double distance = Double.POSITIVE_INFINITY;
			if (semantic.isValid()) {
				double dx = semantic.getPoint().getX() - sample.legacySample.getX();
				double dy = semantic.getPoint().getY() - sample.legacySample.getY();
				distance = Math.hypot(dx, dy);
			}
			comparisons.add(new Comparison(sample, semantic, distance,
					distance <= envelope));
		}
		return Collections.unmodifiableList(comparisons);
	}

	/** Minimal read-only adapter keeps the diagnostic independent of GeoElement. */
	public interface GeoSemanticLocusView {
		/** @return semantic evaluation at an explicit branch and parameter */
		LocusEvaluation2D evaluate(String branchKey, double parameter,
				LocusEvaluationSession2D session);
	}
}

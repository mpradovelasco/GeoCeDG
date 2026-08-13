/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import org.geocedg.common.locus.G7AMetricSemanticModel.AbsentMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.ConstructionFidelity;
import org.geocedg.common.locus.G7AMetricSemanticModel.EvaluatorMethod;
import org.geocedg.common.locus.G7AMetricSemanticModel.FiniteMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.LocusMetricResult2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricComputationStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricCoverage;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorEvidence2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricMethod;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricRectifiability;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.RepresentationRole;
import org.junit.jupiter.api.Test;

/** Test-private publication/lifecycle candidate; not a productive GeoElement. */
class G7AMetricGeoLifecycleCharacterizationTest {

	@Test
	void creationIsUndefinedUntilOneAtomicRichSnapshotIsPublished() {
		GeoCandidate candidate = new GeoCandidate("construction-a");
		assertFalse(candidate.isDefined());
		candidate.publish(1, success(3));
		assertTrue(candidate.isDefined());
		assertEquals(3, candidate.result().finiteValue().orElseThrow(), 0);
		assertEquals(1, candidate.semanticRevision());
	}

	@Test
	void richFailureSnapshotIsDefinedButNeverScalarAdmissible() {
		GeoCandidate candidate = new GeoCandidate("construction-a");
		candidate.publish(1, failure());
		assertTrue(candidate.isDefined());
		assertTrue(candidate.result().richDefined());
		assertFalse(candidate.result().scalarAdmissible());
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				candidate.result().computationStatus());
	}

	@Test
	void copyAndSetCannotImportForeignRevisionBindingOrPartialCurrentState() {
		GeoCandidate source = new GeoCandidate("construction-a");
		source.publish(9, success(4));
		GeoCandidate copy = source.copyInternal("construction-b");
		assertEquals("construction-b", copy.constructionToken());
		assertFalse(copy.isDefined());
		assertEquals(0, copy.semanticRevision());
		assertNull(copy.result());

		GeoCandidate assigned = new GeoCandidate("construction-a");
		assigned.set(source);
		assertFalse(assigned.isDefined());
		assertEquals("explicit recompute required after set/copy",
				assigned.diagnostic());
	}

	@Test
	void invalidationRemovalAndUndoRedoRecoveryNeverExposeStaleSnapshot() {
		GeoCandidate candidate = new GeoCandidate("construction-a");
		candidate.publish(1, success(2));
		candidate.invalidate("branch disappeared at revision 2");
		assertFalse(candidate.isDefined());
		assertNull(candidate.result());
		candidate.publish(2, success(5));
		assertTrue(candidate.isDefined());
		assertEquals(5, candidate.result().finiteValue().orElseThrow(), 0);

		candidate.remove();
		assertFalse(candidate.isDefined());
		assertThrows(IllegalStateException.class,
				() -> candidate.publish(3, success(7)));
	}

	@Test
	void failedRevisionPublishesCoherentCurrentFailureWithoutStaleSuccess() {
		GeoCandidate candidate = new GeoCandidate("construction-a");
		candidate.publish(1, success(2));
		candidate.publishAtomically(2, () -> {
			throw new IllegalArgumentException("injected build failure");
		});
		assertEquals(2, candidate.semanticRevision());
		assertTrue(candidate.isDefined());
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				candidate.result().computationStatus());
		assertTrue(candidate.result().finiteValue().isEmpty());
		assertFalse(candidate.result().scalarAdmissible());
		assertEquals("IllegalArgumentException: injected build failure",
				candidate.diagnostic());
		assertFalse(candidate.publicationActive());
	}

	private static LocusMetricResult2D success(double value) {
		return result(new FiniteMetricValue2D(value),
				MetricComputationStatus.SUCCESS, MetricCoverage.COMPLETE);
	}

	private static LocusMetricResult2D failure() {
		return result(new AbsentMetricValue2D(),
				MetricComputationStatus.NUMERICAL_FAILURE,
				MetricCoverage.INCOMPLETE);
	}

	private static LocusMetricResult2D result(MetricValue2D value,
			MetricComputationStatus status,
			MetricCoverage coverage) {
		MetricErrorEvidence2D errorEvidence = value.finiteValue().isPresent()
				? MetricErrorEvidence2D.estimated(1e-9,
						OptionalDouble.of(1e-9), "lifecycle fixture",
						List.of("test-private estimate"))
				: MetricErrorEvidence2D.notApplicable("failed computation");
		return new LocusMetricResult2D(value, coverage, status,
				MetricRectifiability.UNDETERMINED, Optional.empty(),
				ConstructionFidelity.SEMANTIC_NUMERICAL_EVALUATION,
				EvaluatorMethod.DIFFERENTIAL,
				MetricMethod.ADAPTIVE_DIFFERENTIAL_QUADRATURE,
				RepresentationRole.SEMANTIC_METRIC,
				errorEvidence,
				"construction-unit", "lifecycle fixture", List.of(), List.of());
	}

	private interface ResultSupplier {
		LocusMetricResult2D get();
	}

	private static final class GeoCandidate {
		private final String constructionToken;
		private LocusMetricResult2D result;
		private long semanticRevision;
		private boolean publicationActive;
		private boolean removed;
		private String diagnostic = "awaiting first publication";

		GeoCandidate(String constructionToken) {
			this.constructionToken = constructionToken;
		}

		void publish(long revision, LocusMetricResult2D newResult) {
			publishAtomically(revision, () -> newResult);
		}

		void publishAtomically(long revision, ResultSupplier supplier) {
			if (removed) {
				throw new IllegalStateException("removed result cannot recover");
			}
			if (revision <= semanticRevision) {
				throw new IllegalArgumentException("revision must increase");
			}
			publicationActive = true;
			result = null;
			semanticRevision = revision;
			diagnostic = "recomputing current revision";
			try {
				LocusMetricResult2D built = supplier.get();
				if (built == null) {
					throw new IllegalArgumentException("null result");
				}
				result = built;
				diagnostic = "";
			} catch (RuntimeException exception) {
				result = failure();
				diagnostic = exception.getClass().getSimpleName() + ": "
						+ exception.getMessage();
			} finally {
				publicationActive = false;
			}
		}

		GeoCandidate copyInternal(String targetConstruction) {
			return new GeoCandidate(targetConstruction);
		}

		void set(GeoCandidate source) {
			result = null;
			semanticRevision = 0;
			diagnostic = "explicit recompute required after set/copy";
		}

		void invalidate(String reason) {
			result = null;
			diagnostic = reason;
		}

		void remove() {
			result = null;
			removed = true;
			diagnostic = "removed";
		}

		boolean isDefined() {
			return !removed && result != null;
		}

		String constructionToken() {
			return constructionToken;
		}

		LocusMetricResult2D result() {
			return result;
		}

		long semanticRevision() {
			return semanticRevision;
		}

		String diagnostic() {
			return diagnostic;
		}

		boolean publicationActive() {
			return publicationActive;
		}
	}
}

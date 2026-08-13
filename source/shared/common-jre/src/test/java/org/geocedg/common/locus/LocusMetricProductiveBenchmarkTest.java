/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexStatistics2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricMultiplicityPolicy;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.junit.jupiter.api.Test;

/** Functional repeated-query and invalidation budgets; timing is informational. */
class LocusMetricProductiveBenchmarkTest {
	private final LocusMetricEngine2D engine = new LocusMetricEngine2D();

	@Test
	void oneTenAndHundredSameQueriesBuildOncePerFreshTrace() {
		for (int queries : List.of(1, 10, 100)) {
			Trace trace = trace(line("repeat", 1, false,
					G7BMetricFixtures.components(-4, 4)));
			try (LocusMetricOwnerLease2D lease =
					trace.owner.acquireLease()) {
				BetweenPositionsMetricQuery query =
						G7BMetricFixtures.between(trace.definition, -3, 3,
								TraversalDirection.FORWARD,
								OpenBoundaryPolicy.STRICT,
								SamePositionPolicy.ZERO_LENGTH);
				final long started = System.nanoTime();
				long firstQueryNanos = 0;
				for (int index = 0; index < queries; index++) {
					long queryStarted = System.nanoTime();
					trace.compute(query, "same-consumer");
					if (index == 0) {
						firstQueryNanos = System.nanoTime() - queryStarted;
					}
				}
				final long elapsedNanos = System.nanoTime() - started;
				LocusMetricIndexStatistics2D statistics =
						trace.owner.statistics();
				LocusMetricInstrumentationSnapshot2D counters =
						trace.instrumentation.snapshot();
				assertEquals(1, statistics.getBuilds());
				assertEquals(queries - 1,
						statistics.getHits());
				assertEquals(1, counters.getComponentStateBuilds());
				assertEquals(0, counters.getEvaluatorCalls());
				assertEquals(0, counters.getDerivativeCalls());
				assertEquals(0, counters.getIntegratorCalls());
				assertEquals(0, counters.getSubdivisions());
				assertEquals(1, counters.getIndexMisses());
				assertEquals(queries - 1, counters.getIndexHits());
				assertEquals(0, counters.getCrossResultHits());
				assertEquals(0, counters.getDuplicateCompatibleBuilds());
				assertEquals(0, statistics.getEvictions());
				assertEquals(1, statistics.getRetainedEntries());
				assertTrue(statistics.getApproximateRetainedBytes() > 0);
				assertEquals(0, statistics.getInvalidations());
				System.out.println("G7B_TRACE kind=same queries=" + queries
						+ " evaluatorCalls=" + counters.getEvaluatorCalls()
						+ " derivativeCalls=" + counters.getDerivativeCalls()
						+ " integratorCalls=" + counters.getIntegratorCalls()
						+ " subdivisions=" + counters.getSubdivisions()
						+ " builds=" + statistics.getBuilds()
						+ " hits=" + statistics.getHits()
						+ " misses=" + statistics.getMisses()
						+ " crossResultHits=" + counters.getCrossResultHits()
						+ " duplicateBuilds="
						+ counters.getDuplicateCompatibleBuilds()
						+ " evictions=" + statistics.getEvictions()
						+ " retainedEntries="
						+ statistics.getRetainedEntries()
						+ " retainedBytes="
						+ statistics.getApproximateRetainedBytes()
						+ " invalidations=" + statistics.getInvalidations()
						+ " firstQueryNanos=" + firstQueryNanos
						+ " warmQueriesNanos="
						+ Math.max(0, elapsedNanos - firstQueryNanos));
			}
		}
	}

	@Test
	void overlappingReversePeriodicStopAndWrapReuseOneCompleteKey() {
		Trace open = trace(line("routes", 1, false,
				G7BMetricFixtures.components(-4, 4)));
		try (LocusMetricOwnerLease2D lease = open.owner.acquireLease()) {
			for (int index = 0; index < 20; index++) {
				double offset = index / 100.0;
				open.compute(G7BMetricFixtures.between(open.definition,
						-3 + offset, 2 + offset,
						TraversalDirection.FORWARD,
						OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH), "overlap");
				open.compute(G7BMetricFixtures.between(open.definition,
						2 + offset, -3 + offset,
						TraversalDirection.REVERSE,
						OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH), "reverse");
			}
			LocusMetricResult2D stop = open.compute(
					G7BMetricFixtures.between(open.definition, 2, -2,
							TraversalDirection.FORWARD,
							OpenBoundaryPolicy.STOP_AT_END,
							SamePositionPolicy.ZERO_LENGTH), "stop");
			LocusMetricResult2D wrap = open.compute(
					G7BMetricFixtures.between(open.definition, 2, -2,
							TraversalDirection.FORWARD,
							OpenBoundaryPolicy.WRAP_TO_START,
							SamePositionPolicy.ZERO_LENGTH), "wrap");
			assertEquals(2, stop.getMetricValue().getFiniteValue()
					.orElseThrow(), 0);
			assertEquals(MetricCoverage.INCOMPLETE, stop.getCoverage());
			assertEquals(TraversalOutcome.STOPPED_AT_BOUNDARY,
					stop.getTraversalOutcome().orElseThrow());
			assertFalse(stop.isScalarAdmissible());
			assertEquals(4, wrap.getMetricValue().getFiniteValue()
					.orElseThrow(), 0);
			assertEquals(MetricCoverage.COMPLETE, wrap.getCoverage());
			assertEquals(TraversalOutcome.WRAPPED_TO_START,
					wrap.getTraversalOutcome().orElseThrow());
			assertTrue(wrap.isScalarAdmissible());
			assertEquals(1, open.owner.statistics().getBuilds());
		}

		Trace periodic = trace(line("periodic-route", 1, true,
				G7BMetricFixtures.components(-4, 4)));
		try (LocusMetricOwnerLease2D lease =
				periodic.owner.acquireLease()) {
			for (int index = 0; index < 100; index++) {
				periodic.compute(G7BMetricFixtures.between(
						periodic.definition, 3, -3,
						TraversalDirection.FORWARD,
						OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH), "periodic");
			}
			assertEquals(1, periodic.owner.statistics().getBuilds());
			assertEquals(99, periodic.owner.statistics().getHits());
		}
	}

	@Test
	void strictTracePerformsZeroMetricWork() {
		Trace trace = trace(line("strict-trace", 1, false,
				G7BMetricFixtures.components(-4, 4)));
		try (LocusMetricOwnerLease2D lease = trace.owner.acquireLease()) {
			for (int index = 0; index < 100; index++) {
				LocusMetricResult2D result = trace.compute(
						G7BMetricFixtures.between(trace.definition, 2, -2,
								TraversalDirection.FORWARD,
								OpenBoundaryPolicy.STRICT,
								SamePositionPolicy.ZERO_LENGTH), "strict");
				assertEquals(MetricComputationStatus.INVALID_QUERY,
						result.getComputationStatus());
			}
			assertEquals(0, trace.owner.statistics().getBuilds());
			assertEquals(0, trace.owner.statistics().getMisses());
		}
	}

	@Test
	void repeatedTotalOverThreeComponentsBuildsExactlyThreeStates() {
		Trace trace = trace(line("three-components", 1, false,
				G7BMetricFixtures.components(-4, -3, -1, 1, 3, 4)));
		try (LocusMetricOwnerLease2D lease = trace.owner.acquireLease()) {
			TotalLocusMetricQuery query = new TotalLocusMetricQuery(
					trace.definition.getLocusIdentity(), 1,
					LocusMetricPolicy2D.initial());
			for (int index = 0; index < 100; index++) {
				LocusMetricResult2D result = trace.compute(query, "total");
				assertEquals(4, result.getMetricValue().getFiniteValue()
						.orElseThrow(), 0);
			}
			assertEquals(3, trace.owner.statistics().getBuilds());
			assertEquals(297, trace.owner.statistics().getHits());
			assertEquals(3, trace.owner.statistics().getRetainedEntries());
		}
	}

	@Test
	void totalFirstAndLocalFirstBuildSameThreeUniqueComponents() {
		assertEquals(3, mixedOrder(false));
		assertEquals(3, mixedOrder(true));
	}

	@Test
	void policyChangeAndRevisionInvalidationForceOneNecessaryRebuild() {
		LocusDefinition2D revisionOne = line("invalidate", 1, false,
				G7BMetricFixtures.components(-4, 4));
		Trace trace = trace(revisionOne);
		try (LocusMetricOwnerLease2D lease = trace.owner.acquireLease()) {
			trace.compute(totalQuery(revisionOne, LocusMetricPolicy2D.initial()),
					"base");
			LocusMetricPolicy2D changed = changedPolicy();
			trace.compute(totalQuery(revisionOne, changed), "policy");
			assertEquals(2, trace.owner.statistics().getBuilds());
			assertEquals(2, trace.owner.statistics().getRetainedEntries());

			LocusDefinition2D revisionTwo = line("invalidate", 2, false,
					G7BMetricFixtures.components(-4, 4));
			trace.owner.invalidateObsoleteRevision(2);
			LocusMetricResult2D result = engine.compute(totalQuery(revisionTwo,
					LocusMetricPolicy2D.initial()), revisionTwo,
					trace.capabilities, trace.owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					trace.instrumentation, "revised");
			assertEquals(8, result.getMetricValue().getFiniteValue()
					.orElseThrow(), 0);
			assertEquals(3, trace.owner.statistics().getBuilds());
			assertEquals(1, trace.owner.statistics().getRetainedEntries());
			assertTrue(trace.owner.statistics().getInvalidations() >= 2);
		}
	}

	@Test
	void lookAlikeLociNeverShareAcrossDedicatedOwners() {
		Trace first = trace(line("same-id", 1, false,
				G7BMetricFixtures.components(-4, 4)));
		Trace second = trace(line("same-id", 1, false,
				G7BMetricFixtures.components(-4, 4)));
		try (LocusMetricOwnerLease2D firstLease =
					first.owner.acquireLease();
				LocusMetricOwnerLease2D secondLease =
						second.owner.acquireLease()) {
			first.compute(totalQuery(first.definition,
					LocusMetricPolicy2D.initial()), "A");
			second.compute(totalQuery(second.definition,
					LocusMetricPolicy2D.initial()), "B");
			assertEquals(1, first.owner.statistics().getBuilds());
			assertEquals(1, second.owner.statistics().getBuilds());
		}
	}

	private int mixedOrder(boolean totalFirst) {
		Trace trace = trace(line("order-" + totalFirst, 1, false,
				G7BMetricFixtures.components(-4, -3, -1, 1, 3, 4)));
		try (LocusMetricOwnerLease2D lease = trace.owner.acquireLease()) {
			if (totalFirst) {
				trace.compute(totalQuery(trace.definition,
						LocusMetricPolicy2D.initial()), "total");
			}
			for (double[] arc : List.of(new double[] {-3.8, -3.2},
					new double[] {-0.8, 0.8},
					new double[] {3.2, 3.8})) {
				trace.compute(G7BMetricFixtures.between(trace.definition,
						arc[0], arc[1], TraversalDirection.FORWARD,
						OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH), "local");
			}
			if (!totalFirst) {
				trace.compute(totalQuery(trace.definition,
						LocusMetricPolicy2D.initial()), "total");
			}
			return (int) trace.owner.statistics().getBuilds();
		}
	}

	private Trace trace(LocusDefinition2D definition) {
		return new Trace(definition,
				G7BMetricFixtures.analytic(1, "line/v1"));
	}

	private static LocusDefinition2D line(String identity, long revision,
			boolean periodic, List<LocusInterval2D> components) {
		return G7BMetricFixtures.definition(identity, revision, periodic,
				components, parameter -> new LocusPoint2D(parameter, 0));
	}

	private static TotalLocusMetricQuery totalQuery(
			LocusDefinition2D definition, LocusMetricPolicy2D policy) {
		return new TotalLocusMetricQuery(definition.getLocusIdentity(),
				definition.getSemanticRevision(), policy);
	}

	private static LocusMetricPolicy2D changedPolicy() {
		LocusMetricPolicy2D base = LocusMetricPolicy2D.initial();
		return new LocusMetricPolicy2D(base.getAbsoluteTolerance() / 10,
				base.getRelativeTolerance(), base.getWorkBudget(),
				base.getMetricAlgorithmVersion(), "changed-policy/v2",
				"changed-tolerance/v2",
				MetricMultiplicityPolicy.CONSTRUCTIVE_TRAVERSAL_LENGTH,
				base.getImproperLimitPolicy(),
				base.getEvaluatorOnlyPolicy());
	}

	private final class Trace {
		private final LocusDefinition2D definition;
		private final LocusMetricCapabilityHierarchy2D capabilities;
		private final LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		private final LocusMetricSharedOwner2D owner;

		private Trace(LocusDefinition2D definition,
				LocusMetricCapabilityHierarchy2D capabilities) {
			this.definition = definition;
			this.capabilities = capabilities;
			this.owner = new LocusMetricSharedOwner2D(
					definition.getLocusIdentity(), instrumentation);
		}

		private LocusMetricResult2D compute(
				org.geocedg.common.kernel.locus.metric.LocusMetricQuery2D query,
				String consumer) {
			return engine.compute(query, definition, capabilities, owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					instrumentation, consumer);
		}
	}
}

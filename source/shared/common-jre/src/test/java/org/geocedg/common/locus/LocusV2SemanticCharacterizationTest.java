/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Executable mathematical fixtures used to review the G6A contract.
 *
 * <p>All types are private test fixtures. They are deliberately not a kernel
 * API or a prototype of the productive G6B classes.</p>
 */
class LocusV2SemanticCharacterizationTest {

	@Test
	void topologyFixtureSeparatesBranchIdentityFromValidComponents() {
		TopologyState one = topology(0, 1);
		TopologyState split = topology(0.25, 1);
		TopologyState isolated = topology(1, 1);
		TopologyState empty = topology(2, 1);
		TopologyState reappeared = topology(0, 1);

		assertEquals("fixture.sheet.main", one.branchKey());
		assertEquals(one.branchKey(), split.branchKey());
		assertEquals(one.branchKey(), isolated.branchKey());
		assertEquals(one.branchKey(), empty.branchKey());
		assertEquals(one.branchKey(), reappeared.branchKey());
		assertEquals(1, one.components().size());
		assertEquals(2, split.components().size());
		assertEquals(2, isolated.components().size());
		assertTrue(isolated.components().stream().allMatch(Interval::isolated));
		assertTrue(empty.components().isEmpty());
		assertEquals(BranchLifecycle.UNCHANGED,
				transition(one, split).branchLifecycle());
		assertEquals(DomainTopology.SPLIT,
				transition(one, split).domainTopology());
		assertEquals(BranchLifecycle.DISAPPEARED,
				transition(isolated, empty).branchLifecycle());
		assertEquals(BranchLifecycle.APPEARED,
				transition(empty, reappeared).branchLifecycle());
	}

	@Test
	void nestedSemanticCompositionScalesWithQueriesTimesDepth() {
		List<Double> queries = List.of(-1.0, -0.5, 0.0, 0.5, 1.0);
		for (int depth : List.of(1, 2, 3, 5)) {
			Counter counter = new Counter();
			Evaluator recursive = recursiveChain(depth, counter);
			List<Double> recursiveResults = evaluateAll(recursive, queries, null);
			assertEquals(queries.size() * depth, counter.calls());

			Counter flattenedCounter = new Counter();
			Evaluator flattened = flattenedChain(depth, flattenedCounter);
			List<Double> flattenedResults = evaluateAll(flattened, queries, null);
			assertEquals(recursiveResults, flattenedResults);
			assertEquals(queries.size() * depth, flattenedCounter.calls());
			metric("semantic-nested-" + depth + "-calls", counter.calls());
		}
	}

	@Test
	void sharedSessionMemoizesExactSemanticKeysWithoutChangingResults() {
		List<Double> repeatedQueries = List.of(-1.0, 0.0, 1.0, -1.0, 0.0, 1.0);
		Counter withoutSessionCounter = new Counter();
		Evaluator withoutSession = recursiveChain(3, withoutSessionCounter);
		List<Double> withoutSessionResults = evaluateAll(withoutSession,
				repeatedQueries, null);

		Counter withSessionCounter = new Counter();
		Evaluator withSession = recursiveChain(3, withSessionCounter);
		EvaluationSession session = new EvaluationSession();
		List<Double> withSessionResults = evaluateAll(withSession,
				repeatedQueries, session);

		assertEquals(withoutSessionResults, withSessionResults);
		assertEquals(repeatedQueries.size() * 3, withoutSessionCounter.calls());
		assertEquals(3 * 3, withSessionCounter.calls());
		assertEquals(3, session.hits());
		assertFalse(session.activeCycleDetected());
		metric("semantic-session-disabled-calls", withoutSessionCounter.calls());
		metric("semantic-session-enabled-calls", withSessionCounter.calls());
		metric("semantic-session-hits", session.hits());
	}

	@Test
	void analyticPilotReferencesAreOrderIndependentWithinFloatingPointContract() {
		double[] forward = {-1.0, -0.5, 0.0, 0.5, 1.0};
		double[] reverse = {1.0, 0.5, 0.0, -0.5, -1.0};
		double[] shuffled = {0.5, -1.0, 1.0, 0.0, -0.5};
		for (ReferenceCurve curve : ReferenceCurve.values()) {
			Map<Double, Point> expected = evaluateReference(curve, forward);
			assertEquals(expected, evaluateReference(curve, reverse));
			assertEquals(expected, evaluateReference(curve, shuffled));
			assertTrue(expected.values().stream()
					.allMatch(point -> curve.residual(point) <= 1e-12));
		}
	}

	@Test
	void evaluationSessionRejectsHiddenCallbackCycle() {
		EvaluationSession session = new EvaluationSession();
		SemanticKey key = new SemanticKey("cycle", 1, "main", 0.25);
		session.begin(key);
		assertTrue(session.isActive(key));
		assertFalse(session.begin(key));
		assertTrue(session.activeCycleDetected());
	}

	private static TopologyState topology(double innerSquared,
			double outerSquared) {
		List<Interval> components = new ArrayList<>();
		if (innerSquared >= 0 && innerSquared <= outerSquared) {
			double outer = Math.sqrt(outerSquared);
			double inner = Math.sqrt(innerSquared);
			if (inner == 0) {
				components.add(new Interval(-outer, outer));
			} else {
				components.add(new Interval(-outer, -inner));
				components.add(new Interval(inner, outer));
			}
		}
		return new TopologyState("fixture.sheet.main", components);
	}

	private static TopologyTransition transition(TopologyState before,
			TopologyState after) {
		BranchLifecycle lifecycle;
		if (before.components().isEmpty() && !after.components().isEmpty()) {
			lifecycle = BranchLifecycle.APPEARED;
		} else if (!before.components().isEmpty() && after.components().isEmpty()) {
			lifecycle = BranchLifecycle.DISAPPEARED;
		} else {
			lifecycle = BranchLifecycle.UNCHANGED;
		}
		DomainTopology topology = before.components().size() == 1
				&& after.components().size() == 2
				? DomainTopology.SPLIT : DomainTopology.UNCHANGED;
		return new TopologyTransition(lifecycle, topology);
	}

	private static Evaluator recursiveChain(int depth, Counter counter) {
		Evaluator evaluator = new LeafEvaluator(counter);
		for (int level = 2; level <= depth; level++) {
			evaluator = new RecursiveEvaluator(level, evaluator, counter);
		}
		return evaluator;
	}

	private static Evaluator flattenedChain(int depth, Counter counter) {
		return new FlattenedEvaluator(depth, counter);
	}

	private static List<Double> evaluateAll(Evaluator evaluator,
			List<Double> queries, EvaluationSession session) {
		return queries.stream().map(query -> evaluator.evaluate(query, session)).toList();
	}

	private static Map<Double, Point> evaluateReference(ReferenceCurve curve,
			double[] parameters) {
		Map<Double, Point> points = new HashMap<>();
		for (double parameter : parameters) {
			points.put(parameter, curve.evaluate(parameter));
		}
		return points;
	}

	private interface Evaluator {
		double evaluate(double parameter, EvaluationSession session);
	}

	private record LeafEvaluator(Counter counter) implements Evaluator {
		@Override
		public double evaluate(double parameter, EvaluationSession session) {
			return evaluateMemoized("L1", 1, parameter, session, counter,
					() -> parameter * 2);
		}
	}

	private record RecursiveEvaluator(int level, Evaluator upstream,
			Counter counter) implements Evaluator {
		@Override
		public double evaluate(double parameter, EvaluationSession session) {
			return evaluateMemoized("L" + level, 1, parameter, session, counter,
					() -> upstream.evaluate(parameter / 2, session)
							+ level * parameter);
		}
	}

	private record FlattenedEvaluator(int depth, Counter counter)
			implements Evaluator {
		@Override
		public double evaluate(double parameter, EvaluationSession session) {
			double[] parameters = new double[depth];
			parameters[depth - 1] = parameter;
			for (int level = depth - 2; level >= 0; level--) {
				parameters[level] = parameters[level + 1] / 2;
			}
			double value = 0;
			for (int level = 1; level <= depth; level++) {
				counter.increment();
				value = level == 1 ? parameters[0] * 2
						: value + level * parameters[level - 1];
			}
			return value;
		}
	}

	private static double evaluateMemoized(String locusId, long revision,
			double parameter, EvaluationSession session, Counter counter,
			DoubleSupplier supplier) {
		SemanticKey key = new SemanticKey(locusId, revision, "main", parameter);
		if (session != null && session.contains(key)) {
			return session.get(key);
		}
		if (session != null && !session.begin(key)) {
			throw new IllegalStateException("Semantic evaluator cycle: " + key);
		}
		try {
			counter.increment();
			double value = supplier.get();
			if (session != null) {
				session.put(key, value);
			}
			return value;
		} finally {
			if (session != null) {
				session.end(key);
			}
		}
	}

	private interface DoubleSupplier {
		double get();
	}

	private static final class EvaluationSession {
		private final Map<SemanticKey, Double> values = new HashMap<>();
		private final Map<SemanticKey, Boolean> active = new HashMap<>();
		private int hits;
		private boolean activeCycleDetected;

		boolean contains(SemanticKey key) {
			boolean found = values.containsKey(key);
			if (found) {
				hits++;
			}
			return found;
		}

		double get(SemanticKey key) {
			return Objects.requireNonNull(values.get(key));
		}

		void put(SemanticKey key, double value) {
			values.put(key, value);
		}

		boolean begin(SemanticKey key) {
			if (isActive(key)) {
				activeCycleDetected = true;
				return false;
			}
			active.put(key, true);
			return true;
		}

		void end(SemanticKey key) {
			active.remove(key);
		}

		boolean isActive(SemanticKey key) {
			return active.containsKey(key);
		}

		int hits() {
			return hits;
		}

		boolean activeCycleDetected() {
			return activeCycleDetected;
		}
	}

	private record SemanticKey(String locusIdentity, long semanticRevision,
			String branchKey, double nativeSemanticParameter) {
	}

	private record Point(double x, double y) {
	}

	private enum ReferenceCurve {
		LINE {
			@Override
			Point evaluate(double parameter) {
				return new Point(parameter, 2 * parameter + 1);
			}

			@Override
			double residual(Point point) {
				return Math.abs(point.y() - 2 * point.x() - 1);
			}
		},
		CIRCLE {
			@Override
			Point evaluate(double parameter) {
				return new Point(2 * Math.cos(parameter), 2 * Math.sin(parameter));
			}

			@Override
			double residual(Point point) {
				return Math.abs(point.x() * point.x() + point.y() * point.y() - 4);
			}
		},
		ELLIPSE {
			@Override
			Point evaluate(double parameter) {
				return new Point(3 * Math.cos(parameter), 2 * Math.sin(parameter));
			}

			@Override
			double residual(Point point) {
				return Math.abs(point.x() * point.x() / 9
						+ point.y() * point.y() / 4 - 1);
			}
		},
		PARABOLA {
			@Override
			Point evaluate(double parameter) {
				return new Point(parameter, parameter * parameter);
			}

			@Override
			double residual(Point point) {
				return Math.abs(point.y() - point.x() * point.x());
			}
		},
		TRANSCENDENTAL {
			@Override
			Point evaluate(double parameter) {
				return new Point(parameter, Math.sin(parameter));
			}

			@Override
			double residual(Point point) {
				return Math.abs(point.y() - Math.sin(point.x()));
			}
		};

		abstract Point evaluate(double parameter);

		abstract double residual(Point point);
	}

	private static final class Counter {
		private int calls;

		void increment() {
			calls++;
		}

		int calls() {
			return calls;
		}
	}

	private record Interval(double start, double end) {
		boolean isolated() {
			return start == end;
		}
	}

	private record TopologyState(String branchKey, List<Interval> components) {
	}

	private record TopologyTransition(BranchLifecycle branchLifecycle,
			DomainTopology domainTopology) {
	}

	private enum BranchLifecycle {
		UNCHANGED,
		APPEARED,
		DISAPPEARED
	}

	private enum DomainTopology {
		UNCHANGED,
		SPLIT
	}

	private static void metric(String name, Object value) {
		System.out.println("G6A_METRIC " + name + "=" + value);
	}
}

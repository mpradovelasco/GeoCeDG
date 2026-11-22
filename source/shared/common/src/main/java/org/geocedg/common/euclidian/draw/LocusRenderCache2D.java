/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.euclidian.draw.LocusRenderData2D.ComponentFidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Fidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Mark;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.MarkKind;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D.SamplingStrategy;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;

/** Small per-drawable cache of view-specific tessellations. */
public final class LocusRenderCache2D {
	private static final int MAX_ENTRIES = 4;
	private final Map<CacheKey, LocusRenderData2D> entries = new LinkedHashMap<>();
	private long hits;
	private long misses;
	private long evictions;
	private long builds;
	private long builtVertices;
	private int lastBuildVertices;
	private int maximumBuildVertices;
	private long partialBuilds;
	private long coarsenedBuilds;
	private long failedComponents;
	private LocusRenderData2D lastBuild;

	/**
	 * Returns or derives render data for the exact view policy and revision.
	 *
	 * @return bounded derived render data
	 */
	public LocusRenderData2D getOrBuild(GeoLocusV2 locus,
			LocusRenderPolicy2D policy) {
		CacheKey key = new CacheKey(locus.getSemanticRevision(), policy,
				conditionedIdentity(locus));
		LocusRenderData2D cached = entries.get(key);
		if (cached != null) {
			hits++;
			lastBuild = cached;
			return cached;
		}
		misses++;
		LocusRenderData2D built = build(locus, policy);
		builds++;
		lastBuildVertices = built.getVertices().size();
		builtVertices += lastBuildVertices;
		maximumBuildVertices = Math.max(maximumBuildVertices, lastBuildVertices);
		if (built.getFidelity() != Fidelity.COMPLETE) {
			partialBuilds++;
		}
		if (built.getCoarseningLevel() > 0) {
			coarsenedBuilds++;
		}
		for (ComponentFidelity component : built.getComponentFidelities()) {
			if (component.getFidelity() == Fidelity.FAILED) {
				failedComponents++;
			}
		}
		if (entries.size() == MAX_ENTRIES) {
			CacheKey oldest = entries.keySet().iterator().next();
			entries.remove(oldest);
			evictions++;
		}
		entries.put(key, built);
		lastBuild = built;
		return built;
	}

	/** @return current bounded entry count */
	public int size() {
		return entries.size();
	}

	public long getHits() {
		return hits;
	}

	public long getMisses() {
		return misses;
	}

	public long getEvictions() {
		return evictions;
	}

	public long getBuilds() {
		return builds;
	}

	/** @return drawing vertices of all builds (PRE-G9B-CU2 gate GC-5) */
	public long getBuiltVertices() {
		return builtVertices;
	}

	/** @return drawing vertices of the most recent build, one view and revision */
	public int getLastBuildVertices() {
		return lastBuildVertices;
	}

	/** @return largest vertex count of one build, one view and revision */
	public int getMaximumBuildVertices() {
		return maximumBuildVertices;
	}

	/** @return builds whose declared fidelity is not complete (PRE-G9B-CU2, P5) */
	public long getPartialBuilds() {
		return partialBuilds;
	}

	/** @return builds coarsened to fit the vertex budget */
	public long getCoarsenedBuilds() {
		return coarsenedBuilds;
	}

	/** @return components that a build could not draw */
	public long getFailedComponents() {
		return failedComponents;
	}

	/**
	 * @return render data most recently returned, with its declared fidelity and marks, or
	 *         {@code null} before the first one
	 */
	public LocusRenderData2D getLastBuild() {
		return lastBuild;
	}

	/** Drops view-specific data without touching semantic state. */
	public void clear() {
		entries.clear();
		lastBuild = null;
	}

	/** Evidence key of the render handle of a conditioned region. */
	public static final String CONDITIONED_REGION_KEY =
			org.geocedg.common.kernel.locus.LocusBranch2D.CONDITIONED_REGION_KEY;

	/**
	 * The content identities of the conditioned evidence that the locus publishes, which a
	 * drawing of its regions reads: another content is another drawing (contract {@code
	 * post-r1-conditioned-use.md}, section 7). Never an identity of the locus.
	 */
	private static String conditionedIdentity(GeoLocusV2 locus) {
		LocusDefinition2D definition = locus.isDefined() ? locus.getSemanticDefinition()
				: null;
		if (definition == null) {
			return "";
		}
		StringBuilder identity = new StringBuilder();
		for (LocusBranch2D branch : definition.getBranches()) {
			LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(
					branch.getBranchKey());
			if (evidence != null) {
				identity.append(evidence.getContentIdentity()).append(';');
			}
		}
		return identity.toString();
	}

	/**
	 * Render handle of one conditioned region for the tessellator: the extent of the region
	 * with evaluated ends. It is never part of an existence structure and states no fact:
	 * its nature is none, and its key is revision-local.
	 */
	private static ContinuousComponent regionHandle(LocusInterval2D region,
			LocusConditionedEvidence2D evidence) {
		return new ContinuousComponent(new LocusInterval2D(region.getLower(),
				region.getUpper(), true, true),
				CONDITIONED_REGION_KEY + Double.toHexString(region.getLower()),
				evidence.getPolicyVersion(),
				LocusExistenceStructure2D.Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
				evidence.getNature().name());
	}

	/**
	 * The stroke of one region through the nodes of the published content, when the chord
	 * deviation at the nodes of every span of the region is within the visual tolerance of
	 * the view. The nodes are evaluations of the producer inside the spans; a view that
	 * needs a finer stroke is tessellated from the evaluator instead.
	 *
	 * @return vertices of the region, or {@code null} when the view needs a finer stroke
	 */
	private static List<LocusRenderData2D.Vertex> nodeStroke(
			LocusConditionedEvidence2D evidence, LocusInterval2D region,
			LocusRenderPolicy2D policy) {
		double scale = Math.max(Math.abs(policy.getXScale()), Math.abs(policy.getYScale()));
		double tolerance = policy.getVisualTolerancePixels();
		List<LocusRenderData2D.Vertex> vertices = new ArrayList<>();
		for (LocusConditionedCell2D cell : evidence.cellsBetween(region.getLower(),
				region.getUpper())) {
			double[] nodes = cell.getNodePoints();
			if (!cell.getKind().isSpan() || cell.getLower() < region.getLower()
					|| cell.getUpper() > region.getUpper()) {
				continue;
			}
			if (nodes == null || !(cell.getChordDeviation() * scale <= tolerance)
					|| vertices.size() + 3 > policy.getVertexBudget() / 2) {
				return null;
			}
			double middle = cell.getLower() + (cell.getUpper() - cell.getLower()) / 2;
			if (vertices.isEmpty()) {
				vertices.add(new LocusRenderData2D.Vertex(new LocusPoint2D(nodes[0],
						nodes[1]), cell.getLower(), true));
			}
			vertices.add(new LocusRenderData2D.Vertex(new LocusPoint2D(nodes[2], nodes[3]),
					middle, false));
			vertices.add(new LocusRenderData2D.Vertex(new LocusPoint2D(nodes[4], nodes[5]),
					cell.getUpper(), false));
		}
		return vertices.size() < 2 ? null : vertices;
	}

	private static LocusRenderData2D build(GeoLocusV2 locus,
			LocusRenderPolicy2D policy) {
		List<LocusRenderData2D.Vertex> vertices = new ArrayList<>();
		LocusDefinition2D definition = locus.getSemanticDefinition();
		if (definition == null || !locus.isDefined()) {
			return new LocusRenderData2D(locus.getSemanticRevision(), policy,
					vertices);
		}
		try (LocusEvaluationSession2D session =
				new LocusEvaluationSession2D(true, 4096)) {
			if (policy.getSamplingStrategy() == SamplingStrategy.ADAPTIVE_VISUAL) {
				return buildVersion2(locus, definition, policy, session);
			}
			for (LocusBranch2D branch : definition.getBranches()) {
				LocusExistenceStructure2D structure = branch.getExistenceStructure();
				for (ContinuousComponent component
						: structure.getContinuousValidComponents()) {
					appendUniformComponent(locus, branch, structure, component, policy,
							session, vertices);
				}
			}
		}
		return new LocusRenderData2D(locus.getSemanticRevision(), policy,
				vertices);
	}

	/**
	 * Drawing policy {@value LocusRenderPolicy2D#VERSION}: every certified component of
	 * every branch at the finest coarsening level whose vertices fit the budget of the view;
	 * at the coarsest level, the components are admitted in kernel order while they fit and
	 * the others fail explicitly. Point components are declared and never drawn. The
	 * exploratory evidence of the unresolved bands is drawn apart (decision E-1).
	 */
	private static LocusRenderData2D buildVersion2(GeoLocusV2 locus,
			LocusDefinition2D definition, LocusRenderPolicy2D policy,
			LocusEvaluationSession2D session) {
		List<LocusComponentTessellator2D> tessellators = new ArrayList<>();
		List<Mark> declarations = new ArrayList<>();
		List<LocusRenderData2D.Vertex> exploratory = new ArrayList<>();
		// Conditioned regions (ADR 0042): one stroke per region, tessellated for the view
		// from the evaluator inside the region and never across a band. A branch with
		// conditioned evidence draws its regions; its exploratory evidence is not drawn.
		List<LocusComponentTessellator2D> conditioned = new ArrayList<>();
		List<LocusRenderData2D.Vertex> nodeVertices = new ArrayList<>();
		List<LocusRenderData2D.ConditionedRegion> nodeStrokes = new ArrayList<>();
		List<String> regionBranches = new ArrayList<>();
		List<LocusInterval2D> regionExtents = new ArrayList<>();
		List<String> regionKeys = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(
					branch.getBranchKey());
			if (evidence == null) {
				appendExploratoryEvidence(locus, branch, session, exploratory, declarations);
				continue;
			}
			for (LocusInterval2D region : evidence.getRegions()) {
				ContinuousComponent handle = regionHandle(region, evidence);
				String key = LocusComponentTessellator2D.componentKey(branch.getBranchKey(),
						handle.getEvidenceKey());
				List<LocusRenderData2D.Vertex> nodes = nodeStroke(evidence, region, policy);
				if (nodes != null) {
					// The nodes of the published content already meet the tolerance of this
					// view: they are evaluations of the producer, drawn without new ones.
					nodeStrokes.add(new LocusRenderData2D.ConditionedRegion(
							branch.getBranchKey(), key, region.getLower(), region.getUpper(),
							nodeVertices.size(), nodes.size(), new ComponentFidelity(key,
									Fidelity.COMPLETE, List.of(), nodes.size())));
					nodeVertices.addAll(nodes);
					declarations.add(new Mark(MarkKind.CONDITIONED_REGION, key,
							region.getLower() + (region.getUpper() - region.getLower()) / 2,
							"region " + region
									+ "; existence and continuity under hypotheses ("
									+ evidence.getPolicyVersion() + ")"));
					continue;
				}
				conditioned.add(new LocusComponentTessellator2D(locus, definition,
						branch.getBranchKey(), branch.getExistenceStructure(), handle, policy,
						session));
				regionBranches.add(branch.getBranchKey());
				regionExtents.add(region);
				regionKeys.add(key);
				declarations.add(new Mark(MarkKind.CONDITIONED_REGION, key,
						region.getLower() + (region.getUpper() - region.getLower()) / 2,
						"region " + region + "; existence and continuity under hypotheses ("
								+ evidence.getPolicyVersion() + ")"));
			}
		}
		for (LocusBranch2D branch : definition.getBranches()) {
			LocusExistenceStructure2D structure = branch.getExistenceStructure();
			for (ContinuousComponent component : structure.getContinuousValidComponents()) {
				tessellators.add(new LocusComponentTessellator2D(locus, definition,
						branch.getBranchKey(), structure, component, policy, session));
			}
			for (LocusPointComponent2D point : structure.getPointComponents()) {
				declarations.add(new Mark(MarkKind.POINT_COMPONENT_NOT_DRAWN,
						LocusComponentTessellator2D.componentKey(branch.getBranchKey(),
								point.getEvidenceKey()),
						point.getParameterLower()
								+ (point.getParameterUpper() - point.getParameterLower()) / 2,
						"enclosure [" + point.getParameterLower() + ", "
								+ point.getParameterUpper() + "]; position "
								+ point.getPositionState()));
			}
		}
		int certified = tessellators.size();
		tessellators.addAll(conditioned);
		int budget = policy.getVertexBudget();
		for (int level = 0; level <= LocusComponentTessellator2D.COARSEST_LEVEL; level++) {
			List<LocusComponentTessellator2D.Outcome> outcomes = new ArrayList<>();
			int used = 0;
			boolean fits = true;
			for (LocusComponentTessellator2D tessellator : tessellators) {
				LocusComponentTessellator2D.Outcome outcome = tessellator.tessellate(level,
						budget - used);
				if (outcome.isOverBudget()) {
					if (level < LocusComponentTessellator2D.COARSEST_LEVEL) {
						fits = false;
						break;
					}
					outcome = tessellator.exhausted();
				}
				used += outcome.getVertices().size();
				outcomes.add(outcome);
			}
			if (fits) {
				List<LocusRenderData2D.Vertex> regionVertices = new ArrayList<>(nodeVertices);
				List<LocusRenderData2D.ConditionedRegion> regions =
						new ArrayList<>(nodeStrokes);
				for (int index = certified; index < outcomes.size(); index++) {
					LocusComponentTessellator2D.Outcome outcome = outcomes.get(index);
					regions.add(new LocusRenderData2D.ConditionedRegion(
							regionBranches.get(index - certified),
							regionKeys.get(index - certified),
							regionExtents.get(index - certified).getLower(),
							regionExtents.get(index - certified).getUpper(),
							regionVertices.size(), outcome.getVertices().size(),
							outcome.getFidelity()));
					regionVertices.addAll(outcome.getVertices());
					declarations.addAll(outcome.getMarks());
				}
				return assemble(locus, policy, outcomes.subList(0, certified), declarations,
						level, exploratory, regionVertices, regions);
			}
		}
		throw new IllegalStateException("The coarsest level always assembles");
	}

	/**
	 * Exploratory evidence of the unresolved bands of a branch (decision E-1;
	 * presentation version 2): the valid runs that its producer explored, each chain of
	 * consecutive runs in its own subpath through the explored samples, only inside the
	 * unresolved coverage and never joined to a component. One mark declares each band
	 * drawn this way.
	 */
	private static void appendExploratoryEvidence(GeoLocusV2 locus, LocusBranch2D branch,
			LocusEvaluationSession2D session, List<LocusRenderData2D.Vertex> vertices,
			List<Mark> marks) {
		LocusExploratoryEvidence2D evidence = locus.getExploratoryEvidence(
				branch.getBranchKey());
		if (evidence == null) {
			return;
		}
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		for (Coverage band : structure.getCoverage()) {
			if (band.getState() != CoverageState.UNRESOLVED) {
				continue;
			}
			boolean drawn = false;
			// Parameter of the last vertex of the open chain, NaN when none is open.
			double chainEnd = Double.NaN;
			for (LocusInterval2D run : evidence.getValidRuns()) {
				if (!band.getInterval().contains(run.getLower(), 0)
						|| !band.getInterval().contains(run.getUpper(), 0)) {
					continue;
				}
				boolean continues = run.getLower() == chainEnd;
				if (!continues) {
					continues = appendExploratoryVertex(locus, branch, run.getLower(), true,
							session, vertices);
				}
				chainEnd = appendExploratoryVertex(locus, branch, run.getUpper(), !continues,
						session, vertices) ? run.getUpper() : Double.NaN;
				drawn = true;
			}
			if (drawn) {
				marks.add(new Mark(MarkKind.EXPLORATORY_EVIDENCE,
						LocusComponentTessellator2D.componentKey(branch.getBranchKey(),
								"unresolved-band"),
						band.getInterval().getLower() + (band.getInterval().getUpper()
								- band.getInterval().getLower()) / 2,
						"band " + band.getInterval() + "; " + evidence.getMethod() + " ("
								+ evidence.getPolicyVersion() + ")"));
			}
		}
	}

	/**
	 * @return whether the explored sample evaluated to a vertex; when it does not, the next
	 *         vertex starts a new subpath, and nothing is interpolated
	 */
	private static boolean appendExploratoryVertex(GeoLocusV2 locus, LocusBranch2D branch,
			double parameter, boolean startsSubpath, LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		LocusEvaluation2D evaluation = locus.evaluateForRender(branch.getBranchKey(),
				parameter, session);
		if (!evaluation.isValid()) {
			return false;
		}
		vertices.add(new LocusRenderData2D.Vertex(evaluation.getPoint(), parameter,
				startsSubpath));
		return true;
	}

	private static LocusRenderData2D assemble(GeoLocusV2 locus, LocusRenderPolicy2D policy,
			List<LocusComponentTessellator2D.Outcome> outcomes, List<Mark> declarations,
			int level, List<LocusRenderData2D.Vertex> exploratory,
			List<LocusRenderData2D.Vertex> conditioned,
			List<LocusRenderData2D.ConditionedRegion> regions) {
		List<LocusRenderData2D.Vertex> vertices = new ArrayList<>();
		List<Mark> marks = new ArrayList<>();
		List<ComponentFidelity> components = new ArrayList<>();
		for (LocusComponentTessellator2D.Outcome outcome : outcomes) {
			vertices.addAll(outcome.getVertices());
			marks.addAll(outcome.getMarks());
			components.add(outcome.getFidelity());
		}
		marks.addAll(declarations);
		return new LocusRenderData2D(locus.getSemanticRevision(), policy, vertices, marks,
				components, level, exploratory, conditioned, regions);
	}

	/**
	 * Uniform reference of the tests that predate presentation version 2: it reaches its
	 * closing point only as the certified closed periodic component, an excluded
	 * structural-event end is drawn to its one-sided limit and any other excluded end keeps
	 * the inset.
	 */
	private static void appendUniformComponent(GeoLocusV2 locus, LocusBranch2D branch,
			LocusExistenceStructure2D structure, ContinuousComponent certified,
			LocusRenderPolicy2D policy, LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		LocusInterval2D component = certified.getInterval();
		double lower = component.getLower();
		double upper = component.getUpper();
		double span = upper - lower;
		double inset = span * policy.getEndpointInsetFraction();
		boolean unbounded = branch.getProperties()
				.contains(BranchProperty.UNBOUNDED);
		boolean fullPeriodicCycle = structure.isClosedPeriodic()
				&& certified.isClosedPeriodic();
		if (unbounded || (!component.isLowerClosed() && !fullPeriodicCycle
				&& !isLimitEnd(certified.getLowerBoundary()))) {
			lower += inset;
		}
		if (unbounded || (!component.isUpperClosed() && !fullPeriodicCycle
				&& !isLimitEnd(certified.getUpperBoundary()))) {
			upper -= inset;
		}
		if (!(lower <= upper)) {
			return;
		}
		boolean startsSubpath = true;
		int samples = policy.getSamplesPerComponent();
		for (int index = 0; index <= samples; index++) {
			double ratio = index / (double) samples;
			double parameter = lower + ratio * (upper - lower);
			LocusEvaluation2D evaluation = locus.evaluateForRender(
					branch.getBranchKey(), component, parameter, session);
			if (evaluation.isValid()) {
				vertices.add(new LocusRenderData2D.Vertex(evaluation.getPoint(),
						parameter, startsSubpath));
				startsSubpath = false;
			} else {
				startsSubpath = true;
			}
		}
	}

	/** @return whether an excluded end is a structural event reached as a limit */
	private static boolean isLimitEnd(LocusBoundary2D end) {
		return !end.isBinaryEndIncluded()
				&& end.supportsOneSidedLimit();
	}

	private static final class CacheKey {
		private final long revision;
		private final LocusRenderPolicy2D policy;
		private final String conditioned;

		CacheKey(long revision, LocusRenderPolicy2D policy, String conditioned) {
			this.revision = revision;
			this.policy = policy;
			this.conditioned = conditioned;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof CacheKey)) {
				return false;
			}
			CacheKey that = (CacheKey) other;
			return revision == that.revision && policy.equals(that.policy)
					&& conditioned.equals(that.conditioned);
		}

		@Override
		public int hashCode() {
			return 31 * (31 * Long.hashCode(revision) + policy.hashCode())
					+ conditioned.hashCode();
		}
	}
}

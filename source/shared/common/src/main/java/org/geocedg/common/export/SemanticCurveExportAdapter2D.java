/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.CurveEvaluation2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.CurveEvaluator2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.RequiredVertex;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.Result;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.WorkLedger;
import org.geocedg.common.export.GeometryExportModel.AreaExclusion;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Exactness;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.Style;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.IdentityScope;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.export.SourceExportOutcome.SemanticCoverage;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundaryImage2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * PRE-G9B-R6-plus-C (C10): the one read-only semantic curve export adapter of
 * GeoCeDG, extracted from the G9X1 DXF adapter. For every Locus V2 (Spline V2
 * included) it enumerates the certified continuous valid components of each
 * branch in kernel order, keeps branch keys, component addresses, orientation,
 * gaps (one approximation per component, never bridged), constructive
 * multiplicity, coverage and closure from a full-period certificate only, and
 * guards the captured definition and semantic revision.
 *
 * <p>It evaluates only {@link LocusDefinition2D#evaluate}, never a render path;
 * it creates no construction element, dependency, undo point or label and reads
 * no view, viewport, zoom, DPI or render cache. It returns one outcome per
 * component (and one per source that yields none) and decides no writability:
 * each format applies its own admissibility policy.
 */
public final class SemanticCurveExportAdapter2D {

	/** Neutral entity identifier prefix used by the G9X1 DXF path. */
	public static final String G9X1_ENTITY_PREFIX = "entity:g9x1:";
	/**
	 * Minimum subdivision depth of every certified Locus V2 component, open or closed
	 * (PRE-G9B-CU2, consumption design section 6).
	 */
	static final int LOCUS_MINIMUM_DEPTH = 2;
	/** Gaps of failed evaluations at which one certified component may be cut, at most. */
	static final int MAXIMUM_CUTS = 16;
	/**
	 * An excluded end without a limit is approached to the component length times 2^-28,
	 * the approach of the drawing (16 initial spans halved 24 times).
	 */
	static final int OPEN_END_APPROACH_BITS = 28;
	/** A bisection stops at the component length times 2^-52. */
	private static final int RESOLUTION_BITS = 52;

	/**
	 * Export-area participation of one certified component. DXF applies the
	 * {@code B1} rule; LaTeX keeps every component and clips at format level.
	 */
	@FunctionalInterface
	public interface ComponentParticipation {
		/**
		 * @param definition captured definition
		 * @param branchKey branch of the component
		 * @param component component or requested closed subdomain
		 * @return whether the component is kept
		 */
		boolean participates(LocusDefinition2D definition, String branchKey,
				LocusInterval2D component);
	}

	/** Every component participates. */
	public static final ComponentParticipation ALL_COMPONENTS =
			(definition, branchKey, component) -> true;

	/**
	 * One component result: an emitted approximation, a failure with its
	 * diagnostic, or a component outside the explicit export area.
	 */
	public static final class Component {
		private final SourceExportOutcome outcome;
		private final Entity entity;
		private final Diagnostic diagnostic;
		private final AreaExclusion exclusion;

		private Component(SourceExportOutcome outcome, Entity entity,
				Diagnostic diagnostic, AreaExclusion exclusion) {
			this.outcome = outcome;
			this.entity = entity;
			this.diagnostic = diagnostic;
			this.exclusion = exclusion;
		}

		/** @return per-component outcome; null outside the export area */
		public SourceExportOutcome getOutcome() {
			return outcome;
		}

		/** @return emitted approximate neutral entity, or null */
		public Entity getEntity() {
			return entity;
		}

		/** @return failure diagnostic, or null */
		public Diagnostic getDiagnostic() {
			return diagnostic;
		}

		/** @return exclusion record, or null when the component participates */
		public AreaExclusion getExclusion() {
			return exclusion;
		}

		/** @return whether the component is emitted */
		public boolean isEmitted() {
			return entity != null;
		}

		/** @return whether the component is outside the explicit export area */
		public boolean isOutsideArea() {
			return exclusion != null;
		}
	}

	/** Ordered components of one semantic source and its currentness guard. */
	public static final class SourceResult {
		private final String sourceId;
		private final List<Component> components;
		private final GeometryExportPreflight.SourceRevisionGuard guard;

		private SourceResult(String sourceId, List<Component> components,
				GeometryExportPreflight.SourceRevisionGuard guard) {
			this.sourceId = sourceId;
			this.components = Collections.unmodifiableList(
					new ArrayList<>(components));
			this.guard = guard;
		}

		public String getSourceId() {
			return sourceId;
		}

		/** @return components in kernel branch and component order */
		public List<Component> getComponents() {
			return components;
		}

		/** @return whether the captured definition and revision are current */
		public boolean isCurrent() {
			try {
				return guard.isCurrent();
			} catch (RuntimeException exception) {
				return false;
			}
		}

		GeometryExportPreflight.SourceRevisionGuard getGuard() {
			return guard;
		}
	}

	private final AdaptiveCurveApproximationBuilder2D approximationBuilder;

	/** Creates the adapter with the shared approximation builder. */
	public SemanticCurveExportAdapter2D() {
		this(new AdaptiveCurveApproximationBuilder2D());
	}

	SemanticCurveExportAdapter2D(
			AdaptiveCurveApproximationBuilder2D approximationBuilder) {
		if (approximationBuilder == null) {
			throw new IllegalArgumentException("Approximation builder is required");
		}
		this.approximationBuilder = approximationBuilder;
	}

	/**
	 * Identifier used by request-domain overrides for one ordered source.
	 * Persistent Locus V2 identity is used when present; ordinary sources remain
	 * scoped to the construction revision.
	 *
	 * @param geo source
	 * @param sourceOrdinal ordinal in the requested population
	 * @return request-domain source identifier
	 */
	public static String requestSourceId(GeoElement geo, int sourceOrdinal) {
		if (geo == null) {
			throw new IllegalArgumentException("Source geometry is required");
		}
		if (geo instanceof GeoLocusV2) {
			PersistentGeoId persistent = ((GeoLocusV2) geo).getPersistentLocusId();
			if (persistent != null) {
				return persistent.toExternalForm();
			}
		}
		return legacySourceId(geo, sourceOrdinal);
	}

	/**
	 * Adapts one Locus V2 (Spline V2 included).
	 *
	 * @param locus semantic source
	 * @param sourceOrdinal ordinal in the requested population
	 * @param request tolerance, budgets and explicit domains
	 * @param ledger request-wide deterministic work ledger
	 * @param entityPrefix neutral entity identifier prefix
	 * @param participation export-area participation rule
	 * @return ordered per-component results and the source guard
	 */
	public SourceResult adaptLocus(GeoLocusV2 locus, int sourceOrdinal,
			GeometryExportRequest request, WorkLedger ledger, String entityPrefix,
			ComponentParticipation participation) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		String sourceId = requestSourceId(locus, sourceOrdinal);
		IdentityScope scope = locus.getPersistentLocusId() == null
				? IdentityScope.CONSTRUCTION_REVISION : IdentityScope.PERSISTENT;
		long revision = definition == null ? 0 : definition.getSemanticRevision();
		GeometryExportPreflight.SourceRevisionGuard guard = () -> locus.isDefined()
				&& locus.getSemanticDefinition() == definition
				&& locus.getSemanticRevision() == revision;
		List<Component> components = new ArrayList<>();
		if (definition == null || !locus.isDefined()
				|| definition.getDefinitionStatus() != DefinitionStatus.VALID) {
			Reason reason = definition != null
					&& definition.getDefinitionStatus() == DefinitionStatus.UNSUPPORTED
							? Reason.UNSUPPORTED_FAMILY : Reason.UNDEFINED_SOURCE;
			components.add(failure(locus, sourceId, revision, scope,
					new ComponentAddress(null, "semantic-definition"), reason,
					"Locus V2 has no valid semantic definition for export.", null));
			return new SourceResult(sourceId, components, guard);
		}
		if (definition.getDeterminism() == Determinism.UNSUPPORTED_NONDETERMINISM) {
			components.add(failure(locus, sourceId, revision, scope,
					new ComponentAddress(null, "semantic-definition"),
					Reason.UNSUPPORTED_FAMILY,
					"Locus V2 evaluator has no approved deterministic rule.", null));
			return new SourceResult(sourceId, components, guard);
		}
		int maximumEntries = (int) Math.min(Integer.MAX_VALUE,
				Math.max(1, request.getMaximumEvaluations()));
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.memoizing(maximumEntries)) {
			List<SemanticDomain> overrides = request.getSourceSemanticDomains()
					.get(sourceId);
			if (overrides != null) {
				adaptRequestedLocusDomains(locus, definition, sourceId, sourceOrdinal,
						revision, scope, overrides, request, ledger, session,
						entityPrefix, participation, components);
				return new SourceResult(sourceId, components, guard);
			}
			int componentOrdinal = 0;
			for (LocusBranch2D branch : definition.getBranches()) {
				LocusExistenceStructure2D structure = branch.getExistenceStructure();
				List<ContinuousComponent> certified = structure
						.getContinuousValidComponents();
				for (int branchComponent = 0;
						branchComponent < certified.size(); branchComponent++) {
					ContinuousComponent certificate = certified.get(branchComponent);
					LocusInterval2D component = certificate.getInterval();
					boolean increasing = branch.getOrientation()
							== Orientation.INCREASING;
					double start = increasing ? component.getLower()
							: component.getUpper();
					double end = increasing ? component.getUpper()
							: component.getLower();
					// An excluded structural-event end is exported to its one-sided limit
					// (PRE-G9B-CU2, consumption design section 5.3).
					boolean lowerEvaluable = component.isLowerClosed()
							|| isLimitEnd(certificate.getLowerBoundary());
					boolean upperEvaluable = component.isUpperClosed()
							|| isLimitEnd(certificate.getUpperBoundary());
					boolean startClosed = increasing ? lowerEvaluable : upperEvaluable;
					boolean endClosed = increasing ? upperEvaluable : lowerEvaluable;
					boolean complete = structure.getCompleteness() == Completeness.COMPLETE;
					String componentKey = complete ? "component-" + branchComponent
							: certificate.getEvidenceKey();
					ComponentAddress address = new ComponentAddress(
							branch.getBranchKey(), componentKey, start, end,
							startClosed, endClosed);
					if (start == end) {
						components.add(failure(locus, sourceId, revision, scope, address,
								Reason.DEGENERATE_SOURCE,
								"A zero-width driver component has no curve export.",
								null));
						componentOrdinal++;
						continue;
					}
					if (!participation.participates(definition, branch.getBranchKey(),
							component)) {
						components.add(outside(locus, sourceId, revision, scope,
								address));
						componentOrdinal++;
						continue;
					}
					boolean semanticClosure = hasFullPeriodClosure(branch, component);
					// PRE-G9B-CU2 (A2.6): each end is reached as the certificate states it,
					// or the export stops at the last evaluated vertex and declares it.
					EndReach lower = reachEnd(definition, branch.getBranchKey(), certificate,
							component, false, semanticClosure, session);
					EndReach upper = reachEnd(definition, branch.getBranchKey(), certificate,
							component, true, semanticClosure, session);
					if (lower.reason != null || upper.reason != null) {
						Reason reason = lower.reason != null ? lower.reason : upper.reason;
						components.add(failure(locus, sourceId, revision, scope, address,
								reason, failureMessage(reason), null));
						componentOrdinal++;
						continue;
					}
					List<String> declarations = new ArrayList<>();
					if (lower.declaration != null) {
						declarations.add(lower.declaration);
					}
					if (upper.declaration != null) {
						declarations.add(upper.declaration);
					}
					double exportStart = increasing ? lower.parameter : upper.parameter;
					double exportEnd = increasing ? upper.parameter : lower.parameter;
					List<RequiredVertex> singular = singularVertices(definition,
							branch.getBranchKey(), structure, component, exportStart,
							exportEnd, declarations);
					SemanticDomain approximationDomain = new SemanticDomain(componentKey,
							exportStart, exportEnd, true, true);
					double residual = semanticClosure ? closureResidual(definition,
							branch.getBranchKey(), component, exportStart, exportEnd,
							session) : Double.NaN;
					EvaluationRecorder evaluator = new EvaluationRecorder(parameter -> {
						EndReach fixed = parameter == lower.parameter ? lower
								: parameter == upper.parameter ? upper : null;
						if (fixed != null && fixed.point != null) {
							return new Evaluated(finitePoint(fixed.point.getX(),
									fixed.point.getY()), null);
						}
						return locusEvaluated(definition, branch.getBranchKey(), component,
								parameter, session);
					});
					List<Component> parts = approximateParts(new PartRequest(locus, sourceId,
							sourceOrdinal, componentOrdinal, revision, scope, address,
							semanticClosure, residual, singular, evaluator, request, ledger,
							entityPrefix, complete ? SemanticCoverage.COMPLETE : SemanticCoverage
									.LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED, declarations),
							approximationDomain);
					components.addAll(parts);
					componentOrdinal += parts.size();
				}
				// A point component is declared, never exported as a path (D-CU2-19).
				for (LocusPointComponent2D point : structure.getPointComponents()) {
					components.add(declared(locus, sourceId, revision, scope,
							branch.getBranchKey(), point));
					componentOrdinal++;
				}
			}
			// PRE-G9B-CU2 step T11: a certified empty cover has nothing to export and nothing
			// missing (D-CU2-8); a locus without any certified component whose existence is
			// not established is declared, never a failure (V-DXF-2).
			if (componentOrdinal == 0 && !isCertifiedEmpty(definition)) {
				components.add(existenceNotEstablished(locus, sourceId, revision, scope,
						definition));
			}
		}
		return new SourceResult(sourceId, components, guard);
	}

	/** @return whether every branch has a complete certificate without any component */
	private static boolean isCertifiedEmpty(LocusDefinition2D definition) {
		for (LocusBranch2D branch : definition.getBranches()) {
			LocusExistenceStructure2D structure = branch.getExistenceStructure();
			if (structure.getCompleteness() != Completeness.COMPLETE
					|| !structure.getContinuousValidComponents().isEmpty()
					|| !structure.getPointComponents().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the declaration of a Locus V2 that has no certified component and whose
	 *         existence is not established ({@code dxf-curve-fidelity-and-approximation.md},
	 *         PRE-G9B-CU2 amendment; V-DXF-2): nothing is exported, its exploratory evidence
	 *         never is, the output is incomplete and the export is not blocked
	 */
	private static Component existenceNotEstablished(GeoElement geo, String sourceId,
			long sourceRevision, IdentityScope scope, LocusDefinition2D definition) {
		StringBuilder bands = new StringBuilder();
		for (LocusBranch2D branch : definition.getBranches()) {
			for (Coverage item : branch.getExistenceStructure().getCoverage()) {
				if (item.getState() == CoverageState.UNRESOLVED) {
					bands.append(bands.length() == 0 ? "" : "; ")
							.append(branch.getBranchKey()).append(' ')
							.append(item.getInterval()).append(' ').append(item.getCause());
				}
			}
		}
		String message = "The existence of the Locus V2 is not established (" + bands
				+ "): no certified component is exported, and its exploratory evidence"
				+ " never is.";
		SourceExportOutcome outcome = new SourceExportOutcome(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope,
				new ComponentAddress(null, "existence-not-established"), Fidelity.DECLARED,
				Reason.EXISTENCE_NOT_ESTABLISHED, null, null, message);
		Diagnostic diagnostic = new Diagnostic(sourceId, geo.getGeoClassType().name(),
				DiagnosticCode.EXISTENCE_NOT_ESTABLISHED, message);
		return new Component(outcome, null, diagnostic, null);
	}

	private void adaptRequestedLocusDomains(GeoLocusV2 locus,
			LocusDefinition2D definition, String sourceId, int sourceOrdinal,
			long revision, IdentityScope scope, List<SemanticDomain> overrides,
			GeometryExportRequest request, WorkLedger ledger,
			LocusEvaluationSession2D session, String entityPrefix,
			ComponentParticipation participation, List<Component> components) {
		if (overrides.isEmpty()) {
			components.add(failure(locus, sourceId, revision, scope,
					new ComponentAddress(null, "request-domain"),
					Reason.MISSING_DOMAIN,
					"The explicit Locus V2 domain partition is empty.", null));
			return;
		}
		for (int ordinal = 0; ordinal < overrides.size(); ordinal++) {
			SemanticDomain domain = overrides.get(ordinal);
			LocusDomainDecision decision = validateLocusDomain(definition,
					overrides, ordinal, domain);
			String componentKey = decision.valid
					? "component-" + decision.componentOrdinal + "/subdomain-"
							+ domain.getKey()
					: domain.getKey();
			ComponentAddress address = new ComponentAddress(domain.getBranchKey(),
					componentKey, domain.getStartParameter(),
					domain.getEndParameter(), domain.isStartClosed(),
					domain.isEndClosed());
			if (!decision.valid) {
				components.add(failure(locus, sourceId, revision, scope, address,
						decision.reason, decision.message, null));
				continue;
			}
			LocusInterval2D requested = new LocusInterval2D(
					Math.min(domain.getStartParameter(), domain.getEndParameter()),
					Math.max(domain.getStartParameter(), domain.getEndParameter()),
					true, true);
			if (!participation.participates(definition,
					decision.branch.getBranchKey(), requested)) {
				components.add(outside(locus, sourceId, revision, scope, address));
				continue;
			}
			CurveEvaluator2D evaluator = parameter -> locusEvaluation(definition,
					decision.branch.getBranchKey(), decision.component, parameter,
					session);
			boolean semanticClosure = hasFullPeriodClosure(decision.branch,
					decision.component)
					&& sameOrientedInterval(domain, decision.branch,
							decision.component);
			components.add(approximate(locus, sourceId, sourceOrdinal, ordinal,
					revision, scope, address, domain, semanticClosure, evaluator,
					request, ledger, entityPrefix,
					decision.branch.getExistenceStructure().getCompleteness()
							== Completeness.COMPLETE ? SemanticCoverage.COMPLETE
									: SemanticCoverage
											.LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED));
		}
	}

	/**
	 * Approximates one component through the shared builder.
	 *
	 * @return emitted component or failure with evidence
	 */
	Component approximate(GeoElement geo, String sourceId, int sourceOrdinal,
			int componentOrdinal, long sourceRevision, IdentityScope scope,
			ComponentAddress address, SemanticDomain domain, boolean semanticClosure,
			CurveEvaluator2D evaluator, GeometryExportRequest request,
			WorkLedger ledger, String entityPrefix,
			SemanticCoverage semanticCoverage) {
		return approximate(geo, sourceId, sourceOrdinal, componentOrdinal, sourceRevision,
				scope, address, domain, semanticClosure, Double.NaN, Collections.emptyList(),
				semanticClosure ? 2 : 0, evaluator, request, ledger, entityPrefix,
				semanticCoverage, Collections.emptyList());
	}

	/**
	 * Approximates one certified component of a Locus V2 through the shared builder
	 * (PRE-G9B-CU2, A2.6), with the closure residual measured against its certificate, its
	 * exact singular vertices and its representation declarations.
	 *
	 * @return emitted component or failure with evidence
	 */
	private Component approximate(GeoElement geo, String sourceId, int sourceOrdinal,
			int componentOrdinal, long sourceRevision, IdentityScope scope,
			ComponentAddress address, SemanticDomain domain, boolean semanticClosure,
			double closureResidual, List<RequiredVertex> requiredVertices, int minimumDepth,
			CurveEvaluator2D evaluator, GeometryExportRequest request, WorkLedger ledger,
			String entityPrefix, SemanticCoverage semanticCoverage,
			List<String> declarations) {
		Result result = approximationBuilder.approximate(evaluator, domain,
				semanticClosure, closureResidual, requiredVertices, minimumDepth, request,
				ledger);
		if (!result.isSuccess()) {
			return failure(geo, sourceId, sourceRevision, scope, address,
					result.getReason(), failureMessage(result.getReason()),
					result.toApproximationEvidence());
		}
		String neutralId = entityPrefix + sourceOrdinal + ":" + componentOrdinal;
		Entity entity = new Entity(neutralId, sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(),
				layerName(geo.getLayer()), style(geo), Exactness.APPROXIMATE,
				request.getRequestedTolerance(), result.toPolylineGeometry());
		SourceExportOutcome outcome = new SourceExportOutcome(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope, address, Fidelity.APPROXIMATE,
				Reason.NONE, neutralId, result.toApproximationEvidence(), null,
				semanticCoverage, declarations);
		return new Component(outcome, entity, null, null);
	}

	/**
	 * Approximates one certified component of a Locus V2 and cuts it where one of its points
	 * cannot be evaluated (PRE-G9B-CU2, A2.4-R to A2.6; consumption design sections 5.6 and
	 * 6; continuation of step T11 under D-CU2-T11-R1). A failed evaluation inside a certified
	 * component is computational, not a gap of existence: the failure is located by bisection
	 * up to the resolution of the parameter, the parts on both sides are approximated
	 * separately, never joined or interpolated across it, and every part declares the gap
	 * with its cause, so the export is incomplete. A component whose only part fails, or whose
	 * approximation fails for any other reason, keeps the failure of the shared builder.
	 *
	 * @return the emitted parts in traversal order, or one failure
	 */
	private List<Component> approximateParts(PartRequest part, SemanticDomain domain) {
		List<double[]> pending = new ArrayList<>();
		pending.add(new double[] {domain.getStartParameter(), domain.getEndParameter()});
		List<double[]> ranges = new ArrayList<>();
		List<String> gaps = new ArrayList<>();
		List<Result> results = new ArrayList<>();
		Result failed = null;
		double resolution = Math.abs(domain.getEndParameter() - domain.getStartParameter())
				* Math.scalb(1.0, -RESOLUTION_BITS);
		while (!pending.isEmpty()) {
			double[] range = pending.remove(0);
			boolean whole = gaps.isEmpty() && pending.isEmpty() && ranges.isEmpty();
			part.evaluator.reset();
			Result result = approximationBuilder.approximate(part.evaluator,
					new SemanticDomain(domain.getKey(), range[0], range[1], true, true),
					whole && part.semanticClosure, part.closureResidual,
					inside(part.singular, range), LOCUS_MINIMUM_DEPTH, part.request,
					part.ledger);
			double failure = part.evaluator.failedParameter;
			if (result.isSuccess()) {
				insert(ranges, results, range, result);
				continue;
			}
			if (gaps.size() >= MAXIMUM_CUTS || Double.isNaN(failure)
					|| !(Math.min(range[0], range[1]) < failure
							&& failure < Math.max(range[0], range[1]))) {
				failed = result;
				break;
			}
			String cause = part.evaluator.failure;
			double before = edge(part.evaluator, range[0], failure, resolution, true);
			double after = edge(part.evaluator, failure, range[1], resolution, false);
			gaps.add((cause.startsWith(EvaluationStatus.EVALUATION_FAILED.name())
					|| cause.startsWith(EvaluationStatus.NON_FINITE.name())
							? "COMPUTATIONAL_GAP" : "EVALUATION_GAP")
					+ ": the evaluation fails between " + before + " and " + after
					+ " inside the certified component (" + cause
					+ "); the path is cut there and never interpolated");
			if (before != range[0]) {
				pending.add(new double[] {range[0], before});
			}
			if (after != range[1]) {
				pending.add(new double[] {after, range[1]});
			}
		}
		if (failed != null || results.isEmpty()) {
			Result reason = failed != null ? failed : approximationBuilder.approximate(
					part.evaluator, domain, part.semanticClosure, part.closureResidual,
					part.singular, LOCUS_MINIMUM_DEPTH, part.request, part.ledger);
			return List.of(failure(part.geo, part.sourceId, part.sourceRevision, part.scope,
					part.address, reason.getReason(), failureMessage(reason.getReason()),
					reason.toApproximationEvidence()));
		}
		List<String> declarations = new ArrayList<>(part.declarations);
		declarations.addAll(gaps);
		List<Component> parts = new ArrayList<>();
		for (int index = 0; index < results.size(); index++) {
			ComponentAddress address = results.size() == 1 ? part.address
					: new ComponentAddress(part.address.getBranchKey(),
							part.address.getComponentKey() + "/part-" + index,
							ranges.get(index)[0], ranges.get(index)[1],
							index > 0 || part.address.isStartIncluded(),
							index + 1 < results.size() || part.address.isEndIncluded());
			parts.add(emitted(part, part.componentOrdinal + index, address,
					results.get(index), declarations));
		}
		return parts;
	}

	/** Inserts a successful part in traversal order. */
	private static void insert(List<double[]> ranges, List<Result> results, double[] range,
			Result result) {
		boolean increasing = range[0] <= range[1];
		int index = 0;
		while (index < ranges.size() && (increasing ? ranges.get(index)[0] < range[0]
				: ranges.get(index)[0] > range[0])) {
			index++;
		}
		ranges.add(index, range);
		results.add(index, result);
	}

	/** @return the singular vertices strictly inside the range */
	private static List<RequiredVertex> inside(List<RequiredVertex> vertices, double[] range) {
		List<RequiredVertex> inside = new ArrayList<>();
		for (RequiredVertex vertex : vertices) {
			if (Math.min(range[0], range[1]) < vertex.getParameter()
					&& vertex.getParameter() < Math.max(range[0], range[1])) {
				inside.add(vertex);
			}
		}
		return inside;
	}

	/**
	 * Locates the edge of a run of failed evaluations by bisection, up to the resolution of
	 * the parameter.
	 *
	 * @param validFirst whether {@code from} is the valid side
	 * @return the valid parameter nearest to the run
	 */
	private static double edge(EvaluationRecorder evaluator, double from, double to,
			double resolution, boolean validFirst) {
		double valid = validFirst ? from : to;
		double invalid = validFirst ? to : from;
		for (int step = 0; step < 2 * RESOLUTION_BITS; step++) {
			double middle = valid / 2 + invalid / 2;
			if (!(Math.min(valid, invalid) < middle && middle < Math.max(valid, invalid))
					|| Math.abs(invalid - valid) <= resolution) {
				break;
			}
			if (evaluator.probe(middle)) {
				valid = middle;
			} else {
				invalid = middle;
			}
		}
		return valid;
	}

	private Component emitted(PartRequest part, int componentOrdinal, ComponentAddress address,
			Result result, List<String> declarations) {
		String neutralId = part.entityPrefix + part.sourceOrdinal + ":" + componentOrdinal;
		Entity entity = new Entity(neutralId, part.sourceId,
				part.geo.getGeoClassType().name(), part.geo.getLabelSimple(),
				layerName(part.geo.getLayer()), style(part.geo), Exactness.APPROXIMATE,
				part.request.getRequestedTolerance(), result.toPolylineGeometry());
		SourceExportOutcome outcome = new SourceExportOutcome(part.sourceId,
				part.geo.getGeoClassType().name(), part.geo.getLabelSimple(),
				part.sourceRevision, part.geo.isEuclidianVisible(), part.scope, address,
				Fidelity.APPROXIMATE, Reason.NONE, neutralId, result.toApproximationEvidence(),
				null, part.semanticCoverage, declarations);
		return new Component(outcome, entity, null, null);
	}

	/** Everything one certified component needs to be approximated in parts. */
	private static final class PartRequest {
		private final GeoElement geo;
		private final String sourceId;
		private final int sourceOrdinal;
		private final int componentOrdinal;
		private final long sourceRevision;
		private final IdentityScope scope;
		private final ComponentAddress address;
		private final boolean semanticClosure;
		private final double closureResidual;
		private final List<RequiredVertex> singular;
		private final EvaluationRecorder evaluator;
		private final GeometryExportRequest request;
		private final WorkLedger ledger;
		private final String entityPrefix;
		private final SemanticCoverage semanticCoverage;
		private final List<String> declarations;

		private PartRequest(GeoElement geo, String sourceId, int sourceOrdinal,
				int componentOrdinal, long sourceRevision, IdentityScope scope,
				ComponentAddress address, boolean semanticClosure, double closureResidual,
				List<RequiredVertex> singular, EvaluationRecorder evaluator,
				GeometryExportRequest request, WorkLedger ledger, String entityPrefix,
				SemanticCoverage semanticCoverage, List<String> declarations) {
			this.geo = geo;
			this.sourceId = sourceId;
			this.sourceOrdinal = sourceOrdinal;
			this.componentOrdinal = componentOrdinal;
			this.sourceRevision = sourceRevision;
			this.scope = scope;
			this.address = address;
			this.semanticClosure = semanticClosure;
			this.closureResidual = closureResidual;
			this.singular = singular;
			this.evaluator = evaluator;
			this.request = request;
			this.ledger = ledger;
			this.entityPrefix = entityPrefix;
			this.semanticCoverage = semanticCoverage;
			this.declarations = declarations;
		}
	}

	/** One evaluation for the builder with the typed cause of its failure. */
	private static final class Evaluated {
		private final CurveEvaluation2D evaluation;
		private final String cause;

		private Evaluated(CurveEvaluation2D evaluation, String cause) {
			this.evaluation = evaluation;
			this.cause = cause;
		}
	}

	/** Evaluator of one component that records its first failed evaluation. */
	private static final class EvaluationRecorder implements CurveEvaluator2D {
		private final java.util.function.DoubleFunction<Evaluated> source;
		private double failedParameter = Double.NaN;
		private String failure;

		private EvaluationRecorder(java.util.function.DoubleFunction<Evaluated> source) {
			this.source = source;
		}

		@Override
		public CurveEvaluation2D evaluate(double parameter) {
			Evaluated evaluated = source.apply(parameter);
			if (!evaluated.evaluation.isValid() && Double.isNaN(failedParameter)) {
				failedParameter = parameter;
				failure = evaluated.cause;
			}
			return evaluated.evaluation;
		}

		/** @return whether the parameter evaluates, without recording a failure */
		private boolean probe(double parameter) {
			return source.apply(parameter).evaluation.isValid();
		}

		private void reset() {
			failedParameter = Double.NaN;
			failure = null;
		}
	}

	/**
	 * @return a point component declared with its parameter enclosure and the status of its
	 *         position, never exported as a path and never a failure (D-CU2-19)
	 */
	private static Component declared(GeoElement geo, String sourceId, long sourceRevision,
			IdentityScope scope, String branchKey, LocusPointComponent2D point) {
		ComponentAddress address = new ComponentAddress(branchKey,
				"point-component/" + point.getEvidenceKey(), point.getParameterLower(),
				point.getParameterUpper(), true, true);
		String message = "Point component, declared and never exported as a path: parameter"
				+ " enclosure [" + point.getParameterLower() + ", " + point.getParameterUpper()
				+ "]; position " + point.getPositionState()
				+ (point.getPositionState()
						== LocusPointComponent2D.PositionState.CERTIFIED_ENCLOSURE
								? " " + java.util.Arrays.toString(point.getPositionEnclosure())
								: "")
				+ ".";
		SourceExportOutcome outcome = new SourceExportOutcome(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope, address, Fidelity.DECLARED,
				Reason.DEGENERATE_SOURCE, null, null, message);
		Diagnostic diagnostic = new Diagnostic(sourceId, geo.getGeoClassType().name(),
				DiagnosticCode.DEGENERATE, message);
		return new Component(outcome, null, diagnostic, null);
	}

	/** Where the export of a component reaches one of its ends, and what it declares. */
	private static final class EndReach {
		private final double parameter;
		private final Point2D point;
		private final String declaration;
		private final Reason reason;

		private EndReach(double parameter, Point2D point, String declaration,
				Reason reason) {
			this.parameter = parameter;
			this.point = point;
			this.declaration = declaration;
			this.reason = reason;
		}
	}

	/**
	 * Reaches one end of a certified component (PRE-G9B-CU2, A2.6): an evaluable end is
	 * its evaluation or its one-sided limit; an included certified transition that the
	 * evaluator does not define is the limit form of its certificate; any other end that
	 * cannot be evaluated is replaced by the last evaluated parameter, located by bisection
	 * from inside up to the resolution, and declared; an excluded end without a limit is
	 * approached from inside and declared.
	 */
	private static EndReach reachEnd(LocusDefinition2D definition, String branchKey,
			ContinuousComponent certificate, LocusInterval2D component, boolean upper,
			boolean closure, LocusEvaluationSession2D session) {
		double end = upper ? component.getUpper() : component.getLower();
		LocusBoundary2D boundary = upper ? certificate.getUpperBoundary()
				: certificate.getLowerBoundary();
		String side = upper ? "upper" : "lower";
		if (closure || boundary.isBinaryEndIncluded() || isLimitEnd(boundary)) {
			CurveEvaluation2D evaluation = locusEvaluation(definition, branchKey, component,
					end, session);
			if (evaluation.isValid()) {
				return new EndReach(end, null, null, null);
			}
			if (!closure && boundary.isBinaryEndIncluded()) {
				LocusBoundaryImage2D image = definition.encloseBoundaryImage(branchKey,
						boundary);
				if (image.isEnclosed()) {
					return new EndReach(end, new Point2D(image.getPoint().getX(),
							image.getPoint().getY()), null, null);
				}
			}
			return lastEvaluated(definition, branchKey, component, end, session,
					"END_NOT_EVALUATED: the " + side + " end " + end + " is not evaluated ("
							+ evaluation.getReason() + ")");
		}
		double approach = Math.scalb(component.getUpper() - component.getLower(),
				-OPEN_END_APPROACH_BITS);
		double near = upper ? end - approach : end + approach;
		CurveEvaluation2D evaluation = locusEvaluation(definition, branchKey, component, near,
				session);
		if (evaluation.isValid()) {
			return new EndReach(near, null, "OPEN_END_APPROACHED: the excluded " + side
					+ " end " + end + " is approached to " + near, null);
		}
		return lastEvaluated(definition, branchKey, component, near, session,
				"END_NOT_EVALUATED: the excluded " + side + " end " + end
						+ " is not evaluated near " + near + " (" + evaluation.getReason() + ")");
	}

	/**
	 * @return the last evaluated parameter from the middle of the component toward a failed
	 *         one, up to the resolution of the parameter, with its declaration; or the
	 *         failure of a component whose middle is not evaluated either
	 */
	private static EndReach lastEvaluated(LocusDefinition2D definition, String branchKey,
			LocusInterval2D component, double failed, LocusEvaluationSession2D session,
			String declaration) {
		double length = component.getUpper() - component.getLower();
		double valid = component.getLower() + length / 2;
		CurveEvaluation2D middle = locusEvaluation(definition, branchKey, component, valid,
				session);
		if (!middle.isValid()) {
			return new EndReach(Double.NaN, null, null, middle.getReason());
		}
		double invalid = failed;
		double resolution = Math.scalb(length, -RESOLUTION_BITS);
		while (Math.abs(invalid - valid) > resolution) {
			double probe = valid + (invalid - valid) / 2;
			if (probe == valid || probe == invalid) {
				break;
			}
			if (locusEvaluation(definition, branchKey, component, probe, session).isValid()) {
				valid = probe;
			} else {
				invalid = probe;
			}
		}
		return new EndReach(valid, null, declaration + "; exported to the last evaluated"
				+ " vertex at " + valid, null);
	}

	/**
	 * @return the exact vertices of the certified singular parameters inside the exported
	 *         range, from the limit form of the certificate, in domain order; a singular
	 *         parameter without one is declared
	 */
	private static List<RequiredVertex> singularVertices(LocusDefinition2D definition,
			String branchKey, LocusExistenceStructure2D structure, LocusInterval2D component,
			double exportStart, double exportEnd, List<String> declarations) {
		LocusInterval2D domain = structure.getCanonicalDomain();
		double period = structure.getTopology()
				== LocusExistenceStructure2D.DomainTopology.PERIODIC
						? domain.getUpper() - domain.getLower() : 0;
		double low = Math.min(exportStart, exportEnd);
		double high = Math.max(exportStart, exportEnd);
		List<RequiredVertex> vertices = new ArrayList<>();
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			double middle = singular.getParameterLower()
					+ (singular.getParameterUpper() - singular.getParameterLower()) / 2;
			double lifted = Double.NaN;
			for (double candidate : new double[] {middle, middle + period, middle - period}) {
				if (component.getLower() <= candidate && candidate <= component.getUpper()) {
					lifted = candidate;
					break;
				}
			}
			if (!(low < lifted && lifted < high)) {
				continue;
			}
			LocusBoundaryImage2D image = definition.encloseSingularImage(branchKey, singular);
			if (!image.isEnclosed()) {
				declarations.add("SINGULAR_VERTEX_NOT_EXACT: the singular parameter " + middle
						+ " has no exact image (" + image.getDiagnostic() + ")");
				continue;
			}
			vertices.add(new RequiredVertex(lifted, new Point2D(image.getPoint().getX(),
					image.getPoint().getY())));
		}
		vertices.sort((first, second) -> exportStart < exportEnd
				? Double.compare(first.getParameter(), second.getParameter())
				: Double.compare(second.getParameter(), first.getParameter()));
		return vertices;
	}

	/**
	 * @return the distance between the start and the curve just before the closing point,
	 *         which the closure certificate identifies with the start (A2.6), or infinity
	 *         when either is not evaluated
	 */
	static double closureResidual(LocusDefinition2D definition, String branchKey,
			LocusInterval2D component, double exportStart, double exportEnd,
			LocusEvaluationSession2D session) {
		double before = closureProbe(exportStart, exportEnd,
				definition.getProvider().getDomainEpsilon());
		CurveEvaluation2D start = locusEvaluation(definition, branchKey, component,
				exportStart, session);
		CurveEvaluation2D last = locusEvaluation(definition, branchKey, component, before,
				session);
		if (!start.isValid() || !last.isValid()) {
			return Double.POSITIVE_INFINITY;
		}
		return Math.hypot(start.getPoint().getX() - last.getPoint().getX(),
				start.getPoint().getY() - last.getPoint().getY());
	}

	/**
	 * @param domainEpsilon provider tolerance of the domain, which excludes a band at its
	 *        open end
	 * @return the parameter at which the closure residual of a closed path is measured:
	 *         before the closing point by four ulps, so that its canonicalization never rounds
	 *         onto the start and measures the trivial identity, and by twice the domain
	 *         tolerance, so that it belongs to the domain
	 */
	static double closureProbe(double exportStart, double exportEnd, double domainEpsilon) {
		double step = Math.max(4 * Math.max(Math.ulp(exportEnd), Math.ulp(exportStart)),
				2 * domainEpsilon);
		return exportEnd > exportStart ? exportEnd - step : exportEnd + step;
	}

	/**
	 * @return failed component with its outcome and diagnostic
	 */
	static Component failure(GeoElement geo, String sourceId, long sourceRevision,
			IdentityScope scope, ComponentAddress address, Reason reason,
			String message, ApproximationEvidence approximationEvidence) {
		Fidelity fidelity = reason == Reason.UNSUPPORTED_FAMILY
				? Fidelity.UNSUPPORTED : Fidelity.INVALID;
		SourceExportOutcome outcome = new SourceExportOutcome(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope, address, fidelity, reason, null,
				approximationEvidence, message);
		Diagnostic diagnostic = new Diagnostic(sourceId,
				geo.getGeoClassType().name(), diagnosticCode(reason), message);
		return new Component(outcome, null, diagnostic, null);
	}

	/**
	 * @return component recorded outside the explicit export area
	 */
	static Component outside(GeoElement geo, String sourceId, long sourceRevision,
			IdentityScope scope, ComponentAddress address) {
		return new Component(null, null, null, new AreaExclusion(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope, address));
	}

	private static CurveEvaluation2D locusEvaluation(LocusDefinition2D definition,
			String branchKey, LocusInterval2D component, double parameter,
			LocusEvaluationSession2D session) {
		// Only an end of the component may be the excluded end of a structural event,
		// whose one-sided limit is evaluated explicitly (PRE-G9B-CU2).
		boolean end = parameter == component.getLower()
				|| parameter == component.getUpper();
		LocusEvaluation2D evaluation = end
				? definition.evaluateInComponent(branchKey, component, parameter, session)
				: definition.evaluate(branchKey, parameter, session);
		if (!evaluation.isValid()) {
			return CurveEvaluation2D.invalid(locusReason(evaluation.getStatus()));
		}
		double x = evaluation.getPoint().getX();
		double y = evaluation.getPoint().getY();
		return finitePoint(x, y);
	}

	/** @return the evaluation for the builder, with the status and diagnostic of a failure */
	private static Evaluated locusEvaluated(LocusDefinition2D definition, String branchKey,
			LocusInterval2D component, double parameter, LocusEvaluationSession2D session) {
		boolean end = parameter == component.getLower()
				|| parameter == component.getUpper();
		LocusEvaluation2D evaluation = end
				? definition.evaluateInComponent(branchKey, component, parameter, session)
				: definition.evaluate(branchKey, parameter, session);
		if (!evaluation.isValid()) {
			return new Evaluated(CurveEvaluation2D.invalid(locusReason(evaluation.getStatus())),
					evaluation.getStatus() + ": " + evaluation.getDiagnostic());
		}
		CurveEvaluation2D point = finitePoint(evaluation.getPoint().getX(),
				evaluation.getPoint().getY());
		return new Evaluated(point, point.isValid() ? null
				: EvaluationStatus.NON_FINITE + ": non-finite coordinates");
	}

	private static Reason locusReason(EvaluationStatus status) {
		switch (status) {
		case NON_FINITE:
			return Reason.NON_FINITE;
		case UNSUPPORTED_NONDETERMINISM:
			return Reason.UNSUPPORTED_FAMILY;
		case OUT_OF_DOMAIN:
		case DEPENDENCY_UNDEFINED:
		case EVALUATION_FAILED:
		default:
			return Reason.DISCONTINUITY_UNRESOLVED;
		}
	}

	static CurveEvaluation2D finitePoint(double x, double y) {
		if (!Double.isFinite(x) || !Double.isFinite(y)) {
			return CurveEvaluation2D.invalid(Reason.NON_FINITE);
		}
		return CurveEvaluation2D.valid(new Point2D(x, y));
	}

	/**
	 * Single closure authority ({@code locus-v2-existence-components.md} section 15.8): the
	 * component closes only as the certified closed periodic component of its branch.
	 */
	private static boolean hasFullPeriodClosure(LocusBranch2D branch,
			LocusInterval2D component) {
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		return structure.isClosedPeriodic()
				&& component.equals(structure.getContinuousValidComponents().get(0)
						.getInterval());
	}

	/** @return whether an excluded end is a structural event reached as a limit */
	private static boolean isLimitEnd(LocusBoundary2D end) {
		return !end.isBinaryEndIncluded()
				&& end.getKind() == LocusBoundary2D.Kind.STRUCTURAL_EVENT;
	}

	private static LocusDomainDecision validateLocusDomain(
			LocusDefinition2D definition, List<SemanticDomain> domains,
			int domainOrdinal, SemanticDomain domain) {
		if (domain.getBranchKey() == null) {
			return LocusDomainDecision.invalid(Reason.INVALID_DOMAIN,
					"A Locus V2 export subdomain requires a branch key.");
		}
		LocusBranch2D matchingBranch = null;
		for (LocusBranch2D branch : definition.getBranches()) {
			if (domain.getBranchKey().equals(branch.getBranchKey())) {
				matchingBranch = branch;
				break;
			}
		}
		if (matchingBranch == null) {
			return LocusDomainDecision.invalid(Reason.INVALID_DOMAIN,
					"The requested Locus V2 branch does not exist.");
		}
		if (!domain.isStartClosed() || !domain.isEndClosed()) {
			return LocusDomainDecision.invalid(Reason.MISSING_DOMAIN,
					"A Locus V2 override must be a closed export subdomain.");
		}
		boolean sourceIncreasing = matchingBranch.getOrientation()
				== Orientation.INCREASING;
		if (domain.isIncreasing() != sourceIncreasing) {
			return LocusDomainDecision.invalid(Reason.INVALID_DOMAIN,
					"The requested subdomain orientation disagrees with its branch.");
		}
		for (int otherOrdinal = 0; otherOrdinal < domains.size(); otherOrdinal++) {
			if (otherOrdinal == domainOrdinal) {
				continue;
			}
			SemanticDomain other = domains.get(otherOrdinal);
			if (domain.getBranchKey().equals(other.getBranchKey())
					&& intervalsOverlap(domain, other)) {
				return LocusDomainDecision.invalid(Reason.INVALID_DOMAIN,
						"Locus V2 export subdomains must not overlap.");
			}
		}
		LocusInterval2D containing = null;
		int containingOrdinal = -1;
		List<LocusInterval2D> components =
				matchingBranch.getCertifiedContinuousValidComponents();
		for (int componentOrdinal = 0;
				componentOrdinal < components.size(); componentOrdinal++) {
			LocusInterval2D component = components.get(componentOrdinal);
			if (containsClosedSubdomain(component, domain)
					|| isTypedFullPeriodOverride(definition, matchingBranch,
							component, domain)) {
				if (containing != null) {
					return LocusDomainDecision.invalid(Reason.INVALID_DOMAIN,
							"The requested subdomain is ambiguous across components.");
				}
				containing = component;
				containingOrdinal = componentOrdinal;
			}
		}
		if (containing == null) {
			return LocusDomainDecision.invalid(Reason.INVALID_DOMAIN,
					"The requested subdomain is outside a valid Locus V2 component.");
		}
		return LocusDomainDecision.valid(matchingBranch, containing,
				containingOrdinal);
	}

	private static boolean isTypedFullPeriodOverride(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, SemanticDomain domain) {
		return hasFullPeriodClosure(branch, component)
				&& sameOrientedInterval(domain, branch, component);
	}

	private static boolean containsClosedSubdomain(LocusInterval2D component,
			SemanticDomain domain) {
		double lower = Math.min(domain.getStartParameter(),
				domain.getEndParameter());
		double upper = Math.max(domain.getStartParameter(),
				domain.getEndParameter());
		if (lower < component.getLower() || upper > component.getUpper()) {
			return false;
		}
		if (lower == component.getLower() && !component.isLowerClosed()) {
			return false;
		}
		return upper != component.getUpper() || component.isUpperClosed();
	}

	/**
	 * @return whether two declared domains overlap in their parameter interiors
	 */
	static boolean intervalsOverlap(SemanticDomain first, SemanticDomain second) {
		double firstLower = Math.min(first.getStartParameter(),
				first.getEndParameter());
		double firstUpper = Math.max(first.getStartParameter(),
				first.getEndParameter());
		double secondLower = Math.min(second.getStartParameter(),
				second.getEndParameter());
		double secondUpper = Math.max(second.getStartParameter(),
				second.getEndParameter());
		return Math.max(firstLower, secondLower)
				< Math.min(firstUpper, secondUpper);
	}

	private static boolean sameOrientedInterval(SemanticDomain domain,
			LocusBranch2D branch, LocusInterval2D component) {
		double expectedStart = branch.getOrientation() == Orientation.INCREASING
				? component.getLower() : component.getUpper();
		double expectedEnd = branch.getOrientation() == Orientation.INCREASING
				? component.getUpper() : component.getLower();
		return Double.compare(domain.getStartParameter(), expectedStart) == 0
				&& Double.compare(domain.getEndParameter(), expectedEnd) == 0;
	}

	/**
	 * @return diagnostic code of a failure reason
	 */
	static DiagnosticCode diagnosticCode(Reason reason) {
		switch (reason) {
		case MISSING_DOMAIN:
			return DiagnosticCode.MISSING_DOMAIN;
		case INVALID_DOMAIN:
			return DiagnosticCode.INVALID_DOMAIN;
		case NON_FINITE:
			return DiagnosticCode.NON_FINITE;
		case DISCONTINUITY_UNRESOLVED:
			return DiagnosticCode.DISCONTINUITY_UNRESOLVED;
		case TOLERANCE_NOT_ESTABLISHED:
			return DiagnosticCode.TOLERANCE_NOT_ESTABLISHED;
		case WORK_LIMIT:
			return DiagnosticCode.WORK_LIMIT;
		case STALE_SOURCE_REVISION:
			return DiagnosticCode.STALE_SOURCE_REVISION;
		case UNDEFINED_SOURCE:
			return DiagnosticCode.UNDEFINED;
		case NOT_2D:
			return DiagnosticCode.NOT_2D;
		case DEGENERATE_SOURCE:
			return DiagnosticCode.DEGENERATE;
		case DUPLICATE_COMPONENT:
			return DiagnosticCode.DUPLICATE_COMPONENT;
		case EXISTENCE_NOT_ESTABLISHED:
			return DiagnosticCode.EXISTENCE_NOT_ESTABLISHED;
		case UNSUPPORTED_FAMILY:
			return DiagnosticCode.UNSUPPORTED;
		case NONE:
		default:
			throw new IllegalArgumentException(
					"A failure diagnostic requires a failure reason");
		}
	}

	/**
	 * @return deterministic failure message of an approximation reason
	 */
	static String failureMessage(Reason reason) {
		switch (reason) {
		case MISSING_DOMAIN:
			return "The semantic component has no approved closed finite domain.";
		case INVALID_DOMAIN:
			return "The requested semantic domain is outside source authority.";
		case NON_FINITE:
			return "Semantic evaluation produced a non-finite coordinate.";
		case DISCONTINUITY_UNRESOLVED:
			return "A discontinuity or invalid interval could not be isolated.";
		case TOLERANCE_NOT_ESTABLISHED:
			return "The requested approximation guarantee was not established.";
		case WORK_LIMIT:
			return "A deterministic approximation work limit was reached.";
		case STALE_SOURCE_REVISION:
			return "The source revision changed during export preflight.";
		case UNSUPPORTED_FAMILY:
		default:
			return "No approved approximation exists for the source component.";
		}
	}

	static Style style(GeoElement geo) {
		GColor color = geo.getObjectColor();
		return new Style(color.getRed(), color.getGreen(), color.getBlue(),
				geo.isEuclidianVisible());
	}

	static String layerName(int layer) {
		return layer == 0 ? "0" : "GEOCEDG_L" + layer;
	}

	static String legacySourceId(GeoElement geo, int ordinal) {
		String label = geo.getLabelSimple();
		String suffix = label == null || label.isEmpty() ? "item-" + ordinal
				: label.replaceAll("[^A-Za-z0-9_.-]", "_");
		return "geo-" + geo.getConstructionIndex() + "-" + suffix;
	}

	private static final class LocusDomainDecision {
		private final boolean valid;
		private final LocusBranch2D branch;
		private final LocusInterval2D component;
		private final int componentOrdinal;
		private final Reason reason;
		private final String message;

		private LocusDomainDecision(boolean valid, LocusBranch2D branch,
				LocusInterval2D component, int componentOrdinal, Reason reason,
				String message) {
			this.valid = valid;
			this.branch = branch;
			this.component = component;
			this.componentOrdinal = componentOrdinal;
			this.reason = reason;
			this.message = message;
		}

		private static LocusDomainDecision valid(LocusBranch2D branch,
				LocusInterval2D component, int componentOrdinal) {
			return new LocusDomainDecision(true, branch, component,
					componentOrdinal, Reason.NONE, null);
		}

		private static LocusDomainDecision invalid(Reason reason,
				String message) {
			return new LocusDomainDecision(false, null, null, -1, reason, message);
		}
	}
}

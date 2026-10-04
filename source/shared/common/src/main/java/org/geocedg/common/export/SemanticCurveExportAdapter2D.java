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
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
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
				List<ContinuousComponent> certified = branch.getExistenceStructure()
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
					boolean startClosed = increasing ? component.isLowerClosed()
							: component.isUpperClosed();
					boolean endClosed = increasing ? component.isUpperClosed()
							: component.isLowerClosed();
					boolean complete = branch.getExistenceStructure().getCompleteness()
							== Completeness.COMPLETE;
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
					boolean semanticClosure = hasFullPeriodClosure(definition, branch,
							component);
					SemanticDomain approximationDomain = new SemanticDomain(componentKey,
							start, end, semanticClosure || startClosed,
							semanticClosure || endClosed);
					CurveEvaluator2D evaluator = parameter -> locusEvaluation(definition,
							branch.getBranchKey(), parameter, session);
					components.add(approximate(locus, sourceId, sourceOrdinal,
							componentOrdinal++, revision, scope, address,
							approximationDomain, semanticClosure, evaluator, request,
							ledger, entityPrefix, complete ? SemanticCoverage.COMPLETE
									: SemanticCoverage
											.LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED));
				}
			}
			if (componentOrdinal == 0) {
				components.add(failure(locus, sourceId, revision, scope,
						new ComponentAddress(null, "valid-domain"),
						Reason.MISSING_DOMAIN,
						"Locus V2 exposes no valid semantic component.", null));
			}
		}
		return new SourceResult(sourceId, components, guard);
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
					decision.branch.getBranchKey(), parameter, session);
			boolean semanticClosure = hasFullPeriodClosure(definition,
					decision.branch, decision.component)
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
		Result result = approximationBuilder.approximate(evaluator, domain,
				semanticClosure, request, ledger);
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
				semanticCoverage);
		return new Component(outcome, entity, null, null);
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
			String branchKey, double parameter, LocusEvaluationSession2D session) {
		LocusEvaluation2D evaluation = definition.evaluate(branchKey, parameter,
				session);
		if (!evaluation.isValid()) {
			return CurveEvaluation2D.invalid(locusReason(evaluation.getStatus()));
		}
		double x = evaluation.getPoint().getX();
		double y = evaluation.getPoint().getY();
		return finitePoint(x, y);
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

	private static boolean hasFullPeriodClosure(LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getExistenceStructure().getCompleteness()
						== Completeness.COMPLETE
				&& branch.getCertifiedContinuousValidComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(definition.getProvider().getDeclaredDomain());
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
		return hasFullPeriodClosure(definition, branch, component)
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

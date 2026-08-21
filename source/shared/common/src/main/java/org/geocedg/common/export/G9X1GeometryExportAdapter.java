/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.CurveEvaluation2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.CurveEvaluator2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.Result;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.WorkLedger;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Exactness;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportModel.Style;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.IdentityScope;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.FunctionVariable;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoCurveCartesian;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.matrix.Coords;
import org.geogebra.common.plugin.Operation;

/**
 * G9X1 read-only source adapter. Exact G5 entities are delegated unchanged;
 * approved curve families receive export-only component approximations.
 */
public final class G9X1GeometryExportAdapter {

	private final GeoElementGeometryExportAdapter exactAdapter;
	private final AdaptiveCurveApproximationBuilder2D approximationBuilder;

	/** Creates the default additive G9X1 adapter. */
	public G9X1GeometryExportAdapter() {
		this(new GeoElementGeometryExportAdapter(),
				new AdaptiveCurveApproximationBuilder2D());
	}

	G9X1GeometryExportAdapter(GeoElementGeometryExportAdapter exactAdapter,
			AdaptiveCurveApproximationBuilder2D approximationBuilder) {
		if (exactAdapter == null || approximationBuilder == null) {
			throw new IllegalArgumentException("Both export adapters are required");
		}
		this.exactAdapter = exactAdapter;
		this.approximationBuilder = approximationBuilder;
	}

	/**
	 * @param geos ordered requested source population
	 * @param selectionMode population provenance
	 * @param request explicit fidelity and work policy
	 * @return complete preflight without destination or file-system access
	 */
	public GeometryExportPreflight preflight(Collection<GeoElement> geos,
			SelectionMode selectionMode, GeometryExportRequest request) {
		if (geos == null || selectionMode == null || request == null) {
			throw new IllegalArgumentException(
					"Sources, selection mode, and request are required");
		}
		if (request.isPartialOutputAllowed()) {
			throw new IllegalArgumentException(
					"G9X1 does not authorize partial component output");
		}
		List<GeoElement> ordered = new ArrayList<>(geos);
		Map<Construction, ConstructionSnapshot> constructionSnapshots =
				captureConstructions(ordered);
		GeometryExportModel exactModel = exactAdapter.adapt(ordered, selectionMode);
		Map<String, Entity> exactBySource = new LinkedHashMap<>();
		for (Entity entity : exactModel.getEntities()) {
			exactBySource.put(entity.getSourceId(), entity);
		}
		Map<String, Diagnostic> diagnosticBySource = new LinkedHashMap<>();
		for (Diagnostic diagnostic : exactModel.getDiagnostics()) {
			diagnosticBySource.put(diagnostic.getSourceId(), diagnostic);
		}

		List<Entity> entities = new ArrayList<>();
		List<Diagnostic> diagnostics = new ArrayList<>(exactModel.getDiagnostics());
		List<SourceExportOutcome> outcomes = new ArrayList<>();
		List<GeometryExportPreflight.SourceRevisionGuard> guards =
				new ArrayList<>();
		for (ConstructionSnapshot snapshot : constructionSnapshots.values()) {
			guards.add(snapshot::isCurrent);
		}
		WorkLedger ledger = new WorkLedger(request);
		Set<String> unconsumedOverrideIds = new TreeSet<>(
				request.getSourceSemanticDomains().keySet());
		for (int sourceOrdinal = 0; sourceOrdinal < ordered.size(); sourceOrdinal++) {
			GeoElement geo = ordered.get(sourceOrdinal);
			if (geo == null) {
				continue;
			}
			String legacyId = legacySourceId(geo, sourceOrdinal);
			ConstructionSnapshot constructionSnapshot = constructionSnapshots.get(
					geo.getConstruction());
			long sourceRevision = constructionSnapshot.getReportedRevision();
			Entity exact = exactBySource.get(legacyId);
			if (exact != null) {
				entities.add(exact);
				outcomes.add(exactOutcome(exact, sourceRevision));
				continue;
			}

			Diagnostic diagnostic = diagnosticBySource.get(legacyId);
			if (diagnostic != null
					&& diagnostic.getCode() == DiagnosticCode.DUPLICATE_POLYGON_SIDE) {
				// G5 deliberately reports generated polygon sides as suppressed
				// duplicates of the exact polygon boundary. Retain that diagnostic,
				// but do not turn it into a blocking fidelity outcome.
				continue;
			}
			if (mayApproximate(geo)) {
				unconsumedOverrideIds.remove(requestSourceId(geo, sourceOrdinal));
				diagnostics.removeIf(item -> item.getSourceId().equals(legacyId));
				adaptApproximateSource(geo, sourceOrdinal, sourceRevision, request,
						ledger, entities, diagnostics, outcomes, guards);
			} else if (diagnostic != null) {
				outcomes.add(outcomeFromDiagnostic(geo, diagnostic,
						sourceRevision));
			}
		}
		for (String sourceId : unconsumedOverrideIds) {
			List<SemanticDomain> unconsumed =
					request.getSourceSemanticDomains().get(sourceId);
			if (unconsumed.isEmpty()) {
				outcomes.add(new SourceExportOutcome(sourceId,
						"UNMATCHED_REQUEST_SOURCE", null, 0, true,
						IdentityScope.CONSTRUCTION_REVISION,
						new ComponentAddress(null, "request-domain"),
						Fidelity.INVALID, Reason.INVALID_DOMAIN, null, null,
						"The empty source-specific partition was not matched "
								+ "to an approximable selected source."));
			}
			for (SemanticDomain domain : unconsumed) {
				outcomes.add(new SourceExportOutcome(sourceId,
						"UNMATCHED_REQUEST_SOURCE", null, 0, true,
						IdentityScope.CONSTRUCTION_REVISION,
						new ComponentAddress(domain.getBranchKey(), domain.getKey(),
								domain.getStartParameter(), domain.getEndParameter(),
								domain.isStartClosed(), domain.isEndClosed()),
						Fidelity.INVALID, Reason.INVALID_DOMAIN, null, null,
						"The source-specific domain was not consumed by an "
								+ "approximable selected source."));
			}
		}
		for (ConstructionSnapshot snapshot : constructionSnapshots.values()) {
			if (!snapshot.isCurrent()) {
				throw new IllegalStateException(
						"Source construction changed during export preflight");
			}
		}
		GeometryExportModel model = new GeometryExportModel(selectionMode, entities,
				diagnostics, outcomes);
		return new GeometryExportPreflight(request, model, guards);
	}

	/**
	 * Identifier used by request-domain overrides for one ordered source.
	 * Persistent Locus V2 identity is used when present; ordinary sources remain
	 * scoped to the construction revision.
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

	private void adaptApproximateSource(GeoElement geo, int sourceOrdinal,
			long ordinaryRevision, GeometryExportRequest request, WorkLedger ledger,
			List<Entity> entities, List<Diagnostic> diagnostics,
			List<SourceExportOutcome> outcomes,
			List<GeometryExportPreflight.SourceRevisionGuard> guards) {
		String sourceId = requestSourceId(geo, sourceOrdinal);
		if (!geo.isDefined()) {
			addFailure(geo, sourceId, ordinaryRevision,
					IdentityScope.CONSTRUCTION_REVISION,
					new ComponentAddress(null, "source"), Reason.UNDEFINED_SOURCE,
					"Undefined source cannot be approximated.", diagnostics, outcomes);
			return;
		}
		if (geo.isGeoElement3D()) {
			addFailure(geo, sourceId, ordinaryRevision,
					IdentityScope.CONSTRUCTION_REVISION,
					new ComponentAddress(null, "source"), Reason.NOT_2D,
					"G9X1 exports only resolved 2D geometry.", diagnostics, outcomes);
			return;
		}
		if (geo instanceof GeoLocusV2) {
			adaptLocus((GeoLocusV2) geo, sourceOrdinal, request, ledger, entities,
					diagnostics, outcomes, guards);
			return;
		}
		List<SemanticDomain> domains = domainsFor(sourceId, request);
		if (domains.isEmpty()) {
			addFailure(geo, sourceId, ordinaryRevision,
					IdentityScope.CONSTRUCTION_REVISION,
					new ComponentAddress(null, "request-domain"),
					Reason.MISSING_DOMAIN,
					"An explicit finite semantic domain is required.", diagnostics,
					outcomes);
			return;
		}
		CurveEvaluator2D evaluator = evaluatorFor(geo);
		if (evaluator == null) {
			addFailure(geo, sourceId, ordinaryRevision,
					IdentityScope.CONSTRUCTION_REVISION,
					new ComponentAddress(null, "source"), Reason.UNSUPPORTED_FAMILY,
					"No approved G9X1 approximation strategy exists for the source.",
					diagnostics, outcomes);
			return;
		}
		for (int componentOrdinal = 0;
				componentOrdinal < domains.size(); componentOrdinal++) {
			SemanticDomain domain = domains.get(componentOrdinal);
			DomainDecision decision = validateGenericDomain(geo, domains,
					componentOrdinal, domain);
			ComponentAddress address = new ComponentAddress(decision.branchKey,
					domain.getKey(), domain.getStartParameter(),
					domain.getEndParameter(), domain.isStartClosed(),
					domain.isEndClosed());
			if (!decision.valid) {
				addFailure(geo, sourceId, ordinaryRevision,
						IdentityScope.CONSTRUCTION_REVISION, address,
						decision.reason, decision.message, diagnostics, outcomes);
				continue;
			}
			approximateComponent(geo, sourceId, sourceOrdinal, componentOrdinal,
					ordinaryRevision, IdentityScope.CONSTRUCTION_REVISION, address,
					domain, false, evaluator, request, ledger, entities, diagnostics,
					outcomes);
		}
	}

	private void adaptLocus(GeoLocusV2 locus, int sourceOrdinal,
			GeometryExportRequest request, WorkLedger ledger, List<Entity> entities,
			List<Diagnostic> diagnostics, List<SourceExportOutcome> outcomes,
			List<GeometryExportPreflight.SourceRevisionGuard> guards) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		String sourceId = requestSourceId(locus, sourceOrdinal);
		IdentityScope scope = locus.getPersistentLocusId() == null
				? IdentityScope.CONSTRUCTION_REVISION : IdentityScope.PERSISTENT;
		long revision = definition == null ? 0 : definition.getSemanticRevision();
		guards.add(() -> locus.isDefined()
				&& locus.getSemanticDefinition() == definition
				&& locus.getSemanticRevision() == revision);
		if (definition == null || !locus.isDefined()
				|| definition.getDefinitionStatus() != DefinitionStatus.VALID) {
			Reason reason = definition != null
					&& definition.getDefinitionStatus() == DefinitionStatus.UNSUPPORTED
							? Reason.UNSUPPORTED_FAMILY : Reason.UNDEFINED_SOURCE;
			addFailure(locus, sourceId, revision, scope,
					new ComponentAddress(null, "semantic-definition"), reason,
					"Locus V2 has no valid semantic definition for export.", diagnostics,
					outcomes);
			return;
		}
		if (definition.getDeterminism() == Determinism.UNSUPPORTED_NONDETERMINISM) {
			addFailure(locus, sourceId, revision, scope,
					new ComponentAddress(null, "semantic-definition"),
					Reason.UNSUPPORTED_FAMILY,
					"Locus V2 evaluator has no approved deterministic rule.",
					diagnostics, outcomes);
			return;
		}
		int maximumEntries = (int) Math.min(Integer.MAX_VALUE,
				Math.max(1, request.getMaximumEvaluations()));
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.memoizing(maximumEntries)) {
			List<SemanticDomain> overrides = request.getSourceSemanticDomains()
					.get(sourceId);
			if (overrides != null) {
				adaptRequestedLocusDomains(locus, definition, sourceId, sourceOrdinal,
						revision, scope, overrides, request, ledger, session, entities,
						diagnostics, outcomes);
				return;
			}
			int componentOrdinal = 0;
			for (LocusBranch2D branch : definition.getBranches()) {
				List<LocusInterval2D> components = branch.getValidDomainComponents();
				for (int branchComponent = 0;
						branchComponent < components.size(); branchComponent++) {
					LocusInterval2D component = components.get(branchComponent);
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
					String componentKey = "component-" + branchComponent;
					ComponentAddress address = new ComponentAddress(
							branch.getBranchKey(), componentKey, start, end,
							startClosed, endClosed);
					if (start == end) {
						addFailure(locus, sourceId, revision, scope, address,
								Reason.DEGENERATE_SOURCE,
								"A zero-width driver component has no curve export.",
								diagnostics, outcomes);
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
					approximateComponent(locus, sourceId, sourceOrdinal,
							componentOrdinal++, revision, scope, address,
							approximationDomain, semanticClosure, evaluator, request,
							ledger, entities, diagnostics, outcomes);
				}
			}
			if (componentOrdinal == 0) {
				addFailure(locus, sourceId, revision, scope,
						new ComponentAddress(null, "valid-domain"),
						Reason.MISSING_DOMAIN,
						"Locus V2 exposes no valid semantic component.", diagnostics,
						outcomes);
			}
		}
	}

	private void adaptRequestedLocusDomains(GeoLocusV2 locus,
			LocusDefinition2D definition, String sourceId, int sourceOrdinal,
			long revision, IdentityScope scope, List<SemanticDomain> overrides,
			GeometryExportRequest request, WorkLedger ledger,
			LocusEvaluationSession2D session, List<Entity> entities,
			List<Diagnostic> diagnostics, List<SourceExportOutcome> outcomes) {
		if (overrides.isEmpty()) {
			addFailure(locus, sourceId, revision, scope,
					new ComponentAddress(null, "request-domain"),
					Reason.MISSING_DOMAIN,
					"The explicit Locus V2 domain partition is empty.", diagnostics,
					outcomes);
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
				addFailure(locus, sourceId, revision, scope, address,
						decision.reason, decision.message, diagnostics, outcomes);
				continue;
			}
			CurveEvaluator2D evaluator = parameter -> locusEvaluation(definition,
					decision.branch.getBranchKey(), parameter, session);
			boolean semanticClosure = hasFullPeriodClosure(definition,
					decision.branch, decision.component)
					&& sameOrientedInterval(domain, decision.branch,
							decision.component);
			approximateComponent(locus, sourceId, sourceOrdinal, ordinal, revision,
					scope, address, domain, semanticClosure, evaluator, request, ledger,
					entities, diagnostics, outcomes);
		}
	}

	private void approximateComponent(GeoElement geo, String sourceId,
			int sourceOrdinal, int componentOrdinal, long sourceRevision,
			IdentityScope scope, ComponentAddress address, SemanticDomain domain,
			boolean semanticClosure, CurveEvaluator2D evaluator,
			GeometryExportRequest request, WorkLedger ledger, List<Entity> entities,
			List<Diagnostic> diagnostics, List<SourceExportOutcome> outcomes) {
		Result result = approximationBuilder.approximate(evaluator, domain,
				semanticClosure, request, ledger);
		if (!result.isSuccess()) {
			addFailure(geo, sourceId, sourceRevision, scope, address,
					result.getReason(), failureMessage(result.getReason()),
					result.toApproximationEvidence(), diagnostics, outcomes);
			return;
		}
		String neutralId = "entity:g9x1:" + sourceOrdinal + ":"
				+ componentOrdinal;
		Entity entity = new Entity(neutralId, sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(),
				layerName(geo.getLayer()), style(geo), Exactness.APPROXIMATE,
				request.getRequestedTolerance(), result.toPolylineGeometry());
		entities.add(entity);
		outcomes.add(new SourceExportOutcome(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope, address, Fidelity.APPROXIMATE,
				Reason.NONE, neutralId, result.toApproximationEvidence(), null));
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

	private static boolean hasFullPeriodClosure(LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(definition.getProvider().getDeclaredDomain());
	}

	private static List<SemanticDomain> domainsFor(String sourceId,
			GeometryExportRequest request) {
		return request.resolveSemanticDomains(sourceId);
	}

	private static DomainDecision validateGenericDomain(GeoElement geo,
			List<SemanticDomain> domains, int domainOrdinal,
			SemanticDomain domain) {
		if (!domain.isStartClosed() || !domain.isEndClosed()) {
			return DomainDecision.invalid(domain.getBranchKey(),
					Reason.MISSING_DOMAIN,
					"Approximation requires a closed finite export subdomain.");
		}
		for (int otherOrdinal = 0; otherOrdinal < domains.size(); otherOrdinal++) {
			if (otherOrdinal != domainOrdinal
					&& intervalsOverlap(domain, domains.get(otherOrdinal))) {
				return DomainDecision.invalid(domain.getBranchKey(),
						Reason.INVALID_DOMAIN,
						"The declared semantic-domain partition overlaps.");
			}
		}
		String expectedBranch = genericBranchKey(geo);
		if (geo instanceof GeoConic
				&& ((GeoConic) geo).getType()
						== GeoConicNDConstants.CONIC_HYPERBOLA) {
			double lower = Math.min(domain.getStartParameter(),
					domain.getEndParameter());
			double upper = Math.max(domain.getStartParameter(),
					domain.getEndParameter());
			if (lower > -1 && upper < 1) {
				expectedBranch = "hyperbola-right";
			} else if (lower > 1 && upper < 3) {
				expectedBranch = "hyperbola-left";
			} else {
				return DomainDecision.invalid(domain.getBranchKey(),
						Reason.INVALID_DOMAIN,
						"A hyperbola domain must lie strictly inside one branch.");
			}
		} else if (geo instanceof GeoCurveCartesian) {
			GeoCurveCartesian curve = (GeoCurveCartesian) geo;
			double sourceMinimum = curve.getMinParameter();
			double sourceMaximum = curve.getMaxParameter();
			double lower = Math.min(domain.getStartParameter(),
					domain.getEndParameter());
			double upper = Math.max(domain.getStartParameter(),
					domain.getEndParameter());
			if (Double.isNaN(sourceMinimum) || Double.isNaN(sourceMaximum)
					|| lower < sourceMinimum || upper > sourceMaximum) {
				return DomainDecision.invalid(expectedBranch, Reason.INVALID_DOMAIN,
						"The requested domain extends beyond the source path domain.");
			}
		} else if (geo instanceof GeoFunction) {
			GeoFunction function = (GeoFunction) geo;
			double[] sourceInterval = functionInterval(function);
			double lower = Math.min(domain.getStartParameter(),
					domain.getEndParameter());
			double upper = Math.max(domain.getStartParameter(),
					domain.getEndParameter());
			if (sourceInterval != null && (!Double.isFinite(sourceInterval[0])
					|| !Double.isFinite(sourceInterval[1])
					|| sourceInterval[0] > sourceInterval[1]
					|| lower < sourceInterval[0] || upper > sourceInterval[1])) {
				return DomainDecision.invalid(expectedBranch, Reason.INVALID_DOMAIN,
						"The requested domain extends beyond the function interval.");
			}
		}
		if (domain.getBranchKey() != null
				&& !domain.getBranchKey().equals(expectedBranch)) {
			return DomainDecision.invalid(domain.getBranchKey(),
					Reason.INVALID_DOMAIN,
					"The requested branch does not match the source domain.");
		}
		return DomainDecision.valid(expectedBranch);
	}

	private static double[] functionInterval(GeoFunction function) {
		if (function.hasInterval()) {
			return new double[] {function.getIntervalMin(), function.getIntervalMax()};
		}
		ExpressionNode expression = function.getFunctionExpression();
		if (expression == null || expression.getOperation() != Operation.IF) {
			return null;
		}
		ExpressionValue conditionValue = expression.getLeft().unwrap();
		if (!(conditionValue instanceof ExpressionNode)) {
			return null;
		}
		ExpressionNode condition = (ExpressionNode) conditionValue;
		if (condition.getOperation() != Operation.AND_INTERVAL) {
			return null;
		}
		ExpressionValue lowerValue = condition.getLeft().unwrap();
		ExpressionValue upperValue = condition.getRight().unwrap();
		if (!(lowerValue instanceof ExpressionNode)
				|| !(upperValue instanceof ExpressionNode)) {
			return null;
		}
		ExpressionNode lower = (ExpressionNode) lowerValue;
		ExpressionNode upper = (ExpressionNode) upperValue;
		if (lower.getOperation() != Operation.LESS_EQUAL
				|| upper.getOperation() != Operation.LESS_EQUAL
				|| !(lower.getRight().unwrap() instanceof FunctionVariable)
				|| !(upper.getLeft().unwrap() instanceof FunctionVariable)) {
			return null;
		}
		try {
			return new double[] {lower.getLeft().evaluateDouble(),
					upper.getRight().evaluateDouble()};
		} catch (RuntimeException exception) {
			return new double[] {Double.NaN, Double.NaN};
		}
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
				matchingBranch.getValidDomainComponents();
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

	private static boolean intervalsOverlap(SemanticDomain first,
			SemanticDomain second) {
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

	private static CurveEvaluator2D evaluatorFor(GeoElement geo) {
		if (geo instanceof GeoFunction) {
			GeoFunction function = (GeoFunction) geo;
			if (function.isBooleanFunction()) {
				return null;
			}
			return parameter -> function.isFunctionOfY()
					? finitePoint(function.value(parameter), parameter)
					: finitePoint(parameter, function.value(parameter));
		}
		if (geo instanceof GeoCurveCartesian) {
			GeoCurveCartesian curve = (GeoCurveCartesian) geo;
			return parameter -> {
				double[] value = new double[2];
				curve.evaluateCurve(parameter, value);
				return finitePoint(value[0], value[1]);
			};
		}
		if (geo instanceof GeoConic) {
			GeoConic conic = (GeoConic) geo;
			if (conic.getType() != GeoConicNDConstants.CONIC_PARABOLA
					&& conic.getType() != GeoConicNDConstants.CONIC_HYPERBOLA) {
				return null;
			}
			return parameter -> evaluateConic(conic, parameter);
		}
		return null;
	}

	private static CurveEvaluation2D evaluateConic(GeoConic conic,
			double parameter) {
		Coords coordinates = new Coords(3);
		conic.pathChangedWithoutCheck(coordinates, new PathParameter(parameter),
				false);
		double scale = coordinates.getZ();
		if (!Double.isFinite(scale) || scale == 0) {
			return CurveEvaluation2D.invalid(Reason.NON_FINITE);
		}
		return finitePoint(coordinates.getX() / scale,
				coordinates.getY() / scale);
	}

	private static CurveEvaluation2D finitePoint(double x, double y) {
		if (!Double.isFinite(x) || !Double.isFinite(y)) {
			return CurveEvaluation2D.invalid(Reason.NON_FINITE);
		}
		return CurveEvaluation2D.valid(new Point2D(x, y));
	}

	private static boolean mayApproximate(GeoElement geo) {
		if (geo instanceof GeoConicPart) {
			return false;
		}
		if (geo instanceof GeoLocusV2 || geo instanceof GeoFunction
				|| geo instanceof GeoCurveCartesian) {
			return true;
		}
		if (geo instanceof GeoConic) {
			int type = ((GeoConic) geo).getType();
			return type == GeoConicNDConstants.CONIC_PARABOLA
					|| type == GeoConicNDConstants.CONIC_HYPERBOLA;
		}
		return false;
	}

	private static String genericBranchKey(GeoElement geo) {
		if (geo instanceof GeoFunction) {
			return "function";
		}
		if (geo instanceof GeoCurveCartesian) {
			return "parametric-curve";
		}
		if (geo instanceof GeoConic
				&& ((GeoConic) geo).getType()
						== GeoConicNDConstants.CONIC_HYPERBOLA) {
			return "hyperbola";
		}
		return "parabola";
	}

	private static SourceExportOutcome exactOutcome(Entity entity,
			long sourceRevision) {
		return new SourceExportOutcome(entity.getSourceId(), entity.getSourceType(),
				entity.getLabel(), sourceRevision, entity.getStyle().isVisible(),
				IdentityScope.CONSTRUCTION_REVISION,
				new ComponentAddress(null, entity.getNeutralEntityId()), Fidelity.EXACT,
				Reason.NONE, entity.getNeutralEntityId(), null, null);
	}

	private static SourceExportOutcome outcomeFromDiagnostic(GeoElement geo,
			Diagnostic diagnostic, long sourceRevision) {
		Reason reason;
		Fidelity fidelity;
		switch (diagnostic.getCode()) {
		case UNDEFINED:
			reason = Reason.UNDEFINED_SOURCE;
			fidelity = Fidelity.INVALID;
			break;
		case NON_FINITE:
			reason = Reason.NON_FINITE;
			fidelity = Fidelity.INVALID;
			break;
		case NOT_2D:
			reason = Reason.NOT_2D;
			fidelity = Fidelity.INVALID;
			break;
		case DEGENERATE:
			reason = Reason.DEGENERATE_SOURCE;
			fidelity = Fidelity.INVALID;
			break;
		case DUPLICATE_POLYGON_SIDE:
			reason = Reason.DUPLICATE_COMPONENT;
			fidelity = Fidelity.UNSUPPORTED;
			break;
		case UNSUPPORTED:
		default:
			reason = Reason.UNSUPPORTED_FAMILY;
			fidelity = Fidelity.UNSUPPORTED;
			break;
		}
		return new SourceExportOutcome(diagnostic.getSourceId(),
				diagnostic.getSourceType(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), IdentityScope.CONSTRUCTION_REVISION,
				new ComponentAddress(null, "source"), fidelity, reason, null, null,
				diagnostic.getMessage());
	}

	private static void addFailure(GeoElement geo, String sourceId,
			long sourceRevision, IdentityScope scope, ComponentAddress address,
			Reason reason, String message, List<Diagnostic> diagnostics,
			List<SourceExportOutcome> outcomes) {
		addFailure(geo, sourceId, sourceRevision, scope, address, reason, message,
				null, diagnostics, outcomes);
	}

	private static void addFailure(GeoElement geo, String sourceId,
			long sourceRevision, IdentityScope scope, ComponentAddress address,
			Reason reason, String message,
			ApproximationEvidence approximationEvidence,
			List<Diagnostic> diagnostics,
			List<SourceExportOutcome> outcomes) {
		Fidelity fidelity = reason == Reason.UNSUPPORTED_FAMILY
				? Fidelity.UNSUPPORTED : Fidelity.INVALID;
		outcomes.add(new SourceExportOutcome(sourceId,
				geo.getGeoClassType().name(), geo.getLabelSimple(), sourceRevision,
				geo.isEuclidianVisible(), scope, address, fidelity, reason, null,
				approximationEvidence, message));
		diagnostics.add(new Diagnostic(sourceId, geo.getGeoClassType().name(),
				diagnosticCode(reason), message));
	}

	private static DiagnosticCode diagnosticCode(Reason reason) {
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

	private static String failureMessage(Reason reason) {
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

	private static Style style(GeoElement geo) {
		GColor color = geo.getObjectColor();
		return new Style(color.getRed(), color.getGreen(), color.getBlue(),
				geo.isEuclidianVisible());
	}

	private static String layerName(int layer) {
		return layer == 0 ? "0" : "GEOCEDG_L" + layer;
	}

	private static String legacySourceId(GeoElement geo, int ordinal) {
		String label = geo.getLabelSimple();
		String suffix = label == null || label.isEmpty() ? "item-" + ordinal
				: label.replaceAll("[^A-Za-z0-9_.-]", "_");
		return "geo-" + geo.getConstructionIndex() + "-" + suffix;
	}

	private static Map<Construction, ConstructionSnapshot> captureConstructions(
			List<GeoElement> geos) {
		Map<Construction, ConstructionSnapshot> snapshots = new IdentityHashMap<>();
		for (GeoElement geo : geos) {
			if (geo != null) {
				snapshots.computeIfAbsent(geo.getConstruction(),
						ConstructionSnapshot::new);
			}
		}
		return snapshots;
	}

	private static final class DomainDecision {
		private final boolean valid;
		private final String branchKey;
		private final Reason reason;
		private final String message;

		private DomainDecision(boolean valid, String branchKey, Reason reason,
				String message) {
			this.valid = valid;
			this.branchKey = branchKey;
			this.reason = reason;
			this.message = message;
		}

		private static DomainDecision valid(String branchKey) {
			return new DomainDecision(true, branchKey, Reason.NONE, null);
		}

		private static DomainDecision invalid(String branchKey, Reason reason,
				String message) {
			return new DomainDecision(false, branchKey, reason, message);
		}
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

	private static final class ConstructionSnapshot {
		private final Construction construction;
		private final String fingerprint;
		private final long reportedRevision;

		private ConstructionSnapshot(Construction construction) {
			this.construction = construction;
			fingerprint = constructionFingerprint(construction);
			reportedRevision = revisionFrom(fingerprint);
		}

		private long getReportedRevision() {
			return reportedRevision;
		}

		private boolean isCurrent() {
			return fingerprint.equals(constructionFingerprint(construction));
		}
	}

	private static String constructionFingerprint(Construction construction) {
		XMLStringBuilder xml = new XMLStringBuilder();
		construction.beginSpatialIdentityXML();
		try {
			xml.append(new XMLStringBuilder(new StringBuilder(
					construction.getSpatialIdentityRegistry().writeSpatialSection())));
			construction.getConstructionElementsXML(xml, false);
			return xml.toString();
		} finally {
			construction.endSpatialIdentityXML();
		}
	}

	private static long revisionFrom(String fingerprint) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(
					fingerprint.getBytes(StandardCharsets.UTF_8));
			long value = 0;
			for (int index = 0; index < Long.BYTES; index++) {
				value = value << 8 | digest[index] & 0xffL;
			}
			value &= Long.MAX_VALUE;
			return value == 0 ? 1 : value;
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}

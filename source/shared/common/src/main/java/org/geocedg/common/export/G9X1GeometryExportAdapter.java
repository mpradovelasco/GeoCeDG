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
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D.WorkLedger;
import org.geocedg.common.export.GeometryExportModel.AreaExclusion;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.Component;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.ComponentParticipation;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.IdentityScope;
import org.geocedg.common.export.SourceExportOutcome.Reason;
import org.geocedg.common.export.SourceExportOutcome.SemanticCoverage;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.CertifiedComponentAreaDisjointness2D;
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
 * approved curve families receive export-only component approximations; Locus V2
 * and Spline V2 sources go through the shared {@link SemanticCurveExportAdapter2D}.
 * PRE-G9B-R6-plus-C adds the document export context (unit state, hidden
 * layers, explicit export area) to the model and to the currentness check, and
 * applies the {@code B1} export-area participation rule of {@code DQ-C13}.
 */
public final class G9X1GeometryExportAdapter {

	private final GeoElementGeometryExportAdapter exactAdapter;
	private final SemanticCurveExportAdapter2D semanticAdapter;

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
		this.semanticAdapter = new SemanticCurveExportAdapter2D(approximationBuilder);
	}

	/**
	 * Preflight with the document context of the sources (unit state and hidden
	 * layers) and no export area.
	 *
	 * @param geos ordered requested source population
	 * @param selectionMode population provenance
	 * @param request explicit fidelity and work policy
	 * @return complete preflight without destination or file-system access
	 */
	public GeometryExportPreflight preflight(Collection<GeoElement> geos,
			SelectionMode selectionMode, GeometryExportRequest request) {
		if (geos == null) {
			throw new IllegalArgumentException(
					"Sources, selection mode, and request are required");
		}
		List<GeoElement> sources = new ArrayList<>(geos);
		return preflight(geos, selectionMode, request,
				() -> GeoElementGeometryExportAdapter.documentContext(sources));
	}

	/**
	 * @param geos ordered requested source population
	 * @param selectionMode population provenance
	 * @param request explicit fidelity and work policy
	 * @param contextSource current unit state, hidden layers and export area;
	 *        resolved again on every currentness check
	 * @return complete preflight without destination or file-system access
	 */
	public GeometryExportPreflight preflight(Collection<GeoElement> geos,
			SelectionMode selectionMode, GeometryExportRequest request,
			GeometryExportContext.Source contextSource) {
		if (geos == null || selectionMode == null || request == null
				|| contextSource == null) {
			throw new IllegalArgumentException(
					"Sources, selection mode, and request are required");
		}
		if (request.isPartialOutputAllowed()) {
			throw new IllegalArgumentException(
					"G9X1 does not authorize partial component output");
		}
		GeometryExportContext context = requireContext(contextSource.resolve());
		GeometryExportPopulation2D.Result population =
				GeometryExportPopulation2D.select(geos, selectionMode);
		List<GeoElement> ordered = population.getSources();
		Map<Construction, ConstructionSnapshot> constructionSnapshots =
				captureConstructions(ordered);
		GeometryExportModel exactModel = exactAdapter.adapt(ordered, selectionMode,
				GeometryExportContext.UNSPECIFIED);
		Map<String, Entity> exactBySource = new LinkedHashMap<>();
		for (Entity entity : exactModel.getEntities()) {
			exactBySource.put(entity.getSourceId(), entity);
		}
		Map<String, Diagnostic> diagnosticBySource = new LinkedHashMap<>();
		for (Diagnostic diagnostic : exactModel.getDiagnostics()) {
			diagnosticBySource.put(diagnostic.getSourceId(), diagnostic);
		}

		List<Entity> entities = new ArrayList<>();
		List<Diagnostic> diagnostics = new ArrayList<>(
				population.getDiagnostics());
		diagnostics.addAll(exactModel.getDiagnostics());
		List<SourceExportOutcome> outcomes = new ArrayList<>();
		List<AreaExclusion> exclusions = new ArrayList<>();
		List<GeometryExportPreflight.SourceRevisionGuard> guards =
				new ArrayList<>();
		for (ConstructionSnapshot snapshot : constructionSnapshots.values()) {
			guards.add(snapshot::isCurrent);
		}
		guards.add(() -> context.equals(contextSource.resolve()));
		ComponentParticipation participation = participation(context);
		WorkLedger ledger = new WorkLedger(request);
		Set<String> unconsumedOverrideIds = new TreeSet<>(
				request.getSourceSemanticDomains().keySet());
		for (int sourceOrdinal = 0; sourceOrdinal < ordered.size(); sourceOrdinal++) {
			GeoElement geo = ordered.get(sourceOrdinal);
			if (geo == null) {
				continue;
			}
			String legacyId = SemanticCurveExportAdapter2D.legacySourceId(geo,
					sourceOrdinal);
			ConstructionSnapshot constructionSnapshot = constructionSnapshots.get(
					geo.getConstruction());
			long sourceRevision = constructionSnapshot.getReportedRevision();
			Entity exact = exactBySource.get(legacyId);
			if (exact != null) {
				if (context.hasAreaBoundary() && !ExportAreaParticipation2D.meets(
						exact.getGeometry(), context.getArea())) {
					exclusions.add(new AreaExclusion(exact.getSourceId(),
							exact.getSourceType(), exact.getLabel(), sourceRevision,
							exact.getStyle().isVisible(),
							IdentityScope.CONSTRUCTION_REVISION,
							new ComponentAddress(null, exact.getNeutralEntityId())));
					continue;
				}
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
						ledger, participation, entities, diagnostics, outcomes,
						exclusions, guards);
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
				diagnostics, outcomes, context, exclusions);
		return new GeometryExportPreflight(request, model, guards);
	}

	/**
	 * Identifier used by request-domain overrides for one ordered source.
	 * Persistent Locus V2 identity is used when present; ordinary sources remain
	 * scoped to the construction revision.
	 * @return request-domain source identifier
	 */
	public static String requestSourceId(GeoElement geo, int sourceOrdinal) {
		return SemanticCurveExportAdapter2D.requestSourceId(geo, sourceOrdinal);
	}

	/**
	 * {@code DQ-C13}: without an explicit boundary every component participates;
	 * with one, a semantic component is excluded only when the kernel's certified
	 * interval model proves it disjoint from the closed area.
	 */
	private static ComponentParticipation participation(
			GeometryExportContext context) {
		if (!context.hasAreaBoundary()) {
			return SemanticCurveExportAdapter2D.ALL_COMPONENTS;
		}
		GeometryExportArea area = context.getArea();
		return (definition, branchKey, component) ->
				CertifiedComponentAreaDisjointness2D.prove(definition, branchKey,
						component, area.getXmin(), area.getXmax(), area.getYmin(),
						area.getYmax(),
						CertifiedComponentAreaDisjointness2D.DEFAULT_MAXIMUM_BOXES)
						!= CertifiedComponentAreaDisjointness2D.Status.PROVEN_DISJOINT;
	}

	private static GeometryExportContext requireContext(
			GeometryExportContext context) {
		if (context == null) {
			throw new IllegalArgumentException("The export context is required");
		}
		return context;
	}

	private void adaptApproximateSource(GeoElement geo, int sourceOrdinal,
			long ordinaryRevision, GeometryExportRequest request, WorkLedger ledger,
			ComponentParticipation participation, List<Entity> entities,
			List<Diagnostic> diagnostics, List<SourceExportOutcome> outcomes,
			List<AreaExclusion> exclusions,
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
			SemanticCurveExportAdapter2D.SourceResult result = semanticAdapter
					.adaptLocus((GeoLocusV2) geo, sourceOrdinal, request, ledger,
							SemanticCurveExportAdapter2D.G9X1_ENTITY_PREFIX,
							participation);
			guards.add(result.getGuard());
			for (Component component : result.getComponents()) {
				replay(component, entities, diagnostics, outcomes, exclusions);
			}
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
			replay(semanticAdapter.approximate(geo, sourceId, sourceOrdinal,
					componentOrdinal, ordinaryRevision,
					IdentityScope.CONSTRUCTION_REVISION, address, domain, false,
					evaluator, request, ledger,
					SemanticCurveExportAdapter2D.G9X1_ENTITY_PREFIX,
					SemanticCoverage.NOT_APPLICABLE), entities, diagnostics,
					outcomes, exclusions);
		}
	}

	private static void replay(Component component, List<Entity> entities,
			List<Diagnostic> diagnostics, List<SourceExportOutcome> outcomes,
			List<AreaExclusion> exclusions) {
		if (component.isOutsideArea()) {
			exclusions.add(component.getExclusion());
			return;
		}
		if (component.isEmitted()) {
			entities.add(component.getEntity());
			outcomes.add(component.getOutcome());
			return;
		}
		outcomes.add(component.getOutcome());
		diagnostics.add(component.getDiagnostic());
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
					&& SemanticCurveExportAdapter2D.intervalsOverlap(domain,
							domains.get(otherOrdinal))) {
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

	private static CurveEvaluator2D evaluatorFor(GeoElement geo) {
		if (geo instanceof GeoFunction) {
			GeoFunction function = (GeoFunction) geo;
			if (function.isBooleanFunction()) {
				return null;
			}
			return parameter -> function.isFunctionOfY()
					? SemanticCurveExportAdapter2D.finitePoint(
							function.value(parameter), parameter)
					: SemanticCurveExportAdapter2D.finitePoint(parameter,
							function.value(parameter));
		}
		if (geo instanceof GeoCurveCartesian) {
			GeoCurveCartesian curve = (GeoCurveCartesian) geo;
			return parameter -> {
				double[] value = new double[2];
				curve.evaluateCurve(parameter, value);
				return SemanticCurveExportAdapter2D.finitePoint(value[0], value[1]);
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
		return SemanticCurveExportAdapter2D.finitePoint(coordinates.getX() / scale,
				coordinates.getY() / scale);
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
		Component failure = SemanticCurveExportAdapter2D.failure(geo, sourceId,
				sourceRevision, scope, address, reason, message, null);
		outcomes.add(failure.getOutcome());
		diagnostics.add(failure.getDiagnostic());
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

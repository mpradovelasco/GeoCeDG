/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionCompletenessEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootTokenSource2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetSupportStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionTargetSupport2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionContinuation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionQuery2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolver2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTargets2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.locus.intersection.PublicIntersectionRootIdentityResolver2D;
import org.geocedg.common.kernel.locus.intersection.PublicTargetIntersectionCapability2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;

/** Normal-DAG publisher for one internal native Locus V2 intersection query. */
public final class AlgoLocusIntersectionV2 extends AlgoElement {
	private final GeoLocusV2 source;
	private final GeoElement target;
	private String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private String targetIdentity;
	private final String topologyContext;
	private final boolean publicCommand;
	private final GetCommand commandName;
	private final LocusIntersectionCapability2D preferredCapability;
	private final GeoElement[] configuredInputs;
	private final GeoLocusIntersectionResult result;
	private final LocusIntersectionSolver2D solver =
			new LocusIntersectionSolver2D();
	private final LocusIntersectionContinuation2D continuation =
			new LocusIntersectionContinuation2D();
	private final PublicIntersectionRootIdentityResolver2D publicIdentityResolver =
			new PublicIntersectionRootIdentityResolver2D();
	private LocusIntersectionResult2D lastContinuableResult;
	private long targetUpdateStamp;
	private long nextRootToken;

	/**
	 * Creates, wires and computes one internal rich intersection.
	 *
	 * @param capabilityDependencies extra GeoElements read by the explicitly
	 *        supplied capability; may be empty but never hidden
	 */
	public AlgoLocusIntersectionV2(Construction construction, GeoLocusV2 source,
			GeoElement target, String sourcePairIdentity,
			String constructiveIntersectionLineage, String targetIdentity,
			String topologyContext,
			LocusIntersectionCapability2D preferredCapability,
			GeoElement[] capabilityDependencies) {
		this(construction, source, target, sourcePairIdentity,
				constructiveIntersectionLineage, targetIdentity, topologyContext,
				preferredCapability, capabilityDependencies, false,
				Algos.Expression, null, null);
	}

	/** Creates a reconstructible public rich intersection result. */
	public AlgoLocusIntersectionV2(Construction construction, String label,
			GeoLocusV2 source, GeoElement target, PersistentGeoId resultId) {
		this(construction, source, target, "g9u0-pending-source-pair",
				"g9u0-public-intersection/v1", "g9u0-pending-target",
				"g9u0-public-topology/v1",
				new PublicTargetIntersectionCapability2D(), new GeoElement[0], true,
				Commands.Intersect, label, resultId);
	}

	private AlgoLocusIntersectionV2(Construction construction,
			GeoLocusV2 source, GeoElement target, String sourcePairIdentity,
			String constructiveIntersectionLineage, String targetIdentity,
			String topologyContext,
			LocusIntersectionCapability2D preferredCapability,
			GeoElement[] capabilityDependencies, boolean addToConstructionList,
			GetCommand commandName, String label,
			PersistentGeoId reservedResultId) {
		super(construction, addToConstructionList);
		this.source = java.util.Objects.requireNonNull(source);
		this.target = java.util.Objects.requireNonNull(target);
		this.sourcePairIdentity = requireText(sourcePairIdentity,
				"Source-pair identity");
		this.constructiveIntersectionLineage = requireText(
				constructiveIntersectionLineage, "Intersection lineage");
		this.targetIdentity = requireText(targetIdentity, "Target identity");
		this.topologyContext = requireText(topologyContext, "Topology context");
		this.publicCommand = addToConstructionList;
		if (addToConstructionList && !construction.isFileLoading()) {
			java.util.Objects.requireNonNull(reservedResultId);
		}
		this.commandName = commandName;
		this.preferredCapability = preferredCapability;
		this.configuredInputs = combineInputs(source, target,
				capabilityDependencies);
		this.result = new GeoLocusIntersectionResult(construction,
				sourcePairIdentity);
		if (publicCommand) {
			result.enablePublicPersistence();
		}
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
		if (label != null) {
			result.setLabel(label);
		}
	}

	/** Convenience constructor with evaluator-only candidate discovery. */
	public AlgoLocusIntersectionV2(Construction construction, GeoLocusV2 source,
			GeoElement target, String sourcePairIdentity,
			String constructiveIntersectionLineage, String targetIdentity,
			String topologyContext) {
		this(construction, source, target, sourcePairIdentity,
				constructiveIntersectionLineage, targetIdentity, topologyContext,
				null, new GeoElement[0]);
	}

	@Override
	protected void setInputOutput() {
		input = configuredInputs;
		setOnlyOutput(result);
	}

	@Override
	public void compute() {
		if (!result.isTokenLedgerReadyForEvaluation()) {
			lastContinuableResult = null;
			result.deferUntilPersistentIdentityAttachment();
			return;
		}
		if (!refreshPublicIdentities()) {
			lastContinuableResult = null;
			result.deferUntilPersistentIdentityAttachment();
			return;
		}
		LocusDefinition2D definition = source.getSemanticDefinition();
		long locusRevision = Math.max(1, source.getSemanticRevision());
		targetUpdateStamp++;
		String providerId = definition == null ? "unavailable-provider/v1"
				: definition.getProvider().getProviderId();
		String parameterDescriptor = definition == null
				? "unavailable semantic parameter"
				: definition.getProvider().getParameterDescriptor();
		LocusIntersectionPolicy2D basePolicy = LocusIntersectionPolicy2D.initial(
				providerId, parameterDescriptor);
		IntersectionTargetSupport2D support =
				LocusIntersectionTargets2D.assess(target);
		if (definition == null || !source.isDefined()) {
			LocusIntersectionQuery2D query = query(locusRevision, basePolicy);
			IntersectionSourceBinding2D binding =
					new IntersectionSourceBinding2D(query, support.getFamily());
			result.beginIntersectionRevision(binding);
			publishWithLedger(binding,
					failure(binding, ComputationStatus.INVALID_INPUT,
							DiagnosticCode.INVALID_SOURCE,
							"Source locus has no current defined semantic snapshot",
							basePolicy));
			return;
		}
		if (!support.isSupported()) {
			LocusIntersectionQuery2D query = query(locusRevision, basePolicy);
			IntersectionSourceBinding2D binding =
					new IntersectionSourceBinding2D(query, TargetFamily.UNSUPPORTED);
			result.beginIntersectionRevision(binding);
			ComputationStatus status = support.getStatus()
					== TargetSupportStatus.TARGET_UNDEFINED
							? ComputationStatus.INVALID_INPUT
							: ComputationStatus.UNSUPPORTED;
			publishWithLedger(binding,
					failure(binding, status, support.getDiagnosticCode(),
							support.getStatus() + ": " + support.getDiagnostic(),
							basePolicy));
			lastContinuableResult = null;
			return;
		}
		try {
			LocusIntersectionTarget2D captured =
					LocusIntersectionTargets2D.capture(target, targetIdentity,
							targetUpdateStamp);
			LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
					providerId, parameterDescriptor,
					captured.getResidualContract());
			LocusIntersectionQuery2D query = query(locusRevision, policy);
			IntersectionSourceBinding2D binding =
					new IntersectionSourceBinding2D(query, captured.getFamily());
			result.beginIntersectionRevision(binding);
			LocusIntersectionTokenLedger2D.Evaluation evaluation =
					beginTokenEvaluation();
			try {
				IntersectionRootTokenSource2D tokenSource = evaluation == null
						? this::nextLegacyToken
						: IntersectionRootTokenSource2D.semantic(
								evaluation::mint,
								evaluation::revisionLocalHandle);
				LocusIntersectionResult2D candidate = solver.intersect(query,
						definition, captured, binding, preferredCapability,
						tokenSource);
				LocusIntersectionResult2D current = publicCommand
						? publicIdentityResolver.resolve(lastContinuableResult,
								candidate, definition, captured, policy, evaluation)
						: continuation.continueRoots(lastContinuableResult,
								candidate, policy);
				publishWithLedger(binding, current, evaluation);
				updateContinuationBaseline(current);
			} catch (RuntimeException exception) {
				result.abortTokenEvaluation(evaluation);
				throw exception;
			}
		} catch (RuntimeException exception) {
			LocusIntersectionQuery2D query = query(locusRevision, basePolicy);
			IntersectionSourceBinding2D binding =
					new IntersectionSourceBinding2D(query, support.getFamily());
			result.beginIntersectionRevision(binding);
			publishWithLedger(binding,
					failure(binding, ComputationStatus.NUMERICAL_FAILURE,
							DiagnosticCode.INTERNAL_FAILURE,
							"Intersection target capture or publication failed: "
									+ exception.getClass().getSimpleName(),
							basePolicy));
		}
	}

	private LocusIntersectionQuery2D query(long locusRevision,
			LocusIntersectionPolicy2D policy) {
		return new LocusIntersectionQuery2D(sourcePairIdentity,
				constructiveIntersectionLineage, source.getLocusIdentity(),
				locusRevision, targetIdentity, targetUpdateStamp, topologyContext,
				policy);
	}

	public GeoLocusIntersectionResult getResult() {
		return result;
	}

	public GeoLocusV2 getSource() {
		return source;
	}

	public GeoElement getTarget() {
		return target;
	}

	/** @return durable constructive lineage declared by this public algorithm */
	public String getConstructiveIntersectionLineage() {
		return constructiveIntersectionLineage;
	}

	/** @return durable topology context declared by this public algorithm */
	public String getTopologyContext() {
		return topologyContext;
	}

	@Override
	public void remove() {
		lastContinuableResult = null;
		super.remove();
	}

	@Override
	public GetCommand getClassName() {
		return commandName;
	}

	private boolean refreshPublicIdentities() {
		if (!publicCommand) {
			return true;
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId targetId = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(target);
		PersistentGeoId resultId = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(result);
		if (sourceId == null || targetId == null || resultId == null) {
			return false;
		}
		targetIdentity = targetId.toExternalForm();
		sourcePairIdentity = framed(sourceId.toExternalForm())
				+ framed(targetIdentity);
		result.refreshSourcePairIdentity(sourcePairIdentity);
		return true;
	}

	private String nextLegacyToken() {
		if (nextRootToken == Long.MAX_VALUE) {
			throw new IllegalStateException("Root token sequence exhausted");
		}
		nextRootToken++;
		return sourcePairIdentity + "/opaque-root-" + nextRootToken;
	}

	private String resultOwnerIdentity() {
		PersistentGeoId id = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(result);
		if (id == null) {
			throw new IllegalStateException(
					"Public intersection result has no attached identity");
		}
		return id.toExternalForm();
	}

	private LocusIntersectionTokenLedger2D.Evaluation beginTokenEvaluation() {
		return publicCommand ? result.beginTokenEvaluation(resultOwnerIdentity(),
				constructiveIntersectionLineage, topologyContext) : null;
	}

	private void publishWithLedger(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D current) {
		LocusIntersectionTokenLedger2D.Evaluation evaluation =
				beginTokenEvaluation();
		try {
			publishWithLedger(binding, current, evaluation);
		} catch (RuntimeException exception) {
			result.abortTokenEvaluation(evaluation);
			throw exception;
		}
	}

	private void publishWithLedger(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D current,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		if (evaluation == null) {
			result.publishIntersectionResult(binding, current);
		} else {
			result.publishIntersectionResult(binding, current, evaluation);
		}
	}

	private void updateContinuationBaseline(LocusIntersectionResult2D current) {
		if (current.getComputationStatus() == ComputationStatus.SUCCESS
				&& current.getGeometryKind() == GeometryKind.FINITE) {
			lastContinuableResult = current;
		} else if (current.getComputationStatus() == ComputationStatus.SUCCESS
				&& (current.getGeometryKind() == GeometryKind.OVERLAP
						|| current.getGeometryKind()
								== GeometryKind.INFINITELY_MANY)) {
			lastContinuableResult = null;
		}
	}

	private static LocusIntersectionResult2D failure(
			IntersectionSourceBinding2D binding, ComputationStatus status,
			DiagnosticCode code, String message,
			LocusIntersectionPolicy2D policy) {
		LocusIntersectionInstrumentation2D instrumentation =
				new LocusIntersectionInstrumentation2D(policy.getWorkBudget());
		instrumentation.recordFailedPrivateComputation();
		instrumentation.recordPublishedSnapshot();
		List<IntersectionDiagnostic2D> diagnostics = List.of(
				new IntersectionDiagnostic2D(code, message));
		return new LocusIntersectionResult2D(binding, status,
				new IntersectionCompletenessEvidence2D(
						Completeness.NOT_ESTABLISHED,
						CompletenessMethod.NOT_ESTABLISHED, 0,
						Collections.emptyList(), diagnostics),
				GeometryKind.UNRESOLVED, Currentness.CURRENT,
				SupportLevel.UNSUPPORTED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				Collections.emptyList(), Collections.emptyList(),
				instrumentation.snapshot(), diagnostics);
	}

	private static GeoElement[] combineInputs(GeoLocusV2 source,
			GeoElement target, GeoElement[] dependencies) {
		ArrayList<GeoElement> inputs = new ArrayList<>();
		inputs.add(source);
		inputs.add(target);
		if (dependencies != null) {
			for (GeoElement dependency : dependencies) {
				if (dependency == null) {
					throw new IllegalArgumentException(
							"Capability dependencies cannot contain null");
				}
				if (!inputs.contains(dependency)) {
					inputs.add(dependency);
				}
			}
		}
		return inputs.toArray(new GeoElement[0]);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}
}

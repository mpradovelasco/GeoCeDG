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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionContinuation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionInstrumentation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionQuery2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolver2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTargets2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoElement;

/** Normal-DAG publisher for one internal native Locus V2 intersection query. */
public final class AlgoLocusIntersectionV2 extends AlgoElement {
	private final GeoLocusV2 source;
	private final GeoElement target;
	private final String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private final String targetIdentity;
	private final String topologyContext;
	private final LocusIntersectionCapability2D preferredCapability;
	private final GeoElement[] configuredInputs;
	private final GeoLocusIntersectionResult result;
	private final LocusIntersectionSolver2D solver =
			new LocusIntersectionSolver2D();
	private final LocusIntersectionContinuation2D continuation =
			new LocusIntersectionContinuation2D();
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
		super(construction, false);
		this.source = java.util.Objects.requireNonNull(source);
		this.target = java.util.Objects.requireNonNull(target);
		this.sourcePairIdentity = requireText(sourcePairIdentity,
				"Source-pair identity");
		this.constructiveIntersectionLineage = requireText(
				constructiveIntersectionLineage, "Intersection lineage");
		this.targetIdentity = requireText(targetIdentity, "Target identity");
		this.topologyContext = requireText(topologyContext, "Topology context");
		this.preferredCapability = preferredCapability;
		this.configuredInputs = combineInputs(source, target,
				capabilityDependencies);
		this.result = new GeoLocusIntersectionResult(construction,
				sourcePairIdentity);
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
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
		LocusDefinition2D definition = source.getSemanticDefinition();
		long locusRevision = Math.max(1, source.getSemanticRevision());
		targetUpdateStamp++;
		String providerId = definition == null ? "unavailable-provider/v1"
				: definition.getProvider().getProviderId();
		String parameterDescriptor = definition == null
				? "unavailable semantic parameter"
				: definition.getProvider().getParameterDescriptor();
		LocusIntersectionPolicy2D policy = LocusIntersectionPolicy2D.initial(
				providerId, parameterDescriptor);
		LocusIntersectionQuery2D query = new LocusIntersectionQuery2D(
				sourcePairIdentity, constructiveIntersectionLineage,
				source.getLocusIdentity(), locusRevision, targetIdentity,
				targetUpdateStamp, topologyContext, policy);
		TargetFamily family = LocusIntersectionTargets2D.familyOf(target);
		IntersectionSourceBinding2D binding =
				new IntersectionSourceBinding2D(query, family);
		result.beginIntersectionRevision(binding);
		if (definition == null || !source.isDefined()) {
			result.publishIntersectionResult(binding,
					failure(binding, ComputationStatus.INVALID_INPUT,
							DiagnosticCode.INVALID_SOURCE,
							"Source locus has no current defined semantic snapshot",
							policy));
			return;
		}
		if (family == TargetFamily.UNSUPPORTED) {
			result.publishIntersectionResult(binding,
					failure(binding, ComputationStatus.UNSUPPORTED,
							DiagnosticCode.UNSUPPORTED_TARGET,
							"Target family is outside the G8B internal minimum",
							policy));
			lastContinuableResult = null;
			return;
		}
		try {
			LocusIntersectionTarget2D captured =
					LocusIntersectionTargets2D.capture(target, targetIdentity,
							targetUpdateStamp);
			LocusIntersectionResult2D candidate = solver.intersect(query,
					definition, captured, binding, preferredCapability,
					this::nextToken);
			LocusIntersectionResult2D current = continuation.continueRoots(
					lastContinuableResult, candidate, policy);
			result.publishIntersectionResult(binding, current);
			updateContinuationBaseline(current);
		} catch (RuntimeException exception) {
			result.publishIntersectionResult(binding,
					failure(binding, ComputationStatus.NUMERICAL_FAILURE,
							DiagnosticCode.INTERNAL_FAILURE,
							"Intersection target capture or publication failed: "
									+ exception.getClass().getSimpleName(), policy));
		}
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

	@Override
	public void remove() {
		lastContinuableResult = null;
		super.remove();
	}

	@Override
	public Algos getClassName() {
		return Algos.Expression;
	}

	private String nextToken() {
		if (nextRootToken == Long.MAX_VALUE) {
			throw new IllegalStateException("Root token sequence exhausted");
		}
		nextRootToken++;
		return sourcePairIdentity + "/opaque-root-" + nextRootToken;
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
}

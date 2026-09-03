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
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionContinuation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIdentity2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionInstrumentation2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionQuery2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionSolver2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairRootTokenSource2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;

/** Normal-DAG publisher for one internal Locus V2 x Locus V2 query. */
public final class AlgoLocusLocusIntersectionV2 extends AlgoElement {
	private final GeoLocusV2 callerFirst;
	private final GeoLocusV2 callerSecond;
	private String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private final String topologyContext;
	private final LocusPairIntersectionCapability2D preferredCapability;
	private final boolean publicCommand;
	private final GetCommand commandName;
	private final GeoElement[] configuredInputs;
	private final GeoLocusIntersectionResult result;
	private final LocusPairIntersectionSolver2D solver =
			new LocusPairIntersectionSolver2D();
	private final LocusIntersectionContinuation2D continuation =
			new LocusIntersectionContinuation2D();
	private LocusIntersectionResult2D lastContinuableResult;
	private long tokenEpoch;

	/** Creates, wires and computes one internal two-source rich result. */
	public AlgoLocusLocusIntersectionV2(Construction construction,
			GeoLocusV2 callerFirst, GeoLocusV2 callerSecond,
			String constructiveIntersectionLineage, String topologyContext,
			LocusPairIntersectionCapability2D preferredCapability,
			GeoElement[] capabilityDependencies) {
		this(construction, callerFirst, callerSecond,
				constructiveIntersectionLineage, topologyContext,
				preferredCapability, capabilityDependencies, false,
				Algos.Expression, null, null);
	}

	/** Creates a reconstructible public rich locus-pair intersection. */
	public AlgoLocusLocusIntersectionV2(Construction construction, String label,
			GeoLocusV2 callerFirst, GeoLocusV2 callerSecond,
			PersistentGeoId resultId) {
		this(construction, callerFirst, callerSecond,
				"g9u0-public-locus-pair-intersection/v1",
				"g9u0-public-topology/v1", null, new GeoElement[0], true,
				Commands.Intersect, label, resultId);
	}

	private AlgoLocusLocusIntersectionV2(Construction construction,
			GeoLocusV2 callerFirst, GeoLocusV2 callerSecond,
			String constructiveIntersectionLineage, String topologyContext,
			LocusPairIntersectionCapability2D preferredCapability,
			GeoElement[] capabilityDependencies, boolean addToConstructionList,
			GetCommand commandName, String label,
			PersistentGeoId reservedResultId) {
		super(construction, addToConstructionList);
		this.callerFirst = java.util.Objects.requireNonNull(callerFirst);
		this.callerSecond = java.util.Objects.requireNonNull(callerSecond);
		this.sourcePairIdentity = addToConstructionList
				? initialSourcePairIdentity(callerFirst, callerSecond)
				: LocusPairIdentity2D.sourcePair(
						callerFirst.getLocusIdentity(),
						callerSecond.getLocusIdentity());
		this.constructiveIntersectionLineage = requireText(
				constructiveIntersectionLineage, "Intersection lineage");
		this.topologyContext = requireText(topologyContext, "Topology context");
		this.preferredCapability = preferredCapability;
		this.publicCommand = addToConstructionList;
		if (addToConstructionList && !construction.isFileLoading()) {
			java.util.Objects.requireNonNull(reservedResultId);
		}
		this.commandName = commandName;
		// Public command arity must survive XML even for Intersect(S,S).
		// The identity graph separately stores its one distinct dependency.
		this.configuredInputs = publicCommand
				? new GeoElement[] {callerFirst, callerSecond}
				: combineInputs(callerFirst, callerSecond, capabilityDependencies);
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

	/** Convenience constructor with evaluator-only parameter boxes. */
	public AlgoLocusLocusIntersectionV2(Construction construction,
			GeoLocusV2 first, GeoLocusV2 second,
			String constructiveIntersectionLineage, String topologyContext) {
		this(construction, first, second, constructiveIntersectionLineage,
				topologyContext, null, new GeoElement[0]);
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
		if (!refreshPublicIdentity()) {
			lastContinuableResult = null;
			result.deferUntilPersistentIdentityAttachment();
			return;
		}
		LocusDefinition2D firstDefinition = callerFirst.getSemanticDefinition();
		LocusDefinition2D secondDefinition = callerSecond.getSemanticDefinition();
		if (firstDefinition == null || secondDefinition == null
				|| !callerFirst.isDefined() || !callerSecond.isDefined()) {
			IntersectionSourceBinding2D binding =
					IntersectionSourceBinding2D.unavailableLocusPair(
							callerFirst.getLocusIdentity(),
							callerSecond.getLocusIdentity(),
							constructiveIntersectionLineage, topologyContext);
			result.beginIntersectionRevision(binding);
			publishWithLedger(binding, failure(binding,
					ComputationStatus.INVALID_INPUT, DiagnosticCode.INVALID_SOURCE,
					"Both Locus V2 sources require current semantic snapshots"));
			return;
		}
		LocusPairIntersectionPolicy2D callerPolicy =
				LocusPairIntersectionPolicy2D.initial(firstDefinition,
						secondDefinition);
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(
				firstDefinition, secondDefinition,
				constructiveIntersectionLineage, topologyContext, callerPolicy);
		LocusDefinition2D canonicalFirst = query.isCallerOrderCanonical()
				? firstDefinition : secondDefinition;
		LocusDefinition2D canonicalSecond = query.isCallerOrderCanonical()
				? secondDefinition : firstDefinition;
		IntersectionSourceBinding2D binding =
				new IntersectionSourceBinding2D(query);
		result.beginIntersectionRevision(binding);
		LocusIntersectionTokenLedger2D.Evaluation evaluation = null;
		try {
			if (publicCommand) {
				evaluation = beginTokenEvaluation();
			}
			long currentTokenEpoch = nextTokenEpoch();
			LocusPairRootTokenSource2D tokenSource =
					lineage -> provisionalToken(lineage, currentTokenEpoch);
			LocusIntersectionResult2D candidate = solver.intersect(query,
					canonicalFirst, canonicalSecond, binding, preferredCapability,
					tokenSource, evaluation);
			LocusIntersectionResult2D current = publicCommand ? candidate
					: continuation.continuePairRoots(lastContinuableResult,
							candidate, query.getPolicy());
			if (evaluation == null) {
				result.publishIntersectionResult(binding, current);
			} else {
				result.publishIntersectionResult(binding, current, evaluation);
			}
			updateContinuationBaseline(current);
		} catch (RuntimeException exception) {
			if (evaluation != null) {
				result.abortTokenEvaluation(evaluation);
			}
			publishWithLedger(binding, failure(binding,
					ComputationStatus.NUMERICAL_FAILURE,
					DiagnosticCode.INTERNAL_FAILURE,
					"Pair capture or publication failed: "
							+ exception.getClass().getSimpleName()));
		}
	}

	public GeoLocusIntersectionResult getResult() {
		return result;
	}

	public GeoLocusV2 getCallerFirst() {
		return callerFirst;
	}

	public GeoLocusV2 getCallerSecond() {
		return callerSecond;
	}

	public String getConstructiveIntersectionLineage() {
		return constructiveIntersectionLineage;
	}

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

	private boolean refreshPublicIdentity() {
		if (!publicCommand) {
			return true;
		}
		PersistentGeoId firstId = callerFirst.getPersistentLocusId();
		PersistentGeoId secondId = callerSecond.getPersistentLocusId();
		PersistentGeoId resultId = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(result);
		if (firstId == null || secondId == null || resultId == null) {
			return false;
		}
		sourcePairIdentity = LocusPairIdentity2D.sourcePair(
				firstId.toExternalForm(), secondId.toExternalForm());
		result.refreshSourcePairIdentity(sourcePairIdentity);
		return true;
	}

	private void updateContinuationBaseline(LocusIntersectionResult2D current) {
		if (current.getComputationStatus() == ComputationStatus.SUCCESS
				&& (current.getGeometryKind() == GeometryKind.FINITE
						|| current.getGeometryKind()
								== GeometryKind.MIXED_FINITE_OVERLAP)) {
			lastContinuableResult = current;
		} else if (current.getComputationStatus() == ComputationStatus.SUCCESS
				&& (current.getGeometryKind() == GeometryKind.OVERLAP
						|| current.getGeometryKind()
								== GeometryKind.INFINITELY_MANY)) {
			lastContinuableResult = null;
		}
	}

	private long nextTokenEpoch() {
		if (tokenEpoch == Long.MAX_VALUE) {
			throw new IllegalStateException("Pair token epoch exhausted");
		}
		return ++tokenEpoch;
	}

	private String provisionalToken(String solutionLineage, long epoch) {
		return LocusPairIdentity2D.solutionToken(sourcePairIdentity,
				constructiveIntersectionLineage, topologyContext,
				solutionLineage + "/appearance-epoch-" + epoch);
	}

	private void publishWithLedger(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D current) {
		if (publicCommand) {
			// An unavailable snapshot makes retained slots dormant, not retired.
			result.publishIntersectionResult(binding, current,
					beginTokenEvaluation());
		} else {
			result.publishIntersectionResult(binding, current);
		}
	}

	private LocusIntersectionTokenLedger2D.Evaluation beginTokenEvaluation() {
		return result.beginTokenEvaluation(cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(result).toExternalForm(),
				constructiveIntersectionLineage, topologyContext);
	}

	private static LocusIntersectionResult2D failure(
			IntersectionSourceBinding2D binding, ComputationStatus status,
			DiagnosticCode code, String message) {
		LocusPairIntersectionInstrumentation2D instrumentation =
				new LocusPairIntersectionInstrumentation2D(
						unavailablePolicy());
		instrumentation.recordFailedPrivateComputation();
		instrumentation.recordPublishedSnapshot();
		List<IntersectionDiagnostic2D> diagnostics =
				List.of(new IntersectionDiagnostic2D(code, message));
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

	private static LocusPairIntersectionPolicy2D unavailablePolicy() {
		LocusIntersectionPolicy2D.ParameterTolerance first =
				new LocusIntersectionPolicy2D.ParameterTolerance(1E-12,
						"unavailable-first/v1", "unavailable parameter");
		LocusIntersectionPolicy2D.ParameterTolerance second =
				new LocusIntersectionPolicy2D.ParameterTolerance(1E-12,
						"unavailable-second/v1", "unavailable parameter");
		return new LocusPairIntersectionPolicy2D(
				LocusPairIntersectionPolicy2D.POLICY_VERSION, first, second,
				first, second,
				new LocusIntersectionPolicy2D.ResidualTolerance(
						org.geocedg.common.kernel.locus.intersection
								.IntersectionSemanticMetadata2D.ResidualQuantityKind
								.MODEL_COORDINATE_DISTANCE,
						"model-coordinate", 2E-12, 2E-12,
						"max(1,coordinate-magnitude-of-both-sources)"),
				new LocusIntersectionPolicy2D.TangencyTolerance(
						"normalized-two-source-tangent-determinant",
						"dimensionless", 1E-10),
				new LocusIntersectionPolicy2D.CoordinateTolerance(4E-12,
						"model-coordinate"),
				org.geocedg.common.kernel.locus.intersection
						.LocusIntersectionWorkBudget2D.initial(),
				org.geocedg.common.kernel.locus.intersection
						.LocusPairIntersectionWorkBudget2D.initial());
	}

	private static GeoElement[] combineInputs(GeoLocusV2 first,
			GeoLocusV2 second, GeoElement[] dependencies) {
		ArrayList<GeoElement> inputs = new ArrayList<>();
		inputs.add(first);
		if (second != first) {
			inputs.add(second);
		}
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

	private static String initialSourcePairIdentity(GeoLocusV2 first,
			GeoLocusV2 second) {
		PersistentGeoId firstId = first.getPersistentLocusId();
		PersistentGeoId secondId = second.getPersistentLocusId();
		return firstId == null || secondId == null
				? "g9u0-pending-locus-pair"
				: LocusPairIdentity2D.sourcePair(firstId.toExternalForm(),
						secondId.toExternalForm());
	}
}

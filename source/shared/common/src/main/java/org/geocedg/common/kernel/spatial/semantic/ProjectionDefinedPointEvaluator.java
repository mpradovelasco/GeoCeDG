/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/** Reconstructs the admitted point pilot from defining diagram observations. */
public final class ProjectionDefinedPointEvaluator {
	private static final String SVD_METHOD = "Apache Commons Math SVD";
	private final ProjectionSystemEvaluator systemEvaluator;

	public ProjectionDefinedPointEvaluator() {
		this(new ProjectionSystemEvaluator());
	}

	public ProjectionDefinedPointEvaluator(ProjectionSystemEvaluator systemEvaluator) {
		this.systemEvaluator = Objects.requireNonNull(systemEvaluator);
	}

	/**
	 * Evaluates observations without frame relations using fresh instrumentation.
	 *
	 * @return atomic projection-defined point certificate
	 */
	public SpatialPointCertificate evaluate(List<ProjectionObservation> observations,
			NumericPolicy policy) {
		return evaluate(observations, Collections.emptyList(), policy,
				new SpatialSemanticInstrumentation());
	}

	/**
	 * Evaluates observations without frame relations using supplied instrumentation.
	 *
	 * @return atomic projection-defined point certificate
	 */
	public SpatialPointCertificate evaluate(List<ProjectionObservation> observations,
			NumericPolicy policy, SpatialSemanticInstrumentation instrumentation) {
		return evaluate(observations, Collections.emptyList(), policy, instrumentation);
	}

	/**
	 * Evaluates observations and their explicit frame relations.
	 *
	 * @return atomic projection-defined point certificate
	 */
	public SpatialPointCertificate evaluate(List<ProjectionObservation> observations,
			List<ProjectionRelationDefinition> relations, NumericPolicy policy,
			SpatialSemanticInstrumentation instrumentation) {
		Objects.requireNonNull(observations);
		Objects.requireNonNull(relations);
		Objects.requireNonNull(policy);
		Objects.requireNonNull(instrumentation);
		observations = new ArrayList<>(observations);
		relations = new ArrayList<>(relations);
		observations.sort(Comparator.comparing(ProjectionObservation::getEvidenceKey));
		relations.sort(Comparator.comparing(
				ProjectionRelationDefinition::getEvidenceKey));
		requireDistinctObservationKeys(observations);
		requireDistinctRelationKeys(relations);
		long sourceRevision = maximumRevision(observations, relations);
		RepresentationFidelity fidelity = aggregateFidelity(observations);
		CorrespondenceStatus correspondence = aggregateCorrespondence(observations);
		ProjectionSystemCertificate systemCertificate = systemEvaluator
				.evaluateObservations(observations, relations, policy, instrumentation);

		if (observations.isEmpty()) {
			return failure(systemCertificate, SpatialDefinitionStatus.DEFINED,
					SpatialCertificateStatus.UNDERDETERMINED, fidelity,
					NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
					0, new double[0], Double.NaN, Collections.emptyList(),
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					instrumentation);
		}
		SpatialDefinitionStatus inputStatus = aggregateDefinition(observations);
		if (inputStatus != SpatialDefinitionStatus.DEFINED) {
			SpatialCertificateStatus status = inputStatus == SpatialDefinitionStatus.DEGENERATE
					? SpatialCertificateStatus.DEGENERATE
					: SpatialCertificateStatus.UNDEFINED;
			return failure(systemCertificate, inputStatus, status, fidelity,
					NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
					0, new double[0], Double.NaN, Collections.emptyList(),
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					instrumentation);
		}
		if (correspondence == CorrespondenceStatus.BROKEN) {
			return failure(systemCertificate, SpatialDefinitionStatus.DEFINED,
					SpatialCertificateStatus.UNDEFINED, fidelity,
					NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
					0, new double[0], Double.NaN, Collections.emptyList(),
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					instrumentation);
		}
		if (correspondence == CorrespondenceStatus.AMBIGUOUS) {
			return failure(systemCertificate, SpatialDefinitionStatus.DEFINED,
					SpatialCertificateStatus.AMBIGUOUS, fidelity,
					NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
					0, new double[0], Double.NaN, Collections.emptyList(),
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					instrumentation);
		}
		if (systemCertificate.getStatus() != ProjectionSystemStatus.CONSISTENT) {
			SpatialCertificateStatus status = systemFailureStatus(
					systemCertificate.getStatus());
			return failure(systemCertificate, systemFailureDefinitionStatus(
					systemCertificate.getStatus()), status, fidelity,
					NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
					0, new double[0], Double.NaN, Collections.emptyList(),
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					instrumentation);
		}

		instrumentation.recordReconstructionAttempt();
		int rowCount = observations.size() * 2;
		double[][] matrixData = new double[rowCount][3];
		double[] rightHandSide = new double[rowCount];
		List<Vector2> intrinsicPoints = new ArrayList<>();
		for (int index = 0; index < observations.size(); index++) {
			ProjectionObservation observation = Objects.requireNonNull(
					observations.get(index));
			if (!observation.getDiagramPoint().isFinite()) {
				return failure(systemCertificate, SpatialDefinitionStatus.UNDEFINED,
						SpatialCertificateStatus.UNDEFINED, fidelity,
						NumericalEvidenceStatus.UNRESOLVED, correspondence,
						sourceRevision, 0, new double[0], Double.NaN,
						Collections.emptyList(), Double.POSITIVE_INFINITY,
						Double.POSITIVE_INFINITY, instrumentation);
			}
			Vector2 intrinsic = observation.getDiagramMap().inverse(
					observation.getDiagramPoint());
			instrumentation.recordDiagramMapInverseEvaluation();
			if (!intrinsic.isFinite()) {
				return undefinedBeforeCandidate(systemCertificate, fidelity,
						correspondence, sourceRevision, instrumentation);
			}
			intrinsicPoints.add(intrinsic);
			ProjectionFrameDefinition frame = observation.getFrame();
			fillRow(matrixData[2 * index], frame.getFirstProjectionRow());
			fillRow(matrixData[2 * index + 1], frame.getSecondProjectionRow());
			double firstRightHandSide = intrinsic.getX()
					+ frame.getFirstProjectionRow().dot(frame.getOrigin());
			double secondRightHandSide = intrinsic.getY()
					+ frame.getSecondProjectionRow().dot(frame.getOrigin());
			if (!Double.isFinite(firstRightHandSide)
					|| !Double.isFinite(secondRightHandSide)) {
				return undefinedBeforeCandidate(systemCertificate, fidelity,
						correspondence, sourceRevision, instrumentation);
			}
			rightHandSide[2 * index] = firstRightHandSide;
			rightHandSide[2 * index + 1] = secondRightHandSide;
		}

		RealMatrix matrix = new Array2DRowRealMatrix(matrixData, false);
		SingularValueDecomposition decomposition =
				new SingularValueDecomposition(matrix);
		double[] singularValues = decomposition.getSingularValues();
		instrumentation.recordRankEvaluation();
		double maximumSingularValue = singularValues.length == 0
				? 0 : singularValues[0];
		double rankThreshold = policy.rankThreshold(maximumSingularValue,
				rowCount, 3);
		int rank = rank(singularValues, rankThreshold);
		double[] solution = solveWithDeclaredThreshold(decomposition,
				rightHandSide, rankThreshold);
		Vector3 candidate = new Vector3(solution[0], solution[1], solution[2]);
		instrumentation.recordCandidateObjectBuilt();
		if (!candidate.isFinite()) {
			return failure(systemCertificate, SpatialDefinitionStatus.UNDEFINED,
					SpatialCertificateStatus.UNDEFINED, fidelity,
					NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
					rank, singularValues, Double.NaN, Collections.emptyList(),
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
					instrumentation);
		}

		List<ProjectionResidualEvidence> residuals = new ArrayList<>();
		double maximumIntrinsic = 0;
		double maximumDiagram = 0;
		for (int index = 0; index < observations.size(); index++) {
			ProjectionObservation observation = observations.get(index);
			Vector2 expectedIntrinsic = intrinsicPoints.get(index);
			Vector2 projectedIntrinsic = observation.getFrame().project(candidate);
			Vector2 intrinsicResidual = projectedIntrinsic.subtract(expectedIntrinsic);
			double intrinsicNorm = intrinsicResidual.norm();
			double intrinsicScale = Math.max(1, Math.max(expectedIntrinsic.norm(),
					projectedIntrinsic.norm()));
			double normalizedIntrinsic = intrinsicNorm
					/ policy.tolerance(intrinsicScale);
			if (!projectedIntrinsic.isFinite() || !intrinsicResidual.isFinite()
					|| !Double.isFinite(intrinsicNorm)
					|| !Double.isFinite(intrinsicScale)
					|| !Double.isFinite(normalizedIntrinsic)) {
				return failure(systemCertificate, SpatialDefinitionStatus.UNDEFINED,
						SpatialCertificateStatus.UNDEFINED, fidelity,
						NumericalEvidenceStatus.UNRESOLVED, correspondence,
						sourceRevision, rank, singularValues, Double.NaN, residuals,
						Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
						instrumentation);
			}
			Vector2 projectedDiagram = observation.getDiagramMap().forward(
					projectedIntrinsic);
			instrumentation.recordDiagramMapForwardEvaluation();
			Vector2 diagramResidual = projectedDiagram.subtract(
					observation.getDiagramPoint());
			double diagramNorm = diagramResidual.norm();
			// Pull the diagram residual back through the admitted similarity scale.
			// Diagram translation and a coherent common-gauge similarity must not
			// change acceptance merely by moving or rescaling the drawing origin.
			double normalizedDiagram = diagramNorm
					/ observation.getDiagramMap().getDeclaredScale()
					/ policy.tolerance(intrinsicScale);
			if (!projectedDiagram.isFinite() || !diagramResidual.isFinite()
					|| !Double.isFinite(diagramNorm)
					|| !Double.isFinite(normalizedDiagram)) {
				return failure(systemCertificate, SpatialDefinitionStatus.UNDEFINED,
						SpatialCertificateStatus.UNDEFINED, fidelity,
						NumericalEvidenceStatus.UNRESOLVED, correspondence,
						sourceRevision, rank, singularValues, Double.NaN, residuals,
						Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
						instrumentation);
			}
			instrumentation.recordReprojectionEvaluation();
			maximumIntrinsic = Math.max(maximumIntrinsic, normalizedIntrinsic);
			maximumDiagram = Math.max(maximumDiagram, normalizedDiagram);
			residuals.add(new ProjectionResidualEvidence(observation.getEvidenceKey(),
					index, intrinsicResidual, intrinsicNorm, normalizedIntrinsic,
					diagramResidual, diagramNorm, normalizedDiagram));
		}

		double conditionNumber = conditionNumber(singularValues, rank);
		if (maximumIntrinsic > 1 || maximumDiagram > 1) {
			return failure(systemCertificate, SpatialDefinitionStatus.DEFINED,
					SpatialCertificateStatus.INCONSISTENT_PROJECTIONS, fidelity,
					NumericalEvidenceStatus.ESTIMATED_ERROR, correspondence,
					sourceRevision, rank, singularValues, conditionNumber, residuals,
					maximumIntrinsic, maximumDiagram, instrumentation);
		}
		if (rank < 3) {
			return failure(systemCertificate, SpatialDefinitionStatus.DEFINED,
					SpatialCertificateStatus.UNDERDETERMINED, fidelity,
					NumericalEvidenceStatus.ESTIMATED_ERROR, correspondence,
					sourceRevision, rank, singularValues, conditionNumber, residuals,
					maximumIntrinsic, maximumDiagram, instrumentation);
		}
		if (!Double.isFinite(conditionNumber)
				|| conditionNumber > policy.getMaximumConditionNumber()) {
			return failure(systemCertificate, SpatialDefinitionStatus.DEGENERATE,
					SpatialCertificateStatus.DEGENERATE, fidelity,
					NumericalEvidenceStatus.ESTIMATED_ERROR, correspondence,
					sourceRevision, rank, singularValues, conditionNumber, residuals,
					maximumIntrinsic, maximumDiagram, instrumentation);
		}

		instrumentation.recordObjectCertificate(true);
		return new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
				systemCertificate, SpatialDefinitionStatus.DEFINED,
				SpatialCertificateStatus.VALID, SpatialCurrentnessStatus.CURRENT,
				fidelity, NumericalEvidenceStatus.ESTIMATED_ERROR, correspondence,
				sourceRevision, Optional.of(candidate), rank, singularValues,
				conditionNumber, residuals, maximumIntrinsic, maximumDiagram,
				SVD_METHOD);
	}

	private static SpatialPointCertificate undefinedBeforeCandidate(
			ProjectionSystemCertificate systemCertificate,
			RepresentationFidelity fidelity, CorrespondenceStatus correspondence,
			long sourceRevision, SpatialSemanticInstrumentation instrumentation) {
		return failure(systemCertificate, SpatialDefinitionStatus.UNDEFINED,
				SpatialCertificateStatus.UNDEFINED, fidelity,
				NumericalEvidenceStatus.UNRESOLVED, correspondence, sourceRevision,
				0, new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, instrumentation);
	}

	private static SpatialPointCertificate failure(
			ProjectionSystemCertificate systemCertificate,
			SpatialDefinitionStatus definitionStatus,
			SpatialCertificateStatus certificateStatus,
			RepresentationFidelity fidelity,
			NumericalEvidenceStatus numericalEvidence,
			CorrespondenceStatus correspondence, long sourceRevision, int rank,
			double[] singularValues, double conditionNumber,
			List<ProjectionResidualEvidence> residuals, double maximumIntrinsic,
			double maximumDiagram, SpatialSemanticInstrumentation instrumentation) {
		instrumentation.recordObjectCertificate(false);
		return new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
				systemCertificate, definitionStatus, certificateStatus,
				SpatialCurrentnessStatus.FAILED_CURRENT_REVISION, fidelity,
				numericalEvidence, correspondence, sourceRevision, Optional.empty(),
				rank, singularValues, conditionNumber, residuals, maximumIntrinsic,
				maximumDiagram, SVD_METHOD);
	}

	private static void fillRow(double[] row, Vector3 vector) {
		row[0] = vector.getX();
		row[1] = vector.getY();
		row[2] = vector.getZ();
	}

	private static void requireDistinctObservationKeys(
			List<ProjectionObservation> observations) {
		Map<String, String> contentByKey = new LinkedHashMap<>();
		for (ProjectionObservation observation : observations) {
			ProjectionObservation current = Objects.requireNonNull(observation);
			String previous = contentByKey.putIfAbsent(current.getEvidenceKey(),
					current.getEvidenceContentKey());
			if (previous != null && !previous.equals(
					current.getEvidenceContentKey())) {
				throw new IllegalArgumentException(
						"conflicting observation evidence key");
			}
		}
	}

	private static void requireDistinctRelationKeys(
			List<ProjectionRelationDefinition> relations) {
		Map<String, String> contentByKey = new LinkedHashMap<>();
		for (ProjectionRelationDefinition relation : relations) {
			ProjectionRelationDefinition current = Objects.requireNonNull(relation);
			String previous = contentByKey.putIfAbsent(current.getEvidenceKey(),
					current.getEvidenceContentKey());
			if (previous != null && !previous.equals(
					current.getEvidenceContentKey())) {
				throw new IllegalArgumentException("conflicting relation evidence key");
			}
		}
	}

	private static int rank(double[] singularValues, double threshold) {
		int rank = 0;
		for (double singularValue : singularValues) {
			if (singularValue > threshold) {
				rank++;
			}
		}
		return rank;
	}

	private static double[] solveWithDeclaredThreshold(
			SingularValueDecomposition decomposition, double[] rightHandSide,
			double threshold) {
		RealMatrix left = decomposition.getU();
		RealMatrix right = decomposition.getV();
		double[] singularValues = decomposition.getSingularValues();
		double[] solution = new double[3];
		for (int component = 0; component < singularValues.length; component++) {
			double singularValue = singularValues[component];
			if (!(singularValue > threshold)) {
				continue;
			}
			double coefficient = 0;
			for (int row = 0; row < rightHandSide.length; row++) {
				coefficient += left.getEntry(row, component) * rightHandSide[row];
			}
			coefficient /= singularValue;
			for (int column = 0; column < solution.length; column++) {
				solution[column] += right.getEntry(column, component) * coefficient;
			}
		}
		return solution;
	}

	private static double conditionNumber(double[] singularValues, int rank) {
		if (rank < 3 || singularValues.length < 3 || singularValues[2] == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return singularValues[0] / singularValues[2];
	}

	private static SpatialDefinitionStatus aggregateDefinition(
			List<ProjectionObservation> observations) {
		SpatialDefinitionStatus result = SpatialDefinitionStatus.DEFINED;
		for (ProjectionObservation observation : observations) {
			SpatialDefinitionStatus status = Objects.requireNonNull(observation)
					.getDefinitionStatus();
			if (status == SpatialDefinitionStatus.UNDEFINED) {
				return status;
			}
			if (status == SpatialDefinitionStatus.DEGENERATE) {
				result = status;
			}
		}
		return result;
	}

	private static RepresentationFidelity aggregateFidelity(
			List<ProjectionObservation> observations) {
		for (ProjectionObservation observation : observations) {
			if (Objects.requireNonNull(observation).getFidelity()
					== RepresentationFidelity.DISCRETE) {
				return RepresentationFidelity.DISCRETE;
			}
		}
		return RepresentationFidelity.NUMERICAL;
	}

	private static CorrespondenceStatus aggregateCorrespondence(
			List<ProjectionObservation> observations) {
		CorrespondenceStatus result = CorrespondenceStatus.NOT_REQUIRED;
		for (ProjectionObservation observation : observations) {
			CorrespondenceStatus status = Objects.requireNonNull(observation)
					.getCorrespondenceStatus();
			if (status == CorrespondenceStatus.BROKEN) {
				return status;
			}
			if (status == CorrespondenceStatus.AMBIGUOUS) {
				result = status;
			} else if (status == CorrespondenceStatus.ESTABLISHED
					&& result == CorrespondenceStatus.NOT_REQUIRED) {
				result = status;
			}
		}
		return result;
	}

	private static long maximumRevision(List<ProjectionObservation> observations,
			List<ProjectionRelationDefinition> relations) {
		long result = 0;
		for (ProjectionObservation observation : observations) {
			result = Math.max(result, Objects.requireNonNull(observation).getRevision());
		}
		for (ProjectionRelationDefinition relation : relations) {
			result = Math.max(result, Objects.requireNonNull(relation).getRevision());
		}
		return result;
	}

	private static SpatialCertificateStatus systemFailureStatus(
			ProjectionSystemStatus status) {
		if (status == ProjectionSystemStatus.DEGENERATE) {
			return SpatialCertificateStatus.DEGENERATE;
		}
		return SpatialCertificateStatus.UNDEFINED;
	}

	private static SpatialDefinitionStatus systemFailureDefinitionStatus(
			ProjectionSystemStatus status) {
		if (status == ProjectionSystemStatus.DEGENERATE) {
			return SpatialDefinitionStatus.DEGENERATE;
		}
		if (status == ProjectionSystemStatus.UNDEFINED
				|| status == ProjectionSystemStatus.NOT_EVALUATED) {
			return SpatialDefinitionStatus.UNDEFINED;
		}
		return SpatialDefinitionStatus.DEFINED;
	}
}

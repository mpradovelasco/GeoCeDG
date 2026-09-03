/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedAffineLocus2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusParameterPartition2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D.SearchCoverage;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionWorkLimitException;
import org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2D;
import org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2D.IsolationResult;
import org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2D.RootCell;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;

/**
 * Resolves world requests to exact semantic addresses without reading Path,
 * rendering, viewport, screen or movement-trajectory state.
 */
public final class LocusPointInteractionResolver2D {

	/**
	 * Resolves one query against one coherent current source revision.
	 *
	 * @param query semantic interaction query
	 * @return typed inverse-resolution result
	 */
	public LocusPointInteractionResult2D resolve(
			LocusPointInteractionQuery2D query) {
		Objects.requireNonNull(query);
		LocusPointInteractionInstrumentation2D instrumentation =
				new LocusPointInteractionInstrumentation2D(
						query.getPolicy().getWorkBudget());
		GeoLocusV2 source = query.getSource();
		LocusDefinition2D definition = source.getSemanticDefinition();
		if (definition != null && definition.getDefinitionStatus()
				== DefinitionStatus.UNSUPPORTED) {
			return result(LocusPointInteractionStatus2D.UNSUPPORTED_CAPABILITY,
					List.of(), SearchCoverage.NOT_APPLICABLE,
					"The semantic evaluator has no supported deterministic capability",
					instrumentation);
		}
		if (!source.isDefined() || definition == null
				|| source.getPersistentLocusId() == null
				|| definition.getDefinitionStatus() != DefinitionStatus.VALID) {
			return result(LocusPointInteractionStatus2D.INVALID_SOURCE,
					List.of(), SearchCoverage.NOT_APPLICABLE,
					"The Locus V2 has no current valid semantic definition",
					instrumentation);
		}
		if (!currentAddressMatches(query, source, definition)) {
			return result(LocusPointInteractionStatus2D.INVALID_SOURCE,
					List.of(), SearchCoverage.NOT_APPLICABLE,
					"The current edit address does not belong to this source",
					instrumentation);
		}
		if (definition.getBranches().stream().anyMatch(branch -> branch
				.getProperties().contains(BranchProperty.COLLAPSED_IMAGE))) {
			return result(LocusPointInteractionStatus2D.DEGENERATE_SOURCE_IMAGE,
					List.of(), SearchCoverage.NOT_APPLICABLE,
					"A collapsed image has multiple retained semantic addresses",
					instrumentation);
		}
		instrumentation.searchScope(query.getCurrentAddress().isPresent());

		int maximumEvaluations = query.getPolicy().getWorkBudget()
				.getMaximumSemanticEvaluations();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D
				.memoizingWithLimits(maximumEvaluations, maximumEvaluations,
						query.getPolicy().getWorkBudget()
								.getMaximumEvaluatorCompositionDepth())) {
			Search search;
			try {
				search = supportsCertifiedAffine(definition)
						? affineSearch(query, definition,
								(CertifiedAffineLocus2D) definition
										.getEvaluatorCapability(), session,
								instrumentation)
						: supportsPiecewisePolynomial(definition,
								query.getPolicy().getWorkBudget())
							? polynomialSearch(query, definition,
									(PiecewisePolynomialLocus2D) definition
											.getEvaluatorCapability(), session,
									instrumentation)
							: evaluatorSearch(query, definition, session,
									instrumentation);
			} finally {
				instrumentation.sessionCache(session.getHits(), session.getMisses());
			}
			return classify(query, search, instrumentation);
		} catch (LocusPointInteractionInstrumentation2D.WorkLimitException
				| LocusIntersectionWorkLimitException
				| LocusEvaluationSession2D.EvaluationWorkLimitException exception) {
			return result(
					LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
					List.of(), SearchCoverage.NOT_APPLICABLE,
					"R6 work budget exhausted: " + exception.getMessage(),
					instrumentation);
		} catch (IllegalArgumentException exception) {
			return result(
					LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
					List.of(), SearchCoverage.NOT_APPLICABLE,
					exception.getMessage(), instrumentation);
		}
	}

	private static boolean supportsCertifiedAffine(
			LocusDefinition2D definition) {
		if (!(definition.getEvaluatorCapability()
				instanceof CertifiedAffineLocus2D)) {
			return false;
		}
		return ((CertifiedAffineLocus2D) definition.getEvaluatorCapability())
				.supportsCertifiedAffine(definition);
	}

	private static boolean supportsPiecewisePolynomial(
			LocusDefinition2D definition,
			LocusPointInteractionWorkBudget2D workBudget) {
		if (!(definition.getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D)) {
			return false;
		}
		PiecewisePolynomialLocus2D polynomial = (PiecewisePolynomialLocus2D) definition
				.getEvaluatorCapability();
		int compositionDepth = polynomial.getPolynomialCompositionDepth();
		if (compositionDepth < 1 || compositionDepth
				> workBudget.getMaximumEvaluatorCompositionDepth()) {
			throw new LocusPointInteractionInstrumentation2D.WorkLimitException(
					"polynomial evaluator composition depth");
		}
		return polynomial.supportsPiecewisePolynomial(definition);
	}

	private static Search affineSearch(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, CertifiedAffineLocus2D affine,
			LocusEvaluationSession2D session,
			LocusPointInteractionInstrumentation2D instrumentation) {
		SearchAccumulator accumulated = new SearchAccumulator();
		int requestedComponents = 0;
		for (LocusBranch2D branch : definition.getBranches()) {
			if (!matchesCurrentBranch(query, branch)) {
				continue;
			}
			instrumentation.branch();
			double[] x = checkedAffineCoefficients(affine,
					branch.getBranchKey(), 0);
			double[] y = checkedAffineCoefficients(affine,
					branch.getBranchKey(), 1);
			if (x[0] == 0 && y[0] == 0) {
				throw new IllegalArgumentException(
						"Certified affine source has a collapsed derivative");
			}
			for (LocusInterval2D component : branch.getValidDomainComponents()) {
				if (!matchesCurrentComponent(query, branch, component)) {
					continue;
				}
				instrumentation.component();
				requestedComponents++;
				Double parameter = affineProjectionParameter(query, component,
						x, y, definition.getProvider().getDomainEpsilon());
				if (parameter == null) {
					continue;
				}
				accumulated.record(retainCertifiedAffine(query, definition,
						branch, component, parameter, session, instrumentation,
						accumulated.candidates));
			}
		}
		if (requestedComponents == 0) {
			accumulated.barrier("No requested semantic component was inspected");
		}
		return accumulated.search(
				SearchCoverage.ALL_CERTIFIED_AFFINE_COMPONENTS);
	}

	private static double[] checkedAffineCoefficients(
			CertifiedAffineLocus2D affine, String branchKey, int coordinate) {
		double[] coefficients = affine.getCertifiedAffineCoefficients(branchKey,
				coordinate);
		if (coefficients == null || coefficients.length != 2
				|| !Double.isFinite(coefficients[0])
				|| !Double.isFinite(coefficients[1])) {
			throw new IllegalArgumentException(
					"Certified affine capability returned invalid coefficients");
		}
		return coefficients;
	}

	private static Double affineProjectionParameter(
			LocusPointInteractionQuery2D query, LocusInterval2D component,
			double[] x, double[] y, double domainEpsilon) {
		double scale = Math.max(Math.abs(x[0]), Math.abs(y[0]));
		if (!(scale > 0) || !Double.isFinite(scale)) {
			throw new IllegalArgumentException(
					"Certified affine derivative must be finite and nonzero");
		}
		double normalizedX = x[0] / scale;
		double normalizedY = y[0] / scale;
		double deltaX = query.getTargetX() / scale - x[1] / scale;
		double deltaY = query.getTargetY() / scale - y[1] / scale;
		double denominator = normalizedX * normalizedX
				+ normalizedY * normalizedY;
		double parameter = (deltaX * normalizedX + deltaY * normalizedY)
				/ denominator;
		if (!Double.isFinite(parameter)) {
			throw new IllegalArgumentException(
					"Certified affine projection is not finite");
		}
		if (parameter <= component.getLower()) {
			return component.isLowerClosed() ? component.getLower() : null;
		}
		if (parameter >= component.getUpper()) {
			return component.isUpperClosed() ? component.getUpper() : null;
		}
		return component.contains(parameter, domainEpsilon) ? parameter : null;
	}

	private static LocusPointInteractionLocalEvidence2D.Status
			retainCertifiedAffine(LocusPointInteractionQuery2D query,
					LocusDefinition2D definition, LocusBranch2D branch,
					LocusInterval2D component, double parameter,
					LocusEvaluationSession2D session,
					LocusPointInteractionInstrumentation2D instrumentation,
					List<RawCandidate> candidates) {
		double canonical = definition.getProvider().canonicalize(parameter);
		if (!Double.isFinite(canonical) || !component.contains(canonical,
				definition.getProvider().getDomainEpsilon())) {
			return LocusPointInteractionLocalEvidence2D.Status.NOT_ESTABLISHED;
		}
		LocusEvaluation2D evaluation = evaluate(definition, branch, canonical,
				session);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return LocusPointInteractionLocalEvidence2D.Status.NOT_ESTABLISHED;
		}
		LocusPointInteractionLocalEvidence2D localEvidence =
				LocusPointInteractionLocalEvidence2D.established(
						LocusPointInteractionLocalEvidence2D.Method
								.CERTIFIED_AFFINE_PROJECTION,
						NumericGuarantee.ESTIMATED_ERROR,
						"A provider-owned affine certificate establishes the unique "
								+ "minimum on this oriented semantic component");
		double distance = Math.hypot(evaluation.getPoint().getX()
				- query.getTargetX(), evaluation.getPoint().getY()
				- query.getTargetY());
		LocusSemanticAddress2D address = address(query, definition, branch,
				component, canonical);
		instrumentation.candidate();
		candidates.add(new RawCandidate(new LocusPointInteractionCandidate2D(
				address, definition.getSemanticRevision(), evaluation.getPoint(),
				distance, component.getLower(), component.getUpper(),
				Regularity.REGULAR,
				evaluation.getQuality().getNumericGuarantee(),
				"certified-affine-projection/v1", localEvidence)));
		return localEvidence.getStatus();
	}

	private static Search polynomialSearch(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, PiecewisePolynomialLocus2D polynomial,
			LocusEvaluationSession2D session,
			LocusPointInteractionInstrumentation2D instrumentation) {
		SearchAccumulator accumulated = new SearchAccumulator();
		boolean completeCoverage = true;
		int requestedComponents = 0;
		for (LocusBranch2D branch : definition.getBranches()) {
			if (!matchesCurrentBranch(query, branch)) {
				continue;
			}
			instrumentation.branch();
			for (LocusInterval2D component : branch.getValidDomainComponents()) {
				if (!matchesCurrentComponent(query, branch, component)) {
					continue;
				}
				instrumentation.component();
				requestedComponents++;
				List<PolynomialSpan> spans = polynomialSpans(query, polynomial,
						branch, component,
						definition.getProvider().getDomainEpsilon());
				if (!coversComponent(spans, component,
						definition.getProvider().getDomainEpsilon())) {
					completeCoverage = false;
					accumulated.barrier(
							"Explicit polynomial spans do not cover the requested component");
				}
				for (PolynomialSpan span : spans) {
					instrumentation.polynomialSpan();
					double lower = span.lower;
					double upper = span.upper;
					double[][] coordinates = checkedPolynomialCoordinateCoefficients(
							query, polynomial, branch.getBranchKey(), span.index);
					double[] x = coordinates[0];
					double[] y = coordinates[1];
					x[0] -= query.getTargetX();
					y[0] -= query.getTargetY();
					double[] stationarity = add(multiply(x, derivative(x)),
							multiply(y, derivative(y)));
					double isolationTolerance = polynomialIsolationTolerance(lower,
						upper, query.getPolicy().getParameterTolerance());
					IsolationResult isolated = PolynomialRootIsolation2D.isolate(
							stationarity, lower, upper,
							isolationTolerance,
							query.getPolicy().getWorkBudget()
									.getMaximumRefinementIterations(),
							new RootWorkRecorder(instrumentation));
					if (isolated.isZeroPolynomial()) {
						double middle = lower + (upper - lower) / 2;
						double squared = squaredDistance(query, definition, branch,
								middle, session);
						if (Double.isFinite(squared)) {
							accumulated.degenerateDistance = Math.min(
									accumulated.degenerateDistance, Math.sqrt(squared));
						} else {
							accumulated.barrier(
									"A constant-distance polynomial span could not be evaluated");
						}
						continue;
					}
					if (component.contains(lower,
							definition.getProvider().getDomainEpsilon())) {
						accumulated.record(retain(query, definition, branch,
								component, lower, lower, upper, session,
								instrumentation, accumulated.candidates,
								LocusPointInteractionLocalEvidence2D.Method
										.POLYNOMIAL_ENDPOINT,
								"piecewise-polynomial-endpoint/v2"));
					}
					if (!hasSemanticRightOwner(spans, span, upper)
							&& component.contains(upper,
							definition.getProvider().getDomainEpsilon())) {
						accumulated.record(retain(query, definition, branch,
								component, upper, lower, upper, session,
								instrumentation, accumulated.candidates,
								LocusPointInteractionLocalEvidence2D.Method
										.POLYNOMIAL_ENDPOINT,
								"piecewise-polynomial-endpoint/v2"));
					}
					for (RootCell cell : isolated.getCells()) {
						double parameter = snapToSpanBoundary(cell.getParameter(),
								lower, upper,
								query.getPolicy().getParameterTolerance());
						if (sameParameter(parameter, upper,
								query.getPolicy().getParameterTolerance())
								&& hasSemanticRightOwner(spans, span, upper)) {
							continue;
						}
						accumulated.record(retain(query, definition, branch,
								component, parameter, Math.max(lower, cell.getLower()),
								Math.min(upper, cell.getUpper()), session,
								instrumentation, accumulated.candidates,
								LocusPointInteractionLocalEvidence2D.Method
										.POLYNOMIAL_STATIONARY_CELL,
								"piecewise-polynomial-distance/v2"));
					}
				}
			}
		}
		if (requestedComponents == 0) {
			completeCoverage = false;
			accumulated.barrier("No requested semantic component was inspected");
		}
		return accumulated.search(completeCoverage
				? SearchCoverage.ALL_EXPLICIT_POLYNOMIAL_SPANS
				: SearchCoverage.PARTIAL_EXPLICIT_POLYNOMIAL_SPANS);
	}

	private static Search evaluatorSearch(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, LocusEvaluationSession2D session,
			LocusPointInteractionInstrumentation2D instrumentation) {
		SearchAccumulator accumulated = new SearchAccumulator();
		ArrayList<SemanticSegment> segments = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			if (!matchesCurrentBranch(query, branch)) {
				continue;
			}
			instrumentation.branch();
			for (LocusInterval2D component : branch.getValidDomainComponents()) {
				if (!matchesCurrentComponent(query, branch, component)) {
					continue;
				}
				instrumentation.component();
				segments.addAll(semanticSegments(definition, branch, component));
			}
		}
		if (segments.isEmpty()) {
			accumulated.barrier("No requested semantic component was inspected");
			return accumulated.search(SearchCoverage.BOUNDED_EVALUATOR_SEARCH);
		}
		int maximumSubdivisions = query.getPolicy().getWorkBudget()
				.getMaximumSubdivisions();
		int subdivisions = Math.min(96, maximumSubdivisions / segments.size());
		if (subdivisions < 2) {
			throw new LocusPointInteractionInstrumentation2D.WorkLimitException(
					"semantic partition subdivisions");
		}
		for (SemanticSegment segment : segments) {
			searchSegment(query, definition, segment, subdivisions, session,
					instrumentation, accumulated);
		}
		return accumulated.search(SearchCoverage.BOUNDED_EVALUATOR_SEARCH);
	}

	private static void searchSegment(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, SemanticSegment segment,
			int subdivisions, LocusEvaluationSession2D session,
			LocusPointInteractionInstrumentation2D instrumentation,
			SearchAccumulator accumulated) {
		if (segment.lower == segment.upper) {
			accumulated.record(retain(query, definition, segment.branch,
					segment.component, segment.lower, segment.lower, segment.upper,
					session, instrumentation, accumulated.candidates,
					LocusPointInteractionLocalEvidence2D.Method
							.ISOLATED_SEMANTIC_COMPONENT,
					"bounded-semantic-evaluator-isolated/v2"));
			return;
		}
		double sampleLower = sampleEndpoint(segment.lower,
				segment.component.isLowerClosed() || !segment.first,
				true, definition.getProvider().getDomainEpsilon());
		double sampleUpper = sampleEndpoint(segment.upper,
				segment.component.isUpperClosed() || !segment.last,
				false, definition.getProvider().getDomainEpsilon());
		if (!(sampleLower < sampleUpper)) {
			accumulated.barrier(
					"An open semantic partition cell has no evaluable interior");
			return;
		}
		Sample[] samples = new Sample[subdivisions + 1];
		for (int index = 0; index <= subdivisions; index++) {
			if (index < subdivisions) {
				instrumentation.subdivision();
			}
			double parameter = sampleLower + (sampleUpper - sampleLower)
					* index / subdivisions;
			samples[index] = sample(query, definition, segment.branch,
					parameter, session);
			if (!samples[index].valid) {
				accumulated.barrier(samples[index].diagnostic);
			} else if (samples[index].regularity == Regularity.SINGULAR) {
				accumulated.barrier(
						"A sampled semantic cell contains singular evaluator evidence");
			}
		}
		for (int index = 0; index <= subdivisions; index++) {
			Sample center = samples[index];
			if (!center.valid || center.regularity == Regularity.SINGULAR) {
				continue;
			}
			boolean ownedUpper = index == subdivisions
					&& (!segment.last || isPeriodicCycle(definition,
							segment.component)
							|| !segment.component.isUpperClosed());
			if (ownedUpper) {
				continue;
			}
			double left = index == 0 ? Double.NaN
					: samples[index - 1].squaredDistance;
			double right = index == subdivisions ? Double.NaN
					: samples[index + 1].squaredDistance;
			if (Double.isFinite(left) && center.squaredDistance > left
					|| Double.isFinite(right) && center.squaredDistance > right) {
				continue;
			}
			double cellLower = index == 0 ? center.parameter
					: samples[index - 1].parameter;
			double cellUpper = index == subdivisions ? center.parameter
					: samples[index + 1].parameter;
			double parameter = center.parameter;
			if (index > 0 && index < subdivisions) {
				Refinement refinement = goldenMinimum(query, definition,
						segment.branch, cellLower, cellUpper, session,
						instrumentation);
				if (!refinement.valid || !refinement.converged) {
					accumulated.barrier(refinement.diagnostic);
					continue;
				}
				parameter = refinement.parameter;
			}
			accumulated.record(retain(query, definition, segment.branch,
					segment.component, parameter, cellLower, cellUpper, session,
					instrumentation, accumulated.candidates,
					LocusPointInteractionLocalEvidence2D.Method
							.SEMANTIC_EVALUATOR_BRACKET,
					"bounded-semantic-evaluator/v2"));
		}
	}

	private static Refinement goldenMinimum(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, LocusBranch2D branch, double lower,
			double upper, LocusEvaluationSession2D session,
			LocusPointInteractionInstrumentation2D instrumentation) {
		double ratio = (Math.sqrt(5) - 1) / 2;
		double left = upper - ratio * (upper - lower);
		double right = lower + ratio * (upper - lower);
		double leftValue = squaredDistance(query, definition, branch, left,
				session);
		double rightValue = squaredDistance(query, definition, branch, right,
				session);
		if (!Double.isFinite(leftValue) || !Double.isFinite(rightValue)) {
			return Refinement.invalid(
					"Semantic evaluation failed inside a refinement cell");
		}
		for (int iteration = 0; iteration < query.getPolicy().getWorkBudget()
				.getMaximumRefinementIterations(); iteration++) {
			instrumentation.refinementIteration();
			if (upper - lower <= query.getPolicy().getParameterTolerance()) {
				break;
			}
			if (leftValue <= rightValue) {
				upper = right;
				right = left;
				rightValue = leftValue;
				left = upper - ratio * (upper - lower);
				leftValue = squaredDistance(query, definition, branch, left,
						session);
			} else {
				lower = left;
				left = right;
				leftValue = rightValue;
				right = lower + ratio * (upper - lower);
				rightValue = squaredDistance(query, definition, branch, right,
						session);
			}
			if (!Double.isFinite(leftValue) || !Double.isFinite(rightValue)) {
				return Refinement.invalid(
						"Semantic evaluation failed inside a refinement cell");
			}
		}
		boolean converged = upper - lower
				<= query.getPolicy().getParameterTolerance();
		return new Refinement(lower + (upper - lower) / 2, converged,
				converged, converged ? "" : "Bounded refinement did not converge");
	}

	private static LocusPointInteractionLocalEvidence2D.Status retain(
			LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, double parameter, double cellLower,
			double cellUpper, LocusEvaluationSession2D session,
			LocusPointInteractionInstrumentation2D instrumentation,
			List<RawCandidate> candidates,
			LocusPointInteractionLocalEvidence2D.Method evidenceMethod,
			String method) {
		double canonical = definition.getProvider().canonicalize(parameter);
		if (!Double.isFinite(canonical) || !component.contains(canonical,
				definition.getProvider().getDomainEpsilon())) {
			return LocusPointInteractionLocalEvidence2D.Status.NOT_ESTABLISHED;
		}
		LocusEvaluation2D evaluation = evaluate(definition, branch, canonical,
				session);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return LocusPointInteractionLocalEvidence2D.Status.NOT_ESTABLISHED;
		}
		LocusPointInteractionLocalEvidence2D localEvidence = localEvidence(query,
				definition, branch, component, canonical, cellLower, cellUpper,
				evaluation, evidenceMethod, session);
		if (!localEvidence.isEstablished()) {
			return localEvidence.getStatus();
		}
		double distance = Math.hypot(evaluation.getPoint().getX()
				- query.getTargetX(), evaluation.getPoint().getY()
				- query.getTargetY());
		LocusSemanticAddress2D address = address(query, definition, branch,
				component, canonical);
		instrumentation.candidate();
		candidates.add(new RawCandidate(new LocusPointInteractionCandidate2D(
				address, definition.getSemanticRevision(), evaluation.getPoint(),
				distance, cellLower, cellUpper, evaluation.getRegularity(),
				evaluation.getQuality().getNumericGuarantee(), method,
				localEvidence)));
		return localEvidence.getStatus();
	}

	private static LocusPointInteractionLocalEvidence2D localEvidence(
			LocusPointInteractionQuery2D query, LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component, double canonical,
			double cellLower, double cellUpper, LocusEvaluation2D center,
			LocusPointInteractionLocalEvidence2D.Method method,
			LocusEvaluationSession2D session) {
		if (component.getLower() == component.getUpper()) {
			if (center.getRegularity() == Regularity.SINGULAR) {
				return LocusPointInteractionLocalEvidence2D.singular(
						"The semantic evaluator reports a singular address");
			}
			return LocusPointInteractionLocalEvidence2D.established(method,
					NumericGuarantee.ESTIMATED_ERROR,
					"The component contains one explicit semantic address");
		}
		Neighborhood neighborhood = neighborhood(query, definition, component,
				canonical, cellLower, cellUpper);
		Sample left = neighborhood.hasLeft
				? sample(query, definition, branch, neighborhood.left, session) : null;
		Sample right = neighborhood.hasRight
				? sample(query, definition, branch, neighborhood.right, session) : null;
		if (left != null && !left.valid || right != null && !right.valid) {
			return LocusPointInteractionLocalEvidence2D.unresolved(
					"A local semantic-minimum guard could not be evaluated");
		}
		double centerDistance = squaredDistance(query, center.getPoint());
		double scale = Math.max(1, centerDistance);
		if (left != null) {
			scale = Math.max(scale, left.squaredDistance);
		}
		if (right != null) {
			scale = Math.max(scale, right.squaredDistance);
		}
		double comparisonTolerance = 256 * Math.ulp(scale);
		if (left != null
				&& centerDistance > left.squaredDistance + comparisonTolerance
				|| right != null && centerDistance > right.squaredDistance
						+ comparisonTolerance) {
			return LocusPointInteractionLocalEvidence2D.notMinimum(
					"The refined address is not a local distance minimum");
		}
		// A singular address that is already excluded by the same strict
		// nonminimum test cannot obstruct unrelated regular preimages. No
		// derivative or nonzero-speed assumption is used for that exclusion.
		if (center.getRegularity() == Regularity.SINGULAR) {
			return LocusPointInteractionLocalEvidence2D.singular(
					"The semantic evaluator reports a singular address");
		}
		if (left != null && left.regularity == Regularity.SINGULAR
				|| right != null && right.regularity == Regularity.SINGULAR) {
			return LocusPointInteractionLocalEvidence2D.singular(
					"A local semantic-minimum guard is singular");
		}
		boolean strict = left != null
				&& left.squaredDistance > centerDistance + comparisonTolerance
				|| right != null && right.squaredDistance > centerDistance
						+ comparisonTolerance;
		if (!strict) {
			return LocusPointInteractionLocalEvidence2D.unresolved(
					"The bounded cell does not isolate a strict semantic minimum");
		}
		LocusPointInteractionLocalEvidence2D regularity = regularityEvidence(
				definition, branch, canonical, center, neighborhood, session);
		if (!regularity.isEstablished()) {
			return regularity;
		}
		return LocusPointInteractionLocalEvidence2D.established(method,
				NumericGuarantee.ESTIMATED_ERROR,
				"A bounded semantic cell establishes a strict regular minimum");
	}

	private static LocusPointInteractionLocalEvidence2D regularityEvidence(
			LocusDefinition2D definition, LocusBranch2D branch, double canonical,
			LocusEvaluation2D center, Neighborhood neighborhood,
			LocusEvaluationSession2D session) {
		if (center.getRegularity() == Regularity.REGULAR) {
			return LocusPointInteractionLocalEvidence2D.established(
					LocusPointInteractionLocalEvidence2D.Method
							.SEMANTIC_EVALUATOR_BRACKET,
					NumericGuarantee.ESTIMATED_ERROR,
					"The semantic evaluator explicitly reports regularity");
		}
		if (definition.getEvaluatorCapability()
				instanceof LocusDifferentialEvaluator2D) {
			LocusDifferentialEvaluator2D differential =
					(LocusDifferentialEvaluator2D) definition.getEvaluatorCapability();
			if (differential.supportsDifferential(definition)) {
				LocusDifferentialEvaluation2D evaluation = differential
						.evaluateDifferential(definition, branch.getBranchKey(),
								canonical, session);
				if (evaluation == null || !evaluation.isValid()) {
					return LocusPointInteractionLocalEvidence2D.unresolved(
							"Explicit differential evidence is unavailable");
				}
				if (!(evaluation.getSpeed() > differentialFloor(center.getPoint(),
						neighborhood.step))) {
					return LocusPointInteractionLocalEvidence2D.singular(
							"Explicit differential evidence has zero semantic speed");
				}
				return LocusPointInteractionLocalEvidence2D.established(
						LocusPointInteractionLocalEvidence2D.Method
								.SEMANTIC_EVALUATOR_BRACKET,
						NumericGuarantee.ESTIMATED_ERROR,
						"Explicit semantic differential evidence is regular");
			}
		}
		return finiteDifferenceRegularity(definition, branch, canonical, center,
				neighborhood, session);
	}

	private static LocusPointInteractionLocalEvidence2D finiteDifferenceRegularity(
			LocusDefinition2D definition, LocusBranch2D branch, double canonical,
			LocusEvaluation2D center, Neighborhood neighborhood,
			LocusEvaluationSession2D session) {
		DerivativeEstimate coarse = derivativeEstimate(definition, branch,
				canonical, center.getPoint(), neighborhood, neighborhood.step,
				session);
		DerivativeEstimate fine = derivativeEstimate(definition, branch, canonical,
				center.getPoint(), neighborhood, neighborhood.step / 2, session);
		if (!coarse.valid || !fine.valid) {
			return LocusPointInteractionLocalEvidence2D.unresolved(
					"Finite-difference semantic regularity could not be established");
		}
		double maximumSpeed = Math.max(coarse.speed(), fine.speed());
		if (!(maximumSpeed > differentialFloor(center.getPoint(),
				neighborhood.step))) {
			return LocusPointInteractionLocalEvidence2D.singular(
					"Finite-difference semantic speed is indistinguishable from zero");
		}
		double difference = Math.hypot(coarse.x - fine.x,
				coarse.y - fine.y);
		if (difference > 0.25 * maximumSpeed) {
			return LocusPointInteractionLocalEvidence2D.singular(
					"Multi-scale semantic differential evidence is unstable");
		}
		return LocusPointInteractionLocalEvidence2D.established(
				LocusPointInteractionLocalEvidence2D.Method
						.SEMANTIC_EVALUATOR_BRACKET,
				NumericGuarantee.ESTIMATED_ERROR,
				"Stable multi-scale semantic differential evidence is regular");
	}

	private static DerivativeEstimate derivativeEstimate(
			LocusDefinition2D definition, LocusBranch2D branch, double canonical,
			LocusPoint2D center, Neighborhood neighborhood, double step,
			LocusEvaluationSession2D session) {
		if (!(step > 0) || !Double.isFinite(step)) {
			return DerivativeEstimate.invalid();
		}
		LocusPoint2D left = null;
		LocusPoint2D right = null;
		if (neighborhood.hasLeft) {
			LocusEvaluation2D evaluation = evaluate(definition, branch,
					definition.getProvider().canonicalize(canonical - step), session);
			if (!evaluation.isValid() || evaluation.getPoint() == null) {
				return DerivativeEstimate.invalid();
			}
			left = evaluation.getPoint();
		}
		if (neighborhood.hasRight) {
			LocusEvaluation2D evaluation = evaluate(definition, branch,
					definition.getProvider().canonicalize(canonical + step), session);
			if (!evaluation.isValid() || evaluation.getPoint() == null) {
				return DerivativeEstimate.invalid();
			}
			right = evaluation.getPoint();
		}
		if (left != null && right != null) {
			return DerivativeEstimate.valid((right.getX() - left.getX())
					/ (2 * step), (right.getY() - left.getY()) / (2 * step));
		}
		if (right != null) {
			return DerivativeEstimate.valid((right.getX() - center.getX()) / step,
					(right.getY() - center.getY()) / step);
		}
		if (left != null) {
			return DerivativeEstimate.valid((center.getX() - left.getX()) / step,
					(center.getY() - left.getY()) / step);
		}
		return DerivativeEstimate.invalid();
	}

	private static double differentialFloor(LocusPoint2D point, double step) {
		double coordinateScale = Math.max(1,
				Math.max(Math.abs(point.getX()), Math.abs(point.getY())));
		return 512 * Math.ulp(coordinateScale) / Math.max(step, Math.ulp(1.0));
	}

	private static Neighborhood neighborhood(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, LocusInterval2D component,
			double canonical, double cellLower, double cellUpper) {
		double span = component.getUpper() - component.getLower();
		double scale = Math.max(1, Math.max(Math.abs(component.getLower()),
				Math.abs(component.getUpper())));
		double minimumStep = Math.max(
				16 * query.getPolicy().getParameterTolerance(), 256 * Math.ulp(scale));
		double proposed = span / 128;
		double leftCell = canonical - Math.min(canonical, cellLower);
		double rightCell = Math.max(canonical, cellUpper) - canonical;
		if (leftCell > minimumStep) {
			proposed = Math.min(proposed, leftCell / 2);
		}
		if (rightCell > minimumStep) {
			proposed = Math.min(proposed, rightCell / 2);
		}
		boolean periodic = isPeriodicCycle(definition, component);
		double epsilon = definition.getProvider().getDomainEpsilon();
		boolean hasLeft = periodic
				|| canonical > component.getLower() + epsilon;
		boolean hasRight = periodic
				|| canonical < component.getUpper() - epsilon;
		if (hasLeft && !periodic) {
			proposed = Math.min(proposed,
					(canonical - component.getLower()) / 2);
		}
		if (hasRight && !periodic) {
			proposed = Math.min(proposed,
					(component.getUpper() - canonical) / 2);
		}
		double step = Math.max(minimumStep, proposed);
		if (hasLeft && !periodic) {
			step = Math.min(step, (canonical - component.getLower()) / 2);
		}
		if (hasRight && !periodic) {
			step = Math.min(step, (component.getUpper() - canonical) / 2);
		}
		return new Neighborhood(hasLeft, hasRight,
				definition.getProvider().canonicalize(canonical - step),
				definition.getProvider().canonicalize(canonical + step), step);
	}

	private static Sample sample(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, LocusBranch2D branch, double parameter,
			LocusEvaluationSession2D session) {
		double canonical = definition.getProvider().canonicalize(parameter);
		LocusEvaluation2D evaluation = evaluate(definition, branch, canonical,
				session);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			String diagnostic = evaluation.getDiagnostic();
			if (diagnostic.contains("EvaluationWorkLimitException")
					|| diagnostic.contains("work limit exhausted")) {
				diagnostic = "R6 work budget exhausted: nested semantic evaluations";
			}
			return Sample.invalid(canonical,
					diagnostic.isEmpty()
							? "A semantic partition cell could not be evaluated"
							: diagnostic);
		}
		return Sample.valid(canonical, squaredDistance(query,
				evaluation.getPoint()), evaluation.getRegularity());
	}

	private static double squaredDistance(LocusPointInteractionQuery2D query,
			LocusPoint2D point) {
		double dx = point.getX() - query.getTargetX();
		double dy = point.getY() - query.getTargetY();
		return dx * dx + dy * dy;
	}

	private static LocusPointInteractionResult2D classify(
			LocusPointInteractionQuery2D query, Search search,
			LocusPointInteractionInstrumentation2D instrumentation) {
		if (search.degenerateDistance
				<= query.getPolicy().getMaximumWorldDistance()) {
			return result(LocusPointInteractionStatus2D.DEGENERATE_SOURCE_IMAGE,
					List.of(), search.coverage,
					"Squared distance is constant over a semantic interval",
					instrumentation);
		}
		ArrayList<LocusPointInteractionCandidate2D> admissible = new ArrayList<>();
		for (LocusPointInteractionCandidate2D candidate : search.candidates) {
			if (candidate.getWorldDistance()
					<= query.getPolicy().getMaximumWorldDistance()) {
				admissible.add(candidate);
			}
		}
		if (hasUnseparatedCandidates(admissible,
				query.getPolicy().getParameterTolerance())) {
			return result(
					LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
					admissible, search.coverage,
					"Admissible semantic preimages are not separated at the requested "
							+ "parameter tolerance",
					instrumentation);
		}
		if (search.unsafeEvidence) {
			return result(
					LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
					admissible, search.coverage, search.diagnostic, instrumentation);
		}
		if (admissible.isEmpty()) {
			boolean complete = search.coverage
					.establishesCompleteRequestedScope();
			return result(complete
					? LocusPointInteractionStatus2D.NO_ADMISSIBLE_PREIMAGE
					: LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
					List.of(), search.coverage,
					complete
							? "Every established preimage is outside the world threshold"
							: "Bounded semantic search established no in-radius preimage; "
									+ "global enumeration is not established",
					instrumentation);
		}
		if (admissible.size() == 1
				&& !search.coverage.establishesCompleteRequestedScope()) {
			return result(
					LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
					admissible, search.coverage,
					"One local candidate was established, but bounded semantic "
							+ "search did not establish global uniqueness",
					instrumentation);
		}
		return result(admissible.size() == 1
				? LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE
				: LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				admissible, search.coverage,
				admissible.size() == 1
						? "One bounded, locally isolated semantic address is admissible; "
								+ "global enumeration is not implied"
						: "Several distinct semantic addresses are admissible within "
								+ "the world-radius policy",
				instrumentation);
	}

	private static boolean hasUnseparatedCandidates(
			List<LocusPointInteractionCandidate2D> candidates,
			double parameterTolerance) {
		for (int firstIndex = 0; firstIndex < candidates.size(); firstIndex++) {
			LocusSemanticAddress2D first = candidates.get(firstIndex).getAddress();
			for (int secondIndex = firstIndex + 1;
					secondIndex < candidates.size(); secondIndex++) {
				LocusSemanticAddress2D second = candidates.get(secondIndex)
						.getAddress();
				if (first.getBranchKey().equals(second.getBranchKey())
						&& first.getComponentLineageKey().equals(
								second.getComponentLineageKey())
						&& first.getPeriodicLift() == second.getPeriodicLift()
						&& !first.equals(second)
						&& Math.abs(first.getCanonicalParameter()
								- second.getCanonicalParameter())
								<= parameterTolerance) {
					return true;
				}
			}
		}
		return false;
	}

	private static List<LocusPointInteractionCandidate2D> canonicalCandidates(
			List<RawCandidate> input) {
		input.sort(Comparator.comparing((RawCandidate candidate) -> candidate
				.value.getAddress().getBranchKey()).thenComparing(candidate -> candidate
				.value.getAddress().getComponentLineageKey()).thenComparingLong(
						candidate -> candidate.value.getAddress().getPeriodicLift())
				.thenComparingDouble(candidate -> candidate.value.getAddress()
						.getCanonicalParameter()).thenComparing(candidate -> candidate
						.value.getAddress().getSeamSide()));
		ArrayList<LocusPointInteractionCandidate2D> output = new ArrayList<>();
		for (RawCandidate raw : input) {
			LocusPointInteractionCandidate2D candidate = raw.value;
			if (!output.isEmpty()) {
				LocusPointInteractionCandidate2D previous =
						output.get(output.size() - 1);
				if (previous.getAddress().equals(candidate.getAddress())) {
					if (strongerEvidence(candidate, previous)) {
						output.set(output.size() - 1, candidate);
					}
					continue;
				}
			}
			output.add(candidate);
		}
		return List.copyOf(output);
	}

	private static boolean strongerEvidence(
			LocusPointInteractionCandidate2D candidate,
			LocusPointInteractionCandidate2D previous) {
		double candidateWidth = candidate.getIntervalUpper()
				- candidate.getIntervalLower();
		double previousWidth = previous.getIntervalUpper()
				- previous.getIntervalLower();
		int width = Double.compare(candidateWidth, previousWidth);
		return width < 0 || width == 0
				&& candidate.getMethod().compareTo(previous.getMethod()) < 0;
	}

	private static double squaredDistance(LocusPointInteractionQuery2D query,
			LocusDefinition2D definition, LocusBranch2D branch, double parameter,
			LocusEvaluationSession2D session) {
		LocusEvaluation2D evaluation = evaluate(definition, branch,
				definition.getProvider().canonicalize(parameter), session);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return Double.NaN;
		}
		double dx = evaluation.getPoint().getX() - query.getTargetX();
		double dy = evaluation.getPoint().getY() - query.getTargetY();
		return dx * dx + dy * dy;
	}

	private static LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double parameter,
			LocusEvaluationSession2D session) {
		return definition.evaluate(branch.getBranchKey(), parameter, session);
	}

	private static LocusSemanticAddress2D address(
			LocusPointInteractionQuery2D query, LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component, double canonical) {
		PersistentGeoId sourceId = query.getSource().getPersistentLocusId();
		LocusDriverDomainProvider2D provider = definition.getProvider();
		long lift = 0;
		if (provider.isPeriodic() && query.getCurrentAddress().isPresent()) {
			LocusSemanticAddress2D current = query.getCurrentAddress().get();
			LocusInterval2D domain = provider.getDeclaredDomain();
			double period = domain.getUpper() - domain.getLower();
			double currentRaw = current.getCanonicalParameter()
					+ current.getPeriodicLift() * period;
			double quotient = (currentRaw - canonical) / period;
			double nearest = Math.rint(quotient);
			if (Math.abs(Math.abs(quotient - nearest) - 0.5) <= 16 * Math.ulp(1.0)) {
				throw new IllegalArgumentException(
						"Periodic lift is semantically ambiguous");
			}
			lift = (long) nearest;
		}
		SeamSide seam = !provider.isPeriodic() ? SeamSide.NOT_PERIODIC
				: canonical == provider.getDeclaredDomain().getLower()
						? lift == 0 ? SeamSide.LOWER_APPROACH
								: SeamSide.UPPER_APPROACH
						: SeamSide.INTERIOR;
		return new LocusSemanticAddress2D(sourceId, provider.getProviderId(),
				branch.getBranchKey(), LocusComponentLineage2D.create(
						branch.getBranchKey(), component), canonical, lift, seam);
	}

	private static boolean currentAddressMatches(
			LocusPointInteractionQuery2D query, GeoLocusV2 source,
			LocusDefinition2D definition) {
		if (query.getCurrentAddress().isEmpty()) {
			return true;
		}
		LocusSemanticAddress2D current = query.getCurrentAddress().get();
		LocusDriverDomainProvider2D provider = definition.getProvider();
		if (!current.getSourceLocusId().equals(source.getPersistentLocusId())
				|| !current.getProviderVersion().equals(provider.getProviderId())) {
			return false;
		}
		LocusBranch2D branch = definition.getBranch(current.getBranchKey());
		if (branch == null) {
			return false;
		}
		LocusInterval2D component = null;
		for (LocusInterval2D candidate : branch.getValidDomainComponents()) {
			if (current.getComponentLineageKey().equals(
					LocusComponentLineage2D.create(branch.getBranchKey(), candidate))) {
				component = candidate;
				break;
			}
		}
		if (component == null || !provider.contains(current.getCanonicalParameter())
				|| !component.contains(current.getCanonicalParameter(),
						provider.getDomainEpsilon())
				|| Double.doubleToLongBits(current.getCanonicalParameter())
						!= Double.doubleToLongBits(provider.canonicalize(
								current.getCanonicalParameter()))) {
			return false;
		}
		if (!provider.isPeriodic()) {
			return current.getPeriodicLift() == 0
					&& current.getSeamSide() == SeamSide.NOT_PERIODIC;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		double period = domain.getUpper() - domain.getLower();
		double lifted = current.getCanonicalParameter()
				+ current.getPeriodicLift() * period;
		if (!(period > 0) || !Double.isFinite(period) || !Double.isFinite(lifted)) {
			return false;
		}
		boolean atSeam = Double.doubleToLongBits(current.getCanonicalParameter())
				== Double.doubleToLongBits(domain.getLower());
		SeamSide expected = atSeam
				? current.getPeriodicLift() == 0 ? SeamSide.LOWER_APPROACH
						: SeamSide.UPPER_APPROACH
				: SeamSide.INTERIOR;
		return current.getSeamSide() == expected;
	}

	private static boolean matchesCurrentBranch(
			LocusPointInteractionQuery2D query, LocusBranch2D branch) {
		return query.getCurrentAddress().isEmpty()
				|| query.getCurrentAddress().get().getBranchKey()
						.equals(branch.getBranchKey());
	}

	private static boolean matchesCurrentComponent(
			LocusPointInteractionQuery2D query, LocusBranch2D branch,
			LocusInterval2D component) {
		return query.getCurrentAddress().isEmpty()
				|| query.getCurrentAddress().get().getComponentLineageKey().equals(
						LocusComponentLineage2D.create(branch.getBranchKey(), component));
	}

	private static List<SemanticSegment> semanticSegments(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component) {
		if (component.getLower() == component.getUpper()) {
			return List.of(new SemanticSegment(branch, component,
					component.getLower(), component.getUpper(), true, true));
		}
		ArrayList<Double> boundaries = new ArrayList<>();
		boundaries.add(component.getLower());
		if (definition.getEvaluatorCapability()
				instanceof LocusParameterPartition2D) {
			List<Double> breakpoints = ((LocusParameterPartition2D) definition
					.getEvaluatorCapability()).getInteriorBreakpoints(
							branch.getBranchKey(), component.getLower(),
							component.getUpper());
			if (breakpoints == null) {
				throw new IllegalArgumentException(
						"Semantic parameter partition returned null");
			}
			double previous = component.getLower();
			for (Double boxed : breakpoints) {
				if (boxed == null || !Double.isFinite(boxed)) {
					throw new IllegalArgumentException(
							"Semantic partition breakpoints must be finite");
				}
				double value = boxed;
				if (!(value > previous && value < component.getUpper())
						|| Double.doubleToLongBits(value)
								!= Double.doubleToLongBits(definition.getProvider()
										.canonicalize(value))) {
					throw new IllegalArgumentException(
							"Semantic partition must be strictly ordered and canonical");
				}
				boundaries.add(value);
				previous = value;
			}
		}
		boundaries.add(component.getUpper());
		ArrayList<SemanticSegment> segments = new ArrayList<>();
		for (int index = 0; index + 1 < boundaries.size(); index++) {
			segments.add(new SemanticSegment(branch, component,
					boundaries.get(index), boundaries.get(index + 1), index == 0,
					index + 2 == boundaries.size()));
		}
		return segments;
	}

	private static double sampleEndpoint(double boundary, boolean included,
			boolean lower, double domainEpsilon) {
		if (included) {
			return boundary;
		}
		double offset = Math.max(2 * domainEpsilon,
				256 * Math.ulp(Math.max(1, Math.abs(boundary))));
		return lower ? boundary + offset : boundary - offset;
	}

	private static boolean isPeriodicCycle(LocusDefinition2D definition,
			LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& component.equals(definition.getProvider().getDeclaredDomain());
	}

	private static List<PolynomialSpan> polynomialSpans(
			LocusPointInteractionQuery2D query,
			PiecewisePolynomialLocus2D polynomial, LocusBranch2D branch,
			LocusInterval2D component, double epsilon) {
		int count = polynomial.getPolynomialSpanCount(branch.getBranchKey());
		if (count < 0 || count > query.getPolicy().getWorkBudget()
				.getMaximumSubdivisions()) {
			throw new LocusPointInteractionInstrumentation2D.WorkLimitException(
					"polynomial span inventory");
		}
		ArrayList<PolynomialSpan> spans = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			double lower = polynomial.getPolynomialSpanLower(branch.getBranchKey(),
					index);
			double upper = polynomial.getPolynomialSpanUpper(branch.getBranchKey(),
					index);
			if (!Double.isFinite(lower) || !Double.isFinite(upper)
					|| !(lower < upper)) {
				throw new IllegalArgumentException(
						"Polynomial spans must be finite and nonempty");
			}
			double middle = lower + (upper - lower) / 2;
			if (component.contains(middle, epsilon)) {
				spans.add(new PolynomialSpan(index, lower, upper));
			}
		}
		return List.copyOf(spans);
	}

	private static boolean coversComponent(List<PolynomialSpan> input,
			LocusInterval2D component, double epsilon) {
		if (input.isEmpty()) {
			return false;
		}
		ArrayList<PolynomialSpan> spans = new ArrayList<>(input);
		spans.sort(Comparator.comparingDouble(span -> span.lower));
		double cursor = component.getLower();
		for (PolynomialSpan span : spans) {
			if (span.lower < component.getLower() - epsilon
					|| span.upper > component.getUpper() + epsilon
					|| span.lower > cursor + epsilon) {
				return false;
			}
			cursor = Math.max(cursor, span.upper);
		}
		return cursor >= component.getUpper() - epsilon;
	}

	private static double[][] checkedPolynomialCoordinateCoefficients(
			LocusPointInteractionQuery2D query,
			PiecewisePolynomialLocus2D polynomial, String branchKey, int span) {
		double[][] coordinates = polynomial.getPolynomialCoordinateCoefficients(
				branchKey, span);
		int coefficientLimit = Math.min(32, Math.max(2,
				query.getPolicy().getWorkBudget().getMaximumSubdivisions()));
		if (coordinates == null || coordinates.length != 2) {
			throw new IllegalArgumentException(
					"Polynomial capability must return one x/y coordinate pair");
		}
		double[][] ascending = new double[2][];
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			double[] descending = coordinates[coordinate];
			if (descending == null || descending.length == 0
					|| descending.length > coefficientLimit) {
				throw new LocusPointInteractionInstrumentation2D.WorkLimitException(
						"polynomial coefficient degree");
			}
			for (double coefficient : descending) {
				if (!Double.isFinite(coefficient)) {
					throw new IllegalArgumentException(
							"Polynomial coefficients must be finite");
				}
			}
			ascending[coordinate] = descendingToAscending(descending);
		}
		return ascending;
	}

	private static boolean hasSemanticRightOwner(List<PolynomialSpan> spans,
			PolynomialSpan current, double boundary) {
		for (PolynomialSpan span : spans) {
			if (span != current && Double.doubleToLongBits(span.lower)
					== Double.doubleToLongBits(boundary)) {
				return true;
			}
		}
		return false;
	}

	private static double snapToSpanBoundary(double parameter, double lower,
			double upper, double tolerance) {
		if (sameParameter(parameter, lower, tolerance)) {
			return lower;
		}
		return sameParameter(parameter, upper, tolerance) ? upper : parameter;
	}

	private static boolean sameParameter(double first, double second,
			double tolerance) {
		return Double.doubleToLongBits(first) == Double.doubleToLongBits(second)
				|| Math.abs(first - second) <= tolerance;
	}

	private static double polynomialIsolationTolerance(double lower,
			double upper, double requestedTolerance) {
		double scale = Math.max(1,
				Math.max(Math.abs(lower), Math.abs(upper)));
		return Math.max(128 * Math.ulp(scale), requestedTolerance / 64);
	}

	private static double[] descendingToAscending(double[] descending) {
		double[] ascending = new double[descending.length];
		for (int index = 0; index < descending.length; index++) {
			ascending[index] = descending[descending.length - 1 - index];
		}
		return ascending;
	}

	private static double[] derivative(double[] polynomial) {
		if (polynomial.length == 1) {
			return new double[] {0};
		}
		double[] derivative = new double[polynomial.length - 1];
		for (int power = 1; power < polynomial.length; power++) {
			derivative[power - 1] = power * polynomial[power];
		}
		return derivative;
	}

	private static double[] multiply(double[] first, double[] second) {
		double[] product = new double[first.length + second.length - 1];
		for (int firstIndex = 0; firstIndex < first.length; firstIndex++) {
			for (int secondIndex = 0; secondIndex < second.length; secondIndex++) {
				product[firstIndex + secondIndex] += first[firstIndex]
						* second[secondIndex];
			}
		}
		return product;
	}

	private static double[] add(double[] first, double[] second) {
		double[] sum = new double[Math.max(first.length, second.length)];
		for (int index = 0; index < first.length; index++) {
			sum[index] += first[index];
		}
		for (int index = 0; index < second.length; index++) {
			sum[index] += second[index];
		}
		return sum;
	}

	private static LocusPointInteractionResult2D result(
			LocusPointInteractionStatus2D status,
			List<LocusPointInteractionCandidate2D> candidates,
			SearchCoverage coverage, String diagnostic,
			LocusPointInteractionInstrumentation2D instrumentation) {
		return new LocusPointInteractionResult2D(status, candidates, coverage,
				diagnostic, instrumentation.snapshot());
	}

	private static final class RawCandidate {
		private final LocusPointInteractionCandidate2D value;

		private RawCandidate(LocusPointInteractionCandidate2D value) {
			this.value = value;
		}
	}

	private static final class SearchAccumulator {
		private final List<RawCandidate> candidates = new ArrayList<>();
		private double degenerateDistance = Double.POSITIVE_INFINITY;
		private boolean unsafeEvidence;
		private String diagnostic =
				"Bounded semantic evidence is incomplete";

		private void record(LocusPointInteractionLocalEvidence2D.Status status) {
			if (status == LocusPointInteractionLocalEvidence2D.Status.NOT_ESTABLISHED
					|| status == LocusPointInteractionLocalEvidence2D.Status.SINGULAR) {
				barrier(status == LocusPointInteractionLocalEvidence2D.Status.SINGULAR
						? "Singular local evidence prevents deterministic resolution"
						: "Local minimum evidence was not established");
			}
		}

		private void barrier(String reason) {
			unsafeEvidence = true;
			if (reason != null && !reason.trim().isEmpty()) {
				diagnostic = reason;
			}
		}

		private Search search(SearchCoverage coverage) {
			return new Search(canonicalCandidates(candidates), coverage,
					degenerateDistance, unsafeEvidence, diagnostic);
		}
	}

	private static final class Search {
		private final List<LocusPointInteractionCandidate2D> candidates;
		private final SearchCoverage coverage;
		private final double degenerateDistance;
		private final boolean unsafeEvidence;
		private final String diagnostic;

		private Search(List<LocusPointInteractionCandidate2D> candidates,
				SearchCoverage coverage, double degenerateDistance,
				boolean unsafeEvidence, String diagnostic) {
			this.candidates = candidates;
			this.coverage = coverage;
			this.degenerateDistance = degenerateDistance;
			this.unsafeEvidence = unsafeEvidence;
			this.diagnostic = diagnostic;
		}
	}

	private static final class PolynomialSpan {
		private final int index;
		private final double lower;
		private final double upper;

		private PolynomialSpan(int index, double lower, double upper) {
			this.index = index;
			this.lower = lower;
			this.upper = upper;
		}
	}

	private static final class SemanticSegment {
		private final LocusBranch2D branch;
		private final LocusInterval2D component;
		private final double lower;
		private final double upper;
		private final boolean first;
		private final boolean last;

		private SemanticSegment(LocusBranch2D branch,
				LocusInterval2D component, double lower, double upper,
				boolean first, boolean last) {
			this.branch = branch;
			this.component = component;
			this.lower = lower;
			this.upper = upper;
			this.first = first;
			this.last = last;
		}
	}

	private static final class Sample {
		private final double parameter;
		private final double squaredDistance;
		private final Regularity regularity;
		private final boolean valid;
		private final String diagnostic;

		private Sample(double parameter, double squaredDistance,
				Regularity regularity, boolean valid, String diagnostic) {
			this.parameter = parameter;
			this.squaredDistance = squaredDistance;
			this.regularity = regularity;
			this.valid = valid;
			this.diagnostic = diagnostic;
		}

		private static Sample valid(double parameter, double squaredDistance,
				Regularity regularity) {
			return new Sample(parameter, squaredDistance, regularity, true, "");
		}

		private static Sample invalid(double parameter, String diagnostic) {
			return new Sample(parameter, Double.NaN, Regularity.UNKNOWN, false,
					diagnostic);
		}
	}

	private static final class Refinement {
		private final double parameter;
		private final boolean valid;
		private final boolean converged;
		private final String diagnostic;

		private Refinement(double parameter, boolean valid, boolean converged,
				String diagnostic) {
			this.parameter = parameter;
			this.valid = valid;
			this.converged = converged;
			this.diagnostic = diagnostic;
		}

		private static Refinement invalid(String diagnostic) {
			return new Refinement(Double.NaN, false, false, diagnostic);
		}
	}

	private static final class Neighborhood {
		private final boolean hasLeft;
		private final boolean hasRight;
		private final double left;
		private final double right;
		private final double step;

		private Neighborhood(boolean hasLeft, boolean hasRight, double left,
				double right, double step) {
			this.hasLeft = hasLeft;
			this.hasRight = hasRight;
			this.left = left;
			this.right = right;
			this.step = step;
		}
	}

	private static final class DerivativeEstimate {
		private final boolean valid;
		private final double x;
		private final double y;

		private DerivativeEstimate(boolean valid, double x, double y) {
			this.valid = valid;
			this.x = x;
			this.y = y;
		}

		private static DerivativeEstimate valid(double x, double y) {
			return Double.isFinite(x) && Double.isFinite(y)
					? new DerivativeEstimate(true, x, y) : invalid();
		}

		private static DerivativeEstimate invalid() {
			return new DerivativeEstimate(false, 0, 0);
		}

		private double speed() {
			return Math.hypot(x, y);
		}
	}

	private static final class RootWorkRecorder
			implements PolynomialRootIsolation2D.WorkRecorder {
		private final LocusPointInteractionInstrumentation2D instrumentation;

		private RootWorkRecorder(
				LocusPointInteractionInstrumentation2D instrumentation) {
			this.instrumentation = instrumentation;
		}

		@Override
		public void recordIsolationSubdivision(int count) {
			for (int index = 0; index < count; index++) {
				instrumentation.subdivision();
			}
		}

		@Override
		public void recordRefinementStarted() {
			// Invocation count is derivable from the root cells retained.
		}

		@Override
		public void recordRefinementIteration(long iteration) {
			instrumentation.refinementIteration();
		}
	}
}

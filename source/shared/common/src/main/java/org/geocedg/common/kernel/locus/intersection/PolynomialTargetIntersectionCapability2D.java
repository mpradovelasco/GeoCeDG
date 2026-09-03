/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2D.RootCell;

/**
 * Span-wise implicit-polynomial composition for semantic polynomial loci.
 *
 * <p>Each source span is localized to {@code [0,1]}, composed with the
 * captured target polynomial and partitioned at recursively isolated
 * derivative roots. This is deterministic and captures even-contact roots,
 * but it deliberately reports floating estimated evidence rather than an
 * interval-certified global root count.</p>
 */
final class PolynomialTargetIntersectionCapability2D
		implements LocusIntersectionCapability2D {
	private static final int MAXIMUM_COMPOSED_DEGREE = 96;

	@Override
	public String getCapabilityId() {
		return "g9s1-span-polynomial-target/v1";
	}

	@Override
	public boolean supports(IntersectionCapabilityContext2D context) {
		if (!(context.getDefinition().getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D)
				|| !(context.getTarget()
						instanceof PolynomialIntersectionTarget2D)) {
			return false;
		}
		PiecewisePolynomialLocus2D source = (PiecewisePolynomialLocus2D) context
				.getDefinition().getEvaluatorCapability();
		int compositionDepth = source.getPolynomialCompositionDepth();
		return compositionDepth > 0 && compositionDepth
				<= PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH
				&& source.supportsPiecewisePolynomial(context.getDefinition())
				&& withinCompositionPolicy(context, source,
						((PolynomialIntersectionTarget2D) context.getTarget())
								.getImplicitPolynomialCoefficients());
	}

	@Override
	public IntersectionCandidateSet2D isolate(
			IntersectionCapabilityContext2D context) {
		if (!supports(context)) {
			throw new IllegalArgumentException(
					"Polynomial target capability requires explicit source spans");
		}
		PiecewisePolynomialLocus2D source = (PiecewisePolynomialLocus2D) context
				.getDefinition().getEvaluatorCapability();
		double[][] target = ((PolynomialIntersectionTarget2D) context.getTarget())
				.getImplicitPolynomialCoefficients();
		validateTarget(target);
		ArrayList<LocatedRoot> located = new ArrayList<>();
		java.util.Map<String, SplineImplicitIntervalCertification2D> certifiers =
				new java.util.LinkedHashMap<>();
		ArrayList<String> examinedComponents = new ArrayList<>();
		boolean zeroPolynomialSpan = false;
		boolean structuralTarget = context.getTarget().getFamily()
				== TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT;
		for (LocusBranch2D branch : context.getDefinition().getBranches()) {
			SplineImplicitIntervalCertification2D certifier = structuralTarget
					? SplineImplicitIntervalCertification2D.capture(context.getDefinition(),
							branch.getBranchKey(), target,
							context.getQuery().getPolicy().getWorkBudget())
					: null;
			if (certifier != null) {
				certifiers.put(branch.getBranchKey(), certifier);
			} else if (structuralTarget
					&& SplineImplicitIntervalCertification2D.isSplineSource(
							context.getDefinition())) {
				return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
						CompletenessMethod.NOT_ESTABLISHED, GeometryKind.UNRESOLVED,
						SupportLevel.VERIFIED_UNCERTIFIED,
						NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
						Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
						List.of(new IntersectionDiagnostic2D(DiagnosticCode.CANDIDATE_REJECTED,
								"Spline structural capture unavailable; floating fallback "
										+ "cannot certify roots")));
			}
			for (int componentIndex = 0; componentIndex < branch
					.getValidDomainComponents().size(); componentIndex++) {
				String componentKey = IntersectionCapabilityContext2D.componentKey(
						branch.getBranchKey(), componentIndex);
				examinedComponents.add(componentKey);
				LocusInterval2D component = branch.getValidDomainComponents()
						.get(componentIndex);
				boolean periodicCycle = isPeriodicFundamentalCycle(context,
						branch, component);
				int spanCount = source.getPolynomialSpanCount(
						branch.getBranchKey());
				for (int span = 0; span < spanCount; span++) {
					double lower = source.getPolynomialSpanLower(
							branch.getBranchKey(), span);
					double upper = source.getPolynomialSpanUpper(
							branch.getBranchKey(), span);
					if (!belongsToComponent(component, lower, upper,
							context.getDefinition().getProvider().getDomainEpsilon())) {
						continue;
					}
					context.getInstrumentation().recordPolynomialSpanExamined();
					double[][] coordinates = source
							.getPolynomialCoordinateCoefficients(
									branch.getBranchKey(), span);
					double[] x = localPolynomial(coordinates[0], lower, upper);
					double[] y = localPolynomial(coordinates[1], lower, upper);
					double[] composed = compose(target, x, y);
					if (composed == null) {
						throw new IllegalArgumentException(
								"Composed spline target polynomial exceeds work policy");
					}
					double parameterTolerance = context.getQuery().getPolicy()
							.getRootParameterTolerance().getValue();
					PolynomialRootIsolation2D.IsolationResult rootEvidence =
							PolynomialRootIsolation2D.isolate(composed, 0, 1,
									parameterTolerance,
									context.getQuery().getPolicy().getWorkBudget()
											.getMaximumRefinementIterations(),
									workRecorder(context));
					if (rootEvidence.isZeroPolynomial()) {
						zeroPolynomialSpan = true;
						continue;
					}
					List<RootCell> roots = rootEvidence.getCells();
					if (roots.isEmpty()) {
						context.getInstrumentation().recordPolynomialSpanRejected();
					}
					for (RootCell root : roots) {
						context.getInstrumentation().recordPolynomialRootCandidate();
						double local = certifier != null ? root.getParameter()
								: canonicalLocal(root.getParameter(),
								context.getQuery().getPolicy()
										.getRootParameterTolerance().getValue()
										/ (upper - lower));
						if (certifier == null && local == 1 && span + 1 < spanCount) {
							continue;
						}
						boolean lowerSeam = periodicCycle && span == 0
								&& local == 0;
						boolean upperSeam = periodicCycle && span + 1 == spanCount
								&& local == 1;
						double parameter = certifier == null && upperSeam ? component.getLower()
								: lower + (upper - lower) * local;
						double cellLower = certifier == null && (lowerSeam || upperSeam)
								? component.getLower()
								: lower + (upper - lower) * root.getLower();
						double cellUpper = certifier == null && (lowerSeam || upperSeam)
								? component.getLower()
								: lower + (upper - lower) * root.getUpper();
						located.add(new LocatedRoot(branch.getBranchKey(),
								componentKey, component, parameter, cellLower,
								cellUpper, lowerSeam ? 1 : upperSeam ? 2 : 0,
								root.isStationaryProposal()));
					}
				}
			}
		}
		if (zeroPolynomialSpan) {
			return unresolvedOverlap(examinedComponents);
		}
		List<LocatedRoot> roots = certifiers.isEmpty() ? deduplicate(context, located) : located;
		ArrayList<IntersectionCandidate2D> candidates = new ArrayList<>();
		ArrayList<IntersectionDiagnostic2D> overallDiagnostics = new ArrayList<>();
		for (LocatedRoot root : roots) {
			SplineImplicitIntervalCertification2D certifier = certifiers.get(root.branchKey);
			SplineImplicitIntervalCertification2D.Proof proof = null;
			if (certifier != null) {
				proof = certifier.verify(root.component, root.parameter, root.lower, root.upper);
				if (proof.status == SplineImplicitIntervalCertification2D.Status.EXCLUDED) {
					context.getInstrumentation().recordRejectedCandidate();
					continue;
				}
			}
			classify(context, root);
			if (proof != null) {
				if (proof.status == SplineImplicitIntervalCertification2D.Status.SIMPLE) {
					root.parameter = proof.parameter;
					root.lower = proof.canonicalRoot.lower;
					root.upper = proof.canonicalRoot.upper;
					root.contact = ContactClass.TRANSVERSE_ESTABLISHED;
					if (context.getDefinition().getProvider().isPeriodic()
							&& root.parameter == root.component.getLower()) {
						root.periodicSeamSides = 3;
					}
				} else {
					boolean exactBoundary = certifier.hasExactBoundaryZero(root.parameter);
					if ((root.stationaryProposal || exactBoundary)
							&& root.contact == ContactClass.TRANSVERSE_ESTABLISHED) {
						// A tiny nonzero gradient at an approximate stationary seed
						// does not prove transversality at its true contact.
						root.contact = ContactClass.CONTACT_UNDETERMINED;
					}
					if (!proof.compatibleContact || !root.stationaryProposal && !exactBoundary) {
						context.getInstrumentation().recordUnresolvedCandidate();
						overallDiagnostics.add(new IntersectionDiagnostic2D(
								DiagnosticCode.CANDIDATE_REJECTED,
								"Floating spline proposal lacks structural root evidence; "
										+ "not a verified root"));
						continue;
					}
					root.parameter = context.getDefinition().getProvider()
							.canonicalize(root.parameter);
					root.lower = Math.min(root.lower, root.parameter);
					root.upper = Math.max(root.upper, root.parameter);
				}
			}
			if (!acceptable(context, root)) {
				continue;
			}
			LocalIsolationStatus isolation = root.contact
					== ContactClass.TRANSVERSE_ESTABLISHED
							&& (proof != null || !root.isPeriodicSeam()
									|| root.hasBothPeriodicSeamSides())
							? LocalIsolationStatus.ESTABLISHED
							: LocalIsolationStatus.NOT_ESTABLISHED;
			ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
			diagnostics.add(new IntersectionDiagnostic2D(
					isolation == LocalIsolationStatus.ESTABLISHED
							? DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED
							: DiagnosticCode.CONTINUATION_AMBIGUOUS,
					proof == null ? "Root localized by source-span polynomial composition and "
							+ "derivative-partition refinement; floating arithmetic "
							+ "does not establish global completeness"
							: isolation == LocalIsolationStatus.ESTABLISHED
									? "Direct structural Q(S(I))/gradient proof: "
											+ "strict scalar inclusion or exact "
											+ "boundary-zero witness "
											+ "with nonzero derivative; "
											+ "local, not complete enumeration"
									: "Estimated stationary/singular contact retained rich-only; "
											+ "structural interval compatibility is not a "
											+ "root-multiplicity certificate"));
			IntersectionCandidate2D candidate = new IntersectionCandidate2D(root.branchKey,
					root.componentKey, root.parameter,
					root.isPeriodicSeam()
							? OptionalDouble.of(root.component.getUpper())
							: OptionalDouble.empty(),
					new IntersectionParameterInterval2D(root.lower, root.upper),
					isolation, Optional.empty(), root.contact,
					MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
					proof != null && isolation == LocalIsolationStatus.ESTABLISHED
							? SolverMethod.CERTIFIED_INTERVAL : SolverMethod.SAFEGUARDED_DERIVATIVE,
					NumericGuarantee.ESTIMATED_ERROR, LineageEventKind.APPEARED,
					Collections.emptyList(), diagnostics);
			candidates.add(proof == null ? candidate : candidate.withStructuralCertificate(proof));
		}
		for (SplineImplicitIntervalCertification2D certifier : certifiers.values()) {
			overallDiagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
					certifier.workSummary()));
		}
		overallDiagnostics.add(new IntersectionDiagnostic2D(DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
				certifiers.isEmpty()
						? "G9S1 examined every explicit polynomial span, but floating coefficient "
								+ "arithmetic is not a certified global root-count proof"
						: "G9S1 examined every explicit polynomial span; structural "
								+ "local certificates do not establish complete root enumeration"));
		return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED,
				candidates.isEmpty() ? GeometryKind.UNRESOLVED
						: GeometryKind.FINITE,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.ESTIMATED_ERROR, examinedComponents, candidates,
				Collections.emptyList(), overallDiagnostics);
	}

	private static PolynomialRootIsolation2D.WorkRecorder workRecorder(
			IntersectionCapabilityContext2D context) {
		return new PolynomialRootIsolation2D.WorkRecorder() {
			@Override
			public void recordIsolationSubdivision(int depth) {
				context.getInstrumentation().recordIsolationSubdivision(depth);
			}

			@Override
			public void recordRefinementStarted() {
				context.getInstrumentation().recordRefinementStarted();
			}

			@Override
			public void recordRefinementIteration(long iteration) {
				context.getInstrumentation().recordRefinementIteration(iteration);
			}
		};
	}

	private static IntersectionCandidateSet2D unresolvedOverlap(
			List<String> examinedComponents) {
		return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED, GeometryKind.UNRESOLVED,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, examinedComponents,
				Collections.emptyList(), Collections.emptyList(), List.of(
						new IntersectionDiagnostic2D(
								DiagnosticCode.OVERLAP_SUSPECTED,
								"An entire semantic polynomial span has zero target "
										+ "composition; finite roots are not fabricated")));
	}

	private static void classify(IntersectionCapabilityContext2D context,
			LocatedRoot root) {
		LocusEvaluation2D evaluation = context.evaluate(root.branchKey,
				root.parameter);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return;
		}
		TargetContactEvidence2D contact = context.evaluateContact(
				evaluation.getPoint(), context.evaluateDifferential(root.branchKey,
						root.parameter, root.component));
		if (!contact.isEstablished()) {
			return;
		}
		root.contact = Math.abs(contact.getNormalizedIndicator()) <= context
				.getQuery().getPolicy().getTangencyTolerance().getThreshold()
						? ContactClass.TANGENT_ESTABLISHED
						: ContactClass.TRANSVERSE_ESTABLISHED;
	}

	private static boolean acceptable(IntersectionCapabilityContext2D context,
			LocatedRoot root) {
		LocusEvaluation2D evaluation = context.evaluate(root.branchKey,
				root.parameter);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return false;
		}
		TargetCandidateEvaluation2D target = context
				.evaluateCandidateLevel(evaluation.getPoint());
		return target.isEstablished() && Math.abs(target.getLevel().getAsDouble())
				<= context.getQuery().getPolicy().getResidualTolerance()
						.threshold(target.getCharacteristicScale().getAsDouble());
	}

	private static List<LocatedRoot> deduplicate(
			IntersectionCapabilityContext2D context, List<LocatedRoot> input) {
		List<LocatedRoot> sorted = input.stream()
				.sorted(Comparator.comparing((LocatedRoot root) -> root.branchKey)
						.thenComparing(root -> root.componentKey)
						.thenComparingDouble(root -> root.parameter))
				.toList();
		ArrayList<LocatedRoot> output = new ArrayList<>();
		double tolerance = context.getQuery().getPolicy()
				.getDeduplicationTolerance().getValue();
		for (LocatedRoot candidate : sorted) {
			LocatedRoot previous = output.isEmpty() ? null
					: output.get(output.size() - 1);
			if (previous != null && previous.branchKey.equals(candidate.branchKey)
					&& previous.componentKey.equals(candidate.componentKey)) {
				context.getInstrumentation().recordDeduplicationComparison();
				if (Math.abs(previous.parameter - candidate.parameter) <= tolerance) {
					previous.include(candidate);
					continue;
				}
			}
			context.getInstrumentation().recordCandidateInterval();
			output.add(candidate);
		}
		return output;
	}

	private static double[] compose(double[][] target, double[] x,
			double[] y) {
		int maximumX = target.length - 1;
		int maximumY = 0;
		for (double[] row : target) {
			maximumY = Math.max(maximumY, row.length - 1);
		}
		int sourceDegree = Math.max(x.length, y.length) - 1;
		if ((long) sourceDegree * (maximumX + maximumY)
				> MAXIMUM_COMPOSED_DEGREE) {
			return null;
		}
		double[][] xPowers = powers(x, maximumX);
		double[][] yPowers = powers(y, maximumY);
		double[] result = new double[MAXIMUM_COMPOSED_DEGREE + 1];
		int used = 1;
		for (int xDegree = 0; xDegree < target.length; xDegree++) {
			for (int yDegree = 0; yDegree < target[xDegree].length;
					yDegree++) {
				double coefficient = target[xDegree][yDegree];
				if (coefficient == 0) {
					continue;
				}
				double[] term = multiply(xPowers[xDegree], yPowers[yDegree]);
				used = Math.max(used, term.length);
				for (int index = 0; index < term.length; index++) {
					result[index] += coefficient * term[index];
				}
			}
		}
		return java.util.Arrays.copyOf(result, used);
	}

	private static boolean withinCompositionPolicy(
			IntersectionCapabilityContext2D context,
			PiecewisePolynomialLocus2D source, double[][] target) {
		try {
			validateTarget(target);
			int targetDegree = maximumTotalDegree(target);
			for (LocusBranch2D branch : context.getDefinition().getBranches()) {
				int spanCount = source.getPolynomialSpanCount(
						branch.getBranchKey());
				if (spanCount < 1 || spanCount > 64) {
					return false;
				}
				for (int span = 0; span < spanCount; span++) {
					double[][] coordinates = source
							.getPolynomialCoordinateCoefficients(
									branch.getBranchKey(), span);
					int sourceDegree = Math.max(coordinates[0].length,
							coordinates[1].length) - 1;
					if ((long) sourceDegree * targetDegree
							> MAXIMUM_COMPOSED_DEGREE) {
						return false;
					}
				}
			}
			return true;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static int maximumTotalDegree(double[][] target) {
		int degree = 0;
		for (int xDegree = 0; xDegree < target.length; xDegree++) {
			for (int yDegree = 0; yDegree < target[xDegree].length;
					yDegree++) {
				if (target[xDegree][yDegree] != 0) {
					degree = Math.max(degree, xDegree + yDegree);
				}
			}
		}
		return degree;
	}

	private static double[][] powers(double[] polynomial, int maximumPower) {
		double[][] result = new double[maximumPower + 1][];
		result[0] = new double[] {1};
		for (int power = 1; power <= maximumPower; power++) {
			result[power] = multiply(result[power - 1], polynomial);
		}
		return result;
	}

	private static double[] multiply(double[] first, double[] second) {
		double[] result = new double[first.length + second.length - 1];
		for (int firstIndex = 0; firstIndex < first.length; firstIndex++) {
			for (int secondIndex = 0; secondIndex < second.length;
					secondIndex++) {
				result[firstIndex + secondIndex] += first[firstIndex]
						* second[secondIndex];
			}
		}
		return result;
	}

	private static double[] localPolynomial(double[] descending, double lower,
			double upper) {
		if (descending == null || descending.length == 0 || !(lower < upper)) {
			throw new IllegalArgumentException(
					"Polynomial source span must be finite and nonempty");
		}
		int degree = descending.length - 1;
		double width = upper - lower;
		double[] local = new double[descending.length];
		for (int globalPower = 0; globalPower <= degree; globalPower++) {
			double coefficient = descending[degree - globalPower];
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Polynomial source coefficients must be finite");
			}
			for (int localPower = 0; localPower <= globalPower;
					localPower++) {
				local[localPower] += coefficient
						* binomial(globalPower, localPower)
						* Math.pow(lower, globalPower - localPower)
						* Math.pow(width, localPower);
			}
		}
		return local;
	}

	private static double binomial(int n, int k) {
		int symmetric = Math.min(k, n - k);
		double value = 1;
		for (int index = 1; index <= symmetric; index++) {
			value *= (double) (n - symmetric + index) / index;
		}
		return value;
	}

	private static double canonicalLocal(double parameter, double tolerance) {
		if (Math.abs(parameter) <= tolerance) {
			return 0;
		}
		if (Math.abs(parameter - 1) <= tolerance) {
			return 1;
		}
		return parameter;
	}

	private static boolean belongsToComponent(LocusInterval2D component,
			double lower, double upper, double epsilon) {
		double middle = lower + (upper - lower) / 2;
		return component.contains(middle, epsilon);
	}

	private static boolean isPeriodicFundamentalCycle(
			IntersectionCapabilityContext2D context, LocusBranch2D branch,
			LocusInterval2D component) {
		return context.getDefinition().getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(context.getDefinition().getProvider()
						.getDeclaredDomain());
	}

	private static void validateTarget(double[][] target) {
		if (target == null || target.length == 0) {
			throw new IllegalArgumentException(
					"Implicit target polynomial is required");
		}
		boolean nonzero = false;
		for (double[] row : target) {
			if (row == null || row.length == 0) {
				throw new IllegalArgumentException(
						"Implicit target polynomial rows are required");
			}
			for (double coefficient : row) {
				if (!Double.isFinite(coefficient)) {
					throw new IllegalArgumentException(
							"Implicit target polynomial must be finite");
				}
				nonzero |= coefficient != 0;
			}
		}
		if (!nonzero) {
			throw new IllegalArgumentException(
					"Implicit target polynomial must be nonzero");
		}
	}

	private static final class LocatedRoot {
		private final String branchKey;
		private final String componentKey;
		private final LocusInterval2D component;
		private double parameter;
		private double lower;
		private double upper;
		private int periodicSeamSides;
		private boolean stationaryProposal;
		private ContactClass contact = ContactClass.CONTACT_UNDETERMINED;

		private LocatedRoot(String branchKey, String componentKey,
				LocusInterval2D component, double parameter, double lower,
				double upper, int periodicSeamSides, boolean stationaryProposal) {
			this.branchKey = branchKey;
			this.componentKey = componentKey;
			this.component = component;
			this.parameter = parameter;
			this.lower = Math.min(lower, parameter);
			this.upper = Math.max(upper, parameter);
			this.periodicSeamSides = periodicSeamSides;
			this.stationaryProposal = stationaryProposal;
		}

		private void include(LocatedRoot other) {
			lower = Math.min(lower, other.lower);
			upper = Math.max(upper, other.upper);
			periodicSeamSides |= other.periodicSeamSides;
			stationaryProposal |= other.stationaryProposal;
		}

		private boolean isPeriodicSeam() {
			return periodicSeamSides != 0;
		}

		private boolean hasBothPeriodicSeamSides() {
			return periodicSeamSides == 3;
		}
	}
}

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
	private static final double COEFFICIENT_EPSILON = 512 * Math.ulp(1.0);

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
		return source.supportsPiecewisePolynomial(context.getDefinition())
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
		ArrayList<String> examinedComponents = new ArrayList<>();
		boolean zeroPolynomialSpan = false;
		for (LocusBranch2D branch : context.getDefinition().getBranches()) {
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
					double[] x = localPolynomial(source.getPolynomialCoefficients(
							branch.getBranchKey(), span, 0), lower, upper);
					double[] y = localPolynomial(source.getPolynomialCoefficients(
							branch.getBranchKey(), span, 1), lower, upper);
					double[] composed = compose(target, x, y);
					if (composed == null) {
						throw new IllegalArgumentException(
								"Composed spline target polynomial exceeds work policy");
					}
					NormalizedPolynomial normalized = normalize(composed);
					if (normalized.zero) {
						zeroPolynomialSpan = true;
						continue;
					}
					List<RootCell> roots = roots(normalized.coefficients, context);
					if (roots.isEmpty()) {
						context.getInstrumentation().recordPolynomialSpanRejected();
					}
					for (RootCell root : roots) {
						context.getInstrumentation().recordPolynomialRootCandidate();
						double local = canonicalLocal(root.parameter,
								context.getQuery().getPolicy()
										.getRootParameterTolerance().getValue()
										/ (upper - lower));
						if (local == 1 && span + 1 < spanCount) {
							continue;
						}
						boolean lowerSeam = periodicCycle && span == 0
								&& local == 0;
						boolean upperSeam = periodicCycle && span + 1 == spanCount
								&& local == 1;
						double parameter = upperSeam ? component.getLower()
								: lower + (upper - lower) * local;
						double cellLower = lowerSeam || upperSeam
								? component.getLower()
								: lower + (upper - lower) * root.lower;
						double cellUpper = lowerSeam || upperSeam
								? component.getLower()
								: lower + (upper - lower) * root.upper;
						located.add(new LocatedRoot(branch.getBranchKey(),
								componentKey, component, parameter, cellLower,
								cellUpper, lowerSeam ? 1 : upperSeam ? 2 : 0));
					}
				}
			}
		}
		if (zeroPolynomialSpan) {
			return unresolvedOverlap(examinedComponents);
		}
		List<LocatedRoot> roots = deduplicate(context, located);
		ArrayList<IntersectionCandidate2D> candidates = new ArrayList<>();
		for (LocatedRoot root : roots) {
			classify(context, root);
			if (!acceptable(context, root)) {
				continue;
			}
			LocalIsolationStatus isolation = root.contact
					== ContactClass.TRANSVERSE_ESTABLISHED
							&& (!root.isPeriodicSeam()
									|| root.hasBothPeriodicSeamSides())
							? LocalIsolationStatus.ESTABLISHED
							: LocalIsolationStatus.NOT_ESTABLISHED;
			ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
			diagnostics.add(new IntersectionDiagnostic2D(
					isolation == LocalIsolationStatus.ESTABLISHED
							? DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED
							: DiagnosticCode.CONTINUATION_AMBIGUOUS,
					"Root localized by source-span polynomial composition and "
							+ "derivative-partition refinement; floating arithmetic "
							+ "does not establish global completeness"));
			candidates.add(new IntersectionCandidate2D(root.branchKey,
					root.componentKey, root.parameter,
					root.isPeriodicSeam()
							? OptionalDouble.of(root.component.getUpper())
							: OptionalDouble.empty(),
					new IntersectionParameterInterval2D(root.lower, root.upper),
					isolation, Optional.empty(), root.contact,
					MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
					SolverMethod.SAFEGUARDED_DERIVATIVE,
					NumericGuarantee.ESTIMATED_ERROR, LineageEventKind.APPEARED,
					Collections.emptyList(), diagnostics));
		}
		return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED,
				candidates.isEmpty() ? GeometryKind.UNRESOLVED
						: GeometryKind.FINITE,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.ESTIMATED_ERROR, examinedComponents, candidates,
				Collections.emptyList(), List.of(new IntersectionDiagnostic2D(
						DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
						"G9S1 examined every explicit polynomial span, but "
								+ "floating coefficient arithmetic is not a certified "
								+ "global root-count proof")));
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

	private static List<RootCell> roots(double[] polynomial,
			IntersectionCapabilityContext2D context) {
		return roots(polynomial, 0, 1, context, 0);
	}

	private static List<RootCell> roots(double[] polynomial, double lower,
			double upper, IntersectionCapabilityContext2D context, int depth) {
		NormalizedPolynomial normalized = normalize(polynomial);
		if (normalized.zero || normalized.coefficients.length == 1) {
			return Collections.emptyList();
		}
		int degree = normalized.coefficients.length - 1;
		if (degree == 1) {
			double root = -normalized.coefficients[0]
					/ normalized.coefficients[1];
			double tolerance = context.getQuery().getPolicy()
					.getRootParameterTolerance().getValue();
			if (!Double.isFinite(root) || root < lower - tolerance
					|| root > upper + tolerance) {
				return Collections.emptyList();
			}
			root = Math.max(lower, Math.min(upper, root));
			return List.of(new RootCell(root, root, root));
		}
		double[] derivative = new double[degree];
		for (int power = 1; power <= degree; power++) {
			derivative[power - 1] = power * normalized.coefficients[power];
		}
		ArrayList<Double> partition = new ArrayList<>();
		partition.add(lower);
		for (RootCell critical : roots(derivative, lower, upper, context,
				depth + 1)) {
			if (critical.parameter > lower && critical.parameter < upper) {
				partition.add(critical.parameter);
			}
		}
		partition.add(upper);
		partition.sort(Double::compare);
		partition = unique(partition, Math.max(Math.ulp(1.0) * 32,
				context.getQuery().getPolicy().getRootParameterTolerance()
						.getValue()));
		ArrayList<RootCell> result = new ArrayList<>();
		double valueTolerance = COEFFICIENT_EPSILON
				* Math.max(1, normalized.coefficients.length);
		for (int index = 0; index < partition.size(); index++) {
			double point = partition.get(index);
			if (Math.abs(evaluate(normalized.coefficients, point))
					<= valueTolerance) {
				double cellLower = index == 0 ? point
						: partition.get(index - 1);
				double cellUpper = index + 1 == partition.size() ? point
						: partition.get(index + 1);
				result.add(new RootCell(point, cellLower, cellUpper));
			}
		}
		for (int index = 0; index + 1 < partition.size(); index++) {
			double left = partition.get(index);
			double right = partition.get(index + 1);
			double leftValue = evaluate(normalized.coefficients, left);
			double rightValue = evaluate(normalized.coefficients, right);
			if (!oppositeSigns(leftValue, rightValue)) {
				continue;
			}
			context.getInstrumentation().recordIsolationSubdivision(1);
			RootCell root = bisect(normalized.coefficients, left, right,
					context);
			result.add(root);
		}
		return deduplicateCells(result, context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue());
	}

	private static RootCell bisect(double[] polynomial, double initialLower,
			double initialUpper, IntersectionCapabilityContext2D context) {
		context.getInstrumentation().recordRefinementStarted();
		double lower = initialLower;
		double upper = initialUpper;
		double lowerValue = evaluate(polynomial, lower);
		double tolerance = context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue();
		for (long iteration = 1; iteration <= context.getQuery().getPolicy()
				.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			context.getInstrumentation().recordRefinementIteration(iteration);
			if (upper - lower <= tolerance) {
				break;
			}
			double middle = lower + (upper - lower) / 2;
			double value = evaluate(polynomial, middle);
			if (value == 0) {
				return new RootCell(middle, middle, middle);
			}
			if (oppositeSigns(lowerValue, value)) {
				upper = middle;
			} else {
				lower = middle;
				lowerValue = value;
			}
		}
		double parameter = lower + (upper - lower) / 2;
		return new RootCell(parameter, lower, upper);
	}

	private static ArrayList<Double> unique(List<Double> input,
			double tolerance) {
		ArrayList<Double> result = new ArrayList<>();
		for (double value : input) {
			if (result.isEmpty()
					|| Math.abs(value - result.get(result.size() - 1)) > tolerance) {
				result.add(value);
			}
		}
		return result;
	}

	private static List<RootCell> deduplicateCells(List<RootCell> input,
			double tolerance) {
		input.sort(Comparator.comparingDouble(root -> root.parameter));
		ArrayList<RootCell> result = new ArrayList<>();
		for (RootCell candidate : input) {
			if (!result.isEmpty() && Math.abs(candidate.parameter
					- result.get(result.size() - 1).parameter) <= tolerance) {
				result.get(result.size() - 1).include(candidate);
			} else {
				result.add(candidate);
			}
		}
		return result;
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
					int sourceDegree = Math.max(source.getPolynomialCoefficients(
							branch.getBranchKey(), span, 0).length,
							source.getPolynomialCoefficients(branch.getBranchKey(),
									span, 1).length) - 1;
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

	private static NormalizedPolynomial normalize(double[] polynomial) {
		double scale = 0;
		for (double coefficient : polynomial) {
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Composed polynomial coefficients must be finite");
			}
			scale = Math.max(scale, Math.abs(coefficient));
		}
		if (scale == 0) {
			return new NormalizedPolynomial(new double[] {0}, true);
		}
		double[] normalized = polynomial.clone();
		for (int index = 0; index < normalized.length; index++) {
			normalized[index] /= scale;
		}
		int degree = normalized.length - 1;
		while (degree > 0 && Math.abs(normalized[degree])
				<= COEFFICIENT_EPSILON) {
			degree--;
		}
		normalized = java.util.Arrays.copyOf(normalized, degree + 1);
		boolean zero = degree == 0
				&& Math.abs(normalized[0]) <= COEFFICIENT_EPSILON;
		return new NormalizedPolynomial(normalized, zero);
	}

	private static double evaluate(double[] ascending, double parameter) {
		double value = 0;
		for (int index = ascending.length - 1; index >= 0; index--) {
			value = value * parameter + ascending[index];
		}
		return value;
	}

	private static boolean oppositeSigns(double first, double second) {
		return first < 0 && second > 0 || first > 0 && second < 0;
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

	private static final class NormalizedPolynomial {
		private final double[] coefficients;
		private final boolean zero;

		private NormalizedPolynomial(double[] coefficients, boolean zero) {
			this.coefficients = coefficients;
			this.zero = zero;
		}
	}

	private static final class RootCell {
		private final double parameter;
		private double lower;
		private double upper;

		private RootCell(double parameter, double lower, double upper) {
			this.parameter = parameter;
			this.lower = Math.min(lower, parameter);
			this.upper = Math.max(upper, parameter);
		}

		private void include(RootCell other) {
			lower = Math.min(lower, other.lower);
			upper = Math.max(upper, other.upper);
		}
	}

	private static final class LocatedRoot {
		private final String branchKey;
		private final String componentKey;
		private final LocusInterval2D component;
		private final double parameter;
		private double lower;
		private double upper;
		private int periodicSeamSides;
		private ContactClass contact = ContactClass.CONTACT_UNDETERMINED;

		private LocatedRoot(String branchKey, String componentKey,
				LocusInterval2D component, double parameter, double lower,
				double upper, int periodicSeamSides) {
			this.branchKey = branchKey;
			this.componentKey = componentKey;
			this.component = component;
			this.parameter = parameter;
			this.lower = Math.min(lower, parameter);
			this.upper = Math.max(upper, parameter);
			this.periodicSeamSides = periodicSeamSides;
		}

		private void include(LocatedRoot other) {
			lower = Math.min(lower, other.lower);
			upper = Math.max(upper, other.upper);
			periodicSeamSides |= other.periodicSeamSides;
		}

		private boolean isPeriodicSeam() {
			return periodicSeamSides != 0;
		}

		private boolean hasBothPeriodicSeamSides() {
			return periodicSeamSides == 3;
		}
	}
}

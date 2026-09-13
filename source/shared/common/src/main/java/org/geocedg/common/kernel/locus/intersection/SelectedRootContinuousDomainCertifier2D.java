/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.locus.intersection.PublicTargetIntersectionCapability2D.TransverseOrientation;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Bounded structural interval capability for an exact selected SplineV2/line
 * root driven by a point on a circle.
 *
 * <p>The fixed grid partitions parameter space only. A cell is published only
 * when outward interval evaluation proves existence, uniqueness under the
 * retained selector germ and continuation throughout the whole cell. Point
 * samples never certify a cell.</p>
 */
public final class SelectedRootContinuousDomainCertifier2D {
	private static final int DRIVER_CELL_COUNT = 512;
	private static final int SOURCE_MAX_DEPTH = 14;
	private static final String METHOD =
			"selected-spline-parallel-line/outward-interval/v1";

	private SelectedRootContinuousDomainCertifier2D() {
		// Static shared-kernel capability.
	}

	/**
	 * Attempts the bounded approved family without changing root identity.
	 *
	 * @return explicit structure when the family is recognized; empty otherwise
	 */
	public static Optional<LocusExistenceStructure2D> certify(
			SemanticGeneratorDescriptor1D descriptor,
			GeoPointND dependentPoint, GeoElement state, GeoElement support) {
		if (descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCLE_POINT
				|| !(support instanceof GeoConic)
				|| ((GeoConic) support).getType()
						!= GeoConicNDConstants.CONIC_CIRCLE) {
			return Optional.empty();
		}
		SelectedRootFamily family = selectedRootFamily(dependentPoint, state);
		if (family == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(certifyFamily(descriptor, (GeoConic) support,
					family));
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return Optional.of(LocusExistenceStructure2D.notEstablished(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
					descriptor.getDeclaredDomain(),
					"selected-root-interval-proof-refused/"
							+ exception.getClass().getSimpleName()));
		}
	}

	private static LocusExistenceStructure2D certifyFamily(
			SemanticGeneratorDescriptor1D descriptor, GeoConic circle,
			SelectedRootFamily family) {
		LocusDefinitionEvidence evidence = sourceEvidence(family);
		SplineIntervalModel2D model = SplineIntervalModel2D.capture(
				evidence.source.getSemanticDefinition(), evidence.branchKey);
		if (model == null || !model.isStructural()
				|| family.selector.hasIntrinsicPhase()) {
			return LocusExistenceStructure2D.notEstablished(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
					descriptor.getDeclaredDomain(),
					"selected-root-family-lacks-structural-unique-selector");
		}
		TransverseOrientation orientation = PublicTargetIntersectionCapability2D
				.currentPublicRootOrientation(
						family.selector.getCurrentRootGerm()).orElse(null);
		if (orientation == null) {
			return LocusExistenceStructure2D.notEstablished(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
					descriptor.getDeclaredDomain(),
					"selected-root-transverse-orientation-not-established");
		}
		double[] knots = model.getKnots();
		int owningSpan = owningSpan(knots, evidence.currentRootParameter);
		if (owningSpan < 0) {
			return LocusExistenceStructure2D.notEstablished(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
					descriptor.getDeclaredDomain(),
					"selected-root-parameter-outside-structural-spline");
		}

		double normalX = family.baseLine.getX();
		double normalY = family.baseLine.getY();
		if (!Double.isFinite(normalX) || !Double.isFinite(normalY)
				|| normalX == 0 && normalY == 0) {
			throw new ArithmeticException("No finite line normal");
		}
		int firstSpan = owningSpan;
		int lastSpan = owningSpan;
		while (firstSpan > 0 && hasOrientation(model, knots[firstSpan - 1],
				knots[firstSpan], normalX, normalY, orientation)) {
			firstSpan--;
		}
		while (lastSpan + 1 < knots.length - 1
				&& hasOrientation(model, knots[lastSpan + 1],
						knots[lastSpan + 2], normalX, normalY, orientation)) {
			lastSpan++;
		}
		double rootLower = knots[firstSpan];
		double rootUpper = knots[lastSpan + 1];
		if (!hasOrientation(model, rootLower, rootUpper, normalX, normalY,
				orientation)
				|| !model.isSmooth(new SplineOutwardInterval2D(rootLower,
						rootUpper))) {
			return LocusExistenceStructure2D.notEstablished(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
					descriptor.getDeclaredDomain(),
					"selected-root-monotone-chart-not-established");
		}
		SplineOutwardInterval2D rootStart = project(model.evaluatePoint(rootLower,
				false), normalX, normalY);
		SplineOutwardInterval2D rootEnd = project(model.evaluatePoint(rootUpper,
				false), normalX, normalY);
		LocusInterval2D canonical = descriptor.getDeclaredDomain();
		ArrayList<CellState> cells = new ArrayList<>();
		int[] work = {0};
		for (int index = 0; index < DRIVER_CELL_COUNT; index++) {
			double lower = canonical.getLower()
					+ (canonical.getUpper() - canonical.getLower()) * index
							/ DRIVER_CELL_COUNT;
			double upper = index + 1 == DRIVER_CELL_COUNT
					? canonical.getUpper()
					: canonical.getLower()
						+ (canonical.getUpper() - canonical.getLower())
								* (index + 1) / DRIVER_CELL_COUNT;
			SplineOutwardInterval2D target = projectCircle(circle, normalX,
					normalY, lower, upper);
			boolean inside = strictlyBetween(target, rootStart, rootEnd,
					orientation);
			boolean remainderSafe = inside
					&& excludesSelectedOrientation(model, knots[0], rootLower,
						target, normalX, normalY, orientation, 0, work)
					&& excludesSelectedOrientation(model, rootUpper,
						knots[knots.length - 1], target, normalX, normalY,
						orientation, 0, work);
			// A half-open canonical seam is never closed merely for export. Keep the
			// final grid cell unresolved so every local component has evaluable,
			// owned endpoints and no epsilon endpoint substitution is needed.
			boolean ownsUpper = index + 1 < DRIVER_CELL_COUNT
					|| canonical.isUpperClosed();
			cells.add(new CellState(lower, upper,
					remainderSafe && ownsUpper));
		}
		return assemble(descriptor, family, evidence, rootLower, rootUpper,
				cells, work[0]);
	}

	private static LocusExistenceStructure2D assemble(
			SemanticGeneratorDescriptor1D descriptor, SelectedRootFamily family,
			LocusDefinitionEvidence source, double rootLower, double rootUpper,
			List<CellState> cells, int workCount) {
		ArrayList<Coverage> coverage = new ArrayList<>();
		ArrayList<ContinuousComponent> components = new ArrayList<>();
		int start = 0;
		while (start < cells.size()) {
			boolean certified = cells.get(start).certified;
			int end = start + 1;
			while (end < cells.size()
					&& cells.get(end).certified == certified) {
				end++;
			}
			CellState first = cells.get(start);
			CellState last = cells.get(end - 1);
			boolean lowerClosed = start == 0
					? descriptor.getDeclaredDomain().isLowerClosed() : certified;
			boolean upperClosed = end == cells.size()
					? descriptor.getDeclaredDomain().isUpperClosed() : certified;
			LocusInterval2D interval = new LocusInterval2D(first.lower,
					last.upper, lowerClosed, upperClosed);
			coverage.add(new Coverage(interval, certified
					? CoverageState.CERTIFIED_EXISTS : CoverageState.UNRESOLVED,
					certified ? METHOD : "unresolved-selected-root-transition/v1"));
			if (certified) {
				SelectedRootIntervalCertificate2D certificate =
						new SelectedRootIntervalCertificate2D(family.rootToken,
								family.selector, source.source.getSemanticRevision(),
								interval, new LocusInterval2D(rootLower, rootUpper,
										true, true), METHOD,
								"bounded-grid-boundary; adjacent unresolved cells are not crossed",
								Math.max(1, workCount));
				components.add(new ContinuousComponent(interval,
						certificate.getEvidenceKey(), certificate.getBoundaryEvidence(),
						Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
						"outward-binary64-interval/v1"));
			}
			start = end;
		}
		return new LocusExistenceStructure2D(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(), 0, coverage, components,
				Completeness.NOT_ESTABLISHED,
				"locus-existence/selected-root-hybrid/v1");
	}

	private static boolean excludesSelectedOrientation(SplineIntervalModel2D model,
			double lower, double upper, SplineOutwardInterval2D target,
			double normalX, double normalY, TransverseOrientation orientation,
			int depth, int[] work) {
		if (lower >= upper) {
			return true;
		}
		work[0]++;
		SplineOutwardInterval2D parameter =
				new SplineOutwardInterval2D(lower, upper);
		SplineOutwardInterval2D values = project(model.evaluate(parameter, false),
				normalX, normalY);
		if (values.disjoint(target)) {
			return true;
		}
		SplineOutwardInterval2D derivative = project(model.evaluate(parameter,
				true), normalX, normalY);
		if (oppositeOrientation(derivative, orientation)) {
			return true;
		}
		if (depth >= SOURCE_MAX_DEPTH) {
			return false;
		}
		double middle = lower / 2 + upper / 2;
		return middle > lower && middle < upper
				&& excludesSelectedOrientation(model, lower, middle, target,
						normalX, normalY, orientation, depth + 1, work)
				&& excludesSelectedOrientation(model, middle, upper, target,
						normalX, normalY, orientation, depth + 1, work);
	}

	private static boolean hasOrientation(SplineIntervalModel2D model,
			double lower, double upper, double normalX, double normalY,
			TransverseOrientation orientation) {
		SplineOutwardInterval2D derivative = project(model.evaluate(
				new SplineOutwardInterval2D(lower, upper), true), normalX, normalY);
		return orientation == TransverseOrientation.POSITIVE
				? derivative.lower > 0 : derivative.upper < 0;
	}

	private static boolean oppositeOrientation(
			SplineOutwardInterval2D derivative,
			TransverseOrientation orientation) {
		return orientation == TransverseOrientation.POSITIVE
				? derivative.upper < 0 : derivative.lower > 0;
	}

	private static boolean strictlyBetween(SplineOutwardInterval2D target,
			SplineOutwardInterval2D start, SplineOutwardInterval2D end,
			TransverseOrientation orientation) {
		SplineOutwardInterval2D low = orientation == TransverseOrientation.POSITIVE
				? start : end;
		SplineOutwardInterval2D high = orientation == TransverseOrientation.POSITIVE
				? end : start;
		return target.lower > low.upper && target.upper < high.lower;
	}

	private static SplineOutwardInterval2D project(
			SplineOutwardInterval2D[] point, double normalX, double normalY) {
		return point[0].multiply(SplineOutwardInterval2D.point(normalX))
				.add(point[1].multiply(SplineOutwardInterval2D.point(normalY)));
	}

	private static SplineOutwardInterval2D projectCircle(GeoConic circle,
			double normalX, double normalY, double lower, double upper) {
		double radius = circle.getCircleRadius();
		double center = normalX * circle.getTranslationVector().getX()
				+ normalY * circle.getTranslationVector().getY();
		double cosine = radius * (normalX * circle.getEigenvec(0).getX()
				+ normalY * circle.getEigenvec(0).getY());
		double sine = radius * (normalX * circle.getEigenvec(1).getX()
				+ normalY * circle.getEigenvec(1).getY());
		return SplineOutwardInterval2D.point(center)
				.add(SplineOutwardInterval2D.point(cosine)
						.multiply(trigRange(lower, upper, false)))
				.add(SplineOutwardInterval2D.point(sine)
						.multiply(trigRange(lower, upper, true)));
	}

	private static SplineOutwardInterval2D trigRange(double lower, double upper,
			boolean sine) {
		double low = Math.min(sine ? StrictMath.sin(lower) : StrictMath.cos(lower),
				sine ? StrictMath.sin(upper) : StrictMath.cos(upper));
		double high = Math.max(sine ? StrictMath.sin(lower) : StrictMath.cos(lower),
				sine ? StrictMath.sin(upper) : StrictMath.cos(upper));
		double quarter = Math.PI / 2;
		int first = (int) Math.floor(lower / quarter) - 1;
		int last = (int) Math.ceil(upper / quarter) + 1;
		for (int index = first; index <= last; index++) {
			double critical = index * quarter;
			if (critical >= lower && critical <= upper) {
				double value = sine ? StrictMath.sin(critical)
						: StrictMath.cos(critical);
				low = Math.min(low, value);
				high = Math.max(high, value);
			}
		}
		return new SplineOutwardInterval2D(Math.nextDown(low),
				Math.nextUp(high));
	}

	private static SelectedRootFamily selectedRootFamily(
			GeoPointND dependentPoint, GeoElement state) {
		AlgoElement parent = dependentPoint.toGeoElement().getParentAlgorithm();
		if (!(parent instanceof AlgoMidpoint)) {
			return null;
		}
		AlgoMidpoint midpoint = (AlgoMidpoint) parent;
		GeoElement first = midpoint.getP();
		GeoElement second = midpoint.getQ();
		GeoElement selected = first == state ? second : second == state ? first : null;
		if (selected == null
				|| !(selected.getParentAlgorithm()
						instanceof AlgoLocusIntersectionPointV2)) {
			return null;
		}
		AlgoLocusIntersectionPointV2 point =
				(AlgoLocusIntersectionPointV2) selected.getParentAlgorithm();
		GeoLocusIntersectionResult rich = point.getRichInput();
		if (!(rich.getParentAlgorithm() instanceof AlgoLocusIntersectionV2)) {
			return null;
		}
		AlgoLocusIntersectionV2 intersection =
				(AlgoLocusIntersectionV2) rich.getParentAlgorithm();
		if (!(intersection.getSource().getParentAlgorithm() instanceof AlgoSplineV2)
				|| !(intersection.getTarget() instanceof GeoLine)
				|| !(intersection.getTarget().getParentAlgorithm()
						instanceof AlgoLinePointLine)) {
			return null;
		}
		AlgoLinePointLine parallel =
				(AlgoLinePointLine) intersection.getTarget().getParentAlgorithm();
		if (parallel.getP() != state || !(parallel.getInput(1) instanceof GeoLine)) {
			return null;
		}
		String token = point.getEffectiveRootToken();
		if (token == null) {
			return null;
		}
		IntersectionRootDeterministicSelector2D selector = rich
				.getRetainedRootSelector(token).orElse(null);
		if (selector == null) {
			return null;
		}
		return new SelectedRootFamily(intersection.getSource(), rich, token,
				selector, (GeoLine) parallel.getInput(1));
	}

	private static LocusDefinitionEvidence sourceEvidence(
			SelectedRootFamily family) {
		LocusIntersectionSolution2D solution = family.rich
				.findExactPointAdmissibleSolution(family.rootToken).orElseThrow();
		String branchKey = solution.getRevisionEvidence().getBranchSnapshotKey();
		LocusBranch2D branch = family.source.getSemanticDefinition()
				.getBranch(branchKey);
		if (branch == null || branch.getValidDomainComponents().size() != 1) {
			throw new IllegalArgumentException(
					"Selected source branch lacks one complete structural component");
		}
		String lineage = IntersectionTokenLineage2D.stableComponentLineage(
				branchKey, branch.getValidDomainComponents().get(0));
		if (!lineage.equals(family.selector.getComponentLineage())) {
			throw new IllegalArgumentException(
					"Selected root lineage disagrees with its source component");
		}
		return new LocusDefinitionEvidence(family.source, branchKey,
				solution.getRevisionEvidence().getSemanticParameter());
	}

	private static int owningSpan(double[] knots, double parameter) {
		for (int index = 0; index + 1 < knots.length; index++) {
			if (parameter >= knots[index]
					&& (parameter < knots[index + 1]
							|| index + 2 == knots.length
									&& parameter == knots[index + 1])) {
				return index;
			}
		}
		return -1;
	}

	private static final class CellState {
		private final double lower;
		private final double upper;
		private final boolean certified;

		private CellState(double lower, double upper, boolean certified) {
			this.lower = lower;
			this.upper = upper;
			this.certified = certified;
		}
	}

	private static final class SelectedRootFamily {
		private final GeoLocusV2 source;
		private final GeoLocusIntersectionResult rich;
		private final String rootToken;
		private final IntersectionRootDeterministicSelector2D selector;
		private final GeoLine baseLine;

		private SelectedRootFamily(GeoLocusV2 source,
				GeoLocusIntersectionResult rich, String rootToken,
				IntersectionRootDeterministicSelector2D selector,
				GeoLine baseLine) {
			this.source = source;
			this.rich = rich;
			this.rootToken = rootToken;
			this.selector = selector;
			this.baseLine = baseLine;
		}
	}

	private static final class LocusDefinitionEvidence {
		private final GeoLocusV2 source;
		private final String branchKey;
		private final double currentRootParameter;

		private LocusDefinitionEvidence(GeoLocusV2 source, String branchKey,
				double currentRootParameter) {
			this.source = source;
			this.branchKey = branchKey;
			this.currentRootParameter = currentRootParameter;
		}
	}
}

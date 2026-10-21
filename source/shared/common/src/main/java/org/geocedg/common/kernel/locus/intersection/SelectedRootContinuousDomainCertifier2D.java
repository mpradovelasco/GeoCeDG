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
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
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
 * root driven by a point on a circle, a particular case of the hybrid existence
 * contract (PRE-G9B-CU2, decision D-CU2-9, option A).
 *
 * <p>The fixed grid partitions parameter space only. A cell is published only
 * when outward interval evaluation proves existence, uniqueness under the
 * retained selector germ and continuation throughout the whole cell. Point
 * samples never certify a cell.</p>
 *
 * <p>The certificate depends only on the revision, never on the current root:
 * every maximal monotone chart of the spline with the selected orientation is
 * considered, so that moving only the driver changes nothing (A1.1-R). The last
 * cell of the half-open periodic domain is certified like any other, and two
 * runs that meet at the seam are joined only when a lifted cell across the seam
 * is certified with the same chart.</p>
 */
public final class SelectedRootContinuousDomainCertifier2D {
	/** Contract of the certificate, a particular case of the hybrid contract. */
	public static final String CONTRACT = "locus-existence/selected-root-hybrid/v2";
	private static final int DRIVER_CELL_COUNT = 512;
	private static final int SOURCE_MAX_DEPTH = 14;
	private static final String METHOD =
			"selected-spline-parallel-line/outward-interval/v2";
	private static final int UNRESOLVED = -1;

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
				|| !descriptor.isPeriodic()
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
			return Optional.of(notEstablished(descriptor,
					"selected-root-interval-proof-refused/"
							+ exception.getClass().getSimpleName()));
		}
	}

	private static LocusExistenceStructure2D certifyFamily(
			SemanticGeneratorDescriptor1D descriptor, GeoConic circle,
			SelectedRootFamily family) {
		String branchKey = sourceBranchKey(family);
		SplineIntervalModel2D model = SplineIntervalModel2D.capture(
				family.source.getSemanticDefinition(), branchKey);
		if (model == null || !model.isStructural()
				|| family.selector.hasIntrinsicPhase()) {
			return notEstablished(descriptor,
					"selected-root-family-lacks-structural-unique-selector");
		}
		TransverseOrientation orientation = PublicTargetIntersectionCapability2D
				.currentPublicRootOrientation(
						family.selector.getCurrentRootGerm()).orElse(null);
		if (orientation == null) {
			return notEstablished(descriptor,
					"selected-root-transverse-orientation-not-established");
		}
		double normalX = family.baseLine.getX();
		double normalY = family.baseLine.getY();
		if (!Double.isFinite(normalX) || !Double.isFinite(normalY)
				|| normalX == 0 && normalY == 0) {
			throw new ArithmeticException("No finite line normal");
		}
		List<Chart> charts = charts(model, normalX, normalY, orientation);
		if (charts.isEmpty()) {
			return notEstablished(descriptor,
					"selected-root-monotone-chart-not-established");
		}
		LocusInterval2D canonical = descriptor.getDeclaredDomain();
		double period = canonical.getUpper() - canonical.getLower();
		double[] knots = model.getKnots();
		int[] work = {0};
		double[] lowers = new double[DRIVER_CELL_COUNT];
		double[] uppers = new double[DRIVER_CELL_COUNT];
		int[] owners = new int[DRIVER_CELL_COUNT];
		for (int index = 0; index < DRIVER_CELL_COUNT; index++) {
			lowers[index] = canonical.getLower()
					+ period * index / DRIVER_CELL_COUNT;
			uppers[index] = index + 1 == DRIVER_CELL_COUNT ? canonical.getUpper()
					: canonical.getLower() + period * (index + 1) / DRIVER_CELL_COUNT;
			owners[index] = owner(model, knots, charts, circle, normalX, normalY,
					orientation, lowers[index], uppers[index], work);
		}
		// Adjacent certified cells of different charts cannot both hold; neither is joined.
		for (int index = 1; index < DRIVER_CELL_COUNT; index++) {
			if (owners[index] != UNRESOLVED && owners[index - 1] != UNRESOLVED
					&& owners[index] != owners[index - 1]) {
				owners[index] = UNRESOLVED;
			}
		}
		int last = DRIVER_CELL_COUNT - 1;
		// The seam joins the last and the first cells only through a lifted cell that
		// contains it and that the same chart certifies.
		boolean seamJoined = owners[last] != UNRESOLVED && owners[last] == owners[0]
				&& owner(model, knots, charts, circle, normalX, normalY, orientation,
						lowers[last], uppers[0] + period, work) == owners[0];
		if (owners[last] != UNRESOLVED && owners[0] != UNRESOLVED && !seamJoined) {
			owners[last] = UNRESOLVED;
		}
		return assemble(descriptor, family, model, charts, lowers, uppers, owners,
				seamJoined, Math.max(1, work[0]));
	}

	/**
	 * @return every maximal run of spans whose projected derivative certifies the selected
	 *         orientation on the whole run, where the spline is smooth
	 */
	private static List<Chart> charts(SplineIntervalModel2D model, double normalX,
			double normalY, TransverseOrientation orientation) {
		double[] knots = model.getKnots();
		List<Chart> charts = new ArrayList<>();
		int span = 0;
		while (span + 1 < knots.length) {
			if (!hasOrientation(model, knots[span], knots[span + 1], normalX, normalY,
					orientation)) {
				span++;
				continue;
			}
			int first = span;
			while (span + 2 < knots.length && hasOrientation(model, knots[span + 1],
					knots[span + 2], normalX, normalY, orientation)) {
				span++;
			}
			double lower = knots[first];
			double upper = knots[span + 1];
			if (hasOrientation(model, lower, upper, normalX, normalY, orientation)
					&& model.isSmooth(new SplineOutwardInterval2D(lower, upper))) {
				charts.add(new Chart(lower, upper,
						project(model.evaluatePoint(lower, false), normalX, normalY),
						project(model.evaluatePoint(upper, false), normalX, normalY)));
			}
			span++;
		}
		return charts;
	}

	/**
	 * @return the chart that holds the unique root of the selected orientation for every
	 *         driver parameter of the cell, or {@link #UNRESOLVED}
	 */
	private static int owner(SplineIntervalModel2D model, double[] knots,
			List<Chart> charts, GeoConic circle, double normalX, double normalY,
			TransverseOrientation orientation, double lower, double upper, int[] work) {
		SplineOutwardInterval2D target = projectCircle(circle, normalX, normalY, lower,
				upper);
		for (int index = 0; index < charts.size(); index++) {
			Chart chart = charts.get(index);
			if (strictlyBetween(target, chart.start, chart.end, orientation)
					&& excludesSelectedOrientation(model, knots[0], chart.lower, target,
							normalX, normalY, orientation, 0, work)
					&& excludesSelectedOrientation(model, chart.upper,
							knots[knots.length - 1], target, normalX, normalY,
							orientation, 0, work)) {
				return index;
			}
		}
		return UNRESOLVED;
	}

	private static LocusExistenceStructure2D assemble(
			SemanticGeneratorDescriptor1D descriptor, SelectedRootFamily family,
			SplineIntervalModel2D model, List<Chart> charts, double[] lowers,
			double[] uppers, int[] owners, boolean seamJoined, int workCount) {
		LocusInterval2D domain = descriptor.getDeclaredDomain();
		String key = SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY;
		int count = owners.length;
		ArrayList<Coverage> coverage = new ArrayList<>();
		int start = 0;
		while (start < count) {
			boolean certified = owners[start] != UNRESOLVED;
			int end = start + 1;
			while (end < count && (owners[end] != UNRESOLVED) == certified) {
				end++;
			}
			boolean lowerClosed = start == 0 ? domain.isLowerClosed() : certified;
			boolean upperClosed = end == count ? domain.isUpperClosed() : certified;
			LocusInterval2D interval = new LocusInterval2D(lowers[start], uppers[end - 1],
					lowerClosed, upperClosed);
			coverage.add(certified
					? new Coverage(interval, CoverageState.CERTIFIED_EXISTS,
							LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE,
							LocusEvidenceNature2D.RIGOROUS_INTERVAL, METHOD)
					: new Coverage(interval, CoverageState.UNRESOLVED,
							LocusCoverageCause2D.NOT_CERTIFIED, LocusEvidenceNature2D.NONE,
							"unresolved-selected-root-transition/v1"));
			start = end;
		}
		ArrayList<ContinuousComponent> components = new ArrayList<>();
		boolean whole = seamJoined;
		for (int owner : owners) {
			whole &= owner != UNRESOLVED;
		}
		if (whole) {
			Chart chart = charts.get(owners[0]);
			double seam = domain.getLower();
			LocusBoundary2D closure = new LocusBoundary2D(
					LocusBoundary2D.Kind.PERIODIC_CLOSURE, new LocusBoundarySource2D(
							"selected-root-grid", "periodic-closure", "seam"),
					true, LocusEvidenceNature2D.RIGOROUS_INTERVAL);
			components.add(ContinuousComponent.arc(domain, seam, seam, true, closure,
					closure, Maximality.ESTABLISHED, LocusEvidenceNature2D.RIGOROUS_INTERVAL,
					evidenceKey(family, model, domain, chart, workCount)));
			return new LocusExistenceStructure2D(key, domain, DomainTopology.PERIODIC, 0,
					coverage, components, List.of(), Completeness.COMPLETE, CONTRACT);
		}
		// Runs of certified cells in cyclic order, starting after an unresolved cell.
		int first = 0;
		while (owners[first] != UNRESOLVED) {
			first++;
		}
		int offset = 1;
		while (offset <= count) {
			int index = (first + offset) % count;
			if (owners[index] == UNRESOLVED) {
				offset++;
				continue;
			}
			int runStart = index;
			int length = 1;
			while (offset + length <= count
					&& owners[(first + offset + length) % count] != UNRESOLVED) {
				length++;
			}
			int runEnd = (runStart + length - 1) % count;
			Chart chart = charts.get(owners[runStart]);
			LocusBoundary2D lower = bandEdge(true, true);
			// The domain end at the seam is open: a run that reaches it ends before it.
			boolean upperIncluded = runEnd != count - 1;
			LocusBoundary2D upper = bandEdge(false, upperIncluded);
			if (runEnd < runStart) {
				// The run crosses the seam, which the lifted seam cell certified.
				components.add(ContinuousComponent.arc(domain, lowers[runStart],
						uppers[runEnd], false, lower, upper,
						Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
						LocusEvidenceNature2D.RIGOROUS_INTERVAL,
						evidenceKey(family, model, new LocusInterval2D(lowers[runStart],
								uppers[runEnd] + (domain.getUpper() - domain.getLower()),
								true, true), chart, workCount)));
			} else {
				LocusInterval2D interval = new LocusInterval2D(lowers[runStart],
						uppers[runEnd], true, upperIncluded);
				components.add(ContinuousComponent.interval(interval, lower, upper,
						Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
						LocusEvidenceNature2D.RIGOROUS_INTERVAL,
						evidenceKey(family, model, interval, chart, workCount)));
			}
			offset += length;
		}
		return new LocusExistenceStructure2D(key, domain, DomainTopology.PERIODIC, 0,
				coverage, components, List.of(), Completeness.NOT_ESTABLISHED, CONTRACT);
	}

	private static String evidenceKey(SelectedRootFamily family,
			SplineIntervalModel2D model, LocusInterval2D driver, Chart chart,
			int workCount) {
		return new SelectedRootIntervalCertificate2D(family.rootToken, family.selector,
				family.source.getSemanticRevision(), driver,
				new LocusInterval2D(chart.lower, chart.upper, true, true), METHOD,
				"bounded-grid-boundary; adjacent unresolved cells are not crossed",
				workCount).getEvidenceKey();
	}

	/** @return the end of a run of certified cells at the edge of an unresolved band */
	private static LocusBoundary2D bandEdge(boolean lower, boolean included) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
				new LocusBoundarySource2D("selected-root-grid", "unresolved-cell",
						lower ? "lower" : "upper"), included, LocusEvidenceNature2D.NONE);
	}

	private static LocusExistenceStructure2D notEstablished(
			SemanticGeneratorDescriptor1D descriptor, String evidence) {
		return LocusExistenceStructure2D.notEstablished(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(), DomainTopology.PERIODIC,
				LocusCoverageCause2D.NOT_CERTIFIED, evidence);
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
		return new SelectedRootFamily(intersection.getSource(), token, selector,
				(GeoLine) parallel.getInput(1));
	}

	/**
	 * The source branch that the retained selector cites, independent of the current root
	 * (A1.1-R): its one complete component carries the selector's lineage key, or equals the
	 * whole declared domain that an earlier key cites (E-3, equality of the decomposition).
	 */
	private static String sourceBranchKey(SelectedRootFamily family) {
		LocusDefinition2D definition = family.source.getSemanticDefinition();
		if (definition != null) {
			for (LocusBranch2D branch : definition.getBranches()) {
				LocusExistenceStructure2D structure = branch.getExistenceStructure();
				if (structure.getCompleteness() != Completeness.COMPLETE
						|| structure.getContinuousValidComponents().size() != 1
						|| !structure.getPointComponents().isEmpty()) {
					continue;
				}
				String retained = family.selector.getComponentLineage();
				if (retained.equals(LocusComponentLineage2D.of(structure,
						structure.getContinuousValidComponents().get(0)))
						|| LocusComponentSelectorResolver2D.citesWholeDomain(retained,
								structure)) {
					return branch.getBranchKey();
				}
			}
		}
		throw new IllegalArgumentException(
				"Selected source branch lacks one complete structural component");
	}

	/** A maximal monotone chart of the spline with its projected end values. */
	private static final class Chart {
		private final double lower;
		private final double upper;
		private final SplineOutwardInterval2D start;
		private final SplineOutwardInterval2D end;

		private Chart(double lower, double upper, SplineOutwardInterval2D start,
				SplineOutwardInterval2D end) {
			this.lower = lower;
			this.upper = upper;
			this.start = start;
			this.end = end;
		}
	}

	private static final class SelectedRootFamily {
		private final GeoLocusV2 source;
		private final String rootToken;
		private final IntersectionRootDeterministicSelector2D selector;
		private final GeoLine baseLine;

		private SelectedRootFamily(GeoLocusV2 source, String rootToken,
				IntersectionRootDeterministicSelector2D selector, GeoLine baseLine) {
			this.source = source;
			this.rootToken = rootToken;
			this.selector = selector;
			this.baseLine = baseLine;
		}
	}
}

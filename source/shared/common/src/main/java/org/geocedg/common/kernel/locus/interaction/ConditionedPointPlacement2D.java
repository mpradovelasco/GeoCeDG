/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;

/**
 * Inverse query of a world request on the conditioned regions of a Locus V2 (ADR 0042;
 * contract {@code post-r1-conditioned-use.md}, sections 6.1 and 8.9). It proposes parameter
 * addresses: one for each stretch of a region whose node polyline passes within the request
 * radius, at the parameter that the producer's own evaluator places nearest to the request.
 *
 * <p>The node polyline only says where to evaluate; every candidate is a semantic
 * evaluation of the producer at its parameter, valid and with established selections. The
 * candidates are the ones found on the published regions: nothing here states that they
 * are all the preimages, and a request with several candidates is never resolved by
 * proximity. A sample, a cell or a node is never an address.</p>
 */
public final class ConditionedPointPlacement2D {
	private static final double GOLDEN = (Math.sqrt(5) - 1) / 2;
	private static final int REFINEMENTS = 32;

	private ConditionedPointPlacement2D() {
		// Utility class.
	}

	/**
	 * @param source locus of the request
	 * @param x abscissa of the request
	 * @param y ordinate of the request
	 * @param policy interaction policy: request radius and candidate limit
	 * @return candidate parameter addresses in branch and parameter order; empty when the
	 *         source publishes no conditioned region near the request
	 */
	public static List<LocusSemanticAddress2D> candidates(GeoLocusV2 source, double x,
			double y, LocusPointInteractionPolicy2D policy) {
		List<LocusSemanticAddress2D> found = new ArrayList<>();
		LocusDefinition2D definition = source == null || !source.isDefined() ? null
				: source.getSemanticDefinition();
		double radius = policy.getMaximumWorldDistance();
		if (definition == null || source.getPersistentLocusId() == null
				|| definition.getDefinitionStatus() != DefinitionStatus.VALID
				|| !Double.isFinite(x) || !Double.isFinite(y) || !(radius > 0)) {
			return found;
		}
		int limit = policy.getWorkBudget().getMaximumCandidates();
		LocusDriverDomainProvider2D provider = definition.getProvider();
		for (LocusBranch2D branch : definition.getBranches()) {
			LocusConditionedEvidence2D evidence = source.getConditionedEvidence(
					branch.getBranchKey());
			if (evidence == null) {
				continue;
			}
			LocusConditionedCell2D best = null;
			double bestDistance = Double.POSITIVE_INFINITY;
			LocusConditionedCell2D previous = null;
			List<LocusConditionedCell2D> cells = new ArrayList<>(evidence.getCells());
			cells.add(null);
			for (LocusConditionedCell2D cell : cells) {
				double distance = cell == null || !cell.getKind().isSpan()
						? Double.POSITIVE_INFINITY : polylineDistance(cell, x, y);
				boolean near = distance <= radius + (cell == null ? 0
						: cell.getChordDeviation());
				boolean adjacent = near && previous != null
						&& previous.getUpperIndex() == cell.getLowerIndex();
				if (best != null && !adjacent) {
					// The stretch ended: one candidate, at its nearest cell.
					LocusSemanticAddress2D address = refine(source, definition, provider,
							branch, evidence, best, x, y, radius);
					if (address != null && !found.contains(address)) {
						found.add(address);
						if (found.size() >= limit) {
							return found;
						}
					}
					best = null;
					bestDistance = Double.POSITIVE_INFINITY;
				}
				if (near && distance < bestDistance) {
					best = cell;
					bestDistance = distance;
				}
				previous = near ? cell : null;
			}
		}
		return found;
	}

	private static double polylineDistance(LocusConditionedCell2D cell, double x, double y) {
		double[] nodes = cell.getNodePoints();
		if (nodes == null) {
			return Double.POSITIVE_INFINITY;
		}
		return Math.min(segmentDistance(x, y, nodes[0], nodes[1], nodes[2], nodes[3]),
				segmentDistance(x, y, nodes[2], nodes[3], nodes[4], nodes[5]));
	}

	private static double segmentDistance(double px, double py, double ax, double ay,
			double bx, double by) {
		double dx = bx - ax;
		double dy = by - ay;
		double squared = dx * dx + dy * dy;
		double t = squared == 0 ? 0 : Math.max(0, Math.min(1,
				((px - ax) * dx + (py - ay) * dy) / squared));
		return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
	}

	/**
	 * The parameter of one cell that the producer's evaluator places nearest to the
	 * request, by a bounded section search between the ends of the cell.
	 */
	private static LocusSemanticAddress2D refine(GeoLocusV2 source,
			LocusDefinition2D definition, LocusDriverDomainProvider2D provider,
			LocusBranch2D branch, LocusConditionedEvidence2D evidence,
			LocusConditionedCell2D cell, double x, double y, double radius) {
		String key = branch.getBranchKey();
		double low = cell.getLower();
		double high = cell.getUpper();
		double first = high - GOLDEN * (high - low);
		double second = low + GOLDEN * (high - low);
		double firstDistance = distance(source, key, first, x, y);
		double secondDistance = distance(source, key, second, x, y);
		for (int step = 0; step < REFINEMENTS && high - low > Math.ulp(high) * 4; step++) {
			if (firstDistance <= secondDistance) {
				high = second;
				second = first;
				secondDistance = firstDistance;
				first = high - GOLDEN * (high - low);
				firstDistance = distance(source, key, first, x, y);
			} else {
				low = first;
				first = second;
				firstDistance = secondDistance;
				second = low + GOLDEN * (high - low);
				secondDistance = distance(source, key, second, x, y);
			}
		}
		double parameter = firstDistance <= secondDistance ? first : second;
		// The ends of the cell are evaluated nodes: an end may be the nearest parameter.
		for (double end : new double[] {cell.getLower(), cell.getUpper()}) {
			if (distance(source, key, end, x, y) < distance(source, key, parameter, x, y)) {
				parameter = end;
			}
		}
		// The provider's modular arithmetic may need its normal stabilization pass; the
		// address is the canonical parameter, and it is the one evaluated.
		double canonical = provider.canonicalize(provider.canonicalize(parameter));
		if (!Double.isFinite(canonical)
				|| Double.doubleToLongBits(canonical) != Double.doubleToLongBits(
						provider.canonicalize(canonical))
				|| !(distance(source, key, canonical, x, y) <= radius)
				|| evidence.regionOf(canonical) == null) {
			return null;
		}
		LocusInterval2D domain = branch.getDeclaredDriverDomain();
		SeamSide seam = !provider.isPeriodic() ? SeamSide.NOT_PERIODIC
				: canonical == domain.getLower() ? SeamSide.LOWER_APPROACH : SeamSide.INTERIOR;
		return LocusSemanticAddress2D.byParameter(source.getPersistentLocusId(),
				provider.getProviderId(), key, canonical, 0, seam);
	}

	private static double distance(GeoLocusV2 source, String branchKey, double parameter,
			double x, double y) {
		LocusConditionedSample2D sample = source.sampleConditioned(branchKey, parameter);
		return sample == null || !sample.isValid() || !sample.hasEstablishedSelections()
				? Double.POSITIVE_INFINITY : Math.hypot(sample.getX() - x, sample.getY() - y);
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.Objects;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.kernel.arithmetic.Function;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.implicit.GeoImplicit;
import org.geogebra.common.kernel.kernelND.GeoConicND;

/** Test-private numerical and identity probes for G8C design only. */
final class G8CCharacterizationSupport {
	private G8CCharacterizationSupport() {
	}

	enum Completeness {
		COMPLETE,
		INCOMPLETE,
		NOT_ESTABLISHED
	}

	enum LocalIsolation {
		ESTABLISHED,
		NOT_ESTABLISHED
	}

	enum OverlapStatus {
		OVERLAP_ESTABLISHED,
		OVERLAP_SUSPECTED_NOT_ESTABLISHED,
		UNSUPPORTED_OVERLAP
	}

	enum ContinuationStatus {
		ESTABLISHED,
		AMBIGUOUS,
		IDENTITY_DISCONTINUITY
	}

	static double conicRawResidual(GeoConicND conic, LocusPoint2D point) {
		return GeoConicND.evaluate(conic.getFlatMatrix(), point.getX(),
				point.getY());
	}

	static OptionalDouble conicFirstOrderNormalResidual(GeoConicND conic,
			LocusPoint2D point) {
		double[] matrix = conic.getFlatMatrix();
		double gradientX = 2 * matrix[0] * point.getX()
				+ 2 * matrix[3] * point.getY() + 2 * matrix[4];
		double gradientY = 2 * matrix[1] * point.getY()
				+ 2 * matrix[3] * point.getX() + 2 * matrix[5];
		return dividedByRegularGradient(conicRawResidual(conic, point),
				gradientX, gradientY);
	}

	static OptionalDouble conicContactIndicator(GeoConicND conic,
			LocusPoint2D point, LocusPoint2D sourceDerivative) {
		double[] matrix = conic.getFlatMatrix();
		double gradientX = 2 * matrix[0] * point.getX()
				+ 2 * matrix[3] * point.getY() + 2 * matrix[4];
		double gradientY = 2 * matrix[1] * point.getY()
				+ 2 * matrix[3] * point.getX() + 2 * matrix[5];
		return normalizedDot(gradientX, gradientY, sourceDerivative.getX(),
				sourceDerivative.getY());
	}

	static double functionVerticalResidual(GeoFunction function,
			LocusPoint2D point) {
		return point.getY() - function.value(point.getX());
	}

	static OptionalDouble functionFirstOrderNormalResidual(GeoFunction function,
			LocusPoint2D point) {
		Function derivative = function.getFunction().getDerivative(1, true);
		double slope = derivative.value(point.getX());
		if (!Double.isFinite(slope)) {
			return OptionalDouble.empty();
		}
		double residual = functionVerticalResidual(function, point);
		if (!Double.isFinite(residual)) {
			return OptionalDouble.empty();
		}
		return OptionalDouble.of(residual / Math.hypot(1, slope));
	}

	static double implicitRawResidual(GeoImplicit implicit,
			LocusPoint2D point) {
		return implicit.evaluateImplicitCurve(point.getX(), point.getY());
	}

	static OptionalDouble implicitFirstOrderNormalResidual(GeoImplicit implicit,
			LocusPoint2D point) {
		return dividedByRegularGradient(implicitRawResidual(implicit, point),
				implicit.derivativeX(point.getX(), point.getY()),
				implicit.derivativeY(point.getX(), point.getY()));
	}

	static double normalizedTangentDeterminant(LocusPoint2D first,
			LocusPoint2D second) {
		double firstSpeed = Math.hypot(first.getX(), first.getY());
		double secondSpeed = Math.hypot(second.getX(), second.getY());
		if (firstSpeed == 0 || secondSpeed == 0) {
			throw new IllegalArgumentException("Regular tangents are required");
		}
		return (first.getX() * second.getY()
				- first.getY() * second.getX()) / (firstSpeed * secondSpeed);
	}

	static String unorderedSourcePair(String firstIdentity,
			String secondIdentity) {
		String first = requireText(firstIdentity);
		String second = requireText(secondIdentity);
		return first.compareTo(second) <= 0 ? first + "||" + second
				: second + "||" + first;
	}

	static String durablePairToken(String firstIdentity, String secondIdentity,
			String constructiveLineage, String topologyContext) {
		return unorderedSourcePair(firstIdentity, secondIdentity) + "::"
				+ requireText(constructiveLineage) + "::"
				+ requireText(topologyContext);
	}

	static PairEvidence pairEvidence(String firstIdentity, long firstRevision,
			String firstBranch, String firstComponent, double firstParameter,
			String secondIdentity, long secondRevision, String secondBranch,
			String secondComponent, double secondParameter,
			LocusPoint2D firstPoint, LocusPoint2D secondPoint,
			LocusPoint2D firstDerivative, LocusPoint2D secondDerivative,
			int rootsInIsolatingRectangle, String constructiveLineage,
			String topologyContext) {
		double residual = Math.hypot(firstPoint.getX() - secondPoint.getX(),
				firstPoint.getY() - secondPoint.getY());
		double determinant = normalizedTangentDeterminant(firstDerivative,
				secondDerivative);
		LocalIsolation isolation = rootsInIsolatingRectangle == 1
				&& Math.abs(determinant) > 1E-12 ? LocalIsolation.ESTABLISHED
						: LocalIsolation.NOT_ESTABLISHED;
		return new PairEvidence(durablePairToken(firstIdentity, secondIdentity,
				constructiveLineage, topologyContext), firstIdentity, firstRevision,
				firstBranch, firstComponent, firstParameter, secondIdentity,
				secondRevision, secondBranch, secondComponent, secondParameter,
				residual, determinant, isolation);
	}

	static boolean pointAdmissible(boolean current, boolean verified,
			LocalIsolation localIsolation, ContinuationStatus continuation,
			Completeness completeness) {
		Objects.requireNonNull(completeness);
		return current && verified && localIsolation == LocalIsolation.ESTABLISHED
				&& continuation == ContinuationStatus.ESTABLISHED;
	}

	static OverlapStatus overlapStatus(boolean explicitParameterMap,
			boolean evaluatorContractSupportsMap, int matchingSamples) {
		if (explicitParameterMap && evaluatorContractSupportsMap) {
			return OverlapStatus.OVERLAP_ESTABLISHED;
		}
		if (matchingSamples > 0) {
			return OverlapStatus.OVERLAP_SUSPECTED_NOT_ESTABLISHED;
		}
		return OverlapStatus.UNSUPPORTED_OVERLAP;
	}

	static BoxRun characterizeLinearPairBoxes(int subdivisions) {
		if (subdivisions < 1) {
			throw new IllegalArgumentException("Positive subdivisions required");
		}
		long visited = 0;
		long candidates = 0;
		for (int first = 0; first < subdivisions; first++) {
			double firstLow = (double) first / subdivisions;
			double firstHigh = (double) (first + 1) / subdivisions;
			for (int second = 0; second < subdivisions; second++) {
				double secondLow = (double) second / subdivisions;
				double secondHigh = (double) (second + 1) / subdivisions;
				visited++;
				boolean xOverlap = intervalsOverlap(firstLow, firstHigh,
						secondLow, secondHigh);
				boolean yOverlap = intervalsOverlap(firstLow, firstHigh,
						1 - secondHigh, 1 - secondLow);
				if (xOverlap && yOverlap) {
					candidates++;
				}
			}
		}
		return new BoxRun(visited, visited - candidates, candidates,
				candidates, 0, 0, 0, 0, 0, 0);
	}

	static long componentPairCount(int[] firstBranchComponents,
			int[] secondBranchComponents) {
		long total = 0;
		for (int first : firstBranchComponents) {
			for (int second : secondBranchComponents) {
				total += (long) first * second;
			}
		}
		return total;
	}

	private static OptionalDouble dividedByRegularGradient(double residual,
			double gradientX, double gradientY) {
		double norm = Math.hypot(gradientX, gradientY);
		if (!Double.isFinite(residual) || !Double.isFinite(norm) || norm == 0) {
			return OptionalDouble.empty();
		}
		return OptionalDouble.of(residual / norm);
	}

	private static OptionalDouble normalizedDot(double firstX, double firstY,
			double secondX, double secondY) {
		double firstNorm = Math.hypot(firstX, firstY);
		double secondNorm = Math.hypot(secondX, secondY);
		if (!Double.isFinite(firstNorm) || !Double.isFinite(secondNorm)
				|| firstNorm == 0 || secondNorm == 0) {
			return OptionalDouble.empty();
		}
		return OptionalDouble.of((firstX * secondX + firstY * secondY)
				/ (firstNorm * secondNorm));
	}

	private static boolean intervalsOverlap(double firstLow, double firstHigh,
			double secondLow, double secondHigh) {
		return firstLow <= secondHigh && secondLow <= firstHigh;
	}

	private static String requireText(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Stable text is required");
		}
		return value;
	}

	static final class PairEvidence {
		private final String durableToken;
		private final String firstIdentity;
		private final long firstRevision;
		private final String firstBranch;
		private final String firstComponent;
		private final double firstParameter;
		private final String secondIdentity;
		private final long secondRevision;
		private final String secondBranch;
		private final String secondComponent;
		private final double secondParameter;
		private final double coordinateResidual;
		private final double normalizedDeterminant;
		private final LocalIsolation localIsolation;

		PairEvidence(String durableToken, String firstIdentity,
				long firstRevision, String firstBranch, String firstComponent,
				double firstParameter, String secondIdentity, long secondRevision,
				String secondBranch, String secondComponent,
				double secondParameter, double coordinateResidual,
				double normalizedDeterminant, LocalIsolation localIsolation) {
			this.durableToken = durableToken;
			this.firstIdentity = firstIdentity;
			this.firstRevision = firstRevision;
			this.firstBranch = firstBranch;
			this.firstComponent = firstComponent;
			this.firstParameter = firstParameter;
			this.secondIdentity = secondIdentity;
			this.secondRevision = secondRevision;
			this.secondBranch = secondBranch;
			this.secondComponent = secondComponent;
			this.secondParameter = secondParameter;
			this.coordinateResidual = coordinateResidual;
			this.normalizedDeterminant = normalizedDeterminant;
			this.localIsolation = localIsolation;
		}

		PairEvidence reversed() {
			return new PairEvidence(durableToken, secondIdentity, secondRevision,
					secondBranch, secondComponent, secondParameter, firstIdentity,
					firstRevision, firstBranch, firstComponent, firstParameter,
					coordinateResidual, -normalizedDeterminant, localIsolation);
		}

		String durableToken() {
			return durableToken;
		}

		String firstIdentity() {
			return firstIdentity;
		}

		double firstParameter() {
			return firstParameter;
		}

		String secondIdentity() {
			return secondIdentity;
		}

		double secondParameter() {
			return secondParameter;
		}

		double coordinateResidual() {
			return coordinateResidual;
		}

		double normalizedDeterminant() {
			return normalizedDeterminant;
		}

		LocalIsolation localIsolation() {
			return localIsolation;
		}
	}

	static final class BoxRun {
		private final long boxesVisited;
		private final long boxesRejected;
		private final long candidateBoxes;
		private final long pairRefinements;
		private final long retainedEntries;
		private final long renderReads;
		private final long legacySampleReads;
		private final long viewportReads;
		private final long metricIndexReads;
		private final long wholeLocusRegenerations;

		BoxRun(long boxesVisited, long boxesRejected, long candidateBoxes,
				long pairRefinements, long retainedEntries, long renderReads,
				long legacySampleReads, long viewportReads, long metricIndexReads,
				long wholeLocusRegenerations) {
			this.boxesVisited = boxesVisited;
			this.boxesRejected = boxesRejected;
			this.candidateBoxes = candidateBoxes;
			this.pairRefinements = pairRefinements;
			this.retainedEntries = retainedEntries;
			this.renderReads = renderReads;
			this.legacySampleReads = legacySampleReads;
			this.viewportReads = viewportReads;
			this.metricIndexReads = metricIndexReads;
			this.wholeLocusRegenerations = wholeLocusRegenerations;
		}

		long boxesVisited() {
			return boxesVisited;
		}

		long boxesRejected() {
			return boxesRejected;
		}

		long candidateBoxes() {
			return candidateBoxes;
		}

		long pairRefinements() {
			return pairRefinements;
		}

		long retainedEntries() {
			return retainedEntries;
		}

		boolean hasForbiddenAuthorityReads() {
			return renderReads != 0 || legacySampleReads != 0 || viewportReads != 0
					|| metricIndexReads != 0 || wholeLocusRegenerations != 0;
		}
	}
}

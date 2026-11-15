/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.kernel.locus;

import java.util.Optional;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.plugin.Operation;

/** Narrow constructive proof that an oriented circle length cancels in a rotation. */
final class ClosedMetricRotationCertificate2D {

	private static final String EVIDENCE = "circle-length-over-own-radius-periodicity/v1";

	private ClosedMetricRotationCertificate2D() {
	}

	static Optional<LocusExistenceStructure2D> capture(GeoElement traced, GeoElement state,
			GeoElement support, SemanticGeneratorDescriptor1D descriptor) {
		if (descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCLE_POINT
				|| AuthorityPolicy.of(traced.getConstruction()).isConstructive()
				|| !(support instanceof GeoConic circle)
				|| circle.getType() != GeoConicNDConstants.CONIC_CIRCLE
				|| !(circle.getParentAlgorithm() instanceof AlgoCirclePointRadius constructor)
				|| !(constructor.getInput(1) instanceof GeoNumeric radius)
				|| !hasCapturedRadiusAndFrame(circle, radius)
				|| !(traced instanceof GeoPoint)
				|| !(traced.getParentAlgorithm() instanceof AlgoRotatePoint rotate)
				|| rotate.getClass() != AlgoRotatePoint.class || rotate.getOutput(0) != traced
				|| !isFixedFinitePoint(rotate.getInput(0).toGeoElement(), state)
				|| !isFixedFinitePoint(rotate.getInput(2).toGeoElement(), state)) {
			return Optional.empty();
		}
		AlgoLocusBetweenMetricV2 metric = findMetric(rotate.getInput(1).toGeoElement(), radius);
		if (metric == null || !(metric.getInput(0) instanceof GeoLocusV2 locus)
				|| !(locus.getParentAlgorithm() instanceof AlgoDependentPointLocusV2 source)
				|| source.getInput(1) != state || source.getInput(0) != metric.getInput(2)
				|| !isFixedTranslation(source.getInput(0).toGeoElement(), state)) {
			return Optional.empty();
		}
		var sourceCertificate = source.getHybridExistenceCertificate();
		if (sourceCertificate == null || !sourceCertificate.getStructure().isClosedPeriodic()
				|| !descriptor.getDeclaredDomain().equals(
						sourceCertificate.getStructure().getCanonicalDomain())) {
			return Optional.empty();
		}
		LocusMetricFunctionStructure2D metricStructure = metric.getFunctionStructure();
		if (!metricStructure.isCertified() || metricStructure.getEvents().size() != 1
				|| metricStructure.getPieces().size() != 1
				|| metricStructure.getPieces().get(0).getState()
						!= LocusMetricFunctionStructure2D.State.DEFINED
				|| !descriptor.getDeclaredDomain().equals(
						metricStructure.getPieces().get(0).getInterval())) {
			return Optional.empty();
		}
		// The driver is a circle of radius r and translation is an isometry. The two
		// lateral metric limits thus differ by one full arc 2πr. The divisor is that very
		// radius input, so the rotation actions agree for both limits and the origin value.
		// All operands are otherwise continuous and defined. No metric number is read here.
		return Optional.of(LocusExistenceStructure2D.composedThroughTotalSteps(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY, sourceCertificate.getStructure(),
				HybridExistenceCertifier2D.CONTRACT, EVIDENCE));
	}

	private static boolean hasCapturedRadiusAndFrame(GeoConic circle, GeoNumeric radius) {
		double value = radius.getDouble();
		return circle.isDefined() && radius.isDefined() && Double.isFinite(value) && value > 0
				&& circle.getHalfAxis(0) == value && circle.getHalfAxis(1) == value
				&& circle.getEigenvec(0).getX() == 1 && circle.getEigenvec(0).getY() == 0
				&& circle.getEigenvec(1).getX() == 0 && circle.getEigenvec(1).getY() == 1;
	}

	private static boolean isFixedFinitePoint(GeoElement point, GeoElement state) {
		return point instanceof GeoPoint p && p.isFinite() && point != state && !point.isChildOf(state);
	}

	private static boolean isFixedTranslation(GeoElement trace, GeoElement state) {
		return trace instanceof GeoPoint && trace.getParentAlgorithm() instanceof AlgoTranslate translate
				&& translate.getClass() == AlgoTranslate.class && translate.getOutput(0) == trace
				&& translate.getInput(0) == state && translate.getInput(1) instanceof GeoVector vector
				&& vector.isDefined() && Double.isFinite(vector.getX()) && Double.isFinite(vector.getY())
				&& !vector.isChildOf(state);
	}

	private static AlgoLocusBetweenMetricV2 findMetric(GeoElement angle, GeoNumeric radius) {
		if (!(angle.getParentAlgorithm() instanceof AlgoDependentNumber quotient)
				|| quotient.getClass() != AlgoDependentNumber.class || quotient.getOutput(0) != angle) {
			return null;
		}
		ExpressionNode expression = quotient.getExpression();
		if (expression.getOperation() != Operation.DIVIDE || expression.getRight().unwrap() != radius
				|| !(expression.getLeft().unwrap() instanceof GeoNumeric scalar)
				|| !(scalar.getParentAlgorithm() instanceof AlgoLocusMetricScalarAdapter adapter)
				|| !(adapter.getRichInput().getParentAlgorithm() instanceof AlgoLocusBetweenMetricV2 between)) {
			return null;
		}
		return between;
	}
}

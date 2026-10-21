/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedStationaryPoint;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedStationaryPoint.StationaryKind;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Policy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T5: certified stationary points of the traced map
 * ({@code locus-v2-certified-construction-model.md} section 10.7; certification design
 * section 5). A fold needs five separate proofs: an exact structural symmetry verified on
 * the captured program, isolation and non-degeneracy by an enclosure of one component of
 * {@code F''}, interiority in a certified component, and opposite one-sided tangents. Matrix
 * rows V-FOLD-1 to V-FOLD-4 on synthetic constructions; the author model is step T7.
 */
class PreG9BCu2FoldCertificationTest extends BaseUnitTest {
	private static final double PERIOD = 2 * Math.PI;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-FOLD-1 (synthetic part): a symmetric locus folds at both fixed points of the
	 * reflection in the horizontal mirror, with an isolation box where {@code x''} excludes
	 * zero and keeps the sign of {@code -2 cos t0}; the turn exemption is limited to that box.
	 */
	@Test
	void aSymmetricLocusFoldsAtTheFixedPoints() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("lx=LocusV2(X,N)");
		Result result = certificate("lx");
		assertEquals(Completeness.COMPLETE, result.getStructure().getCompleteness());
		List<CertifiedStationaryPoint> points = result.getStationaryPoints();
		assertEquals(2, points.size(), points.toString());
		CertifiedStationaryPoint seam = points.get(0);
		CertifiedStationaryPoint zero = points.get(1);
		assertFold(seam, -Math.nextUp(Math.PI), -Math.PI, "HORIZONTAL", 0, 1);
		assertFold(zero, 0, 0, "HORIZONTAL", 0, -1);
		assertTrue(seam.getIsolationLower() < Math.PI && Math.PI < seam.getIsolationUpper());
		assertTrue(result.getExistenceBoxes() <= Policy.DEFAULT.getBoxBudget());
	}

	/**
	 * V-FOLD-1 and V-FOLD-2 (synthetic parts): a circle centred on the mirror through the
	 * driver, as in {@code e_1}, keeps the symmetry by exact equality of the captured centre
	 * with the driver centre; moved {@code 2^-40} off the mirror, it has no symmetry and no
	 * stationary point is certified, although the locus exists everywhere.
	 */
	@Test
	void theSymmetryIsVerifiedOnTheCapturedConstants() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("d=Circle((5,0),N)");
		add("H=Intersect(d,Line((0,0),(1,0)),1)");
		add("lh=LocusV2(H,N)");
		add("dd=Circle((5,2^(-40)),N)");
		add("HH=Intersect(dd,Line((0,0),(1,0)),1)");
		add("lhh=LocusV2(HH,N)");
		Result symmetric = certificate("lh");
		assertEquals(2, symmetric.getStationaryPoints().size());
		for (CertifiedStationaryPoint point : symmetric.getStationaryPoints()) {
			assertEquals(StationaryKind.FOLD, point.getKind());
			assertEquals(0, point.getAxis());
		}
		Result shifted = certificate("lhh");
		assertEquals(Completeness.COMPLETE, shifted.getStructure().getCompleteness());
		assertTrue(shifted.getStationaryPoints().isEmpty());
	}

	/**
	 * V-FOLD-3 (synthetic part): a closed turn is not a fold: the parallel through the
	 * driver to a slanted line turns back where {@code tan t = -1/2}, but no symmetry is
	 * verified, so no stationary point and no exemption is certified.
	 */
	@Test
	void aClosedTurnWithoutSymmetryIsNotAFold() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X=Intersect(Line(N,Line((0,0),(1,2))),Line((0,3),(1,3)))");
		add("lx=LocusV2(X,N)");
		Result result = certificate("lx");
		assertEquals(Completeness.COMPLETE, result.getStructure().getCompleteness());
		assertTrue(result.getStationaryPoints().isEmpty());
	}

	/**
	 * V-FOLD-4: a symmetric locus with {@code F''(t0) = 0} at one fixed point: the
	 * stationary point is proven by the symmetry but is never certified as a non-degenerate
	 * fold, while the other fixed point is a fold. Here {@code x(t) = cos t - cos^2 t / 2}.
	 */
	@Test
	void aDegenerateStationaryPointIsNotAFold() {
		add("c=Circle((0,0),1)");
		add("N=Point(c)");
		add("v=Line(N,Line((0,0),(0,1)))");
		add("P1=Intersect(v,Line((0,0),(1,0)))");
		add("P2=Rotate(P1,pi/2,(0,0))");
		add("h2=PerpendicularLine(P2,Line((0,0),(0,1)))");
		add("P3=Intersect(h2,Line((1,0),(1,1)))");
		add("Q=Intersect(Line((0,0),P3),v)");
		add("X=Intersect(Line(Q,Line((0,0),(1,2))),Line((0,0),(1,0)))");
		add("lx=LocusV2(X,N)");
		List<CertifiedStationaryPoint> points = certificate("lx").getStationaryPoints();
		assertEquals(2, points.size(), points.toString());
		CertifiedStationaryPoint seam = points.get(0);
		CertifiedStationaryPoint zero = points.get(1);
		assertFold(seam, -Math.nextUp(Math.PI), -Math.PI, "HORIZONTAL", 0, 1);
		assertEquals(StationaryKind.DEGENERATE, zero.getKind());
		assertEquals(0, zero.getParameterLower());
		assertEquals(-1, zero.getAxis());
		assertTrue(Double.isNaN(zero.getIsolationLower()));
		assertNull(zero.getSecondDerivative());
		assertEquals(LocusEvidenceNature2D.EXACT, zero.getNature());
	}

	/**
	 * PRE-G9B-CU2 step T10 (V-B06-2, certificate part): the existence structure publishes the
	 * non-degenerate folds with their isolation bands, the only bands where a drawing may waive
	 * its turning criterion; the degenerate stationary point is never published as a fold. The
	 * band of the fold at the seam holds the canonical parameters on both sides of it, modulo
	 * the period, and nothing far from it.
	 */
	@Test
	void theStructurePublishesOnlyTheNonDegenerateFolds() {
		add("c=Circle((0,0),1)");
		add("N=Point(c)");
		add("v=Line(N,Line((0,0),(0,1)))");
		add("P1=Intersect(v,Line((0,0),(1,0)))");
		add("P2=Rotate(P1,pi/2,(0,0))");
		add("h2=PerpendicularLine(P2,Line((0,0),(0,1)))");
		add("P3=Intersect(h2,Line((1,0),(1,1)))");
		add("Q=Intersect(Line((0,0),P3),v)");
		add("X=Intersect(Line(Q,Line((0,0),(1,2))),Line((0,0),(1,0)))");
		add("lx=LocusV2(X,N)");
		Result result = certificate("lx");
		List<LocusCertifiedFold2D> folds = result.getStructure().getCertifiedFolds();
		assertEquals(1, folds.size(), folds.toString());
		CertifiedStationaryPoint seam = result.getStationaryPoints().get(0);
		LocusCertifiedFold2D fold = folds.get(0);
		assertEquals(seam.getParameterLower(), fold.getParameterLower());
		assertEquals(seam.getParameterUpper(), fold.getParameterUpper());
		assertEquals(seam.getIsolationLower(), fold.getBandLower());
		assertEquals(seam.getIsolationUpper(), fold.getBandUpper());
		assertEquals(seam.getSymmetry(), fold.getSymmetry());
		assertEquals(LocusEvidenceNature2D.RIGOROUS_INTERVAL, fold.getNature());
		assertTrue(fold.containsCanonical(-Math.PI, PERIOD));
		assertTrue(fold.containsCanonical(Math.nextDown(Math.PI), PERIOD));
		assertFalse(fold.containsCanonical(0, PERIOD));
		assertFalse(fold.containsCanonical(-Math.PI + 0.1, PERIOD));
		LocusExistenceStructure2D structure = result.getStructure();
		assertTrue(structure.getSemanticSignature().contains("|fold=" + fold.getSignature()));
		assertEquals(fold.getSignature(), structure.bindToRevision(7).getCertifiedFolds().get(0)
				.getSignature());
		// The symmetric locus of the first fixture publishes both folds.
		add("c2=Circle((0,0),2)");
		add("N2=Point(c2)");
		add("X2=Intersect(Line(N2,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("lx2=LocusV2(X2,N2)");
		assertEquals(2, certificate("lx2").getStructure().getCertifiedFolds().size());
	}

	/**
	 * The vertical mirror {@code t -> pi - t}: the horizontal line through the driver meets a
	 * vertical line in a point that folds at {@code t = +-pi/2}.
	 */
	@Test
	void theVerticalMirrorFoldsAtPlusMinusHalfPi() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("Y=Intersect(Line(N,Line((0,0),(1,0))),Line((3,0),(3,1)))");
		add("ly=LocusV2(Y,N)");
		Result result = certificate("ly");
		List<CertifiedStationaryPoint> points = result.getStationaryPoints();
		assertEquals(2, points.size(), points.toString());
		double halfPi = Math.PI / 2;
		assertFold(points.get(0), -Math.nextUp(halfPi), -halfPi, "VERTICAL", 1, 1);
		assertFold(points.get(1), halfPi, Math.nextUp(halfPi), "VERTICAL", 1, -1);
		// The signatures are deterministic.
		assertEquals(points.toString(), certificate("ly").getStationaryPoints().toString());
	}

	private static void assertFold(CertifiedStationaryPoint point, double lower, double upper,
			String mirror, int axis, int sign) {
		assertEquals(StationaryKind.FOLD, point.getKind(), point.toString());
		assertEquals(lower, point.getParameterLower(), point.toString());
		assertEquals(upper, point.getParameterUpper(), point.toString());
		assertEquals(ConstructionSymmetry2D.VERSION + ":" + mirror, point.getSymmetry());
		assertEquals(axis, point.getAxis(), point.toString());
		double[] second = point.getSecondDerivative();
		assertNotNull(second);
		assertTrue(sign > 0 ? second[2 * axis] > 0 : second[2 * axis + 1] < 0,
				point.toString());
		double width = point.getIsolationUpper() - point.getIsolationLower();
		assertTrue(width > 0 && width <= 2.5 * Math.scalb(PERIOD, -8), point.toString());
		assertEquals(LocusEvidenceNature2D.RIGOROUS_INTERVAL, point.getNature());
	}

	private Result certificate(String label) {
		Result result = ((AlgoDependentPointLocusV2) assertInstanceOf(GeoLocusV2.class,
				lookup(label)).getParentAlgorithm()).getHybridExistenceCertificate();
		assertNotNull(result, label);
		return result;
	}
}

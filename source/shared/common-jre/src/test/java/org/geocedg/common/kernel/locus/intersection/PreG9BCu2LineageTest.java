/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D.Status;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T8: structural lineage of certified components and resolution of
 * retained selectors ({@code locus-v2-existence-components.md} section 15.6; certification
 * design section 7, decision E-3 revised), on the synthetic critical configuration. Matrix
 * rows V-LIN-1, V-LIN-2, V-LIN-4, V-LIN-5 and V-LIN-6 (synthetic parts); the author model
 * is in {@code PreG9BCu2AuthorModelLineageTest}. The consumers read the certificates from
 * step T9, and step T11 publishes them.
 */
class PreG9BCu2LineageTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	private static final String CLOSURE = BRANCH + "/component/periodic-closure";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-LIN-1 and V-LIN-2 (synthetic parts), V-LIN-5: the first certificate relates to
	 * nothing; the closed component splits into the bite arc, whose key does not change when
	 * its ends move, and the arc merges back into the closed component. Keys never read the
	 * binary64 ends.
	 */
	@Test
	void theLineageFollowsStructuralSourcesNotEnds() {
		configuration("40-2^(-10)");
		AlgoDependentPointLocusV2 lq = algorithm("lq");
		assertEquals(List.of(CLOSURE), keys(lq));
		assertLineage(lq, LineageTransition.APPEARED, List.of(), List.of(CLOSURE));
		radius(40 + Math.scalb(1.0, -10));
		List<String> bite = keys(lq);
		assertEquals(1, bite.size());
		assertTrue(bite.get(0).contains("/DISCRIMINANT/lower;"), bite.toString());
		assertTrue(bite.get(0).endsWith("/DISCRIMINANT/upper"), bite.toString());
		assertLineage(lq, LineageTransition.SPLIT, List.of(CLOSURE), bite);
		double start = structure(lq).getContinuousValidComponents().get(0).getStart();
		radius(40 + Math.scalb(1.0, -4));
		assertNotEquals(start, structure(lq).getContinuousValidComponents().get(0).getStart());
		assertEquals(bite, keys(lq));
		assertLineage(lq, LineageTransition.UNCHANGED, List.of(), List.of());
		radius(40 - Math.scalb(1.0, -10));
		assertLineage(lq, LineageTransition.MERGED, bite, List.of(CLOSURE));
	}

	/**
	 * V-LIN-6 and V-LIN-4 (synthetic parts): a selector resolves only through a demonstrated
	 * correspondence, which unique containment then validates. A legacy selector citing the
	 * whole domain resolves by equality of the decomposition while the locus is closed, and
	 * not once it bites with no recorded transition, even with its parameter inside the arc;
	 * the recorded split resolves it inside the arc and never in the gap.
	 */
	@Test
	void selectorsResolveOnlyThroughDemonstratedCorrespondence() {
		configuration("40-2^(-10)");
		AlgoDependentPointLocusV2 lq = algorithm("lq");
		LocusExistenceStructure2D closed = structure(lq);
		String legacy = LocusComponentLineage2D.create(BRANCH, closed.getCanonicalDomain());
		assertEquals(Status.RESOLVED_BY_DECOMPOSITION_EQUALITY,
				LocusComponentSelectorResolver2D.resolve(legacy, closed, List.of(), 2.5)
						.getStatus());
		assertEquals(Status.RESOLVED_BY_KEY,
				LocusComponentSelectorResolver2D.resolve(CLOSURE, closed, List.of(), 2.5)
						.getStatus());
		radius(40 + Math.scalb(1.0, -10));
		LocusExistenceStructure2D bite = structure(lq);
		List<LocusLineage2D> split = lq.getHybridLineage();
		double critical = Math.atan2(4, 3);
		assertEquals(Status.NO_CORRESPONDENCE,
				LocusComponentSelectorResolver2D.resolve(legacy, bite, List.of(), 2.5)
						.getStatus());
		assertEquals(Status.NO_CORRESPONDENCE,
				LocusComponentSelectorResolver2D.resolve(CLOSURE, bite, List.of(), 2.5)
						.getStatus());
		LocusComponentSelectorResolver2D.Resolution followed =
				LocusComponentSelectorResolver2D.resolve(CLOSURE, bite, split, 2.5);
		assertEquals(Status.RESOLVED_BY_LINEAGE_TRANSITION, followed.getStatus());
		assertEquals(keys(lq).get(0), followed.getLineageKey());
		assertEquals(Status.NOT_CONTAINED,
				LocusComponentSelectorResolver2D.resolve(CLOSURE, bite, split, critical)
						.getStatus());
		assertEquals(Status.RESOLVED_BY_KEY, LocusComponentSelectorResolver2D.resolve(
				keys(lq).get(0), bite, List.of(), 2.5).getStatus());
	}

	/**
	 * Typed transitions: the shape rules admit the split of a closed component into one arc
	 * and the merge of one arc into a closed component, and still refuse a split without its
	 * parent or a merge without its child.
	 */
	@Test
	void openingAndClosingAreTypedTransitions() {
		assertEquals(LineageTransition.SPLIT, new LocusLineage2D(LineageTransition.SPLIT,
				List.of("closed"), List.of("arc")).getTransition());
		assertEquals(LineageTransition.MERGED, new LocusLineage2D(LineageTransition.MERGED,
				List.of("arc"), List.of("closed")).getTransition());
		assertThrows(IllegalArgumentException.class, () -> new LocusLineage2D(
				LineageTransition.SPLIT, List.of(), List.of("arc")));
		assertThrows(IllegalArgumentException.class, () -> new LocusLineage2D(
				LineageTransition.MERGED, List.of("arc"), List.of()));
	}

	/**
	 * The structural sources of the bite arc name the operation of the discriminant, the
	 * predicate and the side, and the point component of an external tangency is named by
	 * its root; neither key carries a coordinate.
	 */
	@Test
	void keysNameOperationsPredicatesAndSides() {
		configuration("40+2^(-10)");
		LocusExistenceStructure2D bite = structure(algorithm("lq"));
		String arc = keys(algorithm("lq")).get(0);
		assertTrue(arc.startsWith(BRANCH + "/component/"), arc);
		double start = bite.getContinuousValidComponents().get(0).getStart();
		double end = bite.getContinuousValidComponents().get(0).getEnd();
		assertTrue(!arc.contains(Double.toHexString(start))
				&& !arc.contains(Double.toHexString(end)) && !arc.contains("0x"), arc);
		getKernel().clearConstruction(true);
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((83,19),40)");
		add("N=Point(g1)");
		org.geogebra.common.kernel.geos.GeoPoint driver =
				(org.geogebra.common.kernel.geos.GeoPoint) lookup("N");
		driver.getPathParameter().setT(Math.atan2(-4, -3));
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
		LocusExistenceStructure2D tangency = structure(algorithm("lq"));
		assertEquals(1, tangency.getPointComponents().size());
		String point = LocusComponentLineage2D.of(tangency, tangency.getPointComponents().get(0));
		assertTrue(point.startsWith(BRANCH + "/point/") && point.contains("DISCRIMINANT"),
				point);
		assertTrue(!point.contains("0x"), point);
	}

	private void configuration(String radius) {
		add("rr=" + radius);
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),rr)");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
	}

	private void radius(double value) {
		GeoNumeric radius = (GeoNumeric) lookup("rr");
		radius.setValue(value);
		radius.updateCascade();
		getKernel().updateConstruction(false);
	}

	private static void assertLineage(AlgoDependentPointLocusV2 algorithm,
			LineageTransition transition, List<String> parents, List<String> children) {
		List<LocusLineage2D> lineage = algorithm.getHybridLineage();
		assertEquals(1, lineage.size(), lineage.toString());
		assertEquals(transition, lineage.get(0).getTransition());
		assertEquals(parents, lineage.get(0).getParentKeys());
		assertEquals(children, lineage.get(0).getChildKeys());
	}

	private static List<String> keys(AlgoDependentPointLocusV2 algorithm) {
		return LocusComponentLineage2D.keys(structure(algorithm));
	}

	private static LocusExistenceStructure2D structure(AlgoDependentPointLocusV2 algorithm) {
		HybridExistenceCertifier2D.Result result = algorithm.getHybridExistenceCertificate();
		assertNotNull(result);
		return result.getStructure();
	}

	private AlgoDependentPointLocusV2 algorithm(String label) {
		return (AlgoDependentPointLocusV2) assertInstanceOf(GeoLocusV2.class, lookup(label))
				.getParentAlgorithm();
	}
}

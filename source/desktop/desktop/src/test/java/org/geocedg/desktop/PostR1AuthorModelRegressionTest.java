/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Post-R1 regression on the author model of 2026-11-20 (oblique cone and cylinder, development
 * of the cone with the contour of the hole) and on its variant with the transported contour.
 *
 * <p>On the original: moving only {@code E_1}, the driver of the hole loci {@code w_2} and
 * {@code a_3}, or only {@code N}, the driver of the intersection curve that they read through
 * {@code v_2 = Intersect(o, q_1)}, publishes no revision of them (repairs A and A'), and the
 * intersection {@code t_2 = Intersect(s_2, l_2)}, whose inputs do not change, is not resolved
 * again (repair B). On the variant: the contour transported by the right generatrix agrees
 * with the independent mathematical reference, does not contain the conjugate image, does not
 * depend on {@code E_1}, and is saved and reopened with the same identities.</p>
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1AuthorModelRegressionTest {
	private static final double[] HOLE_PARAMETERS = {2.2, 2.3, 2.5, 2.7, -2.5, -2.3, 0.5};
	/**
	 * Agreement with the independent reference. The developed angle of the transport is an
	 * estimated oriented length divided by the radius of the sphere; the tolerance is the one
	 * of that estimate on a development of radius about 14, not a geometric certificate.
	 */
	private static final double REFERENCE_TOLERANCE = 1E-6;

	@TempDir
	Path work;

	@Test
	void movingOnlyADriverPublishesNoRevisionOfTheHoleLoci() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			GeoLocusV2 first = PreG9BCu0Corpus.locus(app, "w_2");
			GeoLocusV2 second = PreG9BCu0Corpus.locus(app, "a_3");
			AlgoLocusIntersectionV2 unchanged = (AlgoLocusIntersectionV2) PostR1Corpus
					.geo(app, "t_2").getParentAlgorithm();
			Map<String, Object> out = new LinkedHashMap<>();
			for (double warm : new double[] {2.17, 2.18, 2.19}) {
				PreG9BCu0Corpus.movePathPoint(app, "E_1", warm);
			}
			long firstRevision = first.getSemanticRevision();
			long secondRevision = second.getSemanticRevision();
			Object evaluator = first.getSemanticDefinition().getEvaluatorCapability();
			long resolutions = unchanged.getResolutionCount();
			List<String> reference = PostR1Corpus.evaluations(first, HOLE_PARAMETERS);

			// E_1 inside the incidence domain, outside it, and back.
			for (double t : new double[] {2.20, 2.24, 2.28, 2.32, 2.36, 2.40, 0.5, 2.30}) {
				PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
				assertEquals(firstRevision, first.getSemanticRevision(), "w_2 at E_1=" + t);
				assertEquals(secondRevision, second.getSemanticRevision(), "a_3 at E_1=" + t);
			}
			assertSame(evaluator, first.getSemanticDefinition().getEvaluatorCapability());
			assertEquals(resolutions, unchanged.getResolutionCount(),
					"t_2 is not resolved again while its inputs are unchanged");
			assertEquals(reference, PostR1Corpus.evaluations(first, HOLE_PARAMETERS));
			out.put("G_2 defined", PostR1Corpus.geo(app, "G_2").isDefined());

			// N, the driver of the source curve o.
			double saved = PreG9BCu0Corpus.pathParameter(app, "N");
			for (double step : new double[] {0.3, 0.6, -0.4}) {
				PreG9BCu0Corpus.movePathPoint(app, "N", saved + step);
				assertEquals(firstRevision, first.getSemanticRevision(), "w_2 at N+" + step);
				assertEquals(secondRevision, second.getSemanticRevision(), "a_3 at N+" + step);
			}
			assertEquals(reference, PostR1Corpus.evaluations(first, HOLE_PARAMETERS));

			// A non-driver input of the geometry still publishes a revision.
			PreG9BCu0Corpus.setNumber(app, "excCyl",
					((org.geogebra.common.kernel.geos.GeoNumeric) PostR1Corpus.geo(app,
							"excCyl")).getValue() + 0.05);
			out.put("revision after excCyl", first.getSemanticRevision() - firstRevision);
			out.put("changed", !reference.equals(PostR1Corpus.evaluations(first,
					HOLE_PARAMETERS)));
			return out;
		});
		assertEquals(Boolean.TRUE, row.get("G_2 defined"));
		assertTrue((Long) row.get("revision after excCyl") > 0, row.toString());
		assertEquals(Boolean.TRUE, row.get("changed"), row.toString());
	}

	@Test
	void theTransportedContourAgreesWithTheIndependentReference() throws Exception {
		List<PostR1Corpus.ReferenceRow> reference = PostR1Corpus.reference();
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			GeoLocusV2 hole = PostR1Corpus.buildTransport(app);
			return measure(app, hole, reference);
		});
		assertContour(row);
	}

	@Test
	void theDerivedVariantOpensWithTheSameContourAndIdentities() throws Exception {
		List<PostR1Corpus.ReferenceRow> reference = PostR1Corpus.reference();
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.transport(), work));
			GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, PostR1Corpus.HOLE);
			Map<String, Object> out = measure(app, hole, reference);
			String holeId = id(app, PostR1Corpus.HOLE);
			String pointId = id(app, "MP");
			String originId = id(app, "POrigen");
			List<String> before = PostR1Corpus.evaluations(hole, new double[] {-3, -1, 0, 1, 3});

			File saved = work.resolve("resaved.cedg").toFile();
			out.put("saved", app.saveGeoGebraFile(saved));
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(saved);
			GeoLocusV2 again = PreG9BCu0Corpus.locus(reopened, PostR1Corpus.HOLE);
			out.put("same ids", holeId.equals(id(reopened, PostR1Corpus.HOLE))
					&& pointId.equals(id(reopened, "MP"))
					&& originId.equals(id(reopened, "POrigen")));
			out.put("same function", before.equals(PostR1Corpus.evaluations(again,
					new double[] {-3, -1, 0, 1, 3})));
			out.put("revision after reopening", again.getSemanticRevision());
			return out;
		});
		assertContour(row);
		assertEquals(Boolean.TRUE, row.get("saved"), row.toString());
		assertEquals(Boolean.TRUE, row.get("same ids"), row.toString());
		assertEquals(Boolean.TRUE, row.get("same function"), row.toString());
	}

	private static void assertContour(Map<String, Object> row) {
		System.out.println("POST-R1 CONTOUR " + row);
		assertEquals(Boolean.TRUE, row.get("faithful"), row.toString());
		assertEquals(0, row.get("invalid"), row.toString());
		assertTrue((Double) row.get("max deviation") <= REFERENCE_TOLERANCE, row.toString());
		assertTrue((Double) row.get("min distance to conjugate") > 1.0,
				"the conjugate image is absent: " + row);
		assertEquals(Boolean.TRUE, row.get("independent of E_1"), row.toString());
		assertEquals(0L, row.get("revisions by E_1"), row.toString());
		assertEquals(0L, row.get("revisions by N"), row.toString());
		assertTrue((Long) row.get("revisions by excCyl") > 0, row.toString());
		assertEquals(Boolean.TRUE, row.get("explicit states when the cylinder leaves the cone"),
				row.toString());
	}

	/** Every measurement of the transported contour on one loaded document. */
	private static Map<String, Object> measure(AppGeoCeDG app, GeoLocusV2 hole,
			List<PostR1Corpus.ReferenceRow> reference) {
		Map<String, Object> out = new LinkedHashMap<>();
		assertNotNull(hole.getSemanticDefinition());
		out.put("faithful", ((ReconstructibleLocusEvaluator2D) hole.getSemanticDefinition()
				.getEvaluatorCapability()).isReconstructionFaithful());
		out.put("completeness", String.valueOf(hole.getSemanticDefinition()
				.getBranch(PostR1Corpus.MAIN).getExistenceStructure().getCompleteness()));
		double[] parameters = new double[reference.size() - 1];
		double deviation = 0;
		double conjugate = Double.POSITIVE_INFINITY;
		int invalid = 0;
		for (int index = 0; index < parameters.length; index++) {
			PostR1Corpus.ReferenceRow expected = reference.get(index);
			// The reference runs over [0, 2 pi]; the canonical driver domain is [-pi, pi).
			parameters[index] = expected.theta >= Math.PI ? expected.theta - 2 * Math.PI
					: expected.theta;
			LocusEvaluation2D evaluation = hole.evaluate(PostR1Corpus.MAIN, parameters[index],
					LocusEvaluationSession2D.reference());
			if (!evaluation.isValid()) {
				invalid++;
				continue;
			}
			deviation = Math.max(deviation, Math.hypot(
					evaluation.getPoint().getX() - expected.x,
					evaluation.getPoint().getY() - expected.y));
			conjugate = Math.min(conjugate, Math.hypot(
					evaluation.getPoint().getX() - expected.conjugateX,
					evaluation.getPoint().getY() - expected.conjugateY));
		}
		out.put("invalid", invalid);
		out.put("max deviation", deviation);
		out.put("min distance to conjugate", conjugate);

		// E_1 and its mirror image have the same incidence in the elevation and different
		// generatrices: the transported contour reads neither.
		long revision = hole.getSemanticRevision();
		List<String> function = PostR1Corpus.evaluations(hole, parameters);
		boolean independent = true;
		for (double t : new double[] {2.3, -2.3, 0.5, 2.9}) {
			PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
			independent &= function.equals(PostR1Corpus.evaluations(hole, parameters));
		}
		out.put("independent of E_1", independent);
		out.put("revisions by E_1", hole.getSemanticRevision() - revision);
		double saved = PreG9BCu0Corpus.pathParameter(app, "N");
		for (double step : new double[] {0.4, 1.3, -0.9}) {
			PreG9BCu0Corpus.movePathPoint(app, "N", saved + step);
		}
		out.put("revisions by N", hole.getSemanticRevision() - revision);
		assertTrue(((GeoPoint) PostR1Corpus.geo(app, "TrG")).isDefined());

		// A structural change, up to a cylinder that leaves the cone for part of the turn.
		double eccentricity = ((org.geogebra.common.kernel.geos.GeoNumeric) PostR1Corpus
				.geo(app, "excCyl")).getValue();
		PreG9BCu0Corpus.setNumber(app, "excCyl", eccentricity + 0.05);
		out.put("revisions by excCyl", hole.getSemanticRevision() - revision);
		assertNotEquals(function, PostR1Corpus.evaluations(hole, parameters));
		PreG9BCu0Corpus.setNumber(app, "excCyl", 3.8);
		List<String> states = new ArrayList<>();
		boolean explicit = hole.getSemanticDefinition() != null;
		if (explicit) {
			for (double parameter : parameters) {
				LocusEvaluation2D evaluation = hole.evaluate(PostR1Corpus.MAIN, parameter,
						LocusEvaluationSession2D.reference());
				String status = String.valueOf(evaluation.getStatus());
				if (!states.contains(status)) {
					states.add(status);
				}
				explicit &= evaluation.isValid() || evaluation.getStatus() != null;
			}
		}
		out.put("states at excCyl=3.8", states);
		out.put("explicit states when the cylinder leaves the cone", explicit);
		PreG9BCu0Corpus.setNumber(app, "excCyl", eccentricity);
		assertFalse(PostR1Corpus.evaluations(hole, parameters).isEmpty());
		return out;
	}

	private static String id(AppGeoCeDG app, String label) {
		GeoElement geo = PostR1Corpus.geo(app, label);
		PersistentGeoId id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		return String.valueOf(id);
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Increment I1 of the conditioned use of Locus V2 (ADR 0042) on the author model: a point
 * on a hole locus that has no certified component is created through the Desktop placement
 * facade, addressed by parameter, followed along its conditioned region, stopped at the
 * fold and read back from the native document.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1ConditionedPointDesktopTest {
	private static final double RADIUS = 0.05;
	@TempDir
	Path work;

	@Test
	void aPointOnAHoleLocusIsPlacedFollowedStoppedAndReopened() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> out = new LinkedHashMap<>();
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, "w_2");
			assertTrue(hole.getSemanticDefinition().getBranch(PostR1Corpus.MAIN)
					.getExistenceStructure().getContinuousValidComponents().isEmpty());
			long revision = hole.getSemanticRevision();
			LocusConditionedEvidence2D evidence = hole.getConditionedEvidence(
					PostR1Corpus.MAIN);
			assertNotNull(evidence);
			out.put("regions", evidence.getRegions().size());
			double lower = evidence.getRegions().get(1).getLower();
			double upper = evidence.getRegions().get(1).getUpper();
			double start = lower + 0.4 * (upper - lower);
			double[] at = point(hole, start);

			// Placement by a world request through the Desktop facade.
			GeoCeDGReferencePlacement offer = new GeoCeDGReferencePlacement(hole, at[0], at[1],
					LocusPointInteractionPolicy2D.initial(RADIUS));
			out.put("route", offer.getRoute());
			out.put("choices", offer.getChoices().size());
			GeoPoint point = offer.commit(offer.getChoices().get(0));
			assertNotNull(point);
			AlgoSemanticLocusPoint2D parent = AlgoSemanticLocusPoint2D
					.requireSemanticParent(point);
			LocusSemanticAddress2D created = parent.getCurrentSemanticAddress();
			out.put("created defined", point.isDefined());
			out.put("created by parameter", created.isParameterAddress());
			out.put("created parameter error", Math.abs(created.getCanonicalParameter()
					- start));
			PersistentGeoId identity = id(app, point);

			// Following along the region by the pointer.
			GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
			LocusPointContinuation2D.Context gesture = interaction.beginDrag(point);
			double target = lower + 0.6 * (upper - lower);
			double[] to = point(hole, target);
			boolean moved = false;
			for (int step = 1; step <= 8; step++) {
				double t = start + (target - start) * step / 8;
				double[] request = point(hole, t);
				moved |= interaction.move(point, request[0], request[1], RADIUS, gesture);
			}
			out.put("followed", moved);
			out.put("followed parameter error", Math.abs(parent.getCurrentSemanticAddress()
					.getCanonicalParameter() - target));
			out.put("followed distance", Math.hypot(point.getInhomX() - to[0],
					point.getInhomY() - to[1]));
			out.put("same identity", identity.equals(id(app, point)));

			// Toward the fold: the region ends at a transition band and the point stops.
			LocusPointContinuation2D.Result last = null;
			for (int step = 1; step <= 40; step++) {
				double t = Math.min(upper, target + (upper - target) * step / 20);
				double[] request = point(hole, Math.min(t, upper));
				interaction.move(point, request[0], request[1], RADIUS, gesture);
			}
			last = org.geocedg.common.kernel.locus.LocusV2PublicOperations
					.followInteractiveSemanticPointToParameter(point,
							Math.min(Math.PI, upper + 0.2),
							LocusPointInteractionPolicy2D.initial(RADIUS), gesture);
			out.put("beyond the fold", last.getStatus());
			out.put("band", last.getBandKind());
			out.put("stopped defined", point.isDefined());
			double stopped = parent.getCurrentSemanticAddress().getCanonicalParameter();
			out.put("stopped inside the region", stopped > lower && stopped < upper);
			out.put("revision unchanged", hole.getSemanticRevision() == revision);

			// Native document: the address is read back; evidence is recomputed.
			File saved = work.resolve("conditioned-point.cedg").toFile();
			out.put("saved", app.saveGeoGebraFile(saved));
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(saved);
			GeoElement loaded = reopened.getKernel().lookupLabel(point.getLabelSimple());
			AlgoSemanticLocusPoint2D reloaded = AlgoSemanticLocusPoint2D
					.requireSemanticParent((GeoPoint) loaded);
			out.put("reopened defined", loaded.isDefined());
			out.put("reopened parameter", reloaded.getCurrentSemanticAddress()
					.getCanonicalParameter() == stopped);
			out.put("reopened by parameter", reloaded.getCurrentSemanticAddress()
					.isParameterAddress());
			out.put("reopened identity", identity.equals(id(reopened, (GeoPoint) loaded)));
			return out;
		});
		assertEquals(2, row.get("regions"), row.toString());
		assertEquals(GeoCeDGReferencePlacement.Route.CONDITIONED_PARAMETER_ADDRESS,
				row.get("route"), row.toString());
		assertEquals(1, row.get("choices"), row.toString());
		assertEquals(Boolean.TRUE, row.get("created defined"), row.toString());
		assertEquals(Boolean.TRUE, row.get("created by parameter"), row.toString());
		assertTrue((Double) row.get("created parameter error") < 1e-4, row.toString());
		assertEquals(Boolean.TRUE, row.get("followed"), row.toString());
		assertTrue((Double) row.get("followed parameter error") < 1e-2, row.toString());
		assertTrue((Double) row.get("followed distance") < RADIUS, row.toString());
		assertEquals(Boolean.TRUE, row.get("same identity"), row.toString());
		assertEquals(LocusPointContinuation2D.Status.CONTINUATION_REACHED_BAND,
				row.get("beyond the fold"), row.toString());
		assertTrue(row.get("band") == LocusConditionedCellKind2D.TRANSITION_BAND
				|| row.get("band") == LocusConditionedCellKind2D.ABSENT_BAND, row.toString());
		assertEquals(Boolean.TRUE, row.get("stopped defined"), row.toString());
		assertEquals(Boolean.TRUE, row.get("stopped inside the region"), row.toString());
		assertEquals(Boolean.TRUE, row.get("revision unchanged"), row.toString());
		assertEquals(Boolean.TRUE, row.get("saved"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened defined"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened parameter"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened by parameter"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened identity"), row.toString());
	}

	private static double[] point(GeoLocusV2 locus, double parameter) {
		LocusEvaluation2D evaluation = locus.evaluate(PostR1Corpus.MAIN, parameter,
				LocusEvaluationSession2D.reference());
		assertTrue(evaluation.isValid(), "parameter " + parameter);
		return new double[] {evaluation.getPoint().getX(), evaluation.getPoint().getY()};
	}

	private static PersistentGeoId id(AppGeoCeDG app, GeoPoint point) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
	}
}

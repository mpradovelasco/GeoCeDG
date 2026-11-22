/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.error.ErrorHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Final corrective intervention of GeoCeDG 1.2.2, step C2 ({@code
 * locus-v2-existence-components.md} section 20; {@code spline-v2-pair-materialization.md}
 * section 11), on the product and its native document: a point on the root of a declared
 * piece follows the piece when the list is redefined, stays unresolved when the piece is
 * replaced, and the native archive reads every state back with its token.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1DeclaredPieceDesktopTest {
	private static final double LOW = 0.174;
	private static final double HIGH = 2.826;
	@TempDir
	Path work;

	@Test
	void aPointOnADeclaredPieceFollowsThePieceThroughTheNativeDocument() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> out = new LinkedHashMap<>();
			AppGeoCeDG app = G9U1TestApp.create();
			for (String command : new String[] {"m=0", "QA=(m,1)",
					"DLA={false,{-2,-1.6,true,true},{0.1,0.9,true,true}}",
					"LA=LocusV2(QA,m,DLA)", "SA=(0,0)", "SB=(1,4)", "SC=(2,-2)", "SD=(3,2)",
					"SP=SplineV2({SA,SB,SC,SD},3)", "RAB=Intersect(LA,SP)",
					"TG=Circle((1.5,1),1.326)", "RS=Intersect(LA,TG)"}) {
				PostR1Corpus.command(app, command);
			}
			String pair = token(app, "RAB", LOW);
			String target = token(app, "RS", LOW);
			PostR1Corpus.command(app, "X=Intersect(RAB,\"" + pair + "\")");
			PostR1Corpus.command(app, "Z=Intersect(RS,\"" + target + "\")");
			out.put("created", at(app, "X", pair) + at(app, "Z", target));

			// The piece of the roots becomes the first of its list.
			redefine(app, "{false,{0.1,0.9,true,true},{2.3,2.95,true,true}}");
			out.put("moved", at(app, "X", pair) + at(app, "Z", target));
			out.put("other pair root", String.valueOf(token(app, "RAB", HIGH)));
			out.put("other target root", String.valueOf(token(app, "RS", HIGH)));
			File moved = work.resolve("declared-piece-moved.cedg").toFile();
			out.put("saved moved", app.saveGeoGebraFile(moved));

			// The piece of the roots is replaced.
			redefine(app, "{false,{-2,-1.6,true,true},{2.3,2.95,true,true}}");
			out.put("replaced", defined(app, "X") + "/" + defined(app, "Z"));
			File replaced = work.resolve("declared-piece-replaced.cedg").toFile();
			out.put("saved replaced", app.saveGeoGebraFile(replaced));

			AppGeoCeDG first = PreG9BCu0Corpus.open(moved);
			out.put("reopened moved", at(first, "X", pair) + at(first, "Z", target));
			AppGeoCeDG second = PreG9BCu0Corpus.open(replaced);
			out.put("reopened replaced", defined(second, "X") + "/" + defined(second, "Z"));
			// The declaration of the piece returns in the reopened document.
			redefine(second, "{false,{0.1,0.9,true,true},{2.3,2.95,true,true}}");
			out.put("returned", at(second, "X", pair) + at(second, "Z", target));
			out.put("pair", pair);
			out.put("target", target);
			return out;
		});
		String kept = "defined at the root with its token;";
		assertEquals(kept + kept, row.get("created"), row.toString());
		assertEquals(kept + kept, row.get("moved"), row.toString());
		assertNotEquals("null", row.get("other pair root"), row.toString());
		assertNotEquals(row.get("pair"), row.get("other pair root"), row.toString());
		assertNotEquals("null", row.get("other target root"), row.toString());
		assertNotEquals(row.get("target"), row.get("other target root"), row.toString());
		assertEquals(Boolean.TRUE, row.get("saved moved"), row.toString());
		assertEquals("false/false", row.get("replaced"), row.toString());
		assertEquals(Boolean.TRUE, row.get("saved replaced"), row.toString());
		assertEquals(kept + kept, row.get("reopened moved"), row.toString());
		assertEquals("false/false", row.get("reopened replaced"), row.toString());
		assertEquals(kept + kept, row.get("returned"), row.toString());
	}

	private static String token(AppGeoCeDG app, String result, double x) {
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) app.getKernel()
				.lookupLabel(result);
		for (LocusIntersectionSolution2D root : rich.getIntersectionResult()
				.getFiniteSolutions()) {
			if (Math.abs(root.getEvaluatedPoint().getX() - x) < 1e-3) {
				return root.getIdentity().getRootToken();
			}
		}
		return null;
	}

	private static boolean defined(AppGeoCeDG app, String label) {
		return app.getKernel().lookupLabel(label).isDefined();
	}

	private static String at(AppGeoCeDG app, String label, String token) {
		GeoPoint point = (GeoPoint) app.getKernel().lookupLabel(label);
		return point.isDefined() && Math.abs(point.getInhomX() - LOW) < 1e-3
				&& token.equals(((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
						.getSelectedRootToken())
								? "defined at the root with its token;"
								: "defined=" + point.isDefined() + " x=" + point.getInhomX() + ";";
	}

	private static void redefine(AppGeoCeDG app, String list) {
		GeoElement target = app.getKernel().lookupLabel("DLA");
		AlgebraProcessor processor = app.getKernel().getAlgebraProcessor();
		processor.setSpatialRedefineAssessmentHandler(assessment -> assessment.getStatus()
				== SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE
						? SpatialRedefineExecutionMode.ADVANCED_RETAIN : null);
		try {
			processor.changeGeoElementNoExceptionHandling(target, list,
					EvalInfoFactory.getEvalInfoForRedefinition(app.getKernel(), target, true),
					true, null, ErrorHelper.silent());
		} finally {
			processor.setSpatialRedefineAssessmentHandler(null);
		}
	}
}

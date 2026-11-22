/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Increment I2 of the conditioned use of Locus V2 (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 9 and 10.1) on the author model: the hole loci are
 * drawn as the strokes of their conditioned regions with the user's style, each visible
 * region carries the system mark in the live view and in a picture export, and the
 * properties state the grade in ordinary words in both languages.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1ConditionedPresentationDesktopTest {
	private static final int WIDTH = 1092;
	private static final int HEIGHT = 797;
	@TempDir
	Path work;

	@Test
	void theHoleLociAreDrawnMarkedAndDescribed() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> out = new LinkedHashMap<>();
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			EuclidianView view = app.getEuclidianView1();
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(190.04375359472687, 421.1450265280542, 23.987955437910177,
					23.987955437910177);
			view.updateAllDrawables(true);
			GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, "w_2");
			DrawLocusV2 drawable = (DrawLocusV2) view.getDrawableFor(hole);
			LocusRenderData2D data = drawable.getRenderCache().getLastBuild();
			out.put("regions drawn", data.getConditionedRegions().size());
			out.put("exploratory vertices", data.getExploratoryVertices().size());
			out.put("certified vertices", data.getVertices().size());
			double[] anchors = drawable.getConditionedMarkAnchors();
			out.put("marks", anchors.length / 2);

			// Live paint: the mark is hollow (its centre is the background) and its outline
			// has the colour of the object, whatever the line style and thickness.
			int rgb = hole.getObjectColor().getARGB() & 0xFFFFFF;
			boolean hollow = true;
			boolean outlined = true;
			for (int[] style : new int[][] {{EuclidianStyleConstants.LINE_TYPE_FULL, 3},
					{EuclidianStyleConstants.LINE_TYPE_DASHED_LONG, 9}}) {
				hole.setLineType(style[0]);
				hole.setLineThickness(style[1]);
				hole.updateRepaint();
				BufferedImage image = paint(view);
				double[] current = ((DrawLocusV2) view.getDrawableFor(hole))
						.getConditionedMarkAnchors();
				for (int index = 0; index + 1 < current.length; index += 2) {
					int x = (int) Math.round(current[index]);
					int y = (int) Math.round(current[index + 1]);
					hollow &= (image.getRGB(x, y) & 0xFFFFFF) == 0xFFFFFF;
					outlined &= near(image.getRGB(x - 3, y), rgb)
							|| near(image.getRGB(x + 3, y), rgb)
							|| near(image.getRGB(x, y - 3), rgb)
							|| near(image.getRGB(x, y + 3), rgb);
				}
				out.put("anchors with style " + style[0], current.length / 2);
			}
			out.put("hollow", hollow);
			out.put("outlined", outlined);

			// A picture export draws what the view draws, the mark included.
			BufferedImage exported = PreG9BR6PlusBPictureFidelityTest.png(app, view, 1);
			BufferedImage live = paint(view);
			out.put("export has the object colour", PreG9BR6PlusBPictureFidelityTest.count(
					exported, hole.getObjectColor()) > 50);
			out.put("live has the object colour", PreG9BR6PlusBPictureFidelityTest.count(live,
					hole.getObjectColor()) > 50);

			// The properties state the grade in ordinary words, in both languages.
			for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
				app.setLocale(locale);
				String language = locale.getLanguage();
				String details = GeoCeDGDefinitionInspector.semanticDetails(app, hole);
				out.put("details " + language, details);
				out.put("grade " + language, GeoCeDGDefinitionInspector.conditionedGrade(app,
						hole));
				out.put("tooltip " + language, GeoCeDGDefinitionInspector
						.readOnlyExplanation(app, hole));
			}
			// A certified locus and an ordinary object state no conditioned grade.
			out.put("grade of a certified locus", String.valueOf(GeoCeDGDefinitionInspector
					.conditionedGrade(app, PreG9BCu0Corpus.locus(app, "o"))));
			GeoElement driver = PostR1Corpus.geo(app, "E_1");
			out.put("grade of a free point", String.valueOf(GeoCeDGDefinitionInspector
					.conditionedGrade(app, driver)));
			// A point addressed by parameter on the region states it the same way.
			double start = (data.getConditionedRegions().get(0).getLower()
					+ data.getConditionedRegions().get(0).getUpper()) / 2;
			GeoPoint point = (GeoPoint) PostR1Corpus.command(app, "ZZ=Point(w_2,\""
					+ PostR1Corpus.MAIN + "\"," + start + ")")[0];
			app.setLocale(Locale.ENGLISH);
			out.put("point defined", point.isDefined());
			out.put("point details", GeoCeDGDefinitionInspector.semanticDetails(app, point));
			return out;
		});
		assertEquals(2, row.get("regions drawn"), row.toString());
		assertEquals(0, row.get("exploratory vertices"), row.toString());
		assertEquals(0, row.get("certified vertices"), row.toString());
		assertEquals(2, row.get("marks"), row.toString());
		assertEquals(2, row.get("anchors with style "
				+ EuclidianStyleConstants.LINE_TYPE_FULL), row.toString());
		assertEquals(2, row.get("anchors with style "
				+ EuclidianStyleConstants.LINE_TYPE_DASHED_LONG), row.toString());
		assertEquals(Boolean.TRUE, row.get("hollow"), row.toString());
		assertEquals(Boolean.TRUE, row.get("outlined"), row.toString());
		assertEquals(Boolean.TRUE, row.get("export has the object colour"), row.toString());
		assertEquals(Boolean.TRUE, row.get("live has the object colour"), row.toString());

		String english = (String) row.get("details en");
		assertTrue(english.contains("Conditioned — existence/continuity not certified"),
				english);
		assertTrue(english.contains("Conditioned regions: 2"), english);
		assertTrue(english.contains("Traversal policy: conditioned-traversal/v1"), english);
		assertTrue(english.contains("H1 validity"), english);
		assertTrue(english.contains("H2 selection"), english);
		assertTrue(english.contains("H3 continuity"), english);
		assertTrue(english.contains("H4 resolution"), english);
		assertTrue(english.contains("transition band"), english);
		assertTrue(english.contains("absence band"), english);
		assertTrue(english.contains("is not stated to be complete"), english);
		assertFalse(english.contains("certified component"), english);
		String spanish = (String) row.get("details es");
		assertTrue(spanish.contains("Condicionado — existencia/continuidad no certificadas"),
				spanish);
		assertTrue(spanish.contains("Regiones condicionadas: 2"), spanish);
		assertTrue(spanish.contains("H3 continuidad"), spanish);
		assertTrue(spanish.contains("banda de transición"), spanish);
		assertTrue(spanish.contains("no se declara completo"), spanish);
		assertEquals("Conditioned — existence/continuity not certified", row.get("grade en"));
		assertEquals("Condicionado — existencia/continuidad no certificadas",
				row.get("grade es"));
		assertTrue(((String) row.get("tooltip es")).startsWith("Condicionado"),
				row.toString());
		assertEquals("null", row.get("grade of a certified locus"), row.toString());
		assertEquals("null", row.get("grade of a free point"), row.toString());
		assertEquals(Boolean.TRUE, row.get("point defined"), row.toString());
		String point = (String) row.get("point details");
		assertNotNull(point);
		assertTrue(point.contains("Result on a conditioned region"), point);
		assertTrue(point.contains("Integrity: NOT_ESTABLISHED"), point);
		assertTrue(point.contains("conditioned stretch"), point);
	}

	private static boolean near(int argb, int rgb) {
		int dr = Math.abs((argb >> 16 & 0xFF) - (rgb >> 16 & 0xFF));
		int dg = Math.abs((argb >> 8 & 0xFF) - (rgb >> 8 & 0xFF));
		int db = Math.abs((argb & 0xFF) - (rgb & 0xFF));
		return dr + dg + db < 150;
	}

	private static BufferedImage paint(EuclidianView view) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
		view.updateAllDrawables(true);
		view.paint(new GGraphics2DD(graphics));
		graphics.dispose();
		return image;
	}
}

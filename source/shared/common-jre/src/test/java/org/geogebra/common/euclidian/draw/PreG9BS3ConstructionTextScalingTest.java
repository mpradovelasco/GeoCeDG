/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.euclidian.draw;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GDimension;
import org.geogebra.common.awt.GFont;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.DrawEquation;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.factories.AwtFactoryCommon;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.jre.headless.DrawEquationStub;
import org.geogebra.common.jre.headless.EuclidianViewNoGui;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.test.LocalizationCommonUTF;
import org.junit.jupiter.api.Test;

class PreG9BS3ConstructionTextScalingTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return new ScaleAwareAppCommon();
	}

	@Test
	void worldTextFontAndBoundsFollowPrimaryViewScale() {
		EuclidianView view = referenceView();
		GeoText text = worldText("ordinary");
		text.setBackgroundColor(GColor.WHITE);
		DrawText drawable = new DrawText(view, text);
		double logicalSize = text.getFontSize(view.getFontSize());
		double referenceWidth = drawable.getBounds().getWidth();
		double referenceHeight = drawable.getBounds().getHeight();

		setScale(view, 100, 100);
		drawable.update();

		assertThat(drawable.getFontSize(), closeTo(logicalSize * 2, 1E-12));
		assertThat(drawable.getBounds().getWidth(), greaterThan(referenceWidth));
		assertThat(drawable.getBounds().getHeight(), greaterThan(referenceHeight));

		setScale(view, 25, 25);
		drawable.update();

		assertThat(drawable.getFontSize(), closeTo(logicalSize / 2, 1E-12));
		assertThat(drawable.getBounds().getWidth(), lessThan(referenceWidth));
		assertThat(drawable.getBounds().getHeight(), lessThan(referenceHeight));
	}

	@Test
	void zoomPreservesAnchorLogicalFontXmlAndSavedState() {
		EuclidianView view = referenceView();
		GeoText text = worldText("identity");
		text.setFontSizeMultiplier(0.7);
		text.setBackgroundColor(GColor.WHITE);
		DrawText drawable = new DrawText(view, text);
		GeoPoint anchor = (GeoPoint) text.getStartPoint();
		double logicalSize = text.getFontSize(view.getFontSize());
		double x = anchor.getInhomX();
		double y = anchor.getInhomY();
		String xml = text.getXML();
		getApp().setSaved();

		setScale(view, 125, 75);
		drawable.update();

		assertThat(anchor.getInhomX(), closeTo(x, 1E-15));
		assertThat(anchor.getInhomY(), closeTo(y, 1E-15));
		assertThat(text.getFontSize(view.getFontSize()), equalTo(logicalSize));
		assertThat(text.getXML(), equalTo(xml));
		assertTrue(getApp().isSaved());
	}

	@Test
	void hitBoundsAreRemeasuredBeforePaintAfterZoom() {
		EuclidianView view = referenceView();
		GeoText text = worldText("select this text");
		DrawText drawable = new DrawText(view, text);
		double oldRight = drawable.getBounds().getX() + drawable.getBounds().getWidth();

		setScale(view, 150, 150);
		drawable.update();

		GRectangle bounds = drawable.getBounds();
		int x = (int) Math.floor(bounds.getX() + bounds.getWidth() - 1);
		int y = (int) Math.floor(bounds.getY() + bounds.getHeight() / 2);
		assertThat((double) x, greaterThan(oldRight));
		assertTrue(drawable.hit(x, y, 0));
	}

	@Test
	void multilineAndLatexUseTheSameEffectiveScale() {
		EuclidianView view = referenceView();
		GeoText multiline = worldText("first line\nsecond line");
		GeoText latex = worldText("x^2 + y^2");
		latex.setLaTeX(true, false);
		DrawText multilineDrawable = new DrawText(view, multiline);
		DrawText latexDrawable = new DrawText(view, latex);
		double multilineHeight = multilineDrawable.getBounds().getHeight();
		double latexWidth = latexDrawable.getBounds().getWidth();

		setScale(view, 100, 100);
		multilineDrawable.update();
		latexDrawable.update();

		assertThat(multilineDrawable.getFontSize(),
				closeTo(multiline.getFontSize(view.getFontSize()) * 2, 1E-12));
		assertThat(latexDrawable.getFontSize(),
				closeTo(latex.getFontSize(view.getFontSize()) * 2, 1E-12));
		assertThat(multilineDrawable.getBounds().getHeight(),
				greaterThan(multilineHeight));
		assertThat(latexDrawable.getBounds().getWidth(), greaterThan(latexWidth));
	}

	@Test
	void nonuniformAxesUseXScaleWithoutStretchingTheFont() {
		EuclidianView view = referenceView();
		GeoText text = worldText("uniform glyphs");
		DrawText drawable = new DrawText(view, text);
		double logicalSize = text.getFontSize(view.getFontSize());

		setScale(view, 100, 25);
		drawable.update();

		assertThat(drawable.getFontSize(), closeTo(logicalSize * 2, 1E-12));
		assertThat(drawable.getTextFont().getSize(),
				closeTo(logicalSize * 2, 1E-12));
	}

	@Test
	void absoluteScreenTextKeepsItsScreenFontSize() {
		EuclidianView view = referenceView();
		GeoText text = worldText("screen fixed");
		text.setAbsoluteScreenLocActive(true);
		text.setAbsoluteScreenLoc(120, 80);
		DrawText drawable = new DrawText(view, text);
		double fontSize = drawable.getFontSize();
		double width = drawable.getBounds().getWidth();

		setScale(view, 150, 150);
		drawable.update();

		assertThat(drawable.getFontSize(), equalTo(fontSize));
		assertThat(drawable.getBounds().getWidth(), equalTo(width));
	}

	@Test
	void separateViewsDeriveIndependentPixelSizesForOneText() {
		EuclidianView firstView = referenceView();
		GeoText text = worldText("two views");
		DrawText firstDrawable = new DrawText(firstView, text);
		EuclidianSettings secondSettings = getSettings().getEuclidian(2);
		EuclidianView secondView = new EuclidianViewNoGui(
				getApp().newEuclidianController(getKernel()), 2, secondSettings,
				AwtFactory.getPrototype().createBufferedImage(800, 600, false)
						.createGraphics());
		setScale(secondView, 100, 100);
		DrawText secondDrawable = new DrawText(secondView, text);

		assertThat(secondDrawable.getFontSize(),
				closeTo(firstDrawable.getFontSize() * 2, 1E-12));
		assertThat(text.getStartPoint().getInhomX(), closeTo(1, 1E-15));
		assertThat(text.getStartPoint().getInhomY(), closeTo(2, 1E-15));
	}

	@Test
	void classicConfigurationRetainsInheritedScreenFontSize() throws Exception {
		AppCommon classic = AppCommonFactory.create(new AppConfigDefault());
		EuclidianView view = classic.getEuclidianView1();
		setScale(view, 50, 50);
		GeoText text = worldText(classic.getKernel().getConstruction(), "classic");
		DrawText drawable = new DrawText(view, text);
		double fontSize = drawable.getFontSize();

		setScale(view, 150, 150);
		drawable.update();

		assertThat(drawable.getFontSize(), equalTo(fontSize));
	}

	@Test
	void deterministicSmokeFixtureLoadsAllClassifiedTextModes() throws Exception {
		Path model = findRepositoryRoot().resolve(
				"models/regression/pre-g9b-s3-text-view-scaling/"
						+ "pre-g9b-s3-text-view-scaling.ggb");
		getApp().setXML(readConstructionXml(model), true);

		GeoText ordinary = (GeoText) getKernel().lookupLabel("ordinaryWorldText");
		GeoText multiline = (GeoText) getKernel().lookupLabel("multilineWorldText");
		GeoText latex = (GeoText) getKernel().lookupLabel("latexWorldText");
		GeoText screen = (GeoText) getKernel().lookupLabel("screenFixedControl");
		assertFalse(ordinary.isAbsoluteScreenLocActive());
		assertTrue(multiline.getTextString().contains("\n"));
		assertTrue(latex.isLaTeX());
		assertTrue(screen.isAbsoluteScreenLocActive());
	}

	private EuclidianView referenceView() {
		EuclidianView view = getApp().getEuclidianView1();
		setScale(view, EuclidianView.SCALE_STANDARD, EuclidianView.SCALE_STANDARD);
		return view;
	}

	private GeoText worldText(String value) {
		try {
			return worldText(getConstruction(), value);
		} catch (Exception exception) {
			throw new AssertionError(exception);
		}
	}

	private static GeoText worldText(Construction construction, String value)
			throws Exception {
		GeoPoint anchor = new GeoPoint(construction);
		anchor.setCoords(1, 2, 1);
		GeoText text = new GeoText(construction, value);
		text.setStartPoint(anchor);
		text.setEuclidianVisible(true);
		return text;
	}

	private static void setScale(EuclidianView view, double xScale, double yScale) {
		view.setCoordSystem(400, 300, xScale, yScale);
	}

	private static String readConstructionXml(Path model) throws IOException {
		try (ZipFile archive = new ZipFile(model.toFile())) {
			ZipEntry entry = archive.getEntry("geogebra.xml");
			assertNotNull(entry, "geogebra.xml is missing from " + model);
			try (InputStream input = archive.getInputStream(entry)) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}

	private static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("models"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new AssertionError("Could not resolve the GeoCeDG repository root.");
	}

	private static final class ScaleAwareAppCommon extends AppCommon {
		private DrawEquation drawEquation;

		private ScaleAwareAppCommon() {
			super(new LocalizationCommonUTF(2), new AwtFactoryCommon(),
					new AppConfigGeoCeDG());
		}

		@Override
		public DrawEquation getDrawEquation() {
			if (drawEquation == null) {
				drawEquation = new DrawEquationStub() {
					@Override
					public GDimension drawEquation(App app, GeoElementND geo,
							GGraphics2D graphics, int x, int y, String value,
							GFont font, boolean serif, GColor foreground,
							GColor background, boolean useCache,
							boolean updateAgain, Runnable callback) {
						return AwtFactory.getPrototype().newDimension(
								(int) Math.ceil(value.length() * font.getSize()),
								(int) Math.ceil(font.getSize()));
					}
				};
			}
			return drawEquation;
		}
	}
}

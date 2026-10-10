/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.function.BiFunction;

import org.geogebra.common.export.pstricks.ExportSettings;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;

/**
 * PRE-G9B-R6-plus-C (C4, C17, {@code DQ-C16}): the export settings a GeoCeDG
 * LaTeX exporter reads during one generation. With a physical construction
 * unit, {@code xunit = yunit = fb(c) * 100 * a / b} centimetres per model unit
 * and the picture width and height are derived from the export area; the dialog
 * or API values are then never a second scale. Without a construction unit the
 * dialog or API values stay explicit non-physical device parameters. The final
 * text passes through the semantic-curve completion of
 * {@link LatexSemanticExportSupport}.
 */
final class GeoCeDGLatexSettings implements ExportSettings {

	private final ExportSettings delegate;
	private final GeoGebraExport exporter;
	private final double physicalScale;
	private final LatexSemanticExportSupport support;
	private final String commentPrefix;
	private final String scaleComment;
	private final BiFunction<String, Double, String> physicalComposition;

	/**
	 * @param delegate dialog or API settings
	 * @param exporter exporter whose bounds give the derived size
	 * @param app exporting application
	 * @param support semantic-curve support of the exporter
	 * @param commentPrefix dialect line-comment prefix
	 * @param physicalComposition dialect page composition of a physical export
	 *            (PRE-G9B-R6-plus-E3-R1), given the text and centimetres per unit
	 */
	GeoCeDGLatexSettings(ExportSettings delegate, GeoGebraExport exporter, App app,
			LatexSemanticExportSupport support, String commentPrefix,
			BiFunction<String, Double, String> physicalComposition) {
		this.delegate = delegate;
		this.exporter = exporter;
		this.support = support;
		this.physicalComposition = physicalComposition;
		this.commentPrefix = commentPrefix;
		double scale = app.getPhysicalExportScale();
		physicalScale = scale > 0 && Double.isFinite(scale) ? scale : Double.NaN;
		scaleComment = Double.isNaN(physicalScale) ? null
				: physicalScaleComment(app, physicalScale);
	}

	private static String physicalScaleComment(App app, double scale) {
		String drawingScale = app instanceof DrawingScaleHolder
				? ((DrawingScaleHolder) app).getDrawingScale().toString()
				: DrawingScale.ONE_TO_ONE.toString();
		String unit = app.getKernel().getConstruction().getUnitSystem().getState()
				.effectiveConstructionUnit().token();
		return "GeoCeDG physical scale: drawing scale " + drawingScale
				+ ", construction unit " + unit + ": 1 model unit = " + scale
				+ " cm on the output (xunit = yunit)";
	}

	/** @return whether the physical unit contract applies */
	boolean isPhysical() {
		return !Double.isNaN(physicalScale);
	}

	@Override
	public double getXUnit() {
		return isPhysical() ? physicalScale : delegate.getXUnit();
	}

	@Override
	public double getYUnit() {
		return isPhysical() ? physicalScale : delegate.getYUnit();
	}

	@Override
	public double getLatexWidth() {
		return isPhysical()
				? (exporter.getXmax() - exporter.getXmin()) * physicalScale
				: delegate.getLatexWidth();
	}

	@Override
	public double getLatexHeight() {
		return isPhysical()
				? (exporter.getYmax() - exporter.getYmin()) * physicalScale
				: delegate.getLatexHeight();
	}

	@Override
	public void write(StringBuilder code) {
		String text = support.finish(code, commentPrefix, scaleComment).toString();
		if (isPhysical()) {
			// PRE-G9B-R6-plus-E3-R1: the page is the export area at the physical scale
			text = physicalComposition.apply(text, physicalScale);
		}
		delegate.write(new StringBuilder(text));
	}

	@Override
	public int getFontSize() {
		return delegate.getFontSize();
	}

	@Override
	public boolean isGrayscale() {
		return delegate.isGrayscale();
	}

	@Override
	public boolean getKeepDotColors() {
		return delegate.getKeepDotColors();
	}

	@Override
	public int getFormat() {
		return delegate.getFormat();
	}

	@Override
	public double textYmaxValue() {
		return delegate.textYmaxValue();
	}

	@Override
	public double textYminValue() {
		return delegate.textYminValue();
	}

	@Override
	public boolean getAsyCompactCse5() {
		return delegate.getAsyCompactCse5();
	}

	@Override
	public boolean getAsyCompact() {
		return delegate.getAsyCompact();
	}

	@Override
	public int getFillType() {
		return delegate.getFillType();
	}

	@Override
	public boolean getExportPointSymbol() {
		return delegate.getExportPointSymbol();
	}

	@Override
	public boolean getShowAxes() {
		return delegate.getShowAxes();
	}

	@Override
	public boolean getUsePairNames() {
		return delegate.getUsePairNames();
	}

	@Override
	public boolean getGnuplot() {
		return delegate.getGnuplot();
	}

	@Override
	public GeoNumeric getcbSlidersItem() {
		return delegate.getcbSlidersItem();
	}
}

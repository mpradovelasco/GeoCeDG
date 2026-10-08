/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.List;

import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.export.pstricks.ExportGraphicsFactory;
import org.geogebra.common.export.pstricks.ExportSettings;
import org.geogebra.common.export.pstricks.GeoGebraToAsymptote;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.App;

/**
 * PRE-G9B-R6-plus-C GeoCeDG Asymptote exporter: the host dialect plus the
 * physical unit contract and the semantic Locus V2 / Spline V2 output of
 * {@link LatexSemanticExportSupport}, one {@code draw} path per certified
 * component ({@code --cycle} when closed), with {@code //} disclosure comments.
 */
public final class GeoCeDGGeoGebraToAsymptote extends GeoGebraToAsymptote
		implements LatexSemanticExporter {

	private final LatexSemanticExportSupport support;

	/**
	 * @param app exporting application
	 * @param graphicsFactory host graphics factory
	 */
	public GeoCeDGGeoGebraToAsymptote(App app,
			ExportGraphicsFactory graphicsFactory) {
		super(app, graphicsFactory);
		support = new LatexSemanticExportSupport(app);
	}

	@Override
	public LatexSemanticExportSupport getSemanticSupport() {
		return support;
	}

	@Override
	public void generateAllCode() {
		ExportSettings original = frame;
		if (support.begin() != null) {
			original.write(new StringBuilder());
			return;
		}
		frame = new GeoCeDGLatexSettings(original, this, getApp(), support, "//");
		try {
			super.generateAllCode();
		} finally {
			frame = original;
		}
	}

	@Override
	protected void drawUnsupportedElement(GeoElement g) {
		if (!support.write(g, this::writePath,
				line -> code.append("\n// ").append(line))) {
			super.drawUnsupportedElement(g);
		}
	}

	/** PRE-G9B-R6-plus-E2: the dimension line of a native dimension with its arrows. */
	@Override
	protected void drawGeoSegment(GeoSegmentND geo) {
		if (DimensionLatexExport.dimensionLineOwner(geo) == null) {
			super.drawGeoSegment(geo);
			return;
		}
		double[] a = new double[3];
		double[] b = new double[3];
		geo.getStartPoint().getInhomCoords(a);
		geo.getEndPoint().getInhomCoords(b);
		StringBuilder draw = new StringBuilder();
		startDraw(draw);
		addPoint(a[0], a[1], draw);
		draw.append("--");
		addPoint(b[0], b[1], draw);
		endDraw(geo, draw);
		int close = draw.lastIndexOf(")");
		code.append(draw, 0, close).append(", Arrows(6)").append(draw, close,
				draw.length());
	}

	/** PRE-G9B-R6-plus-E2: the value of a native dimension, aligned and centred. */
	@Override
	protected void drawText(GeoText geo) {
		AlgoNativeDimension owner = DimensionLatexExport.dimensionTextOwner(geo);
		if (owner == null) {
			super.drawText(geo);
			return;
		}
		double[] place = DimensionLatexExport.placement(owner, xunit, yunit);
		if (place == null || !geo.isDefined()) {
			return;
		}
		code.append("\nlabel(rotate(").append(format(place[2])).append(")*Label(\"")
				.append(DimensionLatexExport.escapeAsymptote(geo.getTextStringSafe()))
				.append("\"), ");
		addPoint(place[0], place[1], code);
		code.append(", dir(").append(format(place[2] + 90)).append(')');
		GColor color = geo.getObjectColor();
		if (!color.equals(GColor.BLACK)) {
			code.append(", ");
			colorCode(color, code);
		}
		code.append("); ");
	}

	private void writePath(GeoLocusV2 locus, PolylineGeometry path) {
		StringBuilder draw = new StringBuilder();
		startDraw(draw);
		List<Point2D> vertices = path.getVertices();
		for (int index = 0; index < vertices.size(); index++) {
			Point2D vertex = vertices.get(index);
			addPoint(vertex.getX(), vertex.getY(), draw);
			if (index != vertices.size() - 1) {
				draw.append("--");
			}
		}
		if (path.isClosed()) {
			draw.append("--cycle");
		}
		endDraw(locus, draw);
		code.append(draw);
	}
}

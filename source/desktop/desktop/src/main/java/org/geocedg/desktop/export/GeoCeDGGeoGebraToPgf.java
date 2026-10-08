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
import org.geogebra.common.export.pstricks.GeoGebraToPgf;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.App;

/**
 * PRE-G9B-R6-plus-C GeoCeDG PGF/TikZ exporter: the host dialect plus the
 * physical unit contract and the semantic Locus V2 / Spline V2 output of
 * {@link LatexSemanticExportSupport}, one {@code \draw} path per certified
 * component ({@code -- cycle} when closed), with {@code %} disclosure comments.
 */
public final class GeoCeDGGeoGebraToPgf extends GeoGebraToPgf
		implements LatexSemanticExporter {

	private final LatexSemanticExportSupport support;

	/**
	 * @param app exporting application
	 * @param graphicsFactory host graphics factory
	 */
	public GeoCeDGGeoGebraToPgf(App app, ExportGraphicsFactory graphicsFactory) {
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
		frame = new GeoCeDGLatexSettings(original, this, getApp(), support, "%");
		try {
			super.generateAllCode();
		} finally {
			frame = original;
		}
	}

	@Override
	protected void drawUnsupportedElement(GeoElement g) {
		if (!support.write(g, this::writePath,
				line -> code.append("% ").append(line).append('\n'))) {
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
		startBeamer(code);
		code.append("\\draw [");
		String options = lineOptionCode(geo, true);
		if (options.length() != 0) {
			code.append(options).append(',');
		}
		code.append("<->,>=latex] (").append(format(a[0])).append(',')
				.append(format(a[1])).append(")-- (").append(format(b[0])).append(',')
				.append(format(b[1])).append(");\n");
		endBeamer(code);
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
		startBeamer(code);
		code.append("\\draw ");
		GColor color = geo.getObjectColor();
		if (!color.equals(GColor.BLACK)) {
			code.append("[color=");
			colorCode(color, code);
			code.append("] ");
		}
		code.append('(').append(format(place[0])).append(',').append(format(place[1]))
				.append(") node[rotate=").append(format(place[2]))
				.append(",anchor=south] {")
				.append(DimensionLatexExport.escapeTeX(geo.getTextStringSafe()))
				.append("};\n");
		endBeamer(code);
	}

	private void writePath(GeoLocusV2 locus, PolylineGeometry path) {
		startBeamer(code);
		code.append("\\draw ");
		String options = lineOptionCode(locus, true);
		if (options.length() != 0) {
			code.append('[').append(options).append("] ");
		}
		List<Point2D> vertices = path.getVertices();
		for (int index = 0; index < vertices.size(); index++) {
			Point2D vertex = vertices.get(index);
			code.append('(').append(format(vertex.getX())).append(',')
					.append(format(vertex.getY())).append(')');
			if (index != vertices.size() - 1) {
				code.append("-- ");
			}
		}
		if (path.isClosed()) {
			code.append("-- cycle");
		}
		code.append(";\n");
		endBeamer(code);
	}
}

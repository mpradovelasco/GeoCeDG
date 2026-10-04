/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.List;

import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.export.pstricks.ExportGraphicsFactory;
import org.geogebra.common.export.pstricks.ExportSettings;
import org.geogebra.common.export.pstricks.GeoGebraToAsymptote;
import org.geogebra.common.kernel.geos.GeoElement;
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

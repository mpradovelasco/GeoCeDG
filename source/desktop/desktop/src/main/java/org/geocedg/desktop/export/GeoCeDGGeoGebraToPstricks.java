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
import org.geogebra.common.export.pstricks.GeoGebraToPstricks;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.App;

/**
 * PRE-G9B-R6-plus-C GeoCeDG PSTricks exporter: the host dialect plus the
 * physical unit contract and the semantic Locus V2 / Spline V2 output of
 * {@link LatexSemanticExportSupport}, one {@code \psline} (open) or
 * {@code \pspolygon} (closed) per certified component, with {@code %}
 * disclosure comments.
 */
public final class GeoCeDGGeoGebraToPstricks extends GeoGebraToPstricks
		implements LatexSemanticExporter {

	private final LatexSemanticExportSupport support;

	/**
	 * @param app exporting application
	 * @param graphicsFactory host graphics factory
	 */
	public GeoCeDGGeoGebraToPstricks(App app, ExportGraphicsFactory graphicsFactory) {
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
		frame = new GeoCeDGLatexSettings(original, this, getApp(), support, "%",
				PhysicalLatexComposition::pstricks);
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
		code.append("\\psline").append(lineOptionCode(geo, true)).append("{<->}(")
				.append(format(a[0])).append(',').append(format(a[1])).append(")(")
				.append(format(b[0])).append(',').append(format(b[1])).append(")\n");
		endBeamer(code);
	}

	/** PRE-G9B-R6-plus-E2: the value of a native dimension, aligned and centred. */
	@Override
	protected void drawText(GeoText geo) {
		if (IsoABorderLatexExport.labelOwner(geo) != null) {
			drawSheetLabel(geo);
			return;
		}
		AlgoNativeDimension owner = DimensionLatexExport.dimensionTextOwner(geo);
		if (owner == null) {
			super.drawText(geo);
			return;
		}
		double[] place = DimensionLatexExport.placement(owner, xunit, yunit);
		if (place == null || !geo.isDefined()) {
			return;
		}
		String value = DimensionLatexExport.escapeTeX(geo.getTextStringSafe());
		GColor color = geo.getObjectColor();
		if (!color.equals(GColor.BLACK)) {
			StringBuilder name = new StringBuilder();
			colorCode(color, name);
			value = "\\textcolor{" + name + "}{" + value + "}";
		}
		startBeamer(code);
		// POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1: a rotation PSTricks can read
		String angle = Double.isFinite(place[2]) ? DimensionLatexExport.pstricksAngle(place[2])
				: format(place[2]);
		code.append("\\rput[b]{").append(angle).append("}(")
				.append(format(place[0])).append(',').append(format(place[1]))
				.append("){\\raisebox{0.5ex}{").append(value).append("}}\n");
		endBeamer(code);
	}

	/** PRE-G9B-R6-plus-E3-R1: the sheet label, up and left of its anchor. */
	private void drawSheetLabel(GeoText geo) {
		double[] at = IsoABorderLatexExport.anchor(geo);
		if (at == null || !geo.isDefined()) {
			return;
		}
		String value = IsoABorderLatexExport.teX(geo.getTextStringSafe());
		GColor color = geo.getObjectColor();
		if (!color.equals(GColor.BLACK)) {
			StringBuilder name = new StringBuilder();
			colorCode(color, name);
			value = "\\textcolor{" + name + "}{" + value + "}";
		}
		startBeamer(code);
		// an unstroked frame gives the text the 0.3333em clearance of a TikZ node
		code.append("\\rput[br](").append(format(at[0])).append(',').append(format(at[1]))
				.append("){\\psframebox[linestyle=none,framesep=0.3333em]{").append(value)
				.append("}}\n");
		endBeamer(code);
	}

	private void writePath(GeoLocusV2 locus, PolylineGeometry path) {
		startBeamer(code);
		code.append(path.isClosed() ? "\\pspolygon" : "\\psline");
		code.append(lineOptionCode(locus, true));
		List<Point2D> vertices = path.getVertices();
		for (Point2D vertex : vertices) {
			code.append('(').append(format(vertex.getX())).append(',')
					.append(format(vertex.getY())).append(')');
		}
		code.append('\n');
		endBeamer(code);
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.Collection;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geogebra.common.kernel.geos.GeoElement;

/** Reusable application service for neutral-model and DXF export. */
public final class GeometryExportService {

	private final GeoElementGeometryExportAdapter adapter;
	private final DxfExporter dxfExporter;

	/** Create the default G5 service. */
	public GeometryExportService() {
		this(new GeoElementGeometryExportAdapter(), new DxfExporter());
	}

	GeometryExportService(GeoElementGeometryExportAdapter adapter,
			DxfExporter dxfExporter) {
		this.adapter = adapter;
		this.dxfExporter = dxfExporter;
	}

	/**
	 * @param geos already selected source population
	 * @param selectionMode population provenance
	 * @return immutable neutral model
	 */
	public GeometryExportModel createModel(Collection<GeoElement> geos,
			SelectionMode selectionMode) {
		return adapter.adapt(geos, selectionMode);
	}

	/**
	 * @param model neutral model
	 * @return deterministic ASCII DXF
	 */
	public String exportDxf(GeometryExportModel model) {
		return dxfExporter.export(model);
	}
}

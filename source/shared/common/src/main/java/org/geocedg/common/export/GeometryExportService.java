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
	private final G9X1GeometryExportAdapter extendedAdapter;
	private final DxfExporter dxfExporter;

	/** Create the default G5 service. */
	public GeometryExportService() {
		this(new GeoElementGeometryExportAdapter(),
				new G9X1GeometryExportAdapter(), new DxfExporter());
	}

	GeometryExportService(GeoElementGeometryExportAdapter adapter,
			DxfExporter dxfExporter) {
		this(adapter, new G9X1GeometryExportAdapter(), dxfExporter);
	}

	GeometryExportService(GeoElementGeometryExportAdapter adapter,
			G9X1GeometryExportAdapter extendedAdapter, DxfExporter dxfExporter) {
		if (adapter == null || extendedAdapter == null || dxfExporter == null) {
			throw new IllegalArgumentException("Export services are required");
		}
		this.adapter = adapter;
		this.extendedAdapter = extendedAdapter;
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
	 * Performs the explicit G9X1 fidelity classification before destination
	 * selection or serialization. The legacy G5 API remains exact-only.
	 *
	 * @param geos ordered requested source population
	 * @param selectionMode population provenance
	 * @param request explicit approximation and work policy
	 * @return immutable complete-request preflight
	 */
	public GeometryExportPreflight preflight(Collection<GeoElement> geos,
			SelectionMode selectionMode, GeometryExportRequest request) {
		return extendedAdapter.preflight(geos, selectionMode, request);
	}

	/**
	 * @param model neutral model
	 * @return deterministic ASCII DXF
	 */
	public String exportDxf(GeometryExportModel model) {
		if (model != null && model.getModelVersion() >= 2) {
			throw new IllegalArgumentException(
					"G9X1 output requires its validated preflight path");
		}
		return dxfExporter.export(model);
	}

	/**
	 * Revalidates a strict preflight and encodes it without file-system access.
	 *
	 * @param preflight previously completed G9X1 preflight
	 * @return deterministic DXF text and actual entity-handle mappings
	 */
	public DxfEncodingResult encode(GeometryExportPreflight preflight) {
		if (preflight == null) {
			throw new IllegalArgumentException("Export preflight is required");
		}
		return dxfExporter.encode(preflight.requireWritableCurrentModel());
	}
}

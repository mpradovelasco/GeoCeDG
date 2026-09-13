/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.plugin.GeoClass;

/**
 * Typed population boundary for a complete 2D geometric DXF request.
 * Non-geometric construction participants are excluded before strict preflight;
 * current selection remains explicit and is never filtered.
 */
public final class GeometryExportPopulation2D {

	/** Versioned rule named in diagnostics and external evidence. */
	public static final String RULE_ID = "geocedg-dxf-geometric-2d/v1";

	/** Closed reasons currently authorized outside the geometric population. */
	public enum ExclusionReason {
		LIST_CONTAINER,
		NUMERIC_PARAMETER,
		RICH_ANALYSIS_RESULT,
		TEXT_WITHOUT_APPROVED_DXF_MAPPING
	}

	/** Immutable result of applying the population rule. */
	public static final class Result {
		private final List<GeoElement> sources;
		private final List<Diagnostic> diagnostics;

		private Result(List<GeoElement> sources, List<Diagnostic> diagnostics) {
			this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
			this.diagnostics = Collections.unmodifiableList(
					new ArrayList<>(diagnostics));
		}

		/** @return ordered eligible sources for strict geometry preflight */
		public List<GeoElement> getSources() {
			return sources;
		}

		/** @return typed records excluded before geometry preflight */
		public List<Diagnostic> getDiagnostics() {
			return diagnostics;
		}
	}

	private GeometryExportPopulation2D() {
	}

	/**
	 * Applies the typed population rule only to complete-construction requests.
	 * Unknown and potentially geometric families remain eligible so that an
	 * unsupported mapping still blocks the existing strict request.
	 *
	 * @param geos ordered construction or selection sources
	 * @param selectionMode requested source mode
	 * @return immutable eligible population and exclusion diagnostics
	 */
	public static Result select(Collection<GeoElement> geos,
			SelectionMode selectionMode) {
		if (geos == null || selectionMode == null) {
			throw new IllegalArgumentException(
					"Sources and selection mode are required");
		}
		List<GeoElement> sources = new ArrayList<>();
		List<Diagnostic> diagnostics = new ArrayList<>();
		int ordinal = 0;
		for (GeoElement geo : geos) {
			if (geo == null) {
				ordinal++;
				continue;
			}
			ExclusionReason reason = selectionMode == SelectionMode.COMPLETE_CONSTRUCTION
					? exclusionReason(geo.getGeoClassType()) : null;
			if (reason == null) {
				sources.add(geo);
			} else {
				diagnostics.add(new Diagnostic(
						GeoElementGeometryExportAdapter.sourceId(geo, ordinal),
						geo.getGeoClassType().name(),
						DiagnosticCode.OUTSIDE_GEOMETRIC_POPULATION,
						"Outside " + RULE_ID + ": " + reason + "."));
			}
			ordinal++;
		}
		return new Result(sources, diagnostics);
	}

	private static ExclusionReason exclusionReason(GeoClass type) {
		switch (type) {
		case LIST:
			return ExclusionReason.LIST_CONTAINER;
		case NUMERIC:
			return ExclusionReason.NUMERIC_PARAMETER;
		case LOCUS_INTERSECTION_RESULT:
			return ExclusionReason.RICH_ANALYSIS_RESULT;
		case TEXT:
			return ExclusionReason.TEXT_WITHOUT_APPROVED_DXF_MAPPING;
		default:
			return null;
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.List;

import org.geocedg.common.kernel.units.UnitState;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.desktop.util.CopyPasteD;

/**
 * PRE-G9B-R6-plus-D1 window copy buffer with transient unit provenance
 * (unit-system v1.0, section 11; DQ-D0-8, DQ-D1-7). The provenance is the source
 * document's unit state at the moment the buffer was replaced (copy, cut or Insert
 * File); it is session state beside the buffer, never clipboard XML, construction XML
 * or document state, and paste semantics never depend on it.
 */
final class GeoCeDGCopyPaste extends CopyPasteD {
	private final AppGeoCeDG owner;
	private UnitState provenance;

	GeoCeDGCopyPaste(AppGeoCeDG owner) {
		this.owner = owner;
	}

	@Override
	public void copyToXML(App app, List<GeoElement> geos, boolean putdown) {
		StringBuilder before = copiedXML;
		super.copyToXML(app, geos, putdown);
		if (copiedXML != before) {
			// the buffer was replaced: record where its numbers come from
			provenance = app.getKernel().getConstruction().getUnitSystem().getState();
		}
	}

	@Override
	public void clearClipboard() {
		super.clearClipboard();
		provenance = null;
	}

	@Override
	protected void onPasteCompleted(App app, boolean putdown) {
		owner.unitPasteCompleted(provenance, app);
	}

	/**
	 * @return provenance of the current buffer, or {@code null} when unavailable
	 */
	UnitState getProvenance() {
		return provenance;
	}
}

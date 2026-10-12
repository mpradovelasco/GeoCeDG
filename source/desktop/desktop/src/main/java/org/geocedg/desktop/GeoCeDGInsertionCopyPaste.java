/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.units.UnitState;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.desktop.util.CopyPasteD;

/**
 * PRE-G9B-R6-plus-F3 operation-scoped insertion buffer (P2, amending DQ-D1-7). One
 * instance serves exactly one Insert File operation and is discarded with it: the
 * window copy buffer and the global clipboard are never used or replaced. The kernel's
 * own construction copy ({@code insertFrom}) does the work. M1 (P3): the macros to
 * transfer are exactly the admitted transitive closure minus the digest-equal macros
 * the target already has, registered in the source's own (dependency) order before the
 * construction is evaluated; the inherited paste, which would add only the directly
 * used macros in an unordered set, then has nothing left to add. The unit state of the
 * inserted document is recorded by the same D1 provenance rule as the window buffer;
 * the notice is issued by the orchestration after a verified success, never here.
 */
final class GeoCeDGInsertionCopyPaste extends CopyPasteD {
	private final Set<String> transferredMacros;
	private UnitState provenance;

	/**
	 * @param transferredMacros admitted macros to transfer, in dependency order
	 */
	GeoCeDGInsertionCopyPaste(Set<String> transferredMacros) {
		this.transferredMacros = transferredMacros;
	}

	@Override
	public void copyToXML(App app, List<GeoElement> geos, boolean putdown) {
		StringBuilder before = copiedXML;
		super.copyToXML(app, geos, putdown);
		if (copiedXML != before) {
			// the same D1 rule as the window buffer: where the numbers come from
			provenance = app.getKernel().getConstruction().getUnitSystem().getState();
		}
	}

	@Override
	public void pasteFromXML(App app, boolean putdown) {
		if (copiedMacros != null && !copiedMacros.isEmpty() && copySource != null) {
			App source = copySource.getApplication();
			List<Macro> transfer = new ArrayList<>();
			for (String name : transferredMacros) {
				Macro macro = source.getKernel().getMacro(name);
				if (macro == null) {
					throw new IllegalStateException("admitted macro missing in source: " + name);
				}
				if (app.getKernel().getMacro(name) == null) {
					transfer.add(macro);
				}
			}
			if (!transfer.isEmpty()) {
				try {
					app.addMacroXML(source.getXMLio().getFullMacroXML(new ArrayList<>(transfer)));
				} catch (Exception failure) {
					throw new IllegalStateException("macro transfer failed", failure);
				}
			}
			copiedMacros.clear();
		}
		super.pasteFromXML(app, putdown);
	}

	/** @return unit state of the inserted document, or null before the copy */
	UnitState getProvenance() {
		return provenance;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import org.geogebra.common.main.App;

/**
 * Document operations on the unit state (unit-system v1.0, section 7.1): one validated
 * change, exactly one undo point, and the document marked modified. An operation whose
 * result equals the current state, or that is rejected, changes nothing and stores no
 * undo point; the Desktop undo manager never de-duplicates snapshots, so the comparison
 * happens here.
 */
public final class UnitDocumentOperations {

	private UnitDocumentOperations() {
	}

	/**
	 * @param app application of the document
	 * @param next requested state, already valid by construction
	 * @return whether the state changed (and one undo point was stored)
	 */
	public static boolean commit(App app, UnitState next) {
		DocumentUnitSystem units = app.getKernel().getConstruction().getUnitSystem();
		if (!units.replace(next)) {
			return false;
		}
		app.storeUndoInfo();
		app.setUnsaved();
		return true;
	}
}

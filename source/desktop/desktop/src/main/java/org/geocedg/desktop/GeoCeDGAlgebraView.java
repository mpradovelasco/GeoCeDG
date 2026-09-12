/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.gui.view.algebra.AlgebraControllerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;

/** Classic Algebra view exposing the approved semantic redefine entry point. */
final class GeoCeDGAlgebraView extends AlgebraViewD {
	private static final long serialVersionUID = 1L;
	private final AppGeoCeDG app;

	GeoCeDGAlgebraView(AlgebraControllerD controller, AppGeoCeDG app) {
		super(controller);
		this.app = app;
	}

	@Override
	public void startEditItem(GeoElement geo) {
		if (GeoCeDGDefinitionInspector.isSemanticRedefineEnabled(geo)) {
			app.getDialogManager().showRedefineDialog(geo, true);
			return;
		}
		super.startEditItem(geo);
	}
}

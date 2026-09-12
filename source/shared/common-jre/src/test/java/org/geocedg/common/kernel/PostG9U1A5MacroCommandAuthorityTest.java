/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.dialog.ToolManagerDialogModel;
import org.geogebra.common.gui.dialog.ToolManagerDialogModel.ToolManagerDialogListener;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/** Shared-kernel command binding authority for POST-G9U1-A5. */
class PostG9U1A5MacroCommandAuthorityTest extends BaseUnitTest {

	@Test
	void explicitDocumentBindingCannotBeReplacedByLaterSameNameRegistration()
			throws Exception {
		GeoElement first = add("A=(0,0)");
		GeoElement second = add("B=(2,0)");
		GeoElement midpoint = add("M=Midpoint(A,B)");
		GeoElement line = add("g=Line(A,B)");
		Macro document = new Macro(getKernel(), "SameName",
				new GeoElement[] {first, second}, new GeoElement[] {midpoint});
		Macro persistent = new Macro(getKernel(), "SameName",
				new GeoElement[] {first, second}, new GeoElement[] {line});

		getKernel().addMacro(document);
		getKernel().bindMacroCommandAuthority(document);
		getKernel().addMacro(persistent);

		assertSame(document, getKernel().getMacro("SameName"));
		assertTrue(getKernel().isMacroCommandAuthority(document));
		assertFalse(getKernel().isMacroCommandAuthority(persistent));

		getKernel().removeMacro(persistent);
		assertSame(document, getKernel().getMacro("SameName"));
		getKernel().removeMacro(document);
		assertNull(getKernel().getMacro("SameName"));

		getKernel().addMacro(persistent);
		assertSame(persistent, getKernel().getMacro("SameName"));
		assertFalse(getKernel().isMacroCommandAuthority(persistent));
	}

	@Test
	void toolManagerRenameAndReorderPreserveExplicitDocumentAuthority()
			throws Exception {
		GeoElement first = add("A=(0,0)");
		GeoElement second = add("B=(2,0)");
		GeoElement midpoint = add("M=Midpoint(A,B)");
		Macro document = new Macro(getKernel(), "OriginalName",
				new GeoElement[] {first, second}, new GeoElement[] {midpoint});
		Macro removed = new Macro(getKernel(), "RemovedName",
				new GeoElement[] {first, second}, new GeoElement[] {midpoint});
		Macro companion = new Macro(getKernel(), "CompanionName",
				new GeoElement[] {first, second}, new GeoElement[] {midpoint});
		getKernel().addMacro(document);
		getKernel().addMacro(removed);
		getKernel().addMacro(companion);
		getKernel().bindMacroCommandAuthority(document);
		getKernel().bindMacroCommandAuthority(removed);

		assertTrue(getKernel().setMacroCommandName(document, "RenamedDocument"));
		assertNull(getKernel().getMacro("OriginalName"));
		assertSame(document, getKernel().getMacro("RenamedDocument"));
		assertTrue(getKernel().isMacroCommandAuthority(document));

		ToolManagerDialogModel model = new ToolManagerDialogModel(getApp(),
				new ToolManagerDialogListener() {
					@Override
					public void removeMacroFromToolbar(int mode) {
						// Presentation-only callback is irrelevant to command authority.
					}

					@Override
					public void refreshCustomToolsInToolBar() {
						// Presentation-only callback is irrelevant to command authority.
					}
				});
		model.addMacros(new Object[] {companion, document});
		assertNull(getKernel().getMacro("OriginalName"));
		assertNull(getKernel().getMacro("RemovedName"));
		assertSame(companion, getKernel().getMacro(0));
		assertSame(document, getKernel().getMacro(1));
		assertSame(document, getKernel().getMacro("RenamedDocument"));
		assertTrue(getKernel().isMacroCommandAuthority(document));
		assertFalse(getKernel().isMacroCommandAuthority(companion));

		Macro nameReuse = new Macro(getKernel(), "RemovedName",
				new GeoElement[] {first, second}, new GeoElement[] {midpoint});
		getKernel().addMacro(nameReuse);
		assertSame(nameReuse, getKernel().getMacro("RemovedName"));
		assertFalse(getKernel().isMacroCommandAuthority(nameReuse));
		getKernel().removeMacro(document);
		assertNull(getKernel().getMacro("RenamedDocument"));
		assertFalse(getKernel().isMacroCommandAuthority(document));
	}
}

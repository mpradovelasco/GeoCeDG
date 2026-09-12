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
}

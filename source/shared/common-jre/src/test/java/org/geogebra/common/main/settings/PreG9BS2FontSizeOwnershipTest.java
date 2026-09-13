/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.main.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geogebra.common.util.Util;
import org.junit.jupiter.api.Test;

class PreG9BS2FontSizeOwnershipTest {

	@Test
	void tenPointsIsAConstructionSizeButNotAnApplicationMenuSize() {
		assertEquals(10, Util.appFontSizes(0));
		assertEquals(12, Util.menuFontSizes(0));
		assertEquals(10, Util.getValidFontSize(10));
		assertTrue(Util.appFontSizesLength() > Util.menuFontSizesLength());
	}

}

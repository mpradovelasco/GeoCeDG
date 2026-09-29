/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;

/**
 * The ordinary clipboard closure in a GeoCeDG document whose geos are all
 * non-participating: the generic host route is used and no identity appears.
 */
class PreG9bR3C1U1CedgClipboardClosureTest extends PreG9bR3C1U1OrdinaryClosureTestBase {
	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}
}

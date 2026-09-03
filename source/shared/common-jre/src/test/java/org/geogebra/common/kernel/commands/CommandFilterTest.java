/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 * 
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

// GeoCeDG modification (2026): test Classic-OFF and explicit V2 opt-in contracts.

package org.geogebra.common.kernel.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.selector.CommandFilter;
import org.geogebra.common.kernel.commands.selector.CommandFilterFactory;
import org.geogebra.common.main.App;
import org.geogebra.test.commands.AlgebraTestHelper;
import org.geogebra.test.commands.CommandSignatures;
import org.junit.jupiter.api.Test;

class CommandFilterTest extends BaseUnitTest {

	private static final List<Commands> GEOCEDG_COMMANDS =
			List.of(Commands.LocusV2, Commands.LocusLength, Commands.SplineV2);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void noCasFilterTest() {
		checkNoCasFilter(getApp(), false);
	}

	@Test
	void noCasFilterWithGeoCeDGOptInTest() {
		checkNoCasFilter(AppCommonFactory.create3D(new AppConfigGeoCeDG(true)), true);
	}

	private void checkNoCasFilter(App app, boolean locusV2Enabled) {
		CommandFilter cf = CommandFilterFactory
				.createNoCasCommandFilter();
		app.getKernel().getAlgebraProcessor().addCommandFilter(cf);
		for (Commands cmd0 : Commands.values()) {
			Commands cmd = cmd0;
			if (cmd0 == Commands.Derivative) {
				cmd = Commands.NDerivative;
			}
			if (cmd0 == Commands.Integral || cmd0 == Commands.IntegralBetween
					|| cmd0 == Commands.NIntegral
					|| cmd0 == Commands.Factors
					|| cmd0 == Commands.Polyhedron
					|| AlgebraTestHelper.internalCAScommand(cmd0)) {
				continue;
			}
			if (GEOCEDG_COMMANDS.contains(cmd)) {
				assertTrue(cf.isCommandAllowed(cmd), cmd.name());
				assertNotNull(CommandSignatures.getSignature(cmd.name(), app), cmd.name());
			}
			if (GEOCEDG_COMMANDS.contains(cmd) && !locusV2Enabled) {
				AlgebraTestHelper.shouldFail(cmd + "()",
						app.getLocalization().getMenu("LocusV2.FeatureDisabled"), app);
			} else if (cf.isCommandAllowed(cmd)) {
				List<Integer> signature = CommandSignatures
						.getSignature(cmd.name(), app);
				if (signature != null && !signature.contains(0)) {
					AlgebraTestHelper.shouldFail(cmd + "()", "number of arg",
							"only",
							app);
				}
			} else {
				AlgebraTestHelper.shouldFail(cmd + "()", "Unknown command",
						"only", app);
			}

		}
	}

	@Test
	void noCasCommandsInSuiteAndClassic() {
		List<String> integralsClassic = getApp().getCommandDictionary().getCompletions("Integ")
				.stream().map(c -> c.content).collect(Collectors.toList());
		// should not contain IntegralSymbolic
		assertEquals(Arrays.asList("Integral", "IntegralBetween",
				"IsInteger", "NIntegral"), integralsClassic);
	}
}

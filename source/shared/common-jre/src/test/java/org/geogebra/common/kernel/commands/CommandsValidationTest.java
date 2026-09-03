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

import static org.geogebra.common.kernel.commands.CommandsConstants.TABLE_CAS;
import static org.geogebra.test.commands.AlgebraTestHelper.shouldFail;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.main.App;
import org.geogebra.test.commands.CommandSignatures;
import org.junit.jupiter.api.Test;

class CommandsValidationTest extends BaseUnitTest {

	private static final List<Commands> GEOCEDG_COMMANDS =
			List.of(Commands.LocusV2, Commands.LocusLength, Commands.SplineV2);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void testArgumentTypeValidation() {
		checkArgumentTypeValidation(getApp(), false);
	}

	@Test
	void testArgumentTypeValidationWithGeoCeDGOptIn() {
		checkArgumentTypeValidation(
				AppCommonFactory.create3D(new AppConfigGeoCeDG(true)), true);
	}

	private void checkArgumentTypeValidation(App app, boolean locusV2Enabled) {
		for (Commands command: Commands.values()) {
			List<Integer> signature = CommandSignatures.getSignature(command.name(), app);
			if (GEOCEDG_COMMANDS.contains(command)) {
				assertNotNull(signature, command.name());
			}
			if (signature != null && command.getTable() != TABLE_CAS
					&& !acceptsAnyArgType(command)) {
				checkArgumentTypeValidation(command.name(), signature, app,
						GEOCEDG_COMMANDS.contains(command) && !locusV2Enabled);
			}
		}
	}

	@Test
	void testArgumentNumberValidation() {
		checkArgumentNumberValidation(getApp(), false);
	}

	@Test
	void testArgumentNumberValidationWithGeoCeDGOptIn() {
		checkArgumentNumberValidation(
				AppCommonFactory.create3D(new AppConfigGeoCeDG(true)), true);
	}

	private void checkArgumentNumberValidation(App app, boolean locusV2Enabled) {
		for (Commands command: Commands.values()) {
			List<Integer> signature = CommandSignatures.getSignature(command.name(), app);
			if (GEOCEDG_COMMANDS.contains(command)) {
				assertNotNull(signature, command.name());
			}
			if (signature != null && command.getTable() != TABLE_CAS
				&& command != Commands.PenStroke
				&& command != Commands.SelectObjects
				&& command != Commands.StartAnimation) {
				checkArgumentNumberValidation(command.name(), signature, app,
						GEOCEDG_COMMANDS.contains(command) && !locusV2Enabled);
			}
		}
	}

	@Test
	void testCasTableValidation() {
		for (Commands command: Commands.values()) {
			if (command.getTable() == TABLE_CAS && command != Commands.SolveQuartic) {
				shouldFail(command.name() + "()", "available only in the CAS", getApp());
			}
		}
	}

	private void checkArgumentTypeValidation(String cmdName,
			List<Integer> signature, App app, boolean featureDisabled) {
		for (int args : signature) {
			StringBuilder withArgs = new StringBuilder(cmdName).append("(");
			for (int i = 0; i < args - 1; i++) {
				withArgs.append("space,");
			}
			withArgs.append("space)");
			if (args > 0) {
				if (featureDisabled) {
					shouldFail(withArgs.toString(),
							app.getLocalization().getMenu("LocusV2.FeatureDisabled"), app);
				} else {
					shouldFail(withArgs.toString(), "arg", "IllegalArgument:", app);
				}
			}
		}
	}

	private boolean acceptsAnyArgType(Commands cmdName) {
		return List.of(Commands.Delete,
				Commands.ConstructionStep,
				Commands.Text,
				Commands.LaTeX,
				Commands.RunClickScript,
				Commands.RunUpdateScript,
				Commands.Defined,
				Commands.AreEqual,
				Commands.AreCongruent,
				Commands.Textfield,
				Commands.GetTime,
				Commands.CopyFreeObject,
				Commands.Name,
				Commands.Relation,
				Commands.SelectObjects,
				Commands.Dot, Commands.Cross,
				Commands.SetConstructionStep,
				Commands.TableText,
				Commands.SetValue,
				Commands.Row,
				Commands.Column,
				Commands.ColumnName
		).contains(cmdName);
	}

	private void checkArgumentNumberValidation(String cmdName,
			List<Integer> signature, App app, boolean featureDisabled) {
		if (featureDisabled) {
			shouldFail(cmdName + "()",
					app.getLocalization().getMenu("LocusV2.FeatureDisabled"), app);
		} else if (!signature.contains(0)) {
			shouldFail(cmdName + "()", "Illegal number of arguments: 0",
					"IllegalArgumentNumber", app);
		} else {
			shouldFail(cmdName + "(space,space,space,space,space,space,space,space,space)",
					"Illegal number of arguments: 9", "IllegalArgumentNumber", app);
		}
	}
}

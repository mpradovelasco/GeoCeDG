/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.ScheduledPreviewFromInputBar;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/** Product-only input/view policy; no new geometry or command semantics. */
class G9U1InputPolicyTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void previewNeverCreatesCommandsNestedInputsOrParameters() {
		add("A=(0,0)");
		String original = getApp().getXML();
		ScheduledPreviewFromInputBar preview = getKernel().getInputPreviewHelper();
		for (String input : new String[] {"k=2", "B=(1,2)", "Line(A,(3,4))",
				"SplineV2({(0,0),(1,2),(3,0)},3)", "Intersect(xAxis,yAxis)",
				"SetValue(A,(5,6))", "Length(SplineV2({(0,0),(1,0),(2,0)},3))"}) {
			preview.updatePreviewFromInputBar(input, new ErrorAccumulator());
			preview.run();
			assertNull(preview.getPreview(input));
			assertEquals(original, getApp().getXML(), input);
		}
		assertNull(lookup("k"));
		assertNull(lookup("B"));
	}

	@Test
	void previewRedefineAndSliderCleanupDoNotMutateExistingObjects() {
		GeoNumeric numeric = add("k=1");
		add("A=(k,0)");
		String xml = getApp().getXML();
		ScheduledPreviewFromInputBar preview = getKernel().getInputPreviewHelper();
		preview.addSliders("k");
		preview.updatePreviewFromInputBar("k=0.25", new ErrorAccumulator());
		preview.run();
		preview.clear();
		assertEquals(1, numeric.getDouble());
		assertEquals(xml, getApp().getXML());
	}

	@Test
	void syntaxFailureAndEscapeLeaveNoProductiveState() {
		add("A=(1,2)");
		String xml = getApp().getXML();
		ErrorAccumulator errors = new ErrorAccumulator();
		ScheduledPreviewFromInputBar preview = getKernel().getInputPreviewHelper();
		preview.updatePreviewFromInputBar("a=2", errors);
		preview.updatePreviewFromInputBar("a=", errors);
		preview.run();
		assertFalse(errors.getErrorsSinceReset().isEmpty());
		preview.clear();
		assertEquals(xml, getApp().getXML());
		assertNull(lookup("a"));
	}

	@Test
	void explicitOrdinaryCommandStillCreatesExactlyOneRequestedObject() {
		ScheduledPreviewFromInputBar preview = getKernel().getInputPreviewHelper();
		preview.updatePreviewFromInputBar("k=0.25", new ErrorAccumulator());
		preview.run();
		int before = getConstruction().getGeoSetConstructionOrder().size();
		GeoNumeric numeric = add("k=0.25");
		assertNotNull(numeric);
		assertEquals(0.25, numeric.getDouble());
		assertEquals(before + 1, getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void continuityCannotBeEnabledInProduct() {
		getKernel().setContinuous(true);
		assertFalse(getKernel().isContinuous());
		getKernel().setContinuous(false);
		assertFalse(getKernel().isContinuous());
	}

	@Test
	void loadedContinuousPreferenceCannotOverrideProductInvariant() {
		add("A=(0,0)");
		String original = getApp().getXML();
		assertTrue(original.contains("<continuous val=\"false\""));
		String xml = original.replace("<continuous val=\"false\"",
				"<continuous val=\"true\"");
		getApp().setXML(xml, true);
		assertFalse(getKernel().isContinuous());
		assertNotNull(lookup("A"));
	}

	@Test
	void classicContinuityRemainsConfigurable() {
		AppCommon classic = AppCommonFactory.create();
		classic.getKernel().setContinuous(true);
		assertTrue(classic.getKernel().isContinuous());
		classic.getKernel().setContinuous(false);
		assertFalse(classic.getKernel().isContinuous());
	}
}

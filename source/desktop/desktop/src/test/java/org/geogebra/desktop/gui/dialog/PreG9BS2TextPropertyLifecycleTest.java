/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.desktop.gui.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.main.LocalizationD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PreG9BS2TextPropertyLifecycleTest {

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void openEditCancelDiscardsDraftAndCloses() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("B");
		scenario.lifecycle.cancel();
		assertEquals("A", scenario.editor.committed);
		assertEquals("A", scenario.editor.draft);
		assertEquals(0, scenario.editor.commits);
		assertEquals(1, scenario.closes);
	}

	@Test
	void openEditOkCommitsAndCloses() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("B");
		scenario.lifecycle.ok();
		assertEquals("B", scenario.editor.committed);
		assertEquals(1, scenario.editor.commits);
		assertEquals(1, scenario.closes);
	}

	@Test
	void openEditApplyCommitsAndStaysOpen() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("B");
		scenario.lifecycle.apply();
		assertEquals("B", scenario.editor.committed);
		assertEquals(1, scenario.editor.commits);
		assertEquals(0, scenario.closes);
	}

	@Test
	void applyFurtherEditCancelRetainsAppliedBaseline() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("B");
		scenario.lifecycle.apply();
		scenario.editor.edit("C");
		scenario.lifecycle.cancel();
		assertEquals("B", scenario.editor.committed);
		assertEquals("B", scenario.editor.draft);
		assertEquals(1, scenario.editor.commits);
		assertEquals(1, scenario.closes);
	}

	@Test
	void applyFurtherEditOkCommitsSecondDraftAndCloses() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("B");
		scenario.lifecycle.apply();
		scenario.editor.edit("C");
		scenario.lifecycle.ok();
		assertEquals("C", scenario.editor.committed);
		assertEquals(2, scenario.editor.commits);
		assertEquals(1, scenario.closes);
	}

	@Test
	void selectionChangeAndWindowCloseDiscardOnlyPendingDraft() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("B");
		scenario.lifecycle.apply();
		scenario.editor.edit("selection draft");
		scenario.lifecycle.discardDraft();
		assertEquals("B", scenario.editor.draft);
		scenario.editor.edit("window draft");
		scenario.lifecycle.discardDraft();
		assertEquals("B", scenario.editor.committed);
		assertEquals("B", scenario.editor.draft);
		assertEquals(0, scenario.closes);
	}

	@Test
	void failedOkKeepsPropertiesOpenAndDraftAvailable() {
		Scenario scenario = new Scenario("A");
		scenario.editor.edit("invalid");
		scenario.editor.nextCommitSucceeds = false;
		scenario.lifecycle.ok();
		assertEquals("A", scenario.editor.committed);
		assertEquals("invalid", scenario.editor.draft);
		assertEquals(0, scenario.closes);
	}

	@Test
	void normalRedefineCompletesOnceAndPreservesObject() {
		AppDNoGui app = app();
		GeoText original = text(app, "t=\"A\"");
		AtomicInteger callbacks = new AtomicInteger();
		AtomicReference<GeoText> result = new AtomicReference<>();
		ErrorAccumulator errors = new ErrorAccumulator();
		TextInputDialogD.redefineText(app, original, "\"B\"", false, errors,
				geo -> {
					callbacks.incrementAndGet();
					result.set(geo);
				});
		assertEquals("", errors.getErrors());
		assertEquals(1, callbacks.get());
		assertNotNull(result.get());
		assertSame(original, result.get());
		assertEquals("B", text(app, "t").getTextString());
	}

	@Test
	void committedTextSurvivesNativeXmlReopen() throws Exception {
		AppDNoGui app = app();
		GeoText original = text(app, "t=\"A\"");
		AtomicReference<GeoText> result = new AtomicReference<>();
		TextInputDialogD.redefineText(app, original, "\"B\"", false,
				new ErrorAccumulator(), result::set);
		assertNotNull(result.get());
		String xml = app.getXML();
		AppDNoGui reopened = app();
		reopened.setXML(xml, true);
		assertEquals("B", text(reopened, "t").getTextString());
	}

	private static AppDNoGui app() {
		return new AppDNoGui(new LocalizationD(3), true, new AppConfigGeoCeDG(true));
	}

	private static GeoText text(AppDNoGui app, String commandOrLabel) {
		if (!commandOrLabel.contains("=")) {
			return (GeoText) app.getKernel().lookupLabel(commandOrLabel);
		}
		return (GeoText) app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(commandOrLabel, false)[0];
	}

	private static final class Scenario {
		private final Draft editor;
		private final PropertiesTextEditLifecycle lifecycle;
		private int closes;

		private Scenario(String baseline) {
			editor = new Draft(baseline);
			lifecycle = new PropertiesTextEditLifecycle(editor, () -> closes++);
		}
	}

	private static final class Draft implements PropertiesTextEditLifecycle.DraftEditor {
		private String committed;
		private String draft;
		private int commits;
		private boolean nextCommitSucceeds = true;

		private Draft(String baseline) {
			committed = baseline;
			draft = baseline;
		}

		private void edit(String value) {
			draft = value;
		}

		@Override
		public void commit(AsyncOperation<Boolean> callback) {
			commits++;
			if (nextCommitSucceeds) {
				committed = draft;
			}
			callback.callback(nextCommitSucceeds);
			nextCommitSucceeds = true;
		}

		@Override
		public void discard() {
			draft = committed;
		}
	}
}

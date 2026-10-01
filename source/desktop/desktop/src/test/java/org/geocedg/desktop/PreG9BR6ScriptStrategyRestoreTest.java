/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6ScriptHarness.SPANISH;
import static org.geocedg.desktop.PreG9BR6ScriptHarness.submit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.geocedg.desktop.PreG9BR6ScriptHarness.Sink;
import org.geogebra.common.kernel.CommandLookupStrategy;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.script.GgbScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6, {@code TD-R0-GGBSCRIPT-STRATEGY-RESTORE} and its nested
 * {@code Execute} analogue: a script or {@code Execute} restores the previous
 * command lookup strategy after success or any throwable, and an escaping
 * throwable is a failed line, never a successful script.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6ScriptStrategyRestoreTest {

	/** An arbitrary failure of the error presentation itself. */
	private static final class PresentationFailure extends Error {
		private static final long serialVersionUID = 1L;

		PresentationFailure() {
			super("injected presentation failure");
		}
	}

	@Test
	void placeholderFailureRestoresStrategyBeforeLaterSpanishAlgebraInput() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(SPANISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			eval(app, "n=1");
			eval(app, "m=0");
			GeoInputBox box = (GeoInputBox) eval(app, "box=InputBox(n)");
			box.setClickScript(new GgbScript(app, "SetValue(m, %0)"));
			// Typed text that does not evaluate stays the box text and becomes %0.
			box.updateLinkedGeo("5$");

			// Pressing Enter: the regex replacement of %0 fails on '$' inside the run.
			assertThrows(IllegalArgumentException.class, box::textSubmitted);

			assertEquals(CommandLookupStrategy.USER,
					app.getKernel().getCommandLookupStrategy());
			GeoElementND[] circle = submit(app, "c=Circunferencia((0,0),1)", sink);
			assertNotNull(circle, sink.joined());
			assertEquals(Commands.Circle,
					circle[0].toGeoElement().getParentAlgorithm().getClassName());
		}
	}

	@Test
	void escapingThrowableIsAFailedLineNotScriptSuccess() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			GeoElement button = eval(app, "btn=Button(\"run\")");
			Event click = new Event(EventType.CLICK, button);
			assertFalse(new GgbScript(app, "Foo(1)").run(click));

			sink.failWith(new PresentationFailure());
			boolean success = new GgbScript(app, "Foo(1)\nk=1").run(click);

			assertFalse(success, "an escaping throwable must not report script success");
			assertNotNull(app.getKernel().lookupLabel("k"), "later lines still run");
			assertEquals(CommandLookupStrategy.USER,
					app.getKernel().getCommandLookupStrategy());
		}
	}

	@Test
	void escapingThrowableInAnUpdateScriptKeepsUpdateScriptsBlocked() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			GeoNumeric trigger = (GeoNumeric) eval(app, "m=0");
			trigger.setUpdateScript(new GgbScript(app, "Foo(1)"));
			sink.failWith(new PresentationFailure());
			app.setBlockUpdateScripts(false);

			trigger.setValue(1);
			trigger.updateRepaint();

			assertTrue(app.isBlockUpdateScripts(),
					"a failed update script blocks later update scripts");
			assertEquals(CommandLookupStrategy.USER,
					app.getKernel().getCommandLookupStrategy());
		}
	}

	@Test
	void everyScriptRunRestoresThePreviousStrategy() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			Kernel kernel = app.getKernel();
			GeoElement button = eval(app, "btn=Button(\"run\")");
			Event click = new Event(EventType.CLICK, button);
			int index = 0;
			for (CommandLookupStrategy previous : CommandLookupStrategy.values()) {
				kernel.setCommandLookupStrategy(previous);
				String label = "c" + index++;
				// Lower-case English resolves only under the SCRIPT strategy.
				assertTrue(new GgbScript(app, label + "=circle((0,0),1)").run(click),
						previous + " " + sink.joined());
				assertNotNull(kernel.lookupLabel(label), previous.toString());
				assertEquals(previous, kernel.getCommandLookupStrategy());

				sink.failWith(new PresentationFailure());
				assertFalse(new GgbScript(app, "Foo(1)").run(click), previous.toString());
				assertEquals(previous, kernel.getCommandLookupStrategy());
				sink.failWith(null);
			}
			kernel.setCommandLookupStrategy(CommandLookupStrategy.USER);
		}
	}

	@Test
	void executeRestoresTheStrategyAfterAnEscapingError() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(SPANISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			Kernel kernel = app.getKernel();
			GeoElement button = eval(app, "btn=Button(\"run\")");
			sink.failWith(new PresentationFailure());

			assertThrows(PresentationFailure.class,
					() -> submit(app, "Execute({\"Foo(1)\"})", sink));
			assertEquals(CommandLookupStrategy.USER, kernel.getCommandLookupStrategy());

			boolean success = new GgbScript(app, "Execute({\"Foo(1)\"})\nc=circle((0,0),1)")
					.run(new Event(EventType.CLICK, button));
			assertFalse(success);
			assertNotNull(kernel.lookupLabel("c"),
					"the next script line runs under SCRIPT, not a leaked XML strategy");
			assertEquals(CommandLookupStrategy.USER, kernel.getCommandLookupStrategy());

			sink.failWith(null);
			assertNotNull(submit(app, "d=Circunferencia((0,0),2)", sink), sink.joined());
		}
	}

	@Test
	void recursiveExecuteOverflowRestoresTheUserStrategy() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(SPANISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			Kernel kernel = app.getKernel();
			eval(app, "Lr={\"Execute(Lr)\"}");

			assertThrows(StackOverflowError.class, () -> submit(app, "Execute(Lr)", sink));

			assertEquals(CommandLookupStrategy.USER, kernel.getCommandLookupStrategy());
			assertNull(kernel.lookupLabel("c"));
			assertNotNull(submit(app, "c=Circunferencia((0,0),1)", sink), sink.joined());
		}
	}
}

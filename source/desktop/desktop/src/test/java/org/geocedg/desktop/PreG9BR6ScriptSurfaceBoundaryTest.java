/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.geocedg.desktop.PreG9BR6ScriptHarness.SPANISH;
import static org.geocedg.desktop.PreG9BR6ScriptHarness.constructionXml;
import static org.geocedg.desktop.PreG9BR6ScriptHarness.submit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.desktop.PreG9BR6ScriptHarness.Sink;
import org.geogebra.common.gui.dialog.options.model.ScriptInputModel;
import org.geogebra.common.kernel.CommandLookupStrategy;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.ScriptType;
import org.geogebra.common.plugin.script.GgbScript;
import org.geogebra.common.plugin.script.Script;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6 surface semantics recorded by the capability matrix: the deliberate
 * Algebra-input / GGBScript asymmetries, undo storage per surface, the inherited
 * result contract of nested {@code Execute}, and the script-editor boundary across
 * UI languages under the final R5-B naming policy.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6ScriptSurfaceBoundaryTest {

	private static final String[] SPLINE_LINE = {"A=(-2,0)", "B=(0,0)", "C=(2,0)",
			"S=SplineV2({A,B,C},3)", "g:x=0"};

	@Test
	void scriptsDoNotInheritAlgebraInputOrchestration() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			for (String statement : SPLINE_LINE) {
				eval(app, statement);
			}
			GeoElement button = eval(app, "btn=Button(\"run\")");
			GeoCeDGEuclidianController controller = controller(app);
			assertFalse(controller.isAutoMaterializeIntersectionSolutions(), "off by default");
			controller.setAutoMaterializeIntersectionSolutions(true);
			long points = points(app);

			assertNotNull(submit(app, "R=Intersect(S,g)", sink), sink.joined());
			GeoLocusIntersectionResult typed = (GeoLocusIntersectionResult) lookup(app, "R");
			assertSame(typed, controller.getIntersectionSession().getActive());
			long materialized = points(app) - points;
			assertTrue(materialized > 0, "explicit Algebra Input materializes the opt-in points");

			button.setClickScript(new GgbScript(app, "Q=Intersect(S,g)"));
			button.runClickScripts(null);
			assertInstanceOf(GeoLocusIntersectionResult.class, lookup(app, "Q"));
			assertEquals(materialized, points(app) - points, "scripts materialize nothing");
			assertSame(typed, controller.getIntersectionSession().getActive(),
					"a script never activates the Desktop intersection session");
		}
	}

	@Test
	void undoStorageDiffersBySurface() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			for (String statement : SPLINE_LINE) {
				eval(app, statement);
			}
			final GeoElement button = eval(app, "btn=Button(\"run\")");
			final GeoNumeric trigger = (GeoNumeric) eval(app, "m=0");
			UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
			AtomicReference<CountDownLatch> stored = new AtomicReference<>();
			AtomicInteger stores = new AtomicInteger();
			undo.addUndoInfoStoredListener(() -> {
				stores.incrementAndGet();
				stored.get().countDown();
			});
			baseline(undo);
			final String fixture = constructionXml(app);

			// Algebra Input: one undo step per successful submission.
			stored.set(new CountDownLatch(1));
			assertNotNull(submit(app, "M=LocusLength(S)", sink), sink.joined());
			assertTrue(stored.get().await(10, TimeUnit.SECONDS));
			String afterSubmission = constructionXml(app);
			stored.set(new CountDownLatch(1));
			assertNotNull(submit(app, "N=Length(S)", sink), sink.joined());
			assertTrue(stored.get().await(10, TimeUnit.SECONDS));
			app.getKernel().undo();
			await(() -> afterSubmission.equals(constructionXml(app)));
			app.getKernel().undo();
			await(() -> fixture.equals(constructionXml(app)));

			// Click script: one undo step for all changing lines of one run.
			baseline(undo);
			stored.set(new CountDownLatch(1));
			stores.set(0);
			button.setClickScript(new GgbScript(app, "M=LocusLength(S)\nN=Length(S)"));
			String scripted = constructionXml(app);
			button.runClickScripts(null);
			assertTrue(stored.get().await(10, TimeUnit.SECONDS));
			assertNotNull(lookup(app, "M"));
			assertNotNull(lookup(app, "N"));
			app.getKernel().undo();
			await(() -> scripted.equals(constructionXml(app)));
			assertEquals(1, stores.get());

			// A failing click script that changes nothing stores no undo step.
			baseline(undo);
			stored.set(new CountDownLatch(1));
			button.setClickScript(new GgbScript(app, "Foo(1)"));
			String failing = constructionXml(app);
			button.runClickScripts(null);
			assertFalse(stored.get().await(1500, TimeUnit.MILLISECONDS));
			assertEquals(failing, constructionXml(app));

			// Update scripts never store undo; only click runs do (GeoScriptRunner).
			stored.set(new CountDownLatch(1));
			trigger.setUpdateScript(new GgbScript(app, "U=Length(S)"));
			trigger.setValue(1);
			trigger.updateRepaint();
			assertNotNull(lookup(app, "U"));
			assertFalse(stored.get().await(1500, TimeUnit.MILLISECONDS));
		}
	}

	@Test
	void executeKeepsItsInheritedResultContract() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(SPANISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			for (String statement : new String[] {"A=(-2,0)", "B=(0,1)", "C=(2,0)",
					"S=SplineV2({A,B,C})", "ax=Line((0,0,0),(0,0,1))"}) {
				eval(app, statement);
			}
			GeoElement button = eval(app, "btn=Button(\"run\")");
			Event click = new Event(EventType.CLICK, button);
			String illegal = app.getLocalization().getError("IllegalArgument");
			String before = constructionXml(app);

			assertFalse(new GgbScript(app, "X=Rotate(S,0.5,ax)").run(click));
			assertTrue(sink.joined().contains(illegal), sink.joined());
			assertEquals(before, constructionXml(app));

			sink.clear();
			// Execute returns its argument list, so an inner refusal is reported by the
			// error presentation and stays atomic, but the script line is not a failure.
			assertTrue(new GgbScript(app, "Execute({\"X=Rotate(S,0.5,ax)\"})").run(click));
			assertTrue(sink.joined().contains(illegal), sink.joined());
			assertEquals(before, constructionXml(app));
			assertNull(app.getKernel().lookupLabel("X"));
			assertEquals(CommandLookupStrategy.USER, app.getKernel().getCommandLookupStrategy());
		}
	}

	@Test
	void scriptEditorBoundaryHoldsAcrossUiLanguages(@TempDir Path directory) throws Exception {
		for (Locale[] pair : new Locale[][] {{SPANISH, Locale.ENGLISH},
				{Locale.ENGLISH, SPANISH}}) {
			AppGeoCeDG authoring = PreG9BR6ScriptHarness.application(pair[0]);
			for (String statement : new String[] {"A=(-2,0)", "B=(0,1)", "C=(2,0)",
					"S=SplineV2({A,B,C})", "O=(0,0)"}) {
				eval(authoring, statement);
			}
			GeoElement button = eval(authoring, "btn=Button(\"run\")");
			String alias = SPANISH.equals(pair[0])
					? authoring.getLocalization().getCommand("Mirror") : "Reflect";
			ScriptInputModel model = new ScriptInputModel(authoring, EventType.CLICK, "OnClick");
			model.setGeos(new Object[] {button});
			model.processInput("X=Reflect(S,O)\nY=" + alias + "(S,O)\nM=Length(X)",
					ScriptType.GGBSCRIPT);
			Script script = button.getScript(EventType.CLICK);
			assertEquals("X=Mirror(S,O)\nY=Mirror(S,O)\nM=Length(X)",
					script.getInternalText(), "stored semantics are internal");
			assertEquals("X=Reflect(S,O)\nY=Reflect(S,O)\nM=Length(X)", script.getText(),
					"the editor shows canonical English heads");
			Path file = directory.resolve("script-" + pair[0] + ".cedg");
			assertTrue(((GuiManagerGeoCeDG) authoring.getGuiManager()).saveAsTo(file.toFile()));

			AppGeoCeDG running = PreG9BR6ScriptHarness.application(pair[1]);
			Sink sink = new Sink();
			try (AutoCloseable installed = PreG9BR6ScriptHarness.install(running, sink)) {
				assertTrue(running.loadFile(file.toFile(), false));
				GeoElement reopened = lookup(running, "btn");
				assertEquals(script.getInternalText(),
						reopened.getScript(EventType.CLICK).getInternalText());
				reopened.runClickScripts(null);
				assertEquals("", sink.joined());
				GeoLocusV2 mirrored = assertInstanceOf(GeoLocusV2.class, lookup(running, "X"));
				assertEquals(Commands.Mirror, mirrored.getParentAlgorithm().getClassName());
				assertEquals(Commands.Mirror,
						lookup(running, "Y").getParentAlgorithm().getClassName());
				assertTrue(mirrored.isChildOf(lookup(running, "B")), "dynamic dependency");
				String metric = lookup(running, "M").getXML();
				eval(running, "B=(0.25,1.5)");
				assertFalse(metric.equals(lookup(running, "M").getXML()), "recomputed");
			}
		}
	}

	@Test
	void sameSessionLanguageChangeKeepsScriptIdentityAndUserAliases() throws Exception {
		AppGeoCeDG app = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		Sink sink = new Sink();
		try (AutoCloseable installed = PreG9BR6ScriptHarness.install(app, sink)) {
			for (String statement : new String[] {"A=(-2,0)", "B=(0,1)", "C=(2,0)",
					"S=SplineV2({A,B,C})", "O=(0,0)"}) {
				eval(app, statement);
			}
			GeoElement button = eval(app, "btn=Button(\"run\")");
			ScriptInputModel model = new ScriptInputModel(app, EventType.CLICK, "OnClick");
			model.setGeos(new Object[] {button});
			model.processInput("X=Reflect(S,O)", ScriptType.GGBSCRIPT);
			String stored = button.getScript(EventType.CLICK).getInternalText();

			// The host language lifecycle (R5-B-R1), not a bare locale swap.
			app.setLanguage(SPANISH);
			app.getCommandDictionary();

			assertEquals(stored, button.getScript(EventType.CLICK).getInternalText());
			assertEquals("X=Reflect(S,O)", button.getScript(EventType.CLICK).getText());
			button.runClickScripts(null);
			assertEquals(Commands.Mirror, lookup(app, "X").getParentAlgorithm().getClassName());
			String alias = app.getLocalization().getCommand("Mirror");
			assertNotNull(submit(app, "Y=" + alias + "(S,O)", sink), sink.joined());
			assertEquals(Commands.Mirror, lookup(app, "Y").getParentAlgorithm().getClassName());
			String before = constructionXml(app);
			button.setClickScript(new GgbScript(app, "Z=" + alias + "(S,O)"));
			String attached = constructionXml(app);
			sink.clear();
			button.runClickScripts(null);
			assertNull(app.getKernel().lookupLabel("Z"), "a raw localized SCRIPT token");
			assertTrue(sink.joined().contains(app.getLocalization().getError("UnknownCommand")),
					sink.joined());
			assertEquals(attached, constructionXml(app));
			assertFalse(before.equals(attached), "only the script text changed");
		}
	}

	private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getEuclidianView1().getEuclidianController();
	}

	private static long points(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoElement::isGeoPoint).count();
	}

	private static void baseline(UndoManagerD undo) throws Exception {
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean(), "Desktop undo did not complete");
	}
}

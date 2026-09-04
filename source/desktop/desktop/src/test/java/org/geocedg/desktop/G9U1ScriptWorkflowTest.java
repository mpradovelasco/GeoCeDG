/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Locale;

import javax.swing.JPanel;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoCurveCartesianND;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.script.GgbScript;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.main.AppD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Script consumption of public commands, not a second command or identity model. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1ScriptWorkflowTest {

	@Test
	void scriptSplineFormsUseSamePublicSemanticConstruction() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement trigger = eval(app, "O=(0,0)");
		assertTrue(run(app, trigger, "A=(-2,0)", "B=(-2/3,0)", "C=(2/3,0)",
				"D=(2,0)", "w(x,y)=sqrt(x^2+y^2)+0.5", "S=SplineV2({A,B,C,D})",
				"SD=SplineV2({A,B,C,D},3)", "SW=SplineV2({A,B,C,D},3,w)",
				"SV=SplineV2(A,B,C)", "SL=SplineV2({A,B,C})"));
		for (String label : new String[] {"S", "SD", "SW", "SV", "SL"}) {
			GeoLocusV2 spline = assertInstanceOf(GeoLocusV2.class, lookup(app, label));
			assertTrue(spline.isDefined(), label);
			assertNotNull(spline.getParentAlgorithm());
		}
		assertEquals(length(app, "S"), length(app, "SD"), 1E-8);
		assertEquals(length(app, "S"), length(app, "SW"), 1E-8);
		assertEquals(length(app, "SV"), length(app, "SL"), 1E-8);
		assertEquals(4, length(app, "S"), 1E-8);
	}

	@Test
	void exactSemanticPointMetricAndTransformsWorkInScripts() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement trigger = eval(app, "O=(0,0)");
		assertTrue(trigger.isGeoPoint());
		assertTrue(run(app, trigger, "k=-2", "S=SplineV2({(-2,0),(0,0),(2,0)},3)",
				"P=Point(S,\"spline-v2/main\",0.25)",
				"Q=Point(S,\"spline-v2/main\",0.75)", "M=Length(S,P,Q)",
				"T=Dilate(S,k,O)", "MT=Length(T)"));
		assertEquals(2, ((GeoNumeric) lookup(app, "M")).getDouble(), 1E-8);
		assertEquals(8, ((GeoNumeric) lookup(app, "MT")).getDouble(), 1E-8);
		GeoElement transformed = lookup(app, "T");
		assertTrue(run(app, trigger, "SetValue(k,0)"));
		assertTrue(transformed.isDefined());
		assertEquals(0, ((GeoNumeric) lookup(app, "MT")).getDouble(), 1E-8);
		assertTrue(run(app, trigger, "SetValue(k,1)"));
		assertEquals(4, ((GeoNumeric) lookup(app, "MT")).getDouble(), 1E-8);
		assertTrue(transformed == lookup(app, "T"));
	}

	@Test
	void allSevenSimilarityFormsAndRichCommandsUseThePublicScriptDispatcher() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement trigger = eval(app, "O=(0,0)");
		assertTrue(run(app, trigger, "u=0", "P=(u,0)", "D={false,{-2,2,true,true}}",
				"L=LocusV2(P,u,D)", "S=SplineV2({(-2,0),(0,0),(2,0)},3)",
				"v=(1,2)", "axis:y=x", "ST=Translate(S,v)", "SR=Rotate(S,0.5)",
				"SRC=Rotate(S,0.5,O)", "SP=Reflect(S,O)", "SL=Reflect(S,axis)",
				"SD=Dilate(S,-2)", "SDC=Dilate(S,-2,O)", "LR=LocusLength(L)",
				"IR=Intersect(L,axis)", "M=Length(L)"));
		for (String label : new String[] {"ST", "SR", "SRC", "SP", "SL", "SD", "SDC"}) {
			assertInstanceOf(GeoLocusV2.class, lookup(app, label));
			assertEquals(label.startsWith("SD") ? 8 : 4, length(app, label), 1E-8);
		}
		assertInstanceOf(org.geocedg.common.kernel.geos.GeoLocusMetricResult.class,
				lookup(app, "LR"));
		assertInstanceOf(org.geocedg.common.kernel.geos.GeoLocusIntersectionResult.class,
				lookup(app, "IR"));
		assertEquals(4, ((GeoNumeric) lookup(app, "M")).getDouble(), 1E-8);
	}

	@Test
	void englishSpanishScriptLocalizationRoundTripsAllSplineForms() throws Exception {
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			AppGeoCeDG app = G9U1TestApp.create();
			app.setLocale(locale);
			String internal = "S=SplineV2({A,B,C})\nD=SplineV2({A,B,C},3)\n"
					+ "W=SplineV2({A,B,C},3,w)\nV=SplineV2(A,B,C)";
			String localized = GgbScript.script2LocalizedScript(app, internal);
			assertEquals(internal, GgbScript.localizedScript2Script(app, localized));
			GeoElement trigger = eval(app, "O=(0,0)");
			assertTrue(run(app, trigger, "A=(-2,0)", "B=(0,0)", "C=(2,0)",
					"w(x,y)=sqrt(x^2+y^2)+0.5", internal));
			assertTrue(lookup(app, "S").isDefined());
			assertTrue(lookup(app, "W").isDefined());
		}
	}

	@Test
	void featureOffScriptRejectsSemanticCreationWithoutMutation() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create(false);
		GeoElement trigger = eval(app, "O=(0,0)");
		String xml = app.getXML();
		ErrorAccumulator errors = new ErrorAccumulator();
		assertFalse(runWithErrors(app, trigger, errors,
				"S=SplineV2({(0,0),(1,0),(2,0)})"));
		assertFalse(errors.getErrors().isEmpty());
		assertNull(app.getKernel().lookupLabel("S"));
		assertEquals(xml, app.getXML());
	}

	@Test
	void nativeReopenPreservesScriptAndDynamicSemanticGraph(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement trigger = eval(app, "O=(0,0)");
		assertTrue(trigger.isGeoPoint());
		assertTrue(run(app, trigger, "k=1", "S=SplineV2({(-2,0),(0,0),(2,0)},3)",
				"T=Dilate(S,k,O)", "MT=Length(T)"));
		trigger.setClickScript(new GgbScript(app, "SetValue(k,-2)"));
		Path file = directory.resolve("script-workspace.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoElement reopenedTrigger = lookup(reopened, "O");
		assertNotNull(reopenedTrigger.getScript(EventType.CLICK));
		assertTrue(reopenedTrigger.getScript(EventType.CLICK)
				.run(new Event(EventType.CLICK, reopenedTrigger)));
		assertEquals(8, ((GeoNumeric) lookup(reopened, "MT")).getDouble(), 1E-8);
	}

	@Test
	void preservedNativeSplineDoesNotPublishCreationInDisabledOrClassicHosts(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG authoring = G9U1TestApp.create();
		GeoElement trigger = eval(authoring, "O=(0,0)");
		assertTrue(run(authoring, trigger, "S=SplineV2({(-2,0),(0,0),(2,0)},3)",
				"T=Dilate(S,-2,O)", "M=Length(T)"));
		Path file = directory.resolve("preserved-semantic-script.cedg");
		assertTrue(((GuiManagerGeoCeDG) authoring.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG disabled = G9U1TestApp.create(false);
		App3D classic = new App3D(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel());
		G9U1TestApp.withoutWindowDispatcher(classic);
		classic.setErrorDialogsActive(false);
		for (AppD host : new AppD[] {disabled, classic}) {
			assertTrue(host.loadFile(file.toFile(), false));
			assertInstanceOf(GeoLocusV2.class, host.getKernel().lookupLabel("S"));
			assertEquals(8, ((GeoNumeric) host.getKernel().lookupLabel("M")).getDouble(), 1E-8);
			String xml = host.getXML();
			ErrorAccumulator errors = new ErrorAccumulator();
			assertFalse(runWithErrors(host, host.getKernel().lookupLabel("O"), errors,
					"Rejected=SplineV2({(0,0),(1,0),(2,0)})"));
			assertFalse(errors.getErrors().isEmpty());
			assertEquals(xml, host.getXML());
			assertTrue(run(host, host.getKernel().lookupLabel("O"),
					"U=Spline({(-2,0),(0,1),(2,0)})"));
			assertInstanceOf(GeoCurveCartesianND.class, host.getKernel().lookupLabel("U"));
			assertTrue(host.getKernel().lookupLabel("U").isDefined());
		}
	}

	@Test
	void classicScriptRetainsSplineAndDoesNotCreateSemanticSpline() throws Exception {
		G9U1TestApp.create(); // Initialize the shared AWT platform before the Classic host.
		App3D classic = new App3D(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel());
		G9U1TestApp.withoutWindowDispatcher(classic);
		classic.setErrorDialogsActive(false);
		GeoElement trigger = classic.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("O=(0,0)", false)[0].toGeoElement();
		assertTrue(run(classic, trigger, "A=(-2,0)", "B=(0,0)", "C=(2,0)",
				"U=Spline({A,B,C})", "V=Spline({A,B,C},3)",
				"w(x,y)=sqrt(x^2+y^2)+0.5", "W=Spline({A,B,C},3,w)"));
		for (String label : new String[] {"U", "V", "W"}) {
			assertInstanceOf(GeoCurveCartesianND.class, classic.getKernel().lookupLabel(label));
			assertTrue(classic.getKernel().lookupLabel(label).isDefined());
		}
		String xml = classic.getXML();
		ErrorAccumulator errors = new ErrorAccumulator();
		assertFalse(runWithErrors(classic, trigger, errors, "S=SplineV2({A,B,C})"));
		assertFalse(errors.getErrors().isEmpty());
		assertNull(classic.getKernel().lookupLabel("S"));
		assertEquals(xml, classic.getXML());
		assertFalse(classic.getConfig()
				instanceof org.geocedg.common.main.settings.config.AppConfigGeoCeDG);
	}

	private static boolean run(App app, GeoElement trigger, String... lines) throws Exception {
		ErrorAccumulator errors = new ErrorAccumulator();
		boolean result = runWithErrors(app, trigger, errors, lines);
		assertEquals("", errors.getErrors());
		return result;
	}

	private static boolean runWithErrors(App app, GeoElement trigger,
			ErrorAccumulator errors, String... lines) throws Exception {
		// Replace only modal error presentation in this test host. Script dispatch,
		// semantic commands and diagnostic generation remain real and are asserted.
		Field field = AppD.class.getDeclaredField("defaultErrorHandler");
		field.setAccessible(true);
		Object previous = field.get(app);
		field.set(app, errors);
		try {
			return new GgbScript(app, String.join("\n", lines))
					.run(new Event(EventType.CLICK, trigger));
		} finally {
			field.set(app, previous);
		}
	}

	private static double length(AppGeoCeDG app, String label) {
		return ((GeoNumeric) eval(app, "Length(" + label + ")")).getDouble();
	}
}

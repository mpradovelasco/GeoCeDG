/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JPanel;

import org.geocedg.common.kernel.algos.AlgoBSpline;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-MAINT-A2, BS-12 and BS-21 on the native document ({@code bspline-2d.md} section 8):
 * a {@code BSpline} document saves, reopens with its durable identity and saves again with
 * identical construction XML; a document whose family element is rejected is refused as a
 * whole with the typed message, and the open document is unchanged; Insert File admits the
 * family through the Locus V2 row.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class MaintA2BSplineNativeArchiveTest {

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	private static AppGeoCeDG app() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static GeoElement eval(App app, String command) {
		GeoElementND[] output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		return output[0].toGeoElement();
	}

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static String entry(Path archive, String name) throws Exception {
		try (ZipFile zip = new ZipFile(archive.toFile())) {
			ZipEntry entry = zip.getEntry(name);
			assertNotNull(entry, name);
			return new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static void rewrite(Path source, Path target, String from, String to)
			throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipFile zip = new ZipFile(source.toFile());
				ZipOutputStream out = new ZipOutputStream(bytes)) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				byte[] content = zip.getInputStream(entry).readAllBytes();
				if ("geogebra.xml".equals(entry.getName())) {
					String xml = new String(content, StandardCharsets.UTF_8);
					assertTrue(xml.contains(from));
					content = xml.replace(from, to).getBytes(StandardCharsets.UTF_8);
				}
				out.putNextEntry(new ZipEntry(entry.getName()));
				out.write(content);
				out.closeEntry();
			}
		}
		Files.write(target, bytes.toByteArray());
	}

	private static String construction(String xml) {
		return xml.substring(xml.indexOf("<construction"), xml.indexOf("</construction>"));
	}

	@Test
	void saveReopenSaveKeepsTheConstructionAndIdentity(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = app();
		for (String command : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2)", "D=(4,0)",
				"E=(5,1)", "b=BSpline({A,B,C,D,E},3,{0,0,0,0,1,4,4,4,4})", "c=BSpline({A,B,C,D})",
				"M=LocusLength(b)"}) {
			eval(app, command);
		}
		GeoLocusV2 curve = (GeoLocusV2) app.getKernel().lookupLabel("b");
		String identity = curve.getPersistentLocusId().toExternalForm();
		Path first = directory.resolve("bspline.cedg");
		assertTrue(gui(app).saveAsTo(first.toFile()));
		String saved = entry(first, "geogebra.xml");
		assertTrue(saved.contains("<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"explicit\"/>"));
		assertTrue(saved.contains(
				"<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"clamped-uniform/v1\"/>"));

		AppGeoCeDG reopened = app();
		assertTrue(reopened.loadFile(first.toFile(), false));
		GeoLocusV2 reopenedCurve = (GeoLocusV2) reopened.getKernel().lookupLabel("b");
		assertTrue(reopenedCurve.isDefined());
		assertTrue(reopenedCurve.getParentAlgorithm() instanceof AlgoBSpline);
		assertEquals(identity, reopenedCurve.getPersistentLocusId().toExternalForm());
		Path second = directory.resolve("bspline-again.cedg");
		assertTrue(gui(reopened).saveAsTo(second.toFile()));
		assertEquals(construction(saved), construction(entry(second, "geogebra.xml")));
	}

	@Test
	void aRejectedFamilyElementRefusesTheWholeDocumentWithItsTypedMessage(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG author = app();
		for (String command : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2)", "D=(4,0)",
				"b=BSpline({A,B,C,D})"}) {
			eval(author, command);
		}
		Path valid = directory.resolve("valid.cedg");
		assertTrue(gui(author).saveAsTo(valid.toFile()));
		Path future = directory.resolve("future.cedg");
		rewrite(valid, future, "contract=\"bspline-2d/v1\"", "contract=\"bspline-2d/v2\"");

		AppGeoCeDG reader = app();
		eval(reader, "Kept=(7,7)");
		String before = reader.getXML();
		List<String> messages = new ArrayList<>();
		reader.setUnitLoadErrorSink(messages::add);
		// The host logs the rejected load; a headless logger left by an earlier class would
		// turn that logged runtime exception into an assertion error.
		Log previous = Log.getLogger();
		Log.setLogger(new LoggerD());
		try {
			assertFalse(reader.loadFile(future.toFile(), false));
		} finally {
			Log.setLogger(previous);
		}
		assertEquals(List.of(reader.layerText("BSpline.LoadError.UNSUPPORTED_CONTRACT_VERSION",
				future.getFileName().toString())), messages);
		assertEquals(construction(before), construction(reader.getXML()));
		assertNotNull(reader.getKernel().lookupLabel("Kept"));
	}

	@Test
	void insertFileAdmitsTheFamilyThroughTheLocusV2Row() {
		AppGeoCeDG app = app();
		for (String command : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2)", "D=(4,0)",
				"b=BSpline({A,B,C,D})"}) {
			eval(app, command);
		}
		assertEquals(GeoCeDGInsertionAdmission.Family.LOCUS_V2,
				GeoCeDGInsertionAdmission.classify(app.getKernel().lookupLabel("b")));
	}
}

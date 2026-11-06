/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_ANGLE;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_LINE;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_LIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;

import org.geocedg.common.jre.io.LegacyStyleDefaultsReader;
import org.geocedg.common.kernel.geos.LegacyStyleDefaults;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/** Native legacy appearance is the comparison oracle in a disposable host. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class Cu4LegacyDefaultsCompatibilityTest {
	private static final String CORPUS = """
			docs/references/cedg/models/g9p/geocedg-reference-general-construction-workflow.ggb
			docs/references/cedg/models/g9p/geocedg-reference-locus-cylindrical-graft-development.ggb
			docs/references/cedg/models/g9p/geocedg-reference-locus-focal-sphere-illumination.ggb
			docs/references/cedg/models/g9p/geocedg-reference-locus-truncated-cone-cylinder-connections.ggb
			docs/validation/g10p-study-optimization/models/ArticulatedDoor.ggb
			docs/validation/g10p-study-optimization/models/SphericValve.ggb
			docs/validation/g10p-study-optimization/models/SphericValveV2.ggb
			docs/validation/g10p-study-optimization/models/TruncatedCone.ggb
			docs/validation/g10p-study-optimization/models/dyscrete-elbows/CylElbow_3V.ggb
			docs/validation/g10p-study-optimization/models/dyscrete-elbows/coneElbow_3V.ggb
			models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb
			models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb
			models/legacy/template-v7/original/Templatev7.ggb
			models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg
			models/regression/pre-g9b-cu/original/InterCilConoObliqueTest.ggb
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-layers-and-area.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-physical-cm-presentation-mm.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-physical-cm.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-semantic-curves.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-units-m.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-units-mm.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-units-usm-inch.cedg
			models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/c-unspecified.cedg
			models/regression/pre-g9b-s1-dxf/original/Elipse.cedg
			models/regression/pre-g9b-s1-dxf/original/TestExport1.cedg
			source/desktop/desktop/src/e2eTest/resources/circles.ggb
			source/desktop/desktop/src/test/resources/org/geocedg/desktop/cu7-general-creation/TestM08-S.cedg
			source/desktop/desktop/src/test/resources/org/geocedg/desktop/g9u1-review/TestBasic1.cedg
			source/desktop/desktop/src/test/resources/org/geocedg/desktop/pre-g9b-r5-b/pre-g9b-r5-b-historical-es.cedg
			source/desktop/desktop/src/test/resources/svg/material-VrT75QCK.ggb
			source/desktop/desktop/src/test/resources/svg/svgs.ggb
			source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg
			source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r4/fourSolutions.cedg
			source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r5/fourSolutionsDynamicDilate.cedg
			source/shared/common-jre/src/test/resources/org/geogebra/common/io/ziptest.ggb
			""";
	private static final Set<String> NON_STYLE = Set.of("coords", "eigenvectors",
			"matrix", "value", "file", "startPoint", "boundingBox");
	private static final List<String> NEW_OBJECTS = List.of("A=(0,0)", "B=(1,1)",
			"ell=Line(A,B)", "seg=Segment(A,B)", "ray1=Ray(A,B)", "v=Vector(A,B)",
			"poly=Polygon(A,B,(2,0))", "circ=Circle(A,B)", "f(x)=x^2", "number=2",
			"theta=47°", "points={A,B}", "t1=\"static\"", "bool1=true");

	@TempDir
	Path work;

	@Test
	void corpusMatchesNativePrototypesAndNewObjects() throws Exception {
		onEdt(() -> {
			AppGeoCeDG nativeHost = app(M);
			AppGeoCeDG receiver = app(M);
			Path repository = PreG9BCu0Corpus.repositoryRoot();
			List<Path> archives = new ArrayList<>(CORPUS.lines().map(repository::resolve)
					.toList());
			// The audit's two scratch archives duplicated c-layers-and-area's block.
			// Generate two archived inputs in the test so a clean checkout needs no scratch files.
			archives.add(generatedArchive(false));
			archives.add(generatedArchive(true));
			assertEquals(37, archives.size());
			for (Path archive : archives) {
				String xml = readDefaults(archive);
				reset(nativeHost);
				reset(receiver);
				LegacyStyleDefaults styles = LegacyStyleDefaultsReader.read(xml, defaults(receiver));
				styles.applyTo(defaults(receiver));
				nativeHost.getXMLio().processXMLString(xml, false, true, false, false);

				assertEquals(appearance(defaultsXML(nativeHost)), appearance(defaultsXML(receiver)),
						archive.toString());
				for (String command : NEW_OBJECTS) {
					GeoElement expected = create(nativeHost, command);
					GeoElement actual = create(receiver, command);
					assertEquals(appearance("<construction>" + expected.getXML() + "</construction>"),
							appearance("<construction>" + actual.getXML() + "</construction>"),
							() -> archive + " " + command);
					if (expected instanceof GeoNumeric numeric) {
						assertEquals(numeric.isAutoStep(), ((GeoNumeric) actual).isAutoStep(),
								() -> archive + " " + command + " automatic increment");
					}
				}
			}
			return null;
		});
	}

	@Test
	void omittedLegacyValuesRetainNativeSemantics() throws Exception {
		onEdt(() -> {
			AppGeoCeDG receiver = app(M);
			String xml = """
					<geogebra format="5.0"><defaults>
					  <element default="20" type="line" label="prototype">
					    <lineStyle thickness="2" type="0"/>
					  </element>
					  <element default="52" type="angle" label="prototype">
					    <animation type="0" playing="false"/>
					  </element>
					  <element default="130" type="list" label="prototype">
					    <pointStyle val="-1"/>
					  </element>
					</defaults></geogebra>
					""";
			ConstructionDefaults defaults = defaults(receiver);
			defaults.setPersonalDefaults(LegacyStyleDefaultsReader.read(xml, defaults), null);
			defaults.applyPersonalDefaults();
			assertEquals(255, defaults.getDefaultGeo(DEFAULT_LINE).getLineOpacity());
			assertEquals(255, must(receiver, "ell=Line((0,0),(1,1))").getLineOpacity());
			GeoNumeric angleDefault = (GeoNumeric) defaults.getDefaultGeo(DEFAULT_ANGLE);
			assertFalse(angleDefault.isAutoStep());
			receiver.getKernel().setAngleUnit(Kernel.ANGLE_RADIANT);
			GeoAngle angle = (GeoAngle) create(receiver, "theta=47°");
			assertFalse(angle.isAutoStep());
			assertEquals(Math.PI / 180, angle.getAnimationStep());
			assertEquals(-1, ((GeoList) defaults.getDefaultGeo(DEFAULT_LIST)).getPointStyle());
			assertEquals(-1, ((GeoList) must(receiver, "points={(1,2),(3,4)}")).getPointStyle());
			return null;
		});
	}

	private Path generatedArchive(boolean explicitStep) {
		AppGeoCeDG source = app(M);
		defaults(source).getDefaultGeo(DEFAULT_LINE).setLineOpacity(explicitStep ? 80 : 255);
		GeoNumeric angle = (GeoNumeric) defaults(source).getDefaultGeo(DEFAULT_ANGLE);
		angle.setAutoStep(!explicitStep);
		if (explicitStep) {
			angle.setAnimationStep(0.25);
		}
		return save(source, work, explicitStep ? "explicit" : "automatic").toPath();
	}

	private static GeoElement create(AppGeoCeDG app, String command) {
		GeoElement geo = must(app, command);
		if (geo instanceof GeoNumeric numeric) {
			GeoNumeric.setSliderFromDefault(numeric, true);
		}
		return geo;
	}

	private static void reset(AppGeoCeDG app) {
		app.getKernel().clearConstruction(false);
		app.getKernel().setAngleUnit(Kernel.ANGLE_RADIANT);
		defaults(app).resetCreationDefaults();
	}

	private static ConstructionDefaults defaults(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getConstructionDefaults();
	}

	private static String defaultsXML(AppGeoCeDG app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		defaults(app).getDefaultsXML(xml, null);
		return xml.toString();
	}

	private static String readDefaults(Path archive) throws Exception {
		try (ZipFile zip = new ZipFile(archive.toFile())) {
			var entry = zip.getEntry(MyXMLio.XML_FILE_DEFAULTS_2D);
			assertNotNull(entry, archive.toString());
			try (var input = zip.getInputStream(entry)) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}

	private static Map<String, Map<String, String>> appearance(String xml) throws Exception {
		var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		var elements = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)))
				.getElementsByTagName("element");
		Map<String, Map<String, String>> appearance = new TreeMap<>();
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);
			String type = element.getAttribute("default");
			for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node instanceof Element leaf && !NON_STYLE.contains(leaf.getTagName())) {
					appearance.put(type + "/" + leaf.getTagName(), styleAttributes(leaf));
				}
			}
		}
		return appearance;
	}

	private static Map<String, String> styleAttributes(Element element) {
		Map<String, String> result = new TreeMap<>();
		var attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			String name = attr.getNodeName();
			if (!"slider".equals(element.getTagName()) || !("x".equals(name) || "y".equals(name))) {
				result.put(name, attr.getNodeValue());
			}
		}
		return result;
	}
}

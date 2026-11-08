/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.geocedg.desktop.GeoCeDGInspectionArchive.Code;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Declaration;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Disposition;
import org.geocedg.desktop.GeoCeDGInspectionArchive.Inspection;
import org.geogebra.common.kernel.Kernel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GeoCeDGInspectionArchiveTest {

	private static final String DOCUMENT_ID = "3718e88e-d397-4749-ab7a-f1efcc3eefe7";
	private static final String GEO_ID = "geo:53597e2faca9b5ed38b856dae8bcd450";
	private static final String POINTS = """
			<expression label="A" exp="(0, 0)" type="point"/>
			<element type="point" label="A"/>
			<expression label="B" exp="(2, 0)" type="point"/>
			<element type="point" label="B"/>
			<expression label="C" exp="(0, 2)" type="point"/>
			<element type="point" label="C"/>
			<expression label="n" exp="2"/>
			<element type="numeric" label="n"><value val="2"/></element>
			""";

	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void passiveNativeVocabularyKeepsXmlAndInventoriesTransport() throws IOException {
		String xml = document(POINTS + """
				<command name="Segment"><input a0="A" a1="B"/><output a0="s"/></command>
				<element type="segment" label="s"/>
				<command name="CircleArc"><input a0="A" a1="B" a2="C"/><output a0="arc"/></command>
				<element type="conicpart" label="arc"/>
				<command name="Point"><input a0="arc" a1="0.5"/><output a0="W"/></command>
				<element type="point" label="W"/>
				<command name="CurveCartesian"><input a0="t" a1="(n * t^(2))" a2="t" a3="-1" a4="1"/>
				<output a0="curve"/></command>
				<element type="curvecartesian" label="curve"/>
				<command name="Sequence"><input a0="(k * n)" a1="k" a2="1" a3="4"/>
				<output a0="seq" randomResult="{2, 4, 6, 8}"/></command>
				<element type="list" label="seq"/>
				<command name="Text"><input a0="n"/><output a0="dynamic"/></command>
				<element type="text" label="dynamic"/>
				""");
		Inspection inspection = GeoCeDGInspectionArchive.inspect(zip("geogebra.xml", xml,
				"geogebra_javascript.js", Kernel.defaultLibraryJavaScript,
				"geogebra_thumbnail.png", "opaque bytes never decoded"));
		assertTrue(inspection.findings().toString(), inspection.loadable());
		assertEquals(DOCUMENT_ID, inspection.documentId().orElseThrow());
		assertEquals(10, inspection.declarations().size());
		assertEquals(2L, inspection.entries().stream()
				.filter(entry -> entry.disposition() == Disposition.OMITTED_TRANSPORT).count());
		try (ZipInputStream nativeZip = new ZipInputStream(inspection.openForNativeLoad())) {
			assertEquals("geogebra.xml", nativeZip.getNextEntry().getName());
			assertArrayEquals(xml.getBytes(StandardCharsets.UTF_8), nativeZip.readAllBytes());
			assertNull(nativeZip.getNextEntry());
		}
	}

	@Test
	public void captureDoesNotRereadChangedFileOrExposeMutableBytes() throws IOException {
		byte[] original = zip("geogebra.xml", document(POINTS));
		Path file = temporary.newFile("source.ggb").toPath();
		Files.write(file, original);
		Inspection fileCapture = GeoCeDGInspectionArchive.capture(file);
		byte[] input = original.clone();
		Inspection byteCapture = GeoCeDGInspectionArchive.inspect(input);
		Arrays.fill(input, (byte) 0);
		Files.write(file, input);
		assertTrue(fileCapture.loadable());
		assertTrue(byteCapture.loadable());
		assertEquals(fileCapture.sha256(), byteCapture.sha256());
		assertArrayEquals(original, fileCapture.openOriginal().readAllBytes());
		assertArrayEquals(original, byteCapture.openOriginal().readAllBytes());
	}

	@Test
	public void ordinaryDurableGeoAttachmentIsReportedWithoutInferringIdentity() throws IOException {
		String content = """
				<geocedgSpatial version="1">
					<geo id="%s" provider="geocedg-construction-provider/v1" family="POINT"
						schema="geocedg-construction-geo" schemaVersion="2" authority="CONSTRUCTION_DEFINED"
						bindingRole="NOT_APPLICABLE" outputRole="VALUE" cardinality="1"
						definitionRevision="0" topologyRevision="0"/>
				</geocedgSpatial>
				<expression label="A" exp="(0, 0)" type="point"/>
				<element type="point" label="A" geocedgId="%s"/>
				""".formatted(GEO_ID, GEO_ID);
		Inspection inspection = GeoCeDGInspectionArchive.inspect(zip("geogebra.xml", document(content)));
		assertTrue(inspection.findings().toString(), inspection.loadable());
		assertEquals(List.of(new Declaration("A", "point", GEO_ID)), inspection.declarations());
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				document(content.replace("CONSTRUCTION_DEFINED", "SPATIAL_DEFINED"))));
	}

	@Test
	public void activeContentIsRejectedBeforeThereIsAnyNativeInput() throws IOException {
		assertRefused(Code.SCRIPT, zip("geogebra.xml", document(POINTS),
				"geogebra_javascript.js", "function ggbOnInit() { ggbApplet.evalCommand('A=(1,1)'); }"));
		assertRefused(Code.SCRIPT, zip("geogebra.xml", document("""
				<element type="point" label="A"><javascript val="active()"/></element>
				""")));
		assertRefused(Code.SCRIPT, zip("geogebra.xml", document("""
				<element type="point" label="A"><listener val="active"/></element>
				""")));
		assertRefused(Code.MACRO, zip("geogebra.xml", document(POINTS), "geogebra_macro.xml",
				"<geogebra format=\"5.0\"><macro cmdName=\"CustomTool\"/></geogebra>"));
		assertRefused(Code.RESOURCE, zip("geogebra.xml", document(POINTS),
				"assets/external.svg", "<svg/>"));
		assertRefused(Code.RESOURCE, zip("geogebra.xml", document("""
				<element type="point" label="A">
					<objColor r="0" g="0" b="0" image="https://example.invalid/pixel.png"/>
				</element>
				""")));
		assertRefused(Code.ANIMATION, zip("geogebra.xml", document("""
				<element type="numeric" label="a"><animation step="1" playing="true"/></element>
				""")));
		String detachedView = document(POINTS).replace("<construction>", """
				<gui><perspectives><perspective id="tmp"><views>
				<view id="1" visible="true" inframe="true" stylebar="false"
					location="1" size="300" window="100,100,600,400"/>
				</views></perspective></perspectives></gui><construction>
				""");
		Inspection detached = assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml", detachedView));
		assertEquals(List.of(new GeoCeDGInspectionArchive.Finding(Code.UNSUPPORTED_XML,
				"geogebra.xml/gui/perspectives/perspective/views/view",
				"Visible detached views are outside the passive trial")), detached.findings());
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				detachedView.replace("visible=\"true\"", "visible=\"false\"")
						.replace("inframe=\"true\"", "inframe=\"invalid\"")));
		assertRefused(Code.SCRIPT, zip("geogebra.xml", document(POINTS), "geogebra_defaults2d.xml",
				"""
				<geogebra format="5.0"><defaults><element default="10" type="point" label="Point">
				<javascript val="active()"/></element></defaults></geogebra>
				"""));
	}

	@Test
	public void nestedCommandsUnknownEffectsAndUnboundedWorkAreRejected() throws IOException {
		assertRefused(Code.UNSUPPORTED_EXPRESSION, zip("geogebra.xml", document("""
				<expression label="a" exp="&#x45;xecute({&quot;A=(1,1)&quot;})"/>
				<element type="numeric" label="a"/>
				""")));
		assertRefused(Code.UNSUPPORTED_COMMAND, zip("geogebra.xml", document(POINTS + """
				<command name="Text"><input a0="Execute({&quot;A=(1,1)&quot;})"/><output a0="T"/></command>
				<element type="text" label="T"/>
				""")));
		assertRefused(Code.UNSUPPORTED_COMMAND, zip("geogebra.xml", document("""
				<command name="Sequence"><input a0="k" a1="k" a2="1" a3="9999"/><output a0="L"/></command>
				<element type="list" label="L"/>
				""")));
		assertRefused(Code.UNSUPPORTED_COMMAND, zip("geogebra.xml", document(POINTS + """
				<command name="Sequence"><input a1="k" a0="k" a2="1" a3="4"/><output a0="L"/></command>
				<element type="list" label="L"/>
				""")));
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml", document("""
				<element type="point" label="A"><objColor r="0" g="0" b="0" dynamicr="42"/></element>
				""")));
	}

	@Test
	public void malformedArchivesAndXmlAreRejected() throws IOException {
		byte[] valid = zip("geogebra.xml", document(POINTS));
		assertRefused(Code.INVALID_ARCHIVE, Arrays.copyOf(valid, valid.length - 22));
		assertRefused(Code.INVALID_ARCHIVE, Arrays.copyOf(valid, valid.length + 1));
		assertRefused(Code.INVALID_ARCHIVE, zip("../geogebra.xml", document(POINTS)));
		byte[] nameMismatch = valid.clone();
		nameMismatch[30] = 'X'; // First byte of the first ZIP local name, after its fixed header.
		assertRefused(Code.INVALID_ARCHIVE, nameMismatch);
		assertRefused(Code.INVALID_XML, zip("geogebra.xml",
				document(POINTS).replace("<geogebra ", """
						<!DOCTYPE geogebra SYSTEM "https://example.invalid/file.dtd">
						<geogebra\s""")));
		assertRefused(Code.INVALID_XML, zip("geogebra.xml",
				document(POINTS.replace("type=\"point\" label=\"A\"", "type=\"point\" label=\"A\" label=\"X\""))));
		assertRefused(Code.LIMIT_EXCEEDED, zip("geogebra.xml", document(POINTS),
				"geogebra_thumbnail.png", "x".repeat(4 * 1024 * 1024 + 1)));
	}

	@Test
	public void presentationReferencesMustResolveToTheDeclaredNativeType() throws IOException {
		String content = """
				<element type="point" label="A"><condition showObject="visible"/></element>
				<element type="boolean" label="visible"><value val="true"/></element>
				""";
		var valid = GeoCeDGInspectionArchive.inspect(zip("geogebra.xml", document(content)));
		assertTrue(valid.findings().toString(), valid.loadable());
		assertRefused(Code.INVALID_XML, zip("geogebra.xml", document(content.replace(
				"showObject=\"visible\"", "showObject=\"missing\""))));
		assertRefused(Code.INVALID_XML, zip("geogebra.xml", document(content.replace(
				"type=\"boolean\"", "type=\"numeric\""))));
	}

	@Test
	public void securelyParsedSyntaxMustAgreeWithTheNativeTokenizer() throws IOException {
		String valid = document(POINTS);
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				valid.replace("</construction>", "</construction >")));
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				valid.replace("(0, 0)", "(0,\t0)")));
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				valid.replace("</construction>", "<!--" + ">".repeat(100_000) + "--></construction>")));
		assertRefused(Code.INVALID_XML, zip("geogebra.xml", valid.replace("utf-8", "utf-16")));
		Inspection entity = GeoCeDGInspectionArchive.inspect(zip("geogebra.xml",
				valid.replace("(0, 0)", "(0,&#x20;0)")));
		assertTrue(entity.findings().toString(), entity.loadable());
	}

	@Test
	public void unitMetadataUsesTheExistingNativeCodec() throws IOException {
		for (String units : List.of(
				"<geocedgUnits version=\"1\" construction=\"mm\" presentation=\"cm\"/>",
				"<geocedgUnits version=\"2\" construction=\"mm\" dimensionUnitSuffix=\"hidden\"/>",
				"<geocedgUnits version=\"1\" construction=\"usm\" usmMetersPerUnit=\"0.0254\"/>")) {
			Inspection inspection = GeoCeDGInspectionArchive.inspect(
					zip("geogebra.xml", document(units + POINTS)));
			assertTrue(inspection.findings().toString(), inspection.loadable());
		}
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				document("<geocedgUnits version=\"2\" construction=\"mm\"/>" + POINTS)));
		assertRefused(Code.UNSUPPORTED_XML, zip("geogebra.xml",
				document("<geocedgUnits version=\"3\"/>" + POINTS)));
	}

	private static Inspection assertRefused(Code expected, byte[] bytes) {
		Inspection inspection = GeoCeDGInspectionArchive.inspect(bytes);
		assertFalse(inspection.loadable());
		assertTrue(inspection.findings().toString(),
				inspection.findings().stream().anyMatch(finding -> finding.code() == expected));
		assertThrows(IllegalStateException.class, inspection::openForNativeLoad);
		return inspection;
	}

	private static String document(String contents) {
		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				+ "<geogebra format=\"5.0\" app=\"classic\" id=\"" + DOCUMENT_ID
				+ "\" xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ " xsi:noNamespaceSchemaLocation=\"https://www.geogebra.org/apps/xsd/ggb.xsd\">"
				+ "<construction>" + contents + "</construction></geogebra>";
	}

	private static byte[] zip(String... pairs) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			for (int index = 0; index < pairs.length; index += 2) {
				zip.putNextEntry(new ZipEntry(pairs[index]));
				zip.write(pairs[index + 1].getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
		}
		return bytes.toByteArray();
	}
}

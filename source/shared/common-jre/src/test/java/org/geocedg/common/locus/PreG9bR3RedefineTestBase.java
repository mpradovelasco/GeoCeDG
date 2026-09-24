/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.test.commands.ErrorAccumulator;

/** Shared PRE-G9B-R3 fixtures and real-path redefine helpers. */
abstract class PreG9bR3RedefineTestBase extends G9U0PublicSurfaceTestBase {
	static final String MIDPOINT_FIXTURE = "/org/geocedg/common/locus/g9u0-r2/"
			+ "locusFromMidpoint.cedg";
	static final String MIDPOINT_FIXTURE_SHA256 =
			"47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c";
	private static final Pattern SPATIAL_GEO = Pattern.compile("<geo id=\"[^\"]+\"[^>]*/>");

	/** Result of one redefine submitted through the real algebra path. */
	static final class Outcome {
		private final SpatialRedefineAssessment assessment;
		private final String errors;

		Outcome(SpatialRedefineAssessment assessment, String errors) {
			this.assessment = assessment;
			this.errors = errors;
		}

		/** @return the kernel assessment the handler saw, if the target participates */
		SpatialRedefineAssessment assessment() {
			return assessment;
		}

		SpatialRedefineAssessmentStatus status() {
			return assessment == null ? null : assessment.getStatus();
		}

		String errors() {
			return errors;
		}

		boolean succeeded() {
			return errors.isBlank();
		}
	}

	/**
	 * Submits {@code definition} for {@code label} through the product redefine
	 * path. The chooser plays the frontend: it receives the kernel assessment and
	 * returns the explicitly selected mode, or {@code null} to cancel.
	 */
	Outcome redefine(String label, String definition,
			Function<SpatialRedefineAssessment, SpatialRedefineExecutionMode> chooser) {
		org.geogebra.common.kernel.geos.GeoElement target = requireLookup(label);
		AtomicReference<SpatialRedefineAssessment> seen = new AtomicReference<>();
		AlgebraProcessor processor = getKernel().getAlgebraProcessor();
		processor.setSpatialRedefineAssessmentHandler(assessment -> {
			seen.set(assessment);
			return chooser.apply(assessment);
		});
		ErrorAccumulator errors = new ErrorAccumulator();
		try {
			processor.changeGeoElementNoExceptionHandling(target, definition,
					EvalInfoFactory.getEvalInfoForRedefinition(getKernel(), target, true),
					true, null, errors);
		} finally {
			processor.setSpatialRedefineAssessmentHandler(null);
		}
		return new Outcome(seen.get(), errors.getErrors());
	}

	/** @return the mode the desktop frontend selects without asking the user */
	static SpatialRedefineExecutionMode retainWhenAvailable(
			SpatialRedefineAssessment assessment) {
		return assessment.getStatus()
				== SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE
				? SpatialRedefineExecutionMode.ADVANCED_RETAIN : null;
	}

	/** @return a chooser that always requests {@code mode}, authorized or not */
	static Function<SpatialRedefineAssessment, SpatialRedefineExecutionMode> always(
			SpatialRedefineExecutionMode mode) {
		return assessment -> mode;
	}

	/**
	 * Builds the construction core of the author witness: the point {@code E} is
	 * an ordinary participant whose direct input {@code D} has no durable identity.
	 */
	void witnessCore(String centre, String through) {
		getKernel().setContinuous(false);
		add("A=" + centre);
		add("B=" + through);
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		assertNotNull(add("a=LocusV2(E,C)"));
	}

	/** Loads the historical version-1 author fixture and returns its XML. */
	String loadMidpointFixture() throws Exception {
		byte[] archive = readResource(MIDPOINT_FIXTURE);
		assertEquals(MIDPOINT_FIXTURE_SHA256, sha256(archive));
		String xml = readZipEntry(archive, "geogebra.xml");
		getApp().setXML(xml, true);
		return xml;
	}

	SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	PersistentGeoId id(String label) {
		PersistentGeoId id = registry().getPersistentGeoId(requireLookup(label));
		assertNotNull(id, label);
		return id;
	}

	GeoIdentityRecord record(String label) {
		return record(id(label));
	}

	GeoIdentityRecord record(PersistentGeoId id) {
		GeoIdentityRecord record = registry().getGeoRecord(id);
		assertNotNull(record, id.toExternalForm());
		return record;
	}

	/** @return sorted durable ids of the labelled geos */
	List<PersistentGeoId> ids(String... labels) {
		ArrayList<PersistentGeoId> ids = new ArrayList<>();
		for (String label : labels) {
			ids.add(id(label));
		}
		Collections.sort(ids);
		return ids;
	}

	/** @return every construction-provider identity record */
	List<GeoIdentityRecord> constructionRecords() {
		ArrayList<GeoIdentityRecord> records = new ArrayList<>();
		for (SpatialIdentityRecord record : registry().getRecords()) {
			if (record instanceof GeoIdentityRecord
					&& ConstructionGeoRedefineProvider.PROVIDER_ID.equals(
							((GeoIdentityRecord) record).getProvider())) {
				records.add((GeoIdentityRecord) record);
			}
		}
		return records;
	}

	/** @return the {@code <geo/>} identity records of a native document, in order */
	static List<String> spatialGeoRecords(String xml) {
		int start = xml.indexOf("<geocedgSpatial");
		int end = xml.indexOf("</geocedgSpatial>");
		assertTrue(start >= 0 && end > start, "native document has no spatial section");
		ArrayList<String> records = new ArrayList<>();
		Matcher matcher = SPATIAL_GEO.matcher(xml.substring(start, end));
		while (matcher.find()) {
			records.add(matcher.group());
		}
		return records;
	}

	/** @return the document with one attribute of one identity record replaced */
	static String withRecordAttribute(String xml, PersistentGeoId id,
			String attribute, String value) {
		String prefix = "<geo id=\"" + id.toExternalForm() + "\"";
		int start = xml.indexOf(prefix);
		assertTrue(start >= 0, "record " + id.toExternalForm() + " is absent");
		int end = xml.indexOf("/>", start);
		String element = xml.substring(start, end);
		Matcher matcher = Pattern.compile(" " + attribute + "=\"[^\"]*\"")
				.matcher(element);
		assertTrue(matcher.find(), attribute + " is absent from " + element);
		String changed = element.substring(0, matcher.start()) + " " + attribute + "=\""
				+ value + "\"" + element.substring(matcher.end());
		return xml.substring(0, start) + changed + xml.substring(end);
	}

	static String externalForms(List<PersistentGeoId> ids) {
		StringBuilder forms = new StringBuilder();
		for (PersistentGeoId id : ids) {
			if (forms.length() > 0) {
				forms.append(' ');
			}
			forms.append(id.toExternalForm());
		}
		return forms.toString();
	}

	/** Asserts that a fresh GeoCeDG rejects the document without partial state. */
	static void assertLoadRejected(String xml, SpatialIdentityDiagnostic.Code code) {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> app.getXMLio().processXMLString(xml, true, false, false));
		assertEquals(code, failure.getDiagnostic().getCode(),
				failure.getDiagnostic().toString());
		assertTrue(app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.isEmpty());
		assertFalse(app.getKernel().getConstruction().isFileLoading());
	}

	/** @return a fresh GeoCeDG application that opened the document */
	static AppCommon openInFreshApp(String xml) throws Exception {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		app.getXMLio().processXMLString(xml, true, false, false);
		return app;
	}

	/** @return finite solution coordinates of a rich intersection, sorted */
	static List<double[]> solutionPoints(GeoLocusIntersectionResult result) {
		assertNotNull(result.getIntersectionResult());
		ArrayList<double[]> points = new ArrayList<>();
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			points.add(new double[] {solution.getEvaluatedPoint().getX(),
					solution.getEvaluatedPoint().getY()});
		}
		points.sort((first, second) -> first[0] != second[0]
				? Double.compare(first[0], second[0])
				: Double.compare(first[1], second[1]));
		return points;
	}

	/** Asserts the same point set up to order within {@code tolerance}. */
	static void assertSamePoints(List<double[]> expected, List<double[]> actual,
			double tolerance) {
		assertEquals(expected.size(), actual.size());
		ArrayList<double[]> unmatched = new ArrayList<>(actual);
		for (double[] point : expected) {
			double[] match = unmatched.stream().filter(candidate ->
					Math.abs(candidate[0] - point[0]) < tolerance
							&& Math.abs(candidate[1] - point[1]) < tolerance)
					.findFirst().orElse(null);
			assertNotNull(match, "no solution at (" + point[0] + ", " + point[1] + ")");
			unmatched.remove(match);
		}
	}

	private static byte[] readResource(String name) throws IOException {
		try (InputStream input = PreG9bR3RedefineTestBase.class
				.getResourceAsStream(name)) {
			assertNotNull(input, "Missing author fixture " + name);
			return input.readAllBytes();
		}
	}

	private static String readZipEntry(byte[] archive, String name)
			throws IOException {
		try (ZipInputStream zip = new ZipInputStream(
				new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (name.equals(entry.getName())) {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					zip.transferTo(output);
					return output.toString(StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing " + name + " in author fixture");
	}

	private static String sha256(byte[] value) throws NoSuchAlgorithmException {
		return HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(value));
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.main.LocalizationD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Original-payload byte identity with the explicit candidate CU7 native-checkpoint
 * exception: Classic construction/full XML stay strictly byte identical. GeoCeDG
 * compares the same immutable hashes after removing only independently validated
 * version-one native checkpoint lines. No other existing bytes or unit metadata
 * may change. This is not an unqualified whole-XML byte-identity assertion or an
 * adopted amendment to D1. The unchanged fixture was produced on the original tree
 * and reproduced on a git archive of the published base.
 */
@ExtendWith(PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class)
class PreG9BR6PlusD1LegacyByteIdentityTest {

	@Test
	// The earlier failed whole-XML receipt remains evidence. This assertion checks
	// the exact original payload under the explicitly validated candidate exception.
	void legacyCorpusOriginalPayloadIsByteIdenticalAfterValidatedCu7NativeMetadata() throws Exception {
		assertValidatedCu7ProjectionFailsClosed();
		JSONObject fixture;
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(
				PreG9BR6PlusD1LegacyFingerprints.FIXTURE)) {
			assertNotNull(input, PreG9BR6PlusD1LegacyFingerprints.FIXTURE);
			fixture = new JSONObject(new String(input.readAllBytes(), StandardCharsets.UTF_8));
		}
		Path root = PreG9BR6PlusD1LegacyFingerprints.findRepositoryRoot();
		for (String configuration : new String[] {"geocedg", "classic"}) {
			Map<String, PreG9BR6PlusD1LegacyFingerprints.Fingerprint> actual =
					PreG9BR6PlusD1LegacyFingerprints
							.computeWithValidatedCu7NativePayloadProjection(root,
									"classic".equals(configuration), 1);
			JSONArray expected = fixture.getJSONArray(configuration);
			Map<String, JSONObject> byPath = new TreeMap<>();
			for (int i = 0; i < expected.length(); i++) {
				byPath.put(expected.getJSONObject(i).getString("path"),
						expected.getJSONObject(i));
			}
			assertEquals(byPath.keySet(), actual.keySet(), configuration);
			for (Map.Entry<String, PreG9BR6PlusD1LegacyFingerprints.Fingerprint> entry
					: actual.entrySet()) {
				JSONObject row = byPath.get(entry.getKey());
				String context = configuration + " " + entry.getKey()
						+ ("classic".equals(configuration) ? " strict whole XML"
								: " original payload; candidate validated CU7 checkpoint exception");
				if (row.has("excluded")) {
					assertEquals(row.getString("excluded"), entry.getValue().exclusion, context);
					continue;
				}
				assertEquals(row.getString("constructionXmlSha256"),
						entry.getValue().constructionXmlSha256, context);
				assertEquals(row.getString("fullXmlSha256"), entry.getValue().fullXmlSha256,
						context);
				assertFalse(entry.getValue().containsUnitElement, context);
			}
		}
	}

	private static void assertValidatedCu7ProjectionFailsClosed() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			try {
				assertValidatedCu7ProjectionFailsClosedOnEventThread();
			} catch (Exception failure) {
				throw new IllegalStateException(failure);
			}
		});
	}

	private static void assertValidatedCu7ProjectionFailsClosedOnEventThread() throws Exception {
		Log previousLogger = Log.getLogger();
		AppDNoGui app;
		try {
			app = new AppDNoGui(new LocalizationD(3), true, new AppConfigGeoCeDG(true, true));
		} finally {
			Log.setLogger(previousLogger);
		}
		try {
			var algebra = app.getKernel().getAlgebraProcessor();
			algebra.processAlgebraCommand("c=Circle((0,0),1)", false);
			algebra.processAlgebraCommand("P=Point(c)", false);
			GeoPoint point = (GeoPoint) app.getKernel().lookupLabel("P");
			point.updatePathParameter(.25); point.updateCascade();
			String xml = app.getXML();
			var lineMatch = Pattern.compile("(?m)^[\\t ]*(<conicPointState[^\\r\\n]*?/>)(?:\\r?\\n)")
					.matcher(xml);
			assertTrue(lineMatch.find(), "A real ordinary-construction checkpoint is required");
			String line = lineMatch.group(), tag = lineMatch.group(1);
			assertFalse(lineMatch.find(), "The fixture has one native checkpoint");
			assertEquals(xml.replace(line, ""), PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml, app), "Only the exact added line is removed");
			String unrelated = xml.replace("</geogebra>", "<!-- unrelated payload bytes -->\n</geogebra>");
			assertEquals(unrelated.replace(line, ""), PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(unrelated, app), "No unrelated byte normalization");
			for (String invalidTag : new String[] {
					tag.replace("version=\"1\"", "version=\"2\""),
					// F7: DOM must reject both duplicate orders before any attribute
					// map can select a version. Payload projection accepts one empty
					// self-closing leaf, matching the native loader's focal grammar.
					tag.replace("version=\"1\"", "version=\"99\" version=\"1\""),
					tag.replace("version=\"1\"", "version=\"1\" version=\"99\""),
					tag.replace("/>", "><unexpected/></conicPointState>"),
					tag.replace("/>", ">unexpected</conicPointState>"),
					tag.replace("/>", "></conicPointState>"),
					tag.replace("/>", "><![CDATA[text]]></conicPointState>"),
					tag.replace("/>", " extra=\"1\"/>"),
					tag.replaceFirst("version=\"1\" (t=\"[^\"]*\")", "$1 version=\"1\""),
					tag.replaceFirst(" t=\"[^\"]*\"", " t=\"0.5\""),
					tag.replaceFirst(" x=\"[^\"]*\"", " x=\"100\""),
					tag.replaceFirst(" tempT=\"[^\"]*\"", " tempT=\"0.125\""),
					tag.replaceFirst(" tempPathType=\"[^\"]*\"", " tempPathType=\"999\""),
					tag.replaceFirst(" animation=\"[^\"]*\"", " animation=\"0.99\"")}) {
				assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
						.projectValidatedCu7NativePayload(xml.replace(tag, invalidTag), app), invalidTag);
			}
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml.replace(line, line + line), app),
					"Duplicate ownership cannot be projected away");
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml.replace(line, tag), app),
					"Inline tags are not the added complete line");
			var ownerMatch = Pattern.compile("(?s)<element type=\"point\" label=\"P\"[^>]*>.*?</element>")
					.matcher(xml);
			assertTrue(ownerMatch.find()); String owner = ownerMatch.group();
			var coordsMatch = Pattern.compile("(?m)^[\\t ]*<coords[^\\r\\n]*?/>\\r?\\n").matcher(owner);
			assertTrue(coordsMatch.find()); String coords = coordsMatch.group();
			String afterCoords = owner.replace(line, "").replace(coords, coords + line);
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml.replace(owner, afterCoords), app),
					"Checkpoint after ordinary coordinates is not eligible");
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml.replace(owner, owner.replace(coords, "")), app),
					"Missing ordinary coordinates cannot be hidden");
			String wrongCoordinates = owner.replace(coords,
					coords.replaceFirst(" x=\"[^\"]*\"", " x=\"100\""));
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml.replace(owner, wrongCoordinates), app),
					"Legacy coordinate payload remains authoritative");
			String macroCollision = xml.replace(line, "").replace("</geogebra>",
					"<macro><construction>" + owner + "</construction></macro></geogebra>");
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(macroCollision, app),
					"Nested macro point sharing a live label is not the main point");
			app.getKernel().lookupLabel("c").setUndefined(); point.updateCascade();
			assertThrows(Exception.class, () -> PreG9BR6PlusD1LegacyFingerprints
					.projectValidatedCu7NativePayload(xml, app), "Undefined state is outside the payload exception");
		} finally {
			app.getKernel().getAnimationManager().stopAnimation();
		}
	}
}

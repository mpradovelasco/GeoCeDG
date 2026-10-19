/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU0 regression corpus: the versioned copies of the author's 2026-10-19 models keep
 * the identity recorded by their manifest, the native model keeps the {@code e_2}
 * composition chain the track characterizes, and the derived variant without {@code e_2}
 * removes exactly that step.
 */
class PreG9BCu0CorpusTest {

	@TempDir
	Path work;

	@Test
	void versionedCopiesMatchTheManifestAndTheBaseline() throws Exception {
		Path root = PreG9BCu0Corpus.repositoryRoot();
		PreG9BCu0Corpus.versioned(PreG9BCu0Corpus.CEDG);
		PreG9BCu0Corpus.versioned(PreG9BCu0Corpus.GGB);
		JSONObject manifest = new JSONObject(Files.readString(root.resolve(
				"models/regression/pre-g9b-cu/manifest.yml"), StandardCharsets.UTF_8));
		assertEquals("cedg.regression.pre-g9b-cu", manifest.getString("id"));
		assertFalse(manifest.getBoolean("loaded_by_default"));
		JSONObject metrics = manifest.getJSONObject("expected_metrics");
		assertEquals(PreG9BCu0Corpus.CEDG_SHA256, metrics.getJSONObject(PreG9BCu0Corpus.CEDG)
				.getString("sha256"));
		assertEquals(PreG9BCu0Corpus.CEDG_BYTES, metrics.getJSONObject(PreG9BCu0Corpus.CEDG)
				.getLong("bytes"));
		assertEquals(PreG9BCu0Corpus.GGB_SHA256, metrics.getJSONObject(PreG9BCu0Corpus.GGB)
				.getString("sha256"));
		assertEquals(PreG9BCu0Corpus.GGB_BYTES, metrics.getJSONObject(PreG9BCu0Corpus.GGB)
				.getLong("bytes"));
		JSONArray evidence = new JSONObject(Files.readString(root.resolve(
				"models/regression/pre-g9b-cu/expected-results.yml"), StandardCharsets.UTF_8))
				.getJSONArray("author_evidence");
		assertEquals(2, evidence.length());
		for (int i = 0; i < evidence.length(); i++) {
			JSONObject entry = evidence.getJSONObject(i);
			String name = entry.getString("filename");
			assertEquals(PreG9BCu0Corpus.CEDG.equals(name) ? PreG9BCu0Corpus.CEDG_SHA256
					: PreG9BCu0Corpus.GGB_SHA256, entry.getString("sha256"), name);
			assertEquals(PreG9BCu0Corpus.CORPUS + "/" + name, entry.getString("versioned_copy"));
		}
		JSONObject corpus = PreG9BCu0Baseline.load().getJSONObject("corpus");
		assertEquals(PreG9BCu0Corpus.CEDG_SHA256, corpus.getString(PreG9BCu0Corpus.CEDG));
		assertEquals(PreG9BCu0Corpus.GGB_SHA256, corpus.getString(PreG9BCu0Corpus.GGB));
	}

	@Test
	void nativeModelKeepsTheCharacterizedCompositionChain() throws IOException {
		String xml = PreG9BCu0Corpus.constructionXml(PreG9BCu0Corpus.CEDG);
		for (String step : List.of("LocusV2|K_1,E_1|o_1", "Point|o_1,text2,v_1|N_1",
				"LocusLength|o_1,N_1,K_1|w_1", "Length|w_1|d_2", "pointJump|L_1,g_2,d_2|Q_1",
				"translationCoor|J_1,O_1,k,Q_1,j_2|R_1", "LocusV2|R_1,E_1|e_2",
				"Intersect|o_1,h_2|u_1", "LocusV2|W,N|o", "LocusV2|J_1,E_1|e_1")) {
			assertTrue(hasCommand(xml, step), "missing construction step " + step);
		}
		assertEquals(7, count(xml, "<element type=\"locusv2\""));
		assertTrue(xml.contains("<expression label=\"v_1\" exp=\"0\"/>"));
		assertTrue(xml.contains("<continuous val=\"false\"/>"));
	}

	@Test
	void derivedVariantRemovesExactlyTheLastStep() throws IOException {
		String original = PreG9BCu0Corpus.constructionXml(PreG9BCu0Corpus.CEDG);
		String variant = xmlOf(PreG9BCu0Corpus.withoutE2(work).toPath());
		assertFalse(variant.contains("\"e_2\""));
		assertEquals(count(original, "<element ") - 1, count(variant, "<element "));
		assertEquals(count(original, "<command ") - 1, count(variant, "<command "));
		assertEquals(count(original, "<geo id=") - 1, count(variant, "<geo id="));
		assertFalse(hasCommand(variant, "LocusV2|R_1,E_1|e_2"));
		assertTrue(hasCommand(variant, "translationCoor|J_1,O_1,k,Q_1,j_2|R_1"));
	}

	private static boolean hasCommand(String xml, String step) {
		String[] parts = step.split("\\|");
		StringBuilder input = new StringBuilder();
		String[] inputs = parts[1].split(",");
		for (int i = 0; i < inputs.length; i++) {
			input.append(" a").append(i).append("=\"").append(Pattern.quote(inputs[i]))
					.append('"');
		}
		Pattern command = Pattern.compile("<command name=\"" + Pattern.quote(parts[0])
				+ "\">\\s*<input" + input + "/>\\s*<output a0=\"" + Pattern.quote(parts[2])
				+ "\"");
		return command.matcher(xml).find();
	}

	private static int count(String text, String token) {
		Matcher matcher = Pattern.compile(Pattern.quote(token)).matcher(text);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	private static String xmlOf(Path archive) throws IOException {
		try (ZipInputStream in = new ZipInputStream(Files.newInputStream(archive))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if ("geogebra.xml".equals(entry.getName())) {
					return new String(in.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IllegalStateException("geogebra.xml not found");
	}
}

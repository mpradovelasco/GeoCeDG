/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;

/**
 * Access to the committed PRE-G9B-CU0 geometric reference baseline
 * ({@value PreG9BCu0Corpus#BASELINE}) and the tolerance-aware comparison of a freshly
 * computed section with its baseline section.
 *
 * <p>A {@code preserve} comparison is a regression reference: corrections must reproduce the
 * value. A {@code pins} comparison is a characterization pin: it records the current defect
 * signature, names its owning package in the failure message and is to be inverted or
 * replaced by that package; it is not a normative expectation.
 */
final class PreG9BCu0Baseline {

	private PreG9BCu0Baseline() {
	}

	static JSONObject load() throws IOException, JSONException {
		return new JSONObject(Files.readString(PreG9BCu0Corpus.repositoryRoot()
				.resolve(PreG9BCu0Corpus.BASELINE), StandardCharsets.UTF_8));
	}

	static Object at(JSONObject root, String... path) throws JSONException {
		Object current = root;
		for (String name : path) {
			current = ((JSONObject) current).get(name);
		}
		return current;
	}

	static String pinMessage(String incidence, String owner, String detail) {
		return "CHARACTERIZATION PIN " + incidence + " (owner " + owner + "; reference evidence,"
				+ " not a normative expectation): " + detail;
	}

	/**
	 * Message of a pin inverted by its owning package: the committed baseline keeps the
	 * pinned signature as historical evidence and the test asserts the corrected behaviour.
	 */
	static String inversionMessage(String incidence, String owner, String detail) {
		return "PIN " + incidence + " INVERTED BY " + owner + ": " + detail;
	}

	/** Structural equality with absolute numeric tolerance; {@code where} names the path. */
	static void assertMatches(String where, Object expected, Object actual, double tolerance)
			throws JSONException {
		if (expected == JSONObject.NULL || expected == null) {
			assertNull(actual, where + ": expected null");
			return;
		}
		if (expected instanceof JSONObject object) {
			Map<?, ?> map = assertInstanceOf(Map.class, actual, where + ": expected an object");
			assertEquals(object.keySet(), stringKeys(map), where + ": keys");
			for (String key : object.keySet()) {
				assertMatches(where + "." + key, object.get(key), map.get(key), tolerance);
			}
			return;
		}
		if (expected instanceof JSONArray array) {
			List<?> list = assertInstanceOf(List.class, actual, where + ": expected an array");
			assertEquals(array.length(), list.size(), where + ": length");
			for (int i = 0; i < array.length(); i++) {
				assertMatches(where + "[" + i + "]", array.get(i), list.get(i), tolerance);
			}
			return;
		}
		if (expected instanceof Number number) {
			Number value = assertInstanceOf(Number.class, actual, where + ": expected a number");
			double difference = Math.abs(number.doubleValue() - value.doubleValue());
			assertTrue(difference <= tolerance, where + ": " + value + " differs from "
					+ number + " by " + difference + " > " + tolerance);
			return;
		}
		if (expected instanceof Boolean || expected instanceof String) {
			assertEquals(expected, actual, where);
			return;
		}
		fail(where + ": unsupported baseline value " + expected);
	}

	private static java.util.Set<String> stringKeys(Map<?, ?> map) {
		java.util.Set<String> keys = new java.util.LinkedHashSet<>();
		for (Object key : map.keySet()) {
			keys.add(String.valueOf(key));
		}
		return keys;
	}
}

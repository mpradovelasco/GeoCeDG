/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.HashSet;
import java.util.Set;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;

/** Strict, bounded interpreter for the keywords used by the packaged profile schema. */
final class GeoCeDGProfileSchema {

	private static final Set<String> KEYWORDS = Set.of("$schema", "$id", "$comment",
			"title", "description", "$defs", "$ref", "type", "required", "properties",
			"additionalProperties", "items", "prefixItems", "uniqueItems", "minItems",
			"maxItems", "minLength", "pattern", "minimum", "maximum", "exclusiveMinimum",
			"exclusiveMaximum", "enum", "const");

	private GeoCeDGProfileSchema() {
	}

	static void validate(JSONObject document, JSONObject schema) throws JSONException {
		validateValue(document, schema, schema, "$", 0);
	}

	private static void validateValue(Object value, JSONObject schema, JSONObject root,
			String path, int depth) throws JSONException {
		require(depth < 96, path, "schema nesting limit");
		for (String key : schema.keySet()) {
			require(KEYWORDS.contains(key), path, "unsupported schema keyword " + key);
		}
		if (schema.has("$ref")) {
			String reference = schema.getString("$ref");
			require(reference.startsWith("#/$defs/")
					&& reference.indexOf('/', 8) < 0, path, "unsupported reference");
			validateValue(value, root.getJSONObject("$defs")
					.getJSONObject(reference.substring(8)), root, path, depth + 1);
			return;
		}
		if (schema.has("const")) {
			require(schema.get("const").equals(value), path, "constant mismatch");
		}
		if (schema.has("enum")) {
			JSONArray choices = schema.getJSONArray("enum");
			boolean found = false;
			for (int i = 0; i < choices.length(); i++) {
				found |= choices.get(i).equals(value);
			}
			require(found, path, "not in enumeration");
		}
		if (schema.has("type")) {
			String type = schema.getString("type");
			boolean matches;
			switch (type) {
			case "object":
				matches = value instanceof JSONObject;
				break;
			case "array":
				matches = value instanceof JSONArray;
				break;
			case "string":
				matches = value instanceof String;
				break;
			case "boolean":
				matches = value instanceof Boolean;
				break;
			case "number":
				matches = value instanceof Number;
				break;
			case "integer":
				matches = value instanceof Number
						&& ((Number) value).doubleValue() == ((Number) value).longValue();
				break;
			default:
				throw new IllegalStateException("Unsupported schema type " + type);
			}
			require(matches, path, "expected " + type);
		}
		if (value instanceof JSONObject) {
			validateObject((JSONObject) value, schema, root, path, depth);
		} else if (value instanceof JSONArray) {
			validateArray((JSONArray) value, schema, root, path, depth);
		} else if (value instanceof String) {
			String text = (String) value;
			require(text.length() >= schema.optInt("minLength", 0), path, "short string");
			if (schema.has("pattern")) {
				require(java.util.regex.Pattern.compile(schema.getString("pattern"))
						.matcher(text).find(), path, "pattern mismatch");
			}
		} else if (value instanceof Number) {
			double number = ((Number) value).doubleValue();
			require(Double.isFinite(number), path, "non-finite number");
			for (String key : new String[] {"minimum", "maximum",
					"exclusiveMinimum", "exclusiveMaximum"}) {
				if (schema.has(key)) {
					double bound = schema.getDouble(key);
					boolean valid = "minimum".equals(key) ? number >= bound
							: "maximum".equals(key) ? number <= bound
							: "exclusiveMinimum".equals(key) ? number > bound : number < bound;
					require(valid, path, key);
				}
			}
		}
	}

	private static void validateObject(JSONObject value, JSONObject schema,
			JSONObject root, String path, int depth) throws JSONException {
		JSONArray required = schema.optJSONArray("required");
		if (required != null) {
			for (int i = 0; i < required.length(); i++) {
				require(value.has(required.getString(i)), path,
						"missing " + required.getString(i));
			}
		}
		JSONObject properties = schema.optJSONObject("properties");
		for (String key : value.keySet()) {
			if (properties != null && properties.has(key)) {
				validateValue(value.get(key), properties.getJSONObject(key), root,
						path + "." + key, depth + 1);
			} else {
				Object additional = schema.opt("additionalProperties");
				require(!Boolean.FALSE.equals(additional), path, "unknown property " + key);
				if (additional instanceof JSONObject) {
					validateValue(value.get(key), (JSONObject) additional, root,
							path + "." + key, depth + 1);
				}
			}
		}
	}

	private static void validateArray(JSONArray value, JSONObject schema,
			JSONObject root, String path, int depth) throws JSONException {
		require(value.length() >= schema.optInt("minItems", 0)
				&& value.length() <= schema.optInt("maxItems", Integer.MAX_VALUE),
				path, "array size");
		Set<Object> unique = new HashSet<>();
		JSONArray prefix = schema.optJSONArray("prefixItems");
		for (int i = 0; i < value.length(); i++) {
			Object item = value.get(i);
			if (schema.optBoolean("uniqueItems", false)) {
				require(unique.add(item), path, "duplicate item");
			}
			JSONObject itemSchema = prefix != null && i < prefix.length()
					? prefix.getJSONObject(i) : schema.optJSONObject("items");
			if (itemSchema != null) {
				validateValue(item, itemSchema, root, path + "[" + i + "]", depth + 1);
			}
		}
	}

	private static void require(boolean value, String path, String problem) {
		if (!value) {
			throw new IllegalStateException("Invalid GeoCeDG profile " + path + ": " + problem);
		}
	}
}

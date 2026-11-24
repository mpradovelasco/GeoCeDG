/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;

/**
 * PRE-G9B-MAINT-A2 (BS-13): deterministic text dump of immutable Locus V2 result objects.
 * Every floating value is written as its exact hexadecimal form, so two dumps are equal
 * only when every captured double has the same bits. Kernel objects, definitions,
 * evaluators, sessions and work counters are not traversed. Random durable identifiers and
 * digests of them (hexadecimal runs of 16 or more characters) are replaced by {@code #}, so
 * the text does not depend on the identifier source.
 */
final class MaintA2DeterministicDump {
	private static final Pattern RANDOM_HEX = Pattern.compile("[0-9a-f]{16,}");
	private static final int MAXIMUM_DEPTH = 48;

	private final IdentityHashMap<Object, Integer> seen = new IdentityHashMap<>();
	private final StringBuilder out = new StringBuilder();

	private MaintA2DeterministicDump() {
	}

	/**
	 * @param value object graph to dump
	 * @return canonical single-line text with normalized random identifiers
	 */
	static String of(Object value) {
		MaintA2DeterministicDump dump = new MaintA2DeterministicDump();
		dump.value(value, 0);
		return normalize(dump.out.toString());
	}

	/**
	 * @param text text that may contain random identifiers
	 * @return the text with each distinct random hexadecimal run replaced by its ordinal
	 */
	static String normalize(String text) {
		return RANDOM_HEX.matcher(text).replaceAll("#").replace("\r", "\\r")
				.replace("\n", "\\n").replace("\t", "\\t");
	}

	private void value(Object value, int depth) {
		if (depth > MAXIMUM_DEPTH) {
			out.append("<deep>");
			return;
		}
		if (value == null) {
			out.append("null");
		} else if (value instanceof Double || value instanceof Float) {
			out.append(Double.toHexString(((Number) value).doubleValue()));
		} else if (value instanceof Number || value instanceof Boolean
				|| value instanceof Character) {
			out.append(value);
		} else if (value instanceof String) {
			out.append('"').append(value).append('"');
		} else if (value instanceof Enum) {
			out.append(((Enum<?>) value).name());
		} else if (value instanceof Optional) {
			Optional<?> optional = (Optional<?>) value;
			out.append("Opt(");
			if (optional.isPresent()) {
				value(optional.get(), depth + 1);
			}
			out.append(')');
		} else if (value instanceof OptionalInt) {
			OptionalInt optional = (OptionalInt) value;
			out.append("Opt(").append(optional.isPresent() ? "" + optional.getAsInt() : "")
					.append(')');
		} else if (value instanceof OptionalLong) {
			OptionalLong optional = (OptionalLong) value;
			out.append("Opt(").append(optional.isPresent() ? "" + optional.getAsLong() : "")
					.append(')');
		} else if (value instanceof OptionalDouble) {
			OptionalDouble optional = (OptionalDouble) value;
			out.append("Opt(").append(optional.isPresent()
					? Double.toHexString(optional.getAsDouble()) : "").append(')');
		} else if (value.getClass().isArray()) {
			array(value, depth);
		} else if (value instanceof Set || isDiagnosticList(value)) {
			unordered((Collection<?>) value, depth);
		} else if (value instanceof Collection) {
			out.append('[');
			boolean first = true;
			for (Object element : (Collection<?>) value) {
				out.append(first ? "" : ",");
				first = false;
				value(element, depth + 1);
			}
			out.append(']');
		} else if (value instanceof Map) {
			map((Map<?, ?>) value, depth);
		} else if (isOpaque(value)) {
			out.append('<').append(value.getClass().getSimpleName()).append('>');
		} else {
			object(value, depth);
		}
	}

	/**
	 * Diagnostic lists of pair results are ordered by an iteration that depends on the
	 * random durable identifiers (observed on the base); their content is compared as a set.
	 */
	private static boolean isDiagnosticList(Object value) {
		if (!(value instanceof List) || ((List<?>) value).isEmpty()) {
			return false;
		}
		for (Object element : (List<?>) value) {
			if (element == null || !element.getClass().getSimpleName().endsWith("Diagnostic2D")) {
				return false;
			}
		}
		return true;
	}

	private static boolean isOpaque(Object value) {
		if (value.getClass().getSimpleName().endsWith("InstrumentationSnapshot2D")) {
			// Work counters of pair results depend on a processing order tied to the random
			// durable identifiers (observed on the base); they are not semantic results.
			return true;
		}
		String name = value.getClass().getName();
		return !name.startsWith("org.geocedg.") || value instanceof LocusDefinition2D
				|| value instanceof LocusEvaluator2D
				|| value instanceof LocusEvaluationSession2D
				|| value instanceof org.geogebra.common.kernel.geos.GeoElement;
	}

	private void array(Object array, int depth) {
		out.append('[');
		for (int index = 0; index < Array.getLength(array); index++) {
			out.append(index == 0 ? "" : ",");
			value(Array.get(array, index), depth + 1);
		}
		out.append(']');
	}

	private void unordered(Collection<?> collection, int depth) {
		List<String> parts = new ArrayList<>();
		for (Object element : collection) {
			parts.add(nested(element, depth + 1));
		}
		parts.sort(Comparator.comparing(MaintA2DeterministicDump::normalize));
		out.append('{').append(String.join(",", parts)).append('}');
	}

	private void map(Map<?, ?> map, int depth) {
		List<String> parts = new ArrayList<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			parts.add(nested(entry.getKey(), depth + 1) + "="
					+ nested(entry.getValue(), depth + 1));
		}
		parts.sort(Comparator.comparing(MaintA2DeterministicDump::normalize));
		out.append('{').append(String.join(",", parts)).append('}');
	}

	private String nested(Object value, int depth) {
		MaintA2DeterministicDump child = new MaintA2DeterministicDump();
		child.seen.putAll(seen);
		child.value(value, depth);
		return child.out.toString();
	}

	private void object(Object value, int depth) {
		Integer reference = seen.get(value);
		if (reference != null) {
			out.append('@').append(reference);
			return;
		}
		seen.put(value, seen.size() + 1);
		List<Field> fields = new ArrayList<>();
		for (Class<?> type = value.getClass(); type != null && type != Object.class;
				type = type.getSuperclass()) {
			for (Field field : type.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
					fields.add(field);
				}
			}
		}
		fields.sort(Comparator.comparing(field -> field.getDeclaringClass().getName()
				+ "." + field.getName()));
		out.append(value.getClass().getSimpleName()).append('{');
		boolean first = true;
		for (Field field : fields) {
			out.append(first ? "" : ";");
			first = false;
			out.append(field.getName()).append('=');
			try {
				field.setAccessible(true);
				value(field.get(value), depth + 1);
			} catch (ReflectiveOperationException | RuntimeException failure) {
				out.append("<inaccessible>");
			}
		}
		out.append('}');
	}
}

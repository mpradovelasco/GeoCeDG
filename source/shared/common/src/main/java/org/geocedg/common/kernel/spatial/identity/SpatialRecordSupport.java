/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Package-local validation and immutable-collection helpers. */
final class SpatialRecordSupport {
	private SpatialRecordSupport() {
	}

	static String requireText(String value, String name) {
		Objects.requireNonNull(value, name);
		if (value.isEmpty() || !isAsciiLetterOrDigit(value.charAt(0))) {
			throw new IllegalArgumentException(name
					+ " must start with an ASCII letter or digit");
		}
		for (int index = 1; index < value.length(); index++) {
			char character = value.charAt(index);
			if (!isAsciiLetterOrDigit(character) && character != '.'
					&& character != '_' && character != '-' && character != ':'
					&& character != '/' && character != '+') {
				throw new IllegalArgumentException(name
						+ " must use canonical semantic-token characters");
			}
		}
		return value;
	}

	private static boolean isAsciiLetterOrDigit(char character) {
		return character >= 'A' && character <= 'Z'
				|| character >= 'a' && character <= 'z'
				|| character >= '0' && character <= '9';
	}

	static int requirePositive(int value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(name + " must be positive");
		}
		return value;
	}

	static long requireRevision(long value, String name) {
		if (value < 0) {
			throw new IllegalArgumentException(name + " must not be negative");
		}
		return value;
	}

	static <T extends SpatialIdentityId> List<T> immutableIds(List<T> values) {
		Objects.requireNonNull(values);
		ArrayList<T> copy = new ArrayList<>();
		for (T value : values) {
			T checked = Objects.requireNonNull(value);
			if (!copy.contains(checked)) {
				copy.add(checked);
			}
		}
		Collections.sort(copy, new Comparator<T>() {
			@Override
			public int compare(T first, T second) {
				return first.compareTo(second);
			}
		});
		return Collections.unmodifiableList(copy);
	}

	static List<SpatialIdentityId> references(SpatialIdentityId... direct) {
		ArrayList<SpatialIdentityId> references = new ArrayList<>();
		for (SpatialIdentityId identity : direct) {
			if (identity != null && !references.contains(identity)) {
				references.add(identity);
			}
		}
		return references;
	}

	static List<SpatialIdentityId> references(List<? extends SpatialIdentityId> first,
			List<? extends SpatialIdentityId> second, SpatialIdentityId... direct) {
		List<SpatialIdentityId> references = references(direct);
		addReferences(references, first);
		addReferences(references, second);
		Collections.sort(references);
		return Collections.unmodifiableList(references);
	}

	private static void addReferences(List<SpatialIdentityId> target,
			List<? extends SpatialIdentityId> source) {
		for (SpatialIdentityId identity : source) {
			if (!target.contains(identity)) {
				target.add(identity);
			}
		}
	}

	@SuppressWarnings("unchecked")
	static <T extends SpatialIdentityId> T remap(T identity,
			Map<SpatialIdentityId, SpatialIdentityId> remap) {
		SpatialIdentityId mapped = remap.get(identity);
		return (T) (mapped == null ? identity : mapped);
	}

	static <T extends SpatialIdentityId> List<T> remap(List<T> identities,
			Map<SpatialIdentityId, SpatialIdentityId> remap) {
		ArrayList<T> result = new ArrayList<>();
		for (T identity : identities) {
			result.add(SpatialRecordSupport.<T>remap(identity, remap));
		}
		return result;
	}
}

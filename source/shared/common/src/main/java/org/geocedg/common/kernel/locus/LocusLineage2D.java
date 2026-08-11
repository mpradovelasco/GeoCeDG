/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.LineageTransition;

/** Typed lifecycle metadata, separate from stable branch identity. */
public final class LocusLineage2D {
	private final LineageTransition transition;
	private final List<String> parentKeys;
	private final List<String> childKeys;

	/** Creates immutable lineage metadata. */
	public LocusLineage2D(LineageTransition transition, List<String> parentKeys,
			List<String> childKeys) {
		this.transition = Objects.requireNonNull(transition);
		this.parentKeys = checkedKeys(parentKeys);
		this.childKeys = checkedKeys(childKeys);
		validateShape();
	}

	/** @return an unchanged lifecycle */
	public static LocusLineage2D unchanged() {
		return new LocusLineage2D(LineageTransition.UNCHANGED,
				Collections.<String>emptyList(), Collections.<String>emptyList());
	}

	public LineageTransition getTransition() {
		return transition;
	}

	public List<String> getParentKeys() {
		return parentKeys;
	}

	public List<String> getChildKeys() {
		return childKeys;
	}

	private void validateShape() {
		if (transition == LineageTransition.SPLIT && childKeys.size() < 2) {
			throw new IllegalArgumentException("Split lineage needs child keys");
		}
		if (transition == LineageTransition.MERGED && parentKeys.size() < 2) {
			throw new IllegalArgumentException("Merged lineage needs parent keys");
		}
	}

	private static List<String> checkedKeys(List<String> keys) {
		Objects.requireNonNull(keys);
		ArrayList<String> copy = new ArrayList<>();
		for (String key : keys) {
			if (key == null || key.trim().isEmpty()) {
				throw new IllegalArgumentException("Lineage keys must be stable");
			}
			copy.add(key);
		}
		return Collections.unmodifiableList(copy);
	}
}

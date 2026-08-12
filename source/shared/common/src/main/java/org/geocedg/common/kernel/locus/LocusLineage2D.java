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
		switch (transition) {
		case UNCHANGED:
			if (!parentKeys.isEmpty() || !childKeys.isEmpty()) {
				throw new IllegalArgumentException("Unchanged lineage has no edges");
			}
			break;
		case APPEARED:
			if (!parentKeys.isEmpty() || childKeys.isEmpty()) {
				throw new IllegalArgumentException("Appeared lineage needs only children");
			}
			break;
		case DISAPPEARED:
			if (parentKeys.isEmpty() || !childKeys.isEmpty()) {
				throw new IllegalArgumentException("Disappeared lineage needs only parents");
			}
			break;
		case SPLIT:
			if (parentKeys.size() != 1 || childKeys.size() < 2) {
				throw new IllegalArgumentException(
						"Split lineage needs one parent and at least two children");
			}
			break;
		case MERGED:
			if (parentKeys.size() < 2 || childKeys.size() != 1) {
				throw new IllegalArgumentException(
						"Merged lineage needs at least two parents and one child");
			}
			break;
		default:
			throw new IllegalStateException("Unhandled lineage transition");
		}
	}

	private static List<String> checkedKeys(List<String> keys) {
		Objects.requireNonNull(keys);
		ArrayList<String> copy = new ArrayList<>();
		for (String key : keys) {
			if (key == null || key.trim().isEmpty()) {
				throw new IllegalArgumentException("Lineage keys must be stable");
			}
			if (copy.contains(key)) {
				throw new IllegalArgumentException("Lineage keys must be unique");
			}
			copy.add(key);
		}
		return Collections.unmodifiableList(copy);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusLineage2D)) {
			return false;
		}
		LocusLineage2D lineage = (LocusLineage2D) other;
		return transition == lineage.transition
				&& parentKeys.equals(lineage.parentKeys)
				&& childKeys.equals(lineage.childKeys);
	}

	@Override
	public int hashCode() {
		return Objects.hash(transition, parentKeys, childKeys);
	}

	@Override
	public String toString() {
		return transition + "[parents=" + parentKeys + ", children=" + childKeys + "]";
	}
}

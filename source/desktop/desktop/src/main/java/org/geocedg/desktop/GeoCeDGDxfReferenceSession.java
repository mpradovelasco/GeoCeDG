/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.geocedg.common.importer.dxf.DxfSplineReference;

/**
 * PRE-G9B-MAINT-A-DXF0 session store {@code R-a} of imported DXF references
 * ({@code dxf-spline-reference/v1} section 11). It belongs to the open document's session,
 * never to {@code Construction}: importing, hiding or removing a reference is no
 * construction step and adds no Undo/Redo entry; Undo/Redo never touch it; Save writes
 * nothing of it; New, Open and Close clear it. Visibility is a session presentation state,
 * not a layer of the construction's layer domain.
 */
public final class GeoCeDGDxfReferenceSession {

	/** One stored reference and its session visibility. */
	public static final class Entry {
		private final DxfSplineReference reference;
		private boolean visible = true;

		Entry(DxfSplineReference reference) {
			this.reference = reference;
		}

		/** @return the immutable record */
		public DxfSplineReference reference() {
			return reference;
		}

		/** @return whether the view draws it */
		public boolean isVisible() {
			return visible;
		}
	}

	private final List<Entry> entries = new ArrayList<>();
	private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

	/** @param listener called after every change (presentation repaint) */
	void addListener(Runnable listener) {
		listeners.add(listener);
	}

	/** @param references admitted records to add, in file order */
	void addAll(List<DxfSplineReference> references) {
		if (references.isEmpty()) {
			return;
		}
		for (DxfSplineReference reference : references) {
			entries.add(new Entry(reference));
		}
		changed();
	}

	/** @param index entry to remove (explicit user action, no construction step) */
	void remove(int index) {
		entries.remove(index);
		changed();
	}

	/**
	 * @param index entry
	 * @param visible new session visibility
	 */
	void setVisible(int index, boolean visible) {
		entries.get(index).visible = visible;
		changed();
	}

	/** Clears the store (New, Open, Close). */
	void clear() {
		if (!entries.isEmpty()) {
			entries.clear();
			changed();
		}
	}

	/** @return current entries, unmodifiable */
	public List<Entry> entries() {
		return Collections.unmodifiableList(new ArrayList<>(entries));
	}

	/** @return whether no reference is stored */
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	/** @return number of stored references */
	public int size() {
		return entries.size();
	}

	private void changed() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}
}

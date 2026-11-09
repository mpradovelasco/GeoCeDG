/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.document.controls;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.document.controls.ControlLayoutException.Code;
import org.geocedg.common.kernel.spatial.identity.DefaultSpatialTokenSource;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoButton;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.GeoClass;

/** Document presentation data for controls in Desktop Graphics 1. */
public final class ControlLayout {
	public static final int MAX_PANELS = 32;
	public static final int MAX_SLOTS = 256;
	public static final int MAX_NAME_CODE_POINTS = 64;
	public static final int DEFAULT_RGB = 0xe1edf7;
	public static final int DEFAULT_OPACITY = 65;
	public static final int MIN_OPACITY = 25;
	public static final int MAX_OPACITY = 80;

	public enum Anchor {
		FIXED, WORLD
	}

	public record Panel(String id, Anchor anchor, double x, double y, int width, int height,
			boolean visible, String name, int rgb, int opacity) {
		public Panel(String id, Anchor anchor, double x, double y, int width, int height,
				boolean visible) {
			this(id, anchor, x, y, width, height, visible, "", DEFAULT_RGB, DEFAULT_OPACITY);
		}

		public Panel {
			Objects.requireNonNull(id);
			Objects.requireNonNull(anchor);
			requireFinite(x, y);
			if (width <= 0 || height <= 0) {
				throw failure(Code.GRAMMAR, "Panel dimensions must be positive");
			}
			requireValidName(name);
			if (rgb < 0 || rgb > 0xffffff || opacity < MIN_OPACITY || opacity > MAX_OPACITY) {
				throw failure(Code.GRAMMAR, "Invalid control panel background");
			}
		}

		/** Changes only the presentation style of this panel. */
		public Panel withStyle(String name, int rgb, int opacity) {
			return new Panel(id, anchor, x, y, width, height, visible, name, rgb, opacity);
		}

		boolean hasDefaultStyle() {
			return name.isEmpty() && rgb == DEFAULT_RGB && opacity == DEFAULT_OPACITY;
		}
	}

	public record Slot(String panelId, String id, GeoElement control, double dx, double dy) {
		public Slot {
			Objects.requireNonNull(panelId);
			Objects.requireNonNull(id);
			Objects.requireNonNull(control);
			requireFinite(dx, dy);
		}
	}

	private final Construction document;
	private final Set<String> issuedIds = new HashSet<>();
	private DefaultSpatialTokenSource tokens;
	private State state = new State();

	/** Creates an empty layout owned by the specified construction. */
	public ControlLayout(Construction document) {
		this.document = document;
	}

	/** @return whether this document has any panels, including empty panels */
	public boolean isEmpty() {
		return state.panels.isEmpty();
	}

	/** @return a snapshot of the panels in composition order, back to front */
	public List<Panel> panels() {
		return List.copyOf(state.panels.values());
	}

	/** @return the panel record, or null for an unknown ID */
	public Panel panel(String id) {
		return state.panels.get(id);
	}

	/** @return a snapshot of the slots owned by a panel */
	public List<Slot> slots(String panelId) {
		var slots = state.slotsByPanel.get(panelId);
		return slots == null ? List.of() : List.copyOf(slots.values());
	}

	/** @return the slot of exactly this live instance, or null */
	public Slot slot(GeoElement control) {
		return state.byControl.get(control);
	}

	/** Adds an empty panel with a fresh documentary ID. */
	public Panel addPanel(Anchor anchor, double x, double y, int width, int height,
			boolean visible) {
		return addPanel(anchor, x, y, width, height, visible, "", DEFAULT_RGB, DEFAULT_OPACITY);
	}

	/** Adds a styled empty panel at the front with a fresh documentary ID. */
	public Panel addPanel(Anchor anchor, double x, double y, int width, int height,
			boolean visible, String name, int rgb, int opacity) {
		Panel panel = new Panel(nextId(), anchor, x, y, width, height, visible, name, rgb, opacity);
		state.addPanel(panel);
		return panel;
	}

	/** Replaces only the presentation fields of an existing panel. */
	public void updatePanel(Panel panel) {
		requirePanel(panel.id());
		state.panels.put(panel.id(), panel);
	}

	/** Releases the panel's members without deleting the native controls. */
	public void removePanel(String panelId) {
		requirePanel(panelId);
		state.slotsByPanel.remove(panelId).values()
				.forEach(slot -> state.byControl.remove(slot.control()));
		state.panels.remove(panelId);
	}

	/** Adds an eligible native control to one panel without modifying its properties. */
	public Slot addSlot(String panelId, GeoElement control, double dx, double dy) {
		requirePanel(panelId);
		requireEligible(control);
		Slot slot = new Slot(panelId, nextId(), control, dx, dy);
		state.addSlot(slot);
		return slot;
	}

	/** Updates the offsets while preserving the slot and its owner. */
	public void updateSlot(Slot slot) {
		Slot current = state.byControl.get(slot.control());
		if (current == null || !current.panelId().equals(slot.panelId())
				|| !current.id().equals(slot.id())) {
			throw failure(Code.OWNER, "Slot update needs its original owner and identity");
		}
		requireEligible(slot.control());
		state.byControl.put(slot.control(), slot);
		state.slotsByPanel.get(slot.panelId()).put(slot.id(), slot);
	}

	/** Releases an exact instance, including during native deletion. */
	public Slot removeSlot(GeoElement control) {
		Slot slot = state.byControl.remove(control);
		if (slot != null) {
			state.slotsByPanel.get(slot.panelId()).remove(slot.id());
		}
		return slot;
	}

	/** Clears membership for a native construction replacement. */
	public void clear() {
		if (!state.panels.isEmpty()) {
			state = new State();
		}
	}

	/** Eligibility is independent of the control's current effective visibility. */
	public boolean isEligible(GeoElement control) {
		if (control == null || control.getConstruction() != document
				|| control.isDefaultGeo() || !control.isLabelSet()
				|| !document.isInConstructionList(control)
				|| !document.getGeoSetConstructionOrder().contains(control)
				|| control.getParentGroup() != null || control.isLockedPosition()
				|| !control.isVisibleInView(App.VIEW_EUCLIDIAN)
				|| control.isVisibleInView3D() || control.isVisibleInViewForPlane()) {
			return false;
		}
		List<Integer> views = control.getViewSet();
		if (views != null && (views.size() != 1 || views.get(0) != App.VIEW_EUCLIDIAN)) {
			return false;
		}
		if (control instanceof GeoNumeric numeric && control.getGeoClassType() == GeoClass.NUMERIC) {
			return numeric.isIndependent() && numeric.isAbsoluteScreenLocActive()
					&& numeric.getIntervalMinObject() != null
					&& numeric.getIntervalMaxObject() != null && staticPoint(numeric.getStartPoint());
		}
		if (control instanceof GeoButton button) {
			return button.isAbsoluteScreenLocActive() && staticPoint(button.getStartPoint());
		}
		return control instanceof GeoBoolean checkbox && checkbox.isIndependent()
				&& checkbox.getStartPoint() == null;
	}

	static String family(GeoElement control) {
		if (control instanceof GeoInputBox) {
			return "I";
		}
		if (control instanceof GeoButton) {
			return "B";
		}
		if (control instanceof GeoBoolean) {
			return "C";
		}
		return "S";
	}

	private static boolean staticPoint(GeoPointND point) {
		return point == null || point.isAbsoluteStartPoint();
	}

	void requireEligible(GeoElement control) {
		if (!isEligible(control)) {
			throw failure(Code.OWNER, "Slot owner is not an eligible Graphics 1 control");
		}
	}

	private void requirePanel(String id) {
		if (panel(id) == null) {
			throw failure(Code.ORPHAN, "Unknown control panel");
		}
	}

	private String nextId() {
		if (tokens == null) {
			tokens = new DefaultSpatialTokenSource();
		}
		for (int attempt = 0; attempt < 32; attempt++) {
			String hex = tokens.nextToken();
			String id = hex.substring(0, 8) + "-" + hex.substring(8, 12)
					+ "-4" + hex.substring(13, 16) + "-8" + hex.substring(17, 20)
					+ "-" + hex.substring(20);
			if (issuedIds.add(id)) {
				return id;
			}
		}
		throw failure(Code.DUPLICATE, "Unable to allocate a fresh control layout identity");
	}

	void publish(State parsed) {
		issuedIds.addAll(parsed.panels.keySet());
		for (Map<String, Slot> panelSlots : parsed.slotsByPanel.values()) {
			issuedIds.addAll(panelSlots.keySet());
		}
		state = parsed;
	}

	private static void requireFinite(double x, double y) {
		if (!Double.isFinite(x) || !Double.isFinite(y)) {
			throw failure(Code.GRAMMAR, "Control layout coordinate must be finite");
		}
	}

	private static void requireValidName(String name) {
		if (name == null) {
			throw failure(Code.GRAMMAR, "Missing control panel name");
		}
		int count = 0;
		for (int index = 0; index < name.length();) {
			if (++count > MAX_NAME_CODE_POINTS) {
				throw failure(Code.LIMIT, "Control panel name exceeds 64 Unicode scalars");
			}
			int codePoint = name.codePointAt(index);
			if (codePoint <= 0x1f || codePoint >= 0x7f && codePoint <= 0x9f
					|| codePoint >= 0xd800 && codePoint <= 0xdfff
					|| codePoint >= 0xfdd0 && codePoint <= 0xfdef
					|| (codePoint & 0xfffe) == 0xfffe) {
				throw failure(Code.GRAMMAR, "Invalid Unicode scalar in control panel name");
			}
			index += Character.charCount(codePoint);
		}
	}

	static ControlLayoutException failure(Code code, String detail) {
		return new ControlLayoutException(code, detail);
	}

	/** Mutable until publication; maintains the same ownership rules as interactive edits. */
	static final class State {
		private final Map<String, Panel> panels = new LinkedHashMap<>();
		private final Map<String, Map<String, Slot>> slotsByPanel = new LinkedHashMap<>();
		private final Map<GeoElement, Slot> byControl = new IdentityHashMap<>();

		void addPanel(Panel panel) {
			if (panels.size() == MAX_PANELS) {
				throw failure(Code.LIMIT, "Too many control panels");
			}
			if (panels.putIfAbsent(panel.id(), panel) != null) {
				throw failure(Code.DUPLICATE, "Duplicate control panel");
			}
			slotsByPanel.put(panel.id(), new LinkedHashMap<>());
		}

		void addSlot(Slot slot) {
			if (byControl.size() == MAX_SLOTS) {
				throw failure(Code.LIMIT, "Too many control slots");
			}
			Map<String, Slot> panelSlots = slotsByPanel.get(slot.panelId());
			if (panelSlots == null) {
				throw failure(Code.ORPHAN, "Slot references an unknown control panel");
			}
			if (panelSlots.containsKey(slot.id()) || byControl.containsKey(slot.control())) {
				throw failure(Code.DUPLICATE, "Duplicate control slot or owner");
			}
			panelSlots.put(slot.id(), slot);
			byControl.put(slot.control(), slot);
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.Map;

import org.geocedg.desktop.GeoCeDGLineReferenceMemory.Slot;
import org.geogebra.desktop.main.AppD;

/**
 * PRE-G9B-R6-plus-F2-R1 activation behaviour of the Parallel and Perpendicular tools
 * (geocedg/specs/ui/authoring-memory.md section 11). Session state only: never
 * serialized, never an undo point, independent of the remembered references.
 *
 * <p>Genuine activation requests are marked at their origins: the native toolbar button
 * (modifier read from its own action event and bound to the AWT event being
 * dispatched), and the single-argument {@code AppGeoCeDG.setMode(int)} entry used by
 * the toolbar button, menus, the action registry, user tools and scripting, which is a
 * {@code NORMAL} request unless a mark or an enclosing scope already decides (Option
 * O1, decision record v1.3 section 7; reference memory is interactive only). Every
 * other mode change is host synchronization: a different mode is a tool departure
 * (both tools return to {@code NORMAL}), the same mode changes nothing. A mark is
 * consumed by the first activation it decides and never reaches a later one.
 */
final class GeoCeDGToolActivation {

	/** Operating behaviour of one tool. */
	enum Behaviour {
		/** inherited tool; the memory is never read, written or cleared by the tool */
		NORMAL,
		/** approved F2 remembered-reference behaviour */
		MEMORY_ENABLED
	}

	/** Result of resolving one mode activation. */
	static final class Outcome {
		private final boolean sameMode;
		private final boolean changed;

		Outcome(boolean sameMode, boolean changed) {
			this.sameMode = sameMode;
			this.changed = changed;
		}

		/** @return whether an active Parallel/Perpendicular tool changed its behaviour */
		boolean sameModeBehaviourChange() {
			return sameMode && changed;
		}
	}

	/** Behaviour captured around a host language change. */
	static final class Snapshot {
		final int mode;
		final Behaviour behaviour;
		final long genuineActivations;

		Snapshot(int mode, Behaviour behaviour, long genuineActivations) {
			this.mode = mode;
			this.behaviour = behaviour;
			this.genuineActivations = genuineActivations;
		}
	}

	private final Map<Slot, Behaviour> behaviours = new EnumMap<>(Slot.class);
	private int toolbarMode = -1;
	private Behaviour toolbarBehaviour;
	private Object toolbarToken;
	private Behaviour scopedBehaviour;
	private boolean scopedGenuine;
	private boolean scopedConsumed;
	private int hostSynchronization;
	private long genuineActivations;

	/**
	 * @param event toolbar action event
	 * @return whether the event carries the memory modifier: Ctrl, or the host's
	 *         platform equivalent through {@link AppD#isControlDown(boolean, boolean)}
	 */
	static boolean requestsMemory(ActionEvent event) {
		int modifiers = event.getModifiers();
		return AppD.isControlDown((modifiers & ActionEvent.META_MASK) != 0,
				(modifiers & ActionEvent.CTRL_MASK) != 0);
	}

	/**
	 * Records a native-toolbar activation; the following {@code setMode} of the same
	 * AWT dispatch consumes it.
	 *
	 * @param event toolbar action event (command = mode number)
	 */
	void recordToolbarActivation(ActionEvent event) {
		int mode;
		try {
			mode = Integer.parseInt(event.getActionCommand());
		} catch (NumberFormatException exception) {
			return;
		}
		if (Slot.forMode(mode) == null) {
			return;
		}
		toolbarMode = mode;
		toolbarBehaviour = requestsMemory(event) ? Behaviour.MEMORY_ENABLED
				: Behaviour.NORMAL;
		toolbarToken = EventQueue.getCurrentEvent();
	}

	/**
	 * Runs a single-argument activation request (toolbar button, menu, registry, user
	 * tools, scripting): unmarked, it is a genuine {@code NORMAL} request, also for the
	 * active tool. A pending toolbar mark of the same AWT event still decides (resolved
	 * first); inside an explicit request or a host-synchronization scope the call adds
	 * no request of its own.
	 *
	 * @param action the inherited single-argument activation
	 */
	void runAsSingleArgumentRequest(Runnable action) {
		if (scopedBehaviour != null || hostSynchronization > 0) {
			action.run();
		} else {
			runWithRequest(Behaviour.NORMAL, true, action);
		}
	}

	/**
	 * Runs host work (a language change) whose mode changes are synchronization only.
	 *
	 * @param action host work
	 */
	void runAsHostSynchronization(Runnable action) {
		hostSynchronization++;
		try {
			action.run();
		} finally {
			hostSynchronization--;
		}
	}

	/**
	 * Restores a captured tool after host work, as synchronization.
	 *
	 * @param snapshot captured behaviour
	 * @param action the restoring mode change
	 */
	void runAsRestore(Snapshot snapshot, Runnable action) {
		runWithRequest(snapshot.behaviour, false, action);
	}

	/**
	 * Runs an activation with an explicit request (also the test seam for activating
	 * {@code MEMORY_ENABLED} without a posted toolbar event).
	 *
	 * @param behaviour requested behaviour
	 * @param genuine whether it is a genuine user request
	 * @param action activation
	 */
	void runWithRequest(Behaviour behaviour, boolean genuine, Runnable action) {
		Behaviour previous = scopedBehaviour;
		boolean previousGenuine = scopedGenuine;
		boolean previousConsumed = scopedConsumed;
		scopedBehaviour = behaviour;
		scopedGenuine = genuine;
		scopedConsumed = false;
		try {
			action.run();
		} finally {
			scopedBehaviour = previous;
			scopedGenuine = previousGenuine;
			scopedConsumed = previousConsumed;
		}
	}

	/**
	 * @param activeMode active Graphics View 1 mode
	 * @return the captured behaviour of an active Parallel/Perpendicular tool, or null
	 */
	Snapshot snapshot(int activeMode) {
		Slot slot = Slot.forMode(activeMode);
		return slot == null ? null
				: new Snapshot(activeMode, behaviour(slot), genuineActivations);
	}

	/**
	 * @param snapshot captured state
	 * @return whether no genuine activation happened since the capture
	 */
	boolean noGenuineActivationSince(Snapshot snapshot) {
		return snapshot.genuineActivations == genuineActivations;
	}

	/**
	 * Resolves one mode activation before it is applied.
	 *
	 * @param targetMode requested mode
	 * @param activeMode active Graphics View 1 mode
	 * @return outcome
	 */
	Outcome resolve(int targetMode, int activeMode) {
		Behaviour requested = null;
		boolean genuine = false;
		if (toolbarToken != null) {
			if (toolbarMode == targetMode && toolbarToken == EventQueue.getCurrentEvent()) {
				requested = toolbarBehaviour;
				genuine = true;
			}
			toolbarToken = null;
			toolbarMode = -1;
		}
		if (scopedBehaviour != null && !scopedConsumed) {
			// the explicit request decides only its own activation; nested host
			// activations inside it resolve as unmarked
			scopedConsumed = true;
			if (requested == null) {
				requested = scopedBehaviour;
				genuine = scopedGenuine;
			}
		}
		if (genuine) {
			genuineActivations++;
		}
		boolean sameMode = targetMode == activeMode;
		Slot target = Slot.forMode(targetMode);
		if (!sameMode && hostSynchronization == 0 && requested == null) {
			// tool departure (or arrival without a request): both tools return to NORMAL
			behaviours.clear();
		}
		if (target == null) {
			if (!sameMode && hostSynchronization == 0) {
				behaviours.clear();
			}
			return new Outcome(sameMode, false);
		}
		Behaviour before = behaviour(target);
		Behaviour next = requested != null ? requested : before;
		if (!sameMode && requested != null) {
			behaviours.clear();
		}
		behaviours.put(target, next);
		return new Outcome(sameMode, before != next);
	}

	/**
	 * @param slot tool slot
	 * @return its behaviour
	 */
	Behaviour behaviour(Slot slot) {
		return behaviours.getOrDefault(slot, Behaviour.NORMAL);
	}

	/**
	 * @param mode tool mode
	 * @return whether that tool reads and writes its memory
	 */
	boolean isMemoryEnabled(int mode) {
		Slot slot = Slot.forMode(mode);
		return slot != null && behaviour(slot) == Behaviour.MEMORY_ENABLED;
	}
}

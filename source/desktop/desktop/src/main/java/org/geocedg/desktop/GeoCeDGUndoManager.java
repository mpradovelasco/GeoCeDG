/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.UndoTemplate;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.geogebra3D.kernel3D.ConstructionDefaults3D;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPlane3D;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.main.undo.AppState;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.main.undo.UndoHistory;
import org.geogebra.desktop.main.undo.FileAppState;
import org.geogebra.desktop.main.undo.UndoManagerD;

/**
 * PRE-G9B-R6-plus-F3 undo manager of every GeoCeDG document (recovery design addendum
 * successor sections 1 and 4), with the CU7 R3 focal native Undo companion.
 * Inherited scheduling and history transfer remain in place. Empty/unsupported
 * native captures keep the historical file state. The additions are:
 *
 * <ul>
 * <li>conservative pending-store accounting C1: {@code pending == 0 && !degraded}
 * implies that no asynchronous store of the current history generation can still
 * mutate the history. Over-counting only refuses an insertion; under-counting never
 * happens because a decrement requires the store body's own success path;</li>
 * <li>detaching and re-attaching the original command objects with their cursor, so
 * that labels, grouping, Redo states and snapshot files are moved, never copied (the
 * inherited {@code undoHistoryTo} copy drops labels and grouping, ER-F3R-3);</li>
 * <li>an owned native file state carries immutable inputs alongside its private
 * XML. Its normal reader uses a fresh typed session, with exact atomic failure
 * recovery and the original history cursor.</li>
 * </ul>
 *
 * <p>The constructor may run inside the host application constructor (the factory is
 * stateless), so this class has no instance field initializer.
 */
class GeoCeDGUndoManager extends UndoManagerD {
	/** Synchronous callers of the inherited notification (re-entrancy depth). */
	private static final ThreadLocal<int[]> SYNCHRONOUS = ThreadLocal.withInitial(() -> new int[1]);
	/** Set on a store thread once its body has added its command and pruned. */
	private static final ThreadLocal<Boolean> STORE_SUCCEEDED = new ThreadLocal<>();

	private int pending;
	private boolean degraded;
	private long epoch;
	private long historyActivity;
	// Lazily initialized: this manager may be called during application bootstrap.
	private Map<StringBuilder, UndoTemplate> pendingNativeStates;

	/**
	 * R12: creation defaults are not part of construction-only Undo XML. Native
	 * style copying nevertheless initializes a source prototype's null animation
	 * step. Preserve precisely that lazy change, without changing the kernel getter
	 * or rebinding any non-null, possibly dynamic, historical default.
	 */
	static final class LazyDefaults {
		private record Prototype(GeoElement geo, NumberValue step, long bits, boolean defined) { }

		private final Construction construction;
		private final Map<Integer, Prototype> prototypes;
		private final String classicXml;
		private final String v2Xml;

		private LazyDefaults(Construction construction) {
			this.construction = construction;
			prototypes = new LinkedHashMap<>();
			Set<GeoElement> graph = liveGraph(construction);
			for (var entry : construction.getConstructionDefaults().getDefaultGeos()) {
				GeoElement geo = entry.getValue();
				if (geo == null || geo.getConstruction() != construction) {
					throw new IllegalStateException("DYNAMIC_ANIMATION_STEP");
				}
				NumberValue step = geo.getAnimationStepObject();
				if (!ownedConstant(construction, step, graph)) {
					throw new IllegalStateException("DYNAMIC_ANIMATION_STEP");
				}
				prototypes.put(entry.getKey(), new Prototype(geo, step,
						step == null ? 0 : Double.doubleToRawLongBits(step.getDouble()),
						step != null && step.isDefined()));
			}
			classicXml = defaultsXml(construction);
			v2Xml = construction.getConstructionDefaults().getV2DefaultsXML();
		}

		static LazyDefaults capture(Construction construction) {
			return new LazyDefaults(construction);
		}

		/** Pure IMAGE admission: constants never acquire a semantic remapping. */
		static String unsupportedImageStep(Construction construction) {
			Set<GeoElement> graph = liveGraph(construction);
			for (var entry : construction.getConstructionDefaults().getDefaultGeos()) {
				GeoElement geo = entry.getValue();
				if (geo == null || geo.getConstruction() != construction
						|| !ownedConstant(construction, geo.getAnimationStepObject(), graph)) {
					return "DYNAMIC_ANIMATION_STEP";
				}
			}
			return null;
		}

		boolean matchesCurrent() {
			if (unsupportedImageStep(construction) != null) { return false; }
			Map<Integer, GeoElement> current = currentPrototypes(construction);
			if (!current.keySet().equals(prototypes.keySet())) { return false; }
			for (var entry : prototypes.entrySet()) {
				Prototype saved = entry.getValue();
				GeoElement geo = current.get(entry.getKey());
				NumberValue step = geo.getAnimationStepObject();
				if (geo != saved.geo || step != saved.step
						|| (step != null && !sameValue(step, saved))) { return false; }
			}
			return sameXml();
		}

		void restoreConstruction() throws IOException {
			restore(false);
		}

		void restoreArchive() throws IOException {
			restore(true);
		}

		private void restore(boolean archive) throws IOException {
			Map<Integer, GeoElement> current = currentPrototypes(construction);
			if (!current.keySet().equals(prototypes.keySet())) {
				throw new IOException("DEFAULT_PROTOTYPES_CHANGED");
			}
			Set<GeoElement> graph = archive ? liveGraph(construction) : null;
			Map<Integer, MyDouble> omittedPlaneSteps = new HashMap<>();
			// Validate every field before the first write. A full archive reader may
			// replace prototypes, but no old GeoNumeric reference is ever installed.
			for (var entry : prototypes.entrySet()) {
				Prototype saved = entry.getValue();
				GeoElement geo = current.get(entry.getKey());
				if (geo == null || geo.getConstruction() != construction
						|| geo.getClass() != saved.geo.getClass() || (!archive && geo != saved.geo)) {
					throw new IOException("DEFAULT_PROTOTYPES_CHANGED");
				}
				NumberValue step = geo.getAnimationStepObject();
				if (saved.step == null) {
					boolean literalOne = archive && ownedConstant(construction, step, graph)
							&& step != null && step.isDefined() && Double.doubleToLongBits(step.getDouble())
									== Double.doubleToLongBits(1.0);
					if (step != null && !lazyStep(construction, step) && !literalOne) {
						throw new IOException("UNEXPECTED_LAZY_DEFAULT_CHANGE");
					}
				} else if (archive && step == null && geo != saved.geo
						&& entry.getKey() == ConstructionDefaults3D.DEFAULT_PLANE3D
						&& geo.getClass() == GeoPlane3D.class) {
					// The plane writer omits animation XML. Only a newly reconstructed
					// plane may recover that omitted literal from its owned MEMORY snapshot.
					MyDouble literal = new MyDouble(construction.getKernel(),
							Double.longBitsToDouble(saved.bits));
					if (!sameValue(literal, saved)) {
						throw new IOException("UNEXPECTED_LAZY_DEFAULT_CHANGE");
					}
					omittedPlaneSteps.put(entry.getKey(), literal);
				} else {
					if ((!archive && step != saved.step) || step == null
							|| (archive && !ownedConstant(construction, step, graph))
							|| !sameValue(step, saved)) {
						throw new IOException("UNEXPECTED_LAZY_DEFAULT_CHANGE");
					}
				}
			}
			for (var entry : prototypes.entrySet()) {
				if (entry.getValue().step == null) {
					current.get(entry.getKey()).setAnimationStep((NumberValue) null);
				} else if (omittedPlaneSteps.containsKey(entry.getKey())) {
					current.get(entry.getKey()).setAnimationStep(omittedPlaneSteps.get(entry.getKey()));
				}
			}
			if (!sameXml()) { throw new IOException("DEFAULTS_SERIALIZATION_CHANGED"); }
		}

		private boolean sameXml() {
			return classicXml.equals(defaultsXml(construction)) && Objects.equals(v2Xml,
					construction.getConstructionDefaults().getV2DefaultsXML());
		}

		private static boolean sameValue(NumberValue step, Prototype saved) {
			return step.isDefined() == saved.defined
					&& Double.doubleToRawLongBits(step.getDouble()) == saved.bits;
		}

		private static String defaultsXml(Construction construction) {
			XMLStringBuilder xml = new XMLStringBuilder();
			construction.getConstructionDefaults().getDefaultsXML(xml);
			return xml.toString();
		}

		private static Map<Integer, GeoElement> currentPrototypes(Construction construction) {
			Map<Integer, GeoElement> current = new LinkedHashMap<>();
			for (var entry : construction.getConstructionDefaults().getDefaultGeos()) {
				current.put(entry.getKey(), entry.getValue());
			}
			return current;
		}

		private static boolean lazyStep(Construction construction, NumberValue step) {
			return step != null && step.getClass() == MyDouble.class
					&& step.wrap().getKernel() == construction.getKernel()
					&& Double.doubleToLongBits(step.getDouble())
							== Double.doubleToLongBits(GeoNumeric.DEFAULT_SLIDER_INCREMENT);
		}

		private static boolean ownedConstant(Construction construction, NumberValue step,
				Set<GeoElement> graph) {
			if (step == null) { return true; }
			if (step.getClass() == MyDouble.class) {
				return step.wrap().getKernel() == construction.getKernel();
			}
			return step.getClass() == GeoNumeric.class
					&& ((GeoNumeric) step).getConstruction() == construction
					&& ((GeoNumeric) step).isIndependent() && !((GeoNumeric) step).isLabelSet()
					&& !graph.contains(step);
		}

		/** Includes the inactive suffix and unlabelled inputs/list members by identity. */
		private static Set<GeoElement> liveGraph(Construction construction) {
			Set<GeoElement> graph = Collections.newSetFromMap(new IdentityHashMap<>());
			ArrayDeque<GeoElement> pending = new ArrayDeque<>(construction.getGeoSetConstructionOrder());
			for (int i = 0; i < construction.steps(); i++) {
				var element = construction.getConstructionElement(i);
				for (var output : element.getGeoElements()) {
					if (output != null) { pending.add(output.toGeoElement()); }
				}
				if (element instanceof AlgoElement algo) {
					for (GeoElement input : algo.getInput()) {
						if (input != null) { pending.add(input); }
					}
				}
			}
			while (!pending.isEmpty()) {
				GeoElement geo = pending.remove();
				if (!graph.add(geo)) { continue; }
				if (geo instanceof GeoList list) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) != null) { pending.add(list.get(i)); }
					}
				}
				AlgoElement parent = geo.getParentAlgorithm();
				if (parent != null) {
					for (GeoElement input : parent.getInput()) {
						if (input != null) { pending.add(input); }
					}
				}
			}
			return graph;
		}

		/** Separate narrow replay scope; non-null historical defaults are untouched. */
		static LazyNulls captureLazyNulls(Construction construction) {
			Map<Integer, GeoElement> nulls = new LinkedHashMap<>();
			for (var entry : construction.getConstructionDefaults().getDefaultGeos()) {
				GeoElement geo = entry.getValue();
				if (geo.getConstruction() == construction && geo.getAnimationStepObject() == null) {
					nulls.put(entry.getKey(), geo);
				}
			}
			return new LazyNulls(construction, nulls);
		}

		static final class LazyNulls implements AutoCloseable {
			private final Construction construction;
			private final Map<Integer, GeoElement> nulls;

			LazyNulls(Construction construction, Map<Integer, GeoElement> nulls) {
				this.construction = construction;
				this.nulls = nulls;
			}

			@Override public void close() throws IOException {
				for (var entry : nulls.entrySet()) {
					GeoElement geo = entry.getValue();
					if (construction.getConstructionDefaults().getDefaultGeo(entry.getKey()) != geo) {
						throw new IOException("DEFAULT_PROTOTYPES_CHANGED");
					}
					NumberValue step = geo.getAnimationStepObject();
					if (step != null && !lazyStep(construction, step)) {
						throw new IOException("UNEXPECTED_LAZY_DEFAULT_CHANGE");
					}
				}
				for (GeoElement geo : nulls.values()) { geo.setAnimationStep((NumberValue) null); }
			}
		}
	}

	/**
	 * @param cons construction whose history this manager owns
	 */
	GeoCeDGUndoManager(Construction cons) {
		super(cons);
	}

	@Override
	protected synchronized StringBuilder captureUndoXML() {
		var captured = construction.captureNativeUndoSnapshot();
		if (captured == null) { return super.captureUndoXML(); }
		if (pendingNativeStates == null) { pendingNativeStates = new IdentityHashMap<>(); }
		pendingNativeStates.put(captured.xml(), captured.template());
		return captured.xml();
	}

	@Override
	protected synchronized FileAppState createUndoState(StringBuilder xml) throws IOException {
		UndoTemplate template = pendingNativeStates == null ? null : pendingNativeStates.remove(xml);
		if (template == null) {
			if (xml.indexOf("<" + NativeTransactionalState2D.BINDING_TAG) >= 0) {
				throw new IOException("Private native Undo XML has no pending owned companion");
			}
			return super.createUndoState(xml);
		}
		if (!template.matchesXml(xml.toString().getBytes(StandardCharsets.UTF_8))) {
			throw new IOException("Native Undo XML changed after caller capture");
		}
		return new NativeFileAppState(this, construction, xml, template);
	}

	/** File and immutable native companion move and are disposed as one original AppState. */
	static final class NativeFileAppState extends FileAppState {
		private final GeoCeDGUndoManager owner;
		private final Construction document;
		private volatile UndoTemplate template;

		private NativeFileAppState(GeoCeDGUndoManager manager, Construction construction,
				StringBuilder xml, UndoTemplate captured) throws IOException {
			super(xml);
			owner = manager; document = construction; template = captured;
		}

		private UndoTemplate requireTemplate(GeoCeDGUndoManager manager, Construction construction) {
			UndoTemplate current = template;
			if (owner != manager || document != construction || current == null
					|| !current.belongsTo(construction)) {
				throw new UndoStateLoadFailure("Foreign or disposed native Undo state", null);
			}
			return current;
		}

		@Override public void delete() {
			try { super.delete(); }
			finally { template = null; }
		}
	}

	/** Removes only authenticated private Undo bindings from an expert comparison. */
	String publicUndoXml(UndoCommand command, String xml) {
		if (command.getAppState() instanceof NativeFileAppState nativeState) {
			UndoTemplate template = nativeState.requireTemplate(this, construction);
			return new String(template.publicXml(xml.getBytes(StandardCharsets.UTF_8)),
					StandardCharsets.UTF_8);
		}
		return xml;
	}

	@Override
	protected void readUndoState(FileAppState state, InputStream input)
			throws IOException, XMLParseException {
		try (var defaults = LazyDefaults.captureLazyNulls(construction)) {
			readUndoStateWithDefaults(state, input);
		}
	}

	private void readUndoStateWithDefaults(FileAppState state, InputStream input)
			throws IOException, XMLParseException {
		if (!(state instanceof NativeFileAppState nativeState)) {
			super.readUndoState(state, input);
			return;
		}
		boolean wasFileLoading = construction.isFileLoading();
		try {
			UndoTemplate template = nativeState.requireTemplate(this, construction);
			byte[] archive = input.readAllBytes();
			byte[] xml;
			try (var zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
				var entry = zip.getNextEntry();
				if (entry == null || !MyXMLio.XML_FILE.equals(entry.getName())) {
					throw new UndoStateLoadFailure("Native Undo snapshot has no construction entry", null);
				}
				xml = zip.readAllBytes();
				if (!template.matchesXml(xml) || zip.getNextEntry() != null) {
					throw new UndoStateLoadFailure("Native Undo snapshot bytes do not match their companion", null);
				}
			}
			var currentReason = NativeTransactionalState2D.unsupportedReason(construction);
			// GeoCeDG CU7 R4: exhausted work is not an unsupported-history permission.
			NativeTransactionalState2D.requireAvailable(currentReason);
			if (currentReason != null) {
				// No exact pre-state exists for this legacy history transition. Project
				// only this authenticated snapshot's private bindings, validating the
				// original public digest; never grant a new CU7 editing permission.
				byte[] publicXml = template.publicXml(xml);
				var historical = new ByteArrayOutputStream();
				MyXMLioJre.writeZipped(historical,
						new StringBuilder(new String(publicXml, StandardCharsets.UTF_8)));
				super.readUndoState(state, new ByteArrayInputStream(historical.toByteArray()));
				return;
			}
			construction.restoreNativeUndoSnapshot(template, () -> {
				try {
					super.readUndoState(state, new ByteArrayInputStream(archive));
				} catch (IOException | XMLParseException | RuntimeException | Error failure) {
					throw new UndoStateLoadFailure("Native Undo reader failed", failure);
				}
			});
		} catch (IOException | RuntimeException | Error failure) {
			throw failure instanceof UndoStateLoadFailure ? (UndoStateLoadFailure) failure
					: new UndoStateLoadFailure("Native Undo restoration failed", failure);
		} finally {
			construction.setFileLoading(wasFileLoading);
			app.getEventDispatcher().enableListeners();
		}
	}

	@Override
	protected void handleUndoLoadFailure(AppState state, Throwable failure) {
		if (state instanceof NativeFileAppState) {
			app.getEventDispatcher().enableListeners();
			throw failure instanceof UndoStateLoadFailure ? (UndoStateLoadFailure) failure
					: new UndoStateLoadFailure("Owned native Undo state could not be read", failure);
		}
		super.handleUndoLoadFailure(state, failure);
	}

	@Override
	public synchronized void undo() {
		historyActivity++;
		runOwnedHistoryOperation(super::undo);
	}

	@Override
	public synchronized void redo() {
		historyActivity++;
		runOwnedHistoryOperation(super::redo);
	}

	private void runOwnedHistoryOperation(Runnable operation) {
		boolean nativeHistory = undoInfoList.stream()
				.anyMatch(command -> command.getAppState() instanceof NativeFileAppState);
		if (!nativeHistory) { operation.run(); return; }
		var currentReason = NativeTransactionalState2D.unsupportedReason(construction);
		NativeTransactionalState2D.requireAvailable(currentReason);
		boolean exactCurrent = currentReason == null;
		List<UndoCommand> originalCommands = new ArrayList<>(undoInfoList);
		int originalCursor = getCursor();
		try {
			if (!exactCurrent) {
				// Legacy construction fidelity only. Still restore the history cursor
				// when an owned target fails owner/digest validation before mutation.
				operation.run();
				return;
			}
			construction.runNativeUndoOperation(() -> {
				try { operation.run(); }
				catch (RuntimeException | Error failure) {
					// The inherited reader re-enables listeners on exit. Recovery of
					// the enclosing exact pre-state must keep replay callbacks disabled.
					app.getEventDispatcher().disableListeners();
					throw failure;
				}
			});
		} catch (RuntimeException | Error failure) {
			try {
				if (undoInfoList.equals(originalCommands)) { restoreUndoCursor(originalCursor); }
				else {
					RuntimeException refresh = attach(originalCommands, originalCursor);
					if (refresh != null) { failure.addSuppressed(refresh); }
				}
			} catch (RuntimeException | Error cursorFailure) { failure.addSuppressed(cursorFailure); }
			throw failure;
		} finally {
			app.getEventDispatcher().enableListeners();
		}
	}

	/** Registration: counted before the inherited manager schedules the store. */
	@Override
	public void storeUndoInfo(StringBuilder currentUndoXML) {
		synchronized (this) {
			historyActivity++;
			pending++;
		}
		try {
			super.storeUndoInfo(currentUndoXML);
		} catch (RuntimeException | Error schedulingFailure) {
			// e.g. thread creation failed: the store may or may not exist
			synchronized (this) {
				degraded = true;
				if (pendingNativeStates != null) { pendingNativeStates.remove(currentUndoXML); }
			}
			throw schedulingFailure;
		}
	}

	@Override
	protected void storeAndNotifyUnsaved(UndoCommand command) {
		synchronized (this) {
			historyActivity++;
		}
		enterSynchronous();
		try {
			super.storeAndNotifyUnsaved(command);
		} finally {
			exitSynchronous();
		}
	}

	/**
	 * A committed baseline advances the inherited history generation: every earlier
	 * store is then discarded by the store body before it can touch the history, so the
	 * accounting is re-established here and nowhere else.
	 */
	@Override
	public synchronized void commitUndoBaseline(PreparedUndoBaseline baseline) {
		enterSynchronous();
		try {
			super.commitUndoBaseline(baseline);
		} finally {
			exitSynchronous();
		}
		pending = 0;
		degraded = false;
		epoch++;
		historyActivity++;
	}

	/**
	 * Only the store body's success path calls this method outside a synchronous
	 * caller; it runs after the command was added. The flag is set after the inherited
	 * method returned, so a failure inside it never counts as success.
	 */
	@Override
	public void pruneStateList() {
		super.pruneStateList();
		if (!isSynchronousCaller()) {
			STORE_SUCCEEDED.set(Boolean.TRUE);
		}
	}

	@Override
	protected void onStoreUndo() {
		synchronized (this) {
			if (!isSynchronousCaller()) {
				if (Boolean.TRUE.equals(STORE_SUCCEEDED.get())) {
					if (pending == 0) {
						degraded = true;
					} else {
						pending--;
					}
				} else {
					// an exception exit of a current or stale store: never a decrement
					degraded = true;
				}
			}
			STORE_SUCCEEDED.remove();
		}
		super.onStoreUndo();
	}

	private static void enterSynchronous() {
		SYNCHRONOUS.get()[0]++;
		STORE_SUCCEEDED.remove();
	}

	private static void exitSynchronous() {
		SYNCHRONOUS.get()[0]--;
	}

	private static boolean isSynchronousCaller() {
		return SYNCHRONOUS.get()[0] > 0;
	}

	/** @return whether no relevant asynchronous store can still change the history */
	synchronized boolean isQuiescent() {
		return pending == 0 && !degraded;
	}

	/** @return stores registered and not yet observed on their success path */
	synchronized int getPendingStores() {
		return pending;
	}

	/** @return whether the accounting became uncertain since the last baseline */
	synchronized boolean isDegraded() {
		return degraded;
	}

	/** @return number of committed baselines (accounting re-establishments) */
	synchronized long getEpoch() {
		return epoch;
	}

	/** @return monotonic history activity, including discarded stores and reverts */
	synchronized long getHistoryActivity() {
		return historyActivity;
	}

	/** @return the live command objects in history order (a new list) */
	synchronized List<UndoCommand> getCommands() {
		return new ArrayList<>(undoInfoList);
	}

	/** @return the iterator position: the number of states up to the current one */
	synchronized int getCursor() {
		return getHistorySize() + 1;
	}

	/**
	 * Takes the whole history aside: the original command objects and the cursor. The
	 * live list becomes empty; nothing is deleted.
	 *
	 * @return the held history
	 */
	synchronized HeldHistory detach() {
		HeldHistory held = new HeldHistory(new ArrayList<>(undoInfoList), getCursor());
		clearUndoInfo();
		return held;
	}

	/**
	 * Installs the given original commands and cursor in place of the live list. The
	 * caller owns (and deletes) whatever the live list held. The history is installed
	 * first; the inherited refresh of the Undo and Redo actions that follows is best
	 * effort.
	 *
	 * @param commands original command objects in history order
	 * @param cursor iterator position
	 * @return the refresh failure, or null
	 */
	synchronized RuntimeException attach(List<UndoCommand> commands, int cursor) {
		if (cursor < 0 || cursor > commands.size()) {
			throw new IllegalArgumentException("cursor " + cursor + " outside 0.."
					+ commands.size());
		}
		clearUndoInfo();
		Map<String, UndoHistory> installed = new HashMap<>();
		installed.put(app.getConfig().getSubAppCode(), new UndoHistory(
				new ArrayList<>(commands), cursor));
		try {
			undoHistoryFrom(installed);
			return null;
		} catch (RuntimeException refreshFailure) {
			if (!undoInfoList.equals(commands) || getCursor() != cursor) {
				throw refreshFailure;
			}
			return refreshFailure;
		}
	}

	/** A detached history: the original commands, never copies, and the cursor. */
	static final class HeldHistory {
		private final List<UndoCommand> commands;
		private final int cursor;

		HeldHistory(List<UndoCommand> commands, int cursor) {
			this.commands = Collections.unmodifiableList(commands);
			this.cursor = cursor;
		}

		/** @return original command objects in history order */
		List<UndoCommand> getCommands() {
			return commands;
		}

		/** @return iterator position */
		int getCursor() {
			return cursor;
		}

		/** @return commands up to the cursor (Undo states) */
		List<UndoCommand> getPrefix() {
			return commands.subList(0, cursor);
		}

		/** @return commands after the cursor (Redo states) */
		List<UndoCommand> getRedo() {
			return commands.subList(cursor, commands.size());
		}
	}
}

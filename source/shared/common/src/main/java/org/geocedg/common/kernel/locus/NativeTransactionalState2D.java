/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.spatial.identity.DefaultSpatialTokenSource;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPoint.NativeRollbackPointState;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Focal trusted-memory complement to the existing Construction XML rollback.
 * Private keys name only one snapshot's reconstruction correspondence. They are
 * never geo IDs, registry records, labels, indices, or geometric identities.
 */
public final class NativeTransactionalState2D {

	/** Missing concrete restoration capability; separate from geometric admission. */
	public enum UnsupportedReason {
		UNSUPPORTED_NATIVE_PROFILE, LOSSY_NATIVE_LITERAL, UNDEFINED_NATIVE_CONIC,
		EDITABLE_MACRO_PLACE, UNSUPPORTED_NATIVE_PLACE, UNSUPPORTED_SOURCE_CLOSURE,
		NO_PRIVATE_SERIALIZATION_SLOT
	}

	public static final String BINDING_TAG = "nativeRollbackBinding";
	private static final Pattern BINDING = Pattern.compile("\\t<" + BINDING_TAG
			+ " version=\"1\" key=\"([0-9a-f]{32})\"/>\\n");
	private final Construction owner;
	private final Map<GeoPoint, Entry> serializationPoints = new IdentityHashMap<>();
	private final Map<String, Entry> durable = new LinkedHashMap<>();
	private final Map<String, Entry> privateKeys = new LinkedHashMap<>();
	private final Map<GeoPoint, Entry> reconstructed = new IdentityHashMap<>();
	private final List<Entry> entries = new ArrayList<>();
	private final Set<SpatialIdentityId> registryIds;
	private final int reservations;
	private final UnsupportedReason unsupported;
	private String ordinaryXml;
	private boolean restoring;
	private boolean finished;
	private long nativeCaptureNanos;
	private long xmlCaptureNanos;
	private long xmlRestoreNanos;
	private long dagRestoreNanos;
	private long verifyNanos;
	private int dagCalls;

	private NativeTransactionalState2D(Construction construction) {
		long started = System.nanoTime();
		owner = construction;
		unsupported = unsupportedReason(construction);
		registryIds = ids(construction);
		reservations = construction.getSpatialIdentityRegistry().getReservedIdentityCount();
		if (unsupported == null) {
			DefaultSpatialTokenSource tokens = new DefaultSpatialTokenSource();
			for (GeoElement geo : closure(construction)) {
				if (!(geo instanceof GeoPoint)) {
					continue;
				}
				GeoPoint point = (GeoPoint) geo;
				NativeRollbackPointState state = point.captureNativeRollbackState();
				if (state == null) {
					continue;
				}
				PersistentGeoId id = construction.getSpatialIdentityRegistry()
						.getPersistentGeoId(point);
				String key = null;
				if (id == null) {
					do {
						key = tokens.nextToken();
					} while (privateKeys.containsKey(key));
				}
				Entry entry = new Entry(id, key, state);
				entries.add(entry);
				serializationPoints.put(point, entry);
				if (id == null) {
					privateKeys.put(key, entry);
				} else {
					durable.put(id.toExternalForm(), entry);
				}
			}
		}
		nativeCaptureNanos = System.nanoTime() - started;
	}

	/** Captures without assigning IDs or changing the construction or random state. */
	public static NativeTransactionalState2D capture(Construction construction) {
		return new NativeTransactionalState2D(construction);
	}

	/** @return an actual missing restorer, or null for the positive native table */
	public static UnsupportedReason unsupportedReason(Construction construction) {
		if (!(construction.getApplication().getConfig() instanceof AppConfigGeoCeDG)
				|| construction.getKernel().isMacroKernel()) {
			return UnsupportedReason.UNSUPPORTED_NATIVE_PROFILE;
		}
		NativeSourceFidelity2D sourcePass = NativeSourceFidelity2D.restorationPass();
		for (GeoElement geo : closure(construction)) {
			if (geo.isIndependent() && (geo instanceof GeoPoint
					&& (negativeZero(((GeoPoint) geo).x) || negativeZero(((GeoPoint) geo).y)
							|| negativeZero(((GeoPoint) geo).z))
					|| geo instanceof GeoNumeric && negativeZero(((GeoNumeric) geo).getDouble()))) {
				return UnsupportedReason.LOSSY_NATIVE_LITERAL;
			}
			for (MyDouble literal : ReconstructionExactState2D.literals(geo.getDefinition())) {
				if (literal.getClass() == MyDouble.class && literal.isAngle()) {
					return UnsupportedReason.LOSSY_NATIVE_LITERAL;
				}
			}
			if (geo.getClass() == GeoConic.class) {
				GeoConic conic = (GeoConic) geo;
				if (!conic.isDefined()) {
					return UnsupportedReason.UNDEFINED_NATIVE_CONIC;
				}
				for (double coefficient : conic.getMatrix()) {
					if (!Double.isFinite(coefficient)) {
						return UnsupportedReason.UNDEFINED_NATIVE_CONIC;
					}
				}
			}
			if (!(geo instanceof GeoPointND)) {
				continue;
			}
			GeoPointND point = (GeoPointND) geo;
			var parent = geo.getParentAlgorithm();
			if (parent instanceof AlgoMacro && ((AlgoMacro) parent).isChangeable(point)) {
				return UnsupportedReason.EDITABLE_MACRO_PLACE;
			}
			if (parent instanceof AlgoPointOnPath && parent.getInputLength() == 1
					|| point.hasRegion()) {
				if (!(geo instanceof GeoPoint) || !((GeoPoint) geo).canCaptureNativeRollbackState()) {
					return UnsupportedReason.UNSUPPORTED_NATIVE_PLACE;
				}
				if (!sourcePass.isFaithful(point.getPath().toGeoElement())) {
					return UnsupportedReason.UNSUPPORTED_SOURCE_CLOSURE;
				}
				if (!geo.isLabelSet() || !construction.isInConstructionList(parent)) {
					return UnsupportedReason.NO_PRIVATE_SERIALIZATION_SLOT;
				}
			}
		}
		return null;
	}

	private static boolean negativeZero(double value) {
		return Double.doubleToLongBits(value) == Double.doubleToLongBits(-0.0);
	}

	private static List<GeoElement> closure(Construction construction) {
		Set<GeoElement> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		ArrayDeque<GeoElement> pending = new ArrayDeque<>(construction.getGeoSetConstructionOrder());
		List<GeoElement> result = new ArrayList<>();
		while (!pending.isEmpty()) {
			GeoElement geo = pending.removeFirst();
			if (!visited.add(geo)) {
				continue;
			}
			result.add(geo);
			if (geo.getParentAlgorithm() != null) {
				Collections.addAll(pending, geo.getParentAlgorithm().getInput());
			}
			if (geo instanceof GeoList) {
				GeoList list = (GeoList) geo;
				for (int index = 0; index < list.size(); index++) {
					pending.addLast(list.get(index));
				}
			}
		}
		return result;
	}

	private static Set<SpatialIdentityId> ids(Construction construction) {
		Set<SpatialIdentityId> ids = new LinkedHashSet<>();
		construction.getSpatialIdentityRegistry().getRecords().forEach(record -> ids.add(record.getId()));
		return ids;
	}

	/** Called only while Construction owns this snapshot's private XML pass. */
	public String serializationKey(GeoPoint point) {
		Entry entry = serializationPoints.get(point);
		if (entry != null) {
			entry.serializationVisits++;
		}
		return entry == null ? null : entry.key;
	}

	/** @return whether this snapshot owns the positive typed native restoration contract */
	public boolean isSupported() {
		return unsupported == null;
	}

	/** Removes only freshly generated private binding lines, never normal payload. */
	public void completeCapture(String privateXml, long elapsedNanos) {
		for (Entry entry : entries) {
			if (entry.serializationVisits != 1) {
				throw new IllegalStateException("Native rollback requires one early element serialization slot");
			}
		}
		Set<String> seen = new LinkedHashSet<>();
		Matcher matcher = BINDING.matcher(privateXml);
		StringBuilder ordinary = new StringBuilder(privateXml.length());
		int previous = 0;
		while (matcher.find()) {
			String key = matcher.group(1);
			if (!privateKeys.containsKey(key) || !seen.add(key)) {
				throw new IllegalStateException("Foreign or duplicate private native snapshot binding");
			}
			ordinary.append(privateXml, previous, matcher.start());
			previous = matcher.end();
		}
		ordinary.append(privateXml, previous, privateXml.length());
		if (!seen.equals(privateKeys.keySet())) {
			throw new IllegalStateException("A native snapshot has no private serialization slot");
		}
		ordinaryXml = ordinary.toString();
		xmlCaptureNanos = elapsedNanos;
		serializationPoints.clear(); // No old Java object is used to resolve restoration.
	}

	/** Opens a single restoration owned by Construction's ROLLBACK_RESTORE boundary. */
	public void beginRestore() {
		if (restoring || finished || ordinaryXml == null) {
			throw new IllegalStateException("Native rollback snapshot is inactive or already used");
		}
		restoring = true;
	}

	/** @return whether an existing geocedgId bound a captured native point */
	public boolean bindDurable(GeoPoint point, String id) {
		Entry entry = id == null ? null : durable.get(id);
		if (entry == null) {
			return false;
		}
		bind(entry, point);
		return true;
	}

	/** Consumes one opaque binding from this private snapshot, never an ordinary file. */
	public void bindPrivate(GeoPoint point, String key) {
		Entry entry = privateKeys.get(key);
		if (entry == null) {
			throw new IllegalStateException("Foreign private native rollback binding");
		}
		bind(entry, point);
	}

	private void bind(Entry entry, GeoPoint point) {
		if (!restoring || finished || entry.point != null || point.getConstruction() != owner
				|| reconstructed.containsKey(point)) {
			throw new IllegalStateException("Inactive, duplicate or replayed native rollback binding");
		}
		entry.point = point;
		reconstructed.put(point, entry);
		point.installNativeRollbackState(entry.state);
	}

	/** @return whether the live rollback session explicitly owns this payload/output pair */
	public boolean authorizes(GeoPoint point, NativeRollbackPointState state) {
		Entry entry = reconstructed.get(point);
		return restoring && !finished && entry != null && entry.state == state;
	}

	/** Records the existing normal DAG's first post-load consumption. */
	public void consumed(GeoPoint point, NativeRollbackPointState state) {
		Entry entry = reconstructed.get(point);
		if (!authorizes(point, state) || entry.consumed) {
			throw new IllegalStateException("Native rollback input was not consumed exactly once");
		}
		entry.consumed = true;
	}

	/** Verifies after the normal XML/update lifecycle; does not run a second DAG pass. */
	public void finishRestore() {
		long started = System.nanoTime();
		for (Entry entry : entries) {
			if (entry.point == null || !entry.consumed || !entry.state.matches(entry.point)
					|| entry.id != null && owner.getSpatialIdentityRegistry().getGeo(entry.id) != entry.point) {
				throw new IllegalStateException("Native rollback input, correspondence or DAG restoration failed");
			}
		}
		if (!registryIds.equals(ids(owner)) || reservations
				!= owner.getSpatialIdentityRegistry().getReservedIdentityCount()
				|| !ordinaryXml.equals(owner.getCurrentUndoXML(false).toString())) {
			throw new IllegalStateException("Native rollback did not preserve registry and ordinary XML exactly");
		}
		verifyNanos = System.nanoTime() - started;
		finished = true;
	}

	public void recordXmlRestore(long nanos) {
		xmlRestoreNanos = nanos;
	}

	public void recordDagRestore(long nanos) {
		dagRestoreNanos += nanos;
		dagCalls++;
	}

	/** @return immutable telemetry; DAG/verify are subsets of inclusive XML restoration */
	public Metrics metrics() {
		return new Metrics(entries.size(), privateKeys.size(), nativeCaptureNanos, xmlCaptureNanos,
				xmlRestoreNanos, dagRestoreNanos, verifyNanos, dagCalls, unsupported);
	}

	private static final class Entry {
		private final PersistentGeoId id;
		private final String key;
		private final NativeRollbackPointState state;
		private GeoPoint point;
		private boolean consumed;
		private int serializationVisits;

		private Entry(PersistentGeoId id, String key, NativeRollbackPointState state) {
			this.id = id; this.key = key; this.state = state;
		}
	}

	/** Local measurements only, with deterministic point/binding/DAG-call counts. */
	public static final class Metrics {
		public final int capturedPoints;
		public final int privateBindings;
		public final long nativeCaptureNanos;
		public final long xmlCaptureNanos;
		public final long xmlRestoreInclusiveNanos;
		public final long dagRestoreSubsetNanos;
		public final long verifySubsetNanos;
		public final int dagCalls;
		public final UnsupportedReason unsupported;

		private Metrics(int points, int keys, long capture, long xml, long restore, long dag,
				long verify, int calls, UnsupportedReason reason) {
			capturedPoints = points; privateBindings = keys; nativeCaptureNanos = capture;
			xmlCaptureNanos = xml; xmlRestoreInclusiveNanos = restore;
			dagRestoreSubsetNanos = dag; verifySubsetNanos = verify; dagCalls = calls;
			unsupported = reason;
		}
	}
}

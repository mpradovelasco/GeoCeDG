/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic.NativeRollbackSelectionState;
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

	/** Intrinsic restoration failure or exhausted work; separate from geometric admission. */
	public enum UnsupportedReason {
		UNSUPPORTED_NATIVE_PROFILE, LOSSY_NATIVE_LITERAL, UNDEFINED_NATIVE_CONIC,
		EDITABLE_MACRO_PLACE, UNSUPPORTED_NATIVE_PLACE, UNSUPPORTED_SOURCE_CLOSURE,
		NO_PRIVATE_SERIALIZATION_SLOT, SOURCE_FIDELITY_RESOURCE_EXHAUSTED
	}

	/** Host-node count is the collected closure size, separate from source exploration. */
	public record HostAssessment(UnsupportedReason reason, int hostNodes,
			NativeSourceFidelity2D.Metrics sourceMetrics) { }
	private record Inspection(HostAssessment assessment, List<GeoElement> closure,
			Map<AlgoIntersectLineConic, NativeRollbackSelectionState> selections) { }

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
	private final int hostNodes;
	private final NativeSourceFidelity2D.Metrics sourceMetrics;
	private final List<AxisVisibility> axisVisibility;
	private final List<NativeViewState> viewStates;
	private NativeViewCache applyingViewCache;
	private boolean viewsRestored;
	private boolean restoringViews;
	private String ordinaryXml;
	private String capturedPrivateXml;
	private byte[] ordinaryDigest;
	private boolean listenersToo;
	private boolean restoring;
	private boolean finished;
	private long nativeCaptureNanos;
	private long xmlCaptureNanos;
	private long xmlRestoreNanos;
	private long dagRestoreNanos;
	private long verifyNanos;
	private int dagCalls;

	private NativeTransactionalState2D(Construction construction, NativeSourceFidelity2D.Limits limits) {
		long started = System.nanoTime();
		owner = construction;
		Inspection inspection = inspect(construction, limits);
		unsupported = inspection.assessment.reason();
		hostNodes = inspection.assessment.hostNodes();
		sourceMetrics = inspection.assessment.sourceMetrics();
		axisVisibility = unsupported == null ? captureAxes(construction) : List.of();
		viewStates = unsupported == null ? captureViews(construction) : List.of();
		registryIds = ids(construction);
		reservations = construction.getSpatialIdentityRegistry().getReservedIdentityCount();
		if (unsupported == null) {
			DefaultSpatialTokenSource tokens = new DefaultSpatialTokenSource();
			for (GeoElement geo : inspection.closure) {
				if (!(geo instanceof GeoPoint)) {
					continue;
				}
				GeoPoint point = (GeoPoint) geo;
				NativeRollbackPointState state = point.captureNativeRollbackState();
				if (state == null) {
					continue;
				}
				captureEntry(point, state, null, tokens);
			}
			inspection.selections.forEach((algorithm, state) ->
					captureEntry(algorithm.getIntersectionPoints()[0], null, state, tokens));
		}
		nativeCaptureNanos = System.nanoTime() - started;
	}

	private void captureEntry(GeoPoint point, NativeRollbackPointState state,
			NativeRollbackSelectionState selection, DefaultSpatialTokenSource tokens) {
		PersistentGeoId id = owner.getSpatialIdentityRegistry().getPersistentGeoId(point);
		String key = null;
		if (id == null) {
			do { key = tokens.nextToken(); } while (privateKeys.containsKey(key));
		}
		Entry entry = new Entry(id, key, state, selection);
		entries.add(entry); serializationPoints.put(point, entry);
		if (id == null) { privateKeys.put(key, entry); }
		else { durable.put(id.toExternalForm(), entry); }
	}

	/** Captures without assigning IDs or changing the construction or random state. */
	public static NativeTransactionalState2D capture(Construction construction) {
		return capture(construction, NativeSourceFidelity2D.DEFAULT_LIMITS);
	}

	/** Explicit technical limits; no geometric permission is changed by this overload. */
	public static NativeTransactionalState2D capture(Construction construction,
			NativeSourceFidelity2D.Limits limits) {
		return new NativeTransactionalState2D(construction, limits);
	}

	/** @return the intrinsic/resource failure, or null for the positive native table */
	public static UnsupportedReason unsupportedReason(Construction construction) {
		return assessHost(construction).reason();
	}

	public static HostAssessment assessHost(Construction construction) {
		return assessHost(construction, NativeSourceFidelity2D.DEFAULT_LIMITS);
	}

	public static HostAssessment assessHost(Construction construction,
			NativeSourceFidelity2D.Limits limits) {
		return inspect(construction, limits).assessment;
	}

	/** Never downgrade exhausted exact-state work to the unsupported historical route. */
	public static void requireAvailable(UnsupportedReason reason) {
		if (reason == UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED) {
			throw new IllegalStateException(reason.name());
		}
	}

	private static Inspection inspect(Construction construction, NativeSourceFidelity2D.Limits limits) {
		NativeSourceFidelity2D sourcePass = NativeSourceFidelity2D.restorationPass(limits);
		if (!(construction.getApplication().getConfig() instanceof AppConfigGeoCeDG)
				|| construction.getKernel().isMacroKernel()) {
			return new Inspection(new HostAssessment(UnsupportedReason.UNSUPPORTED_NATIVE_PROFILE,
					0, sourcePass.metrics()), List.of(), Map.of());
		}
		List<GeoElement> closure = closure(construction);
		UnsupportedReason reason = inspectClosure(closure, sourcePass);
		return new Inspection(new HostAssessment(reason, closure.size(), sourcePass.metrics()), closure,
				sourcePass.memorySelections());
	}

	private static UnsupportedReason inspectClosure(List<GeoElement> closure,
			NativeSourceFidelity2D sourcePass) {
		for (GeoElement geo : closure) {
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
				NativeSourceFidelity2D.Outcome outcome = sourcePass.assess(
						point.getPath().toGeoElement()).outcome();
				if (outcome == NativeSourceFidelity2D.Outcome.RESOURCE_EXHAUSTED) {
					return UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED;
				}
				if (outcome == NativeSourceFidelity2D.Outcome.UNSUPPORTED) {
					return UnsupportedReason.UNSUPPORTED_SOURCE_CLOSURE;
				}
				if (!geo.isLabelSet() || !parent.isInConstructionList()) {
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

	private record AxisVisibility(int view, boolean liveX, boolean liveY,
			boolean settingsX, boolean settingsY) {
		static AxisVisibility capture(int number, EuclidianView view) {
			return new AxisVisibility(number, view.getShowAxis(0), view.getShowAxis(1),
					view.getSettings().getShowAxis(0), view.getSettings().getShowAxis(1));
		}
	}

	/** Private MEMORY failure; never a new unsupported-history permission. */
	public enum NativeViewFailure {
		INACTIVE_SESSION, VIEW_ABSENT, VIEW_DIMENSIONS_CHANGED, UNSUPPORTED_VIEW_CLASS,
		VIEW_STATE_CHANGED, VIEW_CACHE_ABSENT
	}

	public static final class NativeViewRestoreException extends IllegalStateException {
		private final NativeViewFailure reason;
		private NativeViewRestoreException(NativeViewFailure reason, int view) {
			this(reason, view, "");
		}
		private NativeViewRestoreException(NativeViewFailure reason, int view, String detail) {
			super("Native view MEMORY restore: " + reason + "; view=" + view + detail);
			this.reason = reason;
		}
		public NativeViewFailure reason() { return reason; }
	}

	private record ViewCoordinates(double x, double y, double xs, double ys) {
		static ViewCoordinates live(EuclidianView view) {
			return new ViewCoordinates(view.getXZero(), view.getYZero(),
					view.getXscale(), view.getYscale());
		}
		static ViewCoordinates settings(EuclidianView view) {
			var settings = view.getSettings();
			return new ViewCoordinates(settings.getXZero(), settings.getYZero(),
					settings.getXscale(), settings.getYscale());
		}
		boolean admissible() {
			return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(xs)
					&& Double.isFinite(ys) && xs >= Kernel.MAX_DOUBLE_PRECISION
					&& ys >= Kernel.MAX_DOUBLE_PRECISION && xs <= Kernel.INV_MAX_DOUBLE_PRECISION
					&& ys <= Kernel.INV_MAX_DOUBLE_PRECISION;
		}
	}

	private record ViewBounds(double xmin, double xmax, double ymin, double ymax) {
		static ViewBounds live(EuclidianView view) {
			return new ViewBounds(view.getXmin(), view.getXmax(), view.getYmin(), view.getYmax());
		}
		static ViewBounds objects(EuclidianView view) {
			return new ViewBounds(view.getXminObject().getDouble(), view.getXmaxObject().getDouble(),
					view.getYminObject().getDouble(), view.getYmaxObject().getDouble());
		}
		boolean admissible() {
			return Double.isFinite(xmin) && Double.isFinite(xmax) && Double.isFinite(ymin)
					&& Double.isFinite(ymax) && xmin < xmax && ymin < ymax;
		}
	}

	/** Immutable primitives only; constructor is private and session identity authenticates use. */
	public static final class NativeViewCache {
		private final int view;
		private final double xmin, xmax, ymin, ymax, xs, ys;
		private NativeViewCache(int number, Kernel kernel) {
			view = number;
			int index = number - 1;
			xmin = kernel.getXmin(index); xmax = kernel.getXmax(index);
			ymin = kernel.getYmin(index); ymax = kernel.getYmax(index);
			xs = kernel.getXscale(index); ys = kernel.getYscale(index);
		}
		public int view() { return view; }
		public double xmin() { return xmin; }
		public double xmax() { return xmax; }
		public double ymin() { return ymin; }
		public double ymax() { return ymax; }
		public double xs() { return xs; }
		public double ys() { return ys; }
		private boolean admissible() {
			return new ViewBounds(xmin, xmax, ymin, ymax).admissible()
					&& Double.isFinite(xs) && xs > 0 && Double.isFinite(ys) && ys > 0;
		}
		private boolean matches(Kernel kernel) {
			int index = view - 1;
			return same(xmin, kernel.getXmin(index)) && same(xmax, kernel.getXmax(index))
					&& same(ymin, kernel.getYmin(index)) && same(ymax, kernel.getYmax(index))
					&& same(xs, kernel.getXscale(index)) && same(ys, kernel.getYscale(index));
		}
		private boolean present(Kernel kernel) {
			int index = view - 1;
			try {
				kernel.getXmin(index); kernel.getXmax(index);
				kernel.getYmin(index); kernel.getYmax(index);
				kernel.getXscale(index); kernel.getYscale(index);
				return true;
			} catch (IndexOutOfBoundsException absent) {
				return false;
			}
		}
	}

	private static boolean same(double expected, double actual) {
		return Double.doubleToLongBits(expected) == Double.doubleToLongBits(actual);
	}

	private record NativeViewState(int version, int view, int width, int height,
			int settingsWidth, int settingsHeight, int visibleFromX, int visibleUntilY,
			ViewCoordinates live, ViewCoordinates settings, ViewBounds bounds,
			ViewBounds objects, NativeViewCache cache, boolean admitted) {
		static NativeViewState capture(int number, EuclidianView view, Construction owner) {
			boolean free = freeLinearView(view, owner);
			var live = ViewCoordinates.live(view);
			var settings = ViewCoordinates.settings(view);
			var bounds = ViewBounds.live(view);
			var objects = free ? ViewBounds.objects(view) : null;
			var cache = new NativeViewCache(number, owner.getKernel());
			boolean admitted = free && !view.isUpdatingBounds() && view.getWidth() > 0
					&& view.getHeight() > 0 && live.admissible() && settings.admissible()
					&& bounds.admissible() && objects.admissible() && cache.admissible();
			var options = view.getSettings();
			return new NativeViewState(1, number, view.getWidth(), view.getHeight(),
					options.getWidth(), options.getHeight(), options.getVisibleFromX(),
					options.getVisibleUntilY(), live, settings, bounds, objects, cache, admitted);
		}
		private void validate(EuclidianView target, Construction owner) {
			if (target == null || target.getSettings() == null) {
				throw new NativeViewRestoreException(NativeViewFailure.VIEW_ABSENT, view);
			}
			var options = target.getSettings();
			if (version != 1 || width != target.getWidth() || height != target.getHeight()
					|| visibleFromX != options.getVisibleFromX()
					|| (long) settingsHeight - visibleUntilY
							!= (long) options.getHeight() - options.getVisibleUntilY()) {
				// XML writes live size as a file hint. The sentinel-backed visible
				// Y getter follows that hint; preserve the actual writer inset instead.
				throw new NativeViewRestoreException(NativeViewFailure.VIEW_DIMENSIONS_CHANGED, view,
						"; capturedVersion=" + version + " supportedVersion=1"
						+ "; expectedLive=" + width + "x" + height
						+ " actualLive=" + target.getWidth() + "x" + target.getHeight()
						+ "; expectedSettings=" + settingsWidth + "x" + settingsHeight
						+ " actualSettings=" + options.getWidth() + "x" + options.getHeight()
						+ "; expectedVisible=" + visibleFromX + "," + visibleUntilY
						+ " actualVisible=" + options.getVisibleFromX() + "," + options.getVisibleUntilY()
						+ "; expectedInsetY=" + ((long) settingsHeight - visibleUntilY)
						+ " actualInsetY=" + ((long) options.getHeight() - options.getVisibleUntilY()));
			}
			if (!cache.present(owner.getKernel())) {
				throw new NativeViewRestoreException(NativeViewFailure.VIEW_CACHE_ABSENT, view);
			}
			if (admitted ? !freeLinearView(target, owner) || target.isUpdatingBounds()
					: !live.equals(ViewCoordinates.live(target))
							|| !settings.equals(ViewCoordinates.settings(target))
							|| !bounds.equals(ViewBounds.live(target)) || !cache.matches(owner.getKernel())) {
				throw new NativeViewRestoreException(NativeViewFailure.UNSUPPORTED_VIEW_CLASS, view);
			}
		}
		private void apply(EuclidianView target, NativeTransactionalState2D session) {
			if (!admitted) { return; }
			boolean previous = target.isUpdatingBounds();
			boolean previousLoading = session.owner.getKernel().getLoadingMode();
			try {
				target.setUpdatingBounds(true);
				// This is private restoration, never a user coordinate-system edit.
				session.owner.getKernel().setLoadingMode(true);
				target.getSettings().setCoordSystem(settings.x, settings.y, settings.xs, settings.ys, false);
				target.setCoordSystem(live.x, live.y, live.xs, live.ys, false);
				// Native zoom may intentionally retain cached extents separately from
				// the coordinate tuple. Restore captured primitives, never derive them.
				target.setXmin(bounds.xmin); target.setXmax(bounds.xmax);
				target.setYmin(bounds.ymin); target.setYmax(bounds.ymax);
				target.getXminObject().setValue(objects.xmin); target.getXmaxObject().setValue(objects.xmax);
				target.getYminObject().setValue(objects.ymin); target.getYmaxObject().setValue(objects.ymax);
				session.applyingViewCache = cache;
				try { session.owner.getKernel().restoreNativeViewCache(cache); }
				finally { session.applyingViewCache = null; }
			} finally {
				session.owner.getKernel().setLoadingMode(previousLoading);
				target.setUpdatingBounds(previous);
			}
		}
		private void verify(EuclidianView target, Construction owner) {
			validate(target, owner);
			if (!live.equals(ViewCoordinates.live(target))
					|| !settings.equals(ViewCoordinates.settings(target))
					|| !bounds.equals(ViewBounds.live(target)) || !cache.matches(owner.getKernel())
					|| admitted && !objects.equals(ViewBounds.objects(target))) {
				throw new NativeViewRestoreException(NativeViewFailure.VIEW_STATE_CHANGED, view);
			}
		}
	}

	private static boolean freeBound(GeoNumeric numeric, Construction owner) {
		return numeric != null && numeric.getClass() == GeoNumeric.class
				&& numeric.getConstruction() == owner && numeric.isIndependent()
				&& !numeric.isLabelSet() && numeric.isDefined();
	}

	private static boolean freeLinearView(EuclidianView view, Construction owner) {
		return !view.getLogAxis(0) && !view.getLogAxis(1) && !view.isLockedAxesRatio()
				&& freeBound(view.getXminObject(), owner) && freeBound(view.getXmaxObject(), owner)
				&& freeBound(view.getYminObject(), owner) && freeBound(view.getYmaxObject(), owner);
	}

	private static EuclidianView existingView(Construction owner, int number) {
		var app = owner.getApplication();
		return number == 1 ? app.getEuclidianView1()
				: number == 2 && app.hasEuclidianView2EitherShowingOrNot(1)
						? app.getEuclidianView2(1) : null;
	}

	private static List<NativeViewState> captureViews(Construction construction) {
		List<NativeViewState> result = new ArrayList<>();
		for (int number = 1; number <= 2; number++) {
			EuclidianView view = existingView(construction, number);
			if (view != null && view.getSettings() != null) {
				result.add(NativeViewState.capture(number, view, construction));
			}
		}
		return List.copyOf(result);
	}

	/** Restores only authenticated presentation primitives before the one normal DAG. */
	public void restoreViewsBeforeDag() {
		if (!restoring || finished || viewsRestored || restoringViews || dagCalls != 0
				|| !owner.isNativeViewSessionAuthorized(this)) {
			throw new NativeViewRestoreException(NativeViewFailure.INACTIVE_SESSION, 0);
		}
		restoringViews = true;
		try {
			for (NativeViewState state : viewStates) {
				state.validate(existingView(owner, state.view), owner);
			}
			for (NativeViewState state : viewStates) {
				state.apply(existingView(owner, state.view), this);
			}
			viewsRestored = true;
		} finally {
			restoringViews = false;
		}
	}

	/** Only the current primitive payload during its one guarded application may store caches. */
	public boolean authorizesViewCache(NativeViewCache cache) {
		return restoring && !finished && restoringViews && !viewsRestored
				&& cache != null && applyingViewCache == cache;
	}

	private static List<AxisVisibility> captureAxes(Construction construction) {
		var app = construction.getApplication();
		List<AxisVisibility> axes = new ArrayList<>();
		EuclidianView first = app.getEuclidianView1(); // Existing view getter; never creates a view.
		if (first != null && first.getSettings() != null) {
			axes.add(AxisVisibility.capture(1, first));
		}
		if (app.hasEuclidianView2EitherShowingOrNot(1)) {
			EuclidianView second = app.getEuclidianView2(1);
			if (second != null && second.getSettings() != null) {
				axes.add(AxisVisibility.capture(2, second));
			}
		}
		return List.copyOf(axes);
	}

	private void restoreAxes() {
		var app = owner.getApplication();
		for (AxisVisibility saved : axisVisibility) {
			EuclidianView view = saved.view == 1 ? app.getEuclidianView1()
					: app.hasEuclidianView2EitherShowingOrNot(1) ? app.getEuclidianView2(1) : null;
			if (view == null || view.getSettings() == null) {
				throw new IllegalStateException("Captured native checkpoint view is no longer present");
			}
			// The public writer records live evSettings.axes and separate settings
			// axis.show values. Preserve both captured values without normalizing
			// either one, firing settings listeners, or recomputing geometry.
			view.getSettings().setShowAxisNoFireSettingChanged(0, saved.settingsX);
			view.getSettings().setShowAxisNoFireSettingChanged(1, saved.settingsY);
			boolean changed = view.setShowAxis(0, saved.liveX, false);
			changed |= view.setShowAxis(1, saved.liveY, false);
			if (changed) { view.updateBackgroundImage(); }
		}
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

	/** @return whether there are actual native inputs to accompany a history state */
	public boolean hasNativeInputs() {
		return !entries.isEmpty();
	}

	/** Removes only freshly generated private binding lines, never normal payload. */
	public void completeCapture(String privateXml, long elapsedNanos) {
		completeCapture(privateXml, elapsedNanos, false);
	}

	/** Same single capture, retaining the serializer mode used by normal Undo. */
	public void completeCapture(String privateXml, long elapsedNanos, boolean includeListeners) {
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
		capturedPrivateXml = privateXml;
		listenersToo = includeListeners;
		xmlCaptureNanos = elapsedNanos;
		serializationPoints.clear(); // No old Java object is used to resolve restoration.
	}

	/** Opens a single restoration owned by Construction's ROLLBACK_RESTORE boundary. */
	public void beginRestore() {
		if (restoring || finished || ordinaryXml == null && ordinaryDigest == null) {
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
		if (entry.state != null) { point.installNativeRollbackState(entry.state); }
		else {
			if (point.getParentAlgorithm() == null
					|| point.getParentAlgorithm().getClass() != AlgoIntersectLineConic.class
					|| point.getParentAlgorithm().getOutput(0) != point) {
				throw new IllegalStateException("Native selector binding has an incompatible producer/role");
			}
			((AlgoIntersectLineConic) point.getParentAlgorithm())
					.installNativeRollbackSelectionState(entry.selection);
		}
	}

	/** @return whether the live rollback session explicitly owns this payload/output pair */
	public boolean authorizes(GeoPoint point, NativeRollbackPointState state) {
		Entry entry = reconstructed.get(point);
		return restoring && !finished && state != null && entry != null && entry.state == state;
	}

	/** An authenticated typed output role binds the selector, never labels or old objects. */
	public boolean authorizesSelection(AlgoIntersectLineConic algorithm, NativeRollbackSelectionState state) {
		Entry entry = reconstructed.get(algorithm.getIntersectionPoints()[0]);
		return restoring && !finished && state != null && entry != null && entry.selection == state;
	}

	public void consumedSelection(AlgoIntersectLineConic algorithm, NativeRollbackSelectionState state) {
		Entry entry = reconstructed.get(algorithm.getIntersectionPoints()[0]);
		if (!authorizesSelection(algorithm, state) || entry.consumed) {
			throw new IllegalStateException("Native selector was not consumed exactly once");
		}
		entry.consumed = true;
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
		if (!viewsRestored) {
			throw new NativeViewRestoreException(NativeViewFailure.INACTIVE_SESSION, 0);
		}
		for (NativeViewState state : viewStates) {
			state.verify(existingView(owner, state.view), owner);
		}
		for (Entry entry : entries) {
			if (entry.point == null || !entry.consumed || !entry.matches()
					|| entry.id != null && owner.getSpatialIdentityRegistry().getGeo(entry.id) != entry.point) {
				throw new IllegalStateException("Native rollback input, correspondence or DAG restoration failed");
			}
		}
		restoreAxes();
		String restoredXml = owner.getCurrentUndoXML(listenersToo).toString();
		boolean sameXml = ordinaryXml != null ? ordinaryXml.equals(restoredXml)
				: Arrays.equals(ordinaryDigest, digest(restoredXml.getBytes(StandardCharsets.UTF_8)));
		Set<SpatialIdentityId> restoredIds = ids(owner);
		int restoredReservations = owner.getSpatialIdentityRegistry().getReservedIdentityCount();
		boolean sameRegistry = registryIds.equals(restoredIds);
		if (!sameRegistry || reservations != restoredReservations || !sameXml) {
			throw new IllegalStateException("Native rollback did not preserve registry and ordinary XML exactly"
					+ "; registry=" + sameRegistry + " expectedIds=" + registryIds.size()
					+ " actualIds=" + restoredIds.size() + " reservations=" + reservations
					+ "/" + restoredReservations + " xml=" + sameXml + " listeners=" + listenersToo
					+ (ordinaryXml == null ? "; expectedXml=digest-only"
							: firstXmlDifference(ordinaryXml, restoredXml)));
		}
		verifyNanos = System.nanoTime() - started;
		finished = true;
	}

	private static String firstXmlDifference(String expected, String actual) {
		int index = 0, common = Math.min(expected.length(), actual.length());
		while (index < common && expected.charAt(index) == actual.charAt(index)) { index++; }
		int start = Math.max(0, index - 80);
		return "; firstXmlDifference=" + index + " lengths=" + expected.length() + "/" + actual.length()
				+ " expected=" + expected.substring(start, Math.min(expected.length(), index + 160))
						.replace("\n", "\\n").replace("\r", "\\r")
				+ " actual=" + actual.substring(start, Math.min(actual.length(), index + 160))
						.replace("\n", "\\n").replace("\r", "\\r");
	}

	/** Freeze only immutable native data and digests, never retaining whole XML in history. */
	public UndoTemplate freezeUndoTemplate(String privateXml) {
		if (ordinaryXml == null || !privateXml.equals(capturedPrivateXml)
				|| restoring || finished || !isSupported() || !hasNativeInputs()) {
			throw new IllegalStateException("Native Undo capture is not complete");
		}
		return new UndoTemplate(this, privateXml);
	}

	private NativeTransactionalState2D(UndoTemplate template) {
		owner = template.owner;
		unsupported = null;
		hostNodes = template.hostNodes;
		sourceMetrics = template.sourceMetrics;
		axisVisibility = template.axisVisibility;
		viewStates = template.viewStates;
		registryIds = template.registryIds;
		reservations = template.reservations;
		ordinaryDigest = template.ordinaryDigest;
		listenersToo = template.listenersToo;
		for (Captured input : template.inputs) {
			Entry entry = new Entry(input.id, input.key, input.state, input.selection);
			entries.add(entry);
			if (entry.id == null) { privateKeys.put(entry.key, entry); }
			else { durable.put(entry.id.toExternalForm(), entry); }
		}
	}

	private static byte[] digest(byte[] bytes) {
		try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
		catch (NoSuchAlgorithmException failure) {
			throw new IllegalStateException("SHA-256 is unavailable", failure);
		}
	}

	private record Captured(PersistentGeoId id, String key, NativeRollbackPointState state,
			NativeRollbackSelectionState selection) { }

	/** Immutable, owner-bound input template. Every legitimate history load gets a fresh session. */
	public static final class UndoTemplate {
		private final Construction owner;
		private final List<Captured> inputs;
		private final Set<SpatialIdentityId> registryIds;
		private final int reservations;
		private final boolean listenersToo;
		private final List<AxisVisibility> axisVisibility;
		private final List<NativeViewState> viewStates;
		private final byte[] ordinaryDigest;
		private final byte[] privateDigest;
		private final int hostNodes;
		private final NativeSourceFidelity2D.Metrics sourceMetrics;

		private UndoTemplate(NativeTransactionalState2D capture, String xml) {
			owner = capture.owner;
			inputs = capture.entries.stream().map(entry -> new Captured(entry.id, entry.key, entry.state, entry.selection)).toList();
			registryIds = Set.copyOf(capture.registryIds);
			reservations = capture.reservations;
			listenersToo = capture.listenersToo;
			axisVisibility = capture.axisVisibility;
			viewStates = capture.viewStates;
			ordinaryDigest = digest(capture.ordinaryXml.getBytes(StandardCharsets.UTF_8));
			privateDigest = digest(xml.getBytes(StandardCharsets.UTF_8));
			hostNodes = capture.hostNodes;
			sourceMetrics = capture.sourceMetrics;
		}

		/** @return whether this template belongs to this exact document construction */
		public boolean belongsTo(Construction construction) { return owner == construction; }

		/** @return whether the frozen private XML bytes match this template */
		public boolean matchesXml(byte[] xml) { return Arrays.equals(privateDigest, digest(xml)); }

		/** Historical public payload only, for an unsupported current history pre-state. */
		public byte[] publicXml(byte[] xml) {
			if (!matchesXml(xml)) {
				throw new IllegalArgumentException("Native Undo private bytes do not match");
			}
			Set<String> expected = new LinkedHashSet<>();
			for (Captured input : inputs) { if (input.key != null) { expected.add(input.key); } }
			Set<String> seen = new LinkedHashSet<>();
			String text = new String(xml, StandardCharsets.UTF_8);
			Matcher matcher = BINDING.matcher(text);
			StringBuilder publicText = new StringBuilder(text.length());
			int previous = 0;
			while (matcher.find()) {
				String key = matcher.group(1);
				if (!expected.contains(key) || !seen.add(key)) {
					throw new IllegalArgumentException("Foreign or duplicate private history binding");
				}
				publicText.append(text, previous, matcher.start());
				previous = matcher.end();
			}
			publicText.append(text, previous, text.length());
			byte[] result = publicText.toString().getBytes(StandardCharsets.UTF_8);
			if (!seen.equals(expected) || !Arrays.equals(ordinaryDigest, digest(result))) {
				throw new IllegalArgumentException("Native Undo public payload digest does not match");
			}
			return result;
		}

		/** @return a new one-use session without any previous reconstructed GeoPoint */
		public NativeTransactionalState2D newRestoreSession(Construction construction) {
			if (!belongsTo(construction)) {
				throw new IllegalArgumentException("Foreign native Undo construction");
			}
			return new NativeTransactionalState2D(this);
		}
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
				xmlRestoreNanos, dagRestoreNanos, verifyNanos, dagCalls, unsupported,
				hostNodes, sourceMetrics);
	}

	private static final class Entry {
		private final PersistentGeoId id;
		private final String key;
		private final NativeRollbackPointState state;
		private final NativeRollbackSelectionState selection;
		private GeoPoint point;
		private boolean consumed;
		private int serializationVisits;

		private Entry(PersistentGeoId id, String key, NativeRollbackPointState state,
				NativeRollbackSelectionState selection) {
			this.id = id; this.key = key; this.state = state; this.selection = selection;
		}

		private boolean matches() {
			return state != null ? state.matches(point)
					: point.getParentAlgorithm() instanceof AlgoIntersectLineConic
							&& selection.matches((AlgoIntersectLineConic) point.getParentAlgorithm());
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
		public final int hostNodes;
		public final NativeSourceFidelity2D.Metrics sourceMetrics;

		private Metrics(int points, int keys, long capture, long xml, long restore, long dag,
				long verify, int calls, UnsupportedReason reason, int hostNodes,
				NativeSourceFidelity2D.Metrics sourceMetrics) {
			capturedPoints = points; privateBindings = keys; nativeCaptureNanos = capture;
			xmlCaptureNanos = xml; xmlRestoreInclusiveNanos = restore;
			dagRestoreSubsetNanos = dag; verifySubsetNanos = verify; dagCalls = calls;
			unsupported = reason;
			this.hostNodes = hostNodes;
			this.sourceMetrics = sourceMetrics;
		}
	}
}

/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.NativeViewFailure;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.NativeViewRestoreException;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.EuclidianViewInterfaceCommon.DimensionListener;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.MyImageD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Independent R10 oracles: view readers, actual kernel-cache readers and scope guards. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120NativeViewConsumersTest {
	private record ViewState(int width, int height, long[] primitives, long[] cache) {
		static ViewState of(EuclidianView view, Kernel kernel, int index) {
			var settings = view.getSettings();
			return new ViewState(view.getWidth(), view.getHeight(), bits(
					view.getXZero(), view.getYZero(), view.getXscale(), view.getYscale(),
					settings.getXZero(), settings.getYZero(), settings.getXscale(), settings.getYscale(),
					view.getXmin(), view.getXmax(), view.getYmin(), view.getYmax(),
					view.getXminObject().getDouble(), view.getXmaxObject().getDouble(),
					view.getYminObject().getDouble(), view.getYmaxObject().getDouble()),
					cacheBits(kernel, index));
		}

		void assertExact(EuclidianView view, Kernel kernel, int index) {
			ViewState current = of(view, kernel, index);
			assertEquals(width, current.width);
			assertEquals(height, current.height);
			assertArrayEquals(primitives, current.primitives, "Live/settings/bound primitives");
			assertArrayEquals(cache, current.cache, "Each kernel-cache slot must be restored");
			assertFalse(view.isUpdatingBounds(), "The temporary bound guard must close");
		}
	}

	private static long[] bits(double... values) {
		long[] result = new long[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = Double.doubleToLongBits(values[i]);
		}
		return result;
	}

	private static long[] cacheBits(Kernel kernel, int index) {
		return bits(kernel.getXmin(index), kernel.getXmax(index), kernel.getYmin(index),
				kernel.getYmax(index), kernel.getXscale(index), kernel.getYscale(index));
	}

	private static void size(EuclidianView view, int width, int height) {
		var desktop = (EuclidianViewD) view;
		desktop.setSize(new Dimension(width, height));
		desktop.updateSize();
	}

	private static AppGeoCeDG appWithNativeInput() throws Exception {
		return onEdt(() -> {
			AppGeoCeDG app = G9U1TestApp.create();
			size(app.getEuclidianView1(), 1218, 632);
			must(app, "Center=(0,0)");
			must(app, "NativeCircle=Circle(Center,2)");
			must(app, "NativePlace=Point(NativeCircle)");
			app.getEuclidianView1().setRealWorldCoordSystem(14, 36, -20, 6);
			app.getEuclidianView1().zoomAroundCenter(1.1);
			assertNull(NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
			assertNotNull(app.getKernel().getConstruction().captureNativeUndoSnapshot());
			return app;
		});
	}

	private static GeoPoint point(AppGeoCeDG app, String label) {
		return (GeoPoint) app.getKernel().lookupLabel(label);
	}

	private static void assertPointBits(AppGeoCeDG app, String label, double x, double y) {
		GeoPoint point = point(app, label);
		assertTrue(point.isDefined(), label);
		assertArrayEquals(bits(x, y), bits(point.inhomX, point.inhomY), label);
	}

	private static void assertViewCornerAndChild(AppGeoCeDG app, EuclidianView view,
			String corner, String child) {
		double x = view.toRealWorldCoordX(-1);
		double y = view.toRealWorldCoordY(view.getHeight() + 1.0);
		assertPointBits(app, corner, x, y);
		assertPointBits(app, child, x + 1, y + 2);
	}

	private static void addCenteredImageConsumer(AppGeoCeDG app) {
		must(app, "ImageCenter=(5,4)");
		app.getImageManager().addExternalImage("r10-centred-cache.png",
				new MyImageD(new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB)));
		GeoImage image = new GeoImage(app.getKernel().getConstruction());
		image.setImageFileName("r10-centred-cache.png");
		image.setCorner(point(app, "ImageCenter"), 0);
		image.setCentered(true);
		image.setLabel("CenteredPicture");
		must(app, "ImageCorner=Corner(CenteredPicture,1)");
	}

	private static void assertCenteredImageConsumer(AppGeoCeDG app) {
		// GeoImage's centred corner reads Kernel.getXscale/Yscale, unlike Corner(view).
		assertPointBits(app, "ImageCorner", 5 - 4.0 / app.getKernel().getXscale(),
				4 - 3.0 / app.getKernel().getYscale());
	}

	@Test
	void ownedUndoRestoresViewAndKernelConsumersWithoutMaterializingASecondView() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		ViewState before = onEdt(() -> {
			assertFalse(app.hasEuclidianView2EitherShowingOrNot(1));
			must(app, "ViewCorner=Corner(1)");
			must(app, "CornerChild=ViewCorner+(1,2)");
			addCenteredImageConsumer(app);
			assertViewCornerAndChild(app, app.getEuclidianView1(), "ViewCorner", "CornerChild");
			assertCenteredImageConsumer(app);
			baseline(app);
			return ViewState.of(app.getEuclidianView1(), app.getKernel(), 0);
		});
		onEdt(() -> { must(app, "ConsumerMarker=(7,8)"); app.storeUndoInfo(); return null; });
		awaitQuiescent(app);
		onEdt(() -> {
			app.getKernel().undo();
			assertNull(app.getKernel().lookupLabel("ConsumerMarker"));
			before.assertExact(app.getEuclidianView1(), app.getKernel(), 0);
			assertViewCornerAndChild(app, app.getEuclidianView1(), "ViewCorner", "CornerChild");
			assertCenteredImageConsumer(app);
			assertEquals(1, app.getKernel().getConstruction().getLastNativeUndoRestoreMetrics().dagCalls);
			assertFalse(app.hasEuclidianView2EitherShowingOrNot(1));
			app.getKernel().redo();
			assertNotNull(app.getKernel().lookupLabel("ConsumerMarker"));
			before.assertExact(app.getEuclidianView1(), app.getKernel(), 0);
			assertViewCornerAndChild(app, app.getEuclidianView1(), "ViewCorner", "CornerChild");
			assertCenteredImageConsumer(app);
			assertFalse(app.hasEuclidianView2EitherShowingOrNot(1));
			return null;
		});
	}

	@Test
	void twoExistingViewsRetainDistinctFramesAndBothKernelCachePrefixes() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		ViewState[] before = onEdt(() -> {
			EuclidianView second = ((GuiManagerD) app.getGuiManager()).getEuclidianView2(1);
			size(second, 960, 540);
			second.setRealWorldCoordSystem(-9, 8, -5, 12);
			second.zoomAroundCenter(1.2);
			// Set the first frame after EV2 exists, through the native setter. The
			// fixture does not assume untouched cache prefixes while arrays first grow.
			app.getEuclidianView1().setRealWorldCoordSystem(14, 36, -20, 6);
			app.getEuclidianView1().zoomAroundCenter(1.1);
			must(app, "FirstCorner=Corner(1)");
			must(app, "FirstChild=FirstCorner+(1,2)");
			assertViewCornerAndChild(app, app.getEuclidianView1(), "FirstCorner", "FirstChild");
			// The second view exists but is not showing in this embedded host.
			// Corner(2,1) correctly refuses that state; its frame/cache oracle is direct.
			ViewState[] pre = {ViewState.of(app.getEuclidianView1(), app.getKernel(), 0),
					ViewState.of(second, app.getKernel(), 1)};
			baseline(app);
			return pre;
		});
		ViewState[] after = onEdt(() -> {
			must(app, "TwoViewMarker=(7,8)");
			// A queued native component resize may change cached bounds between
			// EDT turns while leaving all coordinate tuple bits unchanged. Redo
			// restores this actual post-state, not the earlier baseline's cache.
			ViewState[] post = {ViewState.of(app.getEuclidianView1(), app.getKernel(), 0),
					ViewState.of(app.getEuclidianView2(1), app.getKernel(), 1)};
			app.storeUndoInfo();
			return post;
		});
		awaitQuiescent(app);
		onEdt(() -> {
			assertTrue(app.hasEuclidianView2EitherShowingOrNot(1));
			EuclidianView second = app.getEuclidianView2(1);
			app.getKernel().undo();
			assertNull(app.getKernel().lookupLabel("TwoViewMarker"));
			before[0].assertExact(app.getEuclidianView1(), app.getKernel(), 0);
			before[1].assertExact(second, app.getKernel(), 1);
			assertViewCornerAndChild(app, app.getEuclidianView1(), "FirstCorner", "FirstChild");
			assertEquals(1, app.getKernel().getConstruction().getLastNativeUndoRestoreMetrics().dagCalls);
			app.getKernel().redo();
			assertNotNull(app.getKernel().lookupLabel("TwoViewMarker"));
			after[0].assertExact(app.getEuclidianView1(), app.getKernel(), 0);
			after[1].assertExact(second, app.getKernel(), 1);
			assertViewCornerAndChild(app, app.getEuclidianView1(), "FirstCorner", "FirstChild");
			assertEquals(1, app.getKernel().getConstruction().getLastNativeUndoRestoreMetrics().dagCalls);
			return null;
		});
	}

	@Test
	void foreignTemplatesAndInactiveSessionsCannotWriteAnyViewOrCache() throws Exception {
		AppGeoCeDG owner = appWithNativeInput();
		AppGeoCeDG other = appWithNativeInput();
		onEdt(() -> {
			var ownerConstruction = owner.getKernel().getConstruction();
			var otherConstruction = other.getKernel().getConstruction();
			var captured = ownerConstruction.captureNativeUndoSnapshot();
			assertNotNull(captured);
			ViewState beforeOwner = ViewState.of(owner.getEuclidianView1(), owner.getKernel(), 0);
			ViewState beforeOther = ViewState.of(other.getEuclidianView1(), other.getKernel(), 0);
			AtomicBoolean readerRan = new AtomicBoolean();
			assertThrows(IllegalStateException.class, () -> otherConstruction.restoreNativeUndoSnapshot(
					captured.template(), () -> readerRan.set(true)));
			assertFalse(readerRan.get(), "Foreign ownership must fail before the reader");
			assertThrows(IllegalArgumentException.class,
					() -> captured.template().newRestoreSession(otherConstruction));
			var inactive = captured.template().newRestoreSession(ownerConstruction);
			inactive.beginRestore();
			NativeViewRestoreException rejected = assertThrows(NativeViewRestoreException.class,
					inactive::restoreViewsBeforeDag);
			assertEquals(NativeViewFailure.INACTIVE_SESSION, rejected.reason());
			assertThrows(IllegalStateException.class, () -> owner.getKernel().restoreNativeViewCache(null));
			beforeOwner.assertExact(owner.getEuclidianView1(), owner.getKernel(), 0);
			beforeOther.assertExact(other.getEuclidianView1(), other.getKernel(), 0);
			return null;
		});
	}

	@Test
	void aNamedBoundCannotBeOverwrittenByAnAdmittedPrimitiveRestore() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		onEdt(() -> {
			GeoNumeric named = (GeoNumeric) must(app, "namedBound=-123");
			EuclidianView view = app.getEuclidianView1();
			var construction = app.getKernel().getConstruction();
			var captured = construction.captureNativeUndoSnapshot();
			assertNotNull(captured);
			ViewState before = ViewState.of(view, app.getKernel(), 0);
			String xml = app.getXML();
			boolean loading = app.getKernel().getLoadingMode();
			long value = Double.doubleToLongBits(named.getDouble());
			NativeViewRestoreException rejected = assertThrows(NativeViewRestoreException.class,
					() -> construction.restoreNativeUndoSnapshot(captured.template(), () -> {
						GeoNumeric previous = view.getXminObject();
						try {
							view.setXminObject(named);
							construction.updateConstruction(false);
						} finally {
							assertEquals(value, Double.doubleToLongBits(named.getDouble()),
									"An excluded bound is a read-only input of this repair");
							view.setXminObject(previous);
						}
					}));
			assertEquals(NativeViewFailure.UNSUPPORTED_VIEW_CLASS, rejected.reason());
			before.assertExact(view, app.getKernel(), 0);
			assertEquals(xml, app.getXML(), "The outer native rollback preserves the real pre-state");
			assertEquals(loading, app.getKernel().getLoadingMode());
			assertEquals(value, Double.doubleToLongBits(
					((GeoNumeric) app.getKernel().lookupLabel("namedBound")).getDouble()));
			return null;
		});
	}

	@Test
	void aFailedNativeMutationRestoresTheViewAndConsumersThroughTheNormalRollback() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		onEdt(() -> {
			must(app, "RollbackCorner=Corner(1)");
			must(app, "RollbackChild=RollbackCorner+(1,2)");
			addCenteredImageConsumer(app);
			ViewState before = ViewState.of(app.getEuclidianView1(), app.getKernel(), 0);
			String xml = app.getXML();
			boolean loading = app.getKernel().getLoadingMode();
			IllegalStateException injected = new IllegalStateException("R10 view consumer injected failure");
			IllegalStateException result = assertThrows(IllegalStateException.class,
					() -> app.getKernel().getConstruction().runAtomicConstructionMutation(() -> {
						app.getEuclidianView1().zoomAroundCenter(1.2);
						throw injected;
					}));
			assertSame(injected, result, "A certified rollback preserves the original cause");
			before.assertExact(app.getEuclidianView1(), app.getKernel(), 0);
			assertViewCornerAndChild(app, app.getEuclidianView1(), "RollbackCorner", "RollbackChild");
			assertCenteredImageConsumer(app);
			assertEquals(xml, app.getXML());
			assertEquals(loading, app.getKernel().getLoadingMode());
			assertEquals(1, app.getKernel().getConstruction().getLastNativeTransactionMetrics().dagCalls);
			assertFalse(app.hasEuclidianView2EitherShowingOrNot(1));
			return null;
		});
	}

	@Test
	void anOutsideClassCacheClobberCannotHideBehindAnUnchangedCoordinateTuple() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		onEdt(() -> {
			EuclidianView view = app.getEuclidianView1();
			Kernel kernel = app.getKernel();
			var construction = kernel.getConstruction();
			// A fixed binary64 tuple avoids requiring any repair of this excluded
			// view. Its named bound stays a read-only construction input.
			view.setCoordSystem(100, 200, 64, 32);
			GeoNumeric named = (GeoNumeric) must(app, "outsideBound=-1.5625");
			view.setXminObject(named);
			view.getSettings().setXminObject(named, false);
			assertFalse(view.isZoomable());
			var captured = construction.captureNativeUndoSnapshot();
			assertNotNull(captured);
			ViewState before = ViewState.of(view, kernel, 0);
			String xml = app.getXML();
			long boundValue = Double.doubleToLongBits(named.getDouble());
			NativeViewRestoreException rejected = assertThrows(NativeViewRestoreException.class,
					() -> construction.restoreNativeUndoSnapshot(captured.template(), () -> {
						boolean loading = construction.isFileLoading();
						try {
							construction.setFileLoading(true);
							app.getXMLio().processConstructionSnapshot(captured.xml().toString(), false);
						} catch (Exception failure) {
							throw new IllegalStateException("The unchanged outside-class reader failed", failure);
						} finally {
							construction.setFileLoading(loading);
						}
						before.assertExact(view, kernel, 0);
						// Simulate a post-reader callback that modifies only the native
						// cache. Ordinary XML and all view/settings coordinates stay equal.
						kernel.setEuclidianViewBounds(1, kernel.getXmin(0), kernel.getXmax(0),
								kernel.getYmin(0), kernel.getYmax(0), 128, kernel.getYscale(0));
						assertArrayEquals(before.primitives, ViewState.of(view, kernel, 0).primitives);
						assertEquals(xml, app.getXML(), "Ordinary XML alone cannot detect this cache delta");
						assertEquals(boundValue, Double.doubleToLongBits(
								((GeoNumeric) kernel.lookupLabel("outsideBound")).getDouble()));
					}));
			assertEquals(NativeViewFailure.UNSUPPORTED_VIEW_CLASS, rejected.reason());
			before.assertExact(view, kernel, 0);
			assertEquals(xml, app.getXML());
			assertEquals(boundValue, Double.doubleToLongBits(
					((GeoNumeric) kernel.lookupLabel("outsideBound")).getDouble()));
			return null;
		});
	}

	@Test
	void aOneShotFailureInsideTheGuardedSetterRestoresTheOwnedUndoPreState() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		onEdt(() -> { baseline(app); must(app, "GuardMarker=(7,8)"); app.storeUndoInfo(); return null; });
		awaitQuiescent(app);
		onEdt(() -> {
			EuclidianView view = app.getEuclidianView1();
			Kernel kernel = app.getKernel();
			var construction = kernel.getConstruction();
			ViewState before = ViewState.of(view, kernel, 0);
			String xml = app.getXML();
			var commands = undo(app).getCommands();
			int cursor = undo(app).getCursor();
			boolean loading = kernel.getLoadingMode();
			boolean fileLoading = construction.isFileLoading();
			GeoPoint nativePlace = point(app, "NativePlace");
			long[] nativeBits = bits(nativePlace.x, nativePlace.y, nativePlace.z,
					nativePlace.getPathParameter().t);
			AtomicBoolean fired = new AtomicBoolean();
			IllegalStateException injected = new IllegalStateException("Inside R10 guarded view setter");
			DimensionListener listener = () -> {
				if (view.isUpdatingBounds() && fired.compareAndSet(false, true)) {
					assertTrue(construction.isUpdateConstructionRunning(),
							"The fault must enter through the pre-DAG restoration seam");
					assertTrue(kernel.getLoadingMode(), "The failure belongs to private view restoration");
					throw injected;
				}
			};
			view.addDimensionListener(listener);
			try {
				IllegalStateException failure = assertThrows(IllegalStateException.class, kernel::undo);
				assertTrue(fired.get(), "Earlier ordinary parsing must not consume the one-shot fault");
				boolean causeFound = false;
				for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
					causeFound |= cause == injected;
				}
				assertTrue(causeFound, "The actual guarded-setter cause must survive owned Undo failure");
			} finally {
				view.removeDimensionListener(listener);
			}
			before.assertExact(view, kernel, 0);
			assertEquals(xml, app.getXML());
			assertEquals(loading, kernel.getLoadingMode());
			assertEquals(fileLoading, construction.isFileLoading());
			assertEquals(cursor, undo(app).getCursor());
			var restoredCommands = undo(app).getCommands();
			assertEquals(commands.size(), restoredCommands.size());
			for (int i = 0; i < commands.size(); i++) {
				assertSame(commands.get(i), restoredCommands.get(i), "Original owned history command " + i);
			}
			GeoPoint restoredPlace = point(app, "NativePlace");
			assertArrayEquals(nativeBits, bits(restoredPlace.x, restoredPlace.y, restoredPlace.z,
					restoredPlace.getPathParameter().t));
			assertNotNull(kernel.lookupLabel("GuardMarker"));
			// The listener is removed and the failed session is closed: the same
			// owned transition remains usable rather than being left half consumed.
			kernel.undo();
			assertNull(kernel.lookupLabel("GuardMarker"));
			before.assertExact(view, kernel, 0);
			assertEquals(1, construction.getLastNativeUndoRestoreMetrics().dagCalls);
			kernel.redo();
			assertNotNull(kernel.lookupLabel("GuardMarker"));
			before.assertExact(view, kernel, 0);
			return null;
		});
	}

	@Test
	void aGenuineVisibleInsetChangeIsRejectedBeforeAnyPrimitiveIsWritten() throws Exception {
		AppGeoCeDG app = appWithNativeInput();
		onEdt(() -> {
			EuclidianView view = app.getEuclidianView1();
			Kernel kernel = app.getKernel();
			var construction = kernel.getConstruction();
			var captured = construction.captureNativeUndoSnapshot();
			assertNotNull(captured);
			ViewState before = ViewState.of(view, kernel, 0);
			String xml = app.getXML();
			int visibleFromX = view.getSettings().getVisibleFromX();
			boolean loading = kernel.getLoadingMode();
			NativeViewRestoreException rejected = assertThrows(NativeViewRestoreException.class,
					() -> construction.restoreNativeUndoSnapshot(captured.template(), () -> {
						try {
							// This is an actual occlusion offset edit, not a size hint
							// populated by the native XML reader from unchanged live size.
							view.getSettings().setVisibleFromX(visibleFromX + 2);
							construction.updateConstruction(false);
						} finally {
							view.getSettings().setVisibleFromX(visibleFromX);
							before.assertExact(view, kernel, 0);
						}
					}));
			assertEquals(NativeViewFailure.VIEW_DIMENSIONS_CHANGED, rejected.reason());
			assertEquals(visibleFromX, view.getSettings().getVisibleFromX());
			before.assertExact(view, kernel, 0);
			assertEquals(xml, app.getXML(), "The unchanged pre-state remains the rollback target");
			assertEquals(loading, kernel.getLoadingMode());
			return null;
		});
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.layout.DockManagerD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Shared desktop test host; commands still use the real application dispatcher. */
final class G9U1TestApp {

	private G9U1TestApp() {
	}

	/** Retire only the global registrations created by an embedded test host. */
	static final class Lifecycle implements BeforeEachCallback, AfterEachCallback {
		private static final ExtensionContext.Namespace NAMESPACE =
				ExtensionContext.Namespace.create(Lifecycle.class);

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			context.getStore(NAMESPACE).put("roots", GlobalRoots.capture());
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			GlobalRoots roots = context.getStore(NAMESPACE).remove("roots", GlobalRoots.class);
			if (roots != null) {
				roots.close();
			}
		}
	}

	/** A test-local ownership snapshot; never a blanket application shutdown. */
	static final class GlobalRoots implements AutoCloseable {
		private final Set<GeoGebraFrame> frames = identitySet();
		private final Set<DockManagerD> managers;

		private GlobalRoots() {
			frames.addAll(GeoGebraFrame.getInstances());
			managers = dockManagers();
		}

		static GlobalRoots capture() throws Exception {
			AtomicReference<GlobalRoots> roots = new AtomicReference<>();
			onEventThread(() -> roots.set(new GlobalRoots()));
			return roots.get();
		}

		@Override
		public void close() throws Exception {
			onEventThread(this::release);
		}

		private void release() {
			Set<DockManagerD> createdManagers = dockManagers();
			createdManagers.removeAll(managers);
			Set<GeoGebraFrame> createdFrames = identitySet();
			createdFrames.addAll(GeoGebraFrame.getInstances());
			createdFrames.removeAll(frames);
			Set<AppD> createdApps = identitySet();
			for (DockManagerD manager : createdManagers) {
				AppD app = manager.getLayout().getApplication();
				requireEmbeddedHost(app);
				createdApps.add(app);
			}
			for (GeoGebraFrame frame : createdFrames) {
				requireEmbeddedHost(frame.getApplication());
				if (frame.isVisible() || frame.isDisplayable()) {
					throw new IllegalStateException("A real window requires its own lifecycle");
				}
				createdApps.add(frame.getApplication());
			}
			// Validate every owner before removing anything. Never touch preexisting roots.
			for (DockManagerD manager : createdManagers) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(manager);
			}
			for (GeoGebraFrame frame : createdFrames) {
				// Lazy frames have no native peer; dispose removes the host's
				// static registry entry.
				frame.dispose();
			}
			for (AppD app : createdApps) {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(app);
			}
		}
	}

	static Set<DockManagerD> dockManagers() {
		Set<DockManagerD> managers = identitySet();
		for (AWTEventListener registered : Toolkit.getDefaultToolkit().getAWTEventListeners()) {
			AWTEventListener listener = registered instanceof AWTEventListenerProxy
					? ((AWTEventListenerProxy) registered).getListener() : registered;
			if (listener instanceof DockManagerD) {
				managers.add((DockManagerD) listener);
			}
		}
		return managers;
	}

	private static <T> Set<T> identitySet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	private static void onEventThread(Runnable action) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
		} else {
			SwingUtilities.invokeAndWait(action);
		}
	}

	private static void requireEmbeddedHost(AppD app) {
		if (app == null || !(app.getMainComponent() instanceof JPanel)
				|| SwingUtilities.getWindowAncestor(app.getMainComponent()) != null) {
			throw new IllegalStateException("Only test-owned embedded hosts may be retired");
		}
	}

	static AppGeoCeDG create() {
		return create(true);
	}

	static AppGeoCeDG create(boolean enabled) {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(
				new String[] {"--silent", "--enableLocusV2=" + enabled}), new JPanel());
		app.setErrorDialogsActive(false);
		return withoutWindowDispatcher(app);
	}

	static <T extends AppD> T withoutWindowDispatcher(T app) {
		// getFrame() creates a JFrame lazily; inspect the existing component only.
		if (!(app.getMainComponent() instanceof JPanel)
				|| SwingUtilities.getWindowAncestor(app.getMainComponent()) != null) {
			throw new IllegalArgumentException(
					"Windowed apps require their real keyboard lifecycle");
		}
		// These embedded fixtures call handlers directly, not the global focus dispatcher.
		// AppD registers itself globally even without a window, retaining every test kernel.
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(app);
		return app;
	}

	static GeoElement eval(AppGeoCeDG app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		assertTrue(result.length > 0, command);
		return result[0].toGeoElement();
	}

	static GeoElement lookup(AppGeoCeDG app, String label) {
		GeoElement result = app.getKernel().lookupLabel(label);
		assertNotNull(result, label);
		return result;
	}
}

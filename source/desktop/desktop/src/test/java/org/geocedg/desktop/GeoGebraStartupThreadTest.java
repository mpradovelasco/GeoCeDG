/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.geogebra.desktop.GeoGebra;
import org.junit.jupiter.api.Test;

class GeoGebraStartupThreadTest {

	@Test
	void productInitializationIsMarshalledFromLauncherThreadToSwing() throws Exception {
		AtomicBoolean ranOnSwing = new AtomicBoolean();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread launcher = new Thread(() -> {
			try {
				runOnEventDispatchThreadAndWait(
						() -> ranOnSwing.set(SwingUtilities.isEventDispatchThread()));
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		}, "geocedg-product-launcher-test");

		launcher.start();
		launcher.join(10_000);

		assertFalse(launcher.isAlive());
		assertNull(failure.get());
		assertTrue(ranOnSwing.get());
	}

	@Test
	void productInitializationRunsDirectlyWhenAlreadyOnSwing() throws Exception {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		AtomicBoolean ran = new AtomicBoolean();
		SwingUtilities.invokeAndWait(() -> {
			Thread eventThread = Thread.currentThread();
			try {
				runOnEventDispatchThreadAndWait(() -> {
					assertSame(eventThread, Thread.currentThread());
					ran.set(true);
				});
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		});

		assertNull(failure.get());
		assertTrue(ran.get());
	}

	private static void runOnEventDispatchThreadAndWait(Runnable action) throws Throwable {
		Method helper = GeoGebra.class.getDeclaredMethod(
				"runOnEventDispatchThreadAndWait", Runnable.class);
		helper.setAccessible(true);
		try {
			helper.invoke(null, action);
		} catch (InvocationTargetException exception) {
			throw exception.getCause();
		}
	}
}

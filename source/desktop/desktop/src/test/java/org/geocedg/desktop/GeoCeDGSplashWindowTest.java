/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.geogebra.desktop.SplashWindow;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class GeoCeDGSplashWindowTest {

	@Test
	void productForegroundPolicyShowsBeforeRaisingAndRequestsAlwaysOnTop()
			throws Throwable {
		SplashWindow window = mock(SplashWindow.class);
		when(window.isAlwaysOnTopSupported()).thenReturn(true);

		showSplashWindow(window, true);

		InOrder order = inOrder(window);
		order.verify(window).isAlwaysOnTopSupported();
		order.verify(window).setAlwaysOnTop(true);
		order.verify(window).setVisible(true);
		order.verify(window).toFront();
	}

	@Test
	void productForegroundPolicyStillRaisesWhenAlwaysOnTopIsUnsupported()
			throws Throwable {
		SplashWindow window = mock(SplashWindow.class);
		when(window.isAlwaysOnTopSupported()).thenReturn(false);

		showSplashWindow(window, true);

		verify(window, never()).setAlwaysOnTop(true);
		InOrder order = inOrder(window);
		order.verify(window).setVisible(true);
		order.verify(window).toFront();
	}

	@Test
	void classicPolicyRetainsInheritedOrderingWithoutAlwaysOnTop()
			throws Throwable {
		SplashWindow window = mock(SplashWindow.class);

		showSplashWindow(window, false);

		verify(window, never()).isAlwaysOnTopSupported();
		verify(window, never()).setAlwaysOnTop(true);
		InOrder order = inOrder(window);
		order.verify(window).toFront();
		order.verify(window).setVisible(true);
	}

	private static void showSplashWindow(SplashWindow window, boolean foreground)
			throws Throwable {
		Method helper = SplashWindow.class.getDeclaredMethod(
				"showSplashWindow", SplashWindow.class, boolean.class);
		helper.setAccessible(true);
		try {
			helper.invoke(null, window, foreground);
		} catch (InvocationTargetException exception) {
			throw exception.getCause();
		}
	}
}

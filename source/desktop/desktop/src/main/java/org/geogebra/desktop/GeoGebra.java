/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 * 
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.desktop;

import java.awt.Frame;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.geogebra.common.main.PreviewFeature;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GeoGebraServer;
import org.geogebra.desktop.util.GuiResourcesD;

public class GeoGebra {

	private static Frame splashFrame = null;

	protected GeoGebra() {
	}

	/**
	 * Run the app.
	 * @param cmdArgs command line arguments
	 */
	public static void main(String[] cmdArgs) {
		doMain(cmdArgs, GeoGebraFrame::new);
	}

	/**
	 * @param cmdArgs Command line arguments
	 * @param frameFactory frame constructor
	 */
	public static void doMain(String[] cmdArgs, Supplier<GeoGebraFrame> frameFactory) {
		doMain(cmdArgs, frameFactory,
				() -> GeoGebra.class.getResource(GuiResourcesD.SPLASH.getFilename()),
				false, false);
	}

	/**
	 * Start a Desktop product with its own splash resource while preserving all
	 * existing command-line and lifecycle behavior.
	 *
	 * @param cmdArgs command line arguments
	 * @param frameFactory frame constructor
	 * @param splashResource product splash resource supplier
	 */
	public static void doMain(String[] cmdArgs, Supplier<GeoGebraFrame> frameFactory,
			Supplier<URL> splashResource) {
		doMain(cmdArgs, frameFactory, splashResource, true, true);
	}

	private static void doMain(String[] cmdArgs,
			Supplier<GeoGebraFrame> frameFactory, Supplier<URL> splashResource,
			boolean initializeOnEventDispatchThread, boolean foregroundSplash) {

		CommandLineArguments args = new CommandLineArguments(cmdArgs);

		boolean showSplash = true;
		if (!args.getBooleanValue("showSplash", true)) {
			showSplash = false;
		}
		if (args.containsArg("prerelease")) {
			PreviewFeature.setPreviewFeaturesEnabled(true);
			Log.warn("!!! Running with --prerelease");
		}
		if (args.containsArg("startHttpServer")) {
			new GeoGebraServer().start();
			return;
		}
		if (args.containsArg("help") || args.containsArg("proverhelp")
				|| args.containsArg("v")
				|| args.containsArg("regressionFile")) {
			showSplash = false;
		}

		if (showSplash) {
			// Show splash screen
			URL imageURL = splashResource.get();
			if (imageURL != null) {
				splashFrame = SplashWindow.splash(
						Toolkit.getDefaultToolkit().createImage(imageURL), foregroundSplash);
			} else {
				System.err.println("Splash image not found");
			}
		}

		// Start GeoGebra
		try {
			Runnable initialize = () -> GeoGebraFrame.init(args, frameFactory.get());
			if (initializeOnEventDispatchThread) {
				runOnEventDispatchThreadAndWait(initialize);
			} else {
				initialize.run();
			}
		} catch (Throwable e) {
			Log.debug(e);
			System.err.flush();
			AppD.exit(10);
		}

		// Hide splash screen
		if (splashFrame != null) {
			splashFrame.setVisible(false);
		}
	}

	static void runOnEventDispatchThreadAndWait(Runnable action)
			throws InterruptedException, InvocationTargetException {
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
			return;
		}
		SwingUtilities.invokeAndWait(action);
	}

	/**
	 * Hide the splash window
	 */
	public static void hideSplash() {
		if (splashFrame != null) {
			splashFrame.setVisible(false);
		}
	}

}

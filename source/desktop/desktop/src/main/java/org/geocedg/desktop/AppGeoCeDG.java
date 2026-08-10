/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Container;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.main.AppD;

/**
 * Desktop application instance bound to the GeoCeDG product profile.
 */
public final class AppGeoCeDG extends App3D {

	/**
	 * @param args command line arguments
	 * @param frame product frame
	 */
	public AppGeoCeDG(CommandLineArguments args, JFrame frame) {
		super(args, frame, new AppConfigGeoCeDG());
	}

	/**
	 * @param args command line arguments
	 * @param component parent component
	 */
	public AppGeoCeDG(CommandLineArguments args, Container component) {
		super(args, component, new AppConfigGeoCeDG());
	}

	@Override
	public Perspective getTmpPerspective() {
		Perspective loadedPerspective = super.getTmpPerspective();
		return loadedPerspective == null
				? GeoCeDGProfile.createInitialPerspective() : loadedPerspective;
	}

	@Override
	public void createNewWindow() {
		GeoCeDGFrame.createNewWindow(cmdArgs.getGlobalArguments());
	}

	@Override
	protected AppD newAppForTemplateOrInsertFile() {
		return new AppGeoCeDG(new CommandLineArguments(null), new JPanel());
	}

	@Override
	protected Image getFrameIcon() {
		return null;
	}

	@Override
	protected GuiManagerD newGuiManager() {
		return new GuiManagerGeoCeDG(this);
	}
}

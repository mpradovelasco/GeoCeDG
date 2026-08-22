/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Container;
import java.awt.Image;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
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
		this(args, frame, createConfig(args));
	}

	private AppGeoCeDG(CommandLineArguments args, JFrame frame,
			AppConfigGeoCeDG config) {
		super(args, frame, config);
		bindFeatureService(config);
	}

	/**
	 * @param args command line arguments
	 * @param component parent component
	 */
	public AppGeoCeDG(CommandLineArguments args, Container component) {
		this(args, component, createConfig(args));
	}

	private AppGeoCeDG(CommandLineArguments args, Container component,
			AppConfigGeoCeDG config) {
		super(args, component, config);
		bindFeatureService(config);
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
	public GeoGebraFrame createNewWindow(CommandLineArguments arguments) {
		return GeoCeDGFrame.createNewWindow(arguments);
	}

	@Override
	protected AppD newAppForTemplateOrInsertFile() {
		AppConfigGeoCeDG config = (AppConfigGeoCeDG) getConfig();
		return new AppGeoCeDG(new CommandLineArguments(null), new JPanel(),
				new AppConfigGeoCeDG(config.getRuntimeFeatureService()
						.isLocusV2CreationEnabled(), config.getRuntimeFeatureService()
								.isExtendedDxfEnabled()));
	}

	@Override
	public EuclidianController newEuclidianController(Kernel kernel) {
		return new GeoCeDGEuclidianController(kernel);
	}

	@Override
	protected Image getFrameIcon() {
		return null;
	}

	@Override
	protected GuiManagerD newGuiManager() {
		return new GuiManagerGeoCeDG(this);
	}

	@Override
	public boolean saveGeoGebraFile(File file) {
		if (!GeoCeDGDocumentPolicy.isNative(file)) {
			showError(Errors.InvalidInput, file == null ? "" : file.getName());
			return false;
		}
		return super.saveGeoGebraFile(file);
	}

	@Override
	protected AppConfig createDocumentPreflightConfig() {
		AppConfigGeoCeDG config = (AppConfigGeoCeDG) getConfig();
		return new AppConfigGeoCeDG(config.getRuntimeFeatureService()
				.isLocusV2CreationEnabled(), config.getRuntimeFeatureService()
						.isExtendedDxfEnabled());
	}

	private static AppConfigGeoCeDG createConfig(CommandLineArguments args) {
		return new AppConfigGeoCeDG(args != null && args.getBooleanValue(
				RuntimeFeatureService.LOCUS_V2_ARGUMENT, false),
				args != null && args.getBooleanValue(
						RuntimeFeatureService.EXTENDED_DXF_ARGUMENT, false));
	}

	private void bindFeatureService(AppConfigGeoCeDG config) {
		config.getRuntimeFeatureService().bindPreservationContext(
				() -> getKernel().getConstruction().isFileLoading());
	}
}

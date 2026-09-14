/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.desktop.resources.GeoCeDGBrandingResource;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.common.main.settings.FontSettings;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolNavigationD;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GlobalKeyDispatcherD;

/**
 * Desktop application instance bound to the GeoCeDG product profile.
 */
public final class AppGeoCeDG extends App3D {
	private GeoCeDGPresentationPreferences presentationPreferences;

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
		initializePresentationPreferences();
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
		initializePresentationPreferences();
	}

	@Override
	public Perspective getTmpPerspective() {
		Perspective loadedPerspective = super.getTmpPerspective();
		return loadedPerspective == null
				? GeoCeDGWorkspaceController.loadInitialPerspective() : loadedPerspective;
	}

	/** @return whether a loaded document supplied its own presentation layout */
	public boolean hasDocumentPerspective() {
		return super.getTmpPerspective() != null;
	}

	@Override
	protected void exitFrame() {
		GeoCeDGWorkspaceController.saveCurrentLayout(this);
		super.exitFrame();
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
	protected EuclidianView newEuclidianView(boolean[] showAxes, boolean showGrid) {
		return new GeoCeDGEuclidianView(getEuclidianController(), showAxes,
				showGrid, 1, getSettings().getEuclidian(1));
	}

	@Override
	public void setLocale(Locale locale) {
		// Product language policy only; never remove the upstream locale corpus.
		super.setLocale(locale != null && "es".equals(locale.getLanguage())
				? Locale.forLanguageTag("es") : Locale.ENGLISH);
	}

	@Override
	protected Image getFrameIcon() {
		return getInternalImage(getFrameIconResource());
	}

	static GeoCeDGBrandingResource getFrameIconResource() {
		return GeoCeDGBrandingResource.APPLICATION_ICON;
	}

	@Override
	protected void showPerspectivePopup() {
		// Construction already has its declarative workspace. Do not cover startup
		// with the inherited Classic perspective chooser; explicit Classic stays separate.
	}

	@Override
	protected GuiManagerD newGuiManager() {
		return new GuiManagerGeoCeDG(this);
	}

	@Override
	public int getGUIFontSize() {
		FontSettings fonts = getSettings().getFontSettings();
		return fonts.getGuiFontSize() == -1
				? getDefaultSettings().getAppFontSize() : fonts.getGuiFontSize();
	}

	@Override
	public void setGUIFontSize(int size) {
		if (presentationPreferences == null) {
			super.setGUIFontSize(size);
			return;
		}
		getFontSettingsUpdater().setGUIFontSizeAndUpdate(size);
		reapplyToolbarIconSize();
	}

	@Override
	public void setFontSize(int points, boolean update) {
		super.setFontSize(points, update);
		if (presentationPreferences != null) {
			reapplyToolbarIconSize();
		}
	}

	@Override
	public Font getMenuFont() {
		return presentationFont(GeoCeDGPresentationPreferences.Category.MENU_FONT,
				super.getMenuFont());
	}

	@Override
	public Font getAlgebraFont() {
		return presentationFont(GeoCeDGPresentationPreferences.Category.ALGEBRA_FONT,
				super.getAlgebraFont());
	}

	@Override
	public Font getConstructionProtocolFont() {
		return presentationFont(
				GeoCeDGPresentationPreferences.Category.CONSTRUCTION_PROTOCOL_FONT,
				super.getConstructionProtocolFont());
	}

	@Override
	public int getEuclidianViewFontSize() {
		return presentationPreferences == null ? super.getEuclidianViewFontSize()
				: presentationPreferences.get(
						GeoCeDGPresentationPreferences.Category.GRAPHICS_FONT);
	}

	@Override
	public int getScaledIconSize() {
		return presentationPreferences == null ? super.getScaledIconSize()
				: presentationPreferences.get(
						GeoCeDGPresentationPreferences.Category.TOOLBAR_ICON);
	}

	@Override
	public OptionPanelD newProductPresentationOptionsPanel() {
		return presentationPreferences == null ? null
				: new GeoCeDGPresentationOptionsPanel(this);
	}

	int getPresentationSize(GeoCeDGPresentationPreferences.Category category) {
		return presentationPreferences.get(category);
	}

	void setPresentationSize(GeoCeDGPresentationPreferences.Category category,
			int size) {
		presentationPreferences.set(category, size);
		refreshPresentation(category);
	}

	private Font presentationFont(GeoCeDGPresentationPreferences.Category category,
			Font inherited) {
		return presentationPreferences == null ? inherited
				: inherited.deriveFont((float) presentationPreferences.get(category));
	}

	private void initializePresentationPreferences() {
		presentationPreferences = new GeoCeDGPresentationPreferences(
				new GeoCeDGPresentationPreferences.InheritedValues(
						getGUIFontSize(), super.getScaledIconSize(),
						getPlainFont().getSize(), getPlainFont().getSize(), getFontSize()));
		getImageManager().setMaxIconSize(getPresentationSize(
				GeoCeDGPresentationPreferences.Category.TOOLBAR_ICON));
		getEuclidianView1().updateFonts();
		if (getGuiManager() != null) {
			((GuiManagerD) getGuiManager()).updateFonts();
		}
	}

	private void reapplyToolbarIconSize() {
		getImageManager().setMaxIconSize(getPresentationSize(
				GeoCeDGPresentationPreferences.Category.TOOLBAR_ICON));
		if (getGuiManager() != null) {
			((GuiManagerD) getGuiManager()).updateToolbar();
		}
	}

	private void refreshPresentation(GeoCeDGPresentationPreferences.Category category) {
		GuiManagerD manager = getGuiManager() == null ? null
				: (GuiManagerD) getGuiManager();
		switch (category) {
		case MENU_FONT:
			if (manager != null && manager.getMenuBar() instanceof GeoGebraMenuBar menuBar) {
				menuBar.updateFonts();
			}
			break;
		case TOOLBAR_ICON:
			getImageManager().setMaxIconSize(getPresentationSize(category));
			if (manager != null) {
				manager.updateToolbar();
			}
			break;
		case ALGEBRA_FONT:
			if (manager != null) {
				manager.getAlgebraView().updateFonts();
			}
			break;
		case CONSTRUCTION_PROTOCOL_FONT:
			if (manager != null && manager.isUsingConstructionProtocol()) {
				((ConstructionProtocolViewD) manager.getConstructionProtocolView()).initGUI();
				if (manager.getCPNavigationIfExists()
						instanceof ConstructionProtocolNavigationD navigation) {
					navigation.initGUI();
				}
			}
			break;
		case GRAPHICS_FONT:
			getEuclidianView1().updateFonts();
			if (hasEuclidianView2EitherShowingOrNot(1)) {
				getEuclidianView2(1).updateFonts();
			}
			break;
		default:
			throw new IllegalStateException("Unhandled presentation category " + category);
		}
	}

	@Override
	protected GlobalKeyDispatcherD newGlobalKeyDispatcher() {
		return new GlobalKeyDispatcherD(this) {
			@Override
			public boolean handleGeneralKeys(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ESCAPE
						&& getActiveEuclidianView() != null
						&& getActiveEuclidianView().getEuclidianController()
								instanceof GeoCeDGEuclidianController) {
					GeoCeDGEuclidianController controller =
							(GeoCeDGEuclidianController) getActiveEuclidianView()
									.getEuclidianController();
					if (controller.isZoomWindowActive()) {
						controller.cancelZoomWindow();
					}
				}
				return super.handleGeneralKeys(event);
			}
		};
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
		getKernel().setDocumentMacroCommandAuthorityEnabled(true);
		getKernel().getAlgebraProcessor().setSpatialRedefineAssessmentHandler(
				new GeoCeDGSpatialRedefineFrontend(this));
	}

	@Override
	protected boolean isAdditionalTransactionalDocument(FileExtensions extension) {
		return FileExtensions.GEOGEBRA.equals(extension);
	}

	void setSpatialRedefinePresentation(
			GeoCeDGSpatialRedefineFrontend.Presentation presentation) {
		getKernel().getAlgebraProcessor().setSpatialRedefineAssessmentHandler(
				new GeoCeDGSpatialRedefineFrontend(presentation));
	}

	@Override
	public byte[] getMacroFileAsByteArray() {
		// This host hook is consumed only by Save Settings. Never install all
		// document-local macros implicitly as application-wide startup tools.
		// Native document writing and explicit Tool Manager GGT export are separate.
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			getXMLio().writeMacroStream(output, new ArrayList<>(), new ArrayList<>());
			return output.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("Cannot create empty tool-preference snapshot",
					exception);
		}
	}

	@Override
	public void loadMacroFileFromByteArray(byte[] bytes, boolean removeOldMacros) {
		if (!removeOldMacros) {
			// Explicit host Tool Manager dependency loading is document-local.
			super.loadMacroFileFromByteArray(bytes, false);
		}
		// The true branch is exclusively inherited startup preference loading.
		// Explicit installation lives in the isolated GeoCeDG user-tool library.
	}
}

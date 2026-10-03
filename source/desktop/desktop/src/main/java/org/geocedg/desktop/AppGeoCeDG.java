/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.DocumentUnitSystem;
import org.geocedg.common.kernel.units.UnitDocumentOperations;
import org.geocedg.common.kernel.units.UnitMetadataException;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.desktop.export.ExportArea;
import org.geocedg.desktop.export.ExportAreaSession;
import org.geocedg.desktop.export.ExportAreaUnavailableException;
import org.geocedg.desktop.export.PictureExportRoute;
import org.geocedg.desktop.export.PictureExportService;
import org.geocedg.desktop.resources.GeoCeDGBrandingResource;
import org.geogebra.common.awt.GBufferedImage;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.MyImage;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.main.App;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.common.main.OptionType;
import org.geogebra.common.main.settings.FontSettings;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.geogebra.desktop.gui.inputbar.InputBarHelpPanelD;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.gui.util.ImageSelection;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolNavigationD;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.AppD.UpstreamExportEntry;
import org.geogebra.desktop.main.GlobalKeyDispatcherD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.CopyPasteD;
import org.geogebra.editor.share.util.KeyCodes;

/**
 * Desktop application instance bound to the GeoCeDG product profile.
 */
public final class AppGeoCeDG extends App3D {
	private GeoCeDGPresentationPreferences presentationPreferences;
	private GeoCeDGThemePreference themePreference;
	/**
	 * PRE-G9B-R6-plus-A-1 session layer state. Views created inside the host
	 * constructor may create it early (no field initializer), but it takes part
	 * in object creation only once the product constructor has finished.
	 */
	private GeoCeDGLayerWorkspace layerWorkspace;
	private boolean layerWorkspaceActive;
	private GeoCeDGStatusBar statusBar;
	private GeoCeDGWorkingLayerChooser layerChooser;
	/**
	 * PRE-G9B-R6-plus-B session export area and the single picture service; created
	 * lazily because views created inside the host constructor may paint first.
	 */
	private ExportAreaSession exportAreaSession;
	private PictureExportService pictureExportService;
	private GeoCeDGExportAreaPrompt exportAreaPrompt = GeoCeDGExportAreaPrompt.dialog();
	private Runnable dxfShortcutAction = () -> runProfileAction("export.dxf-2d");
	/**
	 * PRE-G9B-R6-plus-D1: whether the host constructor left a blank document. It is set
	 * inside the host constructor, so it has no field initializer.
	 */
	private boolean blankStartupDocument;
	private boolean documentUnitsActive;
	private boolean insideFileNew;
	private boolean clearedInFileNew;
	private GeoCeDGCopyPaste unitCopyPaste;
	private GeoCeDGDocumentUnitsPrompt documentUnitsPrompt = GeoCeDGDocumentUnitsPrompt.dialog();
	private Consumer<String> unitLoadErrorSink;

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
		initializeLayerWorkspace();
		initializePresentationTheme();
		initializePresentationPreferences();
		initializeDocumentUnits(true);
	}

	/**
	 * @param args command line arguments
	 * @param component parent component
	 */
	public AppGeoCeDG(CommandLineArguments args, Container component) {
		this(args, component, createConfig(args), true);
	}

	private AppGeoCeDG(CommandLineArguments args, Container component,
			AppConfigGeoCeDG config, boolean newDocumentDefaults) {
		super(args, component, config);
		bindFeatureService(config);
		initializeLayerWorkspace();
		initializePresentationTheme();
		initializePresentationPreferences();
		initializeDocumentUnits(newDocumentDefaults);
	}

	// ----------------------------------------------------- PRE-G9B-R6-plus-D1 units

	private void initializeDocumentUnits(boolean newDocumentDefaults) {
		getKernel().getConstruction().getUnitSystem().addListener(this::unitStateChanged);
		documentUnitsActive = true;
		if (newDocumentDefaults && blankStartupDocument) {
			applyNewDocumentUnitDefaults();
		}
	}

	@Override
	protected void recordStartupDocument(boolean blankDocument) {
		blankStartupDocument = blankDocument;
	}

	/** @return the shared unit-state owner of the document construction */
	DocumentUnitSystem getDocumentUnits() {
		return getKernel().getConstruction().getUnitSystem();
	}

	/**
	 * Applies the new-document defaults to a new blank document (section 10): no undo
	 * point, the document stays saved and the undo baseline includes them. With an
	 * unspecified construction default the state stays EMPTY and nothing else happens.
	 */
	void applyNewDocumentUnitDefaults() {
		if (!documentUnitsActive) {
			return;
		}
		UnitState defaults = new GeoCeDGUnitPreferences().newDocumentState();
		if (defaults.isEmpty() || !getDocumentUnits().replace(defaults)) {
			return;
		}
		if (getKernel().isUndoActive() && getKernel().getConstruction()
				.getUndoManager() instanceof UndoManagerD undo) {
			try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
				undo.commitUndoBaseline(baseline);
			} catch (IOException e) {
				Log.debug("unit defaults: undo baseline not retaken: " + e.getMessage());
			}
		}
		setSaved();
	}

	private void unitStateChanged() {
		if (statusBar == null) {
			return;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			statusBar.updateText();
		} else {
			SwingUtilities.invokeLater(statusBar::updateText);
		}
	}

	/** Opens the single Document Units dialog through its profile action. */
	void openDocumentUnits() {
		if (getGuiManager() instanceof GuiManagerGeoCeDG) {
			runProfileAction("document.units");
		} else {
			editDocumentUnits();
		}
	}

	/**
	 * The {@code document.units} action: one dialog, one validated combined operation,
	 * one undo point when the state changes (sections 4.3 and 7.1).
	 *
	 * @return whether the document state changed
	 */
	boolean editDocumentUnits() {
		UnitState current = getDocumentUnits().getState();
		GeoCeDGDocumentUnits.Request request = documentUnitsPrompt.ask(this,
				GeoCeDGDocumentUnits.fromState(current));
		if (request == null) {
			return false;
		}
		GeoCeDGDocumentUnits.Result result = GeoCeDGDocumentUnits.validate(request, current);
		if (!result.isValid()) {
			JOptionPane.showMessageDialog(getMainComponent(), layerText(result.errorKey),
					layerText("Units.Dialog.Title"), JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return UnitDocumentOperations.commit(this, result.state);
	}

	void setDocumentUnitsPrompt(GeoCeDGDocumentUnitsPrompt prompt) {
		documentUnitsPrompt = prompt;
	}

	@Override
	public CopyPasteD getCopyPaste() {
		if (unitCopyPaste == null) {
			unitCopyPaste = new GeoCeDGCopyPaste(this);
		}
		return unitCopyPaste;
	}

	/**
	 * After a successful paste from this window's buffer: a non-blocking notice when
	 * both documents are physical with different metre factors (section 11). The paste
	 * itself never depends on it.
	 *
	 * @param source provenance of the buffer, or {@code null} when unavailable
	 * @param target target application
	 */
	void unitPasteCompleted(UnitState source, App target) {
		GeoCeDGStatusBar bar = getStatusBar();
		bar.clearPasteNotice();
		UnitState targetState = target.getKernel().getConstruction().getUnitSystem()
				.getState();
		if (source == null || !source.isPhysical() || !targetState.isPhysical()
				|| UnitState.samePhysicalMeaning(source, targetState)) {
			return;
		}
		bar.showPasteNotice(layerText("Units.PasteNotice", describeConstructionUnit(source),
				describeConstructionUnit(targetState)));
	}

	private static String describeConstructionUnit(UnitState state) {
		UnitToken unit = state.effectiveConstructionUnit();
		return unit == UnitToken.USM ? state.symbolOf(unit) + " (1 usm = "
				+ state.getUsm().canonicalFactor() + " m)" : unit.token();
	}

	@Override
	protected void showDocumentLoadFailure(String fileName, Throwable failure) {
		UnitMetadataException rejection = UnitMetadataException.find(failure);
		if (rejection == null) {
			super.showDocumentLoadFailure(fileName, failure);
			return;
		}
		showUnitLoadError(unitLoadErrorText(rejection, fileName));
	}

	@Override
	protected void showXMLLoadFailure(Exception failure) {
		UnitMetadataException rejection = UnitMetadataException.find(failure);
		if (rejection == null) {
			super.showXMLLoadFailure(failure);
			return;
		}
		showUnitLoadError(unitLoadErrorText(rejection, layerText("Units.LoadError.Document")));
	}

	private void showUnitLoadError(String message) {
		if (unitLoadErrorSink != null) {
			unitLoadErrorSink.accept(message);
		} else {
			showErrorDialog(message);
		}
	}

	void setUnitLoadErrorSink(Consumer<String> sink) {
		unitLoadErrorSink = sink;
	}

	/**
	 * @param rejection unit-metadata rejection
	 * @param documentName file or document name
	 * @return the localized message naming the defect (DQ-D1-4)
	 */
	String unitLoadErrorText(UnitMetadataException rejection, String documentName) {
		return layerText("Units.LoadError." + rejection.getCode().name(), documentName);
	}

	@Override
	public OptionPanelD newProductNewDocumentUnitsPanel() {
		return new GeoCeDGNewDocumentUnitsPanel(this, new GeoCeDGUnitPreferences());
	}

	private void initializeLayerWorkspace() {
		GeoCeDGLayerWorkspace workspace = getLayerWorkspace();
		// A document opened by the host constructor initializes the session too.
		workspace.resetForOpenedDocument();
		layerChooser = GeoCeDGWorkingLayerChooser.dialog(this);
		workspace.addListener(() -> {
			// Presentation only: repaint the views, never mark the document changed.
			getKernel().notifyRepaint();
			if (statusBar != null) {
				statusBar.updateText();
			}
		});
		layerWorkspaceActive = true;
	}

	/** @return PRE-G9B-R6-plus-A-1 session layer workspace */
	GeoCeDGLayerWorkspace getLayerWorkspace() {
		if (layerWorkspace == null) {
			layerWorkspace = new GeoCeDGLayerWorkspace(getKernel());
		}
		return layerWorkspace;
	}

	@Override
	public int getLayerForNewObject(Construction construction, int upstreamLayer) {
		return layerWorkspaceActive
				? layerWorkspace.layerForNewObject(construction, upstreamLayer)
				: upstreamLayer;
	}

	@Override
	public boolean isLayerShown(int layer) {
		return layerWorkspace == null || layerWorkspace.isLayerShown(layer);
	}

	/** @return PRE-G9B-R6-plus-B session export-area authority */
	ExportAreaSession getExportAreaSession() {
		if (exportAreaSession == null) {
			exportAreaSession = new ExportAreaSession(getKernel());
			// presentation only: the overlay repaints, the document never changes
			exportAreaSession.addListener(() -> getKernel().notifyRepaint());
		}
		return exportAreaSession;
	}

	/** @return PRE-G9B-R6-plus-B single picture-export service */
	PictureExportService getPictureExportService() {
		if (pictureExportService == null) {
			pictureExportService = new PictureExportService(this, getExportAreaSession(),
					AppGeoCeDG::copyToSystemClipboard);
		}
		return pictureExportService;
	}

	@Override
	public PictureExportRoute getPictureExportRoute() {
		return getPictureExportService();
	}

	@Override
	public double getExportFrameWidth(EuclidianView view) {
		PictureExportService service = getPictureExportService();
		if (!service.handles(view)) {
			return super.getExportFrameWidth(view);
		}
		ExportArea area = service.resolve(view);
		return area == null ? 0 : area.pixelWidth(view.getXscale());
	}

	@Override
	public double getExportFrameHeight(EuclidianView view) {
		PictureExportService service = getPictureExportService();
		if (!service.handles(view)) {
			return super.getExportFrameHeight(view);
		}
		ExportArea area = service.resolve(view);
		return area == null ? 0 : area.pixelHeight(view.getYscale());
	}

	/** Animated GIF/WebM stay outside this generation (AQ-X4, AQ-X5, DQ-B1). */
	@Override
	public boolean isAnimatedExportAvailable() {
		return false;
	}

	/**
	 * Worksheet upload, Animated GIF, STL, Collada and Collada HTML are never
	 * offered, also in the v1 fallback (AQ-X3, DQ-B2).
	 */
	@Override
	public boolean isUpstreamExportEntryAvailable(UpstreamExportEntry entry) {
		return false;
	}

	/**
	 * The Save preview of a pending picture export is rendered by the picture
	 * service from the current export area and hidden layers; the native
	 * document Save keeps the host preview.
	 */
	@Override
	public MyImage getSavePreviewImage(FileExtensions extension, double maxX,
			double maxY) {
		EuclidianView view = getActiveEuclidianView();
		PictureExportService service = getPictureExportService();
		if (isPictureExtension(extension) && service.handles(view)) {
			GBufferedImage image = service.previewImage(view, maxX, maxY);
			return image == null ? null
					: new MyImageD(GBufferedImageD.getAwtBufferedImage(image));
		}
		return super.getSavePreviewImage(extension, maxX, maxY);
	}

	private static boolean isPictureExtension(FileExtensions extension) {
		return FileExtensions.PNG.equals(extension) || FileExtensions.PDF.equals(extension)
				|| FileExtensions.SVG.equals(extension) || FileExtensions.EMF.equals(extension);
	}

	/** The graphics clipboard is a consumer of the picture service (AQ-X4, DQ-B6). */
	@Override
	public void copyGraphicsViewToClipboard(EuclidianView copyView) {
		PictureExportService service = getPictureExportService();
		if (!service.handles(copyView)) {
			super.copyGraphicsViewToClipboard(copyView);
			return;
		}
		getSelectionManager().clearSelectedGeos(true, false);
		updateSelection(false);
		try {
			if (!service.copyToClipboard(copyView)) {
				showError(Errors.SaveFileFailed);
			}
		} catch (ExportAreaUnavailableException e) {
			showError(Errors.SaveFileFailed);
		}
	}

	private static void copyToSystemClipboard(BufferedImage image) {
		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new ImageSelection(image), null);
	}

	/** File action: defines and activates the MANUAL producer for Graphics 1. */
	void defineManualExportArea() {
		EuclidianView view = getEuclidianView1();
		ExportArea current = getExportAreaSession().resolve(view);
		ExportArea visible = ExportAreaSession.visibleViewportOf(view);
		double[] initial = current == null ? new double[] { -1, 1, -1, 1 }
				: GeoCeDGExportAreaPrompt.bounds(current);
		double[] chosen;
		try {
			chosen = exportAreaPrompt.ask(this, initial,
					visible == null ? initial : GeoCeDGExportAreaPrompt.bounds(visible));
		} catch (NumberFormatException e) {
			exportAreaMessage("ExportArea.Invalid");
			return;
		}
		if (chosen != null && !getExportAreaSession().defineManual(view.getViewID(),
				chosen[0], chosen[1], chosen[2], chosen[3])) {
			exportAreaMessage("ExportArea.Invalid");
		}
	}

	/** @return whether Export_1/Export_2 were activated explicitly */
	boolean useExportPointsArea() {
		return getExportAreaSession().useExportPoints(getEuclidianView1().getViewID());
	}

	void toggleExportAreaOverlay() {
		getExportAreaSession().setOverlayShown(!getExportAreaSession().isOverlayShown());
	}

	boolean isExportAreaOverlayShown() {
		return exportAreaSession != null && exportAreaSession.isOverlayShown();
	}

	void clearExportArea() {
		getExportAreaSession().clear();
	}

	void setExportAreaPrompt(GeoCeDGExportAreaPrompt prompt) {
		exportAreaPrompt = prompt;
	}

	void setDxfShortcutAction(Runnable action) {
		dxfShortcutAction = action;
	}

	private void exportAreaMessage(String key) {
		JOptionPane.showMessageDialog(getMainComponent(),
				GeoCeDGProfile.getText(key, getLocale().getLanguage()),
				GeoCeDGProfile.getText("ExportArea.Define.Title", getLocale().getLanguage()),
				JOptionPane.WARNING_MESSAGE);
	}

	private void runProfileAction(String actionId) {
		if (getGuiManager() instanceof GuiManagerGeoCeDG manager) {
			manager.getActionRegistry().get(actionId).actionPerformed(
					new ActionEvent(this, 0, actionId));
		}
	}

	/**
	 * Opens the bounded layer chooser and applies an explicit choice; a hidden
	 * layer named here is shown and becomes the working layer (AQ-L7).
	 *
	 * @return whether a layer was chosen
	 */
	boolean chooseWorkingLayer() {
		Integer chosen = layerChooser.choose(layerWorkspace.getWorkingLayer());
		if (chosen == null) {
			return false;
		}
		layerWorkspace.setWorkingLayer(chosen);
		return true;
	}

	void setWorkingLayerChooser(GeoCeDGWorkingLayerChooser chooser) {
		layerChooser = chooser;
	}

	/** @return the status bar, created once and reattached by every panel rebuild */
	GeoCeDGStatusBar getStatusBar() {
		if (statusBar == null) {
			statusBar = new GeoCeDGStatusBar(this);
		}
		return statusBar;
	}

	/**
	 * @param key GeoCeDG text key
	 * @param arguments values for %0, %1, ...
	 * @return product text in the current language
	 */
	String layerText(String key, String... arguments) {
		String text = GeoCeDGProfile.getText(key, getLocale().getLanguage());
		for (int i = 0; i < arguments.length; i++) {
			text = text.replace("%" + i, arguments[i]);
		}
		return text;
	}

	@Override
	public JPanel buildApplicationPanel() {
		JPanel panel = super.buildApplicationPanel();
		// The SOUTH slot of the returned panel is free in the host layout and is
		// not cleared by updateApplicationLayout, which only rebuilds side panels.
		if (isUsingFullGui()) {
			panel.add(getStatusBar(), BorderLayout.SOUTH);
		}
		return panel;
	}

	@Override
	public void fileNew() {
		insideFileNew = true;
		clearedInFileNew = false;
		try {
			super.fileNew();
		} finally {
			insideFileNew = false;
		}
		// after the host reapplied the preferences XML, itself a clearing load
		if (clearedInFileNew) {
			applyNewDocumentUnitDefaults();
		}
	}

	@Override
	public boolean clearConstruction() {
		boolean cleared = super.clearConstruction();
		if (cleared && layerWorkspaceActive) {
			layerWorkspace.resetForNewDocument();
			getExportAreaSession().resetForDocument();
		}
		if (cleared && documentUnitsActive) {
			getStatusBar().clearPasteNotice();
			if (insideFileNew) {
				clearedInFileNew = true;
			} else {
				// reset without a current file, openURL: a new blank document too
				applyNewDocumentUnitDefaults();
			}
		}
		return cleared;
	}

	@Override
	public boolean loadExistingFile(File file, boolean isMacroFile) {
		if (isMacroFile || !layerWorkspaceActive) {
			// Startup files load inside the host constructor, before the session
			// workspace exists; it then starts from that document below.
			return super.loadExistingFile(file, isMacroFile);
		}
		boolean loaded = layerWorkspace.runDocumentTransition(
				() -> super.loadExistingFile(file, false));
		if (loaded) {
			layerWorkspace.resetForOpenedDocument();
			getExportAreaSession().resetForDocument();
			getStatusBar().clearPasteNotice();
		}
		return loaded;
	}

	@Override
	public boolean loadXML(String xml) {
		if (!layerWorkspaceActive) {
			return super.loadXML(xml);
		}
		boolean loaded = layerWorkspace.runDocumentTransition(() -> super.loadXML(xml));
		if (loaded) {
			layerWorkspace.resetForOpenedDocument();
			getExportAreaSession().resetForDocument();
			getStatusBar().clearPasteNotice();
		}
		return loaded;
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
		// A hidden helper is replaced by the loaded file at once: no new-document defaults.
		return new AppGeoCeDG(new CommandLineArguments(null), new JPanel(),
				new AppConfigGeoCeDG(config.getRuntimeFeatureService()
						.isLocusV2CreationEnabled(), config.getRuntimeFeatureService()
								.isExtendedDxfEnabled()), false);
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
		if (statusBar != null) {
			statusBar.updateText();
		}
	}

	/**
	 * Input Help visibility stays user-controlled: choosing a tool never calls this. When
	 * the user shows Input Help while a SplineV2 tool is active, it selects the SplineV2
	 * syntax, whichever menu, contextual help or input-bar button asked. PRE-G9B-R5-B:
	 * the entry is selected by its command identity, the authority of the canonical tree.
	 */
	@Override
	public void setShowInputHelpPanel(boolean isVisible) {
		super.setShowInputHelpPanel(isVisible);
		if (isVisible && GeoCeDGSplineV2Authoring.handles(getMode())) {
			GuiManagerD gui = (GuiManagerD) getGuiManager();
			((InputBarHelpPanelD) gui.getInputHelpPanel()).focusCommand(
					Commands.SplineV2.name());
			((AlgebraInputD) gui.getAlgebraInput()).getTextField().setToolTipText(
					GeoCeDGProfile.getText("Workspace.SplineHelp", getLocale().getLanguage()));
		}
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
		setPresentationSize(GeoCeDGPresentationPreferences.Category.GENERAL_UI_FONT,
				size == -1 ? GeoCeDGPresentationPreferences.freshDefault(
						GeoCeDGPresentationPreferences.Category.GENERAL_UI_FONT) : size);
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
	public int getToolbarIconSize() {
		return presentationPreferences == null ? super.getToolbarIconSize()
				: presentationPreferences.get(
						GeoCeDGPresentationPreferences.Category.TOOLBAR_ICON);
	}

	@Override
	public OptionPanelD newProductPresentationOptionsPanel() {
		return presentationPreferences == null ? null
				: new GeoCeDGPresentationOptionsPanel(this);
	}

	@Override
	public boolean productOwnsPresentationSizing() {
		return presentationPreferences != null;
	}

	@Override
	public OptionPanelD newProductPresentationThemePanel() {
		return themePreference == null ? null : new GeoCeDGThemeOptionsPanel(this);
	}

	@Override
	public String getProductOptionTypeTitle(OptionType type) {
		if (type != OptionType.LAYOUT || themePreference == null) {
			return null;
		}
		return GeoCeDGProfile.getText("Presentation.LayoutTab", getLocale().getLanguage());
	}

	/**
	 * Supplies the application presentation background of a Graphics view.
	 *
	 * <p>The returned color is painted only. It never reaches
	 * {@code EuclidianSettings}, the document XML or the preferences XML, so selecting a
	 * theme cannot rewrite {@code bgColor} in a {@code .cedg} or {@code .ggb} file. A
	 * document whose background differs from the application default background role keeps
	 * its own explicitly chosen color.
	 */
	@Override
	public GColor getPresentationBackground(GColor documentBackground) {
		GeoCeDGPresentationTheme theme = getPresentationTheme();
		if (theme == null || theme.palette() == null) {
			return null;
		}
		return GColor.WHITE.equals(documentBackground) ? theme.palette().canvasColor() : null;
	}

	GeoCeDGPresentationTheme getPresentationTheme() {
		return themePreference == null ? GeoCeDGPresentationTheme.DEFAULT_THEME
				: themePreference.get();
	}

	void setPresentationTheme(GeoCeDGPresentationTheme theme) {
		themePreference.set(theme);
		refreshPresentationTheme();
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
						getGUIFontSize(), getGUIFontSize(), super.getToolbarIconSize(),
						getPlainFont().getSize(), getPlainFont().getSize(), getFontSize()));
		applyGeneralUIFontSize();
		reapplyToolbarIconSize();
		getEuclidianView1().updateFonts();
		if (getGuiManager() != null) {
			((GuiManagerD) getGuiManager()).updateFonts();
		}
	}

	private void initializePresentationTheme() {
		themePreference = new GeoCeDGThemePreference();
		GeoCeDGThemeInstaller.applyRoles(themePreference.get());
	}

	@Override
	protected void updateComponentTreeUI() {
		// The inherited startup path builds menus and toolbars before this instance exists,
		// so reassert the roles before the ordinary Desktop component-tree refresh.
		GeoCeDGThemeInstaller.applyRoles(getPresentationTheme());
		super.updateComponentTreeUI();
	}

	private void refreshPresentationTheme() {
		GeoCeDGThemeInstaller.install(getPresentationTheme());
		GuiManagerD manager = getGuiManager() == null ? null : (GuiManagerD) getGuiManager();
		if (manager != null) {
			// Rebuild the seams that explicitly paint a presentation role on creation.
			manager.updateToolbar();
			if (manager.isUsingConstructionProtocol()) {
				((ConstructionProtocolViewD) manager.getConstructionProtocolView()).initGUI();
			}
		}
		repaintPresentationViews();
	}

	private void repaintPresentationViews() {
		if (getEuclidianView1() != null) {
			getEuclidianView1().updateBackground();
		}
		if (hasEuclidianView2EitherShowingOrNot(1)) {
			getEuclidianView2(1).updateBackground();
		}
	}

	private void reapplyToolbarIconSize() {
		getImageManager().setMaxIconSize(getToolbarIconSize());
		if (getGuiManager() != null) {
			((GuiManagerD) getGuiManager()).updateToolbar();
		}
	}

	private void refreshPresentation(GeoCeDGPresentationPreferences.Category category) {
		GuiManagerD manager = getGuiManager() == null ? null
				: (GuiManagerD) getGuiManager();
		switch (category) {
		case GENERAL_UI_FONT:
			applyGeneralUIFontSize();
			if (manager != null) {
				manager.updateFonts();
			}
			break;
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

	private void applyGeneralUIFontSize() {
		int size = getPresentationSize(
				GeoCeDGPresentationPreferences.Category.GENERAL_UI_FONT);
		getFontSettingsUpdater().setGUIFontSizeAndUpdate(size);
		getFontManager().setFontSize(size);
		getImageManager().setMaxIconSize(getToolbarIconSize());
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

			/**
			 * PRE-G9B-R6-plus-B hidden export routes (AQ-X3): U opens the Picture
			 * surface and C copies through the picture service (both reach the same
			 * service through the host handling and the copy override); W and M are
			 * consumed without action; D runs only the GeoCeDG DXF action, so the
			 * host branch never writes selectionAllowed or slider fixing; B keeps
			 * the host Base64 document copy.
			 */
			@Override
			protected boolean handleCtrlKey(KeyCodes key, boolean isShiftDown,
					boolean fromSpreadsheet, boolean fromEuclidianView) {
				if (isShiftDown) {
					switch (key) {
					case W:
					case M:
						return true;
					case D:
						dxfShortcutAction.run();
						return true;
					default:
						break;
					}
				}
				return super.handleCtrlKey(key, isShiftDown, fromSpreadsheet,
						fromEuclidianView);
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

	/**
	 * Resolves the runtime feature policy for one GeoCeDG launch.
	 *
	 * <p>PRE-G9B-P1 promoted both surfaces to product defaults. The historical
	 * arguments remain accepted compatibility syntax and keep working as explicit
	 * diagnostic overrides in both directions, so {@code --enableLocusV2=false}
	 * still starts GeoCeDG without the Locus V2 creation surface. Neither argument
	 * changes durable identity, serialization or geometric meaning.
	 */
	private static AppConfigGeoCeDG createConfig(CommandLineArguments args) {
		return new AppConfigGeoCeDG(
				args == null ? AppConfigGeoCeDG.DEFAULT_LOCUS_V2_CREATION_ENABLED
						: args.getBooleanValue(RuntimeFeatureService.LOCUS_V2_ARGUMENT,
								AppConfigGeoCeDG.DEFAULT_LOCUS_V2_CREATION_ENABLED),
				args == null ? AppConfigGeoCeDG.DEFAULT_EXTENDED_DXF_ENABLED
						: args.getBooleanValue(RuntimeFeatureService.EXTENDED_DXF_ARGUMENT,
								AppConfigGeoCeDG.DEFAULT_EXTENDED_DXF_ENABLED));
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

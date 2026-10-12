/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.desktop.euclidianND.EuclidianViewInterfaceD;
import org.geogebra.desktop.geogebra3D.gui.GuiManager3D;
import org.geogebra.desktop.gui.ContextMenuChooseGeoD;
import org.geogebra.desktop.gui.app.FileExtensionFilter;
import org.geogebra.desktop.gui.dialog.DialogManagerD;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.gui.util.GeoGebraFileChooser;
import org.geogebra.desktop.gui.view.algebra.AlgebraControllerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.main.AppD;

/** GeoCeDG GUI manager preserving all inherited 3D/Desktop behavior. */
final class GuiManagerGeoCeDG extends GuiManager3D {
	private GeoCeDGActionRegistry actionRegistry;
	private GeoCeDGWorkspaceController workspaceController;
	private ArrayList<GeoElement> contextGeos;

	GuiManagerGeoCeDG(AppD app) {
		super(app);
	}

	@Override
	protected GeoGebraMenuBar newMenuBar() {
		if (GeoCeDGProfile.isLegacyFallback()) {
			return new GeoGebraMenuBar(getApp(),
					(org.geogebra.desktop.gui.layout.LayoutD) getLayout()) {
				private static final long serialVersionUID = 1L;

				@Override
				public void initMenubar() {
					super.initMenubar();
					JMenu diagnostic = new JMenu("GeoCeDG v1");
					diagnostic.setToolTipText(GeoCeDGProfile.getFallbackDiagnostic(
							getApp().getLocale().getLanguage()));
					diagnostic.getAccessibleContext().setAccessibleDescription(
							diagnostic.getToolTipText());
					diagnostic.setEnabled(false);
					add(diagnostic);
				}
			};
		}
		return new GeoCeDGMenuBar(getApp());
	}

	GeoCeDGActionRegistry getActionRegistry() {
		if (actionRegistry == null) {
			actionRegistry = new GeoCeDGActionRegistry(getApp());
		}
		return actionRegistry;
	}

	GeoCeDGWorkspaceController getWorkspaceController() {
		if (workspaceController == null) {
			workspaceController = new GeoCeDGWorkspaceController(getApp(), getActionRegistry());
		}
		return workspaceController;
	}

	@Override
	protected ToolbarContainer newToolbarContainer() {
		if (GeoCeDGProfile.isLegacyFallback()) {
			return super.newToolbarContainer();
		}
		return new GeoCeDGToolbarContainer(getApp(), getWorkspaceController());
	}

	@Override
	protected AlgebraViewD newAlgebraView(AlgebraControllerD controller) {
		return new GeoCeDGAlgebraView(controller, (AppGeoCeDG) getApp());
	}

	@Override
	public void showPopupMenu(ArrayList<GeoElement> geos, Component invoker, GPoint p) {
		contextGeos = geos;
		try {
			super.showPopupMenu(geos, invoker, p);
		} finally {
			contextGeos = null;
		}
	}

	@Override
	protected void decorateProductContextMenu(JPopupMenu menu) {
		decorateProductContextMenu(menu, contextGeos);
	}

	/**
	 * @param menu already constructed host popup
	 * @param geos the objects the popup was opened for, or null
	 */
	void decorateProductContextMenu(JPopupMenu menu, Collection<GeoElement> geos) {
		if (GeoCeDGProfile.isLegacyFallback()) {
			return;
		}
		JPopupMenu projection = getWorkspaceController().createContextMenu();
		JMenu product = new JMenu("GeoCeDG");
		while (projection.getComponentCount() > 0) {
			product.add(projection.getComponent(0));
		}
		menu.addSeparator();
		// POST-E2-P4: the explicit offset conversion of the semantic dimension whose
		// outputs the object context menu was opened for
		JMenuItem offset = new GeoCeDGDimensionOffsetConversion(getApp())
				.menuItem(geos);
		if (offset != null) {
			menu.add(offset);
		}
		menu.add(product);
	}

	/**
	 * POST-E2-P4 follow-up: a right click in the Graphics View always opens the inherited
	 * chooser menu, even for a single object, and that menu is not decorated. When the
	 * menu is opened for outputs of one native dimension it gains the same offset action
	 * as the object menu; every other context keeps the inherited chooser unchanged.
	 */
	@Override
	public void showPopupChooseGeo(ArrayList<GeoElement> selectedGeos,
			ArrayList<GeoElement> geos, EuclidianView view, GPoint p) {
		JMenuItem offset = chooseGeoOffsetItem(selectedGeos);
		if (offset == null) {
			super.showPopupChooseGeo(selectedGeos, geos, view, p);
			return;
		}
		getApp().getActiveEuclidianView().resetMode();
		Component invoker = ((EuclidianViewInterfaceD) view).getJPanel();
		Point screenPos = invoker.isShowing() ? invoker.getLocationOnScreen()
				: new Point(0, 0);
		screenPos.translate(p.x, p.y);
		JPopupMenu menu = new ContextMenuChooseGeoD(getApp(), view, selectedGeos, geos,
				screenPos, p).getWrappedPopup();
		menu.addSeparator();
		menu.add(offset);
		showContextMenu(menu, invoker, p);
	}

	/**
	 * @param selectedGeos objects the Graphics View menu is opened for
	 * @return the offset action of their native dimension, or null
	 */
	JMenuItem chooseGeoOffsetItem(Collection<GeoElement> selectedGeos) {
		if (GeoCeDGProfile.isLegacyFallback()) {
			return null;
		}
		return new GeoCeDGDimensionOffsetConversion(getApp()).menuItem(selectedGeos);
	}

	/**
	 * @param menu menu to show
	 * @param invoker Graphics View panel
	 * @param p position in the panel
	 */
	void showContextMenu(JPopupMenu menu, Component invoker, GPoint p) {
		menu.show(invoker, p.x, p.y);
	}

	@Override
	protected FileExtensions[] getDocumentOpenExtensions() {
		return GeoCeDGDocumentPolicy.documentOpenExtensions();
	}

	@Override
	protected String getDocumentOpenDescription() {
		return AppConfigGeoCeDG.APPLICATION_NAME + " " + getLocalization().getMenu("Files");
	}

	/**
	 * PRE-G9B-R6-plus-F3 GeoCeDG Open (A1, OFF-B, F-1): the inherited save prompt, then
	 * the shared chooser with exactly one filter, the document filter; Insert File and
	 * Apply Styles are separate File -> Insert actions and OFF is not offered. Each
	 * selection resolves by {@link GeoCeDGDocumentPolicy#resolveOpenName}; an OFF or
	 * unresolved name is refused with a message. Cancel does nothing. The v1 fallback
	 * keeps the inherited dialog.
	 */
	@Override
	public void openFile(File file) {
		if (GeoCeDGProfile.isLegacyFallback()) {
			super.openFile(file);
			return;
		}
		if (!(getApp().isSaved() || getApp().saveCurrentFile())) {
			return;
		}
		((DialogManagerD) getDialogManager()).initFileChooser();
		GeoGebraFileChooser chooser = ((DialogManagerD) getDialogManager()).getFileChooser();
		chooser.setMode(GeoGebraFileChooser.MODE_GEOGEBRA);
		chooser.setCurrentDirectory(getApp().getCurrentPath());
		chooser.setMultiSelectionEnabled(true);
		chooser.setSelectedFile(file != null ? file : getApp().getCurrentFile());
		chooser.resetChoosableFileFilters();
		FileExtensionFilter documents = new FileExtensionFilter();
		for (FileExtensions extension : getDocumentOpenExtensions()) {
			documents.addExtension(extension);
		}
		documents.addExtension(FileExtensions.GEOGEBRA_TOOL);
		documents.addExtension(FileExtensions.HTML);
		documents.addExtension(FileExtensions.HTM);
		documents.setDescription(getDocumentOpenDescription());
		chooser.addChoosableFileFilter(documents);
		chooser.setFileFilter(documents);
		File[] selected = null;
		try {
			if (chooser.showOpenDialog(getApp().getMainComponent())
					== JFileChooser.APPROVE_OPTION) {
				selected = chooser.getSelectedFiles();
				if (selected == null || selected.length == 0) {
					selected = new File[] {chooser.getSelectedFile()};
				}
			}
		} finally {
			chooser.setMultiSelectionEnabled(false);
		}
		openResolved(selected);
	}

	/**
	 * @param selected chooser selection, or null after cancellation
	 * @return the files passed to the inherited open (refusals shown)
	 */
	List<File> openResolved(File[] selected) {
		List<File> resolved = new ArrayList<>();
		if (selected == null) {
			return resolved;
		}
		for (File candidate : selected) {
			if (candidate == null) {
				continue;
			}
			File file = GeoCeDGDocumentPolicy.resolveOpenName(candidate);
			if (file == null) {
				refuseOpen(candidate);
			} else {
				resolved.add(file);
			}
		}
		if (!resolved.isEmpty()) {
			super.doOpenFiles(resolved.toArray(new File[0]), true);
		}
		return resolved;
	}

	private void refuseOpen(File file) {
		AppGeoCeDG app = (AppGeoCeDG) getApp();
		app.showInsertionMessage(GeoCeDGDocumentPolicy.isOff(file)
				? app.layerText("Open.Reject.Off", file.getName())
				: app.layerText("Open.Reject.NotFound", file.getName()));
	}

	/**
	 * Every caller (chooser, file drop, recent list) refuses OFF files in the GeoCeDG
	 * UI, and nothing opens over an isolated drawing (P7). The v1 fallback is inherited.
	 */
	@Override
	public synchronized void doOpenFiles(File[] files, boolean allowOpeningInThisInstance) {
		if (GeoCeDGProfile.isLegacyFallback() || files == null) {
			super.doOpenFiles(files, allowOpeningInThisInstance);
			return;
		}
		AppGeoCeDG app = (AppGeoCeDG) getApp();
		if (app.isInsertionIsolated()) {
			app.showInsertionMessage(app.layerText("Insert.Isolation.Refused"));
			return;
		}
		List<File> accepted = new ArrayList<>();
		for (File file : files) {
			if (GeoCeDGDocumentPolicy.isOff(file)) {
				refuseOpen(file);
			} else {
				accepted.add(file);
			}
		}
		if (!accepted.isEmpty()) {
			super.doOpenFiles(accepted.toArray(new File[0]), allowOpeningInThisInstance);
		}
	}

	@Override
	public boolean save() {
		if (((AppGeoCeDG) getApp()).isInsertionIsolated()) {
			((AppGeoCeDG) getApp()).showInsertionMessage(
					((AppGeoCeDG) getApp()).layerText("Insert.Isolation.Refused"));
			return false;
		}
		getApp().setWaitCursor();
		getDialogManager().closeAll();
		File currentFile = getApp().getCurrentFile();
		boolean success;
		if (GeoCeDGDocumentPolicy.requiresNativeSaveAs(currentFile)
				|| !currentFile.canWrite()) {
			success = saveAs();
		} else {
			success = getApp().saveGeoGebraFile(currentFile);
		}
		getApp().setDefaultCursor();
		return success;
	}

	@Override
	public boolean saveAs() {
		if (((AppGeoCeDG) getApp()).isInsertionIsolated()) {
			((AppGeoCeDG) getApp()).showInsertionMessage(
					((AppGeoCeDG) getApp()).layerText("Insert.Isolation.Refused"));
			return false;
		}
		getApp().needThumbnailFor3D();
		File file = showSaveDialog(FileExtensions.GEOCEDG,
				GeoCeDGDocumentPolicy.nativeSuggestion(getApp().getCurrentFile()),
				AppConfigGeoCeDG.APPLICATION_NAME + " "
						+ getLocalization().getMenu("Files"), true, false);
		return saveAsTo(file);
	}

	/**
	 * Completes a native Save As after target selection. Keeping this seam free
	 * of chooser UI lets the document-state transition be verified directly.
	 *
	 * @param file selected native target, or {@code null} after cancellation
	 * @return whether a complete native document was published
	 */
	boolean saveAsTo(File file) {
		if (file == null) {
			return false;
		}
		boolean success = getApp().saveGeoGebraFile(file);
		if (success) {
			getApp().setCurrentFile(file);
		}
		return success;
	}

	@Override
	public File showSaveDialog(FileExtensions extension, File selectedFile,
			String description, boolean promptOverwrite, boolean dirsOnly) {
		if (!FileExtensions.GEOCEDG.equals(extension)) {
			return super.showSaveDialog(extension, selectedFile, description,
					promptOverwrite, dirsOnly);
		}
		File suggestion = selectedFile;
		while (true) {
			File target = super.showSaveDialog(extension, suggestion, description,
					promptOverwrite, dirsOnly);
			if (target == null) {
				return target;
			}
			File normalized = GeoCeDGDocumentPolicy.normalizeNativeSuffix(target);
			if (!normalized.getName().equals(target.getName())) {
				suggestion = normalized;
				continue;
			}
			if (!GeoCeDGDocumentPolicy.hasConflictingSuffix(target)) {
				return target;
			}
			getApp().showError(Errors.InvalidInput, target.getName());
			suggestion = GeoCeDGDocumentPolicy.nativeSuggestion(target);
		}
	}
}

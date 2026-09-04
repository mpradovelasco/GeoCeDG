/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.main.MyError.Errors;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.desktop.geogebra3D.gui.GuiManager3D;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.main.AppD;

/** GeoCeDG GUI manager preserving all inherited 3D/Desktop behavior. */
final class GuiManagerGeoCeDG extends GuiManager3D {
	private GeoCeDGActionRegistry actionRegistry;
	private GeoCeDGWorkspaceController workspaceController;

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
	protected void decorateProductContextMenu(JPopupMenu menu) {
		if (GeoCeDGProfile.isLegacyFallback()) {
			return;
		}
		JPopupMenu projection = getWorkspaceController().createContextMenu();
		JMenu product = new JMenu("GeoCeDG");
		while (projection.getComponentCount() > 0) {
			product.add(projection.getComponent(0));
		}
		menu.addSeparator();
		menu.add(product);
	}

	@Override
	protected FileExtensions[] getDocumentOpenExtensions() {
		return GeoCeDGDocumentPolicy.documentOpenExtensions();
	}

	@Override
	protected String getDocumentOpenDescription() {
		return AppConfigGeoCeDG.APPLICATION_NAME + " " + getLocalization().getMenu("Files");
	}

	@Override
	public boolean save() {
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

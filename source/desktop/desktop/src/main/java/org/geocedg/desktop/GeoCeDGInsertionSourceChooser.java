/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;

import javax.swing.JFileChooser;

import org.geogebra.common.util.FileExtensions;
import org.geogebra.desktop.gui.app.FileExtensionFilter;
import org.geogebra.desktop.gui.dialog.DialogManagerD;
import org.geogebra.desktop.gui.util.GeoGebraFileChooser;

/**
 * PRE-G9B-R6-plus-F3 source chooser of Insert File and Apply Styles (X1): single
 * selection, one filter "GeoCeDG documents (.cedg, .ggb)", starting in the current
 * path, no save prompt. Injectable for tests.
 */
@FunctionalInterface
interface GeoCeDGInsertionSourceChooser {

	/**
	 * @param app document window
	 * @param titleKey product text key of the dialog title
	 * @return the selected file, or null after cancellation
	 */
	File choose(AppGeoCeDG app, String titleKey);

	/** @return the Swing chooser used in the product */
	static GeoCeDGInsertionSourceChooser swing() {
		return (app, titleKey) -> {
			DialogManagerD dialogs = (DialogManagerD) app.getDialogManager();
			dialogs.initFileChooser();
			GeoGebraFileChooser chooser = dialogs.getFileChooser();
			chooser.setMode(GeoGebraFileChooser.MODE_GEOGEBRA);
			chooser.setCurrentDirectory(app.getCurrentPath());
			chooser.setMultiSelectionEnabled(false);
			chooser.setSelectedFile(null);
			FileExtensionFilter filter = new FileExtensionFilter();
			for (FileExtensions extension : GeoCeDGDocumentPolicy.documentOpenExtensions()) {
				filter.addExtension(extension);
			}
			filter.setDescription(app.layerText("Insert.Chooser.Filter"));
			chooser.resetChoosableFileFilters();
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
			chooser.setDialogTitle(app.layerText(titleKey));
			try {
				return chooser.showOpenDialog(app.getMainComponent())
						== JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
			} finally {
				chooser.setDialogTitle(null);
			}
		};
	}
}

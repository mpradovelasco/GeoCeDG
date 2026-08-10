/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportService;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;

/** Desktop dialog and file-writing boundary for the shared export service. */
final class GeoCeDGDxfExportController {

	private static final String COMPLETE = "Complete labeled 2D construction";
	private static final String SELECTION = "Current selection";
	private final AppD app;
	private final GeometryExportService service;

	GeoCeDGDxfExportController(AppD app) {
		this(app, new GeometryExportService());
	}

	GeoCeDGDxfExportController(AppD app, GeometryExportService service) {
		this.app = app;
		this.service = service;
	}

	/** Run the explicit two-step selection and destination flow. */
	void showExportDialog() {
		Component parent = app.getMainComponent();
		JComboBox<String> mode = new JComboBox<>(new String[] {COMPLETE, SELECTION});
		JPanel panel = new JPanel();
		panel.add(new JLabel("Objects:"));
		panel.add(mode);
		int decision = JOptionPane.showConfirmDialog(parent, panel,
				"GeoCeDG DXF export", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (decision != JOptionPane.OK_OPTION) {
			return;
		}

		SelectionMode selectionMode = mode.getSelectedIndex() == 0
				? SelectionMode.COMPLETE_CONSTRUCTION
				: SelectionMode.CURRENT_SELECTION;
		Collection<GeoElement> source = source(selectionMode);
		if (source.isEmpty()) {
			showError("The selected export population is empty.");
			return;
		}

		GeometryExportModel model = service.createModel(source, selectionMode);
		if (model.getEntities().isEmpty()) {
			showDiagnostics(model, "No supported 2D entity can be exported.",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!model.getDiagnostics().isEmpty()) {
			int proceed = showDiagnostics(model,
					"Some objects are not exportable. Continue with supported entities?",
					JOptionPane.WARNING_MESSAGE);
			if (proceed != JOptionPane.OK_OPTION) {
				return;
			}
		}

		File target = chooseTarget(parent);
		if (target == null) {
			return;
		}
		try {
			Files.write(target.toPath(), service.exportDxf(model)
					.getBytes(StandardCharsets.US_ASCII));
			JOptionPane.showMessageDialog(parent,
					"DXF written: " + target.getAbsolutePath() + "\nEntities: "
							+ model.getEntities().size() + "\nSkipped: "
							+ model.getDiagnostics().size(),
					"GeoCeDG DXF export", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException | RuntimeException exception) {
			showError("DXF export failed: " + exception.getMessage());
		}
	}

	private Collection<GeoElement> source(SelectionMode selectionMode) {
		if (selectionMode == SelectionMode.CURRENT_SELECTION) {
			return new ArrayList<>(app.getSelectionManager().getSelectedGeos());
		}
		return new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
	}

	private File chooseTarget(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Export GeoCeDG 2D geometry as DXF");
		chooser.setFileFilter(new FileNameExtensionFilter("DXF drawing (*.dxf)",
				"dxf"));
		chooser.setSelectedFile(new File("geocedg-export.dxf"));
		if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File selected = chooser.getSelectedFile();
		if (!selected.getName().toLowerCase().endsWith(".dxf")) {
			selected = new File(selected.getParentFile(), selected.getName() + ".dxf");
		}
		if (selected.exists()) {
			int overwrite = JOptionPane.showConfirmDialog(parent,
					"Replace existing file?\n" + selected.getAbsolutePath(),
					"GeoCeDG DXF export", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (overwrite != JOptionPane.YES_OPTION) {
				return null;
			}
		}
		return selected;
	}

	private int showDiagnostics(GeometryExportModel model, String heading,
			int messageType) {
		List<String> lines = new ArrayList<>();
		for (Diagnostic diagnostic : model.getDiagnostics()) {
			lines.add(diagnostic.getSourceId() + " [" + diagnostic.getCode()
					+ "]: " + diagnostic.getMessage());
		}
		JTextArea details = new JTextArea(String.join("\n", lines), 9, 72);
		details.setEditable(false);
		details.setCaretPosition(0);
		Object[] content = {heading, new JScrollPane(details)};
		if (messageType == JOptionPane.ERROR_MESSAGE) {
			JOptionPane.showMessageDialog(app.getMainComponent(), content,
					"GeoCeDG DXF export", messageType);
			return JOptionPane.CANCEL_OPTION;
		}
		return JOptionPane.showConfirmDialog(app.getMainComponent(), content,
				"GeoCeDG DXF export", JOptionPane.OK_CANCEL_OPTION, messageType);
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(app.getMainComponent(), message,
				"GeoCeDG DXF export", JOptionPane.ERROR_MESSAGE);
	}
}

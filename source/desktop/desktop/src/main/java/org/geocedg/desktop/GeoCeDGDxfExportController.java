/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.geocedg.common.export.ApproximationEvidence.Guarantee;
import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.G9X1GeometryExportAdapter;
import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.desktop.export.DxfExportPreflightPresentation;
import org.geocedg.desktop.export.DxfExportPreflightPresentation.Destination;
import org.geocedg.desktop.export.DxfFidelityManifestWriter;
import org.geocedg.desktop.export.DxfPairedOutputWriter;
import org.geocedg.desktop.export.DxfPairedOutputWriter.CollisionPolicy;
import org.geocedg.desktop.export.DxfPreparedOutput;
import org.geocedg.desktop.export.DxfWriteException;
import org.geocedg.desktop.export.DxfWriteResult;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;

/** Desktop request, preflight, and safe-output boundary for DXF export. */
final class GeoCeDGDxfExportController {

	private static final String COMPLETE = "Complete labeled 2D construction";
	private static final String SELECTION = "Current selection";
	private static final String DEFAULT_TOLERANCE = "0.001";
	private final AppD app;
	private final GeometryExportService service;
	private final DxfFidelityManifestWriter manifestWriter;
	private final DxfPairedOutputWriter outputWriter;

	GeoCeDGDxfExportController(AppD app) {
		this(app, new GeometryExportService());
	}

	GeoCeDGDxfExportController(AppD app, GeometryExportService service) {
		this(app, service, null, null);
	}

	GeoCeDGDxfExportController(AppD app, GeometryExportService service,
			DxfFidelityManifestWriter manifestWriter,
			DxfPairedOutputWriter outputWriter) {
		this.app = app;
		this.service = service;
		this.manifestWriter = manifestWriter;
		this.outputWriter = outputWriter;
	}

	/** Run G5 by default and the explicit G9X1 flow only when opted in. */
	void showExportDialog() {
		if (!isExtendedDxfEnabled()) {
			showLegacyExportDialog();
			return;
		}
		showExtendedExportDialog();
	}

	private boolean isExtendedDxfEnabled() {
		return DxfExportPreflightPresentation.isExtendedDxfEnabled(
				app.getConfig());
	}

	private void showExtendedExportDialog() {
		Component parent = app.getMainComponent();
		RequestChoice choice = chooseRequest(parent);
		if (choice == null) {
			return;
		}
		List<GeoElement> sources = choice.sources;

		try {
			GeometryExportPreflight preflight = service.preflight(sources,
					choice.selectionMode, choice.request);
			DxfExportPreflightPresentation presentation =
					DxfExportPreflightPresentation.from(preflight);
			Destination destination = presentation.requestDestination(
					new SwingDestinationPort(parent));
			if (destination == null) {
				return;
			}

			DxfEncodingResult encoding = service.encode(preflight);
			DxfPreparedOutput output = manifestWriter().prepare(preflight, encoding);
			CollisionPolicy collisionPolicy = destination.isReplaceExisting()
					? CollisionPolicy.REPLACE_EXISTING
					: CollisionPolicy.FAIL_IF_EXISTS;
			DxfWriteResult result = outputWriter().write(destination.getDxfPath(),
					output, collisionPolicy);
			showSuccess(preflight, presentation, result);
		} catch (DxfWriteException exception) {
			showError("DXF publication failed at " + exception.getStage()
					+ "; rollback complete=" + exception.isRollbackComplete()
					+ ": " + exception.getMessage());
		} catch (RuntimeException exception) {
			showError("DXF export failed before publication: "
					+ exception.getMessage());
		}
	}

	private DxfFidelityManifestWriter manifestWriter() {
		return manifestWriter == null
				? new DxfFidelityManifestWriter() : manifestWriter;
	}

	private DxfPairedOutputWriter outputWriter() {
		return outputWriter == null ? new DxfPairedOutputWriter() : outputWriter;
	}

	private void showLegacyExportDialog() {
		Component parent = app.getMainComponent();
		JComboBox<String> mode = new JComboBox<>(
				new String[] {COMPLETE, SELECTION});
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
		Collection<GeoElement> sources = source(selectionMode);
		if (sources.isEmpty()) {
			showError("The selected export population is empty.");
			return;
		}

		GeometryExportModel model = service.createModel(sources, selectionMode);
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

		File target = chooseLegacyTarget(parent);
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

	private File chooseLegacyTarget(Component parent) {
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
			selected = new File(selected.getParentFile(),
					selected.getName() + ".dxf");
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

	private RequestChoice chooseRequest(Component parent) {
		JComboBox<String> mode = new JComboBox<>(
				new String[] {COMPLETE, SELECTION});
		JTextField tolerance = new JTextField(DEFAULT_TOLERANCE, 12);
		JCheckBox allowApproximation = new JCheckBox(
				"Approved typed curve approximation", true);
		JTextField domains = new JTextField("", 24);
		JTextField evaluations = new JTextField(Long.toString(
				GeometryExportRequest.DEFAULT_MAXIMUM_EVALUATIONS), 12);
		JTextField depth = new JTextField(Integer.toString(
				GeometryExportRequest.DEFAULT_MAXIMUM_DEPTH), 12);
		JTextField componentVertices = new JTextField(Integer.toString(
				GeometryExportRequest.DEFAULT_MAXIMUM_VERTICES_PER_COMPONENT), 12);
		JTextField totalVertices = new JTextField(Integer.toString(
				GeometryExportRequest.DEFAULT_MAXIMUM_TOTAL_VERTICES), 12);
		JCheckBox sidecar = new JCheckBox(
				"Request manifest even for all-exact output", false);

		JPanel panel = new JPanel(new GridLayout(0, 2, 8, 5));
		panel.add(new JLabel("Objects:"));
		panel.add(mode);
		panel.add(new JLabel("Model-coordinate tolerance:"));
		panel.add(tolerance);
		panel.add(new JLabel("Approximation:"));
		panel.add(allowApproximation);
		panel.add(new JLabel(
				"Closed domains (start:end or source@branch:start:end; ...):"));
		panel.add(domains);
		panel.add(new JLabel("Allowed evidence:"));
		panel.add(new JLabel("ESTIMATED_ERROR"));
		panel.add(new JLabel("Maximum evaluations:"));
		panel.add(evaluations);
		panel.add(new JLabel("Maximum dyadic depth:"));
		panel.add(depth);
		panel.add(new JLabel("Maximum vertices/component:"));
		panel.add(componentVertices);
		panel.add(new JLabel("Maximum total vertices:"));
		panel.add(totalVertices);
		panel.add(new JLabel("Coordinates / units:"));
		panel.add(new JLabel("Cartesian 2D world / UNITLESS"));
		panel.add(new JLabel("Partial output:"));
		panel.add(new JLabel("Disabled (strict complete request)"));
		panel.add(new JLabel("Sidecar:"));
		panel.add(sidecar);

		while (true) {
			int decision = JOptionPane.showConfirmDialog(parent, panel,
					"GeoCeDG extended DXF export", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (decision != JOptionPane.OK_OPTION) {
				return null;
			}
			try {
				SelectionMode selectionMode = mode.getSelectedIndex() == 0
						? SelectionMode.COMPLETE_CONSTRUCTION
						: SelectionMode.CURRENT_SELECTION;
				List<GeoElement> sources = source(selectionMode);
				if (sources.isEmpty()) {
					showError("The selected export population is empty.");
					continue;
				}
				GeometryExportRequest request = request(tolerance.getText(),
						allowApproximation.isSelected(), domains.getText(),
						evaluations.getText(), depth.getText(),
						componentVertices.getText(), totalVertices.getText(),
						sidecar.isSelected(), sources);
				return new RequestChoice(selectionMode, request, sources);
			} catch (IllegalArgumentException exception) {
				showError("Invalid DXF export request: " + exception.getMessage());
			}
		}
	}

	private static GeometryExportRequest request(String tolerance,
			boolean approximationAllowed, String domains, String evaluations,
			String depth, String componentVertices, String totalVertices,
			boolean sidecarRequested, List<GeoElement> sources) {
		GeometryExportRequest.Builder builder = GeometryExportRequest
				.builder(parsePositiveDouble(tolerance, "Tolerance"))
				.allowedGuarantees(EnumSet.of(Guarantee.ESTIMATED_ERROR))
				.maximumEvaluations(parsePositiveLong(evaluations,
						"Maximum evaluations"))
				.maximumDepth(parseNonNegativeInt(depth, "Maximum depth"))
				.maximumVerticesPerComponent(parsePositiveInt(componentVertices,
						"Maximum vertices per component"))
				.maximumTotalVertices(parsePositiveInt(totalVertices,
						"Maximum total vertices"))
				.allowApproximation(approximationAllowed)
				.allowPartialOutput(false)
				.requestSidecar(sidecarRequested);
		addDomains(builder, domains, sources);
		return builder.build();
	}

	private static void addDomains(GeometryExportRequest.Builder builder,
			String text, List<GeoElement> sources) {
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		String[] tokens = text.split(";", -1);
		for (int index = 0; index < tokens.length; index++) {
			String token = tokens[index].trim();
			int endSeparator = token.lastIndexOf(':');
			int startSeparator = token.lastIndexOf(':', endSeparator - 1);
			if (token.isEmpty() || endSeparator <= 0
					|| endSeparator == token.length() - 1) {
				throw new IllegalArgumentException(
						"Domains use start:end or source@branch:start:end");
			}
			String startText = startSeparator < 0
					? token.substring(0, endSeparator)
					: token.substring(startSeparator + 1, endSeparator);
			double start = parseFiniteDouble(startText, "Domain start");
			double end = parseFiniteDouble(token.substring(endSeparator + 1),
					"Domain end");
			String domainKey = "desktop-domain-" + index;
			if (startSeparator < 0) {
				builder.addDefaultSemanticDomain(new SemanticDomain(domainKey,
						start, end, true, true));
				continue;
			}
			String sourceAndBranch = token.substring(0, startSeparator).trim();
			int branchSeparator = sourceAndBranch.lastIndexOf('@');
			if (branchSeparator <= 0
					|| branchSeparator == sourceAndBranch.length() - 1) {
				throw new IllegalArgumentException(
						"Source domains require source@branch:start:end");
			}
			String sourceId = resolveSourceId(
					sourceAndBranch.substring(0, branchSeparator).trim(), sources);
			String branchKey = sourceAndBranch.substring(branchSeparator + 1)
					.trim();
			builder.addSourceSemanticDomain(sourceId, new SemanticDomain(branchKey,
					domainKey, start, end, true, true));
		}
	}

	private static String resolveSourceId(String reference,
			List<GeoElement> sources) {
		if (reference.isEmpty()) {
			throw new IllegalArgumentException("Domain source is required");
		}
		for (int index = 0; index < sources.size(); index++) {
			String sourceId = G9X1GeometryExportAdapter.requestSourceId(
					sources.get(index), index);
			if (reference.equals(sourceId)) {
				return sourceId;
			}
		}
		String labelMatch = null;
		for (int index = 0; index < sources.size(); index++) {
			GeoElement source = sources.get(index);
			if (reference.equals(source.getLabelSimple())) {
				if (labelMatch != null) {
					throw new IllegalArgumentException(
							"Domain source label is ambiguous: " + reference);
				}
				labelMatch = G9X1GeometryExportAdapter.requestSourceId(source, index);
			}
		}
		if (labelMatch == null) {
			throw new IllegalArgumentException(
					"Domain source is not in the selected population: " + reference);
		}
		return labelMatch;
	}

	private static double parsePositiveDouble(String value, String name) {
		double parsed = parseFiniteDouble(value, name);
		if (parsed <= 0) {
			throw new IllegalArgumentException(name + " must be positive");
		}
		return parsed;
	}

	private static double parseFiniteDouble(String value, String name) {
		try {
			double parsed = Double.parseDouble(value.trim());
			if (!Double.isFinite(parsed)) {
				throw new NumberFormatException();
			}
			return parsed;
		} catch (NumberFormatException | NullPointerException exception) {
			throw new IllegalArgumentException(name + " must be finite", exception);
		}
	}

	private static long parsePositiveLong(String value, String name) {
		try {
			long parsed = Long.parseLong(value.trim());
			if (parsed < 1) {
				throw new NumberFormatException();
			}
			return parsed;
		} catch (NumberFormatException | NullPointerException exception) {
			throw new IllegalArgumentException(name + " must be positive", exception);
		}
	}

	private static int parsePositiveInt(String value, String name) {
		int parsed = parseNonNegativeInt(value, name);
		if (parsed < 1) {
			throw new IllegalArgumentException(name + " must be positive");
		}
		return parsed;
	}

	private static int parseNonNegativeInt(String value, String name) {
		try {
			int parsed = Integer.parseInt(value.trim());
			if (parsed < 0) {
				throw new NumberFormatException();
			}
			return parsed;
		} catch (NumberFormatException | NullPointerException exception) {
			throw new IllegalArgumentException(
					name + " must be a non-negative integer", exception);
		}
	}

	private List<GeoElement> source(SelectionMode selectionMode) {
		if (selectionMode == SelectionMode.CURRENT_SELECTION) {
			return new ArrayList<>(app.getSelectionManager().getSelectedGeos());
		}
		return new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
	}

	private void showSuccess(GeometryExportPreflight preflight,
			DxfExportPreflightPresentation presentation, DxfWriteResult result) {
		StringBuilder message = new StringBuilder();
		message.append("DXF written: ").append(result.getDxfPath())
				.append("\nExact components: ")
				.append(preflight.getExactCount())
				.append("\nApproximate components: ")
				.append(preflight.getApproximateCount())
				.append("\nHidden sources: ").append(preflight.getHiddenCount())
				.append("\nCoordinates: unitless Cartesian 2D world")
				.append("\nDXF SHA-256: ").append(result.getDxfSha256());
		if (result.getManifestPath() != null) {
			message.append("\nFidelity sidecar: ")
					.append(result.getManifestPath());
		} else {
			message.append("\nFidelity sidecar: not required (all exact)");
		}
		if (preflight.getApproximateCount() > 0) {
			message.append('\n').append(
					presentation.getCompletionEvidenceText());
		}
		JOptionPane.showMessageDialog(app.getMainComponent(), message.toString(),
				"GeoCeDG DXF export", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(app.getMainComponent(), message,
				"GeoCeDG DXF export", JOptionPane.ERROR_MESSAGE);
	}

	private final class SwingDestinationPort
			implements DxfExportPreflightPresentation.DestinationPort {
		private final Component parent;

		private SwingDestinationPort(Component parent) {
			this.parent = parent;
		}

		@Override
		public boolean presentPreflight(
				DxfExportPreflightPresentation presentation) {
			String details = presentation.getSummaryText() + "\n\n"
					+ presentation.getApproximationEvidenceText() + "\n\n"
					+ presentation.getWarningsText();
			JTextArea report = new JTextArea(details, 22, 88);
			report.setEditable(false);
			report.setCaretPosition(0);
			Object[] content = {
					"Preflight completed before destination selection.",
					new JScrollPane(report)
			};
			if (!presentation.isWritable()) {
				JOptionPane.showMessageDialog(parent, content,
						"GeoCeDG DXF preflight rejected",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return JOptionPane.showConfirmDialog(parent, content,
					"GeoCeDG DXF preflight", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION;
		}

		@Override
		public boolean confirmApproximateExport(
				DxfExportPreflightPresentation presentation) {
			Object[] content = {
					"This DXF contains explicit approximate geometry.",
					"The approximation is export-only and does not alter CeDG geometry.",
					"A hash-bound fidelity sidecar is mandatory.",
					presentation.getApproximationEvidenceText(),
					"Continue to destination selection?"
			};
			return JOptionPane.showConfirmDialog(parent, content,
					"Confirm approximate DXF export", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
		}

		@Override
		public void reportStaleSource(
				DxfExportPreflightPresentation presentation) {
			showError("STALE_SOURCE_REVISION: the construction changed after "
					+ "preflight; choose the export request again.");
		}

		@Override
		public Destination chooseDestination(
				DxfExportPreflightPresentation presentation) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Export GeoCeDG 2D geometry as DXF");
			chooser.setFileFilter(new FileNameExtensionFilter(
					"DXF drawing (*.dxf)", "dxf"));
			chooser.setSelectedFile(new File("geocedg-export.dxf"));
			if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			File selected = chooser.getSelectedFile();
			if (!selected.getName().toLowerCase(java.util.Locale.ROOT)
					.endsWith(".dxf")) {
				selected = new File(selected.getParentFile(),
						selected.getName() + ".dxf");
			}
			Path dxfPath = selected.toPath().toAbsolutePath().normalize();
			Path manifestPath = DxfPairedOutputWriter.manifestPath(dxfPath);
			boolean collision = Files.exists(dxfPath) || Files.exists(manifestPath);
			if (!collision) {
				return new Destination(dxfPath, false);
			}
			String collisionText = "Replace occupied DXF/sidecar destinations?\n"
					+ dxfPath + "\n" + manifestPath;
			int overwrite = JOptionPane.showConfirmDialog(parent, collisionText,
					"GeoCeDG DXF export", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			return overwrite == JOptionPane.YES_OPTION
					? new Destination(dxfPath, true) : null;
		}
	}

	private static final class RequestChoice {
		private final SelectionMode selectionMode;
		private final GeometryExportRequest request;
		private final List<GeoElement> sources;

		private RequestChoice(SelectionMode selectionMode,
				GeometryExportRequest request, List<GeoElement> sources) {
			this.selectionMode = selectionMode;
			this.request = request;
			this.sources = new ArrayList<>(sources);
		}
	}
}

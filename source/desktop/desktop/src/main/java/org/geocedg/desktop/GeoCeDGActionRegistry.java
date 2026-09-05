/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.desktop.GeoCeDGProfile.ActionDefinition;
import org.geocedg.desktop.resources.GeoCeDGToolImageResource;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.main.OptionType;
import org.geogebra.common.main.settings.AlgebraStyle;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.gui.menubar.LoadFileListener;
import org.geogebra.desktop.gui.view.properties.PropertiesViewD;
import org.geogebra.desktop.main.AppD;

/** Host adapters for one declarative action catalog; never a geometry/identity authority. */
public final class GeoCeDGActionRegistry {

	/** Stable action ID carried by all Swing projections. */
	public static final String ACTION_ID = "geocedg.action.id";
	private static final Set<String> TARGETS = Set.of(
			"host.edit.undo", "host.edit.redo", "host.context.animation-toggle",
			"geocedg.result.inspect", "geocedg.result.markers.toggle",
			"geocedg.result.materialize-selected", "geocedg.result.materialize-multiple",
			"geocedg.result.materialize-all", "geocedg.result.auto-materialize-initial.toggle",
			"SplineV2", "cedg-dihedral-procedures", "host.view.axes-toggle",
			"host.view.grid-toggle", "geocedg.navigation.zoom-window",
			"host.view.standard-view", "host.view.show-all-objects", "host.document.new",
			"host.document.open", "host.document.open-recent", "host.document.save",
			"host.document.save-as", "host.document.print-preview", "host.document.close",
			"host.view.algebra-input", "host.tools.manage", "host.properties.scripting-tab",
			"cedg.laboratory.legacy", "geocedg.export.dxf.dialog", "host.help.input",
			"host.help.command-list", "geocedg.help.contextual-action", "geocedg.help.user-guide",
			"host.help.keyboard-shortcuts", "geocedg.help.about", "geocedg.classic",
			"geocedg.language.selector", "host.view.construction-protocol",
			"host.view.construction-protocol-navigation",
			"host.preference.global-properties",
			"geocedg.inspect.definition", "geocedg.semantic-curve.inspect-definition",
			"host.preference.algebra-style.VALUE", "host.preference.algebra-style.DESCRIPTION",
			"host.preference.algebra-style.DEFINITION");

	private final AppD app;
	private final Map<String, Action> actions = new LinkedHashMap<>();

	/** @param app GeoCeDG application, not Classic */
	public GeoCeDGActionRegistry(AppD app) {
		this.app = app;
		for (ActionDefinition definition : GeoCeDGProfile.getActions()) {
			if (definition.mode() == null && !TARGETS.contains(definition.target())) {
				throw new IllegalStateException("Unbound profile target " + definition.target());
			}
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent event) {
					invoke(definition.id(), event);
				}
			};
			action.putValue(ACTION_ID, definition.id());
			GeoCeDGToolImageResource artwork = GeoCeDGToolImageResource
					.forIconKey(definition.iconKey());
			if (artwork != null) {
				Image image = artwork.renderImage();
				if (image != null) {
					action.putValue(Action.SMALL_ICON,
							app.getImageManager().getResponsiveScaledIcon(image, 20));
					action.putValue(Action.LARGE_ICON_KEY,
							app.getImageManager().getResponsiveScaledIcon(image, 32));
				}
			}
			if ("geocedg.export.dxf.dialog".equals(definition.target())) {
				action.putValue(Action.ACCELERATOR_KEY, GeoCeDGMenuBar.DXF_ACTION_ACCELERATOR);
			}
			actions.put(definition.id(), action);
		}
		refresh();
	}

	/**
	 * @param id stable action ID
	 * @return shared Swing action
	 */
	public Action get(String id) {
		Action result = actions.get(id);
		if (result == null) {
			throw new IllegalArgumentException("Unknown profile action " + id);
		}
		return result;
	}

	/** @return exact stable catalog IDs */
	public Set<String> ids() {
		return Set.copyOf(actions.keySet());
	}

	/** Refresh names, availability and checked state from current host authority. */
	public void refresh() {
		for (ActionDefinition definition : GeoCeDGProfile.getActions()) {
			Action action = get(definition.id());
			String name = definition.mode() == null ? text(definition.textKey() + ".name")
					: app.getToolName(definition.mode());
			String reason = unavailableReason(definition);
			String shortHelp = definition.mode() == null
					? text(definition.textKey() + ".short_help")
					: app.getToolHelp(definition.mode());
			String longHelp = definition.mode() == null
					? text(definition.textKey() + ".long_help") : shortHelp;
			action.putValue(Action.NAME, name);
			action.putValue(Action.SHORT_DESCRIPTION,
					reason == null ? shortHelp : reason);
			action.putValue(Action.LONG_DESCRIPTION, longHelp);
			action.setEnabled(reason == null);
			action.putValue(Action.SELECTED_KEY, checked(definition.target()));
		}
	}

	/**
	 * @param id action
	 * @return localized reason or null when currently available
	 */
	public String unavailableReason(String id) {
		return unavailableReason(GeoCeDGProfile.getAction(id));
	}

	private String unavailableReason(ActionDefinition definition) {
		if ("gated-g9u2".equals(definition.availability())) {
			return text("GeoCeDG.Workspace.Unavailable.G9U2");
		}
		if (definition.features().contains(RuntimeFeatureService.LOCUS_V2_FEATURE_ID)
				&& !RuntimeFeatureService.mayCreateLocusV2(app.getKernel().getConstruction())) {
			return text("Action.Unavailable.Feature");
		}
		String target = definition.target();
		if (target.startsWith("geocedg.result.materialize-")
				&& !controller().hasEligibleIntersectionSolutions()) {
			return text("Action.Unavailable.Result");
		}
		if ("geocedg.result.materialize-multiple".equals(target)
				&& !controller().hasSelectedIntersectionSolutions(false)) {
			return text("Action.Unavailable.Selection");
		}
		if ((target.contains("inspect.definition") || target.contains("inspect-definition")
				|| "host.properties.scripting-tab".equals(target))
				&& app.getSelectionManager().getSelectedGeos().size() != 1) {
			return text("Action.Unavailable.Selection");
		}
		if ("host.context.animation-toggle".equals(target)
				&& app.getSelectionManager().getSelectedGeos().stream()
				.noneMatch(GeoElement::isAnimatable)) {
			return text("Action.Unavailable.Selection");
		}
		if ("host.document.open-recent".equals(target) && AppD.getFileListSize() == 0) {
			return text("Action.Unavailable.NoFiles");
		}
		return null;
	}

	/**
	 * @param id exact declared action
	 * @param event initiating frontend event
	 */
	public void invoke(String id, ActionEvent event) {
		ActionDefinition definition = GeoCeDGProfile.getAction(id);
		String reason = unavailableReason(definition);
		if (reason != null) {
			message(reason);
			return;
		}
		if (definition.mode() != null) {
			app.setActiveView(App.VIEW_EUCLIDIAN);
			app.setMode(definition.mode());
			return;
		}
		execute(definition.target(), event);
		refresh();
	}

	private void execute(String target, ActionEvent event) {
		GuiManagerD gui = (GuiManagerD) app.getGuiManager();
		switch (target) {
		case "host.edit.undo":
			gui.getUndoAction().actionPerformed(event);
			break;
		case "host.edit.redo":
			gui.getRedoAction().actionPerformed(event);
			break;
		case "host.context.animation-toggle":
			toggleAnimation();
			break;
		case "geocedg.result.inspect":
			controller().inspectRichResultSelection();
			break;
		case "geocedg.result.markers.toggle":
			controller().setIntersectionMarkersVisible(
				!controller().isIntersectionMarkersVisible());
			break;
		case "geocedg.result.materialize-selected":
			controller().materializeSelectedIntersectionSolution();
			break;
		case "geocedg.result.materialize-multiple":
			controller().materializeSelectedIntersectionSolutions();
			break;
		case "geocedg.result.materialize-all":
			controller().materializeAllEligibleIntersectionSolutions();
			break;
		case "geocedg.result.auto-materialize-initial.toggle":
			controller().setAutoMaterializeIntersectionSolutions(
					!controller().isAutoMaterializeIntersectionSolutions());
			break;
		case "SplineV2":
			focusSplineInput();
			break;
		case "cedg-dihedral-procedures":
			message(text("GeoCeDG.Workspace.Unavailable.G9U2"));
			break;
		case "host.view.axes-toggle":
			gui.getShowAxesAction().actionPerformed(event);
			break;
		case "host.view.grid-toggle":
			gui.getShowGridAction().actionPerformed(event);
			break;
		case "geocedg.navigation.zoom-window":
			controller().activateZoomWindow();
			break;
		case "host.view.standard-view":
			app.setStandardView();
			break;
		case "host.view.show-all-objects":
			app.setViewShowAllObjects();
			break;
		case "host.document.new":
			app.fileNew();
			break;
		case "host.document.open":
			gui.openFile();
			break;
		case "host.document.open-recent":
			openRecent(event);
			break;
		case "host.document.save":
			gui.save();
			break;
		case "host.document.save-as":
			gui.saveAs();
			break;
		case "host.document.print-preview":
			GeoGebraMenuBar.showPrintPreview(app);
			break;
		case "host.document.close":
			app.exit();
			break;
		case "host.view.algebra-input":
			app.setShowAlgebraInput(!app.showAlgebraInput(), true);
			break;
		case "host.tools.manage":
			GeoCeDGUserTools.showManager(app);
			break;
		case "host.properties.scripting-tab":
			app.getDialogManager()
					.showPropertiesDialog(app.getSelectionManager().getSelectedGeos());
			if (!((PropertiesViewD) gui.getPropertiesView()).showScriptingTab()) {
				message(text("Workspace.Scripting"));
			}
			break;
		case "cedg.laboratory.legacy":
			openDiagnostic(true);
			break;
		case "geocedg.export.dxf.dialog":
			new GeoCeDGDxfExportController(app).showExportDialog();
			break;
		case "host.help.input":
		case "host.help.command-list":
			app.setShowAlgebraInput(true, true);
			gui.getInputHelpPanel();
			app.setShowInputHelpPanel(true);
			break;
		case "geocedg.help.contextual-action":
			message(app.getToolName(app.getMode()) + "\n" + app.getToolHelp(app.getMode()));
			break;
		case "geocedg.help.user-guide":
			showUserGuide();
			break;
		case "host.help.keyboard-shortcuts":
			message(text("Workspace.Shortcuts"));
			break;
		case "geocedg.help.about":
			message(aboutText());
			break;
		case "geocedg.classic":
			openDiagnostic(false);
			break;
		case "geocedg.language.selector":
			chooseLanguage();
			break;
		case "host.view.construction-protocol":
			toggleView(App.VIEW_CONSTRUCTION_PROTOCOL);
			break;
		case "host.view.construction-protocol-navigation":
			app.toggleShowConstructionProtocolNavigation(App.VIEW_EUCLIDIAN);
			break;
		case "host.preference.global-properties":
			app.getDialogManager().showPropertiesDialog(OptionType.GLOBAL, null);
			break;
		case "geocedg.inspect.definition":
		case "geocedg.semantic-curve.inspect-definition":
			GeoCeDGDefinitionInspector.show(app,
					app.getSelectionManager().getSelectedGeos().get(0));
			break;
		case "host.preference.algebra-style.VALUE":
			setAlgebraStyle(AlgebraStyle.VALUE);
			break;
		case "host.preference.algebra-style.DESCRIPTION":
			setAlgebraStyle(AlgebraStyle.DESCRIPTION);
			break;
		case "host.preference.algebra-style.DEFINITION":
			setAlgebraStyle(AlgebraStyle.DEFINITION);
			break;
		default:
			throw new IllegalStateException("Unbound action target " + target);
		}
	}

	private Boolean checked(String target) {
		switch (target) {
		case "host.view.axes-toggle":
			return app.getEuclidianView1().getShowXaxis();
		case "host.view.grid-toggle":
			return app.getEuclidianView1().getShowGrid();
		case "host.view.algebra-input":
			return app.showAlgebraInput();
		case "host.view.construction-protocol":
			return app.getGuiManager().showView(App.VIEW_CONSTRUCTION_PROTOCOL);
		case "host.view.construction-protocol-navigation":
			return app.showConsProtNavigation(App.VIEW_EUCLIDIAN);
		case "geocedg.result.markers.toggle":
			return controller().isIntersectionMarkersVisible();
		case "geocedg.result.auto-materialize-initial.toggle":
			return controller().isAutoMaterializeIntersectionSolutions();
		case "host.preference.algebra-style.VALUE":
			return app.getSettings().getAlgebra().getStyle() == AlgebraStyle.VALUE;
		case "host.preference.algebra-style.DESCRIPTION":
			return app.getSettings().getAlgebra().getStyle() == AlgebraStyle.DESCRIPTION;
		case "host.preference.algebra-style.DEFINITION":
			return app.getSettings().getAlgebra().getStyle() == AlgebraStyle.DEFINITION;
		default:
			return null;
		}
	}

	private void toggleView(int id) {
		app.getGuiManager().setShowView(!app.getGuiManager().showView(id), id);
	}

	private void setAlgebraStyle(AlgebraStyle style) {
		app.getSettings().getAlgebra().setStyle(style);
	}

	private void toggleAnimation() {
		for (GeoElement geo : app.getSelectionManager().getSelectedGeos()) {
			if (geo.isAnimatable()) {
				geo.setAnimating(!geo.isAnimating());
			}
		}
		app.getKernel().getAnimationManager().startAnimation();
		app.storeUndoInfo();
	}

	private void focusSplineInput() {
		app.setShowAlgebraInput(true, true);
		AlgebraInputD input = (AlgebraInputD) ((GuiManagerD) app.getGuiManager())
				.getAlgebraInput();
		if (input.getTextField().getText().isBlank()) {
			input.getTextField().setText("SplineV2(");
		}
		input.getTextField().setToolTipText(text("Workspace.SplineHelp"));
		input.requestFocus();
		app.setShowInputHelpPanel(true);
	}

	private void chooseLanguage() {
		Object choice = JOptionPane.showInputDialog(app.getMainComponent(),
				text("Menu.Help"), "GeoCeDG", JOptionPane.PLAIN_MESSAGE, null,
				new String[] {"English", "Espa\u00f1ol"},
				"es".equals(app.getLocale().getLanguage()) ? "Espa\u00f1ol" : "English");
		if (choice != null) {
			app.setLocale("Espa\u00f1ol".equals(choice) ? new Locale("es") : Locale.ENGLISH);
		}
	}

	private void openRecent(ActionEvent event) {
		List<File> files = new ArrayList<>();
		for (int i = 0; i < AppD.getFileListSize(); i++) {
			File file = AppD.getFromFileList(i);
			if (file != null) {
				files.add(file);
			}
		}
		Object choice = JOptionPane.showInputDialog(app.getMainComponent(),
				text("Menu.File"), "GeoCeDG", JOptionPane.PLAIN_MESSAGE, null,
				files.toArray(), files.isEmpty() ? null : files.get(0));
		if (choice instanceof File) {
			new LoadFileListener(app, (File) choice).actionPerformed(event);
		}
	}

	private void openDiagnostic(boolean laboratory) {
		File resource = null;
		if (laboratory) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(text("Workspace.Laboratory"));
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(new FileNameExtensionFilter("GGB / GGT", "ggb", "ggt"));
			if (chooser.showOpenDialog(app.getMainComponent()) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			resource = chooser.getSelectedFile();
			if (JOptionPane.showConfirmDialog(app.getMainComponent(),
					text("Workspace.LaboratoryWarning"), text("Workspace.Laboratory"),
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
				return;
			}
		}
		try {
			Path preferences = GeoCeDG.getDefaultPreferencesFile().resolveSibling(
					laboratory ? "laboratory.properties" : "classic-diagnostic.properties");
			Files.createDirectories(preferences.getParent());
			Path java = Path.of(System.getProperty("java.home"), "bin", "javaw.exe");
			if (!Files.isRegularFile(java)) {
				java = Path.of(System.getProperty("java.home"), "bin", "java");
			}
			new ProcessBuilder(diagnosticCommand(java, preferences, resource)).start();
		} catch (IOException exception) {
			message(text("Action.Unavailable.Failed") + "\n" + exception.getMessage());
		}
	}

	static List<String> diagnosticCommand(Path java, Path preferences, File resource)
			throws IOException {
		List<String> arguments = new ArrayList<>(List.of(java.toString(), "-cp",
				System.getProperty("java.class.path"), "org.geogebra.desktop.GeoGebra3D",
				"--showSplash=false", "--settingsfile=" + preferences));
		if (resource != null) {
			String name = resource.getName().toLowerCase(Locale.ROOT);
			if (!resource.isFile() || !(name.endsWith(".ggb") || name.endsWith(".ggt"))) {
				throw new IOException("Diagnostic resource must be an existing GGB/GGT file");
			}
			arguments.add(resource.getAbsolutePath());
		}
		return List.copyOf(arguments);
	}

	private void showUserGuide() {
		try (InputStream stream = GeoCeDGActionRegistry.class.getResourceAsStream(
				"/org/geocedg/desktop/geocedg_construction_quick_guide.md")) {
			if (stream == null) {
				throw new IOException("Packaged GeoCeDG guide is missing");
			}
			message(text("Workspace.Guide") + "\n\n"
					+ new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException exception) {
			message(text("Action.Unavailable.Failed") + "\n" + exception.getMessage());
		}
	}

	private GeoCeDGEuclidianController controller() {
		return (GeoCeDGEuclidianController) app.getEuclidianView1().getEuclidianController();
	}

	String text(String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}

	String aboutText() {
		return GeoCeDGProductInfo.applicationTitle() + "\n\n"
				+ text("Workspace.About") + "\n\n"
				+ text("About.UpstreamBaseline") + ": "
				+ GeoGebraConstants.VERSION_STRING + "\n"
				+ text("About.Author") + "\n\n"
				+ text("About.Licenses");
	}

	private void message(String text) {
		JTextArea area = new JTextArea(text, 8, 54);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(app.getPlainFont());
		JOptionPane.showMessageDialog(app.getMainComponent(), new JScrollPane(area),
				GeoCeDGProductInfo.applicationTitle(), JOptionPane.INFORMATION_MESSAGE);
	}
}

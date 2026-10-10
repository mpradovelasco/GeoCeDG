/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.kernel.sheet.IsoASheet;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.desktop.export.DrawingScaleControl;
import org.geocedg.desktop.export.ExportScalePresentation;
import org.geocedg.desktop.export.LatexExportPanel;
import org.geocedg.desktop.export.LatexSemanticExporter;
import org.geogebra.common.export.pstricks.GeoGebraExport;

/**
 * PRE-G9B-R6-plus-C product scale presentation of the export dialogs of one
 * window (author decisions {@code DQ-C6}, {@code DQ-C8}). It reads the document
 * unit state and the window's session drawing scale; it never writes the
 * document.
 */
final class GeoCeDGExportScalePresentation implements ExportScalePresentation {

	private final AppGeoCeDG app;

	GeoCeDGExportScalePresentation(AppGeoCeDG app) {
		this.app = app;
	}

	private UnitState unitState() {
		return app.getKernel().getConstruction().getUnitSystem() == null
				? UnitState.EMPTY
				: app.getKernel().getConstruction().getUnitSystem().getState();
	}

	@Override
	public boolean isPhysical() {
		return unitState().isPhysical();
	}

	@Override
	public double centimetresPerUnit() {
		return app.getPhysicalExportScale();
	}

	@Override
	public JComponent createScaleControl(Runnable onChange) {
		DrawingScaleControl control = new DrawingScaleControl(app,
				app.layerText("ExportScale.Engineering"),
				app.layerText("ExportScale.Unit", constructionUnitText()),
				app.layerText("ExportScale.Invalid"), onChange);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.add(control, BorderLayout.CENTER);
		panel.add(new SheetNotice(app), BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * PRE-G9B-R6-plus-E3 transient notice of the linked ISO A sheet in the picture,
	 * print and LaTeX dialogs (DQ-E3-12): both coherence states as a short line whose
	 * tooltip holds the full message, and the explicit "Use sheet scale" action. It is
	 * hidden without a linked sheet and is never exported or stored.
	 */
	static final class SheetNotice extends JPanel {
		private static final long serialVersionUID = 1L;
		private final transient AppGeoCeDG app;
		private final JLabel line = new JLabel();
		private final JButton useScale;
		private final transient Runnable refresh = this::refresh;

		SheetNotice(AppGeoCeDG app) {
			super(new FlowLayout(FlowLayout.LEFT, 4, 0));
			this.app = app;
			setOpaque(false);
			setName("geocedg.exportScale.sheetNotice");
			useScale = new JButton(app.layerText("IsoA.Coherence.UseScale"));
			useScale.setName("geocedg.exportScale.useSheetScale");
			useScale.addActionListener(event -> app.useSheetScale());
			add(line);
			add(useScale);
			refresh();
		}

		@Override
		public void addNotify() {
			super.addNotify();
			app.addDrawingScaleListener(refresh);
			refresh();
		}

		@Override
		public void removeNotify() {
			app.removeDrawingScaleListener(refresh);
			super.removeNotify();
		}

		void refresh() {
			AlgoIsoABorder border = app.getLinkedIsoABorder();
			if (border == null) {
				setVisible(false);
				return;
			}
			IsoASheet.Coherence coherence = app.sheetCoherence(border);
			String name = border.getLabelText().isDefined()
					? border.getLabelText().getTextString() : "?";
			line.setText(app.layerText(coherence.getPhysical()
					== IsoASheet.PhysicalCoherence.COHERENT ? "IsoA.Notice.Coherent"
					: "IsoA.Notice.Check", name));
			line.setToolTipText(app.sheetCoherenceMessage(border, coherence));
			useScale.setVisible(coherence.getScale() == IsoASheet.ScaleCoherence.DIFFERENT);
			setVisible(true);
			revalidate();
		}
	}

	@Override
	public JComponent createDeviceModeStatement() {
		return deviceStatement(app.layerText("ExportScale.DeviceStatement"));
	}

	/**
	 * PRE-G9B-R6-plus-C (C-UX-1): a short visible note whose tooltip and
	 * accessible description carry the full explanation, so the statement never
	 * sets the width of an export dialog; the device controls keep their
	 * non-physical labels.
	 *
	 * @param explanation full non-physical statement
	 * @return compact statement component
	 */
	private JLabel deviceStatement(String explanation) {
		JLabel statement = new JLabel(app.layerText("ExportScale.DeviceShort"));
		statement.setToolTipText(explanation);
		statement.getAccessibleContext().setAccessibleDescription(explanation);
		statement.setName("geocedg.exportScale.deviceStatement");
		return statement;
	}

	@Override
	public String deviceScaleLabel(String hostKey, String hostLabel) {
		switch (hostKey) {
		case "ScaleInCentimeter":
			return app.layerText("ExportScale.DeviceCm") + ":";
		case "FixedSize":
			return app.layerText("ExportScale.DeviceFixed") + ":";
		case "SizeInPixels":
			return app.layerText("ExportScale.DevicePixels") + ":";
		default:
			return hostLabel;
		}
	}

	@Override
	public String printScaleTitle() {
		return app.layerText("ExportScale.PrintTitle",
				app.getDrawingScale().toString(), constructionUnitText());
	}

	@Override
	public JComponent createLatexPanel(GeoGebraExport exporter,
			Runnable onScaleChange) {
		JComponent scale;
		if (isPhysical()) {
			scale = createScaleControl(onScaleChange);
		} else {
			scale = deviceStatement(app.layerText("ExportScale.LatexDevice"));
		}
		return new LatexExportPanel(scale, app.layerText("LatexExport.Tolerance"),
				app.layerText("LatexExport.Report"),
				exporter instanceof LatexSemanticExporter
						? ((LatexSemanticExporter) exporter).getSemanticSupport()
						: null);
	}

	/** @return the construction unit as shown in the export dialogs */
	String constructionUnitText() {
		UnitState state = unitState();
		UnitToken unit = state.effectiveConstructionUnit();
		if (unit == null) {
			return app.layerText("Units.Unspecified");
		}
		if (unit == UnitToken.USM) {
			UsmDefinition usm = state.getUsm();
			return usm.displaySymbol() + " (usm, " + usm.canonicalFactor() + " m)";
		}
		return unit.token();
	}
}

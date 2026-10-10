/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.kernel.sheet.IsoASheet;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;

/**
 * PRE-G9B-R6-plus-A-1 complementary status bar in the free SOUTH slot of the
 * application panel. It is presentation state only; the kernel never reads it.
 *
 * <p>Segments are identified by a stable id so later subphases can add theirs.
 * A-1 delivers the working-layer segment; PRE-G9B-R6-plus-D1 adds the permanent
 * construction-unit and presentation-unit segments, derived from the shared document
 * unit state at refresh time, and the transient paste-notice segment. A click on a unit
 * segment opens the Document Units dialog; the bar edits nothing itself.
 * POST-E2-P1-R2 smoke follow-up C adds the permanent drawing-scale segment: the
 * window's session {@link AppGeoCeDG#getDrawingScale() drawing scale} a:b, or an
 * explicit non-physical statement when the document has no construction unit. It is
 * read at refresh time and is never a scale authority or control.
 */
final class GeoCeDGStatusBar extends JPanel {
	private static final long serialVersionUID = 1L;
	/** Stable id of the working-layer segment. */
	static final String LAYER_SEGMENT = "layer";
	/** Stable id of the construction-unit segment. */
	static final String CONSTRUCTION_UNIT_SEGMENT = "construction-unit";
	/** Stable id of the presentation-unit segment. */
	static final String PRESENTATION_UNIT_SEGMENT = "presentation-unit";
	/** Stable id of the session drawing-scale segment (POST-E2-P1-R2 follow-up C). */
	static final String DRAWING_SCALE_SEGMENT = "drawing-scale";
	/**
	 * Stable id of the PRE-G9B-R6-plus-E3 sheet-coherence segment: the linked ISO A
	 * sheet against the current unit and session scale, or a lost link. Derived and
	 * transient; a click is the explicit "Use sheet scale" action.
	 */
	static final String SHEET_SEGMENT = "sheet-coherence";
	/** Middle-dot separator inside the sheet segment. */
	private static final String SEPARATOR = " " + (char) 0xb7 + " ";
	/** Stable id of the transient unit-mismatch paste notice (DQ-D1-9). */
	static final String PASTE_NOTICE_SEGMENT = "paste-notice";
	/** Lifetime of the paste notice. */
	static final int PASTE_NOTICE_MILLIS = 10_000;

	/** Starts a one-shot expiry; returns its cancellation. Tests inject a manual one. */
	interface NoticeTimer {
		Runnable start(int delayMillis, Runnable expiry);
	}

	private final Map<String, JLabel> segments = new LinkedHashMap<>();
	private final transient AppGeoCeDG app;
	private final JLabel noticeSeparator;
	private final JLabel sheetSeparator;
	private transient NoticeTimer noticeTimer = GeoCeDGStatusBar::swingTimer;
	private transient Runnable cancelNotice;

	/**
	 * One row that never wraps (PRE-G9B-R6-plus-D1 remediation). A FlowLayout moves a
	 * segment that does not fit into a second row, but the SOUTH slot of the
	 * application panel is one row high, so a long paste notice was laid out below the
	 * visible bar. Here every segment keeps its preferred size on the single row; the
	 * last visible segment gets only the remaining width, so a long notice is elided
	 * (its tooltip holds the full text) instead of disappearing.
	 */
	static final class SingleRowLayout implements LayoutManager {
		private static final int HGAP = 8;
		private static final int VGAP = 1;

		@Override
		public void addLayoutComponent(String name, Component component) {
			// no constraints
		}

		@Override
		public void removeLayoutComponent(Component component) {
			// no constraints
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return size(parent, true);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return size(parent, false);
		}

		private static Dimension size(Container parent, boolean lastAtPreferredWidth) {
			Component last = lastVisible(parent);
			int width = HGAP;
			int height = 0;
			for (Component component : parent.getComponents()) {
				if (component.isVisible()) {
					Dimension preferred = component.getPreferredSize();
					width += (component == last && !lastAtPreferredWidth ? 0 : preferred.width)
							+ HGAP;
					height = Math.max(height, preferred.height);
				}
			}
			Insets insets = parent.getInsets();
			return new Dimension(width + insets.left + insets.right,
					height + 2 * VGAP + insets.top + insets.bottom);
		}

		private static Component lastVisible(Container parent) {
			Component last = null;
			for (Component component : parent.getComponents()) {
				if (component.isVisible()) {
					last = component;
				}
			}
			return last;
		}

		@Override
		public void layoutContainer(Container parent) {
			Insets insets = parent.getInsets();
			Component last = lastVisible(parent);
			int rowHeight = 0;
			for (Component component : parent.getComponents()) {
				if (component.isVisible()) {
					rowHeight = Math.max(rowHeight, component.getPreferredSize().height);
				}
			}
			int right = parent.getWidth() - insets.right - HGAP;
			int x = insets.left + HGAP;
			for (Component component : parent.getComponents()) {
				if (!component.isVisible()) {
					continue;
				}
				Dimension preferred = component.getPreferredSize();
				int width = component == last
						? Math.max(0, Math.min(preferred.width, right - x))
						: preferred.width;
				component.setBounds(x, insets.top + VGAP + (rowHeight - preferred.height) / 2,
						width, preferred.height);
				x += width + HGAP;
			}
		}
	}

	GeoCeDGStatusBar(AppGeoCeDG app) {
		super(new SingleRowLayout());
		this.app = app;
		setName("geocedg.status-bar");
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.controlShadow));
		JLabel layer = addSegment(LAYER_SEGMENT);
		layer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		layer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				app.chooseWorkingLayer();
			}
		});
		add(separator());
		unitSegment(CONSTRUCTION_UNIT_SEGMENT);
		add(separator());
		unitSegment(PRESENTATION_UNIT_SEGMENT);
		add(separator());
		addSegment(DRAWING_SCALE_SEGMENT);
		sheetSeparator = separator();
		sheetSeparator.setVisible(false);
		add(sheetSeparator);
		JLabel sheet = addSegment(SHEET_SEGMENT);
		sheet.setVisible(false);
		sheet.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		sheet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				app.useSheetScale();
			}
		});
		noticeSeparator = separator();
		noticeSeparator.setVisible(false);
		add(noticeSeparator);
		addSegment(PASTE_NOTICE_SEGMENT).setVisible(false);
		updateText();
	}

	private JLabel separator() {
		JLabel separator = new JLabel("|");
		separator.setName("geocedg.status-bar.separator");
		separator.setForeground(SystemColor.controlShadow);
		return separator;
	}

	private void unitSegment(String id) {
		JLabel label = addSegment(id);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				app.openDocumentUnits();
			}
		});
	}

	/**
	 * @param id stable segment id
	 * @return the new segment
	 */
	JLabel addSegment(String id) {
		if (segments.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate status segment " + id);
		}
		JLabel label = new JLabel();
		label.setName("geocedg.status-bar." + id);
		segments.put(id, label);
		add(label);
		return label;
	}

	/**
	 * @param id stable segment id
	 * @return segment or null
	 */
	JLabel getSegment(String id) {
		return segments.get(id);
	}

	/** Refreshes every permanent segment for the current state and locale. */
	void updateText() {
		JLabel layer = segments.get(LAYER_SEGMENT);
		layer.setText(app.layerText("Workspace.Layer.Status",
				Integer.toString(app.getLayerWorkspace().getWorkingLayer())));
		layer.setToolTipText(app.layerText("Workspace.Layer.StatusTooltip"));
		UnitState state = app.getKernel().getConstruction().getUnitSystem().getState();
		UnitToken construction = state.effectiveConstructionUnit();
		UnitToken presentation = state.effectivePresentationUnit();
		String usmLine = usmLine(state.getUsm());
		JLabel constructionLabel = segments.get(CONSTRUCTION_UNIT_SEGMENT);
		constructionLabel.setText(app.layerText("Units.Status.Construction",
				construction == null ? app.layerText("Units.Unspecified")
						: state.symbolOf(construction)));
		constructionLabel.setToolTipText(app.layerText("Units.Status.ConstructionTooltip")
				+ usmLine);
		JLabel presentationLabel = segments.get(PRESENTATION_UNIT_SEGMENT);
		presentationLabel.setText(app.layerText("Units.Status.Presentation",
				presentation == null ? app.layerText("Units.None")
						: state.symbolOf(presentation)));
		presentationLabel.setToolTipText(app.layerText(
				state.getPresentationSelection() != null
						? "Units.Status.PresentationTooltipExplicit"
						: "Units.Status.PresentationTooltipFollows") + usmLine);
		JLabel scaleLabel = segments.get(DRAWING_SCALE_SEGMENT);
		boolean physical = state.isPhysical();
		scaleLabel.setText(app.layerText("Units.Status.Scale", physical
				? app.getDrawingScale().toString()
				: app.layerText("Units.Status.ScaleNonPhysical")));
		scaleLabel.setToolTipText(app.layerText(physical ? "Units.Status.ScaleTooltip"
				: "Units.Status.ScaleTooltipNonPhysical"));
		updateSheetSegment();
	}

	/** PRE-G9B-R6-plus-E3: derived each refresh; never stored, exported or in undo. */
	private void updateSheetSegment() {
		JLabel sheet = segments.get(SHEET_SEGMENT);
		AlgoIsoABorder border = app.getLinkedIsoABorder();
		String text = null;
		if (border != null) {
			IsoASheet.Coherence coherence = app.sheetCoherence(border);
			String name = border.getLabelText().isDefined()
					? border.getLabelText().getTextString() : "?";
			StringBuilder line = new StringBuilder(app.layerText("IsoA.Status.Sheet", name));
			line.append(SEPARATOR);
			if (coherence.getPhysical() == IsoASheet.PhysicalCoherence.NOT_DETERMINABLE) {
				line.append(app.layerText("IsoA.Status.NotDeterminable"));
			} else {
				line.append(app.layerText("IsoA.Status.Page",
						AppGeoCeDG.millimetres(coherence.getEffectiveWidthMm()),
						AppGeoCeDG.millimetres(coherence.getEffectiveHeightMm())));
			}
			if (coherence.getScale() == IsoASheet.ScaleCoherence.DIFFERENT) {
				line.append(SEPARATOR).append(app.layerText("IsoA.Status.ScaleDifferent",
						app.getDrawingScale().toString(), AppGeoCeDG.sheetScale(border)));
			}
			text = line.toString();
			sheet.setToolTipText(app.sheetCoherenceMessage(border, coherence) + "\n"
					+ app.layerText("IsoA.Status.Tooltip"));
		} else if (app.getExportAreaSession().isLinkLost()) {
			text = app.layerText("IsoA.Status.LinkLost");
			sheet.setToolTipText(text);
		}
		sheet.setVisible(text != null);
		sheetSeparator.setVisible(text != null);
		sheet.setText(text == null ? "" : text);
	}

	private String usmLine(UsmDefinition usm) {
		if (usm == null) {
			return "";
		}
		return " " + app.layerText("Units.Status.UsmDefinition",
				usm.getName() == null ? usm.displaySymbol() : usm.getName(),
				usm.canonicalFactor());
	}

	/**
	 * Shows the transient paste notice, replacing an earlier one; it clears itself
	 * after {@link #PASTE_NOTICE_MILLIS}.
	 *
	 * @param text localized notice
	 */
	void showPasteNotice(String text) {
		clearPasteNotice();
		JLabel notice = segments.get(PASTE_NOTICE_SEGMENT);
		notice.setText(text);
		notice.setToolTipText(text);
		notice.setVisible(true);
		noticeSeparator.setVisible(true);
		Runnable[] cancel = new Runnable[1];
		cancel[0] = noticeTimer.start(PASTE_NOTICE_MILLIS, () -> {
			if (cancelNotice == cancel[0]) {
				clearPasteNotice();
			}
		});
		cancelNotice = cancel[0];
		revalidate();
		repaint();
	}

	/** Clears the paste notice, if any. */
	void clearPasteNotice() {
		if (cancelNotice != null) {
			Runnable cancel = cancelNotice;
			cancelNotice = null;
			cancel.run();
		}
		JLabel notice = segments.get(PASTE_NOTICE_SEGMENT);
		if (notice.isVisible() || !notice.getText().isEmpty()) {
			notice.setText("");
			notice.setToolTipText(null);
			notice.setVisible(false);
			noticeSeparator.setVisible(false);
			revalidate();
			repaint();
		}
	}

	void setNoticeTimer(NoticeTimer timer) {
		noticeTimer = timer;
	}

	private static Runnable swingTimer(int delayMillis, Runnable expiry) {
		Timer timer = new Timer(delayMillis, event -> expiry.run());
		timer.setRepeats(false);
		timer.start();
		return timer::stop;
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

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
 */
final class GeoCeDGStatusBar extends JPanel {
	private static final long serialVersionUID = 1L;
	/** Stable id of the working-layer segment. */
	static final String LAYER_SEGMENT = "layer";
	/** Stable id of the construction-unit segment. */
	static final String CONSTRUCTION_UNIT_SEGMENT = "construction-unit";
	/** Stable id of the presentation-unit segment. */
	static final String PRESENTATION_UNIT_SEGMENT = "presentation-unit";
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
	private transient NoticeTimer noticeTimer = GeoCeDGStatusBar::swingTimer;
	private transient Runnable cancelNotice;

	GeoCeDGStatusBar(AppGeoCeDG app) {
		super(new FlowLayout(FlowLayout.LEADING, 8, 1));
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

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

/**
 * PRE-G9B-R6-plus-A-1 complementary status bar in the free SOUTH slot of the
 * application panel. It is presentation state only; the kernel never reads it.
 *
 * <p>Segments are identified by a stable id so later subphases can add theirs
 * (D1 adds the unit segments). A-1 delivers only the working-layer segment.
 */
final class GeoCeDGStatusBar extends JPanel {
	private static final long serialVersionUID = 1L;
	/** Stable id of the working-layer segment. */
	static final String LAYER_SEGMENT = "layer";

	private final Map<String, JLabel> segments = new LinkedHashMap<>();
	private final transient AppGeoCeDG app;

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
		updateText();
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

	/** Refreshes the text of the A-1 segment for the current state and locale. */
	void updateText() {
		JLabel layer = segments.get(LAYER_SEGMENT);
		layer.setText(app.layerText("Workspace.Layer.Status",
				Integer.toString(app.getLayerWorkspace().getWorkingLayer())));
		layer.setToolTipText(app.layerText("Workspace.Layer.StatusTooltip"));
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * PRE-G9B-R6-plus-C engineering-scale control shared by the picture,
 * print-preview and LaTeX dialogs ({@code DQ-C6}): an editable {@code a:b} field
 * with the presets {@code 1:1}, {@code 1:2}, {@code 1:5}, {@code 1:10},
 * {@code 2:1} and {@code 5:1}. An accepted entry is shown in normal form and
 * becomes the window's session drawing scale; an invalid or overflowing entry
 * is refused explicitly and the previous scale is kept, never clamped.
 */
public final class DrawingScaleControl extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DrawingScaleHolder holder;
	private final JComboBox<String> scaleField;
	private final JLabel status;
	private final String invalidText;
	private final Runnable onChange;
	private final Runnable holderListener = this::showCurrent;
	private boolean updating;

	/**
	 * @param holder per-window drawing-scale owner
	 * @param caption control caption
	 * @param unitStatement construction-unit statement
	 * @param invalidText explicit refusal text
	 * @param onChange called after an accepted change
	 */
	public DrawingScaleControl(DrawingScaleHolder holder, String caption,
			String unitStatement, String invalidText, Runnable onChange) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.holder = holder;
		this.invalidText = invalidText;
		this.onChange = onChange;
		scaleField = new JComboBox<>();
		for (DrawingScale preset : DrawingScale.PRESETS) {
			scaleField.addItem(preset.toString());
		}
		scaleField.setEditable(true);
		scaleField.setName("geocedg.drawingScale");
		status = new JLabel(" ");
		status.setName("geocedg.drawingScale.status");
		add(new JLabel(caption));
		add(scaleField);
		add(new JLabel(unitStatement));
		add(status);
		showCurrent();
		scaleField.addActionListener(event -> {
			if (!updating) {
				Object item = scaleField.getEditor().getItem();
				commit(item == null ? "" : item.toString());
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();
		holder.addDrawingScaleListener(holderListener);
	}

	@Override
	public void removeNotify() {
		holder.removeDrawingScaleListener(holderListener);
		super.removeNotify();
	}

	/**
	 * Applies one user entry.
	 *
	 * @param text entry such as {@code 1:2}
	 * @return whether the entry was accepted
	 */
	public boolean commit(String text) {
		DrawingScale scale;
		try {
			scale = DrawingScale.parse(text);
		} catch (IllegalArgumentException refused) {
			status.setText(invalidText);
			showCurrent();
			return false;
		}
		status.setText(" ");
		holder.setDrawingScale(scale);
		showCurrent();
		if (onChange != null) {
			onChange.run();
		}
		return true;
	}

	/** @return text currently shown */
	public String getShownText() {
		Object item = scaleField.getEditor().getItem();
		return item == null ? "" : item.toString();
	}

	/** @return refusal text currently shown, blank when none */
	public String getStatusText() {
		return status.getText();
	}

	private void showCurrent() {
		updating = true;
		try {
			String current = holder.getDrawingScale().toString();
			scaleField.setSelectedItem(current);
			scaleField.getEditor().setItem(current);
		} finally {
			updating = false;
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.main.App;

/**
 * POST-E2-P1-R2 smoke follow-up B: a numeric line-thickness field beside the host
 * thickness slider of the object Properties. The slider stays the only writer of the
 * style: an accepted entry moves the slider, whose own listener applies the
 * thickness, and then stores one undo point. The field follows every slider change,
 * its range included (a polygon allows 0), without writing back. An entry that is not
 * an integer within the slider range is refused and the field shows the slider value
 * again; the style is never touched by a refused entry.
 */
public final class GeoCeDGLineThicknessField {
	private static final String COMMIT_ACTION = "geocedg.lineThickness.commit";

	private final JSlider slider;
	private final Runnable storeUndo;
	private final SpinnerNumberModel model;
	private final JSpinner spinner;
	private final JFormattedTextField text;
	private boolean following;

	/**
	 * @param app application
	 * @param slider host thickness slider
	 * @param storeUndo stores one undo point after an applied entry
	 * @return the field for the GeoCeDG product, null for Classic
	 */
	public static GeoCeDGLineThicknessField forProduct(App app, JSlider slider,
			Runnable storeUndo) {
		return app.getConfig() instanceof AppConfigGeoCeDG
				? new GeoCeDGLineThicknessField(slider, storeUndo) : null;
	}

	GeoCeDGLineThicknessField(JSlider slider, Runnable storeUndo) {
		this.slider = slider;
		this.storeUndo = storeUndo;
		model = new SpinnerNumberModel(slider.getValue(), slider.getMinimum(),
				slider.getMaximum(), 1);
		spinner = new JSpinner(model);
		spinner.setName("geocedg.properties.lineThickness");
		spinner.putClientProperty(GeoCeDGLineThicknessField.class, this);
		text = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
		text.setFormatterFactory(new DefaultFormatterFactory(new RangeFormatter()));
		text.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		text.setColumns(3);
		text.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), COMMIT_ACTION);
		text.getActionMap().put(COMMIT_ACTION, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				commitText(text.getText());
			}
		});
		spinner.setEnabled(slider.isEnabled());
		slider.getModel().addChangeListener(event -> follow());
		slider.addPropertyChangeListener("enabled",
				event -> spinner.setEnabled(slider.isEnabled()));
		model.addChangeListener(event -> apply());
	}

	/** Mirrors the slider value and range; never applies a style. */
	private void follow() {
		following = true;
		try {
			model.setMinimum(slider.getMinimum());
			model.setMaximum(slider.getMaximum());
			model.setValue(slider.getValue());
		} finally {
			following = false;
		}
	}

	/** An accepted user entry: the slider applies it, then one undo point. */
	private void apply() {
		if (following) {
			return;
		}
		int value = (Integer) model.getValue();
		if (value == slider.getValue()) {
			return;
		}
		slider.setValue(value);
		storeUndo.run();
	}

	/**
	 * Applies one typed entry.
	 *
	 * @param entry typed text
	 * @return whether the entry was accepted
	 */
	boolean commitText(String entry) {
		text.setText(entry);
		try {
			text.commitEdit();
			return true;
		} catch (ParseException refused) {
			text.setValue(model.getValue());
			return false;
		}
	}

	/** @return the spinner shown beside the slider */
	public JComponent getComponent() {
		return spinner;
	}

	/** @return the shown value */
	int getShownValue() {
		return (Integer) model.getValue();
	}

	/** @return the editor text */
	String getShownText() {
		return text.getText();
	}

	/**
	 * @param language product language
	 */
	public void setLabels(String language) {
		String label = GeoCeDGProfile.getText("Properties.LineThickness.Field", language);
		spinner.setToolTipText(label);
		text.setToolTipText(label);
		text.getAccessibleContext().setAccessibleName(label);
	}

	/**
	 * @param font plain font
	 */
	public void setFont(Font font) {
		spinner.setFont(font);
		text.setFont(font);
	}

	/** Integers within the current slider range only; anything else is refused. */
	private final class RangeFormatter extends JFormattedTextField.AbstractFormatter {
		private static final long serialVersionUID = 1L;

		@Override
		public Object stringToValue(String entry) throws ParseException {
			String trimmed = entry == null ? "" : entry.trim();
			if (!trimmed.matches("[0-9]{1,4}")) {
				throw new ParseException(String.valueOf(entry), 0);
			}
			int value = Integer.parseInt(trimmed);
			if (value < slider.getMinimum() || value > slider.getMaximum()) {
				throw new ParseException(trimmed, 0);
			}
			return value;
		}

		@Override
		public String valueToString(Object value) {
			return value == null ? "" : value.toString();
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.geogebra.common.euclidian.EuclidianViewInterfaceCommon;
import org.geogebra.desktop.euclidian.EuclidianStyleBarD;

import com.formdev.flatlaf.FlatClientProperties;

/**
 * PRE-G9B-R6-plus-F4-A (scope amendment 01): the Graphics style bar of GeoCeDG, with the
 * numeric line-thickness field of Object Properties beside the slider of its line-style
 * popup. Every {@code createButtons()} (construction, language and font changes) creates
 * a new line-style button; a fresh field bound to its slider is attached to each one. The
 * field is kept on its button, so the bar holds no state of its own.
 *
 * <p>The application shows its popups in heavy-weight windows, and the inherited popup of
 * this button is not focusable, so a field in it never receives the keyboard. Only this
 * popup is therefore shown light-weight, inside the window, without the drop shadow.
 * Should Swing still use a separate window (a popup that does not fit), the field is
 * disabled instead of looking editable.
 */
final class GeoCeDGEuclidianStyleBar extends EuclidianStyleBarD {
	private static final long serialVersionUID = 1L;

	/** Component name of the field in the line-style popup. */
	static final String FIELD_NAME = "geocedg.stylebar.lineThickness";
	/** Component name of the row that holds the slider and the field. */
	static final String ROW_NAME = "geocedg.stylebar.lineThicknessRow";

	/**
	 * @param view Graphics view
	 */
	GeoCeDGEuclidianStyleBar(EuclidianViewInterfaceCommon view) {
		super(view);
	}

	@Override
	protected void createButtons() {
		super.createButtons();
		JSlider slider = btnLineStyle.getMySlider();
		GeoCeDGLineThicknessField field = GeoCeDGLineThicknessField.forProduct(app, slider,
				app::storeUndoInfo);
		if (field == null || !(slider.getParent() instanceof JPopupMenu popup)) {
			return;
		}
		JComponent editor = field.getComponent();
		editor.setName(FIELD_NAME);
		field.setLabels(app.getLocale().getLanguage());
		field.setFont(app.getPlainFont());
		compact(editor, slider.getMaximum());

		// the inherited slider moves, unchanged, into a row with the field at its right
		final int index = popup.getComponentIndex(slider);
		popup.remove(slider);
		JPanel row = new JPanel(new GridBagLayout());
		row.setName(ROW_NAME);
		row.setOpaque(false);
		GridBagConstraints cell = new GridBagConstraints();
		cell.gridx = 0;
		cell.weightx = 1;
		cell.fill = GridBagConstraints.HORIZONTAL;
		row.add(slider, cell);
		cell = new GridBagConstraints();
		cell.gridx = 1;
		cell.anchor = GridBagConstraints.CENTER;
		cell.insets = new Insets(0, editor.getFontMetrics(editor.getFont()).charWidth(' '),
				0, 0);
		row.add(editor, cell);
		popup.insert(row, index);

		popup.setLightWeightPopupEnabled(true);
		popup.putClientProperty(FlatClientProperties.POPUP_DROP_SHADOW_PAINTED,
				Boolean.FALSE);
		popup.addPopupMenuListener(new FieldPopupListener(btnLineStyle, field, slider));
		btnLineStyle.putClientProperty(GeoCeDGLineThicknessField.class, field);
	}

	/** A field for the largest value of the slider, sized from its font. */
	private static void compact(JComponent editor, int maximum) {
		if (editor instanceof JSpinner spinner
				&& spinner.getEditor() instanceof JSpinner.DefaultEditor spinnerEditor) {
			JFormattedTextField text = spinnerEditor.getTextField();
			text.setColumns(Integer.toString(maximum).length());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnLineStyle && isFieldApplying()) {
			// an accepted entry: the field stores its single undo point afterwards
			processSource(btnLineStyle, selection.getGeos());
			return;
		}
		super.actionPerformed(event);
	}

	private boolean isFieldApplying() {
		return btnLineStyle.getClientProperty(GeoCeDGLineThicknessField.class)
				instanceof GeoCeDGLineThicknessField field && field.isApplying();
	}

	/**
	 * @param window window that shows the field, or null
	 * @return whether the field can take the keyboard there
	 */
	static boolean isKeyboardReachable(Window window) {
		return window != null && window.getFocusableWindowState();
	}

	/**
	 * @param popupWindow separate window that shows the popup
	 * @return whether another visible window of the application overlaps it, which
	 *         keeps Swing from showing the popup light-weight
	 */
	static boolean isCoveredByAnotherWindow(Window popupWindow) {
		Window owner = popupWindow.getOwner();
		if (owner == null) {
			return false;
		}
		for (Window other : owner.getOwnedWindows()) {
			if (other != popupWindow && other.isVisible()
					&& other.getBounds().intersects(popupWindow.getBounds())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Opening: the button's tooltip, a window of the application over the popup that
	 * would keep it from being light-weight, is dismissed as a press on the button
	 * dismisses it, only earlier. The look-and-feel hides a tooltip window one event
	 * later; a popup that was therefore shown in a separate window is shown once more in
	 * place when nothing covers it any longer. The field is enabled only where the
	 * keyboard reaches it. Closing ends the edit as focus loss ends it in Object
	 * Properties (an integer within the range is applied, anything else is restored),
	 * before the click that closed the popup can change the selection the entry was
	 * meant for.
	 */
	private static final class FieldPopupListener implements PopupMenuListener {
		private final JComponent button;
		private final GeoCeDGLineThicknessField field;
		private final JSlider slider;
		private boolean shownAgain;

		FieldPopupListener(JComponent button, GeoCeDGLineThicknessField field,
				JSlider slider) {
			this.button = button;
			this.field = field;
			this.slider = slider;
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
			ToolTipManager.sharedInstance().mousePressed(new MouseEvent(button,
					MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 0, 0, 1, false));
			// runs after the popup has been placed in its window
			JPopupMenu popup = (JPopupMenu) event.getSource();
			SwingUtilities.invokeLater(() -> placed(popup));
		}

		private void placed(JPopupMenu popup) {
			JComponent editor = field.getComponent();
			Window window = SwingUtilities.getWindowAncestor(editor);
			boolean reachable = isKeyboardReachable(window);
			if (!reachable && !shownAgain && popup.isVisible() && window != null
					&& !isCoveredByAnotherWindow(window) && popup.getInvoker() != null
					&& popup.getInvoker().isShowing()) {
				shownAgain = true;
				Component invoker = popup.getInvoker();
				Point at = popup.getLocationOnScreen();
				SwingUtilities.convertPointFromScreen(at, invoker);
				popup.setVisible(false);
				popup.show(invoker, at.x, at.y);
				return;
			}
			shownAgain = false;
			editor.setEnabled(slider.isEnabled() && reachable);
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
			field.commitText(field.getShownText());
			field.getComponent().setEnabled(slider.isEnabled());
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent event) {
			// popupMenuWillBecomeInvisible follows
		}
	}
}

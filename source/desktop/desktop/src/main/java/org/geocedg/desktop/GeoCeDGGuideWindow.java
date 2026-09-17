/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Dedicated read-only window for the packaged GeoCeDG user guide.
 *
 * <p>The window is a non-modal, freely resizable dialog so that the guide can be
 * consulted while a construction is being built. It reuses the Swing HTML
 * infrastructure already present in the Desktop module rather than adding any
 * dependency, and it never navigates a link or opens an external browser.
 *
 * <p>This is presentation only: showing, resizing or closing the guide creates
 * no object, no undo step and no preference, and changes no geometry,
 * dependency graph, serialization or semantic identity.
 */
final class GeoCeDGGuideWindow {

	/** Reading width in pixels before screen clamping; not content derived. */
	static final int PREFERRED_WIDTH = 820;
	/** Reading height in pixels before screen clamping; not content derived. */
	static final int PREFERRED_HEIGHT = 720;
	/** Smallest usable reading area. */
	static final int MINIMUM_WIDTH = 480;
	/** Smallest usable reading area. */
	static final int MINIMUM_HEIGHT = 320;

	private final Component owner;
	private final Font font;
	private JDialog dialog;
	private JEditorPane view;

	/**
	 * @param owner component the guide is shown relative to
	 * @param font presentation font the guide typography follows
	 */
	GeoCeDGGuideWindow(Component owner, Font font) {
		this.owner = owner;
		this.font = font;
	}

	/**
	 * Computes the default reading size. It depends only on the available screen
	 * area, never on the rendered content, so a wide table or a long line can
	 * never stretch the window.
	 *
	 * @param screen available screen bounds
	 * @return default window size, clamped to that screen
	 */
	static Dimension defaultSize(Rectangle screen) {
		int width = Math.max(MINIMUM_WIDTH,
				Math.min(PREFERRED_WIDTH, screen.width - 80));
		int height = Math.max(MINIMUM_HEIGHT,
				Math.min(PREFERRED_HEIGHT, screen.height - 80));
		return new Dimension(width, height);
	}

	/**
	 * Builds the read-only HTML view used by the guide window.
	 *
	 * @param font presentation font the typography follows
	 * @return configured, non-editable editor pane
	 */
	static JEditorPane createView(Font font) {
		JEditorPane pane = new JEditorPane();
		HTMLEditorKit kit = new HTMLEditorKit();
		pane.setEditorKit(kit);
		applyStyle(kit, font);
		pane.setEditable(false);
		// A read-only pane still allows selection and copying.
		pane.setContentType("text/html");
		return pane;
	}

	private static void applyStyle(HTMLEditorKit kit, Font font) {
		String family = font == null ? "SansSerif" : font.getFamily();
		int size = font == null ? 12 : font.getSize();
		StyleSheet sheet = kit.getStyleSheet();
		sheet.addRule("body { font-family: \"" + family + "\", sans-serif;"
				+ " font-size: " + size + "pt; margin: 12px; }");
		sheet.addRule("h1 { font-size: " + (size + 8) + "pt; margin-top: 4px;"
				+ " margin-bottom: 8px; }");
		sheet.addRule("h2 { font-size: " + (size + 5) + "pt; margin-top: 16px;"
				+ " margin-bottom: 6px; }");
		sheet.addRule("h3 { font-size: " + (size + 2) + "pt; margin-top: 12px;"
				+ " margin-bottom: 4px; }");
		sheet.addRule("h4, h5, h6 { font-size: " + (size + 1) + "pt;"
				+ " margin-top: 10px; margin-bottom: 4px; }");
		sheet.addRule("p { margin-top: 4px; margin-bottom: 8px; }");
		sheet.addRule("li { margin-top: 2px; margin-bottom: 2px; }");
		sheet.addRule("code { font-family: Monospaced; }");
		sheet.addRule("pre { font-family: Monospaced; background-color: #F2F2F2;"
				+ " margin-top: 6px; margin-bottom: 10px;"
				+ " padding-top: 6px; padding-bottom: 6px;"
				+ " padding-left: 8px; padding-right: 8px; }");
		sheet.addRule("table { border-width: 1px; border-style: solid;"
				+ " border-color: #B0B0B0; margin-top: 6px; margin-bottom: 10px; }");
		sheet.addRule("th { text-align: left; border-width: 1px; border-style: solid;"
				+ " border-color: #B0B0B0; padding-top: 3px; padding-bottom: 3px;"
				+ " padding-left: 6px; padding-right: 6px; }");
		sheet.addRule("td { text-align: left; border-width: 1px; border-style: solid;"
				+ " border-color: #B0B0B0; padding-top: 3px; padding-bottom: 3px;"
				+ " padding-left: 6px; padding-right: 6px; }");
		sheet.addRule("hr { border-width: 1px; border-color: #D0D0D0; }");
	}

	/**
	 * Shows the guide, replacing any content shown earlier. Reopening after a
	 * product-language change therefore presents the other edition in the same
	 * window instead of a second one.
	 *
	 * @param html rendered guide document
	 * @param title window title
	 */
	void show(String html, String title) {
		if (dialog == null) {
			build();
		}
		dialog.setTitle(title);
		view.setText(html);
		view.setCaretPosition(0);
		if (!dialog.isVisible()) {
			dialog.setLocationRelativeTo(owner);
			dialog.setVisible(true);
		}
		dialog.toFront();
		SwingUtilities.invokeLater(() -> view.setCaretPosition(0));
	}

	private void build() {
		view = createView(font);
		JScrollPane scroll = new JScrollPane(view);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// A wide reference table scrolls; it never widens the window.
		scroll.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		Window ancestor = owner == null ? null : SwingUtilities.getWindowAncestor(owner);
		dialog = new JDialog(ancestor, GeoCeDGProductInfo.applicationTitle(),
				JDialog.ModalityType.MODELESS);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setResizable(true);
		dialog.setContentPane(scroll);
		Dimension size = defaultSize(screenBounds());
		// The size contract is applied to the window, not derived from the view.
		scroll.setPreferredSize(size);
		dialog.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
		dialog.pack();
		dialog.setSize(size);
	}

	private static Rectangle screenBounds() {
		if (GraphicsEnvironment.isHeadless()) {
			return new Rectangle(0, 0, PREFERRED_WIDTH + 80, PREFERRED_HEIGHT + 80);
		}
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();
	}

	/** @return the live dialog, or {@code null} before the guide is first shown */
	JDialog getDialog() {
		return dialog;
	}

	/** @return the live view, or {@code null} before the guide is first shown */
	JEditorPane getView() {
		return view;
	}
}

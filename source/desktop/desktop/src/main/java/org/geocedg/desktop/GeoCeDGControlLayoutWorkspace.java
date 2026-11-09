/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geocedg.common.document.controls.ControlLayout.Slot;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.euclidian.draw.DrawInputBox;
import org.geogebra.common.euclidian.draw.DrawSlider;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoButton;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.EventType;

/** Graphics 1 manager and uncommitted gesture state for CU8 presentation. */
final class GeoCeDGControlLayoutWorkspace {
	private static final int HEADER = 24;
	private static final int HANDLE = 12;
	private static final int MARGIN = 8;
	private static final GColor OUTLINE = GColor.newColor(54, 91, 123);
	private static final Comparator<GeoElement> MEMBER_ORDER = Comparator
			.comparingInt(GeoElement::getLayer).thenComparingInt(GeoElement::getConstructionIndex);

	private final AppGeoCeDG app;
	private final GeoCeDGEuclidianView view;
	private JDialog dialog;
	private JComboBox<Panel> panels;
	private JComboBox<Slot> members;
	private JLabel target;
	private GeoElement candidate;
	private Gesture gesture;
	private Panel previewPanel;
	private Slot previewSlot;
	private GeoCeDGControlLayoutStyleDialog styleEditor;
	private Panel stylePreviewPanel;
	private boolean ownsPointer;

	private record Transform(double xZero, double yZero, double xScale, double yScale,
			int left, int top, int width, int height, boolean logX, boolean logY,
			double xmin, double xmax, double ymin, double ymax) { }

	private record Gesture(ControlLayout layout, Panel panel, Slot slot, Transform transform,
			int x, int y, boolean resize) { }

	GeoCeDGControlLayoutWorkspace(AppGeoCeDG app, GeoCeDGEuclidianView view) {
		this.app = app;
		this.view = view;
		app.getLayerWorkspace().addListener(this::refreshLayerVisibility);
		app.getEventDispatcher().addEventListener(event -> {
			if (dialog != null && (event.type == EventType.UNDO || event.type == EventType.REDO
					|| event.type == EventType.REMOVE)) {
				cancelGesture();
				SwingUtilities.invokeLater(() -> {
					if (dialog != null) {
						refreshManager(selectedPanelId(), null);
					}
				});
			}
		});
	}

	private ControlLayout layout() {
		return app.getKernel().getConstruction().getControlLayout();
	}

	private Transform transform() {
		var insets = view.getSafeAreaInsets();
		return new Transform(view.getXZero(), view.getYZero(), view.getXscale(),
				view.getYscale(), insets.getLeft(), insets.getTop(),
				view.getWidth(), view.getHeight(), view.getXaxisLog(), view.getYaxisLog(),
				view.getXmin(), view.getXmax(), view.getYmin(), view.getYmax());
	}

	private GPoint2D origin(Panel panel) {
		var insets = view.getSafeAreaInsets();
		return panel.anchor() == Anchor.FIXED
				? new GPoint2D(insets.getLeft() + panel.x(), insets.getTop() + panel.y())
				: new GPoint2D(view.toScreenCoordXd(panel.x()), view.toScreenCoordYd(panel.y()));
	}

	private Panel positioned(Panel panel, Anchor anchor, double x, double y) {
		var insets = view.getSafeAreaInsets();
		if (anchor == Anchor.FIXED) {
			return new Panel(panel.id(), anchor, x - insets.getLeft(), y - insets.getTop(),
					panel.width(), panel.height(), panel.visible(), panel.name(), panel.rgb(),
					panel.opacity());
		}
		double worldX = view.getXaxisLog()
				? fromLogScale(view.getXmin(), view.getXmax(), x / view.getWidth())
				: view.toRealWorldCoordX(x);
		double worldY = view.getYaxisLog()
				? fromLogScale(view.getYmin(), view.getYmax(), 1 - y / view.getHeight())
				: view.toRealWorldCoordY(y);
		return new Panel(panel.id(), anchor, worldX, worldY, panel.width(), panel.height(),
				panel.visible(), panel.name(), panel.rgb(), panel.opacity());
	}

	private static double fromLogScale(double min, double max, double fraction) {
		return Math.pow(10, Math.log10(min) + fraction * (Math.log10(max) - Math.log10(min)));
	}

	private boolean live() {
		return view.getViewID() == App.VIEW_EUCLIDIAN && !app.isExporting();
	}

	private Slot liveSlot(GeoElement geo) {
		if (!live()) {
			return null;
		}
		Slot slot = layout().slot(geo);
		return slot != null && layout().isEligible(geo) ? slot : null;
	}

	private Panel displayedPanel(Panel panel) {
		Panel style = stylePreviewFor(panel);
		if (style != null) {
			return style;
		}
		return previewPanel != null && gesture != null && gesture.panel() == panel
				? previewPanel : panel;
	}

	GPoint2D position(GeoElement geo) {
		Slot slot = liveSlot(geo);
		if (slot == null) {
			return null;
		}
		Panel panel = displayedPanel(layout().panel(slot.panelId()));
		Slot displayed = previewSlot != null && gesture.slot() == slot ? previewSlot : slot;
		GPoint2D point = origin(panel);
		point.x += displayed.dx();
		point.y += displayed.dy();
		return screenPoint(point) ? point : null;
	}

	private static boolean screenPoint(GPoint2D point) {
		return Math.abs(point.x) < Integer.MAX_VALUE / 2
				&& Math.abs(point.y) < Integer.MAX_VALUE / 2;
	}

	boolean visible(GeoElement geo) {
		Slot slot = liveSlot(geo);
		return slot == null || (layout().panel(slot.panelId()).visible()
				&& app.isLayerShown(geo.getLayer())
				&& geo.isAvailableAtConstructionStep(geo.getConstruction().getStep())
				&& position(geo) != null);
	}

	boolean deferred(GeoElement geo) {
		return liveSlot(geo) != null;
	}

	private boolean shown(GeoElement geo) {
		return geo.isEuclidianVisible() && view.isVisibleInThisView(geo)
				&& app.isLayerShown(geo.getLayer()) && visible(geo);
	}

	void paint(GGraphics2D graphics) {
		if (!live() || layout().isEmpty()) {
			return;
		}
		finishCoveredEditor();
		Rectangle viewport = new Rectangle(0, 0, view.getWidth(), view.getHeight());
		for (Panel stored : layout().panels()) {
			Panel panel = displayedPanel(stored);
			if (!panel.visible() && dialog == null) {
				continue;
			}
			Rectangle bounds = bounds(panel);
			Rectangle clip = bounds.intersection(viewport);
			if (clip.isEmpty()) {
				continue;
			}
			if (panel.visible()) {
				int rgb = panel.rgb();
				graphics.setColor(GColor.newColor(rgb >> 16, rgb >> 8 & 0xff, rgb & 0xff,
						(int) Math.round(panel.opacity() * 255 / 100.0)));
				graphics.fillRect(clip.x, clip.y, clip.width, clip.height);
				paintMembers(graphics, panel);
			}
			paintChrome(graphics, panel, bounds);
		}
	}

	private void paintMembers(GGraphics2D graphics, Panel panel) {
		for (GeoElement geo : layout().slots(panel.id()).stream().map(Slot::control)
				.filter(this::shown).sorted(MEMBER_ORDER).toList()) {
			if (view.getDrawableFor(geo) instanceof Drawable drawable && geo.isDefined()) {
				drawable.updateIfNeeded();
				drawable.draw(graphics);
			}
		}
	}

	private void paintChrome(GGraphics2D graphics, Panel panel, Rectangle bounds) {
		graphics.setColor(OUTLINE);
		graphics.setStroke(AwtFactory.getPrototype().newBasicStroke(1));
		graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
		Rectangle bar = header(bounds);
		graphics.fillRect(bar.x, bar.y, bar.width, bar.height);
		graphics.setColor(GColor.WHITE);
		var font = view.getFontPoint();
		graphics.setFont(font);
		String label = fittedName(graphics, panelName(panel), bar.width - 2 * MARGIN);
		graphics.drawString(label, bar.x + MARGIN, bar.y + (bar.height + font.getSize()) / 2 - 2);
		if (dialog != null) {
			Rectangle grip = handle(bounds);
			graphics.setColor(OUTLINE);
			graphics.fillRect(grip.x, grip.y, grip.width, grip.height);
		}
	}

	String fittedName(GGraphics2D graphics, String label, int width) {
		if (textWidth(graphics, label) <= width) {
			return label;
		}
		BreakIterator boundaries = BreakIterator.getCharacterInstance(app.getLocale());
		boundaries.setText(label);
		for (int end = boundaries.preceding(label.length()); end > 0;
				end = boundaries.previous()) {
			String shortened = label.substring(0, end) + "…";
			if (textWidth(graphics, shortened) <= width) {
				return shortened;
			}
		}
		return textWidth(graphics, "…") <= width ? "…" : "";
	}

	private double textWidth(GGraphics2D graphics, String text) {
		return AwtFactory.getPrototype().newTextLayout(text, view.getFontPoint(),
				graphics.getFontRenderContext()).getAdvance();
	}

	private Rectangle bounds(Panel panel) {
		GPoint2D point = origin(panel);
		if (!screenPoint(point)) {
			return new Rectangle();
		}
		int headerHeight = headerHeight();
		Rectangle rect = new Rectangle((int) Math.round(point.x), (int) Math.round(point.y),
				Math.min(panel.width(), Integer.MAX_VALUE / 4),
				Math.max(headerHeight, Math.min(panel.height(), Integer.MAX_VALUE / 4)));
		int top = rect.y;
		for (Slot slot : layout().slots(panel.id())) {
			GeoElement geo = slot.control();
			if (shown(geo) && view.getDrawableFor(geo) instanceof Drawable drawable) {
				drawable.updateIfNeeded();
				GRectangle child = drawable.getBounds();
				if (child != null && (child.getWidth() > 0 || child.getHeight() > 0)) {
					top = Math.min(top, (int) child.getY() - MARGIN - headerHeight);
					rect.add(new Rectangle((int) child.getX() - MARGIN,
							(int) child.getY() - MARGIN,
							(int) child.getWidth() + 2 * MARGIN,
							(int) child.getHeight() + 2 * MARGIN));
				}
			}
		}
		rect.add(rect.x, top);
		return rect;
	}

	private int headerHeight() {
		return Math.max(HEADER, (int) Math.ceil(view.getFontPoint().getSize()) + 8);
	}

	private Rectangle header(Rectangle bounds) {
		return new Rectangle(bounds.x, bounds.y, bounds.width, headerHeight());
	}

	private static Rectangle handle(Rectangle bounds) {
		return new Rectangle(bounds.x + bounds.width - HANDLE,
				bounds.y + bounds.height - HANDLE, HANDLE, HANDLE);
	}

	Panel panelAt(int x, int y) {
		if (!live()) {
			return null;
		}
		var ordered = layout().panels();
		for (int i = ordered.size() - 1; i >= 0; i--) {
			Panel panel = ordered.get(i);
			if (panel.visible() && bounds(displayedPanel(panel)).contains(x, y)) {
				return panel;
			}
		}
		return null;
	}

	void filterHits(GPoint point, Hits hits) {
		if (!live() || layout().isEmpty() || hits.isEmpty()) {
			return;
		}
		Panel front = panelAt(point.x, point.y);
		List<GeoElement> visibleHits = new ArrayList<>(hits.size());
		boolean frontMemberHit = false;
		for (GeoElement geo : hits) {
			boolean inFront = isMemberOf(geo, front);
			if ((front == null || inFront || !isControl(geo))
					&& (!(geo instanceof GeoInputBox) || !editorCovered(geo))) {
				visibleHits.add(geo);
				frontMemberHit |= inFront;
			}
		}
		if (frontMemberHit && app.getMode() == EuclidianConstants.MODE_MOVE
				&& hits.stream().noneMatch(geo -> geo.isMask() || geo.isMeasurementTool())) {
			visibleHits.removeIf(geo -> !isMemberOf(geo, front));
		}
		if (visibleHits.size() != hits.size()) {
			hits.init();
			visibleHits.forEach(hits::add);
		}
	}

	private boolean isMemberOf(GeoElement geo, Panel panel) {
		Slot slot = liveSlot(geo);
		return panel != null && slot != null && panel.id().equals(slot.panelId());
	}

	private static boolean isControl(GeoElement geo) {
		return geo instanceof GeoButton || geo.isGeoBoolean()
				|| geo instanceof GeoNumeric numeric && numeric.isSlider();
	}

	private boolean editorCovered(GeoElement geo) {
		if (!(view.getDrawableFor(geo) instanceof DrawInputBox box)) {
			return false;
		}
		GRectangle field = box.getBounds();
		if (field == null) {
			return false;
		}
		Rectangle region = new Rectangle((int) field.getX(), (int) field.getY(),
				(int) field.getWidth(), (int) field.getHeight());
		var ordered = layout().panels();
		for (int i = ordered.size() - 1; i >= 0; i--) {
			Panel panel = ordered.get(i);
			if (isMemberOf(geo, panel)) {
				break;
			}
			if (panel.visible() && bounds(displayedPanel(panel)).intersects(region)) {
				return true;
			}
		}
		return false;
	}

	private void finishCoveredEditor() {
		var symbolic = view.getSymbolicEditor();
		DrawInputBox box = symbolic == null ? null : symbolic.getDrawInputBox();
		if (box != null && box.isEditing() && editorCovered(box.getGeoElement())) {
			symbolic.applyAndHidDeferred();
		}
		var field = view.getTextField();
		if (field != null && field.hasFocus() && field.getInputBox() != null
				&& editorCovered(field.getInputBox())) {
			view.getViewTextField().hideDeferred();
		}
	}

	JPopupMenu panelContextMenu(Panel panel) {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item = new JMenuItem(text("ControlLayout.OpenForPanel"));
		item.addActionListener(event -> {
			if (layout().panel(panel.id()) == panel && eligibleContext()) {
				openPanel(panel.id());
			}
		});
		menu.add(item);
		return menu;
	}

	boolean eligibleContext() {
		return styleEditor == null && live() && !app.isInsertionIsolated()
				&& app.getActiveEuclidianView() == view
				&& view.getEuclidianController() instanceof GeoCeDGEuclidianController controller
				&& app.getMode() == EuclidianConstants.MODE_MOVE
				&& !controller.isTextfieldHasFocus() && !controller.isZoomWindowActive()
				&& !controller.referencePointTool().isPanelActive()
				&& !GeoCeDGArgumentDialog.isOpen(app)
				&& (view.getSymbolicEditor() == null
						|| view.getSymbolicEditor().getDrawInputBox() == null
						|| !view.getSymbolicEditor().getDrawInputBox().isEditing());
	}

	void open() {
		openPanel(null);
	}

	void openPanel(String panelId) {
		if (!eligibleContext()) {
			warn("ControlLayout.Context");
			return;
		}
		if (dialog != null) {
			if (panelId != null) {
				refreshManager(panelId, null);
			}
			dialog.toFront();
			return;
		}
		dialog = new JDialog(SwingUtilities.getWindowAncestor(view.getJPanel()),
				text("ControlLayout.Title"), Dialog.ModalityType.MODELESS);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelLayoutGesture");
		dialog.getRootPane().getActionMap().put("cancelLayoutGesture", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				cancelGesture();
			}
		});
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				if (event.getWindow() == dialog) {
					close();
				}
			}
		});
		JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panels = new JComboBox<>();
		panels.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
					int index, boolean selected, boolean focus) {
				putClientProperty("html.disable", true);
				return super.getListCellRendererComponent(list,
						value instanceof Panel panel ? panelSelectorName(panel) : " ",
						index, selected, focus);
			}
		});
		panels.getAccessibleContext().setAccessibleName(text("ControlLayout.Panel"));
		panels.addActionListener(event -> refreshMembers(null));
		members = new JComboBox<>();
		members.setRenderer((list, item, index, selected, focus) ->
				new JLabel(item == null ? " " : item.control().getLabelSimple()));
		members.getAccessibleContext().setAccessibleName(text("ControlLayout.Member"));
		target = new JLabel(text("ControlLayout.Pick"));
		selectors.add(panels);
		selectors.add(members);
		selectors.add(target);

		JPanel buttons = new JPanel(new GridLayout(0, 3, 4, 4));
		button(buttons, "New", this::create);
		button(buttons, "Attach", this::attach);
		button(buttons, "Detach", this::detach);
		button(buttons, "Anchor", this::convert);
		button(buttons, "Visibility", this::toggleVisible);
		button(buttons, "Appearance", this::editAppearance);
		button(buttons, "Duplicate", this::duplicate);
		button(buttons, "BringIntoView", this::bringIntoView);
		button(buttons, "Remove", this::remove);
		JButton close = new JButton(text("ControlLayout.Close"));
		close.addActionListener(event -> close());
		buttons.add(close);
		JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(selectors, BorderLayout.NORTH);
		content.add(buttons, BorderLayout.CENTER);
		content.add(new JLabel("<html><div style='width: 420px'>"
				+ GeoCeDGControlLayoutNotice.message(app) + "</div></html>"), BorderLayout.SOUTH);
		dialog.setContentPane(content);
		refreshManager(panelId, null);
		dialog.pack();
		dialog.setLocationRelativeTo(view.getJPanel());
		dialog.setVisible(true);
		view.repaintView();
	}

	private void button(JPanel buttons, String action, Runnable command) {
		JButton button = new JButton(text("ControlLayout." + action));
		button.addActionListener(event -> {
			if (eligibleContext()) {
				try {
					command.run();
				} catch (IllegalArgumentException | IllegalStateException exception) {
					warn("ControlLayout.Rejected");
				}
			} else {
				warn("ControlLayout.Context");
			}
		});
		buttons.add(button);
	}

	private String panelName(Panel panel) {
		return panel.name().isBlank() ? text("ControlLayout.DefaultName") : panel.name();
	}

	private Panel stylePreviewFor(Panel panel) {
		return styleEditor != null && styleEditor.isEditing(panel) ? stylePreviewPanel : null;
	}

	private String panelSelectorName(Panel panel) {
		String name = panelName(panel);
		if (name.codePointCount(0, name.length()) > 24) {
			name = name.substring(0, name.offsetByCodePoints(0, 24)) + "…";
		}
		return name + " · " + text(panel.anchor() == Anchor.FIXED
				? "ControlLayout.Fixed" : "ControlLayout.World") + " " + panel.id().substring(0, 8)
				+ (panel.visible() ? "" : " (" + text("ControlLayout.Hidden") + ")");
	}

	private Panel selectedPanel() {
		Panel selected = panels == null ? null : (Panel) panels.getSelectedItem();
		return selected == null ? null : layout().panel(selected.id());
	}

	private String selectedPanelId() {
		Panel selected = selectedPanel();
		return selected == null ? null : selected.id();
	}

	private void refreshManager(String panelId, GeoElement member) {
		if (dialog == null) {
			return;
		}
		panels.removeAllItems();
		for (Panel panel : layout().panels()) {
			panels.addItem(panel);
			if (panel.id().equals(panelId)) {
				panels.setSelectedItem(panel);
			}
		}
		refreshMembers(member);
	}

	private void refreshMembers(GeoElement selected) {
		members.removeAllItems();
		Panel panel = selectedPanel();
		if (panel != null) {
			for (Slot slot : layout().slots(panel.id())) {
				members.addItem(slot);
				if (slot.control() == selected) {
					members.setSelectedItem(slot);
				}
			}
		}
	}

	private void create() {
		Panel panel = layout().addPanel(Anchor.FIXED, 24, 24, 220, 110, true);
		commit(panel.id(), null, List.of());
	}

	private void editAppearance() {
		Panel panel = selectedPanel();
		if (panel == null) {
			return;
		}
		cancelGesture();
		ControlLayout originalLayout = layout();
		var editor = new GeoCeDGControlLayoutStyleDialog(app, dialog, panel, preview -> {
			stylePreviewPanel = preview;
			view.repaintView();
		});
		styleEditor = editor;
		Panel accepted;
		try {
			accepted = editor.open();
		} finally {
			editor.close();
			styleEditor = null;
			stylePreviewPanel = null;
			view.repaintView();
		}
		if (accepted != null && dialog != null && eligibleContext() && layout() == originalLayout
				&& layout().panel(panel.id()) == panel) {
			update(accepted);
		}
	}

	private void duplicate() {
		Panel panel = selectedPanel();
		if (panel == null || !layout().slots(panel.id()).isEmpty()) {
			warn("ControlLayout.EmptyOnly");
			return;
		}
		GPoint2D current = origin(panel);
		Panel shifted = positioned(panel, panel.anchor(), current.x + HEADER, current.y + HEADER);
		JTextField x = new JTextField(Double.toString(shifted.x()), 12);
		JTextField y = new JTextField(Double.toString(shifted.y()), 12);
		Object[] values = {text("ControlLayout.DuplicateOrigin"), "X", x, "Y", y};
		if (JOptionPane.showConfirmDialog(dialog, values, text("ControlLayout.Duplicate"),
				JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION
				|| layout().panel(panel.id()) != panel) {
			return;
		}
		Panel copy = layout().addPanel(panel.anchor(), Double.parseDouble(x.getText()),
				Double.parseDouble(y.getText()),
				panel.width(), panel.height(), panel.visible(), panel.name(), panel.rgb(), panel.opacity());
		commit(copy.id(), null, List.of());
	}

	private void attach() {
		Panel panel = selectedPanel();
		List<GeoElement> selected = app.getSelectionManager().getSelectedGeos();
		GeoElement geo = candidate == null && selected.size() == 1 ? selected.get(0) : candidate;
		if (panel == null || geo == null || !layout().isEligible(geo)
				|| layout().slot(geo) != null) {
			warn("ControlLayout.Pick");
			return;
		}
		GPoint2D origin = origin(panel);
		GPoint2D control = nativePosition(geo);
		if (overlaps(panel.id(), geo) && !acceptOverlap()) {
			return;
		}
		Slot slot = layout().addSlot(panel.id(), geo, control.x - origin.x, control.y - origin.y);
		candidate = null;
		target.setText(text("ControlLayout.Pick"));
		commit(panel.id(), geo, List.of(slot));
	}

	private GPoint2D nativePosition(GeoElement geo) {
		if (view.getDrawableFor(geo) instanceof DrawSlider slider) {
			return slider.getSliderLocation();
		}
		if (geo instanceof GeoButton button) {
			return new GPoint2D(button.getScreenLocX(view), button.getScreenLocY(view));
		}
		AbsoluteScreenLocateable absolute = (AbsoluteScreenLocateable) geo;
		return new GPoint2D(absolute.getAbsoluteScreenLocX(), absolute.getAbsoluteScreenLocY());
	}

	private void detach() {
		Slot selected = (Slot) members.getSelectedItem();
		Slot slot = selected == null ? null : layout().slot(selected.control());
		if (slot == null) {
			return;
		}
		GPoint2D position = position(slot.control());
		if (position == null || !new Rectangle(0, 0, view.getWidth(), view.getHeight())
				.contains(position.x, position.y)) {
			Object[] options = {text("ControlLayout.VisiblePosition"),
					text("ControlLayout.Backup"), app.getLocalization().getMenu("Cancel")};
			int choice = JOptionPane.showOptionDialog(dialog, text("ControlLayout.Outside"),
					text("ControlLayout.Title"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (choice != 0 && choice != 1) {
				return;
			}
			position = choice == 0 ? new GPoint2D(transform().left() + MARGIN,
					transform().top() + HEADER) : null;
		}
		if (position != null && layout().isEligible(slot.control())) {
			((AbsoluteScreenLocateable) slot.control()).setAbsoluteScreenLoc(
					(int) Math.round(position.x), (int) Math.round(position.y));
		}
		layout().removeSlot(slot.control());
		commit(slot.panelId(), null, List.of(slot));
	}

	private void convert() {
		Panel panel = selectedPanel();
		if (panel != null) {
			GPoint2D point = origin(panel);
			update(positioned(panel, panel.anchor() == Anchor.FIXED
					? Anchor.WORLD : Anchor.FIXED, point.x, point.y));
		}
	}

	private void bringIntoView() {
		Panel panel = selectedPanel();
		if (panel != null) {
			var insets = view.getSafeAreaInsets();
			update(positioned(panel, panel.anchor(), insets.getLeft() + 24,
					insets.getTop() + 24));
		}
	}

	private void toggleVisible() {
		Panel panel = selectedPanel();
		if (panel != null) {
			update(new Panel(panel.id(), panel.anchor(), panel.x(), panel.y(), panel.width(),
					panel.height(), !panel.visible(), panel.name(), panel.rgb(), panel.opacity()));
		}
	}

	private void remove() {
		Panel panel = selectedPanel();
		if (panel != null) {
			List<Slot> affected = layout().slots(panel.id());
			layout().removePanel(panel.id());
			commit(null, null, affected);
		}
	}

	private void update(Panel panel) {
		if (panel.equals(layout().panel(panel.id()))) {
			return;
		}
		layout().updatePanel(panel);
		commit(panel.id(), null, layout().slots(panel.id()));
	}

	private void commit(String panel, GeoElement member, List<Slot> affected) {
		updateDrawables(affected);
		app.storeUndoInfo();
		refreshManager(panel, member);
	}

	private void refreshLayerVisibility() {
		for (Panel panel : layout().panels()) {
			refreshPanel(panel.id());
		}
	}

	int refreshPanel(String panelId) {
		return updateDrawables(layout().slots(panelId));
	}

	private int updateDrawables(List<Slot> affected) {
		for (Slot slot : affected) {
			if (view.getDrawableFor(slot.control()) instanceof Drawable drawable) {
				drawable.updateForView();
			}
		}
		view.repaintView();
		return affected.size();
	}

	boolean press(AbstractEvent event, Iterable<GeoElement> hits) {
		if (dialog == null || !eligibleContext() || event.isRightClick()) {
			return false;
		}
		cancelGesture();
		var available = layout().panels();
		for (int i = available.size() - 1; i >= 0; i--) {
			Panel panel = available.get(i);
			Rectangle bounds = bounds(panel);
			boolean resize = !bounds.isEmpty() && handle(bounds).contains(event.getX(), event.getY());
			if (resize || header(bounds).contains(event.getX(), event.getY())) {
				begin(panel, null, resize, event);
				return true;
			}
			if (panel.visible() && bounds.contains(event.getX(), event.getY())) {
				break;
			}
		}
		for (GeoElement geo : hits) {
			Slot slot = liveSlot(geo);
			if (slot != null) {
				begin(layout().panel(slot.panelId()), slot, false, event);
				return true;
			}
			if (geo instanceof AbsoluteScreenLocateable && layout().isEligible(geo)) {
				candidate = geo;
				target.setText(geo.getLabelSimple());
				ownsPointer = true;
				return true;
			}
		}
		return false;
	}

	private void begin(Panel panel, Slot slot, boolean resize, AbstractEvent event) {
		ownsPointer = true;
		gesture = new Gesture(layout(), panel, slot, transform(), event.getX(), event.getY(), resize);
		refreshManager(panel.id(), slot == null ? null : slot.control());
	}

	private boolean gestureCurrent() {
		return gesture != null && eligibleContext() && gesture.layout() == layout()
				&& layout().panel(gesture.panel().id()) == gesture.panel()
				&& (gesture.slot() == null || liveSlot(gesture.slot().control()) == gesture.slot())
				&& transform().equals(gesture.transform());
	}

	boolean drag(AbstractEvent event) {
		if (!ownsPointer) {
			return false;
		}
		if (!gestureCurrent()) {
			cancelGesture();
			return true;
		}
		double dx = (double) event.getX() - gesture.x();
		double dy = (double) event.getY() - gesture.y();
		Panel panel = gesture.panel();
		Slot slot = gesture.slot();
		if (slot != null) {
			previewSlot = new Slot(slot.panelId(), slot.id(), slot.control(),
					slot.dx() + dx, slot.dy() + dy);
			updateDrawables(List.of(slot));
		} else {
			GPoint2D point = origin(panel);
			try {
				previewPanel = gesture.resize()
						? new Panel(panel.id(), panel.anchor(), panel.x(), panel.y(),
								preferredSize(panel.width() + dx, HANDLE * 2),
								preferredSize(panel.height() + dy, headerHeight()), panel.visible(),
								panel.name(), panel.rgb(), panel.opacity())
						: positioned(panel, panel.anchor(), point.x + dx, point.y + dy);
			} catch (IllegalStateException invalidPosition) {
				previewPanel = null;
			}
			refreshPanel(panel.id());
		}
		return true;
	}

	private static int preferredSize(double value, int minimum) {
		return (int) Math.max(minimum, Math.min(Integer.MAX_VALUE, value));
	}

	boolean release() {
		if (!ownsPointer) {
			return false;
		}
		ownsPointer = false;
		Gesture current = gesture;
		Panel panel = previewPanel;
		Slot slot = previewSlot;
		boolean valid = gestureCurrent();
		boolean overlap = valid && slot != null && !slot.equals(current.slot())
				&& overlaps(slot.panelId(), slot.control());
		cancelGesture();
		if (!valid || (overlap && !acceptOverlap()) || !eligibleContext()
				|| current.layout() != layout()
				|| layout().panel(current.panel().id()) != current.panel()
				|| !transform().equals(current.transform())) {
			return true;
		}
		if (slot != null && !slot.equals(current.slot())
				&& layout().slot(slot.control()) == current.slot()) {
			layout().updateSlot(slot);
			commit(slot.panelId(), slot.control(), List.of(slot));
		} else if (panel != null && !panel.equals(current.panel())) {
			update(panel);
		}
		return true;
	}

	private boolean overlaps(String panelId, GeoElement geo) {
		if (!(view.getDrawableFor(geo) instanceof Drawable drawable)
				|| drawable.getBounds() == null) {
			return false;
		}
		for (Slot slot : layout().slots(panelId)) {
			GeoElement other = slot.control();
			if (geo != other && shown(other)
					&& view.getDrawableFor(other) instanceof Drawable member
					&& member.intersectsRectangle(drawable.getBounds())) {
				return true;
			}
		}
		return false;
	}

	private boolean acceptOverlap() {
		return JOptionPane.showConfirmDialog(dialog, text("ControlLayout.Overlap"),
				text("ControlLayout.Title"), JOptionPane.OK_CANCEL_OPTION)
				== JOptionPane.OK_OPTION;
	}

	boolean cancelGesture() {
		Gesture current = gesture;
		gesture = null;
		previewPanel = null;
		previewSlot = null;
		if (current == null) {
			return false;
		}
		updateDrawables(current.slot() == null ? layout().slots(current.panel().id())
				: List.of(current.slot()));
		return true;
	}

	boolean isOpen() {
		return dialog != null;
	}

	void close() {
		cancelGesture();
		if (styleEditor != null) {
			styleEditor.close();
		}
		stylePreviewPanel = null;
		JDialog previous = dialog;
		dialog = null;
		candidate = null;
		if (previous != null) {
			previous.dispose();
			view.repaintView();
		}
	}

	private String text(String key) {
		return app.layerText(key);
	}

	private void warn(String key) {
		JOptionPane.showMessageDialog(view.getJPanel(), text(key),
				text("ControlLayout.Title"), JOptionPane.WARNING_MESSAGE);
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DragSource;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.arithmetic.FunctionalNVar;
import org.geogebra.common.kernel.arithmetic.SymbolicMode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventListener;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.euclidian.EuclidianViewJPanelD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraTree;
import org.geogebra.desktop.gui.view.algebra.AlgebraTreeCellRenderer;
import org.geogebra.desktop.main.AppD;

/** One modeless Desktop argument prompt with a document-local input shield. */
public final class GeoCeDGArgumentDialog implements ErrorHandler, EventListener {
	private static final String SESSION = "geocedg.argumentDialog";
	private final AppGeoCeDG app;
	private final JDialog dialog;
	private final JTextField field;
	private final JComboBox<GeoElement> linkSelector;
	private final References references;
	private final List<GeoElement> operands;
	private int mode;
	private final Construction construction;
	private final Object registry;
	private final Consumer<GeoCeDGArgumentDialog> submit;
	private final Map<JRootPane, Component> shields = new IdentityHashMap<>();
	private final JLabel status = new JLabel(" ");
	private final JButton confirm;
	private final JButton pick;
	private PropertyChangeListener focusListener;
	private AbstractDocument document;
	private References.Choice selectedLink;
	private Supplier<GeoElement> redefineOwner;
	private SecondaryLoop loop;
	private boolean armed;
	private boolean processing;
	private boolean finished;
	private boolean operandStale;
	private boolean historyChanged;
	private boolean escapeDown;
	private boolean closeOnEscape;

	static JPanel numberForm(String message, JTextField field) {
		JPanel form = new JPanel(new BorderLayout(0, 4));
		JLabel label = new JLabel(message);
		label.setLabelFor(field);
		form.add(label, BorderLayout.NORTH);
		form.add(field, BorderLayout.CENTER);
		return form;
	}

	static boolean supports(AppD app) {
		return app instanceof AppGeoCeDG && EventQueue.isDispatchThread()
				&& owner(app) instanceof RootPaneContainer;
	}

	static boolean isOpen(AppD app) {
		return owner(app) instanceof RootPaneContainer root
				&& root.getRootPane().getClientProperty(SESSION) != null;
	}

	private static Window owner(AppD app) {
		Component main = app.getMainComponent();
		return main instanceof Window window ? window : SwingUtilities.getWindowAncestor(main);
	}

	static void show(AppGeoCeDG app, String title, JComponent form, JTextField field,
			List<GeoElement> operands, Consumer<GeoCeDGArgumentDialog> submit) {
		if (!supports(app) || isOpen(app)) {
			return;
		}
		new GeoCeDGArgumentDialog(app, owner(app), title, form, field, operands, submit).open();
	}

	private GeoCeDGArgumentDialog(AppGeoCeDG app, Window owner, String title,
			JComponent form, JTextField field, List<GeoElement> operands,
			Consumer<GeoCeDGArgumentDialog> submit) {
		this(app, new JDialog(owner, title, java.awt.Dialog.ModalityType.MODELESS),
				field, null, operands, References.NUMERIC, submit);
		dialog.setName(SESSION);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JButton cancel = new JButton(app.getLocalization().getMenu("Cancel"));
		cancel.addActionListener(event -> finish());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttons.add(pick);
		buttons.add(confirm);
		buttons.add(cancel);

		JPanel content = new JPanel(new BorderLayout(0, 4));
		content.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(form, BorderLayout.NORTH);
		content.add(status, BorderLayout.CENTER);
		content.add(buttons, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(confirm);
	}

	private GeoCeDGArgumentDialog(AppGeoCeDG app, JDialog dialog, JTextField field,
			JComboBox<GeoElement> linkSelector, List<GeoElement> operands,
			Predicate<GeoElement> eligibility, Consumer<GeoCeDGArgumentDialog> submit) {
		this.app = app;
		this.dialog = dialog;
		this.field = field;
		this.linkSelector = linkSelector;
		this.operands = List.copyOf(operands);
		this.submit = submit;
		construction = app.getKernel().getConstruction();
		registry = construction.getSpatialIdentityRegistry();
		references = new References(construction, eligibility);

		confirm = new JButton(app.getLocalization().getMenu("OK"));
		confirm.addActionListener(event -> confirm());
		pick = new JButton(text("Button"));
		pick.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(pick.getText().charAt(0)));
		pick.addActionListener(event -> arm());
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent event) {
				// The legacy dispatcher in another application can hide a dialog.
				finish();
			}
		});
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				finish();
			}

			@Override
			public void windowClosed(WindowEvent event) {
				finish();
			}
		});
	}

	/**
	 * Install capture for an existing numeric or Redefine field.
	 * @param app Desktop application supplying the construction
	 * @param dialog existing dialog
	 * @param panel panel receiving the pick button and status
	 * @param field active expression field
	 * @param owner current redefinition target, or null for a numeric prompt
	 * @return capture session when the Desktop context supports it
	 */
	public static GeoCeDGArgumentDialog forField(AppD app, JDialog dialog, JPanel panel,
			JTextField field, Supplier<GeoElement> owner) {
		if (!supports(app) || !(field.getDocument() instanceof AbstractDocument fieldDocument)
				|| fieldDocument.getDocumentFilter() != null) {
			return null;
		}
		Predicate<GeoElement> eligibility = owner == null ? References.NUMERIC
				: candidate -> redefineReference(owner.get(), candidate);
		GeoCeDGArgumentDialog capture = new GeoCeDGArgumentDialog((AppGeoCeDG) app,
				dialog, field, null, List.of(), eligibility, null);
		capture.redefineOwner = owner;
		panel.add(capture.pick, 0);
		panel.add(capture.status, 1);
		return capture;
	}

	/**
	 * Install capture for the native Input Box link selector.
	 * @param app Desktop application supplying the construction
	 * @param dialog existing Input Box dialog
	 * @param panel panel receiving the pick button and status
	 * @param selector native link selector
	 * @return capture session when the Desktop context supports it
	 */
	public static GeoCeDGArgumentDialog forLink(AppD app, JDialog dialog, JPanel panel,
			JComboBox<GeoElement> selector) {
		if (!supports(app)) {
			return null;
		}
		GeoCeDGArgumentDialog capture = new GeoCeDGArgumentDialog((AppGeoCeDG) app,
				dialog, null, selector, List.of(), candidate -> {
					for (int index = 0; index < selector.getItemCount(); index++) {
						if (candidate == selector.getItemAt(index)) {
							return GeoInputBox.isGeoLinkable(candidate);
						}
					}
					return false;
				}, null);
		selector.addActionListener(event -> capture.selectedLink = null);
		panel.add(capture.pick);
		panel.add(capture.status);
		return capture;
	}

	static boolean redefineReference(GeoElement owner, GeoElement candidate) {
		return owner != null && !candidate.isChildOrEqual(owner)
				&& (!(owner instanceof GeoInputBox) || GeoInputBox.isGeoLinkable(candidate))
				&& (!(owner instanceof FunctionalNVar function) || function.getFunction() == null
						|| Arrays.stream(function.getFunction().getFunctionVariables())
								.noneMatch(variable -> variable.getSetVarString()
										.equals(candidate.getLabelSimple())));
	}

	private void open() {
		if (!begin()) {
			dialog.dispose();
			return;
		}
		try {
			app.setComponentOrientation(dialog);
			dialog.pack();
			dialog.setLocationRelativeTo(dialog.getOwner());
			showAndWait(() -> dialog.setVisible(true));
		} finally {
			dialog.dispose();
			end();
		}
	}

	/** @return whether the dialog claimed the current document's input shield */
	public boolean begin() {
		if (focusListener != null) {
			return true;
		}
		if (finished || app.isInsertionIsolated() || isOpen(app)
				|| !(owner(app) instanceof RootPaneContainer main)
				|| app.getEuclidianView1().getEuclidianController()
						instanceof GeoCeDGEuclidianController controller
						&& controller.referencePointTool().isPanelActive()) {
			return false;
		}
		app.closeControlLayout();
		mode = app.getMode();
		if (!shield(main.getRootPane())) {
			return false;
		}
		for (var panel : ((GuiManagerD) app.getGuiManager()).getLayout()
				.getDockManager().getPanels()) {
			if (panel.getFrame() instanceof RootPaneContainer detached
					&& !shield(detached.getRootPane())) {
				end();
				return false;
			}
		}
		if (field != null) {
			if (!(field.getDocument() instanceof AbstractDocument currentDocument)
					|| currentDocument.getDocumentFilter() != null) {
				end();
				return false;
			}
			document = currentDocument;
			document.setDocumentFilter(references);
		}
		dialog.getRootPane().putClientProperty(SESSION, this);
		focusListener = event -> {
			if (armed && event.getNewValue() instanceof Window window && !ownsFocus(window)) {
				armed = false;
				resetError();
			}
		};
		app.getEventDispatcher().addEventListener(this);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addPropertyChangeListener("focusedWindow", focusListener);
		return true;
	}

	/**
	 * Maintain the blocking API for an originally modal prompt using the document shield.
	 * @param show action that makes the shielded dialog visible
	 */
	public void showAndWait(Runnable show) {
		loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
		try {
			show.run();
			if (!finished) {
				loop.enter();
			}
		} finally {
			end();
		}
	}

	/** Release the existing dialog's capture when it closes. */
	public void end() {
		finished = true;
		armed = false;
		if (loop != null) {
			loop.exit();
			loop = null;
		}
		shields.forEach((root, previous) -> {
			if (root.getClientProperty(SESSION) == this) {
				root.getGlassPane().setVisible(false);
				root.setGlassPane(previous);
				root.putClientProperty(SESSION, null);
			}
		});
		shields.clear();
		dialog.getRootPane().putClientProperty(SESSION, null);
		if (focusListener != null) {
			app.getEventDispatcher().removeEventListener(this);
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.removePropertyChangeListener("focusedWindow", focusListener);
			focusListener = null;
		}
		if (document != null && document.getDocumentFilter() == references) {
			document.setDocumentFilter(null);
		}
		document = null;
	}

	private boolean ownsFocus(Window window) {
		if (window instanceof RootPaneContainer root && shields.containsKey(root.getRootPane())) {
			return true;
		}
		for (Window owned = window; owned != null; owned = owned.getOwner()) {
			if (owned == dialog) {
				return true;
			}
		}
		return false;
	}

	private boolean shield(JRootPane root) {
		if (shields.containsKey(root)) {
			return true;
		}
		Component old = root.getGlassPane();
		if (old.isVisible() || root.getClientProperty(SESSION) != null) {
			return false;
		}
		shields.put(root, old);
		JPanel shield = new JPanel();
		shield.setOpaque(false);
		shield.setFocusTraversalKeysEnabled(false);
		MouseAdapter pointer = new MouseAdapter() {
			private Point press;

			@Override
			public void mousePressed(MouseEvent event) {
				event.consume();
				press = armed && isPickClick(event) ? event.getPoint() : null;
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				event.consume();
				boolean pick = armed && isPickClick(event) && withinDragThreshold(event);
				press = null;
				if (pick) {
					pickAt(event);
				}
			}

			@Override
			public void mouseClicked(MouseEvent event) {
				event.consume();
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				event.consume();
				if (!withinDragThreshold(event)) {
					press = null;
				}
			}

			private boolean withinDragThreshold(MouseEvent event) {
				return press != null && press.distance(event.getPoint()) <= DragSource.getDragThreshold();
			}
		};
		shield.addMouseListener(pointer);
		shield.addMouseMotionListener(pointer);
		shield.addMouseWheelListener(MouseEvent::consume);
		root.putClientProperty(SESSION, this);
		root.setGlassPane(shield);
		shield.setVisible(true);
		return true;
	}

	private static boolean isPickClick(MouseEvent event) {
		return event.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(event)
				&& !event.isControlDown() && !event.isShiftDown()
				&& !event.isAltDown() && !event.isMetaDown();
	}

	private void arm() {
		if (focusListener != null && !processing && operandsCurrent()) {
			armed = true;
			showError(text(referenceMessage("Armed")));
		}
	}

	private void pickAt(MouseEvent event) {
		if (!operandsCurrent()) {
			disarm(text("OperandStale"));
			return;
		}
		var view = app.getEuclidianView1();
		JComponent canvas = view.getJPanel();
		Point point = localPoint(event, canvas);
		List<GeoElement> choices = new ArrayList<>();
		if (point != null) {
			GPoint location = new GPoint(point.x, point.y);
			for (var drawable : view.getAllDrawableList()) {
				GeoElement geo = drawable.getGeoElement();
				if (geo.isEuclidianVisible() && geo.isLabelVisible() && geo.isSelectionAllowed(view)
						&& app.isLayerShown(geo.getLayer()) && drawable.hitLabel(point.x, point.y)
						&& references.canPick(geo)) {
					choices.add(geo);
				}
			}
			if (choices.isEmpty()) {
				view.setHits(location, PointerEventType.MOUSE);
				for (GeoElement hit : view.getHits()) {
					if (references.canPick(hit) && choices.stream().noneMatch(geo -> geo == hit)) {
						choices.add(hit);
					}
				}
			}
		} else if (((GuiManagerD) app.getGuiManager()).hasAlgebraViewShowing()
				&& app.getGuiManager().getAlgebraView() instanceof AlgebraTree tree) {
			point = localPoint(event, tree);
			if (point != null) {
				var path = tree.getPathForLocation(point.x, point.y);
				Rectangle bounds = path == null ? null : tree.getPathBounds(path);
				GeoElement geo = AlgebraTree.getGeoElementForPath(path);
				if (bounds != null
						&& tree.getCellRenderer() instanceof AlgebraTreeCellRenderer renderer
						&& point.x >= bounds.x + renderer.getIconShown().getIconWidth()
						&& references.canPick(geo)) {
					choices.add(geo);
				}
			}
		}

		if (choices.isEmpty()) {
			showError(text(referenceMessage("None")));
			return;
		}
		List<References.Choice> candidates = choices.stream().map(references::offer).toList();
		References.Choice selected = candidates.get(0);
		if (candidates.size() > 1) {
			Object answer = JOptionPane.showInputDialog(dialog, text(referenceMessage("Armed")),
					dialog.getTitle(),
					JOptionPane.QUESTION_MESSAGE, null, candidates.toArray(), null);
			if (!(answer instanceof References.Choice choice)) {
				disarm(null);
				return;
			}
			selected = choice;
		}
		if (!finished && operandsCurrent() && insert(selected)) {
			disarm(null);
		} else {
			disarm(text("Stale"));
		}
	}

	private String referenceMessage(String suffix) {
		return references.eligibility == References.NUMERIC ? suffix : "Geo" + suffix;
	}

	private boolean insert(References.Choice choice) {
		if (linkSelector == null) {
			return references.insert(field, choice);
		}
		references.offers.clear();
		if (!references.current(choice)) {
			return false;
		}
		linkSelector.setSelectedItem(choice.geo);
		selectedLink = choice;
		return true;
	}

	static Point localPoint(MouseEvent event, JComponent component) {
		if (!component.isShowing()
				|| SwingUtilities.getWindowAncestor(component)
						!= SwingUtilities.getWindowAncestor(event.getComponent())) {
			return null;
		}
		Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), component);
		// The Graphics panel's getBounds() is the drawing's extent, not the viewport.
		boolean inside = component instanceof EuclidianViewJPanelD ? component.contains(point)
				: component.getVisibleRect().contains(point);
		return inside ? point : null;
	}

	private void disarm(String message) {
		armed = false;
		showError(message);
		dialog.toFront();
		if (field == null) {
			linkSelector.requestFocusInWindow();
		} else {
			field.requestFocusInWindow();
		}
	}

	static boolean dispatchKey(KeyEvent event) {
		JRootPane root = SwingUtilities.getRootPane(event.getComponent());
		if (root == null || !(root.getClientProperty(SESSION) instanceof GeoCeDGArgumentDialog session)) {
			return false;
		}
		if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (event.getID() == KeyEvent.KEY_PRESSED && !session.escapeDown) {
				session.escapeDown = true;
				session.closeOnEscape = !session.armed;
				if (session.armed) {
					session.disarm(null);
				}
			} else if (event.getID() == KeyEvent.KEY_RELEASED) {
				session.escapeDown = false;
				if (session.closeOnEscape) {
					session.finish();
				}
			}
			return true;
		}
		return root != session.dialog.getRootPane();
	}

	private void confirm() {
		if (!processing && isCurrent()) {
			armed = false;
			processing = true;
			confirm.setEnabled(false);
			pick.setEnabled(false);
			field.setEditable(false);
			resetError();
			try {
				submit.accept(this);
			} catch (RuntimeException | Error exception) {
				finish();
				throw exception;
			}
		}
	}

	/** @return whether the existing submit handler can use the captured instance identities */
	public boolean isCurrent() {
		if (finished) {
			return false;
		}
		if (!operandsCurrent()) {
			showError(text("OperandStale"));
			return false;
		}
		if (linkSelector != null) {
			GeoElement candidate = (GeoElement) linkSelector.getSelectedItem();
			if (candidate == null || (selectedLink != null && selectedLink.geo == candidate
					? references.current(selectedLink) : references.canChoose(candidate))) {
				return true;
			}
			showError(text("Stale"));
			linkSelector.requestFocusInWindow();
			return false;
		}
		References.Token stale = references.firstStale(field.getText());
		if (stale != null) {
			showError(text("Stale"));
			field.requestFocusInWindow();
			field.select(stale.start, stale.end);
			return false;
		}
		return true;
	}

	private boolean operandsCurrent() {
		return !operandStale && !historyChanged && app.getMode() == mode
				&& app.getKernel().getConstruction() == construction
				&& construction.getSpatialIdentityRegistry() == registry
				&& (redefineOwner == null || references.isLive(redefineOwner.get()))
				&& operands.stream().allMatch(references::isLive);
	}

	String input() {
		return field.getText().trim();
	}

	void retry(String message) {
		if (finished) {
			return;
		}
		processing = false;
		confirm.setEnabled(true);
		pick.setEnabled(true);
		field.setEditable(true);
		if (message != null) {
			showError(message);
		}
		field.requestFocusInWindow();
	}

	void finish() {
		if (finished) {
			return;
		}
		end();
		dialog.setVisible(false);
	}

	@Override
	public void sendEvent(Event event) {
		if (event.type == EventType.REMOVE || event.type == EventType.RENAME) {
			references.invalidate(event.target);
			if (selectedLink != null) {
				selectedLink.invalidate(event.target);
			}
			if (event.type == EventType.REMOVE && operands.stream().anyMatch(geo -> geo == event.target)) {
				operandStale = true;
			}
		} else if (event.type == EventType.UNDO || event.type == EventType.REDO) {
			references.invalidateAll();
			if (selectedLink != null) {
				selectedLink.invalid = true;
			}
		}
	}

	@Override
	public void reset() {
		// The dispatcher can be iterating its listeners here.
		historyChanged = true;
		EventQueue.invokeLater(this::finish);
	}

	private String text(String suffix) {
		return GeoCeDGProfile.getText("ArgumentPick." + suffix, app.getLocale().getLanguage());
	}

	@Override
	public void showError(String message) {
		status.setText(message == null ? " " : message);
		dialog.pack();
	}

	@Override
	public void showCommandError(String command, String message) {
		showError(message);
	}

	@Override
	public String getCurrentCommand() {
		return null;
	}

	@Override
	public boolean onUndefinedVariables(String variables, AsyncOperation<String[]> callback) {
		return app.getGuiManager().checkAutoCreateSliders(variables, callback);
	}

	@Override
	public void resetError() {
		showError(null);
	}

	/** Tracks only field spans supplied by graphical selection. */
	static final class References extends DocumentFilter {
		private static final Predicate<GeoElement> NUMERIC = References::numeric;
		private final Construction construction;
		private final Predicate<GeoElement> eligibility;
		private final List<Token> tokens = new ArrayList<>();
		private final List<Choice> offers = new ArrayList<>();

		References(Construction construction) {
			this(construction, NUMERIC);
		}

		References(Construction construction, Predicate<GeoElement> eligibility) {
			this.construction = construction;
			this.eligibility = eligibility;
		}

		private static boolean numeric(GeoElement geo) {
			return geo instanceof GeoNumberValue number && Double.isFinite(number.getDouble());
		}

		boolean isLive(GeoElement geo) {
			return geo != null && geo.getConstruction() == construction
					&& !construction.isFileLoading()
					&& geo.isAvailableAtConstructionStep(construction.getStep())
					&& construction.getGeoSetConstructionOrder().stream()
							.anyMatch(current -> current == geo);
		}

		boolean canPick(GeoElement geo) {
			return canChoose(geo) && geo.isDefined();
		}

		boolean canChoose(GeoElement geo) {
			return isLive(geo) && geo.isLabelSet() && labelNames(geo.getLabelSimple(), geo)
					&& eligibility.test(geo);
		}

		private boolean labelNames(String label, GeoElement geo) {
			return construction.getKernel().lookupLabel(label, false, SymbolicMode.NONE) == geo;
		}

		Choice offer(GeoElement geo) {
			Choice choice = new Choice(geo, geo.getLabelSimple());
			offers.add(choice);
			return choice;
		}

		boolean insert(JTextField field, Choice choice) {
			offers.clear();
			if (!current(choice)) {
				return false;
			}
			int start = field.getSelectionStart() + 1;
			field.replaceSelection(" " + choice.label + " ");
			tokens.add(new Token(start, start + choice.label.length(), choice));
			return true;
		}

		void invalidate(GeoElement geo) {
			tokens.forEach(token -> token.choice.invalidate(geo));
			offers.forEach(choice -> choice.invalidate(geo));
		}

		void invalidateAll() {
			tokens.forEach(token -> token.choice.invalid = true);
			offers.forEach(choice -> choice.invalid = true);
		}

		Token firstStale(String input) {
			return tokens.stream()
					.filter(token -> !token.matches(input) || !current(token.choice))
					.findFirst().orElse(null);
		}

		private boolean current(Choice choice) {
			return !choice.invalid && choice.label.equals(choice.geo.getLabelSimple())
					&& canPick(choice.geo);
		}

		@Override
		public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes)
				throws BadLocationException {
			replace(bypass, offset, 0, text, attributes);
		}

		@Override
		public void remove(FilterBypass bypass, int offset, int length) throws BadLocationException {
			replace(bypass, offset, length, "", null);
		}

		@Override
		public void replace(FilterBypass bypass, int offset, int length, String text,
				AttributeSet attributes) throws BadLocationException {
			String replacement = text == null ? "" : text;
			boolean sameText = replacement.equals(bypass.getDocument().getText(offset, length));
			bypass.replace(offset, length, replacement, attributes);
			if (sameText) {
				return;
			}
			int delta = replacement.length() - length;
			int end = offset + length;
			tokens.removeIf(token -> offset < token.end && end > token.start);
			for (Token token : tokens) {
				if (end <= token.start) {
					token.start += delta;
					token.end += delta;
				}
			}
		}

		static final class Choice {
			final GeoElement geo;
			final String label;
			boolean invalid;

			Choice(GeoElement geo, String label) {
				this.geo = geo;
				this.label = label;
			}

			void invalidate(GeoElement target) {
				invalid |= target == geo;
			}

			@Override
			public String toString() {
				return label;
			}
		}

		static final class Token {
			int start;
			int end;
			final Choice choice;

			Token(int start, int end, Choice choice) {
				this.start = start;
				this.end = end;
				this.choice = choice;
			}

			boolean matches(String input) {
				return end <= input.length() && input.startsWith(choice.label, start)
						&& (start == 0 || !identifierCharacter(input.codePointBefore(start)))
						&& (end == input.length() || !identifierCharacter(input.codePointAt(end)));
			}

			private static boolean identifierCharacter(int codePoint) {
				return Character.isUnicodeIdentifierPart(codePoint)
						|| codePoint == '\'' || codePoint == '{' || codePoint == '}';
			}
		}
	}
}

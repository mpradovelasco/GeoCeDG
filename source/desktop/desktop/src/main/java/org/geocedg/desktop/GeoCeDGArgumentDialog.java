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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
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
import org.geogebra.common.kernel.arithmetic.SymbolicMode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventListener;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraTree;
import org.geogebra.desktop.gui.view.algebra.AlgebraTreeCellRenderer;
import org.geogebra.desktop.main.AppD;

/** One modeless Desktop argument prompt with a document-local input shield. */
final class GeoCeDGArgumentDialog implements ErrorHandler, EventListener {
	private static final String SESSION = "geocedg.argumentDialog";
	private final AppGeoCeDG app;
	private final JDialog dialog;
	private final JTextField field;
	private final References references;
	private final List<GeoElement> operands;
	private final int mode;
	private final Construction construction;
	private final Object registry;
	private final Consumer<GeoCeDGArgumentDialog> submit;
	private final Map<JRootPane, Component> shields = new IdentityHashMap<>();
	private final JLabel status = new JLabel(" ");
	private final JButton confirm;
	private final JButton pick;
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
		if (!EventQueue.isDispatchThread() || app.isInsertionIsolated()
				|| !(owner(app) instanceof RootPaneContainer owner)
				|| owner.getRootPane().getClientProperty(SESSION) != null
				|| app.getEuclidianView1().getEuclidianController()
						instanceof GeoCeDGEuclidianController controller
						&& controller.referencePointTool().isPanelActive()) {
			return;
		}
		app.closeControlLayout();
		new GeoCeDGArgumentDialog(app, (Window) owner, title, form, field, operands, submit)
				.open(owner.getRootPane());
	}

	private GeoCeDGArgumentDialog(AppGeoCeDG app, Window owner, String title,
			JComponent form, JTextField field, List<GeoElement> operands,
			Consumer<GeoCeDGArgumentDialog> submit) {
		this.app = app;
		this.field = field;
		this.operands = List.copyOf(operands);
		this.submit = submit;
		mode = app.getMode();
		construction = app.getKernel().getConstruction();
		registry = construction.getSpatialIdentityRegistry();
		references = new References(construction);
		dialog = new JDialog(owner, title, java.awt.Dialog.ModalityType.MODELESS);
		dialog.setName(SESSION);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		confirm = new JButton(app.getLocalization().getMenu("OK"));
		confirm.addActionListener(event -> confirm());
		JButton cancel = new JButton(app.getLocalization().getMenu("Cancel"));
		cancel.addActionListener(event -> finish());
		pick = new JButton(text("Button"));
		pick.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(pick.getText().charAt(0)));
		pick.addActionListener(event -> arm());
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
		dialog.getRootPane().putClientProperty(SESSION, this);
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

	private void open(JRootPane owner) {
		if (!(field.getDocument() instanceof AbstractDocument document)
				|| document.getDocumentFilter() != null) {
			dialog.dispose();
			return;
		}
		document.setDocumentFilter(references);
		var events = app.getEventDispatcher();
		var focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		PropertyChangeListener focusListener = event -> {
			if (armed && event.getNewValue() instanceof Window window && !ownsFocus(window)) {
				armed = false;
				resetError();
			}
		};
		events.addEventListener(this);
		focusManager.addPropertyChangeListener("focusedWindow", focusListener);
		loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
		try {
			if (!shield(owner)) {
				return;
			}
			for (var panel : ((GuiManagerD) app.getGuiManager()).getLayout()
					.getDockManager().getPanels()) {
				if (panel.getFrame() instanceof RootPaneContainer detached
						&& !shield(detached.getRootPane())) {
					return;
				}
			}
			app.setComponentOrientation(dialog);
			dialog.pack();
			dialog.setLocationRelativeTo(dialog.getOwner());
			dialog.setVisible(true);
			// The first field of the form keeps normal keyboard order.
			if (!finished) {
				loop.enter();
			}
		} finally {
			finished = true;
			dialog.dispose();
			shields.forEach((root, previous) -> {
				if (root.getClientProperty(SESSION) == this) {
					root.getGlassPane().setVisible(false);
					root.setGlassPane(previous);
					root.putClientProperty(SESSION, null);
				}
			});
			events.removeEventListener(this);
			focusManager.removePropertyChangeListener("focusedWindow", focusListener);
			document.setDocumentFilter(null);
		}
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
		if (!processing && operandsCurrent()) {
			armed = true;
			showError(text("Armed"));
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
			showError(text("None"));
			return;
		}
		List<References.Choice> candidates = choices.stream().map(references::offer).toList();
		References.Choice selected = candidates.get(0);
		if (candidates.size() > 1) {
			Object answer = JOptionPane.showInputDialog(dialog, text("Armed"), dialog.getTitle(),
					JOptionPane.QUESTION_MESSAGE, null, candidates.toArray(), null);
			if (!(answer instanceof References.Choice choice)) {
				disarm(null);
				return;
			}
			selected = choice;
		}
		if (!finished && operandsCurrent() && references.insert(field, selected)) {
			disarm(null);
		} else {
			disarm(text("Stale"));
		}
	}

	private static Point localPoint(MouseEvent event, JComponent component) {
		if (!component.isShowing()
				|| SwingUtilities.getWindowAncestor(component)
						!= SwingUtilities.getWindowAncestor(event.getComponent())) {
			return null;
		}
		Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), component);
		return component.getVisibleRect().contains(point) ? point : null;
	}

	private void disarm(String message) {
		armed = false;
		showError(message);
		dialog.toFront();
		field.requestFocusInWindow();
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

	boolean isCurrent() {
		if (finished) {
			return false;
		}
		if (!operandsCurrent()) {
			showError(text("OperandStale"));
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
		finished = true;
		dialog.setVisible(false);
		if (loop != null) {
			loop.exit();
		}
	}

	@Override
	public void sendEvent(Event event) {
		if (event.type == EventType.REMOVE || event.type == EventType.RENAME) {
			references.invalidate(event.target);
			if (event.type == EventType.REMOVE && operands.stream().anyMatch(geo -> geo == event.target)) {
				operandStale = true;
			}
		} else if (event.type == EventType.UNDO || event.type == EventType.REDO) {
			references.invalidateAll();
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
		private final Construction construction;
		private final List<Token> tokens = new ArrayList<>();
		private final List<Choice> offers = new ArrayList<>();

		References(Construction construction) {
			this.construction = construction;
		}

		boolean isLive(GeoElement geo) {
			return geo != null && geo.getConstruction() == construction
					&& !construction.isFileLoading()
					&& geo.isAvailableAtConstructionStep(construction.getStep())
					&& construction.getGeoSetConstructionOrder().stream()
							.anyMatch(current -> current == geo);
		}

		boolean canPick(GeoElement geo) {
			return geo instanceof GeoNumberValue && geo.isLabelSet()
					&& isLive(geo) && labelNames(geo.getLabelSimple(), geo);
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
			return !choice.invalid && isLive(choice.geo) && labelNames(choice.label, choice.geo);
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

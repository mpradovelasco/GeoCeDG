/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.LayerView;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventDispatcher;
import org.geogebra.common.plugin.EventListener;
import org.geogebra.desktop.gui.GuiManagerD;

/** Product-only dialog and one-use context; native Desktop remains the image reader. */
final class GeoCeDGClipboardImageInsertion {
	enum Position { AFTER_CURRENT, APPEND }

	record Choice(int layer, Position position) { }

	record DialogState(int currentLayer, int maxLayer, int currentStep, List<Integer> hiddenLayers) {
		DialogState {
			hiddenLayers = List.copyOf(hiddenLayers);
		}
	}

	@FunctionalInterface
	interface Presentation {
		Choice choose(AppGeoCeDG app, DialogState state);
	}

	@FunctionalInterface
	interface NativeLoader {
		boolean load(AppGeoCeDG app);
	}

	private final AppGeoCeDG app;
	private Presentation presentation = GeoCeDGClipboardImageInsertion::dialog;
	private NativeLoader nativeLoader = target ->
			((GuiManagerD) target.getGuiManager()).loadImage(null, true);

	GeoCeDGClipboardImageInsertion(AppGeoCeDG app) {
		this.app = app;
	}

	void setPresentation(Presentation presentation) {
		this.presentation = Objects.requireNonNull(presentation);
	}

	void setNativeLoader(NativeLoader nativeLoader) {
		this.nativeLoader = Objects.requireNonNull(nativeLoader);
	}

	/** @return null for Cancel; otherwise the certified transaction's outcome */
	GeoCeDGDocumentInsertion.Result insert() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Clipboard image insertion requires the event dispatch thread");
		}
		String refusal = app.getDocumentInsertion().clipboardImageRefusal();
		if (refusal != null) {
			return rejected(refusal);
		}
		try (Request request = new Request(app)) {
			Choice choice = presentation.choose(app, request.dialogState());
			if (choice == null) {
				return null;
			}
			if (choice.position() == null || choice.layer() < 0
					|| choice.layer() > app.getLayerWorkspace().getMaxLayer()) {
				return rejected(app.layerText("ClipboardImage.InvalidChoice"));
			}
			return app.getDocumentInsertion().insertClipboardImage(request, choice, nativeLoader);
		}
	}

	private GeoCeDGDocumentInsertion.Result rejected(String reason) {
		return app.getDocumentInsertion().clipboardRejected(reason, null);
	}

	/** Short-lived host correspondence, never durable geometric identity. */
	static final class Request implements AutoCloseable, EventListener {
		private final AppGeoCeDG app;
		private final Construction construction;
		private final String document;
		private final int step;
		private final ConstructionElement anchor;
		private final int workingLayer;
		private final List<Integer> hiddenLayers;
		private final EuclidianView view;
		private final long historyActivity;
		private final GeoCeDGUndoManager.LazyDefaults defaults;
		private final RequestEvents dispatcher;
		private volatile boolean changed;
		private final Runnable workspaceChanged = () -> changed = true;
		private boolean closed;

		Request(AppGeoCeDG app) {
			this.app = app;
			construction = app.getKernel().getConstruction();
			document = app.getUniqueId();
			step = construction.getStep();
			anchor = step < 0 ? null : construction.getConstructionElement(step);
			workingLayer = app.getLayerWorkspace().getWorkingLayer();
			hiddenLayers = List.copyOf(app.getLayerWorkspace().getHiddenLayers());
			view = app.getActiveEuclidianView();
			historyActivity = ((GeoCeDGUndoManager) construction.getUndoManager()).getHistoryActivity();
			defaults = GeoCeDGUndoManager.LazyDefaults.capture(construction);
			// This passive Kernel view is independent of the application's script
			// dispatcher, which may legitimately already have listeners disabled.
			dispatcher = new RequestEvents(app, workspaceChanged);
			dispatcher.addEventListener(this);
			app.getLayerWorkspace().addListener(workspaceChanged);
		}

		DialogState dialogState() {
			return new DialogState(workingLayer, app.getLayerWorkspace().getMaxLayer(), step, hiddenLayers);
		}

		int step() { return step; }

		boolean isCurrent() {
			return !closed && !changed && construction == app.getKernel().getConstruction()
					&& app.clipboardImageCallbackState() == null
					&& defaults.matchesCurrent()
					&& Objects.equals(document, app.getUniqueId()) && step == construction.getStep()
					&& (step < 0 || anchor == construction.getConstructionElement(step))
					&& view == app.getActiveEuclidianView()
					&& workingLayer == app.getLayerWorkspace().getWorkingLayer()
					&& hiddenLayers.equals(app.getLayerWorkspace().getHiddenLayers())
					&& historyActivity == ((GeoCeDGUndoManager) construction.getUndoManager()).getHistoryActivity();
		}

		@Override
		public void sendEvent(Event event) {
			switch (event.type) {
			case ADD, REMOVE, RENAME, REDEFINE, UPDATE, UPDATE_STYLE, STOREUNDO, UNDO, REDO,
					SET_MODE, SELECT, DESELECT, VIEW_PROPERTIES_CHANGED, VIEW_CHANGED_2D,
					VIEW_CHANGED_3D, PERSPECTIVE_CHANGE, CLEAR, START_ANIMATION, STOP_ANIMATION -> changed = true;
			default -> { }
			}
		}

		@Override
		public void reset() { changed = true; }

		@Override
		public void close() {
			if (!closed) {
				closed = true;
				dispatcher.removeEventListener(this);
				app.getKernel().detach(dispatcher);
				app.getLayerWorkspace().removeListener(workspaceChanged);
			}
		}
	}

	private static final class RequestEvents extends EventDispatcher implements LayerView {
		private final Runnable invalidate;

		RequestEvents(AppGeoCeDG app, Runnable invalidate) {
			super(app);
			this.invalidate = invalidate;
		}

		@Override
		public void changeLayer(GeoElement geo, int oldLayer, int newLayer) {
			if (oldLayer != newLayer) { invalidate.run(); }
		}
	}

	private static Choice dialog(AppGeoCeDG app, DialogState state) {
		Integer[] layers = new Integer[state.maxLayer() + 1];
		for (int i = 0; i < layers.length; i++) { layers[i] = i; }
		JComboBox<Integer> layer = new JComboBox<>(layers);
		layer.setSelectedItem(state.currentLayer());
		layer.setRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> list, Object value,
					int index, boolean selected, boolean focused) {
				super.getListCellRendererComponent(list, value, index, selected, focused);
				if (value instanceof Integer number && state.hiddenLayers().contains(number)) {
					setText(app.layerText("ClipboardImage.Hidden", number.toString()));
				}
				return this;
			}
		});
		JRadioButton after = new JRadioButton(app.layerText("ClipboardImage.AfterStep",
				Integer.toString(state.currentStep() + 1)), true);
		JRadioButton append = new JRadioButton(app.layerText("ClipboardImage.Append"));
		ButtonGroup position = new ButtonGroup();
		position.add(after); position.add(append);
		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
		panel.add(new JLabel(app.layerText("ClipboardImage.Layer")));
		panel.add(layer); panel.add(after); panel.add(append);
		int chosen = JOptionPane.showConfirmDialog(app.getMainComponent(), panel,
				app.layerText("ClipboardImage.Title"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		return chosen == JOptionPane.OK_OPTION
				? new Choice((Integer) layer.getSelectedItem(),
						after.isSelected() ? Position.AFTER_CURRENT : Position.APPEND) : null;
	}
}

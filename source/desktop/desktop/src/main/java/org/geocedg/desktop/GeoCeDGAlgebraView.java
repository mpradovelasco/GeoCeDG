/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.settings.AlgebraStyle;
import org.geogebra.common.util.IndexHTMLBuilder;
import org.geogebra.desktop.gui.view.algebra.AlgebraControllerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraHelperBar;
import org.geogebra.desktop.gui.view.algebra.AlgebraTree;
import org.geogebra.desktop.gui.view.algebra.AlgebraTreeCellRenderer;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewCellRenderer;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.gui.view.algebra.GeoMutableTreeNode;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.ScaledIcon;

/**
 * Classic Algebra view exposing the approved semantic redefine entry point and,
 * since PRE-G9B-R6-plus-A-1, a per-layer eye in Sort by Layer.
 *
 * <p>The eye toggles the session hidden state of the layer; it never writes the
 * visibility of any object, so the marbles keep showing object visibility. The
 * working layer's eye is disabled (AQ-L7).
 */
final class GeoCeDGAlgebraView extends AlgebraViewD {
	private static final long serialVersionUID = 1L;
	private static final int TOOLTIP_CODE_POINTS = 240;
	private final AppGeoCeDG app;
	private GeoCeDGAlgebraSearchBar searchBar;
	/** Whether the current press gesture was consumed by a layer eye. */
	private boolean eyeGesture;
	/** Reference selection is one press/release/click gesture, not an edit. */
	private boolean referenceGesture;

	GeoCeDGAlgebraView(AlgebraControllerD controller, AppGeoCeDG app) {
		super(controller);
		this.app = app;
		app.getLayerWorkspace().addListener(this::layerStateChanged);
	}

	@Override
	public AlgebraHelperBar getHelperBar() {
		if (searchBar == null) {
			searchBar = new GeoCeDGAlgebraSearchBar(this, app);
		}
		return searchBar;
	}

	@Override
	public void setLabels() {
		super.setLabels();
		if (searchBar != null) {
			searchBar.updateLabels();
		}
	}

	@Override
	public void updateFonts() {
		super.updateFonts();
		if (searchBar != null) {
			searchBar.update();
		}
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		GeoElement geo = AlgebraTree.getGeoElementForPath(
				getPathForLocation(event.getX(), event.getY()));
		return geo == null ? null : complementaryToolTip(geo);
	}

	String complementaryToolTip(GeoElement geo) {
		boolean noDefinition = geo.isIndependent() && geo.getDefinition() == null;
		boolean showsValue = noDefinition || app.getAlgebraStyle() == AlgebraStyle.VALUE;
		String heading = app.getLocalization().getMenu(showsValue ? "Definition" : "Value");
		String body;
		if (showsValue) {
			body = noDefinition ? "" : geo.getDefinition(StringTemplate.defaultTemplate);
			if (body == null || body.isBlank()) {
				body = app.layerText("Algebra.Tooltip.NoDefinition");
			}
		} else {
			body = geo.isDefined() ? Objects.toString(
					geo.toValueString(StringTemplate.defaultTemplate), "")
					: app.layerText("Algebra.Tooltip.Undefined");
		}
		boolean shortened = body.codePointCount(0, body.length()) > TOOLTIP_CODE_POINTS;
		String display = shortened
				? body.substring(0, body.offsetByCodePoints(0, TOOLTIP_CODE_POINTS)) + "…" : body;
		if (!shortened && !geo.isGeoText()) {
			IndexHTMLBuilder builder = new IndexHTMLBuilder(false);
			IndexHTMLBuilder.convertIndicesToHTML(display.replace("&", "&amp;"), builder);
			display = builder.toString();
		} else {
			display = escapeHtml(display);
		}
		String undefined = showsValue && !geo.isDefined()
				? "<br>" + escapeHtml(app.layerText("Algebra.Tooltip.Undefined")) : "";
		return "<html><b>" + escapeHtml(heading) + "</b><br>" + display
				+ undefined + "</html>";
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	@Override
	public void startEditItem(GeoElement geo) {
		if (GeoCeDGDefinitionInspector.isSemanticRedefineEnabled(geo)) {
			app.getDialogManager().showRedefineDialog(geo, true);
			return;
		}
		super.startEditItem(geo);
	}

	@Override
	protected AlgebraTreeCellRenderer newMyRenderer(AppD appD) {
		// Runs inside the AlgebraTree constructor, before this.app is assigned.
		return new LayerRenderer((AppGeoCeDG) appD, this);
	}

	/**
	 * Handles a left press on a layer eye before the host controller, so the
	 * press neither expands the group nor selects its objects.
	 */
	@Override
	protected void processMouseEvent(MouseEvent event) {
		int id = event.getID();
		if (id == MouseEvent.MOUSE_PRESSED) {
			eyeGesture = SwingUtilities.isLeftMouseButton(event)
					&& toggleLayerEyeAt(event.getX(), event.getY());
		}
		if (eyeGesture && (id == MouseEvent.MOUSE_PRESSED
				|| id == MouseEvent.MOUSE_RELEASED || id == MouseEvent.MOUSE_CLICKED)) {
			referenceGesture = false;
			if (id == MouseEvent.MOUSE_CLICKED) {
				eyeGesture = false;
			}
			event.consume();
			return;
		}
		if (id == MouseEvent.MOUSE_PRESSED) {
			referenceGesture = SwingUtilities.isLeftMouseButton(event)
					&& !event.isControlDown() && !event.isShiftDown()
					&& selectReferenceAt(event.getX(), event.getY());
		}
		if (referenceGesture && (id == MouseEvent.MOUSE_PRESSED
				|| id == MouseEvent.MOUSE_RELEASED || id == MouseEvent.MOUSE_CLICKED)) {
			if (id == MouseEvent.MOUSE_CLICKED) { referenceGesture = false; }
			event.consume();
			return;
		}
		super.processMouseEvent(event);
	}

	private boolean selectReferenceAt(int x, int y) {
		TreePath path = getPathForLocation(x, y);
		Rectangle bounds = path == null ? null : getPathBounds(path);
		if (bounds == null || !(path.getLastPathComponent() instanceof GeoMutableTreeNode node)
				// Preserve the native visibility marble on the left of an object row.
				|| x < bounds.x + renderer.getIconShown().getIconWidth()) { return false; }
		var view = app.getActiveEuclidianView();
		return view != null && view.getEuclidianController() instanceof GeoCeDGEuclidianController controller
				&& controller.referencePointTool().selectFromAlgebra(node.getGeo(), this);
	}

	/**
	 * @param x view x
	 * @param y view y
	 * @return whether the point lies on a layer eye; a refused toggle of the
	 *         working layer still belongs to the disabled eye
	 */
	boolean toggleLayerEyeAt(int x, int y) {
		TreePath path = getPathForLocation(x, y);
		if (path == null) {
			return false;
		}
		Integer layer = layerOfGroup(this, path.getLastPathComponent());
		Rectangle bounds = getPathBounds(path);
		if (layer == null || bounds == null) {
			return false;
		}
		int start = bounds.x + expandIconWidth();
		int end = start + renderer.getIconShown().getIconWidth();
		if (x < start || x >= end) {
			return false;
		}
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		workspace.setLayerHidden(layer, workspace.isLayerShown(layer));
		return true;
	}

	private int expandIconWidth() {
		Icon open = renderer.getOpenIcon();
		return open == null ? 0 : open.getIconWidth();
	}

	/** Layer groups change only in text and icon; their objects are unchanged. */
	private void layerStateChanged() {
		if (getTreeMode() != SortMode.LAYER || !(getModel() instanceof DefaultTreeModel)) {
			return;
		}
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		Object root = model.getRoot();
		if (root instanceof TreeNode) {
			for (int i = 0; i < ((TreeNode) root).getChildCount(); i++) {
				model.nodeChanged(((TreeNode) root).getChildAt(i));
			}
		}
		repaint();
	}

	/**
	 * @param tree algebra tree
	 * @param value tree value
	 * @return layer of a Sort-by-Layer group node, otherwise null
	 */
	static Integer layerOfGroup(JTree tree, Object value) {
		if (!(tree instanceof AlgebraTree)
				|| ((AlgebraTree) tree).getTreeMode() != SortMode.LAYER
				|| !(value instanceof DefaultMutableTreeNode)
				|| value instanceof GeoMutableTreeNode) {
			return null;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object layer = node.getUserObject();
		return layer instanceof Integer && node.getParent() != null
				&& node.getParent() == tree.getModel().getRoot() ? (Integer) layer : null;
	}

	/** Host renderer plus the layer eye and hidden marker of group nodes. */
	private static final class LayerRenderer extends AlgebraViewCellRenderer {
		private static final long serialVersionUID = 1L;
		private final transient AppGeoCeDG geoCeDG;
		private transient ScaledIcon disabledEye;

		LayerRenderer(AppGeoCeDG app, AlgebraTree view) {
			super(app, view);
			this.geoCeDG = app;
		}

		@Override
		public void update() {
			super.update();
			disabledEye = null;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean itemSelected, boolean expanded, boolean leaf, int row,
				boolean itemHasFocus) {
			Component component = super.getTreeCellRendererComponent(tree, value,
					itemSelected, expanded, leaf, row, itemHasFocus);
			Integer layer = layerOfGroup(tree, value);
			if (layer == null || geoCeDG == null) {
				return component;
			}
			GeoCeDGLayerWorkspace workspace = geoCeDG.getLayerWorkspace();
			boolean shown = workspace.isLayerShown(layer);
			ScaledIcon eye = !shown ? getIconHidden()
					: workspace.isHidingRefused(layer) ? disabledEye() : getIconShown();
			Icon expand = getIcon();
			setIcon(expand instanceof ScaledIcon
					? ScaledIcon.joinIcons((ScaledIcon) expand, eye, this) : eye);
			if (!shown) {
				setText(geoCeDG.layerText("Workspace.Layer.HiddenGroup", getText()));
			}
			return component;
		}

		private ScaledIcon disabledEye() {
			if (disabledEye == null) {
				disabledEye = new ScaledIcon(this);
				disabledEye.setImage(GrayFilter.createDisabledImage(getIconShown().getImage()));
			}
			return disabledEye;
		}
	}
}

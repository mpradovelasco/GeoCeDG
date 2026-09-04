/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.main.AppD;

/** Compact native tool flyouts plus declared non-mode actions; menus remain primary discovery. */
final class GeoCeDGToolbarContainer extends ToolbarContainer {

	private static final long serialVersionUID = 1L;
	private final GeoCeDGWorkspaceController workspace;
	private final AppD app;

	GeoCeDGToolbarContainer(AppD app, GeoCeDGWorkspaceController workspace) {
		super(app, true);
		this.workspace = workspace;
		this.app = app;
	}

	@Override
	public void buildGui() {
		super.buildGui();
		if (workspace != null) {
			boolean horizontal = orientation == SwingConstants.NORTH
					|| orientation == SwingConstants.SOUTH;
			String placement = horizontal ? BorderLayout.WEST : BorderLayout.NORTH;
			Component nativeTools = ((BorderLayout) getLayout()).getLayoutComponent(placement);
			JPanel tools = new JPanel();
			tools.setLayout(new BoxLayout(tools, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
			if (nativeTools != null) {
				remove(nativeTools);
				tools.add(nativeTools);
			}
			tools.add(workspace.createProductToolbar());
			tools.add(GeoCeDGUserTools.createPinnedToolbar(app));
			add(tools, placement);
			revalidate();
		}
	}
}

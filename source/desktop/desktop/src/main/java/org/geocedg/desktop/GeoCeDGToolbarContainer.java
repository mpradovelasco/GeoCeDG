/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.main.AppD;

/** Adds a scrollable declarative action palette without inventing kernel modes. */
final class GeoCeDGToolbarContainer extends ToolbarContainer {

	private static final long serialVersionUID = 1L;
	private final GeoCeDGWorkspaceController workspace;

	GeoCeDGToolbarContainer(AppD app, GeoCeDGWorkspaceController workspace) {
		super(app, true);
		this.workspace = workspace;
	}

	@Override
	public void buildGui() {
		super.buildGui();
		if (workspace != null) {
			JScrollPane palette = workspace.createActionPalette();
			JPanel controls = new JPanel(new BorderLayout());
			controls.add(workspace.createWorkspaceSelector(), BorderLayout.WEST);
			controls.add(palette, BorderLayout.CENTER);
			add(controls, orientation == SwingConstants.NORTH || orientation == SwingConstants.SOUTH
					? BorderLayout.SOUTH : BorderLayout.EAST);
			revalidate();
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.io.layout.DockPanelData;
import org.geogebra.common.io.layout.DockSplitPaneData;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.javax.swing.SwingConstants;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.InputPosition;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.move.ggtapi.models.json.JSONTokener;

/**
 * Validated runtime adapter for the GeoCeDG application profile manifest.
 */
public final class GeoCeDGProfile {

	private static final String PROFILE_RESOURCE =
			"/org/geocedg/desktop/application-profile.yml";
	private static final int CUSTOM_PERSPECTIVE_ID = 0;
	private static final ProfileDefinition DEFINITION = loadDefinition();

	private GeoCeDGProfile() {
	}

	/**
	 * @return stable profile identifier
	 */
	public static String getProfileId() {
		return DEFINITION.profileId;
	}

	/**
	 * @return toolbar string compiled from the profile manifest
	 */
	public static String getToolbarDefinition() {
		return DEFINITION.toolbarDefinition;
	}

	/**
	 * @return fresh initial perspective derived from the profile manifest
	 */
	public static Perspective createInitialPerspective() {
		DockSplitPaneData[] splitPanes = {
			new DockSplitPaneData("", DEFINITION.divider,
					SwingConstants.HORIZONTAL_SPLIT)
		};
		DockPanelData[] dockPanels = new DockPanelData[DEFINITION.views.length];
		for (int index = 0; index < DEFINITION.views.length; index++) {
			ViewDefinition view = DEFINITION.views[index];
			dockPanels[index] = new DockPanelData(view.viewId, null, view.visible,
					false, view.viewId == App.VIEW_PROPERTIES,
					AwtFactory.getPrototype().newRectangle(100, 100, 600, 400),
					view.embeddedDefinition, view.embeddedSize);
		}

		Perspective perspective = new GeoCeDGPerspective(DEFINITION.perspectiveId,
				splitPanes, dockPanels, DEFINITION.toolbarDefinition,
				DEFINITION.showToolbar, DEFINITION.showGrid, DEFINITION.showAxes,
				DEFINITION.showInputPanel, DEFINITION.showInputHelp);
		perspective.setUnitAxesRatio(DEFINITION.unitAxesRatio);
		return perspective;
	}

	private static ProfileDefinition loadDefinition() {
		try (InputStream stream = GeoCeDGProfile.class.getResourceAsStream(
				PROFILE_RESOURCE)) {
			if (stream == null) {
				throw new IllegalStateException(
						"GeoCeDG profile resource is missing: " + PROFILE_RESOURCE);
			}
			String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			return parseDefinition(new JSONObject(new JSONTokener(json)));
		} catch (IOException | JSONException exception) {
			throw new IllegalStateException("Cannot load GeoCeDG profile", exception);
		}
	}

	private static ProfileDefinition parseDefinition(JSONObject root) throws JSONException {
		if (root.getInt("schema_version") != 1) {
			throw new IllegalStateException("Unsupported GeoCeDG profile version");
		}
		String profileId = root.getString("profile_id");
		JSONObject application = root.getJSONObject("application");
		JSONObject serialization = root.getJSONObject("serialization");
		if (!AppConfigGeoCeDG.PROFILE_ID.equals(profileId)
				|| !AppConfigGeoCeDG.APPLICATION_NAME.equals(
						application.getString("name"))
				|| !AppConfigGeoCeDG.PREFERENCES_KEY.equals(
						application.getString("preferences_key"))
				|| !GeoGebraConstants.CLASSIC_APPCODE.equals(
						serialization.getString("app_code"))) {
			throw new IllegalStateException("GeoCeDG profile identity is inconsistent");
		}

		JSONObject perspective = root.getJSONObject("perspective");
		JSONArray viewArray = perspective.getJSONArray("views");
		ViewDefinition[] views = new ViewDefinition[viewArray.length()];
		Set<Integer> viewIds = new HashSet<>();
		for (int index = 0; index < viewArray.length(); index++) {
			JSONObject view = viewArray.getJSONObject(index);
			int viewId = toViewId(view.getString("id"));
			if (!viewIds.add(viewId)) {
				throw new IllegalStateException("Duplicate GeoCeDG view: " + viewId);
			}
			views[index] = new ViewDefinition(viewId, view.getBoolean("visible"),
					view.getString("embedded_definition"),
					view.getInt("embedded_size"));
		}
		if (!viewIds.contains(App.VIEW_EUCLIDIAN)
				|| !viewIds.contains(App.VIEW_ALGEBRA)) {
			throw new IllegalStateException("GeoCeDG requires Euclidian and Algebra views");
		}

		String toolbarDefinition = compileToolbar(
				root.getJSONObject("toolbar").getJSONArray("categories"));
		return new ProfileDefinition(profileId, perspective.getString("id"),
				perspective.getDouble("divider"),
				views, toolbarDefinition, perspective.getBoolean("show_toolbar"),
				perspective.getBoolean("show_grid"),
				perspective.getBoolean("show_axes"),
				perspective.getBoolean("unit_axes_ratio"),
				perspective.getBoolean("show_input_panel"),
				perspective.getBoolean("show_input_help"));
	}

	private static String compileToolbar(JSONArray categories) throws JSONException {
		StringBuilder toolbar = new StringBuilder();
		Set<Integer> modes = new HashSet<>();
		for (int categoryIndex = 0; categoryIndex < categories.length(); categoryIndex++) {
			JSONArray categoryModes = categories.getJSONObject(categoryIndex)
					.getJSONArray("modes");
			if (categoryModes.length() == 0) {
				throw new IllegalStateException("Empty GeoCeDG toolbar category");
			}
			if (toolbar.length() > 0) {
				toolbar.append(" | ");
			}
			for (int modeIndex = 0; modeIndex < categoryModes.length(); modeIndex++) {
				int mode = categoryModes.getInt(modeIndex);
				if (!modes.add(mode)) {
					throw new IllegalStateException(
							"Duplicate GeoCeDG toolbar mode: " + mode);
				}
				if (modeIndex > 0) {
					toolbar.append(' ');
				}
				toolbar.append(mode);
			}
		}
		return toolbar.toString();
	}

	private static int toViewId(String viewId) {
		switch (viewId) {
		case "euclidian":
			return App.VIEW_EUCLIDIAN;
		case "algebra":
			return App.VIEW_ALGEBRA;
		case "spreadsheet":
			return App.VIEW_SPREADSHEET;
		case "cas":
			return App.VIEW_CAS;
		case "properties":
			return App.VIEW_PROPERTIES;
		case "euclidian3d":
			return App.VIEW_EUCLIDIAN3D;
		default:
			throw new IllegalStateException("Unknown GeoCeDG view: " + viewId);
		}
	}

	private static final class ProfileDefinition {
		private final String profileId;
		private final String perspectiveId;
		private final double divider;
		private final ViewDefinition[] views;
		private final String toolbarDefinition;
		private final boolean showToolbar;
		private final boolean showGrid;
		private final boolean showAxes;
		private final boolean unitAxesRatio;
		private final boolean showInputPanel;
		private final boolean showInputHelp;

		private ProfileDefinition(String profileId, String perspectiveId,
				double divider,
				ViewDefinition[] views, String toolbarDefinition,
				boolean showToolbar, boolean showGrid, boolean showAxes,
				boolean unitAxesRatio, boolean showInputPanel,
				boolean showInputHelp) {
			this.profileId = profileId;
			this.perspectiveId = perspectiveId;
			this.divider = divider;
			this.views = views;
			this.toolbarDefinition = toolbarDefinition;
			this.showToolbar = showToolbar;
			this.showGrid = showGrid;
			this.showAxes = showAxes;
			this.unitAxesRatio = unitAxesRatio;
			this.showInputPanel = showInputPanel;
			this.showInputHelp = showInputHelp;
		}
	}

	/**
	 * Product defaults must apply their axes/grid policy. Upstream custom
	 * document perspectives deliberately do not.
	 */
	private static final class GeoCeDGPerspective extends Perspective {

		private final String profilePerspectiveId;

		private GeoCeDGPerspective(String perspectiveId,
				DockSplitPaneData[] splitPanes, DockPanelData[] dockPanels,
				String toolbarDefinition, boolean showToolbar, boolean showGrid,
				boolean showAxes, boolean showInputPanel, boolean showInputHelp) {
			super(CUSTOM_PERSPECTIVE_ID, splitPanes, dockPanels,
					toolbarDefinition, showToolbar, showGrid, showAxes,
					showInputPanel, showInputHelp, InputPosition.algebraView);
			this.profilePerspectiveId = perspectiveId;
		}

		@Override
		public boolean isUserDefined() {
			return false;
		}

		@Override
		public String getId() {
			return profilePerspectiveId;
		}

		@Override
		public String getSlug() {
			return profilePerspectiveId;
		}
	}

	private static final class ViewDefinition {
		private final int viewId;
		private final boolean visible;
		private final String embeddedDefinition;
		private final int embeddedSize;

		private ViewDefinition(int viewId, boolean visible,
				String embeddedDefinition, int embeddedSize) {
			this.viewId = viewId;
			this.visible = visible;
			this.embeddedDefinition = embeddedDefinition;
			this.embeddedSize = embeddedSize;
		}
	}
}

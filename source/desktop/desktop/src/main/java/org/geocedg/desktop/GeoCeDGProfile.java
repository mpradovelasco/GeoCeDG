/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
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

	/** @return immutable action definitions from the one live catalog */
	public static List<ActionDefinition> getActions() {
		return DEFINITION.actions;
	}

	/** @return whether the packaged v2 profile failed closed to the frozen v1 profile */
	public static boolean isLegacyFallback() {
		return DEFINITION.legacyFallback;
	}

	/** @return explicit degraded-mode diagnostic; no replacement action catalog is created */
	public static String getFallbackDiagnostic(String language) {
		return "es".equals(language)
				? "Perfil v2 no disponible. GeoCeDG usa el perfil v1 validado; "
						+ "el espacio de trabajo v2 no est\u00e1 disponible."
				: "Profile v2 unavailable. GeoCeDG is using the validated v1 profile; "
						+ "the v2 workspace is unavailable.";
	}

	/**
	 * @param id stable action ID
	 * @return its validated definition
	 */
	public static ActionDefinition getAction(String id) {
		return getActions().stream().filter(action -> action.id().equals(id))
				.findFirst().orElseThrow(() -> new IllegalArgumentException(id));
	}

	/** @return detached profile data for the frontend placement compiler */
	static JSONObject getCatalog() {
		try {
			return new JSONObject(DEFINITION.catalog.toString());
		} catch (JSONException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * @param key profile-owned localization key
	 * @param language requested language; unsupported languages use English
	 * @return required localized text, never an opaque internal fallback key
	 */
	public static String getText(String key, String language) {
		if (isLegacyFallback()) {
			return getFallbackDiagnostic(language);
		}
		try {
			return DEFINITION.catalog.getJSONObject("localized_text").getJSONObject(key)
					.getString("es".equals(language) ? "es" : "en");
		} catch (JSONException exception) {
			throw new IllegalArgumentException("Missing GeoCeDG text: " + key, exception);
		}
	}

	/** Validated declarative action; no geometry or solver lives in this catalog. */
	public record ActionDefinition(String id, String kind, String target, Integer mode,
			List<String> features, String selection, String availability, String textKey,
			String iconKey) {
	}

	/**
	 * @return fresh initial perspective derived from the profile manifest
	 */
	public static Perspective createInitialPerspective() {
		DockSplitPaneData[] splitPanes = DEFINITION.splitPanes.clone();
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
		return loadDefinition(readProfileResource(PROFILE_RESOURCE),
				readProfileResource("/org/geocedg/desktop/application-profile-v1.yml"));
	}

	static ProfileDefinition loadDefinition(String current, String fallback) {
		try {
			JSONObject root = new JSONObject(new JSONTokener(current));
			if (root.getInt("schema_version") != 2) {
				throw new IllegalStateException("The live profile must use schema v2");
			}
			return parseDefinition(root);
		} catch (JSONException | IllegalStateException exception) {
			try {
				JSONObject root = new JSONObject(new JSONTokener(fallback));
				if (root.getInt("schema_version") != 1) {
					throw new IllegalStateException("The fallback profile must use schema v1");
				}
				ProfileDefinition definition = parseDefinition(root);
				definition.legacyFallback = true;
				return definition;
			} catch (JSONException | IllegalStateException fallbackFailure) {
				fallbackFailure.addSuppressed(exception);
				throw new IllegalStateException("No validated GeoCeDG profile", fallbackFailure);
			}
		}
	}

	private static String readProfileResource(String resource) {
		try (InputStream stream = GeoCeDGProfile.class.getResourceAsStream(resource)) {
			return stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			return "";
		}
	}

	private static ProfileDefinition parseDefinition(JSONObject root) throws JSONException {
		int version = root.getInt("schema_version");
		if (version != 1 && version != 2) {
			throw new IllegalStateException("Unsupported GeoCeDG profile version");
		}
		GeoCeDGProfileSchema.validate(root, readSchema(version));
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

		JSONObject perspective = root.getJSONObject(version == 1
				? "perspective" : "runtime_layout");
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

		List<ActionDefinition> actions = version == 2 ? compileActions(root) : List.of();
		String toolbarDefinition = version == 1 ? compileToolbar(
				root.getJSONObject("toolbar").getJSONArray("categories"))
				: compileActionToolbar(root, actions);
		ProfileDefinition definition = new ProfileDefinition(profileId,
				perspective.getString("id"),
				perspective.getDouble("divider"),
				views, toolbarDefinition, perspective.getBoolean("show_toolbar"),
				perspective.getBoolean("show_grid"),
				perspective.getBoolean("show_axes"),
				perspective.getBoolean("unit_axes_ratio"),
				perspective.getBoolean("show_input_panel"),
				perspective.getBoolean("show_input_help"));
		definition.actions = List.copyOf(actions);
		definition.catalog = root;
		definition.splitPanes = compileSplitPanes(perspective, version, views);
		return definition;
	}

	private static DockSplitPaneData[] compileSplitPanes(JSONObject perspective,
			int version, ViewDefinition[] views) throws JSONException {
		if (version == 1) {
			return new DockSplitPaneData[] {new DockSplitPaneData("",
					perspective.getDouble("divider"), SwingConstants.HORIZONTAL_SPLIT)};
		}
		JSONArray panes = perspective.getJSONArray("split_panes");
		List<DockSplitPaneData> compiled = new ArrayList<>();
		Set<String> locations = new HashSet<>();
		for (int i = 0; i < panes.length(); i++) {
			JSONObject pane = panes.getJSONObject(i);
			String location = pane.getString("location");
			if (!locations.add(location) || (i == 0 && !location.isEmpty())
					|| (i > 0 && !locations.contains(parentLocation(location)))) {
				throw new IllegalStateException("Invalid or unordered dock split tree");
			}
			compiled.add(new DockSplitPaneData(location, pane.getDouble("divider"),
					pane.getInt("orientation")));
		}
		Set<String> occupied = new HashSet<>();
		for (ViewDefinition view : views) {
			if (view.visible) {
				String binary = view.embeddedDefinition.replace('3', '0').replace('2', '1');
				if (locations.contains(binary) || !locations.contains(parentLocation(binary))
						|| !occupied.add(binary)) {
					throw new IllegalStateException("Invalid visible dock panel placement");
				}
			}
		}
		return compiled.toArray(new DockSplitPaneData[0]);
	}

	private static String parentLocation(String location) {
		int comma = location.lastIndexOf(',');
		return comma < 0 ? "" : location.substring(0, comma);
	}

	static String compileProfile(String json) throws JSONException {
		return parseDefinition(new JSONObject(json)).toolbarDefinition;
	}

	private static JSONObject readSchema(int version) throws JSONException {
		String name = version == 1 ? "application-profile-v1.schema.json"
				: "application-profile.schema.json";
		try (InputStream stream = GeoCeDGProfile.class.getResourceAsStream(
				"/org/geocedg/desktop/" + name)) {
			if (stream == null) {
				throw new IllegalStateException("Missing profile schema " + name);
			}
			return new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static List<ActionDefinition> compileActions(JSONObject root)
			throws JSONException {
		JSONArray array = root.getJSONArray("actions");
		List<ActionDefinition> actions = new ArrayList<>();
		Set<String> ids = new HashSet<>();
		Set<Integer> modes = new HashSet<>();
		JSONObject texts = root.getJSONObject("localized_text");
		JSONObject policies = root.getJSONObject("policy_profiles");
		for (int i = 0; i < array.length(); i++) {
			JSONObject action = array.getJSONObject(i);
			String id = action.getString("id");
			if (!ids.add(id)) {
				throw new IllegalStateException("Duplicate action " + id);
			}
			JSONObject target = action.getJSONObject("target");
			String symbol = target.getString("symbol");
			String kind = action.getString("kind");
			Integer mode = null;
			if ("upstream-mode".equals(kind)) {
				mode = target.getInt("audited_numeric_id");
				try {
					if (EuclidianConstants.class.getField(symbol).getInt(null) != mode
							|| !modes.add(mode)) {
						throw new IllegalStateException("Mismatched/duplicate mode " + id);
					}
				} catch (ReflectiveOperationException exception) {
					throw new IllegalStateException("Unknown mode " + symbol, exception);
				}
			} else {
				for (String field : List.of("name", "short_help", "long_help", "status", "error")) {
					texts.getJSONObject(action.getString("localization_ref") + "." + field);
				}
			}
			for (String[] pair : new String[][] {{"effects", "effect_profile_id"},
					{"availability", "availability_profile_id"},
					{"command_surfaces", "command_surface_profile_id"}}) {
				requireReference(policies.getJSONArray(pair[0]), action.getString(pair[1]));
			}
			actions.add(new ActionDefinition(id, kind, symbol, mode,
					strings(action.getJSONArray("feature_requirements")),
					action.getString("selection_contract_ref"),
					action.getString("availability_profile_id"),
					action.getString("localization_ref"), action.getString("icon_ref")));
		}
		if (actions.size() != 110) {
			throw new IllegalStateException("Approved G9U1 catalog requires 110 actions");
		}
		validatePlacements(root, ids);
		return actions;
	}

	private static void validatePlacements(JSONObject root, Set<String> ids)
			throws JSONException {
		JSONArray clusters = root.getJSONArray("clusters");
		JSONArray families = root.getJSONObject("taxonomy").getJSONArray("broad_families");
		Set<String> clusterIds = new HashSet<>();
		Set<String> placed = new HashSet<>();
		Set<String> declaredToolbarActions = new HashSet<>();
		for (int i = 0; i < clusters.length(); i++) {
			JSONObject cluster = clusters.getJSONObject(i);
			if (!clusterIds.add(cluster.getString("id"))) {
				throw new IllegalStateException("Duplicate cluster");
			}
			requireReference(families, cluster.getString("broad_family_id"));
			for (String key : cluster.keySet()) {
				if (key.endsWith("_action_ids")) {
					for (String actionId : strings(cluster.getJSONArray(key))) {
						if (!ids.contains(actionId)) {
							throw new IllegalStateException("Unknown placement " + actionId);
						}
						placed.add(actionId);
						if ("toolbar_action_ids".equals(key)
								&& !declaredToolbarActions.add(actionId)) {
							throw new IllegalStateException(
									"Duplicate toolbar action placement " + actionId);
						}
					}
				}
			}
		}
		if (!placed.equals(ids)) {
			throw new IllegalStateException("Unplaced workspace action");
		}
		JSONArray presentationGroups = root.getJSONArray("presentation_groups");
		Set<String> presentationGroupIds = new HashSet<>();
		Set<String> presentationActions = new HashSet<>();
		Set<String> toolbarActions = new HashSet<>();
		for (int i = 0; i < presentationGroups.length(); i++) {
			JSONObject group = presentationGroups.getJSONObject(i);
			String groupId = group.getString("id");
			if (!presentationGroupIds.add(groupId)) {
				throw new IllegalStateException("Duplicate presentation group " + groupId);
			}
			root.getJSONObject("localized_text").getJSONObject(group.getString("name_key"));
			Set<String> groupActions = new HashSet<>();
			for (String actionId : strings(group.getJSONArray("action_ids"))) {
				if (!ids.contains(actionId) || !groupActions.add(actionId)
						|| !presentationActions.add(actionId)) {
					throw new IllegalStateException(
							"Unknown/duplicate presentation action " + actionId);
				}
			}
			for (String actionId : strings(group.getJSONArray("toolbar_action_ids"))) {
				if (!groupActions.contains(actionId) || !toolbarActions.add(actionId)) {
					throw new IllegalStateException(
							"Unknown/duplicate toolbar presentation action " + actionId);
				}
			}
		}
		if (!presentationActions.equals(ids)) {
			throw new IllegalStateException("Presentation does not cover the action catalog");
		}
		Set<String> toolbarGroups = new HashSet<>();
		Set<String> referencedToolbarActions = new HashSet<>();
		for (String groupId : strings(root.getJSONArray("toolbar_group_ids"))) {
			if (!presentationGroupIds.contains(groupId) || !toolbarGroups.add(groupId)) {
				throw new IllegalStateException("Unknown/duplicate toolbar group " + groupId);
			}
			if (find(presentationGroups, groupId).getJSONArray("toolbar_action_ids").length()
					== 0) {
				throw new IllegalStateException("Empty toolbar group " + groupId);
			}
			referencedToolbarActions.addAll(strings(find(presentationGroups, groupId)
					.getJSONArray("toolbar_action_ids")));
		}
		if (!referencedToolbarActions.equals(toolbarActions)) {
			throw new IllegalStateException("Unreachable toolbar presentation action");
		}
		if (!toolbarActions.equals(declaredToolbarActions)) {
			throw new IllegalStateException(
					"Toolbar presentation does not match the operational catalog");
		}

		Set<String> menuClusters = new HashSet<>();
		Set<String> menuSectionIds = new HashSet<>();
		Set<String> menuGroups = new HashSet<>();
		Set<String> menuActions = new HashSet<>();
		Set<String> specialEntries = new HashSet<>();
		int userToolsEntries = 0;
		JSONArray sections = root.getJSONArray("menu_sections");
		for (int i = 0; i < sections.length(); i++) {
			JSONObject section = sections.getJSONObject(i);
			if (!menuSectionIds.add(section.getString("id"))) {
				throw new IllegalStateException("Duplicate menu section");
			}
			root.getJSONObject("localized_text").getJSONObject(section.getString("name_key"));
			for (String id : strings(section.getJSONArray("cluster_ids"))) {
				if (!clusterIds.contains(id) || !menuClusters.add(id)) {
					throw new IllegalStateException("Unknown/duplicate menu cluster " + id);
				}
			}
			JSONArray deprecatedActions = section.optJSONArray("action_ids");
			if (deprecatedActions != null && deprecatedActions.length() != 0) {
				throw new IllegalStateException("Menu actions must use presentation groups");
			}
			JSONArray entries = section.getJSONArray("entries");
			boolean separator = true;
			for (int e = 0; e < entries.length(); e++) {
				JSONObject entry = entries.getJSONObject(e);
				String kind = entry.getString("kind");
				if ("user-tools".equals(kind) && ++userToolsEntries > 1) {
					throw new IllegalStateException("Duplicate dynamic user-tools menu entry");
				}
				boolean grouped = "group".equals(kind) || "actions".equals(kind)
						|| "user-tools".equals(kind);
				if (grouped != entry.has("group_id")) {
					throw new IllegalStateException("Invalid menu entry " + kind);
				}
				if (grouped) {
					String groupId = entry.getString("group_id");
					if (!presentationGroupIds.contains(groupId) || !menuGroups.add(groupId)) {
						throw new IllegalStateException(
								"Unknown/duplicate menu presentation group " + groupId);
					}
					for (String actionId : strings(find(presentationGroups, groupId)
							.getJSONArray("action_ids"))) {
						if (!menuActions.add(actionId)) {
							throw new IllegalStateException(
									"Duplicate menu action " + actionId);
						}
					}
					separator = false;
				} else if ("separator".equals(kind)) {
					if (separator || e == entries.length() - 1) {
						throw new IllegalStateException("Invalid menu separator");
					}
					separator = true;
				} else {
					if (!Set.of("workspace-switcher", "user-tools", "host-views",
							"sort-by", "rounding", "labeling", "font-size",
							"save-settings").contains(kind)) {
						throw new IllegalStateException("Unknown menu entry " + kind);
					}
					if ("workspace-switcher".equals(kind) && !specialEntries.add(kind)) {
						throw new IllegalStateException("Duplicate special menu entry " + kind);
					}
					separator = false;
				}
			}
		}
		if (!menuClusters.equals(clusterIds)) {
			throw new IllegalStateException("Unreachable menu cluster");
		}
		if (userToolsEntries != 1) {
			throw new IllegalStateException(
					"Exactly one dynamic user-tools menu entry is required");
		}
		if (!menuActions.equals(ids) || !menuGroups.equals(presentationGroupIds)) {
			throw new IllegalStateException("Menu presentation does not cover the catalog");
		}
		JSONArray workspaces = root.getJSONArray("workspaces");
		requireReference(workspaces, root.getString("default_workspace_id"));
		for (int i = 0; i < workspaces.length(); i++) {
			JSONObject workspace = workspaces.getJSONObject(i);
			for (String id : strings(workspace.getJSONArray("menu_cluster_ids"))) {
				requireReference(clusters, id);
			}
			for (String id : strings(workspace.getJSONArray("toolbar_broad_family_ids"))) {
				requireReference(families, id);
			}
		}
	}

	private static void requireReference(JSONArray array, String id) throws JSONException {
		int count = 0;
		for (int i = 0; i < array.length(); i++) {
			if (id.equals(array.getJSONObject(i).getString("id"))) {
				count++;
			}
		}
		if (count != 1) {
			throw new IllegalStateException("Unresolved/nonunique profile reference " + id);
		}
	}

	static List<String> strings(JSONArray array) throws JSONException {
		List<String> values = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			values.add(array.getString(i));
		}
		return List.copyOf(values);
	}

	private static String compileActionToolbar(JSONObject root,
			List<ActionDefinition> actions) throws JSONException {
		Map<String, ActionDefinition> byId = new LinkedHashMap<>();
		for (ActionDefinition action : actions) {
			byId.put(action.id(), action);
		}
		List<String> groups = new ArrayList<>();
		Set<Integer> included = new HashSet<>();
		JSONArray presentationGroups = root.getJSONArray("presentation_groups");
		for (String groupId : strings(root.getJSONArray("toolbar_group_ids"))) {
			List<String> group = new ArrayList<>();
			for (String id : strings(find(presentationGroups, groupId)
					.getJSONArray("toolbar_action_ids"))) {
				Integer mode = byId.get(id).mode();
				if (mode != null && included.add(mode)) {
					group.add(mode.toString());
				}
			}
			if (!group.isEmpty()) {
				groups.add(String.join(" ", group));
			}
		}
		return String.join(" | ", groups);
	}

	private static JSONObject find(JSONArray array, String id) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			JSONObject item = array.getJSONObject(i);
			if (id.equals(item.getString("id"))) {
				return item;
			}
		}
		throw new IllegalStateException("Unresolved profile reference " + id);
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
		case "construction-protocol":
			return App.VIEW_CONSTRUCTION_PROTOCOL;
		default:
			throw new IllegalStateException("Unknown GeoCeDG view: " + viewId);
		}
	}

	static final class ProfileDefinition {
		boolean legacyFallback;

		String toolbar() {
			return toolbarDefinition;
		}

		int actionCount() {
			return actions.size();
		}

		private DockSplitPaneData[] splitPanes;
		private JSONObject catalog;
		private List<ActionDefinition> actions;
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

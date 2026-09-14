/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.EnumMap;
import java.util.Map;

import org.geogebra.common.util.Util;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/** Independent user preferences for bounded application presentation sizes. */
final class GeoCeDGPresentationPreferences {

	private static final int[] TOOLBAR_ICON_SIZES = {
			16, 20, 24, 28, 32, 40, 48, 56, 64
	};

	enum Category {
		MENU_FONT("geocedg.presentation.menu-font-size.v1", false),
		TOOLBAR_ICON("geocedg.presentation.toolbar-icon-size.v1", true),
		ALGEBRA_FONT("geocedg.presentation.algebra-font-size.v1", false),
		CONSTRUCTION_PROTOCOL_FONT(
				"geocedg.presentation.construction-protocol-font-size.v1", false),
		GRAPHICS_FONT("geocedg.presentation.graphics-font-size.v1", false);

		private final String preferenceKey;
		private final boolean iconSize;

		Category(String preferenceKey, boolean iconSize) {
			this.preferenceKey = preferenceKey;
			this.iconSize = iconSize;
		}

		String preferenceKey() {
			return preferenceKey;
		}
	}

	record InheritedValues(int menuFont, int toolbarIcon, int algebraFont,
			int constructionProtocolFont, int graphicsFont) {
		int get(Category category) {
			return switch (category) {
			case MENU_FONT -> menuFont;
			case TOOLBAR_ICON -> toolbarIcon;
			case ALGEBRA_FONT -> algebraFont;
			case CONSTRUCTION_PROTOCOL_FONT -> constructionProtocolFont;
			case GRAPHICS_FONT -> graphicsFont;
			};
		}
	}

	interface Store {
		String load(String key);

		void save(String key, String value);
	}

	private final Store store;
	private final Map<Category, Integer> values = new EnumMap<>(Category.class);

	GeoCeDGPresentationPreferences(InheritedValues inheritedValues) {
		this(systemStore(), inheritedValues);
	}

	GeoCeDGPresentationPreferences(Store store, InheritedValues inheritedValues) {
		this.store = store;
		for (Category category : Category.values()) {
			int inherited = normalize(category, inheritedValues.get(category));
			String stored = store.load(category.preferenceKey());
			int value = decode(category, stored, inherited);
			values.put(category, value);
			if (!Integer.toString(value).equals(stored)) {
				store.save(category.preferenceKey(), Integer.toString(value));
			}
		}
	}

	int get(Category category) {
		return values.get(category);
	}

	void set(Category category, int value) {
		if (!isSupported(category, value)) {
			throw new IllegalArgumentException("Unsupported " + category + " size: " + value);
		}
		store.save(category.preferenceKey(), Integer.toString(value));
		values.put(category, value);
	}

	static int[] supportedSizes(Category category) {
		if (category.iconSize) {
			return TOOLBAR_ICON_SIZES.clone();
		}
		boolean constructionCompatible = category == Category.GRAPHICS_FONT;
		int[] sizes = new int[constructionCompatible
				? Util.appFontSizesLength() : Util.menuFontSizesLength()];
		for (int index = 0; index < sizes.length; index++) {
			sizes[index] = constructionCompatible
					? Util.appFontSizes(index) : Util.menuFontSizes(index);
		}
		return sizes;
	}

	private static int decode(Category category, String stored, int fallback) {
		try {
			int value = Integer.parseInt(stored);
			return isSupported(category, value) ? value : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	private static boolean isSupported(Category category, int value) {
		for (int supported : supportedSizes(category)) {
			if (supported == value) {
				return true;
			}
		}
		return false;
	}

	private static int normalize(Category category, int inherited) {
		int[] supported = supportedSizes(category);
		int closest = supported[0];
		for (int candidate : supported) {
			if (Math.abs(candidate - inherited) < Math.abs(closest - inherited)) {
				closest = candidate;
			}
		}
		return closest;
	}

	private static Store systemStore() {
		return new Store() {
			@Override
			public String load(String key) {
				return GeoGebraPreferencesD.getPref().loadPreference(key, "");
			}

			@Override
			public void save(String key, String value) {
				GeoGebraPreferencesD.getPref().savePreference(key, value);
			}
		};
	}
}

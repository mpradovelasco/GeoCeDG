/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.EnumMap;
import java.util.Map;

import org.geogebra.common.main.GeoGebraPreferences;
import org.geogebra.common.util.Util;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/** Independent user preferences for bounded application presentation sizes. */
final class GeoCeDGPresentationPreferences {

	private static final int[] TOOLBAR_ICON_SIZES = {
			16, 20, 24, 28, 32, 40, 48, 56, 64
	};

	enum Category {
		GENERAL_UI_FONT("geocedg.presentation.general-ui-font-size.v1", false, 12),
		MENU_FONT("geocedg.presentation.menu-font-size.v1", false, 14),
		TOOLBAR_ICON("geocedg.presentation.toolbar-icon-size.v1", true, 28),
		ALGEBRA_FONT("geocedg.presentation.algebra-font-size.v1", false, 12),
		CONSTRUCTION_PROTOCOL_FONT(
				"geocedg.presentation.construction-protocol-font-size.v1", false, 12),
		GRAPHICS_FONT("geocedg.presentation.graphics-font-size.v1", false, 12);

		private final String preferenceKey;
		private final boolean iconSize;
		private final int freshDefault;

		Category(String preferenceKey, boolean iconSize, int freshDefault) {
			this.preferenceKey = preferenceKey;
			this.iconSize = iconSize;
			this.freshDefault = freshDefault;
		}

		String preferenceKey() {
			return preferenceKey;
		}
	}

	enum ValueSource {
		FRESH_DEFAULT,
		MIGRATED_EFFECTIVE,
		EXPLICIT_PERSISTED
	}

	record InheritedValues(int generalUiFont, int menuFont, int toolbarIcon, int algebraFont,
			int constructionProtocolFont, int graphicsFont) {
		int get(Category category) {
			return switch (category) {
			case GENERAL_UI_FONT -> generalUiFont;
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

		default boolean hasLegacyUserPreferences() {
			return false;
		}
	}

	private final Store store;
	private final Map<Category, Integer> values = new EnumMap<>(Category.class);
	private final Map<Category, ValueSource> sources = new EnumMap<>(Category.class);

	GeoCeDGPresentationPreferences(InheritedValues inheritedValues) {
		this(systemStore(), inheritedValues);
	}

	GeoCeDGPresentationPreferences(Store store, InheritedValues inheritedValues) {
		this.store = store;
		Map<Category, String> storedValues = new EnumMap<>(Category.class);
		boolean existingProfile = store.hasLegacyUserPreferences();
		for (Category category : Category.values()) {
			String stored = store.load(category.preferenceKey());
			storedValues.put(category, stored);
			existingProfile |= stored != null;
		}
		for (Category category : Category.values()) {
			int inherited = normalize(category, inheritedValues.get(category));
			String stored = storedValues.get(category);
			Integer explicit = decode(category, stored);
			int value;
			ValueSource source;
			if (explicit != null) {
				value = explicit;
				source = ValueSource.EXPLICIT_PERSISTED;
			} else if (existingProfile) {
				value = inherited;
				source = ValueSource.MIGRATED_EFFECTIVE;
			} else {
				value = category.freshDefault;
				source = ValueSource.FRESH_DEFAULT;
			}
			values.put(category, value);
			sources.put(category, source);
			if (!Integer.toString(value).equals(stored)) {
				store.save(category.preferenceKey(), Integer.toString(value));
			}
		}
	}

	int get(Category category) {
		return values.get(category);
	}

	ValueSource source(Category category) {
		return sources.get(category);
	}

	void set(Category category, int value) {
		if (!isSupported(category, value)) {
			throw new IllegalArgumentException("Unsupported " + category + " size: " + value);
		}
		store.save(category.preferenceKey(), Integer.toString(value));
		values.put(category, value);
		sources.put(category, ValueSource.EXPLICIT_PERSISTED);
	}

	static int freshDefault(Category category) {
		return category.freshDefault;
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

	private static Integer decode(Category category, String stored) {
		try {
			int value = Integer.parseInt(stored);
			return isSupported(category, value) ? value : null;
		} catch (RuntimeException exception) {
			return null;
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
				return GeoGebraPreferencesD.getPref().loadPreference(key, null);
			}

			@Override
			public void save(String key, String value) {
				GeoGebraPreferencesD.getPref().savePreference(key, value);
			}

			@Override
			public boolean hasLegacyUserPreferences() {
				return GeoGebraPreferencesD.getPref().loadPreference(
						GeoGebraPreferences.XML_USER_PREFERENCES, null) != null;
			}
		};
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/** Standalone launcher for the diagnostic and explicit baseline controls. */
public final class P4ProbeRunner {
	private static final String F3 = "org.geocedg.desktop.PreG9BR6PlusF3InsertionTest#";
	private static final String PROBE = "org.geocedg.desktop.PostCu4P4Probe";

	private P4ProbeRunner() {
	}

	public static void main(String[] args) {
		if (!"-".equals(args[0])) {
			GeoGebraPreferencesD.setPropertyFileName(args[0]);
		}
		List<DiscoverySelector> selectors = switch (args[1]) {
		case "study" -> List.of(DiscoverySelectors.selectClass(PROBE));
		case "catalog" -> List.of(DiscoverySelectors.selectMethod(PROBE
				+ "#reproducesUserCatalogLayerLeakInAnEmptyProfile"));
		case "f3" -> List.of(
				DiscoverySelectors.selectMethod(F3
						+ "f3i01ElementaryInsertionIsOneUndoStepWithIdentityKept"),
				DiscoverySelectors.selectMethod(F3
						+ "f3i02LabelRenamingIsDeterministicAndClosed"),
				DiscoverySelectors.selectMethod(F3
						+ "f3i07LocusV2AndSplineV2ReceiveFreshIdentitiesAndPersist"));
		default -> throw new IllegalArgumentException("Unknown probe selection: " + args[1]);
		};
		var request = LauncherDiscoveryRequestBuilder.request().selectors(selectors);
		if ("study".equals(args[1])) {
			request.filters(TagFilter.excludeTags("synthetic-catalog"));
		}
		var listener = new SummaryGeneratingListener();
		LauncherFactory.create().execute(request.build(), listener);
		var summary = listener.getSummary();
		var writer = new PrintWriter(System.out, true);
		summary.printTo(writer);
		summary.printFailuresTo(writer);
		long found = summary.getTestsFoundCount();
		long expected = switch (args[1]) {
		case "f3" -> 3;
		case "study" -> 8;
		case "catalog" -> 1;
		default -> throw new IllegalArgumentException(args[1]);
		};
		System.exit(found == expected && summary.getTestsSucceededCount() == found
				? 0 : 1);
	}

	/** Keeps the normal Desktop preference backend without accessing the Windows registry. */
	public static final class MemoryPreferencesFactory implements PreferencesFactory {
		private final Preferences user = new MemoryPreferences(null, "");
		private final Preferences system = new MemoryPreferences(null, "");

		@Override
		public Preferences userRoot() {
			return user;
		}

		@Override
		public Preferences systemRoot() {
			return system;
		}
	}

	private static final class MemoryPreferences extends AbstractPreferences {
		private final Map<String, String> values = new HashMap<>();

		MemoryPreferences(AbstractPreferences parent, String name) {
			super(parent, name);
		}

		@Override
		protected void putSpi(String key, String value) {
			values.put(key, value);
		}

		@Override
		protected String getSpi(String key) {
			return values.get(key);
		}

		@Override
		protected void removeSpi(String key) {
			values.remove(key);
		}

		@Override
		protected void removeNodeSpi() {
			values.clear();
		}

		@Override
		protected String[] keysSpi() {
			return values.keySet().toArray(String[]::new);
		}

		@Override
		protected String[] childrenNamesSpi() {
			return new String[0];
		}

		@Override
		protected AbstractPreferences childSpi(String name) {
			return new MemoryPreferences(this, name);
		}

		@Override
		protected void syncSpi() {
			// All writes are already in this process.
		}

		@Override
		protected void flushSpi() {
			// No backing store.
		}
	}
}

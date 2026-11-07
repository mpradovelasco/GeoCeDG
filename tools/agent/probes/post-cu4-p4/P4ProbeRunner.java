/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

import java.io.PrintWriter;
import java.util.ArrayList;

import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/** Standalone launcher for the diagnostic and explicit baseline controls. */
class P4ProbeRunner {
	public static void main(String[] args) {
		GeoGebraPreferencesD.setPropertyFileName(args[0]);
		var selectors = new ArrayList<DiscoverySelector>();
		selectors.add(DiscoverySelectors.selectClass("org.geocedg.desktop.PostCu4P4Probe"));
		for (int i = 1; i < args.length; i++) {
			selectors.add(DiscoverySelectors.selectClass(args[i]));
		}
		var request = LauncherDiscoveryRequestBuilder.request().selectors(selectors).build();
		var listener = new SummaryGeneratingListener();
		LauncherFactory.create().execute(request, listener);
		var summary = listener.getSummary();
		var writer = new PrintWriter(System.out, true);
		summary.printTo(writer);
		summary.printFailuresTo(writer);
		long found = summary.getTestsFoundCount();
		System.exit(found > 0 && summary.getTestsSucceededCount() == found ? 0 : 1);
	}
}

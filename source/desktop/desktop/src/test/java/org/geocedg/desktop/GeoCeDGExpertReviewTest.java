/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;
import javax.swing.JMenuBar;

import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.CommandLineArguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class GeoCeDGExpertReviewTest {
	private static final String ACTION = "document.insert-expert";

	@BeforeAll
	static void initialize() {
		G9U1ActionRegistryTest.initializeDesktop();
	}

	@Test
	void actionIsOptInLocalizedAndChooserCancellationKeepsLiveState() throws Exception {
		onEdt(() -> {
			AppGeoCeDG defaultApp = app(false);
			defaultApp.getGuiManager().initMenubar();
			var defaultMenu = (JMenuBar) defaultApp.getGuiManager().getMenuBar();
			assertNull(G9U1WorkspaceSurfaceTest.findItem(defaultMenu.getMenu(0), ACTION));
			assertNotNull(G9U1WorkspaceSurfaceTest.findItem(defaultMenu.getMenu(0),
					"document.insert-file"));

			AppGeoCeDG enabled = app(true);
			enabled.setLocale(Locale.forLanguageTag("es"));
			enabled.getGuiManager().initMenubar();
			var fileMenu = ((JMenuBar) enabled.getGuiManager().getMenuBar()).getMenu(0);
			var action = G9U1WorkspaceSurfaceTest.findItem(fileMenu, ACTION);
			assertNotNull(action);
			assertEquals("Insertar con revisión experta…", action.getText());
			assertNotNull(action.getAccessibleContext().getAccessibleDescription());
			var construction = enabled.getKernel().getConstruction();
			var undo = (GeoCeDGUndoManager) construction.getUndoManager();
			var commands = undo.getCommands();
			int cursor = undo.getCursor();
			int mode = enabled.getMode();
			String id = enabled.getUniqueId();
			var selected = List.copyOf(enabled.getSelectionManager().getSelectedGeos());
			var path = enabled.getCurrentFile();
			boolean saved = enabled.isSaved();
			AtomicInteger chooserCalls = new AtomicInteger();
			enabled.setInsertionSourceChooser((app, key) -> {
				assertEquals("Insert.Expert.Title", key);
				chooserCalls.incrementAndGet();
				return null;
			});
			action.doClick();
			assertEquals(1, chooserCalls.get());
			assertEquals(commands, undo.getCommands());
			assertEquals(cursor, undo.getCursor());
			assertEquals(selected, enabled.getSelectionManager().getSelectedGeos());
			assertEquals(path, enabled.getCurrentFile());
			assertEquals(mode, enabled.getMode());
			assertEquals(id, enabled.getUniqueId());
			assertEquals(saved, enabled.isSaved());
			assertFalse(enabled.requiresExpertSaveAs());
			assertEquals("Insert with expert review…",
					GeoCeDGProfile.getText("geocedg.document.InsertExpert.name", "en"));
		});
	}

	@Test
	void reviewSeparatesKnownLayerAndViewChangesFromUncertaintyAndIntegrity() throws Exception {
		JSONObject report = new JSONObject("""
				{"stages":[{"phase":"SOURCE","objects":[{"ref":"s","label":"arc","geoClass":"CONIC_PART"}],
				  "entries":[]},{"phase":"TRIAL","objects":[{"ref":"t","label":"arc_1",
				  "geoClass":"CONIC_PART"}],"entries":[]}],
				 "comparisons":[{"before":"SOURCE","after":"TRIAL","status":"VERIFIED_CHANGED",
				  "assessments":[],"additions":[],"unmatchedAfter":[],
				  "objects":[{"beforeRef":"s","afterRef":"t","outcome":"CORRESPONDENCE_PROVEN",
				  "observations":[{"property":"PRESENTATION_FIELDS","status":"VERIFIED_CHANGED",
				  "before":"layer=4,view1=true,view2=true,","after":"layer=0,view1=true,view2=false,"},
				  {"property":"CURRENT_VALUE","status":"NOT_ESTABLISHED","before":"arc","after":"arc"}]}]},
				  {"before":"RECEIVER","after":"TRIAL","status":"VERIFIED_CHANGED","assessments":[],
				  "additions":[{"sourceRef":"s","afterRef":"t","basis":"NATIVE_COPY_TRACE"}],
				  "unmatchedAfter":[],"objects":[]},
				  {"before":"TRIAL","after":"REOPENED","status":"NOT_ESTABLISHED",
				  "assessments":[],"additions":[],"unmatchedAfter":[],"objects":[]}],
				 "admission":{"state":"REJECTED","reason":"P4"},"diagnostics":[]}
				""");
		onEdt(() -> {
			AppGeoCeDG app = app(true);
			app.setLocale(Locale.forLanguageTag("es"));
			var model = GeoCeDGExpertReviewModel.project(app, report, true, "PASSIVE_NATIVE_REPLAY");
			assertTrue(model.summary().contains("1 incorporaciones"));
			assertTrue(model.summary().contains("0 omisiones confirmadas"));
			assertTrue(model.summary().contains("1 observaciones inciertas"));
			assertTrue(model.details().contains("Capa 4 → 0"));
			assertTrue(model.details().contains("vista gráfica 2 incluido → excluido"));
			assertTrue(model.details().contains("Valor actual"));
			report.getJSONArray("diagnostics").put(new JSONObject("""
					{"phase":"TRIAL","status":"INVALID","code":"MALFORMED","detail":"invalid geometry"}
					"""));
			assertThrows(JSONException.class, () -> GeoCeDGExpertReviewModel.project(
					app, report, true, "PASSIVE_NATIVE_REPLAY"));
			var blocked = GeoCeDGExpertReviewModel.project(app, report, false, "NATIVE_EXPERT_COPY");
			assertTrue(blocked.details().contains("Inserción no disponible"));
			assertTrue(blocked.details().contains("MALFORMED"));
			return null;
		});
	}

	@Test
	void sourceEditAndRestoreInvalidatesEvenWhenTheBytesMatch(@TempDir Path directory)
			throws Exception {
		Path source = directory.resolve("original.cedg");
		Files.writeString(source, "original bytes");
		try (var watch = new GeoCeDGExpertWorker.Source(source.toFile())) {
			assertFalse(watch.changed());
			Files.writeString(source, "edited bytes");
			Files.writeString(source, "original bytes");
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
			while (!watch.changed() && System.nanoTime() - deadline < 0) {
				Thread.sleep(10);
			}
			assertTrue(watch.changed());
			assertEquals("original bytes", Files.readString(source));
			assertTrue(watch.changed());
		}
	}

	private static AppGeoCeDG app(boolean expert) {
		return G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
				new CommandLineArguments(new String[]{"--silent", "--enableExpertInsertion=" + expert}),
				new JPanel()));
	}
}

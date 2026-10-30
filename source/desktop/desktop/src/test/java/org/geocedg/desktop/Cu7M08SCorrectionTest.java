/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D.Reason;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Corrective authoring consumers; geometric admission remains kernel-owned. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SCorrectionTest {

	@BeforeAll
	static void initialize() {
		G9U1SemanticPointInteractionTest.initializeDesktop();
	}

	@Test
	void creationAndMovementHaveDifferentLocalizedMessages() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		var result = new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, 20, 20,
						LocusPointInteractionPolicy2D.initial(0.1)));
		assertEquals(Reason.CERTIFIED_ABSENCE, result.getReason());
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLanguage(locale);
			var dialogs = new GeoCeDGLocusV2Dialogs(app);
			String creation = dialogs.interactionMessage(result, true);
			assertTrue(creation.startsWith(GeoCeDGProfile.getText(
					"point.creationNotCreated", locale.getLanguage())));
			assertFalse(creation.equals(dialogs.interactionMessage(result, false)));
			assertFalse(dialogs.interactionMessage(result)
					.equals(dialogs.interactionMessage(result, false)));
			assertTrue(creation.contains(GeoCeDGProfile.getText(
					"point.reason.absent", locale.getLanguage())));
		}
	}

	@Test
	void budgetCauseDoesNotChangeUnresolvedAdmission() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		var result = new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, 0, 0,
						new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
								new LocusPointInteractionWorkBudget2D(1, 2, 1, 1))));
		assertEquals(Reason.BUDGET_EXHAUSTED, result.getReason());
		assertNull(result.getUniqueCandidate());
		assertTrue(new GeoCeDGLocusV2Dialogs(app).interactionMessage(result, true)
				.contains(GeoCeDGProfile.getText("point.reason.budget",
						app.getLocale().getLanguage())));
	}
}

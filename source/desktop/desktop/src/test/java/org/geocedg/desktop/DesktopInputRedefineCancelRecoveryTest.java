/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.desktop.GeoCeDGSpatialRedefineFrontend.ContractChoice;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * TD-DESKTOP-INPUT-REDEFINE-CANCEL-STRANDS-SUBMISSION: every explicit Input
 * submission reaches a terminal state that releases the Input, including a
 * spatial redefine that the user cancels or that is unavailable.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class DesktopInputRedefineCancelRecoveryTest {

	@Test
	void cancelledContractChangeLeavesTheConstructionAndTheInputUsable() {
		AppGeoCeDG app = contractApp();
		RecordingPresentation presentation = install(app);
		AlgebraInputD input = new AlgebraInputD(app);
		String xml = app.getXML();

		enter(input, "s=a+1");

		assertEquals(1, presentation.contractPrompts.size());
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				presentation.contractPrompts.get(0).getStatus());
		assertEquals(xml, app.getXML());
		assertEquals("s=a+1", input.getTextField().getText());
		assertNextEnterCreates(app, input);
	}

	@Test
	void unavailableRedefineLeavesTheConstructionAndTheInputUsable() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"sa=Segment((0,0),(4,0))", "Cs=Point(sa)",
				"K=(0,2)", "ls=LocusV2(Midpoint(Cs,K),Cs)", "t=0.25",
				"Pa=Point(ls,\"generator.main\",t)"}) {
			eval(app, command);
		}
		RecordingPresentation presentation = install(app);
		AlgebraInputD input = new AlgebraInputD(app);
		String xml = app.getXML();

		enter(input, "t=Line((0,0),xAxis)");

		assertEquals(1, presentation.unavailable.size());
		assertTrue(presentation.contractPrompts.isEmpty());
		assertEquals(xml, app.getXML());
		assertEquals(0.25, ((GeoNumeric) lookup(app, "t")).getDouble());
		assertNextEnterCreates(app, input);
	}

	@Test
	void acceptedContractChangeAndOrdinaryErrorsKeepTheirBehaviour() {
		AppGeoCeDG app = contractApp();
		RecordingPresentation presentation = install(app);
		presentation.choice = ContractChoice.RETAIN_IDENTITY;
		AlgebraInputD input = new AlgebraInputD(app);

		enter(input, "s=a+1");

		assertEquals(1, presentation.contractPrompts.size());
		assertEquals("s = a + 1", lookup(app, "s").getDefinitionForInputBar());
		assertEquals("", input.getTextField().getText());
		enter(input, "k=1");
		enter(input, "k=2");
		assertEquals(2, ((GeoNumeric) lookup(app, "k")).getDouble());
		// Command errors open the ordinary modal error dialog; answer it with OK.
		try (MockedStatic<JOptionPane> ignored = Mockito.mockStatic(JOptionPane.class)) {
			for (String invalid : new String[] {"W=(1,", "W=Foo(1)", "W=Circle(3)"}) {
				enter(input, invalid);
				assertEquals(invalid, input.getTextField().getText());
				assertNull(app.getKernel().lookupLabel("W"));
			}
			assertNextEnterCreates(app, input);
		}
	}

	private static void enter(AlgebraInputD input, String text) {
		input.getTextField().setText(text);
		input.keyPressed(new KeyEvent(input, KeyEvent.KEY_PRESSED, 0, 0,
				KeyEvent.VK_ENTER, '\n'));
	}

	private static void assertNextEnterCreates(AppGeoCeDG app, AlgebraInputD input) {
		enter(input, "next=(7,3)");
		assertNotNull(app.getKernel().lookupLabel("next"),
				"The Input must accept the next explicit submission");
		assertEquals("", input.getTextField().getText());
	}

	/** Two loci whose independent drivers {@code s} and {@code a} are durable. */
	private static AppGeoCeDG contractApp() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"s=0", "Q=(s,0)",
				"D={false,{-2,2,true,true}}", "L=LocusV2(Q,s,D)", "a=2", "Qa=(a,a^2)",
				"Da={false,{-2,2,true,true}}", "La=LocusV2(Qa,a,Da)"}) {
			eval(app, command);
		}
		return app;
	}

	private static RecordingPresentation install(AppGeoCeDG app) {
		RecordingPresentation presentation = new RecordingPresentation();
		app.setSpatialRedefinePresentation(presentation);
		return presentation;
	}

	private static final class RecordingPresentation
			implements GeoCeDGSpatialRedefineFrontend.Presentation {
		private final List<SpatialRedefineAssessment> unavailable = new ArrayList<>();
		private final List<SpatialRedefineAssessment> contractPrompts = new ArrayList<>();
		private ContractChoice choice = ContractChoice.CANCEL;

		@Override
		public boolean confirmLegacy(SpatialRedefineAssessment assessment) {
			return false;
		}

		@Override
		public void showUnavailable(SpatialRedefineAssessment assessment) {
			unavailable.add(assessment);
		}

		@Override
		public void showStaleAssessment() {
			// Not exercised by these fixtures.
		}

		@Override
		public ContractChoice chooseContractChange(SpatialRedefineAssessment assessment) {
			contractPrompts.add(assessment);
			return choice;
		}
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDedicatedAuxiliary2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * Post-R1 repair F7: the dedicated selector text of {@code Point(L, u)} is an exclusive parent
 * of that point ({@code locus-v2-point-interaction.md} section 12). Another consumer or a
 * redefinition of it is refused before any mutation, so the persisted identity role of the
 * owner, and with it the ability to save the document, is never lost.
 */
final class PostR1DedicatedAuxiliaryOwnershipTest extends G9U0PublicSurfaceTestBase {

	@Test
	void borrowedSelectorIsRefusedBeforeAnyMutation() {
		Fixture fixture = fixture();
		String before = getApp().getXML();
		int elements = getConstruction().steps();

		String refusal = errorsOf("MP=Point(L," + fixture.selector.getLabelSimple() + ",0.7)");

		assertTrue(refusal.contains("DEDICATED_AUXILIARY_OWNED"), refusal);
		assertTrue(refusal.contains(fixture.selector.getLabelSimple()), refusal);
		assertTrue(refusal.contains("PO"), refusal);
		assertNull(getKernel().lookupLabel("MP"));
		assertEquals(elements, getConstruction().steps(), "no element was added");
		assertEquals(1, fixture.selector.getAlgorithmList().size(), "no consumer was added");
		assertSame(fixture.owner.getParentAlgorithm(),
				fixture.selector.getAlgorithmList().get(0));
		assertEquals(before, getApp().getXML(), "the document is unchanged and savable");
		assertOwned(getKernel(), fixture.ownerId);
	}

	@Test
	void everyOtherConsumerAndTheRedefinitionOfTheAuxiliaryAreRefused() {
		Fixture fixture = fixture();
		String selector = fixture.selector.getLabelSimple();
		String before = getApp().getXML();

		// Another command, an expression and the redefinition of the auxiliary itself.
		for (String command : new String[] {"n=Length(" + selector + ")",
				"t=" + selector + "+\"x\"", selector + "=\"abc\"",
				"SetValue(" + selector + ",\"abc\")"}) {
			String refusal = errorsOf(command);
			assertTrue(refusal.contains("DEDICATED_AUXILIARY_OWNED"), command + ": " + refusal);
			assertEquals(1, fixture.selector.getAlgorithmList().size(), command);
			assertEquals(before, getApp().getXML(), command);
		}
		assertTrue(fixture.owner.isDefined());
		assertOwned(getKernel(), fixture.ownerId);
	}

	@Test
	void publicFormsAndAnOrdinaryTextStayAvailable() {
		Fixture fixture = fixture();
		GeoPoint two = add("MB=Point(L,0.7)");
		GeoPoint three = add("MC=Point(L,\"" + BRANCH + "\",0.7)");
		// A text of the user with the content of the selector is ordinary and shareable.
		GeoText own = add("mySel=\"" + fixture.selector.getTextString() + "\"");
		GeoPoint first = add("MD=Point(L,mySel,0.7)");
		GeoPoint second = add("ME=Point(L,mySel,-0.4)");
		for (GeoPoint point : new GeoPoint[] {two, three, first, second}) {
			assertNotNull(point);
			assertTrue(point.isDefined(), point.getLabelSimple());
		}
		assertNull(LocusDedicatedAuxiliary2D.ownerOf(own), "an ordinary text has no owner");
		assertEquals(2, own.getAlgorithmList().size());
		assertSame(two, LocusDedicatedAuxiliary2D.ownerOf(
				((AlgoSemanticLocusPoint2D) two.getParentAlgorithm()).getBranchInput()));

		// The definition that the product presents for the owner names its selector: the
		// ownership guard admits it. Whether the redefinition itself is accepted is decided
		// by the redefine contract, which this repair does not change.
		String definition = fixture.owner.getRedefineString(false, true);
		assertTrue(definition.endsWith("u)"), definition);
		ErrorAccumulator errors = new ErrorAccumulator();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(fixture.owner,
				definition.substring(0, definition.length() - 2) + "0.2)",
				new EvalInfo(true, true).withSymbolicMode(
						AlgebraProcessor.getRedefinitionMode(fixture.owner, getKernel()))
						.withLabelRedefinitionAllowedFor("PO").withSymbolic(true)
						.withSliders(true),
				false, null, errors);
		assertFalse(errors.getErrors().contains("DEDICATED_AUXILIARY_OWNED"),
				definition + ": " + errors.getErrors());
		GeoElement redefined = getKernel().lookupLabel("PO");
		assertNotNull(redefined);
		assertTrue(redefined.isDefined());

		AppCommon reopened = reopen(getApp().getXML());
		for (String label : new String[] {"PO", "MB", "MC", "MD", "ME"}) {
			GeoElement point = reopened.getKernel().lookupLabel(label);
			assertNotNull(point, label);
			assertTrue(point.isDefined(), label);
		}
	}

	@Test
	void saveReopenAndUndoKeepOwnershipAndIdentity() {
		Fixture fixture = fixture();
		getApp().setUndoActive(true);
		getApp().storeUndoInfo();
		GeoNumeric parameter = (GeoNumeric) getKernel().lookupLabel("u");
		parameter.setValue(0.9);
		parameter.updateCascade();
		getApp().storeUndoInfo();
		getKernel().undo();
		assertOwned(getKernel(), fixture.ownerId);
		getKernel().redo();
		assertOwned(getKernel(), fixture.ownerId);

		AppCommon reopened = reopen(getApp().getXML());
		assertOwned(reopened.getKernel(), fixture.ownerId);
		GeoPoint owner = (GeoPoint) reopened.getKernel().lookupLabel("PO");
		GeoText selector = ((AlgoSemanticLocusPoint2D) owner.getParentAlgorithm())
				.getBranchInput();
		ErrorAccumulator errors = new ErrorAccumulator();
		reopened.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
				"MP=Point(L," + selector.getLabelSimple() + ",0.7)", false, errors, false, null);
		assertTrue(errors.getErrors().contains("DEDICATED_AUXILIARY_OWNED"),
				errors.getErrors());
		assertEquals(getApp().getXML(), reopened.getXML(), "same document after the refusal");
	}

	@Test
	void anAlreadyDamagedDocumentKeepsItsTypedDiagnosticWithoutRepair() {
		Fixture fixture = fixture();
		// The kernel operation itself is not the admission seam: it reproduces the state
		// that an earlier version could leave in memory.
		GeoPoint foreign = LocusV2PublicOperations.createSemanticPoint(getConstruction(), "MP",
				(GeoLocusV2) getKernel().lookupLabel("L"), fixture.selector,
				(GeoNumeric) getKernel().lookupLabel("u"));
		assertNotNull(foreign);
		assertEquals(2, fixture.selector.getAlgorithmList().size());
		assertSame(fixture.owner, LocusDedicatedAuxiliary2D.ownerOf(fixture.selector),
				"the owner is still known from its persisted role");

		// The damaged document is still written, and reading it back is refused with the
		// typed diagnostic of the owner's record.
		String damaged = getApp().getXML();
		Throwable thrown = assertThrows(Throwable.class, () -> reopen(damaged));
		SpatialIdentityException failure = null;
		for (Throwable cause = thrown; cause != null && failure == null;
				cause = cause.getCause()) {
			if (cause instanceof SpatialIdentityException) {
				failure = (SpatialIdentityException) cause;
			}
		}
		assertNotNull(failure, String.valueOf(thrown));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				failure.getDiagnostic().getCode());
		assertEquals(fixture.ownerId, String.valueOf(failure.getDiagnostic().getSubject()),
				"the diagnostic names the owner, and nothing is repaired");
		assertEquals(2, fixture.selector.getAlgorithmList().size());

		// The user removes the foreign consumer: the document is consistent again.
		foreign.remove();
		assertEquals(1, fixture.selector.getAlgorithmList().size());
		assertNotNull(getApp().getXML());
		assertOwned(getKernel(), fixture.ownerId);
	}

	private Fixture fixture() {
		createLine();
		add("u=0.25");
		GeoPoint owner = add("PO=Point(L,u)");
		assertNotNull(owner);
		// The selector text is labelled as in a saved document.
		AppCommon reopened = reopen(getApp().getXML());
		getApp().setXML(reopened.getXML(), true);
		owner = (GeoPoint) getKernel().lookupLabel("PO");
		GeoText selector = ((AlgoSemanticLocusPoint2D) owner.getParentAlgorithm())
				.getBranchInput();
		assertNotNull(selector.getLabelSimple(), "the selector of a saved document is labelled");
		assertSame(owner, LocusDedicatedAuxiliary2D.ownerOf(selector));
		return new Fixture(owner, selector, persistentId(getKernel(), owner));
	}

	private static AppCommon reopen(String document) {
		AppCommon reopened = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		reopened.setXML(document, true);
		return reopened;
	}

	private String errorsOf(String command) {
		ErrorAccumulator errors = new ErrorAccumulator();
		getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(command, false,
				errors, false, null);
		return errors.getErrors();
	}

	private static String persistentId(Kernel kernel, GeoElement geo) {
		PersistentGeoId id = kernel.getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id);
		return id.toExternalForm();
	}

	private static void assertOwned(Kernel kernel, String ownerId) {
		GeoPoint owner = (GeoPoint) kernel.lookupLabel("PO");
		assertNotNull(owner);
		assertTrue(owner.isDefined());
		SpatialIdentityRegistry registry = kernel.getConstruction().getSpatialIdentityRegistry();
		PersistentGeoId id = registry.getPersistentGeoId(owner);
		assertNotNull(id);
		assertEquals(ownerId, id.toExternalForm(), "same durable identity");
		GeoIdentityRecord record = registry.getGeoRecord(id);
		assertEquals(ConstructionGeoRedefineProvider.PRINCIPAL_BRANCH_POINT_OUTPUT_ROLE,
				record.getStableOutputRole());
		assertTrue(ConstructionGeoRedefineProvider.hasDedicatedPrincipalBranchState(owner));
		assertFalse(((AlgoSemanticLocusPoint2D) owner.getParentAlgorithm()).getBranchInput()
				.isEuclidianVisible());
	}

	private static final class Fixture {
		private final GeoPoint owner;
		private final GeoText selector;
		private final String ownerId;

		private Fixture(GeoPoint owner, GeoText selector, String ownerId) {
			this.owner = owner;
			this.selector = selector;
			this.ownerId = ownerId;
		}
	}
}

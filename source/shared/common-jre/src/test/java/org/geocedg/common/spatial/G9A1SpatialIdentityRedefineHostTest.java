/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineTransaction;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.Region;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoElementSetup;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.PolygonFactory;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.matrix.Coords;
import org.geogebra.common.main.error.ErrorLogger;
import org.geogebra.common.util.AsyncOperation;
import org.junit.jupiter.api.Test;

class G9A1SpatialIdentityRedefineHostTest extends BaseUnitTest {
	private static final String PROVIDER = "g9a1.host.test";
	private static final String MISSING_PROVIDER = "g9a1.host.missing";
	private static final String THROWING_PROVIDER = "g9a1.host.throwing";
	private static final String DIRECT_ROUTE_PROVIDER = "g9a1.host.direct";
	private static final String SCHEMA = "cedg.numeric.test";

	@Test
	void explicitCompatibleHostRedefineRetainsIdentity() {
		GeoNumeric oldTarget = add("A=1");
		GeoIdentityRecord original = register(oldTarget);
		registry().registerRedefineProvider(new NumericProvider());

		editGeoElement(oldTarget, "A=2");

		GeoNumeric actualResult = (GeoNumeric) lookup("A");
		assertEquals(2, actualResult.getDouble());
		assertEquals(original.getId(), registry().getPersistentGeoId(actualResult));
		assertEquals(1,
				registry().getGeoRecord(original.getId()).getDefinitionRevision());
		assertEquals(1, registry().getInstrumentation().getRedefineRetainDecisions());
	}

	@Test
	void explicitFreshHostRedefineDoesNotTransferIdentity() {
		GeoNumeric oldTarget = add("A=1");
		PersistentGeoId oldId = register(oldTarget).getId();
		registry().registerRedefineProvider(new NumericProvider());

		EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), oldTarget, true).withSpatialReplacementOperation();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(oldTarget,
				"A=3", info, false, null, new CapturingErrorHandler());

		GeoElement actualResult = lookup("A");
		PersistentGeoId newId = registry().getPersistentGeoId(actualResult);
		assertNotNull(newId);
		assertNotEquals(oldId, newId);
		assertEquals(1, registry().getInstrumentation().getRedefineFreshDecisions());
	}

	@Test
	void freshProviderDecisionRequiresExplicitReplacementSelection() {
		GeoNumeric oldTarget = add("A=1");
		PersistentGeoId oldId = register(oldTarget).getId();
		registry().registerRedefineProvider(new NumericProvider());
		CapturingErrorHandler errors = new CapturingErrorHandler();
		EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), oldTarget, true);

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(oldTarget,
				"A=3", info, false, null, errors);

		GeoNumeric restored = (GeoNumeric) lookup("A");
		assertEquals(1, restored.getDouble());
		assertEquals(oldId, registry().getPersistentGeoId(restored));
		assertTrue(errors.sawSpatialIdentityFailure());
		assertEquals(1, registry().getInstrumentation().getRedefineRejectDecisions());
	}

	@Test
	void incompatibleHostRedefineRollsBackBeforeMutation() {
		GeoNumeric missingProviderTarget = add("M=1");
		register(missingProviderTarget, numericSignature(MISSING_PROVIDER));
		CapturingErrorHandler missingProviderErrors = new CapturingErrorHandler();
		EvalInfo missingProviderInfo = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), missingProviderTarget, true);
		String beforeMissingProvider = getApp().getXML();
		int stepsBeforeMissingProvider = getConstruction().steps();

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(missingProviderTarget,
				"M=Line((0,0),(1,1))", missingProviderInfo, false, null,
				missingProviderErrors);

		assertEquals(beforeMissingProvider, getApp().getXML());
		assertEquals(stepsBeforeMissingProvider, getConstruction().steps());
		assertTrue(missingProviderErrors.sawSpatialIdentityFailure());

		GeoNumeric oldTarget = add("A=1");
		PersistentGeoId oldId = register(oldTarget).getId();
		registry().registerRedefineProvider(new NumericProvider());
		CapturingErrorHandler errors = new CapturingErrorHandler();
		EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), oldTarget, true);
		String originalXml = getApp().getXML();
		int originalSteps = getConstruction().steps();

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(oldTarget,
				"A=Line((0,0),(1,1))", info, false, null, errors);

		GeoNumeric restored = (GeoNumeric) lookup("A");
		assertEquals(1, restored.getDouble());
		assertEquals(oldId, registry().getPersistentGeoId(restored));
		assertEquals(originalXml, getApp().getXML());
		assertEquals(originalSteps, getConstruction().steps());
		assertTrue(errors.sawSpatialIdentityFailure());
		assertEquals(2, registry().getInstrumentation().getRedefineRejectDecisions());

		GeoNumeric throwingTarget = add("T=1");
		PersistentGeoId throwingId = register(throwingTarget,
				numericSignature(THROWING_PROVIDER)).getId();
		registry().registerRedefineProvider(new ThrowingProvider());
		String beforeThrowingProvider = getApp().getXML();
		int stepsBeforeThrowingProvider = getConstruction().steps();

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(throwingTarget,
				"T=Line((0,0),(1,1))",
				EvalInfoFactory.getEvalInfoForRedefinition(
						getKernel(), throwingTarget, true),
				false, null, new CapturingErrorHandler());

		assertEquals(beforeThrowingProvider, getApp().getXML());
		assertEquals(stepsBeforeThrowingProvider, getConstruction().steps());
		GeoElement restoredThrowingTarget = lookup("T");
		assertEquals(throwingId,
				registry().getPersistentGeoId(restoredThrowingTarget));

		EvalInfo directInfo = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), restoredThrowingTarget, true).withSpatialRedefineContext(
						registry().captureRedefineContext(restoredThrowingTarget));
		GeoNumeric directCandidate = new GeoNumeric(getConstruction(), 2);
		String beforeDirectFailure = getApp().getXML();
		int stepsBeforeDirectFailure = getConstruction().steps();

		assertThrows(IllegalStateException.class, () -> getConstruction().replace(
				restoredThrowingTarget, directCandidate, directInfo));
		assertEquals(beforeDirectFailure, getApp().getXML());
		assertEquals(stepsBeforeDirectFailure, getConstruction().steps());
		assertEquals(throwingId,
				registry().getPersistentGeoId(lookup("T")));
	}

	@Test
	void labelOnlyReplacementHasNoAuthorityToRetainIdentity() {
		GeoNumeric oldTarget = add("A=1");
		PersistentGeoId oldId = register(oldTarget).getId();
		registry().registerRedefineProvider(new NumericProvider());
		CapturingErrorHandler errors = new CapturingErrorHandler();
		setErrorHandler(errors);

		add("A=2");
		resetErrorHandler();

		assertSame(oldTarget, lookup("A"));
		assertEquals(1, oldTarget.getDouble());
		assertEquals(oldId, registry().getPersistentGeoId(oldTarget));
		assertTrue(errors.sawSpatialIdentityFailure());
		assertEquals(1, registry().getInstrumentation().getRedefineMissingContexts());
	}

	@Test
	void hostRollbackRestoresGeometryAndIdentityAfterSetupFailure() {
		GeoNumeric oldTarget = add("A=1");
		PersistentGeoId oldId = register(oldTarget).getId();
		registry().registerRedefineProvider(new NumericProvider());
		GeoElementSetup throwingSetup = geo -> {
			throw new IllegalStateException("deliberate setup failure");
		};
		CapturingErrorHandler errors = new CapturingErrorHandler();
		getAlgebraProcessor().addGeoElementSetup(throwingSetup);
		try {
			EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
					getKernel(), oldTarget, true);
			getAlgebraProcessor().changeGeoElementNoExceptionHandling(oldTarget,
					"A=2", info, false, null, errors);
		} finally {
			getAlgebraProcessor().removeGeoElementSetup(throwingSetup);
		}

		GeoNumeric restored = (GeoNumeric) lookup("A");
		assertEquals(1, restored.getDouble());
		assertEquals(oldId, registry().getPersistentGeoId(restored));

		ThrowingRemoveNumeric removalTarget = new ThrowingRemoveNumeric();
		removalTarget.setValue(2);
		removalTarget.setLabel("B");
		PersistentGeoId removalId = register(removalTarget).getId();
		add("C=1");
		SpatialRedefineContext removalContext =
				registry().captureRedefineContext(removalTarget);
		GeoNumeric candidate = new GeoNumeric(getConstruction(), 4);
		SpatialRedefineTransaction removalTransaction = registry().prepareRedefine(
				removalContext, candidate, 1, true, false);
		editGeoElement((GeoNumeric) lookup("C"), "C=2");
		String beforeRemovalFailure = getApp().getXML();
		int stepsBeforeRemovalFailure = getConstruction().steps();
		long removalRevision = registry().getGeoRecord(removalId)
				.getDefinitionRevision();
		EvalInfo removalInfo = new EvalInfo(true)
				.withSpatialRedefineContext(removalContext)
				.withSpatialRedefineTransaction(removalTransaction);

		assertThrows(IllegalStateException.class,
				() -> getConstruction().replace(removalTarget, candidate, removalInfo));

		assertEquals(beforeRemovalFailure, getApp().getXML());
		assertEquals(stepsBeforeRemovalFailure, getConstruction().steps());
		assertEquals(2, ((GeoNumeric) lookup("C")).getDouble());
		assertFalse(getConstruction().isRemovingGeoToReplaceIt());
		assertEquals(removalId,
				registry().getPersistentGeoId(lookup("B")));
		assertEquals(removalRevision,
				registry().getGeoRecord(removalId).getDefinitionRevision());
	}

	@Test
	void staleExplicitContextCannotOverwriteANewerFreshRedefine() {
		GeoNumeric oldTarget = add("A=1");
		register(oldTarget);
		registry().registerRedefineProvider(new NumericProvider());
		EvalInfo staleInfo = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), oldTarget, true).withSpatialRedefineContext(
						registry().captureRedefineContext(oldTarget));
		editGeoElement(oldTarget, "A=2");
		PersistentGeoId retainedId = registry().getPersistentGeoId(oldTarget);
		CapturingErrorHandler staleRevisionErrors = new CapturingErrorHandler();

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(oldTarget,
				"A=4", staleInfo, false, null, staleRevisionErrors);

		assertEquals(2, ((GeoNumeric) lookup("A")).getDouble());
		assertEquals(retainedId, registry().getPersistentGeoId(lookup("A")));
		assertTrue(staleRevisionErrors.sawSpatialIdentityFailure());

		GeoNumeric retainedTarget = (GeoNumeric) lookup("A");
		EvalInfo replacementInfo = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), retainedTarget, true).withSpatialReplacementOperation();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(retainedTarget,
				"A=3", replacementInfo, false, null, new CapturingErrorHandler());
		GeoNumeric current = (GeoNumeric) lookup("A");
		PersistentGeoId currentId = registry().getPersistentGeoId(current);
		CapturingErrorHandler errors = new CapturingErrorHandler();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(current,
				"A=4", staleInfo, false, null, errors);

		GeoNumeric unchanged = (GeoNumeric) lookup("A");
		assertEquals(3, unchanged.getDouble());
		assertEquals(currentId, registry().getPersistentGeoId(unchanged));
		assertTrue(errors.sawSpatialIdentityFailure());

		SpatialRedefineContext directStaleContext =
				registry().captureRedefineContext(unchanged);
		editGeoElement(unchanged, "A=2");
		GeoElement directCurrent = lookup("A");
		PersistentGeoId directCurrentId = registry().getPersistentGeoId(directCurrent);
		String beforeDirectStale = getApp().getXML();
		int stepsBeforeDirectStale = getConstruction().steps();
		EvalInfo directStaleInfo = new EvalInfo(true)
				.withSpatialRedefineContext(directStaleContext);

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replace(directCurrent,
						new GeoNumeric(getConstruction(), 4), directStaleInfo));
		assertEquals(beforeDirectStale, getApp().getXML());
		assertEquals(stepsBeforeDirectStale, getConstruction().steps());
		assertEquals(directCurrentId,
				registry().getPersistentGeoId(lookup("A")));

		GeoElement transactionTarget = lookup("A");
		SpatialRedefineContext transactionContext =
				registry().captureRedefineContext(transactionTarget);
		GeoNumeric transactionCandidate = new GeoNumeric(getConstruction(), 4);
		SpatialRedefineTransaction staleTransaction = registry().prepareRedefine(
				transactionContext, transactionCandidate, 1, true, false);
		editGeoElement(transactionTarget, "A=6");
		GeoElement newerTarget = lookup("A");
		PersistentGeoId newerId = registry().getPersistentGeoId(newerTarget);
		long newerRevision = registry().getGeoRecord(newerId).getDefinitionRevision();
		String beforeStaleTransaction = getApp().getXML();
		int stepsBeforeStaleTransaction = getConstruction().steps();
		EvalInfo staleTransactionInfo = new EvalInfo(true)
				.withSpatialRedefineContext(transactionContext)
				.withSpatialRedefineTransaction(staleTransaction);

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replace(newerTarget,
						transactionCandidate, staleTransactionInfo));
		assertEquals(SpatialRedefineTransaction.State.ROLLED_BACK,
				staleTransaction.getState());
		assertEquals(beforeStaleTransaction, getApp().getXML());
		assertEquals(stepsBeforeStaleTransaction, getConstruction().steps());
		assertEquals(newerId, registry().getPersistentGeoId(lookup("A")));
		assertEquals(newerRevision,
				registry().getGeoRecord(newerId).getDefinitionRevision());

		GeoNumeric wrongContextTarget = add("B=1");
		register(wrongContextTarget);
		SpatialRedefineContext wrongContext =
				registry().captureRedefineContext(wrongContextTarget);
		GeoElement directTarget = lookup("A");
		String beforeWrongContext = getApp().getXML();
		int stepsBeforeWrongContext = getConstruction().steps();

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replace(directTarget,
						new GeoNumeric(getConstruction(), 5),
						new EvalInfo(true).withSpatialRedefineContext(wrongContext)));
		assertEquals(beforeWrongContext, getApp().getXML());
		assertEquals(stepsBeforeWrongContext, getConstruction().steps());
		assertEquals(directCurrentId,
				registry().getPersistentGeoId(lookup("A")));

		GeoElement operationTarget = lookup("A");
		SpatialRedefineContext staleOperationContext =
				registry().captureRedefineContext(operationTarget);
		editGeoElement(operationTarget, "A=8");
		String beforeStaleOperation = getApp().getXML();
		int stepsBeforeStaleOperation = getConstruction().steps();

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replaceFromSpatialRedefineOperation(
						lookup("A"), new GeoNumeric(getConstruction(), 9),
						staleOperationContext));
		assertEquals(beforeStaleOperation, getApp().getXML());
		assertEquals(stepsBeforeStaleOperation, getConstruction().steps());

		String beforeWrongOperation = getApp().getXML();
		int stepsBeforeWrongOperation = getConstruction().steps();
		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replaceFromSpatialRedefineOperation(
						lookup("A"), new GeoNumeric(getConstruction(), 10),
						wrongContext));
		assertEquals(beforeWrongOperation, getApp().getXML());
		assertEquals(stepsBeforeWrongOperation, getConstruction().steps());
	}

	@Test
	void multiOutputRedefineRejectsWithoutChangingEitherSibling() {
		add("c=Circle((0,0),2)");
		add("g:y=0");
		CapturingErrorHandler errors = new CapturingErrorHandler();
		GeoElementND[] outputs = getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling("Intersect(c,g)", false,
						errors, false, null);
		assertEquals(2, outputs.length);
		GeoElement first = (GeoElement) outputs[0];
		GeoElement second = (GeoElement) outputs[1];
		String firstLabel = first.getLabelSimple();
		String secondLabel = second.getLabelSimple();
		SpatialRedefineSignature signature = structuralSignature("POINT", "BRANCH");
		PersistentGeoId firstId = register(first, signature).getId();
		PersistentGeoId secondId = register(second, signature).getId();
		registry().registerRedefineProvider(new StructuralProvider(signature));
		String firstValue = first.toValueString(StringTemplate.xmlTemplate);
		String secondValue = second.toValueString(StringTemplate.xmlTemplate);
		EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), first, true);

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(first,
				firstLabel + "=(0,0)", info, false, null, errors);

		GeoElement restoredFirst = lookup(firstLabel);
		GeoElement restoredSecond = lookup(secondLabel);
		assertEquals(firstValue,
				restoredFirst.toValueString(StringTemplate.xmlTemplate));
		assertEquals(secondValue,
				restoredSecond.toValueString(StringTemplate.xmlTemplate));
		assertEquals(firstId, registry().getPersistentGeoId(restoredFirst));
		assertEquals(secondId, registry().getPersistentGeoId(restoredSecond));
		assertTrue(errors.sawSpatialIdentityFailure());

		add("c2=Circle((0,0),3)");
		add("g2:y=1");
		GeoElementND[] secondOutputs = getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling("Intersect(c2,g2)", false,
						new CapturingErrorHandler(), false, null);
		GeoElement unassociatedTarget = (GeoElement) secondOutputs[0];
		GeoElement participatingSibling = (GeoElement) secondOutputs[1];
		String targetLabel = unassociatedTarget.getLabelSimple();
		String siblingLabel = participatingSibling.getLabelSimple();
		PersistentGeoId siblingId = register(participatingSibling, signature).getId();

		editGeoElement(unassociatedTarget, targetLabel + "=(0,1)");

		assertNull(lookup(siblingLabel));
		assertNull(registry().getRecord(siblingId));
	}

	@Test
	void compatibleSameAlgorithmUsesSoftRedefineAndRetainsIdentity() {
		add("P=(1,1)");
		GeoElement oldTarget = add("l=Line(P,(1,3))");
		SpatialRedefineSignature signature = structuralSignature("LINE", "AXIS");
		PersistentGeoId id = register(oldTarget, signature).getId();
		registry().registerRedefineProvider(new StructuralProvider(signature));

		editGeoElement(oldTarget, "l=Line(P,(1,2))");

		assertSame(oldTarget, lookup("l"));
		assertEquals(id, registry().getPersistentGeoId(oldTarget));
	}

	@Test
	void compatibleNoChildReplacementTransfersRetainedIdentity() {
		add("P=(0,0)");
		add("Q=(1,0)");
		add("R=(0,1)");
		add("g=Line(Q,R)");
		GeoElement oldTarget = add("l=Line(P,Q)");
		SpatialRedefineSignature signature = structuralSignature("LINE", "AXIS");
		PersistentGeoId id = register(oldTarget, signature).getId();
		registry().registerRedefineProvider(new StructuralProvider(signature));

		editGeoElement(oldTarget, "l=PerpendicularLine(P,g)");

		GeoElement actual = lookup("l");
		assertNotSame(oldTarget, actual);
		assertEquals(id, registry().getPersistentGeoId(actual));

		GeoElement acceptedPoint = add("U=(1,1)");
		Path path = (Path) add("u:y=0");
		SpatialRedefineSignature pointSignature = structuralSignature(
				DIRECT_ROUTE_PROVIDER, "POINT", "POSITION");
		PersistentGeoId pointId = register(acceptedPoint, pointSignature).getId();
		registry().registerRedefineProvider(new StructuralProvider(pointSignature));

		GeoPointND attached = getKernel().getAlgoDispatcher().attach(
				(GeoPointND) acceptedPoint, path,
				getApp().getActiveEuclidianView(), new Coords(1, 0, 1));

		assertNotNull(attached);
		assertEquals(pointId,
				registry().getPersistentGeoId(lookup("U")));

		GeoElement rejectedPoint = add("V=(2,2)");
		PersistentGeoId rejectedId = register(rejectedPoint,
				numericSignature(THROWING_PROVIDER)).getId();
		registry().registerRedefineProvider(new ThrowingProvider());
		String beforeRejectedAttach = getApp().getXML();
		int stepsBeforeRejectedAttach = getConstruction().steps();

		assertNull(getKernel().getAlgoDispatcher().attach(
				(GeoPointND) rejectedPoint, path,
				getApp().getActiveEuclidianView(), new Coords(2, 0, 1)));
		assertEquals(beforeRejectedAttach, getApp().getXML());
		assertEquals(stepsBeforeRejectedAttach, getConstruction().steps());
		assertEquals(rejectedId,
				registry().getPersistentGeoId(lookup("V")));

		Region region = (Region) add("d=Circle((0,0),4)");
		GeoElement rejectedRegionPoint = add("W=(3,3)");
		PersistentGeoId rejectedRegionId = register(rejectedRegionPoint,
				numericSignature(THROWING_PROVIDER)).getId();
		String beforeRejectedRegionAttach = getApp().getXML();
		int stepsBeforeRejectedRegionAttach = getConstruction().steps();

		assertNull(getKernel().getAlgoDispatcher().attach(
				(GeoPointND) rejectedRegionPoint, region,
				getApp().getActiveEuclidianView(), new Coords(3, 0, 1)));
		assertEquals(beforeRejectedRegionAttach, getApp().getXML());
		assertEquals(stepsBeforeRejectedRegionAttach, getConstruction().steps());
		assertEquals(rejectedRegionId,
				registry().getPersistentGeoId(lookup("W")));

		GeoPointND[] rigidPoints = {
				(GeoPointND) add("J=(0,0)"),
				(GeoPointND) add("K=(1,0)"),
				(GeoPointND) add("L=(0,1)")
		};
		PersistentGeoId rejectedPolygonId = register(
				(GeoElement) rigidPoints[1],
				numericSignature(THROWING_PROVIDER)).getId();
		String beforeRejectedPolygon = getApp().getXML();
		int stepsBeforeRejectedPolygon = getConstruction().steps();

		assertNull(new PolygonFactory(getKernel()).rigidPolygon(null, rigidPoints));
		assertEquals(beforeRejectedPolygon, getApp().getXML());
		assertEquals(stepsBeforeRejectedPolygon, getConstruction().steps());
		assertEquals(rejectedPolygonId,
				registry().getPersistentGeoId(lookup("K")));
	}

	@Test
	void compatibleReplacementWithChildrenUsesXmlRebuildAndRetainsIdentity()
			throws Exception {
		add("P=(0,0)");
		add("Q=(1,0)");
		add("R=(0,1)");
		add("g=Line(Q,R)");
		GeoElement oldTarget = add("l=Line(P,Q)");
		add("X=Point(l)");
		SpatialRedefineSignature signature = structuralSignature("LINE", "AXIS");
		PersistentGeoId id = register(oldTarget, signature).getId();
		registry().registerRedefineProvider(new StructuralProvider(signature));

		editGeoElement(oldTarget, "l=PerpendicularLine(P,g)");

		GeoElement actual = lookup("l");
		assertNotSame(oldTarget, actual);
		assertEquals(id, registry().getPersistentGeoId(actual));
		assertNotNull(lookup("X"));

		add("S=(2,0)");
		add("T=(2,1)");
		add("h=Line(S,T)");
		GeoElement firstCollected = add("m=Line(P,S)");
		add("M=Point(m)");
		GeoElement overflowCollected = add("n=Line(Q,T)");
		add("N=Point(n)");
		final PersistentGeoId firstCollectedId =
				register(firstCollected, signature).getId();
		final PersistentGeoId overflowCollectedId = register(overflowCollected, signature,
				Long.MAX_VALUE).getId();
		String firstDefinition = firstCollected.getDefinition(StringTemplate.xmlTemplate);
		String overflowDefinition = overflowCollected.getDefinition(
				StringTemplate.xmlTemplate);
		String preBatchXml = getApp().getXML();

		getConstruction().startCollectingRedefineCalls();
		editGeoElement(firstCollected, "m=PerpendicularLine(P,h)");
		editGeoElement(overflowCollected, "n=PerpendicularLine(Q,h)");
		SpatialIdentityException batchFailure = assertThrows(
				SpatialIdentityException.class,
				() -> getConstruction().processCollectedRedefineCalls());

		assertTrue(batchFailure.getMessage().contains(
				"Definition revision cannot advance"));
		assertEquals(preBatchXml, getApp().getXML());
		GeoElement restoredFirst = lookup("m");
		GeoElement restoredOverflow = lookup("n");
		assertEquals(firstDefinition,
				restoredFirst.getDefinition(StringTemplate.xmlTemplate));
		assertEquals(overflowDefinition,
				restoredOverflow.getDefinition(StringTemplate.xmlTemplate));
		assertEquals(firstCollectedId,
				registry().getPersistentGeoId(restoredFirst));
		assertEquals(overflowCollectedId,
				registry().getPersistentGeoId(restoredOverflow));
		assertEquals(0, registry().getGeoRecord(firstCollectedId)
				.getDefinitionRevision());
		assertEquals(Long.MAX_VALUE, registry().getGeoRecord(overflowCollectedId)
				.getDefinitionRevision());
		assertNotNull(lookup("M"));
		assertNotNull(lookup("N"));

		GeoElement overflowTarget = add("q=Line(P,S)");
		add("Y=Point(q)");
		PersistentGeoId overflowId = register(overflowTarget, signature,
				Long.MAX_VALUE).getId();
		String beforeOverflow = getApp().getXML();
		int stepsBeforeOverflow = getConstruction().steps();
		CapturingErrorHandler overflowErrors = new CapturingErrorHandler();

		getAlgebraProcessor().changeGeoElementNoExceptionHandling(overflowTarget,
				"q=PerpendicularLine(P,h)",
				EvalInfoFactory.getEvalInfoForRedefinition(
						getKernel(), overflowTarget, true),
				false, null, overflowErrors);

		assertEquals(beforeOverflow, getApp().getXML());
		assertEquals(stepsBeforeOverflow, getConstruction().steps());
		assertEquals(overflowId,
				registry().getPersistentGeoId(lookup("q")));
		assertTrue(overflowErrors.sawSpatialIdentityFailure());
	}

	@Test
	void parametricProcessorPreservesExplicitRedefineContext() {
		GeoElement oldTarget = add("l=Line((0,0),(1,0))");
		SpatialRedefineSignature signature = structuralSignature("LINE", "AXIS");
		PersistentGeoId id = register(oldTarget, signature).getId();
		registry().registerRedefineProvider(new StructuralProvider(signature));
		String oldValue = oldTarget.toValueString(StringTemplate.xmlTemplate);

		editGeoElement(oldTarget, "l: X = (0,0) + t * (0,1)");

		GeoElement actual = lookup("l");
		assertNotNull(actual);
		assertNotEquals(oldValue,
				actual.toValueString(StringTemplate.xmlTemplate));
		assertEquals(id, registry().getPersistentGeoId(actual));
	}

	private GeoIdentityRecord register(GeoElement geo) {
		return register(geo, numericSignature());
	}

	private GeoIdentityRecord register(GeoElement geo,
			SpatialRedefineSignature signature) {
		return register(geo, signature, 0);
	}

	private GeoIdentityRecord register(GeoElement geo,
			SpatialRedefineSignature signature, long definitionRevision) {
		GeoIdentityRecord record = new GeoIdentityRecord(
				registry().allocatePersistentGeoId(), signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), signature.getStableOutputRole(),
				signature.getOutputCardinality(), definitionRevision, 0);
		registry().registerParticipation(geo, record);
		return record;
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private static SpatialRedefineSignature numericSignature() {
		return numericSignature(PROVIDER);
	}

	private static SpatialRedefineSignature numericSignature(String provider) {
		return new SpatialRedefineSignature(provider, "NUMERIC", SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"VALUE", 1);
	}

	private static SpatialRedefineSignature structuralSignature(String family,
			String stableOutputRole) {
		return structuralSignature(PROVIDER, family, stableOutputRole);
	}

	private static SpatialRedefineSignature structuralSignature(String provider,
			String family, String stableOutputRole) {
		return new SpatialRedefineSignature(provider, family, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				stableOutputRole, 1);
	}

	private static final class NumericProvider implements SpatialRedefineProvider {
		@Override
		public String getProviderId() {
			return PROVIDER;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
				GeoElement candidate) {
			return candidate instanceof GeoNumeric ? numericSignature()
					: new SpatialRedefineSignature(PROVIDER, "INCOMPATIBLE", SCHEMA, 1,
							EditAuthorityMode.PROJECTION_DEFINED,
							ProjectionBindingRole.DEFINING, "VALUE", 1);
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return candidate instanceof GeoNumeric;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			if (!(proposal.getCandidate() instanceof GeoNumeric)) {
				return SpatialRedefineDecision.REJECT;
			}
			return ((GeoNumeric) proposal.getCandidate()).getDouble() == 3
					? SpatialRedefineDecision.FRESH : SpatialRedefineDecision.RETAIN;
		}
	}

	private static final class ThrowingProvider implements SpatialRedefineProvider {
		@Override
		public String getProviderId() {
			return THROWING_PROVIDER;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
				GeoElement candidate) {
			throw new IllegalStateException("deliberate provider inspection failure");
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return true;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return SpatialRedefineDecision.RETAIN;
		}
	}

	private final class ThrowingRemoveNumeric extends GeoNumeric {
		private ThrowingRemoveNumeric() {
			super(G9A1SpatialIdentityRedefineHostTest.this.getConstruction());
		}

		@Override
		public void remove() {
			throw new IllegalStateException("deliberate remove failure");
		}
	}

	private static final class StructuralProvider implements SpatialRedefineProvider {
		private final SpatialRedefineSignature signature;

		private StructuralProvider(SpatialRedefineSignature signature) {
			this.signature = signature;
		}

		@Override
		public String getProviderId() {
			return signature.getProvider();
		}

		@Override
		public SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
				GeoElement candidate) {
			return signature;
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return true;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			return SpatialRedefineDecision.RETAIN;
		}
	}

	private static final class CapturingErrorHandler implements ErrorLogger {
		private Throwable throwable;
		private String message;

		@Override
		public void showError(String error) {
			message = error;
		}

		@Override
		public void showCommandError(String command, String error) {
			message = error;
		}

		@Override
		public String getCurrentCommand() {
			return null;
		}

		@Override
		public boolean onUndefinedVariables(String variables,
				AsyncOperation<String[]> callback) {
			return false;
		}

		@Override
		public void resetError() {
			message = null;
			throwable = null;
		}

		@Override
		public void log(Throwable error) {
			throwable = error;
		}

		private boolean sawSpatialIdentityFailure() {
			Throwable current = throwable;
			while (current != null) {
				if (current instanceof SpatialIdentityException) {
					return true;
				}
				current = current.getCause();
			}
			return message != null && message.contains("REDEFINE_");
		}
	}
}

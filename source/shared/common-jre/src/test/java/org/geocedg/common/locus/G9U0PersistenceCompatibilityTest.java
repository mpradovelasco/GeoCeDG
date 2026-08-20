/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2DomainDescriptor;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootAddressProof2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionTokenLineage2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.locus.intersection.LocusSemanticIntersectionToken2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/** Exact P16 native XML, copy, undo and compatibility scenarios. */
class G9U0PersistenceCompatibilityTest extends G9U0PublicSurfaceTestBase {

	private static final Path FIXTURE_ROOT = Path.of("src", "test", "resources",
			"org", "geocedg", "common", "locus", "g9u0");

	@Test
	void p01EveryPublicGeneratorSaveAndReopenRestoresExactDescriptor() {
		add("s=0.5");
		add("t=s^2");
		add("Q=(t,s)");
		add("Ds={false,{-2,2,true,true}}");
		add("Ls=LocusV2(Q,t,s,Ds)");

		add("gSegment=Segment((0,0),(2,0))");
		add("Ssegment=Point(gSegment)");
		add("Qsegment=(x(Ssegment),x(Ssegment)^2)");
		add("Lsegment=LocusV2(Qsegment,Ssegment)");

		add("gCircle=Circle((0,0),2)");
		add("Scircle=Point(gCircle)");
		add("Qcircle=(x(Scircle),y(Scircle)+1)");
		add("Lcircle=LocusV2(Qcircle,Scircle)");

		add("gArc=CircumcircularArc((1,0),(0,1),(-1,0))");
		add("Sarc=Point(gArc)");
		add("Qarc=(x(Sarc),y(Sarc)+2)");
		add("Larc=LocusV2(Qarc,Sarc)");

		add("Pbranch=Point(Ls,\"" + BRANCH + "\",0.25)");
		add("Qbranch=(x(Pbranch),y(Pbranch)+3)");
		add("Lbranch=LocusV2(Qbranch,Pbranch)");

		List<String> labels = List.of("Ls", "Lsegment", "Lcircle", "Larc",
				"Lbranch");
		Map<String, GeneratorSnapshot> expected = new LinkedHashMap<>();
		for (String label : labels) {
			expected.put(label, generatorSnapshot(
					(GeoLocusV2) requireLookup(label)));
		}
		reload();
		for (Map.Entry<String, GeneratorSnapshot> entry : expected.entrySet()) {
			GeoLocusV2 reopened = (GeoLocusV2) requireLookup(entry.getKey());
			assertTrue(reopened.isDefined(), entry.getKey());
			assertEquals(entry.getValue(), generatorSnapshot(reopened),
					entry.getKey());
		}
	}

	@Test
	void p02MetricQuerySaveAndReopenRestoresRichParent() {
		GeoLocusV2 locus = createLine();
		GeoLocusMetricResult metric = totalMetric(locus);
		final String source = metric.getSourceLocusIdentity();
		activateUndo();
		getApp().storeUndoInfo();
		GeoNumeric scalar = add("len=Length(L)");
		AlgoLocusMetricScalarAdapter scalarParent =
				(AlgoLocusMetricScalarAdapter) scalar.getParentAlgorithm();
		GeoLocusMetricResult hiddenRich = scalarParent.getRichInput();
		assertTrue(hiddenRich.isAuxiliaryObject());
		assertTrue(hiddenRich.isLabelSet());
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId scalarId = idOf(registry, scalar);
		PersistentGeoId hiddenRichId = idOf(registry, hiddenRich);
		assertExactDependencies(registry.getGeoRecord(scalarId), hiddenRichId);
		getApp().storeUndoInfo();

		getKernel().undo();
		assertNull(lookup("len"));
		getKernel().redo();
		scalar = (GeoNumeric) requireLookup("len");
		scalarParent = (AlgoLocusMetricScalarAdapter) scalar.getParentAlgorithm();
		hiddenRich = scalarParent.getRichInput();
		assertEquals(scalarId, idOf(registry, scalar));
		assertEquals(hiddenRichId, idOf(registry, hiddenRich));
		assertEquals(4, scalar.getDouble(), 1E-10);

		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(scalar), text -> text);
		paste(getApp(), clipboard);
		GeoIdentityRecord copiedScalarRecord = recordCopying(registry, scalarId);
		GeoIdentityRecord copiedRichRecord = recordCopying(registry, hiddenRichId);
		GeoNumeric copiedScalar = (GeoNumeric) registry.getGeo(
				copiedScalarRecord.getId());
		GeoLocusMetricResult copiedRich = (GeoLocusMetricResult) registry.getGeo(
				copiedRichRecord.getId());
		assertNotNull(copiedScalar);
		assertNotNull(copiedRich);
		assertEquals(copiedRich,
				copiedScalar.getParentAlgorithm().getInput(0));
		assertExactDependencies(copiedScalarRecord, copiedRichRecord.getId());
		assertEquals(4, copiedScalar.getDouble(), 1E-10);

		reload();
		registry = getConstruction().getSpatialIdentityRegistry();
		GeoLocusMetricResult reopened =
				(GeoLocusMetricResult) requireLookup("M");
		assertTrue(reopened.isDefined());
		assertEquals(source, reopened.getSourceLocusIdentity());
		assertEquals(4, reopened.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 1E-10);
		GeoNumeric reopenedScalar = (GeoNumeric) registry.getGeo(scalarId);
		GeoLocusMetricResult reopenedHidden =
				(GeoLocusMetricResult) registry.getGeo(hiddenRichId);
		assertNotNull(reopenedScalar);
		assertNotNull(reopenedHidden);
		assertEquals(reopenedHidden,
				reopenedScalar.getParentAlgorithm().getInput(0));
		assertEquals(4, reopenedScalar.getDouble(), 1E-10);
		GeoNumeric reopenedCopy = (GeoNumeric) registry.getGeo(
				copiedScalarRecord.getId());
		GeoLocusMetricResult reopenedCopyRich =
				(GeoLocusMetricResult) registry.getGeo(copiedRichRecord.getId());
		assertNotNull(reopenedCopy);
		assertNotNull(reopenedCopyRich);
		assertEquals(reopenedCopyRich,
				reopenedCopy.getParentAlgorithm().getInput(0));
		assertEquals(4, reopenedCopy.getDouble(), 1E-10);
	}

	@Test
	void p03EveryIntersectionFamilyReopensWithExactQueryAndLedger() {
		List<GeoLocusIntersectionResult> results =
				allPublicIntersectionFamilies(createLine());
		Map<String, IntersectionSnapshot> expected = new LinkedHashMap<>();
		for (GeoLocusIntersectionResult result : results) {
			expected.put(result.getLabelSimple(), snapshot(result));
		}
		reload();
		for (Map.Entry<String, IntersectionSnapshot> entry
				: expected.entrySet()) {
			GeoLocusIntersectionResult reopened =
					(GeoLocusIntersectionResult) requireLookup(entry.getKey());
			IntersectionSnapshot actual = snapshot(reopened);
			assertEquals(entry.getValue(), actual, entry.getKey());
			assertTrue(reopened.isDefined(), entry.getKey());
		}
	}

	@Test
	void p04BoundAndTokenPointsSaveAndReopenWithExactAddresses() {
		GeoLocusV2 locus = createLine();
		GeoPoint bound = semanticPoint(locus, 0.25);
		GeoLocusIntersectionResult result = intersect(locus, "x=0");
		String token = firstToken(result);
		GeoPoint selected = tokenPoint(result, token);
		assertTrue(bound.isDefined() && selected.isDefined());
		var address = ((AlgoSemanticLocusPoint2D) bound.getParentAlgorithm())
				.getSemanticAddress();
		String ledger = result.getTokenLedgerState();
		final Map<String, PersistentGeoId> identities = Map.of(
				"L", idOf(getConstruction().getSpatialIdentityRegistry(), locus),
				"P", idOf(getConstruction().getSpatialIdentityRegistry(), bound),
				"R", idOf(getConstruction().getSpatialIdentityRegistry(), result),
				"X", idOf(getConstruction().getSpatialIdentityRegistry(), selected));
		reload();
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		GeoPoint reopenedBound = (GeoPoint) requireLookup("P");
		GeoLocusIntersectionResult reopenedResult =
				(GeoLocusIntersectionResult) requireLookup("R");
		GeoPoint reopenedSelected = (GeoPoint) requireLookup("X");
		assertTrue(reopenedBound.isDefined());
		assertTrue(reopenedSelected.isDefined());
		assertEquals(address, ((AlgoSemanticLocusPoint2D)
				reopenedBound.getParentAlgorithm()).getSemanticAddress());
		assertEquals(ledger, reopenedResult.getTokenLedgerState());
		assertEquals(token, firstToken(reopenedResult));
		assertEquals(token, ((GeoText) reopenedSelected.getParentAlgorithm()
				.getInput(1)).getTextString());
		for (Map.Entry<String, PersistentGeoId> identity : identities.entrySet()) {
			assertEquals(identity.getValue(), idOf(registry,
					requireLookup(identity.getKey())), identity.getKey());
		}
	}

	@Test
	void p05NativeSaveReopenSaveIsByteIdentical() throws Exception {
		GeoLocusV2 locus = createParabola();
		semanticPoint(locus, 0.5);
		totalMetric(locus);
		intersect(locus, "y=1");
		String first = getApp().getXML();
		getApp().getXMLio().processXMLString(first, true, false, false);
		assertEquals(first, getApp().getXML());
	}

	@Test
	void p06OwnedClosureCopyAllocatesFreshIdsAndRebasesToken() {
		GeoLocusIntersectionResult result = intersect(createLine(), "x=0");
		String originalToken = firstToken(result);
		GeoPoint selected = tokenPoint(result, originalToken);
		GeoPoint second = add("Y=Intersect(R,\"" + originalToken + "\")");
		GeoText selectedToken = (GeoText) selected.getParentAlgorithm().getInput(1);
		GeoText secondToken = (GeoText) second.getParentAlgorithm().getInput(1);
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId resultId = idOf(registry, result);
		PersistentGeoId selectedTokenId = idOf(registry, selectedToken);
		PersistentGeoId secondTokenId = idOf(registry, secondToken);
		PersistentGeoId selectedId = idOf(registry, selected);
		PersistentGeoId secondId = idOf(registry, second);
		assertExactDependencies(registry.getGeoRecord(selectedId), resultId,
				selectedTokenId);
		assertExactDependencies(registry.getGeoRecord(secondId), resultId,
				secondTokenId);
		Set<PersistentGeoId> original = ids(getConstruction());
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Arrays.asList(selected, second), text -> text);
		paste(getApp(), clipboard);
		List<GeoIdentityRecord> copies = copyRecords(getConstruction());
		assertFalse(copies.isEmpty());
		assertTrue(copies.stream().allMatch(record ->
				original.contains(record.getCopySourceId())
						&& !original.contains(record.getId())));

		GeoIdentityRecord copiedResultRecord = recordCopying(registry, resultId);
		GeoIdentityRecord copiedSelectedTokenRecord = recordCopying(registry,
				selectedTokenId);
		GeoIdentityRecord copiedSecondTokenRecord = recordCopying(registry,
				secondTokenId);
		GeoIdentityRecord copiedSelectedRecord = recordCopying(registry,
				selectedId);
		GeoIdentityRecord copiedSecondRecord = recordCopying(registry, secondId);
		assertExactDependencies(copiedSelectedRecord, copiedResultRecord.getId(),
				copiedSelectedTokenRecord.getId());
		assertExactDependencies(copiedSecondRecord, copiedResultRecord.getId(),
				copiedSecondTokenRecord.getId());
		GeoLocusIntersectionResult copiedResult =
				(GeoLocusIntersectionResult) registry.getGeo(
						copiedResultRecord.getId());
		GeoPoint copiedSelected =
				(GeoPoint) registry.getGeo(copiedSelectedRecord.getId());
		GeoPoint copiedSecond =
				(GeoPoint) registry.getGeo(copiedSecondRecord.getId());
		assertTrue(copiedSelected.isDefined());
		assertTrue(copiedSecond.isDefined());
		assertNotEquals(originalToken, firstToken(copiedResult));
		GeoPoint freshlyTypedOldToken = add("Z=Intersect("
				+ copiedResult.getLabelSimple() + ",\"" + originalToken + "\")");
		assertFalse(freshlyTypedOldToken.isDefined());

		PersistentGeoId firstCopyResultId = copiedResultRecord.getId();
		final PersistentGeoId firstCopyTokenId =
				copiedSelectedTokenRecord.getId();
		PersistentGeoId firstCopyPointId = copiedSelectedRecord.getId();
		reload();
		registry = getConstruction().getSpatialIdentityRegistry();
		copiedResult = (GeoLocusIntersectionResult) registry.getGeo(
				firstCopyResultId);
		copiedSelected = (GeoPoint) registry.getGeo(firstCopyPointId);
		assertNotNull(copiedResult);
		assertTrue(copiedSelected.isDefined());
		GeoPoint reopenedFreshOldToken = add("ReopenedOld=Intersect("
				+ copiedResult.getLabelSimple() + ",\"" + originalToken + "\")");
		assertFalse(reopenedFreshOldToken.isDefined());

		clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(copiedSelected), text -> text);
		paste(getApp(), clipboard);
		GeoIdentityRecord secondCopyResultRecord = recordCopying(registry,
				firstCopyResultId);
		GeoIdentityRecord secondCopyTokenRecord = recordCopying(registry,
				firstCopyTokenId);
		GeoIdentityRecord secondCopyPointRecord = recordCopying(registry,
				firstCopyPointId);
		assertEquals(firstCopyResultId,
				secondCopyResultRecord.getCopySourceId());
		assertEquals(firstCopyTokenId,
				secondCopyTokenRecord.getCopySourceId());
		assertEquals(firstCopyPointId,
				secondCopyPointRecord.getCopySourceId());
		assertExactDependencies(secondCopyPointRecord,
				secondCopyResultRecord.getId(), secondCopyTokenRecord.getId());
		GeoLocusIntersectionResult secondCopyResult =
				(GeoLocusIntersectionResult) registry.getGeo(
						secondCopyResultRecord.getId());
		GeoPoint secondCopyPoint =
				(GeoPoint) registry.getGeo(secondCopyPointRecord.getId());
		assertTrue(secondCopyPoint.isDefined());
		GeoPoint grandparentToken = add("W=Intersect("
				+ secondCopyResult.getLabelSimple() + ",\"" + originalToken
				+ "\")");
		assertFalse(grandparentToken.isDefined());

		String copiedState = copiedResult.getTokenLedgerState();
		String[] copiedStateFields = copiedState.split("\\|", -1);
		assertNotEquals("-", copiedStateFields[3]);
		String[] copiedSourceMaterial = copiedStateFields[3].split("~", -1);
		copiedSourceMaterial[1] = hex(unhex(copiedSourceMaterial[1])
				+ "/wrong");
		copiedStateFields[3] = String.join("~", copiedSourceMaterial);
		String copiedXml = getApp().getXML();
		String forgedCopiedXml = copiedXml.replace(
				"state=\"" + copiedState + "\"",
				"state=\"" + String.join("|", copiedStateFields) + "\"");
		assertNotEquals(copiedXml, forgedCopiedXml);
		AppCommon forgedCopiedApp = AppCommonFactory.create(
				new AppConfigGeoCeDG(true));
		SpatialIdentityException forgedCopied = assertThrows(
				SpatialIdentityException.class,
				() -> forgedCopiedApp.getXMLio().processXMLString(
						forgedCopiedXml, true, false, false));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				forgedCopied.getDiagnostic().getCode());
		assertTrue(forgedCopiedApp.getKernel().getConstruction()
				.getSpatialIdentityRegistry().isEmpty());
		assertFalse(forgedCopiedApp.getKernel().getConstruction().isFileLoading());
	}

	@Test
	void p07CrossConstructionCopyUsesFreshTargetOwnedClosure() {
		GeoLocusV2 locus = createParabola();
		GeoPoint point = semanticPoint(locus, 0.5);
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(point), text -> text);
		AppCommon target = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		paste(target, clipboard);
		Construction targetConstruction = target.getKernel().getConstruction();
		assertFalse(copyRecords(targetConstruction).isEmpty());
		assertTrue(targetConstruction.getGeoSetConstructionOrder().stream()
				.anyMatch(GeoLocusV2.class::isInstance));
	}

	@Test
	void p08AssignmentRequiresExplicitReplacementIntentAndPublishesFresh()
			throws Exception {
		createLine();
		add("a=2");
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		String dependencyBeforeXml = getApp().getXML();
		int dependencyBeforeSteps = getConstruction().steps();
		int dependencyBeforeRecords = registry.getRecords().size();
		int dependencyBeforeReservations = registry.getReservedIdentityCount();
		ErrorAccumulator dependencyErrors = new ErrorAccumulator();
		GeoElement state = requireLookup("s");
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				state, "a+1", redefineInfo(state), false, ignored -> { },
				dependencyErrors);
		assertFalse(dependencyErrors.getErrors().isBlank());
		assertEquals(dependencyBeforeXml, getApp().getXML());
		assertEquals(dependencyBeforeSteps, getConstruction().steps());
		assertEquals(dependencyBeforeRecords, registry.getRecords().size());
		assertEquals(dependencyBeforeReservations,
				registry.getReservedIdentityCount());

		ErrorAccumulator explicitDependencyErrors = new ErrorAccumulator();
		state = requireLookup("s");
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				state, "a+1",
				redefineInfo(state).withSpatialReplacementOperation(), false,
				ignored -> { }, explicitDependencyErrors);
		assertFalse(explicitDependencyErrors.getErrors().isBlank());
		assertEquals(dependencyBeforeXml, getApp().getXML());
		assertEquals(dependencyBeforeSteps, getConstruction().steps());
		assertEquals(dependencyBeforeRecords, registry.getRecords().size());
		assertEquals(dependencyBeforeReservations,
				registry.getReservedIdentityCount());

		add("Qa=(a,a^2)");
		add("Da={false,{-2,2,true,true}}");
		add("La=LocusV2(Qa,a,Da)");
		assertNotNull(registry.getPersistentGeoId(requireLookup("a")));
		String registeredBeforeXml = getApp().getXML();
		int registeredBeforeSteps = getConstruction().steps();
		int registeredBeforeRecords = registry.getRecords().size();
		int registeredBeforeReservations = registry.getReservedIdentityCount();
		for (boolean replacementIntent : List.of(false, true)) {
			ErrorAccumulator registeredDependencyErrors = new ErrorAccumulator();
			state = requireLookup("s");
			EvalInfo info = redefineInfo(state);
			if (replacementIntent) {
				info = info.withSpatialReplacementOperation();
			}
			getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
					state, "a+1", info, false, ignored -> { },
					registeredDependencyErrors);
			assertFalse(registeredDependencyErrors.getErrors().isBlank());
			assertEquals(registeredBeforeXml, getApp().getXML());
			assertEquals(registeredBeforeSteps, getConstruction().steps());
			assertEquals(registeredBeforeRecords, registry.getRecords().size());
			assertEquals(registeredBeforeReservations,
					registry.getReservedIdentityCount());
		}

		GeoLocusV2 source = (GeoLocusV2) requireLookup("L");
		GeoLocusV2 shell = (GeoLocusV2) source.copyInternal(getConstruction());
		GeoLocusV2 sourceForAssignment = source;
		assertThrows(UnsupportedOperationException.class,
				() -> shell.set(sourceForAssignment));
		assertFalse(shell.isDefined());
		GeoLocusIntersectionResult result = intersect(source, "x=0");
		GeoLocusIntersectionResult resultShell =
				(GeoLocusIntersectionResult) result.copyInternal(getConstruction());
		resultShell.set(result);
		assertFalse(resultShell.isDefined());

		GeoPoint oldDependent = semanticPoint(source, 0.25);
		final PersistentGeoId oldSourceId = idOf(registry, source);
		final PersistentGeoId oldDependentId = idOf(registry, oldDependent);
		String beforeXml = getApp().getXML();
		int beforeSteps = getConstruction().steps();
		int beforeGeos = getConstruction().getGeoSetConstructionOrder().size();
		int beforeRecords = registry.getRecords().size();
		int beforeReservations = registry.getReservedIdentityCount();
		String changedDefinition =
				"LocusV2((s,1),s,{false,{-1,1,true,true}})";
		ErrorAccumulator rejectedErrors = new ErrorAccumulator();
		AtomicReference<GeoElementND> rejectedCallback = new AtomicReference<>();

		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				source, changedDefinition, redefineInfo(source), false,
				rejectedCallback::set, rejectedErrors);

		assertFalse(rejectedErrors.getErrors().isBlank());
		assertEquals(beforeXml, getApp().getXML());
		assertEquals(beforeSteps, getConstruction().steps());
		assertEquals(beforeGeos,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(beforeRecords, registry.getRecords().size());
		assertEquals(beforeReservations, registry.getReservedIdentityCount());
		assertEquals(oldSourceId,
				idOf(registry, (GeoLocusV2) requireLookup("L")));
		assertEquals(oldDependentId,
				idOf(registry, (GeoPoint) requireLookup("P")));

		long commitsBefore = registry.getInstrumentation().getRedefineCommits();
		long freshBefore = registry.getInstrumentation()
				.getRedefineFreshDecisions();
		ErrorAccumulator replacementErrors = new ErrorAccumulator();
		AtomicReference<GeoElementND> replacementCallback = new AtomicReference<>();
		GeoLocusV2 current = (GeoLocusV2) requireLookup("L");
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				current, changedDefinition,
				redefineInfo(current).withSpatialReplacementOperation(), false,
				replacementCallback::set, replacementErrors);

		assertTrue(replacementErrors.getErrors().isBlank(),
				replacementErrors.getErrors());
		GeoLocusV2 replacement = (GeoLocusV2) requireLookup("L");
		assertNotNull(replacementCallback.get());
		assertEquals(replacement, replacementCallback.get().toGeoElement());
		PersistentGeoId replacementId = idOf(registry, replacement);
		assertNotEquals(oldSourceId, replacementId);
		assertEquals(commitsBefore + 1,
				registry.getInstrumentation().getRedefineCommits());
		assertEquals(freshBefore + 1,
				registry.getInstrumentation().getRedefineFreshDecisions());
		assertEquals(0, registry.getReservedIdentityCount());
		assertNull(registry.getGeo(oldSourceId));
		assertNull(registry.getGeo(oldDependentId));
		assertNull(lookup("P"));
		List<PersistentGeoId> directDependencies = Arrays.stream(
				replacement.getParentAlgorithm().getInput())
				.peek(input -> assertTrue(input.isLabelSet()))
				.map(input -> idOf(registry, input)).collect(Collectors.toList());
		GeoIdentityRecord replacementRecord = registry.getGeoRecord(replacementId);
		assertNotNull(replacementRecord);
		assertEquals(new LinkedHashSet<>(directDependencies),
				new LinkedHashSet<>(replacementRecord.getDependencies()));
		assertEquals(directDependencies.size(),
				replacementRecord.getDependencies().size());
	}

	@Test
	void p09SourceDeletionWithdrawsEveryDerivedAuthority() {
		GeoLocusV2 locus = createLine();
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoLocusIntersectionResult result = intersect(locus, "x=0");
		locus.remove();
		assertTrue(lookup("M") == null || !metric.isDefined());
		assertTrue(lookup("R") == null || !result.isDefined());
	}

	@Test
	void p10UndoAndRedoRestoreTheExactIdentityGraph() {
		activateUndo();
		getApp().storeUndoInfo();
		GeoLocusV2 locus = createLine();
		semanticPoint(locus, 0.5);
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId retainedId = idOf(registry, locus);
		final int beforeRetainSteps = getConstruction().steps();
		final int beforeRetainGeos = getConstruction().getGeoSetConstructionOrder()
				.size();
		final int beforeRetainRecords = registry.getRecords().size();
		final int beforeRetainReservations = registry.getReservedIdentityCount();
		AtomicBoolean callbackEntered = new AtomicBoolean();
		AtomicBoolean nestedRejected = new AtomicBoolean();
		View listener = mock(View.class);
		doAnswer(invocation -> {
			if (callbackEntered.compareAndSet(false, true)) {
				try {
					LocusV2PublicOperations.createScalar(getConstruction(),
							"NestedReentrant", (GeoPoint) requireLookup("Q"),
							(GeoNumeric) requireLookup("s"),
							(GeoNumeric) requireLookup("s"),
							LocusV2DomainDescriptor.parse(
									(GeoList) requireLookup("D")));
				} catch (SpatialIdentityException expected) {
					nestedRejected.set(true);
				}
			}
			return null;
		}).when(listener).repaintView();
		ErrorAccumulator retainErrors = new ErrorAccumulator();
		getKernel().attach(listener);
		try {
			getKernel().getAlgebraProcessor()
					.changeGeoElementNoExceptionHandling(locus,
							"LocusV2(Q,s,D)", redefineInfo(locus), false,
							ignored -> { }, retainErrors);
		} finally {
			getKernel().detach(listener);
		}
		assertTrue(retainErrors.getErrors().isBlank(),
				retainErrors.getErrors());
		assertTrue(callbackEntered.get());
		assertTrue(nestedRejected.get());
		assertNull(lookup("NestedReentrant"));
		assertEquals(beforeRetainSteps, getConstruction().steps());
		assertEquals(beforeRetainGeos,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(beforeRetainRecords, registry.getRecords().size());
		assertEquals(beforeRetainReservations,
				registry.getReservedIdentityCount());
		locus = (GeoLocusV2) requireLookup("L");
		assertEquals(retainedId, idOf(registry, locus));
		getApp().storeUndoInfo();
		final String createdXml = getApp().getXML();
		Set<PersistentGeoId> createdIds = ids(getConstruction());

		ErrorAccumulator replacementErrors = new ErrorAccumulator();
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				locus, "LocusV2((s,1),s,{false,{-1,1,true,true}})",
				redefineInfo(locus).withSpatialReplacementOperation(), false,
				ignored -> { }, replacementErrors);
		assertTrue(replacementErrors.getErrors().isBlank(),
				replacementErrors.getErrors());
		getApp().storeUndoInfo();
		GeoLocusV2 replacement = (GeoLocusV2) requireLookup("L");
		String replacementXml = getApp().getXML();
		Set<PersistentGeoId> replacementIds = ids(getConstruction());
		assertNotEquals(createdIds, replacementIds);
		assertFalse(replacementIds.isEmpty());

		replacement.remove();
		getApp().storeUndoInfo();
		assertNull(lookup("L"));

		getKernel().undo();
		assertEquals(replacementXml, getApp().getXML());
		assertEquals(replacementIds, ids(getConstruction()));
		getKernel().undo();
		assertEquals(createdXml, getApp().getXML());
		assertEquals(createdIds, ids(getConstruction()));
		getKernel().redo();
		assertEquals(replacementXml, getApp().getXML());
		assertEquals(replacementIds, ids(getConstruction()));
		getKernel().redo();
		assertNull(lookup("L"));
		getKernel().undo();
		assertEquals(replacementXml, getApp().getXML());
		assertEquals(replacementIds, ids(getConstruction()));
		reload();
		assertEquals(replacementXml, getApp().getXML());
		assertEquals(replacementIds, ids(getConstruction()));
		assertTrue(((GeoLocusV2) requireLookup("L")).isDefined());
	}

	@Test
	void p11LabelsLayersAndVisibilityAreNotPersistenceAuthority() {
		GeoLocusIntersectionResult result = intersect(createLine(), "x=0");
		String token = firstToken(result);
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(result);
		result.setLabel("RenamedResult");
		result.setLayer(7);
		result.setEuclidianVisible(!result.isEuclidianVisible());
		assertEquals(id, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(result));
		assertEquals(token, firstToken(result));
	}

	@Test
	void p12OldLegacyLocusCorpusLoadsWithoutSilentV2Migration()
			throws Exception {
		Path model = repositoryRoot().resolve("docs/references/cedg/models/g9p/"
				+ "geocedg-reference-locus-focal-sphere-illumination.ggb");
		getApp().setXML(readConstructionXml(model), true);
		assertTrue(getConstruction().getGeoSetConstructionOrder().stream()
				.noneMatch(GeoLocusV2.class::isInstance));
		assertFalse(getApp().getXML().contains("name=\"LocusV2\""));
	}

	@Test
	void p13GeoCeDGClassicPreservesNativeV2WithoutEnablingCreation()
			throws Exception {
		createParabola();
		String xml = getApp().getXML();
		AppCommon classic = AppCommonFactory.create(new AppConfigGeoCeDG(false));
		classic.getXMLio().processXMLString(xml, true, false, false);
		assertTrue(classic.getKernel().lookupLabel("L") instanceof GeoLocusV2);
		assertEquals(xml, classic.getXML());
		assertFalse(((AppConfigGeoCeDG) classic.getConfig())
				.getRuntimeFeatureService().isLocusV2CreationEnabled());
	}

	@Test
	void p14ExternalUpstreamBoundaryNeverDowngradesToPolyline()
			throws Exception {
		String xml = readFixture("external-upstream-no-downgrade.xml");
		getApp().getXMLio().processXMLString(xml, true, false, false);
		assertNotNull(lookup("A"));
		assertTrue(getConstruction().getGeoSetConstructionOrder().stream()
				.noneMatch(GeoLocusV2.class::isInstance));
		assertFalse(getApp().getXML().contains("locusV2"));
	}

	@Test
	void p15UnknownSemanticVersionFailsWithoutLegacyRepair()
			throws Exception {
		String xml = readFixture("future-provider.xml");
		assertThrows(RuntimeException.class, () ->
				getApp().getXMLio().processXMLString(xml, true, false, false));
		assertTrue(getConstruction().getGeoSetConstructionOrder().stream()
				.noneMatch(GeoLocusV2.class::isInstance));
	}

	@Test
	void p16CorruptOrPartialTokenLedgerFailsAtomically() throws Exception {
		GeoLocusV2 locus = createLine();
		GeoPoint semanticPoint = semanticPoint(locus, 0.25);
		GeoLocusMetricResult metric = totalMetric(locus);
		GeoNumeric scalar = add("len=Length(L)");
		GeoLocusMetricResult hiddenRich =
				((AlgoLocusMetricScalarAdapter) scalar.getParentAlgorithm())
						.getRichInput();
		GeoLocusIntersectionResult result = intersect(locus, "x=0");
		GeoPoint tokenPoint = tokenPoint(result, firstToken(result));
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		String valid = getApp().getXML();
		PersistentGeoId unrelated = idOf(registry, requireLookup("target"));
		for (GeoElement output : List.of(locus, semanticPoint, metric, hiddenRich,
				scalar, result, tokenPoint)) {
			PersistentGeoId outputId = idOf(registry, output);
			GeoIdentityRecord record = registry.getGeoRecord(outputId);
			assertFalse(record.getDependencies().isEmpty(), outputId.toString());
			String recordTag = geoIdentityTag(valid, outputId);
			assertMalformedIdentityDagLoad(valid.replace(recordTag,
					withoutDependencies(recordTag)), "omitted " + outputId);
			assertMalformedIdentityDagLoad(valid.replace(recordTag,
					withDependencies(recordTag, unrelated)), "swapped " + outputId);
		}
		GeoIdentityRecord locusRecord = registry.getGeoRecord(idOf(registry, locus));
		String locusRecordTag = geoIdentityTag(valid, locusRecord.getId());
		String[] baseNames = {"provider", "family", "schema"};
		String[] baseValues = {locusRecord.getProvider(), locusRecord.getFamily(),
				locusRecord.getSchemaId()};
		for (int field = 0; field < baseNames.length; field++) {
			String forgedTag = withIdentityAttribute(locusRecordTag,
					baseNames[field], baseValues[field], baseValues[field] + "/wrong");
			assertNotEquals(locusRecordTag, forgedTag, baseNames[field]);
			assertMalformedIdentityDagLoad(valid.replace(locusRecordTag, forgedTag),
					"forged " + baseNames[field]);
		}
		int start = valid.indexOf("<locusIntersectionTokenLedger ");
		int end = valid.indexOf("/>", start) + 2;
		assertTrue(start >= 0 && end > start);
		String tag = valid.substring(start, end);

		AppCommon missingApp = AppCommonFactory.create(
				new AppConfigGeoCeDG(true));
		missingApp.getXMLio().processXMLString(valid.replace(tag, ""), true,
				false, false);
		GeoLocusIntersectionResult missing =
				(GeoLocusIntersectionResult) missingApp.getKernel()
						.lookupLabel("R");
		assertNotNull(missing);
		assertFalse(missing.isDefined());
		assertNull(missing.getIntersectionResult());
		assertEquals("1|1|-|-", missing.getTokenLedgerState());

		String duplicate = valid.replace(tag, tag + tag);
		assertThrows(RuntimeException.class,
				() -> loadInFreshApp(duplicate));
		String noncanonical = valid.replace(tag,
				tag.replace("state=\"1|", "state=\"01|"));
		assertNotEquals(valid, noncanonical);
		assertThrows(RuntimeException.class,
				() -> loadInFreshApp(noncanonical));
		String corrupt = valid.replace(tag,
				tag.replace("state=\"1|", "state=\"999|"));
		assertNotEquals(valid, corrupt);
		assertThrows(RuntimeException.class,
				() -> loadInFreshApp(corrupt));
		assertFalse(getConstruction().isFileLoading());

		String[] stateFields = result.getTokenLedgerState().split("\\|", -1);
		assertEquals(4, stateFields.length);
		String[] material = stateFields[2].split("~", -1);
		assertTrue(material.length >= 5);
		assertEquals(result.getSourcePairIdentity(), unhex(material[1]));
		String[] materialNames = {"owner", "source pair", "constructive lineage",
				"topology context"};
		for (int field = 0; field < materialNames.length; field++) {
			String[] mismatchedState = stateFields.clone();
			String[] mismatchedMaterial = material.clone();
			mismatchedMaterial[field] = hex(unhex(mismatchedMaterial[field])
					+ "/wrong");
			mismatchedState[2] = String.join("~", mismatchedMaterial);
			String mismatchedTag = tag.replace(
					"state=\"" + result.getTokenLedgerState() + "\"",
					"state=\"" + String.join("|", mismatchedState) + "\"");
			assertNotEquals(tag, mismatchedTag, materialNames[field]);
			AppCommon mismatchApp = AppCommonFactory.create(
					new AppConfigGeoCeDG(true));
			SpatialIdentityException mismatch = assertThrows(
					SpatialIdentityException.class,
					() -> mismatchApp.getXMLio().processXMLString(
							valid.replace(tag, mismatchedTag), true, false, false),
					materialNames[field]);
			assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
					mismatch.getDiagnostic().getCode(), materialNames[field]);
			assertTrue(mismatchApp.getKernel().getConstruction()
					.getSpatialIdentityRegistry().isEmpty(), materialNames[field]);
			assertFalse(mismatchApp.getKernel().getConstruction().isFileLoading(),
					materialNames[field]);
		}
		String[] forgedCopySourceState = stateFields.clone();
		forgedCopySourceState[3] = forgedCopySourceState[2];
		String forgedCopySourceTag = tag.replace(
				"state=\"" + result.getTokenLedgerState() + "\"",
				"state=\"" + String.join("|", forgedCopySourceState) + "\"");
		AppCommon forgedCopySourceApp = AppCommonFactory.create(
				new AppConfigGeoCeDG(true));
		SpatialIdentityException forgedCopySource = assertThrows(
				SpatialIdentityException.class,
				() -> forgedCopySourceApp.getXMLio().processXMLString(
						valid.replace(tag, forgedCopySourceTag), true, false, false));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				forgedCopySource.getDiagnostic().getCode());
		assertTrue(forgedCopySourceApp.getKernel().getConstruction()
				.getSpatialIdentityRegistry().isEmpty());
		assertFalse(forgedCopySourceApp.getKernel().getConstruction()
				.isFileLoading());
		LocusIntersectionTokenLedger2D orphanedCopySource =
				new LocusIntersectionTokenLedger2D();
		assertThrows(IllegalArgumentException.class,
				() -> orphanedCopySource.importState(
						"1|1|-|" + stateFields[2]));
		assertEquals("1|1|-|-", orphanedCopySource.exportState());
		stateFields[1] = Long.toString(Long.MAX_VALUE);
		LocusIntersectionTokenLedger2D overflowCopyLedger =
				new LocusIntersectionTokenLedger2D();
		overflowCopyLedger.importState(String.join("|", stateFields));
		String sourceOwner = unhex(material[0]);
		String copiedOwner = sourceOwner + "/copy";
		String copiedPair = unhex(material[1]) + "/copy";
		overflowCopyLedger.authorizeImmediateCopy(sourceOwner);
		var evaluation = overflowCopyLedger.begin(copiedOwner, copiedPair,
				unhex(material[2]), unhex(material[3]));
		evaluation.mint(new IntersectionTokenLineage2D("overflow/solution",
				"overflow/branch", Optional.of("overflow/continuation")),
				new IntersectionRootAddressProof2D("overflow/provider",
						"overflow/target", 0.5));
		String overflowBefore = overflowCopyLedger.exportState();
		assertThrows(ArithmeticException.class, () -> overflowCopyLedger.commit(
				evaluation, result.getIntersectionResult()));
		assertEquals(overflowBefore, overflowCopyLedger.exportState());
		overflowCopyLedger.abort(evaluation);
		var failureEvaluation = overflowCopyLedger.begin(copiedOwner, copiedPair,
				unhex(material[2]), unhex(material[3]));
		overflowCopyLedger.commit(failureEvaluation,
				result.getIntersectionResult());
		String[] recovered = overflowCopyLedger.exportState().split("\\|", -1);
		String[] recoveredCurrent = recovered[2].split("~", -1);
		String[] recoveredSource = recovered[3].split("~", -1);
		assertEquals(copiedOwner, unhex(recoveredCurrent[0]));
		assertEquals(copiedPair, unhex(recoveredCurrent[1]));
		assertEquals("0", recoveredCurrent[4]);
		assertEquals(sourceOwner, unhex(recoveredSource[0]));
	}

	private static String geoIdentityTag(String xml, PersistentGeoId id) {
		String marker = "<geo id=\"" + id.toExternalForm() + "\" ";
		int start = xml.indexOf(marker);
		int end = xml.indexOf("/>", start) + 2;
		assertTrue(start >= 0 && end > start, id.toString());
		return xml.substring(start, end);
	}

	private static String withoutDependencies(String recordTag) {
		return recordTag.replaceFirst(" dependencies=\"[^\"]+\"", "");
	}

	private static String withDependencies(String recordTag,
			PersistentGeoId dependency) {
		return recordTag.replaceFirst(" dependencies=\"[^\"]+\"",
				" dependencies=\"" + dependency.toExternalForm() + "\"");
	}

	private static String withIdentityAttribute(String recordTag, String name,
			String current, String replacement) {
		return recordTag.replace(" " + name + "=\"" + current + "\"",
				" " + name + "=\"" + replacement + "\"");
	}

	private static void assertMalformedIdentityDagLoad(String xml,
			String description) {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		SpatialIdentityException failure = assertThrows(
				SpatialIdentityException.class,
				() -> app.getXMLio().processXMLString(xml, true, false, false),
				description);
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				failure.getDiagnostic().getCode(), description);
		assertTrue(app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().isEmpty(), description);
		assertFalse(app.getKernel().getConstruction().isFileLoading(),
				description);
	}

	private static Set<PersistentGeoId> ids(Construction construction) {
		return construction.getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.map(GeoIdentityRecord::getId).collect(Collectors.toSet());
	}

	private static List<GeoIdentityRecord> copyRecords(
			Construction construction) {
		return construction.getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> record.getCopySourceId() != null)
				.collect(Collectors.toList());
	}

	private static PersistentGeoId idOf(SpatialIdentityRegistry registry,
			org.geogebra.common.kernel.geos.GeoElement geo) {
		PersistentGeoId id = registry.getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
	}

	private static GeoIdentityRecord recordCopying(
			SpatialIdentityRegistry registry, PersistentGeoId source) {
		return registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> source.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
	}

	private static void assertExactDependencies(GeoIdentityRecord record,
			PersistentGeoId... dependencies) {
		assertEquals(new LinkedHashSet<>(Arrays.asList(dependencies)),
				new LinkedHashSet<>(record.getDependencies()));
		assertEquals(dependencies.length, record.getDependencies().size());
	}

	private static void paste(AppCommon app, String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(app, labels,
				clipboard.substring(separator));
	}

	private static void loadInFreshApp(String xml) throws Exception {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		app.getXMLio().processXMLString(xml, true, false, false);
	}

	private EvalInfo redefineInfo(GeoElement target) {
		return new EvalInfo(true, true)
				.withSymbolicMode(AlgebraProcessor.getRedefinitionMode(target,
						getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
	}

	private static String readFixture(String name) throws IOException {
		return Files.readString(FIXTURE_ROOT.resolve(name),
				StandardCharsets.UTF_8);
	}

	private static String unhex(String value) {
		return new String(java.util.HexFormat.of().parseHex(value),
				StandardCharsets.UTF_8);
	}

	private static String hex(String value) {
		return java.util.HexFormat.of().formatHex(
				value.getBytes(StandardCharsets.UTF_8));
	}

	private static String readConstructionXml(Path model) throws IOException {
		try (ZipFile archive = new ZipFile(model.toFile())) {
			ZipEntry entry = archive.getEntry("geogebra.xml");
			assertNotNull(entry, "geogebra.xml is missing from " + model);
			try (InputStream input = archive.getInputStream(entry)) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}

	private static Path repositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("geocedg"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}

	private IntersectionSnapshot snapshot(GeoLocusIntersectionResult result) {
		assertNotNull(result.getIntersectionResult());
		List<String> durableTokens = new ArrayList<>();
		int nonDurableHandles = 0;
		for (var solution : result.getIntersectionResult().getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			if (LocusSemanticIntersectionToken2D.decode(token).isPresent()) {
				durableTokens.add(token);
			} else {
				// Revision-local single-target handles and frozen G8 locus-pair
				// diagnostic tokens are intentionally outside the durable v3 codec.
				assertTrue(LocusSemanticIntersectionToken2D
						.isRevisionLocalHandle(token)
						|| token.startsWith("locus-pair-root/"), token);
				assertFalse(result.isPointAdmissible(token), token);
				nonDurableHandles++;
			}
		}
		return new IntersectionSnapshot(idOf(getConstruction()
				.getSpatialIdentityRegistry(), result),
				result.getSourcePairIdentity(), result.getTokenLedgerState(),
				result.getIntersectionResult().getGeometryKind(),
				durableTokens, nonDurableHandles);
	}

	private GeneratorSnapshot generatorSnapshot(GeoLocusV2 locus) {
		SemanticGeneratorDescriptor1D descriptor =
				((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
						.getGeneratorDescriptor();
		return new GeneratorSnapshot(idOf(getConstruction()
				.getSpatialIdentityRegistry(), locus), descriptor.getFamily(),
				descriptor.getSemanticSignature());
	}

	private record GeneratorSnapshot(PersistentGeoId locusId,
			SemanticGeneratorFamily1D family,
			String descriptorSignature) {
	}

	private record IntersectionSnapshot(PersistentGeoId resultId,
			String sourcePair, String ledgerState,
			GeometryKind geometry,
			List<String> durableTokens, int nonDurableHandles) {
	}
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.common.kernel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityInstrumentation;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/** Regression for input-bar preview isolation at the public V2 boundary. */
class LocusV2InputPreviewLifecycleTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void inputBarPreviewSkipsDurablePublicationAndDefinitiveExecutionStillSucceeds() {
		createControlLocus();
		createReportedInputs();
		GeoLocusV2 control = (GeoLocusV2) lookup("Existing");
		SpatialIdentityRegistry registry =
				getConstruction().getSpatialIdentityRegistry();
		SpatialIdentityInstrumentation identity = registry.getInstrumentation();
		SpatialSemanticInstrumentation semantic = getConstruction()
				.getSpatialSemanticRuntime().getInstrumentation();
		ScheduledPreviewFromInputBar preview =
				getKernel().getInputPreviewHelper();

		String xmlBefore = getApp().getXML();
		final int stepsBefore = getConstruction().steps();
		final int geosBefore = getConstruction().getGeoSetConstructionOrder().size();
		final int algorithmsBefore = getConstruction().getAlgoList().size();
		final long locusAlgorithmsBefore = locusAlgorithmCount();
		final long lociBefore = locusCount();
		final String recordsBefore = registry.writeSpatialSection();
		final String diagnosticsBefore = registry.getDiagnostics().toString();
		final int reservationsBefore = registry.getReservedIdentityCount();
		final long[] identityBefore = identitySnapshot(identity);
		final long publicationEpochBefore = semantic
				.getAuthoritativePublicationEpoch();
		final Map<SpatialIdentityId, Long> publicationCountsBefore =
				semantic.snapshotAuthoritativePublicationCounts();
		final long controlRevisionBefore = control.getSemanticRevision();
		ErrorAccumulator errors = new ErrorAccumulator();

		preview.updatePreviewFromInputBar("L=LocusV2(E,C)", errors);
		GeoElement[] previewGeos = preview.getPreview("L=LocusV2(E,C)");

		assertNotNull(previewGeos);
		assertEquals(0, previewGeos.length);
		assertEquals("", errors.getErrors());
		preview.clear();
		assertNull(lookup("L"));
		assertEquals(xmlBefore, getApp().getXML());
		assertEquals(stepsBefore, getConstruction().steps());
		assertEquals(geosBefore,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(algorithmsBefore, getConstruction().getAlgoList().size());
		assertEquals(locusAlgorithmsBefore, locusAlgorithmCount());
		assertEquals(lociBefore, locusCount());
		assertEquals(recordsBefore, registry.writeSpatialSection());
		assertEquals(diagnosticsBefore, registry.getDiagnostics().toString());
		assertEquals(reservationsBefore, registry.getReservedIdentityCount());
		assertArrayEquals(identityBefore, identitySnapshot(identity));
		assertEquals(publicationEpochBefore,
				semantic.getAuthoritativePublicationEpoch());
		assertEquals(publicationCountsBefore,
				semantic.snapshotAuthoritativePublicationCounts());
		assertEquals(controlRevisionBefore, control.getSemanticRevision());

		GeoLocusV2 definitive = add("L=LocusV2(E,C)");
		assertNotNull(definitive);
		assertEquals(SemanticGeneratorFamily1D.CIRCLE_POINT,
				((AlgoDependentPointLocusV2) definitive.getParentAlgorithm())
						.getGeneratorDescriptor().getFamily());
		assertNotNull(definitive.getPersistentLocusId());
	}

	private void createControlLocus() {
		add("s=0");
		add("Q=(s,s^2)");
		add("Domain={false,{-2,2,true,true}}");
		add("Existing=LocusV2(Q,s,Domain)");
	}

	private void createReportedInputs() {
		add("O=(1,2)");
		add("r=2");
		add("c=Circle(O,r)");
		add("C=Point(c)");
		add("g=PerpendicularLine(C,yAxis)");
		add("D=Intersect(g,yAxis)");
		add("E=Midpoint(C,D)");
	}

	private long locusAlgorithmCount() {
		return getConstruction().getAlgoList().stream()
				.filter(AlgoDependentPointLocusV2.class::isInstance).count();
	}

	private long locusCount() {
		return getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoLocusV2.class::isInstance).count();
	}

	private static long[] identitySnapshot(
			SpatialIdentityInstrumentation instrumentation) {
		return new long[] {
				instrumentation.getAllocationAttempts(),
				instrumentation.getAllocations(),
				instrumentation.getLifecyclePreparationAttempts(),
				instrumentation.getLifecyclePrepared(),
				instrumentation.getLifecyclePreflightRejects(),
				instrumentation.getLifecycleCommits(),
				instrumentation.getLifecycleRollbacks(),
				instrumentation.getLifecycleRecordCreates(),
				instrumentation.getLifecycleRecordReplacements(),
				instrumentation.getLifecycleRecordRetirements(),
				instrumentation.getLifecycleResolutionChanges()
		};
	}
}

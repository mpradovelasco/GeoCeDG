/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCertificationScope2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInstrumentationSnapshot2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T1: public counters of certification and revision work
 * ({@link LocusCertificationWork2D}) for gates GC-1 to GC-6 and D-CU2-20-R. They count
 * work only; this step changes no behaviour.
 */
class PreG9BCu2CertificationInstrumentationTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void certifyRecordsOneComputationAndClosesItsScope() {
		LocusInstrumentation2D owner = new LocusInstrumentation2D();
		Integer result = LocusCertificationScope2D.certify(owner, () -> {
			assertTrue(LocusCertificationScope2D.isOpen());
			LocusCertificationScope2D.recordMetricRead();
			return 7;
		});
		assertEquals(7, result);
		assertFalse(LocusCertificationScope2D.isOpen());
		assertEquals(1, owner.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
		assertEquals(1, owner.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
	}

	@Test
	void metricReadsOutsideAScopeAreNotCounted() {
		LocusInstrumentation2D owner = new LocusInstrumentation2D();
		LocusCertificationScope2D.recordMetricRead();
		LocusCertificationScope2D.certify(owner, () -> null);
		LocusCertificationScope2D.recordMetricRead();
		assertEquals(0, owner.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
	}

	@Test
	void nestedScopesReportToTheInnermostOwner() {
		LocusInstrumentation2D outer = new LocusInstrumentation2D();
		LocusInstrumentation2D inner = new LocusInstrumentation2D();
		LocusCertificationScope2D.certify(outer, () -> {
			LocusCertificationScope2D.recordMetricRead();
			LocusCertificationScope2D.certify(inner, () -> {
				LocusCertificationScope2D.recordMetricRead();
				LocusCertificationScope2D.recordMetricRead();
				return null;
			});
			LocusCertificationScope2D.recordMetricRead();
			return null;
		});
		assertEquals(2, outer.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
		assertEquals(2, inner.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
		assertEquals(1, outer.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
		assertEquals(1, inner.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
	}

	@Test
	void aFailingComputationStillClosesItsScope() {
		LocusInstrumentation2D owner = new LocusInstrumentation2D();
		assertThrows(IllegalStateException.class,
				() -> LocusCertificationScope2D.certify(owner, () -> {
					throw new IllegalStateException("certificate failure");
				}));
		assertFalse(LocusCertificationScope2D.isOpen());
		assertEquals(1, owner.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
	}

	@Test
	void nestedCopiesAggregateTheirWorkIntoTheSliceOwner() {
		LocusInstrumentation2D owner = new LocusInstrumentation2D();
		LocusInstrumentation2D copy = new LocusInstrumentation2D();
		copy.recordCertificationWork(LocusCertificationWork2D.FINGERPRINT_COMPUTATION, 3);
		copy.recordCertificationWork(LocusCertificationWork2D.CERTIFICATE_COMPUTATION);
		copy.attachNestedParent(owner);
		copy.recordCertificationWork(LocusCertificationWork2D.FINGERPRINT_COMPUTATION);
		copy.recordCertificationWork(LocusCertificationWork2D.FIDELITY_FAILURE, 0);
		assertEquals(0, owner.getCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
		assertEquals(4, owner.getNestedCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
		assertEquals(1, owner.getNestedCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
		assertEquals(0, owner.getNestedCertificationWork(
				LocusCertificationWork2D.FIDELITY_FAILURE));
		LocusInstrumentationSnapshot2D snapshot = owner.snapshot();
		assertEquals(4, snapshot.getNestedCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
		assertEquals(0, snapshot.getCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
		owner.reset();
		assertEquals(0, owner.getNestedCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
		assertEquals(4, snapshot.getNestedCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
	}

	@Test
	void kernelProducersReportCertificatesAndFingerprintsWithoutMetricReads() {
		add("r=3");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		LocusInstrumentation2D instrumentation = locus.getInstrumentation();
		assertTrue(instrumentation.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION) >= 1);
		assertTrue(instrumentation.getCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION) >= 1);
		long revision = locus.getSemanticRevision();
		long certificates = instrumentation.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION);
		GeoNumeric radius = assertInstanceOf(GeoNumeric.class, lookup("r"));
		radius.setValue(3.5);
		radius.updateRepaint();
		assertEquals(revision + 1, locus.getSemanticRevision());
		assertTrue(instrumentation.getCertificationWork(
				LocusCertificationWork2D.CERTIFICATE_COMPUTATION) > certificates);
		assertEquals(0, instrumentation.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
	}

	@Test
	void metricValuesComputedOrReadInsideACertificateAreCounted() {
		add("c=Circle((0,0),2)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		add("l=LocusV2(Q,P)");
		GeoLocusMetricResult length = add("len=LocusLength(l)");
		LocusInstrumentation2D owner = new LocusInstrumentation2D();
		LocusCertificationScope2D.certify(owner, () -> length.getMetricResult());
		assertEquals(1, owner.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
		LocusCertificationScope2D.certify(owner, () -> {
			length.getParentAlgorithm().compute();
			return null;
		});
		assertEquals(2, owner.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
		length.getMetricResult();
		assertEquals(2, owner.getCertificationWork(
				LocusCertificationWork2D.METRIC_READ_DURING_CERTIFICATION));
	}

	@Test
	void renderCacheCountsTheVerticesOfEachBuild() {
		add("c=Circle((0,0),2)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		LocusRenderCache2D cache = new LocusRenderCache2D();
		LocusRenderPolicy2D policy = LocusRenderPolicy2D.adaptive(1, 800, 600, 40, 40,
				32, 0.75, 8);
		LocusRenderData2D data = cache.getOrBuild(locus, policy);
		int vertices = data.getVertices().size();
		assertTrue(vertices > 0);
		assertEquals(vertices, cache.getLastBuildVertices());
		assertEquals(vertices, cache.getBuiltVertices());
		assertEquals(vertices, cache.getMaximumBuildVertices());
		assertSame(data, cache.getOrBuild(locus, policy));
		assertEquals(vertices, cache.getBuiltVertices());
		assertEquals(1, cache.getBuilds());
	}
}

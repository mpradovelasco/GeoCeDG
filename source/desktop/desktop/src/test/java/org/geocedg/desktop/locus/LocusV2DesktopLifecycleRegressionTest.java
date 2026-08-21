/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.desktop.AppGeoCeDG;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Desktop bootstrap-to-EDT regression for public Locus V2 creation. */
class LocusV2DesktopLifecycleRegressionTest {

	@BeforeAll
	static void initializeDesktopFactories() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void launcherCreatedConstructionSupportsScheduledPreviewOnEdt()
			throws Exception {
		assertFalse(SwingUtilities.isEventDispatchThread());
		AppGeoCeDG app = enabledApp();
		app.setErrorDialogsActive(false);
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(SwingUtilities.isEventDispatchThread());
			createReportedInputs(app);
		});
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		int recordsBefore = registry.size();
		int reservationsBefore = registry.getReservedIdentityCount();
		int diagnosticsBefore = registry.getDiagnostics().size();
		int algorithmsBefore = app.getKernel().getConstruction()
				.getAlgoList().size();
		int geosBefore = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		CountDownLatch previewUpdated = new CountDownLatch(1);
		AtomicReference<GeoElement[]> previewOutput = new AtomicReference<>();
		View listener = mock(View.class);
		doAnswer(invocation -> {
			previewOutput.set(invocation.getArgument(0));
			previewUpdated.countDown();
			return null;
		}).when(listener).updatePreviewFromInputBar(any());
		app.getKernel().attach(listener);
		ErrorAccumulator previewErrors = new ErrorAccumulator();

		try {
			SwingUtilities.invokeAndWait(() -> app.getKernel()
					.getInputPreviewHelper().updatePreviewFromInputBar(
							"L=LocusV2(E,C)", previewErrors));
			assertTrue(previewUpdated.await(10, TimeUnit.SECONDS));
			assertNotNull(previewOutput.get());
			assertEquals(0, previewOutput.get().length);
			assertEquals("", previewErrors.getErrors());
			assertEquals(recordsBefore, registry.size());
			assertEquals(reservationsBefore,
					registry.getReservedIdentityCount());
			assertEquals(diagnosticsBefore, registry.getDiagnostics().size());
			assertEquals(algorithmsBefore, app.getKernel().getConstruction()
					.getAlgoList().size());
			assertEquals(geosBefore, app.getKernel().getConstruction()
					.getGeoSetConstructionOrder().size());
		} finally {
			app.cancelPreview();
			app.getKernel().detach(listener);
			SwingUtilities.invokeAndWait(
					() -> app.getKernel().getInputPreviewHelper().clear());
		}
	}

	@Test
	void launcherCreatedConstructionSupportsDefinitiveEdtCreation()
			throws Exception {
		assertFalse(SwingUtilities.isEventDispatchThread());
		AppGeoCeDG app = enabledApp();
		app.setErrorDialogsActive(false);
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(SwingUtilities.isEventDispatchThread());
			createReportedInputs(app);
		});
		CapturingErrorHandler definitiveErrors = new CapturingErrorHandler();
		AtomicReference<GeoElementND[]> output = new AtomicReference<>();

		SwingUtilities.invokeAndWait(() -> {
			assertTrue(SwingUtilities.isEventDispatchThread());
			output.set(app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(
							"L=LocusV2(E,C)", false, definitiveErrors,
							false, null));
		});

		assertNull(definitiveErrors.getFailure());
		assertEquals("", definitiveErrors.getErrors());
		assertNotNull(output.get());
		assertEquals(1, output.get().length);
		GeoLocusV2 locus = assertInstanceOf(GeoLocusV2.class,
				output.get()[0].toGeoElement());
		AlgoDependentPointLocusV2 parent =
				assertInstanceOf(AlgoDependentPointLocusV2.class,
						locus.getParentAlgorithm());
		assertEquals(SemanticGeneratorFamily1D.CIRCLE_POINT,
				parent.getGeneratorDescriptor().getFamily());
		assertNotNull(app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(locus));
	}

	private static AppGeoCeDG enabledApp() {
		return new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
	}

	private static void createReportedInputs(AppGeoCeDG app) {
		eval(app, "O=(1,2)");
		eval(app, "r=2");
		eval(app, "c=Circle(O,r)");
		eval(app, "C=Point(c)");
		eval(app, "g=PerpendicularLine(C,yAxis)");
		eval(app, "D=Intersect(g,yAxis)");
		eval(app, "E=Midpoint(C,D)");
	}

	private static GeoElement eval(AppGeoCeDG app, String command) {
		GeoElementND[] output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		assertTrue(output.length > 0, command);
		return output[0].toGeoElement();
	}

	private static final class CapturingErrorHandler extends ErrorAccumulator {
		private Throwable failure;

		@Override
		public void log(Throwable throwable) {
			failure = throwable;
		}

		private Throwable getFailure() {
			return failure;
		}
	}
}

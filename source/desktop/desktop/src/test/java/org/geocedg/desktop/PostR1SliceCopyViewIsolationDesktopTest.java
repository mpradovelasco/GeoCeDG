/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Post-R1 repair F8 on the Desktop application: updating and painting Graphics 1 while a
 * consumer of a nested locus rebuilds slice copies never modifies the drawables of the view
 * that is being updated.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
final class PostR1SliceCopyViewIsolationDesktopTest {

	private static final int WIDTH = 900;
	private static final int HEIGHT = 700;

	@Test
	void theViewUpdatesAndPaintsWhileNestedCopiesAreRebuilt() throws Throwable {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				scenario();
			} catch (Throwable thrown) {
				failure.set(thrown);
			}
		});
		if (failure.get() != null) {
			throw failure.get();
		}
	}

	private static void scenario() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"c=Circle((0,0),2)", "E2=Point(c)",
				"k=Circle((0,0),0.6)", "T=Point(k)", "QO=(2,0)", "QT=Midpoint(T,QO)",
				"Mt=Midpoint(E2,QT)", "Lt=LocusV2(Mt,E2)", "HA=(4,0.1)", "HB=(-3,0.2)",
				"l=Line(HA,HB)"}) {
			G9U1TestApp.eval(app, command);
		}
		move(app, 0.4);
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) G9U1TestApp.eval(app, "v=Intersect(Lt,l)");
		String token = rich.getIntersectionResult().getFiniteSolutions().get(0).getIdentity()
				.getRootToken();
		G9U1TestApp.eval(app, "RA=Intersect(v,\"" + token + "\")");
		G9U1TestApp.eval(app, "ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = (GeoLocusV2) G9U1TestApp.eval(app, "Zt=LocusV2(ZP,T)");
		GeoLocusV2 nested = (GeoLocusV2) G9U1TestApp.lookup(app, "Lt");
		app.getKernel().updateConstruction(false);

		EuclidianView view = app.getEuclidianView1();
		((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
		// Updating every drawable of the view iterates its drawable list.
		view.updateSize();
		paint(view);
		for (double angle : new double[] {1.1, 2.0, -2.5, -1.2, 0.0, 2.9}) {
			move(app, angle);
			view.updateSize();
			paint(view);
			assertNotNull(nested.getSemanticDefinition(), "the nested locus at T = " + angle);
			assertNotNull(consumer.getSemanticDefinition(), "the consumer at T = " + angle);
		}
	}

	private static void move(AppGeoCeDG app, double angle) {
		GeoPoint point = (GeoPoint) G9U1TestApp.lookup(app, "T");
		point.setCoords(0.6 * Math.cos(angle), 0.6 * Math.sin(angle), 1.0);
		point.updateCascade();
	}

	private static void paint(EuclidianView view) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, WIDTH, HEIGHT);
			view.paint(new GGraphics2DD(graphics));
		} finally {
			graphics.dispose();
		}
	}
}

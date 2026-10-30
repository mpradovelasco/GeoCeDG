/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Isolated diagnostic, outside product/test discovery; author file never modified. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SNativeAuthorDiagnosticTest {

 @BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

 @Test void actualAuthorFile() {
  AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
  assertTrue(app.loadFile(new File("C:/Temp/TestM08-S.cedg"), false));
  GeoLocusV2 source = (GeoLocusV2) app.getKernel().lookupLabel("h");
  assertNotNull(source);
  var definition = source.getSemanticDefinition();
  assertNotNull(definition);
  String branch = definition.getBranches().get(0).getBranchKey();
  double x; double y;
  try (var session = LocusEvaluationSession2D.reference()) {
   var value = source.evaluate(branch, 0, session);
   assertNotNull(value.getPoint());
   x = value.getPoint().getX(); y = value.getPoint().getY();
  }
  GeoPoint reference = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
     "ReferenceR1=(" + (x + 0.01) + "," + y + ")");
  var policy = LocusPointInteractionPolicy2D.initial(0.1);
  var result = new LocusPointInteractionResolver2D().resolve(
    new LocusPointInteractionQuery2D(source, reference.getInhomX(), reference.getInhomY(), policy));
  Cu7M08SGeneralLocusDiagnosticTest.trace(source, result);
  var view = app.getEuclidianView1();
  boolean hit = new DrawLocusV2(view, source).hit(view.toScreenCoordX(x), view.toScreenCoordY(y), 4);
  System.out.println("AUTHOR h=" + source.getPersistentLocusId() + " Q=" + reference.getInhomX()
     + "," + reference.getInhomY() + " hit=" + hit);
  String before = app.getXML();
  GeoPoint created = new GeoCeDGPointInteraction(app).create(source, reference.getInhomX(),
    reference.getInhomY(), 0.1, ignored -> null);
  System.out.println("clickCreated=" + (created != null));
  if (created == null) { assertEquals(before, app.getXML()); }
  GeoPoint forward = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
     "ForwardR1=Point(h,\"" + branch + "\",0)");
  System.out.println("forwardDefined=" + forward.isDefined() + " x=" + forward.getInhomX()
     + " y=" + forward.getInhomY());
 }
}

/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Path;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Explicit opt-in probe: actual author file, current admitted multiple-result route. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SNativeAuthoringDiagnosticTest {
 @BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

 @Test void nativeMultipleSelectionSnapshotAndReopen(@TempDir Path directory) {
  AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
  assertTrue(app.loadFile(new File("C:/Temp/TestM08-S.cedg"), false));
  GeoLocusV2 h = (GeoLocusV2) app.getKernel().lookupLabel("h");
  GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
     "ReferenceMultipleR1=(0.24829332226784415,1.8854818601338847)");
  GeoCeDGPointInteraction operation = new GeoCeDGPointInteraction(app);
  GeoPoint point = operation.createFromReference(h, q,
     LocusPointInteractionPolicy2D.initial(1), result -> {
      Cu7M08SGeneralLocusDiagnosticTest.trace(h, result);
      assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES, result.getStatus());
      assertFalse(result.getSearchCoverage().establishesCompleteRequestedScope());
      return result.getCandidates().get(0);
     });
  assertNotNull(point);
  assertFalse(point.getAllPredecessors().contains(q));
  var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
  var address = parent.getCurrentSemanticAddress();
  var id = app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
  double x = point.getInhomX(); double y = point.getInhomY();
  q.setCoords(20,20,1); q.updateCascade();
  assertEquals(x, point.getInhomX()); assertEquals(y, point.getInhomY());
  assertFalse(operation.move(point, x + 0.01, y, 0.1));
  assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
     operation.getLastResult().getStatus());
  assertEquals(address, parent.getCurrentSemanticAddress());
  System.out.println("AUTHOR general drag rejection=" + operation.getLastResult().getStatus()
     + " cause=" + operation.getLastResult().getReason());
  Path output = directory.resolve("author-snapshot.cedg");
  assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(output.toFile()));
  AppGeoCeDG reopened = G9U1SemanticPointInteractionTest.app();
  assertTrue(reopened.loadFile(output.toFile(), false));
  GeoPoint restored = (GeoPoint) reopened.getKernel().lookupLabel(point.getLabelSimple());
  assertEquals(id, reopened.getKernel().getConstruction().getSpatialIdentityRegistry()
     .getPersistentGeoId(restored));
  assertEquals(address, ((AlgoSemanticLocusPoint2D) restored.getParentAlgorithm())
     .getCurrentSemanticAddress());
  assertFalse(restored.getAllPredecessors().contains(reopened.getKernel()
     .lookupLabel("ReferenceMultipleR1")));
  System.out.println("AUTHOR admitted multiple: forward-only Q snapshot, native identity/address reopen OK");
 }
}

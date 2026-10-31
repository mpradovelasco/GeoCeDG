/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.lang.reflect.*;
import java.math.BigDecimal;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.*;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Isolated real-Java characterization, never product discovery/admission. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SRealCaptureProbeTest {
 @BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }
 @Test void authorizedBridgeOnExactNativeHasForwardOnlyPersistenceAndCopy(
   @org.junit.jupiter.api.io.TempDir java.nio.file.Path directory) throws Exception {
  String input = System.getProperty("geocedg.m08.native.file", "C:/Temp/TestM08-S.cedg");
  String hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
    .digest(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(input))));
  assertEquals("4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d",hash);
  AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
  assertTrue(app.loadFile(new File(input),false));
  GeoLocusV2 h = (GeoLocusV2) app.getKernel().lookupLabel("h");
  assertNull(app.getKernel().lookupLabel("ReferenceR1"));
  GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
    "ReferenceR1=(0.8976484384297626,1.7854818601338847)");
  String before = app.getXML();
  var selection = M08SnapshotPlacement2D.queryFromReference(h,q,LocusPointInteractionPolicy2D.initial(0.1));
  assertEquals(before,app.getXML());
  assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
    selection.getResult().getHistoricalResult().getStatus());
  assertEquals(M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,
    selection.getResult().getPermission());
  assertEquals(1,selection.getResult().getChoices().size());
  var choice=selection.getResult().getChoices().get(0);
  assertEquals(selection.getResult().getHistoricalResult().getCandidates().get(0).getAddress(),choice.getAddress());
  System.out.println("NATIVE_BRIDGE_PERMISSION="+selection.getResult().getPermission()+" historical="
    +selection.getResult().getHistoricalResult().getStatus()+" grade="+choice.getEvidence().getNumericGuarantee()
    +" distanceSquaredUpper="+choice.getEvidence().getDistanceSquaredUpper());
  GeoPoint p=selection.commit("NativeP",choice);
  var registry=app.getKernel().getConstruction().getSpatialIdentityRegistry();
  var id=registry.getPersistentGeoId(p);
  assertNotNull(id);
  assertFalse(p.getAllPredecessors().contains(q));
  assertNull(registry.getPersistentGeoId(q));
  assertEquals(choice.getAddress(),((org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D)
    p.getParentAlgorithm()).getCurrentSemanticAddress());
  double x=p.inhomX,y=p.inhomY;
  q.setCoords(20,30,1);q.updateCascade();
  assertEquals(x,p.inhomX,0);assertEquals(y,p.inhomY,0);
  java.nio.file.Path output=directory.resolve("native-m08-bridge.cedg");
  assertTrue(((GuiManagerGeoCeDG)app.getGuiManager()).saveAsTo(output.toFile()));
  AppGeoCeDG reopened=G9U1SemanticPointInteractionTest.app();
  assertTrue(reopened.loadFile(output.toFile(),false));
  GeoPoint restored=(GeoPoint)reopened.getKernel().lookupLabel("NativeP");
  assertEquals(id,reopened.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(restored));
  assertEquals(choice.getAddress(),((org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D)
    restored.getParentAlgorithm()).getCurrentSemanticAddress());
  String clipboard=org.geogebra.common.util.InternalClipboard.getTextToSave(app,java.util.List.of(p),text->text);
  assertFalse(clipboard.contains("ReferenceR1"));
  G9U1SemanticPointInteractionTest.paste(app,clipboard);
  var copied=registry.getRecords().stream()
    .filter(org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord.class::isInstance)
    .map(org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord.class::cast)
    .filter(record->id.equals(record.getCopySourceId())).findFirst().orElseThrow();
  assertNotEquals(id,copied.getId());
  assertFalse(((GeoPoint)registry.getGeo(copied.getId())).getAllPredecessors().contains(q));
  assertEquals(hash,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
    .digest(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(input)))));
  System.out.println("NATIVE_CEDG_REOPEN_ID_ADDRESS=true COPY_LINEAGE=true NO_Q_PARENT=true ORIGINAL_HASH_UNCHANGED=true");
 }
 @Test void captureNativeHBeforeAnyNewAdmission() throws Exception {
  AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
  assertTrue(app.loadFile(new File(System.getProperty("geocedg.m08.native.file", "C:/Temp/TestM08-S.cedg")), false));
  assertNull(app.getKernel().lookupLabel("ReferenceR1"));
  GeoLocusV2 source = (GeoLocusV2) app.getKernel().lookupLabel("h");
  GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
    "ReferenceR1=(0.8976484384297626,1.7854818601338847)");
  String before = app.getXML();
  var definition = source.getSemanticDefinition();
  var r6 = new LocusPointInteractionResolver2D().resolve(new LocusPointInteractionQuery2D(
    source, q.inhomX, q.inhomY, LocusPointInteractionPolicy2D.initial(0.1)));
  assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH, r6.getStatus());
  assertEquals(1, r6.getCandidates().size());
  var chosen = r6.getCandidates().get(0);
  var evaluator = (ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability();
  var program = evaluator.captureCertifiedProgram();
  System.out.println("NATIVE capturePresent=" + program.isPresent() + " revision="
    + definition.getSemanticRevision() + " R6=" + r6.getStatus() + " address="
    + chosen.getAddress().getCanonicalParameter());
  assertTrue(program.isPresent(), "Native capture is a scientific gate, never replaced by a mock");
  System.out.println("PROGRAM version=" + program.get().getVersion() + " driver=" + program.get().getDriver()
    + " signature=" + program.get().getSignature());
  for (int i=0;i<8;i++) System.out.println("driverConstant["+i+"]="+program.get().getDriverConstant(i));
  program.get().getSteps().forEach(step -> System.out.println("step=" + step.getOperation()));
  Class<?> modelClass = Class.forName("org.geocedg.common.kernel.locus.intersection.CertifiedIntervalCurveModel2D");
  Method capture = modelClass.getDeclaredMethod("capture", LocusDefinition2D.class, String.class);
  capture.setAccessible(true);
  Object model = capture.invoke(null, definition, chosen.getAddress().getBranchKey());
  assertNotNull(model);
  Class<?> intervalClass = Class.forName("org.geocedg.common.kernel.locus.intersection.SplineOutwardInterval2D");
  Constructor<?> interval = intervalClass.getDeclaredConstructor(double.class,double.class);
  interval.setAccessible(true);
  Method evaluate = modelClass.getDeclaredMethod("evaluate",intervalClass,boolean.class);
  evaluate.setAccessible(true);
  Field lower = intervalClass.getDeclaredField("lower"), upper = intervalClass.getDeclaredField("upper");
  lower.setAccessible(true); upper.setAccessible(true);
  double u = chosen.getAddress().getCanonicalParameter();
  Object[] pointBox = (Object[]) evaluate.invoke(model,interval.newInstance(u,u),false);
  Object[] pointJet = (Object[]) evaluate.invoke(model,interval.newInstance(u,u),true);
  BigDecimal distanceUpper = BigDecimal.ZERO;
  for(int i=0;i<2;i++) {
   double lo=lower.getDouble(pointBox[i]), hi=upper.getDouble(pointBox[i]);
   System.out.println("F["+i+"]=["+lo+","+hi+"] derivative=["
     +lower.getDouble(pointJet[i])+","+upper.getDouble(pointJet[i])+"]");
   BigDecimal query = new BigDecimal(i==0?q.inhomX:q.inhomY);
   BigDecimal dl = new BigDecimal(lo).subtract(query), dh = new BigDecimal(hi).subtract(query);
   distanceUpper = distanceUpper.add(dl.multiply(dl).max(dh.multiply(dh)));
  }
  assertTrue(distanceUpper.compareTo(new BigDecimal(0.1).pow(2))<0);
  boolean regular = false;
  for(int i=0;i<2;i++) regular |= lower.getDouble(pointJet[i])>0 || upper.getDouble(pointJet[i])<0;
  assertTrue(regular);
  boolean neighborhoodEstablished = false;
  for(double width:new double[]{1E-12,1E-6,0.01}) {
   try {
    Object[] jet = (Object[]) evaluate.invoke(model,interval.newInstance(u-width,u+width),true);
    boolean nonzero = false;
    for(int i=0;i<2;i++) nonzero |= lower.getDouble(jet[i])>0 || upper.getDouble(jet[i])<0;
    System.out.println("NEIGHBORHOOD width="+width+" regular="+nonzero);
    neighborhoodEstablished |= nonzero;
   } catch(InvocationTargetException e) { System.out.println("NEIGHBORHOOD refusal="+e.getCause()); }
  }
  assertTrue(neighborhoodEstablished);
  assertEquals(before, app.getXML(), "Capture must not mutate authoritative document state");
  System.out.println("SCIENTIFIC_PREREQUISITE nativeModelPointBoundAndRegularNeighborhood=true XML_UNCHANGED=true");
 }
}

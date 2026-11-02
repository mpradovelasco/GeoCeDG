/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.List;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Exact native document through real AWT/Desktop listeners. No native input or visible windows. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SDesktopEventsNativeProbeTest {
 @BeforeAll static void initialize(){G9U1SemanticPointInteractionTest.initializeDesktop();}
 @Test void nativeEventsConfirmationPersistenceAndCopy(@org.junit.jupiter.api.io.TempDir Path directory)throws Exception {
  Cu7M08SDesktopEventsTest.edt(()->{
   Path input=Path.of("C:/Temp/TestM08-S.cedg");String expected="4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d";
   assertEquals(expected,hash(input));var app=G9U1SemanticPointInteractionTest.app();assertTrue(app.loadFile(input.toFile(),false));
   var h=(GeoLocusV2)app.getKernel().lookupLabel("h");assertNull(app.getKernel().lookupLabel("ReferenceR1"));
   var q=Cu7M08SDesktopEventsTest.q(app,"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
   Cu7M08SDesktopEventsTest.start(app);Cu7M08SDesktopEventsTest.pointer(app,.8,2.4);Cu7M08SDesktopEventsTest.assertL(app,h);
   saveInput(app,"after-L");
   String before=app.getXML();Cu7M08SDesktopEventsTest.pointer(app,q.inhomX,q.inhomY);
   assertTrue(app.getEuclidianView1().getHits().contains(q));var tool=Cu7M08SDesktopEventsTest.tool(app);var selection=tool.pendingSelection();
   assertEquals(GeoCeDGReferencePlacement.Route.R2_H1,selection.getRoute());var result=selection.getLocalSelection().getResult();
   assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,result.getHistoricalResult().getStatus());
   assertEquals(M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,result.getPermission());assertEquals(before,app.getXML());
   var evidence=result.getChoices().get(0).getEvidence();var address=selection.getChoices().get(0).getAddress();
   System.out.println("NATIVE_EVENTS_QUERY mode="+app.getMode()+" L_HIT=true L_RETAINED=true Q_HIT=true route="+selection.getRoute()+" permission="+result.getPermission()+" grade="+evidence.getNumericGuarantee()+" distanceSquaredUpper="+evidence.getDistanceSquaredUpper());
   var registry=app.getKernel().getConstruction().getSpatialIdentityRegistry();
   saveInput(app,"confirm-H1");
   var prior=registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast).map(GeoIdentityRecord::getId).collect(java.util.stream.Collectors.toSet());
   Cu7M08SDesktopEventsTest.confirm(app);
   var added=registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
    .filter(r->!prior.contains(r.getId())).map(r->registry.getGeo(r.getId())).filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast).filter(GeoCeDGPointInteraction::owns).toList();
   assertEquals(1,added.size());var p=added.get(0);p.setLabel("NativeEventsP");
   var id=registry.getPersistentGeoId(p);assertNotNull(id);assertEquals(address,((AlgoSemanticLocusPoint2D)p.getParentAlgorithm()).getCurrentSemanticAddress());
   assertFalse(p.getAllPredecessors().contains(q));assertNull(registry.getPersistentGeoId(q));
   double x=p.inhomX,y=p.inhomY;q.setCoords(20,30,1);q.updateCascade();assertEquals(x,p.inhomX,0);assertEquals(y,p.inhomY,0);
   Path saved=directory.resolve("events-native.cedg");assertTrue(((GuiManagerGeoCeDG)app.getGuiManager()).saveAsTo(saved.toFile()));
   var reopened=G9U1SemanticPointInteractionTest.app();assertTrue(reopened.loadFile(saved.toFile(),false));var restored=(GeoPoint)reopened.getKernel().lookupLabel("NativeEventsP");
   assertEquals(id,reopened.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(restored));
   assertEquals(address,((AlgoSemanticLocusPoint2D)restored.getParentAlgorithm()).getCurrentSemanticAddress());
   String clipboard=InternalClipboard.getTextToSave(app,List.of(p),text->text);assertFalse(clipboard.contains("ReferenceR1"));
   G9U1SemanticPointInteractionTest.paste(app,clipboard);
   var copy=registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
    .filter(r->id.equals(r.getCopySourceId())).findFirst().orElseThrow();assertNotEquals(id,copy.getId());assertFalse(((GeoPoint)registry.getGeo(copy.getId())).getAllPredecessors().contains(q));
   assertEquals(expected,hash(input));app.setMode(EuclidianConstants.MODE_MOVE);
   System.out.println("NATIVE_EVENTS_COMMIT AWT_BUTTON=true FORWARD_ADDRESS=true NO_Q_PARENT=true REOPEN_ID=true COPY_REMAP=true ORIGINAL_UNCHANGED=true NO_NATIVE_INPUT=true");
  });
 }
 static void saveInput(AppGeoCeDG app,String name)throws Exception {
  Path root=Path.of(System.getProperty("geocedg.events.layout.output","artifacts/pre-g9b-cu7-i1-r2-desktop-events/layout"));Files.createDirectories(root);
  var input=((GuiManagerGeoCeDG)app.getGuiManager()).getAlgebraInput();input.setSize(1000,input.getPreferredSize().height);layout(input);
  var image=new java.awt.image.BufferedImage(input.getWidth(),input.getHeight(),java.awt.image.BufferedImage.TYPE_INT_RGB);
  var g=image.createGraphics();try{g.setColor(java.awt.Color.WHITE);g.fillRect(0,0,image.getWidth(),image.getHeight());input.printAll(g);}finally{g.dispose();}
  assertTrue(javax.imageio.ImageIO.write(image,"png",root.resolve(name+".png").toFile()));
  System.out.println("OFFSCREEN_LAYOUT="+root.resolve(name+".png")+"; NOT_A_NATIVE_SCREENSHOT=true");
 }
 static void layout(java.awt.Container root){root.doLayout();for(var c:root.getComponents())if(c instanceof java.awt.Container child)layout(child);}
 static String hash(Path input)throws Exception{return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));}
}

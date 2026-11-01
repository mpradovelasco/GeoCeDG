/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SReferenceToolTest {
 @BeforeAll static void initialize(){G9U1SemanticPointInteractionTest.initializeDesktop();}
 private GeoCeDGReferencePointTool tool(AppGeoCeDG app){
  var tool=new GeoCeDGReferencePointTool(app);tool.activate();return tool;
 }
 private GeoPoint q(AppGeoCeDG app){return (GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(0.3,0.02)");}
 @Test void orderEmptyClicksAndCancelNeverCreateFreePoints(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);var t=tool(app);
  String before=app.getXML();t.click(List.of(q),null,0,0);t.click(List.of(),null,0,0);assertNull(t.pendingSelection());
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);assertNotNull(t.pendingSelection());assertEquals(before,app.getXML());
  t.reset();assertNull(t.pendingSelection());assertEquals(before,app.getXML());
 }
 @Test void uniqueLocalRequiresConfirmationAndQDoesNotFollow(){
  var app=G9U1SemanticPointInteractionTest.app();var s=general(app);var q=reference(app);var t=tool(app);
  String before=app.getXML();t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);
  var selection=t.pendingSelection();assertNotNull(selection);assertEquals(before,app.getXML());
  assertEquals(GeoCeDGReferencePlacement.Route.R2_H1,selection.getRoute());
  assertEquals(M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,selection.getLocalSelection().getResult().getPermission());
  var p=t.commit(selection.getChoices().get(0));assertNotNull(p);assertFalse(p.getAllPredecessors().contains(q));
  assertNull(app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(q));
  double x=p.getInhomX();q.setCoords(3,4,1);q.updateCascade();assertEquals(x,p.getInhomX(),0);assertNull(t.pendingSelection());t.reset();
 }
 @Test void dependentReferenceRemainsTransient(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);q(app);
  GeoPoint r=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"R=Q+(0.1,0)");var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(r),null,0,0);var p=t.commit(t.pendingSelection().getChoices().get(0));
  assertFalse(p.getAllPredecessors().contains(r));assertFalse(p.getAllPredecessors().contains(app.getKernel().lookupLabel("Q")));t.reset();
 }
 @Test void staleChoiceRejectsAndCanStartAgain(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var choice=t.pendingSelection().getChoices().get(0);
  q.setCoords(.4,.02,1);q.updateCascade();String before=app.getXML();assertNull(t.commit(choice));assertEquals(before,app.getXML());
  assertNull(t.pendingSelection());t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);assertNotNull(t.pendingSelection());t.reset();
 }
 @Test void switchingModeCancelsPendingCreation(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);
  app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);var controller=G9U1SemanticPointInteractionTest.controller(app);
  var t=controller.referencePointTool();t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var selection=t.pendingSelection();
  String before=app.getXML();app.setMode(EuclidianConstants.MODE_POINT);
  assertNull(t.pendingSelection());assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED,selection.getValidity());assertEquals(before,app.getXML());
 }

 private static <T> T component(java.awt.Container root, Class<T> type){
  for(var c:root.getComponents()){
   if(type.isInstance(c))return type.cast(c);
   if(c instanceof java.awt.Container child){T found=component(child,type);if(found!=null)return found;}
  }return null;
 }
 private javax.swing.JButton button(java.awt.Container root){
  for(var c:root.getComponents()){
   if(c instanceof javax.swing.JButton b && ("Confirm".equals(b.getText())||"Confirmar".equals(b.getText())))return b;
   if(c instanceof java.awt.Container child){var found=button(child);if(found!=null)return found;}
  }return null;
 }
 private javax.swing.JButton confirm(GeoCeDGReferencePointTool tool){
  return button(tool.contextMenu());
 }
 @Test void oneChoiceIsOnlyPublishedAfterContextualConfirmation(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var selection=t.pendingSelection();
  assertNotNull(selection);assertTrue(confirm(t).isEnabled());String before=app.getXML();
  var button=confirm(t);button.doClick();assertNotEquals(before,app.getXML());String after=app.getXML();
  button.doClick();assertEquals(after,app.getXML());assertNull(t.pendingSelection());t.reset();
 }
 @Test void multipleHistoricalAddressesHaveNoDefaultChoice(){
  var app=G9U1SemanticPointInteractionTest.app();
  for(String command:List.of("I=(0.4965866445356883,1.7854818601338847)","J=(-0.5082406249102692,2.5763181370126467)",
   "f=Circle(I,J)","D=Point(f)","g=Line(D,xAxis)","K=Intersect(g,yAxis)","M=Midpoint(K,D)","h=LocusV2(M,D)",
   "Q=(0.24829332226784415,1.8854818601338847)"))G9U1SemanticPointInteractionTest.eval(app,command);
  var t=tool(app);t.setPolicy(org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D.initial(1));
  t.click(List.of(app.getKernel().lookupLabel("h")),null,0,0);t.click(List.of(app.getKernel().lookupLabel("Q")),null,0,0);
  assertNotNull(t.pendingSelection());assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,t.pendingSelection().getRoute());
  assertNull(t.pendingSelection().getLocalSelection());assertTrue(t.pendingSelection().getChoices().size()>1);
  var button=confirm(t);assertFalse(button.isEnabled());var list=component(t.contextMenu(),javax.swing.JList.class);
  assertEquals(-1,list.getSelectedIndex());assertEquals(-1,list.getFixedCellWidth());String before=app.getXML();button.doClick();assertEquals(before,app.getXML());
  list.setSelectedIndex(1);assertTrue(button.isEnabled());button.doClick();assertNotEquals(before,app.getXML());t.reset();
 }
 @Test void budgetExhaustionAndEscapeCancelWithoutMutation(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);
  app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);var c=G9U1SemanticPointInteractionTest.controller(app);var t=c.referencePointTool();
  t.setPolicy(new org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D(.1,1e-10,1e-12,
   new org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D(1,2,1,1)));
  String before=app.getXML();t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);
  assertNull(t.pendingSelection());assertTrue(t.lastFailure().contains("BUDGET_EXHAUSTED"));assertEquals(before,app.getXML());
  t.setPolicy(org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D.initial(.1));
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);assertNotNull(t.pendingSelection());c.cancelReferencePoint();
  assertNull(t.pendingSelection());assertEquals(before,app.getXML());t.reset();
 }
 @Test void sourceAncestorAndItsDependentsPropagateNormally(){
  var app=G9U1SemanticPointInteractionTest.app();G9U1SemanticPointInteractionTest.spline(app);
  var q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(0,0)");
  var s=(GeoLocusV2)G9U1SemanticPointInteractionTest.eval(app,"T=Dilate(S,2,Q)");var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var p=t.commit(t.pendingSelection().getChoices().get(0));
  assertNotNull(p);assertFalse(java.util.Arrays.asList(p.getParentAlgorithm().getInput()).contains(q));assertTrue(p.getAllPredecessors().contains(q));
  var dependent=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Z="+p.getLabelSimple()+"+(1,0)");
  q.setCoords(1,0,1);q.updateCascade();assertEquals(-1,p.getInhomX(),1e-8);assertEquals(0,dependent.getInhomX(),1e-8);t.reset();
 }
 @Test void advancedDialogIsCompactWithoutReferenceTab(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);
  app.setLanguage(java.util.Locale.ENGLISH);
  assertTrue(app.getToolHelp(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT).contains("existing point"));
  app.setLanguage(java.util.Locale.forLanguageTag("es"));
  assertTrue(app.getToolHelp(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT).contains("punto existente"));
  app.setLanguage(java.util.Locale.ENGLISH);
  try(var dialogs=org.mockito.Mockito.mockStatic(javax.swing.JOptionPane.class)){
   dialogs.when(()->javax.swing.JOptionPane.showConfirmDialog(org.mockito.ArgumentMatchers.any(java.awt.Component.class),
    org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyInt(),org.mockito.ArgumentMatchers.anyInt()))
    .thenAnswer(call->{
     var panel=(javax.swing.JPanel)call.getArgument(1);assertNull(component(panel,javax.swing.JTabbedPane.class));
     assertTrue(panel.getPreferredSize().width<600);var field=component(panel,javax.swing.JTextField.class);assertEquals(18,field.getColumns());
     String key="semantic-address-".repeat(100);field.setText(key);assertEquals(key,field.getText());assertTrue(panel.getPreferredSize().width<600);
     return javax.swing.JOptionPane.CANCEL_OPTION;
    });
   String before=app.getXML();assertNull(new GeoCeDGLocusV2Dialogs(app).createSemanticPoint(s));assertEquals(before,app.getXML());
  }
 }

 private GeoLocusV2 general(AppGeoCeDG app){
  for(String command:List.of("I=(0.4965866445356883,1.7854818601338847)","J=(-0.5082406249102692,2.5763181370126467)",
   "f=Circle(I,J)","D=Point(f)","g=Line(D,xAxis)","K=Intersect(g,yAxis)","M=Midpoint(K,D)","h=LocusV2(M,D)"))
   G9U1SemanticPointInteractionTest.eval(app,command);
  return (GeoLocusV2)app.getKernel().lookupLabel("h");
 }
 private GeoPoint reference(AppGeoCeDG app){
  return (GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
 }
 @Test void d1EndpointRestoresHistoricalAuthorityWithoutRelaxingH1(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);
  var q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(2.02,0.01)");
  var policy=LocusPointInteractionPolicy2D.initial(.1);String before=app.getXML();
  try(var h1=M08SnapshotPlacement2D.queryFromReference(s,q,policy)){
   assertEquals(M08SnapshotPlacement2D.Permission.NOT_OFFERABLE,h1.getResult().getPermission());
   assertEquals(M08SnapshotPlacement2D.Failure.COMPONENT_NOT_ESTABLISHED,h1.getResult().getFailure());
  }
  var t=tool(app);t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var selection=t.pendingSelection();
  assertNotNull(selection);assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,selection.getRoute());
  assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,selection.getHistoricalResult().getStatus());
  assertNull(selection.getLocalSelection());assertEquals(before,app.getXML());assertEquals(1,selection.getChoices().size());
  var p=t.commit(selection.getChoices().get(0));assertNotNull(p);assertEquals(2,p.getInhomX(),1e-12);assertEquals(0,p.getInhomY(),1e-12);
  assertFalse(p.getAllPredecessors().contains(q));assertNull(app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(q));
  assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED,selection.getValidity());
  System.out.println("D1_AFTER R6=UNIQUE_ADMISSIBLE_PREIMAGE H1=COMPONENT_NOT_ESTABLISHED tool=R1_R6 P=(2,0) NO_Q_PARENT=true");t.reset();
 }
 @Test void d1MultipleSplineCandidatesRequireExplicitChoice(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.eval(app,
   "S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
  var q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(0,1)");var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var selection=t.pendingSelection();
  assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,selection.getRoute());assertNull(selection.getLocalSelection());
  assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,selection.getHistoricalResult().getStatus());
  assertTrue(selection.getChoices().size()>1);assertFalse(confirm(t).isEnabled());String before=app.getXML();
  confirm(t).doClick();assertEquals(before,app.getXML());var choice=selection.getChoices().get(1);
  var list=component(t.contextMenu(),javax.swing.JList.class);list.setSelectedIndex(1);confirm(t).doClick();
  var p=app.getKernel().getConstruction().getGeoSetConstructionOrder().stream().filter(GeoPoint.class::isInstance)
   .map(GeoPoint.class::cast).filter(GeoCeDGPointInteraction::owns).findFirst().orElseThrow();
  assertEquals(choice.getAddress(),((org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D)p.getParentAlgorithm()).getCurrentSemanticAddress());t.reset();
 }
 @Test void d1HistoricalBudgetAndAbsenceAreTerminalWithoutH1Query(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);
  var tiny=new LocusPointInteractionPolicy2D(.1,1e-10,1e-12,new LocusPointInteractionWorkBudget2D(1,2,1,1));
  String before=app.getXML();
  try(var selection=new GeoCeDGReferencePlacement(s,q,tiny)){
   assertEquals(GeoCeDGReferencePlacement.Route.REJECTED,selection.getRoute());assertEquals("BUDGET_EXHAUSTED",selection.getFailure());
   assertNull(selection.getLocalSelection());assertTrue(selection.getChoices().isEmpty());
  }assertEquals(before,app.getXML());q.setCoords(20,20,1);q.updateCascade();before=app.getXML();
  try(var selection=new GeoCeDGReferencePlacement(s,q,LocusPointInteractionPolicy2D.initial(.1))){
   assertEquals(GeoCeDGReferencePlacement.Route.REJECTED,selection.getRoute());assertEquals("CERTIFIED_ABSENCE",selection.getFailure());assertNull(selection.getLocalSelection());
  }assertEquals(before,app.getXML());
 }
 @Test void d1HistoricalSeamAdmissionIsNotOverriddenByH1Rejection(){
  var app=G9U1SemanticPointInteractionTest.app();var s=general(app);var q=(GeoPoint)app.getKernel().lookupLabel("I");
  var policy=LocusPointInteractionPolicy2D.initial(1);String before=app.getXML();
  try(var h1=M08SnapshotPlacement2D.queryFromReference(s,q,policy)){
   assertEquals(M08SnapshotPlacement2D.Permission.NOT_OFFERABLE,h1.getResult().getPermission());
   assertEquals(M08SnapshotPlacement2D.Failure.COMPONENT_NOT_ESTABLISHED,h1.getResult().getFailure());
  }
  try(var selection=new GeoCeDGReferencePlacement(s,q,policy)){
   assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,selection.getRoute());assertNull(selection.getLocalSelection());
   assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,selection.getHistoricalResult().getStatus());
   assertFalse(selection.getChoices().isEmpty());
  }assertEquals(before,app.getXML());
 }
 @Test void d1H1ExhaustionWithNoHistoricalAdmissionStillRejects(){
  var app=G9U1SemanticPointInteractionTest.app();var s=general(app);var q=reference(app);long discovery;
  try(var measured=M08SnapshotPlacement2D.queryFromReference(s,q,LocusPointInteractionPolicy2D.initial(.1))){
   assertEquals(M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,measured.getResult().getPermission());
   discovery=measured.getWorkSnapshot().getDiscoveryMisses();
  }
  var limited=new LocusPointInteractionPolicy2D(.1,1e-10,1e-12,new LocusPointInteractionWorkBudget2D((int)discovery,512,80,1024));
  String before=app.getXML();try(var selection=new GeoCeDGReferencePlacement(s,q,limited)){
   assertEquals(GeoCeDGReferencePlacement.Route.REJECTED,selection.getRoute());assertTrue(selection.getChoices().isEmpty());
   assertEquals(LocusPointInteractionResult2D.Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED,selection.getHistoricalResult().getReason());
   assertNotNull(selection.getLocalSelection());assertEquals(M08SnapshotPlacement2D.Failure.BUDGET_EXHAUSTED,selection.getLocalSelection().getResult().getFailure());
   assertEquals(0,selection.getLocalSelection().getWorkSnapshot().getPublicationAttempts());
  }assertEquals(before,app.getXML());
 }
 @Test void d1RoutesCannotExchangeChoicesOrReuseConsumedSelection(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var h=general(app);var q=q(app);var r=reference(app);
  try(var historical=new GeoCeDGReferencePlacement(s,q,LocusPointInteractionPolicy2D.initial(.1));
   var local=new GeoCeDGReferencePlacement(h,r,LocusPointInteractionPolicy2D.initial(.1))){
   assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,historical.getRoute());assertEquals(GeoCeDGReferencePlacement.Route.R2_H1,local.getRoute());
   var hChoice=historical.getChoices().get(0);var lChoice=local.getChoices().get(0);String before=app.getXML();
   assertEquals("FOREIGN_CHOICE",assertThrows(GeoCeDGReferencePlacement.Rejected.class,()->historical.commit(lChoice)).getMessage());
   assertEquals("CONSUMED_SELECTION",assertThrows(GeoCeDGReferencePlacement.Rejected.class,()->historical.commit(hChoice)).getMessage());
   assertEquals("FOREIGN_CHOICE",assertThrows(GeoCeDGReferencePlacement.Rejected.class,()->local.commit(hChoice)).getMessage());
   assertEquals(before,app.getXML());
  }
 }
 @Test void d1LocalStaleReferenceAndCancellationLeaveNoMutation(){
  var app=G9U1SemanticPointInteractionTest.app();var s=general(app);var q=reference(app);var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var selection=t.pendingSelection();assertEquals(GeoCeDGReferencePlacement.Route.R2_H1,selection.getRoute());
  q.setCoords(.9,1.8,1);q.updateCascade();String before=app.getXML();assertNull(t.commit(selection.getChoices().get(0)));
  assertEquals("STALE_SELECTION",t.lastFailure());assertEquals(before,app.getXML());assertNull(t.pendingSelection());
  q.setCoords(.8976484384297626,1.7854818601338847,1);q.updateCascade();t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);
  selection=t.pendingSelection();assertNotNull(selection);before=app.getXML();var choice=selection.getChoices().get(0);t.reset();
  var consumed=selection;assertThrows(GeoCeDGReferencePlacement.Rejected.class,()->consumed.commit(choice));assertEquals(before,app.getXML());
 }
 @Test void d1EndpointNativeReopenAndCopyKeepForwardAddress(@org.junit.jupiter.api.io.TempDir java.nio.file.Path directory){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);
  var q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(2.02,0.01)");var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var choice=t.pendingSelection().getChoices().get(0);var p=t.commit(choice);
  var registry=app.getKernel().getConstruction().getSpatialIdentityRegistry();var id=registry.getPersistentGeoId(p);assertNotNull(id);
  String label=p.getLabelSimple();var file=directory.resolve("d1-endpoint.cedg");assertTrue(((GuiManagerGeoCeDG)app.getGuiManager()).saveAsTo(file.toFile()));
  var reopened=G9U1SemanticPointInteractionTest.app();assertTrue(reopened.loadFile(file.toFile(),false));var restored=(GeoPoint)reopened.getKernel().lookupLabel(label);
  assertEquals(id,reopened.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(restored));
  assertEquals(choice.getAddress(),((org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D)restored.getParentAlgorithm()).getCurrentSemanticAddress());
  assertFalse(restored.getAllPredecessors().contains(reopened.getKernel().lookupLabel("Q")));
  String clipboard=org.geogebra.common.util.InternalClipboard.getTextToSave(app,List.of(p),text->text);G9U1SemanticPointInteractionTest.paste(app,clipboard);
  var copy=registry.getRecords().stream().filter(org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord.class::isInstance)
   .map(org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord.class::cast).filter(r->id.equals(r.getCopySourceId())).findFirst().orElseThrow();
  assertNotEquals(id,copy.getId());var copied=(GeoPoint)registry.getGeo(copy.getId());assertTrue(copied.isDefined());
  var copiedAddress=((org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D)copied.getParentAlgorithm()).getCurrentSemanticAddress();
  assertNotEquals(choice.getAddress().getSourceLocusId(),copiedAddress.getSourceLocusId());
  assertEquals(choice.getAddress().getProviderVersion(),copiedAddress.getProviderVersion());
  assertEquals(choice.getAddress().getCanonicalParameter(),copiedAddress.getCanonicalParameter(),0);
  assertEquals(choice.getAddress().getPeriodicLift(),copiedAddress.getPeriodicLift());assertEquals(choice.getAddress().getSeamSide(),copiedAddress.getSeamSide());
  assertTrue(registry.getRecords().stream().filter(org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord.class::isInstance)
   .map(org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord.class::cast).anyMatch(r->r.getId().equals(copiedAddress.getSourceLocusId())
    && choice.getAddress().getSourceLocusId().equals(r.getCopySourceId())));assertFalse(copied.getAllPredecessors().contains(q));t.reset();
 }
}

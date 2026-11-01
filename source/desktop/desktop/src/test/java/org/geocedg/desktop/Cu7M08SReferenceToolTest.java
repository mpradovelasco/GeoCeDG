/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
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
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);var t=tool(app);
  String before=app.getXML();t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);
  var selection=t.pendingSelection();assertNotNull(selection);assertEquals(before,app.getXML());
  assertEquals(M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,selection.getResult().getPermission());
  var p=t.commit(selection.getResult().getChoices().get(0));assertNotNull(p);assertFalse(p.getAllPredecessors().contains(q));
  assertNull(app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(q));
  double x=p.getInhomX();q.setCoords(3,4,1);q.updateCascade();assertEquals(x,p.getInhomX(),0);assertNull(t.pendingSelection());t.reset();
 }
 @Test void dependentReferenceRemainsTransient(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);q(app);
  GeoPoint r=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"R=Q+(0.1,0)");var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(r),null,0,0);var p=t.commit(t.pendingSelection().getResult().getChoices().get(0));
  assertFalse(p.getAllPredecessors().contains(r));assertFalse(p.getAllPredecessors().contains(app.getKernel().lookupLabel("Q")));t.reset();
 }
 @Test void staleChoiceRejectsAndCanStartAgain(){
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app);var t=tool(app);
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var choice=t.pendingSelection().getResult().getChoices().get(0);
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
 @Test void multipleLocalAddressesHaveNoDefaultChoice(){
  var app=G9U1SemanticPointInteractionTest.app();
  for(String command:List.of("I=(0.4965866445356883,1.7854818601338847)","J=(-0.5082406249102692,2.5763181370126467)",
   "f=Circle(I,J)","D=Point(f)","g=Line(D,xAxis)","K=Intersect(g,yAxis)","M=Midpoint(K,D)","h=LocusV2(M,D)",
   "Q=(0.24829332226784415,1.8854818601338847)"))G9U1SemanticPointInteractionTest.eval(app,command);
  var t=tool(app);t.setPolicy(org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D.initial(1));
  t.click(List.of(app.getKernel().lookupLabel("h")),null,0,0);t.click(List.of(app.getKernel().lookupLabel("Q")),null,0,0);
  assertNotNull(t.pendingSelection());assertTrue(t.pendingSelection().getResult().getChoices().size()>1);
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
  t.click(List.of(s),null,0,0);t.click(List.of(q),null,0,0);var p=t.commit(t.pendingSelection().getResult().getChoices().get(0));
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
}

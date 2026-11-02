/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.*;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Real Component -> AWT listeners -> controller, with no visible windows or native input.
 * Not a physical focus/mouse smoke. Kernel services are never invoked as substitute clicks. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SDesktopEventsTest {
 @BeforeAll static void initialize(){G9U1SemanticPointInteractionTest.initializeDesktop();}
 interface Checked {void run() throws Exception;}
 static void edt(Checked action)throws Exception {
  AtomicReference<Throwable> failure=new AtomicReference<>();
  SwingUtilities.invokeAndWait(()->{try{action.run();}catch(Throwable e){failure.set(e);}});
  if(failure.get()!=null)throw new AssertionError(failure.get());
 }
 static void pointer(AppGeoCeDG app,double x,double y){
  for(int id:new int[]{MouseEvent.MOUSE_MOVED,MouseEvent.MOUSE_PRESSED,MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED}){mouse(app,id,x,y);}
 }
 static void mouse(AppGeoCeDG app,int id,double x,double y){
  var view=app.getEuclidianView1();JPanel canvas=view.getJPanel();
  canvas.dispatchEvent(new MouseEvent(canvas,id,System.currentTimeMillis(),id==MouseEvent.MOUSE_PRESSED?InputEvent.BUTTON1_DOWN_MASK:0,
   view.toScreenCoordX(x),view.toScreenCoordY(y),id==MouseEvent.MOUSE_MOVED?0:1,false,
   id==MouseEvent.MOUSE_MOVED?MouseEvent.NOBUTTON:MouseEvent.BUTTON1));
 }
 static void press(AbstractButton button){
  assertNotNull(button);button.setSize(button.getPreferredSize());
  for(int id:new int[]{MouseEvent.MOUSE_ENTERED,MouseEvent.MOUSE_PRESSED,MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED}){
   button.dispatchEvent(new MouseEvent(button,id,System.currentTimeMillis(),id==MouseEvent.MOUSE_PRESSED?InputEvent.BUTTON1_DOWN_MASK:0,
    5,5,1,false,MouseEvent.BUTTON1));
  }
 }
 static <T extends Component>T find(Container root,Class<T> type,java.util.function.Predicate<T> accept){
  if(root==null)return null;
  for(Component c:root.getComponents()){
   if(type.isInstance(c)&&accept.test(type.cast(c)))return type.cast(c);
   if(c instanceof Container child){T result=find(child,type,accept);if(result!=null)return result;}
  }return null;
 }
 static JButton button(Container root,String text){return find(root,JButton.class,b->text.equals(b.getText()));}
 static GeoCeDGReferencePointTool tool(AppGeoCeDG app){return G9U1SemanticPointInteractionTest.controller(app).referencePointTool();}
 static JPanel panel(AppGeoCeDG app){return (JPanel)((BorderLayout)((GuiManagerGeoCeDG)app.getGuiManager()).getAlgebraInput().getLayout()).getLayoutComponent(BorderLayout.NORTH);}
 static JTextField radius(AppGeoCeDG app){return find(panel(app),JTextField.class,f->"m08-policy-0".equals(f.getName()));}
 static GeoPoint q(AppGeoCeDG app,String definition){
  var q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,definition);q.setEuclidianVisible(true);q.updateRepaint();return q;
 }
 static void start(AppGeoCeDG app){app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);}
 static GeoPoint point(AppGeoCeDG app){return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
  .filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast).filter(GeoCeDGPointInteraction::owns).findFirst().orElseThrow();}
 static void confirm(AppGeoCeDG app){press(button(tool(app).contextMenu(),"Confirm"));}
 static GeoLocusV2 general(AppGeoCeDG app){
  for(String command:List.of("I=(0.4965866445356883,1.7854818601338847)","J=(-0.5082406249102692,2.5763181370126467)",
   "f=Circle(I,J)","D=Point(f)","g=Line(D,xAxis)","K=Intersect(g,yAxis)","M=Midpoint(K,D)","h=LocusV2(M,D)"))
   G9U1SemanticPointInteractionTest.eval(app,command);
  return (GeoLocusV2)app.getKernel().lookupLabel("h");
 }
 static void assertL(AppGeoCeDG app,GeoLocusV2 source){assertTrue(app.getSelectionManager().getSelectedGeos().contains(source));}

 @Test void endpointUsesRealListenersAndInlineConfirmationSurvivesFrameHandler()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(2.02,0.01)");
  var frame=new GeoCeDGFrame();frame.setApplication(app);var main=(JPanel)app.getMainComponent();
  main.setLayout(new BorderLayout());main.add(app.buildApplicationPanel());frame.setContentPane(main);
  try{
   start(app);pointer(app,-1,0);assertL(app,s);assertNull(tool(app).pendingSelection());
   var c=G9U1SemanticPointInteractionTest.controller(app);assertEquals(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT,c.getMode());
   c.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT,ModeSetter.DOCK_PANEL);assertL(app,s);
   var canvas=app.getEuclidianView1().getJPanel();canvas.dispatchEvent(new FocusEvent(canvas,FocusEvent.FOCUS_LOST));
   canvas.dispatchEvent(new FocusEvent(canvas,FocusEvent.FOCUS_GAINED));assertL(app,s);
   String before=app.getXML();pointer(app,q.inhomX,q.inhomY);assertTrue(app.getEuclidianView1().getHits().contains(q));
   var selection=tool(app).pendingSelection();assertNotNull(selection);assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,selection.getRoute());
   assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,selection.getHistoricalResult().getStatus());
   assertSame(panel(app),tool(app).contextMenu().getParent().getParent());
   assertSame(((GuiManagerGeoCeDG)app.getGuiManager()).getAlgebraInput(),panel(app).getParent());assertSame(frame,SwingUtilities.getWindowAncestor(panel(app)));
   frame.windowLostFocus(new WindowEvent(frame,WindowEvent.WINDOW_LOST_FOCUS));
   assertSame(selection,tool(app).pendingSelection());assertTrue(tool(app).contextMenu().isVisible());
   assertEquals(0,java.util.Arrays.stream(frame.getOwnedWindows()).filter(w->w.getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow")).count());
   pointer(app,q.inhomX,q.inhomY);assertSame(selection,tool(app).pendingSelection());assertEquals(before,app.getXML());
   confirm(app);var p=point(app);assertEquals(2,p.inhomX,1e-12);assertEquals(0,p.inhomY,1e-12);assertFalse(p.getAllPredecessors().contains(q));
   assertNull(tool(app).pendingSelection());assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());
   double x=p.inhomX;q.setCoords(20,30,1);q.updateCascade();assertEquals(x,p.inhomX,0);
   System.out.println("EVENTS_AFTER R1_R6 L_RETAINED=true Q_HIT=true INLINE_CONFIRM=true FRAME_LOST_FOCUS_SAFE=true P=(2,0) NO_NATIVE_INPUT=true");
  }finally{app.setMode(EuclidianConstants.MODE_MOVE);frame.setContentPane(new JPanel());frame.dispose();}
 });}

 @Test void eachAWTBoundaryRetainsLAndQueriesOnlyOnQPress()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");start(app);String before=app.getXML();
  mouse(app,MouseEvent.MOUSE_MOVED,-1,0);assertNull(tool(app).pendingSelection());
  for(int id:new int[]{MouseEvent.MOUSE_PRESSED,MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED}){
   mouse(app,id,-1,0);assertL(app,s);assertTrue(app.getEuclidianView1().getHits().contains(s));assertNull(tool(app).pendingSelection());
   System.out.println("AWT_BOUNDARY target=L event="+id+" mode="+app.getMode()+" L_SELECTED=true QUERY=false");
  }
  mouse(app,MouseEvent.MOUSE_MOVED,q.inhomX,q.inhomY);assertL(app,s);assertNull(tool(app).pendingSelection());
  mouse(app,MouseEvent.MOUSE_PRESSED,q.inhomX,q.inhomY);var selection=tool(app).pendingSelection();assertNotNull(selection);
  for(int id:new int[]{MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED}){
   mouse(app,id,q.inhomX,q.inhomY);assertSame(selection,tool(app).pendingSelection());assertL(app,s);assertTrue(app.getEuclidianView1().getHits().contains(q));
   System.out.println("AWT_BOUNDARY target=Q event="+id+" mode="+app.getMode()+" L_RETAINED=true Q_HIT=true SAME_QUERY=true");
  }
  assertEquals(before,app.getXML());confirm(app);assertNotNull(point(app));tool(app).reset();
 });}

 @Test void generalFixtureRequiresExplicitLocalConfirmation()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=general(app);var q=q(app,"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
  start(app);pointer(app,.8,2.4);assertL(app,s);String before=app.getXML();pointer(app,q.inhomX,q.inhomY);
  var selection=tool(app).pendingSelection();assertEquals(GeoCeDGReferencePlacement.Route.R2_H1,selection.getRoute());
  assertEquals(M08SnapshotPlacement2D.Permission.EXPLICIT_LOCAL_SELECTION_REQUIRED,selection.getLocalSelection().getResult().getPermission());
  assertEquals(1,selection.getChoices().size());assertEquals(before,app.getXML());assertTrue(button(tool(app).contextMenu(),"Confirm").isEnabled());
  confirm(app);assertFalse(point(app).getAllPredecessors().contains(q));assertNull(tool(app).pendingSelection());tool(app).reset();
 });}

 @Test void radiusEditsKeepLInvalidateOfferAndDoNotRequery()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");start(app);
  pointer(app,-1,0);String before=app.getXML();radius(app).setText("0.2");assertL(app,s);assertNull(tool(app).pendingSelection());
  radius(app).postActionEvent();assertL(app,s);pointer(app,q.inhomX,q.inhomY);var first=tool(app).pendingSelection();assertNotNull(first);
  JButton stale=button(tool(app).contextMenu(),"Confirm");radius(app).setText("0.1");assertNull(tool(app).pendingSelection());
  assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED,first.getValidity());assertL(app,s);
  assertTrue(find(panel(app),JTextField.class,f->f.getText().contains("previous choice cancelled"))!=null);press(stale);assertEquals(before,app.getXML());
  pointer(app,q.inhomX,q.inhomY);assertNotNull(tool(app).pendingSelection());assertNotSame(first,tool(app).pendingSelection());
  confirm(app);assertEquals(.3,point(app).inhomX,1e-8);tool(app).reset();
 });}

 @Test void invalidPolicyBlocksQueryWithoutLResetOrMutation()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");start(app);
  pointer(app,-1,0);String before=app.getXML();radius(app).setText("NaN");pointer(app,q.inhomX,q.inhomY);
  assertNull(tool(app).pendingSelection());assertL(app,s);assertEquals(before,app.getXML());radius(app).setText("0.1");
  pointer(app,q.inhomX,q.inhomY);assertNotNull(tool(app).pendingSelection());tool(app).reset();
 });}

 @Test void policyToggleIsInlineAndDoesNotResetSelection()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);start(app);pointer(app,-1,0);
  var options=find(panel(app),JToggleButton.class,b->b.getText().equals("Policy"));press(options);assertTrue(options.isSelected());assertL(app,s);
  assertNotNull(radius(app));assertEquals(1,countRadius(panel(app)));press(options);assertFalse(options.isSelected());assertL(app,s);tool(app).reset();
 });}
 static int countRadius(Container root){int n=0;for(Component c:root.getComponents()){
  if(c instanceof JTextField f&&"m08-policy-0".equals(f.getName()))n++;
  if(c instanceof Container child)n+=countRadius(child);
 }return n;}

 @Test void overlappingPointHitsRequireExplicitObjectThenAddress()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");q(app,"R=(0.3,0.02)");start(app);
  pointer(app,-1,0);String before=app.getXML();pointer(app,q.inhomX,q.inhomY);assertNull(tool(app).pendingSelection());assertNotNull(tool(app).contextMenu());
  var list=find(tool(app).contextMenu(),JList.class,l->true);assertEquals(2,list.getModel().getSize());assertEquals(-1,list.getSelectedIndex());
  assertFalse(button(tool(app).contextMenu(),"Confirm").isEnabled());list.setSelectedIndex(0);confirm(app);
  assertNotNull(tool(app).pendingSelection());assertEquals(before,app.getXML());confirm(app);assertNotNull(point(app));tool(app).reset();
 });}

 @Test void historicalMultipleAddressesHaveNoImplicitChoiceThroughEvents()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=(GeoLocusV2)G9U1SemanticPointInteractionTest.eval(app,"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
  var q=q(app,"Q=(0,1)");start(app);pointer(app,-1,0);assertL(app,s);pointer(app,q.inhomX,q.inhomY);
  var selection=tool(app).pendingSelection();assertNotNull(selection);assertEquals(GeoCeDGReferencePlacement.Route.R1_R6,selection.getRoute());
  assertTrue(selection.getChoices().size()>1);String before=app.getXML();assertFalse(button(tool(app).contextMenu(),"Confirm").isEnabled());
  find(tool(app).contextMenu(),JList.class,l->true).setSelectedIndex(1);confirm(app);assertNotEquals(before,app.getXML());tool(app).reset();
 });}

 @Test void emptyQClickCancellationModeAndEscapeNeverCreatePoints()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");start(app);String before=app.getXML();
  pointer(app,-1,0);pointer(app,3,2);assertL(app,s);assertNull(tool(app).pendingSelection());assertEquals(before,app.getXML());
  pointer(app,q.inhomX,q.inhomY);assertNotNull(tool(app).pendingSelection());press(button(tool(app).contextMenu(),"Cancel"));
  assertNull(tool(app).pendingSelection());assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());
  pointer(app,-1,0);var p=panel(app);var escape=p.getActionForKeyStroke(KeyStroke.getKeyStroke("ESCAPE"));assertNotNull(escape);
  escape.actionPerformed(new ActionEvent(p,ActionEvent.ACTION_PERFORMED,"Escape"));assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());
  pointer(app,-1,0);pointer(app,q.inhomX,q.inhomY);var pending=tool(app).pendingSelection();app.setMode(EuclidianConstants.MODE_POINT);
  assertNull(tool(app).pendingSelection());assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED,pending.getValidity());assertNull(panel(app));assertEquals(before,app.getXML());
 });}

 @Test void inlineKeysDoNotReachGlobalGeometryShortcuts()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");start(app);pointer(app,-1,0);pointer(app,q.inhomX,q.inhomY);
  var list=find(tool(app).contextMenu(),JList.class,l->true);String before=app.getXML();var selection=tool(app).pendingSelection();
  for(int key:new int[]{KeyEvent.VK_ENTER,KeyEvent.VK_SPACE,KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_ESCAPE}){
   var event=new KeyEvent(list,KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,key,KeyEvent.CHAR_UNDEFINED);
   assertFalse(app.newGlobalKeyDispatcher().dispatchKeyEvent(event));assertFalse(event.isConsumed());
  }
  assertSame(selection,tool(app).pendingSelection());assertEquals(before,app.getXML());tool(app).reset();
 });}

 @Test void staleAndBudgetRejectionThroughControllerDoNotPublish()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();G9U1SemanticPointInteractionTest.spline(app);var q=q(app,"Q=(0.3,0.02)");start(app);pointer(app,-1,0);pointer(app,q.inhomX,q.inhomY);
  q.setCoords(.4,.02,1);q.updateCascade();String before=app.getXML();confirm(app);assertEquals("STALE_SELECTION",tool(app).lastFailure());assertEquals(before,app.getXML());
  tool(app).setPolicy(new LocusPointInteractionPolicy2D(.1,1e-10,1e-12,new LocusPointInteractionWorkBudget2D(1,2,1,1)));
  pointer(app,-1,0);pointer(app,q.inhomX,q.inhomY);assertNull(tool(app).pendingSelection());assertTrue(tool(app).lastFailure().contains("BUDGET_EXHAUSTED"));assertEquals(before,app.getXML());tool(app).reset();
 });}

 @Test void localizedPanelFitsInputLayoutAndSurvivesInputRebuild()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();
  for(var locale:List.of(java.util.Locale.ENGLISH,java.util.Locale.forLanguageTag("es"))){
   app.setLanguage(locale);start(app);var input=((GuiManagerGeoCeDG)app.getGuiManager()).getAlgebraInput();var p=panel(app);assertNotNull(p);assertEquals(1,countRadius(p));
   assertTrue(p.getPreferredSize().width<900);assertTrue(p.getPreferredSize().height<90);assertNull(find(p,JLabel.class,l->l.getText().contains("no query yet")));
   var instruction=find(p,JTextField.class,f->!f.isEditable());assertFalse(instruction.getText().contains("r="));
   assertEquals(locale.getLanguage().equals("es")?"Seleccione L":"Select L",instruction.getText());
   for(int width:new int[]{640,1280}){input.setSize(width,200);input.doLayout();assertEquals(input.getWidth(),p.getWidth());}
   ((org.geogebra.desktop.gui.inputbar.AlgebraInputD)input).initGUI();assertSame(input,((GuiManagerGeoCeDG)app.getGuiManager()).getAlgebraInput());assertSame(p,panel(app));
   app.setMode(EuclidianConstants.MODE_MOVE);assertNull(panel(app));
  }
 });}

 @Test void dependentQAndLegitimateLAncestorWorkThroughController()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();G9U1SemanticPointInteractionTest.spline(app);q(app,"Q=(0.3,0.02)");var r=q(app,"R=Q+(0.2,0)");
  start(app);pointer(app,-1,0);pointer(app,r.inhomX,r.inhomY);confirm(app);var p=point(app);
  assertFalse(p.getAllPredecessors().contains(r));assertFalse(p.getAllPredecessors().contains(app.getKernel().lookupLabel("Q")));tool(app).reset();
  var other=G9U1SemanticPointInteractionTest.app();var s=G9U1SemanticPointInteractionTest.spline(other);s.setEuclidianVisible(false);s.updateRepaint();
  var o=q(other,"O=(0,0)");var t=(GeoLocusV2)G9U1SemanticPointInteractionTest.eval(other,"T=Dilate(S,2,O)");
  t.setEuclidianVisible(true);t.updateRepaint();
  start(other);pointer(other,-1,0);assertL(other,t);pointer(other,0,0);confirm(other);var result=point(other);
  assertFalse(java.util.Arrays.asList(result.getParentAlgorithm().getInput()).contains(o));assertTrue(result.getAllPredecessors().contains(o));
  var z=q(other,"Z="+result.getLabelSimple()+"+(1,0)");o.setCoords(1,0,1);o.updateCascade();
  assertEquals(-1,result.inhomX,1e-8);assertEquals(0,z.inhomX,1e-8);tool(other).reset();
 });}

 @Test void creationThroughEventsHasExactlyOneUndoAndStableRedo()throws Exception {
  AtomicReference<AppGeoCeDG> host=new AtomicReference<>();AtomicReference<String> label=new AtomicReference<>();AtomicReference<Object> id=new AtomicReference<>();
  edt(()->{var app=G9U1SemanticPointInteractionTest.app();host.set(app);G9U1SemanticPointInteractionTest.spline(app);q(app,"Q=(0.3,0.02)");app.getKernel().initUndoInfo();});
  var app=host.get();var undo=app.getKernel().getConstruction().getUndoManager();await(()->!undo.undoPossible());
  edt(()->{start(app);pointer(app,-1,0);pointer(app,.3,.02);confirm(app);var p=point(app);label.set(p.getLabelSimple());id.set(app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(p));});
  await(undo::undoPossible);edt(()->app.getKernel().undo());await(undo::redoPossible);
  edt(()->{assertNull(app.getKernel().lookupLabel(label.get()));assertFalse(undo.undoPossible());app.getKernel().redo();});
  await(()->app.getKernel().lookupLabel(label.get())!=null);
  edt(()->{assertEquals(id.get(),app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(app.getKernel().lookupLabel(label.get())));tool(app).reset();});
 }
 static void await(BooleanSupplier condition)throws Exception {long deadline=System.nanoTime()+5_000_000_000L;while(!condition.getAsBoolean()&&System.nanoTime()<deadline)Thread.sleep(10);assertTrue(condition.getAsBoolean());}
}

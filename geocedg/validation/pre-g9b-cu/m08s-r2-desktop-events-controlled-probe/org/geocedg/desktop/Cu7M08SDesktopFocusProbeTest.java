/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import javax.swing.*;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Before-only characterization. No Window.show, Robot, native input or focus request. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SDesktopFocusProbeTest {
 @BeforeAll static void initialize(){G9U1SemanticPointInteractionTest.initializeDesktop();}
 static Object field(Object value,String name)throws Exception {Field f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value);}
 static void edt(Throwing action)throws Exception {
  java.util.concurrent.atomic.AtomicReference<Throwable> failed=new java.util.concurrent.atomic.AtomicReference<>();
  SwingUtilities.invokeAndWait(()->{try{action.run();}catch(Throwable e){failed.set(e);}});
  if(failed.get()!=null)throw new AssertionError(failed.get());
 }
 interface Throwing{void run()throws Exception;}
 static void pointer(AppGeoCeDG app,double x,double y){
  JPanel canvas=app.getEuclidianView1().getJPanel();
  for(int id:new int[]{MouseEvent.MOUSE_MOVED,MouseEvent.MOUSE_PRESSED,MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED}){
   canvas.dispatchEvent(new MouseEvent(canvas,id,System.currentTimeMillis(),id==MouseEvent.MOUSE_PRESSED?InputEvent.BUTTON1_DOWN_MASK:0,
    app.getEuclidianView1().toScreenCoordX(x),app.getEuclidianView1().toScreenCoordY(y),id==MouseEvent.MOUSE_MOVED?0:1,false,
    id==MouseEvent.MOUSE_MOVED?MouseEvent.NOBUTTON:MouseEvent.BUTTON1));
  }
 }
 @Test void retainedR1QueryBecomesInaccessibleWhenRealFrameHandlesFocusLoss()throws Exception {edt(()->focusLoss(false));}
 @Test void retainedH1QueryBecomesInaccessibleWhenRealFrameHandlesFocusLoss()throws Exception {edt(()->focusLoss(true));}
 static void focusLoss(boolean nativeModel)throws Exception {
  AppGeoCeDG app=G9U1SemanticPointInteractionTest.app();
  GeoLocusV2 source;GeoPoint q;
  if(nativeModel){
   assertTrue(app.loadFile(new java.io.File("C:/Temp/TestM08-S.cedg"),false));
   source=(GeoLocusV2)app.getKernel().lookupLabel("h");
   q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
  }else{source=G9U1SemanticPointInteractionTest.spline(app);q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(2.02,0.01)");}
  q.setEuclidianVisible(true);q.updateRepaint();
  var frame=new GeoCeDGFrame();frame.setApplication(app);frame.setBounds(100,100,700,600);
  var main=(JPanel)app.getMainComponent();
  if(main.getComponentCount()==0){main.setLayout(new BorderLayout());main.add(app.buildApplicationPanel());}
  frame.setContentPane(main);
  assertSame(frame,SwingUtilities.getWindowAncestor(app.getEuclidianView1().getJPanel()));
  var controller=G9U1SemanticPointInteractionTest.controller(app);var tool=controller.referencePointTool();
  Popup presentation=null;Window child=null;
  try{
   app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
   pointer(app,nativeModel?.8:-1,nativeModel?2.4:0);assertSame(source,field(tool,"source"));
   String before=app.getXML();pointer(app,q.getInhomX(),q.getInhomY());
   var selection=tool.pendingSelection();assertNotNull(selection);
   assertEquals(nativeModel?GeoCeDGReferencePlacement.Route.R2_H1:GeoCeDGReferencePlacement.Route.R1_R6,selection.getRoute());
   var menu=tool.contextMenu();assertInstanceOf(JPopupMenu.class,menu);
   // Real JDK PopupFactory, placed beyond the owner edge, just as a pointer popup can be.
   // getPopup constructs but does not show the native window; there is no native peer.
   presentation=PopupFactory.getSharedInstance().getPopup(app.getEuclidianView1().getJPanel(),menu,frame.getX()+frame.getWidth()-8,frame.getY()+40);
   child=SwingUtilities.getWindowAncestor(menu);
   assertNotNull(child);assertEquals("javax.swing.Popup$HeavyWeightWindow",child.getClass().getName());
   assertSame(frame,child.getOwner());assertTrue(child.getFocusableWindowState());
   assertEquals("###focusableSwingPopup###",child.getName());
   assertFalse(frame.isShowing());assertFalse(child.isDisplayable());
   // Test-only Java visibility flags model an already-shown popup. NEVER call show/setVisible(true).
   Field visible=Component.class.getDeclaredField("visible");visible.setAccessible(true);
   visible.setBoolean(menu,true);visible.setBoolean(child,true);
   Field handle=JPopupMenu.class.getDeclaredField("popup");handle.setAccessible(true);handle.set(menu,presentation);
   assertTrue(child.isVisible());assertTrue(menu.isVisible());
   frame.windowLostFocus(new WindowEvent(frame,WindowEvent.WINDOW_LOST_FOCUS,child));
   assertFalse(child.isVisible());assertTrue(menu.isVisible());assertSame(selection,tool.pendingSelection());
   pointer(app,q.getInhomX(),q.getInhomY());assertSame(selection,tool.pendingSelection());
   assertEquals(before,app.getXML());
   System.out.println("FOCUS_REPLAY route="+selection.getRoute()+" L_RETAINED=true Q_HIT=true QUERY_COMPLETE=true HEAVY_FOCUSABLE=true WINDOW_HIDDEN=true MENU_FLAG_STILL_TRUE=true QUERY_STILL_PENDING=true NO_PUBLICATION=true NO_NATIVE_SHOW_OR_INPUT=true");
  }finally{
   if(child!=null)child.dispose();if(presentation!=null)presentation.hide();
   tool.reset();app.setMode(EuclidianConstants.MODE_MOVE);frame.setContentPane(new JPanel());frame.dispose();
  }
 }
 @Test void sameModeSynchronizationDiscardsLWithoutChangingTheDocument()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var source=G9U1SemanticPointInteractionTest.spline(app);
  var controller=G9U1SemanticPointInteractionTest.controller(app);app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
  pointer(app,-1,0);assertSame(source,field(controller.referencePointTool(),"source"));String before=app.getXML();
  controller.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT,ModeSetter.DOCK_PANEL);
  assertNull(field(controller.referencePointTool(),"source"));assertEquals(before,app.getXML());
  System.out.println("SAME_MODE_REPLAY L_WAS_SELECTED=true L_DISCARDED=true DOCUMENT_UNCHANGED=true");controller.referencePointTool().reset();
 });}
 @Test void policyApplicationDiscardsLAndTheNextQCannotQuery()throws Exception {edt(()->{
  var app=G9U1SemanticPointInteractionTest.app();var source=G9U1SemanticPointInteractionTest.spline(app);
  var q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(2.02,0.01)");q.setEuclidianVisible(true);q.updateRepaint();
  var controller=G9U1SemanticPointInteractionTest.controller(app);app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
  pointer(app,-1,0);assertSame(source,field(controller.referencePointTool(),"source"));String before=app.getXML();
  controller.referencePointTool().setPolicy(LocusPointInteractionPolicy2D.initial(.2));
  assertNull(field(controller.referencePointTool(),"source"));pointer(app,q.getInhomX(),q.getInhomY());
  assertNull(controller.referencePointTool().pendingSelection());assertEquals(before,app.getXML());
  System.out.println("POLICY_REPLAY L_WAS_SELECTED=true L_DISCARDED=true Q_CANNOT_QUERY=true DOCUMENT_UNCHANGED=true");controller.referencePointTool().reset();
 });}
}

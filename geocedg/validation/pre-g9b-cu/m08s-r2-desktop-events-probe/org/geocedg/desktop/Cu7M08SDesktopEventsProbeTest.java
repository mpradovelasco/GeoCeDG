/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Opt-in headful real Swing listeners, synthetic AWT dispatch; not physical input. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SDesktopEventsProbeTest {
 @BeforeAll static void initialize(){G9U1SemanticPointInteractionTest.initializeDesktop();}
 static final class Host implements AutoCloseable {
  AppGeoCeDG app; JFrame frame; JPanel main,canvas; GeoCeDGEuclidianController controller;
  GeoCeDGReferencePointTool tool; GeoLocusV2 source; GeoPoint q; ChangeListener menus;
  int menuOpened,menuClosed; String before; long sequence;
  Host(boolean nativeModel)throws Exception {
   edt(()->{
    assertFalse(GraphicsEnvironment.isHeadless(),"Probe requires a real display");
    app=G9U1SemanticPointInteractionTest.app();
    if(nativeModel){
     assertTrue(app.loadFile(new java.io.File("C:/Temp/TestM08-S.cedg"),false));
     source=(GeoLocusV2)app.getKernel().lookupLabel("h");
     q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
    }else{
     source=G9U1SemanticPointInteractionTest.spline(app);
     q=(GeoPoint)G9U1SemanticPointInteractionTest.eval(app,"Q=(2.02,0.01)");
    }
    q.setEuclidianVisible(true); q.updateRepaint();
    main=(JPanel)app.getMainComponent();
    if(main.getComponentCount()==0){main.setLayout(new BorderLayout());main.add(app.buildApplicationPanel());}
    frame=new JFrame("GeoCeDG M08-S owned event probe");frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.setContentPane(main);frame.setSize(1000,720);frame.setLocationByPlatform(true);frame.setVisible(true);
    app.getEuclidianView1().setCoordSystem(nativeModel?220:270,nativeModel?420:260,100,100);
    app.getEuclidianView1().updateSize();
    controller=G9U1SemanticPointInteractionTest.controller(app);tool=controller.referencePointTool();
    canvas=app.getEuclidianView1().getJPanel();
    menus=e->{
     int count=MenuSelectionManager.defaultManager().getSelectedPath().length;
     if(count>0)menuOpened++;else menuClosed++;
     trace("MENU_PATH_"+count);
     if(count==0){String stack=StackWalker.getInstance().walk(s->s.limit(16).map(Object::toString).collect(java.util.stream.Collectors.joining("\n")));System.out.println(stack);}
    };
    MenuSelectionManager.defaultManager().addChangeListener(menus);
    app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
   });
   settle();edt(()->{before=app.getXML();trace("ACTIVATED");});
  }
  void trace(String event){
   try{
    var selected=field(tool,"source"); var pending=tool.pendingSelection();
    System.out.println("EVENT_TRACE "+(++sequence)+" "+event+" mode="+controller.getMode()
     +" source="+(selected==null?"null":((GeoLocusV2)selected).getLabelSimple())
     +" generation="+field(tool,"generation")+" pending="+(pending==null?"null":pending.getRoute())
     +" popup="+(tool.contextMenu()!=null&&tool.contextMenu().isVisible())
     +" focus="+KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()
     +" hits="+app.getEuclidianView1().getHits());
   }catch(Exception e){throw new IllegalStateException(e);}
  }
  void pointer(double x,double y)throws Exception {
   for(int id:new int[]{MouseEvent.MOUSE_MOVED,MouseEvent.MOUSE_PRESSED,MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED}){
    edt(()->{
     int sx=app.getEuclidianView1().toScreenCoordX(x),sy=app.getEuclidianView1().toScreenCoordY(y);
     int button=id==MouseEvent.MOUSE_MOVED?MouseEvent.NOBUTTON:MouseEvent.BUTTON1;
     var e=new MouseEvent(canvas,id,System.currentTimeMillis(),id==MouseEvent.MOUSE_PRESSED?InputEvent.BUTTON1_DOWN_MASK:0,
      sx,sy,id==MouseEvent.MOUSE_MOVED?0:1,false,button);
     canvas.dispatchEvent(e);trace("DISPATCH_"+id);
    });settle();
   }
  }
  @Override public void close()throws Exception {
   edt(()->{
    MenuSelectionManager.defaultManager().removeChangeListener(menus);
    tool.reset();app.setMode(EuclidianConstants.MODE_MOVE);
    frame.setContentPane(new JPanel());frame.dispose();
   });settle();
  }
 }
 static Object field(Object value,String name)throws Exception {Field f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value);}
 static void edt(Runnable action)throws Exception {if(SwingUtilities.isEventDispatchThread())action.run();else SwingUtilities.invokeAndWait(action);}
 static void settle()throws Exception {edt(()->{});Thread.sleep(70);edt(()->{});}
 @Test void characterizeActualSwingListenerPathForR1Endpoint()throws Exception {
  try(Host h=new Host(false)){
   h.pointer(-1,0);edt(()->{assertSame(h.source,uncheckedSource(h.tool));h.trace("L_SELECTED");});
   h.pointer(h.q.getInhomX(),h.q.getInhomY());
   edt(()->{
    h.trace("AFTER_Q");assertEquals(h.before,h.app.getXML());
    System.out.println("PROBE_SUMMARY menusOpened="+h.menuOpened+" menusClosed="+h.menuClosed
     +" pending="+(h.tool.pendingSelection()!=null)+" popup="+(h.tool.contextMenu()!=null&&h.tool.contextMenu().isVisible()));
   });
  }
 }
 @Test void characterizeActualSwingListenerPathForNativeH1()throws Exception {
  try(Host h=new Host(true)){
   // Choose the finite right-side branch away from existing points.
   h.pointer(.8,2.4);edt(()->{assertSame(h.source,uncheckedSource(h.tool));h.trace("L_SELECTED");});
   h.pointer(h.q.getInhomX(),h.q.getInhomY());
   edt(()->{h.trace("AFTER_Q");assertEquals(h.before,h.app.getXML());System.out.println("PROBE_NATIVE pending="+(h.tool.pendingSelection()!=null));});
  }
 }
 static Object uncheckedSource(GeoCeDGReferencePointTool t){try{return field(t,"source");}catch(Exception e){throw new IllegalStateException(e);}}
 @Test void characterizePhysicalDispatchOnRealProductFrame()throws Exception {
  var output=java.nio.file.Path.of("artifacts/pre-g9b-cu7-i1-r2-desktop-events/before/physical-probe");
  java.nio.file.Files.createDirectories(output);
  var ready=output.resolve("ready.json");var stop=output.resolve("finish.signal");var log=output.resolve("events.log");
  assertFalse(java.nio.file.Files.exists(stop),"Each probe needs a fresh output root");
  AtomicReference<org.geogebra.desktop.gui.app.GeoGebraFrame> owned=new AtomicReference<>();
  AtomicReference<AppGeoCeDG> application=new AtomicReference<>();
  AtomicReference<AWTEventListener> awt=new AtomicReference<>();
  AtomicReference<ChangeListener> menus=new AtomicReference<>();
  AtomicReference<java.beans.PropertyChangeListener> focus=new AtomicReference<>();
  edt(()->{
   var frame=GeoCeDGFrame.createNewWindow(new org.geogebra.desktop.CommandLineArguments(
    new String[]{"--silent","--enableLocusV2=true"}));owned.set(frame);
   var app=(AppGeoCeDG)frame.getApplication();application.set(app);app.setErrorDialogsActive(false);
   G9U1SemanticPointInteractionTest.spline(app);G9U1SemanticPointInteractionTest.eval(app,"Q=(2.02,0.01)");
   frame.setSize(900,650);app.getEuclidianView1().setCoordSystem(330,250,100,100);app.getEuclidianView1().updateSize();
   var controller=G9U1SemanticPointInteractionTest.controller(app);var tool=controller.referencePointTool();
   java.util.function.Consumer<String> trace=event->{
    try{
     var selected=uncheckedSource(tool);var pending=tool.pendingSelection();
     String state="PHYSICAL_TRACE "+System.nanoTime()+" "+event+" appMode="+app.getMode()+" viewMode="+app.getEuclidianView1().getMode()+" controllerMode="+controller.getMode()
      +" source="+(selected==null?"null":((GeoLocusV2)selected).getLabelSimple())+" generation="+field(tool,"generation")
      +" pending="+(pending==null?"null":pending.getRoute())+" popup="+(tool.contextMenu()!=null&&tool.contextMenu().isVisible())
      +" focus="+(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()==null?"null":KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner().getClass().getName())
      +" hits="+app.getEuclidianView1().getHits()+"\n";
     java.nio.file.Files.writeString(log,state,java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
    }catch(Exception failure){throw new IllegalStateException(failure);}
   };
   AWTEventListener observer=event->{
    if(event instanceof MouseEvent mouse){EventQueue.invokeLater(()->trace.accept("MOUSE_"+mouse.getID()+"_"+mouse.getComponent().getClass().getSimpleName()));}
    if(event instanceof WindowEvent window){EventQueue.invokeLater(()->trace.accept("WINDOW_"+window.getID()+"_"+window.getWindow().getClass().getSimpleName()));}
   };awt.set(observer);Toolkit.getDefaultToolkit().addAWTEventListener(observer,AWTEvent.MOUSE_EVENT_MASK|AWTEvent.WINDOW_EVENT_MASK|AWTEvent.WINDOW_FOCUS_EVENT_MASK);
   ChangeListener menu=e->{trace.accept("MENU_PATH_"+MenuSelectionManager.defaultManager().getSelectedPath().length);
    try{String stack=StackWalker.getInstance().walk(stream->stream.limit(22).map(Object::toString).collect(java.util.stream.Collectors.joining("\n")));
     java.nio.file.Files.writeString(log,stack+"\n",java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
    }catch(Exception failure){throw new IllegalStateException(failure);}
   };menus.set(menu);MenuSelectionManager.defaultManager().addChangeListener(menu);
   java.beans.PropertyChangeListener f=e->trace.accept("FOCUS_"+e.getPropertyName());focus.set(f);
   KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner",f);
   KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow",f);
   ((GuiManagerGeoCeDG)app.getGuiManager()).getActionRegistry().invoke("semantic.locus-v2.point-from-reference",null);
   trace.accept("READY");
   try{java.nio.file.Files.writeString(ready,"{\"ready\":true,\"title\":\""+frame.getTitle()+"\"}");}catch(Exception e){throw new IllegalStateException(e);}
  });
  try{while(!java.nio.file.Files.exists(stop)){Thread.sleep(100);}}
  finally{edt(()->{
   var app=application.get();var frame=owned.get();
   Toolkit.getDefaultToolkit().removeAWTEventListener(awt.get());
   MenuSelectionManager.defaultManager().removeChangeListener(menus.get());
   KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner",focus.get());
   KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("activeWindow",focus.get());
   G9U1SemanticPointInteractionTest.controller(app).referencePointTool().reset();app.setMode(EuclidianConstants.MODE_MOVE);
   for(var manager:G9U1TestApp.dockManagers()){if(manager.getLayout().getApplication()==app)Toolkit.getDefaultToolkit().removeAWTEventListener(manager);}
   KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(app);frame.dispose();
  });}
  System.out.println("PHYSICAL_PROBE_TRACE="+log.toAbsolutePath());
 }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.awt.KeyboardFocusManager;

/**
 *
 * @author cnsaeman
 */
public class CelsiusMain {

    public static MainFrame MF;

    private static SplashScreen StartUp;

    public static void main(String args[]) throws Exception {
        MF=new MainFrame();
        System.out.println("Celsius "+MF.RSC.VersionNumber);
        MF.RSC.guiScaleFactor=2;
        if ((args.length>0) && (args[0].equals("HiDPI"))) {
            MF.RSC.guiScaleFactor=2;
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run()  {
                StartUp = new SplashScreen(MF.RSC.VersionNumber, true,MF.RSC);
                MF.StartUp=StartUp;
                StartUp.setStatus("Initializing Resources...");
            }
        });
        MF.RSC.initResources();
        MF.RSC.setLookAndFeel();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run()  {
                StartUp.setStatus("Loading Plugins...");
            }
        });
        MF.RSC.loadPlugins();
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run()  {
                StartUp.setStatus("Laying out GUI...");
                MF.gui1();
                StartUp.setStatus("Setting Shortcuts...");
                MF.loadShortCuts();
                MF.setShortCuts();
                StartUp.setStatus("Loading Libraries...");
            }
        });
        MF.RSC.ReadInitFile();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run()  {
                MF.gui2();
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.addKeyEventDispatcher(new HotKeyManager(MF));
            }
        });
    }


}

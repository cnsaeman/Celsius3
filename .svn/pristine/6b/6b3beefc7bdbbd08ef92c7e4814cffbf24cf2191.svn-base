/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;

/**
 *
 * @author cnsaeman
 */
public class HotKeyManager implements KeyEventDispatcher {

    public MainFrame MF;

    public int lastkeycode;
    
    public HotKeyManager(MainFrame mf) {
        MF=mf;
        lastkeycode=0;
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode()==lastkeycode) return(false);
        Class cls=e.getSource().getClass();
        if (cls.getName().startsWith("celsius.Dialogs.")) {
            System.out.println("AA");
            return(false);
        }
        if (e.getSource().getClass().getCanonicalName()==null) return(false);
        if (e.getSource().getClass().getCanonicalName().equals("celsius.SplashScreen")) return(false);
        if (e.getSource().getClass().getCanonicalName().equals("javax.swing.JDialog")) return(false);
        if (e.getSource().getClass().getCanonicalName().equals("celsius.MainFrame")) return(false);
        if (MF.getRootPane()!=((JComponent)e.getSource()).getRootPane()) return(false);
        lastkeycode=e.getKeyCode();
        // ctrl+f : look for
        if (e.isControlDown() && (e.getKeyCode()==70)) {
            MF.jTFMainSearch.requestFocus();
            MF.jTFMainSearch.selectAll();
            e.consume();
            return(true);
        }
        // ctrl+t : switch thumbnailview
        if (e.isControlDown() && (e.getKeyCode()==84)) {
            if (MF.RSC.getCurrentItemTable()!=null)
                MF.RSC.getCurrentItemTable().switchView();
            e.consume();
            return(true);
        }
        // ctrl+p : people search
        if (e.isControlDown() && (e.getKeyCode()==80)) {
            MF.jTPSearches.setSelectedIndex(1);
            MF.jTFSearchAuthors.requestFocus();
            MF.jTFSearchAuthors.selectAll();
            e.consume();
            return(true);
        }
        return(false);
    }

}

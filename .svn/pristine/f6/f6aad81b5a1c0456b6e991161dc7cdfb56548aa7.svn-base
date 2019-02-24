//
// Celsius Library System v2
// (w) by C. Saemann
//
// SafeMessage.java
//
// This class displays messages avoid deadlocks, not completely working as intended
//
// typesafe
// 
// checked 16.09.2007
//


package celsius;

import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author cnsaeman
 */
public class SafeMessage {
    
    public int returnCode;
    public String msg;
    public String head;
    public int type;        // 0: später anzeigen, 1: sofort anzeigen
    private Object[] optionsVar;

    public final static Object[] optionsOC = { "OK", "Cancel" };
    
    /** Creates a new instance of SafeMessage */
    public SafeMessage(String s1, String s2,int t) {
        msg=s1; head=s2; type=t; returnCode=255;
    }
    
    public SafeMessage(String s1, String s2, String o1, String o2, String o3) {
        msg=s1; head=s2; type=2;
        optionsVar=new Object[3];
        optionsVar[0]=o1; optionsVar[1]=o2; optionsVar[2]=o3;
    }

    public SafeMessage(String s1, String s2, String o1, String o2, String o3, String o4) {
        msg=s1; head=s2; type=2;
        optionsVar=new Object[4];
        optionsVar[0]=o1; optionsVar[1]=o2; optionsVar[2]=o3; optionsVar[3]=o4;
    }
    
    public void showMsg() {
        if (type==0) SwingUtilities.invokeLater(new Runnable() { public void run() { JOptionPane.showMessageDialog(null,msg,head, JOptionPane.WARNING_MESSAGE); }});
        if (type==1) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() { public void run() { returnCode=JOptionPane.showOptionDialog(null,msg, head, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, optionsOC, optionsOC[0]); }});
            } catch (HeadlessException ex) {
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
        if (type==2) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() { public void run() { returnCode=JOptionPane.showOptionDialog(null,msg, head, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, optionsVar, optionsVar[0]); }});
            } catch (HeadlessException ex) {
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }
    
}

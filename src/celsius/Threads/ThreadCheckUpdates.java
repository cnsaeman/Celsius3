/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius.Threads;

import celsius.Resources;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;

/**
 *
 * @author cnsaeman
 */
public class ThreadCheckUpdates extends Thread {

    Resources RSC;

    public ThreadCheckUpdates(Resources rsc) {
        super();
        RSC=rsc;
        this.setPriority(MIN_PRIORITY);
    }

    @Override
    public void run() {
        String lv=TextFile.ReadOutURL("http://celsius.christiansaemann.de/lv");
        String ver1=Parser.CutTill(lv, "\n");
        String ver2=Parser.CutFrom(lv,"\n");
        if ((!ver1.equals(RSC.VersionNumber)) && (ver2.compareTo(RSC.VersionNumber)>0)) {
            int i=toolbox.QuestionAB(RSC.getMF(),"There is a new version of Celsius available for download:\nCurrent version: "+RSC.VersionNumber+"\nVersion available: "+ver1, "New version available:","Ignore","Go to webpage");
            if (i==1) {
                RSC.Configuration.viewHTML("http://celsius.christiansaemann.de");
            }
        }
    }

}

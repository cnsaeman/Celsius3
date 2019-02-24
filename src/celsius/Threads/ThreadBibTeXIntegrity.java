//
// Celsius Library System v2
// (w) by C. Saemann
//
// BibTeXIntegrityThread.java
//
// This class checks the integrity of BibTeX records
//
// typesafe
// 
// checked 11/2009
//

package celsius.Threads;

import celsius.*;
import javax.swing.ProgressMonitor;


public class ThreadBibTeXIntegrity extends Thread {
    
    private final Resources RSC;
    private final ProgressMonitor PM;                    // Progress label
    private final Library Lib;
    private final ItemTable DT;
    private final ThreadRegistrar reg;
    
    // Constructor
    public ThreadBibTeXIntegrity(Resources rsc, ProgressMonitor p,ItemTable dt) {
        RSC=rsc; Lib=RSC.getCurrentSelectedLib(); PM=p; DT=dt;
        DT.setLibrary(Lib);
        reg=DT.newRegistrar();
    }
    
    // Main Routine
    @Override
    public void run() {
        reg.start();
        for (Item doc : Lib) {
            if (PM.isCanceled()) break;
            PM.setProgress(doc.pos);
            String bib=doc.get("bibtex");
            if (bib!=null) {
                if (!BibTeXRecord.BibTeXconsistency(bib))
                    reg.add(doc);
            }
        }
        if (!PM.isCanceled()) {
            reg.end = true;
            try {
                reg.join();
            } catch (InterruptedException ex) {
            }
            RSC.getMF().jIP.updateHTMLview();
        } else {
            reg.interrupt();
        }
        PM.close();
        RSC.getMF().setThreadMsg("Ready.");
   }
    
}
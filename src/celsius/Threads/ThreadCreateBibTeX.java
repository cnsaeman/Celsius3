//
// Celsius Library System
// (w) by C. Saemann
//
// ThreadCreateBibTeX.java
//
// This class contains the thread for creating a bibtex file
//
// typesafe
//
// checked 11/2009
//

package celsius.Threads;

import celsius.BibTeXRecord;
import celsius.Item;
import celsius.Library;
import celsius.tools.MsgLogger;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.io.IOException;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

public class ThreadCreateBibTeX extends Thread {
    
    private final Library Lib;           // Library
    private final ProgressMonitor PM;    // Progress label
    private String target;               // target file
    
    private final MsgLogger Msg1;
    private final String TI;
    
    // Constructor
    public ThreadCreateBibTeX(Library lib, MsgLogger msg1,ProgressMonitor p, String t) {
        TI="CBT>";
        Lib=lib; PM=p; Msg1=msg1;
        target=t;
    }
    
    // Main Routine
    @Override
    public void run() {
        setStatusBegin();
        try {
            
            Msg1.repS();
            Msg1.repS(TI+"CreateBibTeXThread::"+toolbox.ActDatum());
            Msg1.repS(TI+"This routine creates a BibTeX-File");
            
            TextFile f1=new TextFile(target,false);
            for (Item doc : Lib) {
                try // Reading addinfo of all entries
                {
                    if (doc.get("bibtex")!=null) {
                        f1.putString("");
                        // consistency check
                        BibTeXRecord btr=new BibTeXRecord(doc.get("bibtex"));
                        if (btr.parseError==0)
                            f1.putString(btr.toString());
                        else
                            toolbox.Warning(null,"The following BibTeX-entry belonging to the item \n"+doc.get("title")+"\n by "+doc.get("authors")+"\n is not consistent and left out:\n"+doc.get("bibtex")+"\nError: "+BibTeXRecord.status[btr.parseError],"Warning!");
                    }
                } catch (Exception ecx) { Msg1.repS(TI+"Error writing file: "+ecx.toString()); }
                if (doc.pos%20==0) PM.setProgress(doc.pos);
            }
            f1.close();
            Msg1.repS(TI+"Creating BibTex file done.");
        } catch (IOException e) { Msg1.repS(TI+"Error during creating BibTeX-file: "+e.toString());  }
        setStatusDone();
    }

    private void setStatusBegin() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Lib.RSC.getMF().setThreadMsg("Creating BibTeX file...");
                Lib.RSC.getMF().jPBSearch.setIndeterminate(true);
            }
        });
    }

    private void setStatusDone() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Lib.RSC.getMF().setThreadMsg("Ready.");
                Lib.RSC.getMF().jPBSearch.setIndeterminate(false);
                PM.close();
            }
        });
    }
    
}
//
// Celsius Library System
// (w) by C. Saemann
//
// ThreadShowCited.java
//
// This class contains a thread showing the papers cited in a tex file
//
// typesafe
//
// checked 11/2009
//

package celsius.Threads;

import celsius.Item;
import celsius.ItemTable;
import celsius.Library;
import celsius.tools.toolbox;
import java.util.ArrayList;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

public class ThreadShowCited extends Thread {
    
    private final ProgressMonitor PM;                    // Progress label
    private final ItemTable DT;
    private final ArrayList<Item> FinalRefs;
    private final String filename;
    
    // Constructor
    public ThreadShowCited(Library L, ProgressMonitor p, String fn, ItemTable dt) {
        PM=p; DT=dt; filename=fn;
        FinalRefs=new ArrayList<Item>();
    }
    
    // Main Routine
    @Override
    public void run() {
        setStatusBegin();
        ArrayList<String> Refs = toolbox.getCitations(filename);
        if (Refs.isEmpty()) {
            warning("No citation tags found.");
            return;
        }
        for(int i=0;i<Refs.size();i++)
            FinalRefs.add(null);
        int pos;
        for (Item doc : DT.Lib) {
            if (PM.isCanceled()) break;
            PM.setProgress(doc.pos);
            pos=Refs.indexOf(doc.get("citation-tag"));
            if (pos>-1)
               FinalRefs.set(pos, doc);
        }
        for (Item doc : FinalRefs)
            if (doc!=null) DT.addItem(doc);
        setStatusDone();
    }

    private void warning(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                toolbox.Warning(null,s, "Warning");
            }
        });
    }

    private void setStatusBegin() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DT.MF.setThreadMsg("Looking for references...");
                DT.MF.jPBSearch.setIndeterminate(true);
                DT.clear();
            }
        });
    }

    private void setStatusDone() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DT.MF.setThreadMsg("Ready.");
                DT.MF.jPBSearch.setIndeterminate(false);
                DT.resizeTable(true);
                PM.close();
            }
        });
    }

    
}
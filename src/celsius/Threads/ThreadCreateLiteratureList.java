//
// Celsius Library System
// (w) by C. Saemann
//
// SynchronizeLibThread.java
//
// This class synchronizes the physical files with the library
//
// typesafe
//
// checked: 16.09.2007
//

package celsius.Threads;

import celsius.*;
import celsius.tools.MsgLogger;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.ProgressMonitor;

public class ThreadCreateLiteratureList extends Thread {

    private final Resources RSC;
    
    private final MainFrame Mother;
    private final ProgressMonitor PM;                    // Progress label
    private final Library Lib;

    private StructureNode Root;
    private String Target;
    private TextFile out;
    private Plugin plugin;
    private String params;
    
    private final MsgLogger Msg1;
    private final String TI;
    
    private boolean autoinclude;
    
    int pos;
    
    // Constructor
    public ThreadCreateLiteratureList(StructureNode SN, String target, ProgressMonitor p,Resources rsc,MainFrame m) {
        RSC=rsc;
        TI="CLL>";
        Lib=RSC.getCurrentSelectedLib(); Msg1=RSC.Msg1; PM=p; Mother=m;
        Root=SN; Target=target;
        String name=(String)m.jIP.jCBBibPlugins.getSelectedItem();
        plugin = RSC.Plugins.get(name);
        params = RSC.Plugins.parameters.get(name);
    }

    public void out(String s,int d) throws IOException {
        if (s.length()==0) {
            out.putString("");
            return ;
        }
        String off=("                                                 ").substring(0,d);
        while (s.length()>0) {
            out.putString(off+Parser.CutTill(s, "\n"));
            s=Parser.CutFrom(s,"\n");
        }
    }

    public void addNode(StructureNode TN,int d) throws IOException {
        String cat=TN.toString();
        PM.setNote("working on: "+cat);
        PM.setMaximum(Lib.getSize());
        out(cat,d);
        out("-------------------------",d);
        for (Item doc : Lib) {
            if (PM.isCanceled()) break;
            if (Parser.EnumContains(doc.get("autoregistered"), cat) || Parser.EnumContains(doc.get("registered"), cat)) {
                out(Mother.JM.getBibOutput(doc),d);
                out("",d);
            }
            PM.setProgress(doc.pos);
        }
        if (!PM.isCanceled()) {
            Enumeration<StructureNode> E=TN.children();
            for (;E.hasMoreElements();) {
                addNode(E.nextElement(),d+3);
            }
        }
    }
    
    // Main Routine
    @Override
    public void run() {
        try {
            Msg1.repS();
            Msg1.repS(TI+"CreateLiteratureListThread::"+toolbox.ActDatum());
            Msg1.repS(TI+"This routine creates a literature list");

            out=new TextFile(Target,false);
            addNode(Root,0);
            out.close();
            Msg1.repS(TI+"Creating LiteratureList done.");
            PM.close();
        } catch (Exception e) { 
            Msg1.repS(TI+"Error creating literature list: "+e.toString());
            e.printStackTrace();
        }
        Mother.updateStatusBar(true);
        Mother.setThreadMsg("Ready.");
    }
    
}
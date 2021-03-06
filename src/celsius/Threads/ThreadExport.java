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

import celsius.Dialogs.MultiLineEditor;
import celsius.Dialogs.SingleLineEditor;
import celsius.*;
import celsius.tools.MsgLogger;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

public class ThreadExport extends Thread {
    
    private final Library Lib;           // Library
    private final Resources RSC;
    private final ProgressMonitor PM;    // Progress label

    private final ArrayList<Item> items;
    private final HashMap<String,String> KeepInfo;
    private final boolean clip;
    private String target;               // target file
    private String parameters;
    
    private final MsgLogger Msg1;
    private final String TI;

    private final Plugin plugin;

    private int done;
    
    // Constructor
    public ThreadExport(Library lib, Resources rsc, MsgLogger msg1,ProgressMonitor p, Plugin pl, String para, ArrayList<Item> d, boolean c, String t) {
        TI="CBT>";
        KeepInfo=new HashMap<String,String>();
        Lib=lib; PM=p; RSC=rsc; Msg1=msg1;
        parameters=para;
        items=d; clip=c; plugin=pl;
        target=t;
    }
    
    // Main Routine
    @Override
    public void run() {
        try {
            
            Msg1.repS();
            Msg1.repS(TI+"Export started::"+toolbox.ActDatum());
            Msg1.repS(TI+"This routine handles export tasks");
            
            StringBuffer out=new StringBuffer(10000);
            done=0;
            if (items==null) {
                for (Item doc : Lib) {
                    done++;
                    doDoc(doc,out);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            PM.setProgress(done);
                        }
                    });
                    if (PM.isCanceled()) break;
                }
            } else {
                for (Item doc : items) {
                    done++;
                    doDoc(doc, out);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            PM.setProgress(done);
                        }
                    });
                }
            }
            String mD=plugin.metaData.get("finalize");
            if ((mD!=null) && mD.equals("yes")) {
                    try {
                        FinalizePlugin(out);
                    } catch (Exception ex) {
                        Msg1.repS("Error while finalizing plugin: " + ex.toString());
                        Msg1.printStackTrace(ex);
                        toolbox.Warning(null,"Error while finalizing plugin:\n" + ex.toString(), "Exception:");
                    }
            }
            if (clip) {
                Clipboard Clp = RSC.getMF().getToolkit().getSystemClipboard();
                StringSelection cont = new StringSelection(out.toString());
                Clp.setContents(cont, RSC.getMF());
            } else {
                TextFile f1 = new TextFile(target, false);
                f1.putString(out.toString());
                f1.close();
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    RSC.getMF().setThreadMsg("Ready.");
                }
            });
            Msg1.repS(TI + "Export done.");
            PM.close();
        } catch (Exception e) {
            e.printStackTrace();
       }
    }

    private void doDoc(Item doc, StringBuffer out) {
        try // Reading addinfo of all entries
        {
            MProperties Information = doc.getMProperties(true);
            Information.put("$$params", parameters);
            for (String field : KeepInfo.keySet())
                Information.put(field,KeepInfo.get(field));
            String plaintxt = doc.get("plaintxt");
            if (plugin.metaData.containsKey("questions")) {
                String[] questions=plugin.metaData.get("questions").split("\\|");
                for (int i=0;i<questions.length;i++) {
                    String q=questions[i];
                    String type=Parser.CutTill(q, ":");
                    String target=Parser.CutFromLast(q,":");
                    String question=Parser.CutTill(Parser.CutFrom(q,":"),":");
                    if (!Information.containsKey(target)) {
                        if (type.equals("line")) {
                            SingleLineEditor SLE=new SingleLineEditor(RSC, question, "", true);
                            SLE.setVisible(true);
                            if (!SLE.cancel) {
                                Information.put(target, SLE.text);
                            } else {
                                Information.put(target,null);
                            }
                        }
                        if (type.equals("multiline")) {
                            MultiLineEditor MLE=new MultiLineEditor(RSC, question,"");
                            MLE.setVisible(true);
                            if (!MLE.cancel) {
                                Information.put(target, MLE.text);
                            } else {
                                Information.put(target,null);
                            }
                        }
                        if (type.equals("file")) {


                        }
                    }
                }
            }
            if ((plugin.needsFirstPage() || plugin.wouldLikeFirstPage()) && (plaintxt!=null)) {
                String firstpage = toolbox.getFirstPage(doc.completeDir(plaintxt));
                Information.put("firstpage", firstpage);
            }
            ArrayList<String> msg = new ArrayList<String>();
            try {
                Thread tst = plugin.Initialize(Information, msg);
                tst.start();
                tst.join();
                String s1=Information.getS("output");
                if (s1.length()>0) {
                    out.append(s1);
                }
            } catch (Exception ex) {
                RSC.Msg1.repS("jIP>Error while running BibPlugin: " + plugin.metaData.get("title") + ". Exception: " + ex.toString());
                toolbox.Warning(RSC.getMF(),"Error while applying Bibplugins to doc:\n"+doc.toText()+"\nMessage:\n" + ex.toString(), "Exception:");
                RSC.Msg1.printStackTrace(ex);
            }
            KeepInfo.clear();
            for (String field : Information.keySet()) {
                if (field.startsWith("$$keep"))
                    KeepInfo.put(field, Information.get(field));
            }
        } catch (Exception ecx) { Msg1.repS(TI+"Error writing file: "+ecx.toString()); }
    }

    private void FinalizePlugin(StringBuffer out) {
        try // Reading addinfo of all entries
        {
            MProperties Information = new MProperties();
            Information.put("$$params", parameters);
            for (String field : KeepInfo.keySet())
                Information.put(field,KeepInfo.get(field));
            Information.put("$$finalize","yes");
            if (plugin.metaData.containsKey("questions")) {
                String[] questions=plugin.metaData.get("questions").split("\\|");
                for (int i=0;i<questions.length;i++) {
                    String q=questions[i];
                    String type=Parser.CutTill(q, ":");
                    String ltarget=Parser.CutFromLast(q,":");
                    String question=Parser.CutTill(Parser.CutFrom(q,":"),":");
                    if (!Information.containsKey(ltarget)) {
                        if (type.equals("line")) {
                            SingleLineEditor SLE=new SingleLineEditor(RSC, question, "", true);
                            SLE.setVisible(true);
                            if (!SLE.cancel) {
                                Information.put(ltarget, SLE.text);
                            } else {
                                Information.put(ltarget,null);
                            }
                        }
                        if (type.equals("multiline")) {
                            MultiLineEditor MLE=new MultiLineEditor(RSC, question,"");
                            MLE.setVisible(true);
                            if (!MLE.cancel) {
                                Information.put(ltarget, MLE.text);
                            } else {
                                Information.put(ltarget,null);
                            }
                        }
                    }
                }
            }
            ArrayList<String> msg = new ArrayList<String>();
            try {
                Thread tst = plugin.Initialize(Information, msg);
                tst.start();
                tst.join();
                String s1=Information.getS("output");
                if (s1.length()>0) {
                    out.append(s1);
                }
            } catch (Exception ex) {
                RSC.Msg1.repS("jIP>Error while finalizing BibPlugin: " + plugin.metaData.get("title") + ". Exception: " + ex.toString());
                toolbox.Warning(RSC.getMF(),"Error while finalizing Bibplugins\nMessage:\n" + ex.toString(), "Exception:");
                RSC.Msg1.printStackTrace(ex);
            }
        } catch (Exception ecx) { Msg1.repS(TI+"Error writing file: "+ecx.toString()); }
    }
    
}
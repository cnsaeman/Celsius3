//
// Celsius Library System v2
// (w) by C. Saemann
//
// ThreadApplyPlugin.java
//
// This class combines all necessary data for a library
//
// typesafe
// 
// checked 16.09.2007
//
package celsius.Threads;

import celsius.Dialogs.ChooseSearchResult;
import celsius.Dialogs.MultiLineEditor;
import celsius.Dialogs.SingleLineEditor;
import celsius.*;
import celsius.tools.MsgLogger;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

public class ThreadApplyPlugin extends Thread {

    private final MainFrame MF;
    private final ProgressMonitor PM;                    // Progress label
    private final Library Lib;
    private final Plugin plugin;
    private final String params;
    private ArrayList<Item> items;
    private MProperties Info;
    private final HashMap<String,String> KeepInfo;
    private final MsgLogger Msg1;
    private final String TI;
    public String errorMsg;
    private boolean cancel;
    private boolean interact;
    private boolean updateUI;
    private final Resources RSC;

    // Constructor
    public ThreadApplyPlugin(ProgressMonitor pm, Library L, Plugin pl, String para, Resources rsc, ArrayList<Item> docs, boolean update) {
        RSC=rsc;
        MF = rsc.getMF();
        Msg1 = rsc.Msg1;
        plugin = pl;
        params = para;
        Lib = L;
        PM = pm;
        KeepInfo=new HashMap<String,String>();
        items = docs;
        if (items == null) {
            PM.setMaximum(Lib.getSize());
        } else {
            PM.setMaximum(docs.size());
        }
        TI = "Thread " + toolbox.getThreadIndex() + ">";
        errorMsg = "";
        updateUI = update;
        interact = true;
    }

    // Constructor
    public ThreadApplyPlugin(ProgressMonitor pm, Plugin pl, String para, Resources rsc, Item doc, boolean update, boolean inter) {
        RSC=rsc;
        MF = rsc.getMF();
        Msg1 = rsc.Msg1;
        plugin = pl;
        params = para;
        Lib = doc.Lib;
        PM = pm;
        KeepInfo=new HashMap<String,String>();
        items = new ArrayList<Item>();
        items.add(doc);
        TI = "Thread " + toolbox.getThreadIndex() + ">";
        errorMsg = "";
        updateUI = update;
        interact = inter;
    }

    public void ApplyPluginTo(Item item) throws Exception {
        //System.out.print(".");
        Msg1.repS(TI + "Applying Plugin \"" + plugin.metaData.get("title") + "\" to document with id :" + item.get("id"));

        try {
            MProperties Information = item.getMProperties(true);

            //System.out.println("ApplyPluginToInfo :: "+plugin.className);
            Information.put("$$params", params);

            for (String field : KeepInfo.keySet())
                Information.put(field,KeepInfo.get(field));

            if (plugin.metaData.containsKey("questions")) {
                String[] questions=plugin.metaData.get("questions").split("\\|");
                for (int i=0;i<questions.length;i++) {
                    String q=questions[i];
                    String type=Parser.CutTill(q, ":");
                    String target=Parser.CutFromLast(q,":");
                    String question=Parser.CutTill(Parser.CutFrom(q,":"),":");
                    if (!Information.containsKey(target)) {
                        if (type.equals("line")) {
                            SingleLineEditor SLE=new SingleLineEditor(MF.RSC, question, "", true);
                            SLE.setVisible(true);
                            if (!SLE.cancel) {
                                Information.put(target, SLE.text);
                            } else {
                                Information.put(target,null);
                            }
                        }
                        if (type.equals("multiline")) {
                            MultiLineEditor MLE=new MultiLineEditor(MF.RSC, question,"");
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

            String plaintxt = Information.get("plaintxt");

            if (plugin.needsFirstPage && (plaintxt == null)) {
                errorMsg = "Plugin needs plaintext information, but the given document does not contain any such information.";
                return;
            }

            String requiredFields=plugin.metaData.get("requiredFields");
            if ((requiredFields!=null) && (requiredFields.length()>0)) {
                String[] fields=requiredFields.split("\\|");
                for (int i=0;i<fields.length;i++) {
                    if (fields[i].equals("$$linkedFiles")) {
                        ArrayList<Item> AL=item.getLinksOfType("Available Links");
                        if (AL.isEmpty()) return;
                        String res="";
                        for (Item it : AL) {
                            res+="|"+it.getCompleteDirS("location");
                        }
                        Information.put("$$linkedFiles",res.substring(1));
                    }
                    if (!Information.containsKey(fields[i])) {
                        return;
                    }
                }
            }


            if ((plugin.needsFirstPage) && (!Information.containsKey("firstpage"))) {
                String firstpage;
                if (Lib != null) {
                    firstpage = toolbox.getFirstPage(item.completeDir(plaintxt));
                } else {
                    firstpage = toolbox.getFirstPage(plaintxt);
                }
                Information.put("firstpage", firstpage);
            }
            ArrayList<String> msg = new ArrayList<String>();
            Information.reset();
            Thread tst = plugin.Initialize(Information, msg);
            try {
                tst.start();
            } catch (Exception e) {
            }
            msg.add("Plugin started");
            if (plugin.metaData.get("longRunTime").equals("yes")) {
                //System.out.println("Special!!");
                tst.join();
            } else {
                tst.join(60000);
            }
            if (Information.containsKey("##search-results")) {
                msg.add("Search plugin half-finished");
                if (Information.get("##search-results").length() == 0) {
                    toolbox.Information(MF,"There was no search result returned by the plugin.", "Nothing found.");
                    msg.add("Plugin finished::no search result");
                    for (String t : msg) {
                        Msg1.repS(TI + "::Plugin>" + t);
                    }
                    return;
                }
                ChooseSearchResult CSR = new ChooseSearchResult(MF, Information.get("##search-results"),RSC);
                CSR.setVisible(true);
                if (CSR.result == -1) {
                    msg.add("Plugin finished::cancelled");
                    for (String t : msg) {
                        Msg1.repS(TI + "::Plugin>" + t);
                    }
                    return;
                }
                String[] keys = Information.get("##search-keys").split("\\|");
                Information.remove("##search-results");
                Information.remove("##search-keys");
                Information.put("##search-selection", keys[CSR.result]);
                CSR.dispose();
                tst = plugin.Initialize(Information, msg);
                try {
                    tst.start();
                } catch (Exception e) {
                }
                msg.add("Search plugin re-started");
                if (plugin.metaData.get("longRunTime").equals("yes")) {
                    //System.out.println("Special!!");
                    tst.join();
                } else {
                    tst.join(60000);
                }
            }
            msg.add("Plugin finished");
            if (Information.containsKey("$$output")) {
                toolbox.Information(MF,Information.get("$$output"), "Plugin reports:");
                Information.remove("$$output");
            }
            if (Information.changed) {
                msg.add("Entry changed.");
            }

            Information.remove("firstpage");
            Information.remove("$$params");
            Information.remove("##search-selection");
            for (String t : msg) {
                Msg1.repS(TI + "::Plugin>" + t);
            }

            // Write obtained data
            if (Information.changed) {
                for (String field : Information.keySet()) {
                    if (!field.startsWith("$$")) {
                        String value = Information.get(field);
                        if (value.startsWith("/$") && (Lib != null)) {
                            String fn = Parser.CutTill(value.substring(2), "/$");
                            String ft = Parser.CutFromLast(value, "/$");
                            value = "AI::"+ ft;
                            if (((new File(fn)).exists()) && ((new File(item.completeDir(value))).exists())) {
                                TextFile.Delete(item.completeDir(value));
                            }
                            TextFile.moveFile(fn, item.completeDir(value));
                        }
                        if (value.startsWith("/Compress:") && (Lib != null)) {
                            String fn = value.substring(10);
                            value=Lib.compressDir(fn);
                        }
                        item.put(field, value);
                    }
                }
                item.remove("##search-results");
                item.remove("##search-keys");
                item.save();
            }
            KeepInfo.clear();
            for (String field : Information.keySet()) {
                if (field.startsWith("$$keep"))
                    KeepInfo.put(field, Information.get(field));
            }
        } catch (Exception ex) {
            Msg1.repS(TI + ">Error while running Plugin: " + plugin.metaData.get("title") + ". Exception: " + ex.toString());
            Msg1.printStackTrace(ex);
            int i = toolbox.QuestionYN(MF,"Error while applying plugins:\n" + ex.toString() + "\n Continue anyway?", "Exception:");
            if (i == JOptionPane.NO_OPTION) {
                cancel = true;
            }
        }
    }

    public void FinalizePlugin() throws Exception {
        //System.out.print(".");
        Msg1.repS(TI + "Finalizing Plugin \"" + plugin.metaData.get("title"));

        try {
            MProperties Information = new MProperties();

            //System.out.println("ApplyPluginToInfo :: "+plugin.className);
            Information.put("$$params", params);
            Information.put("$$finalize","yes");

            for (String field : KeepInfo.keySet())
                Information.put(field,KeepInfo.get(field));

            ArrayList<String> msg = new ArrayList<String>();
            Information.reset();
            Thread tst = plugin.Initialize(Information, msg);
            try {
                tst.start();
            } catch (Exception e) {
            }
            msg.add("Plugin started");
            if (plugin.metaData.get("longRunTime").equals("yes")) {
                //System.out.println("Special!!");
                tst.join();
            } else {
                tst.join(60000);
            }
            if (Information.containsKey("##search-results")) {
                msg.add("Search plugin half-finished");
                if (Information.get("##search-results").length() == 0) {
                    toolbox.Information(MF,"There was no search result returned by the plugin.", "Nothing found.");
                    msg.add("Plugin finished::no search result");
                    for (String t : msg) {
                        Msg1.repS(TI + "::Plugin>" + t);
                    }
                    return;
                }
                ChooseSearchResult CSR = new ChooseSearchResult(MF, Information.get("##search-results"),RSC);
                CSR.setVisible(true);
                if (CSR.result == -1) {
                    msg.add("Plugin finished::cancelled");
                    for (String t : msg) {
                        Msg1.repS(TI + "::Plugin>" + t);
                    }
                    return;
                }
                String[] keys = Information.get("##search-keys").split("\\|");
                Information.remove("##search-results");
                Information.remove("##search-keys");
                Information.put("##search-selection", keys[CSR.result]);
                CSR.dispose();
                tst = plugin.Initialize(Information, msg);
                try {
                    tst.start();
                } catch (Exception e) {
                }
                msg.add("Search plugin re-started");
                if (plugin.metaData.get("longRunTime").equals("yes")) {
                    //System.out.println("Special!!");
                    tst.join();
                } else {
                    tst.join(60000);
                }
            }
            msg.add("Plugin finished");
            if (Information.containsKey("$$output")) {
                toolbox.Information(MF,Information.get("$$output"), "Plugin reports:");
                Information.remove("$$output");
            }


            for (String t : msg) {
                Msg1.repS(TI + "::Plugin>" + t);
            }
        } catch (Exception ex) {
            Msg1.repS(TI + ">Error while running Plugin: " + plugin.metaData.get("title") + ". Exception: " + ex.toString());
            Msg1.printStackTrace(ex);
            int i = toolbox.QuestionYN(MF,"Error while applying plugins:\n" + ex.toString() + "\n Continue anyway?", "Exception:");
            if (i == JOptionPane.NO_OPTION) {
                cancel = true;
            }
        }
    }

    // Main Routine
    @Override
    public void run() {
        if (items == null) {
            for (Item doc : Lib) {
                if (PM.isCanceled() || cancel) {
                    break;
                }
                if (PM != null) {
                    PM.setProgress(doc.pos);
                }
                try {
                    ApplyPluginTo(doc);
                } catch (Exception ex) {
                    Msg1.repS("Error working on file with id " + doc.get("id") + ": " + ex.toString());
                    Msg1.printStackTrace(ex);
                    int i = toolbox.QuestionYN(MF,"Error working on file with id " + doc.get("id") + ":\n" + ex.toString() + "\n Continue anyway?", "Exception:");
                    if (i == JOptionPane.NO_OPTION) {
                        cancel = true;
                    }
                }
            }
        } else {
            for (Item doc : items) {
                if (PM != null) {
                    if (PM.isCanceled() || cancel) {
                        break;
                    }
                    PM.setProgress(items.indexOf(doc));
                }
                try {
                    ApplyPluginTo(doc);
                } catch (Exception ex) {
                    Msg1.repS("Error working on file with id " + doc.get("id") + ": " + ex.toString());
                    Msg1.printStackTrace(ex);
                    int i = toolbox.QuestionYN(MF,"Error working on file with id " + doc.get("id") + ":\n" + ex.toString() + "\n Continue anyway?", "Exception:");
                    if (i == JOptionPane.NO_OPTION) {
                        cancel = true;
                    }
                }
            }
        }
        String mD=plugin.metaData.get("finalize");
        if ((mD!=null) && mD.equals("yes")) {
                try {
                    FinalizePlugin();
                } catch (Exception ex) {
                    Msg1.repS("Error while finalizing plugin: " + ex.toString());
                    Msg1.printStackTrace(ex);
                    toolbox.Warning(MF,"Error while finalizing plugin:\n" + ex.toString(), "Exception:");
                }
        }
        if (PM != null) {
            PM.close();
        }
        if (Lib != null) {
            Lib.updatePeopleAndKeywordsLists();
        }
        if (interact && (items != null) && (errorMsg.length() != 0)) {
            toolbox.Warning(MF,errorMsg, "Error applying plugin:");
        }
        if (updateUI) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (items==null) {
                        MF.JM.updateDTs();
                    } else {
                        for (Item doc : items)
                            MF.JM.updateDTs(doc.id);
                    }
                    MF.jIP.updateHTMLview();
                    MF.jIP.updateRawData();
                    MF.updateStatusBar(false);
                    MF.setThreadMsg("Ready.");
                }
            });
        }
    }
}

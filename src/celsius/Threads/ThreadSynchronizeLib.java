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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

public class ThreadSynchronizeLib extends Thread {

    private final MainFrame MF;
    private final ProgressMonitor PM;                    // Progress label
    private final Library Lib;
    private ArrayList<String> IDs;
    private ArrayList<String> toDelete;
    private ArrayList<String> toRemove;
    private ArrayList<String> toRemoveSecond;
    public final ArrayList<Item> addedItems;
    private final MsgLogger Msg1;
    private final String TI;
    private boolean autoinclude;
    int pos;

    // Constructor
    public ThreadSynchronizeLib(Library L, ProgressMonitor p, MsgLogger msg, MainFrame m) {
        Lib = L;
        Msg1 = msg;
        PM = p;
        MF = m;
        IDs = new ArrayList<String>();
        toDelete = new ArrayList<String>();
        toRemove = new ArrayList<String>();
        toRemoveSecond = new ArrayList<String>();
        autoinclude = false;
        addedItems = new ArrayList<Item>();
        TI = "SLT>";
    }

    private void Test(Item doc, String tag) throws IOException {
        if (doc.get(tag) != null) {
            if (!(new File(doc.getCompleteDirS(tag))).exists()) {
                Msg1.repS(TI + "File " + doc.get(tag) + " belonging to item " + doc.toText() + "\nis missing.");
                int i = toolbox.QuestionAB(MF, "Parts of the library entry for the item:\n" + doc.toText() + "\nare apparently corrupt "
                        + "as the file\n" + doc.get(tag) + "\nis missing.\nShould the file reference be removed?", "Please decide:", "Yes", "No");
                if (i == 0) {
                    doc.putF(tag, null);
                    doc.save();
                }
            }
        }
    }

    private void TestFileType(Item doc) throws IOException {
        if (doc.get("location") == null) {
            if (doc.get("filetype") != null) {
                toolbox.Warning(MF, "No associated file but filetype set for " + doc.toText() + ".\n Please adjust manually.", "Found ill-defined file location");
            }
            return;
        }
        if (!doc.get("location").endsWith(doc.get("filetype"))) {
            Msg1.repS(TI + "Filetype " + doc.get("filetype") + " not matching for " + doc.toText() + ".");
            toolbox.Warning(MF, "Filetype " + doc.get("filetype") + " not matching for " + doc.toText() + ".\n Please adjust manually.", "Found different filetypes");
        }
        if ((doc.get("filetype") == null) && (doc.get("location") != null)) {
            toolbox.Warning(MF, "Filetype not available but associated file set for " + doc.toText() + ".\n Please adjust manually.", "Found ill-defined file location");
        }
    }

    public void doJob() {
        String[] docsA = (new File(Lib.basedir + "/documents/.")).list();
        ArrayList<String> remainingFiles = new ArrayList(Arrays.asList(docsA));
        PM.setNote("Synchronizing tags and checking document files...");
        try {
        Msg1.repS();
        Msg1.repS(TI + "SynchronizeLibThread::" + toolbox.ActDatum());
        Msg1.repS(TI + "This routine synchronizes the library to the physical files");

            // See if all documents are available and standard tags match
            int max = Lib.getSize();
            String f;
            int op = 0;
            int synchtest = Lib.Index.checkIntegrity();
            if (synchtest == -100) {
                toolbox.Warning(MF, "Number of indices and elements do not match!", "Warning!");
                return;
            }
            if (synchtest > -1) {
                toolbox.Warning(MF, "Index not consistent starting at position " + String.valueOf(synchtest), "Warning!");
                return;
            }
            for (Item item : Lib) {
                if (PM.isCanceled()) {
                    return;
                }
                PM.setProgress(item.pos);
                item.ensureAddInfo();
                if (item.error == 0) {
                    String fn;
                    if (!item.getCompleteDirS("addinfo").endsWith(item.get("id") + ".xml")) {
                        toolbox.Warning(MF, "The id for the item\n" + item.toText() + "\n does not match the id in the information file.", "Warning:");
                    }
                    for (String tag : item.totalKeySet()) {
                        if ((!tag.equals("autoregistered")) && (!tag.equals("registered")) && (!tag.equals("addinfo"))) {
                            if (!item.isSynchronous(tag)) {
                                Msg1.repS(TI + "Tag " + tag + " belonging to item\n" + item.toString() + "\nis asynchronous.");
                                int i = 0;
                                if (op == 0) {
                                    i = toolbox.QuestionABCD(MF, item.toText() + "\nTag : " + tag + "\nLibrary Index: " + item.getI(tag) + "\nAssociated datafile: " + item.getFromCD(tag) + "\nWhich one should be kept?", "Found different tags", "Library Index", "Data File", "No changes", "Cancel");
                                }
                                if (i == 3) {
                                    PM.close();
                                    return;
                                }
                                if (op == 0) {
                                    int j = toolbox.QuestionAB(MF, "Would you like to do this in all cases", "Confirm", "Yes", "No");
                                    if (j == 0) {
                                        op = i + 10;
                                    }
                                    System.out.println(op);
                                }
                                if ((op == 10) || (i == 0)) {
                                    item.putF(tag, item.get(tag));
                                    item.save();
                                }
                                if ((op == 11) || (i == 1)) {
                                    item.putF(tag, item.getFromCD(tag));
                                    Lib.setChanged(true);
                                }
                            }
                            fn = item.get(tag);
                            if ((fn != null) && ((fn.startsWith("LD::/documents/") || (fn.startsWith("LD::documents/"))))) {
                                Test(item, tag);
                                remainingFiles.remove(Parser.CutFromLast(fn, toolbox.filesep));
                            }
                        }
                    }
                    if ((item.get("bibtex") != null) && (item.get("bibtex").trim().length() != 0)) {
                        BibTeXRecord btr = new BibTeXRecord(item.get("bibtex"));
                        if ((btr.parseError != 0) && (btr.parseError != 2) && (btr.parseError < 250)) {
                            toolbox.Warning(MF, "Bibtex-entry not consistent for item:\n" + item.toText() + "\nError: " + BibTeXRecord.status[btr.parseError] + "\nPlease correct manually.", "Warning!");
                        }
                        if (item.get("citation-tag") == null) {
                            int i = toolbox.QuestionYN(MF, "BibTeX-entry, but no citation tag found for item:\n" + item.toText() + "\nShall the citation tag be created?", "Question");
                            if (i == JOptionPane.YES_OPTION) {
                                item.putF("citation-tag", btr.tag);
                                item.save();
                            }
                        } else {
                            if (!btr.tag.equals(item.get("citation-tag"))) {
                                int i = toolbox.QuestionYN(MF, "BibTeX-entry tag does not match citation tag for item:\n" + item.toText() + "\nShall the citation tag be adjusted?", "Question");
                                if (i == JOptionPane.YES_OPTION) {
                                    item.putF("citation-tag", btr.tag);
                                    item.save();
                                }
                            }
                        }
                    }
                    TestFileType(item);
                } else {
                    int i = toolbox.QuestionYNC(MF, "The library entry for the item:\n" + item.toText() + "\nis apparently corrupt "
                            + "as the information file is not available.\nShould the information file be restored?\nOtherwise, the entry will be removed from the library and the associated files will be deleted", "Please decide:");
                    if (i == JOptionPane.NO_OPTION) {
                        toRemove.add(item.get("id"));
                        System.out.println("...");
                    }
                    if (i == JOptionPane.YES_OPTION) {
                        item.restoreAI();
                    }
                    if (i == JOptionPane.CANCEL_OPTION) {
                        PM.close();
                        return;
                    }
                }
                if (IDs.indexOf(item.get("id")) > -1) {
                    Msg1.repS(TI + "ID " + item.get("id") + " belonging to item\n" + item.toText() + " already in use!");
                    int i = toolbox.QuestionABCD(MF, "ID " + item.get("id") + " belonging to item\n" + item.toText() + " already in use by\n" + (new Item(Lib, item.get("id"))).toText() + "\n", "Data File", "Delete from Index", "Delete completely", "Nothing", "Cancel");
                    if (i == 0) {
                        toRemoveSecond.add(item.get("id"));
                    }
                    if (i == 1) {
                        toDelete.add(item.get("id"));
                    }
                    if (i == 3) {
                        PM.close();
                        return;
                    }
                } else {
                    IDs.add(item.get("id"));
                }
            }
            for (String id : toRemove) {
                int pos = Lib.Index.getPosition(id);
                Lib.Index.deleteElement(pos);
            }
            for (String id : toRemoveSecond) {
                Lib.Index.toFirstElement();
                boolean found = false;
                while (!Lib.Index.endReached) {
                    if (Lib.Index.get("id").equals(id)) {
                        if (found) {
                            Lib.Index.deleteCurrentElement();
                            Lib.setChanged(true);
                        } else {
                            found = true;
                        }
                    }
                    Lib.Index.nextElement();
                }
            }
            for (String id : toDelete) {
                (new Item(Lib, id)).removeFromLib(true);
            }

            // See if there are any unrecognized id-tags left over...
            String[] liste = (new File(Lib.basedir + "/information/.")).list();
            PM.setNote("Looking for files not included in the index...");
            PM.setMaximum(liste.length);
            for (int i = 0; (i < liste.length) && (!PM.isCanceled()); i++) {
                PM.setProgress(i);
                // Teste, ob potentieller Eintrag
                if (liste[i].endsWith(".xml")) {
                    String id = Parser.CutTill(liste[i], ".");
                    if (IDs.indexOf(id) < 0) {
                        try {
                            celsius.tools.XMLHandler CD = new celsius.tools.XMLHandler(Lib.basedir + "/information/" + id + ".xml");
                            String actid = CD.get("id");
                            if (!actid.equals(id)) {
                                TextFile.Delete(Lib.basedir + "/information/" + id + ".xml");
                            } else {
                                int jk = 0;
                                if (!autoinclude) {
                                    jk = toolbox.QuestionABCD(MF, "Found an unregistered entry:\n"
                                            + "title: " + CD.get("title") + "\n"
                                            + "authors: " + CD.get("authors") + "\n"
                                            + "id: " + CD.get("id") + "\n"
                                            + "id in file: " + id + ".\n"
                                            + "Add entry to library?", "Please decide:", "Yes", "Yes to all", "No", "Cancel");
                                    if (jk == 3) {
                                        PM.close();
                                        return;
                                    }
                                    if (jk == 1) {
                                        autoinclude = true;
                                    }
                                }
                                if (jk < 2) {
                                    Msg1.repS(TI + "---------------------");
                                    Msg1.repS(TI + "Adding the item:");
                                    Msg1.repS(TI + CD.get("title"));
                                    Msg1.repS(TI + CD.get("authors"));
                                    Item item = Lib.createEmptyItem();
                                    String fn;
                                    for (String key : CD.XMLTags) {
                                        if (!key.equals("id")) {
                                            fn = CD.get(key);
                                            if ((fn != null) && ((fn.startsWith("LD::information/") || fn.startsWith("LD::/information/") || fn.startsWith("AI::")))) {
                                                String src = Lib.completeDir(fn, CD.get("id"));
                                                item.put(key, item.getAIFile(TextFile.getFileType(src)));
                                                String trg = item.getCompleteDirS(key);
                                                TextFile.moveFile(src, trg);
                                            } else {
                                                item.putF(key, CD.get(key));
                                            }
                                            if ((fn != null) && ((fn.startsWith("LD::/documents/") || (fn.startsWith("LD::documents/"))))) {
                                                remainingFiles.remove(Parser.CutFromLast(fn, toolbox.filesep));
                                            }
                                        }
                                    }
                                    item.save();
                                    addedItems.add(item);
                                    TextFile.Delete(CD.source);
                                    Lib.addPeople(item);
                                } else {
                                    jk = toolbox.QuestionYN(MF, "Remove the corresponding file?", "Confirm:");
                                    if (jk == JOptionPane.YES_OPTION) {
                                        Msg1.repO(TI + "Deleting superfluous file: " + Lib.basedir + "/information/" + id + ".xml: ");
                                        Msg1.repS(TextFile.Delete(Lib.basedir + "/information/" + id + ".xml"));
                                    }
                                }
                            }
                        } catch (Exception exp) {
                            Msg1.printStackTrace(exp);
                            int jk = toolbox.QuestionYN(MF, "Error loading unregistered .xml file:\n" + Lib.basedir + "/information/" + id + ".xml\nRemove the corresponding file?", "Confirm:");
                            if (jk == JOptionPane.YES_OPTION) {
                                Msg1.repO(TI + "Deleting superfluous file: " + Lib.basedir + "/information/" + id + ".xml: ");
                                Msg1.repS(TextFile.Delete(Lib.basedir + "/information/" + id + ".xml"));
                            }
                        }
                    }
                }
            }
            if ((!PM.isCanceled()) && (remainingFiles.size() > 0)) {
                int jk = toolbox.QuestionYN(MF, "There are files in the document directory, which are not referred to by any Library entry.\nShould they be moved to the toinclude/ subdirectory?", "Confirm:");
                if (jk == JOptionPane.YES_OPTION) {
                    if (!(new File("toinclude").exists())) {
                        (new File("toinclude")).mkdir();
                    }
                    for (String p : remainingFiles) {
                        TextFile.moveFile(Lib.basedir + "documents" + toolbox.filesep + p, "toinclude/" + p);
                    }
                }
            }
            Msg1.repS(TI + "Synchronizing library done.");
            PM.close();
        } catch (Exception e) {
            Msg1.printStackTrace(e);
            Msg1.repS(TI + "Error synchronizing library: " + e.toString());
        }

    }

    // Main Routine
    @Override
    public void run() {
        doJob();

        MF.updateStatusBar(true);
        toolbox.Information(MF, "Synchronizing complete.", "Information");
        MF.setThreadMsg("Ready.");
        if (addedItems.size() > 0) {
            ItemTable CDT = MF.RSC.makeNewTabAvailable(8, "Last added", "magnifier");
            for (Item doc : addedItems) {
                CDT.addItemFast(doc);
            }
            CDT.resizeTable(true);
            for (ItemTable DT : MF.RSC.ItemTables) {
                DT.refresh();
            }
            MF.jIP.updateHTMLview();
            MF.jIP.updateRawData();
            MF.updateStatusBar(true);
        }

    }
}

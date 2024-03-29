/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import celsius.tools.ExecutionShell;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author cnsaeman
 */
public class JobManager {

    private final MainFrame MF;
    private final Resources RSC;

    public JobManager(MainFrame mf,Resources rsc) {
        MF=mf;
        RSC=rsc;
    }

    /**
     * Shows the links of given type. If type is null, then it shows all links
     * @param type
     */
    public void showCombined() {
        Item doc = RSC.getCurrentItemTable().getSelectedItems().get(0);
        String name=doc.toText();
        ItemTable IT=new ItemTable(MF,doc.Lib,name,7);
        RSC.ItemTables.add(IT);

        MF.jMICloseTab2.setEnabled(true);
        MF.jMICloseTab.setEnabled(true);
        final JScrollPane scrollpane = new JScrollPane(IT.jtable);
        if (name==null) name="Links";
        MF.jTPTabList.add(scrollpane);
        MF.jTPTabList.setTabComponentAt(MF.jTPTabList.getTabCount() - 1,new TabLabel( name,"folder_link",RSC,IT,true));
        MF.jTPTabList.setSelectedComponent(scrollpane);
        RSC.setCurrentDT(name,"folder_link");
        IT.setType(7);
        IT.lastHTMLview=doc.Lib.displayString(7,null, null);
        IT.creationHTMLview=IT.lastHTMLview;
        int pages=0;
        double duration=0;
        int documents=0;
        IT.setLibrary(doc.Lib);
        for (Item doc2 : doc.getCombined()) {
            IT.addItemFast(doc2);
            pages+=doc2.getPages();
            duration+=doc2.getDuration();
        }
        IT.mproperties.put("linktype",name);
        IT.jtable.setVisible(true);
        IT.Lib.autoSortColumn(IT);
        IT.resizeTable(true);
    }

    /**
     * View Selected Document
     */
    public void ViewSelected(String nmb) {
        if (MF.jTPTabList.getSelectedIndex() < 0) {
            return;
        }
        Item item = RSC.getCurrentItemTable().getSelectedItems().get(0);
        if (item.get("combine")!=null) {
            showCombined();
            return;
        }
        if (item.get("location")==null) {
            String cmdln=RSC.getJournalLinkCmd(item);
            if (cmdln.length()>0) {
                JournalLinkSelected();
                return;
            } else {
                if (item.get("url")!=null) {
                    RSC.Configuration.viewHTML(item.get("url"));
                    return;
                } else {
                    if (item.getS("links").length()>0) {
                        if (item.getS("links").indexOf("combines")>-1) {
                            showLinksOfType("combines");
                            return;
                        }
                        showLinksOfType("Available Links");
                        return;
                    } else {
                        toolbox.Warning(MF,"No file or journal link associated with this entry.", "Warning");
                        return;
                    }
                }
            }
        }
        RSC.Configuration.view(item,nmb);
    }

    /**
     * Shows the links of given type. If type is null, then it shows all links
     * @param type
     */
    public void showLinksOfType(String type) {
        Item doc = RSC.getCurrentItemTable().getSelectedItems().get(0);
        ArrayList<Item> AL=doc.getLinksOfType(type);
        String name=type;
        if (name==null) name="Links";
        Library Lib=RSC.getCurrentSelectedLib();
        ItemTable IT=RSC.makeNewTabAvailable(7, name,"folder_link");
        MF.jMICloseTab2.setEnabled(true);
        MF.jMICloseTab.setEnabled(true);
        int pages=0;
        double duration=0;
        int documents=0;
        //System.out.println(Lib.Links.keySet().size());
        for (Item it : AL) {
            IT.addItemFast(it);
            pages += it.getPages();
            duration += it.getDuration();
            documents++;
        }
        MF.jIP.updateHTMLview();
        IT.jtable.setVisible(true);
        IT.Lib.autoSortColumn(IT);
        IT.resizeTable(true);
    }


    public void JournalLinkSelected() {
        if (MF.jTPTabList.getSelectedIndex() < 0) {
            return;
        }
        Item doc = RSC.getCurrentItemTable().getSelectedItems().get(0);
        String cmdln=RSC.getJournalLinkCmd(doc);
        if (cmdln.length()>0) {
            RSC.Msg1.repS("JM>Journal link command: " + cmdln);
            (new ExecutionShell(cmdln, 0, true)).start();
        } else {
            toolbox.Warning(MF,"No journal link found!", "Warning");
        }
    }

    public void goToPerson(String s) {
        try {
            s = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            RSC.Msg1.printStackTrace(ex);
        }
        if (s.indexOf(" ")>-1) s=Parser.CutFromLast(s," ");
        String srch = s.toLowerCase();
        MF.listPersons(srch);
    }

    public String getBibOutput(Item doc) {
        String n = (String)MF.jIP.jCBBibPlugins.getSelectedItem();
        if (n==null) return("");
        Plugin plugin = RSC.Plugins.get(n);
        String params = RSC.Plugins.parameters.get(n);
        Library Lib=RSC.getCurrentSelectedLib();
        if (doc==null) return ("");
        MProperties Information = doc.getMProperties(true);

        String plaintxt = doc.get("plaintxt");

        if ((plugin.needsFirstPage() || plugin.wouldLikeFirstPage()) && (plaintxt!=null)) {
            String firstpage = toolbox.getFirstPage(doc.completeDir(plaintxt));
            Information.put("firstpage", firstpage);
        }
        Information.put("$$params",params);

        ArrayList<String> msg = new ArrayList<String>();
        try {
            Thread tst = plugin.Initialize(Information, msg);
            tst.start();
            tst.join();
        } catch (Exception ex) {
            RSC.Msg1.repS("jIP>Error while running BibPlugin: " + plugin.metaData.get("title") + ". Exception: " + ex.toString());
            toolbox.Warning(MF,"Error while applying Bibplugins:\n" + ex.toString(), "Exception:");
            RSC.Msg1.printStackTrace(ex);
        }
        return (Information.get("output"));
    }

    public void updateDTs(int id) {
        for(ItemTable doctab : RSC.ItemTables) {
            doctab.replace(id);
        }
    }

    public void updateDTs() {
        for(ItemTable doctab : RSC.ItemTables)
            doctab.updateAll();
    }

    public boolean joinItems(Library Lib,String id1,String id2) {
        Item doc1=new Item(Lib,id1);
        Item doc2=new Item(Lib,id2);
        RSC.Msg1.repS("MAIN>Combining items:  + " + doc1.toText() + "\n and \n" + doc2.toText());
        // Move everything into doc1
        for (String tag : doc2.getAITags()) {
            if ((tag.length()!=0) && !tag.equals("location") && !tag.equals("plaintxt") && !tag.equals("id") && 
                    !tag.equals("pages") && !tag.equals("filetype") && !tag.startsWith("altversion") &&
                    !tag.equals("registered") && !tag.equals("autoregistered")) {
                if (doc1.get(tag)==null) {
                    doc1.put(tag, doc2.get(tag));
                    if (doc1.getS(tag).indexOf("::")==2) {
                        String f1 = doc2.getCompleteDirS(tag);
                        String f2 = "AI::" + TextFile.getFileType(f1);
                        doc1.put(tag, f2);
                        TextFile.Delete(doc1.completeDir(f2));
                        try {
                            TextFile.moveFile(f1, doc1.completeDir(f2));
                        } catch (IOException ex) {
                            RSC.Msg1.printStackTrace(ex);
                            RSC.Msg1.printStackTrace(ex);
                        }
                    }
                } else if (!doc1.get(tag).equals(doc2.get(tag))) {
                    int i = toolbox.QuestionABC(MF,"Which information should be kept for the tag:\n" + tag + "?\nA: " + doc1.get(tag) + "\nB: " + doc2.get(tag), "Please decide:", "A", "B", "Cancel");
                    if (i == 2) {
                        return(false);
                    }
                    if (i == 1) {
                        if (doc2.getS(tag).indexOf("::")==2) {
                            String f1 = doc2.getCompleteDirS(tag);
                            String f2 = "AI::"+ TextFile.getFileType(f1);
                            doc1.put(tag, f2);
                            TextFile.Delete(doc1.completeDir(f2));
                            try {
                                TextFile.moveFile(f1, doc1.completeDir(f2));
                            } catch (IOException ex) {
                                RSC.Msg1.printStackTrace(ex);
                                RSC.Msg1.printStackTrace(ex);
                            }
                        }
                        doc1.put(tag, doc2.get(tag));
                    }
                }
            }
        }
        String reg=doc2.getS("registered");
        if (reg.length()>0) {
            reg=doc1.getS("registered")+"|"+reg;
            if (reg.startsWith("|")) reg=reg.substring(1);
            doc1.put("registered", Parser.Substitute(reg, "||", "|"));
        }
        reg=doc2.getS("autoregistered");
        if (reg.length()>0) {
            reg=doc1.getS("autoregistered")+"|"+reg;
            if (reg.startsWith("|")) reg=reg.substring(1);
            doc1.put("autoregistered", Parser.Substitute(reg, "||", "|"));
        }
        try {
            if (doc2.get("location")!=null) {
                // Associate document 2 with document 1
                String avn=doc1.getFreeAltVerNo();
                String filename=Parser.CutProhibitedChars2(doc1.get("title")+" ("+toolbox.shortenNames(doc1.get("authors"))+")");
                doc1.guaranteeStandardFolder();
                filename=doc1.getStandardFolder()+toolbox.filesep+filename+"."+avn+"."+doc2.get("filetype");
                doc1.put("altversion-location-"+avn,filename);
                TextFile.moveFile(doc2.getCompleteDirS("location"), doc1.getCompleteDirS("altversion-location-"+avn));
                if (doc2.get("plaintxt")!=null) {
                    String txttarget="AI::"+avn+".txt.gz";
                    doc1.put("altversion-plaintxt-"+avn,txttarget);
                    TextFile.moveFile(doc2.getCompleteDirS("plaintxt"), doc1.getCompleteDirS("altversion-plaintxt-"+avn));
                }
                doc1.put("altversion-label-"+avn,"Alt version "+avn);
                doc1.put("altversion-filetype-"+avn,doc2.get("filetype"));
                doc1.put("altversion-pages-"+avn,doc2.get("pages"));
            }
            for (String t : doc2.getAITags()) {
                // Associate document 2 altversions with document 1
                if (t.startsWith("altversion-location-")) {
                    String vn=Parser.CutFromLast(t, "-");
                    String avn=doc1.getFreeAltVerNo();
                    String filename=Parser.CutProhibitedChars2(doc1.get("title")+" ("+toolbox.shortenNames(doc1.get("authors"))+")");
                    doc1.guaranteeStandardFolder();
                    filename=doc1.getStandardFolder()+toolbox.filesep+filename+"."+avn+"."+doc2.get("altversion-filetype-"+vn);
                    doc1.put("altversion-location-"+avn,filename);
                    TextFile.moveFile(doc2.getCompleteDirS("altversion-location-"+vn), doc1.getCompleteDirS("altversion-location-"+avn));
                    if (doc2.get("altversion-plaintxt-"+vn)!=null) {
                        String txttarget="AI::"+avn+".txt.gz";
                        doc1.put("altversion-plaintxt-"+avn,txttarget);
                        TextFile.moveFile(doc2.getCompleteDirS("altversion-plaintxt-"+vn), doc1.getCompleteDirS("altversion-plaintxt-"+avn));
                    }
                    doc1.put("altversion-label-"+avn,doc2.get("altversion-label-"+vn));
                    doc1.put("altversion-filetype-"+avn,doc2.get("altversion-filetype-"+vn));
                    doc1.put("altversion-pages-"+avn,doc2.get("altversion-pages-"+vn));
                }
            }
        } catch (IOException ex) {
            RSC.Msg1.printStackTrace(ex);
            return(false);
        }
        doc1.save();
        if (doc1.error==6) toolbox.Warning(MF,"Error writing back information", "Warning");
        Lib.setChanged(true);
        doc2.put("location", null);
        doc2.removeFromLib(true);
        return(true);
    }

    public void searchPeopleUpdate(boolean isLong) {
        if (MF.jLSearchPeople.getModel().getSize()==0) return;
        Library Lib = RSC.getCurrentSelectedLib();
        int i = MF.jLSearchPeople.getSelectedIndex();
        if (i == -1) {
            return;
        }
        String person = (String) MF.jLSearchPeople.getSelectedValue();
        if (!isLong) {
            DefaultListModel DLM=new DefaultListModel();
            String s;
            for (String personlong : Lib.PeopleLongList) {
                if (personlong.startsWith(person+", ")) {
                    s=Parser.CutFrom(personlong,", ").trim();
                    if (s.length()>0) DLM.addElement(s);
                }
            }
            MF.jLPeopleLong.setModel(DLM);
        } else {
            person+=", "+(String) MF.jLPeopleLong.getSelectedValue();
        }
        ItemTable IT=RSC.makeTabAvailable(1, person,"user_b");
        Lib.moveToPerson(person);
        Lib.showDocsPerson(person, IT);

        IT.resizeTable(true);
        MF.jIP.setupPeopleRemarks();
    }

    public void updateDTs(ArrayList<Item> docs) {
        for (Item doc : docs)
            updateDTs(doc.id);
    }

    public boolean closeCurrentLibrary(boolean rememberLib) {
        if (RSC.currentLib==-1) return(false);
        final Library Lib=RSC.getCurrentSelectedLib();
        if (Lib.hasChanged()) {
            int h = toolbox.QuestionABC(MF,"Modifications in library " + Lib.name + "\nhave not been saved and will be lost.", "Warning:","Cancel","Save now","Ignore");
            if (h==0) return(false);
            if (h==1) {
                try {
                    Lib.writeBack();
                } catch (IOException ex) {
                    RSC.Msg1.printStackTrace(ex);
                }
            }
        }
        int CSL=RSC.getCurrentSelectedLibNo();
        // Remove all RSC.DocumentTables corresponding to library library
        for (int i=RSC.ItemTables.size()-1;i>-1;i--) {
            if (Lib==RSC.ItemTables.get(i).Lib)
                RSC.ItemTables.get(i).close();
        }
        // Remember Library
        if (rememberLib) {
            RecentLibraryCache RLC=RSC.LastLibraries;
            if (!RLC.containsKey(Lib.name)) {
                RLC.put(Lib.name,Lib.MainFile.source);
                RSC.addRecentLib(Lib.name, Lib.MainFile.source);
            }
        }

        DefaultComboBoxModel DCBM=(DefaultComboBoxModel)MF.jCBLibraries.getModel();

        MF.jMCopyToDiff.remove(CSL);
        MF.jMCopyToDiff1.remove(CSL);
        DCBM.removeElementAt(CSL);
        RSC.Libraries.remove(CSL);
        Lib.closeLibrary();
        MF.switchToLibrary(null);
        return(true);
    }

    public void performAction(String aname, ItemTable IT) {
        RSC.Msg1.repS("MAIN>Action performed: " + aname);
        String currentType="nothing";
        String cmdln=null;
        for (Item item : IT.getSelectedItems()) {
            if (item.get("filetype")!=null) {
                if (!currentType.equals(item.get("filetype"))) {
                    currentType=item.get("filetype");
                    String secondary=RSC.Configuration.SecondaryViewers(currentType);
                    String[] commands=secondary.split("\\|");
                    cmdln=null;
                    for (int i=0;i<commands.length;i++) {
                        if (commands[i].startsWith(aname+":")) {
                            cmdln=Parser.CutFrom(commands[i],":")+" ";
                            break;
                        }
                    }
                }
                if (cmdln!=null) {
                    String actcmdln = cmdln.replace("%from%", item.getCompleteDirS("location"));
                    RSC.Msg1.repS("JM>Action command: " + actcmdln);
                    (new ExecutionShell(actcmdln, 0, true)).start();
                }
            }
        }
    }

    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public void viewPlainText(Item doc) {
        String fn = doc.getCompleteDirS("plaintxt");
        if (fn==null) return;
        try {
            if (new File(fn).exists()) {
                TextFile.CopyFile(fn, doc.Lib.basedir + "/viewer.tmp.txt.gz");
                TextFile.GUnZip(doc.Lib.basedir + "/viewer.tmp.txt.gz");
                (new ViewerText(RSC,doc.Lib.basedir + "/viewer.tmp.txt","Plain text for document: "+doc.get("title")+" by "+toolbox.Authors3FromCelAuthors(doc.get("authors")))).setVisible(true);
                //RSC.Configuration.viewHTML(Lib.basedir + "/viewer.tmp.txt");
            } else {
                toolbox.Warning(MF,"The associated plain text file:\n" + fn + "\ncould not be found.", "Error:");
            }
        } catch (Exception ex) {
            toolbox.Warning(MF,"Error while viewing plain text:\n" + ex.toString(), "Exception:");
            RSC.Msg1.printStackTrace(ex);
        }        
    }

}

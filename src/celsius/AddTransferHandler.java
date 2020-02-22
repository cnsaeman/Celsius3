/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package celsius;

import celsius.Threads.ThreadGetDetails;
import celsius.tools.ExecutionShell;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;

/**
 *
 * @author cnsaeman
 */
public class AddTransferHandler extends TransferHandler {

    public static final DataFlavor[] SUPPORTED_DATA_FLAVORS = new DataFlavor[]{
        DataFlavor.stringFlavor
    };

    MainFrame MF;
    Library lib;

    public AddTransferHandler(MainFrame mf) {
        super();
        MF = mf;
        lib=MF.RSC.getCurrentSelectedLib();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        boolean canImport = false;
        for (DataFlavor flavor : SUPPORTED_DATA_FLAVORS) {
            if (support.isDataFlavorSupported(flavor)) {
                canImport = true;
                break;
            }
        }
        return canImport;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        boolean accept = false;
        if (canImport(support)) {
            try {
                Transferable t = support.getTransferable();
                Component component = support.getComponent();
                if (component instanceof JButton) {
                    String out = (String) t.getTransferData(DataFlavor.stringFlavor);
                    if (out.startsWith("https://arxiv.org/abs/")) {
                        //MF.jPBSearch.setIndeterminate(true);
                        String url="https://arxiv.org/pdf/"+Parser.CutFrom(out, "https://arxiv.org/abs/");
                        ExecutionShell ES = new ExecutionShell("curl -L "+url+" --output out.pdf", 0, false);
                        ES.start();
                        ES.join();
                        if (ES.errorflag) {
                            MF.Msg1.rep("ADD>Error Message: " + ES.errorMsg);
                        }
                        if (new File("out.pdf").exists()) {
                            Item doc = createDoc();
                            doc.put("location", (new File("out.pdf")).getAbsolutePath());
                            doc.put("filetype", "pdf");
                            ThreadGetDetails TGD=new ThreadGetDetails(doc,MF.RSC,true);
                            TGD.start();
                            TGD.join();
                            addItem(doc);
                            ItemTable CDT=MF.RSC.makeNewTabAvailable(8, "Last added","magnifier");
                            CDT.addItemFast(lib.lastAddedItem);
                            CDT.resizeTable(true);
                            for (ItemTable DT : MF.RSC.ItemTables) {
                                DT.refresh();
                            }
                            MF.jIP.updateHTMLview();
                            MF.jIP.updateRawData();
                            MF.updateStatusBar(true);
                        }
                        //MF.jPBSearch.setIndeterminate(false);
                    }
                    System.out.println(out);
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
        return accept;
    }
    
    private Item createDoc() {
        Item doc = new Item();
        for (String tag : lib.IndexTags) {
            if (!tag.equals("addinfo") && !tag.equals("autoregistered") && !tag.equals("registered") && !tag.equals("id")) {
                doc.put(tag, null);
            }
        }
        if (lib.MainFile.get("standardfields")!=null) 
            for (String tag : lib.listOf("standardfields"))
                doc.put(tag, null);
        return (doc);
    }
    
    private void addItem(final Item doc) {
        try {
            if (!doc.getS("$$beingadded").equals("")) return;
            doc.put("$$beingadded","true");
            final Integer[] res=new Integer[1];
            int dbl=lib.Doublette(doc);
            if (dbl==10) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        res[0]=toolbox.QuestionABCD(null,"An exact copy of the document\n"+doc.toText(lib)+"\nis already existing in the library:\n"+lib.marker.toText()+"\nDelete the file "+doc.get("location")+"?","Warning","Yes","No","Always","Abort");
                    }
                });
                if (res[0]==0) {
                    deleteItem(doc,false);
                }
                if (res[0]==2) {
                    deleteItem(doc,false);
                }
                if (res[0]==3) {
                    doc.put("$$beingadded",null);
                }
                return;
            }
            if (dbl==5) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        res[0]=toolbox.QuestionYN(null,"A file with exactly the same length as the document\n"+doc.toText(lib)+"\nis already existing in the library.\nProceed anyway?","Warning");
                    }
                });
                if (res[0]==JOptionPane.NO_OPTION) {
                    doc.put("$$beingadded",null);
                    return;
                }
            }
            if (dbl==4) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                        Object[] options=new Object[6];
                        options[0]="Delete"; options[1]="Replace"; options[2]="New Version"; options[3]="Replace All"; options[4]="Ignore"; options[5]="Cancel";
                        String msg="The document \n"+doc.toText(lib)+
                                                   "\nis already existing in the library. You can\n"+
                                                   "- Delete the item in the inclusion folder\n"+
                                                   "- Replace the item in the library by this item\n"+
                                                   "- Replace the item but treat its current file as an additional version\n"+
                                                   "- Replace all items \n"+
                                                   "- Ignore\n"+
                                                   "- Cancel";
                        res[0]=JOptionPane.showOptionDialog(null, msg, "Warning",JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[4]);
                        }
                    });
                if (res[0]==0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            deleteItem(doc,false);
                        }
                    });
                    return;
                }
                if ((res[0]==1) || (res[0]==3)) {
                    lib.replaceItem(doc);
                    return;
                }
                if (res[0]==2) {
                    Item doc2=lib.marker;
                    doc2.shiftReplaceWithFile(MF.RSC,doc.get("location"));
                    TextFile.Delete(doc.get("plaintxt"));
                    return;
                }
                return;
            }
            int mode = 0;
            int i = lib.addItem(doc, "", mode);
        } catch (Exception e) {
            MF.RSC.Msg1.printStackTrace(e);
        }
    }
    
    private void deleteItem(Item doc, boolean confirmed) {
        if (doc.getS("location").length()>0) {
            if (!confirmed) {
                int j = toolbox.QuestionOC(MF, "Really delete the file " + doc.get("location") + "?", "Warning");
                if (j != JOptionPane.NO_OPTION) {
                    doc.deleteFiles();
                    MF.RSC.Msg1.repS("ADD>Deleting :: " + doc.get("filename"));
                    MF.RSC.Msg1.repS("ADD>Deleting :: " + doc.get("plaintxt"));
                } else {
                    return;
                }
            } else {
                doc.deleteFiles();
                MF.RSC.Msg1.repS("ADD>Deleting :: " + doc.get("filename"));
                MF.RSC.Msg1.repS("ADD>Deleting :: " + doc.get("plaintxt"));
            }
        }
    }
    
    
}

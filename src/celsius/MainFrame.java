//
// Celsius Library System v2
// (w) by C. Saemann
//
// MainFrame.java
//
// This class contains the main frame and the main class of the Celsius Library System
//
// typesafe
//
// ##checked 15.09.2007
// adjusted for long authors
//
package celsius;

import celsius.tools.Parser;
import celsius.Dialogs.*;
import celsius.Threads.*;
import celsius.tools.*;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author  cnsaeman
a */
public class MainFrame extends javax.swing.JFrame implements DropTargetListener,
        ClipboardOwner, TreeModelListener, DocumentListener {

    public Resources RSC;
    public JobManager JM;
    public SplashScreen StartUp;   // splash screen
    public MsgLogger Msg1;                    // protocol class
    public HashMap<String, String> ShortCuts;            // list of shortcuts, implemented in this way to allow for shortcut editor later
    public DefaultTreeModel StructureTreeModel;         // libraries structure tree model
    public DefaultTreeModel RegistrationTreeModel;      // registration tree model
    public XMLTree SRules;

    public Configuration dialogConfiguration;     // Configuration dialog
    public DeepSearch deepSearch;
    public JInfoPanel jIP;

    public ThreadSearch threadSearch;

    private int bufmousex,  bufmousey;                    // buffers for popup menu over categories

    // GUI flags
    public boolean buildingNewTab;

    public boolean isDocSelected;
    public boolean isCatSelected;
    public boolean isLibSelected;
    public boolean isPlugSelected;
    public boolean isTabAvailable;
    private int jTFSearchstate;
    
    /** Creates new form MainFrame */
    public MainFrame() {
        RSC=new Resources(this);
        JM=new JobManager(this,RSC);
        Msg1=RSC.Msg1;
    }

    public void gui1() {
        initComponents();
        jTPSearches.setTabComponentAt(0, new TabLabel("","folder_explore",RSC,null,false));
        jTPSearches.setTabComponentAt(1, new TabLabel("","user_b",RSC,null,false));
        jTPSearches.setTabComponentAt(2, new TabLabel("","book_key",RSC,null,false));
        jTPTechnical.setTabComponentAt(0, new TabLabel("","plugin",RSC,null,false));
        jTPTechnical.setTabComponentAt(1, new TabLabel("","arrow_switch_bluegreen",RSC,null,false));
        jTPTechnical.setTabComponentAt(2, new TabLabel("","lightning_go",RSC,null,false));
        this.setTitle("Celsius Library System "+RSC.VersionNumber);
        jIP=new JInfoPanel(this,RSC,JM);
        jSPMain.setBottomComponent(jIP);
        initFurther();
    }

    public void gui2() {
        this.setLocationByPlatform(true);
        jTFMainSearch.getDocument().addDocumentListener(this);
        jTFSearchAuthors.getDocument().addDocumentListener(this);
        jTFSearchCategories.getDocument().addDocumentListener(this);
        jTFSearchKey.getDocument().addDocumentListener(this);
        dialogConfiguration = new Configuration(this, RSC.Configuration);
        Msg1.repS("Libraries loaded");
        jIP.switchModeTo1();
        jLPlugins.setModel(RSC.Plugins.getPluginsDLM("manual",RSC.getCurrentSelectedLib()));
        jTBAdd.setTransferHandler(new AddTransferHandler(this));
        StartUp.setStatus("Ready...");
        Msg1.repS("packed");
        //this.setSize(1224,740);
        pack();
        setVisible(true);
        jSPMain3.setMinimumSize(new Dimension(RSC.guiScale(280),RSC.guiScale(0)));
        jSPMain3.setDividerLocation(jSPMain3.getMaximumDividerLocation());
        jIP.setMinimumSize(new Dimension(RSC.guiScale(0),RSC.guiScale(280)));
        jSPMain.setDividerLocation(jSPMain.getMaximumDividerLocation());
        Msg1.repS("divider locations set");
        StartUp.toFront();
        StartUp.setTimeTrigger();
        buildingNewTab=false;
        Msg1.repS("all done, checking for updates");
        //(new ThreadCheckUpdates(RSC)).start();
        Msg1.repS("check for updates complete, all systems ready.");
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Remaining initialization
     */
    private void initFurther() {

        // Init Structuretree
        jTStructureTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTStructureTree.setShowsRootHandles(true);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(RSC.Icons.getIcon("folder"));
        renderer.setClosedIcon(RSC.Icons.getIcon("folder"));
        renderer.setOpenIcon(RSC.Icons.getIcon("folder_table"));
        jTStructureTree.setCellRenderer(renderer);
        StructureTreeModel = new DefaultTreeModel(null);
        jTStructureTree.setModel(StructureTreeModel);
        @SuppressWarnings("unused")
        DropTarget dt = (new DropTarget(jTStructureTree, this));

        // Init Registrationtree
        jTRegistrationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultTreeCellRenderer renderer2 = new DefaultTreeCellRenderer();
        renderer2.setLeafIcon(RSC.Icons.getIcon("bullet_sparkle"));
        renderer2.setClosedIcon(RSC.Icons.getIcon("bullet_sparkle"));
        renderer2.setOpenIcon(RSC.Icons.getIcon("bullet_sparkle"));
        jTRegistrationTree.setCellRenderer(renderer2);
        RegistrationTreeModel = new DefaultTreeModel(new StructureNode("Rules"));
        RegistrationTreeModel.addTreeModelListener(this);
        jTRegistrationTree.setModel(RegistrationTreeModel);

        RSC.SM.register(this, "noDoc", new JComponent[] { jMDoc, jBtnApplyPluginSelDoc, jMICitationTagClipboard, jMIBibOutClipboard, jMIBibClipboard, jMICites, jMIDelReg, jMIFixReg});
        RSC.SM.register(this, "noLib", new JComponent[] { jMICloseLib, jMISaveLib, jMIDeleteLib, jMRegistration, jMIShowCitedinFile, jMISynch, jMICheckBib, jTFSearchCategories, jMIEditLib, jMICreateBib, jMIEditDS, jMIReloadDisplayString2, jMISynchER, jBtnEditRules, jMIAddToLib, jMIDeepSearch, jTBAdd, jTBSearch, jTFSearchAuthors, jTFSearchKey, jTFMainSearch, jBtnExpAll, jMIPluginInfo, jMIReloadPlugins1, jMIMPlugins1, jBMPlugins});
        RSC.SM.register(this, "noTab", new JComponent[] { jMICopyTab, jMICopyTab2, jMITab2Cat, jMITab2Cat2, jMICloseTab, jMICloseTab2});

        isLibSelected=false;
        isCatSelected=false;
        isDocSelected=false;
        isPlugSelected=false;
        isTabAvailable=false;
        adjustStates();


        final Image image = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("images/celsius.gif"));
        setIconImage(image);

   }

    public void loadShortCuts() {
        ShortCuts = new HashMap<String, String>();
        try {
            TextFile TD = new TextFile("celsius.shortcuts");
            String tmp;
            while (TD.ready()) {
                tmp = TD.getString();
                if (tmp.indexOf("::") > 0) {
                    ShortCuts.put(Parser.CutTill(tmp, "::").trim(), Parser.CutFrom(tmp, "::").trim());
                }
            }
            TD.close();
            TD = new TextFile("celsius.journallinks");
            while (TD.ready()) {
                tmp = TD.getString();
                if (tmp.indexOf("::") > 0) {
                    RSC.JournalLinks.put(Parser.CutTill(tmp, "::").trim(), Parser.CutFrom(tmp, "::").trim());
                }
            }
            TD.close();
        } catch (IOException ex) {
            Msg1.repS("MAIN>Error while loading shortcut/journallinks file:\n" + ex.toString());
            (new SafeMessage("Error while loading shortcut/journallinks file:\n" + ex.toString(), "Exception:", 0)).showMsg();
            RSC.Msg1.printStackTrace(ex);
        }
    }

    public void setShortCuts() {
        for (int i = 0; i < jMainMenu.getMenuCount(); i++) {
            JMenu jM = jMainMenu.getMenu(i);
            for (int j = 0; j < jM.getItemCount(); j++) {
                if (jM.getItem(j) == null) {
                    j++;
                }
                if (ShortCuts.containsKey(jM.getItem(j).getText())) {
                    jM.getItem(j).setAccelerator(KeyStroke.getKeyStroke(ShortCuts.get(jM.getItem(j).getText())));
                } 
            }
        }
    }

    /**
     * Terminate Program
     */
    private void FinishProgram() {
        for (int i = 0; i < RSC.Libraries.size(); i++) {
            if (RSC.Libraries.get(i).hasChanged()) {
                int h = toolbox.QuestionABC(this,"Modifications in library " + RSC.Libraries.get(i).name + "\nhave not been saved and will be lost.", "Warning:","Cancel","Save now","Ignore");
                if (h==0) return;
                if (h==1) {
                    try {
                        RSC.Libraries.get(i).writeBack();
                    } catch (IOException ex) {
                        RSC.Msg1.printStackTrace(ex);
                    }
                }
            }
        }
        RSC.closeResources();
        // Init-Daten schreiben
        dispose();
        System.exit(0);
    }

    public void addLib(final Library Lib) {
        JMenuItem jmi = new JMenuItem(Lib.name);
        jmi.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyDocToLib(Lib);
            }
        });
        jMCopyToDiff.add(jmi);
        jmi = new JMenuItem(Lib.name);
        jmi.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyDocToLib(Lib);
            }
        });

        jMCopyToDiff1.add(jmi);

        DefaultComboBoxModel DCBM=(DefaultComboBoxModel)jCBLibraries.getModel();
        DCBM.addElement(Lib.name);
        DCBM.setSelectedItem(Lib.name);
    }

    public void setSelectedLibrary(int i) {
        if ((i > -1) && (i < jCBLibraries.getItemCount())) {
            jCBLibraries.setSelectedIndex(i);
        }
    }
    
    /** 
     * Switch to library Lib, if Lib==null, then to the currently selected one.
     * @param i
     */
    public void switchToLibrary(Library Lib) {
        // Library already selected
        if ((RSC.getCurrentSelectedLibNo()>-1) && (RSC.getCurrentSelectedLibNo()<RSC.Libraries.size()))
            if (RSC.getCurrentSelectedLib()==Lib) return;
        // No library remaining
        if (RSC.Libraries.isEmpty()) {
            noLibSelected();
            return;
        }

        // switch to currently selected or other library
        if (Lib==null) RSC.currentLib=jCBLibraries.getSelectedIndex();
        else RSC.currentLib=RSC.Libraries.indexOf(Lib);
        
        if (RSC.currentLib==-1) {
            noLibSelected();
            return;
        }
        LibSelected();
        if (RSC.currentLib!=jCBLibraries.getSelectedIndex())
            jCBLibraries.setSelectedIndex(RSC.currentLib);
        StructureTreeModel.setRoot(RSC.getCurrentSelectedLib().Structure.Root);
        RegistrationTreeModel.setRoot(RSC.getCurrentSelectedLib().Rules.Root);
        updateStatusBar(true);
        RSC.Plugins.updatePlugins();
    }
    
    /**
     * Updates the statusbar, argument indicates, whether or not the document
     * content should be analyzed.
     */
    public void updateStatusBar(final boolean pagenumber) {
        if (RSC.currentLib==-1) {
            jLStatusBar.setText("No Library loaded.");
            return;
        }
        jLStatusBar.setText(RSC.getCurrentSelectedLib().Status(pagenumber));
    }


    /**
     * Update Table according to selected category
     */
    public void updateTableByCategory() {
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN == null) {
            return;
        }
        Library Lib=RSC.getCurrentSelectedLib();
        Lib.Structure.goTo(TN);
        String title = Lib.Structure.get("title").trim();
        ItemTable IT=RSC.makeTabAvailable(2, title, "folder_table");
        IT.resizeTable(true);
        jIP.setDocumentTable(IT);
        if (TN.isRoot()) title="::root";
        Lib.showItemsInCategory(title, IT);
        jIP.setupCatRemarks();
    }

    /**
     * Update Rules
     */
    public void updateRulesByCategory() {
        if (RSC.currentLib==-1) return;
        SRules = new XMLTree(RSC.getCurrentSelectedLib().Rules);
        if (jTStructureTree.getSelectionModel().isSelectionEmpty()) {
            RegistrationTreeModel.setRoot(SRules.Root);
            return;
        }
        String cat=RSC.getCurrentSelectedLib().Structure.get("title").trim();
        if (RSC.getCurrentSelectedLib().Structure.isRoot()) {
            // Return All Roots
            RegistrationTreeModel.setRoot(SRules.Root);
        } else {
            final ArrayList<StructureNode> ToDelete = new ArrayList<StructureNode>();
            for (final Enumeration enu = SRules.Root.children(); enu.hasMoreElements();) {
                SRules.goTo((StructureNode) enu.nextElement());
                if (SRules.name.equals("test")) {
                    boolean todel = true;
                    final StructureNode TMP = SRules.Node;
                    for (final Enumeration enu2 = SRules.Node.children(); enu2.hasMoreElements();) {
                        SRules.goTo((StructureNode) enu2.nextElement());
                        if (Parser.EnumContains(SRules.get("target"), cat)) {
                            todel = false;
                        }
                    }
                    SRules.goTo(TMP);
                    if (todel) {
                        ToDelete.add(SRules.Node);
                    }
                } else {
                    if (!Parser.EnumContains(SRules.get("target"), cat))
                        ToDelete.add(SRules.Node);
                }
            }
            for (StructureNode it : ToDelete) {
                SRules.goTo(it);
                SRules.DeleteNode();
            }
            RegistrationTreeModel.setRoot(SRules.Root);
        }
    }

    /**
     * Copies the currently selected documents to library Lib.
     * @param Lib
     */
    private void CopyDocToLib(Library Lib) {
        Library CSL = RSC.getCurrentSelectedLib();
        if (CSL != Lib) {
            for (Item doc : RSC.getCurrentItemTable().getSelectedItems()) {
                int i;
                try {
                    i = Lib.Doublette(doc);
                } catch (IOException ex) {
                    RSC.Msg1.printStackTrace(ex);
                    i = 12;
                }
                boolean add=true;
                if (i==12) {
                    add=false;
                    toolbox.Warning(this,"I/O Error while checking for doublettes.", "Error:");
                }
                if (i==10) {
                    int j=toolbox.QuestionOC(this,"An exact copy of the item "+doc.toText()+"\nalready exists in the library. Should another copy be added?", "Warning:");
                    if (j==JOptionPane.NO_OPTION) add=false;
                }
                if (i==4) {
                    int j=toolbox.QuestionOC(this,"A paper with the same key information as the item "+doc.toText()+"\nalready exists in the library. Should another copy be added?", "Warning:");
                    if (j==JOptionPane.NO_OPTION) add=false;
                }
                if (add) Lib.acquireCopyOfDocument(doc);
            }
        } else {
            toolbox.Warning(this,"Items can only be copied to different libraries.", "Warning!");
        }
    }
    
    //****************************** States ***********************************************
    public void adjustStates() {
        if (isLibSelected) {
            RSC.SM.switchOn(this, "noLib");
            jTStructureTree.setComponentPopupMenu(jPMCategories);
            if (isPlugSelected) jBTNApplyToAll.setEnabled(true);
            if (StructureTreeModel.getRoot()!=RSC.getCurrentSelectedLib().Structure.Root)
                StructureTreeModel.setRoot(RSC.getCurrentSelectedLib().Structure.Root);
            if (RegistrationTreeModel.getRoot()==null)
                RegistrationTreeModel.setRoot(RSC.getCurrentSelectedLib().Rules.Root);
        } else {
            RSC.SM.switchOff(this, "noLib");
            jTStructureTree.setComponentPopupMenu(null);
            jBTNApplyToAll.setEnabled(false);
            StructureTreeModel.setRoot(null);
            RegistrationTreeModel.setRoot(null);
            updateStatusBar(false);
            isDocSelected=false;
        }
        if (isCatSelected) {
            jMCategories.setEnabled(true);
            jMICreateLit.setEnabled(true);
        } else {
            jMCategories.setEnabled(false);
            jMICreateLit.setEnabled(false);
        }
        if (isDocSelected) {
            RSC.SM.switchOn(this, "noDoc");
            jBtnExpSel.setEnabled(true);
            if (isPlugSelected) jBtnApplyPluginSelDoc.setEnabled(true);
        } else {
            RSC.SM.switchOff(this, "noDoc");
            jBtnExpSel.setEnabled(false);
        }
        if (!isPlugSelected) {
            jBTNApplyToAll.setEnabled(false);
            jBtnApplyPluginSelDoc.setEnabled(false);
        }
        if (isTabAvailable) {
            RSC.SM.switchOn(this, "noTab");
        } else {
            RSC.SM.switchOff(this, "noTab");
        }
    }

    public void noLibSelected() {
        isLibSelected=false;
        adjustStates();
    }

    public void LibSelected() {
        if (RSC.getCurrentSelectedLib().Hide.contains("Menu:Bibliography")) {
            jMBibTeX.setVisible(false);
        } else {
            jMBibTeX.setVisible(true);
        }
        if (RSC.getCurrentSelectedLib().Hide.contains("MenuItem:Set my location to current item's location")) {
            jMIToCurrentLoc.setVisible(false);
            jMIToCurrentLoc1.setVisible(false);
        } else {
            jMIToCurrentLoc.setVisible(true);
            jMIToCurrentLoc1.setVisible(true);
        }
        isLibSelected=true;
        adjustStates();
    }

    public void noCatSelected() {
        isCatSelected=false;
        adjustStates();
    }

    public void CatSelected() {
        isCatSelected=true;
        adjustStates();
    }

    public void noDocSelected() {
        isDocSelected=false;
        adjustStates();
    }

    public void DocSelected() {
        isDocSelected=true;
        adjustStates();
    }

    public void noTabAvailable() {
        isTabAvailable=false;
        adjustStates();
    }

    public void TabAvailable() {
        isTabAvailable=true;
        adjustStates();
    }

    //*******************************States end

    /**
     * Set thread message
     */
    public void setThreadMsg(final String s) {
        jLThreadStatus.setText(s);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jDialog1 = new javax.swing.JDialog();
        jScrollBar1 = new javax.swing.JScrollBar();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPMDocList = new javax.swing.JPopupMenu();
        jMINew = new javax.swing.JMenuItem();
        jMICopyTab2 = new javax.swing.JMenuItem();
        jMITab2Cat2 = new javax.swing.JMenuItem();
        jMICloseTab2 = new javax.swing.JMenuItem();
        jPMCategories = new javax.swing.JPopupMenu();
        jMIOpenNewTab = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        jMIInsertCat1 = new javax.swing.JMenuItem();
        jMIRenameCat1 = new javax.swing.JMenuItem();
        jMIDelCat1 = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        jMICatUp1 = new javax.swing.JMenuItem();
        jMICatDown1 = new javax.swing.JMenuItem();
        jMICatSub1 = new javax.swing.JMenuItem();
        jMICatSuper1 = new javax.swing.JMenuItem();
        jSeparator31 = new javax.swing.JPopupMenu.Separator();
        jMIExpand = new javax.swing.JMenuItem();
        jMICollapse = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPMPlugins = new javax.swing.JPopupMenu();
        jMIPluginInfo = new javax.swing.JMenuItem();
        jMIMPlugins1 = new javax.swing.JMenuItem();
        jMIReloadPlugins1 = new javax.swing.JMenuItem();
        jPMDocuments = new javax.swing.JPopupMenu();
        jMIEditData1 = new javax.swing.JMenuItem();
        jMIView1 = new javax.swing.JMenuItem();
        jMActions = new javax.swing.JMenu();
        jMShow = new javax.swing.JMenu();
        jMShowCombined = new javax.swing.JMenuItem();
        jMShowLinked = new javax.swing.JMenuItem();
        jSeparator30 = new javax.swing.JPopupMenu.Separator();
        jMIViewPlain1 = new javax.swing.JMenuItem();
        jMIReExtract1 = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JSeparator();
        jMIRemoveFromTab1 = new javax.swing.JMenuItem();
        jMIUnregisterDoc1 = new javax.swing.JMenuItem();
        jMIDeleteFile1 = new javax.swing.JMenuItem();
        jMIRemoveHalf1 = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JSeparator();
        jMIFixReg1 = new javax.swing.JMenuItem();
        jMIDelReg1 = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JSeparator();
        jMICites1 = new javax.swing.JMenuItem();
        jSeparator24 = new javax.swing.JSeparator();
        jMIToCurrentLoc1 = new javax.swing.JMenuItem();
        jMIExportTab1 = new javax.swing.JMenuItem();
        jMIEmail1 = new javax.swing.JMenuItem();
        jMIAnnotatePDF1 = new javax.swing.JMenuItem();
        jMIOpenAnnotation1 = new javax.swing.JMenuItem();
        jSeparator27 = new javax.swing.JSeparator();
        jMIReduceDR1 = new javax.swing.JMenuItem();
        jMIAssociateFile1 = new javax.swing.JMenuItem();
        jMIJoin1 = new javax.swing.JMenuItem();
        jMICreateCombiner1 = new javax.swing.JMenuItem();
        jSeparator26 = new javax.swing.JSeparator();
        jMCopyToDiff1 = new javax.swing.JMenu();
        buttonGroup2 = new javax.swing.ButtonGroup();
        bGSearch = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jCBLibraries = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        jRBSearchDeep = new javax.swing.JRadioButton();
        jTFMainSearch = new celsius.jExtTextField();
        jRBSearchMeta = new javax.swing.JRadioButton();
        jRBSearchIndex = new javax.swing.JRadioButton();
        jPanel20 = new javax.swing.JPanel();
        jTBAdd = new javax.swing.JButton();
        jTBSearch = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLStatusBar = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPBSearch = new javax.swing.JProgressBar();
        jPanel11 = new javax.swing.JPanel();
        jLThreadStatus = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jSPMain3 = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        jTPSearches = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTStructureTree = new javax.swing.JTree();
        jBtnClrSrchCat = new javax.swing.JButton();
        jTFSearchCategories = new celsius.jExtTextField();
        jPanel14 = new javax.swing.JPanel();
        jBtnClrSrchAuthors = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLSearchPeople = new javax.swing.JList();
        jScrollPane13 = new javax.swing.JScrollPane();
        jLPeopleLong = new javax.swing.JList();
        jTFSearchAuthors = new celsius.jExtTextField();
        jPanel9 = new javax.swing.JPanel();
        jBtnClrSrchKey = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jLSearchKeys = new javax.swing.JList();
        jTFSearchKey = new celsius.jExtTextField();
        jTPTechnical = new javax.swing.JTabbedPane();
        jPanel17 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jBMPlugins = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jLPlugins = new javax.swing.JList();
        jBtnApplyPluginSelDoc = new javax.swing.JButton();
        jBTNApplyToAll = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTRegistrationTree = new javax.swing.JTree();
        jBtnEditRules = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jCBExpFilter = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jRBExpFile = new javax.swing.JRadioButton();
        jRBExpClip = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jBtnExpSel = new javax.swing.JButton();
        jBtnExpAll = new javax.swing.JButton();
        jTFExpFile = new javax.swing.JTextField();
        jBtnSelExpFile = new javax.swing.JButton();
        jPanel21 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jSPMain = new javax.swing.JSplitPane();
        jTPTabList = new javax.swing.JTabbedPane();
        jMainMenu = new javax.swing.JMenuBar();
        jMFile = new javax.swing.JMenu();
        jMIAddTab = new javax.swing.JMenuItem();
        jMICopyTab = new javax.swing.JMenuItem();
        jMITab2Cat = new javax.swing.JMenuItem();
        jMICloseTab = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMIConfig = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        jMIQuit = new javax.swing.JMenuItem();
        jMLibraries = new javax.swing.JMenu();
        jMIEditLib = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        jMICreateLib = new javax.swing.JMenuItem();
        jMILoadLib = new javax.swing.JMenuItem();
        jMRecent = new javax.swing.JMenu();
        jMISaveLib = new javax.swing.JMenuItem();
        jMICloseLib = new javax.swing.JMenuItem();
        jMIDeleteLib = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMIAddToLib = new javax.swing.JMenuItem();
        jSeparator28 = new javax.swing.JSeparator();
        jMIDeepSearch = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMIReloadDisplayString2 = new javax.swing.JMenuItem();
        jMIEditDS = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
        jMCDisplayHidden = new javax.swing.JRadioButtonMenuItem();
        jMDoc = new javax.swing.JMenu();
        JMEditData = new javax.swing.JMenuItem();
        jMIView = new javax.swing.JMenuItem();
        jMShow1 = new javax.swing.JMenu();
        jMShowCombined1 = new javax.swing.JMenuItem();
        jMShowLinked1 = new javax.swing.JMenuItem();
        jMIViewPlain = new javax.swing.JMenuItem();
        jMICreateTxt = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMIRemoveFromTab = new javax.swing.JMenuItem();
        jMIUnregisterDoc = new javax.swing.JMenuItem();
        jMIDeleteFile = new javax.swing.JMenuItem();
        jMIRemoveHalf = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMIToCurrentLoc = new javax.swing.JMenuItem();
        jMIExportTab = new javax.swing.JMenuItem();
        jMIEmail = new javax.swing.JMenuItem();
        jMIAnnotatePDF = new javax.swing.JMenuItem();
        jMIOpenAnnotation = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JSeparator();
        jMIReduceDR = new javax.swing.JMenuItem();
        jMIAssociateFile = new javax.swing.JMenuItem();
        jMIJoin = new javax.swing.JMenuItem();
        jMICreateCombiner = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMCopyToDiff = new javax.swing.JMenu();
        jMCategories = new javax.swing.JMenu();
        jMIInsertCat = new javax.swing.JMenuItem();
        jMIRenameCat = new javax.swing.JMenuItem();
        jMIDelCat = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        jMICatUp = new javax.swing.JMenuItem();
        jMICatDown = new javax.swing.JMenuItem();
        jMICatSub = new javax.swing.JMenuItem();
        jMICatSuper = new javax.swing.JMenuItem();
        jSeparator32 = new javax.swing.JPopupMenu.Separator();
        jMIEditCatTree = new javax.swing.JMenuItem();
        jMRegistration = new javax.swing.JMenu();
        jMIAutoregister = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        jMIDelAllAutoReg = new javax.swing.JMenuItem();
        jMIDelAllReg = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JSeparator();
        jMIFixReg = new javax.swing.JMenuItem();
        jMIDelReg = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMICites = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMIEditRules = new javax.swing.JMenuItem();
        jMTools = new javax.swing.JMenu();
        jMISynch = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        jMISetupER = new javax.swing.JMenuItem();
        jMISynchER = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        jMIEditLocation = new javax.swing.JMenuItem();
        jSeparator29 = new javax.swing.JSeparator();
        jMIEditLibTemplates = new javax.swing.JMenuItem();
        jMBibTeX = new javax.swing.JMenu();
        jMICitationTagClipboard = new javax.swing.JMenuItem();
        jMIBibClipboard = new javax.swing.JMenuItem();
        jMIBibOutClipboard = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        jMIShowCitedinFile = new javax.swing.JMenuItem();
        jMICreateBib = new javax.swing.JMenuItem();
        jMICheckBib = new javax.swing.JMenuItem();
        jMICreateLit = new javax.swing.JMenuItem();
        jMHelp = new javax.swing.JMenu();
        JMIManual = new javax.swing.JMenuItem();
        jMIUpdate = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMIAbout = new javax.swing.JMenuItem();

        jPasswordField1.setText("jPasswordField1");

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(jScrollBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(296, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                .addContainerGap(208, Short.MAX_VALUE)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(83, 83, 83))
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(jScrollBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(145, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                .addContainerGap(155, Short.MAX_VALUE)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(126, 126, 126))
        );

        jMINew.setText("New Tab");
        jMINew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMINewActionPerformed(evt);
            }
        });
        jPMDocList.add(jMINew);

        jMICopyTab2.setText("Copy Tab");
        jMICopyTab2.setEnabled(false);
        jMICopyTab2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICopyTabActionPerformed(evt);
            }
        });
        jPMDocList.add(jMICopyTab2);

        jMITab2Cat2.setText("Create Category from Tab");
        jMITab2Cat2.setEnabled(false);
        jMITab2Cat2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMITab2Cat2ActionPerformed(evt);
            }
        });
        jPMDocList.add(jMITab2Cat2);

        jMICloseTab2.setText("Close Tab");
        jMICloseTab2.setEnabled(false);
        jMICloseTab2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICloseTab2TabActionPerformed(evt);
            }
        });
        jPMDocList.add(jMICloseTab2);

        jPMCategories.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPMCategoriesPropertyChange(evt);
            }
        });

        jMIOpenNewTab.setText("Open Category in New Tab");
        jMIOpenNewTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIOpenNewTabActionPerformed(evt);
            }
        });
        jPMCategories.add(jMIOpenNewTab);
        jPMCategories.add(jSeparator16);

        jMIInsertCat1.setText("Insert Subcategory");
        jMIInsertCat1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIInsertCatActionPerformed(evt);
            }
        });
        jPMCategories.add(jMIInsertCat1);

        jMIRenameCat1.setText("Rename Category");
        jMIRenameCat1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIRenameCatActionPerformed(evt);
            }
        });
        jPMCategories.add(jMIRenameCat1);

        jMIDelCat1.setText("Delete Category");
        jMIDelCat1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDelCatActionPerformed(evt);
            }
        });
        jPMCategories.add(jMIDelCat1);
        jPMCategories.add(jSeparator15);

        jMICatUp1.setText("Move up");
        jMICatUp1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatUpActionPerformed(evt);
            }
        });
        jPMCategories.add(jMICatUp1);

        jMICatDown1.setText("Move down");
        jMICatDown1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatDownActionPerformed(evt);
            }
        });
        jPMCategories.add(jMICatDown1);

        jMICatSub1.setText("Turn into subcategory of above");
        jMICatSub1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatSubActionPerformed(evt);
            }
        });
        jPMCategories.add(jMICatSub1);

        jMICatSuper1.setText("Turn into supercategory");
        jMICatSuper1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatSuperActionPerformed(evt);
            }
        });
        jPMCategories.add(jMICatSuper1);
        jPMCategories.add(jSeparator31);

        jMIExpand.setText("Expand tree");
        jMIExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIExpandActionPerformed(evt);
            }
        });
        jPMCategories.add(jMIExpand);

        jMICollapse.setText("Collapse tree");
        jMICollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICollapseActionPerformed(evt);
            }
        });
        jPMCategories.add(jMICollapse);

        jMIPluginInfo.setText("Help for selected plugin");
        jMIPluginInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIPluginInfoActionPerformed(evt);
            }
        });
        jPMPlugins.add(jMIPluginInfo);

        jMIMPlugins1.setText("Manage plugins");
        jMIMPlugins1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIMPluginsActionPerformed(evt);
            }
        });
        jPMPlugins.add(jMIMPlugins1);

        jMIReloadPlugins1.setText("Reload plugins");
        jMIReloadPlugins1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIReloadPluginsActionPerformed(evt);
            }
        });
        jPMPlugins.add(jMIReloadPlugins1);

        jMIEditData1.setText("Edit");
        jMIEditData1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JMEditDataActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIEditData1);

        jMIView1.setText("Open selected item");
        jMIView1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIViewActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIView1);

        jMActions.setText("Action");
        jPMDocuments.add(jMActions);

        jMShow.setText("Show");

        jMShowCombined.setText("Show Combined");
        jMShowCombined.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMShowCombinedActionPerformed(evt);
            }
        });
        jMShow.add(jMShowCombined);

        jMShowLinked.setText("Show Linked");
        jMShowLinked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMShowLinkedActionPerformed(evt);
            }
        });
        jMShow.add(jMShowLinked);

        jPMDocuments.add(jMShow);
        jPMDocuments.add(jSeparator30);

        jMIViewPlain1.setText("View plain text");
        jMIViewPlain1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIViewPlainActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIViewPlain1);

        jMIReExtract1.setText("Re-extract plain text");
        jMIReExtract1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateTxtActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIReExtract1);
        jPMDocuments.add(jSeparator20);

        jMIRemoveFromTab1.setText("Remove from current table");
        jMIRemoveFromTab1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIRemoveFromTabActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIRemoveFromTab1);

        jMIUnregisterDoc1.setText("Remove from current category");
        jMIUnregisterDoc1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIUnregisterDocActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIUnregisterDoc1);

        jMIDeleteFile1.setText("Remove from library and delete files");
        jMIDeleteFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDeleteFileActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIDeleteFile1);

        jMIRemoveHalf1.setText("Remove from library and keep files");
        jMIRemoveHalf1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIRemoveHalfActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIRemoveHalf1);
        jPMDocuments.add(jSeparator21);

        jMIFixReg1.setText("Fix registration of selected items");
        jMIFixReg1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIFixRegActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIFixReg1);

        jMIDelReg1.setText("Delete autoregistration of selected items");
        jMIDelReg1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDelRegActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIDelReg1);
        jPMDocuments.add(jSeparator22);

        jMICites1.setText("Add rule \"cites\"");
        jMICites1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICitesActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMICites1);
        jPMDocuments.add(jSeparator24);

        jMIToCurrentLoc1.setText("Set my location to current item's location");
        jMIToCurrentLoc1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIToCurrentLocActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIToCurrentLoc1);

        jMIExportTab1.setText("Export files of selected items");
        jMIExportTab1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIExportTabActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIExportTab1);

        jMIEmail1.setText("Send selected items in email");
        jMIEmail1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEmailActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIEmail1);

        jMIAnnotatePDF1.setText("Annotate PDF");
        jMIAnnotatePDF1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAnnotatePDFActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIAnnotatePDF1);

        jMIOpenAnnotation1.setText("Open annotated file");
        jMIOpenAnnotation1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIOpenAnnotationActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIOpenAnnotation1);
        jPMDocuments.add(jSeparator27);

        jMIReduceDR1.setText("Reduce to item reference");
        jMIReduceDR1.setToolTipText("Delete the associated file");
        jMIReduceDR1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIReduceDRActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIReduceDR1);

        jMIAssociateFile1.setText("Associate file to current entry");
        jMIAssociateFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAssociateFileActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIAssociateFile1);

        jMIJoin1.setText("Combine the selected items");
        jMIJoin1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIJoinActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMIJoin1);

        jMICreateCombiner1.setText("Create a combining item");
        jMICreateCombiner1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateCombiner1ActionPerformed(evt);
            }
        });
        jPMDocuments.add(jMICreateCombiner1);
        jPMDocuments.add(jSeparator26);

        jMCopyToDiff1.setText("Copy to library");
        jPMDocuments.add(jMCopyToDiff1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Celsius Library System v2.0");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 10, 1, 10));
        jPanel5.setLayout(new java.awt.BorderLayout(100, 0));

        jPanel7.setPreferredSize(new java.awt.Dimension(RSC.guiScale(180), RSC.guiScale(49)));

        jCBLibraries.setPreferredSize(new java.awt.Dimension(RSC.guiScale(202), RSC.guiScale(26)));
        jCBLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBLibrariesActionPerformed(evt);
            }
        });

        jLabel1.setText("Active Library:");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jCBLibraries, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBLibraries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel5.add(jPanel7, java.awt.BorderLayout.WEST);

        jPanel19.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jPanel19.setLayout(new java.awt.GridBagLayout());

        bGSearch.add(jRBSearchDeep);
        jRBSearchDeep.setText("Deep search");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 8, 0);
        jPanel19.add(jRBSearchDeep, gridBagConstraints);

        jTFMainSearch.setDefaultText("Enter a search string (ctrl+f)");
        jTFMainSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTFMainSearchKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        jPanel19.add(jTFMainSearch, gridBagConstraints);

        bGSearch.add(jRBSearchMeta);
        jRBSearchMeta.setText("Metadata");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 8, 0);
        jPanel19.add(jRBSearchMeta, gridBagConstraints);

        bGSearch.add(jRBSearchIndex);
        jRBSearchIndex.setSelected(true);
        jRBSearchIndex.setText("Index");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(2, 51, 8, 0);
        jPanel19.add(jRBSearchIndex, gridBagConstraints);

        jPanel5.add(jPanel19, java.awt.BorderLayout.CENTER);

        jPanel20.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jTBAdd.setIcon(RSC.getScaledIcon("Add Icon"));
        jTBAdd.setToolTipText("Add items to current library");
        jTBAdd.setPreferredSize(new java.awt.Dimension(RSC.guiScale(42), RSC.guiScale(42)));
        jTBAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTBAddActionPerformed(evt);
            }
        });
        jPanel20.add(jTBAdd);

        jTBSearch.setIcon(RSC.getScaledIcon("search2"));
        jTBSearch.setToolTipText("Extended search");
        jTBSearch.setPreferredSize(new java.awt.Dimension(RSC.guiScale(42), RSC.guiScale(42)));
        jTBSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTBSearchActionPerformed(evt);
            }
        });
        jPanel20.add(jTBSearch);

        jPanel5.add(jPanel20, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel1.setToolTipText("Status-Bar");
        jPanel1.setPreferredSize(new java.awt.Dimension(RSC.guiScale(100), RSC.guiScale(20)));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setPreferredSize(new java.awt.Dimension(RSC.guiScale(200), RSC.guiScale(100)));
        jPanel10.setLayout(new java.awt.BorderLayout());

        jLStatusBar.setText("Status-Bar:");
        jLStatusBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 0));
        jPanel10.add(jLStatusBar, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel12.setLayout(new java.awt.GridLayout(1, 0));

        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPBSearch.setBorder(null);
        jPBSearch.setMinimumSize(new java.awt.Dimension(10, 6));
        jPBSearch.setPreferredSize(new java.awt.Dimension(RSC.guiScale(146), RSC.guiScale(6)));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 208, Short.MAX_VALUE)
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPBSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPBSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 6, Short.MAX_VALUE))
        );

        jPanel12.add(jPanel13);

        jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel11.setPreferredSize(new java.awt.Dimension(RSC.guiScale(184), RSC.guiScale(100)));
        jPanel11.setLayout(new java.awt.BorderLayout());

        jLThreadStatus.setText("Threads: all ended");
        jLThreadStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 0));
        jLThreadStatus.setMinimumSize(new java.awt.Dimension(180, 15));
        jPanel11.add(jLThreadStatus, java.awt.BorderLayout.CENTER);

        jPanel12.add(jPanel11);

        jPanel1.add(jPanel12, java.awt.BorderLayout.EAST);

        jPanel6.add(jPanel1, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setOneTouchExpandable(true);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jSPMain3.setDividerLocation(300);
        jSPMain3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSPMain3.setResizeWeight(1.0);
        jSPMain3.setMinimumSize(new java.awt.Dimension(256, 321));
        jSPMain3.setOneTouchExpandable(true);
        jSPMain3.setPreferredSize(new java.awt.Dimension(RSC.guiScale(256), RSC.guiScale(948)));

        jPanel8.setPreferredSize(new java.awt.Dimension(RSC.guiScale(205), RSC.guiScale(427)));
        jPanel8.setLayout(new java.awt.GridLayout(1, 0));

        jTPSearches.setPreferredSize(new java.awt.Dimension(204, 186));

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setPreferredSize(new java.awt.Dimension(RSC.guiScale(200), RSC.guiScale(600)));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("JTree");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("colors");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("blue");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("violet");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("red");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("yellow");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("sports");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("basketball");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        jTStructureTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTStructureTree.setComponentPopupMenu(jPMCategories);
        jTStructureTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTStructureTreeMouseClicked(evt);
            }
        });
        jTStructureTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTStructureTreeValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jTStructureTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 98;
        gridBagConstraints.ipady = 206;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 5, 5);
        jPanel3.add(jScrollPane3, gridBagConstraints);

        jBtnClrSrchCat.setIcon(RSC.getScaledIcon("closebtn"));
        jBtnClrSrchCat.setBorderPainted(false);
        jBtnClrSrchCat.setContentAreaFilled(false);
        jBtnClrSrchCat.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jBtnClrSrchCat.setPreferredSize(new java.awt.Dimension(RSC.guiScale(16), RSC.guiScale(17)));
        jBtnClrSrchCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClrSrchCatActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 5);
        jPanel3.add(jBtnClrSrchCat, gridBagConstraints);

        jTFSearchCategories.setDefaultText("Enter a category here");
        jTFSearchCategories.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTFSearchCategoriesKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 84;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 0);
        jPanel3.add(jTFSearchCategories, gridBagConstraints);

        jTPSearches.addTab("", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/folder_explore.png")), jPanel3); // NOI18N

        jPanel14.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel14.setPreferredSize(new java.awt.Dimension(RSC.guiScale(0), RSC.guiScale(500)));
        jPanel14.setLayout(new java.awt.GridBagLayout());

        jBtnClrSrchAuthors.setIcon(RSC.getScaledIcon("closebtn"));
        jBtnClrSrchAuthors.setBorderPainted(false);
        jBtnClrSrchAuthors.setContentAreaFilled(false);
        jBtnClrSrchAuthors.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jBtnClrSrchAuthors.setPreferredSize(new java.awt.Dimension(RSC.guiScale(16), RSC.guiScale(17)));
        jBtnClrSrchAuthors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClrSrchAuthorsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 5);
        jPanel14.add(jBtnClrSrchAuthors, gridBagConstraints);

        jPanel16.setLayout(new java.awt.GridLayout(1, 0, 10, 10));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jLSearchPeople.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jLSearchPeople.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLSearchPeopleMouseClicked(evt);
            }
        });
        jLSearchPeople.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jLSearchPeopleValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jLSearchPeople);

        jPanel16.add(jScrollPane1);

        jLPeopleLong.setPreferredSize(new java.awt.Dimension(RSC.guiScale(80), RSC.guiScale(0)));
        jLPeopleLong.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLPeopleLongMouseClicked(evt);
            }
        });
        jLPeopleLong.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jLPeopleLongValueChanged(evt);
            }
        });
        jScrollPane13.setViewportView(jLPeopleLong);

        jPanel16.add(jScrollPane13);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 132;
        gridBagConstraints.ipady = 293;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 5, 5);
        jPanel14.add(jPanel16, gridBagConstraints);

        jTFSearchAuthors.setDefaultText("Enter a person here (ctrl+p)");
        jTFSearchAuthors.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTFSearchAuthorsKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 0);
        jPanel14.add(jTFSearchAuthors, gridBagConstraints);

        jTPSearches.addTab("", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/user_b.png")), jPanel14); // NOI18N

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel9.setPreferredSize(new java.awt.Dimension(200, 157));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        jBtnClrSrchKey.setIcon(RSC.getScaledIcon("closebtn"));
        jBtnClrSrchKey.setBorderPainted(false);
        jBtnClrSrchKey.setContentAreaFilled(false);
        jBtnClrSrchKey.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jBtnClrSrchKey.setPreferredSize(new java.awt.Dimension(RSC.guiScale(16), RSC.guiScale(17)));
        jBtnClrSrchKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClrSrchKeyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 5);
        jPanel9.add(jBtnClrSrchKey, gridBagConstraints);

        jScrollPane7.setPreferredSize(new java.awt.Dimension(RSC.guiScale(259), RSC.guiScale(31)));

        jLSearchKeys.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jLSearchKeys.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLSearchKeysMouseClicked(evt);
            }
        });
        jLSearchKeys.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jLSearchKeysValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(jLSearchKeys);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 168;
        gridBagConstraints.ipady = 93;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 5, 5);
        jPanel9.add(jScrollPane7, gridBagConstraints);

        jTFSearchKey.setDefaultText("Enter a keyword");
        jTFSearchKey.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTFSearchKeyKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 154;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 0);
        jPanel9.add(jTFSearchKey, gridBagConstraints);

        jTPSearches.addTab("", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/book_key.png")), jPanel9); // NOI18N

        jPanel8.add(jTPSearches);

        jSPMain3.setTopComponent(jPanel8);

        jPanel17.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel17.setMinimumSize(new java.awt.Dimension(100, 256));
        jPanel17.setPreferredSize(new java.awt.Dimension(100, 409));
        jPanel17.setLayout(new java.awt.GridBagLayout());

        jLabel9.setText("Plugins:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        jPanel17.add(jLabel9, gridBagConstraints);

        jBMPlugins.setText("manage");
        jBMPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIMPluginsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 0, 5);
        jPanel17.add(jBMPlugins, gridBagConstraints);

        jLPlugins.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jLPlugins.setComponentPopupMenu(jPMPlugins);
        jLPlugins.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jLPluginsValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(jLPlugins);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 216;
        gridBagConstraints.ipady = 318;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 0, 5);
        jPanel17.add(jScrollPane5, gridBagConstraints);

        jBtnApplyPluginSelDoc.setMnemonic('s');
        jBtnApplyPluginSelDoc.setText("selected");
        jBtnApplyPluginSelDoc.setEnabled(false);
        jBtnApplyPluginSelDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyPluginSelDocActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 5, 0);
        jPanel17.add(jBtnApplyPluginSelDoc, gridBagConstraints);

        jBTNApplyToAll.setText("all docs");
        jBTNApplyToAll.setEnabled(false);
        jBTNApplyToAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBTNApplyToAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        jPanel17.add(jBTNApplyToAll, gridBagConstraints);

        jLabel12.setText("Apply to:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 0, 0);
        jPanel17.add(jLabel12, gridBagConstraints);

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 1.0;
        jPanel17.add(jPanel22, gridBagConstraints);

        jTPTechnical.addTab("tab1", jPanel17);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setMinimumSize(new java.awt.Dimension(0, RSC.guiScale(250)));
        jPanel4.setPreferredSize(new java.awt.Dimension(100, 417));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("Registration Rules:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 0);
        jPanel4.add(jLabel8, gridBagConstraints);

        jScrollPane4.setViewportView(jTRegistrationTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 218;
        gridBagConstraints.ipady = 359;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 5, 5);
        jPanel4.add(jScrollPane4, gridBagConstraints);

        jBtnEditRules.setText("edit");
        jBtnEditRules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditRulesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 83, 0, 5);
        jPanel4.add(jBtnEditRules, gridBagConstraints);

        jTPTechnical.addTab("tab2", jPanel4);

        jPanel18.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel18.setMinimumSize(new java.awt.Dimension(100, 115));
        jPanel18.setPreferredSize(new java.awt.Dimension(200, 83));
        java.awt.GridBagLayout jPanel18Layout = new java.awt.GridBagLayout();
        jPanel18Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        jPanel18Layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel18.setLayout(jPanel18Layout);

        jLabel3.setText("Export:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        jPanel18.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        jPanel18.add(jCBExpFilter, gridBagConstraints);

        jLabel6.setText("Target:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jPanel18.add(jLabel6, gridBagConstraints);

        buttonGroup3.add(jRBExpFile);
        jRBExpFile.setSelected(true);
        jRBExpFile.setText("File");
        jRBExpFile.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRBExpFileItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel18.add(jRBExpFile, gridBagConstraints);

        buttonGroup3.add(jRBExpClip);
        jRBExpClip.setText("Clipboard");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel18.add(jRBExpClip, gridBagConstraints);

        jLabel5.setText("Export");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        jPanel18.add(jLabel5, gridBagConstraints);

        jBtnExpSel.setText("selected");
        jBtnExpSel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnExpSelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        jPanel18.add(jBtnExpSel, gridBagConstraints);

        jBtnExpAll.setText("all docs");
        jBtnExpAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnExpAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel18.add(jBtnExpAll, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.7;
        jPanel18.add(jTFExpFile, gridBagConstraints);

        jBtnSelExpFile.setText("Choose");
        jBtnSelExpFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelExpFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        jPanel18.add(jBtnSelExpFile, gridBagConstraints);

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 239, Short.MAX_VALUE)
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel18.add(jPanel21, gridBagConstraints);

        jTPTechnical.addTab("tab3", jPanel18);

        jSPMain3.setRightComponent(jTPTechnical);

        jPanel2.add(jSPMain3);

        jSplitPane1.setLeftComponent(jPanel2);

        jPanel15.setLayout(new java.awt.GridLayout(1, 0));

        jSPMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSPMain.setMinimumSize(new java.awt.Dimension(300, 37));
        jSPMain.setOneTouchExpandable(true);

        jTPTabList.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTPTabList.setComponentPopupMenu(jPMDocList);
        jTPTabList.setMinimumSize(new java.awt.Dimension(0, 0));
        jTPTabList.setOpaque(true);
        jTPTabList.setPreferredSize(new java.awt.Dimension(300, 600));
        jTPTabList.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTPTabListStateChanged(evt);
            }
        });
        jTPTabList.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jTPTabListComponentResized(evt);
            }
        });
        jSPMain.setTopComponent(jTPTabList);

        jPanel15.add(jSPMain);

        jSplitPane1.setRightComponent(jPanel15);

        jPanel6.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel6, java.awt.BorderLayout.CENTER);

        jMFile.setText("File");

        jMIAddTab.setText("New tab");
        jMIAddTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAddTabActionPerformed(evt);
            }
        });
        jMFile.add(jMIAddTab);

        jMICopyTab.setText("Copy tab");
        jMICopyTab.setEnabled(false);
        jMICopyTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICopyTabActionPerformed(evt);
            }
        });
        jMFile.add(jMICopyTab);

        jMITab2Cat.setText("Create category from tab");
        jMITab2Cat.setEnabled(false);
        jMITab2Cat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMITab2Cat2ActionPerformed(evt);
            }
        });
        jMFile.add(jMITab2Cat);

        jMICloseTab.setText("Close tab");
        jMICloseTab.setEnabled(false);
        jMICloseTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICloseTab2TabActionPerformed(evt);
            }
        });
        jMFile.add(jMICloseTab);
        jMFile.add(jSeparator5);

        jMIConfig.setText("Configuration");
        jMIConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIConfigActionPerformed(evt);
            }
        });
        jMFile.add(jMIConfig);
        jMFile.add(jSeparator10);

        jMIQuit.setText("Quit");
        jMIQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIQuitActionPerformed(evt);
            }
        });
        jMFile.add(jMIQuit);

        jMainMenu.add(jMFile);

        jMLibraries.setText("Libraries");

        jMIEditLib.setText("Edit library properties");
        jMIEditLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEditLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMIEditLib);
        jMLibraries.add(jSeparator11);

        jMICreateLib.setText("Create new library");
        jMICreateLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMICreateLib);

        jMILoadLib.setText("Load library");
        jMILoadLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMILoadLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMILoadLib);

        jMRecent.setText("Open recent libraries");
        jMLibraries.add(jMRecent);

        jMISaveLib.setText("Save current library");
        jMISaveLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMISaveLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMISaveLib);

        jMICloseLib.setText("Close current library");
        jMICloseLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICloseLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMICloseLib);

        jMIDeleteLib.setText("Delete current library");
        jMIDeleteLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDeleteLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMIDeleteLib);
        jMLibraries.add(jSeparator2);

        jMIAddToLib.setText("Add items to library");
        jMIAddToLib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAddToLibActionPerformed(evt);
            }
        });
        jMLibraries.add(jMIAddToLib);
        jMLibraries.add(jSeparator28);

        jMIDeepSearch.setText("Search in Library");
        jMIDeepSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDeepSearchActionPerformed(evt);
            }
        });
        jMLibraries.add(jMIDeepSearch);
        jMLibraries.add(jSeparator4);

        jMIReloadDisplayString2.setText("Reload HTML template");
        jMIReloadDisplayString2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIReloadDisplayStringActionPerformed(evt);
            }
        });
        jMLibraries.add(jMIReloadDisplayString2);

        jMIEditDS.setText("Edit HTML template");
        jMIEditDS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEditDSActionPerformed(evt);
            }
        });
        jMLibraries.add(jMIEditDS);
        jMLibraries.add(jSeparator23);

        jMCDisplayHidden.setText("Show hidden items");
        jMCDisplayHidden.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jMCDisplayHiddenStateChanged(evt);
            }
        });
        jMLibraries.add(jMCDisplayHidden);

        jMainMenu.add(jMLibraries);

        jMDoc.setText("Items");

        JMEditData.setText("Edit");
        JMEditData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JMEditDataActionPerformed(evt);
            }
        });
        jMDoc.add(JMEditData);

        jMIView.setText("Open selected item");
        jMIView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIViewActionPerformed(evt);
            }
        });
        jMDoc.add(jMIView);

        jMShow1.setText("Show");

        jMShowCombined1.setText("Show Combined");
        jMShowCombined1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMShowCombinedActionPerformed(evt);
            }
        });
        jMShow1.add(jMShowCombined1);

        jMShowLinked1.setText("Show Linked");
        jMShowLinked1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMShowLinkedActionPerformed(evt);
            }
        });
        jMShow1.add(jMShowLinked1);

        jMDoc.add(jMShow1);

        jMIViewPlain.setText("View plain text");
        jMIViewPlain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIViewPlainActionPerformed(evt);
            }
        });
        jMDoc.add(jMIViewPlain);

        jMICreateTxt.setText("Re-extract plain text ");
        jMICreateTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateTxtActionPerformed(evt);
            }
        });
        jMDoc.add(jMICreateTxt);
        jMDoc.add(jSeparator3);

        jMIRemoveFromTab.setText("Remove from current table");
        jMIRemoveFromTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIRemoveFromTabActionPerformed(evt);
            }
        });
        jMDoc.add(jMIRemoveFromTab);

        jMIUnregisterDoc.setText("Remove from current category");
        jMIUnregisterDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIUnregisterDocActionPerformed(evt);
            }
        });
        jMDoc.add(jMIUnregisterDoc);

        jMIDeleteFile.setText("Remove from library and delete files");
        jMIDeleteFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDeleteFileActionPerformed(evt);
            }
        });
        jMDoc.add(jMIDeleteFile);

        jMIRemoveHalf.setText("Remove from library but keep files");
        jMIRemoveHalf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIRemoveHalfActionPerformed(evt);
            }
        });
        jMDoc.add(jMIRemoveHalf);
        jMDoc.add(jSeparator6);

        jMIToCurrentLoc.setText("Set my location to current item's location");
        jMIToCurrentLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIToCurrentLocActionPerformed(evt);
            }
        });
        jMDoc.add(jMIToCurrentLoc);

        jMIExportTab.setText("Export files of selected items");
        jMIExportTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIExportTabActionPerformed(evt);
            }
        });
        jMDoc.add(jMIExportTab);

        jMIEmail.setText("Send selected items in email");
        jMIEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEmailActionPerformed(evt);
            }
        });
        jMDoc.add(jMIEmail);

        jMIAnnotatePDF.setText("Annotate PDF");
        jMIAnnotatePDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAnnotatePDFActionPerformed(evt);
            }
        });
        jMDoc.add(jMIAnnotatePDF);

        jMIOpenAnnotation.setText("Open annotated file");
        jMIOpenAnnotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIOpenAnnotationActionPerformed(evt);
            }
        });
        jMDoc.add(jMIOpenAnnotation);
        jMDoc.add(jSeparator25);

        jMIReduceDR.setText("Reduce to item reference");
        jMIReduceDR.setToolTipText("Delete the associated file");
        jMIReduceDR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIReduceDRActionPerformed(evt);
            }
        });
        jMDoc.add(jMIReduceDR);

        jMIAssociateFile.setText("Associate file to current entry");
        jMIAssociateFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAssociateFileActionPerformed(evt);
            }
        });
        jMDoc.add(jMIAssociateFile);

        jMIJoin.setText("Combine the selected items");
        jMIJoin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIJoinActionPerformed(evt);
            }
        });
        jMDoc.add(jMIJoin);

        jMICreateCombiner.setText("Create a combining item");
        jMICreateCombiner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateCombiner1ActionPerformed(evt);
            }
        });
        jMDoc.add(jMICreateCombiner);
        jMDoc.add(jSeparator8);

        jMCopyToDiff.setText("Copy to library");
        jMDoc.add(jMCopyToDiff);

        jMainMenu.add(jMDoc);

        jMCategories.setText("Categories");

        jMIInsertCat.setText("Insert subcategory");
        jMIInsertCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIInsertCatActionPerformed(evt);
            }
        });
        jMCategories.add(jMIInsertCat);

        jMIRenameCat.setText("Rename category");
        jMIRenameCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIRenameCatActionPerformed(evt);
            }
        });
        jMCategories.add(jMIRenameCat);

        jMIDelCat.setText("Delete category");
        jMIDelCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDelCatActionPerformed(evt);
            }
        });
        jMCategories.add(jMIDelCat);
        jMCategories.add(jSeparator14);

        jMICatUp.setText("Move up");
        jMICatUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatUpActionPerformed(evt);
            }
        });
        jMCategories.add(jMICatUp);

        jMICatDown.setText("Move down");
        jMICatDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatDownActionPerformed(evt);
            }
        });
        jMCategories.add(jMICatDown);

        jMICatSub.setText("Turn into subcategory of above");
        jMICatSub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatSubActionPerformed(evt);
            }
        });
        jMCategories.add(jMICatSub);

        jMICatSuper.setText("Turn into supercategory");
        jMICatSuper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICatSuperActionPerformed(evt);
            }
        });
        jMCategories.add(jMICatSuper);
        jMCategories.add(jSeparator32);

        jMIEditCatTree.setText("Edit category tree");
        jMIEditCatTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEditCatTreeActionPerformed(evt);
            }
        });
        jMCategories.add(jMIEditCatTree);

        jMainMenu.add(jMCategories);

        jMRegistration.setText("Registration");

        jMIAutoregister.setText("Start autoregistration");
        jMIAutoregister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAutoregisterActionPerformed(evt);
            }
        });
        jMRegistration.add(jMIAutoregister);
        jMRegistration.add(jSeparator9);

        jMIDelAllAutoReg.setText("Delete all autoregistrations");
        jMIDelAllAutoReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDelAllAutoRegActionPerformed(evt);
            }
        });
        jMRegistration.add(jMIDelAllAutoReg);

        jMIDelAllReg.setText("Delete all registrations");
        jMIDelAllReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDelAllRegActionPerformed(evt);
            }
        });
        jMRegistration.add(jMIDelAllReg);
        jMRegistration.add(jSeparator17);

        jMIFixReg.setText("Fix registration of selected items");
        jMIFixReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIFixRegActionPerformed(evt);
            }
        });
        jMRegistration.add(jMIFixReg);

        jMIDelReg.setText("Delete autoregistration of selected items");
        jMIDelReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIDelRegActionPerformed(evt);
            }
        });
        jMRegistration.add(jMIDelReg);
        jMRegistration.add(jSeparator12);

        jMICites.setText("Add rule \"cites\"");
        jMICites.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICitesActionPerformed(evt);
            }
        });
        jMRegistration.add(jMICites);
        jMRegistration.add(jSeparator7);

        jMIEditRules.setText("Edit rules");
        jMIEditRules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditRulesActionPerformed(evt);
            }
        });
        jMRegistration.add(jMIEditRules);

        jMainMenu.add(jMRegistration);

        jMTools.setText("Tools");

        jMISynch.setText("Synchronize library");
        jMISynch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMISynchActionPerformed(evt);
            }
        });
        jMTools.add(jMISynch);
        jMTools.add(jSeparator13);

        jMISetupER.setText("Setup USB devices");
        jMISetupER.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMISetupERActionPerformed(evt);
            }
        });
        jMTools.add(jMISetupER);

        jMISynchER.setText("Synchronize with USB device");
        jMISynchER.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMISynchERActionPerformed(evt);
            }
        });
        jMTools.add(jMISynchER);
        jMTools.add(jSeparator18);

        jMIEditLocation.setText("Edit my location");
        jMIEditLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEditLocationActionPerformed(evt);
            }
        });
        jMTools.add(jMIEditLocation);
        jMTools.add(jSeparator29);

        jMIEditLibTemplates.setText("Edit library templates");
        jMIEditLibTemplates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEditLibTemplatesActionPerformed(evt);
            }
        });
        jMTools.add(jMIEditLibTemplates);

        jMainMenu.add(jMTools);

        jMBibTeX.setText("Bibliography");

        jMICitationTagClipboard.setText("Citation tag to clipboard");
        jMICitationTagClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICitationTagClipboardActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMICitationTagClipboard);

        jMIBibClipboard.setText("BibTeX for selected items to clipboard");
        jMIBibClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIBibClipboardActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMIBibClipboard);

        jMIBibOutClipboard.setText("Bibliography record for selected items to clipboard");
        jMIBibOutClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIBibOutClipboardActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMIBibOutClipboard);
        jMBibTeX.add(jSeparator19);

        jMIShowCitedinFile.setText("Show all papers cited in a TeX file");
        jMIShowCitedinFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIShowCitedinFileActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMIShowCitedinFile);

        jMICreateBib.setText("Create BibTeX-file of items in the current library");
        jMICreateBib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateBibActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMICreateBib);

        jMICheckBib.setText("Check BibTeX integrity");
        jMICheckBib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICheckBibActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMICheckBib);

        jMICreateLit.setText("Create literature list");
        jMICreateLit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMICreateLitActionPerformed(evt);
            }
        });
        jMBibTeX.add(jMICreateLit);

        jMainMenu.add(jMBibTeX);

        jMHelp.setText("Help");

        JMIManual.setText("Manual");
        JMIManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JMIManualActionPerformed(evt);
            }
        });
        jMHelp.add(JMIManual);

        jMIUpdate.setText("Celsius Homepage");
        jMIUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIUpdateActionPerformed(evt);
            }
        });
        jMHelp.add(jMIUpdate);
        jMHelp.add(jSeparator1);

        jMIAbout.setText("About");
        jMIAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAboutActionPerformed(evt);
            }
        });
        jMHelp.add(jMIAbout);

        jMainMenu.add(jMHelp);

        setJMenuBar(jMainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMIJoinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIJoinActionPerformed
        if (isTabAvailable) {
            Library Lib = RSC.getCurrentSelectedLib();
            ArrayList<Item> docs=RSC.getCurrentItemTable().getSelectedItems();
            if (docs.size() > 2) {
                toolbox.Warning(this,"Only the first two entries will be joined.", "Warning:");
            } else {
                if (docs.size()<2) {
                    toolbox.Warning(this,"Please selected two documents to be joined.", "Warning:");
                    return;
                }
            }
            String id0=docs.get(0).get("id");
            int id=docs.get(0).id;
            String id1=docs.get(1).get("id");
            if (JM.joinItems(Lib,id1,id0)) {
                RSC.getCurrentItemTable().replace(new Item(Lib,id1));
                int i=RSC.getCurrentItemTable().getSelectedRow();

                DefaultListSelectionModel DLSM=(DefaultListSelectionModel) RSC.getCurrentItemTable().jtable.getSelectionModel();
                DLSM.addSelectionInterval(i, i);
                RSC.getCurrentItemTable().jtable.setSelectionModel(DLSM);
                RSC.getCurrentItemTable().removeID(id);
                updateStatusBar(true);
                jIP.updateHTMLview();
                jIP.updateRawData();
            }
        }

    }//GEN-LAST:event_jMIJoinActionPerformed

    private void jMIAssociateFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAssociateFileActionPerformed
        if (isTabAvailable) {
            Library Lib = RSC.getCurrentSelectedLib();
            for (Item doc : RSC.getCurrentItemTable().getSelectedItems()) {
                JFileChooser FC = new JFileChooser();
                FC.setDialogTitle("Indicate the file to be associated with the selected record");
                FC.setCurrentDirectory(new File(RSC.getDir("associate")));
                FC.setDialogType(JFileChooser.OPEN_DIALOG);
                FC.setFileFilter(new FFilter("_ALL", "All files"));
                RSC.setComponentFont(FC.getComponents());
                // Akzeptiert?
                if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
                    String filename = FC.getSelectedFile().getAbsolutePath();
                    RSC.rememberDir("associate", FC);
                    String name=null;
                    if (doc.get("location")==null) {
                        name="";
                    } else {
                        final SingleLineEditor DSLE = new SingleLineEditor(RSC, "Please enter a description for the associated file", "", true);
                        DSLE.setVisible(true);
                        if (!DSLE.cancel)
                            name = DSLE.text.trim();
                        DSLE.dispose();
                    }
                    if (name!=null) {
                        if (name.length()==0) name=null;
                        try {
                            doc.associateWithFile(RSC, filename,name);
                            jIP.updateHTMLview();
                            jIP.updateRawData();
                            updateStatusBar(true);
                            Msg1.repS("LIBFA>Added file " + filename + " to record with ID: " + doc.get("id"));
                        } catch (IOException ex) {
                            Msg1.repS("LIBFA>Failed::Adding file " + filename + " to record with ID: " + doc.get("id"));
                            RSC.Msg1.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        updateStatusBar(true);
        jIP.updateHTMLview();
        jIP.updateRawData();
    }//GEN-LAST:event_jMIAssociateFileActionPerformed

    private void jMIReduceDRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIReduceDRActionPerformed
        if (isTabAvailable) {
            Library Lib = RSC.getCurrentSelectedLib();
            for (Item doc : RSC.getCurrentItemTable().getSelectedItems()) {
                if (doc.get("location")==null) {
                    toolbox.Warning(this,"There is no file associated with the current entry.", "Warning");
                } else {
                    int i = toolbox.QuestionYN(this,"Should the file\n" + doc.get("location") + "\nbelonging to the record for\n" + doc.toText() + "\nbe deleted?", "Please decide:");
                    if (i == JOptionPane.YES_OPTION) {
                       doc.reduceToDocRef();
                    }
                }
            }
            jIP.updateHTMLview();
            jIP.updateRawData();
            updateStatusBar(true);
        }
    }//GEN-LAST:event_jMIReduceDRActionPerformed

    private void jBtnClrSrchCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClrSrchCatActionPerformed
        jTFSearchCategories.resetText();
    }//GEN-LAST:event_jBtnClrSrchCatActionPerformed

    private void jMIOpenAnnotationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIOpenAnnotationActionPerformed
        String names = "";
        if (isTabAvailable) {
            ItemTable DT=RSC.getCurrentItemTable();
            for (Item doc : DT.getSelectedItems()) {
                if (doc.get("filetype").equals("pdf")) {
                    names += " '" + doc.getCompleteDirS("location") + ".xoj'";
                }
            }
        } else {
            return;
        }
        names = names.trim();
        if (names.length()==0) {
            return;
        }
        try {
            String cmdln = RSC.Configuration.getDefault("annotate");
            cmdln = cmdln.replace("%from%", names);
            ExecutionShell ES = new ExecutionShell(cmdln, 0, true);
            ES.start();
        } catch (Exception e) {
            Msg1.printStackTrace(e);
        }
    }//GEN-LAST:event_jMIOpenAnnotationActionPerformed

    private void jMIAnnotatePDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAnnotatePDFActionPerformed
        String names = "";
        if (isTabAvailable) {
            ItemTable DT=RSC.getCurrentItemTable();
            for (Item doc : DT.getSelectedItems()) {
                if (doc.get("filetype").equals("pdf")) {
                    names += " '" + doc.getCompleteDirS("location") + ".xoj'";
                }
            }
        } else {
            return;
        }
        names = names.trim();
        if (names.equals("")) {
            return;
        }
        try {
            String cmdln = RSC.Configuration.getDefault("annotate");
            cmdln = cmdln.replace("%from%", names);
            ExecutionShell ES = new ExecutionShell(cmdln, 0, true);
            ES.start();
        } catch (Exception e) {
            Msg1.printStackTrace(e);
        }
    }//GEN-LAST:event_jMIAnnotatePDFActionPerformed

    private void jMICopyTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICopyTabActionPerformed
        noDocSelected();
        ItemTable DT=new ItemTable(RSC.getCurrentItemTable());
        RSC.ItemTables.add(DT);

        jMICloseTab2.setEnabled(true);
        jMICloseTab.setEnabled(true);
        final JScrollPane scrollpane = new JScrollPane(DT.jtable);
        jTPTabList.add(scrollpane);
        TabLabel TL=(TabLabel)jTPTabList.getTabComponentAt(jTPTabList.getSelectedIndex());
        jTPTabList.setTabComponentAt(jTPTabList.getTabCount() - 1, new TabLabel(TL.title + "'",TL.II,RSC,DT,true));
        DT.title=TL.title + "'";
        jTPTabList.setSelectedComponent(scrollpane);
        jTPTabList.setSelectedIndex(jTPTabList.getTabCount() - 1);
        int cordx = bufmousex - jTStructureTree.getLocationOnScreen().x;
        int cordy = bufmousey - jTStructureTree.getLocationOnScreen().y;
        jTStructureTree.setSelectionPath(null);
        jTStructureTree.setSelectionPath(jTStructureTree.getPathForLocation(cordx, cordy));
    }//GEN-LAST:event_jMICopyTabActionPerformed

    private void jMIPluginInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIPluginInfoActionPerformed
        if (jLPlugins.getSelectedIndex() == -1) {
            return;
        }
        String n=(String)jLPlugins.getSelectedValue();
        toolbox.Information(this,RSC.Plugins.getInfo(n),"Information");
    }//GEN-LAST:event_jMIPluginInfoActionPerformed

    private void jBTNApplyToAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBTNApplyToAllActionPerformed
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "Applying plugin to items ...", "", 0, RSC.getCurrentSelectedLib().getSize());
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);
        String n=(String)jLPlugins.getSelectedValue();
        (new ThreadApplyPlugin(progressMonitor, RSC.getCurrentSelectedLib(), RSC.Plugins.get(n),RSC.Plugins.parameters.get(n),RSC, (ArrayList<Item>)null,true)).start();
    }//GEN-LAST:event_jBTNApplyToAllActionPerformed

    private void jPMCategoriesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPMCategoriesPropertyChange
        String s1 = Parser.CutTill(Parser.CutFrom(evt.toString(), "desiredLocationX="), ",");
        String s2 = Parser.CutTill(Parser.CutFrom(evt.toString(), "desiredLocationY="), ",");
        bufmousex = Integer.valueOf(s1);
        bufmousey = Integer.valueOf(s2);
    }//GEN-LAST:event_jPMCategoriesPropertyChange

    private void jMICreateTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICreateTxtActionPerformed
        // plaintxt already existing?
        Library Lib = RSC.getCurrentSelectedLib();
        boolean doit=false;
        int h=0;
        for (Item doc : RSC.getCurrentItemTable().getSelectedItems()) {
            if ((doc.get("plaintxt") != null) && (new File(doc.getCompleteDirS("plaintxt"))).exists()) {
                if (!doit) h=toolbox.QuestionABCD(this,"Overwrite existing plaintxt file?", "Confirm:","Yes","No","Yes to all","Cancel");
                if (h==3) break;
                if (h==2) doit=true;
            }
            if (doit || (h == 0)) {
                TextFile.Delete(doc.getCompleteDirS("plaintxt"));
                String target = "AI::txt";
                String source = doc.getCompleteDirS("location");
                try {
                    Msg1.repS("MAIN>Getting Plain Txt for :: " + source);
                    RSC.Configuration.ExtractText("MAIN>", source, doc.completeDir(target));
                    doc.put("plaintxt", target+".gz");
                    doc.put("pages", Integer.toString(toolbox.ReadNumberOfPagesOf(Msg1, "MAIN>", source, doc.completeDir(target + ".gz"))));
                    doc.save();
                    Lib.setChanged(true);
                } catch (IOException ex) {
                    toolbox.Warning(this,"Error while creating plain text:\n" + ex.toString(), "Exception:");
                    RSC.Msg1.printStackTrace(ex);
                }
            }
        }
        jIP.updateHTMLview();
        jIP.updateRawData();
        updateStatusBar(true);
    }//GEN-LAST:event_jMICreateTxtActionPerformed

    private void jMIConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIConfigActionPerformed
        dialogConfiguration.showTab(0);
    }//GEN-LAST:event_jMIConfigActionPerformed

    private void jMIRenameCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIRenameCatActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN == null) {
            return;
        }
        final Library Lib = RSC.getCurrentSelectedLib();
        Lib.Structure.goTo(TN);
        if (Lib.Structure.isRoot()) return;
        final SingleLineEditor DSLE = new SingleLineEditor(RSC, "Please enter a new name for the category", Lib.Structure.get("title").trim(),true);
        DSLE.setVisible(true);
        if (!DSLE.cancel) {
            int i=toolbox.QuestionYN(this,"Should the items currently registered under "+TN.toString()+" be adjusted?", "Please confirm");
            if (i==0) {
                final MainFrame MF=this;
                (new Thread("Refactor") {

                    @Override
                    public void run() {
                        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
                        setThreadMsg("Refactoring...");
                        MF.jPBSearch.setIndeterminate(true);
                        String Told = Lib.Structure.get("title").trim();
                        String Tnew = DSLE.text.trim();
                        Lib.Structure.write("title", Tnew);

                        // Adapt Rules
                        Lib.Rules.refactor("target",Told,Tnew);

                        // Adapt Registrations
                        for (Item doc : Lib) {
                            if (Parser.EnumContains(doc.get("autoregistered"), Told)) {
                                doc.put("autoregistered", Parser.EnumReplace(doc.get("autoregistered"), Told, Tnew));
                            }
                            if (Parser.EnumContains(doc.get("registered"), Told)) {
                                doc.put("registered", Parser.EnumReplace(doc.get("registered"), Told, Tnew));
                            }
                        }
                        Lib.CatRemarks.toFirstElement();
                        while (!Lib.CatRemarks.endReached) {
                            if (Lib.CatRemarks.get("category").equals(Told))
                                Lib.CatRemarks.put("category",Tnew);
                            Lib.CatRemarks.nextElement();
                        }
                        setThreadMsg("Ready.");
                        MF.jPBSearch.setIndeterminate(false);
                        Lib.setChanged(true);
                        StructureTreeModel.reload();
                        jTStructureTree.scrollPathToVisible(new TreePath(StructureTreeModel.getPathToRoot(TN)));
                        updateStatusBar(false);
                    }
                }).start();
            } else {
                Lib.Structure.write("title", DSLE.text);
                Lib.setChanged(true);
                StructureTreeModel.reload();
                final StructureNode child = Lib.Structure.Node;
                jTStructureTree.scrollPathToVisible(new TreePath(child.getPath().toArray()));
                updateStatusBar(false);
            }
        }
        DSLE.dispose();
    }//GEN-LAST:event_jMIRenameCatActionPerformed

    private void jMIShowCitedinFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIShowCitedinFileActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Indicate the LaTeX source file");
        FC.setCurrentDirectory(new File(RSC.getDir("showcited")));
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        FC.setFileFilter(new FFilter("_ALL", "All files"));
        // Akzeptiert?
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            RSC.rememberDir("showcited", FC);
            ItemTable IT=RSC.makeNewTabAvailable(5, "Cited in " + FC.getSelectedFile().getName(),"magnifier");
            noDocSelected();
            ProgressMonitor progressMonitor = new ProgressMonitor(this, "Looking for papers ...", "", 0, RSC.getCurrentSelectedLib().getSize());
            (new ThreadShowCited(RSC.getCurrentSelectedLib(), progressMonitor, FC.getCurrentDirectory() + toolbox.filesep + FC.getSelectedFile().getName(),IT)).start();
        }
    }//GEN-LAST:event_jMIShowCitedinFileActionPerformed

    private void jMIReloadPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIReloadPluginsActionPerformed
        RSC.Plugins.ReadIn();
        jLPlugins.setModel(RSC.Plugins.getPluginsDLM("manual",RSC.getCurrentSelectedLib()));
        DefaultComboBoxModel DCBM=RSC.Plugins.getPluginsDCBM("export",RSC.getCurrentSelectedLib());
        jIP.jCBBibPlugins.setModel(DCBM);
        jCBExpFilter.setModel(DCBM);
    }//GEN-LAST:event_jMIReloadPluginsActionPerformed

    private void jMIOpenNewTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIOpenNewTabActionPerformed
        RSC.makeNewTabAvailable(0, "", "default");
        jTPTabList.setSelectedIndex(jTPTabList.getTabCount() - 1);
        int cordx = bufmousex - jTStructureTree.getLocationOnScreen().x;
        int cordy = bufmousey - jTStructureTree.getLocationOnScreen().y;
        jTStructureTree.setSelectionPath(null);
        jTStructureTree.setSelectionPath(jTStructureTree.getPathForLocation(cordx, cordy));
    }//GEN-LAST:event_jMIOpenNewTabActionPerformed

    private void jMISynchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMISynchActionPerformed
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "", "", 0, RSC.getCurrentSelectedLib().getSize());                    // Progress label
        setThreadMsg("Working...");
        (new ThreadSynchronizeLib(RSC.getCurrentSelectedLib(), progressMonitor, Msg1, this)).start();
    }//GEN-LAST:event_jMISynchActionPerformed

    private void jMIEditLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditLibActionPerformed
        (new EditLibrary(this,RSC.getCurrentSelectedLib())).setVisible(true);
    }//GEN-LAST:event_jMIEditLibActionPerformed

    private void jMIEditDSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditDSActionPerformed
        Library Lib = RSC.getCurrentSelectedLib();
        String tmp = Lib.getDisplayString(jIP.infoMode);
        MultiLineEditor DMLE = new MultiLineEditor(RSC, "Edit HTML template", tmp);
        DMLE.setVisible(true);
        if (!DMLE.cancel) {
            Lib.setDisplayString(jIP.infoMode,DMLE.text);
            jIP.updateHTMLview();
        }
    }//GEN-LAST:event_jMIEditDSActionPerformed

    private void jMICatSuperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICatSuperActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if ((TN == null) || (TN.getParent() == null)) {
            return;
        }
        if (TN.getParent().getParent()==null) {
            return;
        }
        StructureNode TN2 = TN.getParent().getParent();
        if ((TN2 != RSC.getCurrentSelectedLib().Structure.Root) || (TN2 != null)) {
            final StructureNode TNT = TN;
            StructureTreeModel.removeNodeFromParent(TN);
            StructureTreeModel.insertNodeInto(TNT, TN2, TN2.getChildCount());
            jTStructureTree.scrollPathToVisible(new TreePath(TNT.getPath().toArray()));
            RSC.getCurrentSelectedLib().setChanged(true);
            updateStatusBar(false);
        }
    }//GEN-LAST:event_jMICatSuperActionPerformed

    private void jMICatSubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICatSubActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN == null) {
            return;
        }
        if (TN.isRoot()) {
            return;
        }
        StructureNode TN2 = TN.getParent();
        final int i = TN2.getIndex(TN);
        if (i > 0) {
            TN2 = TN2.getChildAt(i - 1);
            final StructureNode TNT = TN;
            StructureTreeModel.removeNodeFromParent(TN);
            StructureTreeModel.insertNodeInto(TNT,TN2, TN2.getChildCount());
            jTStructureTree.scrollPathToVisible(new TreePath(TNT.getPath().toArray()));
            RSC.getCurrentSelectedLib().setChanged(true);
            updateStatusBar(false);
        }
    }//GEN-LAST:event_jMICatSubActionPerformed

    private void jMICatDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICatDownActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN != null) {
            if (TN.isRoot()) {
                return;
            }
            StructureNode TN2 = TN.getParent();
            final int i = TN2.getIndex(TN);
            final int j = (jTStructureTree.getSelectionRows())[0];
            if (i < TN2.getChildCount() - 1) {
                TN2.remove(TN);
                TN2.insert(TN, i + 1);
                StructureTreeModel.nodeStructureChanged(TN2);
                jTStructureTree.addSelectionPath(jTStructureTree.getPathForRow(j + 1));
                RSC.getCurrentSelectedLib().setChanged(true);
                updateStatusBar(false);
            }
        }
    }//GEN-LAST:event_jMICatDownActionPerformed

    private void jMICatUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICatUpActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN != null) {
            if (TN.isRoot()) {
                return;
            }
            StructureNode TN2 = TN.getParent();
            final int i = TN2.getIndex(TN);
            final int j = (jTStructureTree.getSelectionRows())[0];
            if (i > 0) {
                TN2.remove(TN);
                TN2.insert(TN, i - 1);
                StructureTreeModel.nodeStructureChanged(TN2);
                jTStructureTree.addSelectionPath(jTStructureTree.getPathForRow(j - 1));
                RSC.getCurrentSelectedLib().setChanged(true);
                updateStatusBar(false);
            }
        }
    }//GEN-LAST:event_jMICatUpActionPerformed

    private void jMIDelCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDelCatActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN != null) {
            if (TN.isRoot()) {
                return;
            }
            final int i = toolbox.QuestionOC(this,"Click OK to delete subcategory.", "Warning");
            if (i == JOptionPane.YES_OPTION) {
                StructureTreeModel.removeNodeFromParent(TN);
                RSC.getCurrentSelectedLib().setChanged(true);
                updateStatusBar(false);
            }
        }
    }//GEN-LAST:event_jMIDelCatActionPerformed

    private void jMIInsertCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIInsertCatActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN == null) {
            return;
        }
        SingleLineEditor DSLE = new SingleLineEditor(RSC, "Please enter a name for the category", "Category",false);
        DSLE.setVisible(true);
        if (!DSLE.cancel) {
            Library Lib = RSC.getCurrentSelectedLib();
            final HashMap<String,String> data = new HashMap<String,String>();
            data.put("title",DSLE.text.trim());
            Lib.Structure.goTo(TN);
            final StructureNode child = Lib.Structure.CreateNewNode("menu",data);
            StructureTreeModel.reload();
            jTStructureTree.scrollPathToVisible(new TreePath(child.getPath().toArray()));
            Lib.setChanged(true);
            updateStatusBar(false);
        }
        DSLE.dispose();
    }//GEN-LAST:event_jMIInsertCatActionPerformed

    private void jBtnApplyPluginSelDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyPluginSelDocActionPerformed
        int plugin=jLPlugins.getSelectedIndex();
        if (plugin == -1) {
            return;
        }
        ItemTable IT=RSC.getCurrentItemTable();
        if (IT == null) return;
        setThreadMsg("Working...");
        ArrayList<Item> docs=IT.getSelectedItems();
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "Applying plugin to items ...", "", 0, docs.size());
        String n=(String)jLPlugins.getSelectedValue();
        ThreadApplyPlugin TAP = (new ThreadApplyPlugin(progressMonitor, IT.Lib, RSC.Plugins.get(n),RSC.Plugins.parameters.get(n),RSC, docs,true));
        TAP.start();
        RSC.getCurrentItemTable().jtable.requestFocus();
    }//GEN-LAST:event_jBtnApplyPluginSelDocActionPerformed

    private void jMICheckBibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICheckBibActionPerformed
        ItemTable IT=RSC.makeTabAvailable(5, "Items with corrupt BibTeX","magnifier");
        setThreadMsg("Working...");
        jIP.setDocumentTable(IT);
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "Checking BibTeX integrity ...", "", 0, RSC.getCurrentSelectedLib().getSize());
        (new ThreadBibTeXIntegrity(RSC, progressMonitor, IT)).start();
    }//GEN-LAST:event_jMICheckBibActionPerformed

    private void jMIEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEmailActionPerformed
        int i = jTPTabList.getSelectedIndex();
        String names = "";
        Library Lib = RSC.getCurrentSelectedLib();
        if (i > -1) {
            for (Item doc : RSC.ItemTables.get(i).getSelectedItems())
                names += " '" + doc.getCompleteDirS("location") + "'";
        } else {
            return;
        }
        names = names.trim();
        try {
            String cmdln = RSC.Configuration.getDefault("email");
            cmdln = cmdln.replace("%from%", names);
            ExecutionShell ES = new ExecutionShell(cmdln, 0, true);
            ES.start();
        } catch (Exception e) {
            Msg1.printStackTrace(e);
        }
    }//GEN-LAST:event_jMIEmailActionPerformed

    /**
     * Copy the selected documents into another folder, appropriately renamed
     */
    private void jMIExportTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIExportTabActionPerformed
        if (!isTabAvailable) {
            return;
        }
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Select the target folder for exporting");
        FC.setCurrentDirectory(new File(RSC.getDir("exportTab")));
        FC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        FC.setFileFilter(new FFilter("_DIR", "Folders"));
        RSC.setComponentFont(FC.getComponents());
        // Akzeptiert?
        if (!(FC.showSaveDialog(this) == JFileChooser.CANCEL_OPTION)) {
            RSC.rememberDir("exportTab", FC);
            try {
                String folder = (String) FC.getSelectedFile().getCanonicalPath();
                if (!(new File(folder)).exists()) {
                    (new File(folder)).mkdir();
                }
                for (Item doc : RSC.getCurrentItemTable().getSelectedItems()) {
                    if (doc.getS("location").length()>0) {
                        String filename = doc.standardFileName(null);
                        (new InteractiveFileCopy(this,doc.getCompleteDirS("location"), folder + "/" + filename,RSC)).go();
                        if (doc.getS("filetype").equals("m3u")) {
                            TextFile PL=new TextFile(doc.getCompleteDirS("location"));
                            while (PL.ready()) {
                                String fn=PL.getString();
                                (new InteractiveFileCopy(this,(new File(doc.getCompleteDirS("location"))).getParent()+"/"+fn, folder + "/" + fn,RSC)).go();
                            }
                            PL.close();
                        }
                    }
                }
            } catch (Exception ex) {
                toolbox.Warning(this,"Error while exporting files:\n" + ex.toString(), "Exception:");
                RSC.Msg1.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_jMIExportTabActionPerformed

    private void jMIAutoregisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAutoregisterActionPerformed
        try {
            (new RegisterItems(this, RSC)).setVisible(true);
            if (RSC.getCurrentSelectedLib().hasChanged()) {
                updateStatusBar(true);
            }
        } catch (IOException ex) {
            toolbox.Warning(this,"Error during auto-registration:\n" + ex.toString(), "Exception:");
            RSC.Msg1.printStackTrace(ex);
        }
    }//GEN-LAST:event_jMIAutoregisterActionPerformed

    private void jMIDelAllRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDelAllRegActionPerformed
        int i = toolbox.QuestionOC(this,"Clear all registration entries?", "Confirm:");
        if (i == JOptionPane.YES_OPTION) {
            for (Item doc : RSC.getCurrentSelectedLib()) {
                doc.put("registered",null);
            }
            jIP.updateRawData();
            updateStatusBar(false);
        }
    }//GEN-LAST:event_jMIDelAllRegActionPerformed

    private void jMIDelAllAutoRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDelAllAutoRegActionPerformed
        int i = toolbox.QuestionOC(this,"Clear all autoregistration entries?", "Confirm:");
        if (i == JOptionPane.YES_OPTION) {
            for (Item doc : RSC.getCurrentSelectedLib()) {
                doc.put("autoregistered",null);
            }
            jIP.updateRawData();
            updateStatusBar(false);
        }
    }//GEN-LAST:event_jMIDelAllAutoRegActionPerformed

    private void jMIDelRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDelRegActionPerformed
        if (!isTabAvailable) {
            return;
        }
        for (Item doc : RSC.ItemTables.get(jTPTabList.getSelectedIndex()).getSelectedItems()) {
            doc.put("autoregistered", null);
        }
        RSC.getCurrentSelectedLib().setChanged(true);
        updateStatusBar(false);
        jIP.updateRawData();
    }//GEN-LAST:event_jMIDelRegActionPerformed
    // +
    private void jMIFixRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIFixRegActionPerformed
        if (!isTabAvailable) {
            return;
        }
        for (Item doc : RSC.ItemTables.get(jTPTabList.getSelectedIndex()).getSelectedItems()) {
            String reg = doc.get("registered") + "|" + doc.get("autoregistered");
            if (reg.startsWith("|")) {
                reg = reg.substring(1);
            }
            if (reg.endsWith("|")) {
                reg = Parser.CutTillLast(reg, "|");
            }
            doc.put("registered", reg);
            doc.put("autoregistered", null);
        }
        RSC.getCurrentSelectedLib().setChanged(true);
        updateStatusBar(false);
        jIP.updateRawData();
    }//GEN-LAST:event_jMIFixRegActionPerformed
    // +
    private void jMIReloadDisplayStringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIReloadDisplayStringActionPerformed
        jIP.reloadDisplayString();
    }//GEN-LAST:event_jMIReloadDisplayStringActionPerformed
    // +
    private void jMICitesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICitesActionPerformed
        Library Lib = RSC.getCurrentSelectedLib();
        String paper = RSC.getCurrentItemTable().getSelectedItems().get(0).get("identifier");
        String target=Lib.Structure.get("title").trim();
        if ((paper==null) || (target==null)) {
            toolbox.Warning(this,"No document or category selected!", "Warning:");
            return;
        }
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("paper",paper);
        data.put("target",target);
        Lib.Rules.goTo(Lib.Rules.Root);
        Lib.Rules.CreateNewNode("citing",data);
        Lib.setChanged(true);
        updateStatusBar(false);
        updateRulesByCategory();
    }//GEN-LAST:event_jMICitesActionPerformed

    private void jMIUnregisterDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIUnregisterDocActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN != null) {
            ItemTable DT=RSC.getCurrentItemTable();
            if (DT!=null) {
                Library Lib = RSC.getCurrentSelectedLib();
                Lib.Structure.goTo(TN);
                final String s1 = Lib.Structure.get("title").trim();
                boolean doit=false;
                int h=0;
                for (Item doc : DT.getSelectedItems()) {
                    String s3 = doc.getS("autoregistered");
                    String s4 = doc.getS("registered");
                    if (Parser.EnumContains(s3, s1) || Parser.EnumContains(s4, s1)) {
                        if (!doit) h=toolbox.QuestionABCD(this,"Remove the document \n" + doc.toText() + "\nfrom the current category?",
                                "Warning","Yes","No","Yes to all","Cancel");
                        if (h==3) break;
                        if (h==2) doit=true;
                        if (doit || (h == 0)) {
                            doc.put("autoregistered", Parser.EnumDelete(s3,s1));
                            doc.put("registered", Parser.EnumDelete(s4,s1));
                            updateTableByCategory();
                            Lib.setChanged(true);
                            updateStatusBar(false);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_jMIUnregisterDocActionPerformed

    private void jMIDeleteFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDeleteFileActionPerformed
        if (isTabAvailable) {
            ItemTable DT = RSC.ItemTables.get(jTPTabList.getSelectedIndex());
            Library CSL = DT.Lib;
            boolean doit=false;
            int h=0;
            for (Item doc : DT.getSelectedItems()) {
                if (!doit) h=toolbox.QuestionABCD(this,"Delete the document \n" + doc.toText() + "\nand all related information?",
                                "Warning","Yes","No","Yes to all","Cancel");
                if (h==3) break;
                if (h==2) doit=true;
                if (doit || (h == 0)) {
                    DT.removeItem(doc);
                    doc.removeFromLib(true);
                }
            }
            if (CSL.hasChanged()) {
                updateStatusBar(true);
            }
            jIP.restoreCreationType();
        }
    }//GEN-LAST:event_jMIDeleteFileActionPerformed

    private void jMIRemoveFromTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIRemoveFromTabActionPerformed
        ItemTable DT = RSC.getCurrentItemTable();
        if (DT!=null) {
            for (Item doc : DT.getSelectedItems())
                DT.removeItem(doc);
        }
        jIP.restoreCreationType();
    }//GEN-LAST:event_jMIRemoveFromTabActionPerformed

    private void jMIViewPlainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIViewPlainActionPerformed
        Item doc=RSC.getCurrentItemTable().getSelectedItems().get(0);
        Library Lib=RSC.getCurrentItemTable().Lib;
        if (doc==null) return;
        JM.viewPlainText(doc);
    }//GEN-LAST:event_jMIViewPlainActionPerformed

    private void jMIBibOutClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIBibOutClipboardActionPerformed
        Clipboard Clp = getToolkit().getSystemClipboard();
        ItemTable DT=RSC.getCurrentItemTable();
        if (DT!=null) {
            String latex = "";
            for(Item doc : DT.getSelectedItems()) {
                latex += "\n\n" + JM.getBibOutput(doc);
            }
            StringSelection cont = new StringSelection(latex.trim());
            Clp.setContents(cont, this);
        }
    }//GEN-LAST:event_jMIBibOutClipboardActionPerformed

    private void jMIBibClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIBibClipboardActionPerformed
        Clipboard Clp = getToolkit().getSystemClipboard();
        ItemTable DT=RSC.getCurrentItemTable();
        if (DT!=null) {
            String bibtex = "";
            for (Item doc : DT.getSelectedItems())
                bibtex += "\n\n" + doc.get("bibtex");
            StringSelection cont = new StringSelection(bibtex.trim());
            Clp.setContents(cont, this);
        }
    }//GEN-LAST:event_jMIBibClipboardActionPerformed

    private void jMICloseTab2TabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICloseTab2TabActionPerformed
        RSC.getCurrentItemTable().close();
}//GEN-LAST:event_jMICloseTab2TabActionPerformed

    private void jMIAddTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAddTabActionPerformed
        RSC.makeNewTabAvailable(0, "New" + Integer.toString(jTPTabList.getTabCount()), "default");
        jIP.switchModeTo1();
    }//GEN-LAST:event_jMIAddTabActionPerformed

    private void jMIUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIUpdateActionPerformed
        RSC.Configuration.viewHTML(RSC.celsiushome);
    }//GEN-LAST:event_jMIUpdateActionPerformed

    private void jMICreateBibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICreateBibActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Indicate the target BibTeX-File");
        FC.setCurrentDirectory(new File(RSC.getDir("createBibTeX")));
        FC.setDialogType(JFileChooser.SAVE_DIALOG);
        FC.setFileFilter(new FFilter("_ALL", "All files"));
        RSC.setComponentFont(FC.getComponents());
        // abgebrochen?
        if (FC.showSaveDialog(this) != JFileChooser.CANCEL_OPTION) {
            RSC.rememberDir("createBibTeX", FC);
            Msg1.repS("MAIN>Creating BibTeX file " + FC.getSelectedFile().getAbsolutePath());
            ProgressMonitor progressMonitor = new ProgressMonitor(this, "Creating BibTeX-file", "", 0, RSC.getCurrentSelectedLib().getSize());
            (new ThreadCreateBibTeX(RSC.getCurrentSelectedLib(), Msg1, progressMonitor, FC.getSelectedFile().getAbsolutePath())).start();
        }
    }//GEN-LAST:event_jMICreateBibActionPerformed

    /**
     * View a given hyperlink (usually from the document pane)
     * if http://$$view is given, then the viewer for the current document is started
     */
    private void jTPTabListStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTPTabListStateChanged
        if (buildingNewTab) return;
        ItemTable DT=RSC.getCurrentItemTable();
        if (DT == null) return;
        switchToLibrary(DT.Lib);
        if (DT.getSelectedRow() > -1) {
            DT.moveToSelectedinTable();
        } else {
            noDocSelected();
            jIP.switchModeTo(DT.getType());
            jIP.setDocumentTable(DT);
            jIP.restoreHTMLview();
        }        
    }//GEN-LAST:event_jTPTabListStateChanged

    private void jTStructureTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTStructureTreeValueChanged
        if (jTStructureTree.getSelectionModel().isSelectionEmpty()) {
            noCatSelected();
            return;
        }
        updateTableByCategory();
        updateRulesByCategory();
        jIP.switchModeTo2(RSC.getCurrentItemTable());
    }//GEN-LAST:event_jTStructureTreeValueChanged
    // Search stopped by clicking on Button "Stop""    // Search started by clicking on Button "Start"    // Menu: View Selected
    private void jMIViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIViewActionPerformed
        JM.ViewSelected(null);
    }//GEN-LAST:event_jMIViewActionPerformed

    private void jMINewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMINewActionPerformed
        RSC.makeNewTabAvailable(0, "New" + Integer.toString(jTPTabList.getTabCount()), "default");
        jIP.switchModeTo1();
    }//GEN-LAST:event_jMINewActionPerformed

    private void jCBLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBLibrariesActionPerformed
        switchToLibrary(null);
        jIP.switchModeTo1();
    }//GEN-LAST:event_jCBLibrariesActionPerformed
    // Save Current Library
    private void jMISaveLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMISaveLibActionPerformed
        final MainFrame MF=this;
        (new Thread("SavingCurrentLib") {

            @Override
            public void run() {
                setThreadMsg("Saving library...");
                MF.jPBSearch.setIndeterminate(true);
                try {
                    RSC.getCurrentSelectedLib().writeBack();
                } catch (Exception e) {
                    Msg1.printStackTrace(e);
                    toolbox.Warning(MF,"Saving current library failed:\n" + e.toString(), "Warning:");
                }
                setThreadMsg("Ready.");
                MF.jPBSearch.setIndeterminate(false);
                updateStatusBar(false);
            }
        }).start();
    }//GEN-LAST:event_jMISaveLibActionPerformed
    // Close Current Library
    private void jMICloseLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICloseLibActionPerformed
        JM.closeCurrentLibrary(true);
    }//GEN-LAST:event_jMICloseLibActionPerformed
    // Load Current Library
    private void jMILoadLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMILoadLibActionPerformed
        final JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Select the main file of the library you wish to open.");
        FC.setCurrentDirectory(new File(RSC.getDir("loadlibraries")));
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        RSC.setComponentFont(FC.getComponents());
        // abgebrochen?
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            RSC.rememberDir("loadlibraries", FC);
            final MainFrame MF=this;
            (new Thread("LoadingLib") {

                @Override
                public void run() {
                    setThreadMsg("Opening library...");
                    MF.jPBSearch.setIndeterminate(true);
                    try {
                        RSC.openLibrary(FC.getSelectedFile().getAbsolutePath());
                    } catch (Exception e) {
                        toolbox.Warning(MF,"Loading library failed:\n" + e.toString(), "Warning:");
                    }
                    setThreadMsg("Ready.");
                    MF.jPBSearch.setIndeterminate(false);
                    updateStatusBar(false);
                }
            }).start();
        }
    }//GEN-LAST:event_jMILoadLibActionPerformed
    // Create New Library
    private void jMICreateLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICreateLibActionPerformed
        CreateNewLibrary DCNL=(new CreateNewLibrary(this,RSC));
        DCNL.setVisible(true);
        if (DCNL.Lib==null) return;
        final Library Lib=DCNL.Lib;

        JMenuItem jmi=new JMenuItem(Lib.name);
        jmi.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyDocToLib(Lib);
            }
        });
        jMCopyToDiff.add(jmi);
        jmi=new JMenuItem(Lib.name);
        jmi.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyDocToLib(Lib);
            }
        });
        jMCopyToDiff1.add(jmi);
        
        DefaultComboBoxModel DCBM=(DefaultComboBoxModel)jCBLibraries.getModel();
        DCBM.addElement(Lib.name);
        DCBM.setSelectedItem(Lib.name);
    }//GEN-LAST:event_jMICreateLibActionPerformed

    private void jMICitationTagClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICitationTagClipboardActionPerformed
        if (isTabAvailable) {
            Clipboard Clp = getToolkit().getSystemClipboard();
            String ref = "";
            Library Lib = RSC.getCurrentSelectedLib();
            for (Item doc : RSC.getCurrentItemTable().getSelectedItems())
                ref += "," + doc.get("citation-tag");
            ref = ref.substring(1);
            StringSelection cont = new StringSelection(ref);
            Clp.setContents(cont, this);
        }
    }//GEN-LAST:event_jMICitationTagClipboardActionPerformed
    // Window closing with x
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        FinishProgram();
    }//GEN-LAST:event_formWindowClosing
    // Window closing from Menu
    private void jMIQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIQuitActionPerformed
        FinishProgram();
    }//GEN-LAST:event_jMIQuitActionPerformed

    private void JMIManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JMIManualActionPerformed
        RSC.Configuration.view("pdf", "manual.pdf");
    }//GEN-LAST:event_JMIManualActionPerformed

    private void jMIAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAboutActionPerformed
        new SplashScreen(RSC.VersionNumber, false,RSC);
    }//GEN-LAST:event_jMIAboutActionPerformed

private void jMIDeleteLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDeleteLibActionPerformed
    Library Lib=RSC.getCurrentSelectedLib();
    if (toolbox.QuestionYN(this,"Do you really want to delete the Library "+Lib.name+"?\nWarning: all files in the library's directory will be erased!", "Confirm:")==0) {
        JM.closeCurrentLibrary(false);
        Lib.deleteLibrary();
    }
}//GEN-LAST:event_jMIDeleteLibActionPerformed

private void jTStructureTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTStructureTreeMouseClicked
    if (jTStructureTree.getSelectionModel().isSelectionEmpty()) return;
    if (!isTabAvailable) return;
    ItemTable DT=RSC.getCurrentItemTable();
    if (DT==null) return;
    updateTableByCategory();
    updateRulesByCategory();
    jIP.switchModeTo2(DT);
}//GEN-LAST:event_jTStructureTreeMouseClicked

private void jMICreateLitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICreateLitActionPerformed
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        if (TN == null) {
            return;
        }
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Indicate the target text file");
        FC.setCurrentDirectory(new File(RSC.getDir("createLiteratureList")));
        FC.setDialogType(JFileChooser.SAVE_DIALOG);
        FC.setFileFilter(new FFilter("_ALL", "All files"));
        RSC.setComponentFont(FC.getComponents());
        // abgebrochen?
        if (!(FC.showSaveDialog(this) == JFileChooser.CANCEL_OPTION)) {
            RSC.rememberDir("createLiteratureList", FC);
            ProgressMonitor progressMonitor = new ProgressMonitor(this, "Creating Literature List", "", 0, 20);
            setThreadMsg("Working...");
            (new ThreadCreateLiteratureList(TN,FC.getCurrentDirectory() + "/" + FC.getSelectedFile().getName(),progressMonitor, RSC, this)).start();
        }
}//GEN-LAST:event_jMICreateLitActionPerformed

private void jMIMPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIMPluginsActionPerformed
        (new EditLibraryPlugins(this,RSC.getCurrentSelectedLib())).setVisible(true);
}//GEN-LAST:event_jMIMPluginsActionPerformed

private void jBtnEditRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEditRulesActionPerformed
    Library Lib=RSC.getCurrentSelectedLib();
    String source=Lib.Rules.source;
    String tmp=TextFile.ReadOutFile(source);
    boolean entering=true;
    boolean cancelled=false;
    while (!cancelled && (invalid(entering,tmp))) {
        if (!entering)
            toolbox.Warning(this,"The set of rules you entered is not valid.", "Warning:");
        entering=false;
        MultiLineEditor DMLE = new MultiLineEditor(RSC, "Edit registration rules", tmp);
        DMLE.setVisible(true);
        tmp=DMLE.text;
        cancelled=DMLE.cancel;
    }
    if (!cancelled) {
        try {
            TextFile TF=new TextFile(source,false);
            TF.putString(tmp);
            TF.close();
            Lib.Rules=new XMLTree(source,"$full");
        } catch (Exception e) {
            RSC.Msg1.printStackTrace(e);
        }
    }
}//GEN-LAST:event_jBtnEditRulesActionPerformed

private void JMEditDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JMEditDataActionPerformed
    ItemTable DT=RSC.getCurrentItemTable();
    if (DT == null) return;
    ArrayList<Item> docs=DT.getSelectedItems();
    if ((docs==null) || (docs.size()<1)) return;
    EditItem DED=new EditItem(this,RSC,docs);
    DED.setVisible(true);
    if (DT.Lib.hasChanged()) {
        JM.updateDTs(docs);
        updateStatusBar(false);
        DT.Lib.updatePeopleAndKeywordsLists();
    }
    jIP.updateHTMLview();
    jIP.updateRawData();
}//GEN-LAST:event_JMEditDataActionPerformed

private void jTPTabListComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jTPTabListComponentResized
    for(ItemTable DT : RSC.ItemTables) {
        DT.resizeTable(false);
    }
}//GEN-LAST:event_jTPTabListComponentResized

private void jMITab2Cat2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMITab2Cat2ActionPerformed
    Library Lib = RSC.getCurrentSelectedLib();
    StructureNode TN = Lib.Structure.Root;
    final HashMap<String, String> data = new HashMap<String, String>();
    String cat = RSC.getCurrentItemTable().title;
    data.put("title", cat);
    Lib.Structure.goTo(TN);
    StructureNode child = Lib.Structure.CreateNewNode("menu", data);
    for (Item doc : RSC.getCurrentItemTable().DTM.Items) {
        String s4 = doc.get("registered");
        doc.put("registered", s4 + "|" + cat);
    }
    StructureTreeModel.reload();
    jTStructureTree.scrollPathToVisible(new TreePath(child.getPath().toArray()));
    Lib.setChanged(true);
    updateStatusBar(false);
}//GEN-LAST:event_jMITab2Cat2ActionPerformed

private void jMISetupERActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMISetupERActionPerformed
    (new EditUSBDevices(this,RSC.getCurrentSelectedLib())).setVisible(true);
}//GEN-LAST:event_jMISetupERActionPerformed

private void jMISynchERActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMISynchERActionPerformed
    Library Lib=RSC.getCurrentSelectedLib();
    ChooseUSBDevice CUD=new ChooseUSBDevice(this,Lib);
    CUD.setVisible(true);
    if (CUD.deviceName!=null) (new ThreadSynchronizeUSBdev(this,Lib,CUD.deviceName)).start();
}//GEN-LAST:event_jMISynchERActionPerformed

private void jLPluginsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jLPluginsValueChanged
    if (jLPlugins.getSelectedIndex()>-1) {
        isPlugSelected=true;
    } else {
        isPlugSelected=false;
    }
    adjustStates();
}//GEN-LAST:event_jLPluginsValueChanged

private void jMIEditLibTemplatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditLibTemplatesActionPerformed
    (new celsius.Dialogs.EditLibraryTemplates(this,RSC)).setVisible(true);
}//GEN-LAST:event_jMIEditLibTemplatesActionPerformed

private void jMIAddToLibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAddToLibActionPerformed
    displayAddDocDialog();
}//GEN-LAST:event_jMIAddToLibActionPerformed

private void jTBAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTBAddActionPerformed
    displayAddDocDialog();
}//GEN-LAST:event_jTBAddActionPerformed

private void jBtnClrSrchAuthorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClrSrchAuthorsActionPerformed
    jTFSearchAuthors.resetText();
}//GEN-LAST:event_jBtnClrSrchAuthorsActionPerformed

private void jLPeopleLongMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLPeopleLongMouseClicked
    if (!(evt.getButton() == MouseEvent.BUTTON1)) return;
    JM.searchPeopleUpdate(true);
}//GEN-LAST:event_jLPeopleLongMouseClicked

private void jLPeopleLongValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jLPeopleLongValueChanged
    if (evt.getValueIsAdjusting()) return;
    JM.searchPeopleUpdate(true);
}//GEN-LAST:event_jLPeopleLongValueChanged

private void jLSearchPeopleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLSearchPeopleMouseClicked
    if (!(evt.getButton() == MouseEvent.BUTTON1)) return;
    JM.searchPeopleUpdate(false);
}//GEN-LAST:event_jLSearchPeopleMouseClicked

private void jLSearchPeopleValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jLSearchPeopleValueChanged
    if (evt.getValueIsAdjusting()) return;
    JM.searchPeopleUpdate(false);
}//GEN-LAST:event_jLSearchPeopleValueChanged

private void jBtnClrSrchKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClrSrchKeyActionPerformed
    jTFSearchKey.resetText();
}//GEN-LAST:event_jBtnClrSrchKeyActionPerformed

private void jLSearchKeysMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLSearchKeysMouseClicked
    if (!(evt.getButton() == MouseEvent.BUTTON1))
        return;
    searchKeysUpdate();
}//GEN-LAST:event_jLSearchKeysMouseClicked

private void jLSearchKeysValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jLSearchKeysValueChanged
    if (evt.getValueIsAdjusting()) return;
    searchKeysUpdate();
}//GEN-LAST:event_jLSearchKeysValueChanged

private void jTBSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTBSearchActionPerformed
    showSearchDialog();
}//GEN-LAST:event_jTBSearchActionPerformed

private void jMIDeepSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIDeepSearchActionPerformed
    showSearchDialog();
}//GEN-LAST:event_jMIDeepSearchActionPerformed

private void jTFSearchAuthorsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTFSearchAuthorsKeyTyped
    if (evt.isAltDown()) {
        return;
    }
    if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
        if (jTFSearchstate == 1) {
            if ((isTabAvailable) && (RSC.getCurrentItemTable().jtable.getModel().getRowCount() > 0)) {
                RSC.getCurrentItemTable().jtable.clearSelection();
                RSC.getCurrentItemTable().jtable.setRowSelectionInterval(0, 0);
                JM.ViewSelected(null);
            }
        } else {
            jLSearchPeople.setSelectedIndex(0);
            jTFSearchstate = 1;
        }
    } else {
        jTFSearchstate = 0;
    }
}//GEN-LAST:event_jTFSearchAuthorsKeyTyped

private void jTFSearchKeyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTFSearchKeyKeyTyped
    if (evt.isAltDown()) {
        return;
    }
    if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
        if (jTFSearchstate == 1) {
            if ((isTabAvailable) && (RSC.getCurrentItemTable().jtable.getRowCount() > -1)) {
                RSC.getCurrentItemTable().jtable.clearSelection();
                RSC.getCurrentItemTable().jtable.setRowSelectionInterval(0, 0);
                JM.ViewSelected(null);
            }
        } else {
            jLSearchKeys.setSelectedIndex(0);
            jTFSearchstate = 1;
        }
    } else {
        jTFSearchstate = 0;
    }
}//GEN-LAST:event_jTFSearchKeyKeyTyped

private void jMICreateCombiner1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICreateCombiner1ActionPerformed
    CreateCombiner CC=new CreateCombiner(this,RSC.getCurrentItemTable().getSelectedItems());
    CC.setVisible(true);
    if (CC.addedCombiner != null) {
        ItemTable CDT=RSC.makeNewTabAvailable(8, "Last added","magnifier");
        CDT.addItemFast(CC.addedCombiner);
        CDT.resizeTable(true);
        jIP.updateHTMLview();
        jIP.updateRawData();
        updateStatusBar(true);
    }
}//GEN-LAST:event_jMICreateCombiner1ActionPerformed

private void jRBExpFileItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRBExpFileItemStateChanged
    boolean b=jRBExpFile.isSelected();
    jTFExpFile.setEnabled(b);
    jBtnSelExpFile.setEnabled(b);
}//GEN-LAST:event_jRBExpFileItemStateChanged

private void jBtnSelExpFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelExpFileActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Indicate the target file");
        FC.setCurrentDirectory(new File(RSC.getDir("export")));
        FC.setDialogType(JFileChooser.SAVE_DIALOG);
        FC.setFileFilter(new FFilter("_ALL", "All files"));
        RSC.setComponentFont(FC.getComponents());
        if (FC.showSaveDialog(this) != JFileChooser.CANCEL_OPTION) {
            RSC.rememberDir("export", FC);
            this.jTFExpFile.setText(FC.getSelectedFile().getAbsolutePath());
        }
}//GEN-LAST:event_jBtnSelExpFileActionPerformed

private void jBtnExpSelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnExpSelActionPerformed
        int plugin=jCBExpFilter.getSelectedIndex();
        if (plugin == -1) {
            return;
        }
        ItemTable DT=RSC.getCurrentItemTable();
        if (DT == null) return;
        setThreadMsg("Exporting...");
        ArrayList<Item> docs=DT.getSelectedItems();
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "Exporting selected documents ...", "", 0, docs.size());
        String n=(String)jCBExpFilter.getSelectedItem();
        ThreadExport TE;
        TE = (new ThreadExport(DT.Lib, RSC, Msg1, progressMonitor, RSC.Plugins.get(n), RSC.Plugins.parameters.get(n), docs,jRBExpClip.isSelected(),jTFExpFile.getText()));
        TE.start();
}//GEN-LAST:event_jBtnExpSelActionPerformed

private void jBtnExpAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnExpAllActionPerformed
        int plugin=jCBExpFilter.getSelectedIndex();
        if (plugin == -1) {
            return;
        }
        Library Lib=RSC.getCurrentSelectedLib();
        setThreadMsg("Exporting...");
        ProgressMonitor progressMonitor = new ProgressMonitor(this, "Exporting documents in library ...", "", 0, Lib.getSize());
        String n=(String)jCBExpFilter.getSelectedItem();
        ThreadExport TE;
        TE = (new ThreadExport(Lib, RSC, Msg1, progressMonitor, RSC.Plugins.get(n), RSC.Plugins.parameters.get(n), null,jRBExpClip.isSelected(),jTFExpFile.getText()));
        TE.start();
}//GEN-LAST:event_jBtnExpAllActionPerformed

private void jMIEditLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditLocationActionPerformed
    (new EditMyLocation(this,RSC)).setVisible(true);    
}//GEN-LAST:event_jMIEditLocationActionPerformed

private void jMIToCurrentLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIToCurrentLocActionPerformed
    ArrayList<Item> docs=RSC.getCurrentItemTable().getSelectedItems();
    if (docs.isEmpty()) {
        toolbox.Warning(this,"No item selected!", "Warning:");
        return;
    }
    if (!docs.get(0).Lib.IndexTags.contains("lat")) {
        toolbox.Warning(this,"Item doesn't contain information on its location!", "Warning:");
        return;
    }
    RSC.mylat=Double.valueOf(docs.get(0).getS("lat"));
    RSC.mylon=Double.valueOf(docs.get(0).getS("lon"));
    JM.updateDTs();
}//GEN-LAST:event_jMIToCurrentLocActionPerformed

private void jMIRemoveHalfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIRemoveHalfActionPerformed
        if (isTabAvailable) {
            ItemTable DT = RSC.ItemTables.get(jTPTabList.getSelectedIndex());
            Library CSL = DT.Lib;
            boolean doit=false;
            int h=0;
            for (Item doc : DT.getSelectedItems()) {
                if (!doit) h=toolbox.QuestionABCD(this,"Delete the document \n" + doc.toText() + ",\nkeeping the associated file?",
                                "Warning","Yes","No","Yes to all","Cancel");
                if (h==3) break;
                if (h==2) doit=true;
                if (doit || (h == 0)) {
                    DT.removeItem(doc);
                    doc.removeFromLib(false);
                }
            }
            if (CSL.hasChanged()) {
                updateStatusBar(true);
            }
            jIP.restoreCreationType();
        }
}//GEN-LAST:event_jMIRemoveHalfActionPerformed

private void jTFSearchCategoriesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTFSearchCategoriesKeyTyped
    String srch = jTFSearchCategories.getText();
    StructureNode DMTN;
    if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
        StructureNode TN = (StructureNode) jTStructureTree.getLastSelectedPathComponent();
        DMTN = RSC.getCurrentSelectedLib().Structure.nextOccurence(srch.toLowerCase(), TN);
        if (!(DMTN == null)) {
            jTStructureTree.setSelectionPath(new TreePath(DMTN.getPath().toArray()));
            jTStructureTree.scrollPathToVisible(new TreePath(DMTN.getPath().toArray()));
            RSC.getCurrentSelectedLib().Structure.goTo(DMTN);
            updateTableByCategory();
            updateRulesByCategory();
            noDocSelected();
        }
    }
}//GEN-LAST:event_jTFSearchCategoriesKeyTyped

private void jTFMainSearchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTFMainSearchKeyTyped
    if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
        if (jTFSearchstate == 1) {
            if ((isTabAvailable) && (RSC.getCurrentItemTable().jtable.getModel().getRowCount() > 0)) {
                RSC.getCurrentItemTable().jtable.clearSelection();
                RSC.getCurrentItemTable().jtable.setRowSelectionInterval(0, 0);
                JM.ViewSelected(null);
            }
        } else {
            stopSearch();
            int mode=0;
            if (jRBSearchMeta.isSelected()) mode=1;
            if (jRBSearchDeep.isSelected()) mode=2;
            if (jTFMainSearch.getText().equals("")) return;
            if (RSC.getCurrentItemTable()!=null)
                if (!RSC.getCurrentItemTable().tableview) return;
            String srch = jTFMainSearch.getText();
            if ((srch.length() > 0) && (!srch.equals(jTFMainSearch.getDefaultText()))) {
                noDocSelected();
                //System.out.println(String.valueOf(System.currentTimeMillis())+"Request send.");
                startSearch(srch,mode);
            }
            jTFSearchstate = 1;
        }
    } else {
        jTFSearchstate=0;
    }
}//GEN-LAST:event_jTFMainSearchKeyTyped

private void jMCDisplayHiddenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jMCDisplayHiddenStateChanged
    RSC.displayHidden=jMCDisplayHidden.isSelected();
}//GEN-LAST:event_jMCDisplayHiddenStateChanged

private void jMShowCombinedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMShowCombinedActionPerformed
    JM.showCombined();
}//GEN-LAST:event_jMShowCombinedActionPerformed

private void jMShowLinkedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMShowLinkedActionPerformed
    JM.showLinksOfType("Available Links");
}//GEN-LAST:event_jMShowLinkedActionPerformed

private void jMIExpandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIExpandActionPerformed
    JM.expandAll(jTStructureTree, true);
}//GEN-LAST:event_jMIExpandActionPerformed

private void jMICollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMICollapseActionPerformed
    JM.expandAll(jTStructureTree, false);
}//GEN-LAST:event_jMICollapseActionPerformed

    private void jMIEditCatTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditCatTreeActionPerformed
        Library Lib = RSC.getCurrentSelectedLib();
        String source = Lib.Structure.source;
        String tmp = TextFile.ReadOutFile(source);
        boolean entering = true;
        boolean cancelled = false;
        while (!cancelled && (invalid(entering, tmp))) {
            if (!entering) {
                toolbox.Warning(this, "The set of categories you entered is not valid.", "Warning:");
            }
            entering = false;
            MultiLineEditor DMLE = new MultiLineEditor(RSC, "Edit registration rules", tmp);
            DMLE.setVisible(true);
            tmp = DMLE.text;
            cancelled = DMLE.cancel;
        }
        if (!cancelled) {
            try {
                TextFile TF = new TextFile(source, false);
                TF.putString(tmp);
                TF.close();
                Lib.Structure = new XMLTree(source, "/title/");
                StructureTreeModel.setRoot(Lib.Structure.Root);
            } catch (Exception e) {
                RSC.Msg1.printStackTrace(e);
            }
        }
    }//GEN-LAST:event_jMIEditCatTreeActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem JMEditData;
    private javax.swing.JMenuItem JMIManual;
    private javax.swing.ButtonGroup bGSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jBMPlugins;
    private javax.swing.JButton jBTNApplyToAll;
    private javax.swing.JButton jBtnApplyPluginSelDoc;
    private javax.swing.JButton jBtnClrSrchAuthors;
    private javax.swing.JButton jBtnClrSrchCat;
    private javax.swing.JButton jBtnClrSrchKey;
    private javax.swing.JButton jBtnEditRules;
    private javax.swing.JButton jBtnExpAll;
    private javax.swing.JButton jBtnExpSel;
    private javax.swing.JButton jBtnSelExpFile;
    public javax.swing.JComboBox jCBExpFilter;
    public javax.swing.JComboBox jCBLibraries;
    private javax.swing.JDialog jDialog1;
    public javax.swing.JList jLPeopleLong;
    public javax.swing.JList jLPlugins;
    private javax.swing.JList jLSearchKeys;
    public javax.swing.JList jLSearchPeople;
    private javax.swing.JLabel jLStatusBar;
    private javax.swing.JLabel jLThreadStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    public javax.swing.JMenu jMActions;
    private javax.swing.JMenu jMBibTeX;
    private javax.swing.JRadioButtonMenuItem jMCDisplayHidden;
    private javax.swing.JMenu jMCategories;
    public javax.swing.JMenu jMCopyToDiff;
    public javax.swing.JMenu jMCopyToDiff1;
    public javax.swing.JMenu jMDoc;
    private javax.swing.JMenu jMFile;
    private javax.swing.JMenu jMHelp;
    private javax.swing.JMenuItem jMIAbout;
    private javax.swing.JMenuItem jMIAddTab;
    private javax.swing.JMenuItem jMIAddToLib;
    private javax.swing.JMenuItem jMIAnnotatePDF;
    private javax.swing.JMenuItem jMIAnnotatePDF1;
    public javax.swing.JMenuItem jMIAssociateFile;
    public javax.swing.JMenuItem jMIAssociateFile1;
    private javax.swing.JMenuItem jMIAutoregister;
    private javax.swing.JMenuItem jMIBibClipboard;
    private javax.swing.JMenuItem jMIBibOutClipboard;
    private javax.swing.JMenuItem jMICatDown;
    private javax.swing.JMenuItem jMICatDown1;
    private javax.swing.JMenuItem jMICatSub;
    private javax.swing.JMenuItem jMICatSub1;
    private javax.swing.JMenuItem jMICatSuper;
    private javax.swing.JMenuItem jMICatSuper1;
    private javax.swing.JMenuItem jMICatUp;
    private javax.swing.JMenuItem jMICatUp1;
    private javax.swing.JMenuItem jMICheckBib;
    private javax.swing.JMenuItem jMICitationTagClipboard;
    private javax.swing.JMenuItem jMICites;
    private javax.swing.JMenuItem jMICites1;
    private javax.swing.JMenuItem jMICloseLib;
    public javax.swing.JMenuItem jMICloseTab;
    public javax.swing.JMenuItem jMICloseTab2;
    private javax.swing.JMenuItem jMICollapse;
    private javax.swing.JMenuItem jMIConfig;
    private javax.swing.JMenuItem jMICopyTab;
    private javax.swing.JMenuItem jMICopyTab2;
    private javax.swing.JMenuItem jMICreateBib;
    private javax.swing.JMenuItem jMICreateCombiner;
    private javax.swing.JMenuItem jMICreateCombiner1;
    private javax.swing.JMenuItem jMICreateLib;
    private javax.swing.JMenuItem jMICreateLit;
    private javax.swing.JMenuItem jMICreateTxt;
    private javax.swing.JMenuItem jMIDeepSearch;
    private javax.swing.JMenuItem jMIDelAllAutoReg;
    private javax.swing.JMenuItem jMIDelAllReg;
    private javax.swing.JMenuItem jMIDelCat;
    private javax.swing.JMenuItem jMIDelCat1;
    private javax.swing.JMenuItem jMIDelReg;
    private javax.swing.JMenuItem jMIDelReg1;
    private javax.swing.JMenuItem jMIDeleteFile;
    private javax.swing.JMenuItem jMIDeleteFile1;
    private javax.swing.JMenuItem jMIDeleteLib;
    private javax.swing.JMenuItem jMIEditCatTree;
    public javax.swing.JMenuItem jMIEditDS;
    private javax.swing.JMenuItem jMIEditData1;
    private javax.swing.JMenuItem jMIEditLib;
    private javax.swing.JMenuItem jMIEditLibTemplates;
    private javax.swing.JMenuItem jMIEditLocation;
    private javax.swing.JMenuItem jMIEditRules;
    private javax.swing.JMenuItem jMIEmail;
    private javax.swing.JMenuItem jMIEmail1;
    private javax.swing.JMenuItem jMIExpand;
    private javax.swing.JMenuItem jMIExportTab;
    private javax.swing.JMenuItem jMIExportTab1;
    private javax.swing.JMenuItem jMIFixReg;
    private javax.swing.JMenuItem jMIFixReg1;
    private javax.swing.JMenuItem jMIInsertCat;
    private javax.swing.JMenuItem jMIInsertCat1;
    private javax.swing.JMenuItem jMIJoin;
    private javax.swing.JMenuItem jMIJoin1;
    private javax.swing.JMenuItem jMILoadLib;
    private javax.swing.JMenuItem jMIMPlugins1;
    private javax.swing.JMenuItem jMINew;
    private javax.swing.JMenuItem jMIOpenAnnotation;
    private javax.swing.JMenuItem jMIOpenAnnotation1;
    private javax.swing.JMenuItem jMIOpenNewTab;
    private javax.swing.JMenuItem jMIPluginInfo;
    private javax.swing.JMenuItem jMIQuit;
    private javax.swing.JMenuItem jMIReExtract1;
    public javax.swing.JMenuItem jMIReduceDR;
    public javax.swing.JMenuItem jMIReduceDR1;
    public javax.swing.JMenuItem jMIReloadDisplayString2;
    private javax.swing.JMenuItem jMIReloadPlugins1;
    private javax.swing.JMenuItem jMIRemoveFromTab;
    private javax.swing.JMenuItem jMIRemoveFromTab1;
    private javax.swing.JMenuItem jMIRemoveHalf;
    private javax.swing.JMenuItem jMIRemoveHalf1;
    private javax.swing.JMenuItem jMIRenameCat;
    private javax.swing.JMenuItem jMIRenameCat1;
    private javax.swing.JMenuItem jMISaveLib;
    private javax.swing.JMenuItem jMISetupER;
    private javax.swing.JMenuItem jMIShowCitedinFile;
    private javax.swing.JMenuItem jMISynch;
    private javax.swing.JMenuItem jMISynchER;
    private javax.swing.JMenuItem jMITab2Cat;
    private javax.swing.JMenuItem jMITab2Cat2;
    private javax.swing.JMenuItem jMIToCurrentLoc;
    private javax.swing.JMenuItem jMIToCurrentLoc1;
    private javax.swing.JMenuItem jMIUnregisterDoc;
    private javax.swing.JMenuItem jMIUnregisterDoc1;
    private javax.swing.JMenuItem jMIUpdate;
    private javax.swing.JMenuItem jMIView;
    private javax.swing.JMenuItem jMIView1;
    public javax.swing.JMenuItem jMIViewPlain;
    public javax.swing.JMenuItem jMIViewPlain1;
    private javax.swing.JMenu jMLibraries;
    public javax.swing.JMenu jMRecent;
    private javax.swing.JMenu jMRegistration;
    private javax.swing.JMenu jMShow;
    private javax.swing.JMenu jMShow1;
    private javax.swing.JMenuItem jMShowCombined;
    private javax.swing.JMenuItem jMShowCombined1;
    private javax.swing.JMenuItem jMShowLinked;
    private javax.swing.JMenuItem jMShowLinked1;
    private javax.swing.JMenu jMTools;
    private javax.swing.JMenuBar jMainMenu;
    public javax.swing.JProgressBar jPBSearch;
    private javax.swing.JPopupMenu jPMCategories;
    public javax.swing.JPopupMenu jPMDocList;
    public javax.swing.JPopupMenu jPMDocuments;
    private javax.swing.JPopupMenu jPMPlugins;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JRadioButton jRBExpClip;
    private javax.swing.JRadioButton jRBExpFile;
    private javax.swing.JRadioButton jRBSearchDeep;
    private javax.swing.JRadioButton jRBSearchIndex;
    private javax.swing.JRadioButton jRBSearchMeta;
    private javax.swing.JSplitPane jSPMain;
    private javax.swing.JSplitPane jSPMain3;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JSeparator jSeparator24;
    private javax.swing.JSeparator jSeparator25;
    private javax.swing.JSeparator jSeparator26;
    private javax.swing.JSeparator jSeparator27;
    private javax.swing.JSeparator jSeparator28;
    private javax.swing.JSeparator jSeparator29;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator30;
    private javax.swing.JPopupMenu.Separator jSeparator31;
    private javax.swing.JPopupMenu.Separator jSeparator32;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton jTBAdd;
    private javax.swing.JButton jTBSearch;
    private javax.swing.JTextField jTFExpFile;
    public celsius.jExtTextField jTFMainSearch;
    public celsius.jExtTextField jTFSearchAuthors;
    private celsius.jExtTextField jTFSearchCategories;
    private celsius.jExtTextField jTFSearchKey;
    public javax.swing.JTabbedPane jTPSearches;
    public javax.swing.JTabbedPane jTPTabList;
    private javax.swing.JTabbedPane jTPTechnical;
    private javax.swing.JTree jTRegistrationTree;
    private javax.swing.JTree jTStructureTree;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }
    
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }
    
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
    
    @Override
    public void dragExit(DropTargetEvent dte) {
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        final Point p = dtde.getLocation();
        if (jTStructureTree.getPathForLocation(p.x,p.y)==null) {
            toolbox.Warning(this, "Could not find category to drop into. Cancelling...", "Warning");
        } else {
            StructureNode TN = (StructureNode) ((jTStructureTree.getPathForLocation(p.x,p.y)).getLastPathComponent());
            Library Lib = RSC.getCurrentSelectedLib();
            Lib.Structure.goTo(TN);
            ItemTable DT = RSC.getCurrentItemTable();
            if (DT != null) {
                for (Item doc : DT.getSelectedItems()) {
                    String tmp = doc.get("registered");
                    if ((tmp == null) || (!Parser.EnumContains(tmp, Lib.Structure.get("title").trim()))) {
                        if ((tmp == null) || (tmp.length() == 0)) {
                            tmp = Lib.Structure.get("title").trim();
                        } else {
                            tmp += "|" + Lib.Structure.get("title").trim();
                        }
                        doc.put("registered", tmp);
                        Lib.setChanged(true);
                        jIP.updateRawData();
                        jIP.updateHTMLview();
                        updateStatusBar(false);
                    }
                }
            }
        }
    }
    
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
    
    @Override
    public void treeNodesInserted(TreeModelEvent e) {
    }
    
    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
    }
    
    @Override
    public void treeStructureChanged(TreeModelEvent e) {
    }

    public void treeNodesChanged(TreeModelEvent e) {
    }

    public void startSearch(String srch,int mode) {
        //System.out.println(String.valueOf(System.currentTimeMillis())+"Search Requested: "+srch);
        ItemTable IT=RSC.makeTabAvailable(5, srch,"magnifier");
        Library Lib=RSC.getCurrentSelectedLib();
        jIP.setLibrary(Lib);
        stopSearch();
        threadSearch=new ThreadSearch(this,Lib,IT,srch,mode);
        threadSearch.start();
        //System.out.println(String.valueOf(System.currentTimeMillis())+"Search Requested done: "+srch+" :: "+threadSearch.toString());
    }

    public void stopSearch() {
        if (threadSearch!=null)
            if (threadSearch.running) {
                //System.out.println(String.valueOf(System.currentTimeMillis())+"Stopping: "+threadSearch.toString());
                threadSearch.interrupt();
            }
    }

    private void displayAddDocDialog() {
        AddItems DA=new AddItems(this,RSC);
        DA.setVisible(true);
        if (DA.addedItems.size()>0) {
            ItemTable CDT=RSC.makeNewTabAvailable(8, "Last added","magnifier");
            for (Item doc : DA.addedItems)
                CDT.addItemFast(doc);
            CDT.resizeTable(true);
            for (ItemTable DT : RSC.ItemTables) {
                DT.refresh();
            }
            jIP.updateHTMLview();
            jIP.updateRawData();
            updateStatusBar(true);
        }
    }

    public void searchKeysUpdate() {
        if (this.jLSearchKeys.getModel().getSize()==0) return;
        int i = jLSearchKeys.getSelectedIndex();
        if (i == -1) {
            return;
        }
        String key = (String) jLSearchKeys.getSelectedValue();
        ItemTable IT=RSC.makeTabAvailable(6, key,"book_key");
        RSC.getCurrentSelectedLib().showDocsKey(key, IT);
    }

    private void showSearchDialog() {
        if (deepSearch==null) {
            deepSearch=new DeepSearch(this);
        }
        deepSearch.setLib(RSC.getCurrentSelectedLib());
        deepSearch.setVisible(true);
    }

    public void listPersons(String srch) {
        DefaultListModel DLM = new DefaultListModel();
        if (srch.length() > 0) {
            for (String a1 : RSC.getCurrentSelectedLib().PeopleList) {
                if (a1.toLowerCase().equals(srch)) {
                    DLM.addElement(a1);
                }
            }
        }
        jLSearchPeople.setModel(DLM);
        jLSearchPeople.setSelectedIndex(0);
        JM.searchPeopleUpdate(false);
    }

    private boolean invalid(boolean entering, String tmp) {
        if (entering) return(true);
        boolean valid=true;
        try {
            TextFile TF=new TextFile("tree.tmp.$$$",false);
            TF.putString(tmp);
            TF.close();
            try {
                XMLTree TestTree=new XMLTree("tree.tmp.$$$","$full");
            } catch(Exception e) {
                valid=false;
            }
            TextFile.Delete("tree.tmp.$$$");
        } catch (Exception e) {
            RSC.Msg1.printStackTrace(e);
            valid=false;
        }
        return(!valid);
    }

    public void DoMainSearch() {
        stopSearch();
        int mode=0;
        if (jRBSearchMeta.isSelected()) mode=1;
        if (jRBSearchDeep.isSelected()) mode=2;
        if (jTFMainSearch.getText().equals("")) return;
        jTFSearchstate = 0;
        if (RSC.getCurrentItemTable()!=null)
            if (!RSC.getCurrentItemTable().tableview) return;
        if (mode>0) return;
        String srch = jTFMainSearch.getText();
        if ((srch.length() > 0) && (!srch.equals(jTFMainSearch.getDefaultText()))) {
            noDocSelected();
            //System.out.println(String.valueOf(System.currentTimeMillis())+"Request send.");
            startSearch(srch,mode);
        }
    }

    public void DoPeopleSearch() {
        String srch = jTFSearchAuthors.getText().toLowerCase();
        DefaultListModel DLM = new DefaultListModel();
        if ((srch.length() > 0)) {
            for (String a1 : RSC.getCurrentSelectedLib().PeopleList) {
                if (a1.toLowerCase().indexOf(srch) > -1) {
                    DLM.addElement(a1);
                }
            }
        }
        jLSearchPeople.setModel(DLM);
        jLPeopleLong.setModel(new DefaultListModel());
    }

    public void DoCategoriesSearch() {
        String srch = jTFSearchCategories.getText();
        if (srch.length() > 0) {
            StructureNode DMTN;
            DMTN = RSC.getCurrentSelectedLib().Structure.nextOccurence(srch.toLowerCase(),null);
            if (!(DMTN == null)) {
                jTStructureTree.setSelectionPath(new TreePath(DMTN.getPath().toArray()));
                jTStructureTree.scrollPathToVisible(new TreePath(DMTN.getPath().toArray()));
                RSC.getCurrentSelectedLib().Structure.goTo(DMTN);
                updateTableByCategory();
                updateRulesByCategory();
                noDocSelected();
            }
        }
    }

    public void DoKeySearch() {
        String srch = jTFSearchKey.getText();
        DefaultListModel DLM = new DefaultListModel();
        if (srch.length() > 0) {
            for (String a1 : RSC.getCurrentSelectedLib().KeywordList) {
                if (a1.toLowerCase().indexOf(srch) > -1) {
                    DLM.addElement(a1);
                }
            }
        }
        jLSearchKeys.setModel(DLM);
    }

    public void keyPressed(DocumentEvent e) {
        if (e.getDocument().equals(jTFMainSearch.getDocument())) DoMainSearch();
        if (e.getDocument().equals(jTFSearchAuthors.getDocument())) DoPeopleSearch();
        if (e.getDocument().equals(jTFSearchCategories.getDocument())) DoCategoriesSearch();
        if (e.getDocument().equals(jTFSearchKey.getDocument())) DoKeySearch();
    }

    public void insertUpdate(DocumentEvent e) {
        keyPressed(e);
    }

    public void removeUpdate(DocumentEvent e) {
        keyPressed(e);
    }

    public void changedUpdate(DocumentEvent e) {
        keyPressed(e);
    }
}


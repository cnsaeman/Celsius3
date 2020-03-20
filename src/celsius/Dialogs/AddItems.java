/*
 * Celsius v2
 *
 * DialogAdd.java
 *
 * Created on 19.03.2010, 18:19:12
 */
package celsius.Dialogs;

import celsius.*;
import celsius.Threads.ThreadApplyPlugin;
import celsius.Threads.ThreadGetDetails;
import celsius.Threads.ThreadImport;
import celsius.tools.FFilter;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.TableColumn;

/**
 *
 * @author cnsaeman
 */
public class AddItems extends javax.swing.JDialog implements ActionListener {

    private final String TI = "DIF>";             // Protocol header
    private final Library Lib;
    private final ArrayList<Item> items;
    public final ArrayList<Item> addedItems;
    private final ArrayList<ThreadGetDetails> GetDetailsThreads;
    private int currentDoc;
    public final Resources RSC;

    private final ThreadPoolExecutor TPE;
    private final LinkedBlockingQueue<Runnable> LBQ;

    private KeyValueTableModel KVTM;

    private boolean initializing;

    private int queuesize;
    private final Timer timer;

    private int importMode;

    private Thread DoIt;

    private JList currentList;

    private boolean cancelAdding, autoDelete, autoReplace;

    /** Creates new form DialogAdd */
    public AddItems(MainFrame mf, Resources rsc) {
        super(mf, true);
        initializing = true;
        RSC = rsc;
        Lib = RSC.getCurrentSelectedLib();
        setIconImage(RSC.getOriginalIcon("book_add"));
        items = new ArrayList<Item>();
        addedItems = new ArrayList<Item>();
        GetDetailsThreads = new ArrayList<ThreadGetDetails>();
        LBQ=new LinkedBlockingQueue<Runnable>();
        TPE=new ThreadPoolExecutor(5, 5, 500L, TimeUnit.DAYS,LBQ);
        initComponents();
        currentList=jLstFileList;
        jTPane.setTabComponentAt(0,new TabLabel("Add files in a folder as items","folder",RSC,null,false));
        jTPane.setTabComponentAt(1,new TabLabel("Add single file as an item","default",RSC,null,false));
        jTPane.setTabComponentAt(2,new TabLabel("Add a manually entered item","application_edit",RSC,null,false));
        jTPane.setTabComponentAt(3,new TabLabel("Import items from file","lightning_add",RSC,null,false));
        RSC.SM.register(this, "folder", new JComponent[] { jBtnView, jBtnDrop, jBtnDelete });
        RSC.SM.register(this, "folder2", new JComponent[] { jBtnImpRec, jBtnImpDrop, jBtnImpDoub });
        Library Lib=RSC.getCurrentSelectedLib();
        setTitle("Add new items to active library " + Lib.name);
        if (Lib.MainFile.isKeySet("default-add-method"))
            if (Lib.MainFile.get("default-add-method").equals("doNotMove"))
                jCBMove.setSelected(false);
        jTFFolder.setText(RSC.getDir("toinclude"));
        jLPlugins.setModel(RSC.Plugins.getPluginsDLM("manual",RSC.getCurrentSelectedLib()));

        // Hide stuff
        DefaultComboBoxModel DCBM=new DefaultComboBoxModel();
        if (!Lib.Hide.contains("Import:BibTeX")) DCBM.addElement("BibTeX");
        if (!Lib.Hide.contains("Import:zipped gpx-file")) DCBM.addElement("zipped gpx-file");
        jCBImport.setModel(DCBM);

        toInitState();
        toolbox.centerDialog(this,mf);
        initializing = false;
        ThreadStatus.setText("No item selected. No threads running.");
        timer=new Timer(500,this);
        timer.start();
    }

    private void toInitState() {
        currentDoc = -1;
        RSC.SM.switchOff(this, "folder");
        RSC.SM.switchOff(this, "folder2");
        jBtnAddRec.setEnabled(false);
        jBtnFindDoublettes.setEnabled(false);
        items.clear();
        jLstFileList.setModel(new DefaultListModel());
        jImpList.setModel(new DefaultListModel());
        jTFFileNameImp.setText("");
        jBtnImport.setEnabled(false);
        jTABibTeX.setText("");
        updateDocInfo();
    }

    private void viewDocument() {
        String fn = items.get(currentDoc).get("location");
        String ft = items.get(currentDoc).get("filetype");
        if ((fn != null) && (ft != null)) {
            RSC.Configuration.view(ft, fn);
        }
        jLstFileList.grabFocus();
    }

    private Item createDoc() {
        Item doc = new Item();
        for (String tag : Lib.IndexTags) {
            if (!tag.equals("addinfo") && !tag.equals("autoregistered") && !tag.equals("registered") && !tag.equals("id")) {
                doc.put(tag, null);
            }
        }
        if (Lib.MainFile.get("standardfields")!=null) 
            for (String tag : Lib.listOf("standardfields"))
                doc.put(tag, null);
        return (doc);
    }

    private void createDocFromFile(String s) {
        String ft = TextFile.getFileType(s);
        if (RSC.Configuration.supportedFileTypes().indexOf(ft) == -1) {
            return;
        }
        String sft = Lib.MainFile.get("filetypes");
        if (!sft.equals("*")) {
            if (!Parser.EnumContains(sft, ft)) {
                return;
            }
        }
        Item doc = createDoc();
        doc.put("location", s);
        doc.put("filetype", ft);
        items.add(doc);
        if (jTPane.getSelectedIndex()==0) {
            if (s.length() > 52) {
                s = s.substring(0, 20) + "..." + s.substring(s.length() - 29);
            }
            ((DefaultListModel) jLstFileList.getModel()).addElement(s);
        }
    }

    public void applyChanges() {
        Item doc = items.get(currentDoc);
        for (int i = 0; i < KVTM.getRowCount(); i++) {
            String k=((String) KVTM.getValueAt(i, 0)).trim().toLowerCase();
            String v=((String) KVTM.getValueAt(i, 1)).trim();
            if (k.length()>0)
                if ((!v.equals("")) && (!v.equals("<unknown>"))) {
                    doc.put(k,v);
                } else {
                    doc.put(k,null);
                }
        }
        updateDocInfo();
    }

    private void AddFilesFrom(String dir) {
        if ((new File(dir)).isDirectory()) {
            String[] flist = (new File(dir)).list();
            java.util.Arrays.sort(flist);
            for (int i = 0; (i < flist.length); i++) {
                if ((new File(dir + toolbox.filesep + flist[i])).isDirectory()) {
                    AddFilesFrom(dir + toolbox.filesep + flist[i]);
                } else {
                    String fn=dir + toolbox.filesep + flist[i];
                    boolean found=false;
                    for (Item item : Lib)
                        if (item.getCompleteDirS("location").equals(fn)) {
                            found=true;
                            break;
                        }
                    if (!found) {
                        try {
                            String tmp=RSC.Configuration.correctFileType(fn);
                            if (!tmp.equals(fn))
                                (new File(fn)).renameTo(new File(tmp));
                            createDocFromFile(tmp);
                        } catch(Exception e) {
                            RSC.Msg1.printStackTrace(e);
                        }
                    }
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTPane = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jBtnStart = new javax.swing.JButton();
        jTFFolder = new javax.swing.JTextField();
        jBtnSelectFolder = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTFirstPage = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLstFileList = new javax.swing.JList();
        jPanel15 = new javax.swing.JPanel();
        jBtnAddRec = new javax.swing.JButton();
        jBtnView = new javax.swing.JButton();
        jBtnDrop = new javax.swing.JButton();
        jBtnDelete = new javax.swing.JButton();
        jBtnFindDoublettes = new javax.swing.JButton();
        ThreadStatus = new javax.swing.JTextArea();
        jCBPlugins = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jTFFile = new javax.swing.JTextField();
        jBtnSelectFile = new javax.swing.JButton();
        jBtnFileOK = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTAFileText = new javax.swing.JTextArea();
        jBtnView2 = new javax.swing.JButton();
        jCBPlugins2 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTABibTeX = new javax.swing.JTextArea();
        jPanel16 = new javax.swing.JPanel();
        jBtnCreateEmpty = new javax.swing.JButton();
        jBtnNormalize1 = new javax.swing.JButton();
        jCBAddProperty = new javax.swing.JComboBox();
        jBtnAdd1 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jBtnCreateManualEntry = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jBtnDoneBib = new javax.swing.JButton();
        jTFBarcode = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jCBImport = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTFFileNameImp = new javax.swing.JTextField();
        jBtnChooseFile = new javax.swing.JButton();
        jBtnImpRec = new javax.swing.JButton();
        jBtnImpDrop = new javax.swing.JButton();
        jBtnImpDoub = new javax.swing.JButton();
        jBtnImport = new javax.swing.JButton();
        jPB1 = new javax.swing.JProgressBar();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jImpList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTInfo = new javax.swing.JTable();jTInfo.setRowHeight(RSC.guiScale(24));
        jPanel17 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jBtnNewTag1 = new javax.swing.JButton();
        jBtnAdd = new javax.swing.JButton();
        jBtnApply = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jLPlugins = new javax.swing.JList();
        jBtnNormalize = new javax.swing.JButton();
        jBtnNewTag = new javax.swing.JButton();
        jBtnApplyPlugin = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTFprereg = new javax.swing.JTextField();
        jBtnClrPreReg = new javax.swing.JButton();
        jBtnChooseCat = new javax.swing.JButton();
        jCBMove = new javax.swing.JCheckBox();
        jBtnFormatAuthors = new javax.swing.JButton();
        jBtnDone = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(RSC.guiScale(952), RSC.guiScale(725)));
        getContentPane().setLayout(new java.awt.GridLayout(2, 1));

        jTPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTPaneStateChanged(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel13.setLayout(new java.awt.BorderLayout(10, 0));

        jBtnStart.setMnemonic('s');
        jBtnStart.setText("Start");
        jBtnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnStartActionPerformed(evt);
            }
        });
        jPanel13.add(jBtnStart, java.awt.BorderLayout.WEST);
        jPanel13.add(jTFFolder, java.awt.BorderLayout.CENTER);

        jBtnSelectFolder.setText("Select Folder");
        jBtnSelectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelectFolderActionPerformed(evt);
            }
        });
        jPanel13.add(jBtnSelectFolder, java.awt.BorderLayout.EAST);

        jPanel2.add(jPanel13, java.awt.BorderLayout.NORTH);

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setOneTouchExpandable(true);

        jTFirstPage.setColumns(20);
        jTFirstPage.setRows(5);
        jScrollPane3.setViewportView(jTFirstPage);

        jSplitPane1.setRightComponent(jScrollPane3);

        jLstFileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLstFileListMouseClicked(evt);
            }
        });
        jLstFileList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jLstFileListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jLstFileList);

        jSplitPane1.setLeftComponent(jScrollPane2);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1142, Short.MAX_VALUE)
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1142, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 356, Short.MAX_VALUE)
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jPanel2.add(jPanel14, java.awt.BorderLayout.CENTER);

        jBtnAddRec.setText("Add recognized files");
        jBtnAddRec.setToolTipText("Add all documents with recognition=100 to the active library");
        jBtnAddRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAddRecActionPerformed(evt);
            }
        });

        jBtnView.setText("View");
        jBtnView.setToolTipText("Open selected document in external viewer");
        jBtnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnViewActionPerformed(evt);
            }
        });

        jBtnDrop.setMnemonic('d');
        jBtnDrop.setText("Drop");
        jBtnDrop.setToolTipText("Drop selected document from list");
        jBtnDrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDropActionPerformed(evt);
            }
        });

        jBtnDelete.setText("Delete");
        jBtnDelete.setToolTipText("Delete selected document with all associated files");
        jBtnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDeleteActionPerformed(evt);
            }
        });

        jBtnFindDoublettes.setText("Find doublettes");
        jBtnFindDoublettes.setPreferredSize(null);
        jBtnFindDoublettes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnFindDoublettesActionPerformed(evt);
            }
        });

        ThreadStatus.setColumns(20);
        ThreadStatus.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        ThreadStatus.setRows(5);
        ThreadStatus.setMinimumSize(new java.awt.Dimension(0, 0));
        ThreadStatus.setPreferredSize(null);

        jCBPlugins.setSelected(true);
        jCBPlugins.setText("Auto-Plugins");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jBtnAddRec)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnView)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnDrop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnFindDoublettes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ThreadStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBPlugins))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBtnAddRec)
                        .addComponent(jBtnView)
                        .addComponent(jBtnDrop)
                        .addComponent(jBtnDelete)
                        .addComponent(jBtnFindDoublettes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCBPlugins))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(ThreadStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel15, java.awt.BorderLayout.SOUTH);

        jTPane.addTab("Add files in a folder as items", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/folder.png")), jPanel2); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jBtnSelectFile.setText("Select File");
        jBtnSelectFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelectFileActionPerformed(evt);
            }
        });

        jBtnFileOK.setText("Read in file");
        jBtnFileOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnFileOKActionPerformed(evt);
            }
        });

        jTAFileText.setColumns(20);
        jTAFileText.setRows(5);
        jScrollPane5.setViewportView(jTAFileText);

        jBtnView2.setText("View");
        jBtnView2.setEnabled(false);
        jBtnView2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnView2ActionPerformed(evt);
            }
        });

        jCBPlugins2.setSelected(true);
        jCBPlugins2.setText("Auto-Plugins");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jTFFile, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnSelectFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnFileOK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnView2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBPlugins2)
                .addGap(119, 119, 119))
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1142, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTFFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSelectFile)
                    .addComponent(jBtnFileOK)
                    .addComponent(jBtnView2)
                    .addComponent(jCBPlugins2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
        );

        jTPane.addTab("Add a single file as an item", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/default.png")), jPanel3); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("BibTeX"));
        jPanel7.setLayout(new java.awt.BorderLayout());

        jTABibTeX.setColumns(20);
        jTABibTeX.setFont(new java.awt.Font("Monospaced", 0, RSC.guiScale(12)));
        jTABibTeX.setRows(5);
        jScrollPane7.setViewportView(jTABibTeX);

        jPanel7.add(jScrollPane7, java.awt.BorderLayout.CENTER);

        jPanel16.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        jBtnCreateEmpty.setText("Create empty BibTeX-record");
        jBtnCreateEmpty.setPreferredSize(null);
        jBtnCreateEmpty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCreateEmptyActionPerformed(evt);
            }
        });

        jBtnNormalize1.setText("Normalize");
        jBtnNormalize1.setPreferredSize(null);
        jBtnNormalize1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNormalize1ActionPerformed(evt);
            }
        });

        jCBAddProperty.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "add property", "author", "title", "journal", "volume", "year", "pages", "eprint", "note", "doi" }));
        jCBAddProperty.setPreferredSize(new java.awt.Dimension(RSC.guiScale(101),23));
        jCBAddProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAddPropertyActionPerformed(evt);
            }
        });

        jBtnAdd1.setText("Done editing");
        jBtnAdd1.setPreferredSize(null);
        jBtnAdd1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAdd1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addComponent(jBtnCreateEmpty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnNormalize1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBAddProperty, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnAdd1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(618, 618, 618))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jBtnCreateEmpty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jBtnNormalize1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jCBAddProperty, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jBtnAdd1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel7.add(jPanel16, java.awt.BorderLayout.SOUTH);

        jPanel5.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Empty Item", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jBtnCreateManualEntry.setText("Create Empty Record");
        jBtnCreateManualEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCreateManualEntryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jBtnCreateManualEntry)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jBtnCreateManualEntry)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(jPanel9, java.awt.BorderLayout.WEST);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Barcode"));

        jBtnDoneBib.setText("Apply Barcode Plugin");
        jBtnDoneBib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDoneBibActionPerformed(evt);
            }
        });

        jTFBarcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTFBarcodeKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTFBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 759, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBtnDoneBib)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnDoneBib)
                    .addComponent(jTFBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel5.add(jPanel8, java.awt.BorderLayout.SOUTH);

        jTPane.addTab("Add a manually entered item", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/bookmark_go.png")), jPanel5); // NOI18N

        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Source:");

        jLabel2.setText("File:");

        jTFFileNameImp.setEnabled(false);

        jBtnChooseFile.setText("Choose file");
        jBtnChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChooseFileActionPerformed(evt);
            }
        });

        jBtnImpRec.setText("Import all");
        jBtnImpRec.setEnabled(false);
        jBtnImpRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImpRecActionPerformed(evt);
            }
        });

        jBtnImpDrop.setText("Drop current");
        jBtnImpDrop.setEnabled(false);
        jBtnImpDrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImpDropActionPerformed(evt);
            }
        });

        jBtnImpDoub.setText("Remove exact doublettes");
        jBtnImpDoub.setEnabled(false);
        jBtnImpDoub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImpDoubActionPerformed(evt);
            }
        });

        jBtnImport.setText("Read in file");
        jBtnImport.setEnabled(false);
        jBtnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTFFileNameImp, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCBImport, 0, 215, Short.MAX_VALUE))
                            .addComponent(jLabel2)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(jBtnChooseFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                                .addComponent(jBtnImport))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                                .addComponent(jBtnImpRec)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jBtnImpDrop, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(jBtnImpDoub)
                        .addGap(49, 49, 49))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(jPB1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jCBImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTFFileNameImp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnChooseFile)
                    .addComponent(jBtnImport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 207, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnImpRec)
                    .addComponent(jBtnImpDrop))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnImpDoub)
                .addContainerGap())
        );

        jPanel6.add(jPanel11, java.awt.BorderLayout.WEST);

        jPanel12.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel12.setLayout(new java.awt.GridLayout(1, 0));

        jImpList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jImpListValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(jImpList);

        jPanel12.add(jScrollPane6);

        jPanel6.add(jPanel12, java.awt.BorderLayout.CENTER);

        jTPane.addTab("Import items from file", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/application_edit.png")), jPanel6); // NOI18N

        getContentPane().add(jTPane);
        jTPane.getAccessibleContext().setAccessibleName("");

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jTInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ));
        jScrollPane1.setViewportView(jTInfo);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel17.setPreferredSize(new java.awt.Dimension(RSC.guiScale(350),RSC.guiScale(490)));

        jBtnNewTag1.setText("Delete tag");
        jBtnNewTag1.setToolTipText("Remove currently selected tag");
        jBtnNewTag1.setPreferredSize(new java.awt.Dimension(RSC.guiScale(92),RSC.guiScale(24)));
        jBtnNewTag1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNewTag1ActionPerformed(evt);
            }
        });

        jBtnAdd.setMnemonic('a');
        jBtnAdd.setText("Add Item");
        jBtnAdd.setToolTipText("Add the currently selected document to the active library");
        jBtnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAddActionPerformed(evt);
            }
        });

        jBtnApply.setText("Apply changes");
        jBtnApply.setToolTipText("Apply the changes made to the information tags");
        jBtnApply.setPreferredSize(new java.awt.Dimension(RSC.guiScale(92),RSC.guiScale(24)));
        jBtnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyActionPerformed(evt);
            }
        });

        jLPlugins.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jLPluginsValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(jLPlugins);

        jBtnNormalize.setMnemonic('n');
        jBtnNormalize.setText("Normalize title");
        jBtnNormalize.setToolTipText("Correct upper/lowercase for title");
        jBtnNormalize.setPreferredSize(new java.awt.Dimension(RSC.guiScale(92),RSC.guiScale(24)));
        jBtnNormalize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNormalizeActionPerformed(evt);
            }
        });

        jBtnNewTag.setText("New tag");
        jBtnNewTag.setToolTipText("Add a new tag to the list");
        jBtnNewTag.setPreferredSize(new java.awt.Dimension(RSC.guiScale(92),RSC.guiScale(24)));
        jBtnNewTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNewTagActionPerformed(evt);
            }
        });

        jBtnApplyPlugin.setMnemonic('p');
        jBtnApplyPlugin.setText("Apply plugin");
        jBtnApplyPlugin.setToolTipText("Apply the selected plugin to the currently selected document");
        jBtnApplyPlugin.setEnabled(false);
        jBtnApplyPlugin.setPreferredSize(new java.awt.Dimension(RSC.guiScale(104),RSC.guiScale(24)));
        jBtnApplyPlugin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyPluginActionPerformed(evt);
            }
        });

        jLabel3.setText("Add items to the following categories:");

        jBtnClrPreReg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/celsius/images/closebtn.png"))); // NOI18N
        jBtnClrPreReg.setBorderPainted(false);
        jBtnClrPreReg.setContentAreaFilled(false);
        jBtnClrPreReg.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jBtnClrPreReg.setPreferredSize(new java.awt.Dimension(16, 17));
        jBtnClrPreReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClrPreRegActionPerformed(evt);
            }
        });

        jBtnChooseCat.setText("Choose");
        jBtnChooseCat.setPreferredSize(new java.awt.Dimension(RSC.guiScale(78),RSC.guiScale(24)));
        jBtnChooseCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChooseCatActionPerformed(evt);
            }
        });

        jCBMove.setSelected(true);
        jCBMove.setText("Move files to library's document folder");

        jBtnFormatAuthors.setText("Format people");
        jBtnFormatAuthors.setToolTipText("Add a new tag to the list");
        jBtnFormatAuthors.setPreferredSize(new java.awt.Dimension(RSC.guiScale(92),RSC.guiScale(24)));
        jBtnFormatAuthors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnFormatAuthorsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jTFprereg, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnClrPreReg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnChooseCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jBtnNewTag1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnNewTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnFormatAuthors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnNormalize, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnApply, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jBtnApplyPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jCBMove))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnApplyPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jBtnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnApply, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnNormalize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnFormatAuthors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnNewTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnNewTag1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jBtnClrPreReg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnChooseCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTFprereg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBMove))
        );

        jBtnDone.setMnemonic('d');
        jBtnDone.setText("Done");
        jBtnDone.setToolTipText("End dialog");
        jBtnDone.setMaximumSize(new java.awt.Dimension(640, 240));
        jBtnDone.setPreferredSize(new java.awt.Dimension(RSC.guiScale(64),RSC.guiScale(24)));
        jBtnDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap(348, Short.MAX_VALUE)
                .addComponent(jBtnDone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel17Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap(518, Short.MAX_VALUE)
                .addComponent(jBtnDone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel17Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(44, 44, 44)))
        );

        jPanel1.add(jPanel17, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnSelectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelectFolderActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Select the directory in which the files are located.");
        FC.setCurrentDirectory(new File(RSC.getDir("toinclude")));
        FC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        FC.setFileFilter(new FFilter("_DIR", "Folders"));
        RSC.setComponentFont(FC.getComponents());
        // cancelled?
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            jTFFolder.setText(FC.getSelectedFile().getAbsolutePath());
            RSC.rememberDir("toinclude", FC);
        }
    }//GEN-LAST:event_jBtnSelectFolderActionPerformed

    private void jBtnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDeleteActionPerformed
        deleteItem(items.get(currentDoc),false);
}//GEN-LAST:event_jBtnDeleteActionPerformed

    private void jBtnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnViewActionPerformed
        viewDocument();
}//GEN-LAST:event_jBtnViewActionPerformed

    private void jBtnFindDoublettesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnFindDoublettesActionPerformed
        ArrayList<Item> docs2=cloneDocs();
        boolean always=false;
        for (Item doc : docs2) {
            try {
                int i=Lib.Doublette(doc);
                if (i == 10) {
                    if (always) {
                        deleteItem(doc,true);
                    } else {
                        int j = toolbox.QuestionABCD(this, "Exact doublette found in current library.\nItem in library: " + Lib.marker.get("location") + "\nDelete the file " + doc.get("location") + "?", "Warning","Delete all exact doublettes","Delete this one","No","Cancel");
                        if (j == 0) {
                            always=true;
                            deleteItem(doc,true);
                        }
                        if (j == 1) {
                            deleteItem(doc,true);
                        }
                        if (j == 3)  {
                            return;
                        }
                    }
                }
                if (i == 5) {
                    int j = toolbox.QuestionYNC(this,"File with exactly the same size found in library:\n"+Lib.marker.get("location")+"\nDelete the file " + doc.get("location") + "?", "Warning");
                    if (j == JOptionPane.YES_OPTION) deleteItem(doc,false);
                    if (j == JOptionPane.CANCEL_OPTION) return;
                }
            } catch (Exception e) {
                RSC.Msg1.printStackTrace(e);
            }
        }
        if (always) toolbox.Information(this, "Finished deleting exact doublettes.", "Task completed");
}//GEN-LAST:event_jBtnFindDoublettesActionPerformed

    private void jBtnDropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDropActionPerformed
        removeFromTable(items.get(currentDoc));
    }//GEN-LAST:event_jBtnDropActionPerformed

    private void jBtnAddRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAddRecActionPerformed
        jBtnAdd.setEnabled(false);
        jBtnAddRec.setEnabled(false);
        final ArrayList<Item> docs2=cloneDocs();
        final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Adding fully recognized items ...", "", 0, 0);
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);
        final AddItems DA=this;
        cancelAdding=false;
        autoDelete=false;
        autoReplace=false;
        DoIt=(new Thread() {
            @Override
            public void run() {
                int c = 0;
                for (Item doc : docs2) {
                    int no=0;
                    for (Item doc2 : docs2) {
                        if (doc2.getS("recognition").equals("100"))
                            no++;
                    }
                    progressMonitor.setMaximum(no);
                    if (doc.getS("recognition").equals("100")) {
                        addItem(doc);
                        c++;
                        progressMonitor.setProgress(c);
                    }
                    if (cancelAdding) break;
                }
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run()  {
                        DA.adjustJBtnAdd();
                        DA.jBtnAddRec.setEnabled(true);
                    }
                });
            }
        });
        DoIt.start();
}//GEN-LAST:event_jBtnAddRecActionPerformed

    private void jBtnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAddActionPerformed
        jBtnAdd.setEnabled(false);
        jBtnAddRec.setEnabled(false);
        final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Adding item ...", "", 0, 1);
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);
        final AddItems DA=this;
        cancelAdding=false;
        autoDelete=false;
        autoReplace=false;
        DoIt=(new Thread() {
            @Override
            public void run() {
                addItem(items.get(currentDoc));
                progressMonitor.setProgress(1);
                DA.adjustJBtnAdd();
                DA.jBtnAddRec.setEnabled(true);
            }
        });
        DoIt.start();
}//GEN-LAST:event_jBtnAddActionPerformed

    private void jBtnNormalizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNormalizeActionPerformed
        applyChanges();
        String title = Parser.LowerEndOfWords(items.get(currentDoc).get("title"));
        title = title.replace('\n', ' ');
        title = title.replace("  ", " ");
        items.get(currentDoc).put("title", title);
        updateDocInfo();
}//GEN-LAST:event_jBtnNormalizeActionPerformed

    private void jBtnDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDoneActionPerformed
        LBQ.clear();
        TPE.shutdownNow();
        RSC.SM.unregister(this);
        this.setVisible(false);
        this.dispose();
}//GEN-LAST:event_jBtnDoneActionPerformed

    private void jLPluginsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jLPluginsValueChanged
        if (jLPlugins.isSelectionEmpty()) {
            jBtnApplyPlugin.setEnabled(false);
        } else {
            jBtnApplyPlugin.setEnabled(true);
        }
}//GEN-LAST:event_jLPluginsValueChanged

    private void jBtnApplyPluginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyPluginActionPerformed
        applyChanges();
        String pltitle=(String)jLPlugins.getSelectedValue();
        final ThreadApplyPlugin TAP=new ThreadApplyPlugin(null,RSC.Plugins.get(pltitle),RSC.Plugins.parameters.get(pltitle),RSC, items.get(currentDoc),false,true);
        jBtnApplyPlugin.setEnabled(false);
        SwingWorker worker = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() {
                TAP.start();
                try {
                    TAP.join();
                } catch (InterruptedException ex) {
                    RSC.Msg1.printStackTrace(ex);
                }
                return(null);
            }

            @Override
            protected void done() {
               try {
                jBtnApplyPlugin.setEnabled(true);
                updateDocInfo();
               } catch (Exception ignore) {
               }
            }

        };
        worker.execute();
}//GEN-LAST:event_jBtnApplyPluginActionPerformed

    private void jBtnNewTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNewTagActionPerformed
        KVTM.addRow("","<unknown>");
}//GEN-LAST:event_jBtnNewTagActionPerformed

    private void jBtnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyActionPerformed
        applyChanges();
}//GEN-LAST:event_jBtnApplyActionPerformed

    private void jBtnNewTag1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNewTag1ActionPerformed
        int i=jTInfo.getSelectedRow();
        String tag=((String)KVTM.getValueAt(i, 0)).toLowerCase();
        KVTM.removeRow(i);
        Item doc=items.get(currentDoc);
        if (doc.get("tag")!=null) doc.put(tag, null);
    }//GEN-LAST:event_jBtnNewTag1ActionPerformed

    private void jBtnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnStartActionPerformed
        jBtnAddRec.setEnabled(false);
        jBtnFindDoublettes.setEnabled(false);
        String dir = jTFFolder.getText();
        if (dir.endsWith(toolbox.filesep)) dir=dir.substring(0,dir.length()-1);
        String[] flist = (new File(dir)).list();
        if (flist==null) {
            toolbox.Warning(this,"The selected folder is empty.", "Warning");
            return;
        }
        String msg = "";
        for (int i = 0; (i < flist.length); i++) {
            if ((new File(dir + "/" + flist[i])).isDirectory()) {
                msg += flist[i] + "\n";
                if (Parser.HowOftenContains(msg, "\n") > 10) {
                    msg += "... and others.\n";
                    i = flist.length;
                }
            }
        }
        if (msg.length() != 0) {
            int i = toolbox.QuestionYN(this,"This folder contains the following subfolders, which will also be scanned:\n" + msg + "Are you sure about this?", "Warning:");
            if (i == JOptionPane.NO_OPTION) {
                return;
            }
        }
        items.clear();
        GetDetailsThreads.clear();
        jLstFileList.setModel(new DefaultListModel());

        currentDoc = -1;
        updateDocInfo();
        AddFilesFrom(dir);
        if (items.isEmpty()) {
            toolbox.Warning(this,"There are no files of supported type in the selected folder.", "Warning");
            return;
        }
        jBtnAddRec.setEnabled(true);
        jBtnFindDoublettes.setEnabled(true);
        for(int k=0;k<items.size();k++) {
            ThreadGetDetails TGD=new ThreadGetDetails(items.get(k),RSC,jCBPlugins.isSelected());
            GetDetailsThreads.add(TGD);
            TPE.execute(TGD);
        }
    }//GEN-LAST:event_jBtnStartActionPerformed

    private void jLstFileListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jLstFileListValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        currentDoc = jLstFileList.getSelectedIndex();
        if (currentDoc == -1) {
            RSC.SM.switchOff(this, "folder");
        } else {
            RSC.SM.switchOn(this, "folder");
        }
        updateDocInfo();
    }//GEN-LAST:event_jLstFileListValueChanged

    private void jLstFileListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLstFileListMouseClicked
        if (evt.getClickCount() == 2) {
            viewDocument();
        }
    }//GEN-LAST:event_jLstFileListMouseClicked

    private void jTPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTPaneStateChanged
        if (initializing) {
            return; // if still initializing
        }
        toInitState();
        if (jTPane.getSelectedIndex()==0) currentList=jLstFileList;
        else {
            currentList=jImpList;
        }
    }//GEN-LAST:event_jTPaneStateChanged

    private void jBtnAdd1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAdd1ActionPerformed
        BibTeXRecord bibtex = new BibTeXRecord(jTABibTeX.getText());
        if (bibtex.parseError == 0) {
            Item doc = createDoc();
            String bibtitle=bibtex.get("title");
            if (bibtitle.startsWith("{")) {
                bibtitle = bibtitle.substring(1, bibtitle.length() - 1);
            }
            doc.put("title", bibtitle);
            doc.put("authors", BibTeXRecord.authorsBibTeX2Cel(bibtex.get("author")));
            doc.put("citation-tag",bibtex.tag);
            doc.put("identifier",bibtex.getIdentifier());
            if (bibtex.get("journal")!=null) {
                doc.put("type","Paper");
            }
            doc.put("bibtex",bibtex.toString());
            items.add(doc);
            currentDoc = 0;
            updateDocInfo();
        } else {
            toolbox.Warning(this,"The BibTeX record is inconsistent:\n" + BibTeXRecord.status[bibtex.parseError], "Warning:");
        }
}//GEN-LAST:event_jBtnAdd1ActionPerformed

    private void jBtnCreateEmptyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCreateEmptyActionPerformed
        jTABibTeX.setText((new BibTeXRecord()).toString());
}//GEN-LAST:event_jBtnCreateEmptyActionPerformed

    private void jBtnNormalize1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNormalize1ActionPerformed
        jTABibTeX.setText(toolbox.NormalizeBibTeX(jTABibTeX.getText()));
}//GEN-LAST:event_jBtnNormalize1ActionPerformed

    private void jBtnClrPreRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClrPreRegActionPerformed
        jTFprereg.setText("");
}//GEN-LAST:event_jBtnClrPreRegActionPerformed

    private void jBtnChooseCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChooseCatActionPerformed
        ChooseCategory DCC=new ChooseCategory(RSC);
        DCC.setVisible(true);
        if (DCC.selected) {
            String tmp=jTFprereg.getText();
            if (tmp.length()!=0) tmp+="|";
            jTFprereg.setText(tmp+DCC.category);
        }
}//GEN-LAST:event_jBtnChooseCatActionPerformed

    private void jTFBarcodeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTFBarcodeKeyTyped
        if (evt.getKeyChar() == KeyEvent.VK_ENTER)
            applyBarCode();
}//GEN-LAST:event_jTFBarcodeKeyTyped

    private void jBtnDoneBibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDoneBibActionPerformed
        applyBarCode();
}//GEN-LAST:event_jBtnDoneBibActionPerformed

    private void jBtnCreateManualEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCreateManualEntryActionPerformed
        Item doc=createDoc();
        items.clear();
        items.add(doc);
        currentDoc=0;
        updateDocInfo();
    }//GEN-LAST:event_jBtnCreateManualEntryActionPerformed

    private void jBtnSelectFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelectFileActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Select the file which you want to add.");
        FC.setCurrentDirectory(new File(RSC.getDir("addsingledoc")));
        FC.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        RSC.setComponentFont(FC.getComponents());
        // cancelled?
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            jTFFile.setText(FC.getSelectedFile().getAbsolutePath());
            RSC.rememberDir("addsingledoc", FC);
        }
    }//GEN-LAST:event_jBtnSelectFileActionPerformed

    private void jBtnFileOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnFileOKActionPerformed
        String fn=jTFFile.getText();
        if (!(new File(fn)).exists()) return;
        boolean found=false;
        for (Item item : Lib)
            if (item.getCompleteDirS("location").equals(fn)) {
                found=true;
                break;
            }
        if (found) {
            toolbox.Warning(this,"This file is already contained in the current library!", "Reading file cancelled...");
            return;
        }
        items.clear();
        GetDetailsThreads.clear();
        try {
            String tmp=RSC.Configuration.correctFileType(fn);
            if (!tmp.equals(fn))
                (new File(fn)).renameTo(new File(tmp));
            createDocFromFile(tmp);
        } catch(Exception e) {
            RSC.Msg1.printStackTrace(e);
            return;
        }
        if (items.isEmpty()) {
            toolbox.Warning(this,"This filetype is not supported.", "Warning:");
            return;
        }
        currentDoc=0;
        ThreadGetDetails TGD=new ThreadGetDetails(items.get(currentDoc),RSC,jCBPlugins2.isSelected());
        TGD.start();
        try {
            TGD.join();
        } catch (InterruptedException ex) {
            RSC.Msg1.printStackTrace(ex);
        }
        jTAFileText.setText(toolbox.getFirstPage(items.get(currentDoc)));
        jTAFileText.setCaretPosition(0);
        updateDocInfo();
        jBtnView2.setEnabled(true);
    }//GEN-LAST:event_jBtnFileOKActionPerformed

    private void jCBAddPropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAddPropertyActionPerformed
        String val=(String)jCBAddProperty.getSelectedItem();
        if (!val.equals("add property")) {
            String bib=jTABibTeX.getText();
            if (bib.length()==0) {
                toolbox.Warning(this,"Please create a BibTeX entry first with the \"Create\" button.", "Cancelled...");
            } else {
                int i=Parser.CutFrom(bib,"\n").trim().indexOf("=");
                String tmp=Parser.CutTillLast(Parser.CutTillLast(bib,"}"),"\n");
                if (val.length()<i) val=val+("                ").substring(0,i-val.length());
                tmp+=",\n   "+val+"= \"\"";
                tmp+="\n}";
                jTABibTeX.setText(tmp);
                jCBAddProperty.setSelectedIndex(0);
            }
        }
}//GEN-LAST:event_jCBAddPropertyActionPerformed

    private void jBtnView2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnView2ActionPerformed
        viewDocument();
    }//GEN-LAST:event_jBtnView2ActionPerformed

    private void jBtnChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChooseFileActionPerformed
        String importS=(String)jCBImport.getSelectedItem();
        importMode=0;
        if (importS.equals("zipped gpx-file")) importMode=1;
        String imptype="bib";
        String impname="BibTeX-file";
        if (importMode==1) {
            imptype="zip";
            impname="zipped gpx-file";
        }
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Indicate the source of the "+impname);
        FC.setCurrentDirectory(new File(RSC.getDir("import"+imptype)));
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        FC.setFileFilter(new FFilter(imptype, impname+"s"));
        RSC.setComponentFont(FC.getComponents());
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            RSC.rememberDir("import"+imptype, FC);
            jTFFileNameImp.setText(FC.getSelectedFile().getAbsolutePath());
            jBtnImport.setEnabled(true);
        }        
    }//GEN-LAST:event_jBtnChooseFileActionPerformed

    private void jBtnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnImportActionPerformed
        if (jPB1.isIndeterminate()) {
            toolbox.Warning(this,"Import in progress.", "Warning:");
            return;
        }
        jPB1.setIndeterminate(true);
        (new ThreadImport(jTFFileNameImp.getText(),importMode,items,Lib,jImpList,jPB1)).start();
        jBtnImpRec.setEnabled(true);
    }//GEN-LAST:event_jBtnImportActionPerformed

    private void jImpListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jImpListValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        currentDoc = jImpList.getSelectedIndex();
        if (currentDoc == -1) {
            RSC.SM.switchOff(this, "folder2");
        } else {
            RSC.SM.switchOn(this, "folder2");
        }
        updateDocInfo();
    }//GEN-LAST:event_jImpListValueChanged

    private void jBtnImpDropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnImpDropActionPerformed
        removeFromTable(items.get(currentDoc));
    }//GEN-LAST:event_jBtnImpDropActionPerformed

    private void jBtnImpRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnImpRecActionPerformed
        jBtnAdd.setEnabled(false);
        jBtnAddRec.setEnabled(false);
        final ArrayList<Item> docs2=cloneDocs();
        final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Adding list of items ...", "", 0, 0);
        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);
        final AddItems DA=this;
        progressMonitor.setMaximum(docs2.size());
        cancelAdding=false;
        autoDelete=false;
        autoReplace=false;
        DoIt=(new Thread() {
            @Override
            public void run() {
                int c = 0;
                for (Item doc : docs2) {
                    addItem(doc);
                    if (cancelAdding) break;
                    c++;
                    progressMonitor.setProgress(c);
                }
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run()  {
                        DA.adjustJBtnAdd();
                        DA.jBtnAddRec.setEnabled(true);
                    }
                });
            }
        });
        DoIt.start();
    }//GEN-LAST:event_jBtnImpRecActionPerformed

    private void jBtnImpDoubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnImpDoubActionPerformed
        ArrayList<String> tags=new ArrayList<String>();
        for (Item doc : Lib) {
            String tmp=doc.get("citation-tag");
            if ((tmp!=null) && (tmp.length()!=0))
                tags.add(tmp);
        }
        ArrayList<Item> docs2=cloneDocs();
        for (Item doc : docs2) {
            if (tags.indexOf(doc.get("citation-tag"))>-1)
                deleteItem(doc,false);
        }
    }//GEN-LAST:event_jBtnImpDoubActionPerformed

    private void jBtnFormatAuthorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnFormatAuthorsActionPerformed
        applyChanges();
        for (String p : Lib.listOf("people")) {
            String s=items.get(currentDoc).get(p);
            s=toolbox.authorsBibTeX2Cel(s);
            items.get(currentDoc).put(p,s);
        }
        updateDocInfo();
    }//GEN-LAST:event_jBtnFormatAuthorsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea ThreadStatus;
    public javax.swing.JButton jBtnAdd;
    private javax.swing.JButton jBtnAdd1;
    private javax.swing.JButton jBtnAddRec;
    private javax.swing.JButton jBtnApply;
    private javax.swing.JButton jBtnApplyPlugin;
    private javax.swing.JButton jBtnChooseCat;
    private javax.swing.JButton jBtnChooseFile;
    private javax.swing.JButton jBtnClrPreReg;
    private javax.swing.JButton jBtnCreateEmpty;
    private javax.swing.JButton jBtnCreateManualEntry;
    private javax.swing.JButton jBtnDelete;
    private javax.swing.JButton jBtnDone;
    private javax.swing.JButton jBtnDoneBib;
    private javax.swing.JButton jBtnDrop;
    private javax.swing.JButton jBtnFileOK;
    private javax.swing.JButton jBtnFindDoublettes;
    private javax.swing.JButton jBtnFormatAuthors;
    private javax.swing.JButton jBtnImpDoub;
    private javax.swing.JButton jBtnImpDrop;
    private javax.swing.JButton jBtnImpRec;
    private javax.swing.JButton jBtnImport;
    private javax.swing.JButton jBtnNewTag;
    private javax.swing.JButton jBtnNewTag1;
    private javax.swing.JButton jBtnNormalize;
    private javax.swing.JButton jBtnNormalize1;
    private javax.swing.JButton jBtnSelectFile;
    private javax.swing.JButton jBtnSelectFolder;
    private javax.swing.JButton jBtnStart;
    private javax.swing.JButton jBtnView;
    private javax.swing.JButton jBtnView2;
    private javax.swing.JComboBox jCBAddProperty;
    private javax.swing.JComboBox jCBImport;
    private javax.swing.JCheckBox jCBMove;
    private javax.swing.JCheckBox jCBPlugins;
    private javax.swing.JCheckBox jCBPlugins2;
    private javax.swing.JList jImpList;
    private javax.swing.JList jLPlugins;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jLstFileList;
    private javax.swing.JProgressBar jPB1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTABibTeX;
    private javax.swing.JTextArea jTAFileText;
    private javax.swing.JTextField jTFBarcode;
    private javax.swing.JTextField jTFFile;
    private javax.swing.JTextField jTFFileNameImp;
    private javax.swing.JTextField jTFFolder;
    private javax.swing.JTextArea jTFirstPage;
    public javax.swing.JTextField jTFprereg;
    private javax.swing.JTable jTInfo;
    private javax.swing.JTabbedPane jTPane;
    // End of variables declaration//GEN-END:variables

    private void updateDocInfo() {
        if (currentDoc == -1) {
            jBtnAdd.setEnabled(false);
            jTInfo.setModel(new KeyValueTableModel("Tag", "Value"));
        } else {
            if (jTPane.getSelectedIndex()==0) {
                if (GetDetailsThreads.get(currentDoc).done) {
                    ThreadStatus.setText("Details obtained for current item. Threads running:"+String.valueOf(queuesize));
                } else {
                    ThreadStatus.setText("Getting details for current item. Threads running:"+String.valueOf(queuesize));
                }
            }
            // rest
            adjustJBtnAdd();
            Item doc = items.get(currentDoc);
            KVTM=new KeyValueTableModel("Tag","Value");
            for (String k : doc.totalKeySet()) {
                String t=doc.get(k);
                if (t==null) t="<unknown>";
                KVTM.addRow(Parser.LowerEndOfWords(k),t);
            }
            jTInfo.setModel(KVTM);
            jTInfo.getColumnModel().getColumn(0).setCellEditor(null);
            TableColumn column = jTInfo.getColumnModel().getColumn(1);
            column.setPreferredWidth(230);
            column.setCellEditor(new InfoEditor(RSC, Lib.ChoiceFields, Lib.IconFields,Lib.IconDictionary));
            jTFirstPage.setText(toolbox.getFirstPage(doc).replaceAll("^[^\\P{C}]", "?"));
            jTFirstPage.setCaretPosition(0);
        }
    }

    private void toNext(int i) {
        i++;
        if (i + 2 > currentList.getModel().getSize()) {
            i = currentList.getModel().getSize() - 1;
        }
        currentList.grabFocus();
        currentList.setSelectedIndex(i);
        currentList.ensureIndexIsVisible(i);
    }

    private void applyBarCode() {
        Item doc=createDoc();
        items.clear();
        items.add(doc);
        currentDoc=0;
        doc.put("barcode",jTFBarcode.getText().trim());
        doc.put("title",jTFBarcode.getText().trim());
        updateDocInfo();
        final ThreadApplyPlugin TAP=new ThreadApplyPlugin(null,RSC.Plugins.get("Look at Amazon"),RSC.Plugins.parameters.get("Look at Amazon"),RSC, items.get(currentDoc),false,true);
        SwingWorker worker = new SwingWorker<Object, Object>() { //#####
            @Override
            protected Object doInBackground() {
                TAP.start();
                try {
                    TAP.join();
                } catch (InterruptedException ex) {
                    RSC.Msg1.printStackTrace(ex);
                }
                return(null);
            }

            @Override
            protected void done() {
               try {
                updateDocInfo();
               } catch (Exception ignore) {
               }
            }

        };
        worker.execute();
    }

    public void actionPerformed(ActionEvent e) {
        int d=LBQ.size()+TPE.getActiveCount();
        if (d!=queuesize) {
            queuesize=d;
            if (currentDoc>-1) {
                if (GetDetailsThreads.get(currentDoc).done) {
                    if (ThreadStatus.getText().startsWith("Getting")) {
                        updateDocInfo();
                    }
                    ThreadStatus.setText("Details obtained for current item. Threads running:"+String.valueOf(queuesize));
                } else {
                    ThreadStatus.setText("Getting details for current item. Threads running:"+String.valueOf(queuesize));
                }
            } else {
                ThreadStatus.setText("No item selected. Threads running:"+String.valueOf(queuesize));
            }
        } else if (currentDoc>-1) {
            if (ThreadStatus.getText().startsWith("Getting"))
                if (GetDetailsThreads.get(currentDoc).done)
                    ThreadStatus.setText("Details obtained for current item. Threads running:"+String.valueOf(queuesize));
        }
    }

    private void addItem(final Item doc) {
        try {
            if (!doc.getS("$$beingadded").equals("")) return;
            doc.put("$$beingadded","true");
            final Integer[] res=new Integer[1];
            int dbl=Lib.Doublette(doc);
            if (dbl==10) {
                if (!autoDelete) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            res[0]=toolbox.QuestionABCD(null,"An exact copy of the document\n"+doc.toText(Lib)+"\nis already existing in the library:\n"+Lib.marker.toText()+"\nDelete the file "+doc.get("location")+"?","Warning","Yes","No","Always","Abort");
                        }
                    });
                } else res[0]=0;
                if (res[0]==0) {
                    deleteItem(doc,false);
                }
                if (res[0]==2) {
                    deleteItem(doc,false);
                    autoDelete=true;
                }
                if (res[0]==3) {
                    cancelAdding=true;
                    doc.put("$$beingadded",null);
                }
                return;
            }
            if (dbl==5) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        res[0]=toolbox.QuestionYN(null,"A file with exactly the same length as the document\n"+doc.toText(Lib)+"\nis already existing in the library.\nProceed anyway?","Warning");
                    }
                });
                if (res[0]==JOptionPane.NO_OPTION) {
                    doc.put("$$beingadded",null);
                    return;
                }
            }
            if (dbl==4) {
                if (!autoReplace) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                        Object[] options=new Object[6];
                        options[0]="Delete"; options[1]="Replace"; options[2]="New Version"; options[3]="Replace All"; options[4]="Ignore"; options[5]="Cancel";
                        String msg="The document \n"+doc.toText(Lib)+
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
                } else res[0]=1;
                if (res[0]==0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            deleteItem(doc,false);
                        }
                    });
                    return;
                }
                if (res[0]==3) autoReplace=true;
                if ((res[0]==1) || (res[0]==3)) {
                    Lib.replaceItem(doc);
                    addedItems.add(Lib.lastAddedItem);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            removeFromTable(doc);
                        }
                    });
                    return;
                }
                if (res[0]==2) {
                    Item doc2=Lib.marker;
                    doc2.shiftReplaceWithFile(RSC,doc.get("location"));
                    TextFile.Delete(doc.get("plaintxt"));
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            removeFromTable(doc);
                        }
                    });
                    return;
                }
                if (res[0]==5) cancelAdding=true;
                return;
            }
            int mode = 0;
            if (!jCBMove.isSelected()) {
                mode = 1;
            }
            int i = Lib.addItem(doc, jTFprereg.getText(), mode);
            addedItems.add(Lib.lastAddedItem);
            if (i == 0) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        jBtnAdd.setEnabled(false);
                        jTInfo.setModel(new KeyValueTableModel("Tag", "Value"));
                        jTAFileText.setText("");
                        jTFFile.setText("");
                        removeFromTable(doc);
                        jBtnView2.setEnabled(false);
                    }
                });
            } else {
               doc.put("$$beingadded",null);
            }
        } catch (Exception e) {
            RSC.Msg1.printStackTrace(e);
        }
    }

    private void deleteItem(Item doc, boolean confirmed) {
        int lcurrentDoc=items.indexOf(doc);
        if (lcurrentDoc==-1) return;
        if (doc.getS("location").length()>0) {
            if (!confirmed) {
                int j = toolbox.QuestionOC(this, "Really delete the file " + doc.get("location") + "?", "Warning");
                if (j != JOptionPane.NO_OPTION) {
                    doc.deleteFiles();
                    RSC.Msg1.repS(TI + "Deleting :: " + doc.get("filename"));
                    RSC.Msg1.repS(TI + "Deleting :: " + doc.get("plaintxt"));
                } else {
                    return;
                }
            } else {
                doc.deleteFiles();
                RSC.Msg1.repS(TI + "Deleting :: " + doc.get("filename"));
                RSC.Msg1.repS(TI + "Deleting :: " + doc.get("plaintxt"));
            }
        }
        removeFromTable(doc);
    }

    private void removeFromTable(Item doc) {
        int lcurrentDoc=items.indexOf(doc);
        int selectedDoc=jLstFileList.getSelectedIndex();
        items.remove(lcurrentDoc);
        if (currentList.getModel().getSize()>0) {
            ((DefaultListModel) currentList.getModel()).remove(lcurrentDoc);
            currentList.repaint();
            if (lcurrentDoc==selectedDoc)
                toNext(lcurrentDoc - 1);
        }
        if (GetDetailsThreads.size()>0) {
            GetDetailsThreads.remove(lcurrentDoc);
        }
        if (items.isEmpty()) {
            toInitState();
        }
    }

    private ArrayList<Item> cloneDocs() {
        ArrayList<Item> clone=new ArrayList<Item>();
        for (Item doc : items) clone.add(doc);
        return(clone);
    }

    private void adjustJBtnAdd() {
        boolean en = false;
        String[] ef = Lib.listOf("essential-fields");
        if (currentDoc>-1) {
            en=true;
            Item doc = items.get(currentDoc);
            for (int k = 0; k < ef.length; k++) {
                if (doc.get(ef[k]) == null)
                    en = false;
            }
        }
        jBtnAdd.setEnabled(en);
    }

}

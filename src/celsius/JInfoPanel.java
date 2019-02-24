//
// Celsius Library System
// (w) by C. Saemann
//
// jInfoPanel.java
//
// This class represents the InfoPanel
//
// typesafe
//
// checked: 16.09.2007
//

package celsius;

import celsius.Dialogs.MultiLineEditor;
import celsius.Threads.ThreadShowCited;
import celsius.tools.*;
import java.awt.Component;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author cnsaeman
 */
public final class JInfoPanel extends javax.swing.JPanel { //implements DropTargetListener {
        

    private final Resources RSC;
    private final JobManager JM;
    private final MainFrame MF;
    private ArrayList<Component[]> iMC;
    private ArrayList<String[]> iMII;

    private ItemTable IT;
    private Library Lib;
    private Item item;
    private boolean warned;

    public int infoMode; //-1: startup, 0: document, 1:person, 2:category

    public HTMLEditorKit kit;

    /** Creates new form jInfoPanel */
    public JInfoPanel(MainFrame mf,Resources rsc,JobManager jm) {
        RSC=rsc;
        initComponents();
        jHTMLview.setTransferHandler( createTransferHandler(this));
        //DropTarget dt = (new DropTarget(jHTMLview, DnDConstants.ACTION_COPY_OR_MOVE,this,true,null));
        kit = new HTMLEditorKit();
        jHTMLview.setEditorKit(kit);
        jTPDoc.setTabComponentAt(0,new TabLabel("Info","information",rsc,null,false));
        MF=mf;
        JM=jm;
        warned=false;
        jSPraw.setDividerLocation(0.8);
        // Init Linktree
        DefaultTreeCellRenderer renderer3 = new DefaultTreeCellRenderer();
        renderer3.setLeafIcon(RSC.Icons.getIcon("arrow_right"));
        renderer3.setClosedIcon(RSC.Icons.getIcon("folder"));
        renderer3.setOpenIcon(RSC.Icons.getIcon("folder_link"));
        jTLinks.setCellRenderer(renderer3);
        jLFiles1.setModel(new DefaultListModel());
        jLFiles2.setModel(new DefaultListModel());
        jLFiles3.setModel(new DefaultListModel());
        
        // init information tabs and images.
        iMC = new ArrayList<Component[]>();
        iMII = new ArrayList<String[]>();
        // infoMode 0: Single document selected
        Component[] C0C = {jPBibData, jPLinks, jPRemarks, jPInspect, jPFiles, jPThumb, jPDocRaw};
        String[] C0II = {"star","arrow_out","pencil","magnifier","star","image_edit","wrench_orange"};

        iMC.add(C0C);
        iMII.add(C0II);
        // infoMode 1: Person selected
        Component[] C1C = {jPRemarks, jPPersonRaw};
        String[] C1II = {"pencil","wrench_orange"};
        iMC.add(C1C);
        iMII.add(C1II);
        // infoMode 2: Category selected
        Component[] C2C = {jPRemarks};
        String[] C2II = {"pencil"};
        iMC.add(C2C);
        iMII.add(C2II);
        // infoMode 3: multiple items selected
        Component[] C3C = {};
        String[] C3II = {};
        iMC.add(C3C);
        iMII.add(C3II);
        // infoMode 4: search identifier results
        Component[] C4C = {};
        String[] C4II = {};
        iMC.add(C4C);
        iMII.add(C4II);
        // infoMode 5: search identifier results
        Component[] C5C = {};
        String[] C5II = {};
        iMC.add(C5C);
        iMII.add(C5II);
        // infoMode 6: search key results
        Component[] C6C = {};
        String[] C6II = {};
        iMC.add(C6C);
        iMII.add(C6II);
        // infoMode 7: link results
        Component[] C7C = {};
        String[] C7II = {};
        iMC.add(C7C);
        iMII.add(C7II);
        // infoMode 8: Just added
        Component[] C8C = {};
        String[] C8II = {};
        iMC.add(C7C);
        iMII.add(C7II);

        RSC.SM.register(MF, "noLib", new JComponent[] {jMIEditDS1,jMIReloadDisplayString});
        
        item=null;
        IT=null;
        updateHTMLview();

    }
    
    public void setItem(Item it,ItemTable dt) {
        IT=dt;
        item=it;
        if (it!=null) {
            setLibrary(item.Lib);
            if (it.getS("plaintxt").length() > 0) {
                jBtnInspect1.setEnabled(true);
                jBtnInspect2.setEnabled(true);
                MF.jMIViewPlain.setEnabled(true);
                MF.jMIViewPlain1.setEnabled(true);
            } else {
                jBtnInspect1.setEnabled(false);
                jBtnInspect2.setEnabled(false);
                MF.jMIViewPlain.setEnabled(false);
                MF.jMIViewPlain1.setEnabled(false);
            }
        }
    }

    public void setLibrary(Library l) {
        if (l==null) {
            Lib=null;
            kit = new HTMLEditorKit();
            return;
        }
        if (Lib!=l) {
            Lib=l;
            kit = new HTMLEditorKit();
            kit.setStyleSheet(null);
            Lib.adjustStyleSheet(kit.getStyleSheet());
            jHTMLview.setEditorKit(kit);
            RSC.Plugins.updatePlugins();
        }
    }

    public void setDocumentTable(ItemTable dt) {
        IT=dt;
        setLibrary(IT.Lib);
    }

    public Item getItem() {
        return(item);
    }

    public void updateThumb() {
        if (jPThumb.isVisible()) {
            if (item!=null) {
                if (item.get("thumbnail")!=null) {
                    jBtnResizeThumb.setEnabled(true);
                    jBtnRemoveThumb.setEnabled(true);
                    try {
                        jLIcon.setIcon(new ImageIcon(new URL("file://"+item.getCompleteDirS("thumbnail"))));
                    } catch (Exception e) {
                        MF.Msg1.printStackTrace(e);
                    }
                } else {
                    jBtnResizeThumb.setEnabled(false);
                    jBtnRemoveThumb.setEnabled(false);
                    jLIcon.setIcon(null);
                }
            } else {
                jBtnResizeThumb.setEnabled(false);
                jBtnRemoveThumb.setEnabled(false);
                jLIcon.setIcon(null);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPMHTML = new javax.swing.JPopupMenu();
        jMIReloadDisplayString = new javax.swing.JMenuItem();
        jMIEditDS1 = new javax.swing.JMenuItem();
        jMIAddThumb = new javax.swing.JMenuItem();
        jTPDoc = new javax.swing.JTabbedPane();
        jSP3 = new javax.swing.JScrollPane();
        jHTMLview = new javax.swing.JEditorPane();
        jPBibData = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTABibTeX = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        jBtnApplyBibTeX = new javax.swing.JButton();
        jBtnCreateBibTeX = new javax.swing.JButton();
        jBtnNormalizeBibTeX = new javax.swing.JButton();
        jCBAddProperty = new javax.swing.JComboBox();
        jCBBibPlugins = new javax.swing.JComboBox();
        jPLinks = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTLinks = new javax.swing.JTree();
        jTFLinkType = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTFLinkTarget = new javax.swing.JTextField();
        jBtnAddLink = new javax.swing.JButton();
        jBtnRemoveLink = new javax.swing.JButton();
        jBtnLinkHelp = new javax.swing.JButton();
        jBtnCompressLinks = new javax.swing.JButton();
        jPRemarks = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jBtnApplyRem = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTARemarks = new javax.swing.JTextArea();
        jPInspect = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        jEPInspect = new javax.swing.JEditorPane();
        jPanel6 = new javax.swing.JPanel();
        jBtnInspect2 = new javax.swing.JButton();
        jBtnInspect1 = new javax.swing.JButton();
        jPFiles = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jLFiles1 = new javax.swing.JList<>();
        jScrollPane9 = new javax.swing.JScrollPane();
        jLFiles2 = new javax.swing.JList<>();
        jScrollPane13 = new javax.swing.JScrollPane();
        jLFiles3 = new javax.swing.JList<>();
        jBtnChooseSourceFolder = new javax.swing.JButton();
        jBtnShowCited = new javax.swing.JButton();
        jPThumb = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jLIcon = new javax.swing.JLabel();
        jBtnResizeThumb = new javax.swing.JButton();
        jBtnAddThumb = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jBtnRemoveThumb = new javax.swing.JButton();
        jPDocRaw = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jBtnRawApply = new javax.swing.JButton();
        jBtnRawUndo = new javax.swing.JButton();
        jBtnRename = new javax.swing.JButton();
        jSPraw = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTARaw1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTARaw2 = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPPersonRaw = new javax.swing.JPanel();
        Panelx = new javax.swing.JPanel();
        jBtnApplyPerson = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTARaw3 = new javax.swing.JTextArea();

        jPMHTML.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPMHTMLPopupMenuWillBecomeVisible(evt);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jMIReloadDisplayString.setText("Reload HTML template");
        jMIReloadDisplayString.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIReloadDisplayStringActionPerformed(evt);
            }
        });
        jPMHTML.add(jMIReloadDisplayString);

        jMIEditDS1.setText("Edit HTML template");
        jMIEditDS1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIEditDS1jMIEditDSActionPerformed(evt);
            }
        });
        jPMHTML.add(jMIEditDS1);

        jMIAddThumb.setText("Add Thumbnail");
        jMIAddThumb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMIAddThumbActionPerformed(evt);
            }
        });
        jPMHTML.add(jMIAddThumb);

        setMinimumSize(new java.awt.Dimension(0, 298));
        setPreferredSize(new java.awt.Dimension(300, 298));

        jTPDoc.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTPDoc.setName(""); // NOI18N
        jTPDoc.setPreferredSize(new java.awt.Dimension(395, 627));

        jHTMLview.setEditable(false);
        jHTMLview.setFont(jHTMLview.getFont());
        jHTMLview.setComponentPopupMenu(jPMHTML);
        jHTMLview.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                jHTMLviewHyperlinkUpdate(evt);
            }
        });
        jSP3.setViewportView(jHTMLview);

        jTPDoc.addTab("Info", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/information.png")), jSP3); // NOI18N

        jPBibData.setName("Bibliography"); // NOI18N
        jPBibData.setLayout(new java.awt.BorderLayout());

        jTABibTeX.setColumns(20);
        jTABibTeX.setFont(new java.awt.Font("Monospaced", 0, RSC.guiScale(12))
        );
        jTABibTeX.setRows(5);
        jScrollPane10.setViewportView(jTABibTeX);

        jPBibData.add(jScrollPane10, java.awt.BorderLayout.CENTER);

        jPanel8.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel8.setName(""); // NOI18N
        jPanel8.setPreferredSize(new java.awt.Dimension(RSC.guiScale(100), RSC.guiScale(34)));
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jBtnApplyBibTeX.setText("Apply");
        jBtnApplyBibTeX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyBibTeXActionPerformed(evt);
            }
        });
        jPanel8.add(jBtnApplyBibTeX);

        jBtnCreateBibTeX.setText("Create");
        jBtnCreateBibTeX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCreateBibTeXActionPerformed(evt);
            }
        });
        jPanel8.add(jBtnCreateBibTeX);

        jBtnNormalizeBibTeX.setText("Normalize");
        jBtnNormalizeBibTeX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNormalizeBibTeXActionPerformed(evt);
            }
        });
        jPanel8.add(jBtnNormalizeBibTeX);

        jCBAddProperty.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "add property", "author", "editor", "publisher", "title", "journal", "volume", "number", "series", "year", "pages", "note", "doi", "eprint", "archiveprefix", "primaryclass", "slaccitation" }));
        jCBAddProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBAddPropertyActionPerformed(evt);
            }
        });
        jPanel8.add(jCBAddProperty);

        jCBBibPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBBibPluginsActionPerformed(evt);
            }
        });
        jPanel8.add(jCBBibPlugins);

        jPBibData.add(jPanel8, java.awt.BorderLayout.SOUTH);

        jTPDoc.addTab("Bibliography1", jPBibData);

        jPLinks.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPLinks.setName("Links"); // NOI18N
        jPLinks.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPLinksComponentShown(evt);
            }
        });
        jPLinks.setLayout(new java.awt.GridBagLayout());

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("JTree");
        jTLinks.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTLinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTLinksMouseClicked(evt);
            }
        });
        jScrollPane14.setViewportView(jTLinks);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 643;
        gridBagConstraints.ipady = 169;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPLinks.add(jScrollPane14, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 5, 0);
        jPLinks.add(jTFLinkType, gridBagConstraints);

        jLabel7.setText("Link type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPLinks.add(jLabel7, gridBagConstraints);

        jLabel10.setText("Link target:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPLinks.add(jLabel10, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 5, 0);
        jPLinks.add(jTFLinkTarget, gridBagConstraints);

        jBtnAddLink.setText("Add Link");
        jBtnAddLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAddLinkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 5, 0);
        jPLinks.add(jBtnAddLink, gridBagConstraints);

        jBtnRemoveLink.setText("Remove Link");
        jBtnRemoveLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRemoveLinkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        jPLinks.add(jBtnRemoveLink, gridBagConstraints);

        jBtnLinkHelp.setText("Help");
        jBtnLinkHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLinkHelpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 0, 0);
        jPLinks.add(jBtnLinkHelp, gridBagConstraints);

        jBtnCompressLinks.setText("Compress Links");
        jBtnCompressLinks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCompressLinksActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 0);
        jPLinks.add(jBtnCompressLinks, gridBagConstraints);

        jTPDoc.addTab("Links", jPLinks);

        jPRemarks.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPRemarks.setName("Remarks"); // NOI18N
        jPRemarks.setLayout(new java.awt.BorderLayout());

        jPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        jPanel7.setPreferredSize(new java.awt.Dimension(RSC.guiScale(100), RSC.guiScale(25)));
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jBtnApplyRem.setText("Apply");
        jBtnApplyRem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyRemActionPerformed(evt);
            }
        });
        jPanel7.add(jBtnApplyRem);

        jPRemarks.add(jPanel7, java.awt.BorderLayout.SOUTH);

        jTARemarks.setColumns(20);
        jTARemarks.setFont(jTARemarks.getFont());
        jTARemarks.setLineWrap(true);
        jTARemarks.setRows(5);
        jTARemarks.setWrapStyleWord(true);
        jScrollPane8.setViewportView(jTARemarks);

        jPRemarks.add(jScrollPane8, java.awt.BorderLayout.CENTER);

        jTPDoc.addTab("Remarks", jPRemarks);

        jPInspect.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPInspect.setName("Inspect"); // NOI18N
        jPInspect.setLayout(new java.awt.BorderLayout());

        jEPInspect.setEditable(false);
        jScrollPane12.setViewportView(jEPInspect);

        jPInspect.add(jScrollPane12, java.awt.BorderLayout.CENTER);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        jPanel6.setPreferredSize(new java.awt.Dimension(RSC.guiScale(300), RSC.guiScale(29)));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jBtnInspect2.setText("View Plain Text");
        jBtnInspect2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnInspect2ActionPerformed(evt);
            }
        });
        jPanel6.add(jBtnInspect2);

        jBtnInspect1.setText("Show first page");
        jBtnInspect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnInspect1ActionPerformed(evt);
            }
        });
        jPanel6.add(jBtnInspect1);

        jPInspect.add(jPanel6, java.awt.BorderLayout.SOUTH);

        jTPDoc.addTab("Inspect", jPInspect);

        jPFiles.setName("Files"); // NOI18N

        jPanel10.setLayout(new java.awt.GridLayout(1, 0, 10, 0));

        jLFiles1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLFiles1MouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(jLFiles1);

        jPanel10.add(jScrollPane7);

        jLFiles2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLFiles2MouseClicked(evt);
            }
        });
        jScrollPane9.setViewportView(jLFiles2);

        jPanel10.add(jScrollPane9);

        jLFiles3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLFiles3MouseClicked(evt);
            }
        });
        jScrollPane13.setViewportView(jLFiles3);

        jPanel10.add(jScrollPane13);

        jBtnChooseSourceFolder.setText("Choose Source folder");
        jBtnChooseSourceFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChooseSourceFolderActionPerformed(evt);
            }
        });

        jBtnShowCited.setText("Show cited papers in TeX-File");
        jBtnShowCited.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnShowCitedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPFilesLayout = new javax.swing.GroupLayout(jPFiles);
        jPFiles.setLayout(jPFilesLayout);
        jPFilesLayout.setHorizontalGroup(
            jPFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                    .addGroup(jPFilesLayout.createSequentialGroup()
                        .addComponent(jBtnChooseSourceFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnShowCited)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPFilesLayout.setVerticalGroup(
            jPFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnChooseSourceFolder)
                    .addComponent(jBtnShowCited))
                .addContainerGap())
        );

        jTPDoc.addTab("Files", new javax.swing.ImageIcon(getClass().getResource("/celsius/images/star.png")), jPFiles, "Files"); // NOI18N

        jPThumb.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPThumb.setName(""); // NOI18N
        jPThumb.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPThumbComponentShown(evt);
            }
        });

        jScrollPane2.setViewportView(jLIcon);

        jBtnResizeThumb.setFont(jBtnResizeThumb.getFont().deriveFont(jBtnResizeThumb.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnResizeThumb.getFont().getSize()-1));
        jBtnResizeThumb.setText("Resize to 240x240");
        jBtnResizeThumb.setEnabled(false);
        jBtnResizeThumb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResizeThumbActionPerformed(evt);
            }
        });

        jBtnAddThumb.setFont(jBtnAddThumb.getFont().deriveFont(jBtnAddThumb.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnAddThumb.getFont().getSize()-1));
        jBtnAddThumb.setText("Add File");
        jBtnAddThumb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAddThumbActionPerformed(evt);
            }
        });

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel4.getFont().getSize()-1));
        jLabel4.setText("Thumbnail:");

        jBtnRemoveThumb.setFont(jBtnRemoveThumb.getFont().deriveFont(jBtnRemoveThumb.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnRemoveThumb.getFont().getSize()-1));
        jBtnRemoveThumb.setText("Remove");
        jBtnRemoveThumb.setEnabled(false);
        jBtnRemoveThumb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRemoveThumbActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPThumbLayout = new javax.swing.GroupLayout(jPThumb);
        jPThumb.setLayout(jPThumbLayout);
        jPThumbLayout.setHorizontalGroup(
            jPThumbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPThumbLayout.createSequentialGroup()
                .addGroup(jPThumbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPThumbLayout.createSequentialGroup()
                        .addComponent(jBtnAddThumb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnResizeThumb)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnRemoveThumb))
                    .addComponent(jLabel4))
                .addContainerGap(530, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
        );
        jPThumbLayout.setVerticalGroup(
            jPThumbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPThumbLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPThumbLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnResizeThumb)
                    .addComponent(jBtnAddThumb)
                    .addComponent(jBtnRemoveThumb)))
        );

        jTPDoc.addTab("Thumbnail", jPThumb);

        jPDocRaw.setToolTipText("Information associated to the document in raw form");
        jPDocRaw.setName(""); // NOI18N
        jPDocRaw.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setPreferredSize(new java.awt.Dimension(RSC.guiScale(100), RSC.guiScale(34)));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jBtnRawApply.setFont(jBtnRawApply.getFont().deriveFont(jBtnRawApply.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnRawApply.getFont().getSize()-1));
        jBtnRawApply.setText("Apply");
        jBtnRawApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRawApplyActionPerformed(evt);
            }
        });
        jPanel5.add(jBtnRawApply);

        jBtnRawUndo.setFont(jBtnRawUndo.getFont().deriveFont(jBtnRawUndo.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnRawUndo.getFont().getSize()-1));
        jBtnRawUndo.setText("Undo");
        jBtnRawUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRawUndoActionPerformed(evt);
            }
        });
        jPanel5.add(jBtnRawUndo);

        jBtnRename.setFont(jBtnRename.getFont().deriveFont(jBtnRename.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnRename.getFont().getSize()-1));
        jBtnRename.setText("Rename file (use naming conventions)");
        jBtnRename.setToolTipText("Rename the file according to the standard naming convention of the Library.");
        jBtnRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRenameActionPerformed(evt);
            }
        });
        jPanel5.add(jBtnRename);

        jPDocRaw.add(jPanel5, java.awt.BorderLayout.SOUTH);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jTARaw1.setColumns(20);
        jTARaw1.setFont(jTARaw1.getFont());
        jTARaw1.setRows(5);
        jTARaw1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTARaw1FocusGained(evt);
            }
        });
        jScrollPane1.setViewportView(jTARaw1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(RSC.guiScale(223), RSC.guiScale(17)));

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel1.getFont().getSize()-1));
        jLabel1.setText("Main index:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        jSPraw.setLeftComponent(jPanel1);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jTARaw2.setColumns(20);
        jTARaw2.setRows(5);
        jScrollPane6.setViewportView(jTARaw2);

        jPanel3.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jPanel4.setPreferredSize(new java.awt.Dimension(RSC.guiScale(223), RSC.guiScale(17)));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel2.getFont().getSize()-1));
        jLabel2.setText("Full information, separate file:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(419, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        jSPraw.setRightComponent(jPanel3);

        jPDocRaw.add(jSPraw, java.awt.BorderLayout.CENTER);

        jTPDoc.addTab("Data", jPDocRaw);

        jPPersonRaw.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPPersonRaw.setName("Data"); // NOI18N
        jPPersonRaw.setLayout(new java.awt.BorderLayout());

        Panelx.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        Panelx.setPreferredSize(new java.awt.Dimension(100, 29));

        jBtnApplyPerson.setFont(jBtnApplyPerson.getFont().deriveFont(jBtnApplyPerson.getFont().getStyle() & ~java.awt.Font.BOLD, jBtnApplyPerson.getFont().getSize()-1));
        jBtnApplyPerson.setText("Apply");
        jBtnApplyPerson.setPreferredSize(new java.awt.Dimension(70, 24));
        jBtnApplyPerson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyPersonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelxLayout = new javax.swing.GroupLayout(Panelx);
        Panelx.setLayout(PanelxLayout);
        PanelxLayout.setHorizontalGroup(
            PanelxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelxLayout.createSequentialGroup()
                .addComponent(jBtnApplyPerson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(744, Short.MAX_VALUE))
        );
        PanelxLayout.setVerticalGroup(
            PanelxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelxLayout.createSequentialGroup()
                .addComponent(jBtnApplyPerson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPPersonRaw.add(Panelx, java.awt.BorderLayout.SOUTH);

        jTARaw3.setColumns(20);
        jTARaw3.setFont(jTARaw3.getFont());
        jTARaw3.setRows(5);
        jScrollPane11.setViewportView(jTARaw3);

        jPPersonRaw.add(jScrollPane11, java.awt.BorderLayout.CENTER);

        jTPDoc.addTab("Data", jPPersonRaw);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTPDoc, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTPDoc, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jHTMLviewHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_jHTMLviewHyperlinkUpdate
        if (evt.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
            String cmd=evt.getDescription();
            if (cmd.charAt(0)=='#') {
                jHTMLview.scrollToReference(cmd.substring(1));
                return;
            }
            if (cmd.equals("http://$$view")) {
                JM.ViewSelected(null);
                return;
            }
            if (cmd.equals("http://$$viewsimilar")) {
                JM.showCombined();
                return;
            }
            if (cmd.startsWith("http://$$display-message")) {
                String s1=Parser.CutFrom(cmd, "http://$$display-message:");
                String s2=Parser.CutFrom(s1,":");
                s1=Parser.CutTill(s1,":");
                toolbox.Information(MF,s2, s1);
                return;
            }
            if (cmd.startsWith("http://$$view")) {
                String nmb=Parser.CutFromLast(cmd, "-");
                JM.ViewSelected(nmb);
                return;
            }
            if (cmd.startsWith("http://cid-")) {
                String id=Parser.CutFromLast(cmd, "-");
                String nmb=null;
                if (id.indexOf("-")>-1) {
                    nmb=Parser.CutFrom(id,"-");
                    id=Parser.CutTill(id,"-");
                }
                RSC.Configuration.view((new Item(Lib,id)),null);
                return;
            }
            if (cmd.startsWith("http://$$links")) {
                String type=Parser.CutFromLast(cmd, "-");
                updateLinks();
                JM.showLinksOfType(type);
                return;
            }
            if (cmd.equals("http://$$journallink")) {
                JM.JournalLinkSelected();
                return;
            }
            if (cmd.startsWith("http://$$author.")) {
                String a=Parser.CutFrom(cmd,"http://$$author.");
                JM.goToPerson(a);
                return;
            }
            RSC.Configuration.viewHTML(evt.getURL().toString());
        }
}//GEN-LAST:event_jHTMLviewHyperlinkUpdate

    private void jBtnApplyBibTeXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyBibTeXActionPerformed
        if (jTABibTeX.getText().trim().equals("")) {
            item.put("bibtex",null);
            if (item.get("type").equals("Paper")) {
                item.put("type","Preprint");
            }
            updateHTMLview();
            updateRawData();
            item.save();
            return;
        }
        BibTeXRecord btr = new BibTeXRecord(jTABibTeX.getText());
        if (btr.parseError != 0) {
            toolbox.Warning(MF,"BibTeX entry not consistent: " + BibTeXRecord.status[btr.parseError], "Warning:");
            return;
        }
        jTABibTeX.setText(btr.toString());
        item.put("bibtex", btr.toString());
        item.put("citation-tag", btr.tag);
        if (item.get("type").equals("Paper") && (btr.get("journal")==null)) {
            item.put("type","Preprint");
        } else if (item.get("type").equals("Preprint") && (btr.get("journal")!=null)) {
            item.put("type","Paper");
        }
        updateHTMLview();
        updateRawData();
        item.save();
        if (item.error==6) toolbox.Warning(MF,"Error while saving information file.", "Exception:");
}//GEN-LAST:event_jBtnApplyBibTeXActionPerformed

    private void jBtnCreateBibTeXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCreateBibTeXActionPerformed
        if (item.get("type").equals("Book")) {
            jTABibTeX.setText("@book{,\n   author    = \"" + toolbox.ToBibTeXAuthors(item.getS("authors")) + "\",\n   title     = \"" + item.getS("title") + "\",\n   year      = \"\",\n   publisher = \"\",\n   location  = \"\"\n}");
        } else if (item.get("type").equals("Thesis")) {
            jTABibTeX.setText("@phdthesis{,\n   author     = \"" + toolbox.ToBibTeXAuthors(item.getS("authors")) + "\",\n   title      = \"" + item.getS("title") + "\",\n   university = \"\",\n   year       = \"\"\n}");
        } else {
            jTABibTeX.setText("@Article{,\n   author    = \"" + toolbox.ToBibTeXAuthors(item.getS("authors")) + "\",\n   title     = \"" + item.getS("title") + "\",\n   journal   = \"\",\n   volume    = \"\",\n   year      = \"\",\n   pages     = \"\"\n}");
        }
        jTABibTeX.setCaretPosition(0);
}//GEN-LAST:event_jBtnCreateBibTeXActionPerformed

    private void jBtnNormalizeBibTeXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNormalizeBibTeXActionPerformed
        String tmp = jTABibTeX.getText();
        tmp = toolbox.NormalizeBibTeX(tmp);
        jTABibTeX.setText(tmp);
        jTABibTeX.setCaretPosition(0);
}//GEN-LAST:event_jBtnNormalizeBibTeXActionPerformed

    private void jCBAddPropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBAddPropertyActionPerformed
        String val=(String)jCBAddProperty.getSelectedItem();
        if (!val.equals("add property")) {
            String bib=jTABibTeX.getText();
            if (bib.length()==0) {
                toolbox.Warning(MF,"Please create a BibTeX entry first with the \"Create\" button.", "Cancelled...");
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

    private void jCBBibPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBBibPluginsActionPerformed
        if (!jCBBibPlugins.isShowing()) return;
        if (jCBBibPlugins.getSelectedIndex()==0) {
            BibTeXRecord bibtex = new BibTeXRecord(item.get("bibtex"));
            if (bibtex.parseError == 0) {
                jTABibTeX.setText(bibtex.toString());
            } else {
                jTABibTeX.setText(item.get("bibtex"));
                if (bibtex.parseError < 250)
                    toolbox.Warning(MF,"BibTeX parsing error: " + BibTeXRecord.status[bibtex.parseError], "Warning:");
            }
            jTABibTeX.setCaretPosition(0);
            jBtnApplyBibTeX.setEnabled(true);
            jBtnCreateBibTeX.setEnabled(true);
            jBtnNormalizeBibTeX.setEnabled(true);
            jCBAddProperty.setEnabled(true);
        } else {
            jTABibTeX.setText(JM.getBibOutput(item));
            jTABibTeX.setCaretPosition(0);
            jBtnApplyBibTeX.setEnabled(false);
            jBtnCreateBibTeX.setEnabled(false);
            jBtnNormalizeBibTeX.setEnabled(false);
            jCBAddProperty.setEnabled(false);
        }
}//GEN-LAST:event_jCBBibPluginsActionPerformed

    private void jTLinksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTLinksMouseClicked
        if (evt.getClickCount() == 2) {
            StructureNode SN=(StructureNode)jTLinks.getSelectionPath().getLastPathComponent();
            if (SN==null) return;
            if ((!SN.isRoot()) && (SN.isLeaf())) {
                int i=Integer.valueOf(SN.getData("pos"));
                if (i>-1) {
                    String id=SN.getData("id");
                    item=new Item(Lib,id);
                    RSC.Configuration.view(item,null);
                }
            } else {
                String type=SN.getLabel();
                JM.showLinksOfType(type);
            }
        }
}//GEN-LAST:event_jTLinksMouseClicked

    private void jBtnAddLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAddLinkActionPerformed
        String type=jTFLinkType.getText();
        String target=jTFLinkTarget.getText();
        if ((target.length()<2) || (type.length()<2)) {
            toolbox.Warning(MF,"Both type and target must be specified", "Aborting...");
            return;
        }
        String links=item.getS("links");
        links+="|"+type+":"+target;
        if (links.startsWith("|")) links=links.substring(1);
        item.put("links", links);
        item.save();
        if (item.error==6) toolbox.Warning(MF,"Error writing document information file.","Warning");
}//GEN-LAST:event_jBtnAddLinkActionPerformed

    private void jBtnRemoveLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRemoveLinkActionPerformed
        StructureNode SN=(StructureNode)jTLinks.getSelectionPath().getLastPathComponent();
        if (SN==null) return;
        if ((SN!=null) && (SN.isLeaf())) {
            String l=SN.getData("link");
            String links=item.getS("links");
            links=Parser.Substitute(Parser.Substitute(links,l,""),"||","|");
            if (links.startsWith("|")) links=links.substring(1);
            item.put("links", links);
            item.save();
            if (item.error==6) toolbox.Warning(MF,"Error writing information file.", "Warning:");
        } else {
            toolbox.Warning(MF,"You selected no proper link to be deleted.","Cancelling...");
        }
        updateLinks();
}//GEN-LAST:event_jBtnRemoveLinkActionPerformed

    private void jBtnLinkHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLinkHelpActionPerformed
        toolbox.Information(MF,"Links are used to connect related items to each other.\nStandard types are \"citation\" and \"refers to\", while links\ncan be entered as field:value.", "Quick Help on Links");
}//GEN-LAST:event_jBtnLinkHelpActionPerformed

    private void jBtnCompressLinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCompressLinksActionPerformed
        StructureNode root=(StructureNode)jTLinks.getModel().getRoot();
        if (root.isLeaf()) return;
        String links="";
        for (Enumeration<StructureNode> e=root.children();e.hasMoreElements();) {
            StructureNode SN=e.nextElement();
            String type=SN.getLabel();
            for (Enumeration<StructureNode> f=SN.children();f.hasMoreElements();) {
                StructureNode sn=f.nextElement();
                if (sn.getData("pos").equals("-1")) {
                    links+="|"+sn.getData("link");
                } else {
                    links+="|"+type+":id:"+sn.getData("id");
                }
            }
        }
        links=links.substring(1);
        if (!links.equals(item.get("links"))) {
            item.put("links", links);
            item.save();
            if (item.error==6) toolbox.Warning(MF,"Error writing information file.", "Warning:");
        }
        updateRawData();
}//GEN-LAST:event_jBtnCompressLinksActionPerformed

    private void jPLinksComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPLinksComponentShown
        updateLinks();
}//GEN-LAST:event_jPLinksComponentShown

    private void jBtnApplyRemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyRemActionPerformed
        if (infoMode == 0) {
            item.put("remarks", jTARemarks.getText().trim());
            item.save();
            if (item.error==6) toolbox.Warning(MF,"Error while saving information file.", "Exception:");
        }
        if (infoMode == 1) {
            XMLHandler CD = Lib.PeopleRemarks;
            Lib.guaranteePerson();
            CD.put("remarks", jTARemarks.getText().trim());
            try {
                CD.writeBack();
            } catch (IOException ex) {
                toolbox.Warning(MF,"Error while saving person information file:\n" + ex.toString(), "Exception:");
                RSC.Msg1.printStackTrace(ex);
            }
        }
        if (infoMode == 2) {
            XMLHandler CD = Lib.CatRemarks;
            Lib.guaranteeCat();
            CD.put("remarks", jTARemarks.getText().trim());
            try {
                CD.writeBack();
            } catch (IOException ex) {
                toolbox.Warning(MF,"Error while saving author information file:\n" + ex.toString(), "Exception:");
                RSC.Msg1.printStackTrace(ex);
            }
        }
        updateHTMLview();
}//GEN-LAST:event_jBtnApplyRemActionPerformed

    private void jBtnInspect2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnInspect2ActionPerformed
        JM.viewPlainText(item);
}//GEN-LAST:event_jBtnInspect2ActionPerformed

    private void jBtnRawApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRawApplyActionPerformed
        String tag, value, tmp1;
        tmp1=jTARaw1.getText();
        String lines[]=tmp1.split("\n");
        for (int i=0;i<lines.length;i++) {
            tag=Parser.CutTill(lines[i], ":").trim();
            value=Parser.CutFrom(lines[i], ":").trim();
            if (value.length()==0) item.put(tag,null);
            else item.put(tag, value);
        }
        tmp1=jTARaw2.getText();
        lines=tmp1.split("\n");
        for (int i=0;i<lines.length;i++) {
            tag=Parser.CutTill(lines[i], ":").trim();
            value=Parser.CutFrom(lines[i], ":").trim();
            if (value.length()==0) item.put(tag,null);
            else item.put(tag, value);
        }
        item.save();
        JM.updateDTs(item.id);
        if (Lib.hasChanged()) {
            IT.Lib.updatePeopleAndKeywordsLists();
        }
        updateHTMLview();
        updateRawData();
        MF.updateStatusBar(false);
}//GEN-LAST:event_jBtnRawApplyActionPerformed

    private void jBtnRawUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRawUndoActionPerformed
        updateRawData();
    }//GEN-LAST:event_jBtnRawUndoActionPerformed

    private void jBtnRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRenameActionPerformed
        String oldname = item.getCompleteDirS("location");
        String newname = item.completeDir(item.getStandardFolder()) + toolbox.filesep + item.standardFileName(null);
        String newcompname = Lib.compressDir(newname);
        RSC.Msg1.repS("MAIN>>Renaming file: " + oldname + " --> " + newname);
        if (!(new File(item.completeDir(item.getStandardFolder()))).exists()) {
            if (!(new File(item.completeDir(item.getStandardFolder()))).mkdir()) {
                toolbox.Warning(MF,"Creating the item directory failed.", "Warning!");
                return;
            }
        }
        if ((new File(oldname)).renameTo(new File(newname))) {
            item.put("location", newcompname);
            item.save();
            Lib.setChanged(true);
            MF.updateStatusBar(false);
            updateRawData();
        } else {
            toolbox.Warning(MF,"Renaming failed.", "Warning!");
        }
}//GEN-LAST:event_jBtnRenameActionPerformed

    private void jBtnApplyPersonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyPersonActionPerformed
        Lib.guaranteePerson();
        String tag, value, tmp1;
        for (int i = 0; i < jTARaw3.getLineCount() - 1; i++) {
            try {
                int s = jTARaw3.getLineStartOffset(i);
                int end = jTARaw3.getLineEndOffset(i);
                tmp1 = jTARaw3.getText(s, end - s);
            } catch (BadLocationException BLE) {
                tmp1 = new String();
            }
            tag = Parser.CutTill(tmp1, ":").trim();
            value = Parser.CutFrom(tmp1, ":").trim();
            Lib.PeopleRemarks.put(tag, value);
        }
        try {
            Lib.PeopleRemarks.writeBack();
        } catch (IOException ex) {
            RSC.Msg1.printStackTrace(ex);
            toolbox.Warning(MF,"Error while saving information file:\n" + ex.toString(), "Exception:");
        }
        updateHTMLview();
}//GEN-LAST:event_jBtnApplyPersonActionPerformed

    private void jMIReloadDisplayStringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIReloadDisplayStringActionPerformed
        reloadDisplayString();
}//GEN-LAST:event_jMIReloadDisplayStringActionPerformed

    private void jMIEditDS1jMIEditDSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIEditDS1jMIEditDSActionPerformed
        String tmp = Lib.getDisplayString(infoMode);
        MultiLineEditor DMLE = new MultiLineEditor(RSC, "Edit HTML template", tmp);
        DMLE.setVisible(true);
        if (!DMLE.cancel) {
            Lib.setDisplayString(infoMode,DMLE.text);
            updateHTMLview();
        }
}//GEN-LAST:event_jMIEditDS1jMIEditDSActionPerformed

    private void jBtnInspect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnInspect1ActionPerformed
        String fn=item.getCompleteDirS("plaintxt");
        String results=toolbox.getFirstPage(fn);
        results=Parser.Substitute(results, "\n", "<br>");
        results="<html><body><b>First 4000 characters of this document:</b><hr>"+ results + "</body></html>";
        jEPInspect.setContentType("text/html");
        jEPInspect.setText(results);
        jEPInspect.setCaretPosition(0);
    }//GEN-LAST:event_jBtnInspect1ActionPerformed

    private void jTARaw1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTARaw1FocusGained
        if (!warned) {
            warned=true;
            toolbox.Warning(MF,"Properties appearing in both boxes should be edited in the right one!", "Warning:");
        }
    }//GEN-LAST:event_jTARaw1FocusGained

    private void jPMHTMLPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPMHTMLPopupMenuWillBecomeVisible
        Clipboard cb=Toolkit.getDefaultToolkit().getSystemClipboard();
        if ((infoMode==0) && cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            jMIAddThumb.setEnabled(true);
        } else {
            jMIAddThumb.setEnabled(false);
        }
    }//GEN-LAST:event_jPMHTMLPopupMenuWillBecomeVisible
    
    private void jMIAddThumbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMIAddThumbActionPerformed
        Clipboard cb=Toolkit.getDefaultToolkit().getSystemClipboard();
        if ((infoMode==0) && cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            try {
                createThumb(cb.getData(DataFlavor.imageFlavor));
            } catch (UnsupportedFlavorException ex) {
                RSC.Msg1.printStackTrace(ex);
            } catch (IOException ex) {
                RSC.Msg1.printStackTrace(ex);
            }
        } else {
            toolbox.Warning(MF,"Incompatible file type.", "Warning:");
        }
    }//GEN-LAST:event_jMIAddThumbActionPerformed

    private void jBtnAddThumbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAddThumbActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Indicate the file containing the thumbnail");
        FC.setCurrentDirectory(new File(RSC.getDir("thumbnail")));
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        FC.setFileFilter(new FFilter("_ALL", "All files"));
        RSC.setComponentFont(FC.getComponents());
        // Akzeptiert?
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            String filename = FC.getSelectedFile().getAbsolutePath();
            RSC.rememberDir("thumbnail", FC);
            try {
                String filetype=TextFile.getFileType(filename);
                TextFile.Delete(item.getCompleteDirS("thumbnail"));
                String target="AI::"+filetype;
                while ((new File(item.completeDir(target))).exists()) target+="x";
                item.put("thumbnail", target);
                TextFile.moveFile(filename, item.getCompleteDirS("thumbnail"));
                item.save();
                this.updateHTMLview();
                this.updateRawData();
            } catch (IOException ex) {
                toolbox.Warning(MF,"Error writing thumbnail: "+ex.toString(), "Warning:");
                RSC.Msg1.printStackTrace(ex);
            }
            JM.updateDTs(item.id);
        }
}//GEN-LAST:event_jBtnAddThumbActionPerformed

    private void jPThumbComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPThumbComponentShown
        updateThumb();
}//GEN-LAST:event_jPThumbComponentShown

    private void jBtnResizeThumbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResizeThumbActionPerformed
        try {
            String target = item.getCompleteDirS("thumbnail");
            BufferedImage bf = ImageIO.read(new File(target));
            System.out.println(bf.getWidth());
            int w = bf.getWidth();
            int h = bf.getHeight();
            System.out.println(w);
            System.out.println(h);
            double rx = (240+0.001) / w;
            double ry = (240+0.001) / h;
            double r = rx;
            if (ry < rx) {
                r = ry;
            }
            BufferedImageOp op = new AffineTransformOp(
              AffineTransform.getScaleInstance(r, r),
              new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                                 RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            BufferedImage img = op.filter(bf,null);
            String targetname=target;
            if (!target.endsWith("png"))
               targetname+=".png";
            System.out.println(targetname);
            ImageIO.write(img, "png", new File(targetname));
            item.putS("thumbnail", Lib.compressDir(targetname));
            if (!target.equals(targetname))
                TextFile.Delete(target);
            updateThumb();
            this.updateHTMLview();
            this.updateRawData();
        } catch (IOException ex) {
            RSC.Msg1.printStackTrace(ex);
        }
    }//GEN-LAST:event_jBtnResizeThumbActionPerformed

    private void jBtnRemoveThumbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRemoveThumbActionPerformed
        item.put("thumbnail",null);
        item.save();
        updateThumb();
        this.updateHTMLview();
        this.updateRawData();
    }//GEN-LAST:event_jBtnRemoveThumbActionPerformed

    private void jLFiles1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLFiles1MouseClicked
        if (evt.getClickCount() == 2) {
            String fn=jLFiles1.getSelectedValue();
            if (fn.length()>0) {
                fn=item.get("source")+toolbox.filesep+fn;
                RSC.Configuration.view("pdf", fn);
            }
        }
    }//GEN-LAST:event_jLFiles1MouseClicked

    private void jLFiles2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLFiles2MouseClicked
        if (evt.getClickCount() == 2) {
            String fn=jLFiles2.getSelectedValue();
            if (fn.length()>0) {
                fn=item.get("source")+toolbox.filesep+fn;
                RSC.Configuration.view("tex", fn);
            }
        }
    }//GEN-LAST:event_jLFiles2MouseClicked

    private void jLFiles3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLFiles3MouseClicked
        if (evt.getClickCount() == 2) {
            String fn=jLFiles3.getSelectedValue();
            if (fn.length()>0) {
                fn=item.get("source")+toolbox.filesep+fn;
                RSC.Configuration.view("---", fn);
            }
        }
    }//GEN-LAST:event_jLFiles3MouseClicked

    private void jBtnChooseSourceFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChooseSourceFolderActionPerformed
        JFileChooser FC = new JFileChooser();
        FC.setDialogTitle("Select the source folder for this item");
        FC.setCurrentDirectory(new File(RSC.getDir("sourcefolders")));
        FC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FC.setDialogType(JFileChooser.OPEN_DIALOG);
        FC.setFileFilter(new FFilter("_DIR", "Folders"));
        RSC.setComponentFont(FC.getComponents());
        // cancelled?
        if (!(FC.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)) {
            item.putS("source",FC.getSelectedFile().getAbsolutePath());
            item.save();
            RSC.rememberDir("sourcefolders", FC);
            updateRawData();
        }
    }//GEN-LAST:event_jBtnChooseSourceFolderActionPerformed

    private void jBtnShowCitedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnShowCitedActionPerformed
        String fn=jLFiles2.getSelectedValue();
        if (fn!=null)  {
            fn=item.get("source")+toolbox.filesep+fn;
            ItemTable IT=RSC.makeNewTabAvailable(5, "Cited in " + jLFiles2.getSelectedValue(),"magnifier");
            MF.noDocSelected();
            ProgressMonitor progressMonitor = new ProgressMonitor(this, "Looking for papers ...", "", 0, RSC.getCurrentSelectedLib().getSize());
            (new ThreadShowCited(RSC.getCurrentSelectedLib(), progressMonitor, fn,IT)).start();
        }
    }//GEN-LAST:event_jBtnShowCitedActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Panelx;
    private javax.swing.JButton jBtnAddLink;
    private javax.swing.JButton jBtnAddThumb;
    private javax.swing.JButton jBtnApplyBibTeX;
    private javax.swing.JButton jBtnApplyPerson;
    private javax.swing.JButton jBtnApplyRem;
    private javax.swing.JButton jBtnChooseSourceFolder;
    private javax.swing.JButton jBtnCompressLinks;
    private javax.swing.JButton jBtnCreateBibTeX;
    private javax.swing.JButton jBtnInspect1;
    private javax.swing.JButton jBtnInspect2;
    private javax.swing.JButton jBtnLinkHelp;
    private javax.swing.JButton jBtnNormalizeBibTeX;
    private javax.swing.JButton jBtnRawApply;
    private javax.swing.JButton jBtnRawUndo;
    private javax.swing.JButton jBtnRemoveLink;
    private javax.swing.JButton jBtnRemoveThumb;
    private javax.swing.JButton jBtnRename;
    private javax.swing.JButton jBtnResizeThumb;
    private javax.swing.JButton jBtnShowCited;
    private javax.swing.JComboBox jCBAddProperty;
    public javax.swing.JComboBox jCBBibPlugins;
    public javax.swing.JEditorPane jEPInspect;
    private javax.swing.JEditorPane jHTMLview;
    private javax.swing.JList<String> jLFiles1;
    private javax.swing.JList<String> jLFiles2;
    private javax.swing.JList<String> jLFiles3;
    private javax.swing.JLabel jLIcon;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuItem jMIAddThumb;
    public javax.swing.JMenuItem jMIEditDS1;
    public javax.swing.JMenuItem jMIReloadDisplayString;
    private javax.swing.JPanel jPBibData;
    private javax.swing.JPanel jPDocRaw;
    private javax.swing.JPanel jPFiles;
    private javax.swing.JPanel jPInspect;
    private javax.swing.JPanel jPLinks;
    private javax.swing.JPopupMenu jPMHTML;
    private javax.swing.JPanel jPPersonRaw;
    private javax.swing.JPanel jPRemarks;
    private javax.swing.JPanel jPThumb;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jSP3;
    private javax.swing.JSplitPane jSPraw;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextArea jTABibTeX;
    private javax.swing.JTextArea jTARaw1;
    private javax.swing.JTextArea jTARaw2;
    private javax.swing.JTextArea jTARaw3;
    private javax.swing.JTextArea jTARemarks;
    private javax.swing.JTextField jTFLinkTarget;
    private javax.swing.JTextField jTFLinkType;
    private javax.swing.JTree jTLinks;
    public javax.swing.JTabbedPane jTPDoc;
    // End of variables declaration//GEN-END:variables

    public void restoreCreationHTMLview() {
        IT.lastHTMLview=IT.creationHTMLview;
        restoreHTMLview();
    }

    public void restoreHTMLview() {
        jHTMLview.setContentType("text/html");
        if ((IT==null) || (IT.lastHTMLview==null) || (IT.lastHTMLview.length()==0))
            setStdHTML();
        else jHTMLview.setText(IT.lastHTMLview);
        jHTMLview.setCaretPosition(0);
    }

    public void setStdHTML() {
        kit = new HTMLEditorKit();
        kit.setStyleSheet(null);
        kit.getStyleSheet().addRule("body {color:#000; font-family:sans; margin: 4px; }");
        jHTMLview.setEditorKit(kit);
        jHTMLview.setContentType("text/html");
        jHTMLview.setText(RSC.stdHTMLstring);
        jHTMLview.setCaretPosition(0);
        jMIReloadDisplayString.setEnabled(false);
        MF.jMIReloadDisplayString2.setEnabled(false);
        MF.jMIEditDS.setEnabled(false);
        jMIEditDS1.setEnabled(false);
    }

    public void updateHTMLview() {
        if ((infoMode==-1) || (MF.jTPTabList.getSelectedIndex() < 0)) {
            setStdHTML();
            return;
        }
        jMIReloadDisplayString.setEnabled(true);
        MF.jMIReloadDisplayString2.setEnabled(true);
        MF.jMIEditDS.setEnabled(true);
        jMIEditDS1.setEnabled(true);
        IT.lastHTMLview=RSC.stdHTMLstring;
        jHTMLview.setContentType("text/html");
        if (Lib==null) {
            setStdHTML();
        } else {
            jHTMLview.setText(Lib.displayString(infoMode,item,RSC.JournalLinks));
            //System.out.println(Lib.displayString(infoMode,item,RSC.JournalLinks));
        }
        IT.lastHTMLview=jHTMLview.getText();
        if (infoMode!=0)
            IT.creationHTMLview=IT.lastHTMLview;
        jHTMLview.setCaretPosition(0);
        if (this.jTPDoc.getSelectedComponent()==this.jPLinks) {
            updateLinks();
        }
        updateThumb();
    }

    public void reloadDisplayString() {
        jHTMLview.setContentType("text/html");
        Lib.reloadDisplayString();
        updateHTMLview();
    }


    public void updateLinks() {
        if (infoMode==-1) return;
        if (item==null) return;
        DefaultTreeModel DTM=Lib.createLinksTree(item);
        jTLinks.setModel(DTM);
    }

    /**
     * Update the raw Data fields for the current document
     */
    public void updateRawData() {
        if (item==null) return;
        boolean docref=(item.get("filetype")==null);
        jBtnRename.setEnabled(!docref);
        MF.jMIReduceDR.setEnabled(!docref);
        MF.jMIReduceDR1.setEnabled(!docref);
        jTARaw1.setText(item.getRawData(1));
        jTARaw1.setCaretPosition(0);
        jTARaw2.setText(item.getRawData(2));
        jTARaw2.setCaretPosition(0);
        IT.replace(item);
        jTARemarks.setText(item.get("remarks"));
        jTARemarks.setCaretPosition(0);
        if (this.jCBBibPlugins.getSelectedIndex()==0) {
            BibTeXRecord bibtex = new BibTeXRecord(item.get("bibtex"));
            if (bibtex.parseError == 0) {
                jTABibTeX.setText(bibtex.toString());
            } else {
                jTABibTeX.setText(item.get("bibtex"));
                if (bibtex.parseError < 250)
                    toolbox.Warning(MF,"BibTeX parsing error: " + BibTeXRecord.status[bibtex.parseError], "Warning:");
            }
            jTABibTeX.setCaretPosition(0);
        } else {
            jTABibTeX.setText(JM.getBibOutput(item));
            jTABibTeX.setCaretPosition(0);
        }
        DefaultListModel listModel = (DefaultListModel) jLFiles1.getModel();
        listModel.clear();
        listModel = (DefaultListModel) jLFiles2.getModel();
        listModel.clear();
        listModel = (DefaultListModel) jLFiles3.getModel();
        listModel.clear();
        if (item.get("source")!=null) {
            // Fill File lists with pdf, tex and all
            listModel = new DefaultListModel();
            File folder = new File(item.get("source"));
            if (folder.exists()) {
                File[] listOfFiles;
                listOfFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".pdf");
                    }});
                Arrays.sort(listOfFiles, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(),f1.lastModified());
                    }
                });            
                for (int i = 0; i < listOfFiles.length; i++) {
                    listModel.addElement(listOfFiles[i].getName());
                }
                jLFiles1.setModel(listModel);
                // tex-files
                listModel = new DefaultListModel();
                folder = new File(item.get("source"));
                listOfFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".tex");
                    }});
                Arrays.sort(listOfFiles, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(),f1.lastModified());
                    }
                });            
                for (int i = 0; i < listOfFiles.length; i++) {
                    listModel.addElement(listOfFiles[i].getName());
                }
                jLFiles2.setModel(listModel);
                // all files
                listModel = new DefaultListModel();
                folder = new File(item.get("source"));
                listOfFiles = folder.listFiles();
                Arrays.sort(listOfFiles, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(),f1.lastModified());
                    }
                });            
                for (int i = 0; i < listOfFiles.length; i++) {
                    listModel.addElement(listOfFiles[i].getName());
                }
                jLFiles3.setModel(listModel);
            }
        }
    }

    public void setupPeopleRemarks() {
        if (Lib.personExists) {
            jTARemarks.setText(Lib.PeopleRemarks.get("remarks"));
            jTARaw3.setText(Lib.rawData(3));
        } else {
            jTARaw3.setText("person: " + Lib.CurrentPerson + "\n");
            jTARemarks.setText("No remarks found.");
        }
    }

    public void setupCatRemarks() {
        if (Lib.catExists) {
            jTARemarks.setText(Lib.CatRemarks.get("remarks"));
            //jTARaw3.setText(Lib.rawData(3));
        } else {
            //jTARaw3.setText("author: " + Lib.CurrentAuthor + "\n");
            jTARemarks.setText("No remarks found.");
        }
    }

    public void switchModeTo(int newMode) {
        if (newMode != 0) item=null;
        if (newMode == infoMode) {
            return;
        }
        while (jTPDoc.getTabCount() > 1) {
            jTPDoc.remove(1);
        }
        infoMode = newMode;
        if (infoMode > -1) {
            int j=0;
            for (int i = 0; i < iMC.get(infoMode).length; i++) {
                boolean hide=false;
                if ((Lib!=null) && (iMC.get(infoMode)[i]==jPBibData) && (Lib.Hide.contains("Tab:Bibliography"))) hide=true;
                if ((Lib!=null) && (iMC.get(infoMode)[i]==jPInspect) && (Lib.Hide.contains("Tab:Search"))) hide=true;
                if ((Lib!=null) && (iMC.get(infoMode)[i]==jPThumb) && (Lib.Hide.contains("Tab:Thumbnail"))) hide=true;
                if (!hide) {
                    jTPDoc.add(iMC.get(infoMode)[i]);
                    jTPDoc.setTabComponentAt(j+1,new TabLabel(iMC.get(infoMode)[i].getName(),iMII.get(infoMode)[i],RSC,null,false));
                    j++;
                }
            }
        }
    }

    public void restoreCreationType() {
        if (IT.creationType==0) {
            switchModeTo0(this.IT,item);
        }
        if (IT.creationType==3) {
            switchModeTo3(this.IT);
        }
        restoreCreationHTMLview();
    }

    public void switchModeTo1() {
        setItem(null,null);
        setLibrary(null);
        switchModeTo(-1);
        updateHTMLview();
    }

    public void switchModeTo0(ItemTable dt, Item d) {
        setItem(d,dt);
        switchModeTo(0);
        updateHTMLview();
        updateRawData();
        MF.DocSelected();
    }

    public void switchModeTo2(ItemTable dt) {
        setItem(null,dt);
        switchModeTo(2);
        MF.CatSelected();
        MF.noDocSelected();
        updateHTMLview();
    }

    public void switchModeTo3(ItemTable dt) {
        setItem(null,dt);
        setLibrary(dt.Lib);
        switchModeTo(3);
        updateHTMLview();
        MF.DocSelected();
    }

//    public void dragEnter(DropTargetDragEvent dtde) {
//        if (!acceptData(dtde.getTransferable()))
//            dtde.rejectDrag();
//    }
//
//    public void dragOver(DropTargetDragEvent dtde) {
//        if (!acceptData(dtde.getTransferable()))
//            dtde.rejectDrag();
//    }
//
//    public void dropActionChanged(DropTargetDragEvent dtde) {
//        System.out.println("Drop action changed");
//    }
//
//    public void dragExit(DropTargetEvent dte) {
//        System.out.println("Drag exit");
//        System.out.println("Drop3:"+dte.toString());
//    }
//
//    public void drop(DropTargetDropEvent dtde) {
//        dtde.acceptDrop(DnDConstants.ACTION_COPY);
//        System.out.println("Drop1");
//        if (acceptData(dtde.getTransferable())) {
//            System.out.println("Drop2");
//            dtde.acceptDrop(dtde.getDropAction());
//            try {
//                System.out.println("Drop3:"+dtde.getTransferable().getTransferData(DataFlavor.imageFlavor).toString());
//                createThumb(dtde.getTransferable().getTransferData(DataFlavor.imageFlavor));
//            } catch (UnsupportedFlavorException ex) {
//                RSC.Msg1.printStackTrace(ex);
//            } catch (IOException ex) {
//                RSC.Msg1.printStackTrace(ex);
//            }
//        }
//    }

//    private boolean acceptData(Transferable t) {
//        boolean ret = false;
//        try {
//            if (t == null) {
//                return (false);
//            }
//            if (infoMode != 0) {
//                return (false);
//            }
//            ret = t.isDataFlavorSupported(DataFlavor.imageFlavor);
//        } catch (Exception e) {
//            ret = false;
//            MF.Msg1.printStackTrace(e);
//        }
//        return (ret);
//    }
//
    private void createThumb(Object o) {
        try {
            Image i = (Image)o;
            String target=item.get("thumbnail");
            if (target==null) {
                target="AI::";
                if (!item.completeDir(target+".jpg").equals(item.getCompleteDirS("thumbnail")))
                    while ((new File(item.completeDir(target+".jpg"))).exists()) target+="x";
            }
            item.put("thumbnail", target+".jpg");
            ImageIO.write( (RenderedImage) i, "jpeg", new File( item.getCompleteDirS("thumbnail")) );
            item.save();
            this.updateHTMLview();
            this.updateRawData();
        } catch (IOException ex) {
            toolbox.Warning(MF,"Error writing thumbnail: "+ex.toString(), "Warning:");
            RSC.Msg1.printStackTrace(ex);
        }
        JM.updateDTs(item.id);
    }
    
  private static TransferHandler createTransferHandler(JInfoPanel jip){
    return new TransferHandler(  ){
      @Override
      public boolean importData( JComponent comp, Transferable aTransferable ) {
        System.out.println("IMPORT1");
        Object o = null;
        Image image = null;
        try {
            DataFlavor[] transferData2 = aTransferable.getTransferDataFlavors();
            for(DataFlavor df:transferData2){
                System.out.println(df.toString());
            }
//            if (aTransferable.isDataFlavorSupported (DataFlavor.imageFlavor)) {
//                try {
//                        o = aTransferable.getTransferData (DataFlavor.imageFlavor);
//                } catch (UnsupportedFlavorException ufe) {
//                        ufe.printStackTrace ();
//                } catch (IOException ioe) {
//                        ioe.printStackTrace ();
//                }
//                System.out.println(o.toString());
//            }
            if (aTransferable.isDataFlavorSupported (DataFlavor.stringFlavor)) {
                o = aTransferable.getTransferData (DataFlavor.stringFlavor);
                URL url = new URL((String) o);
                image = ImageIO.read(url);
                if (image!=null) {
                    jip.createThumb(image);
                }
            }
//            if (aTransferable.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
//
//            try {
//                    o = aTransferable.getTransferData (DataFlavor.javaFileListFlavor);
//            } catch (UnsupportedFlavorException ufe) {
//                    ufe.printStackTrace ();
//            } catch (IOException ioe) {
//                    ioe.printStackTrace ();
//            }
//
//            // if o is still null we had an exception
//            if ((o != null) && (o instanceof List)) {
//                    List  fileList = (List) o;
//                    final int length = fileList.size ();
//
//                    for (int i = 0; i < length; ++ i) {
//                        System.out.println(((File) fileList.get (i)).toString());
//                    }
//            }            
//            }
//            
//            System.out.println("IMPORT1.5");
//          Object transferData = aTransferable.getTransferData( DataFlavor.imageFlavor );
//            System.out.println("IMPORT2");
//          jip.createThumb(transferData);
//            System.out.println("IMPORT3");
        } catch ( UnsupportedFlavorException e ) {
            System.out.println("UFE: "+e.toString());
        } catch ( IOException e ) {
            System.out.println("IOE: "+e.toString());
        }
        return true;
      }

      @Override
      public boolean canImport( JComponent comp, DataFlavor[] transferFlavors ) {
        return true;
      }
    };
  }    


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DeepSearch.java
 *
 * Created on 24.03.2010, 17:17:03
 */

package celsius.Dialogs;

import celsius.*;
import celsius.Threads.ThreadSearchDetail;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

/**
 *
 * @author cnsaeman
 */
public class DeepSearch extends javax.swing.JDialog {

    private final MainFrame MF;
    private final JobManager JM;
    private final Resources RSC;

    public ThreadSearchDetail threadSearch;

    public final HashMap<String,JCheckBox> checkBoxes;
    public final HashMap<String,JComboBox> comboBoxes;
    public final HashMap<String,RangeEditor> rangeEditors;

    /** Creates new form DeepSearch */
    public DeepSearch(MainFrame mf) {
        super(mf,false);
        MF=mf;
        JM=MF.JM;
        RSC=MF.RSC;
        initComponents();
        setTitle("Detailed search");
        setIconImage(RSC.getOriginalIcon("magnifier"));
        checkBoxes=new HashMap<String,JCheckBox>();
        comboBoxes=new HashMap<String,JComboBox>();
        rangeEditors=new HashMap<String,RangeEditor>();
        toolbox.centerFrame(this);
    }
    public JPanel createPanel() {
        JPanel Pnl=new JPanel();
        Pnl.setLayout(new GridLayout(0,1,0,0));
        return(Pnl);
    }

    public JLabel createLabel(String s) {
        JLabel jl=new JLabel(Parser.LowerEndOfWords(s)+":");
        jl.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jl.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return(jl);
    }

    public JComboBox createComboBox() {
        JComboBox CB=new JComboBox();
        CB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        CB.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        return(CB);
    }

    public void setLib(Library Lib) {
        jPCheck.removeAll();
        jPLarge.removeAll();
        jPCheck.add(jCBAll);
        String[] searchtags=Lib.listOf("searchtags");
        boolean done;
        comboBoxes.clear();
        for (int i=0;i<searchtags.length;i++) {
            done=false;
            if (Lib.ChoiceFields.containsKey(searchtags[i])) {
                JPanel Pnl = createPanel();
                JLabel jl=createLabel(searchtags[i]);
                Pnl.add(jl);
                JComboBox CB=createComboBox();
                CB.setRenderer(new CBRenderer(RSC.Icons,RSC.getCurrentSelectedLib().IconDictionary,RSC));
                DefaultComboBoxModel DCBM=(DefaultComboBoxModel) CB.getModel();
                DCBM.addElement("arbitrary");
                ArrayList<String> list=Lib.ChoiceFields.get(searchtags[i]);
                for (String t : list)
                    DCBM.addElement(t);
                Pnl.add(CB);
                jPLarge.add(Pnl);
                comboBoxes.put(searchtags[i],CB);
                done=true;
            }
            if (searchtags[i].equals("pages")) {
                JPanel Pnl = createPanel();
                JLabel jl=createLabel("length");
                Pnl.add(jl);
                JComboBox CB=createComboBox();
                DefaultComboBoxModel DCBM=(DefaultComboBoxModel) CB.getModel();
                DCBM.addElement("arbitrary size");
                DCBM.addElement("very small (<5)");
                DCBM.addElement("small (<15)");
                DCBM.addElement("medium (15-40)");
                DCBM.addElement("large (40-100)");
                DCBM.addElement("very large (100-200)");
                DCBM.addElement("huge (>200)");
                Pnl.add(CB);
                jPLarge.add(Pnl);
                comboBoxes.put("pages",CB);
                done=true;
            }
            if (searchtags[i].equals("lastmodified")) {
                JPanel Pnl = createPanel();
                JLabel jl=createLabel("last modified");
                Pnl.add(jl);
                JComboBox CB=createComboBox();
                DefaultComboBoxModel DCBM=(DefaultComboBoxModel) CB.getModel();
                DCBM.addElement("arbitrary");
                DCBM.addElement("today");
                DCBM.addElement("last week");
                DCBM.addElement("last month");
                Pnl.add(CB);
                jPLarge.add(Pnl);
                comboBoxes.put("lastmodified",CB);
                done=true;
            }
            if (searchtags[i].equals("filetype")) {
                JPanel Pnl = createPanel();
                JLabel jl=createLabel("file type");
                Pnl.add(jl);
                JComboBox CB=createComboBox();
                DefaultComboBoxModel DCBM=(DefaultComboBoxModel) CB.getModel();
                DCBM.addElement("arbitrary");
                DCBM.addElement("item ref");
                String sft = Lib.MainFile.get("filetypes");
                if (!sft.equals("*")) {
                    String [] filetypes=sft.split("\\|");
                    for (int j=0;j<filetypes.length;j++)
                        DCBM.addElement(filetypes[j]);
                } else {
                    RSC.Configuration.addTypesCBM(DCBM);
                }
                Pnl.add(CB);
                jPLarge.add(Pnl);
                comboBoxes.put("filetype",CB);
                done=true;
            }
            if (searchtags[i].equals("date")) {
                RangeEditor RE=new RangeEditor("Date:",RSC);
                jPLarge.add(RE);
                rangeEditors.put("date",RE);
                done=true;
            }
            if (searchtags[i].equals("filesize")) {
                RangeEditor RE=new RangeEditor("Size:",RSC);
                jPLarge.add(RE);
                rangeEditors.put("filesize",RE);
                done=true;
            }
            if (searchtags[i].equals("distance")) {
                RangeEditor RE=new RangeEditor("Distance:",RSC);
                jPLarge.add(RE);
                rangeEditors.put("distance",RE);
                done=true;
            }
            if (searchtags[i].equals("plaintext")) {
                JCheckBox cb = new JCheckBox("Extracted plain text");
                cb.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11))); // NOI18N
                cb.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
                cb.setMargin(new java.awt.Insets(0, 0, 0, 0));
                jPCheck.add(cb);
                checkBoxes.put("plaintext",cb);
                done=true;
            }
            if (!done) {
                JCheckBox cb = new JCheckBox();
                cb.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11))); // NOI18N
                cb.setText(Parser.LowerEndOfWords2(searchtags[i]));
                cb.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
                cb.setMargin(new java.awt.Insets(0, 0, 0, 0));
                jPCheck.add(cb);
                checkBoxes.put(searchtags[i],cb);
            }
        }
        jPCheck.add(jCBSselected);
        jPCheck.add(jCBScurrent);
        jPCheck.add(jCBHidden);
        this.pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPCheck = new javax.swing.JPanel();
        jCBAll = new javax.swing.JCheckBox();
        jCBSselected = new javax.swing.JCheckBox();
        jCBScurrent = new javax.swing.JCheckBox();
        jCBHidden = new javax.swing.JCheckBox();
        jPLarge = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jBtnSearch = new javax.swing.JButton();
        jBtnStop = new javax.swing.JButton();
        jRBbackwards = new javax.swing.JRadioButton();
        jRBforwards = new javax.swing.JRadioButton();
        jCBexactmatch = new javax.swing.JCheckBox();
        jBtnClrSrchGen = new javax.swing.JButton();
        jTFSearch = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setTitle("Detailed Search");

        jPCheck.setLayout(new java.awt.GridLayout(0, 2, 5, 5));

        jCBAll.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jCBAll.setSelected(true);
        jCBAll.setText("All Metadata");
        jCBAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCBAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPCheck.add(jCBAll);

        jCBSselected.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jCBSselected.setText("Only selected category");
        jCBSselected.setToolTipText("search only in selected category");
        jCBSselected.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCBSselected.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPCheck.add(jCBSselected);

        jCBScurrent.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jCBScurrent.setText("Only current table");
        jCBScurrent.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCBScurrent.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPCheck.add(jCBScurrent);

        jCBHidden.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jCBHidden.setSelected(true);
        jCBHidden.setText("include hidden items");
        jCBHidden.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCBHidden.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPCheck.add(jCBHidden);

        jPLarge.setLayout(new java.awt.GridLayout(0, 2, 5, 5));

        jButton1.setText("Close");
        jButton1.setPreferredSize(new java.awt.Dimension(RSC.guiScale(73), RSC.guiScale(20)));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jBtnSearch.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(12)));
        jBtnSearch.setIcon(new javax.swing.ImageIcon(RSC.getImageURL("play_green"))); // NOI18N
        jBtnSearch.setPreferredSize(new java.awt.Dimension(RSC.guiScale(63), RSC.guiScale(20)));
        jBtnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSearchActionPerformed(evt);
            }
        });

        jBtnStop.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(12)));
        jBtnStop.setIcon(new javax.swing.ImageIcon(RSC.getImageURL("stop_red"))); // NOI18N
        jBtnStop.setEnabled(false);
        jBtnStop.setPreferredSize(new java.awt.Dimension(RSC.guiScale(62), RSC.guiScale(20)));
        jBtnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnStopActionPerformed(evt);
            }
        });

        buttonGroup3.add(jRBbackwards);
        jRBbackwards.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jRBbackwards.setIcon(new javax.swing.ImageIcon(RSC.getImageURL("rewind_gray"))); // NOI18N
        jRBbackwards.setSelectedIcon(new javax.swing.ImageIcon(RSC.getImageURL("rewind_green"))); // NOI18N

        buttonGroup3.add(jRBforwards);
        jRBforwards.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jRBforwards.setSelected(true);
        jRBforwards.setIcon(new javax.swing.ImageIcon(RSC.getImageURL("forward_gray"))); // NOI18N
        jRBforwards.setSelectedIcon(new javax.swing.ImageIcon(RSC.getImageURL("forward_green"))); // NOI18N

        jCBexactmatch.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jCBexactmatch.setSelected(true);
        jCBexactmatch.setText("exact match");
        jCBexactmatch.setBorder(null);

        jBtnClrSrchGen.setIcon(new javax.swing.ImageIcon(RSC.getImageURL("closebtn"))); // NOI18N
        jBtnClrSrchGen.setBorderPainted(false);
        jBtnClrSrchGen.setContentAreaFilled(false);
        jBtnClrSrchGen.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jBtnClrSrchGen.setPreferredSize(new java.awt.Dimension(RSC.guiScale(17), RSC.guiScale(16)));
        jBtnClrSrchGen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClrSrchGenActionPerformed(evt);
            }
        });

        jTFSearch.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jTFSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTFSearchKeyTyped(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jLabel5.setIcon(new javax.swing.ImageIcon(RSC.getImageURL("magnifier"))); // NOI18N
        jLabel5.setText("Search:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPCheck, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(299), Short.MAX_VALUE)
                    .addComponent(jPLarge, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(299), Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jBtnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, RSC.guiScale(38), javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnStop, javax.swing.GroupLayout.PREFERRED_SIZE, RSC.guiScale(40), javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRBbackwards)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRBforwards)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
                        .addComponent(jCBexactmatch))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jTFSearch, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(276), Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnClrSrchGen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jCBexactmatch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jBtnClrSrchGen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTFSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPCheck, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(35), Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPLarge, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(8), Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jRBforwards)
                        .addComponent(jRBbackwards)
                        .addComponent(jBtnStop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnClrSrchGenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClrSrchGenActionPerformed
        jCBexactmatch.setSelected(true);
        jCBAll.setSelected(true);
        if (checkBoxes.containsKey("plaintext")) checkBoxes.get("plaintext").setSelected(false);
        jCBSselected.setSelected(false);
        jCBScurrent.setSelected(false);
        jTFSearch.setText("");
}//GEN-LAST:event_jBtnClrSrchGenActionPerformed

    private void jTFSearchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTFSearchKeyTyped
        stopSearch();
        if (evt.isAltDown()) {
            return;
        }
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            startSearch(jTFSearch.getText());
        }
}//GEN-LAST:event_jTFSearchKeyTyped

    private void jBtnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSearchActionPerformed
        startSearch(jTFSearch.getText());
}//GEN-LAST:event_jBtnSearchActionPerformed

    private void jBtnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnStopActionPerformed
        jBtnSearch.setEnabled(true);
        jBtnStop.setEnabled(false);
        stopSearch();
}//GEN-LAST:event_jBtnStopActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jBtnClrSrchGen;
    public javax.swing.JButton jBtnSearch;
    public javax.swing.JButton jBtnStop;
    private javax.swing.JButton jButton1;
    public javax.swing.JCheckBox jCBAll;
    public javax.swing.JCheckBox jCBHidden;
    public javax.swing.JCheckBox jCBScurrent;
    public javax.swing.JCheckBox jCBSselected;
    public javax.swing.JCheckBox jCBexactmatch;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPCheck;
    private javax.swing.JPanel jPLarge;
    public javax.swing.JRadioButton jRBbackwards;
    public javax.swing.JRadioButton jRBforwards;
    public javax.swing.JTextField jTFSearch;
    // End of variables declaration//GEN-END:variables


    public void startSearch(String srch) {
        Library CSL=RSC.getCurrentSelectedLib();
        MF.jIP.setLibrary(CSL);
        stopSearch();
        ItemTable IT=RSC.makeTabAvailable(5, srch,"magnifier");
        threadSearch=new ThreadSearchDetail(MF,CSL,IT,srch);
        threadSearch.start();
    }

    public void stopSearch() {
        if (threadSearch!=null)
            if (threadSearch.running) {
                threadSearch.interrupt();
                try {
                    threadSearch.join(100);
                } catch (InterruptedException ex) {
                    RSC.Msg1.printStackTrace(ex);
                }
            }
    }

}

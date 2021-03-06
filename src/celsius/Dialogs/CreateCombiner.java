/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CreateCombiner.java
 *
 * Created on 06.05.2010, 19:41:15
 */

package celsius.Dialogs;

import celsius.*;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.util.ArrayList;
import javax.swing.table.TableColumn;

/**
 *
 * @author cnsaeman
 */
public final class CreateCombiner extends javax.swing.JDialog {

    final private ArrayList<Item> items;
    private Item combiner;
    final private KeyValueTableModel KVTM;
    final private Library Lib;
    final private Resources RSC;
    public Item addedCombiner;

    /** Creates new form CreateCombiner */
    public CreateCombiner(MainFrame MF, ArrayList<Item> ds) {
        super(MF, true);
        RSC=MF.RSC;
        Lib=RSC.getCurrentSelectedLib();
        combiner=new Item();
        items=ds;
        initComponents();
        jTCombiner.setModel(new KeyValueTableModel("Tag", "Value"));
        addedCombiner=null;
        ArrayList<String> tags=new ArrayList<String>();
        for (String t : items.get(0).totalKeySet()) {
            if (relevant(t)) {
                String iv=items.get(0).get(t);
                if (iv!=null) {
                    tags.add(t);
                    combiner.put(t,iv);
                }
            }
        }
        ArrayList<String> shortenedTags=new ArrayList<String>();
        for (String t : tags) {
            String cv=combiner.get(t);
            for (Item it : items) {
                String iv=it.get(t);
                if (!cv.equals(iv)) {
                    int i=0;
                    while ((cv!=null) && (iv!=null) && (i<cv.length()) && (i<iv.length()) && (cv.charAt(i)==iv.charAt(i))) i++;
                    cv=cv.substring(0,i).trim();
                    while (cv.endsWith("-") || cv.endsWith(" ")) cv=cv.substring(0,cv.length()-1);
                    combiner.put(t,cv);
                    if (!shortenedTags.contains(t)) shortenedTags.add(t);
                }
            }
        }
        KVTM=new KeyValueTableModel("Tag", "Value");
        String combination="";
        for (String k : tags) {
            String t=combiner.get(k);
            if (t==null) t="<unknown>";
            else {
                if (t.length()>0) {
                    if (shortenedTags.contains(k)) {
                        combination+="|"+k+":"+t+"*";
                    } else {
                        combination+="|"+k+":"+t;
                    }
                }
            }
            KVTM.addRow(Parser.LowerEndOfWords(k), t);
        }
        jTFCriteria.setText(combination.substring(1));
        jTCombiner.setModel(KVTM);
        jTCombiner.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jTCombiner.getTableHeader().setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11)));
        jTCombiner.setRowHeight(RSC.guiScale(16));
        TableColumn column = jTCombiner.getColumnModel().getColumn(1);
        column.setPreferredWidth(230);
        column.setCellEditor(new InfoEditor(RSC, Lib.ChoiceFields, Lib.IconFields, Lib.IconDictionary));
        column.setPreferredWidth(400);
        column.setWidth(400);
        pack();
        toolbox.centerDialog(this,MF);
    }

    public boolean relevant(String t) {
        if (t.equals("id")) return(false);
        if (t.equals("location")) return(false);
        if (t.equals("filetype")) return(false);
        if (t.equals("addinfo")) return(false);
        return(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpType = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jRBLink = new javax.swing.JRadioButton();
        jRBCombine = new javax.swing.JRadioButton();
        jTFCriteria = new javax.swing.JTextField();
        jCBHidden = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTCombiner = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jBtnCreate = new javax.swing.JButton();
        jBtnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create a combining item");

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setPreferredSize(new java.awt.Dimension(RSC.guiScale(400), RSC.guiScale(25)));

        jLabel1.setText("Properties of the combining item:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(410, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        btnGrpType.add(jRBLink);
        jRBLink.setSelected(true);
        jRBLink.setText("Link all selected items");

        btnGrpType.add(jRBCombine);
        jRBCombine.setText("Combine items satisfying the criteria");

        jCBHidden.setSelected(true);
        jCBHidden.setText("set \"hidden\" attribute in combined items");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jRBLink)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCBHidden))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jRBCombine)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTFCriteria, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(340), Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRBLink)
                    .addComponent(jCBHidden))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRBCombine)
                    .addComponent(jTFCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel5, java.awt.BorderLayout.SOUTH);

        jTCombiner.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ));
        jScrollPane1.setViewportView(jTCombiner);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setPreferredSize(new java.awt.Dimension(RSC.guiScale(400), RSC.guiScale(35)));

        jBtnCreate.setText("Create Combiner");
        jBtnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCreateActionPerformed(evt);
            }
        });

        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(399, Short.MAX_VALUE)
                .addComponent(jBtnCreate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnCancel))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnCancel)
                    .addComponent(jBtnCreate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jBtnCancelActionPerformed

    private void jBtnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCreateActionPerformed
        //Collections.sort(items,Lib.getComparator(null,true,0));
        readOutTable();
        if (jRBLink.isSelected()) {
            String links="";
            Lib.addItem(combiner, "", 0);
            addedCombiner=Lib.lastAddedItem;
            for (Item it : items) {
                links+="|combines:id:"+it.get("id");
                if (jCBHidden.isSelected()) it.setAttribute("hidden");
                it.addLink("combined","id:"+addedCombiner.get("id"));
                it.save();
            }
            addedCombiner.put("links", links.substring(1));
            addedCombiner.save();
        } else {
            Lib.addItem(combiner, "", 0);
            addedCombiner=Lib.lastAddedItem;
            addedCombiner.put("combine",jTFCriteria.getText());
        }
        /*Item doc2=items.get(0).Lib.createEmptyItem();
        String combine=new String("");
        for(String tag : tags) {
            if (fields.get(tag).getText().trim().length()>0) {
                doc2.put(tag, items.get(0).get(tag));
                combine+="|"+tag+":"+items.get(0).get(tag);
            }
        }
        doc2.put("combine",combine.substring(1));
        doc2.save();*/
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jBtnCreateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGrpType;
    private javax.swing.JButton jBtnCancel;
    private javax.swing.JButton jBtnCreate;
    private javax.swing.JCheckBox jCBHidden;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRBCombine;
    private javax.swing.JRadioButton jRBLink;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTCombiner;
    private javax.swing.JTextField jTFCriteria;
    // End of variables declaration//GEN-END:variables

    private void readOutTable() {
        combiner=new Item();
        for (int i=0; i<KVTM.getRowCount(); i++) {
            String t=((String) KVTM.getValueAt(i,0)).toLowerCase();
            String v=(String) KVTM.getValueAt(i,1);
            if (!v.equals("<unknown>")) {
                combiner.put(t, v);
            }
        }
    }

}

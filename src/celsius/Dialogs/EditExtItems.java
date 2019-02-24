/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EditExtItems.java
 *
 * Created on 08.07.2011, 21:59:55
 */

package celsius.Dialogs;

import celsius.Item;
import celsius.Resources;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.util.ArrayList;
import javax.swing.JDialog;

/**
 *
 * @author cnsaeman
 */
public class EditExtItems extends javax.swing.JDialog {

    private final ArrayList<Item> docs;
    private final String key;
    private final Resources RSC;

    /** Creates new form EditExtItems */
    public EditExtItems(JDialog parent,ArrayList<Item> d,String k,Resources rsc) {
        super(parent);
        RSC=rsc;        
        initComponents();
        docs=d;
        key=k.toLowerCase();
        String list="";
        for (Item i : docs) {
            list+="\n"+i.getS(key);
        }
        jTAList.setText(list.substring(1));
        jTAList.setCaretPosition(0);
        celsius.tools.toolbox.centerFrame(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTAList = new javax.swing.JTextArea();
        jBtnApply = new javax.swing.JButton();
        jBtnCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jBtnReset = new javax.swing.JButton();
        jBtnAuthorConv = new javax.swing.JButton();
        jBtnCut2 = new javax.swing.JButton();
        jBtnCut1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTF3 = new javax.swing.JTextField();
        jTF4 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jBtnReplace = new javax.swing.JButton();
        jTF1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTF2 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jBtnAuthorConv1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Key of Multiple Items");
        setModal(true);

        jTAList.setColumns(20);
        jTAList.setRows(5);
        jScrollPane1.setViewportView(jTAList);

        jBtnApply.setText("Apply changes");
        jBtnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyActionPerformed(evt);
            }
        });

        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelActionPerformed(evt);
            }
        });

        jBtnReset.setText("Reset");
        jBtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResetActionPerformed(evt);
            }
        });

        jBtnAuthorConv.setText("People conv.");
        jBtnAuthorConv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAuthorConvActionPerformed(evt);
            }
        });

        jBtnCut2.setText("Cut");
        jBtnCut2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCut2ActionPerformed(evt);
            }
        });

        jBtnCut1.setText("Cut");
        jBtnCut1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCut1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Cut from");

        jLabel3.setText("Cut until");

        jBtnReplace.setText("Replace");
        jBtnReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnReplaceActionPerformed(evt);
            }
        });

        jLabel1.setText("Replace");

        jLabel2.setText("with");

        jBtnAuthorConv1.setText("End of words lowercase");
        jBtnAuthorConv1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAuthorConv1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jBtnCut1)
                            .addComponent(jTF3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(118), Short.MAX_VALUE)
                            .addComponent(jTF1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(118), Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jBtnAuthorConv, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jBtnReplace)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(50, 50, 50)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jBtnCut2)
                                            .addComponent(jBtnReset)))))
                            .addComponent(jTF2, javax.swing.GroupLayout.PREFERRED_SIZE, RSC.guiScale(118), javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTF4, javax.swing.GroupLayout.PREFERRED_SIZE, RSC.guiScale(118), javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jBtnAuthorConv1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTF1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTF2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTF3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTF4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jBtnReplace)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(25, 25, 25)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnCut2)
                    .addComponent(jBtnCut1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnReset)
                    .addComponent(jBtnAuthorConv))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnAuthorConv1)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(407), Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jBtnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnApply))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(316), Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jBtnApply)
                            .addComponent(jBtnCancel))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyActionPerformed
        String[] lines=jTAList.getText().split("\n");
        for (int i=0;i<lines.length;i++) {
            docs.get(i).put(key, lines[i]);
            docs.get(i).save();
        }
        this.dispose();
}//GEN-LAST:event_jBtnApplyActionPerformed

    private void jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelActionPerformed
        this.dispose();
}//GEN-LAST:event_jBtnCancelActionPerformed

    private void jBtnReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnReplaceActionPerformed
        String[] lines=jTAList.getText().split("\n");
        String out="";
        for (String s : lines) {
            out+="\n"+Parser.Substitute(s, jTF1.getText(), jTF2.getText());
        }
        jTAList.setText(out.substring(1));
}//GEN-LAST:event_jBtnReplaceActionPerformed

    private void jBtnCut2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCut2ActionPerformed
        String[] lines=jTAList.getText().split("\n");
        String out="";
        for (String s : lines) {
            out+="\n"+Parser.CutTill(s, jTF4.getText());
        }
        jTAList.setText(out.substring(1));
}//GEN-LAST:event_jBtnCut2ActionPerformed

    private void jBtnCut1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCut1ActionPerformed
        String[] lines=jTAList.getText().split("\n");
        String out="";
        for (String s : lines) {
            out+="\n"+Parser.CutFrom(s, jTF3.getText());
        }
        jTAList.setText(out.substring(1));
}//GEN-LAST:event_jBtnCut1ActionPerformed

    private void jBtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResetActionPerformed
       String list="";
        for (Item i : docs) {
            list+="\n"+i.getS(key);
        }
        jTAList.setText(list.substring(1));
        jTAList.setCaretPosition(0);
    }//GEN-LAST:event_jBtnResetActionPerformed

    private void jBtnAuthorConvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAuthorConvActionPerformed
        String[] lines=jTAList.getText().split("\n");
        String out="";
        for (String s : lines) {
            out+="\n"+toolbox.authorsBibTeX2Cel(s);
        }
        jTAList.setText(out.substring(1));
    }//GEN-LAST:event_jBtnAuthorConvActionPerformed

    private void jBtnAuthorConv1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAuthorConv1ActionPerformed
        String[] lines=jTAList.getText().split("\n");
        String out="";
        for (String s : lines) {
            out+="\n"+Parser.LowerEndOfWords(s);
        }
        jTAList.setText(out.substring(1));
    }//GEN-LAST:event_jBtnAuthorConv1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnApply;
    private javax.swing.JButton jBtnAuthorConv;
    private javax.swing.JButton jBtnAuthorConv1;
    private javax.swing.JButton jBtnCancel;
    private javax.swing.JButton jBtnCut1;
    private javax.swing.JButton jBtnCut2;
    private javax.swing.JButton jBtnReplace;
    private javax.swing.JButton jBtnReset;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTAList;
    private javax.swing.JTextField jTF1;
    private javax.swing.JTextField jTF2;
    private javax.swing.JTextField jTF3;
    private javax.swing.JTextField jTF4;
    // End of variables declaration//GEN-END:variables

}

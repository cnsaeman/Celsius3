/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogEditLibrary.java
 *
 * Created on 18.02.2010, 22:23:30
 */

package celsius.Dialogs;

import celsius.Library;
import celsius.MainFrame;
import celsius.Resources;
import celsius.StateManager;
import celsius.tools.toolbox;
import javax.swing.DefaultListModel;

/**
 *
 * @author cnsaeman
 */
public class ChooseUSBDevice extends javax.swing.JDialog {

    private final Library Lib;
    private final MainFrame MF;
    private final Resources RSC;

    private DefaultListModel DLM;
    private StateManager SM;

    public String deviceName;

    /** Creates new form DialogEditLibrary */
    public ChooseUSBDevice(MainFrame mf, Library lib) {
        super(mf, true);
        MF=mf;
        RSC=mf.RSC;
        Lib=lib;
        initComponents();
        DLM=new DefaultListModel();
        for (String tag : Lib.usbdrives.keySet())
            DLM.addElement(tag);
        jLDevices.setModel(DLM);
        if (Lib.usbdrives.keySet().size()>0)
            jLDevices.setSelectedIndex(0);
        jBtnSelect.grabFocus();
        toolbox.centerDialog(this,mf);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLDevices = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jBtnSelect = new javax.swing.JButton();
        jBtnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Setup USB devices");
        getContentPane().setLayout(new java.awt.GridLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jScrollPane1.setViewportView(jLDevices);

        jLabel2.setText("USB Devices:");

        jBtnSelect.setText("Select Device");
        jBtnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelectActionPerformed(evt);
            }
        });

        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jBtnSelect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnCancel))
                    .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, RSC.guiScale(120), javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnSelect)
                    .addComponent(jBtnCancel)))
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelActionPerformed
        deviceName=null;
        this.setVisible(false);
        this.dispose();
}//GEN-LAST:event_jBtnCancelActionPerformed

    private void jBtnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelectActionPerformed
        deviceName=(String)jLDevices.getSelectedValue();
        this.setVisible(false);
        this.dispose();
}//GEN-LAST:event_jBtnSelectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnCancel;
    private javax.swing.JButton jBtnSelect;
    private javax.swing.JList jLDevices;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}

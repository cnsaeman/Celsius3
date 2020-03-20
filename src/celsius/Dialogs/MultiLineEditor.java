//
// Celsius Library System v2
// (w) by C. Saemann
//
// DialogMultiLineEditor.java
//
// This class contains a multiline editor dialog frame
//
// typesafe, completed
// 
// checked 15.09.2007
//

package celsius.Dialogs;

import celsius.Resources;
import celsius.tools.toolbox;

/**
 *
 * @author  cnsaeman
 */
public class MultiLineEditor extends javax.swing.JDialog {
    
    public String text;
    public boolean cancel;
    private Resources RSC;
    
    /** Creates new form DialogMultiLineEditor */
    public MultiLineEditor(Resources rsc, String title, String txt) {
        super(rsc.getMF(), true);
        RSC=rsc;
        setIconImage(rsc.getOriginalIcon("application_edit"));
        this.setTitle(title);
        initComponents();
        text=txt;
        jText.setText(text);
        jText.setCaretPosition(0);
        toolbox.centerDialog(this,rsc.getMF());
    }

    public void setLineWrapping(boolean b) {
        jText.setLineWrap(b);
        if (b) jText.setWrapStyleWord(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jText = new javax.swing.JTextArea();
        jBtnCancel = new javax.swing.JButton();
        jBtnApply = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jText.setColumns(20);
        jText.setRows(5);
        jScrollPane1.setViewportView(jText);

        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelActionPerformed(evt);
            }
        });

        jBtnApply.setText("Apply");
        jBtnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(548), Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jBtnApply)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(250), Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnCancel)
                    .addComponent(jBtnApply))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelActionPerformed
        cancel=true;
        setVisible(false);
    }//GEN-LAST:event_jBtnCancelActionPerformed

    private void jBtnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyActionPerformed
        text=jText.getText();
        cancel=false;
        setVisible(false);
    }//GEN-LAST:event_jBtnApplyActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnApply;
    private javax.swing.JButton jBtnCancel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jText;
    // End of variables declaration//GEN-END:variables
    
}

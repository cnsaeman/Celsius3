//
// Celsius Library System v2
// (w) by C. Saemann
//
// DialogSingleLineEditor.java
//
// This class contains a singleline editor dialog frame
//
// typesafe, completed
// 
// checked 15.09.2007
//

package celsius.Dialogs;

import celsius.Resources;
import celsius.tools.toolbox;
import java.awt.event.KeyEvent;

/**
 *
 * @author  cnsaeman
 */
public class SingleLineEditor extends javax.swing.JDialog {
    
    public String text;
    public String pre;
    public boolean content;
    public boolean cancel;
    private Resources RSC;
    
    /** Creates new form DialogMultiLineEditor */
    public SingleLineEditor(Resources rsc, String title, String txt, boolean c) {
        super(rsc.getMF(), true);
        RSC=rsc;
        setIconImage(rsc.getOriginalIcon("application_edit"));
        this.setTitle(title);
        initComponents();
        text=txt;
        pre=txt;
        content=c;
        jText.setText(text);
        if (!content) jText.setForeground(rsc.getLightGray());
        jText.setCaretPosition(0);
        toolbox.centerDialog(this,rsc.getMF());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jBtnCancel = new javax.swing.JButton();
        jBtnApply = new javax.swing.JButton();
        jText = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

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

        jText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jBtnApply)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnCancel))
                    .addComponent(jText, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(303), Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnCancel)
                    .addComponent(jBtnApply))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextKeyPressed
        if (!content) {
            content=true;
            jText.setForeground(java.awt.Color.BLACK);
            jText.setText("");
        }
        if (evt.getKeyCode()==KeyEvent.VK_ENTER)
            jBtnApplyActionPerformed(null);
        if (evt.getKeyCode()==KeyEvent.VK_ESCAPE)
            jBtnCancelActionPerformed(null);
    }//GEN-LAST:event_jTextKeyPressed

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
    private javax.swing.JTextField jText;
    // End of variables declaration//GEN-END:variables
    
}

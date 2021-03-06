//
// Celsius Library System v2
// (w) by C. Saemann
//
// DialogChooseCategory.java
//
// This class contains the dialog for choosing a category for preregistering
// when adding documents
//
// typesafe, completed
// 

package celsius.Dialogs;

import celsius.Resources;
import celsius.StructureNode;
import celsius.tools.toolbox;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class ChooseCategory extends javax.swing.JDialog {

    private final Resources RSC;
    
    public String category;
    public boolean selected;
    
    /** Creates new form DialogChooseCategory */
    public ChooseCategory(Resources rsc) {
        super(rsc.getMF(), true);
        RSC=rsc;
        setIconImage(rsc.getOriginalIcon("folder_table"));
        initComponents();
        jTreeCategories.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTreeCategories.setShowsRootHandles(true);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(RSC.Icons.getIcon("folder"));
        renderer.setClosedIcon(RSC.Icons.getIcon("folder"));
        renderer.setOpenIcon(RSC.Icons.getIcon("folder_table"));
        jTreeCategories.setCellRenderer(renderer);
        DefaultTreeModel CatTreeModel=new DefaultTreeModel(RSC.getCurrentSelectedLib().Structure.Root);
        jTreeCategories.setModel(CatTreeModel);
        selected=false;
        toolbox.centerDialog(this,rsc.getMF());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeCategories = new javax.swing.JTree();
        jTreeCategories.setFont(new java.awt.Font("Arial", 0, RSC.guiScale(11))); // NOI18N
        jBtnSel = new javax.swing.JButton();
        jBtnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose a category");

        jScrollPane1.setViewportView(jTreeCategories);

        jBtnSel.setText("Select");
        jBtnSel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelActionPerformed(evt);
            }
        });

        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, RSC.guiScale(296), Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jBtnSel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 137, Short.MAX_VALUE)
                        .addComponent(jBtnCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, RSC.guiScale(310), javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnSel)
                    .addComponent(jBtnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnSelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelActionPerformed
        selected=true;
        category=jTreeCategories.getLastSelectedPathComponent().toString();
        if (((StructureNode)jTreeCategories.getSelectionPath().getLastPathComponent()).isRoot()) return;
        this.setVisible(false);
    }//GEN-LAST:event_jBtnSelActionPerformed

    private void jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelActionPerformed
        selected=false;
        this.setVisible(false);
    }//GEN-LAST:event_jBtnCancelActionPerformed
     
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnCancel;
    private javax.swing.JButton jBtnSel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTreeCategories;
    // End of variables declaration//GEN-END:variables
    
}

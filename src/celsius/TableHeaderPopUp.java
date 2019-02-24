/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.table.TableColumn;

/**
 *
 * @author cnsaeman
 */
public class TableHeaderPopUp extends JPopupMenu implements ActionListener {

    private Library Lib;
    private int column;
    private TableColumn[] Columns;
    private ArrayList<Integer> sizes;
    
    private JMenuItem jMI;
    private JRadioButtonMenuItem jRBMI1,jRBMI2;

    private MainFrame MF;


    public TableHeaderPopUp(MainFrame mf,Library lib, int c, TableColumn[] cols, ArrayList<Integer> s) {
        super();
        MF=mf;
        Lib=lib; column=c; Columns=cols; sizes=s;
        jMI=new JMenuItem("Save width of all columns");
        add(jMI);
        jRBMI1=new JRadioButtonMenuItem("fixed width column");
        jRBMI2=new JRadioButtonMenuItem("variable width column");
        if (Lib.ColumnSizes.get(c)>0) {
            jRBMI1.setSelected(false);
            jRBMI2.setSelected(true);
        } else {
            jRBMI1.setSelected(true);
            jRBMI2.setSelected(false);
        }
        ButtonGroup BtnGrp=new ButtonGroup();
        BtnGrp.add(jRBMI1);
        BtnGrp.add(jRBMI2);
        add(jRBMI1);
        add(jRBMI2);
        jMI.addActionListener(this);
        jRBMI1.addActionListener(this);
        jRBMI2.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==jMI) {
            for (int i=0;i<Columns.length;i++) {
                int w=Columns[i].getWidth();
                if (sizes.get(i)<0) w=-w;
                Lib.setColumnSize(i, w);
                sizes.set(i, w);
            }
            MF.updateStatusBar(false);
        }
        if (e.getSource()==jRBMI1) {
            int w=Columns[column].getWidth();
            if (w>0) w=-w;
            Columns[column].setResizable(false);
            sizes.set(column, w);
            Lib.setColumnSize(column, w);
            MF.updateStatusBar(false);
        }
        if (e.getSource()==jRBMI2) {
            int w=Columns[column].getWidth();
            if (w<0) w=-w;
            Columns[column].setResizable(true);
            sizes.set(column, w);
            Lib.setColumnSize(column, w);
            MF.updateStatusBar(false);
        }
    }



}

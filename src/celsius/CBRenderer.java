/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.awt.Component;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author cnsaeman
 */
public class CBRenderer extends JLabel
                       implements ListCellRenderer {

    private final Icons icons;
    private final HashMap<String,String> dictionary;

    public CBRenderer(Icons i,HashMap<String,String> dict, Resources rsc) {
        icons=i;
        dictionary=dict;
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        this.setFont(new java.awt.Font("Arial", 0, rsc.guiScale(11)));
    }

    public Component getListCellRendererComponent(
                                       JList list,
                                       Object value,
                                       int index,
                                       boolean isSelected,
                                       boolean cellHasFocus) {
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)

        //this.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        //Set the icon and text.  If icon was null, say so.
        String svalue = (String)value;
        String ivalue=svalue;
        if (dictionary!=null)
            if (dictionary.containsKey(ivalue)) ivalue=dictionary.get(ivalue);
        setIcon(icons.get(ivalue));
        if (value != null) {
            setText(svalue);
            //setFont(list.getFont());
        } else {
            setText(svalue + " (no image available)");
            //setFont(list.getFont());
        }
        return this;
    }

}

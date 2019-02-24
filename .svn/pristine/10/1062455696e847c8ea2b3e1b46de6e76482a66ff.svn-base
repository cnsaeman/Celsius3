/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import javax.swing.JTextField;

/**
 *
 * @author cnsaeman
 */
public class jExtTextField extends JTextField implements Serializable, FocusListener {

    private final java.awt.Color lightgray=new java.awt.Color(204,204,204);
    private String defaultText;

    public jExtTextField() {
        super();
        addFocusListener(this);
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String value) {
        defaultText=value;
        resetText();
    }

    public void focusGained(FocusEvent e) {
        if (getText().equals(defaultText)) {
            setForeground(java.awt.Color.BLACK);
            super.setText("");
        }
    }

    public void focusLost(FocusEvent e) {
        if (getText().equals("")) {
            resetText();
        }
    }

    public void resetText() {
        setForeground(lightgray);
        super.setText(defaultText);
    }

    @Override
    public void setText(String s) {
        super.setText(s);
        setForeground(java.awt.Color.BLACK);
    }

}

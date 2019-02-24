/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComponent;

/**
 *
 * @author cnsaeman
 */
public class StateManager {

    private final ArrayList<Component> registered;
    private final ArrayList<HashMap<String,ArrayList<JComponent>>> lists;

    public StateManager() {
        registered=new ArrayList<Component>();
        lists=new ArrayList<HashMap<String,ArrayList<JComponent>>>();
    }

    public void register(Component where, String state, JComponent[] l) {
        if (registered.indexOf(where)==-1) {
            registered.add(where);
            lists.add(new HashMap<String,ArrayList<JComponent>>());
        }
        HashMap<String,ArrayList<JComponent>> lst=lists.get(registered.indexOf(where));
        if (!lst.containsKey(state)) {
            lst.put(state, new ArrayList<JComponent>());
        }
        ArrayList<JComponent> cmps=lst.get(state);
        for (int i=0;i<l.length;i++) {
            if (!cmps.contains(l[i])) cmps.add(l[i]);
        }
    }

    public void unregister(Component where) {
        int i=registered.indexOf(where);
        if (i>0) {
            registered.remove(i);
            lists.remove(i);
        }
    }

    public void switchOn(Component where, String state) {
        HashMap<String,ArrayList<JComponent>> lst=lists.get(registered.indexOf(where));
        for (JComponent cmp : lst.get(state)) {
            cmp.setEnabled(true);
        }
    }

    public void switchOff(Component where, String state) {
        HashMap<String,ArrayList<JComponent>> lst=lists.get(registered.indexOf(where));
        if (lst==null) return;
        for (JComponent cmp : lst.get(state)) {
            cmp.setEnabled(false);
        }
    }

}

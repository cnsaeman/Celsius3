/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import java.util.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author cnsaeman
 */
public class StructureNode implements MutableTreeNode {

    public ArrayList<StructureNode> Nodes;
    private StructureNode parent;

    private String label;
    private HashMap<String,String> data;

    public String representation;
    public int regid;

    public StructureNode() {
        Nodes=new ArrayList<StructureNode>();
        parent=null;

        label="";
        data=new HashMap<String,String>();

        representation="$full";
    }

    public StructureNode(String l) {
        Nodes=new ArrayList<StructureNode>();
        parent=null;

        label=l;
        data=new HashMap<String,String>();
        representation="$full";
    }

    public StructureNode(HashMap<String,String> d) {
        Nodes = new ArrayList<StructureNode>();
        parent = null;

        label = new String("");
        data = d;
        representation="$full";
    }

    public StructureNode(String l,HashMap<String,String> d) {
        Nodes = new ArrayList<StructureNode>();
        parent = null;

        label = l;
        data = d;
        representation="$full";
    }

    @Override
    public StructureNode getChildAt(int childIndex) {
        return(Nodes.get(childIndex));
    }

    @Override
    public int getChildCount() {
        return(Nodes.size());
    }

    @Override
    public StructureNode getParent() {
        return(parent);
    }

    @Override
    public int getIndex(TreeNode node) {
        return(Nodes.indexOf(node));
    }

    @Override
    public boolean getAllowsChildren() {
        return(true);
    }

    @Override
    public boolean isLeaf() {
        return(Nodes.isEmpty());
    }

    public boolean isRoot() {
        return(parent==null);
    }

    @Override
    public Enumeration children() {
        return(Collections.enumeration(Nodes));
    }

    public void add(StructureNode node) {
        if (node!=null) {
            node.parent=this;
            Nodes.add(node);
        }
    }

    public void insert(StructureNode node, int i) {
        if (node!=null) {
            node.parent=this;
            Nodes.add(i,node);
        }
    }

    public void remove(StructureNode node) {
        if (node!=null) {
            node.parent=null;
            Nodes.remove(node);
        }
    }

    @Override
    public String toString() {
        String tmp="";
        if (representation.equals("$full")) {
            tmp+=getLabel()+" ";
            for (String key : data.keySet()) {
                tmp+=key+"=\""+data.get(key)+"\" ";
            }
        } else {
            if (representation.indexOf("/name/")>-1)
                tmp+=getLabel()+" ";
            else
            for (String key : data.keySet()) {
                if (representation.indexOf("/"+key+"/")>-1) tmp+=data.get(key)+" ";
            }
        }
        return(tmp.trim());
    }

    public String getLabel() {
        return(label);
    }

    public void setLabel(String s) {
        label=s;
    }

    public void writeData(String key,String value) {
        data.put(key, value);
    }


    public String getData(String key) {
        return(data.get(key));
    }

    public Set<String> getDataKeys() {
        return(data.keySet());
    }

    public HashMap<String,String> getData() {
        return(data);
    }

    public void setData(HashMap<String,String> d) {
        data=d;
    }

    public StructureNode cloneCompletely() {
        StructureNode clone=new StructureNode();
        clone.representation=representation;
        clone.regid=regid;
        clone.label=label;
        for (String key : data.keySet()) {
            clone.writeData(key, data.get(key));
        }
        for (StructureNode subnode : Nodes) {
            clone.add(subnode.cloneCompletely());
        }
        return(clone);
    }

    public StructureNode getRoot() {
        if (parent==null) return(this);
        else return(parent.getRoot());
    }

    public ArrayList<StructureNode> getPath() {
        if (parent==null) {
            ArrayList<StructureNode> path=new ArrayList<StructureNode>();
            path.add(this);
            return(path);
        }
        ArrayList<StructureNode> path=parent.getPath();
        path.add(this);
        return(path);
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        if (child!=null) {
            child.setParent(this);
            Nodes.add(index,(StructureNode)child);
        }
    }

    @Override
    public void remove(int index) {
        StructureNode SN=getChildAt(index);
        remove(SN);
    }

    @Override
    public void remove(MutableTreeNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setUserObject(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFromParent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        parent=(StructureNode)newParent;
    }

}

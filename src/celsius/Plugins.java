/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import celsius.tools.MsgLogger;
import celsius.tools.Parser;
import celsius.tools.XMLHandler;
import celsius.tools.toolbox;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 *
 * @author cnsaeman
 */
public class Plugins extends HashMap<String,Plugin> {

    //Rewrite as arrays

    public final HashMap<String,String> parameters;
    private final MsgLogger Msg1;                             // protocol class
    private Resources RSC;

    public Plugins(MsgLogger msg1, Resources rsc) {
        super();
        RSC=rsc;
        parameters=new HashMap<String,String>();
        Msg1=msg1;
    }

    private void ErrorOut(String name, Exception ex) {
        (new SafeMessage("Problem loading plugin "+name+".\n" + ex.toString(), "Error: Plugin not loaded", 0)).showMsg();
        Msg1.repS("CPlugins>Problem loading plugin "+name+".\n" + ex.toString());
    }
    
    public void ReadIn() {
        Msg1.repS("CPlugins>Started reading in plugins");
        XMLHandler cfg;
        try {
            cfg = new XMLHandler("plugins/configuration.plugins.xml");
        } catch(Exception e) {
            Msg1.printStackTrace(e);
           (new SafeMessage("CPlugins>Error reading plugin file "+e.toString(), "Error: No plugins loaded", 0)).showMsg();
            Msg1.repS("CPlugins>Error reading plugin file "+e.toString());
            return;
        }
        this.clear();
        parameters.clear();
        String[] listOfFiles=new File("plugins").list();
        for (int i=0;i<listOfFiles.length;i++) {
            String fn=listOfFiles[i];
            if (fn.endsWith(".class")) {
                try {
                    Plugin pl=new Plugin(Parser.CutTill(fn,".class"));
                    put(pl.metaData.get("title"),pl);
                    cfg.toFirstElement();
                    while (!cfg.endReached) {
                        try {
                            if (cfg.get("name").equals(pl.className))
                                parameters.put(pl.metaData.get("title"), cfg.get("parameters"));
                        } catch (Exception ex) {
                            Msg1.printStackTrace(ex);
                            ErrorOut(cfg.get("name"), ex);
                        }
                        cfg.nextElement();
                    }
                } catch(Exception e) { }
                catch (NoClassDefFoundError e) {
                    Msg1.repS("Error loading Plugin "+fn);
                    Msg1.printStackTrace(e);
                }
            }
        }
        Msg1.repS("CPlugins>Done reading in plugins");
        
    }

    public ArrayList<Plugin> listPlugins(String type, Library Lib) {
        ArrayList<Plugin> ret= new ArrayList<Plugin>();
        String pluginsString=Lib.MainFile.get("plugins-"+type);
        if (pluginsString==null) return(ret);
        String[] plugins=pluginsString.split("\\|");
        for (int i=0;i<plugins.length;i++)
            if (this.containsKey(plugins[i]))
                ret.add(get(plugins[i]));
        return(ret);
    }

    public DefaultListModel getPluginsDLM() {
        DefaultListModel DLM = new DefaultListModel();
        ArrayList<String> pls=new ArrayList<String>();
        for (String title : keySet())
            pls.add(title);
        Collections.sort(pls);
        for (String title : pls)
            DLM.addElement(title);
        return(DLM);
    }

    /**
     *
     * @param type : which type of plugin
     * @param Lib : which Library
     * @param sorted : whether the output should be sorted alphabetically
     * @return
     */
    public DefaultListModel getPluginsDLM(String type, Library Lib) {
        DefaultListModel DLM = new DefaultListModel();
        if (Lib==null) return(DLM);
        String pluginsString=Lib.MainFile.get("plugins-"+type);
        if (pluginsString==null) return(DLM);
        String[] plugins=pluginsString.split("\\|");
        for (int i=0;i<plugins.length;i++)
            if (this.containsKey(plugins[i]))
                DLM.addElement(plugins[i]);
        return(DLM);
    }

    /**
     *
     * @param type : which type of plugin
     * @param Lib : which Library
     * @param sorted : whether the output should be sorted alphabetically
     * @return
     */
    public DefaultComboBoxModel getPluginsDCBM(String type, Library Lib) {
        DefaultComboBoxModel DCBM = new DefaultComboBoxModel();
        if (Lib==null) return(DCBM);
        String pluginsString=Lib.MainFile.get("plugins-"+type);
        if (pluginsString==null) return(DCBM);
        String[] plugins=pluginsString.split("\\|");
        for (int i=0;i<plugins.length;i++)
            if (this.containsKey(plugins[i]))
                DCBM.addElement(plugins[i]);
        return(DCBM);
    }

    public String getInfo(String name) {
        Plugin current = get(name);
        String params = parameters.get(name);
        return("Plugin: " + current.metaData.get("title") + "\nAuthor: " + current.metaData.get("author") + "\nVersion: " + current.metaData.get("version") +"\nParameters: " + params +"\n" + toolbox.wrap(current.metaData.get("help")));
    }

    public String getText(String name) {
        Plugin theplug = get(name);
        String tmp;
        if (theplug!=null) {
            tmp="<html><b>"+theplug.metaData.get("title")+"</b>, Version: "+theplug.metaData.get("version");
            tmp+="<br/>Author: "+theplug.metaData.get("author")+"\n";
            tmp+="<br/>Class name: "+theplug.className+"\n<br/>";
            tmp+=theplug.metaData.get("help")+"\n";
            if (theplug.metaData.containsKey("parameter-help") && (!theplug.metaData.get("parameter-help").startsWith("none"))) {
                tmp+="<br/>Parameters: "+theplug.metaData.get("parameter-help")+"\n";
                tmp+="<br/>Default parameters: "+theplug.metaData.get("defaultParameters")+"\n";
            } else {
                tmp+="<br/>This plugin does not require any parameters.\n";
            }
            tmp+="<br/>Types: "+theplug.metaData.get("type")+"\n";
            tmp+="<br/>Required fields: "+theplug.metaData.get("requiredFields")+"\n";
            tmp+="<br/>Needs plain text of first page: "+theplug.metaData.get("needsFirstPage")+"\n";
            tmp+="<br/>Would like plain text of first page: "+theplug.metaData.get("wouldLikeFirstPage")+"\n";
            if (theplug.metaData.containsKey("longRunTime"))
                tmp+="<br/>Longer runtime: "+theplug.metaData.get("longRunTime");
        } else {
            tmp="<br/>not found!";
        }
        return(tmp+"</html>");
    }

    public void setParams(String name, String p) {
        parameters.put(name,p);
    }

    public String getParams(String name) {
        return(parameters.get(name));
    }

    /**
     * This procedure tries to add the plugin with name s and type type and returns true if successful.
     */
    public boolean add(Plugin p) throws Exception {
        boolean added=false;
        boolean exists=false;
        String title=p.metaData.get("title");
        if (containsKey(title))
            exists=true;
        if (!exists) {
            put(title,p);
            parameters.put(title, "");
            added=true;
            save();
        }
        return(added);
    }

    /**
     * This procedure tries to add the plugin with name s and type type and returns true if successful.
     */
    public void remove(String name) {
        remove(name);
        parameters.remove(name);
        try {
            save();
        } catch (Exception e) {
            Msg1.printStackTrace(e);
        }
    }

    public void save() throws IOException {
        XMLHandler out=new XMLHandler("plugins/configuration.plugins.xml");
        out.clear();
        for (String t : keySet()) {
            out.addEmptyElement();
            out.put("name", get(t).className);
            String p=parameters.get(t);
            if ((p!=null) && (p.trim().length()>0))
                out.put("parameters", p);
        }
        out.writeBack();
    }

    public void updatePlugins() {
        MainFrame MF=RSC.getMF();
        MF.jCBExpFilter.setModel(getPluginsDCBM("export",RSC.getCurrentSelectedLib()));
        MF.jLPlugins.setModel(getPluginsDLM("manual",RSC.getCurrentSelectedLib()));
        MF.jIP.jCBBibPlugins.setModel(getPluginsDCBM("export",RSC.getCurrentSelectedLib()));
        if (MF.jCBExpFilter.getItemCount()>0)
            MF.jCBExpFilter.setSelectedIndex(0);
    }
    
}

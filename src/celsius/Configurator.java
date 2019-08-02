/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius;

import celsius.tools.Parser;
import celsius.tools.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 *
 * @author cnsaeman
 */
public final class Configurator {

    private XMLHandler ConfigXML;
    private final MsgLogger Msg1;
    public String maxthreads1;
    public String maxthreads2;

    public Configurator(MsgLogger msg1) throws IOException {
        Msg1=msg1;
        ConfigXML = new XMLHandler("configuration.xml");
        ConfigXML.toFirstElement();
        if (ConfigXML.get("maxthreads1")==null) {
            ConfigXML.put("maxthreads1", "6");
        }
        if (ConfigXML.get("maxthreads2")==null) {
            ConfigXML.put("maxthreads2", "6");
        }
        maxthreads1=ConfigXML.get("maxthreads1");
        maxthreads2=ConfigXML.get("maxthreads2");
        if (getDefault("iconfolder")==null) {
            ConfigXML.put("iconfolder","Icons");
        }
    }

    public DefaultListModel getTypeDLM() {
        DefaultListModel DLM=new DefaultListModel();
        ConfigXML.toFirstElement();
        ConfigXML.nextElement();
        while (!ConfigXML.endReached) {
            DLM.addElement(ConfigXML.get("filetype"));
            ConfigXML.nextElement();
        }
        return(DLM);
    }

    public String getDefault(String key) {
        ConfigXML.goToFirst("filetype","..none..");
        return(ConfigXML.get(key));
    }

    public void writeDefault(String key, String value) {
        if (!ConfigXML.goToFirst("filetype", "..none..")) {
            ConfigXML.addEmptyElement("fileytype", "..none..");
        }
        ConfigXML.put(key,value);
    }

    public void writeDefault(String email, String annotate, String proxyadd, String proxyport, boolean proxy) {
        if (!ConfigXML.goToFirst("filetype", "..none..")) {
            ConfigXML.addEmptyElement("fileytype", "..none..");
        }
        ConfigXML.put("email",email);
        ConfigXML.put("annotate",annotate);
        ConfigXML.put("proxy-address",proxyadd);
        ConfigXML.put("proxy-port",proxyport);
        if (proxy) {
            ConfigXML.put("proxy","true");
            System.setProperty( "proxySet", "true" );
            String prox=ConfigXML.get("proxy-address");
            if (prox.startsWith("http://")) prox=prox.substring(7);
            //System.out.println(prox);
            System.setProperty( "proxyHost", prox);
            System.setProperty( "proxyPort", ConfigXML.get("proxy-port"));
        } else {
            ConfigXML.put("proxy","false");
            System.setProperty( "proxySet", "false" );
        }
    }

    public String getType(String type,String key) {
        ConfigXML.goToFirst("filetype", type);
        return(ConfigXML.get(key));
    }

    public void writeFileType(String type, String extractor, String viewer, String ident, String secondary) {
        if (!ConfigXML.goToFirst("filetype", type)) {
            ConfigXML.addEmptyElement("fileytype", type);
        }
        ConfigXML.put("Extractor",extractor);
        ConfigXML.put("Viewer",viewer);
        ConfigXML.put("Ident",ident);
        ConfigXML.put("Secondary",secondary);
    }

    public void addType(String type) {
        ConfigXML.addEmptyElement("filetype",type);
    }

    public void removeType(String type) {
        ConfigXML.goToFirst("filetype", type);
        ConfigXML.deleteCurrentElement();
    }

    public void writeBack() {
        try {
            ConfigXML.writeBack();
        } catch (IOException ex) {
            Msg1.printStackTrace(ex);
        }
    }

    public void setProxy(MsgLogger Msg1) {
        if (getDefault("proxy").equals("true")) {
            System.setProperty("proxySet", "true");
            String prox = getDefault("proxy-address");
            if (prox.startsWith("http://")) {
                prox = prox.substring(7);
            }
            Msg1.repS("MAIN>Setting proxy to " + prox + ":" + getDefault("proxy-port"));
            System.setProperty("proxyHost", prox);
            System.setProperty("proxyPort", getDefault("proxy-port"));
        }
    }

    public void addTypesCBM(DefaultComboBoxModel DCBM) {
        ConfigXML.toFirstElement();
        while (!ConfigXML.endReached) {
            if ((DCBM.getIndexOf(ConfigXML.get("filetype")) == -1) &&
                (!ConfigXML.get("filetype").equals("..none.."))) {
                DCBM.addElement(ConfigXML.get("filetype"));
            }
            ConfigXML.nextElement();
        }
    }

    /**
     * Extract plain text (protocol, PreMsg, Filename source, FileName target, Configuration)
     */
    public void ExtractText(String TI, String s,String t) throws IOException {
        String filetype;
        String stmp;    // tatsächlicher Dateiname (evtl. ohne ".gz")
        // if gzipped, extract the file first
        if (s.toLowerCase().endsWith(".gz")) {
            TextFile f1=new TextFile(s);
            String tmp=f1.getString();
            f1.close();
            if (tmp.startsWith("\u001f")) {
                TextFile.GUnZip(s);
                stmp=Parser.CutLastChars(s,3);
                Msg1.repS(TI+"extracting...");
            } else {
                stmp=Parser.CutLastChars(s,3);
                (new File(s)).renameTo(new File(stmp));
                Msg1.repS(TI+"no extraction necessary, wrong ending, corrected");
            }
        } else { stmp=s; }
        // geeigneten Extractor finden
        String extractorstr;
        try {
            synchronized(ConfigXML) {
                ConfigXML.toFirstElement();
                while (!ConfigXML.endReached && (!s.toLowerCase().endsWith(ConfigXML.get("filetype"))))
                    ConfigXML.nextElement();
                if ((ConfigXML.endReached) || (ConfigXML.get("Extractor")==null)) {
                    Msg1.repS(TI+"...No configuration entry for "+s+".");
                    return;
                }
                Msg1.repS(TI+"...Extracting Text for "+s+".");
                // Teste Filetype, versuche dennoch zu extrahieren
                TextFile H=new TextFile(stmp);
                String tmp=H.getString();
                H.close();
                if ((ConfigXML.get("Ident")!=null) && (ConfigXML.get("Ident").length()==0)) {
                    if (!tmp.startsWith(ConfigXML.get("Ident"))) {
                        // Filetyp scheint nicht zu stimmen
                        toolbox.Warning(null,"The file "+s+" is not of type "+ConfigXML.get("filetype")+".","Incorrect File Type Assignment");
                        Msg1.repS(TI+"Identification of filetype failed.");
                        Msg1.repS(TI+ConfigXML.get("Ident")+"  :: vs ::  "+tmp.substring(0,10));
                    }
                }
                extractorstr=ConfigXML.get("Extractor");
            }
            if ((extractorstr==null) || (extractorstr.length()==0)) return;
            // Cut Spaces
            extractorstr=extractorstr.replace("%from%",stmp);
            extractorstr=extractorstr.replace("%to%",t);
            Msg1.repS(TI+"Extractor command: "+extractorstr);
            // Prozess starten und auf Ende warten
            ExecutionShell ES=new ExecutionShell(extractorstr,0,false);
            ES.start();
            ES.join(5000);
            if (ES.errorflag) Msg1.repS(TI+"Error Message: "+ES.errorMsg);
            // Textdatei und ggfs Originaldatei gzippen
            if ((new File(t)).exists()) TextFile.GZip(t);
        } catch (Exception e) { Msg1.printStackTrace(e);Msg1.repS(TI+"Error extracting text: "+e.toString()); }
        if (s.toLowerCase().endsWith(".ps.gz"))
            TextFile.GZip(stmp);
    }

    public void view(Item item,String nmb) {
        if (nmb==null) {
            view(item.get("filetype"),item.getCompleteDirS("location"));
        } else {
            view(item.get("altversion-filetype-"+nmb),item.getCompleteDirS("altversion-location-"+nmb));
        }
    }

    public void view(String type, String location) {
        if (type=="---") {
            try {
                if (type.equals("html"))
                    java.awt.Desktop.getDesktop().browse(new URI(location));
                    else java.awt.Desktop.getDesktop().open(new File(location));
            } catch (Exception ex) {
                toolbox.Warning(null,"Standard viewer for " + type + " reports an error!", "Warning");
                Msg1.printStackTrace(ex);
            }
            return;
        } else {
            boolean res=ConfigXML.goToFirst("filetype", type);
            if (!res) {
                toolbox.Warning(null,"No viewer for " + type + " installed!", "Warning");
                return;
            }
            if (ConfigXML.get("Viewer").equals("use standard viewer")) {
                try {
                    if (type.equals("html"))
                        java.awt.Desktop.getDesktop().browse(new URI(location));
                        else java.awt.Desktop.getDesktop().open(new File(location));
                } catch (Exception ex) {
                    toolbox.Warning(null,"Standard viewer for " + type + " reports an error!", "Warning");
                    Msg1.printStackTrace(ex);
                }
                return;
            }
            if (ConfigXML.get("Viewer")!=null) {
                String cmdln = ConfigXML.get("Viewer") + " ";
                if (cmdln.indexOf("'%from%'")>-1) location=Parser.Substitute(location, "'", "\\'");
                cmdln = cmdln.replace("%from%", location);
                Msg1.repS("MAIN>Viewer command: " + cmdln);
                (new ExecutionShell(cmdln, 0, true)).start();
            } else {
                toolbox.Warning(null,"No viewer for " + type + " installed!", "Warning");
            }
        }
    }

    public String SecondaryViewers(String ft) {
        ConfigXML.goToFirst("filetype", ft);
        if (ConfigXML.get("filetype").equals(ft)) {
            return(ConfigXML.get("Secondary"));
        }
        return(null);
    }

    /**
     * Show an external HTML page
     */
    public void viewHTML(String url) {
        view("html",url);
    }

    public ArrayList<String> supportedFileTypes() {
        ConfigXML.toFirstElement();
        ArrayList<String> filetypes=new ArrayList<String>();
        while (!ConfigXML.endReached) {
            filetypes.add(ConfigXML.get("filetype"));
            ConfigXML.nextElement();
        }
        return(filetypes);
    }

    /**
     * returns the actual filetype of a file according to the config file
     * @param path - path to the file
     * @return
     */
    public String getFileType(String path) throws IOException {
        String filetype=TextFile.getFileType(path);
        File f = new File(path);
        byte[] buffer = new byte[10]; 
        InputStream in = new FileInputStream(f); 
        in.read(buffer); 
        in.close();
        String tmp = new String(buffer);
        if (tmp!=null) {
            ConfigXML.toFirstElement();
            while (!ConfigXML.endReached) {
                if (!ConfigXML.get("filetype").equals("..none..")) {
                    if ((ConfigXML.get("Ident")!=null) && (ConfigXML.get("Ident").length()!=0) && (tmp.startsWith(ConfigXML.get("Ident")))) filetype=ConfigXML.get("filetype");
                }
                ConfigXML.nextElement();
            }
        }
        return(filetype);
    }

    public String correctFileType(String fn) throws IOException {
        String tmp=getFileType(fn);
        if (!fn.endsWith("."+tmp)) {
            String newfile=fn+"."+tmp;
            Msg1.repS("CONF>Renaming "+fn+" to "+newfile);
            (new File(fn)).renameTo(new File(newfile));
            fn=newfile;
        }
        return(fn);
    }
}

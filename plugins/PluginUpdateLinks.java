/*
 * PluginSpires.java
 *
 * v1.1
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;


/**
 * @author cnsaeman
 */
public class PluginUpdateLinks extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Update Links");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin updates all links from  Inspire.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"arxiv-ref");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"http://inspirehep.net/");
            put("parameter-help"    ,"Link to the Inspires mirror to be used.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String spiresbase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    public void run() {
        String inspirebase=Information.get("$$params");
        if (inspirebase==null) inspirebase="http://inspirehep.net/";
        
        if (!Information.isEmpty("inspirekey")) {
            String lnk=Information.get("inspirekey");
            Information.put("links",webToolsHEP.linksFromInspire(inspirebase,lnk));
            Information.put("references",webToolsHEP.arXivRefFromInspire(inspirebase,lnk));
        }
    }
  

}

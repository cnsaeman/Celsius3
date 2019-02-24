/*
 * PluginHeader.java
 *
 * Created on 17. October 2009, 16:50
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import celsius.tools.ExecutionShell;



/**
 * @author cnsaeman
 */
public class PluginTitleFromBibTeX extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Set Title As In BibTeX");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin sets the title of an item to the title in the bibtex record");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"bibtex");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI="P:Con1>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public void run() {
        String bib = Information.get("bibtex");
        if (bib != null) {
            celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(Information.get("bibtex"));
            if (BTR!=null) {
                String bibtitle=BTR.get("title");
                if (bibtitle.startsWith("{")) {
                    bibtitle = bibtitle.substring(1, bibtitle.length() - 1);
                }
                Information.put("title",bibtitle);
            }
        }
    }
  
                      
}

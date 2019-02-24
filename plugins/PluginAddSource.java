/*
 * PluginUpdateComplete.java
 *
 * Created on 05. September 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.io.File;
import java.io.FilenameFilter;


/**
 * @author cnsaeman
 */
public class PluginAddSource extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Add Source Folder");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin adjusts the source entry.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"bibtex");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String inspirebase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public void run() {
        celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(Information.get("bibtex"));
        String eprint=BTR.get("eprint");
        if (!Character.isDigit(eprint.charAt(0))) {
          eprint=Parser.CutFrom(eprint,"/");
        }
        String base="/home/cnsaeman/Projects - finished/arXiv/";
        File folder = new File(base);
        String[] directories = folder.list(new FilenameFilter() {
          @Override
          public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
          }
        });
        for (int i = 0; i < directories.length; i++) {
          if (directories[i].indexOf(eprint)>-1) {
            Information.put("source", base+"/"+directories[i]);
          }
        }
    }
}

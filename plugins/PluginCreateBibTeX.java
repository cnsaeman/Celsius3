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
public class PluginCreateBibTeX extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Create BibTeX");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin automatically creates rudimentary BibTeX entries for preprints that are not found on inSPIRE.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String spiresbase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    public void run() {
      String tag=Parser.CutTill(Information.get("authors"),",")+":xxxx";
      String s;
      if (Information.get("type").equals("Book")) {
        s="@book{"+tag+",\n";
        s+="author =\""+toolbox.ToBibTeXAuthors(Information.get("authors"))+"\",\n";
        s+="title =\""+Information.get("title")+"\",";
        s+="publisher=\"...\",";
        s+="location=\"...\",";
        s+="year=\"...\",";
        s+="}";
      } else {
        tag=Parser.CutTill(Information.get("authors"),",")+":"+Information.get("arxiv-number");
        s="@article{"+tag+",\n";
        s+="author =\""+toolbox.ToBibTeXAuthors(Information.get("authors"))+"\",\n";
        s+="title =\""+Information.get("title")+"\",";
        s+="eprint =\""+Information.get("arxiv-ref")+"\"";
        s+="   archiveprefix = \"arXiv\"";
        if (Information.get("arxiv-ref").length()==9) {
            s+=",\n primaryclass=\""+Information.get("arxiv-name")+"\"\n";
        }
        s+="}";
      }
      celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(s);
      Information.put("bibtex", BTR.toString());
      Information.put("citation-tag", tag);
    }
  

}

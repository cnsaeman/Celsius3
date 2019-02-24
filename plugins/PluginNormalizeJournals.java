import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.*;

public class PluginNormalizeJournals extends Thread {
 
    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Normalize Journals");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.01");
            put("help"              ,"This plugin substitutes long journal names in BibTeX by their abbreviation."
                    + "\n A list of journal names is found in the configuration file \"journal substitutions.txt\".");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"bibtex");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
    
    public static String normalizeJournal(String j) {
        j=j.replaceAll("\\.",". ");
        j=j.replaceAll("\\.  ",". ");
        return(j);
    }    

    public void run() {
	if (!Information.isEmpty("bibtex")) {
            try {
                String bib=Information.get("bibtex");
                celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(bib);
                if (!BTR.isEmpty("journal")) {
                    String journal=normalizeJournal(BTR.get("journal"));
                    TextFile in = new TextFile("plugins/journal substitutions.txt");
                    int i=0;
                    while (in.ready()) {
                        String sub=in.getString();
                        i++;
                        if (sub.indexOf("<=")>-1) {
                            String jlong=Parser.CutFrom(sub,"<=").trim();
                            String jshort=Parser.CutTill(sub,"<=").trim();
                            if (journal.indexOf(jlong)>-1) {
                                System.out.println("Found: "+jlong);
                                journal=Parser.Substitute(journal,jlong,jshort);
                            }
                        }
                    }
                    if (!journal.equals(BTR.get("journal"))) {
                        BTR.put("journal",journal);
                        Information.put("bibtex", BTR.toString());
                        String ident = toolbox.Identifier(Information);
                        if ((ident == null) || (ident.length() < 3)) {
                            Information.remove("identifier");
                        } else {
                            Information.put("identifier", ident.trim());
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }
  
                      
}

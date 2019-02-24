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


/**
 * @author cnsaeman
 */
public class PluginGetDOI extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"GetDOI");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin updates records from arXiv and inSpire. "+
                                     "The parameter is the Inspire base URL, e.g. http://inspirehep.net/.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"manual");
            put("defaultParameters" ,"http://inspirehep.net/");
            put("parameter-help"    ,"Link to the Inspires mirror to be used.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String inspirebase;

    // Spaceless comparison
    private boolean comp(String a, String b) //throws IOException
    {
        if ((a == null) && (b == null)) {
            return (true);
        }
        if (a == null) {
            return (false);
        }
        if (b == null) {
            return (false);
        }
        a = a.replaceAll(" ", "").trim().toLowerCase();
        b = b.replaceAll(" ", "").trim().toLowerCase();
        return (a.equals(b));
    }

    /**
     * get citation tag from a bibtex string
     */
    public String getCitationFromBibTeX(String in) {
        in = Parser.CutFrom(Parser.CutTill(in, ","), "{");
        return (in);
    }

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public void run() {
        inspirebase=Information.get("$$params");
        if (inspirebase==null) inspirebase="http://inspirehep.net/";
        String key = Information.get("inspirekey");
        celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(Information.get("bibtex"));
        if (!BTR.containsKey("doi")) {
          if ((key!=null) && (Information.get("bibtex").indexOf("doi") == -1)) {
            String bt=webToolsHEP.bibTeXFromInspire(inspirebase,key);
            System.out.println("From inSpire:");
            celsius.BibTeXRecord BTRInsp=new celsius.BibTeXRecord(bt);
            BTR.put("doi",BTRInsp.get("doi"));
            System.out.println(BTRInsp.toString());
            Information.put("bibtex", BTR.toString());
          }
          if ((!BTR.containsKey("doi")) && (Information.containsKey("arxiv-ref"))) {
              System.out.println("From ArXiv:");
              String oai = oai = TextFile.ReadOutURL("http://export.arxiv.org/oai2?verb=GetRecord&identifier=oai:arXiv.org:" + Information.get("arxiv-ref") + "&metadataPrefix=arXiv");
              String doi = Parser.CutTill(Parser.CutFrom(oai, "<doi>"), "</doi>");
              System.out.println(doi);
              if (doi.length() > 3) {
                  BTR.put("doi", doi);
                  Information.put("bibtex", BTR.toString());
              }
          }
        }
    }
}

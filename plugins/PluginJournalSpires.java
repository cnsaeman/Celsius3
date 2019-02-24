/*
 * PluginArXiv.java
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
public class PluginJournalSpires extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get journal data from Spires");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin looks for an entry at SLAC/Spires for records with a certain journal reference. " +
                                     "The parameter is the Spires base URL, e.g. http://www.slac.stanford.edu/spires/.");
            put("needsFirstPage"    ,"yes");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"http://www.slac.stanford.edu/spires/");
            put("parameter-help"    ,"Link to the Spires mirror to be used.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    private String spiresbase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public String normalize(String s) {
        if (s.startsWith("{")) s=s.substring(1);
        if (s.endsWith("}")) s=Parser.CutTillLast(s,"}");
        return(s);
    }

    public void run() {
        spiresbase = Information.get("");
        if (spiresbase == null) {
            spiresbase = "http://www.slac.stanford.edu/spires/";
        }
        String namepart = Parser.CutFromLast(Information.get("fullpath"), arxivTools.filesep);
        String fp = Information.get("firstpage");
        while (fp.indexOf("  ") > -1) {
            fp = fp.replace("  ", " ");
        }
        String link = "";
        int i = fp.indexOf("Commun. Math. Phys.");
        if ((i < 130) && (i > -1)) {
            String ref = Parser.CutTill(Parser.CutFrom(fp, "Commun. Math. Phys."), "\n");
            String vol = Parser.CutTill(ref, ",").trim();
            String page = Parser.CutTill(Parser.CutFrom(ref, ","), "-").trim();
            Msgs.add("CMP document found. Vol/Pages: " + vol + "/" + page + ".");
            link = spiresbase + "/find/hep/www?rawcmd=FIND+J+CMPHA%2C+" + vol + "%2C+" + page;
        }
        i = fp.indexOf("Commun Math Phys");
        if ((i < 130) && (i > -1)) {
            String ref = Parser.CutTill(Parser.CutFrom(fp, "Commun Math Phys"), "\n");
            String vol = Parser.CutTill(ref, ",").trim();
            String page = Parser.CutTill(Parser.CutFrom(ref, ","), "-").trim();
            Msgs.add("CMP document found. Vol/Pages: " + vol + "/" + page + ".");
            link = spiresbase + "/find/hep/www?rawcmd=FIND+J+CMPHA%2C+" + vol + "%2C+" + page;
        }
        if (namepart.toLowerCase().startsWith("jmathphys")) {
            String vol = Parser.CutFrom(namepart, "JMathPhys_");
            String page = Parser.CutTillLast(Parser.CutFrom(vol, "_"), ".");
            vol = Parser.CutTill(vol, "_");
            Msgs.add("JMathPhys document found. Vol/Pages: " + vol + "/" + page + ".");
            link = spiresbase + "/find/hep/www?rawcmd=FIND+J+JMAPA%2C+" + vol + "%2C+" + page;
        }
        i = fp.indexOf("Class. Quantum Grav.");
        if ((i < 130) && (i > -1)) {
            String ref = Parser.CutTill(Parser.CutFrom(fp, "Class. Quantum Grav."), "\n");
            String vol = Parser.CutTill(ref, "(").trim();
            String page = Parser.CutTill(Parser.CutFrom(ref, ")"), "-").trim();
            Msgs.add("CQG document found. Vol/Pages: " + vol + "/" + page + ".");
            link = spiresbase + "/find/hep/www?rawcmd=FIND+J+CQGRD%2C+" + vol + "%2C+" + page;
        }
        i = fp.indexOf("Adv. Theor. Math. Phys.");
        if ((i < 130) && (i > -1)) {
            String ref = Parser.CutTill(Parser.CutFrom(fp, "Adv. Theor. Math. Phys."), "\n");
            String vol = Parser.CutTill(ref, "(").trim();
            String page = Parser.CutTill(Parser.CutFrom(ref, ")"), "-").trim();
            Msgs.add("ATMP document found. Vol/Pages: " + vol + "/" + page + ".");
            link = spiresbase + "/find/hep/www?rawcmd=FIND+J+ATMP%2C+" + vol + "%2C+" + page;
        }
        if (!link.equals("")) {
            try {
                // Obtain Data from SPIRES
                String key = arxivTools.GetKeyFromInternet(link, Msgs);
                Information.put("spires-key", key);
                Information.put("bibtex", arxivTools.GetRefFromInternet(key, spiresbase, Msgs));
                String tmp = Information.get("bibtex");
                if (tmp!=null) {
                    Information.put("type","Paper");
                    celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(Information.get("bibtex"));
                    if (BTR.parseError == 0) {
                        if (Information.get("title").equals("<unknown>")) {
                            Information.put("title", normalize(BTR.get("title")));
                            Information.put("authors", celsius.BibTeXRecord.authorsBibTeX2Cel(BTR.get("author")));
                        }
                        Information.put("citation-tag", BTR.tag);
                    }
                }
                if ((!Information.get("title").equals("<unknown>")) && (!Information.get("authors").equals("<unknown>")) && (Information.get("bibtex")!=null) && (Information.get("spires-key")!=null)) {
                    Information.put("recognition", "100");
                }
		Information.put("identifier", toolbox.Identifier(Information));
            } catch (Exception e) {
		e.printStackTrace();
                Msgs.add(e.toString());
            }
        }
    }
}

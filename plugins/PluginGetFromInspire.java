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
public class PluginGetFromInspire extends Thread {

    public static final HashMap<String, String> metaData = new HashMap<String, String>() {
        {
            put("title", "Get from Inspire");
            put("author", "Christian Saemann");
            put("version", "1.2");
            put("help", "This plugin gets information for arXiv preprints from  Inspire."
                    + "The parameter is the Inspire base URL, e.g. http://inspirehep.net/.");
            put("needsFirstPage", "no");
            put("longRunTime", "no");
            put("requiredFields", "");
            put("type", "auto|manual");
            put("defaultParameters", "http://inspirehep.net/");
            put("parameter-help", "Link to the Inspires mirror to be used.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String spiresbase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public void run() {
        System.out.println("GETFROMINSPIRE");
        String inspirebase = Information.get("$$params");
        if (inspirebase == null) {
            inspirebase = "http://inspirehep.net/";
        }

        String lnk, tmp, abs;
        if (Information.isEmpty("arxiv-ref")) {
            lnk = Information.get("inspirekey");
        } else {
            String no = Information.get("arxiv-ref");
            if (no.charAt(4) == '.') {
                no = Parser.Substitute(no, ".", "+");
            }
            String srchstring = new String(inspirebase + "search?ln=en&p=find+eprint+" + no);
            tmp = TextFile.ReadOutURL(srchstring);
            lnk = Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp, "class = \"titlelink\""), "record/"), "\"");
        }
        System.out.println("LINK::" + lnk);
        if ((lnk != null) && (lnk.length() > 2)) {
            Information.put("inspirekey", lnk);
            tmp = webToolsHEP.getInspireRecord(inspirebase, lnk);

            //Keywords
            String keywords = webToolsHEP.keywordsFromInspire(tmp);
            boolean changed = false;
            String infokeywords = Information.getS("keywords");
            while (keywords.length() > 0) {
                String keyword = Parser.CutFrom(keywords, "\">");
                keyword = Parser.CutTill(keyword, "</a>");
                if (!Parser.EnumContains(Information.getS("keywords"), keywords)) {
                    infokeywords += "|" + keyword;
                    if (infokeywords.charAt(0) == '|') {
                        infokeywords = infokeywords.substring(1);
                    }
                    changed = true;
                }
                keywords = Parser.CutFrom(keywords, "</small>");
            }
            if (changed) {
                Information.put("keywords", infokeywords);
            }

            //Abstract
            if (Information.isEmpty("abstract")) {
                abs = webToolsHEP.abstractFromInspire(tmp);
                if (abs != null) {
                    Information.put("abstract", abs);
                }
            }

            //BibTeX
            String bt = webToolsHEP.bibTeXFromInspire(inspirebase, lnk);
            bt = "@" + Parser.CutFrom(bt, "@");
            celsius.BibTeXRecord BTRInsp = new celsius.BibTeXRecord(bt);
            System.out.println("BibTeX accepted");

            if (Information.isEmpty("bibtex") || (Math.abs(bt.length() - Information.get("bibtex").length()) < 20)) {
                Information.put("bibtex", bt);
            }

            if (!Information.isEmpty("bibtex")) {
                celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(Information.getS("bibtex"));

                boolean BTRmod = false;

                // overwrite doi results if empty
                if (Parser.Blank(BTR.get("journal")) && !Parser.Blank(BTRInsp.get("journal"))) {
                    BTR.put("journal", BTRInsp.get("journal"));
                    BTR.put("year", BTRInsp.get("year"));
                    BTRmod = true;
                }

                String[] copies = {"pages", "pages", "volume", "doi", "slaccitation"};
                for (String s : copies) {
                    if (Parser.Blank(BTR.get(s)) && !Parser.Blank(BTRInsp.get(s))) {
                        BTR.put(s, BTRInsp.get(s));
                        BTRmod = true;
                    }
                }

                if ((Parser.Blank(BTR.get("primaryclass")) || (BTR.get("primaryclass").equals("unknown"))) && !Parser.Blank(BTRInsp.get("primaryclass"))) {
                    BTR.put("primaryclass", BTRInsp.get("primaryclass"));
                    BTRmod = true;
                }

                // Paper type
                if (!Parser.Blank(BTR.get("journal"))) {
                    Information.put("type", "Paper");
                } else {
                    Information.put("type", "Preprint");
                }

                // Inspire citation tag over arxiv:
                if (!BTR.tag.equals(BTRInsp.tag)) {
                    BTR.tag = BTRInsp.tag;
                    Information.put("bibtex", BTR.toString());
                }

                // Citation tag
                if (Information.getS("citation-tag").equals("") || !Information.getS("citation-tag").equals(BTR.tag)) {
                    Information.put("citation-tag", BTR.tag);
                }

                //doi, but not in BibTeX
                String doi = null;
                if ((BTR.getS("doi").equals("")) && (tmp.indexOf("http://dx.doi.org/") > -1)) {
                    doi = Parser.CutFrom(tmp, "http://dx.doi.org/");
                    doi = Parser.CutTill(doi, "\">");
                    BTR.put("doi", doi);
                    BTRmod = true;
                }

                // has BTR been modified?
                if (BTRmod) {
                    Information.put("bibtex", BTR.toString());
                }

                if (!Information.isEmpty("bibtex")) {
                    if (Information.isEmpty("title")) {
                        Information.put("title", BTR.get("title"));
                    }
                    if (Information.get("title").startsWith("{")) {
                        Information.put("title", Parser.CutTillLast(Information.get("title").substring(1), "}"));
                    }
                    if (Information.isEmpty("authors")) {
                        Information.put("authors", celsius.BibTeXRecord.authorsBibTeX2Cel(BTR.get("author")));
                    }
                }
            }

            //Identifier
            if (!Information.getS("identifier").equals(toolbox.Identifier(Information))) {
                Information.put("identifier", toolbox.Identifier(Information));
            }

            // adjust recognition level
            if (!Information.isEmpty("title") && !Information.isEmpty("authors") && !Information.isEmpty("bibtex")) {
                if ((!Information.get("title").equals("<unknown>")) && (!Information.get("authors").equals("<unknown>")) && (Information.get("bibtex").length() > 2) && (Information.getS("inspirekey").length() > 2)) {
                    Information.put("recognition", "100");
                }
            }

            //References
            if (Information.isEmpty("links")) {
                Information.put("links", webToolsHEP.linksFromInspire(inspirebase, lnk));
            }
        }

    }

}

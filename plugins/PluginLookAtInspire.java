/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;


/**
 * @author cnsaeman
 */
public class PluginLookAtInspire extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look at Inspire");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin looks for a record at Inspire. The search string is the string entered as \"title\".");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"title");
            put("type"              ,"interactive");
            put("defaultParameters" ,"http://inspirehep.net/");
            put("parameter-help"    ,"Link to the Inspire mirror to be used.");
        }
    };

    private final String TI = "P:LaS>";
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    private String srchstring,tmp,abs;
    private String title;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }
    
    public String getTag(String s,String tag) {
      return tmp=Parser.CutTill(Parser.CutFrom(s,"\""+tag+"\": \""),"\"").trim();
    }

    public String getFirstTag(String s,String tag) {
      String tmp=Parser.CutTill(Parser.CutFrom(s,"\""+tag+"\": {"),"}").trim();
      if (tmp==null) Parser.CutTill(Parser.CutFrom(s,"\""+tag+"\": [{"),"}").trim();
      return tmp;
    }
    
    public String journalEntry(String s) {
      System.out.println("jE:"+s);
      if (s.trim().length()<2) return("");
      return getTag(s,"title")+" "+getTag(s,"volume")+" ("+getTag(s,"year")+") "+getTag(s,"pagination");
    }

    public void run() {
        String inspirebase=Information.get("$$params");
        if (inspirebase==null) inspirebase="http://inspirehep.net/";

        if (Information.containsKey("##search-selection")) {
            String lnk = Information.get("##search-selection");
            Msgs.add("found key: " + lnk);
            Information.put("inspirekey",lnk);
            tmp=webToolsHEP.getInspireRecord(inspirebase,lnk);

            //Keywords
            String keywords=webToolsHEP.keywordsFromInspire(tmp);
            boolean changed=false;
            String infokeywords=Information.getS("keywords");
            while (keywords.length()>0) {
                String keyword=Parser.CutFrom(keywords,"\">");
                keyword=Parser.CutTill(keyword,"</a>");
                if (!Parser.EnumContains(Information.getS("keywords"), keywords)) {
                    infokeywords+="|"+keyword;
                    if (infokeywords.charAt(0)=='|') infokeywords=infokeywords.substring(1);
                    changed=true;
                }
                keywords=Parser.CutFrom(keywords,"</small>");
            }
            if (changed) Information.put("keywords",infokeywords);

            //Abstract
            if (Information.isEmpty("abstract")) {
                abs = webToolsHEP.abstractFromInspire(tmp);
                if (abs != null) Information.put("abstract", abs);
            }

            //BibTeX
            if (Information.isEmpty("bibtex")) {
                Information.put("bibtex",webToolsHEP.bibTeXFromInspire(inspirebase,lnk));
            }

            if (Information.getS("bibtex").length()>2) {
                celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(Information.getS("bibtex"));

		// title and authors
                String title = BTR.get("title");
                if (title.startsWith("{")) {
                    title = title.substring(1, title.length() - 1);
                }
                Information.put("title", title);
                Information.put("authors", celsius.BibTeXRecord.authorsBibTeX2Cel(BTR.get("author")));

                // Paper type
                if (BTR.get("journal")!=null) {
                    Information.put("type","Paper");
                } else {
                    Information.put("type","Preprint");
                }

                // Citation tag
                if (Information.getS("citation-tag").equals("") || !Information.getS("citation-tag").equals(BTR.tag))
                    Information.put("citation-tag",BTR.tag);

                //doi, but not in BibTeX
                String doi=null;
                if ((BTR.getS("doi").equals("")) && (tmp.indexOf("http://dx.doi.org/")>-1)) {
                    doi=Parser.CutFrom(tmp,"http://dx.doi.org/");
                    doi=Parser.CutTill(doi,"\">");
                    BTR.put("doi", doi);
                    Information.put("bibtex",BTR.toString());
                }
            }
            
            //Identifier
            if (!Information.getS("identifier").equals(toolbox.Identifier(Information)))
                Information.put("identifier",toolbox.Identifier(Information));

            //References
            if (Information.isEmpty("links")) {
                Information.put("links",webToolsHEP.linksFromInspire(inspirebase,lnk));
            }
            Information.remove("##search-selection");
        } else {
            String title = Information.get("title").replaceAll("[^a-zA-Z0-9 ]", "");
            String authors = Information.getS("authors").replaceAll("\\|"," ");
            if (authors.length()>0) title=authors+" "+title;
            String srch= Parser.Substitute(title," ","+");
            if (srch.equals("unknown")) {
                toolbox.Warning(null,"Please enter at least one keyword from the title.", "Could not search at Inspire.net");
                return;
            }
            String results = new String("");
            String keys = new String("");
            Msgs.add(TI + "Looking for: " + srch);
            // http://inspirehep.net/search?ln=en&p=saemann+monopole&of=hb&action_search=Search&sf=earliestdate&so=d
            srchstring = "http://inspirehep.net/search?p="+srch+"&of=recjson&ot=recid,title,authors,publication_info,comment,primary_report_number&rg=25";
            Msgs.add(TI + "Search string: " + srchstring);
            String tmp = TextFile.ReadOutURL(srchstring);
            //Msgs.add(tmp);
            System.out.println(tmp);
            if (tmp.startsWith("##??")) {
                Msgs.add(TI + "Error contacting Inspire: " + tmp);
                toolbox.Information(null,"Error contacting Inspire: " + tmp, "Sorry...");
                return;
            }
            tmp=Parser.CutFrom(tmp,"[");
            if (tmp.length() > 0) {
                Msgs.add(TI + "Inspire answered:");
                while(tmp.indexOf("{\"comment\":")>-1) {
                    System.out.println("---");
                    String record=Parser.CutTill(Parser.CutFrom(tmp,"\"recid\": "),",").trim();
                    System.out.println(record);
                    title=getTag(tmp,"title\": {\"title");
                    System.out.println(title);
                    String preauthors=Parser.CutTill(Parser.CutFrom(tmp,"\"authors\": ["),"{\"comment\":").trim();
                    authors=new String("");
                    System.out.println("PRE:"+preauthors);
                    while(preauthors.indexOf("\"last_name\": \"")>-1) {
                        authors+=", "+getTag(preauthors,"last_name");
                        System.out.println(authors);
                        preauthors=Parser.CutFrom(preauthors, "\"last_name\": \"");
                    }
                    authors=authors.substring(2);
                    String comments=getTag(tmp,"comment")+" "+getTag(tmp,"primary_report_number")+" "+getTag(tmp,"reference");
                    results += "|<html><b>" + title + "</b><br/>"+authors+" ("+comments.trim()+")</html>";
                    keys += "|" + record;
                    tmp="{\"comment\""+Parser.CutFrom(tmp,"},{\"comment\"");
                }
            }
            Information.put("##search-results", results);
            Information.put("##search-keys", keys);
        }
    }
}

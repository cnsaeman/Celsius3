/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.*;
import java.io.*;
import celsius.tools.*;

public class webToolsHEP {
    
    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    public final static String EOP=String.valueOf((char)12);   // EndOfPage signal
    
    /**
     * Download inspire record
     * @param inspirebase
     * @param inspirekey
     * @return 
     */
    public static String getInspireRecord(String inspirebase, String inspirekey) {
        String srchstring = inspirebase + "record/" + inspirekey;
        return(TextFile.ReadOutURL(srchstring));
    }
    
    public static String getInspireDetails(String inspirebase, String inspirekey) {
        String srchstring = inspirebase + "record/" + inspirekey + "?of=xm&ot=100,700,84,37,245,260,300,520,773";
        String tmp=TextFile.ReadOutURL(srchstring);
        if (!tmp.startsWith("<?xml version")) return(null);
        HashMap data=new HashMap<String,String>();
        return(Parser.CutTill(Parser.CutFrom(tmp,"<controlfield tag=\"001\">"),"</controlfield>"));
    }
    
    
    /**
     * Downloads abstract from the ADSABS-link given in the inspires record
     * @param tmp : The inspire record page
     * @return 
     */
    public static String abstractFromInspire(String tmp) {
        String abs=Parser.CutFrom(tmp,"<strong>Abstract:");
        abs=Parser.CutTill(abs,"</small>");
        abs=Parser.CutFrom(abs,">").trim();
        if (abs.length()>1) return(abs);
        if (tmp.indexOf("www.adsabs.harvard.edu/abs")>-1) {
            String link="http://www.adsabs.harvard.edu/abs"+Parser.CutTill(Parser.CutFrom(tmp,"www.adsabs.harvard.edu/abs"),"\"");
            tmp=TextFile.ReadOutURL(link);
            abs=Parser.CutTill(Parser.CutFrom(tmp, "Abstract</h3>"),"<hr>").trim();
            if (abs.length()>3) return(abs);
        }
        return(null);
    }
    
    public static String bibTeXFromInspire(String inspirebase,String lnk) {
        String tmp = TextFile.ReadOutURL(inspirebase + "record/" + lnk + "/export/hx");
        System.out.println(inspirebase + "record/" + lnk + "/export/hx");
        //System.out.println("Response:"+tmp);
        if (tmp.indexOf("<pre>") > -1) {
            String bib = Parser.CutTill(Parser.CutFrom(tmp, "<pre>"), "</pre>").trim();
            if (bib.charAt(bib.length() - 3) == ',') {
                bib = Parser.CutTillLast(bib, ",") + "\n}";
            }
            return(bib);
        }
        return(null);
    }
    
    public static String keywordsFromInspire(String tmp) {
        String keywords = Parser.CutFrom(tmp, "<strong>Keyword(s):");
        keywords = Parser.CutTill(keywords, "<br").trim();
        keywords = Parser.CutTill(keywords, "<div").trim();
        keywords = Parser.CutFrom(keywords, "<a").trim();
        return(keywords);
    }
    
    public static String extractCitations(String tmp) {
        return(Parser.CutTill(Parser.CutFrom(tmp, "Citations ("), ")"));
    }
    
    public static String linksFromInspire(String inspirebase, String lnk) {
        String srchstring = inspirebase + "record/" + lnk + "/references";
        String tmp2 = TextFile.ReadOutURL(srchstring);
        String links = new String("");
        tmp2 = Parser.CutFrom(tmp2, "<table><tr><td valign=\"top\">");
        while (tmp2.indexOf("</tr><tr>") > -1) {
            String link = Parser.CutTill(tmp2, "</tr><tr>").trim();
            if (link.indexOf("/record/") > -1) {
                links += "|refers to:inspirekey:" + Parser.CutTill(Parser.CutFrom(link, "/record/"), "\n");
            } else {
                link = Parser.CutTill(Parser.CutFrom(Parser.CutFrom(Parser.CutFrom(link, "<small>"),"<small>"), "<small>"), "</small>").trim();
                links += "|refers to:identifier:" + link;
            }
            tmp2 = Parser.CutFrom(tmp2, "</tr><tr>");
        }
        srchstring = inspirebase + "search?ln=en&p=refersto%3Arecid%3A" + lnk;
        tmp2 = TextFile.ReadOutURL(srchstring);
        tmp2 = Parser.CutFrom(tmp2, "<!C-START");
        while (tmp2.indexOf("<div class=\"record_body\">") > -1) {
            tmp2 = Parser.CutFrom(tmp2, "<div class=\"record_body\">");
            String link = Parser.CutTill(tmp2, "</div>").trim();
            links += "|citation:inspirekey:" + Parser.CutTill(Parser.CutFrom(link, "/record/"), "\"");
            tmp2 = Parser.CutFrom(tmp2, "</div>");
        }
        return(links.substring(1));
    }
    
    public static String arXivRefFromInspire(String inspirebase, String lnk) {
        String links = new String("");
        String srchstring = inspirebase + "search?ln=en&p=refersto%3Arecid%3A" + lnk + "&rg=100";
        String tmp2 = TextFile.ReadOutURL(srchstring);
        tmp2 = Parser.CutFrom(tmp2, "<!C-START");
        while (tmp2.indexOf("<div class=\"record_body\">") > -1) {
            tmp2 = Parser.CutFrom(tmp2, "<div class=\"record_body\">");
            String link = Parser.CutTill(tmp2, "</div>").trim();
            links += "|" + Parser.CutTill(Parser.CutFrom(link, "<br/>e-Print: <b>"), "</b>");
            tmp2 = Parser.CutFrom(tmp2, "</div>");
        }
        return(links.substring(1));
    }
    
    public static String inspireRecordJSON(String rec) {
      String tmp2 = TextFile.ReadOutURL("https://inspirehep.net/record/"+rec+"?of=recjson&ot=comment,title,system_number,abstract,authors,doi,primary_report_number,publication_info,physical_description,number_of_citations,thesaurus_terms");
      return tmp2;
    }
    
    
}

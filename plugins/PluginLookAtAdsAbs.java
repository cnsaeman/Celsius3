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
public class PluginLookAtAdsAbs extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look at ADSabs");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for a record corresponding to title (and author, if specified) at adsabs.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"title");
            put("type"              ,"interactive");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI = "P:LaAA>";
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    private String title;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    private void putSafely(String tag,String value) {
        if (Information.getS(tag).length()<value.length()) Information.put(tag,value);
    }

    public void run() {
        if (Information.containsKey("##search-selection")) {
            String url = Information.get("##search-selection");
            Msgs.add("found url: " + url);
            Information.put("adsabs-url", url);
            String tmp=TextFile.ReadOutURL(url);
            if (tmp.startsWith("##??")) {
                Msgs.add(TI + "Error contacting adsabs: " + tmp);
                toolbox.Information(null,"Error contacting adsabs: " + tmp, "Sorry...");
                return;
            }
            String value=Parser.CutTags(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp,"<b>Title:</b>"),"valign=\"top\">"),"</td>"));
            putSafely("title",value);
            value=Parser.Substitute(Parser.Substitute(Parser.CutTags(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp,"<b>Authors:</b>"),"valign=\"top\">"),"</td>")),"&#160;"," "),"; ","|");
            putSafely("authors",value);
            value=Parser.CutTags(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp,"<b>Publication Date:</b>"),"valign=\"top\">"),"</td>"));
            putSafely("date",value);
            value=Parser.CutTags(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp,"<b>Keywords:</b>"),"valign=\"top\">"),"</td>"));
            putSafely("keywords",value);
            value=Parser.CutTill(Parser.CutFrom(tmp,"Abstract</h3>"),"<hr>");
            putSafely("abstract",value);
            value=Parser.CutTags(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp,"<b>Bibliographic"),"valign=\"top\">"),"</td>"));
            tmp=TextFile.ReadOutURL("http://adsabs.harvard.edu/cgi-bin/nph-bib_query?bibcode="+value+"&data_type=BIBTEX&db_key=PHY&nocookieset=1");
            String bib="@ARTICLE"+Parser.CutFrom(tmp,"@ARTICLE");
            celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(bib);
            if (BTR.parseError == 0) {
                putSafely("bibtex",BTR.toString());
                Information.put("citation-tag", BTR.tag);
                Information.put("identifier", toolbox.Identifier(Information));
            }
            Information.remove("##search-selection");
        } else {
            String title = Parser.Substitute((Information.getS("title")+" "+Information.getS("authors")).trim().replaceAll("[^a-zA-Z0-9 ]", "")," ","+");
            if (title.length()<3) {
                toolbox.Warning(null,"Please enter at least one keyword from the title.", "Could not search adsabs");
                return;
            }
            String srchstring="http://adsabs.harvard.edu/cgi-bin/basic_connect?qsearch="+title+"&version=1";
            String results = new String("");
            String keys = new String("");
            Msgs.add(TI + "Search string: " + srchstring);
            String tmp = TextFile.ReadOutURL(srchstring);
            if (tmp.startsWith("##??")) {
                Msgs.add(TI + "Error contacting adsabs: " + tmp);
                toolbox.Information(null,"Error contacting adsabs: " + tmp, "Sorry...");
                return;
            }
            tmp = Parser.CutTill(Parser.CutFrom(tmp, "<HR></td></tr>"),"</table");
            if (tmp.length() > 0) {
                Msgs.add(TI + "adsabs answered:");
                String entry = new String("");
                String inauthors = new String("");
                while (tmp.length()>0) {
                    entry=Parser.CutTill(Parser.CutFrom(tmp,"<a href=\""),"\">");
                    title = Parser.CutTill(Parser.CutFrom(tmp,"<td align=\"left\" valign=\"top\" colspan=3>"),"</td>");
                    inauthors = Parser.CutTill(Parser.CutFrom(tmp,"<td align=\"left\" valign=\"top\" width=\"25%\">"),"</td>");
                    if ((title.length() > 0) && !(Parser.HowOftenContains(title, "\n") > 8)) {
                        results += "|<html><b>" + title + "</b><br/>by " + inauthors+"</html>";
                        keys += "|" + entry;
                    }
                    tmp = Parser.CutFrom(tmp, "<HR></td></tr>").trim();
                }
            }
            if (results.length()==0) {
                toolbox.Information(null,"Sorry, no relevant entry was returned by ADSabs.", "No search results found.");
            }
            Information.put("##search-results", results);
            Information.put("##search-keys", keys);
        }
    }
}

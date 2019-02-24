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
public class PluginJSTOR extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from JSTOR");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin looks for an entry at JSTOR and downloads information.");
            put("needsFirstPage"    ,"yes");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public String shorten(String tmp) {
        return (tmp);
    }

    public void run() {
        String fp = Information.get("firstpage");
        int i = fp.indexOf("http://www.jstor.org/stable/");
        if (i > -1) {
            String nmb = Parser.CutTill(Parser.CutFrom(fp, "http://www.jstor.org/stable/"), " ");
            String title = Parser.CutFromLast(Parser.CutTill(fp, "Author(s):").trim(),"\n").trim();
            String authors = Parser.CutTill(Parser.CutFrom(fp, "Author(s):"), "\n").trim();
            fp = Parser.CutFrom(fp, "Source:");
            String journal = Parser.CutTill(fp, ", ").trim();
            String vol;
            if (fp.indexOf("No.") > -1) {
                vol = Parser.CutTill(Parser.CutFrom(fp, ", Vol."), ", No.").trim();
                fp = Parser.CutFrom(fp, ", No.");
            } else {
                vol = Parser.CutTill(Parser.CutFrom(fp, ", Vol."), "(").trim();
            }
            String year = Parser.CutTill(Parser.CutFrom(fp, ".,"), ")").trim();
            String pages = Parser.CutTill(Parser.CutFrom(fp, "pp."), "Published").trim();
            Information.put("JSTOR-ID", nmb);
            Information.put("title", title);
            Information.put("authors", toolbox.authorsBibTeX2Cel(authors));
            Information.put("type","Paper");
            String bibtex = new String("@Article{JSTOR:" + nmb + ",\n  author    = \"" + shorten(authors) + "\",\n  title     = \"" + title + "\",\n  journal   = \"" + journal + "\",\n  volume    = \"" + vol + "\",\n  year      = \"" + year + "\"\n  pages     = \"" + pages + "\"\n  url       = \"http://www.jstor.org/stable/" + nmb+"\"\n}");
            Information.put("bibtex", bibtex);
            Information.put("citation-tag", Parser.CutTill(Parser.CutFrom(bibtex, "{"), ",").trim());
            Information.put("identifier", toolbox.Identifier(Information));
            Msgs.add("Found JSTOR-ID:" + nmb);
            Msgs.add("Found Information:" + bibtex);
        }
    }
}

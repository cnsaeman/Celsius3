import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;


public class PluginExtLaTeX extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Extended LaTeX");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin is an export plugin and creates an extended bibitem entry from the BibTeX record.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"bibtex");
            put("type"              ,"export");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    /**
     * Returns Authors Latex format from BibTex string
     */
    public String LaTeXAuthorsFromBibTex(String authors) {
        String author;
        String out = new String("");
        while (authors.length() > 0) {
            author = Parser.CutTill(authors, " and ");
            if (author.indexOf(",") > -1) {
                author = Parser.CutFrom(author, ",").trim() + " " + Parser.CutTill(author, ",").trim();
            }
            if (author.indexOf(".") == -1) {
                String prenomes = Parser.CutTillLast(author, " ").trim();
                author = Parser.CutFromLast(author, " ").trim();
                int i = prenomes.lastIndexOf(" ");
                while (i > -1) {
                    author = prenomes.substring(i + 1, i + 2) + ". " + author;
                    prenomes = prenomes.substring(0, i).trim();
                    i = prenomes.lastIndexOf(" ");
                }
                if (prenomes.length()>0) author = prenomes.substring(0, 1) + ". " + author;
            }
            out += author.replace(" ", "~") + ", ";
            authors = Parser.CutFrom(authors, " and ");
        }
        out = Parser.CutTillLast(out, ", ");
        if (out.indexOf(", ") > -1) {
            out = Parser.CutTillLast(out, ", ") + " and " + Parser.CutFromLast(out, ", ");
        }
        return (out);
    }

    /**
     * Turn BibTeX-information string into a Latex string
     */
    public String LatexFromBibTeX(String t1) {
        celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(t1);
        if (BTR.parseError!=0) return("parse Error");
        String out = new String("%\\cite{" + BTR.tag + "}\n" +
                "\\bibitem{" + BTR.tag + "}\n");
        out += LaTeXAuthorsFromBibTex(BTR.get("author")) + ", {\\em " + BTR.get("title") + ",}";
        boolean journal = false;
        if (BTR.get("journal")!=null) {
            if (BTR.get("doi")!=null) {
                out+= "\n\\href{http://dx.doi.org/"+BTR.get("doi")+"}{";
            } else out+="\n";
            out += BTR.get("journal").replace(".", ".\\");
            String tmp = BTR.getS("volume");
            if (!tmp.equals("")) {
                if (Character.isLetter(tmp.charAt(0))) {
                    out += " " + tmp.substring(0, 1);
                    tmp = tmp.substring(1);
                }
                out += " {\\bf " + tmp + "} (" + BTR.getS("year") + ") ";
            } else {
                out += " (" + BTR.getS("year") + ") ";
            }
            out += Parser.CutTill(BTR.getS("pages"), "-");
            journal = true;
            if (BTR.get("doi")!=null)
                out+= "}";
        }
        if (BTR.get("note")!=null) {
            out += "\n" + BTR.get("note");
        }
        if (BTR.get("eprint")!=null) {
            if (!out.endsWith("\n")) {
                out += " ";
            }
            if (journal) {
                out += "[";
            }
            out += "{\\tt \\href{http://www.arxiv.org/abs/"+BTR.get("eprint")+"}{"+BTR.get("eprint")+"}}";
            if (journal) {
                out += "]";
            }
        }
        out += ".";
        if (BTR.get("slaccitation")!=null) {
            out += "\n" + BTR.get("slaccitation");
        }
        while (out.indexOf("  ") > -1) {
            out = out.replace("  ", " ");
        }
        return (out);
    }

    public void run() {
        String bib = Information.get("bibtex");
        Information.put("::inter","\n\n");
        if (bib == null) {
            Information.put("output", "");
            return;
        }
        Information.put("output", LatexFromBibTeX(bib));
    }
}

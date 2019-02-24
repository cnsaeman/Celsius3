import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;


public class PluginSimple extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Simple reference");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin creates a simple literature reference");
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
    
    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
    
    /**
     * Returns Authors Latex format from BibTex string
     */
    public String LaTeXAuthorsFromBibTex(String authors) {
        String author;
        String out=new String("");
        while(authors.length()>0) {
            author=Parser.CutTill(authors," and ");
            if (author.indexOf(",")>-1)
                author=Parser.CutFrom(author,",").trim()+" "+Parser.CutTill(author,",").trim();
            if (author.indexOf(".")==-1) {
                String prenomes=Parser.CutTillLast(author," ").trim();
                author=Parser.CutFromLast(author," ").trim();
                int i=prenomes.lastIndexOf(" ");
                while (i>-1) {
                    author=prenomes.substring(i+1,i+2)+". "+author;
                    prenomes=prenomes.substring(0,i).trim();
                    i=prenomes.lastIndexOf(" ");
                }
                if (prenomes.length()>0) author = prenomes.substring(0, 1) + ". " + author;
            }
            out+=author+", ";
            authors=Parser.CutFrom(authors," and ");
        }
        out=Parser.CutTillLast(out,", ");
        if (out.indexOf(", ")>-1)
           out=Parser.CutTillLast(out,", ")+" and "+Parser.CutFromLast(out,", ");
        return(out);
    }
    
    public String clean(String s) {
      if (s.startsWith("{"))
        s=s.substring(1);
      if (s.endsWith("}"))
        s=s.substring(0,s.length()-1);
      return(s);  
    }
    
    
    /**
     * Turn BibTeX-information string into a Latex string
     */
    public String LatexFromBibTeX(String t1) {
        celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(t1);
        if (BTR.parseError!=0) return("parse Error");
        String out=new String("");
        out+=LaTeXAuthorsFromBibTex(BTR.get("author"))+", \""+clean(BTR.get("title"))+"\",";
        boolean journal=false;
        if (BTR.get("journal")!=null) {
            out+="\n"+BTR.get("journal");
            String tmp=BTR.getS("volume");
            if (!tmp.equals("")) {
                if (Character.isLetter(tmp.charAt(0))) {
                    out+=" "+tmp.substring(0,1);
                    tmp=tmp.substring(1);
                }
                out+=" "+tmp+" ("+BTR.getS("year")+") ";
            } else {
                out+=" ("+BTR.getS("year")+") ";
            }
            out+=Parser.CutTill(BTR.getS("pages"),"-");
            journal=true;
        } else if (BTR.type.equals("book")) {
            out+=" "+BTR.get("publisher");
            if (BTR.get("location")!=null) out+=", "+BTR.get("location");
            out+=", "+BTR.get("year");            
        }
        if (BTR.get("note")!=null)
            out+="\n"+BTR.get("note");
        if (BTR.get("eprint")!=null) {
            if (!out.endsWith("\n")) out+=" ";
            if (journal) out+="[";
            out+="arXiv:"+BTR.get("eprint");
            if (journal) out+="]";
        }
        out+=".";
        while (out.indexOf("  ")>-1) out=out.replace("  "," ");
        return(out);
    }
    
    
    
    public void run() {
        String bib=Information.get("bibtex");
        if (bib==null) {
            Information.put("output","");
            return;
        }
        Information.put("output",LatexFromBibTeX(bib));
    }
    
    
}

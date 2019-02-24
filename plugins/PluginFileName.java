/*
 * PluginFileName.java
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
public class PluginFileName extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from Filename");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.3");
            put("help"              ,"This plugin names a file according to its filename, if this is of the form title (author).");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
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

    public void run() {
        String FileName = Parser.CutFromLast(Information.get("fullpath"), arxivTools.filesep);
	if (FileName.endsWith(".avi") || FileName.endsWith(".mpg") || FileName.endsWith(".mkv")) {
	    String title=Parser.CutTill(FileName,".avi").toLowerCase();
	    title=Parser.CutTill(title,"(");
	    title=Parser.CutTill(title,"[");
	    title=Parser.Substitute(title,"."," ");
	    title=Parser.Substitute(title,"_"," ");
	    title=Parser.Substitute(title,"for","");
	    title=Parser.Substitute(title,"the","");
	    Information.put("title",title);
	    return;
	} else if (FileName.startsWith("[")) {
          String authors=Parser.Substitute(Parser.CutTill(FileName.substring(1),"]"),"_"," ").trim();
          authors=Parser.Substitute(authors,","," and ");
          authors=toolbox.authorsBibTeX2Cel(authors);
          String title=Parser.Substitute(Parser.CutFrom(FileName,"]"),"_"," ").trim();
          title=Parser.Substitute(title,"(1)","");
          title=Parser.Substitute(title,"(BookFi.org)","");
          title=Parser.Substitute(title,"(BookZZ.org)","");
          if ((title.length() > 3) && (authors.length() > 1)) {
              Information.put("type", "Book");
              Information.put("title", title);
              Information.put("authors", authors);
              Information.put("recognitionlevel", "50");
          }
	} else if ((FileName.indexOf("(") > 0) || (FileName.indexOf(" - ")>0)) {
            if (FileName.indexOf(")(") > 0) {
                System.out.println("Type 0::"+FileName);
                String title = Parser.CutTill(FileName, "(").trim();
                String authors = Parser.CutTillLast(title, ". ") + ".";
                title = Parser.CutFromLast(title, ". ");
                StringBuffer sb = new StringBuffer(authors);
                while (sb.indexOf(".") > 2) {
                    sb.delete(sb.indexOf(".") - 1, sb.indexOf(".") + 1);
                }
                authors = sb.toString().replace(" ,", ",").trim();
                // Entries correct -> make the necessary entries
                if ((title.length() > 3) && (authors.length() > 1)) {
                    Information.put("title", title);
                    Information.put("authors", authors);
                    Information.put("recognitionlevel", "50");
                }
            } else {
                // Filename is of the form "authorname, authorfirstname - title.filetype"
                System.out.println(FileName+"::"+String.valueOf(FileName.indexOf(" - "))+"::"+String.valueOf(FileName.indexOf(" - ")));
                if ((FileName.indexOf(" - ")>FileName.indexOf(", "))) {
                    System.out.println("Type 2::"+FileName);
                    String title = Parser.CutTillLast(Parser.CutFrom(FileName, " - "),".").trim();
                    String authors = Parser.CutTill(FileName, " - ").trim();
                    System.out.println(title+" by "+authors);
                    if ((title.length() > 3) && (authors.length() > 1)) {
                        Information.put("title", title);
                        Information.put("authors", authors);
                        Information.put("recognition", "50");
                    }
                } else {
                    System.out.println("Type 3::"+FileName);
                    // FileName is of the form "title (authors)"
                    String title = Parser.CutTillLast(FileName, "(").trim().replace("_", " ");
                    String authors = Parser.CutTill(Parser.CutFromLast(FileName, "("), ")").replace("_", " ");
                    authors = authors.replace(',', '|');
                    authors = authors.replace("| ", "|");
                    // Entries correct -> make the necessary entries
                    if ((title.length() > 3) && (authors.length() > 1)) {
                        Information.put("title", title);
                        Information.put("authors", authors);
                        Information.put("recognition", "50");
                    }
                }
            }
        }
    }
}

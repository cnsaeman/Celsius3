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
public class PluginFileName2 extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from Filename2");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.2");
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
        String list=TextFile.ReadOutFile("/home/cnsaeman/Celsius3/info.txt");
        String FileName = Parser.CutFromLast(Information.get("fullpath"), arxivTools.filesep);
        String id=Parser.CutFrom(Parser.CutTill(FileName," "),"gtm")+" ";
        while (id.startsWith("0")) id=id.substring(1);
        String line=Parser.CutTill(Parser.CutFrom(list,id),"\n");
        String title=Parser.CutTill(line,",").trim();
        String preauthors=Parser.CutTill(Parser.CutFrom(line,", "),"(").trim();
        String authors="";
            while (preauthors.length()>0) {
                String author = Parser.CutTill(preauthors, ",").trim();
                preauthors=Parser.CutFrom(preauthors, ",").trim();
                authors+="|"+Parser.CutFromLast(author," ").trim()+", "+Parser.CutTillLast(author," ");
            }
        authors=authors.substring(1);
        String year="";
        String isbn="";
        if (line.indexOf("(")>-1) {
            year=Parser.CutTill(Parser.CutFrom(line,"("),", ");
            isbn=Parser.CutTill(Parser.CutFromLast(line,","),")").trim();
            if (isbn.startsWith("ISBN")) isbn=Parser.CutFrom(isbn,"ISBN").trim();
            Information.put("year", year);
            Information.put("isbn", isbn);
        }
        Information.put("series","GTM "+id);
        Information.put("title",title);
        Information.put("authors",authors);
        Information.put("recognition","100");
        Information.put("type","Book");
    }
}

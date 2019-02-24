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
public class PluginIMSLP extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from International Music Score Library Project");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for an entry at IMSLP and downloads information.");
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


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    public void run() {
        String namepart=Parser.CutFromLast(Information.get("fullpath"),arxivTools.filesep);
        if (namepart.startsWith("IMSLP")) {
            String id=Parser.CutTill(Parser.CutFrom(namepart,"IMSLP"),"-");
            String inf=TextFile.ReadOutURL("http://imslp.org/index.php?title=Special:ReverseLookup&action=submit&indexsearch="+id);
            String link=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(inf,"<div class=\"printfooter\">"),"<a href=\""),"\"");
            String maintitle=Parser.CutFromLast(Parser.CutTill(inf,"</h1>"),"\">");
            String composer=Parser.CutTill(Parser.CutFrom(maintitle,"("),")");
            maintitle=Parser.CutTill(maintitle," (");
            inf=Parser.CutTill(Parser.CutFrom(inf,"<div id=\"IMSLP"+id),"<div id=\"");
            String subtitle=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(inf,"<span title=\"Download this file\""),"</span></span>"),"</span></a>");
            while (inf.indexOf("\"we_edition_label\">")>-1) {
                String tag=Parser.CutTill(Parser.CutFrom(inf,"\"we_edition_label\">"),":</div");
                inf=Parser.CutFrom(inf,"\"we_edition_label\">");
                String val=Parser.CutTags(Parser.CutTill(Parser.CutFrom(inf,"\"we_edition_entry\">"),"</div"));
                if (val.indexOf("\"we_edition_label\">")==-1) Information.put(tag,val);
            }
            Information.put("composer",composer);
            Information.put("title",maintitle+" - "+subtitle);
            Information.put("imslp-id",id);
            Information.put("imslp-link",link);
        } 
    }
  
                      
}

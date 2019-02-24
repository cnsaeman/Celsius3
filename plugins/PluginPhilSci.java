/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;



/**
 * @author cnsaeman
 */
public class PluginPhilSci extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look at PhilSci Archive");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for an entry at the PhilSci Archive and downloads information.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"interactive");
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
        if (Information.containsKey("##search-selection")) {
            String id = Information.get("##search-selection");
            String inf=TextFile.ReadOutURL("http://philsci-archive.pitt.edu/archive/"+id);
            System.out.println(inf);
            String title=Parser.CutTill(Parser.CutFrom(inf,"<h1 class=\"pagetitle\">"),"</h1>");
            String authors=Parser.CutTill(Parser.CutFrom(inf,"<p><span class=\"citation\">")," (");
            String abs=Parser.CutTill(Parser.CutFrom(inf,"<h2>Abstract</h2><p>"),"</p>");
            String key=Parser.CutTill(Parser.CutFrom(inf,"Keywords:</th><td>"),"</td>");
            String subjects=Parser.CutTags(Parser.CutTill(Parser.CutFrom(inf,"Subjects:</th><td>"),"</td>"));
            String date=Parser.CutTill(Parser.CutFrom(inf,"<th>Deposited On:</th><td>"),"</td>");
            Information.put("type","Preprint");
            Information.put("authors",authors);
            Information.put("title",title);
            Information.put("abstract",abs);
            Information.put("key",key);
            Information.put("subjects",subjects);
            Information.put("date",date);
            Information.put("PhilSci-ID",id);
        } else {
            String results = new String("");
            String keys = new String("");
            String title=Information.get("title");
            if (title==null) title=new String("");
            String authors=Information.get("authors");
            if (authors==null) authors=new String("");
            if (title.length()+authors.length()>3) {
                  String inf=TextFile.ReadOutURL("http://philsci-archive.pitt.edu/perl/search?abstract/keywords/title="+title+"&abstract/keywords/title_srchtype=ALL&authors/editors="+authors+"&authors/editors_srchtype=ALL&year=&_satisfyall=ALL&_order=byyear&_action_search=Search");
                  boolean found=false;
                  boolean cancel=false;
                  inf=Parser.CutFrom(inf,"<a  href=\"http://philsci-archive.pitt.edu/archive/");
                  String id=new String("");
                  while((inf.length()>0) && (!found) && (!cancel)) {
                      results+="|"+Parser.CutTill(Parser.CutFrom(inf,"<span  class=\"citation\">"),"</span>");
                      keys+="|"+Parser.CutTill(inf,"/");
                      inf=Parser.CutFrom(inf,"<a  href=\"http://philsci-archive.pitt.edu/archive/");
                  }
            }
            Information.put("##search-results", results);
            Information.put("##search-keys", keys);
        }
    }
  
                      
}

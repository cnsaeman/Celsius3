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
public class tester {

    private final String TI = "P:LaS>";
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    private static String srchstring,tmp,abs;
    private static String title;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }
    
    public static String getTag(String s,String tag) {
      return tmp=Parser.CutTill(Parser.CutFrom(s,"\""+tag+"\": \""),"\"").trim();
    }

    public static String getFirstTag(String s,String tag) {
      String tmp=Parser.CutTill(Parser.CutFrom(s,"\""+tag+"\": {"),"}").trim();
      if (tmp==null) Parser.CutTill(Parser.CutFrom(s,"\""+tag+"\": [{"),"}").trim();
      return tmp;
    }
    
    public static String journalEntry(String s) {
      System.out.println("jE:"+s);
      if (s.trim().length()<2) return("");
      return getTag(s,"title")+" "+getTag(s,"volume")+" ("+getTag(s,"year")+") "+getTag(s,"pagination");
    }

    public static void main(String params[]) {
      System.out.println("Hello!");
        String inspirebase;
            String title = "Geometry";
            String authors = "Rosly";
            if (authors.length()>0) title=authors+" "+title;
            String srch= Parser.Substitute(title," ","+");
            if (srch.equals("unknown")) {
                toolbox.Warning(null,"Please enter at least one keyword from the title.", "Could not search at Inspire.net");
                return;
            }
            String results = new String("");
            String keys = new String("");
            //Msgs.add(TI + "Looking for: " + srch);
            // http://inspirehep.net/search?ln=en&p=saemann+monopole&of=hb&action_search=Search&sf=earliestdate&so=d
            srchstring = "http://inspirehep.net/search?p="+srch+"&of=recjson&ot=recid,title,authors,publication_info,comment,primary_report_number&rg=25";
            //Msgs.add(TI + "Search string: " + srchstring);
            String tmp = TextFile.ReadOutURL(srchstring);
            //Msgs.add(tmp);
            System.out.println(tmp);
            if (tmp.startsWith("##??")) {
                //Msgs.add(TI + "Error contacting Inspire: " + tmp);
                //toolbox.Information(null,"Error contacting Inspire: " + tmp, "Sorry...");
                return;
            }
            tmp=Parser.CutFrom(tmp,"[");
            if (tmp.length() > 0) {
                System.out.println("parsing...");
                //Msgs.add(TI + "Inspire answered:");
                while(tmp.indexOf("{\"comment\":")>-1) {
                    System.out.println("---");
                    String record=getTag(tmp,"comment");
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
                    String comments=getTag(tmp,"comment")+" "+getTag(tmp,"primary_report_number")+" "+journalEntry(getFirstTag(tmp,"publication_info"));
                    results += "|<html><b>" + title + "</b><br/>"+authors+" ("+comments.trim()+")</html>";
                    keys += "|" + record;
                    tmp="\"comment\":"+Parser.CutFrom(tmp,"},{\"comment\":");
                }
            }
            //Information.put("##search-results", results);
            //Information.put("##search-keys", keys);
  }
}

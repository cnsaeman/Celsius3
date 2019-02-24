/*
 * PluginLookAtIMDBTV.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;




/**
 * @author cnsaeman
 */
public class PluginLookAtIMDBTV extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look for TV show at IMDB");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for a record corresponding to the show's title at the Internet Movie Database.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"show");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI = "P:LaS>";
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    private String spiresbase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public String imdb_req(String request) {
        StringBuffer output = new StringBuffer("");
        try {
            Socket server = new Socket("www.imdb.com", 80);
            OutputStream out = server.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));

            //GET-Kommando senden
            String req = "GET "+request+" HTTP/1.0\r\n";
            req += "Host: www.imdb.com\r\n";
            req += "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7" + "\r\n" + "\r\n";

            out.write(req.getBytes());

            int len;
            try {
                for (String line; (line = in.readLine()) != null;) {
                    output.append(line);
                    output.append('\n');
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (output.toString());
    }

    private String div_class (String s1,String s2) {
        String ret=s1;
        ret=Parser.CutFrom(Parser.CutFrom(ret,"<h5>"+s2),"</h5>");
        ret=Parser.CutTill(ret,"</div>");
        ret=Parser.CutFrom(ret,"<div class=\"info-content\">");
        return(ret.trim());
    }

    private String to_people(String s1) {
        String ret=new String("");
        String tmp;
        while (s1.indexOf("</a>")>-1) {
            tmp=Parser.CutTill(s1, "</a>");
            tmp=Parser.CutFromLast(tmp,">").trim();
            ret+="|"+Parser.CutFromLast(tmp, " ")+", "+Parser.CutTillLast(tmp, " ");
            s1=Parser.CutFrom(s1,"</a>");
        }
        return(ret.substring(1));
    }

    private String to_people2(String s1) {
        String ret=new String("");
        String tmp;
        while (s1.indexOf("<a href=\"/name/nm")>-1) {
            tmp=Parser.CutFrom(s1,"<a href=\"/name/nm");
            s1=tmp;
            tmp=Parser.CutFrom(tmp,";\">");
            if (!tmp.startsWith("<img src")) {
                tmp=Parser.CutTill(tmp, "</a>").trim();
                ret+="|"+Parser.CutFromLast(tmp, " ")+", "+Parser.CutTillLast(tmp, " ");
            }
        }
        return(ret.substring(1));
    }

    private String to_genre(String s1) {
        String ret=new String("");
        String tmp;
        while (s1.indexOf("</a>")>-1) {
            tmp=Parser.CutTill(s1, "</a>");
            tmp=Parser.CutFromLast(tmp,">").trim();
            ret+="|"+tmp.trim();
            s1=Parser.CutFrom(s1,"</a>");
        }
        return(ret.substring(1));
    }

    public String from_HTML(String s) {
        s=Parser.Substitute(s,"&#x27;","'");
        s=Parser.Substitute(s, "&nbsp;", " ");
        s=Parser.Substitute(s, "&#xE9;", "é");
        s=Parser.Substitute(s, "&#xE8;", "è");
	s=Parser.Substitute(s, "&#xEE;", "î");
	s=Parser.Substitute(s, "&#xFC;", "ü");
	s=Parser.Substitute(s, "&#xF6;", "ö");
	s=Parser.Substitute(s, "&#x22;", "\"");
        return(s);
    }

    public void run() {
        if (!Information.containsKey("##search-selection")) {
            String title = Information.get("show");
            if ((title==null) || (title.equals("<unknown>"))) {
                toolbox.Warning(null,"Please enter at least parts of the title of the movie.", "Could not search at IMDB");
                return;
            }
            String result = new String("");
            String keys = new String("");
            Msgs.add(TI + "Looking for: " + title + " at IMDB");

            String broadsearch=imdb_req("/find?s=tt&q="+Parser.Substitute(title, " ", "+"));
            /*System.out.println(broadsearch);*/
            System.out.println(broadsearch.indexOf("302 Found"));

            if (broadsearch.indexOf("302 Found")>-1) {
                Information.put("##search-selection",Parser.CutTill(Parser.CutFrom(broadsearch, "http://www.imdb.com/title/"),"/").trim());
            } else {
                String srchstring = from_HTML(broadsearch);

                System.out.println("/find?s=tt&q=" + Parser.Substitute(title, " ", "+"));
                srchstring = Parser.CutFrom(srchstring, "</b> (Displaying");
                srchstring = Parser.CutTill(srchstring, "(Approx Matches)</b>");
                String id;
                while (srchstring.indexOf("<td valign=\"top\"><a href=\"/title/") > -1) {
                    id = Parser.CutTill(srchstring, "</td></tr>");
                    id = Parser.CutFrom(id, "<td valign=\"top\"><a href=\"/title/");
                    title = Parser.CutFrom(id, "?link=/title/");
                    title = Parser.CutFrom(title, ";\">");
                    title = Parser.CutTags(title);
                    id = Parser.CutTill(id, "/\"");
                    result += "|" + title;
                    keys += "|" + id;
                    srchstring = Parser.CutFrom(srchstring, "</td></tr>");
                }
                Information.put("##search-results", result);
                Information.put("##search-keys", keys);
            }
        }
        if (Information.containsKey("##search-selection")) {
            String result=from_HTML(imdb_req("/title/"+Information.get("##search-selection")+"/"));
            Information.put("imdb-id",Information.get("##search-selection"));
            Information.remove("##search-selection");
            String cont;
            cont=Parser.CutTill(Parser.CutFrom(result, "<h1>"),"<").trim();
            cont=from_HTML(cont);
            if (cont.startsWith("\"")) cont=cont.substring(1);
            if (cont.endsWith("\"")) cont=Parser.CutTillLast(cont,"\"");
            Information.put("show", from_HTML(cont));
            cont=Parser.CutFrom(Parser.CutTill(Parser.CutFrom(result,"<div class=\"starbar-meta\">"),"</b>"),"<b>");
            Information.put("rating", cont);
            cont=Parser.CutTill(Parser.CutFrom(result, "<a href=\"/year/"),"/").trim();
            Information.put("year", cont);
            cont=div_class(result,"Release Date");
            cont=Parser.CutTill(cont,"<").trim();
            Information.put("release date",cont);
            cont=div_class(result,"Genre");
            cont=to_genre(cont);
            Information.put("autoregistered",cont);
            Information.put("genre",cont);
            cont=div_class(result,"Plot");
            cont=Parser.CutTill(cont,"<a").trim();
            Information.put("plot",cont);
            cont=Parser.CutTill(Parser.CutFrom(result, "<table class=\"cast\">"),"</div> </div>").trim();
            cont=to_people2(cont);
            Information.put("cast",cont);
        }
    }
}

/*
 * PluginArXiv.java
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
public class PluginLookAtIMDB extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look at IMDB");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for a record corresponding to the title at the Internet Movie Database.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"title");
            put("type"              ,"interactive");
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
        System.out.println("In...");
        ret=Parser.CutTags(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(ret,s2+":"),"</h4>"),"</div>")).trim();
        System.out.println("in...");
        ret=Parser.Substitute(ret,"See more","");
        ret=Parser.Substitute(ret,"&raquo;","");
        System.out.println("out...");
        return(ret.trim());
    }

    private String to_people(String s1) {
        String ret=new String("");
        String tmp;
        while (s1.indexOf("</a>")>-1) {
            tmp=Parser.CutTill(s1, "</a>");
            tmp=Parser.CutFromLast(tmp,">").trim();
            if (!tmp.startsWith("and"))
                ret+="|"+Parser.CutFromLast(tmp, " ")+", "+Parser.CutTillLast(tmp, " ");
            s1=Parser.CutFrom(s1,"</a>");
        }
        if (ret.length()==0) return(ret);
        return(ret.substring(1));
    }

    private String to_people2(String s1) {
        String ret=new String("");
        String tmp;
        while (s1.indexOf("href=\"/name/nm") > -1) {
            s1=Parser.CutFrom(Parser.CutFrom(Parser.CutFrom(s1, "href=\"/name/nm"), "href=\"/name/nm"),">");
            tmp = Parser.CutTill(s1, "</a>");
            ret += "|" + Parser.CutFromLast(tmp, " ") + ", " + Parser.CutTillLast(tmp, " ");
        }
        if (ret.length()==0) return(ret);
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
        if (Information.containsKey("##search-selection")) {
            String result=from_HTML(imdb_req("/title/"+Information.get("##search-selection")+"/"));
            //Information.put("IMDB-res",result);
            Information.put("imdb-id",Information.get("##search-selection"));
            Information.remove("##search-selection");
            String cont;
            cont=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(result, "<h1"),">"),"<").trim();
            Information.put("title", from_HTML(cont));
            cont=Parser.CutTill(Parser.CutFrom(result,"\"ratingValue\">"),"</span>");
            Information.put("rating", cont);
            cont=Parser.CutTill(Parser.CutFrom(result, "<a href=\"/year/"),"/").trim();
            Information.put("year", cont);
            System.out.println("0");
            cont=Parser.CutTill(Parser.CutFrom(result,"Director:"),"<h4");
            System.out.println("1::"+cont);
            cont=to_people(cont);
            System.out.println("2");
            Information.put("director", cont);
            System.out.println("3");
            cont=Parser.CutTill(Parser.CutFrom(result,"Writer:"),"<h4");
            System.out.println("4");
            cont=to_people(cont);
            Information.put("writers", cont);
            System.out.println("5");
            cont=div_class(result,"Release Date");
            //cont=Parser.CutTill(cont,"<").trim();
            Information.put("release date",cont);
            System.out.println("6");
            cont=Parser.CutTill(Parser.CutFrom(result, "<h4 class=\"inline\">Genres:</h4>"),"</div>");
            cont=to_genre(cont);
            System.out.println("7");
            Information.put("autoregistered",cont);
            Information.put("genre",cont);
            System.out.println("8");
            cont=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(result,"itemprop=\"description\""),">"),"</p>");
            Information.put("plot",cont);
            System.out.println("9");
            cont=Parser.CutTill(Parser.CutFrom(result, "<h4 class=\"inline\">Runtime:</h4> "),"</div>");
            Information.put("runtime",cont);
            cont=Parser.CutTill(Parser.CutFrom(result,"<table class=\"cast_list\">"),"</table>").trim();
            cont=to_people2(cont);
            Information.put("cast",cont);
            String target=Information.get("fullpath")+".jpg";
            Information.put("thumbnail", "/$" + target + "/$.jpg");
            Information.put("people",Information.get("director")+"|"+Information.get("cast"));
            cont=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(result,"<td rowspan=\"2\" id=\"img_primary\">"),"<img src=\""),"\"");
            System.out.println("pic");
            System.out.println(cont);
            System.out.println(target);
            TextFile.ReadOutURLToFile(cont, target);
        } else {
            String title = Information.get("title");
            if ((title==null) || (title.equals("<unknown>"))) {
                toolbox.Warning(null,"Please enter at least parts of the title of the movie.", "Could not search at IMDB");
                return;
            }
            String result = new String("");
            String keys = new String("");
            Msgs.add(TI + "Looking for: " + title + " at IMDB");

            String srchurl="/find?s=tt&q="+Parser.Substitute(title, " ", "+");
            Msgs.add(TI + "Search request: "+srchurl);
            String srchstring = from_HTML(imdb_req(srchurl));
            if (srchstring.startsWith("HTTP/1.1 302 Found")) {
                Information.put("##search-results", "Unique result");
                Information.put("##search-keys", Parser.CutTill(Parser.CutFrom(srchstring, "Location: http://www.imdb.com/title/"),"/"));
            } else {
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
    }
}

/*
 * PluginAmazon.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cnsaeman
 */
public class PluginAmazon extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look at Amazon");
            put("author"            ,"Christian Saemann");
            put("version"           ,"2.5");
            put("help"              ,"This plugin tries to find a record at Amazon.com and downloads the thumbnail. It is also used as the barcode plugin.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"title");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public String amazon_req(String request) {
        System.out.println("Amazon request: "+request);
        if (request.startsWith("http://www.amazon.com/")) request=Parser.CutFrom(request,"http://www.amazon.com");
        StringBuffer output = new StringBuffer("");
        try {
            Socket server = new Socket("www.amazon.com", 80);
            //server.set
            OutputStream out = server.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));

            //GET-Kommando senden
            String req = "GET "+request+" HTTP/1.0\r\n";
            req += "Host: www.amazon.com\r\n";
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
            //System.out.println(output.toString());
            //TextFile t=new TextFile("amazon.txt",false); t.putString(output.toString()); t.close();
        } catch (Exception e) {
            System.out.println("Error!");
            e.printStackTrace();
        }
        return (output.toString());
    }
  
    public void run() {
        if (Information.containsKey("##search-selection")) {
            boolean afterwards=false;
            String target;
            if (Information.isEmpty("fullpath")) {
                if (Information.isEmpty("barcode")) {
                    target=Parser.CutProhibitedChars(Information.getS("title")+".jpg");
                } else {
                    target=Parser.CutProhibitedChars(Information.getS("barcode"))+".jpg";
                }
            } else {
              target=Information.get("fullpath")+".jpg";
            }
            String url=Information.get("##search-selection");
	    if (Information.getS("type").equals("")) Information.put("type", "Book");
            String page = amazon_req(url);
            Information.put("amazon-link", url);
            String authors=new String("");
            /*TextFile TF;
            try {
                TF = new TextFile("dump.txt",false);
            TF.putString(page);
            TF.close();
            } catch (IOException ex) {
                Logger.getLogger(PluginAmazon.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            String title=Parser.CutTill(Parser.CutFrom(page, "<span id=\"btAsinTitle\" >"),"<").trim();
            Information.put("title",title);
            int i=page.indexOf("(Author)");
            int j,k;
            while (i>-1) {
                j=page.lastIndexOf("</a>",i);
                k=page.lastIndexOf(">",j);
                String aut=page.substring(k+1, j);
                aut=Parser.CutFromLast(aut," ")+", "+Parser.CutTillLast(aut," ");
                authors+="|"+aut;
                i=page.indexOf("(Author)",i+1);
            }
            Information.put("authors",authors.substring(1));

            if (page.startsWith("##??")) {
                Msgs.add("Error: " + page);
            } else {
                Information.put("thumbnail", "/$" + target + "/$.jpg");
                TextFile.ReadOutURLToFile(imageURL(page), target);
            }
            if (page.indexOf("<h2>Book Description</h2>") > -1) {
                String rem = Parser.CutTill(Parser.CutFrom(Parser.CutFrom(page, "<h2>Book Description</h2>"),"<div class=\"content\">"), "</div>");
                //System.out.println("remarks::" + Parser.CutTags(rem));
                Information.put("remarks", Parser.CutTags(rem).trim());
            }
            if (page.indexOf("<h2>Product Details</h2>") > -1) {
                String rem = Parser.CutTill(Parser.CutFrom(page, "<h2>Product Details</h2>"), "Product Dimensions:");
                String tag, val;
                while (rem.indexOf("</li>") > -1) {
                    tag = Parser.CutTill(Parser.CutFrom(rem, "<li><b>"), "</li>");
                    if (tag.indexOf("\n") > -1) {
                        break;
                    }
                    val = Parser.CutFrom(tag, "</b>").trim();
                    tag = Parser.CutTill(tag, ":").trim();
                    if ((tag.equals("")) || (val.equals(""))) {
                        break;
                    }
                    //System.out.println(tag + "::" + val);
                    Information.put(Parser.CutTags(tag).toLowerCase(), Parser.CutTags(val));
                    rem = Parser.CutFrom(rem, "</li>");
                }
                if ((Information.containsKey("isbn-10")) && (Information.containsKey("publisher")) && (!Information.containsKey("bibtex"))) {
                    celsius.BibTeXRecord btr = new celsius.BibTeXRecord();
                    btr.tag = Information.get("isbn-10");
                    btr.remove("year");
                    btr.remove("volume");
                    btr.remove("journal");
                    btr.remove("pages");
                    btr.put("title", Information.get("title"));
                    btr.put("author", toolbox.ToBibTeXAuthors(Information.get("authors")));
                    btr.put("note", Information.get("publisher"));
                    Information.put("bibtex", btr.toString());
                    Information.put("citation-tag", btr.tag);
                    //System.out.println("bibtex::" + btr.toString());
                }
                Information.put("identifier", toolbox.Identifier(Information));
            }
        } else {
            String results = new String("");
            String keys = new String("");
            String title = Parser.Substitute(Information.get("title"), ",", "%2C");
            if (!Information.isEmpty("authors")) title+=" "+Parser.Substitute(toolbox.shortenNames(Information.getS("authors")),", "," ");
            String author,url;
            title = Parser.Substitute(title, " ", "+");
            if (Information.getS("isbn").length()>0) {
                title=Parser.Substitute(Information.getS("isbn"),"-","");
            }
            Msgs.add("URL used: "+"/s/ref=nb_ss_gw/102-9769508-3048904?url=search-alias%3Daps&field-keywords=" + title + "&Go.x=0&Go.y=0&Go=Go");
            String tmp = amazon_req("/s/ref=nb_ss_gw/102-9769508-3048904?url=search-alias%3Daps&field-keywords=" + title + "&Go.x=0&Go.y=0&Go=Go");
            String toDo=Parser.CutTill(Parser.CutFrom(tmp,"<div id=\"atfResults"),"<script ");
            while (toDo.indexOf("<div id=\"result")>-1) {
                            String toDo2=Parser.CutTill(Parser.CutFrom(toDo,"<h3 class=\"newaps"),"</h3>");
                            //System.out.println(toDo);
                            title=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(toDo2,"<span"),">"),"</span>").trim();
                            System.out.println("ATF-found::"+title);
                            Msgs.add(title);
                            author = Parser.CutTags(Parser.CutTill(Parser.CutFrom(toDo2, "by"),"</a>"));
                            url = Parser.CutFrom(toDo2, "href=\"http://www.amazon");
                            url = Parser.CutFrom(url, "/");
                            url = "/" + Parser.CutTill(url, "\">");
                            results += "|<html><b>" + title + "</b><br/>" + author + "</html>";
                            keys += "|" + url;
                            tmp=Parser.CutFrom(tmp,"<div id=\"result");
                            Msgs.add("out...");
	      toDo=Parser.CutFrom(toDo,"<div id=\"result");
            }
            
            /*int j = tmp.indexOf("<table class=\"searchresults\"");
            int k,l,m,n;
            if (j>-1) {
                j=tmp.indexOf("<table class=\"n2\"");
                try {
                while (j>0) {
                    j = tmp.indexOf("<span class=\"srTitle\">",j);
                    k = tmp.indexOf("</span", j);
                    l = tmp.lastIndexOf("a href", j);
                    m = tmp.indexOf("</a>", j);
                    n = tmp.indexOf("<span class=\"bindingBlock\">", m);
                    title = tmp.substring(j + 22, k);
                    System.out.println("found::"+title);
                    author = Parser.CutTags(tmp.substring(m + 4, n)).trim();
                    url = tmp.substring(l + 8, j - 3);
                    url = Parser.CutTill(url,"\"");
                    results+="|<html><b>"+title+"</b><br/>"+author+"</html>";
                    keys+="|"+url;
                    j=tmp.indexOf("<table class=\"n2\"",j+1);
                }
                } catch (Exception e) {
                    Msgs.add("Error in search results!");
                }
            } else {
            Msgs.add("ATF-result");
            //System.out.println("A");
                j = tmp.indexOf("<div id=\"atfResults\"");
                Msgs.add(Integer.toString(j));
                j=tmp.indexOf("<div class=\"data\">",j);
                if (j==-1) {
                    Msgs.add(Integer.toString(j));
                    j=tmp.indexOf("<div id=\"result",0);
                    try {
                        while (tmp.length() > 0) {
                            Msgs.add("in...");
                            String toDo=Parser.CutTill(Parser.CutFrom(tmp,"<h3 class=\"newaps"),"</h3>");
                            //System.out.println(toDo);
                            title=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(toDo,"<span"),">"),"</span>").trim();
                            System.out.println("ATF-found::"+title);
                            Msgs.add(title);
                            author = Parser.CutTags(Parser.CutTill(Parser.CutFrom(toDo, "by"),"</a>"));
                            url = Parser.CutFrom(toDo, "href=\"http://www.amazon");
                            url = Parser.CutFrom(url, "/");
                            url = "/" + Parser.CutTill(url, "\">");
                            results += "|<html><b>" + title + "</b><br/>" + author + "</html>";
                            keys += "|" + url;
                            tmp=Parser.CutFrom(tmp,"<div id=\"result");
                            Msgs.add("out...");
                        }
                    } catch (Exception e) {
                        Msgs.add("Error in search results! "+e.toString());
                    }
                } else {
                    Msgs.add(Integer.toString(j));
                    tmp = Parser.CutTill(tmp, "<div class=\"fkmrResults first\">");
                    Msgs.add("Nos done");
                    try {
                        while (j>0) {
                            Msgs.add("in...");
                            j = tmp.indexOf("<a class=\"title\"", j);
                            k = tmp.indexOf("</div>", j);
                            title = tmp.substring(j, k);
                            System.out.println("ATF2-found::"+title);
                            Msgs.add(title);
                            author = Parser.CutFrom(title, "<span");
                            author = Parser.CutTags(Parser.CutTill(Parser.CutFrom(author, ">"), "</span").trim());
                            url = Parser.CutFrom(title, "href=\"http://www.amazon");
                            url = Parser.CutFrom(url, "/");
                            url = "/" + Parser.CutTill(url, "\">");
                            title = Parser.CutTill(Parser.CutFrom(title, ">"), "<").trim();
                            results += "|<html><b>" + title + "</b><br/>" + author + "</html>";
                            keys += "|" + url;
                            tmp=Parser.CutFrom(tmp,"<div id=\"result");
                            j = tmp.indexOf("<div class=\"data\">", j);
                            Msgs.add("out...");
                        }
                    } catch (Exception e) {
                        Msgs.add("Error in search results!");
                    }
                }
            }*/
            Msgs.add(results);
            Information.put("##search-results", results);
            Msgs.add(keys);
            Information.put("##search-keys", keys);
        }
    }
  
    private String imageURL(String tmp) {
        int i = tmp.indexOf("id=\"prodImage\"");
        int j = tmp.lastIndexOf("src=", i);
        int p = tmp.indexOf("Book Description");
        if (p > -1) {
            p = tmp.indexOf("/>", p) + 2;
            int q = tmp.indexOf("<", p + 15);
            String com = tmp.substring(p, q).trim();
            if (com.startsWith("<"))
                com = Parser.CutFrom(com, ">");
            String rem=Information.get("remarks");
	    if (rem==null) Information.put("remarks",com);
            else Information.put("remarks",rem+"\n\n"+com);
        }
        if (j==-1) {
            i = tmp.indexOf("id=\"prodImageCell\"");
            j = tmp.indexOf("src=", i);
            i = tmp.indexOf("\"",j+5)+2;
        }
        System.out.println("URL::"+tmp.substring(j + 5, i - 2));
        return (tmp.substring(j + 5, i - 2));
    }
                      
}

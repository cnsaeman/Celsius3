/*
 * PluginAmazon.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.net.URLEncoder;
/**
 * @author cnsaeman
 */
public class PluginAmazonAudio extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look at Amazon/Music");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin tries to find an album at Amazon.com and downloads the thumbnail.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"album");
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
        if (request.startsWith("http://www.amazon.com/")) request=Parser.CutFrom(request,"http://www.amazon.com");
        StringBuffer output = new StringBuffer("");
        try {
            Socket server = new Socket("www.amazon.com", 80);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (output.toString());
    }
  
    public void run() {
        System.out.println("HERE!");
        if (Information.containsKey("##search-selection")) {
            boolean afterwards=false;
            String target=Information.get("fullpath")+".jpg";
            String url=Information.get("##search-selection");
            String page = amazon_req(url);
           /*try {
            TextFile TS=new TextFile("dump.txt",false);
            TS.putString(page);
            TS.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }*/
            Information.put("amazon-link", url);
            if (page.startsWith("##??")) {
                Msgs.add("Error: " + page);
            } else {
                Information.put("thumbnail", "/$" + target + "/$.jpg");
                TextFile.ReadOutURLToFile(imageURL(page), target);
            }
        } else {
            String results = new String("");
            String keys = new String("");
            String search=Information.get("album");
            if (!Information.isEmpty("composer")) search+=" "+toolbox.shortenNames(Information.get("composer"));
            if (!Information.isEmpty("artists")) search+=" "+Parser.CutTill(Information.get("artists"),",");
            try {
                search=Parser.Substitute(search, "&", "");
                search=URLEncoder.encode(search, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            System.out.println("/s/ref=nb_sb_noss?url=search-alias=popular&field-keywords=" + search + "&x=0&y=0");
            String tmp = amazon_req("/s/ref=nb_sb_noss?url=search-alias=popular&field-keywords=" + search + "&x=0&y=0");
            String part="<div class=\"title\"";
            if (tmp.indexOf("<div id=\"title")>-1) {
                part="<div id=\"title";
            }
            if (tmp.indexOf("<table class=\"searchresults\"")>-1) {
                part="<table class=\"n2\" ";
            }
            /*try {
            TextFile TS=new TextFile("dump.txt",false);
            TS.putString(tmp);
            TS.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }*/
            while (tmp.indexOf(part)>0) {
                tmp=Parser.CutFrom(Parser.CutFrom(tmp,part),">");
                String res=Parser.CutTags(Parser.CutTill(tmp,"</div>").trim());
                String url=Parser.CutTill(Parser.CutFrom(tmp, "href=\""),"\"");
                results+="|"+res;
                keys+="|"+url;
            }
            Information.put("##search-results", celsius.tools.Parser.decodeHTML(results));
            Information.put("##search-keys", keys);
        }
    }
  
    private String imageURL(String tmp) {
        int i = tmp.indexOf("td id=\"prodImageCell\"");
        tmp= tmp.substring(i);
        return(celsius.tools.Parser.CutTill(celsius.tools.Parser.CutFrom(tmp, "src=\""),"\""));
    }
                      
}

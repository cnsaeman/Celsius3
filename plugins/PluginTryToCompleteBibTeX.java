/*
 * PluginUpdateComplete.java
 *
 * Created on 05. September 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;
import java.io.*;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import celsius.tools.*;

/**
 * @author cnsaeman
 */
public class PluginTryToCompleteBibTeX extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Try To Complete BibTeX");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin tries to get more BibTeX information, if none was found.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"manual");
            put("defaultParameters" ,"http://inspirehep.net/");
            put("parameter-help"    ,"Link to the Inspires mirror to be used.");
        }
    };
    
    public static String Condense(String pre) {
        StringBuffer out=new StringBuffer(pre.toLowerCase());
        int i=0;
        while (i<out.length()) {
            if (!Character.isLetter(out.charAt(i))) out.deleteCharAt(i);
            else i++;
        }
        return(out.toString().toLowerCase());
    }


    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String inspirebase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public String lineUnder(String in, String s) {
      String out = Parser.CutFrom(in,s);
      out=Parser.CutTill(out,"<http://").trim();
      out=Parser.CutTillLast(Parser.CutFrom(out,"\""),"\"");
      return out;
    }    
    
    public void run() {
        String bib=Information.get("bibtex");
        celsius.BibTeXRecord BTR;
        Boolean modified=false;
        
        if (bib==null) {
            // no BibTeX information whatsoever
            modified=true;
            BTR=new celsius.BibTeXRecord();
            String tag=Parser.CutTill(Parser.CutTill(Information.get("authors")," "),",").trim();
            if (Information.get("arxiv-number")!=null) {
               tag+=":"+Information.get("arxiv-number");
            } else {
               tag+=":"+Parser.CutTill(Information.get("title")," "); 
            }
            BTR.tag=tag;
            BTR.put("title",Information.get("title"));
            BTR.put("author",toolbox.ToBibTeXAuthors(Information.get("authors")));
            if (Information.get("type").equals("Book")) {
                BTR.type="book";
            } else {
                BTR.type="article";
            }
        } else {
            BTR=new celsius.BibTeXRecord(bib);
        }
        
        if ((Information.get("arxiv-ref")!=null) && (BTR.get("eprint")==null)) {
            modified=true;
            BTR.put("eprint",Information.get("arxiv-ref"));
            BTR.put("archiveprefix","arXiv");
            BTR.put("primaryclass",Information.get("arxiv-name"));
            if (Information.get("arxiv-ref").length()==9) {
                BTR.put("primaryclass",Information.get("arxiv-name"));
            }
        }
        
        if ((Information.get("doi")==null) && (Information.get("title").length()>10)) {
            String title=Parser.Substitute(Parser.Substitute(Parser.Substitute(Parser.CutTill(Parser.CutTill(Information.get("authors")," "),",")+" "+Information.get("title")," ","+"),".",""),":","");
            String crossrefSearch=TextFile.ReadOutURL("https://api.crossref.org/works?query="+title+"&rows=1");
            System.out.println("reply: "+crossrefSearch);
            String retTitle=Parser.CutFrom(Parser.CutTill(Parser.CutFrom(crossrefSearch,"\"title\":"),"\"]"),"[\"");
            System.out.println(retTitle+"::"+title+"::"+Condense(title)+"::"+Condense(retTitle));
            if (Condense(title).equals(Condense(retTitle))) {
                String doi=Parser.Substitute(Parser.CutTill(Parser.CutFrom(Parser.CutFrom(crossrefSearch,"\"DOI\":"),"\""),"\""),"\\/","/");
                System.out.println(doi);
                if (doi.length()>4) Information.put("doi",doi);
            }
        }
        
        if ((Information.get("doi")!=null) && (BTR.get("doi")==null)) {
            modified=true;
            BTR.put("doi",Information.get("doi"));
            
            String dataPage;
            StringBuffer rtn = new StringBuffer(4000);
            try {
              URL url=new URL("http://dx.doi.org/"+Information.get("doi"));
              URLConnection urlconn=url.openConnection();
              urlconn.setReadTimeout(10000);
              urlconn.setConnectTimeout(10000);
              urlconn.setRequestProperty("Accept","text/turtle");
              InputStream in=urlconn.getInputStream();
              
              byte[] buffer=new byte[4096];
              int bytes_read;
              while ((bytes_read=in.read(buffer))!=-1) {
                  rtn.append(new String(buffer,0,bytes_read));
              }
              in.close();
              
              dataPage=rtn.toString();
              
              if (dataPage.indexOf("Handle Redirect")>-1) {
                dataPage=Parser.CutFrom(dataPage,"\">");
                url=new URL(Parser.CutTill(dataPage,"</a>"));
                urlconn=url.openConnection();
                urlconn.setReadTimeout(10000);
                urlconn.setConnectTimeout(10000);
                urlconn.setRequestProperty("Accept","text/turtle");
                in=urlconn.getInputStream();
                
                while ((bytes_read=in.read(buffer))!=-1) {
                    rtn.append(new String(buffer,0,bytes_read));
                }
                in.close();
              }
              
              TextFile tf=new TextFile("Plugin.LookUpDoi.ReturnedData.txt.tmp",false);
              tf.putString(rtn.toString());
              tf.close();
            } catch (Exception e) {
                rtn = new StringBuffer("##??" + e.toString());
            }
            
            dataPage = rtn.toString();
            String dataPaper = Parser.CutFrom(dataPage,"<http://dx.doi.org");
            BTR.put("year",Parser.CutTill(lineUnder(dataPaper,"<http://purl.org/dc/terms/date>"),"-"));
            BTR.put("volume",lineUnder(dataPaper,"<http://purl.org/ontology/bibo/volume>"));
            BTR.put("pages",lineUnder(dataPaper,"<http://purl.org/ontology/bibo/pageStart>")+"-"+lineUnder(dataPaper,"<http://purl.org/ontology/bibo/pageEnd>"));
            BTR.put("journal",lineUnder(Parser.CutFrom(dataPage,"\n<http://id.crossref.org/issn"),"<http://purl.org/dc/terms/title>"));
        }
        
        if ((!BTR.isEmpty("journal")) && (Information.get("type").equals("Preprint"))) {
            Information.put("type","Paper");
        }
        
        if (modified) {
            Information.put("bibtex", BTR.toString());
            Information.put("citation-tag", BTR.tag);
        }
    }
}

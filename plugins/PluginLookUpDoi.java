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
import java.net.*;
import java.io.*;


/**
 * @author cnsaeman
 */
public class PluginLookUpDoi extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Look up DOI");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.2");
            put("help"              ,"This plugin looks up publication information from the DOI.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"doi");
            put("type"              ,"interactive");
        }
    };

    private final String TI = "P:LaS>";
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    private String srchstring,tmp,abs;
    private String title;

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
        String doi = Information.get("doi");
        StringBuffer rtn=new StringBuffer(4000);
        String dataPage;
        try {
            URL url=new URL("http://dx.doi.org/"+doi);
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
            
        } catch (Exception e) { rtn=new StringBuffer("##??"+e.toString()); }
        dataPage=rtn.toString();
        try {
            TextFile tf=new TextFile("Plugin.LookUpDoi.ReturnedData.txt.tmp",false);
            tf.putString(dataPage);
            tf.close();
        } catch (Exception e) { System.out.println(e.toString()); }
        String dataPaper = Parser.CutFrom(dataPage,"<http://dx.doi.org");
        String title=lineUnder(dataPaper,"<http://purl.org/dc/terms/title>");
        String year=Parser.CutTill(lineUnder(dataPaper,"<http://purl.org/dc/terms/date>"),"-");
        String volume=lineUnder(dataPaper,"<http://purl.org/ontology/bibo/volume>");
        String page=lineUnder(dataPaper,"<http://purl.org/ontology/bibo/pageStart>")+"-"+lineUnder(dataPaper,"<http://purl.org/ontology/bibo/pageEnd>");
        String authors="";
        String res3=dataPage;
        while (res3.indexOf("<http://xmlns.com/foaf/0.1/name>")>-1) {
            String author = lineUnder(res3,"<http://xmlns.com/foaf/0.1/name>");
            res3=Parser.CutFrom(res3, "<http://xmlns.com/foaf/0.1/name>");
            authors+="|"+Parser.CutFromLast(author," ").trim()+", "+Parser.CutTillLast(author," ");
        }
        authors=authors.substring(1);
        String journal=lineUnder(Parser.CutFrom(dataPage,"<http://purl.org/ontology/bibo/Journal>"),"<http://purl.org/dc/terms/title>");
        Information.put("title",title);
        Information.put("authors",authors);
              String tag=Parser.CutTill(authors, ",").trim() + ":" + year + ":" + page;
              Information.put("citation-tag",tag);
              String bibtex = new String("@Article{" + tag + ",\n"
                          + "   author = \"" + toolbox.ToBibTeXAuthors(authors) + "\",\n"+
              "   title = \""+title+"\",\n"+
              "   journal = \""+journal+"\",\n"+
              "   volume = \""+volume+"\",\n"+
              "   year = \""+year+"\",\n"+
              "   pages = \""+page+"\",\n"+
              "   doi = \""+doi+"\"\n"+
              "}");
              bibtex=bibtex.replaceAll("\\s+volume\\s+\\= \\\"\\\",","");
              celsius.BibTeXRecord BTR = new celsius.BibTeXRecord(bibtex);
              Information.put("bibtex", BTR.toString());
              Information.put("type","Paper");
              String ident = toolbox.Identifier(Information);
        if ((ident==null) || (ident.length()<3)) {
          Information.remove("identifier");
        } else {
          Information.put("identifier", ident.trim());
        }
    }
}

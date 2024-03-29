/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package celsius.Threads;

import celsius.BibTeXRecord;
import celsius.Item;
import celsius.Library;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JProgressBar;

/**
 *
 * @author cnsaeman
 */
public class ThreadImport extends Thread {

    private final String filename;
    private final int mode;
    private final ArrayList<Item> docs;
    private final DefaultListModel DLM;
    private final Library Lib;
    private final JList jL;
    private final JProgressBar jPB;
    private final HashMap<String,String> dictionary;

    public ThreadImport(String fn,int m, ArrayList<Item> d, Library l, JList jl, JProgressBar jpb) {
        filename=fn; mode=m;
        docs=d;
        Lib=l;
        DLM=new DefaultListModel();
        docs.clear();
        jL=jl;
        jPB=jpb;
        dictionary=new HashMap<String,String>();
    }

    @Override
    public synchronized void run() {
        System.out.println("Thread import started...");
        if (mode==0) bibimp();
        if (mode==1) gpximp();
        jL.setModel(DLM);
        jPB.setIndeterminate(false);
        System.out.println("Thread import ended...");
    }

    private Item createDoc() {
        Item doc = new Item();
        for (String tag : Lib.IndexTags) {
            if (!tag.equals("addinfo") && !tag.equals("autoregistered") && !tag.equals("registered") && !tag.equals("id")) {
                doc.put(tag, null);
            }
        }
        return (doc);
    }

    private void bibimp() {
        try {
            TextFile TF=new TextFile(filename);
            StringBuilder SB=new StringBuilder();
            while (TF.ready()) {
                String line=TF.getString();
                if (line.trim().startsWith("@")) line=line.trim();
                SB.append("\n").append(line);
            }                
            TF.close();
            String cont=SB.toString();
            String[] bibs=cont.split("\\n@");
            for (int i=1; i<bibs.length; i++) {
                BibTeXRecord bibtex = new BibTeXRecord("@"+bibs[i]);
                if (bibtex.parseError == 0) {
                    Item doc = createDoc();
                    String bibtitle=bibtex.get("title");
                    if (bibtitle.startsWith("{")) {
                        bibtitle = bibtitle.substring(1, bibtitle.length() - 1);
                    }
                    doc.put("title", bibtitle);
                    doc.put("authors", BibTeXRecord.authorsBibTeX2Cel(bibtex.get("author")));
                    doc.put("citation-tag",bibtex.tag);
                    doc.put("identifier",bibtex.getIdentifier());
                    doc.put("bibtex",bibtex.toString());
                    docs.add(doc);
                    DLM.addElement(bibtex.tag+": "+bibtitle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cut(String s,String k) {
        if ((s.indexOf("<"+k+" />")>-1) || (s.indexOf("<"+k+"/>")>-1)) return("");
        String t=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(s, "<"+k),">"),"</"+k+">");
        t=t.replaceAll(Pattern.quote("&amp;"),"&");
        return(t);
    }

    private void write(Item doc, String k,String value) {
        if (dictionary.containsKey(value)) {
            doc.put(k, dictionary.get(value));
        } else {
            doc.put(k,value);
        }
    }

    private void gpximp() {
        try{
            StringBuffer[] SB=new StringBuffer[2];
            ZipFile ZF = new ZipFile(filename);
            String main;
            String waypoints;
            int i;
            for (Enumeration<ZipEntry> e=(Enumeration<ZipEntry>) ZF.entries();e.hasMoreElements();) {
                ZipEntry ze=e.nextElement();
                i=0;
                if (ze.getName().indexOf("-wpts")>-1) i=1;
                SB[i]=new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(ZF.getInputStream(ze),"UTF8"));
                while (br.ready())
                    SB[i].append("\n").append(br.readLine());
                br.close();
                i++;
            }
            ZF.close();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String date=df.format((new Date((new File(filename)).lastModified())));
            String cont=SB[0].toString();
            String[] wpts=cont.split("<wpt");
            //Pattern p=Pattern.compile("lat=\\\"(.+?)\\\" lon=\\\"(.+?)\\\".+?<name>(.+?)</name>.+?<desc>(.+?)</desc>.+?");
            Pattern p1=Pattern.compile("lat=\"(.*?)\".*?lon=\"(.*?)\"");
            for (i=1; i<wpts.length; i++) {
                Matcher m1=p1.matcher(wpts[i]);
                if (m1.find()) {
                    Item doc=createDoc();
                    doc.put("lat",m1.group(1));
                    doc.put("lon",m1.group(2));
                    doc.put("cache-id",cut(wpts[i],"name"));
                    doc.put("placedby",cut(wpts[i],"groundspeak:placed_by"));
                    doc.put("owner",cut(wpts[i],"groundspeak:owner"));
                    doc.put("name",cut(wpts[i],"desc"));
                    doc.put("type",cut(wpts[i],"groundspeak:type"));
                    write(doc,"size",cut(wpts[i],"groundspeak:container"));
                    doc.put("difficulty",cut(wpts[i],"groundspeak:difficulty"));
                    doc.put("gpx-date",date);
                    doc.put("placed",Parser.CutTill(cut(wpts[i],"time"),"T"));
                    doc.put("terrain",cut(wpts[i],"groundspeak:terrain"));
                    doc.put("description",Parser.decodeHTML(cut(wpts[i],"groundspeak:short_description")));
                    String longdesc=Parser.decodeHTML(cut(wpts[i],"groundspeak:long_description"));
                    doc.put("long-description",Parser.CutTill(longdesc,"<p>Additional Waypoints</p>"));
                    longdesc=Parser.CutFrom(longdesc,"<p>Additional Waypoints</p>").trim();
                    if (longdesc.length()>5) {
                        String addwaypoints="";
                        String links="";
                        while (longdesc.length()>5) {
                            links+="|waypoints:cache-id:"+Parser.CutTill(longdesc, "-").trim();
                            for (int k=0;k<3;k++) {
                                addwaypoints+=Parser.CutTill(longdesc,"<br />")+"<br />";
                                longdesc=Parser.CutFrom(longdesc,"<br />");
                            }
                        }
                        doc.put("additional-waypoints",addwaypoints);
                        doc.put("links",links.substring(1));
                    }
                    doc.put("encoded-hints",cut(wpts[i],"groundspeak:encoded_hints"));
                    doc.put("url",cut(wpts[i],"url"));
                    doc.put("url-name",cut(wpts[i],"urlname"));
                    doc.put("found",cut(wpts[i],"sym"));
                    String lastfound=Parser.CutTillLast(wpts[i],"<groundspeak:type>Found it</groundspeak:type>");
                    if (lastfound.equals(wpts[i])) {
                        doc.put("last-found","never");
                    } else {
                        lastfound=Parser.CutFromLast(wpts[i],"<groundspeak:log");
                        doc.put("last-found",Parser.CutTill(cut(lastfound,"groundspeak:date"),"T"));
                    }
                    int j=wpts[i].indexOf("<groundspeak:log");
                    String lastfour=wpts[i].substring(j);
                    //System.out.println(Parser.HowOftenContains(lastfour,"<groundspeak:log"));
                    String lastresults="";
                    while (lastfour.length()>0) {
                        String res=cut(lastfour,"groundspeak:type");
                        if (res.equals("Found it")) lastresults+="F";
                        if (res.equals("Didn't find it")) lastresults+="X";
                        if (res.equals("Write note")) lastresults+="W";
                        if (res.equals("Enable Listing")) lastresults+="E";
                        if (res.equals("Owner Maintenance")) lastresults+="M";
                        if (res.equals("Temporarily Disable Listing")) lastresults+="D";
                        lastfour=Parser.CutFrom(lastfour,"</groundspeak:type");
                    }
                    doc.put("last-logs", lastresults);
                    String logs="<ul>";
                    String gslogs=cut(wpts[i],"groundspeak:logs").trim();
                    while (gslogs.length()>0) {
                        String log=cut(gslogs,"groundspeak:log");
                        logs+="<li>"+cut(log,"groundspeak:date")+" : "+cut(log,"groundspeak:type");
                        logs+=" by "+cut(log,"groundspeak:finder")+" <br />";
                        logs+=cut(log,"groundspeak:text")+"</li>";
                        gslogs=Parser.CutFrom(gslogs, "</groundspeak:log>");
                    }
                    logs+="</ul>";
                    if (logs.length()>10) doc.put("logs",logs);
                    doc.put("original-xml","  <wpt "+wpts[i]);
                    docs.add(doc);
                    DLM.addElement(doc.get("cache-id")+": "+doc.get("name"));
                } else {
                    System.out.println("Not found!");
                }
            }
            cont=SB[1].toString();
            wpts=cont.split("<wpt");
            //Pattern p=Pattern.compile("lat=\\\"(.+?)\\\" lon=\\\"(.+?)\\\".+?<name>(.+?)</name>.+?<desc>(.+?)</desc>.+?");
            for (i=1; i<wpts.length; i++) {
                Matcher m1=p1.matcher(wpts[i]);
                if (m1.find()) {
                    Item doc=createDoc();
                    doc.put("lat",m1.group(1));
                    doc.put("lon",m1.group(2));
                    doc.put("attributes","hidden");
                    doc.put("placed",Parser.CutTill(cut(wpts[i],"time"),"T"));
                    doc.put("cache-id",cut(wpts[i],"name"));
                    doc.put("name",cut(wpts[i],"desc"));
                    doc.put("description",Parser.decodeHTML(cut(wpts[i],"cmt")));
                    doc.put("url",cut(wpts[i],"url"));
                    doc.put("url-name",cut(wpts[i],"urlname"));
                    doc.put("type",Parser.CutTill(cut(wpts[i],"type"),"|"));
                    docs.add(doc);
                    DLM.addElement(doc.get("cache-id")+": "+doc.get("name"));
                } else {
                    System.out.println("Not found!");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}

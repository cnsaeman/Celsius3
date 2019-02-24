/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import celsius.tools.*;

/**
 * @author cnsaeman
 */
public class PluginArXiv extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from arXiv");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.4");
            put("help"              ,"This plugin looks for an arXiv identifier in the text and downloads the information belonging to this file.");
            put("needsFirstPage"    ,"yes");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI="P:GfA>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
    
    public String extract(String s) {
      int i=oai.indexOf("<"+s+">");
      int k=oai.indexOf("</"+s+">");
      if (i==-1) return(new String(""));
      String ret=oai.substring(i+s.length()+2,k).trim();
      ret=ret.replace("\n"," ");
      while (ret.indexOf("  ")>-1) ret=ret.replace("  "," ");
      return(ret);
    }

    public void SPut(String s) {
        String tmp=extract(s);
        if (!tmp.equals("")) Information.put(s,tmp);
    }

    public String getS(String s) {
	String tmp=Information.get(s);
	if (tmp==null) tmp=new String("");
	return(tmp);
    }
  
    public void run() {
	String FirstPage=Information.get("firstpage");
        String arx="";
        String nmb="";
        String tmp="";
        if (!Information.containsKey("arxiv-ref")) {
            if (FirstPage.indexOf("arXiv:")>-1) {
                ArrayList<String> T=arxivTools.GetArXivTags(FirstPage);
                arx=T.get(0);
                nmb=T.get(1);
                Information.put("arxiv-name",arx);
                Information.put("arxiv-number",nmb);
                tmp=nmb;
                // Math arxives correction
                if (arx.indexOf(".")>-1) arx=Parser.CutTill(arx,".");
                if (nmb.indexOf(".")==-1) tmp=arx+"/"+nmb;
                Information.put("arxiv-ref",tmp);
            } else {
                String fn=Parser.CutTillLast(Parser.CutFromLast(Information.get("fullpath"),"/"),".");
                if (fn.matches("\\d{4}\\.\\d{4,5}")) {
                    Information.put("arxiv-ref",fn);
                }                
            }
        } else {
            String ref=Information.get("arxiv-ref");
            if(ref.indexOf('/')>-1) {
                // old format
                arx=Parser.CutTill(ref,"/");
                nmb=Parser.CutFrom(ref,"/");
                tmp=arx+"/"+nmb;
            } else {
                // new format
                arx="unknown";
                nmb=ref;
                tmp=nmb;
            }
            Information.put("arxiv-name",arx);
            Information.put("arxiv-number",nmb);
        }
        if (Information.containsKey("arxiv-ref")) {
            String tag=arx+nmb;   // tag for naming temp files
            System.out.println("arXiv-ref: "+Information.get("arxiv-ref")+" :: TAG: "+tag+" :: ident: "+tmp);
            try {
                Msgs.add("Getting data from arXiv :: "+tmp);
		
                oai=TextFile.ReadOutURL("http://export.arxiv.org/oai2?verb=GetRecord&identifier=oai:arXiv.org:"+tmp+"&metadataPrefix=arXiv");
                System.out.println(oai);
		
                if (!oai.startsWith("##??")) {
                    SPut("title");
                    System.out.println("TITLE: "+Information.get("title"));
                    SPut("abstract");
                    SPut("created");
                    SPut("updated");
                    SPut("categories");
                    SPut("journal-ref");
                    SPut("doi");
                    tmp=extract("comments");
                    if (!tmp.equals("")) Information.put("remarks","Comments: "+tmp);
                    String authors=extract("authors");
                    tmp=new String();
                    //System.out.println("A");
                    authors=Parser.CutFrom(authors,"<author><keyname>");
                    while (authors.length()>0) {
                        tmp+="|"+Parser.CutTill(authors,"</keyname>")+", ";
                        authors=Parser.CutFrom(authors,"<forenames>");
                        tmp+=Parser.CutTill(authors,"</forenames>");
                        authors=Parser.CutFrom(authors,"<author><keyname>");
                    }
                    //System.out.println("B");
                    Information.put("authors",Parser.decodeHTML(tmp.substring(1)));
                    Information.put("type","Preprint");
		    if (getS("remarks").toLowerCase().indexOf("talk")>-1) Information.put("type","Talk");
		    if (getS("abstract").toLowerCase().indexOf("talk")>-1) Information.put("type","Talk");
		    if (getS("remarks").toLowerCase().indexOf(" thesis")>-1) Information.put("type","Thesis");
                    //System.out.println("C");
		    if (getS("abstract").toLowerCase().indexOf(" thesis")>-1) Information.put("type","Thesis");
		    if (getS("remarks").toLowerCase().indexOf("lecture")>-1) Information.put("type","Lecture Notes");
		    if (getS("abstract").toLowerCase().indexOf("lecture")>-1) Information.put("type","Lecture Notes");
                    //System.out.println("D");
		    if (Information.get("journal-ref")!=null) {
                celsius.BibTeXRecord BTR=new celsius.BibTeXRecord();
                BTR.tag=Parser.CutTill(getS("authors"),",")+":"+getS("arxiv-number");
                BTR.put("title",getS("title"));
                BTR.put("author",Parser.Substitute(getS("authors"),"|"," and "));
                BTR.put("archiveprefix","arXiv");
                BTR.put("eprint",getS("arxiv-ref"));
                BTR.put("doi",getS("doi"));
                BTR.put("primaryclass",getS("arxiv-name"));
                String jref=getS("journal-ref");
                if (jref.length()>4) {
                    Information.put("type","Paper");
                }

                // Extract bibtex info from arXiv records 
                Matcher myear = Pattern.compile("\\((\\d\\d\\d\\d)\\)").matcher(jref);
                // Find year of the form (yyyy)
                if (myear.find()) {
                    // Match following type: J. Topol. 8 (2015), no. 4, 1045-1084
                    String beforeYear=jref.substring(0,myear.start()).trim();
                    String afterYear=jref.substring(myear.end()).trim();
                    BTR.put("year",myear.group(1));
                    Matcher mvol = Pattern.compile("\\s([A-Z]?\\d+)").matcher(beforeYear);
                    if (mvol.find()) {
                        BTR.put("volume",mvol.group(1).trim());
                        BTR.put("journal",beforeYear.substring(0,mvol.start()).trim());
                    }
                    Matcher mpages = Pattern.compile("[,\\s](\\d+-\\d+)").matcher(afterYear);
                    if (mpages.find()) {
                        BTR.put("pages",mpages.group(1).trim());
                    }
                } else {
                    // Match following types: JHEP 0403:048,2004   J.Math.Phys.54:013507,2013    Nucl.Phys.B826:456-489,2010
                    Matcher mjhep = Pattern.compile("([A-Z]?\\d+):([\\d-]+)\\s?,\\s?(\\d\\d\\d\\d)").matcher(jref);
                    if (mjhep.find()) {
                        BTR.put("journal",jref.substring(0,mjhep.start()).trim());
                        BTR.put("volume",mjhep.group(1).trim());
                        BTR.put("pages",mjhep.group(2).trim());
                        BTR.put("year",mjhep.group(3).trim());
                    }
                }
                Information.put("bibtex",BTR.toString());
		    }
                } else {
                  Msgs.add(TI+"Retrieving data from arXiv failed :: "+oai);
                  Msgs.add(TI);
                }                       
		
            } catch (Exception e) {
                Msgs.add(e.toString());
            }
            Information.put("identifier", toolbox.Identifier(Information));
	    Information.put("recognition","50");
        }
    }
  
                      
}

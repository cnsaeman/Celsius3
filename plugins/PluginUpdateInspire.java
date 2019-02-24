/*
 * PluginSpires.java
 *
 * v1.1
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;


/**
 * @author cnsaeman
 */
public class PluginUpdateInspire extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Update to Inspire");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin updates all records with a Spires-ID to Inspire. " +
                                     "The parameter is the Inspire base URL, e.g. http://inspirehep.net/.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"spires-key|citation-tag");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"http://inspirehep.net/");
            put("parameter-help"    ,"Link to the Inspires mirror to be used.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String spiresbase;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    public void run() {
        String inspiresbase=Information.get("$$params");
        if (inspiresbase==null) inspiresbase="http://inspirehep.net/";

        String citationtag=Parser.Substitute(Information.get("citation-tag"),":","%3A");
        String srchstring=inspiresbase+"search?ln=en&p="+citationtag+"&f=&action_search=Search&c=HEP&sf=&so=d&rm=&rg=25&sc=0&of=hb";
        String tmp=TextFile.ReadOutURL(srchstring);
        String lnk=Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp,"class = \"titlelink\""),"record/"),"\"");
        if (lnk.length()>2) {
            Information.put("inspirekey",lnk);
            srchstring=inspiresbase+"record/"+lnk;
            tmp=TextFile.ReadOutURL(srchstring);

            //Keywords
            String keywords=Parser.CutFrom(tmp,"<strong>Keyword(s):");
            keywords=Parser.CutTill(keywords,"<br").trim();
            keywords=Parser.CutTill(keywords,"<div").trim();
            boolean changed=false;
            String infokeywords=Information.getS("keywords");
            while (keywords.length()>0) {
                String keyword=Parser.CutFrom(keywords,"\">");
                keyword=Parser.CutTill(keyword,"</a>");
                if (!Parser.EnumContains(Information.getS("keywords"), keywords)) {
                    infokeywords+="|"+keyword;
                    if (infokeywords.charAt(0)=='|') infokeywords=infokeywords.substring(1);
                    changed=true;
                }
                keywords=Parser.CutFrom(keywords,"</small>");
            }
            if (changed) Information.put("keywords",infokeywords);

            //Abstract
            if (Information.getS("abstract").length()<2) {
                String abs=Parser.CutFrom(tmp,"<strong>Abstract:");
                abs=Parser.CutTill(abs,"</small>");
                abs=Parser.CutFrom(abs,">").trim();
                Information.put("abstract",abs);
            }

            //BibTeX
            if (Information.getS("bibtex").length()<2) {
                srchstring=inspiresbase+"record/"+lnk+"/export/hx";
                String tmp2=TextFile.ReadOutURL(srchstring);
                if (tmp2.indexOf("<pre>")>-1) {
                    String bib=Parser.CutTill(Parser.CutFrom(tmp2,"<pre>"),"</pre>").trim();
                    if (bib.charAt(bib.length()-3)==',')
                        bib=Parser.CutTillLast(bib,",")+"\n}";
                    Information.put("bibtex",bib);
                }
            }
            celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(Information.getS("bibtex"));
            if (BTR.get("journal")!=null) {
                Information.put("type","Paper");
            } else {
                Information.put("type","Preprint");
            }

            //Identifier
            if (!Information.getS("identifier").equals(toolbox.Identifier(Information)))
                Information.put("identifier",toolbox.Identifier(Information));

            //doi, but not in BibTeX
            String doi=null;
            if ((BTR.getS("doi").equals("")) && (tmp.indexOf("http://dx.doi.org/")>-1)) {
                doi=Parser.CutFrom(tmp,"http://dx.doi.org/");
                doi=Parser.CutTill(doi,"\">");
                BTR.put("doi", doi);
                Information.put("bibtex",BTR.toString());
            }

            //References
            if (Information.getS("links").length()<2) {
                srchstring=inspiresbase+"record/"+lnk+"/references";
                String tmp2=TextFile.ReadOutURL(srchstring);
                String links=new String("");
                tmp2=Parser.CutFrom(tmp2,"<ul class=\"tight_list\">");
                while (tmp2.indexOf("<li>")>-1) {
                    String link=Parser.CutTill(tmp2,"</li>").trim();
                    if (link.indexOf("/record/")>-1) {
                        links+="|refers to:inspirekey:"+Parser.CutTill(Parser.CutFrom(link,"/record/"),"\n");
                    } else {
                        link=Parser.CutTill(Parser.CutFrom(link,"<small>")," (");
                        if (link.indexOf("<")>-1)
                            link=Parser.CutTill(link,"<");
                        links+="|refers to:identifier:"+link;
                    }
                    tmp2=Parser.CutFrom(tmp2,"</li>");
                }
                srchstring=inspiresbase+"search?ln=en&p=refersto%3Arecid%3A"+lnk;
                tmp2=TextFile.ReadOutURL(srchstring);
                tmp2=Parser.CutFrom(tmp2,"<!C-START");
                while (tmp2.indexOf("<div class=\"record_body\">")>-1) {
                    tmp2=Parser.CutFrom(tmp2,"<div class=\"record_body\">");
                    String link=Parser.CutTill(tmp2,"</div>").trim();
                    links+="|citation:inspirekey:"+Parser.CutTill(Parser.CutFrom(link,"/record/"),"\"");
                    tmp2=Parser.CutFrom(tmp2,"</div>");
                }
                Information.put("links",links.substring(1));
            }
        }

        /*
        if (!(arx==null) && (arxivTools.arxivAtSpires(arx))) {
            String nmb=Information.get("arxiv-number");
            try {
                // Obtain Data from SPIRES
                String key=arxivTools.GetKeyFromInternet(arx,nmb,spiresbase,Msgs);
                if (!key.equals("")) Information.put("spires-key",key);
                Information.put("bibtex",arxivTools.GetRefFromInternet(key,spiresbase,Msgs));
                celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(Information.get("bibtex"));
                Information.put("citation-tag",BTR.tag);
                if (BTR.get("journal")!=null) {
                    Information.put("type","Paper");
                } else {
                    Information.put("type","Preprint");
                }
                if ((!Information.get("title").equals("<unknown>")) && (!Information.get("authors").equals("<unknown>")) 
                    && (Information.get("bibtex")!=null) && (Information.get("spires-key")!=null)) {
                    Information.put("recognition","100");
		}
		Information.put("identifier", toolbox.Identifier(Information));
		String spireskey=Information.get("spires-key");
		String links=new String("");
		String srchstring=spiresbase+"hep/wwwrefsbibtex?key="+spireskey;
		String tmp=TextFile.ReadOutURL(srchstring);
		String bib;
		tmp="<pre>"+Parser.CutTillLast(Parser.CutFrom(tmp,"<pre>"),"</pre>").trim()+"</pre>";
		while (tmp.length()>15) {
		    bib=Parser.CutFrom(Parser.CutTill(tmp,"</pre>"),"<pre>").trim();
		    if (bib.startsWith("@")) {
			BTR=new celsius.BibTeXRecord(bib);
			if (BTR.parseError == 0) {
			    if (BTR.keySet().contains("eprint")) {
				links+="|refers to:"+BTR.get("eprint");
			    } else {
				if (BTR.get("journal").length() > 2)
				    links+="|refers to:"+BTR.getIdentifier();
			    }
			}
		    }
		    tmp=Parser.CutFrom(tmp,"</pre>").trim();
		}
		srchstring=spiresbase+"hep/www?key="+spireskey+"&FORMAT=WWWBRIEFBIBTEX";
		tmp=TextFile.ReadOutURL(srchstring);
		bib=Parser.CutTillLast(Parser.CutFrom(tmp,"<pre>"),"</pre>").trim();
		BTR=new celsius.BibTeXRecord(bib);
		if (BTR.parseError == 0) {
		    String ref=Parser.CutFromLast(Parser.CutTill(BTR.get("slaccitation"),";")," ");
		    srchstring = spiresbase + "find/hep/www?rawcmd=FIND+C+"+ref+"&FORMAT=wwwbriefbibtex&SEQUENCE=";
		    tmp = TextFile.ReadOutURL(srchstring);
		    tmp = "<pre>" + Parser.CutTillLast(Parser.CutFrom(tmp, "<pre>"), "</pre>").trim() + "</pre>";
		    while (tmp.length() > 15) {
			bib=Parser.CutFrom(Parser.CutTill(tmp,"</pre>"),"<pre>").trim();
			if (bib.startsWith("@")) {
			    BTR = new celsius.BibTeXRecord(bib);
			    if (BTR.parseError == 0) {
				if (BTR.keySet().contains("eprint")) {
				    links += "|citation:" + BTR.get("eprint");
				} else {
				    if (BTR.keySet().contains("journal") && BTR.get("journal").length() > 2) {
					links += "|citation:" + BTR.getIdentifier();
				    }
				}
			    }
			}
			tmp = Parser.CutFrom(tmp, "</pre>").trim();
		    }
		}
		if (links.length()>0) links=links.substring(1);
		Information.put("links",links);
		Msgs.add("Found links: "+links);

            } catch (Exception e) {
		e.printStackTrace();
                Msgs.add(e.toString());
            }
        }*/
    }
  

}

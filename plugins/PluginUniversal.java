import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.io.*;
import celsius.tools.*;

/**
 * @author cnsaeman
 * 
 * To include:
 * http://api.semanticscholar.org/v1/paper/6162a894c2481599a92e4adaa06d08c200d8c31e
 * 
 */
public class PluginUniversal extends Thread {
    
    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Universal Information Plugin");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin tries to retrieve all possible information from offline and online sources.");
            put("needsFirstPage"    ,"no");
            put("wouldLikeFirstPage","yes");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };
    
    // to do:
    // arXiv: check that any new information can be gained from obtaining arXiv info
    
    private final String TI="P:Un>";
    
    private String oai;
    
    private boolean debug;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    celsius.BibTeXRecord BTR;
    private boolean BTRjustCreated;
    
    
    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
    
    public void run() {
        debug=false;
        Msgs.add("Init");
        initializeThings();
        Msgs.add("FN");
        if (containsKey("fullpath")) getFromFileName();
        Msgs.add("FP");
        if (containsKey("firstpage")) getFromFirstPage();
        Msgs.add("PE");
        if (containsKey("projecteuclid")) getFromProjectEuclid();
        Msgs.add("ArX");
        if (containsKey("arxiv-ref")) getFromArXiv();
        Msgs.add("INSP");
        if (containsKey("inspirekey") || containsKey("arxiv-ref")) getFromInspire();
        Msgs.add("DOI");
        getFromDoiNew();
        completeBibTeX();
        completeIdentifier();
    }
    
    public void initializeThings() {
        if ((getS("authors").indexOf(" ")==0) || (getS("authors").length()<5)) putS("authors","<unknown>");
        if ((getS("title").indexOf(" ")==0) && (getS("title").length()<5)) putS("title","<unknown>");
        if (getS("bibtex").length()>5) {
            BTR=new celsius.BibTeXRecord(getS("bibtex"));
            BTRjustCreated=false;
        } else {
            BTR=new celsius.BibTeXRecord();
            BTRjustCreated=true;
        }
    }
    
    public void getFromFileName() {
        // extract potential arXiv infomation from file name, do not overwrite.
        if (Information.isEmpty("arxiv-ref") && !Information.containsKey("arxiv-ref")) {
            String fn=Parser.CutTillLast(Parser.CutFromLast(Information.get("fullpath"),"/"),".");
            if (fn.matches("\\d{4}\\.\\d{4,5}")) {
                Information.put("arxiv-ref",fn);
            }                 
        }
        if (getS("authors").equals("<unknown>") || getS("title").equals("<unknown>")) {
            String FileName = Parser.CutFromLast(Information.get("fullpath"), arxivTools.filesep);
            if (FileName.startsWith("euclid.")) {
                putS("projecteuclid",Parser.Substitute(Parser.CutFrom(Parser.CutTillLast(FileName,"."),"."),".","/"));
            } else if (FileName.startsWith("[")) {
                String authors=Parser.Substitute(Parser.CutTill(FileName.substring(1),"]"),"_"," ").trim();
                authors=Parser.Substitute(authors,","," and ");
                authors=toolbox.authorsBibTeX2Cel(authors);
                String title=Parser.Substitute(Parser.CutFrom(FileName,"]"),"_"," ").trim();
                title=Parser.Substitute(title,"(1)","");
                title=Parser.CutTill(title,"(");
                if ((title.length() > 3) && (authors.length() > 1)) {
                    putS("type", "Book");
                    putS("title", title);
                    putS("authors", authors);
                    putS("recognition", "50");
                }
            } else if ((FileName.indexOf("(") > 0) || (FileName.indexOf(" - ")>0)) {
                if (FileName.indexOf(")(") > 0) {
                    // System.out.println("Type 0::"+FileName);
                    String title = Parser.CutTill(FileName, "(").trim();
                    String authors = Parser.CutTillLast(title, ". ") + ".";
                    title = Parser.CutFromLast(title, ". ");
                    StringBuffer sb = new StringBuffer(authors);
                    while (sb.indexOf(".") > 2) {
                        sb.delete(sb.indexOf(".") - 1, sb.indexOf(".") + 1);
                    }
                    authors = sb.toString().replace(" ,", ",").trim();
                    // Entries correct -> make the necessary entries
                    if ((title.length() > 3) && (authors.length() > 1)) {
                        putS("title", title);
                        putS("authors", authors);
                        putS("recognition", "50");
                    }
                } else {
                    // Filename is of the form "authorname, authorfirstname - title.filetype"
                    // System.out.println(FileName+"::"+String.valueOf(FileName.indexOf(" - "))+"::"+String.valueOf(FileName.indexOf(" - ")));
                    if ((FileName.indexOf(" - ")>FileName.indexOf(", "))) {
                        String title = Parser.CutTillLast(Parser.CutFrom(FileName, " - "),".").trim();
                        String authors = Parser.CutTill(FileName, " - ").trim();
                        if ((title.length() > 3) && (authors.length() > 1)) {
                            Information.put("title", title);
                            Information.put("authors", authors);
                            Information.put("recognition", "50");
                        }
                    } else {
                        // System.out.println("Type 3::"+FileName);
                        // FileName is of the form "title (authors)"
                        String title = Parser.CutTillLast(FileName, "(").trim().replace("_", " ");
                        String authors = Parser.CutTill(Parser.CutFromLast(FileName, "("), ")").replace("_", " ");
                        authors = authors.replace(',', '|');
                        authors = authors.replace("| ", "|");
                        // Entries correct -> make the necessary entries
                        if ((title.length() > 3) && (authors.length() > 1)) {
                            Information.put("title", title);
                            Information.put("authors", authors);
                            Information.put("recognition", "50");
                        }
                    }
                }
            }
        }
    }
    
    public void getFromFirstPage() {
        String FirstPage=getS("firstpage");
        // extract potential arXiv infomation from first page, do not overwrite.
        if (!containsKey("arxiv-ref") && (FirstPage.indexOf("arXiv:")>-1)) {
            ArrayList<String> T=arxivTools.GetArXivTags(FirstPage);
            String arx=T.get(0);
            String nmb=T.get(1);
            Information.put("arxiv-name",arx);
            Information.put("arxiv-number",nmb);
            String tmp=nmb;
            // Math arxives correction
            if (arx.indexOf(".")>-1) arx=Parser.CutTill(arx,".");
            if (nmb.indexOf(".")==-1) tmp=arx+"/"+nmb;
            Information.put("arxiv-ref",tmp);
        } else if (FirstPage.indexOf("http://www.jstor.org/stable/")>-1) {
            getFromJSTOR(FirstPage);
        } else {
            Pattern pattern=Pattern.compile("[Dd][Oo][Ii][: ]+(\\S+\\/\\S+)");
            Matcher matcher=pattern.matcher(FirstPage);
            if (matcher.find()) {
                Information.put("doi",matcher.group(1));
            }
        }
    }
    
    // todo: extract primary arXiv, in particular for maths
    public void getFromArXiv() {
        String ref=getS("arxiv-ref");
        String arx="";
        String nmb="";
        String tmp;
        // set arxiv-name and number correctly
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
        if (blank(getS("arXiv-name"))) Information.put("arxiv-name",arx);
        if (blank(getS("arXiv-number"))) Information.put("arxiv-number",nmb);
        String tag=arx+nmb;   // tag for naming temp files
        Msgs.add("arXiv-ref: "+Information.get("arxiv-ref")+" :: TAG: "+tag+" :: ident: "+tmp);
        try {
            Msgs.add("Getting data from arXiv :: "+tmp);
            
            oai=TextFile.ReadOutURL("http://export.arxiv.org/oai2?verb=GetRecord&identifier=oai:arXiv.org:"+tmp+"&metadataPrefix=arXiv");
            //System.out.println(oai);
            
            if (!oai.startsWith("##??")) {
                SPut("title");
                //System.out.println("TITLE: "+Information.get("title"));
                SPut("abstract");
                SPut("created");
                SPut("updated");
                SPut("categories");
                SPut("journal-ref");
                tmp=arXivExtract("doi");
                SPut("doi");
                if (!blank(tmp)) putS("doi",tmp);
                tmp=arXivExtract("comments");
                if (!blank(tmp)) putS("remarks","Comments: "+tmp);
                tmp=arXivExtract("categories");
                if (!blank(tmp)) putS("arxiv-name",Parser.CutTill(tmp," "));
                String authors=arXivExtract("authors");
                tmp=new String();
                //System.out.println("A");
                authors=Parser.CutFrom(authors,"<author><keyname>");
                while (authors.length()>0) {
                    tmp+="|"+Parser.CutTill(authors,"</keyname>")+", ";
                    authors=Parser.CutFrom(authors,"<forenames>");
                    tmp+=Parser.CutTill(authors,"</forenames>");
                    authors=Parser.CutFrom(authors,"<author><keyname>");
                }
                putS("authors",Parser.decodeHTML(tmp.substring(1)));
                putS("type","Preprint");
                if (getS("remarks").toLowerCase().indexOf("talk")>-1) putS("type","Talk");
                if (getS("abstract").toLowerCase().indexOf("talk")>-1) putS("type","Talk");
                if (getS("remarks").toLowerCase().indexOf(" thesis")>-1) putS("type","Thesis");
                if (getS("abstract").toLowerCase().indexOf(" thesis")>-1) putS("type","Thesis");
                if (getS("remarks").toLowerCase().indexOf("lecture")>-1) putS("type","Lecture Notes");
                if (getS("abstract").toLowerCase().indexOf("lecture")>-1) putS("type","Lecture Notes");
                if (blank(BTR.tag)) BTR.tag=Parser.CutTill(getS("authors"),",")+":"+getS("arxiv-number");
                if (blank(BTR.get("title"))) BTR.put("title",getS("title"));
                if (blank(BTR.get("author"))) BTR.put("author",Parser.Substitute(getS("authors"),"|"," and "));
                if (blank(BTR.get("archiveprefix"))) BTR.put("archiveprefix","arXiv");
                if (blank(BTR.get("eprint"))) BTR.put("eprint",getS("arxiv-ref"));
                if (blank(BTR.get("primaryclass"))) BTR.put("primaryclass",getS("arxiv-name"));
                if (Information.get("journal-ref")!=null) {
                    BTR.put("doi",getS("doi"));
                    String jref=getS("journal-ref");
                    if (jref.length()>4) {
                        putS("type","Paper");
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
                            if (blank(BTR.get("volume"))) BTR.put("volume",mvol.group(1).trim());
                            if (blank(BTR.get("journal"))) BTR.put("journal",beforeYear.substring(0,mvol.start()).trim());
                        }
                        Matcher mpages = Pattern.compile("[,\\s](\\d+-\\d+)").matcher(afterYear);
                        if (mpages.find()) {
                            if (blank(BTR.get("pages"))) BTR.put("pages",mpages.group(1).trim());
                        }
                    } else {
                        // Match following types: JHEP 0403:048,2004   J.Math.Phys.54:013507,2013    Nucl.Phys.B826:456-489,2010
                        Matcher mjhep = Pattern.compile("([A-Z]?\\d+):([\\d-]+)\\s?,\\s?(\\d\\d\\d\\d)").matcher(jref);
                        if (mjhep.find()) {
                            if (blank(BTR.get("journal"))) BTR.put("journal",jref.substring(0,mjhep.start()).trim());
                            if (blank(BTR.get("volume"))) BTR.put("volume",mjhep.group(1).trim());
                            if (blank(BTR.get("pages"))) BTR.put("pages",mjhep.group(2).trim());
                            if (blank(BTR.get("year"))) BTR.put("year",mjhep.group(3).trim());
                        }
                    }
                }
            } else {
                Msgs.add(TI+"Retrieving data from arXiv failed :: "+oai);
                Msgs.add(TI);
            }                       
            
        } catch (Exception e) {
            Msgs.add(e.toString());
        }
        putS("recognition","50");
    }
    
    public void getFromInspire() {
        try {
            Msgs.add("Trying inspire...");
            String inspirebase = "http://inspirehep.net/";
            String tmp;
            if (Information.isEmpty("inspirekey") && !Information.isEmpty("arxiv-ref")) {
                String no = Information.get("arxiv-ref");
                if (no.charAt(4) == '.') {
                    no = Parser.Substitute(no, ".", "+");
                }
                String srchstring = new String(inspirebase + "search?ln=en&p=find+eprint+" + no);
                tmp = TextFile.ReadOutURL(srchstring);
                putS("inspirekey",Parser.CutTill(Parser.CutFrom(Parser.CutFrom(tmp, "class = \"titlelink\""), "record/"), "\""));
            }
            if (!Information.isEmpty("inspirekey")) {
                Msgs.add("Trying inspire2...");
                tmp = webToolsHEP.getInspireRecord(inspirebase, getS("inspirekey"));
                
                //Keywords
                String keywords = webToolsHEP.keywordsFromInspire(tmp);
                boolean changed = false;
                String infokeywords = Information.getS("keywords");
                while (keywords.length() > 0) {
                    String keyword = Parser.CutFrom(keywords, "\">");
                    keyword = Parser.CutTill(keyword, "</a>");
                    if (!Parser.EnumContains(Information.getS("keywords"), keywords)) {
                        infokeywords += "|" + keyword;
                        if (infokeywords.charAt(0) == '|') {
                            infokeywords = infokeywords.substring(1);
                        }
                        changed = true;
                    }
                    keywords = Parser.CutFrom(keywords, "</small>");
                }
                if (changed) {
                    putS("keywords", infokeywords);
                }
                
                //Abstract
                if (Information.isEmpty("abstract")) {
                    String abs = webToolsHEP.abstractFromInspire(tmp);
                    if (abs != null) {putS("abstract", abs);
                    }
                }
                
                //BibTeX
                Msgs.add("inspireBIB");
                String bt = webToolsHEP.bibTeXFromInspire(inspirebase, getS("inspirekey"));
                bt = "@" + Parser.CutFrom(bt, "@");
                Msgs.add("before");
                celsius.BibTeXRecord BTRInsp = new celsius.BibTeXRecord(bt);
                Msgs.add("after");
                Msgs.add("BibTeX accepted");
                
                // overwrite doi results if empty
                if (blank(BTR.get("journal")) && !blank(BTRInsp.get("journal"))) {
                    BTR.put("journal", BTRInsp.get("journal"));
                    BTR.put("year", BTRInsp.get("year"));
                }
                
                String[] copies = {"title","author","booktitle","pages", "volume", "doi", "slaccitation"};
                for (String s : copies) {
                    if (blank(BTR.get(s)) && !blank(BTRInsp.get(s))) {
                        BTR.put(s, BTRInsp.get(s));
                    }
                }
                
                if ((blank(BTR.get("primaryclass")) || (BTR.get("primaryclass").equals("unknown"))) && !blank(BTRInsp.get("primaryclass"))) {
                    BTR.put("primaryclass", BTRInsp.get("primaryclass"));
                }
                
                // Paper type
                if (!blank(BTR.get("journal"))) {
                    putS("type", "Paper");
                } else {
                    putS("type", "Preprint");
                }
                
                // Inspire citation tag over arxiv:
                BTR.tag = BTRInsp.tag;
                
                //doi, but not in BibTeX
                String doi = null;
                if (blank(BTR.getS("doi")) && (tmp.indexOf("http://dx.doi.org/") > -1)) {
                    doi = Parser.CutFrom(tmp, "http://dx.doi.org/");
                    doi = Parser.CutTill(doi, "\">");
                    BTR.put("doi", doi);
                }
                
                // adjust title authors, if no other source
                if (invalid(Information.getS("title"))) {
                    putS("title", BTR.get("title"));
                }
                if (Information.get("title").startsWith("{")) {
                    putS("title", Parser.CutTillLast(Information.get("title").substring(1), "}"));
                }
                if (invalid(Information.getS("authors"))) {
                    putS("authors", celsius.BibTeXRecord.authorsBibTeX2Cel(BTR.get("author")));
                }
                
                // adjust recognition level
                if (!Information.isEmpty("title") && !Information.isEmpty("authors") && !Information.isEmpty("bibtex")) {
                    if ((!Information.get("title").equals("<unknown>")) && (!Information.get("authors").equals("<unknown>")) && (Information.get("bibtex").length() > 2) && (Information.getS("inspirekey").length() > 2)) {
                        putS("recognition", "100");
                    }
                }
                                
                //References
                /*if (Information.isEmpty("links")) {
                    putS("links", webToolsHEP.linksFromInspire(inspirebase, getS("inspirekey")));
                }*/
                
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Msgs.add("Error in GetFromInspire : "+errors.toString());
        }
    }
    
    public void getFromDoiNew() {
        try {
            if (!Information.isEmpty("doi") && invalid("BTR.pages")) {
                String doi = Information.get("doi");
                BTR.put("doi",doi);
                StringBuffer rtn=new StringBuffer(4000);
                String dataPage=TextFile.ReadOutURL("https://api.crossref.org/works/"+doi);
                try {
                    TextFile tf=new TextFile("Plugin.Universal.ReturnedData.txt.tmp",false);
                    tf.putString(dataPage);
                    tf.close();
                } catch (Exception e) { 
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    Msgs.add("Error writing: "+errors.toString());
                }
                String title=Parser.CutTill(Parser.CutFrom(dataPage,"\"title\":[\""),"\"");
                String year=Parser.CutFrom(dataPage,"\"published-print\":{\"date-parts\":[[").substring(0,4);
                String volume=Parser.CutTill(Parser.CutFrom(dataPage,"\"volume\":\""),"\"");
                String pages=Parser.CutTill(Parser.CutFrom(dataPage,"\"page\":\""),"\"");
                String journal=Parser.CutTill(Parser.CutFrom(dataPage,"\"container-title\":[\""), "\"");
                if (dataPage.indexOf("\"short-container-title\":[\"")>-1) {
                    journal=Parser.CutTill(Parser.CutFrom(dataPage,"\"short-container-title\":[\""), "\"");
                }
                String authors="";
                String res3=Parser.CutFrom(dataPage,"\"author\":");
                while (res3.indexOf("\"given\"")>-1) {
                    int i=res3.indexOf("\"family\":\"");
                    int j=res3.indexOf("\"given\":\"");
                    if (i<j) i=j;
                    String author = Parser.CutTill(Parser.CutFrom(res3,"\"family\":\""),"\"")+", "+Parser.CutTill(Parser.CutFrom(res3,"\"given\":\""),"\"");
                    res3=res3.substring(i+1);
                    authors+="|"+author;
                }
                authors=authors.substring(1);
                if (invalid("title")) Information.put("title",title);
                if (invalid("authors")) Information.put("authors",authors);
                if (blank(BTR.get("journal"))) BTR.put("journal",journal);
                if (blank(BTR.get("volume"))) BTR.put("volume",volume);
                if (!year.equals(BTR.get("year"))) BTR.put("year",year);
                if (blank(BTR.get("pages"))) BTR.put("pages",pages);
                putS("type","Paper");
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Msgs.add("Error in GetFromDoi: "+errors.toString());
        }
    }
        
    public void completeBibTeX() {
        try {
            Msgs.add("Completing BibTeX...");
            Msgs.add("OLD:"+BTR.toString());
            if (blank(BTR.get("author")) && !invalid("authors")) {
                Msgs.add("Adjusting author..."+toolbox.ToBibTeXAuthors(getS("authors")));
                BTR.put("author",toolbox.ToBibTeXAuthors(getS("authors")));
            }
            if (blank(BTR.get("title")) && !invalid("title")) {
                BTR.put("title",getS("title"));
            }
            String titleN=normalizeBTR(BTR.get("title"));
            if (!titleN.equals(BTR.get("title"))) BTR.put("title",titleN);
            if (blank(BTR.tag) && !invalid("authors")) {
                String tag=Parser.CutTill(getS("authors"),",").replace(" ","");
                if (!blank(BTR.get("year"))) tag=tag+":"+BTR.get("year");
                if (!blank(BTR.get("pages"))) tag=tag+":"+BTR.get("pages"); else tag=tag+"aa";
                BTR.tag=tag;
            }
            if (BTR.get("year")==null) {
                String eprint=BTR.get("eprint");
                if (eprint!=null) {
                    String tmp="";
                    int i=eprint.indexOf("/");
                    if (i>0) {
                        if (eprint.charAt(i+1)=='9') {
                            tmp="19"+eprint.substring(i+1,i+3);
                        } else {
                            tmp="20"+eprint.substring(i+1,i+3);
                        }
                    } else {
                        tmp="20"+eprint.substring(0,2);
                    }
                    BTR.put("year",tmp);
                }
            }
            if ((!blank(BTR.get("journal"))) || (!blank(getS("inspirekey")))) {
                putS("recognition", "100");
            }
            // Remove empty tags 
            BTR.entrySet().removeIf(e -> blank(e.getValue()));
            // Write, if minimum of information.
            normalizeBTRJournal();
            Msgs.add("Thinking about writing BTR...");
            if (!blank(BTR.get("author")) && !blank(BTR.get("title"))) {
                Msgs.add("Entered...");
                celsius.BibTeXRecord BTRold=new celsius.BibTeXRecord(getS("bibtex"));
                Msgs.add("OLD::"+BTRold.toString());
                Msgs.add("NEW::"+BTR.toString());
                if (!BTRold.toString().equals(BTR.toString())) putS("bibtex",BTR.toString());
            }
            // adjust citation tag if necessary
            if (blank(Information.getS("citation-tag")) || !Information.getS("citation-tag").equals(BTR.tag)) {
                putS("citation-tag", BTR.tag);
            }
            Msgs.add("NEW: "+BTR.tag);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Msgs.add("Error in completeBibTeX: "+errors.toString());
        }
    }
    
    public void completeIdentifier() {
        String tmp="";
        
        String arxref=Information.get("arxiv-ref");
        String arxname=Information.get("arxiv-name");
        if (arxref!=null) {
            if (arxref.indexOf(arxname)>-1) {
                tmp=arxref;
            } else {
                tmp=arxref+" ["+arxname+"]";
            }
        }
        if (!blank(BTR.get("journal"))) {
            String identifier=new String("");
            identifier=BTR.get("journal");
            if (!blank(BTR.get("volume"))) identifier+=" "+BTR.get("volume");
            if (!blank(BTR.get("year"))) identifier+=" ("+BTR.get("year")+")";
            if (!blank(BTR.get("pages"))) identifier+=" "+BTR.get("pages");
            tmp+=" "+identifier.trim();
        }
        if (blank(BTR.get("doi")) && !blank(getS("doi"))) BTR.put("doi",getS("doi"));
        
        if (BTR.get("year")!=null) tmp=BTR.get("year")+" "+tmp;
        putS("identifier", tmp.trim());
    }
    
    public void getFromProjectEuclid() {
        String url="https://projecteuclid.org/euclid."+getS("projecteuclid");
        Msgs.add("Reading out from Project Euclid: "+url);
        String info=TextFile.ReadOutURL(url);
        if (!info.startsWith("##??")) {
            if (invalid("title")) {
                putS("title",Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_title\" content=\""),"\""));
                BTR.put("title",getS("title"));
            }
            if (invalid("authors")) {
                String authors="";
                String info2=new String(info);
                while(info2.indexOf("<meta name=\"citation_author\" content=\"")>-1) {
                    info2=Parser.CutFrom(info2,"<meta name=\"citation_author\" content=\"");
                    String aut=Parser.CutTill(info2,"\"");
                    authors+="|"+Parser.CutFromLast(aut," ")+", "+Parser.CutTillLast(aut," ");
                }
                Msgs.add("Authors: "+authors.substring(1));
                putS("authors",authors.substring(1));
            }
            putS("type", "Paper");
            if (blank(getS("abstract"))) putS("abstract",Parser.CutTags(Parser.CutFrom(Parser.CutTill(Parser.CutFrom(info,"<div class=\"abstract-text\">"),"</p>"),"<p>")).trim());
            if (blank(BTR.get("url"))) BTR.put("url",url);
            if (blank(BTR.get("journal"))) BTR.put("journal",Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_journal_title\" content=\""),"\""));
            if (blank(BTR.get("year"))) BTR.put("year",Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_year\" content=\""),"\""));
            if (blank(BTR.get("volume"))) BTR.put("volume",Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_volume\" content=\""),"\""));
            if (blank(BTR.get("issue"))) BTR.put("issue",Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_issue\" content=\""),"\""));
            if (blank(BTR.get("pages"))) {
                BTR.put("pages",Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_firstpage\" content=\""),"\"")+"-"+Parser.CutTill(Parser.CutFrom(info,"<meta name=\"citation_lastpage\" content=\""),"\""));
            }
        }
    }
    
    public void getFromJSTOR(String fp) {
        String nmb = Parser.CutTill(Parser.CutFrom(fp, "http://www.jstor.org/stable/"), " ");
        String title = Parser.CutFromLast(Parser.CutTill(fp, "Author(s):").trim(),"\n").trim();
        String authors = Parser.CutTill(Parser.CutFrom(fp, "Author(s):"), "\n").trim();
        fp = Parser.CutFrom(fp, "Source:");
        String journal = Parser.CutTill(fp, ", ").trim();
        String vol;
        if (fp.indexOf("No.") > -1) {
            vol = Parser.CutTill(Parser.CutFrom(fp, ", Vol."), ", No.").trim();
            fp = Parser.CutFrom(fp, ", No.");
        } else {
            vol = Parser.CutTill(Parser.CutFrom(fp, ", Vol."), "(").trim();
        }
        String year = Parser.CutTill(Parser.CutFrom(fp, ".,"), ")").trim();
        String pages = Parser.CutTill(Parser.CutFrom(fp, "pp."), "Published").trim();
        putS("JSTOR-ID", nmb);
        putS("title", title);
        putS("authors", toolbox.authorsBibTeX2Cel(authors));
        putS("type","Paper");
        if (blank(BTR.get("authors"))) BTR.put("authors",authors);
        if (blank(BTR.get("journal"))) BTR.put("journal",journal);
        if (blank(BTR.get("volume"))) BTR.put("volume",vol);
        if (blank(BTR.get("year"))) BTR.put("volume",year);
        if (blank(BTR.get("pages"))) BTR.put("volume",pages);
        if (blank(BTR.get("url"))) BTR.put("url","http://www.jstor.org/stable/" + nmb);
        Msgs.add("Found JSTOR-ID:" + nmb);
    }
    
    public String arXivExtract(String s) {
        int i=oai.indexOf("<"+s+">");
        int k=oai.indexOf("</"+s+">");
        if (i==-1) return(new String(""));
        String ret=oai.substring(i+s.length()+2,k).trim();
        ret=ret.replace("\n"," ");
        while (ret.indexOf("  ")>-1) ret=ret.replace("  "," ");
        return(ret);
    }
    
    public void SPut(String s) {
        String tmp=arXivExtract(s);
        if (!tmp.equals("")) Information.put(s,tmp);
    }
    
    public void putS(String t,String v) {
        if (v!=null) Information.put(t,v);
    }
    
    public String getS(String s) {
        String tmp=Information.get(s);
        if (tmp==null) tmp=new String("");
        return(tmp);
    }
    
    public boolean containsKey(String s) {
        return(Information.containsKey(s));
    }
    
    public boolean blank(String s) {
        return(!(s != null && !s.isEmpty()));
    }
    
    public String normalizeBTR(String s) {
        // normalize things
        s=s.replaceAll("Chern","{C}hern");
        s=s.replaceAll("Simons","{S}imons");
        s=s.replaceAll("hern\\-\\{S\\}imons","hern--{S}imons");
        s=s.replaceAll("Yang","{Y}ang");
        s=s.replaceAll("Mills","{M}ills");
        s=s.replaceAll("ang\\-\\{M\\}ills","ang--{M}ills");
        s=s.replaceAll("Calabi","{C}alabi");
        s=s.replaceAll("Yau","{Y}au");
        s=s.replaceAll("alabi\\-\\{Y\\}au","alabi--{Y}au");
        s=s.replaceAll("Feynman","{F}eynman");
        s=s.replaceAll("D-brane","{D}-brane");
        s=s.replaceAll(" BV"," {BV}");
        return(s);
    }
    
    public void normalizeBTRJournal() {
        String journal=BTR.get("journal");
        if (!blank(journal)) {
            journal=journal.replaceAll("([a-zA-Z]\\.)([a-zA-Z])","$1 $2");
            journal=journal.replaceAll("Journal", "J.");
            journal=journal.replaceAll("Communications", "Commun.");
            journal=journal.replaceAll(" of ", " ");
            journal=journal.replaceAll(" in ", " ");
            journal=journal.replaceAll("Advances", "Adv.");
            journal=journal.replaceAll("Mathematics", "Math.");
            journal=journal.replaceAll("Mathematical", "Math.");
            journal=journal.replaceAll("Physics", "Phys.");
            journal=journal.replaceAll("Letters", "Lett.");
            if (!journal.equals(BTR.get("journal"))) BTR.put("journal",journal);
        }
    }
    
    public boolean invalid(String s) {
        String tmp;
        if (s.equals("title")) {
            tmp=getS(s);
            return(blank(tmp) || tmp.equals("<unknown>"));
        } 
        if (s.equals("authors")) {
            tmp=getS(s);
            return(blank(tmp) || tmp.equals("<unknown>") || (!tmp.contains(",")));
        }        
        if (s.equals("BTR.pages")) {
            return(blank(BTR.get("pages")));
        } else {
            tmp=getS(s);
            return(blank(tmp));
        }
    }
    
}


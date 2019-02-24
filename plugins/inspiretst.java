import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import celsius.tools.TextFile;
import celsius.tools.Parser;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * tester.java
 *
 * Created on 13. Oktober 2007, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author cnsaeman
 */
public class inspiretst {

    public final static String EOP = String.valueOf((char) 12);   // EndOfPage signal
    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public static ZipFile ZF;
    public static TextFile out;
    public static String md;
    public static String bd; // basedir
    
    public static String inspireRecordJSON(String rec) {
      String tmp2 = TextFile.ReadOutURL("https://inspirehep.net/record/"+rec+"?of=recjson&ot=comment,title,system_number,abstract,authors,doi,primary_report_number,publication_info,physical_description,number_of_citations,thesaurus_terms");
      return tmp2;
    }


    public static String CutTags(String s1,String t) {
      while (s1.indexOf("<"+t)>0) {
	s1=Parser.CutTill(s1,"<"+t)+Parser.CutFrom(Parser.CutFrom(s1,"<"+t),">");
      }
      while (s1.indexOf("</"+t)>0) {
	s1=Parser.CutTill(s1,"</"+t)+Parser.CutFrom(Parser.CutFrom(s1,"</"+t),">");
      }
      return(s1);
    }

    public static String LowerTags(String s) {
        if (s.indexOf("<")>-1) {
            String tag=Parser.CutTill(Parser.CutFrom(s,"<"),">");
            s=Parser.Substitute(s,"<"+tag+">","<"+tag.toLowerCase()+">");
        }
        return(s);
    }
    
    public static String decodeString(String s) throws UnsupportedEncodingException {
        Pattern pattern = Pattern.compile("\\\\u(\\S\\S\\S\\S)");
        Matcher matcher = pattern.matcher(s);
        String rep=s;
        while (matcher.find()) {
            String g=matcher.group(1);
            rep=rep.replaceAll("\\\\u"+g,String.valueOf((char)Integer.parseInt(g,16)));
        }        
        return(rep);
    }

    public static void tryputArray(HashMap<String,String> props,String s, String key, String re) throws UnsupportedEncodingException {
        if (s==null) return;
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(s);
        String rep="";
        while (matcher.find()) {
            rep+="|"+matcher.group(1);
        }        
        if (rep!="") {
            props.put(key,decodeString(rep.substring(1)));
        }
    }
    
    public static void tryput(HashMap<String,String> props,String s, String key, String re) throws UnsupportedEncodingException  {
        if (s==null) return;
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(s);
        String rep=null;
        while (matcher.find()) {
            rep=matcher.group(1);
        }        
        if (rep!=null) props.put(key, decodeString(rep));
    }
    
    public static String extractSub(String s, String re) {
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(s);
        String rep=null;
        while (matcher.find()) {
            rep=matcher.group(1);
        }        
        return(rep);
    }
    
    public static HashMap<String,String> getRecord(String key) throws UnsupportedEncodingException {
        //String s=inspireRecordJSON("1247495");
        String s="[{\"comment\": \"31+1 pages, presentation slightly improved, version published in JMP\", \"doi\": \"10.1063/1.4832395\", \"physical_description\": {\"pagination\": \"32\"}, \"number_of_citations\": 15, \"title\": {\"title\": \"Six-Dimensional (1,0) Superconformal Models and Higher Gauge Theory\"}, \"abstract\": [{\"number\": \"arXiv\", \"summary\": \"We analyze the gauge structure of a recently proposed superconformal field theory in six dimensions. We find that this structure amounts to a weak Courant-Dorfman algebra, which, in turn, can be interpreted as a strong homotopy Lie algebra. This suggests that the superconformal field theory is closely related to higher gauge theory, describing the parallel transport of extended objects. Indeed we find that, under certain restrictions, the field content and gauge transformations reduce to those of higher gauge theory. We also present a number of interesting examples of admissible gauge structures such as the structure Lie 2-algebra of an abelian gerbe, differential crossed modules, the 3-algebras of M2-brane models and string Lie 2-algebras.\"}, {\"number\": \"AIP\", \"summary\": \"We analyze the gauge structure of a recently proposed superconformal field theory in six dimensions. We find that this structure amounts to a weak Courant-Dorfman algebra, which, in turn, can be interpreted as a strong homotopy Lie algebra. This suggests that the superconformal field theory is closely related to higher gauge theory, describing the parallel transport of extended objects. Indeed we find that, under certain restrictions, the field content and gauge transformations reduce to those of higher gauge theory. We also present a number of interesting examples of admissible gauge structures such as the structure Lie 2-algebra of an abelian gerbe, differential crossed modules, the 3-algebras of M2-brane models, and string Lie 2-algebras.\"}], \"thesaurus_terms\": [{\"term\": \"algebra: Lie\"}, {\"term\": \"field theory: conformal\"}, {\"term\": \"dimension: 6\"}, {\"term\": \"transformation: gauge\"}, {\"term\": \"gauge field theory\"}, {\"term\": \"homotopy\"}, {\"term\": \"supersymmetry: conformal\"}], \"system_number\": null, \"primary_report_number\": [\"EMPG-13-11\", \"arXiv:1308.2622\"], \"authors\": [{\"affiliation\": \"Heriot-Watt U.\", \"first_name\": \"Sam\", \"last_name\": \"Palmer\", \"full_name\": \"Palmer, Sam\"}, {\"affiliation\": \"Heriot-Watt U.\", \"first_name\": \"Christian\", \"last_name\": \"S\\u00e4mann\", \"full_name\": \"S\\u00e4mann, Christian\"}], \"publication_info\": {\"volume\": \"54\", \"pagination\": \"113509\", \"title\": \"J.Math.Phys.\", \"year\": \"2013\"}}]";
        HashMap<String,String> props=new HashMap<String,String>();
        tryput(props,s,"comment","\"comment\": \"(.+?)\", \"");
        tryput(props,s,"doi","\"doi\": \"(.+?)\", \"");
        tryput(props,extractSub(s,"\"physical_description\": \\{(.+?)\\}, "),"pages","\"pagination\": \"(.+?)\"");
        tryput(props,s,"citations","\"number_of_citations\": (.+?), \"");
        tryput(props,extractSub(s,"\"title\": \\{(.+?)\\}, "),"title","\"title\": \"(.+?)\"");
        tryput(props,extractSub(s,"\"abstract\": \\[\\{(.+?)\\}, "),"abstract","\"summary\": \"(.+?)\"");
        tryputArray(props,extractSub(s,"\"thesaurus_terms\": \\[(.+?)\\], "),"keywords","\"term\": \"(.+?)\"");
        tryputArray(props,extractSub(s,"\"authors\": \\[(.+?)\\], "),"authors","\"full_name\": \"(.+?)\"");
        String report=extractSub(s,"\"primary_report_number\": \\[(.+?)\\]");
        Pattern pattern = Pattern.compile("\"(.+?)\"");
        // pattern for hep-th/0508137
        Pattern arX1 = Pattern.compile("\\A\\D{4,10}+\\/\\d\\d\\d\\d\\d\\d\\d\\z");
        // pattern for arXiv:1001.3275
        Pattern arX2 = Pattern.compile("\\AarXiv:\\d{4}.\\d{4,5}\\z");
        Matcher matcher = pattern.matcher(report);
        String repno="";
        while (matcher.find()) {
            String sub=matcher.group(1);
            Matcher m=arX1.matcher(sub);
            if (m.find()) {
                String[] arx_rec=sub.split("/");
                props.put("arxiv-ref",sub);
                props.put("arxiv-name",arx_rec[0]);
                props.put("arxiv-number", arx_rec[1]);
            } else {
                m=arX2.matcher(sub);
                if (m.find()) {
                    props.put("arxiv-number", sub.split(":")[1]);
                    props.put("arxiv-ref", sub.split(":")[1]);
                } else {
                    repno+="|"+sub;
                }
            }
        }        
        if (repno!="") props.put("report id", repno.substring(1));
        String pup=extractSub(s,"\"publication_info\": \\{(.+?)\\}");
        tryput(props,pup,"volume","\"volume\": \"(.+?)\"");
        tryput(props,pup,"journal","\"title\": \"(.+?)\"");
        tryput(props,pup,"page","\"pagination\": \"(.+?)\"");
        tryput(props,pup,"year","\"year\": \"(.+?)\"");
        return(props);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(decodeString("S\\u00e4mann"));
        HashMap<String,String> props=getRecord("1247495");
        System.out.println("----");
        System.out.println("comment: "+props.get("comment"));
        System.out.println("doi: "+props.get("doi"));
        System.out.println("pages: "+props.get("pages"));
        System.out.println("citations: "+props.get("citations"));
        System.out.println("title: "+props.get("title"));
        System.out.println("abstract: "+props.get("abstract").substring(0,30));
        System.out.println("keywords: "+props.get("keywords"));
        System.out.println("authors: "+props.get("authors"));
        System.out.println("volume: "+props.get("volume"));
        System.out.println("journal: "+props.get("journal"));
        System.out.println("page: "+props.get("page"));
        System.out.println("year: "+props.get("year"));
        System.out.println("arxiv-name: "+props.get("arxiv-name"));
        System.out.println("arxiv-number: "+props.get("arxiv-number"));
        System.out.println("arxiv-ref: "+props.get("arxiv-ref"));
        System.out.println("report id: "+props.get("report id"));
        //System.out.println(inspireRecordJSON("690295"));
    }
}

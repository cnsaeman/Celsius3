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
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;



/**
 * @author cnsaeman
 */
public class PluginTVShow extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from Epguide etc.");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin downloads metadataabout TV-Show from epguide.com and tvrage.com.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"show|episode|season");
            put("type"              ,"manual|auto");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI="P:TVS>";
    
    private String result;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public String readlink(String l) {
        if (l.equals("http://epguides.com/")) return("##??");
        return(TextFile.ReadOutURL(l));
    }

    public void run() {
	String name=Information.get("show");
        String ep=Information.get("episode");
        String se=Information.get("season");
        try {
            name=Parser.Substitute(Parser.Substitute(name,"'","")," ","").toLowerCase();
            String blink="http://epguides.com/" + Parser.Substitute(name, " ", "");
            String post = "list=tv.com";
            String res = readlink(blink);
            //System.out.println(blink);
            //System.out.println(res);
            if (res.startsWith("##??")) {
                blink="http://epguides.com/" + Parser.Substitute(Parser.CutFrom(name," "), " ", "");
                res = readlink(blink);
            }
            if (res.startsWith("##??")) {
                blink="http://epguides.com/" + Parser.Substitute(Parser.Substitute(Parser.CutFrom(name," "), " ", ""),"-","");
                res = readlink(blink);
            }
            if (res.startsWith("##??")) {
                blink="http://epguides.com/" + Parser.Substitute(Parser.Substitute(name, " ", ""),"-","").toLowerCase();
                res = readlink(blink);
            }
            if (res.startsWith("##??")) {
                blink="http://epguides.com/" + Parser.Substitute(Parser.Substitute(name, " ", ""),"-","_").toLowerCase();
                res = readlink(blink);
            }
            if (res.startsWith("##??")) {
                blink="http://epguides.com/" + Parser.Substitute(Parser.Substitute(Parser.Substitute(name.toLowerCase(),"the",""), " ", ""),"-","");
                res = readlink(blink);
            }
            if (res.startsWith("##??")) {
                blink="http://epguides.com/" + Parser.Substitute(Parser.Substitute(name.substring(3), " ", ""),"-","");
                res = readlink(blink);
            }
            System.out.println("BLINK:::"+blink);
            if (!res.startsWith("##??")) {
                if ((ep==null) || (ep.trim().length()==0)) {
                    String info=Parser.CutTill(Parser.CutFrom(res,"<td>aired from:"),"</tr>");
                    Information.put("start",Parser.CutTill(Parser.CutFrom(info,"<em>"),"</em>"));
                    info=Parser.CutFrom(info,"</em>");
                    Information.put("end",Parser.CutTill(Parser.CutFrom(info,"<em>"),"</em>"));
                    info=Parser.CutFrom(info,"</td>");
                    Information.put("episodecount",Parser.CutTill(Parser.CutFrom(info,"<td>"),"</td>"));
                    info=Parser.CutFrom(info,"</td>");
                    Information.put("channel",Parser.CutTags(Parser.CutTill(Parser.CutFrom(info,"<td>"),"</td>")));
                    info=Parser.CutFrom(info,"</td>");
                    Information.put("duration",Parser.CutTill(Parser.CutFrom(info,"<td>"),"</td>"));
                    info=Parser.CutFrom(info,"</td>");
                    String actors=new String("");
                    String actors2=new String("");
                    Pattern p = Pattern.compile("<strong>(.+)</strong></a> as (.+)<");
                    Matcher m = p.matcher(res);
                    int i=0;
                    while (m.find(i)) {
                        actors+="|"+m.group(1)+" as "+m.group(2);
                        actors2+="|"+Parser.CutFromLast(m.group(1)," ")+", "+Parser.CutTillLast(m.group(1), " ");
                        i=m.end();
                    }
                    Information.put("cast",actors2.substring(1));
                    Information.put("people",actors2.substring(1));
                    Information.put("credits",actors.substring(1));
                    Information.put("epguide-link",blink);
                } else {
                    if (ep.startsWith("0")) ep=ep.substring(1);
                    if (se.startsWith("0")) se=se.substring(1);
                    res=Parser.CutFrom(res,"<pre>");
                    res=Parser.CutTill(res,"</pre>");
                    Pattern p = Pattern.compile(se+"\\-[ 0]?"+ep+"\\s+?\\S+\\s+(.+)\\s\\s\\s<a.+?href=[\"'](.+?)[\"']>(.+?)</a>");
                    Matcher m = p.matcher(res);
                    boolean found=m.find();
                    if  (!found) {
                        p=Pattern.compile(se+"\\-[ 0]?"+ep+"\\s+?(\\S.+\\d\\d)\\s\\s\\s<a.+?href=[\"'](.+?)[\"']>(.+?)</a>");
                        m = p.matcher(res);
                        found=m.find();
                    }
                    if  (!found) {
                        p=Pattern.compile(se+"[0]?"+ep+"\\s+?(.+)\\s\\s\\s<a.+?href=[\"'](.+?)[\"']>(.+?)</a>");
                        m = p.matcher(res);
                        found=m.find();
                    }
                    if (found) {
                        Information.put("attributes","hidden");
                        Information.put("title",m.group(3));
                        Information.put("air-date",m.group(1));
                        Information.put("recognition","50");
                        if (m.group(2).startsWith("http://www.tv.com")) {
                            Information.put("tvcom-link",m.group(2));
                            res = TextFile.ReadOutURL(Information.get("tvcom-link"));
                            if (!res.startsWith("##??")) {
                                Information.put("rating",Parser.CutFromLast(Parser.CutTill(Parser.CutFrom(res,"<h3>Episode Score</h3>"),"</span>"),">"));
                                Information.put("summary",Parser.CutFromLast(Parser.CutTill(Parser.CutFrom(res,"<h3>Episode Summary</h3>"),"</p>"),"<p>"));
                                Information.put("recognition","100");
                            }
                        }
                        if (m.group(2).startsWith("http://www.tvrage.com")) {
                            Information.put("tvrage-link",m.group(2));
                            res = TextFile.ReadOutURL(Information.get("tvrage-link"));
                            if (!res.startsWith("##??")) {
                                String summary=Parser.CutFrom(res,"</div><div>");
                                if (summary.startsWith("<img")) summary=Parser.CutFrom(summary,"'show_synopsis'>").trim();
                                Information.put("summary",Parser.CutTill(summary,"<br>"));
                                Information.put("recognition","100");
                            }
                        }
                    } else {
                        System.out.println("not found");
                        System.out.println(res);
                    }
                }
            }
        } catch (Exception e) {
            Msgs.add(e.toString());
        }
    }
  
                      
}

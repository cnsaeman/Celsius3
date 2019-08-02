/*
 * PluginUpdateComplete.java
 *
 * Created on 05. September 2007, 12:20
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
public class PluginUpdateComplete extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Update Publication Record");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.4");
            put("help"              ,"This plugin updates records using Inspire. ");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"inspirekey");
            put("type"              ,"manual");
            put("defaultParameters" ,"https://inspirehep.net/");
            put("parameter-help"    ,"Link to the Inspire mirror to be used.");
        }
    };


    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private String spiresbase;

    // Spaceless comparison
    private boolean comp(String a, String b) //throws IOException
    {
        if ((a == null) && (b == null)) {
            return (true);
        }
        if (a == null) {
            return (false);
        }
        if (b == null) {
            return (false);
        }
        a = a.replaceAll(" ", "").trim().toLowerCase();
        b = b.replaceAll(" ", "").trim().toLowerCase();
        return (a.equals(b));
    }

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public void run() {
        String inspirebase=Information.get("$$params");
        if (inspirebase==null) inspirebase="https://inspirehep.net/";

        String key = Information.get("inspirekey");
        celsius.BibTeXRecord BTR2=new celsius.BibTeXRecord(Information.get("bibtex"));
        if (!(key == null)) {
            if (((BTR2.get("journal")==null) || (BTR2.get("journal").equals(""))) &&
                ((BTR2.get("note")==null) || (BTR2.get("note").equals("")))) {
                Msgs.add("Updating information for " + Information.get("id") + "\ntitle: " + Information.get("title") + "\nauthors: " + Information.get("authors"));
                Msgs.add("Key::" + key);
                String bib = new String("");
                String lat = new String("");
                String gotit = TextFile.ReadOutURL(inspirebase+"record/"+key+"/export/hx");
                if (!gotit.startsWith("##??")) {
                    if (gotit.indexOf("<pre>")>-1) {
                        bib=Parser.CutTill(Parser.CutFrom(gotit,"<pre>"),"</pre>").trim();
                        if (bib.charAt(bib.length()-3)==',')
                            bib=Parser.CutTillLast(bib,",")+"\n}";
                    }
                } else {
                    Msgs.add("..Error receiving BibTeX data from internet");
                    Msgs.add(gotit);
                }
                if (bib.length() > 5) {
                    if (!comp(bib, Information.get("bibtex"))) {
                        celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(bib);
                        if (BTR.get("title")!=null) {
                            BTR.put("title", BTR2.get("title"));
                            BTR.put("author", BTR2.get("author"));
                        }
                        Msgs.add("Old:"+BTR2.toString());
                        // compare and adjust
                        if (BTR.keySet().size()>BTR2.keySet().size()) {
                            Msgs.add("..obtained bibtex: " + bib);
                            Msgs.add("..bibtex data: " + Information.get("bibtex"));
                            Msgs.add("..new bibtex:  " + BTR.toString());
                            Information.put("citation-tag",BTR.tag);
                            Information.put("bibtex", BTR.toString());
                            if (BTR.keySet().contains("journal")) {
                                Information.put("type","Paper");
                            } else {
                                Information.put("type","Preprint");
                            }
                            Information.put("identifier", toolbox.Identifier(Information));
                        } else {
                            Msgs.add("bibtex: nothing changed");
                        }
                    } else {
                        Msgs.add("bibtex: nothing changed");
                    }
                } else {
                    Msgs.add("bibtex received: "+bib);
                }
            }
        }
    }
}

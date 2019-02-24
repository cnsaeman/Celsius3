import java.util.ArrayList;
import java.util.HashMap;

public class PluginAdjustType extends Thread {
 
    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Adjust Type");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin tries to automatically adjust the document type.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    @Override
    public void run() {
        celsius.BibTeXRecord BTR=new celsius.BibTeXRecord(Information.get("bibtex"));
        String arx=Information.get("arxiv-name");
        if ((arx!=null) && (arx.length()>0))
            Information.put("type","Preprint");
        if (BTR.parseError==0) {
            if (BTR.get("journal").length()>0) Information.put("type","Paper");
        }
        String ptitle=Information.get("title");
        String remarks=Information.get("remarks");
        if (remarks==null) remarks=new String("");
        if (ptitle.indexOf("Lecture")>-1)
            Information.put("type","Lecture Notes");
        String ft=Information.get("filetype");
        if ((ft!=null) && (ft.equals("djvu")))
            Information.put("type","Book");
        String al=Information.get("amazon-link");
        if ((al!=null) && (al.length()>5))
            Information.put("type","Book");
        if (remarks.toLowerCase().indexOf("thesis")>-1)
            Information.put("type","Thesis");
        if (remarks.toLowerCase().indexOf("talk")>-1)
            Information.put("type","Talk");
    }
  
                      
}

/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;


/**
 * @author cnsaeman
 */
public class PluginMutopia extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from Mutopia Project");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin looks for an entry at Mutopia.org and downloads information.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"plaintxt");
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
  
    public void run() {
        String name=Information.get("plaintxt");
        if ((new File(name)).length()<20000) {
            try {
                GZIPInputStream fis = new GZIPInputStream(new FileInputStream(new File(name)));
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String currentLine;
                String ref=new String("");
                while ((currentLine = br.readLine()) != null) {
                    if (currentLine.indexOf("Mutopia-")>-1) {
                        ref=Parser.CutFrom(currentLine,"Mutopia-").trim();                        
                    }
                }
                br.close(); isr.close(); fis.close();
                if (!ref.equals("")) {
                    ref="http://www.mutopiaproject.org/cgibin/make-table.cgi?searchingfor=Mutopia-"+ref.replaceAll("/","%2F");
                    String inf = TextFile.ReadOutURL(ref);
                    inf=Parser.CutFrom(inf,"<table align=");
                    inf=Parser.CutFrom(inf,"<table align=");
                    inf=Parser.CutFrom(inf,"<td>");
                    Information.put("title", Parser.CutTill(inf,"</td>").trim());
                    inf=Parser.CutFrom(inf,"<td>by");
                    String authors=Parser.CutTill(inf,"(").trim();
                    Information.put("authors", Parser.CutFromLast(authors," "));
                    inf=Parser.CutFrom(inf,"<td>");
                    Information.put("opus", Parser.CutTill(inf,"</td>").trim());
                    inf=Parser.CutFrom(inf,"<td>for");
                    Information.put("instruments", Parser.CutTill(inf,"</td>").trim());
                    inf=Parser.CutFrom(inf,"<td>");
                    inf=Parser.CutFrom(inf,"<td>");
                    Information.put("period", Parser.CutTill(inf,"</td>").trim());
                    inf=Parser.CutFrom(inf,"<tr>");
                    inf=Parser.CutFrom(inf,"<td>");
                    Information.put("publisher",Parser.CutTill(inf,"</td>").trim());
                    inf=Parser.CutFrom(inf,"<td>");
                    inf=Parser.CutFrom(inf,">");
                    Information.put("copyright",Parser.CutTill(inf,"<").trim());
                    Information.put("mutopia-link",ref);
                    
                }
            } catch (Exception e) {
                    System.out.println(e.toString());
            }
            
        }
    }
  
                      
}

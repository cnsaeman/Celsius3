/*
 * PluginHeader.java
 *
 * Created on 17. October 2009, 16:50
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import celsius.tools.ExecutionShell;



/**
 * @author cnsaeman
 */
public class PluginConvertps2pdf extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Convert ps.gz to pdf");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin converts a gzipped postscript file into pdf format.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI="P:Con1>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public void run() {
	String location=Information.get("fullpath");
        String ft=Information.get("filetype");
        if (ft.equals("ps.gz")) {
            try {
                TextFile.CopyFile(location, "tmp.ps.gz");
                TextFile.GUnZip("tmp.ps.gz");
                String cmd = "ps2pdf tmp.ps tmp.pdf";
                ExecutionShell ES = new ExecutionShell(cmd, 0, false);
                ES.start();
                ES.join();
                if (ES.errorflag) {
                    Msgs.add(TI + "Error Message: " + ES.errorMsg);
                } else {
                    TextFile.Delete(location);
                    location=location.substring(0,location.length()-5);
                    TextFile.moveFile("tmp.pdf", location+"pdf");
                    Information.put("filetype","pdf");
                    location=Information.get("location");
                    location=location.substring(0,location.length()-5);
                    Information.put("location", location+"pdf");
                }
            } catch (Exception e) {
                Msgs.add(TI + "Error: "+e.toString());
                e.printStackTrace();
            }
            //TextFile.Delete("tmp.ps.gz");
            //TextFile.Delete("tmp.pdf");
        }
    }
  
                      
}

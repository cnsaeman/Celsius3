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



/**
 * @author cnsaeman
 */
public class PluginHeader extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from header");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for metadata in the plain text information of a file.");
            put("needsFirstPage"    ,"yes");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI="P:GfH>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public void run() {
	String FirstPage=Information.get("firstpage");
        if (FirstPage.indexOf("$$metadata")>-1) {
	    String md=Parser.CutTill(Parser.CutFrom(FirstPage,"$$metadata"),"metadata$$").trim();
	    String line;
	    while (md.length()>0) {
	      line=Parser.CutTill(md,"\n");
	      Information.put(Parser.CutTill(line,":"),Parser.CutFrom(line,":").trim());
	      md=Parser.CutFrom(md,"\n");
	    }
        }
    }
  
                      
}

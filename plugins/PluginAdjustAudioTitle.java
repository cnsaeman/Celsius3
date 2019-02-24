/*
 * PluginAmazon.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;

/**
 * @author cnsaeman
 */
public class PluginAdjustAudioTitle extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Adjust Audio Titles");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin tries to adjust the title/maintitle/fulltitle fields of an mp3-file.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"title");
            put("type"              ,"manual");
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
        if (Information.containsKey("maintitle") && Information.containsKey("title")) {
            Information.put("fulltitle",Information.get("maintitle")+" - "+Information.get("title"));
        } else {

        }
    }
  
}

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;



public class PluginCreatePlaylist extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Create Playlist");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin creates a playlist for the linked files.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"$$linkedFiles");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
            put("finalize"          ,"no");
        }
    };

    public final static String linesep = System.getProperty("line.separator");  // EndOfLine signal
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public void run() {
        //System.out.println("A");
        if (Information.containsKey("$$linkedFiles")) {
            System.out.println("B");
            String[] fn=Information.get("$$linkedFiles").split("\\|");
            String pln=new String("");
            if (Information.containsKey("composer")) pln+=" - "+Information.get("composer");
            pln+=" - "+Information.get("fulltitle");
            if (Information.containsKey("artists")) pln+=" - "+Information.get("artists");
            /*System.out.println(fn);
            System.out.println(fn.length);
            System.out.println(pln);
            System.out.println(fn[0]);*/
            pln=(new File(fn[0])).getParent()+File.separator+celsius.tools.Parser.CutProhibitedChars(pln.substring(3))+".m3u";
            //System.out.println("C");
            try {
                celsius.tools.TextFile PL = new celsius.tools.TextFile(pln, false);
                for (String s : fn) {
                    PL.putString((new File(s)).getName());
                }
                PL.close();
                Information.put("location", "/Compress:"+pln);
                Information.put("filetype","m3u");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            
        }
    }

}

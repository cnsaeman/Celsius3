/*
 * PluginFileName.java
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
public class PluginFileNameTV extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from Filename, TV");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin assumes a file is in a folder with the series name and contains season/episode information in its filename.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public void run() {
        String FileName = Parser.CutFromLast(Information.get("fullpath"), arxivTools.filesep);
	if (FileName.endsWith(".avi") || FileName.endsWith(".mpg") || FileName.endsWith(".mkv") || FileName.endsWith(".rm") || FileName.endsWith(".mp4") || FileName.endsWith(".flv")) {
            String show = Parser.CutFromLast(Parser.CutTillLast(Information.get("fullpath"),arxivTools.filesep), arxivTools.filesep);
            Information.put("show",show);
            Pattern p = Pattern.compile("[sS](\\d\\d)[eE](\\d\\d)");
            Matcher m = p.matcher(FileName);
            boolean found=false;
            found=m.find();
            if (!found) {
                p = Pattern.compile("(\\d\\d)(\\d\\d)");
                m = p.matcher(FileName);
                found=m.find();
            }
            if (!found) {
                p = Pattern.compile("(\\d+)x(\\d\\d)");
                m = p.matcher(FileName);
                found=m.find();
            }
            if (!found) {
                p = Pattern.compile("[sS](\\d)[eE](\\d\\d)");
                m = p.matcher(FileName);
                found=m.find();
            }
            if (found) {
                Information.put("season",m.group(1));
                Information.put("episode",m.group(2));
            }
            p = Pattern.compile("(\\d+?) - (.+)\\....");
            m = p.matcher(FileName);
            found=m.find();
            if (found) {
                if (Information.get("season")==null)
                    Information.put("season",m.group(1));
                Information.put("title",m.group(2));
            }
	}
    }
}

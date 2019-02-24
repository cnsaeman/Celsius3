/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.StreamGobbler;

/**
 * @author cnsaeman
 */
public class PluginGetAudioData extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get Audio Data");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin reads out Audio Metadata from an audio file. It requires audioreader/jaudiotagger, which is included in the plugins folder.");
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

    public void putInfo(String tag,String value) {
        if (value==null) return;
        if (value.trim().length()==0) return;
        String currentValue=Information.get(tag);
        //System.out.println(tag+"::"+value);
        if ((currentValue==null) || (value.length()>Information.get(tag).length()-5)) {
            Information.put(tag,value);
        }
    }

    public void run() {
        String fp = Information.get("fullpath");
        try {
            if (fp.startsWith(".")) return;
            if (fp.endsWith("mp3") || fp.endsWith("m4a") || fp.endsWith("flac") || fp.endsWith("mp4") || fp.endsWith("ogg")) {
                ArrayList<String> args=new ArrayList<String>();
                System.out.println(new File(".").getAbsolutePath());
                args.add("java");
                args.add("-classpath");
                args.add("./plugins:./plugins/jaudiotagger-2.0.3.jar");
                args.add("audioreader");
                args.add(fp);
                boolean completed=false;
                Process p = (new ProcessBuilder(args)).start();
                //Process p = Runtime.getRuntime().exec(cmdln);
                StreamGobbler SGout=new StreamGobbler(p.getInputStream(),"Output");
                StreamGobbler SGerr=new StreamGobbler(p.getErrorStream(),"Error");
                SGout.start();
                SGerr.start();
                int m=0;
                int ret=0;
                System.out.println("IN");
                // go through loop at most 150 times=30 secs o
                while ((!completed) && (m<100)) {
                    try {
                        sleep(200); ret=p.exitValue(); completed=true;
                    } catch (IllegalThreadStateException e) { m++;  }
                }
                /*System.out.println("Return from external program:");
                System.out.println(m);
                System.out.println(SGout.getOutput());
                System.out.println(SGerr.getOutput());
                System.out.println(ret);
                System.out.println(completed);
                System.out.println("-----------");
                System.out.println(SGout.getOutput());
                System.out.println(SGerr.getOutput());*/
                // Write Length information:
                String out=SGout.getOutput();
                if (out.indexOf("Length##")>-1) {
                    String length=celsius.tools.Parser.CutTill(celsius.tools.Parser.CutFrom(out,"Length##"),"\n");
                    Information.put("duration", length);
                    String analyze=celsius.tools.Parser.CutFrom(celsius.tools.Parser.CutFrom(out,"Length##"),"\n");
                    while (analyze.indexOf("##")>-1) {
                        String line=celsius.tools.Parser.CutTill(analyze, "\n");
                        String tag=celsius.tools.Parser.CutTill(line,"##").trim();
                        String value=celsius.tools.Parser.CutFrom(line,"##").trim();
                        putInfo(tag,value);
                        analyze=celsius.tools.Parser.CutFrom(analyze, "\n");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

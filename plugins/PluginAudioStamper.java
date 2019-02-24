/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.StreamGobbler;

/**
 * @author cnsaeman
 */
public class PluginAudioStamper extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Stamp Audio Header");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin writes metadata into an audio file. It requires audiowriter/jaudiotagger.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public final String[] fields={"artists","composer","fulltitle","album","comment","year","number","genre"};

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
    }

    public void run() {
        String fp = Information.get("fullpath");
        try {
            if (fp.endsWith("mp3") || fp.endsWith("m4a") || fp.endsWith("flac") || fp.endsWith("mp4") || fp.endsWith("ogg")) {
                ArrayList<String> args=new ArrayList<String>();
                System.out.println(new File(".").getAbsolutePath());
                args.add("java");
                args.add("-classpath");
                args.add("./plugins:./plugins/jaudiotagger-2.0.3.jar");
                args.add("audiowriter");
                args.add(fp);
                for (String s : fields) {
                    if (!Information.isEmpty(s)) {
                        args.add(s+":\""+Information.get(s)+"\"");
                    }
                }
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
                System.out.println("Return from external program:");
                System.out.println(m);
                System.out.println(SGout.getOutput());
                System.out.println(SGerr.getOutput());
                System.out.println(ret);
                System.out.println(completed);
                System.out.println("-----------");
                System.out.println(SGout.getOutput());
                System.out.println(SGerr.getOutput());
                // Write Length information:
                String out=SGout.getOutput();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

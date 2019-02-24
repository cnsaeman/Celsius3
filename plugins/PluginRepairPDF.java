/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import celsius.tools.StreamGobbler;


/**
 * @author cnsaeman
 */
public class PluginRepairPDF extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Repair PDF");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin repairs pdf files. It needs pdftk installed.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public String source;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }

    public void run() {
        String fp = Information.get("fullpath");
        String target=Information.get("fullpath")+".jpg";
	ArrayList<String> args=new ArrayList<String>();
	args.add("pdftk");
	args.add(fp);
	args.add("output");
	args.add("output.pdf");
        boolean completed=false;
        try {
            TextFile.Delete("output.pdf");
	    Process p = (new ProcessBuilder(args)).start();
            //Process p = Runtime.getRuntime().exec(cmdln);
            StreamGobbler SGout=new StreamGobbler(p.getInputStream(),"Output");
            StreamGobbler SGerr=new StreamGobbler(p.getErrorStream(),"Error");
            SGout.start();
            SGerr.start();
            int m=0;
            int ret=0;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
	if (completed) {
            if (new File("output.pdf").length()>10000) {
                System.out.println("YES");
                System.out.println(fp);
                try {
                TextFile.Delete(fp);
                TextFile.moveFile("output.pdf", fp);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                Information.put("registered",Information.get("registered")+"|tmpit2");
                System.out.println("NO");
                System.out.println(fp);
            }
        }
    }
  
                      
}

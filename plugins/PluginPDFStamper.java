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
public class PluginPDFStamper extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Stamp PDF Header");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin writes title/authors information to a pdf-file. It needs pdftk installed.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"manual");
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

    public String getS(String key) {
        String ret=Information.get(key);
        if (ret==null) ret=new String("");
        ret=Parser.Substitute(ret,"(","\\(");
        ret=Parser.Substitute(ret,")","\\)");
        return(ret);
    }

    public String field(String key) {
        String key2=key.toLowerCase();
        if (!getS(key2).equals("")) {
            return("InfoKey: "+key+"\n"+"InfoValue: "+getS(key2)+"\n");
        }
        return("");
    }

    public String createOutput() {
        String ret=new String("");
        ret="InfoKey: Author\nInfoValue: "+getS("authors")+"\n";
        ret+="InfoKey: Title\nInfoValue: "+getS("title")+"\n";
        if (!getS("keywords").equals(""))
            ret+="InfoKey: Title\nInfoValue: "+Parser.Substitute(getS("title"),"|",",")+"\n";
        ret+=field("CreationDate");
        ret+=field("Creator");
        ret+=field("Producer");
        ret+=field("ModDate");
        ret+=field("Version");
        return(ret);
    }

    public void run() {
        String fp = Information.get("fullpath");
        try {
            if (fp.endsWith(".pdf")) {
                RandomAccessFile RAF = new RandomAccessFile(fp, "rw");
                String line = new String("");
                line = RAF.readLine();
                line = RAF.readLine();
                while ((line != null) && !(line.indexOf("/CreationDate") > -1)) {
                    line = RAF.readLine();
                }
                if (line != null) {
                    long p = RAF.getFilePointer();
                    if (p<1000) p=1000;
                    RAF.seek(p - 1000);
                    byte[] buffer = new byte[2000];
                    RAF.read(buffer);
                    String b = new String(buffer);
                    int i = b.indexOf("/CreationDate");
                    int j = b.lastIndexOf("<<", i);
                    int k = b.indexOf(">>", i);
                    String res=b.substring(j+2,k);
                    Pattern pat = Pattern.compile("/(.+?[^\\\\])\\((.*?[^\\\\])\\)");
                    Matcher m = pat.matcher(b);
                    i=0;
                    while (m.find(i)) {
                        String tag = m.group(1).trim().toLowerCase();
                        String value = m.group(2).trim();
                        if (value.charAt(0)==')') {
                            value="";
                            i++;
                        } else {
                            i=m.end();
                        }
                        if ((tag.indexOf('<')==-1) && (tag.indexOf('>')==-1)) {
                            if (!value.equals("")) {
                                if (tag.equals("keywords")) {
                                  value=value.replace(", ","|");
                                  value=value.replace(", ","|");
                                }
                                System.out.println("Found: " + tag + "::" + value);
                                if (!Information.containsKey(tag)) {
                                    Information.put(tag, value);
                                }
                            }
                        } else {
                            i=b.length();
                        }
                    }
                }
                RAF.close();
                ArrayList<String> args=new ArrayList<String>();
                TextFile TF=new TextFile("info.txt.tmp",false);
                TF.putString(createOutput());
                TF.close();
                args.add("pdftk");
                args.add(fp);
                args.add("update_info");
                args.add("info.txt.tmp");
                args.add("output");
                args.add("output.pdf");
                boolean completed=false;
                TextFile.Delete("output.pdf");
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

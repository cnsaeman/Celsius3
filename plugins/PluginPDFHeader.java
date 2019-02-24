/*
 * PluginArXiv.java
 *
 * Created on 14. Juli 2007, 12:20
 *
 * complete, testing
 */

import java.io.RandomAccessFile;
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
public class PluginPDFHeader extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from PDF Header");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.1");
            put("help"              ,"This plugin reads out the header information of a pdf-file.");
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
        String fp = Information.get("fullpath");
        if (fp.endsWith(".pdf")) {
            try {
                RandomAccessFile RAF = new RandomAccessFile(fp, "r");
                String line = new String("");
                line = RAF.readLine();
                while ((line!=null) && !(line.indexOf("/CreationDate")>-1)) {
                    line = RAF.readLine();
                }
                if (line!=null) {
                    long p=RAF.getFilePointer()-1000;
                    if (p<0) p=0;
                    RAF.seek(p);
                    byte[] buffer=new byte[2000];
                    RAF.read(buffer);
                    String b=new String(buffer);
                    int i=b.indexOf("/CreationDate");
                    int j=b.lastIndexOf("<<",i);
                    int k=b.indexOf(">>",i);
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
                                value=Parser.Substitute(value,"\\(","(");
                                value=Parser.Substitute(value,"\\)",")");
                                if (tag.equals("keywords")) {
                                  value=value.replace(", ","|");
                                  value=value.replace(", ","|");
                                }
                                //System.out.println("Found: " + tag + "::" + value);
                                if (!Information.containsKey(tag)) {
                                    Information.put(tag, value);
                                }
                            }
                        }
                    }
                }
                RAF.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

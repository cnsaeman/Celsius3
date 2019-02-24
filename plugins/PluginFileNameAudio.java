/*
 * PluginHeader.java
 *
 * Created on 17. October 2009, 16:50
 *
 * complete, testing
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;



/**
 * @author cnsaeman
 */
public class PluginFileNameAudio extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from filename (audio)");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin interprets a file's position in the folder hierarchy.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public final static String filesep = System.getProperty("file.separator");  // EndOfLine signal
    private final String TI="P:GfH>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private RandomAccessFile RAF;

    private HashMap<String,String> id3v2;
    private int error=0;


    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
        id3v2=new HashMap<String,String>();
        error=0;
    }

    public void run() {
        String fp = Information.get("fullpath");
        if (fp.startsWith(".")) return;
        if (fp.endsWith("mp3") || fp.endsWith("m4a") || fp.endsWith("flac") || fp.endsWith("mp4") || fp.endsWith("ogg")) {
            System.out.println("RUN PFNA----------");
            int type=Integer.valueOf(Parser.CutFromLast(Parser.CutTill(fp,"."),"/"));
            System.out.println(fp);
            System.out.print(type);
            if (type<6) {
                String rem=Parser.CutFrom(fp,".");
                putInfo("genre",Parser.CutTill(rem,filesep));
                rem=Parser.CutFrom(rem,filesep);
                Information.put("composer",Parser.CutTill(rem,filesep));
                rem=Parser.CutFrom(rem,filesep);
                String pre=new String("");
                if (rem.indexOf(filesep)>-1) {
                    pre=Parser.CutTill(rem,filesep);
                    if (pre.indexOf("[")>-1) 
                        putInfo("year",Parser.CutTill(Parser.CutFrom(pre,"["),"]"));
                    if (pre.indexOf("{")>-1)
                        putInfo("composer",Parser.CutTill(Parser.CutFrom(pre,"{"),"}"));
                    if (pre.indexOf("(")>-1)
                        putInfo("artists",Parser.CutTill(Parser.CutFrom(pre,"("),")"));
                    pre=Parser.CutTill(Parser.CutTill(Parser.CutTill(pre,"("),"["),"{").trim();
                    putInfo("album",pre);
                    rem=Parser.CutFrom(rem,filesep);
                }
                if (rem.indexOf(filesep)>-1) {
                    pre+=" - "+Parser.CutTill(rem,filesep);
                    if (pre.indexOf("[")>-1)
                        putInfo("year",Parser.CutTill(Parser.CutFrom(pre,"["),"]"));
                    if (pre.indexOf("{")>-1)
                        putInfo("composer",Parser.CutTill(Parser.CutFrom(pre,"{"),"}"));
                    if (pre.indexOf("(")>-1)
                        putInfo("artists",Parser.CutTill(Parser.CutFrom(pre,"("),")"));
                    pre=Parser.CutTill(Parser.CutTill(Parser.CutTill(pre,"("),"["),"{").trim();
                    putInfo("album",pre);
                    rem=Parser.CutFrom(rem,filesep);
                }
                if (rem.indexOf(filesep)>-1) rem=Parser.CutFromLast(rem,filesep);
                String name=Parser.CutTillLast(rem,".");
                int i=0;
                while ((i<name.length()) && (Character.isDigit(name.charAt(i)))) i++;
                if (i>0) {
                    putInfo("number",name.substring(0,i).trim());
                    i++;
                    if (name.substring(i).trim().startsWith("-"))
                        name=Parser.CutFrom(name,"-").trim();
                }
                if (!pre.equals("")) pre+=" - ";
                pre+=name.trim();
                while (Character.isDigit(pre.charAt(0)) || (pre.charAt(0)=='.') || (pre.charAt(0)=='-')) pre=pre.substring(1).trim();
                System.out.println(pre);
                putInfo("title",name.substring(i).trim());
                if (pre.indexOf("-")>-1) putInfo("maintitle",Parser.CutTill(pre,"-").trim());
                putInfo("recognition","100");
            } else {
                if ((type==6) || (type==9) || (type==14) || (type==15) || (type==11) || (type==10)) {
                    System.out.println("Type:"+Integer.toString(type));
                    String rem = Parser.CutFrom(fp, ".");
                    rem = Parser.CutFrom(rem, filesep);
                    System.out.println(Parser.CutTill(rem, filesep));
                    putInfo("album", Parser.CutTill(rem, filesep));
                    if (type==10) Information.put("maintitle", Parser.CutTill(rem, filesep));
                    String name = Parser.CutFromLast(Parser.CutTillLast(fp, "."), "/");
                    int i = 0;
                    while (Character.isDigit(name.charAt(i))) {
                        i++;
                    }
                    if (i > 0) {
                        putInfo("number", name.substring(0, i));
                        name = name.substring(i);
                        if (name.charAt(0) == '.') {
                            name = name.substring(1);
                        }
                    }
                    if (name.indexOf("{") > -1) {
                        name = Parser.CutTill(name, "{").trim();
                    }
                    if (name.trim().startsWith("-")) name=name.trim().substring(1).trim();
                    putInfo("title", name);
                    putInfo("fulltitle", name);
                    putInfo("recognition","100");
                }
                if (type==7) {
                    System.out.println("Type:7");
                    String rem = Parser.CutFrom(fp, ".");
                    rem = Parser.CutFrom(rem, filesep);
                    System.out.println(Parser.CutTill(rem, filesep));
                    putInfo("artists", Parser.CutTill(rem, filesep));
                    System.out.println("done.");
                    rem = Parser.CutFrom(rem, filesep);
                    if (rem.indexOf(filesep)>-1) {
                        putInfo("album", Parser.CutTill(rem, filesep));
                    }
                    String name = Parser.CutFromLast(Parser.CutTillLast(fp, "."), "/");
                    int i = 0;
                    while (Character.isDigit(name.charAt(i))) {
                        i++;
                    }
                    if (i > 0) {
                        putInfo("number", name.substring(0, i));
                        name = name.substring(i);
                        if (name.charAt(0) == '.') {
                            name = name.substring(1);
                        }
                    }
                    if (name.indexOf("{") > -1) {
                        name = Parser.CutTill(name, "{").trim();
                    }
                    if (name.trim().startsWith("-")) name=name.trim().substring(1).trim();
                    putInfo("title", name);
                    putInfo("fulltitle", name);
                    putInfo("recognition","100");
                }
                if ((type==8) || (type==13) || (type==16)) {
                    System.out.println("Type:"+Integer.toString(type));
                    String rem = Parser.CutFrom(fp, ".");
                    rem = Parser.CutFrom(rem, filesep);
                    System.out.println(Parser.CutTill(rem, filesep));
                    putInfo("artists", Parser.CutTill(rem, filesep));
                    System.out.println("done.");
                    rem = Parser.CutFrom(rem, filesep);
                    if (rem.indexOf(filesep)>-1) {
                        putInfo("album", Parser.CutTill(rem, filesep));
                    }
                    String name = Parser.CutFromLast(Parser.CutTillLast(fp, "."), "/");
                    int i = 0;
                    while (Character.isDigit(name.charAt(i))) {
                        i++;
                    }
                    if (i > 0) {
                        putInfo("number", name.substring(0, i));
                        name = name.substring(i);
                        if (name.charAt(0) == '.') {
                            name = name.substring(1);
                        }
                    }
                    if (name.indexOf("{") > -1) {
                        name = Parser.CutTill(name, "{").trim();
                    }
                    if (name.trim().startsWith("-")) name=name.trim().substring(1).trim();
                    putInfo("title", name);
                    putInfo("fulltitle", name);
                    if (type==7) putInfo("genre","Jazz");
                    if (type==13) putInfo("genre","Pop");
                    putInfo("recognition","100");
                }
                if (type==17) {
                    System.out.println("Type:"+Integer.toString(type));
                    String rem = Parser.CutFrom(fp, ".");
                    rem = Parser.CutFrom(rem, filesep);
                    System.out.println(Parser.CutTill(rem, filesep));
                    putInfo("composer", Parser.CutTill(rem, filesep));
                    System.out.println("done.");
                    rem = Parser.CutFrom(rem, filesep);
                    if (rem.indexOf(filesep)>-1) {
                        putInfo("album", Parser.CutTill(rem, filesep));
                    }
                    String name = Parser.CutFromLast(Parser.CutTillLast(fp, "."), "/");
                    int i = 0;
                    while (Character.isDigit(name.charAt(i))) {
                        i++;
                    }
                    if (i > 0) {
                        putInfo("number", name.substring(0, i));
                        name = name.substring(i);
                        if (name.charAt(0) == '.') {
                            name = name.substring(1);
                        }
                    }
                    if (name.indexOf("{") > -1) {
                        name = Parser.CutTill(name, "{").trim();
                    }
                    putInfo("title", name);
                    putInfo("fulltitle", name);
                    if (name.trim().startsWith("-")) name=name.trim().substring(1).trim();
                    putInfo("recognition","100");
                }
            }
            if (fp.indexOf("{")>-1) {
                String composer=Parser.CutFrom(Parser.CutTill(fp,"}"),"{");
                System.out.println("Composer::"+composer);
                putInfo("composer",composer);
            }
            if (fp.indexOf("[")>-1) {
                String year=Parser.CutFrom(Parser.CutTill(fp,"]"),"[");
                System.out.println("Year::"+year);
                putInfo("year",year);
            }
            if (fp.indexOf("(")>-1) {
                String artist=Parser.CutFrom(Parser.CutTill(fp,")"),"(");
                System.out.println("Artist::"+artist);
                putInfo("artists",artist);
            }
        }
        if (Information.isEmpty("fulltitle")) Information.put("fulltitle",Parser.CutTillLast(Parser.CutFromLast(fp,"/"),"."));
        if (Information.get("recognition").equals("99")) putInfo("recognition","100");
    }

    public void putInfo(String tag,String value) {
        //System.out.println("GO!");
        //System.out.println(value);
        if (value==null) return;
        if (value.trim().length()==0) return;
        String currentValue=Information.get(tag);
        if ((currentValue!=null) && (currentValue.equals("unknown"))) currentValue=null;
        //System.out.println("Entering...");
        //System.out.println(currentValue);
        if ((currentValue==null) || (value.length()>Information.get(tag).length()-5)) {
            Information.put(tag,value);
            //System.out.println("Information written::"+tag+"::"+value);
        } else {
            //System.out.println("Information blocked::"+tag+"::blocking:"+Information.get(tag)+"::"+value);
        }
    }
                      
}

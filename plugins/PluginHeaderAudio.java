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
import java.util.Arrays;
import java.util.HashMap;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;



/**
 * @author cnsaeman
 */
public class PluginHeaderAudio extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Get from header (mp3)");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin looks for the metadata of an mp3-file.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    private final String TI="P:GfH>";
    
    private String oai;
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    private RandomAccessFile RAF;

    private HashMap<String,String> id3v2;
    private int error=0;

    private String version;

    private byte[] data;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
        id3v2=new HashMap<String,String>();
        error=0;
    }

    public void run() {
        String fp = Information.get("fullpath");
        System.out.println(fp);
        if (fp.toLowerCase().endsWith(".mp3")) {
            try {
                RAF = new RandomAccessFile(fp, "r");
                readID3v1();
                readID3v2();
                RAF.close();
                if (!Information.containsKey("number"))
                    Information.put("number","00");
	    } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private String bytesToString(byte[] ar,int st, int end) {
        return(new String(Arrays.copyOfRange(ar, st, end)));
    }

    public void readID3v1() throws IOException {
        RAF.seek(RAF.length()-128);
        byte[] data1=new byte[128];
        RAF.read(data1);
        if (bytesToString(data1,0,3).equals("TAG")) {
            putInfo("title",bytesToString(data1,3,33).trim());
            putInfo("fulltitle",bytesToString(data1,3,33).trim());
            putInfo("artists",bytesToString(data1,33,63).trim());
            putInfo("album",bytesToString(data1,63,93).trim());
            putInfo("year",bytesToString(data1,93,97).trim());
            putInfo("comment",bytesToString(data1,97,126).trim());
            String n=Byte.toString(data1[126]);
            if (n.length()<2) n="0"+n;
            if (!n.equals("00")) {
                putInfo("number",n);
                putInfo("albumtrack",n);
            }
            putInfo("genre",Byte.toString(data1[127]));
            putInfo("id3v1tag","yes");
            putInfo("recognition","99");
        }
    }

    public void fullyRecognized() {
        if (Information.get("fulltitle").length()>1) {
            Information.put("recognition","99");
        }
    }

    public void continueOld() throws IOException {
        int flags = data[5];
        int size = 128 * 128 * 128 * data[6] + 128 * 128 * data[7] + 128 * data[8] + data[9];
        //System.out.print("Total Size:");
        //System.out.println(size);
            int pos = 10;
            RAF.seek(pos);
            while ((pos < size) && (error==0)) {
		byte[] id=new byte[3];
		RAF.read(id);
                //System.out.println("Frame::"+(new String(id)));
		if (id[0]==0) {
                    Msgs.add("Frame starting with 0 found");
                    error=1;
                }
		int fsize=128*128*RAF.readUnsignedByte()+128*RAF.readUnsignedByte()+RAF.readUnsignedByte()-2;
                //System.out.print("Framesize:");
                //System.out.println(fsize);
		if (fsize>10000) {
                    Msgs.add("Framesize error");
                    error=2;
                }
		byte[] bflags=new byte[1];
		RAF.read(bflags);
                if (fsize > 0) {
                    if ((new String(id)).equals("COMM")) {
                        fsize -= 3;
                        RAF.readByte();
                        RAF.readByte();
                        RAF.readByte();
                    }
                    byte[] contents = new byte[fsize];
                    RAF.read(contents);
                    RAF.readUnsignedByte();
                    if (!id3v2.containsKey(new String(id)))
                        id3v2.put(new String(id), new String(contents));
                    contents = null;
                }
                /*for (String t : id3v2.keySet()) {
                    System.out.println(t+" : "+id3v2.get(t));
                }*/
                pos += fsize + 11;
                if ((new String(id)).equals("COMM")) {
                    pos += 3;
                }
                bflags = null;
            }
            putInfo("id3v2tag","yes");
            putInfo("title",id3v2.get("TT2"));
            putInfo("fulltitle",id3v2.get("TT2"));
            putInfo("artists",id3v2.get("TP1"));
            putInfo("album",id3v2.get("TAL"));
            putInfo("comment",id3v2.get("COM"));
            putInfo("number",id3v2.get("TRK"));
            putInfo("albumtrack",id3v2.get("TRK"));
            putInfo("year",id3v2.get("TYE"));
            putInfo("year2",id3v2.get("TDRC"));
            putInfo("year3",id3v2.get("TDRL"));
            putInfo("genre",id3v2.get("TCON"));
            putInfo("duration",id3v2.get("TLEN"));
            putInfo("remarks",id3v2.get("COMM"));
            putInfo("encoder",id3v2.get("TEN"));
            putInfo("setting",id3v2.get("TSSE"));
            putInfo("remarks",id3v2.get("COMM"));
            fullyRecognized();
    }
  
    public void readID3v2() throws IOException {
        RAF.seek(0);
        data=new byte[10000];
        RAF.read(data);
        String firstbytes=new String(data);
        if (firstbytes.startsWith("ID3")) {
            version = String.valueOf(data[3]) + "." + String.valueOf(data[4]);
            putInfo("ID3v2tagversion",version);
            if (data[3]==2) {
                continueOld();
                return;
            }
            int flags = data[5];
            int size = 128 * 128 * 128 * data[6] + 128 * 128 * data[7] + 128 * data[8] + data[9];
            //System.out.print("Total Size:");
            //System.out.println(size);
            int pos = 10;
            while ((pos < size) && (error==0)) {
		byte[] id=new byte[4];
		RAF.seek(pos);
		RAF.read(id);
                //System.out.println("Frame::"+(new String(id)));
		if (id[0]==0) {
                    Msgs.add("Frame starting with 0 found");
                    error=1;
                }
		int fsize=128*128*128*RAF.readUnsignedByte()+128*128*RAF.readUnsignedByte()+128*RAF.readUnsignedByte()+RAF.readUnsignedByte()-1;
                //System.out.print("Framesize:");
                //System.out.println(fsize);
		if (fsize>10000) {
                    Msgs.add("Framesize error");
                    error=2;
                }
		byte[] bflags=new byte[2];
		RAF.read(bflags);
		RAF.readByte();
                if (fsize > 0) {
                    if ((new String(id)).equals("COMM")) {
                        fsize -= 3;
                        RAF.readByte();
                        RAF.readByte();
                        RAF.readByte();
                    }
                    byte[] contents = new byte[fsize];
                    RAF.read(contents);
                    id3v2.put(new String(id), new String(contents));
                    contents = null;
                }
                /*for (String t : id3v2.keySet()) {
                    System.out.println(t+" : "+id3v2.get(t));
                }*/
                pos += fsize + 11;
                if ((new String(id)).equals("COMM")) {
                    pos += 3;
                }
                bflags = null;
            }
            putInfo("id3v2tag","yes");
            putInfo("title",id3v2.get("TIT2"));
            putInfo("fulltitle",id3v2.get("TIT2"));
            putInfo("artists",id3v2.get("TPE1"));
            putInfo("album",id3v2.get("TALB"));
            putInfo("composer",id3v2.get("TCOM"));
            putInfo("number",id3v2.get("TRCK"));
            putInfo("albumtrack",id3v2.get("TRCK"));
            putInfo("year",id3v2.get("TDOR"));
            putInfo("year2",id3v2.get("TDRC"));
            putInfo("year3",id3v2.get("TDRL"));
            putInfo("comment",id3v2.get("COMM"));
            putInfo("genre",id3v2.get("TCON"));
            putInfo("duration",id3v2.get("TLEN"));
            putInfo("remarks",id3v2.get("COMM"));
            putInfo("encoder",id3v2.get("TENC"));
            putInfo("setting",id3v2.get("TSSE"));
            putInfo("remarks",id3v2.get("COMM"));
            fullyRecognized();
        }
    }
                      
}
